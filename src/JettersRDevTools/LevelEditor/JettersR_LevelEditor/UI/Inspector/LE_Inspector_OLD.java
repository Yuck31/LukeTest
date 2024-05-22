package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI.Inspector;
/**
 * Author: Luke Sullivan
 * Last Edit: 7/20/2023
 */
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

import JettersR.Controller;
import JettersR.Entities.Entity;
import JettersR.Graphics.Font;
import JettersR.Graphics.Fonts;
import JettersR.Graphics.Screen;
import JettersR.UI.Menus.Menu;
import JettersR.UI.Menus.MenuComponent;
import JettersR.UI.Menus.TextBoxes.TextBox;
import JettersR.UI.Visuals.Rect_DialogueBox;
import JettersR.Util.fixedVector2;
import JettersR.Util.fixedVector3;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;

public class LE_Inspector_OLD extends Menu
{
    public class LE_Inspector_Field extends Menu
    {
        /**Constructor.*/
        public LE_Inspector_Field(int x, int y)
        {
            super(x, y, new AAB_Box2D(BOX_WIDTH, 10));
            //
            dialogueBox = new Rect_DialogueBox(x, y, BOX_WIDTH, 10, inspectorColor);
        }

        @Override
        public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
        {
            return false;
        }

        @Override
        public void render(Screen screen, float xOffset, float yOffset)
        {

        }
    }
    private Rect_DialogueBox dialogueBox = null;
    public static final int BOX_WIDTH = 256, BOX_HEIGHT = 442;
    private Vector4f inspectorColor = new Vector4f(0.8f, 0.4f, 0.2f, 1.0f);

    //
    private Entity currentEntity = null;
    //private Menu subMenu;

    //
    private Font font = Fonts.get("Arial");
    private Vector4f textBoxColor = new Vector4f(0.4f, 0.2f, 0.1f, 1.0f);
    private Vector4f fontColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    
    /**Constructor.*/
    public LE_Inspector_OLD(int x, int y)
    {
        super(x, y, new AAB_Box2D(BOX_WIDTH, BOX_HEIGHT));
        //
        dialogueBox = new Rect_DialogueBox(x, y, BOX_WIDTH, BOX_HEIGHT, inspectorColor);
    }

    public void setEntity(Entity e)
    {
        //Set entity.
        this.currentEntity = e;

        //
        //Remake MenuComponents to show Entity variables.
        //

        //Clear what's there.
        //subMenu.clearComponents();

        try
        {
            //Get all of the fields of the Entity.
            Field[] fields = e.getClass().getDeclaredFields();

            for(Field field : fields)
            {
                //Make sure the field isn't transient, as they should not be saved.
                if(Modifier.isTransient(field.getModifiers())){continue;}

                //Temporally make the variable public if it is a private variable.
                boolean isPrivate = Modifier.isPrivate(field.getModifiers());
                if(isPrivate){field.setAccessible(true);}

                //The Variable's class.
                Class type = field.getType();

                //The Variable's value.
                Object value = field.get(e);

                //The variable's name, for display purposes.
                String name = field.getName();
                //field.getAnnotation(annotationClass)

                //Java only allows you to make switch statements with constant numbers. Not objects.
                //
                //Primatives
                if(type == byte.class)
                {
                    //If annotaion, Slider.
                    //Otherwise, TextBox.
                    //new TextBox(x, y, width, height, font, Controller.TYPING_INT, 1.0f, textBoxColor, fontColor);

                    //Needs to render byte name and textbox.
                }
                else if(type == short.class)
                {
                    //If annotaion, Slider.
                    //Otherwise, TextBox.
                }
                else if(type == int.class)
                {
                    //fixed-point check.
                    String f_check = name.substring(0, 2);
                    if(f_check.equals("f_"))
                    {
                        //If annotaion, Slider.
                        //Otherwise, TextBox.
                    }
                    else
                    {
                        //If annotaion, Slider.
                        //Otherwise, TextBox.
                    }
                    
                }
                else if(type == long.class)
                {
                    //If annotaion, Slider.
                    //Otherwise, TextBox.
                }
                //
                else if(type == char.class)
                {
                    //One-Character TextBox
                }
                else if(type == String.class)
                {
                    //TextBox
                }
                //
                else if(type == float.class)
                {
                    //If annotaion, Slider.
                    //Otherwise, TextBox.
                }
                else if(type == double.class)
                {
                    //If annotaion, Slider.
                    //Otherwise, TextBox.
                }
                //
                else if(type == boolean.class)
                {
                    //CheckBox
                }

                //Arrays

                //Vectors
                else if(type == Vector2i.class)
                {
                    Vector2i val = (Vector2i)value;
                    //
                    //2 Sliders/TextBoxes.
                }
                else if(type == Vector3i.class)
                {
                    Vector3i val = (Vector3i)value;
                    //
                    //3 Sliders/TextBoxes.
                }
                else if(type == Vector4i.class)
                {
                    Vector4i val = (Vector4i)value;
                    //
                    //4 Sliders/TextBoxes.
                }
                //
                else if(type == Vector2f.class)
                {
                    Vector2f val = (Vector2f)value;
                    //
                    //2 Sliders/TextBoxes.
                }
                else if(type == Vector3f.class)
                {
                    Vector3f val = (Vector3f)value;
                    //
                    //3 Sliders/TextBoxes.
                }
                else if(type == Vector4f.class)
                {
                    Vector4f val = (Vector4f)value;
                    //
                    //4 Sliders/TextBoxes.
                }
                //
                else if(type == fixedVector2.class)
                {
                    fixedVector2 val = (fixedVector2)value;
                    //
                    //2 Sliders/TextBoxes.
                }
                else if(type == fixedVector3.class)
                {
                    fixedVector3 val = (fixedVector3)value;
                    //
                    //3 Sliders/TextBoxes.
                }

                //Enums
                else if(type.isEnum())
                {
                    Enum[] enums = (Enum[])type.getEnumConstants();
                    //
                    //Method valueOf = type.getMethod("valueOf", String.class);
                    //enums[0].name();

                    //Create DropDownMenu.
                    //Entity_Property = 
                    //Add each Enum Constant as an option.
                    //Add DropDownMenu to Inspector Menu.
                }

                //Make the variable private again if it was private.
                if(isPrivate){field.setAccessible(false);}
            }
        }
        catch(IllegalAccessException ex){ex.printStackTrace();}
        //catch(NoSuchMethodException ex){ex.printStackTrace();}
    }

    @Override
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        //if(currentEntity == null){return ;}
        return false;
    }

    @Override
    public void render(Screen screen, float xOffset, float yOffset)
    {
        //Render DialogueBox.
        dialogueBox.render(screen, this.position.x + xOffset, this.position.y + yOffset);

        //Render Components.
    }
}
