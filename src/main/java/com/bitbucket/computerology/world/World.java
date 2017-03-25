package com.bitbucket.computerology.world;

import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SimplexNoise;
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

    private Image map_texture;
    private byte[][] biome_map;
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

    public static void save() {
        File f = new File(Assets.ROOT_DIR+"/saves/" +world.save_name+ "/world.txt");
        FileWriter fw;
        System.out.println("Saving to file " + f.getAbsoluteFile().getAbsolutePath());
        try {
            if (!f.exists()) f.createNewFile();
            fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("p: player stats\n");
            bw.write("s: sector\n");
            bw.write("c: chunk\n");
            bw.write("e: entity\n");
            bw.write("--------------------------------\n");
            bw.write("t=" + (int) world.time + "\n");
            bw.write("cx=" + (int) Camera.getX() + "\n");
            bw.write("cy=" + (int) Camera.getY() + "\n");
            bw.write("cz=" + (int) Camera.getZoom() + "\n");
            world.player.save(bw);
            for (Sector s : world.sectors) {
                s.save(bw);
                for (Entity e : s.getEntities()) e.save(bw);
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
        this.queued_map_updates = new ArrayList<int[]>();
    }

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

    public void setTerrain(int wx, int wy, int terrain, int rot) {
        int[] sc = MiscMath.getSectorCoords(wx, wy);
        int[] cc = MiscMath.getChunkCoords(wx, wy);
        Sector s = getSector(sc[0], sc[1]);
        Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
        if (c != null) {
            c.setRotation(rot);
            c.setTerrain(terrain);
        }
    }

    public int getTerrain(int wx, int wy) {
        int[] sc = MiscMath.getSectorCoords(wx, wy);
        int[] cc = MiscMath.getChunkCoords(wx, wy);
        Sector s = getSector(sc[0], sc[1]);
        Chunk c = s != null ? s.getChunk(cc[0], cc[1]) : null;
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

    public Entity getEntity(int x, int y, int w, int h) {
        ArrayList<Entity> list = getEntities(x, y, w, h);
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
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
     * Returns the current time since world creation (in minutes).
     **/
    public int getTime() {
        return (int) time;
    }

    /**
     * Returns the hour in 24-hour format
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
     * @param x The offset (in sectors) from the origin.
     * @param y The offset (in sectors) from the origin.
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
        if ((sub_bounds[1] + 1) - sub_bounds[0] <= 2) { //if sublist is two in length
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
     * to keep the list sorted. If the sector is found to already exist in the list,
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
        if ((sub_bounds[1] + 1) - sub_bounds[0] <= 2) { //if sublist is two in length
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

    public ArrayList<Sector> sectors() {
        return sectors;
    }

    public void draw(Graphics g) {
        drawTerrain(false, g);
        //drawTerrain(true, g);
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
     * Loops through all entities in the world and draws the ones that are visible.
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
     * Must be called in a render thread (requires OpenGL context).
     */
    private void applyMapChanges() {
        try {
            if (map_texture == null) return;
            Graphics g = map_texture.getGraphics();
            for (int[] mc : queued_map_updates) {
                int wc[] = MiscMath.getWorldCoordsFromMap(mc[0], mc[1]);
                int t = getTerrain(wc[0] + (Chunk.sizePixels() / 2), wc[1] + Chunk.sizePixels() / 2);
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
        , 0.02, 0.04, 8, 0.05)) return false;
        generateTradeRoutes();
        generateAround(getSpawn()[0], getSpawn()[1]);
        return true;
    }

    private static double[] loadGeneratorSettings() {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        f.delete();
        return settings;
    }

    public boolean isGenerated() {
        return new File(Assets.ROOT_DIR+"/saves/" +world.save_name+"/world.txt").exists();
    }

    private boolean generate(int size_sectors, double scale,
                             double g_height, double d_height, double t_height,
                             double forest_scale, double forest_height, int forest_passes,
                             double town_ratio) {
        System.out.println("Generating world of size "+size_sectors+"x"+size_sectors);
        this.biome_map = new byte[size_sectors * Sector.sizeChunks()][size_sectors * Sector.sizeChunks()];
        this.forest_map = new boolean[size_sectors * Sector.sizeChunks()][size_sectors * Sector.sizeChunks()];
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
                        (biome_map[i][j] == Chunk.GRASS || biome_map[i][j] == Chunk.SNOW)) {
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
        boolean[][] empty_sector_map = createEmptySectorMap(biome_distribution);
        boolean[][] town_map = createTownMap(empty_sector_map, town_ratio);
        road_map = createRoadMap(empty_sector_map, town_map);
        return findSpawn(empty_sector_map, town_map);
    }

    private boolean[][] createEmptySectorMap(double biome_distribution[][][]) {
        boolean map[][] = new boolean[size_sectors][size_sectors];
        for (int i = 0; i < size_sectors; i++) {
            for (int j = 0; j < size_sectors; j++) {
                for (int b = 0; b < Chunk.BIOME_COUNT; b++) {
                    if (biome_distribution[i][j][b] == 0) continue;
                    if (biome_distribution[i][j][b] == Sector.sizeChunks() * Sector.sizeChunks()) {
                        map[i][j] = true;
                        break;
                    }
                    if (biome_distribution[i][j][b] != Sector.sizeChunks() * Sector.sizeChunks()) break;
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
            if (!map[chosen[0]][chosen[1]]) {
                //mark the town map
                map[chosen[0]][chosen[1]] = true;
                int[] wcm = MiscMath.getWorldCoordsFromMap(chosen[0] * Sector.sizeChunks(), chosen[1] * Sector.sizeChunks());
                int sc[] = MiscMath.getSectorCoords(wcm[0], wcm[1]);
                //create a new town instance
                Town t = new Town(sc[0], sc[1]);
                towns.add(t);
                valid_locations.remove(chosen);
                tcount--;
            }
        }
        return map;
    }

    private boolean[][] createRoadMap(boolean empty_sector_map[][], boolean[][] town_map) {
        boolean map[][] = new boolean[size_sectors * Sector.sizeChunks()][size_sectors * Sector.sizeChunks()];
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
                            placeRoadSegment(map, x + (ox * Sector.sizeChunks()), y + 1, 1);
                            placeRoadSegment(map, x + (ox * Sector.sizeChunks()), y + Sector.sizeChunks() + 1, 1);
                        } else {
                            right = false;
                        }
                    }
                    if (left) {
                        if (empty_sector_map[i - ox][j]) {
                            placeRoadSegment(map, x - (ox * Sector.sizeChunks()), y + 1, 1);
                            placeRoadSegment(map, x - (ox * Sector.sizeChunks()), y + Sector.sizeChunks() + 1, 1);
                        } else {
                            left = false;
                        }
                    }
                    if (down) {
                        if (empty_sector_map[i][j + oy]) {
                            placeRoadSegment(map, x, y + (oy * Sector.sizeChunks()), 2);
                            placeRoadSegment(map, x + Sector.sizeChunks(), y + (oy * Sector.sizeChunks()), 2);
                        } else {
                            down = false;
                        }
                    }
                    if (up) {
                        if (empty_sector_map[i][j - oy]) {
                            placeRoadSegment(map, x, y - (oy * Sector.sizeChunks()), 2);
                            placeRoadSegment(map, x + Sector.sizeChunks(), y - (oy * Sector.sizeChunks()), 2);
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
        return map;
    }

    /**
     * Loops through each town, then each nearby sector
     * @param empty_sector_map
     * @return
     */
    public boolean findSpawn(boolean empty_sector_map[][], boolean town_map[][]) {
        for (int x = 0; x < empty_sector_map.length; x++) {
            for (int y = 0; y < empty_sector_map.length; y++) {
                //if sector is empty, not water, not a town, and has a road in the top left corner, then it is a valid spawn
                if (empty_sector_map[x][y]
                        && biome_map[x][y] != Chunk.WATER
                        && !town_map[x][y]
                        && !road_map[x * Sector.sizeChunks()][y * Sector.sizeChunks()]) {
                    spawn = new int[]{x, y};
                    return true;
                }
            }
        }
        spawn = null;
        return false;
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
    private void placeRoadSegment(boolean map[][], int x, int y, int dir) {
        int ox = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int oy = dir == 1 ? -1 : (dir == 3 ? 1 : 0);
        int incr_y = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int incr_x = dir == 1 ? 1 : (dir == 3 ? -1 : 0);
        for (int i = 0; i < (Sector.sizeChunks() + 2); i++) {

            if (x > -1 && x < map.length
                    && y > -1 && y < map[0].length) map[x][y] = true;
            if (x + ox > -1 && x + ox < map.length
                    && y + oy > -1 && y + oy < map[0].length) map[x + ox][y + oy] = true;

            x += incr_x;
            y += incr_y;
        }
    }

    /**
     * Matches factories with commercial buildings.
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
     * @return An int[] array {x, y} describing the spawn point, in world coordinates.
     */
    public int[] getSpawn() {
        return spawn == null ?
                new int[]{0, 0} :
                new int[]{(int)((spawn[0]+0.5) * Sector.sizePixels()),
                        ((int)(spawn[1]+0.5) * Sector.sizePixels())};
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
     * Initializes the sectors in a radius around the specified sector coordinate.
     * Imports terrain data for each from the maps in World. Once they are created and initialized,
     * they can be saved and loaded as usual (the entire map is not kept in memory all at once upon creation!).
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

    private void initializeSector(int x, int y) {
        Sector s = getSector(x, y);
        if (s == null) {
            if (validSector(x, y)) {
                s = createSector(x, y);
                s.importBiomes(biome_map);
                s.importRoads(road_map);
                Town t = getTown(x, y);
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
