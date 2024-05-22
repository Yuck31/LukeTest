package JettersR.Entities.Components.Lights;
/**
 * 
 */
import org.joml.Vector3f;

import JettersR.Level;
import JettersR.Entities.Entity;
import JettersR.Entities.Components.OctreeObject;
//import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
//import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

//import static JettersR.Util.Fixed.*;

public abstract class Light extends Entity implements OctreeObject
{
    //Light Colors.
    private Vector3f diffuseColor, ambientColor;

    /**Constructor.*/
    public Light(@fixed int f_x, @fixed int f_y, @fixed int f_z, Vector3f diffuseColor, Vector3f ambientColor)
    {
        super(f_x, f_y, f_z);
        this.diffuseColor = diffuseColor;
        this.ambientColor = ambientColor;
    }

    public void init(Level level)
    {this.level = level;}

    //Color Getters.
    public final Vector3f getDiffuseColor(){return diffuseColor;}
    public final Vector3f getAmbientColor(){return ambientColor;}

    //ID Getter/Setter.
    //public int getID(){return ID;}
    //public void setID(int ID){this.ID = ID;}

    
    @Override
    /**Shape Getter.*/
    public abstract Shape3D getShape();


    //Radius Getter/Setter.
    public abstract float getRadius();
    public abstract void f_setRadius(@fixed int f_r);


    @Override
    //public final CollisionObject getCollisionObject(){return null;}
    //public final Light getLight(){return this;}

    public final void update(@fixed int f_timeMod){}
    //public final void render(Screen screen, float scale)
    //{
        //getShape().render(screen, scale, position);
    //}
    
}
