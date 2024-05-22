package JettersR.UI;
/**
 * Component for UI.
 * 
 * Author: Luke Sullivan
 * Last Edit: 7/6/2023
 */
import org.joml.Vector3f;

import JettersR.Graphics.Screen;

public abstract class UIComponent
{
    //Coordinates of this UIPanel. 3-Dimensional to allow for stuff like waypoints and whatnot.
    public final Vector3f position;

    /**Constructor.*/
    public UIComponent(int x, int y){position = new Vector3f(x, y, 0);}
    public UIComponent(int x, int y, int z){position = new Vector3f(x, y, z);}
    public UIComponent(Vector3f position){this.position = position;}

    //Position Getters.
    public float getX(){return position.x;}
    public float getY(){return position.y;}
    public float getZ(){return position.z;}
    public Vector3f getPosition(){return position;}

    /**
     * Renders this MenuComponent.
     * 
     * @param screen the screen object to render to.
     * @param xOffset a given X offset.
     * @param yOffset a given Y offset.
     */
    public abstract void render(Screen screen, float xOffset, float yOffset);
    //public final void render(Screen screen, int xOffset, int yOffset){render(screen, xOffset, yOffset, 0, 0, screen.getWidth(), screen.getHeight());}
}
