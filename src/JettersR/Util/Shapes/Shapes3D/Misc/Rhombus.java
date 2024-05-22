package JettersR.Util.Shapes.Shapes3D.Misc;
/**
 * 
 */
//import org.joml.Vector3f;

import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.*;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

import org.joml.Vector4f;

public class Rhombus extends Shape_Box
{
    public static final byte TYPE_UP = 0, TYPE_DOWN = 1;

    //
    private byte type;

    //Height of the flat ends of the Rhombus.
    private int baseHeight;

    //
    private @fixed int f_YtoZ_slope, f_ZtoY_slope;

    private @fixed int f_zIntercept;
    
    /**Constructor.*/

    /*
    public Rhombus(int width, int height, int depth)
    {
        super(width, height, depth, 0, 0, 0);
        //
        @fixed int f_height = fixed(this.height);
        //
        this.f_slope = f_div( fixed(this.depth), f_height );
        this.baseHeight = f_toInt( f_div(f_height, f_slope) );
    }
    */

    /**Constructor.*/
    public Rhombus(int width, int baseHeight, @fixed int f_YtoZ_slope, int depth, byte type, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset)
    {
        super(width, f_toInt(f_div(f_ONE, f_YtoZ_slope) * depth) + baseHeight, depth, f_xOffset, f_yOffset, f_zOffset);
        this.type = type;
        //
        this.f_zIntercept = (type == TYPE_DOWN) ? fixed(depth) : 0;
        //
        this.f_YtoZ_slope = f_YtoZ_slope;
        this.f_ZtoY_slope = f_div(f_ONE, f_YtoZ_slope);
        this.baseHeight = baseHeight;
    }

    /**Constructor.*/
    public Rhombus(int width, int baseHeight, @fixed int f_slope, int depth, byte type)
    {this(width, baseHeight, f_slope, depth, type, 0, 0, 0);}

    public @fixed int f_getSlope(){return f_YtoZ_slope;}
    public int getBaseHeight(){return baseHeight;}
    public @fixed int f_getBaseHeight(){return fixed(baseHeight);}

    /**
     * 
     * @param width
     * @param baseHeight
     * @param depth
     */
    public void setDimensions(int width, int baseHeight, int depth)
    {
        this.width = width;
        this.baseHeight = baseHeight;
        this.height = f_toInt(f_div(f_ONE, f_YtoZ_slope) * depth) + baseHeight;
        this.depth = depth;
    }

    @Override
    public @fixed int f_leftContact(){return 0;}
    public @fixed int f_rightContact(){return 0;}

    @Override
    public @fixed int f_backContact(){return 0;}
    public @fixed int f_frontContact(){return 0;}

    @Override
    public @fixed int f_bottomContact(){return 0;}
    public @fixed int f_topContact(){return 0;}

    @Override
    public Shape_Face[] getFaces()
    {
        //TODO
        return null;
    }

