package JettersR.UI.ScrollBars;
/**
 * Author: Luke Sullivan
 * Last Edit: 7/20/2023
 */
import org.lwjgl.glfw.GLFW;

import JettersR.Controller;
import JettersR.Game;
import JettersR.Mouse;
import JettersR.UI.Menus.MenuComponent;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

public abstract class ScrollBar extends MenuComponent
{
	//Collision info.
	protected int barWidth;
	protected int halfHandleHeight = 0;
	protected int scrollHoldRegion;
	protected boolean isVertical;

	//Is the bar being held?
	protected boolean holdingBar = false;

	//Length values.
	protected int lengthOffset;
	//
	private int lengthInc;
	protected int visableLength;
	protected int totalLength;

	/**Constructor.*/
	public ScrollBar(int x, int y, int barWidth, int barHeight, int scrollRegion, int scrollHoldRegion,
	boolean isVertical, int lengthInc, int visableLength, int totalLength)
	{
		//Create collision for the area the scroll bar affects.
		super
		(
			x, y, (isVertical)
			? new AAB_Box2D(scrollRegion + barWidth, barHeight, -(barWidth/2) - scrollRegion, -(barHeight/2))
			: new AAB_Box2D(barWidth, scrollRegion + barHeight, -(barWidth/2), -(barHeight/2) - scrollRegion)
		);
		this.barWidth = (isVertical) ? barWidth : barHeight;
		this.scrollHoldRegion = scrollHoldRegion;

		//Set length values.
		this.isVertical = isVertical;
		this.lengthInc = lengthInc;
		this.visableLength = visableLength;
		this.totalLength = totalLength;
		//Range is from halfHandleHeight to ((height - handleHeight) - halfHandleHeight)
	}



	@Override
	public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
	{
		//Store current offset before change.
		int oldOffset = lengthOffset;

		//Does the mouse intersect with the full region?
		if(mouseIntersectsThis)
		{
			//
			//Hold check.
			//

			//Get mouse and its x position.
			Mouse mouse = Game.mouse;
			double mouseX;
			float rightSide;
			double mouseY;
			float upSide;
			int shapeHeight;
			
			if(isVertical)
			{
				mouseX = mouse.getX();
				rightSide = position.x + xOffset + shape.right();
				mouseY = mouse.getY();
				upSide = position.y + yOffset + shape.up();
				shapeHeight = shape.getHeight();
			}
			else
			{
				mouseX = mouse.getY();
				rightSide = position.y + yOffset + shape.down();
				mouseY = mouse.getX();
				upSide = position.x + xOffset + shape.left();
				shapeHeight = shape.getWidth();
			}

			//Is it inside the scroll bar or already being held within the scroll hold region?
			if
			(
				(
					(mouseX > rightSide - barWidth) ||
					(holdingBar && mouseX > (rightSide - barWidth) - scrollHoldRegion)
				)
				&& (mouseX < rightSide)
			)
			{
				//Set handle color.
				highLight();

				//Is left click being pressed right now? (Held check would pass even if the mouse was outside the scroll bar beforehand, which is why we do a press check)
				if(mouse.getButtonState(GLFW.GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_PRESSED)
				{
					//Set this value for when the mouse goes outside of the scroll bar.
					holdingBar = true;
				}
				//Otherwise, if left click is not being held, we are not holding the scroll bar.
				else if(!mouse.buttonHeld(GLFW.GLFW_MOUSE_BUTTON_LEFT))
				{holdingBar = false;}
			}
			//Otherwise, we are not holding the scroll bar.
			else
			{
				holdingBar = false;

				//Set handle color.
				noHighLight();
			}



			//
			//Drag/Scroll check.
			//

			//Is the bar being held and is left click is being held?
			if(holdingBar && mouse.buttonHeld(GLFW.GLFW_MOUSE_BUTTON_LEFT))
			{
				//Set offset.
				lengthOffset = StrictMath.max
				(
					0,//Top
					StrictMath.min
					(
						(int)(((mouseY - halfHandleHeight) - upSide ) / (shapeHeight / (float)totalLength)),//Mouse position to elements.
						totalLength - visableLength//Bottom.
					)
				);
				//System.out.println(offset);

				//Was confirmed.
				return true;
			}
			//Otherwise, check scroll wheel.
			else
			{
				byte mouseScroll = mouse.getScroll();

				//System.out.println(elementOffset + " " + visableElements + " " + totalElements);
				
				//Scrolling down.
				if(mouseScroll > 0)
				{
					lengthOffset = (lengthOffset + lengthInc > totalLength - visableLength) ?
					totalLength - visableLength : lengthOffset + lengthInc;
				}
				//Scrolling up.
				else if(mouseScroll < 0)
				{
					lengthOffset = (lengthOffset - lengthInc < 0) ?
					0 : lengthOffset - lengthInc;
				}
			}
		}
		//If not, the bar is definitly not being held.
		else
		{
			holdingBar = false;

			//Set handle color.
			noHighLight();
		}

		//Call scroll function.
		if(lengthOffset != oldOffset){scroll();}
        
		//Not being used.
		return false;
	}

	public abstract void noHighLight();
	public abstract void highLight();
	public abstract void scroll();

	//BarWidth getter.
	public int getBarWidth(){return this.barWidth;}

	//Element offset Getter/Setter.
	public int getLengthOffset(){return lengthOffset;}
	public void setLengthOffset(int lengthOffset){this.lengthOffset = lengthOffset;}

	//Visable Elements Getter.
	public int getVisableLength(){return visableLength;}

	//TotalLength Getter/Setter.
	public int getTotalLength(){return totalLength;}
	public abstract void setTotalLength(int totalLength);
}
