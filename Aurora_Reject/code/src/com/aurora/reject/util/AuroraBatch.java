package com.aurora.reject.util;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;

import com.aurora.reject.R;
import com.aurora.reject.util.SelectionManager.SelectionListener;
public abstract class AuroraBatch<T> implements SelectionListener{
	private Context context;
	private SelectionManager<T> mSelectionManager;
	private AuroraActionBar mActionBar;
	private TextView mSelectAllBtn;
    private TextView mCancelBtn;
    private TextView mMiddlePrompt;
    private boolean mInSelectionMode=false;
    
    public AuroraBatch(AuroraActionBar actionBar,Context context){
    	mActionBar=actionBar;
    	this.context=context;
    }
    
    public void enterSelectionMode(boolean autoLeave, T itemPressing) {
        if (null == mSelectionManager) {
            mSelectionManager = new SelectionManager<T>(context);
            bindToAdapter(mSelectionManager);
            mSelectionManager.setSelectionListener(this);
            mSelectionManager.setDataSet(getDataSet());
        }
        mInSelectionMode = true;
		initActionBarListener();
        if (null != mSelectionManager) {
        	if (null == itemPressing) {
        		onSelectionModeChange(SelectionManager.DESELECT_ALL_MODE);
			}else {
				mSelectionManager.toggle(itemPressing);
			}
		}
    }
    
    private void initActionBarListener() {
		//进入批量操作时，如果想获得左上角View
		mCancelBtn = (TextView) mActionBar.getSelectLeftButton();
		mCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				leaveSelectionMode();
			}
		});
		mSelectAllBtn = (TextView) mActionBar.getSelectRightButton();
		mMiddlePrompt = (TextView) mActionBar.getMiddleTextView();
		mSelectAllBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mSelectionManager==null){
					return ;
				}
				boolean isAllSelected = mSelectionManager.isAllSelected();
				if (isAllSelected) {
					mSelectAllBtn.setText(context.getResources().getString(R.string.select_all));
					mSelectionManager.deseletcAll();
					updateListView(2);
				} else {
					mSelectAllBtn.setText(context.getResources().getString(R.string.deselect_all));
					mSelectionManager.selectAll();
					updateListView(1);
				}
			}
		});
	}

	@Override
	public void onSelectionModeChange(int mode) {
		switch (mode) {
        case SelectionManager.SELECT_ALL_MODE:
        	if (null != mSelectAllBtn) {
        		mSelectAllBtn.setText(context.getResources().getString(R.string.deselect_all));
        		mMiddlePrompt.setText("已选择"+mSelectionManager.getSelectedCount()+"项");
			}
            break;
        case SelectionManager.DESELECT_ALL_MODE:
        	if (null != mSelectAllBtn) {
        		mSelectAllBtn.setText(context.getResources().getString(R.string.select_all));
        		mMiddlePrompt.setText("已选择0项");
			}
            break;

        default:
            break;
        }
        
		updateUi();
	}

	@Override
	public void onSelectionChange(Object item, boolean selected) {
		updateUi();
    	if (null != mSelectAllBtn) {
    		if (mSelectAllBtn.getText().equals(context.getResources().getString(R.string.deselect_all)) && !mSelectionManager.isAllSelected()) {
    			mSelectAllBtn.setText(context.getResources().getString(R.string.select_all));
    		}
		}
    	if (null != mMiddlePrompt) {
    		mMiddlePrompt.setText("已选择"+mSelectionManager.getSelectedCount()+"项");
    	}
	}
	
	public SelectionManager<T> getSelectionManger() {
        return mSelectionManager;
    }
	
	public ArrayList<T> getSelected() {
        if (null == mSelectionManager) {
            return null;
        }
        return mSelectionManager.getSelected();
    }
	
	public boolean isInSelectionMode() {
	        return mInSelectionMode;
	    }
	
	public void destroyAction() {
		mInSelectionMode = false;
    	bindToAdapter(null);
    	mSelectionManager = null;
	}

    public abstract void updateUi();
	
	public abstract void updateListView(int allShow);
	
	public abstract void leaveSelectionMode();
	
	public abstract void bindToAdapter(SelectionManager<T> selectionManager);   

}
