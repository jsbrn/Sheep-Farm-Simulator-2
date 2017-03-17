package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.*;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.Window;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;
import com.bitbucket.computerology.world.towns.Town;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.state.transition.Transition;

import java.util.ArrayList;

public class GameScreen extends BasicGameState {

    public static GUI GUI;
    public static boolean DEBUG_MODE = false, DRAW_MAP = false;
    static StateBasedGame game;
    boolean initialized = false;
    Input input;

    public static Panel pause_menu;

    public GameScreen(int state) {

    }

    @Override
    public int getID() {
        return Assets.GAME_SCREEN;
    }

    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
        if (initialized) return;
        game = sbg;
        GUI = new GUI();
        StatusBar status = new StatusBar();
        GUI.addComponent(new GameCanvas());
        GUI.addComponent(status);

        pause_menu = new Panel() {

            @Override
            public void onVisible() {
                grabFocus();
            }

        };
        pause_menu.setWidth(250);
        pause_menu.setHeight(450);
        pause_menu.setTitle("Paused");
        pause_menu.setVisible(false);
        GUI.addComponent(pause_menu);

        Button b = new Button("Resume", Color.green, Color.white) {

            @Override
            public void onMouseRelease(int button, int x, int y, boolean intersection) {
                if (intersection) GUI.clearDialog();
            }

        };
        b.setHeight(24);
        b.anchor(null, GUIElement.ANCHOR_LEFT, 0, 10);
        b.anchor(null, GUIElement.ANCHOR_RIGHT, 1, -10);
        b.anchor(null, GUIElement.ANCHOR_TOP, 0, 10 + pause_menu.getHeaderHeight());
        pause_menu.addComponent(b);

        Button save = new Button("Save and quit", Color.black, Color.white) {

            @Override
            public void onMouseRelease(int button, int x, int y, boolean intersection) {
                if (intersection) {
                    World.save();
                    GUI.clearDialog();
                    game.enterState(Assets.MAIN_MENU);
                }
            }

        };
        save.setHeight(24);
        save.anchor(null, GUIElement.ANCHOR_LEFT, 0, 10);
        save.anchor(null, GUIElement.ANCHOR_RIGHT, 1, -10);
        save.anchor(b, GUIElement.ANCHOR_TOP, 0, 10 + pause_menu.getHeaderHeight());
        pause_menu.addComponent(save);


        initialized = true;
    }

    public void drawMap(Graphics g) {
        if (World.getWorld().getMapTexture() != null)
            g.drawImage(World.getWorld().getMapTexture().getScaledCopy(Window.getHeight(), Window.getHeight()), 0, 0);
    }


    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
        if (!Assets.loaded()) return;
        if (World.getWorld().getMapTexture() == null) World.getWorld().buildMapTexture();
        int x = Mouse.getX(), y = Display.getHeight() - Mouse.getY();
        GUI.draw(g);

        if (DEBUG_MODE) {
            g.setColor(Color.white);
            g.setFont(Assets.getFont(8));

            g.drawString("Entities: " + World.getWorld().activeEntityCount() + "A, " + World.getWorld().movableEntityCount() + "M", 5, 32);
            g.drawString("Sectors: " + World.getWorld().sectorCount(), 5, 42);
            g.drawString("Camera: " + Camera.getX() + ", " + Camera.getY(), 5, 52);

            double wc[] = MiscMath.getWorldCoords(x, y);
            int sc[] = MiscMath.getSectorCoords(wc[0], wc[1]);
            int cc[] = MiscMath.getChunkCoords(wc[0], wc[1]);

            Sector s = World.getWorld().getSector(sc[0], sc[1]);
            if (s == null) return;
            Chunk c = s.getChunk(cc[0], cc[1]);
            g.drawString("Mouse", 5, 72);
            g.drawString("  - Onscreen: " + x + ", " + y, 5, 82);
            g.drawString("  - World: " + wc[0] + ", " + wc[1], 5, 92);
            g.drawString("  - Sector: " + sc[0] + ", " + sc[1] + (s.isTownSector() ? " (is town sector)" : ""), 5, 102);
            g.drawString("  - Chunk: " + cc[0] + ", " + cc[1] + " (b: " + (c != null ? c.getBiome() : "null") + ")", 5, 112);

            Entity en = World.getWorld().getEntity(wc[0], wc[1]);
            g.drawString("Entity at mouse: " + (en != null ? en.toString() : "null"), 5, 132);

            if (c != null) {
                g.drawString("Entities in chunk: ", x + 20, y);
                int i = 0;
                for (Entity e : c.entities) {
                    g.drawString("  - " + e.toString(), x + 20, y + 10 + (i * 10));
                    i++;
                }

                for (int k = -50; k < 50; k++) {
                    for (int j = -50; j < 50; j++) {
                        g.setColor(Color.black);
                        g.drawLine(c.onScreenCoords()[0] + (k * Chunk.onScreenSize()), 0,
                                c.onScreenCoords()[0] + (k * Chunk.onScreenSize()), Display.getHeight());
                        g.drawLine(0, c.onScreenCoords()[1] + (j * Chunk.onScreenSize()),
                                Display.getWidth(), c.onScreenCoords()[1] + (j * Chunk.onScreenSize()));
                    }
                }

                g.setColor(Color.red);
                g.drawRect(s.onScreenCoords()[0], s.onScreenCoords()[1],
                        Sector.onScreenSize(), Sector.onScreenSize());

            }

            g.setColor(Color.blue);
            g.drawRect(x - 200, y - 200, 200, 200);
            ArrayList<Entity> moused_entities = World.getWorld().getEntities(wc[0] - 200, wc[1] - 200, 200, 200);
            for (Entity e : moused_entities) {
                g.setColor(Color.green);
                int[] osc = MiscMath.getOnscreenCoords(e.getWorldX(), e.getWorldY());
                int[] dims = {osc[0] - e.getWidth() / 2, osc[1] - e.getHeight() / 2, e.getWidth(), e.getHeight()};
                g.drawRect(dims[0], dims[1], dims[2], dims[3]);
            }

        }

        if (DRAW_MAP) drawMap(g);

    }

    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {

        MiscMath.DELTA_TIME = delta;
        input = gc.getInput();
        World.getWorld().update();
        GUI.update();
        Camera.update();

        pollMouseScroll();

    }

    public void pollMouseScroll() {
        int dir = Mouse.getDWheel();
        dir = dir > 0 ? 1 : (dir < 0 ? -1 : 0); //clamp
        GUI.applyMouseScroll(input.getMouseX(), input.getMouseY(), dir);
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
