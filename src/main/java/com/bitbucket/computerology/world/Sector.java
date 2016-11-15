package com.bitbucket.computerology.world;

import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.components.Position;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;

public class Sector {
    
    private World parent;
    private Chunk[][] chunks;
    private int x, y, biome;
    private boolean generated;
    
    int chunk_update_index = 0;
    
    private ArrayList<Entity> entities, important_entities;
    
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
        this.entities = new ArrayList<Entity>();
        this.important_entities = new ArrayList<Entity>();
    }
    
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
    
    public void update() {
        for (int i = 0; i != 10; i++) {
            //get the next chunk to update
            Chunk c = getChunk(chunk_update_index % sizeChunks(), chunk_update_index / sizeChunks());
            //update the chunk then add 1 to the chunk index OR set it to 0 if it is >= the
            //total chunk count
            if (c != null) { c.update(); chunk_update_index = 
                    chunk_update_index >= sizeChunks()*sizeChunks() ? 0 : chunk_update_index + 1; }
            else { chunk_update_index = 0; }
        }
    }
    
    /**
     * Updates only the entities marked as "important", i.e. the ones that have flows.
     */
    public void updateEntities() {
        for (Entity e: important_entities) e.update();
    }
    
    public boolean removeEntity(Entity e) {
        if (entities.remove(e)) {
            if (e.isImportant()) {
                important_entities.remove(e);
                if (important_entities.isEmpty()) parent.deactivateSector(this);
            }
            return true;
        }
        return false;
    }
    
    public boolean containsEntity(Entity e) { return entities.contains(e); }
    
    public boolean generated() {
        return generated;
    }
    
    public int[] offsets() {
        return new int[]{x, y};
    }
    
    public int[] worldCoords() {
        return new int[]{x*sizePixels(), y*sizePixels()};
    }
    
    public static int sizeChunks() { return 80; }
    public static int sizePixels() { return Chunk.size()*sizeChunks(); }
    public static int onScreenSize() { return sizePixels()*Camera.getZoom(); }
    
    public int getBiome() {
        return biome;
    }
    
    public int[] onScreenCoords() {
        return World.getWorld().getOnscreenCoords(worldCoords()[0], worldCoords()[1]);
    }
    
    /**
     * Sets the sector biome to the specified biome.
     * @param biome An integer defining the biome (i.e. Chunk.DESERT)
     */
    void setBiome(int biome) {
        this.biome = biome;
    }
    
    /**
     * Get sectors adjacent to this sector. Does not consider any sector to be
     * adjacent to itself.
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
    
    public boolean addEntity(Entity e) {
        Component p = e.getComponent("Position");
        if (p == null) return false;
        int osc[] = parent.getOnscreenCoords(((Position)p).getWorldX(),
                ((Position)p).getWorldY());
        int cc[] = parent.getChunkCoords(osc[0], osc[1]);
        Chunk c = getChunk(cc[0], cc[1]);
        if (c == null) return false;
        if (c.addEntity(e)) { 
            entities.add(e); 
            if (e.isImportant()) important_entities.add(e);
            if (!important_entities.isEmpty()) parent.activateSector(this); 
            return true; 
        } else { 
            return false; 
        }
    }
    
    public int entityCount() { return entities.size(); }
    public Entity getEntity(int index) { 
        if (index > -1 && index < entities.size()) return entities.get(index);
        return null;
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
            
            if (non_null.isEmpty() || Math.abs(parent.rng().nextInt() % 100) <= 30) 
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
    
    public boolean load(BufferedReader br) {
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
                if (line.indexOf("b=") == 0) biome = Integer.parseInt(line.replace("b=", ""));
                if (line.indexOf("g=") == 0) generated = Boolean.parseBoolean(line.replace("g=", ""));
            }
        } catch (IOException ex) {
            Logger.getLogger(Sector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void save(BufferedWriter bw) {
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
    
    void generate() {

        //set each chunk to match the sector biome
        for (int w = 0; w != chunks.length; w++) {
            for (int h = 0; h != chunks[w].length; h++) {
                if (chunks[w][h].getTerrain() == -1) chunks[w][h].setTerrain(biome);
            }
        }
        
        int b_count = Math.abs(parent.rng().nextInt() % (sizeChunks()/2))+8;
        for (int i = 1; i != b_count+2; i++) {
            int b_x = 0, b_y = 0;
            while (i > 0) {
                b_x = Math.abs(parent.rng().nextInt() % sizeChunks());
                b_y = Math.abs(parent.rng().nextInt() % sizeChunks());
                if ((b_x <= (4+(sizeChunks()/32)) || b_x >= sizeChunks()-(4+(sizeChunks()/32))) 
                        || (b_y <= (4+(sizeChunks()/32)) || b_y >= sizeChunks()-(4+(sizeChunks()/32)))) break;
            }
            World.getWorld().brush(onScreenCoords()[0]+b_x*Chunk.onScreenSize(), onScreenCoords()[1]+b_y*Chunk.onScreenSize(), 
                    Math.abs(parent.rng().nextInt() % 8)+(4+(sizeChunks()/32)), chunks[b_x][b_y].randomValidTerrain(-1), true);
        }
        
        generated = true;
        
    }
    
    void cleanup(int passes) {
        if (passes <= 0) return;
        for (int w = 0; w != chunks.length; w++) {
            for (int h = 0; h != chunks[w].length; h++) {
                if (chunks[w][h].isOrphan(false)) {
                    chunks[w][h].setTerrain(biome);
                }
            }
        }
    }
    
    /**
     * Gives the sector a random biome that takes into consideration the sectors
     * around it. Desert will never be adjacent to tundra.
     */
    void randomizeBiome() {
        biome = randomValidBiome(-1);
    }

}
