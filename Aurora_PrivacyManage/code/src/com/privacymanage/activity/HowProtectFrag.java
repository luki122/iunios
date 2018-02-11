/**
 * Vulcan created this file in 2014年10月22日 上午9:21:20 .
 */
package com.privacymanage.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Vulcan created HowProtectFrag in 2014年10月22日 .
 * 
 */
public class HowProtectFrag extends Fragment {

	/**
	 * 
	 */
	public HowProtectFrag() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = null;
		rootView = inflater.inflate(ResIdMan.LAYOUT_FILE_HOW_PROTECT,container,false);
		//rootView = inflater.inflate(ResIdMan.LAYOUT_FILE_NEXT_STEP,null,false);
		
		 //final TextView tvHowProtect = (TextView)rootView.findViewById(ResIdMan.TEXTVIEW_HOW_PROTECT);
		 //Typeface auroraTitleFace = Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		 //tvHowProtect.setTypeface(auroraTitleFace);
		
		//return super.onCreateView(inflater, container, savedInstanceState);
		return rootView;
	}

}
