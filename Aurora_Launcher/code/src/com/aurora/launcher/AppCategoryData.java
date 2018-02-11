package com.aurora.launcher;

import java.util.ArrayList;
import java.util.HashSet;

public class AppCategoryData {
	private final String mLetter;
	private ArrayList<ShortcutInfo> mShortcutInfos;
	
	public AppCategoryData(String letter){
		mLetter = letter;
	}
			
	public AppCategoryData(String letter, ArrayList<ShortcutInfo> infos) {
		// TODO Auto-generated constructor stub
		mLetter = letter;
		mShortcutInfos = infos;
	}
	
	public void addShortcutInfo(HashSet<ShortcutInfo> infos, boolean clear){
		if(mShortcutInfos == null){
			mShortcutInfos = new ArrayList<ShortcutInfo>();
		}
		if(clear) {
			mShortcutInfos.clear();
		}
		mShortcutInfos.addAll(infos);
	}
	
	public void addShortcutInfo(ShortcutInfo item){
		if(mShortcutInfos == null){
			mShortcutInfos = new ArrayList<ShortcutInfo>();
		}
		mShortcutInfos.add(item);
	}
	
	public void removeShortcutInfo(ShortcutInfo item){
		if(mShortcutInfos != null) {
			mShortcutInfos.remove(item);
		}
	}
	
	public String getCategoryName(){
		return mLetter;
	}
	
	public ArrayList<ShortcutInfo> getCategoryDataList(){
		return mShortcutInfos;
	}
	
	public int getCategoryDataSize(){
		if(mShortcutInfos == null) {
			return 0;
		}
		return mShortcutInfos.size();
	}
	
}
