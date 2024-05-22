package JettersR.Audio;
/**
 * SoundSource are AudioSoources meant to be utilized within the positional space of a level.
 * This allows for stuff like audio panning and even the doppler effect.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/12/2022
 */
import static org.lwjgl.openal.AL11.*;

public class SoundSource extends AudioSource
{
    /**Constructs a new Sound Source.*/
    public SoundSource()
    {
        
        //alSourcef(ID, AL_GAIN, 1f);
        //alSourcef(ID, AL_PITCH, 1f);
        //alSource3f(ID, AL_POSITION, 0f, 0f, 0f);

        alSourcef(ID, AL_ROLLOFF_FACTOR, 2);
        alSourcef(ID, AL_REFERENCE_DISTANCE, 6);
        alSourcef(ID, AL_MAX_DISTANCE, 50);
    }

    public void play(int buffer0, int buffer1)
    {
        stop();
        
        alSourceQueueBuffers(ID, buffer0);
        resume();

        alSourceQueueBuffers(ID, buffer1);
        alSourcei(ID, AL_LOOPING, AL_TRUE);
        alSourceUnqueueBuffers(ID);
    }

    /**Sets the spacial position of this sound source.*/
    public final void setPosition(float x, float y, float z)
    {alSource3f(ID, AL_POSITION, x, y, z);}

    /**
     * Sets the "Velocity" of this sound source.
     * It's effectivly meant to simulate the effects of the Doppler Effect:
     * Sounds moving towards you increase in pitch whereas Sounds moving away from you decrease in pitch.
     * This is what causes stuff like the "NNNYYYYOOMMMMM" of a car racing by.
     */
    public final void setVelocity(float x, float y, float z)
    {alSource3f(ID, AL_POSITION, x, y, z);}
}
