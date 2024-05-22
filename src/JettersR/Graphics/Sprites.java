package JettersR.Graphics;
/**
 * A class that represents the SpriteSheet Asset pool for the game
 * 
 * Author: Luke Sullivan
 * Last Edit: 8/25/2023
 */
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.io.File;
//import java.util.Scanner;
//import java.io.FileNotFoundException;

import JettersR.Tiles.Tiles;

public final class Sprites
{
    //Just so this can't be instantiated.
    private Sprites(){}

    //Images Path (so that way if I have to move the images folder, I just need to change this).
    public static final String imagesPath = "assets/Images/";

    //Sprite Objects meant for null, shape drawing, and default normal map.
    public static final SpriteSheet
    nullSheet = new SpriteSheet(),
    whiteSheet = new SpriteSheet(1, 1, 0xffffffff),
    flatSheet = new SpriteSheet(1, 1, 0xff7f7fff);
    //
    public static final Sprite
    nullSprite = new Sprite(nullSheet),
    whiteSprite = new Sprite(whiteSheet),
    flatSprite = new Sprite(flatSheet);

    //To ensure global sprites aren't accidently loaded more than once.
    private static boolean loaded = false;

    //Will be used with OpenGL and Vulkan later...
    //private static int texID = 0;

    //These are the HashMaps that will be used to store loaded SpriteSheets.
    private static final byte
    GLOBAL_DEBUG = 0,
    GLOBAL_UI = 1,
    GLOBAL_ENTITIES = 2,
    GLOBAL_FONTS = 3,
    NUM_GLOBAL_HASHMAPS = 4;//, NUM_HASHMAPS = 4;

    private static final ConcurrentHashMap<String, SpriteSheet>
    global_Debug = new ConcurrentHashMap<>(),//Debug
    global_UI = new ConcurrentHashMap<>(),//UI
    global_Entities = new ConcurrentHashMap<>(),//Entities
    global_Fonts = new ConcurrentHashMap<>();//Fonts
    
    /**
     * This is just so I don't have to apply AS much effort when adding a new HashMap to the Code.
     * That, and so making for loops isn't as annoying.
     * I would use an array of HashMaps, but according to Java, that isn't type-safe.
     * 
     * @param slot can be a GLOBAL_ or LOCAL_ value representing the HashMap to get.
     */
    private static ConcurrentHashMap<String, SpriteSheet> hashMap(byte slot)
    {
        switch(slot)
        {
            case GLOBAL_DEBUG: return global_Debug;
            case GLOBAL_UI: return global_UI;
            case GLOBAL_ENTITIES: return global_Entities;
            case GLOBAL_FONTS : return global_Fonts;

            default: throw new RuntimeException(slot + " is outside of the range of HashMaps.");
        }
    }

    /**Load Sprites from a given filePath into a given HashMap*/
    public static void load(File directory, ConcurrentHashMap<String, SpriteSheet> hashMap, String parentDir)
    {
        File[] files = null;
        if(directory.isDirectory()){files = directory.listFiles();}
        else{files = new File[]{directory};}

        for(int i = 0; i < files.length; i++)
        {
            //Get File and FileName
            File f = files[i];
            String fileName = parentDir + f.getName();

            //It's a .png file
            if(f.getName().contains(".png"))
            {
                //Remove ".png" from fileName
                fileName = fileName.substring(0, fileName.length()-4);

                //Create new Sprite out of file and increment texID
                SpriteSheet spriteSheet = new SpriteSheet(f);//, fileName);

                //Put the Sprite into the hashmap
                hashMap.put(fileName, spriteSheet);
            }

            else if(f.getName().contains(".rsf"))//[R]ectangle [S]prite [F]ile
            {
                /*
                try
                {
                    //width, height, color
                    Scanner scanner = new Scanner(f);
                    String line = scanner.nextLine(), currentString = "";
                    //0 = Width, 1 = Height, 2 = Color
                    int[] parameters = new int[3]; int mode = 0;

                    for(int j = 0; j < line.length(); j++)
                    {
                        char c = line.charAt(j);
                        if(c == ',')
                        {
                            parameters[mode] = Integer.parseInt(currentString);
                            mode++;
                            currentString = "";
                        }
                        else if(j >= line.length()-1)
                        {
                            parameters[mode] = Integer.parseInt(currentString+c);
                            mode++;
                            currentString = "";
                        }
                        else{currentString += c;}
                    }
                    //Remove ".rsf" from fileName
                    fileName = fileName.substring(0, fileName.length()-4);

                    //Use the variables to create a SpriteSheet
                    SpriteSheet spriteSheet = new SpriteSheet(parameters[0], parameters[1], parameters[2]);

                    //Put the SpriteSheet into the hashmap
                    hashMap.put(fileName, spriteSheet);
                    
                    //Close the scanner
                    scanner.close();
                }
                catch(FileNotFoundException e){e.printStackTrace();}
                */
            }

            //.dat/.txt file. Ignore it.
            else if(f.getName().contains(".dat") || f.getName().contains(".txt"))
            {continue;}

            else//It's a folder, load everything in it
            {load(f, hashMap, fileName + "/");}

            //System.out.println(fileName);
        }
    }

