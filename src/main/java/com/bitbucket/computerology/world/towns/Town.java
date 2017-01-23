package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SimplexNoise;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;
import java.util.ArrayList;
import java.util.Random;

public class Town {
    
    int population, x, y;
    ArrayList<Building> buildings;
    
    int[][] distribution;
    
    
    public Town(int sector_x, int sector_y) {
        this.buildings = new ArrayList<Building>();
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
        double industrial[][] = SimplexNoise.generate(Sector.sizeChunks(), Sector.sizeChunks(), 0.01, 0.975, 1);
        
        //blend the three district maps into one
        distribution = new int[Sector.sizeChunks()][Sector.sizeChunks()];
        for (int i = 0; i < distribution.length; i++) {
            for (int j = 0; j < distribution.length; j++) {
                double max = MiscMath.max(commercial[i][j], MiscMath.max(industrial[i][j], residential[i][j]));
                if (max == commercial[i][j])
                    distribution[i][j] = Building.COMMERCIAL;
                if (max == industrial[i][j])
                    distribution[i][j] = Building.INDUSTRIAL;
                if (max == residential[i][j])
                    distribution[i][j] = Building.RESIDENTIAL;
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
        
    }
    
    /**
     * Places buildings randomly within the city block. UNFINISHED.
     */
    private void randomizeBlock(int bx, int by) {
        System.out.println("Generating buildings for block "+bx+", "+by);
        //world coords for the block (relative to the sector)
        int[] b_wc = {Chunk.sizePixels()*((blockSizeChunks()*bx)+2), 
            Chunk.sizePixels()*((blockSizeChunks()*by)+2)};
        String[] res_names = {"House 1"}, ind_names = {"Factory 1"};
        String[] names = distribution[b_wc[0]/Chunk.sizePixels()][b_wc[1]/Chunk.sizePixels()] == Building.INDUSTRIAL ? ind_names : res_names; 
        Sector p = getParent();
        Random r = World.getWorld().rng();
        //initialized to false
        boolean cell_used[][] = new boolean[(blockSizeChunks()-2)/4][(blockSizeChunks()-2)/4];
        
        //determine the rotation using [i, j]
        int i = r.nextInt(cell_used.length), j = r.nextInt(cell_used.length);
        int rot = -1;
        if (i == 0) rot = 3;
        if (i == cell_used.length-1) rot = 1;
        if (j == 0) rot = 0;
        if (j == cell_used.length-1) rot = 2;
        
        System.out.println("Cell chosen: "+i+", "+j);
        
        //if the rotation is -1 then an invalid [i,j] was chosen
        if (rot == -1) return;
        
        System.out.println("Rotation: "+rot);
        
        //create and rotate an entity, getting its dimensions as well
        Entity e = Entity.create(names[r.nextInt(names.length)]);
        e.setRotation(rot*90);
        int ew = (int)((e.getWidth()/Chunk.sizePixels()))/4;
        int eh = (e.getHeight()/Chunk.sizePixels())/4;
        //calculate how far the entity would go out of bounds if placed
        int w_diff = (i+ew)-cell_used.length, h_diff = (j+eh)-cell_used.length;
        
        System.out.println(e);
        System.out.println("Width: "+ew+", height: "+eh);
        System.out.println("Overflow: "+w_diff+", "+h_diff);
        
        //if out of bounds, shift the cell over
        if (w_diff > 0) i -= w_diff;
        if (h_diff > 0) j -= h_diff;
        
        //if you shifted too far over, then invalid
        if (i < 0 || j < 0) return;
        
        System.out.println("Cell after shifting: "+i+", "+j);
        
        //determine whether the entity will fit (if a cell is used, it will not)
        boolean clear = true;
        for (int a = i; a < ew; a++) {
            for (int b = j; b < eh; b++) {
                if (a < 0 || a >= cell_used.length
                        || b < 0 || b >= cell_used.length) continue;
                if (cell_used[a][b]) { clear = false; break; }
            }
        }
        //TODO: mark cells as used
        
        System.out.println("Region ["+i+", "+j+", "+ew+", "+eh+"] clear: "+clear);
        
        if (clear) {
            e.setWorldX(p.getWorldCoords()[0]+b_wc[0]+(i*4*Chunk.sizePixels())+(e.getWidth()/2)+(i*Chunk.sizePixels()));
            e.setWorldY(p.getWorldCoords()[1]+b_wc[1]+(j*4*Chunk.sizePixels())+(e.getHeight()/2)+(j*Chunk.sizePixels()));
            World.getWorld().addEntity(e);
            System.out.println("Added entity to "+e.getWorldX()+", "+e.getWorldY());
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
    
    int getDemand(int resource) {
        int sum = 0;
        for (Building b: buildings) sum+=b.getDemand(resource);
        return sum;
    }
    
    int getPrice(int resource) {
        return 1;
    }
    
}
