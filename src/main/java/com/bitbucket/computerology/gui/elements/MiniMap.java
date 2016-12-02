package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;
import com.bitbucket.computerology.world.World;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class MiniMap extends GUIElement {
    
    Image buffer;
    int zoom = 4;
    double refresh_timer = 1;
    
    public MiniMap() {
        
    }

    /**
     * NEEDS FINISHING: CURRENTLY BROKEN.
     */
    public void refresh() {
        if (World.getWorld() == null) return;
        if (buffer != null) return;
        try {
            buffer = new Image(getWidth(), getHeight());
            Graphics g = buffer.getGraphics();
            //TODO: implement
        } catch (SlickException ex) {
            Logger.getLogger(MiniMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void draw(Graphics g) {
        if (buffer != null) g.drawImage(buffer, getOnscreenX(), getOnscreenY());
    }

}
