package com.junmeng.aad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FragActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag);
        BlankFragment blankFragment=BlankFragment.newInstance();
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),blankFragment,R.id.contentFrame);
    }
}
