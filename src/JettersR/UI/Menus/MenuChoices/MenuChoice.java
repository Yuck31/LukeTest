package JettersR.UI.Menus.MenuChoices;
/**
 * A choice for Menus.
 * 
 * Author: Luke Sullivan
 * Last Edit: 11/18/2023
 */

import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.UI.Menus.MenuComponent;
import JettersR.UI.Visuals.ChoiceVisuals.Basic_ChoiceVisual;
import JettersR.UI.Visuals.ChoiceVisuals.ChoiceVisual;
import JettersR.UI.Visuals.ChoiceVisuals.Sprite_ChoiceVisual;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersR.Util.Shapes.Shapes2D.Shape2D;

public abstract class MenuChoice extends MenuComponent
{
    @FunctionalInterface//Functional Interface for what should happen when this choice is selected
    public interface Action{public abstract void perform();}
    public static final void doNothing(){return;}

    //Action Reference.
    protected Action action = MenuChoice::doNothing;

    //Choice visual component.
    protected ChoiceVisual visual = null;


    /**Constructor.*/
    public MenuChoice(int x, int y, Shape2D shape, Action action, ChoiceVisual visual)
    {
        super(x, y, shape);

        //Set action.
        this.action = action;

        //Set visual.
        this.visual = visual;
    }

    //Action Setter.
    public void setAction(Action action){this.action = action;}


    /*
     * <Pattern>
     * 
     * If is current choice:
     * -visual.highlight
     * -If confim:
     * --visual.noHighlight
     * --Perform Action
     * 
     * Otherwise, visual.noHighlight
     */


    @Override
    public final boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        if(isCurrentChoice)
        {
            visual.highlight(mouseIntersectsThis);
            return confirmCheck(mouseIntersectsThis);
        }
        else
        {
            visual.noHighlight();
            return false;
        }
    }

    /**Used to check if confirm is pressed.*/
    protected abstract boolean confirmCheck(boolean mouseIntersectsThis);

    @Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        //Render box.
        visual.render(screen, xOffset + position.x, yOffset + position.y);

        //shape.render(screen, 1.0f, xa, ya, false);
    }




    /*
     * Preset Builders.
     */
    
    /**Builds a Basic MenuChoice for a single player.*/
    public static MenuChoice_1P basic_1P(int x, int y, int width, int height, byte playerNum, Action action, Vector4f[] colors)
    {
        return new MenuChoice_1P
        (
            x, y,
            new AAB_Box2D
            (
                (width < 19) ? 19 : width,
                (height < 19) ? 19 : height
            ),
            playerNum, action, new Basic_ChoiceVisual(width, height, colors)
        );
    }

    /**Builds a Basic MenuChoice for Player One.*/
    public static MenuChoice_1P basic_1P(int x, int y, int width, int height, Action action, Vector4f[] colors)
    {return basic_1P(x, y, width, height, (byte)0, action, colors);}



    /**Builds a Sprite MenuChoice for a single player.*/
    public static MenuChoice_1P sprite_1P(int x, int y, Shape2D shape, byte playerNum, Action action, Sprite[] sprites)
    {
        return new MenuChoice_1P
        (
            x, y, shape, playerNum,
            action, new Sprite_ChoiceVisual(sprites)
        );
    }

    /**Builds a Sprite MenuChoice for Player One.*/
    public static MenuChoice_1P sprite_1P(int x, int y, Shape2D shape, Action action, Sprite[] sprites)
    {return sprite_1P(x, y, shape, (byte)0, action, sprites);}
}
