package com.example.ehfcn.cookiechat.game;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by ehfcn on 2017-09-28.
 */

public class Item
{
    public Point ItemPos = new Point();
    public Rect ItemRect = new Rect();

    public Item(int posX, int posY)
    {
        ItemPos.x = posX;
        ItemPos.y = posY;

        ItemRect.left = ItemPos.x - 50;
        ItemRect.top = ItemPos.y + 30;
        ItemRect.right = ItemPos.x + 50;
        ItemRect.bottom = ItemPos.y - 70;
    }
}