package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.world.World;
import com.sun.corba.se.impl.orbutil.graph.Graph;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapCanvas extends GUIElement {

    private Image map;
    private int zoom = 4;

    private ArrayList<Object[]> queue; //x, y, color

    public MapCanvas() {
        this.queue = new ArrayList<Object[]>();
    }

    public void set(int x, int y, Color c) {

    }

    private void init(int size) {
        try {
            this.map = new Image(size, size);
        } catch (SlickException ex) {
            Logger.getLogger(MapCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void draw(Graphics g) {

        if (map == null) {
            //if map is null and world is not, initialize the map to the world's size
            if (World.getWorld() != null) {
                init(World.getWorld().size());
            }
        } else {
            if (World.getWorld() == null) {
                try {
                    map.destroy();
                } catch (SlickException e) {
                    e.printStackTrace();
                }
                map = null;
            } else {

            }
        }

        super.draw(g);
    }

}
