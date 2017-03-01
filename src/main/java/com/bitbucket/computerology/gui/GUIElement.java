package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.Window;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;

public class GUIElement {

    public static final int ANCHOR_MID_X = 0, ANCHOR_MID_Y = 1,
            ANCHOR_LEFT = 2, ANCHOR_TOP = 3, ANCHOR_RIGHT = 4, ANCHOR_BOTTOM = 5;

    private ArrayList<GUIElement> components;
    private GUIElement parent;
    private boolean visible = true, enabled = true;
    private GUI gui; //the GUI that owns this element

    private int dims[]; //the values used when no anchors are found
    private Object[][] anchors;

    public GUIElement() {
        this.components = new ArrayList<GUIElement>();
        this.parent = null;
        this.anchors = new Object[6][4];
        this.dims = new int[]{0, 0, 100, 100};
    }

    public void anchor(GUIElement parent, int mode, int parent_mode, int offset) {
        if (mode < -1 || mode >= anchors.length) return;
        if (parent_mode < -1 || parent_mode >= anchors.length) return;
        anchors[mode][0] = parent;
        anchors[mode][1] = mode;
        anchors[mode][2] = parent_mode;
        anchors[mode][3] = offset;
    }

    public void clearAnchors() {
        anchors = new Object[6][4];
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
    public GUIElement getGUIElement(int x, int y) {
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
        for (GUIElement e : components) {
            e.draw(g);
        }
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
     * Can be overridden. Resets the element back to its default state.
     * Call super.update() to reset sub-components.
     */
    public final void reset() {
        for (GUIElement g : components) g.reset();
    }

    /**
     * Returns the onscreen x, y, w, and h of the element.
     * @return An int[4].
     */
    public int[] getOnscreenDimensions() {

        int[] osc_dims = new int[]{dims[0], dims[1], dims[2], dims[3]};

        for (Object[] anchor: anchors) {
            if (anchor[1] == null) continue;
            GUIElement e = anchor[0] == null ? parent : (GUIElement)anchor[0];
            int[] p_dims = e != null ? e.getOnscreenDimensions()
                    : new int[]{0, 0, Window.getWidth(), Window.getHeight()};
            int mode = (Integer)anchor[1];
            int parent_mode = (Integer)anchor[2];
            int offset = (Integer)anchor[3];

            if (mode != ANCHOR_MID_X) {
                if (mode == ANCHOR_LEFT) {
                    osc_dims[0] = offset + p_dims[0]
                            + (parent_mode == ANCHOR_MID_X ? p_dims[2] / 2
                            : (parent_mode == ANCHOR_RIGHT ? p_dims[2] : 0));
                }
                if (mode == ANCHOR_RIGHT) {
                    osc_dims[2] = offset + p_dims[0] - osc_dims[0]
                            + (parent_mode == ANCHOR_MID_X ? p_dims[2] / 2
                            : (parent_mode == ANCHOR_RIGHT ? p_dims[2] : 0));
                }
            } else {
                osc_dims[2] = dims[2];
                osc_dims[0] = p_dims[0] - (osc_dims[2] / 2)
                        + (parent_mode == ANCHOR_MID_X ? p_dims[2] / 2
                        : (parent_mode == ANCHOR_RIGHT ? p_dims[2] : 0));
            }

            if (mode != ANCHOR_MID_Y) {
                if (mode == ANCHOR_TOP) {
                    osc_dims[1] = offset + p_dims[1]
                            + (parent_mode == ANCHOR_MID_Y ? p_dims[3] / 2
                            : (parent_mode == ANCHOR_BOTTOM ? p_dims[3] : 0));
                }
                if (mode == ANCHOR_BOTTOM) {
                    osc_dims[1] = offset + p_dims[1] - osc_dims[3]
                            + (parent_mode == ANCHOR_MID_Y ? p_dims[3] / 2
                            : (parent_mode == ANCHOR_BOTTOM ? p_dims[3] : 0));
                }
            } else {
                osc_dims[3] = dims[3];
                osc_dims[1] = p_dims[1] - (osc_dims[3] / 2)
                        + (parent_mode == ANCHOR_MID_Y ? p_dims[3] / 2
                        : (parent_mode == ANCHOR_BOTTOM ? p_dims[3] : 0));
            }

        }

        return osc_dims;

    }

    public final void setX(int x) { if (!anchored(ANCHOR_LEFT)) dims[0] = x; }
    public final void setY(int y) { if (!anchored(ANCHOR_TOP)) dims[1] = y; }
    public final void addX(int x) { setX(dims[0]+x); }
    public final void addY(int y) { setY(dims[1]+y); }

    public final void setWidth(int width) { if (anchors[2][1] == null) dims[2] = width; }
    public final void setHeight(int height) { if (anchors[3][1] == null) dims[3] = height; }
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
