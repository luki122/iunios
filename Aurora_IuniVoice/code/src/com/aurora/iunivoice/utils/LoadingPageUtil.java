package com.aurora.iunivoice.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aurora.iunivoice.R;

public class LoadingPageUtil implements OnClickListener {
	
	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	
	private static final int STATUS_NO_NETWORK = 1;
	private static final int STATUS_NETWORK_ERROR = 2;
	
	private boolean isReg = false;//是否开启了广播监听

	private Context context;

	private LinearLayout ll_load_page;
	private ProgressBar iv_loading;
	private LinearLayout ll_error;
	private ImageView iv_icon;
	private TextView tv_text;
	private ImageView btn;
	
	private int status;

	private OnShowListener onShowListener;
	private OnHideListener onHideListener;
	private OnRetryListener onRetryListener;
	
	private NetWorkReceiver netWorkReceiver;

	public void init(Context context, View view) {
		this.context = context;
		ll_load_page = (LinearLayout) view.findViewById(R.id.ll_load_page);
		iv_loading = (ProgressBar) view.findViewById(R.id.iv_loading);
		ll_error = (LinearLayout) view.findViewById(R.id.ll_error);
		iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
		tv_text = (TextView) view.findViewById(R.id.tv_text);
		btn = (ImageView) view.findViewById(R.id.btn);
		btn.setOnClickListener(this);
	}
	
	public boolean isShowing() {
		if (ll_load_page.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}
	
	public void showLoadPage() {
		ll_load_page.setVisibility(View.VISIBLE);
		if (onShowListener != null) {
			onShowListener.onShow();
		}
	}

	public void hideLoadPage() {
		ll_load_page.setVisibility(View.GONE);
		if (onHideListener != null) {
			onHideListener.onHide();
		}
	}

	public void showLoading() {
		unRegister();
		
		ll_error.setVisibility(View.GONE);
		iv_loading.setVisibility(View.VISIBLE);
		//iv_loading.clearAnimation();
		//iv_loading.startAnimation(createRotateAnimation());
	}

	public void showNoNetWork() {
		register();
		
		status = STATUS_NO_NETWORK;
		ll_error.setVisibility(View.VISIBLE);
		//iv_loading.clearAnimation();
		iv_loading.setVisibility(View.GONE);

		iv_icon.setImageResource(R.drawable.ic_launcher);
		tv_text.setText(context.getString(R.string.page_loading_network_error));
		//btn.setText(context.getString(R.string.page_loading_set_network));
	}

	public void showNetworkError() {
		unRegister();
		
		status = STATUS_NETWORK_ERROR;
		ll_error.setVisibility(View.VISIBLE);
		//iv_loading.clearAnimation();
		iv_loading.setVisibility(View.GONE);

		iv_icon.setImageResource(R.drawable.ic_launcher);
		tv_text.setText(context.getString(R.string.page_loading_network_error));
		//btn.setText(context.getString(R.string.page_loading_retry));
	}
	/**
	 * 注销广播
	 */
	public void clearRegister(){
		if(isReg){			
			unRegister();
		}
	}

	public void setOnShowListener(OnShowListener onShowListener) {
		this.onShowListener = onShowListener;
	}

	public void setOnHideListener(OnHideListener onHideListener) {
		this.onHideListener = onHideListener;
	}

	public void setOnRetryListener(OnRetryListener onRetryListener) {
		this.onRetryListener = onRetryListener;
	}

	public interface OnShowListener {
		public void onShow();
	}

	public interface OnHideListener {
		public void onHide();
	}

	public interface OnRetryListener {
		public void retry();
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn:
			if (status == STATUS_NO_NETWORK) {
				// open setting
//				Intent intent = new Intent(
//						android.provider.Settings.ACTION_SETTINGS);
//				context.startActivity(intent);
				ToastUtil.shortToast(R.string.page_loading_no_network);
			} else if (status == STATUS_NETWORK_ERROR) {
				showLoading();
				if (onRetryListener != null) {
					onRetryListener.retry();
				}
			}
			break;
		}
	}
	
//	/**
//	* @Title: createRotateAnimation
//	* @Description: 创建旋转动画
//	* @param @return
//	* @return RotateAnimation
//	* @throws
//	 */
//	private RotateAnimation createRotateAnimation() {
//		RotateAnimation animation = null;
//		animation = new RotateAnimation(0, 3600, Animation.RELATIVE_TO_SELF,
//				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//		animation.setInterpolator(new LinearInterpolator());
//		animation.setFillAfter(true);
//		animation.setDuration(10000);
//		animation.setStartOffset(0);
//		animation.setRepeatCount(1000);
//		return animation;
//	}
//	
	private void register() {
		 IntentFilter filter = new IntentFilter();
		 filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		 if (netWorkReceiver == null) {
			 isReg = true;
			 netWorkReceiver = new NetWorkReceiver();
			 context.registerReceiver(netWorkReceiver, filter);
		 }
	}
	
	private void unRegister() {
		if (netWorkReceiver != null&&isReg) {
			isReg = false;
			context.unregisterReceiver(netWorkReceiver);
			netWorkReceiver = null;
		}
	}
	
	private class NetWorkReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (SystemUtils.hasNetwork()) {
				showLoading();
				if (onRetryListener != null) {
					onRetryListener.retry();
				}
			}
		}
	}

}
