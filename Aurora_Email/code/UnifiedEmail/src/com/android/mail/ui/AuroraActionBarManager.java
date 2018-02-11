package com.android.mail.ui;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnActionbarSearchViewQuitListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraSearchView;
import aurora.widget.AuroraSearchView.OnCloseButtonClickListener;
import aurora.widget.AuroraSearchView.OnCloseListener;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import aurora.widget.AuroraMenuBase;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

import com.android.email.provider.AuroraAutoCompleteDBHelper;
import com.android.mail.R;
import com.android.mail.browse.ConversationItemView;
import com.android.mail.compose.ComposeActivity;
import com.aurora.email.AuroraComposeActivity;
import com.aurora.internal.R.color;
import com.android.mail.ui.ConversationSearchView.ListController;
import com.android.mail.ui.MailActivity;
import com.android.mail.ui.SwipeableListView;
import com.android.mail.utils.MyLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import com.android.mail.providers.Account;
import com.android.mail.providers.Conversation;
import com.android.mail.providers.Folder;
import com.android.mail.providers.FolderObserver;
import com.android.mail.ui.AuroraSelectMoveToFolderActivity;
import com.android.mail.utils.Utils;

import android.widget.PopupWindow.OnDismissListener;
import aurora.widget.AuroraMenu;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;
import android.content.DialogInterface;
import android.app.Notification;
//shihao add for conversation_detail_more 20150205
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.content.ContentValues;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.content.ContentUris;

import com.android.email.provider.AuroraAutoCompleteDBHelper;

public class AuroraActionBarManager {

	public enum Mode {
		INITIAL_MODE,
		DASH_BOARD_MODE,
		DETAIL_MODE,
		//BACK_TO_INIT_MODE
	};
	
	public interface ModeChangeListener {
		public void auroraActionBarModeChanged(Mode changedTo);
	}
	
	private static final int AURORA_SEARCH_ACTION = 1;
	private static final int AUROR_ACTION_BAR_ITEM_DETAIL_MOVE_TO = 2;//paul add
	
	private AuroraSelectionManager mSelectionManager;

	//private static AuroraActionBarManager mInstance; 
	private MailActivity mActivity;
	//private AuroraActionBar mAuroraActionBar;
	private Mode mCurrentMode = Mode.INITIAL_MODE;
	private OnClickListener mOnClickListener;	
	
	private OnClickListener mCustomViewOnClickListener;
	private View mCustomView;
	private ImageButton mDrawerToggleButton; 
	private ImageView mNewConversationDotImageView;
	private TextView mBoxTypeTextView;
	private TextView mBoxUnreadCountTextView;
	private TextView mEmailAccountTextView;
	
	private boolean mInitialModeInited = false;
	private boolean mDashBoardModeInited = false;
	private boolean mDetailModeInited = false;
	
	
	private static final int BOTTOM_MENU_COUNT = 3;
	
	private ArrayList<ModeChangeListener> mModeChangeListeners = new ArrayList<ModeChangeListener>(); 
	
	public AuroraActionBarManager(MailActivity activity) {
		this.mActivity = activity;
		this.mSelectionManager = new AuroraSelectionManager(this);
                if(mActivity.getAuroraActionBar() != null) {
                    //mActivity.getAuroraActionBar().setOnAuroraActionBarListener(mAuroActionBarItemClickListener);
                }
	}
	
	
	
	public void registerModeChangeListener(ModeChangeListener listener) {
		mModeChangeListeners.add(listener);
	}
	
	public void notifyModeChange() {
		for(ModeChangeListener listener : mModeChangeListeners) {
			listener.auroraActionBarModeChanged(mCurrentMode);
		}
	}
	
	public void initViews() {
		mCustomView = (View)mActivity.findViewById(R.id.aurora_actionbar_custom_view);
		mDrawerToggleButton = (ImageButton)mCustomView.findViewById(R.id.drawer_toggle_button);
		mNewConversationDotImageView = (ImageView)mCustomView.findViewById(R.id.new_conversation_dot);
		mBoxTypeTextView = (TextView)mCustomView.findViewById(R.id.box_type);
		mBoxUnreadCountTextView = (TextView)mCustomView.findViewById(R.id.box_unread_count);
		mEmailAccountTextView = (TextView)mCustomView.findViewById(R.id.email_account);
	}
	
