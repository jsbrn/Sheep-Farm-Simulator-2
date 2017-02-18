package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.Assets;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Label extends GUIElement {

    String text;
    Image icon;
    int font_size = 12;

    public Label() {
        this.text = "";
    }

    public void setIcon(String icon_url) {
        try {
            icon = new Image(icon_url, false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(Label.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setFontSize(int size) {
        font_size = size;
    }

    public void setText(String t) {
        text = t;
        setWidth(Assets.getFont(font_size).getWidth(t));
    }

    @Override
    public void drawToCanvas() {
        Graphics g = getCanvas();

        int[] dims = getOnscreenDimensions();

        if (icon != null) {
            g.drawImage(icon, 0, dims[3] / 2 - icon.getHeight() / 2);
        }

        if (text.length() > 0) {
            g.setFont(Assets.getFont(font_size));
            int str_x = (icon != null ? icon.getWidth() + 3 : 0);
            int str_y = dims[3] / 2 - Assets.getFont(font_size).getHeight(text) / 2 - 1;
            g.setColor(Color.gray.darker());
            g.drawString(text, str_x + 1, str_y + 1);
            g.setColor(Color.white);
            g.drawString(text, str_x, str_y);
        }
    }

}
