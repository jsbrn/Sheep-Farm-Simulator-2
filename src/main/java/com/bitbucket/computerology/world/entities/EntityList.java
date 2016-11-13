package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.misc.MiscString;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityList {
    
    static ArrayList<Entity> ENTITIES;
    
    public static Entity getEntity(String type) {
        for (Entity e: ENTITIES) if (e.type.equals(type)) return e;
        return null;
    }
    
    public static void loadEntityList() {
        ENTITIES = new ArrayList<Entity>();
        try {
            InputStream in = EntityList.class.getResourceAsStream("/misc/entity_list.txt"); 
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("e")) {
                    Entity e = new Entity();
                    if (loadEntity(e, br)) ENTITIES.add(e);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static boolean loadEntity(Entity e, BufferedReader br) {
        System.out.println("Loading entity...");
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/e")) return true;
                if (line.indexOf("id=") == 0) e.type = line.trim().replace("id=", "");
                if (line.equals("c")) {
                    Component c = loadComponent(br);
                    if (c != null) {
                        c.initParams();
                        e.addComponent(c);
                    }
                }
                if (line.equals("s")) {
                    ComponentSystem c = loadSystem(br);
                    if (c != null) e.addSystem(c);
                }
                /*if (line.equals("a")) {
                    Animation c = new Animation();
                    if (c.load(br)) animations.add(c);
                }*/
                if (line.equals("f")) {
                    Flow f = new Flow();
                    if (loadFlow(f, br)) e.addFlow(f);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    static Component loadComponent(BufferedReader br) {
        System.out.println("Loading component...");
        Component c = null;
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/c")) return c;
                if (line.indexOf("id=") == 0) {
                    c = Component.create(line.trim().replace("id=", ""));
                }
                else {
                    if (c != null) c.params+=line+"\n";
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    static ComponentSystem loadSystem(BufferedReader br) {
        System.out.println("Loading system...");
        ComponentSystem c = null;
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/s")) return c;
                if (line.indexOf("id=") == 0) {
                    c = ComponentSystem.create(line.trim().replace("id=", ""));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    static boolean loadFlow(Flow f, BufferedReader br) {
        System.out.println("Loading flow...");
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/f")) return true;
                if (line.indexOf("id=") == 0) f.id = line.trim().replace("id=", "");
                if (line.indexOf("r=") == 0) f.run = Boolean.parseBoolean(line.trim().replace("t=", ""));
                if (line.equals("b")) {
                    Block b = new Block();
                    if (loadBlock(b, br)) f.blocks.add(b);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    static boolean loadBlock(Block b, BufferedReader br) {
        System.out.println("Loading block...");
        try {
            while (true) {
                String line = br.readLine();
                if (line == null) break;
                if (line.equals("/b")) return true;
                if (line.indexOf("id=") == 0) b.id = Integer.parseInt(line.trim().replace("id=", ""));
                if (line.indexOf("t=") == 0) { 
                    b.type = line.trim().replace("t=", "");
                    Block copy = BlockList.getBlock(b.type);
                    if (copy != null) {
                        copy.copyTo(b); 
                    } else { 
                        System.err.println("Block "+b.type+" does not exist in BLOCK_LIST!");
                        return false;
                    }
                }
                if (line.indexOf("dconns=") == 0) {
                    ArrayList<String> bs = MiscString.parseString(line.trim().replace("dconns=", "").replace(" ", "\n"));
                    b.dot_conns = new int[5];
                    for (int i = 0; i != 5; i++) b.dot_conns[i] = Integer.parseInt(bs.get(i));
                }
                if (line.indexOf("pconns=") == 0) {
                    ArrayList<String> bs = MiscString.parseString(line.trim().replace("pconns=", "").replace(" ", "\n"));
                    b.param_conns = new int[bs.size()];
                    for (int i = 0; i != bs.size(); i++) b.param_conns[i] = Integer.parseInt(bs.get(i));
                }
                if (line.indexOf("vals=") == 0) {
                    ArrayList<String> bs = MiscString.parseString(line.trim().replace("vals=", "").replace("\t", "\n"));
                    for (int i = 0; i != bs.size(); i++) b.values[i][0] = bs.get(i);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
}