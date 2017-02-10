package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.misc.MiscString;

import java.util.ArrayList;
import java.util.Random;

public class Block {

    boolean[] dots; //in, out, yes, no, ok
    int[] dot_conns, param_conns; //the actual connections

    String[][] values; //parametres use this. a value is {value, name, type}
    String output_type; //the type of value that the OUT connection supplies

    String type;
    int id;

    public Block() {
        this.id = Math.abs(new Random().nextInt());
        this.type = "";
        this.dots = new boolean[5];
        this.dot_conns = new int[5];
        this.param_conns = new int[0];
        this.values = new String[0][3];
        this.output_type = "";
    }

    /**
     * Creates a new flowchart block for the list of template blocks.
     *
     * @param title       The title visible in the menu and in the editor.
     * @param type        The type of the block (i.e. "set_color") for the game to recognize.
     * @param category    Where it belongs in the block chooser menu (ex. "All Blocks/Actions"). Only used by the editor.
     * @param input_map   A String that describes which I/Os are active (ex. fffft means only the OK output is active).
     * @param param_count How many parametres does this block have?
     */
    public Block(String type, String input_map, String output_type, String[][] values) {

        this.id = Math.abs(new Random().nextInt());
        this.type = type;
        if (input_map.length() != 5) {
            System.err.println("Block " + type + ": input map " + input_map + " must be 5 chars in length,"
                    + " and must consist of characters t and f only!");
            this.dots = new boolean[]{false, false, false, false, false};
        } else {
            String input = (input_map + "").replace("t", "true\n").replace("f", "false\n");
            ArrayList<String> inputs = MiscString.parseString(input);
            this.dots = new boolean[]{Boolean.parseBoolean(inputs.get(0)),
                    Boolean.parseBoolean(inputs.get(1)),
                    Boolean.parseBoolean(inputs.get(2)),
                    Boolean.parseBoolean(inputs.get(3)),
                    Boolean.parseBoolean(inputs.get(4))};
        }
        this.dot_conns = new int[5];
        this.param_conns = new int[values.length];
        this.values = values;

    }

    public static Block create(String type) {
        Block get = BlockList.getBlock(type), new_b = new Block();
        if (get != null) get.copyTo(new_b);
        return get;
    }

    public void breakConnections() {
        for (int i = 0; i != param_conns.length; i++) {
            param_conns[i] = 0;
        }
        for (int i = 0; i != dot_conns.length; i++) {
            dot_conns[i] = 0;
        }
    }

    public boolean[] dots() {
        return dots;
    }

    public int[] dotConns() {
        return dot_conns;
    }

    public int getID() {
        return id;
    }

    /**
     * Sets the ID of the block to i. This will break any connections referencing the old ID.
     * Should only be used in saving/loading block data.
     */
    public void setID(int i) {
        id = i;
    }

    public int paramCount() {
        return values.length;
    }

    public String getType() {
        return type;
    }

    public String getOutputType() {
        return output_type;
    }

    public void setParametreConnection(int index, int value) {
        if (index <= -1 || index >= param_conns.length) return;
        System.out.println("Parametre " + index + " is now " + value);
        param_conns[index] = value;
    }

    /**
     * Returns a property of the specified parametre. For instance, passing (0, 0) will give you
     * the starting value of the 0th param. (0, 1) will give you the name of the param. (0, 2), the
     * type. Valid types are:
     * <br>
     * <li>number<br>
     * <li>text<br>
     * <li>object<br>
     * <li>level<br>
     * <li>flowchart<br>
     * <li>animation<br>
     * <li>dialogue<br>
     */
    public String getParametre(int index, int dindex) {
        if (dindex < 0 || dindex > 2) return null;
        if (index <= -1 || index >= values.length) return null;
        return values[index][dindex];
    }

    public void randomID() {
        this.id = Math.abs(new Random().nextInt());
    }

    public int getConnection(int index) {
        if (index <= -1 || index >= dot_conns.length) return -1;
        return dot_conns[index];
    }

    public int getParametreConnection(int index) {
        if (index <= -1 || index >= param_conns.length) return -1;
        return param_conns[index];
    }

    public void setConnection(int index, int block_id) {
        if (index <= -1 || index >= dot_conns.length) return;
        dot_conns[index] = block_id;
    }

    public void setParametre(int pindex, int dindex, String new_val) {
        if (dindex < 0 || dindex > 2) return;
        if (pindex <= -1 || pindex >= values.length) return;
        values[pindex][dindex] = new_val;
    }

    public void setDots(boolean in, boolean out, boolean yes, boolean no, boolean ok) {
        dots = new boolean[]{in, out, yes, no, ok};
    }

    public void copyTo(Block b) {
        b.type = type;
        b.id = id;
        b.output_type = output_type;
        b.dots = new boolean[dots.length];
        b.dot_conns = new int[dot_conns.length];
        b.param_conns = new int[param_conns.length];
        b.values = new String[values.length][3];
        for (int i = 0; i != dots.length; i++) {
            b.dots[i] = dots[i];
            b.dot_conns[i] = dot_conns[i];
        }
        for (int i = 0; i != values.length; i++) {
            b.values[i] = new String[]{values[i][0], values[i][1], values[i][2]};
            b.param_conns[i] = param_conns[i];
        }
    }

}
