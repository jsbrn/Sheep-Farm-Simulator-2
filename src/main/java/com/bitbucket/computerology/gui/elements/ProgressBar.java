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

    public void addText(String t) {
        if (Assets.getFont(12).getWidth(title) + 6 < getWidth() - 10) title += t;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.black);
        g.drawRect(getOnscreenX() - 2, getOnscreenY() - 2, getWidth() + 3, getHeight() + 3);
        g.setColor(new Color(45, 50, 75).brighter());
        g.drawRect(getOnscreenX() - 1, getOnscreenY() - 1, getWidth() + 1, getHeight() + 1);

        int rgb[] = new int[]{background_color.getRed(), background_color.getGreen(), background_color.getBlue()};
        int transition = 50 / (getHeight() > 0 ? getHeight() : 1);
        for (int r = 0; r != getHeight(); r++) {
            rgb[0] += transition;
            rgb[1] += transition;
            rgb[2] += transition;
            g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
            g.fillRect(getOnscreenX(), getOnscreenY() + r, getWidth(), 1);
        }

        rgb = new int[]{bar_color.getRed(), bar_color.getGreen(), bar_color.getBlue()};
        transition = -50 / (getHeight() > 0 ? getHeight() : 1);
        for (int r = 0; r != getHeight(); r++) {
            rgb[0] += transition;
            rgb[1] += transition;
            rgb[2] += transition;
            g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
            g.fillRect(getOnscreenX(), getOnscreenY() + r, (int) ((progress / max) * getWidth()), 1);
        }

    }

}
