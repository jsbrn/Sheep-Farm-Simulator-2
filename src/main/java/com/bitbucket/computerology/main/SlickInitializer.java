package com.bitbucket.computerology.main;

import com.bitbucket.computerology.gui.states.GameScreen;
import com.bitbucket.computerology.gui.states.LoadingScreen;
import com.bitbucket.computerology.gui.states.MainMenu;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.Window;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import java.io.IOException;

public class SlickInitializer extends StateBasedGame {

    public SlickInitializer(String gameTitle) {

        super(gameTitle); //set window title to "gameTitle" string

        //add states
        addState(new GameScreen(Assets.GAME_SCREEN));
        addState(new MainMenu(Assets.MAIN_MENU));
        addState(new LoadingScreen(Assets.LOADING_SCREEN));
    }

    public static void main(String args[]) throws IOException {

        Assets.createRootDirectory();
        Assets.setLocalVersion();

        //initialize the window
        try {
            Window.WINDOW_INSTANCE = new AppGameContainer(new SlickInitializer(Window.WINDOW_TITLE + " (" + Assets.VERSION_NAME + ")"));
            Window.WINDOW_INSTANCE.setDisplayMode(1080, 720, false);
            Window.WINDOW_INSTANCE.setFullscreen(false);
            Window.WINDOW_INSTANCE.setShowFPS(false);
            Window.WINDOW_INSTANCE.setVSync(true);
            Window.WINDOW_INSTANCE.setResizable(true);
            Window.WINDOW_INSTANCE.setAlwaysRender(true);
            Window.WINDOW_INSTANCE.start();
        } catch (SlickException e) {
        }
    }

    @Override
    public void initStatesList(GameContainer gc) throws SlickException {
        //initialize states
        getState(Assets.GAME_SCREEN).init(gc, this);
        getState(Assets.MAIN_MENU).init(gc, this);
        getState(Assets.LOADING_SCREEN).init(gc, this);

        //load "menu" state on startup
        this.enterState(Assets.LOADING_SCREEN);
    }

}
