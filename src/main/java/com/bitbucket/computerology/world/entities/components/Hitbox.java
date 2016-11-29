package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Component;
import java.util.ArrayList;

public class Hitbox extends Component {
    boolean collides;
    ArrayList<int[][][]> hitboxes;
    
    public void createHitboxes() {
        Component c = getParent().getComponent("Texture");
        if (c == null) return;
        Texture t = ((Texture)c);
        if (t.getTexture() == null) return;
        this.hitboxes = new ArrayList<int[][][]>();
        int w = t.getTexture().getWidth() / 4;
        int h = t.getTexture().getHeight() / 4;
        int[][][] hb = new int[w][h][1];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                
            }
        }
    }
    
}
