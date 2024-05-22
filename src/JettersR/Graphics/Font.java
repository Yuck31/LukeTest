package JettersR.Graphics;
/**
 * A set of Sprites representing Characters in a font.
 * 
 * Author: Luke Sullivan
 * Last Edit: 1/20/2023
 */
import java.io.File;
//import java.io.FileInputStream;
//import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
//import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.joml.Vector4f;

import JettersR.Game;

//import JettersR.Game;

public class Font
{
    //SpriteSheet.
    private SpriteSheet sheet;

    //Sprites.
    private Sprite[] sprites;

    //Space between lines.  
    private short charSpace = 2, wordSpace = 4;
    private int lineSpace = 0;//, averageCharHeight;

    //Binds a char to a slot in the Sprites array.
    private HashMap<Character, Integer> charToSlot = new HashMap<Character, Integer>();

    /**Constructor.*/
    public Font(SpriteSheet sheet, String name)
    {
        //Set sheet.
        this.sheet = sheet;

        //Get char-binding .txt file.
        File file = new File(Fonts.fontsPath + name + ".txt");
        int length = -1;

        try
        {
            //Create a scanner to read the file.
            Scanner scanner = new Scanner(file);

            //There should be three lines of text in the file.
            for(byte lineNum = 0; lineNum < 3; lineNum++)
            {
                //Get current line of text.
                String line = scanner.nextLine();

                switch(lineNum)
                {
                    //Reading characters.
                    case 0:
                    {
                        //Get length.
                        length = line.length();

                        //System.out.println(length);

                        //Go through each character in the line.
                        for(int i = 0; i < length; i++)
                        {
                            
                            //Get the current character.
                            char character = line.charAt(i);

                            //Bind the character to the HashMap.
                            charToSlot.put(character, i);
                        }
                    }
                    break;

                    //Getting charSpace.
                    case 1:
                    charSpace = Short.parseShort(line.trim());
                    break;

                    //Getting wordSpace.
                    case 2:
                    wordSpace = Short.parseShort(line.trim());
                    break;
                }
            }

            //Close the scanner.
            scanner.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}

        //Set length of Sprites array.
        sprites = new Sprite[length];

        //Get the sheet's pixel data.
        int[] sheet_pixels = sheet.getPixels();
        //System.out.println("Start");

        //Use black pixels to split the sheet into multiple Sprites.
        int currentX = 0, currentY = 0, currentHeight = -1;
        for(int i = 0; i < sprites.length; i++)
        {
            //Get the current row's height.
            if(currentHeight < 0)
            {
                boolean bottom = true;

                //Go through the very left columnn of pixels.
                for(int y = currentY; y < sheet.getHeight(); y++)
                {
                    //System.out.println(currentX + " " + y + " " + sheet_pixels[0 + y * sheet.getWidth()]);

                    //If the current pixel is black, set current height for this row of characters.
                    if(sheet_pixels[0 + y * sheet.getWidth()] == 0xFF000000)
                    {
                        currentHeight = (y) - currentY;
                        bottom = false;
                        break;
                    }
                }
                //Cap height if the bottom of the image was reached.
                if(bottom){currentHeight = sheet.getHeight() - currentY;}
                //if(currentY >= sheet.getHeight() - currentHeight){currentY = sheet.getHeight() - (currentHeight+1);}
            }

            int width = 0, height = 0;
            boolean doneWithThisRow = false;

            //Get current sprite's width.
            //Go through the top row of the current region.
            for(int x = currentX; x < sheet.getWidth(); x++)
            {
                //If the current pixel is black or the end of the image has been reached.
                if(sheet_pixels[x + currentY * sheet.getWidth()] == 0xFF000000 || x >= sheet.getWidth()-1)
                {
                    //If the first pixel is black, this row is done.
                    if(x == currentX)
                    {
                        doneWithThisRow = true;
                        break;
                    }
                    //Otherwise, set this character sprite's width.
                    else
                    {
                        width = x - currentX;
                        break;
                    }
                }
            }
            if(doneWithThisRow)
            {
                currentX = 0;
                currentY += (currentHeight+1);
                currentHeight = -1;
                //
                if(height > lineSpace){lineSpace = height;}
                //
                i--;
                continue;
            }

            //Get current sprite's height.
            boolean bottomHeight = true;
            for(int y = currentY; y < currentY + currentHeight; y++)
            {
                //If the current pixel is magenta.
                if(sheet_pixels[currentX + y * sheet.getWidth()] == 0xFFFF00FF)
                {
                    height = y - currentY;
                    bottomHeight = false;
                    break;
                }
            }
            if(bottomHeight){height = currentHeight;}

            //Make sprite.
            sprites[i] = new Sprite(sheet, currentX, currentY, width, height);
            //System.out.println(i + " " + width + " " + height + " " + currentHeight);

            //Adjust sheet coordinates.
            currentX += (width + 1);
            if(currentX >= sheet.getWidth()-1)
            {
                currentX = 0;
                currentY += (currentHeight+1);
                currentHeight = -1;
            }

            //Adjust lineSpace.
            if(height > lineSpace){lineSpace = height;}
        }
        //System.out.println("End");

        //Add one to lineSpace.
        lineSpace++;
    }

