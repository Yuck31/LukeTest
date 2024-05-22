package JettersR.Util.Shapes.Shapes2D;

import org.joml.Vector4f;

import JettersR.Graphics.Screen;

/**
 * 
 */

public class AAB_RoundedBox2D extends Shape2D
{
    //Radius value used for the corners of this box.
    private float cornerRadius;

    //The base dimensions inside the box.
    private int width, height, baseWidth, baseHeight;

    /**Consstructor.*/
    public AAB_RoundedBox2D(int width, int height, int xOffset, int yOffset, float cornerRadius)
    {
        super(xOffset, yOffset);
        //
        this.width = width;
        this.height = height;
        this.setCornerRadius(cornerRadius);
    }

    /**Consstructor.*/
    public AAB_RoundedBox2D(int width, int height, float cornerRadius)
    {this(width, height,  0, 0, cornerRadius);}

    public int getWidth(){return width;}
    public int getHeight(){return height;}
    public int getBaseWidth(){return baseWidth;}
    public int getBaseHeight(){return baseHeight;}

    public int left(){return xOffset;}
    public int right(){return xOffset + width;}
    public int up(){return yOffset + height;}
    public int down(){return yOffset + height;}
    
    //Corner Radius Getter/Setter.
    public float getCornerRadius(){return cornerRadius;}
    public void setCornerRadius(float cornerRadius)
    {
        this.cornerRadius =
        (cornerRadius > width/2f) ? width/2f :
        (cornerRadius > height/2f) ? height/2f :
        cornerRadius;

        this.baseWidth = (int)(width - (cornerRadius * 2));
        this.baseHeight = (int)(height - (cornerRadius * 2));
    }

    @Override
    public boolean intersects(double x, double y, float thisX, float thisY)
    {
        float//Set Left, Back, and Bottom points.
        this_x = thisX + this.xOffset,
        this_y = thisY + this.yOffset;

        float//Dimensions.
        this_width = this_x + this.width,
        this_height = this_y + this.height;

        //Is it within this Box's dimensions?
        if(x < this_width  && y < this_height
        && x > this_x && y > this_y)
        {
            float//Base Dimensions.
            this_baseX = this_x + cornerRadius,
            this_baseY = this_y + cornerRadius,
            //
            this_baseWidth = this_width - cornerRadius,
            this_baseHeight = this_height - cornerRadius;

            //Guarantee case.
            if(x < this_baseWidth && y < this_baseHeight
            & x > this_baseX && x > this_baseY)
            {return true;}
            //
            //Corner Checks.
            else
            {
                float sideX = 0f, sideY = 0f;

                //Left, distance from right circle to center of left circle.
                if(x < this_baseX){sideX = (float)(this_baseX - x);}
                //Right, distance from left circle to center of right circle.
                else if(x > this_baseWidth){sideX = (float)(x - this_baseWidth);}

                //Back, distance from front circle to center of back circle.
                if(y < this_baseY){sideY = (float)(this_baseY - y);}
                //Front, distance from back circle to center of front circle.
                else if(y > this_baseHeight){sideY = (float)(y - this_baseHeight);}

                //Get the square distance of all the side values combined.
                float sqrLength = (sideX * sideX) + (sideY * sideY);

                //Return true if length is within the span of the radius.
                return sqrLength < (cornerRadius * cornerRadius);
            }
        }

        //No collision is going to be made.
        return false;
    }

    Vector4f g = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
    @Override
    public void render(Screen screen, float scale, float x, float y, boolean fixed)
    {
        int xPos = (int)((x + xOffset + cornerRadius) * scale),
        w = (int)((x + xOffset + cornerRadius + baseWidth) * scale);
        //
        int yPos = (int)((y + yOffset + cornerRadius) * scale),
        h = (int)((y + yOffset + cornerRadius + baseHeight) * scale);

        //Circles.
        screen.drawCircle(xPos, yPos, cornerRadius, 1, g, fixed);
        screen.drawCircle(w, yPos, cornerRadius, 1, g, fixed);
        screen.drawCircle(w, h, cornerRadius, 1, g, fixed);
        screen.drawCircle(xPos, h, cornerRadius, 1, g, fixed);

        //Base Rect.
        screen.drawRect(xPos, yPos, (int)(baseWidth * scale), (int)(baseHeight * scale), g, fixed);
    }
}
