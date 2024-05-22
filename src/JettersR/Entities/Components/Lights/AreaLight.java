package JettersR.Entities.Components.Lights;
/**
 * 
 */
import org.joml.Vector3f;

import JettersR.Util.Shapes.Shapes3D.Misc.AAB_RoundedBox;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class AreaLight extends Light
{
    private AAB_RoundedBox rBox;
    private byte useX = 0, useY = 0, useZ = 0;

    /**Constructor.*/
    public AreaLight(@fixed int f_x, @fixed int f_y, @fixed int f_z, Vector3f diffuseColor, Vector3f ambientColor,
    int baseWidth, int baseHeight, int baseDepth, @fixed int f_outerRadius, byte useX, byte useY, byte useZ)
    {
        super(f_x, f_y, f_z, diffuseColor, ambientColor);
        @fixed int f_r = -f_outerRadius;

        //Create Box.
        this.rBox = new AAB_RoundedBox((int)(baseWidth + f_toInt(f_outerRadius*2)),
        (int)(baseHeight + f_toInt(f_outerRadius*2)), (int)(baseDepth + f_toInt(f_outerRadius*2)),
        f_r, f_r, f_r, f_outerRadius);

        //Set uses.
        this.useX = useX;
        this.useY = useY;
        this.useZ = useZ;
    }
    
    //Shape Getter.
    public Shape3D getShape(){return this.rBox;}
    public AAB_RoundedBox getRBox(){return this.rBox;}

    //Dimension Getters.
    public float getWidth(){return rBox.getBaseWidthFloat();}
    public float getHeight(){return rBox.getBaseHeightFloat();}
    public float getDepth(){return rBox.getBaseDepthFloat();}

    //Radius Getter/Setter.
    public float getRadius(){return rBox.getCornerRadiusFloat();}
    public void f_setRadius(@fixed int f_r){rBox.setCornerRadius_Light(f_r);}

    //Used sides.
    public byte getUseX(){return useX;}
    public byte getUseY(){return useY;}
    public byte getUseZ(){return useZ;}

    public final void render(Screen screen, float scale)
    {
        rBox.render(screen, scale, f_position);
    }  
}
