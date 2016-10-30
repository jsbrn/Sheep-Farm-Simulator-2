package com.bitbucket.computerology.world.entities.systems;

import com.bitbucket.computerology.world.entities.Entity;
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
        
        return null;
    }
    
}
