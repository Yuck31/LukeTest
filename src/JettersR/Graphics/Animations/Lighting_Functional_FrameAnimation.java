package JettersR.Graphics.Animations;
/**
 * 
 */
import JettersR.Graphics.Sprite;
import JettersR.Graphics.SpriteRenderers.SpriteRenderer;
import JettersR.Util.Annotations.fixed;

//import static JettersR.Util.Fixed.*;

public class Lighting_Functional_FrameAnimation
{
    Functional_FrameAnimation anim = null;
    protected Sprite[] normalSprites = null;

    /**Constructor.*/
    public Lighting_Functional_FrameAnimation(Functional_FrameAnimation anim, int xOffset, int yOffset)
    {
        //Set anim.
        this.anim = anim;

        //Use SpriteSheet to get NormalMap sprites.
        normalSprites = anim.sprites[0].getSheet().getNormalMap_Sprites(anim.sprites, xOffset, yOffset);
    }
    
    /**Update Animation and return a normal map sprite.*/
    public Sprite update(@fixed int f_timeMod, SpriteRenderer spriteRenderer)
    {
        //Update the animation.
        int frame = anim.update(f_timeMod, spriteRenderer);

        //Return Sprite.
        return normalSprites[frame];
    }

    //Sprite Getters.
    //public final Sprite getSprite(){return sprites[timer.getFrame()];}
    public final Sprite getSprite(int index){return anim.getSprite(index);}

    //NormalMap getters.
    //public final Sprite getNormalMap(){return normalSprites[timer.getFrame()];}
    public final Sprite getNormalMap(int index){return normalSprites[index];}
    
    public void resetAnim(){anim.resetAnim();}
}
