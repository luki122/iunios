/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.android.mms.ui;

import com.android.mms.MmsConfig;
import com.android.mms.R;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;

import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
// Aurora xuyong 2014-10-23 added for privacy feature start
import android.content.ContentValues;
// Aurora xuyong 2014-10-23 added for privacy feature end
import android.content.Context;
import android.content.DialogInterface;
import android.content.IClipboard;
import android.content.Intent;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Mms;
import com.android.mms.util.PhoneNumberUtils;
import android.text.Annotation;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
// Aurora xuyong 2013-10-11 added for aurora's new feature start
import android.util.DisplayMetrics;
// Aurora xuyong 2013-10-11 added for aurora's new feature end
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.MeasureSpec;
// Aurora xuyong 2013-10-17 added for aurora's new feature start 
import android.view.ViewGroup.LayoutParams;
// Aurora xuyong 2013-10-17 added for aurora's new feature end
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraAutoCompleteTextView;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aurora.featureoption.FeatureOption;
// Aurora xuyong 2015-02-11 added for bug #11672 start
import android.os.Bundle;
// Aurora xuyong 2015-02-11 added for bug #11672 end
import android.os.SystemProperties;

import aurora.app.AuroraAlertDialog;
import com.gionee.mms.ui.ContactsCacheSingleton;

import com.gionee.mms.ui.ContactsCacheSingleton.ContactListItemCache;
// Aurora xuyong 2014-10-23 added for privacy feature start
import com.privacymanage.service.AuroraPrivacyUtils;
// Aurora xuyong 2014-10-23 added for privacy feature end
import android.os.ServiceManager;
import com.android.mms.MmsApp;
//gionee zhouyj 2013-01-04 add for CR00678675 start 
import android.os.Handler;
import android.widget.ListPopupWindow;
import aurora.widget.AuroraListView;
import java.lang.reflect.Field;
//gionee zhouyj 2013-01-04 add for CR00678675 end 
// gionee zhouyj 2013-01-18 add for CR00765069 start 
import android.os.Message;
// gionee zhouyj 2013-01-18 add for CR00765069 end 
//Gionee <zhouyj> <2013-05-22> add for CR00818496 begin
import android.widget.Filterable;
//Gionee <zhouyj> <2013-05-22> add for CR00818496 end

