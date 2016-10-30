package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.Assets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

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
    
    @Override
    public int getWidth() {
        return Assets.getFont(font_size).getWidth(text) + (icon != null ? icon.getWidth() + 3: 0);
    }
    
    @Override
    public int getHeight() {
        if (super.getHeight() <= 0) {
            int font = Assets.getFont(font_size).getHeight(text);
            int img = (icon != null ? icon.getHeight() : 0);
            return font > img ? font : img;
        }
        return super.getHeight();
    }
    
    public void setText(String t) {
        text = t;
    }

    @Override
    public void draw(Graphics g) {
        
        if (icon != null) {
            g.drawImage(icon, getOnscreenX(), getOnscreenY()+getHeight()/2-icon.getHeight()/2);
        }
        
        if (text.length() > 0) {
            g.setFont(Assets.getFont(font_size));
            int str_x = getOnscreenX() + (icon != null ? icon.getWidth() + 3 : 0);
            int str_y = getOnscreenY() + getHeight()/2 - Assets.getFont(font_size).getHeight(text)/2 - 1;
            g.setColor(Color.gray.darker());
            g.drawString(text, str_x+1, str_y+1);
            g.setColor(Color.white);
            g.drawString(text, str_x, str_y);
        }
    }

}
