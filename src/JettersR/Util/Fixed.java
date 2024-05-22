package JettersR.Util;
/**
 * Class dedecacted purely to Fixed-Point Number functions.
 * 
 * Author: Luke Sullivan
 * Last Edit: 3/30/2024
 */
import JettersR.Util.Annotations.fixed;

public final class Fixed
{
	public static final byte f_FRACTION_BITS = 8, f_WHOLE_BITS = (32 - f_FRACTION_BITS) - 1;

	public static final int f_SIGN_PORTION = 0x80000000,//, SIGN_OFFSET = FRACTION_BITS + WHOLE_BITS,
	f_WHOLE_PORTION = 0x7FFFFF00,
	f_SIGNED_WHOLE_PORTION = 0xFFFFFF00,
	f_FRACTION_PORTION = 0x000000FF,
	f_NUMBER_PORTION = ~f_SIGN_PORTION;
	
	public static final @fixed int
	f_MIN_VALUE = -(1 << f_WHOLE_BITS),//-8,388,608
	f_MAX_VALUE = (1 << f_WHOLE_BITS) - 1;//8,388,607

	public static final @fixed int
	f_ONE = f_FRACTION_PORTION+1,
	f_TWO = f_ONE * 2,
	f_THREE = f_ONE * 3,
	//
	f_HALF = f_ONE/2,
	f_QUARTER = f_ONE/4,
	f_EIGHTH = f_ONE/8,
	f_SIXTEENTH = f_ONE/16;

	public static final @fixed int
	f_PI = fixed(3,36),
	f_TWOPI = fixed(6,72),
	f_THREEPI = fixed(9,109),
	f_HALFPI = f_PI / 2,//fixed(1, 146);
	f_HALF_THREEPI = fixed(4,182);


	/**
	 * Creates a fixed with the given whole and fractional inputs.
	 * Number is formatted as whole.(fractionNumerator/256)
	 */
	public static @fixed int fixed(int whole, int fractionNumerator)
	{return (whole << f_FRACTION_BITS) | (fractionNumerator & f_FRACTION_PORTION);}

	/**Converts the given whole number int to a fixed.*/
	public static @fixed int fixed(int i){return (i << f_FRACTION_BITS);}
	public static @fixed int fixed16(int i){return (i << 16);}

	/**Converts the given float to a fixed.*/
	public static @fixed int fixed(float fl){return (int)(fl * f_ONE);}

	/**Converts the given double to a fixed.*/
	public static @fixed int fixed(double d){return (int)(d * f_ONE);}

	public static @fixed short fixedShort(int i){return (short)(i << f_FRACTION_BITS);}



	//Addition (x + y) can just be done with the "+" operator.
	//Subtraction (x - y) can just be done with the "-" operator.

	//Multiplication (x * y >> bits).
	/**
	 * Multiplies two FIXED's together.
	 * Use "*" operator when multiplying with a non-fixed-point int.
	 */
	public static @fixed int f_mul(@fixed int f_a, @fixed int f_b)
	{
		//Equivalent to multiplying a with b and dividing by 256.
		return (int)( ((long)f_a * (long)f_b) >> f_FRACTION_BITS );
	}
	public static @fixed long f_mulL(@fixed long f_a, @fixed long f_b){return (f_a * f_b) >> f_FRACTION_BITS;}
	//public static @fixed int f16_mul(@fixed int f16_a, @fixed int f16_b){return (int)( ((long)f16_a * (long)f16_b) >> 16 );}

	/**Multiplies two FIXED's and rounds the result to the nearest whole number.*/
	public static @fixed int f_mulRound_Whole(@fixed int f_a, @fixed int f_b)
	{
		//Multiply.
		int result = (int)( ((long)f_a * (long)f_b) >> f_FRACTION_BITS );

		//Round to nearest whole number.
		return ((result & f_FRACTION_PORTION) >= f_HALF) ? (result & f_SIGNED_WHOLE_PORTION) + f_ONE : (result & f_SIGNED_WHOLE_PORTION);
	}


	//Division (x << bits / y).
	/**
	 * Divides two FIXED's.
	 * Use "/" operator when dividing with a non-fixed-point int denominator.
	 */
	public static @fixed int f_div(@fixed int f_numerator, @fixed int f_denominator)
	{
		//Equivalent to multiplying the numerator by 256 and dividing by denominator.
		return (int)( ((long)f_numerator << f_FRACTION_BITS) / (long)f_denominator );
	}
	//public static @fixed int f16_div(@fixed int f_numerator, @fixed int f_denominator){return (int)( ((long)f_numerator << 16) / (long)f_denominator );}