    public SpriteSheet getSheet(){return sheet;}

    public short getCharSpace(){return charSpace;}
    public short getWordSpace(){return wordSpace;}

    public int getLineSpace(){return lineSpace;}

    /**Converts a String to a Sprite array using this font.*/
    public Sprite[] textToSprites(String text)
    {
        Sprite[] result = new Sprite[text.length()];

        for(int i = 0; i < result.length; i++)
        {
            char c = text.charAt(i);

            if(c == ' '){result[i] = Sprites.nullSprite;}
            else{result[i] = sprites[charToSlot.get(c)];}
        }

        return result;
    }

    /**
     * Converts a String to the given Sprite array using this font.
     * 
     * @param text the string of text to convert.
     * @param spriteArray the array to put the sprites in.
     * @return the width of the output using charSpace and wordSpace values.
     */
    public int textToSprites(String text, Sprite[] spriteArray)
    {
        int textWidth = 0;

        for(int i = 0; i < spriteArray.length; i++)
        {
            char c = text.charAt(i);

            if(c == ' ')
            {
                spriteArray[i] = Sprites.nullSprite;
                textWidth += wordSpace;
            }
            else
            {
                spriteArray[i] = sprites[charToSlot.get(c)];
                textWidth += spriteArray[i].getWidth() + charSpace;
            }
        }

        return textWidth;
    }

    

    //private int f0 = 0;
    private int f1 = 0;
    /**Renders a String of Text to the Screen in this font.*/
    public void render(Screen screen, int x, int y, String text, float scale, Vector4f blendingColor, boolean fixed)
    {
        final int[] offsets = new int[2];
        
        //Go through each char in the string.
        for(int i = 0; i < text.length(); i++)
        {
            //Get char.
            char c = text.charAt(i);

            //Run render function.
            renderChar(screen, x, y, offsets, c, i, scale, blendingColor, fixed);
        }

        //f0 = (f0+1) % 5;
        //f1 = (f1 + ((f0 == 0) ? 1 : 0)) % 120;
    }

    public void render_RtoL(Screen screen, int x, int y, String text, float scale, Vector4f blendingColor, boolean fixed)
    {
        final int[] offsets = new int[2];

        //Go through each char in the string.
        for(int i = text.length()-1; i >= 0; i--)
        {
            //Get char.
            char c = text.charAt(i);

            //Run render function.
            renderChar_RtoL(screen, x, y, offsets, c, i, scale, blendingColor, fixed);
        }

        //f0 = (f0+1) % 5;
        //f1 = (f1 + ((f0 == 0) ? 1 : 0)) % 120;
    }
    

    //Animated text stuff.
    private float waveyHeight = 0.0f, waveySpeed = 0.0f, waveyTime = 0.0f;
    private float shakeRange = 0.0f;
    private Vector4f currentTextColor = new Vector4f();
    private float textScale = 1.0f;

