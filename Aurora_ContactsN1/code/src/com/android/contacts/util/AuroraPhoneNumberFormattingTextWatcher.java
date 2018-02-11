package com.android.contacts.util;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.util.Log;
import android.widget.TextView;
//aurora change zhouxiaobing 20130918
public class AuroraPhoneNumberFormattingTextWatcher extends PhoneNumberFormattingTextWatcher{
	 private boolean is_self_change;
	 private boolean is_self_change_befor;
	 private boolean is_self_change_after;
     private TextView textview;
    public AuroraPhoneNumberFormattingTextWatcher() {
        super();
    }

    /**
     * The formatting is based on the given <code>countryCode</code>.
     *
     * @param countryCode the ISO 3166-1 two-letter country code that indicates the country/region
     * where the phone number is being entered.
     *
     * @hide
     */
    public AuroraPhoneNumberFormattingTextWatcher(String countryCode) {
        super(countryCode);
    }	
   
    public AuroraPhoneNumberFormattingTextWatcher(String countryCode,TextView tv)
    {
    	super(countryCode);
    	textview=tv;
    }
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		if(is_self_change_befor)
			return;			
		// TODO Auto-generated method stub
		is_self_change_befor=true;
		Log.v("AuroraPhoneNumberFormattingTextWatcher", "beforeTextChanged"+s.toString());
/*		Editable edit=textview.getEditableText();
		String string=edit.toString();
		string=string.replace('-', ' ');
		edit.replace(0, string.length(), string, 0, s.length());		
		super.beforeTextChanged(s, start, count, after);*/
		is_self_change_befor=false;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		if(is_self_change)
			return;
		is_self_change=true;
		Log.v("AuroraPhoneNumberFormattingTextWatcher", "onTextChanged"+s.toString());
		super.onTextChanged(s, start, before, count);
		is_self_change=false;
	}

	@Override
	public synchronized void afterTextChanged(Editable s) {
		if(is_self_change_after)
			return;		
		is_self_change_after=true;
		Log.v("AuroraPhoneNumberFormattingTextWatcher", "afterTextChanged"+s.toString());
		

		super.afterTextChanged(s);			
/*		String string=s.toString();
		string=string.replace(' ', '-');
		s.replace(0, string.length(), string, 0, s.length());*/
		is_self_change_after=false;
	}

	
}
