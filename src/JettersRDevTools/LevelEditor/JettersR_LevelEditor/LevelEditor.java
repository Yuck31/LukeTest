package JettersRDevTools.LevelEditor.JettersR_LevelEditor;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/25/2023
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector4f;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI.*;
import JettersR.*;
import JettersR.Graphics.*;
import JettersR.Entities.Entity;
import JettersR.UI.Menus.*;
import JettersR.UI.Menus.MenuChoices.Labeled_MenuChoice;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersR.Tiles.Tiles;
import JettersR.Tiles.Graphics.TileMesh;
import JettersR.GameStates.GameState;
import JettersR.GameStates.GameStateManager;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class LevelEditor extends GameState
{
	//Edited Levels Path.
    public static final String editedLevelsPath = "src/JettersRDevTools/LevelEditor/EditedLevels/";

	//Controller and Mouse for recieving input.
	private Controller controller;
	private Mouse mouse;
	//private byte mouseScroll = 0;

	//Current level being edited.
	private Level level;
	private String levelName;

	//Level camera pointer.
	private LevelCamera camera;
	private fixedVector3 f_cameraPosition;
	private int floor = 0;

	//Placement cursor position.
	private fixedVector3 f_cursorPosition = new fixedVector3();

	//Current Tiles to paint with.
	public int[] pickedTiles = new int[]{Level.tileValue(1, 0), Level.tileValue(1, 0)};
	

	//
	//TileSets.
	//
	public class TileSet
    {
		//Name of TileSet.
        private String name;

		//Sprites in TileSet.
        private Sprite[] sprites;

		//Because the TileMeshs from each TileSet is placed in one singular array,
        //I need this value to know how far in the array this TileSet starts.
        public int tileMeshOffset = 0;

		//Number of TileMeshs in TileSet.
        private int count = 0;

		/**Constructor.*/
        public TileSet(String name, Sprite[] sprites, int tileMeshOffset)
        {
            this.name = name;
            this.sprites = sprites;
			this.tileMeshOffset = tileMeshOffset;
        }

        public void add(){count++;}
        public void subtract(){count--;}
        public int getCount(){return count;}

        public String getName(){return name;}
		public Sprite getSprite(int  slot){return sprites[slot];}
        public Sprite[] getSprites(){return sprites;}
		public SpriteSheet getSheet(){return sprites[0].getSheet();}
    }
    public List<TileSet> tileSets = new ArrayList<TileSet>();



	//
	//Menus.
	//
	public static final Vector4f
	MENU_COLOR_0 = new Vector4f(0.2f, 0.2f, 1.0f, 1.0f),
	MENU_COLOR_1 = new Vector4f(1.0f, 0.2f, 0.2f, 1.0f),
	//
	SCROLL_HANDLE_COLOR_0 = new Vector4f(0.25f, 1.0f, 0.25f, 1.0f),
	SCROLL_HANDLE_COLOR_1 = new Vector4f(0.75f, 0.75f, 0.75f, 1.0f),
	SCROLL_BAR_COLOR = new Vector4f(0.2f, 0.2f, 0.2f, 1.0f),
	//
	INSPECTOR_FRAME_COLOR = new Vector4f(0.77f, 0.25f, 0.0f, 1.0f),
	INSPECTOR_COLOR_0 = new Vector4f(0.16f, 0.3f, 0.0f, 1.0f),
	INSPECTOR_COLOR_1 = new Vector4f(0.17f, 0.36f, 0.0f, 1.0f);

	//New and Load.
	private HorizontalMenu startUp_Menu;
	private LE_LoadMenu load_Menu;

	//Editing UI.
	private LE_TileBar tileBar;
	private LE_Inspector inspector;

	//TileMesh menus.
	public LE_TileCreator tileCreator;
	public LE_TileAnimation_Editor tileAnimation_Editor;


	/**Constructor.*/
	public LevelEditor(GameStateManager gameStateManager)
	{
		super(gameStateManager);
	}

	@Override
	/**StartUp function for this state.*/
	public void start()
	{
		//Set input pointers.
		this.controller = Game.controller;
		this.mouse = Game.mouse;

		//Load Level Editor Sprites.
        LE_Sprites.load();


		//
		//Create startUp menu.
		//
		this.startUp_Menu = new HorizontalMenu(new AAB_Box2D(0, 0, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT));
        this.startUp_Menu.addComponent
        (
            new Labeled_MenuChoice(257, 270, 326, 100,
            () -> 
            {
				//Create new Level.
				Sprite[] tileSet_Sprites = Sprites.loadTileSet_Sprites("GenericTiles");
				this.level = new Level(tileSet_Sprites);
				inspector.setLevel(this.level);
				fillTileSets(new String[]{"GenericTiles"}, new Sprite[][]{tileSet_Sprites});

                //Get level camera.
				this.camera = level.getCamera();
				this.f_cameraPosition = camera.f_getPosition();

				//Set state.
				previousState = STATE_EDITLEVEL;
                currentState = STATE_EDITLEVEL;
                mouse.resetScroll();
				mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
            },
            new Vector4f[]{MENU_COLOR_0, MENU_COLOR_1},
            "NEW LEVEL", 3.0f)
        )
        .addComponent
        (
            new Labeled_MenuChoice(639, 270, 326, 100,
            () -> 
            {
				//Set state.
                if(fillLoadMenu(editedLevelsPath))
				{
					previousState = STATE_STARTUP;
					currentState = STATE_LOADLEVEL;
					mouse.resetScroll();
					mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
				}
				else{System.err.println("No levels to load.");}
            },
            new Vector4f[]{MENU_COLOR_0, MENU_COLOR_1},
            "LOAD LEVEL", 3.0f)
        );


		//
		//Create LoadLevel menu.
		//
		load_Menu = new LE_LoadMenu();


		//
		//Create Editing UI.
		//
		tileBar = new LE_TileBar(this);
		inspector = new LE_Inspector(this);


		//
		//Create TileMesh creator.
		//
		tileCreator = new LE_TileCreator(this);


		//
		//Create TileAnimation editor.
		//
		tileAnimation_Editor = new LE_TileAnimation_Editor(this);
	}




	/*
	 * Loading Functions.
	 */

	/**Adds any Level folders in the editedLevels directory to the load menu.*/
	public boolean fillLoadMenu(String path)
	{
		//Get all of the files from the editedLevels folder.
		File[] files = new File(path).listFiles();
		//TODO If directory doesn't exist, create it.

		if(files.length <= 0){return false;}

		//Iterate through each file.
		for(int i = 0; i < files.length; i++)
		{
			//Cache file.
			File f = files[i];

			//If the file is a folder.
			if(f.isDirectory())
			{
				//Add a new option to the load menu.
				load_Menu.addOption(f);
			}
		}
	
		load_Menu.calculateScrollBar();
		return true;
	}

	/**Constructs the tileSets from the loaded level.*/
	private void fillTileSets(String[] tileSet_pathNames, Sprite[][] tileSet_Sprites)
	{
		//Keep tileSets for tracking.
		tileSets.clear();
		for(int i = 0; i < tileSet_Sprites.length; i++)
		{
			tileSets.add(new TileSet(tileSet_pathNames[i], tileSet_Sprites[i], 0));
		}

		//Used to track total count.
		int totalCount = 0;

		//Determine number of TileMeshs in each Set.
		int currentSetNum = 0;
		TileSet currentTileSet = tileSets.get(currentSetNum++);
		SpriteSheet currentTileSet_SpriteSheet = currentTileSet.getSheet(); 
		for(int i = 1; i < level.getNumTileMeshs(); i++)//1 because 0 is voidTile.
		{
			TileMesh ts = level.getTileMeshs(i);

			if(ts.getSheet() == currentTileSet_SpriteSheet){currentTileSet.add();}
			else
			{
				//Now that we have the nessessary info, create a new TileSet_Bar using this TileSet.
				currentTileSet.tileMeshOffset = totalCount;
				tileBar.addTileSet(currentTileSet);
				totalCount += currentTileSet.getCount();

				//Change tileSet to add to.
				currentTileSet = tileSets.get(currentSetNum++);

				//Go back one slot just to increment it back up and try again.
				i--;
			}
		}

		//Create a new TileSet_Bar for the last TileSet.
		currentTileSet.tileMeshOffset = totalCount;
		tileBar.addTileSet(currentTileSet);
		totalCount += currentTileSet.getCount();
	}
	
	/**Load a Level from the given folder.*/
	public void loadLevel(File folder)
	{
		//Get the files in the selected folder.
		File[] files = folder.listFiles();

		//Variables to pass to Level constructor.
		File tileDataFolder = null, tileKeyFile = null, entitiesFile = null;
		List<String> tileSet_pathNames = new ArrayList<String>();
		Sprite[][] tileSet_Sprites = null;


		for(int i = 0; i < files.length; i++)
		{
			File file = files[i];
			String fileName = file.getName();

			//TileData folder
			if(fileName.equals("TileData")){tileDataFolder = file;}

			//TileKey file.
			else if(fileName.equalsIgnoreCase("tilekey.dat")){tileKeyFile = file;}

			//Is this the tileSets file?
			else if(fileName.equalsIgnoreCase("tilesets.txt"))
			{
				try
				{
					//Create a scanner to read the file.
					Scanner scanner = new Scanner(file);

					while(scanner.hasNextLine())
					{
						//Get current line of text.
						String line = scanner.nextLine();

						//Empty line check.
						if(fileName.equals("")){continue;}

						//Add line to list of names.
						tileSet_pathNames.add(line);
					}

					//Close the scanner.
					scanner.close();
				}
				catch(FileNotFoundException e){e.printStackTrace();}
				//System.out.println("pathnames " + tileSet_pathNames.size());

				
				//Initialize tileSet sprites array.
				tileSet_Sprites = new Sprite[tileSet_pathNames.size()][];

				//Iterate through each string and load all the sprite layouts.
				for(int t = 0; t < tileSet_Sprites.length; t++)
				{
					tileSet_Sprites[t] = Sprites.loadTileSet_Sprites(tileSet_pathNames.get(t));
					//System.out.println("tl: " + tileSet_Sprites[t].length);
				}
			}

			//Entities file.
			else if(fileName.equalsIgnoreCase("entities.json")){entitiesFile = file;}
		}


		//NOW we can load the level.
		this.level = new Level(tileDataFolder, tileKeyFile, tileSet_Sprites, entitiesFile);
		inspector.setLevel(this.level);

		//Fill tileSets.
		String[] ts_names = new String[tileSet_pathNames.size()];
		tileSet_pathNames.toArray(ts_names);
		fillTileSets(ts_names, tileSet_Sprites);
	}



	/*
	 * Saving Functions.
	 */

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


    /**Saves a Tile Key of all the tilesets, tileMeshs, and TileAnimations used in the current level.*/
    public static void saveTileKey(Level level, String levelName, Sprite[][] tileSet_Sprites, String[] tileSet_Names, int[] tileSet_Counts)
    {
        //Get all the TileMeshs from the Level.
        //final TileMesh[] tileMeshs = level.getTileMeshs();
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

                //Cache the current set's sprites for TileMesh saving.
                Sprite[] ts_Sprites = tileSet_Sprites[t];
                //System.out.println(ts_Sprites);
                
                //
                //Write every tile in this TileSet to the TileKey file.
                //
                final int ts_count = tileSet_Counts[t];
                for(int i = 0; i < ts_count; i++)
                {
                    //Increment totalCount and translate the TileMesh to byte data.
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


    /**Saves the Level to a Folder.*/
    public void save()
    {
		//TODO Make this a custom dialog.
		/*
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
		*/

        //Proceed with Save.
        try
        {
            //Save Entities.
			//saveString = "FAILED: ENTITIES.";
            saveEntities(this.level, this.levelName);

            //Save TileData to .png files.
			//saveString = "FAILED: TILE DATA.";
            saveTileData(this.level, this.levelName);


            //
            //Save TileKey to a .dat file.
            //
			//saveString = "FAILED: TILE KEY.";

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
            //saveString = "SUCCESS!";
            //saveTime = 180;
            return;
        }
        catch(JsonGenerationException e){e.printStackTrace();}
        catch(JsonMappingException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}

        //An Exception happened. Save failed.
        //saveString = "FAIL...";
        //saveTime = 180;
    }




	@Override
	/**Call this function when switching GameStates.*/
	public void end()
	{
		level.unload(gameStateManager);
	}

	//Level getter.
	public Level getLevel(){return this.level;}




	//States.
    public static final byte
    STATE_STARTUP = 0,
    STATE_LOADLEVEL = 1,
    STATE_EDITLEVEL = 2,
    STATE_LOADTILESET_IMGLYT = 3,
    STATE_TILEMESH_EDITOR = 4,
	STATE_TILEANIM_EDITOR = 5;
    public byte currentState = STATE_STARTUP,
	previousState = currentState;

	//UI lock modes.
    public static final byte
    UILOCK_NONE = 0,
    UILOCK_MENUPICK = 1,
	UILOCK_OPTIONS = 2,
	UILOCK_TILESETS = 3;
    public byte uiLockMode = UILOCK_NONE;

	@Override
	public void update()
	{
		//Poll inputs here to reduce input lag slightly.
        glfwPollEvents();
        controller.update();

		//level.update();

		//Get mouse scroll.
		//mouseScroll = mouse.getScroll_Continue();

		switch(currentState)
        {
            case STATE_STARTUP:
			{
				//Update startUp menu.
				startUp_Menu.update(0, 0, true, true);
				mouse.updateState();
			}
            break;


            case STATE_LOADLEVEL:
            {
				//Update Load menu for a Level to load.
				if(load_Menu.update(0, 0, true, load_Menu.intersects(mouse)))
				{
					//Load the selected Level folder.
					loadLevel(load_Menu.getFolder());

					//Get camera.
					this.camera = level.getCamera();
					this.f_cameraPosition = camera.f_getPosition();

					//Set cursor z-position to start.
					this.f_cursorPosition.z = f_cameraPosition.z;

					//Set currentState to editLevel state.
					this.currentState = STATE_EDITLEVEL;
					this.previousState = currentState;//<- To go back to the Edit Level state from menus.

					//Reset loadLevel_Menu.
					load_Menu.reset();
				}
				//Cancel input check.
				else if(Menu.input_Cancel())
				{
					//Go back to previous state.
					currentState = previousState;

					//Reset loadLevel_Menu.
					load_Menu.reset();
				}
				mouse.updateState();
			}
            break;


            case STATE_EDITLEVEL:
			{
				//level.update();

				//Update Tile Animations.
				level.updateTileAnimations();
				
				switch(uiLockMode)
				{
					//Normal mode.
					case UILOCK_NONE:
					{
						//Controller inputs independent of UI first.
						independentOfUI();

						//Mouse inputs.

						//If no UI-locking inputs are being held.
						if(uiLockMode == UILOCK_NONE)
						{
							//Perform Mouse, placement, and UI checks dependent on UI.
							dependentOfUI();
						}
					}
					break;

					//Menu pick.
					case UILOCK_MENUPICK:
					{
						independentOfUI();
					}
					break;

					//Options menu.
					case UILOCK_OPTIONS:
					{
						//Update options menu.
						//optionsMenu.update();
						//TODO put cancel check in menu update

						//Cancel check.
						if(controller.menu_InputPressed(0, Controller.menu_CANCEL, true))
						{uiLockMode = UILOCK_NONE;}
					}
					break;

					//TileSet Bar.
					case UILOCK_TILESETS:
					{
						boolean intersBar = tileBar.intersects(0, 0, mouse);
						tileBar.update(0, 0, true, intersBar);//IS extended.
						mouse.updateState();
					}
					break;
				}
			}
            break;


            case STATE_LOADTILESET_IMGLYT:
			{
				//Update Load menu for a SpriteSheet and Layout to load.
				if(load_Menu.update(0, 0, true, load_Menu.intersects(mouse)))
				{
					//Load the selected folder.
					File[] sheetAndLayout = load_Menu.getFolder().listFiles();

					//For SpriteSheet and Layout of TileSet.
					SpriteSheet setSheet = null;
					File layoutFile = null;

					for(int i = 0; i < sheetAndLayout.length; i++)
					{
						File file = sheetAndLayout[i];
						String fileName = file.getName();

						//SpriteSheet.
						if(fileName.equalsIgnoreCase("IMG.png"))
						{setSheet = new SpriteSheet(file);}

						//Sprite Layout.
						else if(fileName.equalsIgnoreCase("LYT.dat"))
						{layoutFile = file;}
					}


					//Load Layout.
					Sprite[] setSprites = setSheet.loadLayout(layoutFile);

					//Create new TileSet and add it to the TileBar.
					TileSet newTileSet = new TileSet(load_Menu.getFolder().getName(), setSprites, level.getNumTileMeshs());
					tileSets.add(newTileSet);
					tileBar.addTileSet(newTileSet);

					//Set currentState to editLevel state.
					this.currentState = STATE_EDITLEVEL;
					this.previousState = currentState;//<- To go back to the Edit Level state from menus.


					//Reset load menu.
					load_Menu.reset();
				}
				//Cancel input check.
				else if(Menu.input_Cancel())
				{
					//Go back to previous state.
					currentState = previousState;

					//Reset load menu.
					load_Menu.reset();
				}
				mouse.updateState();
			}
            break;

			
            case STATE_TILEMESH_EDITOR:
			{
				//Update Tile Animations.
				level.updateTileAnimations();

				//Update tileCreator.
				tileCreator.update(0, 0, true, true);
				mouse.updateState();
			}
            break;

			case STATE_TILEANIM_EDITOR:
			{
				//Update Tile Animations.
				level.updateTileAnimations();

				//Update TileAnimation Editor.
				//System.out.println(mouse.getButtonState(GLFW_MOUSE_BUTTON_LEFT));
				tileAnimation_Editor.update(0, 0, true, true);
				mouse.updateState();

				
				//mouse.setButtonState(GLFW_MOUSE_BUTTON_LEFT, Controller.STATE_NOT_HELD);
			}
            break;
        }
		
	}

	public boolean independentOfUI()
	{
		//If holding X, perform floor change and zooming.
		if(controller.menu_InputHeld(0, Controller.menu_SPECIAL_0))
		{
			uiLockMode = UILOCK_NONE;

			//Up and Down, Zoom.
			if(controller.menu_InputHeld(0, Controller.menu_UP))
			{
				level.addScale(0.1f, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT);
				//mouse.setScroll((byte)0);
			}
			if(controller.menu_InputHeld(0, Controller.menu_DOWN))
			{
				level.addScale(-0.1f, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT);
				//mouse.setScroll((byte)0);
			}
			//A, Reset zoom.
			if(controller.menu_InputHeld(0, Controller.menu_CONFIRM))
			{level.setScale(1.0f, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT);}

			//Left bumper/Right bumper, Z Scroll
			if(controller.menu_InputPressed(0, Controller.menu_LEFT_TAB, true))
			{
				this.floor = ((floor-1)+level.getDepth()) % level.getDepth();
				f_cameraPosition.z = (floor << Level.FIXED_TILE_BITS);

				f_cursorPosition.z = f_cameraPosition.z;

				//System.out.println(floor);
			}
			if(controller.menu_InputPressed(0, Controller.menu_RIGHT_TAB, true))
			{
				this.floor = (floor+1) % level.getDepth();
				f_cameraPosition.z = (floor << Level.FIXED_TILE_BITS);

				f_cursorPosition.z = f_cameraPosition.z;

				//System.out.println(floor);
			}

			//Tool change
			if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
			{
				
			}
			if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
			{
				//uiLockMode = UILOCK_INSPECTOR;
			}

			//Don't do anything else if this is the case.
			return false;
		}
		//If holding Y, perform object type change and menu checks.
		else if(controller.menu_InputHeld(0, Controller.menu_SPECIAL_1))
		{
			uiLockMode = UILOCK_MENUPICK;

			//Left/Right Bumpers, Object type change.
			if(controller.menu_InputPressed(0, Controller.menu_LEFT_TAB, true))
			{
				//Set type to left
			}
			if(controller.menu_InputPressed(0, Controller.menu_RIGHT_TAB, true))
			{
				//Set type to right.
			}

			
			//Up, Tools.
			if(controller.menu_InputHeld(0, Controller.menu_UP))
			{
				
			}

			//Left, Options menu.
			else if(controller.menu_InputHeld(0, Controller.menu_LEFT))
			{
				uiLockMode = UILOCK_OPTIONS;
			}

			//Right, TileAnimations Menu/Inspector
			else if(controller.menu_InputHeld(0, Controller.menu_RIGHT))
			{
				//TODO Change to navigate Animation Tracker.
				//uiLockMode = UILOCK_INSPECTOR;

				tileAnimation_Editor.setTileAnimation(level.getTileAnimation(0));
				currentState = STATE_TILEANIM_EDITOR;
			}

			//Down, TileSets/Entity catagories menu.
			else if(controller.menu_InputHeld(0, Controller.menu_DOWN))
			{
				//Extend the TileBar.
				tileBar.toggleExtend();
			}
		}
		//Otherwise, perform movement and changing Tiles/Entities.
		else
		{
			uiLockMode = UILOCK_NONE;

			//Right Bumper: Move Fast.
			@fixed int f_xa = fixed(16), f_ya = fixed(16);
			boolean shift = controller.menu_InputHeld(0, Controller.menu_RIGHT_TAB);
			if(shift){f_xa = fixed(32); f_ya = fixed(32);}

			//Left Bumper: select Tiles/Entities. Otherwise, perform basic cursor movement.
			if(!controller.menu_InputHeld(0, Controller.menu_LEFT_TAB))
			{
				//TODO Change to moving a cursor, only moving the camera when the cursor goes beyond an extent.
				//Left/Right: Horizontal Scroll
				if(controller.menu_InputHeld(0, Controller.menu_LEFT)){f_cameraPosition.x -= f_xa;}
				if(controller.menu_InputHeld(0, Controller.menu_RIGHT)){f_cameraPosition.x += f_xa;}

				//Up/Down: Vertical Scroll
				if(controller.menu_InputHeld(0, Controller.menu_UP)){f_cameraPosition.y -= f_ya;}
				if(controller.menu_InputHeld(0, Controller.menu_DOWN)){f_cameraPosition.y += f_ya;}

				//Bound camera X position.
				//if(level.getWidth() << Level.TILE_BITS >= LevelEditorMain.WIDTH)
				{
					@fixed int f_xLim = (int)(fixed(512) * camera.getScale());

					if(f_cameraPosition.x < -f_xLim){f_cameraPosition.x = -f_xLim;}
					else if(f_cameraPosition.x > fixed((level.getWidth() << Level.TILE_BITS) - LevelEditorMain.WIDTH) + f_xLim)
					{f_cameraPosition.x = fixed((level.getWidth() << Level.TILE_BITS) - LevelEditorMain.WIDTH) + f_xLim;}
				}

				//Bound camera Y position.
				//if(level.getHeight() << Level.TILE_BITS >= LevelEditorMain.HEIGHT)
				{
					@fixed int f_yLim = (int)(fixed(256) * camera.getScale());

					if(f_cameraPosition.y < -f_yLim){f_cameraPosition.y = -f_yLim;}
					else if(f_cameraPosition.y > fixed((level.getHeight() << Level.TILE_BITS) - LevelEditorMain.HEIGHT) + f_yLim)
					{f_cameraPosition.y = fixed((level.getHeight() << Level.TILE_BITS) - LevelEditorMain.HEIGHT) + f_yLim;}
				}

				//TODO Do in different function?
				if(controller.menu_InputHeld(0, Controller.menu_CONFIRM)){}
			}
		}


		//TODO Clean up animation editor.
		//TODO Remove once save button is implemented.
		//if(controller.isKeyPressed(GLFW_KEY_ENTER))
		//{

		//}

		return true;
	}

	public void dependentOfUI()
	{
		boolean intersectingTileBar = tileBar.intersects(mouse);
		tileBar.update(0, 0, controller.menu_InputHeld(0, Controller.menu_LEFT_TAB), intersectingTileBar);//Not extended.

		//If not hovering over any UI.
		if(!intersectingTileBar)
		{
			//System.out.println(f_toInt(f_cameraPosition.x) + " " + f_toInt(f_cameraPosition.y) +  " " + f_toInt(f_cameraPosition.z));

			//If mouse is moving...
			if(mouse.isMoving())
			{
				//Place cursor on mouse position.
				f_cursorPosition.x = f_cameraPosition.x + (int)(fixed(mouse.getX()) / camera.getScale());
				f_cursorPosition.y = f_cameraPosition.y + (int)(fixed(mouse.getY()) / camera.getScale()) + (Level.FIXED_TILE_SIZE >> 1);

				f_cursorPosition.x  = (f_cursorPosition.x >> Level.FIXED_TILE_BITS) << Level.FIXED_TILE_BITS;
				f_cursorPosition.y  = (f_cursorPosition.y >> Level.FIXED_TILE_BITS) << Level.FIXED_TILE_BITS;
			}

			//A or Left-Click: Use current tool with slot 0.
			if(controller.menu_InputHeld(0, Controller.menu_CONFIRM) || mouse.buttonHeld(GLFW_MOUSE_BUTTON_LEFT))
			{
				level.setTileMesh
				(
					f_cursorPosition.x >> Level.FIXED_TILE_BITS,
					f_cursorPosition.y >> Level.FIXED_TILE_BITS,
					f_cursorPosition.z >> Level.FIXED_TILE_BITS,
					pickedTiles[0] >> Tiles.TILE_PROPERTIES_BITS
				);
			}

			//B or Right-Click: Use current tool with slot 1.
			if(controller.menu_InputHeld(0, Controller.menu_CANCEL) || mouse.buttonHeld(GLFW_MOUSE_BUTTON_RIGHT))
			{
				level.setTileMesh
				(
					f_cursorPosition.x >> Level.FIXED_TILE_BITS,
					f_cursorPosition.y >> Level.FIXED_TILE_BITS,
					f_cursorPosition.z >> Level.FIXED_TILE_BITS,
					pickedTiles[1] >> Tiles.TILE_PROPERTIES_BITS
				);
			}
		}

		mouse.removePress(GLFW_MOUSE_BUTTON_LEFT);
		mouse.removePress(GLFW_MOUSE_BUTTON_RIGHT);
	}


	public void openTileCreator(int tileMeshID)
	{
		//Get sprites from TileSet the TileSprite is in.
		TileSet ts = getTileSet_tileMeshID(tileMeshID);
		tileCreator.setTileMesh(ts.getSprites(), level.getTileMeshs(tileMeshID));

		//Set current state to TileSprite Editor.
		currentState = STATE_TILEMESH_EDITOR;
	}

	private TileSet getTileSet_tileMeshID(int tileMeshID)
	{
		int total = 0;
		for(int i = 0; i < tileSets.size(); i++)
		{
			TileSet ts = tileSets.get(i);
			total += ts.getCount();

			if(tileMeshID-1 < total)
			{return ts;}
		}

		return null;
	}

	/**Adds a TileSprite to the Level at adjusts the TileSets' offsets accorrdingly.*/
	public void addTileSprite(TileMesh newTileMesh, TileSet tileSet)
	{
		//Add the input TileSprite to the Level. Add 1 to the input TileSet.
		level.addTileMesh(newTileMesh);
		tileSet.add();

		//Increment the offset of every TileSet past the input one by 1.
		boolean pastInputSet = false;
		for(int i = 0; i < tileSets.size(); i++)
		{
			TileSet t = tileSets.get(i);

			if(!pastInputSet)
			{
				if(t == tileSet){pastInputSet = true;}
			}
			//If we're past the input TileSet, increment the current one's offset.
			else{t.tileMeshOffset++;}
		}
	}

	/**Removes a TileSprite from the Level and adjusts the TileSets' offsets accorrdingly.*/
	public void removeTileSprite(int tileMeshSlot, TileSet tileSet)
	{
		//Remove the input TileSprite from the Level. Remove 1 from the input TileSet.
		tileSet.subtract();
		level.removeTileMesh(tileMeshSlot);

		//Decrement the offset of every TileSet past the input one by 1.
		boolean pastInputSet = false;
		for(int i = 0; i < tileSets.size(); i++)
		{
			TileSet t = tileSets.get(i);

			if(!pastInputSet)
			{
				if(t == tileSet){pastInputSet = true;}
			}
			//If we're past the input TileSet, decrement the current one's offset.
			else{t.tileMeshOffset--;}
		}
	}



	private Vector4f darkenColor = new Vector4f(0.0f, 0.0f, 0.0f, 0.5f),
	cubeTop = new Vector4f(1.0f, 1.0f, 0.0f, 0.75f), cubeBottom = new Vector4f(0.75f, 0.75f, 0.0f, 0.75f);

	@Override
	public void render(Screen screen)
	{
		switch(currentState)
        {
			//
			//StartUp state.
			//
			case STATE_STARTUP:
			{
				//Render startUp menu.
				startUp_Menu.render(screen);
			}
            break;

			//
			//Load menu states.
			//
            case STATE_LOADLEVEL:
			case STATE_LOADTILESET_IMGLYT:
            {
				//Update Load Level menu.
				load_Menu.render(screen);
			}
            break;


			//
			//Edit state.
			//
			case STATE_EDITLEVEL:
			{
				//Render Level.
				level.render(screen);

				//Render level boundaries.
				screen.drawRect(0, 0, f_toInt(f_cameraPosition.z),
				(int)((level.getWidth() << Level.TILE_BITS) * camera.getScale()),
				(int)((level.getHeight() << Level.TILE_BITS) * camera.getScale()),
				Screen.DEFAULT_BLEND, true);


				//
				//Render a cube where the cursor is.
				//

				//Top
				screen.fillRect((int)(f_toInt(f_cursorPosition.x) * camera.getScale()), (int)(f_toInt(f_cursorPosition.y) * camera.getScale()),
				(int)(f_toInt(f_cursorPosition.z + Level.FIXED_TILE_SIZE) * camera.getScale()), 0,
				(int)(Level.TILE_SIZE * camera.getScale()), (int)(Level.TILE_SIZE * camera.getScale()), cubeTop, true);
				//Bottom
				screen.fillRect((int)(f_toInt(f_cursorPosition.x) * camera.getScale()), (int)(f_toInt(f_cursorPosition.y + Level.FIXED_TILE_SIZE) * camera.getScale()),
				(int)(f_toInt(f_cursorPosition.z + Level.FIXED_TILE_SIZE) * camera.getScale()),  (int)(Level.TILE_SIZE * camera.getScale()),
				(int)(Level.TILE_SIZE * camera.getScale()), (int)((Level.TILE_SIZE >> 1) * camera.getScale()), cubeBottom, true);


				//Render UI depending on lock mode.
				switch(uiLockMode)
				{
					//Normal state.
					case UILOCK_NONE:
					{
						//Render all UI normally.
						tileBar.render(screen);

						level.getTileMeshs(pickedTiles[0] >> Tiles.TILE_PROPERTIES_BITS).render_2D(screen, 4, 350);
						level.getTileMeshs(pickedTiles[1] >> Tiles.TILE_PROPERTIES_BITS).render_2D(screen, 38, 384);

						inspector.render(screen);
					}
					break;

					case UILOCK_MENUPICK:
					{
						//Render all UI.
						inspector.render(screen);

						//Darken everything.
						screen.fillRect(0, 0, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT, darkenColor, false);
					}
					break;

					case UILOCK_OPTIONS:
					{
						//Render UI not in use.
						inspector.render(screen);

						//Darken everything so far.
						screen.fillRect(0, 0, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT, darkenColor, false);

						//Render options menu over to appear brighter.
						//optionsMenu.render(screen);
					}
					break;

					//TileBar.
					case UILOCK_TILESETS:
					{
						//Render UI not in use.
						inspector.render(screen);

						//Darken everything so far.
						screen.fillRect(0, 0, LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT, darkenColor, false);

						//Render TileBar over to appear brighter.
						tileBar.render(screen);
					}
					break;
				}
			}
			break;

			case STATE_TILEMESH_EDITOR:
			{
				tileCreator.render(screen, 0, 0);
			}
            break;

			case STATE_TILEANIM_EDITOR:
			{
				tileAnimation_Editor.render(screen, 0, 0);
			}
			break;
		}
	}


	//Pencil
	//-Set TileSprite on position to selected TileSprite.

	//Eraser
	//-Set TileSprite on position to 0.

	//Fill Rect (more useful with collision)
	//-Hold: Drag to set dimensions.
	//-Release: Set TileSprites in region to selected one.
}
