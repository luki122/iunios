package com.aurora.view;
// Aurora xuyong 2013-10-14 created for aurora's new feature 
import java.util.ArrayList;
import java.util.Iterator;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.aurora.mms.ui.ClickContent;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import android.os.Handler;
import android.os.Message;
import android.util.*;

public class AuroraMultiLinkAdapter extends BaseAdapter{
    
    public final static int LINK_CLICK = 0x130;
    
    private final LayoutInflater mInflater;
    private ArrayList<String> mList;
    private final Handler mHandler;
    
    public AuroraMultiLinkAdapter(Context context, ArrayList<String> list, Handler handler) {
        super();
        mInflater = LayoutInflater.from(context);
        mList = list;
        mHandler = handler;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList.toArray().length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewGroup view = null;
        final String link = mList.get(position);
        String nlink = null;
        int index = link.indexOf(":");
        if (index >= 0) {
            if (index == "http:".length() - 1) {
                nlink = link.substring(index + 3);
            } else {
                nlink = link.substring(index + 1);
            }
        }
        view = (ViewGroup)mInflater.inflate(R.layout.aurora_multi_link_item, null);
        TextView linkName = (TextView)view.findViewById(R.id.linkname);
        linkName.setText(nlink);
        linkName.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (null != mHandler) {
                    Message msg1 = Message.obtain(mHandler, LINK_CLICK);
                    ClickContent cc = new ClickContent();
                    cc.setValue(link);
                    msg1.obj = cc;
                    msg1.sendToTarget();
                }
            }
        });
        return view;
    }


}
