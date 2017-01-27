package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.towns.Town;
import java.io.BufferedWriter;
import java.util.ArrayList;

public class TownBuilding extends Component {
    
    private int type; //ALL
    
    private int output_quantity; //INDUSTRIAL
    private double quality_score; //INDUSTRIAL
    private ArrayList<TownBuilding> clients; //INDUSTRIAL
    
    private String goods[]; //COMMERCIAL
    private double base_quantities[]; //COMMERCIAL
    private double popularity_score; //COMMERCIAL
    
    private int capacity, residents; //RESIDENTIAL
    private double demand_multiplier; //RESIDENTIAL
    
    public TownBuilding() { this.type = -1; }
    
    public void init(int type) {
        this.clients = new ArrayList<TownBuilding>();
        this.output_quantity = type == Town.INDUSTRIAL_BUILDING ? 
                World.getWorld().rng().nextInt(200) + 100 : 0;
        this.quality_score = type == Town.INDUSTRIAL_BUILDING ? 
                World.getWorld().rng().nextDouble() : -1;
        
        this.capacity = type == Town.RESIDENTIAL_BUILDING ? 10 : -1;
        this.residents = capacity;
        this.demand_multiplier = type == Town.RESIDENTIAL_BUILDING ? 
                World.getWorld().rng().nextDouble() + 0.5 : -1;
        
        this.popularity_score = type == Town.COMMERCIAL_BUILDING ? 1.0 : -1;
    }
    
    public void initParam(ArrayList<String> params) {
        for (String p: params) {
            //in the editor, put type before the rest
            if (p.indexOf("type=") == 0) {
                p = p.replace("type=", "").trim();
                type = p.equals("Residential") ? Town.RESIDENTIAL_BUILDING 
                        : (p.equals("Commercial") ? Town.COMMERCIAL_BUILDING : Town.RESIDENTIAL_BUILDING);
                init(type);
            }
            if (p.indexOf("goods=") == 0) {
                goods = p.trim().split("\\s");
            }
            if (p.indexOf("base_quantities=") == 0) {
                String[] qs = p.trim().split("\\s");
                base_quantities = new double[qs.length];
                for (int i = 0; i < qs.length; i++) base_quantities[i] = Double.parseDouble(qs[i]);
            }
        }
    }

    @Override
    public void customLoad(String line) {
        System.err.println("Load function for TownBuilding not implemented.");
    }

    @Override
    public void customSave(BufferedWriter bw) {
        System.err.println("Save function for TownBuilding not implemented.");
    }
    
    //ALL
    public int getType() { return type; }
    
    //INDUSTRIAL
    public int getOutputQuantity() { return output_quantity; }
    public double getOutputQuality() { return quality_score; }
    public void addClient(TownBuilding b) {
        if (!clients.contains(b)) clients.add(b);
    }
    public ArrayList<TownBuilding> getClients() { return clients; }
    
    public double getPopularity() { return popularity_score; } //INDUSTRIAL
    public String[] getGoods() { return goods; } //INDUSTRIAL, COMMERCIAL
    public double[] getGoodsQuantities() { return base_quantities; } //INDUSTRIAL, COMMERCIAL
    
    //RESIDENTIAL
    public int getResidentCount() { return residents; }
    public int getResidentCapacity() { return capacity; }
    public double getResidentDemandMultiplier() { return demand_multiplier; }

}
