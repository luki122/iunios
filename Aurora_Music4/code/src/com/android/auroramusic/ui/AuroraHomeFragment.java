package com.android.auroramusic.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;

import com.android.auroramusic.adapter.AuroraPlayerPagerAdapter;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.widget.AuroraViewPager;
import com.android.music.R;

public class AuroraHomeFragment extends Fragment {

	private static final String TAG = "AuroraHomeFragment";
	private AuroraMyMusicFragment myMusicfrag = null;
	private AuroraViewPager mViewPager = null;
	private OnMainPageChangeListener mOnMainPageChangeListener;
	private AuroraFindMusicFragment mAuroraFindMusicFragment = null;
	private View findMusicView, findMainMusic;
	private LayoutInflater inflater;
	private ViewStub stubView;

	public void setOnMainPageChangeListener(OnMainPageChangeListener l) {
		mOnMainPageChangeListener = l;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		List<View> mViews = new ArrayList<View>();
		inflater = LayoutInflater.from(getActivity());
		View myMusicView = inflater.inflate(R.layout.aurora_mymusic_fragement, null);
		mViews.add(myMusicView);
		// 发现歌曲页面是否隐藏
		if (Globals.SWITCH_FOR_ONLINE_MUSIC) {
			findMusicView = inflater.inflate(R.layout.aurora_findmusic_main, null);
			stubView = (ViewStub) findMusicView.findViewById(R.id.find_music_stub);
			mViews.add(findMusicView);
		}
		myMusicfrag = new AuroraMyMusicFragment();
		myMusicfrag.initview(myMusicView, getActivity());
		AuroraPlayerPagerAdapter mPagerAdapter = new AuroraPlayerPagerAdapter(mViews);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(mPageChangeListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.aurora_fragment_home_page, container, false);
		mViewPager = (AuroraViewPager) view.findViewById(R.id.id_container);
		return view;

	}

	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
		private int mPosition = 0;

		@Override
		public void onPageSelected(int position) {
			mPosition = position;
			if (mOnMainPageChangeListener != null) {
				mOnMainPageChangeListener.onPageSelected(position);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (mOnMainPageChangeListener != null) {
				mOnMainPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			LogUtil.d(TAG, "onPageScrollStateChanged " + state + " mPosition:" + mPosition);
			if (state == 0) {
				setOnlineMusic(mPosition);
			}
		}
	};

	public void setCurrentPage(int item) {
		if (mViewPager != null) {
			mViewPager.setCurrentItem(item);
			setOnlineMusic(item);
		}
	}

	@Override
	public void onPause() {
		myMusicfrag.onPause();
		if (mAuroraFindMusicFragment != null)
			mAuroraFindMusicFragment.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		myMusicfrag.onResume();
		if (mAuroraFindMusicFragment != null)
			mAuroraFindMusicFragment.onResume();
	}

	public interface OnMainPageChangeListener {
		public void onPageSelected(int position);

		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
	}

	public void setOnlineMusic(int position) {
		if (findMusicView == null)
			return;
		if (position == 1) {
			LogUtil.d(TAG, "setOnlineMusic AuroraFindMusicFragment:" + mAuroraFindMusicFragment);
			if (mAuroraFindMusicFragment == null) {
				mAuroraFindMusicFragment = new AuroraFindMusicFragment();
				if (findMainMusic == null) {
					findMainMusic = stubView.inflate();
				}
				mAuroraFindMusicFragment.initview(findMainMusic, (AuroraActivity) getActivity(), mViewPager);
			}
			mAuroraFindMusicFragment.isLoadData();

		}
	}
	
	

	public void onMediaDbChange() {
		if (myMusicfrag != null)
			myMusicfrag.notifiData();
	}

	// add by tangjie 2014/07/30 start
	/*public void changeButton(int type) {
		if (mAuroraFindMusicFragment != null) {
			mAuroraFindMusicFragment.changeButton(type);
		}
	}*/

	public void setPlayAnimation() {
		if (mAuroraFindMusicFragment != null)
			mAuroraFindMusicFragment.setPlayAnimation();
	}

	@Override
	public void onDestroy() {

		if (mAuroraFindMusicFragment != null) {
			mAuroraFindMusicFragment.destroy();
		}
		if (myMusicfrag != null) {
			myMusicfrag.destroy();
		}
		super.onDestroy();
	}

	// add by end

	public void hideSearchviewLayout() {
		if (mAuroraFindMusicFragment != null) {
			mAuroraFindMusicFragment.hideSearchviewLayout();
		}
	}

	public boolean isSearchBack() {
		if (mAuroraFindMusicFragment != null) {
			View layout = mAuroraFindMusicFragment.getSearchviewLayout();
			if (layout != null && layout.getVisibility() == View.VISIBLE) {
				return true;
			}
		}
		return false;
	}
	
}
