package JettersR.Audio;
/**
 * This is a class from ThinMatrix that can read .WAV sound files.
 * 
 * Author: ThinMatrix
 * Edited by: Luke Sullivan
 * Last Edit: 4/2/2021
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;

public class WaveData
{
	final int format;
	final int sampleRate;
	final int totalBytes;
	final int bytesPerFrame;
	final ByteBuffer data;

	private final AudioInputStream audioStream;
	private final byte[] dataArray;

	/**Constructor.*/
	private WaveData(AudioInputStream stream)
    {
		this.audioStream = stream;
		//
		AudioFormat audioFormat = stream.getFormat();
		format = AudioBuffer.getOpenAlFormat(audioFormat.getChannels(), audioFormat.getSampleSizeInBits());
		//
		this.sampleRate = (int) audioFormat.getSampleRate();
		this.bytesPerFrame = audioFormat.getFrameSize();
		this.totalBytes = (int) (stream.getFrameLength() * bytesPerFrame);
		this.data = BufferUtils.createByteBuffer(totalBytes);
		this.dataArray = new byte[totalBytes];
		loadData();
	}

	/**Disposes this WaveData.*/
	protected final void dispose()
    {
		try
        {
			audioStream.close();
			data.clear();
		}
        catch(IOException e){e.printStackTrace();}
	}
	
	private ByteBuffer loadData()
    {
		try
        {
			int bytesRead = audioStream.read(dataArray, 0, totalBytes);
			data.clear();
			data.put(dataArray, 0, bytesRead);
			data.flip();
		}
        catch(IOException e)
        {
			e.printStackTrace();
			System.err.println("Couldn't read bytes from audio stream.");
		}
		return data;
	}

	/**Obtains WaveData from a File.*/
	protected static WaveData create(File file)
    {
		AudioInputStream audioStream = null;
		try{audioStream = AudioSystem.getAudioInputStream(file);}
        catch(UnsupportedAudioFileException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}

		WaveData wavStream = new WaveData(audioStream);
		return wavStream;
	}
}