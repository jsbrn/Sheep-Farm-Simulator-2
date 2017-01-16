package com.bitbucket.computerology.world.entities.components;

import com.bitbucket.computerology.world.entities.Component;
import com.bitbucket.computerology.world.entities.Entity;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Texture extends Component {
    
    String texture_url;
    Image texture;
    Color average;
    
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
                    return;
                }
                
            } catch (SlickException ex) {
                Logger.getLogger(Texture.class.getName()).log(Level.SEVERE, null, ex);
            }
            //determine the average color
            int[] rgb = {0, 0, 0}; int count = 0;
            for (int i = 0; i < texture.getWidth(); i++) {
                for (int j = 0; j < texture.getHeight(); j++) {
                    Color c = texture.getColor(i, j);
                    if (c.getAlpha() == 0) continue;
                    rgb[0]+=c.getRed();
                    rgb[1]+=c.getGreen();
                    rgb[2]+=c.getBlue();
                    count++;
                }
            }
            if (count == 0) return;
            average = new Color(rgb[0]/count, rgb[1]/count, rgb[2]/count);
            System.out.println(getParent()+": "+rgb[0]/count+", "+rgb[1]/count+", "+rgb[2]/count);
        }
    }
    
    public void copyTo(Component c) {
        super.copyTo(c);
        if (c instanceof Texture == false) return;
        
        if (this.texture == null) return;
        ((Texture)c).texture = this.texture.getScaledCopy(1);
        ((Texture)c).texture_url = this.texture_url;
    }
    
    public Color getAverage() { return average; }
    public Image getTexture() { return texture; }
    public String getTextureURL() { return texture_url; }
    
}
