package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI;
/**
 * Author: Luke Sullivan
 * Last Edit: 3/9/2024
 */
import java.util.List;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector4f;

import JettersR.Game;
import JettersR.Controller;
import JettersR.Mouse;
import JettersR.Graphics.*;
import JettersR.Tiles.Graphics.TileMesh;
import JettersR.UI.Menus.Menu;
import JettersR.UI.Menus.MenuComponent;
import JettersR.UI.Menus.MenuChoices.Labeled_MenuChoice;
import JettersR.UI.Menus.TextBoxes.Rect_TextBox;
import JettersR.UI.ScrollBars.BasicScrollBar;
import JettersR.UI.Visuals.Rect_DialogueBox;
import JettersR.UI.Menus.TextBoxes.Labeled_TextBox;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditorMain;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor;

public class LE_TileCreator extends Menu
{
    //Current Choice constants.
    private static final byte
    CHOICE_TILESET_SPRITE_COLUMN = 0,
    CHOICE_TILEMESH_SPRITE_COLUMN = 1,
    CHOICE_A = 2,
    CHOICE_R = 3,
    CHOICE_G = 4,
    CHOICE_B = 5,
    CHOICE_MATERIAL = 6,//TODO Materials Button
    CHOICE_EMISSION = 7,
    CHOICE_TILEMESH_SIDES = 8,
    CHOICE_DONE = 9;


    //Controller and mouse pointers.
    private Controller controller;
    private Mouse mouse;

    //Fonts.
    protected Font arial, debugFont;

    //Pointer to LevelEditor.
    protected LevelEditor levelEditor;

    //Current TileSet sprites.
    protected Sprite[] tileSet_Sprites = null;

    //Current TileMesh being edited.
    protected TileMesh tileMesh = null;

    //Sprites used by TileMesh.
    protected Sprite[] tileMesh_Sprites = null, tileMesh_NormalMaps = null;
    protected int selectedSpriteIndex = 0;

    //Color of TileMesh.
    protected Vector4f tileMesh_Color = null;

    //All the other info.
    protected TileSide_Menu tileSide_subMenu;
    protected byte[]
    tileMesh_indecies = null,
    tileMesh_offsets = null,
    tileMesh_shearTypes = null;
    protected float[] tileMesh_shearAmounts = null;


    /**Constructor.*/
    public LE_TileCreator(LevelEditor levelEditor)
    {
        //We need to set on the TileMesh:
        //-What sprites and normal maps does it have?
        //-For each side:
        //[
        //	-Which of those sprites and normal maps is used.
        //	-Its positional offset.
        //	-Its shear type.
        //	-Its shear amount.
        //]
        //Color
        //Emission
        //Material
        //
        //Very left has TileSet sprites column.
        //Next to it is columns of Sprites and Normal Maps the TileMesh used.
        //Top has Color.
        //Next to RGB is material.
        //Under RGB is emission.
        //Under all of that is per-side information in column-per-side format (row per array).
        //Very right has a preview of the TileMesh.


        super(0, 0, new AAB_Box2D(LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT));
        this.controller = Game.controller;
        this.mouse = Game.mouse;

        //Get Fonts.
        arial = Fonts.get("Arial");
        debugFont = Fonts.get("Debug");

        //LevelEditor pointer.
        this.levelEditor = levelEditor;


        
        //TileSet sprites column.
        menuComponents.add(new TileSet_Sprites_Column(0, 0));

        //TileMesh sprites column.
        menuComponents.add(new TileMesh_Sprites_Column(78, 0));

        //ARGB textboxes.
        menuComponents.add(new Labeled_TextBox("A:", new ARGB_Emis_TextBox
        (
            261, 11, arial, Screen.DEFAULT_BLEND,
            new Vector4f(0.5f, 0.5f, 0.5f, 1.0f), new Vector4f(0.64f, 0.64f, 0.64f, 1.0f), CHOICE_A
        )));
        menuComponents.add(new Labeled_TextBox("R:", new ARGB_Emis_TextBox
        (
            307, 11, arial, Screen.DEFAULT_BLEND,
            new Vector4f(0.5f, 0.0f, 0.0f, 1.0f), new Vector4f(0.64f, 0.0f, 0.0f, 1.0f), CHOICE_R
        )));
        menuComponents.add(new Labeled_TextBox("G:", new ARGB_Emis_TextBox
        (
            353, 11, arial, Screen.DEFAULT_BLEND,
            new Vector4f(0.0f, 0.5f, 0.0f, 1.0f), new Vector4f(0.0f, 0.64f, 0.0f, 1.0f), CHOICE_G
        )));
        menuComponents.add(new Labeled_TextBox("B:", new ARGB_Emis_TextBox
        (
            399, 11, arial, Screen.DEFAULT_BLEND,
            new Vector4f(0.0f, 0.0f, 0.5f, 1.0f), new Vector4f(0.0f, 0.0f, 0.80f, 1.0f), CHOICE_B
        )));

        //Material dropDown.
        menuComponents.add
        (
            new Labeled_MenuChoice
            (
                525, 8, 75, 15,
                () -> 
                {

                },
                new Vector4f[]{LevelEditor.MENU_COLOR_0, LevelEditor.MENU_COLOR_1}, "Material", 1.0f
            )
        );

        //Emission textbox.
        menuComponents.add(new Labeled_TextBox("Emission:", new ARGB_Emis_TextBox
        (
            311, 39, arial, Screen.DEFAULT_BLEND,
            new Vector4f(0.79f, 0.38f, 0.0f, 1.0f), new Vector4f(0.91f, 0.44f, 0.0f, 1.0f), CHOICE_EMISSION
        )));

        //Side sub-menu.
        tileSide_subMenu = new TileSide_Menu(259, 105);
        menuComponents.add(tileSide_subMenu);

        //Done button.
        menuComponents.add
        (
            new Labeled_MenuChoice
            (
                837, 468, 48, 22, this::backOut,
                new Vector4f[]{LevelEditor.MENU_COLOR_0, LevelEditor.MENU_COLOR_1}, "DONE", 1.0f
            )
        );

        //TileMeshs side columns and remove buttons will be done dynamically.
    }


    /**Sets the selected TileMesh being edited.*/
    public void setTileMesh(Sprite[] tileSet_Sprites, TileMesh tileMesh)
    {
        //Set tileSet sprites.
        this.tileSet_Sprites = tileSet_Sprites;
        ((TileSet_Sprites_Column)menuComponents.get(0)).setTotalLength(tileSet_Sprites.length * SPRITE_BOX.getHeight());
        //System.out.println(tileSet_Sprites.length);

        //Set tileMesh sprites.
        this.tileMesh = tileMesh;
        this.tileMesh_Sprites = tileMesh.getSprites();
        this.tileMesh_NormalMaps = tileMesh.getNormalMaps();
        ((TileMesh_Sprites_Column)menuComponents.get(1)).setTotalLength(tileMesh_Sprites.length * SPRITE_BOX.getHeight());

        //Set color.
        this.tileMesh_Color = tileMesh.getBlendingColor();
        ((Labeled_TextBox)menuComponents.get(2)).setText((int)(tileMesh_Color.w * 255));
        ((Labeled_TextBox)menuComponents.get(3)).setText((int)(tileMesh_Color.x * 255));
        ((Labeled_TextBox)menuComponents.get(4)).setText((int)(tileMesh_Color.y * 255));
        ((Labeled_TextBox)menuComponents.get(5)).setText((int)(tileMesh_Color.z * 255));

        //TODO Set material.
        //menuComponents.get(6)

        //Set emission.
        ((Labeled_TextBox)menuComponents.get(7)).setText((int)(tileMesh.getEmission() * 255));

        

        //Get data pointers from tileMesh.
        tileMesh_indecies = tileMesh.getSpriteIndecies();
        tileMesh_offsets = tileMesh.getOffsets();
        tileMesh_shearTypes = tileMesh.getShearTypes();
        tileMesh_shearAmounts = tileMesh.getShears();

        //Set tileSides.
        tileSide_subMenu.init(tileMesh_indecies.length);
    }


