package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;

import java.util.ArrayList;

public class Hitbox extends Component {

    boolean collides;
    int width, height;
    double offset_x, offset_y;

    ArrayList<int[]> lines, rotated_lines;

    public Hitbox() {
        this.width = 0;
        this.height = 0;
        this.offset_x = 0;
        this.offset_y = 0;
        this.lines = new ArrayList<int[]>();
        this.rotated_lines = new ArrayList<int[]>();
    }

    /**
     * Recalculates the rotated hitbox, including its width and height.
     */
    public void refresh() {
        rotated_lines.clear();
        int lx = Integer.MAX_VALUE, ly = Integer.MAX_VALUE, ux = -Integer.MAX_VALUE, uy = -Integer.MAX_VALUE;
        for (int[] l : lines) {
            int[] p1 = MiscMath.getRotatedOffset(l[0], l[1], getParent().getRotation());
            int[] p2 = MiscMath.getRotatedOffset(l[2], l[3], getParent().getRotation());
            lx = (int) MiscMath.min(lx, MiscMath.min(p1[0], p2[0]));
            ly = (int) MiscMath.min(ly, MiscMath.min(p1[1], p2[1]));
            ux = (int) MiscMath.max(ux, MiscMath.max(p1[0], p2[0]));
            uy = (int) MiscMath.max(uy, MiscMath.max(p1[1], p2[1]));
            rotated_lines.add(new int[]{p1[0], p1[1], p2[0], p2[1]});
        }
        width = ux - lx;
        height = uy - ly;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void offset(double x, double y) {
        offset_x = x;
        offset_y = y;
    }

    public boolean intersectsLine(int x1, int y1, int x2, int y2) {
        for (int[] l1 : rotated_lines) {
            if (MiscMath.linesIntersect(
                    l1[0] + getParent().getWorldX() + offset_x,
                    l1[1] + getParent().getWorldY() + offset_y,
                    l1[2] + getParent().getWorldX() + offset_x,
                    l1[3] + getParent().getWorldY() + offset_y, x1, y1, x2, y2)) {
                return true;
            }
        }
        if (intersects(x1, y1)) return true;
        if (intersects(x2, y2)) return true;
        return false;
    }

    public boolean intersects(double wx, double wy) {
        if (!MiscMath.pointIntersectsRect(wx, wy,
                getParent().getWorldX() - (width / 2), getParent().getWorldY() - (height / 2), width, height))
            return false;
        int count = 0;
        //if odd, intersecting. even, no.
        for (int[] l : rotated_lines) {
            count += MiscMath.linesIntersect(wx, wy, Integer.MAX_VALUE, wy + 1,
                    l[0] + getParent().getWorldX() + offset_x,
                    l[1] + getParent().getWorldY() + offset_y,
                    l[2] + getParent().getWorldX() + offset_x,
                    l[3] + getParent().getWorldY() + offset_y) ? 1 : 0;
        }
        return count % 2 == 1 && count != 0;
    }

    public boolean intersects(Entity e) {
        if (e == null) return false;
        return intersects(e.getHitbox());
    }

    public boolean intersects(Hitbox h) {
        if (h == null) return false;
        if (!MiscMath.rectanglesIntersect(
                h.getParent().getWorldX() - (h.width / 2), h.getParent().getWorldY() - (h.height / 2), h.width, h.height,
                getParent().getWorldX() - (width / 2), getParent().getWorldY() - (height / 2), width, height))
            return false;
        //check if any of the lines intersect
        for (int[] l1 : rotated_lines) {
            for (int[] l2 : h.rotated_lines) {
                if (MiscMath.linesIntersect(
                        l1[0] + getParent().getWorldX() + offset_x,
                        l1[1] + getParent().getWorldY() + offset_y,
                        l1[2] + getParent().getWorldX() + offset_x,
                        l1[3] + getParent().getWorldY() + offset_y,
                        l2[0] + h.getParent().getWorldX() + h.offset_x,
                        l2[1] + h.getParent().getWorldY() + h.offset_y,
                        l2[2] + h.getParent().getWorldX() + h.offset_x,
                        l2[3] + h.getParent().getWorldY() + h.offset_y)) {
                    return true;
                }
            }
        }
        for (int[] l1 : rotated_lines) {
            if (h.intersects(l1[0] + getParent().getWorldX() + offset_x,
                    l1[1] + getParent().getWorldY() + offset_y)) return true;
            if (h.intersects(l1[2] + getParent().getWorldX() + offset_x,
                    l1[3] + getParent().getWorldY() + offset_y)) return true;
        }
        return false;
    }

    public boolean intersects(double x, double y, int w, int h) {
        if (!MiscMath.rectanglesIntersect(
                x, y, w, h,
                getParent().getWorldX() - (width / 2), getParent().getWorldY() - (height / 2), width, height))
            return false;
        Entity e = getParent();
        for (int[] l : rotated_lines) {
            if (l.length != 4) continue;
            if (MiscMath.rectContainsLine(x, y, w, h, e.getWorldX() + l[0] + offset_x,
                    e.getWorldY() + l[1] + offset_y, e.getWorldX() + l[2] + offset_x, e.getWorldY() + l[3] + offset_y))
                return true;
        }
        for (int[] l : rotated_lines) {
            if (l.length != 4) continue;
            if (MiscMath.pointIntersectsRect(e.getWorldX() + l[0] + offset_x,
                    e.getWorldY() + l[1] + offset_y, x, y, w, h)) return true;
            if (MiscMath.pointIntersectsRect(e.getWorldX() + l[2] + offset_x,
                    e.getWorldY() + l[3] + offset_y, x, y, w, h)) return true;
        }
        if (intersects(x, y)) return true;
        if (intersects(x + w, y)) return true;
        if (intersects(x, y + h)) return true;
        if (intersects(x + w, y + h)) return true;
        return false;
    }

    public void addLine(int x, int y, int x2, int y2) {
        lines.add(new int[]{x, y, x2, y2});
        refresh();
    }

    @Override
    public void initParams(ArrayList<String> params) {
        for (String p : params)
            if (p.indexOf("collides=") == 0)
                collides = Boolean.parseBoolean(p.replace("collides=", "").trim());
    }

    @Override
    public void copyTo(Component c) {
        ((Hitbox) c).width = width;
        ((Hitbox) c).height = height;
        ((Hitbox) c).collides = collides;
        ((Hitbox) c).lines.clear();
        for (int[] l : lines) ((Hitbox) c).lines.add(new int[]{l[0], l[1], l[2], l[3]});
        for (int[] l : rotated_lines) ((Hitbox) c).rotated_lines.add(new int[]{l[0], l[1], l[2], l[3]});
    }

    public int lineCount() {
        return lines.size();
    }

    public int[] getLine(int index) {
        return index > -1 && index < lines.size() ? lines.get(index) : null;
    }

}