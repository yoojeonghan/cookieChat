package com.example.ehfcn.cookiechat.library;

import android.graphics.Bitmap;

/**
 * Created by ehfcn on 2017-08-26.
 */

// NDK 라이브러리를 불러오는 클래스입니다.

@SuppressWarnings("JniMissingFunction")
public class GL2JNILib
{
    static
    {
        System.loadLibrary("mygles");
    }

    public static native void nativeCreated();
    public static native void nativeChanged(int w, int h);
    public static native void nativeUpdateGame(float[] arr, int CamX, int PosY);
    public static native void nativeSetTextureData(int[] pixels, int width, int height);
}
