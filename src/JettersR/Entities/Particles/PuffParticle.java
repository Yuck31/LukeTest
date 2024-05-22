package JettersR.Entities.Particles;
/**
 * 
 */
import JettersR.Graphics.Screen;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.SpriteRenderers.ScaleSpriteRenderer;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

import JettersR.Level;


public class PuffParticle extends Particle
{
    private transient ScaleSpriteRenderer spriteRenderer;
    private transient Sprite normalMap;

    public PuffParticle(){}

    /**Constructor.*/
    public PuffParticle(@fixed int f_x, @fixed int f_y, @fixed int f_z, Sprite sprite, Sprite normalMap, int time)
    {
        super(f_x, f_y, f_z);

        //Set Sprite.
        spriteRenderer = new ScaleSpriteRenderer(sprite, this.f_position, false);

        //Set normalMap.
        this.normalMap = normalMap;

        //Set time.
        this.f_maxTime = fixed(time);
        this.f_time = f_maxTime;
    }

    @Override
    public void init(Level level){this.level = level;}
    //public CollisionObject getCollisionObject(){return null;}
    //public Light getLight(){return null;}

    @Override
    public void update(@fixed int f_timeMod)
    {
        f_time -= f_timeMod;
        spriteRenderer.f_setScaleAndCenter(f_div(f_time, f_maxTime));

        if(f_time <= 0){delete();}
    }

    @Override
    public void render(Screen screen, float scale)
    {
        spriteRenderer.renderLighting(screen, normalMap, f_position, scale);
        //
        //System.out.println(spriteRenderer.getXOffset());
    }
}
