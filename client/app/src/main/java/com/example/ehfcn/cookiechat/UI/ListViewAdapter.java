package com.example.ehfcn.cookiechat.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ehfcn.cookiechat.R;
import com.example.ehfcn.cookiechat.graphic.GLRenderer;

import java.util.ArrayList;

/**
 * Created by ehfcn on 2017-08-25.
 */

public class ListViewAdapter extends BaseAdapter
{
    Context context;

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    public ArrayList<Item> listViewItemList = new ArrayList<Item>();
    // ListViewAdapter의 생성자
    public ListViewAdapter()
    {

    }

    @Override
    public int getCount()
    {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final int pos = position;
        context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView1) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        Item listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        titleTextView.setText(listViewItem.getTitle());

        WearItem(convertView, position);

        return convertView;
    }

    @Override
    public long getItemId(int position)
    {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position)
    {
        return listViewItemList.get(position) ;
    }

    public void addItem(Drawable icon, String title)
    {
        Item item = new Item();

        item.setIcon(icon);
        item.setName(title);

        listViewItemList.add(item);
    }

    public void WearItem(View convertView, int position)
    {
        if(listViewItemList.get(position).getIsWear())
        {
            convertView.setBackground(context.getResources().getDrawable(R.drawable.chat_log));
            notifyDataSetChanged();
        }
        else
        {
            convertView.setBackgroundColor(Color.TRANSPARENT);
            notifyDataSetChanged();
        }
    }
}
