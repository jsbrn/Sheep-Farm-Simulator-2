package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.Panel;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class MainMenu extends BasicGameState {

    public static GUI GUI;
    static double bg_x = 0, bg_dx = -50;
    StateBasedGame game;
    boolean initialized = false;

    public MainMenu(int state) {
    }

    @Override
    public int getID() {
        return Assets.MAIN_MENU;
    }

    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
        if (initialized) return;
        game = sbg;
        GUI = new GUI();

        Panel main_menu = new Panel();

        //main_menu.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
        //main_menu.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 10);
        //main_menu.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
        //main_menu.anchor(null, GUIElement.ANCHOR_BOTTOM, GUIElement.ANCHOR_BOTTOM, -10);
        main_menu.anchorMiddle(null, 200, 200);
        GUI.addComponent(main_menu);

        initialized = true;
    }


    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
        if (!Assets.loaded()) return;
        g.drawImage(Assets.MAIN_MENU_BACKGROUND
                .getScaledCopy(Display.getWidth(), Display.getHeight()), 0, 0);
        GUI.draw(g);

    }

    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        MiscMath.DELTA_TIME = delta;
        GUI.update();
    }

    @Override
    public void mouseClicked(int button, int x, int y, int click_count) {
        GUI.applyMouseClick(button, x, y, click_count);
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        GUI.applyMousePress(button, x, y);
    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        GUI.applyMouseRelease(button, x, y);
    }

    public void keyPressed(int key, char c) {
        GUI.applyKeyPress(c);
    }
}
