#version 330 core

//Color from Vertex Shader.
//in vec3 frag_Position;
in vec4 frag_Color;

//Color output to the screen.
layout (location = 0) out vec4 outputColor;

void main()
{
	outputColor = frag_Color;
	//outputColor = vec4(1.0, 0.0, 0.0, 1.0);
}
