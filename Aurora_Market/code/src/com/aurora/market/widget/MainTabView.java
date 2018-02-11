package com.aurora.market.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.aurora.market.R;
import com.aurora.market.activity.module.AppListActivity;
import com.aurora.market.activity.module.AppRankingActivity;
import com.aurora.market.activity.module.CategoryActivity;
import com.aurora.market.activity.module.SpecialActivity;

public class MainTabView extends FrameLayout implements OnClickListener {
	
	private LinearLayout bg;
	
	private MainTabItemView mtiv_new;
	private MainTabItemView mtiv_special;
	private MainTabItemView mtiv_ranking;
	private MainTabItemView mtiv_category;
	
	private int height;
	private int bgHeight;
	private int maintabMaxHeight;
	private int maintabMinHeight;
	
	private int tempHeight;

	public MainTabView(Context context) {
		super(context);
		initView();
	}

	public MainTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public MainTabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.view_main_tab, this);
		
		bg = (LinearLayout) view.findViewById(R.id.bg);
		mtiv_new = (MainTabItemView) view.findViewById(R.id.mtiv_new);
		mtiv_special = (MainTabItemView) view.findViewById(R.id.mtiv_special);
		mtiv_ranking = (MainTabItemView) view.findViewById(R.id.mtiv_ranking);
		mtiv_category = (MainTabItemView) view.findViewById(R.id.mtiv_category);
		
		maintabMaxHeight = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_height_max);
		maintabMinHeight = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_height_min);
		tempHeight = getResources().getDimensionPixelOffset(R.dimen.homepage_main_tab_temp_top);
		
		mtiv_new.setOnClickListener(this);
		mtiv_special.setOnClickListener(this);
		mtiv_ranking.setOnClickListener(this);
		mtiv_category.setOnClickListener(this);
	}
	
	/**
	 * @Title: setProgress
	 * @Description: 设置动画进度
	 * @param @param progress
	 * @return void
	 * @throws
	 */
	public void setProgress(float progress) {
		if (height == 0) {
			height = getHeight();
		}
		if (bgHeight == 0) {
			bgHeight = bg.getHeight();
		}
		
		float translationY = (height - maintabMinHeight) * progress;
		setTranslationY(-translationY);
		
		int temp = 0;
		if (progress <= 0.98) {
			temp = tempHeight;
		} else {
			temp = 0;
		}
		
		bg.setTranslationY((maintabMaxHeight - maintabMinHeight - temp) * progress);
		
		android.view.ViewGroup.LayoutParams params = bg.getLayoutParams();
		params.height = (int) (bgHeight - (maintabMaxHeight - maintabMinHeight - temp) * progress);
		bg.setLayoutParams(params);
		
		mtiv_new.setFast(0.3f);
		mtiv_special.setFast(0.2f);
		mtiv_ranking.setFast(0.1f);
		mtiv_category.setFast(0.0f);
		
		mtiv_new.setProgress(progress);
		mtiv_special.setProgress(progress);
		mtiv_ranking.setProgress(progress);
		mtiv_category.setProgress(progress);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mtiv_new:
			Intent newIntent = new Intent(getContext(),
					AppListActivity.class);
			newIntent.putExtra(AppListActivity.OPEN_TYPE, AppListActivity.TYPE_NEW);
			getContext().startActivity(newIntent);
			break;
		case R.id.mtiv_special:
			Intent specialIntent = new Intent(getContext(),
					SpecialActivity.class);
			getContext().startActivity(specialIntent);
			break;
		case R.id.mtiv_ranking:
			Intent rankingIntent = new Intent(getContext(),
					AppRankingActivity.class);
			getContext().startActivity(rankingIntent);
			break;
		case R.id.mtiv_category:
			Intent categoryIntent = new Intent(getContext(),
					CategoryActivity.class);
			getContext().startActivity(categoryIntent);
			break;
		}
	}

}
