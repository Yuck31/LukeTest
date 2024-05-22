package JettersR.Tiles;
/**
 * Base class for all Tiles in the game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/31/2024
 */
import org.joml.Vector3f;

import JettersR.Level;
import JettersR.Entities.Entity;
import JettersR.Entities.Components.PhysicsComponent;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.*;
import JettersR.Util.fixedVector3;
//import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Tile
{
    @FunctionalInterface
    /**Collision response functional interface.*/
    public interface Tile_CollisionResponse
    {
        /**
         * Executes this CollisionResponse onto the given Entity.
         * 
         * @param shape is the shape to affect.
         * @param entity is the Entity to affect.
         * @param position is the position to affect.
         * @param velocity is the velocity to affect.
         * @param thisPosition is this Shape's position.
         */
        //public abstract void execute(Shape3D shape, Entity entity, Vector3f position, Vector3f velocity,
        //Vector3f thisPosition, Material material);

        public abstract void execute(Shape3D shape, Entity entity, fixedVector3 position, fixedVector3 velocity,
        fixedVector3 thisPosition, Material material);
    }

    //Shape of this Tile.
    protected final Shape_Box collisionShape;    

    //Faces of this Tile's Shape.
    protected final Shape_Box.Shape_Face[] faces;


    //This Tile's CollisionResponse.
    //protected final Tile_CollisionResponse collisionResponse;

    //Vectors for storing old position and velocity values.
    protected static final Vector3f oldPosition = new Vector3f(), oldVelocity = new Vector3f();

    /**
     * Constructor.
     * 
     * @param collisionShape this Tile's collisionShape.
     */
    protected Tile(Shape_Box collisionShape)//, Tile_CollisionResponse collisionResponse)
    {
        //Set shape.
        this.collisionShape = collisionShape;

        //Set faces.
        this.faces = (this.collisionShape == null) ? null : this.collisionShape.getFaces();

        //Set collission response.
        //this.collisionResponse = collisionResponse;
    }

    /**Gets this Tile's shape.*/
    public final Shape_Box getShape(){return collisionShape;}

    public final Shape_Box.Shape_Face[] getFaces(){return faces;}


    /**
     * Performs Collision Check with this Tile but lets the caller decide what to do this tile.
     * 
     * @param shape The shape checking collision against this Tile's shape.
     * @param f_position The shape's position.
     * @param f_velocity The shape's velocity.
     * @param f_thisPosition This tile's position.
     * @return true if a collision was made.
     
    public final boolean collisionCheck(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_thisPosition)
    {
        //Perform Collision check.
        if(shape.performCollision(f_position, f_velocity, this.collisionShape, f_thisPosition))
        {
            //A collision was made.
            return true;
        }

        //No collision was made.
        return false;
    }
    */

    /**
     * Perform's this Tile's collision Ressponse: Run's TileEffect and then TileForce.
     * 
     * @param entity The Entity to affect.
     * @param physicsComponent The PhysicsComponent to affect.
     * @param shape entity's shape.
     * @param f_position The position to affect.
     * @param f_velocity The velocity to affect.
     * @param f_thisPosition This Tile's position.
     * @param thisTileInfo This Tile's information stored a single 32-Bit int.
     * @param thisMaterial This Tile's Material, retrieved from its associated TileSprite.
     */
    public final void runCollisionResponse
    (
        Entity entity, PhysicsComponent physicsComponent, Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, 
        fixedVector3 f_thisPosition, int thisTileInfo, Material thisMaterial
    )
    {
        //Apply Effect.
        if
        (
            //True = Forces should be applied.
            Tiles.tileEffects[(thisTileInfo & Tiles.TILE_EFFECT_PORTION) >> Tiles.TILE_EFFECT_BITS]
            .invoke
            (
                entity, physicsComponent, shape, f_position, f_velocity,//Entity's information.
                this.collisionShape, f_thisPosition, thisMaterial//This Tile's information.
            )
        )
        {
            //byte forces = (thisTileInfo & Tiles.TILE_FORCES_PORTION);
            //System.out.println(forces);

            //Apply Forces if the effect didn't already.
            Tiles.tileForces[thisTileInfo & Tiles.TILE_FORCES_PORTION]
            .invoke(shape, f_position, f_velocity, f_oldPosition, this.collisionShape, f_thisPosition);
            //TODO Just make seperate function arrays for each type of shape.
        }
    }


    /**
     * Performs Collision Check and Response with this Tile.
     * 
     * @param shape the shape to affect.
     * @param entity the entity to affect.
     * @param f_velocity the velocity vector to check for and effect.
     * @param f_thisPosition the position of this Tile during this Collision Check.
     
    //public final boolean performCollision(Shape3D shape, Entity entity, Vector3f velocity, Vector3f thisPosition, Material material)
    public final boolean performCollision(Entity entity, PhysicsComponent physicsComponent, Shape3D shape, fixedVector3 f_velocity, fixedVector3 f_thisPosition, Material material)
    {
        //Cache position and shape.
        fixedVector3 f_position = entity.f_getPosition();

        //Perform Collision and apply forces and effects if needed.
        if(shape.performCollision(f_position, f_velocity, this.collisionShape, f_thisPosition))
        {
            //collisionResponse.execute(shape, entity, f_position, velocity, thisPosition, material);
            runCollisionResponse(entity, physicsComponent, shape, f_position, f_velocity, f_thisPosition, material);
            return true;
        }

        //No collision was made.
        return false;
    }
    */



    /**
     * Box Tile Constructor.
     * 
     * @param depth z-dimension of this tile.
     * @param zOffset zOffset of this tile.
     */
    protected static Tile BoxTile(int depth, int zOffset)
    {
        AAB_Box box = new AAB_Box(Level.TILE_SIZE, Level.TILE_SIZE, depth,
        0, 0, zOffset);

        return new Tile
        (
            box
            /*
            ,
            (shape, entity, position, velocity, thisPosition, material) ->
            {
                int tileInfo = physicsComponent.getCurrentTileInfo();
                //Material material = ((tileInfo & Tiles.TILE_MATERIAL_PORTION) >> (Tiles.TILE_EFFECT_BITS + Tiles.TILE_FORCES_BITS));

                //Apply Effect.
                if
                (
                    Tiles.tileEffects[(tileInfo & Tiles.TILE_EFFECT_PORTION) >> Tiles.TILE_EFFECT_BITS]
                    .invoke(shape, entity, entity.getCollisionObject(), position, velocity,
                    box, thisPosition, material)
                )
                {
                    //Apply Forces if the effect didn't alrready.
                    byte forces = (byte)(tileInfo & Tiles.TILE_FORCES_PORTION);
                    //System.out.println(forces);

                    Tiles.tileForces[forces].invoke(shape, entity, position, velocity, box, thisPosition);
                }
            }
            */
        );
    }

    /**Default BoxTile Constructor.*/
    protected static Tile BoxTile(){return BoxTile(Level.TILE_SIZE, 0);}

    /**
     * Slope Tile Constructor.
     * 
     * @param width x dimension of this tile.
     * @param height y dimension of this tile.
     * @param depth z dimension of this tile.
     * @param slopeType type of this slope.
     * @param rise
     * @param run
     * @param xOffset x offset of this tile's shape.
     * @param yOffset y offset of this tile's shape.
     * @param zOffset z offset of this tile's shape.
     * @return a new SlopeTile.
     */
    protected static Tile SlopeTile(int width, int height, int depth, Slope_Triangle.Type slopeType, int rise, int run, int xOffset, int yOffset, int zOffset)
    {
        Slope_Triangle st = new Slope_Triangle(width, height, depth,
        slopeType, rise, run, fixed(xOffset), fixed(yOffset), fixed(zOffset));

        return new Tile
        (
            st
            /*,
            (shape, entity, position, velocity, thisPosition, material) ->
            {
                int tileInfo = entity.getCollisionObject().getCurrentTileInfo();
                //Material material = ((tileInfo & Tiles.TILE_MATERIAL_PORTION) >> (Tiles.TILE_EFFECT_BITS + Tiles.TILE_FORCES_BITS));

                //Apply Effect.
                if
                (
                    Tiles.tileEffects[(tileInfo & Tiles.TILE_EFFECT_PORTION) >> Tiles.TILE_FORCES_BITS]
                    .invoke(shape, entity, entity.getCollisionObject(), position, velocity,
                    st, thisPosition, material)
                )
                {
                    //Apply Forces if the effect didn't already.
                    byte forces = (byte)(tileInfo & Tiles.TILE_FORCES_PORTION);

                    Tiles.tileForces[forces].invoke(shape, entity, position, velocity, st, thisPosition);
                }
            }
            */
        );
    }

    /**Default SlopeTile Constructor.*/
    protected static Tile SlopeTile(Slope_Triangle.Type slopeType)
    {return SlopeTile(Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE, slopeType, 1, 1, 0, 0, 0);}


    /**
     * Isosceles Tile Constructor.
     * 
     * @param width x dimension of this tile.
     * @param height y dimension of this tile.
     * @param depth z dimension of this tile.
     * @param isoType Isosceles_Triangle.Type value.
     * @param xOffset x offset of this tile's shape.
     * @param yOffset y offset of this tile's shape.
     * @param zOffset z offset of this tile's shape.
     * @return a new IsoscelesTile.
     */
    protected static Tile IsoscelesTile(int width, int height, int depth, byte isoType, int rise, int run, int xOffset, int yOffset, int zOffset)
    {
        Isosceles_Triangle it = new Isosceles_Triangle(width, height, depth, isoType, rise, run, fixed(xOffset), fixed(yOffset), fixed(zOffset));
        //
        return new Tile
        (
            it
            /*,
            (shape, entity, position, velocity, thisPosition, material) ->
            {
                int tileInfo = entity.getCollisionObject().getCurrentTileInfo();
                //Material material = ((tileInfo & Tiles.TILE_MATERIAL_PORTION) >> (Tiles.TILE_EFFECT_BITS + Tiles.TILE_FORCES_BITS));

                //Apply Effect.
                if
                (
                    Tiles.tileEffects[(tileInfo & Tiles.TILE_EFFECT_PORTION) >> Tiles.TILE_EFFECT_BITS]
                    .invoke(shape, entity, entity.getCollisionObject(), position, velocity,
                    it, thisPosition, material)
                )
                {
                    //Apply Forces if the effect didn't alrready.
                    byte forces = (byte)(tileInfo & Tiles.TILE_FORCES_PORTION);
                    //System.out.println(forces);

                    Tiles.tileForces[forces].invoke(shape, entity, position, velocity, it, thisPosition);
                }
            }
            */
        );
    }

    /**Default IsoscelesTile Constructor.*/
    protected static Tile IsoscelesTile(byte isoType)
    {return IsoscelesTile(Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE, isoType, 1, 1, 0, 0, 0);}

    

    /**
     * Debug render function.
     * 
     * @param screen Screen object.
     * @param x x position of shape.
     * @param y y position of shape.
     * @param z z position of shape.
     * @param scale scale of shape.
     * @param layer scale of shape.
     */
    public final void renderShape(Screen screen, int x, int y, int z, float scale, boolean fixed)
    {
        //if(collisionShape instanceof AAB_Box){return;}

        collisionShape.tileRender(screen, scale, x, y, z, fixed);
    }

    /**
     * Debug render function.
     * 
     * @param screen Screen object.
     * @param xPos x position of tile.
     * @param yPos y position of tile.
     * @param zPos z position of tile.
     * @param scale scale of tile.
     * @param layer the layer value of this tile.
     */
    public final void render(Screen screen, int xPos, int yPos, int zPos, float scale, int layer)
    {

    }

    public final void render(Screen screen, int xPos, int yPos)
    {

    }
}
