package com.aurora.iunivoice.activity;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.utils.SystemUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideFragment extends Fragment implements OnClickListener {

	private View mRootView;
	private ImageView mImg;
	private TextView mTvUp;
	private TextView mTvDown;
	private Button mBtnLogin;
	private Button mBtnLL;
	private LinearLayout mLL;
	private SharedPreferences sp;
	private int type;

	public static GuideFragment newInstance(int type) {
		GuideFragment f = new GuideFragment();
		Bundle b = new Bundle();
		b.putInt("type", type);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			int type = args.getInt("type");
			this.type = type + 1;
		}
		sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initView();
		initEvent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = inflater.inflate(R.layout.activity_guide_fragment, null);

		return mRootView;
	}

	public void initView() {
		mLL = (LinearLayout) mRootView.findViewById(R.id.ll);
		mImg = (ImageView) mRootView.findViewById(R.id.iv_img);
		mBtnLL = (Button) mRootView.findViewById(R.id.btn_ll);
		mBtnLogin = (Button) mRootView.findViewById(R.id.btn_login);
		mTvUp = (TextView) mRootView.findViewById(R.id.tv_up_text);
		mTvDown = (TextView) mRootView.findViewById(R.id.tv_down_text);

		switch (type) {
		case 1:
			mImg.setBackgroundResource(R.drawable.guide_one_pic);
			mTvUp.setText(R.string.guide_one_up);
			mTvDown.setText(R.string.guide_one_down);
			mLL.setVisibility(8);
			break;
		case 2:
			mImg.setBackgroundResource(R.drawable.guide_two_pic);
			mTvUp.setText(R.string.guide_two_up);
			mTvDown.setText(R.string.guide_two_down);
			mLL.setVisibility(8);
			break;
		case 3:
			mImg.setBackgroundResource(R.drawable.guide_three_pic);
			mTvUp.setText(R.string.guide_three_up);
			mTvDown.setText(R.string.guide_three_down);
			mLL.setVisibility(0);
			break;

		default:
			break;
		}
	}

	private void initEvent() {
		mBtnLL.setOnClickListener(this);
		mBtnLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		sp.edit().putBoolean("isFirst", false).commit();
		sp.edit().putInt("versioncode",SystemUtils.getVersionCode(getActivity(), getActivity().getPackageName())).commit();
		Intent main = new Intent(getActivity(), MainActivity.class);
		
		switch (v.getId()) {
		case R.id.btn_ll:
			main.putExtra("login", false);
			break;
		case R.id.btn_login:
			main.putExtra("login", true);
			break;

		default:
			break;
		}
		startActivity(main);
		getActivity().finish();
	}
}
