package JettersR.UI.Menus.TextBoxes;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/26/2023
 */
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import JettersR.Game;
import JettersR.Controller;
//import JettersR.Graphics.Screen;
import JettersR.Graphics.Font;
import JettersR.UI.Menus.MenuComponent;
import JettersR.Util.Fixed;
import JettersR.Util.Annotations.fixed;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

import static JettersR.Util.Fixed.*;

public abstract class TextBox extends MenuComponent
{
    //Keyboard pointer.
    protected Controller keyboard;

    //Text values.
    protected Font font;
    protected Vector4f fontColor;
    protected int charLimit;
    protected byte typingMode;
    protected float textScale = 1.0f;
    protected StringBuffer textBuffer = new StringBuffer("");

    //Cursor blink time.
    protected byte cursorTime = 0;

    /**Constructor.*/
    public TextBox(int x, int y, AAB_Box2D box, String text, Font font, Vector4f fontColor, int charLimit, byte typingMode, float textScale)
    {
        //Create collision.
        super(x, y, box);
        keyboard = Game.controller;

        //Text values.
        this.textBuffer = new StringBuffer(text);
        this.font = font;
        this.fontColor = fontColor;
        this.charLimit = charLimit;
        this.typingMode = typingMode;
        this.textScale = textScale;
    }

    /**Constructor.*/
    public TextBox(int x, int y, AAB_Box2D box, Font font, Vector4f fontColor, int charLimit, byte typingMode, float textScale)
    {this(x, y, box, "", font, fontColor, charLimit, typingMode, textScale);}


    public float getTextScale(){return textScale;}

    public Font getFont(){return font;}
    public Vector4f getFontColor(){return fontColor;}

    public int getCharLimit(){return charLimit;}
    public void setCharLimit(int charLimit){this.charLimit = charLimit;}

    public byte getTypingMode(){return typingMode;}
    public void setTypingMode(byte typingMode){this.typingMode = typingMode;}


    public final boolean confirm_Type(boolean mouseIntersectsThis)
    {
        return
        (
            //If not typing and the current StringBuffer in the Controller class is not the one here.
            !keyboard.isTyping() && keyboard.getStringBuffer() != this.textBuffer

            //Click inside of this textbox to start typing in it.
            && input_Pressed(0, Controller.menu_CONFIRM)
            || (mouseIntersectsThis && Game.mouse.getButtonState(GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_RELEASED)
        );
    }

    public final boolean enterCheck(boolean mouseIntersectsThis)
    {
        //boolean ent = keyboard.isKeyPressed(GLFW_KEY_ENTER);
        //if(ent){System.out.println(ent);}

        return //ent
        keyboard.isKeyPressed(GLFW.GLFW_KEY_ENTER)
        || (mouseIntersectsThis == false && Game.mouse.getButtonState(GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_PRESSED);
    }


    //Text setter.
    public TextBox setText(String text)
    {
        //Replace the char[] in the StringBuffer with the input text.
        this.textBuffer.replace(0, textBuffer.length(), text);
        return this;
    }
    public TextBox setText(int text)
    {
        //Replace the char[] in the StringBuffer with the input text.
        this.textBuffer.replace(0, textBuffer.length(), Integer.toString(text));
        return this;
    }

    public void beginTyping()
    {
        keyboard.beginTyping(textBuffer, typingMode, 0, charLimit);
    }
    

    public byte getText_Byte()
    {
        try
        {
            //Try to parse the text as a byte.
            byte result = Byte.parseByte(textBuffer.toString());
            return result;
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }

        //Revert to zero if the parse couldn't be done.
        textBuffer.replace(0, textBuffer.length(), "0");
        return 0;
    }

    public short getText_Short()
    {
        try
        {
            //Try to parse the text as a short.
            short result = Short.parseShort(textBuffer.toString());
            return result;
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }

        //Revert to zero if the parse couldn't be done.
        textBuffer.replace(0, textBuffer.length(), "0");
        return 0;
    }

    public int getText_Int()
    {
        try
        {
            //Try to parse the text as an Int.
            int result = Integer.parseInt(textBuffer.toString());
            return result;
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }

        //Revert to zero if the parse couldn't be done.
        textBuffer.replace(0, textBuffer.length(), "0");
        return 0;
    }

    public @fixed int f_getText_Int()
    {
        try
        {
            String whole = "", frac = "";

            //Try to parse the text as a fixed point int.
            boolean decimal = false;
            for(int i = 0; i < textBuffer.length(); i++)
            {
                if(!decimal)
                {
                    char c = textBuffer.charAt(i);
                    if(c == '.'){decimal = true;}
                    else{whole += c;}
                }
                else{frac += textBuffer.charAt(i);}
            }

            //Clamp the fractional portion.
            int fraction = Integer.parseInt(frac);
            if(fraction > Fixed.f_FRACTION_PORTION){fraction = Fixed.f_FRACTION_PORTION;}

            //Create the resulting fixed point number.
            int result = fixed(Integer.parseInt(whole), fraction);
            return result;
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }

        //Revert to zero if the parse couldn't be done.
        textBuffer.replace(0, textBuffer.length(), "0.0");
        return 0;
    }

    public char getText_Char()
    {
        char result = textBuffer.charAt(0);
        return result;
    }

    public String getText_String()
    {
        return textBuffer.toString();
    }

    public float getText_Float()
    {
        try
        {
            //Try to parse the text as a Float.
            float result = Float.parseFloat(textBuffer.toString());
            return result;
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }

        //Revert to 0.0 if the parse couldn't be done.
        textBuffer.replace(0, textBuffer.length(), "0.0");
        return 0.0f;
    }

    public double getText_Double()
    {
        try
        {
            //Try to parse the text as a Float.
            double result = Double.parseDouble(textBuffer.toString());
            return result;
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
        }

        //Revert to 0.0 if the parse couldn't be done.
        textBuffer.replace(0, textBuffer.length(), "0.0");
        return 0.0;
    }

    /*
    @Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);
        
        //shape.render(screen, 1.0f, xa, ya, false);

        //Text
        font.render(screen, xa+1, ya+1,
        textBuffer.toString(), textScale, fontColor, false);

        //Cursor
        if(keyboard.getStringBuffer() == this.textBuffer && cursorTime < 30)
        {
            font.renderCursor(screen, xa+1, ya+1, textBuffer.toString(), keyboard.getCursorIndex(),
            textScale, fontColor, false);
        }
    }
    */
}
