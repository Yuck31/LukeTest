package JettersR.UI.Visuals;
/**
 * Called Nine-Slice because it's a box made using 9 sprites (slices).
 * 
 * Author: Luke Sullivan
 * Last Edit: 1/17/2023
 */
import org.joml.Vector4f;

import JettersR.Graphics.Sprite;

public abstract class NineSliceBox extends DialogueBox
{
    //Sprite array.
    protected Sprite[] sprites = new Sprite[4];
    //0=Corner, 1=Hori-Edge, 2=Vert-Edge, 3=Center.

    /**Constructor.*/
    public NineSliceBox(int x, int y, int width, int height, Vector4f color, Sprite... sprites)
    {
        //Set position.
        super(x, y, color);

        //Set sprites.
        for(int i = 0; i < sprites.length; i++)
        {this.sprites[i] = sprites[i];}

        Sprite s = sprites[0];

        //Set dimensions.
        this.width = (width < s.getWidth() * 2) ? s.getWidth() * 2 : width;
        this.height = (height < s.getHeight() * 2) ? s.getHeight() * 2 : height;
    }
    
    //Size Setter.
    public final void setSize(float percent, int targetWidth, int targetHeight)
    {
        //Calculate new dimensions.
        int w = (int)(percent * targetWidth);
        int h = (int)(percent * targetHeight);

        //Clamp them.
        Sprite s = sprites[0];
        this.width = (w < s.getWidth() * 2) ? s.getWidth() * 2 : w;
        this.height = (h < s.getHeight() * 2) ? s.getHeight() * 2 : h;
    }
}