	/**Version of f_div that returns 0 when the denominator is 0.*/
	public static @fixed int f_divSAFE(@fixed int f_numerator, @fixed int f_denominator)
	{return (f_denominator == 0) ? 0 : (int)( ((long)f_numerator << f_FRACTION_BITS) / (long)f_denominator );}

	public static @fixed long f_divL(@fixed long numerator, @fixed long denominator)
	{
		//Equivalent to multiplying the numerator by 256 and dividing by denominator.
		return (numerator << f_FRACTION_BITS) / denominator;
	}

	/**Divides two FIXED's and rounds the result to the nearest whole number.*/
	public static @fixed int f_divRound_Whole(@fixed int f_numerator, @fixed int f_denominator)
	{
		@fixed int f_result = (int)( ((long)f_numerator << f_FRACTION_BITS) / (long)f_denominator );

		return ((f_result & f_FRACTION_PORTION) >= f_HALF) ? (f_result & f_WHOLE_PORTION) + f_ONE : (f_result & f_WHOLE_PORTION);
	}

	public static @fixed int f_divRound_Precision(@fixed int f_numerator, @fixed int f_denominator)
	{
		//Temporarily use a 16-bit precision number.
		@fixed int f16_result = (int)( ((long)f_numerator << (f_FRACTION_BITS + 16)) / ((long)f_denominator << f_FRACTION_BITS) );
		//"(FRACTION_BITS + 16)" is used to use the 8-bit precision number as a 16-bit one.

		//Get the absolute value of this 16-bit number.
		int signResult = (f16_result >> 31);
		@fixed int f_absolute = (f16_result ^ signResult) - signResult;
		signResult |= 1;

		//Round to nearest 256th.
		return ((f_absolute & f_FRACTION_PORTION) >= f_HALF) ? (f16_result >> f_FRACTION_BITS) + signResult : f16_result >> f_FRACTION_BITS;
	}
	


	//Negation.
	public static @fixed int f_abs(@fixed int f_value)
	{
		//Sign xor value plus sign.
		//return ((f_value & SIGN_PORTION) >> 31) ^ f_value + ((f_value & SIGN_PORTION) >> 31);

		//Bit shift to the right to fill every bit with the sign bit.
		int signResult = (f_value >> 31);//(f_value & SIGN_PORTION) >> 31);
		return (f_value ^ signResult)//-Xor input to signResult to filp from negative to positive.
		- signResult;
		//-If input was negative, the result will be off by one. Because signResult equals all ones (0xFFFFFFFF = -1), we can
		// just subtract that from the result to correct it. signResult would equal 0 if positive, and thus won't do anything.
	}
	


	//
	//Exponential.
	//

	/**
	 * Calculates the square of a Fixed-Point Number.
	 * 
	 * @param f an input Fixed-Point number.
	 * @return f^2 (in other words, f * f)
	 */
	public static @fixed int f_square(@fixed int f)
	{
		long fl = (long)f;
		return (int)( (fl * fl) >> f_FRACTION_BITS );
	}

	/**
	 * Calculates the square of a Fixed-Point Number. Returns a long.
	 * 
	 * @param f an input Fixed-Point number.
	 * @return f^2 (in other words, f * f)
	 */
	public static @fixed long f_squareL(@fixed int f)
	{
		long fl = (long)f;
		return ( (fl * fl) >> f_FRACTION_BITS );
	}

	/**
	 * Calculates the square of a Fixed-Point Number, rounded to the nearest whole number.
	 * 
	 * @param f an input Fixed-Point number.
	 * @return f^2 (in other words, f * f), rounded to the nearest whole number.
	 */
	public static @fixed int f_squareRound_Whole(@fixed int f)
	{
		//Square.
		@fixed long f_l = (long)f;
		int result = (int)( (f_l * f_l) >> f_FRACTION_BITS );

		//Round to nearest whole number.
		return ((result & f_FRACTION_PORTION) >= f_HALF) ? (result & f_SIGNED_WHOLE_PORTION) + f_ONE : (result & f_SIGNED_WHOLE_PORTION);
	}

	/**
	 * Calculates the cube of a Fixed-Point Number.
	 * 
	 * @param f an input Fixed-Point number.
	 * @return f^3 (in other words, f * f * f)
	 */
	public static @fixed int f_cube(@fixed int f)
	{
		@fixed long f_l = (long)f;
		return (int)( (((f_l * f_l) >> f_FRACTION_BITS) * f) >> f_FRACTION_BITS);
	}

