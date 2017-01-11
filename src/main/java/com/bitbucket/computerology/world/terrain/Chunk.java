package com.bitbucket.computerology.world.terrain;

import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class Chunk {
    
    public static int BIOME_COUNT = 6, 
            GRASS_FIELD = 0, SAND = 1, WATER = 2, SNOW = 3, ROAD_INTERSECTION = 4, ROAD_STRAIGHT = 5;
    private int terrain, x, y, rot;
    private Sector parent;
    
    public static Color[] COLORS = {Color.green, Color.green.darker(), Color.yellow, Color.blue, Color.white};
    
    public ArrayList<Entity> entities;
    
    public Chunk(int x, int y, Sector parent) {
        this.rot = Math.abs(new Random().nextInt() % 4);
        this.terrain = -1;
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.entities = new ArrayList<Entity>();
    }
    
    /**
     * Sets the rotation index to 0 through 3 (0 being up, 1 being right).
     * @param r A number from 0-3. Sets the rotation index to the absolute value of r mod 4.
     */
    public void setRotation(int r) {
        rot = Math.abs(r % 4);
    }
    
    public int getRotation() { return rot; }
    
    public static int sizePixels() { return 32; }
    public static int onScreenSize() { return sizePixels()*Camera.getZoom(); }
    
    public boolean addEntity(Entity e) {
        if (!entities.contains(e)) return entities.add(e);
        return false;
    }
    
    public boolean removeEntity(Entity e) { return entities.remove(e); }
    
    public ArrayList<Entity> getEntities() { return entities; }
    
    /**
     * Returns a Chunk array of size 8. Traverses horizontally first, then vertically.
     * That is: <br><br>
     * 0 1 2<br>
     * 3 x 4<br>
     * 5 6 7<br>
     * @param diag Whether or not to include chunks diagonal to the parent.
     * @return A Chunk[] array. Elements can be null if no chunk was found in the slot.
     */
    public Chunk[] getAdjacentChunks(boolean diag) {
        Chunk d[] = new Chunk[]{null,null,null,null,null,null,null,null}; 
        int i = 0, s_x, s_y; Sector s;
        for (int h = -1; h != 2; h++) {
            for (int w = -1; w != 2; w++) {
                if ((w == 0 && h == 0)) continue;
                s_x = (x+w < 0 ? -1 : (x+w >= Sector.sizeChunks() ? 1 : 0));
                s_y = (y+h < 0 ? -1 : (y+h >= Sector.sizeChunks() ? 1 : 0));
                //if s_x and s_y are the parent sector's coords, then s = parent
                //else, s is the sector at (parent x + s_x, parent y + s_y)
                s = (s_x+w == parent.offsets()[0] && s_y+h == parent.offsets()[1] ? parent : 
                        parent.getWorld().getSector(parent.offsets()[0] + s_x, parent.offsets()[1] + s_y));
                //the Ith element in d is the chunk (cx, cy) in s, where (cx, cy) is the chunk coordinates relative
                //to this chunk's parent sector's origin, mod Sector.SIZE (which right now is 32)
                d[i] = (s != null ? s.getChunk((x+w+Sector.sizeChunks()) % Sector.sizeChunks(), 
                        (y+h+Sector.sizeChunks()) % Sector.sizeChunks()) : null);
                if (((w != 0 && h != 0) && !diag)) d[i] = null;
                i++;
            }
        }
        return d;
    }
    
    public Sector getSector() { return parent; }
    
    public int[] offsets() {
        return new int[]{x, y};
    }
    
    public int[] worldCoords() {
        return new int[]{parent.worldCoords()[0] + (x*sizePixels()), parent.worldCoords()[1] + (y*sizePixels())};
    }
    
    public int getTerrain() {
        return terrain;
    }
    
    public int[] onScreenCoords() {
        return World.getWorld().getOnscreenCoords(worldCoords()[0], worldCoords()[1]);
    }

    public boolean intersects(int x, int y, int w, int h) {
        return MiscMath.rectanglesIntersect(worldCoords()[0], 
                worldCoords()[1], Chunk.sizePixels(), Chunk.sizePixels(), x, y, w, h);
    }

    public void setTerrain(int biome) {
        this.terrain = (biome >= 0 && biome < Chunk.BIOME_COUNT) ? biome : -1;
    }
    
    public boolean load(BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.replace("", "").trim();
                if (line.equals("/c")) return true;
                if (line.indexOf("x=") == 0) x = Integer.parseInt(line.replace("x=", ""));
                if (line.indexOf("y=") == 0) y = Integer.parseInt(line.replace("y=", ""));
                if (line.indexOf("t=") == 0) terrain = Integer.parseInt(line.replace("t=", ""));
            }
        } catch (IOException ex) {
            Logger.getLogger(Sector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void save(BufferedWriter bw) {
        try {
            bw.write("c\n");
            bw.write("x="+x+"\n");
            bw.write("y="+y+"\n");
            bw.write("t="+getTerrain()+"\n");
            bw.write("/c\n");
        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void draw(boolean corners, Graphics g) {
        
        if (getTerrain() < 0 || getTerrain() >= Chunk.BIOME_COUNT) return;
        Image img = Assets.getTerrainSprite(corners);
        
        int src_x = Chunk.onScreenSize()*getTerrain();
        
        int r = (rot*Chunk.onScreenSize());
        int rx = onScreenCoords()[0];
        int ry = onScreenCoords()[1];
        
        if (corners) {
            int draw[] = {-1, -1, -1, -1};
            Chunk[] adj = getAdjacentChunks(false);
            if (adj[1] != null && adj[3] != null) {
                if (adj[1].getTerrain() == adj[3].getTerrain()) draw[0] = adj[1].getTerrain(); 
            }
            if (adj[1] != null && adj[4] != null) {
                if (adj[1].getTerrain() == adj[4].getTerrain()) draw[1] = adj[1].getTerrain(); 
            }
            if (adj[4] != null && adj[6] != null) {
                if (adj[4].getTerrain() == adj[6].getTerrain()) draw[2] = adj[4].getTerrain(); 
            }
            if (adj[6] != null && adj[3] != null) {
                if (adj[6].getTerrain() == adj[3].getTerrain()) draw[3] = adj[6].getTerrain(); 
            }
            for (int i = 0; i < 4; i++) {
                if (draw[i] > -1 && draw[i] < Chunk.BIOME_COUNT && draw[i] != getTerrain()) {
                    src_x = Chunk.onScreenSize()*draw[i];
                    r = (i*Chunk.onScreenSize());
                    img.drawEmbedded(rx,ry,rx+Chunk.onScreenSize(),ry+Chunk.onScreenSize(),
                        src_x, r, src_x+Chunk.onScreenSize(), r+Chunk.onScreenSize());
                }
            }
        } else {
            img.drawEmbedded(rx,ry,rx+Chunk.onScreenSize(),ry+Chunk.onScreenSize(),
                    src_x, r, src_x+Chunk.onScreenSize(), r+Chunk.onScreenSize());
        }
        
    }
    
}
