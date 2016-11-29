package com.bitbucket.computerology.world.terrain.generators;

import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;

public class WaterBiomeGen extends Generator {
    
    
    public WaterBiomeGen(Sector s) { super(s); }
    
    @Override
    public void generate() {
        Sector[] adj = parent.getAdjacentSectors();
        for (int i = 0; i < adj.length; i++) {
            Sector s = adj[i];
            if (s != null) {
                if (s.getBiome() == Chunk.GRASS_FIELD) {
                    int wx = -1, wy = -1;
                    if (i == 1) {
                        wx = parent.worldCoords()[0]+(Sector.sizePixels()/2);
                        wy = parent.worldCoords()[1];
                    }
                    if (i == 4) {
                        wx = parent.worldCoords()[0]+(Sector.sizePixels());
                        wy = parent.worldCoords()[1]+(Sector.sizePixels()/2);
                    }
                    if (i == 6) {
                        wx = parent.worldCoords()[0]+(Sector.sizePixels()/2);
                        wy = parent.worldCoords()[1]+(Sector.sizePixels());
                    }
                    if (i == 3) {
                        wx = parent.worldCoords()[0];
                        wy = parent.worldCoords()[1]+(Sector.sizePixels()/2);
                    }
                    ((GrassBiomeGen)s.generator()).markAsRiver(wx, wy);
                }
            }
        }
    }

}
