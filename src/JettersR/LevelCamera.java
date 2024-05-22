package JettersR;
/**
 * Author: Luke Sullivan
 * Last Edit: 3/8/2024
 */
import java.util.List;
import java.util.ArrayList;

//import org.joml.Vector3f;

import JettersR.Entities.Entity;
import JettersR.Entities.Components.Lights.Light;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.Misc.Rhombus;
import JettersR.Util.Octree;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class LevelCamera
{
    //Level instance.
    //private Level level = null;

    //Camera position.
    //private Vector3f position = new Vector3f(),
    //
    //potentialPosition = new Vector3f(),
    //potentialVelocity = new Vector3f(),
    //
    //entityPosition = new Vector3f(),
    //entityVelocity = new Vector3f();
    //
    private fixedVector3 f_position = new fixedVector3(),
    //
    f_potentialPosition = new fixedVector3(),
    f_potentialVelocity = new fixedVector3(),
    //
    f_entityPosition = new fixedVector3(),
    f_entityVelocity = new fixedVector3();

    private float scale = 1.0f;
    //private @fixed int f_scale = fixed(1, 0);

    //Camera Collision Stuff.
    private Rhombus shape;

    //ID of currently active camera bounds.
    //private int currentBound_ID =  0;

    //Rather this camera is bounded by camera boundaries or not.
    private boolean isBounded = true;

    private boolean ignoreLights = false;

    /**Constructor.*/
    public LevelCamera(Level level, int width, int baseHeight, int depth)
    {
        //this.level = level;
        this.shape = new Rhombus
        (
            width, baseHeight, fixed(2), depth, Rhombus.TYPE_UP,
            0, 0, -fixed(depth) >> 1
        );
        this.shape.f_setYOffset(-fixed(this.shape.getHeight() - this.shape.getBaseHeight()) / 2);
    }

    /**Constructor.*/
    public LevelCamera(Level level)
    {this(level, Game.NORMAL_WIDTH, Game.NORMAL_HEIGHT, (33 << Level.TILE_BITS));}

    //public Vector3f getEntityPosition(){return entityPosition;}
    public fixedVector3 f_getEntityPosition(){return f_entityPosition;}
    public void setEntityPosition(Entity entity, fixedVector3 f_velocity)
    {
        //Reference reasons.
        this.f_entityPosition = null;

        //NOW set.
        this.f_entityPosition = entity.f_getPosition();

        //this.entityVelocity = null;
        //this.entityVelocity = entity.getCollisionObject().f_getTargetVelocity();
        this.f_entityVelocity = f_velocity;
    }

    private static final @fixed int f_pv_inc =
    //fixed(1);
    fixed(0,32);

    /**Updates this camera's position.*/
    public void update()
    {
        if(isBounded)
        {
            /*
            //X Friction
            if(f_potentialVelocity.x < f_entityVelocity.x)
            {
                f_potentialVelocity.x += f_pv_inc;//(friction * timeMod);
                if(f_potentialVelocity.x > f_entityVelocity.x){f_potentialVelocity.x = f_entityVelocity.x;}
            }
            else if(f_potentialVelocity.x > f_entityVelocity.x)
            {
                f_potentialVelocity.x -= f_pv_inc;//(friction * timeMod);
                if(f_potentialVelocity.x < f_entityVelocity.x){f_potentialVelocity.x = f_entityVelocity.x;}
            }

            //Y Friction
            if(f_potentialVelocity.y < f_entityVelocity.y)
            {
                f_potentialVelocity.y += f_pv_inc;//(friction * timeMod);
                if(f_potentialVelocity.y > f_entityVelocity.y){f_potentialVelocity.y = f_entityVelocity.y;}
            }
            else if(f_potentialVelocity.y > f_entityVelocity.y)
            {
                f_potentialVelocity.y -= f_pv_inc;//(friction * timeMod);
                if(f_potentialVelocity.y < f_entityVelocity.y){f_potentialVelocity.y = f_entityVelocity.y;}
            }
            */

            /*
            //Z Gravity
            if(entityVelocity.z > potentialVelocity.z)
            {
                potentialVelocity.z += 0.5f;//(mass * gravity * timeMod);
                if(potentialVelocity.z > entityVelocity.z){potentialVelocity.z = entityVelocity.z;}
            }
            else if(entityVelocity.z < potentialVelocity.z)
            {
                potentialVelocity.z -= 0.5f;//(mass * gravity * timeMod);
                if(potentialVelocity.z < entityVelocity.z){potentialVelocity.z = entityVelocity.z;}
            }
            */


            //Set potential position relative to entity.
            f_potentialPosition.set
            (
                f_entityPosition.x + (f_potentialVelocity.x * 4) - (shape.f_getWidth() >> 1),
                f_entityPosition.y + (f_potentialVelocity.y * 4) - (shape.f_getBaseHeight() >> 1),
                f_entityPosition.z + (f_potentialVelocity.z * 4)// - (shape.f_getDepth() >> 1)
            );

            //Check if entity has moved into a different camera boundary (priority on higher ID bounds).
            
            //Check collision with the current camera boundary and move accordingly.
            //if(entityPosition.x > -200 && potentialPosition.x < -200){potentialPosition.x = -200;}
            //if(entityPosition.y > -200 && potentialPosition.y < -200){potentialPosition.y = -200;}

            //NOW set the actual position.
            //position.set(potentialPosition);
            f_position.set(f_potentialPosition);
        }
    }

    
    //Thread lock.
    public final Object lightLock = new Object();

    //Light list filled from the Octree.
    private List<Light> lights = new ArrayList<Light>();

    /**Checks what lights are within view of the camera.*/
    public void lightCheck(Screen screen, Octree<Light> octree)
    {
        if(ignoreLights){return;}

        //Don't run this code if the update thread is in the middle of filling the Octree.
        synchronized(lightLock)
        {
            //Get Collision Objects from Level Octree.
            lights.clear();
            octree.retrieve(this.f_position, this.shape, lights);

            //float scale = f_toFloat(f_scale);

            //Iterate through each Light.
            for(int i = 0; i < lights.size(); i++)
            {
                //Cache the current Light.
                Light l = lights.get(i);

                //Perform collision check between Camera View and Light.
                if(shape.performCollision(this.f_position, fixedVector3.f_ZEROS, l.getShape(), l.f_getPosition()))
                {
                    l.render(screen, scale);
                    //System.out.println("checked");

                    //Add it to rendering.
                    screen.addLight(l, scale);
                }
            }
        }
    }

    //public float getX(){return position.x;}
    //public int getScaledX(){return (int)(position.x * scale);}
    //
    //public float getY(){return position.y;}
    //public int getScaledY(){return (int)(position.y * scale);}
    //public int getVisualY(){return (int)((position.y - (position.z/2)) * scale);}
    //
    //public float getZ(){return position.z;}
    //public int getScaledZ(){return (int)(position.z * scale);}
    //
    public @fixed int getX(){return f_toInt(f_position.x);}
    public int getScaledX(){return (int)(f_toFloat(f_position.x) * scale);}
    //
    public @fixed int getY(){return f_toInt(f_position.y);}
    public int getScaledY(){return (int)(f_toFloat(f_position.y) * scale);}
    public int getVisualY()
    {
        return (int)( f_toFloat(f_position.y - (f_position.z >> 1)) * scale);
        //return f_toInt( f_mul(f_position.y - f_div(f_position.z, fixed(2, 0)), f_scale) );
    }
    //
    public @fixed int getZ(){return f_toInt(f_position.z);}
    public int getScaledZ(){return (int)(f_toFloat(f_position.z) * scale);}

    //public Vector3f getPosition(){return position;}
    //public void setPosition(float x, float y, float z){this.position.set(x, y, z);}
    //public void addPosition(float xv, float yv, float zv){this.position.add(xv, yv, zv);}
    //
    public fixedVector3 f_getPosition(){return f_position;}
    public void f_setPosition(@fixed int x, @fixed int y, @fixed int z){this.f_position.set(x, y, z);}
    public void f_addPosition(@fixed int xv, @fixed int yv, @fixed int zv){this.f_position.add(xv, yv, zv);}

    //Scale Getter/Setter/Adder.
    public float getScale(){return scale;}
    public void setScale(float scale, int screenWidth, int screenHeight)
    {
        //Set scale value.
        this.scale = scale;

        //Calculate new width and height.
        int w = (int)(screenWidth / this.scale),
        h = (int)(screenHeight / this.scale),
        d = (int)((33 * Level.TILE_SIZE) / this.scale);

        //Update Rhombus.
        shape.setDimensions(w, h, d);
        this.shape.f_setYOffset(-fixed(this.shape.getHeight() - this.shape.getBaseHeight()) / 2);
        this.shape.f_setZOffset(-fixed(d) / 2);
    }
    public void addScale(float scale, int width, int height)
    {
        this.scale += scale;

        //Calculate new width and height.
        int w = (int)(width / this.scale),
        h = (int)(height / this.scale),
        d = (int)((33 * Level.TILE_SIZE) / this.scale);

        //Update Rhombus.
        shape.setDimensions(w, h, d);
        this.shape.f_setYOffset(-fixed(this.shape.getHeight() - this.shape.getBaseHeight()) / 2);
        this.shape.f_setZOffset(-fixed(d) / 2);
    }
    //
    /*
    public @fixed int getScale(){return f_scale;}
    public void setScale(@fixed int scale, int width, int height)
    {
        //Set scale value.
        this.f_scale = scale;

        //Calculate new width and height.
        //int w = (int)(width / this.scale),
        int w = f_toInt( f_div( fixed(width), this.f_scale ) ),
        //h = (int)(height / this.scale);
        h = f_toInt( f_div( fixed(height), this.f_scale ) );

        //Update Rhombus.
        shape.setDimensions(w, h, (33 * Level.TILE_SIZE));
    }
    public void addScale(@fixed int f_scale, int width, int height)
    {
        this.f_scale += f_scale;

        //Calculate new width and height.
        int w = f_toInt( f_div( fixed(width), this.f_scale ) ),
        h = f_toInt( f_div( fixed(height), this.f_scale ) );

        //Update Rhombus.
        shape.setDimensions(w, h, (33 * Level.TILE_SIZE));
    }
    */

    public int getWidth(){return shape.getWidth();}
    public int getHeight(){return shape.getHeight();}
    public int getDepth(){return shape.getDepth();}

    public void setBounded(boolean isBounded){this.isBounded = isBounded;}

    public void render(Screen screen, float scale)
    {
        //f_position.print();
        //System.out.println(screen.getXOffset() + " " + screen.getYOffset() + " " + screen.getZOffset());
        shape.render(screen, scale, this.f_position);
    }
}
