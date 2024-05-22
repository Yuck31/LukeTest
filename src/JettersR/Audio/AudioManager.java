package JettersR.Audio;
/**
 * This manages Game Audio.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/12/2022
 */
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALCapabilities;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC11.*;
import static org.lwjgl.openal.AL11.*;

public class AudioManager
{
    private static boolean initialized = false;

    private static long audioContext, audioDevice;
    private long windowAddress;

    /**This constructor is just here so only the Game class can shut down OpenAL.*/
    public AudioManager(long windowAddress){this.windowAddress = windowAddress;}

    /**Initilizes OpenAL.*/
    public static void init()
    {
        //Only make this function work ONCE.
        if(initialized){return;}

        //Aquire Audio Device.
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        //Apply Attributes.
        int[] attributes = new int[1];
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        //Initialize OpenAL
        ALCCapabilities alc_Capabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alc_Capabilities);
        if(!alCapabilities.OpenAL11){throw new RuntimeException("Audio Library not supported.");}

        setListenerData();
        alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED);

        //Load Global Sound Effects.
        Audio.loadGlobalSounds();

        //Never make this function work again.
        initialized = true;
    }

    public static void setListenerData(float x, float y, float z)
    {
        alListener3f(AL_POSITION, x, y, z);
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }

    public static void setListenerData(){setListenerData(0, 0, 0);}

    /**Closes OpenAL.*/
    public void cleanUp()
    {
        //Just so nothing outside of the Game class can accidently shut off the AudioManager.
        if(!glfwWindowShouldClose(this.windowAddress)){return;}

        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);
    }
}
