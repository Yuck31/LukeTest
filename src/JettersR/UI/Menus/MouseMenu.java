package JettersR.UI.Menus;
/**
 * This is a Menu that only recieves input from the mouse.
 * 
 * Author: Luke Sullivan
 * Last Edit: 7/12/2023
 */
import JettersR.Game;
import JettersR.Mouse;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes2D.Shape2D;

public class MouseMenu extends Menu
{
    /**Constructor.*/
    public MouseMenu(int x, int y, Shape2D shape)
    {
        super(x, y, shape);
    }

    /**Default Constructor.*/
    public MouseMenu(Shape2D shape){this(0, 0, shape);}

    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        //Get mouse.
        Mouse mouse = Game.mouse;

        //Go through each menu component.
        for(int i = 0; i < menuComponents.size(); i++)
        {
            //Cache it.
            MenuComponent m = menuComponents.get(i);
            boolean mouseIntersects = false;

            //Collision check with choice.
            if(m.intersects
            (
                position.x + xOffset,
                position.y + yOffset,
                mouse
            ))
            {mouseIntersects = true;}

            //Update Menu Choice.
            if(m.update(xOffset, yOffset, mouseIntersects, mouseIntersects)){return true;}
        }
        
        //Nothing was confirmed.
        return false;
    }

    @Override
    public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
    {
        for(int i = 0; i < menuComponents.size(); i++)
        {
            menuComponents.get(i).render
            (
                screen, (int)position.x + xOffset, (int)position.y + yOffset//,
                //cropX0, cropY0, cropX1, cropY1
            );
        }
    }
}
