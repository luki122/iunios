package com.android.contacts.list;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import aurora.widget.AuroraTextView;

import com.android.contacts.R;

public class AuroraSimContactListAdapter_3rd extends BaseAdapter {
    private static final String TAG = AuroraSimContactListAdapter_3rd.class.getSimpleName();
    
    private Context mContext;
    private ArrayList<String> mNamesArray = new ArrayList<String>();
    private ArrayList<String> mPhoneNumberArray = new ArrayList<String>();
    
    public AuroraSimContactListAdapter_3rd(Context context, ArrayList<String> name, ArrayList<String> phone) {
    	mContext = context;
    	mNamesArray = name;
    	mPhoneNumberArray = phone;
    }
    
    static class ViewHolder {
		AuroraTextView nameTv;
		AuroraTextView numberTv;
        int position;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mNamesArray.size();
	}


	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vholder;
		
		if (null == convertView) {
			vholder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.aurora_sim_contacts_list_item_3rd, parent, false);
			vholder.nameTv = (AuroraTextView)convertView.findViewById(R.id.name);
			vholder.numberTv = (AuroraTextView)convertView.findViewById(R.id.number);
			vholder.position = position;
			convertView.setTag(vholder);
		} else {
			vholder = (ViewHolder)convertView.getTag();
		}
		
		vholder.nameTv.setText(mNamesArray.get(position));
		vholder.numberTv.setText(mPhoneNumberArray.get(position));
		
		return convertView;
	}

	public String getName(int position) {
		return mNamesArray.get(position);
	}
    
	public String getPhoneNumber(int position) {
		return mPhoneNumberArray.get(position);
	}
} 
