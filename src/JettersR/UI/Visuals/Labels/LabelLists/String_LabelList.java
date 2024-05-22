package JettersR.UI.Visuals.Labels.LabelLists;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Font;

public class String_LabelList implements UILabelList
{
	//Strings to render.
	private String[] strings;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public String_LabelList(String[] strings, Font font)
	{
		this.strings = strings;
		this.font = font;
	}

	public int getLength(){return strings.length;}

	@Override
	public void render(Screen screen, int slot, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, strings[slot], fontColor, false);
	}
}
