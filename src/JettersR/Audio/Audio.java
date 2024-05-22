package JettersR.Audio;
/**
 * This manages Game Audio (Music, Sound Effects and whatnot)
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/12/2022
 */
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class Audio
{
    /*Same thing as SpriteSheets, we load everything into a HashMap*/

    //Audio Paths (so that way if I have to move their respective folders, I just need to change these)
    public static final String musicPath = "assets/Music/", soundsPath = "assets/Sounds/";

    //To be used with OpenAL
    //private static int audioID = 0;

    //Meant to ensure global Sound Effects and Music aren't loaded more than once
    private static boolean loaded = false;

    //These are the HashMaps that will be used to store loaded Sounds
    private static ConcurrentHashMap<String, AudioBuffer>
    global_UI = new ConcurrentHashMap<String, AudioBuffer>();//UI
    //global_Entities = new ConcurrentHashMap<>();//Entities

    /**Load all Global Sounds. Sounds that stay loaded the entire duration of the game running.*/
    public static void loadGlobalSounds()
    {
        //This function will only work ONCE.
        if(loaded){return;}

        //Load UI Sounds.
        load(new File(soundsPath + "Global/UI"), global_UI, "");

        //NEVER make this function work again.
        loaded = true;
    }

    /**Loads Sounds from the Given Directory into the given HashMap.*/
    public static void load(File directory, ConcurrentHashMap<String, AudioBuffer> hashMap, String parentDir)
    {
        File[] files = null;
        if(directory.isDirectory()){files = directory.listFiles();}
        else{files = new File[]{directory};}
        
        for(int i = 0; i < files.length; i++)
        {
            //Get File and FileName
            File f = files[i];
            String fileName = parentDir + f.getName();

            //Skip the ICH and JSON files.
            if(f.getName().contains("ICHs") || f.getName().contains("JSONs")
            || f.getName().contains(".txt")){continue;}
            //It's a .wav file.
            else if(f.getName().contains(".wav"))
            {
                //Remove ".wav" from fileName.
                fileName = fileName.substring(0, fileName.length()-4);

                //Create new Sound out of file and increment audioID.
                AudioBuffer sound = AudioBuffer.wav(f);//, fileName);

                //Put the Sprite into the hashmap.
                global_UI.put(fileName, sound);
            }
            else//It's a folder, load everything in it.
            {load(f, hashMap, fileName + "/");}
        }
    }

    /**Allows you to acquire a Sound from the given HashMap via a String.*/
    public static AudioBuffer getSound(ConcurrentHashMap<String, AudioBuffer> hashMap, String name)
    {
        //Attempt to get a Sound.
        AudioBuffer sound = hashMap.get(name);

        //If no Sound was found.
        if(sound == null)
        {
            //If no Sounds were even loaded.
            if(!loaded)
            {
                //Initialize OpenAL to load the Sounds.
                AudioManager.init();

                //Try to get the Sound again.
                sound = hashMap.get(name);

                //If no Sound was found still, it doesn't exist.
                if(sound == null)
                {
                    System.err.println("Could not locate Sound: " + name + ". This was on Program Execution.");
                    return null;
                }
            }
            //The Requested Sound just does not exist.
            else
            {
                System.err.println("Could not locate Sound: " + name);
                return null;
            }
        }

        //Reaching this point means it exists. Return it.
        return sound;
    }

    /**Gets a Sound from the Global_UI HashMap.*/
    public static AudioBuffer global_UISound(String name){return getSound(global_UI, name);}

    /**
     * Loads the given music folder.
     * 
     * @return
     */
    public static AudioBuffer[] loadMusic(String name)
    {
        //Assuming this is a folder containing audio files.
        File sourceFile = new File(musicPath + name);
        File[] files = sourceFile.listFiles();
        AudioBuffer[] buffers = new AudioBuffer[files.length];

        for(int i = 0; i < files.length; i++)
        {
            //Cache File.
            File f = files[i];

            //Load it as (default) 44100 hrtz .OGG data.
            buffers[i] = AudioBuffer.ogg(f);
        }

        return buffers;
    }
}
