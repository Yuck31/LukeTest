package JettersR.Audio;
/**
 * Author: Luke Sullivan
 * Last Edit: 4/3/2022
 */
import static org.lwjgl.openal.AL11.*;

public class MusicSource extends AudioSource
{
    public interface LoopFunction{public abstract void update();}
    public final LoopFunction loop;
    private boolean looping = false;

    private int[] bufferIDs;

    /**Constructor. */
    public MusicSource(String folderName)
    {
        //Assign ID.
        super();

        //Load the AudioBuffers.
        AudioBuffer[] buffers = Audio.loadMusic(folderName);
        bufferIDs = new int[buffers.length];
        for(int i = 0; i < bufferIDs.length; i++){bufferIDs[i] = buffers[i].getID();}

        //Determine Update function.
        switch(buffers.length)
        {
            case 0:
            throw new RuntimeException("\"" + folderName + "\" appearantly didn't contain any music files.");

            case 1:
            loop = this::update;
            alSourcei(ID, AL_LOOPING, AL_TRUE);
            break;

            case 2:
            loop = this::update_Intro;
            break;

            default:
            loop = this::update_Outro;
            break;
        }
    }

    //YEAH! DOIN' NOTHIN'!
    private void update(){}

    private void update_Intro()
    {
        if(!looping && alGetSourcei(ID, AL_BUFFERS_PROCESSED) > 0)
        {
            alSourceUnqueueBuffers(ID);
            alSourcei(ID, AL_LOOPING, AL_TRUE);
            looping = true;
        }
    }

    private void update_Outro(){}

    /**
     * Plays the AudioBuffers assigned to this MusicSource. "alSourceQueueBuffers" is used to provide
     * seamless play from one audio file to another, allowing for looping a specific part of a song rather than
     * the whole thing.
     */
    public final void play()
    {
        alSourceStop(ID);
        alSourceUnqueueBuffers(ID);
        if(bufferIDs.length > 1)
        {
            alSourcei(ID, AL_LOOPING, AL_FALSE);
            looping = false;
        }
        alSourceQueueBuffers(ID, bufferIDs);
        resume();
    }
}
