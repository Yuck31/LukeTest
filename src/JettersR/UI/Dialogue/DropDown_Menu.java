package JettersR.UI.Dialogue;
/**
 * 
 */
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import JettersR.Controller;
import JettersR.Game;
import JettersR.Mouse;
import JettersR.Graphics.Screen;
import JettersR.UI.Menus.ListMenu;
import JettersR.UI.Menus.MenuChoices.Labeled_MenuChoice;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

public class DropDown_Menu extends Labeled_MenuChoice
{
    //ListMenu.
    private ListMenu subMenu;

    //Shape representing subMenu region.
    private AAB_Box2D subMenuBox;

    //Dropdown timer.
    private boolean droppedDown = false;
    private byte putUpTime = 30;

    //
    //private Font arial = Fonts.get("Arial");

    /**Constructor.*/
    public DropDown_Menu(int x, int y, int width, int height, Vector4f[] colors, String text, float textScale)
    {
        super(x, y, width, height, null, colors, text, textScale);

        //Set action.
        this.action = this::dropDown;

        //Create subMenu.
        int maxElements = 3;
        this.subMenu = new ListMenu(0, 0, width, height, maxElements);

        //Create subMenu region collision.
        int boxHeight = height * maxElements;
        this.subMenuBox = new AAB_Box2D(width + 8, boxHeight, -(width/2), (boxHeight/2) - height);
    }

    private void dropDown()
    {
        if(!droppedDown){droppedDown = true;}
        else
        {
            subMenu.setElementOffset(0);
            droppedDown = false;
        }
    }


    public DropDown_Menu addOption(String text, Action action)
    {
        //subMenu.addOption(text, action, colors);
        //
        return this;
    }

    //TODO: Change how drop-downs work.
    /*
    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        if(!droppedDown)
        {
            //Just update the dropDown component.
            return super.update(xOffset, yOffset, isCurrentChoice, mouseIntersectsThis);
        }
        else
        {
            //Update dropDown component.
            super.update(xOffset, yOffset, isCurrentChoice, mouseIntersectsThis);

            //Update list menu. (Last two parameters don't matter)
            subMenu.update(position.x + xOffset, position.y + yOffset, false, false);

            Mouse mouse = Game.mouse;

            //If none of the list menu options are being selected.
            if(!isCurrentChoice && !subMenuBox.intersects(mouse.getX(), mouse.getY(), position.x + xOffset, position.y + yOffset))
            {
                //Collapse after some time.
                putUpTime--;

                if(putUpTime <= 0 || mouse.getButtonState(GLFW.GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_RELEASED)
                {
                    droppedDown = false;
                    //
                    subMenu.setElementOffset(0);
                    putUpTime = 30;
                }
            }
            //Otherwise, keep the list down.
            else
            {
                putUpTime = 30;
                return true;
            }
        }
        //
        return false;
    }
    */

    Vector4f gb = new Vector4f(0.0f, 1.0f, 1.0f, 1.0f);

    @Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        int xa = (int)(this.position.x + xOffset),
        ya = (int)(this.position.y + yOffset);

        //Set crop region for base choice.
        screen.setCropRegion
        (
            xa + shape.left(), ya + shape.up(),
            xa + shape.right(), ya + shape.down()
        );

        //Render base choice.
        super.render(screen, xOffset, yOffset);
        shape.render(screen, 1.0f, position.x + xOffset, position.y + yOffset, false);

        //Render sub-Menu if it is dropped down.
        if(droppedDown){subMenu.render(screen, position.x + xOffset, position.y + yOffset);}

        //subMenuBox.render(screen, 1.0f, position.x, position.y, false);

        screen.drawPoint((int)(position.x + xOffset), (int)(position.y + yOffset), gb, false);
    }
}
