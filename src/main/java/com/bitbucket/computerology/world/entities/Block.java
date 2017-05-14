package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.misc.Assets;

import javax.annotation.Nullable;
import java.util.Random;

public class Block {

    //output types
    public static final int NO_OUTPUT = -1, NUMBER = 0, STRING = 1;
    //block types
    public static final int ACTION_BLOCK = 0, CONDITIONAL_BLOCK = 1,
            EVENT_BLOCK = 2, VARIABLE_BLOCK = 3;

    public static final int NODE_COUNT = 4, IN = 0, OUT = 1, YES = 2, NO = 3;

    private boolean[] nodes;
    private int[] connections;

    private int output_type; //the title of value that the OUT connection supplies
    private String output; //the output value stored

    private String title;
    private int id;

    private Flow parent;

    public static Block create(String type) {
        Block get = Assets.getBlock(type), new_b = new Block();
        if (get != null) get.copyTo(new_b);
        return get;
    }

    public Block() {
        this.id = Math.abs(new Random().nextInt());
        this.title = "";
        this.nodes = new boolean[NODE_COUNT];
        this.connections = new int[NODE_COUNT];
        this.output_type = NO_OUTPUT;
    }

    /**
     * Creates a new flowchart block for the list of template blocks.
     */
    public Block(String title, int block_type, int output_type) {

        this.id = Math.abs(new Random().nextInt());
        this.title = title;
        this.nodes = new boolean[]{

                block_type == ACTION_BLOCK || block_type == CONDITIONAL_BLOCK || block_type == VARIABLE_BLOCK,
                block_type != CONDITIONAL_BLOCK, //to
                block_type == CONDITIONAL_BLOCK, //yes
                block_type == CONDITIONAL_BLOCK, //no

        };
        this.connections = new int[NODE_COUNT];
        this.output_type = output_type;

    }

    public void update() {}

    /**
     * Set the Flow to the next block. Pass in -1 or an invalid index, and
     * the flow will stop executing.
     * @param conn_index
     */
    public final void to(int conn_index) {
        Block b = getBlock(conn_index);
        parent.setCurrent(b);
        b.from(this);
    }

    public final void to(int conn_index, Object output) {
        Block b = getBlock(conn_index);
        parent.setCurrent(b);
        b.from(this, output);
    }

    public void from(Block from) {}

    public void from(Block from, Object input) {}

    public void setParent(Flow f) { parent = f; }

    public int getID() {
        return id;
    }
    public void setID(int id) { this.id = id; }
    public String getTitle() {
        return title;
    }

    public int getOutputType() { return output_type; }
    public String getOutput() { return output; }

    public Block getBlock(int connection_index) {
        if (connection_index < 0 || connection_index >= connections.length) return null;
        if (connections[connection_index] > 0)
            return parent.getBlock(connections[connection_index]);
        return null;
    }

    @Nullable
    public Object[] getParameter(int p_index) {
        Block b = getBlock(NODE_COUNT+p_index);
        if (b == null) return new Object[]{null, null};
        return new Object[]{b.getOutput(), b.getOutputType()};
    }

    public void setNodes(boolean[] nodes) {
        this.nodes = nodes;
    }
    public void setConnections(int[] d) {
        this.connections = d;
    }

    public void copyTo(Block b) {
        b.title = title;
        b.id = id;
        b.output_type = output_type;
        b.nodes = new boolean[nodes.length];
        b.connections = new int[nodes.length];
        for (int i = 0; i != nodes.length; i++) {
            b.nodes[i] = nodes[i];
            b.connections[i] = connections[i];
        }
    }

}
