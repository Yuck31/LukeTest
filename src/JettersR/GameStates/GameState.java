package JettersR.GameStates;
/**
 * Class meant to represent a State in the game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 8/30/2023
 */
import JettersR.Controller;
import JettersR.Game;
import JettersR.Graphics.Screen;

public abstract class GameState
{
    //Pointer to GameStateManager.
    protected final GameStateManager gameStateManager;

    protected transient Controller controller = Game.controller;

    /**
     * Constructor.
     * 
     * For memory reasons: DO NOT CONSTRUCT LEVEL IN HERE. DO IT IN start().
     */
    public GameState(GameStateManager gameStateManager)
    {
        this.gameStateManager = gameStateManager;
    }

    public abstract void start();
    protected abstract void end();

    public abstract void update();
    public abstract void render(Screen screen);
}
