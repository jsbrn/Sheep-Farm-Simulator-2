package com.bitbucket.computerology.world.entities;

import java.util.ArrayList;

public class Blueprint {

    private ArrayList<String> data;
    private String name;

    public Blueprint() {
        this.data = new ArrayList<String>();
        this.name = "blueprint"+(int)(Math.random()*10000);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void add(String formatted) {
        if (!data.contains(formatted)) data.add(formatted);
    }

    public void remove(int i) {
        if (i >= 0 && i <= data.size()) data.remove(i);
    }

    public int entityCount() {
        return data.size();
    }

    public String get(int i) {
        if (i >= 0 && i <= data.size()) return data.get(i);
        return "";
    }

    public String getEntityType(int i) {
        String get = get(i);
        return get.length() > 0 ? get.substring(0, get.lastIndexOf(" [")) : get;
    }

    /**
     * Returns the offset of the specific entity entry from the origin of the blueprint (from 4x4 cells).
     * @param i The entry index
     * @return An integer array of length 2 indicating {x, y}.
     */
    public int[] getEntityOffset(int i) {
        String get = get(i);
        String coords_str = get.substring(get.lastIndexOf(" [")+ 2, get.lastIndexOf("]"));
        String[] coords = coords_str.split("\\s");
        return get.length() > 0 ? new int[]{Integer.parseInt(coords[0]), Integer.parseInt(coords[1])} : new int[]{0, 0};
    }

}
