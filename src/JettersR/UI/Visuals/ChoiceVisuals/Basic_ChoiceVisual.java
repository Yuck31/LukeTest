package JettersR.UI.Visuals.ChoiceVisuals;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.UI.Visuals.Rect_DialogueBox;

public class Basic_ChoiceVisual implements ChoiceVisual
{
	Rect_DialogueBox dialogueBox = null;

    protected Vector4f[] colors = null;
    protected byte colorNum = 0;

	
	/**Constructor.*/
	public Basic_ChoiceVisual(int width, int height, Vector4f[] colors)
	{
		this.colors = colors;
        this.dialogueBox = new Rect_DialogueBox(0, 0, width, height, colors[0]);
	}


	@Override
	public void noHighlight()
	{
		if(colorNum != 0)
        {
            dialogueBox.setColor(colors[0]);
            colorNum = 0;
        }
	}

	@Override
	public void highlight(boolean mouseIntersectsThis)
	{
		if(colorNum != 1)
        {
            dialogueBox.setColor(colors[1]);
            colorNum = 1;
        }
	}
	

	@Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        //Render box.
        dialogueBox.render(screen, xOffset, yOffset);
    }
}
