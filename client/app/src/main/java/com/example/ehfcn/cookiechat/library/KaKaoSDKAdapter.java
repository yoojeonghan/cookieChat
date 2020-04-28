package com.example.ehfcn.cookiechat.library;

import android.content.Context;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;

/**
 * Created by ehfcn on 2017-06-15.
 */

public class KaKaoSDKAdapter extends KakaoAdapter
{

    @Override
    public ISessionConfig getSessionConfig()
    {
        return new ISessionConfig()
        {
            @Override
            public AuthType[] getAuthTypes()
            {
                 return new AuthType[]
                 {
                         // 로그인시 인증받을 타입을 지정한다. 지정하지 않을 시 가능한 모든 옵션이 지정된다.

                         //1.KAKAO_TALK :  kakaotalk으로 login을 하고 싶을때 지정.
                         //2.KAKAO_STORY : kakaostory으로 login을 하고 싶을때 지정.
                         //3.KAKAO_ACCOUNT :  웹뷰 Dialog를 통해 카카오 계정연결을 제공하고 싶을경우 지정.
                         //4.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN : 카카오톡으로만 로그인을 유도하고 싶으면서 계정이 없을때 계정생성을 위한
                         //버튼도 같이 제공을 하고 싶다면 지정.KAKAO_TALK과 중복 지정불가.
                         //5.KAKAO_LOGIN_ALL : 모든 로그인방식을 사용하고 싶을때 지정.
                    AuthType.KAKAO_TALK
                 };
            }

            @Override
            public boolean isUsingWebviewTimer()
            {
                return false;
            }

            @Override
            public boolean isSecureMode()
            {
                return false;
            }

            @Override
            public ApprovalType getApprovalType()
            {
                return ApprovalType.INDIVIDUAL;
            }

            @Override
            public boolean isSaveFormData()
            {
                return false;
            }
        };
    }


    @Override
    public IApplicationConfig getApplicationConfig()
    {
        return new IApplicationConfig()
        {
            @Override
            public Context getApplicationContext()
            {
                return GlobalApplication.getGlobalApplicationContext();
            }
        };
    }
}