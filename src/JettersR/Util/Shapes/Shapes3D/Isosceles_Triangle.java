package JettersR.Util.Shapes.Shapes3D;
/**
 * This is meant to be used as a portion of a complete Isosceles Triangle.
 * 
 * Author: Luke Sullivan
 * Last Edit: 8/9/2023
 */
import org.joml.Vector4f;

import JettersR.Level;
import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Isosceles_Triangle extends Shape_Box
{
    public static final byte
    TYPE_UL = 0b00,
    TYPE_UR = 0b01,
    TYPE_DL = 0b10,
    TYPE_DR = 0b11;
    public byte type;

    //private float XtoY_slope = 1.0f, YtoX_slope = 1.0f,
    //normX = 0.0f, normY = 0.0f;
    private @fixed int f_XtoY_slope;
    private @fixed int f_YtoX_slope;
    private @fixed int f_normX;
    private @fixed int f_normY;
    
    //private float xSlant_Left = 0.0f, xSlant_Right = 0.0f,
    //ySlant_Back = 0.0f, ySlant_Front = 0.0f,
    ////xIntercept = 0,
    //yIntercept = 0;
    private @fixed int f_xSlant_Left;
    private @fixed int f_xSlant_Right;
    private @fixed int f_ySlant_Back;
    private @fixed int f_ySlant_Front;
    private @fixed int f_yIntercept;

    //private float hyp = 0;

    /*
     * 
     *  |\
     *  |a\
     *  |  \ hyp
     * y|   \
     *  |_   \
     *  | |  b\
     *  --------
     *     x
     * 
     * sin(a) = x/hyp, sin(b) = y/hyp
     */

    /**Constructor.*/
    public Isosceles_Triangle(int width, int height, int depth, byte type, int rise, int run,
    @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset)
    {
        super(width, height, depth, f_xOffset, f_yOffset, f_zOffset);
        setSlope(type, rise, run);
    }

    /**Constructor.*/
    public Isosceles_Triangle(int width, int height, int depth, byte type, int rise, int run)
    {this(width, height, depth, type, rise, run, fixed(-width/2), fixed(-height/2), fixed(-depth/2));}

    public Isosceles_Triangle(int width, int height, int depth, byte type)
    {this(width, height, depth, type, height, width, fixed(-width/2), fixed(-height/2), fixed(-depth/2));}


    //Collision Response related stuff.
    //private float currentCross_X = 0.0f, currentCross_Y = 0.0f;
    private @fixed int f_currentCross_X = 0;
    private @fixed int f_currentCross_Y = 0;

    public @fixed int f_getCurrentCross_X(){return f_currentCross_X;}
    public @fixed int f_getCurrentCross_Y(){return f_currentCross_Y;}
    public void setCurrentCross(@fixed int f_currentCross_X, @fixed int f_currentCross_Y)
    {
        this.f_currentCross_X = f_currentCross_X;
        this.f_currentCross_Y = f_currentCross_Y;
    }

    public @fixed int f_getNormX(){return f_normX;}
    public @fixed int f_getNormY(){return f_normY;}

    //public float getHyp(){return hyp;}

    @Override
    public @fixed int f_leftContact(){return (f_normX <= 0) ? this.f_xOffset + this.f_currentCross_X : f_left();}
    public @fixed int f_rightContact(){return (f_normX > 0)  ? this.f_xOffset + this.f_currentCross_X : f_right();}
    @Override
    public @fixed int f_backContact(){return ((type & 0b10) != 0b10) ? this.f_yOffset + this.f_currentCross_Y : f_back();}
    public @fixed int f_frontContact(){return ((type & 0b10) == 0b10) ? this.f_yOffset + this.f_currentCross_Y : f_front();}
    @Override
    public @fixed int f_bottomContact(){return f_bottom();}
    public @fixed int f_topContact(){return f_top();}

    //Dimension Setters
    public void setSlope(byte type, int rise, int run)
    {
        this.type = type;
        boolean down = (type & 0b10) == 0b10;

        @fixed int f_rise = fixed(rise);
        @fixed int f_run = fixed(run);

        //float hyp = (float)Math.sqrt((run * run) + (rise * rise));
        //@fixed int f_hyp = f_length(f_run, f_rise);
        @fixed int f_hyp = f_sqrt( fixed((run * run) + (rise * rise)) );

        switch(type)
        {
            case TYPE_DR:
            case TYPE_UL:
            {
                //RISE over RUN.
                this.f_XtoY_slope = -f_div(f_rise, f_run);
                this.f_YtoX_slope = -f_div(f_run, f_rise);

                //Calculate the direction the slope face is facing.
                this.f_normX = f_divRound_Precision(f_rise, f_hyp);
                this.f_normY = f_divRound_Precision(f_run, f_hyp);
                if(!down){f_normX = -f_normX; f_normY = -f_normY;}
                //Temporaily used as 16-bit precision values to round to a more accurate normal.

                //Set Intercepts.
                @fixed int f_slopeHeight = -f_XtoY_slope * width;
                @fixed int f_height = fixed(height);
                if(f_slopeHeight <= f_height)
                {
                    this.f_ySlant_Back = (down) ? f_height - f_slopeHeight : 0;
                    this.f_ySlant_Front = (down) ? f_height : f_slopeHeight;
                    //
                    this.f_xSlant_Left = 0;
                    this.f_xSlant_Right = fixed(width);
                    //
                    this.f_yIntercept = (down) ? f_height : f_slopeHeight;
                    ///this.xIntercept = -YtoX_slope * yIntercept;
                }
                else
                {
                    this.f_ySlant_Back = 0;
                    this.f_ySlant_Front = f_height;
                    //
                    this.f_xSlant_Left = (down) ? f_YtoX_slope * -height : 0;
                    this.f_xSlant_Right = (down) ? fixed(width) : f_YtoX_slope * -height;
                    //
                    this.f_yIntercept = (down) ? f_slopeHeight : f_height;
                    //this.xIntercept = (down) ? YtoX_slope * (slopeHeight - height) : 0;    
                }
            }
            break;

            case TYPE_DL:
            case TYPE_UR:
            {
                //RISE over RUN.
                this.f_XtoY_slope = f_div(f_rise, f_run);
                this.f_YtoX_slope = f_div(f_run, f_rise);

                //Convert slopes to directions.
                this.f_normX = f_divRound_Precision(f_rise, f_hyp) * ((down) ? -1 : 1);
                this.f_normY = f_divRound_Precision(f_run, f_hyp) * ((!down) ? -1 : 1);

                //Set Intercepts.
                @fixed int f_slopeHeight = f_XtoY_slope * width;
                @fixed int f_height = fixed(height);
                if(f_slopeHeight <= f_height)
                {
                    this.f_ySlant_Back = (down) ? f_height - f_slopeHeight : 0;
                    this.f_ySlant_Front = (down) ? f_height : f_slopeHeight;
                    //
                    this.f_xSlant_Left = 0;
                    this.f_xSlant_Right = fixed(width);
                    //
                    this.f_yIntercept = this.f_ySlant_Back;
                    //this.xIntercept = YtoX_slope * -yIntercept;
                }
                else
                {
                    this.f_ySlant_Back = 0;
                    this.f_ySlant_Front = f_height;
                    //
                    this.f_xSlant_Left = (down) ? 0 : f_YtoX_slope * height;
                    this.f_xSlant_Right = (down) ? f_YtoX_slope * height : fixed(width);

                    //this.xSlant_Left = (down) ? 0 : YtoX_slope * height;
                    //this.xSlant_Right = (down) ? YtoX_slope * height : width;
                    //
                    this.f_yIntercept = (down) ? 0 : f_height - f_slopeHeight;
                    //this.xIntercept = YtoX_slope * -yIntercept;
                }
            }
            break;

            default: //(int)(-slope * depth)... Diagonal Slopes?
            break;
        }
    }
    public void setSlope(byte type){setSlope(type, f_XtoY_slope, f_YtoX_slope);}
    public void setSlope(int width, int height, int depth, byte type)
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

    //Slope stuff.
    public @fixed int f_get_XtoY_Slope(){return this.f_XtoY_slope;}
    public @fixed int f_get_YtoX_Slope(){return this.f_YtoX_slope;}
    public @fixed int f_getYIntercept(){return this.f_yIntercept;}
    //public float getXIntercept(){return this.xIntercept;}
    public byte getType(){return type;}
    public boolean isDown(){return ((type & 0b10) == 0b10);}
    public boolean isRight(){return ((type & 0b01) == 0b01);}

    public @fixed int f_getXSlant_Left(){return this.f_xSlant_Left;}
    public @fixed int f_getXSlant_Right(){return this.f_xSlant_Right;}
    public @fixed int f_getYSlant_Back(){return this.f_ySlant_Back;}
    public @fixed int f_getYSlant_Front(){return this.f_ySlant_Front;}

    //Start/End X/Y.
    public @fixed int f_getStartX()
    {
        //float sx = (0 - yIntercept) * YtoX_slope;
        //float sx = 0;
        //return sx;

        return f_xSlant_Left;
    }
    public @fixed int f_getStartY()
    {
        //float sy = ((XtoY_slope * 0) + yIntercept);
        //return yIntercept;

        return (f_XtoY_slope < 0) ? f_ySlant_Front : f_ySlant_Back;
    }
    //
    public @fixed int f_getEndX()
    {
        //float ex = ((XtoY_slope * height)) * YtoX_slope;
        //return ex;

        return f_xSlant_Right;
    }
    public @fixed int f_getEndY()
    {
        //float ey = ((XtoY_slope * width) + yIntercept);
        //return ey;

        return (f_XtoY_slope < 0) ? f_ySlant_Back : f_ySlant_Front;
    }

    //public int getStartY(){return (int)(yIntercept);}
    //public int getEndY(){return (int)((XtoY_slope * width) + yIntercept);}
    //public int getStartX(){return (int)(xIntercept);}
    //public int getEndX(){return (int)((YtoX_slope * height) + xIntercept);}


    @Override
    public Shape_Face[] getFaces()
    {
        //TODO
        return null;
    }


    @Override
    public boolean performCollision(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Shape3D shape, fixedVector3 f_shapePosition)
    {
        //AAB_Box
        if(shape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)shape;
           
            //Vector3f tpv = new Vector3f(thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z);
            fixedVector3 f_tpv = new fixedVector3(f_thisPosition.x + f_thisVelocity.x, f_thisPosition.y + f_thisVelocity.y, f_thisPosition.z + f_thisVelocity.z);
            
            if
            (
                box.intersects(f_shapePosition, f_thisVelocity, this, f_tpv, (byte)0b11)
            )
            {
                //box.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        //Cylinder
        else if(shape instanceof Cylinder)
        {
            Cylinder cylinder = (Cylinder)shape;
           
            //Vector3f tpv = new Vector3f(thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z);
            fixedVector3 f_tpv = new fixedVector3(f_thisPosition.x + f_thisVelocity.x, f_thisPosition.y + f_thisVelocity.y, f_thisPosition.z + f_thisVelocity.z);
            
            if
            (
                cylinder.intersects(f_shapePosition, f_thisVelocity, this, f_tpv, (byte)0b11)
            )
            {
                //cylinder.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }

        //No collision was made.
        return false;
    }

    public final boolean tileCollidedBy(Shape3D itsShape, fixedVector3 f_itsPosition, fixedVector3 f_itsVelocity, fixedVector3 f_thisPosition, byte thisTileForces)
    {
        //Using the given tileData, determine which corners of the slant to exclude from the collision check.
        byte isoEnds = 0b11;
        switch(type)
        {
            case TYPE_UL:
            {
                isoEnds = (byte)
                (
                    ((thisTileForces & 0b000100) >> 1) |//Up force = End
                    (thisTileForces & 0b000001)//Left force = Start
                );
            }
            break;

            case TYPE_UR:
            {
                isoEnds = (byte)
                (
                    (thisTileForces & 0b000010) |//Right force = End
                    ((thisTileForces & 0b000100) >> 2)//Up force = Start
                );
            }
            break;

            case TYPE_DL:
            {
                isoEnds = (byte)
                (
                    ((thisTileForces & 0b001000) >> 2) |//Down force = End
                    (thisTileForces & 0b000001)//Left force = Start
                );
            }
            break;

            case TYPE_DR:
            {
                isoEnds = (byte)
                (
                    (thisTileForces & 0b000010) |//Right force = End
                    ((thisTileForces & 0b001000) >> 3)//Down force = Start
                );
            }
            break;
        }


        //AAB_Box
        if(itsShape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)itsShape;

            return box.intersects(f_itsPosition, f_itsVelocity, this, f_thisPosition, isoEnds);
        }
        //Cylinder
        else if(itsShape instanceof Cylinder)
        {
            Cylinder c = (Cylinder)itsShape;

            return c.intersects(f_itsPosition, f_itsVelocity, this, f_thisPosition, isoEnds);
        }
        return false;
    }


    /**
     * Checks if the given Shape is going into the Slope Part of this Isosceles Triangle from the Left.
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

        return (f_x + f_right) <= (f_thisX + this.f_xOffset + this.f_currentCross_X) + (Level.FIXED_TILE_SIZE >> 3)
        ;//&& (x + right) + xVelocity > (thisX + this.xOffset + currentCross_X);
    }

    /**
     * Checks if the given Shape is going into the Slope Part of this Isosceles Triangle from the Left.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_x is the given box's x position.
     * @param f_xVelocity is the given box's x velocity.
     * @param f_thisX is this shape's x position.
     * @return true if the given box is about to collide with this shape from the wall.
     */
    public boolean collide_Slope_Right(Shape3D shape, @fixed int f_x, @fixed int f_xVelocity, @fixed int f_thisX)
    {
        @fixed int f_left = shape.f_leftContact();

        //return (x + left) >= (thisX + this.xOffset) + (currentCross_X + xVelocity)
        //&& (x + left) + xVelocity < (thisX + this.xOffset) + currentCross_X;

        return (f_x + f_left) >= (f_thisX + this.f_xOffset + this.f_currentCross_X) - (Level.FIXED_TILE_SIZE >> 3)
        ;//&& (f_x + f_left) + f_xVelocity < (f_thisX + this.f_xOffset + this.f_currentCross_X);
    }

    /**
     * Checks if the given Shape is going into the Slope Part of this Isosceles Triangle from the Front.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_y is the given box's y position.
     * @param f_yVelocity is the given box's y velocity.
     * @param f_thisY is this shape's y position.
     * @return true if the given box is about to collide with this shape from the wall.
     */
    public boolean collide_Slope_Back(Shape3D shape, @fixed int f_y, @fixed int f_yVelocity, @fixed int f_thisY)
    {
        @fixed int f_front = shape.f_frontContact();

        return (f_y + f_front) <= (f_thisY + this.f_yOffset + this.f_currentCross_Y) + (Level.FIXED_TILE_SIZE >> 3)
        ;//&& (y + front) + yVelocity > (thisY + this.yOffset + currentCross_Y);
    }

    /**
     * Checks if the given Box is going into the Slope Part of this Isosceles Triangle from the Front.
     *
     * @param shape is the shape to check if it's colliding.
     * @param f_y is the given box's y position.
     * @param f_yVelocity is the given box's y velocity.
     * @param f_thisY is this shape's y position.
     * @return true if the given box is about to collide with this shape from the wall.
     */
    public boolean collide_Slope_Front(Shape3D shape, @fixed int f_y, @fixed int f_yVelocity, @fixed int f_thisY)
    {
        @fixed int f_back = shape.f_backContact();

        return (f_y + f_back) >= (f_thisY + this.f_yOffset + this.f_currentCross_Y) - (Level.FIXED_TILE_SIZE >> 3)
        ;//&& (f_y + f_back) + f_yVelocity < (f_thisY + this.f_yOffset + this.f_currentCross_Y);
    }


    /*
     * Collision Responses
     */

    @Override
    public final void putLeft(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Left corner. Will pass if right-facing.
        if((type & 0b01) == 0b01 || collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
        {
            super.putLeft(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }

        //UL Slant.
        if(type == TYPE_UL){putSlant_UL(shape, entity, f_position, f_velocity, f_thisPosition);}

        //DL
        else if(type == TYPE_DL){putSlant_DL(shape, entity, f_position, f_velocity, f_thisPosition);}
    }

    @Override
    public final void putRight(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Right corner. Will pass if left-facing.
        if((type & 0b01) != 0b01 || collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
        {
            super.putRight(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }

        //UR Slant.
        if(type == TYPE_UR){putSlant_UR(shape, entity, f_position, f_velocity, f_thisPosition);}

        //DR
        else if(type == TYPE_DR){putSlant_DR(shape, entity, f_position, f_velocity, f_thisPosition);}
    }

    @Override
    public final void putBack(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Back corner. Will pass if front-facing.
        if((type & 0b10) == 0b10 || collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
        {
            super.putBack(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }

        //UL Slant.
        if(type == TYPE_UL){putSlant_UL(shape, entity, f_position, f_velocity, f_thisPosition);}

        //UR
        else if(type == TYPE_UR){putSlant_UR(shape, entity, f_position, f_velocity, f_thisPosition);}
    }

    @Override
    public final void putFront(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Front corner. Will pass if back-facing.
        if((type & 0b10) != 0b10 || collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
        {
            super.putFront(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }

        //DL Slant.
        if(type == TYPE_DL){putSlant_DL(shape, entity, f_position, f_velocity, f_thisPosition);}

        //DR
        else if(type == TYPE_DR){putSlant_DR(shape, entity, f_position, f_velocity, f_thisPosition);}
    }
    
 
    /**Puts the given Shape against the slope portion of this Triangle, assuming this is an UpLeft facing slope.*/
    public void putSlant_UL(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Set position.
        f_position.y = (f_thisPosition.y + this.f_yOffset + f_currentCross_Y) - (shape.f_frontContact());
        f_position.x = (f_thisPosition.x + this.f_xOffset + f_currentCross_X) - (shape.f_rightContact());

        //Slide.
        lineSlide(f_velocity);
    }

    /**Puts the given Shape against the slope portion of this Triangle, assuming this is an UpRight facing slope.*/
    public void putSlant_UR(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Set position.
        f_position.y = (f_thisPosition.y + this.f_yOffset + f_currentCross_Y) - (shape.f_frontContact());
        f_position.x = (f_thisPosition.x + this.f_xOffset + f_currentCross_X) - (shape.f_leftContact());

        //Slide.
        lineSlide(f_velocity);

        //f_velocity.x = 0;
        //f_velocity.y = 0;
    }

    /**Puts the given Shape against the slope portion of this Triangle, assuming this is a DownLeft facing slope.*/
    public void putSlant_DL(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Set position.
        f_position.y = (f_thisPosition.y + this.f_yOffset + f_currentCross_Y) - (shape.f_backContact());
        f_position.x = (f_thisPosition.x + this.f_xOffset + f_currentCross_X) - (shape.f_rightContact());

        //Slide.
        lineSlide(f_velocity);
    }

    /**Puts the given Shape against the slope portion of this Triangle, assuming this is a DownRight facing slope.*/
    public void putSlant_DR(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //System.out.println("DR");

        //Set position.
        f_position.y = (f_thisPosition.y + this.f_yOffset + f_currentCross_Y) - (shape.f_backContact());
        f_position.x = (f_thisPosition.x + this.f_xOffset + f_currentCross_X) - (shape.f_leftContact());

        //Slide.
        lineSlide(f_velocity);

        //f_velocity.x = 0;
        //f_velocity.y = 0;
    }

    private void lineSlide(fixedVector3 f_velocity)
    {
        //Cache initial velocity.
        @fixed int f_vx = f_velocity.x;
        @fixed int f_vy = f_velocity.y;

        //Get velocity magnitude.
        //float velocityMagnitude = (float)Math.sqrt((vx * vx) + (vy * vy));
        @fixed int f_velocityMagnitude = f_length(f_vx, f_vy);
        //f_print("fv", f_velocityMagnitude);

        //Get velocity normal.
        boolean zero = (f_velocityMagnitude == 0);
        @fixed int f_yDir = (zero) ? 0 : f_divRound_Precision(f_vy , f_velocityMagnitude);
        @fixed int f_xDir = (zero) ? 0 : f_divRound_Precision(f_vx , f_velocityMagnitude);

        //Calculate dot product of velocity direction and slant direction.
        //
        //1.0 = Velocity is same direction as slant.
        //-1.0 = Velocity is opposite direction as slant.
        @fixed int f_dotP = f_mul(f_xDir, f_normX) + f_mul(f_yDir, f_normY);
        //f_print("fDOT", f_dotP);

        //slideVelocity = initialVelocity - (Vector.DotProduct(plane.Normal, initialVelocity) * plane.Normal)
        f_velocity.x = f_vx - f_mul(f_dotP, f_mul(f_normX, f_velocityMagnitude) );
        f_velocity.y = f_vy - f_mul(f_dotP, f_mul(f_normY, f_velocityMagnitude) );
    }


    /**Puts the given Shape against the slope portion of this Triangle.*/
    public void putSlant(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        switch(type)
        {
            case TYPE_UL:
            putSlant_UL(shape, entity, f_position, f_velocity, f_thisPosition);
            break;

            case TYPE_UR:
            putSlant_UR(shape, entity, f_position, f_velocity, f_thisPosition);
            break;

            case TYPE_DL:
            putSlant_DL(shape, entity, f_position, f_velocity, f_thisPosition);
            break;

            case TYPE_DR:
            putSlant_DR(shape, entity, f_position, f_velocity, f_thisPosition);
            break;
        }
    }

    public void putHorizontal(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, fixedVector3 f_thisPosition)
    {
        @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

        //Left slant
        if((type & 0b01) != 0b01)
        {
            //Right
            if(collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {super.putRight(shape, entity, f_position, f_velocity, f_thisPosition);}
            //Left
            else if(collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {super.putLeft(shape, entity, f_position, f_velocity, f_thisPosition);}

            //Slant
            else if((type & 0b10) != 0b10){putSlant_UL(shape, entity, f_position, f_velocity, f_thisPosition);}
            else{putSlant_DL(shape, entity, f_position, f_velocity, f_thisPosition);}
        }
        //Right slant
        else
        {
            //Left
            if(collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {super.putLeft(shape, entity, f_position, f_velocity, f_thisPosition);}
            //Right
            else if(collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {super.putRight(shape, entity, f_position, f_velocity, f_thisPosition);}

            //Slant
            else if((type & 0b10) != 0b10){putSlant_UR(shape, entity, f_position, f_velocity, f_thisPosition);}
            else{putSlant_DR(shape, entity, f_position, f_velocity, f_thisPosition);}
        }
    }

    public void putVertical(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, fixedVector3 f_thisPosition)
    {
        @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

        //Back slant
        if((type & 0b10) != 0b10)
        {
            //Front
            if(collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {super.putFront(shape, entity, f_position, f_velocity, f_thisPosition);}
            //Back
            else if(collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {super.putBack(shape, entity, f_position, f_velocity, f_thisPosition);}

            //Slant
            else if((type & 0b01) != 0b01){putSlant_UL(shape, entity, f_position, f_velocity, f_thisPosition);}
            else{putSlant_UR(shape, entity, f_position, f_velocity, f_thisPosition);}
        }
        //Front slant
        else
        {
            //Back
            if(collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {super.putBack(shape, entity, f_position, f_velocity, f_thisPosition);}
            //Front
            else if(collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {super.putFront(shape, entity, f_position, f_velocity, f_thisPosition);}

            //Slant
            else if((type & 0b01) != 0b01){putSlant_DL(shape, entity, f_position, f_velocity, f_thisPosition);}
            else{putSlant_DR(shape, entity, f_position, f_velocity, f_thisPosition);}
        }
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
    public void putBox_OutComposite(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Bottom
        if(collide_Bottom(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {
            putBottom_Contact(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }
        //Top
        else if(collide_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {
            putTop_Contact(shape, entity, f_position, f_velocity, f_thisPosition);
            return;
        }

        //
        boolean sideCollide = false;

        switch(type)
        {
            case TYPE_UL://UL
            {
                //Front
                if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putFront(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Back
                else if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putBack(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }

                //Right
                if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putRight(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Left
                else if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putLeft(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }

                if(!sideCollide &&
                    (collide_Slope_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y)
                    || collide_Slope_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                )
                {putSlant_UL(shape, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;


            case TYPE_UR://UR
            {
                //Front
                if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putFront(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Back
                else if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putBack(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                
                //Left
                if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putLeft(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Right
                else if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putRight(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }

                if(!sideCollide &&
                    (collide_Slope_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y)
                    || collide_Slope_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                )
                {putSlant_UR(shape, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;


            case TYPE_DL://DL
            {
                //Back
                if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putBack(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Front
                else if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putFront(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }

                //Right
                if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putRight(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Left
                else if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putLeft(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }

                if
                (
                    !sideCollide &&
                    (collide_Slope_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y) ||
                    collide_Slope_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x)
                ))
                {putSlant_DL(shape, entity, f_position, f_velocity, f_thisPosition);} 
            }
            break;


            case TYPE_DR://DR
            {
                //Back
                if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putBack(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Front
                else if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
                {
                    putFront(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }

                //Left
                if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putLeft(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }
                //Right
                else if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                {
                    putRight(shape, entity, f_position, f_velocity, f_thisPosition);
                    sideCollide = true;
                }

                if(!sideCollide &&
                    (collide_Slope_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y)
                    || collide_Slope_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
                )
                {putSlant_DR(shape, entity, f_position, f_velocity, f_thisPosition);}  
            }
            break;
        }
    }
    //
    //
    public void putCylinder_OutComposite(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, fixedVector3 f_thisPosition)
    {
        @fixed int f_velZX = f_velocity.z + (f_position.z - f_oldPosition.z);

        //Bottom
        if(collide_Bottom(cylinder, f_oldPosition.z, f_velZX, f_thisPosition.z))
        {
            putBottom_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);
            return;
        }
        //Top
        else if(collide_Top(cylinder, f_oldPosition.z, f_velZX, f_thisPosition.z))
        {
            putTop_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);
            return;
        }

        f_velZX = f_velocity.x + (f_position.x - f_oldPosition.x);
        @fixed int f_velY = f_velocity.y + (f_position.y - f_oldPosition.y);

        //
        switch(type)
        {
            case TYPE_UL://UpLeft
            {
                //Right
                if(collide_Right(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Left
                else if(collide_Left(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition, f_ySlant_Front, fixed(this.height));}

                //Front
                else if(collide_Front(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Back
                else if(collide_Back(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition, f_xSlant_Right, fixed(this.width));}

                //Slant
                else if
                (
                    collide_Slope_Back(cylinder, f_oldPosition.y, f_velocity.y, f_thisPosition.y) &&
                    collide_Slope_Left(cylinder, f_oldPosition.x, f_velocity.x, f_thisPosition.x)
                )
                {putSlant_UL(cylinder, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;


            case TYPE_UR://UpRight
            {
                //Left
                if(collide_Left(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Right
                else if(collide_Right(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition, f_ySlant_Front, fixed(this.height));}

                //Front
                else if(collide_Front(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Back
                else if(collide_Back(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, f_xSlant_Left); }

                //Slant
                else if
                (
                    collide_Slope_Back(cylinder, f_oldPosition.y, f_velocity.y, f_thisPosition.y) &&
                    collide_Slope_Right(cylinder, f_oldPosition.x, f_velocity.x, f_thisPosition.x)
                )
                {putSlant_UR(cylinder, entity, f_position, f_velocity, f_thisPosition);}
            }
            break;


            case TYPE_DL://DownLeft
            {
                
                //Right
                if(collide_Right(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Left
                else if(collide_Left(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, f_ySlant_Back);}

                //Back
                else if(collide_Back(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Front
                else if(collide_Front(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition, f_xSlant_Right, fixed(this.width));}

                //Slant
                else if
                (
                    collide_Slope_Front(cylinder, f_oldPosition.y, f_velocity.y, f_thisPosition.y) &&
                    collide_Slope_Left(cylinder, f_oldPosition.x, f_velocity.x, f_thisPosition.x)
                )
                {putSlant_DL(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                


                /*
                //Back
                if(collide_Back(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {
                    putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);
                    return;
                }
                //Right
                else if(collide_Right(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {
                    putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);
                    return;
                }

                //Left
                if(collide_Left(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, f_ySlant_Back);}
                //Front
                else if(collide_Front(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition, f_xSlant_Right, fixed(this.width));}

                //Slant
                if
                (
                    collide_Slope_Front(cylinder, f_oldPosition.y, f_velocity.y, f_thisPosition.y) &&
                    collide_Slope_Left(cylinder, f_oldPosition.x, f_velocity.x, f_thisPosition.x)
                )
                {putSlant_DL(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                */
            }
            break;


            case TYPE_DR://DownRight
            {
                //Left
                if(collide_Left(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Right
                else if(collide_Right(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
                {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, f_ySlant_Back);}

                //Back
                else if(collide_Back(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);}
                //Front
                else if(collide_Front(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
                {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, f_xSlant_Left);}

                //Slant
                else if
                (
                    collide_Slope_Front(cylinder, f_oldPosition.y, f_velocity.y, f_thisPosition.y) &&
                    collide_Slope_Right(cylinder, f_oldPosition.x, f_velocity.x, f_thisPosition.x)
                )
                {
                    System.out.println("Slant DR");
                    putSlant_DR(cylinder, entity, f_position, f_velocity, f_thisPosition);
                }
            }
            break;
        }
        
    }

    /**Puts the given Shape outside of this Triangle.*/
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
    pointColor0 = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f),
    pointColor1 = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f),
    pointColor2 = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f),
    pointColor3 = new Vector4f(0.5f, 0.0f, 1.0f, 1.0f);

    @Override
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {
        //System.out.println("yi: " + yIntercept);
        
        int
        x = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        y = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        z = (int)(f_toFloat(f_position.z + f_zOffset) * scale),
        //
        w = (int)(f_toFloat(f_position.x + fixed(width) + f_xOffset) * scale),
        h = (int)(f_toFloat(f_position.y + fixed(height) + f_yOffset) * scale),
        d = (int)(f_toFloat(f_position.z + fixed(depth) + f_zOffset) * scale),
        //
        startY = (int)(f_toFloat(f_getStartY()) * scale), endY = (int)(f_toFloat(f_getEndY()) * scale),
        startX = (int)(f_toFloat(f_getStartX()) * scale), endX = (int)(f_toFloat(f_getEndX()) * scale);
        //startY = (int)(ySlant_Back * scale), endY = (int)(ySlant_Front * scale),
        //startX = (int)(xSlant_Left * scale), endX = (int)(xSlant_Right * scale);

        if(isDown())
        {
            //Bottom Back line.
            screen.drawLine(x, y, z, w, y, z, darkColor, true);

            //Bottom Slope line.
            screen.drawLine(x + startX, y + startY, z, x + endX, y + endY, z, darkColor, true);

            //Lines connecting Back-Top to Bottom-Front.
            screen.drawLine(x, y, d, x, y + startY, z, lightColor, true);
            screen.drawLine(w, y, d, w, y + endY, z, lightColor, true);

            //Top Back line.
            screen.drawLine(x, y, d, w, y, d, lightColor, true);

            //Top Slope line.
            screen.drawLine(x + startX, y + startY, d, x + endX, y + endY, d, lightColor, true);
        }
        else
        {
            //Bottom Slope Line
            screen.drawLine(x + startX, y + startY, z, x + endX, y + endY, z, darkColor, true);

            //Bottom Front Line
            screen.drawLine(x, h, z, w, h, z, darkColor, true);

            //Top Slope Line
            screen.drawLine(x + startX, y + startY, d, x + endX, y + endY, d, lightColor, true);

            //Top Front Line
            screen.drawLine(x, h, d, w, h, d, lightColor, true);

            //Lines Connecting Back-Top to Front-Bottom
            screen.drawLine(x, y + startY, d, x, h, d, lightColor, true);
            screen.drawLine(w, y + endY, d, w, h, d, lightColor, true);
        }

        float normX = f_toFloat(f_normX);
        float normY = f_toFloat(f_normY);

        //Normal Line
        screen.drawLine(w, y, d,
        (int)(w + (width * normX)), (int)((y) + (height * normY)), d,
        pointColor0, true);

        screen.drawLine(w, y, d,
        (int)(w + (width * normY)), (int)((y) + (height * -normX)), d,
        pointColor3, true);

        float currentCross_X = f_toFloat(f_currentCross_X);
        float currentCross_Y = f_toFloat(f_currentCross_Y);

        //Cross Points
        screen.drawLine((int)(x + (currentCross_X * scale)), y, d,
        (int)(x + (currentCross_X * scale)), h, d,
        pointColor1, true);

        screen.drawLine(w, (int)(y + (currentCross_Y * scale)), d,
        x, (int)(y + (currentCross_Y * scale)), d,
        pointColor1, true);

        screen.drawPoint(x + startX, y + startY, d, pointColor2, true);

        screen.drawPoint(x + endX, y + endY, d, pointColor3, true);

        //screen.drawPoint((int)(xa + (currentCross_X * scale)), (int)((ya-d) + (currentCross_Y * scale)), pointColor0, true);
    }

    @Override
    /**Renders this Triangle as a Tile.*/
    public void tileRender(Screen screen, float scale, @fixed int f_x, @fixed int f_y, @fixed int f_z, boolean fixed)
    {

    }
}
