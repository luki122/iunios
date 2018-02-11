package com.aurora.iunivoice.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.aurora.datauiapi.data.bean.SystemMsg;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.MineNewsActivity;
import com.aurora.iunivoice.utils.ImageLoadUtil;
import com.aurora.iunivoice.utils.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MineNewsAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<SystemMsg> datas;
	private HashMap<Integer, Boolean> isLoadMoreMap = new HashMap<Integer, Boolean>();
	public MineNewsAdapter(Context context,ArrayList<SystemMsg> datas){
		this.context = context;
		this.datas= datas;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return datas.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	public boolean isLoadMore(int position){
		if(!isLoadMoreMap.containsKey(position))
		{
			isLoadMoreMap.put(position, false);
			return false;
		}
		return isLoadMoreMap.get(position);
	}
	
	public boolean isUnRead(int position){
		return !datas.get(position).getStatus().equals("1");
	}
	
	@Override
	public View getView(int arg0, View view, ViewGroup arg2) {
		Holder holder = null;
		if(view == null)
		{
			view = LayoutInflater.from(context).inflate(R.layout.item_mine_news, null);
			holder = new Holder();
			holder.user_icon = (ImageView) view.findViewById(R.id.user_icon);
			holder.user_name = (TextView) view.findViewById(R.id.user_name);
			holder.notification_title = (TextView)view.findViewById(R.id.notification_title);
			holder.notification_content = (TextView)view.findViewById(R.id.notification_content);
			holder.load_more = (TextView)view.findViewById(R.id.load_more);
			holder.publish_data = (TextView) view.findViewById(R.id.publish_data);
			view.setTag(holder);
		}else{
			holder = (Holder) view.getTag();
		}
		LinearLayout lview = (LinearLayout)view;
		if(arg0 == 0)
		{
			lview.setPadding(0, 0, 0, 0);
		}else{
			lview.setPadding(0,(int)context.getResources().getDimension(R.dimen.homepage_item_divider_height),  0, 0);
		}
		holder.setData(arg0, datas.get(arg0));
		holder.show();
		
		return view;
	}

	private int unRead = Color.parseColor("#fe6b6b");
	private int alreadyRead = Color.parseColor("#262626");
	static final int MAX_WORD = 50;
	class Holder {
		ImageView user_icon;
		TextView user_name, 
		              publish_data, 
		              notification_title,
				notification_content, 
				load_more;
		
		boolean loadMore = false;
		boolean hasLoadMore = true;
		int position;
		
		String allContent;//全部内容
		String summaryContent;//当字数大于50字的时候只显示前面的内容
		
		static final String POINT = "...";
		
		SystemMsg data = null;
		
		OnClickListener loadMoreClick = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadMore = !loadMore;
				if(loadMore)
				{
					load_more.setText(R.string.mine_shouqi);
					notification_content.setText(allContent);
					if(!isRead())
					{
						((MineNewsActivity)context).systemMsgDetail(data.getId());
						((MineNewsActivity)context).fillterAllNews();
					}
					data.setStatus("1");
				}else{
					load_more.setText(R.string.mine_load_more);
					notification_content.setText(summaryContent);
				}
				isLoadMoreMap.put(position, loadMore);
				if(isRead())
				{
					notification_title.setTextColor(alreadyRead);
				}else{
					notification_title.setTextColor(unRead);
				}
			}
		};
		
		void setData(int position,SystemMsg data){
			this.position = position;
			this.data = data;
			if(!isLoadMoreMap.containsKey(position))
			{
				isLoadMoreMap.put(position, false);
			}
			loadMore = isLoadMoreMap.get(position);
			String content = data.getMessage();
			if(content.length() <= MAX_WORD)
			{
				summaryContent = content;
				allContent = content;
				hasLoadMore = false;
			}else{
				summaryContent = content.substring(0, MAX_WORD-3)+POINT;
				allContent = content;
				hasLoadMore = true;
			}
		}
		
		void show(){
			if(hasLoadMore)
			{
				load_more.setVisibility(View.VISIBLE);
				load_more.setOnClickListener(loadMoreClick);
			}else{
				load_more.setVisibility(View.GONE);
			}
			
			if(loadMore)
			{
				load_more.setText(R.string.mine_shouqi);
				notification_content.setText(allContent);
			}else{
				load_more.setText(R.string.mine_load_more);
				notification_content.setText(summaryContent);
			}
			
			if(isRead())
			{
				notification_title.setTextColor(alreadyRead);
			}else{
				notification_title.setTextColor(unRead);
			}
			publish_data.setText(data.getDateline());
			ImageLoader.getInstance().displayImage(data.getAvatar(), user_icon, ImageLoadUtil.circleUserIconOptions);
			notification_title.setText(data.getSubject());
			user_name.setText(data.getAuthor());
		}
		
		boolean isRead(){
			return data.getStatus().equals("1");
		}
		
	}

}
