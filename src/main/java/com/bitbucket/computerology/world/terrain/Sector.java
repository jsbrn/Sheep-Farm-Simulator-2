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
    private int x, y, biome;
    private boolean generated, town_sector;
    
    ArrayList<Entity> entities;
    
    private Generator generator;
    
    int chunk_update_index = 0;
    
    public Sector(int x, int y, World parent) {
        this.x = x; this.y = y;
        this.parent = parent;
        this.generated = false;
        this.chunks = new Chunk[sizeChunks()][sizeChunks()];
        this.biome = -1;
        for (int h = 0; h != sizeChunks(); h++) {
            for (int w = 0; w != sizeChunks(); w++) {
                chunks[w][h] = new Chunk(w, h, this);
            }
        }
        this.town_sector = Math.abs(parent.rng().nextInt() % 50) == 0;
        this.entities = new ArrayList<Entity>();
        this.entities.ensureCapacity(1000);
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
    
    public boolean generated() {
        return generated;
    }
    
    public int[] offsets() {
        return new int[]{x, y};
    }
    
    public int[] worldCoords() {
        return new int[]{x*sizePixels(), y*sizePixels()};
    }
    
    public static int sizeChunks() { return 32; }
    public static int sizePixels() { return Chunk.sizePixels()*sizeChunks(); }
    public static int onScreenSize() { return sizePixels()*Camera.getZoom(); }
    
    public int getBiome() {
        return biome;
    }
    
    public int[] onScreenCoords() {
        return World.getWorld().getOnscreenCoords(worldCoords()[0], worldCoords()[1]);
    }
    
    /**
     * Sets the sector biome to the specified biome. Applies a new generator
     * instance to the sector, so use this instead of just giving biome a new value.
     * @param biome An integer defining the biome (i.e. Chunk.DESERT)
     */
    void setBiome(int biome) {
        this.biome = biome;
        this.generator = Generator.create(this);
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
    
    /**
     * Gets a random valid biome for this particular sector. Will be more likely to
     * return the biome of the most frequent type from the adjacent sectors.
     * @param exception Will not return this biome in any case. Pass -1 if no exception.
     * @return An Integer representing the randomly chosen biome.
     */
    public int randomValidBiome(int exception) {
        int b = -1;
        while (!isBiomeAllowed(b) || b == exception) {
            Sector adj[] = getAdjacentSectors();
            ArrayList<Sector> non_null = new ArrayList<Sector>();
            for (Sector s: adj) {if (s != null) { non_null.add(s); }}
            
            if (non_null.isEmpty() || Math.abs(parent.rng().nextInt() % 100) <= 10) 
                b = Math.abs(parent.rng().nextInt() % Chunk.BIOME_COUNT);
            else
                b = non_null.get(Math.abs(parent.rng().nextInt() % non_null.size())).getBiome();
            
        }
        return b;
    }
    
    public boolean isBiomeAllowed(int b) {
        if (b < 0 || b >= Chunk.BIOME_COUNT) return false;
        Sector adj[] = getAdjacentSectors();
        ArrayList<Sector> non_null = new ArrayList<Sector>();
        for (Sector s: adj) {if (s != null) { non_null.add(s); }}
        if (non_null.isEmpty()) return true;
        for (Sector s: non_null) {
            if (b == Chunk.SAND && s.getBiome() == Chunk.SNOW) return false;
            if (b == Chunk.SNOW && s.getBiome() == Chunk.SAND) return false;
        }
        return true;
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
                if (line.indexOf("y=") == 0) y = Integer.parseInt(line.replace("y=", ""));
                if (line.indexOf("b=") == 0) setBiome(Integer.parseInt(line.replace("b=", "")));
                if (line.indexOf("g=") == 0) generated = Boolean.parseBoolean(line.replace("g=", ""));
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
                bw.write("b="+biome+"\n");
                bw.write("g="+generated+"\n");
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
    
    /*****************************************************************************************************
     * Generator code below; these functions should not be called outside of generate() or World.init(). *
     *****************************************************************************************************/
    
    public final void generate() {
        if (generated) return;
        
        //blend with the other surrounding sectors
        int section = Sector.sizeChunks()/8, schunks = Sector.sizeChunks();
        for (int bx = 0; bx < schunks; bx+=section) {
            for (int by = 0; by < schunks; by+=section) {
                if ((bx > section*2 && bx < schunks-(section*2)) //if not on the edge, continue
                        && (by > section*2 && by < schunks-(section*2))) continue;
                int d = Math.abs(parent.rng().nextInt() % schunks/2)+6;
                Generator.brush(worldCoords()[0]+(bx*Chunk.sizePixels()), 
                        worldCoords()[1]+(by*Chunk.sizePixels()), 
                        d, biome);
            }
        }
        
        //call on its generator to add some biome specific terrain details
        if (generator != null) generator.generate();
        
        //mark as generated so that it will not regenerate
        generated = true;
        
    }
    
    public Generator generator() { return generator; }
    
    /**
     * Gives the sector a random biome that takes into consideration the sectors
     * around it. Desert will never be adjacent to tundra.
     */
    public void randomizeBiome() {
        if (!generated) setBiome(randomValidBiome(-1));
    }

}