	/**
	 * Multiplies the given Fixed-Point Number to "power"th power.
	 * 
	 * @param f_input an input Fixed-Point number.
	 * @param power the number of times the number should be multiplied by.
	 * @return f^power.
	 */
	public static @fixed int f_power(@fixed int f_input, int power)
	{
		@fixed long f_l = (long)f_input;
		@fixed long f_result = f_input;

		for(int p = 2; p <= power; p++)
		{
			f_result = (f_result * f_l) >> f_FRACTION_BITS;
		}

		return (int)f_result;
	}


	/**
	 * Calculates the square root of the given FIXED.
	 * Special thanks to chmike from Github.
	 * 
	 * @param f_input A fixed-point value.
	 * @return The square root of "f".
	 */
	public static @fixed int f_sqrt(@fixed long f_input)
	{
		//return (int)StrictMath.sqrt( (long)f << FRACTION_BITS);

		if(f_input <= 0)
		{
			//System.out.println("why?");
			return 0;
		}

		
		long value = f_input << f_FRACTION_BITS;

		//long currentBit = 1 << 62,//Leftmost positive bit.
		long currentBit = 1L << (30 + f_FRACTION_BITS),//Leftmost positive bit.
		f_result = 0L;
		


		/*
		long value = f_input << (FRACTION_BITS + 16);

		long currentBit = 1L << (30 + FRACTION_BITS + 16),//Leftmost positive bit.
		f_result = 0L;
		*/


		//int num = 0;
		//Get to leftmost bit of input that is used.
    	while(currentBit > value)
		{
			currentBit >>>= 2;
			//num++;
		}
		//System.out.println(num + " " + Long.toHexString(currentBit) + " " + Long.toHexString(v));

		//Starting from the leftmost bit that is used and going right.
		while(currentBit > 0)
		{
			//t = uh
			long t = f_result + currentBit;
			f_result >>= 1;//Divide f_result by 2.

			//
			if(value >= t)
			{
				//Set the bits
				value -= t;

				//NOW apply this bit in the result value to 1.
				f_result += currentBit;        
			}

			//Set currentBit right 2 bits. (Divide by 4)
			currentBit >>= 2;
		}

		return (int)f_result;

		//Round to nearest 256th.
		//return (int)( ((f_result & FRACTION_PORTION) >= f_HALF) ? (f_result >> FRACTION_BITS) + 1 : f_result >> FRACTION_BITS );
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	/*
	public static @fixed int f_sqrtRound(@fixed int f)
	{
		int result = (int)StrictMath.sqrt( (long)f << FRACTION_BITS);

		return ((result & FRACTION_PORTION) > f_HALF) ? (result & SIGNED_WHOLE_PORTION) + f_ONE : (result & SIGNED_WHOLE_PORTION);
	}
	*/


	//
	//Triganometry.
	//

	/**
	 * Calculates the hypotenuse of two FIXED's.
	 */
	public static @fixed int f_length(@fixed int a, @fixed int b)
	{
		long al = (long)a, bl = (long)b;

		//return (int)StrictMath.sqrt( (((al * al) >> FRACTION_BITS) + ((bl * bl) >> FRACTION_BITS)) << FRACTION_BITS );
		return (int)f_sqrt( ((al * al) >> f_FRACTION_BITS) + ((bl * bl) >> f_FRACTION_BITS) );
	}

	/**
	 * Calculates a missing side from a given hypotenuse and other side.
	 */
	public static @fixed int f_reverseLength(@fixed int hyp, @fixed int oth)
	{
		long hl = (long)hyp, ol = (long)oth;

		//return (int)StrictMath.sqrt( (((hl * hl) >> FRACTION_BITS) - ((ol * ol) >> FRACTION_BITS)) << FRACTION_BITS );
		return f_sqrt( (int)( ((hl * hl) >> f_FRACTION_BITS) - ((ol * ol) >> f_FRACTION_BITS) ) );
	}

	public static final @fixed int f_ONEEIGHTY = fixed(180);

	/**Converts the given angle in degrees to radians.*/
	public static @fixed int f_toRadians(@fixed int f_degrees)
	{return (f_degrees * f_PI) / f_ONEEIGHTY;}

	/**
	 * Calculates the Sine of the input Fixed-Point number.
	 * 
	 * @param f_input A fixed-point value in radians.
	 * @return the Cosine of f.
	 */
	public static @fixed int f_sin(final @fixed int f_input)
	{
		//TODO 
		//The equation: x^1/1! - x^3/3! + x^5/5! - x^7/7! + x^9/9!

		//min + Math.abs(((input + range) % (range * 2)) - range);
		//Loop the value between -pi and +pi, since those are where the unique values are.
		long f_l = (f_input < 0)
		? (long)( ((f_input - f_PI) % f_TWOPI) + f_PI )
		: (long)( -f_PI + ((f_input + f_PI) % f_TWOPI) );

		//f_print("Os", (int)f_l);

		//1 is done for us. So...
		//3
		long f_curPow = (((f_l * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		long f_result = ( (f_curPow << f_FRACTION_BITS) / -6);//-1*2*3
		//5
		f_curPow = (((f_curPow * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		f_result += ( (f_curPow << f_FRACTION_BITS) / 120);//+1*2*3*4*5
		//7
		f_curPow = (((f_curPow * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		f_result += ( (f_curPow << f_FRACTION_BITS) / -5040);//-1*2*3*4*5*6*7
		//9
		f_curPow = (((f_curPow * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		f_result += ( (f_curPow << f_FRACTION_BITS) / 362880);//+1*2*3*4*5*6*7*8*9

		//Add input now and return.
		return (int)(f_l + (f_result >> f_FRACTION_BITS));
	}

	/**
	 * Calculates the Cosine of the input Fixed-Point number.
	 * 
	 * @param f_input A fixed-point value.
	 * @return the Cosine of f.
	 */
	public static @fixed int f_cos(final @fixed int f_input)
	{
		//The equation: 1 - x^2/2! + x^4/4! - x^6/6! + x^8/8! - x^10/10!

		//min + Math.abs(((input + range) % (range * 2)) - range);
		//Loop the value between -pi and +pi, since those are where the unique values are.
		long f_l = (f_input < 0)
		? (long)( ((f_input - f_PI) % f_TWOPI) + f_PI )
		: (long)( -f_PI + ((f_input + f_PI) % f_TWOPI) );

		/*
		int currentSign = 1; // 64-bit values
		long fact = 1;
		@fixed int f_sum = f_ONE;

		// n = 13 max for 32-bit
		// n = 20 max for 64-bit
		// 12 for 32-bit and 20 for 64-bit because MacLaurin series simplifies to 2n
		// Decrease n for less accuracy, n < 6 yields more-than-marginal error
		// Factorial op hits the upper limit
		// TODO: Cache power and *= n each iteration to reduce calls
		// t = ~t | 1 is a bitwise op to flip the sign of t
		// sum += (pow(x,n) / fact(n)) * (t *= -1)
		for(int pow = 2; pow <= 10; pow += 2)
		{
			//sum += power(x, n) / (f *= n * (n - 1)) * (t = ~t | 1);

			fact *= pow * (pow - 1);
			currentSign = ~currentSign | 1;
			f_sum += (f_power((int)f_l, pow) << FRACTION_BITS) / (fact * currentSign);
		}

		return f_sum >> FRACTION_BITS;
		*/

		//2
		long f_curPow = (f_l * f_l) >> f_FRACTION_BITS;
		long f_result = ( (f_curPow << f_FRACTION_BITS) / -2);//-1*2
		//4
		f_curPow = (((f_curPow * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		f_result += ( (f_curPow << f_FRACTION_BITS) / 24);//+1*2*3*4
		//6
		f_curPow = (((f_curPow * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		f_result += ( (f_curPow << f_FRACTION_BITS) / -720);//-1*2*3*4*5*6
		//8
		f_curPow = (((f_curPow * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		f_result += ( (f_curPow << f_FRACTION_BITS) / 40320);//+1*2*3*4*5*6*7*8
		//10
		f_curPow = (((f_curPow * f_l) >> f_FRACTION_BITS) * f_l) >> f_FRACTION_BITS;
		f_result += ( (f_curPow << f_FRACTION_BITS) / -3628800);//-1*2*3*4*5*6*7*8*9*10

		//Add one now and return.
		return f_ONE + (int)(f_result >> f_FRACTION_BITS);
	}


	//
	// Casting.
	//

	private static final double FRACTION_RECIPORACAL = 1.0 / (double)(f_ONE);
	private static final float FLOAT_FRACTION_RECIPORACAL = (float)FRACTION_RECIPORACAL;

	//Translates this fixed as a double.
	public static double f_toDouble(@fixed int value)
	{
		//int sign = (value & SIGN_PORTION) >> FRACTION_BITS;

		//return (double)(sign + ((value & WHOLE_PORTION) >> FRACTION_BITS)) + ((value & FRACTION_PORTION) * FRACTION_RECIPORACAL);
		return (double)( ((value & f_SIGNED_WHOLE_PORTION) >> f_FRACTION_BITS)) + ((value & f_FRACTION_PORTION) * FRACTION_RECIPORACAL);
	}

	//Translates this fixed as a float.
	public static float f_toFloat(@fixed int value)
	{
		//int sign = (value & SIGN_PORTION) >> FRACTION_BITS;

		//return (float)(sign + ((value & WHOLE_PORTION) >> FRACTION_BITS)) + ((value & FRACTION_PORTION) * FLOAT_FRACTION_RECIPORACAL);
		return (float)( ((value & f_SIGNED_WHOLE_PORTION) >> f_FRACTION_BITS) + ((value & f_FRACTION_PORTION) * FLOAT_FRACTION_RECIPORACAL) );
	}

	//Translates the given fixed as a rounded down int.
	public static int f_toInt(@fixed int value)
	{
		//Truncate the fraction.
		return (value & f_SIGNED_WHOLE_PORTION) >> f_FRACTION_BITS;
		//Since a signed bit-shift is used, I do not have to negate the sign value.
	}

	//Translates this fixed as a rounded to the nearest whole int.
	public static int f_toRoundedInt(@fixed int value)
	{
		//Fration > 128? Add one.
		return ( (value & f_SIGNED_WHOLE_PORTION) >> f_FRACTION_BITS)
		+ ( ((value & f_FRACTION_PORTION) > f_ONE / 2) ? 1 : 0 );
	}


	//
	// String printing.
	//

	/**Prints the input fixed-point value as a Whole.0 to FRACTION_VALUES string.*/
	public static String f_toString(@fixed int value)
	{
		int sign = (value & f_SIGN_PORTION) >> f_FRACTION_BITS,
		whole = (value & f_WHOLE_PORTION) >> f_FRACTION_BITS,
		frac = (value & f_FRACTION_PORTION);
		//System.out.println(sign);

		if(sign != 0)
		{
			sign++;
			frac = f_ONE - frac;
			if(frac >= 256)
			{
				frac = -0;
				whole--;
			}

			return "-" + -(sign + whole) + "." + String.format("%03d", frac);
		}

		return sign + whole + "." + String.format("%03d", frac);
	}
	public static void f_print(@fixed int value){System.out.println(f_toString(value));}
	public static void f_print(String label, @fixed int value){System.out.println(label + ": " + f_toString(value));}
	public static void f_print(String label0, @fixed int value0, String label1, @fixed int value1)
	{
		System.out.println
		(
			label0 + ": " + f_toString(value0) + " " +
			label1 + ": " + f_toString(value1)
		);
	}
	public static void f_print(String label0, @fixed int value0, String label1, @fixed int value1, String label2, @fixed int value2)
	{
		System.out.println
		(
			label0 + ": " + f_toString(value0) + " " +
			label1 + ": " + f_toString(value1) + " " +
			label2 + ": " + f_toString(value2)
		);
	}
	public static void f_print(String label0, @fixed int value0, String label1, @fixed int value1, String label2, @fixed int value2, String label3, @fixed int value3)
	{
		System.out.println
		(
			label0 + ": " + f_toString(value0) + " " +
			label1 + ": " + f_toString(value1) + " " +
			label2 + ": " + f_toString(value2) + " " +
			label3 + ": " + f_toString(value3)
		);
	}

	public static void f_printHex(String label, @fixed int value){System.out.println(label + ": " + Integer.toHexString(value));}


	//Prints this fixed as a Whole.Decimal string.
	public static String f_toDecimalString(@fixed int value)
	{
		int sign = (value & f_SIGN_PORTION) >> f_FRACTION_BITS;
		byte inv = (byte)((sign != 0) ? f_ONE : 0);

		return sign + ((value & f_WHOLE_PORTION) >> f_FRACTION_BITS) + "." + //((value & FRACTION_PORTION) * FRACTION_RECIPORACAL);
		String.format("%f", -(inv - (value & f_FRACTION_PORTION)) * FRACTION_RECIPORACAL).substring(2);
	}
	public static void f_printDecimal(@fixed int value){System.out.println(f_toDecimalString(value));}
}
