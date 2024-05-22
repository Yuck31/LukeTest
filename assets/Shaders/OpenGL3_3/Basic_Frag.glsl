#version 330 core

//Input values from the vertex shader.
in vec3 frag_Position;
//in vec4 frag_Crop;
in vec4 frag_Color;
in vec2 frag_TexCoords;
flat in float frag_TexSlot;

//Currently used textures.
uniform sampler2D uTextures[8];

//Output color rendered to the screen.
layout (location = 0) out vec4 outputColor;

void main()
{
	//if(texture(uTextures[int(frag_TexID)], frag_TexCoords).w == 0.0)
	//{outputColor = vec4(0.5, 0.0, 0.0, 1.0);}

	//if(gl_FragCoord.x < frag_Crop.x || gl_FragCoord.x > frag_Crop.z
	//|| gl_FragCoord.y < frag_Crop.y || gl_FragCoord.y > frag_Crop.w)
	//{
		outputColor = frag_Color * texture(uTextures[int(frag_TexSlot)], frag_TexCoords);

		//int d0 = int(gl_FragCoord.z * (255)) % 64;
		//float d1 = float(d0) / 64.0;
		//outputColor = vec4(vec3(d1), 1.0);
		//outputColor = frag_Color * texture(uTextures[int(frag_TexID)], frag_TexCoords) * vec4(vec3(d1), 1.0);
		//outputColor = vec4(vec3(gl_FragCoord.z), 1.0);
		//if(texture(uTextures[int(frag_TexID)], frag_TexCoords).a < 0.01){discard;}

		//int y0 = int(frag_Position.y * (255)) % 64;
		//float y1 = float(y0) / 64.0;
		//outputColor = vec4(0.0, y1, 0.0, 1.0);

		//outputColor = vec4(vec3(gl_FragCoord.z), 1.0);
		//outputColor = texture(uTextures[int(frag_TexID)], frag_TexCoords);
		//outputColor = frag_Color * texture(uTextures[int(frag_TexID)], frag_TexCoords) + vec4((frag_TexID / 8.0), 0.0, 0.0, 1.0);
		//outputColor = vec4(frag_Position, 1.0);
		//outputColor = frag_Color;

		//outputColor = vec4(frag_TexCoords, 0.0, 1.0);
		//int t0 = int(frag_TexCoords.x * (255)) % 64;
		//float t1 = float(t0) / 64.0;
		//outputColor = vec4(t1, frag_TexCoords.y, 0.0, 1.0);

		//If clear, skip.
		if(outputColor.a < 0.01){discard;}
	//}
}
