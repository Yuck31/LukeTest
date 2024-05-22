package JettersR.Entities;
/**
 * 
 */
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;
import org.joml.Vector4f;

import JettersR.Game;
import JettersR.Level;
import JettersR.Controller;
import JettersR.Data.Profile;
import JettersR.Entities.Components.CollisionObject;
import JettersR.Entities.Components.PhysicsComponent;
import JettersR.Entities.Components.Lights.Light;
import JettersR.Entities.Particles.Step_PuffParticle;
import JettersR.Graphics.Screen;
import JettersR.Graphics.ShadowFace;
import JettersR.Graphics.ShadowSilhouette;
import JettersR.Graphics.ShadowVolume;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.SpriteSheet;
import JettersR.Graphics.Sprites;
import JettersR.Graphics.Animations.Functional_FrameAnimation;
import JettersR.Graphics.Animations.Lighting_Functional_FrameAnimation;
import JettersR.Graphics.SpriteRenderers.*;
import JettersR.Tiles.Material;
import JettersR.Util.fixedVector3;
import JettersR.Util.Annotations.fixed;
import JettersR.Util.Shapes.Shapes3D.*;

import static JettersR.Util.Fixed.*;

public class Player extends BasicEntity
{
    @FunctionalInterface//Functional Interface for Determining how this Player recieves input.
    private interface InputFunction{public abstract void invoke();}
    private transient InputFunction inputFunction;

    //Enum Representing this Player's current State
    public enum State
    {
        //The State the Player is in most of the time.
        NEUTRAL,

        //When the Player is Kicking a Bomb.
        KICKING,

        //When the Player is holding something.
        HOLDING,

        //When the Player is stunned.
        STUNNED,

        //When the Player dies.
        DEAD,
    }
    private State state = State.NEUTRAL;
    private boolean moving = false;
    private transient boolean isBeingHeld = false;

    //Controller Reference.
    private transient Controller controller;// = Game.controller;

    //Profile. Stores color pallete, controler scheme, etc.
    private transient Profile profile = null;

    //Player Number for getting Controller input
    private int playerNum = 0;

    //Player Type Stuff
    public transient static final byte
    PLAYER_TYPE_HUMAN = 0,
    PLAYER_TYPE_CPU = 1,
    PLAYER_TYPE_REPLAY = 2;


    //Stats
    public static final byte MAX_BOMBS = 8, MAX_FIRES = 8;
    public static final float MAX_SPEED = 2.6f;

    private byte bombs = 1, fires = 2;
    private @fixed int f_speed = fixed(1,0);

    //Bomb Kick
    public boolean hasBombKick = false;

    //Bomb Throw
    public static final byte BOMB_THROW = 1, SUPER_POWER_GLOVE = 2;
    public byte bombThrow = 0;

    //Bomb Punch
    public boolean hasBombPunch = false;

    private transient Lighting_Functional_FrameAnimation idle, move;

    //SpriteRenderer
    private transient ScaleSpriteRenderer spriteRenderer = null;
    //private transient RotateSpriteRenderer spriteRenderer = null;
    //private transient ShearSpriteRenderer spriteRenderer = null;

    //transient AAB_Box box = null;
    transient Cylinder cylinder = null;
    //transient Slope_Triangle st = null;
    transient Isosceles_Triangle iso = null;

    private transient Sprite[] playerSprites;
    private transient Sprite normalMap = null;

    private transient Sprite particle_Sprite = null, particle_NormalMap = null;

    public Player()
    {
        //System.out.println("Right?");
        controller = Game.controller;
        profile = new Profile();
    }

    /**In-Game Constructor.*/
    public Player(@fixed int f_x, @fixed int f_y, @fixed int f_z, Profile profile, int playerNum, byte player_Type)
    {
        this.controller = Game.controller;

        this.playerNum = playerNum;
        
        //position.set(x, y, z);
        f_position.set(f_x, f_y, f_z);

        this.profile = profile;
        //setPlayerType(player_Type);
    }