    protected byte selectingIndex = -1;

    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        if(isCurrentChoice)
        {
            //If selecting a sprite index, only update the TileMesh_Sprites menu.
            if(selectingIndex != -1)
            {
                if(input_Cancel()){selectedSpriteIndex = -1;}
                else
                {
                    return menuComponents.get(1)
                    .update(xOffset, yOffset, true, true);
                }
            }
            //Back out check.
            else if(input_Cancel())
            {
                backOut();
                return true;
            }


            //Directional checks.
            if(!controller.isTyping())
            {
                switch(currentChoice)
                {
                    //
                    //TileSet sprites column
                    //
                    case CHOICE_TILESET_SPRITE_COLUMN:
                    {
                        //Left/Right input check.
                        boolean left = controller.menu_InputPressed(0, Controller.menu_LEFT, true),
                        right = controller.menu_InputPressed(0, Controller.menu_RIGHT, true);

                        //Left: Go to DONE button.
                        if(left && !right)
                        {
                            currentChoice = CHOICE_DONE;
                            return false;
                        }
                        //Right: Go to TileMesh sprites.
                        else if(right && !left)
                        {
                            currentChoice = CHOICE_TILEMESH_SPRITE_COLUMN;
                            return false;
                        }
                    }
                    break;


                    //
                    //TileMesh sprites column
                    //
                    case CHOICE_TILEMESH_SPRITE_COLUMN:
                    {
                        //Left/Right input check.
                        boolean left = controller.menu_InputPressed(0, Controller.menu_LEFT, true),
                        right = controller.menu_InputPressed(0, Controller.menu_RIGHT, true);

                        //Left, go to TileSet column.
                        if(left && !right)
                        {
                            currentChoice = CHOICE_TILESET_SPRITE_COLUMN;
                            return true;
                        }
                        //Right, go to Alpha.
                        else if(right && !left)
                        {
                            currentChoice = CHOICE_A;
                            return true;
                        }
                    }
                    break;


                    //Alpha TextBox
                    case CHOICE_A:
                    {
                        //Left, to TileMesh sprites.
                        if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
                        {currentChoice = CHOICE_TILEMESH_SPRITE_COLUMN;}
                        //Right, to Red.
                        else if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
                        {currentChoice = CHOICE_R;}

                        //Up, to TileMesh sides.
                        else if(controller.menu_InputPressed(0, Controller.menu_UP, true))
                        {
                            tileSide_subMenu.currentColumn = 0;
                            tileSide_subMenu.currentRow = 5;

                            currentChoice = CHOICE_TILEMESH_SIDES;
                        }
                        //Down, to Emission.
                        else if(controller.menu_InputPressed(0, Controller.menu_DOWN, true))
                        {currentChoice = CHOICE_EMISSION;}
                    }
                    break;

                    //Red TextBox
                    case CHOICE_R:
                    {
                        //Left, to Alpha.
                        if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
                        {currentChoice = CHOICE_A;}
                        //Right, to Green.
                        else if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
                        {currentChoice = CHOICE_G;}

                        //Up, to TileMesh sides.
                        else if(controller.menu_InputPressed(0, Controller.menu_UP, true))
                        {
                            tileSide_subMenu.currentColumn = 0;
                            tileSide_subMenu.currentRow = 5;

                            currentChoice = CHOICE_TILEMESH_SIDES;
                        }
                        //Down, to Emission.
                        else if(controller.menu_InputPressed(0, Controller.menu_DOWN, true))
                        {currentChoice = CHOICE_EMISSION;}
                    }
                    break;

                    //Green TextBox
                    case CHOICE_G:
                    {
                        //Left, to Red.
                        if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
                        {currentChoice = CHOICE_R;}
                        //Right, to Blue.
                        else if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
                        {currentChoice = CHOICE_B;}

                        //Up, to TileMesh sides.
                        else if(controller.menu_InputPressed(0, Controller.menu_UP, true))
                        {
                            tileSide_subMenu.currentColumn = 0;
                            tileSide_subMenu.currentRow = 5;

                            currentChoice = CHOICE_TILEMESH_SIDES;
                        }
                        //Down, to Emission.
                        else if(controller.menu_InputPressed(0, Controller.menu_DOWN, true))
                        {currentChoice = CHOICE_EMISSION;}
                    }
                    break;

                    //Blue TextBox
                    case CHOICE_B:
                    {
                        //Left, to Green.
                        if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
                        {currentChoice = CHOICE_G;}
                        //Right, to Material.
                        //else if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
                        //{currentChoice = CHOICE_MATERIAL;}

                        //Up, to Done button.
                        else if(controller.menu_InputPressed(0, Controller.menu_UP, true))
                        {currentChoice = CHOICE_DONE;}
                        //Down, to Emission.
                        else if(controller.menu_InputPressed(0, Controller.menu_DOWN, true))
                        {currentChoice = CHOICE_EMISSION;}
                    }
                    break;

                    //
                    //Material drop-down.
                    //
                    case CHOICE_MATERIAL:
                    {
                        //Left, to Blue.
                        if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
                        {currentChoice = CHOICE_B;}

                        //Up,  to Done button.
                        else if(controller.menu_InputPressed(0, Controller.menu_UP, true)
                        || controller.menu_InputPressed(0, Controller.menu_DOWN, true))
                        {currentChoice = CHOICE_DONE;}
                    }
                    break;

                    //Emission TextBox
                    case CHOICE_EMISSION:
                    {
                        //Left, to TileMesh sprites.
                        if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
                        {currentChoice = CHOICE_TILEMESH_SPRITE_COLUMN;}
                        //Right, to Materials.
                        else if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
                        {currentChoice = CHOICE_MATERIAL;}

                        //Up, to Alpha.
                        else if(controller.menu_InputPressed(0, Controller.menu_UP, true))
                        {currentChoice = CHOICE_A;}
                        //Down, to add side.
                        else if(controller.menu_InputPressed(0, Controller.menu_DOWN, true))
                        {
                            tileSide_subMenu.currentColumn = 0;
                            tileSide_subMenu.currentRow = 0;

                            currentChoice = CHOICE_TILEMESH_SIDES;
                        }
                    }
                    break;


                    case CHOICE_TILEMESH_SIDES:
                    {
                        //Directional checks are done in the menu itself.
                    }
                    break;

                    //Done button.
                    case CHOICE_DONE:
                    {
                        if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
                        {
                            tileSide_subMenu.currentColumn = tileSide_subMenu.columns.size()-1;
                            tileSide_subMenu.currentRow = 5;
                            currentChoice = CHOICE_TILEMESH_SIDES;
                        }
                        else if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
                        {currentChoice = CHOICE_TILESET_SPRITE_COLUMN;}
                        //
                        else if(controller.menu_InputPressed(0, Controller.menu_UP, true)
                        || controller.menu_InputPressed(0, Controller.menu_DOWN, true))
                        {currentChoice = CHOICE_MATERIAL;}
                    }
                    break;
                }
            }


            //Go through each component and update it.
            for(int i = 0; i < menuComponents.size(); i++)
            {
                //Cache the component.
                MenuComponent m = menuComponents.get(i);
                boolean mouseIntersects = false;

                //If not typing, check cursor collision with component.
                if(!controller.isTyping() && m.intersects(position.x + xOffset, position.y + yOffset, Game.mouse))
                {
                    mouseIntersects = true;
                    if(Game.mouse.isMoving())
                    {
                        //Set currentChoice.
                        currentChoice = i;

                        //Default row and column in TileMesh sides.
                        if(currentChoice != CHOICE_TILEMESH_SIDES)
                        {
                            tileSide_subMenu.currentColumn = -1;
                            tileSide_subMenu.currentRow = -1;
                        }
                    }
                }

                //Update Menu component. Stop updating menu if confirmed.
                if(m.update(position.x + xOffset, position.y + yOffset, currentChoice == i, mouseIntersects)){return true;}
            }
        }

