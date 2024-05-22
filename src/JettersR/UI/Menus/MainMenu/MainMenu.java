package JettersR.UI.Menus.MainMenu;
/**
 * Main Menu class.
 * 
 * Author: Luke Sullivan
 * Last Edit: 11/8/2023
 */
import JettersR.Controller;
import JettersR.Graphics.Screen;
import JettersR.UI.Menus.Menu;
import JettersR.Util.Shapes.Shapes2D.Shape2D;

public class MainMenu extends Menu
{
    //Choice constants.
    public final byte
    CHOICE_STORY = 0,
    CHOICE_BATTLE = 1,
    CHOICE_PROFILES = 2,
    CHOICE_EXTRAS = 3,
    CHOICE_SETTINGS = 4,
    CHOICE_EXIT = 5;

    /**Constructor.*/
    public MainMenu(int x, int y, Shape2D shape)
    {
        //Set position and shape.
        super(x, y, shape);

        //Set Main Menu GameState.
        //this.state = state;

        //Construct UI Components in choice order.
        /*
        mainMenu = new MainMenuChoice[]
        {
            //STORY
            new MainMenuChoice(new Vector2d(18, 40), position,
                Sprites.mainMenu_StoryButton, 276, 146,
                "Description to be made"),
            //BATTLE
            new MainMenuChoice(new Vector2d(318, 40), position,
                Sprites.mainMenu_BattleButton, 276, 146,
                "Battle against other players and claim victory!"),
            //PROFILES
            new MainMenuChoice(new Vector2d(18, 198), position,
                Sprites.mainMenu_ProfilesButton, 182, 102,
                "Customize your own Bomber and Controller Settings."),
            //EXTRAS
            new MainMenuChoice(new Vector2d(215, 198), position,
                Sprites.mainMenu_ExtrasButton, 182, 102,
                "Description to be made"),
            //SETTINGS
            new MainMenuChoice(new Vector2d(412, 198), position,
                Sprites.mainMenu_SettingsButton, 182, 102,
                "Adjust Game Settings."),
            //EXIT
            new MainMenuChoice(new Vector2d(558, 278), position,
                Sprites.mainMenu_ExitButton, 44, 24,
                "Exit to Desktop."),
        };
        */
    }

    //
    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        switch(currentChoice)
        {
            case CHOICE_STORY:
            {
                //if(confirm(key))
                {
                    //state.exitingState = true;
                    /*
                    subMenu = SubMenu.STORY;
                    state.transition(state.STORY);
                    //choices = mainMenu_Story;
                     */
                }
                //else
                {                    
                    if(input_Pressed(Controller.menu_LEFT)
                    || input_Pressed(Controller.menu_RIGHT))
                    {currentChoice = CHOICE_BATTLE;}
                    //
                    if(input_Pressed(Controller.menu_UP)
                    || input_Pressed(Controller.menu_DOWN))
                    {currentChoice = CHOICE_PROFILES;}
                }
            }
            break;

            case CHOICE_BATTLE:
            {
                //if(confirm(key))
                {
                    //subMenu = SubMenu.BATTLE;
                    //state.transition(state.BATTLE);
                    //choices = mainMenu_Battle;
                    //
                    //currentChoice = LOCAL;
                    //choices[currentChoice].setSelected();
                }
                //else
                {
                    if(input_Pressed(Controller.menu_LEFT)
                    || input_Pressed(Controller.menu_RIGHT))
                    {currentChoice = CHOICE_STORY;}
                    //
                    if(input_Pressed(Controller.menu_UP)
                    || input_Pressed(Controller.menu_DOWN))
                    {currentChoice = CHOICE_SETTINGS;}
                }
            }
            break;

            case CHOICE_PROFILES:
            {
                //if(confirm(key))
                {
                    //subMenu = SubMenu.PROFILES;
                    //state.transition(state.PROFILES);
                    //profilesMenu.init();
                }
                //else
                {
                    if(input_Pressed(Controller.menu_LEFT))
                    {currentChoice = CHOICE_EXIT;}
                    //
                    if(input_Pressed(Controller.menu_RIGHT))
                    {currentChoice = CHOICE_EXTRAS;}
                    //
                    if(input_Pressed(Controller.menu_UP)
                    || input_Pressed(Controller.menu_DOWN))
                    {currentChoice = CHOICE_STORY;}
                }
            }
            break;

            case CHOICE_EXTRAS:
            {
                //if(confirm(key))
                {
                    //subMenu = SubMenu.EXTRAS;
                    //state.transition(state.EXTRAS);
                    //choices = mainMenu_Extras;
                    //currentChoice = ITEMTIPS;
                    //choices[currentChoice].setSelected();
                }
                //else
                {
                    if(input_Pressed(Controller.menu_LEFT))
                    {currentChoice = CHOICE_PROFILES;}
                    //
                    if(input_Pressed(Controller.menu_RIGHT))
                    {currentChoice = CHOICE_SETTINGS;}
                    //
                    if(input_Pressed(Controller.menu_UP))
                    {currentChoice = CHOICE_STORY;}
                    //
                    if(input_Pressed(Controller.menu_DOWN))
                    {currentChoice = CHOICE_BATTLE;}
                }
            }
            break;

            case CHOICE_SETTINGS:
            {
                //if(confirm(key))
                {
                    //subMenu = SubMenu.SETTINGS;
                    //state.transition(state.SETTINGS);
                    //choices = mainMenu_Settings;
                }
                //else
                {
                    if(input_Pressed(Controller.menu_LEFT))
                    {currentChoice = CHOICE_EXTRAS;}
                    //
                    if(input_Pressed(Controller.menu_RIGHT))
                    {currentChoice = CHOICE_EXIT;}
                    //
                    if(input_Pressed(Controller.menu_UP)
                    || input_Pressed(Controller.menu_DOWN))
                    {currentChoice = CHOICE_BATTLE;}
                }
            }
            break;

            case CHOICE_EXIT:
            {
                //if(confirm(key))
                {
                    //System.exit(0);
                }
                //else
                {
                    if(input_Pressed(Controller.menu_LEFT))
                    {currentChoice = CHOICE_SETTINGS;}
                    //
                    if(input_Pressed(Controller.menu_RIGHT))
                    {currentChoice = CHOICE_PROFILES;}
                    //
                    if(input_Pressed(Controller.menu_UP)
                    || input_Pressed(Controller.menu_DOWN))
                    {currentChoice = CHOICE_BATTLE;}
                }
            }
            break;
        }

        return false;
    }

    @Override
    public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
    {
        
    }


    /*
     * Profile select
     * 
     * -Check for vertical input from this card's playerNum.
     * --Mouse intersection
     * --Left click or confirm from this playerNum.
     * -Update scroll bar.
     * 
     * -Render Profile card.
     * -Render Profile character on top.
     * -if not ready: Render Name menu background.
     * --Render colored background behind selected name.
     * --Render name.
     * -else: Render "READY"
     */
}
