package JettersR.UI.Visuals.Labels.LabelLists;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Font;

public class Short_LabelList implements UILabelList
{
	//Shorts to render.
	private short[] shorts;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public Short_LabelList(short[] shorts, Font font)
	{
		this.shorts = shorts;
		this.font = font;
	}

	public void set(short[] shorts){this.shorts = shorts;}

	public int getLength(){return shorts.length;}

	@Override
	public void render(Screen screen, int slot, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, Short.toString(shorts[slot]), fontColor, false);
	}
}
