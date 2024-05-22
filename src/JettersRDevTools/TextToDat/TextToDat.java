package JettersRDevTools.TextToDat;
/**
 * Author: Luke Sullivan
 * Last Edit: 1/6/2022
 */
import java.util.Scanner;

import javax.swing.JFileChooser;

import JettersR.Graphics.Fonts;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TextToDat
{
    public static final String outputPath = "src/JettersRDevTools/TextToDat/";

    public static String name = "DigitalFont";

    public static Scanner scanner;

    public static void main(String[] args)
    {
        //Make Scanner.
        scanner = new Scanner(System.in);

        //Ask user for action.
        System.out.println("1 = Make new Font, 2 = Edit font");
        int answer = scanner.nextInt();

        switch(answer)
        {
            case 1:
            newFont();
            break;

            case 2:
            //File
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(Fonts.fontsPath));

            fileChooser.setDialogTitle("Select a Level Folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.requestFocus();

            //Show it and wait for the user to select a Folder. If none was selected, cancel.
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                editFont(fileChooser.getSelectedFile());
            }
            break;
        }

        scanner.close();
    }

    public static void newFont()
    {
        //Ask user for chars.
        System.out.println("-Type chars in order of how they are shown on the sheet.");
        String string = scanner.next();

        short charSpace = askCharSpace(),
        wordSpace = askWordSpace();

        //Convert String to bytes and write them to a .dat file.
        File file = new File(outputPath + name + ".dat");

        try
        {
            //Set up the output stream.
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            
            byte[] bytes = new byte[(string.length() * 2) + 4];
            for(int i = 0; i < string.length(); i++)
            {
                //Technically i * 2.
                int slot = i << 1;

                //Retrive the current char.
                char character = string.charAt(i);

                //Convert the char to 2 bytes.
                bytes[slot]     = (byte)((character & 0xFF00) >> 8);
                bytes[slot + 1] = (byte)(character & 0x00FF);
            }

            //Put charSpace into array.
            bytes[bytes.length-4] = (byte)((charSpace & 0xFF00) >> 8);
            bytes[bytes.length-3] = (byte)(charSpace & 0x00FF);

            //Put wordSpace into array.
            bytes[bytes.length-2] = (byte)((wordSpace & 0xFF00) >> 8);
            bytes[bytes.length-1] = (byte)(wordSpace & 0x00FF);

            //Write all the bytes to the file.
            bos.write(bytes);

            //Output the file.
            file.createNewFile();

            //Close the output stream.
            bos.close();
            fos.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}

        
    }

    public static void editFont(File file)
    {
        //chars, charSpace (2 bytes), wordSpace (2 bytes).
        byte[] bytes = new byte[(int)file.length()];

        try
        {
            //Set up the input stream.
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            
            bis.read(bytes);

            //Close the input streams.
            bis.close();
            fis.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}

        boolean editing = true;
        while(editing)
        {
            System.out.println("\n0 = Done, 1 = Edit CharSpace, 2 = Edit WordSpace");
            int answer = scanner.nextInt();

            switch(answer)
            {
                case 0:
                editing = false;
                break;

                case 1:
                {
                    short c = askCharSpace();
                    bytes[bytes.length-4] = (byte)((c & 0xFF00) >> 8);
                    bytes[bytes.length-3] = (byte)(c & 0x00FF);
                }
                break;

                case 2:
                {
                    short w = askWordSpace();
                    bytes[bytes.length-2] = (byte)((w & 0xFF00) >> 8);
                    bytes[bytes.length-1] = (byte)(w & 0x00FF);
                }
                break;
            }
        }
        
        file = new File(outputPath + name + ".dat");

        try
        {
            //Set up the output stream.
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            //Write all the bytes to the file.
            bos.write(bytes);

            //Output the file.
            file.createNewFile();

            //Close the output stream.
            bos.close();
            fos.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}
    }

    public static short askCharSpace()
    {
        //Ask user for charSpace.
        System.out.println("\n-Type number of pixels of space between chars (from 0 to " + (Short.MAX_VALUE >> 1) + ").");
        short charSpace = scanner.nextShort();
        return charSpace;
    }

    public static short askWordSpace()
    {
        //Ask user for charSpace.
        System.out.println("\n-Type number of pixels of space between words (from 0 to " + (Short.MAX_VALUE >> 1) + ").");
        short wordSpace = scanner.nextShort();
        return wordSpace;
    }
}
