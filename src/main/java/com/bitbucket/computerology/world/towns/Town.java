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
        for (int i = 1; i <= 3; i++) {
            this.placeRoadSegment(i*16, 2, Sector.sizeChunks()-2, 2);
            this.placeRoadSegment(2, 1+(i*16), Sector.sizeChunks()-2, 1);
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
        int[] origin = {2 + (16*bx), 2 + (16*by)};
        String[] res_names = {"House 1"}, ind_names = {"Factory 1"};
        Sector p = getParent();
        for (int i = 0; i < 16; i++) {
            int[][] spawns = 
                {{origin[0]+i, origin[1]}, {origin[0]+14, origin[1]+i}, {origin[0]+i, origin[1]+14}, {origin[0], origin[1]+i}};
            for (int r = 0; r < 1; r++) {
                //determine the world coordinates
                int wx = p.getWorldCoords()[0]+(spawns[r][0]*Chunk.sizePixels());
                int wy = p.getWorldCoords()[1]+(spawns[r][1]*Chunk.sizePixels());
                String[] names = res_names;
                //String[] names = distribution[spawns[r][0]][spawns[r][1]] == Building.INDUSTRIAL ? ind_names : res_names;
                String name = names[new Random().nextInt(names.length)];
                Entity e = Entity.create(name);
                e.setRotation(r*90);
                
                if (r == 0) { wx+=e.getWidth()/2; wy+=e.getHeight()/2; }
                if (r == 1) { wx-=e.getWidth()/2; wy+=e.getHeight()/2; }
                if (r == 2) { wx+=e.getWidth()/2; wy-=e.getHeight()/2; }
                if (r == 3) { wx+=e.getWidth()/2; wy+=e.getHeight()/2; }
                
                e.setWorldX(wx);
                e.setWorldY(wy);
                
                int[] params = {wx-(e.getWidth()/2), wy-(e.getHeight()/2), e.getWidth(), e.getHeight()};
                Entity obstacle = World.getWorld().getEntity(params[0], params[1], params[2], params[3]);
                System.out.println("First entity at: "+params[0]+", "+params[1]+", "+params[2]+", "+params[3]+": "+obstacle);
                if (obstacle == null) {
                    World.getWorld().addEntity(e);
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
    
    int getDemand(int resource) {
        int sum = 0;
        for (Building b: buildings) sum+=b.getDemand(resource);
        return sum;
    }
    
    int getPrice(int resource) {
        return 1;
    }
    
}
