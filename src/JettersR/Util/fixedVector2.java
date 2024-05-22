package JettersR.Util;
/**
 * 
 */
import JettersR.Util.Annotations.fixed;
import static JettersR.Util.Fixed.*;

public class fixedVector2
{
	//Values.
	public @fixed int x;
	public @fixed int y;

	/**Default no-arg constructor.*/
	public fixedVector2(){}

	/**Constructor.*/
	public fixedVector2(int wholeX, int fracX, int wholeY, int fracY)
	{
		//this.x = new fixed(wholeX, fracX);
		//this.y = new fixed(wholeY, fracY);
		this.x = fixed(wholeX, fracX);
		this.y = fixed(wholeY, fracY);
	}

	protected fixedVector2(int x, int y)
	{
		//this.x = new fixed(x);
		//this.y = new fixed(y);
		this.x = x;
		this.y = y;
	}


	/*
	 * Getters/Setters.
	 */
	public @fixed int int_x(){return f_toInt(x);}
	public @fixed int int_y(){return f_toInt(y);}

	/**Set the components of this vector with fixed's as input.*/
	public void set(@fixed int x, @fixed int y)
	{this.x = x; this.y = y;}

	/**Set the components of this vector with whole number ints as input.*/
	public void setInt(int x, int y)
	{
		this.x = fixed(x, 0);
		this.y = fixed(y, 0);
	}

	public void set(fixedVector3 input)
	{this.x = input.x; this.y = input.y;}

	/**
	 * Calcualtes the Dot Product of this fixed-vector with the input fixed-vector.
	 * Dot Product involves multiplying the x's plus multiplying the y's and so on.
	 * Useful for checking by how much do two normalized vectors face the same
	 * direction.
	 * 
	 * @param input The given vector to dot this vector by.
	 */
	public int dot(fixedVector2 input){return f_mul(x, input.x) + f_mul(y, input.y);}
}
