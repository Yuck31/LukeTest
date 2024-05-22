package JettersR.UI.Visuals.Labels.LabelGrids;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Font;
import JettersR.Graphics.Screen;

public class Byte_LabelGrid implements UILabelGrid
{
	//Shorts to render.
	private byte[][] bytes;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public Byte_LabelGrid(byte[][] bytes, Font font)
	{
		this.bytes = bytes;
		
		this.font = font;
		this.fontColor = Screen.DEFAULT_BLEND;
	}

	public void set(byte[][] bytes){this.bytes = bytes;}

	public int getLength(){return bytes.length;}
	public int getLength(int slotX){return bytes[slotX].length;}

	@Override
	public void render(Screen screen, int slotX, int slotY, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, Byte.toString(bytes[slotX][slotY]), fontColor, false);
	}
}
