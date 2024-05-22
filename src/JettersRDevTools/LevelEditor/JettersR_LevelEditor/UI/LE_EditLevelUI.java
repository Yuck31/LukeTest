package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI;
/**
 * Author: Luke Sullivan
 * Last Edit: 7/13/2023
 */
import JettersR.Graphics.SpriteSheet;
import JettersR.UI.Menus.MouseMenu;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LE_Sprites;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor_OLD;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI.Inspector.LE_Inspector_OLD;

public class LE_EditLevelUI extends MouseMenu
{
    public static final SpriteSheet tileBar_Sprites = LE_Sprites.getSheet("TileBar_Sprites");
    
    //private Vector4f color0 = new Vector4f(0.2f, 0.7f, 0.2f, 1.0f),
    //color1 = new Vector4f(0.9f, 0.7f, 0.2f, 1.0f),
    //color2 = new Vector4f(0.9f, 0.2f, 0.2f, 1.0f);

    //Level Editor instance.
    //private LevelEditor levelEditor = null;

    //public TileSet_Menu tileSet_Menu;

    //public TileMesh_Frames tileSprite_Frames = null;

    //States
    public static final byte STATE_LEVEL = 0,
    STATE_TILESPRITE = 1;
    public byte state = STATE_LEVEL;

    /**
     * Constructor.
     * 
     * @param levelEditor LevelEditor instance.
    */
    public LE_EditLevelUI(LevelEditor_OLD levelEditor)
    {
        super(0, 0, null);
        //this.levelEditor = levelEditor;

        //TileBar.
        //addComponent(new LE_TileBar(levelEditor, 0, 442));

        //Tile/Entity Button.
        addComponent(new LE_ObjectType_Button(levelEditor, 50, 420));
        
        //Inspector.
        addComponent(new LE_Inspector_OLD(896-256, 0));
    }
}
