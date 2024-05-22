#version 330 core//<- Says this shader is compatable with OpenGL 3.3 (core profile)

//Vertex Attributes.
layout(location = 0) in vec3 attrib_Position;
layout(location = 1) in vec2 attrib_TexCoords;
layout(location = 2) in float attrib_TexSlot;

//Data shared with all shaders.
layout(std140) uniform GlobalBlock
{
	//View Matrix.
	mat4 uView;
};

//Fragment Values.
out vec2 frag_TexCoords;
flat out float frag_TexSlot;

void main()
{
	//Set gl_position for fragment shader to use.
    //gl_Position = uView * vec4(attrib_Position.x, attrib_Position.y - ((attrib_Position.z) / 2.0), attrib_Position.z, 1.0);
	gl_Position = uView * vec4(attrib_Position.xyz, 1.0);
    gl_Position.z += 0.005;

    //Send all the atrribute data directly to the fragment shader (including position for lighting reasons).
    frag_TexCoords = attrib_TexCoords;
    frag_TexSlot = attrib_TexSlot;
}
