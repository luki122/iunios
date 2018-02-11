package com.aurora.note.activity;

import android.app.Activity;
import android.os.Message;

import com.aurora.note.common.MessageHandler;
import com.aurora.note.common.WeakHandler;

/**
 * BaseActivity
 * @author JimXia
 * 2014-6-27 上午9:46:40
 */
public abstract class BaseActivity extends Activity implements MessageHandler {

    protected WeakHandler mHandler = new WeakHandler(this);

    @Override
    public void handleMessage(Message msg) {
        
    }

}