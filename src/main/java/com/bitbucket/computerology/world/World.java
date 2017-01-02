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
    
    private ArrayList<Entity> active_entities, moving_entities, render_entities;
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
        this.sectors.ensureCapacity(5000); //preallocate array to 5000 sectors
        this.player = new Player();
        this.time = 0;
        this.event_handlers = new ArrayList<EventHandler>();
        this.event_handlers.add(new EventHandler());
        this.active_entities = new ArrayList<Entity>();
        this.active_entities.ensureCapacity(1000);
        this.moving_entities = new ArrayList<Entity>();
        this.moving_entities.ensureCapacity(1000);
        this.render_entities = new ArrayList<Entity>();
        this.render_entities.ensureCapacity(1000);
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
        int max_w = w < Chunk.sizePixels() ? w : w+Chunk.sizePixels();
        int max_h = h < Chunk.sizePixels() ? h : h+Chunk.sizePixels();
        int i_inc = w < Chunk.sizePixels() ? w : Chunk.sizePixels();
        int j_inc = h < Chunk.sizePixels() ? h : Chunk.sizePixels();
        for (int i = 0; i <= max_w; i+=i_inc) {
            for (int j = 0; j <= max_h; j+=j_inc) {
                int sc[] = getSectorCoords(x+i, y+j);
                int cc[] = getChunkCoords(x+i, y+j);
                Sector s = getSector(sc[0], sc[1]);
                Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
                if (c != null && !list.contains(c)
                        && c.intersects(x, y, w, h)) list.add(c);
            }
        }
        return list;
    }
    
    public Entity getEntity(int x, int y) {
        int sc[] = getSectorCoords(x, y);
        int cc[] = getChunkCoords(x, y);
        Sector s = getSector(sc[0], sc[1]);
        Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
        if (c != null) {
            for (int i = c.getEntities().size()-1; i > -1; i--) {
                if (c.getEntities().get(i).intersects(x, y, 1, 1)) 
                    return c.getEntities().get(i);
            }
        }
        return null;
    }
    
    public ArrayList<Entity> getEntities(int x, int y, int w, int h) {
        ArrayList<Entity> list = new ArrayList<Entity>();
        for (Chunk c: getChunks(x, y, w, h))
            for (Entity e: c.getEntities())
                if (!list.contains(e)
                        && e.intersects(x, y, w, h)) list.add(e);
        return list;
    }
    
    public boolean refreshEntity(Entity e, boolean add) {
        int sc[] = getSectorCoords(e.getWorldX(), e.getWorldY());
        Sector s = getSector(sc[0], sc[1]);
        if (s != null) {if (add) s.addEntity(e); else s.removeEntity(e);}
        ArrayList<Chunk> chunks = getChunks(e.getWorldX()-e.getWidth()/2,
                e.getWorldY()-e.getHeight()/2,e.getWidth(), e.getHeight());
        boolean success = true;
        for (Chunk c: chunks) {
            if (add) if (!c.addEntity(e)) success = false;
            if (!add) c.removeEntity(e);
        }
        return success;
    }
    
    public boolean addEntity(Entity e) {
        if (e == null) return false;
        refreshEntity(e, true);
        if (e.moves()) moving_entities.add(e);
        if (e.updates()) active_entities.add(e);
        if (e.renders()) render_entities.add(e);
        return e.moves() || e.updates() || e.renders();
    }
    
    public boolean removeEntity(Entity e) {
        if (e == null) return false;
        refreshEntity(e, false);
        if (e.moves()) moving_entities.remove(e);
        if (e.updates()) active_entities.remove(e);
        if (e.renders()) render_entities.remove(e);
        return e.moves() || e.updates() || e.renders();
    }
    
    /**
     * Generates in a radius around (0,0).
     */
    public void generate() {
        generateAround(0, 0);
    }
    
    public final void update() {
        for (EventHandler e: event_handlers) { e.update(); }
        
        for (int i = 0; i < active_entities.size(); i++) {
            if (i >= active_entities.size() || i < 0) break;
            active_entities.get(i).update();
        }
        for (int i = 0; i < moving_entities.size(); i++) {
            if (i >= moving_entities.size() || i < 0) break;
            moving_entities.get(i).move();
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
        for (x = 0; x < w; x+=Chunk.onScreenSize()) {
            for (y = 0; y < h; y+=Chunk.onScreenSize()) {
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
        
        for (Entity e: render_entities) { 
            int osc[] = getOnscreenCoords(e.getWorldX(), e.getWorldY());
            if (MiscMath.pointIntersectsRect(osc[0], osc[1], -Entity.maxSizePixels(), -Entity.maxSizePixels(), 
                    Display.getWidth()+Entity.maxSizePixels(), Display.getWidth()+Entity.maxSizePixels())) {
                e.draw(g);
            }
        }

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
            for (Sector s: world.sectors) {
                s.save(bw);
                for (Entity e: s.getEntities()) e.save(bw);
            }
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