    /**Renders text as if it were speech bubble dialogue.*/
    public void renderSpeech(Screen screen, int x, int y, String text, int textStop, float waveyTime, float scale, Vector4f blendingColor, boolean fixed)
    {
        final int[] offsets = new int[2];
        textStop = (textStop > text.length()) ? text.length() : textStop;
        //
        this.waveyTime = waveyTime;
        //
        for(int i = 0; i < textStop; i++)
        {
            //Get current character.
            char c = text.charAt(i);

            //Text Command check.
            if(c == '\\')
            {
                //Get following character.
                i++;
                char c0 = text.charAt(i);

                //Exclude cases where we're trying to use backslash or space in the text.
                if(c0 != '\\' && c0 != ' ')
                {
                    //Check which command the char is.
                    switch(c0)
                    {
                        //Wavey text.
                        case '~':
                        {
                            //System.out.println("Wave");

                            //
                            //Default command check.
                            //

                            i++;//Start check from next character.
                            byte d = isDefaultCommand(i, text);//1 space does nothing. 2 spaces indicates a default command.
                            i+=d;//Advance however many spaces.
                            if(d == 2)//Is this a default commannd?
                            {
                                //Toggle wavey text off.
                                waveyHeight = 0.0f;
                                waveySpeed = 0.0f;

                                //Get new char.
                                c = text.charAt(i);

                                //Follow-up command?
                                if(c == '\\')
                                {
                                    i--;//<- This is just so the loop can run for this backspace.
                                    continue;
                                }

                                //Otherwise, proceed to render char.
                                break;
                            }

                            //Get next chars until a space is hit.
                            String line = getUntilSpace(i, text);
                            i += line.length() + 1;
                            c = text.charAt(i);

                            //Should consist of two floats seperated by a comma.
                            String[] words = line.split(",");

                            waveyHeight = Float.parseFloat(words[0]);
                            waveySpeed = Float.parseFloat(words[1]);

                            //Follow-up command?
                            if(c == '\\')
                            {
                                i--;
                                continue;
                            }
                        }
                        break;

                        //Shaky text.
                        case '!':
                        {
                            //Default command check.
                            i++;
                            byte d = isDefaultCommand(i, text);
                            i+=d;//Advance however many spaces.
                            if(d == 2)//Is this a default commannd?
                            {
                                //Toggle shaky text off.
                                shakeRange = 0.0f;

                                //Get new char.
                                c = text.charAt(i);

                                //Follow-up command?
                                if(c == '\\')
                                {
                                    i--;//<- This is just so the loop can run for this backspace.
                                    continue;
                                }

                                //Otherwise, proceed to render car.
                                break;
                            }

                            //Get next chars until a space is hit.
                            String line = getUntilSpace(i, text);
                            i += line.length() + 1;
                            c = text.charAt(i);

                            //Should consist of one float.
                            shakeRange = Float.parseFloat(line);

                            //Follow-up command?
                            if(c == '\\')
                            {
                                i--;
                                continue;
                            }
                        }
                        break;

                        //Text Color.
                        case 'c':
                        {
                            //Default command check.
                            i++;
                            byte d = isDefaultCommand(i, text);
                            i+=d;//Advance however many spaces there were.
                            if(d == 2)//Is this a default command?
                            {
                                //Set text color to default.
                                currentTextColor.set(Screen.DEFAULT_BLEND);

                                //Get new char.
                                c = text.charAt(i);

                                //Follow-up command?
                                if(c == '\\')
                                {
                                    i--;//<- This is just so the loop can run for this backspace.
                                    continue;
                                }

                                //Otherwise, proceed to render car.
                                break;
                            }

                            //Get next chars until a space is hit.
                            String line = getUntilSpace(i, text);
                            i += line.length() + 1;
                            c = text.charAt(i);
                            //System.out.println();

                            //Should consist of 4 bytes in hexadecimal.
                            String[] words = new String[]
                            {
                                line.substring(0, 2),
                                line.substring(2, 4),
                                line.substring(4, 6),
                                line.substring(6, 8),
                            };

                            //Parse the bytes to percents.
                            float
                            a = (float)Integer.parseInt(words[0], 16) / 255.0f,
                            r = (float)Integer.parseInt(words[1], 16) / 255.0f,
                            g = (float)Integer.parseInt(words[2], 16) / 255.0f,
                            b = (float)Integer.parseInt(words[3], 16) / 255.0f;

                            //Set the color.
                            currentTextColor.set
                            (
                                blendingColor.x * r,
                                blendingColor.y * g,
                                blendingColor.z * b,
                                blendingColor.w * a
                            );

                            //Follow-up command?
                            if(c == '\\')
                            {
                                i--;//<- This is just so the loop can run for this backspace.
                                continue;
                            }
                        }
                        break;

                        //Text scale.
                        case '%':
                        {
                            //Default command check.
                            i++;
                            byte d = isDefaultCommand(i, text);
                            i+=d;//Advance however many spaces there were.
                            if(d == 2)//Is this a default command?
                            {
                                //Set text scale to default.
                                textScale = 1.0f;

                                //Get new char.
                                c = text.charAt(i);

                                //Follow-up command?
                                if(c == '\\')
                                {
                                    i--;
                                    continue;
                                }

                                //Otherwise, proceed to render car.
                                break;
                            }

                            //Get next chars until a space is hit.
                            String line = getUntilSpace(i, text);
                            i += line.length() + 1;
                            c = text.charAt(i);

                            //Should consist of one float.
                            textScale = Float.parseFloat(line);

                            //Follow-up command?
                            if(c == '\\')
                            {
                                i--;
                                continue;
                            }
                        }
                        break;

                        // \n, \+, and \- are taken care of at load time.
                        // \>, \. and \t are taken care of by the speech bubble. So skip them.

                        //Skip text speed and triggers.
                        case '>':
                        case 't':
                        {
                            //Default command check.
                            i++;
                            byte d = isDefaultCommand(i, text);
                            i+=d;
                            if(d == 2)
                            {
                                //Get new char.
                                c = text.charAt(textStop);

                                //Next loop.
                                continue;
                            }

                            //Get next chars until a space is hit.
                            String line = getUntilSpace(i, text);
                            i += line.length();
                            c = text.charAt(i);
                        }
                        continue;

                        //Skip pauses. They're handled by speech bubbles.
                        case '.':
                        {
                            //Space check.
                            //if(text.charAt(i+1) == ' '){i++;}
                        }
                        continue;
                    }
                }
            }

            //Render it.
            renderChar(screen, x, y, offsets, c, i, scale, currentTextColor, fixed);
        }

        //Set default values.
        waveyHeight = 0.0f;
        waveySpeed = 0.0f;
        this.waveyTime = 0.0f;

        shakeRange = 0.0f;

        currentTextColor.set(Screen.DEFAULT_BLEND);

        textScale = 1.0f;
    }

