package com.java.shiyusong;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
public class SubscriptionLayoutActivity extends AppCompatActivity {
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_layout);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        ArrayList<Channel> channels = (ArrayList<Channel>)intent.getSerializableExtra("channels");
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.subscriptionlayout);
        for(final Channel channel : channels){
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(channel.getName());
            checkBox.setTextSize(20);
            checkBox.setPadding(50, 20, 200, 20);
            checkBox.setChecked(channel.getChosen());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    for (Classification classification: ((SubscriptionActivity)getParent()).classifications){
                        if(classification.getName().equals(name)){
                            for(Channel channel1: classification.getChannels()){
                                if(channel1.getName() == channel.getName())channel1.setChosen(b);
                            }
                        }
                    }

                }
            });
            linearLayout.addView(checkBox);
        }
    }
}
