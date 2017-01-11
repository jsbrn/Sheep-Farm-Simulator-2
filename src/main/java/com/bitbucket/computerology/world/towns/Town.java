package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.terrain.Sector;
import java.util.ArrayList;
import java.util.Random;

public class Town {
    
    int population, x, y;
    ArrayList<Building> buildings;
    
    public Town(int sector_x, int sector_y) {
        this.buildings = new ArrayList<Building>();
        this.population = Math.abs(new Random().nextInt() % 90)+10;
    }
    
    public void update() {}
    
    public void generate() {
        //generate the noise map describing the building distribution
        //generate buildingss and roads within the parent sector
    }
    
    public int[] getSectorCoordinates() { return new int[]{x, y}; }
    public Sector getParent() { return World.getWorld().getSector(x, y); }
    
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
