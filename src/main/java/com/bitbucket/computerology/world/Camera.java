package com.bitbucket.computerology.world;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Entity;

public class Camera {

    private static int target_coords[];
    private static Entity target_entity;


    private static double x = 0, y = 0;
    private static double zoom = 1, speed = 5;

    public static int getX() {
        return (int) x;
    }

    public static void setX(int x) {
        Camera.x = x;
    }

    public static int getY() {
        return (int) y;
    }

    public static void setY(int y) {
        Camera.y = y;
    }

    public static int getZoom() {
        return (int)zoom;
    }

    public static void setZoom(int z) {
        zoom = z < 1 || z > 3 ? 1 : z;
    }

    /**
     * Sets the speed in pixels per second.
     * @param s
     */
    public static void setSpeed(int s) { speed = s >= 0 ? s : 0; }

    public static void zoom(int delta) {
        setZoom((int)(zoom + delta));
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
                double vel[] = MiscMath.calculateVelocity(target[0] - (int) x, target[1] - (int) y,
                        MiscMath.getConstant(
                                (speed / zoom) * dist,
                                1));
                x += vel[0];
                y += vel[1];
            }
        }
    }

    public static void move(double dx, double dy) {
        if (dx == 0 && dy == 0) return;
        target_coords = null;
        target_entity = null;
        x += dx;
        y += dy;
        World.getWorld().generateAround((int) x, (int) y);
    }
}
