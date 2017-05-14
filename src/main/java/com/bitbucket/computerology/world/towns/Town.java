package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SimplexNoise;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.components.TownBuilding;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Town {

    public static int RESIDENTIAL_BUILDING = 0, COMMERCIAL_BUILDING = 1, INDUSTRIAL_BUILDING = 2;

    private int population, x, y;
    private ArrayList<TownBuilding> buildings,
            industrial_buildings,
            residential_buildings,
            commercial_buildings;

    private int[][] distribution;

    public Town(int sector_x, int sector_y) {
        this.buildings = new ArrayList<TownBuilding>();
        this.industrial_buildings = new ArrayList<TownBuilding>();
        this.residential_buildings = new ArrayList<TownBuilding>();
        this.commercial_buildings = new ArrayList<TownBuilding>();
        this.x = sector_x;
        this.y = sector_y;
    }

    public static int blockSizeChunks() {
        return 16;
    }

    public void update() {
    }

    public void generate() {

        System.out.println("Generating town at sector " + getParent().getSectorCoords()[0] + ", " + getParent().getSectorCoords()[1]);

        //generate the noise map describing the building distribution
        double residential[][] = SimplexNoise.generate(4, 4, 0.01, 0.975, 1);
        double commercial[][] = SimplexNoise.generate(4, 4, 0.01, 0.975, 1);

        //blend the two district maps into one
        distribution = new int[4][4];
        for (int i = 0; i < distribution.length; i++) {
            for (int j = 0; j < distribution.length; j++) {
                double max = MiscMath.max(commercial[i][j], residential[i][j]);
                if (max == commercial[i][j])
                    distribution[i][j] = COMMERCIAL_BUILDING;
                if (max == residential[i][j])
                    distribution[i][j] = RESIDENTIAL_BUILDING;
            }
        }
        //three random factories, one random commercial, and one random residential
        for (int i = 0; i < 5; i++)
            distribution
                    [new Random().nextInt(distribution.length)]
                    [new Random().nextInt(distribution.length)] = (i < 3 ? INDUSTRIAL_BUILDING
                    : (i == 3 ? RESIDENTIAL_BUILDING : COMMERCIAL_BUILDING));


        //divide the sector with roads, creating city blocks to place buildings from
        for (int i = 1; i < (Sector.sizeChunks() / blockSizeChunks()); i++) {
            this.placeRoadSegment(i * blockSizeChunks(), 2, Sector.sizeChunks() - 2, 2);
            this.placeRoadSegment(2, 1 + (i * blockSizeChunks()), Sector.sizeChunks() - 2, 1);
        }

        //place the buildings
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                randomizeBlock(i, j);
            }
        }

        refreshPopulationScore();

        System.out.println("Generated " + this + ": population = " + population);

    }

    private void refreshPopulationScore() {
        double sum = 0;
        for (TownBuilding b : residential_buildings) {
            sum += b.getResidentPercentage();
        }
        sum /= residential_buildings.size();
        population = (int) (10 * sum);
    }

    public void save(BufferedWriter bw) {
        try {
            bw.write("t\n");
            bw.write("x="+x+"\n");
            bw.write("y="+y+"\n");
            bw.write("p="+population+"\n");
            bw.write("/t\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final boolean load(BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.equals("/t")) return true;
                if (line.indexOf("x=") == 0) x = Integer.parseInt(line.substring(2, line.length()));
                if (line.indexOf("y=") == 0) y = Integer.parseInt(line.substring(2, line.length()));
                if (line.indexOf("p=") == 0) population = Integer.parseInt(line.substring(2, line.length()));
            }
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Places buildings randomly within the city block.
     * Building dimensions must be a multiple of Chunk.sizePixels().
     */
    private void randomizeBlock(int bx, int by) {
        //create a grid that breaks the city block into 4x4 cells and use it to
        //generate random buildings
        boolean cell_used[][] = new boolean[((blockSizeChunks() - 2) * 4)][((blockSizeChunks() - 2)) * 4];
        for (int i = 0; i < 32; i++) randomBuilding(bx, by, cell_used);
    }

    private void addBuilding(Entity e) {
        Component c = e.getComponent("TownBuilding");
        if (c == null) return;
        TownBuilding b = ((TownBuilding) c);
        buildings.add(b);
        //add to the appropriate list
        (b.getType() == INDUSTRIAL_BUILDING ? industrial_buildings :
                (b.getType() == RESIDENTIAL_BUILDING ? residential_buildings : commercial_buildings)).add(b);
    }

    /**
     * Picks a random building from the list of options, and places it.
     * This implementation is long and cumbersome but it works from a single function call.
     *
     * @param bx        The block coordinates, that is, the top-left section of the sector is (0, 0).
     *                  The one next to it is (1, 0).
     * @param by        The block coordinates.
     * @param cell_used The boolean array that keeps track of available space from the block.
     */
    private void randomBuilding(int bx, int by, boolean[][] cell_used) {
        //world coords for the block (relative to the sector)
        int[] b_wc = {Chunk.sizePixels() * ((blockSizeChunks() * bx) + 2),
                Chunk.sizePixels() * ((blockSizeChunks() * by) + 2)};

        String[] res_names = {"House 1"},
                ind_names = {"Factory 1"},
                com_names = {"Supermarket 1"};
        int building_type = distribution[bx][by];
        String[] names = building_type == INDUSTRIAL_BUILDING ?
                ind_names : (building_type == COMMERCIAL_BUILDING ? com_names : res_names);

        Sector p = getParent();
        Random r = World.getWorld().rng();

        //determine [i,j] and the rotation
        //round to the nearest multiple of the block size, meaning stay on the edges
        int i = (int) MiscMath.round(r.nextInt(cell_used.length - 2), (cell_used.length - 2) / 2);
        int j = (int) MiscMath.round(r.nextInt(cell_used.length), (cell_used.length - 2) / 2);
        int rot = -1;
        if (i == 0) rot = 3;
        if (i == cell_used.length - 1) rot = 1;
        if (j == 0) rot = 0;
        if (j == cell_used.length - 1) rot = 2;

        //if the rotation is -1 then an invalid [i,j] was chosen
        if (rot == -1) return;

        //create and rotate an entity, getting its dimensions as well
        Entity e = Entity.create(names[r.nextInt(names.length)]);
        e.setRotation(rot * 90);
        double cell_dim = Chunk.sizePixels() / 4;
        int ew = (int) Math.round(e.getWidth() / cell_dim);
        int eh = (int) Math.round(e.getHeight() / cell_dim);
        //calculate how far the entity would go to of bounds if placed
        int w_diff = (i + ew) - cell_used.length, h_diff = (j + eh) - cell_used.length;

        //if to of bounds, shift the cell over
        if (w_diff > 0) i -= w_diff;
        if (h_diff > 0) j -= h_diff;

        //if you shifted too far over, then invalid
        if (i < 0 || j < 0) return;

        //adjust again to ensure that buildings line up with the road
        //for a couple rotations that are a little off
        int cw = ew, ch = eh;
        int cx = i, cy = j;
        int cx_diff = ((blockSizeChunks() - 2) * 4) - (cx + cw);
        int cy_diff = ((blockSizeChunks() - 2) * 4) - (cy + ch);

        if (rot == 1 && cx_diff > 0) i += cx_diff;
        if (rot == 2 && cy_diff > 0) j += cy_diff;

        //determine whether the entity will fit (if a cell is used, it will not)
        boolean clear = true;
        for (int a = i; a < i + ew; a++) { //have an 8px x 8px margin around the entity
            for (int b = j; b < j + eh; b++) {
                if (a < 0 || a >= cell_used.length
                        || b < 0 || b >= cell_used.length) continue;
                if (cell_used[a][b]) {
                    clear = false;
                    break;
                }
            }
            if (!clear) break;
        }

        if (clear) {
            //determine the actual coordinates for the entity to use
            int entity_x = p.getWorldCoords()[0] + b_wc[0] + (i * (int) cell_dim) + (e.getWidth() / 2);//+(i*Chunk.sizePixels());
            int entity_y = p.getWorldCoords()[1] + b_wc[1] + (j * (int) cell_dim) + (e.getHeight() / 2);//+(j*Chunk.sizePixels());

            e.setWorldX(entity_x);
            e.setWorldY(entity_y);
            World.getWorld().addEntity(e);
            addBuilding(e);

            //mark all the used cells as such, for future buildings
            for (int a = i - 2; a < i + ew + 4; a++) {
                for (int b = j - 2; b < j + eh + 4; b++) {
                    if (a < 0 || a >= cell_used.length
                            || b < 0 || b >= cell_used.length) continue;
                    cell_used[a][b] = true;
                }
            }

        }
    }

    /**
     * Creates a proper road segment (2 from width with traffic lines) of any length.
     *
     * @param cx     The number of chunks along the x away from the Town's parent sector. Can overlap into
     *               different sectors if need be. Facing upwards, the origin road tile is at cx, cy and the second
     *               road tile is to the left of it.
     * @param cy     Number of chunks along the y.
     * @param length The length of the road segment.
     * @param dir    The direction of the road segment, 0-3.
     */
    private void placeRoadSegment(int cx, int cy, int length, int dir) {
        System.out.println("Placing road segment at "+cx+", "+cy+" with l = "+length+" and d = "+dir);
        int ox = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int oy = dir == 1 ? -1 : (dir == 3 ? 1 : 0);
        int incr_y = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int incr_x = dir == 1 ? 1 : (dir == 3 ? -1 : 0);
        int rot1 = dir, rot2 = dir + 2;
        for (int i = 0; i <= length; i++) {
            int wc[] = getParent().getWorldCoords();
            wc[0] += Chunk.sizePixels() * cx;
            wc[1] += cy * Chunk.sizePixels();

            int t = World.getWorld().getTerrain(wc[0], wc[1]);

            World.getWorld().setTerrain(wc[0], wc[1],
                    t == Chunk.ROAD_STRAIGHT ? Chunk.ROAD_INTERSECTION : Chunk.ROAD_STRAIGHT, rot1);
            World.getWorld().setTerrain(wc[0] + (ox * Chunk.sizePixels()),
                    wc[1] + (oy * Chunk.sizePixels()),
                    t == Chunk.ROAD_STRAIGHT ? Chunk.ROAD_INTERSECTION : Chunk.ROAD_STRAIGHT, rot2);

            cx += incr_x;
            cy += incr_y;
        }
    }

    public int[] getSectorCoordinates() {
        return new int[]{x, y};
    }

    public Sector getParent() {
        return World.getWorld().getSector(x, y);
    }

    public int getPopulation() {
        return population;
    }

    public ArrayList<TownBuilding> industrialBuildings() { return industrial_buildings; }

    public ArrayList<TownBuilding> commercialBuildings() { return commercial_buildings; }

    @Override
    public String toString() {
        return "Town [" + x + ", " + y + "]";
    }


}
