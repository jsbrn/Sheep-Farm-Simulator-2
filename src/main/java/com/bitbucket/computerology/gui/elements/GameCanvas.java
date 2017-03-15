package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.states.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class GameCanvas extends GUIElement {

    boolean dragging = false;
    double last_x = 0, last_y = 0;

    public GameCanvas() {

    }

    @Override
    public void update() {
        if (dragging) {
            Camera.drag((last_x - Mouse.getX()) / Camera.getZoom(),
                    (last_y - (Display.getHeight() - Mouse.getY())) / Camera.getZoom());
            last_x = Mouse.getX();
            last_y = Display.getHeight() - Mouse.getY();
        }
        super.update();
    }

    @Override
    public int[] getOnscreenDimensions() {
        return new int[]{0, 0, Display.getWidth(), Display.getHeight()};
    }

    @Override
    public void onMousePress(int button, int x, int y) {
        dragging = true;
        last_x = x;
        last_y = y;
        grabFocus();
        if (button == 2) {
            if (Camera.getTarget() != null) Camera.setTarget(null);
            double[] wc = MiscMath.getWorldCoords(x, y);
            Entity e = World.getWorld().getEntity(wc[0], wc[1]);
            if (e != null) Camera.setTarget(e);
        }
    }

    @Override
    public void onKeyPress(char c) {

        if (c == 'm') GameScreen.DRAW_MAP = !GameScreen.DRAW_MAP;
        if (c == '`') GameScreen.DEBUG_MODE = !GameScreen.DEBUG_MODE;
        if (c == 27) GameScreen.GUI.dialog(GameScreen.pause_menu); //escape key

    }

    @Override
    public void onMouseScroll(int x, int y, int dir) {
        Camera.zoomAt(x, y, dir);
    }

    @Override
    public void onMouseRelease(int button, int x, int y, boolean intersection) {
        dragging = false;
    }

    @Override
    public void draw(Graphics g) {

        if (World.getWorld() != null) World.getWorld().draw(g);
        if (GameScreen.DEBUG_MODE) {
            g.setColor(Color.red);
            int sc[] = MiscMath.getOnscreenCoords(Camera.getX(), Camera.getY());
            g.fillRect(sc[0] - 2, sc[1] - 2, 4, 4);
        }

        super.draw(g);

    }

}
