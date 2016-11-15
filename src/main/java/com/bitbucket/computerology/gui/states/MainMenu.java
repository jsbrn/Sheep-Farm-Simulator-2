package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.*;

import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.Window;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import java.io.File;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.state.*;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class MainMenu extends BasicGameState {

    static double bg_x = 0, bg_dx = -50;
    
    StateBasedGame game;
    boolean initialized = false;
    public static GUI GUI;
    
    public MainMenu(int state) {}

    @Override
    public int getID() {
        return Assets.MAIN_MENU;
    }
    
    Panel save_file_menu;

    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
        if (initialized) return;
        game = sbg;
        GUI = new GUI();
        
        final Panel edit_save_panel = new Panel();
        edit_save_panel.setWidth(280);
        edit_save_panel.setHeight(10+32+10+24+10+50);
        edit_save_panel.setX(-edit_save_panel.getWidth()/2);
        edit_save_panel.setY(-edit_save_panel.getHeight()/2);
        edit_save_panel.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        edit_save_panel.setYOffsetMode(GUIElement.ORIGIN_MIDDLE);
        edit_save_panel.setVisible(false);
        edit_save_panel.setTitle("Rename");
        
        final TextField rename_field = new TextField() {
            @Override
            public void addText(String s) {
                super.addText(s);
                if (getText().length() == 0) {
                    
                }
            }
        };
        rename_field.setWidth(260);
        rename_field.setHeight(32);
        rename_field.setX(10);
        rename_field.setY(60);
        
        Button accept_rename = new Button() {
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                File folder = new File(Assets.ROOT_DIR+"/saves/"+rename_field.getAltText());
                boolean renamed = false;
                if (folder.exists()) {
                    renamed = folder.renameTo(new File(Assets.ROOT_DIR+"/saves/"+rename_field.getText()));
                }
                if (renamed) { 
                    edit_save_panel.setVisible(false);
                    save_file_menu.refresh();
                    GUI.undialog(); 
                    rename_field.setText("");
                }
            }
        };
        accept_rename.setHeight(24);
        accept_rename.setWidth(64);
        accept_rename.setText("Rename");
        accept_rename.setBackgroundColor(Color.black);
        accept_rename.setTextColor(Color.white);
        accept_rename.setX(-accept_rename.getWidth()-10);
        accept_rename.setY(-accept_rename.getHeight()-10);
        accept_rename.setXOffsetMode(GUIElement.ORIGIN_RIGHT);
        accept_rename.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        
        Label rename_label = new Label();
        rename_label.setX(10);
        rename_label.setY(30);
        rename_label.setText("Enter a new name:");
        
        Button cancel_rename = new Button() {
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                edit_save_panel.setVisible(false);
                GUI.undialog();
                rename_field.setText("");
            }
        };
        cancel_rename.setHeight(24);
        cancel_rename.setWidth(64);
        cancel_rename.setText("Cancel");
        cancel_rename.setX(10);
        cancel_rename.setBackgroundColor(Color.black);
        cancel_rename.setTextColor(Color.white);
        cancel_rename.setY(-cancel_rename.getHeight()-10);
        cancel_rename.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        
        edit_save_panel.addComponent(cancel_rename);
        edit_save_panel.addComponent(accept_rename);
        edit_save_panel.addComponent(rename_field);
        edit_save_panel.addComponent(rename_label);
        
        save_file_menu = new Panel(){        
            @Override
            public void refresh() {
                super.refresh();
                GUIElement quit_button = getComponents().get(0);
                GUIElement new_button = getComponents().get(1);
                getComponents().clear();
                addComponent(quit_button);
                addComponent(new_button);
                File saves = new File(Assets.ROOT_DIR+"/saves/");
                new_button.setEnabled(saves.listFiles().length < 9);
                for (int i = 0; i != saves.listFiles().length; i++) {
                    final File f = saves.listFiles()[i];
                    if (!f.isDirectory()) continue;
                    Button b = new Button() { 
                        
                        @Override
                        public void onMouseClick(int button, int x, int y, int click_count) {
                            Assets.SAVE_DIR = Assets.ROOT_DIR+"/saves/"+getText();
                            File f = new File(Assets.SAVE_DIR+"/world.txt");
                            Camera.reset();
                            if (f.exists()) {
                                World.newWorld();
                                World.load();
                            } else {
                                World.newWorld();
                                World.getWorld().generate();
                            }
                            game.enterState(Assets.GAME_SCREEN);
                        }
                    
                    };
                    b.setText(f.getName());
                    b.setWidth(save_file_menu.getWidth()-88);
                    b.setTextColor(Color.white);
                    b.setBackgroundColor(Color.black);
                    b.setX(10);
                    b.setHeight(24);
                    b.setY(getHeaderHeight()+10+(i*(b.getHeight()+5)));
                    
                    Button r = new Button() {
                        public void onMouseClick(int button, int x, int y, int click_count) {
                            rename_field.setAltText(f.getName());
                            edit_save_panel.setVisible(true);
                            GUI.dialog(edit_save_panel);
                        }
                    };
                    r.setWidth(24);
                    r.setIcon("images/gui/rename.png");
                    r.setTextColor(Color.white);
                    r.setBackgroundColor(Color.black);
                    r.setX(2*(-10-r.getWidth()));
                    r.setXOffsetMode(GUIElement.ORIGIN_RIGHT);
                    r.setHeight(24);
                    r.setY(getHeaderHeight()+10+(i*(r.getHeight()+5)));
                    
                    Button d = new Button() {
                        
                        public void deleteDir(File f) {
                            if (!f.isDirectory()) return;
                            for (File f2: f.listFiles()) {
                                if (f2.isDirectory()) deleteDir(f2); else f2.delete();
                            }
                            f.delete();
                        }
                        
                        public void onMouseClick(int button, int x, int y, int click_count) {
                            deleteDir(f);
                            save_file_menu.refresh();
                        }
                    };
                    d.setWidth(24);
                    d.setIcon("images/gui/delete_save.png");
                    d.setTextColor(Color.white);
                    d.setBackgroundColor(Color.red);
                    d.setX(-10-d.getWidth());
                    d.setXOffsetMode(GUIElement.ORIGIN_RIGHT);
                    d.setHeight(24);
                    d.setY(getHeaderHeight()+10+(i*(d.getHeight()+5)));
                    
                    addComponent(b);
                    addComponent(d);
                    addComponent(r);
                }
            }
        };
        //SAVE FILE MENU
        save_file_menu.setWidth(300);
        save_file_menu.setTitle("Saved Games");
        save_file_menu.setHeight(350);
        save_file_menu.setX(-save_file_menu.getWidth()/2);
        save_file_menu.setY(-save_file_menu.getHeight()/2);
        save_file_menu.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        save_file_menu.setYOffsetMode(GUIElement.ORIGIN_MIDDLE);
        save_file_menu.setVisible(false);
        
        final Panel title_menu = new Panel();
        title_menu.setWidth(300);
        title_menu.setHeight(10+10+(24*4));
        title_menu.setX(-title_menu.getWidth()/2);
        title_menu.setY(-title_menu.getHeight()/2);
        title_menu.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        title_menu.setYOffsetMode(GUIElement.ORIGIN_MIDDLE);
        
        Button play = new Button(){
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                title_menu.setVisible(false);
                save_file_menu.setVisible(true);
                save_file_menu.refresh();
            }
        };
        play.setText("Play game");
        play.setBackgroundColor(Color.green.darker());
        play.setTextColor(Color.white);
        play.setWidth(title_menu.getWidth()-20);
        play.setX(-play.getWidth()/2);
        play.setY(10);
        play.setHeight(24);
        play.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        title_menu.addComponent(play);
        
        Button options = new Button(){
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                
            }
        };
        options.setText("Options");
        options.setBackgroundColor(Color.black);
        options.setTextColor(Color.white);
        options.setWidth(title_menu.getWidth()-20);
        options.setX(-options.getWidth()/2);
        options.setY(10 + play.getHeight() + 5);
        options.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        options.setHeight(24);
        title_menu.addComponent(options);
        
        Button quit = new Button(){
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                System.exit(0);
            }
        };
        quit.setText("Quit");
        quit.setBackgroundColor(Color.black);
        quit.setTextColor(Color.white);
        quit.setWidth(title_menu.getWidth()-20);
        quit.setX(-options.getWidth()/2);
        quit.setHeight(24);
        quit.setY(-quit.getHeight()-10);
        quit.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        quit.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        title_menu.addComponent(quit);
        
        Label l = new Label();
        l.setText(Assets.VERSION_NAME);
        l.setFontSize(12);
        l.setX(5);
        l.setY(5);
        GUI.addComponent(l);

        Button back = new Button(){
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                title_menu.setVisible(true);
                save_file_menu.setVisible(false);
            }
        };
        back.setText("Back");
        back.setBackgroundColor(Color.black);
        back.setTextColor(Color.white);
        back.setWidth((title_menu.getWidth()/3)-20);
        back.setHeight(24);
        back.setX(10);
        back.setY(-back.getHeight()-10);
        back.setXOffsetMode(GUIElement.ORIGIN_LEFT);
        back.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        save_file_menu.addComponent(back);
        
        Button new_save = new Button() {
            
            public int saveNumber(int i) {
                File folder = new File(Assets.ROOT_DIR+"/saves/saved_game_"+i+"/");
                if (folder.exists()) return saveNumber(i+1);
                return i;
            }
            
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                File f = new File(Assets.ROOT_DIR+"/saves/saved_game_"+saveNumber(1));
                if (!f.exists()) f.mkdir();
                save_file_menu.refresh();
            }
        };
        new_save.setText("New game");
        new_save.setBackgroundColor(Color.black);
        new_save.setTextColor(Color.white);
        new_save.setWidth((title_menu.getWidth()/3)-20);
        new_save.setHeight(24);
        new_save.setX(-10-new_save.getWidth());
        new_save.setY(-new_save.getHeight()-10);
        new_save.setXOffsetMode(GUIElement.ORIGIN_RIGHT);
        new_save.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        save_file_menu.addComponent(new_save);
        
        GUI.addComponent(title_menu);
        GUI.addComponent(save_file_menu);
        GUI.addComponent(edit_save_panel);
        
        initialized = true;
    }


    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
        if (!Assets.loaded()) return;
        int x = Mouse.getX(), y = Display.getHeight() - Mouse.getY();
        g.drawImage(Assets.MAIN_MENU_BACKGROUND, (int)bg_x, 0);
        GUI.draw(g);
        
    }

    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        MiscMath.DELTA_TIME = delta;
        
        //pan the background back and forth
        if (bg_x + Assets.MAIN_MENU_BACKGROUND.getWidth() < Window.getWidth()) {
            bg_dx = -bg_dx;
            bg_x = Window.getWidth() - Assets.MAIN_MENU_BACKGROUND.getWidth();
        }
        if (bg_x > 0) {
            bg_x = 0;
            bg_dx = -bg_dx;
        }
        bg_x+=MiscMath.getConstant(bg_dx, 1);
        //update the GUI elements
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
