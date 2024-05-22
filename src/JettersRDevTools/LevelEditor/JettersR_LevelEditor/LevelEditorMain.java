package JettersRDevTools.LevelEditor.JettersR_LevelEditor;
/**
 * Startup class for the Level Editor.
 * 
 * Author: Luke Sullivan
 * Last Edit: 9/4/2023
 */
import JettersR.*;
//import JettersR.GameStates.GameStateManager;

public class LevelEditorMain
{
    public static final int WIDTH = 896, HEIGHT = (WIDTH / 16) * 9;
    private static String imageIconPath = "src/JettersRDevTools/LevelEditor/LE_ImageIcon.png";

    public static void main(String[] args)
    {
        //Game game = Game.instantiate(Main.SOFTWARE, Game.NORMAL_WIDTH, Game.NORMAL_HEIGHT);
        Game game = Game.instantiate
        (
            //Main.SOFTWARE,
            Main.OPENGL,
            WIDTH, HEIGHT
        );
        game.setImageIconPath(imageIconPath);
        game.start(WIDTH, HEIGHT, new LevelEditor(game.getGameStateManager()));
    }
}
