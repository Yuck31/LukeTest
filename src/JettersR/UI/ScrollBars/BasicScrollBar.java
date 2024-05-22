package JettersR.UI.ScrollBars;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.UI.Visuals.Rect_DialogueBox;

public class BasicScrollBar extends ScrollBar
{
	//Bar and handle info.
	private Rect_DialogueBox backPart, handle;
	private Vector4f handleColor0, handleColor1;

	/**Constructor.*/
	public BasicScrollBar(int x, int y, int barWidth, int barHeight, int scrollRegion, int scrollHoldRegion,
	boolean isVertical, int lengthInc, int visableLength, int totalLength, Vector4f handleColor0, Vector4f handleColor1, Vector4f barColor)
	{
		//Create collision for the area the scroll bar affects.
		super(x, y, barWidth, barHeight, scrollRegion, scrollHoldRegion, isVertical, lengthInc, visableLength, totalLength);

		//Set handle colors.
		this.handleColor0 = handleColor0;
		this.handleColor1 = handleColor1;

		//Create decor box for full scroll bar.
		this.backPart = new Rect_DialogueBox(0, 0, barWidth, barHeight, barColor);

		//Scroll handle is based on (visableElements / totalElements)
		int bh = (isVertical) ? barHeight : barWidth;
		float handleHeight = (totalLength <= 0) ? 0 : (bh / (float)totalLength) * visableLength;
		this.halfHandleHeight = (int)(handleHeight * 0.5f);
		//
		this.handle = (isVertical)
		? new Rect_DialogueBox(0, 0, barWidth, (int)handleHeight, handleColor0)
		: new Rect_DialogueBox(0, 0, (int)handleHeight, barHeight, handleColor0);
		//Range is from halfHandleHeight to ((height - handleHeight) - halfHandleHeight)
	}

	@Override
	public void noHighLight()
	{
		handle.setColor(handleColor0);
	}

	
	public void highLight()
	{
		handle.setColor(handleColor1);
	}

	public void scroll()
	{
		//TODO Play sound effect.
	}

	@Override
	public void setTotalLength(int totalLength)
	{
		this.totalLength = totalLength;

		//float handleHeight = (totalLength <= 0) ? 0 : (shape.getHeight() / (float)totalLength) * visableLength;
		//this.halfHandleHeight = (int)(handleHeight * 0.5f);
		//this.handle.setHeight( (int)handleHeight );

		int bh = (isVertical) ? shape.getHeight() : shape.getWidth();
		float handleHeight = (totalLength <= 0) ? 0 : (bh / (float)totalLength) * visableLength;
		this.halfHandleHeight = (int)(handleHeight * 0.5f);
		//
		if(isVertical){handle.setHeight( (int)handleHeight );}
		else{handle.setWidth( (int)handleHeight );}
	}


	@Override
	/**Render function.*/
	public void render(Screen screen, float xOffset, float yOffset)
	{
		//Reset crop region so the bar can be rendered correctly.
		screen.resetCropRegion();

		int xa = (int)(position.x + xOffset),
		ya = (int)(position.y + yOffset);

		//Background.
		backPart.render(screen, xa, ya);

		shape.render(screen, 1.0f, xa, ya, false);

		//Handle.
		float offsetPercent = (lengthOffset / (float)totalLength);
		if(isVertical)
		{
			handle.render
			(
				screen, xa,
				ya
				+ shape.up()//Place center to top of bar.
				+ halfHandleHeight//Offset so top of handle is on top of bar.
				+ (offsetPercent * shape.getHeight())//Offset by current scroll.
			);
		}
		else
		{
			handle.render
			(
				screen,
				xa
				+ shape.left()//Place center to left of bar.
				+ halfHandleHeight//Offset so left of handle is on left of bar.
				+ (offsetPercent * shape.getWidth()),//Offset by current scroll.
				ya
			);
		}
	}
}
