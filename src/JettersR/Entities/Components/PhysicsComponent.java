package JettersR.Entities.Components;
import java.util.ArrayList;
/**
 * 
 */
import java.util.List;

import JettersR.Level;
import JettersR.Entities.Entity;
import JettersR.Entities.Components.CollisionObject.CollisionResponse;
import JettersR.Graphics.Screen;
import JettersR.Tiles.Tile;
import JettersR.Tiles.Tiles;
import JettersR.Util.Octree;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
import JettersR.Util.fixedVector2;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class PhysicsComponent// implements OctreeObject
{
    //
    //Pointers.
    //

	//Pointer to the Entity that holds this.
    private Entity entity = null;

    //Pointer to Entity's position.
    private fixedVector3 f_entityPosition = null;

    //Pointer to the Level this is in.
    private Level level = null;

	//Pointer to Collision component. In the case this is null, don't add to level.
	private CollisionObject collisionObject = null;

    //Pointer to Collision component's shape.
    private Shape3D collisionShape = null;


    //
    //Physics values.
    //

	//Normalized Direction Vector and X/Y Velocity Scalar.
    private fixedVector2 f_direction = new fixedVector2();

    //Velocity Magnitude.
    private @fixed int f_velocityMagnitude = fixed(0);

    //Target Velocity Vector.
    private fixedVector3 f_velocity = new fixedVector3(),
    f_targetVelocity = new fixedVector3(0, 0, 0, 0, -16, 0);
    //private Vector3f oldPosition = new Vector3f(), oldVelocity = new Vector3f();

    //Accelartion, Mass, and Gravity values
    private @fixed int
    f_acceleration = fixed(Level.TILE_SIZE / 16),
    f_mass = fixed(1),
    f_gravity = fixed(0, 154);

    private @fixed int
    f_current_coFriction = fixed(Level.TILE_SIZE),//From 0.0 to 1.0.
    f_targetVelocityMultiplier = f_ONE;

	//
    private boolean active  = true;


    /**Constructor.*/
    public PhysicsComponent(Entity entity, Shape3D collisionShape)
    {
        //Set entity pointers.
        this.entity = entity;
        this.f_entityPosition = entity.f_getPosition();

        //Set shape pointer.
        this.collisionShape = collisionShape;

        //Create CollisionObject.
        this.collisionObject = new CollisionObject(entity, collisionShape);
    }

    /**Constructor.*/
    public PhysicsComponent(Entity entity, Shape3D collisionShape, CollisionResponse collisionResponse)
    {
        //Set entity pointers.
        this.entity = entity;
        this.f_entityPosition = entity.f_getPosition();

        //Set shape pointer.
        this.collisionShape = collisionShape;

        //Create CollisionObject.
        this.collisionObject = new CollisionObject(entity, collisionShape, collisionResponse);
    }

    /**Sets level pointer and adds this PhysicsComponent to the level.*/
    public void init(Level level)
    {
        //Set pointer to level and add this PhysicsComponent to the level.
        this.level = level;
        level.addPhysicsComponent(this);

        //Do the same with the collision component.
        collisionObject.init(level);
    }

    /**Collision Object getter.*/
    public CollisionObject getCollisionObject(){return this.collisionObject;}

    /**Sets this PhysicsComponent's CollisionObject's CollisionResponse.*/
    public final void setCollisionResponse(CollisionResponse collisionResponse){collisionObject.setCollisionResponse(collisionResponse);}


    /**Updates velocities.*/
	public void update(@fixed int f_timeMod)
	{
        //Cancel if not active.
		if(!active){return;}

        //Adjust Velocity with Friction and Gravity
        f_targetVelocity.x = f_mul( f_mul(f_direction.x, f_velocityMagnitude), f_targetVelocityMultiplier);
        f_targetVelocity.y = f_mul( f_mul(f_direction.y, f_velocityMagnitude), f_targetVelocityMultiplier);

        @fixed int f_fricGrav = f_mul( f_mul(f_acceleration, f_current_coFriction), f_timeMod);

        

        //
        //Perform X Friction calculation... using bit manipulation!
        //
        int signResult = f_targetVelocity.x >> 31;//Fill an int with the sign bit of X velocity.

        //Is the absolute value of xVelocity greater than friction? (Are we not going to meet our target X velocity?)
        if((f_targetVelocity.x ^ signResult) - signResult >= f_fricGrav)
        {
            //Increment xVelocity by the same sign as target velocity.
            f_velocity.x += (f_fricGrav ^ signResult) - signResult;
        }
        //Otherwise, we've reached our target.
        else{f_velocity.x = f_targetVelocity.x;}


        //
        //Same thing with Y friction.
        //
        signResult = f_targetVelocity.y >> 31;
        if((f_targetVelocity.y ^ signResult) - signResult >= f_fricGrav){f_velocity.y += (f_fricGrav ^ signResult) - signResult;}
        else{f_velocity.y = f_targetVelocity.y;}


        //
        //Same thing with Z gravity.
        //
        f_fricGrav = f_mul( f_mul(f_mass, f_gravity), f_timeMod );//We use the gravity value instead.
        signResult = f_targetVelocity.z >> 31;
        if((f_targetVelocity.z ^ signResult) - signResult >= f_fricGrav){f_velocity.z += (f_fricGrav ^ signResult) - signResult;}
        else{f_velocity.z = f_targetVelocity.z;}


        /*
        //Perform X Friction calculation.
        if(f_velocity.x < f_targetVelocity.x)
        {
            f_velocity.x += f_fric;
            if(f_velocity.x > f_targetVelocity.x){f_velocity.x = f_targetVelocity.x;}
        }
        else if(f_velocity.x > f_targetVelocity.x)
        {
            f_velocity.x -= f_fric;
            if(f_velocity.x < f_targetVelocity.x){f_velocity.x = f_targetVelocity.x;}
        }
        */


        //Y Friction
        /*
        if(f_velocity.y < f_targetVelocity.y)
        {
            f_velocity.y += f_fric;
            if(f_velocity.y > f_targetVelocity.y){f_velocity.y = f_targetVelocity.y;}
        }
        else if(f_velocity.y > f_targetVelocity.y)
        {
            f_velocity.y -= f_fric;
            if(f_velocity.y < f_targetVelocity.y){f_velocity.y = f_targetVelocity.y;}
        }
        */


        

        //Z Gravity
        /*
        @fixed int f_grav = f_mul( f_mul(f_mass, f_gravity), f_timeMod );

        if(f_targetVelocity.z > f_velocity.z)
        {
            f_velocity.z += f_grav;
            if(f_velocity.z > f_targetVelocity.z){f_velocity.z = f_targetVelocity.z;}
        }
        else if(f_targetVelocity.z < f_velocity.z)
        {
            f_velocity.z -= f_grav;

            @fixed int termin = f_mul(f_mass, f_targetVelocity.z);
            if(f_velocity.z < termin){f_velocity.z = termin;}
        }
        */


        

        //oldPosition.set(entity.getPosition());
        //oldVelocity.set(f_velocity);
	}


    //
    //OctreeObject methods.
    //

    /***Entity position getter.*/
    //public fixedVector3 f_getPosition(){return this.f_entityPosition;}

    /**Collision shape getter.*/
    public Shape3D getShape(){return this.collisionShape;}


    //
    //Collision Check types.
    //
    public static final byte COLLISIONCHECK_SKIP = 0,
    COLLISIONCHECK_AFFECTEDBY = 1,
    COLLISIONCHECK_AFFECT = 2,
    COLLISIONCHECK_AFFECT_THEN_AFFECTEDBY = 3;

    /**Determines if this PhysicsComponent can collide with Tiles and how it responds or is responded to during a collision.*/
    private byte tileCheckType = COLLISIONCHECK_AFFECTEDBY;
    public byte getTileCheckType(){return this.tileCheckType;}
    public void setTileCheckType(byte tileCheckType){this.tileCheckType = tileCheckType;}

    /**
     * Determines if this PhysicsComponent can collide with other Entities and how it responds or is responded to during a collision.
     * Can still be collided with by other Entities.
     */
    private byte entityCheckType = COLLISIONCHECK_AFFECTEDBY;
    public byte getEntityCheckType(){return this.entityCheckType;}
    public void setEntityCheckType(byte entityCheckType){this.entityCheckType = entityCheckType;}

    /**
     * To be called from Level.
     * 
     * @param collisionObject_Octree
     */
    public void performCollisionCheck(Octree<CollisionObject> collisionObject_Octree)
    {
        //Tile Collision.
        switch(tileCheckType)
        {
            case COLLISIONCHECK_AFFECTEDBY:
            tileCollision_affectedByTile();
            //tileCollision_OLD();
            //System.out.println("Post Tile");
            break;

            case COLLISIONCHECK_AFFECT:
            tileCollision_affectTile();
            break;

            case COLLISIONCHECK_AFFECT_THEN_AFFECTEDBY:
            break;

            default: break;
        }
        //TILE_COLLISION_FUNCTIONS[tileCollisionFunctionID].invoke();

        //Entity Collision via Level Octree.
        switch(entityCheckType)
        {
            case COLLISIONCHECK_AFFECTEDBY:
            entityCollision_affectedByEntity(collisionObject_Octree);
            break;

            case COLLISIONCHECK_AFFECT:
            entityCollision_affectEntity(collisionObject_Octree);
            break;

            case COLLISIONCHECK_AFFECT_THEN_AFFECTEDBY:
            break;

            default: break;
        }
    }

    /**To be called from Level. Applies the final velocity after collision checks.*/
    public void applyVelocity(){entity.f_addPosition(f_velocity);}


    //
    //Tile Collision
    //
    //private static final TileCollision_Function[] TILE_COLLISION_FUNCTIONS =
    //{
        //PhysicsComponent::tileCollision_affectedByTile,
        //PhysicsComponent::tileCollision_affectTile,
        //PhysicsComponent::tileCollision_affectTile_then_affectedByTile,
        //
        //TODO might return boolean to say whether or not to perform other collider's response after.
    //};
    //private byte tileCollisionFunctionID = 0;

    //Used for perform specific collision responses with tiles.
    private int[] currentTileInfo = {0x00000000};
    //public int getCurrentTileInfo(){return currentTileInfo[0];}

    //public static final Vector3f tilePosition = new Vector3f();
    public static final fixedVector3 f_oldEntityPosition = new fixedVector3(),
    f_currentTilePosition = new fixedVector3();


    /**
     * Checks for Collision with nearby Tiles in the Level.
     * This function is used when tileCheckType is COLLISIONCHECK_AFFECTEDBY, allowing the Entity to be affected by tiles.
     */
    private void tileCollision_affectedByTile()
    {
        f_oldEntityPosition.set(this.f_entityPosition);

        /*
        //Determine the Box area to check for collisions.
        widthToCheck = (int)(collisionShape.getWidth() / Level.TILE_SIZE) + 1;
        //widthToCheck = (int)((collisionShape.getWidth() + Math.abs(velocity.x)) / Level.TILE_SIZE) + 1;
        if(collisionShape.getWidth() % Level.TILE_SIZE != 0){widthToCheck++;}
        //
        heightToCheck = (int)(collisionShape.getHeight() / Level.TILE_SIZE) + 1;
        //heightToCheck = (int)((collisionShape.getHeight() + Math.abs(velocity.y)) / Level.TILE_SIZE) + 1;
        if(collisionShape.getHeight() % Level.TILE_SIZE != 0){heightToCheck++;}
        //
        depthToCheck = (int)(collisionShape.getDepth() / Level.TILE_SIZE) + 1;
        //depthToCheck = (int)((collisionShape.getDepth() + Math.abs(velocity.z)) / Level.TILE_SIZE) + 1;
        if(collisionShape.getDepth() % Level.TILE_SIZE != 0){depthToCheck++;}

        widthSegment = collisionShape.getWidth() / (widthToCheck-1);
        heightSegment = collisionShape.getHeight() / (heightToCheck-1);
        depthSegment = collisionShape.getDepth() / (depthToCheck-1);
        */

        //
        //Setup.
        //

        //Calculate the maximum number of tiles we can check.
        int widthToCheck = (collisionShape.f_getWidth() >> Level.FIXED_TILE_BITS) + 1;
        if(collisionShape.f_getWidth() % Level.FIXED_TILE_SIZE != 0){widthToCheck++;}
        //
        int heightToCheck = (collisionShape.f_getHeight() >> Level.FIXED_TILE_BITS) + 1;
        if(collisionShape.f_getHeight() % Level.FIXED_TILE_SIZE != 0){heightToCheck++;}
        //
        int depthToCheck = (collisionShape.f_getDepth() >> Level.FIXED_TILE_BITS) + 1;
        if(collisionShape.f_getDepth() % Level.FIXED_TILE_SIZE != 0){depthToCheck++;}

        //TODO Optimize multi-colllsion fix?
        int[] checkedTiles = new int[(widthToCheck) * (heightToCheck) * (depthToCheck)];
        int collidedTiles = 0;

        //Declare these for the loops.
        int column, row, floor;


        //
        //Bottom and top checks (Z).
        //

        //Calculate what tiles we need to check (excluding z-velocity.)
        int tileX = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS;
        widthToCheck = ((f_entityPosition.x + collisionShape.f_right() + f_velocity.x - 1) >> Level.FIXED_TILE_BITS) - tileX + 1;
        //
        int tileY = (f_entityPosition.y + collisionShape.f_back() + f_velocity.y) >> Level.FIXED_TILE_BITS;
        heightToCheck = ((f_entityPosition.y + collisionShape.f_front() + f_velocity.y - 1) >> Level.FIXED_TILE_BITS) - tileY + 1;
        //
        int tileZ = (f_entityPosition.z + collisionShape.f_bottom()) >> Level.FIXED_TILE_BITS;
        depthToCheck = ((f_entityPosition.z + collisionShape.f_top() - 1) >> Level.FIXED_TILE_BITS) - tileZ + 1;
        
        //Affect the order tiles aare checked.
        boolean movingLeft = f_velocity.x <= 0,
        movingUp = f_velocity.y <= 0,
        movingBelow = f_velocity.z <= 0;

        //System.out.println(collisionShape.getWidth() + " " + widthToCheck);

        
        //Start loop
        int loopInc = depthToCheck, loopValue = 0 - loopInc;
        if(movingBelow){loopInc = -(depthToCheck-1); loopValue = (depthToCheck-1) - loopInc;}
        do//WOW! First instance of a Do-While loop.
        {
            //Increment loop value.
            loopValue += loopInc;

            //Calculate current floor.
            floor = tileZ + loopValue;

            //for(int y = 0; y <= heightToCheck; y++)
            for(int y = (movingUp) ? heightToCheck-1 : 0; (movingUp && y >= 0) || (!movingUp && y < heightToCheck); y += (movingUp) ? -1 : 1)
            {
                //Calculate current row to check.
                row = tileY + y;

                //for(int x = 0; x <= widthToCheck; x++)
                for(int x = (movingLeft) ? widthToCheck-1 : 0; (movingLeft && x >= 0) || (!movingLeft && x < widthToCheck); x += (movingLeft) ? -1 : 1)
                {
                    //column = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS + x;
                    //row = (f_entityPosition.y + collisionShape.f_back() + f_velocity.y) >> Level.FIXED_TILE_BITS + y;
                    //floor = (f_entityPosition.z + collisionShape.f_bottom()) >> Level.FIXED_TILE_BITS + loopValue;


                    //Calculate current column to check.
                    column = tileX + x;

                    //Calculate Tile's position and get the Tile itself.
                    f_currentTilePosition.set(column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    Tile t = level.getTile(column, row, floor, currentTileInfo);

                    //Is this Tile not a void tile?
                    if(t != Tiles.getTileType(0))
                    {
                        int tileIndex = (column + row * level.getWidth()) + (floor * (level.getWidth() * level.getHeight()));

                        boolean canCheck = true;
                        for(int i = 0; i < collidedTiles; i++)
                        {
                            if(checkedTiles[i] == tileIndex)
                            {
                                canCheck = false;
                                break;
                            }
                        }
                        if(!canCheck){continue;}

                        //TODO Allow change of what collision response should be used.
                        //This response
                        //Its response (Tiles get destoryed by explosions)
                        //This and its response?
                        
                        //If there is a collision...
                        //if(this.collisionShape.performCollision(f_entityPosition, f_velocity, t.getShape(), f_currentTilePosition))
                        if(t.getShape().tileCollidedBy(collisionShape, f_entityPosition, f_velocity, f_currentTilePosition, (byte)(currentTileInfo[0] & Tiles.TILE_FORCES_PORTION)))
                        {
                            //Run the Tile's collision response.
                            t.runCollisionResponse
                            (
                                entity, this, this.collisionShape, this.f_entityPosition, this.f_velocity, f_oldEntityPosition,//This PhysicsComponent's information.
                                f_currentTilePosition, currentTileInfo[0], level.getMaterial(column, row, floor)//The current Tile's information.
                            );

                            //Never check for this tile again.
                            if(collidedTiles < checkedTiles.length)//<- Unfortunatly, game might crash without this limit.
                            {
                                checkedTiles[collidedTiles] = tileIndex;
                                collidedTiles++;
                            }
                        }   
                    }
                }
            }
        }
        while((movingBelow && loopValue > 0) || (!movingBelow && loopValue < depthToCheck-1));

        
        //
        //Back and front checks (Y).
        //

        //Calculate what tiles we need to check (excluding y-velocity).
        tileX = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS;
        widthToCheck = ((f_entityPosition.x + collisionShape.f_right() + f_velocity.x - 1) >> Level.FIXED_TILE_BITS) - tileX + 1;
        //
        tileY = (f_entityPosition.y + collisionShape.f_back()) >> Level.FIXED_TILE_BITS;
        heightToCheck = ((f_entityPosition.y + collisionShape.f_front() - 1) >> Level.FIXED_TILE_BITS) - tileY + 1;
        //
        tileZ = (f_entityPosition.z + collisionShape.f_bottom() + f_velocity.z) >> Level.FIXED_TILE_BITS;
        depthToCheck = ((f_entityPosition.z + collisionShape.f_top() + f_velocity.z - 1) >> Level.FIXED_TILE_BITS) - tileZ + 1;

        //System.out.println(tileX + " " + tileY + " " + tileZ);
        //System.out.println(widthToCheck + " " + heightToCheck + " " + depthToCheck);

        //Start loop.
        if(movingUp){loopInc = -(heightToCheck-1); loopValue = (heightToCheck-1) - loopInc;}
        else{loopInc = heightToCheck-1; loopValue = 0 - loopInc;}
        do
        {
            //Increment loop value.
            loopValue += loopInc;

            //Calculate current row.
            row = tileY + loopValue;

            for(int z = (movingBelow) ? depthToCheck-1 : 0; (movingBelow && z >= 0) || (!movingBelow && z < depthToCheck); z += (movingBelow) ? -1 : 1)
            {
                //Calculate current floor to check.
                floor = tileZ + z;

                //for(int x = 0; x <= widthToCheck; x++)
                for(int x = (movingLeft) ? widthToCheck-1 : 0; (movingLeft && x >= 0) || (!movingLeft && x < widthToCheck); x += (movingLeft) ? -1 : 1)
                {
                    //column = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS + x;
                    //row = (f_entityPosition.y + collisionShape.f_back()) >> Level.FIXED_TILE_BITS + loopValue;
                    //floor = (f_entityPosition.z + collisionShape.f_bottom() + f_velocity.z) >> Level.FIXED_TILE_BITS + z;


                    //Calculate current column to check.
                    column = tileX + x;

                    //Calculate Tile's position and get the Tile itself.
                    f_currentTilePosition.set(column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    Tile t = level.getTile(column, row, floor, currentTileInfo);
                    
                    //Is this not a void tile?
                    if(t != Tiles.getTileType(0))
                    {
                        int tileIndex = (column + row * level.getWidth()) + (floor * (level.getWidth() * level.getHeight()));

                        //Has this Tile been checked before?
                        boolean canCheck = true;
                        for(int i = 0; i < collidedTiles; i++)
                        {
                            if(checkedTiles[i] == tileIndex)
                            {
                                canCheck = false;
                                break;
                            }
                        }
                        if(!canCheck){continue;}

                        //If there is a collision...
                        //if(this.collisionShape.performCollision(f_entityPosition, f_velocity, t.getShape(), f_currentTilePosition))
                        if(t.getShape().tileCollidedBy(collisionShape, f_entityPosition, f_velocity, f_currentTilePosition, (byte)(currentTileInfo[0] & Tiles.TILE_FORCES_PORTION)))
                        {
                            //Run the Tile's collision response.
                            t.runCollisionResponse
                            (
                                entity, this, this.collisionShape, this.f_entityPosition, this.f_velocity, f_oldEntityPosition,//This PhysicsComponent's information.
                                f_currentTilePosition, currentTileInfo[0], level.getMaterial(column, row, floor)//The current Tile's information.
                            );

                            //Never check for this tile again.
                            if(collidedTiles < checkedTiles.length)//<- Unfortunatly, game might crash without this limit.
                            {
                                checkedTiles[collidedTiles] = tileIndex;
                                collidedTiles++;
                            }
                        }  
                    }
                }
            }
        }
        while((movingUp && loopValue > 0) || (!movingUp && loopValue < heightToCheck-1));


        //
        //Left and right checks (X).
        //

        //Calculate what tiles we need to check (excluding no velocity).
        tileX = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS;
        widthToCheck = ((f_entityPosition.x + collisionShape.f_right() + f_velocity.x - 1) >> Level.FIXED_TILE_BITS) - tileX + 1;
        //
        tileY = (f_entityPosition.y + collisionShape.f_back() + f_velocity.y) >> Level.FIXED_TILE_BITS;
        heightToCheck = ((f_entityPosition.y + collisionShape.f_front() + f_velocity.y - 1) >> Level.FIXED_TILE_BITS) - tileY + 1;
        //
        tileZ = (f_entityPosition.z + collisionShape.f_bottom() + f_velocity.z) >> Level.FIXED_TILE_BITS;
        depthToCheck = ((f_entityPosition.z + collisionShape.f_top() + f_velocity.z - 1) >> Level.FIXED_TILE_BITS) - tileZ + 1;

        //System.out.println(tileX + " " + tileY + " " + tileZ);
        //System.out.println(widthToCheck + " " + heightToCheck + " " + depthToCheck);

        //Start loop.
        if(movingLeft){loopInc = -(widthToCheck-1); loopValue = (widthToCheck-1) - loopInc;}
        else{loopInc = widthToCheck-1; loopValue = 0 - loopInc;}
        do
        {
            //Increment loop value.
            loopValue += loopInc;

            //Calculate current column.
            column = tileX + loopValue;

            //for(int y = 0; y <= heightToCheck; y++)
            for(int y = (movingUp) ? heightToCheck-1 : 0; (movingUp && y >= 0) || (!movingUp && y < heightToCheck); y += (movingUp) ? -1 : 1)
            {
                //Calculate current row to check.
                row = tileY + y;

                //for(int z = depthToCheck; z >= 0; z--)
                //for(int z = 0; z <= depthToCheck; z++)
                for(int z = (movingBelow) ? depthToCheck-1 : 0; (movingBelow && z >= 0) || (!movingBelow && z < depthToCheck); z += (movingBelow) ? -1 : 1)
                {
                    //column = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS + loopValue;
                    //row = (f_entityPosition.y + collisionShape.f_back() + f_velocity.y) >> Level.FIXED_TILE_BITS + y;
                    //floor = (f_entityPosition.z + collisionShape.f_bottom() + f_velocity.z) >> Level.FIXED_TILE_BITS + z;


                    //Calculate current floor to check.                    
                    floor = tileZ + z;

                    //System.out.println(column + " " + row + " " + floor);

                    //Calculate Tile's position and get the Tile itself.
                    f_currentTilePosition.set(column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    Tile t = level.getTile(column, row, floor, currentTileInfo);

                    //Is this not a void tile?
                    if(t != Tiles.getTileType(0))
                    {
                        int tileIndex = (column + row * level.getWidth()) + (floor * (level.getWidth() * level.getHeight()));

                        //Has this tile been checked on before?
                        boolean canCheck = true;
                        for(int i = 0; i < collidedTiles; i++)
                        {
                            if(checkedTiles[i] == tileIndex)
                            {
                                canCheck = false;
                                break;
                            }
                        }
                        if(!canCheck){continue;}

                        //If there is a collision...
                        //if(this.collisionShape.performCollision(f_entityPosition, f_velocity, t.getShape(), f_currentTilePosition))
                        if(t.getShape().tileCollidedBy(collisionShape, f_entityPosition, f_velocity, f_currentTilePosition, (byte)(currentTileInfo[0] & Tiles.TILE_FORCES_PORTION)))
                        {
                            //Run the Tile's collision response.
                            t.runCollisionResponse
                            (
                                entity, this, this.collisionShape, this.f_entityPosition, this.f_velocity, f_oldEntityPosition,//This PhysicsComponent's information.
                                f_currentTilePosition, currentTileInfo[0], level.getMaterial(column, row, floor)//The current Tile's information.
                            );

                            //Never check for this tile again.
                            if(collidedTiles < checkedTiles.length)//<- Unfortunatly, game might crash without this limit.
                            {
                                checkedTiles[collidedTiles] = tileIndex;
                                collidedTiles++;
                            }
                        }  
                    }
                }
            }

            //System.out.println(loopValue);
        }
        while((movingLeft && loopValue > 0) || (!movingLeft && loopValue < widthToCheck-1));

        //Set current TileInfo to 0 to let the entity know we are no longer colliding with tiles.
        currentTileInfo[0] = 0;
    }




    //private int widthToCheck, heightToCheck, depthToCheck;
    //private @fixed int f_widthSegment, f_heightSegment, f_depthSegment;

    /**Checks for Collision with nearby Tiles in the Level.
    private void tileCollision_OLD()
    {
        //Determine the Box area to check for collisions.
        widthToCheck = (collisionShape.getWidth() >> Level.TILE_BITS) + 1;
        if(collisionShape.getWidth() % Level.TILE_SIZE != 0){widthToCheck++;}
        //
        heightToCheck = (collisionShape.getHeight() >> Level.TILE_BITS) + 1;
        if(collisionShape.getHeight() % Level.TILE_SIZE != 0){heightToCheck++;}
        //
        depthToCheck = (collisionShape.getDepth() >> Level.TILE_BITS) + 1;
        if(collisionShape.getDepth() % Level.TILE_SIZE != 0){depthToCheck++;}

        //System.out.println("w: " + shape.getWidth() + " " + widthToCheck + " " + fixed(widthToCheck-1));

        f_widthSegment = f_div( fixed(collisionShape.getWidth()), fixed(widthToCheck-1) );
        f_heightSegment = f_div( fixed(collisionShape.getHeight()), fixed(heightToCheck-1) );
        f_depthSegment = f_div( fixed(collisionShape.getDepth()), fixed(depthToCheck-1) );
        //
        //int middleX = widthToCheck/2, middleY = heightToCheck/2, middleZ = depthToCheck/2;
        //
        int column, row, floor;
        //
        //TODO Optimize multi-colllsion fix?
        int[] checkedTiles = new int[(widthToCheck) * (heightToCheck) * (depthToCheck)];
        int collidedTiles = 0;
        
        boolean movingLeft = f_velocity.x <= 0,
        movingUp = f_velocity.y <= 0,
        movingBelow = f_velocity.z <= 0;

        //System.out.println(shape.getWidth() + " " + widthToCheck);

        //Bottom and top checks.
        //for(int z = depthToCheck; z >= 0; z -= depthToCheck)
        //for(int z = 0; z <= depthToCheck; z += depthToCheck)
        for(int z = (movingBelow) ? depthToCheck-1 : 0; (movingBelow && z >= 0) || (!movingBelow && z < depthToCheck); z += (movingBelow) ? -(depthToCheck-1) : depthToCheck)
        {
            //for(int y = 0; y <= heightToCheck; y++)
            for(int y = (movingUp) ? heightToCheck-1 : 0; (movingUp && y >= 0) || (!movingUp && y < heightToCheck); y += (movingUp) ? -1 : 1)
            {
                //for(int x = 0; x <= widthToCheck; x++)
                for(int x = (movingLeft) ? widthToCheck-1 : 0; (movingLeft && x >= 0) || (!movingLeft && x < widthToCheck); x += (movingLeft) ? -1 : 1)
                {
                    //int xa = ((x+middleX) % (widthToCheck));
                    int xa = x;
                    //int ya = ((y+middleY) % (heightToCheck));
                    int ya = y;
                    int za = z;

                    column= ((entity.f_getX() + collisionShape.f_left()) +
                    (f_widthSegment * xa) + f_velocity.x
                    ) >> Level.FIXED_TILE_BITS;

                    row=    ((entity.f_getY() + collisionShape.f_back()) +
                    (f_heightSegment * ya) + f_velocity.y
                    ) >> Level.FIXED_TILE_BITS;

                    floor=  ((entity.f_getZ() + collisionShape.f_bottom()) +
                    (f_depthSegment * za)// + f_velocity.z
                    ) >> Level.FIXED_TILE_BITS;

                    //Calculate Tile's position and get the Tile itself.
                    f_currentTilePosition.set(column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    Tile t = level.getTile(column, row, floor, currentTileInfo);

                    //Is this not a void Tile?
                    if(t != Tiles.getTileType(0))
                    {
                        int tileIndex = (column + row * level.getWidth()) + (floor * (level.getWidth() * level.getHeight()));

                        //Has this tile been checked on before?
                        boolean canCheck = true;
                        for(int i = 0; i < collidedTiles; i++)
                        {
                            if(checkedTiles[i] == tileIndex)
                            {
                                canCheck = false;
                                break;
                            }
                        }
                        if(!canCheck){continue;}

                        //If there is a collision.
                        if(this.collisionShape.performCollision(f_entityPosition, f_velocity, t.getShape(), f_currentTilePosition))
                        {
                            //Run the Tile's collision response.
                            t.runCollisionResponse
                            (
                                entity, this, this.collisionShape, this.f_entityPosition, this.f_velocity,//This PhysicsComponent's information.
                                f_currentTilePosition, currentTileInfo[0], level.getMaterial(column, row, floor)//The current Tile's information.
                            );

                            //Never check for this tile again.
                            checkedTiles[collidedTiles] = tileIndex;
                            collidedTiles++;
                        }  
                    }
                }
            }
        }

        
        //Back and front checks.
        //for(int y = 0; y <= heightToCheck; y += heightToCheck)
        for(int y = (movingUp) ? heightToCheck-1 : 0; (movingUp && y >= 0) || (!movingUp && y < heightToCheck); y += (movingUp) ? -(heightToCheck-1) : heightToCheck)
        {
            //for(int z = depthToCheck; z >= 0; z--)
            //for(int z = 0; z <= depthToCheck; z ++)
            for(int z = (movingBelow) ? depthToCheck-1 : 0; (movingBelow && z >= 0) || (!movingBelow && z < depthToCheck); z += (movingBelow) ? -1 : 1)
            {
                //for(int x = 0; x <= widthToCheck; x++)
                for(int x = (movingLeft) ? widthToCheck-1 : 0; (movingLeft && x >= 0) || (!movingLeft && x < widthToCheck); x += (movingLeft) ? -1 : 1)
                {
                    //int xa = ((x+middleX) % (widthToCheck));
                    int xa = x;
                    int ya = y;
                    //int za = ((z+middleZ) % (depthToCheck));
                    int za = z;

                    column= ((entity.f_getX() + collisionShape.f_left()) +
                    (f_widthSegment * xa) + f_velocity.x
                    ) >> Level.FIXED_TILE_BITS;

                    row=    ((entity.f_getY() + collisionShape.f_back()) +
                    (f_heightSegment * ya)// + f_velocity.y
                    ) >> Level.FIXED_TILE_BITS;

                    floor=  ((entity.f_getZ() + collisionShape.f_bottom()) +
                    (f_depthSegment * za) + f_velocity.z
                    ) >> Level.FIXED_TILE_BITS;

                    //Calculate Tile's position and get the Tile itself.
                    f_currentTilePosition.set(column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    Tile t = level.getTile(column, row, floor, currentTileInfo);

                    //Is this not a void Tile?
                    if(t != Tiles.getTileType(0))
                    {
                        int tileIndex = (column + row * level.getWidth()) + (floor * (level.getWidth() * level.getHeight()));

                        //Has this tile been checked on before?
                        boolean canCheck = true;
                        for(int i = 0; i < collidedTiles; i++)
                        {
                            if(checkedTiles[i] == tileIndex)
                            {
                                canCheck = false;
                                break;
                            }
                        }
                        if(!canCheck){continue;}

                        //If there is a collision.
                        if(this.collisionShape.performCollision(f_entityPosition, f_velocity, t.getShape(), f_currentTilePosition))
                        {
                            //Run the Tile's collision response.
                            t.runCollisionResponse
                            (
                                entity, this, this.collisionShape, this.f_entityPosition, this.f_velocity,//This PhysicsComponent's information.
                                f_currentTilePosition, currentTileInfo[0], level.getMaterial(column, row, floor)//The current Tile's information.
                            );

                            //Never check for this tile again.
                            checkedTiles[collidedTiles] = tileIndex;
                            collidedTiles++;
                        }  
                    }
                }
            }
        }
        

        //Left and right checks.
        //for(int x = 0; x <= widthToCheck; x += widthToCheck)
        for(int x = (movingLeft) ? widthToCheck-1 : 0; (movingLeft && x >= 0) || (!movingLeft && x < widthToCheck); x += (movingLeft) ? -(widthToCheck-1) : widthToCheck)
        {
            //for(int y = 0; y <= heightToCheck; y++)
            for(int y = (movingUp) ? heightToCheck-1 : 0; (movingUp && y >= 0) || (!movingUp && y < heightToCheck); y += (movingUp) ? -1 : 1)
            {
                //for(int z = depthToCheck; z >= 0; z--)
                //for(int z = 0; z <= depthToCheck; z++)
                for(int z = (movingBelow) ? depthToCheck-1 : 0; (movingBelow && z >= 0) || (!movingBelow && z < depthToCheck); z += (movingBelow) ? -1 : 1)
                {
                    int xa = x;
                    //int ya = ((y+middleY) % (heightToCheck));
                    int ya = y;
                    //int za = ((z+middleZ) % (depthToCheck));
                    int za = z;

                    column= ((entity.f_getX() + collisionShape.f_left()) +
                    (f_widthSegment * xa) + f_velocity.x
                    ) >> Level.FIXED_TILE_BITS;

                    row=    ((entity.f_getY() + collisionShape.f_back()) +
                    (f_heightSegment * ya) + f_velocity.y
                    ) >> Level.FIXED_TILE_BITS;

                    floor=  ((entity.f_getZ() + collisionShape.f_bottom()) +
                    (f_depthSegment * za) + f_velocity.z
                    ) >> Level.FIXED_TILE_BITS;

                    //Calculate Tile's position and get the Tile itself.
                    f_currentTilePosition.set(column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    Tile t = level.getTile(column, row, floor, currentTileInfo);

                    //Is this not a void Tile?
                    if(t != Tiles.getTileType(0))
                    {
                        int tileIndex = (column + row * level.getWidth()) + (floor * (level.getWidth() * level.getHeight()));

                        //Has this tile been checked on before?
                        boolean canCheck = true;
                        for(int i = 0; i < collidedTiles; i++)
                        {
                            if(checkedTiles[i] == tileIndex)
                            {
                                canCheck = false;
                                break;
                            }
                        }
                        if(!canCheck){continue;}

                        //If there is a collision.
                        if(this.collisionShape.performCollision(f_entityPosition, f_velocity, t.getShape(), f_currentTilePosition))
                        {
                            //Run the Tile's collision response.
                            t.runCollisionResponse
                            (
                                entity, this, this.collisionShape, this.f_entityPosition, this.f_velocity,//This PhysicsComponent's information.
                                f_currentTilePosition, currentTileInfo[0], level.getMaterial(column, row, floor)//The current Tile's information.
                            );

                            //Never check for this tile again.
                            checkedTiles[collidedTiles] = tileIndex;
                            collidedTiles++;
                        }  
                    }
                }
            }
        }

        //Set current TileInfo to 0 to let the entity know we are no longer colliding with tiles.
        currentTileInfo[0] = 0;
    }
    */







    /**
     * Checks for Collision with nearby Tiles in the Level.
     * This function is used when tileCheckType is set to COLLISIONCHECK_AFFECT, allowing the Entity to affect Tiles.
     */
    private void tileCollision_affectTile()
    {
        int column, row, floor;

        int tileX = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS,
        widthToCheck = ((f_entityPosition.x + collisionShape.f_right() + f_velocity.x) >> Level.FIXED_TILE_BITS) - tileX + 1;
        //
        int tileY = (f_entityPosition.y + collisionShape.f_back() + f_velocity.y) >> Level.FIXED_TILE_BITS,
        heightToCheck = ((f_entityPosition.y + collisionShape.f_front() + f_velocity.y) >> Level.FIXED_TILE_BITS) - tileY + 1;
        //
        int tileZ = (f_entityPosition.z + collisionShape.f_bottom() + f_velocity.z) >> Level.FIXED_TILE_BITS,
        depthToCheck = ((f_entityPosition.z + collisionShape.f_top() + f_velocity.z) >> Level.FIXED_TILE_BITS) - tileZ + 1;

        //Loop through all tiles within area.
        for(int z = 0; z < depthToCheck; z++)
        {
            for(int y = 0; y < heightToCheck; y++)
            {
                for(int x = 0; x < widthToCheck; x++)
                {
                    //Calculate current column, row, and floor.
                    column = tileX + x;
                    row = tileY + y;
                    floor = tileZ + z;

                    //Calculate Tile's position and acquire it.
                    f_currentTilePosition.set(column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    Tile t = level.getTile(column, row, floor, currentTileInfo);

                    //Cache Tile's shape.
                    Shape3D t_Shape = t.getShape();

                    //Is this shape colliding with the tile's shape (if it's not a void tile)?
                    if(t != Tiles.getTileType(0) &&
                    this.collisionShape.performCollision(f_entityPosition, f_velocity, t_Shape, f_currentTilePosition))
                    {
                        //Run this CollisionComponent's reponse.
                        //this.collisionObject.collisionResponse.invoke();
                    }
                }
            }
        }

        //Set current TileInfo to 0 to let the entity know we are no longer colliding with tiles.
        currentTileInfo[0] = 0;
    }



    //
    //Entity Collision.
    //

    //List to be filled by the Octree.
    //TODO Maybe not have a list for every PhysicsComponent?
    private List<CollisionObject> collisionObjects = new ArrayList<CollisionObject>();

    /**Checks for Collision with other CollisionObject's in the required portions of the Level.*/
    private void entityCollision_affectedByEntity(Octree<CollisionObject> collisionObject_Octree)
    {
        //Get Collision Objects from Level Octree.
        collisionObjects.clear();
        collisionObject_Octree.retrieve(this.f_entityPosition, this.collisionShape, collisionObjects);


        //Check for Collision with each CollisionObject and respond accordingly.
        for(int i = 0; i < collisionObjects.size(); i++)
        {
            CollisionObject c = collisionObjects.get(i);

            //Skip if it's this CollisionObject or if it's inactive.
            if(c.equals(this.collisionObject) || !c.isActive()){continue;}

            //If a collision is detected with c's shape...
            if(collisionShape.performCollision(this.entity.f_getPosition(), this.f_velocity, c.getShape(), c.getEntity().f_getPosition()))
            {
                //Execute its collision response.
                c.collisionResponse.execute(this.entity, this.collisionShape, this.entity.f_getPosition(), this.f_velocity, c.getEntity().f_getPosition());
            }
        }
    }

    /**Checks for Collision with other CollisionObject's in the required portions of the Level.*/
    private void entityCollision_affectEntity(Octree<CollisionObject> collisionObject_Octree)
    {
        //Get Collision Objects from Level Octree.
        collisionObjects.clear();
        collisionObject_Octree.retrieve(this.f_entityPosition, this.collisionShape, collisionObjects);

        
        //Check for Collision with each CollisionObject and respond accordingly.
        for(int i = 0; i < collisionObjects.size(); i++)
        {
            //Cache current CollisionComponent.
            CollisionObject c = collisionObjects.get(i);

            //Skip if it's this CollisionObject or if it's inactive.
            if(c.equals(this.collisionObject) || !c.isActive()){continue;}

            //If a collision is detected with c's shape...
            if(collisionShape.performCollision(this.entity.f_getPosition(), this.f_velocity, c.getShape(), c.getEntity().f_getPosition()))
            {
                //Execute this component's collision response.
                this.collisionObject.collisionResponse.execute
                (this.entity, this.collisionShape, this.entity.f_getPosition(), this.f_velocity, c.getEntity().f_getPosition());
            }
        }
    }



    //
    //Misc.
    //

    //Shape offset getters.
    public final @fixed int f_getXOffset(){return collisionShape.f_getXOffset();}
    public final @fixed int f_getYOffset(){return collisionShape.f_getYOffset();}
    public final @fixed int f_getZOffset(){return collisionShape.f_getZOffset();}

    //Direction Getter/Setter
    public fixedVector2 f_getDirection(){return f_direction;}
    public void f_setDirection(@fixed int f_x, @fixed int f_y){this.f_direction.set(f_x, f_y);}

    //Velocity Magnitude Getter/Setter
    public @fixed int f_getVelocityaMagnitude(){return f_velocityMagnitude;}
    public void f_setVelocityMagnitude(@fixed int f_v){this.f_velocityMagnitude = f_v;}

    //Velocity Getters/Setters
    public final @fixed int f_getXVelocity(){return f_velocity.x;}
    public final void f_setXVelocity(@fixed int xVelocity){f_velocity.x = xVelocity;}
    //
    public final @fixed int f_getYVelocity(){return f_velocity.y;}
    public final void f_setYVelocity(@fixed int yVelocity){f_velocity.y = yVelocity;}
    //
    public final @fixed int f_getZVelocity(){return f_velocity.z;}
    public final void f_setZVelocity(@fixed int zVelocity){f_velocity.z = zVelocity;}
    //
    public final fixedVector3 f_getVelocity(){return f_velocity;}
    public final fixedVector3 f_getTargetVelocity(){return f_targetVelocity;}


    //Accelation Getter/Setter
    public @fixed int f_getAccelaration(){return f_acceleration;}
    public void f_setAccelaration(@fixed int f_accelaration){this.f_acceleration = f_accelaration;}

    //Mass Getter/Setter
    public @fixed int f_getMass(){return f_mass;}
    public void f_setMass(@fixed int f_mass){this.f_mass = f_mass;}

    //Gravity Getter/Setter
    public @fixed int f_getGravity(){return f_gravity;}
    public void f_setGravity(@fixed int f_gravity){this.f_gravity = f_gravity;}

    //Current coefficient of friction Getter/Setter.
    public @fixed int f_getCurrent_coFriction(){return this.f_current_coFriction;}
    public void f_setCurrent_coFriction(@fixed int f_current_coFriction){this.f_current_coFriction = f_current_coFriction;}

    //Target Velocity Multiplier Getter/Setter.
    public @fixed int f_getTargetVelocityMultiplier(){return this.f_targetVelocityMultiplier;}
    public void f_setTargetVelocityMultiplier(@fixed int f_tvm){this.f_targetVelocityMultiplier = f_tvm;}

    //Terminal Velocity Getter/Setter.
    public @fixed int f_getTerminalVelocity(){return f_targetVelocity.z;}
    public void f_setTerminalVelocity(@fixed int f_terminalVelocity){f_targetVelocity.z = f_terminalVelocity;}

    /**Calls level.getTile().*/
    public final Tile levelGetTile(int column, int row, int floor){return level.getTile(column, row, floor);}


    /*
     * Flags and whatnot.
     */

    //Determines if this CollisionObject is on the ground / able to jump or somethin'...
    private boolean grounded = true;
    public boolean isGrounded(){return grounded;}
    public void setGrounded(boolean grounded){this.grounded = grounded;}

    

    


    //
    //finalX = initiaX + (t * distanceX)
    //finalX - initialX = t * distanceX
    //(finalX - initialX) / distanceX = t
    //
    //(a_initialX + (t * a_distanceX)) - (b_initialX + (t * b_distanceX)) = 0 because they result to the same point.
    // \/
    //(a_initialX - b_initialX) + (t * (a_distanceX - b_distanceX)) = 0 combine the two
    //t * (a_distanceX - b_distanceX) = 0 - (a_initialX - b_initialX) subtract initial
    //t = (0 - (a_initialX - b_initialX)) / (a_distanceX - b_distanceX) divide distance
    //
    //(0 - (a_initialX - b_initialX)) / (a_distanceX - b_distanceX) = t
    //(0 - initialX) / distanceX = t equation confirmed
    //
    //t should range from 0.0 to 1.0. If outside this range (will collide later or would have collided earlier), no collision is made.
    //
    //From here, set objects using t * velocity and add velocities together (possibly subtract based on leftover distance).
    //
    //Under current idea, entities must have a PhysicsComponent to collide with tiles...
    //
    //-Explosions must check for both tiles and entities because pots don't move.
    //-Explosions are not affected by gravity or friction, which causes problems for ice-tiles.
    //
    //When Player collides with an item, the item runs its response, giving the player it effects.
    //-Simply requires Player as Input, which is an Entity.
    //
    //When Explosion collides with player, explosion runs its response, damaging the player.
    //-Player, as Entity, to damage health.
    //-PhysicsComponent for knockback.
    //
    //When Player collides with SoftBlock
    //-Shape to set position.
    //-PhysicsComponent to set velocity.


    public void render(Screen screen, float scale)
    {
        int tileX = (f_entityPosition.x + collisionShape.f_left() + f_velocity.x) >> Level.FIXED_TILE_BITS,
        tileXSpan = ((f_entityPosition.x + collisionShape.f_right() + f_velocity.x - 1) >> Level.FIXED_TILE_BITS) - tileX + 1;
        //
        int tileY = (f_entityPosition.y + collisionShape.f_back() + f_velocity.y) >> Level.FIXED_TILE_BITS,
        tileYSpan = ((f_entityPosition.y + collisionShape.f_front() + f_velocity.y - 1) >> Level.FIXED_TILE_BITS) - tileY + 1;
        //
        int tileZ = (f_entityPosition.z + collisionShape.f_bottom() + f_velocity.z) >> Level.FIXED_TILE_BITS,
        tileZSpan = ((f_entityPosition.z + collisionShape.f_top() + f_velocity.z - 1) >> Level.FIXED_TILE_BITS) - tileZ + 1;

        int column, row, floor;

        for(int z = 0; z < tileZSpan; z++)
        {
            //Current floor.
            floor = tileZ + z;

            for(int y = 0; y < tileYSpan; y++)
            {
                //Current row.
                row = tileY + y;

                for(int x = 0; x < tileXSpan; x++)
                {
                    //Current column.
                    column = tileX + x;
                    
                    //Get the current Tile.
                    Tile t = level.getTile(column, row, floor);

                    //Render its shape if not a void tile.
                    if(t != Tiles.getTileType(0))
                    {
                        t.getShape().render(screen, scale, column << Level.FIXED_TILE_BITS, row << Level.FIXED_TILE_BITS, floor << Level.FIXED_TILE_BITS);
                    }
                }
            }
        }
    }
}
