package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/19/2023
 */
import static org.lwjgl.glfw.GLFW.*;

import java.io.File;

import org.joml.Vector4f;

import JettersR.Controller;
import JettersR.Game;
import JettersR.Level;
import JettersR.Mouse;
import JettersR.Graphics.*;
import JettersR.Tiles.*;
import JettersR.Tiles.Graphics.TileMesh;
import JettersR.UI.Menus.Menu;
import JettersR.UI.Menus.MenuComponent;
import JettersR.UI.Menus.MenuChoices.MenuChoice;
import JettersR.UI.Menus.MenuChoices.MenuChoice_1P;
import JettersR.UI.ScrollBars.BasicScrollBar;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LE_Sprites;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditorMain;

public class LE_TileBar extends Menu
{
    //Controller and Mouse for input.
    private Controller controller = Game.controller;
    private Mouse mouse = Game.mouse;

    public final Sprite tileSet_Bar_Sprite,
    addNewTileSprite_0, addNewTileSprite_1;

    //Fonts for text.
    private Font font, arial;

    //Level Editor instance.
    LevelEditor levelEditor = null;

    //Extend button data.
    public static final int EXTEND_BUTTON_HEIGHT = 18;
    private static final AAB_Box2D EXTEND_BUTTON_BOX = new AAB_Box2D(230, 18, 205, 0);
    public static final int NONEXTENDED_HEIGHT = 70, EXTENDED_HEIGHT = LevelEditorMain.HEIGHT - EXTEND_BUTTON_HEIGHT;

    //Extend button.
    private MenuChoice_1P extendButton;

    //Scroll Bar.
    private BasicScrollBar barScrollBar;


    /**Constructor.*/
    public LE_TileBar(LevelEditor levelEditor)
    {
        super(0, LevelEditorMain.HEIGHT - NONEXTENDED_HEIGHT, new AAB_Box2D(Game.NORMAL_WIDTH, NONEXTENDED_HEIGHT, 0, 0));
        this.levelEditor = levelEditor;

        Sprite[] tileBar_Sprites = LE_Sprites.getSheet("TileBar_Sprites").loadLayout(new File(LE_Sprites.sheetLayoutsPath + "TileBar_Sprites_LYT.dat"));

        //Set SpriteSheets.
        tileSet_Bar_Sprite = tileBar_Sprites[2];
        addNewTileSprite_0 = tileBar_Sprites[3];
        addNewTileSprite_1 = tileBar_Sprites[4];

        //Get font.
        font = Fonts.get("Debug");
        arial = Fonts.get("Arial");


        //Build Extend Button.
        extendButton = MenuChoice.sprite_1P(0, -EXTEND_BUTTON_HEIGHT, EXTEND_BUTTON_BOX, this::toggleExtend, new Sprite[]{tileBar_Sprites[0], tileBar_Sprites[1]});

        //Create scroll bar.
        barScrollBar = new BasicScrollBar
        (
            Game.NORMAL_WIDTH + 4, EXTENDED_HEIGHT >> 1, 8, LevelEditorMain.HEIGHT - EXTEND_BUTTON_HEIGHT, shape.getWidth(), shape.getWidth() >> 1, true,
            1, EXTENDED_HEIGHT, 1 * TILESET_BAR_HEIGHT, LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR
        );
    }

    public void addTileSet(LevelEditor.TileSet tileSet)
    {
        menuComponents.add(new TileSet_Bar(0, menuComponents.size() * TILESET_BAR_HEIGHT, tileSet));
    }


    /**TileBar update function.*/
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        //Calculate offsets.
        int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

        //Update extend button.
        boolean exInts = extendButton.intersects(xa, ya, mouse);
        if(extendButton.update(xa, ya, exInts, exInts)){return true;}


