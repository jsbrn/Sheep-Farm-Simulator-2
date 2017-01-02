package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.Camera;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Panel extends GUIElement {
    
    boolean show_bg = true;
    String title;
    Image icon, background;
    
    boolean dragging = false, resizing = false;
    int last_x = 0, last_y = 0;
    
    boolean resizable, movable, closable;
    
    public Panel() {
        this.title = "";
        this.resizable = true;
        this.movable = true;
        this.closable = true;
    }
    
    @Override
    public void update() {
        if (dragging) {
            this.addX(Mouse.getX()-last_x);
            this.addY((Display.getHeight()-Mouse.getY())-last_y);
            last_x = Mouse.getX();
            last_y = Display.getHeight()-Mouse.getY();
        } else if (resizing) {
            this.addWidth(Mouse.getX()-last_x);
            this.addHeight((Display.getHeight()-Mouse.getY())-last_y);
        }
        super.update();
    }
    
    public void setDraggable(boolean b) { this.movable = b; }
    
    @Override
    public void onMousePress(int button, int x, int y) {
        if (MiscMath.pointIntersectsRect(x, y, getOnscreenX(), getOnscreenY(), getWidth(), getHeaderHeight())) {
            dragging = true;
            last_x = x; last_y = y;
        } else if (MiscMath.pointIntersectsRect(x, y, getOnscreenX()+getWidth()-16, getOnscreenY()+getHeight()-16, 16, 16)) {
            resizing = true;
            last_x = x; last_y = y;
        }
        grabFocus();
    }
    
    @Override
    public void onMouseRelease(int button, int x, int y) {
        dragging = false;
        resizing = false;
    }
    
    @Override
    public int getWidth() {
        return (background != null ? background.getWidth() : super.getWidth());
    }
    
    @Override
    public int getHeight() {
        return (background != null ? background.getHeight() : super.getHeight());
    }
    
    public void setIcon(String icon_url) {
        try {
            icon = new Image(icon_url, false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int getHeaderHeight() {
        return Assets.getFont(12).getHeight(title)+6;
    }
    
    public void setBackground(String icon_url) {
        try {
            background = new Image(icon_url, false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setTitle(String t) {
        title = t;
    }
    
    public void showBackground(boolean b) {
        show_bg = b;
    }
    
    @Override
    public void draw(Graphics g) {
        if (show_bg && background == null) {
            if (!isDialog()) {
                g.setColor(new Color(0, 0, 0, 125));
                g.fillRect(getOnscreenX()+getWidth(), getOnscreenY()+5, 5, getHeight()-5);
                g.fillRect(getOnscreenX()+5, getOnscreenY()+getHeight(), getWidth()-5, 5);
                g.fillRect(getOnscreenX()+getWidth(), getOnscreenY()+getHeight(), 5, 5);
            }
            Color base = new Color(45, 50, 60);
            for (int r = 0; r != getHeight(); r++) {
                int diff = Math.abs((getHeight()/2)-r) / (getHeight() / 64);
                g.setColor(new Color(45-diff, 50-diff, 60-diff, 225));
                g.fillRect(getOnscreenX(), getOnscreenY()+r, getWidth(), 1);
            }
            g.setColor(Color.black);
            g.drawRect(getOnscreenX()-2, getOnscreenY()-2, getWidth()+3, getHeight()+3);
            g.setColor(base.brighter());
            g.drawRect(getOnscreenX()-1, getOnscreenY()-1, getWidth()+1, getHeight()+1);
            if ((icon != null || title.length() > 0) && background == null) {
                int hrgb[] = new int[]{85, 170, 240}; //header rgb
                for (int i = 0; i != getHeaderHeight(); i++) {
                    g.setColor(new Color(hrgb[0]+(i*2), hrgb[1]+(i*2), hrgb[2]+(i*2), 150));
                    g.fillRect(getOnscreenX(), getOnscreenY()+i, getWidth(), 1);
                }
            }
        }
        
        if (show_bg && background != null) {
            g.drawImage(background, getOnscreenX(), getOnscreenY());
        }
        
        if (icon != null) {
            g.drawImage(icon, getOnscreenX()+3, getOnscreenY()+3);
        }
        
        if (title.length() > 0) {
            g.setFont(Assets.getFont(12));
            int str_x = getOnscreenX() + (icon != null ? icon.getWidth() + 6 : 3);
            int str_y = getOnscreenY() + 3;
            g.setColor(Color.gray.darker());
            g.drawString(title, str_x-1, str_y-1);
            g.setColor(Color.white);
            g.drawString(title, str_x, str_y);
        }
        super.draw(g);
    }

}
