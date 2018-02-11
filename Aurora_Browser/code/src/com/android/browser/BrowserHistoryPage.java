/*
 * Copyright (C) 2008 The Android Open Source Project
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


import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import com.android.browser.BrowserBookmarksAdapter.AllCheckedObserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.BrowserContract;
import android.provider.BrowserContract.Combined;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;

/**
 * Activity for displaying the browser's history, divided into
 * days of viewing.
 */
public class BrowserHistoryPage extends Fragment
        implements LoaderCallbacks<Cursor>, OnChildClickListener, AllCheckedObserver {

    static final int LOADER_HISTORY = 1;
//    static final int LOADER_MOST_VISITED = 2;
    private static final int CLEAR_HISTORY = LOADER_HISTORY + 1;
    private static String CHECK_ALL;
	private static String DIS_CHECK_ALL;
	protected static final int DISPLAY_DELETEDIALOG = CLEAR_HISTORY + 1;
	protected static final int DO_CHECK_STATE = DISPLAY_DELETEDIALOG + 1;
	protected static final int DO_REMOVE_HISTORY_DATE = DO_CHECK_STATE + 1;

    CombinedBookmarksCallbacks mCallback;
    BrowserHistorysAdapter historyAdapter;
    HistoryAdapter mAdapter;
    HistoryChildWrapper mChildWrapper;
    boolean mDisableNewWindow;
    HistoryItem mContextHeader;
    String mMostVisitsLimit;
//    ListView mGroupList, mChildList;
    private ViewGroup mPrefsContainer;
    private FragmentBreadCrumbs mFragmentBreadCrumbs;
    private AuroraActionBar titlebar;
    private TextView tvClear;
    private AuroraListView mHistoryList;

    private View mRoot;

    static interface HistoryQuery {
        static final String[] PROJECTION = new String[] {
                Combined._ID, // 0
                Combined.DATE_LAST_VISITED, // 1
                Combined.TITLE, // 2
                Combined.URL, // 3
                Combined.FAVICON, // 4
                Combined.TOUCH_ICON, // 5
                Combined.VISITS, // 6
        };

        static final int INDEX_ID = 0;
        static final int INDEX_DATE_LAST_VISITED = 1;
        static final int INDEX_TITE = 2;
        static final int INDEX_URL = 3;
        static final int INDEX_FAVICON = 4;
        static final int INDEX_TOUCH_ICON = 5;
        static final int INDEX_VISITS = 6;
    }
    
    private Handler historyHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case DISPLAY_DELETEDIALOG:
				HashMap<Integer, Long> needDeleteIdsMap = (HashMap<Integer, Long>) msg.obj;
				String alertMessage = "";
				if(msg.arg2 == 1) {
					alertMessage = getActivity().getString(R.string.if_clear_history,getActivity().getResources().getString(R.string.histories_record));
				}else {
					alertMessage = getActivity().getString(R.string.if_delete_history,msg.arg1,
							msg.arg1 == 1 ? getActivity().getResources().getString(R.string.history_record) : 
								getActivity().getResources().getString(R.string.histories_record)
							);
				}
				displayDeleteDialog(alertMessage,needDeleteIdsMap, msg.arg1);
				break;

			case DO_CHECK_STATE:
				historyAdapter.doCheckState();
				Bundle bundle = msg.getData();
				if(bundle != null) {
					String toastContent = bundle.getString("toast_content");
					if(toastContent != null) {
						Toast.makeText(getActivity(), toastContent, Toast.LENGTH_SHORT).show();
					}
					if(bundle.getBoolean("changeToNormal") && BrowserHistorysAdapter.isInSelectionMode) {
						titlebar.setShowBottomBarMenu(false);
						titlebar.showActionBarDashBoard();
						changeToNormal();
					}
				}
				break;
				
			case DO_REMOVE_HISTORY_DATE:{
				historyAdapter.removeHistoryDate((Long)msg.obj);
				break;
			}
				
			default:
				break;
			}
    	}
    };

    private void copy(CharSequence text) {
        ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(
                Context.CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_HISTORY: {
                String sort = Combined.DATE_LAST_VISITED + " DESC";
                String where = Combined.VISITS + " > 0 and " + Combined.DATE_LAST_VISITED + " < " + getTomorrowZero();
                CursorLoader loader = new CursorLoader(getActivity(), BrowserContract.History.CONTENT_URI,
                        HistoryQuery.PROJECTION, where, null, sort);
                return loader;
            }

//            case LOADER_MOST_VISITED: {
//                Uri uri = combinedBuilder
//                        .appendQueryParameter(BrowserContract.PARAM_LIMIT, mMostVisitsLimit)
//                        .build();
//                String where = Combined.VISITS + " > 0";
//                CursorLoader loader = new CursorLoader(getActivity(), uri,
//                        HistoryQuery.PROJECTION, where, null, Combined.VISITS + " DESC");
//                return loader;
//            }

            default: {
                throw new IllegalArgumentException();
            }
        }
    }
    
    /**
     * 以毫秒的形式返回明天凌晨的时间点
     * @return
     */
    public long getTomorrowZero(){
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_YEAR);
		
		calendar.set(Calendar.DAY_OF_YEAR, day + 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
        return  (calendar.getTimeInMillis());
	}

