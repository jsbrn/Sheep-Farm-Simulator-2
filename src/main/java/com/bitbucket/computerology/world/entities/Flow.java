package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.world.entities.Entity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Flow {
        
    ArrayList<Block> blocks;
    Entity parent_object;
    
    public String id;
    public boolean run; //run on entity creation
    
    public Flow() {
        this.blocks = new ArrayList<Block>();
        this.run = false;
        this.id = "";
        this.parent_object = null;
    }
    
    public Block getBlock(int index) {
        return blocks.get(index);
    }
    
    public int blockCount() {
        return blocks.size();
    }
    
    public int indexOf(Block b) {
        return blocks.indexOf(b);
    }
    
    public void addBlock(Block b) {
        if (blocks.contains(b) == false) blocks.add(b);
    }
    
    public Block getBlockByID(int id) {
        for (Block b: blocks) {
            if (b.getID() == id) {
                return b;
            }
        }
        return null;
    }
    
    public void removeBlock(int index) {
        blocks.remove(index);
    }
    
    public void setParent(Entity o) {
        parent_object = o;
    }
    
    /**
     * Copies the contents of this flow to flow F.
     * @param f The flow, stupid.
     */
    public void copyTo(Flow f) {
        f.parent_object = parent_object;
        f.id = id;
        f.blocks.clear();
        for (Block b: blocks) {
            Block new_b = new Block();
            b.copyTo(new_b);
            f.blocks.add(new_b);
        }
    }
}