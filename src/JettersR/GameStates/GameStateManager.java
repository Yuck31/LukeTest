package JettersR.GameStates;
/**
 * This manages GameStates...
 * 
 * Author: Luke Sullivan
 * Last Edit: 8/25/2023
 */
import JettersR.Game;
import JettersR.Level;
import JettersR.Graphics.*;
//import JettersR.Audio.*;

public class GameStateManager
{
    //private Game game;
    //private AudioManager audioManager;

    private GameState currentGameState = null;

    public GameStateManager()
    {

    }

    private boolean started = false;
    public void start(Game game, GameState firstGameState)
    {
        //This function will only work ONCE
        if(started){return;}

        //Assign Game instance and AudioManager
        //this.game = game;
        //this.audioManager = game.getAudioManager();

        //Begin the game
        this.currentGameState = firstGameState;
        this.currentGameState.start();
    }

    private Object gameStateSet_Lock = new Object();

    public void setGameState(GameState state)
    {
        synchronized(gameStateSet_Lock)
        {
            //Old.
            currentGameState.end();
            currentGameState = null;

            //New.
            currentGameState = state;
            currentGameState.start();
        }
    }

    

    public void update()
    {
        currentGameState.update();
    }

    public void render(Screen screen)
    {
        //System.out.println("ren0");
        synchronized(gameStateSet_Lock){currentGameState.render(screen);}
    }
}
