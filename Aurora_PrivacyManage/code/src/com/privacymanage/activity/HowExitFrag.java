/**
 * Vulcan created this file in 2014年10月22日 上午9:21:37 .
 */
package com.privacymanage.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Vulcan created HowExitFrag in 2014年10月22日 .
 * 
 */
public class HowExitFrag extends Fragment {

	/**
	 * 
	 */
	public HowExitFrag() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = null;
		rootView = inflater.inflate(ResIdMan.LAYOUT_FILE_HOW_EXIT,container,false);
		
		final TextView tvExitMethod = (TextView)rootView.findViewById(ResIdMan.TEXTVIEW_EXIT_METHOD);
		tvExitMethod.setText(Html.fromHtml(getString(ResIdMan.STRING_EXIT_METHOD)));
		
		 //final TextView tvHowExit = (TextView)rootView.findViewById(ResIdMan.TEXTVIEW_HOW_EXIT);
		 //Typeface auroraTitleFace = Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		 //tvHowExit.setTypeface(auroraTitleFace);
		
		//return super.onCreateView(inflater, container, savedInstanceState);
		return rootView;
	}

}
