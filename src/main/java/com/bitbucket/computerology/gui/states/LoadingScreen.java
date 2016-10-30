package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.Button;
import com.bitbucket.computerology.gui.elements.GameCanvas;
import com.bitbucket.computerology.gui.elements.Label;
import com.bitbucket.computerology.gui.elements.Panel;
import com.bitbucket.computerology.gui.elements.ProgressBar;
import com.bitbucket.computerology.gui.elements.StatusBar;
import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.state.*;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class LoadingScreen extends BasicGameState {

    public static int DELTA_TIME = 1;
    
    boolean initialized = false;
    
    public static GUI GUI;
    static ProgressBar bar;
    static Label label;
    
    public LoadingScreen(int state) {}

    @Override
    public int getID() {
        return Assets.LOADING_SCREEN;
    }

    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
        if (initialized) return;
        GUI = new GUI();
        
        bar = new ProgressBar();
        bar.setWidth(300);
        bar.setHeight(16);
        bar.setX(-bar.getWidth()/2);
        bar.setY(-bar.getHeight()/2);
        bar.setMax(Assets.ASSET_COUNT);
        bar.setProgress(0);
        bar.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        bar.setYOffsetMode(GUIElement.ORIGIN_MIDDLE);
        
        label = new Label();
        label.setText("");
        label.setY(-40);
        label.setFontSize(16);
        label.setYOffsetMode(GUIElement.ORIGIN_MIDDLE);
        label.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        GUI.addComponent(label);
        GUI.addComponent(bar);
        
        initialized = true;
    }


    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {

        int x = Mouse.getX(), y = Display.getHeight() - Mouse.getY();
        
        GUI.draw(g);
        
        if (Assets.loadAssets()) {
            sbg.enterState(Assets.MAIN_MENU);
        } else {
            label.setText("Loading "+Assets.STATUS+"...");
            label.setX(-label.getWidth()/2);
            bar.addProgress(1);
        }
        
    }

    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        DELTA_TIME = delta;
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
        /*//if key is number key, select hotbar
        int hotkey_clicked = key-2;
        if (hotkey_clicked >= 0 && hotkey_clicked <= 9) {
            last_selected_hotbar_slot = -1;
            if (input.isKeyDown(Input.KEY_LSHIFT)) {
                if (hotkey_clicked < 9) {
                    //player_inventory.selectHotbarSlot(-1);
                    //player_inventory.select(ItemList.getID(
                    //        TOOL_BUTTONS.get(hotkey_clicked).getTitle()), "Item", null);
                }
            } else {
                //player_inventory.selectHotbarSlot(hotkey_clicked);
            }
        }*/

    }
}
