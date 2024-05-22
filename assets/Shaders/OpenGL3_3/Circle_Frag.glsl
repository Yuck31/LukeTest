#version 330 core

//Size and Color sent from Vertex Shader.
in vec4 frag_Color;
in vec2 frag_QuadCoords;
//in vec3 frag_QuadDims;
in float frag_Thickness;

//Output color rendered to the screen.
layout (location = 0) out vec4 outputColor;

void main()
{
    //Get current "texture" postion on the tri.
    //vec2 uv = ((frag_QuadCoords * frag_QuadDims.xy) * 2.0) - 1.0;
    vec2 uv = (frag_QuadCoords * 2.0) - 1.0;

    //Get distance from center to uv.
    float distance = ((uv.x * uv.x) + (uv.y * uv.y));

    //Only put a pixel on screen if distance is inside the circle's thickness.
    if(distance > 1.0 || distance < frag_Thickness * frag_Thickness){discard;}
    //if(distance > 1.0){discard;}

    //outputColor = vec4(frag_QuadCoords.x, frag_QuadCoords.y, 0.0, 1.0);
    //outputColor = vec4(uv.x, uv.y, 0.0, 1.0);
    outputColor = frag_Color;
}