    /**Deserialization Constructor.*/
    private void initCollision()
    {
        setPlayerType(player_Type);

        //Create CollisionObject
        if(playerNum == 0)
        {
            //box = new AAB_Box(20, 20, 32, fixed(-10), 0, 0);
            //physicsComponent = new PhysicsComponent(this, box);

            cylinder = new Cylinder(fixed(9), 32, 0, fixed(12), 0);
            //System.out.println(cylinder);
            physicsComponent = new PhysicsComponent(this, cylinder);

            //collisionObject.setFriction(0.05f);
        }
        else
        {
            //st = new Slope_Triangle(64, 64, 64, Slope_Triangle.Type.LEFT, 4, 1);
            //collisionObject = new CollisionObject(this, st);

            
            iso = new Isosceles_Triangle(96, 96, 32, Isosceles_Triangle.TYPE_DL, 2, 1);
            physicsComponent = new PhysicsComponent(this, iso);
            physicsComponent.setTileCheckType(PhysicsComponent.COLLISIONCHECK_SKIP);
            physicsComponent.setEntityCheckType(PhysicsComponent.COLLISIONCHECK_SKIP);
            

            /*
            box = new AAB_Box(64, 64, 42);
            physicsComponent = new PhysicsComponent(this, box);
            physicsComponent.setTileCheckType(PhysicsComponent.COLLISIONCHECK_SKIP);
            physicsComponent.setEntityCheckType(PhysicsComponent.COLLISIONCHECK_SKIP);
            */
            

            //cylinder = new Cylinder(fixed(32), 16);
            //physicsComponent = new PhysicsComponent(this, cylinder);
            //physicsComponent.setEntityCheckType(PhysicsComponent.COLLISIONCHECK_SKIP);
        }
        //collisionObject.setTerminalVelocity(-100.0f);

        //Set Collision Response.
        //collisionObject.setCollisionResponse
        physicsComponent.setCollisionResponse
        (
            (entity, shape, f_position, f_velocity, f_thisPosition) ->
            {
                /*
                if(entity instanceof Bomb)
                {
                    if(state == State.NEUTRAL && input_Bomb_Pressed)
                    {
                        //Just so we don't accidently place a bomb immediatly after.
                        input_Bomb_Pressed = false;

                        //Pick Up the Bomb.
                    }
                }
                else
                */
                if(entity instanceof Player)
                {
                    if(state == State.NEUTRAL && input_Bomb_Pressed)
                    {
                        //Just so we don't accidently place a bomb immediatly after.
                        input_Bomb_Pressed = false;

                        //Pick Up the Player.
                    }
                }

                if(playerNum != 0)
                {
                    //System.out.println(position.x + " " + position.y);

                    //box.putLeft(shape, entity, position, velocity, thisPosition);
                    //box.putFront((AAB_Box)shape, entity, position, velocity, thisPosition);
                    //box.putOutComposite(shape, entity, f_position, f_velocity, f_position, f_thisPosition);

                    //cylinder.putCircleAround(shape, entity, position, velocity, thisPosition);
                    //cylinder.putOutOfCylinder(shape, entity, f_position, f_velocity, f_thisPosition);
                    //cylinder.putOutOfCylinder_Composite(shape, entity, f_position, f_velocity, f_thisPosition);
                    //cylinder.putTop(shape, entity, position, velocity, thisPosition);

                    //st.putOutComposite(shape, entity, position, velocity, thisPosition);
                    //st.putCylinderLeft((Cylinder)shape, entity, position, velocity, thisPosition);
                    //st.putOnSlope(shape, entity, position, velocity, thisPosition);

                    //iso.putSlope_UL(shape, entity, position, velocity, thisPosition);
                    //iso.putSlope_DL(shape, entity, position, velocity, thisPosition);
                    //iso.putSlant_DR(shape, entity, f_position, f_velocity, f_thisPosition);
                    //iso.putSlope(shape, entity, position, velocity, thisPosition);

                    iso.putOutComposite(shape, entity, f_position, f_velocity, f_position, f_thisPosition);

                    //iso.putCylinder_OutComposite((Cylinder)shape, entity, f_position, f_velocity, f_thisPosition);
                    //iso.putSlopeAndEdges(shape, entity, position, velocity, thisPosition);

                    //iso.putSlope_Front(shape, entity, position, velocity, thisPosition);
                    //iso.putSlope_Right(shape, entity, position, velocity, thisPosition);

                    f_velocity.x += physicsComponent.f_getVelocity().x;
                    f_velocity.y += physicsComponent.f_getVelocity().y;
                    if(physicsComponent.f_getVelocity().z > 0)
                    {
                        //f_position.z += physicsComponent.f_getVelocity().z;
                        f_velocity.z += physicsComponent.f_getVelocity().z;
                    }
                }
                else //if(!input_Bomb_Held)
                {
                    //cylinder.putOutOfCylinder(shape, entity, position, velocity, thisPosition);
                }
            }
        );

        //Sprites
        SpriteSheet playerSheet = Sprites.global_EntitySheet("Bomber_Front");
        playerSprites = playerSheet.loadLayout("PlayerSprites");
        //System.out.println(playerSprites.length);

        //normalMap = new Sprite(playerSheet, 0, 141, 30, 47);

        //Animation
        idle = new Lighting_Functional_FrameAnimation
        (
            Functional_FrameAnimation.load
            (
                "Player_Front_Idle", playerSprites,
                //
                this::blink,
                //{System.out.println("High Blink");},
                this::blink,
                //{System.out.println("Low Blink");},
                this::step
                //{System.out.println("High Blink and Exhale");}
                
            ),
            0,
            141
        );
        normalMap = idle.getNormalMap(0);
        //Animation
        move = new Lighting_Functional_FrameAnimation
        (
            Functional_FrameAnimation.load
            (
                "Player_Front_Move", playerSprites,
                //
                this::blink,
                //{System.out.println("Left Kick check");},
                this::step,
                //{System.out.println("Right Step and Left Kick check");}
                this::blink,
                //{System.out.println("Right Kick check");},
                this::step
                //{System.out.println("Left Step and Right Kick check");}
                
            ),
            0,
            141
        );
        spriteRenderer = new ScaleSpriteRenderer(playerSprites[0], this.f_position, false);
        
        //spriteRenderer = new ScaleSpriteRenderer(playerSprites[0], -(playerSprites[0].getWidth()/2), -(playerSprites[0].getHeight()/2)-8, 0, true, false);
        //spriteRenderer = new ScaleSpriteRenderer(playerSprites[0], -(playerSprites[0].getWidth()/2), -playerSprites[0].getHeight(), -playerSprites[0].getHeight(), true, false);
        //spriteRenderer = new RotateSpriteRenderer(playerSprites[0], true, false);
        //spriteRenderer = new ShearSpriteRenderer(playerSprites[0], true, false);
        //spriteRenderer.setOffset(0, -16);

        //spriteRenderer.setScale(3f);

        particle_Sprite = playerSprites[8];
        particle_NormalMap = playerSheet.getNormalMap_Sprite(particle_Sprite, 0, 141);
    }

    

