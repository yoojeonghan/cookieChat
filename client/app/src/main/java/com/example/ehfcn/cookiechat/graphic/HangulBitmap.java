package com.example.ehfcn.cookiechat.graphic;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;

/**
 * Created by ehfcn on 2017-07-18.
 */

public class HangulBitmap
{
    public Bitmap Bitmap;
    private Bitmap.Config config;
    private Canvas canvas;
    private Paint mPaint;
    private Typeface font;
    private String htext;

    // 생성자 폰트를 설정

    public HangulBitmap(Activity mActivity)
    {
        font = Typeface.createFromAsset(mActivity.getAssets(), "fonts/SDSwaggerTTF.ttf");
    }

    // 텍스트를 출력하여 비트맵으로 변환함
    public void GetBitmap(Bitmap bitmap, String text, int textSize, int fontColor, int canvasColor, float scale)
    {
        Bitmap = bitmap;
        canvas = new Canvas(Bitmap);
        // 페인트를 이용하여 출력
        mPaint = new Paint();
        mPaint.setColor(fontColor);
        mPaint.setTextSize(textSize);
        if(canvasColor == -1)
        {
            mPaint.setAntiAlias(false);
        }
        else
        {
            mPaint.setAntiAlias(true);
        }
        mPaint.setTypeface(font);
        float textWidth = mPaint.measureText(text);
        mPaint.setTextScaleX(0.8f);
        canvas.drawText(text, textWidth*0.1f, textSize*0.9f, mPaint); // 64
    }

    public void RecycleBitmap()
    {
        Bitmap.recycle();
        Bitmap = null;
    }
}
