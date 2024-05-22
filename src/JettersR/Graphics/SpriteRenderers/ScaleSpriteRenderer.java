package JettersR.Graphics.SpriteRenderers;
/**
 * 
 */
//import org.joml.Vector3f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class ScaleSpriteRenderer extends SpriteRenderer
{
    @FunctionalInterface//Render Function
    //public interface RenderFunction{public abstract void invoke(Screen screen, fixedVector3 f_position, float scaleOffset);}
    public interface RenderFunction{public abstract void invoke(Screen screen, float scaleOffset);}
    public RenderFunction renderFunction = null;

    //Scale values for the Sprite.
    private float xScale = 1.0f, yScale = 1.0f;

    /**Constructor.*/
    public ScaleSpriteRenderer(Sprite sprite, fixedVector3 f_position, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset, boolean canColorBlend)
    {
        this.sprite = sprite;
        this.f_position = f_position;
        this.f_offset.set(f_xOffset, f_yOffset, f_zOffset);
        //this.fixed = fixed;

        if(canColorBlend){renderFunction = this::renderBlend_Sc;}
        else{renderFunction = this::renderNoBlend_Sc;}
    }

    /**Default Center Offset Constructor.*/
    public ScaleSpriteRenderer(Sprite sprite, fixedVector3 f_position, boolean canColorBlend)
    {this(sprite, f_position, fixed(-sprite.getWidth()) / 2, fixed(-sprite.getHeight()) / 2, 0, canColorBlend);}

    //Getters
    public float getXScale(){return this.xScale;}
    public float getYScale(){return this.yScale;}

    //Setters
    public void setXScale(float xScale){this.xScale = xScale;}
    public void setYScale(float yScale){this.yScale = yScale;}
    public void setScale(float xScale, float yScale){this.xScale = xScale; this.yScale = yScale;}
    public void setScale(float scale){this.xScale = scale; this.yScale = scale;}
    public void f_setScaleAndCenter(@fixed int f_scale)
    {
        f_offset.x = f_mul(f_div(fixed(-sprite.getWidth()), fixed(2)), f_scale);
        f_offset.y = f_mul(f_div(fixed(-sprite.getHeight()), fixed(2)), f_scale);
        //
        float fs = f_toFloat(f_scale);
        this.xScale = fs;
        this.yScale = fs;
    }

    //Add Scale
    public void addXScale(float xScale){this.xScale += xScale;}
    public void addYScale(float yScale){this.yScale += yScale;}

    //Render Function.
    //private void renderNoBlend_Sc(Screen screen, fixedVector3 f_position, float scaleOffset)
    private void renderNoBlend_Sc(Screen screen, float scaleOffset)
    {
        float xs = xScale * scaleOffset, ys = yScale * scaleOffset;
        //
        screen.renderSprite_Sc
        (
            (int)( f_toFloat(f_position.x + f_offset.x) * scaleOffset ),
            (int)( (f_toFloat(f_position.y + f_offset.y) + sprite.getHeight()) * scaleOffset ),
            (int)( (f_toFloat(f_position.z + f_offset.z) + (sprite.getHeight() * 2)) * scaleOffset ), (int)((sprite.getHeight() * yScale * 2) * scaleOffset),
            sprite, flip, 0, 0, xs, ys, true
        );
    }

    //Render Function.
    //private void renderBlend_Sc(Screen screen, fixedVector3 f_position, float scaleOffset)
    private void renderBlend_Sc(Screen screen, float scaleOffset)
    {
        float xs = xScale * scaleOffset, ys = yScale * scaleOffset;
        //
        screen.renderSprite_Sc
        (
            (int)( f_toFloat(f_position.x + f_offset.x) * scaleOffset ),
            (int)( (f_toFloat(f_position.y + f_offset.y) + sprite.getHeight()) * scaleOffset ),
            (int)( (f_toFloat(f_position.z + f_offset.z) + (sprite.getHeight() * 2)) * scaleOffset ), (int)((sprite.getHeight() * yScale * 2) * scaleOffset),
            //(int)((f_toFloat(f_position.z) + f_offset.z) * scaleOffset), (int)((sprite.getHeight() * yScale * 2) * scaleOffset),
            sprite, flip, 0, 0, blendingColor, xs, ys, true
        );
    }

    //private int w0 = 0, w1 = 0;

    /**Lighting Render Function.*/
    public void renderLighting(Screen screen, Sprite normalMap, fixedVector3 f_position, float scaleOffset)
    {
        float xs = xScale * scaleOffset, ys = yScale * scaleOffset;
        //
        screen.renderSprite_Sc
        (
            (int)( f_toFloat(f_position.x + f_offset.x) * scaleOffset ),
            (int)( (f_toFloat(f_position.y + f_offset.y) + sprite.getHeight()) * scaleOffset ),
            (int)( (f_toFloat(f_position.z + f_offset.z) + (sprite.getHeight() * 2)) * scaleOffset ), (int)((sprite.getHeight() * yScale * 2) * scaleOffset),
            sprite, flip, normalMap, 0.0f,//TODO Emission value.
            0, 0, xs, ys, true
        );

        //w0 = (w0+1) % 2;
        //w1 = (w1 + ((w0 == 0) ? 1 : 0)) % 120;
    }

    /**Lighting Render Function.*/
    public void renderLighting_Blend(Screen screen, Sprite normalMap, fixedVector3 f_position, float scaleOffset)
    {
        float xs = xScale * scaleOffset, ys = yScale * scaleOffset;
        //
        screen.renderSprite_Sc
        (
            (int)( f_toFloat(f_position.x + f_offset.x) * scaleOffset ),
            (int)( (f_toFloat(f_position.y + f_offset.y) + sprite.getHeight()) * scaleOffset ),
            (int)( (f_toFloat(f_position.z + f_offset.z) + (sprite.getHeight() * 2)) * scaleOffset ), (int)((sprite.getHeight() * yScale * 2) * scaleOffset),
            sprite, flip, normalMap, 0.0f,
            0, 0, xs, ys, true
        );

        //w0 = (w0+1) % 2;
        //w1 = (w1 + ((w0 == 0) ? 1 : 0)) % 120;
    }

    /**Camera-Related Render Function.*/
    public void render(Screen screen, float scaleOffset)
    {renderFunction.invoke(screen, scaleOffset);}

    /**Camera-Related Render Function.*/
    //public void render(Screen screen, float x, float y, float z, float scaleOffset)
    //{renderFunction.invoke(screen, x, y, z, scaleOffset);}

    @Override
    /**Render Function.*/
    public void render(Screen screen)
    {renderFunction.invoke(screen, 0.0f);}

    /*
    public void render(Screen screen)//, float x, float y, float z)
    {
        //renderFunction.invoke(screen, x, y, z, 0.0f);
        renderFunction.invoke(screen, f_position.x, f_position.y, f_position.z, 0.0f);
    }
    */

    /**UI Render Function.*/
    @Override
    public void render2D(Screen screen, int x, int y)
    {
        screen.renderSprite_Sc
        (
            x + f_toInt(f_offset.x),
            //(int)((x + offset.x) - (((sprite.getWidth() * xScale) - sprite.getWidth()) / 2)),
            y + f_toInt(f_offset.y),
            //(int)((y + offset.y) - (((sprite.getHeight() * yScale) - sprite.getHeight()) / 2)),
            sprite, flip, xScale, yScale, true
        );
    }
}
