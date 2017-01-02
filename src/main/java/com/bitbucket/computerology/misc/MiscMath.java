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
        //(arbitrary setting by me)
        return MiscMath.getConstant(amount_to_add, per_ingame_minutes/1.6);
    }
    
    /**
     * Calculates distance between two points.
     * @return The distance between (x1, y1) and (x2, y2).
     */
    public static double distanceBetween(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
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
    public static boolean pointIntersectsRect(double x, double y, 
            double rx, double ry, int rw, int rh) {
        return x > rx && x < rx + rw && y > ry && y < ry + rh;
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
     * @return A boolean indicating intersection.
     */
    public static boolean rectContainsLine(double x, double y, int w, int h, double lx1, double ly1, double lx2, double ly2) {
        if (pointIntersectsRect(lx1, ly1, x, y, w, h) || pointIntersectsRect(lx2, ly2, x, y, w, h)) return true;
        if (linesIntersect(lx1, ly1, lx2, ly2, x, y, x+w, y)) return true;
        if (linesIntersect(lx1, ly1, lx2, ly2, x, y+h, x+w, y+h)) return true;
        if (linesIntersect(lx1, ly1, lx2, ly2, x, y, x, y+h)) return true;
        if (linesIntersect(lx1, ly1, lx2, ly2, x+w, y, x+w, y+h)) return true;
        return false;
    }

    /**
     * Determines if two rectangles intersect each other. Checks each point to see if they
     * intersect the other rectangle. Calls on pointIntersects().
     * @param x The x of the first rectangle.
     * @param y The y of the first rectangle.
     * @param w The width of the first rectangle.
     * @param h The height of the first rectangle.
     * @param x2 The x of the second rectangle.
     * @param y2 The y of the second rectangle.
     * @param w2 The width of the second rectangle.
     * @param h2 The height of the second rectangle.
     * @return A boolean indicating intersection.
     */
    public static boolean rectanglesIntersect(double x, double y, int w, int h, double x2, double y2, int w2, int h2) {
        if (MiscMath.pointIntersectsRect(x, y, x2, y2, w2, h2) || MiscMath.pointIntersectsRect(x + w, y, x2, y2, w2, h2)
                || MiscMath.pointIntersectsRect(x + w, y + h, x2, y2, w2, h2) || MiscMath.pointIntersectsRect(x, y + h, x2, y2, w2, h2)) {
            return true;
        }
        if (MiscMath.pointIntersectsRect(x2, y2, x, y, w, h) || MiscMath.pointIntersectsRect(x2 + w2, y2, x, y, w, h)
                || MiscMath.pointIntersectsRect(x2 + w2, y2 + h2, x, y, w, h) || MiscMath.pointIntersectsRect(x2, y2 + h2, x, y, w, h)) {
            return true;
        }
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
    
    public static double min(double a, double b) { return a <= b ? a : b; }
    public static double max(double a, double b) { return a >= b ? a : b; }
    
    public static boolean linesIntersect(int[] l, int[] l2) {
        if (l == l2) return true;
        if (l.length != 4 || l2.length != 4) return false;
        return linesIntersect(l[0], l[1], l[2], l[3], l2[0], l2[1], l2[2], l2[3]);
    }
    
    public static boolean linesIntersect(double l1x1, double l1y1, double l1x2, double l1y2, 
            double l2x1, double l2y1, double l2x2, double l2y2) {
        
        //determine the 4 points (of lines P1->P2, P3->P4; where P1 and P3 are the leftmost points of each line)
        double[] p1, p2, p3, p4;
        p1 = l1x1 <= l1x2 ? new double[]{l1x1, l1y1} : new double[]{l1x2, l1y2};
        p2 = l1x1 > l1x2 ? new double[]{l1x1, l1y1} : new double[]{l1x2, l1y2};
        p3 = l2x1 <= l2x2 ? new double[]{l2x1, l2y1} : new double[]{l2x2, l2y2};
        p4 = l2x1 > l2x2 ? new double[]{l2x1, l2y1} : new double[]{l2x2, l2y2};
        
        //calculate the slopes and y-intercepts of both lines
        double run1 = (p2[0]-p1[0]), run2 = (p4[0]-p3[0]), m1, m2, b1, b2;
        //set the slope to the highest integer value if the line is vertical
        //this value is never used, as I have special cases below
        m1 = run1 != 0 ? (p2[1]-p1[1])/run1 : Integer.MAX_VALUE;
        m2 = run2 != 0 ? (p4[1]-p3[1])/run2 : Integer.MAX_VALUE;
        b1 = p1[1]-(p1[0]*m1);
        b2 = p3[1]-(p3[0]*m2);
        
        //special case for if both lines are vertical
        if (run1 == 0 && run2 == 0) {
            if (p1[0] != p3[0]) return false; //if x values don't match, no intersection
            //if any of the 4 y values are touching the other line segment, intersection
            if (p1[1] >= min(p3[1], p4[1]) && p1[1] <= max(p3[1], p4[1])) return true;
            if (p2[1] >= min(p3[1], p4[1]) && p2[1] <= max(p3[1], p4[1])) return true;
            if (p3[1] >= min(p1[1], p2[1]) && p3[1] <= max(p1[1], p2[1])) return true;
            if (p4[1] >= min(p1[1], p2[1]) && p4[1] <= max(p1[1], p2[1])) return true;
        }
        //special cases for if only one of the lines are vertical
        //use the line formula to find the point of intersection at x = line 2, and then compare x/y values
        if (run1 == 0) { 
            if (p1[0] > p4[0] || p1[0] < p3[0]) return false;
            double y2 = (m2*p1[0]) + b2; return y2 >= min(p1[1], p2[1]) && y2 <= max(p1[1], p2[1]); 
        }
        if (run2 == 0) {
            if (p3[0] > p2[0] || p3[0] < p1[0]) return false;
            double y1 = (m1*p3[0]) + b1; return y1 >= min(p3[1], p4[1]) && y1 <= max(p3[1], p4[1]); 
        }

        //for when neither lines are vertical, use Cramer's rule to determine a point of intersection
        double a = 1, b = -m1, c = 1, d = -m2;
        double det = (a*d)-(b*c);
        if (det != 0) {
            double detx = (a*b2)-(b1*c);
            double dety = (b1*d)-(b*b2);
            double x = detx/det, y = dety/det;
            if ((x >= p1[0] && x <= p2[0]) && (x >= p3[0] && x <= p4[0])) return true;
        } else { //if no point to be found, then they are paralell
            if ((int)b1 != (int)b2) return false; //if they have different y-intercepts, no intersection
            //otherwise, check each x value to see if they are touching the other segment
            if (p1[0] >= p3[0] && p1[0] <= p4[0]) return true;
            if (p2[0] >= p3[0] && p2[0] <= p4[0]) return true;
            if (p3[0] >= p1[0] && p3[0] <= p2[0]) return true;
            if (p4[0] >= p1[0] && p4[0] <= p2[0]) return true;
        }
        return false;
    }
    
    /**
     * Returns a rotated point about the origin (0, 0).
     * @param offset_x The x coord.
     * @param offset_y The y coord.
     * @param rotation The angle, in degrees.
     * @return 
     */
    public static int[] getRotatedOffset(int offset_x, int offset_y, double rotation) {
        rotation = Math.toRadians(rotation);
        return new int[]{(int)(offset_x * Math.cos(rotation) - offset_y * Math.sin(rotation)),
            (int)(offset_x * Math.sin(rotation) + offset_y * Math.cos(rotation))};
    }

}