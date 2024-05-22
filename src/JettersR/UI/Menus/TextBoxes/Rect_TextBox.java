package JettersR.UI.Menus.TextBoxes;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Graphics.Font;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

public abstract class Rect_TextBox extends TextBox
{
	private Vector4f currentRectColor,
    rectColor0, rectColor1;


	/**Constructor.*/
    public Rect_TextBox(int x, int y, AAB_Box2D box, String text, Font font, Vector4f fontColor,
    int charLimit, byte typingMode, float textScale, Vector4f rectColor0, Vector4f rectColor1)
    {
        //Initialize the usual data.
        super(x, y, box, text, font, fontColor, charLimit, typingMode, textScale);
        
        //Colors.
        this.rectColor0 = rectColor0;
        this.rectColor1 = rectColor1;
        this.currentRectColor = this.rectColor0;
    }

    /**Constructor.*/
    public Rect_TextBox(int x, int y, AAB_Box2D box, Font font, Vector4f fontColor,
    int charLimit, byte typingMode, float textScale, Vector4f rectColor0, Vector4f rectColor1)
    {this(x, y, box, "", font, fontColor, charLimit, typingMode, textScale, rectColor0, rectColor1);}


	@Override
    public final boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
	{
		//If this textbox is being highlighted.
        if(isCurrentChoice)
        {
			//Highlght.
            currentRectColor = rectColor1;

            //If not typing.
            if(!keyboard.isTyping())
            {
                //Check for confirmation.
                if(confirm_Type(mouseIntersectsThis))
                {
                    //Play a sound effect.
                    
                    //Send this textbox's StringBuffer to the Controller class to start typing in it.
                    keyboard.beginTyping(textBuffer, typingMode, charLimit);

                    System.out.println("Began typing.");

                    //This textbox was confirmed on.
                    return true;
                }
            }
            //Otherwise, If enter was pressed or the player clicked outside of the TextBox...
            //else 
		}
        else
        {
            //UnHighlght.
            currentRectColor = rectColor0;
        }
		
		//If this textbox is being typed in.
        if(keyboard.getStringBuffer() == this.textBuffer)
        {
            //Advance cursor flicker timer.
            cursorTime = (byte)((cursorTime+1) % 30);
            //Typing logic is handled in the Controller class.

            if(enterCheck(mouseIntersectsThis))
            {
                //End typing.
                keyboard.endTyping();

                //Set value to value in text.
                valueSet();

                System.out.println(keyboard.getStringBuffer());
            }
        }

        //This textbox is not being clicked on.
        return false;
	}

    public abstract void valueSet();

	@Override
    public final void render(Screen screen, float xOffset, float yOffset)
    {
        int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);
        
        //shape.render(screen, 1.0f, xa, ya, false);

        //Rect
        xa += shape.left();
        ya += shape.up();
        screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), currentRectColor, false);

        //Text
        font.render(screen, xa+1, ya+1,
        textBuffer.toString(), textScale, fontColor, false);

        //Cursor
        if(keyboard.getStringBuffer() == this.textBuffer && cursorTime < 20)
        {
            font.renderCursor(screen, xa+1, ya+1, textBuffer.toString(), keyboard.getCursorIndex(),
            textScale, fontColor, false);
        }
    }
}
