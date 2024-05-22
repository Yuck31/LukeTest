package JettersR.Util.Shapes.Shapes3D;
/**
 * Spheres are a bit outside of my scope, so a cylinder is really all I need.
 * 
 * Author: Luke Sullivan
 * Last Edit: 5/2/2023
 */
//import org.joml.Vector3f;
import org.joml.Vector4f;

//import JettersR.Entities.CollisionObject;
import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.Fixed;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Cylinder extends Shape3D
{
    //Radius of the Cylinder.
    protected @fixed int f_radius = 0;

    //Depth of Cylinder.
    protected int depth = 0;

    /**Constructor.*/
    public Cylinder(@fixed int f_radius, int depth, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset)
    {
        super(f_xOffset, f_yOffset, f_zOffset);
        this.f_radius = f_radius;
        this.depth = depth;
    }

    /**Constructor.*/
    public Cylinder(@fixed int f_radius, int depth)
    {this(f_radius, depth, 0, 0, fixed(-depth) / 2);}

    protected @fixed int
    f_unitX, f_unitY,
    f_pointX, f_pointY;
    //overlap;
    public void f_setUnitVector(@fixed int f_unitX, @fixed int f_unitY,
    @fixed int f_px, @fixed int f_py)
    //@fixed int f_overlap)
    {
        this.f_unitX = f_unitX; this.f_unitY = f_unitY;
        this.f_pointX = f_px; this.f_pointY = f_py;
        //this.overlap = overlap;
    }
    public @fixed int f_getUnitX(){return this.f_unitX;}
    public void f_setUnitX(@fixed int f_unitX){this.f_unitX = f_unitX;}
    public void f_setUnitX_left(){this.f_unitX = -f_abs(f_unitX);}
    public void f_setUnitX_right(){this.f_unitX = f_abs(f_unitX);}

    public @fixed int f_getUnitY(){return this.f_unitY;}
    public void f_setUnitY(@fixed int f_unitY){this.f_unitY = f_unitY;}
    public void f_setUnitY_back(){this.f_unitY = -f_abs(f_unitY);}
    public void f_setUnitY_front(){this.f_unitY = f_abs(f_unitY);}

    //public @fixed int f_getOverlap(){return overlap;}
    //public @fixed int f_getOverlapX(){return overlap * unitX;}
    //public @fixed int f_getOverlapY(){return overlap * unitY;}


    @Override
    public @fixed int f_left(){return this.f_xOffset - f_radius;}
    public @fixed int f_right(){return this.f_xOffset + f_radius;}
    @Override
    public @fixed int f_back(){return this.f_yOffset - f_radius;}
    public @fixed int f_front(){return this.f_yOffset + f_radius;}
    @Override
    public @fixed int f_bottom(){return this.f_zOffset;}
    public @fixed int f_top(){return this.f_zOffset + fixed(depth);}


    @Override
    public @fixed int f_leftContact()
    {
        //return f_xOffset - StrictMath.abs( f_mul(f_radius, f_unitX) );
        return this.f_xOffset - f_abs(f_mul(f_radius, f_unitX));
        //return this.f_xOffset + f_mul(f_radius, f_unitX);
    }
    public @fixed int f_rightContact()
    {
        //f_printHex("normXr", f_unitX);

        //return f_xOffset + StrictMath.abs( f_mul(f_radius, f_unitX) );
        return this.f_xOffset + f_abs(f_mul(f_radius, f_unitX));
        //return this.f_xOffset + f_mul(f_radius, f_unitX);
    }

    @Override
    public @fixed int f_backContact()
    {
        //f_print("Bac", f_mul(f_radius, f_unitY));
        //f_print("Bac", f_abs(f_mul(f_radius, f_unitY)));

        //return f_yOffset - StrictMath.abs( f_mul(f_radius, f_unitY) );
        return this.f_yOffset - f_abs(f_mul(f_radius, f_unitY));
        //return this.f_yOffset + f_mul(f_radius, f_unitY);
    }
    public @fixed int f_frontContact()
    {
        //f_printHex("normYf", f_unitY);

        //return f_yOffset + StrictMath.abs( f_mul(f_radius, f_unitY) );
        return this.f_yOffset + f_abs(f_mul(f_radius, f_unitY));
        //return this.f_yOffset + f_mul(f_radius, f_unitY);
    }
    @Override
    public @fixed int f_bottomContact(){return f_zOffset;}
    public @fixed int f_topContact(){return f_zOffset + fixed(depth);}
    

    @Override
    public int getWidth(){return f_toInt(f_radius * 2);}
    public @fixed int f_getWidth(){return f_radius * 2;}
    //
    public int getHeight(){return f_toInt(f_radius * 2);}
    public @fixed int f_getHeight(){return f_radius * 2;}
    //
    public int getDepth(){return this.depth;}
    public @fixed int f_getDepth(){return fixed(this.depth);}
    //
    //
    //
    public final void putThis_Left_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderLeft(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Left_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderLeft_ul(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Left_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderLeft_dl(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Right_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderRight(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Right_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderRight_ur(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Right_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderRight_dr(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Back_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderBack(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Back_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderBack_ul(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Back_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderBack_ur(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Front_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderFront(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Front_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderFront_dl(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Front_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinderFront_dr(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Bottom(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putBottom_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Top(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putTop_Contact(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_OutComposite(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putCylinder_OutComposite(this, entity, f_position, f_velocity, f_oldPosition, f_itsPosition);}

    /*
    public boolean intersects(@fixed int f_x, @fixed int f_y, @fixed int f_z, AAB_Box box, @fixed int f_bx, @fixed int f_by, @fixed int f_bz)
    {return box.intersects(bx, by, bz, this, x, y, z);}
    */

    /**

    /**
     * Checks if this Box is intersecting the given AAB_Box.
     * 
     * @param f_thisPosition
     * @param f_thisVelocity
     * @param b
     * @param f_bPosition
     * @return
     */
    public boolean intersects(final fixedVector3 f_thisPosition, final fixedVector3 f_thisVelocity, final AAB_Box b, final fixedVector3 f_bPosition)
    {
        //Declare Z-Points.
        @fixed int
        f_this_z = f_thisPosition.z + f_thisVelocity.z + f_zOffset,
        f_this_depth = f_this_z + fixed(depth),
        //
        f_B_z = f_bPosition.z + b.f_getZOffset(),
        f_B_depth = f_bPosition.z + b.f_getZOffset() + b.f_getDepth();

        //Check if Z-Axes intersect.
        if(f_this_z < f_B_depth && f_this_depth > f_B_z)
        {
            //Declare this Cylinder's points.
            @fixed int
            f_this_x = f_thisPosition.x + f_thisVelocity.x + this.f_xOffset,
            f_this_y = f_thisPosition.y + f_thisVelocity.y + this.f_yOffset;

            //Declare Box's points.
            @fixed int
            f_B_x = f_bPosition.x + b.f_getXOffset(),
            f_B_y = f_bPosition.y + b.f_getYOffset(),
            //
            f_B_width = f_B_x + b.f_getWidth(),
            f_B_height = f_B_y + b.f_getHeight();


            //
            //Get the box's closest point to Cylinder center by clamping.
            //
            @fixed int
            f_closePointX = StrictMath.max(f_B_x, StrictMath.min(f_this_x , f_B_width)),
            f_closePointY = StrictMath.max(f_B_y, StrictMath.min(f_this_y, f_B_height));
            //f_print("f_closePointX", f_closePointX, "f_closePointY", f_closePointY);

            //Now, begin Pythagorean Therom.
            @fixed int
            f_sideX = f_closePointX - f_this_x,
            f_sideY = f_closePointY - f_this_y;
            @fixed long f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);

            //If the distance is between the point (0) and the radius, there is a collision.
            if(f_sqrLength < f_squareL(this.f_radius))
            {
                //Values representing the direction from the center of this cylinder to the closest point.
                @fixed int f_unitX = 0, f_unitY = 0;

                if(f_sqrLength <= 0)//Caused by the closest point to this cylinder being INSIDE the other one; rather than on the rim.
                {
                    //Use old position instead.
                    f_sideX += f_thisVelocity.x;
                    f_sideY += f_thisVelocity.y;
                    f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);
                }
                if(f_sqrLength > 0)
                {
                    //Square root to get length.
                    @fixed int f_length = f_sqrt(f_sqrLength);

                    //f_unitX = f_div(f_sideX, f_length);
                    //f_unitY = f_div(f_sideY, f_length);

                    f_unitX = f_divRound_Precision(f_sideX, f_length);
                    f_unitY = f_divRound_Precision(f_sideY, f_length);

                    if(f_abs(f_unitX) > f_ONE){f_unitX = f_ONE | ((f_unitX & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                    if(f_abs(f_unitY) > f_ONE){f_unitY = f_ONE | ((f_unitY & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                }
                //f_print("f_unitX", f_unitX, "f_unitY", f_unitY);

                //Set Unit Vector for Collision Response purposses.
                this.f_setUnitVector
                (
                    f_unitX, f_unitY,
                    f_closePointX - (f_thisPosition.x + f_thisVelocity.x),
                    f_closePointY - (f_thisPosition.y + f_thisVelocity.y)
                );
                //c.getRadius() - length);
                return true;
            }
        }
        return false;
    }

    /**Detects Collision between this Cylinder and a Slope.*/
    public boolean intersects(final fixedVector3 f_thisPosition, final fixedVector3 f_thisVelocity, final Slope_Triangle st, final fixedVector3 f_stPosition)
    {
        //Check if X and Y Axes intersect.
        @fixed int//Set Left, Back, and Bottom points
        f_this_x = f_thisPosition.x + this.f_xOffset,
        f_this_y = f_thisPosition.y + this.f_yOffset,
        //
        f_ST_x = f_stPosition.x + st.f_getXOffset(),
        f_ST_y = f_stPosition.y + st.f_getYOffset();

        @fixed int//Dimensions
        f_ST_width = f_ST_x + st.f_getWidth(),
        f_ST_height = f_ST_y + st.f_getHeight();

        //Get the slope's closest point to cylinder center by clamping.
        @fixed int
        f_closePointX = StrictMath.max(f_ST_x, StrictMath.min(f_this_x + f_thisVelocity.x, f_ST_width)),
        f_closePointY = StrictMath.max(f_ST_y, StrictMath.min(f_this_y + f_thisVelocity.y, f_ST_height));

        //Now, begin Pythagorean Therom.
        @fixed int
        f_sideX = f_closePointX - (f_this_x + f_thisVelocity.x),
        f_sideY = f_closePointY - (f_this_y + f_thisVelocity.y);
        @fixed long f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);

        //If the distance is between the point (0) and the radius, then this Cylinder is within the X and Y bounds of the Slope.
        if(f_sqrLength < f_squareL(this.f_radius))
        {
            //Get Z points.
            @fixed int
            f_this_z = f_thisPosition.z + this.f_zOffset,
            f_this_depth = f_this_z + fixed(this.depth),
            //
            f_ST_z = f_stPosition.z + st.f_getZOffset();

            //Set up local variables for later.
            @fixed int f_cpX = f_closePointX, f_cpY = f_closePointY,
            f_sX = f_sideX, f_sY = f_sideY;
            //
            @fixed long f_sql = f_sqrLength;
            //
            @fixed int f_XYpoint = 0, f_XYpointVEL = 0, f_XYlimit = 0;

            //Proceed depending on type.
            switch(st.getType())
            {
                case LEFT:
                {
                    @fixed int f_lx;
                    if(f_this_y >= f_ST_y && f_this_y <= f_ST_height){f_lx = this.f_radius;}
                    else
                    {
                        f_sY = (f_this_y > f_ST_height) ? f_ST_height - (f_this_y + f_thisVelocity.y) : f_ST_y - (f_this_y + f_thisVelocity.y);
                        //f_lx = (float)Math.sqrt((this.f_radius * this.f_radius) - (f_sY * f_sY));
                        f_lx = f_reverseLength(this.f_radius, f_sY);
                    }
                    //
                    f_XYpoint = ((f_this_x + f_lx) - f_ST_x);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.x, st.f_getNormZ());
                    //XYpoint -= (xVelocity * st.getNormZ());
                    f_XYlimit = fixed(st.getWidth());
                    //
                    f_cpX = StrictMath.max(f_ST_x, StrictMath.min(f_XYpointVEL, f_ST_width));
                    f_sX = f_abs(f_cpX - f_XYpointVEL);
                    f_sql = f_square(f_sX) + f_square(f_sY);
                }
                break;
                case RIGHT:
                {
                    @fixed int f_lx;
                    if(f_this_y >= f_ST_y && f_this_y <= f_ST_height){f_lx = this.f_radius;}
                    else
                    {
                        f_sY = (f_this_y > f_ST_height) ? f_ST_height - (f_this_y + f_thisVelocity.y) : f_ST_y - (f_this_y + f_thisVelocity.y);
                        //f_lx = (float)Math.sqrt((this.f_radius * this.f_radius) - (f_sY * f_sY));
                        f_lx = f_reverseLength(this.f_radius, f_sY);
                    }
                    //
                    f_XYpoint = ((f_this_x - f_lx) - f_ST_x);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.x, st.f_getNormZ());
                    //XYpoint -= (xVelocity * st.getNormZ());
                    f_XYlimit = fixed(st.getWidth());
                    //
                    f_cpX = StrictMath.max(f_ST_x, StrictMath.min(f_XYpointVEL, f_ST_width));
                    f_sX = -f_abs(f_XYpointVEL - f_cpX);
                    f_sql = f_square(f_sX) + f_square(f_sY);
                }
                break;
                
                case UP:
                {
                    @fixed int f_ly;
                    if(f_this_x >= f_ST_x && f_this_x <= f_ST_width){f_ly = this.f_radius;}
                    else
                    {
                        f_sX = (f_this_x > f_ST_width) ? f_ST_width - (f_this_x + f_thisVelocity.x) : f_ST_x - (f_this_x + f_thisVelocity.x);
                        //f_ly = (float)Math.sqrt((this.f_radius * this.f_radius) - (f_sX * f_sX));
                        f_ly = f_reverseLength(this.f_radius, f_sX);
                    }
                    //
                    f_XYpoint = ((f_this_y + f_ly) - f_ST_y);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.y, st.f_getNormZ());
                    //XYpoint -= (yVelocity * st.getNormZ());
                    f_XYlimit = st.f_getHeight();
                    //
                    f_cpY = StrictMath.max(f_ST_y, StrictMath.min(f_XYpointVEL, f_ST_height));
                    f_sY =  f_abs(f_cpY - f_XYpointVEL);
                    f_sql = f_square(f_sX) + f_square(f_sY);
                }
                break;
                case DOWN:
                {
                    @fixed int f_ly;
                    if(f_this_x >= f_ST_x && f_this_x <= f_ST_width){f_ly = this.f_radius;}
                    else
                    {
                        f_sX = (f_this_x > f_ST_width) ? f_ST_width - (f_this_x + f_thisVelocity.x) : f_ST_x - (f_this_x + f_thisVelocity.x);
                        //f_ly = (float)Math.sqrt((this.f_radius * this.f_radius) - (f_sX * f_sX));
                        f_ly = f_reverseLength(this.f_radius, f_sX);
                    }
                    //
                    f_XYpoint = ((f_this_y - f_ly) - f_ST_y);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.y, st.f_getNormZ());
                    //XYpoint -= (yVelocity * st.getNormZ());
                    f_XYlimit = fixed(st.getHeight());
                    //
                    f_cpY = StrictMath.max(f_ST_y, StrictMath.min(f_XYpointVEL, f_ST_height));
                    f_sY =  -f_abs(f_XYpointVEL - f_cpY);
                    f_sql = f_square(f_sX) + f_square(f_sY);
                }
                break;
            }

            //y = mx + b, limited to between 0 and Depth.
            //Set Z cross point for collision response purposes.
            @fixed int
            f_cz0 = f_mul(st.f_getXYtoZ_Slope(), f_XYpoint) + st.f_getZ_Intercept(),
            f_cz1 = f_mul(st.f_getXYtoZ_Slope(), f_XYpointVEL) + st.f_getZ_Intercept();
            //
            @fixed int f_stGetDepth = fixed(st.getDepth());
            if(f_cz0 < st.f_getZSlope_Bottom()){f_cz0 = st.f_getZSlope_Bottom();}
            else if(f_cz0 > f_stGetDepth){f_cz0 = f_stGetDepth;}
            //
            if(f_cz1 < st.f_getZSlope_Bottom()){f_cz1 = st.f_getZSlope_Bottom();}
            else if(f_cz1 > f_stGetDepth){f_cz1 = f_stGetDepth;}
            st.f_setCurrentCross_Z(f_cz1);

            //Same with XY cross.
            if(f_XYpointVEL < 0){f_XYpointVEL = 0;}
            else if(f_XYpointVEL > f_XYlimit){f_XYpointVEL = f_XYlimit;}
            st.f_setCurrentCross_XY(f_XYpointVEL);
            //System.out.println(XYpointVEL);

            //Result Slope_Triangle Depth to check for.
            @fixed int f_ST_depth = f_ST_z + f_cz1;



            //y = mx + b, 
            //Get the Z points of this box's current and potential positions.
            //float cz0 = (st.getXYtoZ_Slope() * XYpoint) + st.getZStart(),
            //cz1 = (st.getXYtoZ_Slope() * XYpointVEL) + st.getZStart();

            //Get the midway point of the two points and limit the result between 0 and Depth.
            //float crossZ = cz0 + ((cz1 - cz0) / 2);
            //if(crossZ < 0){crossZ = 0;}
            //else if(crossZ > st.getDepth()){crossZ = st.getDepth();}

            //Set cross point for collision response purposes.
            //st.setCurrentCross_Z(crossZ);
            //st.setCurrentCross_XY(XYpoint + ((XYpointVEL-XYpoint)/2));
            //float ST_depth = ST_z + crossZ;

            //Same check as AABB to AABB.
            if(f_this_z + f_thisVelocity.z < f_ST_depth && f_this_depth + f_thisVelocity.z > f_ST_z)
            {
                //If the Cylinder is currently above the slope, apply the slope-relative unit vector.
                if(f_this_z >= f_ST_z + f_cz0 + f_thisVelocity.z)
                {
                    f_closePointX = f_cpX;
                    f_closePointY = f_cpY;
                    f_sideX = f_sX;
                    f_sideY = f_sY;
                    f_sqrLength = f_sql;
                }
                //System.out.println("above " + cpX);

                //Set Collision Response stuff.
                //float length = (float)Math.sqrt(f_sqrLength);
                
                //@fixed int length = f_sqrtRound(f_sqrLength);
                //
                @fixed int f_unitX = 0, f_unitY = 0;

                if(f_sqrLength <= 0)//Caused by the closest point to this cylinder being INSIDE the other one; rather than on the rim.
                {
                    //Use old position instead.
                    f_sideX += f_thisVelocity.x;
                    f_sideY += f_thisVelocity.y;
                    f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);
                }
                if(f_sqrLength > 0)
                {
                    @fixed int f_length = f_sqrt(f_sqrLength);

                    f_unitX = f_divRound_Precision(f_sideX, f_length);
                    f_unitY = f_divRound_Precision(f_sideY, f_length);

                    if(f_abs(f_unitX) > f_ONE){f_unitX = f_ONE | ((f_unitX & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                    if(f_abs(f_unitY) > f_ONE){f_unitY = f_ONE | ((f_unitY & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                }

                this.f_setUnitVector
                (
                    f_unitX, f_unitY,
                    f_closePointX - f_stPosition.x,
                    f_closePointY - f_stPosition.y
                );
                //this.radius - length);
                //
                return true;
            }
        }

        return false;
    }

    /**Detects Collision between two Cylinders.*/
    public boolean intersects(final fixedVector3 f_thisPosition, final fixedVector3 f_thisVelocity, final Cylinder c, final fixedVector3 f_cPosition)
    {
        //Time of impact values.
        //@fixed int f_timeOfContact_z = 0, f_timeOfLeave_z = f_ONE;

        //
        //Check if the top and bottom parts of the cylinders collide from the start.
        //
        @fixed int
        f_this_z = f_thisPosition.z + this.f_zOffset + f_thisVelocity.z,
        f_this_depth = f_this_z + fixed(depth),
        //
        f_C_z = f_cPosition.z + c.f_getZOffset(),
        f_C_depth = f_cPosition.z + c.f_getZOffset() + fixed(c.getDepth());

        //Are they colliding along the Z axis?
        if(f_this_z < f_C_depth && f_this_depth > f_C_z)
        {
            //Calculate length between cylinder origins.
            @fixed int
            f_sideX = (f_thisPosition.x + this.f_xOffset + f_thisVelocity.x) - (f_cPosition.x + c.f_getXOffset()),
            f_sideY = (f_thisPosition.y + this.f_yOffset + f_thisVelocity.y) - (f_cPosition.y + c.f_getYOffset());
            //f_sideX = (f_thisPosition.x + this.f_xOffset) - (f_cPosition.x + c.f_getXOffset()),
            //f_sideY = (f_thisPosition.y + this.f_yOffset) - (f_cPosition.y + c.f_getYOffset());
            //
            @fixed long f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);

            //This returns true if the distance between the two cylinders is within the sum of their radii.
            //This can be determined via Pythagorean Therom: a^2 + b^2 = c^2.
            if(f_sqrLength < f_squareL(this.f_radius + c.f_getRadius()))
            {
                //Square root to get length.
                @fixed int f_length = f_sqrt(f_sqrLength);

                //Values representing the direction from the center of this cylinder to the other cylinder.
                @fixed int f_unitX = 0, f_unitY = 0;

                if(f_sqrLength <= 0)//Caused by the closest point to this cylinder being INSIDE the other one; rather than on the rim.
                {
                    //Use old position instead.
                    f_sideX += f_thisVelocity.x;
                    f_sideY += f_thisVelocity.y;
                    f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);
                }
                if(f_length > 0)
                {
                    f_unitX = f_divRound_Precision(f_sideX, f_length);
                    f_unitY = f_divRound_Precision(f_sideY, f_length);

                    if(f_abs(f_unitX) > f_ONE){f_unitX = f_ONE | ((f_unitX & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                    if(f_abs(f_unitY) > f_ONE){f_unitY = f_ONE | ((f_unitY & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                }

                c.f_setUnitVector
                (
                    //TODO
                    f_unitX, f_unitY,
                    f_xOffset - f_mul(this.f_radius, f_unitX),
                    f_yOffset - f_mul(this.f_radius, f_unitY)
                );
                //this.radius - length);

                //Collision was made.
                return true;
            }
        }

        return false;
        

        /*
        //If they are not already colliding along the Z axis.
        if(!(f_this_z < f_C_depth && f_this_depth > f_C_z))
        {
            //
            //Z sweep check.
            //

            //Calculate relative z velocity.
            @fixed int f_vz = f_cVelocity_z - f_thisVelocity.z;

            //Z time of impact.
            f_timeOfContact_z =
            (f_this_depth < f_C_z && f_vz < 0) ? f_div((f_this_depth - f_C_z), f_vz) ://This top less than its bottom and going bottom.
            (f_C_depth < f_this_z && f_vz > 0) ? f_div((f_this_z - f_C_depth), f_vz) ://Its top less than this bottom going top.
            0;//Default.

            f_timeOfLeave_z =
            (f_C_depth > f_this_z && f_vz < 0) ? f_div((f_this_z - f_C_depth), f_vz) ://Its top greater than this bottom going bottom.
            (f_this_depth > f_C_z && f_vz > 0) ? f_div((f_this_depth - f_C_z), f_vz) ://This top greater than its bottom and going top.
            f_ONE;//Default.

            if(f_timeOfContact_z > f_timeOfLeave_z){return false;}
        }


        //
        //Check if the circular parts of the cylinders collide from the start.
        //
        @fixed int f_timeOfContact_circ = 0, f_timeOfLeave_circ = f_ONE;//Default values if already colliding.

        //Calculate length between cylinder origins.
        @fixed int
        //f_sideX = (f_thisPosition.x + this.f_xOffset + f_thisVelocity.x) - (f_cPosition.x + c.f_getXOffset()),
        //f_sideY = (f_thisPosition.y + this.f_yOffset + f_thisVelocity.y) - (f_cPosition.y + c.f_getYOffset());
        f_sideX = (f_thisPosition.x + this.f_xOffset) - (f_cPosition.x + c.f_getXOffset()),
        f_sideY = (f_thisPosition.y + this.f_yOffset) - (f_cPosition.y + c.f_getYOffset());
        //
        @fixed long f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);

        //This returns true if the distance between the two cylinders is within the sum of their radii.
        //This can be determined via Pythagorean Therom: a^2 + b^2 = c^2.
        if(f_sqrLength < f_squareL(this.f_radius + c.f_getRadius()))
        {
            //Square root to get length.
            @fixed int f_length = f_sqrt(f_sqrLength);
            //f_length = f_sqrtRound(f_length);

            //Get unit vectors.
            @fixed int f_unitX = 0, f_unitY = 0;
            if(f_length > 0)
            {
                f_unitX = f_div(f_sideX, f_length);
                f_unitY = f_div(f_sideY, f_length);
            }

            c.setUnitVector
            (
                //TODO
                f_unitX, f_unitY,
                f_xOffset - f_mul(this.f_radius, f_unitX),
                f_yOffset - f_mul(this.f_radius, f_unitY)
            );
            //this.radius - length);

            //Final time of contact is the latest of the times.
            @fixed int f_finalTimeOfContact = StrictMath.max(f_timeOfContact_circ, f_timeOfContact_z);

            //Final time of leave is the earliest of the times.
            @fixed int f_finalTimeOfLeave = StrictMath.min(f_timeOfLeave_circ, f_timeOfLeave_z);

            //Collision was made.
            return true;
        }
        //If this check failed, perform the sweep version.
        
        

        //
        //Circular sweep check.
        //        

        //Calculate relative x and y velocity (interpret as this Cylinder moving quickly into C, not them moving into each other).
        @fixed int
        f_vx = f_cVelocity_x - f_thisVelocity.x,
        f_vy = f_cVelocity_y - f_thisVelocity.y;

        //Squared Length of velocity.
        @fixed int f_vSqrLength = f_square(f_vx) + f_square(f_vy);//'A' component. 

        //-(f_v * f_side) to +(f_v * f_side)
        @fixed int f_velDotDist = 2 * (f_mul(f_vx, f_sideX) + f_mul(f_vy, f_sideY));//'B' component.

        //Square length from this cylinder to c; subtracted by radiiSum.
        @fixed long f_sqrLengthMinusRadii = f_sqrLength//'C' component.
        - f_square(this.f_radius + c.f_getRadius());

        //
        //Solve for at what points does the length between the two circles equal their radiiSum.
        //sqrt( (bInitialPos + (t * bVelocity)) - (aInitialPos + (t * aVelocity))^2 ) = (bRadius + aRadius)
        //
        //(bInitialPos - aInitialPos) + (t * (bVelocity - aVelocity)) = 0 combine the two
        //t * (bVelocity - aVelocity) = 0 - (bInitialPos - aInitialPos) subtract initial
        //t = (0 - (a_initialX - b_initialX)) / (a_distanceX - b_distanceX) divide relative velocity
        //
        //Gomez. Why the hail did you show the equation BACKWARDS?
        //(f_vSqrLength * t^2) + (f_velDotDist * t) + f_sqrLength = -radiiSum
        //(f_vSqrLength * t^2) + (f_velDotDist * t) + (f_sqrLength - radiiSum) = 0
        //
        //
        //
        //Quadratic Formula:
        //x = -b [+ or -] sqrt(b^2 - (4 * a * c)) / (2 * a)
        //
        //initialDist = bInitialPos - aInitialPos
        //initialDist^2 + 2 * ((bVelocity - aVelocity)^2 * t^2 + (bVelocity * initialLength) * t + (aVelocity * initialLength)) = (bRadius + aRadius)^2
        //
        //f_sideSqrLength + (2 * f_velDotDist) * t + (f_vSqrLength * t^2) - f_sqrRadiiSum = 0
        //
        //
        //

        //Start quadratic formula. For now: b^2 - (4 * a^2 * c^2).
        @fixed long f_discriminant = f_squareL(f_velDotDist) - f_mulL(4 * f_vSqrLength, f_sqrLength);

        //If the result is not negative, it has real solutions. This means the circles are colliding.
        if(f_discriminant >= 0)
        {
            @fixed int f_sqrtedPortion = f_sqrt(f_discriminant);

            //Positive solution. Time of impact. (-b + f_sqrtedPortion) / (2 * a)
            f_timeOfContact_circ = f_div(-f_velDotDist + f_sqrtedPortion, f_vSqrLength << 1);

            //Negative solution. Time of leave.
            f_timeOfLeave_circ = f_div(-f_velDotDist - f_sqrtedPortion, f_vSqrLength << 1);

            //Earlier time should be time of contact. Later time should be time of leave.
            if(f_timeOfContact_circ > f_timeOfLeave_circ)
            {
                @fixed int f_swap = f_timeOfLeave_circ;
                f_timeOfLeave_circ = f_timeOfContact_circ;
                f_timeOfContact_circ = f_swap;
            }
            //At this point, a collision has been confirmed.

            //Final time of contact is the latest of the times.
            @fixed int f_finalTimeOfContact = StrictMath.max(f_timeOfContact_circ, f_timeOfContact_z);

            //Final time of leave is the earliest of the times.
            //@fixed int f_finalTimeOfLeave = StrictMath.min(f_timeOfLeave_circ, f_timeOfLeave_z);

            //Square root to get length.
            @fixed int f_length = f_sqrt(f_sqrLength);

            //Get unit vectors.
            @fixed int f_unitX = 0, f_unitY = 0;
            if(f_length > 0)
            {
                f_unitX = f_div(f_sideX, f_length);
                f_unitY = f_div(f_sideY, f_length);
            }

            c.setUnitVector
            (
                f_unitX, f_unitY,
                f_xOffset - f_mul(this.f_radius, f_unitX),
                f_yOffset - f_mul(this.f_radius, f_unitY)
            );

            //Collision was made.
            return true;
        }
        //If the circ result is negative, then imaginary numbers are involved. This means the cylinders are not colliding and the check can stop here.

        return false;
        */
    }

    
    /**Detects Collision between this Cylinder and an Isosceles Triangle.*/
    public boolean intersects(final fixedVector3 f_thisPosition, final fixedVector3 f_thisVelocity,
    final Isosceles_Triangle iso, final fixedVector3 f_isoPosition, final byte isoEnds)
    {
        //
        //Z Check.
        //

        //Declare Z-Points.
        @fixed int
        f_this_z = f_thisPosition.z + this.f_zOffset + f_thisVelocity.z,
        f_ISO_z = f_isoPosition.z + iso.f_getZOffset();

        @fixed int//Dimensions
        f_this_depth = f_this_z + fixed(this.depth),
        f_ISO_depth = f_ISO_z + iso.f_getDepth();

        //Do the Z-Axes intersect?
        if(f_this_z < f_ISO_depth && f_this_depth > f_ISO_z)
        {
            //
            //Check if circle is within Box region.
            //

            //Declare this Cylinder's points.
            @fixed int
            f_this_x = f_thisPosition.x + this.f_xOffset,
            f_this_xVel = f_this_x + f_thisVelocity.x,
            f_this_y = f_thisPosition.y + this.f_yOffset,
            f_this_yVel = f_this_y + f_thisVelocity.y;

            //Declare Triangle's points.
            @fixed int
            f_ISO_x = f_isoPosition.x + iso.f_getXOffset(),
            f_ISO_y = f_isoPosition.y + iso.f_getYOffset(),
            //
            f_ISO_width = f_ISO_x + iso.f_getWidth(),
            f_ISO_height = f_ISO_y + iso.f_getHeight();


            //Get the box's closest point to Cylinder center by clamping.
            @fixed int
            f_boxClosePointX = StrictMath.max(f_ISO_x, StrictMath.min(f_this_xVel , f_ISO_width)),
            f_boxClosePointY = StrictMath.max(f_ISO_y, StrictMath.min(f_this_yVel, f_ISO_height));

            //Now, begin Pythagorean Therom.
            @fixed int
            f_sideX = f_boxClosePointX - f_this_xVel,
            f_sideY = f_boxClosePointY - f_this_yVel;
            @fixed long f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);

            //If the distance is between the point (0) and the radius, then the Cylinder is within the Box region.
            if(f_sqrLength < f_squareL(this.f_radius))
            {
                //
                //Check if the closest point on the circle to the slant is on the other side of it.
                //
                @fixed int
                f_x_PointToCheck = f_this_xVel + f_mul(this.f_radius, -iso.f_getNormX()),
                f_y_PointToCheck = f_this_yVel + f_mul(this.f_radius, -iso.f_getNormY());
                //It's just the normal of the triangle but flipped.

                @fixed int//Treat slanted wall as a two-point line segment.
                f_segA_x = f_ISO_x + iso.f_getStartX(),
                f_segA_y = f_ISO_y + iso.f_getStartY(),
                //
                f_segB_x = f_ISO_x + iso.f_getEndX(),
                f_segB_y = f_ISO_y + iso.f_getEndY(),
                //
                f_segWidth = f_segB_x - f_segA_x,
                f_segHeight = f_segB_y - f_segA_y;

                //Calculate the side of the line the point is on.
                @fixed int f_sign =
                f_mul(f_x_PointToCheck - f_segA_x, -f_segHeight) +
                f_mul(f_y_PointToCheck - f_segA_y, f_segWidth);

                //Is the point on the solid side of the line? If so, collision confirmed.
                if( (iso.isDown() && f_sign < 0) || (!iso.isDown() && f_sign > 0) )
                {
                    //TODO Do we need any more information from here?
                    //if(!needResponseInfo){return true;}
                    //f_print("f_sign", f_sign);

                    //
                    //Try to find the closest point (possibly of intersection) from the slant to the point's path of movement.
                    //
                    @fixed int f_closest_x, f_closest_y;

                    //Direction from point A to point B of slant. Equivalent to rotating the face's direction 90 degrees.
                    @fixed int
                    //seg_ab_dirX = (seg_v_x / seg_v_length),
                    //seg_ab_dirY = (seg_v_y / seg_v_length),
                    f_segAB_dirX = -iso.f_getNormY(),
                    f_segAB_dirY = iso.f_getNormX();

                    if(iso.isDown())
                    {
                        f_segAB_dirX = -f_segAB_dirX;
                        f_segAB_dirY = -f_segAB_dirY;
                    }

                    //Squared length of slant.
                    @fixed int f_segV_sqrLength = f_square(f_segWidth) + f_square(f_segHeight);

                    

                    
                    //
                    //Use dot product projection from cylinder's new position to line.
                    //
                    
                    //Create line from current point to closest point on Circle.
                    @fixed int
                    f_ptAC_x = f_this_xVel - f_segA_x,
                    f_ptAC_y = f_this_yVel - f_segA_y,
                    //f_ptAC_x = f_x_PointToCheck - f_segA_x,
                    //f_ptAC_y = f_y_PointToCheck - f_segA_y,

                    //Dot Product: Multiply all the X's plus multiiply all the Y's.
                    //Used to "project this point onto the line" and determine how far
                    //down the line the Circle's point is.
                    f_distanceDownAB = f_mul(f_ptAC_x, f_segAB_dirX) + f_mul(f_ptAC_y, f_segAB_dirY);
                    //System.out.println(pt_ac_x + " " + pt_ac_y + " " + distanceDownAB);

                    boolean pointOnSlant = true;

                    //If we're outside point A's side (if it is included in the check).
                    if((isoEnds & 0b01) == 0b01 && f_distanceDownAB <= 0)
                    {
                        //Assume point A is the closest point.
                        f_closest_x = f_segA_x;
                        f_closest_y = f_segA_y;

                        pointOnSlant = false;
                    }
                    //If we're beyond point B's side (if it is included in the check).
                    //else if(distanceDownAB > seg_v_length)
                    else if((isoEnds & 0b10) == 0b10 && f_square(f_distanceDownAB) >= f_segV_sqrLength)
                    {
                        //Assume point B is the closest point.
                        f_closest_x = f_segB_x;
                        f_closest_y = f_segB_y;

                        pointOnSlant = false;
                    }
                    //Default case.
                    else
                    {
                        @fixed int//Projected distance is [direction from A to B] * [distance down the line segment].
                        proj_v_x = f_mul(f_segAB_dirX, f_distanceDownAB),
                        proj_v_y = f_mul(f_segAB_dirY, f_distanceDownAB);
                        //proj_v_x = f_mulRound_Whole(f_segAB_dirX, f_distanceDownAB),
                        //proj_v_y = f_mulRound_Whole(f_segAB_dirY, f_distanceDownAB);
                        //Using fixed-point numbers, these need to be rounded to the nearest whole number to prevent automatic sliding.

                        //PointA + projected distance.
                        f_closest_x = f_segA_x + proj_v_x;
                        f_closest_y = f_segA_y + proj_v_y;
                    }

                    //Used for line intersection test.
                    @fixed int f_determinant = f_mul(f_thisVelocity.y, f_segWidth) - f_mul(f_thisVelocity.x, f_segHeight);
                    //System.out.println("pointOnSlant: " + pointOnSlant);
                    f_print("f_determinant", f_determinant);

                    //If the path and slant are paralell or x and y velocities are zero, then that is the point of intersection.
                    //Otherwise...
                    //if(f_determinant != 0)
                    if
                    (
                        pointOnSlant
                        && (!iso.isDown() && f_determinant > 0)
                        || (iso.isDown() && f_determinant < 0)
                    )
                    {
                        //
                        //Perform line intersection test using closest point on circle to line.
                        //

                        @fixed int
                        //f_uA = f_mul(f_segWidth, f_y_PointToCheck - f_segA_y)       -      f_mul(f_segHeight, f_x_PointToCheck - f_segA_x);
                        f_uA = f_mul(f_thisVelocity.x, f_segA_y - f_y_PointToCheck)  -  f_mul(f_thisVelocity.y, f_segA_x - f_x_PointToCheck);

                        //@fixed int
                        //f_uB = f_mul(f_thisVelocity.x, f_y_PointToCheck - f_segA_y)    -   f_mul(f_thisVelocity.y, f_x_PointToCheck - f_segA_x);
                        //f_uB = f_mul(f_segWidth, f_segA_y - f_y_PointToCheck)   -   f_mul(f_segHeight, f_segA_x - f_x_PointToCheck);
            
                        //f_print("f_uA", f_uA, "f_uB", f_uB, "f_determinant", f_determinant);
            
                        //f_uA = (f_uA < 0) ? 0 : (f_uA > f_determinant) ? f_determinant : f_uA;
                        //f_uB = (f_uB < 0) ? 0 : (f_uB > f_determinant) ? f_determinant : f_uB;
            
                        //This is where the line would intersect if it was infinite.
                        //f_closest_x = f_div(f_mul(f_uA, f_thisVelocity.x), f_determinant);
                        f_closest_x = f_divRound_Precision(f_mul(f_uA, f_segWidth), f_determinant);
                        //f_closest_y = f_div(f_mul(f_uA, f_thisVelocity.y), f_determinant);
                        f_closest_y = f_divRound_Precision(f_mul(f_uA, f_segHeight), f_determinant);


                        //StartX clamp.
                        if((isoEnds & 0b01) == 0b01 &&
                        (f_segWidth >= 0 && f_closest_x < 0) || (f_segWidth < 0 && f_closest_x >= 0))
                        {
                            f_closest_x = 0;
                            pointOnSlant = false;
                        }
                        //EndX clamp.
                        else if((isoEnds & 0b10) == 0b10 && f_abs(f_closest_x) >= f_abs(f_segWidth))
                        {
                            f_closest_x = f_segWidth;
                            pointOnSlant = false;
                        }

                        //Apply world position.
                        //f_closest_x += f_x_PointToCheck;
                        f_closest_x += f_segA_x;
                        

                        
                        //StartY clamp.
                        if
                        (
                            (isoEnds & 0b01) == 0b01 &&
                            (
                                (f_segHeight >= 0 && f_closest_y <= 0) ||
                                (f_segHeight < 0 && f_closest_y >= 0)
                            )
                        )
                        {
                            f_closest_y = 0;
                            pointOnSlant = false;
                        }
                        //EndY clamp.
                        else if((isoEnds & 0b10) == 0b10 && f_abs(f_closest_y) >= f_abs(f_segHeight))
                        {
                            f_closest_y = f_segHeight;
                            pointOnSlant = false;
                        }

                        //Apply world position.
                        //f_closest_y += f_y_PointToCheck;
                        f_closest_y += f_segA_y;
                    }

                    //f_print("f_closest_x", f_closest_x, "f_closest_y", f_closest_y);


                    //
                    //
                    //
                    //if(pointOnSlant)
                    //{
                        f_x_PointToCheck = f_this_x + f_mul(this.f_radius, iso.f_getNormX());
                        f_y_PointToCheck = f_this_y + f_mul(this.f_radius, iso.f_getNormY());

                        switch(iso.getType())
                        {
                            case Isosceles_Triangle.TYPE_UL:
                            if(f_x_PointToCheck >= f_segB_x || f_y_PointToCheck >= f_segA_y){pointOnSlant = false;}
                            break;

                            case Isosceles_Triangle.TYPE_UR:
                            if(f_x_PointToCheck <= f_segA_x || f_y_PointToCheck >= f_segB_y){pointOnSlant = false;}
                            break;

                            case Isosceles_Triangle.TYPE_DL:
                            if(f_x_PointToCheck >= f_segB_x || f_y_PointToCheck <= f_segA_y){pointOnSlant = false;}
                            break;

                            case Isosceles_Triangle.TYPE_DR:
                            if(f_x_PointToCheck <= f_segA_x || f_y_PointToCheck <= f_segB_y)
                            {
                                //System.out.println("DR check.");
                                pointOnSlant = false;
                            }
                            break;
                        }
                    //}

                    //System.out.println("pointOnSlant: " + pointOnSlant);


                    //
                    //Calculate the side of the line the closest point from earlier is on.
                    //
                    //if(pointOnSlant)
                    //{
                        f_sign =
                        f_mul(f_boxClosePointX - f_segA_x, -f_segHeight) +
                        f_mul(f_boxClosePointY - f_segA_y, f_segWidth);

                        //Is that point on the non-solid side of the line?
                        if( (iso.isDown() && f_sign >= 0) || (!iso.isDown() && f_sign <= 0) )
                        {
                            //Change ClosePoint.
                            f_boxClosePointX = f_closest_x;
                            f_boxClosePointY = f_closest_y;

                            //Recalculate sqrLength.
                            f_sideX = f_boxClosePointX - f_this_xVel;
                            f_sideY = f_boxClosePointY - f_this_yVel;
                            f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);

                            //System.out.println("pointOnSlant: " + pointOnSlant);
                        }
                    //}


                    


                    //
                    //Final parts.
                    //

                    //Direction to point.
                    @fixed int f_unitX = -iso.f_getNormX();
                    @fixed int f_unitY = -iso.f_getNormY();

                    if(f_sqrLength <= 0)//Caused by the closest point to this cylinder being INSIDE the triangle; rather than on the rim.
                    {
                        //Use old position instead.
                        f_sideX += f_thisVelocity.x;
                        f_sideY += f_thisVelocity.y;
                        f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);
                    }
                    if(!pointOnSlant && f_sqrLength > 0)
                    {
                        //Square root length from cylinder center to closest point on line.
                        @fixed int f_length = f_sqrt(f_sqrLength);
                        //f_print("length", f_length);

                        f_unitX = f_divRound_Precision(f_sideX, f_length);
                        f_unitY = f_divRound_Precision(f_sideY, f_length);

                        if(f_abs(f_unitX) > f_ONE){f_unitX = f_ONE | ((f_unitX & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                        if(f_abs(f_unitY) > f_ONE){f_unitY = f_ONE | ((f_unitY & Fixed.f_SIGN_PORTION) >> Fixed.f_WHOLE_BITS);}
                    }
                    //f_print("f_unitX", f_unitX, "f_unitY", f_unitY);

                    //Set Unit Vector for Collision Response purposses.
                    this.f_setUnitVector
                    (
                        f_unitX, f_unitY,
                        f_boxClosePointX - (f_thisPosition.x + f_thisVelocity.x),
                        f_boxClosePointY - (f_thisPosition.y + f_thisVelocity.y)
                    );
                    //c.getRadius() - length);

                    //Point on slant to collide with.
                    iso.setCurrentCross
                    (
                        f_closest_x - (f_isoPosition.x + iso.f_getXOffset()),// + iso.f_get_YtoX_Slope(),
                        f_closest_y - (f_isoPosition.y + iso.f_getYOffset())// + iso.f_get_XtoY_Slope()
                    );

                    //Collision confirmed.
                    return true;
                }
            }
        }

        //Some part of the initial collision check didn't pass. So no collision.
        return false;
    }

    /**Returns this Sphere's radius.*/
    public @fixed int f_getRadius(){return f_radius;}

    

    /**Checks if this Cylinder is going into a Box.*/
    public boolean collide_Circle(Shape_Box box, @fixed int f_x, @fixed int f_y, @fixed int f_thisX, @fixed int f_thisY)
    {
        //Declare the Box's points.
        @fixed int
        f_B_x = f_x + box.f_getXOffset(),
        f_B_y = f_y + box.f_getYOffset(),
        f_B_width = f_B_x + box.f_getWidth(),
        f_B_height = f_B_y + box.f_getHeight();

        //Declare Sphere's points.
        @fixed int
        f_C_x = f_thisX + this.f_xOffset,
        f_C_y = f_thisY + this.f_yOffset;

        //Get the box's closest point to sphere center by clamping.
        @fixed int
        f_closePointX = StrictMath.max(f_B_x, StrictMath.min(f_C_x, f_B_width)),
        f_closePointY = StrictMath.max(f_B_y, StrictMath.min(f_C_y, f_B_height));

        //Now, begin Pythagorean Therom.
        @fixed int f_sideX = f_closePointX - f_C_x;
        @fixed int f_sideY = f_closePointY - f_C_y;

        //Since we already know the Cylinder and Box intersect with velocity applied,
        //we just need to know if they didn't intersect without velocity.
        return f_squareL(f_sideX) + f_squareL(f_sideY) >= f_squareL(f_radius);
    }

    /**Checks if this Cylinder is going into the circular part of another Cylinder.*/
    public boolean collide_Circle(Cylinder c, @fixed int f_x, @fixed int f_y, @fixed int f_thisX, @fixed int f_thisY)
    {
        @fixed int
        f_sideX = ((f_x + c.f_getXOffset()) - (f_thisX + f_xOffset)),
        f_sideY = ((f_y + c.f_getYOffset()) - (f_thisY + f_yOffset)),
        f_radiiSum = this.f_radius + c.f_getRadius();

        //Since we already know the Cylinders intersect with velocity applied,
        //we just need to know if they didn't intersect without velocity.
        return f_square(f_sideX) + f_square(f_sideY) >= f_square(f_radiiSum);
    }

    public boolean collide_Circle(Shape3D shape, @fixed int f_x, @fixed int f_y, @fixed int f_thisX, @fixed int f_thisY)
    {
        if(shape instanceof Cylinder){return collide_Circle((Cylinder)shape, f_x, f_y, f_thisX, f_thisY);}
        else if(shape instanceof Shape_Box){return collide_Circle((Shape_Box)shape, f_x, f_y, f_thisX, f_thisY);}
        return false;
    }

    /**
     * Checks if this Cylinder is going into the Bottom Side of the given Shape.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_z is the given box's z position.
     * @param f_zVelocity is the given box's z velocity.
     * @param f_thisZ is this shape's z position.
     * @return true if the given box is about to collide with this shape from the bottom.
     */
    public boolean collide_Bottom(Shape3D shape, @fixed int f_z, @fixed int f_zVelocity, @fixed int f_thisZ)
    {
        @fixed int f_top = shape.f_topContact();

        return (f_z + f_top) <= (f_thisZ + this.f_zOffset)
        && (f_z + f_top) + f_zVelocity > (f_thisZ + this.f_zOffset);
    }


    /**
     * Checks if this Cylinder is going into the Top Side of the given Shape.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_z is the given box's z position.
     * @param f_zVelocity is the given box's z velocity.
     * @param f_thisZ is this shape's z position.
     * @return true if the given box is about to collide with this shape from the bottom.
     */
    public boolean collide_Top(Shape3D shape, @fixed int f_z, @fixed int f_zVelocity, @fixed int f_thisZ)
    {
        @fixed int f_bottom = shape.f_bottomContact();

        return (f_z + f_bottom) >= (f_thisZ + f_zOffset) + fixed(this.depth)
        && (f_z + f_bottom) + f_zVelocity < (f_thisZ + f_zOffset) + fixed(this.depth);
    }

    /**Performs collision check.*/
    //public boolean performCollision(Vector3f thisPosition, Vector3f thisVelocity, Shape3D shape, Vector3f shapePosition)
    public boolean performCollision(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Shape3D shape, fixedVector3 f_shapePosition)
    {
        //AAB_box
        if(shape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)shape;

            if
            (
                //box.intersects(shapePosition.x, shapePosition.y, shapePosition.z, this,
                //thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z)

                intersects(f_thisPosition, f_thisVelocity, box, f_shapePosition)
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

            if
            (
                //intersects(thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z,
                //cylinder, shapePosition.x, shapePosition.y, shapePosition.z)

                intersects(f_thisPosition, f_thisVelocity, cylinder, f_shapePosition)
            )
            {
                //cylinder.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        //Slope_Triangle
        else if(shape instanceof Slope_Triangle)
        {
            Slope_Triangle st = (Slope_Triangle)shape;

            if
            (
                //intersects(thisPosition.x, thisPosition.y, thisPosition.z,
                //thisVelocity.x, thisVelocity.y, thisVelocity.z,
                //st, shapePosition.x, shapePosition.y, shapePosition.z)

                intersects(f_thisPosition, f_thisVelocity, st, f_shapePosition)
            )
            {
                //st.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        //Isosceles_Triangle
        else if(shape instanceof Isosceles_Triangle)
        {
            Isosceles_Triangle iso = (Isosceles_Triangle)shape;

            if
            (
                //intersects(thisPosition.x, thisPosition.y, thisPosition.z,
                //thisVelocity.x, thisVelocity.y, thisVelocity.z,
                //iso, shapePosition.x, shapePosition.y, shapePosition.z)

                intersects(f_thisPosition, f_thisVelocity, iso, f_shapePosition, (byte)0b11)
            )
            {
                //iso.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }

        //No collision was made.
        return false;
    }

    /*
     * Collision Responses
     */

    /**
     * Puts the given position next to the Bottom Side of the given Box.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putBottom(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = (f_thisPosition.z + this.f_zOffset) - shape.f_topContact();
        f_velocity.z = 0;
    }

    /**
     * Puts the given position next to the Top Side of the given Box.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putTop(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = (f_thisPosition.z + this.f_zOffset) + fixed(this.depth) - shape.f_bottomContact();
        f_velocity.z = 0;
    }


    /**
     * Puts the given Shape outside of the circle portion of this Cylinder.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putAroundCircle(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.x = ((f_thisPosition.x + this.f_xOffset) + f_mul(this.f_radius, f_unitX)) - f_pointX;
        f_position.y = ((f_thisPosition.y + this.f_yOffset) + f_mul(this.f_radius, f_unitY)) - f_pointY;

        //position.x = ((thisPosition.x + this.xOffset) + (this.radius * unitX)) - (overlap * unitX);
        //position.y = ((thisPosition.y + this.yOffset) + (this.radius * unitY)) - (overlap * unitY);

        f_velocity.x = 0; f_velocity.y = 0;
    }

    /**
     * Puts this Cylinder outside of the given Shape.
     * 
     * @param shape the Shape to put this around.
     * @param entity the Entity to put this around.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putCircleAround(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        @fixed int f_newX = (((f_thisPosition.x + this.f_xOffset) + f_mul(this.f_radius, f_unitX)) - f_pointX) - f_position.x;
        @fixed int f_newY = (((f_thisPosition.y + this.f_yOffset) + f_mul(this.f_radius, f_unitY)) - f_pointY) - f_position.y;

        //float newX = (((thisPosition.x + this.xOffset) + (this.radius * unitX)) - (overlap * unitX)) - position.x;
        //float newY = (((thisPosition.y + this.yOffset) + (this.radius * unitY)) - (overlap * unitY)) - position.y;

        f_thisPosition.x -= f_newX;
        f_thisPosition.y -= f_newY;

        //velocity.x = 0; velocity.y = 0;
    }

    /**
     * Puts the given Cylinder outside of this Cylinder.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putOutOfCylinder(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        //Bottom.
        if(collide_Bottom(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {putBottom(shape, entity, f_position, f_velocity, f_thisPosition);}
        //Top.
        else if(collide_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {putTop(shape, entity, f_position, f_velocity, f_thisPosition);}
        //Cylinder.
        else{putAroundCircle(shape, entity, f_position, f_velocity, f_thisPosition);}
        //System.out.println("col");
    }

    /**
     * Puts the given Cylinder outside of this Cylinder only if it wasn't already inside it.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public void putOutOfCylinder_Composite(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        //Bottom.
        if(collide_Bottom(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {putBottom(shape, entity, f_position, f_velocity, f_thisPosition);}

        //Top.
        else if(collide_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {putTop(shape, entity, f_position, f_velocity, f_thisPosition);}

        //Circle.
        else if(collide_Circle(shape, f_position.x - f_velocity.x, f_position.y - f_velocity.y, f_thisPosition.x, f_thisPosition.y))
        {putAroundCircle(shape, entity, f_position, f_velocity, f_thisPosition);}
    }

    /*
     * Render Functions.
     */

    /**Renders this Cylinder as a series of circles and lines.*/
    private Vector4f lightColor = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f),
    darkColor = new Vector4f(0.0f, 0.5f, 0.0f, 1.0f),
    lineColorR = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f),
    lineColorB = new Vector4f(0.0f, 0.5f, 1.0f, 1.0f);

    @Override
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {
        int xa = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        ya = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        za = (int)(f_toFloat(f_position.z + f_zOffset) * scale),
        //
        d = (int)(f_toFloat(f_position.z + fixed(depth) + f_zOffset) * scale);

        float r = f_toFloat(f_radius) * scale;
        
        //Bottom Circle.
        screen.drawCircle(xa, ya, za, r, 1, darkColor, true);

        //Side Lines.
        screen.drawLine((int)(xa - r), ya, za, (int)(xa - r), ya, d, lightColor, true);
        screen.drawLine((int)(xa + r), ya, za, (int)(xa + r), ya, d, lightColor, true);

        //Top Circle.
        screen.drawCircle(xa, ya, d, r, 1, lightColor, true);

        float unitX = f_toFloat(f_unitX);
        float unitY = f_toFloat(f_unitY);
        //System.out.println(unitX + " " + unitY);

        //Line of contact.
        screen.drawLine(xa, ya, za, (int)(xa + (r * unitX)), (int)(ya + (r * unitY)), za, lineColorR, true);

        screen.drawLine(xa, ya, za, (int)(xa + (r * -unitX)), (int)(ya + (r * -unitY)), za, lineColorB, true);
    }
}
