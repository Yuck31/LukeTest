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

public class ShearSpriteRenderer extends SpriteRenderer
{
    @FunctionalInterface//Render Function
    public interface RenderFunction{public abstract void invoke(Screen screen);}//, fixedVector3 f_position);}
    public RenderFunction renderFunction = null;

    private float xShear = 0.0f, yShear = 0.0f,
    zxShear = 0.0f, zyShear = 0.0f;

    /**Constructor.*/
    public ShearSpriteRenderer(Sprite sprite, fixedVector3 f_position, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset, //boolean fixed,
    boolean canColorBlend)
    {
        this.sprite = sprite;
        this.f_position = f_position;
        this.f_offset.set(f_xOffset, f_yOffset, f_zOffset);
        //this.fixed = fixed;
        //
        if(canColorBlend){renderFunction = this::renderBlend_Sh;}
        else{renderFunction = this::renderNoBlend_Sh;}
    }

    /**Default Constructor.*/
    public ShearSpriteRenderer(Sprite sprite, fixedVector3 f_position, boolean canColorBlend)
    {this(sprite, f_position, fixed(-sprite.getWidth()) / 2, fixed(-sprite.getHeight()) / 2, 0, canColorBlend);}

    public float getXShear(){return xShear;}
    public void setXShear(float xShear){this.xShear = xShear;}

    public float getYShear(){return yShear;}
    public void setYShear(float yShear){this.yShear = yShear;}

    public float getZXShear(){return zxShear;}
    public void setZXShear(float zxShear){this.zxShear = zxShear;}

    public float getZYShear(){return zyShear;}
    public void setZYShear(float zyShear){this.zyShear = zyShear;}


    public void setShear(float xShear, float yShear)
    {
        this.xShear = xShear;
        this.yShear = yShear;
    }

    public void setShear(float xShear, float yShear, float zxShear, float zyShear)
    {
        this.xShear = xShear;
        this.yShear = yShear;
        this.zxShear = zxShear;
        this.zyShear = zyShear;
    }

    private float sx = 0;

    //Render Function.
    private void renderNoBlend_Sh(Screen screen)//, fixedVector3 f_position)
    {
        screen.renderSprite_Sh
        (
            f_toInt(f_position.x + f_offset.x),
            f_toInt(f_position.y + f_offset.y),
            f_toInt(f_position.z + f_offset.z),
            //(int)((z + offset.z + (sprite.getHeight() * 2))),
            sprite, flip, -sx, sx, 0, 0, true
        );

        sx = ((sx + 0.01f) % 5);
        //if(sx < 1){sx = 1;}
        //sx = 2.3f;
        //System.out.println(sx);
    }

    //Render Function.
    private void renderBlend_Sh(Screen screen)//, fixedVector3 f_position)
    {
        screen.renderSprite_Sh
        (
            f_toInt(f_position.x + f_offset.x),
            f_toInt(f_position.y + f_offset.y),
            f_toInt(f_position.z + f_offset.z),
            //f_toInt( (f_position.z / 2) - sprite.getHeight() ),
            sprite, flip, blendingColor, xShear, yShear, zxShear, zyShear, true
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
        screen.renderSprite_Sh
        (
            //TODO
            f_toInt(x + f_offset.x),
            (int)(y + f_offset.y),
            sprite, flip, xShear, yShear, true
        );
    }
}
