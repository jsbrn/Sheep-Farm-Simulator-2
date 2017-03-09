package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.states.GameScreen;
import com.bitbucket.computerology.misc.Assets;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Label extends GUIElement {

    String text;
    int font_size, mode;

    public static int BLACK = 0, WHITE = 1;

    public Label() {
        this.setText("");
        this.setFontSize(12);
        this.mode = WHITE;
    }

    public Label(String text) {
        this();
        this.setText(text);
    }

    public Label(String text, int size) {
        this(text);
        this.setFontSize(size);
    }

    public void setStyle(int style) {
        this.mode = style;
    }

    public void setFontSize(int size) {
        font_size = size;
        setText(text); //refresh the width/height
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
            if (mode == WHITE) {
                g.setColor(Color.gray.darker());
                g.drawString(text, dims[0] + 1, dims[1] + str_y + 1);
            }
            g.setColor(mode == WHITE ? Color.white : Color.black);
            g.drawString(text, dims[0], dims[1] + str_y);
        }

        super.draw(g);

    }

}
