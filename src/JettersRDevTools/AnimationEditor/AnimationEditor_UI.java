package JettersRDevTools.AnimationEditor;
/**
 * Author: Luke Sullivan
 * Last Edit: 9/2/2023
 */
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

//import JettersR.Game;
import JettersR.Graphics.Font;
import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.SpriteSheet;
import JettersR.UI.Menus.Menu;
import JettersR.UI.Menus.MenuComponent;
import JettersR.UI.Menus.MenuChoices.Labeled_MenuChoice;
import JettersR.UI.Visuals.Rect_DialogueBox;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

import static JettersR.Util.Fixed.*;

public class AnimationEditor_UI extends Menu
{
    public static Vector4f color0 = new Vector4f(0.2f, 0.7f, 0.2f, 1.0f),
    color1 = new Vector4f(0.9f, 0.7f, 0.2f, 1.0f),
    color2 = new Vector4f(0.9f, 0.2f, 0.2f, 1.0f);

    /**Frame Column exclusive to this class.*/
    public class Frame_Column extends MenuComponent
    {
        public class Frame extends MenuComponent
        {
            private Frame_Column fc;
            private byte slot;
            //
            private Rect_DialogueBox db;

            /**Connstructor.*/
            public Frame(int x, int y, int width, int height, Frame_Column fc, byte slot)
            {
                super(x, y, new AAB_Box2D(width, height));
                this.fc = fc;
                this.slot = slot;
                //
                db = new Rect_DialogueBox(x, y, width, height, color0);
            }

            @Override
            public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
            {
                if(animEditor.spriteNum == fc.offset + slot)
                {
                    db.setColor(color2);
                    return true;
                }

                if(isCurrentChoice)
                {
                    db.setColor(color1);
                    //
                    if(animEditor.mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                    {animEditor.spriteNum = fc.offset + slot;}
                    //
                    else if(animEditor.mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT))
                    {animEditor.spriteNum = fc.offset + slot;}
                }
                else{db.setColor(color0);}
                return false;
            }

            @Override
            public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
            {
                db.render(screen, 0, 0);
                //shape.render(screen, 1f, position.x, position.y, false);
            }

            public void render(Screen screen, Sprite sprite)
            {
                //this.render(screen, position.x, position.y);
                db.render(screen, 0, 0);
                
                if(sprite.getHeight() > sprite.getWidth())
                {
                    screen.renderSprite_Sc((int)position.x - 24, (int)position.y - 24, sprite, Sprite.FLIP_NONE,
                    (shape.getWidth()-2) / (float)sprite.getHeight(), (shape.getHeight()-2) / (float)sprite.getHeight(), false);
                }
                else
                {
                    screen.renderSprite_Sc((int)position.x - 24, (int)position.y - 24, sprite, Sprite.FLIP_NONE,
                    (shape.getWidth()-2) / (float)sprite.getWidth(), (shape.getHeight()-2) / (float)sprite.getWidth(), false);
                }
            }
        }
        //
        //
        //
        private int offset = 0;
        private Frame[] frames = new Frame[7];

        /**Constructor.*/
        public Frame_Column(Menu menu, int x, int y, int width, int height)
        {
            super(x, y, new AAB_Box2D(width, height));
            //
            for(byte i = 0; i < frames.length; i++)
            {frames[i] = new Frame(3 + 24, 3 + (51*i) + 24, 48, 48, this, i);}
        }

        @Override
        public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
        {
            if(isCurrentChoice)
            {
                byte scroll = animEditor.mouse.getScroll();
                //
                if(animEditor.ints.size() > 7)
                {
                    if(scroll > 0){offset = (offset+1) % (animEditor.ints.size()-6);}
                    else if(scroll < 0){offset = ((offset-1) + animEditor.ints.size()-6) % (animEditor.ints.size()-6);}
                }
                else{offset = 0;}
            }
            //
            for(int i = 0; i < frames.length; i++)
            {
                Frame f = frames[i];
                boolean inter = f.intersects(0, 0, animEditor.mouse);//, false);
                f.update(0, 0, inter, inter);
            }
            //
            return false;
        }

        @Override
        public void render(Screen screen, float xScroll, float yScroll)//, int cropX0, int cropY0, int cropX1, int cropY1)
        {
            for(int i = 0; i < frames.length; i++)
            {
                Frame f = frames[i];
                //
                if(offset + f.slot >= animEditor.sprites.size()){return;}
                f.render(screen, animEditor.sprites.get(offset + f.slot));
            }
        }
    }
    //
    //
    //
    public static final SpriteSheet redRect = new SpriteSheet(6, 6, 0xFFFF0000);
    private class AnimFrame extends MenuComponent
    {
        private byte slot;

