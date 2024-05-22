package JettersR.Graphics;
/**
 * The default renderer for the game.
 * Most Compatability, Worst Performance.
 * 
 * Unfortunitly, due to how annoying GLFW is in terms of working with it,
 * I cannot use Java's rendering API to render a pixel array. I instead 
 * have to rely on a REALLY early version of OpenGL instead.
 * Specifically, OpenGL 1.2.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/24/2023
 */
import org.lwjgl.opengl.GL12;

import JettersR.Game;
import JettersR.Entities.Components.Lights.AreaLight;
import JettersR.Entities.Components.Lights.DirectionalLight;
import JettersR.Entities.Components.Lights.Light;
import JettersR.Tiles.Graphics.TileMesh;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
//import JettersR.Util.Shapes.Shapes3D.Misc.AAB_RoundedBox;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix4f;

//import JettersR.Game;

public class SoftwareScreen extends Screen
{
    //Pixel Data
    private int[] pixels, layers;

    //Lights array.
    private Light[] uLights = new Light[Screen.MAX_LIGHTS_ON_SCREEN];
    private int numLights = 0;

    /**
     * Cell Class for Tile-Based Forward Lighting.
     */
    private class Cell
    {
        //
        //0x[id_2] [id_1] [id_0] [dLights][aLights]
        //0x[id_6] [id_5] [id_4] [id_3]
        //0x[id_a] [id_9] [id_8] [id_7]
        //0x[id_e] [id_d] [id_c] [id_b]
        //
        //public int[] lightID_Data = new int[4];

        //[dLights][aLights]
        public byte lightType_Data = 0x00;

        //Light ID numbers.
        public byte[] lightIDs = new byte[MAX_LIGHTS_PER_CELL];

        /**Adds a light to thiss cell if possible.*/
        public void addLight(Light light, int slot)
        {
            int area_Lights = lightType_Data & 0x0F;
            int directional_Lights = ((lightType_Data & 0xF0) >> 4) & 0xFF;

            if(area_Lights + directional_Lights < MAX_LIGHTS_PER_CELL)
            {
                if(light instanceof AreaLight)
                {
                    //Push Directional Light IDs over.
                    for(int d = 0; d < directional_Lights; d++)
                    {lightIDs[area_Lights + d] = lightIDs[area_Lights + d + 1];}

                    //Add ID.
                    lightIDs[area_Lights] = (byte)slot;

                    //Increment.
                    area_Lights++;
                }
                else if(light instanceof DirectionalLight)
                {
                    //Add ID.
                    lightIDs[area_Lights + directional_Lights] = (byte)slot;

                    //Increment.
                    directional_Lights++;
                }

                //Recombine.
                lightType_Data = (byte)((directional_Lights << 4) | area_Lights);
            }
        }

        public void reset(){lightType_Data = 0x00;}
    }

    //Light Cell Array.
    private Cell[] uCells = new Cell[(Game.BATTLE_WIDTH / Screen.CELL_SIZE) * (Game.BATTLE_HEIGHT / Screen.CELL_SIZE)];

    /**Constructor.*/
    public SoftwareScreen(int width, int height)
    {
        //Initialize Pixel Array and Viewport.
        setDimensions(width, height, false);
        setViewportDimensions(0, 0, width, height);
    }

    public void setDimensions(int w, int h, boolean API_Initialized)
    {
        //Set screen width and Height.
        WIDTH = w; HEIGHT = h;

        //Initialize crop region.
        setCropRegion(0, 0, WIDTH, HEIGHT);

        //Pixel arrays.
        this.pixels = new int[WIDTH * HEIGHT];
        this.layers = new int[WIDTH * HEIGHT];

        //Create texture.
        GL12.glTexImage2D(GL12.GL_TEXTURE_2D, 0, GL12.GL_RGB, WIDTH, HEIGHT, 0,
        GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (int[])null);
    }

    public void init()
    {
        //Initialize Light Stuff.
        for(int i = 0; i < uCells.length; i++)
        {uCells[i] = new Cell();}
    }

    @Override
    public void sync(){}

    /**Plasters pixels from Pixel Array to the Screen via a single OpenGL 1.2 texture.*/
    public void render(long windowAddress)
    {
        //for(int i = 0; i < pixels.length; i++){layers[i] = layers[i] << 8;}

        //I could've just used glDrawPixels() here, but 1: It's slow.
        //And 2: I... don't think it exists on every computer since it was removed since OpenGL 3.0 or so.

        for(int i = 0; i < layers.length; i++){layers[i] <<= 8;}

        //Reset cells.
        for(int i = 0; i < (WIDTH / CELL_SIZE) * (HEIGHT / CELL_SIZE); i++)
        {uCells[i].reset();}

        for(int i = 0; i < numLights; i++)
        {uLights[i] =  null;}
        numLights = 0;

        //TexSubImage is faster since it just modifies pixel data instead of completely remaking the texture.
        GL12.glTexSubImage2D(GL12.GL_TEXTURE_2D, 0, 0, 0, WIDTH, HEIGHT,
        GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
        pixels);
        //layers);

        //Begin OpenGL 1.2 Quad Drawing Mode
        GL12.glBegin(GL12.GL_QUADS);

        //Upper-Left corner
        GL12.glTexCoord2f(0.0f, 0.0f);
        GL12.glVertex3f(-1.0f, 1.0f, 0.0f);

        //Upper-Right Corner
        GL12.glTexCoord2f(1.0f, 0.0f);
        GL12.glVertex3f(1.0f, 1.0f, 0.0f);

        //Lower-Right Corner
        GL12.glTexCoord2f(1.0f, 1.0f);
        GL12.glVertex3f(1.0f, -1.0f, 0.0f);

        //Lower-Left Corner
        GL12.glTexCoord2f(0.0f, 1.0f);
        GL12.glVertex3f(-1.0f, -1.0f, 0.0f);

        //End Drawing Mode
        GL12.glEnd();
        //GL12.glFlush();

        //Display to the screen
        glfwSwapBuffers(windowAddress);

        //System.out.println(ID);
    }

    //Constant color used for clearing the screen.
    private final int clearColor = 0x00000000;
    //0x00202020;

    //Constant used for resetting layers.
    private static final int clearLayer = Integer.MIN_VALUE;

    /**Clears the screen of any pixels.*/
    public void clear()
    {
        for(int i = 0; i < pixels.length; i++)
        {
            pixels[i] = clearColor;
            layers[i] = clearLayer;
        }
    }

    

    //for one vertex: [xPos] [yPos] [zPos] [texID] [tcX] [tcY] [tnX] [tnY] [r] [g] [b] [a]
    //public void passVerticies(SpriteSheet sheet, int xPos, int yPos, )

    @Override
    public void addLight(Light light, float scale)
    {
        if(numLights >= Screen.MAX_LIGHTS_ON_SCREEN){return;}

        //Add light to list.
        uLights[numLights] = light;

        //Which cell is this light in?
        Vector3f position = light.getPosition3f();
        Shape3D shape = light.getShape();

        int left = (int)( ((position.x + shape.f_left()) * scale) - xOffset ) / Screen.CELL_SIZE;
        int right = (int)(( ((position.x + shape.f_right()) * scale) - xOffset ) / Screen.CELL_SIZE) + 1;
        if(left < 0){left = 0;}
        if(right >= Game.BATTLE_WIDTH / Screen.CELL_SIZE){right = (Game.BATTLE_WIDTH / Screen.CELL_SIZE) - 1;}
        //
        int back = (int)( (((position.y + shape.f_back()) * scale) - yOffset) - ((((position.z + shape.f_top()) * scale) - zOffset) / 2) ) / Screen.CELL_SIZE;
        int front = (int)(( (((position.y + shape.f_front()) * scale) - yOffset) - ((((position.z + shape.f_bottom()) * scale) - zOffset) / 2) ) / Screen.CELL_SIZE) + 1;
        if(back < 0){back = 0;}
        if(front >= Game.BATTLE_HEIGHT / Screen.CELL_SIZE){front = (Game.BATTLE_HEIGHT / Screen.CELL_SIZE);}

        for(int y = back; y < front; y++)
        {
            for(int x = left; x < right; x++)
            {
                uCells[x + (y * (Game.BATTLE_WIDTH / Screen.CELL_SIZE))].addLight(light, numLights);
            }
        }

        //Increment numLights.
        numLights++;
    }


    //Global Light stuff
    private Vector3f globalLight_Direction = new Vector3f(),
    globalLight_Diffuse = new Vector3f(1.0f),
    globalLight_Ambient = new Vector3f(0.5f);

    @Override
    /**Sets the currently used Global Light.*/
    public void setGlobalLight(Vector3f direction, Vector3f diffuse, Vector3f ambient)
    {
        globalLight_Direction.set(direction.x, direction.y, direction.z);
        globalLight_Diffuse.set(diffuse.x, diffuse.y, diffuse.z);
        globalLight_Ambient.set(ambient.x, ambient.y, ambient.z);
    }

    

    private static final Vector3f CALC_FPOSITION = new Vector3f(),
    CALC_LIGHTPOS = new Vector3f(),
    CALC_LIGHTDIR = new Vector3f(),
    CALC_AMBIENT = new Vector3f(),
    CALC_DIFFUSE = new Vector3f();

