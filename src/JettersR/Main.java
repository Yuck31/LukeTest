package JettersR;
/**
 * Initialization Class for the game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 1/3/2024
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public final class Main
{
    //private static final long serialVersionUID = 1L;
    public static final byte SOFTWARE = 0, OPENGL = 1, VULKAN = 2;

    public static void main(String[] args)
    {
        byte graphicsAPI;
        try
        {
            File file = new File("Data/GraphicsAPI.txt");
            Scanner scanner = new Scanner(file);
            graphicsAPI = (byte)Integer.parseInt(scanner.nextLine());
            scanner.close();
        }
        catch(FileNotFoundException e)
        {
            //e.printStackTrace();
            //graphicsAPI = SOFTWARE;
            graphicsAPI = OPENGL;

            //Make Default File
        }

        Game game = Game.instantiate(graphicsAPI, Game.NORMAL_WIDTH, Game.NORMAL_HEIGHT);
        game.start(new JettersR.GameStates.TestState(game.getGameStateManager()));
        //game.start();
    }
}
