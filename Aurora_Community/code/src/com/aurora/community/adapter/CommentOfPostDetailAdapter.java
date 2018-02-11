package com.aurora.community.adapter;

import java.util.List;

import com.aurora.datauiapi.data.bean.CommentInfo;
import com.aurora.community.CommunityApp;
import com.aurora.community.R;
import com.aurora.community.activity.MainActivity;
import com.aurora.community.activity.MessageBoxActivity;
import com.aurora.community.activity.OtherUserCenterActivity;
import com.aurora.community.activity.PostDetailActivity;
import com.aurora.community.activity.twitter.TwitterNoteActivity;
import com.aurora.community.adapter.NewsStickyListAdapter.TextViewHolder;
import com.aurora.community.utils.AccountHelper;
import com.aurora.community.utils.DefaultUtil;
import com.aurora.community.utils.ImageLoaderHelper;
import com.aurora.community.utils.TimeUtils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentOfPostDetailAdapter extends BaseAdapter {

	private List<CommentInfo> datas;
	private LayoutInflater mInflater;
	private Context mContext;
	private int replyNameColor,
	              commentContentColor;
	public CommentOfPostDetailAdapter(List<CommentInfo> datas,
			Context mContext) {
		super();
		this.datas = datas;
		this.mContext = mContext;
		replyNameColor=mContext.getResources().getColor(R.color.postdetail_replyname_color);
		commentContentColor=mContext.getResources().getColor(R.color.gloab_title_color);
		mInflater = LayoutInflater.from(mContext);
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
		          line_bottom;
	    TextView comment_time,
	             screen_name,
	             comment_content;
	}

	private void setHolder(View converView,Holder holder){
		holder.user_icon = (ImageView)converView.findViewById(R.id.user_icon);
		holder.comment_content = (TextView)converView.findViewById(R.id.comment_content);
		holder.comment_time = (TextView)converView.findViewById(R.id.comment_time);
		holder.screen_name = (TextView)converView.findViewById(R.id.screen_name);
		holder.line_bottom = (ImageView) converView.findViewById(R.id.line_bottom);
		holder.user_icon.setOnClickListener(userIconClickListener);
	}
	
	private OnClickListener userIconClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			int position = (Integer) arg0.getTag();
			String userId = datas.get(position).getUid();
			if(userId.equals(AccountHelper.user_id))
			{
				/*Intent intent =new Intent(mContext,MainActivity.class);
				intent.putExtra(MainActivity.COME_FROM_KEY, PostDetailActivity.class.getName());
				mContext.startActivity(intent);*/
				if(((PostDetailActivity)mContext).getComeFrom() != null && ((PostDetailActivity)mContext).getComeFrom().equals(MessageBoxActivity.class.getName()))
				{
					((PostDetailActivity)mContext).setResult(PostDetailActivity.RESULT_BACK);
					((PostDetailActivity)mContext).finish();
				}else{
					((PostDetailActivity)mContext).finish();
					CommunityApp.getInstance().getinstance().setDisView(PostDetailActivity.class.getName());
				}
			}else{
				Intent intent =new Intent(mContext,OtherUserCenterActivity.class);
				intent.putExtra(OtherUserCenterActivity.USER_ID_KEY, userId);
				intent.putExtra(OtherUserCenterActivity.USER_NICK_KEY, datas.get(position).getUid_nick());
				mContext.startActivity(intent);
			}
		}
	};
	
	private void setDate(int position,Holder holder){
		CommentInfo data = datas.get(position);
		ImageLoaderHelper.disPlay(data.getUid_avatar(), holder.user_icon, DefaultUtil.getDefaultUserDrawable(mContext));
		holder.comment_time.setText(data.getCreated_format());
		holder.user_icon.setTag(position);
		setCommentText(data, holder.comment_content);
		holder.screen_name.setText(data.getUid_nick());
	}
	
	private void setCommentText(CommentInfo info,TextView commentContent){
		if(TextUtils.isEmpty(info.getReplyuid_nick()))
		{
			commentContent.setText(info.getBody());
		}else{
			String replyName = mContext.getString(R.string.reply_user_nick, info.getReplyuid_nick());
			String content = info.getBody();
			StringBuffer buffer = new StringBuffer();
			buffer.append(replyName);
			buffer.append(content);
			SpannableStringBuilder builder = new SpannableStringBuilder(buffer.toString());
			builder.setSpan(new ForegroundColorSpan(replyNameColor), 0, replyName.length()-1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			builder.setSpan(new ForegroundColorSpan(commentContentColor), replyName.length(), buffer.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			commentContent.setText(builder);
		}
		
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if(convertView == null)
		{
			holder = new Holder();
			convertView = mInflater.inflate(R.layout.item_comment_of_postdetail,null);
			setHolder(convertView, holder);
			convertView.setTag(holder);
		}else{
			holder = (Holder) convertView.getTag();
		}
		setDate(position, holder);
		if(position == getCount()-1)
		{
			holder.line_bottom.setVisibility(View.VISIBLE);
		}else{
			holder.line_bottom.setVisibility(View.GONE);
		}
		return convertView;
	}

}
