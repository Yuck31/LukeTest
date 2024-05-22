package JettersR.Entities.Particles;
/**
 * Author: Luke Sullivan
 * Last Edit: 8/31/2023
 */
import JettersR.Entities.Entity;
import JettersR.Util.Annotations.fixed;

public abstract class Particle extends Entity
{
    protected @fixed int f_maxTime = 0;
    protected @fixed int f_time = f_maxTime;

    public Particle(){}
    public Particle(@fixed int f_x, @fixed int f_y, @fixed int f_z){super(f_x, f_y, f_z);}
}
