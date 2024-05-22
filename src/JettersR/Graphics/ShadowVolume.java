package JettersR.Graphics;
/**
 * Shadow Volume Creation:
 * -Go through each face of the collision model.
 * -If a face is not facing the light, extrude each edge of it that neighbors a face facing the light.
 * 
 * 
 * What needs to happen:
 * -Light and Shadow Pass for JUST opaque things.
 * -Light and Shadow Pass for JUST transperant things.
 * -...das about it.
 * 
 * Opaque depth and ambient draw
 * Opaque Global Shadow Volume stencil
 * TODO Opaque Global Shadow Silhouette stencil. (Meant to reduce tile shadow blockiness).
 * Opqaue Global diffuse apply
 * if there are any non-global shadows, clear stencil value and start loop:
 * -for each:
 * --Opaque Non-Global Shadow Volume stencil
 * --Opaque Non-Global Shadow Silhouette stencil
 * --Opaque Non-Global diffuse apply
 * --Clear stencil buffer.
 * Set stencil test to normal.
 * 
 * //Transperant sprites will not have non-global shadows casted on them.
 * Copy output buffer to color buffer. Transperant colors will be put on top.
 * Transperant depth and draw. Put ambient * color into output buffer and (ambient + diffuse) * color into color buffer. Normal diffuse calc is already done.
 * //NOTE: This means non-global shadows will not be cast on transperant objects. ANOTHER framebuffer would be needed to make that possible.
 * If there are any reflection regions in view.
 * -Copy output buffer to normal buffer.
 * -for each:
 * --Where a pixel is placed, increment the stencil value.
 * --TODO Reflection sprites combined with ambient refracation color, (ambient + diffuse) * color into normal buffer, increment stencil value where a pixel is placed (don't draw above region).
 * --TODO Refraction distortion from normal buffer to color and output buffers.
 * //NOTE: This means transperant sprites under refractions will not get diffuse lighting and refraction pixels where reflection pixels are will not recieve diffuse lighting.
 * --Set stencil test to fail if the stencil value dosn't equal zero.
 * --TODO Refraction Sprites draw on top. Put ambient * color into output buffer and (ambient + diffuse) * color into color buffer.
 * -Clear stencil buffer.
 * Transperant Global Shadow Volume stencil (we have to redo this because the depth buffer has been changed).
 * TODO Transperant Global Shadow Silhouette stencil.
 * Transperant Global diffuse apply, copy color from color buffer where stencil value is 0.
 * (alternativly, ambient diffuse in output, ambient in color, put from color to output where stencil value is not 0)
 * //
 * //for each:
 * //-Transperant Non-Global Shadow Volume stencil
 * //-Transperant Non-Global diffuse apply
 * 
 * Basic shader.
 * Line shader.
 * Circle shader.
 * Basic shader 2D.
 * Line shader 2D.
 * Circle shader 2D.
 */
import java.util.List;

import org.joml.Vector3f;

import JettersR.Util.Shapes.Shapes3D.AAB_Box;

public class ShadowVolume
{
    //Faces.
    private ShadowFace[] faces;

    //Camera Collision.
    //private AAB_Box box = null;

    /**Constructor.*/
    public ShadowVolume(List<ShadowFace> faceList)
    {
        //Set faces.
        this.faces = new ShadowFace[faceList.size()];
        faceList.toArray(this.faces);
    }

    /**Constructor.*/
    public ShadowVolume(ShadowFace... faces)
    {
        this.faces = faces;
    }

    //Face Getter.
    public ShadowFace[] getShadowFaces(){return this.faces;}
}
