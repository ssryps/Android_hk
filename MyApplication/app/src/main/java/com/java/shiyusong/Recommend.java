package com.java.shiyusong;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Record{
    public String classification, channel, time;
    Record(String classification, String channel, String time){
        this.classification = classification;
        this.channel = channel;
        this.time = time;
    }
    @Override
    public boolean equals(Object obj) {
        return classification.equals(((Record)obj).classification) && channel.equals(((Record)obj).channel);
    }
}
public class Recommend extends AppCompatActivity {
    MySQLiteOpenHelper helper;
    Context mContext;
    ArrayList<Classification> classifications;
    ArrayList<News> newsArrayList = new ArrayList<>();
    public int NEWS_NUM_EACH_TIME = 20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        mContext = this;
        this.helper = new MySQLiteOpenHelper(mContext);
        Intent intent = getIntent();
        classifications = (ArrayList<Classification>)intent.getSerializableExtra("classifications");
        generateRecommendNews();
    }

    private void generateRecommendNews(){
        ArrayList<News> temp = new ArrayList<>();
        ArrayList<Record> records = helper.getAllRecords(100);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Hashtable<String, Long> hashtable = new Hashtable<>();
        for(Classification classification: classifications){
            hashtable.put(classification.getName(), new Long(1));
        }
        for(Record record: records){
            Date time = null;
            try {
                time = simpleDateFormat.parse(record.time);
            } catch (ParseException e) {
            }
            long day = (System.currentTimeMillis() - time.getTime() ) / 1000 / 60 / 60 / 24 + 1;
            long power = ((day > 10)? 1: 10 - day);
        //    String key = record.classification + "/" + record.channel;
            String key = record.classification;
            Long value = hashtable.get(key);
            if(value == null)value = new Long(0);
            hashtable.put(key, value + power);
        }
        Set set = hashtable.entrySet();
        Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set.size()]);
        Arrays.sort(entries, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                Long v1 = (Long)((Map.Entry) arg0).getValue();
                Long v2 = (Long)((Map.Entry) arg1).getValue();
                return v2.compareTo(v1);
            }
        });

        for(Classification classification: classifications) {
            for (int i = 0; i < 4; i++) {
                if(entries[i].getKey().equals(classification.getName())) {
                    Classification tmp = new Classification(classification.getName());
                    ArrayList<Channel> channels = classification.getChannels();
                    Collections.shuffle(channels);
                    for (Channel channel : channels) {
                        if (!channel.getChosen()) tmp.addChannel(channel);
                    }
                    if (tmp.getChannels().size() == 0) {
                        tmp.addChannel(channels.get(0));
                    }
                    int chosenSize = Math.min(tmp.getChannels().size(), 3);
                    ArrayList<Channel> newChan = new ArrayList<>();
                    for (int j = 0; j < chosenSize; j++) {
                        newChan.add(tmp.getChannels().get(j));
                    }
                    getNews(newChan, classification.getName(), 3);
                }
            }
        }

    }


    void getNews(final ArrayList<Channel> channels, final String name, final int limit){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1) {
                    ArrayList<News> addon = (ArrayList<News>)msg.obj;
                    UpdateUi(addon);
                }
            }
        };
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Channel channel : channels) {
                    int add_num = 0;
                    ArrayList<News> tempNews = new ArrayList<>();
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
                            News news = new News(title, link, author, description, pubDate, name, channel.getName());
                            if(!helper.isNewsIn(news)) {
                                tempNews.add(news);
                                helper.insertNews(news);
                                add_num ++;
                                if(add_num >= limit)break;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("network error");
                        e.printStackTrace();
                    }
                    if(add_num != 0){
                        Message message = handler.obtainMessage(1, tempNews);
                        handler.sendMessage(message);
                    }
                }

            }
        });
        thread.start();
    }
    private void UpdateUi(ArrayList<News> addon){
        for(News news: addon)newsArrayList.add(news);
        Collections.sort(newsArrayList, new Comparator<News>(){
            public int compare(News o1, News o2) {
                return o2.pubDate.compareTo(o1.pubDate);
            }
        });
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.recommend_list);
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
                desText = news.description.substring(0, 60) + "....";
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
