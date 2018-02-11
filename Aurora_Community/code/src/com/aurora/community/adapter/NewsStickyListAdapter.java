package com.aurora.community.adapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.activity.OtherUserCenterActivity;
import com.aurora.community.activity.PostDetailActivity;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.BitmapUtil;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.StringUtils;
import com.aurora.community.utils.SystemUtils;
import com.aurora.community.utils.TimeUtils;
import com.aurora.community.view.CircleImageView;
import com.aurora.datauiapi.data.bean.ArticleDataInfo;
import com.aurora.datauiapi.data.bean.ImageInfo;
import com.aurora.datauiapi.data.bean.NewsInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class NewsStickyListAdapter extends BaseAdapter implements
		StickyListHeadersAdapter, SectionIndexer {

	private static final int MAX_NUM_OF_PIC = 3;
	private static final int MAX_VIEW_COUNT = 3;
	
	private ArrayList<ArticleDataInfo> list;

	private Context context;

	private LayoutInflater inflater;

	private  ViewHolder holder;
	private TextViewHolder textHolder;
	
	public NewsStickyListAdapter(Context context, ArrayList<ArticleDataInfo> list) {
		this.list = list;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return list.get(position).getType();
	}
	
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return MAX_VIEW_COUNT;
	}
	
    private void toUserCenter(int position){
    	ArticleDataInfo info = list.get(position);
    	if(info == null)  
    	{
    		return;
    	}
    	String userId = info.getUid();
    	String nick = info.getUid_nick();
		if(userId.equals(AccountHelper.user_id))
		{
			/*Intent intent =new Intent(mContext,MainActivity.class);
			intent.putExtra(MainActivity.COME_FROM_KEY, PostDetailActivity.class.getName());
			mContext.startActivity(intent);*/
			CommunityApp.getInstance().getinstance().setDisView(PostDetailActivity.class.getName());
		}else{
			Intent intent =new Intent(context,OtherUserCenterActivity.class);
			intent.putExtra(OtherUserCenterActivity.USER_ID_KEY, userId);
			intent.putExtra(OtherUserCenterActivity.USER_NICK_KEY, nick);
			context.startActivity(intent);
		}
		
	}
	
    private OnClickListener toUserCenterListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int position = (Integer) v.getTag();
			toUserCenter(position);
		}
	};
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ArticleDataInfo info = list.get(position);
		
		if(convertView == null){
			switch (info.getType()) {
			case ArticleDataInfo.NEWS_CONTAINS_PIC_TYPE:
				holder = new ViewHolder();
				convertView = inflater.inflate(
						R.layout.news_fragment_stickylist_adapter_item_muti, parent,
						false);
				//TOP
				holder.iv_user_head = (CircleImageView)convertView.findViewById(R.id.iv_user_head);
				holder.tv_user_nick = (TextView)convertView.findViewById(R.id.tv_user_nick);
				holder.iv_user_head.setTag(position);
				holder.tv_user_nick.setTag(position);
				holder.iv_user_head.setOnClickListener(toUserCenterListener);
				holder.tv_user_nick.setOnClickListener(toUserCenterListener);
				//CONTENT
				holder.ll_content = (LinearLayout)convertView.findViewById(R.id.ll_images_container);
				holder.tv_content = (TextView)convertView.findViewById(R.id.tv_news_content);
				
				//BOTTOM
				holder.tv_twitterDate = (TextView)convertView.findViewById(R.id.tv_twitter_time);
				holder.tv_twitter_comment_num =  (TextView)convertView.findViewById(R.id.tv_twitter_comment_num);
				holder.tv_twitter_collect_num = (TextView)convertView.findViewById(R.id.tv_twitter_collect_num);

				convertView.setTag(holder);
				break;

			case ArticleDataInfo.NEWS_WITHOUT_PIC_TYPE:
				textHolder = new TextViewHolder();
				convertView = inflater.inflate(
						R.layout.news_fragment_stickylist_adapter_item_text, parent,
						false);
				//TOP
				textHolder.iv_user_head = (ImageView)convertView.findViewById(R.id.iv_user_head);
				textHolder.tv_user_nick = (TextView)convertView.findViewById(R.id.tv_user_nick);
				//CONTENT
				textHolder.tv_twitter_content = (TextView)convertView.findViewById(R.id.tv_twitter_content);
				//BOTTOM
				textHolder.tv_twitterDate = (TextView)convertView.findViewById(R.id.tv_twitter_time);
				textHolder.tv_twitter_comment_num =  (TextView)convertView.findViewById(R.id.tv_twitter_comment_num);
				textHolder.tv_twitter_collect_num = (TextView)convertView.findViewById(R.id.tv_twitter_collect_num);
				
				convertView.setTag(textHolder);
				break;
			}
		}else{
			switch (info.getType()) {
			case ArticleDataInfo.NEWS_CONTAINS_PIC_TYPE:
				holder = (ViewHolder) convertView.getTag();
				holder.iv_user_head.setTag(position);
				holder.tv_user_nick.setTag(position);
				break;

			case ArticleDataInfo.NEWS_WITHOUT_PIC_TYPE:
				textHolder = (TextViewHolder)convertView.getTag();
				break;
			}
		}
		
		switch (info.getType()) {
		case ArticleDataInfo.NEWS_CONTAINS_PIC_TYPE:
        
			ImageLoaderHelper.disPlay(info.getUid_avatar(), holder.iv_user_head, DefaultUtil.getDefaultUserDrawable(context));
		
			if(info.getSummary().equals("")){
				holder.tv_content.setText(info.getBody());
			}else{
				holder.tv_content.setText(info.getSummary());
			}
			
			holder.tv_twitterDate.setText(TimeUtils.getDistanceTime(info.getCreated()));
			holder.tv_user_nick.setText(info.getUid_nick());
			holder.tv_twitter_comment_num.setText(info.getReplies());
			holder.tv_twitter_collect_num.setText(info.getFavorites());
			//TODO start to calculate the picture's width and height and position
			show(holder.ll_content,position);
			break;

		case 	ArticleDataInfo.NEWS_WITHOUT_PIC_TYPE:
			ImageLoaderHelper.disPlay(info.getUid_avatar(), textHolder.iv_user_head, DefaultUtil.getDefaultUserDrawable(context));
			textHolder.tv_twitter_content.setText(info.getSummary());
			textHolder.tv_twitterDate.setText(TimeUtils.getDistanceTime(info.getCreated()));
			textHolder.tv_user_nick.setText(info.getUid_nick());
			textHolder.tv_twitter_comment_num.setText(info.getReplies());
			textHolder.tv_twitter_collect_num.setText(info.getFavorites());
			break;
		}
		
		
		return convertView;
	}

	@Override
	public int getPositionForSection(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSectionForPosition(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = inflater.inflate(
					R.layout.news_fragment_stickylist_adapter_header, parent,
					false);
			holder.tv_month = (TextView) convertView
					.findViewById(R.id.header_tv_month);
			holder.tv_date = (TextView) convertView
					.findViewById(R.id.header_tv_date);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}
		ArticleDataInfo item = list.get(position);
		Date d = getDateByString(item.getCreated());
   
		holder.tv_month.setText(StringUtils.getMonth(d.toString()));
		holder.tv_date.setText(String.valueOf(d.getDate()));

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		// TODO Auto-generated method stub
		int id = 0;
		ArticleDataInfo item = list.get(position);
		Date d = getDateByString(item.getCreated());
		id = (d.getMonth() + 1) + d.getDate();
		return id;
	}

	/** getting upload picture url list size */
	private int getSizeOfNewsInfoUrl(int position) {
		int size = 0;
		ArticleDataInfo info = this.list.get(position);
		if (info != null) {
			size = info.getImages().size();
		}
		return size;
	}

	class HeaderViewHolder {
		TextView tv_month;
		TextView tv_date;
	}

	class ViewHolder {
		CircleImageView iv_user_head;
		TextView tv_user_nick;
		
		LinearLayout ll_content;
		TextView tv_content;
		
		TextView tv_twitterDate;
		TextView tv_twitter_comment_num;
		TextView tv_twitter_collect_num;
	}
	
	class TextViewHolder{
		ImageView iv_user_head;
		TextView tv_user_nick;
		
		TextView tv_twitter_content;
		
		TextView tv_twitterDate;
		TextView tv_twitter_comment_num;
		TextView tv_twitter_collect_num;
	}

	private Date getDateByString(String str) {
		Date d = new Date();
		try {
			d = TimeUtils.getDateByString(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}

	private void show(LinearLayout ll,int position){		
		int width =0;
		int height = 0;
		int size = getSizeOfNewsInfoUrl(position);
		
		ArticleDataInfo info = list.get(position);
		ArrayList<ImageInfo> images = info.getImages();
		switch (images.size()) {
		case 1:
			ll.getChildAt(0).setVisibility(View.VISIBLE);
			ll.getChildAt(1).setVisibility(View.GONE);
			ll.getChildAt(2).setVisibility(View.GONE);
			ImageLoaderHelper.disPlay(images.get(0).getThumbPath(), (ImageView)ll.getChildAt(0), DefaultUtil.getDefaultImageDrawable(context));
			break;
		case 2:
			ll.getChildAt(0).setVisibility(View.VISIBLE);
			ll.getChildAt(1).setVisibility(View.VISIBLE);
			ll.getChildAt(2).setVisibility(View.GONE);
			ImageLoaderHelper.disPlay(images.get(0).getThumbPath(1), (ImageView)ll.getChildAt(0), DefaultUtil.getDefaultImageDrawable(context));
			ImageLoaderHelper.disPlay(images.get(1).getThumbPath(1), (ImageView)ll.getChildAt(1), DefaultUtil.getDefaultImageDrawable(context));
			break;
		default:
			ll.getChildAt(0).setVisibility(View.VISIBLE);
			ll.getChildAt(1).setVisibility(View.VISIBLE);
			ll.getChildAt(2).setVisibility(View.VISIBLE);
			ImageLoaderHelper.disPlay(images.get(0).getThumbPath(0), (ImageView)ll.getChildAt(0), DefaultUtil.getDefaultImageDrawable(context));
			ImageLoaderHelper.disPlay(images.get(1).getThumbPath(0), (ImageView)ll.getChildAt(1), DefaultUtil.getDefaultImageDrawable(context));
			ImageLoaderHelper.disPlay(images.get(2).getThumbPath(0), (ImageView)ll.getChildAt(2), DefaultUtil.getDefaultImageDrawable(context));
			break;
		}
		
//		if(size>=MAX_NUM_OF_PIC){
//			
//			width = SystemUtils.getIntegerValueFromResourcesDimens(context, R.dimen.stickylist_item_width_for_column_more);
//			height = SystemUtils.getIntegerValueFromResourcesDimens(context, R.dimen.stickylist_item_height_for_column_more);
//			int gap = SystemUtils.getIntegerValueFromResourcesDimens(context, R.dimen.stickylist_item_right_margin_for_column_more);
//			for(int i=0;i<MAX_NUM_OF_PIC;i++){
//				String url = info.getImages().get(i).getThumbPath(3);
//				ImageView img = new ImageView(context);
//				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
//				if(i==(MAX_NUM_OF_PIC-1)){
//					layoutParams.rightMargin = 0;
//				}else
//					layoutParams.rightMargin = gap;
//				
//				img.setLayoutParams(layoutParams);
//				
//				img.setScaleType(ScaleType.CENTER_CROP);
//				
//				ImageLoaderHelper.disPlay(url, img, DefaultUtil.getDefaultImageDrawable(context));
//				ll.addView(img);
//			}
//		}else{
//			if(info.getImages().size()>0){
//				width = SystemUtils.getIntegerValueFromResourcesDimens(context, R.dimen.stickylist_item_width_for_column_one);
//				height = SystemUtils.getIntegerValueFromResourcesDimens(context, R.dimen.stickylist_item_height_for_column_one);
//				Log.e("linp", "~~~info.getImages()="+info.getImages().size());
//				String url = info.getImages().get(0).getThumbPath();
//				
//				ImageView img = new ImageView(context);
//				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
//				img.setLayoutParams(layoutParams);
//				
//				img.setScaleType(ScaleType.CENTER_CROP);
//				
//				ImageLoaderHelper.disPlay(url, img, DefaultUtil.getDefaultImageDrawable(context));
//				ll.addView(img);
//			}
//
//		}
	}
	
}
