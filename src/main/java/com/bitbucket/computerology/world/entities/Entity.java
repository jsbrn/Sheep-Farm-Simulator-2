package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.Chunk;
import com.bitbucket.computerology.world.entities.components.Position;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Graphics;

public class Entity {
    
    private ArrayList<ComponentSystem> systems;
    private ArrayList<Component> components;
    private ArrayList<Flow> flows;
    String name, type;
    int id;
    
    /**
     * Creates a new entity.
     * as well as the systems Movement and Render.
     * @param x The x-coordinate of the entity (world).
     * @param y The y-coordinate of the entity (world).
     * @param id The ID of the entity.
     * @param name The name of the entity.
     * @return The created entity instance.
     */
    public static Entity create(String type) {
        Entity e = new Entity(), clone = EntityList.getEntity(type);
        if (clone == null) {
            System.err.println("Could not find entity of type "+type+" in ENTITY_LIST");
            return null;
        }
        clone.copyTo(e);
        e.type = type;
        e.id = Math.abs(new Random().nextInt() % 10000000);
        return e;
    }
    
    public Entity() {
        this.systems = new ArrayList<ComponentSystem>();
        this.components = new ArrayList<Component>();
        this.flows = new ArrayList<Flow>();
        this.name = "";
        this.type = "";
        this.id = -1;
    }
    
    public static int maxSizeChunks() { return 16; }
    public static int maxSizePixels() { return maxSizeChunks()*Chunk.sizePixels(); }
    
    /**
     * Checks if the entity intersects the world coordinates and the specified dimensions.
     * @return A boolean. Returns true always, for now. Needs to be implemented.
     */
    public boolean intersects(int x, int y, int w, int h) {
        return true;
    }
    
    public boolean isImportant() { return !flows.isEmpty(); }
    
    public void save(BufferedWriter bw) {
        try {
            bw.write("e\n");
            bw.write("t="+type+"\n");
            bw.write("n="+name+"\n");
            bw.write("id="+id+"\n");
            for (Component c: components) c.save(bw);
            bw.write("/e\n");
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public final boolean load(BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.replace("", "").trim();
                if (line.equals("/e")) return true;
                if (line.indexOf("t=") == 0) {
                    Entity copy = Entity.create(line.replace("t=", "").trim());
                    if (copy != null) copy.copyTo(this);
                }
                if (line.indexOf("n=") == 0) name = line.replace("n=", "").trim();
                if (line.indexOf("id=") == 0) id = Integer.parseInt(line.replace("n=", "").trim());
                if (line.indexOf("c - ") == 0) {
                    //takes the component that already exists (because of entity.copy()) and
                    //calls the custom load on it
                    Component c = this.getComponent(line.replace("c - ", "").trim());
                    if (c != null) {
                        c.customLoad(br);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public int getWorldX() {
        Component p = getComponent("Position");
        if (p != null) {
            return ((Position)p).getWorldX();
        }
        return 0;
    }
    
    public int getWorldY() {
        Component p = getComponent("Position");
        if (p != null) {
            return ((Position)p).getWorldY();
        }
        return 0;
    }
    
    public void setWorldX(int w_x) {
        Component c = getComponent("Position");
        if (c == null) return;
        Position p = ((Position)c);
        p.setWorldX(w_x);
    }
    
    public void setWorldY(int w_y) {
        Component c = getComponent("Position");
        if (c == null) return;
        Position p = ((Position)c);
        p.setWorldY(w_y);
    }
    
    public String getType() { return type; }
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    
    public void update() {
        for (ComponentSystem s: systems) s.update();
    }
    
    public void draw(Graphics g) {
        for (ComponentSystem s: systems) s.draw(g);
    }
    
    public void addComponent(Component c) {components.remove(c);components.add(c);c.setParent(this);}
    public void removeComponent(Component c) {if (!components.contains(c) && c != null) components.remove(c);}
    public Component getComponent(String s) {
        for (Component c: components) {
            if (c.getID().equals(s)) {
                return c;
            }
        }
        return null;
    }
    
    public void addSystem(ComponentSystem c) {if (!systems.contains(c)) {systems.add(c);c.setParent(this);}}
    public void removeSystem(ComponentSystem c) {if (!systems.contains(c)) systems.remove(c);}
    public ComponentSystem getSystem(String s) {
        for (ComponentSystem c: systems) {
            if (c.getID().equals(s)) {
                return c;
            }
        }
        return null;
    }
    
    public void addFlow(Flow f) {if (!flows.contains(f)) {flows.add(f);f.setParent(this);}}
    public void removeFlow(Flow f) {if (!flows.contains(f)) flows.remove(f);}
    public Flow getFlow(String s) {
        for (Flow f: flows) {
            if (f.getID().equals(s)) {
                return f;
            }
        }
        return null;
    }
    
    public void copyTo(Entity e) {
        e.type = this.type;
        e.name = this.name;
        e.components.clear();
        for (Component c: components) {
            Component nc = Component.create(c.id);
            c.copyTo(nc);
            e.addComponent(nc);
        }
        e.systems.clear();
        for (ComponentSystem c: systems) {
            ComponentSystem nc = ComponentSystem.create(c.id);
            c.copyTo(nc);
            e.addSystem(nc);
        }
        e.flows.clear();
        for (Flow f: flows) {
            Flow nf = new Flow();
            f.copyTo(nf);
            e.addFlow(nf);
        }
    }
    
    /**
     * Compares the sector's x and y coordinates (like -2, 5) to the given coordinates,
     * for use in a sorted list of sectors. INCOMPLETE!
     * @param x Sector x coordinate to compare to.
     * @param y Sector y coordinate to compare to.
     * @return -1 if the sector is to the "left" of the given coordinates,
     * and 1 if it is to the "right". 0 if neither.
     */
    public int compareTo(int x, int y) {
        int tx = this.getWorldX();
        int ty = this.getWorldY();
        if (tx > x) return 1;
        if (tx < x) return -1;
        if (ty > y) return 1;
        if (ty < y) return -1;
        return 0;
    }
    
}