	public MailActivity getActivity() {
		return mActivity;
	}
	
	public Mode getCurrentMode() {
		return mCurrentMode;
	}
	
	public void setOnClickListener(OnClickListener listener) {
		mCustomViewOnClickListener = listener;
		setOnCustomViewClickListener();
	}
	
	private void setOnCustomViewClickListener() {
		if(mCustomViewOnClickListener == null) return;
    	mDrawerToggleButton.setOnClickListener(mCustomViewOnClickListener);
    	//mCustomView.setOnClickListener(mCustomViewOnClickListener);
	}
	
	private void setInfos(Folder folder) {
		if(folder == null) return;
			//folder name
		if(mBoxTypeTextView != null) {
			mBoxTypeTextView.setText(folder.name);
		}
		//unread count
		final int folderUnreadCount = folder.isUnreadCountHidden() ? 0 : folder.unreadCount;
		//MyLog.i2("SQF_LOG", "AuroraActionBarManager::setInfos ----> folder.name:" + folder.name + " folderUnreadCount:" + folderUnreadCount + " (mBoxTypeTextView != null):" + (mBoxTypeTextView != null));
		if(mBoxUnreadCountTextView != null) {
			if(folderUnreadCount != 0) {
				mBoxUnreadCountTextView.setVisibility(View.VISIBLE);
				String unreadCountStr = String.valueOf(folderUnreadCount);
				mBoxUnreadCountTextView.setText(unreadCountStr);
			} else {
				mBoxUnreadCountTextView.setVisibility(View.GONE);
			}
		}
		//account name
		Account account = mActivity.getAccountController().getAccount();
		if(account != null && mEmailAccountTextView != null) mEmailAccountTextView.setText(account.name);
	}
	
	public void updateActionBarInfos(Folder newFolder) {
		if(mActivity == null) return;
		//if(mCurrentMode == Mode.DETAIL_MODE) return;
		setInfos(newFolder);
	}
	
	public void updateActionBarInfos() {
		if(mActivity == null) return;
		//if(mCurrentMode == Mode.DETAIL_MODE) return;
		Folder folder = mActivity.getFolderController().getFolder();
		setInfos(folder);
	}
	
