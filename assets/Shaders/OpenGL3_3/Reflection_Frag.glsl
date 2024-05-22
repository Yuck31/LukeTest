#version 330 core

//Input values from the vertex shader.
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

//Currently used textures.
uniform sampler2D uScreen;
uniform sampler2D uDepthBuffer;
uniform sampler2D uNormal;

//const vec3 zUp_Back = vec3(0.0, 0.707, -0.707);
const float maxDistance = 256.0;
const float leeway = 0.0020;
//0.02;

//Output reflection color rendered to reflection buffer.
layout(location = 0) out vec4 output;

void main()
{
	//Get current color.
	//vec4 currentColor = texture(uScreen, frag_TexCoords);

    //Discard completely transperant pixels.
	//if(texColor.a <= 0.0){discard;}

    
    //Get view space coordinate of the current pixel.
    vec2 texSize = textureSize(uScreen, 0).xy;
    vec3 fragPosition = vec3(frag_TexCoords, texture(uDepthBuffer, frag_TexCoords).r);
    //Z position is retrived from the depth buffer.

    //Start and end points of ray.
    vec3 rayStart = vec3(fragPosition.x, fragPosition.y, fragPosition.z);
    //vec3 rayEnd = vec3(rayStart.x, rayStart.y + (maxDistance / texSize.y), rayStart.z - ((maxDistance * 2) / 1056.0));
    vec3 rayEnd = vec3(rayStart.x, rayStart.y + ((maxDistance) / texSize.y), rayStart.z - ((maxDistance) / 1056.0));
    //vec3 rayEnd = vec3(rayStart.x + (maxDistance / texSize.x), rayStart.y, rayStart.z - ((maxDistance) / 1056.0));

    vec3 lastPosition = rayStart;

    for(int i = 0; i < maxDistance; i++)
    {
        //How far along the ray?
        float amount = i / maxDistance;

        //Lerp to calculate our current position.
        vec3 currentPosition = (rayStart * (1.0 - amount)) + (rayEnd * amount);

        //Get the z-position stored in the depth buffer.
        float depthInBuffer = texture(uDepthBuffer, currentPosition.xy).r;

        //Manual depth test.
        float depth = currentPosition.z - depthInBuffer;
        if(depth > 0)// && depth < leeway)
        {
            if(depth < leeway)
            {
                //output = vec4(vec3(depthInBuffer), 1.0);
                //output = vec4(currentPosition, 1.0);
                output = vec4
                (
                    //texture(uScreen, vec2(currentPosition.x + ((int(currentPosition.y * 360) % 4) / 360.0), currentPosition.y)).xyz *
                    texture(uScreen, currentPosition.xy).xyz,// *
                    //(vec3(globalLight_ambient_R, globalLight_ambient_G, globalLight_ambient_B) +
                    //vec3(globalLight_diffuse_R, globalLight_diffuse_G, globalLight_diffuse_B)),
                    1.0
                    //0.5
                );
                //output = vec4(globalLight_ambient_R, globalLight_ambient_G, globalLight_ambient_B, 1.0);

                return;
            }
            /*
            else if(depthInBuffer - lastPosition.z < -leeway)// && depthInBuffer - lastPosition.z > -leeway)
            {
                float depthToLeft = texture(uDepthBuffer, vec2(lastPosition.x - (1.0 / texSize.x), currentPosition.y)).r;

                if(depthToLeft - depthInBuffer > 0)
                {
                    output = texture(uScreen, vec2(lastPosition.x - (1.0 / texSize.x), currentPosition.y));
                    //output = texture(uScreen, lastPosition.xy);
                    break;
                }

                float depthToRight = texture(uDepthBuffer, vec2(lastPosition.x + (1.0 / texSize.x), currentPosition.y)).r;

                if(depthToRight - depthInBuffer > 0)
                {
                    output = texture(uScreen, vec2(lastPosition.x + (1.0 / texSize.x), currentPosition.y));
                    //output = texture(uScreen, lastPosition.xy);
                    break;
                }
            }
            */
            /*
            else if(depth > leeway * 2 && depth < leeway * 4)
            {
                output = vec4(globalLight_ambient_R, globalLight_ambient_G, globalLight_ambient_B, 0.5);
                return;
            }
            */
        }

        lastPosition = vec3(currentPosition.xy, depthInBuffer);
    }

    //output = vec4(globalLight_ambient_R, globalLight_ambient_G, globalLight_ambient_B, 0.5);
    
    
    /*
    //Get current xy normal.
	vec2 currentNormal = (texture(uNormal, frag_TexCoords).rg * 2.0) - 1.0;

    //New distorted coordinates to sample from.
    vec2 newTexCoord = min(max(frag_TexCoords + ((currentNormal * 4.0) / textureSize(uScreen, 0).xy), 0.0), 1.0);

    output = texture(uScreen, newTexCoord);
    */
}
