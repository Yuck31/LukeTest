package JettersR.UI.Menus.MenuChoices;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Font;
import JettersR.Graphics.Fonts;
import JettersR.Graphics.Sprite;
import JettersR.UI.Visuals.ChoiceVisuals.Basic_ChoiceVisual;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

public class Labeled_MenuChoice extends MenuChoice_1P
{
	private Sprite[] textSprites;
    private int halfTextWidth, halfTextHeight;
    protected float textScale = 1f;
    private Font font = Fonts.get("Arial");

	/**Constructor.*/
	public Labeled_MenuChoice(int x, int y, int width, int height, Action action, Vector4f[] colors, String text, float textScale)
	{
		super
        (
            x, y,
            new AAB_Box2D
            (
                (width < 19) ? 19 : width,
                (height < 19) ? 19 : height
            ),
            (byte)0,
            action, new Basic_ChoiceVisual(width, height, colors)
        );

		this.textScale = textScale;
        setText(text);
	}
	

	public Sprite[] getTextSprites(){return textSprites;}

    public void setText(String text)
    {
        this.textSprites = new Sprite[text.length()];
        this.halfTextWidth = (int)(font.textToSprites(text, textSprites) * textScale)/2;
        this.halfTextHeight = (int)((font.getLineSpace()-1) * textScale)/2;
    }

	
	@Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        int xa = (int)(this.position.x + xOffset), ya = (int)(this.position.y + yOffset);

        //Render box.
        visual.render(screen, xa, ya);

        //Render Text.
        font.render
        (
            screen,
            xa - halfTextWidth, ya - halfTextHeight,
            textSprites, textScale, false
        );

        //shape.render(screen, 1.0f, xa, ya, false);
    }
}
