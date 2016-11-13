package com.bitbucket.computerology.world.entities;

import java.util.ArrayList;

public class BlockList {
    static ArrayList<Block> BLOCK_LIST;
    
    public static void loadBlockList() {
        BLOCK_LIST = new ArrayList<Block>();
        
        Block b = new Block("Move", "move", "tttff", "", new String[][]{{"", "x", "number"}, {"", "y", "number"}});
        Block.BLOCK_LIST.add(b);
        
    }
    
    public static Block getBlock(String type) {
        for (Block b: BLOCK_LIST) if (b.type.equals(type)) return b;
        return null;
    }
    
}
