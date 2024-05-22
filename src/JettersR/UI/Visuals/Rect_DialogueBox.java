package JettersR.UI.Visuals;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/18/2023
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;

public class Rect_DialogueBox extends DialogueBox
{
    public static final float DARK_PERCENT = 0.75f;

    //Inner color.
    private Vector4f innerColor;

    /**Constructor.*/
    public Rect_DialogueBox(int x, int y, int width, int height, Vector4f color, Vector4f innerColor)
    {
        //Set position, dimensions, and color.
        super(x, y, width, height, color);

        //Set inner color.
        this.innerColor = innerColor;
    }

    /**One color Constructor.*/
    public Rect_DialogueBox(int x, int y, int width, int height, Vector4f color)
    {this(x, y, width, height, color, new Vector4f(color.x * DARK_PERCENT, color.y * DARK_PERCENT, color.z * DARK_PERCENT, color.w));}

    @Override
    public void setColor(Vector4f color)
    {
        this.color = color;
        this.innerColor.set(color.x * DARK_PERCENT, color.y * DARK_PERCENT, color.z * DARK_PERCENT, color.w);
    }

    @Override
    //Size setter.
    public void setSize(float percent, int targetWidth, int targetHeight)
    {
        //Calculate new dimensions.
        int w = (int)(percent * targetWidth);
        int h = (int)(percent * targetHeight);

        //Clamp them.
        this.width = (w < 0) ? 0 : w;
        this.height = (h < 0) ? 0 : h;
    }

    @Override
    /**Render function. */
    public void render(Screen screen, float xOffset, float yOffset)
    {
        //Calculate offset coordinates.
        int xa = (int)(this.position.x + xOffset) - (width >> 1),
        ya = (int)(this.position.y + yOffset) - (height >> 1);

        //screen.setCropRegion(xa, ya, xa + width, ya + height);

        //Draw outer rim.
        screen.drawRect(xa, ya, width, height, color, false);

        //Fill inner rect.
        screen.fillRect(xa+1, ya+1, width-2, height-2, innerColor, false);
    }
}
