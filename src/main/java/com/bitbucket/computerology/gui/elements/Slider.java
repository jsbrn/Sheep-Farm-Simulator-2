package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Slider extends GUIElement {

    int min, max, incr;
    double value;
    Color color;
    Image knob;

    public Slider() {
        this.max = 10;
        this.value = 5;
        this.min = 0;
        this.incr = 1;
        this.color = Color.white;
        this.setWidth(24);
        try {
            this.knob = new Image("images/gui/slider_knob.png", false, Image.FILTER_LINEAR);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    public double getValue() {
        return value;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int getIncrement() {
        return incr;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setIncrement(int incr) {
        this.incr = incr;
    }

    @Override
    public void draw(Graphics g) {

        int[] dims = getOnscreenDimensions();
        g.setColor(Color.black);
        g.fillRect(dims[0], dims[1] + (dims[2]/2) - 2, dims[2], 3);
        g.setColor(color.darker());
        g.drawRect(dims[0], dims[1] + (dims[2]/2) - 2, dims[2], 3);

        g.setColor(color);
        g.fillOval(dims[0] + (int)((value / max) * dims[2]) - 10,
                dims[1] + (dims[2]/2) - 10, 20, 20);
        g.drawImage(knob, dims[0] + (int)((value / max) * dims[2]) - 12,
                dims[1] + (dims[2]/2) - 12);

        super.draw(g);

    }

}
