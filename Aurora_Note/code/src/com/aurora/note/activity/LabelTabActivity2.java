package com.aurora.note.activity;

import java.util.ArrayList;

import com.aurora.note.R;
import com.aurora.note.adapter.NoteListAdapter2;
import com.aurora.note.alarm.NoteAlarmManager;
import com.aurora.note.alarm.NoteAlarmReceiver;
import com.aurora.note.bean.LabelResult;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.db.LabelAdapter;
import com.aurora.note.db.NoteAdapter;
import com.aurora.note.ui.MultiColumnListView;
import com.aurora.note.ui.PLA_AbsListView;
import com.aurora.note.ui.PLA_AdapterView;
import com.aurora.note.util.Globals;
import com.aurora.note.util.Log;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.ToastUtil;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

public class LabelTabActivity2 extends AuroraActivity {

	private static final String TAG = "LabelTabActivity";
	private String labelName = "";
	private String labelId;
	private static final int AURORA_LABEL_MORE = 0;
	private NoteAdapter mNoteAdapter;
	private LabelAdapter mLabelAdapter;
	private Context mContext;
	private InputMethodManager mInputMethodManager;

	//确定删除对话框
	private AuroraAlertDialog mDeleteConDialog;

	private View mEmptyView;
	// private AuroraListView mListView;
	// private NoteListAdapter mAdapter;
	private MultiColumnListView mListView;
	private NoteListAdapter2 mAdapter;
	private ArrayList<NoteResult> mListToDisplay = new ArrayList<NoteResult>();
	private ArrayList<NoteResult> mMoreList = null;
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

