package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.misc.MiscMath;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;

public class GUIElement {

    //describes what the x, y coords are relative to (i.e. with ORIGIN_RIGHT, an x value of -10 would
    //put the element's top left corner 10 pixels from the right edge of the window
    public static int ORIGIN_LEFT = 0, ORIGIN_RIGHT = 1, ORIGIN_TOP = 0, ORIGIN_BOTTOM = 1, ORIGIN_MIDDLE = 2;
    ArrayList<GUIElement> components;
    GUIElement parent;
    int x, y, width, height, offset_modes[];
    boolean visible = true, enabled = true;
    private GUI gui; //the GUI that owns this element

    public GUIElement() {
        this.components = new ArrayList<GUIElement>();
        this.offset_modes = new int[]{ORIGIN_LEFT, ORIGIN_TOP};
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
        this.parent = null;
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

    public final void setXOffsetMode(int mode) {
        offset_modes[0] = mode;
    }

    public final void setYOffsetMode(int mode) {
        offset_modes[1] = mode;
    }

    public final void addWidth(int w) {
        width += w;
    }

    public final void addHeight(int h) {
        height += h;
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final void addX(int x) {
        this.x += x;
    }

    public final void addY(int y) {
        this.y += y;
    }

    public int getWidth() {
        return width;
    }

    public final void setWidth(int w) {
        width = w;
    }

    public int getHeight() {
        return height;
    }

    public final void setHeight(int h) {
        height = h;
    }

    public boolean mouseHovering() {
        if (getGUI() == null) return false;
        if (getGUI().getHovered() == null) return false;
        return equals(getGUI().getHovered());
    }

    public boolean mouseIntersecting() {
        return isVisible() && MiscMath.pointIntersectsRect(Mouse.getX(), Display.getHeight() - Mouse.getY(),
                getOnscreenX(), getOnscreenY(), getWidth(), getHeight());
    }


    /**
     * @param x The x coordinate relative to the on-screen x of its parent.
     * @param y The y coordinate relative to the on-screen y of its parent.
     * @return
     */
    public GUIElement getGUIElement(int x, int y) {
        for (int i = components.size() - 1; i > -1; i--) {
            GUIElement g = components.get(i);
            if (g.isVisible() && MiscMath.pointIntersectsRect(getOnscreenX() + x, getOnscreenY() + y,
                    g.getOnscreenX(), g.getOnscreenY(), g.getWidth(), g.getHeight())) {
                return g.getGUIElement(getOnscreenX() + x - g.getOnscreenX(), getOnscreenY() + y - g.getOnscreenY());
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

    public int getOnscreenX() {
        if (parent == null) {
            if (offset_modes[0] == ORIGIN_MIDDLE) return Display.getWidth() / 2 + x;
            if (offset_modes[0] == ORIGIN_RIGHT) return Display.getWidth() + x;
            return x;
        } else {
            if (offset_modes[0] == ORIGIN_MIDDLE) return parent.getOnscreenX() + (parent.getWidth() / 2) + x;
            if (offset_modes[0] == ORIGIN_RIGHT) return parent.getOnscreenX() + parent.getWidth() + x;
            return parent.getOnscreenX() + x;
        }
    }

    public int getOnscreenY() {
        if (parent == null) {
            if (offset_modes[1] == ORIGIN_MIDDLE) return Display.getHeight() / 2 + y;
            if (offset_modes[1] == ORIGIN_RIGHT) return Display.getHeight() + y;
            return y;
        } else {
            if (offset_modes[1] == ORIGIN_MIDDLE) return parent.getOnscreenY() + (parent.getHeight() / 2) + y;
            if (offset_modes[1] == ORIGIN_RIGHT) return parent.getOnscreenY() + parent.getHeight() + y;
            return parent.getOnscreenY() + y;
        }
    }

    /**
     * Take the clicked mouse button (check if clicked first before calling),
     * and passes it through to all elements and subelements of this GUI element.
     *
     * @param button The mouse button.
     * @return true if an element was clicked, false otherwise
     */
    public boolean applyMouseClick(int button, int x, int y, int click_count) {
        if (!isVisible() || !enabled()) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).applyMouseClick(button, x, y, click_count)) return true;
        }
        if (MiscMath.pointIntersectsRect(x, y,
                getOnscreenX(), getOnscreenY(), getWidth(), getHeight())) {
            onMouseClick(button, x, y, click_count);
            return true;
        }
        return false;
    }

    public boolean applyMousePress(int button, int x, int y) {
        if (!isVisible() || !enabled()) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).applyMousePress(button, x, y)) return true;
        }
        if (MiscMath.pointIntersectsRect(x, y,
                getOnscreenX(), getOnscreenY(), getWidth(), getHeight())) {
            onMousePress(button, x, y);
            return true;
        }
        return false;
    }

    public boolean applyMouseRelease(int button, int x, int y) {
        if (!isVisible() || !enabled()) return false;
        for (int i = components.size() - 1; i >= 0; i--) {
            components.get(i).applyMouseRelease(button, x, y);
        }
        onMouseRelease(button, x, y);
        return true;
    }

    public void onMouseClick(int button, int x, int y, int click_count) {
    }

    public void onMousePress(int button, int x, int y) {
    }

    public void onMouseRelease(int button, int x, int y) {
    }

    public void onKeyPress(char c) {
    }

}
