package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Texture extends Component {
    
    String texture_url;
    Image texture;
    
    public Texture() {this.texture_url = "";}
    
    @Override
    public void initParams(ArrayList<String> p) {
        for (String s: p) {
            if (s.indexOf("texture=") == 0) { 
                texture_url = s.replace("texture=", "").trim(); 
            }
        }
        if (texture_url.length() > 0) {
            try {
                texture = new Image("images/entities/"+texture_url, false, Image.FILTER_NEAREST);
                if (texture.getWidth() > Entity.maxSizePixels() || texture.getHeight() > Entity.maxSizePixels()) {
                    System.err.println(getParent().getType()+": images/entities/"+texture_url+" "
                            + "is larger than the maximum allowed dimensions!");
                }
            } catch (SlickException ex) {
                Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void copyTo(Component c) {
        super.copyTo(c);
        if (c instanceof Texture == false) return;
        ((Texture)c).texture = this.texture.getScaledCopy(1);
        ((Texture)c).texture_url = this.texture_url;
    }
    
    public Image getTexture() { return texture; }
    
    public String getTextureURL() { return texture_url; }
    
}
