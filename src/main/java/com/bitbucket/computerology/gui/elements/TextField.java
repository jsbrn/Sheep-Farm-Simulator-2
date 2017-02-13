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
        if (Assets.getFont(12).getWidth(text) + 6 < getOnscreenDimensions()[2] - 10) text += t;
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
        int[] dims = getOnscreenDimensions();
        if (!MiscMath.pointIntersectsRect(x, y, dims[0], dims[1], dims[2], dims[3]))
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
    public void drawToCanvas() {
        Graphics g = getCanvas();
        int[] dims = getOnscreenDimensions();
        g.setColor(Color.black);
        g.drawRect(dims[0] - 2, dims[1] - 2, dims[2] + 3, dims[3] + 3);
        g.setColor(new Color(45, 50, 75).brighter());
        g.drawRect(dims[0] - 1, dims[1] - 1, dims[2] + 1, dims[3] + 1);
        g.setColor(Color.white);
        g.fillRect(dims[0], dims[1], dims[2], dims[3]);
        g.setColor(Color.black);
        if (blink > blink_speed && hasFocus())
            g.fillRect(dims[0] + 5 + Assets.getFont(12).getWidth(text), dims[1] + 3, 2, dims[3] - 6);
        g.setFont(Assets.getFont(12));
        if (text.length() > 0) {
            g.setColor(text_color);
            g.drawString(text, dims[0] + 5, dims[1] + dims[3] / 2 - Assets.getFont(12).getHeight(text) / 2);
        } else if (!hasFocus()) {
            g.setColor(alt_text_color);
            g.drawString(alt_text, dims[0] + 5, dims[1] + dims[3] / 2 - Assets.getFont(12).getHeight(alt_text) / 2);
        }
    }

}
