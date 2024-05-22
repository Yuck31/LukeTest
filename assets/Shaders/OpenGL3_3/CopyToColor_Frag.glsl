#version 330 core

//Input texCoords.
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

//FrameBuffer Texture.
uniform sampler2D outputBuffer;

//Output color.
layout (location = 1) out vec4 output_color;

void main()
{
    output_color = texture(outputBuffer, frag_TexCoords);
}
