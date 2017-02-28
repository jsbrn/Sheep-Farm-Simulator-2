package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
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
    public void drawToCanvas() {
        super.drawToCanvas();
        Graphics g = getGUI().getCanvas();
        int[] dims = getOnscreenDimensions();
        int x = getCanvasLocation()[0], y = getCanvasLocation()[1];
        g.setColor(Color.black);
        g.drawRect(x - 2, y - 2, dims[2] + 3, dims[3] + 3);
        g.setColor(new Color(45, 50, 75).brighter());
        g.drawRect(x - 1, y - 1, dims[2] + 1, dims[3] + 1);

        int rgb[] = new int[]{background_color.getRed(), background_color.getGreen(), background_color.getBlue()};
        int transition = 50 / (dims[3] > 0 ? dims[3] : 1);
        for (int r = 0; r != dims[3]; r++) {
            rgb[0] += transition;
            rgb[1] += transition;
            rgb[2] += transition;
            g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
            g.fillRect(x, y + r, dims[2], 1);
        }

        rgb = new int[]{bar_color.getRed(), bar_color.getGreen(), bar_color.getBlue()};
        transition = -50 / (dims[3] > 0 ? dims[3] : 1);
        for (int r = 0; r != dims[3]; r++) {
            rgb[0] += transition;
            rgb[1] += transition;
            rgb[2] += transition;
            g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
            g.fillRect(x, y + r, (int) ((progress / max) * dims[2]), 1);
        }

    }

}
