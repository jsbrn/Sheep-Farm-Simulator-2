package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Force;
import java.util.ArrayList;

public class Forces extends Component {

    ArrayList<Force> forces;
    
    public Forces() {this.forces = new ArrayList<Force>();}
    
    public void addForce(Force f) {
        if (!containsForce(f.getID())) {
            forces.add(f);
        }
    }
    
    public int forceCount() { return forces.size(); }
    
    public Force getForce(int index) {
        return (index >= 0 && index <= forces.size()) ? forces.get(index) : null;
    }
    
    public Force getForce(String name) {
        for (Force f: forces) if (name.equals(f.getID())) return f;
        return null;
    }
    
    public void removeForce(String name) {
        Force f = getForce(name);
        if (f != null) forces.remove(f);
    }
    
    public boolean containsForce(String name) {
        return getForce(name) != null;
    }
    
}
