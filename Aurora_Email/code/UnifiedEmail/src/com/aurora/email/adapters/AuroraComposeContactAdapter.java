package com.aurora.email.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.mail.R;


public class AuroraComposeContactAdapter extends BaseAdapter {
	private ArrayList<String> mList;
	private Context mContext;
	private boolean mFlag;
	class ViewHold {
		TextView mContact;
		TextView mTitle;
	}

	public AuroraComposeContactAdapter(Context context,ArrayList<String> list) {
		mContext = context;
		mList = list;
	}
	public void setShowTag(boolean flag){
		mFlag = flag;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		final ViewHold vh;
		if (arg1 == null) {
			arg1 = View.inflate(mContext, R.layout.aurora_contact_item,
					null);
			vh = new ViewHold();
			vh.mContact = (TextView) arg1
					.findViewById(R.id.contact_address);
			vh.mTitle = (TextView) arg1.findViewById(R.id.contact_newly);
			arg1.setTag(vh);
		} else {
			vh = (ViewHold) arg1.getTag();
		}
		if(arg0==0&&mFlag){
			vh.mTitle.setVisibility(View.VISIBLE);
		}else{
			vh.mTitle.setVisibility(View.INVISIBLE);
		}
		vh.mContact.setText(mList.get(arg0));
		return arg1;
	}

}