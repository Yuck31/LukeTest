package JettersR.Graphics.Animations;
/**
 * A simple frame by frame animation.
 * 
 * Author: Luke Sullivan
 * Last Edit: 5/2/2022
 */
import JettersR.Graphics.Sprite;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class FrameAnimation extends Animation
{
    //Timer.
    protected FrameAnimation_Timer timer;

    //Array of Sprites.
    protected transient Sprite[] sprites;

    /**
     * Constructor.
     * 
     * @param loopTo
     * @param sprites
     * @param f_rates
     */
    public FrameAnimation(int loopTo, Sprite[] sprites, @fixed int[] f_rates)
    {
        //Set Sprites.
        this.sprites = sprites;

        //@fixed int[] f_rates = new @fixed int[f_rates.length];
        //for(int i = 0; i < f_rates.length; i++)
        //{f_rates[i] = fixed(f_rates[i]);}

        //Create Timer.
        timer = new FrameAnimation_Timer(loopTo, f_rates);
    }

    /**
     * Constructor with one rate and a loopTo frame.
     * 
     * @param loopTo
     * @param sprites
     * @param f_rate
     */
    public FrameAnimation(int loopTo, Sprite[] sprites, @fixed int f_rate)
    {
        //Set Sprites.
        this.sprites = sprites;

        //Create Timer
        timer = new FrameAnimation_Timer(loopTo, sprites.length, fixed(f_rate));
    }

    /**Updates this animation.*/
    public final Sprite update(@fixed int f_timeMod){return sprites[timer.update(f_timeMod)];}

    public final Sprite getSprite(){return sprites[timer.getFrame()];}
    public final Sprite getSprite(int index){return sprites[index];}

    /**Returns all of the Sprites in this Animation.*/
    public final Sprite[] getSprites(){return sprites;}

    //FrameRate Getters.
    public final @fixed int f_getFrameRate(){return timer.f_getFrameRate();}
    public final @fixed int f_getFrameRate(int f){return timer.f_getFrameRate(f);}

    //Frame Getter/Setter.
    public final int getFrame(){return timer.getFrame();}
    public final void setFrame(int frame){timer.setFrame(frame);}

    /**Resets this FrameAnimation from its first frame.*/
    public final void resetAnim(){timer.resetTimer();}
}
