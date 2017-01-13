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
    }
    
    public void update() {}
    
    public void generate() {
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
        Entity supermarket = Entity.create("Supermarket 1");
        supermarket.setWorldX(getParent().worldCoords()[0] + (Chunk.sizePixels()*2));
        supermarket.setWorldY(getParent().worldCoords()[1] + (Chunk.sizePixels()*2));
        World.getWorld().addEntity(supermarket);
        //use them to generate initial stats
        
    }
    
    private void placeRoadSegment(int cx, int cy, int dir, int rot) {
        /*int ox = rot == 1 ? 1 : (rot == 3 ? -1 : 0);
        int oy = rot == 0 ? -1 : (rot == 2 ? 1 : 0);
        int incr_y = dir == 0 ? -1 : (dir == 2 ? 1 : 0);
        int incr_x = dir == 1 ? 1 : (dir == 3 ? -1 : 0);
        for (int i = 0; i <= Sector.sizeChunks(); i++) {
            //int[] 
            Chunk c = World.getWorld().getChunk(x, y);
            
            if (x+ox > -1 && x+ox < map.length 
                    && y+oy > -1 && y+oy < map[0].length) map[x+ox][y+oy] = true;
            x+=incr_x;
            y+=incr_y;
        }*/
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