public class ContactContainer extends ViewGroup implements View.OnClickListener ,
    View.OnFocusChangeListener, View.OnLongClickListener, View.OnTouchListener{

    //we want to have our own defined attribute
    private final String TAG = "ContactContainer";
    protected Paint mPaint;
    List<String> mNumberList;

    LayoutInflater mInflater;

    private int mVerticalInterval;
    private int mHorizonInterval;
    //Gionee <zhouyj> <2013-05-17> add for CR00812110 begin
    private String mNewNumber = null;
    //Gionee <zhouyj> <2013-05-17> add for CR00812110 end

    //AuroraEditText can't switch between single line and multiple line
    //So create freezeText member. when editText loses focus,freezeText shows up

    private AuroraAutoCompleteTextView mEditText;
    private TextView mFreezeText;
    // Aurora xuyong 2013-10-17 added for aurora's new feature start 
    private TextView mFreezeNailText;
    private LinearLayout lf;
    // Aurora xuyong 2013-10-17 added for aurora's new feature end
    
    //We need to show a tag
    // Aurora xuyong 2013-09-13 added for aurora's new feature start
    private TextView mContactTag;
    // Aurora xuyong 2013-10-17 added for aurora's new feature start 
    private LinearLayout lc;
    // Aurora xuyong 2013-10-17 added for aurora's new feature end
    // Aurora xuyong 2013-09-13 added for aurora's new feature end

    private int mWidthMeasureSpec  ;
    private int mHeightMeasureSpec ;

    private class ContactInfo{
        String displayName;
        String number;
        // Aurora xuyong 2013-06-19 added for aurora's new feature start
        boolean hasBeenSelected;
        // Aurora xuyong 2013-06-19 added for aurora's new feature end
        long contactId;
        // Aurora xuyong 2014-10-23 added for privacy feature start
        String privacy = "0"; // default value is 0
        // Aurora xuyong 2014-10-23 added for privacy feature end
    }

    HashMap<String, ContactInfo> mNumberMap = new HashMap<String, ContactInfo>();

    private ArrayList <ContactInfo> mContactInfoList = null;
    private int mCurTotalLines = 1;

//    private ContactItemToolBar mToolBar;
    private ContentResolver mCR;
    private static final String[] PROJECTION_PHONE = {
        Phone._ID,                  // 0
        Phone.NUMBER,               // 1
        Phone.DISPLAY_NAME,         // 2
        // Aurora xuyong 2014-10-23 added for privacy feature start
        "is_privacy"
        // Aurora xuyong 2014-10-23 added for privacy feature end
    };

    private final int INDEX_ID     = 0;
    private final int INDEX_NUMBER = 1;
    private final int INDEX_NAME   = 2;
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private final int INDEX_PRIVACY = 3;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private final int UNKNOW_ID = -1;
    private int MIN_CONTACT_AMOUNT = 1;

    private int mExternalScrollMaxHeight = 0;
    private int mMaxLines;
    private TextView mView;
    
    private final int CONTACT_DISPALY_MAX = 100;
    // gionee zhouyj 2013-01-04 add for CR00678675 start 
    private AuroraListView mDropListView = null;
    // gionee zhouyj 2013-01-04 add for CR00678675 end 
    // Aurora xuyong 2013-10-17 added for aurora's new feature start 
    private DisplayMetrics m;

    private Context mContext;
    
    private boolean isEnglishLanguage(){
        String laungue = Locale.getDefault().getLanguage();
        if (laungue != null
                && laungue.equals("en")) {
            return true;
        }
        return false;
    }
    
    // Aurora xuyong 2013-10-17 added for aurora's new feature end
    public ContactContainer(Context context, AttributeSet attrs) {
        super(context,attrs);
        // Aurora xuyong 2013-10-17 added for aurora's new feature start 
        mContext = context;
        // Aurora xuyong 2013-10-17 added for aurora's new feature end
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.container);//, defStyle, 0);
        // Aurora xuyong 2013-10-11 added for aurora's new feature start
        DisplayMetrics m = context.getResources().getDisplayMetrics();
        // Aurora xuyong 2013-10-17 added for aurora's new feature start 
        mHorizonInterval  = a.getDimensionPixelSize(R.styleable.container_cellHorizontalInterval, (int)m.density * 10);
        // Aurora xuyong 2013-12-16 added for aurora's new feature start
        mVerticalInterval = a.getDimensionPixelSize(R.styleable.container_cellVerticalInterval, (int)m.density * 13);
        // Aurora xuyong 2013-12-16 added for aurora's new feature end
        // Aurora xuyong 2013-10-17 added for aurora's new feature end
        // Aurora xuyong 2013-10-11 added for aurora's new feature end

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        if (isEnglishLanguage()) {
            lc = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_en_tag, this,false);
        } else {
            lc = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_tag, this,false);
        }
        mContactTag = (TextView)lc.findViewById(R.id.contact_tag);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        //Aurora xuyong 2013-09-20 added for aurora's new feature start
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        lc.setVisibility(View.VISIBLE);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        //Aurora xuyong 2013-09-20 added for aurora's new feature end
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        addView(lc);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        // Aurora xuyong 2013-09-13 added for aurora's new feature end

        mEditText = (AuroraAutoCompleteTextView) mInflater.inflate(R.layout.gn_contact_edit_text, this,false);
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        //mEditText.setHint(R.string.gn_to_hint);
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
        mEditText.addTextChangedListener(mEditTextWatcher);
        mEditText.setOnKeyListener(mEditTextKeyListener);
        // Aurora xuyong 2014-03-06 added for aurora's new feature start
        mEditText.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
            	// Aurora xuyong 2015-09-07 added for bug #16170 start
            	InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            	imm.showSoftInput(ContactContainer.this, 0);
            	// Aurora xuyong 2015-09-07 added for bug #16170 end
                mEditText.setCursorVisible(true);
                if (mContactInfoList != null) {
                    for (ContactInfo ci : mContactInfoList) {
                        ci.hasBeenSelected = false;
                    }
                }
                mIsDeleteMode = false;
                final int count = ContactContainer.this.getChildCount();
                for(int i=count - 1; i>=0; --i)
                {
                    View child = ContactContainer.this.getChildAt(i);
                    if(child != mEditText && child != lf && child != lc)
                    {
                        setSelectedState(child, false);
                    }
                    if (mNumberCache != null) {
                        mNumberCache.clear();
                    }
                }
            }
        });
        // Aurora xuyong 2014-03-06 added for aurora's new feature end
        mEditText.setOnFocusChangeListener(this);
        mEditText.setOnItemClickListener(mDropdownItemClick);
        mEditText.setVisibility(GONE);

        //gionee gaoj 2013-3-21 added for CR00787217 start
        // Aurora xuyong 2013-09-23 deleted for aurora's new feature start
        //mEditText.setTextColor(getResources().getColor(R.color.gn_msg_edit_text_color));
        // Aurora xuyong 2013-09-23 deleted for aurora's new feature end
        //gionee gaoj 2013-3-21 added for CR00787217 end
        addView(mEditText);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        lf = (LinearLayout)mInflater.inflate(R.layout.gn_contact_freeze_text, this,false);
        mFreezeText = (TextView) lf.findViewById(R.id.aurora_freeze_text);
        mFreezeNailText = (TextView) lf.findViewById(R.id.aurora_freeze_nail);
        lf.setOnTouchListener(this);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        //mFreezeText.setHint(R.string.gn_to_hint);
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end

        //gionee gaoj 2013-3-21 added for CR00787217 start
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        //mFreezeText.setTextColor(getResources().getColor(R.color.gn_msg_edit_text_color));
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
        //gionee gaoj 2013-3-21 added for CR00787217 end
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        addView(lf);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end

        mContactInfoList = new ArrayList <ContactInfo> ();
        mCR = context.getContentResolver();
        mEditText.setThreshold(1);
    }
    // Aurora xuyong 2013-11-13 added for aurora's new feature start
    Handler mNHandler;
    public static final int REFRESH = 222;
    public static final int LISTINVIS = 223;
    // Aurora xuyong 2015-03-04 added for bug #11831 start
    public static final int NOTIFY_FOCUS_CHANGE = 224;
    // Aurora xuyong 2015-03-04 added for bug #11831 end
    public void setHandler(Handler handler) {
        mNHandler = handler;
    }
    // Aurora xuyong 2013-11-13 added for aurora's new feature end
    /**
     * Called to remove some existed item which should be removed
     * @param number unique number
     */
    public void removeContact(String number)
    {
        int count =  getChildCount();
        //delete in opposite order
        while(--count >= 0)
        {
            View child = getChildAt(count);
            //Aurora xuyong 2013-09-20 modified for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            if(child != mEditText && child != lf && child != lc)
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            //Aurora xuyong 2013-09-20 modified for aurora's new feature end
            {
                // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                ContactInfo ci = (ContactInfo) child.findViewById(R.id.contact_item).getTag();
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                if(ci.number.equals(number))
                {
                    removeView(child);

                    mContactInfoList.remove(ci);
                    mNumberMap.remove(ci.number);

                    if(mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();

                    break;
                }
            }
        }
    }

    /**
     * Call to remove all items
     */
    public void reset()
    {
        int count =  getChildCount();

        //delete in opposite order
        while(--count >= 0)
        {
            View child = getChildAt(count);
            //Aurora xuyong 2013-09-20 added for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            if(child != mEditText && child != lf && child != lc)
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            //Aurora xuyong 2013-09-20 added for aurora's new feature end
            {
                removeView(child);
            }
        }

        //merge all name into a string
        if(!mContactInfoList.isEmpty())
        {
            mContactInfoList.clear();

            if(mContactsChangeListener != null)
                mContactsChangeListener.contactsChanged();
        }
    }
    /**
     * If drop down item is clicked,AutoCompleteText will first call textWatch and then call
     * this item click listener.
     * The perfect solution is we can get this item click first
     * then abandon calling text watch.
     * As we can't change this calling sequence,we have to process this fast
     * so that user can't feel the edit text changed twice
     */
    // Aurora xuyong 2013-11-13 added for aurora's new feature start
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    public void getInfo(TextView name, TextView number, TextView privacy) {
    // Aurora xuyong 2014-10-23 modified for privacy feature end
         ContactInfo ci = new ContactInfo();
         ci.hasBeenSelected = false;
         ci.displayName = name.getText().toString();
         if(MmsApp.mGnMessageSupport) {
             ci.number = number.getText().toString().replaceAll(" ", "");
         } else {
         ci.number = number.getText().toString();
         }
         // Aurora xuyong 2014-10-23 modified for privacy feature start
         ci.privacy = privacy.getText().toString();
         int result = hasExistsInContactList(ci.number, ci.privacy);
         if (result == NUMBER_AND_PRIVACY_EQUAL) {
         // Aurora xuyong 2014-10-23 modified for privacy feature end
             mEditText.setText(null);
             mEditText.setHint("");
             return;
         // Aurora xuyong 2014-10-23 added for privacy feature start
         } else if (result == ONLY_NUMBER_EQUAL){
             removeContact(ci.number);
         }
         // Aurora xuyong 2014-10-23 added for privacy feature end
         mContactInfoList.add(ci);
         mNumberMap.put(ci.number, ci);
         LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.gn_contact_item, ContactContainer.this, false);
         TextView tv = (TextView)ll.findViewById(R.id.contact_item);
         setSelectedState(ll, false);
         tv.setTag(ci);
         tv.setText(ci.displayName);
         tv.setOnClickListener(ContactContainer.this);
         mEditText.setText(null);
         addView(ll);
         mEditText.setHint("");

         if(mContactsChangeListener != null)
             mContactsChangeListener.contactsChanged();
    }
    // Aurora xuyong 2013-11-13 added for aurora's new feature end
    private OnItemClickListener mDropdownItemClick = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub

            if (view == null && parent != null) {
                view = parent.getChildAt(position);
            }
            if (view == null) {
                return;
            }
            
            TextView name = (TextView) view.findViewById(R.id.name);
            TextView number = (TextView) view.findViewById(R.id.number);
            // Aurora xuyong 2014-10-23 added for privacy feature start
            TextView privacy = (TextView) view.findViewById(R.id.aurora_privacy);
            // Aurora xuyong 2014-10-23 added for privacy feature end
            // Aurora xuyong 2013-11-13 modified for aurora's new feature start
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            if (MmsApp.sHasPrivacyFeature) {
                getInfo(name, number, privacy);
            } else {
                getInfo(name, number, privacy);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            // Aurora xuyong 2013-11-13 modified for aurora's new feature end
        }
    };
    
    // Aurora xuyong 2013-09-19 added for aurora's new feature start
    private ArrayList<String> mNumberCache = new ArrayList<String>();
    // Aurora xuyong 2014-03-06 modified for aurora's new feature start
    private void addToNumberCache(String number) {
        if (mNumberCache != null) {
            mNumberCache.add(number);
        }
    }
    
    private void revomeFromNumberCache(String number) {
        if (mNumberCache != null) {
            mNumberCache.remove(number);
        }
    }
    
    private int getSelectedNumber() {
        if (mNumberCache != null) {
            return mNumberCache.size();
        }
        return 0;
    }
    // Aurora xuyong 2014-03-06 modified for aurora's new feature start
    // Aurora xuyong 2013-09-19 added for aurora's new feature end
    // Aurora xuyong 2015-09-14 added for aurora's new feature start
    public interface BackKeyListener {
    	public void onBackKeyPressed();
    }
    
    private BackKeyListener mBackKeyListener;
    
    public void setBackKeyListener(BackKeyListener listener) {
    	mBackKeyListener = listener;
    }
    // Aurora xuyong 2015-09-14 added for aurora's new feature end
    private OnKeyListener mEditTextKeyListener = new OnKeyListener()
    {

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            // Aurora xuyong 2015-09-14 added for aurora's new feature start
        	if (keyCode == KeyEvent.KEYCODE_BACK) {
        		mBackKeyListener.onBackKeyPressed();
        	}
            // Aurora xuyong 2015-09-14 added for aurora's new feature end

            if ( keyCode == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                // Perform action on key press
                String inputText = mEditText.getText().toString().trim();

                if(!TextUtils.isEmpty(inputText))
                {
                    // Aurora xuyong 2014-02-13 modified for bug #11672 start
                    processEnterKey(inputText, PROCESS_ENTER_ONLY_TYPE);
                    // Aurora xuyong 2014-02-13 modified for bug #11672 end
                }

                //we abort this enter event,
                //so edit text won't start a new line if its content is empty
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_DEL
                    && event.getAction() == KeyEvent.ACTION_DOWN )
            {
                //text of edit view is empty,we delete other child
                if(mEditText.getText().length() == 0)
                {   
                    // Aurora xuyong 2013-09-16 modified for aurora's new feature start
                    if (getSelectedNumber() <= 0) {
                        final int count = getChildCount();

                        //we trace in opposite order
                        for(int i=count - 1; i>=0; --i)
                        {
                            View child = getChildAt(i);
                            //not edit view and must be visible
                            //Aurora xuyong 2013-09-20 modified for aurora's new feature start
                            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                            if(child != mEditText && child != lf && child != lc)
                            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                            //Aurora xuyong 2013-09-20 modified for aurora's new feature end
                            {
                                // Aurora xuyong 2014-03-06 added for aurora's new feature start
                                mEditText.setCursorVisible(false);
                                // Aurora xuyong 2014-03-06 added for aurora's new feature end
                                //make a fake click action
                                mIsDeleteMode = true;
                                // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                                child.findViewById(R.id.contact_item).performClick();
                                // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                                break;
                            }
                        }
                    } else {
                        for (String number : mNumberCache) {
                            removeContact(number);
                        }
                        mNumberCache.clear();
                        // Aurora xuyong 2014-03-06 added for aurora's new feature start
                        mEditText.setCursorVisible(true);
                        // Aurora xuyong 2014-03-06 added for aurora's new feature end
                    } 
                    // Aurora xuyong 2013-09-16 modified for aurora's new feature end
                    return true;
                }
            }

            return false;
        }
    };

    private TextWatcher mEditTextWatcher = new TextWatcher()
    {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Aurora xuyong 2014-03-06 added for aurora's new feature start
            mEditText.setCursorVisible(true);
            // Aurora xuyong 2014-03-07 modified for aurora's new feature start
            if (mNumberCache != null) {
                mNumberCache.clear();
            }
            // Aurora xuyong 2014-03-07 modified for aurora's new feature end
            // Aurora xuyong 2014-03-06 added for aurora's new feature end
            // TODO Auto-generated method stub
            // Aurora xuyong 2013-09-19 added for aurora's new feature start
            if (mContactInfoList != null) {
                for (ContactInfo ci : mContactInfoList) {
                    ci.hasBeenSelected = false;
                }
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount ; i++) {
                View child = getChildAt(i);
                //Aurora xuyong 2013-09-20 modified for aurora's new feature start
                // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                if (child != mEditText && child != lf && child != lc) {
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                //Aurora xuyong 2013-09-20 modified for aurora's new feature end
                    setSelectedState(child, false);
                }
            }
         // Aurora xuyong 2013-09-19 added for aurora's new feature end
            if (mClientEditTextWatcher != null) {
                mClientEditTextWatcher.beforeTextChanged(s, start, count, after);
            }
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

            Log.v(TAG, "this is onTextChanged");

            int oldWidth = mEditText.getMeasuredWidth();

            measureChild(mEditText, mWidthMeasureSpec, mHeightMeasureSpec);

            int newWidth = mEditText.getMeasuredWidth();

            //if text length is getting larger,we measure items again
            if(newWidth > oldWidth)
            {
                onMeasure(mWidthMeasureSpec, mHeightMeasureSpec);
            }

            if (mClientEditTextWatcher != null) {
                mClientEditTextWatcher.onTextChanged(s, start, before, count);
            }
        }

        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            // Aurora xuyong 2013-11-13 added for aurora's new feature start
            if (mNHandler != null) {
                Message msg = null;
                if (s.toString().length() <= 0) { 
                    msg = Message.obtain(mNHandler, LISTINVIS);
                } else {
                    msg = Message.obtain(mNHandler, REFRESH);
                    if (MmsApp.mGnMessageSupport) {
                        msg.obj = s;
                    }
                }
                msg.sendToTarget();
            }
            // Aurora xuyong 2013-11-13 added for aurora's new feature end
            char oldchar;
            if(s != null && s.length() >= 1){
                oldchar = s.charAt(s.length() - 1);
                Log.v(TAG, "oldchar: " + oldchar);
                if(oldchar == ',' || oldchar == ';' || oldchar == 0xff0c || oldchar == 0xff1b){
                  String inputText = mEditText.getText().toString();
                  inputText = inputText.substring(0, inputText.length() - 1).trim();

                  if(TextUtils.isEmpty(inputText)){
                      s.delete(s.length() - 1, s.length());
                  } else {
                      // Aurora xuyong 2014-02-13 modified for bug #11672 start
                      processEnterKey(inputText, PROCESS_ENTER_ONLY_TYPE);
                      // Aurora xuyong 2014-02-13 modified for bug #11672 end
                  }
                }
            }

            if (mClientEditTextWatcher != null) {
                mClientEditTextWatcher.afterTextChanged(s);
            }
            // gionee zhouyj 2013-01-18 modify for CR00765069 start 
            mHandler.sendMessageDelayed(new Message(), 3000);
            if (!mEditText.isPopupShowing()) {
                mDropListView = null;
            }
            // gionee zhouyj 2013-01-18 modify for CR00765069 end 
        }

    };
    
    // gionee zhouyj 2013-01-18 add for CR00765069 start
    // Aurora xuyong 2015-02-11 added for bug #11672 start
    private final int UPDATE_EXIST_EDIT_CONTACTS = 0x01;
    private final int UPDATE_NEW_EDIT_CONTACTS = 0x02;
    // Aurora xuyong 2014-02-13 added for bug #11672 start
    private final int FOCUS_CHANGED = 0x03;
    // Aurora xuyong 2014-02-13 added for bug #11672 end
    // Aurora xuyong 2015-02-11 added for bug #11672 end
    // Aurora xuyong 2015-08-04 added for bug #15274 start
    private boolean needAutoMatchTheFirst(Cursor cursor, long privacy) {
    	Log.e(TAG, "cursor.count = " + cursor.getCount());
    	if (cursor == null) {
    		return true;
    	}
    	int currentPositon = cursor.getPosition();
    	cursor.moveToPosition(-1);
    	Log.e(TAG, "-----------------------------");
    	List<String> nameList = new ArrayList<String>();
    	while(cursor.moveToNext()) {
    		Log.e(TAG, "privacy = " + cursor.getLong(INDEX_PRIVACY) );
    		if (cursor.getLong(INDEX_PRIVACY) == privacy) {
    			String number = cursor.getString(INDEX_NUMBER);
    			if (nameList.size() > 0) {
	    			for(String item : nameList) {
	    				if (item.indexOf(number) == -1 && number.indexOf(item) == -1) {
	    					nameList.add(number);
	    					break;
	    				}
	    			}
    			} else {
    				nameList.add(number);
    			}
    		}
    	}
    	Log.e(TAG, "-----------------------------");
    	cursor.moveToPosition(currentPositon);
    	Log.e(TAG, "privacyMatchCount = " + nameList.size());
    	if (nameList.size() == 1) {
    		return true;
    	} else {
    		return false;
    	}
    }
    // Aurora xuyong 2015-08-04 added for bug #15274 end
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            // Aurora xuyong 2015-02-11 modified for bug #11672 start
            switch (msg.what) {
                // Aurora xuyong 2014-02-13 added for bug #11672 start
                case FOCUS_CHANGED:
                    mEditText.setText(null);
                    mEditText.setHint("");
                  //hide all view except freeze text view
                    int count =  getChildCount();
                    //delete in opposite order
                    while(--count >= 0)
                    {
                        View child = getChildAt(count);
                        // Aurora xuyong 2013-09-17 modified for aurora's new feature start
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                        if(child == lf || child == lc)
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                        {
                            child.setVisibility(VISIBLE);
                        } else {
                            child.setVisibility(GONE);
                        }
                        // Aurora xuyong 2013-09-17 modified for aurora's new feature end
                    }

                    //merge all name into a string
                    if(mContactInfoList.isEmpty())
                    {
                        mFreezeText.setText(null);
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                        mFreezeNailText.setText(null);
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                    }
                    else
                    {
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                        //String finalString = formatFreezeText();
                        //mFreezeText.setText(Html.fromHtml(finalString));
                        updateFreezeAndNailText();
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                    }
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                    lf.setVisibility(VISIBLE);
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature end

                    if(mEditModeLsn != null)
                    {
                        mEditModeLsn.editModeChanged(false);
                    }
                    break;
                // Aurora xuyong 2014-02-13 added for bug #11672 end
                case UPDATE_EXIST_EDIT_CONTACTS:
                    // Aurora xuyong 2014-02-13 added for bug #11672 start
                    int typeE = msg.arg1;
                    // Aurora xuyong 2014-02-13 added for bug #11672 end
                    long currentAccountId = msg.getData().getLong("currentAccountId");
                    // Aurora xuyong 2015-08-04 modified for bug #15274 start
                    String inputE = msg.getData().getString("inputE");
                    String inputEN = msg.getData().getString("inputN");
                    // Aurora xuyong 2015-08-04 modified for bug #15274 end
                    Cursor result = (Cursor) msg.obj;
                    mEditText.setText(null);
                    mEditText.setHint("");
                    // Aurora xuyong 2015-08-04 added for bug #15274 start
                    long contactPravicy = result.getLong(INDEX_PRIVACY);
                    // Aurora xuyong 2015-08-04 added for bug #15274 end
                 // Aurora xuyong 2014-10-23 modified for privacy feature start
                    // Aurora xuyong 2015-08-04 modified for bug #15274 start
                    if (contactPravicy == currentAccountId && needAutoMatchTheFirst(result, contactPravicy)) {
                    // Aurora xuyong 2015-08-04 modified for bug #15274 end
                        // Aurora xuyong 2015-01-30 modified for bug #11468 start
                        addNewContact(result.getString(INDEX_NAME), inputE, result.getString(INDEX_PRIVACY));
                        // Aurora xuyong 2015-03-06 added for bug #12051 start
                        mIsAddingRecipient = false;
                        // Aurora xuyong 2015-03-06 added for bug #12051 end
                        insertOrUpdatePrivacy(inputE, result.getString(INDEX_PRIVACY));
                        // Aurora xuyong 2015-01-30 modified for bug #11468 end
                        result.close();
                        // Aurora xuyong 2014-02-13 added for bug #11672 start
                        if (typeE == FOUCUS_CHANGE_TYPE) {
                            Message msgN = this.obtainMessage(FOCUS_CHANGED);
                            msgN.sendToTarget();
                        }
                        // Aurora xuyong 2014-02-13 added for bug #11672 end
                        // Aurora xuyong 2015-03-04 added for bug #11831 start
                        if (mNeedNotifyClickSend) {
                            mNeedNotifyClickSend = false;
                            Message nMessage = mNHandler.obtainMessage(NOTIFY_FOCUS_CHANGE);
                            nMessage.obj = mClikedButton;
                            nMessage.arg2 = msg.arg2;
                            nMessage.sendToTarget();
                            mClikedButton = null;
                        }
                        // Aurora xuyong 2015-03-04 added for bug #11831 end
                        return;
                    }
                    while (result.moveToNext()) {
                        // Aurora xuyong 2015-08-04 modified for bug #15274 start
                    	contactPravicy = result.getLong(INDEX_PRIVACY);
                        if (contactPravicy == currentAccountId && needAutoMatchTheFirst(result, contactPravicy)) {
                        // Aurora xuyong 2015-08-04 modified for bug #15274 end
                            // Aurora xuyong 2015-01-30 modified for bug #11468 start
                            addNewContact(result.getString(INDEX_NAME), inputE, result.getString(INDEX_PRIVACY));
                            // Aurora xuyong 2015-03-06 added for bug #12051 start
                            mIsAddingRecipient = false;
                            // Aurora xuyong 2015-03-06 added for bug #12051 end
                            insertOrUpdatePrivacy(inputE, result.getString(INDEX_PRIVACY));
                            // Aurora xuyong 2015-01-30 modified for bug #11468 end
                            result.close();
                            // Aurora xuyong 2014-02-13 modified for bug #11672 start
                            if (typeE == FOUCUS_CHANGE_TYPE) {
                                Message msgN = this.obtainMessage(FOCUS_CHANGED);
                                msgN.sendToTarget();
                            }
                            // Aurora xuyong 2015-03-04 added for bug #11831 start
                            if (mNeedNotifyClickSend) {
                                mNeedNotifyClickSend = false;
                                Message nMessage = mNHandler.obtainMessage(NOTIFY_FOCUS_CHANGE);
                                nMessage.obj = mClikedButton;
                                nMessage.arg2 = msg.arg2;
                                nMessage.sendToTarget();
                                mClikedButton = null;
                            }
                            // Aurora xuyong 2015-03-04 added for bug #11831 end
                            return;
                            // Aurora xuyong 2014-02-13 modified for bug #11672 end
                        }
                    }
                    if (!result.isClosed() && result.moveToFirst()) {
                     // Aurora xuyong 2014-10-25 modified for privacy feature start
                        // Aurora xuyong 2015-08-04 modified for bug #15274 start
                    	contactPravicy = result.getLong(INDEX_PRIVACY);
                        if (contactPravicy > currentAccountId && needAutoMatchTheFirst(result, contactPravicy)) {
                        // Aurora xuyong 2015-08-04 modified for bug #15274 end
                        // Aurora xuyong 2014-10-25 added for privacy feature start
                            // Aurora xuyong 2015-01-30 modified for bug #11468 start
                            addNewContact(inputE, inputE, result.getString(INDEX_PRIVACY));
                            // Aurora xuyong 2015-03-06 added for bug #12051 start
                            mIsAddingRecipient = false;
                            // Aurora xuyong 2015-03-06 added for bug #12051 end
                            // Aurora xuyong 2015-01-30 modified for bug #11468 end
                        // Aurora xuyong 2014-10-25 added for privacy feature end
                     // Aurora xuyong 2014-10-25 modified for privacy feature end
                            // Aurora xuyong 2015-01-30 modified for bug #11468 start
                            insertOrUpdatePrivacy(inputE, result.getString(INDEX_PRIVACY));
                            // Aurora xuyong 2015-01-30 modified for bug #11468 end
                        } else {
                             // Aurora xuyong 2015-08-04 modified for bug #15274 start
                             addNewContact(inputEN, inputEN, "0");
                             // Aurora xuyong 2015-08-04 modified for bug #15274 end
                             // Aurora xuyong 2015-03-06 added for bug #12051 start
                             mIsAddingRecipient = false;
                             // Aurora xuyong 2015-03-06 added for bug #12051 end
                             // Aurora xuyong 2015-08-04 modified for bug #15274 start
                             insertOrUpdatePrivacy(inputEN, String.valueOf(0));
                             // Aurora xuyong 2015-08-04 modified for bug #15274 end
                        }
                    }
                    // Aurora xuyong 2014-02-13 added for bug #11672 start
                    if (typeE == FOUCUS_CHANGE_TYPE) {
                        Message msgN = this.obtainMessage(FOCUS_CHANGED);
                        msgN.sendToTarget();
                    }
                    // Aurora xuyong 2014-02-13 added for bug #11672 end
                    if (result != null && !result.isClosed()) {
                        result.close();
                    }
                    // Aurora xuyong 2015-03-04 added for bug #11831 start
                    if (mNeedNotifyClickSend) {
                        mNeedNotifyClickSend = false;
                        Message nMessage = mNHandler.obtainMessage(NOTIFY_FOCUS_CHANGE);
                        nMessage.obj = mClikedButton;
                        nMessage.arg2 = msg.arg2;
                        nMessage.sendToTarget();
                        mClikedButton = null;
                    }
                    // Aurora xuyong 2015-03-04 added for bug #11831 end
                    break;
                case UPDATE_NEW_EDIT_CONTACTS:
                    mEditText.setText(null);
                    mEditText.setHint("");
                    // Aurora xuyong 2014-02-13 added for bug #11672 start
                    int typeN = msg.arg1;
                    // Aurora xuyong 2014-02-13 added for bug #11672 end
                    // Aurora xuyong 2015-08-04 modified for bug #15274 start
                    String inputN = msg.getData().getString("inputN");
                    // Aurora xuyong 2015-08-04 modified for bug #15274 end
                    addNewContact(inputN, inputN, "0");
                    // Aurora xuyong 2015-03-06 added for bug #12051 start
                    mIsAddingRecipient = false;
                    // Aurora xuyong 2015-03-06 added for bug #12051 end
                    insertOrUpdatePrivacy(inputN, String.valueOf(0));
                    // Aurora xuyong 2014-02-13 added for bug #11672 start
                    if (typeN == FOUCUS_CHANGE_TYPE) {
                        Message msgN = this.obtainMessage(FOCUS_CHANGED);
                        msgN.sendToTarget();
                    }
                    // Aurora xuyong 2014-02-13 added for bug #11672 end
                    // Aurora xuyong 2015-03-04 added for bug #11831 start
                    if (mNeedNotifyClickSend) {
                        mNeedNotifyClickSend = false;
                        Message nMessage = mNHandler.obtainMessage(NOTIFY_FOCUS_CHANGE);
                        nMessage.obj = mClikedButton;
                        nMessage.arg2 = msg.arg2;
                        nMessage.sendToTarget();
                        mClikedButton = null;
                    }
                    // Aurora xuyong 2015-03-04 added for bug #11831 end
                    break;
                default:
                    if (mDropListView == null) {
                        try {
                            Field field = mEditText.getClass().getDeclaredField("mPopup");
                            if (field != null) {
                                field.setAccessible(true);
                                ListPopupWindow listPop = (ListPopupWindow) field.get(mEditText);
                                Field f = listPop.getClass().getDeclaredField("mDropDownList");
                                if (f != null) {
                                    f.setAccessible(true);
                                    mDropListView = (AuroraListView) f.get(listPop);
                                    if (mDropListView != null) {
                                        mDropListView.setOnTouchListener(new View.OnTouchListener() {
                                            
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                // TODO Auto-generated method stub
                                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                                    hideSoftKeyboard();
                                                }
                                                return false;
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // TODO: handle exception
                            Log.e(TAG, e.toString());
                        }
                    }
            }
            // Aurora xuyong 2015-02-11 modified for bug #11672 end
        };
    };
    // gionee zhouyj 2013-01-18 add for CR00765069 end 
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private long mCurrentThreadId = -1;
    private final Uri sPrivacyUri = Uri.parse("content://mms-sms/privacy-account");
    public void setCurrentThreadId(long threadId) {
        mCurrentThreadId = threadId;
        ContentValues values = new ContentValues();
        // syn update the value of the column
        values.put("cur_opt_thread_id", mCurrentThreadId);
        mContext.getContentResolver().update(sPrivacyUri, values, null, null);
    }
    
    public void insertOrUpdatePrivacy(String number, String privacy) {
        // Aurora xuyong 2014-10-30 modified for privacy feature start
        if (MmsApp.sHasPrivacyFeature && (isValidAddress(number, false))) {
            // Aurora xuyong 2014-12-10 added for bug #9503 start
            number = number.replaceAll("#", "*");
            number = number.replaceAll("\\.", "*");
            number = number.replaceAll("\\*", "/*");
            if (number.charAt(0) == '+') {
                number = number.replaceAll("\\+", "");
                number = "+" + number;
            } else {
                number = number.replaceAll("\\+", "");
            }
            // Aurora xuyong 2014-12-10 added for bug #9503 end
            // Aurora xuyong 2014-11-26 modified for bug #9503 start
            // Aurora xuyong 2014-12-10 modified for bug #9503 start
            Cursor cursor = mContext.getContentResolver().query(sPrivacyUri, new String[] { "number" }, "number LIKE ? ESCAPE '/' AND status = 1", new String[]{ number }, null);
            // Aurora xuyong 2014-12-10 modified for bug #9503 end
            // Aurora xuyong 2014-11-26 modified for bug #9503 end
            if (cursor != null && cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put("is_privacy", privacy);
                // Aurora xuyong 2014-11-26 modified for bug #9503 start
                // Aurora xuyong 2014-12-10 modified for bug #9503 start
                mContext.getContentResolver().update(sPrivacyUri, values, "number LIKE ? ESCAPE '/'", new String[]{ number });
                // Aurora xuyong 2014-12-10 modified for bug #9503 end
                // Aurora xuyong 2014-11-26 modified for bug #9503 end
                cursor.close();
            } else {
                ContentValues insertValues = new ContentValues(); 
                insertValues.put("is_privacy", privacy); 
                insertValues.put("cur_opt_thread_id", mCurrentThreadId); 
                insertValues.put("thread_id", mCurrentThreadId); 
                insertValues.put("status", 1); 
                insertValues.put("number", number); 
                mContext.getContentResolver().insert(sPrivacyUri, insertValues);
            }
       }
       // Aurora xuyong 2014-10-30 modified for privacy feature end
    }
    // Aurora xuyong 2015-02-11 added for bug #11672 start
    // Aurora xuyong 2014-02-13 added for bug #11672 start
    public static final int PROCESS_ENTER_ONLY_TYPE = 1;
    public static final int FOUCUS_CHANGE_TYPE      = 2;
    // Aurora xuyong 2014-02-13 added for bug #11672 end
    // Aurora xuyong 2014-02-13 modified for bug #11672 start
    public void processEnterKey(final String input, final int type) {
    // Aurora xuyong 2014-02-13 modified for bug #11672 end
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // Aurora xuyong 2014-02-13 modified for bug #11672 start
                process(input, type);
                // Aurora xuyong 2014-02-13 modified for bug #11672 end
            }
            
        }).start();
    }
    // Aurora xuyong 2015-03-06 modified for bug #12051 start
    public boolean isFocused() {
        return mEditText.isFocused() && mEditText.getText().length() > 0;
    }
    // Aurora xuyong 2015-03-06 modified for bug #12051 end
    // Aurora xuyong 2015-03-04 modified for bug #11831 start
    public void processEnterKey(final String input, final int type, final int backType) {
        // Aurora xuyong 2015-03-06 modified for bug #12051 start
        mIsAddingRecipient = true;
        // Aurora xuyong 2015-03-06 modified for bug #12051 end
    // Aurora xuyong 2015-03-04 modified for bug #11831 end
        // Aurora xuyong 2014-02-13 modified for bug #11672 end
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // Aurora xuyong 2014-02-13 modified for bug #11672 start
                    // Aurora xuyong 2015-03-04 modified for bug #11831 start
                    process(input, type, backType);
                    // Aurora xuyong 2015-03-04 modified for bug #11831 end
                    // Aurora xuyong 2014-02-13 modified for bug #11672 end
                }
                
            }).start();
        }
    // Aurora xuyong 2015-02-11 added for bug #11672 end
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2015-03-04 added for bug #11831 start
    public void process(String input, int type) {
        process(input, type, -1);
    }
    // Aurora xuyong 2015-03-04 added for bug #11831 end
    // Aurora xuyong 2015-02-11 modified for bug #11672 start
    // Aurora xuyong 2014-02-13 modified for bug #11672 start
    // Aurora xuyong 2015-03-04 modified for bug #11831 start
    public void process(String input, int type, int backtype)
    // Aurora xuyong 2015-03-04 modified for bug #11831 end
    // Aurora xuyong 2014-02-13 modified for bug #11672 end
    {
        // Aurora xuyong 2015-05-25 added for aurora's new feature start
    	String inputCopy = new String(input);
        // Aurora xuyong 2015-05-25 added for aurora's new feature end
        //We don't know what type of user input.as even input text is pure digit ,it
        //also can be a contact display name
        //so we query twice,first assume input is name,if it doesn't exist
        //we query it as a number again

        //Assume input is a name
      // Aurora xuyong 2014-10-23 modified for privacy feature start
       String extraSelection = null;
       long currentAccountId = AuroraPrivacyUtils.getCurrentAccountId();
       if (MmsApp.sHasPrivacyFeature) {
               extraSelection = " AND is_privacy >= 0";
       }
       // Aurora xuyong 2015-07-06 added for bug #14043 start
       if (input != null) {
    	   input = input.replace("'", "''");
       }
       // Aurora xuyong 2015-07-06 added for bug #14043 end
        // Aurora xuyong 2014-11-04 modified for bug #9633 start
        // Aurora  xuyong 2014-11-21 modified for bug #9954 start
        // Aurora xuyong 2014-12-16 modified for bug #10602 start
        // Aurora xuyong 2015-01-30 modified for bug #11468 start
        // Aurora xuyong 2015-05-25 modified for aurora's new feature start
        Cursor cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.DISPLAY_NAME + " = '" + input + "'" + extraSelection, null, " is_privacy desc ");
        // Aurora xuyong 2015-05-25 modified for aurora's new feature end
        // Aurora xuyong 2015-01-30 modified for bug #11468 end
        // Aurora xuyong 2014-12-16 modified for bug #10602 end
        // Aurora  xuyong 2014-11-21 modified for bug #9954 end
        // Aurora xuyong 2014-11-04 modified for bug #9633 end
       // Aurora xuyong 2014-10-23 modified for privacy feature end

        //no record exits,so assume it as a number and query again
        if(cs == null || cs.getCount() == 0)
        {
            //gionee gaoj 2012-8-14 added for CR00623375 start
            if (MmsApp.mGnRegularlyMsgSend) {
                if (cs != null) {
                    cs.close();
                }
            }
            //gionee gaoj 2012-8-14 added for CR00623375 start
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            // Aurora xuyong 2014-11-04 modified for bug #9633 start
            // Aurora  xuyong 2014-11-21 modified for bug #9954 start
            // Aurora xuyong 2014-12-16 modified for bug #10602 start
            // Aurora xuyong 2015-01-30 modified for bug #11468 start
            // Aurora xuyong 2015-05-25 modified for aurora's new feature start
            input = PhoneNumberUtils.getCallerIDMinMacth(input);
            // Aurora xuyong 2015-06-30 modified for bug #13952 start
            if (input != null && input.length() > 0) {
            	cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.NUMBER + " LIKE '%" + input + "' "+ extraSelection, null, " is_privacy desc ");
            } else {
            	// we pretend to find nothing here if the number string is null.
            	cs = null;
            }
            // Aurora xuyong 2015-06-30 modified for bug #13952 end
            // Aurora xuyong 2015-05-25 modified for aurora's new feature end
            // Aurora xuyong 2015-01-30 modified for bug #11468 end
            // Aurora xuyong 2014-12-16 modified for bug #10602 end
            // Aurora  xuyong 2014-11-21 modified for bug #9954 end
            // Aurora xuyong 2014-11-04 modified for bug #9633 end
            // Aurora xuyong 2014-10-23 modified for privacy feature end
        }
        //Gionee <zhouyj> <2013-05-21> modify for CR00792405 begin
        //mEditText.setText(null);
        //Gionee <guoyx> <2013-07-23> add for CR00838431 begin
        //mEditText.setHint("");
        //Gionee <guoyx> <2013-07-23> add for CR00838431 end
        Bundle idAndInputData = new Bundle();
        idAndInputData.putLong("currentAccountId", currentAccountId);
        // Aurora xuyong 2015-08-04 modified for bug #15274 start
        idAndInputData.putString("inputN", input);
        if(cs != null && cs.moveToFirst())
        // Aurora xuyong 2015-08-04 modified for bug #15274 end
        {
            // Aurora xuyong 2015-05-25 added for aurora's new feature start
            // Aurora xuyong 2015-08-04 modified for bug #15274 start
        	idAndInputData.putString("inputE", cs.getString(INDEX_NUMBER));
            // Aurora xuyong 2015-08-04 modified for bug #15274 end
            // Aurora xuyong 2015-05-25 added for aurora's new feature end
            Message msg = mHandler.obtainMessage(UPDATE_EXIST_EDIT_CONTACTS);
            msg.obj = cs;
            // Aurora xuyong 2014-02-13 added for bug #11672 start
            msg.arg1 = type;
            // Aurora xuyong 2015-03-04 added for bug #11831 start
            msg.arg2 = backtype;
            // Aurora xuyong 2015-03-04 added for bug #11831 end
            // Aurora xuyong 2014-02-13 added for bug #11672 end
            msg.setData(idAndInputData);
            msg.sendToTarget();
        } else {
            // Aurora xuyong 2015-05-25 added for aurora's new feature start
            // Aurora xuyong 2015-05-25 added for aurora's new feature start
            // Aurora xuyong 2015-08-04 modified for bug #15274 start
            idAndInputData.putString("inputN", inputCopy);
            // Aurora xuyong 2015-08-04 modified for bug #15274 end
            // Aurora xuyong 2015-05-25 added for aurora's new feature end
            // Aurora xuyong 2015-05-25 added for aurora's new feature end
            Message msg = mHandler.obtainMessage(UPDATE_NEW_EDIT_CONTACTS);
            msg.obj = cs;
            // Aurora xuyong 2014-02-13 added for bug #11672 start
            msg.arg1 = type;
            // Aurora xuyong 2015-03-04 added for bug #11831 start
            msg.arg2 = backtype;
            // Aurora xuyong 2015-03-04 added for bug #11831 end
            // Aurora xuyong 2014-02-13 added for bug #11672 end
            msg.setData(idAndInputData);
            msg.sendToTarget();
        }
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        //Gionee <zhouyj> <2013-05-21> modify for CR00792405 end
        //gionee gaoj 2012-7-5 added for CR00628364 start
        //if (cs != null) {
        //    cs.close();
        //}
        //gionee gaoj 2012-7-5 added for CR00628364 end
    }
    // Aurora xuyong 2015-02-11 modified for bug #11672 end

    public boolean hasInvalidRecipient(boolean isMms)
    {
        for (ContactInfo ci: mContactInfoList)
        {
               /*Gionee liuxiangrong 2012-08-21 modify for CR00679211 start*/
            ci.number=ci.number.replace(" ","");
               /*Gionee lixiaohu 2012-10-23 modify for CR00717603 start*/
            ci.number=ci.number.replace("-","");
               /*Gionee lixiaohu 2012-10-23 modify for CR00717603 end*/
            /*Gionee liuxiangrong 2012-08-21 modify for CR00679211 end*/        
            if (!isValidAddress(ci.number, isMms)) {
                if (MmsConfig.getEmailGateway() == null) {
                    return true;
                } else if (!MessageUtils.isAlias(ci.number)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidAddress(String number, boolean isMms) {
        if (isMms) {
            return MessageUtils.isValidMmsAddress(number);
        } else {
            // TODO: PhoneNumberUtils.isWellFormedSmsAddress() only check if the
            // number is a valid
            // GSM SMS address. If the address contains a dialable char, it
            // considers it a well
            // formed SMS addr. CDMA doesn't work that way and has a different
            // parser for SMS
            // address (see CdmaSmsAddress.parse(String address)). We should
            // definitely fix this!!!
            return PhoneNumberUtils.isWellFormedSmsAddress(number)
                    || Mms.isEmailAddress(number);
        }
    }

    public boolean hasValidRecipient(boolean isMms)
    {
        for (ContactInfo ci: mContactInfoList)
        {
            if (isValidAddress(ci.number, isMms))
                return true;
        }
        return false;
    }

    public int getRecipientCount()
    {
        return mContactInfoList.isEmpty()? 0 : mContactInfoList.size();
    }
    // Aurora xuyong 2015-03-06 modified for bug #12051 start
    private boolean mIsAddingRecipient = false;
    
    public void setAddingRecipient(boolean param) {
        mIsAddingRecipient = param;
    }
    
    public boolean  isAddingRecipient() {
        return mIsAddingRecipient;
    }
    // Aurora xuyong 2015-03-06 modified for bug #12051 end

    public void setNumbers(List<String> numberList) {
        this.mNumberList = numberList;
        //mTokenizer.setNumbers(numberList);
    }

    public String formatInvalidNumbers(boolean isMms) {

        StringBuilder sb = new StringBuilder();

        for (ContactInfo ci : mContactInfoList)
        {
            if (!isValidAddress(ci.number, isMms)) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(ci.number);
            }
        }
        return sb.toString();
    }

    public boolean containsEmail() {

        for (ContactInfo ci : mContactInfoList)
        {
            if (Mms.isEmailAddress(ci.number))
            {
                return true;
            }
        }
        return false;
    }

    /*
     * return the count of contacts
     * */
    public int getContactsCount(){
        return mContactInfoList.size();
    }

    public List<String> getNumbers() {
        List<String> numberList = new ArrayList<String>();

        for (ContactInfo ci : mContactInfoList)
        {
            numberList.add(ci.number);
        }

        return numberList;
    }
    // Aurora xuyong 2015-03-17 added for bug #12275 start
    public List<String> getNumbersAndPrivacyWith1C() {
        List<String> numberList = new ArrayList<String>();

        for (ContactInfo ci : mContactInfoList)
        {
            numberList.add(ci.number + String.valueOf('\1') + ci.privacy);
        }

        return numberList;
    }
    // Aurora xuyong 2015-03-17 added for bug #12275 end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public List<String> getNumbersAndPrivacy() {
        List<String> numberList = new ArrayList<String>();

        for (ContactInfo ci : mContactInfoList)
        {
            numberList.add(ci.number + ":" + ci.privacy);
        }

        return numberList;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    protected boolean structLastRecipient() {
//        return super.structLastRecipient();
        return true;
    }


    /**
     * Called to add new input to contact list without creating new view
     * this situation occurs when edit text has lost focus,and it got content
     * we assume that user finished input action,make it as a contact
     * @param input
     */
    private void addNewContact(String input)
    {
        // Aurora xuyong 2015-05-25 added for aurora's new feature start
    	String inputCopy = new String(input);
        // Aurora xuyong 2015-05-25 added for aurora's new feature end
        // Aurora xuyong 2014-11-04 modified for bug #9633 start
        // Aurora xuyong 2015-05-25 modified for aurora's new feature start
        // Aurora xuyong 2015-07-06 added for bug #14043 start
    	if (input != null) {
     	   input = input.replace("'", "''");
        }
        // Aurora xuyong 2015-07-06 added for bug #14043 end
        Cursor cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.DISPLAY_NAME + " = '" + input + "'", null, null);
        // Aurora xuyong 2015-05-25 modified for aurora's new feature end
        // Aurora xuyong 2014-11-04 modified for bug #9633 end
        //no record exits,so assume it as a number and query again
        if(cs == null || cs.getCount() == 0)
        {
            //gionee gaoj 2012-7-20 added for CR00647257 start
            if (cs != null) {
                cs.close();
            }
            // Aurora xuyong 2015-05-25 modified for aurora's new feature start
            input = PhoneNumberUtils.getCallerIDMinMacth(input);
            // Aurora xuyong 2015-05-25 modified for aurora's new feature end
            //gionee gaoj 2012-7-20 added for CR00647257 end
            // Aurora xuyong 2014-11-04 modified for bug #9633 start
            // Aurora xuyong 2015-05-25 modified for aurora's new feature start
            // Aurora xuyong 2015-06-30 modified for bug #13952 start
            if (input != null && input.length() > 0) {
                cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.NUMBER + " LIKE '%" + input + "'", null, null);
            } else {
            	// we pretend to find nothing here if the number string is null.
            	cs = null;
            }
            // Aurora xuyong 2015-06-30 modified for bug #13952 end
            // Aurora xuyong 2015-05-25 modified for aurora's new feature end
            // Aurora xuyong 2014-11-04 modified for bug #9633 end
        }

        ContactInfo ci = new ContactInfo();
        // Aurora xuyong 2013-09-16 added for aurora's new feature start
        ci.hasBeenSelected = false;
        // Aurora xuyong 2013-09-16 added for aurora's new feature end

        if(cs != null && cs.moveToFirst())
        {
            ci.displayName = cs.getString(INDEX_NAME);
            ci.number = cs.getString(INDEX_NUMBER);
        }
        else
        {
            // Aurora xuyong 2015-05-25 modified for aurora's new feature start
            ci.displayName = inputCopy;
            ci.number = inputCopy;
            // Aurora xuyong 2015-05-25 modified for aurora's new feature end
        }

        cs.close();
        // Aurora xuyong 2013-09-16 added for aurora's new feature start
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        ci.privacy = "0";
        int result = hasExistsInContactList(ci.number, ci.privacy);
        if (result == NUMBER_AND_PRIVACY_EQUAL) {
            return;
        } else if (result == ONLY_NUMBER_EQUAL){
               removeContact(ci.number);
        }
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        // Aurora xuyong 2013-09-16 added for aurora's new feature end
        mContactInfoList.add(ci);
        mNumberMap.put(ci.number, ci);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.gn_contact_item, ContactContainer.this, false);
        TextView tv = (TextView)ll.findViewById(R.id.contact_item);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        tv.setTag(ci);
        tv.setText(ci.displayName);
        tv.setOnClickListener(ContactContainer.this);
//        tv.setOnCreateContextMenuListener(mContactListListener);
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature start
        //tv.setOnLongClickListener(onTextViewLongClickListener);
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature end
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        addView(ll);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end

        if(mContactsChangeListener != null)
            mContactsChangeListener.contactsChanged();
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public void addNewContact(String name, String number, String privacy) {
         ContactInfo ci = new ContactInfo();
         ci.hasBeenSelected = false;
         ci.displayName = name;
         ci.number = number;
         ci.privacy = privacy;
         int result = hasExistsInContactList(ci.number, ci.privacy);
         if (result == NUMBER_AND_PRIVACY_EQUAL) {
             return;
         } else if (result == ONLY_NUMBER_EQUAL){
             removeContact(ci.number);
         }
         mContactInfoList.add(ci);
         mNumberMap.put(ci.number, ci);
         LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.gn_contact_item, ContactContainer.this, false);
         TextView tv = (TextView)ll.findViewById(R.id.contact_item);
         setSelectedState(ll, false);
         tv.setTag(ci);
         tv.setText(ci.displayName);
         tv.setOnClickListener(ContactContainer.this);
         addView(ll);

         if(mContactsChangeListener != null)
             mContactsChangeListener.contactsChanged();
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    public void addNewContact(String name, String number)
    {
        ContactInfo ci = new ContactInfo();
        // Aurora xuyong 2013-09-16 added for aurora's new feature start
        ci.hasBeenSelected = false;
        // Aurora xuyong 2013-09-16 added for aurora's new feature end
        ci.displayName = name;
        ci.number = number;
        
        // Aurora xuyong 2013-09-16 added for aurora's new feature start
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        ci.privacy = "0";
        int result = hasExistsInContactList(ci.number, ci.privacy);
        if (result == NUMBER_AND_PRIVACY_EQUAL) {
            return;
        } else if (result == ONLY_NUMBER_EQUAL){
            removeContact(ci.number);
        }
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        // Aurora xuyong 2013-09-16 added for aurora's new feature end
        mContactInfoList.add(ci);
        mNumberMap.put(ci.number, ci);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.gn_contact_item, ContactContainer.this, false);
        TextView tv = (TextView)ll.findViewById(R.id.contact_item);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        setSelectedState(ll, false);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
        tv.setTag(ci);
        tv.setText(ci.displayName);
        tv.setOnClickListener(ContactContainer.this);
//        tv.setOnCreateContextMenuListener(mContactListListener);
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature start
        //tv.setOnLongClickListener(onTextViewLongClickListener);
        // Aurora xuyong 2013-10-24 deleted for aurora's new feature end
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        addView(ll);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end

        if(mContactsChangeListener != null)
            mContactsChangeListener.contactsChanged();
    }
    
    /**
     * Called to add new contact by contact list for batch process
     * @param list original contact list
     */
    public void addNewContact(ContactList list)
    {
        //We add contact directly rather than calling inner method such as addNewContact
        //as this method will call listener every single time.so after we finished contact adding
        //then call listener once

        for (Contact c : list) {

            //Instead of calling addNewContact method,create and add ContactInfo directly
            ContactInfo ci = new ContactInfo();
            // Aurora xuyong 2013-09-16 added for aurora's new feature start
            ci.hasBeenSelected = false;
            // Aurora xuyong 2013-09-16 added for aurora's new feature end
            ci.displayName = c.getName();
            ci.number = c.getNumber();
            // Aurora xuyong 2013-09-16 added for aurora's new feature start
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            ci.privacy = String.valueOf(c.getPrivacy());
            int result = hasExistsInContactList(ci.number, ci.privacy);
            if (result == NUMBER_AND_PRIVACY_EQUAL) {
                continue;
            } else if (result == ONLY_NUMBER_EQUAL){
                   removeContact(ci.number);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            // Aurora xuyong 2013-09-16 added for aurora's new feature end
            mContactInfoList.add(ci);
            mNumberMap.put(ci.number, ci);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.gn_contact_item, ContactContainer.this, false);
            TextView tv = (TextView)ll.findViewById(R.id.contact_item);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            // Aurora xuyong 2013-09-13 added for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            setSelectedState(ll, false);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            // Aurora xuyong 2013-09-13 added for aurora's new feature end
            tv.setTag(ci);
            tv.setText(ci.displayName);
            tv.setOnClickListener(ContactContainer.this);
//            tv.setOnCreateContextMenuListener(mContactListListener);
            // Aurora xuyong 2013-10-24 deleted for aurora's new feature start
            //tv.setOnLongClickListener(onTextViewLongClickListener);
            // Aurora xuyong 2013-10-24 deleted for aurora's new feature end
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            addView(ll);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }

        if(mContactsChangeListener != null)
            mContactsChangeListener.contactsChanged();
    }



    public String allNumberToString()
    {
        StringBuffer sb = new StringBuffer();

        for (ContactInfo ci : mContactInfoList)
        {
            sb.append(ci.number);
            //Gionee <zhouyj> <2013-05-08> modify for CR00801051 begin
            sb.append(";");
            //Gionee <zhouyj> <2013-05-08> modify for CR00801051 end
        }

        String allNumberString = sb.toString();

        if (!TextUtils.isEmpty(allNumberString)) {
            allNumberString = allNumberString.substring(0, allNumberString.length()-1);
        }
        return allNumberString;
    }

    public ContactList constructContactsFromInput(boolean blocking) {
        ContactList list = new ContactList();

        for (ContactInfo ci : mContactInfoList)
        {
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            Contact contact = null;
            if (MmsApp.sHasPrivacyFeature) {
                   contact = Contact.get(ci.number, blocking, Long.parseLong(ci.privacy));
            } else {
                contact = Contact.get(ci.number, blocking);
            }
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            contact.setNumber(ci.number);
            list.add(contact);
        }

        return list;
    }

    public boolean isDuplicateNumber(String number) {
        if (mNumberMap.containsKey(number)) {
            return true;
        }

        return false;
    }
    // Aurora xuyong 2013-10-17 added for aurora's new feature start 
    private void updateFreezeAndNailText() {
        StringBuffer sb = new StringBuffer();
        final int size = mContactInfoList.size();
        if (size > CONTACT_DISPALY_MAX ) {
            ContactInfo it = null;
             for(int i = 0; i < CONTACT_DISPALY_MAX; i++){
                 it = mContactInfoList.get(i);
                 sb.append(it.displayName);
                 // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                 sb.append("、");
                 // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }
        }
        else{
        for( ContactInfo it : mContactInfoList) {
            sb.append(it.displayName);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            sb.append("、");
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }
        }
        sb.deleteCharAt(sb.length()-1);
        

        String mergeString = sb.toString();
        if (mergeString.getBytes().length > 20) {
            // Aurora xuyong 2013-10-18 modified for aurora's new feature start
            if (isEnglishLanguage()) {
                mFreezeText.setMaxWidth((int)(130 * mContext.getResources().getDisplayMetrics().density));
            } else {
                mFreezeText.setMaxWidth((int)(110 * mContext.getResources().getDisplayMetrics().density));
            }
            // Aurora xuyong 2013-10-18 modified for aurora's new feature end
            // Aurora xuyong 2013-10-18 modified for aurora's new feature start
            if (mContactInfoList.size() > 0) {
                // Aurora xuyong 2013-10-18 modified for aurora's new feature start
                mFreezeText.setText(mergeString);
                // Aurora xuyong 2013-10-18 modified for aurora's new feature end
                mFreezeText.setText(mContactInfoList.get(0).displayName);
                // Aurora xuyong 2013-10-18 modified for aurora's new feature start
                int rs = mContactInfoList.size() - 1;
                if (rs > 0) {
                // Aurora xuyong 2013-10-18 modified for aurora's new feature end
                    mFreezeNailText.setText(mContext.getResources().getString(R.string.aurora_recipients_number, mContactInfoList.size() - 1));
                    mFreezeNailText.setVisibility(VISIBLE);
                // Aurora xuyong 2013-10-18 modified for aurora's new feature start
                } else {
                    mFreezeText.setMaxWidth((int)(200 * mContext.getResources().getDisplayMetrics().density));
                    mFreezeNailText.setVisibility(GONE);
                }
                // Aurora xuyong 2013-10-18 modified for aurora's new feature end
            }
            // Aurora xuyong 2013-10-18 modified for aurora's new feature end
        } else {
            mFreezeText.setMaxWidth((int)(200 * mContext.getResources().getDisplayMetrics().density));
            mFreezeText.setText(mergeString);
            mFreezeNailText.setVisibility(GONE);
        }
    }
    // Aurora xuyong 2013-10-17 added for aurora's new feature end 
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        if(lf.getVisibility() == VISIBLE)
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        {
            final int parentHeight = getMeasuredHeight();
            final int parentWidth = getMeasuredWidth();

            final int mostRightPos = parentWidth - mPaddingLeft;

            if (!mContactInfoList.isEmpty()) {
                // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                //String finalString = formatFreezeText();
                //mFreezeText.setText(Html.fromHtml(finalString));
                updateFreezeAndNailText();
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            }

            //place it in vertical center
            // Aurora xuyong 2013-10-11 modified for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            int curY = (parentHeight - lf.getMeasuredHeight() - mPaddingBottom - 1) >> 1;
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            //Aurora xuyong 2013-09-20 modified for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start
            // Aurora xuyong 2013-10-25 added for bug 220 start
            lc.layout(mPaddingLeft, mPaddingTop ,  Math.min(mPaddingLeft + lc.getMeasuredWidth(), mostRightPos), curY + lc.getMeasuredHeight());
            // Aurora xuyong 2013-10-25 added for bug 220 end
            // Aurora xuyong 2013-11-20 modified for aurora's new feature start
            lf.layout(mPaddingLeft - 12 + lc.getMeasuredWidth() + mHorizonInterval, curY + 5, mostRightPos, parentHeight - curY);
            // Aurora xuyong 2013-11-20 modified for aurora's new feature end
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            // Aurora xuyong 2013-10-11 modified for aurora's new feature end
            //Aurora xuyong 2013-09-20 modified for aurora's new feature end
        }
        else
        {
            final int parentWidth = getMeasuredWidth();
            final int parentHeight = getMeasuredHeight();

            final int mostRightPos = parentWidth - mPaddingLeft;
            final int childMostWidth = mostRightPos - mPaddingRight;

            int curX = mPaddingLeft;
            int curY = mPaddingTop;
            int childWidth = 0;
            int childHeight = 0;
            int lastChildHeght = 0;

            final int count = getChildCount();

            //we got text view kids except edit text and freeze text
            if(count > 2)
            {
                for (int i=0; i<count; ++i)
                {
                    final View child = getChildAt(i);
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                    if (child != mEditText && child != lf)
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                    {
                        childWidth  = child.getMeasuredWidth();
                        childHeight = child.getMeasuredHeight();

                        //we won't allow child wider than max width
                        childWidth = Math.min(childWidth, childMostWidth);

                        if(curX + childWidth > mostRightPos)
                        {
                            curX = mPaddingLeft;
                            curY += childHeight + mVerticalInterval;
                        }

                        int rightPos = Math.min(curX + childWidth, mostRightPos);
                        // Aurora xuyong 2013-11-20 modified for aurora's new feature start
                        if (child != lc) {
                            child.layout(curX, curY + 3, rightPos, curY + childHeight);
                        } else {
                            child.layout(curX, curY, rightPos, curY + childHeight);
                        }
                        // Aurora xuyong 2013-11-20 modified for aurora's new feature end

                        curX += childWidth + mHorizonInterval;
                    }
                }
                //Gionee <guoyx> <2013-07-23> delete for CR00838431 begin
                //clean previous hint text
//                mEditText.setHint("");
                //Gionee <guoyx> <2013-07-23> delete for CR00838431 end
                final int editWidth  = mEditText.getMeasuredWidth();
                final int editHeight = mEditText.getMeasuredHeight();

                int vOffset = 0;

                if(editWidth + curX > mostRightPos)
                {
                    //if edit text is at start of a new line ,vOffset remains zero
                    curX = mPaddingLeft;
                    curY += childHeight + mVerticalInterval;
                }
                else
                {
                    //let the bottom of the cursor align to the bottom of other child
                    vOffset = (childHeight - editHeight + 1) >> 1;
                }

                //we always align the right of edit text to right most
                // Aurora xuyong 2013-11-20 modified for aurora's new feature start
                mEditText.layout(curX, curY + vOffset + 1, parentWidth - mPaddingRight, curY + vOffset + editHeight + 1);
                // Aurora xuyong 2013-11-20 modified for aurora's new feature end
                // gionee zhouyj 2012-05-15 add for CR00597006 start
                if(MmsApp.mGnMessageSupport) {
                    mEditText.setDropDownHorizontalOffset(-curX-mPaddingLeft-mHorizonInterval);
                }
                // gionee zhouyj 2012-05-15 add for CR00597006 end
            }
            else
            {
                //only has one kid,it's auto edit text view
                //and if its content is empty,we give it a text hint
                String text = mEditText.getText().toString().trim();
                // Aurora xuyong 2013-09-13 added for aurora's new feature start
                /*if(TextUtils.isEmpty(text))
                {
                    mEditText.setHint(R.string.gn_to_hint);
                }*/
                // Aurora xuyong 2013-09-13 added for aurora's new feature end

                final int editHeight = mEditText.getMeasuredHeight();
                curY = (parentHeight - editHeight - 1) >> 1;
                mEditText.layout(curX, curY, mostRightPos, parentHeight - curY);
                // gionee zhouyj 2012-05-15 add for CR00597006 start
                if(MmsApp.mGnMessageSupport) {
                    mEditText.setDropDownHorizontalOffset(-curX-mPaddingLeft);
                }
                // gionee zhouyj 2012-05-15 add for CR00597006 end
            }
            //gionee gaoj 2012-7-31 removed for CR00661303 start
            //gionee gaoj 2012-7-31 removed for CR00661303 end
        }
    }

    //gionee gaoj 2012-7-31 added for CR00661303 start
    public void setDropDownHeight(int height) {
        if (mEditText.getDropDownHeight() != height) {
            mEditText.setDropDownHeight(height);
        }
    }
    //gionee gaoj 2012-7-31 added for CR00661303 end

    //gionee gaoj 2012-8-6 added for CR00663678 start
    public ListAdapter getAdapter() {
        return mEditText.getAdapter();
    }
    //gionee gaoj 2012-8-6 added for CR00663678 end
    
    //Gionee <zhouyj> <2013-05-22> add for CR00818496 begin
    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        mEditText.setAdapter(adapter);
    }
    //Gionee <zhouyj> <2013-05-22> add for CR00818496 end

    public void setAdapter(ResourceCursorAdapter adapter)
    {
        mEditText.setAdapter(adapter);
    }
    /**
     * Called to initialize contact container
     * @param list original contact list
     */
    public void initContainer(ContactList list)
    {
        reset();
        for (Contact c : list) {

            //Instead of calling addNewContact method,create and add ContactInfo directly
            ContactInfo ci = new ContactInfo();
            // Aurora xuyong 2013-09-16 added for aurora's new feature start
            ci.hasBeenSelected = false;
            // Aurora xuyong 2013-09-16 added for aurora's new feature end
            ci.displayName = c.getName();
            ci.number = c.getNumber();
            // Aurora xuyong 2013-09-16 added for aurora's new feature start
            // Aurora xuyong 2014-10-23 modified for privacy feature start
            ci.privacy = String.valueOf(c.getPrivacy());
            int result = hasExistsInContactList(ci.number, ci.privacy);
            if (result == NUMBER_AND_PRIVACY_EQUAL) {
                continue;
            } else if (result == ONLY_NUMBER_EQUAL){
                   removeContact(ci.number);
            }
            // Aurora xuyong 2013-09-16 added for aurora's new feature end
            mContactInfoList.add(ci);
            mNumberMap.put(ci.number, ci);
            insertOrUpdatePrivacy(ci.number, ci.privacy);
            // Aurora xuyong 2014-10-23 modified for privacy feature end
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.gn_contact_item, ContactContainer.this, false);
            TextView tv = (TextView)ll.findViewById(R.id.contact_item);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            // Aurora xuyong 2013-09-13 added for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            setSelectedState(ll, false);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            // Aurora xuyong 2013-09-13 added for aurora's new feature end
            tv.setTag(ci);
            tv.setText(ci.displayName);
            tv.setOnClickListener(ContactContainer.this);
//            tv.setOnCreateContextMenuListener(mContactListListener);
            // Aurora xuyong 2013-10-24 deleted for aurora's new feature start
            //tv.setOnLongClickListener(onTextViewLongClickListener);
            // Aurora xuyong 2013-10-24 deleted for aurora's new feature end
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            addView(ll);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }

        if(mContactInfoList.size() > 0)
        {
            StringBuffer sb = new StringBuffer();
            for( ContactInfo it : mContactInfoList)
            {
                sb.append(it.displayName);
                sb.append(",");
            }
            //remove the last ","

            String mergeString = sb.toString();
            if(!TextUtils.isEmpty(mergeString))
               // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
               //mFreezeText.setText(mergeString.substring(0, mergeString.length() - 1));
                updateFreezeAndNailText();
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            //Gionee <guoyx> <2013-07-25> add for CR00839534 begin
            mEditText.setHint("");
            //Gionee <guoyx> <2013-07-25> add for CR00839534 end
        }

        if(mContactsChangeListener != null)
            mContactsChangeListener.contactsChanged();
    }

    public void updateText(TextView child)
    {
        TextView tv = (child == null ? (TextView)mView : child);
        String name = null;
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        if (tv != null && tv != mEditText && tv != lf.findViewById(R.id.aurora_freeze_text)) {
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
            ContactInfo ci = (ContactInfo) tv.getTag();
            if (ci != null) {
                name = Contact.get(ci.number, true).getName();
                if (!ci.displayName.equals(name)) {
                    ci.displayName = name;
                    tv.setText(ci.displayName);
                }
            }
        }
    }
    
    public String getText()
    {
        StringBuilder sb = new StringBuilder();

        for( ContactInfo it : mContactInfoList)
        {
            sb.append(it.displayName);
        }

        return sb.toString();
    }
    // Aurora xuyong 2013-09-13 added for aurora's new feature start
    private void setSelectedState(View v, boolean state) {
        if (state) {
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            ((LinearLayout)v).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.aurora_contact_item_bg_pressed));
            // Aurora xuyong 2015-02-06 modified for forher feature start
            ((TextView)v.findViewById(R.id.contact_item)).setTextColor(this.getResources().getColor(R.color.aurora_color_cpd));
            // Aurora xuyong 2015-02-06 modified for forher feature end
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        } else {
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            ((LinearLayout)v).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.aurora_contact_item_bg_normal));
            ((TextView)v.findViewById(R.id.contact_item)).setTextColor(Color.BLACK);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }
    }
    // Aurora xuyong 2013-09-13 added for aurora's new feature end
    // Aurora xuyong 2013-09-13 added for aurora's new feature start
    private boolean mIsDeleteMode;
    // Aurora xuyong 2013-09-13 added for aurora's new feature end
    public void onClick(View v) {
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        String number = ((ContactInfo)v.getTag()).number;
        if (mContactInfoList != null) {
            for (ContactInfo ci : mContactInfoList) {
                if (ci.number.equals(number)) {
                    if (ci.hasBeenSelected && mIsDeleteMode) {
                        mContactInfoList.remove(v.getTag());
                        mNumberMap.remove(((ContactInfo)v.getTag()).number);
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                        removeView((LinearLayout)(v.getParent()));
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                        if(mContactsChangeListener != null) {
                            mContactsChangeListener.contactsChanged();
                        }
                    } else {
                        mIsDeleteMode = false;
                        ci.hasBeenSelected = ci.hasBeenSelected ? false : true;
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature start
                        // Aurora xuyong 2014-03-06 added for aurora's new feature start
                        if (ci.hasBeenSelected) {
                            addToNumberCache(ci.number);
                        } else {
                            revomeFromNumberCache(ci.number);
                        }
                        // Aurora xuyong 2014-03-06 added for aurora's new feature end
                        setSelectedState((LinearLayout)(v.getParent()), ci.hasBeenSelected);
                        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                        mNumberMap.put(ci.number, ci);
                        // Aurora xuyong 2014-03-06 added for aurora's new feature start
                        if (getSelectedNumber() > 0) {
                            mEditText.setCursorVisible(false);
                        } else {
                            mEditText.setCursorVisible(true);
                        }
                        // Aurora xuyong 2014-03-06 added for aurora's new feature end
                    }
                    break;
                }
            }
        }
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
        // TODO Auto-generated method stub
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        // setSelectedState(v, true);
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature start
        /*mContactInfoList.remove(v.getTag());
        mNumberMap.remove(((ContactInfo)v.getTag()).number);
        removeView(v);

        if(mContactsChangeListener != null)
            mContactsChangeListener.contactsChanged();*/
        // Aurora xuyong 2013-09-13 deleted for aurora's new feature end
        onLayout(false, 0, 0, getLayoutParams().width, getLayoutParams().height);
    }

    /**
     * Called to update the text of freeze text view
     * As user presses pick button to contact list for choosing contact
     * then edit text will lose focus,freeze text is set
     * when user has finished choosing and comes back,
     * we won't receive focus change now,so we need to update
     * freeze text according to the contact which user just has chosen
     */
    public void updateFreezeText()
    {
        if(mContactInfoList.isEmpty())
        {
            mFreezeText.setText(null);
            // Aurora xuyong 2013-10-17 added for aurora's new feature start 
            mFreezeNailText.setText(null);
            // Aurora xuyong 2013-10-17 added for aurora's new feature end
        }
        else
        {
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            //String finalString = formatFreezeText();
            //mFreezeText.setText(Html.fromHtml(finalString));
            updateFreezeAndNailText();
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }
    }
    
    public void onFocusChange(View v, boolean hasFocus) {

        if(v == mEditText)
        {
            // Aurora xuyong 2014-03-06 added for aurora's new feature start
            if (mNumberCache != null) {
                mNumberCache.clear();
            }
            // Aurora xuyong 2014-03-06 added for aurora's new feature end
            Log.v(TAG, "edit text focuse: " + hasFocus);
            if(hasFocus)
            {
                //gionee gaoj 2012-5-9 added for CR00588933 start
                if (mRequestFocusListener != null) {
                    mRequestFocusListener.containerRequestFocus(true);
                }
                //gionee gaoj 2012-5-9 added for CR00588933 end
                //show all kids except freeze text view
                int count =  getChildCount();

                while(--count >= 0)
                {
                    View child = getChildAt(count);
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                    if(child != lf)
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                    {
                        child.setVisibility(VISIBLE);
                    }
                }
                // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                lf.setVisibility(GONE);
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end

                mEditText.setText(null);
              //Gionee <guoyx> <2013-07-23> add for CR00838431 begin
                mEditText.setHint("");
              //Gionee <guoyx> <2013-07-23> add for CR00838431 end
                onMeasure(mWidthMeasureSpec, mHeightMeasureSpec);

                if(mEditModeLsn != null)
                {
                    mEditModeLsn.editModeChanged(true);
                }

                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(v, 0);
                }
            }
            else
            {
                //gionee gaoj 2012-5-9 added for CR00588933 start
                if (mRequestFocusListener != null) {
                    mRequestFocusListener.containerRequestFocus(false);
                }
                //gionee gaoj 2012-5-9 added for CR00588933 end

                String curInput = mEditText.getText().toString().trim();
                // Aurora xuyong 2014-02-13 deleted for bug #11672 start
            /*    mEditText.setText(null);
              //Gionee <guoyx> <2013-07-23> add for CR00838431 begin
                mEditText.setHint("");*/
                // Aurora xuyong 2014-02-13 deleted for bug #11672 end
              //Gionee <guoyx> <2013-07-23> add for CR00838431 end
                // Aurora xuyong 2014-10-24 modified for privacy feature start
                if(!TextUtils.isEmpty(curInput)) {
                // Aurora xuyong 2014-02-13 modified for bug #11672 start
                        processEnterKey(curInput, FOUCUS_CHANGE_TYPE);
                } else {
                    Message msg = mHandler.obtainMessage(FOCUS_CHANGED);
                    msg.sendToTarget();
                // Aurora xuyong 2014-02-13 modified for bug #11672 end
                }
                // Aurora xuyong 2014-10-24 modified for privacy feature end
                // Aurora xuyong 2014-02-13 deleted for bug #11672 start
                /*//hide all view except freeze text view
                int count =  getChildCount();

                //delete in opposite order
                while(--count >= 0)
                {
                    View child = getChildAt(count);
                    // Aurora xuyong 2013-09-17 modified for aurora's new feature start
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                    if(child == lf || child == lc)
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                    {
                        child.setVisibility(VISIBLE);
                    } else {
                        child.setVisibility(GONE);
                    }
                    // Aurora xuyong 2013-09-17 modified for aurora's new feature end
                }

                //merge all name into a string
                if(mContactInfoList.isEmpty())
                {
                    mFreezeText.setText(null);
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                    mFreezeNailText.setText(null);
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                }
                else
                {
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                    //String finalString = formatFreezeText();
                    //mFreezeText.setText(Html.fromHtml(finalString));
                    updateFreezeAndNailText();
                    // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                }
                // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                lf.setVisibility(VISIBLE);
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end

                if(mEditModeLsn != null)
                {
                    mEditModeLsn.editModeChanged(false);
                }*/
                // Aurora xuyong 2014-02-13 deleted for bug #11672 end
            }
        }
    }

    /**
     * Called to format a string with all contacts name together
     * @return formatted string
     */
    protected String formatFreezeText()
    {
        StringBuffer sb = new StringBuffer();
        final int size = mContactInfoList.size();
        if (size > CONTACT_DISPALY_MAX ) {
            ContactInfo it = null;
             for(int i = 0; i < CONTACT_DISPALY_MAX; i++){
                 it = mContactInfoList.get(i);
                 sb.append(it.displayName);
                 // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                 sb.append("、");
                 // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }
        }
        else{
        for( ContactInfo it : mContactInfoList) {
            sb.append(it.displayName);
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            sb.append("、");
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        }
        }
        //remove the last ","
        sb.deleteCharAt(sb.length()-1);
        

        String mergeString = sb.toString();
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        return mergeString;
        //Resources res = getResources();
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        //Gionee <guoyx> <2013-07-23> delete for CR00838431 begin
//        final String head = res.getString(R.string.gn_to_hint);
        //Gionee <guoyx> <2013-07-23> delete for CR00838431 end
        //If we got contacts more than MIN_CONTACT_AMOUNT, show a contacts number ,or else not
        // Aurora xuyong 2013-10-17 deleted for aurora's new feature start 
        /*if(size > MIN_CONTACT_AMOUNT)
        {
            return res.getString(R.string.freeze_format_with_bracket, mergeString);
        }
        else
        {
            return res.getString(R.string.freeze_format, mergeString);
        }*/
        // Aurora xuyong 2013-10-17 deleted for aurora's new feature end
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      mWidthMeasureSpec = widthMeasureSpec;
      mHeightMeasureSpec = heightMeasureSpec;

      int heightSize = mPaddingTop + mPaddingBottom;
      heightSize = Math.max(heightSize, getSuggestedMinimumHeight());
      final int desiredHeight = resolveSize(heightSize, heightMeasureSpec);

      int widthSize = mPaddingLeft + mPaddingRight;
      widthSize = Math.max(widthSize, getSuggestedMinimumWidth());
      final int desiredWidth = resolveSize(widthSize, widthMeasureSpec);
      // Check against our minimum width
      // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
      if(lf.getVisibility() == VISIBLE)
      {
          // Aurora xuyong 2013-10-25 added for bug 220 start
          measureChild(lc, mWidthMeasureSpec, mHeightMeasureSpec);
          // Aurora xuyong 2013-10-25 added for bug 220 end
          measureChild(lf, mWidthMeasureSpec, mHeightMeasureSpec);

          final int height = lf.getMeasuredHeight();
      // Aurora xuyong 2013-10-17 modified for aurora's new feature end

          int totalHeight = height + mPaddingTop + mPaddingBottom;

          totalHeight = Math.max(totalHeight, desiredHeight);

          setMeasuredDimension(desiredWidth, totalHeight);

          if(mLinesWatcher != null)
          {
              mLinesWatcher.linesChanged(1);
          }
      }
      else
      {
          int totalHeight = mPaddingTop + mPaddingBottom;
          final int maxRightPos = desiredWidth - mPaddingLeft;
          final int childMaxWidth = maxRightPos - mPaddingRight;

          int x = mPaddingLeft;
          int lines = 1;
          int childHeight = 0 ;
          int childWidth = 0;

          final int childCount = getChildCount();

          if(childCount > 1)
          {
              for(int i=0; i<childCount; ++i)
              {
                  View child = getChildAt(i);

                  //As we always place other child first and finally put edit text view in onLayout method,
                  //so we sync the sequence of the measure operation with layout operation
                  // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                  if(child != mEditText
                          && child != lf )
                  // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                  {
                      measureChild(child, widthMeasureSpec, heightMeasureSpec);

                      childWidth = getChildAt(i).getMeasuredWidth();
                      childHeight = getChildAt(i).getMeasuredHeight();

                      childWidth = Math.min(childWidth, childMaxWidth);

                      x += childWidth;

                      if(x > maxRightPos)
                      {
                          x = mPaddingLeft + childWidth;
                          // Aurora xuyong 2014-08-13 modified for aurora's new feature start
                          if (maxRightPos > 0) {
                                ++lines;
                          }
                          // Aurora xuyong 2014-08-13 modified for aurora's new feature end
                      }

                      x += mHorizonInterval;
                  }
              }
          }

          //now process edit text view at last
          measureChild(mEditText, mWidthMeasureSpec, mHeightMeasureSpec);
          //as we first came in,only has edit text ,so use its height as child height
          final int editWidth = mEditText.getMeasuredWidth();
          final int editHeight = mEditText.getMeasuredHeight();

          x += Math.min(editWidth, childMaxWidth);

          if(x > maxRightPos)
          {
              // Aurora xuyong 2014-08-13 modified for aurora's new feature start
                if (maxRightPos > 0) {
                     ++lines;
              }
              // Aurora xuyong 2014-08-13 modified for aurora's new feature end
          }

          //As the height of edit text may be greater than other text view,so plus it in separate way
          totalHeight += (lines-1)*childHeight + (lines - 1)*mVerticalInterval + Math.max(editHeight,childHeight);
          totalHeight = Math.max(totalHeight, desiredHeight);

          setMeasuredDimension(desiredWidth, totalHeight);

          if(mLinesWatcher != null)
          {
              /*Calculate a max height that scroll view can be set*/
              if(lines > mMaxLines)
              {
                  mExternalScrollMaxHeight = desiredHeight
                                              + (mMaxLines - 1)*childHeight
                                              + (mMaxLines - 1)*mVerticalInterval
                                              + Math.max(editHeight,childHeight);
              }
              // Aurora xuyong 2014-08-13 modified for aurora's new feature start
              if (maxRightPos > 0) {
                     mLinesWatcher.linesChanged(lines);
              }
              // Aurora xuyong 2014-08-13 modified for aurora's new feature end
          }
      }
    }

    public void setMaxLines(int maxLines)
    {
        mMaxLines = maxLines;
    }

    public int getExternalScrollMaxHeight()
    {
        return mExternalScrollMaxHeight;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        //As once user touched this view,give focus to edit text
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(mTouchedLsn != null)
                mTouchedLsn.containerTouched();
        }

        return super.onInterceptTouchEvent(event);
    }

    /*The old listener is not removed*/

    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Listener for contact amount changing
     * Be Called when total lines greater than 1
     * @author allen
     *
     */
    public interface LinesWatcher
    {
        /**
         * Notify that total contact amount changed
         * @param childCount new value of total child amount
         * @param lines how many lines we got
         */
        void linesChanged(int lines);
    }

    private LinesWatcher mLinesWatcher = null;

    public void setLinesWatcher(LinesWatcher l)
    {
        mLinesWatcher = l;
    }

    /**
     * Called to tell parent that edit mode changed
     * @author allen
     */
    public interface EditModeChangeListener
    {
        /**
         * Called to tell caller that edit mode changed
         * @param isEditMode false if view lost focus,we are not editable,
         *        call should hide add contact button .
         */
        void editModeChanged(boolean isEditMode);
    }

    public EditModeChangeListener mEditModeLsn = null;

    public void setEditModeChangeListener(EditModeChangeListener l)
    {
        mEditModeLsn = l;
    }

    public interface ContactsChangeListener
    {
        /**
         * Called to tell caller that edit mode changed
         * @param isEditMode false if view lost focus,we are not editable,
         *        call should hide add contact button .
         */
        void contactsChanged();
    }

    public ContactsChangeListener mContactsChangeListener = null;

    public void setContactsChangeListener(ContactsChangeListener l)
    {
        mContactsChangeListener = l;
    }

    private TextWatcher mClientEditTextWatcher = null;

    public void addTextChangedListener(TextWatcher editTextWatcher) {
        mClientEditTextWatcher = editTextWatcher;
    }
    // Aurora xuyong 2015-03-04 added for bug #11831 start
    private View mClikedButton;
    private boolean mNeedNotifyClickSend;
    public void setOnClickView(View view) {
        String editText = mEditText.getText().toString();
        if (editText != null && editText.length() > 0) {
            mNeedNotifyClickSend = true;
            processEnterKey(mEditText.getText().toString(), FOUCUS_CHANGE_TYPE);
            mClikedButton = view;
        }
    }
    
    public void setOnClickBack(int backType) {
        String editText = mEditText.getText().toString();
        if (editText != null && editText.length() > 0) {
            mNeedNotifyClickSend = true;
            processEnterKey(mEditText.getText().toString(), FOUCUS_CHANGE_TYPE, backType);
        } else {
            Message nMessage = mNHandler.obtainMessage(NOTIFY_FOCUS_CHANGE);
            nMessage.obj = mClikedButton;
            nMessage.arg2 = backType;
            nMessage.sendToTarget();
            mClikedButton = null;
        }
    }
    // Aurora xuyong 2015-03-04 added for bug #11831 end

    public boolean haveRecipients() {
        if (!TextUtils.isEmpty(mEditText.getText()) || !mContactInfoList.isEmpty()) {
            Log.i(TAG, "haveRecipients return true");
            return true;
        }

        Log.i(TAG, "haveRecipients return false");
        return false;
    }

    public void updateContacts() {
        onFocusChange(mEditText, false);
    }
    // Aurora xuyong 2015-02-13 added for bug #11672 start
    public void updateContacts(boolean hasFocus) {
        onFocusChange(mEditText, hasFocus);
    }
    // Aurora xuyong 2015-02-13 added for bug #11672 end

    /**
     * Called to tell parent that user has touched contacts container
     * We have tried to watch the status of IME,but failed
     * with no other choice,we set up this listener once we got touched,tell
     * parents to do what he wants to do
     */
    public interface TouchedEventListener
    {
        /**
         * Called to tell caller that user touched contacts container
         */
        void containerTouched();
    }

    public TouchedEventListener mTouchedLsn = null;

    public void setTouchedEventListener(TouchedEventListener l)
    {
        mTouchedLsn = l;
    }

    public interface CheakContatctListener {
         /**
         * back to set misPickContatct is true for do not SaveDraft
         */
        void containerCheakContatct(boolean isCheakContatct);
    }
    
    public CheakContatctListener mCheakContatctListener = null;

    public void setCheakContatctListener(CheakContatctListener l) {
        mCheakContatctListener = l;
    }

    //gionee gaoj 2012-5-9 added for CR00588933 start
    public interface RequestFocusListener {
         /**
         * back to set misPickContatct is true for do not SaveDraft
         */
        void containerRequestFocus(boolean isFocus);
    }
    
    public RequestFocusListener mRequestFocusListener = null;

    public void setRequestFocusListener(RequestFocusListener l) {
        mRequestFocusListener = l;
    }
    //gionee gaoj 2012-5-9 added for CR00588933 end

    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        if(v == lf)
        {
            lf.setVisibility(GONE);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end

            mEditText.setVisibility(VISIBLE);
            mEditText.requestFocus();

            //Send a fake touch event as if user has clicked our edit text ,
            //then we can show tool bar up when user click our edit text at first time
            //without this action,user has to click twice for showing a tool bar
            MotionEvent me = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
                    event.getAction(), mEditText.getLeft() , mEditText.getTop(),
                    event.getMetaState());
            dispatchTouchEvent(me);
        }

        return false;
    }

    public void resetMode() {
        if (mEditText != null) {
            mEditText.setVisibility(View.VISIBLE);
        }
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        if (lf != null) {
            lf.setVisibility(View.GONE);
        }
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
    }
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    public void addNewContact(String name, String number, String privacy, boolean notify) {
    // Aurora xuyong 2014-10-23 modified for privacy feature end
        ContactInfo ci = new ContactInfo();
        // Aurora xuyong 2013-09-16 added for aurora's new feature start
        ci.hasBeenSelected = false;
        // Aurora xuyong 2013-09-16 added for aurora's new feature end
        ci.displayName = name;
        ci.number = number;
        // Aurora xuyong 2013-09-16 added for aurora's new feature start
        // Aurora xuyong 2014-10-23 modified for privacy feature start
        ci.privacy = privacy;
        int result = hasExistsInContactList(ci.number, ci.privacy);
        if (result == NUMBER_AND_PRIVACY_EQUAL) {
            return;
        } else if (result == ONLY_NUMBER_EQUAL){
               removeContact(ci.number);
        }
        // Aurora xuyong 2014-10-23 modified for privacy feature end
        // Aurora xuyong 2013-09-16 added for aurora's new feature end
        mContactInfoList.add(ci);
        mNumberMap.put(ci.number, ci);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.gn_contact_item, ContactContainer.this, false);
        TextView tv = (TextView)ll.findViewById(R.id.contact_item);
        // Aurora xuyong 2013-09-13 added for aurora's new feature start
        setSelectedState(ll, false);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end
        // Aurora xuyong 2013-09-13 added for aurora's new feature end
        tv.setTag(ci);
        tv.setText(ci.displayName);
        tv.setOnClickListener(ContactContainer.this);
