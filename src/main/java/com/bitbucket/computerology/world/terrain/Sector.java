package com.bitbucket.computerology.world.terrain;

import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.terrain.generators.Generator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sector {
    
    private Chunk[][] chunks;
    private int x, y;
    private boolean filled = false;
    
    private double[] biome_percentages;
    
    ArrayList<Entity> entities;
    
    private Generator generator;
    
    int chunk_update_index = 0;
    
    public Sector(int x, int y) {
        this.x = x; this.y = y;
        this.chunks = new Chunk[sizeChunks()][sizeChunks()];
        this.entities = new ArrayList<Entity>();
        this.entities.ensureCapacity(Sector.sizeChunks()*Sector.sizeChunks());
        this.biome_percentages = new double[Chunk.BIOME_COUNT];
        this.generator = null;
    }
    
    public void addEntity(Entity e) { if (!entities.contains(e)) entities.add(e); }
    public void removeEntity(Entity e) { entities.remove(e); }
    public ArrayList<Entity> getEntities() { return entities; }
    
    /**
     * Compares the sector's x and y coordinates (like -2, 5) to the given coordinates,
     * for use in a sorted list of sectors.
     * @param x Sector x coordinate to compare to.
     * @param y Sector y coordinate to compare to.
     * @return -1 if the sector is to the "left" of the given coordinates,
     * and 1 if it is to the "right". 0 if neither.
     */
    public int compareTo(int x, int y) {
        if (this.x > x) return 1;
        if (this.x < x) return -1;
        if (this.y > y) return 1;
        if (this.y < y) return -1;
        return 0;
    }
    
    public boolean isTownSector() { return World.getWorld().getTown(x, y) != null; }
    
    public int[] getSectorCoords() {
        return new int[]{x, y};
    }
    
    public int[] getWorldCoords() {
        return new int[]{x*sizePixels(), y*sizePixels()};
    }
    
    public static int sizeChunks() { return 64; }
    public static int sizePixels() { return Chunk.sizePixels()*sizeChunks(); }
    public static int onScreenSize() { return sizePixels()*Camera.getZoom(); }
    
    public int[] onScreenCoords() {
        return World.getWorld().getOnscreenCoords(getWorldCoords()[0], getWorldCoords()[1]);
    }
    
    /**
     * Get sectors adjacent to this sector. Does not consider any sector to be
     * adjacent to itself.
     * 
     * <br><br>
     * 0 1 2<br>
     * 3 x 4<br>
     * 5 6 7<br>
     * 
     * @return A Sector[] with 8 elements.
     */
    public Sector[] getAdjacentSectors() {
        World world = World.getWorld();
        return new Sector[]{
            world.getSector(x-1, y-1),
            world.getSector(x, y-1),
            world.getSector(x+1, y-1),
            world.getSector(x-1, y),
            world.getSector(x+1, y),
            world.getSector(x-1, y+1),
            world.getSector(x, y+1),
            world.getSector(x+1, y+1)
        };
    }
    
    public void markFilled() {
        
    }
    
    public boolean filled() {
        return filled;
    }
    
    public void importBiomes(short[][] map) {
        if (filled) return;
        this.chunks = new Chunk[sizeChunks()][sizeChunks()];
        int[] mc = World.getWorld().getMapCoords(x, y, 0, 0);
        for (short i = 0; i < sizeChunks(); i++) {
            for (short j = 0; j < sizeChunks(); j++) {
                int terrain = map[mc[0]+i][mc[1]+j];
                chunks[i][j] = new Chunk(i, j, this);
                chunks[i][j].setBiome(terrain);
            }
        }
    }
    
    public void importForest(boolean[][] map) {
        if (filled) return;
        World world = World.getWorld();
        int[] mc = World.getWorld().getMapCoords(x, y, 0, 0);
        for (int i = 0; i < sizeChunks(); i++) {
            for (int j = 0; j < sizeChunks(); j++) {
                boolean spawn = map[mc[0]+i][mc[1]+j];
                if (spawn) {
                    int terrain = getChunk(i, j).getTerrain();
                    if (terrain != Chunk.GRASS_FIELD && terrain != Chunk.SNOW) continue;
                    Entity tree = Entity.create("Tree");
                    int wc[] = world.getWorldCoordsFromMap(mc[0]+i, mc[1]+j);
                    tree.setWorldX(wc[0]+(Chunk.sizePixels()/2)+(world.rng().nextInt() % 8));
                    tree.setWorldY(wc[1]+(Chunk.sizePixels()/2)+(+(world.rng().nextInt() % 8)));
                    world.addEntity(tree);
                    
                }
            }
        }
    }
    
    public void importRoads(boolean[][] map) {
        if (filled) return;
        int[] mc = World.getWorld().getMapCoords(x, y, 0, 0);
        for (int i = 0; i < sizeChunks(); i++) {
            for (int j = 0; j < sizeChunks(); j++) {
                boolean spawn = map[mc[0]+i][mc[1]+j];
                if (spawn) {
                    Chunk c = getChunk(i, j);
                    c.setTerrain(
                            (i == Sector.sizeChunks()-1 && j == 0) ||
                            i == j || (i == 0 && j == Sector.sizeChunks()-1) ? Chunk.ROAD_INTERSECTION : Chunk.ROAD_STRAIGHT);
                    if (c.getTerrain() == Chunk.ROAD_STRAIGHT) {
                        if (i == 0) c.setRotation(2); if (i == 1) c.setRotation(0);
                        if (j == 0) c.setRotation(3); if (j == 1) c.setRotation(1);
                    }
                }
            }
        }
    }
    
    /**
     * Get the chunk at the specified offset from the origin of the parent sector.
     * @return A Chunk instance or null if not found.
     */
    public Chunk getChunk(int x, int y) {
        if (x > -1 && x < sizeChunks() && y > -1 && y < sizeChunks()) {
            return chunks[x][y];
        }
        return null;
    }
    
    public final boolean load(BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.equals("/s")) return true;
                
                if (line.equals("c")) {
                    Chunk c = new Chunk((short)0, (short)0, this);
                    if (c.load(br)) chunks[c.offsets()[0]][c.offsets()[1]] = c;
                }
                if (line.indexOf("x=") == 0) x = Integer.parseInt(line.replace("x=", ""));
                if (line.indexOf("y=") == 0) y = Integer.parseInt(line.replace("y=", ""));
            }
        } catch (IOException ex) {
            Logger.getLogger(Sector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public final void save(BufferedWriter bw) {
        try {
            bw.write("s\n");
                bw.write("x="+x+"\n");
                bw.write("y="+y+"\n");
                for (int h = 0; h != chunks.length; h++) {
                    for (int w = 0; w != chunks[h].length; w++) {
                        chunks[w][h].save(bw);
                    }
                }
            bw.write("/s\n");
        } catch (IOException ex) {
            Logger.getLogger(Sector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Generator generator() { return generator; }

}
