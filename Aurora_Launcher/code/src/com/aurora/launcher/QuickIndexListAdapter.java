package com.aurora.launcher;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.aurora.stickylistheaders.StickyListHeadersAdapter;
import com.aurora.util.StringUtils;
import com.aurora.util.Utils;
import com.aurora.view.GridViewForEmbed;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class QuickIndexListAdapter extends ArrayAdapter<AppCategoryData> implements 
                           StickyListHeadersAdapter, SectionIndexer{
	private Activity activity;
	private int[] sectionIndices;
	private Character[] sectionsLetters;
	private Typeface mTypeface;
	// 当应用数目少于多少时不需要使用软引用
	public static final int MAX_NUM_FOR_NO_SOFTREFERENCE = 400;
	private boolean useSoftReference;
	public void setUseSoftReference(boolean use){
		useSoftReference = use;
	}
	
	public boolean getUseSoftReference(){
		return useSoftReference;
	}
	
	/**
	 * key:CategoryName
	 * value:DefSoftManageGridAdapter
	 * 存放某一类应用对用的gridView Adapter
	 */
	private HashMap<String ,QuickIndexGridAdapter> gridAdapterMap;

	public QuickIndexListAdapter(Activity activity, List<AppCategoryData> appCategoryDataList, int initNum) {
		super(activity, 0, appCategoryDataList);
		//Log.e("HJJ", "init apps num:" + initNum);
		if(initNum > MAX_NUM_FOR_NO_SOFTREFERENCE){
			useSoftReference = true;
		} 
		this.activity = activity;
		sectionIndices = getSectionIndices();
		sectionsLetters = getStartingLetters();
		gridAdapterMap = new HashMap<String ,QuickIndexGridAdapter>();
		convertViewMap = new HashMap<String, View>();
		softReferenceViewMap = new HashMap<String, SoftReference<View>>();
	}
	
	private int[] getSectionIndices() {
		ArrayList<Integer> sectionIndices = new ArrayList<Integer>();	
		
		if(getCount()>0){
			AppCategoryData item = (AppCategoryData)getItem(0);
			String itemName = item.getCategoryName();
			sectionIndices.add(0);
			for (int i = 1; i < getCount(); i++) {
				item = (AppCategoryData)getItem(i);				
				if( !itemName.equals(item.getCategoryName()) ) {
					itemName = item.getCategoryName();
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
	
	private Character[] getStartingLetters() {
		Character[] letters = new Character[sectionIndices.length];
		for (int i = 0; i < sectionIndices.length; i++) {
			int index = sectionIndices[i];		
			String labelText = getLabelText(index);
			if(!StringUtils.isEmpty(labelText)){
				letters[i] = labelText.charAt(0);
			}			
		}
		return letters;
	}
	
	private String getLabelText(int position){
		String labelText = null;
		if(position >= getCount() || position < 0){
			return labelText;
		}
		AppCategoryData item = (AppCategoryData) getItem(position);
		if(item != null) {
			labelText = item.getCategoryName();	
		}
		
		return labelText;
	}
	
	private String getPinYin(int position){
		if(position >= getCount() || position < 0){
			return null;
		}
		AppCategoryData item = (AppCategoryData) getItem(position);
		
		if(item == null){
			return null;
		}		
		return Utils.getFirstPinYin(item.getCategoryName());
	}

	/**
	 * @hideHeader 这个跟listview中的header保持一致，当不需要header即为搜索模式，此时调整GridView的大小
	 */
	private boolean hideHeader = false;
	
	protected void setAdapterHeaderVisiblity(boolean flag){
		hideHeader = flag;
	}
	
	private HashMap<String, View> convertViewMap;
	private HashMap<String, SoftReference<View>> softReferenceViewMap;
	private HashSet<Integer> buildViewIndexSet = new HashSet<Integer>();
	public HashSet<Integer> getBuildViewIndexSet(){
		return buildViewIndexSet;
	}
	
	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.v("iht-quick", "---------------------position::"+position);
		
		/* 最初采用以下方法，但滑动不流畅
		GridViewItemCache holder;	
		if (convertView == null) {			
			LayoutInflater inflater = activity.getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.quick_index_list_item, parent, false);
			holder = new GridViewItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (GridViewItemCache)convertView.getTag();
		}
		
		if(getCount()<=position){
			return convertView;
		}
				
		AppCategoryData item = (AppCategoryData)getItem(position);
		QuickIndexGridAdapter gridAdapter = gridAdapterMap.get(item.getCategoryName());
		if(gridAdapter == null){
			gridAdapter = new QuickIndexGridAdapter(activity, item);
			gridAdapterMap.put(item.getCategoryName(), gridAdapter);
		}
		holder.getItemGridView().adjustSpaceForSearch(hideHeader);
		holder.getItemGridView().setAdapter(gridAdapter);*/
		
		View vv;
		if(useSoftReference) {
			AppCategoryData item = (AppCategoryData)getItem(position);
			SoftReference<View> sv = softReferenceViewMap.get(item.getCategoryName());
			//Log.e("HJJ", ">>>>>>>use softReference position:" + position + ",sv:" + sv);
			if (sv == null || sv.get() == null) {
				if (sv != null && sv.get() == null) {
					Log.e("HJJ", "sv.get() is null");
				}
				LayoutInflater inflater = activity.getLayoutInflater(); 
				vv = inflater.inflate(R.layout.quick_index_list_item, parent, false);
				GridViewItemCache holder = new GridViewItemCache(vv);
				vv.setTag(holder);
				
				QuickIndexGridAdapter gridAdapter = new QuickIndexGridAdapter(activity, item,holder.getItemGridView());
				holder.getItemGridView().setAdapter(gridAdapter);
				
				sv = new SoftReference<View>(vv);
				softReferenceViewMap.put(item.getCategoryName(), sv);
			} else {
				vv = sv.get();
			}
		} else {
			buildViewIndexSet.add(position);
			AppCategoryData item = (AppCategoryData)getItem(position);
			vv = convertViewMap.get(item.getCategoryName());
			//Log.e("HJJ", ">>>>>>>not use softReference position:" + position + ", vv:" + vv);
			if (vv == null) {
				LayoutInflater inflater = activity.getLayoutInflater(); 
				vv = inflater.inflate(R.layout.quick_index_list_item, parent, false);
				GridViewItemCache holder = new GridViewItemCache(vv);
				vv.setTag(holder);
				
				QuickIndexGridAdapter gridAdapter = new QuickIndexGridAdapter(activity, item,holder.getItemGridView());
				holder.getItemGridView().setAdapter(gridAdapter);
				
				convertViewMap.put(item.getCategoryName(), vv);
			} 
		}
		
		GridViewItemCache holder = (GridViewItemCache) vv.getTag();		
		holder.getItemGridView().adjustSpaceForSearch(hideHeader);
		
		//AURORA_START iht 2015-03-13 更新桌面Icons，快速检索页面的无法更新，需要刷新数据
		holder.getItemGridView().invalidateViews();
		//Log.v("iht-quick", "VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVv");
		//AURORA_END iht 2015-03-13
		
		return vv;
	}

	@Override public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewItemCache holder;
		if (convertView == null) {
			
			LayoutInflater inflater = activity.getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.quick_index_list_header, parent, false);
			holder = new HeaderViewItemCache(convertView);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewItemCache)convertView.getTag();
		}
		
		holder.getLabelText().setText(getLabelText(position));
		return convertView;
	}

	//remember that these have to be static, postion=1 should walys return the same Id that is.
	@Override
	public long getHeaderId(int position) {		
		String pinyin = getPinYin(position);
		if(!StringUtils.isEmpty(pinyin)){
			return pinyin.charAt(0);
		}else{
			return 0;
		}		
	}

	@Override
	public int getPositionForSection(int section) {
		if(section >= sectionIndices.length) {
			section = sectionIndices.length-1;
		}else if(section < 0){
			section = 0;
		}
		return sectionIndices[section];
	}

	// Aurora <haojj> <2014-2-27> add for 得到长度 begin
	public int getSectionLength(){
		if(sectionIndices == null || sectionIndices.length < 1) {
			return 0;
		}
		return sectionIndices.length;
	}
	// Aurora <haojj> <2014-2-27> end
	
	@Override
	public int getSectionForPosition(int position) {
		for (int i = 0; i < sectionIndices.length; i++) {
			if(position < sectionIndices[i]) {
				return i-1;
			}
		}
		return sectionIndices.length-1;
	}

	@Override
	public Object[] getSections() {
		return sectionsLetters;
	}
	
	public void clear(){
		sectionIndices = new int[0];
		sectionsLetters = new Character[0];
		notifyDataSetChanged();
	}
	
	public void restore(){
		sectionIndices = getSectionIndices();
		sectionsLetters = getStartingLetters();
		notifyDataSetChanged();
	}
	
	public void mNotify(){
		if(gridAdapterMap != null){
			gridAdapterMap.clear();
		}
		
		if(convertViewMap != null) {
			convertViewMap.clear();
		}
		
		if(softReferenceViewMap != null) {
			softReferenceViewMap.clear();
		}
		//notifyDataSetChanged();
		//Log.e("HJJ", "mNotify(void) convertViewMap size:" + convertViewMap.size() + ", softReferenceViewMap size:" + softReferenceViewMap.size());
		restore();
	}
	
	public void mNotify(ArrayList<String> titles){
		if(gridAdapterMap != null){
			gridAdapterMap.clear();
		}
		
		if(convertViewMap != null) {
			for(int i=0;i<titles.size();i++){
				String key = titles.get(i);
				if(convertViewMap.containsKey(key)){
					convertViewMap.remove(key);
				}
			}
		}
		
		if(softReferenceViewMap != null) {
			for(int i=0;i<titles.size();i++){
				String key = titles.get(i);
				if(softReferenceViewMap.containsKey(key)){
					softReferenceViewMap.remove(key);
				}
			}
		}
		//Log.e("HJJ", "mNotify(param1) convertViewMap size:" + convertViewMap.size() + ", softReferenceViewMap size:" + softReferenceViewMap.size());
		restore();
	}
	
	private class GridViewItemCache{
		private GridViewForEmbed mGridView;
		public GridViewItemCache(View view) {
			// TODO Auto-generated constructor stub
			mGridView = (GridViewForEmbed) view.findViewById(R.id.item_grid_view);
		}
		
		public GridViewForEmbed getItemGridView(){
			return mGridView;
		}
	}
	
	private class HeaderViewItemCache{
		private TextView mLabelView;
		
		public HeaderViewItemCache(View view) {
			// TODO Auto-generated constructor stub
			mLabelView = (TextView) view.findViewById(R.id.label_text);
			try {
				mTypeface = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
				mLabelView.setTypeface(mTypeface);
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		
		public TextView getLabelText(){
			return mLabelView;
		}
	}
	
	
}
