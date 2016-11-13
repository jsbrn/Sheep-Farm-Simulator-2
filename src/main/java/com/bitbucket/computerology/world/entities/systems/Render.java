package com.bitbucket.computerology.world.entities.systems;

import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.ComponentSystem;
import com.bitbucket.computerology.world.entities.components.*;
import org.newdawn.slick.Graphics;

public class Render extends ComponentSystem {
    
    public Render() {
        
    }
    
    public void draw(Graphics g) {
        Texture t = (Texture)getParent().getComponent("Texture");
        Position p = (Position)getParent().getComponent("Position");
        if (t == null || p == null) return;
        if (t.getTexture() == null) return;
        int[] c = World.getWorld().getOnscreenCoords(p.getWorldX(), p.getWorldY());
        g.drawImage(t.getTexture(), c[0], c[1]);
    }
    
}