    /**
     * Box intersection test.
     * 
     * @param f_thisPosition
     * @param f_thisVelocity
     * @param B
     * @param f_bPosition
     * @return
     */
    public boolean intersects(fixedVector3 f_thisPosition,// fixedVector3 f_thisVelocity,
    AAB_Box B, fixedVector3 f_bPosition)
    {
        @fixed int//Set Left and Back points.
        f_this_x = f_thisPosition.x// + f_thisVelocity.x
        + this.f_xOffset,
        f_this_y = f_thisPosition.y// + f_thisVelocity.y
        + this.f_yOffset,
        //
        f_B_x = f_bPosition.x + B.f_getXOffset(),
        f_B_y = f_bPosition.y + B.f_getYOffset();

        @fixed int//Dimensions.
        f_this_width = f_this_x + fixed(this.width),
        f_this_height = f_this_y + fixed(this.height),
        f_B_width = f_B_x + B.f_getWidth(),
        f_B_height = f_B_y + B.f_getHeight();

        //Start with AABB x and y check.
        if(f_this_x < f_B_width  && f_this_y < f_B_height
        && f_this_width > f_B_x && f_this_height > f_B_y)
        {
            @fixed int f_td = fixed(this.depth);

            @fixed int//Z Dimensions.
            f_this_z = f_thisPosition.z + this.f_zOffset,
            //
            f_B_z = f_bPosition.z + B.f_getZOffset(),
            f_B_depth = f_B_z + B.f_getDepth();

            //Where on the two Z-lines is the Box?
            @fixed int f_topZ = 0, f_bottomZ = 0;

            switch(type)
            {
                case TYPE_UP:
                {
                    //Use front of box to get topZ.
                    f_topZ = f_mul(f_YtoZ_slope, (f_B_height - f_this_y)) + f_zIntercept;

                    //Use back of box to get topZ.
                    f_bottomZ = f_mul(f_YtoZ_slope, (f_B_y - f_this_y)) + (f_zIntercept - (f_YtoZ_slope * baseHeight));
                }
                break;

                case TYPE_DOWN:
                {
                    //Use front of box to get topZ.
                    f_topZ = f_mul(f_YtoZ_slope, (f_B_y - f_this_y)) + f_zIntercept;

                    //Use back of box to get topZ.
                    f_bottomZ = f_mul(f_YtoZ_slope, (f_B_height - f_this_y)) + (f_zIntercept + fixed(baseHeight));
                }
                break;
            }

            //Clamp topZ.
            if(f_topZ < 0){f_topZ = 0;}
            else if(f_topZ > f_td){f_topZ = f_td;}

            //Clamp bottomZ.
            if(f_bottomZ < 0){f_bottomZ = 0;}
            else if(f_bottomZ > f_td){f_bottomZ = f_td;}

            //AABB z-check to finish.
            return (f_B_z - f_this_z < f_topZ && f_B_depth - f_this_z > f_bottomZ);
        }

        //It ain't even on the right x or y coordinate.
        return false;
    }

    @fixed int f_bottomZ = 0, f_topZ = 0;

    /**Rounded Box intersection.*/
    public boolean intersects(fixedVector3 f_thisPosition,// fixedVector3 f_thisVelocity,
    AAB_RoundedBox R, fixedVector3 f_rPosition)
    {
        @fixed int//Set Left and Back points.
        f_this_x = f_thisPosition.x// + f_thisVelocity.x
        + this.f_xOffset,
        f_this_y = f_thisPosition.y// + f_thisVelocity.y
        + this.f_yOffset,
        //
        f_R_x = f_rPosition.x + R.f_getXOffset(),
        f_R_y = f_rPosition.y + R.f_getYOffset();

        @fixed int//Dimensions.
        f_this_width = f_this_x + fixed(this.width),
        f_this_height = f_this_y + fixed(this.height),
        f_R_width = f_R_x + R.f_getWidth(),
        f_R_height = f_R_y + R.f_getHeight();

        //Start with AABB x and y check.
        if(f_this_x < f_R_width  && f_this_y < f_R_height
        && f_this_width > f_R_x && f_this_height > f_R_y)
        {
            @fixed int f_td = fixed(this.depth);

            @fixed int//Z Dimensions.
            f_this_z = f_thisPosition.z + this.f_zOffset,
            //
            f_R_z = f_rPosition.z + R.f_getZOffset(),
            f_R_depth = f_R_z + R.f_getDepth();

            //Where on the two Z-lines is the Box?
            //@fixed int f_topZ = 0, f_bottomZ = 0;

            switch(type)
            {
                case TYPE_UP:
                {
                    //Use front of box to get topZ.
                    f_topZ = f_mul(f_YtoZ_slope, (f_R_height - f_this_y)) + f_zIntercept;

                    //Use back of box to get topZ.
                    f_bottomZ = f_mul(f_YtoZ_slope, (f_R_y - f_this_y)) + (f_zIntercept - (f_YtoZ_slope * baseHeight));
                }
                break;

                case TYPE_DOWN:
                {
                    //Use front of box to get topZ.
                    f_topZ = f_mul(f_YtoZ_slope, (f_R_y - f_this_y)) + f_zIntercept;

                    //Use back of box to get topZ.
                    f_bottomZ = f_mul(f_YtoZ_slope, (f_R_height - f_this_y)) + (f_zIntercept + fixed(baseHeight));
                }
                break;
            }

            //Clamp topZ.
            if(f_topZ < 0){f_topZ = 0;}
            else if(f_topZ > f_td){f_topZ = f_td;}

            //Clamp bottomZ.
            if(f_bottomZ < 0){f_bottomZ = 0;}
            else if(f_bottomZ > f_td){f_bottomZ = f_td;}

            //f_print("R_Back", f_R_y - f_this_y, "R_Front", f_R_height - f_this_y);
            //f_print
            //(
                //"R_Bottom", f_R_z - f_this_z,
                //"Top", f_topZ,
                //"R_Top", f_R_depth - f_this_z,
                //"Bottom", f_bottomZ
            //);

            //AABB z-check to finish.
            return (f_R_z - f_this_z < f_topZ && f_R_depth - f_this_z > f_bottomZ);

            //return true;
        }

        //It ain't even on the right x or y coordinate.
        return false;
    }

