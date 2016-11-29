package com.bitbucket.computerology.world.terrain.generators;

import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;

public class GrassBiomeGen extends Generator {
    
    int[] river_coords = {-1, -1};
    
    public GrassBiomeGen(Sector s) { super(s); }
    
    @Override
    public void generate() {
        if (isRiver()) createRiver();
    }
    
    boolean isRiver() {
        return !(river_coords[0] == -1 && river_coords[1] == -1);
    }
    
    /**
     * Tells this generator to spawn a river,
     * originating at wx and wy.
     */
    public void markAsRiver(int wx, int wy) {
        river_coords = new int[]{wx, wy};
    }
    
    public void createRiver() {
        if (river_coords[0] == -1) return;
        System.out.println("Sector "+parent.worldCoords()[0]+", "+parent.worldCoords()[1]+" is river: creating.");
        int[] end = new int[]{-1, -1}, last = river_coords;
        
        int r = Math.abs(parent.getWorld().rng().nextInt() % 4);
        if (r == 0) end = new int[]{parent.worldCoords()[0]+(Sector.sizePixels()/2)
                , parent.worldCoords()[1]};
        if (r == 0) end = new int[]{parent.worldCoords()[0]+(Sector.sizePixels())
                , parent.worldCoords()[1]+(Sector.sizePixels()/2)};
        if (r == 0) end = new int[]{parent.worldCoords()[0]+(Sector.sizePixels()/2)
                , parent.worldCoords()[1]+Sector.sizePixels()};
        if (r == 0) end = new int[]{parent.worldCoords()[0]
                , parent.worldCoords()[1]+Sector.sizePixels()};
        
        int detail = 10, d = Math.abs(parent.getWorld().rng().nextInt() % 4)+2;
        
        for (int i = 0; i <= detail+1; i++) {
            Generator.brush(last[0], last[1], d*Chunk.sizePixels(), Chunk.WATER);
            last[0] = last[0] + (parent.getWorld().rng().nextInt() % 100) 
                    + (end[0] > last[0] ? 50 : -50);
            last[1] = last[1] + (parent.getWorld().rng().nextInt() % 100) 
                    + (end[1] > last[1] ? 50 : -50);
            if (i == detail) last = end;
            
        }
        
    }

}
