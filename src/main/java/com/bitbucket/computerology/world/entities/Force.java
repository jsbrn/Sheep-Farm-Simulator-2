package com.bitbucket.computerology.world.entities;

import com.bitbucket.computerology.misc.MiscMath;

public class Force {
    
    String id;
    //original velocities
    double o_dx, o_dy;
    //dx, dy velocities; ax, ay multipliers
    double dx, dy, ax, ay;
    
    boolean stop_at_0 = false;
    
    public Force(String id) {
        this.id = id;
        this.dx = 0; this.dy = 0;
        this.ax = 1; this.ay = 1;
        this.o_dx = dx; this.o_dy = dy;
    }
    
    /**
     * Sets whether the force will stop accelerating if both axes are 0.
     */
    public void stopAtZero(boolean b) { stop_at_0 = b; }
    
    public boolean stopAtZero() { return stop_at_0; }
    
    public String getID() { return id; }
    
    public double[] velocity() {
        return new double[]{dx, dy};
    }
    
    public double[] acceleration() {
        return new double[]{ax, ay};
    }
    
    public void setXVelocity(double x) { dx = x; }
    public void setYVelocity(double y) { dy = y; }
    public void setXAcceleration(double x) { ax = x; }
    public void setYAcceleration(double y) { ay = y; }
    
    public void applyAcceleration() {
        boolean acc_x = true, acc_y = true;
        if ((o_dx >= 0 && dx <= 0)
                || (o_dx <= 0 && dx >= 0)) acc_x = false;
        if ((o_dy >= 0 && dy <= 0)
                || (o_dy <= 0 && dy >= 0)) acc_y = false;
        if (!stop_at_0) { acc_x = true; acc_y = true; }
        if (acc_x) dx += MiscMath.getConstant(ax, 1);
        if (acc_y) dy += MiscMath.getConstant(ay, 1);
        
    }
    
    public void reset() {
        dx = o_dx;
        dy = o_dy;
    }
    
}
