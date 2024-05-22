package JettersR.Graphics;
/**
 * A class representing an image that can be cut into multiple Sprites
 * 
 * Author: Luke Sullivan
 * Last Edit: 7/5/2023
 */
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL33;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import JettersR.Game;
import JettersR.Main;

public final class SpriteSheet
{
    //Sheet Layouts Path (so that way if I have to move the Sheet Layouts folder, I just need to change this).
    public static final String sheetLayoutsPath = "assets/SheetLayouts/";

    //SheetLoadFunction, specific for each Graphics API.
    private static SheetLoadFunction sheetLoadFunction = SpriteSheet::doNothing;
    private static SheetDeleteFunction sheetDeleteFunction = null;

    //Identification for OpenGL/Vulkan.
    private int ID = 0;

    //Dimemsions and pixels.
    private int width = 0, height = 0;
    protected int[] pixels;

    /**Null SpriteSheet Constructor.*/
    public SpriteSheet()
    {
        //Zero Pixels...
        pixels = new int[0];

        //Invoke the related SheetLoadFunction.
        ID = sheetLoadFunction.invoke(this);
    }

    /**Meant for initial HashMap filling.*/
    public SpriteSheet(File file)//, String name)
    {
        try
        {
            //Interpret the File as an Image.
            BufferedImage image = ImageIO.read(file);

            //Set Dimensions.
            this.width = image.getWidth();
            this.height = image.getHeight();

            //Make the array of pixels.
            pixels = new int[width * height];

            //Shove the pixels in the image into the pixels array.
            image.getRGB(0, 0, width, height, pixels, 0, width);
        }
        //In case the file wasn't found.
        catch(IOException e){e.printStackTrace();}

        //Invoke the related SheetLoadFunction.
        ID = sheetLoadFunction.invoke(this);
    }

    /**A single color rectangle, made from .rsf files*/
    public SpriteSheet(int width, int height, int color)
    {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];

        for(int i = 0; i < pixels.length; i++)
        {pixels[i] = color;}

