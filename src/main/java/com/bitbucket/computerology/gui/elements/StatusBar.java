package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.world.World;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusBar extends GUIElement {

    Image overhang, bar, sundial, frame;

    Label money, sheep;

    public StatusBar() {
        try {
            overhang = new Image("images/gui/overhang.png", false, Image.FILTER_LINEAR);
            bar = new Image("images/gui/statusbar.png", false, Image.FILTER_NEAREST);
            sundial = new Image("images/gui/sundial.png", false, Image.FILTER_LINEAR);
            frame = new Image("images/gui/sundial_frame.png", false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(StatusBar.class.getName()).log(Level.SEVERE, null, ex);
        }
        money = new Label() {
            public void update() {
                super.update();
                setText("$" + World.getWorld().getPlayer().getMoney());
            }
        };
        money.setX(5);
        money.setText("$0");
        money.setIcon("images/gui/money_16.png");
        money.setHeight(24);
        sheep = new Label();
        sheep.setX(50);
        sheep.setText("0 sheep");
        sheep.setIcon("images/gui/sheep_icon.png");
        sheep.setHeight(24);
        this.addComponent(money);
        this.addComponent(sheep);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public int getWidth() {
        return Display.getWidth();
    }

    @Override
    public int getHeight() {
        return (bar != null) ? bar.getHeight() : 0;
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(overhang, Display.getWidth() / 2 - overhang.getWidth() / 2, 0);
        sundial.setRotation(360F * (float) (((double) World.getWorld().getTime() % 1400D) / 1400D));
        g.drawImage(sundial, Display.getWidth() / 2 - sundial.getWidth() / 2, 10 + frame.getHeight() / 2 - sundial.getHeight() / 2);
        g.drawImage(frame, Display.getWidth() / 2 - frame.getWidth() / 2, 10);
        g.drawImage(bar.getScaledCopy(Display.getWidth() / 2 - overhang.getWidth() / 2, bar.getHeight()), 0, 0);
        g.drawImage(bar.getScaledCopy(Display.getWidth() / 2, bar.getHeight()), Display.getWidth() / 2 + overhang.getWidth() / 2, 0);
        super.draw(g);
    }

}
