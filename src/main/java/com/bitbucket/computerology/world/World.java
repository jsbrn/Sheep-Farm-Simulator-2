package com.bitbucket.computerology.world;

import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.events.EventHandler;
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
    
    private ArrayList<Sector> sectors, active_sectors;
    private Sector origin; //the (0,0) sector marks the world origin
    private Random rng;
    private int seed;
    
    private Player player;
    
    private ArrayList<EventHandler> event_handlers;
    private double time;
    
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
        this.active_sectors = new ArrayList<Sector>();
        this.player = new Player();
        this.time = 0;
        this.event_handlers = new ArrayList<EventHandler>();
        this.event_handlers.add(new EventHandler());
    }
    
    public static World getWorld() {
        return world;
    }
    
    public static void newWorld() {
        world = new World();
    }
    
    
    /**
     * Generates in a radius around (0,0). Force-creates an origin sector that
     * the others will naturally be relative to. You can call generateAround anytime
     * after this function has been called. This must be called before any other world
     * generation functions are called: it adds the origin sector which every other coordinate
     * is relative to.
     */
    public void generate() {
        Sector o = new Sector(0, 0, this);
        addSector(o, SECTOR_LIST);
        o.randomizeBiome();
        o.generate();
        generateAround(0, 0);
    }
    
    public void update() {
        for (EventHandler e: event_handlers) { e.update(); }
        time+=MiscMath.get24HourConstant(1, 1);
    }
    
    public boolean addEntity(Entity e) {
        int sc[] = getSectorCoords(e.getWorldX(), e.getWorldY());
        Sector s = getSector(sc[0], sc[1]);
        if (s == null) return false;
        if (s.addEntity(e)) { 
            this.addSector(s, ACTIVE_SECTOR_LIST); 
            s.addEntity(e);
        }
        return false;
    }
    
    /** Returns the current time since world creation (in minutes).**/
    public int getTime() { return (int)time; }
    
    /** Returns the hour in 24-hour format **/
    public int getHour() { return (int)(time % 1440) / 60; }
    
    public int sectorCount() { return sectors.size(); }
    public int activeSectorCount() { return active_sectors.size(); }

    /**
     * Find the sector with the offset value specified.
     * @param x The offset (in sectors) from the origin.
     * @param y The offset (in sectors) from the origin.
     * @return A Sector instance, or null if not found.
     */
    public Sector getSector(int x, int y) {
        Sector s = getSector(x, y, 0, sectors.size()-1, active_sectors);
        if (s == null) {
            s = getSector(x, y, 0, sectors.size()-1, sectors);
        }
        return s;
    }
    
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
     * Adds (and sorts) a sector to the specified list (LOADED_SECTORS, UNLOADED_SECTORS).
     * @param s The sector to add.
     * @param list The list to add to.
     * @return A boolean indicating the success of the operation.
     */
    public boolean addSector(Sector s, int list) {
        if (list == SECTOR_LIST) return addSector(s, 0, sectors.size()-1, sectors);
        if (list == ACTIVE_SECTOR_LIST) return addSector(s, 0, active_sectors.size()-1, active_sectors);
        return false;
    }
    
    private boolean addSector(Sector s, int l, int u, ArrayList<Sector> list) {
        int index = getPotentialSectorIndex(s.offsets()[0], s.offsets()[1], l, u, list);
        if (index <= -1 || index > list.size()) { 
            return false; 
        }
        list.add(index, s);
        if (s.offsets()[0] == 0 && s.offsets()[1] == 0 && origin == null) origin = s; //set the origin to be the first sector added
        return true;
    }
    
    /**
     * Given a sector (x, y), determine the index it needs to enter the list at,
     * to keep the list sorted.
     * @param l Lower bound of the search region (when calling first, use 0)
     * @param u Upper bound of the search region (when calling first, use size()-1)
     * @param list The list to search.
     * @return 
     */
    private static int getPotentialSectorIndex(int x, int y, int l, int u, ArrayList<Sector> list) {
        //if the bounds are the number, then return the bound
        
        if (list.isEmpty()) return 0;
        if (list.get(0).compareTo(x, y) > 0) return 0;
        if (list.get(list.size()-1).compareTo(x, y) < 0) return list.size();
        
        int lsize = (u+1)-l;
        int index = lsize/2 + l;

        if (lsize == 0) return -1;
        
        Sector element = list.get(index);
        int cmp = element.compareTo(x, y);
        
        if (cmp == 0) return -1;
        
        int sub_bounds[] = new int[]{cmp > 0 ? l : index, cmp > 0 ? index : u};
        if ((sub_bounds[1]+1)-sub_bounds[0] <= 2) { //if sublist is two in length
            if (list.get(sub_bounds[0]).compareTo(x, y) < 0
                    && list.get(sub_bounds[1]).compareTo(x, y) > 0) return sub_bounds[0]+1;
            return -1;
        } else {
            return getPotentialSectorIndex(x, y, sub_bounds[0], sub_bounds[1], list);
        }
    }
    
    public Player getPlayer() { return player; }
    
    public ArrayList<Sector> sectors() { return sectors; }

    public void draw(Graphics g) {
        if (Assets.CHUNK_TERRAIN == null) return;
        int x, y, w = Display.getWidth() + Chunk.SIZE_PIXELS,
                h = Display.getHeight() + Chunk.SIZE_PIXELS;
        Assets.CHUNK_TERRAIN.startUse();
        for (x = -Chunk.SIZE_PIXELS; x < w; x+=Chunk.SIZE_PIXELS) {
            for (y = -Chunk.SIZE_PIXELS; y < h; y+=Chunk.SIZE_PIXELS) {
                int sc[] = getSectorCoords(x, y);
                int cc[] = getChunkCoords(x, y);
                Sector s = getSector(sc[0], sc[1]);
                Chunk c = (s != null) ? s.getChunk(cc[0], cc[1]) : null;
                if (c != null) { c.draw(g); }
            }
        }
        for (Sector s: active_sectors) {
            for (int i = 0; i != s.entityCount(); i++) {
                s.getEntity(i).draw(g);
            }
        }
        Assets.CHUNK_TERRAIN.endUse();
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
            bw.write("--------------------------------\n");
            bw.write("t="+world.time+"\n");
            world.player.save(bw);
            for (Sector s: world.sectors) s.save(bw);
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * "Destroys" the world by setting it to null, thus dereferencing
     * EVERYTHING in it (thanks, GC). This is probably the worst way to do it.
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
                if (line.equals("s")) {
                    Sector s = new Sector(0, 0, world);
                    if (s.load(br)) world.addSector(s, SECTOR_LIST);
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
        addSector(s, SECTOR_LIST);
        return s;
    }
    
    /**
     * Generates around the specified world coordinates.
     * @param world_x World x.
     * @param world_y World y.
     */
    public void generateAround(int world_x, int world_y) {
        int osc[] = getOnscreenCoords(world_x, world_y);
        int sc[] = getSectorCoords(osc[0], osc[1]);
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
    
    public void brush(double os_x, double os_y, int diametre, int terrain, boolean overwrite) {
        for (int w = (-diametre/2); w != (diametre/2); w++) {
            for (int h = (-diametre/2); h != (diametre/2); h++) {
                if (MiscMath.distanceBetween(w, h, 0, 0) <= (diametre/2)) {
                    int sc[] = this.getSectorCoords(os_x+(w*Chunk.SIZE_PIXELS), os_y+(h*Chunk.SIZE_PIXELS));
                    int cc[] = this.getChunkCoords(os_x+(w*Chunk.SIZE_PIXELS), os_y+(h*Chunk.SIZE_PIXELS));
                    Sector s = this.getSector(sc[0], sc[1]);
                    Chunk c = (s != null) ? s.getChunk(cc[0], cc[1]) : null;
                    if (c != null) {
                        if (c.getTerrain() != -1 || overwrite) c.setTerrain(terrain);
                    }
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
    
    public int[] getWorldCoords(double onscreen_x, double onscreen_y) {
        if (origin == null) return new int[]{(int)onscreen_x, (int)onscreen_y};
        int[] o = origin.onScreenCoords();
        return new int[]{(int)(onscreen_x-o[0]), (int)(onscreen_y-o[1])};
    }
    
    public int[] getOnscreenCoords(int world_x, int world_y) {
        double shift_x = (Camera.getX())-(Display.getWidth()/2), shift_y = (Camera.getY())-(Display.getHeight()/2);
        return new int[]{(int)((world_x)-shift_x), (int)((world_y)-shift_y)};
    }
    
    public int[] getSectorCoords(double onscreen_x, double onscreen_y) {
        int[] world_coords = getWorldCoords(onscreen_x, onscreen_y);
        int sector_width = Sector.SIZE_CHUNKS*Chunk.SIZE_PIXELS;
        return new int[]{(int)Math.floor((double)world_coords[0]/(double)sector_width), 
            (int)Math.floor((double)world_coords[1]/(double)sector_width)};
    }
    
    public int[] getChunkCoords(double onscreen_x, double onscreen_y) {
        int[] sector_coords = getSectorCoords(onscreen_x, onscreen_y);
        int[] world_coords = getWorldCoords(onscreen_x, onscreen_y);
        int sector_width = Chunk.SIZE_PIXELS*Sector.SIZE_CHUNKS;
        sector_coords[0]*= sector_width; sector_coords[1]*= sector_width;
        return new int[]{(int)((world_coords[0]-sector_coords[0])/(double)Chunk.SIZE_PIXELS), 
            (int)((world_coords[1]-sector_coords[1])/(double)Chunk.SIZE_PIXELS)};
    }
    
}
