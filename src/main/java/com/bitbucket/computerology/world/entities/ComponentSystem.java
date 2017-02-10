package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.entities.systems.Movement;
import com.bitbucket.computerology.world.entities.systems.Render;
import org.newdawn.slick.Graphics;

public class ComponentSystem {

    String id;
    private Entity parent;

    public ComponentSystem() {
    }

    public static ComponentSystem create(String s) {
        ComponentSystem c = null;
        if ("Render".equals(s)) c = new Render();
        if ("Movement".equals(s)) c = new Movement();

        if (c != null) {
            c.setID(s);
        } else {
            System.err.println("Failed to create component system " + s + "!");
        }
        return c;
    }

    public final Entity getParent() {
        return parent;
    }

    public final void setParent(Entity e) {
        parent = e;
    }

    public final String getID() {
        return id;
    }

    public final void setID(String id) {
        this.id = id;
    }

    public void update() {
    }

    public void draw(Graphics g) {
    }

    public void copyTo(ComponentSystem c) {
        c.id = this.id;
        c.parent = this.parent;
    }

}
