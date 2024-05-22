package JettersR.Graphics.SpriteRenderers;
/**
 * Author: Luke Sullivan
 * Last Edit: 5/6/2023
 */
//import org.joml.Vector3f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class BasicSpriteRenderer extends SpriteRenderer
{
    @FunctionalInterface//Render Function Interface
    public interface RenderFunction{public abstract void invoke(Screen screen);}//, fixedVector3 f_position);}
    public RenderFunction renderFunction = null;

    /**Constructor.*/
    public BasicSpriteRenderer(Sprite sprite, fixedVector3 f_position, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset, //boolean fixed,
    boolean canColorBlend)
    {
        this.sprite = sprite;
        this.f_position = f_position;
        this.f_offset.set(f_xOffset, f_yOffset, f_zOffset);
        //this.fixed = fixed;

        if(canColorBlend){renderFunction = this::renderBlend;}
        else{renderFunction = this::renderNoBlend;}
    }

    /**Default Center Offset Constructor.*/
    public BasicSpriteRenderer(Sprite sprite, fixedVector3 f_position, boolean canColorBlend)
    {this(sprite, f_position, fixed(-sprite.getWidth()) / 2, fixed(-sprite.getHeight()) / 2, 0, canColorBlend);}

    //Render Function.
    private void renderNoBlend(Screen screen)//, fixedVector3 f_position)
    {
        screen.renderSprite
        (
            f_toInt(f_position.x + f_offset.x),
            f_toInt(f_position.y + f_offset.y),
            f_toInt(f_position.z + f_offset.z) + (sprite.getHeight() * 2), sprite.getHeight(),
            sprite, flip, true
        );
    }

    //Render Function.
    private void renderBlend(Screen screen)//, fixedVector3 f_position)
    {
        screen.renderSprite
        (
            f_toInt(f_position.x + f_offset.x),
            f_toInt(f_position.y + f_offset.y),
            f_toInt(f_position.z + f_offset.z) + (sprite.getHeight() * 2), sprite.getHeight(),
            sprite, flip, blendingColor, true
        );
    }


    /**Render Function.*/
    @Override
    public void render(Screen screen)//, fixedVector3 f_position)
    {renderFunction.invoke(screen);}//, f_position);}

    /**UI Render Function.*/
    @Override
    public void render2D(Screen screen, int x, int y)
    {
        screen.renderSprite
        (
            (int)(x + f_offset.x),
            (int)(y + f_offset.y),
            sprite, flip, true
        );
    }
}
