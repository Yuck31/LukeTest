package JettersR.Entities.Components;
/**
 * 
 */
//import org.joml.Vector3f;

import JettersR.Util.fixedVector3;
import JettersR.Util.Shapes.Shapes3D.Shape3D;

public interface OctreeObject
{
    //public abstract Vector3f getPosition();
    public abstract fixedVector3 f_getPosition();
    
    public abstract Shape3D getShape();
}