        //Invoke the related SheetLoadFunction
        ID = sheetLoadFunction.invoke(this);
    }

    /**Creates a SpriteSheet from an int array.*/
    public SpriteSheet(int width, int height, int[] pixels)
    {
        this.width = width;
        this.height = height;
        this.pixels = pixels;

        //Invoke the related SheetLoadFunction
        ID = sheetLoadFunction.invoke(this);
    }

    /**To be used if this sheet was loaded when a Graphics API wasn't initiallized.*/
    public void load(){ID = sheetLoadFunction.invoke(this);}

    /**Unloads this SpriteSheet, freeing up any resources that it occupied.*/
    public void delete(){sheetDeleteFunction.invoke(this);}

    /**Recolors a SpriteSheet.*/
    public static SpriteSheet recolor(SpriteSheet sheet, int[] oldColors, int[] newColors)
    {
        int[] result = new int[sheet.getWidth() * sheet.getHeight()];

        for(int i = 0; i < result.length; i++)
        {
            boolean found = false;
            for(int j = 0; j < oldColors.length; j++)
            {
                if(sheet.getPixel(i) == oldColors[j])
                {
                    result[i] = newColors[j];
                    found = true;
                    break;
                }
            }
            if(!found){result[i] = sheet.getPixel(i);}
        }
        
        return new SpriteSheet(sheet.getWidth(), sheet.getHeight(), result);
    }

    /**
     * Retrieves an array of Sprites based off of parameters from a .dat file.
     * 
     * @param file The .dat file being input.
     * @return An array of Sprites based off of the parameters from the .dat file.
    */
    public Sprite[] loadLayout(File file)
    {
        //Resulting Sprite Array, file.length / (4 bytes per int * 4 ints).
        Sprite[] sprites = new Sprite[(int)file.length() >> 4];
        //Sprite[] sprites = new Sprite[(int)file.length() / (Integer.BYTES << 2)];
        //System.out.println("layout: " + sprites.length);
        //System.out.println("layout: " + file.length());

        try
        {
            //Set up the input stream.
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            
            byte[] bytes = new byte[16];
            for(int i = 0; i < sprites.length; i++)
            {
                //Put the next 16 bytes into the bytes array.
                bis.read(bytes);

                //Create a new Sprite out of the bytes and sheet.
                sprites[i] = new Sprite
                (
                    this,//We have to "and" the last 3 bytes of each int by 255 since these bytes are signed (range from -128 to +127, nowhere near 255).
                    Game.bytesToInt(bytes[0], bytes[1], bytes[2], bytes[3]),//x
                    Game.bytesToInt(bytes[4], bytes[5], bytes[6], bytes[7]),//y
                    Game.bytesToInt(bytes[8], bytes[9], bytes[10], bytes[11]),//width
                    Game.bytesToInt(bytes[12], bytes[13], bytes[14], bytes[15])//height
                );
            }

            //Close the input streams.
            bis.close();
            fis.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}

        //...And we're all set.
        return sprites;
    }

    /**
     * Retrieves an array of Sprites based off of parameters from a .dat file.
     * 
     * @param name The name of the .dat file being input, relative to SpriteSheet.sheetLayoutsPath.
     * @return An array of Sprites based off of the parameters from the .dat file.
    */
    public Sprite[] loadLayout(String name)
    {
        File file = new File(SpriteSheet.sheetLayoutsPath + name + "_LYT.dat");
        return loadLayout(file);
    }

    /**Retrives a Horizontal Array of Sprites from this SpriteSheet.*/
    public Sprite[] getSprites_Hori(final int x, final int y, final int width, final int height, final int numSprites)
    {
        Sprite[] result = new Sprite[numSprites];

        for(int i = 0; i < numSprites; i++)
        {result[i] = new Sprite(this, x + (width * i), y, width, height);}

        return result;
    }

    /**Retrives a Vertical Array of Sprites from this SpriteSheet.*/
    public Sprite[] getSprites_Vert(final int x, final int y, final int width, final int height, final int numSprites)
    {
        Sprite[] result = new Sprite[numSprites];

        for(int i = 0; i < numSprites; i++)
        {result[i] = new Sprite(this, x, y + (height * i), width, height);}

        return result;
    }



    /**Pass in a Sprite Array with an offset to get the normalMap versions of those Sprites.*/
    public Sprite getNormalMap_Sprite(Sprite s, final int xOffset, final int yOffset)
    {
        //Make new Sprite out of Sprite coordinates and offsets.
        return new Sprite(this, s.getX() + xOffset, s.getY() + yOffset, s.getWidth(), s.getHeight());
    }


    /**Pass in a Sprite Array with an offset to get the normalMap versions of those Sprites.*/
    public Sprite[] getNormalMap_Sprites(Sprite[] spriteArray, final int xOffset, final int yOffset)
    {
        //Create array.
        Sprite[] result = new Sprite[spriteArray.length];

        //Go through each sprite
        for(int i = 0; i < result.length; i++)
        {
            //Get sprite.
            Sprite s = spriteArray[i];

            //Make new Sprite out of Sprite coordinates and offsets.
            result[i] = new Sprite(this, s.getX() + xOffset, s.getY() + yOffset, s.getWidth(), s.getHeight());
        }

        //Return the end result.
        return result;
    }

    
    
    //Dimension Getters.
    public int getWidth(){return width;}
    public int getHeight(){return height;}
    
    //Pixel Getters.
    public int getPixel(int x, int y){return pixels[(x % width) + (y % height) * width];}
    public int getPixel(int i){return pixels[i];}
    public int[] getPixels()
    {
        //If the pixels were sent to the GPU.
        if(pixels == null)
        {
            //Bind texture.
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, ID);

            //Retrieve the pixels.
            int[] result = new int[width * height];
            GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_BGRA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, result);
            return result;
        }
        return pixels;
    }
    /*
    public int[] getPixels_0()
    {
        //If the pixels were sent to the GPU.
        if(pixels == null)
        {
            //System.out.println(ID + " " + pixels);

            //int[] ib = new int[1];
            //GL33.glGetIntegerv(GL33.GL_TEXTURE_BINDING_2D, ib);
            //System.out.println(ib[0]);

            //Bind texture.
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, ID);

            //Retrieve the pixels.
            int[] result = new int[width * height];
            //int[] result = new int[1];
            GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_BGRA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, result);
            GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_BGRA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, result);
            return result;
        }
        return pixels;
    }
    */

    public int getID(){return ID;}
    //public String getName(){return name;}

    
    /*
     * Graphics API specific stuff
     */

    /**Sets the function to use when loading SpriteSheets*/
    public static boolean setSheetFunctions(byte graphicsAPI)
    {
        //This function will only work ONCE.
        if(sheetDeleteFunction != null)
        {
            //System.out.println("It works.");
            return false;
        }

        switch(graphicsAPI)
        {
            case Main.OPENGL:
            sheetLoadFunction = SpriteSheet::openGLLoad;
            sheetDeleteFunction = SpriteSheet::openGLDelete;
            break;

            case Main.VULKAN:
            sheetLoadFunction = SpriteSheet::vulkanLoad;
            sheetDeleteFunction = SpriteSheet::vulkanDelete;
            break;

            default:
            sheetLoadFunction = SpriteSheet::softwareLoad;
            sheetDeleteFunction = SpriteSheet::softwareDelete;
            break;
        }
        return true;
    }

    /*
     * Functional Interfaces are effectivly a way to pass a function as a parameter to anything.
     * Functions can be passed as lambda expressions or as actual functions this way.
     */
    @FunctionalInterface
    public interface SheetLoadFunction
    {
        //This is what the passed function will be interpretted as.
        public abstract int invoke(SpriteSheet sheet);
    }

    /**Used before any API is initialized.*/
    private static int doNothing(SpriteSheet sheet){return 0;}

    /**For the Software Renderer.*/
    private static int softwareLoad(SpriteSheet sheet)
    {
        //return glGenTextures();
        return 0;
    }

    /**For the OpenGL Renderer.*/
    private static int openGLLoad(SpriteSheet sheet)
    {
        int result = GL33.glGenTextures();

        //System.out.println(result);
        //if(result == 0){System.out.println(GL33.glGetError());}
        //System.out.println(result + " " + GL33.glGetError());

        //Bind the newly created texture object.
        //GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, result);

        //Set parmeters.
        {
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        }

        //Send pixels.
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, sheet.getWidth(), sheet.getHeight(), 0,
        GL33.GL_BGRA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, sheet.getPixels());  
        
        System.out.println("Sheet " + result + " " + GL33.glGetError());

        //GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, sheet.getWidth(), sheet.getHeight(),
        //0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, sheet.getPixels());

        //Aaaand get rid of 'em. The GPU already has them.
        sheet.pixels = null;

        //Return the newly created ID.
        return result;
    }

    /**For the Vulkan Renderer.*/
    private static int vulkanLoad(SpriteSheet sheet)
    {
        return 0;
    }

    /*
     * Functional Interface for deleting textures.
     */
    @FunctionalInterface
    public interface SheetDeleteFunction
    {
        //This is what the passed function will be interpretted as
        public abstract void invoke(SpriteSheet sheet);
    }

    private static void softwareDelete(SpriteSheet sheet)
    {sheet.pixels = null;}

    private static void openGLDelete(SpriteSheet sheet)
    {GL33.glDeleteTextures(sheet.getID());}

    private static void vulkanDelete(SpriteSheet sheet)
    {}
}
