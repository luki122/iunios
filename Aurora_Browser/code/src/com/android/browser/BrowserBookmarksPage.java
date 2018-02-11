/*
 * Copyright (C) 2006 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.WebAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BrowserContract;
import android.provider.BrowserContract.Accounts;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraMenu;

import com.android.browser.BrowserBookmarksAdapter.AllCheckedObserver;
import com.android.browser.provider.BrowserProvider2;
import com.android.browser.view.BookmarkExpandableView;
import com.android.browser.view.BookmarkExpandableView.BookmarkContextMenuInfo;

import org.json.JSONException;
import org.json.JSONObject;

interface BookmarksPageCallbacks {
	// Return true if handled
	boolean onBookmarkSelected(Cursor c, boolean isFolder);
	
	boolean onBookmarkSelected(String url);

	// Return true if handled
	boolean onOpenInNewWindow(String... urls);
}

/**
 * View showing the user's bookmarks in the browser.
 */
public class BrowserBookmarksPage extends Fragment implements
		View.OnCreateContextMenuListener,
		LoaderManager.LoaderCallbacks<Cursor>, BreadCrumbView.Controller,
		AllCheckedObserver {

	private static ArrayList<BookmarkFolderInfo> bookmarksFolderIdsStack = new ArrayList<BookmarkFolderInfo>();

	private static String CHECK_ALL;
	private static String DIS_CHECK_ALL;
	private static final int DO_CHECK_STATE = 0;
	private static final int DISPLAY_DELETEDIALOG = 1;
	private static final int DISPLAY_EDITDIALOG = 2;
	private static final int MOVE_SELECT_FOLDER_ACTIVITY = 3;
	private static final int SHOW_TOAST = MOVE_SELECT_FOLDER_ACTIVITY + 1;

	// public static class ExtraDragState {
	// public int childPosition;
	// public int groupPosition;
	// }

	static final String LOGTAG = "browser";

	static final int LOADER_ACCOUNTS = 1;
	private final int LOADER_BOOKMARKS = 100;

	static final String EXTRA_DISABLE_WINDOW = "disable_new_window";
	static final String PREF_GROUP_STATE = "bbp_group_state";

	static final String ACCOUNT_TYPE = "account_type";
	static final String ACCOUNT_NAME = "account_name";

	private long mIdRootFolder;
	private int mLastYPos;

	private static final int CREATE_FOLDER = 0;
	private static final int EDIT_FOLDER = 1;

	public static final int SELECT_NEW_FOLDER_RESULT = 10;
	public static final int SELECT_EDIT_BOOKMARK_RESULT = SELECT_NEW_FOLDER_RESULT + 1;
	public static final int SELECT_EDIT_FOLDER_RESULT = SELECT_EDIT_BOOKMARK_RESULT + 1;
	public static final int SELECT_MOVE_FOLDER_BOOKMARK_RESULT = SELECT_EDIT_FOLDER_RESULT + 1;

	private Handler bookmarkHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DO_CHECK_STATE:
				adapter.doCheckState();
				Bundle bundle = msg.getData();
				if(bundle != null) {
					String toastContent = bundle.getString("toast_content");
					if(toastContent != null) {
						Toast.makeText(getActivity(), toastContent, Toast.LENGTH_SHORT).show();
					}
					if(bundle.getBoolean("changeToNormal")) {
						titlebar.setShowBottomBarMenu(false);
						titlebar.showActionBarDashBoard();
						changeToNormal();
					}
				}
				break;

			case DISPLAY_DELETEDIALOG: {
				String dialogMessage = getDeleteDialogMessage(msg.arg1, msg.arg2);
				HashMap<Integer, Long> needDeleteIdsMap = (HashMap<Integer, Long>) msg.obj;
				displayDeleteDialog(dialogMessage, needDeleteIdsMap, msg.arg1, msg.arg2);
				break;
			}
			
			case DISPLAY_EDITDIALOG: {
				HashMap<Integer, BookmarksInfo> needEditIdsMap = (HashMap<Integer, BookmarksInfo>)msg.obj;
				Iterator<Integer> iterator = needEditIdsMap.keySet().iterator();
				if(iterator.hasNext()) {
					int position = iterator.next();
					BookmarksInfo info = needEditIdsMap.get(position);
					if(info.isFolder) {
						displayNewOrEditFolderDialog(EDIT_FOLDER, info.title, info.selfId, position);
					}else {
						displayEditBookmarkDialog(info.favicon, info.title, info.url, info.selfId, position);
//						editBookmark(adapter, position);
					}
				}
				break;
			}
			
			case MOVE_SELECT_FOLDER_ACTIVITY: {
				ArrayList<Integer> folderIdsList = new ArrayList<Integer>();
				int allCount = msg.arg1;
				int folderCount = msg.arg2;
				if(allCount != 0 && folderCount == 0) {
					//全是书签
					moveKind = 1;
				}else {
					Iterator<Integer> iterator = needMoveIdsMap.keySet().iterator();
					while(iterator.hasNext()) {
						int position = iterator.next();
						BookmarksInfo info = needMoveIdsMap.get(position);
						if(info.isFolder) {
							folderIdsList.add((int) info.selfId);
						}
					}
					if(allCount == folderCount) {
						//全是文件夹
						moveKind = 0;
					}else {
						//混合
						moveKind = 2;
					}
				}
				Intent intent = new Intent(getActivity(), BookmarksFolderActivity.class);
				intent.putExtra("selected_id", selectedMoveFolderBookmarkId);
				if(folderIdsList.size() != 0) {
					intent.putExtra("move_folder_ids", folderIdsList);
				}
				getActivity().startActivityForResult(intent, SELECT_MOVE_FOLDER_BOOKMARK_RESULT);
				break;
			}
			
			case SHOW_TOAST:
				Toast.makeText(getActivity(), (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}
	};

	private class BookmarkFolderInfo {
		public long folderId;
		public int lastPos;
		public String lastTitle;
	}

	BookmarksPageCallbacks mCallbacks;
	View mRoot;
	AuroraActionBar titlebar;
	AuroraListView mList;
	boolean mDisableNewWindow;
	boolean mEnableContextMenu = true;
	private View mEmptyView;
	View mHeader;
	
	// HashMap<Integer, BrowserBookmarksAdapter> mBookmarkAdapters = new
	// HashMap<Integer, BrowserMBookmarksAdapter>();
	BrowserBookmarksAdapter adapter;
	JSONObject mState;
	
	protected TextView tvSelectedNewFolder; //新建文件夹
	protected long selectedNewFolderId;
	protected String selectedNewFolderName;
	
	protected long selectedEditFolderId; //编辑文件夹
	protected String selectedEditFolderName;
	
	protected TextView tvSelectedEditBookmark; //编辑书签
	protected long selectedEditBookmarkId;
	protected String selectedEditBookmarkName;
	
	protected long selectedMoveFolderBookmarkId; //移动文件夹书签
	HashMap<Integer, BookmarksInfo> needMoveIdsMap = new HashMap<Integer, BookmarksInfo>();
	private int moveKind = 0;
	
	private long currentFolderId; //当前所在文件夹的id
	
	private ArrayList<BrowserBookmarksAdapterItem> list = new ArrayList<BrowserBookmarksAdapterItem>();
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == LOADER_ACCOUNTS) {
			return new AccountsLoader(getActivity());
		} else if (id == LOADER_BOOKMARKS) {
			String accountType = args.getString(ACCOUNT_TYPE);
			String accountName = args.getString(ACCOUNT_NAME);
			BookmarksLoader bl = new BookmarksLoader(getActivity(),
					accountType, accountName);
			return bl;
		} else {
			throw new UnsupportedOperationException("Unknown loader id " + id);
		}
	}

	protected void displayDeleteDialog(String dialogMessage,
			final HashMap<Integer, Long> needDeleteIdsMap, final int folderCount, final int bookmarkCount) {
		
		new AuroraAlertDialog.Builder(getActivity())
			.setIconAttribute(android.R.attr.alertDialogIcon)
			.setTitle(getActivity().getResources().getString(R.string.autofill_profile_editor_delete_profile))
			.setMessage(dialogMessage)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteCheckedItem(needDeleteIdsMap,folderCount,bookmarkCount);
				}
			})
			.setNegativeButton(R.string.cancel, null)
			.show();
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() == LOADER_ACCOUNTS) {
			LoaderManager lm = getLoaderManager();
			int id = LOADER_BOOKMARKS;
			if (cursor.moveToNext()) {
				String accountName = cursor.getString(0);
				String accountType = cursor.getString(1);
				mIdRootFolder = cursor.getLong(2);
				Bundle args = new Bundle();
				args.putString(ACCOUNT_NAME, accountName);
				args.putString(ACCOUNT_TYPE, accountType);
				adapter = new BrowserBookmarksAdapter(getActivity(), mList,
						this,list);
				mList.setAdapter(adapter);
				// mBookmarkAdapters.put(id, adapter);
				boolean expand = true;
				try {
					expand = mState
							.getBoolean(accountName != null ? accountName
									: BookmarkExpandableView.LOCAL_ACCOUNT_NAME);
				} catch (JSONException e) {
				} // no state for accountName
					// mList.addAccount(accountName, adapter, expand);
				lm.restartLoader(id, args, this);
				id++;
			}
			// TODO: Figure out what a reload of these means
			// Currently, a reload is triggered whenever bookmarks change
			// This is less than ideal
			// It also causes UI flickering as a new adapter is created
			// instead of re-using an existing one when the account_name is the
			// same.
			// For now, this is a one-shot load
			getLoaderManager().destroyLoader(LOADER_ACCOUNTS);
		} else if (loader.getId() == LOADER_BOOKMARKS) {
			// BrowserBookmarksAdapter adapter =
			// mBookmarkAdapters.get(loader.getId());
			Log.i("browser", "onLoadFinished--------------------");
			setBookmarkData(cursor);
			adapter.notifyDataSetChanged();
			// mList.scrollTo(0, mLastYPos);
		}
	}

	private void setBookmarkData(Cursor c) {
		list.clear();
		while(c.moveToNext()) {
			BrowserBookmarksAdapterItem item = new BrowserBookmarksAdapterItem();
			item.id = c.getLong(BookmarksLoader.COLUMN_INDEX_ID);
			Bitmap thumbnail = item.thumbnail != null ? item.thumbnail.getBitmap()
					: null;
			thumbnail = BrowserBookmarksPage.getBitmap(c,
					BookmarksLoader.COLUMN_INDEX_FAVICON, thumbnail);
			item.has_thumbnail = thumbnail != null;
			if (thumbnail != null
					&& (item.thumbnail == null || item.thumbnail.getBitmap() != thumbnail)) {
				item.thumbnail = new BitmapDrawable(getActivity().getResources(),
						thumbnail);
			}
			item.is_folder = c.getInt(BookmarksLoader.COLUMN_INDEX_IS_FOLDER) != 0;
			item.title = c.getString(BookmarksLoader.COLUMN_INDEX_TITLE);
			item.url = c.getString(BookmarksLoader.COLUMN_INDEX_URL);
			
			list.add(item);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!(item.getMenuInfo() instanceof BookmarkContextMenuInfo)) {
			return false;
		}
		BookmarkContextMenuInfo i = (BookmarkContextMenuInfo) item
				.getMenuInfo();
		// If we have no menu info, we can't tell which item was selected.
		if (i == null) {
			return false;
		}

		if (handleContextItem(item.getItemId(), i.groupPosition,
				i.childPosition)) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public boolean handleContextItem(int itemId, int groupPosition,
			int childPosition) {
		final Activity activity = getActivity();
		BrowserBookmarksAdapter adapter = getChildAdapter(groupPosition);

		switch (itemId) {
		case R.id.open_context_menu_id:
			loadUrl(adapter, childPosition);
			break;
		case R.id.edit_context_menu_id:
			editBookmark(adapter, childPosition);
			break;
		case R.id.shortcut_context_menu_id:
//			Cursor c = adapter.getItem(childPosition);
//			activity.sendBroadcast(createShortcutIntent(getActivity(), c));
			break;
		case R.id.delete_context_menu_id:
			displayDeleteSingleDialog(adapter, childPosition);
			break;
		case R.id.new_window_context_menu_id:
			openInNewWindow(adapter, childPosition);
			break;
		case R.id.share_link_context_menu_id: {
//			Cursor cursor = adapter.getItem(childPosition);
//			Controller.sharePage(activity,
//					cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE),
//					cursor.getString(BookmarksLoader.COLUMN_INDEX_URL),
//					getBitmap(cursor, BookmarksLoader.COLUMN_INDEX_FAVICON),
//					getBitmap(cursor, BookmarksLoader.COLUMN_INDEX_THUMBNAIL));
			break;
		}
		case R.id.copy_url_context_menu_id:
			copy(getUrl(adapter, childPosition));
			break;
		case R.id.homepage_context_menu_id: {
			BrowserSettings.getInstance().setHomePage(
					getUrl(adapter, childPosition));
			Toast.makeText(activity, R.string.homepage_set, Toast.LENGTH_LONG)
					.show();
			break;
		}
		// Only for the Most visited page
		case R.id.save_to_bookmarks_menu_id: {
//			Cursor cursor = adapter.getItem(childPosition);
//			String name = cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE);
//			String url = cursor.getString(BookmarksLoader.COLUMN_INDEX_URL);
//			// If the site is bookmarked, the item becomes remove from
//			// bookmarks.
//			Bookmarks.removeFromBookmarks(activity,
//					activity.getContentResolver(), url, name);
			break;
		}
		default:
			return false;
		}
		return true;
	}
	
	/**
     * 显示编辑书签的对话框
     */
    public void displayEditBookmarkDialog(Bitmap favicon, final String title, final String url, final long thisId, final int position) {
    	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity());
    	View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_bookmark_crt_page_dialog, null);
    	LinearLayout llChooseSaveFolder = (LinearLayout)view.findViewById(R.id.ll_add_to_bookmarks_save_foler);
    	llChooseSaveFolder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), BookmarksFolderActivity.class);
				intent.putExtra("selected_id", selectedEditBookmarkId);
				intent.putExtra("selected_title", selectedEditBookmarkName);
				getActivity().startActivityForResult(intent, SELECT_EDIT_BOOKMARK_RESULT);
			}
		});
    	builder.setTitle(getActivity().getResources().getString(R.string.edit_bookmark));
    	builder.setView(view);
    	((TextView)view.findViewById(R.id.tv_add_to_bookmarks_location)).setText(getActivity().getResources().getString(R.string.pref_privacy_location_title));
    	tvSelectedEditBookmark = (TextView)view.findViewById(R.id.tv_add_to_bookmarks_save_foler);
    	tvSelectedEditBookmark.setText(selectedEditBookmarkName);
    	ImageView ivThumb = (ImageView)view.findViewById(R.id.iv_add_to_bookmarks_thumb);
    	final AuroraEditText etTitle = (AuroraEditText)view.findViewById(R.id.et_add_to_bookmarks_title);
    	final AuroraEditText etUrl = (AuroraEditText)view.findViewById(R.id.et_add_to_bookmarks_url);
    	if(favicon != null) ivThumb.setImageBitmap(favicon);
    	if(title != null) {
    		etTitle.setText(title);
    		etTitle.setSelection(title.length());
    	}
    	if(url != null) etUrl.setText(url);
    	
    	builder.setNegativeButton(R.string.cancel, null);
    	builder.setPositiveButton(R.string.autofill_profile_editor_save_profile,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            	editBookmark(title.trim(), etTitle.getText().toString().trim(), url.trim(), etUrl.getText().toString().trim(), thisId, position);
            }
        });
    	
    	AuroraAlertDialog dialog = builder.create();
    	dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		etTitle.addTextChangedListener(new EditTextTextWatcher(dialog,etTitle,etUrl));
		etUrl.addTextChangedListener(new EditTextTextWatcher(dialog,etTitle,etUrl));
		
		dialog.show();
    }

	protected void editBookmark(final String origTitle, final String title, final String origUrl, final String url, final long thisId, final int position) {
		if(title.equals("")) {
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.please_input_the_bookmark_name), Toast.LENGTH_SHORT).show();
			return;
		}
		if(url.equals("")) {
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.please_input_the_bookmark_url), Toast.LENGTH_SHORT).show();
			return;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Uri uri = ContentUris.withAppendedId(
						BrowserContract.Bookmarks.CONTENT_URI, thisId);
				ContentValues value = new ContentValues();
				value.put(BookmarksLoader.PROJECTION[BookmarksLoader.COLUMN_INDEX_TITLE], title);
				WebAddress address = new WebAddress(url);
				value.put(BookmarksLoader.PROJECTION[BookmarksLoader.COLUMN_INDEX_URL], address.toString());
				value.put(BookmarksLoader.PROJECTION[BookmarksLoader.COLUMN_INDEX_PARENT], selectedEditBookmarkId);
				value.put(android.provider.BrowserContract.Bookmarks.DATE_CREATED, System.currentTimeMillis());
				
				boolean doEdit = !isCurrentFolder(selectedEditBookmarkId) || !origTitle.equals(title) || !origUrl.equals(address.toString());
				//如果将要移动的文件夹下存在相同的书签，并且将要移动的文件夹不在当前文件夹，则把其删掉
				long bookmarkId;
				if((bookmarkId = Bookmarks.getBookmarkIdInTheGivedFolder(getActivity(), getActivity().getContentResolver(), address.toString(), title, selectedEditBookmarkId)) != -100 &&
						doEdit) {
					Uri sameUri = ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, bookmarkId);
					getActivity().getContentResolver().delete(sameUri, null, null);
				}
				
				
				if(doEdit) {
					getActivity().getContentResolver().update(uri, value, null, null);
				}
				
