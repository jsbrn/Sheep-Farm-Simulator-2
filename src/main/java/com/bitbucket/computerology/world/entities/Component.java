package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.misc.MiscString;
import com.bitbucket.computerology.world.entities.components.Hitbox;
import com.bitbucket.computerology.world.entities.components.Position;
import com.bitbucket.computerology.world.entities.components.Texture;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Component {
    
    String id, params;
    Entity parent;
    
    public Component() {setID("");params = "";}
    
    public final void setID(String id) {this.id = id;}
    public final String getID() {return id;}
    public final void setParent(Entity e) {parent = e;}
    public final Entity getParent() {return parent;}
    
    public static Component create(String s) {
        Component c = null;
        if ("Texture".equals(s)) c = new Texture();
        if ("Position".equals(s)) c = new Position();
        if ("Hitbox".equals(s)) c = new Hitbox();
        if (c != null) { 
            c.setID(s); 
        } else { System.err.println("Failed to create component "+s+"!"); }
        return c;
    }
    
    /**
     * Takes the param string and splits it into a String array, passing along
     * the array to the other initParams function.
     */
    public final void init() {
        initParams(MiscString.parseString(params));
    }
    
    /**
     * Parses the values in the given string arrays, assigning each to
     * an appropriate variable. Can be overridden to initialize other things.
     */
    public void initParams(ArrayList<String> p) {}
    
    /**
     * Copies the component data to the specified component c.
     * Will copy the param string as well as the id and the parent entity.
     * Override to copy other, custom variables, but call super for the aforementioned
     * three.
     * @param c Component specified.
     */
    public void copyTo(Component c) {
        c.id = this.id;
        c.params = this.params;
        c.parent = this.parent;
    }
    
    /**
     * save() is final, all components saved must call it exactly.
     * But save() calls customSave(), so you have the opportunity to 
     * have custom values saved per component.
     * @param bw 
     */
    public void customSave(BufferedWriter bw) {}
    
    public final void save(BufferedWriter bw) {
        try {
            bw.write("c\n");
            bw.write("id="+id+"\n");
            customSave(bw);
            bw.write("/c\n");
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean customLoad(BufferedReader br) { return true; }
    
}
