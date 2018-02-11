package com.aurora.iunivoice.activity.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.ForumData;
import com.aurora.datauiapi.data.bean.ForumInfo;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.ForumDatailActivity;
import com.aurora.iunivoice.adapter.ForumListAdapter;
import com.aurora.iunivoice.utils.ForumUtil;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.LoadingPageUtil;
import com.aurora.iunivoice.utils.Log;
import com.aurora.iunivoice.utils.ToastUtil;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;

public class ForumFragment extends BaseViewPagerFragment implements AdapterView.OnItemClickListener {

	private static final String TAG = "ForumFragment";

	private ArrayList<ForumInfo> mForumList = new ArrayList<ForumInfo>();

	private PullToRefreshListView mListView;
	private ForumListAdapter mAdapter;
	private IuniVoiceManager mManager;

	private boolean isLoading = false;
	private LoadingPageUtil loadingPageUtil;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(TAG, "ForumFragment onActivityCreated()");

		initData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_forum, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (loadingPageUtil != null) {
			loadingPageUtil.clearRegister();
		}
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case Globals.NETWORK_ERROR:
			isLoading = false;
			if (loadingPageUtil.isShowing()) {
				loadingPageUtil.showNetworkError();
			} else {
				ToastUtil.longToast(R.string.network_exception);
			}
			break;
		case Globals.NO_NETWORK:
			isLoading = false;
			if (loadingPageUtil.isShowing()) {
				loadingPageUtil.showNoNetWork();
			} else {
				ToastUtil.longToast(R.string.network_not_available);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void setupViews() {
		mListView = (PullToRefreshListView) getView().findViewById(R.id.forum_list);
		mListView.setPullRefreshEnable(false);
		mListView.setPullLoadEnable(false);
	}

	private void initData() {
		mManager = new IuniVoiceManager(this);

		mAdapter = new ForumListAdapter(getActivity(), mForumList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		initLoadingPage();
	}

	private void initLoadingPage() {
		loadingPageUtil = new LoadingPageUtil();
		loadingPageUtil.init(getActivity(), getView());
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
		ForumInfo forum = (ForumInfo) parent.getAdapter().getItem(position);
		gotoForumDetailActicity(forum);
	}

	private void gotoForumDetailActicity(ForumInfo forum) {
		if (forum != null) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), ForumDatailActivity.class);
			intent.putExtra(ForumDatailActivity.FORUM_ID, forum.getFid());
			intent.putExtra(ForumDatailActivity.FORUM_NAME, forum.getName());
			getActivity().startActivity(intent);
		}
	}

	@Override
	protected void loadData() {
		loadingPageUtil.showLoadPage();
		loadingPageUtil.showLoading();

		getNetData();
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
					ForumUtil.saveForumData(getActivity(), value);

					if (value.getData() == null) {
						isLoading = false;
						if (loadingPageUtil.isShowing()) {
							loadingPageUtil.showNetworkError();
						}
						return;
					}
					
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
