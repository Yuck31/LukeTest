package JettersR.Graphics.Animations;
/**
 * Author: Luke Sullivan
 * Last Edit: 5/5/2023
 */
import JettersR.Graphics.Sprite;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class WrapAnimation extends Animation
{
    private Sprite sprite;
    private @fixed int f_x = 0, f_y = 0;
    private @fixed int f_wrapX, f_wrapY;

    public WrapAnimation(Sprite sprite, @fixed int f_wrapX, @fixed int f_wrapY, @fixed int f_x, @fixed int f_y)
    {
        this.sprite = sprite;
        this.f_wrapX = f_wrapX;
        this.f_wrapY = f_wrapY;
        this.f_x = f_x;
        this.f_y = f_y;
    }

    public WrapAnimation(Sprite sprite, @fixed int f_wrapX, @fixed int f_wrapY)
    {this(sprite, f_wrapX, f_wrapY, 0, 0);}

    public Sprite update(@fixed int f_timeMod)
    {
        f_x += (f_wrapX * f_timeMod) % fixed(sprite.getWidth());
        f_y += (f_wrapY * f_timeMod) % fixed(sprite.getHeight());
        //
        return sprite;
    }

    public Sprite getSprite(){return sprite;}
    public Sprite[] getSprites(){return new Sprite[]{sprite};}

    public int f_getX(){return (int)f_x;}
    public int f_getY(){return (int)f_y;}

    public @fixed int f_getWrapX(){return f_wrapX;}
    public @fixed int f_getWrapY(){return f_wrapY;}
}
