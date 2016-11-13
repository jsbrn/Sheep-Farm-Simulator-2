package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Component;
import java.util.ArrayList;

public class Position extends Component {
    
    int x, y;
    float rotation;
    
    public Position() {}
    
    @Override
    public void initParams(ArrayList<String> p) {
        for (String s: p) {
            System.out.println("Parsing param: "+s);
            if (s.indexOf("x=") == 0) x = Integer.parseInt(s.replace("x=", ""));
            if (s.indexOf("y=") == 0) y = Integer.parseInt(s.replace("y=", ""));
            if (s.indexOf("rotation=") == 0) rotation = Integer.parseInt(s.replace("rotation=", ""));
        }
    }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setRotation(int r) { this.rotation = r % 360; }
    
    public int getWorldX() { return x; }
    public int getWorldY() { return y; }
    public int getRotation() { return (int)rotation % 360; }
    
}