	private boolean mNeedSetCustomView = true;
	public void setMode(Mode mode) {
		AuroraActionBar actionBar = mActivity.getAuroraActionBar();
		switch(mode) {
		case INITIAL_MODE:
			Log.i("SQF_LOG", "setMode...INITIAL_MODE");
			if(mCurrentMode == Mode.DASH_BOARD_MODE ) {
				Log.i("SQF_LOG", "setMode...INITIAL_MODE 0000000");
				mActivity.setListViewNeedsAnimation(true);
				//call showActionBarDashBoard to exit dash board mode
				mNeedSetCustomView = false;
				actionBar.setShowBottomBarMenu(false);
				actionBar.showActionBarDashBoard();
				actionBar.showActionBottomeBarMenu();
				ConversationItemView.mStarInvisiable = true;
				notifyDontNeedEditingAnimation();
			} else if(mCurrentMode == Mode.DETAIL_MODE){
				Log.i("SQF_LOG", "setMode...INITIAL_MODE 1111111");
				mNeedSetCustomView = true;
				actionBar.setShowBottomBarMenu(false);
				actionBar.showActionBottomeBarMenu();
			}
			
/*			if (!(mActivity.getViewMode().getMode() == ViewMode.SEARCH_RESULTS_CONVERSATION
					|| mActivity.getViewMode().getMode() == ViewMode.SEARCH_RESULTS_LIST) 
					) {
				//mActivity.hideSearchviewLayout();
					//actionBar.showAuroraActionbarSearchView();
				}else {
					//mActivity.showSearchviewLayout();
					//actionBar.showAuroraActionbarSearchView();
					
				}*/
			
			if(mNeedSetCustomView) {
				Log.i("cjslog", "setMode...INITIAL_MODE 2222222");
				actionBar.changeAuroraActionbarType(AuroraActionBar.Type.Custom);
				actionBar.setCustomView(R.layout.aurora_actionbar_custom_view);
				mNeedSetCustomView = false;
				initViews();
				
				AuroraActionBarItem item = actionBar.addItem(com.aurora.R.drawable.aurora_action_bar_search_svg, AURORA_SEARCH_ACTION, "111");
				actionBar.getHomeTextView().setVisibility(View.GONE);
				actionBar.setOnAuroraActionBarListener(mAuroActionBarItemClickListener);
			} 
			
			
			//Log.i("SQF_LOG", "setMode...INITIAL_MODE 3333333333");
			mActivity.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack);
			mActivity.setIsNeedShowMenuWhenKeyMenuClick(true);
			mActivity.setAuroraMenuItems(R.menu.aurora_actionbar_menu_setting);
			//actionBar.setOnAuroraActionBarListener(mAuroActionBarItemClickListener);
			
			updateActionBarInfos();

			View homeBackButton = actionBar.getHomeButton();
			homeBackButton.setVisibility(View.GONE);
			if (mActivity.getConversationListFragment() != null) {
				mActivity.getConversationListFragment().getFloatingActionButton().setVisibility(View.VISIBLE);
			}
			setOnCustomViewClickListener();
			break;
		case DASH_BOARD_MODE:
			Log.i("SQF_LOG", "setMode...DASH_BOARD_MODE");
			actionBar.setShowBottomBarMenu(true);
			mActivity.setIsNeedShowMenuWhenKeyMenuClick(false);//cancel setting menu.
			mActivity.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack);
			actionBar.initActionBottomBarMenu(R.menu.aurora_actionbar_menu, BOTTOM_MENU_COUNT);
			mActivity.setAuroraSystemMenuCallBack(mAuroraMenuItemClickListener);
			actionBar.getSelectLeftButton().setOnClickListener(mDashBoardButtonsOnClickListener);
			actionBar.getSelectRightButton().setOnClickListener(mDashBoardButtonsOnClickListener);
			actionBar.showActionBarDashBoard();
			actionBar.showActionBottomeBarMenu();
			mActivity.getConversationListFragment().getFloatingActionButton().setVisibility(View.INVISIBLE);
			notifyDontNeedEditingAnimation();
			break;
		//paul add start
		case DETAIL_MODE:
			Log.i("SQF_LOG", "setMode...DETAIL_MODE");
			if(mCurrentMode != Mode.DETAIL_MODE) {
				//mActivity.showSearchviewLayout();
				//mActivity.hideSearchviewLayout();
				mActivity.setIsNeedShowMenuWhenKeyMenuClick(false);
				actionBar.changeAuroraActionbarType(AuroraActionBar.Type.Normal);
				actionBar.setTitle(R.string.message_details_title);
				//Modify by shihao for coversation_detail_more 20150205 begin
//				actionBar.addItem(R.drawable.aurora_ic_detail_move_to, AUROR_ACTION_BAR_ITEM_DETAIL_MOVE_TO, null);
				actionBar.addItem(AuroraActionBarItem.Type.More, AUROR_ACTION_BAR_ITEM_DETAIL_MOVE_TO);
				mActivity.setAuroraSystemMenuCallBack(mAuroraMenuItemClickListener);
				//end
				if(null != mDetailCallBack) mActivity.setAuroraBottomBarMenuCallBack(mDetailCallBack);
				actionBar.initActionBottomBarMenu(R.menu.aurora_detail_menu, 4);
				actionBar.setShowBottomBarMenu(true);
				actionBar.showActionBottomeBarMenu();
				
			}
			break;
		//paul add end	
		default:
			Log.i("SQF_LOG", "incorrect ");
			break;
		}
		mCurrentMode = mode;
		notifyModeChange();
	}
	
	public boolean isInSelectionMode() {
	    if(getCurrentMode() == AuroraActionBarManager.Mode.DASH_BOARD_MODE) {
	    	return true;
	    }
	    return false;
	}
	
	public void checkAllConversationSelected() {
		if(mSelectionManager.areAllConversationSelected()) {
			mSelectionManager.setCurrentMode(AuroraSelectionManager.REVERSE_MODE);
		} else {
			mSelectionManager.setCurrentMode(AuroraSelectionManager.SELECT_ALL_MODE);
		}
	}
	
	public void onBackPressedInSelectionMode() {
		AuroraActionBar actionBar = mActivity.getAuroraActionBar();
		TextView rightButton = (TextView)actionBar.getSelectRightButton();
    	mSelectionManager.cancelAllSelect();
    	changeTextToSelectAll();
	}
	
	public void changeTextToSelectAll() {
		AuroraActionBar actionBar = mActivity.getAuroraActionBar();
		TextView rightButton = (TextView)actionBar.getSelectRightButton();
		rightButton.setText(R.string.aurora_select_all);
	}
	
	public void changeTextToReverseSelect() {
		AuroraActionBar actionBar = mActivity.getAuroraActionBar();
		TextView rightButton = (TextView)actionBar.getSelectRightButton();
		rightButton.setText(R.string.aurora_reverse_select);
	}
	
	private OnClickListener mDashBoardButtonsOnClickListener = new OnClickListener() {
		@Override
	    public void onClick(View view) {
			AuroraActionBar actionBar = mActivity.getAuroraActionBar();
			TextView rightButton = (TextView)actionBar.getSelectRightButton();
	        if(view == actionBar.getSelectLeftButton()) {
	        	mActivity.onBackPressed();
	        	onBackPressedInSelectionMode();
	        } else if(view == actionBar.getSelectRightButton()){
	        	mSelectionManager.selectAllOrReverse();
	        }
	    }
	};
	
	private OnAuroraMenuItemClickListener mAuroraMenuItemClickListener = new OnAuroraMenuItemClickListener() {
		
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch(itemId){
			case R.id.aurora_menu_set_as_read:{
				
				Log.i("SQF_LOG", "aurora_menu_set_as_read ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsRead(true);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
			}
				
			case R.id.aurora_menu_set_as_unread:{
				
				Log.i("SQF_LOG", "aurora_menu_set_as_unread ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsRead(false);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
			}
			case R.id.aurora_menu_set_as_starred: {
				Log.i("SQF_LOG", "aurora_menu_set_as_starred ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsStarred(true);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
			}
			case R.id.aurora_menu_set_as_nonstarred: {
				Log.i("SQF_LOG", "aurora_menu_set_as_nonstarred ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsStarred(false);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
				}
			
			case R.id.aurora_details_move_to:
				Intent intent = new Intent(mActivity, AuroraSelectMoveToFolderActivity.class);
				Account account = mActivity.getAccountController().getAccount();
				Folder currentFolder = mActivity.getFolderController().getFolder();
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_ACCOUNT, account);
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_FOLDER, currentFolder);
				mActivity.startActivityForResult(intent, AbstractActivityController.SELECT_MESSAGE_REQUEST_CODE);
				break;
				
			case R.id.aurora_backup_to_calendar:
				Account account1 = mActivity.getAccountController().getAccount();
				Folder currentFolder1 = mActivity.getFolderController().getFolder();
				Conversation conversation = mActivity.getListHandler().getCurrentConversation();
				
				AuroraAutoCompleteDBHelper dbHelper = AuroraAutoCompleteDBHelper.getInstance(mActivity);
				dbHelper.createBackupTable();
				dbHelper.insertData(account1.serialize(),currentFolder1.folderUri.fullUri.toString(),conversation);
				Intent newIntent = generateCreateEventIntent(System.currentTimeMillis(),conversation.subject);
				mActivity.startActivity(newIntent);
				break;
			}
		}
	};
	
	private boolean  quitSearch(ViewMode mViewMode) {
		//Log.d("cjslog", "quitsearch " + mViewMode.getMode());
		mActivity.getSearchView().clearText();
		if(mViewMode != null){
			if(mViewMode.getMode() == ViewMode.SEARCH_RESULTS_LIST){
				//Log.d("cjslog", "SEARCH_RESULTS_LIST");
				mViewMode.enterConversationListMode();
				//mActivity.hideSearchviewLayout();
				
			}else if (mViewMode.getMode() == ViewMode.CONVERSATION_LIST) {
				//setMode(Mode.INITIAL_MODE);
				mViewMode.enterConversationListMode();
				//mActivity.removeAuroraMenuItemById(AURORA_SEARCH_ACTION);
				mActivity.getAuroraActionBar().removeAllViews();
				mNeedSetCustomView = true;
				setMode(Mode.INITIAL_MODE);
				mActivity.getAuroraActionBar().requestLayout();
				//mActivity.getAuroraActionBar().setOnAuroraActionBarListener(mAuroActionBarItemClickListener);
			}
	
		}
		
		if(null != mListController) mListController.refreshList();//SQF ADDED ON 2014.12.30
	    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.CONVERSATION_LIST end
		return true;    
		// TODO Auto-generated method stub
	}
	
	private OnAuroraActionBarItemClickListener mAuroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case AURORA_SEARCH_ACTION:
				//Log.d("cjslog", "AURORA_SEARCH_ACTION");
				
				final AuroraSearchView mSearchView = mActivity.getAuroraActionBar().getAuroraActionbarSearchView();

				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						InputMethodManager imm = (InputMethodManager) mSearchView.getContext()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}, 100);
			    //Aurora <SQF> <2014-12-01>  for NEW_UI begin
				if(((MailActivity)mActivity).getAuroraActionBarManager().isInSelectionMode() ) {
					//manager.setMode(AuroraActionBarManager.Mode.INITIAL_MODE);
					return;
				}
				//Aurora <SQF> <2014-12-01>  for NEW_UI end
				final ViewMode mViewMode = mActivity.getViewMode();
				Account mAccount = mActivity.getAccountController().getAccount();
			    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.SEARCH_RESULTS_LIST begin
				if(mViewMode != null)
					mViewMode.enterSearchResultsListMode();
			    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.SEARCH_RESULTS_LIST end
				if (mAccount == null) {
					// We cannot search if there is no account. Drop the request to the floor.
					return;
				}
				
				//mActivity.getAuroraActionBar();
				//mActivity.getAuroraActionBar().showAuroraActionbarSearchView();
				//Log.d("cjslog", "fukc");
				mActivity.showSearchviewLayout();
				
				
				mActivity.setOnSearchViewQuitListener(new OnSearchViewQuitListener() {
					
					@Override
					public boolean quit() {
						// TODO Auto-generated method stub
						return quitSearch(mViewMode);
					}
				});
				
				if(mSearchView == null){
					return;
				}
				mSearchView.setMaxLength(30);
				mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		/*		mSearchView = ((AuroraActivity) mActivity).getSearchView(); 
				if (null == mSearchView) {
					return;
				}	*/
				
				//mSearchView.setMaxLength(30);
				//mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
				((AuroraActivity) mActivity ).setOnQueryTextListener(new searchListener());
			
				break;
			case AUROR_ACTION_BAR_ITEM_DETAIL_MOVE_TO:
