package JettersR.UI.Visuals;
/**
 * Author: Luke Sullivan
 * Last Edit: 5/6/2023
 */
import org.joml.Vector4f;

import JettersR.Controller;
import JettersR.Game;
import JettersR.Graphics.Font;
import JettersR.Graphics.Fonts;
import JettersR.Graphics.Screen;
import JettersR.UI.Menus.MenuComponent;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
//import JettersR.Util.Shapes.Shapes2D.Shape2D;

public class SpeechBubble extends MenuComponent
{
    //Dialogue Box to put the text inside.
    private Rect_DialogueBox dialogueBox;

    //Dimensions.
    private int width, height;

    //Is this speech bubble currently closing?
    private boolean closing = false;

    //Shrink'n'grow thingie.
    private float sizePercent = 0.0f;

    //String of text and text commands.
    private String text;

    //Font.
    private Font font = Fonts.get("Arial");

    //Where in the string are we printing?
    private int textStop = 0;

    //How quickly are we printing?
    private float writeInterval = 0.125f,
    writeTimer = 0.0f;

    //A timer.
    private float waveyTime = 0.0f;

    //Is this speech bubble paused?
    private boolean paused = false;
    //private byte numPauses = 0;

    /**Constructor.*/
    public SpeechBubble(int x, int y, int width, int height, String text)
    {
        super(x, y, new AAB_Box2D(width, height));
        
        //Set dimensions.
        this.width = width;
        this.height = height;

        //Set text.
        this.text = text;

        //Make box.
        dialogueBox = new Rect_DialogueBox(x, y, 0, 0, new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
    }

    @Override
    /**Update function.*/
    public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
    {
        if(!closing)
        {
            //System.out.println(paused);

            if(sizePercent < 1.0f)
            {
                //Increment percent.
                sizePercent += 0.05f;
                if(sizePercent >= 1.0f){sizePercent = 1.0f;}

                //Set size.
                dialogueBox.setSize(sizePercent, width, height);
            }
            else if(!paused)// && textStop < text.length())
            {
                //Increment write time.
                writeTimer += (1.0f / (float)Game.TARGET_FPS);

                if(writeTimer >= writeInterval)
                {
                    //Reset timer.
                    writeTimer -= writeInterval;

                    //Get current char.
                    char c = text.charAt(textStop);
                    //System.out.println("c:" + c);

                    //Keep track of pauses.
                    //byte current_numPauses = 0;

                    //Determine how much to increment text stop.
                    switch(c)
                    {
                        //Newline.
                        case '\n':
                        textStop+=2;
                        break;
                        
                        //Text commands.
                        case '\\':
                        {
                            while(c == '\\')
                            {
                                //Get following char.
                                textStop++;
                                c = text.charAt(textStop);

                                //Which command is this?
                                switch(c)
                                {
                                    //Text Speed.
                                    case '>':
                                    {
                                        //Default command check.
                                        textStop++;
                                        byte d = font.isDefaultCommand(textStop, text);
                                        textStop+=d;
                                        if(d == 2)
                                        {
                                            //Set text speed to default.
                                            writeInterval = 0.125f;

                                            //Get new char after default command.
                                            c = text.charAt(textStop);

                                            //Next loop.
                                            continue;
                                        }

                                        //Get next chars until a space is hit.
                                        String line = font.getUntilSpace(textStop, text);
                                        textStop += line.length()+1;

                                        //Should consist of one float.
                                        writeInterval = Float.parseFloat(line);

                                        //Advance loop.
                                        c = text.charAt(textStop);
                                    }
                                    break;

                                    //Cutscene Trigger.
                                    case 't':
                                    {
                                        //Default command check.
                                        textStop++;
                                        byte d = font.isDefaultCommand(textStop, text);
                                        textStop+=d;
                                        if(d == 2)
                                        {
                                            //Get new char.
                                            c = text.charAt(textStop);

                                            //Next loop.
                                            continue;
                                        }

                                        //Get next chars until a space is hit.
                                        String line = font.getUntilSpace(textStop, text);
                                        textStop += line.length()+1;

                                        //Parse trigger number out of the string.
                                        //int triggerNum = Integer.parseInt(line);
                                        //activateTrigger(triggerNum);//Use it to activate a cutscene trigger.
                                        //System.out.println(triggerNum);

                                        //Advance loop.
                                        c = text.charAt(textStop);
                                        //System.out.println("T Loop: " + line + " " + c);
                                    }
                                    break;

                                    //Text Pause.
                                    case '.':
                                    {
                                        //current_numPauses++;
                                        //System.out.println(current_numPauses);
                                        //if(current_numPauses > numPauses)
                                        //{
                                            paused = true;
                                            //numPauses++;
                                        //}
                                    }
                                    break;

                                    //All other text commands.
                                    default:
                                    {
                                        //Default command check.
                                        textStop++;
                                        byte d = font.isDefaultCommand(textStop, text);
                                        textStop+=d;
                                        //System.out.println("d: " + d + " " + text.charAt(textStop));
                                        if(d == 2)
                                        {
                                            //Get new char.
                                            c = text.charAt(textStop);
                                            //System.out.println("Def:" + c);

                                            //Next loop.
                                            //textStop--;
                                            continue;
                                        }

                                        //Get next chars until a space is hit.
                                        String line = font.getUntilSpace(textStop, text);
                                        textStop += line.length()+1;

                                        //Advance loop.
                                        c = text.charAt(textStop);
                                        //System.out.println("Loop: " + line + " " + c);
                                    }
                                    break;
                                }
                            }
                            textStop++;

                            //if(c == '\n'){textStop+=2;}
                        }
                        break;

                        //Default case.
                        default:
                        textStop++;
                        break;
                    }
                }

                //Skip to next trigger/pause.
                //if(input_Pressed(Controller.menu_CONFIRM))
                //{
                    //System.out.println(numPauses);
                //}

                if(!paused  && textStop >= text.length()){closing = true;}
            }
            //Advance scroll.
            else if(input_Pressed(Controller.menu_CONFIRM))
            {
                paused = false;
                //textStop++;
            }
        }
        //Close this speech bubble.
        else if(!paused)
        {
            //Increment percent.
            sizePercent -= 0.05f;
            if(sizePercent <= 0.0f){sizePercent = 0.0f;}

            //Set size.
            dialogueBox.setSize(sizePercent, width, height);
        }

        //Increment wavey time.
        waveyTime = (float)((waveyTime + ((0.5f / (float)Game.TARGET_FPS) * Screen.TWO_PI)) % Screen.TWO_PI);

        return false;
    }

    public float getSizePercent(){return sizePercent;}

    public boolean isClosing(){return closing;}

    @Override
    /**Render function.*/
    public void render(Screen screen, float xOffset, float yOffset)//, int cropX0, int cropY0, int cropX1, int cropY1)
    {
        //Calculate offset coordinates.
        int xa = (int)(this.position.x + xOffset) - (dialogueBox.getWidth()/2),
        ya = (int)(this.position.y + yOffset) - (dialogueBox.getHeight()/2);

        //Render dialogue box.
        dialogueBox.render(screen, 0, 0);

        //Render bubble tail.

        //Render text, stoping at textStop.
        if(sizePercent >= 1.0f)
        {
            //for(int i = 0; i < textStop; i++)
            //{
                font.renderSpeech
                (
                    screen, xa+2, ya+2, text,
                    textStop, waveyTime, 1.0f, Screen.DEFAULT_BLEND, false
                );
            //}
        }

        //Render pause cursor.
        //if(paused)
        //{
            //screen.renderSprite
        //}
    }
}
