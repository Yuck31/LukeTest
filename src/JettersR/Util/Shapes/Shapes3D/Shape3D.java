package JettersR.Util.Shapes.Shapes3D;
/**
 * Author: Luke Sullivan
 * Last Edit: 2/23/2023
 */
import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

public abstract class Shape3D
{
    //Offsets to offset the shape's position from the Entity's position.
    protected @fixed int f_xOffset, f_yOffset, f_zOffset;

    /**Constructor.*/
    public Shape3D(@fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset)
    {
        this.f_xOffset = f_xOffset;
        this.f_yOffset = f_yOffset;
        this.f_zOffset = f_zOffset;
    }

    public Shape3D(){}

    //Dimension Getters/Setters
    public final @fixed int f_getXOffset(){return f_xOffset;}
    public final void f_setXOffset(@fixed int f_xOffset){this.f_xOffset = f_xOffset;}
    public final @fixed int f_getYOffset(){return f_yOffset;}
    public final void f_setYOffset(@fixed int f_yOffset){this.f_yOffset = f_yOffset;}
    public final @fixed int f_getZOffset(){return f_zOffset;}
    public final void f_setZOffset(@fixed int f_zOffset){this.f_zOffset = f_zOffset;}

    public abstract int getWidth();
    public abstract @fixed int f_getWidth();
    //
    public abstract int getHeight();
    public abstract @fixed int f_getHeight();
    //
    public abstract int getDepth();
    public abstract @fixed int f_getDepth();
    
    public abstract @fixed int f_left();
    public abstract @fixed int f_right();
    public abstract @fixed int f_back();
    public abstract @fixed int f_front();
    public abstract @fixed int f_bottom();
    public abstract @fixed int f_top();

    //public abstract float leftContact();
    //public abstract float rightContact();
    //public abstract float backContact();
    //public abstract float frontContact();
    //public abstract float bottomContact();
    //public abstract float topContact();
    public abstract @fixed int f_leftContact();
    public abstract @fixed int f_rightContact();
    public abstract @fixed int f_backContact();
    public abstract @fixed int f_frontContact();
    public abstract @fixed int f_bottomContact();
    public abstract @fixed int f_topContact();
    
    /*
     * These functions are used to let one shape ask another shape
     * "Hey, what kind of shape are you annd what intersects
     * function should I use?". So that way I don't have to use a 
     * bunch of instanceof statements.
     */
    //public abstract boolean intersects_AAB_Box
    //(AAB_Box box, fixedVector3 bPosition, fixedVector3 bVelocity, fixedVector3 thisPosition);

    /**
     * Causes this Shape to start performing Collision on the given Shape.
     * THIS reacts to OPPOSING SHAPE'S collisionResponse.
     * 
     * @param f_thisPosition is this Shape's Position.
     * @param f_thisVelocity is this Shape's Velocity.
     * @param shape is the opposing shape to test against.
     * @param f_shapePosition is the opposing shape's position.
     */
    //public abstract boolean performCollision(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Shape3D shape, fixedVector3 f_shapePosition);
    public abstract boolean performCollision
    (
        fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity,
        Shape3D shape, fixedVector3 f_shapePosition//, @fixed int f_shapeXVelocity, @fixed int f_shapeYVelocity, @fixed int f_shapeZVelocity
    );

    //public final void putThis_Left(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    //{itsShape.putLeft(this, entity, position, velocity, itsPosition);}
    public abstract void putThis_Left_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Left_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Left_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    //
    //public final void putThis_Right(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    //{itsShape.putRight(this, entity, position, velocity, itsPosition);}
    public abstract void putThis_Right_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Right_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Right_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    //
    //public final void putThis_Back(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    //{itsShape.putBack(this, entity, position, velocity, itsPosition);}
    public abstract void putThis_Back_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Back_ul(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Back_ur(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    //
    //public final void putThis_Front(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition)
    //{itsShape.putFront(this, entity, position, velocity, itsPosition);}
    public abstract void putThis_Front_Contact(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Front_dl(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Front_dr(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    //
    public abstract void putThis_Bottom(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    public abstract void putThis_Top(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box itsShape, fixedVector3 f_itsPosition);
    //
    public abstract void putThis_OutComposite(Entity entity, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, Shape_Box itsShape, fixedVector3 f_itsPosition);


    /*
     * Checking Functions
     */

    /**Checks if the given point is on the other side of the given line.*/
    public boolean check_PointToLine2D(float pointX, float pointY,
    float lineX, float lineY, float slope, float yIntercept, boolean down)
    {
        return (down) ? pointY < slope * pointX + yIntercept
        : pointY > slope * pointX + yIntercept;
    }

    /*
     * Render Functions.
     */

    public abstract void render(Screen screen, float scale, fixedVector3 position);

    private static final fixedVector3 RENDER_VEC = new fixedVector3();
    public final void render(Screen screen, float scale, @fixed int f_x, @fixed int f_y, @fixed int f_z)
    {
        Shape3D.RENDER_VEC.set(f_x, f_y, f_z);
        render(screen, scale, Shape3D.RENDER_VEC);
    }
}
