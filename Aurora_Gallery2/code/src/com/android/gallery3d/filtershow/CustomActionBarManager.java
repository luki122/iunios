package com.android.gallery3d.filtershow;

import com.android.gallery3d.app.Log;
import com.android.gallery3d.util.MyLog;

import com.android.gallery3d.R;

import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CustomActionBarManager implements OnClickListener{
	
	public interface Listener {
		public void userActionCancel(ActionBarType currentActionBarType);
		public void userActionCancelCrop(ActionBarType currentActionBarType);
		public void userActionConfirmCrop(ActionBarType currentActionBarType);
		public void userActionSave(ActionBarType currentActionBarType);
		
		public void userActionCancelFilter(ActionBarType currentActionBarType);
		public void userActionConfirmFilter(ActionBarType currentActionBarType);
	}
	
	public enum ActionBarType {
		ABT_SAVE,//取消, 存储
		ABT_CONFIRM_CROP,//取消, 确认裁剪
		ABT_CONFIRM_FILTER//取消,确认滤镜
	};
	
	private View mRootView;
	private Button mLeftButton;
	private Button mRightButton;
	private ActionBarType mType;
	
	//private static final String AURORA_DAY_FONT_PATH = "system/fonts/DroidSansFallback.ttf";
	//private Typeface mAuroraChinese;
	private Listener mListener;
	
	public CustomActionBarManager(Listener listener) {
		mListener = listener;
		//mAuroraChinese = Typeface.createFromFile(AURORA_DAY_FONT_PATH);
	}
	
	public void setView(View v) {
		if(v == null) return;
		mRootView = v;
		mLeftButton = (Button)v.findViewById(R.id.action_bar_left_button);
		mRightButton = (Button)v.findViewById(R.id.action_bar_right_right);
		//mLeftButton.setTypeface(mAuroraChinese);
		//mRightButton.setTypeface(mAuroraChinese);
		mLeftButton.setOnClickListener(this);
		mRightButton.setOnClickListener(this);
	}
	
	public void setType(ActionBarType type) {
		mType = type;
		if(ActionBarType.ABT_SAVE == type) {
			mRightButton.setVisibility(View.VISIBLE);
			mLeftButton.setText(R.string.photo_edit_action_bar_cancel);
			mRightButton.setText(R.string.photo_edit_action_bar_save);
		} else if(ActionBarType.ABT_CONFIRM_CROP == type) {
			mRightButton.setVisibility(View.VISIBLE);
			mLeftButton.setText(R.string.photo_edit_action_bar_cancel);
			mRightButton.setText(R.string.photo_edit_action_bar_confirm_crop);
		} else if(ActionBarType.ABT_CONFIRM_FILTER == type) {
			mRightButton.setVisibility(View.VISIBLE);
			mLeftButton.setText(R.string.photo_edit_action_bar_cancel);
			mRightButton.setText(R.string.photo_edit_action_bar_confirm_filter);
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int id = view.getId();
		if(R.id.action_bar_left_button == id) {
			handleLeftButtonClick();
		} else if(R.id.action_bar_right_right == id) {
			handleRightButtonClick();
		}
	}
	
	private void handleLeftButtonClick() {
		switch(mType) {
		case ABT_SAVE:
			mListener.userActionCancel(mType);
			break;
		case ABT_CONFIRM_CROP:
			mListener.userActionCancelCrop(mType);
			break;
		case ABT_CONFIRM_FILTER:
			mListener.userActionCancelFilter(mType);
			break;
		default:
			mListener.userActionCancel(mType);
			break;
		}
	}
	
	private void handleRightButtonClick() {
		switch(mType) {
		case ABT_SAVE:
			mListener.userActionSave(mType);
			break;
		case ABT_CONFIRM_CROP:
			mListener.userActionConfirmCrop(mType);
			break;
		case ABT_CONFIRM_FILTER:
			mListener.userActionConfirmFilter(mType);
			break;
		default:
			break;
		}
	}
}
