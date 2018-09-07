package com.java.shiyusong;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CollectList extends AppCompatActivity {
    MySQLiteOpenHelper helper;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_list);
        mContext = this;
        this.helper = new MySQLiteOpenHelper(mContext);
        getSupportActionBar().setTitle("收藏");
        UpdateUi();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UpdateUi();
    }

    private void UpdateUi(){
        ArrayList<News> newsArrayList = helper.getAllCollected();
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.collect_list);
        linearLayout.removeAllViews();
        for(final News news: newsArrayList) {
            View newView = getLayoutInflater().inflate(R.layout.news, linearLayout, false);

            final TextView title = (TextView) newView.findViewById(R.id.news_title);
            title.setText(news.title);
            if (news.isRead.equals("false")) title.setTextColor(getResources().getColor(R.color.colorNewsListUnread));
            else title.setTextColor(getResources().getColor(R.color.colorNewsListRead));

            TextView des = (TextView) newView.findViewById(R.id.news_description);
            String desText = news.description;
            if (news.description.length() > 60) {
                desText = news.description.substring(0, 60) + "...";
            }
            des.setText(desText);
            if (news.isRead.equals("false")) des.setTextColor(getResources().getColor(R.color.colorNewsListUnread));
            else des.setTextColor(getResources().getColor(R.color.colorNewsListRead));

            TextView channel = (TextView) newView.findViewById(R.id.news_channel);
            channel.setText(news.channel);
            if (news.isRead.equals("false")) channel.setTextColor(getResources().getColor(R.color.colorNewsListUnread));
            else channel.setTextColor(getResources().getColor(R.color.colorNewsListRead));

            TextView pubDate = (TextView) newView.findViewById(R.id.news_pub_date);
            pubDate.setText(news.pubDate);
            if (news.isRead.equals("false")) pubDate.setTextColor(getResources().getColor(R.color.colorNewsListUnread));
            else pubDate.setTextColor(getResources().getColor(R.color.colorNewsListRead));
            View line = new View(this);
            line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) line.getLayoutParams();
            lp.setMargins(20, 1, 20, 1);
            line.setLayoutParams(lp);
            line.setBackgroundColor(Color.GRAY);
            linearLayout.addView(line);
            linearLayout.addView(newView);

            LinearLayout linearLayout1 = (LinearLayout) newView.findViewById(R.id.news_layout);
            linearLayout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    helper.setIsRead(news);
                    title.setTextColor(getResources().getColor(R.color.colorNewsListRead));
                    Intent intent = new Intent(mContext, NewsDisplay.class);
                    intent.putExtra("news", news);
                    startActivityForResult(intent, 1);
                }
            });
        }

    }
}
