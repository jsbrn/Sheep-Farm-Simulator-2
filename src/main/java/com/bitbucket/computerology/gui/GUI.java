package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.gui.elements.Button;
import com.bitbucket.computerology.gui.elements.Label;
import com.bitbucket.computerology.gui.elements.Panel;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.Window;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.ArrayList;

public class GUI {

    private ArrayList<GUIElement> components;
    private GUIElement focus;
    private Panel dialog;

    float dialog_alpha = 0;

    /*private Graphics canvas;
    private Image gfx_img;
    private ArrayList<int[]> allocated;*/

    public GUI() {
        //createCanvas(Window.getScreenWidth()*4, Window.getScreenHeight()*4);
        this.components = new ArrayList<GUIElement>();
    }

    /**
     * Gets the list of components from this GUI instance.
     *
     * @return An ArrayList of GUIElement instances.
     */
    public final ArrayList<GUIElement> getComponents() {
        return components;
    }

    /**
     * Gets the element that currently has focus. Mostly used for key bindings.
     *
     * @return A GUIElement instance.
     */
    public final GUIElement getFocus() {
        return focus;
    }

    public final void setFocus(GUIElement g) {
        System.out.println("Setting focus to "+g);
        focus = g;
    }

    public final void showMessage(String title, String message[]) {
        Panel panel = new Panel();
        panel.setTitle(title);
        panel.setVisible(false);

        final Panel old_dialog = dialog;

        int width = 0, height = panel.getHeaderHeight() + 56;
        for (int i = 0; i < message.length; i++) {
            Label l = new Label(message[i]);
            l.anchor(panel, GUIElement.ANCHOR_TOP, 0, panel.getHeaderHeight() + 10 + (i * 20));
            l.anchor(panel, GUIElement.ANCHOR_LEFT, 0, 10);
            panel.addComponent(l);
            int w = l.getOnscreenDimensions()[2] + 20;
            if (w > width) width = w;
            height += 20;
        }

        Button button = new Button("OK", Color.black, Color.white) {

            @Override
            public void onMouseRelease(int button, int x, int y, boolean intersection) {
                if (intersection) {
                    clearDialog();
                    dialog(old_dialog);
                }
            }

        };
        button.anchor(panel, GUIElement.ANCHOR_MID_X, 0.5, 0);
        button.anchor(panel, GUIElement.ANCHOR_BOTTOM, 1, -10);
        button.anchor(panel, GUIElement.ANCHOR_TOP, 1, -34);
        button.setHeight(24);
        panel.addComponent(button);

        panel.setWidth(width); panel.setHeight(height);
        addComponent(panel);
        clearDialog();
        dialog(panel);
    }

    /**
     * Currently only works properly for top-level components.
     * Locks all external input such that only input on e is
     * accepted. Clears keyboard focus.
     *
     * @param e The element you wish to restrict input to.
     */
    public final void dialog(Panel e) {
        if (!components.contains(e)) {
            System.err.println(e+" cannot be dialogued: is not from "+this);
            return;
        }
        focus = null;
        if (dialog == null) {
            dialog = e;
            e.applyOnVisible();
        }
    }

    public final void clearDialog() {
        if (dialog != null) dialog.applyOnInvisible();
        dialog = null;
    }

    public final Panel getDialog() {
        return (Panel) dialog;
    }

    public final void addComponent(GUIElement g) {
        if (!components.contains(g)) {
            components.add(g);
            g.setGUI(this);
        }
    }

    public final void removeComponent(int index) {
        GUIElement removed = components.remove(index);
        removed.setGUI(null);
    }

    public final void update() {
        for (GUIElement g : components) {
            g.update();
        }
        dialog_alpha += MiscMath.getConstant(dialog != null ? 1 : -1, 1);
        dialog_alpha = (float)MiscMath.clamp(dialog_alpha, 0, 0.5);
    }

    public final GUIElement getGUIElement(int onscreen_x, int onscreen_y) {
        if (dialog != null) {
            int[] g_dims = dialog.getOnscreenDimensions();
            if (MiscMath.pointIntersectsRect(onscreen_x, onscreen_y,
                    g_dims[0], g_dims[1], g_dims[2], g_dims[3]))
                return dialog.getGUIElement(onscreen_x - g_dims[0], onscreen_y - g_dims[1]);
        }
        for (int i = components.size() - 1; i > -1; i--) {
            GUIElement g = components.get(i);
            int[] g_dims = g.getOnscreenDimensions();
            if (g.isVisible() && MiscMath.pointIntersectsRect(onscreen_x, onscreen_y,
                    g_dims[0], g_dims[1], g_dims[2], g_dims[3])) {
                return g.getGUIElement(onscreen_x - g_dims[0], onscreen_y - g_dims[1]);
            }
        }
        return null;
    }

