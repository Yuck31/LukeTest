package JettersR.Util.Shapes.Shapes3D.Misc;
/**
 * 
 */
//import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Entities.Entity;
import JettersR.Graphics.Screen;
import JettersR.Util.Shapes.Shapes3D.*;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class AAB_RoundedBox extends Shape_Box
{
    //Radius value used for the corners of this box.
    private @fixed int f_cornerRadius;

    //The base dimensions inside the box.
    private @fixed int f_baseWidth;
    private @fixed int f_baseHeight;
    private @fixed int f_baseDepth;

    /**Consstructor.*/
    public AAB_RoundedBox(int width, int height, int depth, @fixed int f_xOffset, @fixed int f_yOffset, @fixed int f_zOffset, @fixed int f_cornerRadius)
    {
        super(width, height, depth, f_xOffset, f_yOffset, f_zOffset);
        this.f_setCornerRadius(f_cornerRadius);
    }

    /**Consstructor.*/
    public AAB_RoundedBox(int width, int height, int depth, @fixed int f_cornerRadius)
    {this(width, height, depth, 0, 0, 0, f_cornerRadius);}

    //Corner Radius Getter/Setter.
    public @fixed int f_getCornerRadius(){return f_cornerRadius;}
    public float getCornerRadiusFloat(){return f_toFloat(f_cornerRadius);}
    public void f_setCornerRadius(@fixed int f_cornerRadius)
    {
        @fixed int f_width = fixed(width);
        @fixed int f_height = fixed(height);
        @fixed int f_depth = fixed(depth);

        this.f_cornerRadius =
        (f_cornerRadius > f_width / 2) ? f_width / 2 :
        (f_cornerRadius > f_height / 2) ? f_height / 2 :
        (f_cornerRadius > f_depth / 2) ? f_depth / 2 :
        f_cornerRadius;

        this.f_baseWidth = f_width - (f_cornerRadius * 2);
        this.f_baseHeight = f_height - (f_cornerRadius * 2);
        this.f_baseDepth = f_depth - (f_cornerRadius * 2);
    }

    //Mostly used with lights.
    public void setCornerRadius_Light(@fixed int f_cornerRadius)
    {
        this.f_cornerRadius = (f_cornerRadius < 0) ? 0 : f_cornerRadius;

        this.width = f_toInt(f_baseWidth + (this.f_cornerRadius * 2));
        this.height = f_toInt(f_baseHeight + (this.f_cornerRadius * 2));
        this.depth = f_toInt(f_baseDepth + (this.f_cornerRadius * 2));

        @fixed int f_r = -(this.f_cornerRadius);
        this.f_xOffset = f_r;
        this.f_yOffset = f_r;
        this.f_zOffset = f_r;
    }

    //Base Dimension getters.
    public @fixed int f_getBaseWidth(){return f_baseWidth;}
    public @fixed int f_getBaseHeight(){return f_baseHeight;}
    public @fixed int f_getBaseDepth(){return f_baseDepth;}
    //
    public float getBaseWidthFloat(){return f_toFloat(f_baseWidth);}
    public float getBaseHeightFloat(){return f_toFloat(f_baseHeight);}
    public float getBaseDepthFloat(){return f_toFloat(f_baseDepth);}

    @Override
    public @fixed int f_leftContact(){return 0;}
    public @fixed int f_rightContact(){return 0;}
    //
    public @fixed int f_backContact(){return 0;}
    public @fixed int f_frontContact(){return 0;}
    //
    public @fixed int f_bottomContact(){return 0;}
    public @fixed int f_topContact(){return 0;}

    @Override
    public Shape_Face[] getFaces()
    {
        //TODO
        return null;
    }

    public @fixed int f_baseLeft(){return f_xOffset + f_cornerRadius;}
    public @fixed int f_baseBack(){return f_yOffset + f_cornerRadius;}
    public @fixed int f_baseBottom(){return f_zOffset + f_cornerRadius;}
    //
    public float baseLeftFloat(){return f_toFloat(f_xOffset + f_cornerRadius);}
    public float baseBackFloat(){return f_toFloat(f_yOffset + f_cornerRadius);}
    public float baseBottomFloat(){return f_toFloat(f_zOffset + f_cornerRadius);}


    /**Rounded Box Intersection.*/
    public boolean intersects(float x, float y, float z, AAB_RoundedBox R, float rX, float rY, float rZ)
    {
        float//Set Left, Back, and Bottom points.
        this_x = x + this.f_xOffset,
        this_y = y + this.f_yOffset,
        this_z = z + this.f_zOffset,
        R_x = rX + R.f_getXOffset(),
        R_y = rY + R.f_getYOffset(),
        R_z = rZ + R.f_getZOffset();

        float//Dimensions.
        this_width = this_x + this.width,
        this_height = this_y + this.height,
        this_depth = this_z + this.depth,
        R_width = R_x + R.getWidth(),
        R_height = R_y + R.getHeight(),
        R_depth = R_z + R.getDepth();

        //Is it within this Box's dimensions?
        if(this_x < R_width  && this_y < R_height && this_z < R_depth
        && this_width > R_x && this_height > R_y && this_depth > R_z)
        {
            float//Base Dimensions.
            this_baseX = this_x + f_cornerRadius,
            this_baseY = this_y + f_cornerRadius,
            this_baseZ = this_z + f_cornerRadius,
            R_baseX = R_x + R.f_getCornerRadius(),
            R_baseY = R_y + R.f_getCornerRadius(),
            R_baseZ = R_z  + R.f_getCornerRadius(),
            //
            this_baseWidth = this_width - f_cornerRadius,
            this_baseHeight = this_height - f_cornerRadius,
            this_baseDepth = this_depth - f_cornerRadius,
            R_baseWidth = R_width - R.f_getCornerRadius(),
            R_baseHeight = R_height - R.f_getCornerRadius(),
            R_baseDepth = R_depth - R.f_getCornerRadius();

            //Guarantee case.
            if(this_baseX < R_baseWidth && this_baseY < R_baseHeight && this_baseZ < R_baseDepth
            & this_baseWidth > R_baseX && this_baseHeight > R_baseY && this_baseDepth > R_baseZ)
            {return true;}
            //
            //Corner Checks.
            else
            {
                float sideX = 0f, sideY = 0f, sideZ = 0f;

                //Left, distance from right circle to center of left circle.
                if(R_baseWidth < this_baseX){sideX = this_baseX - R_baseWidth;}
                //Right, distance from left circle to center of right circle.
                else if(R_baseX > this_baseWidth){sideX = R_baseX - this_baseWidth;}

                //Back, distance from front circle to center of back circle.
                if(R_baseHeight < this_baseY){sideY = this_baseY - R_baseHeight;}
                //Front, distance from back circle to center of front circle.
                else if(R_baseY > this_baseHeight){sideY = R_baseY - this_baseHeight;}

                //Bottom, distance from top circle to center of bottom circle.
                if(R_baseDepth < this_baseZ){sideZ = this_baseZ - R_baseDepth;}
                //Top, distance from bottom circle to center of top circle.
                else if(R_baseZ > this_baseDepth){sideZ = R_baseZ - this_baseDepth;}

                //Get the square distance of all the side values combined.
                float radiiSum = f_cornerRadius + R.f_getCornerRadius(),
                sqrLength = (sideX * sideX) + (sideY * sideY) + (sideZ * sideZ);

                //Return true if length is within the span of the two radii.
                return sqrLength < (radiiSum * radiiSum);
            }
        }

        //No collision is going to be made.
        return false;
    }

    /**Box Intersection.*/
    public boolean intersects(float x, float y, float z, AAB_Box B, float bX, float bY, float bZ)
    {
        float//Set Left, Back, and Bottom points
        this_x = x + this.f_xOffset,
        this_y = y + this.f_yOffset,
        this_z = z + this.f_zOffset,
        B_x = bX + B.f_getXOffset(),
        B_y = bY + B.f_getYOffset(),
        B_z = bZ + B.f_getZOffset();

        float//Dimensions
        this_width = this_x + this.width,
        this_height = this_y + this.height,
        this_depth = this_z + this.depth,
        B_width = B_x + B.getWidth(),
        B_height = B_y + B.getHeight(),
        B_depth = B_z + B.getDepth();

        //Is it within this Box's dimensions.
        if(this_x < B_width  && this_y < B_height && this_z < B_depth
        && this_width > B_x && this_height > B_y && this_depth > B_z)
        {
            float//Base Dimensions.
            this_baseX = this_x + f_cornerRadius,
            this_baseY = this_y + f_cornerRadius,
            this_baseZ = this_z + f_cornerRadius,
            this_baseWidth = this_width - f_cornerRadius,
            this_baseHeight = this_height - f_cornerRadius,
            this_baseDepth = this_depth - f_cornerRadius;

            //Guarantee case.
            if(this_baseX < B_width && this_baseY < B_height && this_baseZ < B_depth
            & this_baseWidth > B_x && this_baseHeight > B_y && this_baseDepth > B_z)
            {return true;}
            //
            //Corner Checks.
            else
            {
                float sideX = 0f, sideY = 0f, sideZ = 0f;

                //Left, distance from right side to center of left circle.
                if(B_width < this_baseX){sideX = this_baseX - B_width;}
                //Right, distance from left side to center of right circle.
                else if(B_x > this_baseWidth){sideX = B_x - this_baseWidth;}

                //Back, distance from front side to center of back circle.
                if(B_height < this_baseY){sideY = this_baseY - B_height;}
                //Front, distance from back side to center of front circle.
                else if(B_y > this_baseHeight){sideY = B_y - this_baseHeight;}

                //Bottom, distance from top side to center of bottom circle.
                if(B_depth < this_baseZ){sideZ = this_baseZ - B_depth;}
                //Top, distance from bottom side to center of top circle.
                else if(B_z > this_baseDepth){sideZ = B_z - this_baseDepth;}

                //Get the square distance of all the side values combined.
                float sqrLength = (sideX * sideX) + (sideY * sideY) + (sideZ * sideZ);
                return sqrLength < (f_cornerRadius * f_cornerRadius);
            }
        }

        //No collision is going to be made.
        return false;
    }

    @Override
    public boolean performCollision(fixedVector3 f_thisPosition, fixedVector3 f_thisVelocity, Shape3D shape, fixedVector3 f_shapePosition)
    {
        //AAB_RoundedBox
        if(shape instanceof AAB_RoundedBox)
        {
            AAB_RoundedBox rBox = (AAB_RoundedBox)shape;

            if
            (
                intersects(f_thisPosition.x + f_thisVelocity.x, f_thisPosition.y + f_thisVelocity.y, f_thisPosition.z + f_thisVelocity.z,
                rBox, f_shapePosition.x, f_shapePosition.y, f_shapePosition.z)
            )
            {
                //rBox.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }
        //AAB_Box
        else if(shape instanceof AAB_Box)
        {
            AAB_Box box = (AAB_Box)shape;

            if
            (
                intersects(f_thisPosition.x + f_thisVelocity.x, f_thisPosition.y + f_thisVelocity.y, f_thisPosition.z + f_thisVelocity.z,
                box, f_shapePosition.x, f_shapePosition.y, f_shapePosition.z)
            )
            {
                //box.collisionResponse.execute(this, thisEntity, thisPosition, thisVelocity, shapePosition);
                return true;
            }
        }

        //No collision was made.
        return false;
    }

    public final boolean tileCollidedBy(Shape3D itsShape, fixedVector3 f_itsPosition, fixedVector3 f_itsVelocity, fixedVector3 f_thisPosition, byte thisTileForces)
    {return false;}
    

    @Override
    public void putBox_OutComposite(Shape3D shape, Entity entity, fixedVector3 position, fixedVector3 velocity, fixedVector3 thisPosition)
    {
        
    }

    @Override
    public void putCylinder_OutComposite(Cylinder cylinder, Entity entity, fixedVector3 position, fixedVector3 velocity, fixedVector3 f_oldPosition, fixedVector3 thisPosition)
    {
        
    }

    private Vector4f lightColor = new Vector4f(0.0f, 0.8f, 0.0f, 1.0f),
    //outlineColor = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f),
    darkColor = new Vector4f(0.0f, 0.5f, 0.0f, 1.0f);

    @Override
    public void render(Screen screen, float scale, fixedVector3 f_position)
    {
        /*
        int
        xa = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        xar = (int)(f_toFloat(f_position.x + f_xOffset + f_cornerRadius) * scale),
        //
        ya = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        yar = (int)(f_toFloat(f_position.y + f_yOffset + f_cornerRadius) * scale),
        //
        //za = (int)((z + zOffset) * scale),
        zar = (int)(f_toFloat(f_position.z + f_zOffset + f_cornerRadius) * scale),
        //
        w = (int)(f_toFloat(f_position.x + fixed(width) + f_xOffset) * scale),
        wr = (int)(f_toFloat(f_position.x + fixed(width) + f_xOffset - f_cornerRadius) * scale),
        //
        h = (int)(f_toFloat(f_position.y + fixed(height) + f_yOffset) * scale),
        hr = (int)(f_toFloat(f_position.y + fixed(height) + f_yOffset - f_cornerRadius) * scale),
        //
        //d = (int)((z + depth + zOffset) * scale),
        dr = (int)(f_toFloat(f_position.z + fixed(depth) + f_zOffset - f_cornerRadius) * scale);

        float r = f_toFloat(f_cornerRadius) * scale;

        //Bottom Rect
        screen.drawLine(xar, ya, zar, wr, ya, zar, darkColor, true);
        screen.drawLine(xar, h, zar, wr, h, zar, darkColor, true);

        //Front and Back
        screen.drawLine(xa, hr, zar, xa, yar, dr, lightColor, true);
        screen.drawLine(w, hr, zar, w, yar, dr, lightColor, true);

        //Top Rect
        screen.drawLine(xar, ya, dr, wr, ya, dr, lightColor, true);
        screen.drawLine(xar, h, dr, wr, h, dr, lightColor, true);

        //
        screen.drawCircle(xar, yar, zar, r, 1, darkColor, true);
        screen.drawCircle(wr, yar, zar, r, 1, darkColor, true);
        screen.drawCircle(xar, hr, zar, r, 1, darkColor, true);
        screen.drawCircle(wr, hr, zar, r, 1, darkColor, true);

        //
        screen.drawCircle(xar, yar, dr, r, 1, lightColor, true);
        screen.drawCircle(wr, yar, dr, r, 1, lightColor, true);
        screen.drawCircle(xar, hr, dr, r, 1, lightColor, true);
        screen.drawCircle(wr, hr, dr, r, 1, lightColor, true);
        */


        int
        x = (int)(f_toFloat(f_position.x + f_xOffset) * scale),
        y = (int)(f_toFloat(f_position.y + f_yOffset) * scale),
        z = (int)(f_toFloat(f_position.z + f_zOffset) * scale),
        w = (int)(f_toFloat(f_position.x + fixed(width) + f_xOffset) * scale),
        h = (int)(f_toFloat(f_position.y + fixed(height) + f_yOffset) * scale),
        d = (int)(f_toFloat(f_position.z + fixed(depth) + f_zOffset) * scale);

        //Bottom Rect
        screen.drawLine(x, y, z, w, y, z, darkColor, true);
        screen.drawLine(x, h, z, w, h, z, darkColor, true);

        //Front and Back
        screen.drawLine(x, h, z, x, y, d, lightColor, true);
        screen.drawLine(w, h, z, w, y, d, lightColor, true);

        //Top Rect
        screen.drawLine(x, y, d, w, y, d, lightColor, true);
        screen.drawLine(x, h, d, w, h, d, lightColor, true);
    }

    @Override
    public void tileRender(Screen screen, float scale, @fixed int f_x, @fixed int f_y, @fixed int f_z, boolean fixed)
    {
        
    }
}
