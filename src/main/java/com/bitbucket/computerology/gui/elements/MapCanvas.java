package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

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

    }

}
