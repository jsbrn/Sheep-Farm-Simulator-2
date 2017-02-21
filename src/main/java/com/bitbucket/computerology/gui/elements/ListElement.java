package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ListElement extends GUIElement {

    private boolean even;
    Color bg_color;

    public ListElement(int index) {
        this.even = index % 2 == 0;
        this.bg_color = new Color(10, 10, 25, even ? 150 : 100);
    }

    public void drawToCanvas() {

        Graphics g = getCanvas();
        int[] dims = getOnscreenDimensions();
        g.setColor(bg_color);
        g.fillRect(0, 0, dims[2], dims[3]);

        super.drawToCanvas();

    }

}
