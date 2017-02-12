package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.misc.MiscMath;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;

public class GUIElement {

    public static final int ANCHOR_TOPLEFT = 0, ANCHOR_TOP = 1, ANCHOR_TOPRIGHT = 2,
        ANCHOR_LEFT = 3, ANCHOR_MIDDLE = 4, ANCHOR_RIGHT = 5, ANCHOR_BOTTOMLEFT = 6,
        ANCHOR_BOTTOM = 7, ANCHOR_BOTTOMRIGHT = 8;
    private static final double[][][] anchor_values = {
        {{0, 0}, {0.5, 0}, {1, 0}},
        {{0, 0.5}, {0.5, 0.5}, {1, 0.5}},
        {{0, 1}, {0.5, 1}, {1, 1}}
    };


    private ArrayList<GUIElement> components;
    private GUIElement parent;
    private boolean visible = true, enabled = true;
    private GUI gui; //the GUI that owns this element

    private int dims[];

    private ArrayList<int[]> anchors;

    public GUIElement() {
        this.components = new ArrayList<GUIElement>();
        this.parent = null;
        this.anchors = new ArrayList<int[]>();
        this.dims = new int[]{0, 0, 100, 100};
    }

    public void anchor(int this_point, int parent_point, int offset_x, int offset_y) {
        anchors.add(new int[]{this_point, parent_point, offset_x, offset_y});
    }

    public void clearAnchors() { anchors.clear(); }

    /**
     * Returns the multiplier found in anchor_values, above.
     * @param i The index of the anchor point:<br><br>
     *              0 1 2<br>
     *              3 4 5<br>
     *              6 7 8<br>
     * @return A double[] containing the width/height multipliers for each
     * axis.
     */
    private double[] anchorMultiplier(int i) {
        int x = (i % 3);
        int y = (i / 3);
        return anchor_values[y][x];
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean e) {
        enabled = e;
    }

    public GUI getGUI() {
        return (gui != null) ? gui : (parent != null ? parent.getGUI() : null);
    }

    public final void setGUI(GUI g) {
        gui = g;
    }

    /**
     * A function that can be overridden to refresh specific components in the element,
     * like perhaps reloading a list of buttons to match a list of options
     */
    public void refresh() {
        for (GUIElement g : components) g.refresh();
    }

    public final boolean hasFocus() {
        if (getGUI() == null) return false;
        return this.equals(getGUI().getFocus());
    }

    public final void grabFocus() {
        if (getGUI() == null) return;
        getGUI().setFocus(this);
    }

    public final void releaseFocus() {
        if (getGUI() == null) return;
        if (getGUI().getFocus() == null) return;
        if (getGUI().getFocus().equals(this)) getGUI().setFocus(null);
    }

    public final boolean isVisible() {
        return visible;
    }

    public final void setVisible(boolean v) {
        visible = v;
    }

    public final ArrayList<GUIElement> getComponents() {
        return components;
    }

    public final void addComponent(GUIElement g) {
        if (!components.contains(g)) {
            components.add(g);
            g.setParent(this);
        }
    }

    public final void removeComponent(int index) {
        GUIElement removed = components.remove(index);
        removed.setParent(null);
    }

    public final GUIElement getParent() {
        return parent;
    }

    public final void setParent(GUIElement p) {
        parent = p;
    }

    public boolean mouseHovering() {
        if (getGUI() == null) return false;
        if (getGUI().getHovered() == null) return false;
        return equals(getGUI().getHovered());
    }

    public boolean mouseIntersecting() {
        int[] dims = getOnscreenDimensions();
        return isVisible() && MiscMath.pointIntersectsRect(Mouse.getX(), Display.getHeight() - Mouse.getY(),
                dims[0], dims[1], dims[2], dims[3]);
    }


    /**
     * @param x The x coordinate relative to the on-screen x of its parent.
     * @param y The y coordinate relative to the on-screen y of its parent.
     * @return
     */
    public GUIElement getGUIElement(int x, int y) {
        for (int i = components.size() - 1; i > -1; i--) {
            GUIElement g = components.get(i);
            int[] dims = getOnscreenDimensions();
            int[] g_dims = g.getOnscreenDimensions();
            if (g.isVisible() && MiscMath.pointIntersectsRect(g_dims[0] + x, g_dims[1] + y,
                    g_dims[0], g_dims[1], g_dims[2], g_dims[3])) {
                return g.getGUIElement(dims[0] + x - g_dims[0], dims[1] + y - g_dims[1]);
            }
        }
        return this;
    }

