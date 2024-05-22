package JettersR.Tiles.Graphics;
/**
 * Author: Luke Sullivan
 * Last Edit: 3/9/2024
 */
import org.joml.Vector4f;

import JettersR.Level;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Color_TileAnimation extends TileAnimation
{
	@FunctionalInterface
	public static interface ColorAction
	{
		//Returns true if all TileMeshs need updating.
		public boolean perform(Vector4f blendingColor_Pointer, float framePercent, float[] rgb);
	}

	//Static array of actions.
	private static final ColorAction[] COLOR_ACTIONS =
	{
		Color_TileAnimation::setRGB,
		Color_TileAnimation::setColor,
		Color_TileAnimation::interpolateRGB,
		Color_TileAnimation::interpolateColor
	};
	public static final String[] COLOR_ACTION_NAMES =
	{
		"setRGB",
		"setColor",
		"interpolateRGB",
		"interpolateColor"
	};
	public static String getActionName(int slot){return COLOR_ACTION_NAMES[slot];}

	//1 Component = 1 byte.
	protected static final byte[] PARAM_SIZE =
	{
		3,
		4,
		6,
		8
	};
	public static int getParamSize(int actionID){return PARAM_SIZE[actionID];}

	//Action Parameters. [1 byte each, saved as 0-255 bytes]
	private float[][] actionParameters;
	public float[][] getActionParameters(){return this.actionParameters;}
	public void setActionParameters(float[][] actionParameters){this.actionParameters = actionParameters;}

	//BlendingColor pointer to be shared with all TileMeshs affected.
	private Vector4f blendingColor_Pointer;


	/**
	 * 
	 * 
	 * @param tileMeshs
	 * @param timer
	 * @param actionIDs
	 * @param byte_actionParameters
	 */
	public Color_TileAnimation(TileMesh[] tileMeshs, FrameAnimation_Timer timer, byte[] actionIDs, byte[][] byte_actionParameters)
	{
		super(tileMeshs, timer, actionIDs);

		//Convert 0-255 bytes to floats.
		this.actionParameters = new float[byte_actionParameters.length][];
		for(int i = 0; i < actionParameters.length; i++)
		{
			//Make length the same as the current sequence of bytes.
			this.actionParameters[i] = new float[byte_actionParameters[i].length];

			//Iterate through each byte in the sequence.
			for(int b = 0; b < byte_actionParameters[i].length; b++)
			{
				//Convert 0 - 255 byte to 0.0f - 1.0f float.
				this.actionParameters[i][b] = (byte_actionParameters[i][b] & 0xFF) / 255.0f;
			}
		}

		//Create color pointer.
		this.blendingColor_Pointer = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

		//Set TileMeshs' color pointer to the same one as this animation.
		for(int i = 0; i < tileMeshs.length; i++)
		{tileMeshs[i].setBlendingColorPointer(this.blendingColor_Pointer);}

		//Perform first action just so the TileMeshs have something to start with.
		boolean shouldUpdateAll = COLOR_ACTIONS[ this.actionIDs[0] ].perform(this.blendingColor_Pointer, 0.0f, actionParameters[0]);
		if(shouldUpdateAll){updateIsTranslucent();}
	}

	public byte typeValue(){return TileAnimation.TYPE_COLOR;}
	


	@Override
	public void update(@fixed int f_timeMod)
	{
		//Update timer and get what frame of the timer we are on.
		int frame = timer.update(f_timeMod);

		//Perform Action associated with this frame.
        boolean shouldUpdateAll = COLOR_ACTIONS[ actionIDs[frame] ].perform
		(
			this.blendingColor_Pointer,//Blending Color pointer, since the functions are static.
			f_toFloat( f_div(timer.f_getTime(), timer.f_getFrameRate()) ),//FramePercent.
			actionParameters[frame]//Action parameters.
		);

		if(shouldUpdateAll){updateIsTranslucent();}
	}

	@Override
	public void reset()
	{
		//Reset timer.
		timer.resetTimer();

		//Run first Action.
        boolean shouldUpdateAll = COLOR_ACTIONS[ actionIDs[0] ].perform
		(
			this.blendingColor_Pointer,//Blending Color pointer.
			0,//FramePercent.
			actionParameters[0]//Action parameters.
		);

		if(shouldUpdateAll){updateIsTranslucent();}
	}

	@Override
	public void perpareForDeletion()
	{
		//Give every TileMesh its own color.
		for(int i = 0; i < tileMeshs.length; i++)
		{tileMeshs[i].setBlendingColorPointer(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));}
	}


	private void updateIsTranslucent()
	{
		//Update TileMeshs' isTranslucent if needed.
		boolean isTranslucent = (blendingColor_Pointer.w < 1.0f);
		if(isTranslucent != tileMeshs[0].isTranslucent())
		{
			for(int i = 0; i < tileMeshs.length; i++)
			{tileMeshs[i].setIsTranslucent(isTranslucent);}
		}
	}


	//
	//Saving.
	//

	@Override
	public byte[] translateTo_DAT(Level level, int slot)//, TileAnimation[] tileAnimations)
	{
		//Number of TileMeshs affected, TileMeshIDs, and Number of Frames.
		byte[] base = super.translateBaseTo_DAT(level);


		//
		//Animation Type. (no pointer getting needed)
		//
		byte animationType = typeValue();


		//Calculate size of frameData array.
		int paramSize = 0;
		for(int a = 0; a < actionParameters.length; a++)
		{
			float[] floatArray = actionParameters[a];
			for(int f = 0; f < floatArray.length; f++){paramSize += 1;}
		}

		//Array for all frames.
		@fixed int[] f_frameTimes = timer.f_getRates();
		byte[] frameData = new byte
		[
			(f_frameTimes.length * Integer.BYTES)
			+ actionIDs.length
			+ paramSize
		];
		int offset = 0;

		//Go through each frame and collect their data.
		for(int i = 0; i < f_frameTimes.length; i++)
		{
			//
			//FrameTime - 4 fixed point bytes
			//
			@fixed int f_t = f_frameTimes[i];
			frameData[offset++] = (byte)((f_t & 0xFF000000) >> 24);
			frameData[offset++] = (byte)((f_t & 0x00FF0000) >> 16);
			frameData[offset++] = (byte)((f_t & 0x0000FF00) >> 8);
			frameData[offset++] = (byte)(f_t & 0x000000FF);

			//
			//ActionID - 1 byte, signed
			//
			frameData[offset++] = actionIDs[i];

			//
			//Action parameters - depends on animation type and ActionID
			//
			float[] pArr = actionParameters[i];
			for(int p = 0; p < pArr.length; p++)
			{frameData[offset++] = (byte)(pArr[p] * 255);}
		}


		//Create the final array.
		byte[] result = new byte
		[
			base.length
			+ 1
			+ frameData.length
		];
		offset = 0;

		//Fill it.
		for(int i = 0; i < base.length; i++){result[offset++] = base[i];}
		result[offset++] = animationType;
		for(int i = 0; i < frameData.length; i++){result[offset++] = frameData[i];}

		//Return the result.
		return result;
	}
	

	//
	//Actions.
	//

	//The three bytes are the full parameters. They form an opaque color.
	public static boolean setRGB(Vector4f blendingColor_Pointer, float framePercent, float[] rgb)
	{
		blendingColor_Pointer.x = rgb[0];
		blendingColor_Pointer.y = rgb[1];
		blendingColor_Pointer.z = rgb[2];

		//System.out.println(blendingColor_Pointer.x);

		//Updating not needed.
		return false;
	}


	//The four bytes are the full parameters. They form a possibly transperant color.
	public static boolean setColor(Vector4f blendingColor_Pointer, float framePercent, float[] argb)
	{
		//Set color.
		blendingColor_Pointer.set(argb[1], argb[2], argb[3], argb[0]);

		//For isTranslucent update.
		return true;
	}


	//Six bytes form two colors without alpha.
	public static boolean interpolateRGB(Vector4f blendingColor_Pointer, float framePercent, float[] startRGB_endRGB)
	{
		//Linear interpolation: (range * percentage) + start.
		blendingColor_Pointer.x = ((startRGB_endRGB[3] - startRGB_endRGB[0]) * framePercent) + startRGB_endRGB[0];//Red
        blendingColor_Pointer.y = ((startRGB_endRGB[4] - startRGB_endRGB[1]) * framePercent) + startRGB_endRGB[1];//Green
        blendingColor_Pointer.z = ((startRGB_endRGB[5] - startRGB_endRGB[2]) * framePercent) + startRGB_endRGB[2];//Blue

		//System.out.println(blendingColor_Pointer.x);

		//Updating not needed.
		return false;
	}


	//Eight bytes form two colors.
	public static boolean interpolateColor(Vector4f blendingColor_Pointer, float framePercent, float[] startRGB_endRGB)
	{
		//Linear interpolation: (range * percentage) + start.
		blendingColor_Pointer.w = ((startRGB_endRGB[4] - startRGB_endRGB[0]) * framePercent) + startRGB_endRGB[0];//Alpha
        blendingColor_Pointer.x = ((startRGB_endRGB[5] - startRGB_endRGB[1]) * framePercent) + startRGB_endRGB[1];//Red
        blendingColor_Pointer.y = ((startRGB_endRGB[6] - startRGB_endRGB[2]) * framePercent) + startRGB_endRGB[2];//Green
		blendingColor_Pointer.z = ((startRGB_endRGB[7] - startRGB_endRGB[3]) * framePercent) + startRGB_endRGB[3];//Blue

		//For isTranslucent update.
		return true;
	}
}
