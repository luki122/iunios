package com.android.contacts;


public interface FragmentCallbacks {
	public static final int FINISH_DIALPAD=0x01;
	public static final int COPY_NUMBER_TO_DIALPAD=0x02;
	public static final int SWITCH_TO_EDIT_MODE=0x03;
	public static final int SWITCH_TO_NORMAL_MODE=0x04;
	
	public void onFragmentCallback(int what,Object obj);
}
