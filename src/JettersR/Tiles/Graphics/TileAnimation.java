package JettersR.Tiles.Graphics;
/**
 * Runs a Timer with associated effects that be used on multiple TileMeshs.
 * 
 * Author: Luke Sullivan
 * Last Edit: 1/1/2024
 */
import JettersR.Game;
import JettersR.Level;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Util.Annotations.fixed;

public abstract class TileAnimation
{
	//Type constants.
	public static final byte
	TYPE_SPRITE_INDEX = 0,
	TYPE_COLOR = 1,
	TYPE_WRAP = 2,
	TYPE_SPRITE_COORD = 3;

	//TileMeshs to affect.
	protected TileMesh[] tileMeshs;

	//Animation timer.
	protected FrameAnimation_Timer timer;

	//Action IDs.
	protected byte[] actionIDs;

	
	/**
	 * Constructor. Takes a TileMesh array and Timer as input.
	 * 
	 * @param tileSprites
	 * @param timer
	 */
	public TileAnimation(TileMesh[] tileSprites, FrameAnimation_Timer timer, byte[] actionIDs)
	{
		//Set tileSprites.
		this.tileMeshs = tileSprites;

		//Set timer.
		this.timer = timer;

		//Set actionIDs.
		this.actionIDs = actionIDs;
	}

	public abstract byte typeValue();

	//TileMesh getters.
	public final TileMesh[] getTileMeshs(){return this.tileMeshs;}
	public final TileMesh getTileMesh(int slot){return this.tileMeshs[slot];}
	public final int getNumTileMeshs(){return tileMeshs.length;}

	//Timer getter.
	public final FrameAnimation_Timer getTimer(){return this.timer;}

	//ActionIDs getter/setter.
	public final byte[] getActionIDs(){return this.actionIDs;}
	public final void setActionIDs(byte[] actionIDs){this.actionIDs = actionIDs;}


	/**
	 * Updates this TileAnimation's timer.
	 * 
	 * @param f_timeMod A percentage that can be used for fast-forward and slow-down effects.
	 */
	public abstract void update(@fixed int f_timeMod);

	/**Resets this animation to the beginning.*/
	public abstract void reset();

	/**Gives every TileSprte associated with this TileAnimation its own pointer (depending of type of anim). To be used when deleting this animation.*/
	public abstract void perpareForDeletion();

	//public abstract void add(TileMesh tileSprite);


	public static byte paramSize_bytes(byte animationType, byte actionID)
	{
		switch(animationType)
		{
			case TYPE_SPRITE_INDEX: return SpriteIndex_TileAnimation.PARAM_SIZE[actionID];

			case TYPE_COLOR: return Color_TileAnimation.PARAM_SIZE[actionID];

			case TYPE_WRAP: return Wrap_TileAnimation.PARAM_SIZE[actionID];

			case TYPE_SPRITE_COORD: return SpriteCoord_TileAnimation.PARAM_SIZE[actionID];

			default: return 0;
		}
	}

	public static TileAnimation construct(TileMesh[] tileSprites, FrameAnimation_Timer timer, TileAnimation toGetPointerFrom,
	byte[] actionIDs, byte animationType, byte spriteSlot, byte[][] actionParameters)
	{
		switch(animationType)
		{
			case TYPE_SPRITE_INDEX: 
			{
				//Get pointer if needed.
				byte[] spriteIndecies_Pointer = (toGetPointerFrom == null) ? new byte[spriteSlot]//Create one using SpriteSlots value.
				: ((SpriteIndex_TileAnimation)toGetPointerFrom).getSpriteIndecies_Pointer();//Or get from the pointed to TileAnimation.

				//Create the new animation.
				return new SpriteIndex_TileAnimation(tileSprites, timer, spriteIndecies_Pointer, actionIDs, actionParameters);
			}

			case TYPE_COLOR:
			{
				//TileMeshs can't be bound to multiple Color animations, so no pointer copying is done here.
				return new Color_TileAnimation(tileSprites, timer, actionIDs, actionParameters);
			}

			case TYPE_WRAP:
			{
				//Create initial short array.
				@fixed short[][] f_actionParameters = new short[actionParameters.length][];

				for(int f = 0; f < f_actionParameters.length; f++)
				{
					//Cache this frame's byte array.
					byte[] currentArray = actionParameters[f];

					//Create array for this frame.
					f_actionParameters[f] = new short[currentArray.length >> 1];

					//For each short...
					for(int i = 0; i < f_actionParameters[f].length; i++)
					{
						//Combine the two bytes into a short.
						int slot = f << 1;
						f_actionParameters[f][i] = Game.bytesToShort(currentArray[slot], currentArray[slot+1]);
					}
				}

				@fixed short[] f_wrap_Pointer = (toGetPointerFrom == null) ? null : ((Wrap_TileAnimation)toGetPointerFrom).f_getWrap_Pointer();

				//Create the new animation.
				return new Wrap_TileAnimation(tileSprites, timer, f_wrap_Pointer, actionIDs, f_actionParameters);
			}

			case TYPE_SPRITE_COORD:
			{
				//Create initial short array.
				short[][] short_actionParameters = new short[actionParameters.length][];

				for(int f = 0; f < short_actionParameters.length; f++)
				{
					//Cache this frame's byte array.
					byte[] currentArray = actionParameters[f];

					//Create array for this frame.
					short_actionParameters[f] = new short[currentArray.length >> 1];

					//For each short...
					for(int i = 0; i < short_actionParameters[f].length; i++)
					{
						//Combine the two bytes into a short.
						int slot = f << 1;
						short_actionParameters[f][i] = Game.bytesToShort(currentArray[slot], currentArray[slot+1]);
					}
				}

				Sprite sprite_Pointer = (toGetPointerFrom == null) ? null : ((SpriteCoord_TileAnimation)toGetPointerFrom).getSprite_Pointer();

				//Create the new animation.
				return new SpriteCoord_TileAnimation(tileSprites, timer, sprite_Pointer, actionIDs, spriteSlot, short_actionParameters);
			}

			default: return null;
		}
	}


	//
	//Saving.
	//
	
	protected final byte[] translateBaseTo_DAT(Level level)
	{
		//Create array,
		byte[] result = new byte
		[
			1//NumTileMeshs
			+ (tileMeshs.length << 1)//TileMeshIDs
			+ 1//NumFrames
		];
		int offset = 0;


		//
		//Number of TileMeshs affected - 1 byte
		//
		result[offset++] = (byte)tileMeshs.length;


		//
		//TileMeshIDs - 2 bytes each (& 0xFFFF)
		//
		for(int a = 0; a < tileMeshs.length; a++)
		{
			TileMesh currentTileMesh = tileMeshs[a];

			for(int i = 0; i < level.getNumTileMeshs(); i++)
			{
				//Is this TileMesh the same as the current one from the level?
				if(currentTileMesh == level.getTileMeshs(i))
				{
					//Then the current slot is the ID.
					result[offset++] = (byte)((i & 0xFF00) >> 8);
					result[offset++] = (byte)(i & 0x00FF);
					break;
				}
			}
		}


		//
		//Number of Frames - 1 byte (& 0xFF)
		//
		result[offset++] = (byte)actionIDs.length;


		//Return result.
		return result;
	}

	public abstract byte[] translateTo_DAT(Level level, int slot);//, TileAnimation[] tileAnimations);
}
