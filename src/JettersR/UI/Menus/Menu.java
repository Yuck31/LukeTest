package JettersR.UI.Menus;
/**
 * The base class for menus in this game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 9/9/2023
 */
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

import JettersR.Util.Shapes.Shapes2D.Shape2D;
import JettersR.Game;
import JettersR.Mouse;
import JettersR.Graphics.Screen;
import JettersR.Controller;

public abstract class Menu extends MenuComponent
{
    //List of Menu Components.
    protected final List<MenuComponent> menuComponents = new ArrayList<MenuComponent>();

    //CurrentChoice value.
    protected int currentChoice = 0;

    /**Constructor.*/
    public Menu(int x, int y, Shape2D shape, MenuComponent... components)
    {
        //Set position and Shape.
        super(x, y, shape);

        //Add components.
        for(int i = 0; i < components.length; i++)
        {menuComponents.add(components[i]);}
    }

    /**Constructor.*/
    public Menu(Vector3f position, Shape2D shape, MenuComponent... components)
    {
        //Set position and Shape.
        super(position, shape);

        //Add components.
        for(int i = 0; i < components.length; i++)
        {menuComponents.add(components[i]);}
    }

    /**Constructor.*/
    public Menu(int x, int y, Shape2D shape)
    {
        //Set position and Shape.
        super(x, y, shape);
    }


    /**Adds a Component to this menu.*/
    public Menu addComponent(MenuComponent mc)
    {
        menuComponents.add(mc);
        return this;
    }

    public int numComponents(){return menuComponents.size();}
    public void clearComponents(){menuComponents.clear();}
    
    /**Checks if the mouse intersects with any of this menu's components.*/
    public boolean intersects_components(Mouse mouse)//, boolean needsToMove)
    {
        for(int i = 0; i < menuComponents.size(); i++)
        {
            if(menuComponents.get(i).intersects(mouse))//, needsToMove))
            {return true;}
        }
        return false;
    }
    //public boolean intersects_components(Mouse mouse){return intersects_components(mouse, false);}

    /**Checks if either a cancel button or right-click is pressed.*/
    public static boolean input_Cancel()
    {
        return Game.controller.menu_InputPressed_AnyPlayer(Controller.menu_CANCEL, true)
        || Game.mouse.buttonPressed(GLFW_MOUSE_BUTTON_RIGHT);
    }

    /**Checks if either a cancel button or right-click is held.*/
    public static boolean input_Cancel_Held()
    {
        return Game.controller.menu_InputHeld_AnyPlayer(Controller.menu_CANCEL)
        || Game.mouse.buttonHeld(GLFW_MOUSE_BUTTON_RIGHT);
    }

    /**Menu exclusive render function.*/
    public final void render(Screen screen){render(screen, 0, 0);}
}
