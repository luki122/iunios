/**
 * Vulcan created this file in 2014年10月22日 上午9:15:17 .
 */
package com.privacymanage.activity;

import android.os.Bundle;
import android.app.Fragment;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Vulcan created HowEnterFrag in 2014年10月22日 .
 * 
 */
public class HowEnterFrag extends Fragment {

	/**
	 * 
	 */
	public HowEnterFrag() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月25日 上午10:09:43 .
	 * @param tvHowEnter
	 */
	protected void setupEnterMethodTextViewStyle(TextView tvEnterMethod) {
        //final TextView tvHowEnter = (TextView) getActivity().findViewById(ResIdMan.TEXTVIEW_HOW_ENTER);
        String strEnterMethod = tvEnterMethod.getText().toString();
        int start = strEnterMethod.indexOf('#');
        int end = strEnterMethod.indexOf('#', start + 1);
        Spannable word = new SpannableString(strEnterMethod);
        word.setSpan(new ForegroundColorSpan(Color.parseColor("#019C73")), start, end + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tvEnterMethod.setText(word);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月25日 下午12:07:29 .
	 * @param tvEnterMethod
	 * @param resid
	 */
	protected void setupEnterMethodTextViewStyle(TextView tvEnterMethod,int resid) {
		Spanned spanned = Html.fromHtml(getResources().getString(resid));
		tvEnterMethod.setText(spanned);
		return;
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = null;
		//rootView = inflater.inflate(ResIdMan.LAYOUT_FILE_HOW_ENTER,container,false);
		rootView = inflater.inflate(ResIdMan.LAYOUT_FILE_HOW_ENTER,container,false);
		
		
		//Typeface auroraTitleFace = Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		
		 final TextView tvEnterMethod = (TextView)rootView.findViewById(ResIdMan.TEXTVIEW_ENTER_METHOD);
		 tvEnterMethod.setText(Html.fromHtml(getString(ResIdMan.STRING_ENTER_METHOD)));
		 //tvEnterMethod.setTypeface(auroraTitleFace);
		 
		 //final TextView tvHowEnter = (TextView)rootView.findViewById(ResIdMan.TEXTVIEW_HOW_ENTER);
		 //Typeface auroraTitleFace = Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
		 //tvHowEnter.setTypeface(auroraTitleFace);
		
		//return super.onCreateView(inflater, container, savedInstanceState);
		return rootView;
	}
	
	


}