    /**
     * Renders a single character.
     * 
     * @param screen
     * @param x
     * @param y
     * @param offsets
     * @param c
     * @param scale
     * @param blendingColor
     * @param fixed
     */
    public void renderChar
    (
        Screen screen, int x, int y,
        final int[] offsets,//<- DO NOT change this pointer mid-function.
        char c, int charIndex, float scale, Vector4f blendingColor, boolean fixed
    )
    {
        switch(c)
        {
            case ' ':
            offsets[0] += (wordSpace * scale);
            break;

            case '\n':
            offsets[0] = 0;
            offsets[1] += (lineSpace * scale);
            break;

            default:
            {
                int current_xOffset = offsets[0], current_yOffset = offsets[1];

                //Wavey text.
                if(waveyHeight > 0.0f){current_yOffset += (waveyHeight * Math.sin(waveyTime + (waveySpeed * charIndex)));}

                //Shakey text.
                if(shakeRange > 0.0f)
                {
                    current_xOffset += ((Game.RANDOM.nextInt(3) - 1.0f) * shakeRange);
                    current_yOffset += ((Game.RANDOM.nextInt(3) - 1.0f) * shakeRange);

                    //if(c == 'U'){System.out.println(shakeRange + " " + current_xOffset + " " + current_yOffset);}
                }

                //Text color is already taken care of.

                //Text scale.
                scale *= textScale;

                //Get the slot number associated with this character.
                Integer slot = charToSlot.get(c);
                if(slot == null)
                {
                    //Get first sprite for dimensions.
                    Sprite s0 = sprites[0];

                    //Draw a rect in place of the null character.
                    screen.drawRect(x + current_xOffset, y + current_yOffset, (int)(s0.getWidth() * scale), (int)(s0.getHeight() * scale), blendingColor, fixed);
                    offsets[0] += (int)((s0.getWidth() + charSpace) * scale);
                }
                else
                {
                    Sprite s = sprites[slot];

                    //Render the character.
                    screen.renderSprite_Sc(x + current_xOffset, y + current_yOffset, s, Sprite.FLIP_NONE, f1, f1, blendingColor, scale, scale, fixed);
                    offsets[0] += (int)((s.getWidth() + charSpace) * scale);

                    //screen.renderSprite(x + xOffset, y + yOffset, s, Sprite.Flip.NONE, f1, f1, blendingColor, fixed);
                    //result_xOffset += (int)((s.getWidth() + charSpace) * scale);
                }
            }
            break;
        }
    }

