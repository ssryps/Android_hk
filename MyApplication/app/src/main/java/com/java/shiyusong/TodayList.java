package com.java.shiyusong;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TodayList extends AppCompatActivity {
    Context mContext;
    MySQLiteOpenHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_list);
        this.mContext = this;
        this.helper = new MySQLiteOpenHelper(mContext);
        BmobQuery<NewsFootPrint> query = new BmobQuery<>();
        query.addWhereEqualTo("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        query.order("-visitNum");
        query.setLimit(10);


        final LinearLayout linearLayout = (LinearLayout)findViewById(R.id.today_hot_list);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    List<NewsFootPrint> object = (List<NewsFootPrint>)msg.obj;
                    for(NewsFootPrint newsFootPrint: object){
                        final News news = new News(newsFootPrint.getTitle(), newsFootPrint.getLink(), newsFootPrint.getAuthor(),
                                newsFootPrint.getDes(), newsFootPrint.getPubDate(), newsFootPrint.getClassification(), newsFootPrint.getChannel());
                        if(!helper.isNewsIn(news)){
                            news.setIsRead("false");
                            helper.insertNews(news);
                        }
                        news.isRead = helper.getIsRead(news);
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
                        View line = new View(mContext);
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
        };

        query.findObjects(new FindListener<NewsFootPrint>() {
            @Override
            public void done(List<NewsFootPrint> object, BmobException e) {
                if(e==null && object.size() > 0){
                    Message message = handler.obtainMessage(1, object);
                    handler.sendMessage(message);
                }else{
                    if(e.getErrorCode() == 101) Toast.makeText(mContext, "Today's new list is empty now", Toast.LENGTH_LONG).show();
                    else Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
