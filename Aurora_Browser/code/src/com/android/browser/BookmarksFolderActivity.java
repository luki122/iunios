package com.android.browser;

import java.util.ArrayList;

import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BrowserContract.Bookmarks;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;

import com.android.browser.BrowserBookmarksPage.AccountsLoader;

public class BookmarksFolderActivity extends AuroraActivity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private static final int LOADER_ACCOUNTS = 0;
	private static final int LOADER_BOOKMARKS = 1;
	
	private AuroraActionBar actionBar;
	private AuroraListView listView;
	
	private ArrayList<FolderInfo> folderList = new ArrayList<FolderInfo>();
	private ArrayList<Long> parentIdList = new ArrayList<Long>();
	private ArrayList<Long> movedParentIds = new ArrayList<Long>();
	
	private boolean sortingFolderList;
	
	private BookmarksFolderAdapter adapter;
	private long selectedFolderId;
	private String selectedFolderName;
	private ArrayList<Integer> folderIdsList;
	
	private final int maxSpaceIconShow = 150; //图标显示的最大的宽度,dp
	private final int eachSpaceDefault = 25; //默认宽度，dp
	public static final int rootFolderLeftMargin = 8; //根目录距离左边界的距离，dp
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.bookmarks_folder_activity, AuroraActionBar.Type.Normal);
		Intent intent = getIntent();
		if(intent != null) {
			selectedFolderId = intent.getLongExtra("selected_id", 1);
			selectedFolderName = intent.getStringExtra("selected_title");
			folderIdsList = (ArrayList<Integer>) intent.getSerializableExtra("move_folder_ids");
		}
		actionBar = getAuroraActionBar();
		if(actionBar != null) {
			actionBar.setTitle(getResources().getString(R.string.pref_privacy_location_title));
		}
		listView = (AuroraListView)findViewById(R.id.lv_bookmarks_folder);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				FolderInfo folderInfo = folderList.get(position);
				selectedFolderId = folderInfo.id;
				selectedFolderName = folderInfo.title;
				Intent data = new Intent();
				data.putExtra("selected_id", selectedFolderId);
				data.putExtra("selected_title", selectedFolderName);
				setResult(Controller.SELECT_FOLDER_RESULT, data);
				adapter.setSelectedId(selectedFolderId);
				adapter.notifyDataSetChanged();
				getLoaderManager().destroyLoader(LOADER_BOOKMARKS);
				finish();
			}
		});
		LoaderManager lm = getLoaderManager();
		lm.restartLoader(LOADER_ACCOUNTS, null, this);
		adapter = new BookmarksFolderAdapter(this, folderList, selectedFolderId);
		listView.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		BaseUi.changeStatusBar(this,true);
		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == LOADER_ACCOUNTS) {
			return new AccountsLoader(this);
		} 
		else if (id == LOADER_BOOKMARKS) {
			String accountType = args.getString(BrowserBookmarksPage.ACCOUNT_NAME);
			String accountName = args.getString(BrowserBookmarksPage.ACCOUNT_NAME);
			BookmarksFolderLoader bl = new BookmarksFolderLoader(this,
					accountType, accountName, "_id asc");
			return bl;
		} 
		else {
			throw new UnsupportedOperationException("Unknown loader id " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() == LOADER_ACCOUNTS) {
			LoaderManager lm = getLoaderManager();
			int id = LOADER_BOOKMARKS;
			while (cursor.moveToNext()) {
				String accountName = cursor.getString(0);
				String accountType = cursor.getString(1);
//				mIdRootFolder = cursor.getLong(2);
				Bundle args = new Bundle();
				args.putString(BrowserBookmarksPage.ACCOUNT_NAME, accountName);
				args.putString(BrowserBookmarksPage.ACCOUNT_TYPE, accountType);
				lm.restartLoader(id, args, this);
				id++;
			}
			getLoaderManager().destroyLoader(LOADER_ACCOUNTS);
		}else if(loader.getId() == LOADER_BOOKMARKS) {
			if(!sortingFolderList) {
				sortingFolderList = true;
				folderList.clear();
				while(cursor.moveToNext()) {
					FolderInfo folderInfo = new FolderInfo();
					folderInfo.id = cursor.getLong(cursor.getColumnIndex(Bookmarks._ID));
					folderInfo.title = cursor.getString(cursor.getColumnIndex(Bookmarks.TITLE));
					folderInfo.parent = cursor.getLong(cursor.getColumnIndex(Bookmarks.PARENT));
					if(folderInfo.parent == 1) {
						folderInfo.level = 1;
					}
					folderInfo.createTime = cursor.getLong(cursor.getColumnIndex(Bookmarks.DATE_CREATED));
					folderList.add(folderInfo);
				}
				
				addParentIds();
				
//				Log.i("browser", "----------------pre------------------");
//				for(FolderInfo info : folderList) {
//					Log.i("browser", info.id + "     " + info.parent + "     " + info.title + "      " + info.createTime);
//				}
//				Log.i("browser", "\n");
				
				moveTheFolderByParentId(1l);
				
//				Log.i("browser", "----------------post------------------");
//				for(FolderInfo info : folderList) {
//					Log.i("browser", info.id + "     " + info.parent + "     " + info.title + "      " + info.createTime);
//				}
				
				if(folderIdsList != null && folderIdsList.size() != 0) {
					ArrayList<FolderInfo> noNeedShowFolderList = getNoNeedShowFolder();
					folderList.removeAll(noNeedShowFolderList);
				}
				
				setEachSpaceIconShow();
				
				sortingFolderList = false;
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	private void setEachSpaceIconShow() {
		int spaceCount = 0;
		for(FolderInfo info : folderList) {
			if(info.level > spaceCount) {
				spaceCount = info.level;
			}
		}
		
		if(rootFolderLeftMargin + eachSpaceDefault * spaceCount < maxSpaceIconShow) {
			adapter.setDefaultSpaceDp(eachSpaceDefault);
		}else {
			adapter.setDefaultSpaceDp(maxSpaceIconShow / spaceCount);
		}
		
//		Log.i("browser", "maxspaceCount:" + spaceCount);
	}
	
	/**
	 * 根据父文件夹的id进行排序，此方法是进行一轮的排序。比如先将父类id为1的子文件夹移动到合适的位置
	 * @param parentId
	 */
	private void moveTheFolderByParentId(Long parentId) {
		int parentPos = 0;
		int parentLevel = 0;
		for(int i=0; i<folderList.size();i++) {
			if(folderList.get(i).id == parentId) {
				parentLevel = folderList.get(i).level;
				parentPos = i;
				break;
			}
		}
		
		ArrayList<FolderInfo> childPosition = new ArrayList<FolderInfo>();
		for(int i=0; i<folderList.size();i++) {
			FolderInfo info = folderList.get(i);
			if(info.parent == parentId) {
				if(childPosition.size() == 0) {
					childPosition.add(folderList.get(i));
				}else {
					int aimPos = -1;
					for(int j=0; j<childPosition.size(); j++) {
						if(info.createTime > childPosition.get(j).createTime) {
							aimPos = j;
							break;
						}
					}
					if(aimPos == -1) {
						childPosition.add(folderList.get(i));
					}else {
						childPosition.add(aimPos, folderList.get(i));
					}
				}
			}
		}
		
		int movedCount = 0;
		for(FolderInfo info:childPosition) {
			folderList.remove(info);
			info.level = parentLevel + 1;
			folderList.add(parentPos + 1 + movedCount, info);
			movedCount++;
			movedParentIds.add(info.id);
		}
		
		boolean moveContinue = false;
		long nextMoveId = -1;
		for(Long lMoved:movedParentIds) {
			if(parentIdList.contains(lMoved)) {
				moveContinue = true;
				nextMoveId = lMoved;
				parentIdList.remove(lMoved);
				break;
			}
		}
		if(moveContinue && nextMoveId != -1) {
			moveTheFolderByParentId(nextMoveId);
		}
	}

	private void addParentIds() {
		for(FolderInfo info : folderList) {
			if(info.parent != 0 && !parentIdList.contains(info.parent)) {
				parentIdList.add(info.parent);
			}
		}
//		for(Long l : parentIdList) {
//			Log.i("browser", l + "");
//		}
//		Log.i("browser", "\n");
	}

	/**
	 * 移动文件夹的时候，取消显示已经选中需要移动的文件夹
	 * @return
	 */
	private ArrayList<FolderInfo> getNoNeedShowFolder() {
		ArrayList<FolderInfo> noNeedShowFolder = new ArrayList<FolderInfo>();
		int findLevel = -1;
		for(FolderInfo info : folderList) {
			if(noNeedShow(info.id)) {
				noNeedShowFolder.add(info);
				findLevel = info.level;
				continue;
			}
			if(findLevel != -1) {
				if(info.level > findLevel) {
					noNeedShowFolder.add(info);
				}else if(info.level == findLevel) {
					findLevel = -1;
				}
			}
		}
		return noNeedShowFolder;
	}

	private boolean noNeedShow(long id) {
		boolean noNeedShow = false;
		for(Integer integer : folderIdsList) {
			if(integer == id) {
				noNeedShow = true;
				break;
			}
		}
		
		return noNeedShow;
	}

	public class FolderInfo {
		long id;
		String title;
		long parent;
		long createTime;
		int level;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
}