    /**
     * Is the element or one of its parents marked as a dialog in the GUI?
     * (Anything that is not will not accept input)
     *
     * @return True if yes, false if no.
     */
    public boolean isDialog() {
        if (getGUI() == null) return false;
        if (parent == null) return equals(getGUI().getDialog());
        return parent.isDialog();
    }

    /**
     * Can be overridden, so call super.update() if the element has subcomponents.
     */
    public void update() {
        for (GUIElement g : components) g.update();
    }

    public void draw(Graphics g) {
        for (GUIElement e : components) e.draw(g);
    }

    /**
     * Can be overridden. Resets the element back to its default state.
     * Call super.update() to reset sub-components.
     */
    public void reset() {
        for (GUIElement g : components) g.reset();
    }

    /**
     * Returns the onscreen x, y, w, and h of the element.
     * @return An int[4].
     */
    public int[] getOnscreenDimensions() {

        if (anchors.isEmpty()) return dims;

        int[] osc_dims = {Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MIN_VALUE};
        int[] parent_dims =
                parent == null ? new int[]{0, 0, Display.getWidth(), Display.getHeight()}
                    : parent.getOnscreenDimensions();

        for (int[] anchor: anchors) {
            int i = (int)((parent_dims[2] * anchorMultiplier(anchor[1])[0]) //parent anchor point
                    + anchor[2]); //offset x
            int j = (int)((parent_dims[3] * anchorMultiplier(anchor[1])[1]) //parent anchor point
                    + anchor[3]); //offset y
            if (j > osc_dims[3] && i > osc_dims[2]) {
                osc_dims[2] = i;
                osc_dims[3] = j;
            }

            if (j > osc_dims[1] && i > osc_dims[0]) {
                osc_dims[0] = i;
                osc_dims[1] = j;
            }
        }
        return new int[]{
                (osc_dims[0] == Integer.MAX_VALUE ? dims[0] : osc_dims[0]) + parent_dims[0],
                (osc_dims[1] == Integer.MAX_VALUE ? dims[1] : osc_dims[1]) + parent_dims[1],
                (osc_dims[2] == Integer.MIN_VALUE ? dims[2] : osc_dims[2]),
                (osc_dims[3] == Integer.MIN_VALUE ? dims[3] : osc_dims[3]),
        };
    }

    public void setX(int x) { dims[0] = x; }
    public void setY(int y) { dims[1] = y; }
    public void addX(int x) { dims[0] += x; }
    public void addY(int y) { dims[1] += y; }

    public void setWidth(int width) { dims[0] = width; }
    public void setHeight(int height) { dims[1] = height; }
    public void addWidth(int width) { dims[0] += width; }
    public void addHeight(int height) { dims[1] += height; }

    /**
     * Take the clicked mouse button (check if clicked first before calling),
     * and passes it through to all elements and subelements of this GUI element.
     *
     * @param button The mouse button.
     * @return true if an element was clicked, false otherwise
     */
    public final boolean applyMouseClick(int button, int x, int y, int click_count) {
        if (!isVisible() || !enabled()) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).applyMouseClick(button, x, y, click_count)) return true;
        }
        int[] dims = getOnscreenDimensions();
        if (MiscMath.pointIntersectsRect(x, y,
                dims[0], dims[1], dims[2], dims[3])) {
            onMouseClick(button, x, y, click_count);
            return true;
        }
        return false;
    }

    public final boolean applyMousePress(int button, int x, int y) {
        if (!isVisible() || !enabled()) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).applyMousePress(button, x, y)) return true;
        }
        int[] dims = getOnscreenDimensions();
        if (MiscMath.pointIntersectsRect(x, y,
                dims[0], dims[1], dims[2], dims[3])) {
            onMousePress(button, x, y);
            return true;
        }
        return false;
    }

    public final boolean applyMouseRelease(int button, int x, int y) {
        if (!isVisible() || !enabled()) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).applyMouseRelease(button, x, y);
        }
        onMouseRelease(button, x, y);
        return true;
    }

    public final boolean applyMouseScroll(int x, int y, int dir) {
        if (!isVisible() || !enabled() || !hasFocus()) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).applyMouseScroll(x, y, dir);
        }
        onMouseScroll(x, y, dir);
        return true;
    }

    public void onMouseClick(int button, int x, int y, int click_count) {}

    public void onMousePress(int button, int x, int y) {}

    public void onMouseRelease(int button, int x, int y) {}

    public void onKeyPress(char c) {}

    public void onMouseScroll(int x, int y, int dir) {}

}
