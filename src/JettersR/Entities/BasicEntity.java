package JettersR.Entities;
/**
 * Author: Luke Sullivan
 * Last Edit: 9/27/2023
 */
import JettersR.Entities.Components.PhysicsComponent;
import JettersR.Util.Annotations.fixed;

public abstract class BasicEntity extends Entity
{
    //This Entity's CollisionObject (may not always be needed)
    //protected transient CollisionObject collisionObject = null;
    protected transient PhysicsComponent physicsComponent = null;

    //Entity Direction Enum
    public static enum Direction
    {
        UP(0),
        UP_RIGHT(1),
        RIGHT(2),
        DOWN_RIGHT(3), 
        DOWN(4),
        DOWN_LEFT(5), LEFT(6), UP_LEFT(7),
        NONE(-1);

        public transient final int value;

        private Direction(int value){this.value = value;}
    }
    protected Direction direction = Direction.NONE;
    
    //Health and Damage stuff
    protected transient boolean canBeDamaged = true;
    protected int maxHealth = 0, health = maxHealth;

    /**Default Constructor.*/
    public BasicEntity(){}

    /**Constructor.*/
    //public BasicEntity(float x, float y, float z){super(x, y, z);}
    public BasicEntity(@fixed int x, @fixed int y, @fixed int z){super(x, y, z);}

    /*
    @Override
    public void init(Level level)
    {
        //Set level pointer.
        this.level = level;

        //Add physicsComponenet to the level.
        physicsComponent.init(level);
    }
    */

    //Health and Damage stuff
    public final int getHealth(){return health;}
    public final void setHealth(int health){this.health = health;}
    public void damage(int damage)
    {
        if(canBeDamaged)
        {
            this.health -= damage;
            if(this.health < 0)
            {this.health = 0;}
        }
    }
    public void heal(int health)
    {
        if(canBeDamaged)
        {
            this.health += health;
            if(this.health > maxHealth)
            {this.health = maxHealth;}
        }
    }

    public void delete()
    {
        //Of course...
        this.shouldRemove = true;

        //Remove PhysicsComponent from Level.
        level.removePhysicsComponent(this.physicsComponent);
    }

    /**Returns this Entity's CollisionObject.*/
    //@Override
    //public final CollisionObject getCollisionObject(){return this.collisionObject;}
}
