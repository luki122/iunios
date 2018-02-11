package com.android.mms.util;

import java.util.ArrayList;

import com.android.mms.util.GnSelectionManager.SelectionListener;

import android.app.Activity;
// Aurora liugj 2013-09-13 added for aurora's new feature start
import android.graphics.Typeface;
// Aurora liugj 2013-09-13 added for aurora's new feature end
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.mms.R;

public abstract class GnActionModeHandler<T> implements ActionMode.Callback, SelectionListener {

    private final Activity mActivity;
    private GnSelectionManager<T> mSelectionManager;
    private ActionMode mActionMode;    
    private String mActionTitle;
    private Menu mMenu;
    private int mActionMenuRes;
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//    private CheckBox mSelectAllCheckBox;
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    // Aurora liugj 2013-09-13 added for aurora's new feature start
    private TextView mSelectAllBtn;
    private TextView mCancelBtn;
    // Aurora liugj 2013-09-13 added for aurora's new feature start
    private TextView mTitleTextView;
    
    private boolean mInSelectionMode;
    private boolean mAutoLeave = true;
    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    
    public GnActionModeHandler(Activity activity, String title, int actionMenuRes) {
        mActivity = activity;
        mActionMenuRes = actionMenuRes;
        mActionTitle = title;
    }
    
