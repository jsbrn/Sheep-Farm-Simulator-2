package com.bitbucket.computerology.world.entities;

import java.util.LinkedList;
import java.util.Random;
import org.newdawn.slick.Graphics;

public class Entity {
    
    LinkedList<ComponentSystem> systems;
    LinkedList<Component> components;
    LinkedList<Flow> flows;
    String name, type, texture;
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
    public static Entity create(int x, int y, String type) {
        Entity e = new Entity();
        e.name = "";
        e.type = type;
        e.id = Math.abs(new Random().nextInt() % 100000);
        e.systems = new LinkedList<ComponentSystem>();
        e.components = new LinkedList<Component>();
        return e;
    }
    
    public Entity() {}
    
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
    
}