    /**Load all Global Sprites. Sprites that stay loaded the entire duration of the game running.*/
    public static void loadGlobalSheets()
    {
        //This function will only work ONCE.
        if(loaded){return;}

        //Load the default sheets.
        nullSheet.load();
        whiteSheet.load();
        flatSheet.load();
        
        //Load Debug Sprites.
        load(new File(imagesPath + "Global/Debug"), global_Debug, "");

        //Load UI.
        load(new File(imagesPath + "Global/UI"), global_UI, "");

        //Load Entities.
        load(new File(imagesPath + "Global/Entities"), global_Entities, "");

        //Load Fonts (since Fonts use SpriteSheets, their sheets need to be stored here and be treated as textures too).
        load(new File(imagesPath + "Global/Fonts"), global_Fonts, "");
        Fonts.load(global_Fonts.values(), global_Fonts.keys());

        //NEVER make this function work again.
        loaded = true;
    }

    /**To be used when the Global Sheets were loaded when a Graphics API wasn't initialized.*/
    public static void setSheetFunctions(byte graphicsAPI)
    {
        //Set the load and unload functions if they were not already set.
        if(!SpriteSheet.setSheetFunctions(graphicsAPI)){return;}

        //If Sprites were loaded without a Graphics API Initialized.
        if(loaded)
        {
            //Run the load function on all of the default sheets.
            nullSheet.load();
            whiteSheet.load();
            flatSheet.load();

            //Iterate through every Global HashMap.
            for(byte i = 0; i < NUM_GLOBAL_HASHMAPS; i++)
            {
                //Retrieve the Sheets as a Collection.
                Collection<SpriteSheet> collec = hashMap(i).values();

                //And run the load function on every Sheet.
                collec.forEach(s -> s.load());
            }
        }
    }

    /**
     * Allows you to acquire a SpriteSheet from the given HashMap via a String.
     * If no Sprites were loaded yet, they will be loaded to be set their API
     * functions if no Graphics API was initialized, and this function will try again.
     * 
     * @param hashMap the HashMap to get the SpriteSheet from.
     * @param name the name of the SpriteSheet, used as a key for the HashMap.
     * @return the requested SpriteSheet if it exists. Otherwise, an exception is thrown.
     */
    private static SpriteSheet getSheet(ConcurrentHashMap<String, SpriteSheet> hashMap, String name)
    {
        //Attempt to get a SpriteSheet.
        SpriteSheet spriteSheet = hashMap.get(name);

        //If no Sheet was found.
        if(spriteSheet == null)
        {
            //If no Sheets were even loaded.
            if(!loaded)
            {
                //Load them.
                loadGlobalSheets();

                //Try to get the Sheet again.
                spriteSheet = hashMap.get(name);

                //If no Sheet was found still, it doesn't exist.
                if(spriteSheet == null)
                {
                    throw new RuntimeException("Could not locate SpriteSheet: " + name + ". This was on Program Execution.");

                    //System.err.println("Could not locate Sprite: " + name + ". This was on Program Execution.");
                    //return nullSheet;
                }
            }
            //The Requested Sheet just does not exist.
            else
            {
                throw new RuntimeException("Could not locate SpriteSheet: " + name + ".");
                
                //System.err.println("Could not locate SpriteSheet: " + name + ".");
                //return nullSheet;
            }
        }

        //Reaching this point means it exists. Return it.
        return spriteSheet;
    }

    /**Returns a SpriteSheet from the global_Debug Sheet HashMap.*/
    public static SpriteSheet global_Debug(String name){return getSheet(global_Debug, name);}

    /**Returns a SpriteSheet from the global_UI Sheet HashMap.*/
    public static SpriteSheet global_UISheet(String name){return getSheet(global_UI, name);}

    /**Returns a SpriteSheet from the global_Entity Sheet Hashmap.*/
    public static SpriteSheet global_EntitySheet(String name){return getSheet(global_Entities, name);}

    /**Returns a SpriteSheet from the global_Font Sheet Hashmap.*/
    public static SpriteSheet global_FontSheet(String name){return getSheet(global_Fonts, name);}

    
    /**Loads a TileSheet image from the given Folder name.*/
    public static SpriteSheet loadTileSet(String name)
    {
        //Load and return SpriteSheet.
        SpriteSheet sheet = new SpriteSheet(new File(Tiles.TILESETS_PATH + name + "/IMG.png"));//, name);
        //System.out.println("loadTile " + sheet.getID() + " " + sheet.getPixels_0());
        return sheet;
    }

    /**Loads a TileSet Sprite Layout from the given Folder name.*/
    public static Sprite[] loadTileSet_Sprites(String name)
    {
        //Load SpriteSheet.
        SpriteSheet sheet = new SpriteSheet(new File(Tiles.TILESETS_PATH + name + "/IMG.png"));//, name);

        //Load and return Sprite layout.
        File file = new File(Tiles.TILESETS_PATH + name + "/LYT.dat");
        //return sheet.loadLayout(file);

        Sprite[] result = sheet.loadLayout(file);
        //System.out.println(result[result.length-1]);
        //System.out.println(result[result.length-2]);
        return result;
    }
}
