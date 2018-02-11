package com.aurora.view;
// Aurora xuyong 2013-09-26 created for aurora.s new feature
import java.util.Iterator;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
// Aurora xuyong 2013-09-25 added for aurora's new feature start
import com.android.mms.ui.MessageUtils;
// Aurora xuyong 2013-09-25 added for aurora's new feature end
import com.android.mms.MmsApp;
import com.android.mms.R;
import android.content.Context;
// Aurora xuyong 2013-09-25 added for aurora's new feature start
import android.graphics.Typeface;
// Aurora xuyong 2013-09-25 added for aurora's new feature end
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
// Aurora xuyong 2013-09-25 added for aurora's new feature start
import android.widget.ImageButton;
// Aurora xuyong 2013-09-25 added for aurora's new feature end
import android.widget.TextView;

import android.os.Handler;
import android.os.Message;
import android.util.*;

public class AuroraGroupRecipientsAdapter extends BaseAdapter{
    
    public final static int E_DIAL = 0x123;
    public final static int E_VIEW = 0x124;
    public final static int N_DIAL = 0x125;
    public final static int N_CHECK = 0x126;
    
    private final LayoutInflater mInflater;
    private ContactList mList;
    private final Handler mHandler;
   // Aurora xuyong 2014-07-19 added for sougou start
    private Context mContext;
   // Aurora xuyong 2014-07-19 added for sougou end
    
    public AuroraGroupRecipientsAdapter(Context context, ContactList list, Handler handler) {
        super();
        mInflater = LayoutInflater.from(context);
        mList = list;
        mHandler = handler;
      // Aurora xuyong 2014-07-19 added for sougou start
        mContext = context;
      // Aurora xuyong 2014-07-19 added for sougou end
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
    
    // Aurora xuyong 2013-12-17 added for aurora's new feature start
    static class ViewHolder {
        View mView;
        TextView mNameView;
        TextView mNumberView;
        TextView mOtherView;
        ImageButton mAddButton;
        ImageButton mViewButton;
    }
    // Aurora xuyong 2013-12-17 added for aurora's new feature end

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        // Aurora xuyong 2013-12-17 added for aurora's new feature start
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.aurora_group_recipients_exist, null);
            holder = new ViewHolder();
            holder.mView = view;
            holder.mNameView = (TextView)view.findViewById(R.id.aurora_reci_name_exist);
            holder.mNumberView = (TextView)view.findViewById(R.id.aurora_reci_num_exist);
            holder.mOtherView = (TextView)view.findViewById(R.id.aurora_reci_other_exist);
            holder.mViewButton = (ImageButton)view.findViewById(R.id.aurora_check_exists);
            holder.mAddButton = (ImageButton)view.findViewById(R.id.aurora_add_add);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final Contact contact = mList.get(position);
        holder.mView.setOnClickListener(new View.OnClickListener() {
        // Aurora xuyong 2013-12-17 added for aurora's new feature end
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (null != mHandler) {
                    Message msg = Message.obtain(mHandler, E_DIAL);
                    msg.obj = contact;
                    msg.sendToTarget();
                }
            }
        });
        // Aurora xuyong 2013-12-17 added for aurora's new feature start
        holder.mNumberView.setText(contact.getNumber());
        // Aurora xuyong 2014-04-03 deleted for aurora's new featrue start
        //holder.mNumberView.setTypeface(Typeface.createFromFile("system/fonts/number.ttf"));
        // Aurora xuyong 2014-04-03 deleted for aurora's new featrue end
      // Aurora xuyong 2014-07-19 modified for sougou start
        holder.mOtherView.setText(MessageUtils.getNumAreaFromAora(mContext, contact.getNumber()));
      // Aurora xuyong 2014-07-19 modified for sougou end
        if (contact.existsInDatabase()) {
            holder.mAddButton.setVisibility(View.GONE);
            holder.mNameView.setText(contact.getName());
        // Aurora xuyong 2013-09-25 modified for aurora's new feature end
            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
        // Aurora xuyong 2013-12-17 added for aurora's new feature end
                
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, E_VIEW);
                        msg.obj = contact;
                        msg.sendToTarget();
                    }
                }
            });
        } else {
           // Aurora xuyong 2013-09-25 modified for aurora's new feature start
           // Aurora xuyong 2013-12-17 added for aurora's new feature start
            holder.mViewButton.setVisibility(View.GONE);
            // Aurora xuyong 2013-12-06 modified for aurora's new feature start
            //nameView.setTypeface(Typeface.createFromFile("system/fonts/number.ttf"));
            // Aurora xuyong 2013-12-06 modified for aurora's new feature end
            holder.mNameView.setText(contact.getNumber());
            holder.mAddButton.setOnClickListener(new View.OnClickListener() {
            // Aurora xuyong 2013-12-17 added for aurora's new feature end
            // Aurora xuyong 2013-09-25 modified for aurora's new feature end
                
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (null != mHandler) {
                        Message msg = Message.obtain(mHandler, N_CHECK);
                        msg.obj = contact;
                        msg.sendToTarget();
                    }
                }
            });
            // Aurora xuyong 2013-09-25 deleted for aurora's new feature start
            //numberNewView.setText(contact.getNumber());
            // Aurora xuyong 2013-09-25 deleted for aurora's new feature end
        }
        return view;
    }


}
