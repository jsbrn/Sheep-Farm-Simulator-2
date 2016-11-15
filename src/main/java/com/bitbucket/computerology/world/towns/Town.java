package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.world.Sector;
import java.util.ArrayList;
import java.util.Random;

public class Town {
    
    int x, y, population;
    Sector parent;
    ArrayList<Building> buildings;
    
    public Town() {
        this.buildings = new ArrayList<Building>();
        this.parent = null;
        this.x = -1;
        this.y = -1;
        this.population = Math.abs(new Random().nextInt() % 90)+10;
    }
    
    public void update() {}
    
    public void setParent(Sector s) { parent = s; }
    public Sector getParent() { return parent; }
    public int getWorldX() { return x; }
    public int getWorldY() { return y; }
    public int getPopulation() { return population; }
    
    int getDemand(int resource) {
        int sum = 0;
        for (Building b: buildings) sum+=b.getDemand(resource);
        return sum;
    }
    
    int getPrice(int resource) {
        return 1;
    }
    
}
