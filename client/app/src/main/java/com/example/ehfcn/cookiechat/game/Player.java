package com.example.ehfcn.cookiechat.game;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by ehfcn on 2017-07-18.
 */

// 자신 및 모든 유저들의 정보를 관리하기 위한 객체입니다.

public class Player
{
    // 유저의 아이디
    public String ID;

    // 유저의 이전 좌표
    public PointF Lastpos;

    // 유저의 현재 좌표
    public PointF Currentpos;

    // 현재 유저가 왼쪽 방향을 바라보고 있는가 여부. 이를 통해 스프라이트 이미지 랜더링 위치를 잡습니다.

    // 현재 유저가 말풍선을 띄우고 있는가 여부
    public boolean IsChatBalloons;

    // 현재 유저가 말하고 있는지 여부
    public String ChatString;

    // 현재 유저가 위치한 맵 고유번호
    // 1. 뒷길 2. 부엌 앞 3. 부엌 4. 첫번째 지붕 5. 두번째 지붕
    public int CurrentMapNum;

    public boolean IsMove = false;

    public boolean IsLeft;

    public int WearItem;

    /*-----------------------------------------------------------------------------*/

    // 구성요소들을 초기화합니다.
    public Player(String id)
    {
        Lastpos = new PointF();
        Currentpos = new PointF();

        Lastpos.x = 0.0f;
        Lastpos.y = 0.0f;

        Currentpos.x = 0.0f;
        Currentpos.y = 0.0f;

        ID = id;

        IsChatBalloons = false;
        ChatString = null;

        CurrentMapNum = 1;

        WearItem = 0;
    }
}
