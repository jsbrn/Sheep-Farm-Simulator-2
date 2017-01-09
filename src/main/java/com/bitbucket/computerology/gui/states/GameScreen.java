package com.bitbucket.computerology.gui.states;

import com.bitbucket.computerology.gui.GUI;
import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.elements.Button;
import com.bitbucket.computerology.gui.elements.GameCanvas;
import com.bitbucket.computerology.gui.elements.MiniMap;
import com.bitbucket.computerology.gui.elements.Panel;
import com.bitbucket.computerology.gui.elements.StatusBar;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.terrain.Sector;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.terrain.Chunk;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.state.*;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class GameScreen extends BasicGameState {
    
    boolean initialized = false;
    
    Input input;
    
    public static GUI GUI;
    public static MiniMap MINI_MAP;
    public static boolean DEBUG_MODE = false;
    
    static StateBasedGame game;
    
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
        
        final Panel menu = new Panel();
        menu.setWidth(150);
        menu.setX(-150);
        menu.setY(24);
        menu.setVisible(false);
        menu.setXOffsetMode(GUIElement.ORIGIN_RIGHT);
        menu.setHeight(200);
        
        Button quit = new Button(){
            public void onMouseClick(int button, int x, int y, int click_count) {
                World.save();
                World.destroy();
                game.enterState(Assets.MAIN_MENU);
            }
        };
        quit.setText("Save and quit");
        quit.setWidth(150);
        quit.setHeight(16);
        quit.setBackgroundColor(Color.red);
        quit.setTextColor(Color.white);
        quit.setX(0);quit.setY(-quit.getHeight()); quit.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        menu.addComponent(quit);
        
        
        Button menu_button = new Button() {
            @Override
            public void onMouseClick(int button, int x, int y, int click_count) {
                menu.setVisible(!menu.isVisible());
            }
        };
        menu_button.setHeight(24);
        menu_button.autoWidth(true);
        menu_button.setIcon("images/gui/menu.png");
        menu_button.setXOffsetMode(GUIElement.ORIGIN_RIGHT);
        menu_button.setX(-menu_button.getWidth());
        menu_button.showBackground(false);
        
        final Panel navigator_menu = new Panel();
        navigator_menu.setTitle("Menus");
        navigator_menu.setBackground("images/gui/menu_panel.png");
        navigator_menu.setX(0);
        navigator_menu.setY(-navigator_menu.getHeight());
        navigator_menu.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        
        Button finances = new Button();
        finances.setIcon("images/gui/money_32.png");
        finances.autoWidth(true);
        finances.setHeight(finances.getWidth());
        finances.setBackgroundColor(Color.green.darker());
        finances.setX(10);
        finances.setY(20);
        navigator_menu.addComponent(finances);
        
        final Panel map_menu = new Panel();
        map_menu.setWidth(200);
        map_menu.setHeight(200);
        map_menu.showBackground(true);
        map_menu.setTitle("Map (draggable)");
        map_menu.setDraggable(true);
        map_menu.setX(-map_menu.getWidth());
        map_menu.setY(-map_menu.getHeight());
        map_menu.setYOffsetMode(GUIElement.ORIGIN_BOTTOM);
        map_menu.setXOffsetMode(GUIElement.ORIGIN_RIGHT);
        
        MINI_MAP = new MiniMap();
        MINI_MAP.setWidth(180);
        MINI_MAP.setHeight(140);
        MINI_MAP.setX(-MINI_MAP.getWidth()/2);
        MINI_MAP.setY(10);
        MINI_MAP.setXOffsetMode(GUIElement.ORIGIN_MIDDLE);
        map_menu.addComponent(MINI_MAP);
        
        status.addComponent(menu_button);
        GUI.addComponent(menu);
        GUI.addComponent(navigator_menu);
        GUI.addComponent(map_menu);
        
        initialized = true;
    }


    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
        if (!Assets.loaded()) return;
        int x = Mouse.getX(), y = Display.getHeight() - Mouse.getY();
        GUI.draw(g);
        
        if (DEBUG_MODE) {
            g.setColor(Color.white);
            g.setFont(Assets.getFont(8));
            
            g.drawString("Entities: "+World.getWorld().activeEntityCount()+"A, "+World.getWorld().movableEntityCount()+"M", 5, 32);
            g.drawString("Sectors: "+World.getWorld().sectorCount(), 5, 42);
            g.drawString("Camera: "+Camera.getX()+", "+Camera.getY(), 5, 52);
            
            int wc[] = World.getWorld().getWorldCoords(x, y);
            int sc[] = World.getWorld().getSectorCoords(wc[0], wc[1]);
            int cc[] = World.getWorld().getChunkCoords(wc[0], wc[1]);
            
            Sector s = World.getWorld().getSector(sc[0], sc[1]);
            if (s == null) return;
            Chunk c = s.getChunk(cc[0], cc[1]);
            g.drawString("Mouse", 5, 72);
            g.drawString("  - Onscreen: "+x+", "+y, 5, 82);
            g.drawString("  - World: "+wc[0]+", "+wc[1], 5, 92);
            g.drawString("  - Sector: "+sc[0]+", "+sc[1], 5, 102);
            g.drawString("  - Chunk: "+cc[0]+", "+cc[1]+" (t: "+(c != null ? c.getTerrain() : "null")+")", 5, 112);
            
            Entity en = World.getWorld().getEntity(wc[0], wc[1]);
            g.drawString("Entity at mouse: "+(en != null ? en.toString() : "null"), 5, 132);
            
            g.setColor(Color.blue);
            g.drawRect(x-200, y-200, 200, 200);
            for (Chunk ch: World.getWorld().getChunks(wc[0]-200, wc[1]-200, 200, 200)) {
                g.setColor(new Color(0, 0, 0, 125));
                g.fillRect(ch.onScreenCoords()[0], ch.onScreenCoords()[1], Chunk.onScreenSize(), Chunk.onScreenSize());
            }
            
            if (c != null) {
                g.drawString("Entities in chunk: ", x+20, y);
                int i = 0;
                for (Entity e: c.entities) {
                    g.drawString("  - "+e.toString(), x+20, y+10+(i*10));
                    i++;
                }
                
                for (int k = -50; k < 50; k++) {
                    for (int j = -50; j < 50; j++) {
                        g.setColor(Color.black);
                        g.drawLine(c.onScreenCoords()[0]+(k*Chunk.onScreenSize()), 0, 
                                c.onScreenCoords()[0]+(k*Chunk.onScreenSize()), Display.getHeight());
                        g.drawLine(0, c.onScreenCoords()[1]+(j*Chunk.onScreenSize()), 
                                Display.getWidth(), c.onScreenCoords()[1]+(j*Chunk.onScreenSize()));
                    }
                }
                
                g.setColor(Color.red);
                g.drawRect(s.onScreenCoords()[0], s.onScreenCoords()[1], 
                        Sector.onScreenSize(), Sector.onScreenSize());
                
            } else {
                System.out.println("Chunk at "+cc[0]+", "+cc[1]+" is null!");
            }
            
            
        }
        
    }

    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        MiscMath.DELTA_TIME = delta;
        input = gc.getInput();
        World.getWorld().update();
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
