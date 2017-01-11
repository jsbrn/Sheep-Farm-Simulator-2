package com.bitbucket.computerology.world.towns;

import com.bitbucket.computerology.world.entities.Entity;

public class Building {
    
    public static int RESIDENTIAL = 0, COMMERCIAL = 1, INDUSTRIAL = 2;
    
    int[] demand_multipliers, amounts_recieved;
    Town parent_town; Entity parent;
    double popularity_mult;
    
    public Building(Town parent) {
        this.parent_town = parent;
        this.demand_multipliers = new int[Resource.RESOURCE_COUNT];
        this.amounts_recieved = new int[Resource.RESOURCE_COUNT];
    }
    
    /**
     * Gets the demand of a resource for this building. Consider demand as
     * the amount required on a regular basis.
     * @param resource The Resource ID.
     * @return A demand integer.
     */
    public int getDemand(int resource) {
        return demand_multipliers[resource]*getPopularity();
    }
    
    /**
     * Gets the popularity of the building. Consider this the number of people
     * that purchase from the shop on a regular interval.
     * @return 
     */
    public int getPopularity() {
        return (int)(parent_town.getPopulation()*popularity_mult);
    }
    
}
