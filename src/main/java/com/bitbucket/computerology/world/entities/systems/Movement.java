package com.bitbucket.computerology.world.entities.systems;

import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.World;
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

        Hitbox hitbox = p.getHitbox();
        //convert dx, dy into per-frame amounts
        dx = MiscMath.getConstant(dx, 1); dy = MiscMath.getConstant(dy, 1);
        //create the points A and B
        double[] A = {Math.floor(Math.min(p.getWorldX()-(p.getWidth()/2), p.getWorldX()-(p.getWidth()/2)+dx)), 
            Math.floor(Math.min(p.getWorldY()-(p.getHeight()/2), p.getWorldY()-(p.getHeight()/2)+dy))};
        double[] B = {Math.ceil(Math.max(p.getWorldX()+(p.getWidth()/2), p.getWorldX()+(p.getWidth()/2)+dx)), 
            Math.ceil(Math.max(p.getWorldY()+(p.getHeight()/2), p.getWorldY()+(p.getHeight()/2)+dy))};
        //essentially just incrementing the hitbox by 4px each time until it reaches the destination
        //and if it finds any collisions, revert to the last increment that had no collision
        //it is very crude, but at least I have proper rotating hitboxes, so I can easily come back
        //and implement a better solution when the rest of the game is done
        if (hitbox != null) {
            ArrayList<Entity> entities = 
                    World.getWorld().getEntities((int)A[0], (int)A[1], (int)(B[0]-A[0]), (int)(B[1]-A[1]));

            double total_dist = MiscMath.distance(0, 0, dx, dy);
            double dist = 0, i = 0, incr_x = dx*(4/total_dist), incr_y = dy*(4/total_dist);
            boolean stop = false;
            while (dist < total_dist+4 && !stop) {
                i++;
                dist+=4;
                for (Entity e: entities) {
                    hitbox.offset(incr_x*i, incr_y*i);
                    if (e.intersects(p)) {
                        i--;
                        stop = true;
                        break;
                    }
                }
            }
            if (stop) { dx = incr_x*i; dy = incr_y*i; }
            
        }
        pos.addWorldX(dx);
        pos.addWorldY(dy);
    }
    
}
