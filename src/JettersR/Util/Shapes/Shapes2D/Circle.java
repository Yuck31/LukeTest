package JettersR.Util.Shapes.Shapes2D;
/**
 * 
 */

import org.joml.Vector4f;

import JettersR.Graphics.Screen;

public class Circle extends Shape2D
{
    //Radius of the Cylinder.
    protected float radius = 0;

    /**Constructor.*/
    public Circle(int radius, int xOffset, int yOffset)
    {
        super(xOffset, yOffset);
        this.radius = radius;
    }

    /**Constructor.*/
    public Circle(int radius)
    {this(radius, 0, 0);}

    @Override
    public int getWidth(){return (int)(radius * 2);}
    public int getHeight(){return (int)(radius * 2);}

    @Override
    public int left(){return (int)(xOffset - radius);}
    public int right(){return (int)(xOffset + radius);}
    @Override
    public int up(){return (int)(yOffset - radius);}
    public int down(){return (int)(yOffset + radius);}

    @Override
    public boolean intersects(double x, double y, float thisX, float thisY)
    {
        float
        sideX = (float)((thisX + this.xOffset) - x),
        sideY = (float)((thisY + this.yOffset) - y),
        //
        length = (sideX * sideX) + (sideY * sideY);

        //This returns true if the distance between the two cylinders
        //Is within the sum of their radii.
        //This can be determined via Pythagorean Therom: a^2 + b^2 = c^2.
        return length < (radius * radius);
    }

    Vector4f g = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
    @Override
    public void render(Screen screen, float scale, float x, float y, boolean fixed)
    {
        int xPos = (int)((x + xOffset) * scale);
        int yPos = (int)((y + yOffset) * scale);

        screen.drawCircle(xPos, yPos, radius, 1, g, fixed);
    }
}
