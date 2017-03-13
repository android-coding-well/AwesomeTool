package com.junmeng.aad;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.junmeng.aad.databinding.ActivityMainBinding;
import com.junmeng.annotation.InjectObject;
import com.junmeng.annotation.WorkInBackground;
import com.junmeng.annotation.WorkInMainThread;
import com.junmeng.api.AwesomeTool;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @InjectObject
    MainActivityHelper awesomeThread;

    ActivityMainBinding binding;
    Presenter  presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        AwesomeTool.inject(this);
        binding.tvText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                awesomeThread.needWorkInThread();
            }
        });
        presenter=new Presenter();
        //我不会阻塞线程
        presenter.helper.needWorkInBackground();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        awesomeThread.quit();
        presenter.helper.quit();
    }

    @WorkInBackground
    public void needWorkInThread() {
        try {
            Log.i(TAG, "needWorkInThread: ");
            Thread.sleep(5000);
            awesomeThread.needWorkInMainThread();
            awesomeThread.needWorkInThread2();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @WorkInBackground
    public  void needWorkInThread2(){
        try {
            Log.i(TAG, "needWorkInThread2: ");
            Thread.sleep(8000);
            awesomeThread.needWorkInMainThread2();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @WorkInMainThread
    public void needWorkInMainThread() {
        binding.tvText.setText("我变化了，8秒后我还会变化");
    }

    @WorkInMainThread
    public void needWorkInMainThread2() {
        binding.tvText.setText("Hello!");
    }

    public void onClickFrag(View view) {
        Intent intent=new Intent(MainActivity.this,FragActivity.class);
        startActivity(intent);
    }
}
