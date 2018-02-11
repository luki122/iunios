package com.aurora.view;
// Aurora xuyong 2013-12-11 created for aurora's new feature
import java.util.ArrayList;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
// Aurora xuyong 2013-12-17 added for aurora's new feature start
import com.android.mms.ui.ComposeMessageActivity;
// Aurora xuyong 2013-12-17 added for aurora's new feature end

import com.android.mms.R;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
// Aurora xuyong 2013-12-17 modified for aurora's new feature start
import aurora.widget.AuroraCheckBox;
// Aurora xuyong 2013-12-17 modified for aurora's new feature end
import android.widget.TextView;

public class AuroraGroupReciResendAdapter extends BaseAdapter{
    
    private final LayoutInflater mInflater;
    ArrayList<String> mNumbers;
    ArrayList<String> mNames = new ArrayList<String>();
    // Aurora xuyong 2013-12-17 added for aurora's new feature start
    ArrayList<Long> mIdList;
    // Aurora xuyong 2013-12-17 added for aurora's new feature end
    // Aurora xuyong 2013-12-17 modified for aurora's new feature start
    AuroraCheckBox check;
    // Aurora xuyong 2013-12-17 modified for aurora's new feature end
    TextView viewName;
    TextView viewNumber;
    
    // Aurora xuyong 2013-12-27 modified for aurora's new feature start
    public AuroraGroupReciResendAdapter(Context context, ArrayList<Long> idList, ArrayList<String> numberList, ContactList contactList) {
    // Aurora xuyong 2013-12-27 modified for aurora's new feature end
        super();
        mInflater = LayoutInflater.from(context);
        // Aurora xuyong 2013-12-17 added for aurora's new feature start
        mIdList = idList;
        // Aurora xuyong 2013-12-17 added for aurora's new feature end
        mNumbers = numberList;
        for (String number : mNumbers) {
            // Aurora xuyong 2013-12-27 modified for aurora's new feature start
            for (Contact contact : contactList) {
                if (contact.getNumber().equals(number)) {
                    mNames.add(contact.getName());
                    break;
                }
            }
            // Aurora xuyong 2013-12-27 modified for aurora's new feature end
        }
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (mNumbers != null) {
            return mNumbers.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mNumbers.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    
    // Aurora xuyong 2013-12-17 addede for aurora's new feature start
    static class ViewHolder {
        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
        AuroraCheckBox mCheckBox;
        // Aurora xuyong 2013-12-17 modified for aurora's new feature end
        TextView mNameTV;
        TextView mNumberTV;
    }
    // Aurora xuyong 2013-12-17 added for aurora's new feature end
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.aurora_reci_resend_item, null);
            holder = new ViewHolder();
            // Aurora xuyong 2013-12-17 modified for aurora's new feature start
            holder.mCheckBox = (AuroraCheckBox)view.findViewById(R.id.aurora_reci_check);
            // Aurora xuyong 2013-12-17 modified for aurora's new feature end
            holder.mNameTV = (TextView)view.findViewById(R.id.aurora_reci_name);
            holder.mNumberTV = (TextView)view.findViewById(R.id.aurora_reci_number);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        // Aurora xuyong 2013-12-17 modified for aurora's new feature start
        holder.mCheckBox.auroraSetChecked(ComposeMessageActivity.selectResendMap.get(mIdList.get(position)), false);
        // Aurora xuyong 2013-12-17 modified for aurora's new feature end
        holder.mNameTV.setText(mNames.get(position));
        holder.mNumberTV.setText(mNumbers.get(position));
        // Aurora xuyong 2013-12-17 modified for aurora's new feature end
        return view;
    }

}
