package com.bitbucket.computerology.misc;

import java.io.File;
import java.util.Calendar;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.imageout.ImageOut;

import org.newdawn.slick.AppGameContainer;

public class Window {
    
    public static int MIN_WIDTH = 720, MIN_HEIGHT = 515;
    //the name of the window
    public static final String WINDOW_TITLE = "Top Secret Project";
    //create a window object
    public static AppGameContainer WINDOW_INSTANCE;

    public static void toggleFullScreen() throws SlickException {
        if (WINDOW_INSTANCE.isFullscreen() == false) {
            WINDOW_INSTANCE
                .setDisplayMode(Display.getDesktopDisplayMode().getWidth(),
                Display.getDesktopDisplayMode().getHeight(), true);
        } else {
            WINDOW_INSTANCE.setDisplayMode(Window.MIN_WIDTH, Window.MIN_HEIGHT, false);
        }
    }
    
    public static boolean isFullScreen() {
        return WINDOW_INSTANCE.isFullscreen();
    }

    public static void takeScreenshot(Graphics g) {
        try {
            int month = Calendar.getInstance().get(Calendar.MONTH);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            Image scrn = new Image(Window.getWidth(), Window.getHeight());
            String file_url = System.getProperty("user.home")+"/sheepfarmsimulator//screenshots/"
                    +year+""+month+""+day;
            g.copyArea(scrn, 0, 0);
            //make screenshots folder
            if (new File(System.getProperty("user.home")+"/sheepfarmsimulator/screenshots/").exists() == false) {
                new File(System.getProperty("user.home")+"/sheepfarmsimulator/screenshots/").mkdir();
            }
            //check if image_exists already
            if (new File(file_url+".png").exists()) {
                int count = 2;
                while (true) {
                    if (new File(file_url+"_"+count+".png").exists() == false) {
                        file_url+="_"+count;
                        break;
                    }
                    count++;
                }
            }
            ImageOut.write(scrn, file_url+".png");
            System.out.println("Saved screenshot to "+file_url+".png");
        } catch (SlickException ex) {

        }
    }

    /**
     * Prevent the window from being smaller than an arbitrary dimension.
     * @return true if the window had to be resized, false otherwise
     * @throws SlickException 
     */
    public static boolean enforceMinimumDimensions() throws SlickException {
        if (Display.wasResized()) {
            if (Window.getWidth() < MIN_WIDTH) {
                WINDOW_INSTANCE.setDisplayMode(720, Window.getHeight(), false);
                //SlickInitializer.WINDOW_INSTANCE.setResizable(true);
                return true;
            }
            if (Window.getHeight() < MIN_HEIGHT) {
                WINDOW_INSTANCE.setDisplayMode(Window.getWidth(), 515, false);
                //SlickInitializer.WINDOW_INSTANCE.setResizable(true);
                return true;
            }
        }
        return false;
    }

    public static int getFPS() {
        return WINDOW_INSTANCE.getFPS();
    }

    public static int getScreenWidth() {
        return Display.getDesktopDisplayMode().getWidth();
    }

    public static int getScreenHeight() {
        return Display.getDesktopDisplayMode().getHeight();
    }

    public static float getY() {
        return (float)Display.getY();
    }

    public static float getX() {
        return (float)Display.getX();
    }

    public static int getWidth() {
        return WINDOW_INSTANCE.getWidth();
    }

    public static int getHeight() {
        return WINDOW_INSTANCE.getHeight();
    }

    public static void setResizable(boolean resizable) {
        Display.setResizable(resizable);
    }

    public static void setMouseGrabbed(boolean grabbed) {
        WINDOW_INSTANCE.setMouseGrabbed(grabbed);
    }

}
