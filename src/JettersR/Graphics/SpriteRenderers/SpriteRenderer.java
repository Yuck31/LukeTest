package JettersR.Graphics.SpriteRenderers;
/**
 * Author: Luke Sullivan
 * Last Edit: 5/8/2023
 */
//import org.joml.Vector3i;
//import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public abstract class SpriteRenderer
{
    //Sprite Object.
    protected Sprite sprite = null;

    //Position pointer from holder.
    //protected Vector3f f_position;
    protected fixedVector3 f_position;

    //Offset.
    public final fixedVector3 f_offset = new fixedVector3();

    //Flip Attribute.
    protected byte flip = Sprite.FLIP_NONE;

    //Vector4f representing the color to blend with.
    protected Vector4f blendingColor;

    //Sprite Getter/Setter.
    public final Sprite getSprite(){return sprite;}
    public final void setSprite(Sprite sprite){this.sprite = sprite;}

    //Position Getters.
    public final @fixed int f_getXPosition(){return f_position.x;}
    public final @fixed int f_getYPosition(){return f_position.y;}
    public final @fixed int f_getZPosition(){return f_position.z;}
    public final fixedVector3 f_getPosition(){return this.f_position;}

    //Offset Getters.
    public final @fixed int f_getXOffset(){return f_offset.x;}
    public final @fixed int f_getYOffset(){return f_offset.y;}
    public final @fixed int f_getZOffset(){return f_offset.z;}
    public final fixedVector3 f_getOffset(){return this.f_offset;}

    //Offset setters.
    public final void setXOffset(int xOffset){this.f_offset.x = fixed(xOffset);}
    public final void f_setXOffset(@fixed int f_xOffset){this.f_offset.x = f_xOffset;}
    //
    public final void setYOffset(int yOffset){this.f_offset.y = fixed(yOffset);}
    public final void f_setYOffset(@fixed int f_yOffset){this.f_offset.y = f_yOffset;}
    //
    public final void setOffset(int xOffset, int yOffset, int zOffset){this.f_offset.setInt(xOffset, yOffset, zOffset);}
    public final void f_setOffset(@fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset){this.f_offset.set(f_xOffset, f_yOffset, f_zOffset);}
    public final void f_setOffset(fixedVector3 f_offset){this.f_offset.set(f_offset);}

    //FlipAttribute Getter/Setter
    public final byte getFlip(){return this.flip;}
    public final void setFlip(byte flip){this.flip = flip;}
    
    /**Sets this SpriteRenderer's blendingColor.*/
    public final void setBlendingColor(float r, float g, float b, float a){blendingColor.set(r, g, b, a);}

    /**Render Function.*/
    public abstract void render(Screen screen);//, fixedVector3 position);
    //public abstract void render(Screen screen);

    /**UI Render Function.*/
    public abstract void render2D(Screen screen, int x, int y);
}