        private Vector4f color0 = new Vector4f(0.2f, 0.7f, 0.2f, 1.0f),
        color1 = new Vector4f(0.9f, 0.7f, 0.2f, 1.0f),
        color2 = new Vector4f(0.9f, 0.2f, 0.2f, 1.0f);
        //
        private Rect_DialogueBox db;

        /**Constructor.*/
        public AnimFrame(Menu menu, int x, int y, int width, int height, byte slot)
        {
            super(x, y, new AAB_Box2D(width, height));
            this.slot = slot;
            //
            db = new Rect_DialogueBox(x, y, width, height, color0);
        }

        @Override
        public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
        {
            if(animEditor.currentAnimFrame == animEditor.animOffset + slot)
            {
                db.setColor(color2);
                return true;
            }

            if(isCurrentChoice)
            {
                db.setColor(color1);
                //
                if(animEditor.mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                {animEditor.currentAnimFrame = animEditor.animOffset + slot;}
                //
                else if(animEditor.mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT))
                {animEditor.currentAnimFrame = animEditor.animOffset + slot;}
            }
            else{db.setColor(color0);}
            //
            return false;
        }

        private Vector4f
        w = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
        r = new Vector4f(1.0f, 0.5f, 0.5f, 1.0f),
        g = new Vector4f(0.5f, 1.0f, 0.5f, 1.0f),
        b = new Vector4f(0.5f, 0.5f, 1.0f, 1.0f);

