package com.example.ehfcn.cookiechat.UI;

import android.graphics.drawable.Drawable;

public class Item
{
    private Drawable iconDrawable ;
    private String NameStr;
    public boolean IsWear = false;

    public void setIcon(Drawable icon)
    {
        iconDrawable = icon ;
    }
    public void setName(String title)
    {
        NameStr = title ;
    }

    public Drawable getIcon()
    {
        return this.iconDrawable ;
    }
    public String getTitle()
    {
        return this.NameStr ;
    }

    public boolean getIsWear()
    {
        return this.IsWear;
    }
}