	private AuroraActionBar mActionBar;
	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {

		@Override
		public void onAuroraActionBarItemClicked(int itemId) {

			switch (itemId) {
			case AURORA_LABEL_MORE:
				showAuroraMenu();
				break;

			default:
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = LabelTabActivity2.this;
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (null != getIntent().getExtras()) {
			labelName = getIntent().getExtras().getString("labelname");
			Log.e("liumx","-------------labelName is-------------"+labelName);
		} else {
			Log.e("liumx","-------------labelName is null or empty-------------2");
		}
		// getWindow().setBackgroundDrawableResource(R.drawable.note_main_all_bg);
		setAuroraContentView(R.layout.label_tab_activity_2, AuroraActionBar.Type.Normal);
		initDB();
		initActionBar();
		initViews();
		setListener();
		initData();
	}

	private void initData() {
		isLoadDataFinish = false;
		pageNum = 1;
    	labelId = mLabelAdapter.queryIDByName(labelName);
    	mNoteHandler.initviewdata();
	}

	private void setListener() {

		mListView.setOnItemClickListener(new PLA_AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
				Bundle bl = new Bundle();
				bl.putInt(NewNoteActivity.TYPE_GET_DATA, 0);
				bl.putParcelable(NewNoteActivity.NOTE_OBJ, (NoteResult) mListView.getAdapter().getItem(position));

				Intent intent = new Intent(LabelTabActivity2.this, NewNoteActivity.class);
				intent.putExtras(bl);
				startActivityForResult(intent, Globals.REQUEST_CODE_OPEN_NEWNOTE);
			}

		});

		mListView.setLongClickable(true);
		mListView.setOnItemLongClickListener(new PLA_AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(PLA_AdapterView<?> parent, View view, final int position, long id) {
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

				int title = R.string.dialog_delete_note_title;
				int message = R.string.dialog_delete_note_message;
				mDeleteConDialog = new AuroraAlertDialog.Builder(mContext,
                        AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                        .setTitle(title)
                        .setMessage(message)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                        		new DialogInterface.OnClickListener() {

									@Override
									public void onClick(
											DialogInterface dialog,
											int which) {
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
				// TODO Auto-generated method stub
				if (isLoadDataFinish)
					return;
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(loadMoreView) == view
							.getLastVisiblePosition()) {
						scrollEnd = true;
					}
				} catch (Exception e) {
					// MyLog.i(TAG, e.toString());
					scrollEnd = false;
				}
				if (mListToDisplay.size() < pageNum * rowCount)
					return;
				if (scrollEnd) {
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
				// TODO Auto-generated method stub

			}
		});

	}

	private void deleteListItem(int position) {
		NoteResult deleteNote = (NoteResult) mListView.getAdapter().getItem(position);
		mNoteAdapter.deleteDataById(String.valueOf(deleteNote.getId()));
		NoteAlarmReceiver.scheduleAlarmById(deleteNote.getId(), NoteAlarmManager.ACTION_DELETE);

		totalNum = mNoteAdapter.getCountByLabel(labelId);
		//删除mListToDisplay对应的数据
		mListToDisplay.remove(position- mListView.getHeaderViewsCount());
		if (mListToDisplay.size() >= totalNum) {
			mAdapter.notifyDataSetChanged();
		} else {
			NoteResult addNote = mNoteAdapter.queryDataByIndex(labelId, pageNum,rowCount);
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

	private void initViews() {
		mEmptyView = findViewById(R.id.no_note_fra);
		// mListView = (AuroraListView) findViewById(R.id.note_list);
		// mListView.setDivider(null);
		mListView = (MultiColumnListView) findViewById(R.id.note_list);
		mListView.setDivider(null);
		mListView.setSelector(R.drawable.note_list_item_top_selector);
		mListView.setDrawSelectorOnTop(true);

		Resources resources = getResources();
		mListView.setSelectorInnerPadding(
				resources.getDimensionPixelOffset(R.dimen.note_list2_padding_left),
				resources.getDimensionPixelOffset(R.dimen.note_list2_padding_top),
				resources.getDimensionPixelOffset(R.dimen.note_list2_padding_left), 0);

		loadMoreView = (LinearLayout) getLayoutInflater().inflate(R.layout.listview_footer, null);
		loadMoreView.setClickable(false);
		loadMoreView.setLongClickable(false);
		forum_foot_more = (TextView) loadMoreView.findViewById(R.id.listview_foot_more);
		foot_progress = (ProgressBar) loadMoreView.findViewById(R.id.listview_foot_progress);

		mListView.addFooterView(loadMoreView);
	}

	private void initActionBar() {
		mActionBar = getAuroraActionBar();

		mActionBar.setTitle(labelName);
		addAuroraActionBarItem(AuroraActionBarItem.Type.More, AURORA_LABEL_MORE);
		mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		setAuroraMenuCallBack(new OnAuroraMenuItemClickListener() {

			@Override
			public void auroraMenuItemClick(int menuItemId) {
				switch (menuItemId) {
				case R.id.action_tab_edit:
					showModifyLabelDialog();
					break;
				case R.id.action_tab_delete:
					mDeleteConDialog = null;
					int title = R.string.dialog_delete_label_title;
					int message = R.string.dialog_delete_label_message;
					mDeleteConDialog = new AuroraAlertDialog.Builder(mContext,
                            AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                            .setTitle(title)
                            .setMessage(message)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok,
                            		new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mLabelAdapter.deleteDataById(labelId);
											finish();
										}
                            	
                            		}).create();
					mDeleteConDialog.show();

					break;
				default:
					break;
				}
				
				
			}});
		setAuroraMenuItems(R.menu.label_tab_note);
	}

	private void initDB() {
		mNoteAdapter = new NoteAdapter(this);
		mLabelAdapter = new LabelAdapter(this);
		mNoteAdapter.open();
		mLabelAdapter.open();
	}

	public void setAdapter(ArrayList<NoteResult> list) {
		// mAdapter = new NoteListAdapter(this, list, "");
		// mAdapter.notifyDataSetChanged();
		mAdapter = new NoteListAdapter2(this, list, "");
		mListView.setAdapter(mAdapter);
	}

	private synchronized void loadMoreData() {
		if (ifScroll) {
			ifScroll = false;
			pageNum++;
			mMoreList = new ArrayList<NoteResult>();
			mMoreList = mNoteAdapter.queryDataOfLabelByLine(labelId, pageNum, rowCount);
//			mMoreList = mNoteAdapter.queryDataByLine(pageNum, rowCount);
		}
	}

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
						totalNum = mNoteAdapter.getCountByLabel(labelId);
						mListToDisplay = mNoteAdapter.queryDataOfLabelByLine(labelId, pageNum, rowCount);
//						mListToDisplay = mNoteAdapter.queryDataByLine(pageNum, rowCount);
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

					foot_progress.setVisibility(View.GONE);
					forum_foot_more.setText(R.string.all_loaded);
					
				}
				if (mListToDisplay.size() >= totalNum)
				{
					loadMoreView.setVisibility(View.GONE);
				}

