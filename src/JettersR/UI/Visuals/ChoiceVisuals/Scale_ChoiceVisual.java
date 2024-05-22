package JettersR.UI.Visuals.ChoiceVisuals;
/**
 * 
 */
import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;

public class Scale_ChoiceVisual implements ChoiceVisual
{
	//These manage scaling.
    private float minScale = 1.0f, maxScale = 1.05f,
    scale = minScale, scaleInc = 0.01f;

    //This should be a size of 2.
    private Sprite[] sprites = null;
	private byte spriteNum  = 0;


	/**Default Constructor.*/
	public Scale_ChoiceVisual(Sprite[] sprites)
	{
		//Set sprite array.
		this.sprites = sprites;
	}


	@Override
	public void noHighlight()
	{
		//Set spriteNum to first sprite.
        spriteNum = 0;

		//If scale is bigger than minimum scale.
        if(scale > minScale)
        {
			//Start decreasing it.
            scale -= scaleInc;
            if(scale < minScale){scale = minScale;}
        }
	}

	@Override
	public void highlight(boolean mouseIntersectsThis)
	{
		//Set spriteNum to second sprite.
		spriteNum = 1;

		//Is scale is smaller than maximum scale.
        if(scale < maxScale)
        {
			//Start increasing it.
            scale += scaleInc;
            if(scale > maxScale){scale = maxScale;}
        }
	}


	@Override
	public void render(Screen screen, float xOffset, float yOffset)
	{
		Sprite sprite = sprites[spriteNum];

		screen.renderSprite_Sc
        (
            //(int)xOffset,
            (int)(xOffset - (((sprite.getWidth() * scale) - sprite.getWidth()) / 2)),
            //(int)yOffset,
            (int)(yOffset - (((sprite.getHeight() * scale) - sprite.getHeight()) / 2)),
            sprite, Sprite.FLIP_NONE, scale, scale, true
        );
	}
}
