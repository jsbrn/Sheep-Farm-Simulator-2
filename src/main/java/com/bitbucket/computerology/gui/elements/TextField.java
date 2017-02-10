package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GUIElement;
import com.bitbucket.computerology.misc.Assets;
import com.bitbucket.computerology.misc.MiscMath;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class TextField extends GUIElement {

    String text, alt_text;
    Color text_color, alt_text_color;

    double blink_speed = 0.5;
    double blink = 2;

    public TextField() {
        this.text = "";
        this.alt_text = "";
        this.text_color = Color.black;
        this.alt_text_color = Color.lightGray;
    }

    public void setBlinkSpeed(double seconds) {
        blink_speed = seconds;
        blink = seconds * 2;
    }

    @Override
    public void update() {
        blink -= MiscMath.getConstant(1, 1);
        if (blink <= 0) blink = blink_speed * 2;
    }

    public void addText(String t) {
        if (t == null) t = "";
        if (Assets.getFont(12).getWidth(text) + 6 < getWidth() - 10) text += t;
    }

    @Override
    public void onMouseClick(int button, int x, int y, int click_count) {
        grabFocus();
        blink = blink_speed * 2;
    }

    public void backspace() {
        if (text.length() > 1)
            text = text.substring(0, text.length() - 1);
        else
            text = "";
    }

    public String getText() {
        return text;
    }

    public void setText(String s) {
        if (s == null) s = "";
        text = s;
    }

    public String getAltText() {
        return alt_text;
    }

    public void setAltText(String s) {
        alt_text = s;
    }

    @Override
    public void onMouseRelease(int button, int x, int y) {
        if (!MiscMath.pointIntersectsRect(x, y, getOnscreenX(), getOnscreenY(), getWidth(), getHeight()))
            releaseFocus();
    }

    @Override
    public void onKeyPress(char c) {
        if (!(c == '\b')) {
            //if within the range of proper key characters
            if (c >= 32 && c < 127) addText(c + "");
        } else {
            backspace();
        }
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.black);
        g.drawRect(getOnscreenX() - 2, getOnscreenY() - 2, getWidth() + 3, getHeight() + 3);
        g.setColor(new Color(45, 50, 75).brighter());
        g.drawRect(getOnscreenX() - 1, getOnscreenY() - 1, getWidth() + 1, getHeight() + 1);
        g.setColor(Color.white);
        g.fillRect(getOnscreenX(), getOnscreenY(), getWidth(), getHeight());
        g.setColor(Color.black);
        if (blink > blink_speed && hasFocus())
            g.fillRect(getOnscreenX() + 5 + Assets.getFont(12).getWidth(text), getOnscreenY() + 3, 2, getHeight() - 6);
        g.setFont(Assets.getFont(12));
        if (text.length() > 0) {
            g.setColor(text_color);
            g.drawString(text, getOnscreenX() + 5, getOnscreenY() + getHeight() / 2 - Assets.getFont(12).getHeight(text) / 2);
        } else if (!hasFocus()) {
            g.setColor(alt_text_color);
            g.drawString(alt_text, getOnscreenX() + 5, getOnscreenY() + getHeight() / 2 - Assets.getFont(12).getHeight(alt_text) / 2);
        }
    }

}
