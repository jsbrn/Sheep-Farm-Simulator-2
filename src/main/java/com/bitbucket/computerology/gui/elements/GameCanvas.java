package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.states.GameScreen;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;

public class GameCanvas extends GUIElement {
    
    boolean dragging = false;
    int last_x = 0, last_y = 0;
    
    public GameCanvas() {

    }
    
    @Override
    public void update() {
        if (dragging) {
            Camera.move(last_x-Mouse.getX(), last_y-(Display.getHeight()-Mouse.getY()));
            last_x = Mouse.getX();
            last_y = Display.getHeight()-Mouse.getY();
        }
        super.update();
    }
    
    @Override
    public int getWidth() {
        return Display.getWidth();
    }
    
    @Override
    public int getHeight() {
        return Display.getHeight();
    }
    
    @Override
    public int getOnscreenX() {
        return 0;
    }
    
    @Override
    public int getOnscreenY() {
        return 0;
    }
    
    @Override
    public void onMousePress(int button, int x, int y) {
        dragging = true;
        last_x = x; last_y = y;
        grabFocus();
    }
    
    @Override
    public void onKeyPress(char c) {
        if (!(c == 'x' && GameScreen.DEBUG_MODE)) return;
        int wc[] = World.getWorld().getWorldCoords(last_x, last_y);
        Entity e = Entity.create("Sheep");
        e.setWorldX(wc[0]);
        e.setWorldY(wc[1]);
        World.getWorld().addEntity(e);
    }
    
    @Override
    public void onMouseRelease(int button, int x, int y) {
        dragging = false;
    }
    
    @Override
    public void draw(Graphics g) {
        if (World.getWorld() != null) World.getWorld().draw(g);
    }

}
