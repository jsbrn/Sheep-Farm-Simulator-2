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

    private static int GRAD = 35; //gradient magnitude
    private Image background_img, hover_img;
    private String text;
    private Color bg_color, hover_color, text_color, border_color;

    public Button() {
        this.text = "";
        this.setBackgroundColor(Color.black);
        this.text_color = Color.white;
    }

    public Button(String text, Color bg, Color t) {
        this();
        this.text = text;
        setBackgroundColor(bg);
        this.text_color = t;
    }

    public void setBackgroundImage(String bg_url, String hov_url) {
        try {
            background_img = new Image(bg_url, false, Image.FILTER_LINEAR);
            hover_img = new Image(hov_url, false, Image.FILTER_LINEAR);
            setWidth(background_img.getWidth());
            setHeight(background_img.getHeight());
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

    public void setBackgroundColor(Color c) {
        int rgb[] = new int[]{c.getRed() > GRAD ? (c.getRed() < 255 - GRAD ? c.getRed() : 255 - GRAD) : GRAD,
                c.getGreen() > GRAD ? (c.getGreen() < 255 - GRAD ? c.getGreen() : 255 - GRAD) : GRAD,
                c.getBlue() > GRAD ? (c.getBlue() < 255 - GRAD ? c.getBlue() : 255 - GRAD) : GRAD};
        bg_color = new Color(rgb[0], rgb[1], rgb[2]);
        hover_color = new Color(rgb[0]+50, rgb[1]+50, rgb[2]+50);
        int avg = (rgb[0]+rgb[1]+rgb[2]) / 3;
        border_color = avg > 255 / 4 ? bg_color.darker() : bg_color.brighter();
    }

    public void setTextColor(Color c) {
        text_color = c;
    }

    @Override
    public void draw(Graphics g) {
        
        int[] dims = getOnscreenDimensions();

        if (background_img == null) {

            g.setColor(mouseHovering() ? hover_color : bg_color);
            if (!enabled()) g.setColor(Color.gray);
            g.fillRect(dims[0], dims[1], dims[2], dims[3]);
            g.drawImage(Assets.BLACK_GRADIENT.getScaledCopy(dims[2], dims[3]), dims[0], dims[1]);
            g.setColor(border_color);
            g.drawRect(dims[0], dims[1], dims[2] - 1, dims[3] - 1);

            //draw text if any
            if (text.length() > 0) {
                g.setFont(Assets.getFont(12));
                int str_x = dims[2] / 2 - Assets.getFont(12).getWidth(text) / 2;
                int str_y = dims[3] / 2 - Assets.getFont(12).getHeight(text) / 2 - 1;
                if (background_img != null) {
                    str_x = 6 + background_img.getHeight();
                    str_y = dims[3] / 2 - Assets.getFont(12).getHeight(text) / 2 - 1;
                }
                g.setColor(Color.gray.darker());
                g.drawString(text, dims[0] + str_x + 1, dims[1] + str_y + 1);
                //text color depends on enabled state of button
                g.setColor(enabled() ? text_color : Color.darkGray);
                g.drawString(text, dims[0] + str_x, dims[1] + str_y);
            }

        } else {
            //draw background image
        }

        super.draw(g);

    }

    @Override
    public String toString() {
        return "Button["+text+"]";
    }
}
