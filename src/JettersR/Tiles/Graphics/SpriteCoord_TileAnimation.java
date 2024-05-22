package JettersR.Tiles.Graphics;
/**
 * 
 */
import JettersR.Level;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class SpriteCoord_TileAnimation extends TileAnimation
{
	@FunctionalInterface
	public static interface SpriteCoordAction{public void perform(Sprite sprite_Pointer, @fixed int f_framePercent, short[] parameters);}

	//Static array of actions.
	private static final SpriteCoordAction[] SPRITE_COORD_ACTIONS =
	{
		SpriteCoord_TileAnimation::setSpriteCoord_x,
		SpriteCoord_TileAnimation::setSpriteCoord_y,
		SpriteCoord_TileAnimation::setSpriteCoord_xy,
		//
		SpriteCoord_TileAnimation::linearSpriteCoord_x,
		SpriteCoord_TileAnimation::linearSpriteCoord_y,
		SpriteCoord_TileAnimation::linearSpriteCoord_xy
	};
	public static final String[] SPRITE_COORD_ACTION_NAMES =
	{
		"setSpriteCoord_x",
		"setSpriteCoord_y",
		"setSpriteCoord_xy",
		//
		"linearSpriteCoord_x",
		"linearSpriteCoord_y",
		"linearSpriteCoord_xy"
	};
	public static String getActionName(int slot){return SPRITE_COORD_ACTION_NAMES[slot];}

	//1 Component = 2 bytes.
	protected static final byte[] PARAM_SIZE =
	{
		1 * Short.BYTES,
		1 * Short.BYTES,
		2 * Short.BYTES,
		//
		2 * Short.BYTES,
		2 * Short.BYTES,
		4 * Short.BYTES,
	};
	public static int getParamSize(int actionID){return PARAM_SIZE[actionID];}


	//Action Parameters.
	private short[][] actionParameters;
	public short[][] getActionParameters(){return this.actionParameters;}
	public void setActionParameters(short[][] actionParameters){this.actionParameters = actionParameters;}

	//Sprite pointer to be shared with all TileMeshs affected.
	private Sprite sprite_Pointer;

	//Sprite slot in TileMeshs that is affected.
	private byte spriteSlot;


	/**
	 * 
	 * 
	 * @param tileMeshs
	 * @param timer
	 * @param actionIDs
	 * @param spriteSlot
	 * @param actionParameters
	 */
	public SpriteCoord_TileAnimation(TileMesh[] tileMeshs, FrameAnimation_Timer timer, Sprite sprite_Pointer, byte[] actionIDs, byte spriteSlot, short[][] actionParameters)
	{
		super(tileMeshs, timer, actionIDs);

		this.actionParameters = actionParameters;

		this.spriteSlot = spriteSlot;

		//Only make a new pointer if one wasn't passed in.
		this.sprite_Pointer = (sprite_Pointer == null) ? tileMeshs[0].getSprites()[spriteSlot] : sprite_Pointer;

		//Set TileMeshs' color pointer to the same one as this animation.
		for(int i = 0; i < tileMeshs.length; i++)
		{tileMeshs[i].setSpritePointer(spriteSlot, this.sprite_Pointer);}

		//Perform first action just so the TileMeshs have something to start with.
		SPRITE_COORD_ACTIONS[ this.actionIDs[0] ].perform(this.sprite_Pointer, 0, actionParameters[0]);
	}

	/**Creates a new animation using the same Sprite pointer as this one.*/
	//public SpriteCoord_TileAnimation copyPointer(TileMesh[] tileSprites, FrameAnimation_Timer timer, byte[] actionIDs, byte spriteSlot, short[][] actionParameters)
	//{return new SpriteCoord_TileAnimation(tileSprites, timer, this.sprite_Pointer, actionIDs, spriteSlot, actionParameters);}

	public byte typeValue(){return TileAnimation.TYPE_SPRITE_COORD;}


	public byte getSpriteSlot(){return spriteSlot;}

	public Sprite getSprite_Pointer(){return sprite_Pointer;}

	
	@Override
	public void update(@fixed int f_timeMod)
	{
		//Update timer and get what frame of the timer we are on.
		int frame = timer.update(f_timeMod);

		//Perform Action associated with this frame.
        SPRITE_COORD_ACTIONS[ actionIDs[frame] ].perform
		(
			this.sprite_Pointer,//Sprite pointer, since the functions are static.
			f_div(timer.f_getTime(), timer.f_getFrameRate()),//FramePercent.
			actionParameters[frame]//Action parameters.
		);
	}

	@Override
	public void reset()
	{
		//Reset timer.
		timer.resetTimer();

		//Run first Action.
        SPRITE_COORD_ACTIONS[ actionIDs[0] ].perform
		(
			this.sprite_Pointer,//Sprite pointer.
			0,//FramePercent.
			actionParameters[0]//Action parameters.
		);
	}

	@Override
	public void perpareForDeletion()
	{
		//Perform beginning of first action to reset sprite.
		SPRITE_COORD_ACTIONS[ this.actionIDs[0] ].perform(this.sprite_Pointer, 0, actionParameters[0]);
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

			if(ta instanceof SpriteCoord_TileAnimation)
			{
				SpriteCoord_TileAnimation spAnim = (SpriteCoord_TileAnimation)ta;

				//Is that animation's Sprite pointer the same as this one's?
				if(sprite_Pointer == spAnim.getSprite_Pointer())
				{
					//We can get our pointer from it on load.
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
		for(int a = 0; a < actionParameters.length; a++)
		{
			short[] shortArray = actionParameters[a];
			for(int b = 0; b < shortArray.length; b++){paramSize += Short.BYTES;}
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
			short[] pArr = actionParameters[i];
			for(int p = 0; p < pArr.length; p++)
			{
				short value = pArr[p];
				frameData[offset++] = (byte)((value & 0xFF00) >> 8);
				frameData[offset++] = (byte)(value & 0x00FF);
			}
		}


		//Create the final array.
		byte[] result = new byte
		[
			base.length
			+ animationType.length
			+ 1//SpriteSlot
			+ frameData.length
		];
		offset = 0;

		//Fill it.
		for(int i = 0; i < base.length; i++){result[offset++] = base[i];}
		for(int i = 0; i < animationType.length; i++){result[offset++] = animationType[i];}
		result[offset++] = spriteSlot;
		for(int i = 0; i < frameData.length; i++){result[offset++] = frameData[i];}

		//Return the result.
		return result;
	}


	//
	//Actions.
	//
	
	//2 bytes.
	public static void setSpriteCoord_x(Sprite sprite_Pointer, @fixed int f_framePercent, short[] sprX)
	{sprite_Pointer.setX(sprX[0]);}


	//2 bytes.
	public static void setSpriteCoord_y(Sprite sprite_Pointer, @fixed int f_framePercent, short[] sprY)
	{sprite_Pointer.setY(sprY[0]);}


	//4 bytes.
	public static void setSpriteCoord_xy(Sprite sprite_Pointer, @fixed int f_framePercent, short[] sprCoords)
	{
		sprite_Pointer.setX(sprCoords[0]);
		sprite_Pointer.setY(sprCoords[1]);
	}


	//4 bytes.
	//0 = startX, 1 = endX.
	public static void linearSpriteCoord_x(Sprite sprite_Pointer, @fixed int f_framePercent, short[] sprX)
	{
		@fixed int f_x = ((sprX[1] - sprX[0]) * f_framePercent) + fixed(sprX[0]);

		sprite_Pointer.setX(f_toInt(f_x));
	}


	//4 bytes.
	//0 = startY, 1 = endY.
	public static void linearSpriteCoord_y(Sprite sprite_Pointer, @fixed int f_framePercent, short[] sprY)
	{
		@fixed int f_y = ((sprY[1] - sprY[0]) * f_framePercent) + fixed(sprY[0]);

		sprite_Pointer.setY(f_toInt(f_y));
	}


	//8 bytes.
	//0 = startX, 1 = endX, 2 = startY, 3 = endY.
	public static void linearSpriteCoord_xy(Sprite sprite_Pointer, @fixed int f_framePercent, short[] sprCoords)
	{
		@fixed int f_x = ((sprCoords[1] - sprCoords[0]) * f_framePercent) + fixed(sprCoords[0]);
		@fixed int f_y = ((sprCoords[3] - sprCoords[2]) * f_framePercent) + fixed(sprCoords[2]);

		sprite_Pointer.setX(f_toInt(f_x));
		sprite_Pointer.setY(f_toInt(f_y));
	}
}