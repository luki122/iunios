package com.aurora.account.adapter;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;

public class FragmentAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> listFragment;
	
	public FragmentAdapter(FragmentManager fm, List<Fragment> listFragment) {
		super(fm);
		this.listFragment = listFragment;
	}

	@Override
	public Fragment getItem(int position) {
		return listFragment.get(position);
	}

	@Override
	public int getCount() {
		return listFragment == null ? 0 : listFragment.size();
	}

}
