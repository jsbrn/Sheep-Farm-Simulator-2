package com.bitbucket.computerology.world.events;

import com.bitbucket.computerology.world.World;
import java.util.ArrayList;

public class EventHandler {
    
    private ArrayList<Event> scheduled, active;
    
    private boolean addnew;
    
    public EventHandler() {
        this.scheduled = new ArrayList<Event>();
        this.active = new ArrayList<Event>();
        this.addnew = true;
    }
    
    public final void update() {
        for (Event e: active) {
            e.update();
            if (e.isFinished()) { active.remove(e); break; }
        }
        if (World.getWorld().getTime() % 15 == 0) {
            if (addnew) newEvent(); 
            addnew = false;
        } else { addnew = true; }
    }
    
    void newEvent() {
        System.out.println("New event!!! ("+World.getWorld().getTime()+")");
    }
    
    final void startNextEvent() {
        if (scheduled.isEmpty()) return;
        active.add(scheduled.remove(0));
    }
    
}
