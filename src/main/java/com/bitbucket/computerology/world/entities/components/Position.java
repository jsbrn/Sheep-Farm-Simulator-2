package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Component;

public class Position extends Component {
    int x, y;
    float rotation;
    
    public Position() {}
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setRotation(int r) { this.rotation = r % 360; }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getRotation() { return (int)rotation % 360; }
    
}
