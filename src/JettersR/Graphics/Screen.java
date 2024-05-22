package JettersR.Graphics;
/**
 * Screen object to be utilized by each Graphics API.
 * 
 * Author: Luke Sullivan
 * Last Edit: 12/24/2023
 */
import org.joml.Vector4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import JettersR.Entities.Components.Lights.Light;
import JettersR.Tiles.Graphics.TileMesh;

public abstract class Screen
{
    //Width and Height of in-game screen.
    protected int WIDTH, HEIGHT;

    //Dimensions of GL Viewport.
    protected int VIEWPORT_X, VIEWPORT_Y, VIEWPORT_WIDTH, VIEWPORT_HEIGHT;
    protected float VIEWPORT_WIDTH_RATIO = 1f, VIEWPORT_HEIGHT_RATIO = 1f;
    protected boolean maintainAspectRatio = true, fitToScreen = true;

    //Default Vector3f for color blending.
    public static final Vector4f DEFAULT_BLEND = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    //Color bit values.
    public static final int RED_PORTION = 0x00FF0000, RED_OFFSET = 16,
    GREEN_PORTION = 0x0000FF00, GREEN_OFFSET = 8,
    BLUE_PORTION = 0x000000FF, BLUE_OFFSET = 0,
    ALPHA_PORTION = 0xFF000000, ALPHA_OFFSET = 24;

    public abstract void setDimensions(int w, int h, boolean API_Initialized);
    public final void setDimensions(int w, int h){setDimensions(w, h, true);}
    public abstract void init();

    public final int getWidth(){return WIDTH;}
    public final int getHeight(){return HEIGHT;}

    public final void setViewportDimensions(int vX, int vY, int vWidth, int vHeight)
    {
        this.VIEWPORT_X = vX;
        this.VIEWPORT_Y = vY;
        this.VIEWPORT_WIDTH = vWidth;
        this.VIEWPORT_HEIGHT = vHeight;

        //Calculate window-space to screen-space ratio.
        this.VIEWPORT_WIDTH_RATIO = (WIDTH / (float)vWidth);
        this.VIEWPORT_HEIGHT_RATIO = (HEIGHT / (float)vHeight);
    }

    public final int getViewportX(){return VIEWPORT_X;}
    public final int getViewportY(){return VIEWPORT_Y;}
    //public final int getViewportWidth(){return VIEWPORT_WIDTH;}
    //public final int getViewportHeight(){return VIEWPORT_HEIGHT;}
    public final float getViewportWidthRatio(){return VIEWPORT_WIDTH_RATIO;}
    public final float getViewportHeightRatio(){return VIEWPORT_HEIGHT_RATIO;}

    public final boolean maintainAspectRatio(){return maintainAspectRatio;}
    public final void maintainAspectRatio(boolean m){this.maintainAspectRatio = m;}
    
    public final boolean fitToScreen(){return fitToScreen;}
    public final void fitToScreen(boolean f){this.fitToScreen = f;}

    public abstract void sync();
    public abstract void render(long windowAddress);

    public abstract void clear();

    /*
     * EVERYTHING below this threshold will be applying Sprites to the Pixel Array
     */

    //Camera Offsets
    protected int xOffset = 0, yOffset = 0, zOffset = 0;
    protected float scaleOffset = 1.0f;

    public final int getXOffset(){return xOffset;}
    public final int getYOffset(){return yOffset;}
    public final int getZOffset(){return zOffset;}
    public final float getScaleOffset(){return scaleOffset;}

    public final void setXOffset(int xOffset){this.xOffset = xOffset;}
    public final void setYOffset(int yOffset){this.yOffset = yOffset;}
    public final void setZOffset(int zOffset)
    {
        //this.zOffset = zOffset;
        this.zOffset = (zOffset % 2 != 0) ? zOffset-1 : zOffset;
    }
    public final void setScaleOffset(float scaleOffset){this.scaleOffset = scaleOffset;}
    public final void setCameraOffsets(int xOffset, int yOffset, int zOffset)
    {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        //this.zOffset = zOffset;
        this.zOffset = (zOffset % 2 != 0) ? zOffset-1 : zOffset;
    }
    public final void setCameraOffsets(int xOffset, int yOffset, int zOffset, float scaleOffset)
    {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        //this.zOffset = zOffset;
        this.zOffset = (zOffset % 2 != 0) ? zOffset-1 : zOffset;
        this.scaleOffset = scaleOffset;
    }

