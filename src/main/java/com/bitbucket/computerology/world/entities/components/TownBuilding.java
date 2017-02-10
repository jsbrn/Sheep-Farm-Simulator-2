package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.towns.Town;

import java.io.BufferedWriter;
import java.util.ArrayList;

public class TownBuilding extends Component {

    private int type; //ALL

    private int quantity_mult; //INDUSTRIAL
    private double quality_mult; //INDUSTRIAL
    private ArrayList<TownBuilding> clients; //INDUSTRIAL

    private ArrayList<TownBuilding> suppliers; //COMMERCIAL
    private double popularity_multiplier; //COMMERCIAL

    private String products[]; //COMMERCIAL, INDUSTRIAL
    private double base_quantities[]; //COMMERCIAL, INDUSTRIAL

    private double resident_percentage; //RESIDENTIAL

    public TownBuilding() {
        this.type = -1;
    }

    public void init(int type) {

        this.clients = type == Town.INDUSTRIAL_BUILDING ? new ArrayList<TownBuilding>() : null;
        this.suppliers = type == Town.COMMERCIAL_BUILDING ? new ArrayList<TownBuilding>() : null;

        this.quantity_mult = type == Town.INDUSTRIAL_BUILDING ?
                World.getWorld().rng().nextInt(200) + 100 : -1;
        this.quality_mult = type == Town.INDUSTRIAL_BUILDING ?
                World.getWorld().rng().nextDouble() : -1;

        this.products = type == Town.RESIDENTIAL_BUILDING ? null : new String[]{};
        this.base_quantities = type == Town.RESIDENTIAL_BUILDING ? null : new double[]{};

        this.resident_percentage = type == Town.RESIDENTIAL_BUILDING ? 1 : -1; //100%

        this.popularity_multiplier = type == Town.COMMERCIAL_BUILDING ? 1.0 : -1;

    }

    @Override
    public void initParams(ArrayList<String> params) {
        for (String p : params) {
            //in the editor, put type before the rest
            if (p.indexOf("type=") == 0) {
                p = p.replace("type=", "").trim();
                type = p.equals("Residential") ? Town.RESIDENTIAL_BUILDING
                        : (p.equals("Commercial") ? Town.COMMERCIAL_BUILDING : Town.RESIDENTIAL_BUILDING);
                init(type);
            }
            if (p.indexOf("products=") == 0) {
                products = p.trim().split("\\s");
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
    public int getType() {
        return type;
    }

    //INDUSTRIAL
    public int getOutputQuantity() {
        return quantity_mult;
    }

    public double getOutputQuality() {
        return quality_mult;
    }

    public void addClient(TownBuilding b) {
        if (!clients.contains(b)) clients.add(b);
    }

    public ArrayList<TownBuilding> getClients() {
        return clients;
    }

    public boolean hasProduct(String name) {
        for (String s: products) { if (s.equals(name)) return true; }
        return false;
    }

    public boolean addSupplier(TownBuilding b) {
        if (!suppliers.contains(b) && suppliers.size() < 3) {
            suppliers.add(b);
            return true;
        }
        return false;
    }

    public ArrayList<TownBuilding> getSuppliers() {
        return suppliers;
    }

    public double getPopularity() {
        return popularity_multiplier;
    } //COMMERCIAL

    public String[] getProducts() {
        return products;
    } //INDUSTRIAL, COMMERCIAL

    public double[] getBaseQuantities() {
        return base_quantities;
    } //INDUSTRIAL, COMMERCIAL

    //RESIDENTIAL
    public double getResidentPercentage() {
        return resident_percentage;
    }

}
