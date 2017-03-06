package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.gui.states.GameScreen;
import com.bitbucket.computerology.misc.Assets;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Icon extends GUIElement {

    Image icon;
    int font_size = 12;

    public Icon() {
        setWidth(10);
        setHeight(10);
    }

    public Icon(String url) {
        this();
        setImage(url);
    }

    public void setImage(String url) {
        try {
            icon = new Image(url, false, Image.FILTER_LINEAR);
            setWidth(icon.getWidth());
            setHeight(icon.getHeight());
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(Graphics g) {

        int[] dims = getOnscreenDimensions();

        g.drawImage(icon, dims[0], dims[1]);

        super.draw(g);

        //if (GameScreen.DEBUG_MODE) {
            g.setColor(Color.red);
            g.drawRect(dims[0], dims[1], dims[2], dims[3]);
        //}

    }

}
