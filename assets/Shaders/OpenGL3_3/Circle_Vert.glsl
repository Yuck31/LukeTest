#version 330 core

//Vertex Attributes.
layout(location = 0) in vec3 attrib_Position;
layout(location = 1) in vec4 attrib_Color;
layout(location = 2) in vec2 attrib_QuadCoords;
//layout(location = 3) in vec3 attrib_QuadDims;
layout(location = 3) in float attrib_Thickness;

//Data shared with all shaders.
layout(std140) uniform GlobalBlock
{
	//View Matrix.
	mat4 uView;
};

//Fragment Values.
out vec4 frag_Color;
out vec2 frag_QuadCoords;
//out vec3 frag_QuadDims;
out float frag_Thickness;

void main()
{
    //Set position to start rendering to the screen with.
    //gl_Position = uView * vec4(attrib_Position.x, attrib_Position.y - ((attrib_Position.z) / 2.0), attrib_Position.z, 1.0);
    gl_Position = uView * vec4(attrib_Position.xyz, 1.0);

    //Send all the atrribute data directly to the fragment shader.
    frag_Color = attrib_Color;
    frag_QuadCoords = attrib_QuadCoords;
    //frag_QuadDims = attrib_QuadDims;
    frag_Thickness = attrib_Thickness;
}
