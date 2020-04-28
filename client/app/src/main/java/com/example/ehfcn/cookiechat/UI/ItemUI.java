package com.example.ehfcn.cookiechat.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ehfcn.cookiechat.R;
import com.example.ehfcn.cookiechat.graphic.GLRenderer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.ehfcn.cookiechat.scene.GameScene.player;

/**
 * Created by ehfcn on 2017-09-13.
 */

public class ItemUI extends Dialog
{
    ListView ItemList;
    ListViewAdapter adapter;
    Context mContext;

    public ItemUI(Context context)
    {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.itemui);
        adapter = new ListViewAdapter();
        ItemList = (ListView) findViewById(R.id.itemlistview);
        ItemList.setAdapter(adapter);

        adapter.addItem(ContextCompat.getDrawable(mContext, R.drawable.s_hair0), "벚꽃맛 머리");
        adapter.addItem(ContextCompat.getDrawable(mContext, R.drawable.s_hair1), "오렌지맛 머리");
        adapter.addItem(ContextCompat.getDrawable(mContext, R.drawable.s_hair2), "구미호맛 머리");
        adapter.addItem(ContextCompat.getDrawable(mContext, R.drawable.s_hair3), "쿠키앤크림맛 머리");
        adapter.addItem(ContextCompat.getDrawable(mContext, R.drawable.s_hair4), "팬케이크맛 머리");
        adapter.addItem(ContextCompat.getDrawable(mContext, R.drawable.s_hair5), "박하사탕맛 머리");

        adapter.listViewItemList.get(player.WearItem).IsWear = true;

        ItemList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {

                if(adapter.listViewItemList.get(position).IsWear)
                {
                    adapter.listViewItemList.get(position).IsWear = false;
                    player.WearItem = 0;
                }
                else
                {
                    for(int i = 0; i < adapter.listViewItemList.size(); i++)
                    {
                        if(adapter.listViewItemList.get(i).IsWear)
                        {
                            adapter.listViewItemList.get(i).IsWear = false;
                            //WearItem wearItem = new WearItem();
                            //wearItem.execute("0");
                        }
                    }
                    if(adapter.listViewItemList.get(position).getTitle().equals("벚꽃맛 머리"))
                    {
                        WearItem wearItem = new WearItem();
                        wearItem.execute("0");
                        System.out.println("player.wearitem~~~");
                    }
                    else if(adapter.listViewItemList.get(position).getTitle().equals("오렌지맛 머리"))
                    {
                        WearItem wearItem = new WearItem();
                        wearItem.execute("1");
                    }
                    else if(adapter.listViewItemList.get(position).getTitle().equals("구미호맛 머리"))
                    {
                        WearItem wearItem = new WearItem();
                        wearItem.execute("2");
                    }
                    else if(adapter.listViewItemList.get(position).getTitle().equals("쿠키앤크림맛 머리"))
                    {
                        WearItem wearItem = new WearItem();
                        wearItem.execute("3");
                    }
                    else if(adapter.listViewItemList.get(position).getTitle().equals("팬케이크맛 머리"))
                    {
                        WearItem wearItem = new WearItem();
                        wearItem.execute("4");
                    }
                    else if(adapter.listViewItemList.get(position).getTitle().equals("박하사탕맛 머리"))
                    {
                        WearItem wearItem = new WearItem();
                        wearItem.execute("5");
                    }
                }

                return false;
            }
        });
    }

    class WearItem extends AsyncTask<String, Void, String>
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
            if(result.equals("아이템부족"))
            {
                Toast.makeText(mContext, "교환할 아이템 갯수가 부족해요", Toast.LENGTH_SHORT).show();
            }
            else
            {
                System.out.println("result :: " + result);
                if(result.equals(""))
                {

                }
                else
                {
                    player.WearItem = Integer.parseInt(result);
                    System.out.println("player.wearitem :: " + player.WearItem);
                    adapter.listViewItemList.get(player.WearItem).IsWear = true;
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        protected String doInBackground(String... params)
        {
            String ServerURL = "http://115.71.237.45/wearitem.php";
            String postParameters = "&nickname="+player.ID+"&wearitem="+params[0];

            System.out.println("postParameters :: "+ postParameters);

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
