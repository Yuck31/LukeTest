package JettersR.Util.Shapes.Shapes2D;
/**
 * Author: Luke Sullivan
 * Last Edit: 7/20/2022
 */

import org.joml.Vector2f;

import JettersR.Graphics.Screen;

public abstract class Shape2D
{
    protected int xOffset, yOffset;

    /**Constructor.*/
    public Shape2D(int xOffset, int yOffset)
    {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public Shape2D(){}

    //Dimension Getters/Setters
    public final int getXOffset(){return xOffset;}
    public final void setXOffset(int xOffset){this.xOffset = xOffset;}
    public final int getYOffset(){return yOffset;}
    public final void setYOffset(int yOffset){this.yOffset = yOffset;}

    public abstract int getWidth();
    public abstract int getHeight();
    
    public abstract int left();
    public abstract int right();
    public abstract int up();
    public abstract int down();

    public abstract boolean intersects(double x, double y, float thisX, float thisY);

    /*
     * Render Functions.
     */

    public abstract void render(Screen screen, float scale, float x, float y, boolean fixed);
    public final void render(Screen screen, float scale, Vector2f position, boolean fixed){render(screen, scale, position.x, position.y, fixed);}
}