/*				Intent intent = new Intent(mActivity, AuroraSelectMoveToFolderActivity.class);
				Account account = mActivity.getAccountController().getAccount();
				Folder currentFolder = mActivity.getFolderController().getFolder();
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_ACCOUNT, account);
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_FOLDER, currentFolder);
				mActivity.startActivityForResult(intent, AbstractActivityController.SELECT_MESSAGE_REQUEST_CODE);*/
				//Aurora <shihao> <2015-03-15> for new update begin
				//mActivity.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack);
				mActivity.setAuroraMenuItems(R.menu.aurora_conversation_detail_more);
				mActivity.getAuroraMenu().setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						//mActivity.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack);
						//mActivity.setAuroraMenuItems(R.menu.aurora_actionbar_menu_setting);
						mActivity.removeCoverView();
					}
				});
				mActivity.showAuroraMenu(mActivity.getContentView(), Gravity.TOP | Gravity.RIGHT, 0, 0);
				//mActivity.showAuroraMenu();
				//Aurora <shihao> <2015-03-15> for new update end
				break;
			default:
				break;
			}
		}
	};

	public void enableActionBarBottomMenu(boolean enable) {
		
		//Log.i("SQF_LOG", "enableActionBarBottomMenu enable--------------enable:" + enable + " BOTTOM_MENU_COUNT:"+ BOTTOM_MENU_COUNT);
		AuroraMenu bottomMenu = mActivity.getAuroraActionBar().getAuroraActionBottomBarMenu();
		if(bottomMenu == null) {
			//Log.i("SQF_LOG", "enableActionBarBottomMenu enable--------------return:");
			return;
		}
		for(int i = 0; i<=BOTTOM_MENU_COUNT -1 ; i++) {
			Log.i("SQF_LOG", "enableActionBarBottomMenu enable-------------- " + i);
			bottomMenu.setBottomMenuItemEnable(i, enable);
		}
	}
	
	

	private AuroraMenuBase.OnAuroraMenuItemClickListener mAuroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			switch (itemId) {
			case R.id.aurora_actionbar_menu_delete: {

				int messageId = mActivity.getSelectedSet().size() > 1 ? R.string.aurora_list_delete_item_tip_messages : R.string.aurora_list_delete_item_tip_message;
				AuroraAlertDialog builder = new AuroraAlertDialog.Builder(mActivity)
		        //.setTitle(R.string.aurora_list_delete_item_tip_title)
				.setTitle(messageId).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						// TODO Auto-generated method stub
						SwipeableListView listView = mSelectionManager.getSwipeableListView();
						if(listView == null) return;
						listView.deleteSelectedConversations();
						setMode(Mode.INITIAL_MODE);
				        
						dialog.dismiss();
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();
		    	builder.show();

				break;
			}
			case R.id.aurora_actionbar_menu_mark_as: {
				Log.i("SQF_LOG", "aurora_actionbar_menu_mark_as ");
				mActivity.setAuroraMenuItems(R.menu.aurora_menu_mark_as);
				mActivity.getAuroraMenu().setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						mActivity.setAuroraBottomBarMenuCallBack(mAuroraMenuCallBack);
						mActivity.setAuroraMenuItems(R.menu.aurora_actionbar_menu_setting);
						mActivity.removeCoverView();
					}
				});
				//mActivity.showAuroraMenu();
				mActivity.getAuroraMenu().setAnimationStyle(com.aurora.R.style.AuroraMenuLeftBottomAnimation);
				mActivity.showAuroraMenu(mActivity.getContentView(), Gravity.LEFT | Gravity.BOTTOM, 0, 0);
				break;
			}
			case R.id.aurora_actionbar_menu_move_to: {
				Log.i("SQF_LOG", "aurora_actionbar_menu_move_to ");
				Intent intent = new Intent(mActivity, AuroraSelectMoveToFolderActivity.class);
				Account account = mActivity.getAccountController().getAccount();
				Folder currentFolder = mActivity.getFolderController().getFolder();
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_ACCOUNT, account);
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_FOLDER, currentFolder);
				mActivity.startActivityForResult(intent, AbstractActivityController.SELECT_FOLDER_REQUEST_CODE);
				break;
			}
			/*
			case R.id.aurora_actionbar_menu_archive: {
				Log.i("SQF_LOG", "aurora_actionbar_menu_archive ");
				AbstractActivityController controller = (AbstractActivityController)mActivity.getConversationUpdater();
			    mActivity.getConversationUpdater().delete(R.id.archive, mActivity.getSelectedSet().values(), controller.getDeferredBatchAction(R.id.archive), true);
				//mSelectionManager.cancelAllSelect();
				//listView.refreshList();
				//setMode(Mode.INITIAL_MODE);
				break;
			}
			*/
