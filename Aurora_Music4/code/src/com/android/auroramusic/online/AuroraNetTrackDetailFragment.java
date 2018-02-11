package com.android.auroramusic.online;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.auroramusic.util.LogUtil;
import com.android.music.R;

public class AuroraNetTrackDetailFragment extends Fragment {

	private static final String TAG = "AuroraHomeFragment";
	private AuroraNetTrackDetail mRecommend = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mRecommend = new AuroraNetTrackDetail();
		mRecommend.initview(getView(), getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return LayoutInflater.from(getActivity()).inflate(R.layout.aurora_nettrackdetail_fragment,
				null);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mRecommend.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		LogUtil.d(TAG, "onResume");
		mRecommend.onResume();
	}
	public void playAll(int start){
		mRecommend.playAll(start);
	}
	public void showAnimation(){
		mRecommend.showAnimation();
	}
	public void Destroy(){
		mRecommend.Destroy();
	}
	public View getPlaySelect(){
		if(mRecommend!=null){
			return mRecommend.getPlaySelect();
		}
		return null;
	}
}
