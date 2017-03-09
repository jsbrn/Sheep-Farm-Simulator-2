package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ListElement extends GUIElement {

    private boolean even;
    Color bg_color;

    public ListElement(int index) {
        this.even = index % 2 == 0;
        float mult = even ? 1.25f : 1;
        this.bg_color = new Color((int)(100*mult), (int)(100*mult), (int)(100*mult));
    }

    @Override
    public void draw(Graphics g) {

        int[] dims = getOnscreenDimensions();
        g.setColor(bg_color);
        g.fillRect(dims[0], dims[1], dims[2], dims[3]);

        super.draw(g);

    }

}
