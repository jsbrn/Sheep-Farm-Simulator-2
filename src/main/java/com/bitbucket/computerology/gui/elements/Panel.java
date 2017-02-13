package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.logging.Level;
import java.util.logging.Logger;

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
            this.addX(Mouse.getX() - last_x);
            this.addY((Display.getHeight() - Mouse.getY()) - last_y);
            last_x = Mouse.getX();
            last_y = Display.getHeight() - Mouse.getY();
        } else if (resizing) {
            this.addWidth(Mouse.getX() - last_x);
            this.addHeight((Display.getHeight() - Mouse.getY()) - last_y);
            last_x = Mouse.getX();
            last_y = Display.getHeight() - Mouse.getY();
        }
        super.update();
    }

    public void setDraggable(boolean b) {
        this.movable = b;
    }

    @Override
    public void onMousePress(int button, int x, int y) {
        int[] dims = getOnscreenDimensions();
        if (MiscMath.pointIntersectsRect(x, y, dims[0], dims[1], dims[2], getHeaderHeight())) {
            dragging = true;
            last_x = x;
            last_y = y;
        } else if (MiscMath.pointIntersectsRect(x, y, dims[0] + dims[2] - 16, dims[1] + dims[3] - 16, 16, 16)) {
            resizing = true;
            last_x = x;
            last_y = y;
        }
        grabFocus();
    }

    @Override
    public void onMouseRelease(int button, int x, int y) {
        dragging = false;
        resizing = false;
    }

    @Override
    public int[] getOnscreenDimensions() {
        int[] sup = super.getOnscreenDimensions();
        sup[2] = background != null ? background.getWidth() : sup[2];
        sup[3] = background != null ? background.getHeight() : sup[3];
        return sup;
    }

    public void setIcon(String icon_url) {
        try {
            icon = new Image(icon_url, false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getHeaderHeight() {
        return Assets.getFont(12).getHeight(title) + 6;
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
    public void drawToCanvas() {
        Graphics g = getCanvas();
        int[] dims = getOnscreenDimensions();
        if (show_bg && background == null) {
            if (!isDialog()) {
                g.setColor(new Color(0, 0, 0, 125));
                g.fillRect(0 + dims[2], 0 + 5, 5, dims[3] - 5);
                g.fillRect(0 + 5, 0 + dims[3], dims[2] - 5, 5);
                g.fillRect(0 + dims[2], 0 + dims[3], 5, 5);
            }
            Color base = new Color(45, 50, 60);
            for (int r = 0; r != dims[3]; r++) {
                int diff = dims[3] / 64 != 0 ? (Math.abs((dims[3] / 2) - r) / (dims[3] / 64)) : 1;
                g.setColor(new Color(45 - diff, 50 - diff, 60 - diff, 225));
                g.fillRect(0, 0 + r, dims[2], 1);
            }
            g.setColor(Color.black);
            g.drawRect(0, 0, dims[2], dims[3]);
            g.setColor(base.brighter());
            g.drawRect(1, 1, dims[2]-1, dims[3]-1);
            if ((icon != null || title.length() > 0) && background == null) {
                int hrgb[] = new int[]{85, 170, 240}; //header rgb
                for (int i = 0; i != getHeaderHeight(); i++) {
                    g.setColor(new Color(hrgb[0] + (i * 2), hrgb[1] + (i * 2), hrgb[2] + (i * 2), 150));
                    g.fillRect(0, 0 + i, dims[2], 1);
                }
            }
        }

        if (show_bg && background != null) {
            g.drawImage(background, 0, 0);
        }

        if (icon != null) {
            g.drawImage(icon, 0 + 3, 0 + 3);
        }

        if (title.length() > 0) {
            g.setFont(Assets.getFont(12));
            int str_x = 0 + (icon != null ? icon.getWidth() + 6 : 3);
            int str_y = 0 + 3;
            g.setColor(Color.gray.darker());
            g.drawString(title, str_x - 1, str_y - 1);
            g.setColor(Color.white);
            g.drawString(title, str_x, str_y);
        }
        super.drawToCanvas(); //draw subcomponents to their own canvases
    }

}
