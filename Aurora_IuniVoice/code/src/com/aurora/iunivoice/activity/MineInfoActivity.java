package com.aurora.iunivoice.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.BaseUserInfo;
import com.aurora.datauiapi.data.bean.UserInfoExtcredits;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.fragment.PersonalFragment;
import com.aurora.iunivoice.utils.ImageLoadUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MineInfoActivity extends BaseActivity {
	private TextView user_name, user_id, user_introduce;
	private ImageView user_head_icon;
	private TextView friend_counts, record_counts, diary_counts,
			back_publish_counts, theme_counts, share_counts, group_name,
			already_user_space;
	private ArrayList<UserInfoExtcredits> extcredits;
	private BaseUserInfo baseUserInfo;
	private LinearLayout extcredits_container;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mine_info);
		extcredits = getIntent().getParcelableArrayListExtra(
				PersonalFragment.USER_EXTCREDITS_KEY);
		baseUserInfo = getIntent().getParcelableExtra(
				PersonalFragment.USER_BASE_INFO_KEY);
		setupViews();
		setData();
	}

	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		setTitleRes(R.string.mine_info_text);
		user_name = (TextView) findViewById(R.id.user_name);
		user_id = (TextView) findViewById(R.id.user_id);
		user_introduce = (TextView) findViewById(R.id.user_introduce);
		user_head_icon = (ImageView) findViewById(R.id.user_head_icon);

		friend_counts = (TextView) findViewById(R.id.friend_counts);
		record_counts = (TextView) findViewById(R.id.record_counts);
		diary_counts = (TextView) findViewById(R.id.diary_counts);
		back_publish_counts = (TextView) findViewById(R.id.back_publish_counts);
		theme_counts = (TextView) findViewById(R.id.theme_counts);
		share_counts = (TextView) findViewById(R.id.share_counts);
		group_name = (TextView) findViewById(R.id.group_name);
		extcredits_container = (LinearLayout) findViewById(R.id.extcredits_container);
	}

	private void setData() {
		user_name.setText(baseUserInfo.getUserName());
		user_id.setText("UID:" + baseUserInfo.getUserID());
		user_introduce.setText(baseUserInfo.getUserRemarks());
		ImageLoader.getInstance().displayImage(baseUserInfo.getUserIcon(),
				user_head_icon, ImageLoadUtil.circleUserIconOptions);

		friend_counts.setText(baseUserInfo.getFriendCount());
		record_counts.setText(baseUserInfo.getRecoder());
		diary_counts.setText(baseUserInfo.getDiary());
		back_publish_counts.setText(baseUserInfo.getBackPublish());
		theme_counts.setText(baseUserInfo.getTheme());
		share_counts.setText(baseUserInfo.getShare());
		group_name.setText(baseUserInfo.getGroupName());
		for (int i = 0; i < extcredits.size(); i++) {
			View view = LayoutInflater.from(this).inflate(
					R.layout.childview_user_info, null);

			((TextView) view.findViewById(R.id.title_1)).setText(extcredits
					.get(i).getTitle());
			((TextView) view.findViewById(R.id.value_1)).setText(extcredits
					.get(i).getValue() + extcredits.get(i).getUnit());
			extcredits_container.addView(view, new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}

}
