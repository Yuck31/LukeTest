#version 330 core
#define MAX_LIGHTS_ON_SCREEN 256
#define MAX_LIGHTS_PER_CELL 15
#define CELL_SIZE 16
#define SCR_WIDTH 960
#define SCR_HEIGHT 540
#define SCR_WIDTH_CELLS SCR_WIDTH / CELL_SIZE

//Input values from the vertex shader.
in vec3 frag_Position;
in vec4 frag_Color;
in vec4 frag_TexCoords;
flat in float frag_TexSlot;//Needs to be flat otherwise this value will be interpolated.
in float frag_Emission;
//in vec3 frag_Normal;

//Currently used textures per draw call.
uniform sampler2D uTextures[8];

//Currently used Screen Height.
uniform int uCurrent_scrHeight;

//Are we render transperant sprites? (0 = false, 1 = true)
uniform int uDoingTransperant = 0;

/*
 * Light Struct.
 */
struct Light
{
	//Position of Light and the area of effect outside of the light source.
	vec4 position_OuterRadius;

	//Can be used as either a cutoff angle and outer cutoff for directional lights or as dimensions for an area light.
	//(not a vec3 to prevent padding)
	float innerCutoffCosAngle_width;
	float outerCutoffCosAngle_height;
	float depth_length;

	//Can be used either the direction of a directional light or the used sides of an area light.
	float directionOrSide_X;
	float directionOrSide_Y;
	float directionOrSide_Z;

	//The main color of this light.
	float diffuseColor_R;
	float diffuseColor_G;
	float diffuseColor_B;

	//The color used when a "surface" faces away from the light.
	float ambientColor_R;
	float ambientColor_G;
	float ambientColor_B;
};


//Data shared with all shaders.
layout(std140) uniform GlobalBlock
{
	//View Matrix (we're not using it here).
	mat4 uView;

	//Global Light Direction.
	float globalLight_direction_X;
	float globalLight_direction_Y;
	float globalLight_direction_Z;

	//Global Light Diffuse color.
	float globalLight_diffuse_R;
	float globalLight_diffuse_G;
	float globalLight_diffuse_B;

	//Global Light Ambient color.
	float globalLight_ambient_R;
	float globalLight_ambient_G;
	float globalLight_ambient_B;
};

/*
 * Uniform Block for recieving Light data from the CPU.
 */
layout(std140) uniform LightsBlock
{
	//Lights Array.
	Light uLights[MAX_LIGHTS_ON_SCREEN];
	//16 floats * 4 bytes per float = 64 bytes
	//256 lights * 64 bytes = 16384 (a multiple of 16)

	//GL_MAX_UNIFORM_BLOCK_SIZE 16384
	//GL_MAX_FRAGMENT_INPUT_COMPONENTS 60
};

/*
 * Light Cell Struct.
 */
struct Cell
{
	//8, 8, 8, 4, 4										8, 8, 8, 8				8, 8, 8, 8			8, 8, 8, 8
	//[id2, id1, id0, Directional Lights, Area Lights] [id6, id5, id4, id3] [id10, id9, id8, id7] [id14, id13, id12, id11]

	//Int Vector4 representing light amounts and slot IDs for this cell.
	//I don't use an int array here because in the std140 layout
	//primative elements in an array are padded to the same size as a vector4.
	ivec4 lightID_Data;
};

/*
 * Uniform Blocks for recieving Light-Cell data from the CPU.
 */
layout(std140) uniform CellsBlock_0
{
	//Light Cell Array.
	Cell uCells_0[1020];
	//40 * 23 = 920 cells (640 x 360)
	//56 * 32 = 1792 cells (896 x 504)
	//60 * 34 = 2040 cells (960 x 540)
	//
	//4 ints * 4 bytes = 16 bytes
	//1020 cells per block * 16 bytes = 16320 (a multiple of 16)

	//GL_MAX_UNIFORM_BLOCK_SIZE 16384
	//GL_MAX_FRAGMENT_INPUT_COMPONENTS 60
};

layout(std140) uniform CellsBlock_1
{
	Cell uCells_1[1020];
};

