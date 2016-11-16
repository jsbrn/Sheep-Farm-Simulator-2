package com.bitbucket.computerology.world;

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
    
    private ArrayList<Sector> sectors, active_sectors;
    private Random rng;
    private int seed;
    
    private Player player;
    
    private ArrayList<EventHandler> event_handlers;
    private double time;
    
    private ArrayList<Entity> entities;
    private ArrayList<Town> towns;
    
    private int sector_update_index = 0;
    
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
        
        for (Sector s: active_sectors) s.update();
        
        if (sector_update_index > -1 && sector_update_index < active_sectors.size()) {
            Sector update = active_sectors.get(sector_update_index);
            if (update != null) update.updateEntities();
            sector_update_index++;
        } else {
            sector_update_index = 0;
        }
    }
    
    public boolean addEntity(Entity e) {
        int osc[] = getOnscreenCoords(e.getWorldX(), e.getWorldY());
        int sc[] = getSectorCoords(osc[0], osc[1]);
        Sector s = getSector(sc[0], sc[1]);
        if (s == null) return false;
        if (s.addEntity(e)) {
            System.out.println("Added entity "+e.getType()+" to world! "+e.getWorldX()+", "+e.getWorldY());
            return true;
        }
        return false;
    }
    
    public boolean removeEntity(Entity e) {
        int osc[] = getOnscreenCoords(e.getWorldX(), e.getWorldY());
        int sc[] = getSectorCoords(osc[0], osc[1]);
        Sector s = getSector(sc[0], sc[1]);
        if (s == null) return false;
        if (s.removeEntity(e)) {
            System.out.println("Removed entity "+e.getType()+" from world! "+e.getWorldX()+", "+e.getWorldY());
            entities.remove(e);
            return true;
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
        Sector s = getSector(x, y, 0, active_sectors.size()-1, active_sectors);
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
    
    public boolean activateSector(Sector s) {
        return addSector(s, ACTIVE_SECTOR_LIST);
    }
    
    public boolean deactivateSector(Sector s) {
        return active_sectors.remove(s);
    }
    
    private boolean addSector(Sector s, int l, int u, ArrayList<Sector> list) {
        int index = getPotentialSectorIndex(s.offsets()[0], s.offsets()[1], l, u, list);
        if (index <= -1 || index > list.size()) { 
            return false; 
        }
        list.add(index, s);
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
        drawTerrain(g);
        drawEntities(g);
    }
    
    void drawTerrain(Graphics g) {
        if (Assets.getTerrainSprite() == null) return;
        int x, y, w = Display.getWidth() + Chunk.onScreenSize(),
                h = Display.getHeight() + Chunk.onScreenSize();
        Assets.getTerrainSprite().startUse();
        for (x = -Chunk.onScreenSize(); x < w; x+=Chunk.onScreenSize()) {
            for (y = -Chunk.onScreenSize(); y < h; y+=Chunk.onScreenSize()) {
                int sc[] = getSectorCoords(x, y);
                int cc[] = getChunkCoords(x, y);
                Sector s = getSector(sc[0], sc[1]);
                Chunk c = (s != null) ? s.getChunk(cc[0], cc[1]) : null;
                if (c != null) { c.draw(g); }
            }
        }
        Assets.getTerrainSprite().endUse();
    }
    
    void drawEntities(Graphics g) {
        int osc[] = getOnscreenCoords(Camera.getX(), Camera.getY());
        int sc[] = getSectorCoords(osc[0], osc[1]);
        Sector s = getSector(sc[0], sc[1]);
        if (s != null) {
            for (int i = 0; i != s.entityCount(); i++) s.getEntity(i).draw(g);
            for (Sector a: s.getAdjacentSectors()) {
                if (a == null) continue; //if a == null or is not visible, skip
                if (!MiscMath.rectanglesIntersect(0, 0, Display.getWidth(), Display.getHeight(), 
                        a.onScreenCoords()[0], a.onScreenCoords()[1], Sector.onScreenSize(), Sector.onScreenSize())) continue;
                for (int i = 0; i != a.entityCount(); i++) a.getEntity(i).draw(g);
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
                    if (s.load(br)) world.addSector(s, SECTOR_LIST);
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
                    int sc[] = this.getSectorCoords(os_x+(w*Chunk.onScreenSize()), os_y+(h*Chunk.onScreenSize()));
                    int cc[] = this.getChunkCoords(os_x+(w*Chunk.onScreenSize()), os_y+(h*Chunk.onScreenSize()));
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
        return new int[]{(int)((onscreen_x - (Display.getWidth()/2))/Camera.getZoom()) + Camera.getX()
            , (int)((onscreen_y - (Display.getHeight()/2))/Camera.getZoom()) + Camera.getY()};
    }
    
    public int[] getOnscreenCoords(int world_x, int world_y) {
        return new int[]{((world_x-Camera.getX())*Camera.getZoom())+(Display.getWidth()/2), 
            ((world_y-Camera.getY())*Camera.getZoom())+(Display.getHeight()/2)
        };
    }
    
    public int[] getSectorCoords(double onscreen_x, double onscreen_y) {
        int[] world_coords = getWorldCoords(onscreen_x, onscreen_y);
        int s_size = Sector.sizePixels();
        double x = (double)world_coords[0]/(double)s_size; x = x >= 0 ? x : x-1;
        double y = (double)world_coords[1]/(double)s_size; y = y >= 0 ? y : y-1;
        return new int[]{(int)x,(int)y};
    }
    
    public int[] getChunkCoords(double onscreen_x, double onscreen_y) {
        int[] world_coords = getWorldCoords(onscreen_x, onscreen_y);
        int[] sector_coords = getSectorCoords(onscreen_x, onscreen_y);
        sector_coords[0]*=Sector.sizePixels(); sector_coords[1]*=Sector.sizePixels();
        int[] chunk_coords = new int[]{world_coords[0]-sector_coords[0], world_coords[1]-sector_coords[1]};
        chunk_coords[0] /= Chunk.size(); chunk_coords[1] /= Chunk.size();
        return chunk_coords;
    }
    
}
