package com.bitbucket.computerology.world;

import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.world.entities.Entity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
public class Chunk {
    
    public static int BIOME_COUNT = 5, GRASS_FIELD = 0, GRASS_FOREST = 1, SAND = 2, WATER = 3, SNOW = 4;
    public static int SIZE_PIXELS = 64;
    private int terrain, x, y, rot;
    private Sector parent;
    
    ArrayList<Entity> entities;
    
    public static Color[] COLORS = {Color.green, Color.green.darker(), Color.yellow, Color.blue, Color.white};
    
    public Chunk(int x, int y, Sector parent) {
        this.rot = Math.abs(new Random().nextInt() % 4);
        this.terrain = -1;
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.entities = new ArrayList<Entity>();
    }
    
    public boolean addEntity(Entity e) {
        if (!entities.contains(e)) { entities.add(e); return true; }
        return false;
    }
    
    /**
     * Returns a Chunk array of size 8.
     * @param diag Whether or not to include chunks diagonal to the parent.
     * @return A Chunk[] array. Elements can be null if no chunk was found in the slot.
     */
    public Chunk[] getAdjacentChunks(boolean diag) {
        Chunk d[] = new Chunk[]{null,null,null,null,null,null,null,null}; 
        int i = 0, s_x, s_y; Sector s;
        for (int h = -1; h != 2; h++) {
            for (int w = -1; w != 2; w++) {
                if ((w == 0 && h == 0)) continue;
                s_x = (x+w < 0 ? -1 : (x+w >= Sector.SIZE_CHUNKS ? 1 : 0));
                s_y = (y+h < 0 ? -1 : (y+h >= Sector.SIZE_CHUNKS ? 1 : 0));
                //if s_x and s_y are the parent sector's coords, then s = parent
                //else, s is the sector at (parent x + s_x, parent y + s_y)
                s = (s_x+w == parent.offsets()[0] && s_y+h == parent.offsets()[1] ? parent : 
                        parent.getWorld().getSector(parent.offsets()[0] + s_x, parent.offsets()[1] + s_y));
                //the Ith element in d is the chunk (cx, cy) in s, where (cx, cy) is the chunk coordinates relative
                //to this chunk's parent sector's origin, mod Sector.SIZE (which right now is 32)
                d[i] = (s != null ? s.getChunk((x+w+Sector.SIZE_CHUNKS) % Sector.SIZE_CHUNKS, 
                        (y+h+Sector.SIZE_CHUNKS) % Sector.SIZE_CHUNKS) : null);
                if (((w != 0 && h != 0) && !diag)) d[i] = null;
                i++;
            }
        }
        return d;
    }
    
    /**
     * Gives the sector a random biome that takes into consideration the sectors
     * around it. Desert will never be adjacent to tundra.
     */
    void randomizeTerrain(boolean allow_sector_biome) {
        terrain = randomValidTerrain((allow_sector_biome ? -1 : parent.getBiome()));
    }
    
    public Sector getSector() { return parent; }
    
    public boolean isTerrainAllowed(int b) {
        if (b < 0 || b >= Chunk.BIOME_COUNT) return false;
        Chunk adj[] = getAdjacentChunks(true);
        ArrayList<Chunk> non_null = new ArrayList<Chunk>();
        for (Chunk s: adj) {if (s != null) { non_null.add(s); }}
        if (non_null.isEmpty()) return true;
        for (Chunk s: non_null) {
            if (b == Chunk.SNOW && parent.getBiome() != Chunk.SNOW) return false;
        }
        return true;
    }
    
    public int[] offsets() {
        return new int[]{x, y};
    }
    
    public int[] worldCoords() {
        return new int[]{parent.worldCoords()[0] + (x*SIZE_PIXELS), parent.worldCoords()[1] + (y*SIZE_PIXELS)};
    }
    
    public int getTerrain() {
        return terrain;
    }
    
    public int[] onScreenCoords() {
        double shift_x = (Camera.getX())-(Display.getWidth()/2), shift_y = (Camera.getY())-(Display.getHeight()/2);
        return new int[]{(int)((worldCoords()[0])-shift_x), (int)((worldCoords()[1])-shift_y)};
    }
    
    /**
     * Gets a random valid biome for this particular sector.
     * @param exception Will not return this biome in any case.
     * @return An Integer representing the randomly chosen biome.
     */
    public int randomValidTerrain(int exception) {
        int b = exception;
        while (!isTerrainAllowed(b) || b == exception) {
            Chunk adj[] = getAdjacentChunks(true);
            ArrayList<Chunk> non_null = new ArrayList<Chunk>();
            for (Chunk s: adj) {if (s != null) { non_null.add(s); }}
            
            if (non_null.isEmpty() || Math.abs(parent.getWorld().rng().nextInt() % 100) <= 30) 
                b = Math.abs(parent.getWorld().rng().nextInt() % Chunk.BIOME_COUNT);
            else
                b = non_null.get(Math.abs(parent.getWorld().rng().nextInt() % non_null.size())).getTerrain();
            
        }
        return b;
    }
    
    public boolean isOrphan(boolean stay_in_sector) {
        int count = 0;
        Chunk[] adj = getAdjacentChunks(true);
        for (int a = 0; a != adj.length; a++) { if (adj[a] == null) continue; 
            if (adj[a].getTerrain() == getTerrain()) { 
                if (!stay_in_sector || ((stay_in_sector && parent.equals(adj[a].getSector())))) {
                    count++; 
                }
            }
        }
        return count <= 3;
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
            bw.write("t="+terrain+"\n");
            bw.write("/c\n");
        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void draw(Graphics g) {
        if (terrain < 0 || terrain >= Chunk.BIOME_COUNT) return;
        Assets.CHUNK_TERRAIN.drawEmbedded(onScreenCoords()[0]-((Assets.CHUNK_TERRAIN.getHeight()-SIZE_PIXELS)/2), 
                onScreenCoords()[1]-((Assets.CHUNK_TERRAIN.getHeight()-SIZE_PIXELS)/2), 
                onScreenCoords()[0]-((Assets.CHUNK_TERRAIN.getHeight()-SIZE_PIXELS)/2)+Assets.CHUNK_TERRAIN.getHeight(), 
                onScreenCoords()[1]-((Assets.CHUNK_TERRAIN.getHeight()-SIZE_PIXELS)/2)+Assets.CHUNK_TERRAIN.getHeight(),
                Assets.CHUNK_TERRAIN.getHeight()*terrain, 0, (Assets.CHUNK_TERRAIN.getHeight()*terrain)+Assets.CHUNK_TERRAIN.getHeight(), Assets.CHUNK_TERRAIN.getHeight());
        
    }
    
}
