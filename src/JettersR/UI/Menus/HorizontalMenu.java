package JettersR.UI.Menus;
/**
 * Author: Luke Sullivan
 * Last Edit: 8/29/2023
 */
import JettersR.Game;
import JettersR.Graphics.Screen;
import JettersR.Controller;
//import JettersR.Mouse;
import JettersR.Util.Shapes.Shapes2D.Shape2D;

public class HorizontalMenu extends Menu
{
	/**Constructor.*/
    public HorizontalMenu(int x, int y, Shape2D shape, MenuComponent... components)
    {super(x, y, shape, components);}

    /**Default Constructor.*/
    public HorizontalMenu(Shape2D shape, MenuComponent... components){this(0, 0, shape, components);}

    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        if(isCurrentChoice)
        {
			Controller controller = Game.controller;

			//Left and Right input checks.
			boolean left = controller.menu_InputPressed(0, Controller.menu_LEFT, true),
			right = controller.menu_InputPressed(0, Controller.menu_RIGHT, true);

			//Left
			if(left && !right){currentChoice = ((currentChoice-1)+menuComponents.size()) % menuComponents.size();}
			//Right
			else if(right && !left){currentChoice = (currentChoice+1) % menuComponents.size();}

			//Update each choice.
            for(int i = 0; i < menuComponents.size(); i++)
            {
                //Cache the component.
                MenuComponent m = menuComponents.get(i);
                boolean mouseIntersects = false;

                //Collision check with choice.
                if(m.intersects(position.x + xOffset, position.y + yOffset, Game.mouse))
                {
                    mouseIntersects = true;
                    if(Game.mouse.isMoving()){currentChoice = i;}
                }

                //Update Menu Choice.
                if(m.update(xOffset, yOffset, currentChoice == i, mouseIntersects)){return true;}
            }
        }
        //
        return false;
    }

    @Override
    /**Renders all of the MenuComponents associated with this Menu.*/
    public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
    {
        for(int i = 0; i < menuComponents.size(); i++)
        {
            menuComponents.get(i).render
            (
                screen, (int)position.x + xOffset, (int)position.y + yOffset
                //cropX0, cropY0, cropX1, cropY1
            );
        }
    }
}