    /**Sphere intersection.*/
    public boolean intersects(float x, float y, float z, Sphere S, float sX, float sY, float sZ)
    {
        float//Set Left, Back, and Bottom points
        this_x = x + this.f_xOffset,
        this_y = y + this.f_yOffset,
        //
        S_x = sX + S.f_left(),
        S_y = sY + S.f_back();

        float//Dimensions
        this_width = this_x + this.width,
        this_height = this_y + this.height,
        //
        S_width = sX + S.f_right(),
        S_height = sY + S.f_front();

        if(this_x < S_width  && this_y < S_height
        && this_width > S_x && this_height > S_y)
        {
            float//Z Dimensions.
            this_z = z + this.f_zOffset,
            //this_depth = this_z + this.depth,
            //
            S_z = sZ + S.f_bottom(),
            S_depth = sZ + S.f_top();

            //Get Sphere front and back relative to rhombus.
            float S_front = (S_height - this_y), S_back = (S_y - this_y);

            //Get Z Cross Values to check against.
            float backZ0 = (f_YtoZ_slope * S_front),
            frontZ0 = (f_YtoZ_slope * S_back) - baseHeight;
            backZ0 = (backZ0 < 0) ? 0 : (backZ0 > depth) ? depth : backZ0;
            frontZ0 = (frontZ0 < 0) ? 0 : (frontZ0 > depth) ? depth : frontZ0;

            float backZ1 = backZ0 + S.getDepth(),
            frontZ1 = frontZ0 + S.getDepth() - baseHeight;
            backZ1 = (backZ1 < 0) ? 0 : (backZ1 > depth) ? depth : backZ1;
            frontZ1 = (frontZ1 < 0) ? 0 : (frontZ1 > depth) ? depth : frontZ1;

            //Similar check to AABB to AABB.
            return (S_depth - this_z > backZ0 && S_z - this_z < backZ1
            && S_depth - this_z > frontZ0 && S_z - this_z < frontZ1);
        }

        //It ain't even on the right x or y coordinate.
        return false;
    }


