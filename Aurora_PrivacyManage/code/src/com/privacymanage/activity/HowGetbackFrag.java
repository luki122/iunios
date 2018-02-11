/**
 * Vulcan created this file in 2014年10月23日 下午4:12:27 .
 */
package com.privacymanage.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Vulcan created HowGetbackFrag in 2014年10月23日 .
 * 
 */
public class HowGetbackFrag extends Fragment {

	/**
	 * 
	 */
	public HowGetbackFrag() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = null;
		rootView = inflater.inflate(ResIdMan.LAYOUT_FILE_HOW_GETBACK_PASSWORD,container,false);
		
		 //final TextView tvHowGetback = (TextView)rootView.findViewById(ResIdMan.TEXTVIEW_HOW_GETBACK_PASSWORD);
		 //Typeface auroraTitleFace = Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		 //tvHowGetback.setTypeface(auroraTitleFace);
		
		//return super.onCreateView(inflater, container, savedInstanceState);
		return rootView;
	}

}
