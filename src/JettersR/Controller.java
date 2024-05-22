package JettersR;
/**
 * Main Input Device for the game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/23/2024
 */
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFWGamepadState;

//import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFW.*;

import JettersR.Data.Profile;

public final class Controller
{
    //Left bit is "hold" bit. Right bit is "change" bit.
    public static final byte
    STATE_PRESSED   = 0b11,
    STATE_HELD      = 0b10,
    STATE_RELEASED  = 0b01,
    STATE_NOT_HELD  = 0b00;
    

    //Array of bytes tracking the input state of keys.
    private final byte[] keyStates = new byte[350];//TODO: DO I really need EVERY key?

    public static final byte MAX_PLAYERS = 8;
    private final String[] controllerPresent = new String[MAX_PLAYERS];

    //Two Custom Buttons for Triggers
    public static final int LEFT_TRIGGER = GLFW_GAMEPAD_BUTTON_LAST+1,
    RIGHT_TRIGGER = GLFW_GAMEPAD_BUTTON_LAST+2;


    //IDs for Menu Actions.
    public static final int MAX_MENU_ACTIONS = 9,
    menu_UP = 0,
    menu_DOWN = 1,
    menu_LEFT = 2,
    menu_RIGHT = 3,
    //
    menu_CONFIRM = 4,
    menu_CANCEL = 5,
    menu_SPECIAL_0 = 6,
    menu_SPECIAL_1 = 7,
    menu_LEFT_TAB = 8,
    menu_RIGHT_TAB = 9;

    //Global Menu Keys.
    public static final int[][] menu_Keys =
    {
        //Player 1/5
        {
            GLFW_KEY_W, GLFW_KEY_S, GLFW_KEY_A, GLFW_KEY_D,
            GLFW_KEY_F, GLFW_KEY_G, GLFW_KEY_Q, GLFW_KEY_E, GLFW_KEY_R, GLFW_KEY_T
        },
        //Player 2/6
        {
            GLFW_KEY_I, GLFW_KEY_K, GLFW_KEY_J, GLFW_KEY_L,
            GLFW_KEY_SEMICOLON, GLFW_KEY_APOSTROPHE, GLFW_KEY_U, GLFW_KEY_O, GLFW_KEY_P, GLFW_KEY_LEFT_BRACKET
        },
        //Player 3/7
        {
            GLFW_KEY_UP, GLFW_KEY_DOWN, GLFW_KEY_LEFT, GLFW_KEY_RIGHT,
            GLFW_KEY_KP_0, GLFW_KEY_KP_DECIMAL, GLFW_KEY_RIGHT_CONTROL, GLFW_KEY_KP_1, GLFW_KEY_KP_2, GLFW_KEY_KP_3
        },
        //Player 4/8
        {
            GLFW_KEY_KP_8, GLFW_KEY_KP_5, GLFW_KEY_KP_4, GLFW_KEY_KP_6,
            GLFW_KEY_KP_ADD, GLFW_KEY_KP_SUBTRACT, GLFW_KEY_KP_7, GLFW_KEY_KP_9, GLFW_KEY_KP_DIVIDE, GLFW_KEY_KP_MULTIPLY
        },
    };

    public static final int[] menu_Buttons =
    {
        GLFW_GAMEPAD_BUTTON_DPAD_UP, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT,
        GLFW_GAMEPAD_BUTTON_A, GLFW_GAMEPAD_BUTTON_B, GLFW_GAMEPAD_BUTTON_X, GLFW_GAMEPAD_BUTTON_Y, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER
    };

    //Gee, thanks guide button...
    private GLFWGamepadState[] controllerStates = new GLFWGamepadState[MAX_PLAYERS];

    /**Constructor.*/
    public Controller()
    {
        //Initialize gamepad state.
        for(int i = 0; i < controllerStates.length; i++)
        {controllerStates[i] = GLFWGamepadState.calloc();}

        //checkForControllers();

        //Set menu hold time values for each player.
        for(int i = 0; i < menuHold.length; i++)
        {
            menuHold[i] = false;
            menuHoldTime[i] = MENU_HOLD_START_TIME;
        }
    }

