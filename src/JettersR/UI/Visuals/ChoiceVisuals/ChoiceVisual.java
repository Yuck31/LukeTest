package JettersR.UI.Visuals.ChoiceVisuals;
/**
 * 
 */
import JettersR.Graphics.Screen;

public interface ChoiceVisual
{
	public void noHighlight();
    public void highlight(boolean mouseIntersectsThis);

	public void render(Screen screen, float xOffset, float yOffset);
}
