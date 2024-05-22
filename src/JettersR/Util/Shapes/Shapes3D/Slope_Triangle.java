package JettersR.Util.Shapes.Shapes3D;
/**
 * Author: Luke Sullivan
 * Last Edit: 5/2/2023
 */
//import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Level;
//import JettersR.Entities.CollisionObject;
import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Slope_Triangle extends Shape_Box
{
    public enum Type
    {
        UP, RIGHT, DOWN, LEFT
    }
    public Type type;

    //protected float zSlope_Bottom, Z_intercept;//, XY_intercept;
    //protected float XYtoZ_slope = 1.0f, ZtoXY_slope = 1.0f,
    //normXY = 0.0f, normZ = 0.0f;
    //
    protected @fixed int f_zSlope_Bottom;
    protected @fixed int f_zIntercept;
    protected @fixed int f_XYtoZ_slope;// = fixed(1);
    protected @fixed int f_ZtoXY_slope;// = fixed(1);
    protected @fixed int f_normXY;
    protected @fixed int f_normZ;

    /**Constructor.*/
    public Slope_Triangle(int width, int height, int depth, Type type, int rise, int run,
    @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset)
    {
        super(width, height, depth, f_xOffset, f_yOffset, f_zOffset);
        setSlope(type, rise, run);
    }

    /**Default offset Constructor.*/
    public Slope_Triangle(int width, int height, int depth, Type type, int rise, int run)
    {this(width, height, depth, type, rise, run, fixed(-width/2), fixed(-height/2), fixed(-depth/2));}

    /**Default Constructor.*/
    public Slope_Triangle(int width, int height, int depth, Type type)
    {this(width, height, depth, type, depth, (type == Type.LEFT || type == Type.RIGHT) ? width : height, fixed(-width/2), fixed(-height/2), fixed(-depth/2));}

    //width, height, depth, type, rise, run
    /*
     * ----
     * |   \
     * |    \
     * |     \
     * |     |
     * -------
     */

    //Dimension Setters
    public void setSlope(Type type, int rise, int run)
    {
        this.type = type;

        @fixed int f_rise = fixed(rise);
        @fixed int f_run = fixed(run);

        //float hyp = (float)Math.sqrt((run * run) + (rise * rise));
        @fixed int f_hyp = f_length(f_run, f_rise);
        int w_h = height;

        switch(type)
        {
            case LEFT:
            w_h = width;
            case UP:
            {
                //RISE over RUN.
                this.f_XYtoZ_slope = f_div(f_rise, f_run);
                this.f_ZtoXY_slope = f_div(f_run, f_rise);
 
                //Convert slopes to directions.
                this.f_normXY = -f_div(f_rise, f_hyp);
                this.f_normZ = f_div(f_run, f_hyp);
 
                //Set X / Y intercept and zSlope_Bottom.
                @fixed int f_slopeDepth = f_mul( f_XYtoZ_slope, fixed(w_h) );
                @fixed int f_depth = fixed(depth);
                if(f_slopeDepth < f_depth)
                {
                    this.f_zSlope_Bottom = f_depth - f_slopeDepth;
                    this.f_zIntercept = f_zSlope_Bottom;
                }
                else
                {
                    this.f_zSlope_Bottom = 0;
                    this.f_zIntercept = 0;
                }
            }
            break;

            case RIGHT:
            w_h = width;
            case DOWN:
            {
                //RISE over RUN.
                this.f_XYtoZ_slope = -f_div(f_rise, f_run);
                this.f_ZtoXY_slope = -f_div(f_run, f_rise);
 
                //Convert slopes to directions.
                this.f_normXY = f_div(f_rise, f_hyp);
                this.f_normZ = f_div(f_run, f_hyp);
 
                //Set X / Y intercept and zSlope_Bottom.
                @fixed int f_slopeDepth = f_mul( -f_XYtoZ_slope, fixed(w_h) );
                @fixed int f_depth = fixed(depth);
                if(f_slopeDepth < f_depth)
                {
                    this.f_zSlope_Bottom = f_depth - f_slopeDepth;
                    this.f_zIntercept = f_depth;
                }
                else
                {
                    this.f_zSlope_Bottom = 0;
                    this.f_zIntercept = f_slopeDepth;
                }
            }
            break;
        }
    }
    public void setSlope(Type type)
    {
        this.type = type;

        int w_h = height;

        switch(type)
        {
            case LEFT:
            w_h = width;
            case UP:
            {
                //RISE over RUN.
                if(this.f_XYtoZ_slope < 0)
                {
                    this.f_XYtoZ_slope = -this.f_XYtoZ_slope;
                    this.f_ZtoXY_slope = -this.f_ZtoXY_slope;
                    this.f_normXY = -this.f_normXY;
                }
 
                //Set X / Y intercept and zSlope_Bottom.
                @fixed int f_slopeDepth = f_mul( f_XYtoZ_slope, fixed(w_h) );
                @fixed int f_depth = fixed(depth);
                if(f_slopeDepth < f_depth)
                {
                    this.f_zSlope_Bottom = f_depth - f_slopeDepth;
                    this.f_zIntercept = f_zSlope_Bottom;
                }
                else
                {
                    this.f_zSlope_Bottom = 0;
                    this.f_zIntercept = 0;
                }
            }
            break;

            case RIGHT:
            w_h = width;
            case DOWN:
            {
                //RISE over RUN.
                if(this.f_XYtoZ_slope > 0)
                {
                    this.f_XYtoZ_slope = -this.f_XYtoZ_slope;
                    this.f_ZtoXY_slope = -this.f_ZtoXY_slope;
                    this.f_normXY = -this.f_normXY;
                }
 
                //Set X / Y intercept and zSlope_Bottom.
                @fixed int f_slopeDepth = f_mul( -f_XYtoZ_slope, fixed(w_h) );
                @fixed int f_depth = fixed(depth);
                if(f_slopeDepth < f_depth)
                {
                    this.f_zSlope_Bottom = f_depth - f_slopeDepth;
                    this.f_zIntercept = f_depth;
                }
                else
                {
                    this.f_zSlope_Bottom = 0;
                    this.f_zIntercept = f_slopeDepth;
                }
            }
            break;
        }
    }
    public void setSlope(int width, int height, int depth, Type type)
    {
        this.width = width;
        this.height = height;
        this.depth = depth;

        setSlope(type);
    }
    public void setDimensions(int width, int height, int depth)
    {
        this.width = width;
        this.height = height;
        this.depth = depth;

        setSlope(this.type);
    }
    public void setWidth(int width){setDimensions(width, this.height, this.depth);}
    public void setHeight(int height){setDimensions(this.width, height, this.depth);}
    public void setDepth(int depth){setDimensions(this.width, this.height, depth);}

    //Collision Response stuff.
    //protected float currentCross_Z = 0.0f, currentCross_XY = 0.0f;
    protected @fixed int f_currentCross_Z = 0, f_currentCross_XY = 0;
    //
    public @fixed int f_getCurrentCross_Z(){return f_currentCross_Z;}
    public void f_setCurrentCross_Z(@fixed int currentCross_Z){this.f_currentCross_Z = currentCross_Z;}
    public @fixed int f_getCurrentCross_XY(){return f_currentCross_XY;}
    public void f_setCurrentCross_XY(@fixed int currentCross_XY){this.f_currentCross_XY = currentCross_XY;}

    @Override
    public @fixed int f_leftContact(){return (type == Type.LEFT) ? this.f_xOffset + f_currentCross_XY : f_left();}
    public @fixed int f_rightContact(){return (type == Type.RIGHT) ? this.f_xOffset + f_currentCross_XY : f_right();}
    @Override
    public @fixed int f_backContact(){return (type == Type.UP) ? this.f_yOffset + f_currentCross_XY : f_back();}
    public @fixed int f_frontContact(){return (type == Type.DOWN) ? this.f_yOffset + f_currentCross_XY : f_front();}
    @Override
    public @fixed int f_bottomContact(){return f_bottom();}
    public @fixed int f_topContact(){return this.f_zOffset + this.f_currentCross_Z;}


    @Override
    public Shape_Face[] getFaces()
    {
        //TODO Make faces
        return null;
        
        /*
        Shape_Face[] result = new Shape_Face[6];
        
        switch(type)
        {
            case LEFT:
            {
                int xy_SlopeTop = (int)((ZtoXY_slope * depth) + Z_intercept);
                if(xy_SlopeTop > width)
                {
                    //result = new Shape_Face[5];
                    xy_SlopeTop = width;
                }

                //Back Face (1). /\
                result[1] = new Shape_Face
                (
                    new Vector3f(0.0f, -1.0f, 0.0f),
                    new byte[]{5, 2, 0, 4},
                    xOffset+width, yOffset, zOffset+depth,
                    xOffset+xy_SlopeTop, yOffset, zOffset+depth,
                    xOffset, yOffset, zOffset,
                    xOffset+width, yOffset, zOffset
                );

                //Left Face (2). <
                if(zSlope_Bottom <= 0)
                {
                    result[2] = new Shape_Face
                    (
                        new Vector3f(-1.0f, 0.0f, 0.0f),
                        new byte[]{5, 3, 0, 1},
                        xOffset, yOffset, zOffset+depth,
                        xOffset+xy_SlopeTop, yOffset+height, zOffset+depth,
                        xOffset+xy_SlopeTop, yOffset+height, zOffset,
                        xOffset, yOffset, zOffset
                    );
                }
                else
                {
                    result[2] = new Shape_Face
                    (
                        new Vector3f(-1.0f, 0.0f, 0.0f),
                        new byte[]{5, 3, 0, 1},
                        xOffset, yOffset, zOffset+zSlope_Bottom,
                        xOffset, yOffset+height, zOffset+zSlope_Bottom,
                        xOffset, yOffset+height, zOffset,
                        xOffset, yOffset, zOffset
                    );
                }
                

                //Front Face (3). \/
                result[3] = new Shape_Face
                (
                    new Vector3f(0.0f, 1.0f, 0.0f),
                    new byte[]{5, 4, 0, 2},
                    xOffset+xy_SlopeTop, yOffset+height, zOffset+depth,
                    xOffset+width, yOffset+height, zOffset+depth,
                    xOffset+width, yOffset+height, zOffset,
                    xOffset, yOffset+height, zOffset
                );

                //Right Face (4). >
                result[4] = new Shape_Face
                (
                    new Vector3f(1.0f, 0.0f, 0.0f),
                    new byte[]{5, 1, 0, 3},
                    xOffset+width, yOffset+height, zOffset+depth,
                    xOffset+width, yOffset, zOffset+depth,
                    xOffset+width, yOffset, zOffset,
                    xOffset+width, yOffset+height, zOffset
                );
            }
            break;

            case RIGHT:
            {

            }
            break;

            case UP:
            break;

            case DOWN:
            break;            
        }

        //Bottom Face (0), will connnect to slope, opposite wall, and paralell walls.
        result[0] = new Shape_Face
        (
            new Vector3f(0.0f, 0.0f, -1.0f),
            new byte[]{1, 2, 3, 4},
            xOffset+width, yOffset, zOffset,
            xOffset, yOffset, zOffset,
            xOffset, yOffset+height, zOffset,
            xOffset+width, yOffset+height, zOffset
        );

        return result;
        */
        


        /*
        //Back Face (1). /\
        new Shape_Face(new Vector3f(0.0f, -1.0f, 0.0f),
            new byte[]{5, 2, 0, 4},
            xOffset+width, yOffset, zOffset+depth,
            xOffset, yOffset, zOffset+depth,
            xOffset, yOffset, zOffset,
            xOffset+width, yOffset, zOffset
        ),

        //Left Face (2). <
        new Shape_Face(new Vector3f(-1.0f, 0.0f, 0.0f),
            new byte[]{5, 3, 0, 1},
            xOffset, yOffset, zOffset+depth,
            xOffset, yOffset+height, zOffset+depth,
            xOffset, yOffset+height, zOffset,
            xOffset, yOffset, zOffset
        ),

        //Front Face (3). \/
        new Shape_Face(new Vector3f(0.0f, 1.0f, 0.0f),
            new byte[]{5, 4, 0, 2},
            xOffset, yOffset+height, zOffset+depth,
            xOffset+width, yOffset+height, zOffset+depth,
            xOffset+width, yOffset+height, zOffset,
            xOffset, yOffset+height, zOffset
        ),

        //Right Face (4). >
        new Shape_Face(new Vector3f(1.0f, 0.0f, 0.0f),
            new byte[]{5, 1, 0, 3},
            xOffset+width, yOffset+height, zOffset+depth,
            xOffset+width, yOffset, zOffset+depth,
            xOffset+width, yOffset, zOffset,
            xOffset+width, yOffset+height, zOffset
        ),
        */
    }

    /*
    public boolean intersects(float x, float y, float z, AAB_Box B, float bX, float bY, float bZ)
    {return B.intersects(bX, bY, bZ, this, x, y, z);}
    */

    /**Checks if this Slope is intersecting another Slope.*/
    public boolean intersects(float x, float y, float z, Slope_Triangle st, float bX, float bY, float bZ)
    {
        return false;
    }

    /*
    public boolean intersects(float x, float y, float z, Cylinder C, float cX, float cY, float cZ)
    {return C.intersects(cX, cY, cZ, this, x, y, z);}
    */

    @Override
    //public boolean performCollision(Vector3f thisPosition, Vector3f thisVelocity, Shape3D shape, Vector3f shapePosition)
    public boolean performCollision(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Shape3D shape, fixedVector3 f_shapePosition)
    {
        if(shape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)shape;

            //Vector3f tpv = new Vector3f(thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z);
            fixedVector3 f_tpv = new fixedVector3(f_thisPosition.x + f_thisVelocity.x, f_thisPosition.y + f_thisVelocity.y, f_thisPosition.z + f_thisVelocity.z);
           
            if
            (
                //box.intersects(shapePosition.x, shapePosition.y, shapePosition.z, 0, 0, 0,
                //this, thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z)

                box.intersects(f_shapePosition, f_thisVelocity, this, f_tpv)
            )
            {
                //box.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        else if(shape instanceof Cylinder)
        {
            Cylinder cylinder = (Cylinder)shape;

            //Vector3f tpv = new Vector3f(thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z);
            fixedVector3 f_tpv = new fixedVector3(f_thisPosition.x + f_thisVelocity.x, f_thisPosition.y + f_thisVelocity.y, f_thisPosition.z + f_thisVelocity.z);
           
            if
            (
                //cylinder.intersects(shapePosition.x, shapePosition.y, shapePosition.z, 0, 0, 0,
                //this, thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z)

                cylinder.intersects(f_shapePosition, f_thisVelocity, this, f_tpv)
            )
            {
                //cylinder.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }

        return false;
    }

    public final boolean tileCollidedBy(Shape3D itsShape, fixedVector3 f_itsPosition, fixedVector3 f_itsVelocity, fixedVector3 f_thisPosition, byte thisTileForces)
    {
        //AAB_Box
        if(itsShape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)itsShape;

            return box.intersects(f_itsPosition, f_itsVelocity, this, f_thisPosition);
        }
        //Cylinder
        else if(itsShape instanceof Cylinder)
        {
            Cylinder c = (Cylinder)itsShape;

            return c.intersects(f_itsPosition, f_itsVelocity, this, f_thisPosition);
        }
        return false;
    }

    //Returns this Slope's Type.
    public Type getType(){return type;}

    //Slope Getters.
    public @fixed int f_getXYtoZ_Slope(){return f_XYtoZ_slope;}
    public @fixed int f_getZtoXY_Slope(){return f_ZtoXY_slope;}

    //Intercepts.
    public @fixed int f_getZSlope_Bottom(){return f_zSlope_Bottom;}
    public @fixed int f_getZ_Intercept(){return f_zIntercept;}

    //Normals.
    public @fixed int f_getNormZ(){return f_normZ;}
    public @fixed int f_getNormXY(){return f_normXY;}

    /*
    @Override
    public int top()
    {
        switch(type)
        {
            case LEFT: return zOffset + (int)(XYtoZ_slope * width) + zSlope_Bottom;
            case RIGHT: return zOffset + zSlope_Bottom;
            //
            case UP: return zOffset + (int)(XYtoZ_slope * height) + zSlope_Bottom;
            case DOWN: return zOffset + zSlope_Bottom;
            //
            default: return 0;
        }
    }
    */

    /**
     * Checks if the given Shape is going into the Slope Part of this Slope.
     *
     * @param shape is the shape to check if it's colliding.
     * @param f_z is the given box's z position.
     * @param f_zVelocity is the given box's z velocity.
     * @param f_thisZ is this shape's z position.
     * @return true if the given box is about to collide with this shape from the top.
     */
    public boolean collide_Slope_Top(Shape3D shape, @fixed int f_z, @fixed int f_zVelocity, @fixed int f_thisZ)
    {
        @fixed int f_bottom = shape.f_bottomContact();

        return (f_z + f_bottom) >= (f_thisZ + this.f_zOffset + this.f_currentCross_Z) - (Level.FIXED_TILE_SIZE >> 3)
        && (f_z + f_bottom) + f_zVelocity <= (f_thisZ + this.f_zOffset + this.f_currentCross_Z);

        //boolean result = (z + bottom) >= (thisZ + this.zOffset + this.currentCross_Z) - (Level.TILE_SIZE / 8)
        //&& (z + bottom) + zVelocity <= (thisZ + this.zOffset + this.currentCross_Z);

        //System.out.println(result);

        //return result;

        //float shapeCross_Z = (z + bottom) - (thisZ + this.zOffset);
    }


    public boolean above_Slope_Top(Shape3D shape, @fixed int f_z, @fixed int f_thisZ)
    {
        @fixed int f_bottom = shape.f_bottomContact();

        return (f_z + f_bottom) >= (f_thisZ + this.f_zOffset + this.f_currentCross_Z);

        //float shapeCross_Z = (z + bottom) - (thisZ + this.zOffset);
    }
    

    /**
     * Checks if the given Shape is going into the Slope Part of this Slope Triangle from the Left.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_x is the given box's x position.
     * @param f_xVelocity is the given box's x velocity.
     * @param f_thisX is this shape's x position.
     * @return true if the given box is about to collide with this shape from the wall.
     */
    public boolean collide_Slope_Left(Shape3D shape, @fixed int f_x, @fixed int f_xVelocity, @fixed int f_thisX)
    {
        @fixed int f_right = shape.f_rightContact();

        return (f_x + f_right) <= (f_thisX + this.f_xOffset + this.f_currentCross_XY)
        && (f_x + f_right) + f_xVelocity > (f_thisX + this.f_xOffset + this.f_currentCross_XY);
    }

    /**
     * Checks if the given Shape is going into the Slope Part of this Slope Triangle from the Left.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_x is the given box's x position.
     * @param f_xVelocity is the given box's x velocity.
     * @param f_thisX is this shape's x position.
     * @return true if the given shape is about to collide with this shape from the wall.
     */
    public boolean collide_Slope_Right(Shape3D shape, @fixed int f_x, @fixed int f_xVelocity, @fixed int f_thisX)
    {
        @fixed int f_left = shape.f_leftContact();

        //System.out.println(currentCross_XY + " " + currentCross_Z);

        return (f_x + f_left) >= (f_thisX + this.f_xOffset + this.f_currentCross_XY)
        && (f_x + f_left) + f_xVelocity < (f_thisX + this.f_xOffset + this.f_currentCross_XY);
    }

    /**
     * Checks if the given Shape is going into the Slope Part of this Slope Triangle from the Front.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_y is the given box's y position.
     * @param f_yVelocity is the given box's y velocity.
     * @param f_thisY is this shape's y position.
     * @return true if the given shape is about to collide with this shape from the wall.
     */
    public boolean collide_Slope_Back(Shape3D shape, @fixed int f_y, @fixed int f_yVelocity, @fixed int f_thisY)
    {
        @fixed int f_front = shape.f_frontContact();

        return (f_y + f_front) <= (f_thisY + this.f_yOffset + f_currentCross_XY)
        && (f_y + f_front) + f_yVelocity > (f_thisY + this.f_yOffset + f_currentCross_XY);
    }

    /**
     * Checks if the given Box is going into the Slope Part of this Slope Triangle from the Front.
     *
     * @param shape is the box to check if it's colliding.
     * @param f_y is the given box's y position.
     * @param f_yVelocity is the given box's y velocity.
     * @param f_thisY is this shape's y position.
     * @return true if the given box is about to collide with this shape from the wall.
     */
    public boolean collide_Slope_Front(Shape3D shape, @fixed int f_y, @fixed int f_yVelocity, @fixed int f_thisY)
    {
        @fixed int f_back = shape.f_backContact();

        return (f_y + f_back) >= (f_thisY + this.f_yOffset + f_currentCross_XY)
        && (f_y + f_back) + f_yVelocity < (f_thisY + this.f_yOffset + f_currentCross_XY);
    }
    

    /*
     * Collision Responses
     */

    /**Puts the given Box above this slope with left slope properties.*/
    public void putOnSlope_Left(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = ((f_thisPosition.z + this.f_zOffset) + this.f_currentCross_Z) - shape.f_bottomContact();
        f_velocity.z = 0;

        if(f_velocity.x > 0 && f_currentCross_Z < fixed(depth))
        {
            //velocity.x *= f_normZ;
            f_velocity.x = f_mul(f_velocity.x, f_normZ);
        }
        //else if(velocity.x < 0){velocity.z = -Math.abs(velocity.x / normZ);}
    }

    /**Puts the given Box above this slope with right slope properties.*/
    public void putOnSlope_Right(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = ((f_thisPosition.z + this.f_zOffset) + this.f_currentCross_Z) - shape.f_bottomContact();
        f_velocity.z = 0;

        if(f_velocity.x < 0 && f_currentCross_Z < fixed(depth))
        {
            //velocity.x *= f_normZ;
            f_velocity.x = f_mul(f_velocity.x, f_normZ);
        }
        //else if(velocity.x > 0){velocity.z = -Math.abs(velocity.x / normZ);}
    }

    /**Puts the given Box above this slope with up slope properties.*/
    public void putOnSlope_Back(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = ((f_thisPosition.z + this.f_zOffset) + this.f_currentCross_Z) - shape.f_bottomContact();
        f_velocity.z = 0;

        if(f_velocity.y > 0 && f_currentCross_Z < fixed(depth))
        {
            //velocity.y *= f_normZ;
            f_velocity.y = f_mul(f_velocity.y, f_normZ);
        }
        //else if(velocity.y < 0){velocity.z = -Math.abs(velocity.y / normZ);}
    }

    /**Puts the given Box above this slope with down slope properties.*/
    public void putOnSlope_Front(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = ((f_thisPosition.z + this.f_zOffset) + this.f_currentCross_Z) - shape.f_bottomContact();
        f_velocity.z = 0;

        if(f_velocity.y < 0 && f_currentCross_Z < fixed(depth))
        {
            //velocity.y *= f_normZ;
            f_velocity.y = f_mul(f_velocity.y, f_normZ);
        }
        //else if(velocity.y > 0){velocity.z = -Math.abs(velocity.y / normZ);}
    }


    /**Puts the given Box above this slope.*/
    public void putOnSlope(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        //System.out.println(currentCross_Z);
        
        switch(type)
        {
            case LEFT:
            putOnSlope_Left(shape, entity, f_position, f_velocity, f_thisPosition);
            break;

            case RIGHT:
            putOnSlope_Right(shape, entity, f_position, f_velocity, f_thisPosition);
            break;

            case UP:
            putOnSlope_Back(shape, entity, f_position, f_velocity, f_thisPosition);
            break;

            case DOWN:
            putOnSlope_Front(shape, entity, f_position, f_velocity, f_thisPosition);
            break;

            default: break;
        }
        //this.currentCross_Z = 0.0f;
    }


    /**
     * Puts the given shape outside the side(s) it collides with.
     * 
     * @param shape the shape to affect.
     * @param entity the entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putBox_OutComposite(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        //This is the ONE check we know is possible straight off.
        if(collide_Bottom(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {
            putBottom_Contact(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }
        else if(collide_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {
            putTop_Contact(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }
        //
        //
        switch(type)
        {
            case LEFT:
            {
                /*
                if(collide_Back(shape, position.y, velocity.y, thisPosition.y))
                {putBack(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Front(shape, position.y, velocity.y, thisPosition.y))
                {putFront(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Slope_Top(shape, position.z, velocity.z, thisPosition.z)
                || collide_Slope_Left(shape, position.x, velocity.x, thisPosition.x))
                {
                    putOnSlope_Left(shape, entity, position, velocity, thisPosition);
                }
                //
                else if(collide_Right(shape, position.x, velocity.x, thisPosition.x))
                {putRight(shape, entity, position, velocity, thisPosition);}
                */


                //Top Check
                if(collide_Slope_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Left(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putBack_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putFront_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Left Check
                else if
                (
                   f_position.z + shape.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                   collide_Slope_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x)
                )
                {putOnSlope_Left(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putRight_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putLeft_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;

            case RIGHT:
            {
                /*
                if(collide_Back(shape, position.y, velocity.y, thisPosition.y))
                {putBack(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Front(shape, position.y, velocity.y, thisPosition.y))
                {putFront(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Slope_Top(shape, position.z, velocity.z, thisPosition.z)
                || collide_Slope_Right(shape, position.x, velocity.x, thisPosition.x))
                {
                    putOnSlope_Right(shape, entity, position, velocity, thisPosition);
                }
                //
                else if(collide_Left(shape, position.x, velocity.x, thisPosition.x))
                {putLeft(shape, entity, position, velocity, thisPosition);}
                */


                //Top Check
                if(collide_Slope_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Right(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putBack_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putFront_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Right Check
                else if
                (
                    f_position.z + shape.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                    collide_Slope_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x)
                )
                {putOnSlope_Right(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putLeft_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putRight_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;

            case UP:
            {
                /*
                if(collide_Left(shape, position.x, velocity.x, thisPosition.x))
                {putLeft(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Right(shape, position.x, velocity.x, thisPosition.x))
                {putRight(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Slope_Top(shape, position.z, velocity.z, thisPosition.z)
                || collide_Slope_Back(shape, position.y, velocity.y, thisPosition.y))
                {
                    putOnSlope_Back(shape, entity, position, velocity, thisPosition);
                }   
                //
                else if(collide_Front(shape, position.y, velocity.y, thisPosition.y))
                {putFront(shape, entity, position, velocity, thisPosition);}
                */


                //Top Check
                if(collide_Slope_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Back(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putLeft_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putRight_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Back Check
                else if
                (
                    f_position.z + shape.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                    collide_Slope_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y)
                )
                {putOnSlope_Back(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putFront_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putBack_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;

            case DOWN:
            {
                /*
                if(collide_Left(shape, position.x, velocity.x, thisPosition.x))
                {putLeft(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Right(shape, position.x, velocity.x, thisPosition.x))
                {putRight(shape, entity, position, velocity, thisPosition);}
                //
                else if(collide_Slope_Top(shape, position.z, velocity.z, thisPosition.z)
                || collide_Slope_Front(shape, position.y, velocity.y, thisPosition.y))
                {
                    putOnSlope_Front(shape, entity, position, velocity, thisPosition);
                }
                //
                else if(collide_Back(shape, position.y, velocity.y, thisPosition.y))
                {putBack(shape, entity, position, velocity, thisPosition);}
                */
                
                
                //Top Check
                if(collide_Slope_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Front(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putLeft_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {putRight_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Front Check
                else if
                (
                    f_position.z + shape.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                    collide_Slope_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y)
                )
                {putOnSlope_Front(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putBack_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {putFront_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;

            default: break;
        }
    }

    /**
     * Puts the given cylinder outside the side(s) it collides with.
     * 
     * @param cylinder the cylinder to affect.
     * @param entity the entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putCylinder_OutComposite(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    fixedVector3 f_thisPosition)
    {
        //System.out.println(cylinder.getUnitX() + " " + cylinder.getUnitY());

        //This is the ONE check we know is possible straight off.
        if(collide_Bottom(cylinder, f_position.z, f_velocity.z, f_thisPosition.z))
        {
            putBottom_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);
            return;
        }
        else if(collide_Top(cylinder, f_position.z, f_velocity.z, f_thisPosition.z))
        {
            putTop_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);
            return;
        }
        //
        //
        switch(type)
        {
            case LEFT:
            {
                //Top Check
                if(collide_Slope_Top(cylinder, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Left(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Left Check
                else if
                (
                    f_position.z + cylinder.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                    collide_Slope_Left(cylinder, f_position.x, f_velocity.x, f_thisPosition.x)
                )
                {putOnSlope_Left(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;
            
            case RIGHT:
            {
                //Top Check
                if(collide_Slope_Top(cylinder, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Right(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Right Check
                else if
                (
                    f_position.z + cylinder.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                    collide_Slope_Right(cylinder, f_position.x, f_velocity.x, f_thisPosition.x)
                )
                {putOnSlope_Right(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;

            case UP:
            {
                //Top Check
                if(collide_Slope_Top(cylinder, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Back(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Back Check
                else if
                (
                    f_position.z + cylinder.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                    collide_Slope_Back(cylinder, f_position.y, f_velocity.y, f_thisPosition.y)
                )
                {putOnSlope_Back(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                
            }
            break;

            case DOWN:
            {
                //Top Check
                if(collide_Slope_Top(cylinder, f_position.z, f_velocity.z, f_thisPosition.z))
                {putOnSlope_Front(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Left(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Right(cylinder, f_position.x, f_velocity.x, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                //Front Check
                else if
                (
                    f_position.z + cylinder.f_bottom() >= f_thisPosition.z + this.f_bottom() + this.f_zSlope_Bottom &&
                    collide_Slope_Front(cylinder, f_position.y, f_velocity.y, f_thisPosition.y)
                )
                {putOnSlope_Front(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Back(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //
                else if(collide_Front(cylinder, f_position.y, f_velocity.y, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                
            }
            break;
        }
    }

    /**Puts the given Shape... somewhere, around the slope.*/
    public void putOutComposite(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    fixedVector3 f_thisPosition)
    {
        if(shape instanceof Cylinder){putCylinder_OutComposite((Cylinder)shape, entity, f_position, f_velocity, f_oldPosition, f_thisPosition);}
        else{putBox_OutComposite(shape, entity, f_position, f_velocity, f_thisPosition);}
    }

    /*
     * Render Functions.
     */

    private static Vector4f lightColor = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f),
    darkColor = new Vector4f(0.0f, 0.5f, 0.0f, 1.0f),
    normalColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
    pointColor0 = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f),
    pointColor1 = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);

    @Override
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {
        int
        x = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        y = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        z = (int)(f_toFloat(f_position.z + f_zOffset) * scale),
        //
        zStart = (int)(f_toFloat(f_position.z + f_zOffset + f_zSlope_Bottom) * scale),
        xy_SlopeTop,
        //
        w = (int)(f_toFloat(f_position.x + fixed(width) + f_xOffset) * scale),
        h = (int)(f_toFloat(f_position.y + fixed(height) + f_yOffset) * scale),
        d = (int)(f_toFloat(f_position.z + fixed(depth) + f_zOffset) * scale);

        //Bottom Rect
        screen.drawLine(x, y, z, w, y, z, darkColor, true);
        screen.drawLine(x, h, z, w, h, z, darkColor, true);

        float zIntercept = f_toFloat(f_zIntercept);
        float ZtoXY_slope = f_toFloat(f_ZtoXY_slope);

        float currentCross_XY = f_toFloat(f_currentCross_XY);
        float currentCross_Z = f_toFloat(f_currentCross_Z);

        float normXY = f_toFloat(f_normXY);
        float normZ = f_toFloat(f_normZ);

        switch(type)
        {
            case LEFT:
            {
                xy_SlopeTop = (int)((depth * ZtoXY_slope) + zIntercept);
                if(xy_SlopeTop > width){xy_SlopeTop = width;}
                xy_SlopeTop = x + (int)(xy_SlopeTop * scale);
                //System.out.println(xy_SlopeTop + " " + width);

                //Slope lines.
                screen.drawLine(x, y, zStart, xy_SlopeTop, y, d, lightColor, true);
                screen.drawLine(x, h, zStart, xy_SlopeTop, h, d, lightColor, true);

                //Line Connecting Slope Lines.
                screen.drawLine(x, y, zStart, x, h, z, lightColor, true);
                screen.drawLine(xy_SlopeTop, y, d, xy_SlopeTop, h, d, lightColor, true);
                screen.drawLine(w, y, d, w, h, z, lightColor, true);

                //Normal Line
                screen.drawLine(w, y, d,
                (int)(w + (width * normXY)), y, (int)(d + (depth * normZ)),
                pointColor0, true);

                //Cross Points
                screen.drawLine((int)(x + (currentCross_XY * scale)), h, zStart,
                (int)(x + (currentCross_XY * scale)), h, d,
                pointColor1, true);

                int cz = (int)(currentCross_Z * scale);

                screen.drawLine(x, h, z + cz,
                w, h, z + cz,
                pointColor1, true);

                screen.drawPoint((int)(x + (currentCross_XY * scale)), h, z + cz, pointColor0, true);
            }
            break;

            case RIGHT:
            {
                xy_SlopeTop = (int)((depth * ZtoXY_slope) - width + zIntercept);
                if(xy_SlopeTop < 0){xy_SlopeTop = 0;}
                xy_SlopeTop = x + (int)(xy_SlopeTop * scale);

                //Slope lines.
                screen.drawLine(xy_SlopeTop, y, d, w, y, zStart, lightColor, true);
                screen.drawLine(xy_SlopeTop, h, d, w, h, zStart, lightColor, true);

                //Line Connecting Slope Lines.
                screen.drawLine(w, y, zStart, w, h, z, lightColor, true);
                screen.drawLine(xy_SlopeTop, y, d, xy_SlopeTop, h, d, lightColor, true);
                screen.drawLine(x, y, d, x, h, z, lightColor, true);


                //Normal Line
                screen.drawLine(x, y, d,
                (int)(x + (width * normXY)), y, (int)(d + (depth * normZ)),
                pointColor0, true);

                //Cross Points
                screen.drawLine((int)(x + (currentCross_XY * scale)), h, zStart,
                (int)(x + (currentCross_XY * scale)), h, d,
                pointColor1, true);

                int cz = (int)(currentCross_Z * scale);

                screen.drawLine(x, h, z + cz,
                w, h, z + cz,
                pointColor1, true);

                screen.drawPoint((int)(x + (currentCross_XY * scale)), h, z + cz, pointColor0, true);
            }
            break;

            case UP:
            {
                normalColor.set
                (
                    0.0f,
                    ((f_normXY + 1.0f) / 2.0f),
                    0.0f,
                    1.0f
                );

                //Slope Lines.
                screen.drawLine(x, y, z, x, h, d, normalColor, true);
                screen.drawLine(w, y, z, w, h, d, normalColor, true);

                //Front Lines.
                screen.drawLine(x, h, d, w, h, d, lightColor, true);
                screen.drawLine(x, h, z, x, h, d, lightColor, true);
                screen.drawLine(w, h, z, w, h, d, lightColor, true);
            }
            break;

            case DOWN:
            {
                normalColor.set
                (
                    0.0f,
                    ((normXY + 1.0f) / 2.0f),
                    0.0f,//((normZ + 1.0f) / 2.0f),
                    1.0f
                );

                //Front Lines.
                screen.drawLine(x, y, d, w, y, d, normalColor, true);
                //
                screen.drawLine(x, y, d, x, h, z, normalColor, true);
                screen.drawLine(w, y, d, w, h, z, normalColor, true);
                //
                screen.drawLine(x, h, z, w, h, z, normalColor, true);
            }
            break;

            default:
            break;
        }
    }

    /**Renders this Slope as a Tile.*/
    public void tileRender(Screen screen, float scale, @fixed int f_x, @fixed int f_y, @fixed int f_z, boolean fixed)
    {

    }
}
