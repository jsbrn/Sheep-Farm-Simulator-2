package com.bitbucket.computerology.world;

import com.bitbucket.computerology.world.terrain.Sector;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SimplexNoise;
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
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class World {
    
    private static World world;
    
    Image map_texture;
    private short[][] biome_map;
    private int[] spawn;
    private boolean[][] forest_map, road_map;
    
    private int size_sectors;
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
        this.active_entities.ensureCapacity(500);
        this.moving_entities = new ArrayList<Entity>();
        this.moving_entities.ensureCapacity(500);
        this.render_entities = new ArrayList<Entity>();
        this.render_entities.ensureCapacity(500);
        this.towns = new ArrayList<Town>();
        try {
            this.map_texture = new Image(Sector.sizeChunks(), Sector.sizeChunks());
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
    
    public static World getWorld() {
        return world;
    }
    
    public static void newWorld() {
        world = new World();
    }
    
    public Town getTown(int sector_x, int sector_y) {
        for (Town t: towns) { 
            int[] sc = t.getSectorCoordinates();
            if (sc[0] == sector_x && sc[1] == sector_y) return t;
        }
        return null;
    }
    
    public int movableEntityCount() { return moving_entities.size(); }
    public int activeEntityCount() { return active_entities.size(); }
    
    public void setTerrain(int wx, int wy, int terrain, int rot) {
        int[] sc = getSectorCoords(wx, wy);
        int[] cc = getChunkCoords(wx, wy);
        Sector s = getSector(sc[0], sc[1]);
        Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
        if (c != null) {
            System.out.println("Placing terrain "+terrain+" at rotation "+rot);
            c.setRotation(rot);
            c.setTerrain(terrain);
        }
    }
    
    public int getTerrain(int wx, int wy) {
        int[] sc = getSectorCoords(wx, wy);
        int[] cc = getChunkCoords(wx, wy);
        Sector s = getSector(sc[0], sc[1]);
        Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
        if (c != null) {
            return c.getTerrain();
        }
        return -1;
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
        if (w <= 0 || h <= 0) return list;
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
    
    public Chunk getChunk(int wx, int wy) {
        int[] sc = getSectorCoords(wx, wy);
        int[] cc = getChunkCoords(wx, wy);
        Sector s = getSector(sc[0], sc[1]);
        return s == null ? null : s.getChunk(cc[0], cc[1]);
    }
    
    public Entity getEntity(int x, int y) {
        int sc[] = getSectorCoords(x, y);
        int cc[] = getChunkCoords(x, y);
        Sector s = getSector(sc[0], sc[1]);
        Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
        if (c != null) {
            for (int i = c.getEntities().size()-1; i > -1; i--) {
                if (c.getEntities().get(i).intersects(x, y)) 
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
        //System.out.println(e+" dimensions: "+e.getWidth()+", "+e.getHeight());
        boolean success = true;
        for (Chunk c: chunks) {
            //System.out.println(add ? "Adding "+e+" to "+c : "Removing "+e+" from "+c);
            if (add) { if (!c.addEntity(e)) success = false; }
            if (!add) c.removeEntity(e);
        }
        return success;
    }
    
    public boolean addEntity(Entity e) {
        if (e == null) return false;
        if (refreshEntity(e, true)) {
            if (e.moves()) moving_entities.add(e);
            if (e.updates()) active_entities.add(e);
            if (e.renders()) render_entities.add(e);
            System.out.println("Added entity  "+e+"! ("+e.getWorldX()+", "+e.getWorldY());
            return true;
        }
        return false;
    }
    
    public boolean removeEntity(Entity e) {
        if (e == null) return false;
        if (refreshEntity(e, false)) {
            if (e.moves()) moving_entities.remove(e);
            if (e.updates()) active_entities.remove(e);
            if (e.renders()) render_entities.remove(e);
            return true;
        }
        return false;
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
        if (list.get(u).getSectorCoords()[0] == x && list.get(u).getSectorCoords()[1] == y) return list.get(u);
        if (list.get(l).getSectorCoords()[0] == x && list.get(l).getSectorCoords()[1] == y) return list.get(l);
        
        int lsize = (u+1)-l;
        int index = lsize/2 + l;

        if (lsize == 0) return null;
        
        Sector element = list.get(index);
        int cmp = element.compareTo(x, y);
        
        if (cmp == 0) return list.get(index);
        
        int sub_bounds[] = new int[]{cmp > 0 ? l : index, cmp > 0 ? index : u};
        if ((sub_bounds[1]+1)-sub_bounds[0] <= 2) { //if sublist is two in length
            if (cmp > 0) if (sub_bounds[0] > -1) 
                if (list.get(sub_bounds[0]).getSectorCoords()[0] == x && list.get(sub_bounds[0]).getSectorCoords()[1] == y) 
                    return list.get(sub_bounds[0]);
            if (cmp < 0) if (sub_bounds[1] < list.size()) 
                if (list.get(sub_bounds[1]).getSectorCoords()[0] == x && list.get(sub_bounds[1]).getSectorCoords()[1] == y) 
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
        int index = getPotentialSectorIndex(s.getSectorCoords()[0], s.getSectorCoords()[1], l, u);
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
        //drawTerrain(true, g);
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
        int wc[] = getWorldCoords(0, 0);
        int wc2[] = getWorldCoords(Display.getWidth(), Display.getHeight());
        ArrayList<Chunk> chunks = getChunks(wc[0], wc[1], wc2[0]-wc[0], wc2[1]-wc[1]);
        for (Chunk c: chunks) {
            for (int i = 0; i < c.entities.size(); i++) {
                if (i < 0 || i >= c.entities.size()) break;
                Entity e = c.entities.get(i);
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
                    Sector s = new Sector(0, 0);
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
        Sector s = new Sector(x, y);
        addSector(s);
        return s;
    }
    
    public void generate() { generate(32, 1, 0.095, 0.905, 0.1, 0.39); }
    
    /**
     * Generates a large section of terrain.
     * @param size_sectors The width and height in sectors.
     * @param scale The scale of each landmass (input 0-1).
     * @param sea_level The height of the sea. (input 0-1).
     * @param g_height The height of the grass biome (same as sea level input).
     * @param d_height
     * @param t_height 
     */
    public void generate(int size_sectors, double scale, double sea_level, 
            double g_height, double d_height, double t_height) {
        generate(size_sectors, scale, sea_level, g_height, d_height, t_height, 0.058, 0.083, 8, 0.05, false, true);
    }
    
    private void generate(int size_sectors, double scale, double sea_level, 
            double g_height, double d_height, double t_height, 
            double forest_scale, double forest_height, int forest_passes,
            double town_ratio,
            boolean desert_near_tundra, boolean tundra_at_poles) {
        this.biome_map = new short[size_sectors*Sector.sizeChunks()][size_sectors*Sector.sizeChunks()];
        this.forest_map = new boolean[size_sectors*Sector.sizeChunks()][size_sectors*Sector.sizeChunks()];
        this.size_sectors = size_sectors;
        double[][] grass = SimplexNoise.generate(size_sectors*Sector.sizeChunks(), 
                size_sectors*Sector.sizeChunks(), 1/(1000*scale)/2, (1-(sea_level)), 4);
        double[][] tundra = SimplexNoise.generate(size_sectors*Sector.sizeChunks(), 
                size_sectors*Sector.sizeChunks(), 1/(1000*scale)/2, (1-(sea_level)), 4);
        double[][] desert = SimplexNoise.generate(size_sectors*Sector.sizeChunks(), 
                size_sectors*Sector.sizeChunks(), 1/(1000*scale)/2, (1-(sea_level)), 4);
        double[][] forest = SimplexNoise.generate(size_sectors*Sector.sizeChunks(), 
                size_sectors*Sector.sizeChunks(), 1/(1000*forest_scale)/2, (1-(forest_height)), forest_passes);
        
        double[][][] biome_distribution = new double[size_sectors][size_sectors][Chunk.BIOME_COUNT];
        
        //blend all the biomes together and form the biome map and the empty sector map
        for (int i = 0; i < grass.length; i++) {
            for (int j = 0; j < grass.length; j++) {
                double max = MiscMath.max(grass[i][j], 
                        MiscMath.max(tundra[i][j], desert[i][j]));
                //extra rules beforehand
                if (tundra_at_poles) 
                    tundra[i][j] *= ((float)Math.abs((biome_map[0].length/2)-(j+(biome_map.length/5)))/(float)biome_map[0].length);
                if (!desert_near_tundra) desert[i][j] *= 1f-(tundra[i][j]);
                //calculate which biome each chunk should be
                biome_map[i][j] = Chunk.WATER; Color map_color = Color.green;
                if (grass[i][j] > sea_level && max == grass[i][j] && grass[i][j] > 1-g_height)
                    { biome_map[i][j] = Chunk.GRASS_FIELD; map_color = Color.green; }
                if (tundra[i][j] > sea_level && max == tundra[i][j] && tundra[i][j] > 1-t_height)
                    { biome_map[i][j] = Chunk.SNOW; map_color = Color.white; }
                if (desert[i][j] > sea_level && max == desert[i][j] && desert[i][j] > 1-d_height)
                    { biome_map[i][j] = Chunk.SAND; map_color = Color.yellow; }
                if (forest[i][j] <= forest_height && biome_map[i][j] == Chunk.GRASS_FIELD) {
                    { forest_map[i][j] = true; map_color = Color.green.darker(); }
                }
                int sx = i/Sector.sizeChunks();
                int sy = j/Sector.sizeChunks();
                biome_distribution[sx][sy][biome_map[i][j]]++; //tally the selected biome
            }
        }
        
        //create the empty sector map from the biome_distribution map
        //create the town map from the empty sector map
        //create the road map from the town map and empty sector map
        boolean[][] empty_sector_map = createEmptySectorMap(biome_distribution);
        boolean[][] town_map = createTownMap(empty_sector_map, town_ratio);
        road_map = createRoadMap(empty_sector_map, town_map);
    }
    
    private boolean[][] createEmptySectorMap(double biome_distribution[][][]) {
        boolean map[][] = new boolean[size_sectors][size_sectors];
        for (int i = 0; i < size_sectors; i++) {
            for (int j = 0; j < size_sectors; j++) {
                for (int b = 0; b < Chunk.BIOME_COUNT; b++) {
                    if (biome_distribution[i][j][b] == 0) continue;
                    if (biome_distribution[i][j][b] == Sector.sizeChunks()*Sector.sizeChunks()) { 
                        int[] wcm = getWorldCoordsFromMap(i*Sector.sizeChunks(), j*Sector.sizeChunks());
                        int sc[] = getSectorCoords(wcm[0], wcm[1]);
                        System.out.println("Sector @ map["+i+", "+j+"] is empty!");
                        map[i][j] = true;
                        //if (b != Chunk.WATER) available_town_sectors.add(new int[]{i, j}); 
                        break; 
                    }
                    if (biome_distribution[i][j][b] != Sector.sizeChunks()*Sector.sizeChunks()) break;
                }
            }
        }
        return map;
    }
    
    private boolean[][] createTownMap(boolean empty_sector_map[][], double ratio) {
        boolean map[][] = new boolean[size_sectors][size_sectors];
        //compile a list of all valid town locations in the map
        ArrayList<int[]> valid_locations = new ArrayList<int[]>();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map.length; j++) {
                for (int b = 0; b < Chunk.BIOME_COUNT; b++) {
                    if (empty_sector_map[i][j]) { //if sector is only one biome
                        if (biome_map[i*Sector.sizeChunks()][j*Sector.sizeChunks()] != Chunk.WATER) {
                            valid_locations.add(new int[]{i, j});
                        }
                    }
                }
            }
        }
        
        //use the town ratio to determine the number of towns
        int tcount = (int)(ratio*(float)valid_locations.size());
        spawn = new int[]{-1, -1};
        boolean chosen_spawn = false;
        while (tcount > 0) {
            if (valid_locations.isEmpty()) break;
            int random = Math.abs(rng.nextInt()) % valid_locations.size();
            int[] chosen = valid_locations.get(random);
            if (!map[chosen[0]][chosen[1]]) {
                //mark the town map
                map[chosen[0]][chosen[1]] = true;
                int[] wcm = getWorldCoordsFromMap(chosen[0]*Sector.sizeChunks(), chosen[1]*Sector.sizeChunks());
                int sc[] = getSectorCoords(wcm[0], wcm[1]);
                //create a new town instance
                Town t = new Town(sc[0], sc[1]);
                towns.add(t);
                System.out.println("Sector "+sc[0]+", "+sc[1]+"(mc "+chosen[0]+", "+chosen[1]+") is a town!");
                valid_locations.remove(chosen);
                if (!chosen_spawn && chosen[0] > 0) { //place the starting sector next to the first town
                    if (empty_sector_map[chosen[0]][chosen[1]]) {
                        //check if the biome is grass
                        if (biome_map[(chosen[0]-1)*Sector.sizeChunks()][chosen[1]*Sector.sizeChunks()] == Chunk.GRASS_FIELD) {
                            spawn = new int[]{sc[0]-1, sc[1]};
                            chosen_spawn = true;
                            System.out.println("Sector "+spawn[0]+", "+spawn[1]+" is the spawn region!");
                        }
                    }
                }          
                tcount--;
            }
        }
        return map;
    }
    
    private boolean[][] createRoadMap(boolean empty_sector_map[][], boolean[][] town_map) {
        boolean map[][] = new boolean[size_sectors*Sector.sizeChunks()][size_sectors*Sector.sizeChunks()];
        //now place roads between the towns
        for (int i = 0; i < town_map.length; i++) {
            for (int j = 0; j < town_map.length; j++) {
                int x = i*Sector.sizeChunks(), y = j*Sector.sizeChunks();
                if (!town_map[i][j]) continue;
                int ox = 0, oy = 0; 
                boolean up = true, down = true, left = true, right = true;
                while (true) { 
                    if (i-ox <= -1) left = false;
                    if (i+ox >= empty_sector_map.length) right = false;
                    if (j+oy >= empty_sector_map.length) down = false;
                    if (j-oy <= -1) up = false;
                    
                    if (right) {
                        if (empty_sector_map[i+ox][j]) {
                            placeRoadSegment(map, x+(ox*Sector.sizeChunks()), y, 1);
                            placeRoadSegment(map, x+(ox*Sector.sizeChunks()), y+Sector.sizeChunks(), 1);
                        } else { right = false; }
                    }
                    if (left) { 
                        if (empty_sector_map[i-ox][j]) {
                            placeRoadSegment(map, x-(ox*Sector.sizeChunks()), y, 3);
                            placeRoadSegment(map, x-(ox*Sector.sizeChunks()), y+Sector.sizeChunks(), 3);
                        } else { left = false; }
                    }
                    if (down) { 
                        if (empty_sector_map[i][j+oy]) {
                            placeRoadSegment(map, x, y+(oy*Sector.sizeChunks()), 2);
                            placeRoadSegment(map, x+Sector.sizeChunks(), y+(oy*Sector.sizeChunks()), 2);
                        } else { down = false; }
                    }
                    if (up) { 
                        if (empty_sector_map[i][j-oy]) {
                            placeRoadSegment(map, x, y-(oy*Sector.sizeChunks()), 2);
                            placeRoadSegment(map, x+Sector.sizeChunks(), y-(oy*Sector.sizeChunks()), 2);
                        } else { up = false; }
                    }
                    ox++; oy++;
                    if ((left || right || up || down) == false) break;
                }
            }
        }
        return map;
    }
    
    /**
     * Creates a proper road segment on the road map. Two tiles wide and of the specified length.
     * Facing upwards (dir = 0), the origin road tile is at map[x, y] and the second road tile is to the left of it.
     * @param x The x coordinate on the road map.
     * @param y The y coordinate. 
     * @param length The length of the road segment.
     * @param dir The direction of the road segment, 0-3.
     */
    private void placeRoadSegment(boolean map[][], int x, int y, int dir) {
        int ox = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int oy = dir == 1 ? -1 : (dir == 3 ? 1 : 0);
        int incr_y = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int incr_x = dir == 1 ? 1 : (dir == 3 ? -1 : 0);
        int rot1 = dir, rot2 = dir+2;
        for (int i = 0; i <= Sector.sizeChunks(); i++) {
            
            if (x > -1 && x < map.length 
                    && y > -1 && y < map[0].length) map[x][y] = true;
            if (x+ox > -1 && x+ox < map.length 
                    && y+oy > -1 && y+oy < map[0].length) map[x+ox][y+oy] = true;
            
            x+=incr_x;
            y+=incr_y;
        }
    }
    
    public int[] getSpawn() { 
        return spawn == null ? 
            new int[]{0, 0} : 
            new int[]{spawn[0]*Sector.sizePixels(), spawn[1]*Sector.sizePixels()}; 
    }
    
    /**
     * Generates around the specified world coordinates.
     * @param world_x World x.
     * @param world_y World y.
     */
    public void generateAround(int world_x, int world_y) {
        int sc[] = getSectorCoords(world_x, world_y);
        initializeSectors(sc[0], sc[1], 2);
    }
    
    /**
     * Initializes the sectors in a radius around the specified sector coordinate.
     * Imports terrain data for each from the maps in World. Once they are created and initialized,
     * they can be saved and loaded as usual.
     * @param sector_x
     * @param sector_y
     * @param r 
     */
    private void initializeSectors(int sector_x, int sector_y, int r) {
        for (int h = -r; h != r+1; h++) {
            for (int w = -r; w != r+1; w++) {
                Sector s = getSector(sector_x+w, sector_y+h);
                if (s == null) {
                    if (validSector(sector_x+w, sector_y+h)) {
                        s = createSector(sector_x+w, sector_y+h);
                        s.importBiomes(biome_map);
                        s.importRoads(road_map);
                        Town t = getTown(sector_x+w, sector_y+h);
                        if (t != null) t.generate();
                        s.importForest(forest_map);
                    }
                }
            }
        }
    }
    
    private boolean validSector(int x, int y) {
        x += size_sectors/2; y += size_sectors/2;
        return x >= 0 && x < size_sectors
                && y >= 0 && y < size_sectors;
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
    
    public int[] getMapCoords(int sector_x, int sector_y, int chunk_x, int chunk_y) {
        int[] origin = {Sector.sizeChunks()*size_sectors/2, Sector.sizeChunks()*size_sectors/2};
        return new int[]{origin[0]+(Sector.sizeChunks()*sector_x)+chunk_x,
            origin[1]+(Sector.sizeChunks()*sector_y)+chunk_y};
    }
    
    public int[] getWorldCoordsFromMap(int x, int y) {
        int[] c = new int[]{(x-(Sector.sizeChunks()*size_sectors/2))*Chunk.sizePixels(),
            (y-(Sector.sizeChunks()*size_sectors/2))*Chunk.sizePixels()};
        return c;
    }
}
