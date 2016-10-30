package com.bitbucket.computerology.world.entities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityList {
    
    static ArrayList<EntityTemplate> ALL_ENTITIES;
    
    public static String get(int id, String component, String property) {
        if (id < 0 || id >= ALL_ENTITIES.size()) return "";
        EntityTemplate e = ALL_ENTITIES.get(id);
        for (ComponentTemplate c: e.components) {
            int index = c.value_names.indexOf(property);
            if (index > -1) {
                return c.values.get(index);
            }
        }
        return "";
    }
    
    public static void load() {
        ALL_ENTITIES = new ArrayList<EntityTemplate>();
        Properties prop = new Properties();
        FileInputStream f;
        try {
            f = new FileInputStream(System.getProperty("user.home")+"/entity_list.txt");
            prop.load(f);
            
            int e_count = Integer.parseInt(prop.getProperty("entitycount"));
            for (int e = 0; e != e_count; e++) {
                EntityTemplate ent = new EntityTemplate(prop.getProperty("entity_"+e+"_name"));
                int c_count = Integer.parseInt(prop.getProperty("entity_"+e+"_componentcount"));
                for (int c = 0; c != c_count; c++) {
                    ComponentTemplate com = new ComponentTemplate(prop.getProperty("entity_"+e+"_component_"+c+"_name"));
                    int p_count = Integer.parseInt(prop.getProperty("entity_"+e+"_component_"+c+"_propertycount"));
                    for (int p = 0; p != p_count; p++) {
                        com.value_names.add(prop.getProperty("entity_"+e+"_component_"+c+"_property_"+p+"_name"));
                        com.values.add(prop.getProperty("entity_"+e+"_component_"+c+"_property_"+p+"_value"));
                    }
                    ent.components.add(com);
                }
                int s_count = Integer.parseInt(prop.getProperty("entity_"+e+"_systemcount"));
                for (int s = 0; s != s_count; s++) {
                    ent.systems.add(new ComponentSystemTemplate(prop.getProperty("entity_"+e+"system"+s+"_name")));
                }
                int s2_count = Integer.parseInt(prop.getProperty("entity_"+e+"_scriptcount"));
                for (int s = 0; s != s2_count; s++) {
                    ScriptTemplate sc = new ScriptTemplate(prop.getProperty("entity_"+e+"_script"+s+"_name"));
                    sc.loop = (Boolean.parseBoolean(prop.getProperty("entity_"+e+"_script"+s+"_loops")));
                    sc.startup = (Boolean.parseBoolean(prop.getProperty("entity_"+e+"_script"+s+"_startup")));
                    sc.group = (prop.getProperty("entity_"+e+"_script"+s+"_group"));
                    ent.scripts.add(sc);
                }
                ALL_ENTITIES.add(ent);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EntityList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

class EntityTemplate {
    String name;
    ArrayList<ComponentSystemTemplate> systems;
    ArrayList<ComponentTemplate> components;
    ArrayList<ScriptTemplate> scripts;
    public EntityTemplate(String name) {
        this.name = name;
        systems = new ArrayList<ComponentSystemTemplate>();
        components = new ArrayList<ComponentTemplate>();
        scripts = new ArrayList<ScriptTemplate>();
    }
}

class ScriptTemplate {
    String name, group;
    boolean loop, startup;
    public ScriptTemplate(String name) {
        this.name = name;
        this.name = "";
        this.group = "";
        this.loop = false;
        this.startup = false;
    }
}

class ComponentTemplate {
    String name;
    ArrayList<String> value_names, values;
    public ComponentTemplate(String name) {
        this.name = name;
        this.value_names = new ArrayList<String>();
        this.values = new ArrayList<String>();
    }
}

class ComponentSystemTemplate {
    String name;
    ArrayList<String> value_names, values;
    public ComponentSystemTemplate(String name) {
        this.name = name;
        this.value_names = new ArrayList<String>();
        this.values = new ArrayList<String>();
    }
}