//    void selectGroup(int position) {
//        mGroupItemClickListener.onItemClick(null,
//                mAdapter.getGroupView(position, false, null, null),
//                position, position);
//    }

    void checkIfEmpty() {
        if (mAdapter.mMostVisited != null && mAdapter.mHistoryCursor != null) {
            // Both cursors have loaded - check to see if we have data
            if (mAdapter.isEmpty()) {
                mRoot.findViewById(R.id.history).setVisibility(View.GONE);
                mRoot.findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
            } else {
                mRoot.findViewById(R.id.history).setVisibility(View.VISIBLE);
                mRoot.findViewById(android.R.id.empty).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_HISTORY: {
            	int historyCount = 0;
            	if(data != null) {
            		historyCount = data.getCount();
            	}
            	if(historyCount == 0) {
            		tvClear.setEnabled(false);
            	}else {
            		tvClear.setEnabled(true);
            	}
            	historyAdapter.changeCursor(data);
//                mAdapter.changeCursor(data);
//                if (!mAdapter.isEmpty() && mGroupList != null
//                        && mGroupList.getCheckedItemPosition() == ListView.INVALID_POSITION) {
//                    selectGroup(0);
//                }

//                checkIfEmpty();
                break;
            }

//            case LOADER_MOST_VISITED: {
//                mAdapter.changeMostVisitedCursor(data);
//
//                checkIfEmpty();
//                break;
//            }

            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        mDisableNewWindow = args.getBoolean(BrowserBookmarksPage.EXTRA_DISABLE_WINDOW, false);
        int mvlimit = getResources().getInteger(R.integer.most_visits_limit);
        mMostVisitsLimit = Integer.toString(mvlimit);
        mCallback = (CombinedBookmarksCallbacks) getActivity();
        CHECK_ALL = getActivity().getResources().getString(R.string.select_all);
		DIS_CHECK_ALL = getActivity().getResources().getString(R.string.reverse_select);
    }
    
    protected void displayDeleteDialog(String elartMessage, final HashMap<Integer, Long> needDeleteIdsMap, final int historyCount) {
		
		new AuroraAlertDialog.Builder(getActivity())
			.setIconAttribute(android.R.attr.alertDialogIcon)
			.setTitle(getActivity().getResources().getString(R.string.autofill_profile_editor_delete_profile))
			.setMessage(elartMessage)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteCheckedItem(needDeleteIdsMap,historyCount);
				}
			})
			.setNegativeButton(R.string.cancel, null)
			.show();
	}
    
    /**
	 * 删除所选择的历史
	 */
	public void deleteCheckedItem(final HashMap<Integer, Long> needDeleteIdsMap, final int historyCount) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Iterator<Integer> iterator = needDeleteIdsMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					int pos = iterator.next();
					long id = needDeleteIdsMap.get(pos);
					Uri uri = ContentUris.withAppendedId(
							BrowserContract.History.CONTENT_URI, id);
					historyHandler.sendMessage(historyHandler.obtainMessage(DO_REMOVE_HISTORY_DATE, id));
					int rowCount = getActivity().getContentResolver().delete(uri, null, null);
					BrowserHistorysAdapter.checkedMap.put(pos, false);
				}
				Message message = historyHandler.obtainMessage(DO_CHECK_STATE);
				Bundle data = new Bundle();
				if(historyCount > 0) {
					data.putString("toast_content", getActivity().getResources().getString(R.string.delete_history_success));
				}
				data.putBoolean("changeToNormal", true);
				message.setData(data);
				historyHandler.sendMessageDelayed(message, 500);
			}
		}).start();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.history, container, false);
