package JettersR.UI.Visuals.Labels.LabelLists;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Font;
import JettersR.Graphics.Screen;

public class ByteAsString_LabelList implements UILabelList
{
	//Bytes to pick Strings to render.
	private byte[] bytes;
	private String[] strings;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public ByteAsString_LabelList(byte[] bytes, String[] strings, Font font)
	{
		this.bytes = bytes;
		this.strings = strings;
		
		this.font = font;
		this.fontColor = Screen.DEFAULT_BLEND;
	}

	public int getLength(){return bytes.length;}
	
	public void setBytes(byte[] bytes){this.bytes = bytes;}
	public void setStrings(String[] strings){this.strings = strings;}

	@Override
	public void render(Screen screen, int slot, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, strings[bytes[slot]], fontColor, false);
	}
}
