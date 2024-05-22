package JettersR.Entities;
/**
 * Base class for all Entities in the game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 9/1/2023
 */
import org.joml.Vector3f;

import JettersR.Level;
import JettersR.Graphics.Screen;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

//import static JettersR.Util.Fixed.*;

public abstract class Entity
{
    //Entity Reference simply to act as a "null"  entity
    //public static final Entity nullEntity = new Entity(-1, -1, -1);

    //Level Reference
    protected transient Level level = null;

    //Tells the Level if this Entity should be removed.
    protected transient boolean shouldRemove = false;

    //Position of this Entity.
    //protected final Vector3f position = new Vector3f();
    //@JsonSerialize(as=fixedVector3.class)
    protected fixedVector3 f_position = new fixedVector3();

    /**
     * Default Constructor.
     * 
     * Every entity MUST have a default, no argument constructor so it can be deserialized.
     * It is recommended to perform default functionallity in this constructor that is NOT
     * dependent on the presence of variables to be deserialized.
     */
    protected Entity(){}

    /**Constructor.*/
    protected Entity(@fixed int f_x, @fixed int f_y, @fixed int f_z)
    {f_position.set(f_x, f_y, f_z);}

    /**
     * Initialization function.
     * 
     * Since this function is ran after deserialization is finished, it is recommended to
     * perform functionallity in this function that IS dependent on the presence of
     * any deserialized variables.
     * 
     * @param level the pointer to the level this Entity is in.
     */
    public abstract void init(Level level);

    //Level pointer Getter.
    public final Level getLevel(){return level;}

    //Position Getters.
    //public final float getX(){return position.x;}
    //public final float getY(){return position.y;}
    //public final float getZ(){return position.z;}
    //public final Vector3f getPosition(){return position;}
    //
    public final @fixed int f_getX(){return f_position.x;}
    public final @fixed int f_getY(){return f_position.y;}
    public final @fixed int f_getZ(){return f_position.z;}
    public final fixedVector3 f_getPosition(){return f_position;}
    public final Vector3f getPosition3f(){return f_position.toVector3f();}

    //Position Setters.
    //public final void setX(float x){position.x = x;}
    //public final void setY(float y){position.y = y;}
    //public final void setZ(float z){position.z = z;}
    //public final void setPosition(float x, float y, float z){position.set(x, y, z);}
    //public final void setPosition(Vector3f p){position.set(p);}
    //
    public final void f_setX(@fixed int x){f_position.x = x;}
    public final void f_setY(@fixed int y){f_position.y = y;}
    public final void f_setZ(@fixed int z){f_position.z = z;}
    public final void f_setPosition(@fixed int x, @fixed int y, @fixed int z){f_position.set(x, y, z);}
    public final void f_setPosition(fixedVector3 pos){f_position.set(pos);}

    //Position Adders.
    //public final void addPosition(Vector3f velocity){position.add(velocity);}
    public final void f_addPosition(fixedVector3 f_velocity){f_position.add(f_velocity);}

    //Component Stuff...
    //public abstract CollisionObject getCollisionObject();
    //public abstract Light getLight();

    /**Updates this Entity.*/
    //public abstract void update(float timeMod);
    public abstract void update(@fixed int f_timeMod);

    /**Deletes this Entity. This should be overriden if resources need to be closed.*/
    public void delete(){this.shouldRemove = true;}
    public final boolean shouldRemove(){return shouldRemove;}

    /**Renders this Entity.*/
    public abstract void render(Screen screen, float scale);

    //public abstract void getFront();
    //public abstract void getTop();
}
