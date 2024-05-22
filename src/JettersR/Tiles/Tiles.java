package JettersR.Tiles;
/**
 * List of Tile instances.
 * 
 * Author: Luke Sullivan
 * Last Edit: 5/28/2023
 */
//import org.joml.Vector3f;

import JettersR.Level;
import JettersR.Entities.Entity;
import JettersR.Entities.Components.PhysicsComponent;
import JettersR.Graphics.SpriteSheet;
import JettersR.Graphics.Sprites;
import JettersR.Util.Shapes.Shapes3D.Isosceles_Triangle;
import JettersR.Util.Shapes.Shapes3D.Shape3D;
import JettersR.Util.Shapes.Shapes3D.Shape_Box;
import JettersR.Util.Shapes.Shapes3D.Slope_Triangle;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Tiles
{
    /**Just so this can't be instantiated.*/
    private Tiles(){}

    //Debug SpriteSheet.
    public static final SpriteSheet collisionTiles = Sprites.global_Debug("CollisionTiles");

    //TileSet filePath.
    public static final String TILESETS_PATH_0 = "TileSets/",
    TILESETS_PATH = "assets/Images/" + TILESETS_PATH_0;

    //Array of Basic Tile Shapes.
    private static final Tile[] tileTypes =
    {
        //NullTile.
        new Tile(null),//, null),

        //Box.
        Tile.BoxTile(),
        
        //Half Box.
        Tile.BoxTile(Level.TILE_SIZE>>1, 0),
        Tile.BoxTile(Level.TILE_SIZE>>1, Level.TILE_SIZE>>1),


        //
        //Slope_Triangles.
        //

        //1xy by 1z Slopes.
        Tile.SlopeTile(Slope_Triangle.Type.LEFT),
        Tile.SlopeTile(Slope_Triangle.Type.RIGHT),
        Tile.SlopeTile(Slope_Triangle.Type.UP),
        Tile.SlopeTile(Slope_Triangle.Type.DOWN),

        //2xy = 1z
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE>>1,   Slope_Triangle.Type.LEFT,   1, 2, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.LEFT,   1, 2, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.RIGHT,  1, 2, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE>>1,   Slope_Triangle.Type.RIGHT,  1, 2, 0, 0, 0),
        //
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE>>1,   Slope_Triangle.Type.UP,     1, 2, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.UP,     1, 2, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.DOWN,   1, 2, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE>>1,   Slope_Triangle.Type.DOWN,   1, 2, 0, 0, 0),

        //2z = 1xy
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.LEFT,   2, 1, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE/2, Level.TILE_SIZE, Level.TILE_SIZE,    Slope_Triangle.Type.LEFT,   2, 1, Level.TILE_SIZE/2, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE/2, Level.TILE_SIZE, Level.TILE_SIZE,    Slope_Triangle.Type.RIGHT,  2, 1, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.RIGHT,  2, 1, 0, 0, 0),
        //
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.UP,     2, 1, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE>>1, Level.TILE_SIZE,   Slope_Triangle.Type.UP,     2, 1, 0, Level.TILE_SIZE/2, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE>>1, Level.TILE_SIZE,   Slope_Triangle.Type.DOWN,   2, 1, 0, 0, 0),
        Tile.SlopeTile( Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE,      Slope_Triangle.Type.DOWN,   2, 1, 0, 0, 0),
        

        //
        //Isosceles_Triangles.
        //

        //1x by 1y Slants.
        Tile.IsoscelesTile(Isosceles_Triangle.TYPE_UL),
        Tile.IsoscelesTile(Isosceles_Triangle.TYPE_UR),
        Tile.IsoscelesTile(Isosceles_Triangle.TYPE_DL),
        Tile.IsoscelesTile(Isosceles_Triangle.TYPE_DR),

        //2x = 1y
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE>>1,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UL, 1, 2, 0, Level.TILE_SIZE>>1, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE,       Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UL, 1, 2, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE,       Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UR, 1, 2, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE>>1,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UR, 1, 2, 0, Level.TILE_SIZE>>1, 0),
        //
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE>>1,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DL, 1, 2, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE,       Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DL, 1, 2, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE,       Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DR, 1, 2, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE, Level.TILE_SIZE>>1,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DR, 1, 2, 0, 0, 0),
        
        //2y = 1x UP
        Tile.IsoscelesTile( Level.TILE_SIZE,    Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UL, 2, 1, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE>>1, Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UL, 2, 1, Level.TILE_SIZE>>1, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE>>1, Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UR, 2, 1, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE,    Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_UR, 2, 1, 0, 0, 0),
        //
        Tile.IsoscelesTile( Level.TILE_SIZE,    Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DL, 2, 1, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE>>1, Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DL, 2, 1, Level.TILE_SIZE>>1, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE>>1, Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DR, 2, 1, 0, 0, 0),
        Tile.IsoscelesTile( Level.TILE_SIZE,    Level.TILE_SIZE,    Level.TILE_SIZE,    Isosceles_Triangle.TYPE_DR, 2, 1, 0, 0, 0),
        //44
    };

    //Getter for TileTypes.
    public static Tile getTileType(int slot){return tileTypes[slot];}
    public static int getTileTypes_Length(){return tileTypes.length;}
    
    @FunctionalInterface
    public interface TileForce
    {
        //public abstract void invoke(Shape3D shape, Entity entity, Vector3f f_position, Vector3f f_velocity,
        //Shape_Box thisShape, Vector3f f_thisPosition);

        public abstract void invoke(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, 
        Shape_Box thisShape, fixedVector3 f_thisPosition);
    }

    /**Tile Force Combinations.*/
    public static final TileForce[] tileForces =
    {
        //000000 None
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {return;},

        //000001 Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {thisShape.putLeft(shape, null, f_position, f_velocity, f_thisPosition);},

        //000010 Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {thisShape.putRight(shape, null, f_position, f_velocity, f_thisPosition);},

        //000011 BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {tileForces_BothX(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);},
        //
        //
        //
        //
        //
        //000100 Up
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {thisShape.putBack(shape, null, f_position, f_velocity, f_thisPosition);},

        //000101 UpLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Back.
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left.
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Left
            else{shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //000110 UpRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Back.
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Right.
                else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
                
            }

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else{shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //000111 Up and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Back
            else{shape.putThis_Back_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //001000 Down
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {thisShape.putFront(shape, null, f_position, f_velocity, f_thisPosition);},

        //001001 DownLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Front.
                if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left.
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Left
            else{shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //001010 DownRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Front.
                if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Right.
                else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else{shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //001011 Down and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Front
            else{shape.putThis_Front_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //001100 BothY
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {tileForces_BothY(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);},

        //001101 BothY and Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left
            else{shape.putThis_Left_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //001110 BothY and Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Right
            else{shape.putThis_Right_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //001111 BothY and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left/Right
            else{tileForces_BothY(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //010000 Bottom
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //For isoceles triangles, act as a connecting slant force.
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;
                iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);
                return;
            }

            thisShape.putBottom(shape, null, f_position, f_velocity, f_thisPosition);
        },

        //010001 Bottom & Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left/Slant
            else{thisShape.putLeft(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //010010 Bottom & Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Right/Slant
            else{thisShape.putRight(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //010011 Bottom & BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {thisShape.putLeft(shape, null, f_position, f_velocity, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {thisShape.putRight(shape, null, f_position, f_velocity, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //010100 Bottom & Up
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Back/Slant
            else{thisShape.putBack(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //010101 Bottom & UpLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
            f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                //Bottom
                if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x)
            || thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //010110 Bottom & UpRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
            f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                //Bottom
                if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Right
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Right
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x)
            || thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //010111 Bottom & Up and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //011000 Bottom & Down
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Front/Slant
            else{thisShape.putFront(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //011001 Bottom & DownLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
            f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                //Bottom
                if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            //Front
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x)
            || thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //011010 Bottom & DownRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
            f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                //Bottom
                if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            //Front
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Right
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x)
            || thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //011011 Bottom & Down and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Front
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
            || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //011100 Bottom & BothY
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

            //Bottom
            if(thisShape.collide_Bottom(shape, f_position.z, f_velocity.z, f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Back/Front
            else{tileForces_BothY(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);}
        },

        //011101 Bottom & BothY and Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x)
            || thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Back
            else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //011110 Bottom & BothY and Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Right
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x)
            || thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Back
            else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //011111 Bottom & BothY and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Left
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else{shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //100000 Top
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //For isoceles triangles, act as a connecting slant force if a top collision wasn't made.
            if(thisShape instanceof Isosceles_Triangle
            && !thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z - f_oldPosition.z), f_thisPosition.z))
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;
                iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);
                return;
            }
            
            tileForces_Top(shape, f_position, f_velocity, thisShape, f_thisPosition);
        },

        //100001 Top & Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {iso.putTop(shape, null, f_position, f_velocity, f_thisPosition);}

                //Slant.
                else{iso.putLeft(shape, null, f_position, f_velocity, f_thisPosition);}
                return;
            }

            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {thisShape.putLeft(shape, null, f_position, f_velocity, f_thisPosition);}

            //Top/Slope
            else{tileForces_Top(shape, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //100010 Top & Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {iso.putTop(shape, null, f_position, f_velocity, f_thisPosition);}

                //Slant.
                else{iso.putRight(shape, null, f_position, f_velocity, f_thisPosition);}
                return;
            }

            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Right
            if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {thisShape.putRight(shape, null, f_position, f_velocity, f_thisPosition);}

            //Top/Slope
            else{tileForces_Top(shape, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //100011 Top & BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

                //Top
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {iso.putTop(shape, null, f_position, f_velocity, f_thisPosition);}

                //Left/Right/Slant
                else{iso.putHorizontal(shape, null, f_position, f_velocity, f_oldPosition, f_thisPosition);}

                //We're done here.
                return;
            }

            //Top/Slope Left/Right/Slant
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {tileForces_BothX(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //100100 Top & Up
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {iso.putTop(shape, null, f_position, f_velocity, f_thisPosition);}

                //Slant.
                else{iso.putBack(shape, null, f_position, f_velocity, f_thisPosition);}
                return;
            }

            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            //Left
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {thisShape.putBack(shape, null, f_position, f_velocity, f_thisPosition);}

            //Top/Slope
            else{tileForces_Top(shape, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //100101 Top & UpLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back.
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left.
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Back
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else{shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //100110 Top & UpRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back.
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left.
                else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Back
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else{shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //100111 Top & Up and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, iso, f_thisPosition);}

                //Back.
                else if(iso.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_Contact(null, f_position, f_velocity, iso, f_thisPosition);}

                //Left.
                else if(iso.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_ul(null, f_position, f_velocity, iso, f_thisPosition);}
                //Right.
                else if(iso.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_ur(null, f_position, f_velocity, iso, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Left
                if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Right
                else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else{shape.putThis_Back_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },
        //
        //
        //
        //
        //
        //101000 Top & Down
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {iso.putTop(shape, null, f_position, f_velocity, f_thisPosition);}

                //Slant.
                else{iso.putFront(shape, null, f_position, f_velocity, f_thisPosition);}
                return;
            }

            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            //Left
            if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {thisShape.putFront(shape, null, f_position, f_velocity, f_thisPosition);}

            //Top/Slope
            else{tileForces_Top(shape, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //101001 Top & DownLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front.
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {
                    @fixed int slant = iso.f_getXSlant_Right();
                    f_thisPosition.x -= slant;

                    shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);
                    f_thisPosition.x += slant;
                }

                //Left.
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {
                    @fixed int slant = iso.f_getYSlant_Front() - iso.f_getYSlant_Back();
                    f_thisPosition.y -= slant;

                    shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);
                    f_thisPosition.y += slant;
                }

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);
                //iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);


                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Back
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else{shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //101010 Top & DownRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front.
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {
                    @fixed int slant = iso.f_getXSlant_Right() - iso.f_getXSlant_Left();
                    f_thisPosition.x -= slant;

                    shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);
                    f_thisPosition.x += slant;
                }

                //Right
                else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {
                    @fixed int slant = iso.f_getYSlant_Front() - iso.f_getYSlant_Back();
                    f_thisPosition.y -= slant;

                    shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);
                    f_thisPosition.y += slant;
                }

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Front
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Right
                else{shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //101011 Top & Down and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, iso, f_thisPosition);}

                //Front
                else if(iso.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_Contact(null, f_position, f_velocity, iso, f_thisPosition);}

                //Left
                else if(iso.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_dl(null, f_position, f_velocity, iso, f_thisPosition);}
                //Right
                else if(iso.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_dr(null, f_position, f_velocity, iso, f_thisPosition);}

                //Slant
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Left
                if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Right
                else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front
                else{shape.putThis_Front_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },
        //
        //
        //
        //
        //
        //101100 Top & BothY
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

                //Top
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {iso.putTop(shape, null, f_position, f_velocity, f_thisPosition);}

                //Back/Front/Slant
                else{iso.putVertical(shape, null, f_position, f_velocity, f_oldPosition, f_thisPosition);}

                //We're done here.
                return;
            }

            //Top/Slope Left/Right/Slant
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {tileForces_BothY(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);}
        },

        //101101 Top & BothY and Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);
                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, iso, f_thisPosition);}

                //Back.
                else if(iso.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, iso, f_thisPosition);}
                //Front.
                else if(iso.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, iso, f_thisPosition);}

                //Left.
                else if(iso.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_Contact(null, f_position, f_velocity, iso, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Left
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Right
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else{shape.putThis_Left_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //101110 Top & BothY and Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);
                @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top.
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, iso, f_thisPosition);}

                //Back.
                else if(iso.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, iso, f_thisPosition);}
                //Front.
                else if(iso.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, iso, f_thisPosition);}

                //Left.
                else if(iso.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_Contact(null, f_position, f_velocity, iso, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }

            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Left
                if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Right
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else{shape.putThis_Right_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //101111 Top & BothY and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
            f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            
            //Left
            else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Top/Slope
            else{tileForces_Top(shape, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //
        //110000 BothZ
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //TODO Find the rest of the isosceles triangle edge cases.
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

                //Bottom
                if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Top
                if(thisShape.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}

                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                return;
            }

            tileForces_BothZ(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);
        },

        //110001 BothZ & Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Left
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {thisShape.putLeft(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //110010 BothZ & Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Right
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {thisShape.putRight(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //110011 BothZ & BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Left/Right
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {tileForces_BothX(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //110100 BothZ & Up
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Back
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {thisShape.putBack(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //110101 BothZ & UpLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Bottom
                else if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Bottom
                if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else{shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //110110 BothZ & UpRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Bottom
                else if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Right
                else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant.
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Bottom
                if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z - f_oldPosition.z), f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Back
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Right
                else{shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //110111 BothZ & Up and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z - f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Slope
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {shape.putThis_Back_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },
        //
        //
        //
        //
        //
        //111000 BothZ & Down
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Front
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {thisShape.putFront(shape, null, f_position, f_velocity, f_thisPosition);}
        },

        //111001 BothZ & DownLeft
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Bottom
                else if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Bottom
                if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z - f_oldPosition.z), f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else{shape.putThis_Left_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //111010 BothZ & DownRight
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            if(thisShape instanceof Isosceles_Triangle)
            {
                Isosceles_Triangle iso = (Isosceles_Triangle)thisShape;

                @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z),
                f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y),
                f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

                //Top
                if(iso.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}
                //Bottom
                else if(iso.collide_Bottom(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front
                else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
                {shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Slant
                else{iso.putSlant(shape, null, f_position, f_velocity, f_thisPosition);}

                //We're done.
                return;
            }            

            //Top/Slope
            if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {
                @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

                //Bottom
                if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z - f_oldPosition.z), f_thisPosition.z))
                {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Front
                else if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y)
                || thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
                {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

                //Left
                else{shape.putThis_Right_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}
            }
        },

        //111011 BothZ & Down and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_xVel = f_velocity.x + (f_position.x - f_oldPosition.x);

            //Left
            if(thisShape.collide_Left(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Left_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Right
            else if(thisShape.collide_Right(shape, f_oldPosition.x, f_xVel, f_thisPosition.x))
            {shape.putThis_Right_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z - f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Slope
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {shape.putThis_Front_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //
        //
        //
        //
        //
        //111100 BothZ & BothY
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            //Bottom
            if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Back/Front
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {tileForces_BothY(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);}
        },

        //111101 BothZ & BothY and Left
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Left
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {shape.putThis_Left_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //111110 BothZ & BothY and Right
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {
            @fixed int f_yVel = f_velocity.y + (f_position.y - f_oldPosition.y);

            //Back
            if(thisShape.collide_Back(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Front
            else if(thisShape.collide_Front(shape, f_oldPosition.y, f_yVel, f_thisPosition.y))
            {shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}

            //Bottom
            else if(thisShape.collide_Bottom(shape, f_oldPosition.z, f_velocity.z + (f_position.z + f_oldPosition.z), f_thisPosition.z))
            {shape.putThis_Bottom(null, f_position, f_velocity, thisShape, f_thisPosition);}
            //Top/Left
            else if(!tileForces_CheckTop(shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition))
            {shape.putThis_Right_Contact(null, f_position, f_velocity, thisShape, f_thisPosition);}
        },

        //111111 BothZ & BothY and BothX
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {shape.putThis_OutComposite(null, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition);},
    };

    /*
        (shape, f_position, f_velocity, f_oldPosition, thisShape, f_thisPosition) -> 
        {

        },
    */
    
    /**This is the function used for the 32nd slot in tileForces, since an array can't be referenced during its own initialization.*/
    public static void tileForces_BothX(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    Shape_Box thisShape, fixedVector3 f_thisPosition)
    {
        //Left
        if(f_oldPosition.x + shape.f_left() + (shape.f_getWidth() >> 1) < f_thisPosition.x + thisShape.f_left() + (thisShape.f_getWidth() >> 1))
        {thisShape.putLeft(shape, null, f_position, f_velocity, f_thisPosition);}

        //Right
        else{thisShape.putRight(shape, null, f_position, f_velocity, f_thisPosition);}
    }


    //
    public static void tileForces_BothY(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    Shape_Box thisShape, fixedVector3 f_thisPosition)
    {
        //Back
        if(f_oldPosition.y + shape.f_back() + (shape.f_getHeight() >> 1) < f_thisPosition.y + thisShape.f_back() + (thisShape.f_getHeight() >> 1))
        {thisShape.putBack(shape, null, f_position, f_velocity, f_thisPosition);}

        //Front
        else{thisShape.putFront(shape, null, f_position, f_velocity, f_thisPosition);}
    }

    public static void tileForces_BothY_left(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    Shape_Box thisShape, fixedVector3 f_thisPosition)
    {
        //Back
        if(f_oldPosition.y + shape.f_back() + (shape.f_getHeight() >> 1) < f_thisPosition.y + thisShape.f_back() + (thisShape.f_getHeight() >> 1))
        {shape.putThis_Back_ul(null, f_position, f_velocity, thisShape, f_thisPosition);}

        //Front
        else{shape.putThis_Front_dl(null, f_position, f_velocity, thisShape, f_thisPosition);}
    }

    public static void tileForces_BothY_right(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    Shape_Box thisShape, fixedVector3 f_thisPosition)
    {
        //Back
        if(f_oldPosition.y + shape.f_back() + (shape.f_getHeight() >> 1) < f_thisPosition.y + thisShape.f_back() + (thisShape.f_getHeight() >> 1))
        {shape.putThis_Back_ur(null, f_position, f_velocity, thisShape, f_thisPosition);}

        //Front
        else{shape.putThis_Front_dr(null, f_position, f_velocity, thisShape, f_thisPosition);}
    }


    //
    public static void tileForces_BothZ(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition,
    Shape_Box thisShape, fixedVector3 f_thisPosition)
    {
        if(f_oldPosition.z + shape.f_bottom() + (shape.f_getDepth() >> 1) < f_thisPosition.z + thisShape.f_bottom() + (thisShape.f_getDepth() >> 1))
        {thisShape.putBottom(shape, null, f_position, f_velocity, f_thisPosition);}
        //
        else{thisShape.putTop(shape, null, f_position, f_velocity, f_thisPosition);}
    }
    //
    //
    public static void tileForces_Top(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, Shape_Box thisShape, fixedVector3 f_thisPosition)
    {
        if(thisShape instanceof Slope_Triangle)
        {
            Slope_Triangle st = (Slope_Triangle)thisShape;

            //if(f_position.z + shape.bottom() >= f_thisPosition.z + st.bottom() + st.getZSlope_Bottom())
            //{
            st.putOnSlope(shape, null, f_position, f_velocity, f_thisPosition);
            //}
        }
        else{shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);}
    }
    //
    public static boolean tileForces_CheckTop(Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity, fixedVector3 f_oldPosition, 
    Shape_Box thisShape, fixedVector3 f_thisPosition)
    {
        @fixed int f_zVel = f_velocity.z + (f_position.z - f_oldPosition.z);

        if(thisShape instanceof Slope_Triangle)
        {
            Slope_Triangle st = (Slope_Triangle)thisShape;
            //
            if(st.collide_Slope_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
            {
                st.putOnSlope(shape, null, f_position, f_velocity, f_thisPosition);
                return true;
            }
        }
        else if(thisShape.collide_Top(shape, f_oldPosition.z, f_zVel, f_thisPosition.z))
        {
            shape.putThis_Top(null, f_position, f_velocity, thisShape, f_thisPosition);
            return true;
        }
        //
        return false;
    }
    //
    //
    //
    //
    @FunctionalInterface
    public interface TileEffect
    {
        /**
         * Invokes this Effect's...  effect.
         * 
         * @param shape the Shape to affect.
         * @param entity the Entity to affect.
         * @param phys the PhysicsComponent to affect.
         * @param f_position the position to affect.
         * @param f_velocity the velocity to affect.
         * @param thisShape this Tile's shape.
         * @param f_thisPosition this Tile's position.
         * @return true if Forces should be applied (some effects need forces applied before they can happen, which is why it's here).
         */
        public abstract boolean invoke(Entity entity, PhysicsComponent phys, Shape3D shape, fixedVector3 f_position, fixedVector3 f_velocity,
        Shape_Box thisShape, fixedVector3 f_thisPosition, Material material);
    }

    public static final TileEffect[] tileEffects =
    {
        //000000 None
        (entity, phys, shape, f_position, f_velocity, thisShape, f_thisPosition, material) ->
        {
            //TODO Set isGrounded to true.
            //phys.f_setTargetVelocityMultiplier(fixed(2));
            //phys.f_setCurrent_coFriction(fixed(0,6));
            return true;
        },

        //000001 NotFloor
        (entity, phys, shape, f_position, f_velocity, thisShape, f_thisPosition, material) ->
        {
            //If entity fell onto this Tile.
            if(tileForces_CheckTop(shape, f_position, f_velocity, f_position, thisShape, f_thisPosition))
            {
                //Make it bounce.
                //entity.bounce();
                f_velocity.y -= fixed(1);

                //Skip forces.
                return true;//false;
            }

            //Otherwise, don't.
            return true;
        },

        //000010 Neutral_DestroyThis0
        (entity, phys, shape, f_position, f_velocity, thisShape, f_thisPosition, material) ->
        {
            //If entity is able to destory this tile.
            //if(entity.getDamage() > 0)
            {
                //Create particles.

                //Remove tile from level.

                //TODO: Establish Tile material.
                //None
                //General
                //Wood
                //Stone
                //Pebbles
                //Glass
                //Grass
                //Dirt
                //Leaves
                //Sand
                //Mud
                //Shallow Water
                //Metal
                //Snow
                //Rubber
                //Cloth

                //Clanky_Metel?
                //Screaming_Sand?

                //Get Material.
                //Material material = 
                //Play sound.
                //material.destroy();

                //Skip Forces.
                //return false;
            }

            //Otherwise, don't.
            return true;
        },

        //000011 Neutral_DestroyThis1
        (entity, phys, shape, f_position, f_velocity, thisShape, f_thisPosition, material) -> 
        {
            //If entity is able to destory this tile.
            //if(entity.getDamage() > 20)
            {
                //Destory it.
                //Create particles.

                //Play sound.

                //Skip Forces.
                //return false;
            }

            //Otherwise, don't.
            return true;
        },

        //000100 Fire_DestroyThis
        (entity, phys, shape, f_position, f_velocity, thisShape, f_thisPosition, material) -> 
        {
            return false;
        },
    };

    /*
    (entity, phys, shape, f_position, f_velocity, thisShape, f_thisPosition, material) -> //
    {

    },
    */

    public static final int
    TILE_SPRITE_ID_PORTION  = 0b11111111111111000000000000000000, TILE_ID_BITS = 14,//16,384 different TileSprites.
    TILE_PROPERTIES_PORTION = 0b00000000000000111111111111111111, TILE_PROPERTIES_BITS = 18,//262,144 Tile Property Combinations.
    //
    TILE_SHAPE_PORTION      = 0b00000000000000111111000000000000, TILE_SHAPE_BITS = 6,//64 Shapes.
    //
    TILE_RESPONSE_PORTION   = 0b00000000000000000000111111111111, TILE_RESPONSE_BITS = 12,//4,096 Response Combinations.
    TILE_EFFECT_PORTION     = 0b00000000000000000000111111000000, TILE_EFFECT_BITS = 6,//64 Effects.
    TILE_FORCES_PORTION     = 0b00000000000000000000000000111111, TILE_FORCES_BITS = 6;//64 Force Combinations.
    

    //TILE_TYPE_PORTION = 0x000FFFFF;//1,048,576 different Tiles.
    //TileType:  00000000      000000     000000
    //             Shape       Effect     Forces

    /*
     * Properties:
     * 
     * Effects:
     * 
     * 
     * 
     * 
     * 
     * 000101 Earth_DestroyThis
     * 000110 Thunder_DestroyThis
     * 000111 Water_DestroyThis
     * 001000 Ice_DestoryThis
     * 001001 Wind_DestroyThis
     * 001010 Shadow_DestroyThis
     * 001011 Light_DestroyThis
     * 001100 Bouncy
     * 001101 Bouncy_XForce
     * 001110 Bouncy_YForce
     * 001111 Bouncy_ZForce
     * 
     * 010000 DamageAndForceKnockback
     * 010001 Fire_DamageAndForceKnockback
     * 010010 Earth_DamageAndForceKnockback
     * 010011 Thunder_DamageAndForceKnockback
     * 010100 Water_DamageAndForceKnockback
     * 010101 Ice_DamageAndForceKnockback
     * 010110 Wind_DamageAndForceKnockback
     * 010111 Shadow_DamageAndForceKnockback
     * 011000 Light_DamageAndForceKnockback
     * 011001 Water
     * 011010 Water_Force
     * 011011 Lava
     * 011100 Lava_Force
     * 011101 Kill (if tile has no forces, result in fall to death)
     * 011110 Ice
     * 011111 CrackingIce
     * 
     * 100000
     * 
     * 
     * 
     * Forces:
     * Top-Bottom-Down-Up-Right-Left
     * 
     * 
     * 
     * 
     */
    
    /**
     * Gets a tile based off of a value from the level's tiles array.
     * 
     * @param t the Level.TILE_TYPE_PROTION of a value in the tiles array.
     * @return a Tile based on the input.
     */
    public static Tile get(int t)
    {
        //Get the main Tile from the array.
        int index = (t & TILE_SHAPE_PORTION) >> TILE_RESPONSE_BITS;
        //System.out.println(index);
        Tile tile = tileTypes[index];

        //Return it.
        return tile;
    }
}
