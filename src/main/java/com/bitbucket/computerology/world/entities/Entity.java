package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.entities.components.Forces;
import com.bitbucket.computerology.world.entities.components.Hitbox;
import com.bitbucket.computerology.world.entities.components.Position;
import com.bitbucket.computerology.world.entities.components.Texture;
import com.bitbucket.computerology.world.entities.systems.Movement;
import com.bitbucket.computerology.world.terrain.Chunk;
import org.newdawn.slick.Graphics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Entity {

    String name, type;
    int id;
    //references to commonly used components (to avoid repeated searches)
    Hitbox hitbox;
    Position position;
    Texture texture;
    Forces forces;
    Movement movement;
    private ArrayList<ComponentSystem> systems;
    private ArrayList<Component> components;
    private ArrayList<Flow> flows;


    public Entity() {
        this.systems = new ArrayList<ComponentSystem>();
        this.components = new ArrayList<Component>();
        this.flows = new ArrayList<Flow>();
        this.name = "";
        this.type = "";
        this.id = -1;
    }

    /**
     * Creates a new entity.
     * @return The created entity instance.
     */
    public static Entity create(String type) {
        Entity e = new Entity(), clone = EntityList.getEntity(type);
        if (clone == null) {
            System.err.println("Could not find entity of type " + type + " in ENTITY_LIST");
            return null;
        }
        clone.copyTo(e);
        e.type = type;
        e.id = Math.abs(new Random().nextInt() % 1000000) + 1000;
        return e;
    }

    public static int maxSizeChunks() {
        return 16;
    }

    public static int maxSizePixels() {
        return maxSizeChunks() * Chunk.sizePixels();
    }

    public final void update() {
        //update all systems but Movement, which is separate
        for (ComponentSystem s : systems)
            if (!s.equals(movement)) s.update();
    }

    public final void draw(Graphics g) {
        for (ComponentSystem s : systems) s.draw(g);
    }

    public final void move() {
        if (movement != null) movement.update();
    }

    public final boolean intersects(Entity e) {
        return hitbox == null ? false : hitbox.intersects(e);
    }

    public final boolean intersects(double x, double y, int width, int height) {
        return hitbox == null ? false : hitbox.intersects(x, y, width, height);
    }

    public final boolean intersects(double x, double y) {
        return hitbox == null ? false : hitbox.intersects(x, y);
    }

    /**
     * Does this entity have logic that needs to be updated?
     *
     * @return A boolean.
     */
    public boolean updates() {
        return !flows.isEmpty();
    }

    /**
     * Does this entity have the Movement system component?
     *
     * @return A boolean.
     */
    public boolean moves() {
        return getSystem("Movement") != null;
    }

    /**
     * Does this entity render a texture or other visual effect?
     *
     * @return A boolean.
     */
    public boolean renders() {
        return getSystem("Render") != null;
    }

    public final int getWorldX() {
        return position == null ? 0 : (int) position.getWorldX();
    }

    public final void setWorldX(double wx) {
        if (position == null) return;
        position.setWorldX(wx);
    }

    public final int getWorldY() {
        return position == null ? 0 : (int) position.getWorldY();
    }

    public final void setWorldY(double wy) {
        if (position == null) return;
        position.setWorldY(wy);
    }

    public final void addWorldX(double wx) {
        if (position == null) return;
        position.addWorldX(wx);
    }

    public final void addWorldY(double wy) {
        if (position == null) return;
        position.addWorldY(wy);
    }

    public final int getRotation() {
        return position == null ? 0 : (int) position.getRotation();
    }

    public final void setRotation(int wx) {
        if (position == null) return;
        position.setRotation(wx);
    }

    public final Force getForce(String name) {
        return forces == null ? null : forces.getForce(name);
    }

    public final void addForce(Force f) {
        if (forces == null) return;
        forces.addForce(f);
    }

    public final void removeForce(String f) {
        if (forces == null) return;
        forces.removeForce(f);
    }

    public final int getWidth() {
        return hitbox == null ? 0 : hitbox.getWidth();
    }

    public final int getHeight() {
        return hitbox == null ? 0 : hitbox.getHeight();
    }

    public final String getType() {
        return type;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String n) {
        name = n;
    }

    public final void addComponent(Component c) {
        if (components.contains(c)) return;
        components.add(c);
        c.setParent(this);
        if (c.getID().equals("Position")) position = ((Position) c);
        if (c.getID().equals("Hitbox")) hitbox = ((Hitbox) c);
        if (c.getID().equals("Texture")) texture = ((Texture) c);
        if (c.getID().equals("Forces")) forces = ((Forces) c);
    }

    public final void removeComponent(Component c) {
        if (!components.contains(c) && c != null) components.remove(c);
    }

    public final Component getComponent(String s) {
        for (Component c : components) {
            if (c.getID().equals(s)) {
                return c;
            }
        }
        return null;
    }

    public final void addSystem(ComponentSystem c) {
        if (systems.contains(c)) return;
        systems.add(c);
        c.setParent(this);
        if (c.getID().equals("Movement")) movement = ((Movement) c);
    }

    public final void removeSystem(ComponentSystem c) {
        if (!systems.contains(c)) systems.remove(c);
    }

    public final ComponentSystem getSystem(String s) {
        for (ComponentSystem c : systems) {
            if (c.getID().equals(s)) {
                return c;
            }
        }
        return null;
    }

    public final void addFlow(Flow f) {
        if (!flows.contains(f)) {
            flows.add(f);
            f.setParent(this);
        }
    }

    public final void removeFlow(Flow f) {
        if (!flows.contains(f)) flows.remove(f);
    }

    public final Flow getFlow(String s) {
        for (Flow f : flows) {
            if (f.getID().equals(s)) {
                return f;
            }
        }
        return null;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public Position getPosition() {
        return position;
    }

    public Texture getTexture() {
        return texture;
    }

    public Forces getForces() {
        return forces;
    }

    public final void copyTo(Entity e) {
        e.type = this.type;
        e.name = this.name;
        e.components.clear();
        for (Component c : components) {
            Component nc = Component.create(c.id);
            c.copyTo(nc);
            e.addComponent(nc);
        }
        e.systems.clear();
        for (ComponentSystem c : systems) {
            ComponentSystem nc = ComponentSystem.create(c.id);
            c.copyTo(nc);
            e.addSystem(nc);
        }
        e.flows.clear();
        for (Flow f : flows) {
            Flow nf = new Flow();
            f.copyTo(nf);
            e.addFlow(nf);
        }
    }

    public final void save(BufferedWriter bw) {
        try {
            bw.write("e\n");
            bw.write("t=" + type + "\n");
            bw.write("n=" + name + "\n");
            bw.write("id=" + id + "\n");
            for (Component c : components) c.save(bw);
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
                line = line.trim();
                if (line.equals("/e")) return true;
                if (line.indexOf("t=") == 0) {
                    Entity copy = Entity.create(line.replace("t=", "").trim());
                    if (copy != null) copy.copyTo(this);
                }
                if (line.indexOf("n=") == 0) name = line.replace("n=", "").trim();
                if (line.indexOf("id=") == 0) id = Integer.parseInt(line.replace("id=", "").trim());
                if (line.indexOf("c - ") == 0) {
                    //takes the component that already exists (because of entity.copy()) and
                    //calls the custom load on it
                    Component c = this.getComponent(line.replace("c - ", "").trim());
                    if (c != null) {
                        if (c.load(br) == false) return false;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Overrides Object.toString.
     *
     * @return Returns a string of the following format: type+(#id)
     */
    @Override
    public String toString() {
        return type + " (#" + id + ")";
    }

}