package com.android.gallery3d.loginguide;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraRoundedImageView;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.setting.SettingsActivity;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.fragment.CloudMainFragment;
import com.android.gallery3d.xcloudalbum.inter.IBackPressedListener;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

public class LoginGuideFragment extends Fragment implements OnClickListener,IBackPressedListener{

	private Button mLoginIuni;
	private Button mBindBaidu;
	private ContentResolver cr;
	private AuroraRoundedImageView myHeadView;
	private TextView nickView;
	private AccountHelper accountHelper;
	public boolean hasLogin = false;
	private static int INVISIT_PROGRESSBAR = 100;
	private ProgressBar  bindBaiduProgressBar;
	
	private BaiduAlbumUtils baiduAlbumUtils;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//TODO Auto-generated method stub
		 View view =  inflater.inflate(R.layout.aurora_cloud_login_guide, container, false);
		 
		 myHeadView = (AuroraRoundedImageView)view.findViewById(R.id.guide_head);
		 nickView = (TextView)view.findViewById(R.id.my_head_text);
		 
		 mLoginIuni = (Button)view.findViewById(R.id.bt_login_iuni);
		 mBindBaidu = (Button)view.findViewById(R.id.bt_bind_baidu);
		 mLoginIuni.setOnClickListener(this);
		 mBindBaidu.setOnClickListener(this);
		 
		 bindBaiduProgressBar = (ProgressBar)view.findViewById(R.id.bind_baidu_progressBar);
		 return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		cr = getActivity().getContentResolver();
		updataButton();
//		accountHelper = new AccountHelper(getActivity());
//		accountHelper.registerAccountContentResolver();
//		accountHelper.update();
		
		if (baiduAlbumUtils == null) {
			baiduAlbumUtils = BaiduAlbumUtils.getInstance(getActivity());
		}
		baiduAlbumUtils.setBaiduinterface((CloudActivity)getActivity());
	
	}

	private void updataButton(){
		Cursor cursor = cr.query(Uri.parse(ACCOUNT_CONTENT_URI), null, null,null, null);
		if (cursor != null && cursor.moveToFirst()) {
			hasLogin = cursor.getInt(cursor.getColumnIndex("hasLogin")) == 1 ? true : false;
		}
		if(hasLogin &&  !PrefUtil.getBoolean(getActivity(), "ACCOUNT_BIND", false) ){
			new LoadIcon().execute();
			mLoginIuni.setEnabled(false);
			mLoginIuni.setTextColor(getResources().getColor(R.color.aurora_cloud_login_text_disable));
			mBindBaidu.setEnabled(true);
			mBindBaidu.setTextColor(getResources().getColor(R.color.aurora_cloud_login_text_able));
		}else if( !hasLogin ){
			mLoginIuni.setEnabled(true);
			mBindBaidu.setEnabled(false);
			mLoginIuni.setTextColor(getResources().getColor(R.color.aurora_cloud_login_text_able));
			mBindBaidu.setTextColor(getResources().getColor(R.color.aurora_cloud_login_text_disable));
		}else if(  PrefUtil.getBoolean(getActivity(), "ACCOUNT_BIND", false) ){
			((CloudActivity)getActivity()).startCloudMainFragment();
		}
		cursor.close();
	}
	
	@Override
	public void onResume() {
		//TODO Auto-generated method stub
		super.onResume();
		updataButton();
		
	
	}

    
	@Override
	public void onClick(View v) {
		//TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.bt_login_iuni:
			Uri uri = Uri.parse("openaccount://com.aurora.account.login");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra("type", 1);
			intent.setData(uri);
			startActivityForResult(intent, CloudMainFragment.REQUEST_ACCOUNT_LOGIN);
			break;
		case R.id.bt_bind_baidu:
			if(!NetworkUtil.checkNetwork(getActivity())){
				Toast.makeText(getActivity(), R.string.aurora_album_network_fail, Toast.LENGTH_SHORT).show();
				break;
			}
			String token = PrefUtil.getString(getActivity(),
					AlbumPage.PREF_KEY_IUNI_ACCOUNT_TOKEN, "");
			baiduAlbumUtils.loginBaidu(token, true);
			bindBaiduProgressBar.setVisibility(View.VISIBLE);
			mHandler.sendEmptyMessageDelayed(INVISIT_PROGRESSBAR, 20000);
			break;
		}
	}
	
	private class LoadIcon extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			update();
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			//TODO Auto-generated method stub
			super.onPostExecute(result);
			
			Bitmap bitmap=BitmapFactory.decodeFile(path);
			Drawable drawable =new BitmapDrawable(bitmap);
			if(drawable==null || bitmap==null){
				drawable = getResources().getDrawable(R.drawable.head_icon);
			}
			myHeadView.setImageDrawable(drawable);
			nickView.setText(nick);
		}
		
	}
	private final String AUTHORITY = "com.aurora.account.accountprovider";
	private final String ACCOUNT_CONTENT_URI = "content://" + AUTHORITY + "/account_info";
	String nick;
	String path;
	public void update() {
		try {
			Cursor cursor = cr.query(Uri.parse(ACCOUNT_CONTENT_URI), null, null,null, null);
			if (cursor != null && cursor.moveToFirst()) {
				Boolean mLoginStatus = cursor.getInt(cursor
						.getColumnIndex("hasLogin")) == 1 ? true : false;
				if(mLoginStatus){
					nick = cursor.getString(cursor.getColumnIndex(SettingsActivity.ACCOUNT_INFO_NICK));
					path = cursor.getString(cursor.getColumnIndex(SettingsActivity.ACCOUNT_INFO_ICON_PATH));
				}
			}
			if (cursor != null){
				cursor.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		//TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
	}
	
	@Override
	public void onDestroy() {
		if(bindBaiduProgressBar!=null ){
			bindBaiduProgressBar.setVisibility(View.INVISIBLE);
		}
		super.onDestroy();
		if (accountHelper != null) {
			accountHelper.unregisterAccountContentResolver();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e("", "requestCode::" + requestCode + " resultCode::"
				+ resultCode + " data::" + data);
//		if (requestCode == CloudMainFragment.REQUEST_ACCOUNT_LOGIN
//				&& resultCode == Activity.RESULT_OK) {
//			getActivity()().setToLogin();//paul add
//			loginBaiduAlbum();
//			mHandler.sendEmptyMessageDelayed(CloudMainFragment.MSG_LOGIN_BAIDU_ALBUM, 20000);//wenyongzhe 2015.11.17 call loginBaiduAlbum()  again
//		}else if(requestCode == REQUEST_ACCOUNT_LOGIN) {
//			showLoadingImage(false);
//			ToastUtils.showTast(getCloudActivity(), R.string.aurora_album_login_cloud);
//		}
	}

	@Override
	public boolean onBack() {
		//TODO Auto-generated method stub
		return false;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == INVISIT_PROGRESSBAR){
				if(bindBaiduProgressBar != null){
					bindBaiduProgressBar.setVisibility(View.INVISIBLE);
				}
			}
		}

	};
		
}
