package JettersR.UI.Menus;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/26/2023
 */
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;

import JettersR.Mouse;
import JettersR.UI.UIComponent;
import JettersR.Util.Shapes.Shapes2D.Shape2D;
import JettersR.Game;
import JettersR.Controller;

public abstract class MenuComponent extends UIComponent
{
    //Shape2D.
    protected Shape2D shape;

    /**Constructor.*/
    public MenuComponent(int x, int y, Shape2D shape)
    {
        super(x, y);
        this.shape = shape;
    }

    /**Constructor.*/
    public MenuComponent(Vector3f position, Shape2D shape)
    {
        super(position);
        this.shape = shape;
    }

    public Shape2D getShape(){return shape;}


    /**Checks if the given Mouse intersects this MenuChoice.*/
    public final boolean intersects(float xOffset, float yOffset, Mouse mouse)//, boolean needsToMove)
    {
        float//Offset position by parent position.
        xa = this.position.x + xOffset,
        ya = this.position.y + yOffset;

        //Return collision check.
        return
        (
            //(mouse.isMoving() || !needsToMove) &&
            shape.intersects(mouse.getX(), mouse.getY(), xa, ya)
        );
    }
    //public final boolean intersects(int xOffset, int yOffset, Mouse mouse){return intersects(xOffset, yOffset, mouse, false);}
    //public final boolean intersects(Mouse mouse, boolean needsToMove){return intersects(0, 0, mouse, needsToMove);}
    public final boolean intersects(Mouse mouse){return intersects(0, 0, mouse);}


    /**Checks if either a confirm button or left-click is pressed.*/
    public final boolean input_Confirm_Held(boolean intersects)
    {
        return Game.controller.menu_InputHeld_AnyPlayer(Controller.menu_CONFIRM)
        | (intersects && Game.mouse.buttonHeld(GLFW_MOUSE_BUTTON_LEFT));
    }

    /**Checks if either a confirm button or left-click is pressed.*/
    public final boolean input_Confirm_Pressed(boolean intersects)
    {
        return Game.controller.menu_InputPressed_AnyPlayer(Controller.menu_CONFIRM, false)
        | (intersects && Game.mouse.getButtonState(GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_RELEASED);
    }

    public final boolean input_Confirm_Pressed(int playerNum, boolean intersects)
    {
        return Game.controller.menu_InputPressed(playerNum, Controller.menu_CONFIRM, false)
        | (intersects && Game.mouse.getButtonState(GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_RELEASED);
    }

    /**
     * Checks if the given action was input from the given playerNum.
     * 
     * @param playerNum Player number.
     * @param action Action ID (Controller.MENU_CONFIRM for example).
     * @return true if the input was pressed.
     */
    public boolean input_Held(int playerNum, int action)
    {
        return Game.controller.menu_InputHeld
        (playerNum, action);
    }

    /**
     * Checks if the given action was input from any player.
     * 
     * @param action Action ID (Controller.MENU_CONFIRM for example).
     * @return true if the input was pressed.
     */
    public boolean input_Pressed(int action)
    {
        return Game.controller.menu_InputPressed_AnyPlayer
        (action, true);
    }

    /**
     * Checks if the given action was input from the given playerNum.
     * 
     * @param playerNum Player number.
     * @param action Action ID (Controller.MENU_CONFIRM for example).
     * @return true if the input was pressed.
     */
    public boolean input_Pressed(int playerNum, int action)
    {
        return Game.controller.menu_InputPressed
        (playerNum, action, true);
    }


    /**
     * Returns true if it was selected and confirmed.
     * 
     * @param xOffset X offset of any parent component.
     * @param yOffset Y offset of any parent component.
     * @param isCurrentChoice Should this component be highlighted?
     * @param mouseIntersectsThis Is the mouse cursor intersecting this component? 
     * @return true if this MenuComponent was confirmed on.
     */
    public abstract boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis);
}