        //If extended.
        if(levelEditor.uiLockMode == LevelEditor.UILOCK_TILESETS && isCurrentChoice)
        {
            //
            //Up/Down input check.
            //
            boolean up = controller.menu_InputPressed(0, Controller.menu_UP, true),
            down = controller.menu_InputPressed(0, Controller.menu_DOWN, true);

            //Up
            if(up && !down)
            {
                currentChoice = ((currentChoice-1) + (levelEditor.tileSets.size()+1)) % (levelEditor.tileSets.size()+1);//Sets + 1 for Add New TileSet button.
                int currentScroll = (currentChoice * TILESET_BAR_HEIGHT);

                if(currentScroll < barScrollBar.getLengthOffset()){barScrollBar.setLengthOffset(currentScroll);}
                else if(currentScroll + TILESET_BAR_HEIGHT > barScrollBar.getLengthOffset() + barScrollBar.getVisableLength())
                {barScrollBar.setLengthOffset((currentScroll + TILESET_BAR_HEIGHT) - barScrollBar.getVisableLength());}

                //If holding delete, reset delete timer.
                deleteTimer = MAX_DELETE_TIMER;
            }
            //Down
            else if(down && !up)
            {
                currentChoice = (currentChoice+1) % (levelEditor.tileSets.size()+1);
                int currentScroll = (currentChoice * TILESET_BAR_HEIGHT);
                
                if(currentScroll < barScrollBar.getLengthOffset()){barScrollBar.setLengthOffset(currentScroll);}
                else if(currentScroll + TILESET_BAR_HEIGHT > barScrollBar.getLengthOffset() + barScrollBar.getVisableLength())
                {barScrollBar.setLengthOffset((currentScroll + TILESET_BAR_HEIGHT) - barScrollBar.getVisableLength());}

                //If holding delete, reset delete timer.
                deleteTimer = MAX_DELETE_TIMER;
            }


            //
            //Check for selection with the TileSets in view.
            //
            int first = barScrollBar.getLengthOffset(), last = first + (barScrollBar.getVisableLength());
            if(last > levelEditor.tileSets.size()){last = levelEditor.tileSets.size();}

            for(int i = first; i <= last; i++)
            {
                int ia = (i * TILESET_BAR_BOX.getHeight()) - barScrollBar.getLengthOffset();

                //Collision check with box.
                boolean mouseIntersects = false;
                if(TILESET_BAR_BOX.intersects(mouse.getX(), mouse.getY(), xa, ya + ia))
                {
                    mouseIntersects = true;
                    if(Game.mouse.isMoving()){currentChoice = i;}
                }


                //TileSet bar.
                if(i < levelEditor.tileSets.size())
                {
                    //Update the current TileSet_Bar. If confirmed while updating...
                    if(menuComponents.get(i).update(xa, ya + ia, currentChoice == i, false))
                    {
                        //Just don't update the rest of this menu.
                        return true;
                    }
                }
                //Add New TileSet.
                else if(input_Confirm_Pressed(0, mouseIntersects))
                {
                    //Show menu of available TileSets.
                    if(levelEditor.fillLoadMenu(Tiles.TILESETS_PATH))
                    {
                        levelEditor.previousState = LevelEditor.STATE_EDITLEVEL;
                        levelEditor.currentState = LevelEditor.STATE_LOADTILESET_IMGLYT;
                        mouse.resetScroll();
                    }
                    else{System.err.println("No TileSet sheets or Layouts to load.");}
                }
            }

            //
            //Scroll Bar Check.
            //
            if(levelEditor.tileSets.size() > 6)
            {barScrollBar.update(xa, ya, false, barScrollBar.intersects(xa, ya, mouse));}

            //Cancel check.
            if(controller.menu_InputPressed(0, Controller.menu_RIGHT_TAB, true))// || mouse.buttonPressed(GLFW_MOUSE_BUTTON_RIGHT))
            {
                //UnExtend the TileBar.
                toggleExtend();
                levelEditor.uiLockMode = LevelEditor.UILOCK_NONE;
            }
        }
        //If not extended.
        else
        {
            //Just update the currently selected TileSet bar.
            MenuComponent m = menuComponents.get(currentChoice);
            m.update(xa, ya - m.getY(), isCurrentChoice, mouseIntersectsThis);
        }