//Cell shading lookup-table.
//const float[3] CELL_SHADE_VALUES = float[](0.0, 0.5, 1.0);
const float[4] CELL_SHADE_VALUES = float[](0.0, 0.25, 0.6, 1.0);
//const float[5] CELL_SHADE_VALUES = float[](0.0, 0.2, 0.4, 0.8, 1.0);
const int MAX_CELL_SLOT = CELL_SHADE_VALUES.length-1;
const float CELL_SHADE_ROUND_OFFSET = (1.0 / CELL_SHADE_VALUES.length);

//Output color rendered to the screen.
layout (location = 0) out vec4 output_main;
layout (location = 1) out vec4 output_color;
layout (location = 2) out vec4 output_normal;
//layout (location = 1) out vec4 output_globalDiffuse;


//SmoothStep (a linear version of it) and mix.
float stepmix(float edge0, float edge1, float epsilon, float x)
{
    float T = clamp(0.5 * (x - edge0 + epsilon) / epsilon, 0.0, 1.0);
    return mix(edge0, edge1, T);
}


//Perform lighting calculation with an area light.
vec3 calculate_Area_Light(in ivec3 fPosition, in vec3 normal, in Light aLight)
{
	//Get closest point to origin of Light.
	float closePointX = clamp(fPosition.x, aLight.position_OuterRadius.x, aLight.position_OuterRadius.x + aLight.innerCutoffCosAngle_width);
	float closePointY = clamp(fPosition.y, aLight.position_OuterRadius.y, aLight.position_OuterRadius.y + aLight.outerCutoffCosAngle_height);
	float closePointZ = clamp(fPosition.z, aLight.position_OuterRadius.z, aLight.position_OuterRadius.z + aLight.depth_length);
	
	//Get distances from pixel position to closest point.
	vec3 sides = vec3
	(
		closePointX - fPosition.x,
		closePointY - fPosition.y,
    	closePointZ - fPosition.z
	);

	//Do Pythagorean Theorum (if inside, will be zero).
	float sqrLength = (sides.x * sides.x) + (sides.y * sides.y) + (sides.z * sides.z);
	//float sqrLength = max((sideX * sideX) + (sideY * sideY) + (sideZ * sideZ), 1.0);

	//Attenuation (based solely off of distance from light source).
	//float attenuation = max(aLight.position_OuterRadius.w - sqrt(sqrLength), 0.0) / aLight.position_OuterRadius.w;
	float attenuation = (aLight.position_OuterRadius.w - sqrt(sqrLength)) / (aLight.position_OuterRadius.w + 1.0);

	//Cell the attenuation to specific values.
	//int ia = min(int((attenuation + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
	//float cellAtten = CELL_SHADE_VALUES[ia];
	

	//
	//Ambient Light
	//

	//Clamp the attenuation to 1 where the pixel is in range of the light's AOE.
	//float cellAtten = min( max(0, int(attenuation + 1.0)), 1);
	float cellAtten = max(0, int(attenuation + 1.0));

	//Store in vector.
	vec3 ambient = vec3(cellAtten);
	//vec3 ambient = vec3(attenuation);
	//vec3 ambient = vec3(0);
	//vec3 ambient = vec3(CELL_SHADE_VALUES[MAX_CELL_SLOT]);


	//
	//Diffuse light.
	//

	//sides.x = clamp(sides.x + (aLight.innerCutoffCosAngle_width * (1.0 - attenuation) * normal.x), 0, aLight.innerCutoffCosAngle_width);
	//sides.y = clamp(sides.y + (aLight.outerCutoffCosAngle_height * (1.0 - attenuation) * normal.y), 0, aLight.outerCutoffCosAngle_height);
	//sides.z = clamp(sides.z + (aLight.depth_length * (1.0 - attenuation) * normal.z), 0, aLight.depth_length);
	
	//Change the side values to calculate the diffuse dot product with. The result would look darker when over the light due to using the closest possible point.
	closePointX = clamp(closePointX + (aLight.innerCutoffCosAngle_width * (1.0 - attenuation) * normal.x), aLight.position_OuterRadius.x, aLight.position_OuterRadius.x + aLight.innerCutoffCosAngle_width);
	closePointY = clamp(closePointY + (aLight.outerCutoffCosAngle_height * (1.0 - attenuation) * normal.y), aLight.position_OuterRadius.y, aLight.position_OuterRadius.y + aLight.outerCutoffCosAngle_height);
	closePointZ = clamp(closePointZ + (aLight.depth_length * (1.0 - attenuation) * normal.z), aLight.position_OuterRadius.z, aLight.position_OuterRadius.z + aLight.depth_length);
	
	sides = vec3
	(
		closePointX - fPosition.x,
		closePointY - fPosition.y,
    	closePointZ - fPosition.z
	);
	

	//Low cap to attenuation zero.
	attenuation = max(attenuation, 0.0);
	

	//Calculate direction from pixel to light.
	//vec3 lightPos = vec3(closePointX + normal.x, closePointY + normal.y, closePointZ + normal.z);
	//vec3 lightDir = normalize(lightPos - fPosition);
	vec3 lightDir = normalize(sides + normal);
	//vec3 lightDir = normalize(sides);
	//Offset by normal in case this pixel is inside the light.
    
	//Normalize:
	//-Calculate length of vector. Requires sqrt.
	//-Divide each component by length.
	//(Pixel normal should be normalized in the normal map already.)

	//vec3 lightPos = vec3(closePointX, closePointY, closePointZ);
	//vec3 lightDir = normalize(lightPos - (fPosition - normal));


	//Diffuse Color (Dot product of pixel normal and direction to light, no less than 0).
	float diff = max(dot(normal, lightDir), 0.0) * attenuation;
	//Multiplied by attenutaion to affect the celling depending on distance to light.
	//float diff = dot(normal, lightDir) * attenuation;

	/*
	const float epsilon = 0.025;
	int id = int((diff + 0.32) * 768);
	float region = (id / 255) / 3.0;
	if(region <= 0.33){diff = 0.0;}
	else if(diff > region - epsilon && diff < region + epsilon){diff = stepmix(region, region + 0.33, epsilon, diff);}
	else{diff = region;}
	*/

	//Cell this diffuse value to specific values.
	int id = min(int((diff + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
	diff = CELL_SHADE_VALUES[id];
	//This creates a cell-shading effect.

	//Store in vector.
	vec3 diffuse = vec3(diff);


	//Apply Light Colors.
	ambient *= vec3(aLight.ambientColor_R, aLight.ambientColor_G, aLight.ambientColor_B);
	diffuse *= vec3(aLight.diffuseColor_R, aLight.diffuseColor_G, aLight.diffuseColor_B);

	//Write the final result.
	//return = vec4(vec3(attenuation), 1.0);
	//return ambient;
	//return diffuse;
	return (ambient + diffuse);
}

//Perform lighting calculation with a point/directional light.
vec3 calculate_Directional_Light(in ivec3 fPosition, in vec3 normal, in Light dLight)
{
	//Get distances from pixel position to center of light.
	vec3 sides = dLight.position_OuterRadius.xyz - fPosition;

	//Do Pythagorean Theorum (if directly on, will be zero).
	float length = sqrt( (sides.x * sides.x) + (sides.y * sides.y) + (sides.z * sides.z) );


	//
	//Cutoff angle check.
	//

	//Get direction to light.
	vec3 lightDir = sides /= length;
	//vec3 lightDir = normalize(sides + normal);

	//Calculate cosine angle from light to pixel position.
	float theta = dot(lightDir, -vec3(dLight.directionOrSide_X, dLight.directionOrSide_Y, dLight.directionOrSide_Z));
	//If theta is greater than the cosine of the outer cutoff angle, then it is inside the outer cutoff angle.

	//Determine intensity value based on how much the pixel is in the outer and inner cutoff angles.
    float epsilon = (dLight.innerCutoffCosAngle_width - dLight.outerCutoffCosAngle_height);
    float intensity = clamp((theta - dLight.outerCutoffCosAngle_height) / epsilon, 0.0, 1.0);
	//intensity = 0.0 if outside the outer cutoff angle, 1.0 if inside the inner cutoff angle.

	//float intensity = 0.0;
	//if(theta > dLight.outerCutoffCosAngle_height){intensity = 1.0;}


	//
	//Ambient Light
	//

	//Calculate attenuation based on distance from light source and calculated intensity value.
	//float attenuation = (max((dLight.position_OuterRadius.w) - length, 0.0) / dLight.position_OuterRadius.w) * intensity;
	float attenuation = min(((dLight.depth_length - length) / dLight.position_OuterRadius.w), 1.0) * intensity;

	//"Toonification" by clamping the attenuation to specfic regions.
	//int ia = min(int((attenuation + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
	//attenuation = CELL_SHADE_VALUES[ia];

	//Clamp the attenuation to 1 where the pixel is in range of the light's AOE.
	//float cellAtten = min( max(0, int(attenuation + 1.0)), 1);
	float cellAtten = max(0, int(attenuation + 0.99));

	//Low cap to zero for diffuse lighting.
	attenuation = max(attenuation, 0.0);

	//Store in vector.
	vec3 ambient = vec3(cellAtten);
	//vec3 ambient = vec3(attenuation);
	//vec3 ambient = vec3(intensity);
	//vec3 ambient = vec3(0);
	//vec3 ambient = vec3(CELL_SHADE_VALUES[MAX_CELL_SLOT]);


	//
	//Diffuse Light.
	//

	//Direction to light was already calculated.
	//Diffuse Color (Dot product of pixel normal and direction to light, no less than 0).
    float diff = max(dot(normal, lightDir), 0.0) * attenuation;
	//Multiplied by attenutaion to affect the celling depending on intensity and distance to light.

	//float diff = max(dot(normal, lightDir), 0.0) * intensity;

	//Cell the diffuse value for cell-shading effect.
	int id = min(int((diff + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
	diff = CELL_SHADE_VALUES[id];
	//diff = CELL_SHADE_VALUES[MAX_CELL_SLOT];

	//Store in vector.
	vec3 diffuse = vec3(diff);
	//vec3 diffuse = vec3(attenuation);


	//
	//Final result.
	//

	//Apply Light Colors.
	ambient *= vec3(dLight.ambientColor_R, dLight.ambientColor_G, dLight.ambientColor_B);
	diffuse *= vec3(dLight.diffuseColor_R, dLight.diffuseColor_G, dLight.diffuseColor_B);

	//Return the final result.
	//return ambient;
	//return diffuse;
	return (ambient + diffuse);
}


vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = c.g < c.b ? vec4(c.bg, K.wz) : vec4(c.gb, K.xy);
    vec4 q = c.r < p.x ? vec4(p.xyw, c.r) : vec4(c.r, p.yzx);

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}


void main()
{
    //Get texture color.
	int texSlot = int(frag_TexSlot);
	vec4 texColor = texture(uTextures[texSlot], frag_TexCoords.xy) * frag_Color;

	//Discard completely transperant pixels so they don't fill the depth_length buffer.
	if(texColor.a <= 0.0){discard;}

	//Get texture normal and Specular Intensity (more opaque means not as shiny. A "Crystal"-like corralation).
	vec4 tNormSpec = texture(uTextures[texSlot], frag_TexCoords.zw);
	vec3 texNormal = (tNormSpec.rgb * 2.0) - 1.0;
	float texSpecularIntensity = //1.0 - tNormSpec.a;
	0.0;
	//1.0;
	//
	//texNormal.y *= -1.0;
	//vec3 texNormal = vec3(0.0, 1.0, 0.0f);
	//vec3 texNormal = vec3(0.0, frag_TexCoords.z, frag_TexNormalCoords.w);

	//Check which cell this pixel is in.
	int cellX = int(gl_FragCoord.x / CELL_SIZE);
	int cellY = int((uCurrent_scrHeight - gl_FragCoord.y) / CELL_SIZE);
	//int cellY = int((gl_FragCoord.y - (gl_FragCoord.z / 2.0)) / CELL_SIZE);
	int cellNum = cellX + (cellY * SCR_WIDTH_CELLS);//int
	//
	Cell cell = (cellNum >= uCells_0.length) ? uCells_1[cellNum - uCells_0.length] : uCells_0[cellNum];

	ivec3 fPosition = ivec3(frag_Position);
	//vec3 fPosition = frag_Position;

	//How many of each type of light are in this cell?
	int area_Lights = cell.lightID_Data[0] & 0x0000000F;
	int directional_Lights = (cell.lightID_Data[0] & 0x00000F0) >> 4;

	int int_portion = 1;

	//float loops = 0.0;

	vec3 result_globAm = vec3(0.0);

	//SunLight has: Diffuse, Ambient, and Direction normal (-1.0 to  1.0)
	{
		//Get global light ambient color.
		vec3 globalLight_ambient = vec3(globalLight_ambient_R, globalLight_ambient_G, globalLight_ambient_B);

		//Perform ambient color correction.
		//float average = (texColor.r + texColor.g + texColor.b) / 3.0;
		float average = (globalLight_ambient.r + globalLight_ambient.g + globalLight_ambient.b) / 3.0;

		//Convert to Hue, Saturation, and Value.
		vec3 hsv = rgb2hsv(texColor.rgb);
		hsv.g = min(hsv.g + (hsv.g / average), 1.0);//Affect saturation.
		//* 1.5f;

		//Convert back to RGB.
		vec3 hsvRGB = hsv2rgb(hsv);

		//Add ambient color to result.
		result_globAm = (globalLight_ambient * hsvRGB);
		//result.rgb += hsvRGB;
		//result_globAm += (globalLight_ambient * texColor.rgb);

		//We'll apply diffuse lighting in a different shader (or at the end here if uDoingTransperant).

		//int ir = min(int(((1.0f - abs(texNormal.x)) + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
		//int ig = min(int(((1.0f - abs(texNormal.y)) + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
		//result_globAm.x +=  CELL_SHADE_VALUES[ir] * 0.25f;
		//result_globAm.y +=  CELL_SHADE_VALUES[ig] * 0.25f;
	}

	//This will consist of all the combined light colors of this pixel.
	vec3 result_lightColors = vec3(texColor.rgb) * frag_Emission;
	//vec3 result_lightColors = vec3(0.0);

	//Loop through area lights for cell.
	for(int i = 0; i < area_Lights; i++)
	{
		//Get ID and light associated with ID.
		int lightID = (cell.lightID_Data[(i+1) / 4] & (0x000000FF << (8*int_portion))) >> (8 * int_portion);
		Light aLight = uLights[lightID];

		//Increment cell portion.
		int_portion = (int_portion+1) % 4;
		//loops = lightID / 4.0;

		//Used sides check.
		vec3 sides = vec3(aLight.directionOrSide_X, aLight.directionOrSide_Y, aLight.directionOrSide_Z);
		//
		//-1: can be to the left to check
		//0: can be either
		//1: can be to the right to check
		//Set radius to 0 to not have an outer radius (...I'm such an IDIOT).
		//
		if((sides.x == -1 && (fPosition.x >= aLight.position_OuterRadius.x + aLight.innerCutoffCosAngle_width))
		|| (sides.x == 1 && (fPosition.x <= aLight.position_OuterRadius.x))
		//
		|| (sides.y == -1 && (fPosition.y >= aLight.position_OuterRadius.y + aLight.outerCutoffCosAngle_height))
		|| (sides.y == 1 && (fPosition.y <= aLight.position_OuterRadius.y))
		//
		|| (sides.z == -1 && (fPosition.z >= aLight.position_OuterRadius.z + aLight.depth_length))
		|| (sides.z == 1 && (fPosition.z <= aLight.position_OuterRadius.z)))
		{continue;}

		//Perform light calculation and add the result to only the rgb portion of the result color.
		result_lightColors += calculate_Area_Light(fPosition, texNormal, aLight);
		//result = texColor * vec4(aLight.diffuseColor_R, aLight.diffuseColor_G, aLight.diffuseColor_B, 1.0);

		/*
		if
		(!(
			(sides.x == -1 && (frag_Position.x > aLight.position_OuterRadius.x + aLight.innerCutoffCosAngle_width))
			|| (sides.x == 1 && (frag_Position.x < aLight.position_OuterRadius.x))
			//
			|| (sides.y == -1 && (frag_Position.y > aLight.position_OuterRadius.y + aLight.outerCutoffCosAngle_height))
			|| (sides.y == 1 && (frag_Position.y < aLight.position_OuterRadius.y))
			//
			|| (sides.z == -1 && (frag_Position.z > aLight.position_OuterRadius.z + aLight.depth_length))
			|| (sides.z == 1 && (frag_Position.z < aLight.position_OuterRadius.z))
		))
		{
			//Perform light calculation and add the result to only the rgb portion of the result color.
			result_lightColors += calculate_Area_Light(texColor, texNormal, aLight);
			//result = texColor * vec4(aLight.diffuseColor_R, aLight.diffuseColor_G, aLight.diffuseColor_B, 1.0);
		}
		*/
	}

	//Loop through directional lights for cell.
	for(int i = 0; i < directional_Lights; i++)
	{
		//int lightID = (cell.lightIDs[i/2] & (0x0000FFFF << (16*zero_one))) >> (16 * zero_one);
		int lightID = (cell.lightID_Data[(i+area_Lights+1) / 4] & (0x000000FF << (8*int_portion))) >> (8 * int_portion);
		//int lightID = (cell.lightID_Data[(i+area_Lights+1) / 4] & (0x000000FF << (4*int_portion))) >> (4 * int_portion);
		Light dLight = uLights[lightID];
		//
		int_portion = (int_portion+1) % 4;
		result_lightColors += calculate_Directional_Light(fPosition, texNormal, dLight);
	}

	//gl_FragCoord.x *= 2;
	//Send the output to be rendered.

	//output_main = vec4(cellX / float(SCR_WIDTH / CELL_SIZE), cellY / float(SCR_HEIGHT / CELL_SIZE), 0.0, 1.0);
	//output_main = vec4(frag_Position, 1.0);
	//output_main = texColor;
	//output_main = vec4(int_portion / 4.0, 0.0, 0.0, 1.0);
	//output_main = vec4(loops, 0.0, 0.0, 1.0);
	//
	//if(gl_FrontFacing){output_main = vec4(0.0, 1.0, 0.0, 1.0);}

	//Output texture color to color buffer. Diffuse if doing transperant batches.
	if(uDoingTransperant == 1)
	{
		//
		//Calculate Diffuse Lighting now.
		//
		vec3 globalLight_diffuse = vec3(globalLight_diffuse_R, globalLight_diffuse_G, globalLight_diffuse_B);
		vec3 globalLight_direction = vec3(globalLight_direction_X, globalLight_direction_Y, globalLight_direction_Z);

		//By how much is this pixel facing the light?
		//(Dot product of pixel normal and direction to light, no less than 0).
		float diff = max(dot(texNormal, -globalLight_direction), 0.0);

		//Cell it.
		int id = min(int((diff + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
		diff = CELL_SHADE_VALUES[id];

		//Apply Light Color.
		vec3 diffuse = vec3(diff * globalLight_diffuse);


		//
		//Calculate Specular Lighting now.
		//
		vec3 viewDir = vec3(0.0, 0.0, -1.0);//<- Camera facing straight down internally.
		vec3 reflectDir = reflect(-globalLight_direction, texNormal);
		float spec = max(dot(viewDir, reflectDir), 0.0);
		spec *= spec;

		//Cell it.
		id = int((spec) * 512);
		spec = float((id / 255) / 2.0);

		//Diffuse color tint.
		vec3 specular = (texSpecularIntensity * spec) *  globalLight_diffuse;


		//Output diffuse color to color buffer.
		output_color = vec4(((diffuse + result_lightColors) * texColor.rgb) + specular + result_globAm, texColor.a);

		//Output ambient color to normal and main buffers.
		vec4 resAm = vec4( (result_lightColors * texColor.rgb) + result_globAm, texColor.a);
		output_normal = resAm;
		output_main = resAm;
	}
	else
	{
		//Output texture color to color buffer.
		output_color = texColor;

		//Output texture normal to normal buffer.
		output_normal = tNormSpec;

		//Output result to main buffer.
		output_main = vec4( (result_lightColors * texColor.rgb) + result_globAm, texColor.a);

		//output_main = result;
		//output_main = vec4(mix(vec2(0.0, 1.0), vec2(1.0, 0.0), texSlot / 2.0), 0.0, 1.0);
		//output_main = vec4(globalLight_diffuse, 1.0);
		//output_main = output_globalDiffuse;
		/*
		output_main = vec4
		(
			(1.0f - abs(texNormal.x)) * 0.25f,
			(1.0f - abs(texNormal.y)) * 0.25f,
			0.0f, 1.0f
		);
		*/
	}
}
