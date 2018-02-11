package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public class IndexedLinkedHashMap extends HashMap<String, FileTaskStatusBean>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2930859609395581415L;
	
	private LinkedList<String> mKeys;
	
	public IndexedLinkedHashMap() {
		super();
		mKeys = new LinkedList<String>();
	}
	
	public LinkedList<String> getKeys() {
		return mKeys;
	}

	@Override
	public FileTaskStatusBean put(String key, FileTaskStatusBean value) {
		if( ! containsKey(key)) {
			mKeys.addFirst(key);
		}
		return super.put(key, value);
	}
	
	public FileTaskStatusBean getByPosition(int pos) {
		String key = mKeys.get(pos);
		return get(key);
	}
	
	public int getSize() {
		return mKeys.size();
	}

	@Override
	public FileTaskStatusBean remove(Object key) {
		// TODO Auto-generated method stub
		for(int i = 0; i<mKeys.size(); i++) {
			if(key.equals(mKeys.get(i))) {
				mKeys.remove(i);
			}
		}
		return super.remove(key);
	}
}
