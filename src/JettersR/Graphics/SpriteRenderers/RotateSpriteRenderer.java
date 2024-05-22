package JettersR.Graphics.SpriteRenderers;
import org.joml.Matrix4f;
/**
 * A Sprite Renderer that can render a Rotated Sprite.
 * 
 * Author: Luke Sullivan
 * Last Edit: 1/28/2022
 */
//import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class RotateSpriteRenderer extends SpriteRenderer
{
    @FunctionalInterface//Render Function
    public interface RenderFunction{public abstract void invoke(Screen screen);}//, fixedVector3 f_position);}
    public RenderFunction renderFunction = null;

    //Rotation Degrees.
    private float rads = 0f;

    //Rotation Origin point.
    private int originX, originY;

    /**Constructor.*/
    public RotateSpriteRenderer(Sprite sprite, fixedVector3 f_position, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset,
    float degrees, int originX, int originY, boolean canColorBlend)
    {
        this.sprite = sprite;
        this.f_position = f_position;
        this.f_offset.set(f_xOffset, f_yOffset, f_zOffset);
        //this.fixed = fixed;

        setDegrees(degrees);
        this.originX = originX;
        this.originY = originY;

        if(canColorBlend){renderFunction = this::renderBlend_Ro;}
        else{renderFunction = this::renderNoBlend_Ro;}
    }

    /**Default Center Offset Constructor.*/
    public RotateSpriteRenderer(Sprite sprite, fixedVector3 f_position, boolean canColorBlend)
    {
        this(sprite, f_position, fixed(-sprite.getWidth()) / 2, fixed(-sprite.getHeight()) / 2, 0,
        0f, (sprite.getWidth()/2), (sprite.getHeight()/2), canColorBlend);
    }

    //Getters
    public float getRads(){return rads;}
    public float getDegrees(){return (float)(rads * (180/Math.PI));}

    public int getOriginX(){return originX;}
    public int getOriginY(){return originY;}

    //Setters
    public void setRads(float rads){this.rads = rads;}
    public void setDegrees(float degrees){this.rads = (float)(degrees * (Math.PI/180));}

    public void setOriginX(int originX){this.originX = originX;}
    public void setOriginY(int originY){this.originY = originY;}
    public void setOrigin(int originX, int originY)
    {
        this.originX = originX;
        this.originY = originY;
    }

    public static final float MAX_RADS = (float)(360 * (Math.PI/180));

    //Adder
    public void addRads(float rads){this.rads += rads;}
    public void addDegrees(float degrees){this.rads += (float)(degrees * (Math.PI/180)) % MAX_RADS;}

    protected Matrix4f mat = new Matrix4f().ortho(0, 640, 360, 0, 360, 0);
    protected Vector4f vec = new Vector4f();

    //Render Function.
    private void renderNoBlend_Ro(Screen screen)//, fixedVector3 f_position)
    {
        screen.renderSprite_Ro
        (
            f_toInt(f_position.x + f_offset.x),
            f_toInt(f_position.y + f_offset.y),
            f_toInt(f_position.z + f_offset.z) + (sprite.getHeight() * 2), sprite.getHeight(),
            sprite, flip, rads, originX, originY, true
        );

        addRads(0.01f);
    }

    //Render Function.
    private void renderBlend_Ro(Screen screen)//, fixedVector3 f_position)
    {
        screen.renderSprite_Ro
        (
            f_toInt(f_position.x + f_offset.x),
            f_toInt(f_position.y + f_offset.y),
            f_toInt(f_position.z + f_offset.z) + (sprite.getHeight() * 2), sprite.getHeight(),
            sprite, flip, blendingColor, rads, originX, originY, true
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
        screen.renderSprite_Ro
        (
            //TODO
            f_toInt(x + f_offset.x),
            (int)(y + f_offset.y),
            sprite, flip, rads, originX, originY, true
        );
    }
}