//        mAdapter = new HistoryAdapter(getActivity());
        ViewStub stub = (ViewStub) mRoot.findViewById(R.id.pref_stub);
        if (stub != null) {
//            inflateTwoPane(stub);
        } else {
            inflateSinglePane();
        }

        // Start the loaders
        getLoaderManager().restartLoader(LOADER_HISTORY, null, this);
//        getLoaderManager().restartLoader(LOADER_MOST_VISITED, null, this);

        return mRoot;
    }

    private void inflateSinglePane() {
    	titlebar = ((AuroraActivity)getActivity()).getAuroraActionBar();
    	titlebar.setTitle(getActivity().getResources().getString(R.string.historyed));
    	titlebar.addItem(R.layout.view_history_right_clear, CLEAR_HISTORY);
    	tvClear = (TextView)titlebar.findViewById(R.id.tv_history_clear);
    	tvClear.setOnClickListener(new ViewsOnClickListener());
    	titlebar.initActionBottomBarMenu(R.menu.historys_bottom_menu, 1);
    	titlebar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!BrowserHistorysAdapter.isInSelectionMode && mHistoryList != null) {
					mHistoryList.setSelection(0);
				}
			}
		});
        mHistoryList = (AuroraListView) mRoot.findViewById(R.id.history);
        mHistoryList.auroraSetNeedSlideDelete(true);
        mHistoryList.setOnItemClickListener(new OnHistorysItemClickListener());
        mHistoryList.auroraSetAuroraBackOnClickListener(new HistorysAuroraBackOnClickListener());
        historyAdapter = new BrowserHistorysAdapter(getActivity(),mHistoryList, this);
        changeToNormal();
//        mHistoryList.setAdapter(mAdapter);
//        mHistoryList.setOnChildClickListener(this);
        mHistoryList.setAdapter(historyAdapter);
        registerForContextMenu(mHistoryList);
    }

//	private void inflateTwoPane(ViewStub stub) {
//        stub.setLayoutResource(R.layout.preference_list_content);
//        stub.inflate();
//        mGroupList = (ListView) mRoot.findViewById(android.R.id.list);
//        mPrefsContainer = (ViewGroup) mRoot.findViewById(R.id.prefs_frame);
//        mFragmentBreadCrumbs = (FragmentBreadCrumbs) mRoot.findViewById(android.R.id.title);
//        mFragmentBreadCrumbs.setMaxVisible(1);
//        mFragmentBreadCrumbs.setActivity(getActivity());
//        mPrefsContainer.setVisibility(View.VISIBLE);
//        mGroupList.setAdapter(new HistoryGroupWrapper(mAdapter));
//        mGroupList.setOnItemClickListener(mGroupItemClickListener);
//        mGroupList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
//        mChildWrapper = new HistoryChildWrapper(mAdapter);
//        mChildList = new ListView(getActivity());
//        mChildList.setAdapter(mChildWrapper);
//        mChildList.setOnItemClickListener(mChildItemClickListener);
//        registerForContextMenu(mChildList);
//        ViewGroup prefs = (ViewGroup) mRoot.findViewById(R.id.prefs);
//        prefs.addView(mChildList);
//    }
    
    private class ViewsOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.tv_history_clear:
				prepareDisplayDeleteDialog(true);
				break;

			default:
				break;
			}
		}
    	
    }
    
//    private class OnActionBarItemClickListener implements OnAuroraActionBarItemClickListener {
//
//		@Override
//		public void onAuroraActionBarItemClicked(int itemId) {
//			switch (itemId) {
//			case CLEAR_HISTORY:
//				Toast.makeText(getActivity(), "testclear", 0).show();
//				break;
//		
//			default:
//				break;
//			}
//		}
//    }

