package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.Window;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Slider extends GUIElement {

    private int min, max, incr;
    private double value;
    private Color color;
    private Image knob;
    private boolean snap;

    private int last_x, last_y, dist; //last mouse coords, and the knob distance from 0
    private boolean dragging;

    public Slider() {
        this.max = 10;
        this.value = 5;
        this.min = 0;
        this.incr = 1;
        this.color = Color.white;
        this.setHeight(24);
        try {
            this.knob = new Image("images/gui/slider_knob.png", false, Image.FILTER_LINEAR);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMousePress(int button, int x, int y) {
        dragging = true;
        last_x = x;
        last_y = y;
    }

    @Override
    public void update() {
        if (dragging) {
            int[] dims = getOnscreenDimensions();
            double rel = last_x - dims[0];
            rel = MiscMath.clamp(rel, 0, dims[2]);
            System.out.print("Rel = "+rel+", ["+dims[0]+", "+dims[2]+"] -> ");
            value = min + ((max-min) * (rel/dims[2]));
            System.out.print("val before snap = "+value+", after = ");
            if (snap) value = MiscMath.round(value, incr);
            System.out.println(value);

            last_x = Mouse.getX();
            last_y = Window.getHeight() - Mouse.getY();
        }
        super.update();
    }

    @Override
    public void onMouseRelease(int button, int x, int y, boolean intersection) {
        dragging = false;
    }

    public boolean snaps() { return snap; }

    public void setSnap(boolean snaps) { snap = snaps; }

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
        g.fillRect(dims[0], dims[1] + (dims[3]/2) - 2, dims[2], 3);
        g.setColor(color.darker());
        g.drawRect(dims[0], dims[1] + (dims[3]/2) - 2, dims[2], 3);

        g.setColor(color);

        double x = MiscMath.clamp(((value / max) * dims[2]), 12, dims[2] - 10);
        g.fillOval(dims[0] + (int)x - 10,
                dims[1] + (dims[3]/2) - 10, 20, 20);
        g.drawImage(knob, dims[0] + (int)x - 12,
                dims[1] + (dims[3]/2) - 12);

        super.draw(g);

    }

}
