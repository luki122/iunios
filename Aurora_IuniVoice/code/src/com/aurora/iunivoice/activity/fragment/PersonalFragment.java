package com.aurora.iunivoice.activity.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.BaseUserInfo;
import com.aurora.datauiapi.data.bean.UserInfoDetailInfo;
import com.aurora.datauiapi.data.bean.UserInfoHolder;
import com.aurora.datauiapi.data.bean.UserInfoSpace;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.AccountManageActivity;
import com.aurora.iunivoice.activity.MainActivity;
import com.aurora.iunivoice.activity.MineInfoActivity;
import com.aurora.iunivoice.activity.MineNewsActivity;
import com.aurora.iunivoice.activity.MinePublishActivity;
import com.aurora.iunivoice.activity.MineScoreActivity;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.AccountHelper.IAccountChangeListener;
import com.aurora.iunivoice.utils.AccountUtil;
import com.aurora.iunivoice.utils.ImageLoadUtil;
import com.aurora.iunivoice.utils.SystemUtils;
import com.aurora.iunivoice.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PersonalFragment extends BaseViewPagerFragment implements OnClickListener {

	private static final String TAG = "PersonalFragment";
	private View mRootView;
	
	private Class[] toPageName =  {MineInfoActivity.class,MineScoreActivity.class,MinePublishActivity.class,MineNewsActivity.class,AccountManageActivity.class};
	private ImageView user_head_icon;//用户头像
	private TextView user_name,//用户名
	                           user_group_name;//用户组名
	
	private IuniVoiceManager iuniVoiceManager;
	private UserInfoSpace userInfoSpace;
	private UserInfoDetailInfo detailInfo;
	private LinearLayout ll_login_layout;
	private RelativeLayout unlogin_layout;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AccountHelper.getInstance(getActivity()).removeIAccountChangeListener(accountChangeListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_personal, container, false);
		AccountHelper.getInstance(getActivity()).addIAccountChangeListener(accountChangeListener);
		layoutChange();
		return mRootView;
	}

	@Override
	public void handleMessage(Message msg) {

	}
	
	private IAccountChangeListener accountChangeListener = new IAccountChangeListener() {
		
		@Override
		public void unLogin() {
			layoutChange();
		}
		
		@Override
		public void changeAccount() {
			layoutChange();
		}

		@Override
		public void loginSuccess() {
			// TODO Auto-generated method stub
			layoutChange();
		}

		@Override
		public void userInfoChange() {
			// TODO Auto-generated method stub
			getNetData();
		}
	};

	public void layoutChange(){
		if(AccountHelper.getInstance(getActivity()).mLoginStatus)
		{
			if(checkIsNull())
			{
				unlogin_layout.setVisibility(View.GONE);
				ll_login_layout.setVisibility(View.VISIBLE);
				user_group_name.setVisibility(View.VISIBLE);
				user_head_icon.setImageResource(R.drawable.default_user_icon);
				getNetData();
			}
		}else{
			if(checkIsNull())
			{
			user_head_icon.setImageResource(R.drawable.default_user_icon);
			user_name.setText(R.string.mine_unlogin);
			unlogin_layout.setVisibility(View.VISIBLE);
			ll_login_layout.setVisibility(View.GONE);
			user_group_name.setText("");
			}
		}
	}
	
	private boolean checkIsNull(){
		return unlogin_layout!=null && ll_login_layout!= null && user_group_name != null && user_head_icon != null && user_name!= null;
	}
	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	@Override
	public void setupViews() {
		iuniVoiceManager = new IuniVoiceManager(this);
		mRootView.findViewById(R.id.ll_info).setOnClickListener(this);
		mRootView.findViewById(R.id.ll_score).setOnClickListener(this);
		mRootView.findViewById(R.id.ll_publish).setOnClickListener(this);
		mRootView.findViewById(R.id.ll_news).setOnClickListener(this);
		ll_login_layout = (LinearLayout) mRootView.findViewById(R.id.ll_login_layout);
		unlogin_layout = (RelativeLayout) mRootView.findViewById(R.id.unlogin_layout);
		unlogin_layout.setOnClickListener(this);
		user_name = (TextView) mRootView.findViewById(R.id.user_name);
		user_group_name = (TextView) mRootView.findViewById(R.id.user_group_name);
		user_head_icon = (ImageView) mRootView.findViewById(R.id.user_head_icon);
		user_head_icon.setOnClickListener(this);
		IntentFilter filter = new IntentFilter(CONNECTIVITY_CHANGE_ACTION);
		getActivity().registerReceiver(reciver, filter);
	}
	
	private  BroadcastReceiver reciver =  new  BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (SystemUtils.hasNetwork()) {
				getNetData();
			}
		}
	};
	
	private void getNetData(){
		iuniVoiceManager.getUserInfo(new DataResponse<UserInfoHolder>(){
			@Override
			public void run() {
				super.run();
				if(value != null)
				{
					detailInfo = value.getData();
					if(detailInfo == null)
						return;
					userInfoSpace = detailInfo.getSpace();
					if(userInfoSpace==null){
						return;
					}
					user_name.setText(userInfoSpace.getUsername());
					user_group_name.setVisibility(View.VISIBLE);
					user_group_name.setText(userInfoSpace.getGroup().getGrouptitle());
					ImageLoader.getInstance().displayImage(userInfoSpace.getAvatar(), user_head_icon, ImageLoadUtil.circleUserIconOptions);
				}
			}
		});
		
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		getActivity().unregisterReceiver(reciver);
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.ll_info://我的资料
			toPage(0);
			break;
		case R.id.ll_score://我的积分
			toPage(1);
			break;
		case R.id.ll_publish://我的帖子
			toPage(2);
			break;
		case R.id.ll_news://我的消息
			toPage(3);
			break;
		case R.id.unlogin_layout://随便看看
			((MainActivity)getActivity()).showMainFragment();
			break;
		case R.id.user_head_icon:
			if(AccountHelper.getInstance(getActivity()).mLoginStatus)
			{
				toPage(4);
			}else{
				AccountUtil.getInstance().startLogin(getActivity());
			}
			break;
		default:
			break;
		}
	}
	
	public static final String USER_EXTCREDITS_KEY = "user_extcredits_key";
	public static final String USER_BASE_INFO_KEY = "user_base_info";
	
	private void toPage(int i){
		//
		if(detailInfo==null||detailInfo.getSpace()==null){
			ToastUtil.shortToast(R.string.mine_error);
			return;
		}
		
		Intent intent = new Intent(getActivity(), toPageName[i]);
		if(i == 1)
		{
			intent.putParcelableArrayListExtra(USER_EXTCREDITS_KEY, detailInfo.getExtcredits());
		}else if(i == 0)
		{
			intent.putExtra(USER_BASE_INFO_KEY, new BaseUserInfo(detailInfo));
			intent.putParcelableArrayListExtra(USER_EXTCREDITS_KEY, detailInfo.getExtcredits());
		}else if( i == 4)
		{
			intent.putExtra(USER_BASE_INFO_KEY, new BaseUserInfo(detailInfo));
		}
		getActivity().startActivity(intent);
	}
	
	@Override
	protected void loadData() {
		layoutChange();
	}

}