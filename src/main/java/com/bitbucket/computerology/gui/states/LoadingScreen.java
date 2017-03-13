package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.Label;
import com.bitbucket.computerology.gui.elements.ProgressBar;
import com.bitbucket.computerology.misc.Assets;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class LoadingScreen extends BasicGameState {

    public static int DELTA_TIME = 1;
    public static GUI GUI;
    static ProgressBar bar;
    boolean initialized = false;

    public LoadingScreen(int state) {

    }

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
        bar.setMax(Assets.ASSET_COUNT);
        bar.setProgress(0);
        bar.anchor(null, GUIElement.ANCHOR_MID_X, 0.5, 0);
        bar.anchor(null, GUIElement.ANCHOR_MID_Y, 0.5, 0);
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
