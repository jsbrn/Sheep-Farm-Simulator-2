package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;

import java.util.ArrayList;

public class Hitbox extends Component {

    public static final int COLLISION_BOX = 0, CLICK_BOX = 1;

    boolean collides;
    int dims[][]; //[index][w/h]
    double offsets[][];

    ArrayList<int[]> lines[], rotated_lines[];

    public Hitbox() {
        this.dims = new int[][]{{0, 0}, {0, 0}};
        this.offsets = new double[][]{{0, 0}, {0, 0}};
        this.lines = new ArrayList[]{new ArrayList<int[]>(), new ArrayList<int[]>()};
        this.rotated_lines = new ArrayList[]{new ArrayList<int[]>(), new ArrayList<int[]>()};
    }

    /**
     * Recalculates the rotated hitbox, including its width and height.
     */
    public void refresh() {
        for (int i = 0; i < lines.length; i++) {
            rotated_lines[i].clear();
            int lx = Integer.MAX_VALUE, ly = Integer.MAX_VALUE, ux = -Integer.MAX_VALUE, uy = -Integer.MAX_VALUE;
            for (int[] l : lines[i]) {
                int[] p1 = MiscMath.getRotatedOffset(l[0], l[1], getParent().getRotation());
                int[] p2 = MiscMath.getRotatedOffset(l[2], l[3], getParent().getRotation());
                lx = (int) MiscMath.min(lx, MiscMath.min(p1[0], p2[0]));
                ly = (int) MiscMath.min(ly, MiscMath.min(p1[1], p2[1]));
                ux = (int) MiscMath.max(ux, MiscMath.max(p1[0], p2[0]));
                uy = (int) MiscMath.max(uy, MiscMath.max(p1[1], p2[1]));
                rotated_lines[i].add(new int[]{p1[0], p1[1], p2[0], p2[1]});
            }
            dims[i][0] = ux - lx;
            dims[i][1] = uy - ly;
        }
    }

    public int getWidth(int box_id) {
        return dims[box_id][0];
    }

    public int getHeight(int box_id) {
        return dims[box_id][1];
    }

    public void offset(double x, double y, int box_id) {
        offsets[box_id][0] = x;
        offsets[box_id][1] = y;
    }

    public boolean intersectsLine(int x1, int y1, int x2, int y2, int box_id) {
        for (int[] l1 : rotated_lines[box_id]) {
            if (MiscMath.linesIntersect(
                    l1[0] + getParent().getWorldX() + offsets[box_id][0],
                    l1[1] + getParent().getWorldY() + offsets[box_id][1],
                    l1[2] + getParent().getWorldX() + offsets[box_id][0],
                    l1[3] + getParent().getWorldY() + offsets[box_id][1], x1, y1, x2, y2)) {
                return true;
            }
        }
        if (intersects(x1, y1, Hitbox.COLLISION_BOX)) return true;
        if (intersects(x2, y2, Hitbox.COLLISION_BOX)) return true;
        return false;
    }

    public boolean intersects(double wx, double wy, int box_id) {
        if (!MiscMath.pointIntersectsRect(wx, wy,
                getParent().getWorldX() - (dims[box_id][0] / 2), getParent().getWorldY()
                        - (dims[box_id][1] / 2), dims[box_id][0], dims[box_id][1]))
            return false;
        int count = 0;
        //if odd, intersecting. even, no.
        for (int[] l : rotated_lines[box_id]) {
            count += MiscMath.linesIntersect(wx, wy, Integer.MAX_VALUE, wy + 1,
                    l[0] + getParent().getWorldX() + offsets[box_id][0],
                    l[1] + getParent().getWorldY() + offsets[box_id][1],
                    l[2] + getParent().getWorldX() + offsets[box_id][0],
                    l[3] + getParent().getWorldY() + offsets[box_id][1]) ? 1 : 0;
        }
        return count % 2 == 1 && count != 0;
    }

    public boolean intersects(Entity e) {
        if (e == null) return false;
        return intersects(e.getHitbox(), Hitbox.COLLISION_BOX);
    }

