package com.android.contacts.widget;

import com.android.contacts.R;
import com.android.contacts.util.MergeContacts.CombineItemData;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import aurora.widget.AuroraButton;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CombineItemView extends LinearLayout {
	public LinearListView mCombineItemContainer;
	public Button mCombineButton;

	
	private int mState = CombineItemData.COMBINE_READY;
	private Object mDate;
	private CombineItemViewClick mClick;
	
	public static interface CombineItemViewClick{
		public void combine(Object date);
	}
	
	public void setDate(Object date){
		mDate = date;
	}
	
	public Object getDate(){
		return mDate;
	}
	
	public void setOnCombineItemClickListener(CombineItemViewClick click){
		mClick = click;
	}
	
	public CombineItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CombineItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setState(int state){
		mState = state;
		switch (mState) {
		case CombineItemData.COMBINE_READY:
			mCombineButton.setText(R.string.gn_manual_combine);
			mCombineButton.setEnabled(true);
			mCombineButton.setBackground(getResources().getDrawable(R.drawable.merge_contact_btn));
			//mCombineButton.setTextColor(getResources().getColor(R.color.aurora_merge_button_text_color));
			break;
		case CombineItemData.COMBINE_FINISH:
			mCombineButton.setText(R.string.gn_manual_combine_finish);
			mCombineButton.setEnabled(false);
			mCombineButton.setBackground(null);
			//mCombineButton.setTextColor(getResources().getColor(R.color.merge_contacts_finish));
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCombineItemContainer = (LinearListView) findViewById(R.id.combine_item_container);
		mCombineButton = (Button) findViewById(R.id.combine_action);
		mCombineButton.setOnClickListener(myClickListener);
	}
	
	private OnClickListener myClickListener = new OnClickListener() {
		public void onClick(View view) {
			if(mClick != null){
				switch(view.getId()){
				case R.id.combine_action:
					mClick.combine(mDate);
					break;
				}
			}
		}
	};
	
	public static CombineItemView create(Context context) {
		return (CombineItemView) View.inflate(context,
				R.layout.combine_item_view, null);
	}
}
