#version 330 core//<- Says this shader is compatable with OpenGL 3.3 (core profile)

//Vertex Attributes.
layout(location = 0) in vec3 attrib_Position;
layout(location = 1) in vec4 attrib_Color;
layout(location = 2) in vec2 attrib_TexCoords;
layout(location = 3) in float attrib_TexSlot;

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
out vec3 frag_Position;
out vec4 frag_Color;
out vec2 frag_TexCoords;
flat out float frag_TexSlot;

void main()
{
    //vec3 pos = attrib_Position - cameraOffset;
    //gl_Position = vec4(pos, 1.0);

    //gl_Position = vec4(attrib_Position, 1.0);
    //gl_Position = uView * vec4(attrib_Position.xy, 0.0, 1.0);
    gl_Position = uView * vec4(attrib_Position.xyz, 1.0);
    //gl_Position = uView * vec4(attrib_Position.x, attrib_Position.y - ((attrib_Position.z) / 2.0), attrib_Position.z, 1.0);

    //Send all the atrribute data directly to the fragment shader (including position for lighting reasons).
    frag_Position = attrib_Position;
    frag_Color = attrib_Color;
    frag_TexCoords = attrib_TexCoords;
    frag_TexSlot = attrib_TexSlot;
}
