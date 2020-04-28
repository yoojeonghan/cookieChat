package com.example.ehfcn.cookiechat.graphic;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by ehfcn on 2017-06-22.
 */

public class Map
{
    //float buffer 타입으로 vertexBuffer를 선언합니다.
    private FloatBuffer vertexBuffer;
    private ShortBuffer mDrawListBuffer;
    protected FloatBuffer mUvBuffer;

    protected static float mUvs[];

    private final float[] mMtrxView = new float[16];

    private int mHandleBitmap;

    Bitmap mbitmap;

    public static final String vs_Image =
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;"+
                    "attribute vec2 a_texCoord;"+
                    "varying vec2 v_texCoord;"+
                    "void main()"+
                    "{"+
                    "gl_Position = uMVPMatrix * vPosition;"+
                    "v_texCoord = a_texCoord;"+
                    "}";

    public static final String fs_Image =
                    "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main()"+
                    "{"+
                    "gl_FragColor = texture2D(s_texture, v_texCoord);"+
                    "}";

    static float squareCoords[]=
            {
                    -400.0f, 200.0f, 0.0f,//top left
                    -400.0f, -200.0f, 0.0f,//bottom left
                    400.0f, -200.0f, 0.0f,//bottom right
                    400.0f, 200.0f, 0.0f//top right
            };

    private short mDrawOrder[] = {0, 3, 2, 0, 2, 1};

    private final int mProgram;

    private int mPositionHandle;

    GLRenderer mGLRenderer;

    public Map(GLRenderer GLRenderer, Bitmap bitmap)
    {
        mGLRenderer = GLRenderer;

        //1.ByteBuffer를 할당 받습니다.
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                squareCoords.length * 4);

        //2. ByteBuffer에서 사용할 엔디안을 지정합니다.
        //버퍼의 byte order로써 디바이스 하드웨어의 native byte order를 사용
        bb.order(ByteOrder.nativeOrder());

        //3. ByteBuffer를 FloatBuffer로 변환합니다.
        vertexBuffer = bb.asFloatBuffer();

        //4. float 배열에 정의된 좌표들을 FloatBuffer에 저장합니다.
        vertexBuffer.put(squareCoords);

        //5. 읽어올 버퍼의 위치를 0으로 설정한다. 첫번째 좌표부터 읽어오게됨
        vertexBuffer.position(0);

        /*-----------------------------------------------------------------------------*/

        ByteBuffer dlb = ByteBuffer.allocateDirect(mDrawOrder.length*2);

        dlb.order(ByteOrder.nativeOrder());

        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(mDrawOrder);
        mDrawListBuffer.position(0);

        // 텍스쳐

        mUvs = new float[]
                {
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };

        ByteBuffer bbUvs = ByteBuffer.allocateDirect(mUvs.length * 4);
        bbUvs.order(ByteOrder.nativeOrder());
        mUvBuffer = bbUvs.asFloatBuffer();
        mUvBuffer.put(mUvs);
        mUvBuffer.position(0);

        //vertex shader 타입의 객체를 생성하여 vertexShaderCode에 저장된 소스코드를 로드한 후,
        //   컴파일합니다.
        int vertexShader = GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vs_Image);

        //fragment shader 타입의 객체를 생성하여 fragmentShaderCode에 저장된 소스코드를 로드한 후,
        //  컴파일합니다.
        int fragmentShader = GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fs_Image);

        // Program 객체를 생성한다.
        mProgram = GLES20.glCreateProgram();

        // vertex shader를 program 객체에 추가
        GLES20.glAttachShader(mProgram, vertexShader);

        // fragment shader를 program 객체에 추가
        GLES20.glAttachShader(mProgram, fragmentShader);

        // program객체를 OpenGL에 연결한다. program에 추가된 shader들이 OpenGL에 연결된다.
        GLES20.glLinkProgram(mProgram);

        mHandleBitmap = getImageHandle(bitmap);
    }

    public void draw(float[] mvpMatrix)
    {
        //렌더링 상태(Rendering State)의 일부분으로 program을 추가한다.
        GLES20.glUseProgram(mProgram);

        Matrix.setIdentityM(mMtrxView, 0);

        // program 객체로부터 vertex shader의'vPosition 멤버에 대한 핸들을 가져옴
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        //triangle vertex 속성을 활성화 시켜야 렌더링시 반영되서 그려짐
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // triangle vertex 속성을 vertexBuffer에 저장되어 있는 vertex 좌표들로 정의한다.
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // 텍스쳐

        int texCoordLoc = GLES20.glGetAttribLocation(mProgram, "a_texCoord");

        GLES20.glEnableVertexAttribArray(texCoordLoc);

        GLES20.glVertexAttribPointer(texCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mUvBuffer);

        int mtrxhandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mvpMatrix, 0);

        // 이미지 핸들 출력

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHandleBitmap);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        //vertex 속성을 비활성화 한다.
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(texCoordLoc);
    }

    private int getImageHandle(Bitmap bitmap)
    {
        int[] texturenames = new int[1];
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glGenTextures(1, texturenames, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        mbitmap = bitmap;
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap = null;
        return texturenames[0];
    }

    public void Recyclebitmap()
    {
        if(mbitmap != null)
        {
            mbitmap.recycle();
            mbitmap = null;
        }
    }


}
