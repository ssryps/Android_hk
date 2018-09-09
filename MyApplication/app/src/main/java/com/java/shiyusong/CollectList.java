package com.java.shiyusong;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectList extends AppCompatActivity {
    MySQLiteOpenHelper helper;
    Context mContext;
    ArrayList<Classification> classifications;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_list);
        mContext = this;
        this.helper = new MySQLiteOpenHelper(mContext);
        getSupportActionBar().setTitle("收藏");
        UpdateUi();
        Intent intent = getIntent();
        classifications = (ArrayList<Classification>)intent.getSerializableExtra("classifications");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UpdateUi();
    }

    private void UpdateUi(){
        ArrayList<News> newsArrayList = helper.getAllCollected();
        Collections.sort(newsArrayList, new Comparator<News>(){
            public int compare(News o1, News o2) {
                return o2.pubDate.compareTo(o1.pubDate);
            }
        });
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.collect_list);
        linearLayout.removeAllViews();
        for(final News news: newsArrayList) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_collection, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.collect_settings_local_to_server) {
            BmobUser user = BmobUser.getCurrentUser();
            if(user == null) {
                Toast.makeText(mContext, getResources().getText(R.string.please_sign_in), Toast.LENGTH_LONG).show();
                return true;
            } else {
                BmobQuery<NewsCollection> query = new BmobQuery<>();
                query.addWhereEqualTo("user", BmobUser.getCurrentUser());
                query.findObjects(new FindListener<NewsCollection>() {
                    @Override
                    public void done(List<NewsCollection> object, BmobException e) {
                        if(e==null && object.size() > 0){
                            NewsCollection oldCollection = object.get(0);
                            NewsCollection newCollection = new NewsCollection();
                            newCollection.setValue("newsCollection", helper.getAllCollected());
                            newCollection.update(oldCollection.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e==null){
                                        Toast.makeText(mContext, "导入服务器成功", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }else{
                            if(e.getErrorCode() == 101){
                                NewsCollection newCollection = new NewsCollection();
                                newCollection.setNewsCollection(helper.getAllCollected());
                                newCollection.setUser(BmobUser.getCurrentUser());
                                newCollection.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String objectId, BmobException e) {
                                        if(e==null){
                                            Toast.makeText(mContext, "导入服务器成功", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } else if(id == R.id.collect_settings_server_to_local){
            BmobQuery<NewsCollection> query = new BmobQuery<>();
            query.addWhereEqualTo("user", BmobUser.getCurrentUser());
            query.findObjects(new FindListener<NewsCollection>() {
                @Override
                public void done(List<NewsCollection> object, BmobException e) {
                    if(e==null && object.size() > 0){
                        NewsCollection newsCollection = object.get(0);
                        Toast.makeText(mContext, "导入成功", Toast.LENGTH_LONG).show();
                        helper.removeAllCollection();
                        for(News news: newsCollection.getNewsCollection()) {
                            news.setIsCollect("true");
                            if(helper.isNewsIn(news)){
                                helper.setIsCollect(news);
                            }else{
                                helper.insertNews(news);
                                helper.setIsCollect(news);
                            }
                        }
                        UpdateUi();
                    }else{
                        if(e.getErrorCode() == 101) Toast.makeText(mContext, "You have not backup in the server", Toast.LENGTH_LONG).show();
                        else Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(20);
        super.onBackPressed();
    }
}

class NewsCollection extends BmobObject{
    ArrayList<News> newsCollection;
    BmobUser user;
    public ArrayList<News> getNewsCollection() {
        return newsCollection;
    }

    public void setNewsCollection(ArrayList<News> newsCollection) {
        this.newsCollection = newsCollection;
    }

    public void setUser(BmobUser user) {
        this.user = user;
    }

    public BmobUser getUser() {
        return user;
    }
}
