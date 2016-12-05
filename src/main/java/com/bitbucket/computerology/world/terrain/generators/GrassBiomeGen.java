package com.bitbucket.computerology.world.terrain.generators;

import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.Force;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;

public class GrassBiomeGen extends Generator {
    
    int[] river_coords = {-1, -1};
    
    public GrassBiomeGen(Sector s) { super(s); }
    
    @Override
    public void generate() {
        //if (isRiver()) createRiver();
        
        int c_count = Math.abs(parent.getWorld().rng().nextInt() % 5);
        for (int i = 0; i < c_count; i++) treeCluster();
        
    }
    
    public void treeCluster() {
        int rx = Math.abs(parent.getWorld().rng().nextInt() % Sector.sizePixels());
        int ry = Math.abs(parent.getWorld().rng().nextInt() % Sector.sizePixels());
        int rr = Sector.sizePixels()/8;
        int rd = Math.abs(parent.getWorld().rng().nextInt() % 50);
        
        for (int i = 0; i < rd; i++) {
            int rx2 = rx+(parent.getWorld().rng().nextInt() % rr);
            int ry2 = ry+(parent.getWorld().rng().nextInt() % rr);
            Entity tree = Entity.create("Tree");
            Force f = new Force("move");
            f.setXVelocity(1);
            tree.addForce(f);
            tree.setWorldX(parent.worldCoords()[0]+rx2);
            tree.setWorldY(parent.worldCoords()[1]+ry2);
            parent.getWorld().addEntity(tree);
        }
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
        System.out.println("Sector "+parent.offsets()[0]+", "+parent.offsets()[1]+" is river: creating.");
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
        
        Generator.brush(last[0], last[1], 6, Chunk.WATER);
        /*
        for (int i = 0; i <= detail+1; i++) {
            Generator.brush(last[0], last[1], d*Chunk.sizePixels(), Chunk.WATER);
            last[0] = last[0] + (parent.getWorld().rng().nextInt() % 100) 
                    + (end[0] > last[0] ? 50 : -50);
            last[1] = last[1] + (parent.getWorld().rng().nextInt() % 100) 
                    + (end[1] > last[1] ? 50 : -50);
            if (i == detail) last = end;
            
        }*/
        
    }

}
