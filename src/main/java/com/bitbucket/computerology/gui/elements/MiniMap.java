package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.world.Chunk;
import com.bitbucket.computerology.world.Sector;
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
            int x, y;
            for (x = -getWidth()/(zoom); x < getWidth()/(zoom); x+=1) {
                for (y = -getHeight()/(zoom); y < getHeight()/(zoom); y+=1) {
                    int[] osc = {Display.getWidth()/2 + (x*Chunk.size()), Display.getHeight()/2 + (y*Chunk.size())};
                    int[] sc = World.getWorld().getSectorCoords(osc[0], osc[1]);
                    int[] cc = World.getWorld().getChunkCoords(osc[0], osc[1]);
                    Sector s = World.getWorld().getSector(sc[0], sc[1]);
                    Chunk c = (s != null ? s.getChunk(cc[0], cc[1]) : null);
                    Color color = Color.black;
                    if (c != null) {
                        if (c.getTerrain() > -1) {
                            color = Chunk.COLORS[c.getTerrain()];
                        }
                    }
                    g.setColor(color);
                    g.fillRect(zoom*(x+(getWidth()/(zoom))), zoom*(y+(getHeight()/(zoom))), zoom, zoom);
                }
            }
        } catch (SlickException ex) {
            Logger.getLogger(MiniMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void draw(Graphics g) {
        if (buffer != null) g.drawImage(buffer, getOnscreenX(), getOnscreenY());
    }

}
