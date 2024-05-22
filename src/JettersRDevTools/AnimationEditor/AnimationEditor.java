package JettersRDevTools.AnimationEditor;
/**
 * This is a Dev Tool designed to create Sprite Layouts and Animations for the main game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 9/2/2023
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

import JettersR.Controller;
import JettersR.Game;
import JettersR.Main;
import JettersR.Mouse;
import JettersR.GameStates.GameState;
import JettersR.GameStates.GameStateManager;
import JettersR.Graphics.Font;
import JettersR.Graphics.Fonts;
import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.SpriteSheet;
import JettersR.Graphics.Sprites;
import JettersR.Graphics.Animations.Functional_FrameAnimation;
import JettersR.Graphics.Animations.Functional_FrameAnimation.Action;
import JettersR.UI.Menus.*;
import JettersR.UI.Menus.MenuChoices.Labeled_MenuChoice;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditorMain;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class AnimationEditor extends GameState
{
    public static final String editPath = //"src/JettersRDevTools/AnimationEditor/Edits/";
    //"C:/Users/luke/Desktop/UnBlocked/assets/";
    "C:/Users/luke/Desktop/LukeTest/assets/";

    public final Mouse mouse = Game.mouse;
    public final Controller controller = Game.controller;

    /*
     * -Ask user to make a new Sprite Layout or load an existing one.
     * 
     * -NEW LAYOUT: Use a FlieChooser to pick a SpriteSheet and start the interface as normal.
     * -LOAD LAYOUT: Use a FileChooser to pick a _LYT.dat file and start the interface from that.
     * 
     * Layout Interface:
     * +============+ NEW LOAD SAVE SAVEAS [CHANGE SHEET]
     * |   F        |====================================
     * |   R        |
     * |   A        |
     * |   M        |               SHEET
     * |   E        |
     * |   S        |
     * | New Frame  |
     * +============+
     * 
     * -NEW: Choose to make a new Sprite Layout or Animation.
     * -LOAD: Choose to load an existing Sprite Layout or Animation.
     * -SAVE: Saves the current layout under the current name. Will default to SAVEAS if no name is
     *        present and will ask to override file if a name is present.
     * -SAVEAS: Saves the current layout under a typed name. Will ask for override if an animation
     *          already has the given name.
     * [CHANGE SHEET]: Opens a FlieChooser to select a SpriteSheet to change the animation to. Parameters
     *                 outside the new sheet will be limited to the edges of the new sheet.
     * 
     * Adding a New Frame:
     * -Ask for upper-left corner.
     * -Ask for lower-right corner.
     * 
     * Editing a Frame:
     * -Delete (Deletes this frame)
     * -Copy (Creates a new frame with the same parameters as this one)
     * [Click frame to highlight coords for editing. Will already be clicked if it was just made.]
     * [Hold and drag frame to move it to a different slot. Release to place.]
     * 
     * 
     * Animation Interface:
     * +============+ NEW LOAD SAVE SAVEAS      [CHANGE]
     * |   F        |====================================
     * |   R        |   F I - Rate
     * |   A        |   R N - Rate  
     * |   M        |   A D - Rate
     * |   E        |   M E - Rate              +=========
     * |   S        |   E X - Rate              |  
     * | New Frame  |     S - Rate              | PREVIEW
     * +============+  New Index                |
     * 
     * Adding a New Index:
     * -Ask for frame.
     * 
     * Editing an Index.
     * -LoopTo (Toggles this index as the loopTo frame) -Delete (Deletes this Index)
     *                                                  -Copy (Creates a new index with the same parameters as this one)
     * [Hold and drag index to move it to a different slot. Release to place.]
     */

    private Menu startUp_Menu = null;
    private Menu areYouSure_Layout_menu = null;

    private AnimationEditor_UI ui;

    //Layout/Animation Name.
    private String name = "new";
    private SpriteSheet currentSheet = null;
    private Sprite sheetAsSprite = null;

    //List of int arrays (0 = x, 1 = y, 2 = width, 3 = height).
    public List<int[]> ints = new ArrayList<int[]>();
    public List<Sprite> sprites = new ArrayList<Sprite>();
    public int spriteNum = 0;

    //Camera Position.
    private Vector2f position = new Vector2f(-100, -100);

    public static final byte
    STATE_STARTUP = 0,
    STATE_LOAD_LAYOUT = 1,
    STATE_LOAD_SHEET = 2,
    STATE_EDIT_LAYOUT = 3,
    STATE_areYouSure_LAYOUT = 4,
    STATE_LOAD_ANIM = 5,
    STATE_EDIT_ANIM = 6,
    STATE_PROMPT_RATE = 7,
    STATE_PROMPT_OFFSETX = 8,
    STATE_PROMPT_OFFSETY = 9,
    STATE_PROMPT_ACTION = 10;
    public byte currentState = STATE_STARTUP, previousState = currentState;

    /**Constructor.*/
    public AnimationEditor(GameStateManager gameStateManager)
    {
        super(gameStateManager);
    }

    public static final int STARTUP_WIDTH = 246, STARTUP_HEIGHT = 64;
    @Override
    public void start()
    {
        startUp_Menu = new MouseMenu(new AAB_Box2D(LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT));
        startUp_Menu.addComponent
        (
            new Labeled_MenuChoice(50 + (STARTUP_WIDTH/2), 180 + (STARTUP_HEIGHT/2), STARTUP_WIDTH, STARTUP_HEIGHT,
            () -> 
            {
                currentSheet = null;
                currentSheet = Sprites.global_UISheet("DialogueSprites");
                sheetAsSprite = new Sprite(currentSheet);
                currentState = STATE_EDIT_LAYOUT;
            },
            new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
            "NEW LAYOUT", 2.0f)
        )
        .addComponent
        (
            new Labeled_MenuChoice(Game.NORMAL_WIDTH-(296-(STARTUP_WIDTH/2)), 180 + (STARTUP_HEIGHT/2), STARTUP_WIDTH, STARTUP_HEIGHT,
            () -> 
            {
                load_Sheet(true);
            },
            new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
            "LOAD LAYOUT", 2.0f)
        );
        //
        //
        areYouSure_Layout_menu = new MouseMenu(new AAB_Box2D(LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT));
        areYouSure_Layout_menu.addComponent
        (
            new Labeled_MenuChoice(50 + (STARTUP_WIDTH/2), 180 + (STARTUP_HEIGHT/2), STARTUP_WIDTH, STARTUP_HEIGHT,
            () -> 
            {
                ints.clear();
                sprites.clear();
                currentState = previousState;
            },
            new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
            "YES", 3.0f)
        )
        .addComponent
        (
            new Labeled_MenuChoice(Game.NORMAL_WIDTH-(296-(STARTUP_WIDTH/2)), 180 + (STARTUP_HEIGHT/2), STARTUP_WIDTH, STARTUP_HEIGHT,
            () -> 
            {
                currentState = previousState;
            },
            new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
            "NO", 3.0f)
        );
        //
        //
        ui = new AnimationEditor_UI(this);
    }

    public void load_Sheet(boolean andLayout)
    {
        currentState = STATE_LOAD_SHEET;

        //Create SpriteSheet Selection UI on a Seperate Thread.
        new Thread(() ->
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(editPath));

            fileChooser.setDialogTitle("Select an Image");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.requestFocus();

            //Show it and wait for the user to select a Folder. If none was selected, cancel.
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    File file = fileChooser.getSelectedFile();
                    //
                    currentSheet = new SpriteSheet(file);//, "");
                    sheetAsSprite = new Sprite(currentSheet);
                    //
                    sprites.clear();
                    for(int i = 0; i < ints.size(); i++)
                    {
                        int[] ia = ints.get(i);
                        sprites.add(new Sprite(currentSheet, ia[0], ia[1],  ia[2], ia[3]));
                    }
                    //
                    if(andLayout){load_Layout();}
                    else{currentState = previousState;}
                }
                catch(NullPointerException e)
                {
                    NOtime = 30;
                    currentState = previousState;
                }
            }
            else{currentState = previousState;}
        }).start();
    }

    public void load_Layout()
    {
        currentState = STATE_LOAD_LAYOUT;

        //Create Layout Selection UI on a Seperate Thread.
        new Thread(() ->
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(editPath));

            fileChooser.setDialogTitle("Select a Layout");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.requestFocus();

            //Show it and wait for the user to select a Folder. If none was selected, cancel.
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                //Clear the lists.
                ints.clear();
                sprites.clear();
                //
                File file = fileChooser.getSelectedFile();
                //
                try
                {
                    //Set up the input stream.
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    
                    byte[] bytes = new byte[16];
                    for(int i = 0; i < file.length(); i += 16)
                    {
                        bis.read(bytes);

                        //Create a new Sprite out of the bytes and sheet.
                        int[] ia = new int[]
                        {
                            //We have to "and" the last 3 bytes of each int by 255 since these bytes are signed (range from -128 to +127, nowhere near 255).
                            Game.bytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]),//x
                            Game.bytesToInt(bytes[4], bytes[5], bytes[6], bytes[7]),//y
                            Game.bytesToInt(bytes[8], bytes[9], bytes[10], bytes[11]),//width
                            Game.bytesToInt(bytes[12], bytes[13], bytes[14], bytes[15])//height
                        };

                        ints.add(ia);
                        sprites.add(new Sprite(currentSheet, ia[0], ia[1], ia[2], ia[3]));
                    }

                    //Close the input streams.
                    bis.close();
                    fis.close();
                }
                catch(FileNotFoundException e){e.printStackTrace();}
                catch(IOException e){e.printStackTrace();}
                //
                currentState = STATE_EDIT_LAYOUT;
            }
            else{currentState = previousState;}
        }).start();
    }

    public void load_Animation()
    {
        currentState = STATE_LOAD_ANIM;

        //Create Layout Selection UI on a Seperate Thread.
        new Thread(() ->
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(editPath));

            fileChooser.setDialogTitle("Select an Animation");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.requestFocus();

            //Show it and wait for the user to select a Folder. If none was selected, cancel.
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                //Clear the lists.
                animSprites.clear();
                animSpriteIDs.clear();
                f_rates.clear();
                offsets.clear();
                actionNumbers.clear();

                File file = fileChooser.getSelectedFile();
                //
                try
                {
                    //Set up the input stream.
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    
                    //LoopTo
                    byte[] bytes = new byte[4];
                    bis.read(bytes);
                    loopTo = Game.bytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]);

                    //Frames
                    int length = (int)((file.length()-4) / Functional_FrameAnimation.BYTES_PER_FRAME);
                    bytes = new byte[Functional_FrameAnimation.BYTES_PER_FRAME];
                    for(int i = 0; i < length; i++)
                    {
                        bis.read(bytes);

                        //Sprite
                        short sID = Game.bytesToShort(bytes[0], bytes[1]);
                        animSprites.add(sprites.get(sID));
                        animSpriteIDs.add(sID);

                        //Rate
                        @fixed int f_rate = fixed( Game.bytesToFloat(bytes[2], bytes[3], bytes[4], bytes[5]) );
                        //@fixed int f_rate = Game.bytesToInt(bytes[2], bytes[3], bytes[4], bytes[5]);
                        f_rates.add(f_rate);

                        //Offset
                        short[] offset = new short[]
                        {
                            Game.bytesToShort(bytes[6], bytes[7]),
                            Game.bytesToShort(bytes[8], bytes[9])
                        };
                        offsets.add(offset);

                        //Action Number
                        actionNumbers.add(Game.bytesToShort(bytes[10], bytes[11]));
                    }

                    //Reset Preview
                    resetPreview();

                    //Close the input streams.
                    bis.close();
                    fis.close();
                }
                catch(FileNotFoundException e){e.printStackTrace();}
                catch(IOException e){e.printStackTrace();}
                //
                currentState = STATE_EDIT_ANIM;
            }
            else{currentState = previousState;}
        }).start();
    }

    private int mouseX = 0, mouseY = 0;
    private float scale = 1.0f;

    private int lastWidth, lastHeight;

    private static final byte
    SUB_BASE = 0,
    SUB_POINTS_UL = 1,
    SUB_POINTS_DR = 2,
    SUB_DUP = 3;

    private byte subState = SUB_BASE;

    private StringBuffer textBuffer = new StringBuffer("");

    @Override
    public void update()
    {
        //Poll inputs here to reduce input lag slightly.
        glfwPollEvents();
        controller.update();

        switch(currentState)
        {
            case STATE_STARTUP:
            {
                //Update StartUp menu choices.
                startUp_Menu.update(0, 0, true, true);
                mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
            }
            break;

            case STATE_LOAD_LAYOUT:
            case STATE_LOAD_SHEET:
            //...This is just here, I guess.
            break;

            case STATE_EDIT_LAYOUT:
            spriteLayout_Update();
            mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
            break;

            case STATE_areYouSure_LAYOUT:
            areYouSure_Layout_menu.update(0, 0, true, true);
            mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
            break;

            case STATE_LOAD_ANIM:
            //...This is just here, I guess.
            break;

            case STATE_EDIT_ANIM:
            animation_Update();
            mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
            break;

            case STATE_PROMPT_RATE:
            {
                //textBuffer = controller.getStringObject();
                if(!controller.isTyping())
                {
                    controller.endTyping();
                    //
                    try
                    {
                        //TODO I may as well redo the animation editor at this point.
                        f_lastRate = fixed( Float.parseFloat(textBuffer.toString()) );


                        f_rates.set(currentAnimFrame, f_lastRate);
                        //f_rates.set(currentAnimFrame, fixed(f_lastRate));
                        resetPreview();
                    }
                    catch(NumberFormatException e)
                    {
                        //e.printStackTrace();
                        NOtime = 30;
                    }
                    //
                    textBuffer.replace(0, textBuffer.length(), "");
                    currentState = STATE_EDIT_ANIM;
                }
            }
            break;

            case STATE_PROMPT_OFFSETX:
            {
                //textBuffer = controller.getStringObject();
                if(!controller.isTyping())
                {
                    //textBuffer = controller.endTyping();
                    //
                    try
                    {
                        lastOX = Short.parseShort(textBuffer.toString());
                        //
                        short[] s = offsets.get(currentAnimFrame);
                        s[0] = lastOX;
                        //
                        resetPreview();
                    }
                    catch(NumberFormatException e)
                    {
                        e.printStackTrace();
                        NOtime = 30;
                    }
                    //
                    textBuffer.replace(0, textBuffer.length(), "");
                    currentState = STATE_EDIT_ANIM;
                }
            }
            break;

            case STATE_PROMPT_OFFSETY:
            {
                //textBuffer = controller.getStringObject();
                if(!controller.isTyping())
                {
                    controller.endTyping();
                    //
                    try
                    {
                        lastOY = Short.parseShort(textBuffer.toString());
                        //
                        short[] s = offsets.get(currentAnimFrame);
                        s[1] = lastOY;
                        //
                        resetPreview();
                    }
                    catch(NumberFormatException e)
                    {
                        e.printStackTrace();
                        NOtime = 30;
                    }
                    //
                    textBuffer.replace(0, textBuffer.length(), "");
                    currentState = STATE_EDIT_ANIM;
                }
            }
            break;

            case STATE_PROMPT_ACTION:
            {
                //textBuffer = controller.getStringObject();
                if(!controller.isTyping())
                {
                    controller.endTyping();
                    //
                    try
                    {
                        lastAction = Short.parseShort(textBuffer.toString());
                        actionNumbers.set(currentAnimFrame, lastAction);
                        resetPreview();
                    }
                    catch(NumberFormatException e)
                    {
                        e.printStackTrace();
                        NOtime = 30;
                    }
                    //
                    textBuffer.replace(0, textBuffer.length(), "");
                    currentState = STATE_EDIT_ANIM;
                }
            }
            break;
        }
    }

    /**Sprite Layout update.*/
    public void spriteLayout_Update()
    {
        int xa = 8, ya = 8;
        boolean shift = false;
        if(controller.isKeyHeld(GLFW_KEY_LEFT_SHIFT))
        {
            xa = 16;
            ya = 16;
            scale = (int)scale;
            shift = true;
        }

        if(!ui.intersects_components(mouse))
        {
            //Zoom
            byte scroll = mouse.getScroll();
            //
            if(shift)
            {
                if(scroll < 0){scale += 1f;}
                else if(scroll > 0){scale -= 1f;}
            }
            else
            {
                if(scroll < 0){scale += 0.1f;}
                else if(scroll > 0){scale -= 0.1f;}
            }
            //
            scale = Math.min(8.0f, Math.max(0.1f, scale));
        }
        ui.update(0, 0, true, true);

        //Horizontal Scroll
        if(controller.isKeyHeld(GLFW_KEY_A)){position.x -= xa / scale;}
        if(controller.isKeyHeld(GLFW_KEY_D)){position.x += xa / scale;}

        //Vertical Scroll
        if(controller.isKeyHeld(GLFW_KEY_W)){position.y -= ya / scale;}
        if(controller.isKeyHeld(GLFW_KEY_S)){position.y += ya / scale;}

        position.x = //(int)
        Math.min(sheetAsSprite.getWidth() - ((sheetAsSprite.getWidth()/2.0f) / scale), Math.max(-256.0f / scale, position.x));
        position.y = //(int)
        Math.min(sheetAsSprite.getHeight() - ((sheetAsSprite.getHeight()/2.0f) / scale), Math.max(-256.0f / scale, position.y));

        //Mouse Position
        mouseX = (int)((position.x + (mouse.getX() / scale)));
        mouseY = (int)((position.y + (mouse.getY() / scale)));
        //
        mouseX = Math.min(sheetAsSprite.getWidth(), Math.max(0, mouseX));
        mouseY = Math.min(sheetAsSprite.getHeight(), Math.max(0, mouseY));

        switch(subState)
        {
            case SUB_BASE:
            {
                //Add Frame
                if(controller.isKeyPressed(GLFW_KEY_ENTER))
                {
                    if(spriteNum == ints.size())
                    {
                        spriteNum = ints.size();
                        ints.add(new int[4]);
                    }
                    else
                    {
                        spriteNum++;
                        ints.add(spriteNum, new int[4]);
                    }
                    subState = SUB_POINTS_UL;
                    return;
                }

                //Delete Frame
                if(ints.size() > 1 && controller.isKeyPressed(GLFW_KEY_BACKSPACE))
                {
                    ints.remove(spriteNum);
                    sprites.remove(spriteNum);
                    //
                    if(spriteNum >= ints.size()){spriteNum--;}
                    //
                    return;
                }

                //Duplicate Dimensions
                if(controller.isKeyPressed(GLFW_KEY_C))
                {subState = SUB_DUP;}
            }
            break;

            case SUB_POINTS_UL:
            {
                int[] ia = ints.get(spriteNum);
                ia[0] = mouseX;
                ia[1] = mouseY;

                if(mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                {subState = SUB_POINTS_DR;}
            }
            break;

            case SUB_POINTS_DR:
            {
                int[] ia = ints.get(spriteNum);
                ia[2] = Math.max(0, mouseX - ia[0]);
                ia[3] = Math.max(0, mouseY - ia[1]);

                if(mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                {
                    if(spriteNum >= sprites.size())
                    {
                        sprites.add(new Sprite(currentSheet, ia[0], ia[1], ia[2], ia[3]));
                    }
                    else
                    {
                        sprites.add(spriteNum,
                        new Sprite(currentSheet, ia[0], ia[1], ia[2], ia[3]));
                    }
                    //
                    lastWidth = ia[2]; lastHeight = ia[3];
                    subState = SUB_BASE;
                }
            }
            break;

            case SUB_DUP:
            {
                if(mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                {
                    int[] ia = new int[]{mouseX, mouseY, lastWidth, lastHeight};
                    //
                    ints.add(ia);
                    sprites.add(new Sprite(currentSheet, ia[0], ia[1], ia[2], ia[3]));
                    //
                    spriteNum = ints.size()-1;
                }

                if(controller.isKeyPressed(GLFW_KEY_C)){subState = SUB_BASE;}
            }
            break;
        }
    }

    public int loopTo = -1;
    public List<Sprite> animSprites = new ArrayList<Sprite>();
    public List<Short> animSpriteIDs = new ArrayList<Short>();
    public List<@fixed Integer> f_rates = new ArrayList<@fixed Integer>();
    public List<short[]> offsets = new ArrayList<short[]>();
    public List<Short> actionNumbers = new ArrayList<Short>();

    private Functional_FrameAnimation preview = null;
    public int currentAnimFrame = 0;
    public int animOffset = 0;

    private @fixed int f_lastRate = f_ONE;
    private short lastOX = 0, lastOY = 0,
    lastAction = 0;

    /**Animation update.*/
    public void animation_Update()
    {
        if(animSprites.size() > 0)
        {
            //Delete Frame
            if(controller.isKeyPressed(GLFW_KEY_BACKSPACE))
            {
                animSprites.remove(currentAnimFrame);
                animSpriteIDs.remove(currentAnimFrame);
                f_rates.remove(currentAnimFrame);
                offsets.remove(currentAnimFrame);
                actionNumbers.remove(currentAnimFrame);
                //
                if(loopTo == currentAnimFrame){loopTo = -1;}
                currentAnimFrame = animSprites.size()-1;
                //
                resetPreview();
            }

            //Copy Frame
            else if(controller.isKeyPressed(GLFW_KEY_C))
            {
                if(currentAnimFrame == animSprites.size())
                {
                    currentAnimFrame++;
                    //
                    animSprites.add(currentAnimFrame, animSprites.get(currentAnimFrame));
                    animSpriteIDs.add(currentAnimFrame, animSpriteIDs.get(currentAnimFrame));
                    f_rates.add(currentAnimFrame, f_rates.get(currentAnimFrame));
                    offsets.add(currentAnimFrame, offsets.get(currentAnimFrame));
                    actionNumbers.add(currentAnimFrame, actionNumbers.get(currentAnimFrame));
                }
                else
                {
                    animSprites.add(sprites.get(spriteNum));
                    animSpriteIDs.add((short)spriteNum);
                    f_rates.add(f_lastRate);
                    offsets.add(new short[]{lastOX, lastOY});
                    actionNumbers.add(lastAction);
                }
                if(animSprites.size() > 8){animOffset++;}
                //
                resetPreview();
                return;
            }

            //Set LoopTo
            if(controller.isKeyPressed(GLFW_KEY_L) && animSprites.size() > 0)
            {
                if(loopTo == currentAnimFrame){loopTo = -1;}
                else{loopTo = currentAnimFrame;}
                //
                resetPreview();
            }

            //Set Sprite
            if(controller.isKeyPressed(GLFW_KEY_S))
            {
                animSprites.set(currentAnimFrame, sprites.get(spriteNum));
                animSpriteIDs.set(currentAnimFrame, (short)spriteNum);
                //
                resetPreview();
            }

            //Set Rate
            if(controller.isKeyPressed(GLFW_KEY_R))
            {
                controller.beginTyping(textBuffer, 32);
                currentState = STATE_PROMPT_RATE;
                return;
            }

            //Set OffsetX
            else if(controller.isKeyPressed(GLFW_KEY_X))
            {
                controller.beginTyping(textBuffer, 32);
                currentState = STATE_PROMPT_OFFSETX;
                return;
            }

            //Set OffsetY
            else if(controller.isKeyPressed(GLFW_KEY_Y))
            {
                controller.beginTyping(textBuffer, 32);
                currentState = STATE_PROMPT_OFFSETY;
                return;
            }

            //Set Action Number
            else if(controller.isKeyPressed(GLFW_KEY_A))
            {
                controller.beginTyping(textBuffer, 3);
                currentState = STATE_PROMPT_ACTION;
                return;
            }
        }
        

        //Add Frame
        if(controller.isKeyPressed(GLFW_KEY_ENTER) && sprites.size() > 0)
        {
            if(currentAnimFrame == animSprites.size())
            {
                animSprites.add(sprites.get(spriteNum));
                animSpriteIDs.add((short)spriteNum);
                f_rates.add(f_lastRate);
                offsets.add(new short[]{lastOX, lastOY});
                actionNumbers.add(lastAction);
            }
            else
            {
                currentAnimFrame++;

                animSprites.add(currentAnimFrame, sprites.get(spriteNum));
                animSpriteIDs.add(currentAnimFrame, (short)spriteNum);
                f_rates.add(currentAnimFrame, f_lastRate);
                offsets.add(currentAnimFrame, new short[]{lastOX, lastOY});
                actionNumbers.add(currentAnimFrame, lastAction);
            }
            //
            if(animSprites.size() > 8){animOffset++;}
            resetPreview();
        }

        //Scroll
        if(!ui.intersects_components(mouse))
        {
            if(animSprites.size() > 8)
            {
                byte scroll = mouse.getScroll();
                //
                if(scroll < 0){animOffset = (animOffset+1) % animSprites.size();}
                else if(scroll > 0){animOffset = ((animOffset-1) + animSprites.size()-8) % (animSprites.size()-8);}
            }
            else{animOffset = 0;}
        }

        ui.update(0, 0, true, true);
        if(preview != null){preview.update(f_ONE);}
    }

    /**Resets the animation preview.*/
    public void resetPreview()
    {
        if(animSprites.size() <= 0)
        {
            preview = null;
            return;
        }
        //
        Sprite[] s = new Sprite[animSprites.size()];
        @fixed int[] f_r = new @fixed int[f_rates.size()];
        short[][] o = new short[offsets.size()][2];
        Action[] as = new Action[actionNumbers.size()];
        short[] a = new short[actionNumbers.size()];
        //
        for(int i = 0; i < f_rates.size(); i++)
        {
            s[i] = animSprites.get(i);
            f_r[i] = f_rates.get(i);
            //
            if(f_r[i] == 0)
            {
                preview = null;
                return;
            }
            //
            o[i] = offsets.get(i);
            as[i] = Functional_FrameAnimation::doNothing;
            a[i] = actionNumbers.get(i);
        }
        //
        preview = new Functional_FrameAnimation(loopTo, s, f_r, o, as, a);
    }



    private byte saveTime = 0, NOtime = 0;

    /**Saves the current Sprite Layout to a .dat file.*/
    public void saveLayout()
    {
        if(ints.size() <= 0){return;}

        //Create a new File instance.
        File file = new File(editPath + name + "_LYT.dat");

        try
        {
            //Set up the output stream.
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            
            byte[] bytes = new byte[(ints.size() * 4) * 4];
            for(int i = 0; i < ints.size(); i++)
            {
                //Technically i * 16.
                int slot = i << 4;

                //Retrive the current int array.
                int[] intArray = ints.get(i);

                //Go through each int in the array.
                for(int s = 0; s < intArray.length; s++)
                {
                    int sTimes4 = s << 2;
                    int currentInt = intArray[s];

                    //Convert the int to 4 bytes.
                    bytes[slot + sTimes4]       = (byte)((currentInt & 0xFF000000) >> 24);
                    bytes[slot + sTimes4 + 1]   = (byte)((currentInt & 0x00FF0000) >> 16);
                    bytes[slot + sTimes4 + 2]   = (byte)((currentInt & 0x0000FF00) >> 8);
                    bytes[slot + sTimes4 + 3]   = (byte)(currentInt & 0x000000FF);

                    //System.out.println
                    //(
                        //String.format("%x", bytes[slot + sTimes4]) + " " +
                        //String.format("%x", bytes[slot + sTimes4+1]) + " " +
                        //String.format("%x", bytes[slot + sTimes4+2]) + " " +
                        //String.format("%x", bytes[slot + sTimes4+3])
                    //);
                }
            }

            //Write all the bytes to the file.
            bos.write(bytes);

            //Output the file.
            file.createNewFile();

            //Close the output stream.
            bos.close();
            fos.close();

            saveTime = 120;
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}
    }


    /**Saves the current Animation to a .dat file.*/
    public void saveAnimation()
    {
        if(animSprites.size() <= 0){return;}

        //Create a new File instance.
        File file = new File(editPath + name + "_ANM.dat");

        try
        {
            //Set up the output stream.
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            
            byte[] bytes = new byte[4 + (Functional_FrameAnimation.BYTES_PER_FRAME * animSprites.size())];

            //Write loopTo.
            bytes[0] = (byte)((loopTo & 0xFF000000) >> 24);
            bytes[1] = (byte)((loopTo & 0x00FF0000) >> 16);
            bytes[2] = (byte)((loopTo& 0x0000FF00) >> 8);
            bytes[3] = (byte)(loopTo & 0x000000FF);

            for(int i = 0; i < animSprites.size(); i++)
            {
                int slot = 4 + (i * Functional_FrameAnimation.BYTES_PER_FRAME);

                //SpriteID (Short)
                bytes[slot]     = (byte)((animSpriteIDs.get(i) & 0xFF00) >> 8);
                bytes[slot + 1] = (byte)(animSpriteIDs.get(i) & 0x00FF);

                //Rate (Float)
                //int f = Float.floatToIntBits(f_rates.get(i));
                int f_r = f_rates.get(i);

                bytes[slot + 2] = (byte)((f_r & 0xFF000000) >> 24);
                bytes[slot + 3] = (byte)((f_r & 0x00FF0000) >> 16);
                bytes[slot + 4] = (byte)((f_r & 0x0000FF00) >> 8);
                bytes[slot + 5] = (byte)(f_r & 0x000000FF);

                //Offset (Short, Short)
                short[] s = offsets.get(i);

                bytes[slot + 6] = (byte)((s[0] & 0xFF00) >> 8);
                bytes[slot + 7] = (byte)(s[0] & 0x00FF);

                bytes[slot + 8] = (byte)((s[1] & 0xFF00) >> 8);
                bytes[slot + 9] = (byte)(s[1] & 0x00FF);

                //ActionNumber (Short)
                bytes[slot + 10]    = (byte)((actionNumbers.get(i) & 0xFF00) >> 8);
                bytes[slot + 11]    = (byte)(actionNumbers.get(i) & 0x00FF);
            }

            //Write all the bytes to the file.
            bos.write(bytes);

            //Output the file.
            file.createNewFile();

            //Close the output stream.
            bos.close();
            fos.close();

            saveTime = 120;
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}
    }


    @Override
    public void end()
    {

    }

    public Font arial = Fonts.get("Arial");

    private Vector4f bgColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
    cursorColor = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);
    private SpriteSheet background = new SpriteSheet(640, 360, 0xffFFFFFF);
    private Sprite rect = new Sprite(background, 0, 0, 1, 1);
    private float t0 = 0;

    private Vector4f bla = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
    raa = new Vector4f(1.0f, 0.2f, 0.2f, 1.0f);

    private float t1 = 0;
    private Vector4f fontColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    @Override
    public void render(Screen screen)
    {
        t0 = (float)((t0 + 0.005f) % (2.0f * Math.PI));
        float f = (float)Math.abs(Math.sin(t0) * 0.2f);
        bgColor.set(f, f, f, 1.0f);

        t1 = (float)((t1 + 0.05f) % (2.0f * Math.PI));
        f = (float)Math.abs(Math.sin(t1) * 0.7f);
        fontColor.set(0.5f, f, 1.0f, 1.0f);

        screen.renderSheet(0, 0, 0, background, bgColor, false);
        screen.setCameraOffsets((int)(position.x * scale), (int)(position.y * scale), 0);

        switch(currentState)
        {
            case STATE_STARTUP:
            {
                //Render Text
                arial.render(screen, 90, 100, "Welcome to the JettersR Sprite Layout and Animation Editor.", false);

                //Render MenuChoices
                startUp_Menu.render(screen);
            }
            break;

            case STATE_LOAD_LAYOUT:
            arial.render(screen, 140, (screen.getHeight()/2) - (arial.getLineSpace() * 2),
            "Use the dialog to\n" +
            "  load a Layout.",
            3.0f, false);
            break;

            case STATE_LOAD_SHEET:
            arial.render(screen, 120, (screen.getHeight()/2) - (arial.getLineSpace() * 2),
            "  Use the dialog to\n" +
            "load a SpriteSheet.",
            3.0f, false);
            break;

            case STATE_EDIT_LAYOUT:
            {
                //Sheet
                screen.renderSprite_Sc(0, 0, sheetAsSprite, Sprite.FLIP_NONE, scale, scale, true);

                //Cursor
                if(!ui.intersects_components(mouse))
                {
                    screen.renderSprite_Sc((int)(mouseX * scale), (int)(mouseY * scale),
                    rect, Sprite.FLIP_NONE, cursorColor, scale, scale, true);
                }

                //Rects
                for(int i = 0; i < ints.size(); i++)
                {
                    int[] ia = ints.get(i);
                    //
                    if(i != spriteNum)
                    {
                        screen.drawRect((int)(ia[0] * scale), (int)(ia[1] * scale),
                        (int)(ia[2] * scale), (int)(ia[3] * scale), bla, true);
                    }
                }
                if(ints.size() > 0)
                {
                    int[] ia = ints.get(spriteNum);
                    //
                    screen.drawRect((int)(ia[0] * scale), (int)(ia[1] * scale),
                    (int)(ia[2] * scale), (int)(ia[3] * scale), raa, true);
                }

                //State Stuff
                switch(subState)
                {
                    case SUB_POINTS_UL:
                    {
                        int[] ia = ints.get(spriteNum);
                        arial.render(screen, 100, 330, "Select Upper Left Point " + ia[0] + " " + ia[1], fontColor, false);
                    }
                    
                    break;

                    case SUB_POINTS_DR:
                    {
                        int[] ia = ints.get(spriteNum);
                        arial.render(screen, 100, 330, "Select Lower Right Point " + ia[2] + " " + ia[3], fontColor, false);
                    }
                    break;

                    case SUB_DUP:
                    {
                        screen.drawRect((int)(mouseX * scale), (int)(mouseY * scale),
                        (int)(lastWidth * scale), (int)(lastHeight * scale), fontColor, true);
                        //
                        arial.render(screen, 550, 330, lastWidth + " " + lastHeight, fontColor, false);
                    }
                    break;

                    default: break;
                }


                //UI
                ui.render(screen);
            }
            break;

            case STATE_areYouSure_LAYOUT:
            {
                //Render Text
                arial.render(screen, 90, 100, "Are you sure you want to get rid of the layout currently here?", false);

                //Render MenuChoices
                areYouSure_Layout_menu.render(screen);
            }
            break;

            case STATE_LOAD_ANIM:
            arial.render(screen, 120, (screen.getHeight()/2) - (arial.getLineSpace() * 2),
            "  Use the dialog to\n" +
            "load a Animation.",
            3.0f, false);
            break;

            case STATE_EDIT_ANIM:
            {
                //Tile Area
                screen.fillRect(392, 192, 16, 16, raa, false);

                //Lines
                screen.drawLine(400, 0, 400, screen.getHeight(), Screen.DEFAULT_BLEND, false);
                screen.drawLine(0, 200, screen.getWidth(), 200, Screen.DEFAULT_BLEND, false);

                //UI
                ui.render(screen);

                if(preview != null)
                {
                    arial.render(screen, 400, 72, Integer.toString(preview.getFrame()), false);
                    screen.renderSprite(400 + preview.getOffsetX(), 200 + preview.getOffsetY(), preview.getSprite(), Sprite.FLIP_NONE, false);
                }
            }
            break;

            case STATE_PROMPT_RATE:
            case STATE_PROMPT_OFFSETX:
            case STATE_PROMPT_OFFSETY:
            case STATE_PROMPT_ACTION:
            {
                arial.render(screen, 280, (screen.getHeight()/2) - (arial.getLineSpace() * 2) - 70, "Type", 2.0f, false);
                //
                arial.render(screen, 120, (screen.getHeight()/2) - (arial.getLineSpace() * 2), textBuffer.toString(), 3.0f, false);
            }
            break;
        }

        if(saveTime > 0)
        {
            arial.render(screen, 450, 72, "\"" + name + "\" saved.", fontColor, false);
            saveTime--;
        }

        if(NOtime > 0)
        {
            arial.render(screen, 250, 150, "NO", 4.0f, false);
            NOtime--;
        }
    }

    private static String imageIconPath = "src/JettersRDevTools/AnimationEditor/AE_ImageIcon.png";

    public static void main(String[] args)
    {
        Game game = Game.instantiate(Main.SOFTWARE, Game.NORMAL_WIDTH, Game.NORMAL_HEIGHT);
        game.setImageIconPath(imageIconPath);
        game.start(new AnimationEditor(game.getGameStateManager()));
    }
}
