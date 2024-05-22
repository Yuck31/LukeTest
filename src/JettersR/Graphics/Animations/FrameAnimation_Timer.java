package JettersR.Graphics.Animations;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/26/2023
 */
import JettersR.Util.Annotations.fixed;

//import static JettersR.Util.Fixed.*;

public class FrameAnimation_Timer
{
    //Frame.
    protected int frame = 0;

    //Rate of Change.
    private @fixed int f_time = 0;
    private @fixed int[] f_rates;

    //Loop Stuff.
    private boolean doesLoop = false, hasLooped = false;
    private int loopTo = -1;

    //Has a frame advanced?
    private boolean frameAdvanced = false;

    /**
     * Constructor.
     * 
     * @param loopTo
     * @param f_rates
     */
    public FrameAnimation_Timer(int loopTo, @fixed int[] f_rates)
    {
        //Set Rates.
        this.f_rates = f_rates;

        //Set loopTo frame.
        this.loopTo = loopTo;
        if(loopTo >= 0){doesLoop = true;}
    }

    /**
     * Constructor with one rate and a loopTo frame.
     * 
     * @param loopTo
     * @param frames
     * @param f_rate
     */
    public FrameAnimation_Timer(int loopTo, int frames, @fixed int f_rate)
    {
        //Set rate.
        this.f_rates = new @fixed int[frames];
        for(int i = 0; i < f_rates.length; i++){f_rates[i] = f_rate;}

        //Set loopTo.
        this.loopTo = loopTo;
        if(loopTo >= 0){doesLoop = true;}
    }

    /**
     * Constructor with 0 as the loopTo frame.
     * 
     * @param f_rates
     */
    public FrameAnimation_Timer(@fixed int[] f_rates)
    {
        //Set Rates.
        this.f_rates = f_rates;

        //Set loopTo frame.
        this.loopTo = 0;
        doesLoop = true;
    }

    /**Updates this animation.*/
    public final int update(@fixed int f_timeMod)
    {
        //Set frameAdvanced.
        frameAdvanced = false;

        //Increment time.
        f_time += f_timeMod;

        if(f_time >= f_rates[frame])
        {
            while(f_time >= f_rates[frame])
            {
                //Increment Time accordingly
                f_time -= f_rates[frame];

                //Increment frame accordingly
                frame++;
                frameAdvanced = true;

                if(frame >= f_rates.length)
                {
                    if(doesLoop)
                    {
                        frame = loopTo;
                        hasLooped = true;
                    }
                    else{frame = f_rates.length-1;}
                }
            }
        }
        else if(f_time < 0)
        {
            while(f_time < 0)
            {
                //Increment Time accordingly
                f_time += f_rates[frame];

                //Increment frame accordingly
                frame--;
                frameAdvanced = true;

                if((hasLooped && frame < loopTo) || frame < 0)
                {
                    if(doesLoop)
                    {
                        frame = f_rates.length-1;
                        hasLooped = true;
                    }
                    else{frame = f_rates.length-1;}
                }
            }
        }
        return this.frame;
    }

    //FrameRate Getters/Setters.
    public final @fixed int f_getFrameRate(){return f_rates[this.frame];}
    public final @fixed int f_getFrameRate(int f_fr){return f_rates[f_fr];}
    public final @fixed int[] f_getRates(){return f_rates;}
    //
    public final void f_setRates(@fixed int[] f_rates){this.f_rates = f_rates;}

    //Frame Getter/Setter.
    public final int getFrame(){return frame;}
    public final void setFrame(int frame){this.frame = frame; f_time = 0;}
    public final int getNumFrames(){return f_rates.length;}

    //Time Getter.
    public @fixed int f_getTime(){return f_time;}

    //LoopTo Getter.
    public int getLoopTo(){return loopTo;}

    public final boolean hasFrameAdvanced(){return frameAdvanced;}
    
    /**Resets this timer.*/
    public final void resetTimer()
    {
        frame = 0;
        f_time = 0;
        hasLooped = false;
    }
}
