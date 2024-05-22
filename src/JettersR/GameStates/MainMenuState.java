package JettersR.GameStates;
/**
 * State for the Main Menu
 * 
 * Author: Luke Sullivan
 * Last Edit: 8/30/2023
 */
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector4f;

import JettersR.Game;
import JettersR.Mouse;
import JettersR.Controller;
import JettersR.Graphics.*;
import JettersR.UI.Menus.MainMenu.MainMenu;
import JettersR.UI.Menus.TextBoxes.TextBox;
import JettersR.UI.ScrollBars.BasicScrollBar;
import JettersR.UI.Dialogue.DropDown_Menu;
import JettersR.UI.Menus.MouseMenu;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersR.Audio.*;

public class MainMenuState extends GameState
{
    Mouse mouse;

    //MainMenu mainMenu;
    SpriteSheet test = null;

    AudioBuffer select0 = null, select1 = null;
    SoundSource source = null;

    MouseMenu menu = new MouseMenu(0, 0, new AAB_Box2D(300, 300));
    Vector4f[] colors = {new Vector4f(0.5f, 0.5f, 0.5f, 1.0f), new Vector4f(0.8f, 0.8f, 0.8f, 1.0f)};

    BasicScrollBar scrollBar;
    //TextBox textBox;

    public MainMenuState(GameStateManager gameStateManager)
    {super(gameStateManager);}

    public void start()
    {
        mouse = Game.mouse;

        test = Sprites.global_UISheet("READY_BOMB");
        select0 = Audio.global_UISound("Select0");
        select1 = Audio.global_UISound("Select1");
        source = new SoundSource();
        //

        menu.addComponent
        (
            new DropDown_Menu(100, 100, 100, 18, colors, "Drop DOWN and SEEEE", 1.0f)
            .addOption("WOAH", () -> {})
            .addOption("uh", () -> {})
            .addOption("huh", () -> {})
            .addOption("wat?", () -> {})
            .addOption("AAAAH", () -> {})
            .addOption("This is an option.", () -> {})
            .addOption("THE END...", () -> {})
            //{System.out.println("is nigh");})
        );
        //.addComponent(new TextBox(300, 150, 100, 20, Fonts.get("Arial"), Controller.TYPING_LETTER,
        //1.0f, new Vector4f(0.2f, 0.2f, 0.2f, 1.0f), Screen.DEFAULT_BLEND));

        scrollBar = new BasicScrollBar(300, 100, 200, 8, 100, 50,
        false, 2, 10, 100,
        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), new Vector4f(0.75f, 0.75f, 0.75f, 1.0f), new Vector4f(0.2f, 0.2f, 0.2f, 1.0f));

        //textBox = new TextBox(150, 300, 125, 40, Fonts.get("Arial"), Controller.TYPING_ANY, 1.0f,
        //new Vector4f(0.2f, 0.2f, 0.2f, 1.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
    }

    @Override
    protected void end(){source.delete();}

    

    int i = 0, j = 0; Vector4f color = new Vector4f(1.0f, 0, 0, 1.0f);

    @Override
    public void update()
    {
        //Poll inputs here to reduce input lag slightly.
        glfwPollEvents();
        controller.update();

        //System.out.println("upd");
        //i = (i+1) % 255;
        //j=i;
        
        //color.y = (float)j/255f;
        //System.out.println(j);

        //if(Game.mouse.buttonPressed(GLFW_MOUSE_BUTTON_LEFT))
        //{
            //source.setPosition((float)Game.mouse.getX()-320, (float)Game.mouse.getY()-180, 0);
            //source.play(select0.getID(), select1.getID());
        //}

        //if(Game.controller.isKeyPressed(GLFW_KEY_0)){source.play(select1.getID());}

        menu.update(0, 0, true, true);

        scrollBar.update(0, 0, false, scrollBar.intersects(0, 0, mouse));//, false));

        //boolean ti = textBox.intersects(0, 0, mouse);//, false);
        //textBox.update(0, 0, ti, ti);

        mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
    }


    @Override
    public void render(Screen screen)
    {
        //System.out.println("ren1");
        //screen.renderSheet(3, 3, test, color, false);
        menu.render(screen, 0, 0);

        scrollBar.render(screen, 0, 0);

        //textBox.render(screen, 0, 0);

        //screen.fillRect(10, 250, 40, 50, color, true);
    }
}
