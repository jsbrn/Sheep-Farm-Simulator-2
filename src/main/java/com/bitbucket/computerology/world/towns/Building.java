package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.world.entities.Entity;

public class Building {
    
    int[] demand_multipliers, amounts_recieved;
    Town parent_town; Entity parent;
    double popularity_mult;
    
    public Building(Town parent) {
        this.parent_town = parent;
        this.demand_multipliers = new int[Resource.RESOURCE_COUNT];
        this.amounts_recieved = new int[Resource.RESOURCE_COUNT];
    }
    
    public int getDemand(int resource) {
        return demand_multipliers[resource]*getPopularity();
    }
    
    public int getPopularity() {
        return (int)(parent_town.getPopulation()*popularity_mult);
    }
    
}
