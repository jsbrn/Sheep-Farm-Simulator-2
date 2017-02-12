package com.bitbucket.computerology.world;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Entity;

public class Camera {

    private static int target_coords[];
    private static Entity target_entity;


    private static double x = 0, y = 0;
    private static double zoom = 1, speed = 5;

    /**
     * Sets camera focus speed.
     */
    public static void setSpeed(int s) { speed = s >= 0 ? s : 0; }

    public static double getX() {
        return x;
    }

    public static void setX(int x) {
        Camera.x = x;
    }

    public static double getY() {
        return y;
    }

    public static void setY(int y) {
        Camera.y = y;
    }

    public static double getZoom() {
        return zoom;
    }

    public static void setZoom(int z) {
        zoom = z;
        if (zoom > 3) zoom = 3;
        if (zoom < 1) zoom = 1;
    }

    public static void addZoom(int z) {
        setZoom((int)getZoom()+z);
    }

    public static void zoomAt(int osx, int osy, int amount) {
        double[] wc_old = MiscMath.getWorldCoords(osx, osy);
        addZoom(amount);
        double[] wc_new = MiscMath.getWorldCoords(osx, osy);
        x += wc_old[0] - wc_new[0];
        y += wc_old[1] - wc_new[1];
    }

    public static void reset() {
        zoom = 1;
        x = 0;
        y = 0;
    }

    public static void setTarget(int wx, int wy) {
        target_coords = new int[]{wx, wy};
    }

    public static Entity getTarget() { return target_entity; }

    public static void setTarget(Entity e) {
        target_entity = e;
    }

    public static void update() {
        int[] target = target_coords;
        if (target_entity != null) target
                = new int[]{target_entity.getWorldX(), target_entity.getWorldY()};
        if (target != null) {
            double dist = MiscMath.distance(x, y, target[0], target[1]);
            if (dist > 5) {
                double vel[] = MiscMath.calculateVelocity((int)(target[0]-x), (int)(target[1]-y));
                x += MiscMath.getConstant(vel[0]*speed*dist, 1);
                y += MiscMath.getConstant(vel[1]*speed*dist, 1);
            }
        }
    }

    public static void drag(double dx, double dy) {
        if (dx == 0 && dy == 0) return;
        target_coords = null;
        target_entity = null;
        x += dx;
        y += dy;
        World.getWorld().generateAround((int) x, (int) y);
    }
}
