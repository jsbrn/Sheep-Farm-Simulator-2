package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.entities.components.Hitbox;
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
        if ("Texture".equals(s)) c = new Texture();
        if ("Position".equals(s)) c = new Position();
        if ("Hitbox".equals(s)) c = new Hitbox();
        if (c != null) c.setID(s); else System.err.println("Failed to create component "+s+"!");
        return c;
    }
    
    /**
     * Copies the component data to the specified component c.
     * Will copy the param string as well as the id and the parent entity.
     * Override to copy other, custom variables, but call super for the aforementioned
     * three.
     * @param c Component specified.
     */
    public final void copyTo(Component c) {
        c.id = this.id;
        c.params = this.params;
        c.parent = this.parent;
    }
    
}
