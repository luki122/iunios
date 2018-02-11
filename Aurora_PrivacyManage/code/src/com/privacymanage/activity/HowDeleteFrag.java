/**
 * Vulcan created this file in 2014年10月23日 下午4:11:14 .
 */
package com.privacymanage.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Vulcan created HowDeleteFrag in 2014年10月23日 .
 * 
 */
public class HowDeleteFrag extends Fragment {

	/**
	 * 
	 */
	public HowDeleteFrag() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = null;
		rootView = inflater.inflate(ResIdMan.LAYOUT_FILE_HOW_DELETE,container,false);
		
		 //final TextView tvHowDelete = (TextView)rootView.findViewById(ResIdMan.TEXTVIEW_HOW_DELETE);
		 //Typeface auroraTitleFace = Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		 //tvHowDelete.setTypeface(auroraTitleFace);
		
		//return super.onCreateView(inflater, container, savedInstanceState);
		return rootView;
	}

}
