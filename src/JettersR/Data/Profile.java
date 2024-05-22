package JettersR.Data;
/**
 * 
 */
import static org.lwjgl.glfw.GLFW.*;

import JettersR.Controller;

public class Profile
{
    /*
     * Bomber-Related stuff
     */

    //Color pallete

    /*
     * Controls-Related Stuff
     */

    //ID Numbers for all the different actions a key can be bound to
    public transient static final byte MAX_ACTIONS = 11,
    action_UP = 0,
    action_DOWN = 1,
    action_LEFT = 2,
    action_RIGHT = 3,
    //
    action_BOMB = 4,
    action_PUNCH = 5,
    action_REMOTE = 6,
    action_SPECIAL = 7,
    //
    action_BOMBWHEEL = 8,
    action_SPECIALWHEEL = 9,
    //
    action_PAUSE = 10;
    

    //Array storing Keyboard Keys for this Profile.
    public int[][] keys = new int[Controller.MAX_PLAYERS][MAX_ACTIONS];

    //Array storing Controller Buttons for this Profile.
    public int[] buttons = new int[MAX_ACTIONS];

    /**
     * Default constructor.
     * 
     * Creates a Profile with default controls.
     */
    public Profile()
    {
        this.keys = defaultKeys();
        this.buttons = defaultButtons();
    }

    /**Default Keyboard Control Scheme for Players.*/
    public static int[] defaultKeys(int playerNum)
    {
        switch(playerNum)
        {
            case 0:
            case 4:
            return new int[]
            {
                GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D,
                GLFW_KEY_F, GLFW_KEY_G, GLFW_KEY_E, GLFW_KEY_Q,
                GLFW_KEY_R, GLFW_KEY_T
            };

            case 1:
            case 5:
            return new int[]
            {
                GLFW_KEY_I, GLFW_KEY_K, GLFW_KEY_J, GLFW_KEY_L,
                GLFW_KEY_SEMICOLON, GLFW_KEY_APOSTROPHE, GLFW_KEY_O, GLFW_KEY_U,
                GLFW_KEY_P, GLFW_KEY_LEFT_BRACKET
            };

            case 2:
            case 6:
            return new int[]
            {
                GLFW_KEY_UP, GLFW_KEY_DOWN, GLFW_KEY_LEFT, GLFW_KEY_RIGHT,
                GLFW_KEY_KP_0, GLFW_KEY_KP_DECIMAL, GLFW_KEY_KP_1, GLFW_KEY_RIGHT_CONTROL,
                GLFW_KEY_KP_2, GLFW_KEY_KP_3
            };

            case 3:
            case 7:
            return new int[]
            {
                GLFW_KEY_KP_8, GLFW_KEY_KP_5, GLFW_KEY_KP_4, GLFW_KEY_KP_6,
                GLFW_KEY_KP_ADD, GLFW_KEY_KP_SUBTRACT, GLFW_KEY_KP_9, GLFW_KEY_KP_7,
                GLFW_KEY_KP_DIVIDE, GLFW_KEY_KP_MULTIPLY
            };

            default: return null;
        }
    }

    /**Used for Controller class initialization.*/
    public static int[][] defaultKeys()
    {
        int[][] result = new int[Controller.MAX_PLAYERS][MAX_ACTIONS];

        for(int p = 0; p < result.length; p++)
        {result[p] = defaultKeys(p);}

        return result;
    }

    /**Used for when the Player wants to completly erase the Control Scheme for a different Player Slot*/
    public static int[] nullKeys()
    {
        return new int[]
        {
            GLFW_KEY_UNKNOWN, GLFW_KEY_UNKNOWN, GLFW_KEY_UNKNOWN, GLFW_KEY_UNKNOWN,
            GLFW_KEY_UNKNOWN, GLFW_KEY_UNKNOWN, GLFW_KEY_UNKNOWN, GLFW_KEY_UNKNOWN,
            GLFW_KEY_UNKNOWN, GLFW_KEY_UNKNOWN
        };
    }

    /**
     * Default Controller Control Scheme for Players.
     * 
     * For whatever reason, GLFW's "GetJoystickButtons" function does NOT include the Guide Button
     * despite having it as a constant, meaning everything from LEFT_THUMB and up needs to be subtracted
     * by one to prevent index out-of-bounds exceptions when receiving input. Gawd diamit GLFW...
     */
    public static int[] defaultButtons()
    {
        return new int[]
        {
            GLFW_GAMEPAD_BUTTON_DPAD_UP, GLFW_GAMEPAD_BUTTON_DPAD_DOWN,
            GLFW_GAMEPAD_BUTTON_DPAD_LEFT, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT,
            GLFW_GAMEPAD_BUTTON_A, GLFW_GAMEPAD_BUTTON_X, GLFW_GAMEPAD_BUTTON_Y, GLFW_GAMEPAD_BUTTON_B,
            GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER
        };
    }

    /**Used for Controller class initialization.*/
    public static int[][] defaultButtonsArray()
    {
        int[][] result = new int[Controller.MAX_PLAYERS][MAX_ACTIONS];

        for(int p = 0; p < result.length; p++)
        {result[p] = defaultButtons();}

        return result;
    }
}