    //Button Buffers.
    private ByteBuffer[] buttonBuffers = new ByteBuffer[MAX_PLAYERS];

    //Button States. //TODO Why is this +1 and not +2?
    private byte[][] buttonStates = new byte[MAX_PLAYERS][GLFW_GAMEPAD_BUTTON_LAST+1];

    //Axes.
    private FloatBuffer[] axes = new FloatBuffer[MAX_PLAYERS];

    //Menu stuff.
    public static final byte MENU_REPEAT = 4, MENU_HOLD_START_TIME = -12 + MENU_REPEAT;
    private boolean[] menuHold = new boolean[MAX_PLAYERS];
    private byte[] menuHoldTime = new byte[menuHold.length];

    /**Checks for Button and Axes input*/
    public void update()
    {
        GLFWGamepadState currentGamepadState;
        ByteBuffer currentButtonbuffer;
        FloatBuffer currentAxes;

        //Update menu input.
        for(int playerNum = 0; playerNum < MAX_PLAYERS; playerNum++)
        {
            //Menu repeat.
            if(menuHold[playerNum])
            {
                //Increment time.
                menuHoldTime[playerNum]++;
                //if(playerNum == 0){System.out.println(menuHoldTime[playerNum]);}

                //Loop it if needed.
                if(menuHoldTime[playerNum] > MENU_REPEAT)
                {menuHoldTime[playerNum] = 1;}

                //Do this to reset the hold timer upon the player releasing a menu input.
                menuHold[playerNum] = false;
            }
            else{menuHoldTime[playerNum] = MENU_HOLD_START_TIME;}


            //
            //TODO: Recheck this note.
            //
            //NOTE: The following block of code will crash if the player's controller isn't recognized by GLFW.
            //To prevent this, the controller's GUID, name, and bindings need to be added to extraControllerBindings.txt.
            //

            //Get controller's input state if one is plugged in.
            currentGamepadState = controllerStates[playerNum];
            if(glfwGetGamepadState(playerNum, currentGamepadState))
            {
                //Get state of buttons.
                buttonBuffers[playerNum] = currentGamepadState.buttons();
                currentButtonbuffer = buttonBuffers[playerNum];

                //Get Button Press Inputs.
                byte bState;
                for(int buttonNum = 0; buttonNum < currentButtonbuffer.capacity(); buttonNum++)
                {
                    //Get value from buffer (0 = not held, 1 = held)
                    bState = currentButtonbuffer.get();

                    //Not held case.
                    //if(b == 0)
                    //{buttonStates[i][j] = (buttonStates[i][j] >= STATE_RELEASED) ? STATE_RELEASED : STATE_NOT_HELD;}
                    //
                    //Held case.
                    //else
                    //{buttonStates[i][j] = (buttonStates[i][j] >= STATE_PRESSED) ? STATE_PRESSED : STATE_HELD;}

                    //Run the relevent state setter.
                    //buttonStateSetters[b].invoke(playerNum, buttonNum);
                    //buttonStates[playerNum][buttonNum] = (byte)(0b10 | b);


                    //"A picture is worth a thousand words."
                    // -Psyk
                    //
                    //(00 ^ 01) = 01 (<< 1) = 10 | 01 = 11
                    //(11 ^ 01) = 10 (<< 1) = 00 | 01 = 01 //Note: & 0b11 needed
                    //(01 ^ 01) = 00 (<< 1) = 00 | 01 = 01
                    //
                    //(01 ^ 00) = 01 (<< 1) = 10 | 00 = 10
                    //(10 ^ 00) = 10 (<< 1) = 00 | 00 = 00 //Note: & 0b11 needed
                    //(00 ^ 00) = 00 (<< 1) = 00 | 00 = 00


                    buttonStates[playerNum][buttonNum] = (byte)
                    ((//(
                        (buttonStates[playerNum][buttonNum] ^ bState)//This xor sets the right bit to 1 if the oppposite press state was triggered.
                        //<< 1)//This makes the value of the right bit the left one's now.
                        | (bState << 1))//Append hold state.
                        & 0b11//Limit from 0b00 to 0b11.
                    );
                }

                //Get stick and trigger axes.
                axes[playerNum] = controllerStates[playerNum].axes();
                currentAxes = axes[playerNum];

                
                //Left Trigger check.
                bState = (currentAxes.get(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) > 0.9f) ? (byte)0b1 : (byte)0b0;
                buttonStates[playerNum][LEFT_TRIGGER] = (byte)((((buttonStates[playerNum][LEFT_TRIGGER] ^ bState) << 1) | bState) & 0b11);

                //Right Trigger check.
                bState = (currentAxes.get(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) > 0.9f) ? (byte)0b1 : (byte)0b0;
                buttonStates[playerNum][RIGHT_TRIGGER] = (byte)((((buttonStates[playerNum][RIGHT_TRIGGER] ^ bState) << 1) | bState) & 0b11);
                

                //System.out.println("Buttons: " + buffer.get(0) + "" + buffer.get(1) + ""+ buffer.get(2) + "" + buffer.get(3)
                //+ "" + buffer.get(4) + "" + buffer.get(5) + "" + buffer.get(6) + "" + buffer.get(7)
                //+ "" + buffer.get(8) + "" + buffer.get(9) + "" + buffer.get(10) + "" + buffer.get(11)
                //+ "" + buffer.get(12) + ""  + buffer.get(13) + ""  + buffer.get(14));

                //System.out.println("Axes: " + axes[i].get(0) + " " + axes[i].get(1) + " "
                //+ axes[i].get(2) + " " + axes[i].get(3) + " "
                //+ axes[i].get(4) + " " + axes[i].get(5));
                
            }
            //else{System.err.println(glfwGetGamepadName(i));}
        }
    }

