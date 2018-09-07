package com.java.shiyusong;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class News implements Serializable {
    public static int objnum = 0;
    String title, link, author, description, pubDate, classification, channel, html, isRead, isCollect;
    public News(String title, String link, String author, String description, String pubDate, String classification, String channel){
        this.title = title;
        this.link = link;
        this.author = author;
        this.description = description;
        this.pubDate = pubDate;
        this.classification = classification;
        this.channel = channel;
        this.html = "";
        this.isRead = "false";
        this.isCollect = "false";

    }
    public void setHtml(String html){
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsCollect(String isCollect) {
        this.isCollect = isCollect;
    }

    public String getIsCollect() {
        return isCollect;
    }
    @Override
    public String toString() {
        return classification + "\n" + channel + "\n" + title + "\n" + pubDate + "\n" + link + "\n" + description + "\n" + isRead;
    }

    public ContentValues getContentValues(){
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("link", link);
        contentValues.put("author", author);
        contentValues.put("description", description);
        contentValues.put("pubDate", pubDate);
        contentValues.put("classification", classification);
        contentValues.put("channel", channel);
        contentValues.put("html", html);
        contentValues.put("isRead", isRead);
        contentValues.put("isCollect", isCollect);

        return contentValues;
    }
}

class MySQLiteOpenHelper  extends SQLiteOpenHelper {
    public static final String DB_NAME = "database.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NEWS = "news";
    public static final String TABLE_BITMAP = "bitmap";

    private SQLiteDatabase mSQLiteDatabase;
    private static final String NEWS_CREATE_TABLE_SQL = "create table " + TABLE_NEWS + " ("
            + "ID integer primary key autoincrement,"
            + "title text,"
            + "link text,"
            + "author text,"
            + "description text,"
            + "pubDate text,"
            + "classification text,"
            + "channel text,"
            + "html text,"
            + "isRead text,"
            + "isCollect text"
            + ");";
    private static final String BITMAP_CREATE_TABLE_SQL = "create table " + TABLE_BITMAP + " ( "
            + "ID integer primary key, "
            + "url text, "
            + "bitmap blob ) ";
    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mSQLiteDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BITMAP_CREATE_TABLE_SQL);
        db.execSQL(NEWS_CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void insertNews(News news){
        Cursor cursor = mSQLiteDatabase.query(TABLE_NEWS, null,
                "classification=? and channel=? and title=? and link=?",
                new String[]{news.classification, news.channel, news.title, news.link },
                null, null, null, null);
        if(!cursor.moveToNext()) {
            mSQLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NEWS, null, news.getContentValues());
        }
    }

    public boolean isNewsIn(News news){
        Cursor cursor = mSQLiteDatabase.query(TABLE_NEWS, null,
                "classification=? and channel=? and title=? and link=?",
                new String[]{news.classification, news.channel, news.title, news.link },
                null, null, null, null);
        if(!cursor.moveToNext()) {
            return false;
        }
        else return true;
    }

    public ArrayList<News> getNewsByChannel(Classification classi, String time, String limit){
        ArrayList<News> newsArrayList = new ArrayList<>();
        ArrayList<Channel> channels = classi.getChannels();
        Cursor cursor = mSQLiteDatabase.query(TABLE_NEWS, null,
                "classification=? and pubDate<?", new String[]{classi.getName(), time},
                null, null, "pubDate DESC", limit);
        while (cursor.moveToNext()) {
            newsArrayList.add(getNewsFromCursor(cursor));
        }
        cursor.close();
        return newsArrayList;
    }

    public void setIsRead(News news){
        ContentValues values = new ContentValues();
        values.put("isRead", "true");
        mSQLiteDatabase.update(TABLE_NEWS, values, "classification=? and channel=? and title=? and link=?",
                new String[]{news.classification, news.channel, news.title, news.link});
    }

    public void setIsCollect(News news){
        ContentValues values = new ContentValues();
        values.put("isCollect", news.getIsCollect());
        mSQLiteDatabase.update(TABLE_NEWS, values, "classification=? and channel=? and title=? and link=?",
                new String[]{news.classification, news.channel, news.title, news.link});
    }

    public String getIsCollect(News news){
        Cursor cursor = mSQLiteDatabase.query(TABLE_NEWS, null,
                "classification=? and channel=? and title=? and link=?",
                new String[]{news.classification, news.channel, news.title, news.link},
                null, null, null, null);
        while (cursor.moveToNext()) {
            String isCollect = cursor.getString(cursor.getColumnIndex("isCollect"));
            return isCollect;
        }
        return "false";
    }

    public ArrayList<News> getAllCollected(){
        ArrayList<News> newsArrayList = new ArrayList<>();
        Cursor cursor = mSQLiteDatabase.query(TABLE_NEWS, null,
                "isCollect=?", new String[]{"true"},
                null, null, null, null);
        while (cursor.moveToNext()) {
            newsArrayList.add(getNewsFromCursor(cursor));
        }
        cursor.close();
        return newsArrayList;
    }

    private News getNewsFromCursor(Cursor cursor){
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String link = cursor.getString(cursor.getColumnIndex("link"));
        String author = cursor.getString(cursor.getColumnIndex("author"));
        String description = cursor.getString(cursor.getColumnIndex("description"));
        String pubDate = cursor.getString(cursor.getColumnIndex("pubDate"));
        String classification = cursor.getString(cursor.getColumnIndex("classification"));
        String channel = cursor.getString(cursor.getColumnIndex("channel"));
        String html = cursor.getString(cursor.getColumnIndex("html"));
        String isRead = cursor.getString(cursor.getColumnIndex("isRead"));
        String isCollect = cursor.getString(cursor.getColumnIndex("isCollect"));

        News news = new News(title, link, author, description, pubDate, classification, channel);
        news.setHtml(html);
        news.setIsRead(isRead);
        news.setIsCollect(isCollect);
        return news;
    }

    public void insertBitmap(Bitmap bitmap, String url) {
        Cursor cursor = mSQLiteDatabase.query(TABLE_BITMAP, null,
                "url=?", new String[]{url},
                null, null, null, null);
        if (!cursor.moveToNext()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            ContentValues cv = new ContentValues();
            cv.put("bitmap", baos.toByteArray());
            cv.put("url", url);
            mSQLiteDatabase.insert(TABLE_BITMAP, null, cv);
        }
    }

    public Bitmap getBitmap(String url) {
        Cursor cursor = mSQLiteDatabase.query(TABLE_BITMAP, null,
                "url=?", new String[]{url},
                null, null, null, null);
        if (cursor.moveToNext()) {
            byte[] in = cursor.getBlob(cursor.getColumnIndex("bitmap"));
            Bitmap bmpout = BitmapFactory.decodeByteArray(in, 0, in.length);
            return bmpout;
        } else return null;
    }

    public void insertHtml(News news, String html){
        ContentValues values = new ContentValues();
        values.put("html", html);
        mSQLiteDatabase.update(TABLE_NEWS, values, "classification=? and channel=? and title=? and link=?",
                new String[]{news.classification, news.channel, news.title, news.link});
    }

    public String getHtml(News news){

        Cursor cursor = mSQLiteDatabase.query(TABLE_NEWS, null,
                "classification=? and channel=? and title=? and link=?",
                new String[]{news.classification, news.channel, news.title, news.link},
                null, null, null, null);
        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex("html"));
        } else return null;
    }

    public ArrayList<News> search(String keyword){
        ArrayList<News> newsArrayList = new ArrayList<>();
        Cursor cursor = mSQLiteDatabase.query(TABLE_NEWS, null,
                "title=%?% and description=%?%",
                new String[]{keyword, keyword},
                null, null, null, null);
        while (cursor.moveToNext()) {
            newsArrayList.add(getNewsFromCursor(cursor));
        }
        cursor.close();
        return newsArrayList;
    }
}