//    private OnItemClickListener mGroupItemClickListener = new OnItemClickListener() {
//        @Override
//        public void onItemClick(
//                AdapterView<?> parent, View view, int position, long id) {
//            CharSequence title = ((TextView) view).getText();
//            mFragmentBreadCrumbs.setTitle(title, title);
//            mChildWrapper.setSelectedGroup(position);
//            mGroupList.setItemChecked(position, true);
//        }
//    };

    private OnItemClickListener mChildItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(
                AdapterView<?> parent, View view, int position, long id) {
            mCallback.openUrl(((HistoryItem) view).getUrl());
        }
    };

    @Override
    public boolean onChildClick(ExpandableListView parent, View view,
            int groupPosition, int childPosition, long id) {
        mCallback.openUrl(((HistoryItem) view).getUrl());
        return true;
    }
    
    private class OnHistorysItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (BrowserHistorysAdapter.isInSelectionMode) {
				historyAdapter.setCheckOrNot(position);
				historyAdapter.notifyDataSetChanged();
				historyAdapter.doCheckState();
				return;
			}
			
			Cursor cursor = historyAdapter.getItem(position);
			if (mCallback != null) {
				mCallback.openUrl(cursor.getString(HistoryQuery.INDEX_URL));
			}
		}

	}
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(LOADER_HISTORY);
        BrowserHistorysAdapter.setIsInSelectionMode(false);
