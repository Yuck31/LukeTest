#version 330 core

//Vertex Attributes.
layout (location = 0) in vec3 attrib_Position;
layout (location = 1) in vec3 attrib_RGB;
layout (location = 2) in float attrib_Alpha;
layout (location = 3) in vec4 attrib_TexCoords;
layout (location = 4) in float attrib_TexSlot;
layout (location = 5) in float attrib_Emission;
//attrib_Normal is not needed. They will be converted offline.

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
};
uniform vec3 uOffsets;

//Fragment Values.
out vec3 frag_Position;
out vec4 frag_Color;
out vec4 frag_TexCoords;
flat out float frag_TexSlot;//Needs to be flat otherwise this value will be interpolated.
out float frag_Emission;
//out vec3 frag_Normal;

void main()
{
    //Set screen-space coordinate.
    //gl_Position = uView * vec4(attrib_Position.x, attrib_Position.y - ((attrib_Position.z) / 2.0), attrib_Position.z, 1.0);
    //gl_Position = uView * vec4(attrib_Position.x, attrib_Position.y - (attrib_Position.z / 2.0), attrib_Position.z, 1.0);
	gl_Position = uView * vec4(attrib_Position.xyz, 1.0);

	//invert z: x, y + (z/2), z - (z-originZ * 2)

    //Offset position by screen offsets.
    frag_Position = attrib_Position - uOffsets;
    
    //Send the rest directly to the fragment shader.
    //frag_Color = attrib_Color;
	//frag_Color = vec4(attrib_Alpha, 0.0, 0.0, 1.0);
	frag_Color = vec4(attrib_RGB, attrib_Alpha);
    frag_TexCoords = attrib_TexCoords;
    frag_TexSlot = attrib_TexSlot;
	frag_Emission = attrib_Emission;
    //frag_Normal = attrib_Normal;
}