//        tv.setOnCreateContextMenuListener(mContactListListener);
        //Aurora xuyong 2013-09-20 deleted for aurora's new feature start
        //tv.setOnLongClickListener(onTextViewLongClickListener);
        //Aurora xuyong 2013-09-20 deleted for aurora's new feature end
        // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
        addView(ll);
        // Aurora xuyong 2013-10-17 modified for aurora's new feature end

        if (mContactsChangeListener != null && notify)
            mContactsChangeListener.contactsChanged();
    }

    public void removeContact(String number, boolean notify) {
        int count = getChildCount();
        // delete in opposite order
        while (--count >= 0) {
            View child = getChildAt(count);
            //Aurora xuyong 2013-09-20 modified for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            if (child != mEditText && child != lf && child != lc) {
            //Aurora xuyong 2013-09-20 modified for aurora's new feature end
                ContactInfo ci = (ContactInfo) child.findViewById(R.id.contact_item).getTag();
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                if (ci.number.equals(number)) {
                    removeView(child);
                    mContactInfoList.remove(ci);
                    mNumberMap.remove(number);

                    if (mContactsChangeListener != null && notify)
                        mContactsChangeListener.contactsChanged();

                    break;
                }
            }
        }
    }

    //gionee gaoj 2012-7-9 added for CR00626901 start
    public void removeLastContact(int lastcount) {
        int count = getChildCount();
        int i = 0;
        while (--count >= 0) {
            View child = getChildAt(count);
            //Aurora xuyong 2013-09-20 modified for aurora's new feature start
            // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
            if (child != mEditText && child != lf && child != lc) {
            //Aurora xuyong 2013-09-20 modified for aurora's new feature end
                ContactInfo ci = (ContactInfo) child.findViewById(R.id.contact_item).getTag();
            // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                removeView(child);
                mContactInfoList.remove(ci);
                mNumberMap.remove(ci.number);
                i++;
                if (i == lastcount)
                    break;
            }
        }
        if (mContactsChangeListener != null)
            mContactsChangeListener.contactsChanged();
    }
    //gionee gaoj 2012-7-9 added for CR00626901 end

    private OnLongClickListener onTextViewLongClickListener = new OnLongClickListener() {
        
        @Override
        public boolean onLongClick(View v) {
            // TODO Auto-generated method stub
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getContext());
            
            mMenuContact = (ContactInfo)v.getTag();
            boolean mFlagNumber = false ;
            String[] items = null;
            if (mMenuContact.number != null) {
                mFlagNumber = Contact.get(mMenuContact.number, false).existsInDatabase();
            }

            if (mFlagNumber == false || mMenuContact.number == mMenuContact.displayName) {
                //Gionee <zhouyj> <2013-05-17> add for CR00812110 begin
                mNewNumber = mMenuContact.number;
                //Gionee <zhouyj> <2013-05-17> add for CR00812110 end
                
                builder.setTitle(mMenuContact.number);
                items = new String[4];
                items[0] = getContext().getString(R.string.new_contact);
                items[1] = getContext().getString(R.string.add_contact);
                items[2] = getContext().getString(R.string.edit_contact);
                items[3] = getContext().getString(R.string.copy_contact);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        switch (which) {
                        case 0:
                            newContact();
                            break;
                        case 1:
                            addContact();
                            break;
                        case 2:
                            editContact();
                            break;
                        case 3:
                            copyContact();
                            break;

                        default:
                            break;
                        }
                    }
                });
            } else {
                builder.setTitle(mMenuContact.displayName);
                items = new String[3];
                items[0] = getContext().getString(R.string.view_contact);
                items[1] = getContext().getString(R.string.edit_contact);
                items[2] = getContext().getString(R.string.copy_contact);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        switch (which) {
                        case 0:
                            viewContact();
                            break;
                        case 1:
                            editContact();
                            break;
                        case 2:
                            copyContact();
                            break;

                        default:
                            break;
                        }
                    }
                });
            }
            
            builder.show();
            return false;
        }
    };
    
    private void newContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT,Contacts.CONTENT_URI);

        //gionee gaoj 2013-4-2 added for CR00792780 start
        intent.setComponent(new ComponentName("com.android.contacts",
        "com.android.contacts.activities.ContactEditorActivity"));
        //gionee gaoj 2013-4-2 added for CR00792780 end
        
        //gionee gaoj 2012-6-6 modified for CR00616180 start
        if (Mms.isEmailAddress(mMenuContact.number)) {
            intent.putExtra(Insert.EMAIL, mMenuContact.number);
        } else {
            intent.putExtra(Insert.PHONE, mMenuContact.number);
        }
        //gionee gaoj 2012-6-6 modified for CR00616180 end
        //Gionee <zhouyj> <2013-05-17> modify for CR00812110 begin
        ComposeMessageActivity.getComposeContext().startActivityForResult(intent, 
                ComposeMessageActivity.REQUEST_CODE_UPDATE_CONTACT);
        //Gionee <zhouyj> <2013-05-17> modify for CR00812110 end
    }
    
    private void addContact() {
        //gionee gaoj 2012-5-24 added for CR00608133 CR00616180 start
        final Intent newintent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        
        //gionee gaoj 2013-4-2 added for CR00792780 start
        newintent.setComponent(new ComponentName("com.android.contacts",
        "com.android.contacts.activities.ContactSelectionActivity"));
        //gionee gaoj 2013-4-2 added for CR00792780 end
        
        if (Mms.isEmailAddress(mMenuContact.number)) {
            newintent.putExtra(Insert.EMAIL, mMenuContact.number);
        } else {
            newintent.putExtra(Insert.PHONE, mMenuContact.number);
        }
        newintent.setType(People.CONTENT_ITEM_TYPE);
        //Gionee <zhouyj> <2013-05-17> modify for CR00812110 begin
        ComposeMessageActivity.getComposeContext().startActivityForResult(newintent, 
                ComposeMessageActivity.REQUEST_CODE_UPDATE_CONTACT);
        //Gionee <zhouyj> <2013-05-17> modify for CR00812110 end
        //gionee gaoj 2012-5-24 added for CR00608133 CR00616180 end
    }
    
    private void editContact() {
        String curInput = mEditText.getText().toString().trim();
        mEditText.setText(null);

        if (!TextUtils.isEmpty(curInput)) {
            addNewContact(curInput);
        }
        removeContact(mMenuContact.number);
        mEditText.setText(mMenuContact.displayName);
    }
    
    private void copyContact() {
        String mText = mMenuContact.displayName;
        try {
            @SuppressWarnings("deprecation")
            ClipboardManager cm =(ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(mText);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void viewContact() {
        if (mMenuContact.contactId <= 0) {
            mMenuContact.contactId = Contact.getIdInDatabase(mMenuContact.number, false);
        }
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, mMenuContact.contactId);
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, contactUri);
        
        //gionee gaoj 2013-4-2 added for CR00792780 start
        viewIntent.setComponent(new ComponentName("com.android.contacts",
            "com.android.contacts.activities.ContactDetailActivity"));
        //gionee gaoj 2013-4-2 added for CR00792780 end
        
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        getContext().startActivity(viewIntent);
    }

    ContactInfo mMenuContact = null;
    /*private static final int MENU_NEW_CONTACT           = 41;
    private static final int MENU_ADD_CONTACT           = 42;
    private static final int MENU_EDIT_CONTACT          = 43;
    private static final int MENU_COPY_CONTACT          = 44;
    private static final int MENU_VIEW_CONTACT          = 45;
    private final OnCreateContextMenuListener mContactListListener = 
        new OnCreateContextMenuListener() {
            
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenuInfo arg2) {
                // TODO Auto-generated method stub
                ContactListMenuClickListener l = new ContactListMenuClickListener();

                mMenuContact = (ContactInfo)v.getTag();
                boolean mFlagNumber = false ;

                if (mMenuContact.number != null) {
                    mFlagNumber = Contact.get(mMenuContact.number, false).existsInDatabase();
                }

                if (mFlagNumber == false || mMenuContact.number == mMenuContact.displayName) {
                    //Gionee <zhouyj> <2013-05-17> add for CR00812110 begin
                    mNewNumber = mMenuContact.number;
                    //Gionee <zhouyj> <2013-05-17> add for CR00812110 end
                    menu.setHeaderTitle(mMenuContact.number);
                    menu.add(0, MENU_NEW_CONTACT, 0, R.string.new_contact)
                    .setOnMenuItemClickListener(l);
                    menu.add(0, MENU_ADD_CONTACT, 0, R.string.add_contact)
                    .setOnMenuItemClickListener(l);
                } else {
                    menu.setHeaderTitle(mMenuContact.displayName);
                    menu.add(0, MENU_VIEW_CONTACT, 0, R.string.view_contact)
                    .setOnMenuItemClickListener(l);
                }

                menu.add(0, MENU_EDIT_CONTACT, 0, R.string.edit_contact)
                .setOnMenuItemClickListener(l);
                menu.add(0, MENU_COPY_CONTACT, 0, R.string.copy_contact)
                .setOnMenuItemClickListener(l);
            }
        };
        
    private final class ContactListMenuClickListener implements MenuItem.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // TODO Auto-generated method stub
            switch (item.getItemId()) {
            case MENU_NEW_CONTACT:

                Intent intent = new Intent(Intent.ACTION_INSERT,Contacts.CONTENT_URI);

                //gionee gaoj 2013-4-2 added for CR00792780 start
                intent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.activities.ContactEditorActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                //gionee gaoj 2012-6-6 modified for CR00616180 start
                if (Mms.isEmailAddress(mMenuContact.number)) {
                    intent.putExtra(Insert.EMAIL, mMenuContact.number);
                } else {
                    intent.putExtra(Insert.PHONE, mMenuContact.number);
                }
                //gionee gaoj 2012-6-6 modified for CR00616180 end
                //Gionee <zhouyj> <2013-05-17> modify for CR00812110 begin
                ComposeMessageActivity.getComposeContext().startActivityForResult(intent, 
                        ComposeMessageActivity.REQUEST_CODE_UPDATE_CONTACT);
                //Gionee <zhouyj> <2013-05-17> modify for CR00812110 end
                break;

            case MENU_ADD_CONTACT:
                //gionee gaoj 2012-5-24 added for CR00608133 CR00616180 start
                final Intent newintent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                
                //gionee gaoj 2013-4-2 added for CR00792780 start
                newintent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.activities.ContactSelectionActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                if (Mms.isEmailAddress(mMenuContact.number)) {
                    newintent.putExtra(Insert.EMAIL, mMenuContact.number);
                } else {
                    newintent.putExtra(Insert.PHONE, mMenuContact.number);
                }
                newintent.setType(People.CONTENT_ITEM_TYPE);
                //Gionee <zhouyj> <2013-05-17> modify for CR00812110 begin
                ComposeMessageActivity.getComposeContext().startActivityForResult(newintent, 
                        ComposeMessageActivity.REQUEST_CODE_UPDATE_CONTACT);
                //Gionee <zhouyj> <2013-05-17> modify for CR00812110 end
                //gionee gaoj 2012-5-24 added for CR00608133 CR00616180 end
                break;

            case MENU_EDIT_CONTACT:
                String curInput = mEditText.getText().toString().trim();
                mEditText.setText(null);
        
                if (!TextUtils.isEmpty(curInput)) {
                    addNewContact(curInput);
                }
                removeContact(mMenuContact.number);
                mEditText.setText(mMenuContact.displayName);
                break;

            case MENU_COPY_CONTACT:
                String mText = mMenuContact.displayName;
                try {
                    @SuppressWarnings("deprecation")
                    ClipboardManager cm =(ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(mText);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case MENU_VIEW_CONTACT:
                if (mMenuContact.contactId <= 0) {
                    mMenuContact.contactId = Contact.getIdInDatabase(mMenuContact.number, false);
                }
                Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, mMenuContact.contactId);
                Intent viewIntent = new Intent(Intent.ACTION_VIEW, contactUri);
                
                //gionee gaoj 2013-4-2 added for CR00792780 start
                viewIntent.setComponent(new ComponentName("com.android.contacts",
                    "com.android.contacts.activities.ContactDetailActivity"));
                //gionee gaoj 2013-4-2 added for CR00792780 end
                
                viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                getContext().startActivity(viewIntent);
                break;

            default:
                break;
            }
            return false;
        }
    }*/
    // gionee zhouyj 2013-01-04 add for CR00678675 start 
    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }
    // gionee zhouyj 2013-01-04 add for CR00678675 end 
    
    //Gionee <zhouyj> <2013-05-09> add for CR00810588 begin
    public void setEditTextName(String name) {
        if (mEditText != null) {
            mEditText.setText(name);
        }
    }
    //Gionee <zhouyj> <2013-05-09> add for CR00810588 end
    
    //Gionee <zhouyj> <2013-05-17> add for CR00812110 begin
    public void updateAddContact(String number) {
        // Aurora xuyong 2014-11-04 modified for bug #9633 start
        // Aurora xuyong 2015-05-25 modified for aurora's new feature start
        // Aurora xuyong 2015-07-06 added for bug #14043 start
    	if (number != null) {
    		number = number.replace("'", "''");
        }
        // Aurora xuyong 2015-07-06 added for bug #14043 end
        Cursor cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.DISPLAY_NAME + " = '" + number + "'", null, null);
        // Aurora xuyong 2015-05-25 modified for aurora's new feature end
        // Aurora xuyong 2014-11-04 modified for bug #9633 end
        if(cs == null || cs.getCount() == 0)
        {
            // Aurora xuyong 2015-05-25 added for aurora's new feature start
        	number = PhoneNumberUtils.getCallerIDMinMacth(number);
            // Aurora xuyong 2015-05-25 added for aurora's new feature end 
            // Aurora xuyong 2014-11-04 modified for bug #9633 start
            // Aurora xuyong 2015-05-25 modified for aurora's new feature start
            // Aurora xuyong 2015-06-30 modified for bug #13952 start
        	if (number != null && number.length() > 0) {
        		cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.NUMBER + " LIKE '%" + number + "'",  null, null);
        	} else {
        		// we pretend to find nothing here if the number string is null.
        		cs = null;
        	}
            // Aurora xuyong 2015-06-30 modified for bug #13952 end
            // Aurora xuyong 2015-05-25 modified for aurora's new feature end
            // Aurora xuyong 2014-11-04 modified for bug #9633 end
        }
        if(cs != null && cs.moveToFirst())
        {
            ContactInfo c = new ContactInfo();
            // Aurora xuyong 2013-09-16 added for aurora's new feature start
            c.hasBeenSelected = false;
            // Aurora xuyong 2013-09-16 added for aurora's new feature end
            for (ContactInfo ci : mContactInfoList) {
                if (ci.number.equals(cs.getString(INDEX_NUMBER))) {
                    mContactInfoList.remove(ci);
                    c.displayName = cs.getString(INDEX_NAME);
                    c.number = cs.getString(INDEX_NUMBER);
                    // Aurora xuyong 2013-09-16 added for aurora's new feature start
                    // Aurora xuyong 2014-10-23 modified for privacy feature start
                    ci.privacy = cs.getString(INDEX_PRIVACY);
                    int result = hasExistsInContactList(ci.number, ci.privacy);
                    if (result == NUMBER_AND_PRIVACY_EQUAL) {
                        continue;
                    } else if (result == ONLY_NUMBER_EQUAL){
                           removeContact(ci.number);
                    }
                    // Aurora xuyong 2014-10-23 modified for privacy feature end
                    // Aurora xuyong 2013-09-16 added for aurora's new feature end
                    mContactInfoList.add(c);
                    mNumberMap.put(cs.getString(INDEX_NUMBER), c);
                    break;
                }
            }
            int count =  getChildCount();
            while(--count >= 0)
            {
                View child = getChildAt(count);
                // Aurora xuyong 2013-10-17 modified for aurora's new feature start 
                if(child != lf)
                // Aurora xuyong 2013-10-17 modified for aurora's new feature end
                {
                    if (child instanceof TextView) {
                        TextView tv = (TextView) child;
                        if (tv.getText().toString().equals(c.number)) {
                            tv.setText(c.displayName);
                            tv.setTag(c);
                            break;
                        }
                    }
                }
            }
        }
        if (cs != null) {
            cs.close();
        }
    }
    
    public String getCurrNewNumber() {
        return mNewNumber;
    }
    //Gionee <zhouyj> <2013-05-17> add for CR00812110 end
    
    //Gionee <zhouyj> <2013-06-09> add for CR00824571 begin
    public void clearmNumberMap() {
        if (mNumberMap != null) {
            mNumberMap.clear();
        }
    }
    //Gionee <zhouyj> <2013-06-09> add for CR00824571 end
    
    // Aurora xuyong 2013-09-16 added for aurora's new feature start
    // Aurora xuyong 2014-10-23 modified for privacy feature start
    private final int ONLY_NUMBER_EQUAL = 0;
    private final int NUMBER_AND_PRIVACY_EQUAL = 1;
    private final int NOT_EQUAL = 3;
    private int hasExistsInContactList(String number, String privacy) {
        if (mContactInfoList != null) {
            for (ContactInfo ci : mContactInfoList) {
                if (ci.number.equals(number) && ci.privacy.equals(privacy)) {
                    return NUMBER_AND_PRIVACY_EQUAL;
                } else if (ci.number.equals(number)) {
                    return ONLY_NUMBER_EQUAL;
                }
            }
            return NOT_EQUAL;
        }
        return NOT_EQUAL;
    }
    // Aurora xuyong 2014-10-23 modified for privacy feature end
    // Aurora xuyong 2013-09-16 added for aurora's new feature end
}
