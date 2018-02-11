/**
 * Copyright (c) 2009, Google Inc.
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
 *//*

package com.android.mms.ui;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;

import android.provider.Telephony;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import aurora.widget.AuroraListView;
import android.widget.TextView;

import com.android.mms.data.Contact;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.util.DraftCache;

import gionee.provider.GnTelephony.Threads;
import android.util.Log;
import com.aurora.featureoption.FeatureOption;

*//***
 * Presents a List of search results.  Each item in the list represents a thread which
 * matches.  The item contains the contact (or phone number) as the "title" and a
 * snippet of what matches, below.  The snippet is taken from the most recent part of
 * the conversation that has a match.  Each match within the visible portion of the
 * snippet is highlighted.
 *//*
// Aurora liugj 2013-10-25 modified for fix bug-241 
public class SearchActivity extends AuroraListActivity implements DraftCache.OnDraftChangedListener {
    private AsyncQueryHandler mQueryHandler;

    // Track which TextView's show which Contact objects so that we can update
    // appropriately when the Contact gets fully loaded.
    private HashMap<Contact, TextView> mContactMap = new HashMap<Contact, TextView>();


    
     * Subclass of TextView which displays a snippet of text which matches the full text and
     * highlights the matches within the snippet.
     
    private static final String WP_TAG = "Mms/WapPush";
    private static final String TAG = "Mms/SearchActivity";
    private String searchString;
    private boolean searchFirst = false;
    public static class TextViewSnippet extends TextView {
        private static String sEllipsis = "\u2026";

        private static int sTypefaceHighlight = Typeface.BOLD;

        private String mFullText;
        private String mTargetString;
        private Pattern mPattern;

        public TextViewSnippet(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public TextViewSnippet(Context context) {
            super(context);
        }

        public TextViewSnippet(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        *//**
         * We have to know our width before we can compute the snippet string.  Do that
         * here and then defer to super for whatever work is normally done.
         *//*
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            String fullTextLower = mFullText.toLowerCase();
            String targetStringLower = mTargetString.toLowerCase();

            int startPos = 0;
            int searchStringLength = targetStringLower.length();
            int bodyLength = fullTextLower.length();

            Matcher m = mPattern.matcher(mFullText);
            if (m.find(0)) {
                startPos = m.start();
            }

            TextPaint tp = getPaint();

            float searchStringWidth = tp.measureText(mTargetString);
            float textFieldWidth = getWidth();

            String snippetString = null;
            if (searchStringWidth > textFieldWidth) {
                // gionee zhouyj 2012-07-26 add for CR00657661 start 
                if (MmsApp.mGnMessageSupport) {
                    int end = startPos + searchStringLength <= mFullText.length()? startPos + searchStringLength : 
                        mFullText.length();
                    snippetString = mFullText.substring(startPos, end);
                } else {
                // gionee zhouyj 2012-07-26 add for CR00657661 end 
                snippetString = mFullText.substring(startPos, startPos + searchStringLength);
                // gionee zhouyj 2012-07-26 add for CR00657661 start 
                }
                // gionee zhouyj 2012-07-26 add for CR00657661 end 
            } else {
                float ellipsisWidth = tp.measureText(sEllipsis);
                textFieldWidth -= (2F * ellipsisWidth); // assume we'll need one on both ends

                int offset = -1;
                int start = -1;
                int end = -1;
                 TODO: this code could be made more efficient by only measuring the additional
                 * characters as we widen the string rather than measuring the whole new
                 * string each time.
                 
                while (true) {
                    offset += 1;

                    int newstart = Math.max(0, startPos - offset);
                    int newend = Math.min(bodyLength, startPos + searchStringLength + offset);

                    if (newstart == start && newend == end) {
                        // if we couldn't expand out any further then we're done
                        break;
                    }
                    start = newstart;
                    end = newend;

                    // pull the candidate string out of the full text rather than body
                    // because body has been toLower()'ed
                    String candidate = mFullText.substring(start, end);
                    if (tp.measureText(candidate) > textFieldWidth) {
                        // if the newly computed width would exceed our bounds then we're done
                        // do not use this "candidate"
                        break;
                    }

                    snippetString = String.format(
                            "%s%s%s",
                            start == 0 ? "" : sEllipsis,
                            candidate,
                            end == bodyLength ? "" : sEllipsis);
                }
            }

            //ginoee gaoj 2012-7-27 added for CR00658256 start
            if (MmsApp.mGnMessageSupport && snippetString == null) {
                snippetString = mFullText.substring(startPos, startPos + searchStringLength);
            }
            //ginoee gaoj 2012-7-27 added for CR00658256 end
            SpannableString spannable = new SpannableString(snippetString);
            int start = 0;

            m = mPattern.matcher(snippetString);
            while (m.find(start)) {
                spannable.setSpan(new StyleSpan(sTypefaceHighlight), m.start(), m.end(), 0);
                start = m.end();
            }
            setText(spannable);

            // do this after the call to setText() above
            super.onLayout(changed, left, top, right, bottom);
        }

        public void setText(String fullText, String target) {
            // Use a regular expression to locate the target string
            // within the full text.  The target string must be
            // found as a word start so we use \b which matches
            // word boundaries.
            String patternString = "\\b" + Pattern.quote(target);
            mPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);

            mFullText = fullText;
            mTargetString = target;
            requestLayout();
        }
    }

    Contact.UpdateListener mContactListener = new Contact.UpdateListener() {
        public void onUpdate(Contact updated) {
            TextView tv = mContactMap.get(updated);
            if (tv != null) {
                tv.setText(updated.getNameAndNumber());
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        searchFirst = false;
        Contact.removeListener(mContactListener);
        DraftCache.getInstance().removeOnDraftChangedListener(this);
    }

    private long getThreadId(long sourceId, long which) {
        Uri.Builder b = Uri.parse("content://mms-sms/messageIdToThread").buildUpon();
        b = b.appendQueryParameter("row_id", String.valueOf(sourceId));
        b = b.appendQueryParameter("table_to_use", String.valueOf(which));
        String s = b.build().toString();

        Cursor c = getContentResolver().query(
                Uri.parse(s),
                null,
                null,
                null,
                null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getLong(c.getColumnIndex("thread_id"));
                }
            } finally {
                c.close();
            }
        }
        return -1;
    }

    @Override
    public void onCreate(Bundle icicle) {
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        super.onCreate(icicle);
        Log.d(TAG,"onCreate");
        searchString = getSearchString();

        // If we're being launched with a source_id then just go to that particular thread.
        // Work around the fact that suggestions can only launch the search activity, not some
        // arbitrary activity (such as ComposeMessageActivity).
        final Uri u = getIntent().getData();
        if (u != null && u.getQueryParameter("source_id") != null) {
            gotoComposeMessageActivity(u);
            return;
        }

        setContentView(R.layout.search_activity);
        ContentResolver cr = getContentResolver();

        final AuroraListView listView = getListView();
        listView.setItemsCanFocus(true);
        listView.setFocusable(true);
        listView.setClickable(true);
        final TextView tv = (TextView)findViewById(android.R.id.empty);
        if (MmsApp.mDarkStyle) {
            tv.setTextColor(getResources().getColor(R.color.gn_dark_color_bg));
        }
        tv.setText(getString(R.string.menu_search) + "...");
        // I considered something like "searching..." but typically it will
        // flash on the screen briefly which I found to be more distracting
        // than beneficial.
        // This gets updated when the query completes.
        setTitle("");

        Contact.addListener(mContactListener);

        // When the query completes cons up a new adapter and set our list adapter to that.
        mQueryHandler = new AsyncQueryHandler(cr) {
            protected void onQueryComplete(int token, Object cookie, Cursor c) {
                if (c == null) {
                    return;
                }
                final int threadIdPos = c.getColumnIndex("thread_id");
                final int addressPos  = c.getColumnIndex("address");
                final int bodyPos     = c.getColumnIndex("body");
                final int rowidPos    = c.getColumnIndex("_id");
                final int msgTypePos;
                final int msgBoxPos;
                if(c.getColumnIndex("msg_type") > 0){
                    msgTypePos = c.getColumnIndex("msg_type");
                } else {
                    msgTypePos = 0;
                }
                if(c.getColumnIndex("msg_box") > 0){
                    msgBoxPos = c.getColumnIndex("msg_box");
                } else {
                    msgBoxPos = 0;
                }
                Log.d(TAG, "onQueryComplete msgTypePos = " + msgTypePos);
                int cursorCount = c.getCount();
                // gionee zhouyj 2012-11-08 add for CR00724694 start 
                if (MmsApp.mGnMessageSupport && MmsApp.mIsSafeModeSupport) {
                    Log.d(TAG, "search in safe mode");
                    cursorCount = 0;
                    setTitle(getResources().getQuantityString(
                            R.plurals.search_results_title,
                            cursorCount,
                            cursorCount,
                            searchString));
                    tv.setText(getString(R.string.search_empty));
                    tv.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    return ;
                }
                // gionee zhouyj 2012-11-08 add for CR00724694 end 
                Log.d(TAG, "cursorCount =: " + cursorCount);
                setTitle(getResources().getQuantityString(
                        R.plurals.search_results_title,
                        cursorCount,
                        cursorCount,
                        searchString));
                if (cursorCount == 0){
                    tv.setText(getString(R.string.search_empty));
                }
                // Note that we're telling the CursorAdapter not to do auto-requeries. If we
                // want to dynamically respond to changes in the search results,
                // we'll have have to add a setOnDataSetChangedListener().
                setListAdapter(new CursorAdapter(SearchActivity.this,
                        c, false  no auto-requery ) {
                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        final TextView title = (TextView)(view.findViewById(R.id.title));
                        final TextViewSnippet snippet = (TextViewSnippet)(view.findViewById(R.id.subtitle));

                        String address = cursor.getString(addressPos);
                        // gionee zhouyj 2012-06-21 add for CR00626562 start 
                        if(MmsApp.mGnMessageSupport && (address == null || address.equals("gn_draft_address_token") || address.equals(""))) {
                            address = SearchActivity.this.getString(R.string.gn_draft_operation);
                        }
                        // gionee zhouyj 2012-06-21 add for CR00626562 end 
                        Contact contact = address != null ? Contact.get(address, false) : null;

                        String titleString = contact != null ? contact.getNameAndNumber() : "";
                        title.setText(titleString);

                        snippet.setText(cursor.getString(bodyPos), searchString);

                        // if the user touches the item then launch the compose message
                        // activity with some extra parameters to highlight the search
                        // results and scroll to the latest part of the conversation
                        // that has a match.
                        final long threadId = cursor.getLong(threadIdPos);
                        final long rowid = cursor.getLong(rowidPos);
                        final int msgType = cursor.getInt(msgTypePos);
                        final int msgBox = cursor.getInt(msgBoxPos);
                        Log.d(TAG, "onQueryComplete msgType = " + msgType + "rowid =" + rowid);

                        view.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent onClickIntent= null;
                                if(FeatureOption.MTK_WAPPUSH_SUPPORT == true){
                                    //wappush: add type
                                    int threadType = 0;
                                    String where = Threads._ID + "=" + threadId;
                                    String[] projection = new String[] { Threads.TYPE };
                                    Cursor queryCursor = getContentResolver().query(
                                            Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build(),
                                            projection, where, null, null);
                                    if(null != queryCursor){
                                        try {
                                            if (queryCursor.moveToFirst()) {
                                                threadType = queryCursor.getInt(0);
                                                Log.i(WP_TAG, "SearchActivity: " + "threadType is : " + threadType);
                                            }
                                        } finally {
                                            queryCursor.close();
                                        }
                                    }else{
                                        Log.w(WP_TAG, "SearchActivity: " + "Thread Id queryCursor is null.");
                                    }
                                    boolean dirMode = MmsConfig.getMmsDirMode();
                                    Log.d(TAG, "onClickIntent1 dirMode =" + dirMode);
                                    if (!dirMode) {
                                        if(Threads.WAPPUSH_THREAD == threadType){
                                            Log.i(WP_TAG, "SearchActivity: " + "onClickIntent WPMessageActivity.");
                                            onClickIntent = new Intent(SearchActivity.this, WPMessageActivity.class);
                                        }else{
                                            Log.i(WP_TAG, "SearchActivity: " + "onClickIntent ComposeMessageActivity.");
                                            onClickIntent = new Intent(SearchActivity.this, ComposeMessageActivity.class);
                                        }
                                    } else {
                                        onClickIntent = new Intent();
                                        if (msgType == 1) {
                                            if (msgBox == 3){//draft
                                                onClickIntent = new Intent(SearchActivity.this, ComposeMessageActivity.class);
                                            } else {
                                                final Uri SMS_URI = Uri.parse("content://sms/");
                                                onClickIntent.setClass(SearchActivity.this,FolderModeSmsViewer.class);
                                                onClickIntent.setData(ContentUris.withAppendedId(SMS_URI,rowid));
                                                onClickIntent.putExtra("msg_type", 1);
                                            }
                                        } else if (msgType == 2) {
                                            if (msgBox == 3){//draft
                                                  onClickIntent = new Intent(SearchActivity.this, ComposeMessageActivity.class);
                                              } else {
                                                  final Uri MMS_URI = Uri.parse("content://mms/");
                                                  onClickIntent.setClass(SearchActivity.this,MmsPlayerActivity.class);
                                                  onClickIntent.setData(ContentUris.withAppendedId(MMS_URI,rowid));
                                                  onClickIntent.putExtra("dirmode", true);
                                              }
                                        } else if (msgType == 3) {
                                            final Uri WP_URI = Uri.parse("content://wappush/");
                                            onClickIntent.setClass(SearchActivity.this,FolderModeSmsViewer.class);
                                            onClickIntent.setData(ContentUris.withAppendedId(WP_URI,rowid));
                                            onClickIntent.putExtra("msg_type", 3);
                                        } else if (msgType == 4) {
                                            final Uri CB_URI = Uri.parse("content://cb/messages/");
                                            onClickIntent.setClass(SearchActivity.this,FolderModeSmsViewer.class);
                                            onClickIntent.setData(ContentUris.withAppendedId(CB_URI,rowid));
                                            onClickIntent.putExtra("msg_type", 4);
                                        }
                                    }
                                }else{
                                    boolean dirMode = MmsConfig.getMmsDirMode();
                                    Log.d(TAG, "onClickIntent2 dirMode =" + dirMode);
                                    if (!dirMode){
                                        onClickIntent = new Intent(SearchActivity.this, ComposeMessageActivity.class);
                                    } else {
                                        Log.d(TAG, "onClickIntent2 msgType =" + msgType);
                                        onClickIntent = new Intent();
                                        if (msgType == 1) {
                                            if (msgBox == 3){//draft
                                                onClickIntent = new Intent(SearchActivity.this, ComposeMessageActivity.class);
                                            } else {
                                                final Uri SMS_URI = Uri.parse("content://sms/");
                                                onClickIntent.setClass(SearchActivity.this,FolderModeSmsViewer.class);
                                                onClickIntent.setData(ContentUris.withAppendedId(SMS_URI,rowid));
                                                onClickIntent.putExtra("msg_type", 1);
                                            }
                                        } else if (msgType == 2) {
                                              if (msgBox == 3){//draft
                                                  onClickIntent = new Intent(SearchActivity.this, ComposeMessageActivity.class);
                                              } else {
                                                  final Uri MMS_URI = Uri.parse("content://mms/");
                                                  onClickIntent.setClass(SearchActivity.this,MmsPlayerActivity.class);
                                                  onClickIntent.setData(ContentUris.withAppendedId(MMS_URI,rowid));
                                                  onClickIntent.putExtra("dirmode", true);
                                              }
                                        } else if (msgType == 4) {
                                            final Uri CB_URI = Uri.parse("content://cb/messages/");
                                            onClickIntent.setClass(SearchActivity.this,FolderModeSmsViewer.class);
                                            onClickIntent.setData(ContentUris.withAppendedId(CB_URI,rowid));
                                            onClickIntent.putExtra("msg_type", 4);
                                        }
                                    }
                                }                                
                                onClickIntent.putExtra("thread_id", threadId);
                                onClickIntent.putExtra("highlight", searchString);
                                onClickIntent.putExtra("select_id", rowid);
                                startActivity(onClickIntent);
                            }
                        });
                    }

                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        LayoutInflater inflater = LayoutInflater.from(context);
                        View v = inflater.inflate(R.layout.search_item, parent, false);
                        return v;
                    }

                });

                // AuroraListView seems to want to reject the setFocusable until such time
                // as the list is not empty.  Set it here and request focus.  Without
                // this the arrow keys (and trackball) fail to move the selection.
                listView.setFocusable(true);
                listView.setFocusableInTouchMode(true);
                listView.requestFocus();

                // Remember the query if there are actual results
                if (cursorCount > 0) {
                    SearchRecentSuggestions recent = ((MmsApp)getApplication()).getRecentSuggestions();
                    if (recent != null) {
                        recent.saveRecentQuery(searchString, getString(R.string.search_history, cursorCount, searchString));
                    }
                }
            }
        };

        boolean dirMode = MmsConfig.getMmsDirMode();
        // don't pass a projection since the search uri ignores it
        Uri uri = null;
        if (!dirMode){
            uri = Telephony.MmsSms.SEARCH_URI.buildUpon().appendQueryParameter("pattern", searchString).build();
        } else {
            uri = Uri.parse("content://mms-sms/searchFolder").buildUpon().appendQueryParameter("pattern", searchString).build();
        }

        // kick off a query for the threads which match the search string
        mQueryHandler.startQuery(0, null, uri, null, null, null, null);
        searchFirst = true;
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        actionBar.setDisplayHomeAsUpEnabled(true);
        //gionee gaoj 2012-5-29 added for CR00555790 start
        if (MmsApp.mGnMessageSupport) {
            actionBar.setDisplayShowHomeEnabled(false);
        }
        //gionee gaoj 2012-5-29 added for CR00555790 end
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // The user clicked on the Messaging icon in the action bar. Take them back from
                // wherever they came from
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        setIntent(intent);

        searchString = getSearchString();
        // If we're being launched with a source_id then just go to that particular thread.
        // Work around the fact that suggestions can only launch the search activity, not some
        // arbitrary activity (such as ComposeMessageActivity).
        final Uri u = getIntent().getData();
        if (u != null && u.getQueryParameter("source_id") != null) {
            gotoComposeMessageActivity(u);
            return;
        }

        searchFirst = false;
        super.onNewIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //Gionee <zhouyj> <2013-04-28> add for CR00801667 begin
        if (MmsApp.mGnMessageSupport && MmsApp.mIsSafeModeSupport) {
            setTitle(getResources().getQuantityString(
                    R.plurals.search_results_title, 0, 0, searchString));
            TextView tv = (TextView) findViewById(android.R.id.empty);
            tv.setText(getString(R.string.search_empty));
            tv.setVisibility(View.VISIBLE);
            getListView().setVisibility(View.GONE);
            return ;
        }
        //Gionee <zhouyj> <2013-04-28> add for CR00801667 end
        if (!searchFirst) {
        boolean dirMode = MmsConfig.getMmsDirMode();
        // don't pass a projection since the search uri ignores it
        Uri uri = null;
        if (!dirMode){
            uri = Telephony.MmsSms.SEARCH_URI.buildUpon().appendQueryParameter("pattern", searchString).build();
        } else {
            uri = Uri.parse("content://mms-sms/searchFolder").buildUpon().appendQueryParameter("pattern", searchString).build();
        }
        mQueryHandler.startQuery(0, null, uri, null, null, null, null);
            searchFirst = true;
            DraftCache.getInstance().addOnDraftChangedListener(this);
        }

    }

    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        Log.d(TAG, "onDraftChanged hasDraft = " + hasDraft + " threadId = " + threadId); 
        mQueryHandler.postDelayed(new Runnable() {
            public void run() {
                boolean dirMode = MmsConfig.getMmsDirMode();
                // don't pass a projection since the search uri ignores it
                Uri uri = null;
                if (!dirMode){
                    uri = Telephony.MmsSms.SEARCH_URI.buildUpon().appendQueryParameter("pattern", searchString).build();
                } else {
                    uri = Uri.parse("content://mms-sms/searchFolder").buildUpon().appendQueryParameter("pattern", searchString).build();
                }
                mQueryHandler.startQuery(0, null, uri, null, null, null, null);
            }
        }, 100);
    }

    private void gotoComposeMessageActivity(final Uri u) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    long sourceId = Long.parseLong(u.getQueryParameter("source_id"));
                    long whichTable = Long.parseLong(u.getQueryParameter("which_table"));
                    long threadId = getThreadId(sourceId, whichTable);

                    final Intent onClickIntent = new Intent(SearchActivity.this,
                            ComposeMessageActivity.class);
                    onClickIntent.putExtra("highlight", searchString);
                    onClickIntent.putExtra("select_id", sourceId);
                    onClickIntent.putExtra("thread_id", threadId);
                    startActivity(onClickIntent);
                    finish();
                    return;
                } catch (NumberFormatException ex) {
                    // ok, we do not have a thread id so continue
                }
            }
        });
        t.start();
    }

    private String getSearchString() {
        String searchStringParameter = getIntent().getStringExtra(SearchManager.QUERY);
        if (searchStringParameter == null) {
            searchStringParameter = getIntent().getStringExtra("intent_extra_data_key" SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
        }
        return searchStringParameter != null ? searchStringParameter.trim() : searchStringParameter;
    }
}
*/