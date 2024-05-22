#version 330 core//<- Says this shader is compatable with OpenGL 3.3 (core profile)

//Vertex Attributes.
layout(location = 0) in vec2 attrib_Position;
layout(location = 1) in vec2 attrib_TexCoords;

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

//Fragment Values.
out vec2 frag_TexCoords;

void main()
{
    //gl_Position = uView * vec4(attrib_Position.x, attrib_Position.y, 0, 1.0);
	gl_Position = vec4(attrib_Position.x, attrib_Position.y, 0, 1.0);

    //Send all the atrribute data directly to the fragment shader (including position for lighting reasons).
    frag_TexCoords = attrib_TexCoords;
}
