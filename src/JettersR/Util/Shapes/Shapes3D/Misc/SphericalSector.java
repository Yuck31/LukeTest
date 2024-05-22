package JettersR.Util.Shapes.Shapes3D.Misc;
/**
 * 
 */
import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.*;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class SphericalSector extends Shape3D
{
    //Length of sector/Radius of sphere.
    private @fixed int f_length;

    //Direction this sector is facing.
    private fixedVector3 f_direction;

    //Component distances of length and direction. Used for collision checks.
    private fixedVector3 f_componentDistances = new fixedVector3();

    //Angle of sector spread.
    //private @fixed int f_angle;

    //Cosine of angle. Used for collision checks.
    private @fixed int f_cosAngle;

    /**Constructor. NORMALIZE F_DIRECTION YOURSELF.*/
    public SphericalSector(@fixed int f_length, fixedVector3 f_direction, @fixed int f_angleDegrees, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset)
    {
        //Set offsets.
        super(f_xOffset, f_yOffset, f_zOffset);

        //Component distances.
        this.f_length = f_length;
        this.f_direction = f_direction;
        this.f_componentDistances.mulResult(f_length, f_direction);

        //Cosine angle.
        //this.f_angle = f_toRadians(f_angleDegrees);
        this.f_cosAngle = f_cos(f_toRadians(f_angleDegrees));
        //f_print("Ang", f_angle, "Cos", f_cosAngle);
        //(Degrees * pi) / 180 = Radians.
        //(Radians * 180) / pi = Degrees.
    }

    //Distance Getter/Setter.
    public @fixed int f_getLength(){return f_length;}
    public float getLength(){return f_toFloat(f_length);}
    public void f_setLength(@fixed int f_length)
    {
        //Set length.
        this.f_length = f_length;

        //Calculate component distances.
        this.f_componentDistances.mulResult(this.f_length, this.f_direction);
    }


    //Direction Getter/Setter.
    public fixedVector3 f_getDirection(){return f_direction;}
    public Vector3f getDirection3f(){return this.f_direction.toVector3f();}
    public void f_setLength(fixedVector3 f_direction)
    {
        //Set direction.
        this.f_direction.set(f_direction);

        //Calculate component distances.
        this.f_componentDistances.mulResult(this.f_length, this.f_direction);
    }

    //Component Distances getter.
    public fixedVector3 f_getComponentDistances(){return this.f_componentDistances;}

    //Cosine Angle Getter.
    public @fixed int f_getCosAngle(){return f_cosAngle;}
    public float getCosAngle(){return f_toFloat(f_cosAngle);}

    //Dimension Getters.
    @Override
    public int getWidth(){return f_toInt(f_length*2);}
    public @fixed int f_getWidth(){return (f_length*2);}
    //
    public int getHeight(){return f_toInt(f_length*2);}
    public @fixed int f_getHeight(){return (f_length*2);}
    //
    public int getDepth(){return f_toInt(f_length*2);}
    public @fixed int f_getDepth(){return (f_length*2);}


    @Override
    public @fixed int f_left(){return (f_xOffset - f_length);}
    public @fixed int f_right(){return (f_xOffset + f_length);}
    //
    public @fixed int f_back(){return (f_yOffset - f_length);}
    public @fixed int f_front(){return (f_yOffset + f_length);}
    //
    public @fixed int f_bottom(){return (f_zOffset - f_length);}
    public @fixed int f_top(){return (f_zOffset + f_length);}


    @Override
    public @fixed int f_leftContact(){return 0;}
    public @fixed int f_rightContact(){return 0;}
    //
    public @fixed int f_backContact(){return 0;}
    public @fixed int f_frontContact(){return 0;}
    //
    public @fixed int f_bottomContact(){return 0;}
    public @fixed int f_topContact(){return 0;}

    /*
    bool inside(point p, spherical_sector sec)
    {
        //Get distances of each component.
        vec3 sides = vec3(p - sec.p);

        //Use them to calculate length from Point to origin of Sector.
        float len = length(sides);

        //Is the point within the "Sphere"'s range?
        if (len > sec.distance) return false;

        //(Dot product of component-distances and sector component-distances) / (length * sector length)
        float t = dot(sides, (sec.distance * sec.direction) ) / (len * sec.distance);
        //float t = dot(u, sec.dp) / (l * sec.R);

        return (t >= sec.cosAng);
        //if (t < cos(sec.ang)) return false;
        //return true;
    }
    */


    public boolean intersects(final fixedVector3 f_thisPosition, final fixedVector3 f_point)
    {
        //Get distances of each component.
        @fixed int
        f_sideX = f_point.x - f_thisPosition.x,
        f_sideY = f_point.y - f_thisPosition.y,
        f_sideZ = f_point.z - f_thisPosition.z;

        //Use them to calculate length from Point to origin of Sector.
        @fixed long f_sqrLengthToPoint = f_squareL(f_sideX) + f_squareL(f_sideY) + f_squareL(f_sideZ);

        //Is the point within the "Sphere"'s range?
        if(f_sqrLengthToPoint < f_squareL(this.f_length))
        {
            @fixed int f_lengthToPoint = f_sqrt(f_sqrLengthToPoint);

            //(Dot product of component-distances and sector component-distances) / (length * sector length)
            @fixed int f_theta =
            f_div
            (
                //From -(f_lengthToPoint * this.f_length) to +(f_lengthToPoint * this.f_length).
                f_mul(f_sideX, this.f_componentDistances.x) + f_mul(f_sideY, this.f_componentDistances.y) + f_mul(f_sideZ, this.f_componentDistances.z),
                //Normalize to -1 to +1.
                f_mul(f_lengthToPoint, this.f_length)
            );
            //Theta will be closer to +1 the closer to the exact direction the point is.

            //Returns true if theta is within the cone's cutoff angle.
            //Degrees to Radians: cos(180) = -1, cos(90) = 0, cos(45) = 0.707.
            return (f_theta > this.f_cosAngle);
        }

        //It ain't even within range.
        return false;
    }

    /*
    line closest(point point, line l0)
    {
        //Project point onto line. Dot product of point and line distance components.
        float t = dot(point - l0.p0, l0.dp);

        //Clamp bounds.
        if (t < 0.0) t = 0.0;
        if (t > 1.0) t = 1.0;

        //New line using percentage.
        return line(point, l0.p0 + (l0.dp * t));
    }

    line closest(point point, triangle t0)
    {
        point p;
        line closestLine, ll;
        closestLine.length = 1e300;

        float t = dot(point - t0.p0, t0.n);
        p = point - (t * t0.n);
        
        if((fabs(t) > 1e-6) && (inside(p, t0)))
        {
            ll = line(point, p);
            if(closestLine.length > ll.length){closestLine = ll;}
        }

        //Check line 0.
        ll = closest(point, line(t0.p0, t0.p1));
        if(closestLine.length > ll.length){closestLine = ll;}
        
        //Check line 1.
        ll = closest(point, line(t0.p1, t0.p2));
        if(closestLine.length > ll.length){closestLine = ll;}

        //Check line 2.
        ll = closest(point, line(t0.p2, t0.p0));
        if(closestLine.length > ll.length){closestLine = ll;}
        //
        return closestLine;
    }
    */
        

    /**Box intersection.*/
    public boolean intersects(final fixedVector3 f_thisPosition, final AAB_Box B, final fixedVector3 f_bPosition)
    {
        @fixed int//This Sector's Points.
        f_this_x = f_thisPosition.x + this.f_xOffset,
        f_this_y = f_thisPosition.y + this.f_yOffset,
        f_this_z = f_thisPosition.z + this.f_zOffset;

        @fixed int//Box's points.
        f_B_x = f_bPosition.x + B.f_getXOffset(),
        f_B_y = f_bPosition.y + B.f_getYOffset(),
        f_B_z = f_bPosition.z + B.f_getZOffset(),
        //
        f_B_width = f_B_x + B.f_getWidth(),
        f_B_height = f_B_y + B.f_getHeight(),
        f_B_depth = f_B_z + B.f_getDepth();

        
        //
        //Range Check.
        //

        //Get the box's closest point to the Sector's center by clamping.
        @fixed int
        f_closePointX = StrictMath.max(f_B_x, StrictMath.min(f_this_x, f_B_width)),
        f_closePointY = StrictMath.max(f_B_y, StrictMath.min(f_this_y, f_B_height)),
        f_closePointZ = StrictMath.max(f_B_z, StrictMath.min(f_this_z, f_B_depth));

        //Now, begin Pythagorean Therom.
        @fixed int
        f_sideX = f_this_x - f_closePointX,
        f_sideY = f_this_y - f_closePointY,
        f_sideZ = f_this_z - f_closePointZ;
        @fixed long f_sqrLengthToSec = f_squareL(f_sideX) + f_squareL(f_sideY) + f_squareL(f_sideZ);

        //Dead center check.
        if(f_sqrLengthToSec <= 0){return true;}

        //If the distance is between the point (0) and the radius, the box is within range.
        //Otherwise, this check can stop here.
        else if(f_sqrLengthToSec < f_squareL(this.f_length))
        {
            //
            //Angle check.
            //

            //Get the direction from point to source of Sector.
            @fixed int f_lengthToSec = f_sqrt(f_sqrLengthToSec);//,
            //f_pointToSec_X = f_div(f_sideX, f_lengthToSec),
            //f_pointToSec_Y = f_div(f_sideY, f_lengthToSec),
            //f_pointToSec_Z = f_div(f_sideZ, f_lengthToSec);

            //Get the Dot Product from point to Sector source.
            //@fixed int f_theta = f_mul(f_pointToSec_X, -f_direction.x) + f_mul(f_pointToSec_Y, -f_direction.y) + f_mul(f_pointToSec_Z, -f_direction.z);
            //(Dot product of component-distances and sector component-distances) / (length * sector length)
            @fixed int f_theta =
            f_div
            (
                //From -(f_lengthToPoint * this.f_length) to +(f_lengthToPoint * this.f_length).
                f_mul(f_sideX, this.f_componentDistances.x) + f_mul(f_sideY, this.f_componentDistances.y) + f_mul(f_sideZ, this.f_componentDistances.z),
                //Normalize to -1 to +1.
                f_mul(f_lengthToSec, this.f_length)
            );
            //Theta will be closer to +1 the closer to the exact direction the point is.

            //f_print("px", f_pointToSec_X, "py", f_pointToSec_Y, "pz", f_pointToSec_Z);

            //If theta is within the cutoff angle, return true now.dw
            //Degrees to Radians: cos(180) = -1, cos(90) = 0, cos(45) = 0.707.
            if(f_theta > this.f_cosAngle){return true;}
            //Otherwise...
            else
            {
                //f_print("thet", f_theta, "cang", f_cosAngle);

                //This time, get the box's closest point to the Sector's apex by clamping.             
                f_closePointX = StrictMath.max(f_B_x, StrictMath.min(f_this_x + f_componentDistances.x, f_B_width));
                f_closePointY = StrictMath.max(f_B_y, StrictMath.min(f_this_y + f_componentDistances.y, f_B_height));
                f_closePointZ = StrictMath.max(f_B_z, StrictMath.min(f_this_z + f_componentDistances.z, f_B_depth));

                //Distances to source point.
                f_sideX = f_this_x - f_closePointX;
                f_sideY = f_this_y - f_closePointY;
                f_sideZ = f_this_z - f_closePointZ;


                //
                //Final point check.
                //

                //Calculate the new length.
                f_lengthToSec = f_sqrt(f_squareL(f_sideX) + f_squareL(f_sideY) + f_squareL(f_sideZ));

                //Calculate new direction.
                //f_pointToSec_X = f_div(f_sideX, f_lengthToSec);
                //f_pointToSec_Y = f_div(f_sideY, f_lengthToSec);
                //f_pointToSec_Z = f_div(f_sideZ, f_lengthToSec);
                
                //Get the Dot Product from this point to Sector source.
                //f_theta = f_mul(f_pointToSec_X, -f_direction.x) + f_mul(f_pointToSec_Y, -f_direction.y) + f_mul(f_pointToSec_Z, -f_direction.z);
                f_theta = f_div
                (
                    f_mul(f_sideX, this.f_componentDistances.x) + f_mul(f_sideY, this.f_componentDistances.y) + f_mul(f_sideZ, this.f_componentDistances.z),
                    f_mul(f_lengthToSec, this.f_length)
                );

                //Returns true if theta is within the cone's cutoff angle.
                return (f_theta > f_cosAngle);
            }
        }

        //It wasn't in range.
        return false;

        //TODO Actually test this.
    }


    /**Cylinder intersection.*/
    public boolean intersects(final fixedVector3 f_thisPosition, final Cylinder C, final fixedVector3 f_cPosition)
    {
        @fixed int//This Sector's points.
        f_this_x = f_thisPosition.x + this.f_xOffset,
        f_this_y = f_thisPosition.y + this.f_yOffset,
        f_this_z = f_thisPosition.z + this.f_zOffset;
        
        @fixed int//Cylinder points.
        f_C_x = f_cPosition.x + C.f_getXOffset(),
        f_C_y = f_cPosition.y + C.f_getYOffset(),
        f_C_z = f_cPosition.z + C.f_getZOffset(),
        f_C_depth = f_C_z + C.f_getDepth();

        //Get the Cylinder's closest Z point to this Sector's center by clamping.
        @fixed int f_closePointZ = StrictMath.max(f_C_z, StrictMath.min(f_this_z, f_C_depth));

        //Now, begin Pythagorean Therom.
        @fixed int
        f_sideX = f_C_x - f_this_x,
        f_sideY = f_C_y - f_this_y,
        f_sideZ = f_closePointZ - f_this_z;

        //Calculaate squared length.
        @fixed long f_sqrLengthToSec = f_squareL(f_sideX) + f_squareL(f_sideY) + f_squareL(f_sideZ);

        //Dead center check.
        if(f_sqrLengthToSec <= 0){return true;}

        //If the distance is between the point (0) and this sector's length + cylinder's radius, the cylinder is within range.
        @fixed int f_cRadius = C.f_getRadius();
        if(f_sqrLengthToSec < f_squareL(this.f_length + f_cRadius))
        {
            //Get the direction from point to source of Sector.
            @fixed int
            f_lengthToSec = f_sqrt(f_sqrLengthToSec);//,
            //f_pointToSec_X = f_div(f_sideX, f_lengthToSec),
            //f_pointToSec_Y = f_div(f_sideY, f_lengthToSec),
            //f_pointToSec_Z = f_div(f_sideZ, f_lengthToSec);

            //Get the Dot Product from point to Sector source.
            //@fixed int f_theta = f_mul(f_pointToSec_X, -f_direction.x) + f_mul(f_pointToSec_Y, -f_direction.y) + f_mul(f_pointToSec_Z, -f_direction.z);
            //(Dot product of component-distances and sector component-distances) / (length * sector length)
            @fixed int f_theta =
            f_div
            (
                //From -(f_lengthToPoint * this.f_length) to +(f_lengthToPoint * this.f_length).
                f_mul(f_sideX, this.f_componentDistances.x) + f_mul(f_sideY, this.f_componentDistances.y) + f_mul(f_sideZ, this.f_componentDistances.z),
                //Normalize to -1 to +1.
                f_mul(f_lengthToSec, this.f_length)
            );
            //Theta will be closer to +1 the closer to the exact direction the point is.

            //If theta is within the cutoff angle, intersection returns true.
            if(f_theta > f_cosAngle){return true;}
            else
            {
                //
                //Find a new point to check.
                //

                //Get distance components from Circle origin to Sector apex.
                f_this_z += f_componentDistances.z;
                f_closePointZ = StrictMath.max(f_C_z, StrictMath.min(f_this_z, f_C_depth));
                f_sideX = f_C_x - (f_this_x + f_componentDistances.x);
                f_sideY = f_C_y - (f_this_y + f_componentDistances.y);

                //Get squared length from Circle origin to this Sector's apex.
                f_sqrLengthToSec = f_squareL(f_sideX) + f_squareL(f_sideY);

                //If apex is inside the cylinder, we can stop the test here.
                if(f_this_z < f_C_depth && f_this_z > f_C_z
                && f_sqrLengthToSec < f_squareL(f_cRadius)){return true;}
                
                //Square root sqrLength.
                f_lengthToSec = f_sqrt(f_sqrLengthToSec);

                @fixed int//New point using direction to apex.
                f_pX = f_mul(f_cRadius, f_div(f_sideX, f_lengthToSec)),
                f_pY = f_mul(f_cRadius, f_div(f_sideY, f_lengthToSec));


                //
                //Get direction from point to sector source.
                //

                //Get length to source.
                f_this_z -= f_componentDistances.z;
                f_sideX = f_pX - f_this_x;
                f_sideY = f_pY - f_this_y;
                f_sideZ = f_closePointZ - f_this_z;
                f_lengthToSec = f_sqrt(f_squareL(f_sideX) + f_squareL(f_sideY) + f_squareL(f_sideZ));
                
                //
                //Final check.
                //
                //f_theta = (f_pointToSec_X * -f_direction.x) + (f_pointToSec_Y * -f_direction.y) + (f_pointToSec_Z * -f_direction.z);
                f_theta =
                f_div
                (
                    f_mul(f_sideX, this.f_componentDistances.x) + f_mul(f_sideY, this.f_componentDistances.y) + f_mul(f_sideZ, this.f_componentDistances.z),
                    f_mul(f_lengthToSec, this.f_length)
                );

                //If THIS theta is within the cutoff angle, intersection returns true.
                return (f_theta > f_cosAngle);

                /*
                //Use the unit vectors we have to get a different point to check.
                @fixed int
                f_pX = -f_pointToSec_Y * C.f_getRadius(),
                f_pY = -f_pointToSec_X * C.f_getRadius();
                
                //Recalculate theta and try the test again.
                f_theta = (f_pX * -f_direction.x) + (f_pY * -f_direction.y) + (f_pointToSec_Z * -f_direction.z);

                //If THIS theta is within the cutoff angle, intersection returns true.
                return (f_theta > f_cosAngle);
                */
            }
            //NOTE: We don't need to worry about the circle being bigger than the cone. The first cutoff check would've passed already.
        }

        //It either wasn't in range or not within the cutoff angle.
        return false;
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
                intersects(f_thisPosition, box, f_shapePosition)
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
                intersects(f_thisPosition, cylinder, f_shapePosition)
            )
            {
                //cyliinder.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }

        //No intersection was made.
        return false;
    }

    @Override
    public void putThis_Left_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
            Shape_Box itsShape, fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Left_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Left_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Right_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
            Shape_Box itsShape, fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Right_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Right_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Back_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
            Shape_Box itsShape, fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Back_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Back_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Front_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
            Shape_Box itsShape, fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Front_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Front_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Bottom(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_Top(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape,
            fixedVector3 f_itsPosition) {
        
        
    }

    @Override
    public void putThis_OutComposite(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {
        
    }

    private Vector4f lightColor = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);

    @Override
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {
        int
        xa = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        ya = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        za = (int)(f_toFloat(f_position.z + f_zOffset) * scale),
        //
        dxa = (int)(f_toFloat(f_position.x + f_xOffset + f_mul(f_direction.x, f_length)) * scale),
        dya = (int)(f_toFloat(f_position.y + f_yOffset + f_mul(f_direction.y, f_length)) * scale),
        dza = (int)(f_toFloat(f_position.z + f_zOffset + f_mul(f_direction.z, f_length)) * scale);
        //
        //d = (int)(f_toFloat(f_position.z + f_length + f_zOffset) * scale);

        screen.drawLine(xa, ya, za, dxa, dya, dza, lightColor, true);
    }
}
