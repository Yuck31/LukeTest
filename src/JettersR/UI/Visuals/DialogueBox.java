package JettersR.UI.Visuals;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/18/2023
 */
import org.joml.Vector4f;

import JettersR.UI.UIComponent;

public abstract class DialogueBox extends UIComponent
{
    //Dimensions.
    protected int width, height;

    //Color.
    protected Vector4f color = null;

    /**Constructor.*/
    public DialogueBox(int x, int y, int width, int height, Vector4f color)
    {
        //Set position.
        super(x, y);

        //Set dimensions.
        this.width = width;
        this.height = height;

        //Set color.
        this.color = color;
    }

    /**Constructor.*/
    public DialogueBox(int x, int y, Vector4f color)
    {
        //Set position.
        super(x, y);

        //Set color.
        this.color = color;
    }

    //Color Getter/Setter.
    public final Vector4f getColor(){return color;}
    public void setColor(Vector4f color){this.color = color;}

    //Width Getter/Setter.
    public final int getWidth(){return width;}
    public final void setWidth(int width){this.width = width;}

    //Height Getter/Setter.
    public final int getHeight(){return height;}
    public final void setHeight(int height){this.height = height;}

    //Size setter.
    public abstract void setSize(float percent, int targetWidth, int targetHeight);
}
