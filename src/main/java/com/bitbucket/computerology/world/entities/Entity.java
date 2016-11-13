package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.entities.components.Position;
import java.util.LinkedList;
import java.util.Random;
import org.newdawn.slick.Graphics;

public class Entity {
    
    private LinkedList<ComponentSystem> systems;
    private LinkedList<Component> components;
    private LinkedList<Flow> flows;
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
        e.id = Math.abs(new Random().nextInt() % 100000);
        return e;
    }
    
    public Entity() {
        this.systems = new LinkedList<ComponentSystem>();
        this.components = new LinkedList<Component>();
        this.flows = new LinkedList<Flow>();
        this.name = "";
        this.type = "";
        this.id = -1;
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
        Component p = getComponent("Position");
        if (p != null) {
            ((Position)p).setWorldX(w_x);
        }
    }
    
    public void setWorldY(int w_y) {
        Component p = getComponent("Position");
        if (p != null) {
            ((Position)p).setWorldY(w_y);
        }
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
            Component nc = new Component();
            c.copyTo(nc);
            e.addComponent(nc);
        }
        e.systems.clear();
        for (ComponentSystem c: systems) {
            ComponentSystem nc = new ComponentSystem();
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
    
}