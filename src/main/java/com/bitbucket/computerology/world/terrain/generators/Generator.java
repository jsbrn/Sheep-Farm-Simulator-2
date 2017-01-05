package com.bitbucket.computerology.world.terrain.generators;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;

public class Generator {
    
    Sector parent;

    public static Generator create(Sector s) {
        if (s == null) return null;
        if (s.getBiome() == Chunk.GRASS_FIELD) return new GrassBiomeGen(s);
        if (s.getBiome() == Chunk.WATER) return new WaterBiomeGen(s);
        return null;
    }
    
    public Generator(Sector s) { this.parent = s; }
    
    public void generate() {}
    
    /**
     * Applies a circular brush to the terrain.
     * @param os_x Center of the terrain circle. On-screen coords.
     * @param os_y Center of the terrain circle.
     * @param diametre The diametre of the circle.
     * @param terrain Terrain type to paint.
     * @param overwrite Overwrite chunks with existing terrain data?
     */
    public static void brush(int wx, int wy, int diametre, int terrain) {
        for (int w = (-diametre/2); w != (diametre/2); w++) {
            for (int h = (-diametre/2); h != (diametre/2); h++) {
                if (MiscMath.distance(w, h, 0, 0) <= (diametre/2)) {
                    int sc[] = World.getWorld().getSectorCoords(wx+(w*Chunk.sizePixels()), wy+(h*Chunk.sizePixels()));
                    int cc[] = World.getWorld().getChunkCoords(wx+(w*Chunk.sizePixels()), wy+(h*Chunk.sizePixels()));
                    Sector s = World.getWorld().getSector(sc[0], sc[1]);
                    Chunk c = (s != null) ? s.getChunk(cc[0], cc[1]) : null;
                    if (c != null) c.setTerrain(terrain);
                }
            }
        }
    }
    
}
