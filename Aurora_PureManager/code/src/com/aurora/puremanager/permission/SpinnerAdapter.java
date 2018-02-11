package com.aurora.puremanager.permission;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aurora.puremanager.R;

/**
 * 自定义适配器类
 * 
 */
public class SpinnerAdapter extends BaseAdapter {
	private List<SpinnerInfo> mList;
	private Context mContext;

	public SpinnerAdapter(Context pContext, List<SpinnerInfo> pList) {
		this.mContext = pContext;
		this.mList = pList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 下面是重要代码
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		convertView = layoutInflater.inflate(R.layout.spinner_item, null);
		if (convertView != null) {
			TextView TextView1 = (TextView) convertView.findViewById(R.id.text1);
			TextView1.setText(mList.get(position).getPersonName());
		}
		return convertView;
	}
}