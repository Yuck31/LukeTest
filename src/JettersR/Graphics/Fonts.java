package JettersR.Graphics;
/**
 * Author: Luke Sullivan
 * Last Edit: 4/24/2022
 */
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Enumeration;

public class Fonts
{
    //Just so this can't be instantiated.
    private Fonts(){}

    //Fonts Path.
    public static final String fontsPath = Sprites.imagesPath + "Global/Fonts/";

    //HashMap.
    private static ConcurrentHashMap<String, Font> fontsHashMap = new ConcurrentHashMap<>();

    private static boolean loaded = false;

    public static void load(Collection<SpriteSheet> collec, Enumeration<String> keys)
    {
        //This function will only work ONCE.
        if(loaded){return;}

        //For each Sheet, make a Font out of it and put it into the HashMap.
        collec.forEach
        (s -> 
            {
                String str = keys.nextElement();
                fontsHashMap.put(str, new Font(s, str));
            }
        );

        //NEVER make this function work again.
        loaded = true;
    }

    /**Gets a Font from the HashMap.*/
    public static Font get(String name)
    {
        //Attempt to get a Font.
        Font font = fontsHashMap.get(name);
        
        //If no Font was found.
        if(font == null)
        {
            //If no Fonts were even loaded.
            if(!loaded)
            {
                //Load their Sheets to load the Fonts.
                Sprites.loadGlobalSheets();

                //Try to get the Font again.
                font = fontsHashMap.get(name);

                //If no Font was found still, it doesn't exist.
                if(font == null)
                {
                    throw new RuntimeException("Could not locate Font: " + name + ". This was on Program Execution.");

                    //System.err.println("Could not locate Font: " + name + ". This was on Program Execution.");
                    //return null;
                }
            }
            //The Requested Sheet just does not exist.
            else
            {
                System.err.println("Could not locate Font: " + name + ".");
                return null;
            }
        }

        //Reaching this point means it exists. Return it.
        return font;
    }
}
