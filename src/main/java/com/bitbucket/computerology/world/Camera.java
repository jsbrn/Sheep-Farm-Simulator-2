package com.bitbucket.computerology.world;

public class Camera {
    
    private static double x = 0, y = 0;
    private static int zoom = 1;
    
    public static int getX() { return (int)x; }
    public static int getY() { return (int)y; }
    public static void setX(int x) { Camera.x = x; }
    public static void setY(int y) { Camera.y = y; }
    public static int getZoom() { return zoom; }
    
    public static void zoom(int delta) { 
        zoom += delta;
        if (zoom < 1) zoom = 1;
        if (zoom > 3) zoom = 1;
    }
    
    public static void reset() {
        zoom = 1;
        x = 0;
        y = 0;
    }
    
    public static void move(double dx, double dy) {
        if (dx == 0 && dy == 0) return;
        x+=dx; y+=dy;
        World.getWorld().generateAround((int)x, (int)y);
    }
}
