package JettersR.Entities;
/**
 * 
 */
import JettersR.Level;
import JettersR.Entities.Components.CollisionObject;
//import JettersR.Entities.Components.PhysicsComponent;
import JettersR.Graphics.Screen;
import JettersR.Util.Annotations.fixed;
import JettersR.Util.Shapes.Shapes3D.AAB_Box;

public class SoftBlock extends Entity
{
	private transient CollisionObject collisionObject;
	private transient AAB_Box box = new AAB_Box(Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE, 0, 0, 0);

	public SoftBlock(@fixed int f_x, @fixed int f_y, @fixed int f_z)
	{
		super(f_x, f_y, f_z);

		//Make collision object.
		this.collisionObject = new CollisionObject(this, box);

		//Set Collision Response.
        collisionObject.setCollisionResponse
        (
            (entity, shape, position, velocity, thisPosition) ->
            {
				//if(physicsComponent.doesEject())
				//{
					box.putOutComposite(shape, entity, position, velocity, position, thisPosition);
				//}
				//Entity e = physicsComponent.getEntity();
			}
		);
	}

	@Override
	public void init(Level level)
	{
		this.level = level;
	}

	@Override
	//This dosn't need to do anything.
	public void update(@fixed int f_timeMod){}

	/*
	 * int tileInfo = physicsComponent.getCurrentTileInfo() ;
	 * 
	 * //Are we blowing up a tile?
	 * if(tileInfo != 0)
	 * {
	 *		if((tileInfo & Tiles.TILE_EFFECT_PORTION) == Tiles.EFFECTS_DESTROY_FIRE + element)
	 * 		{
	 * 			level.blowUpTile(f_shapePosition.x >> level.FIXED_TILE_BITS, f_shapePosition.y >> level.FIXED_TILE_BITS, f_shapePosition.z >> level.FIXED_TILE_BITS);
	 * 		}
	 * 		return;
	 * }
	 * //Otherwise, it's an entity we're dealing with.
	 * 
	 * //Is the entity one that can be damaged?
	 * ...
	 */

	@Override
	public void render(Screen screen, float scale)
	{
		// TODO Auto-generated method stub
	}
	
}
