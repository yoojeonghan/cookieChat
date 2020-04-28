package com.example.ehfcn.cookiechat.scene;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ehfcn.cookiechat.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener
{
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    MediaPlayer MainBG;
    private SessionCallback sessionCallback;
    EditText EditNickName;
    String CurrentUserEmail = null;
    String GetNickName = null;
    String LogInPlatform = null;

    /*----------------------------------------------------------------------------------------------*/

    private static final String TAG_JSON="webnautes";
    private static final String TAG_EMAIL= "email";
    private static final String TAG_NICKNAME = "nickname";

    /*----------------------------------------------------------------------------------------------*/

    boolean IsLogin = false;
    boolean IsMember = false;

    /*----------------------------------------------------------------------------------------------*/

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailSession = "EmailKey";
    public static final String NicknameSession = "NicknameKey";

    /*----------------------------------------------------------------------------------------------*/

    SharedPreferences sharedpreferences;

    /*----------------------------------------------------------------------------------------------*/

    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*-----------------------------------세션---------------------------------------------------*/

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor = sharedpreferences.edit();

        //editor.clear();
        //editor.commit();

        /*------------------------------------------------------------------------------------------*/
        if(!isNetWork())
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setTitle("쿠키챗");
            alertDialogBuilder.setMessage("인터넷 연결이 불안정하여 애플리케이션을 종료합니다.")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                            MainBG.stop();
                            finish();
                        }
                    });

            alertDialogBuilder.create();
            alertDialogBuilder.show();
        }
        /*------------------------------------------------------------------------------------------*/
        MultiDex.install(this);
        EditNickName = (EditText) findViewById(R.id.editnick);
        EditNickName.requestFocus();

        /*----------------------------------BGM 셋팅, 재생------------------------------------------*/

        // BGM 셋팅, 재생
        MainBG = MediaPlayer.create(this, R.raw.b202);
        MainBG.setLooping(true);
        MainBG.start();

        /*----------------------------------구글로그인------------------------------------------*/

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
             .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
             .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
             .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        setGooglePlusButtonText(signInButton, "구글계정으로 로그인");

        /*----------------------------------카카오톡로그인------------------------------------------*/

        UserManagement.requestLogout(new LogoutResponseCallback()
        {
            @Override
            public void onCompleteLogout()
            {
            }
        });

        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures)
            {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                //Log.d("aaaa", Base64.encodeBase64URLSafeString(messageDigest.digest()));
            }
        }
        catch (Exception e)
        {
            Log.d("error", "PackageInfo error is " + e.toString());
        }

        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        /*------------------------------------------------------------------------------------------*/

        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.btnKakaoLogin).setVisibility(View.VISIBLE);
        findViewById(R.id.disconnect_button2).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        findViewById(R.id.characterlayout).setVisibility(View.GONE);

        /*------------------------------------------------------------------------------------------*/

        backPressCloseHandler = new BackPressCloseHandler(this);

        NextScene();

    }

    private Boolean isNetWork()
    {
        ConnectivityManager manager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        /*
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        */

        /*
        if ((isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect))
        {
            return true;
        }
        else
        {
            return false;
        }
        */

        return true;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        CurrentUserEmail = null;
        GetNickName = null;
        IsLogin = false;
        IsMember = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        /*----------------------------------카톡로그인------------------------------------------*/

        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
        {
            IsLogin = true;
            //updateUI(IsLogin);
            return ;
        }

        /*----------------------------------구글로그인------------------------------------------*/

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    public void request()
    {
        UserManagement.requestMe(new MeResponseCallback()
        {
            @Override
            public void onSessionClosed(ErrorResult errorResult)
            {
                Log.d("error", "Session Closed Error is " + errorResult.toString());
            }

            @Override
            public void onNotSignedUp()
            {

            }

            @Override
            public void onSuccess(UserProfile result)
            {
                Toast.makeText(MainActivity.this, "카카오톡 " + result.getEmail() + " 계정으로 로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                CurrentUserEmail = result.getEmail();
                LogInPlatform = "kakaotalk";
                IsLogin = true;
                updateUI(true);
            }
        });
    }

    private void handleSignInResult(GoogleSignInResult result)
    {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess())
        {
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(MainActivity.this, "구글 "+ acct.getEmail() +" 계정으로 로그인 되었습니다.", Toast.LENGTH_SHORT).show();
            CurrentUserEmail = acct.getEmail();
            LogInPlatform = "google";
            IsLogin = true;
            updateUI(true);
        }
        else
        {
            updateUI(false);
        }
    }

    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void revokeAccess()
    {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>()
                {
                    @Override
                    public void onResult(Status status)
                    {
                        CurrentUserEmail = null;
                        IsLogin = false;
                        GetNickName = null;
                        LogInPlatform = null;
                        IsMember = false;

                        // 카카오톡 로그아웃 코드
                        UserManagement.requestLogout(new LogoutResponseCallback()
                        {
                            @Override
                            public void onCompleteLogout()
                            {
                                updateUI(false);
                            }
                        });
                        updateUI(false);
                    }
                });
        Toast.makeText(MainActivity.this, "로그인 정보를 해제하였습니다", Toast.LENGTH_SHORT).show();
    }

    public void AnotherSignIn(View v)
    {
        revokeAccess();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void updateUI(boolean signedIn)
    {
        System.out.println("11111");
        if (signedIn)
        {
            System.out.println("22222");
            System.out.println("로그인 이메일 : "+CurrentUserEmail);
            GetData task2 = new GetData();
            task2.execute("http://115.71.237.45/get.php", CurrentUserEmail, LogInPlatform);

            // 이메일 불러오고 유저 목록에 그 이메일이 존재하는지 확인
            if(IsMember)
            {
                //  존재하면 게임시작
                BackThread Backthread = new BackThread();
                Backthread.setDaemon(true);
                Backthread.start();
            }
        }
        else
        {
            System.out.println("와아아악");
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.btnKakaoLogin).setVisibility(View.VISIBLE);
            findViewById(R.id.disconnect_button2).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            findViewById(R.id.characterlayout).setVisibility(View.GONE);
        }
    }

    public void NextScene()
    {
        if(sharedpreferences.getString(NicknameSession, null) != null)
        {
            //Toast.makeText(MainActivity.this, sharedpreferences.getString(NicknameSession, null) + "님, 환영합니다!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, GameScene.class);
            MainBG.stop();
            startActivity(intent);
            finish();
        }
        else
        {
            //Toast.makeText(MainActivity.this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    class BackThread extends Thread
    {
        @Override
        public void run()
        {
                try
                {
                    Thread.sleep(100);
                    handler.sendEmptyMessage(0);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
        }
    }

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 0)
            {
                System.out.println("현재 로그인상태 : "+ IsLogin);
                System.out.println("현재 캐릭터생성 상태 : " + IsMember);

                if(IsMember)
                {
                    //NextScene();
                }

                else
                {
                    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                    findViewById(R.id.btnKakaoLogin).setVisibility(View.GONE);
                    findViewById(R.id.disconnect_button2).setVisibility(View.GONE);
                    findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
                    findViewById(R.id.characterlayout).setVisibility(View.VISIBLE);
                }
            }
        }
    };

    // 사용자의 이메일이 서버 DB에 저장되어 있는가 확인
    class GetData extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            Log.d(TAG, "response  - " + result);

            if (result == null || result.equals("가입되지 않은 사용자"))
            {
                System.out.println("일로 들어옴");
                System.out.println(result);
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.btnKakaoLogin).setVisibility(View.GONE);
                findViewById(R.id.disconnect_button2).setVisibility(View.GONE);
                findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
                findViewById(R.id.characterlayout).setVisibility(View.VISIBLE);
                EditNickName = (EditText) findViewById(R.id.editnick);
                EditNickName.requestFocus();
            }
            else
            {
                String mJsonString = result;

                try
                {
                    JSONObject jsonObject = new JSONObject(mJsonString);
                    JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                    for(int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject item = jsonArray.getJSONObject(i);

                        CurrentUserEmail = item.getString(TAG_EMAIL);

                        // 세션 시작
                        SharedPreferences.Editor editor = sharedpreferences.edit();

                        editor.putString(NicknameSession, item.getString(TAG_NICKNAME));
                        editor.commit();
                    }
                }
                catch(JSONException e)
                {
                    Log.d(TAG, "showresult : ", e);
                }

                // 씬 이동
                NextScene();
            }
        }


        @Override
        protected String doInBackground(String... params)
        {
            String inputname = "false";
            String serverURL = params[0];
            String UserEmail = params[1];
            String platform = params[2];

            String postParameters = "&inputname=" + inputname + "&email=" + UserEmail + "&platform=" + platform;

            try
            {
                URL url = new URL(serverURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

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
                String line;

                while((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            }
            catch (Exception e)
            {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText)
    {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++)
        {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView)
            {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        backPressCloseHandler.onBackPressed();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // 카카오톡 로그아웃 코드
        UserManagement.requestLogout(new LogoutResponseCallback()
        {
            @Override
            public void onCompleteLogout()
            {
            }
        });
    }

    private class SessionCallback implements ISessionCallback
    {

        @Override
        public void onSessionOpened()
        {
            request();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception)
        {
            if(exception != null)
            {
                Logger.e(exception);
                Log.d("error", "Session Fail Error is " + exception.getMessage().toString());
            }
        }
    }

    // api 로그인 시 닉네임이 없을 경우 뜨는 회원가입 메소드
    public void Register(View v)
    {
        // CurrentUserEmail, nickname
        System.out.println("현재 가입하는 이메일 : "+CurrentUserEmail);
        System.out.println("현재 가입하는 닉네임 : "+GetNickName);

        GetNickName = EditNickName.getText().toString();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("쿠키챗");
        alertDialogBuilder.setMessage("정말 이 닉네임으로 결정하시겠어요?")
                .setCancelable(false)
                .setPositiveButton("네", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                        InsertData task = new InsertData();
                        task.execute("true", CurrentUserEmail, GetNickName, LogInPlatform);
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

        /*----------------------------------------------------------------------------------------------*/

        //mTextViewResult = (TextView)findViewById(R.id.textView_main_result);
        //mlistView = (ListView) findViewById(R.id.listView_main_list);

        /*----------------------------------------------------------------------------------------------*/
    }

    // 닉네임이 형식에 일치하는지, 혹은 중복되는지 판단합니다.
    class InsertData extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this, "잠시만 기다려주세요 ... ", null, true, true);
        }


        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "POST response  - " + result);

            if (result == null || result.equals("닉네임이 입력되지 않음"))
            {
                System.out.println(result);
                Toast.makeText(MainActivity.this, "닉네임이 입력되지 않았어요.", Toast.LENGTH_SHORT).show();
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.btnKakaoLogin).setVisibility(View.GONE);
                findViewById(R.id.disconnect_button2).setVisibility(View.GONE);
                findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
                findViewById(R.id.characterlayout).setVisibility(View.VISIBLE);
            }
            else if(result.equals("잘못된 닉네임"))
            {
                Toast.makeText(MainActivity.this, "닉네임을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else if(result.equals("잘못된 닉네임2"))
            {
                Toast.makeText(MainActivity.this, "닉네임을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else if(result.equals("중복된 닉네임"))
            {
                Toast.makeText(MainActivity.this, "닉네임이 이미 사용중이에요.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                GetData task2 = new GetData();
                task2.execute("http://115.71.237.45/get.php", CurrentUserEmail, LogInPlatform);
            }
        }


        @Override
        protected String doInBackground(String... params)
        {

            String inputname = (String)params[0];
            String email = (String)params[1];
            String nickname = (String)params[2];
            String platform = (String)params[3];

            String serverURL = "http://115.71.237.45/insert.php";
            String postParameters = "&inputname=" + inputname + "&email=" + email + "&nickname=" + nickname + "&platform=" + platform;

            try
            {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

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
            catch (Exception e)
            {
                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }

    // 뒤로가기 버튼을 두 번 누를 시 종료됩니다.
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
                MainBG.stop();
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
}