    /*
    @FunctionalInterface
    private interface ButtonStateSetter{public abstract void invoke(int playerNum, int button);}

    private ButtonStateSetter[] buttonStateSetters = new ButtonStateSetter[]
    {
        (playerNum, button) -> {buttonStates[playerNum][button] = STATE_RELEASED;},
        (playerNum, button) -> {buttonStates[playerNum][button] = STATE_PRESSED;},
        (playerNum, button) -> {}
    };
    */

    /**To be used on program startup*/
    public void checkForControllers()
    {
        //Strings are stored instead of booleans so that way, the program
        //can differentiate between an XBOX 360 and PS4 controller, for example.

        //Controller GUID Keywords: Xbox, NSW

        for(int i = 0; i < MAX_PLAYERS; i++)
        {
            //System.out.println(glfwJoystickPresent(i));
            controllerPresent[i] = glfwGetJoystickName(i);
            //System.out.println(controllerPresent[i]);
        }
    }


    /*
     * Keyboard Input checks.
     */

    /**This is this function GLFW refers to when recieving Keyboard input*/
    public void keyCallback(long window, int key, int scancode, int action, int mods)
    {
        //If in a Typing Prompt, check for BackSpace and ignore important actions.
        if(typingMode != TYPING_NONE)
        {
            if(action == GLFW_PRESS || action == GLFW_REPEAT)
            {
                if(key == GLFW_KEY_LEFT && cursorIndex > 0){cursorIndex--;}
                else if(key == GLFW_KEY_RIGHT && cursorIndex < stringBuffer.length()){cursorIndex++;}

                //Backspace check.
                if(key == GLFW_KEY_BACKSPACE && stringBuffer.length() > 0 && cursorIndex > 0)
                {
                    //Delete the character at the current index.
                    stringBuffer.deleteCharAt(cursorIndex-1);

                    //Move cursor index back one slot.
                    cursorIndex--;
                }
                //else if(key == GLFW_KEY_ENTER){typingMode = TYPING_NONE;}
            }
            //return;
        }

        //Otherwise, check important actions
        if(key == GLFW_KEY_UNKNOWN){return;}

        //if(action == GLFW_PRESS || action == GLFW_REPEAT){System.out.println(action);}
        //
        //if(action == GLFW_PRESS){keyStates[key] = STATE_PRESSED;}
        //else if(action == GLFW_RELEASE){keyStates[key] = STATE_RELEASED;}
        keyStateSetters[action].invoke(key);
    }

    @FunctionalInterface
    private interface KeyStateSetter{public abstract void invoke(int keyCode);}

