package com.junmeng.aad;

import com.junmeng.annotation.InjectObject;
import com.junmeng.annotation.WorkInBackground;
import com.junmeng.annotation.WorkInMainThread;
import com.junmeng.api.AwesomeTool;

/**
 * Created by hwj on 2017/3/13.
 */

public class Presenter {
    @InjectObject
    PresenterHelper helper;

    public Presenter(){
        AwesomeTool.inject(this);
    }

    @WorkInBackground
    public void needWorkInBackground(){
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @WorkInMainThread
    public void needWorkInMainThread(){
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void quit(){
        helper.quit();
    }

}