/*			case R.id.aurora_menu_set_as_read: {
				Log.d("cjslog" , "aurora_menu_set_as_read =============");
				Log.i("SQF_LOG", "aurora_menu_set_as_read ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsRead(true);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
			}*/
/*			case R.id.aurora_menu_set_as_unread: {
				Log.d("cjslog" , "aurora_menu_set_as_unread =============");
				Log.i("SQF_LOG", "aurora_menu_set_as_unread ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsRead(false);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
			}*/
/*			case R.id.aurora_menu_set_as_starred: {
				Log.d("cjslog" , "aurora_menu_set_as_starred =============");
				Log.i("SQF_LOG", "aurora_menu_set_as_starred ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsStarred(true);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
			}*/
/*			case R.id.aurora_menu_set_as_nonstarred: {
				Log.d("cjslog" , "aurora_menu_set_as_nonstarred =============");
				Log.i("SQF_LOG", "aurora_menu_set_as_nonstarred ============= ");
				SwipeableListView listView = mSelectionManager.getSwipeableListView();
				if(listView == null) return;
				listView.toggleAsStarred(false);
				mSelectionManager.cancelAllSelect();
				listView.refreshList();
				setMode(Mode.INITIAL_MODE);
				break;
			}*/
			case R.id.aurora_actionbar_menu_setting: {
				Log.i("SQF_LOG", "aurora_actionbar_menu_setting ============= ");
				Account account = mActivity.getAccountController().getAccount();
				Utils.showSettings(mActivity.getActivityContext(), account);
				break;
			}
			
			//Aurora <shihao> <20150205> for Conversation Details More begin