//        getLoaderManager().destroyLoader(LOADER_MOST_VISITED);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.history, menu);
    }

    void promptToClearHistory() {
        final ContentResolver resolver = getActivity().getContentResolver();
        final ClearHistoryTask clear = new ClearHistoryTask(resolver);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.pref_privacy_clear_history_dlg)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         if (which == DialogInterface.BUTTON_POSITIVE) {
                             clear.start();
                         }
                     }
                });
        final Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_history_menu_id) {
            promptToClearHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class ClearHistoryTask extends Thread {
        ContentResolver mResolver;

        public ClearHistoryTask(ContentResolver resolver) {
            mResolver = resolver;
        }

        @Override
        public void run() {
            Browser.clearHistory(mResolver);
        }
    }

    View getTargetView(ContextMenuInfo menuInfo) {
        if (menuInfo instanceof AdapterContextMenuInfo) {
            return ((AdapterContextMenuInfo) menuInfo).targetView;
        }
        if (menuInfo instanceof ExpandableListContextMenuInfo) {
            return ((ExpandableListContextMenuInfo) menuInfo).targetView;
        }
        return null;
    }
    
    private class HistorysAuroraBackOnClickListener implements AuroraBackOnClickListener {
		
		//点击了垃圾桶的响应事件
		@Override
		public void auroraOnClick(int position) {
			displayDeleteSingleDialog(historyAdapter, position);
		}

		//准备滑动删除的响应事件，滑动之前允许用户做一些初始化操作
		@Override
		public void auroraPrepareDraged(int position) {
			
		}

		//成功拖出垃圾桶之后的响应事件
		@Override
		public void auroraDragedSuccess(int position) {
			
		}

		//进行了拖动垃圾桶操作，但是没有成功，比如只拖动了一点点
		@Override
		public void auroraDragedUnSuccess(int position) {
			
		}
		
	}
    
    /**
	 * 删除单条
	 * @param adapter
	 * @param position
	 */
	private void displayDeleteSingleDialog(BrowserHistorysAdapter adapter,
			int position) {
		// Put up a dialog asking if the user really wants to
		// delete the bookmark
		Cursor cursor = adapter.getItem(position);
		long id = cursor.getLong(BookmarksLoader.COLUMN_INDEX_ID);
//		String title = cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE);
		Uri uri = ContentUris.withAppendedId(
                 BrowserContract.History.CONTENT_URI,
                 id);
		BookmarkUtils.displayRemoveBookmarkHistoryDialog("历史记录", uri, getActivity(), 
				historyHandler.obtainMessage(DO_REMOVE_HISTORY_DATE, id));
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    	if (!BrowserHistorysAdapter.isInSelectionMode) {
			changeToDashboard(info.position);
		}
//        View targetView = getTargetView(menuInfo);
//        if (!(targetView instanceof HistoryItem)) {
//            return;
//        }
//        HistoryItem historyItem = (HistoryItem) targetView;
//
//        // Inflate the menu
//        Activity parent = getActivity();
//        MenuInflater inflater = parent.getMenuInflater();
//        inflater.inflate(R.menu.historycontext, menu);
//
//        // Setup the header
//        if (mContextHeader == null) {
//            mContextHeader = new HistoryItem(parent, false);
//            mContextHeader.setEnableScrolling(true);
//        } else if (mContextHeader.getParent() != null) {
//            ((ViewGroup) mContextHeader.getParent()).removeView(mContextHeader);
//        }
//        historyItem.copyTo(mContextHeader);
//        menu.setHeaderView(mContextHeader);
//
//        // Only show open in new tab if it was not explicitly disabled
//        if (mDisableNewWindow) {
//            menu.findItem(R.id.new_window_context_menu_id).setVisible(false);
//        }
//        // For a bookmark, provide the option to remove it from bookmarks
//        if (historyItem.isBookmark()) {
//            MenuItem item = menu.findItem(R.id.save_to_bookmarks_menu_id);
//            item.setTitle(R.string.remove_from_bookmarks);
//        }
//        // decide whether to show the share link option
//        PackageManager pm = parent.getPackageManager();
//        Intent send = new Intent(Intent.ACTION_SEND);
//        send.setType("text/plain");
//        ResolveInfo ri = pm.resolveActivity(send, PackageManager.MATCH_DEFAULT_ONLY);
//        menu.findItem(R.id.share_link_context_menu_id).setVisible(ri != null);
//
//        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void enableActionBarBottomMenu(int start, int end, boolean enable) {
    	AuroraMenu bottomMenu = titlebar.getAuroraActionBottomBarMenu();
		if (bottomMenu == null) {
			return;
		}
		for (int i = start; i <= end; i++) {
			bottomMenu.setBottomMenuItemEnable(i + 1, enable);
		}
	}

	protected void doClickSelectRightButton() {
    	TextView rightButton = (TextView) titlebar.getSelectRightButton();
    	historyAdapter.checkAllOrNot(rightButton.getText().toString()
				.equals(CHECK_ALL) ? true : false);
		// rightButton.setText(rightButton.getText().toString().equals(CHECK_ALL)
		// ? DIS_CHECK_ALL : CHECK_ALL);
    	historyAdapter.doCheckState();
	}
	
	private void changeToDashboard(int position) {
    	titlebar.setShowBottomBarMenu(true);
		titlebar.showActionBarDashBoard();
		// 取消按钮
		titlebar.getSelectLeftButton().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						titlebar.setShowBottomBarMenu(false);
						titlebar.showActionBarDashBoard();
						changeToNormal();
					}
				});
		// 全选按钮
		((TextView) titlebar.getSelectRightButton()).setText(CHECK_ALL);
		titlebar.getSelectRightButton().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						doClickSelectRightButton();
					}
				});
		if (historyAdapter != null) {
			BrowserHistorysAdapter.setIsInSelectionMode(true);
			historyAdapter.contextItemPos = position;
			historyAdapter.notifyDataSetChanged();
		}
		mHistoryList.auroraSetNeedSlideDelete(false);
		enableActionBarBottomMenu(0, 0, true);
	}

	public void changeToNormal() {
//    	if (titlebar.getItem(0) == null) {
//			titlebar.addItem(R.drawable.bookmark_actionbar_new_folder,
//					CREATE_FOLDER, "");
//		}
//		titlebar.setOnAuroraActionBarListener(new OnActionBarItemClickListener());
		if (historyAdapter != null) {
			BrowserHistorysAdapter.setIsInSelectionMode(false);
			historyAdapter.notifyDataSetChanged();
		}
    	mHistoryList.auroraSetNeedSlideDelete(true);
	}

	@Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuInfo menuInfo = item.getMenuInfo();
        if (menuInfo == null) {
            return false;
        }
        View targetView = getTargetView(menuInfo);
        if (!(targetView instanceof HistoryItem)) {
            return false;
        }
        HistoryItem historyItem = (HistoryItem) targetView;
        String url = historyItem.getUrl();
        String title = historyItem.getName();
        Activity activity = getActivity();
        switch (item.getItemId()) {
            case R.id.open_context_menu_id:
                mCallback.openUrl(url);
                return true;
            case R.id.new_window_context_menu_id:
                mCallback.openInNewTab(url);
                return true;
            case R.id.save_to_bookmarks_menu_id:
                if (historyItem.isBookmark()) {
                    Bookmarks.removeFromBookmarks(activity, activity.getContentResolver(),
                            url, title);
                } else {
                    Browser.saveBookmark(activity, title, url);
                }
                return true;
            case R.id.share_link_context_menu_id:
                Browser.sendString(activity, url,
                        activity.getText(R.string.choosertitle_sharevia).toString());
                return true;
            case R.id.copy_url_context_menu_id:
                copy(url);
                return true;
            case R.id.delete_context_menu_id:
                Browser.deleteFromHistory(activity.getContentResolver(), url);
                return true;
            case R.id.homepage_context_menu_id:
                BrowserSettings.getInstance().setHomePage(url);
                Toast.makeText(activity, R.string.homepage_set, Toast.LENGTH_LONG).show();
                return true;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private static abstract class HistoryWrapper extends BaseAdapter {

        protected HistoryAdapter mAdapter;
        private DataSetObserver mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                notifyDataSetInvalidated();
            }
        };

        public HistoryWrapper(HistoryAdapter adapter) {
            mAdapter = adapter;
            mAdapter.registerDataSetObserver(mObserver);
        }

    }
    private static class HistoryGroupWrapper extends HistoryWrapper {

        public HistoryGroupWrapper(HistoryAdapter adapter) {
            super(adapter);
        }

        @Override
        public int getCount() {
            return mAdapter.getGroupCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mAdapter.getGroupView(position, false, convertView, parent);
        }

    }

    private static class HistoryChildWrapper extends HistoryWrapper {

        private int mSelectedGroup;

        public HistoryChildWrapper(HistoryAdapter adapter) {
            super(adapter);
        }

        void setSelectedGroup(int groupPosition) {
            mSelectedGroup = groupPosition;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mAdapter.getChildrenCount(mSelectedGroup);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mAdapter.getChildView(mSelectedGroup, position,
                    false, convertView, parent);
        }

    }
    
    private class HistoryAdapter extends DateSortedExpandableListAdapter {

        private Cursor mMostVisited, mHistoryCursor;
        Drawable mFaviconBackground;

        HistoryAdapter(Context context) {
            super(context, HistoryQuery.INDEX_DATE_LAST_VISITED);
            mFaviconBackground = BookmarkUtils.createListFaviconBackground(context);
        }

        @Override
        public void changeCursor(Cursor cursor) {
            mHistoryCursor = cursor;
            super.changeCursor(cursor);
        }

        void changeMostVisitedCursor(Cursor cursor) {
            if (mMostVisited == cursor) {
                return;
            }
            if (mMostVisited != null) {
                mMostVisited.unregisterDataSetObserver(mDataSetObserver);
                mMostVisited.close();
            }
            mMostVisited = cursor;
            if (mMostVisited != null) {
                mMostVisited.registerDataSetObserver(mDataSetObserver);
            }
            notifyDataSetChanged();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            if (moveCursorToChildPosition(groupPosition, childPosition)) {
                Cursor cursor = getCursor(groupPosition);
                return cursor.getLong(HistoryQuery.INDEX_ID);
            }
            return 0;
        }

        @Override
        public int getGroupCount() {
            return super.getGroupCount() + (!isMostVisitedEmpty() ? 1 : 0);
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (groupPosition >= super.getGroupCount()) {
                if (isMostVisitedEmpty()) {
                    return 0;
                }
                return mMostVisited.getCount();
            }
            return super.getChildrenCount(groupPosition);
        }

        @Override
        public boolean isEmpty() {
            if (!super.isEmpty()) {
                return false;
            }
            return isMostVisitedEmpty();
        }

        private boolean isMostVisitedEmpty() {
            return mMostVisited == null
                    || mMostVisited.isClosed()
                    || mMostVisited.getCount() == 0;
        }

        Cursor getCursor(int groupPosition) {
            if (groupPosition >= super.getGroupCount()) {
                return mMostVisited;
            }
            return mHistoryCursor;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            if (groupPosition >= super.getGroupCount()) {
                if (mMostVisited == null || mMostVisited.isClosed()) {
                    throw new IllegalStateException("Data is not valid");
                }
                TextView item;
                if (null == convertView || !(convertView instanceof TextView)) {
                    LayoutInflater factory = LayoutInflater.from(getContext());
                    item = (TextView) factory.inflate(R.layout.history_header, null);
                } else {
                    item = (TextView) convertView;
                }
                item.setText(R.string.tab_most_visited);
                return item;
            }
            return super.getGroupView(groupPosition, isExpanded, convertView, parent);
        }

        @Override
        boolean moveCursorToChildPosition(
                int groupPosition, int childPosition) {
            if (groupPosition >= super.getGroupCount()) {
                if (mMostVisited != null && !mMostVisited.isClosed()) {
                    mMostVisited.moveToPosition(childPosition);
                    return true;
                }
                return false;
            }
            return super.moveCursorToChildPosition(groupPosition, childPosition);
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            HistoryItem item;
            if (null == convertView || !(convertView instanceof HistoryItem)) {
                item = new HistoryItem(getContext());
                // Add padding on the left so it will be indented from the
                // arrows on the group views.
                item.setPadding(item.getPaddingLeft() + 10,
                        item.getPaddingTop(),
                        item.getPaddingRight(),
                        item.getPaddingBottom());
                item.setFaviconBackground(mFaviconBackground);
            } else {
                item = (HistoryItem) convertView;
            }

            // Bail early if the Cursor is closed.
            if (!moveCursorToChildPosition(groupPosition, childPosition)) {
                return item;
            }

            Cursor cursor = getCursor(groupPosition);
            item.setName(cursor.getString(HistoryQuery.INDEX_TITE));
            String url = cursor.getString(HistoryQuery.INDEX_URL);
            item.setUrl(url);
            byte[] data = cursor.getBlob(HistoryQuery.INDEX_FAVICON);
            if (data != null) {
                item.setFavicon(BitmapFactory.decodeByteArray(data, 0,
                        data.length));
            }
            return item;
        }
    }

	@Override
	public void checkState(int state) {
		if (BrowserHistorysAdapter.isInSelectionMode) {
			String rightBtnMessage = CHECK_ALL;
			if (state == BrowserHistorysAdapter.STATE_NO_CHECKED) {
				enableActionBarBottomMenu(0, 0, false);
			} else if (state == BrowserHistorysAdapter.STATE_CHECKED_SOME) {
				enableActionBarBottomMenu(0, 0, true);
			} else {
				rightBtnMessage = DIS_CHECK_ALL;
				enableActionBarBottomMenu(0, 0, true);
			}
			((TextView) titlebar.getSelectRightButton())
					.setText(rightBtnMessage);
		}
	}

	public void prepareDisplayDeleteDialog(final boolean clear) {
		
		if(clear) {
			historyAdapter.checkAllOrNot(true);
		}
		
		new Thread(new Runnable() {
			// 首先获取需要删除的历史的id，因为id是通过item得到的，而每删除一个历史，会影响item，导致出错
			HashMap<Integer, Long> needDeleteIdsMap = new HashMap<Integer, Long>();
			int historyCount = 0;

			@Override
			public void run() {
				Iterator<Integer> iterator = BrowserHistorysAdapter.checkedMap
						.keySet().iterator();
				while (iterator.hasNext()) {
					int position = iterator.next();
					if (BrowserHistorysAdapter.checkedMap.get(position)) {
						try {
							Cursor cursor = historyAdapter.getItem(position);
							if(cursor != null) {
								long id = cursor
										.getLong(HistoryQuery.INDEX_ID);
								needDeleteIdsMap.put(position, id);
								historyCount++;
							}
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
				Message message = historyHandler
						.obtainMessage(DISPLAY_DELETEDIALOG);
				message.arg1 = historyCount;
				if(clear) {
					message.arg2 = 1;
				}
				message.obj = needDeleteIdsMap;
				historyHandler.sendMessage(message);
			}
		}).start();
	}
}
