package com.bitbucket.computerology.misc;

public class MiscMath {
    
    public static int DELTA_TIME = 1;

    /**
     * Calculate the amount to add per frame to reach a certain value in a certain amount of time.
     * Accounts for delta time and entity update schedules.
     * @param amount_to_add The amount to add in the time specified.
     * @param per_x_seconds The time specified.
     * @param frame_skip_multiplier Whether the output is multiplied by the frame skip multiplier
     * (a value that is determined by the number of frames skipped before updating an entity again).
     * @return The amount to add per frame.
     */
    public static double getConstant(double amount_to_add, double per_x_seconds) {
        if (per_x_seconds == 0) { return 0; }
        double time_in_mills = per_x_seconds * 1000;
        double add_per_frame = amount_to_add / time_in_mills;
        add_per_frame*=DELTA_TIME;
        return add_per_frame;
    }
    
    /**
     * Performs the same function as MiscMath.getConstant, however, the second parameter specifies the
     * number of in-game minutes that will pass before the amount to add is reached.
     * @param amount_to_add The amount to add.
     * @param per_ingame_minutes The number of in-game minutes it should take to add.
     * @param frame_skip_multiplier Whether the output is multiplied by the frame skip multiplier
     * (a value that is determined by the number of frames skipped before updating an entity again).
     * @return The amount to add per frame.
     */
    public static double get24HourConstant(double amount_to_add, double per_ingame_minutes) {
        //divides the second parameter by 1.6 because each minute in game takes 1/1.6 seconds in real life
        return MiscMath.getConstant(amount_to_add, per_ingame_minutes/1.6);
    }
    
    /**
     * Calculates distance between two points.
     * @return The distance between (x1, y1) and (x2, y2).
     */
    public static double distanceBetween(double x1, double y1, double x2, double y2) {
        double distance_squared = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        double distance = Math.sqrt(distance_squared);
        return distance;
    }
    
    /**
     * Finds the angle between (x, y) and (x2, y2) with 0 degrees being a vertical line.
     * @return A double representing the angle in degrees.
     */
    public static double angleBetween(double x1, double y1, double x2, double y2) {
        //slope formula = (Y2 - Y1) / (X2 - X1)
        double x, y, new_rotation;
        if (x1 < x2) {
            x = (x1 - x2) * -1;
            y = (y1 - y2) * -1;
            new_rotation = (((float)Math.atan(y/x) * 60));
        } else {
            x = (x2 - x1) * -1;
            y = (y1 - y2);
            new_rotation = (((float)Math.atan(y/x) * 60) + 180);
        }

        new_rotation += 90;
        return new_rotation % 360;
    }

    /**
     * Determines if point (x,y) intersects rectangle (rx, ry, rw, rh).
     * @param x The x value of the point.
     * @param y The y value of the point.
     * @param rx The x value of the rectangle.
     * @param ry The y value of the rectangle.
     * @param rw The width of the rectangle.
     * @param rh The height of the rectangle.
     * @return A boolean indicating whether the point intersects.
     */
    public static boolean pointIntersects(double x, double y, double rx, double ry, int rw, int rh) {
        if (x > rx && x < rx + rw) {
            if (y > ry && y < ry + rh) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines if the rectangle with origin (x, y) and dimensions of (w, h) 
     * intersects the line between points (lx1, ly1) and (lx2, ly2).
     * @param x Rectangle x value.
     * @param y Rectangle y value.
     * @param w Rectangle width.
     * @param h Rectangle height.
     * @param lx1 x value of line endpoint #1
     * @param ly1 y value of line endpoint #1
     * @param lx2 x value of line endpoint #2
     * @param ly2 y value of line endpoint #2
     * @return
     */
    public static boolean rectangleIntersectsLine(double x, double y, int w, int h, double lx1, double ly1, double lx2, double ly2) {
        //determine values to be used in the equation for the line
        double m = (ly2-ly1)/(lx2-lx1);
        double p = lx1, q = ly1; //p = the offset from left side of screen, q = offset from bottom
        //if point l2 is closer to x = 0 than l1, set p and q to lx2's coordinates
        if (lx2 < lx1) {
            p = lx2;
            q = ly2;
        }
        //test if both end points of line are on left side, right, top, or bottom
        //if any is true, then the line does not intersect
        boolean on_left = (lx1 < x && lx2 < x), on_right = (lx1 > x+w && lx2 > x+w), 
                on_top = (ly1 < y && ly2 < y), on_bottom = (ly1 > y+h && ly2 > y+h); 
        if (!on_left && !on_right && !on_top && !on_bottom) {
            if (((y < (m*(x-p)+q)) && (y+h > (m*(x-p)+q)))
                    || ((y < (m*(x+w-p)+q)) && (y+h > (m*(x+w-p)+q)))) { //if left side or right side of rectangle intersects line
                return true;
            } 
            if ((x < (((y-q)/m)+p) && x+w > (((y-q)/m)+p))
                || (x < (((y+h-m)/q)+p) && x+w > (((y+h-q)/m)+p))) { //if top side or bottom side of rectangle intersects line
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if two rectangles intersect each other.
     * @param x The x of the first rectangle.
     * @param y The y of the first rectangle.
     * @param w The width of the first rectangle.
     * @param h The height of the first rectangle.
     * @param x2 The x of the second rectangle.
     * @param y2 The y of the second rectangle.
     * @param w2 The width of the second rectangle.
     * @param h2 The height of the second rectangle.
     * @return A boolean indicating if the two rectangles intersect.
     */
    public static boolean rectanglesIntersect(double x, double y, int w, int h, double x2, double y2, int w2, int h2) {
        //if any of the corners of rectangle 1 intersect rectangle 2
        if (MiscMath.pointIntersects(x, y, x2, y2, w2, h2)
                || MiscMath.pointIntersects(x + w, y, x2, y2, w2, h2)
                || MiscMath.pointIntersects(x + w, y + h, x2, y2, w2, h2)
                || MiscMath.pointIntersects(x, y + h, x2, y2, w2, h2)) {
            return true;
        }
        //and vice versa
        if (MiscMath.pointIntersects(x2, y2, x, y, w, h)
                || MiscMath.pointIntersects(x2 + w2, y2, x, y, w, h)
                || MiscMath.pointIntersects(x2 + w2, y2 + h2, x, y, w, h)
                || MiscMath.pointIntersects(x2, y2 + h2, x, y, w, h)) {
            return true;
        }
        //else return false
        return false;
    }
    
    /**
     * Check if a rectangle intersects a circle.
     * @param cx The origin x of the circle.
     * @param cy The origin y of the circle.
     * @param r The radius of the circle.
     */
    public static boolean rectangleIntersectsCircle(double x, double y, int w, int h, double cx, double cy, int r) {
        double r_x = x+(w/2), r_y = y+(h/2);
        double min_width = (w/2)+r, min_height = (h/2)+r;
        return MiscMath.distanceBetween(r_x, 0, cx, 0) < min_width 
                && MiscMath.distanceBetween(r_y, 0, cy, 0) < min_height;
    }

}