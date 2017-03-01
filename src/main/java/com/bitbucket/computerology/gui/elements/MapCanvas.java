package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.sun.corba.se.impl.orbutil.graph.Graph;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MapCanvas extends GUIElement {

    Image map;
    int zoom = 4;
    double refresh_timer = 1;

    public MapCanvas() {

    }

    public void init(int size) {
        try {
            this.map = new Image(size, size);
        } catch (SlickException ex) {
            Logger.getLogger(MapCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
    }

}
