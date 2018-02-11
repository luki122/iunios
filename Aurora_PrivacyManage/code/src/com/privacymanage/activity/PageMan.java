package com.privacymanage.activity;

import android.util.Log;

public class PageMan {
	
	
	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:27:04 .
	 * @param thisIndex
	 * @return
	 */
	public FounderPage getNextPage(int thisIndex) {
		Log.d("newPr","page 0 = " + mPageChain[0]);
		Log.d("newPr","page 1 = " + mPageChain[1]);
		return mPageChain[thisIndex + 1];
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:45:41 .
	 * @param thisIndex
	 * @return
	 */
	public FounderPage getPreviousPage(int thisIndex) {
		return mPageChain[thisIndex - 1];
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:50:19 .
	 * @param thisIndex
	 * @return
	 */
	public boolean isFirstPage(int thisIndex) {
		return thisIndex == FIRST_PAGE_INDEX;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:54:18 .
	 * @param thisIndex
	 * @return
	 */
	public boolean isLastPage(int thisIndex) {
		return !pageIndexIsValid(thisIndex + 1);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午4:06:18 .
	 * @param page
	 */
	public void put(FounderPage page) {
		int postion = page.getPageId();
		mPageChain[postion] = page;
	}
	
	/**
	 * clear all of the pages in the page manager
	 * Vulcan created this method in 2014年10月11日 下午5:29:21 .
	 */
	public void clear() {
		for(int i = 0;i < mPageChain.length;i ++) {
			mPageChain[i] = null;
		}
		return;
	}
	
	
	private static final int MAX_PAGE_INDEX = 10;
	private static final int FIRST_PAGE_INDEX = 0;
	
	

	private FounderPage[] mPageChain = new FounderPage[MAX_PAGE_INDEX + 1];
	
	private static PageMan mInstance = new PageMan();
	
	private PageMan() {
		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年9月29日 下午3:32:30 .
	 * @return
	 */
	public static  PageMan getInstance() {
		return mInstance;
	}


	
	/**
	 * 
	 * Vulcan created this method in 2014年9月30日 下午2:33:07 .
	 * @param index
	 * @return
	 */
	public boolean pageIndexIsValid(int index) {
		boolean inBounds =  (index >= FIRST_PAGE_INDEX && index <= MAX_PAGE_INDEX);
		if(!inBounds) {
			return false;
		}
		
		return mPageChain[index] != null;
	}
}
