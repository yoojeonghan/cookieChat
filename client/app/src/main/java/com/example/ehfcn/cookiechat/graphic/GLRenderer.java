package com.example.ehfcn.cookiechat.graphic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.ehfcn.cookiechat.R;
import com.example.ehfcn.cookiechat.library.GL2JNILib;
import com.example.ehfcn.cookiechat.scene.GameScene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.example.ehfcn.cookiechat.scene.GameScene.CurrentUserList;
import static com.example.ehfcn.cookiechat.scene.GameScene.IsItem;
import static com.example.ehfcn.cookiechat.scene.GameScene.ItemList;
import static com.example.ehfcn.cookiechat.scene.GameScene.ItemPosX;
import static com.example.ehfcn.cookiechat.scene.GameScene.ItemPosY;
import static com.example.ehfcn.cookiechat.scene.GameScene.LastUserList;
import static com.example.ehfcn.cookiechat.scene.GameScene.player;
import static com.example.ehfcn.cookiechat.scene.MainActivity.MyPREFERENCES;
import static com.example.ehfcn.cookiechat.scene.MainActivity.NicknameSession;


public class GLRenderer implements GLSurfaceView.Renderer
{
    // 주 액티비티

    private static Context mContext;
    private Activity mActivity;
    private SharedPreferences sharedpreferences;

    /*----------------------------------------------------------------------------------------*/

    // 매트릭스

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    /*----------------------------------------------------------------------------------------*/

    // 프로그램

    private static int mProgramImage;

    long mLastTime;

    /*----------------------------------------------------------------------------------------*/

    public Map mMap;
    public Map mMap2;
    public Map mMap3;
    public Map mMap4;
    public Map mMap5;

    public Character mCharacter;
    public Character mCharacter2;
    public Character mCharacter3;
    public Character mCharacter4;
    public Character mCharacter5;
    public Character mCharacter6;

    public Talk mTalk;
    public String mNameStr;
    public Name mName;
    public Balloons mBalloons;
    ArrayList<Talk> mTalks;

    /*----------------------------------------------------------------------------------------*/

    public ArrayList<Character> uCharacter;
    public ArrayList<float[]> uMVPMatrix;
    public ArrayList<Name> uName;
    public ArrayList<Bitmap> uNameBitmap;
    public ArrayList<String> uNameStr;
    public ArrayList<HangulBitmap> uHangulBitmap;
    public ArrayList<Balloons> uBalloons;
    public HashMap uTalkmap;
    public ArrayList<Talk> uTalk;

    /*----------------------------------------------------------------------------------------*/

    ArrayList<DrawOrder> DrawList;

    /*----------------------------------------------------------------------------------------*/

    // Position of camera
    public float eyeX = 0.0f;
    public float eyeY = 0.0f;
    public float eyeZ = 230.0f;

    // Direction of camera
    private float lookZ = -1.0f;

    // Camera tilt
    private float upX = 0.0f;
    private float upY = 1.0f;
    private float upZ = 0.0f;

    /*----------------------------------------------------------------------------------------*/

    Bitmap Map1Bitmap = null;
    Bitmap Map2Bitmap = null;
    Bitmap Map3Bitmap = null;
    Bitmap Map4Bitmap = null;
    Bitmap Map5Bitmap = null;

    // 아무것도 안 입었을 때
    Bitmap CharBitmap = null;
    // 주황색 머리 입었을 때
    Bitmap CharBitmap2 = null;
    // 하늘색 머리 입었을 때
    Bitmap CharBitmap3 = null;
    // 하얀색 머리 입었을 때
    Bitmap CharBitmap4 = null;
    // 갈색 머리 입었을 때
    Bitmap CharBitmap5 = null;
    // 하늘색 모자 입었을 때
    Bitmap CharBitmap6 = null;

    Bitmap BallonBitmap = null;
    Bitmap mNamebmp = null;

    Bitmap mTalkBitmap = null;
    Bitmap mTalkBitmap2 = null;
    Bitmap mTalkBitmap3 = null;
    Bitmap mTalkBitmap4 = null;

    Bitmap umNamebmp = null;
    Bitmap umTalkBitmap = null;
    Bitmap umTalkBitmap2 = null;
    Bitmap umTalkBitmap3 = null;
    Bitmap umTalkBitmap4 = null;

    /*----------------------------------------------------------------------------------------*/

    HangulBitmap mHangulBitmap;
    HangulBitmap mTalkHangulBitmap;

    /*----------------------------------------------------------------------------------------*/

    HangulBitmap umHangulBitmap;

    public static boolean IsRendering = false;

