package JettersR.Entities.Particles;

import JettersR.Graphics.Sprite;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

public class Step_PuffParticle extends PuffParticle
{
    private transient @fixed int f_xVelocity, f_yVelocity;
    private transient @fixed int f_zVelocity = fixed(1,0), f_zAcceleration = fixed(0,8);

    public Step_PuffParticle(){}

    public Step_PuffParticle(@fixed int f_x, @fixed int f_y, @fixed int f_z, @fixed int f_xVelocity, @fixed int f_yVelocity, Sprite sprite, Sprite normalMap, int time)
    {
        super(f_x, f_y, f_z, sprite, normalMap, time);
        
        //Set Velocity
        this.f_xVelocity = f_xVelocity;
        this.f_yVelocity = f_yVelocity;
    }
    
    @Override
    public void update(@fixed int f_timeMod)
    {
        super.update(f_timeMod);
        //
        f_position.x += f_mul(f_xVelocity, f_timeMod);
        f_position.y += f_mul(f_yVelocity, f_timeMod);
        //
        f_zVelocity -= f_mul(f_zAcceleration, f_timeMod);
        f_position.z += f_mul(f_zVelocity, f_timeMod);
    }
}
