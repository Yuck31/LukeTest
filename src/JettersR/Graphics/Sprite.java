package JettersR.Graphics;
/**
 * A class representing an image cut from a SpriteSheet.
 * 
 * Author: Luke Sullivan
 * Last Edit: 6/26/2023
 */

public class Sprite 
{
    //What SpriteSheet to get pixels from.
    private transient SpriteSheet sheet;

    //TexCoords, effectivly (not final so effects like scrolling lava can happen).
    public int x, y;
    private int width, height;

    /**If a Sprite is just the sheet or somethin'*/
    public Sprite(SpriteSheet sheet)
    {
        this.sheet = sheet;
        this.x = 0;
        this.y = 0;
        this.width = sheet.getWidth();
        this.height = sheet.getHeight();
    }

    /**Effectivly, TexCoords and a texture...*/
    public Sprite(SpriteSheet sheet, int x, int y, int width, int height)
    {
        this.sheet = sheet;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Sprite[] spriteArray(SpriteSheet sheet, int w, int h)
    {
        int numSprites = (int)((sheet.getWidth()/w) * (sheet.getHeight()/h));
        Sprite[] result = new Sprite[numSprites];

        for(int i = 0; i < numSprites; i++)
        {result[i] = new Sprite(sheet, i*w, i*h, w, h);}

        return result;
    }

    //GL_REPEAT
    //GL_MIRRORED_REPEAT
    //GL_CLAMP_TO_EDGE
    //GL_CLAMP_TO_BORDER

    @FunctionalInterface
    public interface PixelGetter{public abstract int invoke(int x, int y);}

    /**Returns a pixels from the region of the SpriteSheet this Sprite take up.*/
    public int getPixel(int x, int y)
    {
        //return (x < 0 || x >= width ||  y < 0 || y >= height) ? 0x00000000
        //: sheet.getPixel(this.x + x, this.y + y);

        //x = (x < 0) ? 0 : (x >= width) ? width-1 : x;
        //y = (y < 0) ? 0 : (y >= height) ? height-1 : y;
        //return sheet.getPixel(this.x + x, this.y + y);

        return sheet.getPixel(this.x + (x % width), this.y + (y % height));

        //if (x < 0 || x >= width ||  y < 0 || y >= height){return 0xFFFF0000;}
        //int p = sheet.getPixel(this.x + (x % width), this.y + (y % height));
        //return (p == 0x00000000) ? 0xFFFFFFFF : p;
    }

    public int getPixel_FlipX(int x, int y)
    {
        //return (x < 0 || x >= width ||  y < 0 || y >= height) ? 0x00000000 :
        //sheet.getPixel(this.x + (width-x)-1, this.y + y);

        return sheet.getPixel(this.x + ((width-x-1) % width), this.y + ((y % height) % height));
    }

    public int getPixel_FlipY(int x, int y)
    {
        //return (x < 0 || x >= width ||  y < 0 || y >= height) ? 0x00000000 :
        //sheet.getPixel(this.x + x, this.y + (height-y)-1);

        return sheet.getPixel(this.x + ((x % width) % width), this.y + ((height-y-1) % height));
    }

    public int getPixel_FlipXY(int x, int y)
    {
        //return (x < 0 || x >= width ||  y < 0 || y >= height) ? 0x00000000 :
        //sheet.getPixel(this.x + (width-x-1), this.y + (height-y-1));

        return sheet.getPixel(this.x + ((width-x-1) % width), this.y + ((height-y-1) % height));
    }

    //Flip Constants.
    public static final byte
    FLIP_NONE = 0b00,
    FLIP_X = 0b01,
    FLIP_Y = 0b10,
    FLIP_XY = 0b11;

    /**
     * Gets a function for use with getting pixels in whatever flipped way.
     * 
     * @param flip should be a "Sprite.Flip." value deterimining how a sprite's pixels should be flipped.
     * @return a pixel getter function based on flipAttribute.
     */
    public PixelGetter getPixelGetter(byte flip)
    {
        switch(flip)
        {
            case FLIP_X:
            return this::getPixel_FlipX;

            case FLIP_Y:
            return this::getPixel_FlipY;

            case FLIP_XY:
            return this::getPixel_FlipXY;

            default:
            return this::getPixel;
        }
    };

    /**Gets this Sprite's pixel array.*/
    public int[] getPixels()
    {
        int[] result = new int[width * height];

        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {result[x+y*width] = sheet.getPixel(this.x+x, this.y+y);}
        }

        return result;
    }

    //Dimension Getters/Setters.
    public int getX(){return x;}
    public void setX(int x){this.x = x;}
    public void setX(int inputX, int leftX, int totalWidth)
    {
        this.x = (inputX < 0) ? ((inputX - leftX) % totalWidth) + leftX
		: -leftX + ((inputX + leftX) % totalWidth);
    }
    //
    public int getY(){return y;}
    public void setY(int y){this.y = y;}
    public void setY(int inputY, int upY, int totalHeight)
    {
        this.y = (inputY < 0) ? ((inputY - upY) % totalHeight) + upY
		: -upY + ((inputY + upY) % totalHeight);
    }
    //
    public int getWidth(){return width;}
    public int getHeight(){return height;}

    //Sheet Getter
    public SpriteSheet getSheet(){return sheet;}
}
