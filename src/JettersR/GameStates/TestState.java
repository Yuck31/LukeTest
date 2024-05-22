package JettersR.GameStates;
/**
 * Author: Luke Sullivan
 * Last Edit: 8/22/2023
 */
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.glfw.GLFW.*;

import java.io.File;
import java.io.IOException;

import JettersR.Controller;
import JettersR.Game;
import JettersR.Level;
//import JettersR.Audio.MusicSource;
import JettersR.Data.Profile;
import JettersR.Entities.Player;
import JettersR.Entities.Components.Lights.*;
import JettersR.Graphics.*;
import JettersR.UI.Visuals.Rect_DialogueBox;
import JettersR.UI.Visuals.SpeechBubble;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;
//import JettersR.UI.Menus.BasicMenuChoice;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor_OLD;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor;

import static JettersR.Util.Fixed.*;

public class TestState extends GameState
{
    Level level;
    Sprite[] tileSet_Sprites;

    Player player = null;
    Light aLight = null, dLight = null;

    private Rect_DialogueBox rect;
    private SpeechBubble speechBubble;

    //private MusicSource musicSource;

    public TestState(GameStateManager gameStateManager)
    {super(gameStateManager);}

    public void start()
    {
        
        player = new Player(fixed(32), fixed(128), fixed(32), new Profile(), 0, Player.PLAYER_TYPE_HUMAN);

        tileSet_Sprites = Sprites.loadTileSet_Sprites("GenericTiles");
        level = new Level(tileSet_Sprites);
        level.add(new Player(fixed(-64),fixed(64), fixed(32), new Profile(), 2, Player.PLAYER_TYPE_HUMAN));
        level.add(player);
        
        

        /*
        aLight = new AreaLight(fixed(0), fixed(0), fixed(0), new Vector3f(1.0f, 0.5f, 0.0f), new Vector3f(0.25f, 0.125f, 0.0f),
        160, 128, 48, fixed(96), (byte)0, (byte)0, (byte)0);

        dLight = new DirectionalLight(fixed(96), fixed(300), fixed(60), new Vector3f(0.0f, 0.5f, 1.0f), new Vector3f(0.0f, 0.25f, 0.5f),
        fixed(200), fixed(32), new fixedVector3(fixed(0), -fixed(1), -fixed(0)).normalize(),
        fixed(25), 23f);

        //level.addLight(dLight);
        //level.addLight(aLight);

        level.addLight
        (
            new AreaLight(fixed(232), fixed(32), fixed(32), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(0.5f, 0.5f, 0.0f),
            0, 0, 0, fixed(128), (byte)0, (byte)0, (byte)0)
        );
        */

        //level = new Level(new File(LevelEditor.editedLevelsPath + "/Test"));



        //Load Music File
        //musicSource = new MusicSource("Battle/Sup3Battle");
        //musicSource.play();

        rect = new Rect_DialogueBox
        (
            1, 0, 32, 64,
            new Vector4f(0.5f, 0.5f, 1.0f, 1.0f),
            new Vector4f(0.2f, 0.2f, 0.7f, 1.0f)
        );

        speechBubble = new SpeechBubble(70, 70, 64, 32, "\\c  \\~2.0,1.0 Uh...\\~  \n\\!1.0 WHAT!?\\.\nWAZZA.\\.");
        //speechBubble = new SpeechBubble(70, 70, 64, 32, "\\~ 2.0,1.0 Uh...\\~  \\!1.0 \nWHAT!?");
        /*
        menuChoice = new BasicMenuChoice
        (
            0, 0, 128, 64, null, 
            new Vector4f[]
            {
                new Vector4f(0.5f, 0.5f, 1.0f, 1.0f),
                new Vector4f(1.0f, 0.5f, 0.5f, 1.0f)
            },
            "...Uh", 1.0f
        );
        */
    }

    protected void end()
    {
        level.unload(this.gameStateManager);
        //tileSet_Sprites[0].getSheet().delete();
    }


    boolean paused = false;

    //float j = 0;
    @fixed int f_j = 0;
    public void update()
    {
        //Poll inputs here to reduce input lag slightly.
        glfwPollEvents();
        controller.update();

        //if(!paused){level.update();}
        
        //musicSource.loop.update();
        //
        if(!paused || controller.isKeyPressed(GLFW_KEY_LEFT_BRACKET))
        {
            if(controller.isKeyHeld(GLFW_KEY_KP_ADD)){level.addScale(0.1f, Game.NORMAL_WIDTH, Game.NORMAL_HEIGHT);}
            if(controller.isKeyHeld(GLFW_KEY_KP_SUBTRACT)){level.addScale(-0.1f, Game.NORMAL_WIDTH, Game.NORMAL_HEIGHT);}
            if(controller.isKeyHeld(GLFW_KEY_KP_ENTER)){level.setScale(1.0f, Game.NORMAL_WIDTH, Game.NORMAL_HEIGHT);}
            //
            level.update();
            //
            c.y = Math.abs((float)Math.sin(i += 0.05));
            c.z = c.y;
            //
            speechBubble.update(0, 0, false, false);
            //
            //light.setRadius((64 * (float)Math.abs(Math.sin(j += 0.01))));
            //dLight.f_setZ(fixed( 32 + (int)(64 * (float)Math.sin(j += 0.01)) ));

            //@fixed int f_co = f_sin(f_j -= 5);
            //dLight.f_setZ(fixed(32) + f_mul(fixed(32), f_co) );
            //f_print("ssin", f_co);
        }
        //else{level.unlockRenderThread();}

        if(controller.isKeyPressed(GLFW_KEY_ENTER))
        //if(controller.menu_InputPressed(0, Controller.menu_SPECIAL_0, false))
        {
            //Pause.
            paused = !paused;

            //System.out.println(paused);
        }
        if(controller.isKeyPressed(GLFW_KEY_1))
        {
            //Save Entities.
            try
            {
                LevelEditor.saveTileData(level, "Test");
                LevelEditor.saveTileKey(level, "Test", new Sprite[][]{tileSet_Sprites}, new String[]{"GenericTiles"}, new int[]{level.getNumTileMeshs()-1});
                LevelEditor.saveEntities(level, "Test");
                System.out.println("Saved");
            }
            catch(IOException e){e.printStackTrace();}
        }
        if(controller.isKeyPressed(GLFW_KEY_2))
        {
            //Unload level and change GameState to MainMenuState.
            gameStateManager.setGameState(new MainMenuState(gameStateManager));
            //gameStateManager.setGameState(new TestState(gameStateManager));
        }
    }

    //On each user.
    //-Poll that user's input.
    //-Send input as packet to other users.
    //-Update level.
    //

    Vector4f color = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);

    //Font arial = Fonts.get("Arial");
    //SpriteSheet ts = Sprites.global_EntitySheet("Bomber_Front");
    Vector4f c = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    float i = 0;

    public void render(Screen screen)
    {
        level.render(screen);
        //screen.drawLine(100, 10, 32, (int)player.getX(), (int)player.getY(), (int)player.getZ(), color, true);

        //rect.setSize((float)Math.sin(i * 0.25f), 180, 70);
        rect.render(screen, 0, 0);
        //enuChoice.render(screen, 100, 100);

        speechBubble.render(screen, 0, 0);

        //arial.render(screen, 100, 280, "gorsh! Does this work?\nOnly time will tell...", c, false);
        //screen.renderSheet(400, 150, 0, ts, false);

        //screen.drawRect(0, 0, 200, 64, color, false);
        //screen.renderSprite(0, 0, player.getSprite(), Sprite.FLIP_NONE, color, false);
    }
}
