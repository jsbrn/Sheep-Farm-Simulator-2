package com.bitbucket.computerology.misc;

import com.bitbucket.computerology.world.entities.BlockList;
import com.bitbucket.computerology.world.entities.EntityList;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;

public class Assets {
    
    //used to keep track of the asset loading
    static int pass = 0;
    public static int ASSET_COUNT = 44; //this number needs to be accurate
    private static boolean loaded;
    public static String STATUS = ""; //a description of what is being loaded currently
    
    //assets
    public static Image CHUNK_TERRAIN, MAIN_MENU_BACKGROUND;
    static TrueTypeFont[] FONTS;
    static TrueTypeFont placeholder_font;
    
    //version info, menu IDs, and the root directory
    public static int UPDATE_ID = 0;
    public static String VERSION_NAME = "0.0.1-dev", ROOT_DIR, SAVE_DIR;
    public static final int MAIN_MENU = 1, LOADING_SCREEN = 2, GAME_SCREEN = 0;
    
    public static boolean loaded() {
        return loaded;
    }
    
    public static boolean loadAssets() {
        pass++; loaded = false;
        
        if (pass == 1) { STATUS = "background"; loadMenuBackground(); return false; }
        if (pass == 2) { STATUS = "logic blocks"; BlockList.loadBlockList(); return false; }
        if (pass == 3) { STATUS = "entities"; EntityList.loadEntityList(); return false; }
        if (pass == 4) { STATUS = "terrain spritesheet"; loadTerrainSprite(); return false; }
        if (pass == 5) { FONTS = new TrueTypeFont[40]; return false; }
        if (pass >= 6 && pass < 6+FONTS.length) {
            STATUS = "fonts";
            Font awtFont = new Font("Arial", Font.PLAIN, pass-6+8);
            TrueTypeFont f = new TrueTypeFont(awtFont, true);
            FONTS[pass-6] = f;
            return false;
        }
        
        loaded = true;
        return true;
    }
    
    private static void loadMenuBackground() {
        try {
            Assets.MAIN_MENU_BACKGROUND = new Image("images/gui/background.png", false, Image.FILTER_NEAREST)
                    .getScaledCopy(Window.getScreenWidth(), Window.getScreenHeight());
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadTerrainSprite() {
        try {
            Assets.CHUNK_TERRAIN = new Image("images/gui/terrain.png", false, Image.FILTER_NEAREST).getScaledCopy(2);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gets a font from the list of pre-loaded fonts.
     * @param size The font size, anything from 8-48.
     * @return A TrueTypeFont of the size specified, or just the placeholder font
     * if nothing found.
     */
    public static TrueTypeFont getFont(int size) {
        int index = size-8;
        if (FONTS != null && !(index >= FONTS.length || index < -1)) { 
            if (FONTS[index] != null) return FONTS[index];
        }
        if (placeholder_font == null) {
            Font awtFont = new Font("Arial", Font.PLAIN, 16);
            placeholder_font = new TrueTypeFont(awtFont, true);
        }
        return placeholder_font;
    }
    
    public static void createRootDirectory() {
        Assets.ROOT_DIR = System.getProperty("user.home")+"/secret";
        File root = new File(Assets.ROOT_DIR);
        File save_folder = new File(Assets.ROOT_DIR+"/saves/");
        if (root.exists() == false) {
            root.mkdir();
        }
        if (save_folder.exists() == false) {
            save_folder.mkdir();
        }
    }

    public static void setLocalVersion() {
        //register the local version so the launcher can compare to the most recent update
        Properties prop = new Properties();
        try {
            prop.setProperty("updateID", ""+Assets.UPDATE_ID); //make sure this matches the server's updateID
            prop.setProperty("name", ""+Assets.VERSION_NAME);
            prop.store(
                    new FileOutputStream(ROOT_DIR+"/version.txt"), null);

        } catch (IOException ex) {}
    }
    
}