				break;
//			case NOTEMAIN_QUERY:
//				new Thread() {
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//					/*	if (mListToDisplay != null) {
//							mListToDisplay.clear();
//						}*/
//						mListToDisplay = mNoteAdapter.queryDataByKey(queryText);
//						Log.i(TAG, "mListToDisplay.size()------"
//								+ (mListToDisplay == null ? 0 : mListToDisplay.size()));
//						disview();
//					}
//
//				}.start();
//				break;
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
//			if (isSearchMode) {
//				if (mListToDisplay == null || mListToDisplay.size() == 0) {
//					mNoMatchView.setVisibility(View.VISIBLE);
//				} else {
//					mNoMatchView.setVisibility(View.GONE);
//				}
//			} else {
				if (mListToDisplay == null || mListToDisplay.size() == 0) {
					mEmptyView.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				} else {
					mEmptyView.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
				}
//			}
			updateFootState();
		}

		private void updateFootState() {
			ifScroll = true;
			
			/*mListView
					.onRefreshComplete(getString(R.string.pull_to_refresh_update)
							+ new Date().toLocaleString());*/
		
			if(null == mListToDisplay)
			{					
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				return;
				
			}
			if(totalNum <= rowCount){
				loadMoreView.setVisibility(View.INVISIBLE);
				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				loadMoreView.setVisibility(View.GONE);
				return;
			}
			if (isLoadDataFinish) {

				foot_progress.setVisibility(View.GONE);
				forum_foot_more.setText(R.string.all_loaded);
				
			}
		}
		
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case Globals.REQUEST_CODE_OPEN_NEWNOTE:
				initData();

				break;
			default:
				break;
			}
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		setResult(Activity.RESULT_OK);
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
		//mNoteHandler.initviewdata();
		super.onResume();
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

	private void showModifyLabelDialog() {
		View view = LayoutInflater.from(this).inflate(
				R.layout.edit_label_dialog, null);
		final AuroraEditText editText = (AuroraEditText) view
				.findViewById(R.id.edit_note_label);
		editText.setText(labelName);
		final AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.menu_tab_edit)
				.setView(view)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String labelString = editText.getText()
										.toString().trim();

								if (mLabelAdapter.queryIDByName(labelString) != null) {
									ToastUtil.shortToast(R.string.note_modify_label_notice);
									mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
									return;
								}

								LabelResult labelResult = new LabelResult();
								labelResult.setContent(labelString);
								labelResult.setUpdate_time(System.currentTimeMillis());

								mLabelAdapter.updateNoteByID(labelResult, labelId);
								mActionBar.setTitle(labelString);
								labelName = labelString;
								mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
							}
						})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
					}
				})
				.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface arg0) {
                mInputMethodManager.showSoftInput(editText, 0);
            }
        });

		dialog.show();

		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				boolean enabled = !TextUtils.isEmpty(editText.getText()
						.toString().trim());
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(
						enabled);
			}
		});
		editText.requestFocus();

		SystemUtils.lengthFilter(editText, 20, getString(R.string.new_note_text_limit));
    }

}