    // 생성자. 컨텍스트와 액티비티, 세션을 정의합니다.
    public GLRenderer(Context context, GameScene activity)
    {
        mContext = context;
        mActivity = activity;
        sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, mContext.MODE_PRIVATE);
        mLastTime = System.currentTimeMillis() + 100;
    }

    public void onResume()
    {
        mLastTime = System.currentTimeMillis();
    }

    // 그래픽 랜더링 서페이스가 생성되었을 때 한 번 호출되는 메소드입니다.
    // 다른 유저들의 렌더링 정보를 담을 리스트를 정의합니다.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        mProgramImage = GLES20.glCreateProgram();

        GLES20.glLinkProgram(mProgramImage);
        GLES20.glUseProgram(mProgramImage);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        GL2JNILib.nativeCreated();

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.item);

        int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        GL2JNILib.nativeSetTextureData(pixels, bitmap.getWidth(), bitmap.getHeight());

        init();
    }

    public void init()
    {
        Map1Bitmap = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/map2bit", null, mContext.getPackageName()));
        Map2Bitmap = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/map3", null, mContext.getPackageName()));
        Map3Bitmap = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/map4", null, mContext.getPackageName()));
        Map4Bitmap = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/map5", null, mContext.getPackageName()));
        Map5Bitmap = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/map6", null, mContext.getPackageName()));

        CharBitmap = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/char1", null, mContext.getPackageName()));
        CharBitmap2 = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/char2", null, mContext.getPackageName()));
        CharBitmap3 = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/char3", null, mContext.getPackageName()));
        CharBitmap4 = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/char4", null, mContext.getPackageName()));
        CharBitmap5 = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/char5", null, mContext.getPackageName()));
        CharBitmap6 = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/char6", null, mContext.getPackageName()));

        BallonBitmap = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier("drawable/talk", null, mContext.getPackageName()));

        mMap = new Map(this, Map1Bitmap);
        mMap2 = new Map(this, Map2Bitmap);
        mMap3 = new Map(this, Map3Bitmap);
        mMap4 = new Map(this, Map4Bitmap);
        mMap5 = new Map(this, Map5Bitmap);

        mCharacter = new Character(mProgramImage, this, CharBitmap, player.ID);
        mCharacter2 = new Character(mProgramImage, this, CharBitmap2, player.ID);
        mCharacter3 = new Character(mProgramImage, this, CharBitmap3, player.ID);
        mCharacter4 = new Character(mProgramImage, this, CharBitmap4, player.ID);
        mCharacter5 = new Character(mProgramImage, this, CharBitmap5, player.ID);
        mCharacter6 = new Character(mProgramImage, this, CharBitmap6, player.ID);

        mBalloons = new Balloons(this, BallonBitmap);
        mNamebmp = Bitmap.createBitmap((int) (48*6), (int)(48), Bitmap.Config.ARGB_8888);
        mHangulBitmap = new HangulBitmap(mActivity);
        mNameStr = mCharacter.NickName;
        mHangulBitmap.GetBitmap(mNamebmp, mNameStr, 48, Color.WHITE, Color.BLACK, 1);
        mName = new Name(this, mNamebmp);
        mTalks = new ArrayList<Talk>();

        // 다른 유저들의 랜더링을 담을 리스트
        uCharacter = new ArrayList<Character>();
        uMVPMatrix = new ArrayList<float[]>();
        uName = new ArrayList<Name>();
        uNameStr = new ArrayList<String>();
        uNameBitmap = new ArrayList<Bitmap>();
        uHangulBitmap = new ArrayList<HangulBitmap>();
        uTalk = new ArrayList<Talk>();
        uBalloons = new ArrayList<Balloons>();
        uTalkmap = new HashMap();

        // 모든 유동적인 랜더링(맵 제외한 캐릭터/말풍선/이름 같은 요소)를 담을 리스트
        DrawList = new ArrayList<DrawOrder>();

    }

    // GLSurfaceView의 크기 변경 또는 디바이스 화면의 방향 전환 등으로 인해
    // GLSurfaceView의 geometry가 바뀔때 호출되는 메소드입니다.
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);

        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.orthoM(mProjectionMatrix, 0, -500, 500, -800, 800, 0, 50);
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //GLSurfaceView 너비와 높이 사이의 비율을 계산합니다.
        float ratio = (float) width / height;

        //3차원 공간의 점을 2차원 화면에 보여주기 위해 사용되는 projection matrix를 정의
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 100000);

        GL2JNILib.nativeChanged(width, height);
    }

    // 다시 그려질때마다 호출되는 메소드입니다.
    public void onDrawFrame(GL10 gl)
    {
        IsRendering = true;

        if(eyeX < -230)
        {
            eyeX = -230;
        }
        else if(eyeX > 230)
        {
            eyeX = 230;
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        Matrix.setLookAtM(
                mViewMatrix, 0,
                eyeX, eyeY, eyeZ,
                eyeX, eyeY, eyeZ + lookZ,
                upX, upY, upZ);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        MyRender();
        UserRender();
        AddDrawList();
        Draw();
        //GL2JNILib.step();

        for(int i = 0; i < ItemList.size(); i++)
        {
            int SendPosX = ItemList.get(i).ItemPos.x-(int)eyeX;
            GL2JNILib.nativeUpdateGame(mMVPMatrix, SendPosX, ItemList.get(i).ItemPos.y);
        }

        ClearDrawList();

        IsRendering = false;
    }

    // 쉐이더를 불러옵니다.
    public static int loadShader(int type, String shaderCode)
    {
        // 다음 2가지 타입 중 하나로 shader객체를 생성한다.
        // vertex shader type (GLES20.GL_VERTEX_SHADER)
        // 또는 fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // shader객체에 shader source code를 로드합니다.
        GLES20.glShaderSource(shader, shaderCode);

        //shader객체를 컴파일 합니다.
        GLES20.glCompileShader(shader);

        return shader;
    }

    // 렌더링할 요소들을 리스트에 넣습니다.
    public void AddDrawList()
    {
        DrawOrder myChar = null;

        if (GameScene.player.ChatString != null)
        {
            if (mTalks.size() == 1)
            {
                switch(player.WearItem)
                {
                    case 0:
                        myChar = new DrawOrder(mCharacter, mName, mBalloons, mTalk, mCharacter.translation.y);
                        break;
                    case 1:
                        myChar = new DrawOrder(mCharacter2, mName, mBalloons, mTalk, mCharacter.translation.y);
                        break;
                    case 2:
                        myChar = new DrawOrder(mCharacter3, mName, mBalloons, mTalk, mCharacter.translation.y);
                        break;
                    case 3:
                        myChar = new DrawOrder(mCharacter4, mName, mBalloons, mTalk, mCharacter.translation.y);
                        break;
                    case 4:
                        myChar = new DrawOrder(mCharacter5, mName, mBalloons, mTalk, mCharacter.translation.y);
                        break;
                    case 5:
                        myChar = new DrawOrder(mCharacter6, mName, mBalloons, mTalk, mCharacter.translation.y);
                        break;
                }
                DrawList.add(myChar);
            }
            else if (mTalks.size() == 2)
            {
                switch(player.WearItem)
                {
                    case 0:
                        myChar = new DrawOrder(mCharacter, mName, mBalloons, mTalks.get(0), mTalks.get(1), mCharacter.translation.y);
                        break;
                    case 1:
                        myChar = new DrawOrder(mCharacter2, mName, mBalloons, mTalks.get(0), mTalks.get(1), mCharacter2.translation.y);
                        break;
                    case 2:
                        myChar = new DrawOrder(mCharacter3, mName, mBalloons, mTalks.get(0), mTalks.get(1), mCharacter3.translation.y);
                        break;
                    case 3:
                        myChar = new DrawOrder(mCharacter4, mName, mBalloons, mTalks.get(0), mTalks.get(1), mCharacter4.translation.y);
                        break;
                    case 4:
                        myChar = new DrawOrder(mCharacter5, mName, mBalloons, mTalks.get(0), mTalks.get(1),mCharacter.translation.y);
                        break;
                    case 5:
                        myChar = new DrawOrder(mCharacter6, mName, mBalloons, mTalks.get(0), mTalks.get(1), mCharacter.translation.y);
                        break;
                }
                DrawList.add(myChar);
            }
            else if (mTalks.size() == 3)
            {
                switch(player.WearItem)
                {
                    case 0:
                        myChar = new DrawOrder(mCharacter3, mName, mBalloons,mTalks.get(0), mTalks.get(1), mTalks.get(2),mCharacter.translation.y);
                        break;
                    case 1:
                        myChar = new DrawOrder(mCharacter3, mName, mBalloons,mTalks.get(0), mTalks.get(1), mTalks.get(2),mCharacter2.translation.y);
                        break;
                    case 2:
                        myChar = new DrawOrder(mCharacter3, mName, mBalloons,mTalks.get(0), mTalks.get(1), mTalks.get(2), mCharacter3.translation.y);
                        break;
                    case 3:
                        myChar = new DrawOrder(mCharacter4, mName, mBalloons, mTalks.get(0), mTalks.get(1), mTalks.get(2), mCharacter4.translation.y);
                        break;
                    case 4:
                        myChar = new DrawOrder(mCharacter5, mName, mBalloons, mTalks.get(0), mTalks.get(1), mTalks.get(2), mCharacter5.translation.y);
                        break;
                    case 5:
                        myChar = new DrawOrder(mCharacter6, mName, mBalloons, mTalks.get(0), mTalks.get(1), mTalks.get(2), mCharacter6.translation.y);
                        break;
                }
                DrawList.add(myChar);
            }
            else if (mTalks.size() == 4)
            {
                switch(player.WearItem)
                {
                    case 0:
                        myChar = new DrawOrder(mCharacter, mName, mBalloons,  mTalks.get(0), mTalks.get(1), mTalks.get(2), mTalks.get(3),mCharacter.translation.y);
                        break;
                    case 1:
                        myChar = new DrawOrder(mCharacter2, mName, mBalloons,  mTalks.get(0), mTalks.get(1), mTalks.get(2), mTalks.get(3),mCharacter2.translation.y);
                        break;
                    case 2:
                        myChar = new DrawOrder(mCharacter3, mName, mBalloons,  mTalks.get(0), mTalks.get(1), mTalks.get(2), mTalks.get(3), mCharacter3.translation.y);
                        break;
                    case 3:
                        myChar = new DrawOrder(mCharacter4, mName, mBalloons,  mTalks.get(0), mTalks.get(1), mTalks.get(2), mTalks.get(3), mCharacter4.translation.y);
                        break;
                    case 4:
                        myChar = new DrawOrder(mCharacter5, mName, mBalloons, mTalks.get(0), mTalks.get(1), mTalks.get(2), mTalks.get(3), mCharacter5.translation.y);
                        break;
                    case 5:
                        myChar = new DrawOrder(mCharacter6, mName, mBalloons, mTalks.get(0), mTalks.get(1), mTalks.get(2), mTalks.get(3), mCharacter6.translation.y);
                        break;
                }
                DrawList.add(myChar);
            }
        }
        else
        {
            switch(player.WearItem)
            {
                case 0:
                    myChar = new DrawOrder(mCharacter, mName, mCharacter.translation.y);
                    break;
                case 1:
                    myChar = new DrawOrder(mCharacter2, mName, mCharacter2.translation.y);
                    break;
                case 2:
                    myChar = new DrawOrder(mCharacter3, mName, mCharacter3.translation.y);
                    break;
                case 3:
                    myChar = new DrawOrder(mCharacter4, mName, mCharacter4.translation.y);
                    break;
                case 4:
                    myChar = new DrawOrder(mCharacter5, mName, mCharacter5.translation.y);
                    break;
                case 5:
                    myChar = new DrawOrder(mCharacter6, mName, mCharacter6.translation.y);
                    break;
            }
            DrawList.add(myChar);
        }

            for (int i = 0; i < uCharacter.size(); i++)
            {
                if (uTalkmap.get(i) != null)
                {
                    ArrayList<Talk> TempArray = (ArrayList<Talk>) uTalkmap.get(i);

                    if (TempArray.size() == 1)
                    {
                        DrawOrder userChar = new DrawOrder(uCharacter.get(i), uName.get(i), uBalloons.get(i), (Talk) TempArray.get(0), uCharacter.get(i).translation.y);
                        DrawList.add(userChar);
                    }
                    else if (TempArray.size() == 2)
                    {
                        DrawOrder userChar = new DrawOrder(uCharacter.get(i), uName.get(i), uBalloons.get(i), (Talk) TempArray.get(0), (Talk) TempArray.get(1), uCharacter.get(i).translation.y);
                        DrawList.add(userChar);
                    }
                    else if (TempArray.size() == 3)
                    {
                        DrawOrder userChar = new DrawOrder(uCharacter.get(i), uName.get(i), uBalloons.get(i), (Talk) TempArray.get(0), (Talk) TempArray.get(1), (Talk) TempArray.get(2), uCharacter.get(i).translation.y);
                        DrawList.add(userChar);
                    }
                    else if (TempArray.size() == 4)
                    {
                        DrawOrder userChar = new DrawOrder(uCharacter.get(i), uName.get(i), uBalloons.get(i), (Talk) TempArray.get(0), (Talk) TempArray.get(1), (Talk) TempArray.get(2), (Talk) TempArray.get(3), uCharacter.get(i).translation.y);
                        DrawList.add(userChar);
                    }

                }
                else
                {
                    if(uCharacter.size() == uName.size())
                    {
                        DrawOrder userChar = new DrawOrder(uCharacter.get(i), uName.get(i), uCharacter.get(i).translation.y);
                        DrawList.add(userChar);
                    }

                }
            }
    }

    // 내 캐릭터를 렌더링합니다.
    public void MyRender()
    {
        mCharacter.translation.x = player.Currentpos.x;
        mCharacter.translation.y = player.Currentpos.y;
        mCharacter.IsCharacterleft = player.IsLeft;
        mCharacter.translate();

        mCharacter2.translation.x = player.Currentpos.x;
        mCharacter2.translation.y = player.Currentpos.y;
        mCharacter2.IsCharacterleft = player.IsLeft;
        mCharacter2.translate();

        mCharacter3.translation.x = player.Currentpos.x;
        mCharacter3.translation.y = player.Currentpos.y;
        mCharacter3.IsCharacterleft = player.IsLeft;
        mCharacter3.translate();

        mCharacter4.translation.x = player.Currentpos.x;
        mCharacter4.translation.y = player.Currentpos.y;
        mCharacter4.IsCharacterleft = player.IsLeft;
        mCharacter4.translate();

        mCharacter5.translation.x = player.Currentpos.x;
        mCharacter5.translation.y = player.Currentpos.y;
        mCharacter5.IsCharacterleft = player.IsLeft;
        mCharacter5.translate();

        mCharacter6.translation.x = player.Currentpos.x;
        mCharacter6.translation.y = player.Currentpos.y;
        mCharacter6.IsCharacterleft = player.IsLeft;
        mCharacter6.translate();

        // 이름 렌더링 위치 지정
        mName.translate(mCharacter.translation.x + 7, mCharacter.translation.y - 35);

        // 플레이어가 채팅을 하여 말풍선을 띄웠을 경우
        if (GameScene.player.ChatString != null)
        {
            mBalloons.translate(mCharacter.translation.x + 7, mCharacter.translation.y + 52);
            /*------------------------------------------------------------------------------------*/

            byte[] temp = GameScene.player.ChatString.getBytes();
            int ballonstringsize = GameScene.player.ChatString.getBytes().length;

            if (0 < ballonstringsize && ballonstringsize < 30)
            {
                mTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                mTalkHangulBitmap = new HangulBitmap(mActivity);
                mTalkHangulBitmap.GetBitmap(mTalkBitmap, GameScene.player.ChatString, 48, Color.WHITE, Color.BLACK, 1);
                mTalk = new Talk(this, mTalkBitmap);
                mTalkHangulBitmap.RecycleBitmap();

                mTalk.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 60);
                mTalks.add(mTalk);
            }
            else if (30 <= ballonstringsize && ballonstringsize < 60)
            {
                int twolinesize = ballonstringsize - 30;
                String s1 = new String(temp, 0, 30);
                String s2 = new String(temp, 30, twolinesize);

                mTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                mTalkHangulBitmap = new HangulBitmap(mActivity);
                mTalkHangulBitmap.GetBitmap(mTalkBitmap, s1, 48, Color.WHITE, Color.BLACK, 1);

                mTalk = new Talk(this, mTalkBitmap);
                mTalkHangulBitmap.RecycleBitmap();
                mTalk.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 70);

                mTalkBitmap2 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                HangulBitmap mTalkHangulBitmap2 = new HangulBitmap(mActivity);
                mTalkHangulBitmap2.GetBitmap(mTalkBitmap2, s2, 48, Color.WHITE, Color.BLACK, 1);

                Talk mTalk2 = new Talk(this, mTalkBitmap2);
                mTalkHangulBitmap2.RecycleBitmap();
                mTalk2.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 60);

                mTalks.add(mTalk);
                mTalks.add(mTalk2);
            }
            else if (60 <= ballonstringsize && ballonstringsize < 90)
            {
                int twolinesize = ballonstringsize - 30;
                int threelinesize = ballonstringsize - 60;

                String s1 = new String(temp, 0, 30);
                String s2 = new String(temp, 30, twolinesize);
                String s3 = new String(temp, 60, threelinesize);

                mTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                mTalkHangulBitmap = new HangulBitmap(mActivity);
                mTalkHangulBitmap.GetBitmap(mTalkBitmap, s1, 48, Color.WHITE, Color.BLACK, 1);

                mTalk = new Talk(this, mTalkBitmap);
                mTalkHangulBitmap.RecycleBitmap();
                mTalk.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 60);

                mTalkBitmap2 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                HangulBitmap mTalkHangulBitmap2 = new HangulBitmap(mActivity);
                mTalkHangulBitmap2.GetBitmap(mTalkBitmap2, s2, 48, Color.WHITE, Color.BLACK, 1);

                Talk mTalk2 = new Talk(this, mTalkBitmap2);
                mTalkHangulBitmap2.RecycleBitmap();
                mTalk2.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 50);

                mTalkBitmap3 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                HangulBitmap mTalkHangulBitmap3 = new HangulBitmap(mActivity);
                mTalkHangulBitmap3.GetBitmap(mTalkBitmap3, s3, 48, Color.WHITE, Color.BLACK, 1);

                Talk mTalk3 = new Talk(this, mTalkBitmap3);
                mTalkHangulBitmap3.RecycleBitmap();
                mTalk3.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 40);

                mTalks.add(mTalk);
                mTalks.add(mTalk2);
                mTalks.add(mTalk3);
            }
            else if (90 <= ballonstringsize && ballonstringsize < 120)
            {
                int twolinesize = ballonstringsize - 30;
                int threelinesize = ballonstringsize - 60;
                int fourlinesize = ballonstringsize - 90;

                String s1 = new String(temp, 0, 30);
                String s2 = new String(temp, 30, twolinesize);
                String s3 = new String(temp, 60, threelinesize);
                String s4 = new String(temp, 90, fourlinesize);

                mTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                mTalkHangulBitmap = new HangulBitmap(mActivity);
                mTalkHangulBitmap.GetBitmap(mTalkBitmap, s1, 48, Color.WHITE, Color.BLACK, 1);

                mTalk = new Talk(this, mTalkBitmap);
                mTalkHangulBitmap.RecycleBitmap();
                mTalk.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 70);

                mTalkBitmap2 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                HangulBitmap mTalkHangulBitmap2 = new HangulBitmap(mActivity);
                mTalkHangulBitmap2.GetBitmap(mTalkBitmap2, s2, 48, Color.WHITE, Color.BLACK, 1);

                Talk mTalk2 = new Talk(this, mTalkBitmap2);
                mTalkHangulBitmap2.RecycleBitmap();
                mTalk2.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 60);

                mTalkBitmap3 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                HangulBitmap mTalkHangulBitmap3 = new HangulBitmap(mActivity);
                mTalkHangulBitmap3.GetBitmap(mTalkBitmap3, s3, 48, Color.WHITE, Color.BLACK, 1);

                Talk mTalk3 = new Talk(this, mTalkBitmap3);
                mTalkHangulBitmap3.RecycleBitmap();
                mTalk3.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 50);

                mTalkBitmap4 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                HangulBitmap mTalkHangulBitmap4 = new HangulBitmap(mActivity);
                mTalkHangulBitmap4.GetBitmap(mTalkBitmap4, s4, 48, Color.WHITE, Color.BLACK, 1);

                Talk mTalk4 = new Talk(this, mTalkBitmap4);
                mTalkHangulBitmap4.RecycleBitmap();
                mTalk4.translate(mCharacter.translation.x - 5, mCharacter.translation.y + 40);

                mTalks.add(mTalk);
                mTalks.add(mTalk2);
                mTalks.add(mTalk3);
                mTalks.add(mTalk4);
            }
        }
    }

    public void GenUser()
    {
        if(uCharacter != null)
        {
            uCharacter.clear();
            uCharacter = null;
        }

        uCharacter = new ArrayList<Character>();

        for(int i = 0; i < CurrentUserList.size(); i++)
        {
            Character User = null;
            switch(CurrentUserList.get(i).WearItem)
            {
                case 0:
                    User = new Character(mProgramImage, this, CharBitmap, CurrentUserList.get(i).ID);
                    break;
                case 1:
                    User = new Character(mProgramImage, this, CharBitmap2, CurrentUserList.get(i).ID);
                    break;
                case 2:
                    User = new Character(mProgramImage, this, CharBitmap3, CurrentUserList.get(i).ID);
                    break;
                case 3:
                    User = new Character(mProgramImage, this, CharBitmap4, CurrentUserList.get(i).ID);
                    break;
                case 4:
                    User = new Character(mProgramImage, this, CharBitmap5, CurrentUserList.get(i).ID);
                    break;
                case 5:
                    User = new Character(mProgramImage, this, CharBitmap6, CurrentUserList.get(i).ID);
            }
            uCharacter.add(User);
        }

        System.out.println("렌더러 : 유저 리스트가 갱신되었습니다. ");
        System.out.print("렌더러 : 그려질 유저 리스트 : ");

        for(int i = 0; i < uCharacter.size(); i++)
        {
            System.out.print(uCharacter.get(i).NickName);
            System.out.print(" , ");
        }
        System.out.println("");

    }

    // 내 캐릭터를 제외한 나머지 유저들의 캐릭터를 렌더링합니다.
    public void UserRender()
    {
        if(LastUserList.size() == 0)
        {
            GenUser();
        }
        else if(LastUserList.size() != CurrentUserList.size())
        {
            GenUser();
        }
        else
        {
            int UserListSize = 0;
            if(LastUserList.size() > CurrentUserList.size())
            {
                UserListSize = LastUserList.size();
            }
            else
            {
                UserListSize = CurrentUserList.size();
            }

            for(int i = 0; i < UserListSize; i++)
            {
                if(!LastUserList.get(i).ID.equals(CurrentUserList.get(i).ID))
                {
                    GenUser();
                    break;
                }
                else if(LastUserList.get(i).WearItem != CurrentUserList.get(i).WearItem)
                {
                    GenUser();
                    break;
                }
            }
        }

        if(CurrentUserList.size() != uCharacter.size())
        {
            GenUser();
        }

        for(int i = 0; i < uCharacter.size(); i++)
        {
            if(uCharacter.size() == CurrentUserList.size())
            {
                uCharacter.get(i).NickName = CurrentUserList.get(i).ID;
                uCharacter.get(i).translation.x = CurrentUserList.get(i).Currentpos.x;
                uCharacter.get(i).translation.y = CurrentUserList.get(i).Currentpos.y;
                uCharacter.get(i).IsCharacterleft = CurrentUserList.get(i).IsLeft;
                uCharacter.get(i).translate();

                String umNamestr = uCharacter.get(i).NickName;
                uNameStr.add(umNamestr);
            }
        }

        /*------------------------------------------------------------------------------------*/

        for (int i = 0; i < uCharacter.size(); i++)
        {
            Balloons umBalloons = new Balloons(this, BallonBitmap);
            umBalloons.translate(uCharacter.get(i).translation.x + 7, uCharacter.get(i).translation.y + 52);

            uBalloons.add(umBalloons);
            uTalkmap.put(i, null);

            umHangulBitmap = new HangulBitmap(mActivity);
            uHangulBitmap.add(umHangulBitmap);
        }

        for (int i = 0 ; i < uHangulBitmap.size(); i++)
        {
            umNamebmp = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
            uNameBitmap.add(umNamebmp);

            /*------------------------------------------------------------------------------------*/

            float[] umMVPMatrix = new float[16];
            uMVPMatrix.add(umMVPMatrix);
            Matrix.multiplyMM(uMVPMatrix.get(i), 0, mProjectionMatrix, 0, mViewMatrix, 0);

            /*------------------------------------------------------------------------------------*/
        }

        for (int i = 0; i < uHangulBitmap.size(); i++)
        {
            try
            {
                uHangulBitmap.get(i).GetBitmap(uNameBitmap.get(i), uNameStr.get(i), 48, Color.WHITE, Color.BLACK, -1);
            }
            catch(IndexOutOfBoundsException e)
            {
                System.out.println(e);
            }
        }

        for(int i = 0; i < uNameBitmap.size(); i++)
        {
            Name umName = new Name(this, uNameBitmap.get(i));
            uName.add(umName);
        }

        for(int i = 0; i < uName.size(); i++)
        {
            if(uName.size() == uCharacter.size())
            {
                uName.get(i).translate(uCharacter.get(i).translation.x + 7, uCharacter.get(i).translation.y - 35);
            }
        }

        /*------------------------------------------------------------------------------------*/
        for (int i = 0; i < uCharacter.size(); i++)
        {
            /*------------------------------------------------------------------------------------*/

            if(CurrentUserList.size() > 0)
            {
                try
                {
                    if(CurrentUserList.get(i).ChatString != null)
                    {
                        if (!CurrentUserList.get(i).ID.equals(NicknameSession))
                        {
                    /*----------------------------------------------------------------------------*/

                            byte[] temp = CurrentUserList.get(i).ChatString.getBytes();
                            int ballonstringsize = CurrentUserList.get(i).ChatString.getBytes().length;

                            if (0 < ballonstringsize && ballonstringsize < 30) {
                                umTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap = new HangulBitmap(mActivity);
                                umTalkHangulBitmap.GetBitmap(umTalkBitmap, CurrentUserList.get(i).ChatString, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk = new Talk(this, umTalkBitmap);
                                umTalkHangulBitmap.RecycleBitmap();
                                umTalk.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 60);

                                ArrayList<Talk> uTalkArray = new ArrayList<Talk>();

                                uTalkArray.add(umTalk);

                                uTalkmap.put(i, uTalkArray);
                            } else if (30 <= ballonstringsize && ballonstringsize < 60) {
                                int twolinesize = ballonstringsize - 30;

                                String s1 = new String(temp, 0, 30);
                                String s2 = new String(temp, 30, twolinesize);

                                umTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap = new HangulBitmap(mActivity);
                                umTalkHangulBitmap.GetBitmap(umTalkBitmap, s1, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk = new Talk(this, umTalkBitmap);
                                umTalkHangulBitmap.RecycleBitmap();
                                umTalk.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 70);

                                umTalkBitmap2 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap2 = new HangulBitmap(mActivity);
                                umTalkHangulBitmap2.GetBitmap(umTalkBitmap2, s2, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk2 = new Talk(this, umTalkBitmap2);
                                umTalkHangulBitmap2.RecycleBitmap();
                                umTalk2.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 60);

                                ArrayList<Talk> uTalkArray = new ArrayList<Talk>();

                                uTalkArray.add(0, umTalk);
                                uTalkArray.add(1, umTalk2);

                                uTalkmap.put(i, uTalkArray);
                            } else if (60 <= ballonstringsize && ballonstringsize < 90) {
                                int twolinesize = ballonstringsize - 30;
                                int threelinesize = ballonstringsize - 60;

                                String s1 = new String(temp, 0, 30);
                                String s2 = new String(temp, 30, twolinesize);
                                String s3 = new String(temp, 60, threelinesize);

                                umTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap = new HangulBitmap(mActivity);
                                umTalkHangulBitmap.GetBitmap(umTalkBitmap, s1, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk = new Talk(this, umTalkBitmap);
                                umTalkHangulBitmap.RecycleBitmap();
                                umTalk.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 60);

                                umTalkBitmap2 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap2 = new HangulBitmap(mActivity);
                                umTalkHangulBitmap2.GetBitmap(umTalkBitmap2, s2, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk2 = new Talk(this, umTalkBitmap2);
                                umTalkHangulBitmap2.RecycleBitmap();
                                umTalk2.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 50);

                                umTalkBitmap3 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap3 = new HangulBitmap(mActivity);
                                umTalkHangulBitmap3.GetBitmap(umTalkBitmap3, s3, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk3 = new Talk(this, umTalkBitmap3);
                                umTalkHangulBitmap3.RecycleBitmap();
                                umTalk3.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 40);

                                ArrayList<Talk> uTalkArray = new ArrayList<Talk>();

                                uTalkArray.add(0, umTalk);
                                uTalkArray.add(1, umTalk2);
                                uTalkArray.add(2, umTalk3);

                                uTalkmap.put(i, uTalkArray);
                            }
                            else if (90 <= ballonstringsize && ballonstringsize < 120)
                            {
                                int twolinesize = ballonstringsize - 30;
                                int threelinesize = ballonstringsize - 60;
                                int fourlinesize = ballonstringsize - 90;

                                String s1 = new String(temp, 0, 30);
                                String s2 = new String(temp, 30, twolinesize);
                                String s3 = new String(temp, 60, threelinesize);
                                String s4 = new String(temp, 90, fourlinesize);

                                umTalkBitmap = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap = new HangulBitmap(mActivity);
                                umTalkHangulBitmap.GetBitmap(umTalkBitmap, s1, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk = new Talk(this, umTalkBitmap);
                                umTalkHangulBitmap.RecycleBitmap();
                                umTalk.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 70);

                                umTalkBitmap2 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap2 = new HangulBitmap(mActivity);
                                umTalkHangulBitmap2.GetBitmap(umTalkBitmap2, s2, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk2 = new Talk(this, umTalkBitmap2);
                                umTalkHangulBitmap2.RecycleBitmap();
                                umTalk2.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 60);

                                umTalkBitmap3 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap3 = new HangulBitmap(mActivity);
                                umTalkHangulBitmap3.GetBitmap(umTalkBitmap3, s3, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk3 = new Talk(this, umTalkBitmap3);
                                umTalkHangulBitmap3.RecycleBitmap();
                                umTalk3.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 50);

                                umTalkBitmap4 = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
                                HangulBitmap umTalkHangulBitmap4 = new HangulBitmap(mActivity);
                                umTalkHangulBitmap4.GetBitmap(umTalkBitmap4, s4, 48, Color.WHITE, Color.BLACK, 1);

                                Talk umTalk4 = new Talk(this, umTalkBitmap4);
                                umTalkHangulBitmap4.RecycleBitmap();
                                umTalk4.translate(uCharacter.get(i).translation.x - 5, uCharacter.get(i).translation.y + 40);

                                ArrayList<Talk> uTalkArray = new ArrayList<Talk>();

                                uTalkArray.add(0, umTalk);
                                uTalkArray.add(1, umTalk2);
                                uTalkArray.add(2, umTalk3);
                                uTalkArray.add(3, umTalk4);

                                uTalkmap.put(i, uTalkArray);
                            }

                    /*----------------------------------------------------------------------------*/
                        }
                    }
                }
                catch(IndexOutOfBoundsException e)
                {
                    System.out.println("IndexOutOFBoundsException :: " + e);
                }

            }

        }
    }

    // 렌더링한 요소들을 그립니다.
    public void Draw()
    {
        Collections.sort(DrawList);
        // 맵 랜더링
        switch(player.CurrentMapNum)
        {
            case 1:
                mMap.draw(mMVPMatrix);
                break;
            case 2:
                mMap2.draw(mMVPMatrix);
                break;
            case 3:
                mMap3.draw(mMVPMatrix);
                break;
            case 4:
                mMap4.draw(mMVPMatrix);
                break;
            case 5:
                mMap5.draw(mMVPMatrix);
                break;
        }

        for (int i = 0; i < DrawList.size(); i++)
        {
            DrawList.get(i).Character.draw(mMVPMatrix);
            //DrawList.get(i).Character.Recyclebitmap();
            DrawList.get(i).Name.draw(mMVPMatrix);
            DrawList.get(i).Name.Recyclebitmap();

            if (DrawList.get(i).Talk4 != null)
            {
                DrawList.get(i).Balloons.draw(mMVPMatrix);
                DrawList.get(i).Talk.draw(mMVPMatrix);
                DrawList.get(i).Talk.Recyclebitmap();
                DrawList.get(i).Talk2.draw(mMVPMatrix);
                DrawList.get(i).Talk2.Recyclebitmap();
                DrawList.get(i).Talk3.draw(mMVPMatrix);
                DrawList.get(i).Talk3.Recyclebitmap();
                DrawList.get(i).Talk4.draw(mMVPMatrix);
                DrawList.get(i).Talk4.Recyclebitmap();
            }
            else if (DrawList.get(i).Talk4 == null && DrawList.get(i).Talk3 != null)
            {
                DrawList.get(i).Balloons.draw(mMVPMatrix);
                DrawList.get(i).Talk.draw(mMVPMatrix);
                DrawList.get(i).Talk.Recyclebitmap();
                DrawList.get(i).Talk2.draw(mMVPMatrix);
                DrawList.get(i).Talk2.Recyclebitmap();
                DrawList.get(i).Talk3.draw(mMVPMatrix);
                DrawList.get(i).Talk3.Recyclebitmap();
            }
            else if (DrawList.get(i).Talk4 == null && DrawList.get(i).Talk3 == null && DrawList.get(i).Talk2 != null)
            {
                DrawList.get(i).Balloons.draw(mMVPMatrix);
                DrawList.get(i).Talk.draw(mMVPMatrix);
                DrawList.get(i).Talk.Recyclebitmap();
                DrawList.get(i).Talk2.draw(mMVPMatrix);
                DrawList.get(i).Talk2.Recyclebitmap();
            }
            else if (DrawList.get(i).Talk4 == null && DrawList.get(i).Talk3 == null && DrawList.get(i).Talk2 == null && DrawList.get(i).Talk != null)
            {
                DrawList.get(i).Balloons.draw(mMVPMatrix);
                DrawList.get(i).Talk.draw(mMVPMatrix);
                DrawList.get(i).Talk.Recyclebitmap();
            }
        }
    }

    // 사용된 bmp와 리스트를 초기화합니다.
    public void ClearDrawList()
    {
        if(mNamebmp != null)
        {
            mNamebmp.recycle();
            mNamebmp = null;
        }
        if(mTalkBitmap != null)
        {
            mTalkBitmap.recycle();
            mTalkBitmap = null;
        }
        if(mTalkBitmap2 != null)
        {
            mTalkBitmap2.recycle();
            mTalkBitmap2 = null;
        }
        if(mTalkBitmap3 != null)
        {
            mTalkBitmap3.recycle();
            mTalkBitmap3 = null;
        }
        if(mTalkBitmap4 != null)
        {
            mTalkBitmap4.recycle();
            mTalkBitmap4 = null;
        }

        /*------------------------------------------------------------------------------------*/

        mTalks.clear();

        /*------------------------------------------------------------------------------------*/

        if(umNamebmp != null)
        {
            umNamebmp.recycle();
            umNamebmp = null;
        }

        if(umTalkBitmap != null)
        {
            umTalkBitmap.recycle();
            umTalkBitmap = null;
        }

        if(umTalkBitmap2 != null)
        {
            umTalkBitmap2.recycle();
            umTalkBitmap2 = null;
        }

        if(umTalkBitmap3 != null)
        {
            umTalkBitmap3.recycle();
            umTalkBitmap3 = null;
        }

        if(umTalkBitmap4 != null)
        {
            umTalkBitmap4.recycle();
            umTalkBitmap4 = null;
        }

        /*------------------------------------------------------------------------------------*/

        uMVPMatrix.clear();
        uName.clear();
        uNameStr.clear();
        uNameBitmap.clear();
        uHangulBitmap.clear();
        uTalk.clear();
        uBalloons.clear();
        uTalkmap.clear();
        DrawList.clear();
    }

    // 렌더링 순서를 정렬하기 위하여 사용하는 클래스입니다.
    public class DrawOrder implements Comparable<DrawOrder>
    {
        public Character Character;
        public Name Name;
        public Balloons Balloons;
        public Talk Talk;
        public Talk Talk2;
        public Talk Talk3;
        public Talk Talk4;
        public float PointY;

        public DrawOrder(Character character, Name name, Balloons balloons, Talk talk, float pointY)
        {
            Character = character;
            Name = name;
            Balloons = balloons;
            Talk = talk;
            PointY = pointY;
        }

        public DrawOrder(Character character, Name name, Balloons balloons, Talk talk, Talk talk2, float pointY)
        {
            Character = character;
            Name = name;
            Balloons = balloons;
            Talk = talk;
            Talk2 = talk2;
            PointY = pointY;
        }

        public DrawOrder(Character character, Name name, Balloons balloons, Talk talk, Talk talk2, Talk talk3, float pointY)
        {
            Character = character;
            Name = name;
            Balloons = balloons;
            Talk = talk;
            Talk2 = talk2;
            Talk3 = talk3;
            PointY = pointY;
        }

        public DrawOrder(Character character, Name name, Balloons balloons, Talk talk, Talk talk2, Talk talk3, Talk talk4, float pointY)
        {
            Character = character;
            Name = name;
            Balloons = balloons;
            Talk = talk;
            Talk2 = talk2;
            Talk3 = talk3;
            Talk4 = talk4;
            PointY = pointY;
        }


        public DrawOrder(Character character, Name name, float pointY)
        {
            Character = character;
            Name = name;
            Balloons = null;
            PointY = pointY;
        }

        public int compareTo(DrawOrder drawOrder)
        {
            if(this.PointY < drawOrder.PointY)
            {
                return 1;
            }
            else if(this.PointY == drawOrder.PointY)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
    }

}