    @Override
    public void init(Level level)
    {
        this.level = level;

        initCollision();
        //System.out.println("Init");
        physicsComponent.init(level);

        level.centerCameraTo(this, physicsComponent.f_getVelocity());
    }

    private byte player_Type = 0;

    /**Sets Player Type Functionality.*/
    public void setPlayerType(byte player_Type)
    {
        this.player_Type = player_Type;
        switch(player_Type)
        {
            case PLAYER_TYPE_HUMAN:
            inputFunction = this::humanInput;
            break;

            case PLAYER_TYPE_CPU:
            inputFunction = this::cpuInput;
            break;

            case PLAYER_TYPE_REPLAY:
            inputFunction = this::replayInput;
            break;
        }
    }

    /**Sets InputFunction.*/
    public void setInputFunction(InputFunction inputFunction){this.inputFunction = inputFunction;}

    /**Sets InputFunction back to its default.*/
    public void resetInputFunction(){setPlayerType(player_Type);}

    /*
     * Input Functions
     */
    private @fixed int f_input_xAxes = 0, f_input_yAxes = 0;
    
    private boolean
    input_Bomb_Pressed = false, input_Bomb_Held = false, 
    input_Punch_Pressed = false, input_Punch_Held = false,
    input_Remote_Pressed = false, input_Remote_Held = false,
    input_Special_Pressed = false, input_Special_Held = false;

