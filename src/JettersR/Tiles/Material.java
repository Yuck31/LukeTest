package JettersR.Tiles;
/**
 * 
 */
import JettersR.Level;

public class Material
{
    public static final Material NULL_MATERIAL = null;
    //
    private final byte id;
    //private Level level;
    //
    public Material(byte id, Level level, String soundPath, String[] particlePaths)
    {
        this.id = id;
        //this.level = level;
    }

    public byte getID(){return id;}

    public void step(int r)
    {

    }

    public void land()
    {

    }

    public void bump()
    {

    }

    public void slide()
    {

    }

    public void destroy()
    {

    }
}
