package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SimplexNoise;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.components.TownBuilding;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;
import java.util.ArrayList;
import java.util.Random;
import org.newdawn.slick.Color;

public class Town {
    
    public static int RESIDENTIAL_BUILDING = 0, COMMERCIAL_BUILDING = 1, INDUSTRIAL_BUILDING = 2;
    public static Color[] BUILDING_COLORS = {Color.green, Color.cyan, Color.lightGray};
    
    int population, x, y;
    ArrayList<Entity> buildings, 
            industrial_buildings, 
            residential_buildings, 
            commercial_buildings;
    
    int[][] distribution;
    
    
    public Town(int sector_x, int sector_y) {
        this.buildings = new ArrayList<Entity>();
        this.industrial_buildings = new ArrayList<Entity>();
        this.residential_buildings = new ArrayList<Entity>();
        this.commercial_buildings = new ArrayList<Entity>();
        this.x = sector_x;
        this.y = sector_y;
    }
    
    public static int blockSizeChunks() { return 16; }
    
    public int[][] buildingDistribution() { return distribution; }
    
    public void update() {}
    
    public void generate() {
        
        System.out.println("Generating town at sector "+getParent().getSectorCoords()[0]+", "+getParent().getSectorCoords()[1]);
        
        //generate the noise map describing the building distribution
        double residential[][] = SimplexNoise.generate(Sector.sizeChunks(), Sector.sizeChunks(), 0.01, 0.975, 1);
        double commercial[][] = SimplexNoise.generate(Sector.sizeChunks(), Sector.sizeChunks(), 0.01, 0.975, 1);
        double industrial[][] = SimplexNoise.generate(Sector.sizeChunks(), Sector.sizeChunks(), 0.01, 0.900, 1);
        
        //blend the three district maps into one
        distribution = new int[Sector.sizeChunks()][Sector.sizeChunks()];
        for (int i = 0; i < distribution.length; i++) {
            for (int j = 0; j < distribution.length; j++) {
                double max = MiscMath.max(commercial[i][j], MiscMath.max(industrial[i][j], residential[i][j]));
                if (max == commercial[i][j])
                    distribution[i][j] = COMMERCIAL_BUILDING;
                if (max == industrial[i][j])
                    distribution[i][j] = INDUSTRIAL_BUILDING;
                if (max == residential[i][j])
                    distribution[i][j] = RESIDENTIAL_BUILDING;
            }
        }
        
        //divide the sector with roads, creating city blocks to place buildings in
        for (int i = 1; i < (Sector.sizeChunks()/blockSizeChunks()); i++) {
            this.placeRoadSegment(i*blockSizeChunks(), 2, Sector.sizeChunks()-2, 2);
            this.placeRoadSegment(2, 1+(i*blockSizeChunks()), Sector.sizeChunks()-2, 1);
        }
        
        //place the buildings
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                randomizeBlock(i, j);
            }
        }
        
        refreshPopulationScore();
        assignIndustrialBuildings();
        
    }
    
    private void refreshPopulationScore() {
        population = 0;
        Component c; TownBuilding b;
        for (Entity e: buildings) {
            c = e.getComponent("TownBuilding");
            if (c == null) continue;
            b = ((TownBuilding)c);
            population+=b.getResidentCount();
        }
    }
    
    private void assignIndustrialBuildings() {
        //for (Entity e: )
    }
    
    /**
     * Places buildings randomly within the city block. UNFINISHED.
     * Building dimensions must be a multiple of Chunk.sizePixels().
     */
    private void randomizeBlock(int bx, int by) {
        System.out.println("Generating buildings for block "+bx+", "+by);
        //create a grid that breaks the city block into 4x4 cells and use it to
        //generate random buildings
        boolean cell_used[][] = new boolean[((blockSizeChunks()-2)*4)][((blockSizeChunks()-2))*4];
        for (int i = 0; i < 32; i++) randomBuilding(bx, by, cell_used);
    }
    
    private void addBuilding(Entity e) {
        Component c = e.getComponent("TownBuilding");
        if (c == null) return;
        TownBuilding b = ((TownBuilding)c);
        buildings.add(e);
        //add to the appropriate list
        (b.getType() == INDUSTRIAL_BUILDING ? industrial_buildings : 
                (b.getType() == RESIDENTIAL_BUILDING ? residential_buildings : commercial_buildings)).add(e);
    }
    
    private void randomBuilding(int bx, int by, boolean[][] cell_used) {
        //world coords for the block (relative to the sector)
        int[] b_wc = {Chunk.sizePixels()*((blockSizeChunks()*bx)+2), 
            Chunk.sizePixels()*((blockSizeChunks()*by)+2)};
        
        String[] res_names = {"House 1"}, ind_names = {"Factory 1"}, com_names = {"Supermarket 1"};
        int building_type = distribution[b_wc[0]/Chunk.sizePixels()][b_wc[1]/Chunk.sizePixels()];
        String[] names = building_type == INDUSTRIAL_BUILDING ? 
                ind_names : (building_type == COMMERCIAL_BUILDING ? com_names : res_names); 
        
        Sector p = getParent();
        Random r = World.getWorld().rng();
        
        //determine [i,j] and the rotation
        //round to the nearest <block size>, so basically be either 0 or 14.
        int i = (int)MiscMath.round(r.nextInt(cell_used.length-2), (cell_used.length-2)/2); 
        int j = (int)MiscMath.round(r.nextInt(cell_used.length), (cell_used.length-2)/2); 
        int rot = -1;
        if (i == 0) rot = 3;
        if (i == cell_used.length-1) rot = 1;
        if (j == 0) rot = 0;
        if (j == cell_used.length-1) rot = 2;
        
        //if the rotation is -1 then an invalid [i,j] was chosen
        if (rot == -1) return;
        
        //System.out.println(" Rotation: "+rot);
        
        //create and rotate an entity, getting its dimensions as well
        Entity e = Entity.create(names[r.nextInt(names.length)]);
        e.setRotation(rot*90);
        double cell_dim = Chunk.sizePixels()/4;
        int ew = (int)Math.round(e.getWidth()/cell_dim);
        int eh = (int)Math.round(e.getHeight()/cell_dim);
        //calculate how far the entity would go out of bounds if placed
        int w_diff = (i+ew)-cell_used.length, h_diff = (j+eh)-cell_used.length;

        //if out of bounds, shift the cell over
        if (w_diff > 0) i -= w_diff;
        if (h_diff > 0) j -= h_diff;
        
        //if you shifted too far over, then invalid
        if (i < 0 || j < 0) return;
        
        //adjust again to ensure that buildings line up with the road
        //for a couple rotations that are a little off
        int cw = ew, ch = eh;
        int cx = i, cy = j;
        int cx_diff = ((blockSizeChunks()-2)*4) - (cx+cw);
        int cy_diff = ((blockSizeChunks()-2)*4) - (cy+ch);

        if (rot == 1 && cx_diff > 0) i += cx_diff;
        if (rot == 2 && cy_diff > 0) j += cy_diff;
        
        //determine whether the entity will fit (if a cell is used, it will not)
        boolean clear = true;
        for (int a = i; a < i+ew; a++) { //have an 8px x 8px margin around the entity
            for (int b = j; b < j+eh; b++) {
                if (a < 0 || a >= cell_used.length
                        || b < 0 || b >= cell_used.length) continue;
                System.out.println("  "+a+", "+b+" is "+(cell_used[a][b] ? "NOT clear." : "clear!"));
                if (cell_used[a][b]) { clear = false; break; }
            }
            if (!clear) break;
        }
        
        if (clear) {
            
            //determine the actual coordinates for the entity to use
            int entity_x = p.getWorldCoords()[0]+b_wc[0]+(i*(int)cell_dim)+(e.getWidth()/2);//+(i*Chunk.sizePixels());
            int entity_y = p.getWorldCoords()[1]+b_wc[1]+(j*(int)cell_dim)+(e.getHeight()/2);//+(j*Chunk.sizePixels());
            
            e.setWorldX(entity_x);
            e.setWorldY(entity_y);
            World.getWorld().addEntity(e);
            addBuilding(e);
            
            //mark all the used cells as such, for future buildings
            for (int a = i-2; a < i+ew+4; a++) {
                for (int b = j-2; b < j+eh+4; b++) {
                    if (a < 0 || a >= cell_used.length
                            || b < 0 || b >= cell_used.length) continue;
                    cell_used[a][b] = true;
                }
            }
            
        }
    }
    
    /**
     * Creates a proper road segment (2 in width with traffic lines) of any length.
     * @param cx The number of chunks along the x away from the Town's parent sector. Can overlap into
     * different sectors if need be. Facing upwards, the origin road tile is at cx, cy and the second
     * road tile is to the left of it.
     * @param cy Number of chunks along the y.
     * @param length The length of the road segment.
     * @param dir The direction of the road segment, 0-3.
     */
    private void placeRoadSegment(int cx, int cy, int length, int dir) {
        int ox = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int oy = dir == 1 ? -1 : (dir == 3 ? 1 : 0);
        int incr_y = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int incr_x = dir == 1 ? 1 : (dir == 3 ? -1 : 0);
        int rot1 = dir, rot2 = dir+2;
        for (int i = 0; i <= length; i++) {
            int wc[] = getParent().getWorldCoords(); 
            wc[0]+=Chunk.sizePixels()*cx; wc[1]+=cy*Chunk.sizePixels();
            
            int t = World.getWorld().getTerrain(wc[0], wc[1]);
            
            World.getWorld().setTerrain(wc[0], wc[1], 
                    t == Chunk.ROAD_STRAIGHT ? Chunk.ROAD_INTERSECTION : Chunk.ROAD_STRAIGHT, rot1);
            World.getWorld().setTerrain(wc[0]+(ox*Chunk.sizePixels()), 
                    wc[1]+(oy*Chunk.sizePixels()), 
                    t == Chunk.ROAD_STRAIGHT ? Chunk.ROAD_INTERSECTION : Chunk.ROAD_STRAIGHT, rot2);
            
            cx+=incr_x;
            cy+=incr_y;
        }
    }
    
    public int[] getSectorCoordinates() { return new int[]{x, y}; }
    public Sector getParent() { return World.getWorld().getSector(x, y); }
    
    public int getPopulation() { return population; }
    
}
