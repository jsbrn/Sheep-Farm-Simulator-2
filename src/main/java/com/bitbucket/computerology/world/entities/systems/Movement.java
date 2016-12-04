package com.bitbucket.computerology.world.entities.systems;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.ComponentSystem;
import com.bitbucket.computerology.world.entities.Force;
import com.bitbucket.computerology.world.entities.components.Forces;
import com.bitbucket.computerology.world.entities.components.Position;

public class Movement extends ComponentSystem {
    
    public void update() {
        Component c = getParent().getComponent("Forces");
        Forces f = c == null ? null : ((Forces)c);
        if (f == null) return;
        double dx = 0, dy = 0;
        for (int i = 0; i < f.forceCount(); i++) {
            Force force = f.getForce(i);
            dx += force.velocity()[0];
            dy += force.velocity()[1];
            force.applyAcceleration();
        }
        
        if (dx == 0 && dy == 0) return;
        
        c = getParent().getComponent("Position");
        
        Position p = c == null ? null : ((Position)c);
        
        if (p == null) return;
        p.addWorldX(MiscMath.getConstant(dx, 1));
        p.addWorldY(MiscMath.getConstant(dy, 1));
    }
    
}
