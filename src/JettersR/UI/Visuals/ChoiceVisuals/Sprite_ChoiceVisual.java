package JettersR.UI.Visuals.ChoiceVisuals;
/**
 * 
 */
import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;

public class Sprite_ChoiceVisual implements ChoiceVisual
{
	private Sprite[] sprites;
	private int spriteNum = 0;

	/**Constructor.*/
	public Sprite_ChoiceVisual(Sprite[] sprites)
	{
		//Set sprite array.
		this.sprites = sprites;
	}


	@Override
	public void noHighlight()
	{
		//Set spriteNum to first sprite.
        spriteNum = 0;
	}

	@Override
	public void highlight(boolean mouseIntersectsThis)
	{
		//Set spriteNum to second sprite.
		spriteNum = 1;
	}

	@Override
	public void render(Screen screen, float xOffset, float yOffset)
	{
		//Render the current sprite.
		screen.renderSprite((int)xOffset, (int)yOffset, sprites[spriteNum], Sprite.FLIP_NONE, Screen.DEFAULT_BLEND, false);
	}
}
