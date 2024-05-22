package JettersR.Audio;
/**
 * This is an Audio Source which can play Audio from whatever Audio Buffers you want.
 * 
 * Sounds in OpenAL are divided into two parts:
 * -The Sound Buffer, which contains the Sound itself
 * -and the Sound Source, which is effectivly a Speaker for the sound to be played from.
 * 
 * If you wanted to play multiple of the same sound concerrently, you can just
 * use multiple Sound Sources to play from the same Sound Buffer.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/12/2022
 */
import static org.lwjgl.openal.AL11.*;

public abstract class AudioSource
{
    protected int ID = -1;

    protected AudioSource(){this.ID = alGenSources();}

    /**Returns this Audio Source's ID.*/
    public final int getID(){return ID;}

    /**Plays the given buffer through this Source from the Beginning.*/
    public final void play(int buffer)
    {
        stop();
        alSourcei(ID, AL_BUFFER, buffer);
        resume();
    }

    /**Resumes this Source if it was Paused.*/
    public final void resume(){alSourcePlay(ID);}

    /**Pauses this Source.*/
    public final void pause(){alSourcePause(ID);}

    /**Stops and rewinds this Source.*/
    public final void stop(){alSourceStop(ID);}

    /**Returns whether or not this Source is currently playing.*/
    public final boolean isPlaying(){return alGetSourcei(ID, AL_SOURCE_STATE) == AL_PLAYING;}

    /**Sets whether or not this Source should loop the Sounds it plays.*/
    public final void setLooping(boolean loop){alSourcei(ID, AL_LOOPING, (loop) ? AL_TRUE : AL_FALSE);}

    /**Sets the Volume (well, gain) of this source.*/
    public final void setVolume(float volume){alSourcef(ID, AL_GAIN, volume);}

    /**Sets the pitch of this sound source (affects the speed of the sound as a result).*/
    public final void setPitch(float pitch){alSourcef(ID, AL_PITCH, pitch);}

    /**Deletes this Sound Source's data.*/
    public final void delete()
    {
        stop();
        alDeleteSources(ID);
    }
}
