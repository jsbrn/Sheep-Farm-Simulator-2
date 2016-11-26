package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.Assets;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Button extends GUIElement {
    
    boolean background = true, auto_width = false, pressed = false;
    Image icon;
    String text;
    Color bg_color, text_color;
    
    static int GRAD = 35; //gradient magnitude
    
    public Button() {
        this.text = "";
        this.bg_color = Color.white;
        this.text_color = Color.gray.darker();
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
    
    public void autoWidth(boolean b) {
        auto_width = b;
    }
    
    public void setText(String t) {
        text = t;
    }
    
    public String getText() {
        return text;
    }
    
    public void showBackground(boolean b) {
        background = b;
    }
    
    public void setBackgroundColor(Color c) {
        int rbg[] = new int[]{c.getRed() > GRAD ? (c.getRed() < 255-GRAD ? c.getRed() : 255-GRAD) : GRAD,
            c.getGreen() > GRAD ? (c.getGreen() < 255-GRAD ? c.getGreen() : 255-GRAD) : GRAD,
            c.getBlue() > GRAD ? (c.getBlue() < 255-GRAD ? c.getBlue() : 255-GRAD) : GRAD};
        bg_color = new Color(rbg[0], rbg[1], rbg[2]);
    }
    
    public void setTextColor(Color c) {
        text_color = c;
    }
    
    @Override
    public int getWidth() {
        int a_width = (text.length() > 0 ? Assets.getFont(12).getWidth(text) : 0) + (icon != null ? icon.getWidth() : 0);
        if (text.length() > 0 && icon != null) a_width += 3;
        if (text.length() > 0 || icon != null) a_width += 4;
        return (auto_width ? a_width : super.getWidth());
    }
    
    @Override
    public void draw(Graphics g) {
        //if set to draw a background
        if (background) {
            //create the top color
            int rgb[] = new int[]{bg_color.getRed()-GRAD, bg_color.getGreen()-GRAD, bg_color.getBlue()-GRAD};
            //if not enabled, set it to grey but with a touch of the original color :D
            if (!enabled()) { rgb[0] = 50 + (rgb[0]/4);rgb[1] = 50 + (rgb[1]/4);rgb[2] = 50 + (rgb[2]/4);}
            //for each pixel row of the button, transition to a color symmetrically brighter to the top color
            //that is, the specified background color is the middle row of the background
            int transition = (GRAD*2)/(getHeight() > 0 ? getHeight() : 1);
            for (int r = 0; r != getHeight(); r++) {
                rgb[0]+=transition;
                rgb[1]+=transition;
                rgb[2]+=transition;
                g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
                g.fillRect(getOnscreenX(), getOnscreenY()+r, getWidth(), 1);
            }
            //if enabled, draw a bevelled border
            if (enabled()) {
                g.setColor(bg_color);
                g.fillRect(getOnscreenX(), getOnscreenY(), getWidth(), 1);
                g.fillRect(getOnscreenX(), getOnscreenY(), 1, getHeight());
                g.setColor(new Color(bg_color.getRed()-(GRAD*2), bg_color.getGreen()-(GRAD*2), bg_color.getBlue()-(GRAD*2)));
                g.fillRect(getOnscreenX(), getOnscreenY()+getHeight()-1, getWidth(), 1);
                g.fillRect(getOnscreenX()+getWidth()-1, getOnscreenY(), 1, getHeight());
            } else { //if not enabled, draw a box with greyer variant of the original background color
                g.setColor(new Color(50+bg_color.getRed()/4, 50+bg_color.getGreen()/4, 50+bg_color.getBlue()/4));
                g.drawRect(getOnscreenX(), getOnscreenY(), getWidth()-1, getHeight()-1);
            }
        }
        //if the button is not being pressed, the mouse is touching it, and it is enabled, lighten the background
        if (!pressed && mouseHovering() && enabled()) {
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(getOnscreenX(), getOnscreenY(), getWidth(), getHeight());
        }
        //if the icon is not null, draw it in the appropriate location (depends on whether there is text)
        if (icon != null) {
            if (text.length() > 0) {
                g.drawImage(icon, getOnscreenX() + 2, getOnscreenY() + getHeight()/2 - icon.getHeight()/2);
            } else {
                g.drawImage(icon, getOnscreenX() + getWidth()/2 - icon.getWidth()/2, 
                        getOnscreenY() + getHeight()/2 - icon.getHeight()/2);
            }
        }
        //draw text if any
        if (text.length() > 0) {
            g.setFont(Assets.getFont(12));
            int str_x = getOnscreenX() + getWidth()/2 - Assets.getFont(12).getWidth(text)/2;
            int str_y = getOnscreenY() + getHeight()/2 - Assets.getFont(12).getHeight(text)/2-1;
            if (icon != null) {
                str_x = getOnscreenX() + 6 + icon.getWidth();
                str_y = getOnscreenY() + getHeight()/2 - Assets.getFont(12).getHeight(text)/2-1;
            }
            g.setColor(Color.gray.darker());
            g.drawString(text, str_x+1, str_y+1);
            //text color depends on enabled state of button
            g.setColor(enabled() ? text_color : Color.lightGray);
            g.drawString(text, str_x, str_y);
        }
    }

}
