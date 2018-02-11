/**
 * Copyright (c) 2011, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.mail.compose;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.android.mail.R;
import com.android.mail.providers.Address;
import com.android.mail.providers.Message;
import com.android.mail.utils.MyLog;
import com.android.mail.utils.Utils;

import java.text.DateFormat;
import java.util.Date;

/*
 * View for displaying the quoted text in the compose screen for a reply
 * or forward. A close button is included in the upper right to remove
 * the quoted text from the message.
 */
public class QuotedTextView extends LinearLayout implements OnClickListener {
    // HTML tags used to quote reply content
    // The following style must be in-sync with
    // pinto.app.MessageUtil.QUOTE_STYLE and
    // java/com/google/caribou/ui/pinto/modules/app/messageutil.js
    // BEG_QUOTE_BIDI is also available there when we support BIDI
    private static final String BLOCKQUOTE_BEGIN = "<blockquote class=\"quote\" style=\""
            + "margin:0 0 0 .8ex;" + "border-left:1px #ccc solid;" + "padding-left:1ex\">";
    private static final String BLOCKQUOTE_END = "</blockquote>";
    private static final String QUOTE_END = "</div>";

    // Separates the attribution headers (Subject, To, etc) from the body in
    // quoted text.
    private static final String HEADER_SEPARATOR = "<br type='attribution'>";
    private static final int HEADER_SEPARATOR_LENGTH = HEADER_SEPARATOR.length();

    private CharSequence mQuotedText;
    private WebView mQuotedTextWebView;
    private ShowHideQuotedTextListener mShowHideListener;
    private CheckBox mShowHideCheckBox;
    private boolean mIncludeText = true;
    private Button mRespondInlineButton;
    private RespondInlineListener mRespondInlineListener;
    private static String sQuoteBegin;

    public QuotedTextView(Context context) {
        this(context, null);
    }