        @Override
        public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
        {
            int xa = (int)(position.x + xOffset - 16),
            ya = (int)(position.y + yOffset - 16);
            //
            db.render(screen, 0, 0);
            //
            if(animEditor.animOffset + slot >= animEditor.animSprites.size()){return;}
            //
            Sprite sprite = animEditor.animSprites.get(animEditor.animOffset + slot);
            float rate = f_toFloat( animEditor.f_rates.get(animEditor.animOffset + slot) );
            short[] offset = animEditor.offsets.get(animEditor.animOffset + slot);
            boolean isLoopTo = (animEditor.animOffset + slot == animEditor.loopTo);
            short actionNumber = animEditor.actionNumbers.get(animEditor.animOffset + slot);

            //Sprite
            screen.renderSprite_Sc(xa, ya, sprite, Sprite.FLIP_NONE,
            //xa + cropX0, ya + cropY0, xa + cropX1, ya + cropY1,
            (shape.getWidth()-2) / (float)sprite.getWidth(), (shape.getHeight()-2) / (float)sprite.getHeight(),
            false);

            Font arial = animEditor.arial;

            //Frame Number
            arial.render(screen, xa + 33, ya + 3,
            Integer.toString(animEditor.animOffset + slot), w, false);

            //Rate
            arial.render(screen, xa + 73, ya + 3,
            Float.toString(rate), r, false);
            
            //Offset
            arial.render(screen, xa + 113, ya + 3,
            Short.toString(offset[0]) + ", " + Short.toString(offset[1]), g, false);

            //Action Number
            arial.render(screen, xa + 183, ya + 3,
            Short.toString(actionNumber) + ", ", b, false);

            //LoopTo
            if(isLoopTo)
            {
                screen.renderSheet(xa, ya, 0, redRect, false);
                //screen.fillRect(xa, ya, 6, 6, Screen.RED, cropX0, cropY0, cropX1, cropY1, false);
            }

            //shape.render(screen, actionNumber, position.x + xOffset, position.y + yOffset, isLoopTo);
        }
    }
    //
    //
    //
    private AnimationEditor animEditor;
    //
    private Rect_DialogueBox db0 = new Rect_DialogueBox
    (
        0+35, 0+180, 70, 360,
        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f)
    ),
    db1 = new Rect_DialogueBox
    (
        70+285, 0+32, 570, 64,
        new Vector4f(0.2f, 0.2f, 0.2f, 1.0f)
    );
    //
    private AnimFrame[] animFrames = new AnimFrame[8];

    /**Constructor.*/
    public AnimationEditor_UI(AnimationEditor animEditor)
    {
        super(0, 0, null);
        this.animEditor = animEditor;
        //
        menuComponents.add(new Frame_Column(this, 0, 0, 70, 360));
        //
        menuComponents.add(new Labeled_MenuChoice(73 + 60, 3 + 14, 120, 29, () ->
        {
            animEditor.previousState = AnimationEditor.STATE_EDIT_LAYOUT;
            animEditor.currentState = AnimationEditor.STATE_areYouSure_LAYOUT;
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "NEW LAYOUT", 1.0f));
        //
        menuComponents.add(new Labeled_MenuChoice(73 + 120 + 60, 3 + 14, 120, 29, () ->
        {
            animEditor.previousState = AnimationEditor.STATE_EDIT_LAYOUT;
            animEditor.load_Layout();
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "LOAD LAYOUT", 1.0f));
        //
        menuComponents.add(new Labeled_MenuChoice(73 + 120 + 60, 32 + 14, 120, 29, () ->
        {
            animEditor.previousState = AnimationEditor.STATE_EDIT_LAYOUT;
            animEditor.load_Sheet(false);
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "LOAD SHEET", 1.0f));
        //
        menuComponents.add(new Labeled_MenuChoice(73 + (120*2) + 60, 3 + 14, 120, 29, () ->
        {
            animEditor.saveLayout();
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "SAVE LAYOUT", 1.0f));
        //
        menuComponents.add(new Labeled_MenuChoice(73 + (120*3) + 60, 3 + 14, 120, 29, () ->
        {
            animEditor.previousState = AnimationEditor.STATE_EDIT_LAYOUT;
            animEditor.currentState = AnimationEditor.STATE_EDIT_ANIM;
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "ANIMATE", 1.0f));
        //
        //
        //
        menuComponents.add(new Labeled_MenuChoice(73 + 120 + 60, 3 + 14, 120, 29, () ->
        {
            animEditor.previousState = AnimationEditor.STATE_EDIT_ANIM;
            animEditor.load_Animation();
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "LOAD ANIM", 1.0f));
        //
        menuComponents.add(new Labeled_MenuChoice(73 + (120*2) + 60, 3 + 14, 120, 29, () ->
        {
            animEditor.saveAnimation();
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "SAVE ANIM", 1.0f));
        //
        menuComponents.add(new Labeled_MenuChoice(73 + (120*3) + 60, 3 + 14, 120, 29, () ->
        {
            animEditor.previousState = AnimationEditor.STATE_EDIT_ANIM;
            animEditor.currentState = AnimationEditor.STATE_EDIT_LAYOUT;
        },
        new Vector4f[]{new Vector4f(0.2f, 0.2f, 1.0f, 1.0f), new Vector4f(1.0f, 0.2f, 0.2f, 1.0f)},
        "LAYOUT", 1.0f));
        //
        //
        //
        for(byte i = 0; i < animFrames.length; i++)
        {
            animFrames[i] = new AnimFrame(this, 74 + 16, 74 + (i * 34) + 16, 32, 32, i);
        }
    }

    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        //boolean button = animEditor.mouse.buttonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        MenuComponent fc = menuComponents.get(0);
        boolean fc_inter = fc.intersects(0, 0, animEditor.mouse);//, false);
        fc.update(0, 0, fc_inter, fc_inter);

        //Layout
        if(animEditor.currentState == AnimationEditor.STATE_EDIT_LAYOUT)
        {
            for(int i = 1; i < 6; i++)
            {
                MenuComponent mc = menuComponents.get(i);
                boolean inter = mc.intersects(0, 0, animEditor.mouse);//, false);
                mc.update(0, 0, inter, inter);
            }
        }
        //Anim
        else if(animEditor.currentState == AnimationEditor.STATE_EDIT_ANIM)
        {
            for(int i = 6; i < menuComponents.size(); i++)
            {
                MenuComponent mc = menuComponents.get(i);
                boolean inter = mc.intersects(0, 0, animEditor.mouse);//, false);
                mc.update(0, 0, inter, inter);
            }
            //
            for(int i = 0; i < animFrames.length; i++)
            {
                AnimFrame a = animFrames[i];
                if(animEditor.animOffset + i >= animEditor.animSprites.size()){return false;}
                //
                boolean inter = a.intersects(0, 0, animEditor.mouse);//, false);
                a.update(0, 0, inter, inter);
            }
        }

        return false;
    }

    @Override
    public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
    {
        db0.render(screen, position.x + xOffset, position.y + yOffset);
        db1.render(screen, position.x + xOffset, position.y + yOffset);
        //
        menuComponents.get(0).render(screen, position.x + xOffset, position.y + yOffset);
        //
        if(animEditor.currentState == AnimationEditor.STATE_EDIT_LAYOUT)
        {
            for(int i = 1; i < 6; i++)
            {menuComponents.get(i).render(screen, position.x + xOffset, position.y + yOffset);}
        }
        //
        else if(animEditor.currentState == AnimationEditor.STATE_EDIT_ANIM)
        {
            for(int i = 6; i < menuComponents.size(); i++)
            {menuComponents.get(i).render(screen, position.x + xOffset, position.y + yOffset);}
            //
            for(int i = 0; i < animFrames.length; i++)
            {
                if(animEditor.animOffset + i >= animEditor.animSprites.size()){return;}
                //
                animFrames[i].render(screen, position.x + xOffset, position.y + yOffset);
            }
        }
    }
}