    /**
     * Spherical Sector intersection.
     * 
     * @param f_thisPosition
     * @param SS
     * @param f_ssPosition
     * @return
     */
    public boolean intersects(fixedVector3 f_thisPosition, SphericalSector SS, fixedVector3 f_ssPosition)
    {
        @fixed int//Rhombus points.
        f_this_x = f_thisPosition.x + this.f_xOffset,
        f_this_y = f_thisPosition.y + this.f_yOffset,
        f_this_z = f_thisPosition.z + this.f_zOffset,
        //
        f_this_width = f_this_x + fixed(this.width),
        f_this_height = f_this_y + fixed(this.height),
        f_this_depth = f_this_z + fixed(this.depth);

        @fixed int//Sector Points.
        f_SS_x = f_ssPosition.x + SS.f_getXOffset(),
        f_SS_y = f_ssPosition.y + SS.f_getYOffset(),
        f_SS_z = f_ssPosition.z + SS.f_getZOffset();


        //
        //Range Check.
        //

        //Get the box's closest point to the Sector's center by clamping.
        @fixed int
        f_closePointX = StrictMath.max(f_this_x, StrictMath.min(f_SS_x, f_this_width)),
        f_closePointY = StrictMath.max(f_this_y, StrictMath.min(f_SS_y, f_this_height)),
        f_closePointZ = StrictMath.max(f_this_z, StrictMath.min(f_SS_z, f_this_depth));

        //Now, begin Pythagorean Therom.
        @fixed int
        f_sideX = f_SS_x - f_closePointX,
        f_sideY = f_SS_y - f_closePointY,
        f_sideZ = f_SS_z - f_closePointZ;
        @fixed long f_sqrLengthToSec = f_squareL(f_sideX) + f_squareL(f_sideY) + f_squareL(f_sideZ);
        @fixed int f_ssLength = SS.f_getLength();

        //Dead center check.
        if(f_sqrLengthToSec <= 0){return true;}

        //If the distance is between the point (0) and the radius, the box is within range.
        //Otherwise, this check can stop here.
        else if(f_sqrLengthToSec < f_squareL(f_ssLength))
        {
            //
            //Angle check.
            //

            //Get the direction from point to source of Sector.
            @fixed int f_lengthToSec = f_sqrt(f_sqrLengthToSec),
            f_pointToSec_X = f_div(f_sideX, f_lengthToSec),
            f_pointToSec_Y = f_div(f_sideY, f_lengthToSec),
            f_pointToSec_Z = f_div(f_sideZ, f_lengthToSec);

            //Get the Dot Product from point to Sector source.
            fixedVector3 f_ssDirection = SS.f_getDirection();
            @fixed int f_theta = f_mul(f_pointToSec_X, -f_ssDirection.x) + f_mul(f_pointToSec_Y, -f_ssDirection.y) + f_mul(f_pointToSec_Z, -f_ssDirection.z);

            //f_print("px", f_pointToSec_X, "py", f_pointToSec_Y, "pz", f_pointToSec_Z);

            //If theta is within the cutoff angle, return true now.dw
            //Degrees to Radians: cos(180) = -1, cos(90) = 0, cos(45) = 0.707.
            @fixed int f_cosAngle = SS.f_getCosAngle();
            if(f_theta > f_cosAngle){return true;}
            //Otherwise...
            else
            {
                //f_print("thet", f_theta, "cang", f_cosAngle);

                //This time, get the box's closest point to the Sector's apex by clamping.
                fixedVector3 f_ss_componentDistances = SS.f_getComponentDistances();                
                f_closePointX = StrictMath.max(f_this_x, StrictMath.min(f_SS_x + f_ss_componentDistances.x, f_this_width));
                f_closePointY = StrictMath.max(f_this_y, StrictMath.min(f_SS_y + f_ss_componentDistances.y, f_this_height));
                f_closePointZ = StrictMath.max(f_this_z, StrictMath.min(f_SS_z + f_ss_componentDistances.z, f_this_depth));

                //Distances to source point.
                f_sideX = f_SS_x - f_closePointX;
                f_sideY = f_SS_y - f_closePointY;
                f_sideZ = f_SS_z - f_closePointZ;


                //
                //Final point check.
                //

                //Calculate the new length.
                f_lengthToSec = f_sqrt(f_squareL(f_sideX) + f_squareL(f_sideY) + f_squareL(f_sideZ));

                //Calculate new direction.
                f_pointToSec_X = f_div(f_sideX, f_lengthToSec);
                f_pointToSec_Y = f_div(f_sideY, f_lengthToSec);
                f_pointToSec_Z = f_div(f_sideZ, f_lengthToSec);
                
                //Get the Dot Product from this point to Sector source.
                f_theta = f_mul(f_pointToSec_X, -f_ssDirection.x) + f_mul(f_pointToSec_Y, -f_ssDirection.y) + f_mul(f_pointToSec_Z, -f_ssDirection.z);

                //Returns true if theta is within the cone's cutoff angle.
                return (f_theta > f_cosAngle);
            }
        }

        //It wasn't in range.
        return false;

        //TODO Actually test this.
    }
    