    private void calculate_Area_Light(Vector4f color, Vector3f normal, AreaLight aLight, Vector3f result)
    {
        Vector3f aLight_position = aLight.getPosition3f();

        //Get closest corner.
        float closePointX = Math.max(aLight_position.x, Math.min(CALC_FPOSITION.x(), aLight_position.x + aLight.getWidth()));
        float closePointY = Math.max(aLight_position.y, Math.min(CALC_FPOSITION.y(), aLight_position.y + aLight.getHeight()));
        float closePointZ = Math.max(aLight_position.z, Math.min(CALC_FPOSITION.z(), aLight_position.z + aLight.getDepth()));
        
        //Get distances from pixel position to closest corner.
        float sideX = closePointX - CALC_FPOSITION.x();
        float sideY = closePointY - CALC_FPOSITION.y();
        float sideZ = closePointZ - CALC_FPOSITION.z();

        //Do Pythagorean Theorum (if inside, will be zero).
        float sqrLength = (sideX * sideX) + (sideY * sideY) + (sideZ * sideZ);
        //float sqrLength = max((sideX * sideX) + (sideY * sideY) + (sideZ * sideZ), 1.0);

        //Attenuation (based solely off of distance from light source).
        float attenuation = Math.max((float)(aLight.getRadius() - Math.sqrt(sqrLength)), 0.0f) / aLight.getRadius();

        //"Toonification" by clamping the attenuation to specfic regions.
        //int ia = (int)((attenuation + 0.24f) * 1020);
        //float amAten = (ia / 255) / 4.0f;

        //int ia = int((attenuation + 0.49) * 510);
        //float amAten = (ia / 255) / 2.0;

        //int ia = int((attenuation + 0.238) * (aLight.position_OuterRadius.w * 4.0));
        //float amAten = int(ia / aLight.position_OuterRadius.w) / 4.0;

        //Ambient value.
        //CALC_AMBIENT.set(amAten);
        CALC_AMBIENT.set(attenuation);

        //Normal Stuff.
        //vec3 norm = normalize(normal);
        CALC_LIGHTPOS.set(closePointX + normal.x(), closePointY + normal.y(), closePointZ + normal.z());
        //vec3 lightPos = vec3(closePointX, closePointY, closePointZ);
        CALC_LIGHTPOS.sub(CALC_FPOSITION, CALC_LIGHTDIR);
        CALC_LIGHTDIR.normalize();

        //Diffuse Color (Dot product of pixel normal and direction to light, no less than 0).
        float diff = Math.max(normal.dot(CALC_LIGHTDIR), 0.0f) * attenuation;
        //float diff = dot(normal, lightDir);

        //int id = (int)((diff + 0.24f) * 1020);
        //diff = (id / 255) / 4.0f;

        //int id = int((diff) * (aLight.position_OuterRadius.w * 4.0));
        //diff = int(id / aLight.position_OuterRadius.w) / 4.0;

        //Diffuse value.
        CALC_DIFFUSE.set(diff);
        //vec3 diffuse = vec3(diff * attenuation);

        //Apply Light Colors.
        CALC_AMBIENT.mul(aLight.getAmbientColor());
        CALC_DIFFUSE.mul(aLight.getDiffuseColor());

        //Write the final result.
        //return = vec4(vec3(attenuation), 1.0);
        //return (diffuse);
        //return (ambient) * color.rgb;
        //return (diffuse) * color.rgb;

        //return (ambient + diffuse) * color.rgb;
        //System.out.println(aLight.getAmbientColor());
        result.add( CALC_AMBIENT.add(CALC_DIFFUSE).mul(color.x, color.y, color.z) );

        //CALC_AMBIENT.add(CALC_DIFFUSE);
        //result.add(CALC_AMBIENT);
    }

    private void calculate_Directional_Light(int xPos, int yPos, int zPos, Vector4f color, Vector3f normal, DirectionalLight dLight)
    {
        
    }

    @Override
    public void reset_RenderBatches(){return;}

    
    //Crop region points.
    protected int CROP_X0, CROP_Y0, CROP_X1, CROP_Y1;

    @Override
    /**Sets crop region.*/
    public final void setCropRegion(int cropX0, int cropY0, int cropX1, int cropY1)
    {
        this.CROP_X0 = cropX0;
        this.CROP_Y0 = cropY0;
        this.CROP_X1 = cropX1;
        this.CROP_Y1 = cropY1;
    }

    @Override
    public final void resetCropRegion()
    {
        //Set crop region to default values.
        this.CROP_X0 = 0;
        this.CROP_Y0 = 0;
        this.CROP_X1 = WIDTH;
        this.CROP_Y1 = HEIGHT;
    }

    /*
     * EVERYTHING below this threshold will be applying Sprites to the Pixel Array
     */

    @Override
    /**Renders a SpriteSheet to the Pixel Array.*/
    public void renderSheet(int xPos, int yPos, int zPos, SpriteSheet sheet, boolean fixed)
    {
        //Offset Positions by Camera
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        yPos -= (zPos/2);

        //Determine what pixels you NEED to render
        int startY = (yPos < 0) ? -yPos : 0,
        endY = (yPos + sheet.getHeight() >= HEIGHT) ? HEIGHT - yPos : sheet.getHeight();

        int startX = (xPos < 0) ? -xPos : 0,
        endX = (xPos + sheet.getWidth() >= WIDTH) ? WIDTH - xPos : sheet.getWidth();

        for(int y = startY; y < endY; y++)
        {
            int y0 = y + yPos;

            for(int x = startX; x < endX; x++)
            {
                int x0 = x + xPos;

                int color = sheet.getPixel(x, y);
                int index = x0 + y0 * WIDTH;
                if((color & 0xFF000000) == 0xFF000000)
                {
                    //Put Opaque Pixel
                    pixels[index] = color;
                }
                else if(color != 0x00000000)
                {
                    //Put Translucent Pixel instead.
                    pixels[index] = translucentColor(color, pixels[index]);
                }
            }
        }
    }

    @Override
    /**Renders a SpriteSheet to the Pixel Array.*/
    public void renderSheet(int xPos, int yPos, int zPos, SpriteSheet sheet, Vector4f blendingColor, boolean fixed)
    {
        if(blendingColor.x >= 1.0f && blendingColor.y >= 1.0f
        && blendingColor.z >= 1.0f && blendingColor.w >= 1.0f)
        {
            renderSheet(xPos, yPos, zPos, sheet, fixed);
            return;
        }

        //Offset Positions by Camera
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        yPos -= (zPos/2);

        //Determine what pixels you NEED to render
        int startY = (yPos < 0) ? -yPos : 0,
        endY = (yPos + sheet.getHeight() >= HEIGHT) ? HEIGHT - yPos : sheet.getHeight();

        int startX = (xPos < 0) ? -xPos : 0,
        endX = (xPos + sheet.getWidth() >= WIDTH) ? WIDTH - xPos : sheet.getWidth();

        for(int y = startY; y < endY; y++)
        {
            int y0 = y + yPos;

            for(int x = startX; x < endX; x++)
            {
                int x0 = x + xPos;

                int color = multipliedColor(sheet.getPixel(x, y), blendingColor);
                int index = x0 + y0 * WIDTH;
                if((color & 0xFF000000) == 0xFF000000)
                {
                    //Put Opaque Pixel
                    pixels[index] = color;
                }
                else if(color != 0x00000000)
                {
                    //Put Translucent Pixel instead
                    pixels[index] = translucentColor(color, pixels[index]);
                }
            }
        }
    }


    
    /**
     * Software Screen specific function for rendering sprites.
     * 
     * @param pixelPutter the PixelPutter function.
     * @param xPos the x position of the sprite.
     * @param yPos the y position of the sprite.
     * @param sprite the sprite to render.
     * @param pixelGetter the function to uses for getting a pixel. Used for Sprite flipping.
     * @param blendingColor the color to blend the sprite's pixels with.
     * @param fixed determines if this sprite's position should be "fixed" with the screen's offsets.
     */
    private void renderSprite(PixelPutter pixelPutter, int xPos, int yPos, int zPos, int depth, Sprite sprite, int sprite_Width, int sprite_Height,
    Sprite.PixelGetter pixelGetter, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {
        //Offset Positions by Camera
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        int screenY = yPos - (zPos/2);
        
        //Normal stuff.
        float abs_depth = Math.abs(depth / 2f);
        //float zNormal = abs_depth / ((float)sprite_Height - abs_depth);
        float yNormal = (depth == 0) ? 0 : -(abs_depth / sprite_Height);

        //if depth == 0 ? zNormal = 1.0

        //Determine what pixels we NEED to render so that way we don't have to do an out-of-bounds check every time.
        int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
        endY = (screenY + sprite_Height >= CROP_Y1) ? CROP_Y1 - screenY : sprite_Height;

        int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
        endX = (xPos + sprite_Width >= CROP_X1) ? CROP_X1 - xPos : sprite_Width;

        //int layerY = (layer & 0x0000FFFF), layerZ = (layer & 0xFFFF0000) >> 16;
        //int layerY = sprite_Height + yPos;
        for(int y = startY; y < endY; y++)
        {
            //Y Screen Position.
            int y0 = y + screenY;

            //Current Z Position.
            int currentZ = (int)( ((y * 2) * yNormal) + zPos);

            for(int x = startX; x < endX; x++)
            {
                //X Screen Position.
                int x0 = x + xPos;

                //Final index.
                int index = x0 + y0 * WIDTH;
                pixelPutter.invoke(pixelGetter, x + wrapX, y + wrapY, null, index, blendingColor, 0, 0, currentZ);
            }
        }
    }

    
    @Override
    /**Renders a Sprite to the Pixel Array.*/
    public void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {
        renderSprite(this::putPixel, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), 0, 0, null, fixed);
    }

