package JettersR.UI.Visuals.Labels.LabelGrids;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Font;
import JettersR.Graphics.Screen;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class f_Short_LabelGrid implements UILabelGrid
{
	//Shorts to render.
	private @fixed short[][] f_shorts;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public f_Short_LabelGrid(@fixed short[][] f_shorts, Font font)
	{
		this.f_shorts = f_shorts;

		this.font = font;
		this.fontColor = Screen.DEFAULT_BLEND;
	}

	public void set(@fixed short[][] f_shorts){this.f_shorts = f_shorts;}
	
	public int getLength(){return f_shorts.length;}
	public int getLength(int slotX){return f_shorts[slotX].length;}

	@Override
	public void render(Screen screen, int slotX, int slotY, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, f_toString(f_shorts[slotX][slotY]), fontColor, false);
	}
}
