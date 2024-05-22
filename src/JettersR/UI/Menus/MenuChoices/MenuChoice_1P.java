package JettersR.UI.Menus.MenuChoices;
import JettersR.UI.Visuals.ChoiceVisuals.ChoiceVisual;
/**
 * 
 */
import JettersR.Util.Shapes.Shapes2D.Shape2D;

public class MenuChoice_1P extends MenuChoice
{
	//The player number that can confirm this choice.
	private byte playerNum = 0;

	/**Constructor.*/
	public MenuChoice_1P(int x, int y, Shape2D shape, byte playerNum, Action action, ChoiceVisual visual)
	{
		//Set coordinates and shape.
		super(x, y, shape, action, visual);

		//Set playerNum
		this.playerNum = playerNum;
	}

	//public MenuChoice_1P(int x, int y, Shape2D shape, Action action, ChoiceVisual visual){super(x, y, shape, action, visual);}


	@Override
    protected final boolean confirmCheck(boolean mouseIntersectsThis)
    {
        //Check Input from the requested player.
        if(input_Confirm_Pressed(playerNum, mouseIntersectsThis))
        {
            //UnHighlight this choice.
            visual.noHighlight();

            //Perform Action.
            action.perform();

            //Action performed. Don't update the rest of the menu.
            return true;
        }
        return false;
    }
}
