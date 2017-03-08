package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.*;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class MainMenu extends BasicGameState {

    public static GUI GUI;
    private StateBasedGame game;
    private boolean initialized = false;

    private static Panel main_menu, world_select_menu, world_create_menu;

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

        /**
         * WORLD CREATION MENU
         */
        world_create_menu = buildWorldCreateMenu();
        GUI.addComponent(world_create_menu);

        /**
         * WORLD SELECT MENU
         */
        world_select_menu = new Panel() {

            @Override
            public void refresh() {
                getComponents().clear();

                Button back = new Button("Back", Color.black, Color.white) {

                    @Override
                    public void onMousePress(int button, int x, int y) {
                        world_select_menu.setVisible(false);
                        main_menu.setVisible(true);
                    }

                };
                back.setHeight(24);
                back.setWidth(48);
                back.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
                back.anchor(null, GUIElement.ANCHOR_BOTTOM, GUIElement.ANCHOR_BOTTOM, -10);
                this.addComponent(back);

                Button new_save = new Button("New...", Color.black, Color.white) {

                    @Override
                    public void onMousePress(int button, int x, int y) {

                        GUI.dialog(world_create_menu);

                    }

                };
                new_save.setHeight(24);
                new_save.setWidth(64);
                new_save.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
                new_save.anchor(null, GUIElement.ANCHOR_BOTTOM, GUIElement.ANCHOR_BOTTOM, -10);
                this.addComponent(new_save);

                Icon icon = new Icon();
                icon.setImage("images/gui/placeholder.png");
                icon.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
                icon.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, getHeaderHeight()+10);
                this.addComponent(icon);


                //build the list of save files
                File save_dir = new File(Assets.ROOT_DIR+"/saves");
                File[] saves = save_dir.listFiles();
                for (int i = 0; i < saves.length; i++) {
                    final File curr_save = saves[i];
                    ListElement le = new ListElement(i);
                    le.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
                    le.anchor(icon, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_LEFT, -10);
                    le.anchor(icon, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, i*40);
                    le.setHeight(40);

                    Label label = new Label();
                    label.setText(curr_save.getName());
                    label.anchor(null, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
                    label.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
                    le.addComponent(label);

                    Button play = new Button("Play!", Color.green, Color.white) {
                        @Override
                        public void onMousePress(int button, int x, int y) {
                            world_create_menu.refresh();
                        }
                    };
                    play.setWidth(48);
                    play.setHeight(24);
                    play.anchor(null, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
                    play.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
                    le.addComponent(play);

                    Button delete = new Button("Delete", Color.red, Color.white) {
                        @Override
                        public void onMousePress(int button, int x, int y) {
                            if (button == 0) {
                                deleteDirectory(curr_save);
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
        world_select_menu.setWidth(600);
        world_select_menu.setHeight(500);
        world_select_menu.setTitle("Choose a save...");
        world_select_menu.setVisible(false);
        world_select_menu.setClosable(false);
        world_select_menu.setResizable(false);
        world_select_menu.setDraggable(false);

        main_menu = new Panel();
        main_menu.anchor(null, GUIElement.ANCHOR_MID_X, GUIElement.ANCHOR_MID_X, 0);
        main_menu.anchor(null, GUIElement.ANCHOR_MID_Y, GUIElement.ANCHOR_MID_Y, 0);
        main_menu.setWidth(300);
        main_menu.setHeight(200);
        main_menu.setResizable(false);
        main_menu.setDraggable(false);
        main_menu.setClosable(false);

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

    private void deleteDirectory(File dir) {
        if (!dir.isDirectory()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) deleteDirectory(f); else f.delete();
        }
        dir.delete();
    }

    private static Panel buildWorldCreateMenu() {

        Panel p = new Panel() {
        };
        p.setWidth(250);
        p.setHeight(450);
        p.setTitle("Create world");
        p.setVisible(false);

        final TextField name_field = new TextField() {
            @Override
            public void onVisible() {
                System.out.println("Now visible!!!!");
                clearText();
                grabFocus();
            }
        };
        name_field.setAltText("Enter a name...");
        name_field.anchor(null, GUIElement.ANCHOR_LEFT, GUIElement.ANCHOR_LEFT, 10);
        name_field.anchor(null, GUIElement.ANCHOR_RIGHT, GUIElement.ANCHOR_RIGHT, -10);
        name_field.anchor(null, GUIElement.ANCHOR_TOP, GUIElement.ANCHOR_TOP, 10 + p.getHeaderHeight());
        name_field.setHeight(24);
        p.addComponent(name_field);

        Button create_btn = new Button("Create!", Color.black, Color.white) {

            @Override
            public void onMouseRelease(int button, int x, int y) {
                saveWorldSettings();
                GUI.clearDialog();
                world_select_menu.refresh();
            }

            public void saveWorldSettings() {
                if (name_field.getText().length() <= 0) return;
                File f = new File(Assets.ROOT_DIR + "/saves/"+name_field.getText());
                if (f.exists()) return;
                f.mkdir();
                f = new File(Assets.ROOT_DIR + "/saves/"+name_field.getText()+"/generator_settings.txt");
                FileWriter fw;
                System.out.println("Saving to file " + f.getAbsoluteFile().getAbsolutePath());
                try {
                    if (!f.exists()) f.createNewFile();
                    fw = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write("size=");
                    bw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        create_btn.setHeight(24);
        create_btn.anchor(null, GUIElement.ANCHOR_MID_X, GUIElement.ANCHOR_MID_X, 0);
        create_btn.anchor(null, GUIElement.ANCHOR_BOTTOM, GUIElement.ANCHOR_BOTTOM, -10);
        p.addComponent(create_btn);

        return p;
    }

}
