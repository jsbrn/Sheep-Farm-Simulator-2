package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.gui.elements.Panel;
import com.bitbucket.computerology.misc.MiscMath;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class GUI {
    
    ArrayList<GUIElement> components;
    private GUIElement focus, dialog;
    
    Image dialog_shadow;
    
    public GUI() {
        this.components = new ArrayList<GUIElement>();
        try {
            this.dialog_shadow = new Image("images/dialog_shadow.png", false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Gets the list of components in this GUI instance.
     * @return An ArrayList of GUIElement instances.
     */
    public final ArrayList<GUIElement> getComponents() {
        return components;
    }
    
    /**
     * Gets the element currently focussed on
     * @return 
     */
    public final GUIElement getFocus() {
        return focus;
    }
    
    /**
     * Currently only works properly for top-level components.
     * Locks all external input such that only input on e is
     * accepted. Clears keyboard focus.
     * @param e The element you wish to restrict input to. 
     */
    public final void dialog(Panel e) {
        if (dialog == null) dialog = e;
        focus = null;
    }
    
    public final void undialog() { dialog = null; }
    
    public final Panel getDialog() { return (Panel)dialog; }
    
    public final void setFocus(GUIElement g) {
        focus = g;
    }
    
    public final void addComponent(GUIElement g) {
        if (!components.contains(g)) { components.add(g); g.setGUI(this); }
    }
    
    public final void removeComponent(int index) {
        GUIElement removed = components.remove(index);
        removed.setGUI(null);
    }
    
    public final void update() {
        for (GUIElement g: components) { 
            if (g.isVisible()) g.update();
        }
    }
    
    public final void reset() {
        for (GUIElement g: components) { 
            g.reset();
        }
    }
    
    public final GUIElement getGUIElement(int onscreen_x, int onscreen_y) {
        for (int i = components.size()-1; i > -1; i--) {
            GUIElement g = components.get(i);
            if (g.isVisible() && MiscMath.pointIntersects(onscreen_x, onscreen_y, 
                g.getOnscreenX(), g.getOnscreenY(), g.getWidth(), g.getHeight())) {
                return g.getGUIElement(onscreen_x-g.getOnscreenX(), onscreen_y-g.getOnscreenY());
            }
        }
        return null;
    }
    
    public final GUIElement getHovered() {
        GUIElement hovered = getGUIElement(Mouse.getX(), Display.getHeight() - Mouse.getY());
        if (hovered == null) return null;
        if (dialog != null) {
            if (hovered.isDialog()) return hovered;
        } else {
            return hovered;
        }
        return null;
    }
    
    public final void draw(Graphics g) {
        for (GUIElement e: components) { 
            if (e.isVisible()) { 
                if (!e.equals(dialog)) {
                    e.draw(g); 
                }
            } 
        }
        if (dialog != null) {
            g.setColor(new Color(0, 0, 0, 125));
            g.fillRect(0, 0, Display.getWidth(), Display.getHeight());
            dialog.draw(g);
        }
    }
    
    public final boolean applyMouseClick(int button, int x, int y, int click_count) {
        for (int i = components.size()-1; i >= 0; i--) {
            if (dialog != null && !components.get(i).equals(dialog)) continue;
            if (components.get(i).applyMouseClick(button, x, y, click_count)) return true;
        }
        return false;
    }
    
    /**
     * Applies the mouse press to all components.
     * A component will take action if the mouse intersects and if its
     * is not restricted by the dialog, if any.
     * @param button Mouse button.
     * @param x Onscreen mouse x.
     * @param y Onscreen mouse y.
     * @return 
     */
    public final boolean applyMousePress(int button, int x, int y) {
        for (int i = components.size()-1; i >= 0; i--) {
            if (dialog != null && !components.get(i).equals(dialog)) continue;
            if (components.get(i).applyMousePress(button, x, y)) return true;
        }
        return false;
    }
    
    public final boolean applyMouseRelease(int button, int x, int y) {
        for (int i = components.size()-1; i >= 0; i--) {
            if (dialog != null && !components.get(i).equals(dialog)) continue;
            components.get(i).applyMouseRelease(button, x, y);
        }
        return true;
    }
    
    public final boolean applyKeyPress(char c) {
        if (focus == null) return false;
        focus.onKeyPress(c);
        return true;
    }
    
}