    public void renderChar(Screen screen, int x, int y, char c, float scale, boolean fixed)
    {renderChar(screen, x, y, new int[2], c, 0, scale, Screen.DEFAULT_BLEND, fixed);}


    public void renderChar_RtoL
    (
        Screen screen, int x, int y,
        final int[] offsets,//<- DO NOT change this pointer mid-function.
        char c, int charIndex, float scale, Vector4f blendingColor, boolean fixed
    )
    {
        switch(c)
        {
            case ' ':
            offsets[0] -= (wordSpace * scale);
            break;

            case '\n':
            offsets[0] = 0;
            offsets[1] += (lineSpace * scale);
            break;

            default:
            {
                int current_xOffset = offsets[0], current_yOffset = offsets[1];

                //Wavey text.
                if(waveyHeight > 0.0f){current_yOffset += (waveyHeight * Math.sin(waveyTime + (waveySpeed * charIndex)));}

                //Shakey text.
                if(shakeRange > 0.0f)
                {
                    current_xOffset += ((Game.RANDOM.nextInt(3) - 1.0f) * shakeRange);
                    current_yOffset += ((Game.RANDOM.nextInt(3) - 1.0f) * shakeRange);

                    //if(c == 'U'){System.out.println(shakeRange + " " + current_xOffset + " " + current_yOffset);}
                }

                //Text color is already taken care of.

                //Text scale.
                scale *= textScale;

                //Get the slot number associated with this character.
                Integer slot = charToSlot.get(c);
                if(slot == null)
                {
                    //Get first sprite for dimensions.
                    Sprite s0 = sprites[0];
                    current_xOffset -= s0.getWidth();

                    //Draw a rect in place of the null character.
                    screen.drawRect(x + current_xOffset, y + current_yOffset, (int)(s0.getWidth() * scale), (int)(s0.getHeight() * scale), blendingColor, fixed);
                    offsets[0] -= (int)((s0.getWidth() + charSpace) * scale);
                }
                else
                {
                    Sprite s = sprites[slot];
                    current_xOffset -= s.getWidth();

                    //Render the character.
                    screen.renderSprite_Sc(x + current_xOffset, y + current_yOffset, s, Sprite.FLIP_NONE, f1, f1, blendingColor, scale, scale, fixed);
                    offsets[0] -= (int)((s.getWidth() + charSpace) * scale);

                    //screen.renderSprite(x + xOffset, y + yOffset, s, Sprite.Flip.NONE, f1, f1, blendingColor, fixed);
                    //result_xOffset += (int)((s.getWidth() + charSpace) * scale);
                }
            }
            break;
        }
    }


    /**
     * To be used after a backslash. Checks how many spaces there are after it.
     * 
     * @param textIndex Should be the first index after a backslash.
     * @param text The whole text being rendered.
     * @return How many spaces there are from textIndex. 1 does nothing. 2 indicates a default command.
     */
    public byte isDefaultCommand(int textIndex, String text)
    {
        //Get first char.
        char c = text.charAt(textIndex);

        //If the first char is a space.
        if(c == ' ')
        {
            //Nothing so far. Get the second char.
            c = text.charAt(textIndex+1);

            //If its also a space.
            if(c == ' ')
            {
                //There we're two spaces. This is a default command.
                return 2;
            }
            //There was one space. This is not a default command.
            else{return 1;}
        }

        //There were no spaces. This is not a default command.
        return 0;
    }

