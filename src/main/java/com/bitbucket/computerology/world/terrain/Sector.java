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
    
    private World parent;
    private Chunk[][] chunks;
    private int x, y;
    private boolean town_sector, imported_terrain = false, imported_forest = false;
    
    private double[] biome_percentages;
    
    ArrayList<Entity> entities;
    
    private Generator generator;
    
    int chunk_update_index = 0;
    
    public Sector(int x, int y, World parent) {
        this.x = x; this.y = y;
        this.parent = parent;
        this.chunks = new Chunk[sizeChunks()][sizeChunks()];
        this.town_sector = Math.abs(parent.rng().nextInt() % 50) == 0;
        this.entities = new ArrayList<Entity>();
        this.entities.ensureCapacity(1000);
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
    
    public boolean hasTown() {
        return World.getWorld().getTown(this) != null;
    }
    
    public boolean isTownSector() { return town_sector; }
    
    public int[] offsets() {
        return new int[]{x, y};
    }
    
    public int[] worldCoords() {
        return new int[]{x*sizePixels(), y*sizePixels()};
    }
    
    public static int sizeChunks() { return 32; }
    public static int sizePixels() { return Chunk.sizePixels()*sizeChunks(); }
    public static int onScreenSize() { return sizePixels()*Camera.getZoom(); }
    
    public int[] onScreenCoords() {
        return World.getWorld().getOnscreenCoords(worldCoords()[0], worldCoords()[1]);
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
        return new Sector[]{
            parent.getSector(x-1, y-1),
            parent.getSector(x, y-1),
            parent.getSector(x+1, y-1),
            parent.getSector(x-1, y),
            parent.getSector(x+1, y),
            parent.getSector(x-1, y+1),
            parent.getSector(x, y+1),
            parent.getSector(x+1, y+1)
        };
    }
    
    public void importTerrain(int[][] map) {
        System.out.println("Importing terrain map: "+map.length+"x"+map[0].length);
        if (imported_terrain) return;
        this.chunks = new Chunk[sizeChunks()][sizeChunks()];
        int[] mc = parent.getMapCoords(x, y, 0, 0);
        System.out.println("Sector "+x+", "+y+" has map coords "+mc[0]+", "+mc[1]);
        for (int i = 0; i < sizeChunks(); i++) {
            for (int j = 0; j < sizeChunks(); j++) {
                System.out.print("terrain_map("+(mc[0]+i)+", "+(mc[1]+j)+") = ");
                int terrain = map[mc[0]+i][mc[1]+j];
                System.out.println(terrain);
                chunks[i][j] = new Chunk(i, j, this);
                chunks[i][j].setTerrain(terrain);
            }
        }
        imported_terrain = true;
    }
    
    public void importForest(int[][] map) {
        if (imported_forest) return;
        int trees = 0;
        int[] mc = parent.getMapCoords(x, y, 0, 0);
        for (int i = 0; i < sizeChunks(); i++) {
            for (int j = 0; j < sizeChunks(); j++) {
                int spawn = map[mc[0]+i][mc[1]+j];
                if (spawn == 1) {
                    Entity tree = Entity.create("Tree");
                    int wc[] = parent.getWorldCoordsFromMap(i, j);
                    tree.setWorldX(wc[0]+(Chunk.sizePixels()/4));
                    tree.setWorldY(wc[1]+Chunk.sizePixels()/4);
                    if (parent.addEntity(tree)) trees++;
                }
            }
        }
        System.out.println("Importing forest map: "+map.length+"x"+map[0].length);
        System.out.println(trees+" trees imported!");
        imported_forest = true;
    }  
    
    public World getWorld() {
        return parent;
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
                    Chunk c = new Chunk(0, 0, this);
                    if (c.load(br)) chunks[c.offsets()[0]][c.offsets()[1]] = c;
                }
                if (line.indexOf("x=") == 0) x = Integer.parseInt(line.replace("x=", ""));
                if (line.indexOf("it=") == 0) imported_terrain = Boolean.parseBoolean(line.replace("it=", ""));
                if (line.indexOf("if=") == 0) imported_forest = Boolean.parseBoolean(line.replace("if=", ""));
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
                bw.write("it="+imported_terrain+"\n");
                bw.write("if="+imported_forest+"\n");
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
