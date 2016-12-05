package com.bitbucket.computerology.world;

import com.bitbucket.computerology.world.terrain.Sector;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.events.EventHandler;
import com.bitbucket.computerology.world.towns.Town;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;

public class World {
    
    private static World world;
    public static int SECTOR_LIST = 0, ACTIVE_SECTOR_LIST = 1;
    
    private ArrayList<Sector> sectors;
    private Random rng;
    private int seed;
    
    private Player player;
    
    private ArrayList<EventHandler> event_handlers;
    private double time;
    
    private ArrayList<Entity> entities;
    private ArrayList<Town> towns;
    
    private World(int seed) {
        this.seed = seed;
        init();
    }
    
    private World() {
        this.seed = Math.abs(new Random().nextInt());
        init();
    }
    
    private void init() {
        this.rng = new Random(seed);
        this.sectors = new ArrayList<Sector>();
        this.player = new Player();
        this.time = 0;
        this.event_handlers = new ArrayList<EventHandler>();
        this.event_handlers.add(new EventHandler());
        this.entities = new ArrayList<Entity>();
        this.towns = new ArrayList<Town>();
    }
    
    public static World getWorld() {
        return world;
    }
    
    public static void newWorld() {
        world = new World();
    }
    
    public Town getTown(Sector s) {
        for (Town t: towns) if (t.getParent().equals(s)) return t;
        return null;
    }
    
    public Town getTown(int sector_x, int sector_y) {
        Sector s = getSector(sector_x, sector_y);
        return getTown(s);
    }
    
    /**
     * Gets all of the chunks intersecting the specified rectangle.
     * @param x World coordinates.
     * @param y World coordinates.
     * @param w Width.
     * @param h Height.
     * @return An ArrayList<Chunk>.
     */
    public ArrayList<Chunk> getChunks(int x, int y, int w, int h) {
        ArrayList<Chunk> list = new ArrayList<Chunk>();
        for (int i = 0; i < (w/Chunk.sizePixels())+1; i++) {
            for (int j = 0; j < (h/Chunk.sizePixels())+1; j++) {
                int sc[] = getSectorCoords(x+(i*Chunk.sizePixels()), y+(j*Chunk.sizePixels()));
                int cc[] = getChunkCoords(x+(i*Chunk.sizePixels()), y+(j*Chunk.sizePixels()));
                Sector s = getSector(sc[0], sc[1]);
                Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
                if (c != null) list.add(c);
            }
        }
        return list;
    }
    
