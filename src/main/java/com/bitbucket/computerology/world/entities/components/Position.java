package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Position extends Component {
    
    double x, y;
    float rotation;
    
    public Position() {}
    
    @Override
    public void customSave(BufferedWriter bw) {
        try {
            bw.write("x="+(int)x);
            bw.write("y="+(int)y);
            bw.write("r="+(int)rotation);
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

    public void setWorldX(int x) { this.x = x; }
    public void setWorldY(int y) { this.y = y; }
    public void addWorldX(double x) { this.x += x; }
    public void addWorldY(double y) { this.y += y; }
    
    
    public void setRotation(int r) { this.rotation = r % 360; }
    
    public int getWorldX() { return (int)x; }
    public int getWorldY() { return (int)y; }
    public int getRotation() { return (int)rotation % 360; }
    
}
