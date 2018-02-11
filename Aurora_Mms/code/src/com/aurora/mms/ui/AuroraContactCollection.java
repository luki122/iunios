package com.aurora.mms.ui;

import com.android.mms.R;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.Mms;

import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ContactContainer;
import com.android.mms.util.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.mms.ui.MessageUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.os.Bundle;
import com.privacymanage.service.AuroraPrivacyUtils;
import com.android.mms.MmsApp;
import android.os.Handler;
import android.os.Message;

public class AuroraContactCollection extends ViewGroup implements View.OnClickListener,View.OnLongClickListener, View.OnTouchListener, ContactContainer.SyncContactChangeListener {

        private final String TAG = "AuroraContactCollection";

        List<String> mNumberList;

        LayoutInflater mInflater;

        private int mVerticalInterval;
        private int mHorizonInterval;


        private int mWidthMeasureSpec  ;
        private int mHeightMeasureSpec ;

        HashMap<String, ContactInfo> mNumberMap = new HashMap<String, ContactInfo>();

        private ArrayList <ContactInfo> mContactInfoList = null;

        private ContentResolver mCR;
        private static final String[] PROJECTION_PHONE = {
                Phone._ID,                  // 0
                Phone.NUMBER,               // 1
                Phone.DISPLAY_NAME,         // 2
                "is_privacy"                // 3
        };

        private final int INDEX_ID     = 0;
        private final int INDEX_NUMBER = 1;
        private final int INDEX_NAME   = 2;
        private final int INDEX_PRIVACY = 3;

        private final int UNKNOW_ID = -1;
        private int MIN_CONTACT_AMOUNT = 1;

        private int mExternalScrollMaxHeight = 0;
        private int mMaxLines;

        private final int CONTACT_DISPALY_MAX = 6;

        private Context mContext;

