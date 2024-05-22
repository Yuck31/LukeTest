package JettersR.UI.Visuals.Labels.LabelGrids;
/**
 * 
 */
import JettersR.Graphics.Screen;

public interface UILabelGrid
{
	public int getLength();
	public int getLength(int slotX);
	
	public void render(Screen screen, int slotX, int slotY, int xOffset, int yOffset);
}
