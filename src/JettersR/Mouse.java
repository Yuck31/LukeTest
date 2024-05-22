package JettersR;
/**
 * This is the class that manages Mouse input.
 * 
 * Author: Luke Sullivan
 * Last Edit: 12/26/2023
 */
//import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFW;

import JettersR.Graphics.Screen;

public final class Mouse
{
    //Bitmask of Pressed Buttons.
    private int buttonStates;//GLFW_MOUSE_BUTTON_LAST is 7 buttons. We have more than 14 bits.

    //Screen for the Mouse to calculate to.
    private Screen screen;

    //If the mouse was moving
    private boolean isMoving = false;

    //Cursor Coordinates
    private double x = 0, y = 0;

    //Scroll direction
    private byte scroll = 0;


    /**Mouse Constructor.*/
    public Mouse(){}

    public void update()
    {
        isMoving = false;
        //System.out.println(Integer.toBinaryString(buttonStates));
    }

    //public static final int ZERO_OUT_CHANGE_BITS = GLFW.GLFW_MOUSE_BUTTON_LAST
    public void updateState(){buttonStates &= 0b10101010101010;}
    
    public void setScreen(Screen screen){this.screen = screen;}
    

    /*
     * Mouse Buttons
     */

    /**This is the function GLFW will refer to whenever a Mouse Button is pressed or released.*/
    public void mouseButtonCallback(long window, int button, int action, int mods)
    {
        //Location in bitmask is (buttonID * 2) bits.
        button <<= 1;

        //This is to put the action in the left bit.
        action <<= 1;

        buttonStates =
        (buttonStates & ~(Controller.STATE_PRESSED << button))//Remove given button's state from the bitmask.
        | ((action | Controller.STATE_RELEASED) << button);//Add the new state in.
    }



    /**
     * Checks if the given button is Held Down.
     * 
     * @param button should a GLFW_MOUSE_BUTTON Constant.
     * @return true if the button was held down during this check.
     */
    public boolean buttonHeld(int button)
    {
        //Location in bitmask is (buttonID * 2) bits.
        button <<= 1;
        
        return (((buttonStates & (Controller.STATE_PRESSED << button)) >> button)//Isolate button's state and bit-shift it down.
        & Controller.STATE_HELD) == Controller.STATE_HELD;//Is the "change" bit 1?
    }

    /**
     * Same as buttonHeld(), but disables the button upon retrival, allowing for Input-Buffering.
     * 
     * @param button should a GLFW_MOUSE_BUTTON Constant.
     * @return true if the button was pressed during this check.
     */
    public boolean buttonPressed(int button)
    {
        //Location in bitmask is (buttonID * 2) bits.
        button <<= 1;

        boolean result =
        ((buttonStates & (Controller.STATE_PRESSED << button)) >> button)//Isolate button's state and bit-shift it down.
        == Controller.STATE_PRESSED;//Are both bits 1?

        buttonStates &= ~(Controller.STATE_RELEASED << button);//Remove press/release from button.
        //& with inverse of button bits.

        //Return result.
        return result;
    }

    public void pressButton(int button)
    {
        //Turn on press/release to given button.
        buttonStates |= (Controller.STATE_RELEASED << (button << 1));
    }

    public void removePress(int button)
    {
        //Remove press/release from given button.
        buttonStates &= ~(Controller.STATE_RELEASED << (button << 1));

        //buttonStates =
        //(
            //~(buttonStates & (0b10 << button))//Remove button's state from the bitmask. ("and" with location's state then flip the bits)
            //& (state << button)//Put the new state in its place.
        //);
    }

    /**
     * Checks if the given button was just let go.
     * 
     * @param button
     * @return
     */
    public boolean buttonReleased(int button)
    {
        //Location in bitmask is (buttonID * 2) bits.
        button <<= 1;

        boolean result =
        ((buttonStates & (Controller.STATE_PRESSED << button)) >> button)//Isolate button's state and bit-shift it down.
        == Controller.STATE_RELEASED;//Is the left bit 1 and the right one 0?

        buttonStates &= ~(Controller.STATE_RELEASED << button);//Remove press/release from button.
        return result;//Return result.
    }

    /**Gets the ButtonState of the given button.*/
    public byte getButtonState(int button)
    {
        button <<= 1;//Location in bitmask is (buttonID * 2) bits.
        return (byte)
        (
            (buttonStates & (Controller.STATE_PRESSED << button))//Isolate the portion of the bitmask this button's state is in.
            >> button//Shift to a value from 0 (not held) to 3 (just pressed).
        );


        //Get buttonState value.
        //byte buttonState = buttonStates[keyCode];

        //Return ButtonState.
        //return buttonState;


        //Presssed -> Held / Released -> Not Held.
        //buttonStates[keyCode] &= (keyState >= STATE_HELD) ? STATE_HELD : STATE_NOT_HELD;
        //buttonStates[keyCode] &= Controller.STATE_HELD;
    }

    public void setButtonState(int button, byte state)
    {
        button <<= 1;//Location in bitmask is (buttonID * 2) bits.

        buttonStates =
        (
            ~(buttonStates & (Controller.STATE_PRESSED << button))//Remove button's state from the bitmask.
            & (state << button)//Put the new state in its place.
        );
    }


    /*
     * Mouse Cursor
     */

    /**This is the function GLFW will refer to whenever the Mouse Cursor moves.*/
    public void mouseCursorCallback(long window, double x, double y)
    {
        /*
        if(screen.maintainAspectRatio())
        {
            this.x = ((x - screen.getViewportX()) / screen.getViewportWidth()) * screen.getWidth();
            this.y = ((y - screen.getViewportY()) / screen.getViewportHeight()) * screen.getHeight();
        }
        else
        {
            int[] width = new int[1], height = new int[1];
            glfwGetWindowSize(window, width, height);

            this.x = (x/width[0]) * screen.getWidth();
            this.y = (y/height[0]) * screen.getHeight();
        }
        */


        this.x = (x - screen.getViewportX()) * screen.getViewportWidthRatio();// / screen.getViewportWidth()) * screen.getWidth();
        this.y = (y - screen.getViewportY()) * screen.getViewportHeightRatio();// / screen.getViewportHeight()) * screen.getHeight();

        isMoving = true;
        //System.out.println(this.x + " " + this.y);
    }

    /**Returns if the mouse is moving.*/
    public boolean isMoving(){return isMoving;}
    
    /**Returns Mouse's X-Position.*/
    public double getX(){return x;}

    /**Returns Mouse's Y-Position.*/
    public double getY(){return y;}


    /*
     * Mouse Scroll Wheel
     */

    /**This is the function GLFW will refer to whenever the Mouse Scroll-Wheel is used.*/
    public void mouseScrollCallback(long window, double xOffset, double yOffset)
    {
        //This program isn't going to do anything with horizontal scroll input, so ignore it.
        scroll = (byte)((yOffset < 0) ? 1 : -1);
    }

    /**Returns which direction the Scroll-Wheel was scrolled in.*/
    public byte getScroll()
    {
        byte result = scroll;
        scroll = 0;
        return result;
    }

    public byte getScroll_Continue(){return scroll;}

    public void setScroll(byte scroll){this.scroll = scroll;}
    public void resetScroll(){scroll = 0;}
}
