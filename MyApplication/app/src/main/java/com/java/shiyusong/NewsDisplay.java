package com.java.shiyusong;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.*;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

class NewsComment extends BmobObject{
    private String content;
    private BmobUser user;
    private String pubDate;
    private String link;
    public BmobUser getUser() {
        return user;
    }

    public void setUser(BmobUser user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}


class NewsFootPrint extends BmobObject{
    private Integer visitNum;
    private String date, title, des, link, author, pubDate, classification, channel;

    public Integer getVisitNum() {
        return visitNum;
    }

    public void setVisitNum(Integer visitNum) {
        this.visitNum = visitNum;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getClassification() {
        return classification;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}

public class NewsDisplay extends AppCompatActivity {
    News news;
    MySQLiteOpenHelper helper;
    ImageView shareImageView = null;
    boolean isload = false;
    Context mContext;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        verifyStoragePermissions(this);
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".jpeg");
            file.getParentFile().mkdirs();
            System.out.println(file.exists());
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_display);
        mContext = this;
        this.helper = new MySQLiteOpenHelper(this);

        Intent intent = getIntent();
        news = (News)intent.getSerializableExtra("news");
        helper.insertRecord(news);
        getSupportActionBar().setTitle(news.classification + "/" + news.channel);

        final ImageButton shareImageButton = (ImageButton)findViewById(R.id.collectButton);
        ImageButton wechatImageButton = (ImageButton)findViewById(R.id.wechatShareButton);
        ImageButton qqShareButton = (ImageButton)findViewById(R.id.qqShareButton);
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
                if (isload) {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, "【" + news.title + "】\n\n" + news.description + "\n\n" + news.link);
                        startActivity(Intent.createChooser(intent, "Share"));
                } else{
                    Toast.makeText(mContext, "请等待加载", Toast.LENGTH_LONG).show();
                }
            }
        });
        qqShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isload) {
                    if(shareImageView == null) {
                        Toast.makeText(mContext, "此新闻没有图片", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("image/*");
                        Uri bmpUri = getLocalBitmapUri(shareImageView);
                        intent.putExtra(Intent.EXTRA_STREAM, String.valueOf(bmpUri));
                        intent.putExtra(Intent.EXTRA_TEXT, "【" + news.title + "】\n\n" + news.description + "\n\n" + news.link);
                        startActivity(Intent.createChooser(intent, "Share"));
                    }
                } else{
                    Toast.makeText(mContext, "请等待加载", Toast.LENGTH_LONG).show();
                }
            }
        });

        final EditText editText = (EditText)findViewById(R.id.comment_input);

        Button clearButton = (Button)findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });

        Button submitButton = (Button)findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobUser user = BmobUser.getCurrentUser();
                if(user == null){
                    Toast.makeText(mContext, getResources().getText(R.string.comment_without_signin), Toast.LENGTH_LONG).show();
                    return;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                NewsComment newsComment = new NewsComment();
                newsComment.setContent(editText.getText().toString());
                newsComment.setLink(news.link);
                newsComment.setUser(user);
                newsComment.setPubDate(simpleDateFormat.format(new Date()));
                newsComment.save(new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        if(e == null){
                            Toast.makeText(mContext, getResources().getText(R.string.comment_success), Toast.LENGTH_LONG).show();
                            loadCommentList();
                            editText.setText("");
                        }else{
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.start();
                            } else{
                                imageView.setImageBitmap(bitmap);
                            }
                            if(shareImageView == null)shareImageView = imageView;
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
                    WebView webView = new WebView(mContext);//WebView)findViewById(R.id.webviewdisplay);
                    webView.setWebViewClient(new WebViewClient() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        }
                        @TargetApi(android.os.Build.VERSION_CODES.M)
                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                            onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        webView.getSettings().setSafeBrowsingEnabled(false);
                    }

                    webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    WebSettings webSettings = webView.getSettings();
                    webSettings.setUseWideViewPort(true);
                    webSettings.setJavaScriptEnabled(true);
                    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                    webSettings.setSupportMultipleWindows(true);
                    webSettings.setDatabaseEnabled(true);
                    webSettings.setGeolocationEnabled(true);
                    webSettings.setPluginState(WebSettings.PluginState.ON);
                    webSettings.setDomStorageEnabled(true);
                    String cacheDirPath = getFilesDir().getAbsolutePath() + "/webcache";
                    webSettings.setAppCachePath(cacheDirPath);
                    webSettings.setAppCacheEnabled(true);

//                    webView.loadUrl("javascript:(function() { document.getElementById('email_field').value = '" + "https://news.qq.com/a/20180906/084152.htm" + "'; })()");
                    webView.loadUrl(news.link);

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
                    isload = true;
                } catch (Exception e){
                    Message message = handler.obtainMessage(2);
                    handler.sendMessage(message);
                    isload = true;
                }

            }
        });
        thread.start();
        loadCommentList();
        leaveAFootPrint();
        setResult(1, intent);
    }


    private void loadCommentList(){
        EditText editText = (EditText)findViewById(R.id.comment_input);

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.comment_list);
        linearLayout.removeAllViews();
        BmobQuery<NewsComment> query = new BmobQuery<>();
        query.addWhereEqualTo("link", news.link);
        query.include("user");
        query.setLimit(10);
        query.findObjects(new FindListener<NewsComment>() {
            @Override
            public void done(List<NewsComment> object, BmobException e) {
                if(e==null){
                    for (NewsComment newsComment : object) {
                        View tempView = getLayoutInflater().inflate(R.layout.comment_display, linearLayout, false);

                        TextView title = tempView.findViewById(R.id.comment_display_title);
                        title.setText(newsComment.getUser().getUsername());
                        System.out.println(newsComment.getUser());
                        System.out.println(newsComment.getUser().getUsername());
                        TextView pubDate = tempView.findViewById(R.id.comment_display_pub_date);
                        pubDate.setText(newsComment.getPubDate());
                        TextView content = tempView.findViewById(R.id.comment_display_content);
                        content.setText(newsComment.getContent());

                        linearLayout.addView(tempView);
                    }
                }else{
                    if(e.getErrorCode() != 101) System.out.println(e.getMessage());
                }
            }
        });
    }

    private void leaveAFootPrint(){
        BmobQuery<NewsFootPrint> query = new BmobQuery<>();
        query.addWhereEqualTo("link", news.link);
        query.addWhereEqualTo("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        query.findObjects(new FindListener<NewsFootPrint>() {
            @Override
            public void done(List<NewsFootPrint> object, BmobException e) {
                if(e==null && object.size() > 0){
                    NewsFootPrint oldCollection = object.get(0);
                    NewsFootPrint newCollection = new NewsFootPrint();
                    newCollection.setValue("visitNum", oldCollection.getVisitNum() + 1);
                    newCollection.update(oldCollection.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                System.out.println("udpate new footprint successfully");
                            }else{
                                System.out.println("fail to update new footprint ");
                            }
                        }
                    });
                }else{
                    if((e != null && e.getErrorCode() == 101) || (e == null && object.size() == 0)){
                        NewsFootPrint newCollection = new NewsFootPrint();
                        newCollection.setLink(news.link);
                        newCollection.setDes(news.description);
                        newCollection.setTitle(news.title);
                        newCollection.setVisitNum(1);
                        newCollection.setPubDate(news.pubDate);
                        newCollection.setAuthor(news.author);
                        newCollection.setClassification(news.classification);
                        newCollection.setChannel(news.channel);
                        newCollection.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                        newCollection.save(new SaveListener<String>() {
                            @Override
                            public void done(String objectId, BmobException e) {
                                if(e==null){
                                    System.out.println("create new footprint successfully");
                                }else{
                                    System.out.println("fail to create new footprint ");
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.putExtra("news", news);
        setResult(0, intent);
        super.onDestroy();
    }
}

