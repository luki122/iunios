package com.aurora.reject.adapter;
import com.aurora.reject.R;
import com.aurora.reject.util.SelectionManager;
import com.aurora.reject.util.YuloreUtil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsListView.RecyclerListener;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;

public class BlackNameAdapter extends CursorAdapter implements RecyclerListener {
	private int auroraItemHegiht;
	private LayoutInflater mInflater;
	private Context context;
	private boolean mIsShowCheckBox = false;
	private SelectionManager<Integer> mSelectionManager;
	public int mAllShowCheckBox = 0;
	public boolean mNeedAnim = false;
	
	public void updateAllCheckBox(int allShow) {
		this.mAllShowCheckBox = allShow;
	}
	public void setCheckBoxAnim(boolean needAnim) {
		this.mNeedAnim = needAnim;
	}
	public void showCheckBox(boolean show) {
        mIsShowCheckBox = show;
    }
	public void setSelectionManager(SelectionManager<Integer> selectionManager) {
        mSelectionManager = selectionManager;
    }
	public BlackNameAdapter(Context context, Cursor c) {
		super(context, c);
		this.context=context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		// TODO Auto-generated method stub

	}
	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		RelativeLayout front = (RelativeLayout)arg0.findViewById(com.aurora.R.id.aurora_listview_front);
        AuroraCheckBox mCheckBox = (AuroraCheckBox) arg0.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        TextView name=(TextView)((LinearLayout)(front.getChildAt(0))).getChildAt(0);
        TextView title=(TextView)((LinearLayout)(front.getChildAt(0))).getChildAt(1);
        String names=arg2.getString(arg2.getColumnIndex("black_name"));
        String lable=arg2.getString(arg2.getColumnIndex("lable"));
        String number=arg2.getString(arg2.getColumnIndex("number"));
        if(names==null||"".equals(names)){
        	 name.setText(number);
        	 if(lable==null||"".equals(lable)){
        		 String s=YuloreUtil.getArea(number);
        		 if(s==null||"".equals(s)){
        			 title.setText(arg1.getResources().getString(R.string.mars)); 
        		 }else{
        			 title.setText(s);  
        		 }
        	 }else{
        		 title.setText(lable);
        	 }
        }else{
        	 name.setText(names);
             title.setText(number);
        }
       

    	if (mIsShowCheckBox) {
        	if (mAllShowCheckBox == 1) {
				mCheckBox.auroraSetChecked(true, true);
			}else if (mAllShowCheckBox == 2) {
				mCheckBox.auroraSetChecked(false, true);
			}
        	if (mCheckBox.getAlpha() == 0.0f) {
        		if (mNeedAnim) {
            		aurora.widget.AuroraListView.auroraStartCheckBoxAppearingAnim(front, mCheckBox, true);//启动进入编辑状态动画
        		}else {
        			aurora.widget.AuroraListView.auroraSetCheckBoxVisible(front, mCheckBox, true);
				}
    		}
        	if (mSelectionManager != null) {
        		mCheckBox.setChecked(mSelectionManager.isSelected(arg2.getPosition()));
			}
        } else {
        	if (mCheckBox.getAlpha() == 1.0f){
        		aurora.widget.AuroraListView.auroraStartCheckBoxDisappearingAnim(front, mCheckBox);//启动退出编辑状态动画
                mCheckBox.setChecked(false);
        	}
           
        }
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View view = (View) mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null, false);
		RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		front.addView(mInflater.inflate(R.layout.black_name_list_item, null));
		auroraItemHegiht = view.getHeight();
		return view;
	}

}
