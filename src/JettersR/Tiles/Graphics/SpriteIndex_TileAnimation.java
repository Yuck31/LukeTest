package JettersR.Tiles.Graphics;
/**
 * 
 */
import JettersR.Level;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Util.Annotations.fixed;

public class SpriteIndex_TileAnimation extends TileAnimation
{
	@FunctionalInterface
	public static interface SpriteIndexAction{public void perform(byte[] spriteIndecies_Pointer, byte[] parameters);}

	//Static array of actions.
	private static final SpriteIndexAction[] SPRITE_INDEX_ACTIONS =
	{
		SpriteIndex_TileAnimation::setSpriteIndex_0,
		SpriteIndex_TileAnimation::setSpriteIndecies_01,
		SpriteIndex_TileAnimation::setSpriteIndecies_012,
		SpriteIndex_TileAnimation::setSpriteIndex_any,
		SpriteIndex_TileAnimation::setSpriteIndex_anyTwo
	};
	public static final String[] SPRITE_INDEX_ACTION_NAMES =
	{
		"setSpriteIndex_0",
		"setSpriteIndecies_01",
		"setSpriteIndecies_012",
		"setSpriteIndex_any",
		"setSpriteIndex_anyTwo"
	};
	public static String getActionName(int slot){return SPRITE_INDEX_ACTION_NAMES[slot];}

	//1 Component = 1 byte.
	protected static final byte[] PARAM_SIZE =
	{
		1,
		2,
		3,
		2,
		4
	};
	public static int getParamSize(int actionID){return PARAM_SIZE[actionID];}

	//Action Parameters.
	private byte[][] actionParameters;
	public byte[][] getActionParameters(){return this.actionParameters;}
	public void setActionParameters(byte[][] actionParameters){this.actionParameters = actionParameters;}

	//Sprite indecies pointer to be shared with all TileMeshs affected.
	private byte[] spriteIndecies_Pointer;


	/**
	 * 
	 * 
	 * @param tileMeshs
	 * @param timer
	 * @param actionIDs
	 * @param actionParameters
	 */
	public SpriteIndex_TileAnimation(TileMesh[] tileMeshs, FrameAnimation_Timer timer, byte[] spriteIndecies_Pointer, byte[] actionIDs, byte[][] actionParameters)
	{
		super(tileMeshs, timer, actionIDs);

		this.actionParameters = actionParameters;

		//Set pointer.
		this.spriteIndecies_Pointer = spriteIndecies_Pointer;

		//Set TileMeshs' color pointer to the same one as this animation.
		for(int i = 0; i < tileMeshs.length; i++)
		{tileMeshs[i].setSpriteIndeciesPointer(this.spriteIndecies_Pointer);}

		//Perform first action just so the TileMeshs have something to start with.
		SPRITE_INDEX_ACTIONS[ this.actionIDs[0] ].perform(this.spriteIndecies_Pointer, actionParameters[0]);
	}

	public byte typeValue(){return TileAnimation.TYPE_SPRITE_INDEX;}

	public byte[] getSpriteIndecies_Pointer(){return spriteIndecies_Pointer;}

	@Override
	public void update(@fixed int f_timeMod)
	{
		//Update timer and get what frame of the timer we are on.
		int frame = timer.update(f_timeMod);

		//Perform Action associated with this frame.
        SPRITE_INDEX_ACTIONS[ actionIDs[frame] ].perform
		(
			this.spriteIndecies_Pointer,
			actionParameters[frame]
		);
			//f_timeMod, f_div(timer.f_getTime(), timer.f_getFrameRate()));
	}

	@Override
	public void reset()
	{
		//Reset timer.
		timer.resetTimer();

		//Run first Action.
        SPRITE_INDEX_ACTIONS[ actionIDs[0] ].perform
		(
			this.spriteIndecies_Pointer,
			actionParameters[0]
		);
	}

	@Override
	public void perpareForDeletion()
	{
		//Give every TileMesh its own indecies array.
		for(int i = 0; i < tileMeshs.length; i++)
		{tileMeshs[i].setSpriteIndeciesPointer(new byte[spriteIndecies_Pointer.length]);}
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

			if(ta instanceof SpriteIndex_TileAnimation)
			{
				SpriteIndex_TileAnimation spAnim = (SpriteIndex_TileAnimation)ta;

				//Is that animation's Sprite Indecies pointer the same as this one's?
				if(spriteIndecies_Pointer == spAnim.getSpriteIndecies_Pointer())
				{
					animationType = new byte[]{-1, (byte)i};
					break;
				}
			}
		}
		
		//Was a pointer not found?
		if(animationType == null)
		{
			//Set it to this type's value, following with the number of slots in the index array.
			animationType = new byte[]
			{
				typeValue(),
				(byte)spriteIndecies_Pointer.length
			};
		}


		//Calculate size of frameData array.
		int paramSize = 0;
		for(int a = 0; a < actionParameters.length; a++)
		{
			byte[] byteArray = actionParameters[a];
			for(int b = 0; b < byteArray.length; b++){paramSize += 1;}
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
			byte[] pArr = actionParameters[i];
			for(int p = 0; p < pArr.length; p++){frameData[offset++] = pArr[p];}
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


	//
	//Actions.
	//

	//The one byte is the full parameter.
	public static void setSpriteIndex_0(byte[] spriteIndecies_Pointer, byte[] indecies)
	{spriteIndecies_Pointer[0] = indecies[0];}



	//The two bytes are the full parameters.
	//index0 = actionParameters[0],
	//index1 = actionParameters[1];
	public static void setSpriteIndecies_01(byte[] spriteIndecies_Pointer, byte[] indecies)
	{
		spriteIndecies_Pointer[0] = indecies[0];
		spriteIndecies_Pointer[1] = indecies[1];
	}



	//The three bytes are the full parameters.
	//index0 = actionParameters[0],
	//index1 = actionParameters[1],
	//index2 = actionParameters[2];
	public static void setSpriteIndecies_012(byte[] spriteIndecies_Pointer, byte[] indecies)
	{
		spriteIndecies_Pointer[0] = indecies[0];
		spriteIndecies_Pointer[1] = indecies[1];
		spriteIndecies_Pointer[2] = indecies[2];
	}
	


	//The two bytes are the full parameters.
	//byte
	//slot = actionParameters[0],
	//index = actionParameters[1];
	public static void setSpriteIndex_any(byte[] spriteIndecies_Pointer, byte[] slot_index)
	{spriteIndecies_Pointer[slot_index[0]] = slot_index[1];}
	


	//The four bytes are the full parameters.
	//byte
	//slot0 = actionParameters[0],
	//index0 = actionParameters[1],
	//slot1 = actionParameters[2],
	//index1 = actionParameters[3];
	public static void setSpriteIndex_anyTwo(byte[] spriteIndecies_Pointer, byte[] slots_indecies)
	{
		spriteIndecies_Pointer[slots_indecies[0]] = slots_indecies[1];
		spriteIndecies_Pointer[slots_indecies[2]] = slots_indecies[3];
	}
}
