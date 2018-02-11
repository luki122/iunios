/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.browser;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BrowserContract;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.browser.UI.ComboViews;
import com.android.browser.provider.BrowserProvider2.OmniboxSuggestions;
import com.android.browser.search.SearchEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * adapter to wrap multiple cursors for url/search completions
 */
public class SuggestionsAdapter extends BaseAdapter implements Filterable,
        OnClickListener {

//    public static final int TYPE_BOOKMARK = 0;
//    public static final int TYPE_HISTORY = 1;
//    public static final int TYPE_SUGGEST_URL = 2;
//    public static final int TYPE_SEARCH = 3;
//    public static final int TYPE_SUGGEST = 4;
//    public static final int TYPE_PROSET = 5;
    
	
	public static final int TYPE_MY_SEARCH = 0;
	public static final int TYPE_SUGGEST = 1;
    public static final int TYPE_BOOKMARK = 2;
    public static final int TYPE_HISTORY = 3;
    public static final int TYPE_SUGGEST_URL = 4;
    public static final int TYPE_SEARCH = 5;
    public static final int TYPE_PROSET = 6;

    private static final String[] COMBINED_PROJECTION = {
            OmniboxSuggestions._ID,
            OmniboxSuggestions.TITLE,
            OmniboxSuggestions.URL,           
            OmniboxSuggestions.IS_BOOKMARK,
            OmniboxSuggestions.FOLDER
            };

    private static final String COMBINED_SELECTION =
            "(url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ? OR title LIKE ?)";

    final Context mContext;
    final Filter mFilter;
    SuggestionResults mMixedResults;
    List<SuggestItem> mSuggestResults, mFilterResults;
    List<CursorSource> mSources;
    boolean mLandscapeMode;
    final CompletionListener mListener;
    final int mLinesPortrait;
    final int mLinesLandscape;
    final Object mResultsLock = new Object();
    boolean mIncognitoMode=true;
    BrowserSettings mSettings;
    UrlInputView urlInputView;
    int idex=0;
    interface CompletionListener {

        public void onSearch(String txt);

        public void onSelect(String txt, int type, String extraData);
        
        public void onBookmarkOrHistory(ComboViews comboView);

    }

    public SuggestionsAdapter(Context ctx, CompletionListener listener,UrlInputView urlInputView) {
        mContext = ctx;
        this.urlInputView= urlInputView;
        mSettings = BrowserSettings.getInstance();
        mListener = listener;
        mLinesPortrait = mContext.getResources().
                getInteger(R.integer.max_suggest_lines_portrait);
        mLinesLandscape = mContext.getResources().
                getInteger(R.integer.max_suggest_lines_landscape);
        Log.i("browser", "SuggestionsAdapter-----0");
        mFilter = new SuggestFilter();
        Log.i("browser", "SuggestionsAdapter-----");
        addSource(new CombinedCursor());
    }

    public void setLandscapeMode(boolean mode) {
        mLandscapeMode = mode;
        notifyDataSetChanged();
    }

    public void addSource(CursorSource c) {
        if (mSources == null) {
            mSources = new ArrayList<CursorSource>(5);
        }
        mSources.add(c);
    }

    @Override
    public void onClick(View v) {
        SuggestItem item = (SuggestItem) ((View) v.getParent().getParent()).getTag();
        
        switch (v.getId()) {
		case R.id.imageView1:
			mListener.onBookmarkOrHistory(ComboViews.Bookmarks);
			break;

		case R.id.imageView2:
			mListener.onBookmarkOrHistory(ComboViews.History);
			break;
			
		case R.id.icon2:
			mListener.onSearch(getSuggestionUrl(item));
			break;
			
		default:
			mListener.onSelect(getSuggestionUrl(item), item.type, item.extra);
			break;
		}
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public int getCount() {
        return (mMixedResults == null) ? 0 : mMixedResults.getLineCount();
    }

    @Override
    public SuggestItem getItem(int position) {
        if (mMixedResults == null) {
            return null;
        }
        return mMixedResults.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = convertView;
       
        
        if (view == null) {
            view = inflater.inflate(R.layout.suggestion_item, parent, false);
        }
      
      
    
        bindView(view, getItem(position));
        return view;
    }

    private void bindView(View view, SuggestItem item) {
        // store item for click handling
        view.setTag(item);
        LinearLayout l1=(LinearLayout) view.findViewById(R.id.list);
        LinearLayout l2=(LinearLayout) view.findViewById(R.id.button);
        if(l1.getParent() != null) {
        	LinearLayout layout = (LinearLayout) l1.getParent();
        	LayoutParams layoutParams = layout.getLayoutParams();
        	if(Controller.screenPortrait) {
        		layoutParams.width = BaseUi.getLongerSmallerSide((Activity)mContext, false);
        	}else {
        		layoutParams.width = BaseUi.getLongerSmallerSide((Activity)mContext, true);
        	}
        	layout.setLayoutParams(layoutParams);
        }
        
        if(item.flage==1){
        	l2.setVisibility(View.VISIBLE);
        	l1.setVisibility(View.GONE);
        }else{
        	l1.setVisibility(View.VISIBLE);	
        	l2.setVisibility(View.GONE);
        }
        
        TextView tv1 = (TextView) view.findViewById(android.R.id.text1);
        TextView tv2 = (TextView) view.findViewById(android.R.id.text2);
        ImageView ic1 = (ImageView) view.findViewById(R.id.icon1);
        View ic2 = view.findViewById(R.id.icon2);
        View div = view.findViewById(R.id.divider);
    	Log.i("browser", "_____________________3_____--"+item+"   "+item.type+"________"+Html.fromHtml(item.title)+"--"+tv1);
        tv1.setText(Html.fromHtml(item.title));
        if (TextUtils.isEmpty(item.url)) {
            tv2.setVisibility(View.GONE);
            tv1.setMaxLines(2);
        } else {
            tv2.setVisibility(View.VISIBLE);
           //
            String url=item.url;
            String u=	urlInputView.getUrlText();
            if(u.contains("http://")){
            int num=u.indexOf("http://");
            if(u.length()>7){
            	 u=u.substring(num+7,u.length());		
            }
           
         	Log.i("xiexiujie55", "__________444444__________________"+u+"    "+num);
            }
            if(u.length()>0&&u.length()<=url.length()){
            	 idex=url.indexOf(u) ; 
                 
                 if(idex!=-1){
                	  SpannableString sp = new SpannableString(url);
                  	Log.i("xiexiujie55", "____________________________"+u+"    "+idex);
                  		sp.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.list_text)),idex,idex+u.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                  		tv2.setText(sp); 
                 }else {
                	 tv2.setText(item.url);
    			}
            	
            }else{
            	 tv2.setText(item.url);
            }
            
            	
    
            tv1.setMaxLines(1);
        }
        int id = -1;
        switch (item.type) {
            case TYPE_SUGGEST:
            case TYPE_SEARCH:
                id = R.drawable.search_glass;
                break;
            case TYPE_BOOKMARK:
                id = R.drawable.suggestions_bookmark;
                break;
            case TYPE_HISTORY:
                id = R.drawable.suggestions_history;
                break;
            case TYPE_SUGGEST_URL:
                id = R.drawable.ic_search_category_browser;
                break;
            case TYPE_PROSET:
                id = R.drawable.default_icon_title;
                break;
                
            case TYPE_MY_SEARCH:
            	id = R.drawable.search_glass;
            	break;
            default:
                id = -1;
        }
        if (id != -1) {
            ic1.setImageDrawable(mContext.getResources().getDrawable(id));
        }
        ic2.setVisibility(((TYPE_SUGGEST == item.type)
                || (TYPE_SEARCH == item.type))
                ? View.VISIBLE : View.GONE);
        div.setVisibility(View.GONE);
        ic2.setOnClickListener(this);
        view.findViewById(R.id.suggestion).setOnClickListener(this);
        view.findViewById(R.id.imageView1).setOnClickListener(this);
        view.findViewById(R.id.imageView2).setOnClickListener(this);
    }

    class SlowFilterTask extends AsyncTask<CharSequence, Void, List<SuggestItem>> {

        @Override
        protected List<SuggestItem> doInBackground(CharSequence... params) {
       	 Log.i("browser", "doInBackground-----");
            SuggestCursor cursor = new SuggestCursor();
            cursor.runQuery(params[0]);
            List<SuggestItem> results = new ArrayList<SuggestItem>();
            int count = cursor.getCount();
            Log.i("browser", "doInBackground--2---"+count);
            for (int i = 0; i < count; i++) {
                results.add(cursor.getItem());
                cursor.moveToNext();
            }
            cursor.close();
            return results;
        }

        @Override
        protected void onPostExecute(List<SuggestItem> items) {
        	 Log.i("browser", "onPostExecute-----");
        	
            mSuggestResults = items;
            mMixedResults = buildSuggestionResults();
            notifyDataSetChanged();
        }
    }

    SuggestionResults buildSuggestionResults() {
    	Log.i("browser", "________buildSuggestionResults________________");
        SuggestionResults mixed = new SuggestionResults();
        List<SuggestItem> filter, suggest;
        synchronized (mResultsLock) {
            filter = mFilterResults;
            suggest = mSuggestResults;
        }
        if (filter != null) {
            for (SuggestItem item : filter) {
            
                mixed.addResult(item);
            }
        }
        if (suggest != null) {
            for (SuggestItem item : suggest) {
            
                mixed.addResult(item);
            }
        }
        return mixed;
    }

    class SuggestFilter extends Filter {

        @Override
        public CharSequence convertResultToString(Object item) {
            if (item == null) {
                return "";
            }
            SuggestItem sitem = (SuggestItem) item;
            if (sitem.title != null) {
                return sitem.title;
            } else {
                return sitem.url;
            }
        }

        void startSuggestionsAsync(final CharSequence constraint) {
            if (!mIncognitoMode) {
            	 Log.i("browser", "startSuggestionsAsync-----");
                new SlowFilterTask().execute(constraint);
            }
        }

        private boolean shouldProcessEmptyQuery() {
            final SearchEngine searchEngine = mSettings.getSearchEngine();
            return searchEngine.wantsEmptyQuery();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
        	Log.i("browser", "performFiltering-----");
            FilterResults res = new FilterResults();
            if (TextUtils.isEmpty(constraint) && !shouldProcessEmptyQuery()) {
            	Log.i("browser", "performFiltering--2---");
                res.count = 0;
                res.values = null;
                return res;
            }
            startSuggestionsAsync(constraint);
            List<SuggestItem> filterResults = new ArrayList<SuggestItem>();
            if (constraint != null) {
            	Log.i("browser", "performFiltering--3---");
                for (CursorSource sc : mSources) {
                	Log.i("browser", "performFiltering--4---");
                    sc.runQuery(constraint);
                }
                mixResults(filterResults);
            }
            synchronized (mResultsLock) {
            	Log.i("browser", "performFiltering---5--");
                mFilterResults = filterResults;
            }
            SuggestionResults mixed = buildSuggestionResults();
            res.count = mixed.getLineCount();
            res.values = mixed;
            return res;
        }

        void mixResults(List<SuggestItem> results) {
        	Log.i("browser", "mixResults----");
            int maxLines = getMaxLines();
            for (int i = 0; i < mSources.size(); i++) {
                CursorSource s = mSources.get(i);
                int n = Math.min(s.getCount(), maxLines);
                maxLines -= n;
                boolean more = false;
                for (int j = 0; j < n; j++) {
                    results.add(s.getItem());
                    more = s.moveToNext();
                }
            }
            results.add(0, new SuggestItem("aa", "aa", 0,1)); 
            maxLines=maxLines+1;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults fresults) {
        	 Log.i("browser", "publishResults-----");
            if (fresults.values instanceof SuggestionResults) {
                mMixedResults = (SuggestionResults) fresults.values;
     
                Log.i("browser", "publishResults--2222222222---"+mMixedResults.items.size()+"                "+mMixedResults.getLineCount());
                notifyDataSetChanged();
            }
        }
    }

    private int getMaxLines() {
        int maxLines = mLandscapeMode ? mLinesLandscape : mLinesPortrait;
        maxLines = (int) Math.ceil(maxLines / 1.0);
        return maxLines;
    }

    /**
     * sorted list of results of a suggestion query
     *
     */
    class SuggestionResults {

        ArrayList<SuggestItem> items;
        // count per type
        int[] counts;

        SuggestionResults() {
            items = new ArrayList<SuggestItem>(24);
            // n of types:
            counts = new int[7];
        }

        int getTypeCount(int type) {
            return counts[type];
        }

        void addResult(SuggestItem item) {
            int ix = 0;
            while ((ix < items.size()) && (item.type >= items.get(ix).type))
                ix++;
            items.add(ix, item);
            counts[item.type]++;
        }

        int getLineCount() {
            return Math.min((mLandscapeMode ? mLinesLandscape : mLinesPortrait), items.size());
        }

        @Override
        public String toString() {
            if (items == null) return null;
            if (items.size() == 0) return "[]";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                SuggestItem item = items.get(i);
                sb.append(item.type + ": " + item.title);
                if (i < items.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }

    /**
     * data object to hold suggestion values
     */
    public class SuggestItem {
        public String title;
        public String url;
        public int type;
        public String extra;
        public int flage;

        public SuggestItem(String text, String u, int t,int f) {
            title = text;
            url = u;
            type = t;
            flage = f;
        }

    }

    abstract class CursorSource {

        Cursor mCursor;

        boolean moveToNext() {
            return mCursor.moveToNext();
        }

        public abstract void runQuery(CharSequence constraint);

        public abstract SuggestItem getItem();

        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

        public void close() {
            if (mCursor != null) {
                mCursor.close();
            }
        }
    }
    /**
     * combined bookmark & history source
     * 混合了书签和历史的数据来源
     */
    /**
     * combined bookmark & history source
     */
    class CombinedCursor extends CursorSource {
         private int type=10;
        @Override
        public SuggestItem getItem() {
            if ((mCursor != null) && (!mCursor.isAfterLast())) {
                String title = mCursor.getString(1);
                String url = mCursor.getString(2);
                if(mCursor.getInt(3)==0){
                	  Log.i("xiexiujie111", "______________**********TYPE_HISTORY*************___________");
                	type=	TYPE_HISTORY;
                }
                if(mCursor.getInt(3)==1){
                	  Log.i("xiexiujie111", "______________********TYPE_BOOKMARK***************___________");
                	type=	TYPE_BOOKMARK;
                }
                if(mCursor.getInt(4) == 2){
                	  Log.i("xiexiujie111", "______________********TYPE_PROSET***************___________");
                	type=TYPE_PROSET;
                }
                if(mCursor.getInt(4) == 3){
              	  Log.i("xiexiujie111", "______________********TYPE_PROSET***************___________");
              	type=TYPE_MY_SEARCH;
              }
                boolean isBookmark = (mCursor.getInt(3) == 1);
                return new SuggestItem(getTitle(title, url), getUrl(title, url),
                        type,2);
            }
            return null;
        }

        @Override
        public void runQuery(CharSequence constraint) {
            // constraint != null
            if (mCursor != null) {
                mCursor.close();
            }
            String like = constraint + "%";
            String[] args = null;
            String selection = null;
            if (like.startsWith("http") || like.startsWith("file")) {
                args = new String[1];
                args[0] = like;
                selection = "url LIKE ?";
            } else {
                args = new String[5];
                args[0] = "http://" + like;
                args[1] = "http://www." + like;
                args[2] = "https://" + like;
                args[3] = "https://www." + like;
                // To match against titles.
                args[4] = like;
                selection = COMBINED_SELECTION;
            }
            Uri.Builder ub = OmniboxSuggestions.CONTENT_URI.buildUpon();
            ub.appendQueryParameter(BrowserContract.PARAM_LIMIT,
                    Integer.toString(Math.max(mLinesLandscape, mLinesPortrait)));       
            mCursor =
                    mContext.getContentResolver().query(ub.build(), COMBINED_PROJECTION,
                            selection, (constraint != null) ? args : null, null);
                  
//            Log.i("browser", "______________***********************___________"+mCursor.getCount());
//               while (mCursor.moveToNext()) {  
//            		Log.i("browser", "_________22__********************" + mCursor.getString(1));  
//            	         }  
        
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        }

        /**
         * Provides the title (text line 1) for a browser suggestion, which should be the
         * webpage title. If the webpage title is empty, returns the stripped url instead.
         *
         * @return the title string to use
         */
        private String getTitle(String title, String url) {
            if (TextUtils.isEmpty(title) || TextUtils.getTrimmedLength(title) == 0) {
                title = UrlUtils.stripUrl(url);
            }
            return title;
        }

        /**
         * Provides the subtitle (text line 2) for a browser suggestion, which should be the
         * webpage url. If the webpage title is empty, then the url should go in the title
         * instead, and the subtitle should be empty, so this would return null.
         *
         * @return the subtitle string to use, or null if none
         */
        private String getUrl(String title, String url) {
            if (TextUtils.isEmpty(title)
                    || TextUtils.getTrimmedLength(title) == 0
                    || title.equals(url)) {
                return null;
            } else {
                return UrlUtils.stripUrl(url);
            }
        }
    }
//网络联想
    class SuggestCursor extends CursorSource {

        @Override
        public SuggestItem getItem() {
            if (mCursor != null) {
                String title = mCursor.getString(
                        mCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                String text2 = mCursor.getString(
                        mCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2));
                String url = mCursor.getString(
                        mCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2_URL));
                String uri = mCursor.getString(
                        mCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA));
                int type = (TextUtils.isEmpty(url)) ? TYPE_SUGGEST : TYPE_SUGGEST_URL;
                SuggestItem item = new SuggestItem(title, url, type,2);
                item.extra = mCursor.getString(
                        mCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA));
                return item;
            }
            return null;
        }

        @Override
        public void runQuery(CharSequence constraint) {
            if (mCursor != null) {
                mCursor.close();
            }
            SearchEngine searchEngine = mSettings.getSearchEngine();
            if (!TextUtils.isEmpty(constraint)) {
                if (searchEngine != null && searchEngine.supportsSuggestions()) {
                    mCursor = searchEngine.getSuggestions(mContext, constraint.toString());
                 
                    if (mCursor != null) {
                    	   Log.i("browser", "SuggestCursor   runQuery"+mCursor.getCount());
                        mCursor.moveToFirst();
                    }
                }
            } else {
                if (searchEngine.wantsEmptyQuery()) {
                    mCursor = searchEngine.getSuggestions(mContext, "");
                    Log.i("browser", "SuggestCursor2   runQuery"+mCursor.getCount());
                }
                mCursor = null;
            }
        }

    }

    public void clearCache() {
        mFilterResults = null;
        mSuggestResults = null;
        notifyDataSetInvalidated();
    }

    public void setIncognitoMode(boolean incognito) {
        mIncognitoMode = incognito;
        clearCache();
    }

    static String getSuggestionTitle(SuggestItem item) {
        // There must be a better way to strip HTML from things.
        // This method is used in multiple places. It is also more
        // expensive than a standard html escaper.
        return (item.title != null) ? Html.fromHtml(item.title).toString() : null;
    }

    static String getSuggestionUrl(SuggestItem item) {
        final String title = SuggestionsAdapter.getSuggestionTitle(item);

        if (TextUtils.isEmpty(item.url)) {
            return title;
        }

        return item.url;
    }
}
