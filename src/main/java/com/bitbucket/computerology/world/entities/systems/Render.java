package com.bitbucket.computerology.world.entities.systems;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.ComponentSystem;
import com.bitbucket.computerology.world.entities.components.Position;
import com.bitbucket.computerology.world.entities.components.Texture;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class Render extends ComponentSystem {

    public Render() {

    }

    public void draw(Graphics g) {
        Texture t = getParent().getTexture();
        Position p = getParent().getPosition();
        if (t.getTexture() == null) return;
        int[] c = MiscMath.getOnscreenCoords(p.getWorldX(), p.getWorldY());
        Image img = t.getTexture().getScaledCopy((int)Camera.getZoom());
        img.setRotation(p.getRotation());
        g.drawImage(img, c[0] - img.getWidth() / 2, c[1] - img.getHeight() / 2);
    }

}
