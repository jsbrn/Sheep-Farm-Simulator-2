package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ListElement extends GUIElement {

    private boolean even;
    Color bg_color;

    public ListElement(int index) {
        this.even = index % 2 == 0;
        int mult = even ? 2 : 1;
        this.bg_color = new Color(20*mult, 20*mult, 25*mult);
    }

    @Override
    public void draw(Graphics g) {

        int[] dims = getOnscreenDimensions();
        g.setColor(bg_color);
        g.fillRect(dims[0], dims[1], dims[2], dims[3]);

        super.draw(g);

    }

}