    public boolean intersects(Hitbox h, int box_id) {
        if (h == null) return false;
        if (!MiscMath.rectanglesIntersect(
                h.getParent().getWorldX() - (h.dims[box_id][0] / 2), h.getParent().getWorldY()
                        - (h.dims[box_id][1] / 2), h.dims[box_id][0], h.dims[box_id][1],
                getParent().getWorldX() - (dims[box_id][1] / 2), getParent().getWorldY() - (dims[box_id][1] / 2),
                dims[box_id][1], dims[box_id][1]))
            return false;
        //check if any of the lines intersect
        for (int[] l1 : rotated_lines[box_id]) {
            for (int[] l2 : h.rotated_lines[box_id]) {
                if (MiscMath.linesIntersect(
                        l1[0] + getParent().getWorldX() + offsets[box_id][0],
                        l1[1] + getParent().getWorldY() + offsets[box_id][1],
                        l1[2] + getParent().getWorldX() + offsets[box_id][0],
                        l1[3] + getParent().getWorldY() + offsets[box_id][1],
                        l2[0] + h.getParent().getWorldX() + h.offsets[box_id][0],
                        l2[1] + h.getParent().getWorldY() + h.offsets[box_id][1],
                        l2[2] + h.getParent().getWorldX() + h.offsets[box_id][0],
                        l2[3] + h.getParent().getWorldY() + h.offsets[box_id][1])) {
                    return true;
                }
            }
        }
        for (int[] l1 : rotated_lines[box_id]) {
            if (h.intersects(l1[0] + getParent().getWorldX() + offsets[box_id][0],
                    l1[1] + getParent().getWorldY() + offsets[box_id][1], Hitbox.COLLISION_BOX)) return true;
            if (h.intersects(l1[2] + getParent().getWorldX() + offsets[box_id][0],
                    l1[3] + getParent().getWorldY() + offsets[box_id][1], Hitbox.COLLISION_BOX)) return true;
        }
        return false;
    }

    public boolean intersects(double x, double y, int w, int h, int box_id) {
        if (!MiscMath.rectanglesIntersect(
                x, y, w, h,
                getParent().getWorldX() - (dims[box_id][0] / 2), getParent().getWorldY()
                        - (dims[box_id][1] / 2), dims[box_id][0], dims[box_id][1]))
            return false;
        Entity e = getParent();
        for (int[] l : rotated_lines[box_id]) {
            if (l.length != 4) continue;
            if (MiscMath.rectContainsLine(x, y, w, h, e.getWorldX() + l[0] + offsets[box_id][0],
                    e.getWorldY() + l[1] + offsets[box_id][1], e.getWorldX() + l[2] + offsets[box_id][0], e.getWorldY() + l[3] + offsets[box_id][1]))
                return true;
        }
        for (int[] l : rotated_lines[box_id]) {
            if (l.length != 4) continue;
            if (MiscMath.pointIntersectsRect(e.getWorldX() + l[0] + offsets[box_id][0],
                    e.getWorldY() + l[1] + offsets[box_id][1], x, y, w, h)) return true;
            if (MiscMath.pointIntersectsRect(e.getWorldX() + l[2] + offsets[box_id][0],
                    e.getWorldY() + l[3] + offsets[box_id][1], x, y, w, h)) return true;
        }
        if (intersects(x, y, Hitbox.COLLISION_BOX)) return true;
        if (intersects(x + w, y, Hitbox.COLLISION_BOX)) return true;
        if (intersects(x, y + h, Hitbox.COLLISION_BOX)) return true;
        if (intersects(x + w, y + h, Hitbox.COLLISION_BOX)) return true;
        return false;
    }

    public void addLine(int x, int y, int x2, int y2, int box_id) {
        lines[box_id].add(new int[]{x, y, x2, y2});
        refresh();
    }

    @Override
    public void initParams(ArrayList<String> params) {
        for (String p : params)
            if (p.indexOf("collides=") == 0)
                collides = Boolean.parseBoolean(p.replace("soft=", "").trim());
    }

    @Override
    public void copyTo(Component c) {
        for (int i = 0; i < 2; i++) {
            ((Hitbox) c).dims[i][0] = dims[i][0];
            ((Hitbox) c).dims[i][1] = dims[i][1];
            ((Hitbox) c).collides = collides;
            ((Hitbox) c).lines[i].clear();
            for (int[] l : lines[i]) ((Hitbox) c).lines[i].add(new int[]{l[0], l[1], l[2], l[3]});
            for (int[] l : rotated_lines[i]) ((Hitbox) c).rotated_lines[i].add(new int[]{l[0], l[1], l[2], l[3]});
        }
    }

    public int lineCount(int box_id) {
        return lines[box_id].size();
    }

    public int[] getLine(int index, int box_id) {
        return index > -1 && index < lines[box_id].size() ? lines[box_id].get(index) : null;
    }

}