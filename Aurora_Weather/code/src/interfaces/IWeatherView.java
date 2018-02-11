package interfaces;

public interface IWeatherView {
	
	/**
	 * the view's state , COMING_IN means view just prepare to show , we should call entry animation
	 */
	public static final int COMING_IN = 0;
	
	/**
	 * the view's state , NORMAL_SHOWING means view just has prepared ready , we just normal showing 
	 */
	public static final int NORMAL_SHOWING = 1;
	
	/**
	 *  the view's state , COMING_OUT means view prepared ready , we should call exit animation 
	 */
	public static final int COMING_OUT = 2;
	
	/**
	 * pressed back key quit application
	 */
	public static final int QUIT_APP = 3;
	
	public static final int UPDATING = 4;

	public static final int CURRENT_PAGE_SELECT=5;
	/**
	 * when state = COMING_IN, call this !!!
	 */
	public void startEntryAnim();
	
	/**
	 * when state = NORMAL_SHOWING, call this !!!
	 */
	public void normalShow();
	
	/**
	 * when state = COMING_OUT, call this !!!
	 */
	public void startExitAnim();
	
	/**
	 * 
	 * @param state : COMING_IN , NORMAL_SHOWING , COMING_OUT
	 */
	public void onStateChanged(int state);
	
	/**
	 * when quit this application, we should destroy all resources !!!
	 */
	public void onDestroy();
	
	/**
	 * push down to update data, surface changed!!!
	 */
	public void onUpdating();
	
	/**
	 * after update,data Changed ,we should update our Surface !!!
	 */
	public void onDataChanged();
	
	/**
	 * @each child view should to know which city to show !!!
	 * @param index : data index 
	 */
	public void setIndex(int index);
}
