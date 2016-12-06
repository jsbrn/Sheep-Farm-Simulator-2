package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import java.util.ArrayList;

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
        Hitbox h2 = null;
        Component c = e.getComponent("Hitbox");
        if (c != null) h2 = ((Hitbox)c);
        if (h2 == null) return false;
        return intersects(e.getWorldX()-(h2.width/2),
                e.getWorldY()-(h2.height/2), h2.width, h2.height);
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
    
}