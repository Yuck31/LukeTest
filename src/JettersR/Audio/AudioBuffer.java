package JettersR.Audio;
/**
 * This is an Audio Buffer from which a Audio Source can get its Sound from.
 * 
 * Author: Luke Sullivan
 * Last Edit: 4/3/2022
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.lwjgl.openal.AL10.*;

import com.jcraft.oggdecoder.OggData;
import com.jcraft.oggdecoder.OggDecoder;

import org.lwjgl.openal.AL11;

public class AudioBuffer
{
    private int ID = -1;
    //private String name;

    /**Constructs a new Sound Buffer from .WAV data.*/
    private AudioBuffer(WaveData waveFile)//, String name)
    {
        //Set the name.
        //this.name = name;

        //Set the ID.
        ID = alGenBuffers();

        //Give the Data to OpenAL.
        alBufferData(ID, waveFile.format, waveFile.data, waveFile.sampleRate);

        //Dispose the WaveData.
        waveFile.dispose();
    }

    /**Constructs a new Sound Buffer from .OGG data.*/
    private AudioBuffer(OggData oggData)
    {
        //Set the ID.
        ID = alGenBuffers();

        //Give the Data to OpenAL.
        alBufferData(ID, getOpenAlFormat(oggData.channels, 16/*I tried...*/),
        oggData.data, oggData.rate);
    }

    /**Constructs a new Sound Buffer from a .WAV file.*/
    public static AudioBuffer wav(File file)
    {
        //Acquire the Wave Data from the file.
        WaveData waveFile = WaveData.create(file);

        //Create the new Sound Buffer.
        return new AudioBuffer(waveFile);
    }

    private static final OggDecoder OGG_DECODER = new OggDecoder();

    /**Constructs a new Sound Buffer from a .OGG file.*/
    public static AudioBuffer ogg(File file)
    {
        try
        {
            //Convert the file to an InputStream.
            FileInputStream input = new FileInputStream(file);

            //Acquire the Uncompressed Wave Data from the file.
            OggData oggData = OGG_DECODER.getData(input);

            //Close the input stream to free up resources.
            input.close();
            
            //Create the new Audio Buffer.
            return new AudioBuffer(oggData);
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}
        //
        return null;
    }

    /**Gets the OpenAL format of this Sound.*/
	public static int getOpenAlFormat(int channels, int bitsPerSample)
    {
		if(channels == 1)
        {
			return bitsPerSample == 8 ? AL11.AL_FORMAT_MONO8
            : AL11.AL_FORMAT_MONO16;
		}
        else
        {
			return bitsPerSample == 8 ? AL11.AL_FORMAT_STEREO8
            : AL11.AL_FORMAT_STEREO16;
		}
	}

    /**Returns this Sound Buffer's ID.*/
    public final int getID(){return ID;}

    /**Returns this Sound Effect's name.*/
    //public final String getName(){return name;}

    /**Deletes this Sound Effect's data.*/
    public final void delete(){alDeleteBuffers(ID);}
}
