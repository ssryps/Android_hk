package com.java.shiyusong;

import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.*;

import java.util.ArrayList;

public class SubscriptionActivity extends ActivityGroup {
    ArrayList<Classification> classifications;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        Intent intent = getIntent();
        classifications = (ArrayList<Classification>)intent.getSerializableExtra("classifications");
        TabHost tabhost =(TabHost) findViewById(R.id.mytab);
        tabhost.setup(this.getLocalActivityManager());
        for(Classification classification: classifications){
            String name = classification.getName();
            ArrayList<Channel> channels = classification.getChannels();
            Intent chlidIntent = new Intent(SubscriptionActivity.this, SubscriptionLayoutActivity.class);
            chlidIntent.putExtra("name", name);
            chlidIntent.putExtra("channels", channels);

            tabhost.addTab(tabhost.newTabSpec(name).setIndicator(name).setContent(chlidIntent));
        }
        for(int i=0;i< tabhost.getTabWidget().getChildCount();i++){
            TextView tv = (TextView)tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.rgb(255, 255, 255));
        }

        setResult(10, intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("classifications", classifications);
        setResult(10, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

}

