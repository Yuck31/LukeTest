#version 330 core

//Output color rendered to the screen... oh wait.\/
layout(location = 0) out vec4 output;

//We don't need to do anything here since the only thing that matters is the depth and stencil tests.
void main()
{
	//output = vec4(1.0, 0.0, 0.0, 1.0);

	//if(gl_FrontFacing){output = vec4(0.0, 1.0, 0.0, 1.0);}
	//else{output = vec4(1.0, 0.0, 0.0, 1.0);}
}
