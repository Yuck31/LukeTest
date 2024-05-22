package JettersR.Util;
/**
 * 
 */
import org.joml.Vector3f;

import JettersR.Util.Annotations.fixed;
import static JettersR.Util.Fixed.*;

public class fixedVector3
{
	//Global "zeros" Vector3.
    public static final fixedVector3 f_ZEROS = new fixedVector3();

	//For debugging purposes.
	public static void printZeros(){System.out.println(f_ZEROS);}

	//Values.
	public @fixed int x;
	public @fixed int y;
	public @fixed int z;

	/**Default no-arg constructor.*/
	public fixedVector3(){}
	
	/**Constructor.*/
	public fixedVector3(int wholeX, int fracX, int wholeY, int fracY, int wholeZ, int fracZ)
	{
		//this.x = new fixed(wholeX, fracX);
		//this.y = new fixed(wholeY, fracY);
		//this.z = new fixed(wholeZ, fracZ);
		this.x = fixed(wholeX, fracX);
		this.y = fixed(wholeY, fracY);
		this.z = fixed(wholeZ, fracZ);
	}	

	public fixedVector3(@fixed int f_x, @fixed int f_y, @fixed int f_z)
	{
		//this.x = new fixed(x);
		//this.y = new fixed(y);
		//this.z = new fixed(z);
		this.x = f_x;
		this.y = f_y;
		this.z = f_z;
	}
	

	/*
	 * Getters/Setters.
	 */
	public @fixed int int_x(){return f_toInt(x);}
	public @fixed int int_y(){return f_toInt(y);}
	public @fixed int int_z(){return f_toInt(z);}

	/**Set the components of this vector with fixed's as input.*/
	public void set(@fixed int x, @fixed int y, @fixed int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**Set the components of this vector with whole number ints as input.*/
	public void setInt(int x, int y, int z)
	{
		this.x = fixed(x, 0);
		this.y = fixed(y, 0);
		this.z = fixed(z, 0);
	}

	public void set(fixedVector3 input)
	{
		this.x = input.x;
		this.y = input.y;
		this.z = input.z;
	}


	//Addition.
	public void add(fixedVector3 fv)
	{
		this.x += fv.x;
		this.y += fv.y;
		this.z += fv.z;
	}
	public void add(@fixed int x, @fixed int y, @fixed int z)
	{this.x += x; this.y += y; this.z += z;}

	//Subtraction.
	public void sub(fixedVector3 fv)
	{
		this.x -= fv.x;
		this.y -= fv.y;
		this.z -= fv.z;
	}

	//
	//Multiplication.
	//
	public void mul(fixedVector3 f_v)
	{
		this.x = f_mul(this.x, f_v.x);
		this.y = f_mul(this.y, f_v.y);
		this.z = f_mul(this.z, f_v.z);
	}

	public void mulResult(fixedVector3 f_v0, fixedVector3 f_v1)
	{
		this.x = f_mul(f_v0.x, f_v1.x);
		this.y = f_mul(f_v0.y, f_v1.y);
		this.z = f_mul(f_v0.z, f_v1.z);
	}

	public void mulResult(@fixed int f_v0, fixedVector3 f_v1)
	{
		this.x = f_mul(f_v0, f_v1.x);
		this.y = f_mul(f_v0, f_v1.y);
		this.z = f_mul(f_v0, f_v1.z);
	}


	//
	//Division.
	//
	public void div(fixedVector3 fv)
	{
		this.x = f_div(this.x, fv.x);
		this.y = f_div(this.y, fv.y);
		this.z = f_div(this.z, fv.z);
	}



	/**
	 * Calcualtes the Dot Product of this fixed-vector with the input fixed-vector.
	 * Dot Product involves multiplying the x's plus multiplying the y's and so on.
	 * Useful for checking by how much do two normalized vectors face the same
	 * direction.
	 * 
	 * @param input The given vector to dot this vector by.
	 */
	public int dot(fixedVector3 input)
	{
		return
		f_mul(x, input.x) +
		f_mul(y, input.y) +
		f_mul(z, input.z);
	}

	/**
	 * Normalizes the components of this Vector, treating them as if the Vector's length was 1.
	 * Components become x, y, and z / length, values from -1 to +1.
	 */
	public fixedVector3 normalize()
	{
		@fixed int f_length = f_sqrt( f_square(x) + f_square(y) + f_square(z) );

		this.x = f_div(this.x, f_length);
        this.y = f_div(this.y, f_length);
        this.z = f_div(this.z, f_length);

		return this;
	}


	public Vector3f toVector3f(){return new Vector3f(f_toFloat(x), f_toFloat(y), f_toFloat(z));}

	public void print(){System.out.println("X: " + f_toString(x) + " Y: " + f_toString(y) + " Z: " + f_toString(z));}
}
