package com.bitbucket.computerology.misc;

import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.entities.BlockList;
import com.bitbucket.computerology.world.entities.EntityList;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Assets {

    public static final int MAIN_MENU = 1, LOADING_SCREEN = 2, GAME_SCREEN = 0;
    public static final int ASSET_COUNT = 16; //this number needs to be accurate
    public static String STATUS = ""; //a description of what is being loaded currently
    public static Image MAIN_MENU_BACKGROUND;
    //version info, menu IDs, and the root directory
    public static final int UPDATE_ID = 0;
    public static String VERSION_NAME = "0.0.1-dev", ROOT_DIR, CURR_SAVE_DIR;
    //used to keep track of the asset loading
    private static int pass = 0;
    //assets
    private static Image[] TERRAIN = new Image[3]; //3 zoom levels
    private static Image[] TERRAIN_CORNERS = new Image[3];
    private static TrueTypeFont[] FONTS;
    private static TrueTypeFont placeholder_font;
    public static Image WHITE_GRADIENT, BLACK_GRADIENT;
    private static boolean loaded;

    public static boolean loaded() {
        return loaded;
    }

    public static boolean loadAssets() {
        pass++;
        loaded = false;
        if (pass == 1) {
            STATUS = "background";
            loadMenuBackground();
            loadGradients();
            return false;
        }
        if (pass == 2) {
            STATUS = "logic blocks";
            BlockList.loadBlockList();
            return false;
        }
        if (pass == 3) {
            STATUS = "entities";
            EntityList.loadEntityList();
            return false;
        }
        if (pass == 4) {
            STATUS = "terrain spritesheet";
            loadTerrainSprite();
            return false;
        }
        if (pass == 5) {
            FONTS = new TrueTypeFont[11];
            return false;
        }
        if (pass >= 6 && pass < 6 + FONTS.length) {
            STATUS = "fonts";
            Font awtFont = new Font("Arial", Font.PLAIN, 8 + ((pass - 6) * 4));
            TrueTypeFont f = new TrueTypeFont(awtFont, true);
            FONTS[pass - 6] = f;
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

    private static void loadGradients() {
        try {
            Assets.BLACK_GRADIENT = new Image("images/gui/gradient.png",
                    false, Image.FILTER_NEAREST);
            Assets.WHITE_GRADIENT = new Image("images/gui/gradient.png",
                    false, Image.FILTER_NEAREST);
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    public static Image getTerrainSprite(boolean corners) {
        return corners ?
                Assets.TERRAIN_CORNERS[(int)Camera.getZoom() - 1]
                : Assets.TERRAIN[(int)Camera.getZoom() - 1];
    }

    private static void loadTerrainSprite() {
        try {
            for (int z = 0; z < 3; z++) {
                Assets.TERRAIN[z]
                        = new Image("images/terrain/terrain.png", false, Image.FILTER_NEAREST)
                        .getScaledCopy(z + 1);
                Assets.TERRAIN_CORNERS[z]
                        = new Image("images/terrain/terrain_corners.png", false, Image.FILTER_NEAREST)
                        .getScaledCopy(z + 1);
            }
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a font from the list of pre-loaded fonts.
     *
     * @param size The font size, anything from 8-48.
     * @return A TrueTypeFont of the size specified, or just the placeholder font
     * if nothing found.
     */
    public static TrueTypeFont getFont(int size) {
        size -= size % 4;
        int index = (size / 4) - 2;
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
        Assets.ROOT_DIR = System.getProperty("user.home") + "/secret";
        File root = new File(Assets.ROOT_DIR);
        File save_folder = new File(Assets.ROOT_DIR + "/saves/");
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
            prop.setProperty("updateID", "" + Assets.UPDATE_ID); //make sure this matches the server's updateID
            prop.setProperty("name", "" + Assets.VERSION_NAME);
            prop.store(
                    new FileOutputStream(ROOT_DIR + "/version.txt"), null);

        } catch (IOException ex) {
        }
    }

}
