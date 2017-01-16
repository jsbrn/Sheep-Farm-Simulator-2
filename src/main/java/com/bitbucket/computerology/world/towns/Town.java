package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SimplexNoise;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;
import java.util.ArrayList;

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
        
        //place the buildings and towns
        Entity supermarket = Entity.create("House 1");
        supermarket.setWorldX(getParent().getWorldCoords()[0] + 32);
        supermarket.setWorldY(getParent().getWorldCoords()[1] + 32);
        World.getWorld().addEntity(supermarket);
        
        for (int i = 1; i <= 3; i++) {
            this.placeRoadSegment(i*16, 2, Sector.sizeChunks()-2, 2);
            this.placeRoadSegment(2, 1+(i*16), Sector.sizeChunks()-2, 1);
        }
        
        //use them to generate initial stats
        
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
