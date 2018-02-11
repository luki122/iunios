package com.aurora.note;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraSearchView;

import com.aurora.note.activity.NewNoteActivity;
import com.aurora.note.adapter.LabelListAdapter;
import com.aurora.note.adapter.NoteListAdapter2;
import com.aurora.note.alarm.NoteAlarmManager;
import com.aurora.note.alarm.NoteAlarmReceiver;
import com.aurora.note.bean.LabelResult;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.db.LabelAdapter;
import com.aurora.note.db.NoteAdapter;
import com.aurora.note.ui.PLA_AdapterView;
import com.aurora.note.ui.PLA_AbsListView;
import com.aurora.note.ui.PLA_PullListview;
import com.aurora.note.util.Globals;
import com.aurora.note.util.Log;
import com.aurora.note.widget.LabelList;

import java.util.ArrayList;
import java.util.Date;

public class NoteMainActivity2 extends AuroraActivity implements
		OnSearchViewQuitListener {

	private static final String TAG = "NoteMainActivity";
	private static final int AURORA_NEW_NOTE = 0;

	private AuroraActionBar mActionBar;
	private AuroraSearchView mSearchView;
	private Button mCancelBtn;
	private View mGotoSearchLayout;
	private LinearLayout mBackgroundSearchLayout;
	private FrameLayout mSearchViewBackground;
	private View mNoMatchView;
	private View mEmptyView;
	// private PullToRefreshListview mListView;
	private PLA_PullListview mListView;
//	private View mLabelListContainer;
	private LabelList mLabelList;
	//确定删除对话框
	private AuroraAlertDialog mDeleteConDialog;

	private AuroraActivity mActivity;
//	private boolean mIsRecreatedInstance;
//	public ImageLoader imageLoader = ImageLoader.getInstance();
	private NoteAdapter mNoteAdapter;
	private LabelAdapter mLabelAdapter;

	private LabelListAdapter mLabelListAdapter;
	// private NoteListAdapter mAdapter;
	private NoteListAdapter2 mAdapter;
	private ArrayList<NoteResult> mListToDisplay = new ArrayList<NoteResult>();
	private ArrayList<NoteResult> mMoreList = null;
	private ArrayList<LabelResult> labelResultList = null;
	//输入的搜索关键字
	private String queryText;
	private boolean isSearchMode = false;
	// 加载更多面板
	private LinearLayout loadMoreView;
	// 底面板加载更多字段控件
	private TextView forum_foot_more;
	private ProgressBar foot_progress;
	
	// 分页加载滑动标志
	private boolean ifScroll = true;
	// 分页数
	private int pageNum = 1;
	// 一页数据展示数
	private int rowCount = 10;
	private long totalNum = 0;
	// 数据是否加载完毕
	private boolean isLoadDataFinish = false;
	private NoteHandler mNoteHandler = new NoteHandler();
	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {

		@Override
		public void onAuroraActionBarItemClicked(int itemId) {

			switch (itemId) {
			case AURORA_NEW_NOTE:
				Intent intent = new Intent(NoteMainActivity2.this,
						NewNoteActivity.class);
				startActivityForResult(intent,
						Globals.REQUEST_CODE_OPEN_NEWNOTE);
				break;

			default:
				break;
			}

		}
	};

	class NoteHandler extends Handler {
		private final int NOTEMAIN_INIT = 0;
		private final int NOTEMAIN_DISVIEW = 1;
		private final int NOTEMAIN_MORE_DISVIEW = 2;
		private final int NOTEMAIN_QUERY = 3;
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NOTEMAIN_INIT:
				new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
					/*	if (mListToDisplay != null) {
							mListToDisplay.clear();
						}*/
						totalNum = mNoteAdapter.getCount();
						mListToDisplay = mNoteAdapter.queryDataByLine(pageNum, rowCount);
						Log.i(TAG, "total Notes::"+totalNum);
						Log.i(TAG, "mListToDisplay is Null::"+(mListToDisplay == null));
						Log.i(TAG, "mListToDisplay::"
								+ (mListToDisplay == null ? 0 : mListToDisplay.size()));
						disview();
					}

				}.start();
				break;
			case NOTEMAIN_DISVIEW:
				updateUIView();
				break;
			case NOTEMAIN_MORE_DISVIEW:
				ifScroll = true;
				if(null ==  mMoreList)
				{
					loadMoreView.setVisibility(View.GONE);
					foot_progress.setVisibility(View.GONE);
					forum_foot_more.setText(R.string.all_loaded);
					break;
				}
				if (mMoreList.size() < rowCount)
					isLoadDataFinish = true;
				for (int i = 0; i < mMoreList.size(); i++) {
					mListToDisplay.add(mMoreList.get(i));
				}
				//mListView.onRefreshComplete();
				mAdapter.notifyDataSetChanged();
				//setAdapter(mListToDisplay);
				
				if (isLoadDataFinish) {
					loadMoreView.setVisibility(View.GONE);
					foot_progress.setVisibility(View.GONE);
					forum_foot_more.setText(R.string.all_loaded);
				}
				if (mListToDisplay.size() >= totalNum)
				{
					loadMoreView.setVisibility(View.GONE);
				}
		
				break;
			case NOTEMAIN_QUERY:
				new Thread() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
					/*	if (mListToDisplay != null) {
							mListToDisplay.clear();
						}*/
						mListToDisplay = mNoteAdapter.queryDataByKey(queryText);
						Log.i(TAG, "mListToDisplay.size()------"
								+ (mListToDisplay == null ? 0 : mListToDisplay.size()));
						disview();
					}

				}.start();
				break;
			default:
				super.handleMessage(msg);
				break;
			}

		}
		public void initviewdata() {
			sendEmptyMessage(NOTEMAIN_INIT);
		}

		public void disview() {
			sendEmptyMessage(NOTEMAIN_DISVIEW);
		}

		public void dismoreview() {
			sendEmptyMessage(NOTEMAIN_MORE_DISVIEW);
		}
		
		public void queryKeyNotes() {
			sendEmptyMessage(NOTEMAIN_QUERY);
		}
		
		public void updateUIView() {
			setAdapter(mListToDisplay);
			if (isSearchMode) {
				if (mListToDisplay == null || mListToDisplay.size() == 0) {
					getWindow().setBackgroundDrawableResource(R.color.white);
					mNoMatchView.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				} else {
					getWindow().setBackgroundDrawableResource(R.color.note_main_bg_color);
					mNoMatchView.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
				}
			} else {
				if (mListToDisplay == null || mListToDisplay.size() == 0) {
					mEmptyView.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				} else {
					mEmptyView.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
				}
			}
			updateFootState();
		}
		
		private void updateFootState() {
			ifScroll = true;
			
			/*mListView
					.onRefreshComplete(getString(R.string.pull_to_refresh_update)
							+ new Date().toLocaleString());*/

			if (isSearchMode) {
				loadMoreView.setVisibility(View.GONE);
				return;
			}

			if(null == mListToDisplay)
			{					
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				return;
				
			}
			if(totalNum <= rowCount){
				loadMoreView.setVisibility(View.GONE);
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				return;
			}
			if (isLoadDataFinish) {

				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
			}
		}
		
	};

	public void setAdapter(ArrayList<NoteResult> list) {
		// mAdapter = new NoteListAdapter(this, list, queryText);
		// mAdapter.notifyDataSetChanged();
		mAdapter = new NoteListAdapter2(this, list, queryText);
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(R.color.note_main_bg_color);
		setAuroraContentView(R.layout.note_main_2, AuroraActionBar.Type.Empty);
		mActivity = NoteMainActivity2.this;
		initDB();
		initViews();
		initActionBar();
		setListener();
		initdata();
	}

    private void initdata() {
        NoteAlarmReceiver.initNoteData(this);
		// TODO Auto-generated method stub
    	isLoadDataFinish = false;
		pageNum = 1;
    	queryText = "";
    	mNoteHandler.initviewdata();
	}

	private void initDB() {
		// TODO Auto-generated method stub
		mNoteAdapter = new NoteAdapter(this);
		mLabelAdapter = new LabelAdapter(this);
		mNoteAdapter.open();
		mLabelAdapter.open();
	}

	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.app_name);
		// addAuroraActionBarItem(AuroraActionBarItem.Type.Add, AURORA_NEW_NOTE);
		mActionBar.addItem(R.drawable.note_main_new_selector, AURORA_NEW_NOTE, getString(R.string.new_note));
		mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
	}

	private void initViews() {
		addSearchviewInwindowLayout();
		mEmptyView = findViewById(R.id.no_note_fra);
		// mListView = (PullToRefreshListview)getListView();
		mListView = (PLA_PullListview) findViewById(R.id.note_list_view);
		mListView.setDivider(null);
		mListView.setSelector(R.color.note_main_bg_color);
		// mListView.setDrawSelectorOnTop(true);

		mNoMatchView = findViewById(R.id.no_match);
		mSearchViewBackground = (FrameLayout)getSearchViewGreyBackground();
		mBackgroundSearchLayout = (LinearLayout)LayoutInflater.from(mActivity)
				.inflate(R.layout.search_view_background, null, false);

		loadMoreView = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.listview_footer, null);
		loadMoreView.setClickable(false);
		loadMoreView.setLongClickable(false);
		forum_foot_more = (TextView) loadMoreView
				.findViewById(R.id.listview_foot_more);
		foot_progress = (ProgressBar) loadMoreView
				.findViewById(R.id.listview_foot_progress);

		mListView.addFooterView(loadMoreView);

		if (mSearchViewBackground != null) {
			mSearchViewBackground.addView(mBackgroundSearchLayout);
//			mLabelListContainer = mBackgroundSearchLayout.findViewById(R.id.label_list_container);
			mLabelList = (LabelList) mBackgroundSearchLayout.findViewById(R.id.label_list);
		}
	}

	private void initLabelAdapter() {
		if (labelResultList != null) {
			labelResultList.clear();
			labelResultList = null;
		}
		/*labelResultList = new ArrayList<LabelResult>();
		labelResultList = mLabelAdapter.queryAllData();
		if (null != labelResultList) {
			mLabelListAdapter = new LabelListAdapter(mActivity, labelResultList);
			mLabelList.setAdapter(mLabelListAdapter);
			if (labelResultList.size() == 0) {
				mBackgroundSearchLayout.setVisibility(View.GONE);
			} else {
				mBackgroundSearchLayout.setVisibility(View.VISIBLE);
			}
		} else {
			if (null != mSearchViewBackground) {
				mBackgroundSearchLayout.setVisibility(View.GONE);
			}
		}*/
	}

	private void setListener() {

		mGotoSearchLayout = mListView.getHeadView();
		if (mGotoSearchLayout != null) {
			mGotoSearchLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					mSearchView = getSearchView();
					mCancelBtn = getSearchViewRightButton();
					if (null == mSearchView) {
						return;
					}
					if (mCancelBtn != null) {
						mCancelBtn.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								hideSearchViewLayout(true);
								
							}
						});
					}
					initLabelAdapter();
					setOnQueryTextListener(new svQueryTextListener());
					showSearchviewLayout();
					setSearchMode(true);

				}

			});
		}

		mListView.setOnItemClickListener(new PLA_AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
				closeSoftInputWindow();
				Intent intent = new Intent(NoteMainActivity2.this, NewNoteActivity.class);
				Bundle bl = new Bundle();
				bl.putParcelable("newNote", (NoteResult)mListView.getAdapter().getItem(position));
				bl.putInt("note_type", 0);
				intent.putExtras(bl);
				startActivityForResult(intent, Globals.REQUEST_CODE_OPEN_NEWNOTE);
			}

		});

		mListView.setLongClickable(true);
		mListView.setOnItemLongClickListener(new PLA_AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(PLA_AdapterView<?> parent, View view, final int position, long id) {
				mDeleteConDialog = new AuroraAlertDialog.Builder(mActivity)
                        .setTitle(R.string.dialog_delete_note_title)
                        .setMessage(R.string.dialog_delete_note_message)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                        		new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										deleteListItem(position);
									}

                        		}).create();
				mDeleteConDialog.show();
				return true;
			}

		});

		mListView.setOnScrollListener(new PLA_AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(PLA_AbsListView view, int scrollState) {
				mListView.onScrollStateChanged(view, scrollState);

				if (isLoadDataFinish) return;

				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(loadMoreView) == view.getLastVisiblePosition()) {
						scrollEnd = true;
					}
				} catch (Exception e) {
					scrollEnd = false;
				}

				if(mListToDisplay == null || mListToDisplay.size() < pageNum * rowCount) return;

				if (scrollEnd) {
					loadMoreView.setVisibility(View.VISIBLE);
					forum_foot_more.setText(R.string.loading);
					foot_progress.setVisibility(View.VISIBLE);

					new Thread() {
						public void run() {
							loadMoreData();
							mNoteHandler.dismoreview();
						}
					}.start();
				}
			}

			@Override
			public void onScroll(PLA_AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				mListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});

		mListView.setOnRefreshListener(new PLA_PullListview.OnRefreshListener() {
					@Override
					public void onRefresh() {
						mListView.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toString());
					}
				});

		setOnSearchViewQuitListener(this);
	}

	private void deleteListItem(int position) {
		NoteResult deleteNote = (NoteResult) mListView.getAdapter().getItem(position);
		mNoteAdapter.deleteDataById(String.valueOf(deleteNote.getId()));
		NoteAlarmReceiver.scheduleAlarmById(deleteNote.getId(), NoteAlarmManager.ACTION_DELETE);

		totalNum = mNoteAdapter.getCount();
		//删除mListToDisplay对应的数据
		mListToDisplay.remove(position - mListView.getHeaderViewsCount());
		if (mListToDisplay.size() >= totalNum) {
			mAdapter.notifyDataSetChanged();
		} else {
			NoteResult addNote = mNoteAdapter.queryDataByIndex(pageNum, rowCount);
			if (null == addNote) {
				mAdapter.notifyDataSetChanged();
				return;
			}
			mListToDisplay.add(addNote);
			mAdapter.notifyDataSetChanged();

			if (mListToDisplay.size() >= totalNum) {
				loadMoreView.setVisibility(View.GONE);
			}
		}
		if (totalNum == 0) {
			mEmptyView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		} else {
			mEmptyView.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}

	private void closeSoftInputWindow() {
		if (getCurrentFocus() != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private synchronized void loadMoreData() {
		
			if (ifScroll) {
				ifScroll=false;

				pageNum++;
				mMoreList = new ArrayList<NoteResult>();
				
				// aurora ukiliu 2014-05-09 modify for BUG #4742 begin
				if (null != mNoteAdapter) {
					mMoreList = mNoteAdapter.queryDataByLine(pageNum, rowCount);
				}
				// aurora ukiliu 2014-05-09 modify for BUG #4742 end
				
			}
	}

	@Override
	public boolean quit() {
		getWindow().setBackgroundDrawableResource(R.color.note_main_bg_color);
		//退出搜索模式的时候需要放出mListView的搜索头部
		if (mGotoSearchLayout != null) {
			mGotoSearchLayout.setVisibility(View.VISIBLE);
		}
		setSearchMode(false);
		// mListView.auroraSetNeedSlideDelete(true);
		initdata();
		return false;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void finish() {
		super.finish();
		if (null != mNoteAdapter) {
			mNoteAdapter.close();
			mNoteAdapter = null;
		}
		if (null != mLabelAdapter) {
			mLabelAdapter.close();
			mLabelAdapter = null;
		}
	}

	@Override
	protected void onPause() {
		// mListView.auroraOnPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// mListView.auroraOnResume();
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case Globals.REQUEST_CODE_OPEN_NEWNOTE:
				if (!isSearchMode){
					initdata();
				} else {
					mNoteHandler.queryKeyNotes();
					// mListView.auroraSetNeedSlideDelete(false);
					// mAdapter.setAuroraListHasDelete(false);
					mAdapter.notifyDataSetChanged();
				}
				break;
			case Globals.REQUEST_CODE_MODIFY_LABEL:
				initLabelAdapter();
				break;
			default:
				break;
			}
		}
	}

	// 搜索输入响应监听
	private final class svQueryTextListener implements
			aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener {

		@Override
		public boolean onQueryTextChange(String mQueryText) {
			// do my thing
			Log.i(TAG, "mQueryText===============" + mQueryText);
			if (mQueryText.length() > 0) {
				setQueryText(mQueryText);
				mNoteHandler.queryKeyNotes();
				// mLabelListContainer.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
				//以下为：搜索结果大于一页时，防止拖拽出mListView的搜索头部
				if (mGotoSearchLayout != null) {
					mGotoSearchLayout.setVisibility(View.GONE);
				}
			} else {
				// mLabelListContainer.setVisibility(View.GONE);
				// mListView.setVisibility(View.GONE);
				initdata();
			}
			// mAdapter.setAuroraListHasDelete(false);
			mAdapter.notifyDataSetChanged();
			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String arg0) {
			return false;
		}

	}

	public void setQueryText(String mQueryText) {
		this.queryText = mQueryText;
	}

	//判断当前主界面是否为搜索模式
	public boolean isSearchMode() {
		return isSearchMode;
	}

	//设置当前主界面的搜索模式
	public void setSearchMode(boolean isSearchMode) {
		this.isSearchMode = isSearchMode;
		if (mListView != null) {
			mListView.setCanMoveHeadView(!isSearchMode);
			mListView.showHeadView();
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*switch (keyCode) {
			case KeyEvent.KEYCODE_BACK: {
				boolean deleteIsShow = mListView.auroraIsRubbishOut();
				if (deleteIsShow) {
					mListView.auroraSetRubbishBack();
					return true;
				}
				break;
			}
			default: {
			}
		}*/
		return super.onKeyDown(keyCode, event);
	}
}
