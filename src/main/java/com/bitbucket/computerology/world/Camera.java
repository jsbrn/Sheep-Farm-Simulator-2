package com.bitbucket.computerology.world;

import com.bitbucket.computerology.gui.states.GameScreen;

public class Camera {
    
    private static double x = Chunk.SIZE_PIXELS/2, y = Chunk.SIZE_PIXELS/2;
    private static int zoom = 2;
    
    public static int getX() {
        return (int)x;
    }
    
    public static int getY() {
        return (int)y;
    }
    
    public static void move(double dx, double dy) {
        if (dx == 0 && dy == 0) return;
        x+=dx; y+=dy;
        World.getWorld().generateAround((int)x, (int)y);
    }
}
