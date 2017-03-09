package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.Assets;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ProgressBar extends GUIElement {

    String title;
    Color bar_color, background_color;

    double max, progress;

    public ProgressBar() {
        this.title = "";
        this.max = 1;
        this.progress = 0;
        this.bar_color = Color.green;
        this.background_color = Color.gray;
    }

    public void setProgress(double p) {
        progress = (p > 0) ? p : 0;
    }

    public void addProgress(double p) {
        progress += p;
        if (progress < 0) progress = 0;
        if (progress > max) progress = max;
    }

    public void setMax(double p) {
        max = (p > 0) ? p : 1;
    }

    public void setTitle(String s) {
        title = s;
    }

    @Override
    public void draw(Graphics g) {
        int[] dims = getOnscreenDimensions();

        g.setColor(background_color);
        g.fillRect(dims[0], dims[1], dims[2], dims[3]);
        if (Assets.BLACK_GRADIENT != null)
            g.drawImage(Assets.BLACK_GRADIENT.getFlippedCopy(false, true).getScaledCopy(dims[2], dims[3]), dims[1], dims[2]);
        g.setColor(bar_color);
        g.fillRect(dims[0], dims[1], (int) ((progress / max) * dims[2]), dims[3]);
        if (Assets.BLACK_GRADIENT != null)
            g.drawImage(Assets.BLACK_GRADIENT.getScaledCopy((int)((progress / max) * dims[2]), dims[3]), dims[0], dims[1]);


        g.setColor(Color.gray.darker());
        g.drawRect(dims[0] + 1, dims[1] + 1, dims[2] - 2, dims[3] - 2);
        g.setColor(Color.black);
        g.drawRect(dims[0], dims[1], dims[2], dims[3]);

        super.draw(g);

    }

}
