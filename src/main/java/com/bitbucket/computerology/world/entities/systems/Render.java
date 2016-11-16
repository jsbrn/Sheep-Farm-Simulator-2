package com.bitbucket.computerology.world.entities.systems;

import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.ComponentSystem;
import com.bitbucket.computerology.world.entities.components.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class Render extends ComponentSystem {
    
    public Render() {
        
    }
    
    public void draw(Graphics g) {
        Component tc = getParent().getComponent("Texture"), tp = getParent().getComponent("Position");
        if (tc == null || tp == null) return;
        Texture t = (Texture)tc; Position p = (Position)tp;
        if (t.getTexture() == null) return;
        int[] c = World.getWorld().getOnscreenCoords(p.getWorldX(), p.getWorldY());
        Image img = t.getTexture().getScaledCopy(Camera.getZoom());
        img.setRotation(p.getRotation());
        g.drawImage(t.getTexture().getScaledCopy(Camera.getZoom()), c[0]-img.getWidth()/2, c[1]-img.getHeight()/2);
    }
    
}
