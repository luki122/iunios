package com.aurora.account.activity;

import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnQueryTextListener;

import com.aurora.account.R;
import com.aurora.account.adapter.CountryCodeAdapter;
import com.aurora.account.adapter.CountryCodeQueryAdapter;
import com.aurora.account.model.CountryCode;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.PingYinUtil;
import com.aurora.account.widget.QuickSearchBar.OnTouchingLetterChangedListener;
import com.aurora.account.widget.QuickSearchView;
import com.aurora.account.widget.stickylistheaders.StickyListHeadersListView;
import com.aurora.utils.DensityUtil;

public class CountryCodeActivity extends BaseActivity implements OnTouchingLetterChangedListener {
	
	public static final String COUNTRY_CODE = "country_code";
	
	public StickyListHeadersListView mListView;
	private QuickSearchView quickSearchView;
	
	private List<CountryCode> listData;
	private CountryCodeAdapter adapter;
	
	private View defaultView;
	private CountryCode defaultCountryCode;
	
	private int lastDealFirstVisibleItem = -1;
	
	boolean isQuery = true;
	private AuroraSearchView searchView;
	private List<CountryCode> queryListData;
	private CountryCodeQueryAdapter queryAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setAuroraContentView(R.layout.activity_countrycode,
				AuroraActionBar.Type.Normal, true);
		
		initView();
		initData();
		setListener();
	}
	
	@Override
    protected boolean shouldHandleFocusChanged() {
        return false;
    }
	
	@Override
	protected String getActionBarTitle() {
		return getString(R.string.register_countries_and_regions);
	}
	
	private void initView() {
		mListView = (StickyListHeadersListView) findViewById(R.id.mListView);
		quickSearchView = (QuickSearchView) findViewById(R.id.quickSearchView);
		quickSearchView.setOnTouchingLetterChangedListener(this);
		
		View sV = LayoutInflater.from(this).inflate(R.layout.header_country_code_search, null);
		mListView.addHeaderView(sV);
		
		defaultView = LayoutInflater.from(this).inflate(R.layout.item_country_code, null);
		mListView.addHeaderView(defaultView);
		
		searchView = getSearchView();
	}
	
	private void initData() {
		defaultCountryCode = CommonUtil.getDefaultCountryCode(this);
		
		((TextView) defaultView.findViewById(R.id.tv_name)).setText(defaultCountryCode
				.getCountryOrRegionsCN() + "  +" + defaultCountryCode.getCode());
		
		listData = new ArrayList<CountryCode>();
		
		String[] countries = getResources().getStringArray(R.array.countryOrRegions);
		String[] countriesCN = getResources().getStringArray(R.array.countryOrRegionsCH);
		String[] abbreviation = getResources().getStringArray(R.array.abbreviation);
		String[] code = getResources().getStringArray(R.array.code);
		
		CountryCode countryCode = null;
		for (int i = 0, l = countries.length; i < l; i++) {
			countryCode = new CountryCode();
			countryCode.setCountryOrRegions(countries[i]);
			countryCode.setCountryOrRegionsCN(countriesCN[i]);
			countryCode.setAbbreviation(abbreviation[i]);
			countryCode.setCode(code[i]);
			listData.add(countryCode);
		}
		
		Collections.sort(listData, new CountryCodeComparator());
		
		adapter = new CountryCodeAdapter(this, listData);
		mListView.setAdapter(adapter);
		
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		searchView.getQueryTextView().setThreshold(1);
		searchView.getQueryTextView().setDropDownHorizontalOffset(0);
		searchView.getQueryTextView().setDropDownVerticalOffset(
				DensityUtil.dip2px(this, 6));
		searchView.getQueryTextView().setDropDownWidth(width);
		searchView.getQueryTextView().setDropDownBackgroundDrawable(
				getResources().getDrawable(R.drawable.search_autotext_bg));
		
		queryListData = new ArrayList<CountryCode>();
		
		queryAdapter = new CountryCodeQueryAdapter(CountryCodeActivity.this, 
				R.layout.item_search_pop_list, R.id.tv_text, queryListData, searchView);
		searchView.getQueryTextView().setAdapter(queryAdapter);
		
	}
	
	private void setListener() {
		mListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				setHighlightTitle(firstVisibleItem);
			}
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if ((position - mListView.getHeaderViewsCount()) >= 0) {
					Intent data = new Intent();
					data.putExtra(COUNTRY_CODE, listData.get((position - mListView.getHeaderViewsCount())));
					setResult(RESULT_OK, data);
					finish();
				} else if ((position - mListView.getHeaderViewsCount()) == -1) {
					Intent data = new Intent();
					data.putExtra(COUNTRY_CODE, defaultCountryCode);
					setResult(RESULT_OK, data);
					
					finish();
				} else if ((position - mListView.getHeaderViewsCount()) == -2) {
					showSearchviewLayout();
				}
			}
		});
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String pQuery) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onQueryTextChange(String pQuery) {
				// TODO Auto-generated method stub
				if (isQuery) {
					queryListData.clear();
					for (CountryCode code : listData) {
						if (code.getCountryOrRegionsCN().contains(pQuery)
								|| code.getCountryOrRegions().contains(pQuery)) {
							queryListData.add(code);
						}
					}
					
					mHandler.sendEmptyMessage(100);
				}
				
				return false;
			}
		});
		
		searchView.getQueryTextView().setOnItemClickListener(
			new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent data = new Intent();
					data.putExtra(COUNTRY_CODE, queryListData.get(position));
					setResult(RESULT_OK, data);
					
					isQuery = false;
					hideSearchViewLayoutWithNoAnim();
					
					finish();
				}
		});
		
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		
		switch (msg.what) {
		case 100:
			queryAdapter.notifyDataSetChanged();
			break;
		}
	}

	/**
	 * 根据当前ListView中第一个显示的item去确定LetterSideBar中高亮的字符
	 * @param firstVisibleItem
	 */
	private void setHighlightTitle(int firstVisibleItem) {
		if (lastDealFirstVisibleItem == firstVisibleItem 
				|| listData == null
				|| quickSearchView == null) {
			return;
		}
		
		if (mListView.getHeaderViewsCount() > 0 && firstVisibleItem < mListView.getHeaderViewsCount()) {
			quickSearchView.setCurChooseTitle("#");	
		} else {
			char firstChar = PingYinUtil.getFirstLetter(listData.get(firstVisibleItem).getCountryOrRegionsCN().charAt(0));
			quickSearchView.setCurChooseTitle(Character.toString(firstChar).toUpperCase());			
		}
	
		lastDealFirstVisibleItem = firstVisibleItem;	
	}

	@Override
	public void onTouchingLetterChanged(String s, float positionOfY) {
		quickSearchView.LetterChanged(s, positionOfY, mListView, adapter);
	}

	@Override
	public int getToPosition(String s) {
		int position = adapter.findPosition(s);
		if (position != -1 && mListView.getHeaderViewsCount() > 0) {
			position += mListView.getHeaderViewsCount();
		}
		return position;
	}
	
	private class CountryCodeComparator implements Comparator<CountryCode> {
		
	    private RuleBasedCollator collator;  
	    
	    public CountryCodeComparator() {  
	        collator = (RuleBasedCollator) Collator.getInstance(java.util.Locale.CHINA);  
	    } 

		@Override
		public int compare(CountryCode lhs, CountryCode rhs) {
			
			CollationKey c1 = collator.getCollationKey(lhs.getCountryOrRegionsCN());  
	        CollationKey c2 = collator.getCollationKey(rhs.getCountryOrRegionsCN());  
	        
			return c1.compareTo(c2);
		}
		
	}

}
