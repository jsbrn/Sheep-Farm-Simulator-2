package com.bitbucket.computerology.world.entities;

import java.util.ArrayList;

public class BlockList {
    static ArrayList<Block> BLOCK_LIST;
    
    public static void loadBlockList() {
        System.out.println("Loading BLOCK_LIST...");
        BLOCK_LIST = new ArrayList<Block>();
        
        Block b = new Block("move", "tttff", "", new String[][]{{"", "x", "number"}, {"", "y", "number"}});
        BLOCK_LIST.add(b);

        for (Block b2: BLOCK_LIST) System.out.println(b2.type);
        
    }
    
    public static Block getBlock(String type) {
        for (Block b: BLOCK_LIST) if (b.type.equals(type)) return b;
        return null;
    }

}
