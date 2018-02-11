package com.aurora.iunivoice.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.ForumInfo;
import com.aurora.iunivoice.R;

public class ForumChoiceListAdapter extends BaseAdapter {
	
	private int checkIndex = -1;

	private ArrayList<ForumInfo> forumList;
	private LayoutInflater inflater;

	public ForumChoiceListAdapter(Context context, ArrayList<ForumInfo> forumList) {
		this.forumList = forumList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return forumList.size();
	}

	@Override
	public Object getItem(int position) {
		return forumList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Long.valueOf(forumList.get(position).getFid());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_forum_choice, null);
			holder = new Holder();
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.iv_check = (ImageView) convertView.findViewById(R.id.iv_check);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		ForumInfo forum = forumList.get(position);

		holder.tv_title.setText(forum.getName());
		if (checkIndex == position) {
			holder.iv_check.setImageResource(R.drawable.icon_checkbox_checked);
		} else {
			holder.iv_check.setImageResource(R.drawable.icon_checkbox_uncheck);
		}

		return convertView;
	}

	private static class Holder {
		TextView tv_title;
		ImageView iv_check;
	}

	public int getCheckIndex() {
		return checkIndex;
	}

	public void setCheckIndex(int checkIndex) {
		this.checkIndex = checkIndex;
	}

}