    public String getUntilSpace(int textIndex, String text)
    {
        String result = "";
        int textLength = text.length();
        //
        for(int i = textIndex; i < textLength; i++)
        {
            char c2 = text.charAt(i);

            if(c2 == ' '){break;}
            else{result += text.charAt(i);}
        }
        //
        return result;
    }


    public void renderCursor(Screen screen, int x, int y, String text, int cursorIndex, float scale, Vector4f blendingColor, boolean fixed)
    {
        if(cursorIndex > text.length()){cursorIndex = text.length();}
        int xOffset = 0;
        //
        for(int i = 0; i < cursorIndex; i++)
        {
            char c = text.charAt(i);

            switch(c)
            {
                case ' ':
                xOffset += (wordSpace * scale);
                break;

                //case '`':
                //xOffset = 0;
                //break;

                default:
                Integer slot = charToSlot.get(c);
                if(slot == null)
                {
                    Sprite s0 = sprites[0];
                    xOffset += ((s0.getWidth() + charSpace) * scale);
                }
                else
                {
                    Sprite s = sprites[slot];
                    xOffset += ((s.getWidth() + charSpace) * scale);
                }
                break;
            }
        }
        //
        screen.drawLine(x + xOffset-1, y, x + xOffset, (int)(y + (lineSpace * scale)), blendingColor, fixed);
    }

    /**Renders a String of Text to the Screen in this font, dafault scale is 1f.*/
    public void render(Screen screen, int x, int y, String text, Vector4f blendingColor, boolean fixed)
    {render(screen, x, y, text, 1f, blendingColor, fixed);}

    /**Renders a String of Text to the Screen in this font, default blending color is white.*/
    public void render(Screen screen, int x, int y, String text, float scale, boolean fixed)
    {render(screen, x, y, text, scale, Screen.DEFAULT_BLEND, fixed);}

    /**Renders a String of Text to the Screen in this font using default scale and blending color.*/
    public void render(Screen screen, int x, int y, String text, boolean fixed)
    {render(screen, x, y, text, 1f, Screen.DEFAULT_BLEND, fixed);}


    /**Renders an Array of Sprites using this font's charSpace and wordSpace value.*/
    public void render(Screen screen, int x, int y, Sprite[] textSprites, float scale, Vector4f blendingColor, boolean fixed)
    {
        int xOffset = 0, yOffset = 0;
        for(int i = 0; i < textSprites.length; i++)
        {
            Sprite s = textSprites[i];

            if(s == Sprites.nullSprite){xOffset += (wordSpace * scale);}
            else
            {
                screen.renderSprite_Sc(x + xOffset, y + yOffset, s, Sprite.FLIP_NONE, blendingColor, scale, scale, fixed);
                xOffset += ((s.getWidth() + charSpace) * scale);
            }
        }
    }

    /**Renders an Array of Sprites using this font's charSpace and wordSpace value, default blending color is white.*/
    public void render(Screen screen, int x, int y, Sprite[] textSprites, float scale, boolean fixed)
    {render(screen, x, y, textSprites, scale, Screen.DEFAULT_BLEND, fixed);}

    /**Renders an Array of Sprites using this font's charSpace and wordSpace value, default scale is 1f.*/
    public void render(Screen screen, int x, int y, Sprite[] textSprites, Vector4f blendingColor, boolean fixed)
    {render(screen, x, y, textSprites, 1f, blendingColor, fixed);}

    /**Renders an Array of Sprites using this font's charSpace and wordSpace value ussing default scale and blending color.*/
    public void render(Screen screen, int x, int y, Sprite[] textSprites, boolean fixed)
    {render(screen, x, y, textSprites, 1f, Screen.DEFAULT_BLEND, fixed);}
}
