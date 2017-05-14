package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.misc.MiscString;
import com.bitbucket.computerology.world.entities.components.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Component {

    private String id, params;
    private Entity parent;

    public Component() {
        setID("");
        params = "";
    }

    public static Component create(String s) {
        Component c = null;
        if ("Texture".equals(s)) c = new Texture();
        if ("Position".equals(s)) c = new Position();
        if ("Hitbox".equals(s)) c = new Hitbox();
        if ("Forces".equals(s)) c = new Forces();
        if ("TownBuilding".equals(s)) c = new TownBuilding();
        if (c != null) {
            c.setID(s);
        } else {
            System.err.println("Failed to create component " + s + "!");
        }
        return c;
    }

    public final String getID() {
        return id;
    }

    public final void setID(String id) {
        this.id = id;
    }

    public final Entity getParent() {
        return parent;
    }

    public final void setParent(Entity e) {
        parent = e;
    }

    /**
     * Takes the param string and splits it into a String array, passing along
     * the array to the other initParams function.
     */
    public final void init() {
        initParams(MiscString.parseString(params));
    }

    /**
     * Parses the values from the given string arrays, assigning each to
     * an appropriate variable. Classes that extend Component can override to add functionality.
     */
    public void initParams(ArrayList<String> p) {
    }

    /**
     * Copies the component data to the specified component c.
     * Will copy the param string as well as the id and the parent entity.
     * Override to copy other, custom variables, but call super for the aforementioned
     * three.
     *
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
     *
     * @param bw
     */
    public void customSave(BufferedWriter bw) {
    }

    /**
     * Just like save(), load() is final. It calls customLoad and passes the current line
     * as a parametre. Custom load can be overridden per component.
     *
     * @param line The line to interpret.
     */
    public void customLoad(String line) {
    }

    public final void save(BufferedWriter bw) {
        try {
            bw.write("c - " + id + "\n");
            bw.write("id=" + id + "\n");
            customSave(bw);
            bw.write("/c\n");
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
                if (line.equals("/c")) return true;
                customLoad(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(Position.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void addParameter(String s) { this.params += s+"\n"; }

}