    /**Human Input Function.*/
    private void humanInput()
    {
        //Movement
        boolean
        l = controller.inputHeld(profile, playerNum, Profile.action_LEFT),
        r = controller.inputHeld(profile, playerNum, Profile.action_RIGHT),
        u = controller.inputHeld(profile, playerNum, Profile.action_UP),
        d = controller.inputHeld(profile, playerNum, Profile.action_DOWN);

        //boolean
        //l = controller.menu_InputPressed(playerNum, Controller.menu_LEFT, true),
        //r = controller.menu_InputPressed(playerNum, Controller.menu_RIGHT, true),
        //u = controller.menu_InputPressed(playerNum, Controller.menu_UP, true),
        //d = controller.menu_InputPressed(playerNum, Controller.menu_DOWN, true);

        //If D-Pad isn't being used, use control stick instead.
        if(!l && !r && !u && !d)
        {
            f_input_xAxes = fixed( controller.getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_X) );
            f_input_yAxes = fixed( controller.getAxes(playerNum, GLFW_GAMEPAD_AXIS_LEFT_Y) );
        }
        else 
        {
            //Horizontal
            if(l && !r){f_input_xAxes = -f_ONE;}
            else if(!l && r){f_input_xAxes = f_ONE;}
            else{f_input_xAxes = 0;}

            //Vertical and movement accuracy (sorry, no fast diagonals here)
            if(u && !d)
            {
                if(f_input_xAxes < 0)
                {
                    f_input_xAxes = -CollisionObject.f_DIAGONAL_AXES;
                    f_input_yAxes = -CollisionObject.f_DIAGONAL_AXES;
                }
                else if(f_input_xAxes > 0)
                {
                    f_input_xAxes = CollisionObject.f_DIAGONAL_AXES;
                    f_input_yAxes = -CollisionObject.f_DIAGONAL_AXES;
                }
                else{f_input_yAxes = -f_ONE;}
            }
            else if(!u && d)
            {
                if(f_input_xAxes < 0)
                {
                    f_input_xAxes = -CollisionObject.f_DIAGONAL_AXES;
                    f_input_yAxes = CollisionObject.f_DIAGONAL_AXES;
                }
                else if(f_input_xAxes > 0)
                {
                    f_input_xAxes = CollisionObject.f_DIAGONAL_AXES;
                    f_input_yAxes = CollisionObject.f_DIAGONAL_AXES;
                }
                else{f_input_yAxes = f_ONE;}
            }
            else{f_input_yAxes = 0;}
        }

        //Bomb
        input_Bomb_Pressed = controller.inputPressed(profile, playerNum, Profile.action_BOMB);
        input_Bomb_Held = controller.inputHeld(profile, playerNum, Profile.action_BOMB);
        
        //Punch
        input_Punch_Pressed = controller.inputPressed(profile, playerNum, Profile.action_PUNCH);
        input_Punch_Held = controller.inputHeld(profile, playerNum, Profile.action_PUNCH);
        
        //Remote
        input_Remote_Pressed = controller.inputPressed(profile, playerNum, Profile.action_REMOTE);
        input_Remote_Held = controller.inputHeld(profile, playerNum, Profile.action_REMOTE);

        //Special
        input_Special_Pressed = controller.inputPressed(profile, playerNum, Profile.action_SPECIAL);
        input_Special_Held = controller.inputHeld(profile, playerNum, Profile.action_SPECIAL);

