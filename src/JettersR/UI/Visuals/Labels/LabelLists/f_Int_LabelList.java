package JettersR.UI.Visuals.Labels.LabelLists;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Font;
import JettersR.Graphics.Screen;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class f_Int_LabelList implements UILabelList
{
	//Bytes to render.
	private @fixed int[] f_ints;

	//Font to render with.
	private Font font;
	private Vector4f fontColor;

	/**Constructor.*/
	public f_Int_LabelList(int[] f_ints, Font font)
	{
		this.f_ints = f_ints;
		this.font = font;
		this.fontColor = Screen.DEFAULT_BLEND;
	}

	public int getLength(){return f_ints.length;}

	public void set(@fixed int[] f_ints){this.f_ints = f_ints;}

	@Override
	public void render(Screen screen, int slot, int xOffset, int yOffset)
	{
		font.render(screen, xOffset, yOffset, f_toString(f_ints[slot]), fontColor, false);
	}
}
