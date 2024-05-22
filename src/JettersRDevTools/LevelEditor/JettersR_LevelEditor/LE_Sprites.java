package JettersRDevTools.LevelEditor.JettersR_LevelEditor;
/**
 * HashMap for Sprites exclusive to the Level Editor.
 * 
 * Author: Luke Sullivan
 * Last Edit: 11/5/2023
 */
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import JettersR.Graphics.SpriteSheet;
import JettersR.Graphics.Sprites;

public class LE_Sprites
{
    //Just so this just can't be instantiated as an object.
    private LE_Sprites(){}

    public static final String imagesPath = "src/JettersRDevTools/LevelEditor/images/",
    sheetLayoutsPath = "src/JettersRDevTools/LevelEditor/layouts/";

    private static boolean loaded = false;

    private static ConcurrentHashMap<String, SpriteSheet> LE_hashMap = new ConcurrentHashMap<>();

    /**Loads all the Sprites for the Level Editor.*/
    public static void load()
    {
        //This function will only work ONCE
        if(loaded){return;}

        //Load LE_Sprites
        Sprites.load(new File(imagesPath), LE_hashMap, "");

        //NEVER make this function work again
        loaded = true;
    }

    /**Gets a SpriteSheet from the LE_Sprites HashMap.*/
    public static SpriteSheet getSheet(String name)
    {
        SpriteSheet spriteSheet = LE_hashMap.get(name);

        if(spriteSheet == null){throw new RuntimeException("Could not locate LE_SpriteSheet: " + name + ".");}

        return spriteSheet;
    }
}
