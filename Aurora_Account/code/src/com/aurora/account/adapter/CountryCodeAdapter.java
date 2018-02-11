package com.aurora.account.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.aurora.account.R;
import com.aurora.account.model.CountryCode;
import com.aurora.account.util.PingYinUtil;
import com.aurora.account.widget.stickylistheaders.StickyListHeadersAdapter;

public class CountryCodeAdapter extends BaseAdapter implements
		StickyListHeadersAdapter, SectionIndexer {
	
	private LayoutInflater inflater;
	
	private List<CountryCode> listData;
	
	private int[] sectionIndices;
	private Character[] sectionsHeaders;
	
	public CountryCodeAdapter(Context context, List<CountryCode> listData) {
		inflater = LayoutInflater.from(context);
		this.listData = listData;
		
		updateSectionIndice();
	}
	
	public void updateSectionIndice() {
		sectionIndices = getSectionIndices();
		sectionsHeaders = getSectionHeaders();
	}
	
	private int[] getSectionIndices() {
		ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
		if (getCount() > 0) {
			char c = PingYinUtil.getFirstLetter(listData.get(0).getCountryOrRegionsCN().charAt(0));
			sectionIndices.add(0);
			for (int i = 1; i < getCount(); i++) {
				CountryCode countryCode = listData.get(i);
				if (PingYinUtil.getFirstLetter(countryCode.getCountryOrRegionsCN().charAt(0)) != c) {
					c = PingYinUtil.getFirstLetter(countryCode.getCountryOrRegionsCN().charAt(0));
					sectionIndices.add(i);
				}
			}
		}
		int[] sections = new int[sectionIndices.size()];
		for (int i = 0; i < sectionIndices.size(); i++) {
			sections[i] = sectionIndices.get(i);
		}
		
		return sections;
	}

	private Character[] getSectionHeaders() {
		Character[] sectionHeaders = new Character[sectionIndices.length];
		for (int i = 0; i < sectionIndices.length; i++) {
			sectionHeaders[i] = PingYinUtil.getFirstLetter(
					listData.get(sectionIndices[i]).getCountryOrRegionsCN().charAt(0));
		}
		
		return sectionHeaders;
	}

	@Override
	public int getCount() {
		return listData == null ? 0 : listData.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_country_code, null);
			holder = new Holder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.tv_name.setText(listData.get(position).getCountryOrRegionsCN()
				+ "  +" + listData.get(position).getCode());
		
		return convertView;
	}

	@Override
	public Object[] getSections() {
		return sectionsHeaders;
	}

	@Override
	public int getPositionForSection(int sectionIndex) {
		if (sectionIndex >= sectionIndices.length) {
			sectionIndex = sectionIndices.length - 1;
		} else if (sectionIndex < 0) {
			sectionIndex = 0;
		}
		return sectionIndices[sectionIndex];
	}

	@Override
	public int getSectionForPosition(int position) {
		for (int i = 0; i < sectionIndices.length; i++) {
			if (position < sectionIndices[i]) {
				return i - 1;
			}
		}
		return sectionIndices.length - 1;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderHolder headerHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_header_country_code, null);
			headerHolder = new HeaderHolder();
			headerHolder.tv_indices = (TextView) convertView.findViewById(R.id.tv_indices);
			convertView.setTag(headerHolder);
		} else {
			headerHolder = (HeaderHolder) convertView.getTag();
		}
		
		headerHolder.tv_indices.setText(sectionsHeaders[getSectionForPosition(position)] + "");
		
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		return getSectionForPosition(position);
	}
	
	static class Holder {
		TextView tv_name;
	}
	
	static class HeaderHolder {
		TextView tv_indices;
	}
	
	public int findPosition(String s) {
		for (int i = 0; i < sectionsHeaders.length; i++) {
			if (s.equalsIgnoreCase(String.valueOf(sectionsHeaders[i]))) {
				return getPositionForSection(i);
			}
		}
		return -1;
	}

}