        //System.out.println(input_Bomb_Pressed + " " + input_Punch_Pressed);
        
    }

    /**Human Input Function.*/
    private void cpuInput()
    {

    }

    /**Human Input Function.*/
    private void replayInput()
    {

    }

    transient float ii = 0;
    transient fixedVector3 f_oldPosition = new fixedVector3();

    //About 0.31
    public static final @fixed int f_DIAG_THRESHOLD = fixed(0,79);

    @Override 
    /**Update this Player.*/
    //public void update(float timeMod)
    public void update(@fixed int f_timeMod)
    {
        //Recieve Input depending on if this is a Human, CPU, or Replay.
        inputFunction.invoke();

        ii += 0.01 * f_timeMod;
        if(playerNum != 0)
        {
            //st.setHeight((int)(Math.abs(Math.sin(ii)) * 128));
            //System.out.println(((float)st.getHeight() / st.getDepth()) + " " + Math.toDegrees(st.getNormXY()));
            //box.setZOffset((int)(Math.sin(ii) * 12));
            //System.out.println(box.getXOffset());
        }
        else
        {
            //cylinder.setZOffset((int)(Math.sin(ii) * 12));
        }

        @fixed int f_velocity = f_mul((2 * f_speed), f_timeMod);

        //if(input_Bomb_Held){position.z++;}
        if(input_Remote_Held){f_velocity = f_mul((16 * f_speed), f_timeMod);}

        switch(state)
        {
            case KICKING:
            {
                f_velocity = f_mul(fixed(1,0), f_timeMod);
            }
            case NEUTRAL:
            case HOLDING:
            {
                //Set Direction
                if(f_input_yAxes < 0)
                {
                    if(f_input_xAxes > f_DIAG_THRESHOLD){direction = Direction.UP_RIGHT;}
                    else if(f_input_xAxes < -f_DIAG_THRESHOLD){direction = Direction.UP_LEFT;}
                    else{direction = Direction.UP;}
                    moving = true;
                }
                else if(f_input_yAxes > 0)
                {
                    if(f_input_xAxes > f_DIAG_THRESHOLD){direction = Direction.DOWN_RIGHT;}
                    else if(f_input_xAxes < -f_DIAG_THRESHOLD){direction = Direction.DOWN_LEFT;}
                    else{direction = Direction.DOWN;}
                    moving = true;
                }
                else if(f_input_xAxes < 0){direction = Direction.LEFT; moving = true;}
                else if(f_input_xAxes > 0){direction = Direction.RIGHT; moving = true;}
                else{moving = false;}

                //Set Velocity Stuff to move.
                if(moving)
                {
                    //Reset idle.
                    idle.resetAnim();

                    //Update move and set normal map.
                    normalMap = move.update(f_timeMod, spriteRenderer);
                    //spriteRenderer.setSprite(normalMap);
                    
                    //Set velocity.
                    physicsComponent.f_setDirection(f_input_xAxes, f_input_yAxes);
                    physicsComponent.f_setVelocityMagnitude(f_velocity);
                }
                else
                {
                    //Reset move.
                    move.resetAnim();

                    //Update idle and set normal map.
                    normalMap = idle.update(f_timeMod, spriteRenderer);
                    //spriteRenderer.setSprite(normalMap);
                    
                    //Set velocity to 0.
                    physicsComponent.f_setVelocityMagnitude(0);
                }

                if(input_Bomb_Held){physicsComponent.f_setZVelocity( fixed(10) );}

                if(input_Punch_Held)
                {
                    f_position.x = (f_position.x >> Level.FIXED_TILE_BITS) << Level.FIXED_TILE_BITS;
                    f_position.y = (f_position.y >> Level.FIXED_TILE_BITS) << Level.FIXED_TILE_BITS;
                }
            }
            break;

            case STUNNED:
            break;

            default:
            break;
        }

        
        f_oldPosition.set(f_position);

        //Collision Check
        //if(playerNum == 0)
        physicsComponent.update(f_timeMod);

        if(f_position.z + physicsComponent.f_getZOffset() + physicsComponent.f_getZVelocity() < 0)
        {
            f_position.z = 0 - physicsComponent.f_getZOffset();
            physicsComponent.f_setZVelocity(-physicsComponent.f_getGravity());
        }
        

        //Place Bomb
        if(playerNum == 0)
        {
            //if(input_Bomb_Held){iso.setDimensions(96, 48, 32);}
            //else{iso.setDimensions(48, 96, 32);}

            //st.setHeight(Math.sin());

            //f_position.print();
        }

        //if(playerNum == 0)
        //System.out.println(position.x + " " + position.y + " " + (position.z + collisionObject.getShape().getZOffset()));
        //System.out.println(collisionObject.getXVelocity() + " " + collisionObject.getYVelocity());
        //System.out.println((float)Math.cos(collisionObject.getYVelocity() / collisionObject.getXVelocity()) + " " + (float)Math.sin(collisionObject.getXVelocity() / collisionObject.getYVelocity()));
        //System.out.println(physicsComponent.f_getYVelocity());
        //System.out.println((position.x - oldPosition.x) + " " + (position.y - oldPosition.y));
    }

    //Sprite getter.
    public Sprite getSprite(){return spriteRenderer.getSprite();}

    /*
    private transient ShadowVolume shadowVolume = null;

    private void createShadowVolume()
    {
        Vector3f lightDirection = level.getGlobalLight_Direction(),
        cl = new Vector3f(lightDirection);
        cl.z = 0.0f;
        cl.normalize();
        //cl.absolute();

        float radius = f_toFloat(cylinder.f_getRadius()),
        x0 = f_toFloat(f_position.x + cylinder.f_getXOffset()) + (radius * cl.y),
        y0 = f_toFloat(f_position.y + cylinder.f_getYOffset()) + (radius * -cl.x),
        z0 = f_toFloat(f_position.z + cylinder.f_bottom()),
        //
        x1 = f_toFloat(f_position.x + cylinder.f_getXOffset()) + (radius * -cl.y),
        y1 = f_toFloat(f_position.y + cylinder.f_getYOffset()) + (radius * cl.x),
        z1 = f_toFloat(f_position.z + cylinder.f_top());

        Vector3f p0 = new Vector3f(x0, y0, z1),
        p1 = new Vector3f(x1, y1, z1),
        p2 = new Vector3f(x1, y1, z0),
        p3 = new Vector3f(x0, y0, z0),

        sp0 = new Vector3f
        (
            p0.x + (400 * lightDirection.x),
            p0.y + (400 * lightDirection.y),
            p0.z + (400 * lightDirection.z)
        ),
        sp1 = new Vector3f
        (
            p1.x + (400 * lightDirection.x),
            p1.y + (400 * lightDirection.y),
            p1.z + (400 * lightDirection.z)
        ),
        sp2 = new Vector3f
        (
            p2.x + (400 * lightDirection.x),
            p2.y + (400 * lightDirection.y),
            p2.z + (400 * lightDirection.z)
        ),
        sp3 = new Vector3f
        (
            p3.x + (400 * lightDirection.x),
            p3.y + (400 * lightDirection.y),
            p3.z + (400 * lightDirection.z)
        );

        ShadowFace orig = new ShadowFace(p0, p1, p2, p3),
        top = new ShadowFace(p1, p0, sp0, sp1),
        side0 = new ShadowFace(p2, p1, sp1, sp2),
        bottom = new ShadowFace(p3, p2, sp2, sp3),
        side1 = new ShadowFace(p0, p3, sp3, sp0);

        shadowVolume = new ShadowVolume(top, orig, side0, side1, bottom);
    }
    */

    private transient ShadowSilhouette shadowSilhouette = null;

    private void createShadowSilhouette()
    {
        Vector3f lightDirection = level.getGlobalLight_Direction(),
        cl = new Vector3f(lightDirection);
        cl.z = 0.0f;
        cl.normalize();
        //cl.absolute();

        Sprite srs = spriteRenderer.getSprite();

        float radius = f_toFloat(cylinder.f_getRadius()),
        x0 = f_toFloat(f_position.x + cylinder.f_getXOffset()) - (radius+6),
        y0 = f_toFloat(f_position.y + cylinder.f_getYOffset()) + 5,
        z0 = f_toFloat(f_position.z + cylinder.f_bottom()),
        //z0 = 32.0f,
        //
        x1 = f_toFloat(f_position.x + cylinder.f_getXOffset()) + (radius+6),
        y1 = f_toFloat(f_position.y + cylinder.f_getYOffset()) + 5 + srs.getHeight();

        Vector3f
        p0 = new Vector3f(x0, y0, z0),
        p1 = new Vector3f(x1, y0, z0),
        p2 = new Vector3f(x1, y1, z0),
        p3 = new Vector3f(x0, y1, z0);

        shadowSilhouette = new ShadowSilhouette(srs, p3, p2, p1, p0);
    }


    private boolean stepped = false;
    public void blink(@fixed int f_timeMod)
    {
        stepped = false;
    }

    public void step(@fixed int f_timeMod)
    {
        //Roll number.
        //int r = Game.RANDOM.nextInt(3);
        //
        //Get material of current tile.
        //Material m = level.getMaterial((int)position.x >> Level.TILE_BITS, (int)position.y >> Level.TILE_BITS, (int)position.z >> Level.TILE_BITS);
        //m.step(r);

        if(!stepped)
        {
            @fixed int f_xa = f_position.x + physicsComponent.f_getXVelocity();
            @fixed int f_ya = f_position.y + physicsComponent.f_getYVelocity() + physicsComponent.getShape().f_getYOffset();
            //
            level.add(new Step_PuffParticle(f_xa, f_ya+6, f_position.z-2, -f_ONE, 0, playerSprites[8], particle_NormalMap, 30));
            level.add(new Step_PuffParticle(f_xa, f_ya+6, f_position.z-2, f_ONE, 0, playerSprites[8], particle_NormalMap, 30));
            level.add(new Step_PuffParticle(f_xa, f_ya+6, f_position.z-2, 0, -f_ONE, playerSprites[8], particle_NormalMap, 30));
            level.add(new Step_PuffParticle(f_xa, f_ya+6, f_position.z-2, 0, f_ONE, playerSprites[8], particle_NormalMap, 30));
            //
            stepped = true;
        }
    }

    //private Vector4f lightColor = new Vector4f(0.0f, 0.5f, 0.5f, 1.0f);
    //private fixed testF = new fixed(-16, 24);

    /**Render this Player's Sprite.*/
    public void render(Screen screen, float scale)
    {
        //System.out.println(scale);

        //spriteRenderer.renderLighting(screen, normalMap, position.x, position.y, position.z, scale);
        spriteRenderer.renderLighting(screen, normalMap, f_position, scale);

        //screen.renderSprite_Sc((int)position.x - 12, (int)position.y - 12, 0, 0, uhh, Sprite.Flip.NONE, scale, scale, true);
        //spriteRenderer.render(screen, position, scale);

        if(playerNum == 0)
        {
            //createShadowVolume();
            //screen.applyShadow(shadowVolume, scale, true);

            createShadowSilhouette();
            screen.applyShadow(shadowSilhouette, scale, true);

            //box.render(screen, scale, f_position);
            cylinder.render(screen, scale, f_position);
        }
        else
        {
            //box.render(screen, scale, f_position);
            //st.render(screen, scale, f_position);
            iso.render(screen, scale, f_position);
            //cylinder.render(screen, scale, f_position);
        }

        physicsComponent.render(screen, scale);
        
        //testF.print();
        //testF.printDecimal();
        //testF.toInt();
        //System.out.println(testF.toInt());
        
        /*
        screen.renderLine(((int)position.x + collisionObject.xOffset()),
        (int)((position.y + collisionObject.yOffset()) - ((position.z + collisionObject.zOffset()) / 2)),
        (int)position.x + collisionObject.xOffset(),
        (int)(position.y + collisionObject.yOffset()),
        lightColor, true);
        */
        
    }
}