//				ContentValues valueImage = new ContentValues();
//				valueImage.put(Images.URL, url);
//				getActivity().getContentResolver().update(BrowserContract.Images.CONTENT_URI, valueImage, null, null);
				BrowserBookmarksAdapter.checkedMap.put(position, false);
				Message message = bookmarkHandler.obtainMessage(DO_CHECK_STATE);
				Bundle data = new Bundle();
				if(doEdit) {
					data.putString("toast_content", getActivity().getString(R.string.edit_success,getActivity().getResources().getString(R.string.shortcut_bookmark)));
				}
				data.putBoolean("changeToNormal", true);
				message.setData(data);
				bookmarkHandler.sendMessageDelayed(message, 500);
			}
		}).start();
	}

	protected boolean isCurrentFolder(long moveFolderId) {
		if(bookmarksFolderIdsStack.size() == 0) {
			return 1 == moveFolderId;
		}else {
			return bookmarksFolderIdsStack.get(bookmarksFolderIdsStack.size() - 1).folderId == moveFolderId;
		}
	}

	static Bitmap getBitmap(Cursor cursor, int columnIndex) {
		return getBitmap(cursor, columnIndex, null);
	}

	static ThreadLocal<Options> sOptions = new ThreadLocal<Options>() {
		@Override
		protected Options initialValue() {
			return new Options();
		};
	};

	static Bitmap getBitmap(Cursor cursor, int columnIndex, Bitmap inBitmap) {
		byte[] data = cursor.getBlob(columnIndex);
		if (data == null) {
			return null;
		}
		Options opts = sOptions.get();
		opts.inBitmap = inBitmap;
		opts.inSampleSize = 1;
		opts.inScaled = false;
		try {
			return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		} catch (IllegalArgumentException ex) {
			// Failed to re-use bitmap, create a new one
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
	}

	private MenuItem.OnMenuItemClickListener mContextItemClickListener = new MenuItem.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			return onContextItemSelected(item);
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		// Cursor cursor = adapter.getItem(info.position);
		// if (!canEdit(cursor)) {
		// return;
		// }
		// boolean isFolder = cursor
		// .getInt(BookmarksLoader.COLUMN_INDEX_IS_FOLDER) != 0;

		// final Activity activity = getActivity();
		// MenuInflater inflater = activity.getMenuInflater();
		// inflater.inflate(R.menu.bookmarkscontext, menu);
		// if (isFolder) {
		// menu.setGroupVisible(R.id.FOLDER_CONTEXT_MENU, true);
		// } else {
		// menu.setGroupVisible(R.id.BOOKMARK_CONTEXT_MENU, true);
		// if (mDisableNewWindow) {
		// menu.findItem(R.id.new_window_context_menu_id)
		// .setVisible(false);
		// }
		// }
		// Toast.makeText(activity, info.position + " " + (isFolder ? "文件夹" :
		// "文件"), 0).show();
		if (!BrowserBookmarksAdapter.isInSelectionMode) {
			changeToDashboard(info.position);
		}

		// BookmarkItem header = new BookmarkItem(activity);
		// header.setEnableScrolling(true);
		// populateBookmarkItem(cursor, header, isFolder);
		// menu.setHeaderView(header);
		//
		// int count = menu.size();
		// for (int i = 0; i < count; i++) {
		// menu.getItem(i).setOnMenuItemClickListener(
		// mContextItemClickListener);
		// }
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
		if (adapter != null) {
			BrowserBookmarksAdapter.setIsInSelectionMode(true);
			adapter.contextItemPos = position;
			adapter.notifyDataSetChanged();
		}
		mList.auroraSetNeedSlideDelete(false);
		enableActionBarBottomMenu(0, 2, true);
	}

	private void doClickSelectRightButton() {
		TextView rightButton = (TextView) titlebar.getSelectRightButton();
		adapter.checkAllOrNot(rightButton.getText().toString()
				.equals(CHECK_ALL) ? true : false);
		// rightButton.setText(rightButton.getText().toString().equals(CHECK_ALL)
		// ? DIS_CHECK_ALL : CHECK_ALL);
		adapter.doCheckState();
	}

	public void changeToNormal() {
		if (titlebar.getItem(0) == null) {
			titlebar.addItem(R.drawable.bookmark_actionbar_new_folder,
					CREATE_FOLDER, "");
		}
		titlebar.setOnAuroraActionBarListener(new OnActionBarItemClickListener());
		if (adapter != null) {
			adapter.doDisappearingAnim = true;
			BrowserBookmarksAdapter.setIsInSelectionMode(false);
			adapter.notifyDataSetChanged();
		}
		mList.auroraSetNeedSlideDelete(true);
	}

	boolean canEdit(Cursor c) {
		int type = c.getInt(BookmarksLoader.COLUMN_INDEX_TYPE);
		return type == BrowserContract.Bookmarks.BOOKMARK_TYPE_BOOKMARK
				|| type == BrowserContract.Bookmarks.BOOKMARK_TYPE_FOLDER;
	}

	private void populateBookmarkItem(Cursor cursor, BookmarkItem item,
			boolean isFolder) {
		item.setName(cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE));
		if (isFolder) {
			item.setUrl(null);
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_folder_holo_dark);
			item.setFavicon(bitmap);
			new LookupBookmarkCount(getActivity(), item).execute(cursor
					.getLong(BookmarksLoader.COLUMN_INDEX_ID));
		} else {
			String url = cursor.getString(BookmarksLoader.COLUMN_INDEX_URL);
			item.setUrl(url);
			Bitmap bitmap = getBitmap(cursor,
					BookmarksLoader.COLUMN_INDEX_FAVICON);
			item.setFavicon(bitmap);
		}
	}

	/**
	 * Create a new BrowserBookmarksPage.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		SharedPreferences prefs = BrowserSettings.getInstance()
				.getPreferences();
		try {
			mState = new JSONObject(prefs.getString(PREF_GROUP_STATE, "{}"));
		} catch (JSONException e) {
			// Parse failed, clear preference and start with empty state
			prefs.edit().remove(PREF_GROUP_STATE).apply();
			mState = new JSONObject();
		}
		Bundle args = getArguments();
		mDisableNewWindow = args == null ? false : args.getBoolean(
				EXTRA_DISABLE_WINDOW, false);
		setHasOptionsMenu(true);
		if (mCallbacks == null
				&& getActivity() instanceof CombinedBookmarksCallbacks) {
			mCallbacks = new CombinedBookmarksCallbackWrapper(
					(CombinedBookmarksCallbacks) getActivity());
		}
		CHECK_ALL = getActivity().getResources().getString(R.string.select_all);
		DIS_CHECK_ALL = getActivity().getResources().getString(R.string.reverse_select);
	}

	@Override
	public void onPause() {
		super.onPause();
		// try {
		// mState = mList.saveGroupState();
		// Save state
		SharedPreferences prefs = BrowserSettings.getInstance()
				.getPreferences();
		prefs.edit().putString(PREF_GROUP_STATE, mState.toString()).apply();
		// } catch (JSONException e) {
		// Not critical, ignore
		// }
	}

	private static class CombinedBookmarksCallbackWrapper implements
			BookmarksPageCallbacks {

		private CombinedBookmarksCallbacks mCombinedCallback;

		private CombinedBookmarksCallbackWrapper(CombinedBookmarksCallbacks cb) {
			mCombinedCallback = cb;
		}

		@Override
		public boolean onOpenInNewWindow(String... urls) {
			mCombinedCallback.openInNewTab(urls);
			return true;
		}

		@Override
		public boolean onBookmarkSelected(Cursor c, boolean isFolder) {
			if (isFolder) {
				return false;
			}
			String strUrl = BrowserBookmarksPage.getUrl(c);
			WebAddress address = new WebAddress(strUrl);
			mCombinedCallback.openUrl(address.toString());
			return true;
		}

		@Override
		public boolean onBookmarkSelected(String url) {
			WebAddress address = new WebAddress(url);
			mCombinedCallback.openUrl(address.toString());
			return true;
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.bookmarks, container, false);
		titlebar = ((AuroraActivity) getActivity()).getAuroraActionBar();
		currentFolderId = 1;
		selectedNewFolderId = currentFolderId;
		selectedNewFolderName = getActivity().getResources().getString(R.string.shortcut_bookmark);
		titlebar.setTitle(selectedNewFolderName);
		
		selectedEditBookmarkId = selectedNewFolderId;
		selectedEditBookmarkName = selectedNewFolderName;
		
		selectedEditFolderId = selectedNewFolderId;
		selectedEditFolderName = selectedNewFolderName;
		
		selectedMoveFolderBookmarkId = selectedNewFolderId;
		titlebar.initActionBottomBarMenu(R.menu.bookmarks_bottom_menu, 3);
		titlebar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!BrowserBookmarksAdapter.isInSelectionMode && mList != null) {
					mList.setSelection(0);
				}
			}
		});
		titlebar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {
			
			@Override
			public void onAuroraActionBarBackItemClicked(int itemId) {
				if(hasParent()) {
					backToParent();
				}else {
					getActivity().finish();
				}
			}
		});
		
		mEmptyView = mRoot.findViewById(android.R.id.empty);

		mList = (AuroraListView) mRoot.findViewById(R.id.list);
		mList.setOnItemClickListener(new OnBookmarksItemClickListener());
		mList.auroraSetAuroraBackOnClickListener(new BookmarksAuroraBackOnClickListener());
		changeToNormal();
		// mList.setOnChildClickListener(this);
		// mList.setColumnWidthFromLayout(R.layout.bookmark_thumbnail);
		// mList.setBreadcrumbController(this);
		// 是否注册上下文菜单
		setEnableContextMenu(mEnableContextMenu);

		

		// Start the loaders
		LoaderManager lm = getLoaderManager();
		lm.restartLoader(LOADER_ACCOUNTS, null, this);

		return mRoot;
	}

	/**
	 * 设置底部menu按钮可以或不可用
	 * 
	 * @param enable
	 */
	public void enableActionBarBottomMenu(int start, int end, boolean enable) {
		AuroraMenu bottomMenu = titlebar.getAuroraActionBottomBarMenu();
		if (bottomMenu == null) {
			return;
		}
		for (int i = start; i <= end; i++) {
			bottomMenu.setBottomMenuItemEnable(i + 1, enable);
		}
	}

	private class OnActionBarItemClickListener implements
			OnAuroraActionBarItemClickListener {

		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case CREATE_FOLDER:
				displayNewOrEditFolderDialog(CREATE_FOLDER,null,0,0);
				break;

			default:
				break;
			}
		}

	}

	private void displayNewOrEditFolderDialog(final int dialogKind, final String title, final long selfId, final int position) {

		final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity());
		if(dialogKind == CREATE_FOLDER) {
			builder.setTitle(getActivity().getResources().getString(R.string.new_folder));
		}else if(dialogKind == EDIT_FOLDER) {
			builder.setTitle(getActivity().getResources().getString(R.string.edit_folder));
		}
		
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.view_bookmark_new_folder, null);
		//文件夹名称输入框
		final EditText etFolderName = (EditText) view.findViewById(R.id.et_new_folder_name);
		builder.setView(view);
		builder.setNegativeButton(getActivity().getResources().getString(R.string.cancel), null);
		builder.setPositiveButton(getActivity().getResources().getString(R.string.autofill_profile_editor_save_profile), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String folderName = etFolderName.getText().toString().trim();
				if (TextUtils.isEmpty(folderName)) {
					Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.please_input_the_foldername), 0).show();
					return;
				}
				if(dialogKind == CREATE_FOLDER) {
					newFolder(folderName,selectedNewFolderId);
				}else if(dialogKind == EDIT_FOLDER) {
					editFolder(title,folderName,selfId,selectedEditFolderId,position);
				}
			}
		});
		final AuroraAlertDialog dialog = builder.create();
		
		//图标
		ImageView ivIcon = (ImageView)view.findViewById(R.id.iv_bookmark_new_folder_icon);
		
		//标题
		TextView tvTitle = (TextView)view.findViewById(R.id.tv_bookmark_new_folder_title);
		
		etFolderName.setHint(getActivity().getString(R.string.folder_name));
		etFolderName.setText(" ");
		etFolderName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				//获取按钮对象
		        Button positiveButton=((AuroraAlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
		        if(positiveButton != null) {
		        	if(etFolderName.getText().toString().equals("")) {
		        		positiveButton.setEnabled(false);
		        	}else {
		        		positiveButton.setEnabled(true);
		        	}
		        }else {
		        	if(dialogKind == CREATE_FOLDER) {
		        		bookmarkHandler.postDelayed(new Runnable() {
		        			public void run() {
		        				etFolderName.setText("");
		        			}
		        		}, 150);
		        	}
		        }
			}
		});
		
		// 位置
		((TextView) view.findViewById(R.id.tv_bookmark_new_folder_location)).setText(getActivity().getResources().getString(R.string.pref_privacy_location_title));
		
		tvSelectedNewFolder = (TextView)view.findViewById(R.id.tv_bookmark_new_folder_choose_foler);
		
		
		LinearLayout llChooseFolder = (LinearLayout) view
				.findViewById(R.id.ll_bookmark_new_folder);
		llChooseFolder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						BookmarksFolderActivity.class);
				if(dialogKind == CREATE_FOLDER) {
					intent.putExtra("selected_id", selectedNewFolderId);
					intent.putExtra("selected_title", selectedNewFolderName);
					getActivity().startActivityForResult(intent,
							SELECT_NEW_FOLDER_RESULT);
				}else if(dialogKind == EDIT_FOLDER) {
					ArrayList<Integer> folderIdsList = new ArrayList<Integer>();
					folderIdsList.add((int) selfId);
					intent.putExtra("selected_id", selectedEditFolderId);
					intent.putExtra("selected_title", selectedEditFolderName);
					intent.putExtra("move_folder_ids", folderIdsList);
					getActivity().startActivityForResult(intent,
							SELECT_EDIT_FOLDER_RESULT);
				}
			}
		});
		
		if(dialogKind == CREATE_FOLDER) {
			ivIcon.setVisibility(View.VISIBLE);
			tvTitle.setGravity(Gravity.LEFT);
			tvTitle.setText(getActivity().getResources().getString(R.string.new_folder));
			etFolderName.setText("");
			tvSelectedNewFolder.setText(titlebar.getTitleView().getText().toString());
			selectedNewFolderId = currentFolderId;
		}else if(dialogKind == EDIT_FOLDER) {
			ivIcon.setVisibility(View.GONE);
			tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
			tvTitle.setText(getActivity().getResources().getString(R.string.edit_folder));
			etFolderName.setText(title);
			etFolderName.setSelection(title.length());
			tvSelectedNewFolder.setText(selectedEditFolderName);
		}
		
		//显示输入法
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	protected void editFolder(String origFolderName, String folderName, long selfId, long parentId, int position) {
		// update the folder in the database
		boolean doEdit = !isCurrentFolder(parentId) || !origFolderName.equals(folderName);
		boolean hasSameFolder = Bookmarks.getFolderIdInTheGivedFolder(getActivity(), folderName, parentId) != -100;
		if(doEdit) {
			if(!hasSameFolder) {
				ContentValues values = new ContentValues();
				values.put(BrowserContract.Bookmarks.TITLE, folderName);
				values.put(BrowserContract.Bookmarks.PARENT, parentId);
				values.put(android.provider.BrowserContract.Bookmarks.DATE_CREATED, System.currentTimeMillis());
				
				Uri uri = ContentUris.withAppendedId(
						BrowserContract.Bookmarks.CONTENT_URI,
						selfId);
				getActivity().getContentResolver().update(uri, values, null, null);
			}else {
				Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.the_same_folder_exists), Toast.LENGTH_SHORT).show();
			}
		}
		if(doEdit && hasSameFolder) {
			return;
		}
		BrowserBookmarksAdapter.checkedMap.put(position, false);
		Message message = bookmarkHandler.obtainMessage(DO_CHECK_STATE);
		Bundle data = new Bundle();
		if(doEdit) {
			data.putString("toast_content", getActivity().getString(R.string.edit_success,getActivity().getResources().getString(R.string.folder)));
		}
		data.putBoolean("changeToNormal", true);
		message.setData(data);
		bookmarkHandler.sendMessageDelayed(message, 500);
	}

	/**
	 * 创建新文件夹
	 * @param folderName
	 * @param parentId
	 * @return
	 */
	private long newFolder(String folderName,long parentId) {
		// Add the folder to the database
		ContentValues values = new ContentValues();
		values.put(BrowserContract.Bookmarks.TITLE, folderName);
		values.put(BrowserContract.Bookmarks.IS_FOLDER, 1);
		values.put(BrowserContract.Bookmarks.PARENT, parentId);
		
		if(Bookmarks.getFolderIdInTheGivedFolder(getActivity(), folderName, parentId) == -100) {
			Uri uri = getActivity().getContentResolver().insert(
					BrowserContract.Bookmarks.CONTENT_URI, values);
			if (uri != null) {
				return ContentUris.parseId(uri);
			}
		}else {
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.the_same_folder_exists), Toast.LENGTH_SHORT).show();
		}
		return -1;
		// descendInto(name, id);
	}

	private class BookmarksAuroraBackOnClickListener implements
			AuroraBackOnClickListener {

		// 点击了垃圾桶的响应事件
		@Override
		public void auroraOnClick(int position) {
			displayDeleteSingleDialog(adapter, position);
		}

		// 准备滑动删除的响应事件，滑动之前允许用户做一些初始化操作
		@Override
		public void auroraPrepareDraged(int position) {

		}

		// 成功拖出垃圾桶之后的响应事件
		@Override
		public void auroraDragedSuccess(int position) {

		}

		// 进行了拖动垃圾桶操作，但是没有成功，比如只拖动了一点点
		@Override
		public void auroraDragedUnSuccess(int position) {

		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// mList.setBreadcrumbController(null);
		// mList.clearAccounts();
		LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ACCOUNTS);
		// for (int id : mBookmarkAdapters.keySet()) {
		// synchronized (mBookmarkAdapters.get(id).mCursorLock) {
		// lm.destroyLoader(id);
		// }
		// }
		// mBookmarkAdapters.clear();
	}

	private BrowserBookmarksAdapter getChildAdapter(int groupPosition) {
		// return mList.getChildAdapter(groupPosition);
		return null;
	}

	private BreadCrumbView getBreadCrumbs(int groupPosition) {
		// return mList.getBreadCrumbs(groupPosition);
		return null;
	}

	private class OnBookmarksItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (BrowserBookmarksAdapter.isInSelectionMode) {
				adapter.setCheckOrNot(position);
				adapter.notifyDataSetChanged();
				adapter.doCheckState();
				return;
			}

			BrowserBookmarksAdapterItem item = list.get(position);
			if (!item.is_folder && mCallbacks != null) {
				mCallbacks.onBookmarkSelected(item.url);
				// return true;
			}

			if (item.is_folder) {
				selectedNewFolderId = item.id;
				selectedNewFolderName = item.title;
				
				currentFolderId = selectedNewFolderId;
				
				selectedEditBookmarkId = selectedNewFolderId;
				selectedEditBookmarkName = selectedNewFolderName;
				
				selectedEditFolderId = selectedNewFolderId;
				selectedEditFolderName = selectedNewFolderName;
				
				selectedMoveFolderBookmarkId = selectedNewFolderId;
				
				Uri uri = ContentUris.withAppendedId(
						BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER,
						selectedNewFolderId);
				pushIdStack(selectedNewFolderId);
				// BreadCrumbView crumbs = getBreadCrumbs(position);
				// if (crumbs != null) {
				// // update crumbs
				// crumbs.pushView(title, uri);
				// crumbs.setVisibility(View.VISIBLE);
				// }
				mLastYPos = 0;
				loadFolder(0, uri);
				titlebar.setTitle(selectedNewFolderName);
			}
		}
	}

	private void pushIdStack(long id) {
		boolean exist = false;
		for (BookmarkFolderInfo folderInfo : bookmarksFolderIdsStack) {
			if (folderInfo.folderId == id) {
				exist = true;
				break;
			}
		}
		if (!exist) {
			BookmarkFolderInfo info = new BookmarkFolderInfo();
			info.folderId = id;
			info.lastPos = getCurrentListYPos();
			info.lastTitle = titlebar.getTitleView().getText().toString();
			bookmarksFolderIdsStack.add(info);
		}
	}

	private int getCurrentListYPos() {
		View c = mList.getChildAt(0);
		if (c == null) {
			return 0;
		}
		int firstVisiblePosition = mList.getFirstVisiblePosition();
		int top = c.getTop();
		return -top + firstVisiblePosition * c.getHeight();
	}

	// @Override
	// public boolean onChildClick(ExpandableListView parent, View v,
	// int groupPosition, int childPosition, long id) {
	// BrowserBookmarksAdapter adapter = getChildAdapter(groupPosition);
	// Cursor cursor = adapter.getItem(childPosition);
	// boolean isFolder = cursor.getInt(BookmarksLoader.COLUMN_INDEX_IS_FOLDER)
	// != 0;
	// if (mCallbacks != null &&
	// mCallbacks.onBookmarkSelected(cursor, isFolder)) {
	// return true;
	// }
	//
	// if (isFolder) {
	// String title = cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE);
	// Uri uri = ContentUris.withAppendedId(
	// BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER, id);
	// BreadCrumbView crumbs = getBreadCrumbs(groupPosition);
	// if (crumbs != null) {
	// // update crumbs
	// crumbs.pushView(title, uri);
	// crumbs.setVisibility(View.VISIBLE);
	// }
	// loadFolder(groupPosition, uri);
	// }
	// return true;
	// }

	static Intent createShortcutIntent(Context context, Cursor cursor) {
		String url = cursor.getString(BookmarksLoader.COLUMN_INDEX_URL);
		String title = cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE);
		Bitmap touchIcon = getBitmap(cursor,
				BookmarksLoader.COLUMN_INDEX_TOUCH_ICON);
		Bitmap favicon = getBitmap(cursor, BookmarksLoader.COLUMN_INDEX_FAVICON);
		return BookmarkUtils.createAddToHomeIntent(context, url, title,
				touchIcon, favicon);
	}

	private void loadUrl(BrowserBookmarksAdapter adapter, int position) {
//		if (mCallbacks != null && adapter != null) {
//			mCallbacks.onBookmarkSelected(adapter.getItem(position), false);
//		}
	}

	private void openInNewWindow(BrowserBookmarksAdapter adapter, int position) {
//		if (mCallbacks != null) {
//			Cursor c = adapter.getItem(position);
//			boolean isFolder = c.getInt(BookmarksLoader.COLUMN_INDEX_IS_FOLDER) == 1;
//			if (isFolder) {
//				long id = c.getLong(BookmarksLoader.COLUMN_INDEX_ID);
//				new OpenAllInTabsTask(id).execute();
//			} else {
//				mCallbacks.onOpenInNewWindow(BrowserBookmarksPage.getUrl(c));
//			}
//		}
	}

	class OpenAllInTabsTask extends AsyncTask<Void, Void, Cursor> {
		long mFolderId;

		public OpenAllInTabsTask(long id) {
			mFolderId = id;
		}

		@Override
		protected Cursor doInBackground(Void... params) {
			Context c = getActivity();
			if (c == null)
				return null;
			return c.getContentResolver().query(
					BookmarkUtils.getBookmarksUri(c),
					BookmarksLoader.PROJECTION,
					BrowserContract.Bookmarks.PARENT + "=?",
					new String[] { Long.toString(mFolderId) }, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			if (mCallbacks != null && result.getCount() > 0) {
				String[] urls = new String[result.getCount()];
				int i = 0;
				while (result.moveToNext()) {
					urls[i++] = BrowserBookmarksPage.getUrl(result);
				}
				mCallbacks.onOpenInNewWindow(urls);
			}
		}

	}

	private void editBookmark(BrowserBookmarksAdapter adapter, int position) {
//		Intent intent = new Intent(getActivity(), AddBookmarkPage.class);
//		Cursor cursor = adapter.getItem(position);
//		Bundle item = new Bundle();
//		item.putString(BrowserContract.Bookmarks.TITLE,
//				cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE));
//		item.putString(BrowserContract.Bookmarks.URL,
//				cursor.getString(BookmarksLoader.COLUMN_INDEX_URL));
//		byte[] data = cursor.getBlob(BookmarksLoader.COLUMN_INDEX_FAVICON);
//		if (data != null) {
//			item.putParcelable(BrowserContract.Bookmarks.FAVICON,
//					BitmapFactory.decodeByteArray(data, 0, data.length));
//		}
//		item.putLong(BrowserContract.Bookmarks._ID,
//				cursor.getLong(BookmarksLoader.COLUMN_INDEX_ID));
//		item.putLong(BrowserContract.Bookmarks.PARENT,
//				cursor.getLong(BookmarksLoader.COLUMN_INDEX_PARENT));
//		intent.putExtra(AddBookmarkPage.EXTRA_EDIT_BOOKMARK, item);
//		intent.putExtra(AddBookmarkPage.EXTRA_IS_FOLDER,
//				cursor.getInt(BookmarksLoader.COLUMN_INDEX_IS_FOLDER) == 1);
//		startActivity(intent);
	}

	/**
	 * 删除单条
	 * @param adapter
	 * @param position
	 */
	private void displayDeleteSingleDialog(BrowserBookmarksAdapter adapter,
			int position) {
		// Put up a dialog asking if the user really wants to
		// delete the bookmark
//		String title = cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE);
		BrowserBookmarksAdapterItem item = list.get(position);
		
		Uri uri = ContentUris.withAppendedId(
                BrowserContract.Bookmarks.CONTENT_URI,
                item.id);
		BookmarkUtils.displayRemoveBookmarkHistoryDialog(item.is_folder ? getActivity().getResources().getString(R.string.this_folder) : 
			getActivity().getResources().getString(R.string.this_bookmark), uri, getActivity(), null);
	}

	private String getUrl(BrowserBookmarksAdapter adapter, int position) {
//		return getUrl(adapter.getItem(position));
		return list.get(position).url;
	}

	/* package */static String getUrl(Cursor c) {
		return c.getString(BookmarksLoader.COLUMN_INDEX_URL);
	}

	private void copy(CharSequence text) {
		ClipboardManager cm = (ClipboardManager) getActivity()
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cm.setPrimaryClip(ClipData.newRawUri(null, Uri.parse(text.toString())));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Resources res = getActivity().getResources();
		// mList.setColumnWidthFromLayout(R.layout.bookmark_thumbnail);
		int paddingTop = (int) res.getDimension(R.dimen.combo_paddingTop);
		mRoot.setPadding(0, paddingTop, 0, 0);
		getActivity().invalidateOptionsMenu();
	}

	/**
	 * BreadCrumb controller callback
	 */
	@Override
	public void onTop(BreadCrumbView view, int level, Object data) {
		int groupPosition = (Integer) view.getTag(R.id.group_position);
		Uri uri = (Uri) data;
		if (uri == null) {
			// top level
			uri = BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER;
		}
		loadFolder(groupPosition, uri);
		if (level <= 1) {
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * @param uri
	 */
	private void loadFolder(int groupPosition, Uri uri) {
		adapter.doDisappearingAnim = false;
		LoaderManager manager = getLoaderManager();
		// This assumes groups are ordered the same as loaders
		BookmarksLoader loader = (BookmarksLoader) ((Loader<?>) manager
				.getLoader(LOADER_BOOKMARKS));
		loader.setUri(uri);
		loader.forceLoad();
	}

	public void setCallbackListener(BookmarksPageCallbacks callbackListener) {
		mCallbacks = callbackListener;
	}

	public void setEnableContextMenu(boolean enable) {
		mEnableContextMenu = enable;
		if (mList != null) {
			if (mEnableContextMenu) {
				registerForContextMenu(mList);
			} else {
				unregisterForContextMenu(mList);
				mList.setLongClickable(false);
			}
		}
	}

	private static class LookupBookmarkCount extends
			AsyncTask<Long, Void, Integer> {
		Context mContext;
		BookmarkItem mHeader;

		public LookupBookmarkCount(Context context, BookmarkItem header) {
			mContext = context.getApplicationContext();
			mHeader = header;
		}

		@Override
		protected Integer doInBackground(Long... params) {
			if (params.length != 1) {
				throw new IllegalArgumentException("Missing folder id!");
			}
			Uri uri = BookmarkUtils.getBookmarksUri(mContext);
			Cursor c = null;
			try {
				c = mContext.getContentResolver().query(uri, null,
						BrowserContract.Bookmarks.PARENT + "=?",
						new String[] { params[0].toString() }, null);

				return c.getCount();
			} finally {
				if (c != null) {
					c.close();
				}
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result > 0) {
				mHeader.setUrl(mContext.getString(
						R.string.contextheader_folder_bookmarkcount, result));
			} else if (result == 0) {
				mHeader.setUrl(mContext
						.getString(R.string.contextheader_folder_empty));
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		BrowserBookmarksAdapter.setIsInSelectionMode(false);
		bookmarksFolderIdsStack.clear();
	}

	static class AccountsLoader extends CursorLoader {

		static String[] ACCOUNTS_PROJECTION = new String[] {
				Accounts.ACCOUNT_NAME, Accounts.ACCOUNT_TYPE, Accounts.ROOT_ID };

		public AccountsLoader(Context context) {
			super(context, Accounts.CONTENT_URI
					.buildUpon()
					.appendQueryParameter(
							BrowserProvider2.PARAM_ALLOW_EMPTY_ACCOUNTS,
							"true").build(), ACCOUNTS_PROJECTION, null, null,
					null);
		}

	}

	public boolean hasParent() {
		return bookmarksFolderIdsStack.size() > 0;
	}

	/**
	 * 返回上一级目录
	 */
	public void backToParent() {
		if (bookmarksFolderIdsStack.size() > 1) {
			selectedNewFolderId = bookmarksFolderIdsStack
					.get(bookmarksFolderIdsStack.size() - 2).folderId;
		} else {
			selectedNewFolderId = mIdRootFolder;
		}
		currentFolderId = selectedNewFolderId;
		selectedEditBookmarkId = selectedNewFolderId;
		selectedEditFolderId = selectedNewFolderId;
		selectedMoveFolderBookmarkId = selectedNewFolderId;
		Uri uri = ContentUris.withAppendedId(
				BrowserContract.Bookmarks.CONTENT_URI_DEFAULT_FOLDER, selectedNewFolderId);
		mLastYPos = bookmarksFolderIdsStack
				.get(bookmarksFolderIdsStack.size() - 1).lastPos;
		loadFolder(0, uri);
		selectedNewFolderName = bookmarksFolderIdsStack.get(bookmarksFolderIdsStack.size() - 1).lastTitle;
		
		selectedEditBookmarkName = selectedNewFolderName;
		
		selectedEditFolderName = selectedNewFolderName;
		
		titlebar.setTitle(selectedNewFolderName);
		popIdStack();
		mList.auroraSetRubbishBackNoAnim();
	}

	private void popIdStack() {
		if (bookmarksFolderIdsStack.size() == 1) {
			bookmarksFolderIdsStack.clear();
		} else {
			bookmarksFolderIdsStack.remove(bookmarksFolderIdsStack.size() - 1);
		}
	}

	/**
	 * 删除所选择的书签
	 */
	public void deleteCheckedItem(final HashMap<Integer, Long> needDeleteIdsMap, final int folderCount, final int bookmarkCount) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Iterator<Integer> iterator = needDeleteIdsMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					int pos = iterator.next();
					long id = needDeleteIdsMap.get(pos);
					Uri uri = ContentUris.withAppendedId(
							BrowserContract.Bookmarks.CONTENT_URI, id);
					getActivity().getContentResolver().delete(uri, null, null);
					BrowserBookmarksAdapter.checkedMap.put(pos, false);
				}
				Message message = bookmarkHandler.obtainMessage(DO_CHECK_STATE);
				Bundle data = new Bundle();
				data.putString("toast_content", 
						(folderCount > 0 ? getActivity().getResources().getString(folderCount == 1 ? R.string.folder_small : R.string.folders_small) : "") 
					  + (folderCount > 0 && bookmarkCount > 0 ? getActivity().getResources().getString(R.string.and) : "") 
					  + (bookmarkCount > 0 ? getActivity().getResources().getString(bookmarkCount == 1 ? R.string.bookmark_small : R.string.bookmarks_small) : "") 
					  + getActivity().getResources().getString(R.string.deleted_success));
				data.putBoolean("changeToNormal", true);
				message.setData(data);
				bookmarkHandler.sendMessageDelayed(message, 500);
			}
		}).start();
	}

	public void prepareDisplayDeleteDialog() {
		new Thread(new Runnable() {
			// 首先获取需要删除的书签的id，因为id是通过item得到的，而每删除一个书签，会影响item，导致出错
			HashMap<Integer, Long> needDeleteIdsMap = new HashMap<Integer, Long>();
			int folderCount = 0;
			int bookmarkCount = 0;

			@Override
			public void run() {
				Iterator<Integer> iterator = BrowserBookmarksAdapter.checkedMap
						.keySet().iterator();
				while (iterator.hasNext()) {
					int position = iterator.next();
					if (BrowserBookmarksAdapter.checkedMap.get(position)) {
						BrowserBookmarksAdapterItem origItem = list.get(position);
						needDeleteIdsMap.put(position, origItem.id);
						if (origItem.is_folder) {
							// folder
							folderCount++;
						} else {
							bookmarkCount++;
						}
					}
				}
				Message message = bookmarkHandler
						.obtainMessage(DISPLAY_DELETEDIALOG);
				message.arg1 = folderCount;
				message.arg2 = bookmarkCount;
				message.obj = needDeleteIdsMap;
				bookmarkHandler.sendMessage(message);
			}
		}).start();
	}
	
	/**
	 * 移动书签，跳转到位置界面
	 */
	public void prepareToBookmarksFolderActivity() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				needMoveIdsMap.clear();
				Iterator<Integer> iterator = BrowserBookmarksAdapter.checkedMap
						.keySet().iterator();
				int allCount = 0;
				int folderCount = 0;
				while (iterator.hasNext()) {
					int position = iterator.next();
					if (BrowserBookmarksAdapter.checkedMap.get(position)) {
						allCount++;
						BrowserBookmarksAdapterItem origItem = list.get(position);
						BookmarksInfo item = new BookmarksInfo();
//						item.favicon = BrowserBookmarksPage.getBitmap(cursor, BookmarksLoader.COLUMN_INDEX_FAVICON);
						item.title = origItem.title;
						item.url = origItem.url;
						item.selfId = origItem.id;
						item.isFolder = origItem.is_folder;
						if(item.isFolder) {
							folderCount++;
						}
						needMoveIdsMap.put(position, item);
					}
				}
				Message message = bookmarkHandler
						.obtainMessage(MOVE_SELECT_FOLDER_ACTIVITY);
				message.arg1 = allCount;
				message.arg2 = folderCount;
				bookmarkHandler.sendMessage(message);
			}
		}).start();
		
	}

	public void prepareDisplayEditDialog() {
		new Thread(new Runnable() {
			HashMap<Integer, BookmarksInfo> needEditIdsMap = new HashMap<Integer, BookmarksInfo>();

			@Override
			public void run() {
				Iterator<Integer> iterator = BrowserBookmarksAdapter.checkedMap
						.keySet().iterator();
				while (iterator.hasNext()) {
					int position = iterator.next();
					if (BrowserBookmarksAdapter.checkedMap.get(position)) {
						BrowserBookmarksAdapterItem origItem = list.get(position);
						BookmarksInfo item = new BookmarksInfo();
						if(!origItem.is_folder) {
							if(origItem.thumbnail == null) {
								item.favicon = BitmapFactory.decodeResource(getResources(), R.drawable.default_icon_title);
							}else {
								item.favicon =  origItem.thumbnail.getBitmap();
							}
							item.url = origItem.url;
						}
						item.title = origItem.title;
						item.selfId = origItem.id;
						item.isFolder = origItem.is_folder;
						needEditIdsMap.put(position, item);
					}
				}
				Message message = bookmarkHandler
						.obtainMessage(DISPLAY_EDITDIALOG);
				message.obj = needEditIdsMap;
				bookmarkHandler.sendMessage(message);
			}
		}).start();
	}

	protected String getDeleteDialogMessage(int folderCount, int bookmarkCount) {
		String message = "";
		if (folderCount == 0 && bookmarkCount != 0) {
			if (bookmarkCount == 1) {
				message += getActivity().getResources().getString(R.string.if_delete_the) + getActivity().getResources().getString(R.string.this_bookmark) + "？";
			} else {
				message += getActivity().getString(R.string.if_delete_history,bookmarkCount,getActivity().getResources()
						.getString(bookmarkCount == 1 ? R.string.bookmark_small : R.string.bookmarks_small));
			}
		} else if (folderCount != 0 && bookmarkCount == 0) {
			if (folderCount == 1) {
				message += getActivity().getResources().getString(R.string.if_delete_the) + getActivity().getResources().getString(R.string.this_folder) + "？";
			} else {
				message += getActivity().getString(R.string.if_delete_history,folderCount,getActivity().getResources()
						.getString(folderCount == 1 ? R.string.folder_small : R.string.folders_small));
			}
		} else if (folderCount != 0 && bookmarkCount != 0) {
			message += getActivity().getString(
					R.string.if_delete_bookmark_folder,
				    bookmarkCount,
				    getActivity().getResources().getString(bookmarkCount == 1 ? R.string.bookmark_small : R.string.bookmarks_small),
				    folderCount,
				    getActivity().getResources().getString(folderCount == 1 ? R.string.folder_small : R.string.folders_small)
					);
				    
		}
		return message;
	}

	@Override
	public void checkState(int state) {
		if (BrowserBookmarksAdapter.isInSelectionMode) {
			String rightBtnMessage = CHECK_ALL;
			if (state == BrowserBookmarksAdapter.STATE_NO_CHECKED) {
				enableActionBarBottomMenu(0, 2, false);
			} else if (state == BrowserBookmarksAdapter.STATE_CHECKED_ONE) {
				enableActionBarBottomMenu(0, 2, true);
			} else if (state == BrowserBookmarksAdapter.STATE_CHECKED_ONE_BUT_ALL) {
				rightBtnMessage = DIS_CHECK_ALL;
				enableActionBarBottomMenu(0, 2, true);
			} else if (state == BrowserBookmarksAdapter.STATE_CHECKED_SOME) {
				enableActionBarBottomMenu(0, 1, true);
				enableActionBarBottomMenu(2, 2, false);
			} else {
				rightBtnMessage = DIS_CHECK_ALL;
				enableActionBarBottomMenu(0, 1, true);
				enableActionBarBottomMenu(2, 2, false);
			}
			((TextView) titlebar.getSelectRightButton())
					.setText(rightBtnMessage);
		}
	}

	public void setSelectedNewFolderResult(Intent intent) {
		selectedNewFolderId = intent.getLongExtra("selected_id", 1);
		selectedNewFolderName = intent.getStringExtra("selected_title");
		selectedNewFolderName = (selectedNewFolderName.equals("Bookmarks") 
				? getActivity().getResources().getString(R.string.bookmarks)
				: selectedNewFolderName);
		if (tvSelectedNewFolder != null && selectedNewFolderName != null) {
			tvSelectedNewFolder.setText(selectedNewFolderName);
		}
	}
	
	public void setSelectedEditBookmarkResult(Intent intent) {
		selectedEditBookmarkId = intent.getLongExtra("selected_id", 1);
		selectedEditBookmarkName = intent.getStringExtra("selected_title");
		selectedEditBookmarkName = (selectedEditBookmarkName.equals("Bookmarks") 
				? getActivity().getResources().getString(R.string.bookmarks)
				: selectedEditBookmarkName);
		if (tvSelectedEditBookmark != null && selectedEditBookmarkName != null) {
			tvSelectedEditBookmark.setText(selectedEditBookmarkName);
		}
	}
	
	public void setSelectedEditFolderResult(Intent intent) {
		selectedEditFolderId = intent.getLongExtra("selected_id", 1);
		selectedEditFolderName = intent.getStringExtra("selected_title");
		selectedEditFolderName = (selectedEditFolderName.equals("Bookmarks") 
				? getActivity().getResources().getString(R.string.bookmarks)
				: selectedEditFolderName);
		if (tvSelectedNewFolder != null && selectedEditFolderName != null) {
			tvSelectedNewFolder.setText(selectedEditFolderName);
		}
	}
	
	public void moveSelectedFolderBookmarks(Intent intent) {
		final long selectedParentId = intent.getLongExtra("selected_id", 1);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int moveCount = 0;
				Iterator<Integer> iterator = needMoveIdsMap.keySet().iterator();
				while(iterator.hasNext()) {
					int position = iterator.next();
					BookmarksInfo info = needMoveIdsMap.get(position);
					if(info.isFolder && Bookmarks.getFolderIdInTheGivedFolder(getActivity(), info.title, selectedParentId) != -100) {
						bookmarkHandler.sendMessage(bookmarkHandler.obtainMessage(SHOW_TOAST, getActivity().getResources().getString(R.string.the_same_folder_exists)));
						return;
					}
				}
				
				iterator = null;
				iterator = needMoveIdsMap.keySet().iterator();
				while(iterator.hasNext()) {
					int position = iterator.next();
					BookmarksInfo info = needMoveIdsMap.get(position);
					ContentValues values = new ContentValues();
					values.put(BrowserContract.Bookmarks.PARENT, selectedParentId);
					values.put(android.provider.BrowserContract.Bookmarks.DATE_CREATED, System.currentTimeMillis());
					
					//如果移动书签，且目标文件夹下存在相同的书签，则把其删掉
					if(!info.isFolder) {
						long bookmarkId;
						if((bookmarkId = Bookmarks.getBookmarkIdInTheGivedFolder(getActivity(), getActivity().getContentResolver(), info.url, info.title, selectedParentId)) != -100 &&
								!isCurrentFolder(selectedParentId)) {
							Uri sameUri = ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, bookmarkId);
							getActivity().getContentResolver().delete(sameUri, null, null);
						}
					}
					
					Uri uri = ContentUris.withAppendedId(
							BrowserContract.Bookmarks.CONTENT_URI, info.selfId);
					int updateCount = getActivity().getContentResolver().update(uri, values, null, null);
					if(updateCount == 1) {
						moveCount++;
						BrowserBookmarksAdapter.checkedMap.put(position, false);
						
						info = null;
						uri = null;
						values = null;
					}
				}
				
				if(moveCount == needMoveIdsMap.size()) {
					Message message = bookmarkHandler.obtainMessage(DO_CHECK_STATE);
					Bundle data = new Bundle();
					String toastContent = getActivity().getResources().getString(R.string.folder_small);
					if(moveKind == 1) {
						toastContent = getActivity().getResources().getString(R.string.bookmark_small);
					}else if(moveKind == 2) {
						toastContent = getActivity().getResources().getString(R.string.folder_small)
								     + getActivity().getResources().getString(R.string.and)
								     + getActivity().getResources().getString(R.string.bookmark_small);
					}
					toastContent += getActivity().getResources().getString(R.string.moved_success);
					data.putString("toast_content", toastContent);
					data.putBoolean("changeToNormal", true);
					message.setData(data);
					bookmarkHandler.sendMessageDelayed(message, 500);
				}
			}
		}).start();
		
	}

	private class BookmarksInfo {
		public Bitmap favicon;
		public String title;
		public String url;
		public long selfId;
		public boolean isFolder;
	}



}