class MyPagerAdapter extends FragmentStatePagerAdapter{

    private ArrayList<Classification> classifications;
    MyPagerAdapter(FragmentManager fm, ArrayList<Classification> classificationArrayList) {
        super(fm);
        this.classifications = classificationArrayList;
    }

    public void setClassifications(ArrayList<Classification> classifications){
        this.classifications = classifications;
    }
    @Override
    public Fragment getItem(int position) {
        NewsListFragment fragment = new NewsListFragment();
        fragment.setParas(classifications.get(position));
        return fragment;
    }

     public Object instantiateItem(ViewGroup container, int position) {
         Fragment fragment = (Fragment) super.instantiateItem(container,position);
         return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return classifications.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}

public class NewsListFragment extends Fragment {
    final String LOAD_LIMIT = "10";
    Classification classification;
    MySQLiteOpenHelper helper;
    LayoutInflater inflater;
    ViewGroup container;
    View view;
    Handler handler;
    Context mContext;
    MainActivity mainActivity;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private ArrayList<News> showNewsList = new ArrayList<>();
    public NewsListFragment(){ }

    public void setParas(Classification classification) {
        this.classification = classification;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ArrayList<Channel> channels = classification.getChannels();
        this.inflater = inflater;
        this.container = container;
        this.mContext = getContext();
        this.helper = new MySQLiteOpenHelper(getContext());
        view = inflater.inflate(R.layout.news_list, container , false);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1) {
                    updateView();
                }
            }
        };

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {//设置刷新监听器
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {//模拟耗时操作
                    @Override
                    public void run() {
                        updateNewsDateset();
                        swipeRefreshLayout.setRefreshing(false);//取消刷新
                    }
                },300);
            }
        });
        NestedScrollView nestedScrollView = (NestedScrollView)view.findViewById(R.id.nestscrollview);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == ( v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight() )) {

                    new Handler().postDelayed(new Runnable() {//模拟耗时操作
                        @Override
                        public void run() {
                            loadFromDateset();
                        }
                    },1000);
                }
            }
        });
        loadFromDateset();
        updateNewsDateset();
        return view;
    }


    private void updateView(){
        ArrayList<News> newsArrayList = showNewsList;
        for(News news : newsArrayList){
            System.out.println(news.pubDate);
        }
        Collections.sort(newsArrayList, new Comparator<News>(){
            public int compare(News o1, News o2) {
                return o2.pubDate.compareTo(o1.pubDate);

            }
        });
        System.out.println("after");
        for(News news : newsArrayList){
            System.out.println(news.pubDate);
        }
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.news_list);
        linearLayout.removeAllViews();
        for(final News news: newsArrayList) {

            View newView = inflater.inflate(R.layout.news, container, false);

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
            View line = new View(getContext());
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
                    Intent intent = new Intent(getContext(), NewsDisplay.class);
                    intent.putExtra("news", news);
                    startActivityForResult(intent, 1);
                }
            });
        }
    }



    private void updateNewsDateset(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Channel channel : classification.getChannels()) {
                    try {
                        URL url = new URL(channel.getHref());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(2000);
                        conn.setReadTimeout(2000);
                        InputStreamReader inputStream = new InputStreamReader(conn.getInputStream());
                        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                        String result = scanner.hasNext() ? scanner.next() : "";
                        Pattern pattern = Pattern.compile("encoding=\"(.*)\"");
                        Matcher matcher = pattern.matcher(result);
                        matcher.find();
                        String charset = matcher.group(1);
                        HttpURLConnection _conn = (HttpURLConnection) url.openConnection();
                        InputStreamReader _inputStream = new InputStreamReader(_conn.getInputStream(), charset);
                        Scanner _scanner = new Scanner(_inputStream).useDelimiter("\\A");
                        String _result = _scanner.hasNext() ? _scanner.next() : "";
                        org.jsoup.nodes.Document document = Jsoup.parse(_result);
                        Elements nodes = document.getElementsByTag("item");

                        for (Element node : nodes) {
                            String title = node.getElementsByTag("title").text();
                            String link = node.toString().split("<link>")[1].split("\n")[0];
                            String author = node.getElementsByTag("author").text();
                            String description = node.getElementsByTag("description").text();
                            String pubDate = node.getElementsByTag("pubDate").text();
                            News news = new News(title, link, author, description, pubDate, classification.getName(), channel.getName());
                            if(!helper.isNewsIn(news)) {
                                showNewsList.add(news);
                                helper.insertNews(news);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("network error");
                        e.printStackTrace();
                    }
                }
                Message message = handler.obtainMessage(1);
                handler.sendMessage(message);
            }
        });
        thread.start();
    }

    private void loadFromDateset(){
        Collections.sort(showNewsList, new Comparator<News>(){
            public int compare(News o1, News o2) {
                return o2.pubDate.compareTo(o1.pubDate);
            }
        });
        String time = null;
        if(showNewsList.size() == 0){
            time = simpleDateFormat.format(new Date());
        } else {
            time = showNewsList.get(showNewsList.size() - 1).pubDate;
        }

        ArrayList<News> addOn = helper.getNewsByChannel(classification, time, LOAD_LIMIT);
        for(News news : addOn) showNewsList.add(news);
        updateView();
    }
}
