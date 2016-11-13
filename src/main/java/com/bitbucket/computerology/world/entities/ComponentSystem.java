package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.entities.systems.*;
import org.newdawn.slick.Graphics;

public class ComponentSystem {
    
    private Entity parent;
    String id;
    
    public ComponentSystem() {}
    
    public final void setParent(Entity e) {parent = e;}
    public final Entity getParent() {return parent;}
    public final void setID(String id) {this.id = id;}
    public final String getID() {return id;}
    
    public void update() {}
    public void draw(Graphics g) {}
    
    public static ComponentSystem create(String s) {
        ComponentSystem c = null;
        if ("Render".equals(s)) c = new Render();
        if ("Movement".equals(s)) c = new Movement();

        if (c != null) { 
            c.setID(s); 
        } else { System.err.println("Failed to create component system "+s+"!"); }
        return c;
    }
    
    public void copyTo(ComponentSystem c) {
        c.id = this.id;
        c.parent = this.parent;
    }
    
}