    private KeyStateSetter[] keyStateSetters = new KeyStateSetter[]
    {
        (keyCode) -> {keyStates[keyCode] = STATE_RELEASED;},
        (keyCode) -> {keyStates[keyCode] = STATE_PRESSED;},
        (keyCode) -> {}
    };

    /**Checks if a key is being held.*/
    public boolean isKeyHeld(int keyCode){return keyStates[keyCode] >= STATE_HELD;}

    /**Same thing as isKeyHeld(), but disables the key upon retrival, allowing for Input-Buffering.*/
    public boolean isKeyPressed(int keyCode)
    {
        boolean result = (keyStates[keyCode] == STATE_PRESSED);

        keyStates[keyCode] &= STATE_HELD;
        if(keyCode == GLFW_KEY_ENTER && keyStates[keyCode] != 0){System.out.println(keyStates[keyCode]);}

        return result;
    }

    /**Same thing as isKeyPressed, but for releasing.*/
    public boolean isKeyReleased(int keyCode)
    {
        boolean result = (keyStates[keyCode] == STATE_RELEASED);

        keyStates[keyCode] = STATE_NOT_HELD;
        return result;
    }

    /**Gets the KeyState of the given key.*/
    public byte getKeyState(int keyCode)
    {
        //Get keyState value.
        byte keyState = keyStates[keyCode];

        //Presssed -> Held / Released -> Not Held.
        //keyStates[keyCode] &= STATE_HELD;

        //Return KeyState.
        return keyState;
    }



    /*
     * Typing input checks.
     */

    public static final byte TYPING_NONE = 0,
    TYPING_ANY = 1,
    TYPING_INT = 2,
    TYPING_SIGNED_INT = 3,
    TYPING_FLOAT = 4,
    TYPING_LETTER = 5;

    private byte typingMode = TYPING_NONE;
    private int cursorIndex = 0, charLimit = 0;
    private StringBuffer stringBuffer = null;

    /**Allows for typing in-program without external API.*/
    public void charCallback(long window, int codepoint)
    {
        char c = (char)codepoint;
        //System.out.println(codepoint);

        switch(typingMode)
        {
            case TYPING_NONE:
            //Prevent typing entirely.
            return;

            case TYPING_ANY:
            //Allow any character.
            break;

            case TYPING_INT:
            //Only allow numbers.
            if(c < '0' || c > '9'){return;}
            break;

            case TYPING_SIGNED_INT:
            {
                //Only allow numbers and minus sign.
                if
                (!(
                    (c >= '0' || c <= '9')
                    || (c == '-' && stringBuffer.charAt(0) != '-' && cursorIndex == 0)
                ))
                {return;}
            }
            break;

            case TYPING_FLOAT:
            //Only allow numbers and decimal points.
            if((c < '0' || c > '9') && c != '.'){return;}
            break;

            case TYPING_LETTER:
            //Only allow letters.
            if((c < 'A' || c > 'Z') && (c < 'a' || c > 'z')){return;}
            break;
        }
        //char n = '';

        if(cursorIndex < charLimit)
        {
            //Insert the character into the string at the current index.
            stringBuffer.insert(cursorIndex, c);
                
            //Increment the index.
            cursorIndex++;
        }

        //System.out.println(stringObject);
        //System.out.println((char)codepoint);
        
    }

    public boolean isTyping(){return typingMode != TYPING_NONE;}
    public int getCursorIndex()
    {
        //if(cursorIndex >= stringObject.length()){cursorIndex = stringObject.length()-1;}
        return cursorIndex;
    }

    public void beginTyping(StringBuffer stringBuffer, final byte typingMode, int cursorIndex, int charLimit)
    {
        //Set typing mode.
        this.typingMode = typingMode;

        //Set stringBuffer to type into.
        this.stringBuffer = null;
        this.stringBuffer = stringBuffer;

        //Set cursor index.
        this.cursorIndex = (cursorIndex > stringBuffer.length()) ? stringBuffer.length() : cursorIndex;

        //Set characater limit.
        this.charLimit = charLimit;
    }
    public void beginTyping(StringBuffer stringObject, final byte typingMode, int charLimit){beginTyping(stringObject, typingMode, stringObject.length(), charLimit);}
    public void beginTyping(StringBuffer stringObject, int charLimit){beginTyping(stringObject, TYPING_ANY, stringObject.length(), charLimit);}

