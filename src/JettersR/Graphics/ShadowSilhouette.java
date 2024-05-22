package JettersR.Graphics;
/**
 * 4 points and a sprite...
 * 
 * Author: Luke Sullivan
 * Last Edit: 6/5/2023
 */
import org.joml.Vector3f;

public class ShadowSilhouette
{
	//TODO Implement Sprite Sillouetes
    public Sprite silhouete;

	//The points this silhouette is made up of.
    private Vector3f[] points;

	 /**Constructor.*/
	public ShadowSilhouette(Sprite silhouette, Vector3f point0, Vector3f point1, Vector3f point2, Vector3f point3)
	{
		this.silhouete = silhouette;

		this.points = new Vector3f[]
		{
			point0,
			point1,
			point2,
			point3
		};
	}
 
	//Points getter.
	public Vector3f[] getPoints(){return this.points;}
}
