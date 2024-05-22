package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI;
/**
 * 
 */
import org.lwjgl.glfw.GLFW;

import JettersR.Game;
import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.UI.Menus.MenuComponent;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor_OLD;

public class LE_ObjectType_Button extends MenuComponent
{
    private LevelEditor_OLD levelEditor;

    public final Sprite t0, t1, e0, e1;
    //
    private Sprite sprite = null;
    
    /**Constuctor.*/
    public LE_ObjectType_Button(LevelEditor_OLD levelEditor, int x, int y)
    {
        super(x, y, new AAB_Box2D(16, 16, 0, 0));
        this.levelEditor = levelEditor;
        //
        t0 = new Sprite(LE_EditLevelUI.tileBar_Sprites, 81, 99, 16, 16);
        t1 = new Sprite(LE_EditLevelUI.tileBar_Sprites, 97, 99, 16, 16);
        e0 = new Sprite(LE_EditLevelUI.tileBar_Sprites, 81, 115, 16, 16);
        e1 = new Sprite(LE_EditLevelUI.tileBar_Sprites, 97, 115, 16, 16);
        //
        this.sprite = t0;
    }

    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        if(levelEditor.tilePlacement)
        {
            if(isCurrentChoice)
            {
                sprite = t1;
                //
                if(mouseIntersectsThis && Game.mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                {
                    levelEditor.tilePlacement = false;
                    sprite = e0;
                    return true;
                }
            }
            else{sprite = t0;}
        }
        else
        {
            if(isCurrentChoice)
            {
                sprite = e1;
                //
                if(mouseIntersectsThis && Game.mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                {
                    levelEditor.tilePlacement = true;
                    sprite = t0;
                    return true;
                }
            }
            else{sprite = e0;}
        }

        return false;
    }

    @Override
    public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
    {
        int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

        screen.renderSprite(xa, ya, sprite, Sprite.FLIP_NONE,
        //cropX0, cropY0, cropX1, cropY1,
        false);

        //shape.render(screen, 1.0f, xa, ya, false);
    }
}
