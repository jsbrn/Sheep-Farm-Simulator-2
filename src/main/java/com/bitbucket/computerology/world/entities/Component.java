package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.entities.components.Position;
import com.bitbucket.computerology.world.entities.components.Texture;

public class Component {
    
    String id, params;
    Entity parent;
    
    public Component() {setID("");params = "";}
    
    public final void setID(String id) {this.id = id;}
    public final String getID() {return id;}
    public final void setParent(Entity e) {parent = e;}
    public final Entity getParent() {return parent;}
    
    /**
     * Takes param string and parses the values, assigning each to
     * an appropriate variable. Can be overridden.
     */
    public void initParams() {
        
    }
    
    public static Component create(String s) {
        Component c = null;
        if ("texture".equals(s)) c = new Texture();
        if ("position".equals(s)) c = new Position();
        if (c != null) c.setID(s);
        return c;
    }
    
}