        return false;
    }


    private void backOut()
    {
        //Put new data into TileMesh.
        tileMesh.setSpriteIndeciesPointer(tileMesh_indecies);
        tileMesh.setOffsets(tileMesh_offsets);
        tileMesh.setShearTypes(tileMesh_shearTypes);
        tileMesh.setShearAmounts(tileMesh_shearAmounts);

        //Go back to previous state in LevelEditor.
        mouse.pressButton(GLFW_MOUSE_BUTTON_LEFT);
        levelEditor.currentState = levelEditor.previousState;

        currentChoice = CHOICE_TILESET_SPRITE_COLUMN;
    }


    @Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        float xa = position.x + xOffset, ya = position.y + yOffset;

        //Render every menu component here.
        for(int i = 0; i < menuComponents.size(); i++)
        {menuComponents.get(i).render(screen, xa, ya);}

        //Render TileMesh.
        //tileMesh.render_2D(screen, (int)xa, (int)ya);
        tileMesh.render(screen, 735 + (int)xa + screen.getXOffset(), 236 + (int)ya + screen.getYOffset(), 0 + screen.getZOffset(), 1.0f);
        //System.out.println(xa + " " + ya);
    }





    /*
     * Components used by the Tile Creator.
     */

    private static final AAB_Box2D SPRITE_BOX = new AAB_Box2D(64, 64, 0, 0),
    SPRITE_BOX_2 = new AAB_Box2D(129, 64, 0, 0);

    private static final Vector4f
    BACKGROUND_BOX0 = new Vector4f(0.22f, 0.19f, 0.64f, 1.0f),
    BACKGROUND_BOX1 = new Vector4f(0.165f, 0.1425f, 0.48f, 1.0f),
    //
    SPRITE_BOX_GREEN0 = new Vector4f(0.0f, 0.63f, 0.0f, 1.0f),
    SPRITE_BOX_GREEN1 = new Vector4f(0.0f, 0.19f, 0.0f, 1.0f),
    //
    SPRITE_BOX_YELLOW0 = new Vector4f(0.79f, 0.69f, 0.0f, 1.0f),
    SPRITE_BOX_YELLOW1 = new Vector4f(0.4f, 0.2f, 0.0f, 1.0f),
    //
    SPRITE_BOX_PURPLE0 = new Vector4f(0.63f, 0.0f, 0.94f, 1.0f),
    SPRITE_BOX_PURPLE1 = new Vector4f(0.21f, 0.0f, 0.31f, 1.0f);

    private static final AAB_Box2D TILESET_BOX = new AAB_Box2D(73, LevelEditorMain.HEIGHT, 0, 0);

    /**Used to select sprites from the TileSet.*/
    public class TileSet_Sprites_Column extends MenuComponent
    {
        //Background box.
        private Rect_DialogueBox bgBox;

        //Scroll Bar.
        private BasicScrollBar scrollBar;

        //Current selected sprite.
        private int currentSpriteNum = 0;


        /**Constructor.*/
        public TileSet_Sprites_Column(int x, int y)
        {
            super(x, y, TILESET_BOX);

            //Background box.
            this.bgBox = new Rect_DialogueBox
            (
                TILESET_BOX.getWidth() >> 1, TILESET_BOX.getHeight() >> 1, TILESET_BOX.getWidth(), TILESET_BOX.getHeight(),
                BACKGROUND_BOX0, BACKGROUND_BOX1
            );

            //Scroll bar.
            this.scrollBar = new BasicScrollBar(TILESET_BOX.getWidth() - 3, TILESET_BOX.getHeight() >> 1, 8, TILESET_BOX.getHeight(),
            TILESET_BOX.getWidth(), TILESET_BOX.getWidth() - 8, true, 31, LevelEditorMain.HEIGHT - 2, LevelEditorMain.HEIGHT - 2,
            LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR);
        }

        public void setTotalLength(int length)
        {
            scrollBar.setTotalLength(length);
            //System.out.println(scrollBar.getTotalLength());
        }


        @Override
        public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
        {
            if(isCurrentChoice)
            {
                //
                //Up/Down input check.
                //
                boolean up = controller.menu_InputPressed(0, Controller.menu_UP, true),
                down = controller.menu_InputPressed(0, Controller.menu_DOWN, true);

                //Up
                if(up && !down)
                {
                    currentSpriteNum = ((currentSpriteNum-1) + tileSet_Sprites.length) % tileSet_Sprites.length;
                    int currentScroll = (currentSpriteNum * SPRITE_BOX.getHeight());

                    if(currentScroll < scrollBar.getLengthOffset()){scrollBar.setLengthOffset(currentScroll);}
                    else if(currentScroll + SPRITE_BOX.getHeight() > scrollBar.getLengthOffset() + scrollBar.getVisableLength())
                    {scrollBar.setLengthOffset((currentScroll + SPRITE_BOX.getHeight()) - scrollBar.getVisableLength());}
                }
                //Down
                else if(down && !up)
                {
                    currentSpriteNum = (currentSpriteNum+1) % tileSet_Sprites.length;
                    int currentScroll = (currentSpriteNum * SPRITE_BOX.getHeight());
                    
                    if(currentScroll < scrollBar.getLengthOffset()){scrollBar.setLengthOffset(currentScroll);}
                    else if(currentScroll + SPRITE_BOX.getHeight() > scrollBar.getLengthOffset() + scrollBar.getVisableLength())
                    {scrollBar.setLengthOffset((currentScroll + SPRITE_BOX.getHeight()) - scrollBar.getVisableLength());}
                }


                


                //
                //Sprite select check.
                //
                int first = scrollBar.getLengthOffset() / SPRITE_BOX.getHeight(),
                last = first + (scrollBar.getVisableLength() / SPRITE_BOX.getHeight());
                if(last >= tileSet_Sprites.length){last = tileSet_Sprites.length-1;}

                for(int i = first; i <= last; i++)
                {
                    int ia = (i * SPRITE_BOX.getHeight()) - scrollBar.getLengthOffset();

                    //Collision check with box.
                    boolean mouseIntersects = false;
                    if(SPRITE_BOX.intersects(mouse.getX(), mouse.getY(), position.x + xOffset, (position.y + yOffset) + ia))
                    {
                        mouseIntersects = true;
                        if(Game.mouse.isMoving()){currentSpriteNum = i;}
                    }

                    if(currentSpriteNum == i)
                    {
                        //Confirm input check.
                        if(input_Confirm_Pressed(0, mouseIntersects))
                        {
                            //Set sprite in selected slot of the TileMesh's (not TileSet's) sprites to the selected sprite.
                            tileMesh_Sprites[selectedSpriteIndex] = tileSet_Sprites[i];
                            
                            //Don't update the rest of this menu.
                            return true;
                        }
                        //Special 0 input check.
                        else if(controller.menu_InputPressed(0, Controller.menu_SPECIAL_0, false))
                        {
                            //Set normalMap in selected slot of the TileMesh's normalMaps to the selected sprite.
                            tileMesh_NormalMaps[selectedSpriteIndex] = tileSet_Sprites[i];

                            //Don't update the rest of this menu.
                            return true;
                        }
                    }
                }
            }

            //
            //Scroll bar check.
            //
            if(tileSet_Sprites.length * SPRITE_BOX.getHeight() > scrollBar.getVisableLength())
            {scrollBar.update(xOffset, yOffset, false, scrollBar.intersects(xOffset, yOffset, mouse));}

            return false;
        }

        @Override
        public void render(Screen screen, float xOffset, float yOffset)
        {
            int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

            //Render background box.
            bgBox.render(screen, xa, ya);

            //Render scroll bar.
            if(tileSet_Sprites.length * SPRITE_BOX.getHeight() > scrollBar.getVisableLength())
            {scrollBar.render(screen, xa, ya);}


            
            //
            //Render sprites in TileSet.
            //

            //Crop to what's inside the dialouge box.
            //screen.setCropRegion(xa + 1, ya + 1, xa + 1 + SPRITE_BOX.getWidth(), ya + 1 + (TILESET_BOX.getHeight()- 2));

            
            //Which sprite choices are visible?
            int first = scrollBar.getLengthOffset() / SPRITE_BOX.getHeight(),
            last = first + (scrollBar.getVisableLength() / SPRITE_BOX.getHeight());
            if(last >= tileSet_Sprites.length){last = tileSet_Sprites.length-1;}

            for(int i = first; i <= last; i++)
            {
                int ia = (i * SPRITE_BOX.getHeight()) - scrollBar.getLengthOffset();
                Vector4f innerColor = SPRITE_BOX_GREEN1, outterColor = SPRITE_BOX_GREEN0;

                if(currentChoice == CHOICE_TILESET_SPRITE_COLUMN && currentSpriteNum == i)
                {
                    innerColor = SPRITE_BOX_YELLOW1;
                    outterColor = SPRITE_BOX_YELLOW0;
                }

                //Render outter rect then inner rect on top.
                //screen.fillRect(xa+1, (ya+1) + ia, SPRITE_BOX.getWidth(), SPRITE_BOX.getHeight(), outterColor, false);
                screen.drawCroppedRect(xa+1, (ya+1) + ia, SPRITE_BOX.getWidth(), SPRITE_BOX.getHeight(), outterColor, false);
                screen.fillRect(xa+2, (ya+2) + ia, SPRITE_BOX.getWidth()-2, SPRITE_BOX.getHeight()-2, innerColor, false);

                
                //screen.renderSprite(xa+2, (ya+2) + ia, sprite, Sprite.FLIP_NONE, false);

                //Render sprite.
                Sprite sprite = tileSet_Sprites[i];
                if(sprite.getHeight() > sprite.getWidth())
                {
                    screen.renderSprite_Sc(xa+2, (ya+2) + ia, sprite, Sprite.FLIP_NONE,
                    (SPRITE_BOX.getWidth()-2) / (float)sprite.getHeight(), (SPRITE_BOX.getHeight()-2) / (float)sprite.getHeight(), false);
                }
                else
                {
                    screen.renderSprite_Sc(xa+2, (ya+2) + ia, sprite, Sprite.FLIP_NONE,
                    (SPRITE_BOX.getWidth()-2) / (float)sprite.getWidth(), (SPRITE_BOX.getHeight()-2) / (float)sprite.getWidth(), false);
                }

                //screen.drawRect(xa+1, (ya+1) - ia, SPRITE_BOX.getWidth(), SPRITE_BOX.getHeight(), outterColor, false);

                //Render number.
                debugFont.render(screen, xa+2, ya+2 + ia, Integer.toString(i), Screen.DEFAULT_BLEND, false);
            }
            

            //Reset crop region.
            //screen.resetCropRegion();
        }
    }




    private static final AAB_Box2D TILEMESH_BOX = new AAB_Box2D(138, LevelEditorMain.HEIGHT, 0, 0);

    private static final Vector4f 
    SPRITE_BOX_TS0 = new Vector4f(0.02f, 0.0f, 0.36f, 1.0f),
    SPRITE_BOX_TS1 = new Vector4f(0.22f, 0.19f, 0.64f, 1.0f);

    /**Used to select the sprite index to change in the TileMesh.*/
    public class TileMesh_Sprites_Column extends MenuComponent
    {
        //Scroll Bar.
        private BasicScrollBar scrollBar;


        /**Constructor.*/
        public TileMesh_Sprites_Column(int x, int y)
        {
            super(x, y, TILEMESH_BOX);

            //Scroll bar.
            this.scrollBar = new BasicScrollBar(TILEMESH_BOX.getWidth() - 3, TILESET_BOX.getHeight() >> 1, 8, TILESET_BOX.getHeight(),
            TILEMESH_BOX.getWidth(), TILEMESH_BOX.getWidth(), true, 31, LevelEditorMain.HEIGHT - 2, LevelEditorMain.HEIGHT - 2,
            LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR);
        }

        public void setTotalLength(int length){scrollBar.setTotalLength(length);}


        @Override
        public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
        {
            if(isCurrentChoice)
            {
                //
                //Up/Down input check.
                //
                boolean up = controller.menu_InputPressed(0, Controller.menu_UP, true),
                down = controller.menu_InputPressed(0, Controller.menu_DOWN, true);

                //Up
                if(up && !down)
                {
                    selectedSpriteIndex = ((selectedSpriteIndex-1)+tileMesh_Sprites.length) % tileMesh_Sprites.length;
                    int currentScroll = (selectedSpriteIndex * SPRITE_BOX_2.getHeight());

                    if(currentScroll < scrollBar.getLengthOffset()){scrollBar.setLengthOffset(currentScroll);}
                    else if(currentScroll + SPRITE_BOX_2.getHeight() > scrollBar.getLengthOffset() + scrollBar.getVisableLength())
                    {scrollBar.setLengthOffset((currentScroll + SPRITE_BOX_2.getHeight()) - scrollBar.getVisableLength());}
                }
                //Down
                else if(down && !up)
                {
                    selectedSpriteIndex = (selectedSpriteIndex+1) % tileMesh_Sprites.length;
                    int currentScroll = (selectedSpriteIndex * SPRITE_BOX_2.getHeight());
                    
                    if(currentScroll < scrollBar.getLengthOffset()){scrollBar.setLengthOffset(currentScroll);}
                    else if(currentScroll + SPRITE_BOX_2.getHeight() > scrollBar.getLengthOffset() + scrollBar.getVisableLength())
                    {scrollBar.setLengthOffset((currentScroll + SPRITE_BOX_2.getHeight()) - scrollBar.getVisableLength());}
                }



                //
                //Sprite select check.
                //
                int first = scrollBar.getLengthOffset() / SPRITE_BOX_2.getHeight(),
                last = first + (scrollBar.getVisableLength() / SPRITE_BOX_2.getHeight());
                if(last >= tileMesh_Sprites.length){last = tileMesh_Sprites.length-1;}

                for(int i = first; i <= last; i++)
                {
                    int ia = (i * SPRITE_BOX.getHeight()) - scrollBar.getLengthOffset();

                    //Collision check with box.
                    boolean mouseIntersects = false;
                    if(SPRITE_BOX_2.intersects(mouse.getX(), mouse.getY(), position.x + xOffset, (position.y + yOffset) + ia))
                    {
                        mouseIntersects = true;
                        if(Game.mouse.isMoving()){selectedSpriteIndex = i;}
                    }

                    //For current choice.
                    if(selectedSpriteIndex == i)
                    {
                        //Confirm input check.
                        if(input_Confirm_Pressed(0, mouseIntersects))
                        {
                            //If selecting for a column, set the index and exiting selectingIndex mode.
                            if(selectingIndex != -1)
                            {
                                tileMesh_indecies[selectingIndex] = (byte)i;
                                selectingIndex = -1;
                                currentChoice = CHOICE_TILEMESH_SIDES;
                            }
                            //TODO Otherwise...
                            else
                            {

                            }
                            //Set sprite in selected slot of the TileMesh's (not TileSet's) sprites to the selected sprite.
                            //tileMesh.setSpritePointer(selectedSpriteIndex, sprites[i]);
                            
                            //Don't update the rest of this menu.
                            return true;
                        }
                    }
                }
            }

            //
            //Scroll bar check.
            //
            if(tileMesh_Sprites.length * SPRITE_BOX_2.getHeight() > scrollBar.getVisableLength())
            {scrollBar.update(position.x + xOffset, position.y + yOffset, false, scrollBar.intersects(position.x + xOffset, position.y + yOffset, mouse));}

            return false;
        }

        @Override
        public void render(Screen screen, float xOffset, float yOffset)
        {
            int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

            //Background rect.
            screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), SPRITE_BOX_TS0, false);

            //Foreground rects.
            screen.fillRect(xa+1, ya+1, SPRITE_BOX.getWidth(), shape.getHeight()-2, SPRITE_BOX_TS1, false);
            screen.fillRect(xa+3+SPRITE_BOX.getWidth(), ya+1, SPRITE_BOX.getWidth(), shape.getHeight()-2, SPRITE_BOX_TS1, false);

            //Render scroll bar.
            if(menuComponents.size() * SPRITE_BOX.getHeight() > scrollBar.getVisableLength())
            {scrollBar.render(screen, xa, ya);}



            //
            //Render sprites in TileMesh.
            //

            //Crop to what's inside the dialouge boxes.
            //screen.setCropRegion(xa + 1, ya + 1, xa + 1 + TILESPRITE_BOX.getWidth(), ya + 1 + (LevelEditorMain.HEIGHT - 2));

            //Which sprite choices are visible?
            int first = scrollBar.getLengthOffset() / SPRITE_BOX_2.getHeight(),
            last = first + (scrollBar.getVisableLength() / SPRITE_BOX_2.getHeight());
            if(last >= tileMesh_Sprites.length){last = tileMesh_Sprites.length-1;}

            for(int i = first; i <= last; i++)
            //for(int i = 0; i < tileMesh_Sprites.length; i++)
            {
                int ia = (i * SPRITE_BOX.getHeight()) - scrollBar.getLengthOffset();

                //Make option yellow if highlighted.
                Vector4f innerColor0 = SPRITE_BOX_GREEN1, outterColor0 = SPRITE_BOX_GREEN0,
                innerColor1 = SPRITE_BOX_PURPLE1, outterColor1 = SPRITE_BOX_PURPLE0;

                if(currentChoice == CHOICE_TILEMESH_SPRITE_COLUMN && selectedSpriteIndex == i)
                {
                    innerColor0 = SPRITE_BOX_YELLOW1;
                    outterColor0 = SPRITE_BOX_YELLOW0;
                    innerColor1 = SPRITE_BOX_YELLOW1;
                    outterColor1 = SPRITE_BOX_YELLOW0;
                }


                //Render outter rect then inner rect on top.
                //screen.fillRect(xa+1, (ya+1) + ia, SPRITE_BOX.getWidth(), SPRITE_BOX.getHeight(), outterColor0, false);
                screen.drawCroppedRect(xa+1, (ya+1) + ia, SPRITE_BOX.getWidth(), SPRITE_BOX.getHeight(), outterColor0, false);
                screen.fillRect(xa+2, (ya+2) + ia, SPRITE_BOX.getWidth()-2, SPRITE_BOX.getHeight()-2, innerColor0, false);

                //Render sprite.
                Sprite sprite = tileMesh_Sprites[i];
                if(sprite.getHeight() > sprite.getWidth())
                {
                    screen.renderSprite_Sc(xa+2, (ya+2) + ia, sprite, Sprite.FLIP_NONE,
                    (SPRITE_BOX.getWidth()-2) / (float)sprite.getHeight(), (SPRITE_BOX.getHeight()-2) / (float)sprite.getHeight(), false);
                }
                else
                {
                    screen.renderSprite_Sc(xa+2, (ya+2) + ia, sprite, Sprite.FLIP_NONE,
                    (SPRITE_BOX.getWidth()-2) / (float)sprite.getWidth(), (SPRITE_BOX.getHeight()-2) / (float)sprite.getWidth(), false);
                }

                //Render number.
                screen.fillRect(xa+2, (ya+2) + ia, 9, 6, outterColor0, false);
                debugFont.render_RtoL(screen, xa+10, (ya+2) + ia, Integer.toString(i), 1.0f, Screen.DEFAULT_BLEND, false);



                //Same thing for the normal, but without the number.
                //screen.fillRect(xa+3+SPRITE_BOX.getWidth(), (ya+1) + ia, SPRITE_BOX.getWidth(), SPRITE_BOX.getHeight(), outterColor1, false);
                screen.drawCroppedRect(xa+3+SPRITE_BOX.getWidth(), (ya+1) + ia, SPRITE_BOX.getWidth(), SPRITE_BOX.getHeight(), outterColor1, false);
                screen.fillRect(xa+4+SPRITE_BOX.getWidth(), (ya+2) + ia, SPRITE_BOX.getWidth()-2, SPRITE_BOX.getHeight()-2, innerColor1, false);

                sprite = tileMesh_NormalMaps[i];
                if(sprite.getHeight() > sprite.getWidth())
                {
                    screen.renderSprite_Sc(xa+4+SPRITE_BOX.getWidth(), (ya+2) + ia, sprite, Sprite.FLIP_NONE,
                    (SPRITE_BOX.getWidth()-2) / (float)sprite.getHeight(), (SPRITE_BOX.getHeight()-2) / (float)sprite.getHeight(), false);
                }
                else
                {
                    screen.renderSprite_Sc(xa+4+SPRITE_BOX.getWidth(), (ya+2) + ia, sprite, Sprite.FLIP_NONE,
                    (SPRITE_BOX.getWidth()-2) / (float)sprite.getWidth(), (SPRITE_BOX.getHeight()-2) / (float)sprite.getWidth(), false);
                }
            }
            //System.out.println(selectedSpriteIndex);

            //Reset crop region.
            //screen.resetCropRegion();
        }
    }

    

    private static final AAB_Box2D ARGB_BOX = new AAB_Box2D(25, 15);

    /**
     * TextBox for setting the A, R, G, or B component of a TileMesh.
     * This is to be used within a Labeled_TextBox.
     */
    public class ARGB_Emis_TextBox extends Rect_TextBox
    {
        private final byte thisChoice;

        public ARGB_Emis_TextBox(int x, int y, Font font, Vector4f fontColor, Vector4f rectColor0, Vector4f rectColor1, byte thisChoice)
        {
            super(x, y, ARGB_BOX, "", font, fontColor, 3, Controller.TYPING_INT, 1.0f, rectColor0, rectColor1);
            
            this.thisChoice = thisChoice;
        }


        @Override
        public void valueSet()
        {
            int result = getText_Int();
            if(result < 0){result = 0;}
            else if(result > 255){result = 255;}

            float floatResult = result / 255.0f;

            switch(thisChoice)
            {
                case CHOICE_A:
                tileMesh_Color.w = floatResult;
                break;

                case CHOICE_R:
                tileMesh_Color.x = floatResult;
                break;

                case CHOICE_G:
                tileMesh_Color.y = floatResult;
                break;

                case CHOICE_B:
                tileMesh_Color.z = floatResult;
                break;

                case CHOICE_EMISSION:
                tileMesh.setEmission(floatResult);
                break;
            }
        }
    }



    private static final AAB_Box2D
    INDEX_BOX = new AAB_Box2D(34, 70, 0, 0),
    SIDE_TEXTBOX = new AAB_Box2D(34, 15, 0, 0);

    public class TileSide_Menu extends MenuComponent
    {
        //List of TileSides.
        public List<TileSide_Column> columns = new ArrayList<TileSide_Column>();

        //Current column and row.
        public int currentColumn = -1, currentRow = -1;

        
        /**Constructor.*/
        public TileSide_Menu(int x, int y){super(x, y, new AAB_Box2D(36, 147, 0, 0));}

        /**Starts this menu with the given number of sides.*/
        public void init(int numSides)
        {
            //Delete extras that are not needed.
            if(numSides < columns.size())
            {
                for(int i = columns.size()-1; i >= numSides; i--)
                {columns.remove(i);}
            }
            //Add extras that are needed.
            else if(numSides > columns.size())
            {
                for(int i = columns.size(); i < numSides; i++)
                {columns.add(new TileSide_Column(i * 36, 0, (byte)i));}
            }
            ((AAB_Box2D)shape).setWidth(columns.size() * 36);


            //Change text in offset textBoxes.
            for(int i = 0; i < columns.size(); i++)
            {
                TileSide_Column c = columns.get(i);
                c.setSlotNum((byte)i);

                c.offsetX_textBox.setText( tileMesh_offsets[(i*3)] );
                c.offsetY_textBox.setText( tileMesh_offsets[(i*3) + 1] );
                c.offsetZ_textBox.setText( tileMesh_offsets[(i*3) + 2] );
            }
        }


        @Override
        /**Update function.*/
        public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
        {
            boolean moveOut = false;

            if(isCurrentChoice)
            {
                //If not typing.
                if(!controller.isTyping())
                {
                    //
                    //Add/Remove side.
                    //
                    //TODO Only allow if not bound to a Sprite_Indecies animation.
                    //if()
                    {
                        //Add side check.
                        if(controller.menu_InputPressed(0, Controller.menu_SPECIAL_0, true))
                        {
                            addSide();
                            return true;
                        }
                        //Remove side check.
                        else if(columns.size() > 1 && controller.menu_InputPressed(0, Controller.menu_SPECIAL_1, true))
                        {
                            removeSide();
                            return true;
                        }
                    }

                    
                    //
                    //Left/Right input check.
                    //
                    boolean left = controller.menu_InputPressed(0, Controller.menu_LEFT, true),
                    right = controller.menu_InputPressed(0, Controller.menu_RIGHT, true);

                    //Left, go to DONE button.
                    if(left && !right)
                    {
                        if(currentColumn <= 0)
                        {
                            //Go to TileMesh sprites.
                            currentChoice = CHOICE_TILEMESH_SPRITE_COLUMN;

                            currentColumn = -1;
                            currentRow = -1;

                            //Update sides but not the rest of menu.
                            moveOut = true;
                        }
                        else{currentColumn--;}
                    }
                    //Right, go to Alpha.
                    else if(right && !left)
                    {
                        if(currentColumn >= tileMesh_indecies.length-1)
                        {
                            //Go to done button.
                            currentChoice = CHOICE_DONE;

                            currentColumn = -1;
                            currentRow = -1;

                            //Update sides but not the rest of menu.
                            moveOut = true;
                        }
                        else{currentColumn++;}
                    }


                    //
                    //Up/Down input check.
                    //
                    boolean up = controller.menu_InputPressed(0, Controller.menu_UP, true),
                    down = controller.menu_InputPressed(0, Controller.menu_DOWN, true);

                    //Up
                    if(up && !down)
                    {
                        if(currentRow == 0)
                        {
                            currentChoice = CHOICE_EMISSION;

                            currentColumn = -1;
                            currentRow = -1;

                            //Update sides but not the rest of menu.
                            moveOut = true;
                        }
                        else{currentRow--;}
                    }
                    //Down
                    else if(down && !up)
                    {
                        if(currentRow >= 5)
                        {
                            currentChoice = CHOICE_A;

                            currentColumn = -1;
                            currentRow = -1;

                            //Update sides but not the rest of menu.
                            moveOut = true;
                        }
                        else{currentRow++;}
                    }
                }


                //Update each column.
                for(int i = 0; i < columns.size(); i++)
                {
                    //Cache the component.
                    TileSide_Column tc = columns.get(i);
                    boolean mouseIntersects = false;

                    //If not typing, check cursor collision with component.
                    if(!controller.isTyping() && tc.intersects(position.x + xOffset, position.y + yOffset, Game.mouse))
                    {
                        mouseIntersects = true;
                        if(Game.mouse.isMoving()){currentColumn = i;}
                    }

                    //Update Menu component.
                    if(tc.update(position.x + xOffset, position.y + yOffset, currentColumn == i, mouseIntersects)){return true;}
                }

                //System.out.println(currentColumn);
            }
            return moveOut;
        }


        private void addSide()
        {
            //Add one slot to tileData.
            byte[]
            bi = new byte[tileMesh_indecies.length + 1],
            bo = new byte[tileMesh_offsets.length + 3],
            bst = new byte[tileMesh_indecies.length + 1];
            float[] bsa = new float[tileMesh_shearAmounts.length + 1];

            int ii = 0;
            for(int i = 0; i < tileMesh_indecies.length; i++)
            {
                bi[ii] = tileMesh_indecies[i];

                bo[(ii*3)] = tileMesh_offsets[(i*3)];
                bo[(ii*3)+1] = tileMesh_offsets[(i*3)+1];
                bo[(ii*3)+2] = tileMesh_offsets[(i*3)+2];

                bst[ii] = tileMesh_shearTypes[i];

                bsa[ii] = tileMesh_shearAmounts[i];

                if(ii == currentColumn){ii++;}
                ii++;
            }
            tileMesh_indecies = bi;
            tileMesh_offsets = bo;
            tileMesh_shearTypes = bst;
            tileMesh_shearAmounts = bsa;
            

            //Add column and change collision.
            columns.add(currentColumn+1, new TileSide_Column((currentColumn+1) * 36, 0, (byte)(currentColumn+1)));
            ((AAB_Box2D)shape).setWidth(columns.size() * 36);

            //Update columns.
            for(int i = 0; i < columns.size(); i++)
            {
                TileSide_Column c = columns.get(i);
                c.setSlotNum((byte)i);

                c.offsetX_textBox.setText( tileMesh_offsets[(i*3)] );
                c.offsetY_textBox.setText( tileMesh_offsets[(i*3) + 1] );
                c.offsetZ_textBox.setText( tileMesh_offsets[(i*3) + 2] );

                c.position.x = i*36;
            }
        }

        private void removeSide()
        {
            //tileMesh_indecies;
            //tileMesh_offsets;
            //tileMesh_shearTypes;
            //tileMesh_shearAmounts;

            //Remove one slot from tileData.
            byte[]
            bi = new byte[tileMesh_indecies.length - 1],
            bo = new byte[tileMesh_offsets.length - 3],
            bst = new byte[tileMesh_indecies.length - 1];
            float[] bsa = new float[tileMesh_shearAmounts.length - 1];

            int ii = 0;
            for(int i = 0; i < bi.length; i++)
            {
                if(ii == currentColumn){ii++;}

                
                bi[i] = tileMesh_indecies[ii];

                bo[(i*3)] = tileMesh_offsets[(ii*3)];
                bo[(i*3)+1] = tileMesh_offsets[(ii*3)+1];
                bo[(i*3)+2] = tileMesh_offsets[(ii*3)+2];

                bst[i] = tileMesh_shearTypes[ii];

                bsa[i] = tileMesh_shearAmounts[ii];


                ii++;
            }
            tileMesh_indecies = bi;
            tileMesh_offsets = bo;
            tileMesh_shearTypes = bst;
            tileMesh_shearAmounts = bsa;
            

            //Add column and change collision.
            columns.remove(currentColumn);
            ((AAB_Box2D)shape).setWidth(columns.size() * 36);

            //Update columns.
            for(int i = 0; i < columns.size(); i++)
            {
                TileSide_Column c = columns.get(i);
                c.setSlotNum((byte)i);

                c.offsetX_textBox.setText( tileMesh_offsets[(i*3)] );
                c.offsetY_textBox.setText( tileMesh_offsets[(i*3) + 1] );
                c.offsetZ_textBox.setText( tileMesh_offsets[(i*3) + 2] );

                c.position.x = i*36;
            }
        }



        @Override
        public void render(Screen screen, float xOffset, float yOffset)
        {
            float xa = position.x + xOffset, ya = position.y + yOffset;

            //Render each side column.
            for(int i = 0; i < columns.size(); i++)
            {columns.get(i).render(screen, xa, ya);}
        }



        /*
         * Components used by TileSide_Menu
         */


        public class TileSide_Column extends MenuComponent
        {
            /*Choice for Sprite index.*/
            private class SpriteIndex_Choice extends MenuComponent
            {
                public SpriteIndex_Choice(int x, int y){super(x, y, INDEX_BOX);}

                @Override
                public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
                {
                    if(isCurrentChoice)
                    {
                        if(input_Confirm_Pressed(0, mouseIntersectsThis))
                        {
                            //Set menu to select a sprite index from the left.
                            selectingIndex = slotNum;
                            currentChoice = CHOICE_TILEMESH_SPRITE_COLUMN;

                            //Don't update the rest of the menu.
                            return true;
                        }
                    }

                    return false;
                }

                @Override
                public void render(Screen screen, float xOffset, float yOffset)
                {
                    int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

                    //Background.
                    if(currentColumn == slotNum && currentRow == 0){screen.fillRect(xa, ya, 34, 70, SPRITE_BOX_YELLOW0, false);}
                    else{screen.fillRect(xa, ya, 34, 70, BACKGROUND_BOX0, false);}

                    //Sprite.
                    Sprite sprite = tileMesh_Sprites[ tileMesh_indecies[slotNum] ];
                    screen.renderSprite_Sc(xa+1, ya+1, sprite, Sprite.FLIP_NONE, 32 / (float)sprite.getHeight(), 32 / (float)sprite.getWidth(), false);

                    //Normal Map.
                    sprite = tileMesh_NormalMaps[ tileMesh_indecies[slotNum] ];
                    screen.renderSprite_Sc(xa+1, ya+38, sprite, Sprite.FLIP_NONE, 32 / (float)sprite.getHeight(), 32 / (float)sprite.getWidth(), false);
                }
            }
            SpriteIndex_Choice spriteIndex_Choice;



            /*TextBoxes for offset.*/
            private class Offset_TextBox extends Rect_TextBox
            {
                private byte slot;

                /**Constructor.*/
                public Offset_TextBox(int x, int y, byte slot)
                {
                    super(x, y, SIDE_TEXTBOX, arial, Screen.DEFAULT_BLEND, 4,
                    Controller.TYPING_SIGNED_INT, 1.0f, BACKGROUND_BOX0, SPRITE_BOX_YELLOW0);

                    this.slot = slot;
                }

                @Override
                public void valueSet(){tileMesh_offsets[(slotNum * 3) + slot] = getText_Byte();}
            }
            Offset_TextBox offsetX_textBox, offsetY_textBox, offsetZ_textBox;



            /*Toggle for ShearType.*/
            private class ShearType_Toggle extends MenuComponent
            {
                public ShearType_Toggle(int x, int y){super(x, y, SIDE_TEXTBOX);}

                @Override
                public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
                {
                    if(isCurrentChoice)
                    {
                        if(input_Confirm_Pressed(0, mouseIntersectsThis) || controller.menu_InputPressed(0, Controller.menu_RIGHT_TAB, true))
                        {
                            //Set shearType forward one index.
                            tileMesh_shearTypes[slotNum] = (byte)((tileMesh_shearTypes[slotNum] + 1) % TileMesh.MAX_SHEARTYPES);
                            return true;
                        }
                        else if(controller.menu_InputPressed(0, Controller.menu_LEFT_TAB, true))
                        {
                            //Set shearType backward one index.
                            tileMesh_shearTypes[slotNum] = (byte)(((tileMesh_shearTypes[slotNum] - 1) + TileMesh.MAX_SHEARTYPES) % TileMesh.MAX_SHEARTYPES);
                            return true;
                        }
                    }

                    return false;
                }

                @Override
                public void render(Screen screen, float xOffset, float yOffset)
                {
                    int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

                    //Background.
                    if(currentColumn == slotNum && currentRow == 4){screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), SPRITE_BOX_YELLOW0, false);}
                    else{screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), BACKGROUND_BOX0, false);}

                    //ShearType text.
                    arial.render(screen, xa+1, ya+2, TileMesh.getShearType_Text(tileMesh_shearTypes[slotNum]), false);
                }
            }
            private ShearType_Toggle shearType_Toggle;



            /*Toggle for Shear Amount.*/
            private class ShearAmount_Toggle extends MenuComponent
            {
                public ShearAmount_Toggle(int x, int y){super(x, y, SIDE_TEXTBOX);}

                @Override
                public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
                {
                    if(isCurrentChoice)
                    {
                        if(input_Confirm_Pressed(0, mouseIntersectsThis) || controller.menu_InputPressed(0, Controller.menu_RIGHT_TAB, true))
                        {
                            //Set shearType forward one index.
                            byte newVal = (byte)((TileMesh.convertShearToByte(tileMesh_shearAmounts[slotNum]) + 1) % 7);
                            tileMesh_shearAmounts[slotNum] = TileMesh.convertByteToShear(newVal);
                            return true;
                        }
                        else if(controller.menu_InputPressed(0, Controller.menu_LEFT_TAB, true))
                        {
                            //Set shearType backward one index.
                            byte newVal = (byte)(((TileMesh.convertShearToByte(tileMesh_shearAmounts[slotNum]) - 1) + 7) % 7);
                            tileMesh_shearAmounts[slotNum] = TileMesh.convertByteToShear(newVal);
                            return true;
                        }
                    }

                    return false;
                }

                @Override
                public void render(Screen screen, float xOffset, float yOffset)
                {
                    int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

                    //Background.
                    if(currentColumn == slotNum && currentRow == 5){screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), SPRITE_BOX_YELLOW0, false);}
                    else{screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), BACKGROUND_BOX0, false);}

                    //Shear amount text.
                    arial.render(screen, xa+1, ya+2, Float.toString(tileMesh_shearAmounts[slotNum]), false);
                }
            }
            private ShearAmount_Toggle shearAmount_Toggle;

            
            public byte slotNum = 0;


            /**Constructor.*/
            public TileSide_Column(int x, int y, byte slotNum)
            {
                super(x, y, new AAB_Box2D(36, 153, 0, 0));
                this.slotNum = slotNum;

                //Sprite index.
                spriteIndex_Choice = new SpriteIndex_Choice(1, 1);

                //Offset.
                offsetX_textBox = new Offset_TextBox(1, 73, (byte)0);
                offsetY_textBox = new Offset_TextBox(1, 88, (byte)1);
                offsetZ_textBox = new Offset_TextBox(1, 103, (byte)2);

                //Shear Type.
                shearType_Toggle = new ShearType_Toggle(1, 120);

                //Shear Amount.
                shearAmount_Toggle = new ShearAmount_Toggle(1, 137);
            }

            public void setSlotNum(byte slotNum){this.slotNum = slotNum;}


            @Override
            public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
            {
                float xa = position.x + xOffset, ya = position.y + yOffset;

                //
                //Sprite index.
                //
                boolean intersects = spriteIndex_Choice.intersects(xa, ya, mouse);
                if(intersects){currentRow = 0;}
                if(spriteIndex_Choice.update(xa, ya, isCurrentChoice && currentRow == 0, intersects)){return true;}


                //
                //Offsets.
                //
                intersects = offsetX_textBox.intersects(xa, ya, mouse);
                if(intersects){currentRow = 1;}
                if(offsetX_textBox.update(xa, ya, isCurrentChoice && currentRow == 1, intersects)){return true;}

                intersects = offsetY_textBox.intersects(xa, ya, mouse);
                if(intersects){currentRow = 2;}
                if(offsetY_textBox.update(xa, ya, isCurrentChoice && currentRow == 2, intersects)){return true;}

                intersects = offsetZ_textBox.intersects(xa, ya, mouse);
                if(intersects){currentRow = 3;}
                if(offsetZ_textBox.update(xa, ya, isCurrentChoice && currentRow == 3, intersects)){return true;}


                //
                //ShearType.
                //
                intersects = shearType_Toggle.intersects(xa, ya, mouse);
                if(intersects){currentRow = 4;}
                if(shearType_Toggle.update(xa, ya, isCurrentChoice && currentRow == 4, intersects)){return true;}

                //
                //Shear amount.
                //
                intersects = shearAmount_Toggle.intersects(xa, ya, mouse);
                if(intersects){currentRow = 5;}
                if(shearAmount_Toggle.update(xa, ya, isCurrentChoice && currentRow == 5, intersects)){return true;}
            

                return false;
            }

            @Override
            public void render(Screen screen, float xOffset, float yOffset)
            {
                int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

                //Background.
                screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), BACKGROUND_BOX1, false);

                //Render TileMesh sprite and normalMap.
                spriteIndex_Choice.render(screen, xa, ya);

                //Render textBoxes.
                offsetX_textBox.render(screen, xa, ya);
                offsetY_textBox.render(screen, xa, ya);
                offsetZ_textBox.render(screen, xa, ya);

                //Render shearType choice.
                shearType_Toggle.render(screen, xa, ya);

                //Render shear amount choice.
                shearAmount_Toggle.render(screen, xa, ya);
            }
        }
    }
    /*
     * [-RATE- -ACTION- -PARAMS-]           |
     * [-RATE- -ACTION- -PARAMS-]           |
     * [-RATE- -ACTION- -PARAMS-]           |
     * [-RATE- -ACTION- -PARAMS-]           |
     * [-RATE- -ACTION- -PARAMS-]           |TILESPRITES
     * [      ADD NEW FRAME     ]           |USED BY
     *                                      |THIS ANIM
     *                              |DONE|  |
     *                                      |
     * |============================|       |
     * |        TILEBAR..?          |       |
     * |                            |       |
     */



    
}
