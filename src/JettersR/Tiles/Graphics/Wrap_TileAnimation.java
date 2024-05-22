package JettersR.Tiles.Graphics;
/**
 * Author: Luke Sullivan
 * Last Edit: 12/16/2023
 */
import JettersR.Level;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Wrap_TileAnimation extends TileAnimation
{
	@FunctionalInterface
	public static interface WrapAction{public void perform(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_parameters);}

	//Static array of actions.
	private static final WrapAction[] WRAP_ACTIONS =
	{
		Wrap_TileAnimation::setWrap_x,
		Wrap_TileAnimation::setWrap_y,
		Wrap_TileAnimation::setWrap_xy,
		//
		Wrap_TileAnimation::linearWrap_x,
		Wrap_TileAnimation::linearWrap_y,
		Wrap_TileAnimation::linearWrap_xy,
		//
		Wrap_TileAnimation::smoothWrap_x,
		Wrap_TileAnimation::smoothWrap_y,
		Wrap_TileAnimation::smoothWrap_xy
	};
	public static final String[] WRAP_ACTION_NAMES =
	{
		"setWrap_x",
		"setWrap_y",
		"setWrap_xy",
		//
		"linearWrap_x",
		"linearWrap_y",
		"linearWrap_xy",
		//
		"smoothWrap_x",
		"smoothWrap_y",
		"smoothWrap_xy"
	};
	public static String getActionName(int slot){return WRAP_ACTION_NAMES[slot];}

	//1 Component = 2 bytes.
	protected static final byte[] PARAM_SIZE =
	{
		2 * Short.BYTES,
		2 * Short.BYTES,
		4 * Short.BYTES,
		//
		2 * Short.BYTES,
		2 * Short.BYTES,
		4 * Short.BYTES,
		//
		2 * Short.BYTES,
		2 * Short.BYTES,
		4 * Short.BYTES
	};
	public static int getParamSize(int actionID){return PARAM_SIZE[actionID];}
	

	//Action Parameters.
	private @fixed short[][] f_actionParameters;
	public @fixed short[][] f_getActionParameters(){return this.f_actionParameters;}
	public void f_setActionParameters(@fixed short[][] f_actionParameters){this.f_actionParameters = f_actionParameters;}

	//Wrap pointer.
	private @fixed short[] f_wrap_Pointer;


	/**
	 * 
	 * 
	 * @param tileMeshs
	 * @param timer
	 * @param actionIDs
	 * @param f_actionParameters
	 */
	public Wrap_TileAnimation(TileMesh[] tileMeshs, FrameAnimation_Timer timer, @fixed short[] f_wrap_Pointer, byte[] actionIDs, @fixed short[][] f_actionParameters)
	{
		super(tileMeshs, timer, actionIDs);

		this.f_actionParameters = f_actionParameters;

		//Set pointer.
		this.f_wrap_Pointer = (f_wrap_Pointer == null) ? new @fixed short[2] : f_wrap_Pointer;

		//Set TileMeshs' color pointer to the same one as this animation.
		for(int i = 0; i < tileMeshs.length; i++)
		{tileMeshs[i].f_setWrapPointer(this.f_wrap_Pointer);}

		//Perform first action just so the TileMeshs have something to start with.
		WRAP_ACTIONS[ this.actionIDs[0] ].perform(this.f_wrap_Pointer, 0, f_actionParameters[0]);
	}

	public byte typeValue(){return TileAnimation.TYPE_WRAP;}

	public @fixed short[] f_getWrap_Pointer(){return f_wrap_Pointer;}

	
	@Override
	public void update(@fixed int f_timeMod)
	{
		//Update timer and get what frame of the timer we are on.
		int frame = timer.update(f_timeMod);

		//Perform Action associated with this frame.
        WRAP_ACTIONS[ actionIDs[frame] ].perform
		(
			this.f_wrap_Pointer,//Wrap pointer, since the functions are static.
			f_div(timer.f_getTime(), timer.f_getFrameRate()),//FramePercent.
			f_actionParameters[frame]//Action parameters.
		);
	}

	@Override
	public void reset()
	{
		//Reset timer.
		timer.resetTimer();

		WRAP_ACTIONS[ actionIDs[0] ].perform
		(
			this.f_wrap_Pointer,//Wrap pointer.
			0,//FramePercent.
			f_actionParameters[0]//Action parameters.
		);
	}

	@Override
	public void perpareForDeletion()
	{
		//Give every TileMesh its own wrap pointer.
		for(int i = 0; i < tileMeshs.length; i++)
		{tileMeshs[i].f_setWrapPointer(new @fixed short[2]);}
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
		//Animation Type.
		//
		byte[] animationType = null;

		//Start from slot before this one.
		for(int i = slot-1; i >= 0; i--)
		{
			TileAnimation ta = level.getTileAnimation(i);

			if(ta instanceof Wrap_TileAnimation)
			{
				Wrap_TileAnimation wrAnim = (Wrap_TileAnimation)ta;

				//Is that animation's Wrap pointer the same as this one's.
				if(f_wrap_Pointer == wrAnim.f_getWrap_Pointer())
				{
					animationType = new byte[]{-1, (byte)i};
					break;
				}
			}
		}
		
		//Was a pointer not found?
		if(animationType == null)
		{
			//Set it to this type's value.
			animationType = new byte[]{typeValue()};
		}


		//Calculate size of frameData array.
		int paramSize = 0;
		for(int a = 0; a < f_actionParameters.length; a++)
		{
			short[] f_shortArray = f_actionParameters[a];

			//for(int b = 0; b < f_shortArray.length; b++){paramSize += Short.BYTES;}
			paramSize += (f_shortArray.length * Short.BYTES);
		}

		//Array for all frames.
		@fixed int[] f_frameTimes = timer.f_getRates();
		byte[] frameData = new byte
		[
			(f_frameTimes.length << 2)
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
			@fixed short[] f_pArr = f_actionParameters[i];
			for(int p = 0; p < f_pArr.length; p++)
			{
				@fixed short f_value = f_pArr[p];
				frameData[offset++] = (byte)((f_value & 0xFF00) >> 8);
				frameData[offset++] = (byte)(f_value & 0x00FF);
			}
		}


		//Create the final array.
		byte[] result = new byte
		[
			base.length
			+ animationType.length
			+ frameData.length
		];
		offset = 0;

		//Fill it.
		for(int i = 0; i < base.length; i++){result[offset++] = base[i];}
		for(int i = 0; i < animationType.length; i++){result[offset++] = animationType[i];}
		for(int i = 0; i < frameData.length; i++){result[offset++] = frameData[i];}

		//Return the result.
		return result;
	}


	//Four bytes for two values.
	//@fixed short
	//f_xWrap = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_xWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]);
	public static void setWrap_x(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_xWrap)
	{f_wrap_Pointer[0] = (short)( f_xWrap[0] % f_xWrap[1] );}


	//Four bytes for two values.
	//@fixed short
	//f_yWrap = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_yWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]);
	public static void setWrap_y(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_yWrap)
	{f_wrap_Pointer[1] = (short)( f_yWrap[0] % f_yWrap[1] );}


	//Eight bytes for four values.
	//@fixed short
	//f_xWrap = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_xWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]),
	//f_yWrap = Game.bytesToShort(actionParameters[4], actionParameters[5]),
	//f_yWrapSpan = Game.bytesToShort(actionParameters[6], actionParameters[7]);
	public static void setWrap_xy(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_wrap)
	{
		f_wrap_Pointer[0] = (short)( f_wrap[0] % f_wrap[1] );
		f_wrap_Pointer[1] = (short)( f_wrap[2] % f_wrap[3] );
	}


	//Four bytes for two values.
	//@fixed short
	//f_xWrapStart = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_xWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]);
	public static void linearWrap_x(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_xWrap)
	{
		f_wrap_Pointer[0] = (short)(f_xWrap[0] + f_mul(f_framePercent, f_xWrap[1]) % f_xWrap[1]);
		//f_print("f_wrap_Pointer[0]", f_wrap_Pointer[0]);
	}


	//Four bytes for two values.
	//@fixed short
	//f_yWrapStart = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_yWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]);
	public static void linearWrap_y(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_yWrap)
	{f_wrap_Pointer[1] = (short)(f_yWrap[0] + f_mul(f_framePercent, f_yWrap[1]) % f_yWrap[1]);}


	//Eight bytes for four values.
	//@fixed short
	//f_xWrapStart = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_xWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]),
	//f_yWrapStart = Game.bytesToShort(actionParameters[4], actionParameters[5]),
	//f_yWrapSpan = Game.bytesToShort(actionParameters[6], actionParameters[7]);
	public static void linearWrap_xy(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_wrap)
	{
		f_wrap_Pointer[0] = (short)(f_wrap[0] + f_mul(f_framePercent, f_wrap[1]) % f_wrap[1]);
		f_wrap_Pointer[1] = (short)(f_wrap[2] + f_mul(f_framePercent, f_wrap[3]) % f_wrap[3]);
	}


	//Four bytes for two values.
	//@fixed short
	//f_xWrapStart = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_xWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]);
	public static void smoothWrap_x(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_xWrap)
	{
		//Smooth step.
		@fixed int f_perc = f_mul( f_square(f_framePercent), (f_THREE - f_mul(f_TWO, f_framePercent) ) );

		//Set.
		f_wrap_Pointer[0] = (short)(f_xWrap[0] + f_mul(f_perc, f_xWrap[1]) % f_xWrap[1]);
	}


	//Four bytes for two values.
	//@fixed short
	//f_yWrapStart = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_yWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]);
	public static void smoothWrap_y(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_yWrap)
	{
		//Smooth step.
		@fixed int f_perc = f_mul( f_square(f_framePercent), (f_THREE - f_mul(f_TWO, f_framePercent) ) );

		//Set.
		f_wrap_Pointer[1] = (short)(f_yWrap[0] + f_mul(f_perc, f_yWrap[1]) % f_yWrap[1]);
	}


	//Eight bytes for four values.
	//@fixed short
	//f_xWrapStart = Game.bytesToShort(actionParameters[0], actionParameters[1]),
	//f_xWrapSpan = Game.bytesToShort(actionParameters[2], actionParameters[3]),
	//f_yWrapStart = Game.bytesToShort(actionParameters[4], actionParameters[5]),
	//f_yWrapSpan = Game.bytesToShort(actionParameters[6], actionParameters[7]);
	public static void smoothWrap_xy(short[] f_wrap_Pointer, @fixed int f_framePercent, @fixed short[] f_wrap)
	{
		//Smooth step.
		@fixed int f_perc = f_mul( f_square(f_framePercent), (f_THREE - f_mul(f_TWO, f_framePercent) ) );

		//Set.
		f_wrap_Pointer[0] = (short)(f_wrap[0] + f_mul(f_perc, f_wrap[1]) % f_wrap[1]);
		f_wrap_Pointer[1] = (short)(f_wrap[2] + f_mul(f_perc, f_wrap[3]) % f_wrap[3]);
	}
}
