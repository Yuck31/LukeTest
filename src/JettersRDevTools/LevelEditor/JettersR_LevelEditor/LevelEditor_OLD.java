package JettersRDevTools.LevelEditor.JettersR_LevelEditor;
/**
 * Level Editor GameState.
 * 
 * Everything Level Editor related is placed outside of the base game
 * so that if this was removed from the project, the base game will stiil
 * work just fine.
 * 
 * This is effectivly my propriatary Level Editor for JettersR.
 * I could design my levels in Piskel, but that would be annoying.
 * So, I made this to easily visualize my levels as I make them.
 * This is also how I place entities in the levels.
 * 
 * Author: Luke Sullivan
 * Last Edit: 8/28/2023
 */
import java.util.List;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.util.ArrayList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import org.joml.Vector4f;
//import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

import JettersR.Game;
import JettersR.LevelCamera;
import JettersR.Controller;
import JettersR.Mouse;
import JettersR.Level;
import JettersR.Tiles.*;
import JettersR.Tiles.Graphics.*;
import JettersR.UI.Menus.*;
import JettersR.UI.Menus.MenuChoices.Labeled_MenuChoice;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI.LE_TileCreator;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI.LE_EditLevelUI;
import JettersR.Entities.Entity;
import JettersR.Graphics.*;
import JettersR.GameStates.GameState;
import JettersR.GameStates.GameStateManager;
import JettersR.Util.fixedVector3;
//import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class LevelEditor_OLD extends GameState
{
    public class TileSet
    {
        private String name;
        private Sprite[] sprites;
        private int count = 0;

        public TileSet(String name, Sprite[] sprites)
        {
            this.name = name;
            this.sprites = sprites;
        }

        public void add(){count++;}
        public void subtract(){count--;}
        public int getCount(){return count;}

        public String getName(){return name;}
        public Sprite[] getSprites(){return sprites;}
    }
    public List<TileSet> tileSets = new ArrayList<TileSet>();
    
    //Edited Levels Path.
    public static final String editedLevelsPath = "src/JettersRDevTools/LevelEditor/EditedLevels/";

    //Controller.
    public final Controller controller = Game.controller;

    //Mouse.
    public final Mouse mouse = Game.mouse;

    //Level.
    public Level level = new Level(4, 4, 4);

    //Name of Level.
    private String levelName = "Test";

    //Position of Camera
    private LevelCamera camera = null;
    private int floor = 0;

    //Position of mouse.
    private int mtX, mtY;

    //Tile Pallates.
    public final int[] tileMeshs = {0x00000000, 0x00000000},
    tileTypes = {0x00000000, 0x00000000};

    //TileType pallete.
    public static final int[] tileType_Pallete = new int[Tiles.getTileTypes_Length()];

    //Save stuff.
    private String saveString = "";
    public short saveTime = 0, NOtime = 0;

    //Arial Font.
    private Font arial = Fonts.get("Arial");

    //StartUp Menu.
    private Menu startUp_Menu = null;

    //EditLevel UI.
    private LE_EditLevelUI editLevelUI = null;

    //TileType Menu.
    private LE_TileCreator tileCreator = null;

    //Add Frame Menu.
    //private Menu addFrame_Menu = null;



    //States.
    public static final byte
    STATE_STARTUP = 0,
    STATE_LOADLEVEL = 1,
    STATE_EDITLEVEL = 2,
    STATE_LOADTILESET_IMGLYT = 3,
    STATE_TILECREATOR = 4;
    public byte currentState = STATE_STARTUP;

    //public SpriteSheet

    /**Constructor.*/
    public LevelEditor_OLD(GameStateManager gameStateManager)
    {
        super(gameStateManager);
        
        for(int i = 0; i < tileType_Pallete.length; i++)
        {tileType_Pallete[i] = (i << Tiles.TILE_RESPONSE_BITS);}
    }

    /** Starts Level Editor. */
    public void start()
    {
        //TODO Brainstorm how this is going to work.

        /*
         * Startup: |NEW LEVEL| |LOAD LEVEL|
         * 
         * NEW LEVEL: Select a folder in Images/TileSets to start a TileSet with.
         * -FileChooser in Images/TileSets/ to choose a Folder containing a SpriteSheet and SheetLayout.
         * -Upon selection, load the sheet and layout and create a new blank level.
         * 
         * LOAD LEVEL: Select a Level to load.
         * -FileChooser in LevelEditor/EditedLevels to choose a folder containing level data.
         * -Must contain tile, tileset, and entity data.
         */
        
        //Load Level Editor Sprites.
        LE_Sprites.load();

        //Make StartUp menu.
        startUp_Menu = new MouseMenu(new AAB_Box2D(LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT));
        startUp_Menu.addComponent
        (
            new Labeled_MenuChoice(257, 270, 326, 100,
            () -> 
            {
                camera = level.getCamera();
                camera.setBounded(false);
                currentState = STATE_EDITLEVEL;
                mouse.resetScroll();
            },
            new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
            "NEW LEVEL", 3.0f)
        )
        .addComponent
        (
            new Labeled_MenuChoice(639, 270, 326, 100,
            () -> 
            {
                load();
                mouse.resetScroll();
            },
            new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
            "LOAD LEVEL", 3.0f)
        );

        //Make EditLevelUI
        editLevelUI = new LE_EditLevelUI(this);

        //Make TileSprite Type Menu.
        //tileCreator = new LE_TileCreator(this);
    }

    /** Loads a Level from the given Directory. */
    public void load()
    {
        //Set currentState to LOADLEVEL.
        currentState = STATE_LOADLEVEL;

        //Create Level Folder Selection UI on a Seperate Thread.
        new Thread(() ->
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(editedLevelsPath));

            fileChooser.setDialogTitle("Select a Level Folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.requestFocus();

            //Show it and wait for the user to select a Folder. If none was selected, cancel.
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                level = new Level(fileChooser.getSelectedFile());
                camera = level.getCamera();
                currentState = STATE_EDITLEVEL;
            }
            else{currentState = STATE_STARTUP;}
        }).start();
    }



    
    //
    //Saving.
    //

    @Deprecated
    /**
     * Saves the Entities in the given Level.
     * 
     * @param level the level who's entities need saving.
     * @param levelName the name of the level.
     * @throws IOException if no folder path was made...
     */
    public static void saveEntities(Level level, String levelName) throws IOException
    {
        //Get List of Entities.
        final ArrayList<Entity> entities = level.getEntities();

        //Create ObjectMapper.
        ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.writerWithDefaultPrettyPrinter();
        //objectMapper.writerFor(new TypeReference<ArrayList<Entity>>() {});
        objectMapper.writerFor(new TypeReference<Entity>() {});
        //objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        //This sets objectMapper to serialize only non-transient fields of any visibility, rather than fields with getters.
        objectMapper.setVisibility
        (
            objectMapper.getSerializationConfig().
            getDefaultVisibilityChecker().
            withFieldVisibility(JsonAutoDetect.Visibility.ANY).
            withGetterVisibility(JsonAutoDetect.Visibility.NONE).
            withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );

        //This is used to manage Entity inheritance, so that way, I don't need an ECS.
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("JettersR.Entities")
        //.allowIfSubType("JettersR.Util")
        .build();

        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        //Allows deserialization of Objects, Abstract types, and Arrays/Lists of them.
        //objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);


        //Create and Output File.
        File file = new File(editedLevelsPath + levelName + "/ENTITIES.json");
        file.createNewFile();

        //Write the entire list of Entities to the file.
        objectMapper.writeValue(file, entities);
        //for(int i = 0; i < entities.size(); i++){objectMapper.writeValue(file, entities.get(i));}
        //objectMapper.writeValue(g, value);

        //file.createNewFile();
    }

    
    @Deprecated
    /**Save the Level's tileData in the map to .png files.*/
    public static void saveTileData(Level level, String levelName) throws IOException
    {
        //Cache size of level width and height.
        int size = level.getWidth() * level.getHeight();

        for(int z = 0; z < level.getDepth(); z++)
        {
            //Create pixel array.
            int[] pixels = new int[size];

            //Go through each tile on this floor.
            for(int y = 0; y < level.getHeight(); y++)
            {
                for(int x = 0; x < level.getWidth(); x++)
                {
                    //Put the tileData at this spot into the pixel array.
                    pixels[x + y * level.getWidth()] = level.getTileInfo(x, y, z);
                    //0xFF000000 | (level.getTileInfo(x, y, z) & 0x00FFFFFF);
                }
            }

            //Create an empty image to put the current floor's pixels into.
            BufferedImage image = new BufferedImage(level.getWidth(), level.getHeight(), BufferedImage.TYPE_INT_ARGB);

            //Set the colors of the image to the colors in the floor array.
            image.setRGB(0, 0, level.getWidth(), level.getHeight(), pixels, 0, level.getWidth());

            //This is the path used to save the maps into.
            String path = editedLevelsPath + levelName + "/TileData/" + String.format("%03d", z) + ".png";

            //Use the path to make a new file.
            File outputFile = new File(path);

            //Create any folders within the path.
            outputFile.mkdirs();

            //Output the file the the folder.
            outputFile.createNewFile();

            //Write the pixel data to the file in PNG format.
            ImageIO.write(image, "png", outputFile);
        }
    }


    @Deprecated
    /**Saves a Tile Key of all the tilesets, tileMeshs, and TileAnimations used in the current level.*/
    public static void saveTileKey(Level level, String levelName, Sprite[][] tileSet_Sprites, String[] tileSet_Names, int[] tileSet_Counts)
    {
        //Get all the TileSprites from the Level.
        //final TileSprite[] tileMeshs = level.getTileSprites();
        int totalCount = 0;

        //Create two new File instances.
        File tileSets_file = new File(editedLevelsPath + levelName + "/TILESETS.txt");
        File tileKey_file = new File(editedLevelsPath + levelName + "/TILEKEY.dat");

        try
        {
            //Set up the writer for the TileSet names.
            FileWriter fw = new FileWriter(tileSets_file);
            BufferedWriter bw = new BufferedWriter(fw);

            //Set up the output stream for the TileKey.
            FileOutputStream fos = new FileOutputStream(tileKey_file);
            BufferedOutputStream bos = new BufferedOutputStream(fos); 
            
            //
            //Go through the TileSets.
            //
            for(int t = 0; t < tileSet_Sprites.length; t++)
            {
                //Write the tileSet's name to the TileSet names file.
                bw.write(tileSet_Names[t]);
                bw.newLine();

                //Cache the current set's sprites for TileSprite saving.
                Sprite[] ts_Sprites = tileSet_Sprites[t];
                //System.out.println(ts_Sprites);
                
                //
                //Write every tile in this TileSet to the TileKey file.
                //
                final int ts_count = tileSet_Counts[t];
                for(int i = 0; i < ts_count; i++)
                {
                    //Increment totalCount and translate the TileSprite to byte data.
                    byte[] tileMeshData = level.getTileMeshs(++totalCount).translateTo_DAT(ts_Sprites);

                    //Write to the file.
                    bos.write(tileMeshData);
                }

                //Prepare for the next TileSet if there are more.
                if(t < tileSet_Sprites.length-1){bos.write((byte)0);}

                //Otherwise, prepare for tileAnimations if needed.
                else if(level.getNumTileAnimations() > 0)
                {
                    System.out.println(t + " " + tileSet_Sprites.length);
                    bos.write((byte)-1);
                }
            }


            //
            //Write all TileAnimations in the level.
            //
            for(int i = 0; i < level.getNumTileAnimations(); i++)
            {
                //Translate the TileAnimation to byte data.
                byte[] tileAnimData = level.getTileAnimation(i).translateTo_DAT(level, i);

                //Write to the file.
                bos.write(tileAnimData);
            }

            //Output the files.
            tileSets_file.createNewFile();
            tileKey_file.createNewFile();

            //Close the writer.
            bw.close();
            fw.close();

            //Close the output stream.
            bos.close();
            fos.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}
    }


    @Deprecated
    /**Saves the Level to a Folder.*/
    public void save()
    {
        //Rename Dialog
        String levelName = (String)JOptionPane.showInputDialog(null,
            "If you want, you can update the Level Name.", "Change Level Name?",
            JOptionPane.PLAIN_MESSAGE, null, null, this.levelName);
        if(levelName == null)
        {
            saveString = "CANCELLED";
            saveTime = 180;
            return;
        }

        //Proceed with Save.
        try
        {
            //
            //Save Entities.
            //
            saveEntities(this.level, this.levelName);


            //
            //Save TileData to .png files.
            //
            saveTileData(this.level, this.levelName);


            //
            //Save TileKey (DAT).
            //

            //Create arrays to get Data from TileSets.
            Sprite[][] ts_sprites = new Sprite[tileSets.size()][];
            String[] ts_names = new String[tileSets.size()];
            int[] ts_counts = new int[tileSets.size()];

            //Fill arrays with data.
            for(int i = 0; i < ts_sprites.length; i++)
            {
                TileSet ts = tileSets.get(i);

                ts_sprites[i] = ts.getSprites();
                ts_names[i] = ts.getName();
                ts_counts[i] = ts.getCount();
            }

            //Run save function.
            saveTileKey(this.level, this.levelName, ts_sprites, ts_names, ts_counts);

            

            //If we reached this point, then the save was successful. Return.
            saveString = "SUCCESS!";
            saveTime = 180;
            return;
        }
        catch(JsonGenerationException e){e.printStackTrace();}
        catch(JsonMappingException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}

        //An Exception happened. Save failed.
        saveString = "FAIL...";
        saveTime = 180;
    }



    /**Ends Level Editor.*/
    public void end()
    {

    }

    public Level getLevel(){return level;}

    @Deprecated
    /**Loads a folder containing a tileset image and layout.*/
    public void loadTileSet_ImageAndLayout()
    {
        currentState = LevelEditor_OLD.STATE_LOADTILESET_IMGLYT;

        //Create SpriteSheet Selection UI on a Seperate Thread.
        new Thread(() ->
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(Tiles.TILESETS_PATH));

            fileChooser.setDialogTitle("Select a TileSet folder.");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.requestFocus();

            //Show it and wait for the user to select a Folder. If none was selected, cancel.
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                File folder = fileChooser.getSelectedFile();
                File[] files = folder.listFiles();

                //Sheet and Sprite layout.
                SpriteSheet sheet = null;
                Sprite[] sprites = null;

                //TileSet name.
                String tileSetName;

                //Account for having selected a child folder.
                if(!folder.getParent().equalsIgnoreCase(Tiles.TILESETS_PATH_0))
                {tileSetName = folder.getParent() + folder.getName();}

                //Otherwise, set normally.
                else{tileSetName = folder.getName();}
                //
                for(int i = 0; i < files.length; i++)
                {
                    File f = files[i];
                    //
                    if(f.getName().equals("IMG.png"))
                    {
                        //Load Image.
                        sheet = new SpriteSheet(f);//, f.getName());
                    }
                    else if(f.getName().equals("LYT.dat"))
                    {
                        //Load Layout.
                        sprites = sheet.loadLayout(f);
                    }
                }
                //
                tileSets.add(new TileSet(tileSetName, sprites));
                currentState = STATE_EDITLEVEL;
            }
            else{currentState = STATE_EDITLEVEL;}
        }).start();
    }

    //Used to disable use of Tile Placment when using the UI.
    private boolean UILocked = false;
    public void setUILocked(boolean UILocked){this.UILocked = UILocked;}

    //Switch between placing Tiles or Entities.
    public boolean tilePlacement = true;

    /**Updates the Level Editor every frame.*/
    public void update()
    {
        switch(currentState)
        {
            case STATE_STARTUP:
            startUp_Menu.update(0, 0, true, true);
            break;

            case STATE_LOADLEVEL:
            //...This is just here, I guess.
            break;

            case STATE_EDITLEVEL:
            update_EditLevel();
            break;

            case STATE_LOADTILESET_IMGLYT:
            //We wait...
            break;

            case STATE_TILECREATOR:
            //tileCreator.update(0, 0, true, true);
            break;
        }
    }


    /**Edit Level function.*/
    private void update_EditLevel()
    {
        if(tileSets.size() <= 0)
        {
            loadTileSet_ImageAndLayout();
            return;
        }

        //Check if mouse intersects with any of the UI.
        boolean uiIntersects = editLevelUI.intersects_components(mouse);

        //Update Level to respond to input
        level.update();

        byte scroll  = mouse.getScroll_Continue();

        //If the Mouse isn't hover over any UI elements, 
        if(!UILocked && !uiIntersects)
        {
            fixedVector3 f_position = camera.f_getPosition();

            //Toggle Show Collision
            if(controller.isKeyPressed(GLFW_KEY_L)){level.toggleShowCollision();}

            //Move Fast Check.
            int xa = fixed(16), ya = fixed(16);
            boolean shift = controller.isKeyHeld(GLFW_KEY_LEFT_SHIFT);
            if(shift){xa = 32; ya = 32;}

            //Horizontal Scroll
            if(controller.isKeyHeld(GLFW_KEY_A)){f_position.x -= xa;}
            if(controller.isKeyHeld(GLFW_KEY_D)){f_position.x += xa;}

            //Vertical Scroll
            if(controller.isKeyHeld(GLFW_KEY_W)){f_position.y -= ya;}
            if(controller.isKeyHeld(GLFW_KEY_S)){f_position.y += ya;}

            if(f_position.x < -512){f_position.x = -512;}
            else if(f_position.x > (level.getWidth() << Level.TILE_BITS)){f_position.x = (level.getWidth() << Level.TILE_BITS);}
            //
            if(f_position.y < -512){f_position.y = -512;}
            else if(f_position.y > (level.getHeight() << Level.TILE_BITS)){f_position.y = (level.getHeight() << Level.TILE_BITS);}

            //Z Scroll
            if(controller.isKeyPressed(GLFW_KEY_SPACE))
            {
                floor = (floor+1) % level.getDepth();
                f_position.z = (floor << Level.TILE_BITS);
            }
            if(controller.isKeyPressed(GLFW_KEY_LEFT_CONTROL))
            {
                floor = ((floor-1)+level.getDepth()) % level.getDepth();
                f_position.z = (floor << Level.TILE_BITS);
            }

            //Zoom.
            if(scroll > 0)
            {
                level.addScale(0.1f, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT);
                mouse.setScroll((byte)0);
            }
            else if(scroll < 0)
            {
                level.addScale(-0.1f, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT);
                mouse.setScroll((byte)0);
            }

            //Tile Placment.
            if(tilePlacement)
            {
                //Mouse Position
                mtX = (int)((f_position.x + (mouse.getX()/level.getScale())) / Level.TILE_SIZE) << Level.TILE_BITS;
                mtY = (int)((f_position.y - (f_position.z/2) + (mouse.getY()/level.getScale())) / Level.TILE_SIZE) << Level.TILE_BITS;
                //
                int mtX0 = mtX >> Level.TILE_BITS, mtY0 = mtY >> Level.TILE_BITS;

                if(mouse.buttonHeld(GLFW_MOUSE_BUTTON_LEFT))
                {
                    //Place Left Click Tile.
                    if(level.isShowingCollision())
                    {
                        level.setTileType(mtX0, mtY0, floor, tileTypes[0]);
                        System.out.println(level.getTileType(mtX0, mtY0, floor));
                    }
                    else
                    {
                        level.setTileMesh(mtX0, mtY0, floor, tileMeshs[0]);
                        System.out.println(level.getTileMeshID(mtX0, mtY0, floor));
                    }
                }
                else if(mouse.buttonHeld(GLFW_MOUSE_BUTTON_RIGHT))
                {
                    //Place Right Click Tile.
                    if(level.isShowingCollision()){level.setTileType(mtX0, mtY0, floor, tileTypes[1]);}
                    else{level.setTileMesh(mtX0, mtY0, floor, tileMeshs[1]);}
                }

                //Tile Picker.
                if(mouse.buttonPressed(GLFW_MOUSE_BUTTON_MIDDLE))
                {
                    //Set Left Click Tile to Clicked Tile.
                    if(level.isShowingCollision()){tileTypes[0] = level.getTileType(mtX0, mtY0, floor);}
                    else{tileMeshs[0] = level.getTileMeshID(mtX0, mtY0, floor);}
                }
            }

            //Entity Placment.
            else
            {
                //Mouse Position
                mtX = (int)(f_position.x + mouse.getX());
                mtY = (int)(f_position.y - (f_position.z/2) + mouse.getY());
                //
                if(shift)
                {
                    mtX = (int)(mtX / Level.TILE_SIZE) << Level.TILE_BITS;
                    mtY = (int)(mtY / Level.TILE_SIZE) << Level.TILE_BITS;
                }
                //
                if(mouse.buttonPressed(GLFW_MOUSE_BUTTON_LEFT))
                {
                    //Place Entity.
                }
                else if(mouse.buttonHeld(GLFW_MOUSE_BUTTON_RIGHT))
                {
                    //Move currently selected Entity.
                }

                //Entity Picker.
                if(mouse.buttonPressed(GLFW_MOUSE_BUTTON_MIDDLE))
                {
                    //Set Entity to place to selected Entity.
                }
            }
        }
        else
        {
            //System.out.println("Wat");
        }

        editLevelUI.update(0, 0, true, true);
    }

    //private void resetFramePreview()
    //{fPreviewTimer = new FrameAnimation_Timer(0, currentFloorSprites.size(), currentFrameRate);}

    private Font debug = Fonts.get("Debug");
    private Vector4f cyAN = new Vector4f(0.0f, 1.0f, 1.0f, 1.0f);

    private Vector4f cursorColor = new Vector4f(1.0f, 1.0f, 1.0f, 0.5f);

    /**Renders the Level Editor.*/
    public void render(Screen screen)
    {
        switch(currentState)
        {
            case STATE_STARTUP:
            {
                //Render Welcome Text.
                arial.render(screen, 20, 100, "Welcome to the JettersR Level Editor.", 3.0f, false);

                //Render StartUp Menu.
                startUp_Menu.render(screen);
            }
            break;

            case STATE_LOADLEVEL:
            {
                //Render this text while waiting for the user to finish using the JFileChooser.
                arial.render(screen, 140, (screen.getHeight()/2) - (arial.getLineSpace() * 2),
                "Use the dialog to`" +
                "  load a Level.",
                3.0f, false);
            }
            break;

            case STATE_EDITLEVEL:
            {
                //Render Level based on Position, Floor Toggles.
                level.render(screen);
                //
                float scale = level.getScale();
                screen.drawRect(0, (int)-(camera.f_getPosition().z/2),
                (int)((level.getWidth() << Level.TILE_BITS) * scale),
                (int)((level.getHeight() << Level.TILE_BITS) * scale),
                Screen.DEFAULT_BLEND, true);
                //
                debug.render(screen, 2, 10, "X " + camera.f_getPosition().x, cyAN, false);
                debug.render(screen, 2, 16, "Y " + camera.f_getPosition().y, cyAN, false);
                debug.render(screen, 2, 22, "Z " + camera.f_getPosition().z, cyAN, false);
    
                if(!editLevelUI.intersects_components(mouse))//, false))
                {
                    screen.fillRect((int)(mtX * scale), (int)(mtY * scale),
                    (int)(Level.TILE_SIZE * scale), (int)(Level.TILE_SIZE * scale),
                    cursorColor, true);
                }

                //Render Level Editor UI.
                editLevelUI.render(screen);
                
                //Render Selected TileSprites.
                //if(level.getTileSprites().length > 0)
                if(level.getNumTileMeshs() > 0)
                {
                    TileMesh ts = level.getTileMeshs(tileMeshs[0]);
                    if(ts != null)
                    {
                        screen.renderSprite(10, 400, ts.getSprites()[0], Sprite.FLIP_NONE, false);
                    }
                }

                //Render Save Text.
                if(saveTime > 0)
                {
                    arial.render(screen, 500, 16, "SAVE " + saveString, false);
                    saveTime--;
                }
            }
            break;

            case STATE_LOADTILESET_IMGLYT:
            {
                arial.render(screen, 100, 250, "Choose a TileSet Folder.", 3.0f, false);
            }
            break;

            case STATE_TILECREATOR:
            tileCreator.render(screen);
            break;
        }
        //
        if(NOtime > 0)
        {
            arial.render(screen, 250, 150, "NO", 4.0f, false);
            NOtime--;
        }
    }
}
