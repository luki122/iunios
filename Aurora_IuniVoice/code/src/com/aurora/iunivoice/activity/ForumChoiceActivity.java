package com.aurora.iunivoice.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.ForumData;
import com.aurora.datauiapi.data.bean.ForumInfo;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.adapter.ForumChoiceListAdapter;
import com.aurora.iunivoice.utils.ForumUtil;
import com.aurora.iunivoice.utils.LoadingPageUtil;
import com.aurora.iunivoice.utils.ToastUtil;

public class ForumChoiceActivity extends BaseActivity implements OnItemClickListener {
	
	public static final String FORUM_INDEX = "forum_index";
	public static final String FORUM_ID = "forum_choice";
	public static final String FORUM_TITLE = "forum_title";

	protected static final int ACTION_BAR_RIGHT_ITEM_ID = 1;
	
	private ArrayList<ForumInfo> mForumList = new ArrayList<ForumInfo>();

	private ListView mListView;
	private ForumChoiceListAdapter mAdapter;
	private IuniVoiceManager mManager;

	private boolean isLoading = false;
	private LoadingPageUtil loadingPageUtil;
	
	private int checkIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_forum_choice);
		
		setupViews();
		initData();
		getIntentData();
	}

	@Override
	public void setupViews() {
		mListView = (ListView) findViewById(R.id.forum_list);
	}

	@Override
	public void setupAuroraActionBar() {
		super.setupAuroraActionBar();
		setTitleRes(R.string.publish_forum);

		addActionBarItem(getString(R.string.publish_confirm), ACTION_BAR_RIGHT_ITEM_ID);
	}

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		super.onActionBarItemClick(view, itemId);
		switch (itemId) {
		case BACK_ITEM_ID:
			finish();
			break;
		case ACTION_BAR_RIGHT_ITEM_ID:
			if (checkIndex == -1) {
				ToastUtil.shortToast(R.string.publish_please_choice_forum);
				return;
			}
			Intent data = new Intent();
			data.putExtra(FORUM_INDEX, checkIndex);
			data.putExtra(FORUM_ID, mForumList.get(checkIndex).getFid());
			data.putExtra(FORUM_TITLE, mForumList.get(checkIndex).getName());
			setResult(RESULT_OK, data);
			finish();
			break;
		}
	}
	
	private void initData() {
		mAdapter = new ForumChoiceListAdapter(this, mForumList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		initLoadingPage();
		
		loadingPageUtil.showLoadPage();
		loadingPageUtil.showLoading();

		List<ForumInfo> forumList = ForumUtil.getForumList(this);
		if (forumList != null && forumList.size() > 0) {
			mForumList.clear();
			mForumList.addAll(forumList);
			mAdapter.notifyDataSetChanged();
			
			isLoading = false;
			loadingPageUtil.hideLoadPage();
		} else {
			mManager = new IuniVoiceManager(this);
			
			getNetData();
		}
	}
	
	private void getIntentData() {
		int index = getIntent().getIntExtra(FORUM_INDEX, -1);
		checkIndex = index;
		if (checkIndex != -1) {
			mAdapter.setCheckIndex(checkIndex);
			mAdapter.notifyDataSetChanged();
		}
	}

	private void initLoadingPage() {
		loadingPageUtil = new LoadingPageUtil();
		loadingPageUtil.init(this, getWindow().getDecorView());
		loadingPageUtil.setOnShowListener(new LoadingPageUtil.OnShowListener() {
			@Override
			public void onShow() {
				mListView.setVisibility(View.GONE);
			}
		});
		loadingPageUtil.setOnHideListener(new LoadingPageUtil.OnHideListener() {
			@Override
			public void onHide() {
				mListView.setVisibility(View.VISIBLE);			
			}
		});
		loadingPageUtil.setOnRetryListener(new LoadingPageUtil.OnRetryListener() {
			@Override
			public void retry() {
				getNetData();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter != null) {
			checkIndex = position;
			mAdapter.setCheckIndex(checkIndex);
			mAdapter.notifyDataSetChanged();
		}
	}

	private void getNetData() {
		if (isLoading) {
			return;
		}
		isLoading = true;

		mManager.getForumData(new DataResponse<ForumData>() {
			@Override
			public void run() {
				if (value != null) {
					ForumUtil.saveForumData(ForumChoiceActivity.this, value);

					ArrayList<ForumInfo> forumList = value.getData().getForumlist();
					if (forumList != null) {
						mForumList.clear();
						mForumList.addAll(forumList);
						mAdapter.notifyDataSetChanged();
					}
				}
				isLoading = false;
				loadingPageUtil.hideLoadPage();
			}
		});
	}

}
