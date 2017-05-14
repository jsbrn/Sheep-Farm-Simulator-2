package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Position extends Component {

    double x, y, last_x, last_y;
    float rotation;

    public Position() {
        last_x = 0;
        last_y = 0;
    }

    @Override
    public void customSave(BufferedWriter bw) {
        try {
            bw.write("x=" + (int) x + "\n");
            bw.write("y=" + (int) y + "\n");
            bw.write("r=" + (int) rotation + "\n");
        } catch (IOException ex) {
            Logger.getLogger(Position.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void customLoad(String line) {
        if (line.indexOf("x=") == 0) x = Integer.parseInt(line.replace("x=", ""));
        if (line.indexOf("y=") == 0) y = Integer.parseInt(line.replace("y=", ""));
        if (line.indexOf("r=") == 0) rotation = Integer.parseInt(line.replace("r=", ""));
    }

    public void addWorldX(double x) {
        this.setWorldX(getWorldX() + x);
    }

    public void addWorldY(double y) {
        this.setWorldY(getWorldY() + y);
    }

    public double getWorldX() {
        return x;
    }

    public void setWorldX(double x) {
        if (Math.abs(x - last_y) > 1) World.getWorld().refreshEntity(getParent(), false);
        this.x = x;
        if (Math.abs(x - last_y) > 1) World.getWorld().refreshEntity(getParent(), true);
        if (Math.abs(x - last_y) > 1) this.last_x = x;
    }

    public double getWorldY() {
        return y;
    }

    public void setWorldY(double y) {
        if (Math.abs(x - last_y) > 1) World.getWorld().refreshEntity(getParent(), false);
        this.y = y;
        if (Math.abs(x - last_y) > 1) World.getWorld().refreshEntity(getParent(), true);
        if (Math.abs(x - last_y) > 1) this.last_y = y;
    }

    public int getRotation() {
        return (int) rotation % 360;
    }

    /**
     * Sets the entity's rotation, from degrees.
     * Refreshes the entity hitbox.
     */
    public void setRotation(int r) {
        if (r != rotation) World.getWorld().refreshEntity(getParent(), false);
        this.rotation = r % 360;
        if (r != rotation) World.getWorld().refreshEntity(getParent(), true);
        if (getParent().getHitbox() == null) return;
        getParent().getHitbox().refresh();
    }

}