    @Override
    public boolean performCollision(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Shape3D shape, fixedVector3 f_shapePosition)
    {
        //AAB_Box
        if(shape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)shape;

            if
            (
                intersects(f_thisPosition,// f_thisVelocity,
                box, f_shapePosition)
            )
            {
                //box.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        //AAB_RoundedBox
        else if(shape instanceof AAB_RoundedBox)
        {
            AAB_RoundedBox rBox = (AAB_RoundedBox)shape;

            if
            (
                intersects(f_thisPosition,// f_thisVelocity,
                rBox, f_shapePosition)
            )
            {
                //rBox.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        /*
        //Sphere
        else if(shape instanceof Sphere)
        {
            Sphere sphere = (Sphere)shape;

            if
            (
                intersects(f_thisPosition.x + f_thisVelocity.x, f_thisPosition.y + f_thisVelocity.y, f_thisPosition.z + f_thisVelocity.z,
                sphere, f_shapePosition.x, f_shapePosition.y, f_shapePosition.z)
            )
            {
                //sphere.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        */
        //Cone
        else if(shape instanceof SphericalSector)
        {
            SphericalSector ss = (SphericalSector)shape;

            if
            (
                intersects(f_thisPosition, ss, f_shapePosition)
            )
            {
                //ss.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }

        //No collision was made.
        return false;
    }

    public final boolean tileCollidedBy(Shape3D itsShape, fixedVector3 f_itsPosition, fixedVector3 f_itsVelocity, fixedVector3 f_thisPosition, byte thisTileForces)
    {return false;}

    @Override
    public void putBox_OutComposite(Shape3D shape, Entity entity, fixedVector3 position, fixedVector3 velocity, fixedVector3 thisPosition)
    {
        
    }

    @Override
    public void putCylinder_OutComposite(Cylinder cylinder, Entity entity, fixedVector3 position, fixedVector3 velocity, fixedVector3 f_oldPosition, fixedVector3 thisPosition)
    {
        
    }

    private Vector4f lightColor = new Vector4f(0.0f, 0.8f, 0.0f, 1.0f),
    redColor = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f),
    yellowColor = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f),
    purpleColor = new Vector4f(0.5f, 0.0f, 1.0f, 1.0f),
    orangeColor = new Vector4f(1.0f, 0.5f, 0.0f, 1.0f);

    @Override
    /**For debugging purposes. */
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {
        int
        x = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        y0 = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        y1 = (int)(f_toFloat(f_position.y + f_yOffset + (f_ZtoY_slope * depth)) * scale),
        z = (int)(f_toFloat(f_position.z + f_zOffset) * scale),
        //
        ww = (int)(f_toFloat(f_position.x + fixed(width) + f_xOffset) * scale),
        h0 = (int)(f_toFloat(f_position.y + fixed(baseHeight) + f_yOffset) * scale),
        h1 = (int)(f_toFloat(f_position.y + fixed(baseHeight) + f_yOffset + (f_ZtoY_slope * depth)) * scale),
        d = (int)(f_toFloat(f_position.z + fixed(depth) + f_zOffset) * scale),
        w = (int)(width * scale),
        h = (int)(baseHeight * scale);

        //Bottom Rect
        screen.drawRect(x, y0, z, w, h, redColor, true);

        //Line connecting bottom and top rects.
        screen.drawLine(x, y0, z, x, y1, d, lightColor, true);
        screen.drawLine(ww, y0, z, ww, y1, d, lightColor, true);
        screen.drawLine(x, h0, z, x, h1, d, lightColor, true);
        screen.drawLine(ww, h0, z, ww, h1, d, lightColor, true);

        //Top Rect
        screen.drawRect(x, y1, d, w, h, yellowColor, true);

        screen.drawPoint(x + 100, y0, z + f_toInt(f_bottomZ), purpleColor, true);
        screen.drawPoint(x + 100, y0, z + f_toInt(f_topZ), orangeColor, true);
    }

    @Override
    public void tileRender(Screen screen, float scale, @fixed int f_x, @fixed int f_y, @fixed int f_z, boolean fixed)
    {
        
    }
}
