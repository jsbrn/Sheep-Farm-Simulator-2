package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.Button;
import com.bitbucket.computerology.gui.elements.Label;
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

    Panel main_menu, world_select_menu;

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

        world_select_menu = new Panel() {

            @Override
            public void refresh() {
                getComponents().clear();
                //header label
                Label head = new Label();
                head.setText("Choose a save...");
                head.setFontSize(20);
                head.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
                head.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 10);
                addComponent(head);


                //build the list of save files
                File save_dir = new File(Assets.ROOT_DIR+"/saves");
                File[] saves = save_dir.listFiles();
                for (int i = 0; i < saves.length; i++) {
                    final File curr_save = saves[i];
                    ListElement le = new ListElement(i);
                    le.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
                    le.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_MID_X, -10);
                    le.anchor(head, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_BOTTOM, 10 + (i*40));
                    le.setHeight(40);

                    Label label = new Label();
                    label.setText(curr_save.getName());
                    label.anchor(null, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
                    label.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
                    le.addComponent(label);

                    Button play = new Button("Play!", Color.green, Color.white);
                    play.setWidth(48);
                    play.setHeight(24);
                    play.anchor(null, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
                    play.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
                    le.addComponent(play);

                    Button delete = new Button("Delete", Color.red, Color.white) {
                        @Override
                        public void onMousePress(int button, int x, int y) {
                            if (button == 0) {
                                curr_save.delete();
                                getParent().getParent().refresh();
                            }
                        }
                    };
                    delete.setWidth(48);
                    delete.setHeight(24);
                    delete.anchor(play, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
                    delete.anchor(play, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_LEFT, -10);
                    le.addComponent(delete);

                    this.addComponent(le);

                }
            }

        };
        world_select_menu.anchor(null, GUIElement.ANCHOR_MID_X, GUIElement.ANCHOR_MID_X, 0);
        world_select_menu.anchor(null, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
        world_select_menu.setWidth(500);
        world_select_menu.setHeight(600);
        world_select_menu.setVisible(false);

        main_menu = new Panel();
        main_menu.anchor(null, GUIElement.ANCHOR_MID_X, GUIElement.ANCHOR_MID_X, 0);
        main_menu.anchor(null, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
        main_menu.setWidth(300);
        main_menu.setHeight(200);
        main_menu.setResizable(false);
        main_menu.setDraggable(false);
        Button play_button = new Button("Play", Color.green, Color.white) {

            public void onMousePress(int button, int x, int y) {
                if (button == 0) {
                    world_select_menu.setVisible(true);
                    main_menu.setVisible(false);
                    world_select_menu.refresh();
                }
            }

        };
        play_button.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
        play_button.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 10);
        play_button.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
        play_button.setHeight(24);
        main_menu.addComponent(play_button);


        /*Button back_button = new Button("Back", Color.black, Color.white) {

            public void onMousePress(int button, int x, int y) {
                if (button == 0) {
                    world_select_menu.setVisible(false);
                    main_menu.setVisible(true);
                }
            }

        };
        back_button.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 10);
        back_button.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
        back_button.setWidth(300);
        back_button.setHeight(24);
        world_select_menu.addComponent(back_button);*/

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
