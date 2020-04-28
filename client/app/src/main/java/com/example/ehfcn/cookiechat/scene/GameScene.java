package com.example.ehfcn.cookiechat.scene;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.ehfcn.cookiechat.R;
import com.example.ehfcn.cookiechat.UI.ItemUI;
import com.example.ehfcn.cookiechat.game.Item;
import com.example.ehfcn.cookiechat.game.Player;
import com.example.ehfcn.cookiechat.graphic.GLRenderer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import static com.example.ehfcn.cookiechat.scene.MainActivity.MyPREFERENCES;
import static com.example.ehfcn.cookiechat.scene.MainActivity.NicknameSession;

// 게임씬
@SuppressWarnings("JniMissingFunction")

public class GameScene extends AppCompatActivity
{
    // 전역으로 사용될 context를 정의합니다.
    public static Context context;
    // 로그인 세션으로 사용될 쉐어드를 정의합니다.
    SharedPreferences sharedpreferences;
    // 두번 뒤로가기 버튼을 누를 시 클라이언트를 종료할 핸들러를 정의합니다.
    private BackPressCloseHandler backPressCloseHandler;
    // 적절한 레이아웃 크기를 적용하기 위해 디스플레이 매트릭스를 정의합니다.
    WindowManager windowManager;
    DisplayMetrics metrics;
    ItemUI itemUI;

    public static ArrayList<Item> ItemList;

    private static final String TAG_JSON="webnautes";
    private static final String TAG_ITEM="itemnum";

    /*----------------------------------------------------------------------------------------*/

    public static boolean IsItem = false;

    /*----------------------------------------------------------------------------------------*/

    private GLSurfaceView mGLsurfaceView;

    // 그래픽스 랜더러를 정의합니다.
    private GLRenderer GLRenderer;

    // 서페이스뷰(캔버스)의 크기를 정의합니다.
    public static float swidth;
    public static float sheight;

    /*----------------------------------------------------------------------------------------*/

    // 연결할 서버의 포트번호를 정의합니다.
    private static int port = 5002;
    // 연결할 서버의 ip를 정의합니다.
    private static final String ipText = "115.71.237.45";

    public ClientTask clienttask;
    public Socket ct_sock;
    public DataInputStream ct_in;
    public DataOutputStream ct_out;
    public StringBuffer ct_buffer;
    public AsyncTask thisTask;

    /*----------------------------------------------------------------------------------------*/

    // 유저 목록 갱신에 사용될 리스트를 정의합니다.
    public static ArrayList<Player> CurrentUserList;
    public static ArrayList<Player> LastUserList;

    // 자신의 좌표를 정의합니다.
    public static float MyPosX;
    public static float MyPosY;

    /*----------------------------------------------------------------------------------------*/

    // 레이아웃 구성요소들을 정의합니다.
    TextView showText;
    ImageButton SendChatButton;
    EditText ChatBox;
    ImageButton Ent_btn;

    // UI갱신에 사용될 핸들러를 정의합니다.
    Handler msghandler;

    IsBalloonsThread isballoons;

    // 코드의 간결함을 위해 쉐어드에 저장된 세션을 재정의합니다.
    String UserNickName;

    // 클라이언트가 현재 연결되었는지 확인하기 위할 변수입니다. (별 쓸모없는것 같음)
    public static boolean result = false;

    // 채팅창 고정 여부를 정의합니다.
    boolean IsScroll = true;

    // 말풍선 출력 여부를 정의합니다.
    boolean IsChat = false;

    // 플레이어의 정보를 관리하는 객체입니다.
    public static Player player;

    // 말풍선 사라지는 시간을 재기 위한 변수입니다.
    public static int SleepTime = 0;

    // 중복로그인 처리에 사용됩니다.
    public boolean IsDual = false;

    static String Sendmessage = null;

    HeartThread heartThread;

    /*----------------------------------------------------------------------------------------*/

    private static final String SEPARATOR = "|"; // 구분자
    private static final int HEART_BEAT = 1000; // 하트비트

    private static final int REQ_LOGON = 1001; // 로그인
    private static final int REQ_WISPERSEND = 1022; // 귓속말 요청
    private static final int REQ_LOGOUT = 1041; // 로그아웃
    private static final int REQ_SENDWORDS = 1051; // 일반채팅
    private static final int REQ_CHARMOVE = 1061; // 캐릭터 움직임
    private static final int REQ_WORLDCHAT = 1071; // 전체채팅
    private static final int REQ_CHARCHANGE = 1081; // 캐릭터 옷 바꿈

    private static final int YES_LOGON = 2001; // 로그인 동기화
    private static final int YES_LOGOUT = 2041; // 로그아웃 동기화
    private static final int YES_SENDWORD = 2051; // 채팅 동기화

    private static final int ANS_CURRENTUSER = 1300; // 현재 접속한 유저 알림
    private static final int ANS_CHARMOVE =1141; // 캐릭터 움직임 응답
    private static final int ANS_DUPLICATION = 1200; // 중복로그인 알림
    private static final int ANS_NOTICE = 1100; // 서버로부터의 알림
    private static final int ANS_WISPERSEND_1 = 1122; // 귓속말 발신 응답
    private static final int ANS_WISPERSEND_2 = 1123; //귓속말 수신
    private static final int ANS_WORLDCHAT = 1171;

    private static final int ITEM_GEN = 1500;
    private static final int REQ_ITEMDEL = 1501;

    /*----------------------------------------------------------------------------------------*/

    MoveThreadX moveThreadX;
    MoveThreadY moveThreadY;

    public static boolean IsMoveX = false;
    public static boolean IsNew = false;

    WindowManager.LayoutParams params20;

    /*----------------------------------------------------------------------------------------*/

    TextView ItemNumText;

    public static int ItemPosX = 0;
    public static int ItemPosY = 0;

    // 화면의 해상도에 따라 레이아웃의 크기를 적절하게 조절합니다.
    // 게임씬의 레이아웃 최하단에 그래픽스를 적용할 서페이스뷰를 넣습니다
    // 그래픽스 카메라의 초기 좌표를 설정합니다.
    // 소켓을 생성합니다.
    // 서버에서 로그를 받을 경우 화면에 갱신할 핸들러를 설정합니다.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_scene_1);

        /*----------------------------------------------------------------------------------------*/
        // 세션으로 사용할 쉐어드를 선언합니다.
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        context = this;

        UserNickName = sharedpreferences.getString(NicknameSession, null);

        /*----------------------------------------------------------------------------------------*/
        // 레이아웃 UI에 유저 정보를 갱신합니다.
        TextView nickname = (TextView) findViewById(R.id.UserNickName);
        nickname.setText(sharedpreferences.getString(NicknameSession, null));
        nickname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/SDSwaggerTTF.ttf"));

        player = new Player(UserNickName);

        // 현재 유저 목록을 담을 리스트를 선언합니다.
        CurrentUserList = new ArrayList<Player>();

        ItemInfo itemInfo = new ItemInfo();
        itemInfo.execute();

        WearInfo wearInfo = new WearInfo();
        wearInfo.execute();

