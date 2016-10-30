package com.bitbucket.computerology.world.entities.components.types;

import com.bitbucket.computerology.world.entities.components.Component;
import org.newdawn.slick.Image;

public class Texture extends Component {
    
    Image texture;
    
    public Texture() {}
    
    public void setTexture(Image i) {
        texture = i;
    }
    
    public Image getTexture() {
        return texture;
    }
    
}
