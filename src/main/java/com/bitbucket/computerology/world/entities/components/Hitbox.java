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
    }
    
    public boolean intersects(double wx, double wy) {
        double[] line = {wx, wy, Integer.MAX_VALUE, wy};
        int count = 0;
        //if odd, intersecting. even, no.
        for (int[] l: lines) {
            count += MiscMath.linesIntersect(wx, wy, Integer.MAX_VALUE, wy, 
                    l[0]+getParent().getWorldX()+offset_x, 
                    l[1]+getParent().getWorldY()+offset_y, 
                    l[2]+getParent().getWorldX()+offset_x, 
                    l[3]+getParent().getWorldY()+offset_y) ? 1 : 0;
        }
        return count % 2 == 1 && count != 0;
    }
    
    public boolean intersects(Entity e) {
        if (e == null) return false;
        Hitbox h2;
        Component c = e.getComponent("Hitbox");
        if (c != null) h2 = ((Hitbox)c); else h2 = null;
        if (h2 == null) return false;
        return intersects(h2);
    }
    
    public boolean intersects(Hitbox h) {
        //check if any of the lines intersect
        for (int[] l1: lines) {
            for (int[] l2: h.lines) {
                if (MiscMath.linesIntersect(
                        l1[0]+getParent().getWorldX()+offset_x, 
                    l1[1]+getParent().getWorldY()+offset_y, 
                    l1[2]+getParent().getWorldX()+offset_x, 
                    l1[3]+getParent().getWorldY()+offset_y, 
                        l2[0]+h.getParent().getWorldX()+h.offset_x, 
                    l2[1]+h.getParent().getWorldY()+h.offset_y, 
                    l2[2]+h.getParent().getWorldX()+h.offset_x, 
                    l2[3]+h.getParent().getWorldY()+h.offset_y)) {
                    return true;
                }
            }
        }
        for (int[] l1: lines) {
            if (h.intersects(l1[0]+getParent().getWorldX()+offset_x, 
                    l1[1]+getParent().getWorldY()+offset_y)) return true;
            if (h.intersects(l1[2]+getParent().getWorldX()+offset_x, 
                    l1[3]+getParent().getWorldY()+offset_y)) return true;
        }
        return false;
    }
    
    public boolean intersects(int x, int y, int w, int h) {
        Entity e = getParent();
        for (int[] l: lines) {
            if (l.length != 4) continue;
            if (MiscMath.rectContainsLine(x, y, w, h, e.getWorldX()+l[0], 
                    e.getWorldY()+l[1], e.getWorldX()+l[2], e.getWorldY()+l[3])) return true;
        }
        return false;
    }
    
    public void addLine(int x, int y, int x2, int y2) {
        lines.add(new int[]{x, y, x2, y2});
    }
    
    @Override
    public void initParams(ArrayList<String> params) {
        for (String p: params) if (p.indexOf("collides=") == 0)
            collides = Boolean.parseBoolean(p.replace("collides=", "").trim());
    }
    
    @Override
    public void copyTo(Component c) {
        ((Hitbox)c).width = width;
        ((Hitbox)c).height = height;
        ((Hitbox)c).collides = collides;
        ((Hitbox)c).lines.clear();
        for (int[] l: lines) ((Hitbox)c).lines.add(new int[]{l[0], l[1], l[2], l[3]});
    }
    
    public int lineCount() {
        return lines.size();
    }
    
    public int[] getLine(int index) {
        return index >= 0 && index < lines.size() ? lines.get(index) : null;
    }
    
}