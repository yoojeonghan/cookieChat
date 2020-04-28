#include <jni.h>
#include <GLES/gl.h>
#include <GLES/glext.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES2/gl2platform.h>

#define  LOG_TAG    "libJNIExInterface"  
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)  
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

GLuint gProgram;
GLuint gvPositionHandle;

GLfloat mUvBuffer;
GLuint	g_textureName;

int		g_nX = 0;
int		g_nY = 0;
int		g_nPandaWidth;
int		g_nPandaHeight;

GLuint mMVPMatrix[] = {};
GLshort mDrawOrder[] = { 0,3,2,0,2,1 };

GLfloat PosX;
GLfloat PosY;

static const char gVertexShader[] =
"attribute vec4 a_position;   \n"
"attribute vec2 a_texCoord;   \n"
"varying vec2 v_texCoord;     \n"
"void main()                  \n"
"{                            \n"
"   gl_Position = a_position; \n"
"   v_texCoord = a_texCoord;  \n"
"}                            \n";

static const char gFragmentShader[] =
"precision mediump float;                            \n"
"varying vec2 v_texCoord;                            \n"
"uniform sampler2D s_texture;                        \n"
"void main()                                         \n"
"{                                                   \n"
"  gl_FragColor = texture2D( s_texture, v_texCoord );\n"
"}   \n";

void setTextureData(char *data, int width, int height)
{
	int i;
	char *buf;

	buf = (char *)malloc((sizeof(char)*width*height) << 2);

	for (i = 0; i < width*height * 4; i += 4)
	{
		buf[i] = data[i + 2];
		buf[i + 1] = data[i + 1];
		buf[i + 2] = data[i];
		buf[i + 3] = data[i + 3];
	}

	g_nPandaWidth = (int)width;
	g_nPandaHeight = (int)height;

	glGenTextures(1, &g_textureName);
	glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

	glBindTexture(GL_TEXTURE_2D, g_textureName);

	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (void*)buf);

	free(buf);
}

GLuint loadShader(GLenum shaderType, const char* pSource)
{
	GLuint shader = glCreateShader(shaderType);
	if (shader)
	{
		glShaderSource(shader, 1, &pSource, NULL);
		glCompileShader(shader);
		GLint compiled = 0;
		glGetShaderiv(shader, 0x8B81, &compiled);
		if (!compiled)
		{
			GLint infoLen = 0;
			glGetShaderiv(shader, 0x8B84, &infoLen);
			if (infoLen)
			{
				char* buf = (char*)malloc(infoLen);
				if (buf)
				{
					glGetShaderInfoLog(shader, infoLen, NULL, buf);
					//LOGE("Could not compile shader %d:\n%s\n",
					//	shaderType, buf);
					free(buf);
				}
				glDeleteShader(shader);
				shader = 0;
			}
		}
	}

	return shader;
}

GLuint createProgram(const char* pVertexSource, const char* pFragmentSource)
{
	GLuint program = glCreateProgram();
	GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
	if (!vertexShader)
	{
		return 0;
	}

	GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
	if (!pixelShader)
	{
		return 0;
	}

	if (program)
	{
		glAttachShader(program, vertexShader);
		glAttachShader(program, pixelShader);
		glLinkProgram(program);

		GLint linkStatus = GL_FALSE;
		glGetProgramiv(program, 0x8B82, &linkStatus);

		if (linkStatus != GL_TRUE)
		{
			GLint bufLength = 0;
			glGetProgramiv(program, 0x8B84, &bufLength);
			
			/*
			if (bufLength)
			{
				char* buf = (char*)malloc(bufLength);
				if (buf)
				{
					glGetProgramInfoLog(program, bufLength, NULL, buf);
					//LOGE("Could not link program:\n%s\n", buf);
					free(buf);
				}
			}
			glDeleteProgram(program);
			program = 0;
			*/
		}
		
	}

	return program;
}

void Java_com_example_ehfcn_cookiechat_library_GL2JNILib_nativeCreated(JNIEnv*  env)
{
	gProgram = createProgram(gVertexShader, gFragmentShader);
	gvPositionHandle = glGetAttribLocation(gProgram, "a_position");
	glClearColor(0.4f, 0.4f, 0.4f, 0.4f);
}

void Java_com_example_ehfcn_cookiechat_library_GL2JNILib_nativeChanged(JNIEnv* env, jobject thiz, jint w, jint h)
{
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
}

void Java_com_example_ehfcn_cookiechat_library_GL2JNILib_nativeUpdateGame(JNIEnv* env, jobject thiz, jfloatArray arr, jint camX, jint posY)
{
	jfloatArray result;
	result = (*env)->GetFloatArrayElements(env, arr, 0);
	*mMVPMatrix = (GLuint)result;

	PosX = (GLfloat)camX/100;
	PosY = (GLfloat)posY/100;

	g_nPandaWidth = 1;
	g_nPandaHeight = 1;

	GLfloat LeftPos = -0.2f + PosX;
	GLfloat TopPos = -0.12f + PosY;
	GLfloat RightPos = 0.2f + PosX;
	GLfloat BottomPos = 0.12f + PosY;

	GLfloat vertices[12] =
	{
		LeftPos   ,	BottomPos	,	0.0f,	// LEFT  | BOTTOM
		RightPos  ,	BottomPos	,	0.0f,	// RIGHT | BOTTOM
		LeftPos	  ,	TopPos	,	0.0f,	// LEFT  | TOP
		RightPos  ,	TopPos	,	0.0f	// RIGHT | TOP
	};

	GLfloat texture[] =
	{
		1	, 0, // 4
		0	, 0, // 3
		1	, 1, // 2
		0	, 1  // 1
	};

	glUseProgram(gProgram);
	glEnable(GL_TEXTURE_2D);
	glEnableClientState(GL_VERTEX_ARRAY);

	glGetAttribLocation(gProgram, "vPosition");
	glEnableVertexAttribArray(gvPositionHandle);
	glVertexAttribPointer(gvPositionHandle, 3, GL_FLOAT, GL_FALSE, 0, vertices);

	glEnableClientState(GL_TEXTURE_COORD_ARRAY);
	glGetAttribLocation(gProgram, "a_texCoord");
	glEnableVertexAttribArray(g_textureName);
	glVertexAttribPointer(g_textureName, 2, GL_FLOAT, GL_FALSE, 0, texture);
	glTexCoordPointer(2, GL_FLOAT, 0, texture);

	glBindTexture(GL_TEXTURE_2D, g_textureName);
	glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

	glDisableVertexAttribArray(gvPositionHandle);
	glDisableVertexAttribArray(g_textureName);

	glDisableClientState(GL_TEXTURE_COORD_ARRAY);
	glDisableClientState(GL_VERTEX_ARRAY);

	glDisable(GL_TEXTURE_2D);
}

void Java_com_example_ehfcn_cookiechat_library_GL2JNILib_nativeSetTextureData(JNIEnv* env, jobject thiz, jintArray arr, jint width, jint height)
{
	jint *temp = (*env)->GetIntArrayElements(env, arr, 0);
	char *data = (char*)temp;
	setTextureData(data, width, height);
	(*env)->ReleaseIntArrayElements(env, arr, (jint*)data, JNI_ABORT);
}