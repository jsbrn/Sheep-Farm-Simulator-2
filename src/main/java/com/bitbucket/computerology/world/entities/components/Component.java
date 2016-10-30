package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.components.types.Position;
import com.bitbucket.computerology.world.entities.components.types.Texture;

public class Component {
    
    String id;
    Entity parent;
    
    public Component() {setID("");}
    
    public final void setID(String id) {this.id = id;}
    public final String getID() {return id;}
    public final void setParent(Entity e) {parent = e;}
    public final Entity getParent() {return parent;}
    
    public static Component create(String s) {
        Component c = null;
        if ("texture".equals(s)) c = new Texture();
        if ("position".equals(s)) c = new Position();
        if (c != null) c.setID(s);
        return c;
    }
    
}
