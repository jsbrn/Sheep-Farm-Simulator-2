package com.bitbucket.computerology.world.entities;

import java.util.ArrayList;

public class BlockList {
    static ArrayList<Block> BLOCK_LIST;
    
    public static void loadBlockList() {
        System.out.println("Loading BLOCK_LIST...");
        BLOCK_LIST = new ArrayList<Block>();
        
        Block b = new Block("move", "tttff", "", new String[][]{{"", "x", "number"}, {"", "y", "number"}});
        BLOCK_LIST.add(b);
        
        System.out.println("Successfully loaded "+BLOCK_LIST.size()+" flowchart blocks!");
        
    }
    
    public static Block getBlock(String type) {
        for (Block b: BLOCK_LIST) if (b.type.equals(type)) return b;
        return null;
    }

}
