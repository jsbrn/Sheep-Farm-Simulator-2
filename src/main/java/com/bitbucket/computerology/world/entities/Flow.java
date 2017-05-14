package com.bitbucket.computerology.world.entities;

import java.util.ArrayList;

public class Flow {

    private String name;
    private ArrayList<Block> blocks;
    private Entity parent;

    private Block current;

    public Flow() {
        this.blocks = new ArrayList<Block>();
        this.name = "";
        this.parent = null;
    }

    public void update() {
        if (current != null) current.update();
    }

    public String getID() {
        return name;
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
        if (blocks.contains(b) == false) {
            blocks.add(b);
            b.setParent(this);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public Block getBlockByID(int id) {
        for (Block b : blocks) {
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
        parent = o;
    }

    public void setCurrent(Block b) { current = b; }

    public void copyTo(Flow f) {
        f.parent = parent;
        f.name = name;
        f.blocks.clear();
        for (Block b : blocks) {
            Block new_b = new Block();
            b.copyTo(new_b);
            f.blocks.add(new_b);
        }
    }
}
