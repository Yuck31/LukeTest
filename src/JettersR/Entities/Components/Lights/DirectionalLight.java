package JettersR.Entities.Components.Lights;
/**
 * 
 */
import org.joml.Vector3f;

import JettersR.Util.Shapes.Shapes3D.Misc.SphericalSector;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class DirectionalLight extends Light
{
    //Collision Shape for Camera.
    private SphericalSector sphericalSector;

    //Length and outer length.
    private @fixed int f_length, f_outerLength;

    //Inner cutoff angle.
    private float //innerAngle,
    cosInnerAngle;

    /**Constructor.*/
    public DirectionalLight(@fixed int f_x, @fixed int f_y, @fixed int f_z, Vector3f diffuseColor, Vector3f ambientColor,
    @fixed int f_length, @fixed int f_outerLength, fixedVector3 f_direction, @fixed int f_outerAngleDegrees, float innerAngleDegrees)
    {
        //Do light stuff.
        super(f_x, f_y, f_z, diffuseColor, ambientColor);

        //Create shape.
        this.sphericalSector = new SphericalSector(f_length + f_outerLength, f_direction, f_outerAngleDegrees, 0, 0, 0);

        //Lengths.
        this.f_length = f_length;
        this.f_outerLength = f_outerLength;

        //Inner angle.
        //this.innerAngle = (float)(innerAngleDegrees * (StrictMath.PI / 180));
        this.cosInnerAngle = (float)StrictMath.cos( (innerAngleDegrees * (StrictMath.PI / 180))  );
    }

    //Length Getters.
    public @fixed int f_getLength(){return this.f_length;}
    public float getLengthFloat(){return f_toFloat(this.f_length);}
    //
    public @fixed int f_getOuterLength(){return this.f_outerLength;}
    public float getOuterLengthFloat(){return f_toFloat(this.f_outerLength);}

    //Angle Getters.
    public float getCosInnerAngle(){return cosInnerAngle;}
    public float getCosOuterAngle(){return sphericalSector.f_getCosAngle();}

    //Shape Getter.
    public Shape3D getShape(){return this.sphericalSector;}
    public SphericalSector getSphericalSector(){return this.sphericalSector;}

    //Radius Getter/Setter.
    public float getRadius(){return sphericalSector.f_getLength();}
    public void f_setRadius(@fixed int f_r){sphericalSector.f_setLength(f_r);}
    
    /**Render function. */
    public final void render(Screen screen, float scale)
    {
        sphericalSector.render(screen, scale, this.f_position);
    }
}
