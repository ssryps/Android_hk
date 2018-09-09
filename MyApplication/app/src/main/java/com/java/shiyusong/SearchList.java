package com.java.shiyusong;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SearchList extends AppCompatActivity {
    MySQLiteOpenHelper helper;
    SearchView searchView;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
 //       setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("搜索");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.helper = new MySQLiteOpenHelper(this);
        mContext = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        LinearLayout searchFrameLL=(LinearLayout)searchView.findViewById(R.id.search_edit_frame);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,  LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0,0,0,0); //params.setMargins(left, top, right, bottom)
        searchFrameLL.setLayoutParams(params);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconified(false);
        //searchView.setIconifiedByDefault(false);
        searchView.onActionViewExpanded();
        searchView.setQueryHint("搜索新闻");

        final LinearLayout linearLayout = (LinearLayout)findViewById(R.id.search_list);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<News> result = helper.search(query);
                Collections.sort(result, new Comparator<News>(){
                    public int compare(News o1, News o2) {
                        return o2.pubDate.compareTo(o1.pubDate);
                    }
                });
                linearLayout.removeAllViews();
                for(News news: result){
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
                    final News _news = news;
                    LinearLayout linearLayout1 = (LinearLayout) newView.findViewById(R.id.news_layout);
                    linearLayout1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            helper.setIsRead(_news);
                            title.setTextColor(getResources().getColor(R.color.colorNewsListRead));
                            Intent intent = new Intent(mContext, NewsDisplay.class);
                            intent.putExtra("news", _news);
                            startActivityForResult(intent, 1);
                        }
                    });
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        SearchView.SearchAutoComplete mSearchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        if(mSearchAutoComplete.getText().length() == 0){
            setResult(50);
            finish();
        } else{
            System.out.println(mSearchAutoComplete.getText().length());
            mSearchAutoComplete.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
