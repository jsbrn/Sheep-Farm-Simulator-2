package com.bitbucket.computerology.world.entities.systems;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.World;
import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.ComponentSystem;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.Force;
import com.bitbucket.computerology.world.entities.components.Forces;
import com.bitbucket.computerology.world.entities.components.Hitbox;
import com.bitbucket.computerology.world.entities.components.Position;
import java.util.ArrayList;

public class Movement extends ComponentSystem {
    
    public void update() {
        Entity p = getParent();
        Forces f = p.getForces();
        Position pos = p.getPosition();
        if (pos == null || f == null) return;
        double dx = 0, dy = 0;
        for (int i = 0; i < f.forceCount(); i++) {
            Force force = f.getForce(i);
            dx += force.velocity()[0];
            dy += force.velocity()[1];
            force.applyAcceleration();
        }
        
        if (dx == 0 && dy == 0) return;

        Hitbox h = p.getHitbox();
        boolean move_x = true, move_y = true;
        if (h != null) {
            ArrayList<Entity> entities = World.getWorld().getEntities(
                    x, y, w, h);
        }
        if (move_x) pos.addWorldX(MiscMath.getConstant(dx, 1));
        if (move_y) pos.addWorldY(MiscMath.getConstant(dy, 1));
    }
    
}
