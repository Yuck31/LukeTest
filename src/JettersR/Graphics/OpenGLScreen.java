package JettersR.Graphics;
/**
 * This is a version of the base Screen class that utilizes OpenGL 3.3 to render the game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 12/24/2023
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL33;

import org.joml.Vector3f;
import org.joml.Vector4f;
//import org.joml.Matrix3f;
import org.joml.Matrix4f;

import JettersR.Game;
import JettersR.Entities.Components.Lights.AreaLight;
import JettersR.Entities.Components.Lights.DirectionalLight;
import JettersR.Entities.Components.Lights.Light;
import JettersR.Tiles.Graphics.TileMesh;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
import JettersR.Util.Shapes.Shapes3D.Misc.AAB_RoundedBox;
import JettersR.Util.Shapes.Shapes3D.Misc.SphericalSector;

public class OpenGLScreen extends Screen
{
    public static final String shadersPath = "assets/Shaders/OpenGL3_3/";

    /**
     * Shader Program class. Compiles given Vertex and Fragment Shaders into
     * a Shader Program.
     */
    private class ShaderProgram
    {
        //ID
        protected final int ID;

        //Variable location for the view matrix.
        //protected int uView = -4;

        /**Constructor.*/
        public ShaderProgram(String vertexPath, String fragmentPath)
        {
            ID = linkShaders
            (
                loadShader(OpenGLScreen.shadersPath + vertexPath + ".glsl", GL_VERTEX_SHADER),
                loadShader(OpenGLScreen.shadersPath + fragmentPath + ".glsl", GL_FRAGMENT_SHADER)
            );

            //this.uView = glGetUniformLocation(ID, "uView");
            //System.out.println(uView);
        }

        /**Loads and compiles shaders*/
        private int loadShader(String path, int shaderType)
        {
            String code = "";
            try
            {
                File file = new File(path);
                Scanner scanner = new Scanner(file);

                while(scanner.hasNextLine())
                {code = code + scanner.nextLine() + "\n";}
                scanner.close();

                //System.out.println(code);
            }
            catch(Exception e){e.printStackTrace();}

            //Create a new shader of the given shaderType
            int shader = glCreateShader(shaderType);
            glShaderSource(shader, code);

            //Compile Shader
            glCompileShader(shader);

            //Check if compile succeded
            int[] success = new int[1];
            String infoLog;
            glGetShaderiv(shader, GL_COMPILE_STATUS, success);
            if(success[0] < 1)
            {
                infoLog = glGetShaderInfoLog(shader, 512);
                throw new RuntimeException("ERROR: Shader \"" + path + "\" Compilation failed\n" + infoLog);
            }

            //Return the Shader address
            return shader;
        }

        /**Links given shaders to a shader program*/
        private int linkShaders(int... shaders)
        {
            //Create Shader program
            int shaderProgram = glCreateProgram();

            //Attach shaders to program
            for(int i = 0; i < shaders.length; i++)
            {glAttachShader(shaderProgram, shaders[i]);}

            //Link the program
            glLinkProgram(shaderProgram);

            //Check for Linking Errors
            int success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
            if(success == GL_FALSE)
            {
                int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
                throw new RuntimeException
                (
                    "ERROR: Linking of Shaders failed.\n" +
                    glGetProgramInfoLog(shaderProgram, len)
                );
            }

            //Delete shaders from CPU. We're done with them here.
            for(int i = 0; i < shaders.length; i++)
            {glDeleteShader(shaders[i]);}

            return shaderProgram;
        }

        //ID Getter.
        public int getID(){return ID;}

        //Matrix Location Getters.
        //public int get_uProj(){return uProj;}
        //public int get_uView(){return uView;}
    
        public void use(){glUseProgram(this.ID);}
        public void detach(){glUseProgram(0);}
        //public void delete(){glDeleteProgram(this.ID);}
    }

    //ShaderPrograms.
    private ShaderProgram frameBuffer_ShaderProgram, copyToColor_ShaderProgram,
    basic_ShaderProgram,
    lighting_ShaderProgram,
    shadowVolume_ShaderProgram, shadowSilhouette_ShaderProgram,
    //
    diffuse_ShaderProgram,
    reflection_ShaderProgram,
    //
    line_ShaderProgram,
    circle_ShaderProgram;

    //Texture Slots.
    private final int[] TEX_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7};

    /**
     * This is a batch that takes a bunch of verticies and renders them all in one draw call.
     * This is much faster than drawing one sprite per draw call as it saves on draw calls.
     * Up to 8 textures can be used per draw call and one shader can be used per draw call.
     */
    private abstract class RenderBatch
    {
        //Vertices Array.
        protected float[] vertices;
        //protected int maxVertexCount;

        //IDs
        protected int vertexArrayObject_ID, vertexBufferObject_ID,
        elementBufferObject_ID;

        //Shader.
        //protected ShaderProgram shaderProgram;

        //Vertex stuff.
        protected int offset = 0; 
        protected boolean hasRoom;

        /**Constructor.*/
        public RenderBatch
        (//ShaderProgram shaderProgram,
            int maxVertexCount, int vertexSize)
        {   
            //this.shaderProgram = shaderProgram;
            //this.maxVertexCount = maxVertexCount;

            //Create vertices array.
            vertices = new float[maxVertexCount * vertexSize];

            //It has room initially.
            this.hasRoom = true;
        }

        protected abstract void start();

        public final boolean hasRoom(){return hasRoom;}

        public abstract void render();
    }


    /**
     * RenderBatch specifically for Sprites.
     */
    private abstract class SpriteBatch extends RenderBatch
    {
        protected int numSprites = 0;
        protected final int maxSpriteCount;
        protected final List<SpriteSheet> sheets;

        /**Constructor.*/
        public SpriteBatch(int maxSpriteCount, int vertexSize)
        {
            super(maxSpriteCount * 4, vertexSize);
            this.maxSpriteCount = maxSpriteCount;

            //Initialize sheets list.
            this.numSprites = 0;
            this.hasRoom = true;
            this.sheets = new ArrayList<SpriteSheet>();
        }

        /**Sets up OpenGL objects needed for rendering.*/
        public void start()
        {
            //Generate and bind a Vertex Array Object.
            vertexArrayObject_ID = glGenVertexArrays();
            glBindVertexArray(vertexArrayObject_ID);

            //Generate and bind a Vertex Buffer Object.
            vertexBufferObject_ID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

            //Create and upload Index Buffer to GPU.
            elementBufferObject_ID = glGenBuffers();
            int[] indices = new int[6 * maxSpriteCount];
            for(int index = 0; index < maxSpriteCount; index++)
            {
                int offsetArrayIndex = 6 * index;
                int offset = 4 * index;

                //3, 2, 0, 0, 2, 1      7, 6, 4, 4, 6, 5
                //Triangle 1
                indices[offsetArrayIndex]   = offset + 0;
                indices[offsetArrayIndex+1] = offset + 1;
                indices[offsetArrayIndex+2] = offset + 2;

                //Triangle 2
                indices[offsetArrayIndex+3] = offset + 2;
                indices[offsetArrayIndex+4] = offset + 3;
                indices[offsetArrayIndex+5] = offset + 0;

                //indices[offsetArrayIndex+5] = offset + 0;
                //indices[offsetArrayIndex+4] = offset + 3;
                //indices[offsetArrayIndex+3] = offset + 2;
            }
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferObject_ID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        }

        /**Adds a Texture to this RenderBatch.*/
        public void addSheet(SpriteSheet s){sheets.add(s);}
        public boolean hasSheet(SpriteSheet s){return sheets.contains(s);}
        public boolean hasSheetRoom(){return this.sheets.size() < 8;}

        public int getTexSlot(int sheetID)
        {
            for(int i = 0; i < sheets.size(); i++)
            {
                if(sheetID == sheets.get(i).getID())
                {return i + 1;}
            }
            return 0;

            //return sheetID;
        }

        public int sheets_size(){return this.sheets.size();}

        //private void printVertex(int v)
        //{
            //int offset = v * Basic_RenderBatch.VERTEX_SIZE;

            //System.out.println
            //(
                //vertices[offset] + " " + vertices[offset + 1] + " " + vertices[2] + " " +
                //vertices[offset + 3] + " " + vertices[offset + 4] + " " + vertices[offset + 5] + " " + vertices[offset + 6] + " " +
                //vertices[offset + 7] + " " + vertices[offset + 8] + " " +
                //vertices[offset + 9]
            //);
        //}

        /**Sends all stored vertex data in this SpriteBatch to the GPU for rendering then resets vertex offset and SpriteSheets.*/
        public final void render()
        {
            //Use Vertex Buffer Object to upload vertices to Shader.
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

            //Bind Textures.
            for(int i = 0; i < sheets.size(); i++)
            {
                glActiveTexture(GL_TEXTURE0 + i + 1);
                glBindTexture(GL_TEXTURE_2D, sheets.get(i).getID());
            }

            //Bind Vertex Array Object.
            glBindVertexArray(vertexArrayObject_ID);

            //Use the uploaded vertcies to render.
            glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

            //Reset offset-related values to default.
            this.numSprites = 0;
            this.offset = 0;
            this.hasRoom = true;
            //This is so we don't have to clear the verticies array every frame.

            //Remove SpriteSheets from this batch.
            for(int i = sheets.size()-1; i >= 0; i--)
            {sheets.remove(i);}
        }

        /**Renders this SpriteBatch without reseting vertex offset or SpriteSheets.*/
        public final void render_noClear()
        {
            //Use Vertex Buffer Object to upload vertices to Shader.
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

            //Bind Textures.
            for(int i = 0; i < sheets.size(); i++)
            {
                glActiveTexture(GL_TEXTURE0 + i + 1);
                glBindTexture(GL_TEXTURE_2D, sheets.get(i).getID());
            }

            //Bind Vertex Array Object.
            glBindVertexArray(vertexArrayObject_ID);

            //Use the uploaded vertcies to render.
            glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);
        }

        /**Resets this SpriteBatch's vertex offset and SpriteSheets.*/
        public final void clear()
        {
            //Reset offset-related values to default.
            this.numSprites = 0;
            this.offset = 0;
            this.hasRoom = true;
 
            //Remove SpriteSheets from this batch.
            for(int i = sheets.size()-1; i >= 0; i--)
            {sheets.remove(i);}
        }
    }


    /**
     * Sprite Batch for basic sprites.
     */
    private class Basic_SpriteBatch extends SpriteBatch
    {
        //Vertex constants.
        private static final int
        POSITION_SIZE = 3,
        COLOR_SIZE = 4,
        TEX_COORDS_SIZE = 2,
        TEX_ID_SIZE = 1,
        //
        POSITION_OFFSET = 0,
        COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES,
        TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES,
        TEX_SLOT_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES,
        //
        VERTEX_SIZE = 10,
        VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

        //Current crop region.
        public int scissorX, scissorY, scissorWidth, scissorHeight;

        /**Constructor.*/
        public Basic_SpriteBatch(int maxSpriteCount){super(maxSpriteCount, VERTEX_SIZE);}

        /**Crop Constructor.*/
        public Basic_SpriteBatch(int maxSpriteCount, int scissorX, int scissorY, int scissorWidth, int scissorHeight)
        {
            super(maxSpriteCount, VERTEX_SIZE);//, scissorX, scissorY, scissorWidth, scissorHeight);

            //Set scissor region.
            this.scissorX = scissorX;
            this.scissorY = scissorY;
            this.scissorWidth = scissorWidth;
            this.scissorHeight = scissorHeight;
        }

        public void start()
        {
            super.start();

            //Create and enable vertex attribute pointers.
            glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
            glEnableVertexAttribArray(2);

            glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_SLOT_OFFSET);
            glEnableVertexAttribArray(3);
        }

        public void addVertices(float position_x, float position_y, float position_z, Vector4f color, float texX, float texY, int texSlot)
        {
            //Load Position
            vertices[offset] = position_x;
            vertices[offset+1] = position_y;
            vertices[offset+2] = position_z;

            //Load Color
            vertices[offset+3] = color.x;
            vertices[offset+4] = color.y;
            vertices[offset+5] = color.z;
            vertices[offset+6] = color.w;

            //Load Texture Coordinates
            vertices[offset+7] = texX;
            vertices[offset+8] = texY;

            //Load Texture Slot
            vertices[offset+9] = texSlot;

            //Increment offset;
            offset += VERTEX_SIZE;

            //Increment Sprite count if needed.
            if(offset % (VERTEX_SIZE * 4) == 0)
            {
                numSprites++;
                if(numSprites >= this.maxSpriteCount){this.hasRoom = false;}
            }
        }

        /**Sets the scisssor region for this batch.*/
        public void setScisssorRegion(int scissorX, int scissorY, int scissorWidth, int scissorHeight)
        {
            this.scissorX = scissorX;
            this.scissorY = scissorY;
            this.scissorWidth = scissorWidth;
            this.scissorHeight = scissorHeight;
        }
    }

    /**
     * Sprite batch meant for sprites affected by lighting.
     */
    private class Lighting_SpriteBatch extends SpriteBatch
    {
        private static final int
        POSITION_SIZE = 3,
        RGB_SIZE = 3,
        ALPHA_SIZE = 1, ALPHA_SIZE_BYTES = ALPHA_SIZE * Float.BYTES,
        TEX_COORDS_SIZE = 4,
        TEX_ID_SIZE = 1,
        EMISSION_SIZE = 1,
        //
        POSITION_OFFSET = 0,
        RGB_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES,
        ALPHA_OFFSET = RGB_OFFSET + RGB_SIZE * Float.BYTES,
        TEX_COORDS_OFFSET = ALPHA_OFFSET + ALPHA_SIZE_BYTES,
        TEX_SLOT_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES,
        EMISSION_OFFSET = TEX_SLOT_OFFSET + EMISSION_SIZE * Float.BYTES,
        //
        VERTEX_SIZE = 13,
        VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES,
        //For opaque batches.
        OPAQUE_VERTEX_SIZE = VERTEX_SIZE - ALPHA_SIZE,
        OPAQUE_VERTEX_SIZE_BYTES = OPAQUE_VERTEX_SIZE * Float.BYTES;

        /**Constructor.*/
        public Lighting_SpriteBatch(int maxSpriteCount, boolean opaque){super(maxSpriteCount, (opaque) ? OPAQUE_VERTEX_SIZE : VERTEX_SIZE);}

        /**Buffer object initialization for opaque SpriteBatches.*/
        public void start_Opaque()
        {
            //Create VAO, VBO, and Index Buffer.
            super.start();

            //Create and enable vertex attribute pointers.
            glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, OPAQUE_VERTEX_SIZE_BYTES, POSITION_OFFSET);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, RGB_SIZE, GL_FLOAT, false, OPAQUE_VERTEX_SIZE_BYTES, RGB_OFFSET);
            glEnableVertexAttribArray(1);

            //Assign default alpha.
            glVertexAttrib1f(2, 1.0f);

            glVertexAttribPointer(3, TEX_COORDS_SIZE, GL_FLOAT, false, OPAQUE_VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET - ALPHA_SIZE_BYTES);
            glEnableVertexAttribArray(3);

            glVertexAttribPointer(4, TEX_ID_SIZE, GL_FLOAT, false, OPAQUE_VERTEX_SIZE_BYTES, TEX_SLOT_OFFSET - ALPHA_SIZE_BYTES);
            glEnableVertexAttribArray(4);

            glVertexAttribPointer(5, EMISSION_SIZE, GL_FLOAT, false, OPAQUE_VERTEX_SIZE_BYTES, EMISSION_OFFSET - ALPHA_SIZE_BYTES);
            glEnableVertexAttribArray(5);
        }

        /**Buffer object initialization for transperant SpriteBatches.*/
        public void start_Transperant()
        {
            //Create VAO, VBO, and Index Buffer.
            super.start();

            //Create and enable vertex attribute pointers.
            glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, RGB_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, RGB_OFFSET);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, ALPHA_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, ALPHA_OFFSET);
            glEnableVertexAttribArray(2);

            glVertexAttribPointer(3, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
            glEnableVertexAttribArray(3);

            glVertexAttribPointer(4, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_SLOT_OFFSET);
            glEnableVertexAttribArray(4);

            glVertexAttribPointer(5, EMISSION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, EMISSION_OFFSET);
            glEnableVertexAttribArray(5);
        }

        public void addVertices_Opaque(float position_x, float position_y, float position_z, Vector4f color, float texX, float texY, float normX, float normY, int texSlot, float emission)
        {
            //Load Position.
            vertices[offset] = position_x;
            vertices[offset+1] = position_y;
            vertices[offset+2] = position_z;

            //Load Color.
            vertices[offset+3] = color.x;
            vertices[offset+4] = color.y;
            vertices[offset+5] = color.z;

            //Load Texture color and noraml Coordinates.
            vertices[offset+6] = texX;
            vertices[offset+7] = texY;
            vertices[offset+8] = normX;
            vertices[offset+9] = normY;

            //Load Texture Slot.
            vertices[offset+10] = texSlot;

            //Load Emission.
            vertices[offset+11] = emission;

            //Increment offset;
            offset += OPAQUE_VERTEX_SIZE;

            //Increment Sprite count if needed.
            if(offset % (OPAQUE_VERTEX_SIZE * 4) == 0)
            {
                numSprites++;
                if(numSprites >= this.maxSpriteCount){this.hasRoom = false;}
            }
        }

        public void addVertices_Transperant(float position_x, float position_y, float position_z, Vector4f color, float texX, float texY, float normX, float normY, int texSlot, float emission)
        {
            //Load Position.
            vertices[offset] = position_x;
            vertices[offset+1] = position_y;
            vertices[offset+2] = position_z;

            //Load Color.
            vertices[offset+3] = color.x;
            vertices[offset+4] = color.y;
            vertices[offset+5] = color.z;
            vertices[offset+6] = color.w;

            //Load Texture color and noraml Coordinates.
            vertices[offset+7] = texX;
            vertices[offset+8] = texY;
            vertices[offset+9] = normX;
            vertices[offset+10] = normY;

            //Load Texture Slot.
            vertices[offset+11] = texSlot;

            //Load Emission.
            vertices[offset+12] = emission;

            //Increment offset;
            offset += VERTEX_SIZE;

            //Increment Sprite count if needed.
            if(offset % (VERTEX_SIZE * 4) == 0)
            {
                numSprites++;
                if(numSprites >= this.maxSpriteCount){this.hasRoom = false;}
            }
        }
    }

    /**
     * Render Batch meant for Shadow Volumes.
     */
    private class ShadowVolume_RenderBatch extends RenderBatch
    {
        private static final int
        POSITION_SIZE = 3,
        //
        POSITION_OFFSET = 0,
        //
        VERTEX_SIZE = 3,
        VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

        private int numQuads = 0;
        private final int maxQuadCount;

        /**Constructor.*/
        public ShadowVolume_RenderBatch(int maxQuadCount)
        {
            //Initialize vertex array.
            super(maxQuadCount * 4, VERTEX_SIZE);
            this.maxQuadCount = maxQuadCount;

            //Initialize sheets list.
            this.numQuads = 0;
            this.hasRoom = true;
        }

        /**
         * 
         */
        public final void start()
        {
            //Generate and bind a Vertex Array Object.
            vertexArrayObject_ID = glGenVertexArrays();
            glBindVertexArray(vertexArrayObject_ID);

            //Generate and bind a Vertex Buffer Object.
            vertexBufferObject_ID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

            //Create and upload Index Buffer to GPU.
            elementBufferObject_ID = glGenBuffers();
            int[] indices = new int[6 * this.maxQuadCount];
            for(int index = 0; index < this.maxQuadCount; index++)
            {
                int offsetArrayIndex = 6 * index;
                int offset = 4 * index;

                //3, 2, 0, 0, 2, 1      7, 6, 4, 4, 6, 5
                //Triangle 1
                indices[offsetArrayIndex]   = offset + 0;
                indices[offsetArrayIndex+1] = offset + 1;
                indices[offsetArrayIndex+2] = offset + 2;

                //Triangle 2
                indices[offsetArrayIndex+3] = offset + 2;
                indices[offsetArrayIndex+4] = offset + 3;
                indices[offsetArrayIndex+5] = offset + 0;
            }
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferObject_ID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);


            //
            //Create and enable vertex attribute pointer.
            //
            glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
            glEnableVertexAttribArray(0);
        }

        /**
         * Adds the given position to this batch as a vertex.
         * 
         * @param position_x
         * @param position_y
         * @param position_z
         */
        public void addVertices(float position_x, float position_y, float position_z)
        {
            //Load Position.
            vertices[offset] = position_x;
            vertices[offset+1] = position_y;
            vertices[offset+2] = position_z;

            //Increment offset;
            offset += VERTEX_SIZE;

            //Increment Quad count if this number of vertex is a multiple of 4.
            if(offset % (VERTEX_SIZE * 4) == 0)
            {
                numQuads++;
                if(numQuads >= this.maxQuadCount){this.hasRoom = false;}
            }
        }

        /**Sends all stored vertex data in this batch to the GPU for rendering.*/
        public final void render()
        {
            //Use Vertex Buffer Object to upload vertices to Shader.
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

            //Bind Vertex Array Object.
            glBindVertexArray(vertexArrayObject_ID);

            //Use the uploaded vertcies to render.
            glDrawElements(GL_TRIANGLES, this.numQuads * 6, GL_UNSIGNED_INT, 0);

            //Reset offset-related values to default.
            this.numQuads = 0;
            this.offset = 0;
            this.hasRoom = true;
            //This is so we don't have to clear the verticies array every frame.
        }

        public final void render_noClear()
        {
            //Use Vertex Buffer Object to upload vertices to Shader.
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

            //Bind Vertex Array Object.
            glBindVertexArray(vertexArrayObject_ID);

            //Use the uploaded vertcies to render.
            glDrawElements(GL_TRIANGLES, this.numQuads * 6, GL_UNSIGNED_INT, 0);
        }

        public final void clear()
        {
            //Reset offset-related values to default.
            this.numQuads = 0;
            this.offset = 0;
            this.hasRoom = true;
        }
    }

    /**
     * Render Batch meant for Shadow Volumes.
     */
    private class ShadowSilhouette_RenderBatch extends SpriteBatch
    {
        private static final int
        POSITION_SIZE = 3,
        TEX_COORDS_SIZE = 2,
        TEX_SLOT_SIZE = 1,
        //
        POSITION_OFFSET = 0,
        TEX_COORDS_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES,
        TEX_SLOT_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES,
        //
        VERTEX_SIZE = 6,
        VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

        /**Constructor.*/
        public ShadowSilhouette_RenderBatch(int maxSpriteCount)
        {
            super(maxSpriteCount, VERTEX_SIZE);
        }

        public final void start()
        {
            super.start();

            //Create and enable vertex attribute pointers.
            glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, TEX_SLOT_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_SLOT_OFFSET);
            glEnableVertexAttribArray(2);
        }

        public final void addVertices(float position_x, float position_y, float position_z, float texX, float texY, int texSlot)
        {
            //Load Position.
            vertices[offset] = position_x;
            vertices[offset+1] = position_y;
            vertices[offset+2] = position_z;

            //Load Texture color Coordinates.
            vertices[offset+3] = texX;
            vertices[offset+4] = texY;

            //Load Texture Slot.
            vertices[offset+5] = texSlot;

            //Increment offset;
            offset += VERTEX_SIZE;

            //Increment Sprite count if needed.
            if(offset % (VERTEX_SIZE * 4) == 0)
            {
                numSprites++;
                if(numSprites >= this.maxSpriteCount){this.hasRoom = false;}
            }
        }
    }


    /**
     * Render Batch meant for lines.
     */
    private class Line_RenderBatch extends RenderBatch
    {
        private static final int
        POSITION_SIZE = 3,
        COLOR_SIZE = 4,
        //
        POSITION_OFFSET = 0,
        COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES,
        //
        VERTEX_SIZE = 7,
        VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

        protected int numLines = 0;
        protected final int maxLineCount;

        /**Constructor.*/
        public Line_RenderBatch(int maxLineCount)
        {
            super(maxLineCount * 2, VERTEX_SIZE);
            this.maxLineCount = maxLineCount;
        }

        @Override
        public void start()
        {
            //Generate and bind a Vertex Array Object.
            vertexArrayObject_ID = glGenVertexArrays();
            glBindVertexArray(vertexArrayObject_ID);

            //Generate and bind a Vertex Buffer Object.
            vertexBufferObject_ID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);


            //Create and enable vertex attribute pointers.
            glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
            glEnableVertexAttribArray(1);
        }

        public void addVertices(float position_x, float position_y, float position_z, Vector4f color)
        {
            //Load Position.
            vertices[offset] = position_x;
            vertices[offset+1] = position_y;
            vertices[offset+2] = position_z;

            //Load Color.
            vertices[offset+3] = color.x;
            vertices[offset+4] = color.y;
            vertices[offset+5] = color.z;
            vertices[offset+6] = color.w;

            //Increment offset.
            offset += VERTEX_SIZE;

            //Increment Sprite count if needed.
            if(offset % (VERTEX_SIZE * 2) == 0)
            {
                numLines++;
                if(numLines >= this.maxLineCount){this.hasRoom = false;}
            }
        }

        @Override
        /**Renders every line in this batch.*/
        public final void render()
        {
            //System.out.println("Rendering " + numLines);

            //Use Vertex Buffer Object to upload vertices to Shader.
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

            //Bind Vertex Array Object.
            glBindVertexArray(vertexArrayObject_ID);

            //Use the uploaded vertcies to render.
            glDrawArrays(GL_LINES, 0, this.numLines * 2);
            //glDrawArrays(GL_POINTS, 0, this.numLines * 2);
            this.numLines = 0;
            this.offset = 0;
            this.hasRoom = true;
        }
    }


    /**
     * Render Batch meant for circles.
     */
    private class Circle_RenderBatch extends RenderBatch
    {
        private static final int
        POSITION_SIZE = 3,
        COLOR_SIZE = 4,
        QUAD_COORDS_SIZE = 2,
        THICKNESS_SIZE = 3,
        //
        POSITION_OFFSET = 0,
        COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES,
        QUAD_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES,
        THICKNESS_OFFSET = QUAD_COORDS_OFFSET + QUAD_COORDS_SIZE * Float.BYTES,
        //
        VERTEX_SIZE = 10,
        VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

        protected int numCircles = 0;
        protected final int maxCircleCount;

        public Circle_RenderBatch(int maxCircleCount)
        {
            super(maxCircleCount * 4, VERTEX_SIZE);
            this.maxCircleCount = maxCircleCount;
        }

        @Override
        public void start()
        {
            //Generate and bind a Vertex Array Object.
            vertexArrayObject_ID = glGenVertexArrays();
            glBindVertexArray(vertexArrayObject_ID);

            //Generate and bind a Vertex Buffer Object.
            vertexBufferObject_ID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

            //Create and upload Index Buffer to GPU.
            elementBufferObject_ID = glGenBuffers();
            int[] indices = new int[6 * maxCircleCount];
            for(int index = 0; index < maxCircleCount; index++)
            {
                int offsetArrayIndex = 6 * index;
                int offset = 4 * index;

                //3, 2, 0, 0, 2, 1      7, 6, 4, 4, 6, 5
                //Triangle 1
                indices[offsetArrayIndex]   = offset + 0;
                indices[offsetArrayIndex+1] = offset + 1;
                indices[offsetArrayIndex+2] = offset + 2;

                //Triangle 2
                indices[offsetArrayIndex+3] = offset + 2;
                indices[offsetArrayIndex+4] = offset + 3;
                indices[offsetArrayIndex+5] = offset + 0;
            }
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferObject_ID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);


            //Create and enable vertex attribute pointers.
            glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, QUAD_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, QUAD_COORDS_OFFSET);
            glEnableVertexAttribArray(2);

            glVertexAttribPointer(3, THICKNESS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, THICKNESS_OFFSET);
            glEnableVertexAttribArray(3);
        }

        public void addVertices(float position_x, float position_y, float position_z, Vector4f color, float quadX, float quadY, float thickness)
        {
            //Load Position.
            vertices[offset] = position_x;
            vertices[offset+1] = position_y;
            vertices[offset+2] = position_z;

            //Load Color.
            vertices[offset+3] = color.x;
            vertices[offset+4] = color.y;
            vertices[offset+5] = color.z;
            vertices[offset+6] = color.w;

            //Load Quad Coordinates.
            vertices[offset+7] = quadX;
            vertices[offset+8] = quadY;

            //Load Quad size and Circle thickness.
            vertices[offset+9] = thickness;

            //Increment offset.
            offset += VERTEX_SIZE;

            //Increment Sprite count if needed.
            if(offset % (VERTEX_SIZE * 4) == 0)
            {
                numCircles++;
                if(numCircles >= this.maxCircleCount){this.hasRoom = false;}
            }
        }

        @Override
        public final void render()
        {
            //Use Vertex Buffer Object to upload vertices to Shader.
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject_ID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

            //Bind Vertex Array Object.
            glBindVertexArray(vertexArrayObject_ID);

            //Use the uploaded vertcies to render.
            glDrawElements(GL_TRIANGLES, this.numCircles * 6, GL_UNSIGNED_INT, 0);
            this.numCircles = 0;
            this.offset = 0;
            this.hasRoom = true;
        }
    }
    

    //RenderBatches.
    private final int BATCH_MAX_SPRITE_COUNT = 256;
    //
    private List<Basic_SpriteBatch> basic_Batches,
    basic2D_Batches, cropped_Basic2D_Batches,
    current_Basic2D_Batches = null;
    private List<Lighting_SpriteBatch> lighting_Batches, lightingTransperant_Batches;
    private List<ShadowVolume_RenderBatch> shadowVolume_Batches;
    private List<ShadowSilhouette_RenderBatch> shadowSilhouette_Batches;
    //
    private List<Line_RenderBatch> line_Batches, line2D_Batches;
    private List<Circle_RenderBatch> circle_Batches, circle2D_Batches;

    //Global Shader data.
    private float[] globalUniformData;

    //Light Data.
    private float[] lightData;
    private int[][] cellData = new int[2][];
    private int numLights;

    //Uniform Block Addresses.
    private int globalUniformBlock_UBO,
    lightsBlock_UBO, cellsBlock0_UBO, cellsBlock1_UBO;

    //FrameBuffer and RenderBuffer Addresses.
    private int output_FrameBuffer_Object = -1,
    output_FrameBuffer_Texture = -1, //diffuse_FrameBuffer_Texture = -1,
    color_FrameBuffer_Texture = -1,
    normal_FrameBuffer_Texture = -1,
    //
    output_RenderBuffer_Object = -1;

    //Matricies.
    private Matrix4f projectionMatrix = new Matrix4f(), viewMatrix = new Matrix4f();
    protected float[] projectionMatrixArray = new float[16], viewMatrixArray = new float[16];

    /**
     * Constructor.
     */
    public OpenGLScreen(int width, int height)
    {
        //Set Dimensions and Viewport.
        setDimensions(width, height, false);
        setViewportDimensions(0, 0, width, height);

        //Set lighting tile render functions.
        lighting_TileRenderFunctions[TileMesh.SHEARTYPE_FLOOR] = this::renderTile_shearFloor;
        lighting_TileRenderFunctions[TileMesh.SHEARTYPE_WALL] = this::renderTile_shearWall;
        lighting_TileRenderFunctions[TileMesh.SHEARTYPE_Y] = this::renderTile_shearY;
        lighting_TileRenderFunctions[TileMesh.SHEARTYPE_ZX] = this::renderTile_shearZX;
        lighting_TileRenderFunctions[TileMesh.SHEARTYPE_ZY] = this::renderTile_shearZY;
    }

    public void setDimensions(int w, int h, boolean API_Initialized)
    {
        //Width and Height.
        WIDTH = w; HEIGHT = h;

        //Set crop region.
        this.CROP_X = 0;
        this.CROP_Y = 0;
        this.CROP_WIDTH = WIDTH;
        this.CROP_HEIGHT = HEIGHT;

        //Base Projection Matrix.
        projectionMatrix.identity()
        //.m00(1.0f).m10(-0.5f).m01(0.5f).m11(0.5f)
        //.m20(0.75f)
        //.m21(-1.4667f)
        .m21(-((33f * 32f) / (float)h) * 0.5f)///<- ZY shear to represent the Z-Axis as y - (z/2).
        //.m21(-1.475f)
        .mul//Multiply by orthographic matrix.
        (
            
            new Matrix4f().identity().ortho
            (
                0, w,
                h, 0,
                -(16.5f * 32f), (16.5f * 32f)
                //-(32f * 32f), (32f * 32f)
            )
            //.reflect(0.0f, 0.0f, -1.0f, 0, 0, 0)
        );
        projectionMatrix.get(projectionMatrixArray);//Put in an array to upload to GPU.

        //System.out.println(-((33f * 32f) / (float)h) * 0.5f);


        //Upload new Height to GPU.
        if(lighting_ShaderProgram != null)
        {
            lighting_ShaderProgram.use();
            int lighting_uCurrent_scrHeight_Loc = glGetUniformLocation(lighting_ShaderProgram.getID(), "current_scrHeight");
            glUniform1i(lighting_uCurrent_scrHeight_Loc, HEIGHT);
            lighting_ShaderProgram.detach();
        }
        
        //System.out.println(projectionMatrix);


        //Screen dimensions float array.
        float[] SCR_DIMS = new float[]
        {
            0, 0, 0.0f, 1.0f,
            w, 0, 1.0f, 1.0f,
            w, h, 1.0f, 0.0f,
            //
            w, h, 1.0f, 0.0f,
            0, h, 0.0f, 0.0f,
            0, 0, 0.0f, 1.0f,
        };


        //Custom FrameBuffer, to avoid texture offset problems and improve performance at higher resolutions.
        {
            //
            //Create Output FrameBuffer.
            //If one hasn't already been made, make one. Bind it.
            //
            if(output_FrameBuffer_Object == -1){output_FrameBuffer_Object = glGenFramebuffers();}
            glBindFramebuffer(GL_FRAMEBUFFER, output_FrameBuffer_Object);

            //Create FrameBuffer Textures.
            output_FrameBuffer_Texture = createFramebufferTexture(output_FrameBuffer_Texture, GL_COLOR_ATTACHMENT0);
            color_FrameBuffer_Texture = createFramebufferTexture(color_FrameBuffer_Texture, GL_COLOR_ATTACHMENT1);
            normal_FrameBuffer_Texture = createFramebufferTexture(normal_FrameBuffer_Texture, GL_COLOR_ATTACHMENT2);
            glBindTexture(GL_TEXTURE_2D, 0);//Unbind Texture for now.

            //Enable other render targets so the shaders can draw stuff in them.
            glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2});

            //Put screen dimensions into the Deferred VAO.
            glBindVertexArray(deferred_frameBuffer_VAO);
            glBindBuffer(GL_ARRAY_BUFFER, deferred_frameBuffer_VBO);

            //TODO Why is this invalid?
            //glBufferData(GL_ARRAY_BUFFER, SCR_DIMS, GL_STATIC_DRAW);
            //System.out.println("screen error: " + GL33.glGetError());

            //Unbind vertex array for now.
            glBindVertexArray(0);
            

            //
            //RenderBuffer, used as the Depth and Stencil buffers.
            //
            /*
            if(output_RenderBuffer_Object == -1){output_RenderBuffer_Object = glGenRenderbuffers();}
            glBindRenderbuffer(GL_RENDERBUFFER, output_RenderBuffer_Object);

            //Set the RenderBuffer up as a Depth and Stencil buffer.
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, w, h);

            //Unbind RenderBuffer for now.
            glBindRenderbuffer(GL_RENDERBUFFER, 0);

            //Set it to the FrameBuffer.
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, output_RenderBuffer_Object);
            */


            //
            if(output_RenderBuffer_Object == -1){output_RenderBuffer_Object = glGenTextures();}
            glBindTexture(GL_TEXTURE_2D, output_RenderBuffer_Object);

            //(Re)Create texture.
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, w, h, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, (int[])null);

            //Set texture parameters.
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            //Apply texture as Depth and Stencil buffers.
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, output_RenderBuffer_Object, 0);

            //Unbind Texture for now.
            glBindTexture(GL_TEXTURE_2D, 0);
            //

            
            //Completion check for debugging purposes.
            int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
            if(status != GL_FRAMEBUFFER_COMPLETE)
            {System.err.println("FRAMEBUFFER INCOMPLETE: " + status);}
        }

        
        //Go back to default framebuffer.
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private int createFramebufferTexture(int inputID, int attachmentSlot)
    {
        int outputID = inputID;

        if(inputID == -1){outputID = glGenTextures();}
        glBindTexture(GL_TEXTURE_2D, outputID);

        //(Re)Create texture.
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, WIDTH, HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, (int[])null);

        //Set texture parameters.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //Apply texture to the given Atachment slot.
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentSlot, GL_TEXTURE_2D, outputID, 0);

        return outputID;
    }


    //VAO and VBO addressess for Deferred Rendering paasses.
    private int deferred_frameBuffer_VAO, deferred_frameBuffer_VBO;

    //VAO and VBO addresses for rendering Output FrameBuffer.
    private int output_frameBuffer_VAO, output_frameBuffer_VBO;

    //Array for rendering framebuffer.
    private final float[] SCR_VERTS =
    {
        -1.0f, -1.0f, 0.0f, 0.0f,
        //-1.1f, -1.1f, 0.0f, 0.0f,
        1.0f, -1.0f, 1.0f, 0.0f,
        //0.9f, -1.1f, 1.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f,
        //0.9f, 0.9f, 1.0f, 1.0f,
        //
        1.0f, 1.0f, 1.0f, 1.0f,
        //0.9f, 0.9f, 1.0f, 1.0f,
        -1.0f, 1.0f, 0.0f, 1.0f,
        //-1.1f, 0.9f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f
        //-1.1f, -1.1f, 0.0f, 0.0f
    };

    //Lighting Shadar uniform locations.
    private int lightingShader_uOffsets, lightingShader_uDoingTransperant;

    /**Initialization functions.*/
    public void init()
    {
        //
        //Compile FrameBuffer Shader.
        //
        frameBuffer_ShaderProgram = new ShaderProgram("Deffered_Vert", "FrameBuffer_Frag");

        //Create a Vertex Array Object for the custom framebuffer texture.
        output_frameBuffer_VAO = glGenVertexArrays();
        glBindVertexArray(output_frameBuffer_VAO);

        //Put SCR_VERTS into a Vertex Buffer Object into the VAO.
        output_frameBuffer_VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, output_frameBuffer_VBO);
        glBufferData(GL_ARRAY_BUFFER, SCR_VERTS, GL_STATIC_DRAW);

        //Create and enable attribute pointers.
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        //
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        //Unbind VAO.
        glBindVertexArray(0);


        //
        //Compile CopyToColor shader.
        //
        copyToColor_ShaderProgram = new ShaderProgram("Deffered_Vert", "CopyToColor_Frag");


        //
        //Compile Basic Shader.
        //
        basic_ShaderProgram = new ShaderProgram("Basic_Vert", "Basic_Frag");
        basic_ShaderProgram.use();
        int basicShader_ID = basic_ShaderProgram.getID();
        
        //Upload Texture Slots to Basic Shader.
        int basic_uTex = glGetUniformLocation(basic_ShaderProgram.getID(), "uTextures");
        glUniform1iv(basic_uTex, TEX_SLOTS);

        //Create Basic_SpriteBatch ArrayLists.
        basic_Batches = new ArrayList<Basic_SpriteBatch>();
        basic2D_Batches = new ArrayList<Basic_SpriteBatch>();
        cropped_Basic2D_Batches = new ArrayList<Basic_SpriteBatch>();

        //Set current basic batch list.
        current_Basic2D_Batches = basic2D_Batches;

        //Bind Global Uniform Buffer Object.
        int[] blockID_container = new int[1];
        this.globalUniformData = createUBO_Float(basicShader_ID, "GlobalBlock", 0, blockID_container);
        this.globalUniformData = new float[25];
        this.globalUniformBlock_UBO = blockID_container[0];

        //Upload Inverse projection matrix to GlobalUniformBlock.
        projectionMatrix.invert(viewMatrix);
        //projectionMatrix.translate(-WIDTH / 2.0f, -HEIGHT / 2.0f, 0.0f, viewMatrix);
        //viewMatrix.invert();
        viewMatrix.get(viewMatrixArray);
        glBindBuffer(GL_UNIFORM_BUFFER, globalUniformBlock_UBO);
        //System.out.println(viewMatrixArray[0]);
        glBufferSubData(GL_UNIFORM_BUFFER, 28 * 4, viewMatrixArray);


        //
        //Compile Lighting Shader.
        //
        lighting_ShaderProgram = new ShaderProgram("Lighting_Vert", "Lighting_Frag");
        lighting_ShaderProgram.use();
        int lightingShader_ID = lighting_ShaderProgram.getID();

        //Upload Texture Slots to Lighting Shader.
        int lighting_uTex = glGetUniformLocation(lightingShader_ID, "uTextures");
        glUniform1iv(lighting_uTex, TEX_SLOTS);

        //Upload Screen Height.
        int lighting_uCurrent_scrHeight_Loc = glGetUniformLocation(lightingShader_ID, "uCurrent_scrHeight");
        glUniform1i(lighting_uCurrent_scrHeight_Loc, HEIGHT);

        //Get uOffsets location.
        lightingShader_uOffsets = glGetUniformLocation(lightingShader_ID, "uOffsets");

        //Get uDoingTransperant location.
        lightingShader_uDoingTransperant = glGetUniformLocation(lightingShader_ID, "uDoingTransperant");

        //Create Lighting_SpriteBatch ArrayLists.
        lighting_Batches = new ArrayList<Lighting_SpriteBatch>();
        lightingTransperant_Batches = new ArrayList<Lighting_SpriteBatch>();

        //Bind Uniform Buffer Objects for Light and Light-Cell data.
        this.lightData = createUBO_Float(lightingShader_ID, "LightsBlock", 1, blockID_container);
        this.lightsBlock_UBO = blockID_container[0];

        this.cellData[0] = createUBO_Int(lightingShader_ID, "CellsBlock_0", 2, blockID_container);
        this.cellsBlock0_UBO = blockID_container[0];

        this.cellData[1] = createUBO_Int(lightingShader_ID, "CellsBlock_1", 3, blockID_container);
        this.cellsBlock1_UBO = blockID_container[0];


        //
        //Compile ShadowVolume Shader.
        //
        shadowVolume_ShaderProgram = new ShaderProgram("ShadowVolume_Vert", "ShadowVolume_Frag");
        shadowVolume_ShaderProgram.use();

        //Create ShadowVolume_RenderBatch ArrayLists.
        shadowVolume_Batches = new ArrayList<ShadowVolume_RenderBatch>();


        //
        //Compile ShadowSilhouete Shader.
        //
        shadowSilhouette_ShaderProgram = new ShaderProgram("ShadowSilhouette_Vert", "ShadowSilhouette_Frag");
        shadowSilhouette_ShaderProgram.use();
        int shadowSilhouetteShader_ID = shadowSilhouette_ShaderProgram.getID();

        //Create ShadowVolume_RenderBatch ArrayLists.
        shadowSilhouette_Batches = new ArrayList<ShadowSilhouette_RenderBatch>();

        //Upload Texture Slots to Shadow Shader.
        int shadowSilhouette_uTex = glGetUniformLocation(shadowSilhouetteShader_ID, "uTextures");
        glUniform1iv(shadowSilhouette_uTex, TEX_SLOTS);


        //
        //Compile Diffuse Shader.
        //
        diffuse_ShaderProgram = new ShaderProgram("Deffered_Vert", "Diffuse_Frag");
        diffuse_ShaderProgram.use();
        int diffuseShader_ID = diffuse_ShaderProgram.getID();

        //Upload Texture Slots to Diffuse Shader.
        int diffuse_u = glGetUniformLocation(diffuseShader_ID, "uColor");
        glUniform1i(diffuse_u, 0);
        diffuse_u = glGetUniformLocation(diffuseShader_ID, "uNormal");
        glUniform1i(diffuse_u, 1);

        //Create a Vertex Array Object for the custom framebuffer texture.
        deferred_frameBuffer_VAO = glGenVertexArrays();
        glBindVertexArray(deferred_frameBuffer_VAO);

        //Put screen dimensions into a Vertex Buffer Object into the VAO.
        deferred_frameBuffer_VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, deferred_frameBuffer_VBO);
        glBufferData
        (
            GL_ARRAY_BUFFER,
            new float[]
            {
                0, 0, 0.0f, 1.0f,
                WIDTH, 0, 1.0f, 1.0f,
                WIDTH, HEIGHT, 1.0f, 0.0f,
                //
                WIDTH, HEIGHT, 1.0f, 0.0f,
                0, HEIGHT, 0.0f, 0.0f,
                0, 0, 0.0f, 1.0f,
            },
            GL_STATIC_DRAW
        );

        //Create and enable attribute pointers.
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        //
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        //Unbind VAO.
        glBindVertexArray(0);


        //
        //Compile Reflection Shader.
        //
        reflection_ShaderProgram = new ShaderProgram("Deffered_Vert", "Reflection_Frag");
        reflection_ShaderProgram.use();
        int reflectionShader_ID = reflection_ShaderProgram.getID();

        //Upload Texture Slots to Diffuse Shader.
        int reflection_u = glGetUniformLocation(reflectionShader_ID, "uScreen");
        glUniform1i(reflection_u, 0);
        reflection_u = glGetUniformLocation(reflectionShader_ID, "uDepthBuffer");
        glUniform1i(reflection_u, 1);
        reflection_u = glGetUniformLocation(reflectionShader_ID, "uNormal");
        glUniform1i(reflection_u, 2);



        //
        //Compile Line Shader.
        //
        line_ShaderProgram = new ShaderProgram("Line_Vert", "Line_Frag");

        //Create Line_RenderBatch ArrayLists.
        line_Batches = new ArrayList<Line_RenderBatch>();
        line2D_Batches = new ArrayList<Line_RenderBatch>();


        //
        //Compile Circle Shader.
        //
        circle_ShaderProgram = new ShaderProgram("Circle_Vert", "Circle_Frag");

        //Create Circle_RenderBatch ArrayLists.
        circle_Batches = new ArrayList<Circle_RenderBatch>();
        circle2D_Batches = new ArrayList<Circle_RenderBatch>();
    }



    /**Create Uniform Buffer Object with float data.*/
    private float[] createUBO_Float(final int shaderID, final String blockName, final int binding, final int[] blockID_container)
    {
        //Get buffer object index.
        int blockIndex = glGetUniformBlockIndex(shaderID, blockName);
        
        //Create array with buffer size.
        int[] blockSize = new int[1];
        glGetActiveUniformBlockiv(shaderID, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, blockSize);
        float[] floatData = new float[blockSize[0] / Float.BYTES];
        System.out.println("BlockSize " + shaderID + " " + binding + ": " + blockSize[0] + " " + floatData.length);
        
        //Create buffer.
        int block_UBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, block_UBO);
        glBufferData(GL_UNIFORM_BUFFER, floatData, GL_DYNAMIC_DRAW);

        //Set buffer binding value.
        glUniformBlockBinding(shaderID, blockIndex, binding);
        glBindBufferBase(GL_UNIFORM_BUFFER, binding, block_UBO);

        //Unbind buffer for now and return buffer address and array.
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        blockID_container[0] = block_UBO;
        return floatData;
    }

     /**Create Uniform Buffer Object with int data.*/
     private int[] createUBO_Int(int shaderID, String blockName, int binding, final int[] blockID_container)
    {
        //Get buffer object index.
        int blockIndex = glGetUniformBlockIndex(shaderID, blockName);
        
        //Create array with buffer size.
        int[] blockSize = new int[1];
        glGetActiveUniformBlockiv(shaderID, blockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, blockSize);
        int[] intData = new int[blockSize[0] / Integer.BYTES];
        //System.out.println("BlockSize " + blockSize[0] + " " + intData.length);
        
        //Create buffer.
        int block_UBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, block_UBO);
        glBufferData(GL_UNIFORM_BUFFER, intData, GL_DYNAMIC_DRAW);

        //Set buffer binding value.
        glUniformBlockBinding(shaderID, blockIndex, binding);
        glBindBufferBase(GL_UNIFORM_BUFFER, binding, block_UBO);

        //Unbind buffer for now and return buffer address and array.
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        blockID_container[0] = block_UBO;
        return intData;
    }


    @Override
    /**Adds a Light to rendering and calculates which cells of the screen the light takes up.*/
    public void addLight(Light light, float scale)
    {
        //Don't add the light if the array is full.
        if(numLights >= Screen.MAX_LIGHTS_ON_SCREEN){return;}

        //Offset in lightData array.
        int offset = numLights * Screen.LIGHT_SIZE;

        //Get light position.
        Vector3f position = light.getPosition3f();

        //Diffuse Color.
        Vector3f diffuse = light.getDiffuseColor();
        lightData[offset+10] = diffuse.x;
        lightData[offset+11] = diffuse.y;
        lightData[offset+12] = diffuse.z;

        //Ambient Color.
        Vector3f ambient = light.getAmbientColor();
        lightData[offset+13] = ambient.x;
        lightData[offset+14] = ambient.y;
        lightData[offset+15] = ambient.z;

        //Area Light case.
        if(light instanceof AreaLight)
        {
            AreaLight aLight = (AreaLight)light;
            AAB_RoundedBox rBox = aLight.getRBox();

            //Position and outer radius.
            lightData[offset] = ((position.x + rBox.baseLeftFloat()) * scale) - xOffset;
            lightData[offset+1] = ((position.y + rBox.baseBackFloat()) * scale) - yOffset;
            lightData[offset+2] = ((position.z + rBox.baseBottomFloat()) * scale) - zOffset;
            lightData[offset+3] = (rBox.getCornerRadiusFloat() * scale);

            //Dimensions.
            lightData[offset+4] = rBox.getBaseWidthFloat() * scale;
            lightData[offset+5] = rBox.getBaseHeightFloat() * scale;
            lightData[offset+6] = rBox.getBaseDepthFloat() * scale;

            //Used sides.
            lightData[offset+7] = aLight.getUseX();
            lightData[offset+8] = aLight.getUseY();
            lightData[offset+9] = aLight.getUseZ();
        }
        //Directional Light case.
        else if(light instanceof DirectionalLight)
        {
            DirectionalLight dLight = (DirectionalLight)light;
            SphericalSector sector = dLight.getSphericalSector();

            //Position and outer radius.
            lightData[offset] = (position.x * scale) - xOffset;
            lightData[offset+1] = (position.y * scale) - yOffset;
            lightData[offset+2] = (position.z * scale) - zOffset;
            lightData[offset+3] = (dLight.getOuterLengthFloat() * scale);

            //Cutoff Angles and length.
            lightData[offset+4] = dLight.getCosInnerAngle();
            lightData[offset+5] = sector.getCosAngle();
            lightData[offset+6] = (dLight.getLengthFloat() * scale);

            //Direction.
            Vector3f dir = sector.getDirection3f();
            lightData[offset+7] = dir.x;
            lightData[offset+8] = dir.y;
            lightData[offset+9] = dir.z;
        }


        //Calulate used cells.
        Shape3D shape = light.getShape();
        //
        int left = (int)( ((position.x + (shape.f_left() >> 8) ) * scale) - xOffset ) / Screen.CELL_SIZE;
        int right = (int)(( ((position.x + (shape.f_right() >> 8) ) * scale) - xOffset ) / Screen.CELL_SIZE) + 1;
        if(left < 0){left = 0;}
        if(right >= Game.BATTLE_WIDTH / Screen.CELL_SIZE){right = (Game.BATTLE_WIDTH / Screen.CELL_SIZE) - 1;}
        //
        int back = (int)( (((position.y + (shape.f_back() >> 8) ) * scale) - yOffset) - ((((position.z + (shape.f_top() >> 8) ) * scale) - zOffset) / 2) ) / Screen.CELL_SIZE;
        int front = (int)(( (((position.y + (shape.f_front() >> 8) ) * scale) - yOffset) - ((((position.z + (shape.f_bottom() >> 8) ) * scale) - zOffset) / 2) ) / Screen.CELL_SIZE) + 1;
        if(back < 0){back = 0;}
        if(front >= Game.BATTLE_HEIGHT / Screen.CELL_SIZE){front = (Game.BATTLE_HEIGHT / Screen.CELL_SIZE);}
        //System.out.println(back + " " + front + " " + (Game.BATTLE_HEIGHT / Screen.CELL_SIZE));
        //
        for(int y = back; y < front; y++)
        {
            for(int x = left; x < right; x++)
            {
                //What cell number is this?
                int arrayNum = 0,
                cellIndex = (x + (y * (Game.BATTLE_WIDTH / CELL_SIZE))) << 2;//* 4;

                //Which array?
                if(cellIndex >= cellData[0].length)
                {
                    arrayNum = 1;
                    cellIndex -= cellData[0].length;
                }

                //How many area and directional lights are in this cell [obtained from first two nibbles of first int]
                int cellNum = cellData[arrayNum][cellIndex];
                int area_Lights = cellNum & 0x0000000F;
                int directional_Lights = (cellNum & 0x000000F0) >> 4;

                //System.out.println(area_Lights);

                //If the light cap has not been reached for this cell...
                if(area_Lights + directional_Lights < MAX_LIGHTS_PER_CELL)
                {
                    if(light instanceof AreaLight)
                    {
                        //System.out.println(directional_Lights + area_Lights + " " + area_Lights);

                        //Iterate from newest directional light to oldest.
                        for(int d = (directional_Lights + area_Lights); d > area_Lights; d--)
                        {
                            //How many bits over?
                            int dOffset = 8 + (d << 3);//(d * 8);
                            int dIntNum = dOffset >> 5;// / 32;
                            dOffset %= 32;

                            //Int in cell and portion of int.
                            int c = cellData[arrayNum][cellIndex + dIntNum];
                            int portion = c & (0x000000FF << dOffset);

                            //System.out.println(dIntNum);

                            if(dOffset >= 24)
                            {
                                //Push bytes of next int over to put portion in.
                                //if(cellIndex + dIntNum+1 >= cellData[arrayNum].length)
                                //{
                                    //cellData[arrayNum+1][0] =
                                    //((cellData[arrayNum+1][0] & 0xFFFFFF00) | (portion >> dOffset));
                                //}
                                //else
                                //{ 
                                    cellData[arrayNum][cellIndex + dIntNum+1] =
                                    ((cellData[arrayNum][cellIndex + dIntNum+1] & 0xFFFFFF00) | (portion >> dOffset));
                                //}

                                //Remove portion from the previous int.
                                cellData[arrayNum][cellIndex + dIntNum] = (c & 0x00FFFFFF);
                            }

                            else if(dOffset >= 16)
                            {
                                int c0 = (c & 0x0000FFFF);
                                cellData[arrayNum][cellIndex + dIntNum] = (portion << 8) | c0;
                            }
                            else if(dOffset >= 8)
                            {
                                int c0 = (c & 0xFF0000FF);
                                cellData[arrayNum][cellIndex + dIntNum] = (portion << 8) | c0;
                            }
                            else if(dOffset >= 0)
                            {
                                int c0 = (c & 0xFFFF0000);
                                cellData[arrayNum][cellIndex + dIntNum] = (portion << 8) | c0;
                            }

                            /*
                            else
                            {
                                //Remove portion from its original position.
                                int c0 = (c & (0xFFFF0000 << dOffset));

                                //Move it over.
                                cellData[arrayNum][cellIndex + dIntNum] = (portion << 8) | c0;
                            }
                            */
                        }

                        //Get first available spot.
                        int availableBits = 8 + (area_Lights * 8);
                        int availableInt = availableBits / 32;
                        availableBits %= 32;

                        //Put the new ID in.
                        cellData[arrayNum][cellIndex + availableInt] |= (numLights << availableBits);

                        //Increment area lights.
                        area_Lights++;
                    }
                    else if(light instanceof DirectionalLight)
                    {
                        //Get first available spot.
                        int availableBits = 8 + ((area_Lights + directional_Lights) * 8);
                        int availableInt = availableBits / 32;
                        availableBits %= 32;

                        //Put the new ID in.
                        cellData[arrayNum][cellIndex + availableInt] |= (numLights << availableBits);

                        //Increment.
                        directional_Lights++;
                    }

                    //Update first int with incremented area/directional light value.
                    cellData[arrayNum][cellIndex] = (cellData[arrayNum][cellIndex] & 0xFFFFFF00) | ((directional_Lights << 4) | area_Lights);
                }
            }
        }

        //Increment light count.
        numLights++;
    }

    float[] globalLight_Array = new float[9];

    @Override
    /**Sets the currently used Global Light.*/
    public void setGlobalLight(Vector3f direction, Vector3f diffuse, Vector3f ambient)
    {
        //Set Direction.
        globalLight_Array[0] = direction.x;
        globalLight_Array[1] = direction.y;
        globalLight_Array[2] = direction.z;

        //Set Diffuse.
        globalLight_Array[3] = diffuse.x;
        globalLight_Array[4] = diffuse.y;
        globalLight_Array[5] = diffuse.z;

        //Set Ambient.
        globalLight_Array[6] = ambient.x;
        globalLight_Array[7] = ambient.y;
        globalLight_Array[8] = ambient.z;
    }



    private float[] offsets_Array = new float[3];

    @Override
    public void sync(){glFinish();}
    
    @Override
    public void render(long windowAddress)
    {
        //Calculate View Matrix.
        //projectionMatrix.translate(-xOffset, -yOffset + (zOffset/2), -zOffset, viewMatrix);
        projectionMatrix.translate(-xOffset, -yOffset, -zOffset, viewMatrix);
        viewMatrix.get(viewMatrixArray);

        //Add view matrix to array.
        for(int i = 0; i < viewMatrixArray.length; i++)
        {globalUniformData[i] = viewMatrixArray[i];}

        //Add global light data to array.
        for(int i = 0; i < globalLight_Array.length; i++)
        {globalUniformData[i+16] = globalLight_Array[i];}

        //Upload it to GlobalUniformBlock.
        glBindBuffer(GL_UNIFORM_BUFFER, globalUniformBlock_UBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, globalUniformData);


        //Use custom FrameBuffer and set viewport to fixed size.
        //glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer_Object);
        glViewport(0, 0, WIDTH, HEIGHT);

        //Enable depth testing.
        glEnable(GL_DEPTH_TEST);

        //Disable writing to the Stencil Buffer.
        glStencilMask(0x00);
        

        //
        //Opaque lighting pass.
        //
        lighting_ShaderProgram.use();

        //Upload Light data.
        //System.out.println(lightData[LIGHT_SIZE]);
        glBindBuffer(GL_UNIFORM_BUFFER, lightsBlock_UBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, lightData);

        //Upload first array of Light-Cell data.
        //System.out.println(Integer.toHexString(cellData[0][0]));
        glBindBuffer(GL_UNIFORM_BUFFER, cellsBlock0_UBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, cellData[0]);

        //Upload second array of Light-Cell data.
        glBindBuffer(GL_UNIFORM_BUFFER, cellsBlock1_UBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, cellData[1]);

        //Unbind UBO for now.
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        //Reset numLights and cells.
        numLights = 0;
        for(int i0 = 0; i0 < cellData.length; i0++)
        {
            for(int i1 = 0; i1 < cellData[i0].length; i1++)
            {cellData[i0][i1] = 0;}
        }

        //Upload position offests.
        offsets_Array[0] = xOffset;
        offsets_Array[1] = yOffset;
        offsets_Array[2] = zOffset;
        glUniform3fv(lightingShader_uOffsets, offsets_Array);

        //Upload every Opaque Lighting RenderBatch.
        for(Lighting_SpriteBatch batch : lighting_Batches)
        {
            batch.render();
            //batch.clear();
        }   


        //
        //Use ShadowVolume Shader for Global Light.
        //
        shadowVolume_ShaderProgram.use();

        //Inform OpenGL that we're only using the main output buffer render target.
        glDrawBuffers(GL_COLOR_ATTACHMENT0);
        //Flickering caused by reading from and writing to the Color Buffer simultaniously would occur otherwise.
        
        glColorMask(false, false, false, false);//Disable writing to the Output buffer.
        glDepthMask(false);//Disable writing to Depth buffer.
        glStencilMask(0xFF);//Enable writing to the Stencil Buffer.

        //Vertex clockwise orientation is used to determine facing.
        //
        //For every front face that passes the depth test, increment the stencil value.
        //For every back face that passes the depth test, decrement the stencil value.
        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_INCR_WRAP);
        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_KEEP, GL_DECR_WRAP);

        //glCullFace(GL_BACK);
        //glCullFace(GL_FRONT);
        //glDisable(GL_CULL_FACE);

        //Upload every Global Light Shadow-Volume Batch. Behaviour depends on if there are transperant sprites or not.
        boolean weHaveTransperants = false;
        if(lightingTransperant_Batches.size() > 0)
        {
            Lighting_SpriteBatch ls0 = lightingTransperant_Batches.get(0);
            weHaveTransperants = (ls0 != null && ls0.sheets_size() > 0);
        }
        if(weHaveTransperants)
        {
            //We're going to reuse the volumes later, so don't clear them.
            for(ShadowVolume_RenderBatch batch : shadowVolume_Batches){batch.render_noClear();}
        }  
        else
        {
            //We only need them here. Render and clear them.
            for(ShadowVolume_RenderBatch batch : shadowVolume_Batches){batch.render();}
        }

        //Set the stencil test to pass when the value equals zero.
        glStencilFunc(GL_EQUAL, 0, 0xFF);
        //Pixels where the stencil value equals zero are where there are no shadows.
        

        
        //
        //Use ShadowSilhouette Shader for Global Light.
        //
        shadowSilhouette_ShaderProgram.use();

        //Where the depth tests fails (it will be slightly offset in the vertex shader), increment the stencil value.
        glStencilOp(GL_KEEP, GL_INCR_WRAP, GL_KEEP);

        if(weHaveTransperants)
        {
            //We're going to reuse the silhouettes later, so don't clear them.
            for(ShadowSilhouette_RenderBatch batch : shadowSilhouette_Batches){batch.render_noClear();}
        }  
        else
        {
            //We only need them here. Render and clear them.
            for(ShadowSilhouette_RenderBatch batch : shadowSilhouette_Batches){batch.render();}
        }

        //glDepthFunc(GL33.GL_LEQUAL);
        


        //
        //Use Diffuse Shader to apply diffuse light to non-shadowed pixels.
        //
        diffuse_ShaderProgram.use();

        //Upload projection matrix.
        glBindBuffer(GL_UNIFORM_BUFFER, globalUniformBlock_UBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projectionMatrixArray);

        //Unbind UBO for now.
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        //Set Blend Mode to add colors.
        //glBlendFunc(GL_ONE, GL_ONE);
        glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ZERO, GL_ZERO);
        //glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ZERO, GL_ZERO);
        //glBlendFunc(GL_ZERO, GL_ZERO);
        //glBlendEquation(mode);
        //Blend equation is now: SRC_RGB + DST_RGB, SRC_ALPHA

        
        glColorMask(true, true, true, true);//Enable writing to the Output Buffer.     
        glDisable(GL_DEPTH_TEST);//Disable Depth testing.
        glStencilOp(GL_ZERO, GL_ZERO, GL_ZERO);//Use this pass to clear the stencil buffer.
        //The stencil test currently passes when the value equals 0. (Is not in shadow)

        //Bind Deferred Vertex Array Object and Textures.
        //glBindVertexArray(deferred_frameBuffer_VAO);
        glBindVertexArray(output_frameBuffer_VAO);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, color_FrameBuffer_Texture);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, normal_FrameBuffer_Texture);

        //Render the quad.
        glDrawArrays(GL_TRIANGLES, 0, 6);
        //This will perform the stencil test on every pixel on the screen and
        //will apply diffuse lighting where the value is 0.

        //glColorMask(true, true, true, true);



        //
        //Shadow-Casting lights
        //

        /*
        if(  > 0)
        {
            //We're gonna be removing diffuse colors from the output image here.
            glBlendEquation(GL33.GL_FUNC_SUBTRACT);
            //Blend equation is now: SRC_RGB - DST_RGB, SRC_ALPHA
            
            //Enable writing to the Stencil Buffer.
            glStencilMask(0xFF);

            //Unfortunatly, one draw call per shadow-casting light.
            for(int i = 0; i < ; i++)
            {
                //
                //Fill stencil buffer.
                //
                glColorMask(false, false, false, false);//Disable writing to Color buffer.
                glEnable(GL_DEPTH_TEST);//ReEnable Depth testing.
                
                //Make the stencil test always pass and Set stencil operations as before.
                glStencilFunc(GL_ALWAYS, 0, 0xFF);
                glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_INCR_WRAP);
                glStencilOpSeparate(GL_BACK, GL_KEEP, GL_KEEP, GL_DECR_WRAP);

                //Upload every Shadow-Volume Batch associated with this light.
                for(Shadow_RenderBatch batch : shadow_Batches){batch.render();}


                //
                //Deferred render pass.
                //
                glColorMask(true, true, true, true);//Enable writing to Color Buffer.
                glDisable(GL_DEPTH_TEST);//Disable Depth testing.

                //Set stencil operation to clear stencil value and set the test to pass when the value doesn't equal zero.
                glStencilOp(GL_ZERO, GL_ZERO, GL_ZERO);
                glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
                //This is to clear the stencil buffer for the next light.

                //TODO Calculate the region this light takes up to reduce fragment shader calls.

                //Render the region.
                glDrawArrays(GL_TRIANGLES, 0, 6);
            }

            //Set Blend Equation back to additive blending.
            glBlendEquation(GL33.GL_FUNC_ADD);
        }
        */

        //Set Blend Function back to normal.
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFuncSeparate(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA, GL33.GL_ONE, GL33.GL_ONE_MINUS_SRC_ALPHA);
        //Blend equation is now: (SRC_RGB * SRC_ALPHA) + (DST_RGB * 1-SRC_ALPHA), SRC_ALPHA + (DST_ALPHA * 1-SRC_ALPHA)

        
        //
        //Transperant lighting pass.
        //
        if(weHaveTransperants)
        {
            //
            //Use copy to color shader.
            //
            copyToColor_ShaderProgram.use();

            //ReEnable the other color attaachments.
            glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2});

            //Use this pass to clear the stencil buffer.
            glStencilOp(GL_ZERO, GL_ZERO, GL_ZERO);
            glStencilFunc(GL_ALWAYS, 0, 0xFF);

            //Enable writing to the Stencil Buffer.
            glStencilMask(0xFF);

            //Copy output buffer to color buffer.
            glBindVertexArray(output_frameBuffer_VAO);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, output_FrameBuffer_Texture);
            glDrawArrays(GL_TRIANGLES, 0, 6);


            //
            //Use Lighting Shader.
            //
            lighting_ShaderProgram.use();

            //Upload view matrix.
            glBindBuffer(GL_UNIFORM_BUFFER, globalUniformBlock_UBO);
            glBufferSubData(GL_UNIFORM_BUFFER, 0, viewMatrixArray);

            //We are doing transperant sprites.
            glUniform1i(lightingShader_uDoingTransperant, 1);

            //ReEnable Depth testing and writing.
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
            //glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
            //glStencilMask(0x00);

            //Upload every Transperant Lighting batch.
            for(Lighting_SpriteBatch batch : lightingTransperant_Batches)
            {
                batch.render();
                //batch.clear();
            }

            //And now we're not.
            glUniform1i(lightingShader_uDoingTransperant, 0);

            //
            //Refractions and Reflections (if there are any).
            //
            //if(reflectionRegions.size() > 0)
            //{
                //Use refraction shader.
                //refraction_ShaderProgram.use();

                //Use reflection shader.
                //reflection_ShaderProgram.use();

                //Go through every ReflectionRegion.
                //for(int r = 0; r < reflectionRegions.size(); r++)
                //{
                    //Upload every Reflection renderBatch.
                    //for(Reflection_SpriteBatch batch : reflection_Batches)
                    //{
                        //batch.render();
                        ////batch.clear();
                    //}
                //}
            //}
           

            //
            //Use ShadowVolume Shader for Global Light.
            //
            shadowVolume_ShaderProgram.use();

            //Inform OpenGL that we're just using the output buffer, like before.
            glDrawBuffers(GL_COLOR_ATTACHMENT0);

            glColorMask(false, false, false, false);//Disable writing to Color buffer.
            glDepthMask(false);//Disable writing to depth buffer.
            glStencilMask(0xFF);//Enable writing to the Stencil Buffer.

            //Vertex clockwise orientation is used to determine facing.
            //
            //For every front face that passes the depth test, increment the stencil value.
            //For every back face that passes the depth test, decrement the stencil value.
            glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_INCR_WRAP);
            glStencilOpSeparate(GL_BACK, GL_KEEP, GL_KEEP, GL_DECR_WRAP);

            //Upload every Global Light Shadow-Volume Batch.
            for(ShadowVolume_RenderBatch batch : shadowVolume_Batches){batch.render();}

            //Set the stencil test to pass when the value equals zero.
            glStencilFunc(GL_EQUAL, 0, 0xFF);


            
            //
            //Use ShadowSilhouette Shader for Global Light.
            //
            shadowSilhouette_ShaderProgram.use();

            //Where the depth tests fails (it will be slightly offset in the vertex shader), increment the stencil value.
            glStencilOp(GL_KEEP, GL_INCR_WRAP, GL_KEEP);

            //Upload every Global Light Shadow-Silhouette Batch.
            for(ShadowSilhouette_RenderBatch batch : shadowSilhouette_Batches){batch.render();}
            


            //
            //Use FrameBuffer Shader to apply diffuse light pixels to non-shadowed pixels.
            //
            frameBuffer_ShaderProgram.use();

            //Upload projection matrix.
            glBindBuffer(GL_UNIFORM_BUFFER, globalUniformBlock_UBO);
            glBufferSubData(GL_UNIFORM_BUFFER, 0, projectionMatrixArray);

            //Unbind UBO for now.
            glBindBuffer(GL_UNIFORM_BUFFER, 0);
            //We do not need to change the blend equation since we're just putting colors on top.

            
            glColorMask(true, true, true, true);//Enable writing to Color Buffer.
            glDisable(GL_DEPTH_TEST);//Disable Depth testing.

            //Use this pass to clear the stencil buffer.
            glStencilOp(GL_ZERO, GL_ZERO, GL_ZERO);
            //The stencil test currently passes when the value equals 0.

            //Bind Deferred Vertex Array Object and Color Buffer.
            glBindVertexArray(output_frameBuffer_VAO);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, color_FrameBuffer_Texture);

            //Render the quad.
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }

        //Set stencil operations to normal, disable stencil writing, and make the stencil test always pass.
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        glStencilMask(0x00);
        glStencilFunc(GL_ALWAYS, 0, 0xFF);


        /*
        //
        //Use RefecltionShader.
        //
        reflection_ShaderProgram.use();
w
        glBindVertexArray(output_frameBuffer_VAO);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, color_FrameBuffer_Texture);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, output_RenderBuffer_Object);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, normal_FrameBuffer_Texture);

        //Render the quad.
        glDrawArrays(GL_TRIANGLES, 0, 6);
        */

        //ReEnable Depth testing and writing.
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);

        


        //
        //Use BasicShader (3D).
        //
        basic_ShaderProgram.use();

        //Upload view matrix.
        glBindBuffer(GL_UNIFORM_BUFFER, globalUniformBlock_UBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, viewMatrixArray);

        //Unbind UBO for now.
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        //Upload every Basic RenderBatch (3D).
        for(Basic_SpriteBatch batch : basic_Batches)
        {
            batch.render();
            //batch.clear();
        }


        //
        //Use LineShader (3D).
        //
        line_ShaderProgram.use();

        //Disable Depth Testing.
        glDisable(GL_DEPTH_TEST);

        //Upload every Line RenderBatch (3D).
        for(Line_RenderBatch batch : line_Batches){batch.render();}


        //
        //Use CircleShader and upload every Circle RenderBatch (3D).
        //
        circle_ShaderProgram.use();
        for(Circle_RenderBatch batch : circle_Batches){batch.render();}


        //
        //Use BasicShader (2D).
        //
        basic_ShaderProgram.use();
        //glDisable(GL_DEPTH_TEST);

        //Upload projection matrix.
        glBindBuffer(GL_UNIFORM_BUFFER, globalUniformBlock_UBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projectionMatrixArray);

        //Unbind UBO for now.
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        //Upload every 2D Basic RenderBatch.
        for(Basic_SpriteBatch batch : basic2D_Batches)
        {
            batch.render();
            //batch.clear();
        }

        //Enable scissor Test.
        glEnable(GL_SCISSOR_TEST);

        //Upload every 2D cropped Basic RenderBatch.
        for(Basic_SpriteBatch batch : cropped_Basic2D_Batches)
        {
            //System.out.println(batch.scissorX + " " + batch.scissorY + " " + batch.scissorWidth + " " + batch.scissorHeight + " " + (batch.scissorY + batch.scissorHeight));

            //Set scissor region.
            glScissor(batch.scissorX, (HEIGHT - batch.scissorY) - batch.scissorHeight, batch.scissorWidth, batch.scissorHeight);
            //Viewport coordinates start from bottom-left. So we need to translate the Y accordingly.

            batch.render();
            //batch.clear();
        }

        //Disable scissor test and reset scissor region.
        glDisable(GL_SCISSOR_TEST);
        resetCropRegion();


        //
        //Use LineShader and upload every Line RenderBatch (2D)
        //
        line_ShaderProgram.use();
        for(Line_RenderBatch batch : line2D_Batches){batch.render();}


        //
        //Use CircleShader and upload every Circle RenderBatch (2D).
        //
        circle_ShaderProgram.use();
        for(Circle_RenderBatch batch : circle2D_Batches){batch.render();}


        //ReEnable other render targets so we can clear them.
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2});
        glStencilMask(0xFF);//Enable writing to the Stencil Buffer so we can clear it.


        //
        //Use default FrameBuffer and FrameBuffer Shader.
        //
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(VIEWPORT_X, VIEWPORT_Y, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        glClear(GL_COLOR_BUFFER_BIT);
        frameBuffer_ShaderProgram.use();

        //Bind the FrameBuffer's Vertex Array Object.
        glBindVertexArray(output_frameBuffer_VAO);
        //glDisable(GL_DEPTH_TEST);

        //Bind the FrameBuffer's Texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, output_FrameBuffer_Texture);
        //glBindTexture(GL_TEXTURE_2D, color_FrameBuffer_Texture);
        //glBindTexture(GL_TEXTURE_2D, normal_FrameBuffer_Texture);
        //glBindTexture(GL_TEXTURE_2D, output_RenderBuffer_Object);

        //Render the texture as a quad.
        glDrawArrays(GL_TRIANGLES, 0, 6);

        //FINALLY, display to the screen.
        glfwSwapBuffers(windowAddress);

        //Switch back to custom FrameBuffer.
        glBindFramebuffer(GL_FRAMEBUFFER, output_FrameBuffer_Object);
    }

    @Override
    public void clear()//{}
    {
        //Only clear the custom FrameBuffer.
        //glBindFramebuffer(GL_FRAMEBUFFER, output_FrameBuffer_Object);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    @Override
    /**Resets renderBatches to clean out any unused SpriteSheets.*/
    public void reset_RenderBatches()
    {
        //Basic.
        basic_Batches.clear();
        basic2D_Batches.clear();
        cropped_Basic2D_Batches.clear();

        //Lighting.
        lighting_Batches.clear();
        lightingTransperant_Batches.clear();

        //Line.
        line_Batches.clear();
        line2D_Batches.clear();
    
        //Circle.
        circle_Batches.clear();
        circle2D_Batches.clear();
    }

    //Crop region points.
    protected int CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT;

    @Override
    /**Sets the current crop region that future renderbatches will use.*/
    public final void setCropRegion(int cropX0, int cropY0, int cropX1, int cropY1)
    {
        //Complete out-of-bounds check.
        if(cropX0 >= WIDTH || cropY0 >= HEIGHT || cropX1 < 0 || cropY1 < 0)
        {System.err.println("What are you doing with the crop?");}

        //Set crop region.
        this.CROP_X = (cropX0 < 0) ? 0 : cropX0;
        this.CROP_Y = (cropY0 < 0) ? 0 : cropY0;
        this.CROP_WIDTH = ((cropX1 >= WIDTH) ? WIDTH : cropX1) - CROP_X;
        this.CROP_HEIGHT = ((cropY1 >= HEIGHT) ? HEIGHT : cropY1) - CROP_Y;

        //We're using cropped renderBatches.
        current_Basic2D_Batches = cropped_Basic2D_Batches;
    }

    @Override
    public final void resetCropRegion()
    {
        //Reset crop region.
        this.CROP_X = 0;
        this.CROP_Y = 0;
        this.CROP_WIDTH = WIDTH;
        this.CROP_HEIGHT = HEIGHT;

        //We're using regular renderBatches.
        current_Basic2D_Batches = basic2D_Batches;
    }


    /*
     * EVERYTHING below this threshold will be applying Sprite Vertices to the RenderBatches.
     */

    //private Basic_SpriteBatch current_BasicBatch = null;

    /**Adds a sprite to a Basic_SpriteBatch.*/
    private void add_basic(float position_x0, float position_y0, float position_z0, 
    float position_x1, float position_y1, float position_z1, 
    float position_x2, float position_y2, float position_z2, 
    float position_x3, float position_y3, float position_z3,
    Vector4f color, SpriteSheet sheet,
    float tex_x0, float tex_x1, float tex_y0, float tex_y1, boolean fixed)
    {
        int size;
        List<Basic_SpriteBatch> currentBatchList = null;

        //If not fixed, use 2d renderBatches.
        if(!fixed)
        {
            currentBatchList = current_Basic2D_Batches;
            size = currentBatchList.size();

            //Version of this function with a crop region check.
            if(CROP_WIDTH < WIDTH || CROP_HEIGHT < HEIGHT)
            {
                for(int i = 0; i <= size; i++)
                {
                    //Create Batch pointer.
                    Basic_SpriteBatch cropBatch = null;

                    //Happens if every batch is full.
                    if(i >= size)
                    {
                        //Create a new RenderBatch and add the sheet to it.
                        cropBatch = new Basic_SpriteBatch(BATCH_MAX_SPRITE_COUNT, CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);
                        cropBatch.start();
                        cropBatch.addSheet(sheet);

                        //Add this batch to the current list.
                        currentBatchList.add(cropBatch);
                    }
                    //Otherwise, try to add it to the current batch.
                    else
                    {
                        //Get the current batch.
                        cropBatch = currentBatchList.get(i);

                        //Does it have room for another quad?
                        if(cropBatch.hasRoom())
                        {
                            //If this batch is empty, set its scissor region.
                            if(cropBatch.numSprites <= 0)
                            {cropBatch.setScisssorRegion(CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);}
                            //
                            //Otherwise, if its scissor region isn't the current one, skip it.
                            else if(cropBatch.scissorX != CROP_X || cropBatch.scissorY != CROP_Y
                            || cropBatch.scissorWidth != CROP_WIDTH || cropBatch.scissorHeight != CROP_HEIGHT)
                            {continue;}

                            //Does it have this sheet?
                            if(!cropBatch.hasSheet(sheet))
                            {
                                //Does it have room for this sheet? If so, add it. Otherwise, skip to the next one.
                                if(cropBatch.hasSheetRoom()){cropBatch.addSheet(sheet);}
                                else{continue;}
                            }
                        }
                        else{continue;}
                    }

                    //Get Texture Slot.
                    int texSlot = cropBatch.getTexSlot(sheet.getID());

                    //Add the full quad.
                    cropBatch.addVertices(position_x0, position_y0, position_z0, color, tex_x0, tex_y0, texSlot);
                    cropBatch.addVertices(position_x1, position_y1, position_z1, color, tex_x1, tex_y0, texSlot);
                    cropBatch.addVertices(position_x2, position_y2, position_z2, color, tex_x1, tex_y1, texSlot);
                    cropBatch.addVertices(position_x3, position_y3, position_z3, color, tex_x0, tex_y1, texSlot);
                    
                    //We're done here.
                    break;
                }
                //Don't proceed with this function. We're done.
                return;
            }
        }
        else
        {
            currentBatchList = basic_Batches;
            size = currentBatchList.size();
        }


        //
        //Main addition.
        //
        for(int i = 0; i <= size; i++)
        {
            //Create Batch pointer.
            Basic_SpriteBatch batch = null;

            //Happens if every batch is full.
            if(i >= size)
            {
                //Create a new RenderBatch and add the sheet to it.
                batch = new Basic_SpriteBatch(BATCH_MAX_SPRITE_COUNT);
                batch.start();
                batch.addSheet(sheet);

                //Add this batch to the correct list.
                currentBatchList.add(batch);
            }
            //Otherwise, try to add it to the current batch.
            else
            {
                //Get the current batch.
                batch = currentBatchList.get(i);

                //Does it have room for a quad?
                if(batch.hasRoom())
                {
                    //Does it have this sheet?
                    if(!batch.hasSheet(sheet))
                    {
                        //Does it have room for this sheet? If so, add it. Otherwise, skip to the next one.
                        if(batch.hasSheetRoom()){batch.addSheet(sheet);}
                        else{continue;}
                    }
                }
                else{continue;}
            }

            //Get Texture Slot.
            int texSlot = batch.getTexSlot(sheet.getID());

            //Calculate Texture coordinates.
            //float
            //x0 = (sprite.getX() + (((wrapFlags & 0b01) == 0b01) ? 0 : wrapX)) / (float)sheet.getWidth(),
            //x1 = (sprite.getX() + (((wrapFlags & 0b01) == 0b01) ? wrapX : sprite.getWidth())) / (float)sheet.getWidth(),
            //
            //y0 = (sprite.getY() + (((wrapFlags & 0b10) == 0b10) ? 0 : wrapY)) / (float)sheet.getHeight(),
            //y1 = (sprite.getY() + (((wrapFlags & 0b10) == 0b10) ? wrapY : sprite.getHeight())) / (float)sheet.getHeight();

            //int
            //left = sprite.getX(), right = left + sprite.getWidth(),
            //top = sprite.getY(), bottom = top + sprite.getHeight();

            //float
            //x0 = left + (((wrapFlags & 0b01) == 0b01) ? 0 : wrapX);
            //x0 = right - (((wrapFlags & 0b01) == 0b01) ? 0 : wrapX);

            //System.out.println("add vertices");

            //Add the full quad.
            batch.addVertices(position_x0, position_y0, position_z0, color, tex_x0, tex_y0, texSlot);
            batch.addVertices(position_x1, position_y1, position_z1, color, tex_x1, tex_y0, texSlot);
            batch.addVertices(position_x2, position_y2, position_z2, color, tex_x1, tex_y1, texSlot);
            batch.addVertices(position_x3, position_y3, position_z3, color, tex_x0, tex_y1, texSlot);
            
            //We're done here.
            break;
        }
    }

    

    /**Adds a sprite and normal map to a Lighting_SpriteBatch.*/
    private void add_lighting(float position_x0, float position_y0, float position_z0, 
    float position_x1, float position_y1, float position_z1, 
    float position_x2, float position_y2, float position_z2, 
    float position_x3, float position_y3, float position_z3,
    //Vector4f color, Sprite sprite, Sprite normalMap, int wrapFlags, int wrapX, int wrapY, boolean fixed)
    Vector4f color, float emission, SpriteSheet sheet,
    float tex_x0, float tex_x1, float tex_y0, float tex_y1,
    float norm_x0, float norm_x1, float norm_y0, float norm_y1)//,
    //boolean fixed)
    {
        //Get SpriteSheet (assuming normalMap is part of the same sheet).
        //SpriteSheet sheet = sprite.getSheet();

        //Which list?
        boolean isTransperant = (color.w < 1.0f);
        List<Lighting_SpriteBatch> currentBatchList = (isTransperant) ? lightingTransperant_Batches : lighting_Batches;
        int size = currentBatchList.size();

        for(int i = 0; i <= size; i++)
        {
            //Create Batch pointer.
            Lighting_SpriteBatch batch = null;

            //Happens if every batch is full.
            if(i >= size)
            {
                //Create a new RenderBatch.
                if(isTransperant)
                {
                    batch = new Lighting_SpriteBatch(BATCH_MAX_SPRITE_COUNT, false);
                    batch.start_Transperant();
                }
                else
                {
                    batch = new Lighting_SpriteBatch(BATCH_MAX_SPRITE_COUNT, true);
                    batch.start_Opaque();
                }

                //Add the sheet to it.
                batch.addSheet(sheet);

                //Add the batch to the list.
                currentBatchList.add(batch);
            }
            //Otherwise, try to add it to the current batch.
            else
            {
                //Get the current batch.
                batch = currentBatchList.get(i);

                //Does it have room for a quad?
                if(batch.hasRoom())
                {
                    //Does it have this sheet?
                    if(!batch.hasSheet(sheet))
                    {
                        //Does it have room for this sheet? If so, add it. Otherwise, skip to the next one.
                        if(batch.hasSheetRoom()){batch.addSheet(sheet);}
                        else{continue;}
                    }
                }
                else{continue;}
            }

            //Get Texture Slot.
            int texSlot = batch.getTexSlot(sheet.getID());


            //Calculate Texture color coordinates.
            //float
            //cx0 = (sprite.getX() + (((wrapFlags & 0b01) == 0b01) ? 0 : wrapX)) / (float)sheet.getWidth(),
            //cx1 = (sprite.getX() + (((wrapFlags & 0b01) == 0b01) ? wrapX : sprite.getWidth())) / (float)sheet.getWidth(),
            //
            //cy0 = (sprite.getY() + (((wrapFlags & 0b10) == 0b10) ? 0 : wrapY)) / (float)sheet.getHeight(),
            //cy1 = (sprite.getY() + (((wrapFlags & 0b10) == 0b10) ? wrapY : sprite.getHeight())) / (float)sheet.getHeight();


            //Calculate Texture normal coordinates.
            //float
            //nx0 = (normalMap.getX() + (((wrapFlags & 0b01) == 0b01) ? 0 : wrapX)) / (float)sheet.getWidth(),
            //nx1 = (normalMap.getX() + (((wrapFlags & 0b01) == 0b01) ? wrapX : normalMap.getWidth())) / (float)sheet.getWidth(),
            //
            //ny0 = (normalMap.getY() + (((wrapFlags & 0b10) == 0b10) ? 0 : wrapY)) / (float)sheet.getHeight(),
            //ny1 = (normalMap.getY() + (((wrapFlags & 0b10) == 0b10) ? wrapY : normalMap.getHeight())) / (float)sheet.getHeight();


            //Add the full quad.
            if(isTransperant)
            {
                batch.addVertices_Transperant(position_x0, position_y0, position_z0, color, tex_x0, tex_y0, norm_x0, norm_y0, texSlot, emission);
                batch.addVertices_Transperant(position_x1, position_y1, position_z1, color, tex_x1, tex_y0, norm_x1, norm_y0, texSlot, emission);
                batch.addVertices_Transperant(position_x2, position_y2, position_z2, color, tex_x1, tex_y1, norm_x1, norm_y1, texSlot, emission);
                batch.addVertices_Transperant(position_x3, position_y3, position_z3, color, tex_x0, tex_y1, norm_x0, norm_y1, texSlot, emission);
            }
            else
            {
                batch.addVertices_Opaque(position_x0, position_y0, position_z0, color, tex_x0, tex_y0, norm_x0, norm_y0, texSlot, emission);
                batch.addVertices_Opaque(position_x1, position_y1, position_z1, color, tex_x1, tex_y0, norm_x1, norm_y0, texSlot, emission);
                batch.addVertices_Opaque(position_x2, position_y2, position_z2, color, tex_x1, tex_y1, norm_x1, norm_y1, texSlot, emission);
                batch.addVertices_Opaque(position_x3, position_y3, position_z3, color, tex_x0, tex_y1, norm_x0, norm_y1, texSlot, emission);
            }
            
            //We're done here.
            break;
        }
    }



    /**Adds a sprite and normal map to a Lighting_SpriteBatch.*/
    private void add_shadowVolume(float position_x0, float position_y0, float position_z0, 
    float position_x1, float position_y1, float position_z1, 
    float position_x2, float position_y2, float position_z2, 
    float position_x3, float position_y3, float position_z3, boolean fixed)
    {

        int size = shadowVolume_Batches.size();

        for(int i = 0; i <= size; i++)
        {
            //Create Batch pointer.
            ShadowVolume_RenderBatch batch = null;

            //Happens if every batch is full.
            if(i >= size)
            {
                //Create a new RenderBatch and add the sheet to it. 
                batch = new ShadowVolume_RenderBatch(BATCH_MAX_SPRITE_COUNT);
                batch.start();
                
                shadowVolume_Batches.add(batch);
            }
            //Otherwise, try to add it to the current batch.
            else
            {
                //Get the current batch.
                batch = shadowVolume_Batches.get(i);

                //Does it have room for a quad?
                if(!batch.hasRoom()){continue;}
            }

            //Add the full quad.
            batch.addVertices(position_x0, position_y0, position_z0);
            batch.addVertices(position_x1, position_y1, position_z1);
            batch.addVertices(position_x2, position_y2, position_z2);
            batch.addVertices(position_x3, position_y3, position_z3);
            
            //We're done here.
            break;
        }
    }

    /**Adds a sprite and normal map to a Lighting_SpriteBatch.*/
    private void add_shadowSilhouette(float position_x0, float position_y0, float position_z0, 
    float position_x1, float position_y1, float position_z1, 
    float position_x2, float position_y2, float position_z2, 
    float position_x3, float position_y3, float position_z3,
    Sprite sprite, int wrapFlags, int wrapX, int wrapY, boolean fixed)
    {
        //Get SpriteSheet.
        SpriteSheet sheet = sprite.getSheet();

        int size = shadowSilhouette_Batches.size();

        for(int i = 0; i <= size; i++)
        {
            //Create Batch pointer.
            ShadowSilhouette_RenderBatch batch = null;

            //Happens if every batch is full.
            if(i >= size)
            {
                //Create a new RenderBatch and add the sheet to it. 
                batch = new ShadowSilhouette_RenderBatch(BATCH_MAX_SPRITE_COUNT);
                batch.start();
                batch.addSheet(sheet);
                
                shadowSilhouette_Batches.add(batch);
            }
            //Otherwise, try to add it to the current batch.
            else
            {
                //Get the current batch.
                batch = shadowSilhouette_Batches.get(i);

                //Does it have room for a quad?
                if(batch.hasRoom())
                {
                    //Does it have this sheet?
                    if(!batch.hasSheet(sheet))
                    {
                        //Does it have room for this sheet? If so, add it. Otherwise, skip to the next one.
                        if(batch.hasSheetRoom()){batch.addSheet(sheet);}
                        else{continue;}
                    }
                }
                else{continue;}
            }

            //Get Texture Slot.
            int texSlot = batch.getTexSlot(sheet.getID());

            //Calculate Texture color coordinates.
            float
            //cx0 = (sprite.getX() + (((wrapFlags & 0b01) == 0b01) ? 0 : wrapX)) / (float)sheet.getWidth(),
            //cx1 = (sprite.getX() + (((wrapFlags & 0b01) == 0b01) ? wrapX : sprite.getWidth())) / (float)sheet.getWidth(),
            cx0 = sprite.getX() / (float)sheet.getWidth(),
            cx1 = (sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
            //
            //cy0 = (sprite.getY() + (((wrapFlags & 0b10) == 0b10) ? 0 : wrapY)) / (float)sheet.getHeight(),
            //cy1 = (sprite.getY() + (((wrapFlags & 0b10) == 0b10) ? wrapY : sprite.getHeight())) / (float)sheet.getHeight();
            cy0 = sprite.getY() / (float)sheet.getHeight(),
            cy1 = (sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight();

            //Add the full quad.
            batch.addVertices(position_x0, position_y0, position_z0, cx0, cy0, texSlot);
            batch.addVertices(position_x1, position_y1, position_z1, cx1, cy0, texSlot);
            batch.addVertices(position_x2, position_y2, position_z2, cx1, cy1, texSlot);
            batch.addVertices(position_x3, position_y3, position_z3, cx0, cy1, texSlot);
            
            //We're done here.
            break;
        }
    }
    


    /**Adds a sprite to a Basic_SpriteBatch.*/
    private void add_line(float position_x0, float position_y0, float position_z0, 
    float position_x1, float position_y1, float position_z1, 
    Vector4f color, boolean fixed)
    {
        int size = (fixed) ? line_Batches.size() : line2D_Batches.size();
        for(int i = 0; i <= size; i++)
        {
            //Create Batch pointer.
            Line_RenderBatch batch = null;

            //Happens if every batch is full.
            if(i >= size)
            {
                //System.out.println("New line batch.");

                //Create a new RenderBatch and add the sheet to it. 
                batch = new Line_RenderBatch(BATCH_MAX_SPRITE_COUNT);
                batch.start();

                //Add this batch to the correct list.
                if(fixed){line_Batches.add(batch);}
                else{line2D_Batches.add(batch);}
            }
            //Otherwise, try to add it to the current batch.
            else
            {
                //Get the current batch.
                if(fixed){batch = line_Batches.get(i);}
                else{batch = line2D_Batches.get(i);}

                //Does it have room for a line?
                if(!batch.hasRoom()){continue;}
            }

            //Add the full quad.
            batch.addVertices(position_x0, position_y0, position_z0, color);
            batch.addVertices(position_x1, position_y1, position_z1, color);
            
            //We're done here.
            break;
        }
    }


    /**Adds a circle to a Circle_RenderBatch.*/
    private void add_circle(float position_x, float position_y, float position_z, 
    float radius, Vector4f color, float thickness, boolean fixed)
    {
        int size = (fixed) ? circle_Batches.size() : circle2D_Batches.size();
        for(int i = 0; i <= size; i++)
        {
            //Create Batch pointer.
            Circle_RenderBatch batch = null;

            //Happens if every batch is full.
            if(i >= size)
            {
                //Create a new RenderBatch and add the sheet to it. 
                batch = new Circle_RenderBatch(BATCH_MAX_SPRITE_COUNT);
                batch.start();

                //Add this batch to the correct list.
                if(fixed){circle_Batches.add(batch);}
                else{circle2D_Batches.add(batch);}
            }
            //Otherwise, try to add it to the current batch.
            else
            {
                //Get the current batch.
                if(fixed){batch = circle_Batches.get(i);}
                else{batch = circle2D_Batches.get(i);}

                //Does it have room for a quad?
                if(!batch.hasRoom()){continue;}
            }

            float
            x0 = position_x - radius,
            x1 = position_x + radius,
            y0 = position_y - radius,
            y1 = position_y + radius;

            //float t = (thickness) / radius;
            float t = (radius - (thickness)) / radius;

            //Add the full quad.
            batch.addVertices(x0, y0, position_z, color, 0.0f, 0.0f, t);
            batch.addVertices(x1, y0, position_z, color, 1.0f, 0.0f, t);
            batch.addVertices(x1, y1, position_z, color, 1.0f, 1.0f, t);
            batch.addVertices(x0, y1, position_z, color, 0.0f, 1.0f, t);
            
            //We're done here.
            break;
        }
    }


    @Override
    /**Renders a SpriteSheet.*/
    public void renderSheet(int xPos, int yPos, int zPos, SpriteSheet sheet, boolean fixed){}
    public void renderSheet(int xPos, int yPos, int zPos, SpriteSheet sheet, Vector4f blendingColor, boolean fixed){}
    
    @Override
    /**Renders a Sprite.*/
    public void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {renderSprite(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, fixed);}
    //
    public void renderSprite(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {renderSprite(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, blendingColor, fixed);}
    //
    public void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {renderSprite(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, fixed);}
    //
    public void renderSprite(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {
        //Zero check.
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Normal stuff.
        float half_depth = (depth / 2f);
        float zNormal = (sprite.getHeight() - half_depth) / (float)sprite.getHeight();
        float yNormal = (depth == 0) ? 0 : (sprite.getHeight() / half_depth);
        float internalHeight = (sprite.getHeight() * zNormal);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int wrapZ = (int)(wrapY * 2 * yNormal);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        boolean
        flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX

        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            //Pass the data to a RenderBatch.
            add_basic
            (
                //UL
                xPos + ((wxBit) ? sprite.getWidth() - wrapX : 0),
                yPos + ((wyBit) ? internalHeight - wrapY : 0),
                zPos - ((wyBit) ? (depth - wrapZ) : 0),
                //
                //UR
                xPos + ((wxBit) ? sprite.getWidth() : sprite.getWidth() - wrapX),
                yPos + ((wyBit) ? internalHeight - wrapY : 0),
                zPos - ((wyBit) ? (depth - wrapZ) : 0),
                //
                //DR
                xPos + ((wxBit) ? sprite.getWidth() : sprite.getWidth() - wrapX),
                yPos + ((wyBit) ? internalHeight : internalHeight - wrapY),
                zPos - ((wyBit) ? depth : (depth - wrapZ)),
                //
                //DL
                xPos + ((wxBit) ? sprite.getWidth() - wrapX : 0),
                yPos + ((wyBit) ? internalHeight : internalHeight - wrapY),
                zPos - ((wyBit) ? depth : (depth - wrapZ)),
                //
                blendingColor, sheet,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                fixed
            );
        }
    }


    @Override
    /**Renders a scaled Sprite.*/
    public void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed)
    {renderSprite_Sc(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, xScale, yScale, fixed);}
    //
    public void renderSprite_Sc(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed)
    {renderSprite_Sc(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, blendingColor, xScale, yScale, fixed);}
    //
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, float xScale, float yScale, boolean fixed)
    {renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, xScale, yScale, fixed);}
    //
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int resultWidth = (int)((sprite.getWidth() * xScale)//),
         + 0.5f),
        resultHeight = (int)((sprite.getHeight() * yScale)//);
         + 0.5f);

        //Normal stuff.
        float half_depth = (depth / 2f);
        float zNormal = (resultHeight - half_depth) / (float)resultHeight;
        float yNormal = (depth == 0) ? 0 : (resultHeight / half_depth);
        float internalHeight = (resultHeight * zNormal);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * xScale);
        
        float scaleWrapY = (wrapY * yScale);
        int swy = (int)(scaleWrapY * zNormal);
        int wrapZ = (int)(scaleWrapY * 2 * yNormal);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        boolean flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX

        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            //Pass the data to a RenderBatch.
            add_basic
            (
                //UL
                xPos + ((wxBit) ? resultWidth - swx : 0),
                yPos + ((wyBit) ? internalHeight - swy : 0),
                zPos - ((wyBit) ? (depth - wrapZ) : 0),
                //
                //UR
                xPos + ((wxBit) ? resultWidth : resultWidth - swx),
                yPos + ((wyBit) ? internalHeight - swy : 0),
                zPos - ((wyBit) ? (depth - wrapZ) : 0),
                //
                //DR
                xPos + ((wxBit) ? resultWidth : resultWidth - swx),
                yPos + ((wyBit) ? internalHeight : internalHeight - swy),
                zPos - ((wyBit) ? depth : (depth - wrapZ)),
                //
                //DL
                xPos + ((wxBit) ? resultWidth - swx : 0),
                yPos + ((wyBit) ? internalHeight : internalHeight - swy),
                zPos - ((wyBit) ? depth : (depth - wrapZ)),
                //
                blendingColor, sheet,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                fixed
            );
        }
    }

    @Override
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, int wrapX, int wrapY, float xScale, float yScale, boolean fixed)
    {renderSprite_Sc(xPos, yPos, zPos, depth, sprite, flip, normalMap, emission, wrapX, wrapY, Screen.DEFAULT_BLEND, xScale, yScale, fixed);}
    
    @Override
    public void renderSprite_Sc(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, Sprite normalMap, float emission, 
    int wrapX, int wrapY, Vector4f blendingColor, float xScale, float yScale, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int resultWidth = (int)((sprite.getWidth() * xScale)//),
         + 0.5f),
        resultHeight = (int)((sprite.getHeight() * yScale)//);
         + 0.5f);

        //Normal stuff.
        float half_depth = (depth / 2f);
        float zNormal = (resultHeight - half_depth) / (float)resultHeight;
        float yNormal = (depth == 0) ? 0 : (resultHeight / half_depth);
        float internalHeight = (resultHeight * zNormal);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * xScale);
        
        float scaleWrapY = (wrapY * yScale);
        int swy = (int)(scaleWrapY * zNormal);
        int wrapZ = (int)(scaleWrapY * 2 * yNormal);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        boolean flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   ((flipX) ? normalMap.getX() + normalMap.getWidth() - wrapX : normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    ((flipX) ? normalMap.getX() : normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    ((flipX) ? normalMap.getX() + normalMap.getWidth() : normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   ((flipY) ? normalMap.getY() + normalMap.getHeight() - wrapY : normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    ((flipY) ? normalMap.getY() : normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    ((flipY) ? normalMap.getY() + normalMap.getHeight() : normalMap.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX

        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            //Pass the data to a RenderBatch.
            add_lighting
            (
                //UL
                xPos + ((wxBit) ? resultWidth - swx : 0),
                yPos + ((wyBit) ? internalHeight - swy : 0),
                zPos - ((wyBit) ? (depth - wrapZ) : 0),
                //
                //UR
                xPos + ((wxBit) ? resultWidth : resultWidth - swx),
                yPos + ((wyBit) ? internalHeight - swy : 0),
                zPos - ((wyBit) ? (depth - wrapZ) : 0),
                //
                //DR
                xPos + ((wxBit) ? resultWidth : resultWidth - swx),
                yPos + ((wyBit) ? internalHeight : internalHeight - swy),
                zPos - ((wyBit) ? depth : (depth - wrapZ)),
                //
                //DL
                xPos + ((wxBit) ? resultWidth - swx : 0),
                yPos + ((wyBit) ? internalHeight : internalHeight - swy),
                zPos - ((wyBit) ? depth : (depth - wrapZ)),
                //
                blendingColor, emission, sheet,//sprite, normalMap,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                //
                (wxBit) ? norm_midX1 : norm_startX,
                (wxBit) ? norm_startX : norm_midX0,
                (wyBit) ? norm_midY1 : norm_startY,
                (wyBit) ? norm_startY : norm_midY0//,
                //fixed
            );
        }
    }
    



    @Override
    /**Renders a stretch Sprite.*/
    public void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, int resultWidth, int resultHeight, boolean fixed)
    {renderSprite_St(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, resultWidth, resultHeight, fixed);}
    //
    public void renderSprite_St(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed)
    {renderSprite_St(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, blendingColor, resultWidth, resultHeight, fixed);}
    //
    public void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, int resultWidth, int resultHeight, boolean fixed)
    {renderSprite_St(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, resultWidth, resultHeight, fixed);}
    //
    public void renderSprite_St(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, int resultWidth, int resultHeight, boolean fixed)
    {
        //Normal stuff.
        float half_depth = (depth / 2f);
        float zNormal = (resultHeight - half_depth) / (float)resultHeight;
        float yNormal = (depth == 0) ? 0 : (resultHeight / half_depth);
        float internalHeight = (resultHeight * zNormal);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)((wrapX / (float)sprite.getWidth()) * resultWidth);

        float scaleWrapY = ((wrapY / (float)sprite.getHeight()) * resultHeight);
        int swy = (int)(scaleWrapY * zNormal);
        int wrapZ = (int)(scaleWrapY * 2 * yNormal);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        boolean flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();

        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            //Pass the data to a RenderBatch.
            add_basic
            (
                //UL
                xPos + ((wxBit) ? resultWidth - swx : 0),
                yPos + ((wyBit) ? internalHeight - swy : 0),
                zPos - ((wyBit) ? depth - wrapZ : 0),
                //
                //UR
                xPos + ((wxBit) ? resultWidth : resultWidth - swx),
                yPos + ((wyBit) ? internalHeight - swy : 0),
                zPos - ((wyBit) ? depth - wrapZ : 0),
                //
                //DR
                xPos + ((wxBit) ? resultWidth : resultWidth - swx),
                yPos + ((wyBit) ? internalHeight : internalHeight - swy),
                zPos - ((wyBit) ? depth : depth - wrapZ),
                //
                //DL
                xPos + ((wxBit) ? resultWidth - swx : 0),
                yPos + ((wyBit) ? internalHeight : internalHeight - swy),
                zPos - ((wyBit) ? depth : depth - wrapZ),
                //
                blendingColor, sheet,//sprite,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                fixed
            );
        }
    }

    @Override
    /**Renders a sheared Sprite.*/
    public void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xShear, float yShear, boolean fixed)
    {renderSprite_Sh(xPos, yPos, 0, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, xShear, yShear, 0, 0, fixed);}
    //
    public void renderSprite_Sh(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xShear, float yShear, boolean fixed)
    {renderSprite_Sh(xPos, yPos, 0, sprite, flip, wrapX, wrapY, blendingColor, xShear, yShear, 0, 0, fixed);}
    //
    public void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, float xShear, float yShear, float zxShear, float zyShear, boolean fixed)
    {renderSprite_Sh(xPos, yPos, zPos, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, xShear, yShear, zxShear, zyShear, fixed);}
    //
    public void renderSprite_Sh(int xPos, int yPos, int zPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float xShear, float yShear, float zxShear, float zyShear, boolean fixed)
    {
        //add_basic
        //(
            //xPos + (0 + (0 * xShear)),
            //yPos + (0 + (0 * yShear)),
            //zPos,
            //
            //xPos + (sprite.getWidth() + (0 * xShear)),
            //yPos + (0 + (sprite.getWidth() * yShear)),
            //zPos,
            //
            //xPos + (sprite.getWidth() + (sprite.getHeight() * xShear)),
            //yPos + (sprite.getHeight() + (sprite.getWidth() * yShear)),
            //zPos,
            //
            //xPos + (0 + (sprite.getHeight() * xShear)),
            //yPos + (sprite.getHeight() + (0 * yShear)),
            //zPos,
            //
            //blendingColor, sprite, 0, 0, 0, fixed
        //);

        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();

        int wx = (sprite.getWidth() - wrapX), wy = (sprite.getHeight() - wrapY);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        boolean flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();

        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            float
            x0 = xPos + ((wxBit) ? (wx + (wy * xShear)) : (0 + (0 * xShear))),
            y0 = yPos + ((wyBit) ? (wy + (wx * yShear)) : (0 + (0 * yShear))),
            //
            x1 = xPos + ((wxBit) ? (sprite.getWidth() + (wy * xShear)) : (wx + (0 * xShear))),
            y1 = yPos + ((wyBit) ? (wy + (sprite.getWidth() * yShear)) : (0 + (wx * yShear))),
            //
            x2 = xPos + ((wxBit) ? (sprite.getWidth() + (sprite.getHeight() * xShear)) : (wx + (wy * xShear))),
            y2 = yPos + ((wyBit) ? (sprite.getHeight() + (sprite.getWidth() * yShear)) : (wy + (wx * yShear))),
            //
            x3 = xPos + ((wxBit) ? (wx + (sprite.getHeight() * xShear)) : (0 + (wy * xShear))),
            y3 = yPos + ((wyBit) ? (sprite.getHeight() + (wx * yShear)) : (wy + (0 * yShear)));

            //Pass the data to a RenderBatch.
            add_basic
            (
                x0, y0, 0,
                x1, y1, 0,
                x2, y2, 0,
                x3, y3, 0,
                //
                blendingColor, sheet,//sprite,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                fixed
            );
        }
    }


    @Override
    /**Renders a rotated Sprite.*/
    public void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, float rads, int originX, int originY, boolean fixed)
    {renderSprite_Ro(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, rads, originX, originY, fixed);}
    //
    public void renderSprite_Ro(int xPos, int yPos, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed)
    {renderSprite_Ro(xPos, yPos, 0, 0, sprite, flip, wrapX, wrapY, blendingColor, rads, originX, originY, fixed);}
    //
    public void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, float rads, int originX, int originY, boolean fixed)
    {renderSprite_Ro(xPos, yPos, zPos, depth, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, rads, originX, originY, fixed);}
    //
    public void renderSprite_Ro(int xPos, int yPos, int zPos, int depth, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, float rads, int originX, int originY, boolean fixed)
    {
        //Sprite Dimensions and zero check.
        int sprite_width = sprite.getWidth(), sprite_height = sprite.getHeight();
        if(sprite_width == 0 || sprite_height == 0){return;}

        //Trig...
        float sin = (float)StrictMath.sin(rads), cos = (float)StrictMath.cos(rads);

        //Multiply the origin by sin and cos for the actual result origin.
        float oX = ((originX * cos) + (originY * -sin));
        float oY = ((originX * sin) + (originY * cos));

        //Wrap stuff.
        wrapX %= sprite_width;
        wrapY %= sprite_height;

        int wx = (sprite_width - wrapX), wy = (sprite_height - wrapY);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        boolean flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();

        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            float
            x0 = xPos + ((wxBit) ? ((wx * cos) + (wy * -sin)) : 0) - oX,//((0 * cos) + (0 * -sin)) - oX,
            y0 = yPos + ((wyBit) ? ((wx * sin) + (wy * cos)) : 0) - oY,//((0 * sin) + (0 * cos)) - oY,
            //
            x1 = xPos + ((wxBit) ? ((sprite_width * cos) + (wy * -sin)) : (wx * cos)) - oX,//((wx * cos) + (0 * -sin))) - oX,
            y1 = yPos + ((wyBit) ? ((sprite_width * sin) + (wy * cos)) : (wx * sin)) - oY,//((wx * sin) + (0 * cos))) - oY,
            //
            x2 = xPos + ((wxBit) ? ((sprite_width * cos) + (sprite_height * -sin)) : ((wx * cos) + (wy * -sin))) - oX,
            y2 = yPos + ((wyBit) ? ((sprite_width * sin) + (sprite_height * cos)) : ((wx * sin) + (wy * cos))) - oY,
            //
            x3 = xPos + ((wxBit) ? ((wx * cos) + (sprite_height * -sin)) : (wy * -sin)) - oX,//((0 * cos) + (wy * -sin))) - oX,
            y3 = yPos + ((wyBit) ? ((wx * sin) + (sprite_height * cos)) : (wy * cos)) - oY;//((0 * sin) + (wy * cos))) - oY;

            //Pass the data to a RenderBatch.
            add_basic
            (
                x0, y0, 0,
                x1, y1, 0,
                x2, y2, 0,
                x3, y3, 0,
                //
                blendingColor, sheet,//sprite,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                fixed
            );
        }
    }



    @Override
    public void renderTile(int xPos, int yPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(xPos, yPos, 0, sprite, wrapX, wrapY, blendingColor,
        scale, shearType, shear, fixed);
    }

    @Override
    public void renderTile(int xPos, int yPos, int zPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int scaleWidth = (int)((sprite.getWidth() * scale)//),
        + 0.5f),
        scaleHeight = (int)((sprite.getHeight() * scale)//),
        + 0.5f),
        depth = (int)((sprite.getHeight() * 2 * scale)//);
        +  0.5f);
        //depth = ((scaleHeight) * 2);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * scale);
        int swy = (int)(wrapY * scale);
        int swz = (int)((wrapY * 2) * scale);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        //boolean flipX = ((0b01 & flip) == 0b01),
        //flipY = ((0b10 & flip) == 0b10);

        //float
        //startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        //midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        //midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        //startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        //midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        //midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();

        float
        startX =    (sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     (sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     (sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    (sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     (sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     (sprite.getY()) / (float)sheet.getHeight();

        switch(shearType)
        {
            //Just scale, asssume Z-Normal = 0.
            case TileMesh.SHEARTYPE_WALL:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_basic
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos,
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos,
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos,
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos,
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        blendingColor, sheet,//sprite,
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        fixed
                    );
                }
            }
            break;


            //Y Shear, assume Z-Normal = 0.
            case TileMesh.SHEARTYPE_Y:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_basic
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? (scaleWidth * shear) - swy : 0),
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? (scaleWidth * shear) : (scaleWidth * shear) - swy),
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? (scaleWidth * shear) : (scaleWidth * shear) - swy),
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? (scaleWidth * shear) - swy : 0),
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        blendingColor, sheet,//sprite
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        fixed
                    );
                }
            }
            break;


            //ZX Shear, assume Z-Normal = 1.
            case TileMesh.SHEARTYPE_ZX:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    //Pass the data to a RenderBatch.
                    add_basic
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos,
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos + (scaleWidth * shear),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos + (scaleWidth * shear),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos,
                        //
                        blendingColor, sheet,//sprite
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        fixed
                    );
                }
            }
            break;


            //ZY Shear, assume Z-Normal = 1.
            case TileMesh.SHEARTYPE_ZY:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_basic
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos + ((wyBit) ? ((scaleHeight - swy) * shear) : 0),
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos + ((wyBit) ? ((scaleHeight - swy) * shear) : 0),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos + ((wyBit) ? (scaleHeight * shear) : ((scaleHeight - swy) * shear)),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos + ((wyBit) ? (scaleHeight * shear) : ((scaleHeight - swy) * shear)),
                        //
                        blendingColor, sheet,//sprite
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        fixed
                    );
                }
            }
            break;


            //Just scale, assume Z-Normal = 1.
            default:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_basic
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos,
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos,
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos,
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos,
                        //
                        blendingColor, sheet,//sprite
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        fixed
                    );
                }
            }
            break;
        }
    }

    @Override
    public void renderTile_Ent(int xPos, int yPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(xPos, yPos, 0, sprite, wrapX, wrapY, blendingColor,
        scale, shearType, shear, fixed);
    }

    @Override
    public void renderTile_Ent(int xPos, int yPos, int zPos, Sprite sprite, int wrapX, int wrapY, Vector4f blendingColor, 
    float scale, byte shearType, float shear, boolean fixed)
    {
        renderTile(xPos, yPos, zPos, sprite, wrapX, wrapY, blendingColor,
        scale, shearType, shear, fixed);
    }





    //@Deprecated
    //@Override
    /**Lighting Tile render function.
    public void renderTile(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission, byte flip, int wrapX, int wrapY, Vector4f blendingColor,
    float scale, byte shearType, float shear, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int scaleWidth = (int)((sprite.getWidth() * scale)//),
         + 0.5f),
        scaleHeight = (int)((sprite.getHeight() * scale)//),
         + 0.5f),
        depth = (int)(((sprite.getHeight() * 2) * scale)//);
        + 0.5f);
        //depth = ((scaleHeight) * 2);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * scale);
        int swy = (int)(wrapY * scale);
        int swz = (int)((wrapY * 2) * scale);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        boolean flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   ((flipX) ? normalMap.getX() + normalMap.getWidth() - wrapX : normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    ((flipX) ? normalMap.getX() : normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    ((flipX) ? normalMap.getX() + normalMap.getWidth() : normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   ((flipY) ? normalMap.getY() + normalMap.getHeight() - wrapY : normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    ((flipY) ? normalMap.getY() : normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    ((flipY) ? normalMap.getY() + normalMap.getHeight() : normalMap.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX


        switch(shearType)
        {
            //Just scale, asssume Z-Normal = 0.
            case TileSprite.SHEARTYPE_WALL:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_lighting
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos,
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos,
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos,
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos,
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        blendingColor, emission, sheet,//sprite, normalMap,
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        //
                        (wxBit) ? norm_midX1 : norm_startX,
                        (wxBit) ? norm_startX : norm_midX0,
                        (wyBit) ? norm_midY1 : norm_startY,
                        (wyBit) ? norm_startY : norm_midY0//,
                        //fixed
                    );
                }
            }
            break;


            //Y Shear, assume Z-Normal = 0.
            case TileSprite.SHEARTYPE_Y:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_lighting
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? (scaleWidth * shear) - swy : 0),
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? (scaleWidth * shear) : (scaleWidth * shear) - swy),
                        zPos - ((wyBit) ? depth - swz : 0),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? (scaleWidth * shear) : (scaleWidth * shear) - swy),
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? (scaleWidth * shear) - swy : 0),
                        zPos - ((wyBit) ? depth : depth - swz),
                        //
                        blendingColor, emission, sheet,//sprite, normalMap,
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        //
                        (wxBit) ? norm_midX1 : norm_startX,
                        (wxBit) ? norm_startX : norm_midX0,
                        (wyBit) ? norm_midY1 : norm_startY,
                        (wyBit) ? norm_startY : norm_midY0//,
                        //fixed
                    );
                }
            }
            break;


            //ZX Shear, assume Z-Normal = 1.
            case TileSprite.SHEARTYPE_ZX:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    //Pass the data to a RenderBatch.
                    add_lighting
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos,
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos + (scaleWidth * shear),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos + (scaleWidth * shear),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos,
                        //
                        blendingColor, emission, sheet,//sprite, normalMap,
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        //
                        (wxBit) ? norm_midX1 : norm_startX,
                        (wxBit) ? norm_startX : norm_midX0,
                        (wyBit) ? norm_midY1 : norm_startY,
                        (wyBit) ? norm_startY : norm_midY0//,
                        //fixed
                    );
                }
            }
            break;


            //ZY Shear, assume Z-Normal = 1.
            case TileSprite.SHEARTYPE_ZY:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_lighting
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos + ((wyBit) ? ((scaleHeight - swy) * shear) : 0),
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos + ((wyBit) ? ((scaleHeight - swy) * shear) : 0),
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos + ((wyBit) ? (scaleHeight * shear) : ((scaleHeight - swy) * shear)),
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos + ((wyBit) ? (scaleHeight * shear) : ((scaleHeight - swy) * shear)),
                        //
                        blendingColor, emission, sheet,//sprite, normalMap,
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        //
                        (wxBit) ? norm_midX1 : norm_startX,
                        (wxBit) ? norm_startX : norm_midX0,
                        (wyBit) ? norm_midY1 : norm_startY,
                        (wyBit) ? norm_startY : norm_midY0//,
                        //fixed
                    );
                }
            }
            break;


            //Just scale, assume Z-Normal = 1.
            default:
            {
                for(int w = 0b00; w <= wrapFlags; w += wrapInc)
                {
                    //Wrap booleans.
                    boolean
                    wxBit = ((0b01 & w) == 0b01),
                    wyBit = ((0b10 & w) == 0b10);

                    add_lighting
                    (
                        //UL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos,
                        //
                        //UR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight - swy : 0),
                        zPos,
                        //
                        //DR
                        xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos,
                        //
                        //DL
                        xPos + ((wxBit) ? scaleWidth - swx : 0),
                        yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                        zPos,
                        //
                        blendingColor, emission, sheet,//sprite, normalMap,
                        //wrapFlags & w, wrapX, wrapY,
                        (wxBit) ? midX1 : startX,
                        (wxBit) ? startX : midX0,
                        (wyBit) ? midY1 : startY,
                        (wyBit) ? startY : midY0,
                        //
                        (wxBit) ? norm_midX1 : norm_startX,
                        (wxBit) ? norm_startX : norm_midX0,
                        (wyBit) ? norm_midY1 : norm_startY,
                        (wyBit) ? norm_startY : norm_midY0//,
                        //fixed
                    );
                }
            }
            break;
        }
    }
    */




    private void renderTile_shearFloor(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission,
    int wrapX, int wrapY, Vector4f blendingColor, float scale, float shear, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int scaleWidth = (int)((sprite.getWidth() * scale)//),
        + 0.5f);
        int scaleHeight = (int)((sprite.getHeight() * scale)//),
        + 0.5f);
        //depth = (int)(((sprite.getHeight() * 2) * scale)//);
        //+ 0.5f);
        //depth = ((scaleHeight) * 2);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * scale);
        int swy = (int)(wrapY * scale);
        //int swz = (int)((wrapY * 2) * scale);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        /*
        boolean flipX = ((0b01 & flip) == 0b01),
        flipY = ((0b10 & flip) == 0b10);

        float
        startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   ((flipX) ? normalMap.getX() + normalMap.getWidth() - wrapX : normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    ((flipX) ? normalMap.getX() : normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    ((flipX) ? normalMap.getX() + normalMap.getWidth() : normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   ((flipY) ? normalMap.getY() + normalMap.getHeight() - wrapY : normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    ((flipY) ? normalMap.getY() : normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    ((flipY) ? normalMap.getY() + normalMap.getHeight() : normalMap.getY()) / (float)sheet.getHeight();
        */
        //startX -> rl -> startX

        float
        startX =    (sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     (sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     (sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    (sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     (sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     (sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   (normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    (normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    (normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   (normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    (normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    (normalMap.getY()) / (float)sheet.getHeight();
        
        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            add_lighting
            (
                //UL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? scaleHeight - swy : 0),
                zPos,
                //
                //UR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? scaleHeight - swy : 0),
                zPos,
                //
                //DR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                zPos,
                //
                //DL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                zPos,
                //
                blendingColor, emission, sheet,//sprite, normalMap,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                //
                (wxBit) ? norm_midX1 : norm_startX,
                (wxBit) ? norm_startX : norm_midX0,
                (wyBit) ? norm_midY1 : norm_startY,
                (wyBit) ? norm_startY : norm_midY0//,
                //fixed
            );
        }
    }

    private void renderTile_shearWall(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission,
    int wrapX, int wrapY, Vector4f blendingColor, float scale, float shear, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int scaleWidth = (int)((sprite.getWidth() * scale)//),
         + 0.5f);
        //int scaleHeight = (int)((sprite.getHeight() * scale)//),
        // + 0.5f);
        int depth = (int)(((sprite.getHeight() * 2) * scale)//);
        + 0.5f);
        //depth = ((scaleHeight) * 2);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * scale);
        //int swy = (int)(wrapY * scale);
        int swz = (int)((wrapY * 2) * scale);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        float
        startX =    (sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     (sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     (sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    (sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     (sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     (sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   (normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    (normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    (normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   (normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    (normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    (normalMap.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX

        //Start wrap loop.
        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            add_lighting
            (
                //UL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos,
                zPos - ((wyBit) ? depth - swz : 0),
                //
                //UR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos,
                zPos - ((wyBit) ? depth - swz : 0),
                //
                //DR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos,
                zPos - ((wyBit) ? depth : depth - swz),
                //
                //DL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos,
                zPos - ((wyBit) ? depth : depth - swz),
                //
                blendingColor, emission, sheet,//sprite, normalMap,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                //
                (wxBit) ? norm_midX1 : norm_startX,
                (wxBit) ? norm_startX : norm_midX0,
                (wyBit) ? norm_midY1 : norm_startY,
                (wyBit) ? norm_startY : norm_midY0//,
                //fixed
            );
        }
    }

    private void renderTile_shearY(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission,
    int wrapX, int wrapY, Vector4f blendingColor, float scale, float shear, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int scaleWidth = (int)((sprite.getWidth() * scale)//),
         + 0.5f);
        //int scaleHeight = (int)((sprite.getHeight() * scale)//),
        // + 0.5f);
        int depth = (int)(((sprite.getHeight() * 2) * scale)//);
        + 0.5f);
        //depth = ((scaleHeight) * 2);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * scale);
        int swy = (int)(wrapY * scale);
        int swz = (int)((wrapY * 2) * scale);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        float
        startX =    (sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     (sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     (sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    (sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     (sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     (sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   (normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    (normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    (normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   (normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    (normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    (normalMap.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX

        //Start loop.
        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            add_lighting
            (
                //UL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? (scaleWidth * shear) - swy : 0),
                zPos - ((wyBit) ? depth - swz : 0),
                //
                //UR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? (scaleWidth * shear) : (scaleWidth * shear) - swy),
                zPos - ((wyBit) ? depth - swz : 0),
                //
                //DR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? (scaleWidth * shear) : (scaleWidth * shear) - swy),
                zPos - ((wyBit) ? depth : depth - swz),
                //
                //DL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? (scaleWidth * shear) - swy : 0),
                zPos - ((wyBit) ? depth : depth - swz),
                //
                blendingColor, emission, sheet,//sprite, normalMap,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                //
                (wxBit) ? norm_midX1 : norm_startX,
                (wxBit) ? norm_startX : norm_midX0,
                (wyBit) ? norm_midY1 : norm_startY,
                (wyBit) ? norm_startY : norm_midY0//,
                //fixed
            );
        }
    }

    private void renderTile_shearZX(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission,
    int wrapX, int wrapY, Vector4f blendingColor, float scale, float shear, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int scaleWidth = (int)((sprite.getWidth() * scale)//),
         + 0.5f);
        int scaleHeight = (int)((sprite.getHeight() * scale)//),
         + 0.5f);
        //depth = (int)(((sprite.getHeight() * 2) * scale)//);
        //+ 0.5f);
        //depth = ((scaleHeight) * 2);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * scale);
        int swy = (int)(wrapY * scale);
        //int swz = (int)((wrapY * 2) * scale);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        float
        startX =    (sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     (sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     (sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    (sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     (sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     (sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   (normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    (normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    (normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   (normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    (normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    (normalMap.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX

        //Start loop.
        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            //Pass the data to a RenderBatch.
            add_lighting
            (
                //UL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? scaleHeight - swy : 0),
                zPos,
                //
                //UR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? scaleHeight - swy : 0),
                zPos + (scaleWidth * shear),
                //
                //DR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                zPos + (scaleWidth * shear),
                //
                //DL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                zPos,
                //
                blendingColor, emission, sheet,//sprite, normalMap,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                //
                (wxBit) ? norm_midX1 : norm_startX,
                (wxBit) ? norm_startX : norm_midX0,
                (wyBit) ? norm_midY1 : norm_startY,
                (wyBit) ? norm_startY : norm_midY0//,
                //fixed
            );
        }
    }

    private void renderTile_shearZY(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission,
    int wrapX, int wrapY, Vector4f blendingColor, float scale, float shear, boolean fixed)
    {
        if(sprite.getWidth() == 0 || sprite.getHeight() == 0){return;}

        //Dimensions.
        int scaleWidth = (int)((sprite.getWidth() * scale)//),
         + 0.5f);
        int scaleHeight = (int)((sprite.getHeight() * scale)//),
         + 0.5f);
        //int depth = (int)(((sprite.getHeight() * 2) * scale)//);
        //+ 0.5f);
        //depth = ((scaleHeight) * 2);

        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        int swx = (int)(wrapX * scale);
        int swy = (int)(wrapY * scale);
        //int swz = (int)((wrapY * 2) * scale);

        int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        wrapInc = (wrapFlags == 0b10) ? 2 : 1;

        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        float
        startX =    (sprite.getX() + wrapX) / (float)sheet.getWidth(),
        midX0 =     (sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        midX1 =     (sprite.getX()) / (float)sheet.getWidth(),
        //
        startY =    (sprite.getY() + wrapY) / (float)sheet.getHeight(),
        midY0 =     (sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        midY1 =     (sprite.getY()) / (float)sheet.getHeight();

        float
        norm_startX =   (normalMap.getX() + wrapX) / (float)sheet.getWidth(),
        norm_midX0 =    (normalMap.getX() + normalMap.getWidth()) / (float)sheet.getWidth(),
        norm_midX1 =    (normalMap.getX()) / (float)sheet.getWidth(),
        //
        norm_startY =   (normalMap.getY() + wrapY) / (float)sheet.getHeight(),
        norm_midY0 =    (normalMap.getY() + normalMap.getHeight()) / (float)sheet.getHeight(),
        norm_midY1 =    (normalMap.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX

        //Start loop.
        for(int w = 0b00; w <= wrapFlags; w += wrapInc)
        {
            //Wrap booleans.
            boolean
            wxBit = ((0b01 & w) == 0b01),
            wyBit = ((0b10 & w) == 0b10);

            add_lighting
            (
                //UL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? scaleHeight - swy : 0),
                zPos + ((wyBit) ? ((scaleHeight - swy) * shear) : 0),
                //
                //UR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? scaleHeight - swy : 0),
                zPos + ((wyBit) ? ((scaleHeight - swy) * shear) : 0),
                //
                //DR
                xPos + ((wxBit) ? scaleWidth : scaleWidth - swx),
                yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                zPos + ((wyBit) ? (scaleHeight * shear) : ((scaleHeight - swy) * shear)),
                //
                //DL
                xPos + ((wxBit) ? scaleWidth - swx : 0),
                yPos + ((wyBit) ? scaleHeight : scaleHeight - swy),
                zPos + ((wyBit) ? (scaleHeight * shear) : ((scaleHeight - swy) * shear)),
                //
                blendingColor, emission, sheet,//sprite, normalMap,
                //wrapFlags & w, wrapX, wrapY,
                (wxBit) ? midX1 : startX,
                (wxBit) ? startX : midX0,
                (wyBit) ? midY1 : startY,
                (wyBit) ? startY : midY0,
                //
                (wxBit) ? norm_midX1 : norm_startX,
                (wxBit) ? norm_startX : norm_midX0,
                (wyBit) ? norm_midY1 : norm_startY,
                (wyBit) ? norm_startY : norm_midY0//,
                //fixed
            );
        }
    }

    
    @Override
    public void renderTile_Ent(int xPos, int yPos, int zPos, Sprite sprite, Sprite normalMap, float emission,
    int wrapX, int wrapY, Vector4f blendingColor, float scale, byte shearType, float shear, boolean fixed)
    {
        //renderTile(xPos, yPos, zPos, sprite, normalMap, flip, wrapX, wrapY, blendingColor, scale, shearType, shear, fixed);

        lighting_TileRenderFunctions[shearType].invoke
        (
            xPos, yPos, zPos, sprite, normalMap, emission,
            wrapX, wrapY, blendingColor, scale, shear, true
        );
    }




    @Override
    /**Adds a Shadow Volume to the Shadow Buffers.*/
    public void applyShadow(ShadowVolume shadow, float scale, boolean fixed)
    {
        //Get faces.
        ShadowFace[] shadowFaces = shadow.getShadowFaces();

        //For each one.
        for(int f = 0; f < shadowFaces.length; f++)
        {
            //Cache face.
            ShadowFace face = shadowFaces[f];

            //Get points.
            Vector3f[] points = face.getPoints();
            Vector3f p0 = points[0], p1 = points[1],
            p2 = points[2], p3 = points[3];

            //Add the face to a buffer.
            add_shadowVolume
            (
                p0.x * scale, p0.y * scale, p0.z * scale,
                p1.x * scale, p1.y * scale, p1.z * scale,
                p2.x * scale, p2.y * scale, p2.z * scale,
                p3.x * scale, p3.y * scale, p3.z * scale, fixed
            );
        }
    }

    @Override
    /**Adds a Shadow Volume to the Shadow Buffers.*/
    public void applyShadow(ShadowSilhouette shadow, float scale, boolean fixed)
    {
        //Get points.
        Vector3f[] points = shadow.getPoints();
        Vector3f p0 = points[0], p1 = points[1],
        p2 = points[2], p3 = points[3];

        //Add the face to a buffer.
        add_shadowSilhouette
        (
            p0.x * scale, p0.y * scale, p0.z * scale,
            p1.x * scale, p1.y * scale, p1.z * scale,
            p2.x * scale, p2.y * scale, p2.z * scale,
            p3.x * scale, p3.y * scale, p3.z * scale,
            //
            shadow.silhouete, 0, 0, 0, fixed
        );
    }




    @Override
    /**Renders a sprite using any combination of affine transformations.*/
    public void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed){}
    public void renderSprite_Affine_2D(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed){}
    public void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed){}
    public void renderSprite_Affine(Matrix4f matrix, Matrix4f invertedMatrix, Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {

    }
    
    @Override
    /**Renders a sprite using 4 points.*/
    public void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {renderSprite_Quad(x0, y0, 0, x1, y1, 0, x2, y2, 0, x3, y3, 0, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, fixed);}
    //
    public void renderSprite_Quad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {renderSprite_Quad(x0, y0, 0, x1, y1, 0, x2, y2, 0, x3, y3, 0, sprite, flip, wrapX, wrapY, blendingColor, fixed);}
    //
    public void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Sprite sprite, byte flip, int wrapX, int wrapY, boolean fixed)
    {renderSprite_Quad(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, sprite, flip, wrapX, wrapY, Screen.DEFAULT_BLEND, fixed);}
    //
    public void renderSprite_Quad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Sprite sprite, byte flip, int wrapX, int wrapY, Vector4f blendingColor, boolean fixed)
    {
        //Wrap stuff.
        wrapX %= sprite.getWidth();
        wrapY %= sprite.getHeight();
        //int swx = (int)(wrapX * scale);
        //int swy = (int)(wrapY * scale);
        //int swz = (int)((wrapY * 2) * scale);

        //int wrapFlags = ((wrapY != 0) ? 0b10 : 0) | ((wrapX != 0) ? 0b01 : 0),
        //wrapInc = (wrapFlags == 0b10) ? 2 : 1;


        //Texture Coordinate stuff.
        SpriteSheet sheet = sprite.getSheet();

        //boolean
        //flipX = ((0b01 & flip) == 0b01),
        //flipY = ((0b10 & flip) == 0b10);

        //float
        //startX =    ((flipX) ? sprite.getX() + sprite.getWidth() - wrapX : sprite.getX() + wrapX) / (float)sheet.getWidth(),
        //midX0 =     ((flipX) ? sprite.getX() : sprite.getX() + sprite.getWidth()) / (float)sheet.getWidth(),
        //midX1 =     ((flipX) ? sprite.getX() + sprite.getWidth() : sprite.getX()) / (float)sheet.getWidth(),
        //
        //startY =    ((flipY) ? sprite.getY() + sprite.getHeight() - wrapY : sprite.getY() + wrapY) / (float)sheet.getHeight(),
        //midY0 =     ((flipY) ? sprite.getY() : sprite.getY() + sprite.getHeight()) / (float)sheet.getHeight(),
        //midY1 =     ((flipY) ? sprite.getY() + sprite.getHeight() : sprite.getY()) / (float)sheet.getHeight();
        //startX -> rl -> startX


        add_basic
        (
            x0, y0, z0,
            x1, y1, z1,
            x2, y2, z2,
            x3, y3, z3,
            //
            blendingColor, sheet,//sprite,
            //0, 0, 0,
            0.0f,
            1.0f,
            0.0f,
            1.0f,
            fixed
        );
    }


    /*
     * Debug Stuff
     */

    @Override
    /**Renders a Point.*/
    public void drawPoint(int xPos, int yPos, Vector4f pointColor, boolean fixed)
    {drawPoint(xPos, yPos, 0, pointColor, fixed);}

    /**Renders a Point taking this game's z-coordinate into account.*/
    public void drawPoint(int xPos, int yPos, int zPos, Vector4f pointColor, boolean fixed)
    {
        add_circle(xPos, yPos, zPos, 3.0f, pointColor, 3, fixed);
    }
    

    @Override
    /**Renders a Line.*/
    public void drawLine(int x0, int y0, int x1, int y1, Vector4f color, boolean fixed)
    {drawLine(x0, y0, 0, x1, y1, 0, color, fixed);}
    //
    public void drawLine(int x0, int y0, int z0, int x1, int y1, int z1, Vector4f color, boolean fixed)
    {
        //System.out.println("Line");
        add_line(x0, y0, z0, x1, y1, z1, color, fixed);
    }

    @Override
    /**Renders a Rect.*/
    public void drawRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed)
    {drawRect(xPos, yPos, 0, w, h, vecColor, fixed);}
    //
    public void drawRect(int xPos, int yPos, int zPos, int w, int h, Vector4f vecColor, boolean fixed)
    {
        w--;
        h--;

        add_line(xPos-1,    yPos,   zPos,   xPos+w, yPos,   zPos, vecColor, fixed);
        add_line(xPos+w,    yPos+1, zPos,   xPos+w, yPos+h, zPos, vecColor, fixed);
        //
        add_line(xPos-1,    yPos+h, zPos,   xPos+w,   yPos+h, zPos, vecColor, fixed);
        add_line(xPos,      yPos+1, zPos,   xPos,   yPos+h, zPos, vecColor, fixed);
    }

    @Override
    public void drawCroppedRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed)
    {drawCroppedRect(xPos, yPos, 0, w, h, vecColor, fixed);}
    //
    public void drawCroppedRect(int xPos, int yPos, int zPos, int w, int h, Vector4f vecColor, boolean fixed)
    {
        w--;
        h--;

        int
        left = (xPos < CROP_X) ? CROP_X : xPos,
        right = (xPos+w > CROP_X + CROP_WIDTH) ? CROP_X + CROP_WIDTH : xPos+w,
        up = (yPos < CROP_Y) ? CROP_Y : yPos,
        down = (yPos+h > CROP_Y + CROP_HEIGHT) ? CROP_Y + CROP_HEIGHT : yPos+h;

        if(yPos >= CROP_Y){add_line(left-1, yPos, zPos, right, yPos, zPos, vecColor, fixed);}
        if(xPos <= CROP_X + CROP_WIDTH){add_line(xPos+w, up+1, zPos, xPos+w, down, zPos, vecColor, fixed);}
        //
        if(yPos <= CROP_Y + CROP_HEIGHT){add_line(left-1, yPos+h, zPos,   right,  yPos+h, zPos, vecColor, fixed);}
        if(xPos >= CROP_X){add_line(xPos, up+1, zPos, xPos, down, zPos, vecColor, fixed);}
        
    }


    @Override
    public void fillRect(int xPos, int yPos, int w, int h, Vector4f vecColor, boolean fixed)
    {renderSprite_St(xPos, yPos, 0, 0, Sprites.whiteSprite, Sprite.FLIP_NONE, 0, 0, vecColor, w, h, fixed);}
    //
    public void fillRect(int xPos, int yPos, int zPos, int depth, int w, int h, Vector4f vecColor, boolean fixed)
    {renderSprite_St(xPos, yPos, zPos, depth, Sprites.whiteSprite, Sprite.FLIP_NONE, 0, 0, vecColor, w, h, fixed);}


    @Override
    /**Draws a Quad.*/
    public void drawQuad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3,
    Vector4f vecColor, boolean fixed)
    {drawQuad(x0, y0, 0, x1, y1, 0, x2, y2, 0, x3, y3, 0, vecColor, fixed);}

    @Override
    /**Draws a Quad.*/
    public void drawQuad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3,
    Vector4f vecColor, boolean fixed)
    {
        add_line(x0, y0, z0, x1, y1, z1, vecColor, fixed);
        add_line(x1, y1, z1, x2, y2, z2, vecColor, fixed);
        add_line(x2, y2, z2, x3, y3, z3, vecColor, fixed);
        add_line(x3, y3, z3, x0, y0, z0, vecColor, fixed);
    }


    @Override
    /**Renders a filled Quad.*/
    public void fillQuad(int x0, int y0, int x1, int y1, int x2, int y2, int x3, int y3, Vector4f vecColor, boolean fixed)
    {renderSprite_Quad(x0, y0, 0, x1, y1, 0, x2, y2, 0, x3, y3, 0, Sprites.whiteSprite, Sprite.FLIP_NONE, 0, 0, vecColor, fixed);}
    //
    public void fillQuad(int x0, int y0, int z0, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3, Vector4f vecColor, boolean fixed)
    {renderSprite_Quad(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, Sprites.whiteSprite, Sprite.FLIP_NONE, 0, 0, vecColor, fixed);}


    @Override
    /**Renders a Circle.*/
    public void drawCircle(int xPos, int yPos, float radius, int thickness, Vector4f vecColor, boolean fixed)
    {drawCircle(xPos, yPos, 0, radius, thickness, vecColor, fixed);}

    @Override
    public void drawCircle(int xPos, int yPos, int zPos, float radius, int thickness, Vector4f vecColor, boolean fixed)
    {
        add_circle(xPos, yPos, zPos, radius, vecColor, thickness, fixed);
    }
}
