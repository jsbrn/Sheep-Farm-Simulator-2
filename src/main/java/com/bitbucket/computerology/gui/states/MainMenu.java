package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.Button;
import com.bitbucket.computerology.gui.elements.ListElement;
import com.bitbucket.computerology.gui.elements.Panel;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;

public class MainMenu extends BasicGameState {

    public static GUI GUI;
    static double bg_x = 0, bg_dx = -50;
    StateBasedGame game;
    boolean initialized = false;

    Panel main_menu, world_select_menu, saves_panel;

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

        world_select_menu = new Panel();
        world_select_menu.anchorMiddle(null, 0, 0);
        world_select_menu.setWidth(500);
        world_select_menu.setHeight(600);
        world_select_menu.setVisible(true);

        main_menu = new Panel();
        main_menu.anchorMiddle(null, 0, 0);
        main_menu.setWidth(300);
        main_menu.setHeight(200);

        saves_panel = new Panel() {

            @Override
            public void refresh() {
                getComponents().clear();
                File save_dir = new File(Assets.ROOT_DIR+"/saves");
                File[] saves = save_dir.listFiles();
                for (int i = 0; i < 5; i++) {
                    ListElement l = new ListElement(i);
                    l.anchor(this, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 0);
                    l.anchor(this, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, 0);
                    l.anchor(this, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, i*100);
                    l.setHeight(100);
                    this.addComponent(l);
                }
            }

        };
        saves_panel.anchor(world_select_menu, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
        saves_panel.anchor(world_select_menu, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
        saves_panel.anchor(world_select_menu, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 10);
        saves_panel.anchor(world_select_menu, GUIElement.ANCHOR_BOTTOM, GUIElement.ANCHOR_BOTTOM, -10);
        saves_panel.showBackground(false);
        world_select_menu.addComponent(saves_panel);

        Button play_button = new Button("Play", Color.green, Color.white) {

            public void onMousePress(int button, int x, int y) {
                if (button == 0) {
                    world_select_menu.setVisible(true);
                    main_menu.setVisible(false);
                    saves_panel.refresh();
                }
            }

        };
        play_button.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
        play_button.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 10);
        play_button.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
        play_button.setHeight(24);
        main_menu.addComponent(play_button);


        Button back_button = new Button("Back", Color.black, Color.white) {

            public void onMousePress(int button, int x, int y) {
                if (button == 0) {
                    world_select_menu.setVisible(false);
                    main_menu.setVisible(true);
                }
            }

        };
        back_button.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 40);
        back_button.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
        back_button.setWidth(100);
        back_button.setHeight(24);
        world_select_menu.addComponent(back_button);

        GUI.addComponent(main_menu);
        GUI.addComponent(world_select_menu);

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
