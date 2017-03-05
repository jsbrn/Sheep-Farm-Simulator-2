package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.states.GameScreen;
import com.bitbucket.computerology.misc.Assets;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Label extends GUIElement {

    String text;
    int font_size = 12;

    public Label() {
        this.text = "";
    }

    public void setFontSize(int size) {
        font_size = size;
    }

    public void setText(String t) {
        text = t;
        setWidth(Assets.getFont(font_size).getWidth(t));
        setHeight(Assets.getFont(font_size).getHeight(t));
    }

    @Override
    public void draw(Graphics g) {

        int[] dims = getOnscreenDimensions();

        if (text.length() > 0) {
            g.setFont(Assets.getFont(font_size));
            int str_y = dims[3] / 2 - Assets.getFont(font_size).getHeight(text) / 2 - 1;
            g.setColor(Color.gray.darker());
            g.drawString(text, dims[0] + 1, dims[1] + str_y + 1);
            g.setColor(Color.white);
            g.drawString(text, dims[0], dims[1] + str_y);
        }

        super.draw(g);

        if (GameScreen.DEBUG_MODE) {
            g.setColor(Color.red);
            g.drawRect(dims[0], dims[1], dims[2], dims[3]);
        }

    }

}
