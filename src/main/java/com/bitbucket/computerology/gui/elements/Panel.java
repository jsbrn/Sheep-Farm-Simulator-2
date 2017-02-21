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

    public static Color BACKGROUND_COLOR = new Color(0, 0, 0, 200),
        SHADOW_COLOR = new Color(0, 0, 0, 125);

    boolean show_bg = true;
    Image icon, background;

    boolean dragging = false, resizing = false;
    int last_x = 0, last_y = 0;

    boolean resizable, draggable, closable;

    public Panel() {
        this.resizable = false;
        this.draggable = false;
        this.closable = false;
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
        this.draggable = b;
    }

    @Override
    public void onMousePress(int button, int x, int y) {
        int[] dims = getOnscreenDimensions();
        if (MiscMath.pointIntersectsRect(x, y, dims[0], dims[1], dims[2], 24)) {
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

    public void setBackground(String icon_url) {
        try {
            background = new Image(icon_url, false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showBackground(boolean b) {
        show_bg = b;
    }

    @Override
    public void drawToCanvas() {
        Graphics g = getCanvas();
        int[] dims = getOnscreenDimensions();

        if (show_bg) {

            if (background == null) {
                if (!isDialog()) { //draw shadow
                    g.setColor(SHADOW_COLOR);
                    g.fillRect(dims[2], 5, 5, dims[3]);
                    g.fillRect(5, dims[3], dims[2] - 5, 5);
                }
                //draw background
                g.setColor(BACKGROUND_COLOR);
                g.fillRect(0, 0, dims[2], dims[3]);
                //draw border
                g.setColor(Color.black);
                g.drawRect(0, 0, dims[2], dims[3]);
                g.setColor(BACKGROUND_COLOR.brighter());
                g.drawRect(1, 1, dims[2] - 2, dims[3] - 2);
            } else {
                g.drawImage(background, 0, 0);
            }

        }

        super.drawToCanvas(); //draw subcomponents to their own canvases
    }

}
