package com.bitbucket.computerology.misc;

import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.entities.*;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.components.Hitbox;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Assets {

    public static final int MAIN_MENU = 1, LOADING_SCREEN = 2, GAME_SCREEN = 0;
    public static final int ASSET_COUNT = 16; //this number needs to be accurate
    public static String STATUS = ""; //a description of what is being loaded currently
    public static Image MAIN_MENU_BACKGROUND;
    //version info, menu IDs, and the root directory
    public static final int UPDATE_ID = 0;
    public static String VERSION_NAME = "0.0.1-dev", ROOT_DIR;
    //used to keep track of the asset loading
    private static int pass = 0;
    //assets
    private static Image[] TERRAIN = new Image[3]; //3 zoom levels
    private static Image[] TERRAIN_CORNERS = new Image[3];
    private static TrueTypeFont[] FONTS;
    private static TrueTypeFont placeholder_font;
    public static Image WHITE_GRADIENT, BLACK_GRADIENT;
    private static boolean loaded;

    private static ArrayList<Block> block_list;

    private static void loadBlockList() {
        System.out.println("Loading block_list...");
        block_list = new ArrayList<Block>();

        Block b = new Block("move", Block.CONDITIONAL_BLOCK, Block.NO_OUTPUT,
                new String[][]{{"", "x", "number"}, {"", "y", "number"}});
        block_list.add(b);

        System.out.println("Successfully loaded " + block_list.size() + " flowchart blocks!");

    }

    public static Block getBlock(String type) {
        for (Block b : block_list) if (b.getTitle().equals(type)) return b;
        return null;
    }

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
            loadBlockList();
            return false;
        }
        if (pass == 3) {
            STATUS = "entities";
            loadEntityList();
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

    private static ArrayList<Entity> entities;
    private static ArrayList<Blueprint> blueprints;

    @Nullable
    public static Entity getEntity(String type) {
        for (Entity e : entities) if (e.getType().equals(type)) return e;
        return null;
    }

    @Nullable
    public static Blueprint getBlueprint(String name) {
        for (Blueprint b: blueprints) if (b.getName().equals(name)) return b;
        return null;
    }

    private static void loadEntityList() {
        System.out.println("Loading entities and blueprints...");
        entities = new ArrayList<Entity>();
        blueprints = new ArrayList<Blueprint>();
        try {
            InputStream in = Assets.class.getResourceAsStream("/misc/entity_list.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("e")) {
                    Entity e = new Entity();
                    if (loadEntity(e, br)) entities.add(e);
                }
                if (line.equals("b")) {
                    Blueprint b = new Blueprint();
                    if (loadBlueprint(b, br)) blueprints.add(b);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Assets.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Assets.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Successfully loaded " + entities.size() + " entities!");
    }

    private static boolean loadBlueprint(Blueprint b, BufferedReader br) {
        System.out.println("Loading animation...");
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/b")) return true;
                if (line.indexOf("id=") == 0) { b.setName(line.substring(3, line.length())); continue; }
                b.add(line.trim());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static boolean loadEntity(Entity e, BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/e")) return true;
                if (line.indexOf("id=") == 0) e.setType(line.trim().replace("id=", ""));
                if (line.equals("c")) {
                    Component c = loadComponent(br);
                    if (c != null) {
                        e.addComponent(c);
                        c.init(); //load custom parametres, etc.
                    }
                }
                if (line.equals("s")) {
                    ComponentSystem c = loadSystem(br);
                    if (c != null) {
                        e.addSystem(c);
                    }
                }
                /*if (line.equals("a")) {
                    Animation c = new Animation();
                    if (c.load(br)) animations.add(c);
                }*/
                if (line.equals("f")) {
                    Flow f = new Flow();
                    if (loadFlow(f, br)) e.addFlow(f);
                }
                //load the hitbox stuff (as designed from the editor)
                if (line.equals("h")) {
                    com.bitbucket.computerology.world.entities.Component c = e.getComponent("Hitbox");
                    Hitbox h = c != null ? ((Hitbox) c) : null;
                    int h_index = 0;
                    while (h != null) {
                        line = br.readLine();
                        if (line == null) break;
                        if (line.equals("/h")) break;
                        if (line.equals("---")) { h_index++; continue; }
                        ArrayList<String> l = MiscString.parseString(line.replace(" ", "\n"));
                        h.addLine(Integer.parseInt(l.get(0))
                                , Integer.parseInt(l.get(1))
                                , Integer.parseInt(l.get(2))
                                , Integer.parseInt(l.get(3)), h_index);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Assets.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Assets.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static Component loadComponent(BufferedReader br) {
        Component c = null;
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/c")) return c;
                if (line.indexOf("id=") == 0) {
                    c = Component.create(line.trim().replace("id=", ""));
                } else {
                    if (c != null) c.addParameter(line);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Assets.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Assets.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static ComponentSystem loadSystem(BufferedReader br) {
        ComponentSystem c = null;
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/s")) return c;
                if (line.indexOf("id=") == 0) {
                    c = ComponentSystem.create(line.trim().replace("id=", ""));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static boolean loadFlow(Flow f, BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/f")) return true;
                if (line.indexOf("id=") == 0) f.setName(line.trim().replace("id=", ""));
                if (line.equals("b")) {
                    Block b = new Block();
                    if (loadBlock(b, br)) f.addBlock(b);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static boolean loadBlock(Block b, BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/b")) return true;
                if (line.indexOf("id=") == 0) b.setID(Integer.parseInt(line.trim().replace("id=", "")));
                if (line.indexOf("t=") == 0) {
                    Block copy = getBlock(line.substring(2, line.length()));
                    if (copy != null) {
                        copy.copyTo(b);
                    } else {
                        System.err.println("Block " + b.getTitle() + " does not exist from BLOCK_LIST!");
                        return false;
                    }
                }
                if (line.indexOf("conns=") == 0) {
                    String[] bs = line.substring(7, line.length()).split("\\s");
                    int[] conns = new int[bs.length];
                    for (int i = 0; i < conns.length; i++) conns[i] = Integer.parseInt(bs[i]);
                    b.setConnections(conns);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
