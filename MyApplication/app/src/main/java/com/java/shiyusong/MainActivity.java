package com.java.shiyusong;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.SQLOutput;
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
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bmob.initialize(this, "4503d9c9cbab1c6b86670233ec8fdfd2", "Bmob");

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

        loadUserInfo();
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_main_search) {
            Intent intent = new Intent(MainActivity.this, SearchList.class);
            startActivityForResult(intent, 5);
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
            startActivityForResult(intent, 1);
        } else if (id == R.id.collection) {
            Intent intent = new Intent(MainActivity.this, CollectList.class);
            startActivityForResult(intent, 2);

        } else if (id == R.id.recommend) {
            Intent intent = new Intent(MainActivity.this, Recommend.class);
            ArrayList<Classification> arrayList = new ArrayList<>(this.classifications);
            intent.putExtra("classifications", arrayList);
            startActivityForResult(intent, 3);
        } else if (id == R.id.hot_list) {
            Intent intent = new Intent(MainActivity.this, TodayList.class);
            startActivityForResult(intent, 6);
        } else if (id == R.id.sign_in_and_up) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, 4);
        } else if (id == R.id.signout) {
            if (BmobUser.getCurrentUser() == null) {
                Toast.makeText(getApplicationContext(), "You have not signed in", Toast.LENGTH_LONG).show();
            } else {
                BmobUser.logOut();
                loadUserInfo();
                Toast.makeText(getApplicationContext(), "sign out successfully", Toast.LENGTH_LONG).show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 10){
            this.classifications = (ArrayList<Classification>)data.getSerializableExtra("classifications");
            saveClassificationsConfig();
            changeMainNewsList();
        }

        if(resultCode == 20 || resultCode == 30 || resultCode == 50){
            changeMainNewsList();
        }

        if(resultCode == 40 || resultCode == 41){
            loadUserInfo();
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
    private void loadUserInfo(){
        BmobUser user = BmobUser.getCurrentUser();
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView username = (TextView) headerView.findViewById(R.id.username);
        TextView email = (TextView) headerView.findViewById(R.id.user_email);
        if(user != null) {
            username.setText(user.getUsername());
            email.setText(user.getEmail());
        } else{
            username.setText("未登录");
            email.setText("");
        }
    }
}
