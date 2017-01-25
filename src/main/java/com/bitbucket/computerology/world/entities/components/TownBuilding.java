package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.towns.Town;
import java.util.ArrayList;

public class TownBuilding extends Component {
    
    private int type; //ALL
    
    private int output_quantity; //INDUSTRIAL
    private double quality_score; //INDUSTRIAL
    
    private String goods[]; //INDUSTRIAL, COMMERCIAL
    private double base_quantities[]; //INDUSTRIAL, COMMERCIAL
    
    private double popularity_score; //COMMERCIAL
    
    private int capacity, residents; //RESIDENTIAL
    private double demand_multiplier; //RESIDENTIAL
    
    public TownBuilding() {}
    
    public void init(int type) {
        this.output_quantity = type == Town.INDUSTRIAL_BUILDING ? World.getWorld().rng().nextInt(200) + 100 : 0;
        this.quality_score = type == Town.INDUSTRIAL_BUILDING ? World.getWorld().rng().nextDouble() : -1;
        
        this.capacity = type == Town.RESIDENTIAL_BUILDING ? 10 : -1;
        this.residents = capacity;
        this.demand_multiplier = type == Town.RESIDENTIAL_BUILDING ? World.getWorld().rng().nextDouble() + 0.5 : -1;
        
        this.popularity_score = type == Town.COMMERCIAL_BUILDING ? 1.0 : -1;
    }
    
    public void initParam(ArrayList<String> params) {
        for (String s: params) {
            if (s.indexOf("type=") == 0) {
                s = s.replace("type=", "").trim();
                type = s.equals("Residential") ? Town.RESIDENTIAL_BUILDING : (s.equals("Commercial") ? Town.COMMERCIAL_BUILDING : Town.RESIDENTIAL_BUILDING);
            }
        }
    }
    
    public int getType() { return type; }
    
    public int getOutputQuantity() { return output_quantity; }
    public double getOutputQuality() { return quality_score; }
    
    public double getPopularity() { return popularity_score; }
    public String[] getGoods() { return goods; }
    public double[] getGoodsQuantities() { return base_quantities; }
    
    public int getResidentCount() { return residents; }
    public int getResidentCapacity() { return capacity; }
    public double getResidentDemandMultiplier() { return demand_multiplier; }

}
