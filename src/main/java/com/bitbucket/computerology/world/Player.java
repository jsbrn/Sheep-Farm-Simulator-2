package com.bitbucket.computerology.world;

import com.bitbucket.computerology.world.terrain.Chunk;
import com.bitbucket.computerology.world.terrain.Sector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Player {

    int money = 0;

    public Player() {
    }

    public int getMoney() {
        return money;
    }

    public void save(BufferedWriter bw) {
        try {
            bw.write("p\n");
            bw.write("m=" + money + "\n");
            bw.write("/p\n");
        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean load(BufferedReader br) {
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.equals("/p")) return true;
                if (line.indexOf("m=") == 0) money = Integer.parseInt(line.replace("m=", ""));
            }
        } catch (IOException ex) {
            Logger.getLogger(Sector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