    public final GUIElement getHovered() {
        GUIElement hovered = getGUIElement(Mouse.getX(), Window.getHeight() - Mouse.getY());
        if (hovered == null) return null;
        if (dialog != null) {
            if (hovered.isDialog()) return hovered;
        } else {
            return hovered;
        }
        return null;
    }

    /*public final void createCanvas(int width, int height) {
        try {
            gfx_img = new Image(width, height);
            canvas = gfx_img.getGraphics();
            allocated = new ArrayList<int[]>();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    public final int[] findFreeCanvasSpace(GUIElement e) {
        if (allocated.isEmpty()) return new int[]{0, 0, 0, 0};

        int[] e_dims = e.getOnscreenDimensions();
        for (int[] a: allocated) {
            for (int[] a2: allocated) {
                if (a2.equals(a)) continue;
                if (!MiscMath.rectanglesIntersect(a2[0], a2[1], a2[2], a2[3],
                        a[0] + a[2] + 1, a[1] + 1, e_dims[2], e_dims[3])) {
                    int[] rect = new int[]{a[0] + a[2] + 1, a[1] + 1, e_dims[2], e_dims[3]};
                    allocated.add(rect);
                    return rect;
                }
                if (!MiscMath.rectanglesIntersect(a2[0], a2[1], a2[2], a2[3],
                        a[0] + 1, a[1] + a[3] + 1, e_dims[2], e_dims[3])) {
                    int[] rect = new int[]{a[0] + a[2] + 1, a[1] + 1, e_dims[2], e_dims[3]};
                    allocated.add(rect);
                    return rect;
                }
            }
        }
        return new int[]{-1, -1};
    }

    private final void destroyCanvas() {
        try {
            if (gfx_img != null) gfx_img.destroy();
            if (canvas != null) canvas.destroy();
            canvas = null;
            gfx_img = null;
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    public final Graphics getCanvas() {
        return canvas;
    }

    public final Image getImage(GUIElement e) {
        int[] dims = e.getOnscreenDimensions();
        return gfx_img.getSubImage(e.getCanvasLocation()[0], e.getCanvasLocation()[1], dims[2], dims[3]);
    }*/

    public final void draw(Graphics g) {

        /*if (gfx_img == null) return;
        if (canvas == null) return;

        canvas.clear();
        allocated.clear();*/

        for (GUIElement e : components) {
            if (e.isVisible() && !e.isDialog()) e.draw(g);
        }

        //draw dialog dark bg
        if (dialog_alpha > 0) {
            g.setColor(new Color(0, 0, 0, (int) (dialog_alpha * 255)));
            g.fillRect(0, 0, Window.getWidth(), Window.getHeight());
        }

        if (dialog != null) {
            dialog.draw(g);
        }
    }

    public final boolean applyMouseClick(int button, int x, int y, int click_count) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (dialog != null && !components.get(i).isDialog()) continue;
            if (components.get(i).applyMouseClick(button, x, y, click_count)) return true;
        }
        return false;
    }

    /**
     * Applies the mouse press to all components.
     * A component will take action if the mouse intersects and if its
     * is not restricted by the dialog, if any.
     *
     * @param button Mouse button.
     * @param x      Onscreen mouse x.
     * @param y      Onscreen mouse y.
     * @return
     */
    public final boolean applyMousePress(int button, int x, int y) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (dialog != null && !components.get(i).isDialog()) continue;
            if (components.get(i).applyMousePress(button, x, y)) return true;
        }
        return false;
    }

    public final boolean applyMouseRelease(int button, int x, int y) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (dialog != null && !components.get(i).isDialog()) continue;
            components.get(i).applyMouseRelease(button, x, y);
        }
        return true;
    }

    public final boolean applyKeyPress(char c) {
        if (focus == null) return false;
        focus.onKeyPress(c);
        return true;
    }

    public final boolean applyMouseScroll(int x, int y, int dir) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (dialog != null && !components.get(i).isDialog()) continue;
            components.get(i).applyMouseScroll(x, y, dir);
        }
        return true;
    }

}
