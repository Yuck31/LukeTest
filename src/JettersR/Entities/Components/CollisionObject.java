package JettersR.Entities.Components;
/**
 * Collision Component.
 * 
 * Author: Luke Sullivan
 * Last Edit: 5/28/2023
 */
//import org.joml.Vector2f;
//import org.joml.Vector3f;
//import org.joml.Vector4f;

import JettersR.Level;
import JettersR.Entities.Entity;
import JettersR.Util.fixedVector3;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class CollisionObject implements OctreeObject
{
    /**
     * This is a functional interface that allows you to assign practically
     * ANY function you want to execute when something collides into the shape with said function.
     * 
     * How your CollisionResponse is used depends on how you set the tileCheckType and entityCheckType values in your PhysicsComponent.
     * "What do I want this shape to do to the things it collides with/is collided by?"
     */
    @FunctionalInterface
    public interface CollisionResponse
    {
        /**
         * Executes this CollisionResponse onto the given Entity.
         * We need:
         * -Its Position to affect it.
         * -Its PhysicsComponent for contact points.
         * -This Position to use as an offset.
         * 
         * MAYBE This PhysicsComponent to for "moving into each other" scanario. (But not all will have one. So...)
         * MAYBE Time of contact.
         * 
         * @param entity is the Entity to affect.
         * @param shape is the shape to affect.
         * @param position is the position to affect.
         * @param velocity is the velocity to affect.
         * @param thisPosition is this Shape's position.
         */
        //public abstract void execute(Shape3D shape, Entity entity, Vector3f position, Vector3f velocity,
        //Vector3f thisPosition);
        public abstract void execute(Entity entity, Shape3D shape, fixedVector3 position, fixedVector3 velocity,
        fixedVector3 thisPosition);

        /*
         * velocityA
         * velocityB
         * 
         * length = velocityA + velocityB
         * percentA = velocityA / length
         * percentB = velocityB / length
         */
    }
    public CollisionResponse collisionResponse = this::doNothing;

    //The Shape of this Collision Object
    private Shape3D shape = null;

    //The Entity that owns this Collision Object.
    private Entity entity = null;

    //The Level entity is in.
    //private Level level = null;

    

    //Whether or not this CollisionObject can be collided with
    private boolean active  = true;

    /**
     * Constructor.
     * 
     * @param entity is this CollisionObject's associated Entity.
     * @param shape is this CollisionObject's shape.
     */
    public CollisionObject(Entity entity, Shape3D shape)
    {
        this.entity = entity;
        this.shape = shape;
    }

     /**
     * Constructor.
     * 
     * @param entity is this CollisionObject's associated Entity.
     * @param shape is this CollisionObject's shape.
     * @param collisionResponse
     */
    public CollisionObject(Entity entity, Shape3D shape, CollisionResponse collisionResponse)
    {
        this.entity = entity;
        this.shape = shape;
        this.collisionResponse = collisionResponse;
    }

    /**Sets the Level variable for this CollisionObject.*/
    public void init(Level level)
    {
        //Set level pointer.
        //this.level = level;

        //Add this CollisionComponent to the level.
        level.addCollisionComponent(this);
    }
    

    //public static final float DIAGONAL_AXES = 1f / (float)Math.sqrt(2);
    public static final @fixed int f_DIAGONAL_AXES = f_sqrt( f_square(f_HALF) + f_square(f_HALF) );

    

    /**Sets this CollisionObject's CollisionResponse.*/
    public final void setCollisionResponse(CollisionResponse collisionResponse){this.collisionResponse = collisionResponse;}
    //
    //public final void doNothing(Shape3D shape, Entity entity, Vector3f position, Vector3f velocity, Vector3f thisPosition){return;}
    public final void doNothing(Entity entity, Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition){return;}


    @Override
    /**Returns the Shape of this Collision Object.*/
    public final Shape3D getShape(){return shape;}

    @Override
    /**Returns the position of this Collision Object's entity.*/
    public fixedVector3 f_getPosition(){return entity.f_getPosition();}


    /**Returns the Entity that owns this Collision Object.*/
    public final Entity getEntity(){return entity;}

    
    //Active Getter/Setter
    public final boolean isActive(){return active;}
    public final void setAcitve(boolean active){this.active = active;}

    //Offset Getters/Setters
    //public final @fixed int f_xOffset(){return shape.f_getXOffset();}
    //public final @fixed int f_yOffset(){return shape.f_getYOffset();}
    //public final @fixed int f_zOffset(){return shape.f_getZOffset();}
}
