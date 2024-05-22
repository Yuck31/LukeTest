#version 330 core//<- Says this shader is compatable with OpenGL 3.3 (core profile)

//Vertex Attributes.
layout(location = 0) in vec3 attrib_Position;

//Data shared with all shaders.
layout(std140) uniform GlobalBlock
{
	//View Matrix.
	mat4 uView;
};

void main()
{
    //Calculate render position.
    //gl_Position = uView * vec4(attrib_Position.x, attrib_Position.y - ((attrib_Position.z) / 2.0), attrib_Position.z, 1.0);
    gl_Position = uView * vec4(attrib_Position.xyz, 1.0);
}
