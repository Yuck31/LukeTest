package JettersR.Tiles.Graphics;
/**
 * A Sprite-like object representing the visual part of a tile.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/8/2024
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.SpriteSheet;
import JettersR.Tiles.Material;
import JettersR.Util.Fixed;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class TileMesh
{
    //Used to differentiate Basic and Lighting sprites.
    @FunctionalInterface
    public interface RenderFunction{public abstract void invoke(Screen screen, int xPos, int yPos, int zPos, float scale);}

    //ShearType constants.
    public static final byte MAX_SHEARTYPES = 5,
    SHEARTYPE_FLOOR = 0,
    SHEARTYPE_WALL = 1,
    SHEARTYPE_Y = 2,
    SHEARTYPE_ZX = 3,
    SHEARTYPE_ZY = 4;


    //
    //TileMesh data.
    //

    //Sprites and normal maps. [Saved as 2-byte IDs]
    private Sprite[] sprites,//[Number of Sprites held * 2] bytes (& 0xFF)
    normalMaps;//[Number of Sprites held * 2] bytes (& 0xFF)

    //Sprite Indicies. Used for each of the sides of a TileMesh.
    private byte[] spriteIndecies;//-Number of Sprite Indecies used - 1 byte (& 0xFF)
    //-Sprite Indecies - [Number of Sprite Indecies used] bytes, signed (if first value is -1, skip)

    //X, Y, and Z offsets.
    private byte[] offsets;//[(# sprites * 3) bytes]

    //Sprite Shear data.
    private byte[] shearTypes;//[# sprites bytes]
    private float[] shears;//[# sprites fixed point bytes]

    //Blending Color.
    private Vector4f blendingColor;//[4 bytes]
    private transient boolean isTranslucent = false;

    //Emission, determines how much of the inherent sprite color lighting calculations start with. Useful for lava or other lights.
    private float emission;//[1 fixed point byte]

    //Material.
    protected Material material;//[1 byte]


    //
    //Runtime.
    //

    //Flip and Wrap.
    //private byte flip;
    //private @fixed short[] f_wrap;
    //Flip won't work because of normal maps.

    //Wrap.
    private transient @fixed short[] f_wrap = new short[2];

    

    /**Constructor.*/
    public TileMesh(Vector4f blendingColor, Sprite[] sprites, Sprite[] normalMaps, byte[] spriteIndecies,
    byte[] offsets, byte[] shearTypes, float[] shears, //byte flip,
    float emission, Material material)
    {
        //Set color.
        this.blendingColor = blendingColor;
        isTranslucent = (blendingColor == null || blendingColor.w < 1.0f) ? true : false;

        //Set sprites.
        this.sprites = sprites;
        this.normalMaps = normalMaps;
        this.spriteIndecies = spriteIndecies;

        //Set offsets.
        this.offsets = offsets;

        //Set shear data.
        this.shearTypes = shearTypes;
        this.shears = shears;

        //Set flip.
        //this.flip = flip;

        //Set emisssion.
        this.emission = emission;

        //Set material.
        this.material = material;
    }

    /**One sprite constructor.*/
    public TileMesh(Vector4f blendingColor, Sprite sprite, Sprite normalMap, byte[] offsets, byte shearType, float shear, float emission, Material material)
    {this(blendingColor, new Sprite[]{sprite}, new Sprite[]{normalMap}, new byte[]{0}, offsets, new byte[]{shearType}, new float[]{shear}, emission, material);}


    public Vector4f getBlendingColor(){return blendingColor;}
    public void setBlendingColor(float r, float g, float b, float a)
    {
        this.blendingColor.set(r, g, b, a);
        isTranslucent = (this.blendingColor.w < 1.0f) ? true : false;
    }
    public void setBlendingColor(Vector4f blendingColor)
    {
        this.blendingColor.set(blendingColor);
        isTranslucent = (this.blendingColor.w < 1.0f) ? true : false;
    }
    public void setBlendingColorPointer(Vector4f blendingColor)
    {
        //this.blendingColor = null;
        this.blendingColor = blendingColor;
        isTranslucent = (this.blendingColor.w < 1.0f) ? true : false;
    }

    //isTranslucent Setter/Setter.
    public boolean isTranslucent(){return isTranslucent;}
    protected void setIsTranslucent(boolean isTranslucent){this.isTranslucent = isTranslucent;}

    //Offset Getter/Setter.
    //public int getFloorXOffset(){return floorOffset.x;}
    //public int getFloorYOffset(){return floorOffset.y;}
    //public int getWallXOffset(){return wallOffset.x;}
    //public int getWallYOffset(){return wallOffset.y;}
    public byte[] getOffsets(){return this.offsets;}
    public void setOffsets(byte[] offsets){this.offsets = offsets;}

    //ShearType Getter/Setter.
    public byte[] getShearTypes(){return shearTypes;}
    public void setShearTypes(byte[] shearTypes){this.shearTypes = shearTypes;}

    //Shears Getter/Setter.
    public float[] getShears(){return shears;}
    public void setShearAmounts(float[] shears){this.shears = shears;}

    //Sprite Getters/Setters.
    public Sprite[] getSprites(){return this.sprites;}
    public Sprite[] getNormalMaps(){return this.normalMaps;}
    public void setSpritePointer(int slot, Sprite sprite){this.sprites[slot] = sprite;}

    //SpriteSheet getter
    public SpriteSheet getSheet(){return sprites[0].getSheet();}

    //SpriteIndecies Getters/Setters.
    public byte[] getSpriteIndecies(){return this.spriteIndecies;}
    public void setSpriteIndeciesPointer(byte[] spriteIndecies){this.spriteIndecies = spriteIndecies;}

    //Wrap Getters/Setters.
    public @fixed short[] f_getWrap(){return this.f_wrap;}
    public void f_setWrapPointer(short[] f_wrap){this.f_wrap = f_wrap;}

    //Emission Getter/Setter.
    public float getEmission(){return emission;}
    public void setEmission(float emission){this.emission = emission;}

    //Material Getter/Setter.
    public Material getMaterial(){return material;}
    public void setMaterial(Material material){this.material = material;}


    public static float convertByteToShear(byte b)
    {
        switch(b)
        {
            case 1: return 1.0f;
            case 2: return -1.0f;
            //
            case 3: return 0.5f;
            case 4: return -0.5f;
            //
            case 5: return 2.0f;
            case 6: return -2.0f;
            //
            default: return 0.0f;
        }
    }

    public static byte convertShearToByte(float shear)
    {
        int s = (int)(shear * 2.0f);

        switch(s)
        {
            //1.0f
            case 2: return 1;
            case -2: return 2;

            //0.5f
            case 1: return 3;
            case -1: return 4;

            //2.0f
            case 4: return 5;
            case -4: return 6;

            //0.0f
            default: return 0;
        }
    }

    public byte[] getShearsAsBytes()
    {
        byte[] result = new byte[shears.length];

        for(int i = 0; i < result.length; i++)
        {result[i] = convertShearToByte(shears[i]);}

        return result;
    }


    public static String getShearType_Text(byte st)
    {
        switch(st)
        {
            case SHEARTYPE_FLOOR: return "FLR";
            case SHEARTYPE_WALL: return "WA";
            case SHEARTYPE_Y: return "Y";
            case SHEARTYPE_ZX: return "ZX";
            case SHEARTYPE_ZY: return "ZY";
            default: return null;
        }
    }




    /**Renders this TileMesh altogether.*/
    public void render(Screen screen, int xPos, int yPos, int zPos, float scale)//, int width, int height)
    {
        int wrapX = f_wrap[0] >> Fixed.f_FRACTION_BITS;
        int wrapY = f_wrap[1] >> Fixed.f_FRACTION_BITS;
        
        //double times = 0.0;

        for(int i = 0; i < spriteIndecies.length; i++)
        {
            int spriteIndex = spriteIndecies[i], io = i*3,
            xOffset = (int)(offsets[io] * scale),
            yOffset = (int)(offsets[io+1] * scale),
            zOffset = (int)(offsets[io+2] * scale);
            
            //double time = GLFW.glfwGetTime();

            //Call render function.
            //screen.renderTile(xPos + xOffset, yPos + yOffset, zPos + zOffset, sprites[spriteIndex], normalMaps[spriteIndex],
            //Sprite.FLIP_NONE, wrapX, wrapY, blendingColor, scale, shearTypes[i], shears[i], true);

            //TODO software renderer.
            screen.lighting_TileRenderFunctions[shearTypes[i]].invoke
            (
                xPos + xOffset, yPos + yOffset, zPos + zOffset, sprites[spriteIndex], normalMaps[spriteIndex], emission,
                wrapX, wrapY, blendingColor, scale, shears[i], true
            );

            //System.out.println(shearTypes[i] + " " + (GLFW.glfwGetTime() - time));
            //times += (GLFW.glfwGetTime() - time);

            //screen.renderTile(xPos + xOffset, yPos + yOffset, zPos + zOffset, sprites[spriteIndex],// normalMaps[spriteIndex],
            //Sprite.FLIP_NONE, wrapX, wrapY, blendingColor, scale, shearTypes[i], shears[i], true);
        }

        //System.out.println(times / spriteIndecies.length);
    }

    /**Renders this TileMesh altogether.*/
    public void render_Ent(Screen screen, int xPos, int yPos, int zPos, float scale)//, int width, int height)
    {
        int wrapX = f_wrap[0] >> Fixed.f_FRACTION_BITS;
        int wrapY = f_wrap[1] >> Fixed.f_FRACTION_BITS;
        
        for(int i = 0; i < spriteIndecies.length; i++)
        {
            int spriteIndex = spriteIndecies[i], io = i*3,
            xOffset = (int)(offsets[io] * scale),
            yOffset = (int)(offsets[io+1] * scale),
            zOffset = (int)(offsets[io+2] * scale);
            
            //Call render function.
            screen.renderTile_Ent(xPos + xOffset, yPos + yOffset, zPos + zOffset, sprites[spriteIndex], normalMaps[spriteIndex], emission,
            wrapX, wrapY, blendingColor, scale, shearTypes[i], shears[i], true);
        }
    }

    /**Renders this TileMesh as a 2D sprite.*/
    public void render_2D(Screen screen, int xPos, int yPos)
    {
        //screen.renderSprite(xPos + floorOffset.x, yPos + floorOffset.y, floorSprite, flip, blendingColor, false);
        //screen.renderSprite(xPos + wallOffset.x, yPos + wallOffset.y, wallSprite, flip, blendingColor, false);

        int wrapX = f_wrap[0] >> Fixed.f_FRACTION_BITS;
        int wrapY = f_wrap[1] >> Fixed.f_FRACTION_BITS;
        
        for(int i = 0; i < spriteIndecies.length; i++)
        {
            int spriteIndex = spriteIndecies[i], io = i*3;

            /*
            screen.basic_TileRenderFunctions[shearTypes[i]].invoke
            (
                xPos + offsets[io], yPos + offsets[io+1] - (offsets[io+2] * 0.5f), 0, sprites[spriteIndex],
                wrapX, wrapY, blendingColor, 1.0f, shears[i], true
            );
            */

            //Call render function.
            screen.renderTile
            (
                xPos + offsets[io], yPos + offsets[io+1] - (int)(offsets[io+2] * 0.5f),
                sprites[spriteIndex], wrapX, wrapY, blendingColor, 1.0f, shearTypes[i], shears[i], false
            );
        }
    }

    /*
     * Lava: One sprite, increment X position over time, set y position for different frames.
     */

     /**Converts a single TileMesh into byte data.*/
    public byte[] translateTo_DAT(Sprite[] tileSet_spriteLayout)
    {
        //
        //Number of Sprites held - 1 byte, signed.
        //
        Sprite[][] spritesInTileMesh = new Sprite[][]
        {
            this.sprites,
            this.normalMaps
        };
        byte numSpritesInTileMesh = (byte)sprites.length;


        //
        //Sprite and NormalMap IDs - numSpritesInTileMesh * 2 bytes, unsigned.
        //
        byte[][] spriteIDsInTileMesh_bytes = new byte[spritesInTileMesh.length][spritesInTileMesh[0].length << 1];

        //0 = Sprites, 1 = Normal Maps.
        for(int a = 0; a < spritesInTileMesh.length; a++)
        {
            //Cache array.
            Sprite[] spriteArray = spritesInTileMesh[a];

            //Iterate through all of the sprites in this array.
            for(int s = 0; s < spriteArray.length; s++)
            {
                //Cache the current sprite.
                Sprite tSprite = spriteArray[s];

                //Compare them to the sprites in the sprite layout.
                for(int i = 0; i < tileSet_spriteLayout.length; i++)
                {
                    //If it's the same sprite, then this is the ID.
                    if(tSprite == tileSet_spriteLayout[i])
                    {
                        spriteIDsInTileMesh_bytes[a][(s << 1)] = (byte)((i & 0xFF00) >>> 8);
                        spriteIDsInTileMesh_bytes[a][(s << 1) + 1] = (byte)(i & 0x00FF);

                        break;
                    }
                    //
                    else if(i >= tileSet_spriteLayout.length-1)
                    {
                        System.err.println("This tileSprite does not belong to the given tileSet_spriteLayout");
                        return null;
                    }
                }
            }
        }


        //
        //Number of Sprite Indecies - 1 byte, unsigned.
        //
        byte numSpriteIndeciesInTileMesh = (byte)spriteIndecies.length;


        //
        //Sprite Indecies - numSpriteIndecies bytes, signed.
        //
        byte[] spriteIndeciesInTileMesh;
        if(spriteIndecies[0] == -1){spriteIndeciesInTileMesh = new byte[]{-1};}
        else{spriteIndeciesInTileMesh = spriteIndecies;}


        //Offsets - 3 * numSpriteIndecies bytes, signed. Already in format.
        //byte[] offsets = tileSprite.getOffsets();


        //ShearTypes - numSpriteIndecies bytes, signed. Already in format.
        //byte[] shearTypes = tileSprite.getShearTypes();

        
        //
        //ShearValues - numSpriteIndecies bytes, signed.
        //
        byte[] shearValuesInTileMesh = getShearsAsBytes();


        //
        //Color - 4 bytes [0-255].
        //
        byte[] colorInTileMesh = Screen.vector4fToBytes(blendingColor);
        if(colorInTileMesh[0] == 0){colorInTileMesh = new byte[]{0};}
        //If the alpha is zero, dont save the red, green, and blue portions.


        //
        //Emission - 1 fixed point byte.
        //
        byte emissionInTileMesh = (byte)fixed(emission);


        //
        //MaterialID - 1 byte.
        //
        //byte materialInTileMesh = material.getID();
        byte materialInTileMesh = 0;


        //Create array.
        byte[] result = new byte
        [
            1//numSpritesInTileMesh
            + (numSpritesInTileMesh * Short.BYTES)//spriteIDsInTileMesh
            + (numSpritesInTileMesh * Short.BYTES)//normalMapIDsInTileMesh
            + 1//numSpriteIndeciesInTileMesh
            + spriteIndeciesInTileMesh.length
            + offsets.length
            + shearTypes.length
            + shearValuesInTileMesh.length
            + colorInTileMesh.length
            + 1//emissionInTileMesh
            + 1//materialInTileMesh
        ];
        int offset = 0;

        //
        //Fill it.
        //

        //numSprites
        result[offset++] = numSpritesInTileMesh;

        //spriteIDs
        for(int a = 0; a < spriteIDsInTileMesh_bytes.length; a++)
        {
            byte[] byteArray = spriteIDsInTileMesh_bytes[a];

            for(int i = 0; i < byteArray.length; i++)
            {result[offset++] = byteArray[i];}
        }

        //numSpriteIndecies
        result[offset++] = numSpriteIndeciesInTileMesh;

        //spriteIndecies.
        for(int i = 0; i < numSpriteIndeciesInTileMesh; i++)
        {result[offset++] = spriteIndeciesInTileMesh[i];}

        //offsets
        for(int i = 0; i < numSpriteIndeciesInTileMesh * 3; i++)
        {result[offset++] = offsets[i];}
        //numSpriteIndeciesInTileMesh is used in case TileMesh was instantiated using different amounts of offsets than indecies.

        //shearTypes
        for(int i = 0; i < numSpriteIndeciesInTileMesh; i++)
        {result[offset++] = shearTypes[i];}

        //shearValues
        for(int i = 0; i < numSpriteIndeciesInTileMesh; i++)
        {result[offset++] = shearValuesInTileMesh[i];}

        //color
        for(int i = 0; i < colorInTileMesh.length; i++)
        {result[offset++] = colorInTileMesh[i];}

        //emission
        result[offset++] = emissionInTileMesh;

        //materialID
        result[offset++] = materialInTileMesh;


        //Alright, we're done.
        return result;
    }
}
