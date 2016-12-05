package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
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

    public void setWorldX(double x) {
        //World.getWorld().removeEntity(getParent());
        this.x = x; 
        //World.getWorld().addEntity(getParent());
    }
    public void setWorldY(double y) { 
        //World.getWorld().removeEntity(getParent());
        this.y = y; 
        //World.getWorld().addEntity(getParent());
    }
    public void addWorldX(double x) { 
        this.setWorldX(getWorldX()+x);
    }
    public void addWorldY(double y) { 
        this.setWorldY(getWorldY()+y);
    }
    public void setRotation(int r) { 
        this.rotation = r % 360;
    }
    
    public double getWorldX() { return x; }
    public double getWorldY() { return y; }
    public int getRotation() { return (int)rotation % 360; }
    
}
