package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class ListViewer extends GUIElement {

    int component_height;
    boolean shadow;

    public ListViewer() {
        this.component_height = 24;
        this.shadow = true;
    }


    public void setComponentHeight(int height) {
        this.component_height = height;
    }

    public int getMaxVisible() {
        return getOnscreenDimensions()[1] / component_height;
    }


    @Override
    public void draw(Graphics g) {

        int[] dims = getOnscreenDimensions();

        g.setColor(new Color(0, 0, 0, 25));
        g.fillRect(dims[0], dims[1], dims[2], dims[3]);

        super.draw(g);

        g.setColor(Color.black);
        g.drawRect(dims[0], dims[1], dims[2], dims[3]);

        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(0, 0, 0, 200 - (i*40)));
            g.drawRect(dims[0] + i, dims[1] + i, dims[2] - (i*2), dims[3] - (i*2));
        }

    }

}
