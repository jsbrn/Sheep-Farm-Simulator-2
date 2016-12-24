package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import java.util.ArrayList;
import org.newdawn.slick.Graphics;

public class Hitbox extends Component {
    
    boolean collides;
    int width, height;
    
    ArrayList<int[]> lines;
    
    public Hitbox() {
        this.width = 0;
        this.height = 0;
        this.lines = new ArrayList<int[]>();
    }
    
    public boolean intersects(Entity e) {
        Hitbox h2;
        Component c = e.getComponent("Hitbox");
        if (c != null) h2 = ((Hitbox)c); else h2 = null;
        if (h2 == null) return false;
        for (int[] l1: lines) {
            for (int[] l2: h2.lines) {
                if (MiscMath.linesIntersect(l1, l2)) {
                    
                }
            }
        }
    }
    
    public boolean intersects(int x, int y, int w, int h) {
        Entity e = getParent();
        for (int[] l: lines) {
            if (l.length != 4) continue;
            if (MiscMath.rectangleIntersectsLine(x, y, w, h, e.getWorldX()+l[0], 
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
    }
    
    public int lineCount() {
        return lines.size();
    }
    
    public int[] getLine(int index) {
        return index >= 0 && index < lines.size() ? lines.get(index) : null;
    }
    
}