        /*----------------------------------------------------------------------------------------*/
        // 화면의 해상도에 따라 레이아웃의 크기를 적절하게 조절합니다.
        metrics = new DisplayMetrics();
        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        ImageView UIBar = (ImageView) findViewById(R.id.uibar);
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) UIBar.getLayoutParams();
        params.width = metrics.widthPixels;
        double heightsize = metrics.widthPixels/5.435;
        params.height = (int)heightsize;

        ImageView NameBar = (ImageView) findViewById(R.id.namebar);
        ViewGroup.LayoutParams params2 = (ViewGroup.LayoutParams) NameBar.getLayoutParams();
        params2.width = params.width/5;

        ImageButton ExitBtn = (ImageButton)findViewById(R.id.exit_btn);
        ViewGroup.LayoutParams params3 = (ViewGroup.LayoutParams) ExitBtn.getLayoutParams();
        params3.height = params.height-50;
        params3.width = params3.height;

        ImageView UIBar_bottom = (ImageView) findViewById(R.id.uibar_bottom);
        ViewGroup.LayoutParams params4 = (ViewGroup.LayoutParams) UIBar_bottom.getLayoutParams();
        params4.width = params.width;
        double heightsize2 = metrics.widthPixels/6.13;
        params4.height = (int)heightsize2;

        RelativeLayout textlayout = (RelativeLayout)  findViewById(R.id.textlayout);
        ViewGroup.LayoutParams params5 = (ViewGroup.LayoutParams) textlayout.getLayoutParams();
        params5.width = params.width;
        double heightsize3 = metrics.heightPixels/3;
        params5.height = (int)heightsize3;

        TextView ChatView = (TextView) findViewById(R.id.showtext);

        ViewGroup.MarginLayoutParams params6 = (ViewGroup.MarginLayoutParams) ChatView.getLayoutParams();
        params6.setMargins(0,0,0,(int)heightsize2);

        ChatView.setMovementMethod(ScrollingMovementMethod.getInstance());
        int TextSize = (int)params.width/50;
        if(TextSize < 15)
        {
            ChatView.setTextSize(15);
        }
        else if(TextSize > 20)
        {
            ChatView.setTextSize(20);
        }
        else
        {
            ChatView.setTextSize(TextSize);
        }
        backPressCloseHandler = new BackPressCloseHandler(this);

        ImageButton ChatGonButton = (ImageButton) this.findViewById(R.id.chatgone_btn);
        final ToggleButton ChatToggle = (ToggleButton) this.findViewById(R.id.ChatToggle);

        ViewGroup.LayoutParams params7 = (ViewGroup.LayoutParams) ChatGonButton.getLayoutParams();
        ViewGroup.LayoutParams params8 = (ViewGroup.LayoutParams) ChatToggle.getLayoutParams();

        itemUI = new ItemUI(this);
        params20 = new WindowManager.LayoutParams();
        params20.copyFrom(itemUI.getWindow().getAttributes());
        params20.width = params.width-100;
        params20.height = params.width-100;

        System.out.println("params20.width : "+params20.width);
        System.out.println("params20.height : " + params20.height);

        /*----------------------------------------------------------------------------------------*/
        // 게임씬의 레이아웃 최하단에 그래픽스를 적용할 캔버스를 넣습니다

        LinearLayout DrawingView = (LinearLayout) findViewById(R.id.Drawing);

        mGLsurfaceView = new GLSurfaceView(this);
        mGLsurfaceView.setEGLContextClientVersion(2);
        GLRenderer = new GLRenderer(this, this);
        mGLsurfaceView.setRenderer(GLRenderer);
        mGLsurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        DrawingView.addView(mGLsurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        /*----------------------------------------------------------------------------------------*/
        // 그래픽스의 초기 카메라 좌표입니다.

        GLRenderer.eyeX = 0.0f;
        GLRenderer.eyeY = 0.0f;

        /*----------------------------------------------------------------------------------------*/
        // 캔버스의 가로/세로사이즈를 구합니다.

        swidth = mGLsurfaceView.getWidth();
        sheight = mGLsurfaceView.getHeight();

        /*----------------------------------------------------------------------------------------*/

        ChatBox = (EditText) findViewById(R.id.chatbox);
        SendChatButton = (ImageButton) findViewById(R.id.ent_btn);
        showText = (TextView) findViewById(R.id.showtext);

        /*----------------------------------------------------------------------------------------*/

        // 엔터 버튼을 누를 시 서버로 채팅내용을 보냅니다.

        Ent_btn = (ImageButton) findViewById(R.id.ent_btn);
        Ent_btn.setOnClickListener(new View.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(View v)
                                       {
                                           if(ChatBox.getText().length() > 0)
                                           {
                                               IsChat = true;
                                               clienttask = new ClientTask();
                                               clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_SENDWORDS);
                                               Sendmessage = ChatBox.getText().toString();
                                               ChatBox.setText("");
                                               isballoons = new IsBalloonsThread(77777777);
                                               isballoons.start();
                                           }
                                       }
                                   }
        );

        ChatBox.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                switch(actionId)
                {
                    case EditorInfo.IME_ACTION_SEND:
                        IsChat = true;
                        clienttask = new ClientTask();
                        clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_SENDWORDS);
                        Sendmessage = ChatBox.getText().toString();
                        ChatBox.setText("");
                        isballoons = new IsBalloonsThread(77777777);
                        isballoons.start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        /*----------------------------------------------------------------------------------------*/
        // 서버에서 메시지를 받으면 처리하는 핸들러입니다.

        msghandler = new Handler()
        {
          @Override
            public void handleMessage(Message hdmsg)
            {

                // 서버와의 연결을 감지하여 불안정할 시 클라이언트를 종료합니다.
                if(hdmsg.what == 0)
                {
                    if(!GameScene.this.isFinishing())
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameScene.this);

                        alertDialogBuilder.setTitle("쿠키챗");
                        alertDialogBuilder.setMessage("서버와의 연결이 불안정하여 애플리케이션을 종료합니다.")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.cancel();
                                        finish();
                                    }
                                });

                        alertDialogBuilder.create();
                        alertDialogBuilder.show();
                    }
                }
            }
        };

        /*----------------------------------------------------------------------------------------*/
        // 채팅목록 갱신 여부를 설정하는 토글버튼입니다.

        ChatToggle.setText("");
        ChatToggle.setOnClickListener(new View.OnClickListener()
                                      {
                                          public void onClick(View v)
                                          {
                                              if(ChatToggle.isChecked())
                                              {
                                                  ChatToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_btn));
                                                  ChatToggle.setWidth(28);
                                                  ChatToggle.setHeight(28);
                                                  ChatToggle.setText("");
                                                  IsScroll = false;
                                              }
                                              else
                                              {
                                                  ChatToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause_btn_));
                                                  ChatToggle.setText("");
                                                  IsScroll = true;
                                              }
                                          }
                                      }
        );

        LastUserList = new ArrayList<Player>();
        ItemNumText = (TextView) findViewById(R.id.itemcount);

        ItemList = new ArrayList<Item>();
    }

    // 클라이언트에 로그인 세션이 존재하지 않을 경우 클라이언트를 종료합니다.
    @Override
    public void onStart()
    {
        super.onStart();

        if(sharedpreferences.getString(NicknameSession, null) == null)
        {
            if(!IsDual)
            {
                msghandler.sendEmptyMessage(0);
            }
        }

        mGLsurfaceView.requestRender();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mGLsurfaceView.onPause();

        IsNew = false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mGLsurfaceView.requestRender();
        mGLsurfaceView.onResume();
        GLRenderer.onResume();
    }

    // 뒤로가기 버튼을 두 번 누를시 종료되게끔 하는 메소드
    @Override
    public void onBackPressed()
    {
        backPressCloseHandler.onBackPressed();
    }

    // 정상종료시 서버로 로그아웃한다는 메시지를 보내고 클라이언트의 로그인 세션을 제거합니다.
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        clienttask = new ClientTask();
        clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_LOGOUT);

        /*-----------------------------------세션---------------------------------------------------*/

        // 정상종료시 클라이언트의 세션을 지웁니다.
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.clear();
        editor.commit();
    }

    // 뒤로가기 버튼을 두 번 누를시 종료되게끔 하는 메소드
    public class BackPressCloseHandler
    {
        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;

        public BackPressCloseHandler(Activity context)
        {
            this.activity = context;
        }

        public void onBackPressed()
        {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000)
            {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000)
            {
                activity.finish();
                toast.cancel();
            }
        }

        public void showGuide()
        {
            toast = Toast.makeText(activity,
                    "뒤로가기 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // 종료버튼을 눌렀을 경우 종료하는 대화상자가 뜨게끔 합니다.
    public void ExitButton(View v)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("쿠키챗");
        alertDialogBuilder.setMessage("쿠키챗을 종료하시겠어요?")
                .setCancelable(false)
                .setPositiveButton("네", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                        finish();
                    }
                }).
                setNegativeButton("아니요", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    // 채팅목록을 닫는 메소드
    public void CloseChatLog(View v)
    {
        //ImageView Chat_log = (ImageView) findViewById(R.id.chatbackground);
        //Chat_log.setVisibility(View.GONE);
        showText.setVisibility(View.GONE);
        ImageButton CloseChatLogBtn = (ImageButton) findViewById(R.id.chatgone_btn);
        CloseChatLogBtn.setVisibility(View.GONE);
        ImageButton OpenChatLogBtn = (ImageButton) findViewById(R.id.openchat_btn);
        OpenChatLogBtn.setVisibility(View.VISIBLE);
        final ToggleButton ChatToggle = (ToggleButton) this.findViewById(R.id.ChatToggle);
        ChatToggle.setVisibility(View.GONE);
    }

    // 채팅목록을 여는 메소드
    public void OpenChatLog(View v)
    {
        //ImageView Chat_log = (ImageView) findViewById(R.id.chatbackground);
        //Chat_log.setVisibility(View.VISIBLE);
        showText.setVisibility(View.VISIBLE);
        ImageButton CloseChatLogBtn = (ImageButton) findViewById(R.id.chatgone_btn);
        CloseChatLogBtn.setVisibility(View.VISIBLE);
        ImageButton OpenChatLogBtn = (ImageButton) findViewById(R.id.openchat_btn);
        OpenChatLogBtn.setVisibility(View.GONE);
        final ToggleButton ChatToggle = (ToggleButton) this.findViewById(R.id.ChatToggle);
        ChatToggle.setVisibility(View.VISIBLE);

    }

    // 채팅목록 갱신 시 스크롤이 최하단으로 내려가게끔 합니다.
    private void scrollBottom()
    {
        showText = (TextView) findViewById(R.id.showtext);

        if(showText != null)
        {
            if(showText.getLayout() != null)
            {
                int lineTop =  showText.getLayout().getLineTop(showText.getLineCount());
                int scrollY = lineTop - showText.getHeight();
                if (scrollY > 0)
                {
                    showText.scrollTo(0, scrollY);
                }
                else
                {
                    showText.scrollTo(0, 0);
                }
            }
        }

    }

    // 화면을 터치할 시 정규화 된 좌표로 캐릭터가 이동하는 메소드입니다.
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();
        float nx = 0;
        float ny = 0;
        switch (event.getAction())
        {
            case MotionEvent.ACTION_UP:

                System.out.println("----------------------------------------------------------");

                nx = ((((x / metrics.widthPixels) * 2) - 1)) * 150 + player.Currentpos.x;
                ny = ((-(((y / metrics.heightPixels) * 2) - 1)) * 250);

                if (nx < -230)
                {
                    nx = ((((x/metrics.widthPixels)* 2)-1))*150-200;
                }
                else if (nx > 230)
                {
                    nx = ((((x/metrics.widthPixels)* 2)-1))*150+200;
                }

                // 5<->1<->2<->3<->4<->5<->1

                Rect MovemapTo5From1 = new Rect(-343, 20, -303, -5);

                Rect MovemapTo2From1 = new Rect(-35, 153, 14, 108);
                Rect MovemapTo1From2 = new Rect(-168, 171, -52, 139);

                Rect MovemapTo3From2 = new Rect(218, 152, 326, 12);
                Rect MovemapTo1From4 = new Rect(269, -124, 322, -191);

                Rect MovemapTo4From3 = new Rect(-321, 74, -226, 28);
                Rect MovemapTo3From4 = new Rect(-344, 77, -260, 6);

                Rect MovemapTo5From4 = new Rect(303, 15, 350, -135);
                Rect MovemapTo4From5 = new Rect(-350, 118, -313, -182);

                Rect Map1Obstacle1 = new Rect(-400, 200, -10, 58);
                Rect Map1Obstacle2 = new Rect(237, 23, 400, -82);
                Rect Map1Obstacle3 = new Rect(183, -50, 400, -200);

                Rect Map2Obstacle1 = new Rect(-400, -28, -187, -200);
                Rect Map2Obstacle2 = new Rect(83, 200, 236, 8);
                Rect Map2Obstacle3 = new Rect(360, 200, 400, -80);

                Rect Map3Obstacle1 = new Rect(-204, 200, 400, -10);

                Rect Map4Obstacle1 = new Rect(130, 200, 400, 24);

                Rect Map5Obstacle1 = new Rect(-400, 200, 400, 166);
                Rect Map5Obstacle2 = new Rect(131, 130, 400, 60);
                Rect Map5Obstacle3 = new Rect(166, 65, 400, -49);
                Rect Map5Obstacle4 = new Rect(309, -65, 400, -119);

                ArrayList<Rect> ItemRects = new ArrayList<Rect>();

                for(int i = 0; i < ItemList.size(); i++)
                {
                    ItemRects.add(ItemList.get(i).ItemRect);
                }

                boolean IsItemGet = false;

                for(int i = 0; i < ItemRects.size(); i++)
                {
                    if(ItemRects.get(i).left < nx &&
                            nx < ItemRects.get(i).right &&
                            ItemRects.get(i).bottom < ny &&
                            ny < ItemRects.get(i).top)
                    {
                        clienttask = new ClientTask();
                        clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_ITEMDEL);
                        ItemList.clear();
                        mGLsurfaceView.requestRender();

                        GetItem getitem = new GetItem();
                        getitem.execute();

                        IsItemGet = true;
                    }
                }

                if(!IsItemGet)
                {
                    switch (player.CurrentMapNum)
                    {
                        case 1:
                            if (MovemapTo5From1.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo5From1.right &&
                                    MovemapTo5From1.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo5From1.top)
                            {
                                player.CurrentMapNum = 5;
                                player.Currentpos.x = 227;
                                player.Currentpos.y = -31;
                                MyPosX = 227;
                                MyPosY = -31;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if (MovemapTo2From1.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo2From1.right &&
                                    MovemapTo2From1.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo2From1.top)
                            {
                                player.CurrentMapNum = 2;
                                player.Currentpos.x = -16;
                                player.Currentpos.y = 156;
                                MyPosX = -16;
                                MyPosY = 156;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if(Map1Obstacle1.left < nx &&
                                    nx < Map1Obstacle1.right &&
                                    Map1Obstacle1.bottom < ny &&
                                    ny < Map1Obstacle1.top)
                            {
                            }
                            else if(Map1Obstacle2.left < nx &&
                                    nx < Map1Obstacle2.right &&
                                    Map1Obstacle2.bottom < ny &&
                                    ny < Map1Obstacle2.top)
                            {
                            }
                            else if(Map1Obstacle3.left < nx &&
                                    nx < Map1Obstacle3.right &&
                                    Map1Obstacle3.bottom < ny &&
                                    ny < Map1Obstacle3.top)
                            {
                            }
                            else
                            {
                                if(!player.IsMove)
                                {
                                    MyPosX = nx;
                                    MyPosY = ny;

                                    moveThreadX = new MoveThreadX((int)nx);
                                    moveThreadY = new MoveThreadY((int)ny);

                                    moveThreadX.start();
                                    moveThreadY.start();
                                }
                            }
                            break;

                        case 2:
                            if (MovemapTo1From2.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo1From2.right &&
                                    MovemapTo1From2.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo1From2.top)
                            {
                                player.CurrentMapNum = 1;
                                player.Currentpos.x = 84;
                                player.Currentpos.y = 91;
                                MyPosX = 84;
                                MyPosY = 91;
                                player.IsLeft = false;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if (MovemapTo3From2.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo3From2.right &&
                                    MovemapTo3From2.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo3From2.top)
                            {
                                player.CurrentMapNum = 3;
                                player.Currentpos.x = -292;
                                player.Currentpos.y = -57;
                                MyPosX = -292;
                                MyPosY = -57;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if(Map2Obstacle1.left < nx &&
                                    nx < Map2Obstacle1.right &&
                                    Map2Obstacle1.bottom < ny &&
                                    ny < Map2Obstacle1.top)
                            {
                            }
                            else if(Map2Obstacle2.left < nx &&
                                    nx < Map2Obstacle2.right &&
                                    Map2Obstacle2.bottom < ny &&
                                    ny < Map2Obstacle2.top)
                            {
                            }
                            else if(Map2Obstacle3.left < nx &&
                                    nx < Map2Obstacle3.right &&
                                    Map2Obstacle3.bottom < ny &&
                                    ny < Map2Obstacle3.top)
                            {
                            }
                            else
                            {
                                if(!player.IsMove)
                                {
                                    MyPosX = nx;
                                    MyPosY = ny;

                                    moveThreadX = new MoveThreadX((int)nx);
                                    moveThreadY = new MoveThreadY((int)ny);

                                    moveThreadX.start();
                                    moveThreadY.start();
                                }
                            }
                            break;

                        case 3:
                            if (MovemapTo4From3.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo4From3.right &&
                                    MovemapTo4From3.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo4From3.top) {
                                player.CurrentMapNum = 4;
                                player.Currentpos.x = -230;
                                player.Currentpos.y = -50;
                                MyPosX = -252;
                                MyPosY = -22;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if(Map3Obstacle1.left < nx &&
                                    nx < Map3Obstacle1.right &&
                                    Map3Obstacle1.bottom < ny &&
                                    ny < Map3Obstacle1.top)
                            {
                            }
                            else
                            {
                                if(!player.IsMove)
                                {
                                    MyPosX = nx;
                                    MyPosY = ny;

                                    moveThreadX = new MoveThreadX((int)nx);
                                    moveThreadY = new MoveThreadY((int)ny);

                                    moveThreadX.start();
                                    moveThreadY.start();
                                }
                            }
                            break;

                        case 4:
                            if (MovemapTo3From4.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo3From4.right &&
                                    MovemapTo3From4.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo3From4.top)
                            {
                                player.CurrentMapNum = 3;
                                player.Currentpos.x = 18;
                                player.Currentpos.y = -41;
                                MyPosX = 18;
                                MyPosY = -41;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if (MovemapTo5From4.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo5From4.right &&
                                    MovemapTo5From4.bottom <player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo5From4.top)
                            {
                                player.CurrentMapNum = 5;
                                player.Currentpos.x = -304;
                                player.Currentpos.y = -26;
                                MyPosX = -304;
                                MyPosY = -26;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if(Map4Obstacle1.left < nx &&
                                    nx < Map4Obstacle1.right &&
                                    Map4Obstacle1.bottom < ny &&
                                    ny < Map4Obstacle1.top)
                            {
                            }
                            else
                            {
                                if(!player.IsMove)
                                {
                                    MyPosX = nx;
                                    MyPosY = ny;

                                    moveThreadX = new MoveThreadX((int)nx);
                                    moveThreadY = new MoveThreadY((int)ny);

                                    moveThreadX.start();
                                    moveThreadY.start();
                                }
                            }
                            break;

                        case 5:
                            if (MovemapTo1From4.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo1From4.right &&
                                    MovemapTo1From4.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo1From4.top)
                            {
                                player.CurrentMapNum = 1;
                                player.Currentpos.x = -295;
                                player.Currentpos.y = -26;
                                MyPosX = -295;
                                MyPosY = -26;
                                player.IsLeft = true;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if (MovemapTo4From5.left < player.Currentpos.x &&
                                    player.Currentpos.x < MovemapTo4From5.right &&
                                    MovemapTo4From5.bottom < player.Currentpos.y &&
                                    player.Currentpos.y < MovemapTo4From5.top)
                            {
                                player.CurrentMapNum = 4;
                                player.Currentpos.x = 288;
                                player.Currentpos.y = -40;
                                MyPosX = 288;
                                MyPosY = -40;
                                GLRenderer.eyeX = player.Currentpos.x;
                                GLRenderer.GenUser();
                                mGLsurfaceView.requestRender();
                                clienttask = new ClientTask();
                                clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                            }
                            else if(Map5Obstacle1.left < nx &&
                                    nx < Map5Obstacle1.right &&
                                    Map5Obstacle1.bottom < ny &&
                                    ny < Map5Obstacle1.top)
                            {
                            }
                            else if(Map5Obstacle2.left < nx &&
                                    nx < Map5Obstacle2.right &&
                                    Map5Obstacle2.bottom < ny &&
                                    ny < Map5Obstacle2.top)
                            {
                            }
                            else if(Map5Obstacle3.left < nx &&
                                    nx < Map5Obstacle3.right &&
                                    Map5Obstacle3.bottom < ny &&
                                    ny < Map5Obstacle3.top)
                            {
                            }
                            else if(Map5Obstacle4.left < nx &&
                                    nx < Map5Obstacle4.right &&
                                    Map5Obstacle4.bottom < ny &&
                                    ny < Map5Obstacle4.top)
                            {
                            }
                            else
                            {
                                if(!player.IsMove)
                                {
                                    MyPosX = nx;
                                    MyPosY = ny;

                                    moveThreadX = new MoveThreadX((int)nx);
                                    moveThreadY = new MoveThreadY((int)ny);

                                    moveThreadX.start();
                                    moveThreadY.start();
                                }
                            }
                            break;

                    }
                    break;
                }
        }
        return true;
    }

    // 서버와 통신하는 AsyncTask입니다.
    public class ClientTask extends AsyncTask<Integer, Integer, Long>
    {
        String UpdateChatString;

        public ClientTask()
        {
            super();
        }

        // UI 스레드상에서 실행되며 doInBackground 메소드 전에 호출됩니다.
        // doInBackground 메소드가 실행되기 전에 프로그레스바를 보여주는 등, 초기화 작업을 하는 데 사용합니다.
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        // 이 메소드에 포함된 코드는 백그라운드 스레드 상에서 처리됩니다. 때문에 이곳에서 UI처리를 하면 안됩니다.
        // onPreExecute() 메소드 종료 후 바로 호출됩니다.
        // AsyncTask의 execute 메소드를 호출 시 전달한 인자를 파라메터로 받게 됩니다. 값을 리턴하면 onPostExecute 메소드에서 파라메터로 받게 됩니다.
        @Override
        protected Long doInBackground(Integer... params)
        {
            switch(params[0])
            {
                case REQ_LOGON :
                {
                    try
                    {
                        ct_sock = new Socket(ipText, port);
                        ct_in = new DataInputStream(ct_sock.getInputStream());
                        ct_out = new DataOutputStream(ct_sock.getOutputStream());
                        ct_buffer = new StringBuffer(4096);
                        thisTask = this;
                        requestLogon();

                        heartThread = new HeartThread(ct_sock);
                        heartThread.start();

                        while(true)
                        {
                            String recvData = ct_in.readUTF();
                            System.out.println("recvdata : "+Integer.getInteger(recvData));
                            StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
                            int command = Integer.parseInt(st.nextToken());

                            if(recvData != null)
                            {
                                switch(command)
                                {
                                    case YES_LOGON :
                                    {
                                        String LoginUserNickname = st.nextToken();
                                        UpdateChatString = "[알림] "+LoginUserNickname+"님이 접속하였습니다.";
                                        publishProgress(YES_LOGON);
                                    }
                                    break;

                                    case YES_SENDWORD :
                                    {
                                        String id = st.nextToken();
                                        String SendWord = null;
                                        if(st.hasMoreTokens())
                                        {
                                            int roomNumber = Integer.parseInt(st.nextToken());
                                        }
                                        if(st.hasMoreTokens())
                                        {
                                            SendWord = st.nextToken();
                                        }
                                        if(id.equals(UserNickName))
                                        {
                                            player.ChatString = SendWord;
                                        }
                                        try
                                        {
                                            String data = SendWord;
                                            UpdateChatString = "[일반] "+id+" : "+data;

                                            for(int i = 0; i < CurrentUserList.size(); i++)
                                            {
                                                if(CurrentUserList.get(i).ID.equals(id))
                                                {
                                                    if(!(UserNickName.equals(id)))
                                                    {
                                                        for(int j = 0; j < CurrentUserList.size(); j++)
                                                        {
                                                            System.out.print(CurrentUserList.get(j).ID+",");
                                                        }
                                                        System.out.println("UserNickName : " + UserNickName);
                                                        System.out.println("SendUserNickname : "+ id);
                                                        CurrentUserList.get(i).ChatString = SendWord;
                                                        isballoons = new IsBalloonsThread(i);
                                                        isballoons.start();
                                                    }
                                                }
                                            }
                                            publishProgress(YES_SENDWORD);
                                        }
                                        catch(NoSuchElementException e)
                                        {
                                            break;
                                        }
                                    }
                                    break;

                                    case ANS_CURRENTUSER :
                                    {
                                        LastUserList.clear();

                                        for(int i = 0 ; i < CurrentUserList.size(); i++)
                                        {
                                            LastUserList.add(CurrentUserList.get(i));
                                        }

                                        CurrentUserList.clear();

                                        ArrayList<Player> TempArray = new ArrayList<>();

                                        while(st.hasMoreTokens())
                                        {
                                            Player uplayer;
                                            uplayer = new Player(st.nextToken());
                                            uplayer.Currentpos.x = Float.parseFloat(st.nextToken());
                                            uplayer.Currentpos.y = Float.parseFloat(st.nextToken());
                                            uplayer.IsLeft = Boolean.parseBoolean(st.nextToken());
                                            uplayer.WearItem = Integer.parseInt(st.nextToken());
                                            TempArray.add(uplayer);
                                        }

                                        for(int i = 0; i < TempArray.size(); i++)
                                        {
                                            if(TempArray.get(i).ID.equals(UserNickName))
                                            {
                                                TempArray.remove(i);
                                            }
                                        }

                                        for(int i = 0; i < TempArray.size(); i++)
                                        {
                                            Player uplayer;
                                            uplayer = new Player(TempArray.get(i).ID);
                                            if(LastUserList.size() == TempArray.size())
                                            {
                                                if(LastUserList.get(i).ID.equals(TempArray.get(i).ID))
                                                {
                                                    uplayer.Currentpos.x = LastUserList.get(i).Currentpos.x;
                                                    uplayer.Currentpos.y = LastUserList.get(i).Currentpos.y;
                                                    uplayer.IsLeft = TempArray.get(i).IsLeft;
                                                    uplayer.WearItem = TempArray.get(i).WearItem;
                                                }
                                                else
                                                {
                                                    uplayer.Currentpos.x = TempArray.get(i).Currentpos.x;
                                                    uplayer.Currentpos.y = TempArray.get(i).Currentpos.y;
                                                    uplayer.IsLeft = TempArray.get(i).IsLeft;
                                                    uplayer.WearItem = TempArray.get(i).WearItem;
                                                }

                                            }
                                            else
                                            {
                                                uplayer.Currentpos.x = TempArray.get(i).Currentpos.x;
                                                uplayer.Currentpos.y = TempArray.get(i).Currentpos.y;
                                                uplayer.IsLeft = TempArray.get(i).IsLeft;
                                                uplayer.WearItem = TempArray.get(i).WearItem;
                                            }
                                            CurrentUserList.add(uplayer);
                                        }
                                        publishProgress(ANS_CURRENTUSER);

                                        for(int i = 0 ; i < CurrentUserList.size(); i ++)
                                        {
                                            if((int)CurrentUserList.get(i).Currentpos.x != (int)TempArray.get(i).Currentpos.x)
                                            {
                                                uMoveThreadX uMoveThreadX = new uMoveThreadX((int)TempArray.get(i).Currentpos.x, i);
                                                uMoveThreadX.start();
                                            }
                                            if((int)CurrentUserList.get(i).Currentpos.y != (int)TempArray.get(i).Currentpos.y)
                                            {
                                                uMoveThreadY uMoveThreadY = new uMoveThreadY((int)TempArray.get(i).Currentpos.y, i);
                                                uMoveThreadY.start();
                                            }
                                        }

                                        System.out.print("이전 유저 리스트 ::: ");

                                        for(int i = 0; i < LastUserList.size(); i++)
                                        {
                                            System.out.print(LastUserList.get(i).ID + " :: " + "X좌표 : " + LastUserList.get(i).Currentpos.x + "Y좌표 : " + LastUserList.get(i).Currentpos.y + " //// ");
                                        }

                                        System.out.println("");

                                        System.out.print("현재 유저 리스트 ::: ");

                                        for(int i = 0; i < CurrentUserList.size(); i++)
                                        {
                                            System.out.print(CurrentUserList.get(i).ID + " :: " + "X좌표 : " + CurrentUserList.get(i).Currentpos.x + "Y좌표 : " + CurrentUserList.get(i).Currentpos.y + " //// ");
                                        }

                                        System.out.println("");

                                        break;
                                    }

                                    case YES_LOGOUT :
                                    {
                                        String LogoutChar;
                                        LogoutChar = st.nextToken();

                                        for(int i = 0; i < CurrentUserList.size(); i++)
                                        {
                                            if(CurrentUserList.get(i).ID.equals(LogoutChar))
                                            {
                                                CurrentUserList.remove(i);
                                            }
                                        }

                                        for(int i = 0; i < GLRenderer.uCharacter.size(); i++)
                                        {
                                            if(GLRenderer.uCharacter.get(i).NickName.equals(LogoutChar))
                                            {
                                                GLRenderer.uCharacter.remove(i);
                                            }
                                        }

                                        UpdateChatString = "[알림] "+LogoutChar+"님이 접속 종료하였습니다.";
                                        publishProgress(YES_LOGOUT);
                                        break;
                                    }

                                    case ANS_DUPLICATION :
                                    {
                                        publishProgress(ANS_DUPLICATION);
                                        break;
                                    }

                                    case ANS_NOTICE :
                                    {
                                        UpdateChatString ="[알림] "+st.nextToken();
                                        publishProgress(ANS_NOTICE);
                                        break;
                                    }

                                    case ANS_WISPERSEND_1 :
                                    {
                                        String WisperReceiveUserNickname = st.nextToken();
                                        String WisperSendmessage = null;

                                        if(st.hasMoreTokens())
                                        {
                                            WisperSendmessage = st.nextToken();
                                        }

                                        UpdateChatString = "["+WisperReceiveUserNickname+"에게 귓속말] : "+WisperSendmessage;
                                        publishProgress(ANS_WISPERSEND_1);
                                        break;
                                    }

                                    case ANS_WISPERSEND_2 :
                                    {
                                        String WisperSendNickname = st.nextToken();
                                        String WisperRecivemessage = null;
                                        if(st.hasMoreTokens())
                                        {
                                            WisperRecivemessage = st.nextToken();
                                        }

                                        UpdateChatString =  "["+WisperSendNickname+"의 귓속말] : "+WisperRecivemessage;
                                        publishProgress(ANS_WISPERSEND_2);
                                        break;
                                    }

                                    case ANS_WORLDCHAT :
                                    {
                                        String id = st.nextToken();
                                        String sendMessege = st.nextToken();

                                        UpdateChatString = "[전체] "+id+" : "+sendMessege;
                                        publishProgress(ANS_WORLDCHAT);
                                        break;
                                    }

                                    case ITEM_GEN :
                                    {
                                        ItemList.clear();
                                        System.out.println("아이템 젠 ");
                                        publishProgress(ANS_CURRENTUSER);
                                        while(st.hasMoreTokens())
                                        {
                                            int PosX = Integer.parseInt(st.nextToken());
                                            int PosY = Integer.parseInt(st.nextToken());
                                            Item item = new Item(PosX, PosY);
                                            ItemList.add(item);
                                            publishProgress(ANS_CURRENTUSER);
                                        }
                                        System.out.println("현재 아이템 갯수 : " + ItemList.size());
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                if(!IsDual)
                                {
                                    msghandler.sendEmptyMessage(0);
                                }
                            }
                        }
                    }
                    catch(IOException e)
                    {
                        if(!IsDual)
                        {
                            msghandler.sendEmptyMessage(0);
                        }
                    }
                    break;
                }

                case REQ_SENDWORDS :
                {
                    StringTokenizer st = new StringTokenizer(Sendmessage, " ");
                    StringTokenizer st2 = new StringTokenizer(Sendmessage, " ");
                    StringTokenizer st3 = new StringTokenizer(Sendmessage, " ");
                    StringTokenizer st4 = new StringTokenizer(Sendmessage, " ");

                    if(st.hasMoreTokens())
                    {
                        if(st.nextToken().equals("/w"))
                        {
                            if(st.hasMoreTokens())
                            {
                                String WUserNickName = st.nextToken();
                                if(st.hasMoreTokens())
                                {
                                    String Wmessage = st.nextToken();
                                    while(st.hasMoreTokens())
                                    {
                                        Wmessage = Wmessage + " " + st.nextToken();
                                    }
                                    try
                                    {
                                        ct_buffer.setLength(0);
                                        ct_buffer.append(REQ_WISPERSEND);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(UserNickName);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(WUserNickName);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(Wmessage);
                                        send(ct_buffer.toString());
                                    }
                                    catch(IOException e)
                                    {
                                        System.out.println(e);
                                    }

                                }
                            }
                        }
                        else if(st2.nextToken().equals("/귓"))
                        {
                            if(st.hasMoreTokens())
                            {
                                String WUserNickName = st.nextToken();
                                if(st.hasMoreTokens())
                                {
                                    String Wmessage = st.nextToken();
                                    while(st.hasMoreTokens())
                                    {
                                        Wmessage = Wmessage + " " + st.nextToken();
                                    }
                                    try
                                    {
                                        ct_buffer.setLength(0);
                                        ct_buffer.append(REQ_WISPERSEND);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(UserNickName);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(WUserNickName);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(Wmessage);
                                        send(ct_buffer.toString());
                                    }
                                    catch(IOException e)
                                    {
                                        System.out.println(e);
                                    }

                                }
                            }
                        }
                        else if(st3.nextToken().equals("/ㅈ"))
                        {
                            if(st.hasMoreTokens())
                            {
                                String WUserNickName = st.nextToken();
                                if(st.hasMoreTokens())
                                {
                                    String Wmessage = st.nextToken();
                                    while(st.hasMoreTokens())
                                    {
                                        Wmessage = Wmessage + " " + st.nextToken();
                                    }
                                    try
                                    {
                                        ct_buffer.setLength(0);
                                        ct_buffer.append(REQ_WISPERSEND);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(UserNickName);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(WUserNickName);
                                        ct_buffer.append(SEPARATOR);
                                        ct_buffer.append(Wmessage);
                                        send(ct_buffer.toString());
                                    }
                                    catch(IOException e)
                                    {
                                        System.out.println(e);
                                    }

                                }
                            }
                        }
                        else if(st4.nextToken().equals("/전"))
                        {
                            if(st.hasMoreTokens())
                            {
                                String Wmessage = st.nextToken();
                                while(st.hasMoreTokens())
                                {
                                    Wmessage = Wmessage + " " + st.nextToken();
                                }
                                try
                                {
                                    ct_buffer.setLength(0);
                                    ct_buffer.append(REQ_WORLDCHAT);
                                    ct_buffer.append(SEPARATOR);
                                    ct_buffer.append(UserNickName);
                                    ct_buffer.append(SEPARATOR);
                                    ct_buffer.append(Wmessage);
                                    send(ct_buffer.toString());
                                }
                                catch(IOException e)
                                {
                                    System.out.println(e);
                                }

                            }
                        }
                        else
                        {
                            try
                            {
                                ct_buffer.setLength(0);
                                ct_buffer.append(REQ_SENDWORDS);
                                ct_buffer.append(SEPARATOR);
                                ct_buffer.append(UserNickName);
                                ct_buffer.append(SEPARATOR);
                                ct_buffer.append(player.CurrentMapNum);
                                ct_buffer.append(SEPARATOR);
                                ct_buffer.append(Sendmessage);
                                send(ct_buffer.toString());
                            }
                            catch(IOException e)
                            {
                                System.out.println(e);
                            }
                        }
                    }
                    else
                    {
                        try
                        {
                            ct_buffer.setLength(0);
                            ct_buffer.append(REQ_SENDWORDS);
                            ct_buffer.append(SEPARATOR);
                            ct_buffer.append(UserNickName);
                            ct_buffer.append(SEPARATOR);
                            ct_buffer.append(player.CurrentMapNum);
                            ct_buffer.append(SEPARATOR);
                            ct_buffer.append(Sendmessage);
                            send(ct_buffer.toString());
                        }
                        catch(IOException e)
                        {
                            System.out.println(e);
                        }
                    }
                    break;
                }

                case REQ_LOGOUT :
                {
                    if(ct_buffer != null)
                    {
                        try
                        {
                            ct_buffer.setLength(0);
                            ct_buffer.append(REQ_LOGOUT);
                            ct_buffer.append(SEPARATOR);
                            ct_buffer.append(UserNickName);
                            send(ct_buffer.toString());
                        }
                        catch(IOException e)
                        {
                            System.out.println(e);
                        }
                    }

                    break;
                }

                case REQ_ITEMDEL :
                {
                    try
                    {
                        ct_buffer.setLength(0);
                        ct_buffer.append(REQ_ITEMDEL);
                        ct_buffer.append(SEPARATOR);
                        ct_buffer.append(player.CurrentMapNum);
                        send(ct_buffer.toString());
                    }
                    catch(IOException e)
                    {
                        System.out.println(e);
                    }
                    break;
                }

                case REQ_CHARMOVE :
                {
                    try
                    {
                        ct_buffer.setLength(0);
                        ct_buffer.append(REQ_CHARMOVE);
                        ct_buffer.append(SEPARATOR);
                        ct_buffer.append(UserNickName);
                        ct_buffer.append(SEPARATOR);
                        ct_buffer.append(MyPosX);
                        ct_buffer.append(SEPARATOR);
                        ct_buffer.append(MyPosY);
                        ct_buffer.append(SEPARATOR);
                        ct_buffer.append(player.IsLeft);
                        ct_buffer.append(SEPARATOR);
                        ct_buffer.append(player.CurrentMapNum);
                        ct_buffer.append(SEPARATOR);
                        ct_buffer.append(player.WearItem);
                        send(ct_buffer.toString());
                    }
                    catch(IOException e)
                    {
                        System.out.println(e);
                    }
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong)
        {
            super.onPostExecute(aLong);
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            SpannableStringBuilder builder;
            String CurrentSendMessage;
            int StringSize;

            switch(values[0])
            {
                case YES_LOGON :
                {
                    CurrentSendMessage = "\r\n" + " " + UpdateChatString;
                    builder = new SpannableStringBuilder(CurrentSendMessage);
                    StringSize = CurrentSendMessage.length();
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFAE00")), 0, StringSize, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showText.append(builder);

                    if(IsScroll)
                    {
                        scrollBottom();
                    }
                    break;
                }

                case YES_SENDWORD:
                {
                    CurrentSendMessage = "\r\n" + " " + UpdateChatString;
                    builder = new SpannableStringBuilder(CurrentSendMessage);
                    StringSize = CurrentSendMessage.length();
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFF")), 0, StringSize, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showText.append(builder);
                    mGLsurfaceView.requestRender();

                    if(IsScroll)
                    {
                        scrollBottom();
                    }
                    break;
                }

                case ANS_CURRENTUSER:
                {
                    mGLsurfaceView.requestRender();
                    break;
                }

                case YES_LOGOUT :
                {
                    CurrentSendMessage =   "\r\n" + " " + UpdateChatString;
                    builder = new SpannableStringBuilder(CurrentSendMessage);
                    StringSize = CurrentSendMessage.length();
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFAE00")), 0, StringSize, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showText.append(builder);

                    if(IsScroll)
                    {
                        scrollBottom();
                    }

                    break;
                }
                case ANS_DUPLICATION :
                {
                    IsDual = true;
                    if(!GameScene.this.isFinishing())
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameScene.this);

                        alertDialogBuilder.setTitle("쿠키챗");
                        alertDialogBuilder.setMessage("서버와의 연결이 불안정하여 애플리케이션을 종료합니다.")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.cancel();
                                        finish();
                                    }
                                });

                        alertDialogBuilder.create();
                        alertDialogBuilder.show();
                    }
                    break;
                }

                case ANS_NOTICE :
                {
                    CurrentSendMessage = "\r\n" + " " + UpdateChatString;
                    builder = new SpannableStringBuilder(CurrentSendMessage);
                    StringSize = CurrentSendMessage.length();
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FFAE00")), 0, StringSize, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showText.append(builder);

                    if(IsScroll)
                    {
                        scrollBottom();
                    }
                    break;
                }

                case ANS_WISPERSEND_1 :
                {
                    CurrentSendMessage = "\r\n" + " " + UpdateChatString;
                    builder = new SpannableStringBuilder(CurrentSendMessage);
                    StringSize = CurrentSendMessage.length();
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FF84FA")), 0, StringSize, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showText.append(builder);

                    if(IsScroll)
                    {
                        scrollBottom();
                    }
                    break;
                }

                case ANS_WISPERSEND_2 :
                {
                    CurrentSendMessage = "\r\n" + " " + UpdateChatString;
                    builder = new SpannableStringBuilder(CurrentSendMessage);
                    StringSize = CurrentSendMessage.length();
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#FF84FA")), 0, StringSize, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showText.append(builder);

                    if(IsScroll)
                    {
                        scrollBottom();
                    }
                    break;
                }

                case ANS_WORLDCHAT :
                {
                    CurrentSendMessage = "\r\n" + " " + UpdateChatString;
                    builder = new SpannableStringBuilder(CurrentSendMessage);
                    StringSize = CurrentSendMessage.length();
                    builder.setSpan(new ForegroundColorSpan(Color.parseColor("#D7D967")), 0, StringSize, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showText.append(builder);
                }
                    break;
            }
        }

        @Override
        protected void onCancelled(Long aLong)
        {
            super.onCancelled(aLong);
        }

        @Override
        protected void onCancelled()
        {
            super.onCancelled();
        }

        public void requestLogon()
        {
            if(UserNickName != null)
            {
                try
                {
                    ct_buffer.setLength(0);
                    ct_buffer.append(REQ_LOGON);
                    ct_buffer.append(SEPARATOR);
                    ct_buffer.append(UserNickName);
                    ct_buffer.append(SEPARATOR);
                    ct_buffer.append(player.WearItem);
                    send(ct_buffer.toString());
                }
                catch(IOException e)
                {
                    System.out.println(e);
                }
            }
            else
            {
                if(!IsDual)
                {
                    msghandler.sendEmptyMessage(0);
                }
            }
        }

        public void send(String sendData) throws IOException
        {
            ct_out.writeUTF(sendData);
            ct_out.flush();
        }
    }

    // 채팅 후 말풍선 노출 여부를 제어하는 스레드입니다.
    public class IsBalloonsThread extends Thread
    {
        private int UserNum;

        public IsBalloonsThread(int userNum)
        {
            this.UserNum = userNum;
        }

        public void run()
        {
            try
            {
                SleepTime+=5000;
                System.out.println("SleepTime : "+SleepTime);
                mGLsurfaceView.requestRender();
                sleep(SleepTime);
                if(UserNum == 77777777)
                {
                    player.ChatString = null;
                }
                else
                {
                    try
                    {
                        CurrentUserList.get(UserNum).ChatString = null;
                    }
                    catch(IndexOutOfBoundsException e)
                    {
                        System.out.println(e);
                    }
                }
                mGLsurfaceView.requestRender();
                SleepTime = 0;
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

    // 서버로 현재 연결되어있다는 데이터만을 보내는 스레드입니다.
    class HeartThread extends Thread
    {
        private Socket Socket = null;

        public HeartThread(Socket socket)
        {
            this.Socket = socket;
        }

        public void run()
        {
            try
            {
                while(ct_buffer != null)
                {
                    try
                    {
                        sleep(1000);
                        ct_buffer.setLength(0);
                        ct_buffer.append(HEART_BEAT);
                        ct_out.writeUTF(ct_buffer.toString());
                        ct_out.flush();
                    }
                    catch(InterruptedException e)
                    {
                        System.out.println("예외처리됨");
                        e.printStackTrace();
                        break;
                    }
                }

            }
            catch (IOException e)
            {
                if(!IsDual)
                {
                    msghandler.sendEmptyMessage(0);
                }
                e.printStackTrace();
            }
            catch(NullPointerException npe)
            {
                if(!IsDual)
                {
                    msghandler.sendEmptyMessage(0);
                }
                npe.printStackTrace();
            }
        }
    }

    // 내 캐릭터의 X축 이동 스레드
    class MoveThreadX extends Thread
    {
        public int nx;

        public MoveThreadX(int Nx)
        {
            nx = Nx;
        }
        public void run()
        {
                if (player.Currentpos.x > nx)
                {
                    player.IsMove = true;
                    IsMoveX = true;
                    player.IsLeft = true;
                    clienttask = new ClientTask();
                    clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);

                    while (player.Currentpos.x > nx)
                    {
                        try
                        {
                            sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            break;
                        }
                        player.Currentpos.x -= 15;

                        if(nx < -230)
                        {
                            GLRenderer.eyeX = -230;
                        }
                        else if(nx > 230)
                        {
                            GLRenderer.eyeX = 230;
                        }
                        else
                        {
                            GLRenderer.eyeX = player.Currentpos.x;
                        }

                        mGLsurfaceView.requestRender();
                    }
                    player.Currentpos.x = MyPosX;
                    player.IsMove = false;
                    IsMoveX = false;
                }
                else
                {
                    player.IsMove = true;
                    IsMoveX = true;
                    player.IsLeft = false;

                    clienttask = new ClientTask();
                    clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);

                    while (player.Currentpos.x < (int) nx)
                    {
                        try
                        {
                            sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            break;
                        }
                        player.Currentpos.x += 15;

                        if(nx < -230)
                        {
                            GLRenderer.eyeX = -230;
                        }
                        else if(nx > 230)
                        {
                            GLRenderer.eyeX = 230;
                        }
                        else
                        {
                            GLRenderer.eyeX = player.Currentpos.x;
                        }

                        mGLsurfaceView.requestRender();
                    }
                    player.Currentpos.x = MyPosX;
                    player.IsMove = false;
                    IsMoveX = false;
                    mGLsurfaceView.requestRender();
                }

            System.out.println("카메라 :: 캐릭터 최종 좌표 : " + (int) nx);
            //System.out.println("카메라 :: 캐릭터 현재 x좌표 (감소) : " + GLRenderer.mCharacter.translation.x);
            System.out.println("카메라 :: 캐릭터 현재 x좌표 (감소) : " + player.Currentpos.x);
            System.out.println("카메라 :: 현재 카메라 x좌표 : "+GLRenderer.eyeX);
        }
    }

    // 내 캐릭터의 Y축 이동 스레드
    class MoveThreadY extends Thread
    {
        public int ny;
        public MoveThreadY(int Ny)
        {
            ny = Ny;
        }
        public void run()
        {
            if (player.Currentpos.y > (int) ny)
            {
                //player.IsMove = true;

                if(!IsMoveX)
                {
                    clienttask = new ClientTask();
                    clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                }

                while (player.Currentpos.y > (int) ny)
                {
                    try
                    {
                        sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        break;
                    }

                    player.Currentpos.y -= 15;

                    if(!IsMoveX)
                    {
                    }
                    //System.out.println("캐릭터 이전 좌표 : " + lastPosition);
                    System.out.println("캐릭터 최종 좌표 : " + (int) ny);
                    System.out.println("캐릭터 현재 y좌표 (감소) : " + player.Currentpos.y);
                    mGLsurfaceView.requestRender();
                }
                player.Currentpos.y = MyPosY;
                //player.IsMove = false;
            }
            else
            {
                //player.IsMove = true;

                if(!IsMoveX)
                {
                    clienttask = new ClientTask();
                    clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
                }

                while (player.Currentpos.y < (int) ny)
                {
                    try
                    {
                        sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        break;
                    }
                    player.Currentpos.y += 15;

                    if(!IsMoveX)
                    {
                    }
                    //System.out.println("캐릭터 이전 좌표 : " + lastPosition);
                    System.out.println("캐릭터 최종 좌표 : " + (int) ny);
                    System.out.println("캐릭터 현재 y좌표 (증가) : " + player.Currentpos.y);
                    mGLsurfaceView.requestRender();
                }
                //player.IsMove = false;
                player.Currentpos.y = MyPosY;
                mGLsurfaceView.requestRender();
            }
        }
    }

    // 다른 캐릭터의 X축 이동 스레드
    public class uMoveThreadX extends Thread
    {
        public int MoveX;
        public int CharNum;

        public uMoveThreadX(int moveX, int charNum)
        {
            MoveX = moveX;
            CharNum = charNum;
        }

        public void run()
        {
            try
            {
                if(CurrentUserList.get(CharNum).Currentpos.x > MoveX)
                {
                    CurrentUserList.get(CharNum).IsMove = true;

                    try
                    {
                        while(CurrentUserList.get(CharNum).Currentpos.x > MoveX)
                        {
                            CurrentUserList.get(CharNum).IsMove = true;
                            try
                            {
                                sleep(100);
                            }
                            catch(InterruptedException e)
                            {
                                break;
                            }
                            CurrentUserList.get(CharNum).Currentpos.x -= 15;

                            System.out.println(CurrentUserList.get(CharNum).ID+"의 캐릭터 최종 좌표 : " + (int) MoveX);
                            System.out.println(CurrentUserList.get(CharNum).ID+"의 캐릭터 현재 x좌표 (감소) : " + CurrentUserList.get(CharNum).Currentpos.x);
                            mGLsurfaceView.requestRender();
                        }
                        CurrentUserList.get(CharNum).Currentpos.x = MoveX;
                        CurrentUserList.get(CharNum).IsMove = false;
                        mGLsurfaceView.requestRender();
                    }
                    catch(IndexOutOfBoundsException e)
                    {
                        System.out.println("예외");
                    }
                }
                else
                {
                    CurrentUserList.get(CharNum).IsMove = true;

                    try
                    {
                        while(CurrentUserList.get(CharNum).Currentpos.x < MoveX)
                        {
                            CurrentUserList.get(CharNum).IsMove = true;
                            try
                            {
                                sleep(100);
                            }
                            catch(InterruptedException e)
                            {
                                break;
                            }
                            CurrentUserList.get(CharNum).Currentpos.x += 15;

                            System.out.println(CurrentUserList.get(CharNum).ID+"의 캐릭터 최종 좌표 : " + (int) MoveX);
                            System.out.println(CurrentUserList.get(CharNum).ID+"의 캐릭터 현재 x좌표 (증가) : " + CurrentUserList.get(CharNum).Currentpos.x);
                            mGLsurfaceView.requestRender();
                        }
                        CurrentUserList.get(CharNum).Currentpos.x = MoveX;
                        CurrentUserList.get(CharNum).IsMove = false;
                        mGLsurfaceView.requestRender();
                    }
                    catch(IndexOutOfBoundsException e)
                    {
                        System.out.println("예외");
                    }
                }
            }
            catch(IndexOutOfBoundsException e)
            {
                System.out.println("예외");
            }
        }
    }

    // 다른 캐릭터의 Y축 이동 스레드
    public class uMoveThreadY extends Thread
    {
        public int MoveY;
        public int CharNum;

        public uMoveThreadY(int moveY, int charNum)
        {
            MoveY = moveY;
            CharNum = charNum;
        }

        public void run()
        {
            try
            {
                if(CurrentUserList.get(CharNum).Currentpos.y > MoveY)
                {
                    CurrentUserList.get(CharNum).IsMove = true;
                    while(CurrentUserList.get(CharNum).Currentpos.y > MoveY)
                    {
                        CurrentUserList.get(CharNum).IsMove = true;
                        try
                        {
                            sleep(100);
                        }
                        catch(InterruptedException e)
                        {
                            break;
                        }
                        if(CurrentUserList.size() > 0)
                        {
                            CurrentUserList.get(CharNum).Currentpos.y -= 15;
                        }
                        mGLsurfaceView.requestRender();
                    }
                    CurrentUserList.get(CharNum).Currentpos.y = MoveY;
                    CurrentUserList.get(CharNum).IsMove = false;
                    mGLsurfaceView.requestRender();
                }
                else
                {
                    CurrentUserList.get(CharNum).IsMove = true;
                    while(CurrentUserList.get(CharNum).Currentpos.y < MoveY)
                    {
                        CurrentUserList.get(CharNum).IsMove = true;
                        try
                        {
                            sleep(100);
                        }
                        catch(InterruptedException e)
                        {
                            break;
                        }
                        if(CurrentUserList.size() > 0)
                        {
                            CurrentUserList.get(CharNum).Currentpos.y += 15;
                        }
                        mGLsurfaceView.requestRender();
                    }
                    CurrentUserList.get(CharNum).Currentpos.y = MoveY;
                    CurrentUserList.get(CharNum).IsMove = false;
                    mGLsurfaceView.requestRender();
                }
            }
            catch(IndexOutOfBoundsException e)
            {
                System.out.println("예외");
            }
        }
    }

    public void OpenItemUI(View v)
    {
        itemUI.show();
        itemUI.getWindow().setAttributes(params20);
    }

    public void CloseItemUI(View v)
    {
        itemUI.cancel();
        mGLsurfaceView.requestRender();
        clienttask = new ClientTask();
        clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_CHARMOVE);
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.execute();
    }

    class ItemInfo extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            String ItemNum = result;
            ItemNumText.setText(ItemNum);
        }

        @Override
        protected String doInBackground(String... strings)
        {
            String ServerURL = "http://115.71.237.45/iteminfo.php";
            String postParameters = "&nickname="+player.ID;

            try
            {
                URL url = new URL(ServerURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK)
                {
                    inputStream = httpURLConnection.getInputStream();
                }
                else
                {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();
            }
            catch(Exception e)
            {
                return new String("Error: " + e.getMessage());
            }
        }
    }

    class GetItem extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            String ItemNum = result;
            ItemNumText.setText(ItemNum);
        }

        @Override
        protected String doInBackground(String... strings)
        {
            String ServerURL = "http://115.71.237.45/getitem.php";
            String postParameters = "&nickname="+player.ID;
            try
            {
                URL url = new URL(ServerURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK)
                {
                    inputStream = httpURLConnection.getInputStream();
                }
                else
                {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();
            }
            catch(Exception e)
            {
                return new String("Error: " + e.getMessage());
            }
        }
    }

    class WearInfo extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            String ItemNum = result;
            player.WearItem = Integer.parseInt(ItemNum);

            // 소켓을 생성합니다.

            clienttask = new ClientTask();
            clienttask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, REQ_LOGON);
        }

        @Override
        protected String doInBackground(String... strings)
        {
            String ServerURL = "http://115.71.237.45/wearinfo.php";
            String postParameters = "&nickname="+player.ID;

            try
            {
                URL url = new URL(ServerURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK)
                {
                    inputStream = httpURLConnection.getInputStream();
                }
                else
                {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();
            }
            catch(Exception e)
            {
                return new String("Error: " + e.getMessage());
            }
        }
    }

}
