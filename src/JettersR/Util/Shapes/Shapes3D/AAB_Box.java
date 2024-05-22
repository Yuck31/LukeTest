package JettersR.Util.Shapes.Shapes3D;
/**
 * This is a Box that simply detects if anything is inside it.
 * What happens as a result depends on the CollisionResponse used.
 * 
 * Author: Luke Sullivan
 * Last Edit: 11/10/2022
 */
//import java.awt.Rectangle;

import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class AAB_Box extends Shape_Box
{
    /**Constructor.*/
    public AAB_Box(int width, int height, int depth, int xOffset, int yOffset, int zOffset)
    {super(width, height, depth, xOffset, yOffset, zOffset);}

   /**Construtor. Offsets are Dimemsions / 2 by default.*/
   public AAB_Box(int width, int height, int depth)
   {super(width, height, depth);}

    public @fixed int f_leftContact(){return f_left();}
    public @fixed int f_rightContact(){return f_right();}
    public @fixed int f_backContact(){return f_back();}
    public @fixed int f_frontContact(){return f_front();}
    public @fixed int f_bottomContact(){return f_bottom();}
    public @fixed int f_topContact(){return f_top();}

    @Override
    //Face Getter.
    public Shape_Face[] getFaces()
    {
        return new Shape_Face[]
        {
            //Bottom Face (0).
            new Shape_Face(new Vector3f(0.0f, 0.0f, -1.0f),
                new byte[]{1, 2, 3, 4},
                f_xOffset+width, f_yOffset, f_zOffset,
                f_xOffset, f_yOffset, f_zOffset,
                f_xOffset, f_yOffset+height, f_zOffset,
                f_xOffset+width, f_yOffset+height, f_zOffset
            ),

            //Back Face (1). /\
            new Shape_Face(new Vector3f(0.0f, -1.0f, 0.0f),
                new byte[]{5, 2, 0, 4},
                f_xOffset+width, f_yOffset, f_zOffset+depth,
                f_xOffset, f_yOffset, f_zOffset+depth,
                f_xOffset, f_yOffset, f_zOffset,
                f_xOffset+width, f_yOffset, f_zOffset
            ),

            //Left Face (2). <
            new Shape_Face(new Vector3f(-1.0f, 0.0f, 0.0f),
                new byte[]{5, 3, 0, 1},
                f_xOffset, f_yOffset, f_zOffset+depth,
                f_xOffset, f_yOffset+height, f_zOffset+depth,
                f_xOffset, f_yOffset+height, f_zOffset,
                f_xOffset, f_yOffset, f_zOffset
            ),

            //Front Face (3). \/
            new Shape_Face(new Vector3f(0.0f, 1.0f, 0.0f),
                new byte[]{5, 4, 0, 2},
                f_xOffset, f_yOffset+height, f_zOffset+depth,
                f_xOffset+width, f_yOffset+height, f_zOffset+depth,
                f_xOffset+width, f_yOffset+height, f_zOffset,
                f_xOffset, f_yOffset+height, f_zOffset
            ),

            //Right Face (4). >
            new Shape_Face(new Vector3f(1.0f, 0.0f, 0.0f),
                new byte[]{5, 1, 0, 3},
                f_xOffset+width, f_yOffset+height, f_zOffset+depth,
                f_xOffset+width, f_yOffset, f_zOffset+depth,
                f_xOffset+width, f_yOffset, f_zOffset,
                f_xOffset+width, f_yOffset+height, f_zOffset
            ),

            //Top Face (5).
            new Shape_Face(new Vector3f(0.0f, 0.0f, 1.0f),
                new byte[]{1, 4, 3, 2},
                f_xOffset, f_yOffset, f_zOffset+depth,
                f_xOffset+width, f_yOffset, f_zOffset+depth,
                f_xOffset+width, f_yOffset+height, f_zOffset+depth,
                f_xOffset, f_yOffset+height, f_zOffset+depth
            )
        };
    }

    /**
     * Yes, I realize that I am ripping off of Java's Rectangle intersect function, but IT'S A BOX.
     * 
     * @param f_thisPosition is this shape's position.
     * @param f_thisVelocity is this shape's velocity.
     * @param B is the Box being checked against.
     * @param f_bPosition is B's position.
     * @return true if this Box collides with B.
     */
    //public boolean intersects(Vector3f thisPosition, Vector3f thisVelocity, AAB_Box B, Vector3f bPosition)
    public boolean intersects(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, AAB_Box B, fixedVector3 f_bPosition)
    {
        /*
        if(B_width <= 0 || B_height <= 0 || B_depth <= 0
        || this_width <= 0 || this_height <= 0 || this_depth <= 0)
        {
            System.err.println(this_width + " " + this_height + " " + this_depth
            + " " + B_width + " " + B_height + " " + B_depth);
            return false;
        }
        */

        //
        //Initial check.
        //

        @fixed int//Set Left, Back, and Bottom points
        f_this_x = f_thisPosition.x + this.f_xOffset + f_thisVelocity.x,
        f_this_y = f_thisPosition.y + this.f_yOffset + f_thisVelocity.y,
        f_this_z = f_thisPosition.z + this.f_zOffset + f_thisVelocity.z,
        f_B_x = f_bPosition.x + B.f_getXOffset(),
        f_B_y = f_bPosition.y + B.f_getYOffset(),
        f_B_z = f_bPosition.z + B.f_getZOffset();

        @fixed int//Dimensions
        f_this_width = f_this_x + fixed(this.width),
        f_this_height = f_this_y + fixed(this.height),
        f_this_depth = f_this_z + fixed(this.depth),
        f_B_width = f_B_x + B.f_getWidth(),
        f_B_height = f_B_y + B.f_getHeight(),
        f_B_depth = f_B_z + B.f_getDepth();

        //Final check.
        return (f_this_x < f_B_width  && f_this_y < f_B_height && f_this_z < f_B_depth
        && f_this_width > f_B_x && f_this_height > f_B_y && f_this_depth > f_B_z);

        //      overflow || intersect
        //return ((B_width < B_x || B_width > this_x) &&
                //(B_height < B_y || B_height > this_y) &&
                //(B_depth < B_z || B_depth > this_z) &&
                //(this_width < this_x || this_width > B_x) &&
                //(this_height < this_y || this_height > B_y) &&
                //(this_depth < this_z || this_depth > B_z));

        //boolean result = (f_this_x < f_B_width  && f_this_y < f_B_height && f_this_z < f_B_depth
        //&& f_this_width > f_B_x && f_this_height > f_B_y && f_this_depth > f_B_z);

        //System.out.println(result);
        //return result;


        /*
        //Are the boxes colliding before taking velocity into account?
        if(f_this_x < f_B_width  && f_this_y < f_B_height && f_this_z < f_B_depth
        && f_this_width > f_B_x && f_this_height > f_B_y && f_this_depth > f_B_z)
        {
            //Time of contact is 0.
            //f_finalTimeOfContact = 0;

            //Collision was made.
            return true;
        }


        //
        //Sweep check.
        //

        //Calculate relative velocity (interpret as this Box moving quickly into B, not them moving into each other).
        @fixed int
        f_vx = f_bVelocity_x - f_thisVelocity.x,
        f_vy = f_bVelocity_y - f_thisVelocity.y,
        f_vz = f_bVelocity_z - f_thisVelocity.z;


        //X time of impact.
        @fixed int f_timeOfContact_x =
        (f_this_width < f_B_x && f_vx < 0) ? f_div((f_this_width - f_B_x), f_vx) ://This right less than its left and going left.
        (f_B_width < f_this_x && f_vx > 0) ? f_div((f_this_x - f_B_width), f_vx) ://Its right less than this left going right.
        0,//Default.

        f_timeOfLeave_x =
        (f_B_width > f_this_x && f_vx < 0) ? f_div((f_this_x - f_B_width), f_vx) ://Its right greater than this left going left.
        (f_this_width > f_B_x && f_vx > 0) ? f_div((f_this_width - f_B_x), f_vx) ://This right greater than its left and going right.
        f_ONE;//Default.


        //Y time of impact.
        @fixed int f_timeOfContact_y =
        (f_this_height < f_B_y && f_vy < 0) ? f_div((f_this_height - f_B_y), f_vy) ://This front less than its back and going back.
        (f_B_height < f_this_y && f_vy > 0) ? f_div((f_this_y - f_B_height), f_vy) ://Its front less than this back going front.
        0,//Default.

        f_timeOfLeave_y =
        (f_B_height > f_this_y && f_vy < 0) ? f_div((f_this_y - f_B_height), f_vy) ://Its front greater than this back going back.
        (f_this_height > f_B_y && f_vy > 0) ? f_div((f_this_height - f_B_y), f_vy) ://This front greater than its back and going front.
        f_ONE;//Default.


        //Z time of impact.
        @fixed int f_timeOfContact_z =
        (f_this_depth < f_B_z && f_vz < 0) ? f_div((f_this_depth - f_B_z), f_vz) ://This top less than its bottom and going bottom.
        (f_B_depth < f_this_z && f_vz > 0) ? f_div((f_this_z - f_B_depth), f_vz) ://Its top less than this bottom going top.
        0,//Default.

        f_timeOfLeave_z =
        (f_B_depth > f_this_z && f_vz < 0) ? f_div((f_this_z - f_B_depth), f_vz) ://Its top greater than this bottom going bottom.
        (f_this_depth > f_B_z && f_vz > 0) ? f_div((f_this_depth - f_B_z), f_vz) ://This top greater than its bottom and going top.
        f_ONE;//Default.
        

        //Time of contact is the latest of the times.
        @fixed int f_finalTimeOfContact = StrictMath.max(f_timeOfContact_x, StrictMath.max(f_timeOfContact_y, f_timeOfContact_z));

        //Time of leave is the earliest of the times.
        @fixed int f_finalTimeOfLeave = StrictMath.min(f_timeOfLeave_x, StrictMath.min(f_timeOfLeave_y, f_timeOfLeave_z));

        //A collision only could have occured if the time of contact happened before the time of leave.
        return f_finalTimeOfContact <= f_finalTimeOfLeave;
        */
    }

    /**Checks if this Box is intersecting a Slope.*/
    public boolean intersects(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Slope_Triangle st, fixedVector3 f_stPosition)
    {
        @fixed int //Set Left, Back, and Bottom points
        f_this_x = f_thisPosition.x + this.f_xOffset,
        f_this_y = f_thisPosition.y + this.f_yOffset,
        f_this_z = f_thisPosition.z + this.f_zOffset,
        f_ST_x = f_stPosition.x + st.f_getXOffset(),
        f_ST_y = f_stPosition.y + st.f_getYOffset(),
        f_ST_z = f_stPosition.z + st.f_getZOffset();

        @fixed int //Dimensions
        f_this_width = f_this_x + fixed(this.width),
        f_this_height = f_this_y + fixed(this.height),
        f_this_depth = f_this_z + fixed(this.depth),
        f_ST_width = f_ST_x + fixed(st.getWidth()),
        f_ST_height = f_ST_y + fixed(st.getHeight());

        //Check if X and Y Axes intersect.
        if(f_this_x + f_thisVelocity.x < f_ST_width && f_this_y + f_thisVelocity.y < f_ST_height
        && f_this_width + f_thisVelocity.x > f_ST_x && f_this_height + f_thisVelocity.y > f_ST_y//)
        && f_this_depth + f_thisVelocity.z > f_ST_z)
        {
            //Get needed corner.
            //float XYpoint = 0f, XYpointVEL = 0f, XYlimit = 0f;
            @fixed int f_XYpoint = 0, f_XYpointVEL = 0, f_XYlimit = 0;
            switch(st.getType())
            {
                case LEFT:
                {
                    f_XYpoint = (f_this_width - f_ST_x);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.x, st.f_getNormZ());
                    f_XYlimit = fixed(st.getWidth());
                }
                break;
                case RIGHT:
                {
                    f_XYpoint = (f_this_x - f_ST_x);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.x, st.f_getNormZ());
                    f_XYlimit = fixed(st.getWidth());
                }
                break;

                case UP:
                {
                    f_XYpoint = (f_this_height - f_ST_y);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.y, st.f_getNormZ());
                    f_XYlimit = fixed(st.getHeight());
                }
                break;
                case DOWN:
                {
                    f_XYpoint = (f_this_y - f_ST_y);
                    f_XYpointVEL = f_XYpoint + f_mul(f_thisVelocity.y, st.f_getNormZ());
                    f_XYlimit = fixed(st.getHeight());
                }
                break;
            }
            

            //y = mx + b, limited to between 0 and Depth.
            //Set Z cross point for collision response purposes.
            @fixed int f_crossZ = f_mul(st.f_getXYtoZ_Slope(), f_XYpointVEL) + st.f_getZ_Intercept();

            //f_print("fz", f_crossZ);

            @fixed int f_stGetDepth = fixed(st.getDepth());
            if(f_crossZ < st.f_getZSlope_Bottom()){f_crossZ = st.f_getZSlope_Bottom();}
            else if(f_crossZ > f_stGetDepth){f_crossZ = f_stGetDepth;}
            st.f_setCurrentCross_Z(f_crossZ);
            

            //Same for XY.
            if(f_XYpointVEL < 0){f_XYpointVEL = 0;}
            else if(f_XYpointVEL > f_XYlimit){f_XYpointVEL = f_XYlimit;}
            st.f_setCurrentCross_XY(f_XYpointVEL);

            @fixed int f_ST_depth = f_ST_z + f_crossZ;



            //y = mx + b, 
            //Get the Z points of this box's current and potential positions.
            //float cz0 = (st.getXYtoZ_Slope() * XYpoint) + st.getZStart(),
            //cz1 = (st.getXYtoZ_Slope() * XYpointVEL) + st.getZStart();

            //Get the midway point of the two points and limit the result between 0 and Depth.
            //float crossZ = cz0 + ((cz1 - cz0) / 2);
            //if(crossZ < 0){crossZ = 0;}
            //else if(crossZ > st.getDepth()){crossZ = st.getDepth();}

            //st.setCurrentCross_Z(crossZ);
            //st.setCurrentCross_XY(XYpoint + ((XYpointVEL-XYpoint)/2));
            //float ST_depth = ST_z + crossZ;

            //Same check as AABB to AABB.
            return (f_this_z + f_thisVelocity.z < f_ST_depth);
            //&& this_depth + zVelocity > ST_z);
        }
        return false;
    }


    /**
     * Checks if this Box is intersecting the given Cylinder.

     * @param f_thisPosition
     * @param f_thisVelocity
     * @param c
     * @param f_cPosition
     * @return
     */
    public boolean intersects(final fixedVector3 f_thisPosition, final fixedVector3 f_thisVelocity, final Cylinder c, final fixedVector3 f_cPosition)
    {
        //Declare Z-Points.
        @fixed int
        f_this_z = f_thisPosition.z + f_thisVelocity.z + f_zOffset,
        f_this_depth = f_this_z + fixed(this.depth),
        //
        f_C_z = f_cPosition.z + c.f_getZOffset(),
        f_C_depth = f_cPosition.z + c.f_getZOffset() + c.f_getDepth();

        //Check if Z_Axes intersect.
        if(f_this_z < f_C_depth && f_this_depth > f_C_z)
        {
            //Declare this Box's points.
            @fixed int
            f_this_x = f_thisPosition.x + f_thisVelocity.x + f_xOffset,
            f_this_y = f_thisPosition.y + f_thisVelocity.y + f_yOffset,
            //
            f_this_width = f_this_x + fixed(width),
            f_this_height = f_this_y + fixed(height);

            //Declare Cylinder's points.
            @fixed int
            f_C_x = f_cPosition.x + c.f_getXOffset(),
            f_C_y = f_cPosition.y + c.f_getYOffset();


            //
            //Get the box's closest point to Cylinder center by clamping.
            //
            @fixed int
            f_closePointX = StrictMath.max(f_this_x, StrictMath.min(f_C_x, f_this_width)),
            f_closePointY = StrictMath.max(f_this_y, StrictMath.min(f_C_y, f_this_height));

            //Now, begin Pythagorean Therom.
            @fixed int
            f_sideX = f_closePointX - f_C_x,
            f_sideY = f_closePointY - f_C_y;
            @fixed long f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);

            //If the distance is between the point (0) and the radius, there is a collision.
            @fixed int f_cRadius = c.f_getRadius();
            if(f_sqrLength < f_squareL(f_cRadius))
            {
                /*
                if(f_sqrLength == 0)
                {
                    f_sideX = f_closePointX - (f_this_x - f_thisVelocity.x);
                    f_sideY = f_closePointY - (f_this_y - f_thisVelocity.y);
                    f_sqrLength = f_squareL(f_sideX) + f_squareL(f_sideY);
                }
                */


                @fixed int f_unitX = 0, f_unitY = 0;
                if(f_sqrLength > 0)
                {
                    //Square root to get length.
                    @fixed int f_length = f_sqrt(f_sqrLength);

                    f_unitX = f_divRound_Precision(f_sideX, f_length);
                    f_unitY = f_divRound_Precision(f_sideY, f_length);

                    if(f_abs(f_unitX) > f_ONE){f_unitX = f_ONE | ((f_unitX & f_SIGN_PORTION) >> f_WHOLE_BITS);}
                    if(f_abs(f_unitY) > f_ONE){f_unitY = f_ONE | ((f_unitY & f_SIGN_PORTION) >> f_WHOLE_BITS);}
                }

                //Set Unit Vector for Collision Response purposses.
                c.f_setUnitVector
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


    /**Checks if this Box is intersecting another Isosceles Triangle.*/
    public boolean intersects(final fixedVector3 f_thisPosition, final fixedVector3 f_thisVelocity,
    final Isosceles_Triangle iso, final fixedVector3 f_isoPosition, final byte isoEnds)
    {
        /*
        if(ISO_width <= 0 || ISO_height <= 0 || ISO_depth <= 0
        || this_width <= 0 || this_height <= 0 || this_depth <= 0)
        {
            System.err.println(this_width + " " + this_height + " " + this_depth
            + " " + ISO_width + " " + ISO_height + " " + ISO_depth);
            return false;
        }
        */

        @fixed int//Set Left, Back, and Bottom points.
        f_this_x = f_thisPosition.x + this.f_xOffset,
        f_this_y = f_thisPosition.y + this.f_yOffset,
        f_this_z = f_thisPosition.z + this.f_zOffset + f_thisVelocity.z,
        f_ISO_x = f_isoPosition.x + iso.f_getXOffset(),
        f_ISO_y = f_isoPosition.y + iso.f_getYOffset(),
        f_ISO_z = f_isoPosition.z + iso.f_getZOffset();

        @fixed int//Dimensions.
        f_this_width = f_this_x + fixed(this.width),
        f_this_height = f_this_y + fixed(this.height),
        f_this_depth = f_this_z + fixed(this.depth),
        f_ISO_width = f_ISO_x + iso.f_getWidth(),
        f_ISO_height = f_ISO_y + iso.f_getHeight(),
        f_ISO_depth = f_ISO_z + iso.f_getDepth();

        //Start with AABB Z check.
        if(f_this_z < f_ISO_depth && f_this_depth > f_ISO_z
        && f_this_x + f_thisVelocity.x < f_ISO_width && f_this_width + f_thisVelocity.x > f_ISO_x
        && f_this_y + f_thisVelocity.y < f_ISO_height && f_this_height + f_thisVelocity.y > f_ISO_y)
        {
            //Which corner will collide with the slant?
            @fixed int f_x_PointToCheck, f_y_PointToCheck;
            if(iso.isDown())
            {
                f_x_PointToCheck = ((iso.f_get_XtoY_Slope() > 0) ? f_this_width : f_this_x);
                f_y_PointToCheck = f_this_y;
            }
            else
            {
                f_x_PointToCheck = ((iso.f_get_XtoY_Slope() <= 0) ? f_this_width : f_this_x);
                f_y_PointToCheck = f_this_height;
            }

            //System.out.println(iso.getYSlant_Back() + " " + iso.getXSlant_Left());

            @fixed int//Treat slanted wall as a two-point line segment.
            f_segA_x = f_ISO_x + iso.f_getStartX(),
            f_segA_y = f_ISO_y + iso.f_getStartY(),
            //
            f_segB_x = f_ISO_x + iso.f_getEndX(),
            f_segB_y = f_ISO_y + iso.f_getEndY(),
            //
            f_segWidth = f_segB_x - f_segA_x,
            f_segHeight = f_segB_y - f_segA_y;

            //Calculate the side of the line the corner is on.
            @fixed int f_sign =
            f_mul((f_x_PointToCheck - f_segA_x) + f_thisVelocity.x, -f_segHeight) +
            f_mul((f_y_PointToCheck - f_segA_y) + f_thisVelocity.y, f_segWidth);

            //Is the corner on the solid side of the line?
            if( (iso.isDown() && f_sign < 0) || (!iso.isDown() && f_sign > 0) )
            {
                //f_print("f_sign", f_sign);
                //TODO Do we need any more information from here?
                //if(!needResponseInfo){return true;}

                //
                //Try to find the closest point (possibly of intersection) between point's path of movement and the slant.
                //
                @fixed int f_closest_x, f_closest_y;

                //Direction from point A to point B. Equivalent to rotating the face's direction 90 degrees.
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

                //Used for line intersection test.
                //@fixed int f_determinant = f_mul(f_segHeight, f_thisVelocity.x) - f_mul(f_segWidth, f_thisVelocity.y);
                @fixed int f_determinant = f_mul(f_thisVelocity.y, f_segWidth) - f_mul(f_thisVelocity.x, f_segHeight);

                //If the path and slant are paralell or x and y velocities are zero...
                if(f_determinant == 0)
                {
                    //
                    //Use dot product projection from old position to line.
                    //
                    
                    //Create line from current point to Box corner.
                    @fixed int
                    f_ptAC_x = f_x_PointToCheck - f_segA_x,// + (xVelocity * Math.abs(seg_ab_dirX)),
                    f_ptAC_y = f_y_PointToCheck - f_segA_y,// + (yVelocity * Math.abs(seg_ab_dirY)),

                    //Dot Product: Multiply all the X's plus multiiply all the Y's.
                    //Used to "project this point onto the line" and determine how far
                    //down the line the current corner is.
                    f_distanceDownAB = f_mul(f_ptAC_x, f_segAB_dirX) + f_mul(f_ptAC_y, f_segAB_dirY);
                    //System.out.println(pt_ac_x + " " + pt_ac_y + " " + distanceDownAB);

                    //If we're outside point A's side (if it is included in the check).
                    if((isoEnds & 0b01) == 0b01 && f_distanceDownAB <= 0)
                    {
                        //Assume point A is the closest point.
                        f_closest_x = f_segA_x;
                        f_closest_y = f_segA_y;
                    }
                    //If we're beyond point B's side (if it is included in the check).
                    //else if(distanceDownAB > seg_v_length)
                    else if((isoEnds & 0b10) == 0b10 && f_square(f_distanceDownAB) >= f_segV_sqrLength)
                    {
                        //Assume point B is the closest point.
                        f_closest_x = f_segB_x;
                        f_closest_y = f_segB_y;
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
                }
                else
                {
                    //
                    //Test for point of intersection using Cramers's rule.
                    //
                    @fixed int
                    //f_uA = f_mul(f_segWidth, f_y_PointToCheck - f_segA_y)       -      f_mul(f_segHeight, f_x_PointToCheck - f_segA_x);
                    f_uA = f_mul(f_thisVelocity.x, f_segA_y - f_y_PointToCheck)     -     f_mul(f_thisVelocity.y, f_segA_x - f_x_PointToCheck);

                    //@fixed int
                    //f_uB = f_mul(f_thisVelocity.x, f_y_PointToCheck - f_segA_y)    -   f_mul(f_thisVelocity.y, f_x_PointToCheck - f_segA_x);
                    //f_uB = f_mul(f_segWidth, f_segA_y - f_y_PointToCheck)   -   f_mul(f_segHeight, f_segA_x - f_x_PointToCheck);
        
                    //f_print("f_uA", f_uA, "f_uB", f_uB, "f_determinant", f_determinant);
        
                    //f_uA = (f_uA < 0) ? 0 : (f_uA > f_determinant) ? f_determinant : f_uA;
                    //f_uB = (f_uB < 0) ? 0 : (f_uB > f_determinant) ? f_determinant : f_uB;
        
                    //This is where the line would intersect if it was infinite.
                    //f_closest_x = f_div(f_mul(f_uA, f_thisVelocity.x), f_determinant);
                    f_closest_x = f_div(f_mul(f_uA, f_segWidth), f_determinant);
                    //f_closest_y = f_div(f_mul(f_uA, f_thisVelocity.y), f_determinant);
                    f_closest_y = f_div(f_mul(f_uA, f_segHeight), f_determinant);


                    //StartX clamp.
                    if((isoEnds & 0b01) == 0b01 &&
                    (f_segWidth >= 0 && f_closest_x < 0) || (f_segWidth < 0 && f_closest_x >= 0))
                    {f_closest_x = 0;}
                    //EndX clamp.
                    else if((isoEnds & 0b10) == 0b10 && f_abs(f_closest_x) > f_abs(f_segWidth))
                    {f_closest_x = f_segWidth;}

                    //Apply world position.
                    //f_closest_x += f_x_PointToCheck;
                    f_closest_x += f_segA_x;
                    

                    
                    //StartY clamp.
                    if
                    (
                        (isoEnds & 0b01) == 0b01 &&
                        (
                            (f_segHeight >= 0 && f_closest_y < 0) ||
                            (f_segHeight < 0 && f_closest_y >= 0)
                        )
                    )
                    {f_closest_y = 0;}
                    //EndY clamp.
                    else if((isoEnds & 0b10) == 0b10 && f_abs(f_closest_y) > f_abs(f_segHeight))
                    {f_closest_y = f_segHeight;}

                    //Apply world position.
                    //f_closest_y += f_y_PointToCheck;
                    f_closest_y += f_segA_y;

                    //f_print("f_closest_x", f_closest_x - f_x_PointToCheck, "f_closest_y", f_closest_y - f_y_PointToCheck);
                    //f_print("f_closest_x", f_closest_x - f_segA_x, "f_closest_y", f_closest_y - f_segA_y);

                    //if(f_uA < 0 || f_uA > f_determinant || f_uB < 0 || f_uB > f_determinant)
                }

                //Set point of intersection.
                iso.setCurrentCross
                (
                    f_closest_x - (f_isoPosition.x + iso.f_getXOffset()),
                    f_closest_y - (f_isoPosition.y + iso.f_getYOffset())
                );
                return true;
            }
            //If not on the correct side of the line, no collision.
        }
        //If not within box region, no collision.

        return false;
    }

    

    /**Performs Collision against this Box.*/
    @Override
    public boolean performCollision(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Shape3D shape, fixedVector3 f_shapePosition)
    {
        //TODO Use velocity from both shapes?

        //return shape.intersescts_AAB_Box
        //(
            //this, thisPosition.x, thisPosition.y, thisPosition.z,
            //thisVelocity.x, thisVelocity.y, thisVelocity.z,
            //
            //shapePosition.x, shapePosition.y, shapePosition.z
        //);

        //AAB_Box
        if(shape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)shape;

            return intersects(f_thisPosition, f_thisVelocity, box, f_shapePosition);
        }
        //Slope_Triangle
        else if(shape instanceof Slope_Triangle)
        {
            Slope_Triangle st = (Slope_Triangle)shape;

            return intersects(f_thisPosition, f_thisVelocity, st, f_shapePosition);
        }
        //Cylinder
        else if(shape instanceof Cylinder)
        {
            Cylinder c = (Cylinder)shape;

            return intersects(f_thisPosition, f_thisVelocity, c, f_shapePosition);
        }
        //Isosceles_Triangle
        else if(shape instanceof Isosceles_Triangle)
        {
            Isosceles_Triangle iso = (Isosceles_Triangle)shape;

            return intersects(f_thisPosition, f_thisVelocity, iso, f_shapePosition, (byte)0b11);
        }

        //No collision was made.
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

    /*
     * Collision Responses
     */

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
        //Left
        if(collide_Left(shape, f_position.x, f_velocity.x, f_thisPosition.x))
        {putLeft_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
        //Right
        else if(collide_Right(shape, f_position.x, f_velocity.x, f_thisPosition.x))
        {putRight_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}

        //Back
        if(collide_Back(shape, f_position.y, f_velocity.y, f_thisPosition.y))
        {putBack_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
        //Front
        else if(collide_Front(shape, f_position.y, f_velocity.y, f_thisPosition.y))
        {putFront_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}

        //Bottom
        if(collide_Bottom(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {putBottom_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
        //Top
        else if(collide_Top(shape, f_position.z, f_velocity.z, f_thisPosition.z))
        {putTop_Contact(shape, entity, f_position, f_velocity, f_thisPosition);}
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
        @fixed int f_velZX = f_velocity.z + (f_position.z - f_oldPosition.z);

        //Bottom
        if(collide_Bottom(cylinder, f_oldPosition.z, f_velZX, f_thisPosition.z))
        {putBottom_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //Top
        else if(collide_Top(cylinder, f_oldPosition.z, f_velZX, f_thisPosition.z))
        {putTop_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}

        //Everything else. (inside an else case for when length between point and box is 0)
        else
        {
            f_velZX = f_velocity.x + (f_position.x - f_oldPosition.x);
            @fixed int f_velY = f_velocity.y + (f_position.y - f_oldPosition.y);

            //Left
            if(collide_Left(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
            {
                //cylinder.f_setUnitX_right();
                putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition);
            }
            //Right
            else if(collide_Right(cylinder, f_oldPosition.x, f_velZX, f_thisPosition.x))
            {
                //cylinder.f_setUnitX_left();
                putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition);
            }

            //Back
            else if(collide_Back(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
            {
                //cylinder.f_setUnitY_front();
                putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition);
            }
            //Front
            else if(collide_Front(cylinder, f_oldPosition.y, f_velY, f_thisPosition.y))
            {
                //cylinder.f_setUnitY_back();
                putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition);
            }
        }
    }

    /**This Collision Response only pushs a CollisionObject out if it wasn't already inside it.*/
    public void putOutComposite(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    fixedVector3 f_thisPosition)
    {
        if(shape instanceof Cylinder){putCylinder_OutComposite((Cylinder)shape, entity, f_position, f_velocity, f_oldPosition, f_thisPosition);}
        else{putBox_OutComposite(shape, entity, f_position, f_velocity, f_thisPosition);}
    }
    

    /*
     * Render Functions.
     */

    private Vector4f lightColor = new Vector4f(0.0f, 0.8f, 0.0f, 1.0f),
    //outlineColor = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f),
    darkColor = new Vector4f(0.0f, 0.5f, 0.0f, 1.0f);

    @Override
    /**Renders this AABB as a series of lines.*/
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {
        int
        x = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        y = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        z = (int)(f_toFloat(f_position.z + f_zOffset) * scale),
        w = (int)(f_toFloat(f_position.x + fixed(this.width) + f_xOffset) * scale),
        h = (int)(f_toFloat(f_position.y + fixed(this.height) + f_yOffset) * scale),
        d = (int)(f_toFloat(f_position.z + fixed(this.depth) + f_zOffset) * scale);

        //Bottom Rect
        screen.drawLine(x, y, z, w, y, z, darkColor, true);
        screen.drawLine(x, h, z, w, h, z, darkColor, true);

        //Front and Back
        screen.drawLine(x, h, z, x, y, d, lightColor, true);
        screen.drawLine(w, h, z, w, y, d, lightColor, true);

        //Top Rect
        screen.drawLine(x, y, d, w, y, d, lightColor, true);
        screen.drawLine(x, h, d, w, h, d, lightColor, true);
    }

    @Override
    /**Renders the AABB as a Tile.*/
    public void tileRender(Screen screen, float scale, @fixed int f_x, @fixed int f_y, @fixed int f_z, boolean fixed)
    {
        int
        x = (int)(f_toFloat(f_x + f_xOffset) * scale),
        y = (int)(f_toFloat(f_y + f_yOffset) * scale),
        z = (int)(f_toFloat(f_z + f_zOffset) * scale),
        w = (int)(f_toFloat(f_x + f_xOffset + fixed(this.width)) * scale),
        h = (int)(f_toFloat(f_y + f_yOffset + fixed(this.height)) * scale),
        d = (int)(f_toFloat(f_z + f_zOffset + fixed(this.depth)) * scale);

        //Front Wall.
        screen.fillRect(x, h, z, w-x, 0, d-z, darkColor, fixed);

        //Top Floor.
        screen.fillRect(x, y, d, w-x, h-y, 0, lightColor, fixed);
    }
}