    public boolean addEntity(Entity e) {
        if (e == null) return false;
        boolean b[] = {false,false,false,false};
        int i = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                int sc[] = getSectorCoords(
                        e.getWorldX() - e.getWidth()/2 + (x*e.getWidth()), 
                        e.getWorldY() - e.getHeight()/2 + (y*e.getHeight()));
                Sector s = getSector(sc[0], sc[1]);
                if (s != null) b[i] = s.addEntity(e); else b[i] = false;
                i++;
            }
        }

        if (b[0] || b[1] || b[2] || b[3]) {
            if (!entities.contains(e)) {
                entities.add(e);
                return true;
            }
        }
        return false;
    }
    
    public boolean removeEntity(Entity e) {
        if (e == null) return false;
        boolean b[] = {false,false,false,false};
        int i = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                int sc[] = getSectorCoords(
                        e.getWorldX() - e.getWidth()/2 + (x*e.getWidth()), 
                        e.getWorldY() - e.getHeight()/2 + (y*e.getHeight()));
                Sector s = getSector(sc[0], sc[1]);
                if (s != null) b[i] = s.removeEntity(e); else b[i] = false;
                i++;
            }
        }

        if (b[0] || b[1] || b[2] || b[3]) {
            entities.remove(e);
            return true;
        }
        return false;
    }
    
    /**
     * Generates in a radius around (0,0).
     */
    public void generate() {
        generateAround(0, 0);
    }
    
    public final void update() {
        for (EventHandler e: event_handlers) { e.update(); }
        for (int i = 0; i < entities.size(); i++) {
            if (i >= entities.size() || i < 0) break;
            entities.get(i).update();
        }
        time+=MiscMath.get24HourConstant(1, 1);
    }
    
    /** Returns the current time since world creation (in minutes).**/
    public int getTime() { return (int)time; }
    
    /** Returns the hour in 24-hour format **/
    public int getHour() { return (int)(time % 1440) / 60; }
    
    public int sectorCount() { return sectors.size(); }

    /**
     * Find the sector with the offset value specified.
     * @param x The offset (in sectors) from the origin.
     * @param y The offset (in sectors) from the origin.
     * @return A Sector instance, or null if not found.
     */
    public Sector getSector(int x, int y) {
        Sector s = getSector(x, y, 0, sectors.size()-1, sectors);
        if (s == null) {
            s = getSector(x, y, 0, sectors.size()-1, sectors);
        }
        return s;
    }
    
    /**
     * Gets the sector at sector coords (x, y). Uses a binary search algorithm.
     * @param x Sector coordinate.
     * @param y Sector coordinate.
     * @param l The lower bound of the list to search (start with 0).
     * @param u The upper bound of the list to search (start with list.size - 1).
     * @param list The list to search.
     * @return A Sector instance, or null if none found at (x, y).
     */
    private Sector getSector(int x, int y, int l, int u, ArrayList<Sector> list) {
        
        if (list.isEmpty()) return null;
        //if the sector is beyond the first and last, return null
        //if the sector is the first or last, return the first or last, respectively
        if (list.get(0).compareTo(x, y) > 0 || list.get(list.size()-1).compareTo(x, y) < 0) return null;
        if (list.get(u).offsets()[0] == x && list.get(u).offsets()[1] == y) return list.get(u);
        if (list.get(l).offsets()[0] == x && list.get(l).offsets()[1] == y) return list.get(l);
        
        int lsize = (u+1)-l;
        int index = lsize/2 + l;

        if (lsize == 0) return null;
        
        Sector element = list.get(index);
        int cmp = element.compareTo(x, y);
        
        if (cmp == 0) return list.get(index);
        
        int sub_bounds[] = new int[]{cmp > 0 ? l : index, cmp > 0 ? index : u};
        if ((sub_bounds[1]+1)-sub_bounds[0] <= 2) { //if sublist is two in length
            if (cmp > 0) if (sub_bounds[0] > -1) 
                if (list.get(sub_bounds[0]).offsets()[0] == x && list.get(sub_bounds[0]).offsets()[1] == y) 
                    return list.get(sub_bounds[0]);
            if (cmp < 0) if (sub_bounds[1] < list.size()) 
                if (list.get(sub_bounds[1]).offsets()[0] == x && list.get(sub_bounds[1]).offsets()[1] == y) 
                    return list.get(sub_bounds[1]);
            return null;
        } else {
            return getSector(x, y, sub_bounds[0], sub_bounds[1], list);
        }
    }
    
    /**
     * Adds (and sorts) a sector to the list of sectors.
     * @param s The sector to add.
     * @return A boolean indicating the success of the operation.
     */
    public boolean addSector(Sector s) {
        return addSector(s, 0, sectors.size()-1);
    }
    
    private boolean addSector(Sector s, int l, int u) {
        int index = getPotentialSectorIndex(s.offsets()[0], s.offsets()[1], l, u);
        if (index <= -1 || index > sectors.size()) { 
            return false; 
        }
        sectors.add(index, s);
        return true;
    }
    
    /**
     * Given a sector (x, y), determine the index it needs to enter the list at,
     * to keep the list sorted. If the sector is found to already exist in the list,
     * -1 is returned. Uses a binary search algorithm.
     * @param l Lower bound of the search region (when calling first, use 0)
     * @param u Upper bound of the search region (when calling first, use size()-1)
     * @return An integer of the above specifications.
     */
    private int getPotentialSectorIndex(int x, int y, int l, int u) {
        //if the bounds are the number, then return the bound
        
        if (sectors.isEmpty()) return 0;
        if (sectors.get(0).compareTo(x, y) > 0) return 0;
        if (sectors.get(sectors.size()-1).compareTo(x, y) < 0) return sectors.size();
        
        int lsize = (u+1)-l;
        int index = lsize/2 + l;

        if (lsize == 0) return -1;
        
        Sector element = sectors.get(index);
        int cmp = element.compareTo(x, y);
        
        if (cmp == 0) return -1;
        
        int sub_bounds[] = new int[]{cmp > 0 ? l : index, cmp > 0 ? index : u};
        if ((sub_bounds[1]+1)-sub_bounds[0] <= 2) { //if sublist is two in length
            if (sectors.get(sub_bounds[0]).compareTo(x, y) < 0
                    && sectors.get(sub_bounds[1]).compareTo(x, y) > 0) return sub_bounds[0]+1;
            return -1;
        } else {
            return getPotentialSectorIndex(x, y, sub_bounds[0], sub_bounds[1]);
        }
    }
    
    public Player getPlayer() { return player; }
    
    public ArrayList<Sector> sectors() { return sectors; }

    public void draw(Graphics g) {
        drawTerrain(false, g);
        drawTerrain(true, g);
        drawEntities(g);
    }
    
    void drawTerrain(boolean corners, Graphics g) {
        if (Assets.getTerrainSprite(corners) == null) return;
        int x, y, w = Display.getWidth() + (2*Chunk.onScreenSize()),
                h = Display.getHeight() + (2*Chunk.onScreenSize());
        Assets.getTerrainSprite(corners).startUse();
        for (x = 0; x < w; x+=Chunk.onScreenSize()/2) {
            for (y = 0; y < h; y+=Chunk.onScreenSize()/2) {
                int wc[] = getWorldCoords(x, y);
                int sc[] = getSectorCoords(wc[0], wc[1]);
                int cc[] = getChunkCoords(wc[0], wc[1]);
                Sector s = getSector(sc[0], sc[1]);
                Chunk c = (s != null) ? s.getChunk(cc[0], cc[1]) : null;
                if (c != null) { c.draw(corners, g); }
            }
        }
        Assets.getTerrainSprite(corners).endUse();
    }
    
    /**
     * Loops through all entities in the world and draws the ones that are visible.
     * In the future it will not loop through ALL entities.
     */
    void drawEntities(Graphics g) {
        
        for (Entity e: entities) { 
            int osc[] = getOnscreenCoords(e.getWorldX(), e.getWorldY());
            if (MiscMath.pointIntersects(osc[0], osc[1], -Entity.maxSizePixels(), -Entity.maxSizePixels(), 
                    Display.getWidth()+Entity.maxSizePixels(), Display.getWidth()+Entity.maxSizePixels())) {
                e.draw(g);
            }
        }
        
        /**ArrayList<List>
        int x, y, w = Display.getWidth() + Entity.maxSizePixels(), 
                h = Display.getHeight() + Entity.maxSizePixels();
        for (x = -Entity.maxSizePixels(); x < w; x+=Chunk.onScreenSize()) {
            for (y = -Entity.maxSizePixels(); y < h; y+=Chunk.onScreenSize()) {
                int sc[] = getSectorCoords(x, y);
                int cc[] = getChunkCoords(x, y);
                Sector s = getSector(sc[0], sc[1]);
                
            }
        }**/
    }
    
    public static void save() {
        File f = new File(Assets.SAVE_DIR+"/world.txt");
        FileWriter fw;
        System.out.println("Saving to file "+f.getAbsoluteFile().getAbsolutePath());
        try {
            if (!f.exists()) f.createNewFile();
            fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("p: player stats\n");
            bw.write("s: sector\n");
            bw.write("c: chunk\n");
            bw.write("e: entity\n");
            bw.write("--------------------------------\n");
            bw.write("t="+(int)world.time+"\n");
            bw.write("cx="+(int)Camera.getX()+"\n");
            bw.write("cy="+(int)Camera.getY()+"\n");
            bw.write("cz="+(int)Camera.getZoom()+"\n");
            world.player.save(bw);
            for (Sector s: world.sectors) s.save(bw);
            for (Entity e: world.entities) e.save(bw);
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Sets the world instance to null.
     */
    public static void destroy() {
        world = null;
    }
    
    public static void load() {
        File f = new File(Assets.SAVE_DIR+"/world.txt");
        if (!f.exists()) return;
        FileReader fr;
        System.out.println("Loading from file: "+f.getAbsoluteFile().getAbsolutePath());
        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.replace("", "");
                if (line.contains("t=")) world.time = Double.parseDouble(line.replace("t=", ""));
                if (line.contains("cx=")) Camera.setX(Integer.parseInt(line.replace("cx=", "").trim()));
                if (line.contains("cy=")) Camera.setY(Integer.parseInt(line.replace("cy=", "").trim()));
                if (line.contains("cz=")) Camera.setZoom(Integer.parseInt(line.replace("cz=", "").trim()));
                if (line.equals("s")) {
                    Sector s = new Sector(0, 0, world);
                    if (s.load(br)) world.addSector(s);
                }
                if (line.equals("e")) {
                    Entity e = new Entity();
                    if (e.load(br)) world.addEntity(e);
                }
                if (line.equals("p")) world.player.load(br);
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Creates a new sector at x, y. Adds it to the list of active (loaded) sectors.
     * @param x Sector coordinate x
     * @param y Sector coordinate y
     */
    public Sector createSector(int x, int y) {
        Sector s = new Sector(x, y, this);
        addSector(s);
        return s;
    }
    
    /**
     * Generates around the specified world coordinates.
     * @param world_x World x.
     * @param world_y World y.
     */
    public void generateAround(int world_x, int world_y) {
        int sc[] = getSectorCoords(world_x, world_y);
        createSectors(sc[0], sc[1], 2);
        fillSectors(sc[0], sc[1], 1);
    }
    
    private void createSectors(int sector_x, int sector_y, int r) {
        for (int h = -r; h != r+1; h++) {
            for (int w = -r; w != r+1; w++) {
                Sector s = getSector(sector_x+w, sector_y+h);
                if (s == null) {
                    s = createSector(sector_x+w, sector_y+h);
                    s.randomizeBiome();
                }
            }
        }
    }
    
    private void fillSectors(int sector_x, int sector_y, int r) {
        for (int h = -r; h != r+1; h++) {
            for (int w = -r; w != r+1; w++) {
                Sector s = getSector(sector_x+w, sector_y+h);
                if (s != null) {
                    if (!s.generated()) s.generate();
                }
            }
        }
    }
    
    public int seed() {
        return seed;
    }
    
    /**
     * Gets the random number generator for this world.
     * @return A Random instance.
     */
    public Random rng() {
        return rng;
    }
    
    public int[] getWorldCoords(int onscreen_x, int onscreen_y) {
        return new int[]{((onscreen_x - (Display.getWidth()/2))/Camera.getZoom()) + Camera.getX(),
            ((onscreen_y - (Display.getHeight()/2))/Camera.getZoom()) + Camera.getY()};
    }
    
    public int[] getOnscreenCoords(double world_x, double world_y) {
        return new int[]{(int)((world_x-Camera.getX())*Camera.getZoom())+(Display.getWidth()/2), 
            (int)((world_y-Camera.getY())*Camera.getZoom())+(Display.getHeight()/2)};
    }
    
    public int[] getSectorCoords(double world_x, double world_y) {
        return new int[]{(int)Math.floor(world_x/Sector.sizePixels()),
            (int)Math.floor(world_y/Sector.sizePixels())};
    }
    
    public int[] getChunkCoords(double world_x, double world_y) {
        int[] cc = new int[]{(int)Math.floor(world_x/Chunk.sizePixels()),
            (int)Math.floor(world_y/Chunk.sizePixels())};
        cc[0] %= Sector.sizeChunks(); cc[1] %= Sector.sizeChunks();
        cc[0] = cc[0] >= 0 ? cc[0] : Sector.sizeChunks() + cc[0];
        cc[1] = cc[1] >= 0 ? cc[1] : Sector.sizeChunks() + cc[1];
        return cc;
    }
    
}