        public AuroraContactCollection(Context context, AttributeSet attrs) {
                super(context,attrs);
                mContext = context;
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.container);
                DisplayMetrics m = context.getResources().getDisplayMetrics();
                mHorizonInterval  = a.getDimensionPixelSize(R.styleable.container_cellHorizontalInterval, (int)m.density * 24);
                mVerticalInterval = a.getDimensionPixelSize(R.styleable.container_cellVerticalInterval, (int)m.density * 20);
                mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                mContactInfoList = new ArrayList <ContactInfo> ();
                mCR = context.getContentResolver();
        }

        public void setHandler(Handler handler) {
                mHandler = handler;
        }

        public void reset()
        {
                int count =  getChildCount();
                while(--count >= 0)
                {
                View child = getChildAt(count);
                removeView(child);
                }
                if(!mContactInfoList.isEmpty())
                {
                        mContactInfoList.clear();
                        if(mContactsChangeListener != null) {
                                mContactsChangeListener.contactsChanged();
                        }
                }
        }

        public void getInfo(TextView name, TextView number, TextView privacy) {
                ContactInfo ci = new ContactInfo();
                ci.hasBeenSelected = false;
                ci.displayName = name.getText().toString();
                if(MmsApp.mGnMessageSupport) {
                        ci.number = number.getText().toString().replaceAll(" ", "");
                } else {
                        ci.number = number.getText().toString();
                }
                ci.privacy = privacy.getText().toString();
                int result = hasExistsInContactList(ci.number, ci.privacy);
                if (result == NUMBER_AND_PRIVACY_EQUAL) {
                        return;
                }
                mContactInfoList.add(ci);
                mNumberMap.put(ci.number, ci);
                LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_item, AuroraContactCollection.this, false);
                TextView tv = (TextView)ll.findViewById(R.id.contact_item);
                setSelectedState(ll, false);
                tv.setTag(ci);
                tv.setText(ci.displayName);
                tv.setOnClickListener(AuroraContactCollection.this);
                addView(ll);
                if(mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();
        }

        private ArrayList<String> mNumberCache = new ArrayList<String>();

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

        private final int UPDATE_EXIST_EDIT_CONTACTS = 0x01;
        private final int UPDATE_NEW_EDIT_CONTACTS = 0x02;
        private final int FOCUS_CHANGED = 0x03;

        private boolean needAutoMatchTheFirst(Cursor cursor, long privacy) {
                Log.e(TAG, "cursor.count = " + cursor.getCount());
                if (cursor == null) {
                        return true;
                }
                int currentPositon = cursor.getPosition();
                cursor.moveToPosition(-1);
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
                cursor.moveToPosition(currentPositon);
                if (nameList.size() == 1) {
                        return true;
                } else {
                        return false;
                }
        }

        private Handler mHandler;

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
                if (MmsApp.sHasPrivacyFeature && (isValidAddress(number, false))) {
                        number = number.replaceAll("#", "*");
                        number = number.replaceAll("\\.", "*");
                        number = number.replaceAll("\\*", "/*");
                        if (number.charAt(0) == '+') {
                                number = number.replaceAll("\\+", "");
                                number = "+" + number;
                        } else {
                                number = number.replaceAll("\\+", "");
                        }
                        Cursor cursor = mContext.getContentResolver().query(sPrivacyUri, new String[] { "number" }, "number LIKE ? ESCAPE '/' AND status = 1", new String[]{ number }, null);
                        if (cursor != null && cursor.moveToFirst()) {
                                ContentValues values = new ContentValues();
                                values.put("is_privacy", privacy);
                                mContext.getContentResolver().update(sPrivacyUri, values, "number LIKE ? ESCAPE '/'", new String[]{ number });
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
        }

        public static final int PROCESS_ENTER_ONLY_TYPE = 1;
        public static final int FOUCUS_CHANGE_TYPE      = 2;

        public void processEnterKey(final String input, final int type) {
                new Thread(new Runnable() {

                        @Override
                        public void run() {
                                // TODO Auto-generated method stub
                                process(input, type);
                        }

                }).start();
        }

        public void processEnterKey(final String input, final int type, final int backType) {
                mIsAddingRecipient = true;
                new Thread(new Runnable() {

                        @Override
                        public void run() {
                                // TODO Auto-generated method stub
                                process(input, type, backType);
                        }

                }).start();
        }

        public void process(String input, int type) {
                process(input, type, -1);
        }

        public void process(String input, int type, int backtype)
        {
                String inputCopy = new String(input);
                String extraSelection = null;
                long currentAccountId = AuroraPrivacyUtils.getCurrentAccountId();
                if (MmsApp.sHasPrivacyFeature) {
                        extraSelection = " AND is_privacy >= 0";
                }
                if (input != null) {
                        input = input.replace("'", "''");
                }
                Cursor cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.DISPLAY_NAME + " = '" + input + "'" + extraSelection, null, " is_privacy desc ");

                //no record exits,so assume it as a number and query again
                if(cs == null || cs.getCount() == 0)
                {
                        if (MmsApp.mGnRegularlyMsgSend) {
                                if (cs != null) {
                                        cs.close();
                                }
                        }
                        input = PhoneNumberUtils.getCallerIDMinMacth(input);
                        if (input != null && input.length() > 0) {
                                cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.NUMBER + " LIKE '%" + input + "' "+ extraSelection, null, " is_privacy desc ");
                        } else {
                                // we pretend to find nothing here if the number string is null.
                                cs = null;
                        }
                }
                Bundle idAndInputData = new Bundle();
                idAndInputData.putLong("currentAccountId", currentAccountId);
                idAndInputData.putString("inputN", input);
                if(cs != null && cs.moveToFirst())
                {
                        idAndInputData.putString("inputE", cs.getString(INDEX_NUMBER));
                        Message msg = mHandler.obtainMessage(UPDATE_EXIST_EDIT_CONTACTS);
                        msg.obj = cs;
                        msg.arg1 = type;
                        msg.arg2 = backtype;
                        msg.setData(idAndInputData);
                        msg.sendToTarget();
                } else {
                        idAndInputData.putString("inputN", inputCopy);
                        Message msg = mHandler.obtainMessage(UPDATE_NEW_EDIT_CONTACTS);
                        msg.obj = cs;
                        msg.arg1 = type;
                        msg.arg2 = backtype;
                        msg.setData(idAndInputData);
                        msg.sendToTarget();
                }
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

        public int getRecipientCount()
        {
                return mContactInfoList.isEmpty()? 0 : mContactInfoList.size();
        }

        private boolean mIsAddingRecipient = false;

        public void setAddingRecipient(boolean param) {
                mIsAddingRecipient = param;
        }

        public boolean  isAddingRecipient() {
                return mIsAddingRecipient;
        }

        public void setNumbers(List<String> numberList) {
                this.mNumberList = numberList;
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

        public List<String> getNumbersAndPrivacyWith1C() {
                List<String> numberList = new ArrayList<String>();

                for (ContactInfo ci : mContactInfoList)
                {
                        numberList.add(ci.number + String.valueOf('\1') + ci.privacy);
                }

                return numberList;
        }

        public List<String> getNumbersAndPrivacy() {
                List<String> numberList = new ArrayList<String>();

                for (ContactInfo ci : mContactInfoList)
                {
                        numberList.add(ci.number + ":" + ci.privacy);
                }

                return numberList;
        }

        private void addNewContact(String input)
        {
                String inputCopy = new String(input);
                if (input != null) {
                        input = input.replace("'", "''");
                }
                Cursor cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.DISPLAY_NAME + " = '" + input + "'", null, null);
                if(cs == null || cs.getCount() == 0)
                {
                        if (cs != null) {
                                cs.close();
                        }
                        input = PhoneNumberUtils.getCallerIDMinMacth(input);
                        if (input != null && input.length() > 0) {
                                cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.NUMBER + " LIKE '%" + input + "'", null, null);
                        } else {
                                // we pretend to find nothing here if the number string is null.
                                cs = null;
                        }
                }

                ContactInfo ci = new ContactInfo();
                ci.hasBeenSelected = false;

                if(cs != null && cs.moveToFirst())
                {
                        ci.displayName = cs.getString(INDEX_NAME);
                        ci.number = cs.getString(INDEX_NUMBER);
                }
                else
                {
                        ci.displayName = inputCopy;
                        ci.number = inputCopy;
                }

                cs.close();

                ci.privacy = "0";
                int result = hasExistsInContactList(ci.number, ci.privacy);
                if (result == NUMBER_AND_PRIVACY_EQUAL) {
                        return;
                }
                mContactInfoList.add(ci);
                mNumberMap.put(ci.number, ci);
                LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_item, AuroraContactCollection.this, false);
                TextView tv = (TextView)ll.findViewById(R.id.contact_item);
                tv.setTag(ci);
                tv.setText(ci.displayName);
                tv.setOnClickListener(AuroraContactCollection.this);
                addView(ll);

                if(mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();
        }

        public void addNewContact(String name, String number, String privacy) {
                ContactInfo ci = new ContactInfo();
                ci.hasBeenSelected = false;
                ci.displayName = name;
                ci.number = number;
                ci.privacy = privacy;
                int result = hasExistsInContactList(ci.number, ci.privacy);
                if (result == NUMBER_AND_PRIVACY_EQUAL) {
                        return;
                }
                mContactInfoList.add(ci);
                mNumberMap.put(ci.number, ci);
                LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_item, AuroraContactCollection.this, false);
                TextView tv = (TextView)ll.findViewById(R.id.contact_item);
                setSelectedState(ll, false);
                tv.setTag(ci);
                tv.setText(ci.displayName);
                tv.setOnClickListener(AuroraContactCollection.this);
                addView(ll);

                if(mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();
        }

        public void addNewContact(String name, String number)
        {
                ContactInfo ci = new ContactInfo();
                ci.hasBeenSelected = false;
                ci.displayName = name;
                ci.number = number;
                ci.privacy = "0";
                int result = hasExistsInContactList(ci.number, ci.privacy);
                if (result == NUMBER_AND_PRIVACY_EQUAL) {
                        return;
                }
                mContactInfoList.add(ci);
                mNumberMap.put(ci.number, ci);
                LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_item, AuroraContactCollection.this, false);
                TextView tv = (TextView)ll.findViewById(R.id.contact_item);
                setSelectedState(ll, false);
                tv.setTag(ci);
                tv.setText(ci.displayName);
                tv.setOnClickListener(AuroraContactCollection.this);
                addView(ll);

                if(mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();
        }

        public void addNewContact(ContactList list)
        {
                //We add contact directly rather than calling inner method such as addNewContact
                //as this method will call listener every single time.so after we finished contact adding
                //then call listener once

                for (Contact c : list) {

                        //Instead of calling addNewContact method,create and add ContactInfo directly
                        ContactInfo ci = new ContactInfo();
                        ci.hasBeenSelected = false;
                        ci.displayName = c.getName();
                        ci.number = c.getNumber();
                        ci.privacy = String.valueOf(c.getPrivacy());
                        int result = hasExistsInContactList(ci.number, ci.privacy);
                        if (result == NUMBER_AND_PRIVACY_EQUAL) {
                                continue;
                        }
                        mContactInfoList.add(ci);
                        mNumberMap.put(ci.number, ci);
                        LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_item, AuroraContactCollection.this, false);
                        TextView tv = (TextView)ll.findViewById(R.id.contact_item);
                        setSelectedState(ll, false);
                        tv.setTag(ci);
                        tv.setText(ci.displayName);
                        tv.setOnClickListener(AuroraContactCollection.this);
                        addView(ll);
                }

                if(mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();
        }

        public ContactList constructContactsFromInput(boolean blocking) {
                ContactList list = new ContactList();

                for (ContactInfo ci : mContactInfoList)
                {
                        Contact contact = null;
                        if (MmsApp.sHasPrivacyFeature) {
                                contact = Contact.get(ci.number, blocking, Long.parseLong(ci.privacy));
                        } else {
                                contact = Contact.get(ci.number, blocking);
                        }
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

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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
                for (int i=0; i<count; ++i)
                {
                        final View child = getChildAt(i);
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
                        child.layout(curX, curY + 3, rightPos, curY + childHeight);
                        curX += childWidth + mHorizonInterval;
                }
        }

        public void initContainer(ContactList list)
        {
                reset();
                for (Contact c : list) {
                        //Instead of calling addNewContact method,create and add ContactInfo directly
                        ContactInfo ci = new ContactInfo();
                        ci.hasBeenSelected = false;
                        ci.displayName = c.getName();
                        ci.number = c.getNumber();
                        ci.privacy = String.valueOf(c.getPrivacy());
                        int result = hasExistsInContactList(ci.number, ci.privacy);
                        if (result == NUMBER_AND_PRIVACY_EQUAL) {
                                continue;
                        }
                        mContactInfoList.add(ci);
                        mNumberMap.put(ci.number, ci);
                        insertOrUpdatePrivacy(ci.number, ci.privacy);
                        LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_item, AuroraContactCollection.this, false);
                        TextView tv = (TextView)ll.findViewById(R.id.contact_item);
                        setSelectedState(ll, false);
                        tv.setTag(ci);
                        tv.setText(ci.displayName);
                        tv.setOnClickListener(AuroraContactCollection.this);
                        addView(ll);
                }

                if(mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();
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

        private void setSelectedState(View v, boolean state) {
                if (state) {
                        ((LinearLayout)v).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.aurora_added_contact_bg));
                        ((TextView)v.findViewById(R.id.contact_item)).setTextColor(Color.WHITE);
                } else {
                        ((LinearLayout)v).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.aurora_not_added_contact_bg));
                        ((TextView)v.findViewById(R.id.contact_item)).setTextColor(mContext.getResources().getColor(R.color.aurora_contactcollection_select_color));
                }
        }

        private boolean mIsDeleteMode;

        public void onClick(View v) {
                String number = ((ContactInfo)v.getTag()).number;
                if (mContactInfoList != null) {
                        for (ContactInfo ci : mContactInfoList) {
                                if (ci.number.equals(number)) {
                                        if (!ci.hasBeenSelected) {
                                                setSelectedState((LinearLayout) (v.getParent()), true);
                                                Message msg = Message.obtain(mHandler, ComposeMessageActivity.ADD_RECENT_CONTACT);
                                                msg.obj = v.getTag();
                                                msg.sendToTarget();
                                        }
                                }
                        }
                }
                onLayout(false, 0, 0, getLayoutParams().width, getLayoutParams().height);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                // TODO Auto-generated method stub
                mWidthMeasureSpec = widthMeasureSpec;
                mHeightMeasureSpec = heightMeasureSpec;

                int heightSize = mPaddingTop + mPaddingBottom;
                heightSize = Math.max(heightSize, getSuggestedMinimumHeight());
                final int desiredHeight = resolveSize(heightSize, heightMeasureSpec);

                int widthSize = mPaddingLeft + mPaddingRight;
                widthSize = Math.max(widthSize, getSuggestedMinimumWidth());
                final int desiredWidth = resolveSize(widthSize, widthMeasureSpec);

                int totalHeight = mPaddingTop + mPaddingBottom;
                final int maxRightPos = desiredWidth - mPaddingLeft;
                final int childMaxWidth = maxRightPos - mPaddingRight;

                int x = mPaddingLeft;
                int lines = 1;
                int childHeight = 0 ;
                int childWidth = 0;

                final int childCount = getChildCount();
                for(int i = 0; i < childCount; ++i)
                {
                        View child = getChildAt(i);

                        //As we always place other child first and finally put edit text view in onLayout method,
                        //so we sync the sequence of the measure operation with layout operation
                        measureChild(child, widthMeasureSpec, heightMeasureSpec);
                        childWidth = getChildAt(i).getMeasuredWidth();
                        childHeight = getChildAt(i).getMeasuredHeight();
                        childWidth = Math.min(childWidth, childMaxWidth);
                        x += childWidth;
                        if(x > maxRightPos)
                        {
                                x = mPaddingLeft + childWidth;
                                if (maxRightPos > 0) {
                                        ++lines;
                                }
                        }
                        x += mHorizonInterval;
                }
                // Aurora xuyong 2015-12-30 deleted for aurora 2.0 new feature start
                //x += childMaxWidth;
                // Aurora xuyong 2015-12-30 deleted for aurora 2.0 new feature end
                if(x > maxRightPos)
                {
                        if (maxRightPos > 0) {
                                ++lines;
                        }
                }
                //As the height of edit text may be greater than other text view,so plus it in separate way
                totalHeight += (lines-1)*childHeight + (lines - 1)*mVerticalInterval + childHeight;
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
                                        + childHeight;
                        }
                        // Aurora xuyong 2014-08-13 modified for aurora's new feature start
                        if (maxRightPos > 0) {
                                mLinesWatcher.linesChanged(lines);
                        }
                        // Aurora xuyong 2014-08-13 modified for aurora's new feature end
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

        public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                return true;
        }

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

        public interface ContactsChangeListener
        {
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

        private View mClikedButton;
        private boolean mNeedNotifyClickSend;

        public boolean haveRecipients() {
                if (!mContactInfoList.isEmpty()) {
                        Log.i(TAG, "haveRecipients return true");
                        return true;
                }

                Log.i(TAG, "haveRecipients return false");
                return false;
        }

        public interface TouchedEventListener
        {
                void containerTouched();
        }

        public TouchedEventListener mTouchedLsn = null;

        public void setTouchedEventListener(TouchedEventListener l)
        {
                mTouchedLsn = l;
        }

        public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub

                return false;
        }

        public void resetMode() {
        }

        public void addNewContact(String name, String number, String privacy, boolean notify) {
                ContactInfo ci = new ContactInfo();
                ci.hasBeenSelected = false;
                ci.displayName = name;
                ci.number = number;
                ci.privacy = privacy;
                int result = hasExistsInContactList(ci.number, ci.privacy);
                if (result == NUMBER_AND_PRIVACY_EQUAL) {
                        return;
                }
                mContactInfoList.add(ci);
                mNumberMap.put(ci.number, ci);
                LinearLayout ll = (LinearLayout)mInflater.inflate(R.layout.aurora_contact_item, AuroraContactCollection.this, false);
                TextView tv = (TextView)ll.findViewById(R.id.contact_item);
                setSelectedState(ll, false);
                tv.setTag(ci);
                tv.setText(ci.displayName);
                tv.setOnClickListener(AuroraContactCollection.this);
                addView(ll);

                if (mContactsChangeListener != null && notify)
                        mContactsChangeListener.contactsChanged();
        }

        public void removeLastContact(int lastcount) {
                int count = getChildCount();
                int i = 0;
                while (--count >= 0) {
                        View child = getChildAt(count);
                        ContactInfo ci = (ContactInfo) child.findViewById(R.id.contact_item).getTag();
                        removeView(child);
                        mContactInfoList.remove(ci);
                        mNumberMap.remove(ci.number);
                        i++;
                        if (i == lastcount)
                                break;

                }
                if (mContactsChangeListener != null)
                        mContactsChangeListener.contactsChanged();
        }

        public void updateAddContact(String number) {
                if (number != null) {
                        number = number.replace("'", "''");
                }
                Cursor cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.DISPLAY_NAME + " = '" + number + "'", null, null);
                if(cs == null || cs.getCount() == 0)
                {
                        number = PhoneNumberUtils.getCallerIDMinMacth(number);
                        if (number != null && number.length() > 0) {
                                cs = mCR.query(Phone.CONTENT_URI, PROJECTION_PHONE, Phone.NUMBER + " LIKE '%" + number + "'",  null, null);
                        } else {
                                cs = null;
                        }
                }
                if(cs != null && cs.moveToFirst())
                {
                        ContactInfo c = new ContactInfo();
                        c.hasBeenSelected = false;
                        for (ContactInfo ci : mContactInfoList) {
                                if (ci.number.equals(cs.getString(INDEX_NUMBER))) {
                                        mContactInfoList.remove(ci);
                                        c.displayName = cs.getString(INDEX_NAME);
                                        c.number = cs.getString(INDEX_NUMBER);
                                        ci.privacy = cs.getString(INDEX_PRIVACY);
                                        int result = hasExistsInContactList(ci.number, ci.privacy);
                                        if (result == NUMBER_AND_PRIVACY_EQUAL) {
                                                continue;
                                        }
                                        mContactInfoList.add(c);
                                        mNumberMap.put(cs.getString(INDEX_NUMBER), c);
                                        break;
                                }
                        }
                        int count =  getChildCount();
                        while(--count >= 0)
                        {
                                View child = getChildAt(count);
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
                if (cs != null) {
                        cs.close();
                }
        }

        public void clearmNumberMap() {
                if (mNumberMap != null) {
                        mNumberMap.clear();
                }
        }

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
        // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature start
        private boolean mHasSync = false;

        public void reSyncContact(ArrayList<ContactInfo> contactList) {
            if (!mHasSync) {
                    for (ContactInfo contact : contactList) {
                            syncContact(contact);
                    }
            }
        }
        // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature end
        @Override
        public void syncContact(ContactInfo contact) {
            for (ContactInfo info : mContactInfoList) {
                    // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature start
                    mHasSync = true;
                    // Aurora xuyong 2016-01-19 added for aurora 2.0 new feature end
                    if (info.number.equals(contact.number)) {
                            int childCount = AuroraContactCollection.this.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                    TextView v = (TextView)(AuroraContactCollection.this.getChildAt(i).findViewById(R.id.contact_item));
                                    ContactInfo contactInfo = (ContactInfo)v.getTag();
                                    if (contactInfo.getNumber().equals(info.number)) {
                                            setSelectedState((LinearLayout) v.getParent(), !contactInfo.hasBeenSelected());
                                            contactInfo.setHasBeenSelected(!contactInfo.hasBeenSelected());
                                    }
                            }
                    }
            }
        }

}
