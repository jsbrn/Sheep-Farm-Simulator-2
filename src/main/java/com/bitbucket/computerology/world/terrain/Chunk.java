package com.bitbucket.computerology.world.terrain;

import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chunk {

    public static byte BIOME_COUNT = 7,
            NULL = 0, GRASS = 1, SAND = 2, WATER = 3, SNOW = 4, ROAD_INTERSECTION = 5, ROAD_STRAIGHT = 6;
    public static Color[] COLORS = {Color.black, Color.green, Color.yellow,
            Color.blue.brighter(), Color.white,
            Color.gray.darker(), Color.gray.darker()};
    public ArrayList<Entity> entities;
    private byte biome, terrain, x, y, rot;
    private Sector parent;

    public Chunk(byte x, byte y, Sector parent) {
        this.rot = (byte) Math.abs(new Random().nextInt() % 4);
        this.biome = -1;
        this.terrain = -1;
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.entities = new ArrayList<Entity>();
    }

    public static int sizePixels() {
        return 32;
    }

    public static int onScreenSize() {
        return sizePixels() * (int)Camera.getZoom();
    }

    public int getRotation() {
        return rot;
    }

    /**
     * Sets the rotation index to 0 through 3 (0 being up, 1 being right).
     *
     * @param r A number from 0-3. Sets the rotation index to the absolute value of r mod 4.
     */
    public void setRotation(int r) {
        rot = (byte) Math.abs(r % 4);
    }

    public boolean addEntity(Entity e) {
        if (!entities.contains(e)) return entities.add(e);
        return false;
    }

    public boolean removeEntity(Entity e) {
        return entities.remove(e);
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    /**
     * Returns a Chunk array of size 8. Traverses horizontally first, then vertically.
     * That is: <br><br>
     * 0 1 2<br>
     * 3 x 4<br>
     * 5 6 7<br>
     *
     * @param diag Whether or not to include chunks diagonal to the parent.
     * @return A Chunk[] array. Elements can be null if no chunk was found from the slot.
     */
    public Chunk[] getAdjacentChunks(boolean diag) {
        Chunk d[] = new Chunk[]{null, null, null, null, null, null, null, null};
        int i = 0, s_x, s_y;
        Sector s;
        for (int h = -1; h != 2; h++) {
            for (int w = -1; w != 2; w++) {
                if ((w == 0 && h == 0)) continue;
                s_x = (x + w < 0 ? -1 : (x + w >= Sector.sizeChunks() ? 1 : 0));
                s_y = (y + h < 0 ? -1 : (y + h >= Sector.sizeChunks() ? 1 : 0));
                //if s_x and s_y are the parent sector's coords, then s = parent
                //else, s is the sector at (parent x + s_x, parent y + s_y)
                s = (s_x + w == parent.getSectorCoords()[0] && s_y + h == parent.getSectorCoords()[1] ? parent :
                        World.getWorld().getSector(parent.getSectorCoords()[0] + s_x, parent.getSectorCoords()[1] + s_y));
                //the Ith element from d is the chunk (cx, cy) from s, where (cx, cy) is the chunk coordinates relative
                //to this chunk's parent sector's origin, mod Sector.SIZE (which right now is 32)
                d[i] = (s != null ? s.getChunk((x + w + Sector.sizeChunks()) % Sector.sizeChunks(),
                        (y + h + Sector.sizeChunks()) % Sector.sizeChunks()) : null);
                if (((w != 0 && h != 0) && !diag)) d[i] = null;
                i++;
            }
        }
        return d;
    }

    public Sector getSector() {
        return parent;
    }

    public int getTopLayer() { return getTerrain() == 0 ? getBiome() : getTerrain(); }

    public int getTerrain() {
        return (terrain > -1 && terrain < BIOME_COUNT) ? terrain : 0;
    }

    public void setTerrain(byte terrain) {
        this.terrain = ((terrain > -1 && terrain < BIOME_COUNT) ? terrain : 0);
    }

    public int getBiome() {
        return biome;
    }

    public void setBiome(byte biome) {
        this.biome = ((biome > -1 && biome < BIOME_COUNT) ? biome : 0);
    }

    public int[] offsets() {
        return new int[]{x, y};
    }

    public int[] worldCoords() {
        return new int[]{parent.getWorldCoords()[0] + (x * sizePixels()), parent.getWorldCoords()[1] + (y * sizePixels())};
    }

    public int[] onScreenCoords() {
        return MiscMath.getOnscreenCoords(worldCoords()[0], worldCoords()[1]);
    }

    public boolean intersects(double x, double y, int w, int h) {
        return MiscMath.rectanglesIntersect(worldCoords()[0],
                worldCoords()[1], Chunk.sizePixels(), Chunk.sizePixels(), x, y, w, h);
    }

    public boolean load(BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.replace("", "").trim();
                if (line.equals("/c")) return true;
                if (line.indexOf("x=") == 0) x = Byte.parseByte(line.replace("x=", ""));
                if (line.indexOf("y=") == 0) y = Byte.parseByte(line.replace("y=", ""));
                if (line.indexOf("b=") == 0) biome = Byte.parseByte(line.replace("b=", ""));
                if (line.indexOf("t=") == 0) terrain = Byte.parseByte(line.replace("t=", ""));
                if (line.indexOf("r=") == 0) rot = Byte.parseByte(line.replace("r=", ""));
            }
        } catch (IOException ex) {
            Logger.getLogger(Sector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void save(BufferedWriter bw) {
        try {
            bw.write("c\n");
            bw.write("x=" + x + "\n");
            bw.write("y=" + y + "\n");
            bw.write("b=" + getBiome() + "\n");
            bw.write("t=" + getTerrain() + "\n");
            bw.write("r=" + getRotation() + "\n");
            bw.write("/c\n");
        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void draw(boolean corners, Graphics g) {

        if (getTopLayer() < 0 || getTopLayer() >= Chunk.BIOME_COUNT) return;
        Image img = Assets.getTerrainSprite(corners);

        int src_x = Chunk.onScreenSize() * getTopLayer();

        int r = (rot * Chunk.onScreenSize());
        int rx = onScreenCoords()[0];
        int ry = onScreenCoords()[1];

        if (corners) {
            int draw[] = {-1, -1, -1, -1};
            Chunk[] adj = getAdjacentChunks(false);
            if (adj[1] != null && adj[3] != null) {
                if (adj[1].getTopLayer() == adj[3].getTopLayer()) draw[0] = adj[1].getTopLayer();
            }
            if (adj[1] != null && adj[4] != null) {
                if (adj[1].getTopLayer() == adj[4].getTopLayer()) draw[1] = adj[1].getTopLayer();
            }
            if (adj[4] != null && adj[6] != null) {
                if (adj[4].getTopLayer() == adj[6].getTopLayer()) draw[2] = adj[4].getTopLayer();
            }
            if (adj[6] != null && adj[3] != null) {
                if (adj[6].getTopLayer() == adj[3].getTopLayer()) draw[3] = adj[6].getTopLayer();
            }
            for (int i = 0; i < 4; i++) {
                if (draw[i] > -1 && draw[i] < Chunk.BIOME_COUNT && draw[i] != getTopLayer()) {
                    src_x = Chunk.onScreenSize() * draw[i];
                    r = (i * Chunk.onScreenSize());
                    img.drawEmbedded(rx, ry, rx + Chunk.onScreenSize(), ry + Chunk.onScreenSize(),
                            src_x, r, src_x + Chunk.onScreenSize(), r + Chunk.onScreenSize());
                }
            }
        } else {
            img.drawEmbedded(rx, ry, rx + Chunk.onScreenSize(), ry + Chunk.onScreenSize(),
                    src_x, r, src_x + Chunk.onScreenSize(), r + Chunk.onScreenSize());
        }

    }

    @Override
    public String toString() {
        return "chunk[" + x + ", " + y + "]";
    }

}