    public void enterSelectionMode(boolean autoLeave, T itemPressing) {
        if (null == mSelectionManager) {
            mSelectionManager = new GnSelectionManager<T>(mActivity);
            bindToAdapter(mSelectionManager);
            mSelectionManager.setSelectionListener(this);
            mAutoLeave = autoLeave;
            refreshDataSet();
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
            //mSelectionManager.toggle(itemPressing);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        }
        
        beforeEnterSelectionMode();
        mActionMode = mActivity.startActionMode(this);
        mInSelectionMode = true;
        mActionMode.setCustomView(createCustomView());
        // Aurora liugj 2013-09-13 added for aurora's new feature start
        if (null != mSelectionManager) {
            mSelectionManager.toggle(itemPressing);
        }
        // Aurora liugj 2013-09-13 added for aurora's new feature start
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        updateTitle();
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    }
    
    protected View createCustomView() {
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        View custom = LayoutInflater.from(mActivity).inflate(
                R.layout.aurora_action_mode_custom_view, null);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        // Aurora liugj 2013-09-13 added for aurora's new feature start
        mCancelBtn = (TextView) custom.findViewById(R.id.action_mode_cancel_btn);
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveSelectionMode();
            }
        });
        // Aurora liugj 2013-09-13 added for aurora's new feature end
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        mTitleTextView = (TextView) custom.findViewById(R.id.action_mode_title);
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        mSelectAllBtn = (TextView) custom.findViewById(R.id.action_mode_selectall_btn);
        mSelectAllBtn.setOnClickListener(new OnClickListener() {
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
            @Override
            public void onClick(View v) {
                boolean isAllSelected = isAllSelected();
                // Aurora liugj 2013-09-13 deleted for aurora's new feature start
                //mSelectAllCheckBox.setChecked(isAllSelected);
                // Aurora liugj 2013-09-13 deleted for aurora's new feature end
                if (isAllSelected) {
                    // Aurora liugj 2013-09-13 added for aurora's new feature start
                    mSelectAllBtn.setText(R.string.select_all);
                    // Aurora liugj 2013-09-13 added for aurora's new feature end
                    deselectAll();
                } else {
                    // Aurora liugj 2013-09-13 added for aurora's new feature start
                    mSelectAllBtn.setText(R.string.unselect_all);
                    // Aurora liugj 2013-09-13 added for aurora's new feature end
                    selectAll();
                }
            }
        });
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
        /*mSelectAllCheckBox = (CheckBox) custom.findViewById(R.id.action_mode_check_box);
        mSelectAllCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAllSelected = isAllSelected();
                mSelectAllCheckBox.setChecked(isAllSelected);
                if (isAllSelected) {
                    deselectAll();
                } else {
                    selectAll();
                }
            }
        });*/
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        return custom;
    }
    
    public void beforeEnterSelectionMode() {}
    
    public abstract void bindToAdapter(GnSelectionManager<T> selectionManager);   

    public void leaveSelectionMode() {
        if (null != mActionMode) {
            mActionMode.finish();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mMenu = menu;
        if (mActionMenuRes > 0) {
            MenuInflater inflater = mActivity.getMenuInflater();
            inflater.inflate(mActionMenuRes, menu);
        }
        
        return true;
    }
    
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        
        
        return true;
    }
    
    @Override
    public void onDestroyActionMode(ActionMode mode) {        
        mInSelectionMode = false;
        bindToAdapter(null);
        updateUi();
        mActionMode = null;
        mSelectionManager = null;
    }
    
    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
        case GnSelectionManager.SELECT_ALL_MODE:
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            if (null != mSelectAllCheckBox) {
//                mSelectAllCheckBox.setChecked(true);    
//            }
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            // Aurora liugj 2013-09-13 added for aurora's new feature start
            if (null != mSelectAllBtn) {
                mSelectAllBtn.setText(R.string.unselect_all);
            }
            // Aurora liugj 2013-09-13 added for aurora's new feature end
            break;
        case GnSelectionManager.DESELECT_ALL_MODE:
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            if (null != mSelectAllCheckBox) {
//                mSelectAllCheckBox.setChecked(false);    
//            }
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            // Aurora liugj 2013-09-13 added for aurora's new feature start
            if (null != mSelectAllBtn) {
                mSelectAllBtn.setText(R.string.select_all);
            }
            // Aurora liugj 2013-09-13 added for aurora's new feature end
            if (mAutoLeave) {
                leaveSelectionMode();
                return;
            }
            break;

        default:
            break;
        }
        
        update();
    }

    @Override
    public void onSelectionChange(Object path, boolean selected) {
        update();
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        if (null != mSelectAllBtn) {
            if (!selected && mSelectAllBtn.getText().equals(mActivity.getString(R.string.unselect_all))) {
                mSelectAllBtn.setText(R.string.select_all);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
            }
        }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        if (null != mSelectAllCheckBox) {
//            if (!selected && mSelectAllCheckBox.isChecked()) {
//                mSelectAllCheckBox.setChecked(false);
//            }
//        }
        // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    }
    
    private void update() {
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        onPrepareActionMode(mActionMode, mMenu);
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        updateUi();
    // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//        updateTitle();
    // Aurora liugj 2013-09-13 deleted for aurora's new feature end
    }
    
    protected void updateTitle() {        
        if (null != mActionMode){
            String title = null;
            int count = null != mSelectionManager ? mSelectionManager.getSelectedCount() : 0;
            Integer size =  null != mSelectionManager ? mSelectionManager.getSelectableSize() : null;
            /*if (null != mActionTitle) {
                title = mActionTitle + " (" + count +")";
            } else if (null != size) {
                title = count + "/" +  size;
            } else {
                title = String.valueOf(count);
            }*/
            title = mActivity.getResources().getString(R.string.gn_select_conversation_more, count);
            mActionMode.setTitle(title);
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            if (null != mTitleTextView) {
//                mTitleTextView.setText(title);
//            }
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
        }
    }        
    
    public abstract void updateUi();
    
    public boolean inSelectionMode() {
        return mInSelectionMode;
    }
    
    public GnSelectionManager<T> getSelectionManger() {
        return mSelectionManager;
    }
    
    public ArrayList<T> getSelected() {
        if (null == mSelectionManager) {
            return null;
        }
        return mSelectionManager.getSelected();
    }

    public boolean refreshDataSet() {
        if (null != mSelectionManager) {
            mSelectionManager.setDataSet(getDataSet());
            return true;
        }
        
        return false;
    }
    
    public boolean refreshDataSetIfEmpty() {
        if (null != mSelectionManager && !mSelectionManager.isDataSetReady()) {
            mSelectionManager.setDataSet(getDataSet());
            // gionee zhouyj 2013-03-14 add for CR00784180 start
            // Aurora liugj 2013-09-13 deleted for aurora's new feature start
//            if (null != mSelectAllCheckBox) {
//                mSelectAllCheckBox.setChecked(mSelectionManager.isAllSelected());
//            }
            // Aurora liugj 2013-09-13 deleted for aurora's new feature end
            // Aurora liugj 2013-09-13 added for aurora's new feature start
            if (null != mSelectAllBtn) {
                boolean isAllSelected = mSelectionManager.isAllSelected();
                if (isAllSelected) {
                    mSelectAllBtn.setText(R.string.unselect_all);
                } else {
                    mSelectAllBtn.setText(R.string.select_all);
                }
            // Aurora liugj 2013-09-13 added for aurora's new feature end
            }
            // gionee zhouyj 2013-03-14 add for CR00784180 end
            return true;
        }
        
        return false;
    }
    
    public void earseDataSet() {
        if (null != mSelectionManager) {
            mSelectionManager.setDataSet(null);
        }
    }
    
    public boolean isAllSelected() {
        if (null != mSelectionManager) {
            return mSelectionManager.isAllSelected();
        }
        
        return false;
    }
    
    public void selectAll() {
        refreshDataSetIfEmpty();
        if (null != mSelectionManager) {
            mSelectionManager.selectAll();
        }
    }
    
    public void deselectAll() {
        if (null != mSelectionManager) {
            mSelectionManager.deseletcAll();
        }
    }
    
    public void toggle(T item) {
        if (null != mSelectionManager) {
            mSelectionManager.toggle(item);
        }
    }
    
    public int getSelectedCount() {
        int count = 0;
        if (null != mSelectionManager) {
            count = mSelectionManager.getSelectedCount();
        }
        
        return count;
    }
    
    public boolean isInSelectionMode() {
        return mInSelectionMode;
    }
    
    public void setAutoLeave(boolean enable) {
        mAutoLeave = enable;
    }
    
    public boolean isAutoLeave() {
        return mAutoLeave;
    }
}
