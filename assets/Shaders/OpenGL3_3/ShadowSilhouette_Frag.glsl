#version 330 core

//Input values from the vertex shader.
in vec2 frag_TexCoords;
flat in float frag_TexSlot;

//Currently used textures.
uniform sampler2D uTextures[8];

//Output color rendered to the screen.
layout(location = 0) out vec4 output;

void main()
{
	//If clear, skip.
	if(texture(uTextures[int(frag_TexSlot)], frag_TexCoords).a < 0.1){discard;}

	//If opaque, skip.
	//if(texture(uTextures[int(frag_TexSlot)], frag_TexCoords).a > 0.1){discard;}
	//Clear pixels will set the stencil value at this position to 0.

    //Set output color.
	//output = vec4(1.0, 1.0, 1.0, 1.0);
	//output = texture(uTextures[int(frag_TexSlot)], frag_TexCoords);
	//output = vec4(frag_TexCoords * 5, 0.0, 1.0);
}
