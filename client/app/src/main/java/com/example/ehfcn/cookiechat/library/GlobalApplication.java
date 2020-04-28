package com.example.ehfcn.cookiechat.library;

/**
 * Created by ehfcn on 2017-06-15.
 */

import android.app.Activity;
import android.app.Application;

import com.kakao.auth.KakaoSDK;

/**
 * 이미지를 캐시를 앱 수준에서 관리하기 위한 애플리케이션 객체이다.
 * 로그인 기반 샘플앱에서 사용한다.
 *
 * @author MJ
 */

public class GlobalApplication extends Application
{

    private static volatile GlobalApplication instance = null;
    private static volatile Activity currentActivity = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        KakaoSDK.init(new KaKaoSDKAdapter());
    }

    public static GlobalApplication getGlobalApplicationContext()
    {
        return instance;
    }

    public static void setCurrentActivity(Activity currentActivity)
    {
        GlobalApplication.currentActivity = currentActivity;
    }

    public static Activity getCurrentActivity()
    {
        return currentActivity;
    }
}