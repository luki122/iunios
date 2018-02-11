package com.aurora.email.adapters;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.inputmethodservice.Keyboard.Key;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.util.Rfc822Token;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.android.mail.R;
import com.android.mail.providers.Address;
import com.android.email.provider.AuroraAutoCompleteDBHelper;

public class AuroraComposeMatchAdapter extends SimpleCursorAdapter {

	Activity mActivity;
	private boolean mFlag;
	private String mKey;
	private String matchString;
	class ViewHold {
		TextView mContact;
		TextView mTitle;
	}

	public AuroraComposeMatchAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		// TODO Auto-generated constructor stub
		mActivity = (Activity) context;
	}

	public AuroraComposeMatchAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		// TODO Auto-generated constructor stub
		mActivity = (Activity) context;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		View arg1 = super.newView(context, cursor, parent);
		ViewHold vh = new ViewHold();
		vh.mContact = (TextView) arg1.findViewById(R.id.contact_address);
		vh.mTitle = (TextView) arg1.findViewById(R.id.contact_newly);
		arg1.setTag(vh);
		return arg1;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		ViewHold vh = (ViewHold) view.getTag();
		if (cursor.getPosition() == 0 && mFlag) {
			vh.mTitle.setVisibility(View.VISIBLE);
		} else {
			vh.mTitle.setVisibility(View.INVISIBLE);
		}
		Rfc822Token token = new Rfc822Token(cursor.getString(2),
				cursor.getString(1), "");
		String name = Address.decodeAddressName(token.getName()).trim();
		String text = name + " (" + cursor.getString(1) + ")";
		if (TextUtils.isEmpty(mKey)) {
			vh.mContact.setText(text);
			return;
		}
		if(!match(vh, true, name, text)){
			match(vh, false, name, text);
		}
	}

	@Override
	public void changeCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		if (mActivity.isFinishing() && cursor != null) {
			cursor.close();
			cursor = null;
		}
		if (cursor != null) {
			super.changeCursor(cursor);
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return super.getItem(position);
	}

	public void setFlag(boolean flag) {
		mFlag = flag;
	}

	public void setKey(String key) {
		mKey = key;
	}
	public boolean match(ViewHold vh,boolean all,String name,String text){
		int start = -1;
		boolean flag = false;
		int length = 0;
		matchString = "";
		if(all){
			for (int i = 0; i < name.length(); i++) {
				matchString += AuroraAutoCompleteDBHelper.getSpell(String
						.valueOf(name.charAt(i)));	
				if (!matchString.isEmpty()&&matchString.toUpperCase().indexOf(mKey.toUpperCase())==0) {
					if (start < 0) {
						start = i;
					}
					length++;
					flag = true;
					break;
				}else if(!matchString.isEmpty()&&mKey.toUpperCase().indexOf(matchString.toUpperCase())<0){
					matchString = "";
				}else{
					length++;
				}

			}
			if (!flag) {
				start = text.indexOf(mKey);
				length = mKey.length();
			}else{
				if(length>mKey.length()){
					length = mKey.length();
				}
				start = start - length +1;
			}
			if (start >= 0) {
				String text2 = text.substring(0, start);
				String text3 = "<font color=\"#f4752d\" size=\"13sp\">"
						+ text.substring(start, start + length) + "</font>";
				String text4 = text.substring(start + length);
				vh.mContact.setText(Html.fromHtml(text2 + text3 + text4));
			} else {
				vh.mContact.setText(text);
			}
			return flag;
		}else{
			for (int i = 0; i < name.length(); i++) {
				String pinYin = AuroraAutoCompleteDBHelper.getSpell(String
						.valueOf(name.charAt(i)));
				if(!pinYin.isEmpty())
					matchString += pinYin.substring(0,1);

			}
			start = matchString.toUpperCase().indexOf(mKey.toUpperCase());
			if(start<0){
				start = text.indexOf(mKey);
			}
			if (start >= 0) {
				String text2 = text.substring(0, start);
				String text3 = "<font color=\"#f4752d\" size=\"13sp\">"
						+ text.substring(start, start + mKey.length()) + "</font>";
				String text4 = text.substring(start + mKey.length());
				vh.mContact.setText(Html.fromHtml(text2 + text3 + text4));
				return true;
			} else {
				vh.mContact.setText(text);
				return false;
			}
		}
	}

}
