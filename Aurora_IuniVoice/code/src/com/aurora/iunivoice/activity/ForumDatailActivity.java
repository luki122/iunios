package com.aurora.iunivoice.activity;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView.IXListViewListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.PostData;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.adapter.PostListAdapter;
import com.aurora.iunivoice.bean.PostInfo;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.LoadingPageUtil;
import com.aurora.iunivoice.utils.SystemUtils;
import com.aurora.iunivoice.utils.ToastUtil;

public class ForumDatailActivity extends BaseActivity implements IXListViewListener, AdapterView.OnItemClickListener {

	public static final String FORUM_ID = "forum_id";
	public static final String FORUM_NAME = "forum_name";
	
	public static final String FORUM_FAVOUR = "isFavour";
	public static final String FORUM_COMMEND = "isCommend";
	public static final String FORUM_GRADE = "grade";
	public static final String FORUM_REPLY="reply";

	private String forumId;
	private String forumName;
	private String forumHash;

	private PullToRefreshListView mListView;
	private PostListAdapter mAdapter;

	private ArrayList<PostInfo> mPostList = new ArrayList<PostInfo>();

	private int page = 1;
	private int tpp = 10;
	private boolean isLoading = false;

	private LoadingPageUtil loadingPageUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_forum_detail);

		enableBackItem(true);
		initIntentData();
		setupViews();
		initData();
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (loadingPageUtil != null) {
			loadingPageUtil.clearRegister();
		}
	}

	@Override
	protected void onActionBarItemClick(View view, int itemId) {
		if (BACK_ITEM_ID == itemId) {
			finish();
		}
	}

	@Override
	public void setupViews() {
		mListView = (PullToRefreshListView) findViewById(R.id.post_list);
		mListView.setPullRefreshEnable(true);
		mListView.setPullLoadEnable(true);
	}

	private void initIntentData() {
		Intent intent = getIntent();
		if (intent != null) {
			forumId = intent.getStringExtra(FORUM_ID);
			forumName = intent.getStringExtra(FORUM_NAME);
			if (forumName != null) {
				setTitleText(forumName);
			}
		}
	}

	private void initData() {
		mComanager = new IuniVoiceManager(this);

		mAdapter = new PostListAdapter(this, mPostList);
		mListView.setAdapter(mAdapter);
		mListView.setXListViewListener(this);
		mListView.setOnItemClickListener(this);

		initLoadingPage();
		getNetData();
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
				onRefresh();
			}
		});
		loadingPageUtil.showLoadPage();
		loadingPageUtil.showLoading();
	}

	private void getNetData() {
		if (isLoading) {
			return;
		}
		isLoading = true;

		mComanager.getPostData(new DataResponse<PostData>() {
			@Override
			public void run() {
				if (value != null) {
					if (forumHash == null) {
						forumHash = value.getFormhash();
					} 
					
					if (value.getData() == null) {
						isLoading = false;
						if (loadingPageUtil.isShowing()) {
							loadingPageUtil.showNetworkError();
						}
						return;
					}

					ArrayList<PostInfo> postList = value.getData().getForum_threadlist();
					if (postList != null) {
						if (page == 1) {
							mPostList.clear();
						}

						mPostList.addAll(postList);
						mAdapter.notifyDataSetChanged();

						mListView.stopRefresh();
						mListView.stopLoadMore();

						if (postList.size() < tpp) {
							mListView.setPullLoadEnable(false);
						} else {
							mListView.setPullLoadEnable(true);
						}
					}
				}
				isLoading = false;
				if (page == 1) {
					loadingPageUtil.hideLoadPage();
				}
			}
		}, forumId, page, tpp);
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		PostInfo post = (PostInfo) parent.getAdapter().getItem(position);
		gotoPageDetailActicity(post);
	}

	private void gotoPageDetailActicity(PostInfo post) {
		if (post != null) {
			Intent intent = new Intent();
			intent.setClass(this, PageDetailActivity.class);
			intent.putExtra(PageDetailActivity.PAGE_ID_KEY, post.getTid());
			intent.putExtra(PageDetailActivity.FILD_ID_KEY, forumId);
			intent.putExtra(PageDetailActivity.FORM_HASH_KEY, forumHash);
			intent.putExtra(PageDetailActivity.PAGE_TITLE_KEY, post.getSubject());
			intent.putExtra(PageDetailActivity.PAGE_DESCRIBE_KEY, post.getMessage());
			startActivityForResult(intent, Globals.REQUEST_POSTDETAIL_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Globals.REQUEST_POSTDETAIL_CODE:
			if (resultCode == Activity.RESULT_OK) {
				if(data!=null){
					boolean isFavour = data.getBooleanExtra(FORUM_FAVOUR, false);
					boolean isCommend = data.getBooleanExtra(FORUM_COMMEND, false);
					int grade = data.getIntExtra(FORUM_GRADE,0);
					int reply = data.getIntExtra(FORUM_REPLY, 0);
					String tid = data.getStringExtra("tid");
					
					for (PostInfo info : mPostList) {
						if(info.getTid().equals(tid)){
							if(isFavour){
								info.setRecommends(Integer.parseInt(info.getRecommends())+1+"");
							}
							if(isCommend){
								info.setRate(Integer.parseInt(info.getRate())+grade+"");
							}
							info.setReplies(Integer.parseInt(info.getReplies())+reply+"");
							mAdapter.notifyDataSetChanged();
							break;
						}
					}
					
				}
				
				//onRefresh();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefresh() {
		if(!SystemUtils.hasNetwork()){
			//ToastUtil.shortToast("无可用网络，请检查网络链接是否打开");
			ToastUtil.shortToast(R.string.network_not_available);
			mListView.stopRefresh();
			return ;
		}
		page = 1;
		getNetData();
	}

	@Override
	public void onLoadMore() {
		if(!SystemUtils.hasNetwork()){
			//ToastUtil.shortToast("无可用网络，请检查网络链接是否打开");
			ToastUtil.shortToast(R.string.network_not_available);
			mListView.stopLoadMore();
			return ;
		}
		page++;
		getNetData();
	}

}
