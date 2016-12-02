package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import java.util.ArrayList;

public class Hitbox extends Component {
    boolean collides;
    int width, height;
    
    public void createHitboxes() {
        System.out.println("parent = "+getParent());
        Component c = getParent().getComponent("Texture");
        if (c == null) return;
        Texture t = ((Texture)c);
        if (t.getTexture() == null) return;
        this.width = t.getTexture().getWidth();
        this.height = t.getTexture().getHeight();
    }
    
    public boolean collidesWith(Entity e) {
        Hitbox h = this, h2 = null;
        Component c = e.getComponent("Hitbox");
        if (c != null) h2 = ((Hitbox)c);
        if (h2 == null) return false;
        return MiscMath.rectanglesIntersect(e.getWorldX()-(h2.width/2),
                e.getWorldY()-(h2.height/2), h2.width, h2.height,
                getParent().getWorldX()-(h.width/2),
                getParent().getWorldY()-(h.height/2), h.width, h.height);
    }
    
    @Override
    public void initParams(ArrayList<String> params) {
        for (String p: params) if (p.indexOf("collides=") == 0)
            collides = Boolean.parseBoolean(p.replace("collides=", "").trim());
        createHitboxes();
    }
    
    @Override
    public void copyTo(Component c) {
        ((Hitbox)c).width = width;
        ((Hitbox)c).height = height;
        ((Hitbox)c).collides = collides;
    }
    
}