    public StringBuffer getStringBuffer(){return this.stringBuffer;}

    /**End typing on the current StringBuffer.*/
    public void endTyping()
    {
        typingMode = TYPING_NONE;
        stringBuffer = null;
        //return this.stringObject;
    }



    /*
     * Controller input checks.
     */

    /**This is the function GLFW will refer to whenever a Controller is Connected or Disconnected.*/
    public void controllerCallback(int controllerID, int event)
    {
        if(event == GLFW_CONNECTED)
        {controllerPresent[controllerID] = glfwGetJoystickName(controllerID);}
        else if(event == GLFW_DISCONNECTED)
        {controllerPresent[controllerID] = null;}
    }

    /**Returns a button input every frame it is held*/
    public boolean buttonHeld(int playerNum, int button){return (buttonStates[playerNum][button] >= STATE_HELD);}

    /**Returns a button input once the moment it is checked for*/
    public boolean buttonPressed(int playerNum, int button)
    {
        boolean result = (buttonStates[playerNum][button] == STATE_PRESSED);

        buttonStates[playerNum][button] &= STATE_HELD;
        return result;
    }

    /**Same thing as isKeyPressed, but for releasing.*/
    public boolean buttonReleased(int playerNum, int button)
    {
        boolean result = (buttonStates[playerNum][button] == STATE_RELEASED);

        buttonStates[playerNum][button] = STATE_NOT_HELD;
        return result;
    }

    /**Gets the KeyState of the given key.*/
    public byte getButtonState(int playerNum, int button)
    {
        //Get buttonState value.
        byte buttonState = buttonStates[playerNum][button];

        //Presssed -> Held / Released -> Not Held.
        //buttonStates[playerNum][button] &= STATE_HELD;

        //Return ButtonState.
        return buttonState;
    }

    //Control stick deadZone.
    private float deadZone = 0.3f;
    public float getDeadZone(){return deadZone;}

     /**
      * Returns -1.0f to 1.0f values of either Control Sticks or Triggers of the given controller.
      *
      * @param playerNum is the Player Number.
      * @param a is the GLFW control stick/trigger axis you want to check for.
      * @return a float from -1.0f to 1.0f of the requested axis.
      */
    public float getAxes(int playerNum, int a)
    {
        float result = 0.0f;

        if(glfwJoystickPresent(playerNum))
        {
            result = axes[playerNum].get(a);

            //Left/Up Side (LiveZone)
            result = (result <= -0.9f) ? -1.0f  

            //Right/Down Side (LiveZone)
            : (result >= 0.9f) ? 1.0f

            //Middle (DeadZone)
            : (result >= -deadZone && result <= deadZone) ? 0.0f
            
            //Everything else
            : result;
        }

        return result;
    }



    /*
     * Input in general.
     */

    /**Returns a held input from either Keyboard or Controller.*/
    public boolean inputHeld(Profile profile, int playerNum, int action)
    {
        if(glfwJoystickPresent(playerNum))
        {
            int button = profile.buttons[action];

            switch(button)
            {
                case LEFT_TRIGGER: return (getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) > 0.0f);

                case RIGHT_TRIGGER: return (getAxes(playerNum, GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) > 0.0f);
                
                default: return buttonHeld(playerNum, button);
            }
        }

