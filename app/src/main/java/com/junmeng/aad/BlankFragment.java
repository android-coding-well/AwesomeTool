package com.junmeng.aad;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.junmeng.aad.databinding.FragmentBlankBinding;
import com.junmeng.annotation.InjectObject;
import com.junmeng.annotation.WorkInBackground;
import com.junmeng.annotation.WorkInMainThread;
import com.junmeng.api.AwesomeTool;

public class BlankFragment extends Fragment {
    private static final String TAG = "BlankFragment";
    FragmentBlankBinding binding;
    @InjectObject(priority = Process.THREAD_PRIORITY_AUDIO)
    BlankFragmentHelper blankFragmentHelper;

    public BlankFragment() {
        // Required empty public constructor
    }

    public static BlankFragment newInstance() {
        BlankFragment fragment = new BlankFragment();
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        blankFragmentHelper.quit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_blank, container, false);
        binding.tvText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "请稍等", Toast.LENGTH_SHORT).show();
                blankFragmentHelper.needWorkInThread("1234",5,6.7,new Test());
            }
        });
        AwesomeTool.inject(this);
        return binding.getRoot();
    }

    @WorkInBackground
    public void needWorkInThread(String str,int i,double d,Test test){
        try {
            Log.i(TAG, "needWorkInThread: "+str+i+d+test.toString());
            Thread.sleep(8000);
            blankFragmentHelper.needWorkInMainThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @WorkInMainThread
    public void needWorkInMainThread() {
        binding.tvText.setText("Hello");
    }

}
