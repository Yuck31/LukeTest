package JettersR.UI.Visuals;
/**
 * Nine-slice, but the edges aand center stretch to dimensions.
 * 
 * Author: Luke Sullivan
 * Last Edit: 1/17/2023
 */
import org.joml.Vector4f;

import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;

public class Stretch_NineSliceBox extends NineSliceBox
{
    /**Constructor.*/
    public Stretch_NineSliceBox(int x, int y, int width, int height, Vector4f color, Sprite[] sprites)
    {
        super(x, y, width, height, color, sprites);
    }

    @Override
    public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
    {
        //Calculate offset position.
        int xa = (int)(this.position.x + xOffset), ya = (int)(this.position.y + yOffset);

        //Cache sprite dimensions.
        int spriteWidth = sprites[0].getWidth(), spriteHeight = sprites[0].getHeight();

        int w = width - (spriteWidth*2), h = height - (spriteHeight*2);

        int l = xa - (width/2), mX = l + spriteWidth, r = l + width - spriteWidth;
        int t = ya - (height/2), mY = t + spriteHeight, b = t + height - spriteHeight;

        //Top-Left
        screen.renderSprite(l, t, sprites[0], Sprite.FLIP_NONE, color,
        //cropX0, cropY0, cropX1, cropY1,
        false);

        //Stretch Top-Middle
        screen.renderSprite_St(mX, t, sprites[1], Sprite.FLIP_NONE, color,
        //cropX0, cropY0, cropX1, cropY1,
        w, spriteHeight,
        false);

        //Top-Right
        screen.renderSprite(r, t, sprites[0], Sprite.FLIP_X, color,
        //cropX0, cropY0, cropX1, cropY1,
        false);


        //Stretch Middle-Left
        screen.renderSprite_St(l, mY, sprites[2], Sprite.FLIP_NONE, color,
        //cropX0, cropY0, cropX1, cropY1,
        spriteWidth, h,
        false);

        //Scale Middle-Middle
        screen.renderSprite_St(mX, mY, sprites[3], Sprite.FLIP_NONE, color,
        //cropX0, cropY0, cropX1, cropY1,
        w, h,
        false);

        //Stretch Middle-Right
        screen.renderSprite_St(r, mY, sprites[2], Sprite.FLIP_X, color,
        //cropX0, cropY0, cropX1, cropY1,
        spriteWidth, h,
        false);


        //Bottom-Left
        screen.renderSprite(l, b, sprites[0], Sprite.FLIP_Y, color,
        //cropX0, cropY0, cropX1, cropY1,
        false);

        //Stretch Bottom-Middle
        screen.renderSprite_St(mX, b, sprites[1], Sprite.FLIP_Y, color,
        //cropX0, cropY0, cropX1, cropY1,
        w, spriteHeight,
        false);

        //Bottom-Right
        screen.renderSprite(r, b, sprites[0], Sprite.FLIP_XY, color,
        //cropX0, cropY0, cropX1, cropY1,
        false);
    }
}