    public QuotedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public QuotedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.quoted_text, this);

        mQuotedTextWebView = (WebView) findViewById(R.id.quoted_text_web_view);
        Utils.restrictWebView(mQuotedTextWebView);
        WebSettings settings = mQuotedTextWebView.getSettings();
        settings.setBlockNetworkLoads(false);//modif by chenhl
               
        mShowHideCheckBox = (CheckBox) findViewById(R.id.hide_quoted_text);
        mShowHideCheckBox.setChecked(true);
        mShowHideCheckBox.setOnClickListener(this);
        sQuoteBegin = context.getResources().getString(R.string.quote_begin);
        findViewById(R.id.hide_quoted_text_label).setOnClickListener(this);


        mRespondInlineButton = (Button) findViewById(R.id.respond_inline_button);
        if (mRespondInlineButton != null) {
            mRespondInlineButton.setEnabled(false);
        }
  
        //add by chenhl start for bug#10450
        mQuotedTextWebView.setBackgroundColor(0);
        mQuotedTextWebView.setBackgroundResource(R.drawable.aurora_quote_text_bg);
        mQuotedTextWebView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View view) {
				return true;
			}
		});
        //add by chenhl end
    }

    public void onDestroy() {
        if (mQuotedTextWebView != null) {
            mQuotedTextWebView.destroy();
        }
    }

    /**
     * Allow the user to include quoted text.
     * @param allow
     */
    public void allowQuotedText(boolean allow) {
        View quotedTextRow = findViewById(R.id.quoted_text_row);
        if (quotedTextRow != null) {
            quotedTextRow.setVisibility(allow? View.VISIBLE: View.INVISIBLE);
        }
    }

    /**
     * Allow the user to respond inline.
     * @param allow
     */
    public void allowRespondInline(boolean allow) {
        if (mRespondInlineButton != null) {
            mRespondInlineButton.setVisibility(allow? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Returns the quoted text if the user hasn't dismissed it, otherwise
     * returns null.
     */
    public CharSequence getQuotedTextIfIncluded() {
        if (mIncludeText) {
            return mQuotedText;
        }
        return null;
    }

    /**
     * Always returns the quoted text.
     */
    public CharSequence getQuotedText() {
        return mQuotedText;
    }

    /**
     * @return whether or not the user has selected to include quoted text.
     */
    public boolean isTextIncluded() {
        return mIncludeText;
    }

    public void setShowHideListener(ShowHideQuotedTextListener listener) {
        mShowHideListener = listener;
    }


    public void setRespondInlineListener(RespondInlineListener listener) {
        mRespondInlineListener = listener;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        if (id == R.id.respond_inline_button) {
            respondInline();
        } else if (id == R.id.hide_quoted_text) {
            updateCheckedState(mShowHideCheckBox.isChecked());
        } else if (id == R.id.hide_quoted_text_label) {
            updateCheckedState(!mShowHideCheckBox.isChecked());
        }
    }

    /**
     * Update the state of the checkbox for the QuotedTextView as if it were
     * tapped by the user. Also updates the visibility of the QuotedText area.
     * @param checked Either true or false.
     */
    public void updateCheckedState(boolean checked) {
        mShowHideCheckBox.setChecked(checked);
        updateQuotedTextVisibility(checked);
        if (mShowHideListener != null) {
            mShowHideListener.onShowHideQuotedText(checked);
        }
    }

    private void updateQuotedTextVisibility(boolean show) {
        mQuotedTextWebView.setVisibility(show ? View.VISIBLE : View.GONE);
        mIncludeText = show;
    }

    private void populateData() {
        String backgroundColor = getContext().getResources().getString(
                R.string.quoted_text_background_color_string);
        String fontColor = getContext().getResources().getString(
                R.string.quoted_text_font_color_string);
        //modify by chenhl start
        String html = "<head><style type=\"text/css\">* body { color: " + fontColor + "; } table {table-layout:fixed;} " 
                +" td {word-wrap:break-word;}"
        		+" img { max-width:100%;height:auto; }" 
                +" div {word-break:break-all;}"
                +"</style></head>"
                + mQuotedText.toString();
    //    Log.d("chenhl", "mQuotedText.toString():"+mQuotedText.toString());
        //modify by chenhl end
        mQuotedTextWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    private void respondInline() {
        // Copy the text in the quoted message to the body of the
        // message after stripping the html.
        final String plainText = Utils.convertHtmlToPlainText(getQuotedText().toString());
        if (mRespondInlineListener != null) {
            mRespondInlineListener.onRespondInline("\n" + plainText);
        }
        // Set quoted text to unchecked and not visible.
        updateCheckedState(false);
        mRespondInlineButton.setVisibility(View.GONE);
        // Hide everything to do with quoted text.
        View quotedTextView = findViewById(R.id.quoted_text_area);
        if (quotedTextView != null) {
            quotedTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Interface for listeners that want to be notified when quoted text
     * is shown / hidden.
     */
    public interface ShowHideQuotedTextListener {
        public void onShowHideQuotedText(boolean show);
    }

    /**
     * Interface for listeners that want to be notified when the user
     * chooses to respond inline.
     */
    public interface RespondInlineListener {
        public void onRespondInline(String text);
    }

    private static String getHtmlText(Message message) {
        if (message.bodyHtml != null) {
            return message.bodyHtml;
        } else if (message.bodyText != null) {
            // STOPSHIP Sanitize this
            return Html.toHtml(new SpannedString(message.bodyText));
        } else {
            return "";
        }
    }

    public void setQuotedText(int action, Message refMessage, boolean allow) {
        setVisibility(View.VISIBLE);
        String htmlText = getHtmlText(refMessage);
        StringBuilder quotedText = new StringBuilder();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        Date date = new Date(refMessage.dateReceivedMs);
        Resources resources = getContext().getResources();
        //modify by chenhl start 20141103
        if (action == ComposeActivity.REPLY || action == ComposeActivity.REPLY_ALL) {
            quotedText.append(sQuoteBegin);
            quotedText
                    .append(String.format(
                            resources.getString(R.string.reply_attribution),
                            dateFormat.format(date),
                            transAddressText(refMessage.getFromAddressesUnescaped())));
            quotedText.append(HEADER_SEPARATOR);
            //quotedText.append(BLOCKQUOTE_BEGIN);
            quotedText.append(htmlText);
            //quotedText.append(BLOCKQUOTE_END);
            quotedText.append(QUOTE_END);
        } else if (action == ComposeActivity.FORWARD) {
            quotedText.append(sQuoteBegin);
            quotedText
                    .append(String.format(resources.getString(R.string.forward_attribution),transAddressText(refMessage.getFromAddresses()), dateFormat.format(date), Utils
                            .cleanUpString(refMessage.subject,
                                    false /* don't remove empty quotes */),transAddressText(refMessage.getToAddresses())));
            String[] ccAddresses = refMessage.getCcAddresses();
            if(ccAddresses.length>0){
            	quotedText.append(String.format(resources.getString(R.string.cc_attribution),
            		transAddressText(ccAddresses)));
            }
            //quotedText.append(HEADER_SEPARATOR);
           // quotedText.append(BLOCKQUOTE_BEGIN);
            quotedText.append(htmlText);
           // quotedText.append(BLOCKQUOTE_END);
            quotedText.append(QUOTE_END);
        }
        //modify by chenhl end 20141103
        setQuotedText(quotedText);
        allowQuotedText(allow);
        // If there is quoted text, we always allow respond inline, since this
        // may be a forward.
        allowRespondInline(true);
    }

    public void setQuotedTextFromDraft(CharSequence htmlText, boolean forward) {
        setVisibility(View.VISIBLE);
        setQuotedText(htmlText);
        allowQuotedText(!forward);
        // If there is quoted text, we always allow respond inline, since this
        // may be a forward.
        allowRespondInline(true);
    }

    public void setQuotedTextFromHtml(CharSequence htmlText, boolean shouldQuoteText) {
        setVisibility(VISIBLE);
        if (shouldQuoteText) {
            final StringBuilder quotedText = new StringBuilder();
            final Resources resources = getContext().getResources();
            quotedText.append(sQuoteBegin);
            quotedText.append(
                    String.format(resources.getString(R.string.forward_attribution_no_headers)));
            quotedText.append(HEADER_SEPARATOR);
            quotedText.append(BLOCKQUOTE_BEGIN);
            quotedText.append(htmlText);
            quotedText.append(BLOCKQUOTE_END);
            quotedText.append(QUOTE_END);
            setQuotedText(quotedText);
        } else {
            setQuotedText(htmlText);
        }
        findViewById(R.id.divider_bar).setVisibility(GONE);
        findViewById(R.id.quoted_text_button_bar).setVisibility(GONE);
    }
    /**
     * Set quoted text. Some use cases may not want to display the check box (i.e. forwarding) so
     * allow control of that.
     */
    private void setQuotedText(CharSequence quotedText) {
        mQuotedText = quotedText;
        populateData();
        if (mRespondInlineButton != null) {
            if (!TextUtils.isEmpty(quotedText)) {
                mRespondInlineButton.setVisibility(View.VISIBLE);
                mRespondInlineButton.setEnabled(true);
                mRespondInlineButton.setOnClickListener(this);
            } else {
                // No text to copy; disable the respond inline button.
                mRespondInlineButton.setVisibility(View.GONE);
                mRespondInlineButton.setEnabled(false);
            }
        }
    }

    public static boolean containsQuotedText(String text) {
        int pos = text.indexOf(sQuoteBegin);
        return pos >= 0;
    }

    public static int getQuotedTextOffset(String text) {
        return text.indexOf(QuotedTextView.HEADER_SEPARATOR)
                + QuotedTextView.HEADER_SEPARATOR_LENGTH;
    }

    /**
     * Find the index of where the entire block of quoted text, quotes, divs,
     * attribution and all, begins.
     */
    public static int findQuotedTextIndex(CharSequence htmlText) {
        if (TextUtils.isEmpty(htmlText)) {
            return -1;
        }
        String textString = htmlText.toString();
        return textString.indexOf(sQuoteBegin);
    }

    public void setUpperDividerVisible(boolean visible) {
        findViewById(R.id.upper_quotedtext_divider_bar).setVisibility(
                visible ? View.VISIBLE : View.GONE);
    }
    
    //add by chenhl start 20141103
    private static final String EMAIL_TAG_BEGIN = " <";
    private static final String EMAIL_TAG_END = ">";
    private static final String EMAIL_HREF_BEGIN ="<a href=\"mailto:";
    private static final String EMAIL_HREF_END ="\">";
    private static final String EMAIL_SEP=";";
    
    private String transAddressText(Object addres){
 //   	MyLog.d("AuroraComposeActivity", "transAddressText:"+addres);
    	
    	return Address.decodeAddressName((String)addres);
    }
    
    private String transAddressText(String[] addres){
    	
    	String addString = "";
    	for(int i=0;i<addres.length;i++){
    		String temp = addres[i];
    		//MyLog.d("AuroraComposeActivity", "temp........1:"+temp);
    		final Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(temp);
    		if(i==addres.length-1||addres.length==1){
    			addString+=decodeAddress(tokens);
    		}else{
    			addString+=decodeAddress(tokens)+EMAIL_SEP;
    		}    		
    	}
    	//MyLog.d("AuroraComposeActivity", "transAddressText:"+addString);
    	return addString;
    }

	private String decodeAddress(Rfc822Token[] tokens) {
		if(tokens.length==0){
			return "";
		}
		String addString = "";		
		Rfc822Token token = tokens[0];
		String name=token.getName();
		
//		MyLog.d("AuroraComposeActivity", "decodeAddress name:"+name+" name size:"+name.length());
		if (name != null&&!TextUtils.isEmpty(name)&&!isSpace(name)) {
			addString=Address.decodeAddressName(name);
		} else {
			addString = getNameFromAddres(token.getAddress());
		}
		addString += EMAIL_TAG_BEGIN + EMAIL_HREF_BEGIN + token.getAddress()
				+ EMAIL_HREF_END + token.getAddress() + "</a>" + EMAIL_TAG_END;
//		MyLog.d("AuroraComposeActivity", "decodeAddress:" + addString);
		return addString;
	}
	
	private String getNameFromAddres(String addres){
		if(addres==null){
			return addres;
		}
		int index = addres.lastIndexOf("@");
		if(index!=-1){
			return addres.substring(0, index);
		}
		return addres;
	}
	
	/**
	 * 判断是否都是空格
	 * @param str
	 * @return
	 */
	private boolean isSpace(String str){
		
		if(str==null){
			return true;
		}
		
		for(int i=0;i<str.length();i++){
			if(!Character.isWhitespace(str.charAt(i))){
				return false;
			}
		}
		return true;
	}
	
    //add by chenhl end 20141103
}
