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

    public static Color BACKGROUND_COLOR = new Color(200, 200, 200, 180),
        SHADOW_COLOR = new Color(0, 0, 0, 125);

    private Image icon, background, bar;
    private String title;
    private boolean dragging = false, resizing = false, show_bg;
    private int last_x = 0, last_y = 0;

    boolean resizable, draggable, closable;

    public Panel() {
        this.resizable = false;
        this.draggable = false;
        this.closable = false;
        this.show_bg = true;
        this.title = "";
        try {
            this.bar = new Image("images/gui/statusbar.png", false, Image.FILTER_NEAREST);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {

        if (dragging && draggable) {
            this.addX(Mouse.getX() - last_x);
            this.addY((Display.getHeight() - Mouse.getY()) - last_y);
            last_x = Mouse.getX();
            last_y = Display.getHeight() - Mouse.getY();
        } else if (resizing && resizable) {
            this.addWidth((Mouse.getX() - last_x)
                    * (anchored(GUIElement.ANCHOR_MID_X) ? 2 : 1));
            this.addHeight(((Display.getHeight() - Mouse.getY()) - last_y)
                    * (anchored(GUIElement.ANCHOR_MID_Y) ? 2 : 1));
            last_x = Mouse.getX();
            last_y = Display.getHeight() - Mouse.getY();
        }
        super.update();
    }

    public void setDraggable(boolean b) {
        this.draggable = b;
    }
    public void setResizable(boolean resizable) { this.resizable = resizable; }
    public void setClosable(boolean closable) { this.closable = closable; }

    @Override
    public void onMousePress(int button, int x, int y) {
        int[] dims = getOnscreenDimensions();
        if (MiscMath.pointIntersectsRect(x, y, dims[0], dims[1], dims[2], getHeaderHeight())) {
            dragging = true;
            last_x = x;
            last_y = y;
        } else if (MiscMath.pointIntersectsRect(x, y,
                dims[0] + dims[2] - 16, dims[1] + dims[3] - 16, 16, 16)) {
            resizing = true;
            last_x = x;
            last_y = y;
        } else if (MiscMath.pointIntersectsRect(x, y,
                dims[0] + dims[2] - 16, dims[1], 16, 16) && closable) {
            setVisible(!isVisible());
        }
        grabFocus();
    }

    @Override
    public void onMouseRelease(int button, int x, int y, boolean intersection) {
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

    public void setTitle(String title) {
        if (title == null) title = "";
        this.title = title;
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

    public int getHeaderHeight() { return 24; }

    @Override
    public void draw(Graphics g) {
        int[] dims = getOnscreenDimensions();

        if (show_bg) {

            if (background == null) {
                //draw shadow
                g.setColor(SHADOW_COLOR);
                g.fillRect(dims[0] + dims[2], dims[1] + 5, 5, dims[3]);
                g.fillRect(dims[0] + 5, dims[1] + dims[3], dims[2] - 5, 5);
                //draw background
                g.setColor(BACKGROUND_COLOR);
                g.fillRect(dims[0], dims[1] + (title.length() > 0 ? getHeaderHeight() : 0), dims[2],
                        dims[3] - (title.length() > 0 ? getHeaderHeight() : 0));
                if (title.length() > 0) {
                    g.drawImage(bar.getScaledCopy(dims[2], getHeaderHeight()), dims[0], dims[1]);
                    g.setFont(Assets.getFont(12));
                    g.setColor(Color.white);
                    g.drawString(title, dims[0] + (dims[2]/2) - (Assets.getFont(12).getWidth(title)/2),
                            dims[1] + (getHeaderHeight()/2) - (Assets.getFont(12).getHeight(title)/2));
                }
                //draw border
                g.setColor(Color.black);
                g.drawRect(dims[0], dims[1], dims[2], dims[3]);
                g.setColor(BACKGROUND_COLOR.brighter());
                g.drawRect(dims[0] + 1, dims[1] + 1, dims[2] - 2, dims[3] - 2);
            } else {
                g.drawImage(background, dims[0], dims[1]);
            }

        }

        super.draw(g);

    }

}
