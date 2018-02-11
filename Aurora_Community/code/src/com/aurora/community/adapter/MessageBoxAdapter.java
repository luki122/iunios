package com.aurora.community.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.activity.MessageBoxActivity;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.view.LeftScollDeleteRelativeLayout;
import com.aurora.community.view.LeftScollDeleteRelativeLayout.ILeftScrollListener;
import com.aurora.datauiapi.data.bean.CommentInfo;
import com.aurora.datauiapi.data.bean.MessageBoxArticleInfo;
import com.aurora.datauiapi.data.bean.MessageBoxObj;
import com.aurora.datauiapi.data.bean.PostDetailInfo;
import com.tencent.mm.sdk.platformtools.Log;

public class MessageBoxAdapter extends BaseAdapter {

	private ArrayList<MessageBoxObj> datas;
	private Context mContext;
	private LayoutInflater inflater;
	private HashMap<Integer, LeftScollDeleteRelativeLayout> viewsMap = new HashMap<Integer, LeftScollDeleteRelativeLayout>();
	
	
	public MessageBoxAdapter(ArrayList<MessageBoxObj> datas, Context mContext) {
		super();
		this.datas = datas;
		this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
		((MessageBoxActivity)mContext).showClearItem(datas.size() > 0);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	class Holder{
		ImageView user_icon,
		        content_icon;
		TextView nick_name,
		         msg_content,
		         msg_collection,
		         msg_time,
		         content;
	}
	
	private int currentRubbishPosition = -1;
	
	private String favourType = "favorite";
	private ILeftScrollListener leftScrollListener = new ILeftScrollListener() {
		
		@Override
		public void showRubbish(int position) {
			// TODO Auto-generated method stub
			currentRubbishPosition = position;
		}
		
		@Override
		public void deleteRubbish(int position) {
			// TODO Auto-generated method stub
			((MessageBoxActivity)mContext).delete(position);
		}

		@Override
		public void hideRubbish(int position) {
			// TODO Auto-generated method stub
			currentRubbishPosition = -1;
		}

		@Override
		public void startRubbish(int position) {
			// TODO Auto-generated method stub
			if(currentRubbishPosition != -1)
			{
				if(currentRubbishPosition != position&&viewsMap.get(currentRubbishPosition) != null)
				{
					viewsMap.get(currentRubbishPosition).hide();
				}
			}
		}
	};
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null)
		{
			convertView = new LeftScollDeleteRelativeLayout(mContext);
			View view = inflater.inflate(R.layout.item_message_box, null);
			((LeftScollDeleteRelativeLayout)convertView).addFrontView(view);
			holder = new Holder();
			holder.content_icon = (ImageView) convertView.findViewById(R.id.content_icon);
			holder.user_icon = (ImageView) convertView.findViewById(R.id.user_icon);
			holder.nick_name = (TextView) convertView.findViewById(R.id.nick_name);
			holder.msg_content = (TextView) convertView.findViewById(R.id.msg_content);
			holder.msg_collection = (TextView) convertView.findViewById(R.id.msg_collection);
			holder.msg_time = (TextView) convertView.findViewById(R.id.msg_time);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			((LeftScollDeleteRelativeLayout)convertView).setILeftScrollListener(leftScrollListener);
			convertView.setTag(holder);
		}else{
			holder = (Holder) convertView.getTag();
		}
		((LeftScollDeleteRelativeLayout)convertView).setPosition(position);
		((LeftScollDeleteRelativeLayout)convertView).hide();
		setDate(position, holder);
		viewsMap.put(position, ((LeftScollDeleteRelativeLayout)convertView));
		return convertView;
	}

	private void setDate(int position,Holder holder){
		MessageBoxObj data = datas.get(position);
		ImageLoaderHelper.disPlay(data.getAuthor_avatar(), holder.user_icon, DefaultUtil.getDefaultUserDrawable(mContext));
		holder.msg_content.setText(data.getBody());
		holder.nick_name.setText(data.getAuthor_nickname());
		holder.msg_time.setText(data.getUpdated_format());
		if(data.getType().equals(favourType))
		{
			holder.msg_collection.setVisibility(View.VISIBLE);
			holder.msg_content.setVisibility(View.GONE);
		}else{
			CommentInfo commentInfo = data.getComment_info();
			if(commentInfo != null)
			{
				holder.msg_content.setText(commentInfo.getBody());
			}
			holder.msg_collection.setVisibility(View.GONE);
			holder.msg_content.setVisibility(View.VISIBLE);
		}
		
		MessageBoxArticleInfo info = data.getArticle_info();
		if(info != null)
		{
			if(info.getImages().size() == 0)
			{
				holder.content_icon.setVisibility(View.GONE);
				holder.content.setText(info.getBody());
				holder.content.setVisibility(View.VISIBLE);
			}else{
				holder.content_icon.setVisibility(View.VISIBLE);
				ImageLoaderHelper.disPlay(info.getImages().get(0).getThumbPath(0), holder.content_icon, DefaultUtil.getDefaultImageDrawable(mContext));
				holder.content.setVisibility(View.GONE);
			}
			
		}
	}
}