    @Override
    /**Renders a cropped Sprite to the Pixel Array with depth.*/
    public void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {
        renderSprite(this::putPixel_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), 0, 0, null, fixed);
    }

    @Override
    /**Renders a cropped Sprite with a color to blend.*/
    public void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {
        renderSprite(this::putPixel_Blend, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), 0, 0, blendingColor, fixed);
    }

    @Override
    /**Renders a cropped Sprite to the Pixel Array with a color to blend and layers.*/
    public void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {
        renderSprite(this::putPixel_Blend_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), 0, 0, blendingColor, fixed);
    }



    /**
     * Software Screen specific function for rendering scaled sprites.
     * 
     * @param pixelPutter the PixelPutter function.
     * @param xPos the x position of the sprite.
     * @param yPos the y position of the sprite.
     * @param sprite the sprite to render.
     * @param pixelGetter the function to uses for getting a pixel. Used for Sprite flipping.
     * @param blendingColor the color to blend the sprite's pixels with.
     * @param xScale the x scale of the sprite.
     * @param yScale the y scale of the sprite.
     * @param fixed determines if this sprite's position should be "fixed" with the screen's offsets.
    */
    private void renderSprite_Sc(PixelPutter pixelPutter, int xPos, int yPos, int zPos, int depth, Sprite sprite, int sprite_Width, int sprite_Height, Sprite.PixelGetter pixelGetter,
    Sprite.PixelGetter normalGetter, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed)
    {
        //Offset Positions by Camera
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        int screenY = yPos - (zPos/2);

        //Dimensions
        int resultWidth = (int)((sprite_Width * xScale) + 0.5f),
        resultHeight = (int)((sprite_Height * yScale) + 0.5f);

        //Wraps.
        int wx = (int)(wrapX * xScale),
        wy = (int)(wrapY * yScale);

        //Normal stuff.
        float half_depth = (depth / 2f);
        //float zNormal = half_depth / ((float)resultHeight - half_depth);
        float yNormal = (resultHeight == 0) ? 0 : -(half_depth / resultHeight);

        //Determine what pixels you NEED to render.
        int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
        endY = (screenY + resultHeight >= CROP_Y1) ? CROP_Y1 - screenY : resultHeight;

        int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
        endX = (xPos + resultWidth >= CROP_X1) ? CROP_X1 - xPos : resultWidth;

        //Sprite.PixelGetter normalGetter = Sprites.flatSprite.getPixelGetter(Sprite.Flip.NONE);

        //int layerY = (layer & 0x0000FFFF), layerZ = (layer & 0xFFFF0000) >> 16;
        for(int y = startY; y < endY; y++)
        {
            //Y Screen Position.
            int y0 = y + screenY;

            //Current Y and Z Position.
            int currentZ = (int)( ((y * 2) * yNormal) + zPos);

            for(int x = startX; x < endX; x++)
            {
                int x0 = x + xPos;
                //
                int index = x0 + y0 * WIDTH;
                //
                pixelPutter.invoke(pixelGetter, //(int)Math.round(x / xScale), (int)Math.round(y / yScale),
                (int)(((x + wx) / (float)(resultWidth)) * (sprite_Width)),
                (int)(((y + wy) / (float)(resultHeight)) * (sprite_Height)),
                normalGetter,
                index, blendingColor, 0, 0, currentZ);
            }
        }
    }


    @Override
    /**Renders a scaled Sprite.*/
    public void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed)
    {
        renderSprite_Sc(this::putPixel, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), null, wrapX, wrapY, null, xScale, yScale, fixed);
    }

    @Override
    /**Renders a scaled Sprite with a color to blend.*/
    public void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed)
    {
        renderSprite_Sc(this::putPixel_Blend, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), null, wrapX, wrapY, blendingColor, xScale, yScale, fixed);
    }

    @Override
    /**Renders a scaled Sprite with depth.*/
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed)
    {
        renderSprite_Sc(this::putPixel_Depth,
        //this::light_putPixel_Blend_Depth,
        xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), null, wrapX, wrapY, Screen.DEFAULT_BLEND, xScale, yScale, fixed);
    }
    
    @Override
    /**Renders a scaled and cropped Sprite with a color to blend and depth.*/
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed)
    {
        renderSprite_Sc(this::putPixel_Blend_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), null, wrapX, wrapY, blendingColor, xScale, yScale, fixed);
    }


    @Override
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, int wrapX, int wrapY, float xScale, float yScale, boolean fixed)
    {
        renderSprite_Sc(this::light_putPixel_Blend_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(), sprite.getPixelGetter(flip),
        normalMap.getPixelGetter(flip), wrapX, wrapY, Screen.DEFAULT_BLEND, xScale, yScale, fixed);
    }
    
    @Override
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed)
    {
        renderSprite_Sc(this::light_putPixel_Blend_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(), sprite.getPixelGetter(flip),
        normalMap.getPixelGetter(flip), wrapX, wrapY, blendingColor, xScale, yScale, fixed);
    }
    



    /**
     * Software Screen specific function for rendering stretched sprites.
     * 
     * @param pixelPutter the PixelPutter function.
     * @param xPos the x position of the sprite.
     * @param yPos the y position of the sprite.
     * @param sprite the sprite to render.
     * @param pixelGetter the function to uses for getting a pixel. Used for Sprite flipping.
     * @param resultWidth the width of the sprite.
     * @param resultHeight the height of the sprite.
     * @param blendingColor the color to blend the sprite's pixels with.
     * @param fixed determines if this sprite's position should be "fixed" with the screen's offsets.
     */
    private void renderSprite_St(PixelPutter pixelPutter, int xPos, int yPos, int zPos, int depth, Sprite sprite, int sprite_Width, int sprite_Height,
    Sprite.PixelGetter pixelGetter, int wrapX, int wrapY, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed)//, int layer)
    {
        //Offset Positions by Camera
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        int screenY = yPos - (zPos/2);

        //Normal stuff.
        float abs_depth = Math.abs(depth / 2f);
        //float zNormal = abs_depth / ((float)resultHeight - abs_depth);
        float yNormal = (depth == 0) ? 0 : -(abs_depth / resultHeight);
        

        //Determine what pixels you NEED to render
        int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
        endY = (screenY + resultHeight >= CROP_Y1) ? CROP_Y1 - screenY : resultHeight;

        int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
        endX = (xPos + resultWidth >= CROP_X1) ? CROP_X1 - xPos : resultWidth;


        //int layerY = (layer & 0x0000FFFF), layerZ = (layer & 0xFFFF0000) >> 16;
        for(int y = startY; y < endY; y++)
        {
            //Y Screen Position.
            int y0 = y + screenY;

            //Current Y and Z Position.
            int currentZ = (int)( ((y * 2) * yNormal) + zPos);

            for(int x = startX; x < endX; x++)
            {
                int x0 = x + xPos;
                //
                int index = x0 + y0 * WIDTH;
                //
                pixelPutter.invoke(pixelGetter,
                (int)((x / (float)(resultWidth)) * (sprite_Width)),
                (int)((y / (float)(resultHeight)) * (sprite_Height)),
                null, index, blendingColor, 0, 0, currentZ);
            }
        }
    }

    @Override
    /**Renders a stretched Sprite.*/
    public void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, 
    int resultWidth, int resultHeght, boolean fixed)
    {
        renderSprite_St(this::putPixel, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, null, resultWidth, resultHeght, fixed);
    }

    @Override
    /**Renders a stretched Sprite with a color to blend.*/
    public void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    int resultWidth, int resultHeght, boolean fixed)
    {
        renderSprite_St(this::putPixel_Blend, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, blendingColor, resultWidth, resultHeght, fixed);
    }

    @Override
    /**Renders a stretched Sprite with depth.*/
    public void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, 
    int resultWidth, int resultHeght, boolean fixed)
    {
        renderSprite_St(this::putPixel_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, null, resultWidth, resultHeght, fixed);
    }

    @Override
    /**Renders a stretched Sprite with a color to blend and depth.*/
    public void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    int resultWidth, int resultHeght, boolean fixed)
    {
        renderSprite_St(this::putPixel_Blend_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, blendingColor, resultWidth, resultHeght, fixed);
    }



    /**
     * Software Screen specific function for rendering sheared sprites.
     * 
     * @param pixelPutter the PixelPutter function.
     * @param xPos the x position of the sprite.
     * @param yPos the y position of the sprite.
     * @param sprite the sprite to render.
     * @param pixelGetter the function to uses for getting a pixel. Used for Sprite flipping.
     * @param xShear the x shear of the sprite.
     * @param yShear the y shear of the sprite.
     * @param blendingColor the color to blend the sprite's pixels with.
     * @param fixed determines if this sprite's position should be "fixed" with the screen's offsets.
     */
    private void renderSprite_Sh(PixelPutter pixelPutter, int xPos, int yPos, int zPos, Sprite sprite, int sprite_Width, int sprite_Height, Sprite.PixelGetter pixelGetter,
    int wrapX, int wrapY, Vector4f blendingColor, float xShear, float yShear, float zxShear, float zyShear, boolean fixed)
    {
        //Offset Positions by Camera.
        
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        
        //Bad Case Check.
        //if((xShear != 0.0f && yShear != 0.0f) && (Math.abs(xShear) > 2.0f || Math.abs(yShear) > 2.0f))
        //if(zxShear != 0.0f || zyShear != 0.0f)
        if(xShear != 0.0f)
        {
            TEMP_MATRIX.identity()

            //Shear.
            .m10(xShear)
            .m01(yShear)
            //.m02(zxShear)
            //.m12(zyShear)

            //Translate (xz and yz shear)
            //.m20(xPos)
            //.m21(yPos)
            //.m22(zPos)

            .m30(xPos)
            .m31(yPos)
            .m32(zPos)
            
            //
            //.scale(1.0f, 1.0f, 1.0f)
            //.translate(xPos, yPos, zPos)
            
            .invert(TEMP_INVERT);

            renderSprite_Affine(pixelPutter, TEMP_MATRIX, TEMP_INVERT, sprite, sprite_Width, sprite_Height,
            pixelGetter, blendingColor, fixed);

            return;
        }

        //TODO Normal

        float scanline_width = (sprite_Width + (sprite_Height * Math.abs(xShear)));
        float scanline_height = (sprite_Height + (sprite_Width * Math.abs(yShear)));

        //Determine what pixels you NEED to render
        /*
        int startY = (yPos < CROP_Y0) ? CROP_Y0 - yPos : 0,
        endY = (yPos + spriteHeight >= CROP_Y1) ? CROP_Y1 - yPos : spriteHeight;

        int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
        endX = (xPos + spriteWidth >= CROP_X1) ? CROP_X1 - xPos : spriteWidth;
        */

        //X first to get Y-Depth value.
        //for(float x = 0; x < scanline_width; x++)
        for(int y = 0; y < scanline_height; y++)
        {
            //float xr = ((x) / scanline_width) * sprite_Width;
            float yr = ((y / scanline_height) * sprite_Height);

            //Y-Depth value.
            //short currentY = (short)((x + (xShearPerY * sprite.getHeight())) + yPos);
            
            //for(float y = 0; y < scanline_height; y++)
            for(int x = 0; x < scanline_width; x += 1)//scanline_width-1)
            {        
                //float yr = ((y) / scanline_height) * sprite_Height;
                float xr = ((x / scanline_width) * sprite_Width);

                //Resulting coordinates to put pixel.
                int x0 = (int)((xr + (yr * xShear)) + 0.5f) + xPos;
                int y0 = (int)((yr + (xr * yShear)) + 0.5f) + yPos;
                
                //Out-Of-Bounds check.
                if(x0 < CROP_X0 || x0 >= CROP_X1 || y0 < CROP_Y0 || y0 >= CROP_Y1){continue;}
                
                //Z-Depth value.
                short currentZ = (short)( (y * 2) + zPos);
                //short currentZ = (short)( ((y * 2) * yNormal) + zPos);

                //Pixel index.
                int index = x0 + y0 * WIDTH;

                //Invoke PixelPutter.
                pixelPutter.invoke(pixelGetter, (int)xr, (int)yr,
                null, index, blendingColor, 0, 0, currentZ); 
            }
        }
    }


    @Override
    /**Renders a sheared Sprite.*/
    public void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, 
    float xShear, float yShear, boolean fixed)
    {
        renderSprite_Sh(this::putPixel, xPos, yPos, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, null, xShear, yShear, 0, 0, fixed);
    }

    @Override
    /**Renders a shear and cropped Sprite with a color to blend.*/
    public void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    float xShear, float yShear, boolean fixed)
    {
        renderSprite_Sh(this::putPixel_Blend, xPos, yPos, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, blendingColor, xShear, yShear, 0, 0, fixed);
    }

    @Override
    /**Renders a sheared Sprite with depth.*/
    public void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, 
    float xShear, float yShear, float zxShear, float zyShear, boolean fixed)
    {
        renderSprite_Sh(this::putPixel_Depth, xPos, yPos, zPos, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, null, xShear, yShear, zxShear, zyShear, fixed);
    }

    @Override
    /**Renders a shear and cropped Sprite with a color to blend and depth.*/
    public void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    float xShear, float yShear, float zxShear, float zyShear, boolean fixed)
    {
        renderSprite_Sh(this::putPixel_Blend_Depth, xPos, yPos, zPos, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, blendingColor, xShear, yShear, zxShear, zyShear, fixed);
    }



    /**
     * Software Screen specific function for rendering rotated sprites.
     * 
     * @param pixelPutter the PixelPutter function.
     * @param xPos the x position of the sprite.
     * @param yPos the y position of the sprite.
     * @param sprite the sprite to render.
     * @param pixelGetter the function to uses for getting a pixel. Used for Sprite flipping.
     * @param rads the rotation of the sprite in radians (for performance purposes).
     * @param originX the x origin of the sprite.
     * @param originY the y origin of the sprite.
     * @param blendingColor the color to blend the sprite's pixels with.
     * @param fixed determines if this sprite's position should be "fixed" with the screen's offsets.
     */
    private void renderSprite_Ro(PixelPutter pixelPutter, int xPos, int yPos, int zPos, int depth, Sprite sprite, int sprite_Width, int sprite_Height,
    Sprite.PixelGetter pixelGetter, int wrapX, int wrapY, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed)
    {
        //Offset Positions by Camera.
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        int screenY = yPos - (zPos/2);
        //Y-Normal is always 1 and Z-Normal is always 0.

        //Trig...
        float sin = (float)Math.sin(rads), cos = (float)Math.cos(rads);

        //Multiply the origin by sin and cos for the actual result origin.
        int oX = (int)((originX * cos) + (originY * -sin));
        int oY = (int)((originX * sin) + (originY * cos));

        //Calculate the resulting dimensions of the rotated output.
        int startX = 0, startY = 0,
        endX = startX, endY = startY;
        //
        int sx = (int)((sprite_Width * cos) + (sprite_Height * -sin)),
        sy = (int)((sprite_Width * sin) + (sprite_Height * cos));
        startX = Math.min(startX, sx); startY = Math.min(startY, sy);
        endX = Math.max(endX, sx); endY = Math.max(endY, sy);
        //
        sx = //(int)((0 * cos) +
        (int)((sprite_Height * -sin));
        sy = //(int)((0 * sin) +
        (int)((sprite_Height * cos));
        startX = Math.min(startX, sx); startY = Math.min(startY, sy);
        endX = Math.max(endX, sx); endY = Math.max(endY, sy);
        //
        sx = (int)((sprite_Width * cos));// + (0 * -sin)
        sy = (int)((sprite_Width * sin));// + (0 * cos));
        startX = Math.min(startX, sx); startY = Math.min(startY, sy);
        endX = Math.max(endX, sx); endY = Math.max(endY, sy);


        //int resultHeight = endY - startY;

        //Offset it all by the result origin.
        startX -= oX;
        endX -= oX;
        //
        startY -= oY;
        endY -= oY;

        //NOW we can clamp these Sprite bounds by the bounds of the screen.
        startY = (screenY + startY < CROP_Y0) ? CROP_Y0 - screenY: startY;
        endY = (screenY + endY >= CROP_Y1) ? CROP_Y1 - screenY : endY;

        startX = (xPos + startX < CROP_X0) ? CROP_X0 - xPos: startX;
        endX = (xPos + endX >= CROP_X1) ? CROP_X1 - xPos : endX;

        //...So anyway, we start plasterin'. POP!
        for(int y = startY; y < endY; y++)
        {
            //Z-Depth.
            int currentZ = (int)( (y * 2) + zPos);

            for(int x = startX; x < endX; x++)
            {
                //Coordinates on the sprite to get the pixel from.
                int x0 = (int)(((float)x * cos) + ((float)y * sin)) + originX,
                y0 = (int)(((float)x * -sin) + ((float)y * cos)) + originY;

                if(x0 < 0 || x0 > sprite.getWidth() || y0 < 0 || y0 >= sprite.getHeight()){continue;}

                //Coordinates on the screen to put the pixel on.
                int x1 = x + xPos, y1 = y + screenY;
                
                //Run PixelPutter.
                int index = x1 + y1 * WIDTH;
                pixelPutter.invoke(pixelGetter, x0, y0, null, index, blendingColor, 0, 0, currentZ);
            }
        }
    }

    @Override
    /**Renders a rotated Sprite.*/
    public void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, 
    float rads, int originX, int originY, boolean fixed)
    {
        renderSprite_Ro(this::putPixel, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, null, rads, originX, originY, fixed);
    }

    @Override
    /**Renders a rotated Sprite with a color to blend.*/
    public void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    float rads, int originX, int originY, boolean fixed)
    {
        renderSprite_Ro(this::putPixel_Blend, xPos, yPos, 0, 0, sprite, sprite.getWidth(), sprite.getHeight(), 
        sprite.getPixelGetter(flip), wrapX, wrapY, blendingColor, rads, originX, originY, fixed);
    }

    @Override
    /**Renders a rotated Sprite with depth.*/
    public void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, 
    float rads, int originX, int originY, boolean fixed)
    {
        renderSprite_Ro(this::putPixel_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), wrapX, wrapY, null, rads, originX, originY, fixed);
    }

    @Override
    /**Renders a rotated Sprite with a color to blend and depth.*/
    public void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    float rads, int originX, int originY, boolean fixed)
    {
        renderSprite_Ro(this::putPixel_Blend_Depth, xPos, yPos, zPos, depth, sprite, sprite.getWidth(), sprite.getHeight(), 
        sprite.getPixelGetter(flip), wrapX, wrapY, blendingColor, rads, originX, originY, fixed);
    }




    /**
     * Software Renderer specific function for rendering tiles.
     */
    private void renderTile(PixelPutter pixelPutter, int xPos, int yPos, int zPos, Sprite sprite, int sprite_Width, int sprite_Height,
    Sprite.PixelGetter pixelGetter, Sprite.PixelGetter normalGetter, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        //TODO
        //Offset Positions by Camera.
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        int screenY = yPos - (zPos / 2);
        //int screenY = (int)(yPos - ((zPos + 0.5f) / 2f));

        //Dimensions
        int scaleWidth = (int)((sprite_Width * scale) + 0.7f),
        scaleHeight = (int)((sprite_Height * scale) + 0.7f);

        //Sprite.PixelGetter normalGetter = Sprites.flatSprite.getPixelGetter(Sprite.Flip.NONE);
        
        /*
         * x: int x0 = (int)((xr + (yr * xShear)) + 0.5f) + xPos; z = 0
         * 
         * y: int y0 = (int)((yr + (xr * yShear)) + 0.5f) + yPos; z = 0
         * 
         * z along x: int z0 = (int)((xr * zxShear) + 0.5f) + zPos;
         * 
         * z along y: int z0 = (int)((yr * zyShear) + 0.5f) + zPos;
         */

        switch(shearType)
        {
            //Just scale, asssume Z-Normal = 0.
            case TileMesh.SHEARTYPE_WALL:
            {
                //Clamp.
                int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
                endY = (screenY + scaleHeight >= CROP_Y1) ? CROP_Y1 - screenY : scaleHeight;

                int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
                endX = (xPos + scaleWidth >= CROP_X1) ? CROP_X1 - xPos : scaleWidth;

                //Go through pixels.
                for(int y = startY; y < endY; y++)
                {
                    //Y Screen Position.
                    int y1 = y + yPos,
                    y0 = y + screenY;

                    //Current Z position.
                    int currentZ = -(y * 2) + zPos;

                    for(int x = startX; x < endX; x++)
                    {
                        int x0 = x + xPos;
                        //
                        int index = x0 + y0 * WIDTH;
                        //
                        pixelPutter.invoke(pixelGetter,
                        (int)(((x / (float)scaleWidth) * sprite_Width)) + wrapX,
                        (int)(((y / (float)scaleHeight) * sprite_Height)) + wrapY,
                        normalGetter, 
                        index, blendingColor, x0, y1, currentZ);
                    }
                }
            }
            break;


            //Y Shear, assume Z-Normal = 0.
            case TileMesh.SHEARTYPE_Y:
            {
                //Clamp.
                //int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
                //endY = (screenY + scaleHeight >= CROP_Y1) ? CROP_Y1 - screenY : scaleHeight;

                int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
                endX = (xPos + scaleWidth >= CROP_X1) ? CROP_X1 - xPos : scaleWidth;

                for(int y = 0; y < scaleHeight; y++)
                {
                    //Percentage through y.
                    //float yr = (y / (float)scaleHeight) * sprite_Height;
                    int wrapYr = (int)(((y / (float)scaleHeight) * sprite_Height) + 0.5f) + wrapY;

                    //Current z value.
                    int currentZ = (-y * 2) + zPos;

                    for(int x = startX; x < endX; x++)
                    {
                        //Percentage through x.
                        //float xr = (x / (float)scaleWidth) * sprite_Width;

                        //Get screen coordinates.
                        int x0 =  x + xPos,
                        //y0 = (int)((yr + (xr * shear)) + 0.5f) + screenY;
                        y1 = (int)(y + ((x * shear))) + yPos,
                        //y0 = y + (x / pixelsPerShear) + screenY
                        y0 = y1 - (zPos/2);

                        if(y0 < CROP_Y0 || y0 >= CROP_Y1){continue;}

                        //Screen index.
                        int index = x0 + y0 * WIDTH;

                        //Put pixel.
                        pixelPutter.invoke(pixelGetter,
                        (int)(((x / (float)scaleWidth) * sprite_Width) + 0.5f) + wrapX,
                        wrapYr,
                        normalGetter, 
                        index, blendingColor, x0, y1, currentZ);
                    }
                }
            }
            break;


            //ZX Shear, assume Z-Normal = 1.
            case TileMesh.SHEARTYPE_ZX:
            {
                //Clamp.
                //int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
                //endY = (screenY + scaleHeight >= CROP_Y1) ? CROP_Y1 - screenY : scaleHeight;

                int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
                endX = (xPos + scaleWidth >= CROP_X1) ? CROP_X1 - xPos : scaleWidth;

                for(int x = startX; x < endX; x++)
                {
                    //X Coordinate.
                    int x0 = x + xPos;

                    //Percentage through x.
                    //float xr = (x / (float)scaleWidth) * sprite_Width;
                    //int wrapXr = (int)(((x + wx) / (float)scaleWidth) * sprite_Width);
                    int wrapXr = (int)((x / (float)scaleWidth) * sprite_Width) + wrapX;

                    //Current Z value.
                    //int currentZ = (int)((xr * shear) + 0.5f) + zPos;
                    int currentZ = (int)((x * shear)) + zPos;
                    //int currentZ = (x / pixelsPerShear) + zPos;

                    //TODO Y-Clamp


                    for(int y = 0; y < scaleHeight; y++)
                    {
                        //Y coordinate.
                        int y0 = y + (yPos - (currentZ / 2));
                        int y1 = y + yPos;

                        if(y0 < CROP_Y0 || y0 >= CROP_Y1){continue;}

                        //Screen index.
                        int index = x0 + y0 * WIDTH;

                        //Put pixel.
                        pixelPutter.invoke(pixelGetter,
                        wrapXr,
                        (int)(((y / (float)scaleHeight) * sprite_Height) + 0.5f) + wrapY,
                        //(int)(((y + wy) / (float)(scaleHeight)) * sprite_Height),
                        normalGetter, 
                        index, blendingColor, x0, y1, currentZ);
                    }
                }
            }
            break;


            //ZY Shear, assume Z-Normal = 1.
            case TileMesh.SHEARTYPE_ZY:
            {
                //Since Z is visually an extension to Y, a new height needs to be calculated.
                int resultHeight = (shear >= 0.0f) ?
                (int)(scaleHeight - (((scaleHeight / 2f) - 0.5f) * shear)) :
                (int)(scaleHeight - ((scaleHeight / 2f) * shear));

                //int resultHeight = (int)(scaleHeight - ((scaleHeight / 2f) / pixelsPerShear));

                //Clamp.
                int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
                endY = (screenY + resultHeight >= CROP_Y1) ? CROP_Y1 - screenY : resultHeight;

                int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
                endX = (xPos + scaleWidth >= CROP_X1) ? CROP_X1 - xPos : scaleWidth;

                //for(int y = 0; y < resultHeight; y++)
                for(int y = startY; y < endY; y++)
                {
                    //Percentage through y.
                    float yr = (y / (float)resultHeight) * scaleHeight;
                    int wrapYr = (int)(((y / (float)resultHeight) * (float)sprite_Height) + 0.5f) + wrapY;

                    //Current Z value.
                    int currentZ = (int)((yr * shear)) + zPos;
                    //int currentZ = (int)((yr / (float)pixelsPerShear) + zPos);

                    //Y Coordinate
                    int y1 = (int)(yr) + yPos;
                    int y0 = y + screenY;
                    //int y0 = y + yPos;
                    //int y0 = (int)(yr) + (yPos - (currentZ/2));

                    for(int x = startX; x < endX; x++)
                    {
                        //X coordinate.
                        int x0 = x + xPos;

                        //Screen index.
                        int index = x0 + y0 * WIDTH;

                        //Put pixel.
                        pixelPutter.invoke(pixelGetter,
                        (int)(((x / (float)scaleWidth) * sprite_Width)) + wrapX,
                        wrapYr,
                        normalGetter, 
                        index, blendingColor, x0, y1, currentZ);
                    }
                }
            }
            break;


            //Just scale, assume Z-Normal = 1.
            default:
            {
                //Clamp.
                int startY = (screenY < CROP_Y0) ? CROP_Y0 - screenY : 0,
                endY = (screenY + scaleHeight >= CROP_Y1) ? CROP_Y1 - screenY : scaleHeight;

                int startX = (xPos < CROP_X0) ? CROP_X0 - xPos : 0,
                endX = (xPos + scaleWidth >= CROP_X1) ? CROP_X1 - xPos : scaleWidth;

                //Go through pixels.
                for(int y = startY; y < endY; y++)
                {
                    //Y Screen Position.
                    int y1 = y + yPos,
                    y0 = y + screenY;

                    for(int x = startX; x < endX; x++)
                    {
                        int x0 = x + xPos;
                        //
                        int index = x0 + y0 * WIDTH;
                        //
                        pixelPutter.invoke(pixelGetter,
                        (int)((x / (float)scaleWidth) * sprite_Width) + wrapX,
                        (int)((y / (float)scaleHeight) * sprite_Height) + wrapY,
                        normalGetter,
                        index, blendingColor, x0, y1, zPos);
                    }
                }
            }
            break;
        }
    }


    @Override
    public void renderTile(int xPos, int yPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(this::tile_putPixel, xPos, yPos, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(Sprite.FLIP_NONE), null, wrapX, wrapY, blendingColor, scale, shearType, shear, fixed);
    }

    @Override
    public void renderTile(int xPos, int yPos, int zPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(this::tile_putPixel,
        //this::putPixel_Blend_Depth,
        xPos, yPos, zPos, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(Sprite.FLIP_NONE), null, wrapX, wrapY, blendingColor, scale, shearType, shear, fixed);
    }

    @Override
    public void renderTile_Ent(int xPos, int yPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(this::putPixel_Blend_Depth, xPos, yPos, 0, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(Sprite.FLIP_NONE), null, wrapX, wrapY, blendingColor, scale, shearType, shear, fixed);
    }

    @Override
    public void renderTile_Ent(int xPos, int yPos, int zPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(this::putPixel_Blend_Depth, xPos, yPos, zPos, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(Sprite.FLIP_NONE), null, wrapX, wrapY, blendingColor, scale, shearType, shear, fixed);
    }



    //@Override
    /*
    public void renderTile(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission, int wrapX, int wrapY, Vector4f blendingColor,
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(this::light_tile_putPixel, xPos, yPos, zPos, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(Sprite.FLIP_NONE), normalMap.getPixelGetter(Sprite.FLIP_NONE), wrapX, wrapY, blendingColor, scale, shearType, shear, fixed);
    }
    */
    
    @Override
    public void renderTile_Ent(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission, int wrapX, int wrapY, Vector4f blendingColor,
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(this::light_putPixel_Blend_Depth, xPos, yPos, zPos, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(Sprite.FLIP_NONE), normalMap.getPixelGetter(Sprite.FLIP_NONE), wrapX, wrapY, blendingColor, scale, shearType, shear, fixed);
    }


    
    @Override
    /**Adds a Shadow Volume to the Shadow Buffer.*/
    public void applyShadow(ShadowVolume shadow, float scale, boolean fixed)
    {
        // TODO Auto-generated method stub
    }
    public void applyShadow(ShadowSilhouette shadow, float scale, boolean fixed){}





    private static final Matrix4f TEMP_MATRIX = new Matrix4f(), TEMP_INVERT = new Matrix4f();
    private static final Vector4f
    TRANSFORM_VEC_0 = new Vector4f(),
    TRANSFORM_VEC_1 = new Vector4f(),
    TRANSFORM_VEC_2 = new Vector4f();

    /**
     * Software Screen specific function for rendering sprites using a matrix for any combination of affine transformations.
     * 
     * @param pixelPutter the PixelPutter function.
     * @param matrix the projective transformation matrix of the sprite.
     * @param sprite the sprite to render.
     * @param pixelGetter the function to uses for getting a pixel. Used for Sprite flipping.
     * @param blendingColor the color to blend the sprite's pixels with.
     * @param fixed determines if this sprite's position should be "fixed" with the screen's offsets.
     */
    private void renderSprite_Affine(PixelPutter pixelPutter, Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, int sprite_Width, int sprite_Height,
    Sprite.PixelGetter pixelGetter, Vector4f blendingColor, boolean fixed)
    {
        //TODO

        //Transform Upper-Left corner.
        TRANSFORM_VEC_0.set(0, 0, 1f);
        TRANSFORM_VEC_0.mul(matrix);

        float startX = TRANSFORM_VEC_0.x,
        startY = TRANSFORM_VEC_0.y,
        startZ = TRANSFORM_VEC_0.z,
        endX = startX,
        endY = startY,
        endZ = startZ;


        //Transform Lower-Right corner.
        TRANSFORM_VEC_2.set(sprite_Width, sprite_Height, 1f);
        TRANSFORM_VEC_2.mul(matrix);
        //System.out.println(TRANSFORM_VEC);

        startX = Math.min(startX, TRANSFORM_VEC_2.x);
        startY = Math.min(startY, TRANSFORM_VEC_2.y);
        startZ = Math.min(startZ, TRANSFORM_VEC_2.z);
        endX = Math.max(endX, TRANSFORM_VEC_2.x);
        endY = Math.max(endY, TRANSFORM_VEC_2.y);
        endZ = Math.max(endZ, TRANSFORM_VEC_2.z);


        //Transform Lower-Left corner.
        TRANSFORM_VEC_1.set(0, sprite_Height, 1f);
        TRANSFORM_VEC_1.mul(matrix);

        startX = Math.min(startX, TRANSFORM_VEC_1.x);
        startY = Math.min(startY, TRANSFORM_VEC_1.y);
        startZ = Math.min(startZ, TRANSFORM_VEC_1.z);
        endX = Math.max(endX, TRANSFORM_VEC_1.x);
        endY = Math.max(endY, TRANSFORM_VEC_1.y);
        endZ = Math.max(endZ, TRANSFORM_VEC_1.z);


        //Transform Upper-Right corner.
        TRANSFORM_VEC_1.set(sprite_Width, 0, 1f);
        TRANSFORM_VEC_1.mul(matrix);

        startX = Math.min(startX, TRANSFORM_VEC_1.x);
        startY = Math.min(startY, TRANSFORM_VEC_1.y);
        startZ = Math.min(startZ, TRANSFORM_VEC_1.z);
        endX = Math.max(endX, TRANSFORM_VEC_1.x);
        endY = Math.max(endY, TRANSFORM_VEC_1.y);
        endZ = Math.max(endZ, TRANSFORM_VEC_1.z);

        
        //float c0_x = TRANSFORM_VEC_1.x - TRANSFORM_VEC_0.x,
        //c0_y = TRANSFORM_VEC_1.y - TRANSFORM_VEC_0.y,
        //c0_z = TRANSFORM_VEC_1.z - TRANSFORM_VEC_0.z,
        //
        //c1_x = TRANSFORM_VEC_2.x - TRANSFORM_VEC_0.x,
        //c1_y = TRANSFORM_VEC_2.y - TRANSFORM_VEC_0.y,
        //c1_z = TRANSFORM_VEC_2.z - TRANSFORM_VEC_0.z;

        //float yNormal = (c0_z * c1_x) - (c0_x * c1_z);
        //float zNormal = (c0_x * c1_y) - (c0_y * c1_x);


        //So now, we've established a square-shaped area of the screen to check and fill in.
        //The inverse matrix is used to sample the sprite's pixels as we go.

        //Clamp Sprite bounds.
        //startY = (startY < CROP_Y0) ? CROP_Y0 : startY;
        //endY = (endY >= CROP_Y1) ? CROP_Y1 : endY;

        startX = (startX < CROP_X0) ? CROP_X0 : startX;
        endX = (endX >= CROP_X1) ? CROP_X1 : endX;


        //Start plasterin'.
        int z = 1;
        //for(int z = (int)startZ; z < endZ; z++)
        //{
        for(int y = (int)startY; y < endY; y++)
        {
            for(int x = (int)startX; x < endX; x++)
            {
                //Use the inverted matrix to get sprite pixel coordinates.
                TRANSFORM_VEC_0.set(x, y, z);
                TRANSFORM_VEC_0.mul(invertedMatrix);
                int sx = (int)(TRANSFORM_VEC_0.x),
                sy = (int)(TRANSFORM_VEC_0.y);

                if((y - (z/2)) < 0 || (y - (z/2)) >= HEIGHT){continue;}
                if(sx < 0 || sx > sprite_Width || sy < 0 || sy < sprite_Height){continue;}

                int currentZ = (int)TRANSFORM_VEC_0.z;

                //Run PixelPutter.
                int index = x + (y - (z/2)) * WIDTH;
                pixelPutter.invoke(pixelGetter, sx, sy, null, index, blendingColor, 0, 0, currentZ);
            }
        }
        //}

        /*
        float scale_width = (sprite_Width * matrix.m00());
        float scale_height = (sprite_Height * matrix.m11());

        float scanline_width = (scale_width + (scale_height * Math.abs(matrix.m10())));
        float scanline_height = (scale_height + (scale_width * Math.abs(matrix.m01())));

        int z = 1;
        for(int y = 0; y < scanline_height; y++)
        {
            float yr = (y / scanline_height) * sprite_Height;

            for(int x = 0; x < scanline_width; x++)
            {
                float xr = (x / scanline_width) * sprite_Width;

                //Use the inverted matrix to get sprite pixel coordinates.
                TRANSFORM_VEC_0.set(xr, yr, z);
                TRANSFORM_VEC_0.mul(matrix);
                int sx = (int)(TRANSFORM_VEC_0.x),
                sy = (int)(TRANSFORM_VEC_0.y - (TRANSFORM_VEC_0.z/2));

                if(sy < 0 || sy >= HEIGHT){continue;}
                //if(sx < 0 || sx > sprite_Width || sy < 0 || sy < sprite_Height){continue;}

                //Sample a pixel.
                //int pixel = sprite.getPixel(sx, sy);

                //Get Depth value.
                int currentZ = (int)TRANSFORM_VEC_0.z;

                //Run PixelPutter.
                int index = sx + sy * WIDTH;
                pixelPutter.invoke(pixelGetter, (int)xr, (int)yr, null, index, blendingColor, 0, 0, currentZ);
            }
        }
        */
    }

    /*
     * 
     * [xScale]  [xShear]  [xDrag]   [0]
     * [yShear]  [yScale]  [yDrag]   [0]
     * [zxShear] [zyShear] [zScale]  [0]
     * [xTransl] [yTransl] [zTransl] [1]
     */

    @Override
    /**Renders a sprite using any combination of affine transformations.*/
    public void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY,
    boolean fixed)
    {
        renderSprite_Affine(this::putPixel, matrix, invertedMatrix, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), null, fixed);
    }

    @Override
    /**Renders a sprite using any combination of affine transformations with a color to blend.*/
    public void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    boolean fixed)
    {
        renderSprite_Affine(this::putPixel_Blend, matrix, invertedMatrix, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), blendingColor, fixed);
    }

    @Override
    /**Renders a sprite using any combination of affine transformations with depth.*/
    public void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY,
    boolean fixed)
    {
        renderSprite_Affine(this::putPixel_Depth, matrix, invertedMatrix, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), null, fixed);
    }

    @Override
    /**Renders a sprite using any combination of affine transformations with a color to blend and depth.*/
    public void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    boolean fixed)
    {
        renderSprite_Affine(this::putPixel_Blend_Depth, matrix, invertedMatrix, sprite, sprite.getWidth(), sprite.getHeight(),
        sprite.getPixelGetter(flip), blendingColor, fixed);
    }


    private static final Matrix4f PROJ_TRANSFORMATION = new Matrix4f(),
    INVERTED = new Matrix4f();

    @Override
    public void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {
        //Cache sprite dimensions.
        int sprite_Width = sprite.getWidth(),
        sprite_Height = sprite.getHeight();

        //Matrix of projected coordinates.
        PROJ_TRANSFORMATION.set
        (
            x0,
            y0,
            0,
            1,
            //
            x1,
            y1,
            0,
            1,
            //
            x2,
            y2,
            0,
            1,
            //
            x3,
            y3,
            0,
            1
        );

        //Inverted matrix of Sprite corners.
        INVERTED.set
        (
            0,
            0,
            0,
            1,
            //
            sprite_Width,
            0,
            0,
            1,
            //
            sprite_Width,
            sprite_Height,
            0,
            1,
            //
            0,
            sprite_Height,
            0,
            1
        ).invert();

        //Multiply the two.
        PROJ_TRANSFORMATION.mul(INVERTED);

        //Invert the projection matrix and store the result into INVERTED (will be used for pixel sampling).
        PROJ_TRANSFORMATION.invert(INVERTED);

        //Render the result.
        renderSprite_Affine(this::putPixel, PROJ_TRANSFORMATION, INVERTED, sprite, sprite_Width, sprite_Height,
        sprite.getPixelGetter(flip), null, fixed);
    }

    @Override
    public void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {
        // TODO Auto-generated method stub
    }



    /*
     * Pixel Putter Functions
     */
    @FunctionalInterface
    private interface PixelPutter
    {
        public abstract void invoke(Sprite.PixelGetter colorGetter, int sX, int sY,
        Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ);
    }

    private void putPixel(Sprite.PixelGetter colorGetter, int sX, int sY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        int color = colorGetter.invoke(sX, sY);
        //
        if((color & 0xFF000000) == 0xFF000000)
        {
            //Put Opaque Pixel
            pixels[index] = color;
        }
        else if(color != 0x00000000)
        {
            //Put Translucent Pixel instead
            pixels[index] = translucentColor(color, pixels[index]);
        }
    }

    private void putPixel_Depth(Sprite.PixelGetter colorGetter, int sX, int sY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        if(posZ >= layers[index])
        {
            //Color.
            int color = colorGetter.invoke(sX, sY);

            if((color & 0xFF000000) == 0xFF000000)
            {
                //Put Opaque Pixel
                pixels[index] = color;
                layers[index] = posZ;
            }
            else if(color != 0x00000000)
            {
                //Put Translucent Pixel instead
                pixels[index] = translucentColor(color, pixels[index]);
                layers[index] = posZ;
            }
        }
        else if((pixels[index] & 0xFF000000) != 0x00000000)
        {
            pixels[index] = translucentColor(pixels[index],
            colorGetter.invoke(sX, sY));
        }
    }

    private void putPixel_Blend(Sprite.PixelGetter colorGetter, int sX, int sY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        int color = multipliedColor(colorGetter.invoke(sX, sY), blendingColor);
        //
        if((color & 0xFF000000) == 0xFF000000)
        {
            //Put Opaque Pixel
            pixels[index] = color;
        }
        else if(color != 0x00000000)
        {
            //Put Translucent Pixel instead
            pixels[index] = translucentColor(color, pixels[index]);
        }
    }

    private void putPixel_Blend_Depth(Sprite.PixelGetter colorGetter, int sX, int sY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        //Is this pixel in front of or above the existing layer values?
        if(posZ >= layers[index])
        {
            //Final Color.
            int color = multipliedColor(colorGetter.invoke(sX, sY), blendingColor);
            
            //Is the color opaque?
            if((color & 0xFF000000) == 0xFF000000)
            {
                //Put Opaque Pixel
                pixels[index] = color;
                layers[index] = posZ;
            }
            //Is the color not nothing?
            else if(color != 0x00000000)
            {
                //Put Translucent Pixel instead
                pixels[index] = translucentColor(color, pixels[index]);
                layers[index] = posZ;
            }
        }
        else if((((pixels[index] & 0xFF000000) >> 24) & 0xFF) != 0x00000000)
        {
            pixels[index] = translucentColor(pixels[index],
            multipliedColor(colorGetter.invoke(sX, sY), blendingColor));
        }
    }



    private void tile_putPixel(Sprite.PixelGetter colorGetter, int sX, int sY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        //If an opaque pixel is already present here, skip it.
        if((pixels[index] & 0xFF000000) != 0xFF000000)
        {
            //Final Color.
            int color = multipliedColor(colorGetter.invoke(sX, sY), blendingColor);

            if((color & 0xFF000000) == 0xFF000000)
            {
                //Put Opaque Pixel
                pixels[index] = color;
                layers[index] = posZ;
            }
            else if((color & 0xFF000000) != 0x00000000)
            {
                //Put Translucent Pixel instead
                pixels[index] = translucentColor(color, pixels[index]);
                layers[index] = posZ;
            }
        }
    }


    private final Vector3f CALC_RESULT = new Vector3f();

    private void light_putPixel_Blend_Depth(Sprite.PixelGetter colorGetter, int colX, int colY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        //Is this pixel in front of or above the existing layer values?
        if(posZ < layers[index]){return;}

        //Get Color.
        int color = multipliedColor(colorGetter.invoke(colX, colY), blendingColor);
        if((color & 0xFF000000) == 0x00000000){return;}
        Vector4f vecColor = intToVector4f(color);

        //Set position vector.
        CALC_FPOSITION.set(posX + xOffset, posY + yOffset, posZ + zOffset);
        CALC_RESULT.set(0.0f);

        //Get normal.
        int normal = normalGetter.invoke(colX, colY);
        Vector3f vecNormal = intToVector3f_Normal(normal);

        //Get cell.
        int x0 = index %  WIDTH, y0 = index / WIDTH;
        //
        int cellX = (x0 / CELL_SIZE);
	    int cellY = (y0 / CELL_SIZE);
        Cell cell = uCells[cellX + (cellY * (Game.BATTLE_WIDTH / CELL_SIZE))];

        //How many of each type of light are in this cell?
        int area_Lights = cell.lightType_Data & 0x0F;
        int directional_Lights = ((cell.lightType_Data & 0xF0) >> 4) & 0xFF;

        //Loop through area lights for cell.
        for(int i = 0; i < area_Lights; i++)
        {
            int lightID = cell.lightIDs[i] & 0xFF;
            AreaLight aLight = (AreaLight)uLights[lightID];

            //
            //-1: can be to the left to check
            //0: can be either
            //1: can be to the right to check
            //Set radius to 0 to not have an outer radius (...I'm such an IDIOT).
            //
            /*
            if((aLight.getUseX() == -1 && (posX > aPosition.x + aBox.getBaseWidth()))
            || (aLight.getUseX() == 1 && (posX < aPosition.x))
            //
            || (aLight.getUseY() == -1 && (posY > aPosition.y + aBox.getBaseHeight()))
            || (aLight.getUseY() == 1 && (posY < aPosition.y))
            //
            || (aLight.getUseZ() == -1 && (posZ > aPosition.z + aBox.getBaseDepth()))
            || (aLight.getUseZ() == 1 && (posZ < aPosition.z)))
            {continue;}
            */
            //
            calculate_Area_Light(vecColor, vecNormal, aLight, CALC_RESULT);
        }

        //Loop through directional lights for cell.
        for(int i = 0; i < directional_Lights; i++)
        {
            int lightID = cell.lightIDs[area_Lights + i] & 0xFF;
            DirectionalLight dLight = (DirectionalLight)uLights[lightID];
            //
            calculate_Directional_Light(posX, posY, posZ, vecColor, vecNormal, dLight);
        }

        //Convert back to int.
        //color = vector4fToInt(vecColor);
        if(CALC_RESULT.x > 1.0f){CALC_RESULT.x = 1.0f;}
        if(CALC_RESULT.y > 1.0f){CALC_RESULT.y = 1.0f;}
        if(CALC_RESULT.z > 1.0f){CALC_RESULT.z = 1.0f;}
        color = vector3f_a_ToInt(CALC_RESULT, vecColor.w);

        //Full opactity check.
        if((color & 0xFF000000) == 0xFF000000)
        {
            //Put Opaque Pixel
            pixels[index] = color;
            //pixels[index] = 0xFF000000 | (((cellX+1) * 40) << 16) | (((cellY+1) * 40) << 8);
            layers[index] = posZ;
        }
        else// if((color & 0xFF000000) != 0x00000000)
        {
            //Put Translucent Pixel instead
            pixels[index] = translucentColor(color, pixels[index]);
            layers[index] = posZ;
        }
    }


    private void light_tile_putPixel(Sprite.PixelGetter colorGetter, int colX, int colY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        //If an opaque pixel is already present here, skip it.
        if((pixels[index] & 0xFF000000) == 0xFF000000){return;}

        //Get Color.
        int color = multipliedColor(colorGetter.invoke(colX, colY), blendingColor);
        if((color & 0xFF000000) == 0x00000000){return;}
        Vector4f vecColor = intToVector4f(color);

        //Set position vector.
        CALC_FPOSITION.set(posX + xOffset, posY + yOffset, posZ + zOffset);
        CALC_RESULT.set(0.0f);

        //Get normal.
        int normal = normalGetter.invoke(colX, colY);
        Vector3f vecNormal = intToVector3f_Normal(normal);
        //System.out.println(vecNormal.x + " " + vecNormal.y + " " + vecNormal.z);

        //Get cell.
        int y0 = index / WIDTH, x0 = index % WIDTH;
        //
        int cellX = (x0 / CELL_SIZE);
	    int cellY = (y0 / CELL_SIZE);
        Cell cell = uCells[cellX + (cellY * (Game.BATTLE_WIDTH / CELL_SIZE))];

        //How many of each type of light are in this cell?
        int area_Lights = cell.lightType_Data & 0x0F;
        //if(area_Lights > 0){System.out.println(area_Lights);}
        int directional_Lights = ((cell.lightType_Data & 0xF0) >> 4) & 0xFF;

        //Loop through area lights for cell.
        for(int i = 0; i < area_Lights; i++)
        {
            int lightID = cell.lightIDs[i] & 0xFF;
            AreaLight aLight = (AreaLight)uLights[lightID];

            //
            //-1: can be to the left to check
            //0: can be either
            //1: can be to the right to check
            //Set radius to 0 to not have an outer radius (...I'm such an IDIOT).
            //
            /*
            if((aLight.getUseX() == -1 && (posX > aPosition.x + aBox.getBaseWidth()))
            || (aLight.getUseX() == 1 && (posX < aPosition.x))
            //
            || (aLight.getUseY() == -1 && (posY > aPosition.y + aBox.getBaseHeight()))
            || (aLight.getUseY() == 1 && (posY < aPosition.y))
            //
            || (aLight.getUseZ() == -1 && (posZ > aPosition.z + aBox.getBaseDepth()))
            || (aLight.getUseZ() == 1 && (posZ < aPosition.z)))
            {continue;}
            */
            //
            calculate_Area_Light(vecColor, vecNormal, aLight, CALC_RESULT);
        }

        //Loop through directional lights for cell.
        for(int i = 0; i < directional_Lights; i++)
        {
            int lightID = cell.lightIDs[area_Lights + i] & 0xFF;
            DirectionalLight dLight = (DirectionalLight)uLights[lightID];
            //
            calculate_Directional_Light(posX, posY, posZ, vecColor, vecNormal, dLight);
        }

        //Convert back to int.
        if(CALC_RESULT.x > 1.0f){CALC_RESULT.x = 1.0f;}
        if(CALC_RESULT.y > 1.0f){CALC_RESULT.y = 1.0f;}
        if(CALC_RESULT.z > 1.0f){CALC_RESULT.z = 1.0f;}
        color = vector3f_a_ToInt(CALC_RESULT, vecColor.w);
        
        //Full opactity check.
        if((color & 0xFF000000) == 0xFF000000)
        {
            //Put Opaque Pixel
            pixels[index] = color;
            //pixels[index] = 0xFF000000 | (((cellX+1) * 40) << 16) | (((cellY+1) * 40) << 8);
            layers[index] = posZ;
        }
        else// if((color & 0xFF000000) != 0x00000000)
        {
            //Put Translucent Pixel instead
            pixels[index] = translucentColor(color, pixels[index]);
            layers[index] = posZ;
        }
    }



    private void debug_putPixel(Sprite.PixelGetter colorGetter, int sX, int sY,
    Sprite.PixelGetter normalGetter, int index, Vector4f blendingColor, int posX, int posY, int posZ)
    {
        //Final Color.
        int color = ((int)(blendingColor.w * 255) << ALPHA_OFFSET)
        | ((int)(blendingColor.x * 255) << RED_OFFSET)
        | ((int)(blendingColor.y * 255) << GREEN_OFFSET)
        | (int)(blendingColor.z * 255);

        if((color & 0xFF000000) == 0xFF000000)
        {
            //Put Opaque Pixel
            pixels[index] = color;
            //layers[index] = layer;
        }
        else if(color != 0x00000000)
        {
            //Put Translucent Pixel instead
            pixels[index] = translucentColor(color, pixels[index]);
            //layers[index] = layer;
        }
    }



    /*
     * Debug Stuff
     */

    @Override
    /**Renders a Point.*/
    public void drawPoint(int xPos, int yPos, Vector4f pointColor, boolean fixed)
    {drawPoint(xPos, yPos, 0, pointColor, fixed);}
    
    @Override
    /**Renders a Point.*/
    public void drawPoint(int xPos, int yPos, int zPos, Vector4f pointColor, boolean fixed)
    {
        //Offset Positions by Camera
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        yPos -= (zPos/2);

        //Point's Color.
        int color = vector4fToInt(pointColor);

        //Screen Index.
        int x = -1, y = -1;

        for(byte i = 0; i < 5; i++)
        {
            switch(i)
            {
                case 0: x = xPos;   y = yPos-1; break;
                case 1: x = xPos-1; y = yPos; break;
                case 2: x = xPos;   y = yPos; break;
                case 3: x = xPos+1; y = yPos; break;
                case 4: x = xPos;   y = yPos+1; break;
            }

            if(x < CROP_X0 || x >= CROP_X1 || y < CROP_Y0 || y >= CROP_Y1){continue;}
            pixels[x + y * WIDTH] = color;
        }
    }

    
    @Override
    /**Renders a Line.*/
    public void drawLine(int x0, int y0, int x1, int y1, Vector4f vecColor, boolean fixed)
    {drawLine(x0, y0, 0, x1, y1, 0, vecColor, fixed);}

    @Override
    /**Renders a Line.*/
    public void drawLine(int x0, int y0, int z0, int x1, int y1, int z1, Vector4f vecColor, boolean fixed)
    {
        //Offset Positions by Camera
        if(fixed)
        {
            x0 -= xOffset; x1 -= xOffset;
            y0 -= yOffset; y1 -= yOffset;
            z0 -= zOffset; z1 -= zOffset;
        }
        y0 -= (z0 / 2);
        y1 -= (z1 / 2);

        //Line's Color.
        int color = vector4fToInt(vecColor);

        //Straight Vertical Lines (for undifined slopes).
        if(x0 == x1)
        {
            if(x0 < CROP_X0 || x0 >= CROP_X1){return;}

            int start = y0, end = y1;
            if(y0 > y1){start = y1; end = y0;}

            for(int y = start; y < end; y++)
            {
                //Avoid Out-Of-Bounds Exception.
                if(y < CROP_Y0 || y >= CROP_Y1){continue;}

                //Put the Pixel.
                pixels[x0 + y * WIDTH] = color;
            }
        }
        //Everything else.
        else
        {
            //Calculate the distance between the two points by using pythagorean theorm.
            int sideA = x1 - x0, sideB = y1 - y0,
            length = (int)Math.sqrt((sideA * sideA) + (sideB * sideB));
        
            //Slope (rise over run) and Y-Intersect for line.  
            float slope = (sideB / (float)sideA), yIntersect = (y0 - (slope * (float)x0));

            for(int i = 0; i < length; i++)
            {
                //How far are we on the line in percent?
                float f = (i / (float)length);
                
                //Calculate the x and y position.
                float x = Math.round(x0 + (f * sideA)),
                y = Math.round((slope * x) + yIntersect);

                //Avoid Out-Of-Bounds Exception.
                if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT){continue;}

                //Put the Pixel.
                pixels[(int)x + (int)y * WIDTH] = color;
            }
        }
    }

    @Override
    /**Draws a rectangle.*/
    public void drawRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed)
    {
        drawLine(xPos,      yPos,   xPos+w, yPos,   vecColor, fixed);
        drawLine(xPos+w,    yPos,   xPos+w, yPos+h, vecColor, fixed);
        drawLine(xPos+w,    yPos+h, xPos-1, yPos+h, vecColor, fixed);
        drawLine(xPos,      yPos+h, xPos,   yPos,   vecColor, fixed);
    }

    @Override
    /**Draws a rectangle.*/
    public void drawRect(int xPos, int yPos, int zPos, int w, int h, Vector4f vecColor, boolean fixed)
    {
        drawLine(xPos,      yPos,   zPos,   xPos+w, yPos,   zPos, vecColor, fixed);
        drawLine(xPos+w,    yPos,   zPos,   xPos+w, yPos+h, zPos, vecColor, fixed);
        drawLine(xPos+w,    yPos+h, zPos,   xPos-1,   yPos+h, zPos, vecColor, fixed);
        drawLine(xPos,      yPos+h, zPos,   xPos,   yPos,   zPos, vecColor, fixed);
    }


    @Override
    /**Draws a rectangle.*/
    public void drawCroppedRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed)
    {
        int
        left = (xPos < CROP_X0) ? CROP_X0 : xPos,
        right = (xPos+w > CROP_X1) ? CROP_X1 : xPos+w,
        up = (yPos < CROP_Y0) ? CROP_Y0 : yPos,
        down = (yPos+h > CROP_Y1) ? CROP_Y1 : yPos+h;

        if(yPos >= CROP_Y0){drawLine(left-1, yPos,      right, yPos, vecColor, fixed);}
        if(xPos <= CROP_X1){drawLine(xPos+w, up+1,      xPos+w, down, vecColor, fixed);}
        //
        if(yPos <= CROP_Y1){drawLine(left-1, yPos+h,    right,  yPos+h,  vecColor, fixed);}
        if(xPos >= CROP_X0){drawLine(xPos, up+1,        xPos, down,  vecColor, fixed);}
    }

    @Override
    /**Draws a rectangle.*/
    public void drawCroppedRect(int xPos, int yPos, int zPos, int w, int h, Vector4f vecColor, boolean fixed)
    {
        int
        left = (xPos < CROP_X0) ? CROP_X0 : xPos,
        right = (xPos+w > CROP_X1) ? CROP_X1 : xPos+w,
        up = (yPos < CROP_Y0) ? CROP_Y0 : yPos,
        down = (yPos+h > CROP_Y1) ? CROP_Y1 : yPos+h;

        if(yPos >= CROP_Y0){drawLine(left-1, yPos, zPos, right, yPos, zPos, vecColor, fixed);}
        if(xPos <= CROP_X1){drawLine(xPos+w, up+1, zPos, xPos+w, down, zPos, vecColor, fixed);}
        //
        if(yPos <= CROP_Y1){drawLine(left-1, yPos+h, zPos, right,  yPos+h, zPos, vecColor, fixed);}
        if(xPos >= CROP_X0){drawLine(xPos, up+1, zPos, xPos, down, zPos, vecColor, fixed);}
    }



    @Override
    /**Renders a rect with 4 points, two ints each.*/
    public void drawQuad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Vector4f vecColor, boolean fixed)
    {
        drawLine(x0, y0, 0, x1, y1, 0, vecColor, fixed);
        drawLine(x1, y1, 0, x2, y2, 0, vecColor, fixed);
        drawLine(x2, y2, 0, x3, y3, 0, vecColor, fixed);
        drawLine(x3, y3, 0, x0, y0, 0, vecColor, fixed);
    }

    @Override
    /**Renders a quad with 4 points, three ints each.*/
    public void drawQuad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Vector4f vecColor, boolean fixed)
    {
        drawLine(x0, y0, z0, x1, y1, z1, vecColor, fixed);
        drawLine(x1, y1, z1, x2, y2, z2, vecColor, fixed);
        drawLine(x2, y2, z2, x3, y3, z3, vecColor, fixed);
        drawLine(x3, y3, z3, x0, y0, z0, vecColor, fixed);
    }


    @Override
    public void fillRect(int xPos, int yPos, int w, int h, Vector4f vecColor,
    boolean fixed)
    {
        renderSprite(this::debug_putPixel, xPos, yPos, 0, 0, null, w, h,
        null, 0, 0, vecColor, fixed);
    }

    @Override
    /**Renders a filled rect to the screen.*/
    public void fillRect(int xPos, int yPos, int zPos, int depth, int w, int h, Vector4f vecColor,
    boolean fixed)
    {
        renderSprite(this::debug_putPixel, xPos, yPos, zPos, depth, null, w, h,
        null, 0, 0, vecColor, fixed);
    }


    @Override
    /**Renders a filled quad to the screen.*/
    public void fillQuad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, Vector4f vecColor, boolean fixed)
    {
        //TODO Auto-generated method stub
    }
    //
    public void fillQuad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, Vector4f vecColor, boolean fixed)
    {
        //TODO Auto-generated method stub
    }

    @Override
    /**Renders a Circle.*/
    public void drawCircle(int xPos, int yPos, float radius, int thickness, Vector4f vecColor, boolean fixed)
    {drawCircle(xPos, yPos, 0, radius, thickness, vecColor, fixed);}

    @Override
    /**Renders a Circle.*/
    public void drawCircle(int xPos, int yPos, int zPos, float radius, int thickness, Vector4f vecColor, boolean fixed)
    {
        //Offset Positions by Camera
        if(fixed)
        {
            xPos -= xOffset;
            yPos -= yOffset;
            zPos -= zOffset;
        }
        yPos -= (zPos / 2);

        //Circle's Color.
        int color = vector4fToInt(vecColor);

        //Circle's Diameter.
        int diameter = (int)(radius * 2);

        //Circumference of Circle, to determine how many pixels need to be drawn.
        int circumference = (int)(Math.PI * diameter);

        for(int i = 0; i < circumference; i++)
        {
            //Radian value to use for this pixel.
            float rads = (float)((i / (float)circumference) * TWO_PI);

            //Coordinates to put the pixel on.
            int x = (int)(Math.sin(rads) * radius) + xPos,
            y = (int)(Math.cos(rads) * radius) + yPos;

            //Avoid Out-Of-Bounds Exception.
            if(x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT){continue;}

            //Put the Pixel.
            pixels[x + y * WIDTH] = color;
        }
    }
}
