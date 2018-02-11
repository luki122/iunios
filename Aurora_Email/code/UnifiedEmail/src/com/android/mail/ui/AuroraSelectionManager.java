package com.android.mail.ui;

import com.android.mail.browse.ConversationCursor;
import com.android.mail.providers.Conversation;
import com.android.mail.ui.SwipeableListView;

import java.util.ArrayList;
import android.util.Log;


public class AuroraSelectionManager {
	
	//mode is set according to the text shown in AuroraActionBar, 
	//if AuroraActionBar shows "Select All", then the mode is "SELECT_ALL_MODE".
	//Similarly, if AuroraActionBar shows "Reverse Select", then the mode is "REVERSE_MODE".
	public static final int SELECT_ALL_MODE = 0;
	public static final int REVERSE_MODE = 1;
	
	private AuroraActionBarManager mActionBarManager;
	private MailActivity mActivity;
	private int mCurrentMode;

	public void setCurrentMode(int mode) {
		mCurrentMode = mode;
		if(mode == SELECT_ALL_MODE) {
			mActionBarManager.changeTextToSelectAll();
		} else if(mode == REVERSE_MODE){
			mActionBarManager.changeTextToReverseSelect();
		}
	}
	
	public int getCurrentMode() {
		return mCurrentMode;
	}
	
	public AuroraSelectionManager(AuroraActionBarManager actionBarManager) {
		mCurrentMode = SELECT_ALL_MODE;
		mActionBarManager = actionBarManager;
		
		mActivity = mActionBarManager.getActivity();
	}
	
	public ArrayList<Conversation> getAllConversations() {
		SwipeableListView listView = getSwipeableListView();
		if(listView == null) return null;
		AnimatedAdapter adapter = listView.getAnimatedAdapter();
		ArrayList<Conversation> list = new ArrayList<Conversation>();
		for(int i=0; i<adapter.getCount(); i++) {
			Object cursor = (Object)adapter.getItem(i);
			//Log.i("SQF_LOG", "" + cursor.getClass().getName());
			if(cursor instanceof ConversationCursor) {
				final Conversation conv = ((ConversationCursor)cursor).getConversation();
				list.add(conv);
				//Log.i("SQF_LOG", "getAllConversations() add " + cursor.getClass().getName() + " conv:" + conv.toString());
			}
		}
		return list;
	}
	
	public SwipeableListView getSwipeableListView() {
		if(mActivity == null) return null;
		return mActivity.getSwipeableListView();
	}
	
	private void selectAll() {
		Log.i("SQF_LOG", "AuroraSelectionManager::selectAll() ");
		SwipeableListView listView = getSwipeableListView();
		if(listView == null) return;
		listView.selectAll(getAllConversations());
		setCurrentMode(REVERSE_MODE);
	}
	
	private void reverseSelect() {
		//Log.i("SQF_LOG", "AuroraSelectionManager::reverseSelect() ");
		SwipeableListView listView = getSwipeableListView();
		if(listView == null) return;
		listView.toggleSelections(getAllConversations());
		setCurrentMode(SELECT_ALL_MODE);
		listView.setMiddleText(0);
	}
	
	public void cancelAllSelect() {
		//Log.i("SQF_LOG", "AuroraSelectionManager::cancelAllSelect() ");
		SwipeableListView listView = getSwipeableListView();
		if(listView == null) return;
		listView.cancelAllSelect(getAllConversations());
		setCurrentMode(SELECT_ALL_MODE);
	}
	
	public void selectAllOrReverse() {
		if(mCurrentMode == SELECT_ALL_MODE) {
			selectAll();
		} else if(mCurrentMode == REVERSE_MODE) {
			reverseSelect();
		}
	}
	
	
	public boolean areAllConversationSelected() {
		ArrayList<Conversation> allConversations = getAllConversations();
		if(allConversations == null) return false;
		SwipeableListView listView = getSwipeableListView();
		ConversationSelectionSet selectionSet = listView.getSelectionSet();
		if(selectionSet == null) return false;
		if(selectionSet.size() == allConversations.size()) {
			return true;
		}
		return false;
	}
}