        return isKeyHeld(profile.keys[playerNum][action]);
    }

    /**Returns a Single-Frame Bufferable input for either Keyboard or Controller*/
    public boolean inputPressed(Profile profile, int playerNum, int action)
    {
        if(glfwJoystickPresent(playerNum))
        {
            int button = profile.buttons[action];

            switch(button)
            {
                //TODO trigger press inputs.
                case LEFT_TRIGGER: return (getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) > 0.0f);

                case RIGHT_TRIGGER: return (getAxes(playerNum, GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) > 0.0f);

                default: return buttonPressed(playerNum, button);
            }
        }
        return isKeyPressed(profile.keys[playerNum][action]);
    }

    /**Returns th current input state of the given action for either Keyboard or Controller.*/
    public byte getInputState(Profile profile, int playerNum, int action)
    {
        if(glfwJoystickPresent(playerNum))
        {
            int button = profile.buttons[action];

            switch(button)
            {
                //TODO trigger input states.
                //case LEFT_TRIGGER: return (getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) > 0.0f);

                //case RIGHT_TRIGGER: return (getAxes(playerNum, GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) > 0.0f);

                default: return getButtonState(playerNum, button);
            }
        }
        return getKeyState(profile.keys[playerNum][action]);
    }

    /**This is meant for use with Profile.assignKey()*/
    public int anyKey()
    {
        for(int i = 0; i < keyStates.length; i++)
        {if(keyStates[i] == STATE_PRESSED){return i;}}
        return -1;
    }

    /**
     * Input Held Method for Menus.
     * 
     * @param playerNum is the Player Number.
     * @param action is the action to check. Use menu_[action].
     * @return true if an input assciated with the given action is held down.
     */
    public boolean menu_InputHeld(int playerNum, int action)
    {
        if(glfwJoystickPresent(playerNum))
        {
            int button = menu_Buttons[action];

            boolean dPad = false;
            float axes;
            switch(action)
            {
                case menu_UP:
                {
                    dPad = buttonHeld(playerNum, button);
                    axes = getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_Y);
                    if(axes < -deadZone || dPad){return true;}
                }
                break;

                case menu_DOWN:
                {
                    dPad = buttonHeld(playerNum, button);
                    axes = getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_Y);
                    if(axes > deadZone || dPad){return true;}
                }
                break;

                case menu_LEFT:
                {
                    dPad = buttonHeld(playerNum, button);
                    axes = getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_X);
                    if(axes < -deadZone || dPad){return true;}
                }
                break;

                case menu_RIGHT:
                {
                    dPad = buttonHeld(playerNum, button);
                    axes = getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_X);
                    if(axes > deadZone || dPad){return true;}
                }
                break;

                default: return buttonHeld(playerNum, button);
            }
        }

        //Resort to keyboard input.
        return (playerNum >= 4) ? false
        : isKeyHeld(menu_Keys[playerNum][action]);
    }

    /**
     * Input Pressed Method for Menus.
     * 
     * @param playerNum is the Player Number.
     * @param action is the action to check. Use menu_X.
     * @return true if an input assciated with the given action is initially being pressed.
     */
    public boolean menu_InputPressed(int playerNum, int action, boolean repeat)
    {
        boolean result = menu_InputHeld(playerNum, action);

        //Initial press check.
        if(result && !menuHold[playerNum])
        {
            //Menu repeat.
            if(repeat)
            {
                //Menu input is being held.
                menuHold[playerNum] = true;

                //Return false if and input was already being held or if the repeat timer either didn't just start or will not loop.
                if(!(menuHoldTime[playerNum] == MENU_REPEAT || menuHoldTime[playerNum] == MENU_HOLD_START_TIME))
                {return false;}

                return true;
            }
            //Basic Press.
            else
            {
                //Menu input is being held.
                menuHold[playerNum] = true;

                //If input was already being held.
                if(menuHoldTime[playerNum] != MENU_HOLD_START_TIME)
                {
                    //Stop hold timer and return false.
                    menuHoldTime[playerNum] = MENU_HOLD_START_TIME+1;
                    return false;
                }
                else{return true;}
            }
        }
        
        return false;
    }    

    /**Checks if any player is holding the given input.*/
    public boolean menu_InputHeld_AnyPlayer(int action)
    {
        for(int i = 0; i < MAX_PLAYERS; i++)
        {
            if(menu_InputHeld(i, action))
            {return true;}
        }
        return false;
    }

    /**Checks if any player has pressed the given input.*/
    public boolean menu_InputPressed_AnyPlayer(int action, boolean repeat)
    {
        //Go through each player.
        for(int i = 0; i < MAX_PLAYERS; i++)
        {
            //Return true if this player pressed this input.
            if(menu_InputPressed(i, action, repeat))
            {return true;}
        }

        //Default result.
        return false;
    }
}
