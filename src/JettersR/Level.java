package JettersR;
/**
 * This is what manages everything in a single Level, or Scene, of the Game.
 * 
 * Author: Luke Sullivan
 * Last Edit: 9/3/2023
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Graphics.*;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Graphics.SpriteRenderers.SpriteRenderer;
import JettersR.Tiles.*;
import JettersR.Tiles.Graphics.*;
import JettersR.Entities.Entity;
import JettersR.Entities.Components.CollisionObject;
import JettersR.Entities.Components.PhysicsComponent;
import JettersR.Entities.Components.Lights.Light;
import JettersR.GameStates.GameStateManager;
import JettersR.Util.Octree;
import JettersR.Util.fixedVector3;
import JettersR.Util.Shapes.Shapes3D.Shape_Box;
import JettersR.Util.Annotations.fixed;
import JettersR.Util.Fixed;

import static JettersR.Util.Fixed.*;

public class Level
{
    public static final String levelsPath = "assets/Levels/";
    public static final int TILE_BITS = 5, TILE_SIZE = 1 << TILE_BITS,//32
    FIXED_TILE_BITS = TILE_BITS + Fixed.f_FRACTION_BITS, FIXED_TILE_SIZE = fixed(TILE_SIZE);
    
    //Dimensions.
    private int width = 0, height = 0, depth = 0;

    //Camera for fancy camera boundary stuff.
    private LevelCamera camera = null;

    //List containing all the Entities in the map.
    private ArrayList<Entity> entities = new ArrayList<Entity>(),
    newEntities = new ArrayList<Entity>();

    //List containing entity slots to remove.
    private List<Integer> shouldRemoves = new ArrayList<Integer>(32);
    private int entitiesToRemove = 0;

    //This is used to sort the entitiesRender list in order of Back to Front, Bottom to Top.
    public Comparator<Entity> entitySorter = new Comparator<Entity>()
    {
        @Override
        public int compare(Entity a, Entity b)
        {
            //a < b = -1, a > b = 1
            int x = 0;
            /*
             * if(a.z > b.zDepth){x = 1;}//Above
             * else if(a.zDepth < b.z){x = -1;}//Below
             * else//Same elevation
             * {
             *      if(a.y > b.yHeight){x = 1;}//Front
             *      else if(a.yHeight < b.y){x = -1;}//Behind
             * }
             */

            //Contract: a > b must equal b < a. Otherwise, it throws an error.
            if(a.f_getZ() > b.f_getZ()){x = 1;}
            else if (a.f_getZ() < b.f_getZ()){x = -1;}
            else
            {
                if(a.f_getY() > b.f_getY()){x = 1;}
                else if(a.f_getY() < b.f_getY()){x = -1;}
            }
            return x;
        }
    };


    //
    //Lists, for keeping track of individual components.
    //

    //List containing all collision components to be checked with.
    private List<CollisionObject> collisionComponents = new ArrayList<CollisionObject>();

    //List containing all physics components to be ran.
    private List<PhysicsComponent> physicsComponents = new ArrayList<PhysicsComponent>();
    
    //List containing all SpriteRenderers to be reflected.
    private List<SpriteRenderer> spriteRenderers = new ArrayList<SpriteRenderer>();

    //List containing all Lights in the level.
    private List<Light> lights = new ArrayList<Light>();

    //List Containing all Shadow Volumes in the map.
    private List<ShadowVolume> shadowVolumes = new ArrayList<ShadowVolume>();


    //
    //Octrees, meant to reduce the number of calculations for the corresponding tree.
    //

    //Helps reduce collision checks.
    private Octree<CollisionObject> collisionObject_Octree = null;

    //Mostly used for Reflections.
    //private Octree<SpriteRenderer> spriteRenderer_Octree = null;

    //Helps with on-screen light checks.
    private Octree<Light> light_Octree = null;

    //Helps with on-screen shadow volume checks.
    //private Octree<ShadowVolume> staticShadow_Octree = null,
    //dynamicShadow_Octree  = null;

    

    //Global Light stuff.
    private Vector3f globalLight_Direction = new Vector3f(0.0f, 0.0f, -1.0f), //globalLight_ShadowInc = new Vector3f(),
    globalLight_Diffuse = new Vector3f(1.0f),
    globalLight_Ambient = new Vector3f(0.5f);

    //Pixel Array representing all of the Tiles in the map.
    private int[] tiles;
    private TileMesh[] tileMeshs = {null};
    private TileAnimation[] tileAnimations;

    //Array containing currently used Tile Materials.
    //private Material[] materials = new Material[16];


    /**
     * Loads a Level from a given Folder Directory.
     */
    public Level(final File levelFolder)
    {
        //Get the files in the given folder.
        File[] files = levelFolder.listFiles();

        //TileKey variables.
        String[] tileSet_pathNames = null;
        File tileKeyFile = null;

        for(int i = 0; i < files.length; i++)
        {
            File file = files[i];
            String fileName = file.getName();

            //Is this the TileData folder?
            if(fileName.equals("TileData"))
            {
                //Load it.
                loadTileData(file);
            }

            //Is this the tileKey file?
            else if(fileName.equalsIgnoreCase("tilekey.dat"))
            {
                //We need to wait until we get the pathNames for all the SpriteSheets used.
                tileKeyFile = file;
            }

            //Is this the tileSets file?
            else if(fileName.equalsIgnoreCase("tilesets.txt"))
            {
                //Create String list.
                List<String> stringList = new ArrayList<String>();

                try
                {
                    //Create a scanner to read the file.
                    Scanner scanner = new Scanner(file);

                    while(scanner.hasNextLine())
                    {
                        //Get current line of text.
                        String line = scanner.nextLine();

                        //Empty line check.
                        if(fileName.equals("")){continue;}

                        //Add line to list of names.
                        stringList.add(line);
                    }

                    //Close the scanner.
                    scanner.close();
                }
                catch(FileNotFoundException e){e.printStackTrace();}

                //Convert to array.
                tileSet_pathNames = new String[stringList.size()];
                stringList.toArray(tileSet_pathNames);
            }

            //Load Tile Materials.
            //else if(fileName.equalsIgnoreCase("materials.txt")){loadMaterials(files[i]);}

            //Is this the entities file?
            else if(fileName.equalsIgnoreCase("entities.json"))
            {
                //Load them.
                loadEntities(file);
            }
        }

        //Load the TileKey.
        loadTileKey_DAT(tileSet_pathNames, tileKeyFile);

        //Create this Level's Camera.
        this.camera = new LevelCamera(this);

        //Create Octrees.
        createOctrees();

        //Initialize every entity.
        for(int i = 0; i < entities.size(); i++)
        {entities.get(i).init(this);}
    }

    public Level(String path){this(new File(levelsPath + path));}


    /**Loads a Level from a given TileSet with the rest of the Level data.*/
    public Level(final File tileDataFolder, final File tileKeyFile, final Sprite[][] tileSet_Sprites, final File entitiesFile)
    {
        //Load TileData.
        loadTileData(tileDataFolder);

        //Load the TileKey (TileMeshs and TileAnimations).
        loadTileKey_DAT(tileSet_Sprites, tileKeyFile);

        //Load all the entities.
        loadEntities(entitiesFile);


        //Create Camera.
        this.camera = new LevelCamera(this);

        //Create Octrees.
        createOctrees();

        //Initialize every entity.
        for(int i = 0; i < entities.size(); i++)
        {entities.get(i).init(this);}
    }


    /**Creates an empty level of the given dimensions.*/
    public Level(final int width, final int height, final int depth)
    {
        //Create Camera.
        this.camera = new LevelCamera(this);

        //Set Dimensions
        this.width = width;
        this.height = height;
        this.depth = depth;

        tiles = new int[(width * height) * depth];
        tileAnimations = new TileAnimation[0];

        //Create Octree
        createOctrees();
    }

    private int ts = 0;
    private final int _void = tileValue(ts++, 0, 0, 0b000000),
    _solid = tileValue(ts++, 1, 0, 0b111111),
    _solid_forceDown = tileValue(ts++, 1, 0, 0b001000),
    _solidTop = tileValue(ts++, 1, 0, 0b111111),
    _stR = tileValue(ts++, 1,  0, 0b111100),
    _stG = tileValue(ts++, 1, 0, 0b101111),
    //
    _leftSlope = tileValue(ts++, 4, 0, 0b111111),
    _rightSlope = tileValue(ts++, 5, 0, 0b111111),
    _upSlope = tileValue(ts++, 6, 0, 0b111111),
    _downSlope = tileValue(ts++, 7, 0, 0b111111),
    //
    _horiSlopeS0 = tileValue(ts++, 8, 0, 0b101101),
    _horiSlopeS1 = tileValue(ts++, 9, 0, 0b101100),
    _horiSlopeS2 = tileValue(ts++, 10, 0, 0b101100),
    _horiSlopeS3 = tileValue(ts++, 11, 0, 0b101110),
    //
    _horiSlopeT0 = tileValue(ts++, 16, 0, 0b101111),
    _horiSlopeT1 = tileValue(ts++, 17, 0, 0b101100),
    _horiSlopeT2 = tileValue(ts++, 18, 0, 0b101100),
    _horiSlopeT3 = tileValue(ts++, 19, 0, 0b101110),
    //
    _DRIso = tileValue(ts++, 27, 0, 0b111111),
    //
    _URIsoT0 = tileValue(1, 38, 0, 0b100111),
    _URIsoT1 = tileValue(1, 39, 0, 0b100011),
    _DRIsoT1 = tileValue(ts++, 43, 0, 0b101001),
    _DRIsoT0 = tileValue(ts++, 42, 0, 0b101001);
    
    /**Test Constructor.*/
    public Level(final Sprite[] tileSet_Sprites)
    {
        //Create Camera.
        this.camera = new LevelCamera(this);

        //Set Dimensions
        this.width = 5;
        this.height = 5;
        this.depth = 3;//4;

        //Set Global Light Direction.
        globalLight_Direction.set(1.0f, -0.0f, -0.75f).normalize();

        //Stuff for Shadow Testing.
        float dLength;
        if(globalLight_Direction.z != 0.0f){dLength =  TILE_SIZE / globalLight_Direction.z;}
        else{dLength =  TILE_SIZE / globalLight_Direction.x;}
        
        //globalLight_ShadowInc.x = dLength * globalLight_Direction.x;
        //globalLight_ShadowInc.y = dLength * globalLight_Direction.y;
        //globalLight_ShadowInc.z = (globalLight_Direction.z < 0.0f) ? -TILE_SIZE : (globalLight_Direction.z == 0.0f) ? 0 : TILE_SIZE;

        //Set Global Light Color.
        globalLight_Diffuse.set(1.0, 0.8, 0.3);
        globalLight_Ambient.set(0.75, 0.3, 0.15);

        //globalLight_Diffuse.set(0.0, 0.3, 0.15);
        //globalLight_Ambient.set(0.0, 0.1, 0.07);

        //globalLight_Diffuse.set(1.0, 0.5, 1.0);
        //globalLight_Ambient.set(0.25, 0.0, 0.5);

        //globalLight_Diffuse.set(1.0, 1.0, 1.0);
        //globalLight_Ambient.set(0.5, 0.5, 0.5);

        //globalLight_Diffuse.set(0.0, 0.0, 0.0);
        //globalLight_Ambient.set(0.0, 0.0, 0.0);

        tiles = new int[]
        {
            _solidTop, _solidTop, _solid, _solid, _solid,
            _solidTop, _solidTop, _solidTop, _solidTop, _solidTop,
            _solidTop, _solidTop, _solidTop, _solidTop, _solid,
            _solidTop, _solidTop, _solid, _solid, _void,
            _solid, _downSlope, _void, _leftSlope, _solid,
            //
            _URIsoT0, _void, _solid, _solid_forceDown, _solid_forceDown,
            _stG, _stG, _horiSlopeS0, _horiSlopeS1, _horiSlopeS3,
            _DRIsoT1, _void, _void, _void, _void,
            _void, _void, _horiSlopeT1, _stR, _horiSlopeT2,
            _void, _void, _void, _void, _void,
            //
            _void, _void, _solid, _solid_forceDown, _solid_forceDown,
            _void, _void, _void, _void, _void,
            _void, _void, _void, _void, _void,
            _void, _void, _void, _void, _void,
            _void, _void, _void, _void, _void
        };

        /*
        tiles = new int[]
        {
            _solid, _solid, _solid, _solid, _solid,
            _solid, _solid, _solid, _solid, _solid,
            _solid, _solid, _solid, _solid, _solid,
            _solid, _solid, _solid, _solid, testVoid,
            _solid, testDownSlope, _solid, testVoid, testDRIso,
            //
            testVoid, testVoid, testVoid, _solid, _solid,
            _solid, testVoid, testLeftSlope, _solid, _solid,
            testVoid, testVoid, testVoid, testVoid, _solid,
            testVoid, testDownSlope, testDRIso, testVoid, testVoid,
            testVoid, testVoid, testVoid, testVoid, testVoid,
            //
            testVoid, testVoid, testVoid, _solid, _solid,
            testVoid, testVoid, testVoid, testVoid, testVoid,
            testVoid, testVoid, testVoid, testVoid, testUpSlope,
            testVoid, testVoid, testVoid, testVoid, testTransparent_R,
            testVoid, testVoid, testVoid, testVoid, testVoid,
            //
            testVoid, testVoid, testVoid, _solid, _solid,
            testVoid, testVoid, testVoid, testVoid, testVoid,
            testVoid, testVoid, testVoid, testVoid, testVoid,
            testVoid, testVoid, testVoid, testVoid, testVoid,
            _solid, testVoid, testVoid, testVoid, testVoid,
        };
        */
        //SpriteSheet tileSet = Sprites.loadTileSet("GenericTiles");
        
        Material m = Materials.get(0);
        tileMeshs = new TileMesh[]
        {
            null,

            //Block
            new TileMesh
            (
                //null,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[1]},
                new Sprite[]{tileSet_Sprites[35], tileSet_Sprites[36]},
                new byte[]{0, 1},//Indecies.
                new byte[]//Offsets.
                {
                    0, 0, 0,
                    0, 32, 0
                },
                new byte[]{TileMesh.SHEARTYPE_FLOOR, TileMesh.SHEARTYPE_WALL},//ShearTypes.
                new float[]{0f, 0f},//Shear amounts.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Block that forces down.
            new TileMesh
            (
                //null,
                new Vector4f(0.0f, 0.5f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[1]},
                new Sprite[]{tileSet_Sprites[35], tileSet_Sprites[36]},
                new byte[]{0, 1},//Indecies.
                new byte[]//Offsets.
                {
                    0, 0, 0,
                    0, 32, 0
                },
                new byte[]{TileMesh.SHEARTYPE_FLOOR, TileMesh.SHEARTYPE_WALL},//ShearTypes.
                new float[]{0f, 0f},//Shear amounts.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Block Top
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                //new Vector4f(1.0f, 0.5623f, 0.0f, 1.0f),//Emission = 2, Diffuse + (Diffuse * Ambient)
                //
                tileSet_Sprites[0],
                tileSet_Sprites[35],
                new byte[]{0, 0, 0},//Offsets.
                TileMesh.SHEARTYPE_FLOOR,//ShearType.
                0f,//Shear amount.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //R
            new TileMesh
            (
                new Vector4f(1.0f, 0.1f, 0.1f, 0.65f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[1]},
                new Sprite[]{tileSet_Sprites[35], tileSet_Sprites[36]},
                new byte[]{0, 1},//Indecies.
                new byte[]{0, 0, 0,  0, 32, 0},//Offsets.
                new byte[]{TileMesh.SHEARTYPE_FLOOR, TileMesh.SHEARTYPE_WALL},//ShearTypes.
                new float[]{0f, 0f},//Shear amounts.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //G
            new TileMesh
            (
                new Vector4f(0.1f, 1.0f, 0.1f, 0.65f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[1]},
                new Sprite[]{tileSet_Sprites[35], tileSet_Sprites[36]},
                new byte[]{0, 1},//Indecies.
                new byte[]{0, 0, 0,  0, 32, 0},//Offsets.
                new byte[]{TileMesh.SHEARTYPE_FLOOR, TileMesh.SHEARTYPE_WALL},//ShearTypes.
                new float[]{0f, 0f},//Shear amounts.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            

            //Left Slope
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[3]},
                new Sprite[]{tileSet_Sprites[40], tileSet_Sprites[38]},
                new byte[]{0, 1},//Indecies.
                new byte[]{0, 0, -TILE_SIZE,  0, 32, 0},//Offsets.
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{1f, 0f},//Shear amounts.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Right Slope
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[4]},
                new Sprite[]{tileSet_Sprites[41], tileSet_Sprites[39]},
                new byte[]{0, 1},//Indecies.
                new byte[]{0, 0, 0,  0, 32, 0},//Offsets.
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{-1f, 0f},//Shear amounts.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Up Slope
            new TileMesh
            (
                new Vector4f(0.2f, 0.7f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[1]},
                new Sprite[]{tileSet_Sprites[42], tileSet_Sprites[36]},
                new byte[]{0, 1},//Indecies.
                new byte[]{0, 0, -TILE_SIZE,  0, 32, 0},
                new byte[]{TileMesh.SHEARTYPE_ZY, TileMesh.SHEARTYPE_WALL},
                new float[]{1f, 0f},//Shear amounts.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Down Slope
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                tileSet_Sprites[0],
                tileSet_Sprites[43],
                new byte[]{0, 0, 0},
                TileMesh.SHEARTYPE_ZY,//Shear Type.
                -1f,//Shear amount.
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),


            //Left Slope Portion Short0
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[5]},
                new Sprite[]{tileSet_Sprites[48], tileSet_Sprites[44]},
                new byte[]{0, 1},
                new byte[]{0, 0, -TILE_SIZE,  0, 32, -16},
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{0.5f, 0},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Left Slope Portion Short1
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[6]},
                new Sprite[]{tileSet_Sprites[48], tileSet_Sprites[45]},
                new byte[]{0, 1},
                new byte[]{0, 0, -TILE_SIZE/2,  0, 32, 0},
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{0.5f, 0f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Right Slope Portion Short1
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[7]},
                new Sprite[]{tileSet_Sprites[49], tileSet_Sprites[46]},
                new byte[]{0, 1},
                new byte[]{0, 0, 0,  0, 32, 0},
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{-0.5f, 0},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Right Slope Portion Short0
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[0], tileSet_Sprites[8]},
                new Sprite[]{tileSet_Sprites[49], tileSet_Sprites[47]},
                new byte[]{0, 1},
                new byte[]{0, 0, -TILE_SIZE >> 1,  0, 32, -TILE_SIZE >> 1},
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{-0.5f, 0f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),



            //Left Slope Portion Tall0
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[9], tileSet_Sprites[10], tileSet_Sprites[9]},
                new Sprite[]{tileSet_Sprites[58], tileSet_Sprites[53], tileSet_Sprites[52]},
                new byte[]{0, 1, 2},
                new byte[]
                {
                    0, 0, -TILE_SIZE,
                    0, 32, 0,
                    TILE_SIZE >> 1, 0, 0
                },
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL, TileMesh.SHEARTYPE_FLOOR},
                new float[]{2f, 0f, 0f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Left Slope Portion Tall1
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[9], tileSet_Sprites[11]},
                new Sprite[]{tileSet_Sprites[59], tileSet_Sprites[54]},
                new byte[]{0, 1},
                new byte[]
                {
                    TILE_SIZE >> 1, 0, -TILE_SIZE,
                    TILE_SIZE >> 1, 32, 0
                },
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{2f, 0f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Right Slope Portion Tall1
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[9], tileSet_Sprites[12]},
                new Sprite[]{tileSet_Sprites[60], tileSet_Sprites[55]},
                new byte[]{0, 1},
                new byte[]
                {
                    0, 0, 0,
                    0, 32, 0
                },
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL},
                new float[]{-2f, 0f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //Right Slope Portion Tall0
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[9], tileSet_Sprites[13], tileSet_Sprites[9]},
                new Sprite[]{tileSet_Sprites[61], tileSet_Sprites[56], tileSet_Sprites[52]},
                new byte[]{0, 1, 2},
                new byte[]
                {
                    TILE_SIZE >> 1, 0, 0,
                    0, 32, 0,
                    0, 0, 0
                },
                new byte[]{TileMesh.SHEARTYPE_ZX, TileMesh.SHEARTYPE_WALL, TileMesh.SHEARTYPE_FLOOR},
                new float[]{-2f, 0f, 0f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),


            //DR Iso
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[17], tileSet_Sprites[1]},
                new Sprite[]{tileSet_Sprites[65], tileSet_Sprites[67]},
                new byte[]{0, 1},
                new byte[]{0, 0, 0,  0, 32, 0},
                new byte[]{TileMesh.SHEARTYPE_FLOOR, TileMesh.SHEARTYPE_Y},
                new float[]{0f, -1f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),


            //DR Iso Portion Tall1
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[33], tileSet_Sprites[34], tileSet_Sprites[34]},
                new Sprite[]{tileSet_Sprites[85], tileSet_Sprites[88], tileSet_Sprites[86]},
                new byte[]{0, 1, 2},
                new byte[]
                {
                    0, 0, 0,
                    16, 32, 0,
                    0, TILE_SIZE, 0
                },
                new byte[]{TileMesh.SHEARTYPE_FLOOR, TileMesh.SHEARTYPE_Y, TileMesh.SHEARTYPE_WALL},
                new float[]{0f, -2f, 0f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),

            //DR Iso Portion Tall0
            new TileMesh
            (
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Sprite[]{tileSet_Sprites[32], tileSet_Sprites[34]},
                new Sprite[]{tileSet_Sprites[84], tileSet_Sprites[88]},
                new byte[]{0, 1},
                new byte[]
                {
                    0, 0, 0,
                    0, 32, 0
                },
                new byte[]{TileMesh.SHEARTYPE_FLOOR, TileMesh.SHEARTYPE_Y},
                new float[]{0f, -2f},
                //Sprite.FLIP_NONE,//Flip.
                0.0f, m
            ),
        };
        tileAnimations = new TileAnimation[]
        {
            /*
            new Color_TileAnimation
            (
                new TileMesh[]{tileSprites[1]},
                new FrameAnimation_Timer
                (
                    new @fixed int[]
                    {
                        fixed(60,0),
                        fixed(60,0),
                        fixed(60,0),
                        fixed(60,0)
                    }
                ),
                new byte[]{2, 2, 2, 2},
                new byte[][]
                {
                    {(byte)255, 0, 0,  (byte)255, (byte)255, 0},
                    {(byte)255, (byte)255, 0,  0, (byte)255, 0},
                    {0, (byte)255, 0,  (byte)255, (byte)255, 0},
                    {(byte)255, (byte)255, 0,  (byte)255, 0, 0}
                }
            ),
            */
            
            /*
            new Wrap_TileAnimation
            (
                new TileMesh[]{tileSprites[1], tileSprites[2]},
                new FrameAnimation_Timer(0, 1, fixed(32)),
                new @fixed short[2],
                new byte[]{4},
                new @fixed short[][]
                {
                    {0, fixedShort(32)}
                }
            )
            */

            
            new SpriteIndex_TileAnimation
            (
                new TileMesh[]{tileMeshs[1]},
                new FrameAnimation_Timer(0, 2, fixed(30)),
                tileMeshs[1].getSpriteIndecies(),
                new byte[]{1, 0},
                new byte[][]
                {
                    {0, 1},
                    {1}
                }
            )
        

            /*
            new SpriteCoord_TileAnimation
            (
                new TileMesh[]{tileSprites[1], tileSprites[2]},
                new FrameAnimation_Timer(0, 1, fixed(60)),
                tileSet_Sprites[0],
                new byte[]{5},
                (byte)0,
                new short[][]
                {
                    {0, 32, 0, 32}
                }
            )
            */
        };
        //
        //materials[0] = new Material("Generic");
        //for(int i = 1; i < materials.length; i++){materials[i] = Material.NULL_MATERIAL;}

        //Calculate Shadow Volumes.
        calculateShadows();

        //Create Octrees.
        createOctrees();
    }
    


    public static int tileValue(int tileSpriteID, int tileTypeID)
    {return (tileSpriteID << Tiles.TILE_PROPERTIES_BITS) | tileTypeID;}

    public static int tileValue(int tileSpriteID, int tileShapeID, int tileEffectID, int tileForcesID)
    {
        return (tileSpriteID << Tiles.TILE_PROPERTIES_BITS)
        | (tileShapeID << Tiles.TILE_RESPONSE_BITS)
        | (tileEffectID << Tiles.TILE_FORCES_BITS)
        | tileForcesID;
    }



    //
    //Loading functions.
    //

    /**Loads TileData from a folder of .png images.*/
    private void loadTileData(final File folder)
    {
        //Get the files from it.
        File[] imageFiles = folder.listFiles();

        
        //Attempt to load each file as a .png image.
        BufferedImage[] images = new BufferedImage[imageFiles.length];
        for(int j = 0; j < imageFiles.length; j++)
        {
            try{images[j] = ImageIO.read(imageFiles[j]);}
            catch(IOException e){e.printStackTrace();}
        }
        
        
        //Set Dimensions.
        width = images[0].getWidth(); height = images[0].getHeight();
        depth = images.length;

        //Create the tileData array.
        this.tiles = new int[(width * height) * depth];

        //Finally, fill the tileData array with the information from the images.
        for(int z = 0; z < depth; z++)
        {
            images[z].getRGB(0, 0, width, height,
            this.tiles, (width * height) * z, width);

            //for(int i = 0; i < (width * height); i++)
            //{System.out.println(Integer.toHexString(tiles[i + ((width * height) * z)]));}
        }
    }


    /**Loads a list of Entities from the given folder.*/
    private void loadEntities(final File entitiesFile)
    {
        //Create ObjectMapper.
        ObjectMapper objectMapper = new ObjectMapper();
        
        //Allow setting fields of any visibility. Not having this will cause an UnknownProperty exception.
        objectMapper.setVisibility
        (
            objectMapper.getSerializationConfig().
            getDefaultVisibilityChecker().
            withFieldVisibility(JsonAutoDetect.Visibility.ANY).
            withGetterVisibility(JsonAutoDetect.Visibility.NONE).
            withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );

        //Allow deserialization of Entities, Java utilities, custom utilities, and custom data.
        //Not having this will cause deserialization to fail, since Entity is an abtract class.
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("JettersR.Entities")
        .allowIfSubType("JettersR.Util")//fixedVectors, Shapes, etc.
        .allowIfSubType("JettersR.Data")
        .allowIfSubType("java.util")//ArrayLists
        .build();

        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        //Allows deserialization of Objects, Abstract types, and Arrays/Lists of them.
        //objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);

        try
        {
            entities = objectMapper.readValue
            (
                entitiesFile,
                new TypeReference<ArrayList<Entity>>(){}
                //new TypeReference<Entity>(){}
            );
        }
        catch(Exception e){e.printStackTrace();}
    }
    

    /**Loads a TileKey from a given Sprite[][] and .dat file.*/
    private void loadTileKey_DAT(final Sprite[][] tileSet_Sprites, final File tileKey_file)
    {
        //Cache the first tileSet sprite layout.
        int tSh = 0;
        Sprite[] current_tileSet_sprites = tileSet_Sprites[tSh++];

        //Create lists to load TileMeshs and TileAnimations into.
        List<TileMesh> listMesh = new ArrayList<TileMesh>();
        List<TileAnimation> listAnim = new ArrayList<TileAnimation>();

        try
        {
            //Set up the input stream.
            FileInputStream fis = new FileInputStream(tileKey_file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            //Create byte array for reading.
            byte[] bb = null;

            //
            //Start loading TileMeshs.
            //
            //int numT = 0;
            int loadingPhase = 0;
            while(loadingPhase == 0)
            {
                //System.out.println("numT: " + numT);
               

                //
                //Number of Sprites held - 1 byte, signed.
                //
                byte numSpritesInTileMesh = (byte)bis.read();
                //System.out.println("numSpritesInTileMesh: " +  numSpritesInTileMesh);

                //If 0, start next TileSet.
                if(numSpritesInTileMesh == 0)
                {
                    //Load the next tileSet sprite layout.
                    current_tileSet_sprites = tileSet_Sprites[tSh++];

                    //Loop to next TileMesh.
                    continue;
                }
                //If -1, stop loading TileMeshs and start loading TileAnimations.
                else if(numSpritesInTileMesh == -1)
                {
                    loadingPhase = 1;
                    break;
                }

                //Otherwise, one array for sprites, the other for normal maps.
                Sprite[][] spritesInTileMesh = new Sprite[2][numSpritesInTileMesh];
                


                //
                //Sprite and NormalMap IDs - (numSpritesInTileMesh * 2) bytes, unsigned.
                //
                bb = new byte[2];
                for(int i = 0; i < 2; i++)//0 = sprites, 1 = normalMaps.
                {
                    for(int s = 0; s < numSpritesInTileMesh; s++)
                    {
                        //Get SpriteID.
                        bis.read(bb);
                        int spriteID = Game.bytesToShort(bb[0], bb[1]) & 0x0000FFFF;

                        //if(numT == 0)
                        //System.out.println("spriteID: " +  spriteID);
                        
                        //Use it to get Sprite and add it to array.
                        spritesInTileMesh[i][s] = current_tileSet_sprites[spriteID];
                    }
                }


                //
                //Number of Sprite Indecies - 1 byte, unsigned.
                //
                int numSpriteIndecies = bis.read() & 0xFF;
                //System.out.println("numSpriteIndecies: " +  numSpriteIndecies);
                

                //
                //Sprite Indecies - numSpriteIndecies bytes, signed.
                //
                byte[] spriteIndeciesInTileMesh = null;

                //Get first value.
                byte firstSpriteIndex = (byte)bis.read();

                //if(numT == 0)
                //System.out.println("firstSpriteIndex: " +  firstSpriteIndex);

                //If it's negative one, skip.
                if(firstSpriteIndex != -1)//TODO Why did I put this here?
                {
                    //Otherwise, create array.
                    spriteIndeciesInTileMesh = new byte[numSpriteIndecies];

                    //Put first value into it.
                    spriteIndeciesInTileMesh[0] = firstSpriteIndex;

                    //Zero was already done, so we start from one.
                    for(int i = 1; i < numSpriteIndecies; i++)
                    {
                        //Get index value and add it to array.
                        spriteIndeciesInTileMesh[i] = (byte)bis.read();

                        //if(numT == 0)
                        //System.out.println("spriteIndex: " +  spriteIndeciesInTileMesh[i]);
                    }
                }
                


                //
                //Offsets - 3 * numSpriteIndecies bytes, signed.
                //
                byte[] offsetsInTileMesh = new byte[3 * numSpriteIndecies];
                for(int i = 0; i < offsetsInTileMesh.length; i++)
                {
                    //Get offset value and add it to array.
                    offsetsInTileMesh[i] = (byte)bis.read();
                    //System.out.println("offset: " +  offsetsInTileMesh[i]);
                }


                //
                //ShearTypes - numSpriteIndecies bytes, signed.
                //
                byte[] shearTypesInTileMesh = new byte[numSpriteIndecies];
                for(int i = 0; i < numSpriteIndecies; i++)
                {
                    //Get shearType value and add it to array.
                    shearTypesInTileMesh[i] = (byte)bis.read();
                    //System.out.println("shearType: " +  shearTypesInTileMesh[i]);
                }

                
                //
                //ShearValues - numSpriteIndecies bytes, signed.
                //
                float[] shearValuesInTileMesh = new float[numSpriteIndecies];
                for(int i = 0; i < numSpriteIndecies; i++)
                {
                    //Get shearType value and add it to array.
                    shearValuesInTileMesh[i] = TileMesh.convertByteToShear( (byte)bis.read() );
                    //System.out.println("shearValue: " +  shearValuesInTileMesh[i]);
                }


                //
                //Color - 4 bytes [0-255].
                //
                bb = new byte[3];//3 for rgb.
                Vector4f colorInTileMesh = null;
                byte alpha = (byte)bis.read();
                //System.out.println("alpha: " +  alpha);

                //If the alpha value is zero, skip. We're getting it from an animation instead.
                if(alpha != 0)
                {
                    //Read R, G, and B.
                    bis.read(bb);

                    //Create color.
                    colorInTileMesh = Screen.bytesToVector4f(alpha, bb[0], bb[1], bb[2]);
                    //System.out.println("rgb: " + bb[0] + " " + bb[1] + " " + bb[2]);
                }


                //
                //Emission - 1 fixed point byte.
                //
                float emissionInTileMesh = f_toFloat(bis.read());
                //System.out.println("emission: " +  emissionInTileMesh);


                //
                //MaterialID - 1 byte.
                //
                Material material = Materials.get(bis.read());
                //System.out.println("material: " +  material);


                //Create new TileMesh and add it to the list.
                listMesh.add
                (
                    new TileMesh(colorInTileMesh, spritesInTileMesh[0], spritesInTileMesh[1], spriteIndeciesInTileMesh,
                    offsetsInTileMesh, shearTypesInTileMesh, shearValuesInTileMesh, emissionInTileMesh, material)
                );

                //numT++;
            }

            //Put the TileMesh in the list into an array. Slot 0 is a voidTile.
            this.tileMeshs = new TileMesh[listMesh.size()+1];
            for(int i = 1; i < tileMeshs.length; i++){tileMeshs[i] = listMesh.get(i-1);}



            //
            //Start loading TileAnimations.
            //
            while(loadingPhase == 1)
            {
                //
                //Number of TileMeshs affected - 1 byte, signed.
                //
                byte numTileMeshsInAnimation = (byte)bis.read();
                //System.out.println("numTileMeshsInAnimation: " + numTileMeshsInAnimation);

                //If -1, we have reached the end of the file, and are done loading from the file.
                if(numTileMeshsInAnimation < 0)
                {
                    loadingPhase = 2;//Probably not needed.
                    break;
                }

                //Create array.
                TileMesh[] tileSpritesInAnimation = new TileMesh[numTileMeshsInAnimation];


                //
                //TileMesh IDs - (2 * numSpritesInTileMesh) bytes, unsigned.
                //
                bb = new byte[2];
                for(int a = 0; a < numTileMeshsInAnimation; a++)
                {
                    //Get two bytes.
                    bis.read(bb);

                    //Combine them, unsigned.
                    int tileSpriteID = Game.bytesToShort(bb[0], bb[1]) & 0xFFFF;
                    //System.out.println("tileSpriteID: " + tileSpriteID);

                    //Use it to get a TileMesh and add it to array.
                    tileSpritesInAnimation[a] = tileMeshs[tileSpriteID];
                }


                //
                //Number of Frames - 1 byte, unsigned.
                //
                int numFrames = bis.read() & 0xFF;
                //System.out.println("numFrames: " + numFrames);


                //
                //Animation Type - 1 byte, signed.
                //
                byte animationType = (byte)bis.read();
                //System.out.println("animationType: " + animationType);
                byte spriteSlot = -1;//<- Won't always be used.

                //If -1...
                TileAnimation toGetPointerFrom = null;
                if(animationType == -1)
                {
                    //The next byte is the TileAnimation ID to copy it's type and pointer of.
                    int animationID = bis.read() & 0xFF;
                    //System.out.println("animationIDToCopy: " + animationID);
                    toGetPointerFrom = listAnim.get(animationID);
                    animationType = toGetPointerFrom.typeValue();
                }

                //If SpriteCoord or SpriteIndex, get spriteSlot.
                if(animationType == TileAnimation.TYPE_SPRITE_COORD || animationType == TileAnimation.TYPE_SPRITE_INDEX)
                {
                    //For sprite coordinate animations, this is the slot from the first TileMesh's sprite array this animation should use.
                    //(We have to do it this way since we disposed of the sprite arrays earlier)
                    spriteSlot = (byte)bis.read();
                    //For index animations, this is how many slots are in the index array.

                    //System.out.println("spriteSlot: " + spriteSlot);
                }


                //Create arrays.
                bb = new byte[4];
                @fixed int[] f_frameTimes = new @fixed int[numFrames];
                byte[] actionIDs = new byte[numFrames];

                //This will be translated as needed.
                byte[][] actionParameters = new byte[numFrames][];
                
                //For each frame of the Animation...
                for(int i = 0; i < numFrames; i++)
                {
                    //
                    //FrameTime - 4 fixed point bytes.
                    //
                    bis.read(bb);
                    f_frameTimes[i] = Game.bytesToInt(bb[0], bb[1], bb[2], bb[3]);
                    //f_print("f_frameTimes[" + i + "]", f_frameTimes[i]);


                    //
                    //ActionID - 1 byte, signed.
                    //
                    actionIDs[i] = (byte)bis.read();
                    //System.out.println("actionIDs[" + i + "]: " + actionIDs[i]);


                    //
                    //Action parameters - depends on animation type and ActionID.
                    //
                    actionParameters[i] = new byte[TileAnimation.paramSize_bytes(animationType, actionIDs[i])];
                    //System.out.println("actionParameters.length: " + actionParameters.length);
                    bis.read(actionParameters[i]);
                }

                //Create new TileMesh and add it to the list.
                listAnim.add
                (
                    TileAnimation.construct
                    (
                        tileSpritesInAnimation,
                        new FrameAnimation_Timer(f_frameTimes),
                        toGetPointerFrom,
                        actionIDs,
                        animationType, spriteSlot,
                        actionParameters
                    )
                );
            }

            //Convert the list of TileAnimations to an array.
            this.tileAnimations = new TileAnimation[listAnim.size()];
            listAnim.toArray(tileAnimations);

            //Close the input stream.
            bis.close();
            fis.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}
        catch(IOException e){e.printStackTrace();}
    }

    private void loadTileKey_DAT(String[] tileSet_pathNames, File tileKey_file)
    {
        Sprite[][] tileSet_Sprites = new Sprite[tileSet_pathNames.length][];

        for(int i = 0; i < tileSet_pathNames.length; i++)
        {tileSet_Sprites[i] = Sprites.loadTileSet_Sprites(tileSet_pathNames[i]);}

        loadTileKey_DAT(tileSet_Sprites, tileKey_file);
    }


    private void createOctrees()
    {
        collisionObject_Octree = new Octree<CollisionObject>(0, 0, 0, 0, (width) << TILE_BITS, (height) << TILE_BITS, (depth) << TILE_BITS);
        //spriteRenderer_Octree = 
        light_Octree = new Octree<Light>(0, 0, 0, 0, (width) << TILE_BITS, (height) << TILE_BITS, (depth) << TILE_BITS);
    }


    /**Loads materials from the given material key.
    public void loadMaterials(File file)
    {
        try
        {
            Scanner scanner = new Scanner(file);
            //
            int i = 0;
            while(scanner.hasNextLine())
            {
                //Get the next line of text.
                String line = scanner.nextLine();
                //
                if(line.equals("none")){materials[i] = Material.NULL_MATERIAL;}
                //else{materials[i] = new Material(this, line);}
                //
                i++;
            }
            //
            scanner.close();
        }
        catch(FileNotFoundException e){e.printStackTrace();}
    }
    */


    


    /**Adds a new TileMesh to the TileMeshs Array and adjusts the tiles to accomidate for it.*/
    public void addTileMesh(TileMesh tileMesh)
    {
        //New array with one extra slot.
        TileMesh[] meshs = new TileMesh[tileMeshs.length+1];

        //Get tile's SpriteSheet.
        SpriteSheet sheet = tileMesh.getSprites()[0].getSheet();

        //Determine the slot to put it in.
        int index = 1;//<- Start from 1 because 0 is VoidTile.
        boolean foundSheet = false;
        while(index < tileMeshs.length)
        {
            //Is sheet the same as the current tileSprite's sheet?
            if(!foundSheet && tileMeshs[index].getSprites()[0].getSheet() == sheet){foundSheet = true;}

            //Is the current tileSprite's spriteSheet not the same as sheet?
            else if(foundSheet && tileMeshs[index].getSprites()[0].getSheet() != sheet){break;}
            //This is the last slot associated with sheet, so that's where this is going.

            index++;
        }

        
        boolean added = false;//<- true if the input tileSprite has been added to the new array yet.

        //Start moving all TileMeshs to the new array.
        for(int i = 1; i < meshs.length; i++)//0 is null for VoidTiles anyway, so no need to check it.
        {
            if(!added)
            {
                //Is this where the input tileSprite is going?
                if(i == index)
                {
                    //Put it there.
                    meshs[i] = tileMesh;

                    //Account for every tileSprite after this.
                    added = true;
                }
                //Otherwise, this TileMesh moves to the same slot that it originally was in.
                else{meshs[i] = tileMeshs[i];}
            }
            //We're offset by one from adding input tileSprite. So subtract 1 from i.
            else{meshs[i] = tileMeshs[i-1];}
        }

        //Old length before changing pointers.
        int oldLength = tileMeshs.length;

        //TileMeshs now points to this new array.
        //tileMeshs = null;
        tileMeshs = meshs;

        //Adjust all TileMesh IDs already in the map accordingly.
        if(oldLength > 0)
        {
            for(int i = 0; i < tiles.length; i++)
            {
                //If the current ID value comes after index...
                if((tiles[i] & Tiles.TILE_SPRITE_ID_PORTION) >> Tiles.TILE_PROPERTIES_BITS >= index)
                {
                    //Increment the ID value by 1.
                    tiles[i] += (1 << Tiles.TILE_PROPERTIES_BITS);
                    //This is under the expectation that the input tileSprite has not yet been used in the map.
                }
            }
        }
    }

    /**Removes an existing TileMesh frrom the TileMeshs Array and adjusts the tiles to accomidate for it.*/
    public void removeTileMesh(int tileSpriteID)
    {
        //if(tileSpriteID <= 0 || tileSpriteID >= tileSprites.length){return;}

        //New array with one less slot.
        TileMesh[] meshs = new TileMesh[tileMeshs.length-1];

        //Start moving all TileMeshs to the new array.
        boolean passed = false;
        for(int i = 1; i < meshs.length; i++)//0 is null for VoidTiles anyway, so no need to check it.
        {
            if(!passed)
            {
                //Is this where the input tileSprite was?
                if(i == tileSpriteID)
                {
                    //Account for every tileSprite after this.
                    passed = true;
                    i--;//Redo this slot since  we've passed the input index.
                }
                //Otherwise, this TileMesh moves to the same slot that it originally was in.
                else{meshs[i] = tileMeshs[i];}
            }
            //We're offset by one from removing the input tileSprite. So add 1 to i.
            else{meshs[i] = tileMeshs[i+1];}
        }


        //Old length before changing pointers.
        int oldLength = tileMeshs.length;

        //TileMeshs now points to this new array.
        //tileMeshs = null;
        tileMeshs = meshs;

        //Adjust all TileMesh IDs already in the map accordingly.
        if(oldLength > 0)
        {
            for(int i = 0; i < tiles.length; i++)
            {
                //If the current ID value is the index...
                if((tiles[i] & Tiles.TILE_SPRITE_ID_PORTION) >> Tiles.TILE_PROPERTIES_BITS == tileSpriteID)
                {
                    //Set it to 0.
                    tiles[i] = 0;
                }
                //If the current ID value comes after index...
                else if((tiles[i] & Tiles.TILE_SPRITE_ID_PORTION) >> Tiles.TILE_PROPERTIES_BITS > tileSpriteID)
                {
                    //Decrement the ID value by 1.
                    tiles[i] -= (1 << Tiles.TILE_PROPERTIES_BITS);
                }
            }
        }
    }

    /**Adds a new TileAnimation to the TileAnimations Array.*/
    public void addTileAnimation(TileAnimation tileAnimation)
    {
        //New array to store current anims in with room for the new one.
        TileAnimation[] anims = new TileAnimation[tileAnimations.length + 1];

        //Add every current animation to the new array.
        for(int i = 0; i < tileAnimations.length; i++)
        {anims[i] = tileAnimations[i];}

        //Add the new animaion.
        anims[anims.length-1] = tileAnimation;

        //Point to the new array.
        tileAnimations = anims;

        //Reset every animation.
        for(int i = 0; i < tileAnimations.length; i++)
        {tileAnimations[i].reset();}
    }

    /**Removes an existing TileAnimation from the TileAnimations Array.*/
    public void removeTileAnimation(int tileAnimationID)
    {
        //New array to store current anims  with onne les slot.
        TileAnimation[] anims = new TileAnimation[tileAnimations.length - 1];

        //Add every current animation except the one being removed to the new array.
        boolean passed = false;
        for(int i = 0; i < tileAnimations.length; i++)
        {
            //If we haven't checked the anim we're deleting...
            if(!passed)
            {
                //Is the current animation the one we're deleting?
                if(i == tileAnimationID)
                {
                    //Give every TileMesh associated with it its own pointer.
                    tileAnimations[i].perpareForDeletion();

                    //We've checked it.
                    passed = true;
                }
                //Otherwise, add the current one.
                else{anims[i] = tileAnimations[i];}
            }
            else//Otherwise, offset by one.
            {anims[i-1] = tileAnimations[i];}
        }

        //Point to the new array.
        tileAnimations = anims;

        //Reset every leftover animation.
        for(int i = 0; i < tileAnimations.length; i++)
        {tileAnimations[i].reset();}
    }


    public void unload(GameStateManager gsm)
    {
        //Just to not run this function by accident.
        if(gsm == null || tileMeshs.length <= 1){return;}

        //Get first sheet.
        SpriteSheet currentSheet = tileMeshs[1].getSheet();

        for(int i = 1; i < tileMeshs.length; i++)
        {
            //Cache TileMesh's SpriteSheet.
            SpriteSheet ss = tileMeshs[i].getSheet();

            //Was the previous tile the last one that had the currently stored sheet?
            if(ss != currentSheet)
            {
                //Delete the current sheet.
                currentSheet.delete();

                //Set ss to be the current one.
                currentSheet = ss;
            }
        }

        currentSheet.delete();
    }



    //Dimension getters
    public int getWidth(){return width;}
    public int getHeight(){return height;}
    public int getDepth(){return depth;}

    //Time Modifier, for slowing down time and whatnot.
    //private float timeMod = 1f;
    private @fixed int f_timeMod = fixed(1);

    //Used to prevent reading from and modifying the entites list simultaniously.
    private Object newEntityLock = new Object();

    float dd = 0.0f;

    /**Updates everything in this Level.*/
    public void update()
    {
        //Update Camera.
        camera.update();
        
        //Update Entities to update velocities.
        for(int i = 0; i < entities.size(); i++)
        {
            //Cache the Entity (this is also where I would cache the V-Table if I could)
            Entity e = entities.get(i);

            //If the entity needs to be removed.
            if(e.shouldRemove())
            {
                //Add this slot to the shouldRemoves list.
                shouldRemoves.add(i);
                entitiesToRemove++;
               
                //Go to the next entity.
                continue;
            }

            //Update the Entity, which initializes velocities needed for collision detection and movement.
            e.update(f_timeMod);
        }

        //Synchronized to prevent copying the list in the render thread until this part is done.
        synchronized(newEntityLock)
        {
            //Remove all of the entities that should be removed from the list. (decrement so the entities to be removed won't get shifted)
            for(int r = entitiesToRemove-1; r >= 0; r--)
            {entities.remove( shouldRemoves.get(r).intValue() );}
            entitiesToRemove = 0;

            //Add any new entities to the list.
            entities.addAll(newEntities);
        }
        //System.out.println(entities.size());

        //
        //TODO Update Octrees.
        //

        //Refill CollisionComponent Octree with CollisionComponents.
        collisionObject_Octree.clear();
        for(int i = 0; i < collisionComponents.size(); i++)
        {collisionObject_Octree.insert(collisionComponents.get(i));}

        //Have Physics Components perform Tile and Entity collision checks with nearby CollisionComponents.
        for(int p = 0; p < physicsComponents.size(); p++)
        {physicsComponents.get(p).performCollisionCheck(this.collisionObject_Octree);}

        //Apply velocities of all PhysicsComponents.
        for(int p = 0; p < physicsComponents.size(); p++)
        {physicsComponents.get(p).applyVelocity();}

        //Speen test.
        globalLight_Direction.x = (float)Math.sin(dd);
        globalLight_Direction.y = (float)Math.cos(dd);
        dd -= 0.01f;

        //Refill Light Octree with Lights and let the camera add lights after.
        synchronized(camera.lightLock)//<- This code will not be ran if the Octree is being retrived from in the render thread.
        {
            //Clear octree.
            light_Octree.clear();

            //Refill it.
            for(int i = 0; i < lights.size(); i++)
            {light_Octree.insert(lights.get(i));}
        }
        

        //Refill SpriteRenderer Octree with SpriteRenderers.
        //spriteRenderer_Octree.clear();
        //for(int i = 0; i < spriteRenderers.size(); i++)
        //{spriteRenderer_Octree.insert(spriteRenderers.get(i));}

        //Clear new Entities list (done here so the render thread dosn't have to wait).
        newEntities.clear();

        //Sort Entities for rendering.
        //try{entities.sort(entitySorter);}
        //catch(ConcurrentModificationException e){e.printStackTrace();}

        //Update Tile Animations.
        for(int i = 0; i < tileAnimations.length; i++)
        {tileAnimations[i].update(f_timeMod);}
    }

    public void updateTileAnimations()
    {
        for(int i = 0; i < tileAnimations.length; i++)
        {tileAnimations[i].update(f_timeMod);}
    }

    //Unlocks the lock that the Render Thread waits on.
    //public void unlockRenderThread()
    //{
        //synchronized(listLock)
        //{listLock.notify();}
        //synchronized(camera.lightLock){camera.lightLock.notify();}
    //}

    //Camera Getter.
    public LevelCamera getCamera(){return this.camera;}

    /**Returns the Octree this Level uses to optimize Entity collisions.*/
    public Octree<CollisionObject> getCollisionObject_Octree(){return collisionObject_Octree;}

    /**Returns the Octree this Level uses to optimize Lighting Calculations.*/
    public Octree<Light> getLight_Octree(){return light_Octree;}

    //Entity to center the camera on.
    public void centerCameraTo(Entity centerEntity, fixedVector3 f_velocity)
    {camera.setEntityPosition(centerEntity, f_velocity);}


    //
    //Component Adders/Removers.
    //

    /**Adds an Entity to the Level.*/
    public void add(Entity e)
    {
        newEntities.add(e);
        e.init(this);
    }

    /**Returns this Level's list of Entities.*/
    public ArrayList<Entity> getEntities(){return entities;}

    public void addPhysicsComponent(PhysicsComponent p)
    {
        physicsComponents.add(p);
    }

    public void removePhysicsComponent(PhysicsComponent p)
    {
        //Remove p's collisionComponent.
        collisionComponents.remove(p.getCollisionObject());

        //Remove p itself.
        physicsComponents.remove(p);
    }

    public void addCollisionComponent(CollisionObject c)
    {
        collisionComponents.add(c);
    }

    public void removeCollisionComponent(CollisionObject c)
    {
        collisionComponents.remove(c);
    }

    public void addSpriteRenderer(SpriteRenderer s)
    {
        spriteRenderers.add(s);
    }

    public void removeSpriteRenderer(SpriteRenderer s)
    {
        spriteRenderers.remove(s);
    }

    public void addLight(Light l)
    {
        lights.add(l);
    }

    public void removeLight(Light l)
    {
        lights.remove(l);
    }

    

    /**
     * Tile Proxy class used for Translucent Tiles.
     */
    private class TileProxy extends Entity
    {
        //TileMesh.
        private TileMesh t;

        /**Constructor. */
        //public TileProxy(float x, float y, float z, TileMesh t)
        public TileProxy(@fixed int x, @fixed int y, @fixed int z, TileMesh t)
        {
            super(x, y, z);
            this.t = t;
        }

        @Override
        //Does nothing. Doesn't need level.
        public void init(Level level){}

        //Override
        //public void update(float timeMod){}
        public void update(@fixed int timeMod){}

        @Override
        public void render(Screen screen, float scale)
        {
            //Render Tile wall.
            //t.renderWall_Depth(screen, (int)position.x, (int)position.y, (int)position.z, scale);
            //t.renderWall_Depth(screen, f_position.x, f_position.y, f_position.z, scale);

            //Render Tile floor.
            //t.renderFloor_Depth(screen, (int)position.x, (int)position.y, (int)position.z, scale);
            //t.renderFloor_Depth(screen, f_position.x, f_position.y, f_position.z, scale);

            t.render_Ent(screen, f_position.x, f_position.y, f_position.z, scale);
        }
    }

    //Zoom factor for the Level.
    public void setScale(float scale, int screenWidth, int screenHeight){camera.setScale(scale, screenWidth, screenHeight);}
    public void addScale(float scale, int screenWidth, int screenHeight){camera.addScale(scale, screenWidth, screenHeight);}
    public float getScale(){return camera.getScale();}

    //Determines rather or not the game renders the Collision Type of each Tile.
    private boolean showCollision = false;
    public final void toggleShowCollision(){showCollision = !showCollision;}
    public final boolean isShowingCollision(){return showCollision;}

    public Vector3f getGlobalLight_Direction(){return globalLight_Direction;}


    /*
     * 
     * Shadow_Region object:
     * SAT_Shape shape//Shape of Shadow Region
     * Sprite[] siloueteSprites//For the shadow's silouete
     * 
     * render(Screen screen, float scale)
     * {
     *      shape.renderShadow(screen, scale)
     * }
     * 
     * 
     * 
     * When a level is loading or when calculateShadows(int x, int y, int z, int w, int h, int d) is called:
     * 
     * Tile t
     * if t is opaque
     * for each face of t:
     * for each zCorner of face (if zNormal < 0, start at top):
     * for each yCorner of face (if yNormal < 0, start at front):
     * for each xCorner of face (if xNormal < 0, start at right):
     * 
     * cx = (x << TILE_BITS + cornerX) + (tileCount * globalLight_ShadowInc.x)
     * cy = (y << TILE_BITS + cornerY) + (tileCount * globalLight_ShadowInc.y)
     * cz = (z << TILE_BITS + cornerZ) + (tileCount * globalLight_ShadowInc.z)
     * 
     * Tile t0 = getTile(cx >> TILE_BITS, cy >> TILE_BITS, cz >> TILE_BITS)
     * 
     * if !first corner && t0.shape.intersects(cx, cy, cz):
     *  quad: previousCorner, thisCorner, (thisCorner)
     *      
     * 
     * previousPoint = cx, cy, cz
     * 
     *      
     */
    
    Vector4f testColor = new Vector4f(0.0f, 0.6f, 1.0f, 1.0f);
    
    /**
     * Renders this Level.
     * 
     * @param screen
     */
    public void render(Screen screen)
    {        
        //Get Camera Position.
        int cameraX = (int)camera.getX(),
        //yScroll = camera.getVisualY(),
        cameraY = (int)camera.getY(),
        zScroll = (int)camera.getZ() >> TILE_BITS;

        //Set Screen offset relative to camera position.
        screen.setCameraOffsets(camera.getScaledX(), camera.getScaledY(), camera.getScaledZ());//, camera.getScale());

        calculateShadows();


        //Send global light state to screen.
        screen.setGlobalLight(globalLight_Direction, globalLight_Diffuse, globalLight_Ambient);

        //Get Camera Zoom.
        float scale = camera.getScale();

        //Calculate what needs to be rendered horizontally.
        int x0 = (cameraX >> Level.TILE_BITS);
        int x1 = ((int)((cameraX + (screen.getWidth() / scale)) + Level.TILE_SIZE) >> Level.TILE_BITS);
        if(x0 < 0){x0 = 0;} else if(x0 >= width){x0 = width;}
        if(x1 < 0){x1 = 0;} else if(x1 >= width){x1 = width;}

        //Calculate what can be rendered on the z-axis.
        int z0 = (int)(zScroll - (16 / scale));
        int z1 = (int)(zScroll + (16 / scale));
        if(z0 < 0){z0 = 0;} else if(z0 >= depth){z0 = depth-1;}
        if(z1 < 0){z1 = 0;} else if(z1 >= depth){z1 = depth-1;}

        //System.out.println("Scale " + scale);

        //Check what lights are within view of the camera (will wait for LightLock).
        camera.lightCheck(screen, light_Octree);

        //Lock and wait for any new Entities to be added and for the octrees to be refilled.
        //synchronized(listLock)
        //{
            //try{listLock.wait();}
            //catch(InterruptedException e)
            //{e.printStackTrace();}
        //}

        //Put all Entities into a seperate list for Translucent Tile purposes (not while new entities are being added though).
        List<Entity> currentList;
        synchronized(newEntityLock){currentList = new ArrayList<Entity>(entities);}
        //int addProxy_Index = currentList.size();

        //TODO Check what shadows are within view of the camera.
        //camera.shadowCheck(screen, shadow_Octree);
        for(int i = 0; i < shadowVolumes.size(); i++)
        {
            //Cache Shadow Volume.
            ShadowVolume s = shadowVolumes.get(i);
           
            //Send Shadow Volume to screen.
            screen.applyShadow(s, scale, true);
        }

        //Render Floors: Top to bottom, Front to Back, don't overlap pixels.
        //System.out.println(z0 + " " + z1);

        //Render to Floors and Walls of all the Tiles the camera can see: Top to bottom, Front to Back, don't overlap pixels.
        for(int z = z1; z >= z0; z--)
        //for(int z = z0; z <= z1; z++)
        {
            int zPos = (int)(((z+1) << Level.TILE_BITS) * scale);
            // + 0.5f);
            //d = (((int)((((z+2) << Level.TILE_BITS) * scale) + 0.5f) - zPos) / 2);// + 1;

            //Calculate what needs to be rendered vertically, offset by z.
            int y0 = ((int)(cameraY + (((z-1 - zScroll) << TILE_BITS) / 2)) >> TILE_BITS),
            y1 = ((int)((cameraY + (screen.getHeight() / scale) + TILE_SIZE) + (((z-1 - zScroll) << TILE_BITS) / 2)) >> TILE_BITS);
            //
            if(y0 < 0){y0 = 0;} else if(y0 >= height){y0 = height-1;}
            if(y1 < 0){y1 = 0;} else if(y1 >= height){y1 = height-1;}

            //System.out.println(y0 + " " + y1);

            for(int y = y1; y >= y0; y--)
            {
                int yPos = (int)((y << Level.TILE_BITS) * scale);
                // + 0.5f);
                //h = (int)((((y+1) << Level.TILE_BITS) * scale) + 0.5f) - yPos;
                //
                if(!showCollision)
                {
                    //System.out.println(x0 + " " + x1);

                    for(int x = x0; x < x1; x++)
                    {
                        //Get TileMeshID.
                        int tileSpriteID = getTileMeshID_Unsafe(x, y, z),
                        tileType = getTileType_Unsafe(x, y, z);
                        //System.out.println(tileSpriteID + " " + tileType);

                        //Skip if it's zero or if it's a void tile.
                        if(tileSpriteID == 0 || tileType == 0){continue;}

                        //Calculate X Position.
                        int xPos = (int)(((x << Level.TILE_BITS) * scale));
                        // + 0.5f);
                        //w = (int)((((x+1) << Level.TILE_BITS) * scale) + 0.5f) - xPos;

                        //Get TileMesh.
                        TileMesh t = getTileMeshs(tileSpriteID);
                        //System.out.println("tileSpriteID: " + tileSpriteID);

                        //If Translucent, add to currentList as a TileProxy.
                        if(t.isTranslucent())
                        {
                            currentList.add(new TileProxy(xPos, yPos, zPos, t));
                            //currentList.add(addProxy_Index, new TileProxy(xPos, yPos, zPos, t));
                        }
                        else
                        {
                            //Otherwise, just render it.
                            //
                            //t.renderFloor(screen, xPos, yPos, zPos, scale);//, w, h);
                            //t.renderWall(screen, xPos, yPos, zPos, scale);//, w, d);
                            t.render(screen, xPos, yPos, zPos, scale);
                        }
                    }
                }
                else
                {
                    for(int x = x0; x < x1; x++)
                    {
                        //Get the Tile and its info.
                        int[] tileInfo = new int[1];
                        Tile t = getTile(x, y, z, tileInfo);
                        System.out.println(t);

                        //Skip if it's a void tile.
                        if((tileInfo[0] & Tiles.TILE_PROPERTIES_PORTION) == 0){continue;}

                        //Calculate X Position.
                        int xPos = (int)((x << Level.TILE_BITS) * scale);

                        //Render.
                        //t.render(screen, xPos, yPos, zPos, scale);
                        t.renderShape(screen, xPos, yPos, zPos, scale, true);
                    }
                }
            }
        }
        
        /*
        //Render Walls: Top to bottom, Front to Back, don't overlap pixels.
        for(int z = depth-1; z >= 0; z--)
        {
            int y0 = (int)(yScroll + ((z - zScroll) / 2)) >> TILE_BITS,
            y1 = (int)((yScroll + ((z - zScroll) / 2)) + ((screen.getHeight() + (TILE_SIZE * 1.5f)) / scale)) >> TILE_BITS;
            if(y0 < 0){y0 = 0;} else if(y0 >= height){y0 = height;}
            if(y1 < 0){y1 = 0;} else if(y1 >= height){y1 = height;}

            for(int y = y1-1; y >= y0; y--)
            {
                for(int x = x0; x < x1; x++)
                {
                    //Skip if it's a void tile.
                    if(getTileInfo(x, y, z) == 0x00000000){continue;}

                    getTileMesh(x, y, z).renderWall(screen, x, y, z, scale);
                }
            }
        }
        */

        //for(int i = 0; i < entities.size(); i++)
        //{
            //Render the Entity.
            //Entity e = entities.get(i);
            //e.render(screen, scale);
        //}

        //Sort Entities by Z-Position (Try-Catch for Multi-Threading reasons).
        /*
        try{currentList.sort(entitySorter);}
        catch(NullPointerException exception)
        {
            exception.printStackTrace();

            for(int i = 0; i < currentList.size(); i++)
            {
                //Render it.
                Entity e = currentList.get(i);
                if(e == null){return;}
                e.render(screen, scale);
            }
        }
        */

        //TODO Determine what entities are on-screen.
        currentList.sort(entitySorter);

        //For every Entity in the list...
        for(int i = 0; i < currentList.size(); i++)
        {
            //Render it.
            Entity e = currentList.get(i);
            e.render(screen, scale);
        }


        /*
        //Determine reflection regions in the camera's view.
        List<ReflectionRegion> reflectionRegions = reflectionRegion_Octree.retrieve(camera);

        for(int r = 0; r < reflectionRegions.size(); r++)
        {
            //Cache current region.
            ReflectionRegion currentRegion = reflectionRegions.get(r);

            //
            //TODO Tile reflection loop.
            //

            //Determine what spriteRenderers are in the region.
            List<SpriteRenderer> spriteRenderers = spriteRenderer_Octree.retrieve(camera);

            //
            //
            //SpriteRenderer reflection loop.
            for(int s = 0; s < spriteRenderers.size(); s++)
            {spriteRenderers.get(s).renderReflect(currentRegion);}
        }
        */


        //TileMesh t1 = tileSprites[1];
        //screen.renderSprite_Sc(0, 0, 0, 0, t1.getSprites()[0], Sprite.FLIP_NONE, tileSprites[1].getNormalMaps()[0], 0.0f,
        //testColor, 10.0f * scale, 10.0f * scale, true);

        //System.out.println(camera.getWidth() + " " + camera.getHeight() + " " + camera.getDepth());

        //camera.render(screen, scale);
    }


    /**Creates the shadow volumes within the given region.*/
    public void calculateShadows(int tX, int tY, int tZ, int tW, int tH, int tD)
    {
        shadowVolumes.clear();
        
        //Negate the Light's direction for this purpose.
        Vector3f negative_LightDir = new Vector3f(-globalLight_Direction.x, -globalLight_Direction.y, -globalLight_Direction.z);

        //For each tile in the given area.
        for(int z = (tZ + tD) - 1; z >= tZ; z--)
        {
            for(int y = tY; y < tY + tH; y++)
            {
                for(int x = tX; x < tX + tW; x++)
                {
                    //Get tile type.
                    int type = getTileType_Unsafe(x, y, z);
                    if(type <= 0){continue;}

                    //Get the tile's faces.
                    Tile tile = Tiles.get(type);
                    Shape_Box.Shape_Face[] tileFaces = tile.getFaces();
                    if(tileFaces == null){continue;}// || x > 0 || y < height-1 || z < depth-1)

                    //Get the tile's sprite.
                    TileMesh tileSprite = getTileMesh_Unsafe(x, y, z);
                    if(tileSprite.isTranslucent()){continue;}

                    int rawX = x << TILE_BITS,
                    rawY = y << TILE_BITS,
                    rawZ = z << TILE_BITS;

                    //Boolean array to store which faces are facing the light.
                    boolean[] visible = new boolean[tileFaces.length];

                    //First loop: Check which faces are facing the light.
                    for(int f0 = 0; f0 < tileFaces.length; f0++)
                    {
                        //If the dot product of the face's direction and the light's
                        //direction is greater than zero, it is facing the light.
                        float dot = tileFaces[f0].getDirection().dot(negative_LightDir);
                        if(dot >= 0){visible[f0] = true;}
                    }
                    //Second loop: Use visiblity info to create Shadow Volume.
                    List<ShadowFace> shadowFaceList = new ArrayList<ShadowFace>();
                    //byte numFrontFaces = 0;
                    for(int f1 = 0; f1 < tileFaces.length; f1++)
                    {
                        if(visible[f1])
                        {
                            //Cache face.
                            Shape_Box.Shape_Face face = tileFaces[f1];
                            //Vector3f faceDirection = face.getDirection();
                            Vector3f[] points = face.getPoints();
                            byte[] neighboringIndicies = face.getNeighboringIndicies();

                            //Iterate through each edge.
                            for(int e = 0; e < points.length; e++)
                            {
                                //Get neighboring index.
                                byte neigh = neighboringIndicies[e];

                                //If this edge's neighboring face is not visible or
                                //non-exsistent, use this edge for a shadow volume.
                                if(neigh == -1 || !visible[neigh])
                                {
                                    //Get edge points.
                                    Vector3f point0 = points[e],
                                    point1 = points[(e+1) % points.length];

                                    //Result points.
                                    Vector3f
                                    p0 = new Vector3f
                                    (
                                        rawX + point0.x,
                                        rawY + point0.y,
                                        rawZ + point0.z
                                    ),
                                    p1 = new Vector3f
                                    (
                                        rawX + point1.x,
                                        rawY + point1.y,
                                        rawZ + point1.z
                                    );

                                    //TODO Incorperate Shadow Containers.

                                    Vector3f
                                    p2 = new Vector3f
                                    (
                                        p1.x + (400 * globalLight_Direction.x),
                                        p1.y + (400 * globalLight_Direction.y),
                                        p1.z + (400 * globalLight_Direction.z)
                                    ),
                                    p3 = new Vector3f
                                    (
                                        p0.x + (400 * globalLight_Direction.x),
                                        p0.y + (400 * globalLight_Direction.y),
                                        p0.z + (400 * globalLight_Direction.z)
                                    );

                                    //Create new ShadowFace.
                                    ShadowFace newFace = new ShadowFace(p1, p0, p3, p2);

                                    //Get neighboring face.
                                    //Shape_Box.Shape_Face neighboringFace = (neigh > -1) ? tileFaces[neigh] : null;

                                    //if((faceDirection.y >= 0.0f || faceDirection.z > 0.0f)
                                    //&& (neigh < 0 || (neighboringFace.getDirection().y >= 0.0f && neighboringFace.getDirection().z >= 0.0f)))
                                    //if(faceDirection.y == 1.0f && faceDirection.z == 0.0f)
                                    //{
                                        //shadowFaceList.add(0, newFace);
                                        //numFrontFaces++;
                                    //}
                                    //else
                                    {shadowFaceList.add(newFace);}
                                }
                            }
                        }
                    }
                    //Combine all created faces into one new Shadow Volume.
                    ShadowVolume newVolume = new ShadowVolume(shadowFaceList);
                    shadowVolumes.add(newVolume);
                }
            }
        }
    }

    public void calculateShadows(){calculateShadows(0, 0, 0, width, height, depth);}


    //The Level.TILE_TYPE portion of an int represents the tile's properties.
    public Tile getTile(int x, int y, int z)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
        {return Tiles.getTileType(0);}

        //int t = tiles[(x + y * width) + (z * (width * height))];
        //return Tiles.get(t);
        return Tiles.get( tiles[(x + y * width) + (z * (width * height))] );
    }

    public Tile getTile_Unsafe(int x, int y, int z)
    {
        //int t = tiles[(x + y * width) + (z * (width * height))];
        //return Tiles.get(t);
        return Tiles.get( tiles[(x + y * width) + (z * (width * height))] );
    }


    /**Gets the TileMesh object used at the given coordinates.*/
    public TileMesh getTileMesh(int x, int y, int z)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
        {return tileMeshs[0];}

        int t = tiles[(x + y * width) + (z * (width * height))];
        return tileMeshs[(t & Tiles.TILE_SPRITE_ID_PORTION) >> Tiles.TILE_PROPERTIES_BITS];
    }

    public TileMesh getTileMesh_Unsafe(int x, int y, int z)
    {
        int t = tiles[(x + y * width) + (z * (width * height))];
        return tileMeshs[(t & Tiles.TILE_SPRITE_ID_PORTION) >> Tiles.TILE_PROPERTIES_BITS];
    }


    /**Gets the TileMesh ID used at the given coordinates.*/
    public int getTileMeshID(int x, int y, int z)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
        {return 0;}

        int t = tiles[(x + (y * width)) + (z * (width * height))];
        return (t & Tiles.TILE_SPRITE_ID_PORTION) >> Tiles.TILE_PROPERTIES_BITS;
    }

    public int getTileMeshID_Unsafe(int x, int y, int z)
    {
        int t = tiles[(x + (y * width)) + (z * (width * height))];
        return (t & Tiles.TILE_SPRITE_ID_PORTION) >> Tiles.TILE_PROPERTIES_BITS;
    }


    /**Returns the TileMesh associated with the given ID.*/
    public TileMesh getTileMeshs(int slot){return tileMeshs[slot];}
    public int getNumTileMeshs(){return tileMeshs.length;}

    /**Returns the TileAnimation associated with the given slot.*/
    public TileAnimation getTileAnimation(int slot){return tileAnimations[slot];}
    public int getNumTileAnimations(){return tileAnimations.length;}


    /**Returns the Tile Type at the given x, y, z coordinate.*/
    public int getTileType(int x, int y, int z)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
        {return 0;}

        return tiles[(x + y * width) + (z * (width * height))]
        & Tiles.TILE_PROPERTIES_PORTION;
    }

    public int getTileType_Unsafe(int x, int y, int z)
    {
        return tiles[(x + y * width) + (z * (width * height))]
        & Tiles.TILE_PROPERTIES_PORTION;
    }


    /**Returns the Tile ID and Type at the given x, y, z coordinate.*/
    public int getTileInfo(int x, int y, int z)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
        {return 0;}

        return tiles[(x + y * width) + (z * (width * height))];
    }

    public int getTileInfo_Unsafe(int x, int y, int z)
    {return tiles[(x + y * width) + (z * (width * height))];}


    /**
     * Returns the Tile ID and Type at the given x, y, z coordinate.
     * 
     * @param x the x tilePosition on the map.
     * @param y the y tilePosition on the map.
     * @param z the z tilePosition on the map.
     * @param infoContainer the pointer to an int[] to write the tileInfo to.
     * @return the tileInfo at the given coordinates.
     */
    public Tile getTile(int x, int y, int z, int[] infoContainer)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
        {return Tiles.getTileType(0);}

        infoContainer[0] = tiles[(x + y * width) + (z * (width * height))];
        return Tiles.get(infoContainer[0]);
    }

    /**Sets the Tile at the given x, y, z coordinate.*/
    public void setTile(int x, int y, int z, int t)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth){return;}
        //
        tiles[(x + y * width) + (z * (width * height))] = t;
    }

    /**Sets the Tile Type at the given x, y, z coordinate.*/
    public void setTileType(int x, int y, int z, int t)
    {
        int index = (x + y * width) + (z * (width * height));

        //SpriteID portion | Type Portion.
        tiles[index] = (tiles[index] & Tiles.TILE_SPRITE_ID_PORTION) | t;
    }

    /**Sets the Tile Sprite at the given x, y, z coordinate.*/
    public void setTileMesh(int x, int y, int z, int s)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth){return;}
        //
        int index = (x + y * width) + (z * (width * height));
        //
        //Type portion | SpriteID Portion.
        tiles[index] = (s << Tiles.TILE_PROPERTIES_BITS) | (tiles[index] & Tiles.TILE_PROPERTIES_PORTION);
        //TODO NOTE: If the collision type is set to zero, the TileMesh won't render.
    }

    /**Gets the material at the given tile.*/
    public Material getMaterial(int x, int y, int z)
    {
        TileMesh t = getTileMesh(x, y, z);
        return t.getMaterial();
    }
}
