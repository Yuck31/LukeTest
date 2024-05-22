package JettersR.UI.Menus;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Game;
import JettersR.Mouse;
import JettersR.Graphics.Screen;
import JettersR.UI.Menus.MenuChoices.Labeled_MenuChoice;
import JettersR.UI.Menus.MenuChoices.MenuChoice.Action;
import JettersR.UI.ScrollBars.BasicScrollBar;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

public class ListMenu extends Menu
{
    //ScrollBar. Contains offset.
    private BasicScrollBar scrollBar;

    //Element dimensions.
    public int elementWidth, elementHeight;

    /**Constructor.*/
    public ListMenu(int x, int y, int elementWidth, int elementHeight, int visableElements)
    {
        super(x, y, new AAB_Box2D(elementWidth, elementHeight));

        //Set element width and height.
        this.elementWidth = elementWidth;
        this.elementHeight = elementHeight;

        //Create scroll bar.
        this.scrollBar = new BasicScrollBar
        (
            (int)((elementWidth * 0.5f) + 4),
            elementHeight * 2,
            8, (elementHeight * visableElements),
            elementWidth, (int)((elementWidth * 0.5f)),
            true, 1, visableElements, numComponents(),
            //elementHeight >> 1, visableElements * elementHeight, numComponents() * elementHeight,
            new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), new Vector4f(0.75f, 0.75f, 0.75f, 1.0f), new Vector4f(0.2f, 0.2f, 0.2f, 1.0f)
        );
    }

    public ListMenu addOption(String text, Action action, Vector4f[] colors)
    {
        scrollBar.setTotalLength(numComponents()+1);
        //scrollBar.setTotalLength((numComponents()+1) * elementHeight);

        addComponent
        (
            new Labeled_MenuChoice
            (
                0, (elementHeight * (numComponents()+1)),
                elementWidth, elementHeight, action, colors,
                text, 1.0f
            )
        );

        return this;
    }

    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        //Get mouse.
        Mouse mouse = Game.mouse;

        //Update scroll bar.
        scrollBar.update(xOffset, yOffset, false, scrollBar.intersects(xOffset, yOffset, mouse));

        //The last element that can be seen.
        int offsetToElement = scrollBar.getLengthOffset() + scrollBar.getVisableLength(),
        //int offsetToElement = (scrollBar.getLengthOffset() + scrollBar.getVisableLength()) / elementHeight,
        limit = (offsetToElement >= menuComponents.size()) ? menuComponents.size() : offsetToElement;

        //From offset to the last visable element.
        for(int i = scrollBar.getLengthOffset(); i < limit; i++)
        //for(int i = scrollBar.getLengthOffset() / elementHeight; i < limit; i++)
        {
            //Cache it.
            MenuComponent m = menuComponents.get(i);
            boolean mouseIntersects = false;

            //Mouse Check.
            if(m.intersects
            (
                this.position.x + xOffset,
                this.position.y + yOffset - (scrollBar.getLengthOffset() * elementHeight),
                //this.position.y + yOffset - scrollBar.getLengthOffset(),
                mouse
            ))
            {
                //currentChoice = i;
                mouseIntersects = true;
            }

            //Update Menu Choice.
            if(m.update(xOffset, yOffset, mouseIntersects, mouseIntersects)){return true;}
        }

        //Nothing was confirmed.
        return false;
    }

    //Element offset Getter/Setter.
    public int getElementOffset(int elementOffset){return scrollBar.getLengthOffset();}
    public void setElementOffset(int elementOffset){scrollBar.setLengthOffset(elementOffset);}


    @Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        //The last element that can be seen.
        int offsetToElement = scrollBar.getLengthOffset() + scrollBar.getVisableLength(),
        //int offsetToElement = (scrollBar.getLengthOffset() + scrollBar.getVisableLength()) / elementHeight,
        limit = (offsetToElement >= menuComponents.size()) ? menuComponents.size() : offsetToElement;

        int xa = (int)(position.x + xOffset),
        ya = (int)(position.y + yOffset);

        //Crop content to the region of the list menu.
        screen.setCropRegion
        (
            xa + shape.left(), ya + shape.up() + elementHeight,
            xa + shape.left() + elementWidth, ya + shape.up() + elementHeight + (menuComponents.size() * elementHeight)
        );

        //From offset to the last visable element.
        for(int i = scrollBar.getLengthOffset(); i < limit; i++)
        //for(int i = scrollBar.getLengthOffset() / elementHeight; i < limit; i++)
        {
            menuComponents.get(i).render(screen, xa, ya - (scrollBar.getLengthOffset() * elementHeight));
            //menuComponents.get(i).render(screen, xa, ya - scrollBar.getLengthOffset());
        }

        //Render scroll bar.
        scrollBar.render(screen, xa, ya);
    }
}
