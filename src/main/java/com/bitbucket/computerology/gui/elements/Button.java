package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.Assets;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Button extends GUIElement {

    public static Color[] CONFIRM

    private static int GRAD = 35; //gradient magnitude
    private boolean background = true, auto_width = false, pressed = false;
    private Image icon;
    private String text;
    private Color bg_color, text_color;

    public Button() {
        this.text = "";
        this.bg_color = Color.black;
        this.text_color = Color.white;
    }

    public Button(String text, Color bg, Color t) {
        this();
        this.text = text;
        this.bg_color = bg;
        this.text_color = t;
    }

    @Override
    public void onMousePress(int button, int x, int y) {
        pressed = true;
    }

    @Override
    public void onMouseRelease(int button, int x, int y) {
        pressed = false;
    }

    public void setIcon(String icon_url) {
        try {
            icon = new Image(icon_url, false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(Button.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String t) {
        text = t;
    }

    public void showBackground(boolean b) {
        background = b;
    }

    public void setBackgroundColor(Color c) {
        int rbg[] = new int[]{c.getRed() > GRAD ? (c.getRed() < 255 - GRAD ? c.getRed() : 255 - GRAD) : GRAD,
                c.getGreen() > GRAD ? (c.getGreen() < 255 - GRAD ? c.getGreen() : 255 - GRAD) : GRAD,
                c.getBlue() > GRAD ? (c.getBlue() < 255 - GRAD ? c.getBlue() : 255 - GRAD) : GRAD};
        bg_color = new Color(rbg[0], rbg[1], rbg[2]);
    }

    public void setTextColor(Color c) {
        text_color = c;
    }

    @Override
    public void drawToCanvas() {
        Graphics g = getCanvas();
        int[] dims = getOnscreenDimensions();

        //drawToCanvas text if any
        if (text.length() > 0) {
            g.setFont(Assets.getFont(12));
            int str_x = 0 + dims[2] / 2 - Assets.getFont(12).getWidth(text) / 2;
            int str_y = 0 + dims[3] / 2 - Assets.getFont(12).getHeight(text) / 2 - 1;
            if (icon != null) {
                str_x = 0 + 6 + icon.getHeight();
                str_y = 0 + dims[3] / 2 - Assets.getFont(12).getHeight(text) / 2 - 1;
            }
            g.setColor(Color.gray.darker());
            g.drawString(text, str_x + 1, str_y + 1);
            //text color depends on enabled state of button
            g.setColor(enabled() ? text_color : Color.lightGray);
            g.drawString(text, str_x, str_y);
        }
    }

    @Override
    public String toString() {
        return "Button["+text+"]";
    }
}
