package JettersR.UI.Visuals.Labels.LabelLists;
/**
 * 
 */
import JettersR.Graphics.Screen;
public interface UILabelList
{
	public int getLength();
	
	public void render(Screen screen, int slot, int xOffset, int yOffset);
}
