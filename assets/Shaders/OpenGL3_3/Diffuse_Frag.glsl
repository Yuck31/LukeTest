#version 330 core

//Input values from the vertex shader.
in vec2 frag_TexCoords;

//Data shared with all shaders.
layout(std140) uniform GlobalBlock
{
	//View Matrix.
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

	//Inverse projection Matrix.
	mat4 uInvProj;
};

//Currently used textures.
uniform sampler2D uColor;
uniform sampler2D uNormal;

//const float[3] CELL_SHADE_VALUES = float[](0.0, 0.5, 1.0);
const float[4] CELL_SHADE_VALUES = float[](0.0, 0.25, 0.6, 1.0);
//const float[5] CELL_SHADE_VALUES = float[](0.0, 0.2, 0.4, 0.8, 1.0);
const int MAX_CELL_SLOT = CELL_SHADE_VALUES.length-1;
const float CELL_SHADE_ROUND_OFFSET = (1.0 / CELL_SHADE_VALUES.length);

//Output color rendered to the Framebuffer.
layout(location = 0) out vec4 output;

void main()
{
	//Get current color.
	vec4 currentColor = texture(uColor, frag_TexCoords);

	//Get current normal and specular intensity.
	vec4 tNormSpec = texture(uNormal, frag_TexCoords);
	vec3 texNormal = (tNormSpec.rgb * 2.0) - 1.0;
	float texSpecularIntensity = //1.0 - tNormSpec.a;
	0.0;

	//Gawd diamet std140...
	vec3 globalLight_direction = vec3(globalLight_direction_X, globalLight_direction_Y, globalLight_direction_Z);
	vec3 globalLight_diffuse = vec3(globalLight_diffuse_R, globalLight_diffuse_G, globalLight_diffuse_B);
	//vec3 globalLight_ambient = vec3(globalLight_ambient_R, globalLight_ambient_G, globalLight_ambient_B);


	//
	//Diffuse Lighting.
	//
	//By how much is this pixel facing the light?
	//(Dot product of pixel normal and direction to light, no less than 0).
	float diff = max(dot(texNormal, -globalLight_direction), 0.0);

	//Cell it.
	int id = min(int((diff + CELL_SHADE_ROUND_OFFSET) * MAX_CELL_SLOT), MAX_CELL_SLOT);
	diff = CELL_SHADE_VALUES[id];

	//Apply Light Color.
	vec3 diffuse = vec3(diff * globalLight_diffuse);


	//
	//Specular Lighting.
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

	//int ii = int(4.0);

	//Apply to output.
	output = vec4(((diffuse * currentColor.rgb) + specular), 0.0);
	//output = vec4((diffuse + specular), currentColor.a);
	//output = vec4(vec3(diff), 0.0);
	//output = vec4(vec3(id / 5.0), 1.0);
	//output = tNormSpec;
}
