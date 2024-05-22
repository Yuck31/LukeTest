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

public class Sphere extends Shape3D
{
    //This Sphere's radius.
    private @fixed int f_radius;

    /**Constructor.*/
    public Sphere(int xOffset, int yOffset, int zOffset, @fixed int f_radius)
    {
        super(xOffset, yOffset, zOffset);
        this.f_radius = f_radius;
    }

    //Radius Getter/Setter.
    public float f_getRadius(){return f_radius;}
    public void f_setRadius(@fixed int f_radius){this.f_radius = f_radius;}

    @Override
    public int getWidth(){return f_toInt(f_radius * 2);}
    public @fixed int f_getWidth(){return f_radius * 2;}
    //
    public int getHeight(){return f_toInt(f_radius * 2);}
    public @fixed int f_getHeight(){return f_radius * 2;}
    //
    public int getDepth(){return f_toInt(f_radius*2);}
    public @fixed int f_getDepth(){return f_radius*2;}

    @Override
    public @fixed int f_left(){return (int)(f_xOffset - f_radius);}
    public @fixed int f_right(){return (int)(f_xOffset + f_radius);}
    //
    public @fixed int f_back(){return (int)(f_yOffset - f_radius);}
    public @fixed int f_front(){return (int)(f_yOffset + f_radius);}
    //
    public @fixed int f_bottom(){return (int)(f_zOffset - f_radius);}
    public @fixed int f_top(){return (int)(f_zOffset + f_radius);}

    @Override
    public @fixed int f_leftContact(){return 0;}
    public @fixed int f_rightContact(){return 0;}
    //
    public @fixed int f_backContact(){return 0;}
    public @fixed int f_frontContact(){return 0;}
    //
    public @fixed int f_bottomContact(){return 0;}
    public @fixed int f_topContact(){return 0;}

    /**Sphere intersection.*/
    public boolean intersects(float x, float y, float z, Sphere s, float sX, float sY, float sZ)
    {
        float
        sideX = (x + f_xOffset) - (sX + s.f_getXOffset()),
        sideY = (y + f_yOffset) - (sY + s.f_getYOffset()),
        sideZ = (z + f_zOffset) - (sZ + s.f_getZOffset()),
        //
        radiiSum = this.f_radius + s.f_getRadius(),
        length = (sideX * sideX) + (sideY * sideY) + (sideZ * sideZ);

        //This returns true if the distance between the two spheres
        //Is within the sum of their radii.
        //This can be determined via Pythagorean Therom (albeit a 3D version of it): a^2 + b^2 c^2 = d^2.
        if(length < (radiiSum * radiiSum))
        {
            //length = (float)Math.sqrt(length);
            //if(length < 1){length = 1;}

            //float unitX = sideX / length, unitY = sideY / length;

            //s.setUnitVector(unitX, unitY,
            //xOffset - (this.radius * unitX), yOffset - (this.radius * unitY));
            //this.radius - length);
            return true;
        }

        //No collision was made.
        return false;
    }

    /**Box intersection.*/
    public boolean intersects(float x, float y, float z, AAB_Box B, float bX, float bY, float bZ)
    {
        return false;
    }

    @Override
    public boolean performCollision(fixedVector3 thisPosition, fixedVector3 thisVelocity, Shape3D shape, fixedVector3 shapePosition)
    {
        //Sphere
        if(shape instanceof Sphere)
        {
            Sphere sphere = (Sphere)shape;

            if
            (
                intersects(thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z,
                sphere, shapePosition.x, shapePosition.y, shapePosition.z)
            )
            {
                //box.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        //AAB_Box
        else if(shape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)shape;

            if
            (
                intersects(thisPosition.x + thisVelocity.x, thisPosition.y + thisVelocity.y, thisPosition.z + thisVelocity.z,
                box, shapePosition.x, shapePosition.y, shapePosition.z)
            )
            {
                //box.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }

        //No collision was made.
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
    public void putThis_OutComposite(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,Shape_Box itsShape, fixedVector3 f_itsPosition)
    {
        
    }

    
    @Override
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {

    }
}
