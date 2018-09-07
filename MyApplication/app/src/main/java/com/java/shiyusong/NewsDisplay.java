package com.java.shiyusong;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class NewsDisplay extends AppCompatActivity {
    News news;
    MySQLiteOpenHelper helper;

    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_display);
        mContext = this;
        this.helper = new MySQLiteOpenHelper(this);

        Intent intent = getIntent();
        news = (News)intent.getSerializableExtra("news");
        getSupportActionBar().setTitle(news.classification + "/" + news.channel);

        final ImageButton shareImageButton = (ImageButton)findViewById(R.id.collectButton);
        ImageButton wechatImageButton = (ImageButton)findViewById(R.id.wechatShareButton);
        ImageButton qqShareButton = (ImageButton)findViewById(R.id.qqShareButton);
        ImageButton weiboImageButton = (ImageButton)findViewById(R.id.weiboShareButton);
        news.setIsCollect(helper.getIsCollect(news));
        if(news.isCollect.equals("true"))shareImageButton.setImageResource(R.drawable.collect_on);
        else shareImageButton.setImageResource(R.drawable.collect_off);
        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isNow = news.getIsCollect().equals("false");
                news.setIsCollect(new Boolean(isNow).toString());
                helper.setIsCollect(news);
                if(isNow)shareImageButton.setImageResource(R.drawable.collect_on);
                else shareImageButton.setImageResource(R.drawable.collect_off);
            }
        });

        wechatImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        final Handler handler = new Handler(){

            class WebViewController extends WebViewClient {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

            }
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1) {
                    ArrayList<ArrayList<String>> infoList = (ArrayList<ArrayList<String>>) msg.obj;
                    ArrayList<String> infos = infoList.get(0);
                    final ArrayList<String> paras = infoList.get(1);
                    ArrayList<String> isImage = infoList.get(2);
                    LinearLayout mainArticle = (LinearLayout) findViewById(R.id.display_main_article);
                    TextView title = (TextView) findViewById(R.id.display_title);
                    TextView info = (TextView) findViewById(R.id.display_info);
                    title.setText(infos.get(0));
                    String temp = "";
                    for(int i = 1; i < infos.size(); i++){
                        temp += infos.get(i);
                        temp += "   ";
                    }
                    info.setText(temp);
                    for(int i = 0; i < paras.size(); i++){
                        if(isImage.get(i).equals("true")){
                            final String url = paras.get(i);
                            final ImageView imageView = new ImageView(mContext);
                            imageView.setPadding(0, 10, 0, 10);
                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            imageView.setLayoutParams(layoutParams);
                            Bitmap bitmap = helper.getBitmap(url);
                            if(bitmap == null) {
                                final Handler imageHandler = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        if (msg.what == 1) {
                                            Bitmap bitmap = (Bitmap) msg.obj;
                                            helper.insertBitmap(bitmap, url);
                                            imageView.setImageBitmap(bitmap);
                                        }
                                    }
                                };
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            InputStream in = new java.net.URL(url).openStream();
                                            Bitmap mIcon11 = BitmapFactory.decodeStream(in);
                                            Message message = imageHandler.obtainMessage(1, mIcon11);
                                            imageHandler.sendMessage(message);
                                        } catch (Exception e) {
                                            Toast.makeText(mContext, "error when loading pictures", Toast.LENGTH_LONG);
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.start();
                            } else{
                                imageView.setImageBitmap(bitmap);
                            }
                            mainArticle.addView(imageView);
                        } else{
                            TextView textView = new TextView(mContext);
                            textView.setText("      " + paras.get(i));
                            textView.setTextSize(16);
                            textView.setTextColor(getResources().getColor(R.color.colorNewsDisplayArticle));
                            textView.setPadding(0, 15, 0, 15);
                            mainArticle.addView(textView);
                        }
                    }

                }
                if(msg.what == 2){
                    TextView title = (TextView) findViewById(R.id.display_title);
                    title.setText(news.title);
                    WebView webView = new WebView(mContext);
                    webView.setWebViewClient(new WebViewClient() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                            Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();
                        }
                        @TargetApi(android.os.Build.VERSION_CODES.M)
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                            // Redirect to deprecated method, so you can use it in all SDK versions
                            onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                        }
                    });
                    webView.loadUrl(news.link);

                    webView.getSettings().setAppCacheEnabled(true);
                 //   webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setJavaScriptEnabled(true);
                    webSettings.setDomStorageEnabled(true);

                    LinearLayout mainArticle = (LinearLayout) findViewById(R.id.display_main_article);
                    mainArticle.addView(webView);
                }
                if(msg.what == 3){
                    Toast.makeText(mContext, "Network Error", Toast.LENGTH_LONG * 3).show();
                }
            }
        };
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                String result = helper.getHtml(news);
                if (result.equals("")) {
                    try {
                        URL url = new URL(news.link.replace("http", "https"));
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(3000);
                        conn.setReadTimeout(3000);
                        conn.setInstanceFollowRedirects(true);
                        InputStreamReader inputStream = null;
                        try {
                            inputStream = new InputStreamReader(conn.getInputStream(), "gb2312");
                        } catch (SocketTimeoutException e) {
                            url = new URL(news.link);
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(3000);
                            conn.setReadTimeout(3000);
                            conn.setInstanceFollowRedirects(true);
                            inputStream = new InputStreamReader(conn.getInputStream(), "gb2312");
                        }
                        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                        result = scanner.hasNext() ? scanner.next() : "";
                        helper.insertHtml(news, result);
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.sendMessage(handler.obtainMessage(3));
                    }
                }
                try {
                    org.jsoup.nodes.Document document = Jsoup.parse(result);
                    Element qq_article = document.getElementsByAttributeValue("class", "qq_article").get(0);
                    ArrayList<String> infoList = new ArrayList<>();
                    String title = qq_article.getElementsByAttributeValue("class", "hd").get(0).getElementsByTag("h1").text();
                    infoList.add(title);
                    Elements infoElements = qq_article.getElementsByAttributeValue("class", "qq_bar clearfix").get(0)
                            .getElementsByAttributeValue("class", "a_Info").get(0).children();
                    for (Element element : infoElements) {
                        infoList.add(element.text());
                    }
                    ArrayList<String> mainArticle = new ArrayList<>();
                    ArrayList<String> isImage = new ArrayList<>();
                    Elements pars = qq_article.getElementById("Cnt-Main-Article-QQ").getElementsByTag("p");
                    for (Element par : pars) {
                        int s = par.getElementsByTag("img").size();
                        if (s != 0) {
                            String imagePath = par.getElementsByTag("img").get(0).attr("src");
                            if (imagePath.isEmpty()) continue;
                            if (!imagePath.startsWith("https:")) {
                                imagePath = "https:".concat(imagePath);
                            }
                            mainArticle.add(imagePath);
                        } else {
                            if (par.text().isEmpty()) continue;
                            mainArticle.add(par.text());
                        }
                        isImage.add(new Boolean(s != 0).toString());
                    }
                    ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
                    temp.add(infoList);
                    temp.add(mainArticle);
                    temp.add(isImage);
                    Message message = handler.obtainMessage(1, temp);
                    handler.sendMessage(message);
                } catch (Exception e){
                    Message message = handler.obtainMessage(2);
                    handler.sendMessage(message);
                }

            }
        });
        thread.start();
        setResult(1, intent);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.putExtra("news", news);
        setResult(0, intent);
        super.onDestroy();
    }
}

