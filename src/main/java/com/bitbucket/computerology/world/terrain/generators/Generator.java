package com.bitbucket.computerology.world.terrain.generators;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;

public class Generator {

    Sector parent;

    public Generator(Sector s) {
        this.parent = s;
    }

    public static Generator create(Sector s) {
        if (s == null) return null;
        return null;
    }

    /**
     * Applies a circular brush to the terrain.
     *
     * @param diametre The diametre of the circle.
     * @param terrain  Terrain type to paint.
     * @param biome    Do you want to paint the chunk's biome or the terrain?
     */
    public static void brush(int wx, int wy, int diametre, byte terrain, boolean biome) {
        for (int w = (-diametre / 2); w != (diametre / 2); w++) {
            for (int h = (-diametre / 2); h != (diametre / 2); h++) {
                if (MiscMath.distance(w, h, 0, 0) <= (diametre / 2)) {
                    int sc[] = MiscMath.getSectorCoords(wx + (w * Chunk.sizePixels()), wy + (h * Chunk.sizePixels()));
                    int cc[] = MiscMath.getChunkCoords(wx + (w * Chunk.sizePixels()), wy + (h * Chunk.sizePixels()));
                    Sector s = World.getWorld().getSector(sc[0], sc[1]);
                    Chunk c = (s != null) ? s.getChunk(cc[0], cc[1]) : null;
                    if (c != null) {
                        if (biome) c.setBiome(terrain);
                        else c.setTerrain(terrain);
                    }
                }
            }
        }
    }

    public void generateObjects() {
    }

    public void generateTerrain() {
    }

}
