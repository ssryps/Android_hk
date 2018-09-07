package com.java.shiyusong;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;


class Channel implements Serializable{
    private String href, name, parent;
    private boolean chosen;
    public Channel(String name, String href){
        this.href = href;
        this.name = name;
        this.chosen = false;
    }
    public String getName() { return name; }
    public String getHref() { return href; }
    public void setName(String name) { this.name = name; }
    public void setHref(String href) { this.href = href; }
    public void setChosen(boolean chosen) { this.chosen = chosen; }
    public boolean getChosen() { return chosen; }
}
class Classification implements Serializable {
    private ArrayList<Channel> channels;
    private String name;
    public Classification(String name){
        channels = new ArrayList<>();
        this.name = name;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public void addChannel(Channel channel){
        channels.add(channel);
    }
    public ArrayList<Channel> getChannels() {
        return channels;
    }
    public boolean getChosen(){
        for(Channel channel: channels){
            if(channel.getChosen()){
                return true;
            }
        }
        return false;
    }
}
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<Classification> classifications;
    MyPagerAdapter myPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        loadClassifications();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.maintab);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_page);
        myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), availableClassifications());
        viewPager.setAdapter(myPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        int tabCount = tabLayout.getTabCount();
        for(int i = 0; i < tabCount; i++){
            //获取每个tab
            //           while(!classifications.get(i).getChosen())i++;
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setText(availableClassifications().get(i).getName());
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search_mag_icon) {
            Intent intent = new Intent(MainActivity.this, SearchList.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.subscription) {
            Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
            ArrayList<Classification> arrayList = new ArrayList<>(this.classifications);
            intent.putExtra("classifications", arrayList);
            startActivityForResult(intent, 0);
        } else if (id == R.id.collection) {
            Intent intent = new Intent(MainActivity.this, CollectList.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 0){
            this.classifications = (ArrayList<Classification>)data.getSerializableExtra("classifications");
            saveClassificationsConfig();
            changeMainNewsList();
        }
    }

    private void loadClassifications(){
        SharedPreferences sharedPreferences = getSharedPreferences("classifications", Context.MODE_PRIVATE);
        classifications = new ArrayList<Classification>();
        TypedArray paths = getResources().obtainTypedArray(R.array.path);
        TypedArray hrefs = getResources().obtainTypedArray(R.array.href);
        for(int i = 0; i < paths.length(); i++){
            String href = hrefs.getString(i);
            String parent = paths.getString(i).split("/")[0];
            String name = paths.getString(i).split("/")[1];
            boolean flag = false;
            boolean isChosen = sharedPreferences.getBoolean(parent + ":" + name, false);
            for(Classification classification : classifications){
                if(classification.getName().equals(parent)){
                    Channel temp = new Channel(name, href);
                    temp.setChosen(isChosen);
                    classification.addChannel(temp);
                    flag = true;
                }
            }
            if(!flag){
                Classification tmp = new Classification(parent);
                Channel channel = new Channel(name, href);
                channel.setChosen(isChosen);
                tmp.addChannel(channel);
                classifications.add(tmp);

            }
        }
    }

    private ArrayList<Classification> availableClassifications(){
        ArrayList<Classification> returnValue = new ArrayList<>();
        for(Classification classification: classifications){
            Classification reClass = new Classification(classification.getName());
            boolean isChosen = false;
            for(Channel channel: classification.getChannels()) {
                if(channel.getChosen()){
                    isChosen = true;
                    reClass.addChannel(channel);
                }
            }
            if(isChosen)returnValue.add(reClass);
        }
        return returnValue;
    }

    private void changeMainNewsList(){
        myPagerAdapter.setClassifications(availableClassifications());
        myPagerAdapter.notifyDataSetChanged();
        int tabCount = availableClassifications().size();
        for(int i = 0; i < tabCount; i++){
            TabLayout tabLayout = (TabLayout) findViewById(R.id.maintab);
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setText(availableClassifications().get(i).getName());
        }
    }

    private void saveClassificationsConfig(){
        SharedPreferences sharedPreferences = getSharedPreferences("classifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for(Classification classification: classifications){
            ArrayList<Channel> channels = classification.getChannels();
            for(Channel channel: channels){
                editor.putBoolean(classification.getName() + ":" + channel.getName(), channel.getChosen());
            }
        }
        editor.commit();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

//
//class MyPagerAdapter extends FragmentPagerAdapter {
//
//    private ArrayList<Classification> classifications;
//    MyPagerAdapter(FragmentManager fm, ArrayList<Classification> classificationArrayList) {
//        super(fm);
//        classifications = classificationArrayList;
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        Fragment fragment = new Fragment();
//        return fragment;
//    }
//
//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//
//        return super.instantiateItem(container, position);
//    }
//
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
//    }
//
//    @Override
//    public int getCount() {
//        return classifications.size();
//    }
//}
////      code to get urls
////    Thread thread = new Thread(new Runnable() {
////        @Override
////        public void run() {
////            try {
////
////                URL url = new URL(getResources().getString(R.string.rss_site));
////                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
////                conn.setConnectTimeout(1000);
////                conn.setReadTimeout(2000);
////                InputStreamReader inputStream = new InputStreamReader(conn.getInputStream(), "gbk");
////                Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
////                String result = scanner.hasNext() ? scanner.next() : "";
////                org.jsoup.nodes.Document document = Jsoup.parse(result);
////                Elements nodes = document.getElementById("rss_nav").select("a[href]");
////                for(Element element: nodes) {
////                    String href = element.attr("href"), name = element.text();
////                    if (href == "http://rss.qq.com/index.shtml") continue;
////                    try {
////                        URL _url = new URL(href);
////                        HttpURLConnection _conn = (HttpURLConnection) _url.openConnection();
////                        _conn.setConnectTimeout(1000);
////                        _conn.setReadTimeout(2000);
////                        InputStreamReader _inputStream = new InputStreamReader(_conn.getInputStream(), "gbk");
////                        Scanner _scanner = new Scanner(_inputStream).useDelimiter("\\A");
////                        String _result = _scanner.hasNext() ? _scanner.next() : "";
////                        org.jsoup.nodes.Document _document = Jsoup.parse(_result);
////                        Elements _nodes = _document.getElementsByTag("h4");
////                        for (Element _element : _nodes) {
////                            String input1 = name + "/" + _element.child(0).child(0).text();
////                            String input2 = _element.child(1).child(0).attr("href");
////                            System.out.println("<item>" + input1 + "</item>");
////                        }
////
////                    } catch (Exception e) { }
////                }
////
////            } catch (MalformedURLException e) {
////                e.printStackTrace();
////            } catch (IOException e){
////                e.printStackTrace();
////            }
////        }
////    });
////        thread.start();