/*			case R.id.aurora_details_move_to:
				Intent intent = new Intent(mActivity, AuroraSelectMoveToFolderActivity.class);
				Account account = mActivity.getAccountController().getAccount();
				Folder currentFolder = mActivity.getFolderController().getFolder();
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_ACCOUNT, account);
				intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_FOLDER, currentFolder);
				mActivity.startActivityForResult(intent, AbstractActivityController.SELECT_MESSAGE_REQUEST_CODE);
				break;*/
//			case R.id.aurora_new_email_notification:
//				if(mPagerController != null)
//					mPagerController.pageAction(itemId);
//				break;
/*			case R.id.aurora_backup_to_calendar:
				Account account1 = mActivity.getAccountController().getAccount();
				Folder currentFolder1 = mActivity.getFolderController().getFolder();
				Conversation conversation = mActivity.getListHandler().getCurrentConversation();
				
				AuroraAutoCompleteDBHelper dbHelper = AuroraAutoCompleteDBHelper.getInstance(mActivity);
				dbHelper.createBackupTable();
				dbHelper.insertData(account1.serialize(),currentFolder1.folderUri.fullUri.toString(),conversation);

				Intent newIntent = generateCreateEventIntent(System.currentTimeMillis(),conversation.subject);
				mActivity.startActivity(newIntent);
				break;*/
			//Aurora <shihao> <20150205> for Conversation Details More end
			default:
				break;
			}
		}
	};
	
	//must use handler sendmessage to notify AuroraActionBar
	public static final int NOTIFY_DONT_NEED_EDITING_ANIMATION = 1;
	public static final int NOTIFY_TO_SET_DETAIL_MODE = 2;//paul add
	private Handler mHandler = new Handler() {
		@Override  
        public void handleMessage(Message msg) {  
        	switch(msg.what){
	            case NOTIFY_DONT_NEED_EDITING_ANIMATION: {
	            	Log.i("SQF_LOG", "");
	            	mActivity.setListViewNeedsAnimation(false);
	            }
				break;
				//paul add
				case NOTIFY_TO_SET_DETAIL_MODE: {
					if(mActivity.hasWindowFocus()){
						setMode(Mode.DETAIL_MODE);
					}else{
						trytoSetDetailMode();
					}
				}
				break;
        	}
        }
	};
	//paul add start
	public void trytoSetDetailMode() {
        if (!mHandler.hasMessages(NOTIFY_TO_SET_DETAIL_MODE)) {
        	//Log.d("cjslog", "NOTIFY_TO_SET_DETAIL_MODE");
            mHandler.sendEmptyMessage(NOTIFY_TO_SET_DETAIL_MODE);
        }
	}
	//paul add end
	public void notifyDontNeedEditingAnimation() {
		Message msg = mHandler.obtainMessage(NOTIFY_DONT_NEED_EDITING_ANIMATION);
		//mHandler.sendMessage(msg);
		mHandler.sendMessageDelayed(msg, 10);
	}


    /**
	 * 判断长按编辑动画是否播放完成
	 * @return
	 */
	public boolean actionBarIsAnimating() {
		AuroraActionBar auroraActionBar = mActivity.getAuroraActionBar();
		if (auroraActionBar == null) {// 快速点击图片和手机返回键
			return true;
		}
		if (auroraActionBar.auroraIsEntryEditModeAnimRunning()
				|| auroraActionBar.auroraIsExitEditModeAnimRunning()) {
			return true;
		}
		return false;
	}
	
	//paul add start
	private AuroraMenuBase.OnAuroraMenuItemClickListener mDetailCallBack = null;
	public void setDetailCallBack(AuroraMenuBase.OnAuroraMenuItemClickListener cb){
		mDetailCallBack = cb;
	}
	
	public boolean onBackPressed() {
		//Log.d("cjslog", "onBackPressed");
		int viewmode = mActivity.getViewMode().getMode();
		if (viewmode == ViewMode.SEARCH_RESULTS_CONVERSATION
				|| viewmode == ViewMode.SEARCH_RESULTS_LIST) {
			//Log.d("cjslog", "22221221312312312");
			mActivity.hideSearchviewLayout();
			setMode(Mode.INITIAL_MODE);
			mActivity.getConversationListFragment().onConversationListStatusUpdated();
			return true;
		}
    	if(getCurrentMode() == AuroraActionBarManager.Mode.DASH_BOARD_MODE) {
    		ConversationItemView.mStarInvisiable = true;
    		setMode(AuroraActionBarManager.Mode.INITIAL_MODE);
    		onBackPressedInSelectionMode();
    		mActivity.getConversationListFragment().onConversationListStatusUpdated();//SQF ADDED TO "show listview footer"
    		return true;
    	}
		if(getCurrentMode() == AuroraActionBarManager.Mode.DETAIL_MODE) {
			ConversationItemView.mStarInvisiable = true;
			//setMode(Mode.BACK_TO_INIT_MODE);
			

			
			setMode(Mode.INITIAL_MODE);
			if (mActivity.getViewMode().getPrevMode() == ViewMode.SEARCH_RESULTS_LIST) {
				//mActivity.getViewMode().enterConversationListMode();
				mActivity.hideSearchviewLayout();
				
			}

			return false;
		}

		return false;
	}

	//paul add end
	
	//Aurora <shihao> <2015-03-16> for Back up to Calendar begin
	private Intent generateCreateEventIntent(long startMillis, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        ComponentName cn = new ComponentName("com.android.calendar", "com.android.calendar.event.AuroraEditEventActivity");
        intent.setComponent(cn);
        intent.putExtra(EXTRA_EVENT_BEGIN_TIME, startMillis);
        intent.putExtra(Events.CALENDAR_ID, getCalendarId());
        intent.putExtra(Events.TITLE, title);
        intent.putExtra(Events.EVENT_LOCATION, title);
        return intent;
    }
	
	public static final String EMAIL_REMINDER_ACCOUNT_NAME = "Email Reminder";

	private long getCalendarId() {
		ContentResolver mResolver = mActivity.getActivityContext().getContentResolver();
        Cursor cursor = mActivity.getActivityContext().getContentResolver().query(
                Calendars.CONTENT_URI,
                new String[] { Calendars._ID},
                Calendars.ACCOUNT_NAME + "='" + EMAIL_REMINDER_ACCOUNT_NAME + "'",
                null,
                null);

        long calendarId = 0;
        if (cursor != null && cursor.moveToFirst()) {
            calendarId = cursor.getLong(0);
        } else {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, EMAIL_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, EMAIL_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, Color.parseColor("#019C73"));
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, EMAIL_REMINDER_ACCOUNT_NAME);

            Uri uri = mResolver.insert(Calendars.CONTENT_URI, values);
            calendarId = ContentUris.parseId(uri);
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

		return calendarId;
	}
	//Aurora <shihao> <2015-03-16> for Back up to Calendar end



	private ListController mListController;
	public void setListController(ListController mListController) {
		// TODO Auto-generated method stub
		this.mListController = mListController;
	}
	
	private final class searchListener implements aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener {
		@Override
		public boolean onQueryTextChange(String queryString) {
			if(null != mListController) mListController.setFliterString(queryString);
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return true;
		}
}
	
}
