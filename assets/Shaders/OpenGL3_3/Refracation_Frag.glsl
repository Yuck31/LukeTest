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

//Currently used textures per draw call.
uniform sampler2D uTextures[8];

//Currently used Screen Height.
uniform int uCurrent_scrHeight;

//TODO Lights

//Cell shading lookup-table.
const float[5] CELL_SHADE_VALUES = float[](0.0, 0.2, 0.4, 0.8, 1.0);
const int MAX_CELL_SLOT = CELL_SHADE_VALUES.length-1;
const float CELL_SHADE_ROUND_OFFSET = (1.0 / CELL_SHADE_VALUES.length);

//Output color rendered to the screen.
layout (location = 0) out vec4 output_main;
layout (location = 1) out vec4 output_color;
layout (location = 2) out vec4 output_normal;


void main()
{

}
