package JettersR.Tiles;
/**
 * 
 */
public final class Materials
{
	private static byte num_materials = 0;
	private static final Material[] MATERIALS =
	{
		null
	};
	
	private Materials(){}

	public static Material get(int slot){return MATERIALS[slot];}
}
