package com.android.browser;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

public class ComboBookmarkHistoryActivity extends AuroraActivity implements CombinedBookmarksCallbacks{
	
	public static final String EXTRA_INITIAL_VIEW = "initial_view";
	public static final String EXTRA_OPEN_ALL = "open_all";
	public static final String EXTRA_OPEN_SNAPSHOT = "snapshot_id";
	public static final String EXTRA_COMBO_ARGS = "combo_args";
	
	private BrowserBookmarksPage browserBookmarksPage;
	private BrowserHistoryPage browserHistoryPage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.comboview, AuroraActionBar.Type.Normal);
		init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		BaseUi.changeStatusBar(this,true);
	}
	
	/**
	 * 初始化操作
	 */
	@SuppressLint("NewApi")
	private void init() {
		Intent intent = getIntent();
		String initialView = "";
		if(intent != null) {
			initialView = intent.getStringExtra(EXTRA_INITIAL_VIEW);
			if(initialView.equals(UI.ComboViews.Bookmarks.name())) {
				browserBookmarksPage = new BrowserBookmarksPage();
				getFragmentManager().beginTransaction().add(R.id.ll_comboview, browserBookmarksPage).commit();
			}else if(initialView.equals(UI.ComboViews.History.name())){
				Bundle extras = getIntent().getExtras();
		        Bundle args = extras.getBundle(EXTRA_COMBO_ARGS);
		        browserHistoryPage = new BrowserHistoryPage();
		        browserHistoryPage.setArguments(args);
				getFragmentManager().beginTransaction().add(R.id.ll_comboview, browserHistoryPage).commit();
			}
		}
		setAuroraMenuCallBack(new BottomBarMenuCallback());
	}
	
	private class BottomBarMenuCallback implements OnAuroraMenuItemClickListener {

		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.bookmarks_bottom_menu_delete:
				if(browserBookmarksPage != null) {
//					browserBookmarksPage.deleteCheckedItem();
					browserBookmarksPage.prepareDisplayDeleteDialog();
				}
				break;

			case R.id.bookmarks_bottom_menu_move:
				if(browserBookmarksPage != null) {
					browserBookmarksPage.prepareToBookmarksFolderActivity();
				}
				
				break;
				
			case R.id.bookmarks_bottom_menu_edit:
				if(browserBookmarksPage != null) {
					browserBookmarksPage.prepareDisplayEditDialog();
				}
				break;
				
			case R.id.historys_bottom_menu_delete:
				if(browserHistoryPage != null) {
					browserHistoryPage.prepareDisplayDeleteDialog(false);
				}
				break;
			}
		}
		
	}

	@Override
	public void openUrl(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        setResult(RESULT_OK, i);
        finish();
	}

	@Override
	public void openInNewTab(String... urls) {
		Intent i = new Intent();
        i.putExtra(EXTRA_OPEN_ALL, urls);
        setResult(RESULT_OK, i);
        finish();
	}

	@Override
	public void openSnapshot(long id) {
		Intent i = new Intent();
        i.putExtra(EXTRA_OPEN_SNAPSHOT, id);
        setResult(RESULT_OK, i);
        finish();
	}

	@Override
	public void close() {
		finish();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case BrowserBookmarksPage.SELECT_NEW_FOLDER_RESULT:
			if (intent != null && browserBookmarksPage != null) {
				browserBookmarksPage.setSelectedNewFolderResult(intent);
			}
			break;
			
		case BrowserBookmarksPage.SELECT_EDIT_BOOKMARK_RESULT:
			if (intent != null && browserBookmarksPage != null) {
				browserBookmarksPage.setSelectedEditBookmarkResult(intent);
			}
			break;

		case BrowserBookmarksPage.SELECT_EDIT_FOLDER_RESULT:
			if (intent != null && browserBookmarksPage != null) {
				browserBookmarksPage.setSelectedEditFolderResult(intent);
			}
			break;
			
		case BrowserBookmarksPage.SELECT_MOVE_FOLDER_BOOKMARK_RESULT: 
			if (intent != null && browserBookmarksPage != null) {
				browserBookmarksPage.moveSelectedFolderBookmarks(intent);
			}
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
			if(browserBookmarksPage != null) {
				if(BrowserBookmarksAdapter.isInSelectionMode) {
					browserBookmarksPage.changeToNormal();
				}else {
					if(browserBookmarksPage.hasParent()) {
						browserBookmarksPage.backToParent();
						return true;
					}
				}
			}else if(browserHistoryPage != null) {
				if(BrowserHistorysAdapter.isInSelectionMode) {
					browserHistoryPage.changeToNormal();
				}
			}
			
		}
		
		return super.onKeyDown(keyCode, event);
	}

}
