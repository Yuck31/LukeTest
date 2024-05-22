package JettersR.UI.Menus;
/**
 * Author: Luke Sullivan
 * Last Edit: 8/26/2023
 */
import JettersR.Game;
import JettersR.Graphics.Screen;
//import JettersR.Controller;
//import JettersR.Mouse;
import JettersR.Util.Shapes.Shapes2D.Shape2D;

public class BasicMenu extends Menu
{    
    /**Constructor.*/
    public BasicMenu(int x, int y, Shape2D shape, MenuComponent... components)
    {super(x, y, shape, components);}

    /**Default Constructor.*/
    public BasicMenu(Shape2D shape, MenuComponent... components){this(0, 0, shape, components);}

    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        if(isCurrentChoice)
        {
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
                //System.out.println(currentChoice);

                //Update Menu Choice.
                if(m.update(xOffset, yOffset, currentChoice == i, mouseIntersects)){return true;}
            }
        }
        //
        return false;
    }

    @Override
    /**Renders all of the MenuComponents associated with this Menu.*/
    public void render(Screen screen, float xOffset, float yOffset)
    {
        for(int i = 0; i < menuComponents.size(); i++)
        {
            menuComponents.get(i).render
            (
                screen, (int)position.x + xOffset, (int)position.y + yOffset
            );
        }
    }
}
