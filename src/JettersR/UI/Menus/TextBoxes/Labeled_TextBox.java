package JettersR.UI.Menus.TextBoxes;
/**
 * 
 */
import JettersR.Graphics.Screen;
import JettersR.Graphics.Font;
import JettersR.UI.Menus.MenuComponent;

public class Labeled_TextBox extends MenuComponent
{
    //Label for textBox.
    private String label = null;

    //TextBox.
    private TextBox textBox = null;

    /**Constructor.*/
    public Labeled_TextBox(String label, TextBox textBox)
    {
        super((int)textBox.getX(), (int)textBox.getY(), textBox.getShape());

        this.label = label;
        this.textBox = textBox;
    }

    //Text setter.
    public TextBox setText(String text){return textBox.setText(text);}
    public TextBox setText(int text){return textBox.setText(text);}

    @Override
    public final boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {return textBox.update(xOffset, yOffset, isCurrentChoice, mouseIntersectsThis);}

	@Override
    public final void render(Screen screen, float xOffset, float yOffset)
    {
        //TextBox.
        textBox.render(screen, xOffset, yOffset);

        //Label.
        int xa = (int)(position.x + xOffset) + shape.left(), ya = (int)(position.y + yOffset) + shape.up();
        Font f = textBox.getFont();
        f.render_RtoL(screen, xa-1, ya+1, this.label, textBox.getTextScale(), textBox.getFontColor(), false);
    }

   
}
