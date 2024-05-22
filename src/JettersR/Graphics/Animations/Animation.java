package JettersR.Graphics.Animations;
/**
 * Animates a series of Sprites
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/15/2022
 */
import JettersR.Graphics.Sprite;
import JettersR.Util.Annotations.fixed;

public abstract class Animation
{
    //Animations Path (so that way if I have to move the animaions folder, I just need to change this).
    public static final String animationsPath = "assets/Animations/";

    public abstract Sprite update(@fixed int f_timeMod);

    public abstract Sprite getSprite();
    public abstract Sprite[] getSprites();
}