        return false;
    }

    /**Extends the TileBar if it isn't and vise-versa.*/
    public void toggleExtend()
    {
        //if(!extended)
        if(levelEditor.uiLockMode != LevelEditor.UILOCK_TILESETS)
        {
            //extended = true;
            levelEditor.uiLockMode = LevelEditor.UILOCK_TILESETS;

            position.y = LevelEditorMain.HEIGHT - EXTENDED_HEIGHT;

            AAB_Box2D box = (AAB_Box2D)shape;
            box.setWidth(Game.NORMAL_WIDTH + barScrollBar.getBarWidth());
            box.setHeight(EXTENDED_HEIGHT);
        }
        else
        {
            //extended = false;
            levelEditor.uiLockMode = LevelEditor.UILOCK_NONE;

            position.y = LevelEditorMain.HEIGHT - NONEXTENDED_HEIGHT;

            AAB_Box2D box = (AAB_Box2D)shape;
            box.setWidth(Game.NORMAL_WIDTH);
            box.setHeight(NONEXTENDED_HEIGHT);
        }
    }


    private static final Vector4f highlight = new Vector4f(1.0f, 1.0f, 1.0f, 0.25f),
    deleteHighlight = new Vector4f(1.0f, 0.0f, 0.0f, 0.25f);

    @Override
    /**TileBar render function.*/
    public void render(Screen screen, float xOffset, float yOffset)
    {
        //Calculate offsets.
        int xa = (int)(this.position.x + xOffset), ya = (int)(this.position.y + yOffset);

        //Render the extend button.
        extendButton.render(screen, xa, ya);



        //if(extended)
        if(levelEditor.uiLockMode == LevelEditor.UILOCK_TILESETS)
        {
            //Render Background.
            screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), LevelEditor.SCROLL_BAR_COLOR, false);

            //Render scroll bar.
            if(levelEditor.tileSets.size() > 6){barScrollBar.render(screen, xOffset, yOffset);}

            //Set crop region.
            screen.setCropRegion(xa, ya, xa + TILESET_BAR_BOX.getWidth(), ya + EXTENDED_HEIGHT);


            //
            //Render the currently visable TileSets.
            //
            int first = barScrollBar.getLengthOffset() / TILESET_BAR_HEIGHT,
            last = first + (barScrollBar.getVisableLength() / TILESET_BAR_HEIGHT);
            if(last > levelEditor.tileSets.size()){last = levelEditor.tileSets.size();}

            for(int i = first; i <= last; i++)
            {
                int ia = ((i * TILESET_BAR_HEIGHT) - barScrollBar.getLengthOffset());

                //Render TileSet.
                if(i < levelEditor.tileSets.size())
                {
                    MenuComponent m = menuComponents.get(i);

                    //Render the currrent TileSet bar.
                    m.render(screen, xa, ya - barScrollBar.getLengthOffset());
                }
                //Render new TileSet button.
                else
                {
                    //Render a rect.
                    screen.fillRect
                    (
                        xa, ya + ia,
                        TILESET_BAR_BOX.getWidth(), TILESET_BAR_BOX.getHeight(),
                        LevelEditor.SCROLL_HANDLE_COLOR_0, false
                    );

                    //Render text over it.
                    arial.render(screen, xa + 160, ya + ia + 16, "Create new TileSet.", 2.0f, false);
                }

                //If this bar is the currentChoice, highlight it.
                if(currentChoice == i)
                {
                    screen.fillRect
                    (
                        xa, ya + ia,
                        TILESET_BAR_BOX.getWidth(), TILESET_BAR_BOX.getHeight(),
                        highlight, false
                    );
                }
            }

            //Render scroll bar.
            if(levelEditor.tileSets.size() > 6){barScrollBar.render(screen, xa, ya);}
        }
        else
        {
            //Set crop region to within the bar.
            //screen.setCropRegion(xa, ya, xa + shape.getWidth(), ya + shape.getHeight());

            //Only render the currently selected TileSet bar.
            MenuComponent m = menuComponents.get(currentChoice);
            m.render(screen, xa, ya - m.getY());
        }

        //shape.render(screen, 1.0f, xa, ya, false);

        //Reset crop region.
        screen.resetCropRegion();
    }






    /*
     * Components used by the TileBar.
     */

    public static final int TILESET_BAR_HEIGHT = 70;
    private static final AAB_Box2D TILESET_BAR_BOX = new AAB_Box2D(Game.NORMAL_WIDTH, TILESET_BAR_HEIGHT, 0, 0),
    TILESPRITE_BOX = new AAB_Box2D(34, 62, 0, 0);
    public static final int TILESPRITE_STARTX = 5,
    TILESPRITE_INC = TILESPRITE_BOX.getWidth()+1;

    //Delete Timer values.
    public static int MAX_DELETE_TIMER = 60;
    private int deleteTimer = MAX_DELETE_TIMER;

    /**A bar component for singular TileSets.*/
    private class TileSet_Bar extends MenuComponent
    {
        //TileSet to get TileSprites from.
        private LevelEditor.TileSet tileSet;

        

        //Scroll Bar.
        private BasicScrollBar setScrollBar;

        //Current choice.
        private int currentTileSpriteNum = 0;

        /**Constructor.*/
        public TileSet_Bar(int x, int y, LevelEditor.TileSet tileSet)
        {
            super(x, y, TILESET_BAR_BOX);

            //Set TileSet.
            this.tileSet = tileSet;

            //Create scroll bar.
            setScrollBar = new BasicScrollBar
            (
                Game.NORMAL_WIDTH >> 1, 66, Game.NORMAL_WIDTH, 8, TILESET_BAR_HEIGHT-6, TILESET_BAR_HEIGHT-6,
                false, 1, 18, tileSet.getCount() + 1,
                LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR
            );
        }

        @Override
        /**Update function for TileSet_Bar.*/
        public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
        {
            //Calculate offsets.
            int xa = (int)(this.position.x + xOffset), ya = (int)(this.position.y + yOffset);


            //
            //Button input checks.
            //
            if(isCurrentChoice)//If either Left-Bumper is held or TileBar is extended.
            {
                //
                //Left/Right input check.
                //
                boolean left = controller.menu_InputPressed(0, Controller.menu_LEFT, true),
                right = controller.menu_InputPressed(0, Controller.menu_RIGHT, true);

                //Left
                if(left && !right)
                {
                    //Go left.
                    currentTileSpriteNum = ((currentTileSpriteNum-1) + (tileSet.getCount()+1)) % (tileSet.getCount()+1);

                    if(currentTileSpriteNum < setScrollBar.getLengthOffset()){setScrollBar.setLengthOffset(currentTileSpriteNum);}
                    else if(currentTileSpriteNum + 1 > setScrollBar.getLengthOffset() + setScrollBar.getVisableLength())
                    {setScrollBar.setLengthOffset((currentTileSpriteNum + 1) - setScrollBar.getVisableLength());}

                    //If holding delete, reset delete timer.
                    deleteTimer = MAX_DELETE_TIMER;
                }
                //Right
                else if(right && !left)
                {
                    //Go right.
                    currentTileSpriteNum = (currentTileSpriteNum+1) % (tileSet.getCount()+1);
                    
                    if(currentTileSpriteNum < setScrollBar.getLengthOffset()){setScrollBar.setLengthOffset(currentTileSpriteNum);}
                    else if(currentTileSpriteNum + 1 > setScrollBar.getLengthOffset() + setScrollBar.getVisableLength())
                    {setScrollBar.setLengthOffset((currentTileSpriteNum + 1) - setScrollBar.getVisableLength());}

                    //If holding delete, reset delete timer.
                    deleteTimer = MAX_DELETE_TIMER;
                }
            }


            //
            //Check for selection with the TileSprites in view.
            //
            int first = setScrollBar.getLengthOffset(), last = first + setScrollBar.getVisableLength();
            if(last > tileSet.getCount()){last = tileSet.getCount();}

            for(int i = first; i <= last; i++)
            {
                int ia = TILESPRITE_STARTX + ((i - setScrollBar.getLengthOffset()) * TILESPRITE_INC);

                //Collision check with box.
                boolean mouseIntersects = false;
                if(TILESPRITE_BOX.intersects(mouse.getX(), mouse.getY(), xa + ia, ya))
                {
                    mouseIntersects = true;
                    if(Game.mouse.isMoving()){currentTileSpriteNum = i;}
                }

                
                if(currentTileSpriteNum == i)
                {
                    //TileSprite.
                    if(i < tileSet.getCount())
                    {
                        //A or Left Click: Confirm 0 input check.
                        if((isCurrentChoice && controller.menu_InputPressed(0, Controller.menu_CONFIRM, true))
                        || (mouseIntersects && mouse.getButtonState(GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_RELEASED))
                        {
                            //Set slot 0 to current TileSprite.
                            levelEditor.pickedTiles[0] = (levelEditor.pickedTiles[0] & Tiles.TILE_PROPERTIES_PORTION) | ((tileSet.tileMeshOffset + i+1) << Tiles.TILE_PROPERTIES_BITS);

                            //Don't update the rest of the TileBar.
                            return true;
                        }
                        //B or Right Click: Confirm 1 input check.
                        else if((isCurrentChoice && controller.menu_InputPressed(0, Controller.menu_CANCEL, true))
                        || (mouseIntersects && mouse.getButtonState(GLFW_MOUSE_BUTTON_RIGHT) == Controller.STATE_RELEASED))
                        {
                            //Set slot 1 to current TileSprite.
                            levelEditor.pickedTiles[1] = (levelEditor.pickedTiles[1] & Tiles.TILE_PROPERTIES_PORTION) | ((tileSet.tileMeshOffset + i+1) << Tiles.TILE_PROPERTIES_BITS);
                            //Plus 1 because of VoidTiles.

                            //Don't update the rest of the TileBar.
                            return true;
                        }
                        //Only do these checks if the TileBar is extended.
                        else if(levelEditor.uiLockMode == LevelEditor.UILOCK_TILESETS)
                        {
                            //Edit: X input check.
                            if(controller.menu_InputPressed(0, Controller.menu_SPECIAL_0, true))
                            {
                                //Open TileCreator with this TileSprite.
                                levelEditor.openTileCreator(tileSet.tileMeshOffset + i + 1);

                                //Don't update the rest of the TileBar.
                                return true;
                            }
                            //Delete: Y input check.
                            else if(controller.menu_InputHeld(0, Controller.menu_SPECIAL_1))
                            {
                                //Decrement delete timer.
                                deleteTimer--;

                                //If timer reaches zero.
                                if(deleteTimer <= 0)
                                {
                                    //Decrease scroll bar.
                                    setScrollBar.setTotalLength(setScrollBar.getTotalLength()-1);

                                    //Delete TileSprite and reset timer.
                                    levelEditor.removeTileSprite(tileSet.tileMeshOffset + i + 1, tileSet);
                                    deleteTimer = MAX_DELETE_TIMER;
                                }

                                //Don't update the rest of the TileBar.
                                return true;
                                
                            }
                            //Otherwise, reset delete timer.
                            else{deleteTimer = MAX_DELETE_TIMER;}
                        }
                    }
                    //Add new TileSprite button. A or Left Click.
                    else if(controller.menu_InputPressed(0, Controller.menu_CONFIRM, true)
                    || (mouseIntersects && mouse.getButtonState(GLFW_MOUSE_BUTTON_LEFT) == Controller.STATE_RELEASED))
                    {
                        TileMesh newTileSprite = new TileMesh
                        (
                            new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                            tileSet.getSprite(0), tileSet.getSprite(0),
                            new byte[]{0, 0, 0}, TileMesh.SHEARTYPE_FLOOR, 0.0f, 1.0f, Material.NULL_MATERIAL
                        );
                        levelEditor.addTileSprite(newTileSprite, tileSet);


                        //Calculate scroll bar's length.
                        setScrollBar.setTotalLength(setScrollBar.getTotalLength()+1);

                        //Open the TileSprite editor to let the user configure it.
                        levelEditor.tileCreator.setTileMesh(this.tileSet.getSprites(), newTileSprite);
                        levelEditor.currentState = LevelEditor.STATE_TILEMESH_EDITOR;

                        //Don't update the rest of the TileBar.
                        return true;
                    }
                }
            }

            
            //
            //Scroll bar check.
            //
            if(tileSet.getCount() > 17)
            {setScrollBar.update(xa, ya, false, setScrollBar.intersects(xa, ya, mouse));}

            return false;
        }

        @Override
        public void render(Screen screen, float xOffset, float yOffset)
        {
            //Calculate offsets.
            int xa = (int)(this.position.x + xOffset), ya = (int)(this.position.y + yOffset);

            //Render Background.
            screen.renderSprite(xa, ya, tileSet_Bar_Sprite, Sprite.FLIP_NONE, false);

            //Get level for info to be used.
            Level level = levelEditor.getLevel();

            //From current scroll to scroll extent, render TileSprites.
            int first = setScrollBar.getLengthOffset(), last = first + setScrollBar.getVisableLength()-1;
            if(last > tileSet.getCount()){last = tileSet.getCount();}//Set to max because last option is to add a TileSprite.

            for(int i = first; i <= last; i++)
            {
                int ia = TILESPRITE_STARTX + ((i - setScrollBar.getLengthOffset()) * TILESPRITE_INC);

                if(i < tileSet.getCount())
                {
                    //Render the TileSprite here.
                    level.getTileMeshs(tileSet.tileMeshOffset + i + 1).render_2D(screen, xa + ia + 1, ya+6);
                }
                else
                {
                    //Render add new TileSprite sprite.
                    screen.renderSprite(xa + ia, ya, addNewTileSprite_0, Sprite.FLIP_NONE, false);
                }
                
                

                //Highlight if currentChoice.
                if(currentTileSpriteNum == i)
                {
                    //If not deleting.
                    if(deleteTimer >= MAX_DELETE_TIMER)
                    {
                        screen.fillRect
                        (
                            xa + ia, ya,
                            TILESPRITE_BOX.getWidth(), TILESPRITE_BOX.getHeight(),
                            highlight, false
                        );
                    }
                    //If deleting.
                    else
                    {
                        int deleteHeight = (int)(TILESPRITE_BOX.getHeight() * ((-deleteTimer + MAX_DELETE_TIMER) / (float)MAX_DELETE_TIMER));

                        screen.fillRect
                        (
                            xa + ia, (ya + TILESPRITE_BOX.getHeight()) - deleteHeight,
                            TILESPRITE_BOX.getWidth(), deleteHeight,
                            deleteHighlight, false
                        );
                    }
                }

                //Render its ID above it.
                font.render(screen, xa + ia + 15, ya+1, Integer.toString(i+1), false);
            }

            //shape.render(screen, 1.0f, xa, ya, false);

            //Render scroll bar.
            if(tileSet.getCount() > 17){setScrollBar.render(screen, xa, ya);}
        }
    }
}
