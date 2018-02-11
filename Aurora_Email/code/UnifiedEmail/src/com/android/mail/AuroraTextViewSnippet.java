package com.android.mail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import android.util.Log;
import android.text.SpannableStringBuilder;
import android.text.Spannable;

public class AuroraTextViewSnippet extends TextView{
	
	private static String sEllipsis = "\u2026";
    
	public AuroraTextViewSnippet(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AuroraTextViewSnippet(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
    public void setText(String fullText, String filterStr, Spannable spannable) {
		String patternString = Pattern.quote(filterStr);
		Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);

		int start = 0;
		
		if(null == spannable){
			spannable = new SpannableString(fullText);
		}

		Matcher m = pattern.matcher(fullText);
		while (m.find(start)) {
			spannable.setSpan(new ForegroundColorSpan(
							getContext().getResources().getColor(com.aurora.R.color.aurora_highlighted_color)),
							m.start(),
							m.end(),
							0);
			start = m.end();
		}
		setText(spannable);
    }

    public void setText(SpannableStringBuilder fullText, String filterStr) { 

		String str = fullText.toString();
        String patternString = Pattern.quote(filterStr);
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);


		int start = 0;

		Matcher m = pattern.matcher(str);
		while (m.find(start)) {
			fullText.setSpan(new ForegroundColorSpan(
							getContext().getResources().getColor(com.aurora.R.color.aurora_highlighted_color)),
							m.start(),
							m.end(),
							0);

			start = m.end();
		}
		setText(fullText);
    }
    
}
