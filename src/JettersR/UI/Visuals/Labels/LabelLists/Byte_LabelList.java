package JettersR.UI.Visuals.Labels.LabelLists;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Font;

public class Byte_LabelList implements UILabelList
{
	//Bytes to render.
	private byte[] bytes;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public Byte_LabelList(byte[] bytes, Font font)
	{
		this.bytes = bytes;
		this.font = font;
	}

	public int getLength(){return bytes.length;}
	
	public void set(byte[] bytes){this.bytes = bytes;}

	@Override
	public void render(Screen screen, int slot, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, Byte.toString(bytes[slot]), fontColor, false);
	}
}