    //Light Constants.
    protected static final int
    MAX_LIGHTS_ON_SCREEN = 256,
    LIGHT_SIZE = 16,
    MAX_LIGHTS_PER_CELL = 15,
    CELL_SIZE = 16;
    

    /**Adds a (preferably on-screen) light to check for when rendering.*/
    public abstract void addLight(Light light, float scale);

    /**Sets the currently used Global Light.*/
    public abstract void setGlobalLight(Vector3f direction, Vector3f diffuse, Vector3f ambient);

    /**Resets renderBatches.*/
    public abstract void reset_RenderBatches();

    
    /**Sets current crop region.*/
    public abstract void setCropRegion(int cropX0, int cropY0, int cropX1, int cropY1);
    public abstract void resetCropRegion();


    /**
     * SpriteSheet
     */
    public abstract void renderSheet(int xPos, int yPos, int zPos, SpriteSheet sheet, boolean fixed);
    public abstract void renderSheet(int xPos, int yPos, int zPos, SpriteSheet sheet, Vector4f blendingColor, boolean fixed);
    

    /**
     * Sprite
     */
    //No depth, no blend.
    //No depth, blend.
    //Depth, no blend.
    //Depth, blend.
    public abstract void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed);
    public abstract void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed);
    public abstract void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed);
    public abstract void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed);
    //
    //Same functions but with default crop values.
    //public final void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed){renderSprite(xPos, yPos, sprite, flip, wrapX, wrapY, fixed);}
    //public final void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed){renderSprite(xPos, yPos, sprite, flip, wrapX, wrapY, blendingColor, fixed);}
    //public final void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed){renderSprite(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, fixed);}
    //public final void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed){renderSprite(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, blendingColor, fixed);}
    //
    //Same functions but with default wrap values.
    public final void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, boolean fixed){renderSprite(xPos, yPos, sprite, flip, 0, 0, fixed);}
    public final void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed){renderSprite(xPos, yPos, sprite, flip, 0, 0, blendingColor, fixed);}
    public final void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, boolean fixed){renderSprite(xPos, yPos, zPos, depth, sprite, flip, 0, 0, fixed);}
    public final void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed){renderSprite(xPos, yPos, zPos, depth, sprite, flip, 0, 0, blendingColor, fixed);}
    //
    //Same functions but with default wrap and crop values.
    //public final void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, boolean fixed){renderSprite(xPos, yPos, sprite, flip, 0, 0, fixed);}
    //public final void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed){renderSprite(xPos, yPos, sprite, flip, 0, 0, blendingColor, fixed);}
    //public final void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, boolean fixed){renderSprite(xPos, yPos, zPos, depth, sprite, flip, 0, 0, fixed);}
    //public final void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed){renderSprite(xPos, yPos, zPos, depth, sprite, flip, 0, 0, blendingColor, fixed);}


    /**
     * Scale
     */
    public abstract void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed);
    public abstract void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed);
    public abstract void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed);
    public abstract void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed);
    //
    //Default crop.
    //public final void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, sprite, flip, wrapX, wrapY, xScale, yScale, fixed);}
    //public final void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, sprite, flip, wrapX, wrapY, blendingColor, xScale, yScale, fixed);}
    //public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, xScale, yScale, fixed);}
    //public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, blendingColor, xScale, yScale, fixed);}
    //
    //Default wrap.
    public final void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, sprite, flip, 0, 0, xScale, yScale, fixed);}
    public final void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, sprite, flip, 0, 0, blendingColor, xScale, yScale, fixed);}
    public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, 0, 0, xScale, yScale, fixed);}
    public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Vector4f blendingColor, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, 0, 0, blendingColor, xScale, yScale, fixed);}
    //
    //Default wrap and crop.
    //public final void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, sprite, flip, 0, 0, xScale, yScale, fixed);}
    //public final void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, sprite, flip, 0, 0, blendingColor, xScale, yScale, fixed);}
    //public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, 0, 0, xScale, yScale, fixed);}
    //public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Vector4f blendingColor, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, 0, 0, blendingColor, xScale, yScale, fixed);}
    //
    //Lighting
    public abstract void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, int wrapX, int wrapY, float xScale, float yScale, boolean fixed);
    public abstract void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed);
    public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, normalMap, emission, 0, 0, xScale, yScale, fixed);}
    public final void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, Vector4f blendingColor, float xScale, float yScale, boolean fixed){renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, normalMap, emission, 0, 0, blendingColor, xScale, yScale, fixed);}

    /**
     * Stretch (scaled sprite but it takes dimensions as parameters instead of scales)
     */
    public abstract void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, int resultWidth, int resultHeight, boolean fixed);
    public abstract void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed);
    public abstract void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, int resultWidth, int resultHeight, boolean fixed);
    public abstract void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed);
    //
    //Default crop.
    //public final void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, resultWidth, resultHeight, fixed);}
    //public final void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, blendingColor, resultWidth, resultHeight, fixed);}
    //public final void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, resultWidth, resultHeight, fixed);}
    //public final void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, blendingColor, resultWidth, resultHeight, fixed);}
    //
    //Default wrap.
    public final void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, 0, 0, sprite, flip, 0, 0, resultWidth, resultHeight, fixed);}
    public final void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, 0, 0, sprite, flip, 0, 0, blendingColor, resultWidth, resultHeight, fixed);}
    public final void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, zPos, depth, sprite, flip, 0, 0, resultWidth, resultHeight, fixed);}
    public final void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, zPos, depth, sprite, flip, 0, 0, blendingColor, resultWidth, resultHeight, fixed);}
    //
    //Default wrap and crop.
    //public final void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, 0, 0, sprite, flip, 0, 0, resultWidth, resultHeight, fixed);}
    //public final void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, 0, 0, sprite, flip, 0, 0, blendingColor, resultWidth, resultHeight, fixed);}
    //public final void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, zPos, depth, sprite, flip, 0, 0, resultWidth, resultHeight, fixed);}
    //public final void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed){renderSprite_St(xPos, yPos, zPos, depth, sprite, flip, 0, 0, blendingColor, resultWidth, resultHeight, fixed);}


    /**
     * Shear
     */
    public abstract void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xShear, float yShear, boolean fixed);
    public abstract void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xShear, float yShear, boolean fixed);
    public abstract void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xShear, float yShear, float zxShear, float zyShear, boolean fixed);
    public abstract void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xShear, float yShear, float zxShear, float zyShear, boolean fixed);
    //
    //Default Crop.
    //public final void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xShear, float yShear, boolean fixed){renderSprite_Sh(xPos, yPos, sprite, flip, wrapX, wrapY, xShear, yShear, fixed);}
    //public final void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xShear, float yShear, boolean fixed){renderSprite_Sh(xPos, yPos, sprite, flip, wrapX, wrapY, blendingColor, xShear, yShear, fixed);}
    //public final void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xShear, float yShear, float zxShear, float zyShear, boolean fixed){renderSprite_Sh(xPos, yPos, zPos, sprite, flip, wrapX, wrapY, xShear, yShear, zxShear, zyShear, fixed);}
    //public final void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xShear, float yShear, float zxShear, float zyShear, boolean fixed){renderSprite_Sh(xPos, yPos, zPos, sprite, flip, wrapX, wrapY, blendingColor, xShear, yShear, zxShear, zyShear, fixed);}
    //
    //Default Wrap.
    public final void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, float xShear, float yShear, boolean fixed){renderSprite_Sh(xPos, yPos, sprite, flip, 0, 0, xShear, yShear, fixed);}
    public final void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, float xShear, float yShear, boolean fixed){renderSprite_Sh(xPos, yPos, sprite, flip, 0, 0, blendingColor, xShear, yShear, fixed);}
    public final void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, float xShear, float yShear, float zxShear, float zyShear, boolean fixed){renderSprite_Sh(xPos, yPos, zPos, sprite, flip, 0, 0, xShear, yShear, zxShear, zyShear, fixed);}
    public final void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, Vector4f blendingColor, float xShear, float yShear, float zxShear, float zyShear, boolean fixed){renderSprite_Sh(xPos, yPos, zPos, sprite, flip, 0, 0, blendingColor, xShear, yShear, zxShear, zyShear, fixed);}
    //
    //Default Wrap and Crop.
    //public final void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, float xShear, float yShear, boolean fixed){renderSprite_Sh(xPos, yPos, sprite, flip, 0, 0, xShear, yShear, fixed);}
    //public final void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, float xShear, float yShear, boolean fixed){renderSprite_Sh(xPos, yPos, sprite, flip, 0, 0, blendingColor, xShear, yShear, fixed);}
    //public final void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, float xShear, float yShear, float zxShear, float zyShear, boolean fixed){renderSprite_Sh(xPos, yPos, zPos, sprite, flip, 0, 0, xShear, yShear, zxShear, zyShear, fixed);}
    //public final void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, Vector4f blendingColor, float xShear, float yShear, float zxShear, float zyShear, boolean fixed){renderSprite_Sh(xPos, yPos, zPos, sprite, flip, 0, 0, blendingColor, xShear, yShear, zxShear, zyShear, fixed);}


    /**
     * Rotate
     */
    public abstract void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float rads, int originX, int originY, boolean fixed);
    public abstract void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed);
    public abstract void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, float rads, int originX, int originY, boolean fixed);
    public abstract void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed);
    //
    //Default Crop.
    //public final void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, sprite, flip, wrapX, wrapY, rads, originX, originY, fixed);}
    //public final void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, sprite, flip, wrapX, wrapY, blendingColor, rads, originX, originY, fixed);}
    //public final void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, rads, originX, originY, fixed);}
    //public final void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, blendingColor, rads, originX, originY, fixed);}
    //
    //Default Wrap and Crop.
    public final void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, sprite, flip, 0, 0, rads, originX, originY, fixed);}
    public final void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, sprite, flip, 0, 0, blendingColor, rads, originX, originY, fixed);}
    public final void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, zPos, depth, sprite, flip, 0, 0, rads, originX, originY, fixed);}
    public final void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed){renderSprite_Ro(xPos, yPos, zPos, depth, sprite, flip, 0, 0, blendingColor, rads, originX, originY, fixed);}
    

    
    /**
     * Tile-Specific (Renders a Sprite without overriding pixels already on the screen).
     */
    public abstract void renderTile(int xPos, int yPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, float scale, byte shearType, float shear, boolean fixed);
    public abstract void renderTile(int xPos, int yPos, int zPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, float scale, byte shearType, float shear, boolean fixed);
    public abstract void renderTile_Ent(int xPos, int yPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, float scale, byte shearType, float shear, boolean fixed);
    public abstract void renderTile_Ent(int xPos, int yPos, int zPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, float scale, byte shearType, float shear, boolean fixed);


    /*
    //Functional Interface for basic Tile functions.
    @FunctionalInterface
    public interface Basic_TileRenderFunction
    {
        public abstract void invoke(int xPos, int yPos, int zPos, Sprite sprite,
        byte flip, int wrapX, int wrapY, Vector4f blendingColor, float scale, float shear, boolean fixed);
    }

    public final Basic_TileRenderFunction[] basic_TileRenderFunctions = new Basic_TileRenderFunction[TileSprite.MAX_SHEARTYPES];
    */


    //Functional Interface for lighting Tile functions.
    @FunctionalInterface
    public interface Lighting_TileRenderFunction
    {
        public abstract void invoke(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission,
        int wrapX, int wrapY, Vector4f blendingColor, float scale, float shear, boolean fixed);
    }

    public final Lighting_TileRenderFunction[] lighting_TileRenderFunctions = new Lighting_TileRenderFunction[TileMesh.MAX_SHEARTYPES];

    //Lighting.
    //@Deprecated
    //public abstract void renderTile(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission, int wrapX, int wrapY, Vector4f blendingColor, float scale, byte shearType, float shear, boolean fixed);
    public abstract void renderTile_Ent(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission, int wrapX, int wrapY, Vector4f blendingColor, float scale, byte shearType, float shear, boolean fixed);


    /**
     * Shadow-Specific.
     */
    public abstract void applyShadow(ShadowVolume shadow, float scale, boolean fixed);
    public abstract void applyShadow(ShadowSilhouette shadow, float scale, boolean fixed);
    

    /**
     * Renders a sprite using any combination of affine transformations via a Matrix4f.
     */
    public abstract void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed);
    public abstract void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed);
    public abstract void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed);
    public abstract void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed);
    //
    //
    //public final void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed){renderSprite_Affine_2D(matrix, invertedMatrix, sprite, flip, wrapX, wrapY, fixed);}
    //public final void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed){renderSprite_Affine_2D(matrix, invertedMatrix, sprite, flip, wrapX, wrapY, blendingColor, fixed);}
    //public final void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed){renderSprite_Affine(matrix, invertedMatrix, sprite, flip, wrapX, wrapY, fixed);}
    //public final void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed){renderSprite_Affine(matrix, invertedMatrix, sprite, flip, wrapX, wrapY, blendingColor, fixed);}
    //
    //
    public final void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, boolean fixed){renderSprite_Affine_2D(matrix, invertedMatrix, sprite, flip, 0, 0, fixed);}
    public final void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed){renderSprite_Affine_2D(matrix, invertedMatrix, sprite, flip, 0, 0, blendingColor, fixed);}
    public final void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, boolean fixed){renderSprite_Affine(matrix, invertedMatrix, sprite, flip, 0, 0, fixed);}
    public final void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed){renderSprite_Affine(matrix, invertedMatrix, sprite, flip, 0, 0, blendingColor, fixed);}
    

    /**Renders a sprite using 4 points.*/
    public abstract void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed);
    //
    public abstract void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed);
    //
    public abstract void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed);
    //
    public abstract void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed);
    //
    //
    public final void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, Sprite sprite, byte flip, boolean fixed)
    {renderSprite_Quad(x0, y0, x1, y1, x2, y2, x3, y3, sprite, flip, 0, 0, fixed);}
    //
    public final void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed)
    {renderSprite_Quad(x0, y0, x1, y1, x2, y2, x3, y3, sprite, flip, 0, 0, blendingColor, fixed);}
    //
    public final void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, Sprite sprite, byte flip, boolean fixed)
    {renderSprite_Quad(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, sprite, flip, 0, 0, fixed);}
    //
    public final void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, Sprite sprite, byte flip, Vector4f blendingColor, boolean fixed)
    {renderSprite_Quad(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, sprite, flip, 0, 0, blendingColor, fixed);}


    /*
     * Debug related Functions.
     */

    /**Draws a Point.*/
    public abstract void drawPoint(int xPos, int yPos, Vector4f pointColor, boolean fixed);
    public abstract void drawPoint(int xPos, int yPos, int zPos, Vector4f pointColor, boolean fixed);
    //
    //public final void drawPoint(int xPos, int yPos, Vector4f pointColor, boolean fixed){drawPoint(xPos, yPos, pointColor, fixed);}
    //public final void drawPoint(int xPos, int yPos, int zPos, Vector4f pointColor, boolean fixed){drawPoint(xPos, yPos, zPos, pointColor, fixed);}


    /**Draws a Line.*/
    public abstract void drawLine(int x0, int y0, int x1, int y1, Vector4f color, boolean fixed);
    public abstract void drawLine(int x0, int y0, int z0, int x1, int y1, int z1, Vector4f color, boolean fixed);
    //
    //public final void drawLine(int x0, int y0, int x1, int y1, Vector4f color, boolean fixed){drawLine(x0, y0, x1, y1, color, fixed);}
    //public final void drawLine(int x0, int y0, int z0, int x1, int y1, int z1, Vector4f color, boolean fixed){drawLine(x0, y0, z0, x1, y1, z1, color, fixed);}


    /**Draws a Rectangle.*/
    public abstract void drawRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed);
    public abstract void drawRect(int xPos, int yPos, int zPos, int w, int h, Vector4f vecColor, boolean fixed);
    //
    public abstract void drawCroppedRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed);
    public abstract void drawCroppedRect(int xPos, int yPos, int zPos, int w, int h, Vector4f vecColor, boolean fixed);


    /**Renders a filled Rectangle.*/
    public abstract void fillRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed);
    public abstract void fillRect(int xPos, int yPos, int zPos, int depth, int w, int h, Vector4f vecColor, boolean fixed);
    //
    //public final void fillRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed){fillRect(xPos, yPos, w, h, vecColor, fixed);}
    //public final void fillRect(int xPos, int yPos, int zPos, int depth, int w, int h, Vector4f vecColor, boolean fixed){fillRect(xPos, yPos, zPos, depth, w, h, vecColor, fixed);}
    

    /**Draws a Quad.*/
    public abstract void drawQuad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, Vector4f vecColor, boolean fixed);
    public abstract void drawQuad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, Vector4f vecColor, boolean fixed);


    /**Renders a filled Quad.*/
    public abstract void fillQuad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, Vector4f vecColor, boolean fixed);
    public abstract void fillQuad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, Vector4f vecColor, boolean fixed);
    //
    //public final void fillQuad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, Vector4f vecColor, boolean fixed){fillQuad(x0, y0, x1, y1, x2, y2, x3, y3, vecColor, fixed);}
    //public final void fillQuad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, Vector4f vecColor, boolean fixed){fillQuad(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, vecColor, fixed);}

    //Constant used mostly for circle-related stuff.
    public static final double TWO_PI = 2 * Math.PI;


    /**Renders a Circle.*/
    public abstract void drawCircle(int xPos, int yPos, float radius, int thickness, Vector4f vecColor, boolean fixed);
    public abstract void drawCircle(int xPos, int yPos, int zPos, float radius, int thickness, Vector4f vecColor, boolean fixed);
    //
    //public final void drawCircle(int xPos, int yPos, float radius, int thickness, Vector4f vecColor, boolean fixed){drawCircle(xPos, yPos, radius, thickness, vecColor, fixed);}
    //public final void drawCircle(int xPos, int yPos, int zPos, float radius, int thickness, Vector4f vecColor, boolean fixed){drawCircle(xPos, yPos, zPos, radius, thickness, vecColor, fixed);}
   


    /*
     * Calculation Stuff
     */

    /**Converts a Vector4f color to an int color.*/
    public static int vector4fToInt(Vector4f vec)
    {
        return ((int)(vec.x * 255) << RED_OFFSET)
        | ((int)(vec.y * 255) << GREEN_OFFSET)
        | ((int)(vec.z * 255) << BLUE_OFFSET)
        | ((int)(vec.w * 255) << ALPHA_OFFSET);
    }

    /**Converts a Vector4f color to an int color.*/
    public static int vector3f_a_ToInt(Vector3f vec, float alpha)
    {
        return ((int)(vec.x * 255) << RED_OFFSET)
        | ((int)(vec.y * 255) << GREEN_OFFSET)
        | ((int)(vec.z * 255) << BLUE_OFFSET)
        | ((int)(alpha * 255) << ALPHA_OFFSET);
    }

    /**Converts an int normal to a Vector3f normal.*/
    public static Vector3f intToVector3f_Normal(int i)
    {
        return new Vector3f
        (
            ((((i & 0x00FF0000) >> RED_OFFSET) / 255.0f) * 2.0f) - 1.0f,
            ((((i & 0x0000FF00) >> GREEN_OFFSET) / 255.0f) * 2.0f) - 1.0f,
            ((((i & 0x000000FF) >> BLUE_OFFSET) / 255.0f) * 2.0f) - 1.0f
        );
    }

    /**Converts an int color to a Vector4f color.*/
    public static Vector4f intToVector4f(int i)
    {
        return new Vector4f
        (
            ((i & 0x00FF0000) >> RED_OFFSET) / 255.0f,
            ((i & 0x0000FF00) >> GREEN_OFFSET) / 255.0f,
            ((i & 0x000000FF) >> BLUE_OFFSET) / 255.0f,
            (((i & 0xFF000000) >> ALPHA_OFFSET) & 0xFF) / 255.0f
        );
    }

    /**Converts two shorts to a Vector4f color.*/
    public static Vector4f shortsToVector(short s0, short s1)
    {
        return new Vector4f
        (
            (s0 & 0x00FF) / 255.0f,
            (((s1 & 0xFF00) >> 8) & 0xFF) / 255.0f,
            (s1 & 0x00FF) / 255.0f,
            (((s0 & 0xFF00) >> 8) & 0xFF) / 255.0f
        );
    }

    public static void setShortsToVector(short s0, short s1, Vector4f dest)
    {
        dest.set
        (
            (s0 & 0x00FF) / 255.0f,
            (((s1 & 0xFF00) >> 8) & 0xFF) / 255.0f,
            (s1 & 0x00FF) / 255.0f,
            (((s0 & 0xFF00) >> 8) & 0xFF) / 255.0f
        );
    }

    /**Converts 4 bytes to a Vector4f color.*/
    public static Vector4f bytesToVector4f(byte[] b)
    {
        return new Vector4f
        (
            (b[1] & 0xFF) / 255.0f,
            (b[2] & 0xFF) / 255.0f,
            (b[3] & 0xFF) / 255.0f,
            (b[0] & 0xFF) / 255.0f
        );
    }

    /**Converts 3 bytes to a Vector3f color.*/
    public static Vector3f bytesToVector3f(byte[] b)
    {
        return new Vector3f
        (
            (b[0] & 0xFF) / 255.0f,
            (b[1] & 0xFF) / 255.0f,
            (b[2] & 0xFF) / 255.0f
        );
    }

    /**Converts 4 bytes to a Vector4f color.*/
    public static Vector4f bytesToVector4f(byte bA, byte bR, byte bG, byte bB)
    {
        return new Vector4f
        (
            (bR & 0xFF) / 255.0f,
            (bG & 0xFF) / 255.0f,
            (bB & 0xFF) / 255.0f,
            (bA & 0xFF) / 255.0f
        );
    }

    /**Converts 3 bytes to an opaque Vector4f color.*/
    public static Vector4f bytesToVector4f(byte bR, byte bG, byte bB)
    {
        return new Vector4f
        (
            (bR & 0xFF) / 255.0f,
            (bG & 0xFF) / 255.0f,
            (bB & 0xFF) / 255.0f,
            1.0f
        );
    }

    /**Converts 4 bytes to a Vector4f color.*/
    public static byte[] vector4fToBytes(Vector4f vector)
    {
        return new byte[]
        {
            (byte)(vector.w * 255.0f),
            (byte)(vector.x * 255.0f),
            (byte)(vector.y * 255.0f),
            (byte)(vector.z * 255.0f)
        };
    }

    /**Calculates the result of blending a color with a Vector3f blending color (0.0f to 1.0f for R, G, and B).*/
    public static int multipliedColor(int color, Vector4f blendingColor)
    {
        //If BlendingColor is opaque white, return the given color.
        return (blendingColor.x >= 1.0f && blendingColor.y >= 1.0f
        && blendingColor.z >= 1.0f && blendingColor.w >= 1.0f) ? color
        
        //Otherwise, blend it.
        : ((int)((((color & ALPHA_PORTION) >> ALPHA_OFFSET) & 0xFF) * blendingColor.w) << ALPHA_OFFSET) |
        ((int)(((color & RED_PORTION) >> RED_OFFSET) * blendingColor.x) << RED_OFFSET) |
        ((int)(((color & GREEN_PORTION) >> GREEN_OFFSET) * blendingColor.y) << GREEN_OFFSET) |
        ((int)(((color & BLUE_PORTION) >> BLUE_OFFSET) * blendingColor.z) << BLUE_OFFSET);
    }

    /**Calculates the result of transluceny of two given colors.*/
    public static int translucentColor(int topColor, int bottomColor)
    {
        //Opacque or transparent check.
        int topColor_Alpha = (topColor & ALPHA_PORTION);
        if(topColor_Alpha == 0x00000000){return bottomColor;}
        else if(topColor_Alpha == ALPHA_PORTION){return topColor;}

        int alpha = (topColor_Alpha >> ALPHA_OFFSET) & 0xff;

        //Takes the alpha of the Top Color into 0.0f to 1.0f form.
        float opacity = (float)(alpha) / 255.0f,
        deltaOpacity = (1f-opacity);

        //
        //The formula here is:
        //(topColor * alpha) + (bottomColor * 1-alpha)
        //

        //A hexadecimal digit = 4 bits
        //This extracts the reds, greens, and blues of color0
        //and shifts the bits to make them easily editable.
        int
        red0   = (int)(((topColor & RED_PORTION) >> RED_OFFSET) * opacity),
        green0 = (int)(((topColor & GREEN_PORTION) >> GREEN_OFFSET) * opacity),
        blue0  = (int)(((topColor & BLUE_PORTION) >> BLUE_OFFSET) * opacity);
        //They are also multiplied by the given opacity

        //Then it extracts the red, green, and blue values from color1...
        int
        red1   = (int)(((bottomColor & RED_PORTION) >> RED_OFFSET) * deltaOpacity),
        green1 = (int)(((bottomColor & GREEN_PORTION) >> GREEN_OFFSET) * deltaOpacity),
        blue1  = (int)(((bottomColor & BLUE_PORTION) >> BLUE_OFFSET) * deltaOpacity);
        //1-Opacity is used to get the delta of max opacity and the given opacity.

        //Add the results together.
        red1 += red0; green1 += green0; blue1 += blue0;

        //Limit values of the colors.
        red1 = (red1 > 0xFF) ? 0xFF : (red1 < 0x00) ? 0x00 : red1;
        //
        green1 = (green1 > 0xFF) ? 0xFF : (green1 < 0x00) ? 0x00 : green1;
        //
        blue1 = (blue1 > 0xFF) ? 0xFF : (blue1 < 0x00) ? 0x00 : blue1;

        //The final result.
        return (alpha << ALPHA_OFFSET) | (red1 << RED_OFFSET) | (green1 << GREEN_OFFSET) | (blue1 << BLUE_OFFSET);
        //This took some experimentation in piskel to figure this out by the way.
    }

    /*
    public static float[] forward(Matrix4f matrix, float x, float y)
    {
        return new float[]
        {
            (x * matrix.m00()) + (y * matrix.m10()) + matrix.m20(),
            (x * matrix.m01()) + (y * matrix.m11()) + matrix.m21()
        };
    }
    */
    
    public static void forward(Matrix4f matrix, Vector4f vec)
    {
        float oldX = vec.x, oldY =  vec.y;
        //
        vec.set
        (
            (oldX * matrix.m00()) + (oldY * matrix.m10()) + matrix.m20(),
            (oldX * matrix.m01()) + (oldY * matrix.m11()) + matrix.m21(),
            0,
            0
        );

        //x = (mat.m00() * lx) + (mat.m10() * ly) + (mat.m20() * lz);
        //y = (mat.m01() * lx) + (mat.m11() * ly) + (mat.m21() * lz);
        //z = (mat.m02() * lx) + (mat.m12() * ly) + (mat.m22() * lz);

        //{
            //Tan.x, BiT.x, Nor.x,
            //Tan.y, BiT.y, Nor.y,
            //Tan.z, BiT.z, Nor.z,
        //}

        //x = (Tan.x * lx) + (BiT.x * ly) + (Nor.x * lz);
        //y = (Tan.y * lx) + (BiT.y * ly) + (Nor.y * lz);
        //z = (Tan.z * lx) + (BiT.z * ly) + (Nor.z * lz);

        //Tan = (Nor.y, -Nor.x, Nor.z)
        //BiT = (Nor.z, Nor.x, -Nor.y)
        ////BiT = (Tan.z, Tan.x, Tan.y)

        //x = (Nor.y * inputX) + (Nor.z * inputY) + (Nor.x * inputZ);
        //y = (-Nor.x * inputX) + (Nor.y * inputY) + (Nor.y * inputZ);
        //z = (Nor.z * inputX) + (-Nor.x * inputY) + (Nor.z * inputZ);
    }
}
