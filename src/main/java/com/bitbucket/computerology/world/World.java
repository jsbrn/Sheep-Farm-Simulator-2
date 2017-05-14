package com.bitbucket.computerology.world;

import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SimplexNoise;
import com.bitbucket.computerology.world.entities.Blueprint;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.components.TownBuilding;
import com.bitbucket.computerology.world.events.EventHandler;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;
import com.bitbucket.computerology.world.towns.Town;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class World {

    private static World world;

    //biome_map is a 2D array that contains terrain data for every chunk
    private byte[][] biome_map;
    private int[] spawn;
    //forest_map and road_map keep track of every chunk that is a forest or road
    //empty_sector_map and town_map keep track of every sector that is empty or a town
    private boolean[][] forest_map, road_map, empty_sector_map, town_map;

    private int size_sectors;
    private ArrayList<Sector> sectors;
    private Random rng;
    private int seed;

    private Player player;

    private ArrayList<EventHandler> event_handlers;
    private double time;

    private ArrayList<Entity> active_entities, moving_entities, render_entities;
    private ArrayList<Town> towns;

    private Image map_texture;
    private ArrayList<int[]> queued_map_updates;

    private String save_name;

    private World(int seed) {
        this.seed = seed;
        init();
    }

    private World() {
        this.seed = Math.abs(new Random().nextInt());
        init();
    }

    public static World getWorld() {
        return world;
    }

    public static void newWorld(String save_name) {
        world = new World();
        world.save_name = save_name;
    }

    public void save() {
        File f = new File(Assets.ROOT_DIR+"/saves/" +world.save_name+ "/world.txt");
        FileWriter fw;
        System.out.println("Saving to file " + f.getAbsoluteFile().getAbsolutePath());
        try {
            if (!f.exists()) f.createNewFile();
            fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("t=" + (int) world.time + "\n");
            bw.write("cx=" + (int) Camera.getX() + "\n");
            bw.write("cy=" + (int) Camera.getY() + "\n");
            bw.write("cz=" + (int) Camera.getZoom() + "\n");
            bw.write("sx="+spawn[0]+"\n"); //spawn
            bw.write("sy="+spawn[1]+"\n");
            bw.write("size="+size_sectors+"\n");

            world.player.save(bw);
            ArrayList<Entity> saved = new ArrayList<Entity>(); //TODO: when entity rendering is finished, make a master list of entities?
            for (Sector s : world.sectors) {
                s.save(bw);
                for (Entity e : s.getEntities()) {
                    if (!saved.contains(e)) {
                        saved.add(e); //avoid saving an entity twice
                        e.save(bw);
                    }
                }
            }

            for (Town t: towns) t.save(bw);

            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void saveTerrainData() {
        File dir = new File(Assets.ROOT_DIR+"/saves/" +world.save_name+ "/terrain");
        dir.mkdir();
        for (int x = 0; x < size_sectors; x++) {
            for (int y = 0; y < size_sectors; y++) {
                dir = new File(Assets.ROOT_DIR+"/saves/" +world.save_name+ "/terrain/"+x+" "+y);
                dir.mkdir();
                File f = new File(Assets.ROOT_DIR+"/saves/" +world.save_name+ "/terrain/"+x+" "+y+"/terrain.txt");
                FileWriter fw;
                try {
                    if (!f.exists()) f.createNewFile();
                    fw = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write("e="+empty_sector_map[x][y]+"\n");
                    bw.write("t="+town_map[x][y]+"\n");

                    int a = (x*Sector.sizeChunks()), b = (y*Sector.sizeChunks());

                    for (int i = a; i < a + Sector.sizeChunks(); i++) {
                        for (int j = b; j < b + Sector.sizeChunks(); j++) {
                            String chunk_data = (biome_map[i][j]+"_"
                                    +road_map[i][j]
                                    +forest_map[i][j]).replace("true", "1").replace("false", "0").trim();
                            bw.write(chunk_data+" ");
                        }
                        bw.write("\n");
                    }

                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Sets the world instance to null.
     */
    public void destroy() {
        world = null;
    }

    public void load() {
        File f = new File(Assets.ROOT_DIR+"/saves/" +world.save_name + "/world.txt");
        if (!f.exists()) return;
        FileReader fr;
        System.out.println("Loading from file: " + f.getAbsoluteFile().getAbsolutePath());
        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.indexOf("t=") == 0) world.time = Double.parseDouble(line.replace("t=", ""));
                if (line.indexOf("cx=") == 0) Camera.setX(Integer.parseInt(line.replace("cx=", "").trim()));
                if (line.indexOf("cy=") == 0) Camera.setY(Integer.parseInt(line.replace("cy=", "").trim()));
                if (line.indexOf("cz=") == 0) Camera.setZoom(Integer.parseInt(line.replace("cz=", "").trim()));
                if (line.indexOf("sx=") == 0) spawn[0] = Integer.parseInt(line.substring(3, line.length()));
                if (line.indexOf("sy=") == 0) spawn[1] = Integer.parseInt(line.substring(3, line.length()));
                if (line.indexOf("size=") == 0) {
                    size_sectors = Integer.parseInt(line.substring(5, line.length()));
                    this.forest_map = new boolean[size_sectors*Sector.sizeChunks()][size_sectors*Sector.sizeChunks()];
                    this.empty_sector_map = new boolean[size_sectors][size_sectors];
                    this.road_map = new boolean[size_sectors*Sector.sizeChunks()][size_sectors*Sector.sizeChunks()];
                    this.town_map = new boolean[size_sectors][size_sectors];
                    this.biome_map = new byte[size_sectors*Sector.sizeChunks()][size_sectors*Sector.sizeChunks()];
                }

                if (line.equals("s")) {
                    Sector s = new Sector(0, 0);
                    if (s.load(br)) world.addSector(s);
                }
                if (line.equals("t")) {
                    Town t = new Town(0, 0);
                    if (t.load(br)) towns.add(t);
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
     * Loads terrain data from a file for the given sector into the map arrays.
     * Needs to be called before trying to access terrain data for sectors that have not been generated,
     * or before trying to generate an ungenerated sector.
     * Fails gracefully if the data has already been loaded, or if invalid parameters are given.
     * @param sx The sector's x coordinate.
     * @param sy The sector's y coordinate.
     * @return True if a load was performed, false otherwise.
     */
    public boolean loadTerrainData(int sx, int sy) {

        int[] mc = MiscMath.getMapCoords(sx, sy, 0, 0);
        int[] msc = new int[]{mc[0]/Sector.sizeChunks(), mc[1]/Sector.sizeChunks()};
        if (!validSector(sx, sy)) return false;
        if (biome_map[mc[0]][mc[1]] != Chunk.NULL) return false;

        File f = new File(Assets.ROOT_DIR+"/saves/" +world.save_name
                + "/terrain/"+msc[0]+" "+msc[1]+"/terrain.txt");
        if (!f.exists()) return false;
        FileReader fr;
        System.out.println("Loading from file: " + f.getAbsoluteFile().getAbsolutePath());
        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            int i = 0, j = 0;

            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.indexOf("t=") == 0) {
                    town_map[msc[0]][msc[1]] = Boolean.parseBoolean(line.substring(2, line.length()));
                    continue;
                }
                if (line.indexOf("e=") == 0) {
                    empty_sector_map[msc[0]][msc[1]] = Boolean.parseBoolean(line.substring(2, line.length()));
                    continue;
                }
                j = 0;
                String row[] = line.trim().split("\\s");
                for (String chunk: row) {
                    String data[] = chunk.split("[_]");
                    biome_map[mc[0]+i][mc[1]+j] = Byte.parseByte(data[0]);
                    road_map[mc[0]+i][mc[1]+j] = data[1].charAt(0) == '1';
                    forest_map[mc[0]+i][mc[1]+j] = data[1].charAt(1) == '1';
                    j++;
                }
                i++;
            }
            br.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void init() {
        this.rng = new Random(seed);
        this.sectors = new ArrayList<Sector>();
        this.sectors.ensureCapacity(4096); //preallocate array to 5000 sectors
        this.player = new Player();
        this.spawn = new int[]{0, 0};
        this.time = 0;
        this.event_handlers = new ArrayList<EventHandler>();
        this.event_handlers.add(new EventHandler()); //test handler just to make sure it works
        this.active_entities = new ArrayList<Entity>();
        this.active_entities.ensureCapacity(500);
        this.moving_entities = new ArrayList<Entity>();
        this.moving_entities.ensureCapacity(500);
        this.render_entities = new ArrayList<Entity>();
        this.render_entities.ensureCapacity(500);
        this.towns = new ArrayList<Town>();
        this.queued_map_updates = new ArrayList<int[]>();

    }

    public int townCount() { return towns.size(); }

    public Town getTown(int i) { if (i < 0 || i >= towns.size()) return null; return towns.get(i); }

    public Town getTown(int sector_x, int sector_y) {
        for (Town t : towns) {
            int[] sc = t.getSectorCoordinates();
            if (sc[0] == sector_x && sc[1] == sector_y) return t;
        }
        return null;
    }

    public int movableEntityCount() {
        return moving_entities.size();
    }

    public int activeEntityCount() {
        return active_entities.size();
    }

    public int size() { return size_sectors; }

    public void placeBlueprint(String name, int world_x, int world_y, boolean clear_area, byte biome_fill, byte terrain_fill) {
        Blueprint b = Assets.getBlueprint(name);
        ArrayList<Chunk> cleared = new ArrayList<Chunk>();
        if (b == null) return;
        for (int i = 0; i < b.entityCount(); i++) {
            Entity e = Entity.create(b.getEntityType(i));
            int[] offset = b.getEntityOffset(i);
            int cell_size = Chunk.sizePixels()/8; //each chunk can be broken down 8x from build mode
            if (e != null) {
                e.setWorldX(world_x + (offset[0]*cell_size));
                e.setWorldY(world_y + (offset[1]*cell_size));

                ArrayList<Chunk> chunks = getChunks(e.getWorldX() - (e.getWidth() / 2), e.getWorldY() - (e.getHeight() / 2),
                        e.getWidth(), e.getHeight());
                for (Chunk c : chunks) {
                    if (biome_fill != Chunk.NULL) c.setBiome(biome_fill);
                    if (terrain_fill != Chunk.NULL) c.setTerrain(terrain_fill);
                    if (cleared.contains(c) || !clear_area) continue;
                    for (int j = c.getEntities().size() - 1; j > -1; j--) removeEntity(c.getEntities().get(j));
                    cleared.add(c);
                }
                addEntity(e);
            }
        }
    }

    public void setTerrain(int wx, int wy, byte terrain, int rot) {
        Chunk c = getChunk(wx, wy);
        if (c != null) {
            c.setRotation(rot);
            c.setTerrain(terrain);
            //System.to.println("Set chunk at "+wx+", "+wy+" to t="+terrain+", r="+rot);
        }
    }

    public int getTerrain(int wx, int wy) {
        Chunk c = getChunk(wx, wy);
        if (c != null) {
            return c.getTerrain();
        }
        return -1;
    }

    /**
     * Gets all of the chunks intersecting the specified rectangle.
     *
     * @param x World coordinates.
     * @param y World coordinates.
     * @param w Width.
     * @param h Height.
     * @return An ArrayList<Chunk>.
     */
    public ArrayList<Chunk> getChunks(double x, double y, int w, int h) {
        ArrayList<Chunk> list = new ArrayList<Chunk>();
        if (w <= 0 || h <= 0) return list;
        int max_w = w < Chunk.sizePixels() ? w : w + Chunk.sizePixels();
        int max_h = h < Chunk.sizePixels() ? h : h + Chunk.sizePixels();
        int i_inc = w < Chunk.sizePixels() ? w : Chunk.sizePixels();
        int j_inc = h < Chunk.sizePixels() ? h : Chunk.sizePixels();
        for (int i = 0; i <= max_w; i += i_inc) {
            for (int j = 0; j <= max_h; j += j_inc) {
                int sc[] = MiscMath.getSectorCoords(x + i, y + j);
                int cc[] = MiscMath.getChunkCoords(x + i, y + j);
                Sector s = getSector(sc[0], sc[1]);
                Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
                if (c != null && !list.contains(c)
                        && c.intersects(x, y, w, h)) list.add(c);
            }
        }
        return list;
    }

    public Chunk getChunk(int wx, int wy) {
        int[] sc = MiscMath.getSectorCoords(wx, wy);
        int[] cc = MiscMath.getChunkCoords(wx, wy);
        Sector s = getSector(sc[0], sc[1]);
        return s == null ? null : s.getChunk(cc[0], cc[1]);
    }

    public Entity getEntity(double x, double y) {
        int sc[] = MiscMath.getSectorCoords(x, y);
        int cc[] = MiscMath.getChunkCoords(x, y);
        Sector s = getSector(sc[0], sc[1]);
        Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
        if (c != null) {
            for (int i = c.getEntities().size() - 1; i > -1; i--) {
                if (c.getEntities().get(i).intersects(x, y))
                    return c.getEntities().get(i);
            }
        }
        return null;
    }

    public ArrayList<Entity> getEntities(double x, double y, int w, int h) {
        ArrayList<Entity> list = new ArrayList<Entity>();
        ArrayList<Chunk> chunks = getChunks(x, y, w, h);
        for (Chunk c : chunks) {
            for (Entity e : c.getEntities()) {
                if (!list.contains(e) && e.intersects(x, y, w, h)) list.add(e);
            }
        }
        return list;
    }

    public boolean refreshEntity(Entity e, boolean add) {
        int sc[] = MiscMath.getSectorCoords(e.getWorldX(), e.getWorldY());
        Sector s = getSector(sc[0], sc[1]);
        if (s != null) {
            if (add) s.addEntity(e);
            else s.removeEntity(e);
        }
        ArrayList<Chunk> chunks = getChunks(e.getWorldX() - e.getWidth() / 2,
                e.getWorldY() - e.getHeight() / 2, e.getWidth(), e.getHeight());
        boolean success = true;
        for (Chunk c : chunks) {
            if (add) {
                if (!c.addEntity(e)) success = false;
            }
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
            int[] mc = MiscMath.getMapCoords(e.getWorldX() - (e.getWidth() / 2), e.getWorldY() - (e.getHeight() / 2));
            updateMapTexture(mc[0], mc[1],
                    1 + (e.getWidth() / Chunk.sizePixels()), 1 + (e.getHeight() / Chunk.sizePixels()));
            System.out.println("Added entity  " + e + "! (" + e.getWorldX() + ", " + e.getWorldY());
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
            int[] mc = MiscMath.getMapCoords(e.getWorldX() - (e.getWidth() / 2), e.getWorldY() - (e.getHeight() / 2));
            updateMapTexture(mc[0], mc[1],
                    1 + (e.getWidth() / Chunk.sizePixels()), 1 + (e.getHeight() / Chunk.sizePixels()));
            return true;
        }
        return false;
    }

    public final void update() {
        for (EventHandler e : event_handlers) {
            e.update();
        }

        for (int i = 0; i < active_entities.size(); i++) {
            if (i >= active_entities.size() || i < 0) break;
            active_entities.get(i).update();
        }
        for (int i = 0; i < moving_entities.size(); i++) {
            if (i >= moving_entities.size() || i < 0) break;
            moving_entities.get(i).move();
        }

        time += MiscMath.get24HourConstant(1, 1);
    }

    /**
     * Returns the current time since world creation (from minutes).
     **/
    public int getTime() {
        return (int) time;
    }

    /**
     * Returns the hour from 24-hour format
     **/
    public int getHour() {
        return (int) (time % 1440) / 60;
    }

    public int sectorCount() {
        return sectors.size();
    }

    /**
     * Find the sector with the offset value specified.
     *
     * @param x The offset (from sectors) from the origin.
     * @param y The offset (from sectors) from the origin.
     * @return A Sector instance, or null if not found.
     */
    public Sector getSector(int x, int y) {
        Sector s = getSector(x, y, 0, sectors.size() - 1, sectors);
        if (s == null) {
            s = getSector(x, y, 0, sectors.size() - 1, sectors);
        }
        return s;
    }

    /**
     * Gets the sector at sector coords (x, y). Uses a binary search algorithm.
     *
     * @param x    Sector coordinate.
     * @param y    Sector coordinate.
     * @param l    The lower bound of the list to search (start with 0).
     * @param u    The upper bound of the list to search (start with list.size - 1).
     * @param list The list to search.
     * @return A Sector instance, or null if none found at (x, y).
     */
    private Sector getSector(int x, int y, int l, int u, ArrayList<Sector> list) {
        if (list.isEmpty()) return null;
        //if the sector is beyond the first and last, return null
        //if the sector is the first or last, return the first or last, respectively
        if (list.get(0).compareTo(x, y) > 0 || list.get(list.size() - 1).compareTo(x, y) < 0) return null;
        if (list.get(u).getSectorCoords()[0] == x && list.get(u).getSectorCoords()[1] == y) return list.get(u);
        if (list.get(l).getSectorCoords()[0] == x && list.get(l).getSectorCoords()[1] == y) return list.get(l);

        int lsize = (u + 1) - l;
        int index = lsize / 2 + l;

        if (lsize == 0) return null;

        Sector element = list.get(index);
        int cmp = element.compareTo(x, y);

        if (cmp == 0) return list.get(index);

        int sub_bounds[] = new int[]{cmp > 0 ? l : index, cmp > 0 ? index : u};
        if ((sub_bounds[1] + 1) - sub_bounds[0] <= 2) { //if sublist is two from length
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
     *
     * @param s The sector to add.
     * @return A boolean indicating the success of the operation.
     */
    public boolean addSector(Sector s) {
        return addSector(s, 0, sectors.size() - 1);
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
     * to keep the list sorted. If the sector is found to already exist from the list,
     * -1 is returned. Uses a binary search algorithm.
     *
     * @param l Lower bound of the search region (when calling first, use 0)
     * @param u Upper bound of the search region (when calling first, use size()-1)
     * @return An integer of the above specifications.
     */
    private int getPotentialSectorIndex(int x, int y, int l, int u) {
        //if the bounds are the number, then return the bound

        if (sectors.isEmpty()) return 0;
        if (sectors.get(0).compareTo(x, y) > 0) return 0;
        if (sectors.get(sectors.size() - 1).compareTo(x, y) < 0) return sectors.size();

        int lsize = (u + 1) - l;
        int index = lsize / 2 + l;

        if (lsize == 0) return -1;

        Sector element = sectors.get(index);
        int cmp = element.compareTo(x, y);

        if (cmp == 0) return -1;

        int sub_bounds[] = new int[]{cmp > 0 ? l : index, cmp > 0 ? index : u};
        if ((sub_bounds[1] + 1) - sub_bounds[0] <= 2) { //if sublist is two from length
            if (sectors.get(sub_bounds[0]).compareTo(x, y) < 0
                    && sectors.get(sub_bounds[1]).compareTo(x, y) > 0) return sub_bounds[0] + 1;
            return -1;
        } else {
            return getPotentialSectorIndex(x, y, sub_bounds[0], sub_bounds[1]);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void draw(Graphics g) {
        drawTerrain(false, g);
        drawTerrain(true, g);
        drawEntities(g);
        applyMapChanges();
    }

    private void drawTerrain(boolean corners, Graphics g) {
        if (Assets.getTerrainSprite(corners) == null) return;
        int x, y, w = Display.getWidth() + (2 * Chunk.onScreenSize()),
                h = Display.getHeight() + (2 * Chunk.onScreenSize());
        Assets.getTerrainSprite(corners).startUse();
        for (x = 0; x < w; x += Chunk.onScreenSize()) {
            for (y = 0; y < h; y += Chunk.onScreenSize()) {
                double wc[] = MiscMath.getWorldCoords(x, y);
                int sc[] = MiscMath.getSectorCoords(wc[0], wc[1]);
                int cc[] = MiscMath.getChunkCoords(wc[0], wc[1]);
                Sector s = getSector(sc[0], sc[1]);
                Chunk c = (s != null) ? s.getChunk(cc[0], cc[1]) : null;
                if (c != null) {
                    c.draw(corners, g);
                }
            }
        }
        Assets.getTerrainSprite(corners).endUse();
    }

    /**
     * Loops through all entities from the world and draws the ones that are visible.
     * In the future it will not loop through ALL entities.
     */
    private void drawEntities(Graphics g) {
        double wc[] = MiscMath.getWorldCoords(0, 0);
        double wc2[] = MiscMath.getWorldCoords(Display.getWidth(), Display.getHeight());
        ArrayList<Chunk> chunks = getChunks((int)wc[0], (int)wc[1], (int)(wc2[0] - wc[0]), (int)(wc2[1] - wc[1]));
        for (Chunk c : chunks) {
            for (int i = 0; i < c.entities.size(); i++) {
                if (i < 0 || i >= c.entities.size()) break;
                Entity e = c.entities.get(i);
                e.draw(g);
            }
        }
    }

    /*
     * Must be called from a render thread (requires OpenGL context).
     */
    private void applyMapChanges() {
        try {
            if (map_texture == null) return;
            Graphics g = map_texture.getGraphics();
            for (int[] mc : queued_map_updates) {
                int wc[] = MiscMath.getWorldCoordsFromMap(mc[0], mc[1]);
                int t = getTerrain(wc[0] + (Chunk.sizePixels() / 2), wc[1] + Chunk.sizePixels() / 2); //TODO change to getTopLayer
                if (t > -1 && t < Chunk.BIOME_COUNT) g.setColor(Chunk.COLORS[t]);
                ArrayList<Entity> es = World.getWorld().getEntities(wc[0], wc[1], Chunk.sizePixels(), Chunk.sizePixels());
                if (!es.isEmpty()) {
                    Entity e = es.get(0);
                    if (e.getTexture() != null) {
                        if (e.getTexture().getAverage() != null)
                            g.setColor(e.getTexture().getAverage());
                    }
                }

                g.fillRect(mc[0], mc[1], 1, 1);
            }
            queued_map_updates.clear();
        } catch (SlickException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void buildMapTexture() {
        if (biome_map == null) return;
        Graphics g = null;
        try {
            map_texture = new Image(biome_map.length, biome_map.length);
            g = map_texture.getGraphics();
        } catch (SlickException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (g == null) return;
        System.out.println("Building world map...this could take awhile.");
        for (int i = 0; i < biome_map.length; i++) {
            for (int j = 0; j < biome_map[0].length; j++) {
                g.setColor(Chunk.COLORS[biome_map[i][j]]);
                if (forest_map[i][j]) g.setColor(Color.green.darker());
                if (road_map[i][j]) g.setColor(Color.gray.darker());
                g.fillRect(i, j, 1, 1);
            }
        }
    }

    public void updateMapTexture(int map_x, int map_y, int w, int h) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                queued_map_updates.add(new int[]{map_x + w, map_y + h});
            }
        }
    }

    public Image getMapTexture() {
        return map_texture;
    }

    /**
     * Creates a new sector at x, y. Adds it to the list of active (loaded) sectors.
     *
     * @param x Sector coordinate x
     * @param y Sector coordinate y
     */
    public Sector createSector(int x, int y) {
        Sector s = new Sector(x, y);
        addSector(s);
        return s;
    }

    public boolean generate() {
        double[] gen_settings = loadGeneratorSettings();
        if (!generate((int)gen_settings[0], 1, gen_settings[2], gen_settings[3], gen_settings[4]
        , 0.02, 0.04, 8, 0.03)) return false;
        generateTradeRoutes();
        generateAround(getSpawn()[0], getSpawn()[1]);
        saveTerrainData();
        placeBlueprint("Starter Farm", getSpawn()[0], getSpawn()[1], true, Chunk.NULL, Chunk.NULL);
        return true;
    }

    /**
     * When the user creates a new world, a generator_settings.txt file is created that keeps the
     * specified terrain settings until the world is generated. This method reads from the file and
     * returns an array containing all the values. Then, it deletes the file.
     * @return The terrain settings from the file.
     */
    private double[] loadGeneratorSettings() {
        double[] settings = new double[6];
        File f = new File(Assets.ROOT_DIR+"/saves/" +world.save_name + "/generator_settings.txt");
        if (!f.exists()) return settings;
        FileReader fr;
        try {
            fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.indexOf("size=") == 0) settings[0] = Double.parseDouble(line.replace("size=", ""));

                if (line.indexOf("grass=") == 0) settings[2] = Double.parseDouble(line.replace("grass=", ""));
                if (line.indexOf("desert=") == 0) settings[3] = Double.parseDouble(line.replace("desert=", ""));
                if (line.indexOf("tundra=") == 0) settings[4] = Double.parseDouble(line.replace("tundra=", ""));
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            f.delete();
        }
        return settings;
    }

    /**
     * Checks whether world.txt exists from the save folder. If it does, then the world is generated.
     * World.txt contains the data for all entities, loaded sectors, and also the terrain data of unloaded sectors as well
     * @return
     */
    public boolean isGenerated() {
        return new File(Assets.ROOT_DIR+"/saves/" +world.save_name+"/world.txt").exists();
    }

    /**
     * Generates (for a square world) the terrain data, the empty sector map, the town map,
     * the road map, and finds a spawn location for the player. It should be noted that the biome parameters are
     * based on percentage and are closely related. Setting all biomes to max will fill the world with equal amounts of each.
     * Oceans and water bodies are put from where the algorithm decides no biome will be set.
     *
     * All parameters aside from the world size take a double value from the range of [0-1].
     *
     * @param size_sectors The width and height of the world (square).
     * @param scale The scale of the terrain (0-1). Higher = "zoomed from" terrain shapes.
     * @param g_height The percentage of grass terrain.
     * @param d_height The percentage of desert terrain.
     * @param t_height The percentage of tundra terrain.
     * @param forest_scale The scale of the forest placements. Higher = "zoomed from", denser forests.
     * @param forest_height The percentage of land covered by forests.
     * @param forest_passes The shape of the forests. Lower = smooth, round. Higher = rough, scattered.
     * @param town_ratio The percentage of empty sectors to be marked as town sectors.
     * @return A boolean indicating the success of the operation (that is, if a spawn point was found afterwards).
     */
    private boolean generate(int size_sectors, double scale,
                             double g_height, double d_height, double t_height,
                             double forest_scale, double forest_height, int forest_passes,
                             double town_ratio) {
        System.out.println("Generating world of size "+size_sectors+"x"+size_sectors);
        biome_map = new byte[size_sectors * Sector.sizeChunks()][size_sectors * Sector.sizeChunks()];
        forest_map = new boolean[size_sectors * Sector.sizeChunks()][size_sectors * Sector.sizeChunks()];
        this.size_sectors = size_sectors;
        double[][] grass = SimplexNoise.generate(size_sectors * Sector.sizeChunks(),
                size_sectors * Sector.sizeChunks(), 1 / (1000 * scale) / 3, 1, 4);
        double[][] tundra = SimplexNoise.generate(size_sectors * Sector.sizeChunks(),
                size_sectors * Sector.sizeChunks(), 1 / (1000 * scale) / 3, 1, 4);
        double[][] desert = SimplexNoise.generate(size_sectors * Sector.sizeChunks(),
                size_sectors * Sector.sizeChunks(), 1 / (1000 * scale) / 3, 1, 4);
        double[][] forest = SimplexNoise.generate(size_sectors * Sector.sizeChunks(),
                size_sectors * Sector.sizeChunks(), 1 / (1000 * forest_scale) / 3, (1 - (forest_height)), forest_passes);

        double[][][] biome_distribution = new double[size_sectors][size_sectors][Chunk.BIOME_COUNT];

        //blend all the biomes together and form the biome map and the empty sector map
        for (int i = 0; i < grass.length; i++) {
            for (int j = 0; j < grass.length; j++) {
                double max = MiscMath.max(grass[i][j],
                        MiscMath.max(tundra[i][j], desert[i][j]));
                //calculate which terrain each chunk should be
                biome_map[i][j] = Chunk.WATER;
                if (max == grass[i][j] && grass[i][j] > 1 - g_height) {
                    biome_map[i][j] = Chunk.GRASS;
                }
                if (max == tundra[i][j] && tundra[i][j] > 1 - t_height) {
                    biome_map[i][j] = Chunk.SNOW;
                }
                if (max == desert[i][j] && desert[i][j] > 1 - d_height) {
                    biome_map[i][j] = Chunk.SAND;
                }
                if (forest[i][j] <= forest_height &&
                        (biome_map[i][j] == Chunk.GRASS || biome_map[i][j] == Chunk.SNOW
                        || biome_map[i][j] == Chunk.SAND)) {
                    forest_map[i][j] = true;
                }
                int sx = i / Sector.sizeChunks();
                int sy = j / Sector.sizeChunks();
                biome_distribution[sx][sy][biome_map[i][j]]++; //tally the selected biome
            }
        }

        //create the empty sector map from the biome_distribution map
        //create the town map from the empty sector map
        //create the road map from the town map and empty sector map
        createEmptySectorMap(biome_distribution);
        createTownMap(town_ratio);
        createRoadMap();
        return findSpawn();
    }

    private void createEmptySectorMap(double biome_distribution[][][]) {
        empty_sector_map = new boolean[size_sectors][size_sectors];
        for (int i = 0; i < size_sectors; i++) {
            for (int j = 0; j < size_sectors; j++) {
                for (int b = 0; b < Chunk.BIOME_COUNT; b++) {
                    if (biome_distribution[i][j][b] == 0) continue;
                    if (biome_distribution[i][j][b] == Sector.sizeChunks() * Sector.sizeChunks()) {
                        empty_sector_map[i][j] = true;
                        break;
                    }
                    if (biome_distribution[i][j][b] != Sector.sizeChunks() * Sector.sizeChunks()) break;
                }
            }
        }
    }

    private void createTownMap(double ratio) {
        town_map = new boolean[size_sectors][size_sectors];
        //compile a list of all valid town locations from the map
        ArrayList<int[]> valid_locations = new ArrayList<int[]>();
        for (int i = 0; i < town_map.length; i++) {
            for (int j = 0; j < town_map.length; j++) {
                for (int b = 0; b < Chunk.BIOME_COUNT; b++) {
                    if (empty_sector_map[i][j]) { //if sector is only one biome
                        if (biome_map[i * Sector.sizeChunks()][j * Sector.sizeChunks()] != Chunk.WATER) {
                            valid_locations.add(new int[]{i, j});
                        }
                    }
                }
            }
        }

        //use the town ratio to determine the number of towns
        int tcount = (int) (ratio * (float) valid_locations.size());

        while (tcount > 0) {
            if (valid_locations.isEmpty()) break;
            int random = Math.abs(rng.nextInt()) % valid_locations.size();
            int[] chosen = valid_locations.get(random);
            if (!town_map[chosen[0]][chosen[1]]) {
                //mark the town map
                town_map[chosen[0]][chosen[1]] = true;
                int[] wcm = MiscMath.getWorldCoordsFromMap(chosen[0] * Sector.sizeChunks(), chosen[1] * Sector.sizeChunks());
                int sc[] = MiscMath.getSectorCoords(wcm[0], wcm[1]);
                //create a new town instance
                Town t = new Town(sc[0], sc[1]);
                towns.add(t);
                valid_locations.remove(chosen);
                tcount--;
            }
        }
    }

    private void createRoadMap() {
        road_map = new boolean[size_sectors * Sector.sizeChunks()][size_sectors * Sector.sizeChunks()];
        //now place roads between the towns
        for (int i = 0; i < town_map.length; i++) {
            for (int j = 0; j < town_map.length; j++) {
                int x = i * Sector.sizeChunks(), y = j * Sector.sizeChunks();
                if (!town_map[i][j]) continue;
                int ox = 0, oy = 0;
                boolean up = true, down = true, left = true, right = true;
                while (true) {
                    if (i - ox <= -1) left = false;
                    if (i + ox >= empty_sector_map.length) right = false;
                    if (j + oy >= empty_sector_map.length) down = false;
                    if (j - oy <= -1) up = false;

                    if (right) {
                        if (empty_sector_map[i + ox][j]) {
                            placeRoadSegment(x + (ox * Sector.sizeChunks()), y + 1, 1);
                            placeRoadSegment(x + (ox * Sector.sizeChunks()), y + Sector.sizeChunks() + 1, 1);
                        } else {
                            right = false;
                        }
                    }
                    if (left) {
                        if (empty_sector_map[i - ox][j]) {
                            placeRoadSegment(x - (ox * Sector.sizeChunks()), y + 1, 1);
                            placeRoadSegment(x - (ox * Sector.sizeChunks()), y + Sector.sizeChunks() + 1, 1);
                        } else {
                            left = false;
                        }
                    }
                    if (down) {
                        if (empty_sector_map[i][j + oy]) {
                            placeRoadSegment(x, y + (oy * Sector.sizeChunks()), 2);
                            placeRoadSegment(x + Sector.sizeChunks(), y + (oy * Sector.sizeChunks()), 2);
                        } else {
                            down = false;
                        }
                    }
                    if (up) {
                        if (empty_sector_map[i][j - oy]) {
                            placeRoadSegment(x, y - (oy * Sector.sizeChunks()), 2);
                            placeRoadSegment(x + Sector.sizeChunks(), y - (oy * Sector.sizeChunks()), 2);
                        } else {
                            up = false;
                        }
                    }
                    ox++;
                    oy++;
                    if ((left || right || up || down) == false) break;
                }
            }
        }
    }

    /**
     * Loops through each town, then each nearby sector until it finds a spawn point.
     * @return
     */
    public boolean findSpawn() {
        ArrayList<int[]> valid_spawns = new ArrayList<int[]>();
        for (int x = 0; x < town_map.length; x++) {
            for (int y = 0; y < town_map.length; y++) {
                //if sector is a town, pick an adjacent sector and check if empty
                if (town_map[x][y]) {
                    for (int x2 = -1; x2 < 2; x2++) {
                        for (int y2 = -1; y2 < 2; y2++) {
                            int[] coords = {(int)MiscMath.clamp(x+x2, 0, size_sectors-1), (int)MiscMath.clamp(y+y2, 0, size_sectors-1)};
                            if (empty_sector_map[coords[0]][coords[1]] && !town_map[coords[0]][coords[1]]
                                    && biome_map[coords[0]*Sector.sizeChunks()][coords[1]*Sector.sizeChunks()] == Chunk.GRASS) {
                                valid_spawns.add(coords);
                            }
                        }
                    }
                }
            }
        }
        if (valid_spawns.isEmpty()) return false;
        spawn = valid_spawns.get(rng.nextInt(valid_spawns.size()));
        return true;
    }

    public byte[][] getBiomeMap() {
        return biome_map;
    }

    /**
     * Creates a proper road segment on the road map. Two tiles wide.
     * Facing upwards (dir = 0), the origin road tile is at map[x, y] and the second road tile is to the left of it.
     *
     * @param x      The x coordinate on the road map.
     * @param y      The y coordinate.
     * @param dir    The direction of the road segment, 0-3.
     */
    private void placeRoadSegment(int x, int y, int dir) {
        int ox = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int oy = dir == 1 ? -1 : (dir == 3 ? 1 : 0);
        int incr_y = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int incr_x = dir == 1 ? 1 : (dir == 3 ? -1 : 0);

        for (int i = 0; i < (Sector.sizeChunks() + 2); i++) {
            if (x > -1 && x < road_map.length
                    && y > -1 && y < road_map[0].length) road_map[x][y] = true;
            if (x + ox > -1 && x + ox < road_map.length
                    && y + oy > -1 && y + oy < road_map[0].length) road_map[x + ox][y + oy] = true;

            x += incr_x;
            y += incr_y;
        }
    }

    /**
     * Matches factories with commercial buildings. Probably doesn't work that great.
     */
    private void generateTradeRoutes() {
        LinkedList<TownBuilding>
                industrials = new LinkedList<TownBuilding>(),
                commercials = new LinkedList<TownBuilding>();
        for (Town t: towns) {
            industrials.addAll(t.industrialBuildings());
            commercials.addAll(t.commercialBuildings());
        }
        for (TownBuilding c: commercials) {
            for (TownBuilding i: industrials) {
                for (int p = 0; p < c.getProducts().length; p++) {
                    if (i.hasProduct(c.getProducts()[p])) {
                        if (i.addClient(c)) c.setSupplier(i, p);

                    }
                }
            }
        }
    }

    /**
     * Get the world spawn.
     * @return An int[] array {x, y} describing the spawn point, from world coordinates.
     */
    public int[] getSpawn() {
        System.out.println("Spawn is : "+spawn[0]+", "+spawn[1]);
        return spawn == null ?
                new int[]{0, 0} :
                new int[]{(int)((spawn[0]+0.5-(size_sectors/2))*Sector.sizePixels()),
                        (int)((spawn[1]+0.5-(size_sectors/2))*Sector.sizePixels())};
    }

    /**
     * Generates around the specified world coordinates.
     *
     * @param world_x World x.
     * @param world_y World y.
     */
    public void generateAround(int world_x, int world_y) {
        int sc[] = MiscMath.getSectorCoords(world_x, world_y);
        initializeSectors(sc[0], sc[1], 2);
    }

    /**
     * Initializes the sectors from a radius around the specified sector coordinate.
     * Imports terrain data for each from the maps from World. Once they are created and initialized,
     * they can be saved and loaded as usual (the entire map is not kept from memory all at once upon creation!).
     *
     * @param x The sector X coordinate.
     * @param y The sector Y coordinate.
     * @param r The radius.
     */
    private void initializeSectors(int x, int y, int r) {
        for (int h = -r; h != r + 1; h++) {
            for (int w = -r; w != r + 1; w++) {
                initializeSector(x + w, y + h);
            }
        }
    }

    private void initializeSector(int sx, int sy) {
        Sector s = getSector(sx, sy);
        if (s == null) {
            loadTerrainData(sx, sy);
            if (validSector(sx, sy)) {
                s = createSector(sx, sy);
                s.importBiomes(biome_map);
                s.importRoads(road_map);
                Town t = getTown(sx, sy);
                if (t != null) t.generate();
                s.importForest(forest_map);
            }
        }
    }

    private boolean validSector(int sector_x, int sector_y) {
        sector_x += size_sectors / 2;
        sector_y += size_sectors / 2;
        return sector_x >= 0 && sector_x < size_sectors
                && sector_y >= 0 && sector_y < size_sectors;
    }

    public int seed() {
        return seed;
    }

    /**
     * Gets the random number generator for this world.
     *
     * @return A Random instance.
     */
    public Random rng() {
        return rng;
    }

}
