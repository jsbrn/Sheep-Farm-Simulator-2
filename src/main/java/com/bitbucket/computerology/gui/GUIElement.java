package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.gui.elements.Panel;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.Window;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;

public class GUIElement {

    public static final int ANCHOR_LEFT = 0, ANCHOR_MID_X = 1,
            ANCHOR_RIGHT = 2, ANCHOR_TOP = 3, ANCHOR_MID_Y = 4, ANCHOR_BOTTOM = 5;

    private ArrayList<GUIElement> components;
    private GUIElement parent;
    private boolean visible = true, enabled = true;
    private GUI gui; //the GUI that owns this element

    private int dims[]; //the values used when no anchors are found
    private Object[][] anchors;

    public GUIElement() {
        this.components = new ArrayList<GUIElement>();
        this.parent = null;
        this.anchors = new Object[6][3];
        this.dims = new int[]{0, 0, 100, 100};
    }

    public void anchor(GUIElement parent, int edge, double percentage, int offset) {
        if (edge < -1 || edge >= anchors.length) return;
        anchors[edge][0] = parent;
        anchors[edge][1] = percentage;
        anchors[edge][2] = offset;
    }

    public boolean anchored(int mode) {
        if (mode < -1 || mode >= anchors.length) return false;
        return anchors[mode][1] != null;
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
        if (visible) applyOnVisible();
        if (!visible) applyOnInvisible();
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

    /**
     * @param x The x coordinate relative to the on-screen x of its parent.
     * @param y The y coordinate relative to the on-screen y of its parent.
     * @return
     */
    public final GUIElement getGUIElement(int x, int y) {
        for (int i = components.size() - 1; i > -1; i--) {
            GUIElement g = components.get(i);
            int[] dims = getOnscreenDimensions();
            int[] g_dims = g.getOnscreenDimensions();
            if (g.isVisible() && MiscMath.pointIntersectsRect(dims[0] + x, dims[1] + y,
                    g_dims[0], g_dims[1], g_dims[2], g_dims[3])) {
                return g.getGUIElement(dims[0] + x - g_dims[0], dims[1] + y - g_dims[1]);
            }
        }
        return this;
    }

    /**
     * Is the element or one of its parents marked as a dialog in the GUI?
     * (While there is a dialog being shown, other elements will not accept key input)
     *
     * @return True if yes, false if no.
     */
    public final boolean isDialog() {
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
        for (GUIElement e : components) {
            e.draw(g);
        }
        g.setColor(Color.red);
        int[] dims = getOnscreenDimensions();
        g.drawRect(dims[0], dims[1], dims[2], dims[3]);
    }

    /*protected void setCanvasLocation(int[] loc) {
        canvas_location = loc;
    }

    protected int[] getCanvasLocation() {
        return canvas_location;
    }

    private final void drawComponents(Graphics g) {
        int[] dims = getOnscreenDimensions();
        for (GUIElement e : components) {
            if (e.getGUI().getImage(e) == null) continue;
            int[] e_dims = e.getOnscreenDimensions();
            int[] union = new int[]{
                    (int)MiscMath.max(0, dims[0]-e_dims[0]),
                    (int)MiscMath.max(0, dims[1]-e_dims[1]),
                    (int)MiscMath.min(e_dims[2], (dims[2]+dims[0])-e_dims[0]),
                    (int)MiscMath.min(e_dims[3], (dims[3]+dims[1])-e_dims[1])
            };
            Image i = e.getGUI().getImage(e).getSubImage(union[0], union[1], union[2], union[3]);
            g.drawImage(i, e_dims[0], e_dims[1]);
            e.drawComponents(g);
        }
    }*/

    /**
     * Returns the onscreen x, y, w, and h of the element.
     * @return An int[4].
     */
    public int[] getOnscreenDimensions() {

        int[] points = new int[]{dims[0], dims[1], dims[2], dims[3]};

        //if this is a panel and is the current dialog, return the middle of window
        if (isDialog() && getParent() == null) {
            points[0] = (Window.getWidth()/2) - (dims[2]/2);
            points[1] = (Window.getHeight()/2) - (dims[3]/2);
            points[2] = dims[2];
            points[3] = dims[3];
            return points;
        }

        //otherwise, continue with calculating the anchors
        for (int i = 0; i < anchors.length; i++) {
            Object[] anchor = anchors[i];
            if (!anchored(i)) continue; //if anchor not set
            GUIElement anchored_to = anchor[0] == null ? parent : (GUIElement) anchor[0];
            int[] p_dims = anchored_to != null ? anchored_to.getOnscreenDimensions()
                    : new int[]{0, 0, Window.getWidth(), Window.getHeight()};
            int mode = i;
            double percent = (Double) anchor[1];
            int offset = (Integer) anchor[2];

            //if not anchored to the middle, handle the position of each edge
            if (!anchored(ANCHOR_MID_X)) {
                if (mode == ANCHOR_LEFT) points[0] = (int) (p_dims[0] + offset + ((double) p_dims[2] * percent));
                if (mode == ANCHOR_RIGHT) points[2] = (int) (p_dims[0] + offset + ((double) p_dims[2] * percent));
            }
            if (!anchored(ANCHOR_MID_Y)) {
                if (mode == ANCHOR_TOP) points[1] = (int) (p_dims[1] + offset + ((double) p_dims[3] * percent));
                if (mode == ANCHOR_BOTTOM) points[3] = (int) (p_dims[1] + offset + ((double) p_dims[3] * percent));
            }

            //otherwise, use the specified dimensions to calculate the corner points
            //mid overrides the others
            if (mode == ANCHOR_MID_X) {
                points[0] = (p_dims[0] - (dims[2]/2)) + offset + (int)((double) p_dims[2] * percent);
                points[2] = points[0] + dims[2];
            }
            if (mode == ANCHOR_MID_Y) {
                points[1] = p_dims[1] - (dims[3]/2) + offset + (int)((double) p_dims[3] * percent);
                points[3] = points[1] + dims[3];
            }

        }

        /* if only one edge is anchored on an axis, adjust the other point to reflect the specified width in dims[]
         * but only if not anchored mid on that axis
         * (do for both axes)
         */
        if (!anchored(ANCHOR_MID_X)) {
            if (!anchored(ANCHOR_LEFT) && anchored(ANCHOR_RIGHT)) {
                points[0] = points[2] - dims[2];
            }
            if (anchored(ANCHOR_LEFT) && !anchored(ANCHOR_RIGHT)) {
                points[2] = points[0] + dims[2];
            }
        }
        if (!anchored(ANCHOR_MID_Y)) {
            if (!anchored(ANCHOR_TOP) && anchored(ANCHOR_BOTTOM)) {
                points[1] = points[3] - dims[3];
            }
            if (anchored(ANCHOR_TOP) && !anchored(ANCHOR_BOTTOM)) {
                points[3] = points[1] + dims[3];
            }
        }

        //since each pair is a coordinate, convert the second pair into width/height
        points[2] -= points[0]; points[3] -= points[1];

        return points;

    }

    public final void setX(int x) { if (!anchored(GUIElement.ANCHOR_LEFT)) dims[0] = x; }
    public final void setY(int y) { if (!anchored(GUIElement.ANCHOR_TOP)) dims[1] = y; }
    public final void addX(int x) { setX(dims[0]+x); }
    public final void addY(int y) { setY(dims[1]+y); }

    public final void setWidth(int width) { if (!anchored(GUIElement.ANCHOR_RIGHT)) dims[2] = width; }
    public final void setHeight(int height) { if (!anchored(GUIElement.ANCHOR_BOTTOM)) dims[3] = height; }
    public final void addWidth(int width) { setWidth(dims[2]+width); }
    public final void addHeight(int height) { setHeight(dims[3]+height); }

    /**
     * Take the clicked mouse button (check if clicked first before calling),
     * and passes it through to all elements and subelements of this GUI element.
     *
     * @param button The mouse button.
     * @return true if an element was clicked, false otherwise
     */
    public final boolean applyMouseClick(int button, int x, int y, int click_count) {
        if (!isDialog() && (!isVisible() || !enabled())) return false;
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
        if (!isDialog() && (!isVisible() || !enabled())) return false;
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
        if (!isDialog() && (!isVisible() || !enabled())) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).applyMouseRelease(button, x, y);
        }
        int[] dims = getOnscreenDimensions();
        onMouseRelease(button, x, y, MiscMath.pointIntersectsRect(x, y,
                    dims[0], dims[1], dims[2], dims[3]));
        return true;
    }

    public final boolean applyMouseScroll(int x, int y, int dir) {
        if (!isDialog() && (!isVisible() || !enabled())) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).applyMouseScroll(x, y, dir);
        }
        onMouseScroll(x, y, dir);
        return true;
    }

    public final boolean applyOnVisible() {
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).applyOnVisible();
        }
        onVisible();
        return true;
    }

    public final boolean applyOnInvisible() {
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).applyOnInvisible();
        }
        onInvisible();
        return true;
    }

    public void onMouseClick(int button, int x, int y, int click_count) {}

    public void onMousePress(int button, int x, int y) {}

    public void onMouseRelease(int button, int x, int y, boolean intersection) {}

    public void onKeyPress(char c) {}

    public void onMouseScroll(int x, int y, int dir) {}

    public void onVisible() {}

    public void onInvisible() {}

}
