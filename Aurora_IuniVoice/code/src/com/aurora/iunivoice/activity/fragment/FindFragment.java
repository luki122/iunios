package com.aurora.iunivoice.activity.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.feedback.UserFBActivity;
import com.aurora.iunivoice.utils.AccountUtil;
import com.aurora.iunivoice.utils.Log;

public class FindFragment extends BaseViewPagerFragment implements
		OnClickListener {

	private static final String TAG = "FindFragment";
	private RelativeLayout rl_shop;
	private RelativeLayout rl_call;
	private RelativeLayout rl_feedback;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "FindFragment onActivityCreated()");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_find1, container, false);
	}

	@Override
	public void handleMessage(Message msg) {

	}

	@Override
	public void setupViews() {
		rl_shop = (RelativeLayout) getView().findViewById(R.id.rl_shop);
		rl_call = (RelativeLayout) getView().findViewById(R.id.rl_call);
		rl_feedback = (RelativeLayout) getView()
				.findViewById(R.id.rl_feedback);

		rl_shop.setOnClickListener(this);
		rl_call.setOnClickListener(this);
		rl_feedback.setOnClickListener(this);
	}

	@Override
	protected void loadData() {

	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.rl_shop:
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://m.iuni.com"));
			// DialogUtil.getLoadingDialog(getActivity(), "dsfsdf").show();

			break;
		// case R.id.siv_qq:
		//
		// String url = "mqqwpa://im/chat?chat_type=wpa&uin=626398373";
		// intent.setAction(Intent.ACTION_VIEW);
		// intent.setData(Uri.parse(url));
		// ComponentName component = intent.resolveActivity(getActivity()
		// .getPackageManager());
		// if (component == null) {
		// Toast.makeText(getActivity(), "请先安装QQ客服端", Toast.LENGTH_SHORT)
		// .show();
		// return;
		// }
		// break;
		case R.id.rl_call:
			intent.setAction(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel://4008666210"));
			break;
		case R.id.rl_feedback:
			if (AccountUtil.getInstance().isIuniOS()) {
				intent.setAction("com.android.settings.action.feedback");
				ComponentName resolveActivity = intent
						.resolveActivity(getActivity().getPackageManager());
				if (resolveActivity == null) {
					intent.setClass(getActivity(), UserFBActivity.class);
				}
			} else {
				intent.setClass(getActivity(), UserFBActivity.class);
			}
			break;
		default:
			break;
		}
		startActivity(intent);

	}

}