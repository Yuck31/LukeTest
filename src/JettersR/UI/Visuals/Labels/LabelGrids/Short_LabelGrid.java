package JettersR.UI.Visuals.Labels.LabelGrids;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Font;
import JettersR.Graphics.Screen;

public class Short_LabelGrid implements UILabelGrid
{
	//Shorts to render.
	private short[][] shorts;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public Short_LabelGrid(short[][] shorts, Font font)
	{
		this.shorts = shorts;

		this.font = font;
		this.fontColor = Screen.DEFAULT_BLEND;
	}

	public void set(short[][] shorts){this.shorts = shorts;}
	
	public int getLength(){return shorts.length;}
	public int getLength(int slotX){return shorts[slotX].length;}

	@Override
	public void render(Screen screen, int slotX, int slotY, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, Short.toString(shorts[slotX][slotY]), fontColor, false);
	}
}
