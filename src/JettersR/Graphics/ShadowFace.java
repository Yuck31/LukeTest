package JettersR.Graphics;
/**
 * 
 */
import org.joml.Vector3f;

public class ShadowFace
{
    //The points this face is made up of.
    private Vector3f[] points;

    /**Constructor.*/
    public ShadowFace(Vector3f... points)
    {this.points = points;}

    //Points getter.
    public Vector3f[] getPoints(){return this.points;}
}
