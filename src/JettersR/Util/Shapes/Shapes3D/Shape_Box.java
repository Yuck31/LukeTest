package JettersR.Util.Shapes.Shapes3D;
/**
 * Abstract Shape class for Shapes with a Width, Height, and Depth value.
 * 
 * Author: Luke Sullivan
 * Last Edit: 8/19/2023
 */
import org.joml.Vector3f;

import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public abstract class Shape_Box extends Shape3D
{
    /**Class to represent a face of a shape.*/
    public class Shape_Face
    {
        //Direction.
        public Vector3f direction;

        //Points.
        public Vector3f[] points;

        //Indicies of neighboring faces.
        public byte[] neighboringIndicies;

        //Constructor.
        public Shape_Face(Vector3f direction, byte[] neighboringIndicies, float... pointData)
        {
            //Set direction.
            this.direction = direction;

            //Set points.
            this.points = new Vector3f[pointData.length/3];
            for(int i = 0; i < points.length; i++)
            {
                int dataIndex = i*3;
                points[i] = new Vector3f
                (
                    pointData[dataIndex],
                    pointData[dataIndex+1],
                    pointData[dataIndex+2]
                );
            }

            //Set neighboring indicies.
            this.neighboringIndicies = neighboringIndicies;
        }

        //Direction getter.
        public Vector3f getDirection(){return direction;}

        //Points getter.
        public Vector3f[] getPoints(){return points;}

        //Gets an edge from this face.
        public byte[] getNeighboringIndicies(){return neighboringIndicies;}
    }

    //Dimensions.
    protected int width, height, depth;

    /**Constructor.*/
    public Shape_Box(int width, int height, int depth, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset)
    {
        super(f_xOffset, f_yOffset, f_zOffset);
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**Construtor. Offsets are Dimemsions / 2 by default.*/
    public Shape_Box(int width, int height, int depth)
    {
        this
        (
            width, height, depth,
            -fixed(width) / 2,
            -fixed(height) / 2,
            -fixed(depth) / 2
        );
    }

    @Override
    //Dimension Getters.
    public final int getWidth(){return width;}
    public final @fixed int f_getWidth(){return fixed(width);}
    //
    public final int getHeight(){return height;}
    public final @fixed int f_getHeight(){return fixed(height);}
    //
    public final int getDepth(){return depth;}
    public final @fixed int f_getDepth(){return fixed(depth);}


    @Override
    public @fixed int f_left(){return f_xOffset;}
    public @fixed int f_right(){return f_xOffset + fixed(width);}
    //
    public @fixed int f_back(){return f_yOffset;}
    public @fixed int f_front(){return f_yOffset + fixed(height);}
    //
    public @fixed int f_bottom(){return f_zOffset;}
    public @fixed int f_top(){return f_zOffset + fixed(depth);}

    //Edge getter.
    public abstract Shape_Face[] getFaces();
    

    public abstract boolean tileCollidedBy(Shape3D itsShape, fixedVector3 f_itsPosition, fixedVector3 f_itsVelocity, fixedVector3 f_thisPosition, byte thisTileForces);

    //
    //
    //
    public final void putThis_Left_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putLeft_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Left_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putLeft_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Left_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putLeft_Contact(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Right_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putRight_Contact(this, entity, f_position, f_velocity, f_itsPosition);}
    
    public final void putThis_Right_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putRight_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Right_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putRight_Contact(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Back_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putBack_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Back_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putBack_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Back_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putBack_Contact(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Front_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putFront_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Front_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putFront_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Front_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putFront_Contact(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_Bottom(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putBottom_Contact(this, entity, f_position, f_velocity, f_itsPosition);}

    public final void putThis_Top(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putTop_Contact(this, entity, f_position, f_velocity, f_itsPosition);}
    //
    //
    public final void putThis_OutComposite(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, Shape_Box itsShape, fixedVector3 f_itsPosition)
    {itsShape.putBox_OutComposite(this, entity, f_position, f_velocity, f_itsPosition);}


    /**
     * Checks if the Given Box is going into the Left Side of this Shape.
     * 
     * @param shape is the Shape to check if it's colliding.
     * @param f_x is the given box's x position.
     * @param f_xVelocity is the given box's x velocity.
     * @param f_thisX is this shape's x position.
     * @return true if the given box is about to collide with this shape from the left.
     */
    public final boolean collide_Left(Shape3D shape, @fixed int f_x, @fixed int f_xVelocity, @fixed int f_thisX)
    {
        //System.out.println("Left " + (x + right) + " | " + (thisX + this.xOffset));
        @fixed int f_right = shape.f_rightContact(); 

        return (f_x + f_right) <= (f_thisX + this.f_xOffset)
        ;//&& (f_x + f_right) + f_xVelocity > (f_thisX + this.f_xOffset);
    }

    /**
     * Checks if the Given Box is going into the Right Side of this Shape.
     * 
     * @param shape is the Shape to check if it's colliding.
     * @param f_x is the given box's x position.
     * @param f_xVelocity is the given box's x velocity.
     * @param f_thisX is this shape's x position.
     * @return true if the given box is about to collide with this shape from the right.
     */
    public final boolean collide_Right(Shape3D shape, @fixed int f_x, @fixed int f_xVelocity, @fixed int f_thisX)
    {
        //System.out.println("Right " + (x + box.getXOffset()) + " | " + (thisX + this.xOffset + this.width));
        @fixed int f_left = shape.f_leftContact();

        return (f_x + f_left) >= (f_thisX + this.f_xOffset + fixed(this.width))
        ;//&& (f_x + f_left) + f_xVelocity < (f_thisX + this.f_xOffset + fixed(this.width));
    }

    /**
     * Checks if this Box is going into the Back Side of the given Shape.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_y is the given box's y position.
     * @param f_yVelocity is the given box's y velocity.
     * @param f_thisY is this shape's y position.
     * @return true if the given box is about to collide with this shape from the back.
     */
    public final boolean collide_Back(Shape3D shape, @fixed int f_y, @fixed int f_yVelocity, @fixed int f_thisY)
    {
        @fixed int f_front = shape.f_frontContact();
        //f_print("Front", (f_y + f_front), "Back", (f_thisY + this.f_yOffset));

        return (f_y + f_front) <= (f_thisY + this.f_yOffset)
        ;//&& (f_y + f_front) + f_yVelocity > (f_thisY + this.f_yOffset);
    }

    /**
     * Checks if this Box is going into the Front Side of the given Shape.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_y is the given box's y position.
     * @param f_yVelocity is the given box's y velocity.
     * @param f_thisY is this shape's y position.
     * @return true if the given box is about to collide with this shape from the front.
     */
    public final boolean collide_Front(Shape3D shape, @fixed int f_y, @fixed int f_yVelocity, @fixed int f_thisY)
    {
        @fixed int f_back = shape.f_backContact();
        //System.out.println("Front " + (y + box.getYOffset()) + " | " + (thisY + this.yOffset + this.height));

        return (f_y + f_back) >= (f_thisY + this.f_yOffset + fixed(this.height))
        ;//&& (f_y + f_back) + f_yVelocity < (f_thisY + this.f_yOffset + fixed(this.height));
    }

    /**
     * Checks if this Box is going into the Bottom Side of the given Shape.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_z is the given box's z position.
     * @param f_zVelocity is the given box's z velocity.
     * @param f_thisZ is this shape's z position.
     * @return true if the given box is about to collide with this shape from the bottom.
     */
    public final boolean collide_Bottom(Shape3D shape, @fixed int f_z, @fixed int f_zVelocity, @fixed int f_thisZ)
    {
        @fixed int f_top = shape.f_topContact();

        return (f_z + f_top) <= (f_thisZ + this.f_zOffset)
        ;//&& (f_z + f_top) + f_zVelocity > (f_thisZ + this.f_zOffset);
    }

    /**
     * Checks if this Box is going into the Top Side of the given Shape.
     *
     * @param shape is the Shape to check if it's colliding.
     * @param f_z is the given box's z position.
     * @param f_zVelocity is the given box's z velocity.
     * @param f_thisZ is this shape's z position.
     * @return true if the given box is about to collide with this shape from the top.
     */
    public final boolean collide_Top(Shape3D shape, @fixed int f_z, @fixed int f_zVelocity, @fixed int f_thisZ)
    {
        @fixed int f_bottom = shape.f_bottomContact();

        return (f_z + f_bottom) >= (f_thisZ + this.f_zOffset + fixed(this.depth)) //- fixed(4)
        ;//&& (f_z + f_bottom) + f_zVelocity < (f_thisZ + this.f_zOffset + fixed(this.depth));
    }


    /*
     * Collision Responses
     */

    /**
     * Puts the given position next to the Left Side of the given Shape.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putLeft_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.x = (f_thisPosition.x + this.f_xOffset) - shape.f_rightContact();
        f_velocity.x = 0;
    }

    public final void putLeft_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisLeft)
    {
        f_position.x = (f_thisPosition.x + this.f_xOffset + f_thisLeft) - shape.f_rightContact();
        f_velocity.x = 0;
    }

    public void putLeft(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.x = (f_thisPosition.x + this.f_xOffset) - shape.f_right();
        f_velocity.x = 0;
    }

    /**
     * Puts the given position next to the Left Side of the given Shape.
     * 
     * @param cylinder the Cylinder to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putCylinderLeft(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisBack, @fixed int f_thisFront)
    {
        //System.out.println("cynLeft " + cylinder.f_getUnitX());

        f_print("UnitX", cylinder.f_getUnitX(), "UnitY", cylinder.f_getUnitY());

        if(cylinder.f_getUnitX() <= 0)
        {
            cylinder.f_setUnitX(0);
            cylinder.f_setUnitY(f_ONE);
        }
        else{putLeft_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.y + cylinder.f_getYOffset() < f_thisPosition.y + this.f_yOffset + f_thisBack)
        {
            System.out.println("Putting Back.");
            putBack_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisBack);
        }
        //
        else if(f_position.y + cylinder.f_getYOffset() > f_thisPosition.y + this.f_yOffset + f_thisFront)
        {
            //System.out.println("Putting Front.");
            
            putFront_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisFront);
        }
    }
    public final void putCylinderLeft(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {putCylinderLeft(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, fixed(this.height));}

    public final void putCylinderLeft_ul(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitX() <= 0)
        {
            cylinder.f_setUnitX(0);
            cylinder.f_setUnitY(f_ONE);
        }
        else{putLeft_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.y + cylinder.f_getYOffset() < f_thisPosition.y + this.f_yOffset)
        {putBack_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }

    public final void putCylinderLeft_dl(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitX() <= 0)
        {
            cylinder.f_setUnitX(0);
            cylinder.f_setUnitY(-f_ONE);
        }
        else{putLeft_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.y + cylinder.f_getYOffset() > f_thisPosition.y + this.f_yOffset + fixed(this.height))
        {putFront_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }


    /**
     * Puts the given position next to the Right Side of the given Shape.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putRight_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.x = (f_thisPosition.x + this.f_xOffset + fixed(this.width)) - (shape.f_leftContact());
        f_velocity.x = 0;
    }

    public final void putRight_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisRight)
    {
        f_position.x = (f_thisPosition.x + this.f_xOffset + f_thisRight) - (shape.f_leftContact());
        f_velocity.x = 0;
    }

    public void putRight(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.x = (f_thisPosition.x + this.f_xOffset + fixed(this.width)) - shape.f_left();
        f_velocity.x = 0;
    }

    /**
     * Puts the given position next to the Right Side of the given Shape.
     * 
     * @param cylinder the Cylinder to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putCylinderRight(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisBack, @fixed int f_thisFront)
    {
        if(cylinder.f_getUnitX() >= 0)
        {
            //Set straight down/up.
            cylinder.f_setUnitX(0);
            cylinder.f_setUnitY(f_ONE);
        }
        else{putRight_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.y + cylinder.f_getYOffset() < f_thisPosition.y + this.f_yOffset + f_thisBack)
        {
            System.out.println("Putting Back.");
            putBack_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisBack);
        }
        //
        else if(f_position.y + cylinder.f_getYOffset() > f_thisPosition.y + this.f_yOffset + f_thisFront)
        {
            System.out.println("Putting Front.");
            putFront_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisFront);
        }
    }
    public final void putCylinderRight(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {putCylinderRight(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, fixed(this.height));}

    public final void putCylinderRight_ur(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitX() >= 0)
        {
            cylinder.f_setUnitX(0);
            cylinder.f_setUnitY(f_ONE);
        }
        else{putRight_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.y + cylinder.f_getYOffset() < f_thisPosition.y + this.f_yOffset)
        {putBack_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }

    public final void putCylinderRight_dr(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitX() >= 0)
        {
            cylinder.f_setUnitX(0);
            cylinder.f_setUnitY(-f_ONE);
        }
        else{putRight_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.y + cylinder.f_getYOffset() > f_thisPosition.y + this.f_yOffset + fixed(this.height))
        {putFront_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }


    /**
     * Puts the given position next to the Back Side of the given Shape.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putBack_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.y = (f_thisPosition.y + this.f_yOffset) - shape.f_frontContact();
        f_velocity.y = 0;
    }

    public final void putBack_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisBack)
    {
        f_position.y = (f_thisPosition.y + this.f_yOffset + f_thisBack) - shape.f_frontContact();
        f_velocity.y = 0;
    }

    public void putBack(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.y = (f_thisPosition.y + this.f_yOffset) - shape.f_front();
        f_velocity.y = 0;
    }

    /**
     * Puts the given position next to the Back Side of the given Shape.
     * 
     * @param cylinder the Cylinder to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putCylinderBack(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisLeft, @fixed int f_thisRight)
    {
        if(cylinder.f_getUnitY() <= 0)
        {
            cylinder.f_setUnitY(0);
            cylinder.f_setUnitX(f_ONE);

            //System.out.println("cylinder.f_getUnitY() < 0");
        }
        else{putBack_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}

        if(f_position.x + cylinder.f_getXOffset() < f_thisPosition.x + this.f_xOffset + f_thisLeft)
        {putLeft_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisLeft);}
        //
        else if(f_position.x + cylinder.f_getXOffset() > f_thisPosition.x + this.f_xOffset + f_thisRight)
        {
            System.out.println("Putting right.");
            putRight_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisRight);
        }
    }
    public final void putCylinderBack(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {putCylinderBack(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, fixed(this.width));}

    public final void putCylinderBack_ul(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitY() <= 0)
        {
            cylinder.f_setUnitY(0);
            cylinder.f_setUnitX(f_ONE);
        }
        else{putBack_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.x + cylinder.f_getXOffset() < f_thisPosition.x + this.f_xOffset)
        {putLeft_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }

    public final void putCylinderBack_ur(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitY() <= 0)
        {
            cylinder.f_setUnitY(0);
            cylinder.f_setUnitX(-f_ONE);
        }
        else{putBack_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.x + cylinder.f_getXOffset() > f_thisPosition.x + this.f_xOffset + fixed(this.width))
        {putRight_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }



    /**
     * Puts the given position next to the Front Side of the given Shape.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putFront_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.y = (f_thisPosition.y + this.f_yOffset + fixed(this.height)) - shape.f_backContact();
        f_velocity.y = 0;
    }

    public final void putFront_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisFront)
    {
        f_position.y = (f_thisPosition.y + this.f_yOffset + f_thisFront) - shape.f_backContact();
        f_velocity.y = 0;
    }

    public void putFront(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.y = (f_thisPosition.y + this.f_yOffset + fixed(this.height)) - shape.f_back();
        f_velocity.y = 0;
    }

    /**
     * Puts the given position next to the Front Side of the given Shape.
     * 
     * @param cylinder the Cylinder to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putCylinderFront(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition, @fixed int f_thisLeft, @fixed int f_thisRight)
    {
        if(cylinder.f_getUnitY() >= 0)
        {
            cylinder.f_setUnitY(0);
            cylinder.f_setUnitX(f_ONE);

            //System.out.println("f_getUnitY() > 0");
        }
        else{putFront_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.x + cylinder.f_getXOffset() < f_thisPosition.x + this.f_xOffset + f_thisLeft)
        {putLeft_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisLeft);}
        //
        else if(f_position.x + cylinder.f_getXOffset() > f_thisPosition.x + this.f_xOffset + f_thisRight)
        {putRight_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition, f_thisRight);}
    }
    public final void putCylinderFront(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {putCylinderFront(cylinder, entity, f_position, f_velocity, f_thisPosition, 0, fixed(this.width));}

    public final void putCylinderFront_dl(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitY() >= 0)
        {
            cylinder.f_setUnitY(0);
            cylinder.f_setUnitX(f_ONE);
        }
        else{putFront_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.x + cylinder.f_getXOffset() < f_thisPosition.x + this.f_xOffset)
        {putLeft_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }

    public final void putCylinderFront_dr(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        if(cylinder.f_getUnitY() >= 0)
        {
            cylinder.f_setUnitY(0);
            cylinder.f_setUnitX(-f_ONE);
        }
        else{putFront_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
        //
        if(f_position.x + cylinder.f_getXOffset() > f_thisPosition.x + this.f_xOffset + fixed(this.width))
        {putRight_Contact(cylinder, entity, f_position, f_velocity, f_thisPosition);}
    }


    /**
     * Puts the given position next to the Back Side of the given Shape.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putBottom_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = (f_thisPosition.z + this.f_zOffset) - shape.f_topContact();
        f_velocity.z = 0;
    }

    public final void putBottom(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = (f_thisPosition.z + this.f_zOffset) - shape.f_top();
        f_velocity.z = 0;
    }


    /**
     * Puts the given position next to the Right Side of the given Shape.
     * 
     * @param shape the Shape to affect.
     * @param entity the Entity to affect.
     * @param f_position the position to affect.
     * @param f_velocity the velocity to affect.
     * @param f_thisPosition this shape's position.
     */
    public final void putTop_Contact(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        f_position.z = (f_thisPosition.z + this.f_zOffset + fixed(this.depth)) - shape.f_bottomContact();
        f_velocity.z = 0;
    }

    public final void putTop(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity,
    fixedVector3 f_thisPosition)
    {
        //System.out.println("putTop");

        f_position.z = (f_thisPosition.z + this.f_zOffset + fixed(this.depth)) - shape.f_bottom();
        f_velocity.z = 0;
    }

    public abstract void putBox_OutComposite(Shape3D shape, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition);
    public abstract void putCylinder_OutComposite(Cylinder cylinder, Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, fixedVector3 f_thisPosition);

    public abstract void tileRender(Screen screen, float scale, @fixed int f_x, @fixed int f_y, @fixed int f_z, boolean fixed);
    public final void tileRender(Screen screen, float scale, fixedVector3 f_position, boolean fixed){tileRender(screen, scale, f_position.x, f_position.y, f_position.z, fixed);}
}
