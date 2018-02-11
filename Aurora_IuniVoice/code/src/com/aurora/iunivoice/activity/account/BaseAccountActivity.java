package com.aurora.iunivoice.activity.account;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.aurora.datauiapi.data.interf.INotifiableManager;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.BaseActivity;
import com.aurora.iunivoice.utils.AccountPreferencesUtil;

public abstract class BaseAccountActivity extends BaseActivity {
	
	protected static final int ACTION_BAR_RIGHT_ITEM_ID = 1;
	
	private TextView mErrorTipsTv; // 显示错误提示信息的文本控件，可直接<include layout="@layout/error_info_layout" />
	private View mErrorView; // 需要高亮显示的控件的引用
	private EditText mErrorInputEt; // 输入不合法的输入框的引用
	protected TextView mActionBarRightTv;
	
	protected AccountPreferencesUtil mPref;
	
	private ProgressDialog mProgressDialog;
	
	private ArrayList<ErrorTextWatcher> mRegisteredWatchers =
	        new ArrayList<BaseAccountActivity.ErrorTextWatcher>(); // 注册过的TextWatcher
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mPref = AccountPreferencesUtil.getInstance(this);
	    
	    setContentBg(R.color.white);
	    
	    View tv =  getActionBarItem(ACTION_BAR_RIGHT_ITEM_ID);
	    if (tv != null) {
	    	((TextView) tv).setTextColor(getResources().getColor(R.color.action_bar_account_text_color));
	    }
	}
	

	
	protected void initErrorViews() {
	    mErrorTipsTv = (TextView) findViewById(R.id.error_tips_tv);
	}
	
	protected void setListenerForErrorView(EditText et) {
	    if (et != null) {
	        ErrorTextWatcher watcher = new ErrorTextWatcher(et);
	        et.addTextChangedListener(watcher);
	        mRegisteredWatchers.add(watcher);
	    }
    }
	
	/**
	 * 红色显示出错信息并高亮指定的输入框
	 * @param errorView
	 * @param et
	 * @param msg
	 */
	protected void showErrorInfo(View errorView, EditText et, String msg) {
	    if (mErrorTipsTv.getVisibility() != View.VISIBLE) {
            mErrorTipsTv.setVisibility(View.VISIBLE);
        }
        mErrorTipsTv.setText(msg);
        errorView.setSelected(true);
        mErrorView = errorView;
        mErrorInputEt = et;
        if (et != null) {
            et.requestFocus();
        }
    }
	
	private void hideErrorInfo() {
	    if (mErrorTipsTv.getVisibility() != View.GONE) {
            mErrorTipsTv.setText("");
            mErrorTipsTv.setVisibility(View.GONE);
            if (mErrorView != null) {
                mErrorView.setSelected(false);
                mErrorView = null;
            }
        }
    }
	
	private class ErrorTextWatcher implements TextWatcher {
	    private EditText mErrorEt; // 关联的文本控件的引用
	    
	    public ErrorTextWatcher(EditText errorEt) {
	        mErrorEt = errorEt;
	    }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mErrorInputEt != null && mErrorInputEt.equals(mErrorEt)) {
                hideErrorInfo();
                mErrorInputEt = null;
            }
        }
        
        public void unregister() {
            if (mErrorEt != null) {
                mErrorEt.removeTextChangedListener(this);
            }
        }
	}
	
	protected void showProgressDialog(String msg) {
        showProgressDialog(getString(R.string.dialog_prompt), msg);
    }
	
	protected void showProgressDialog(String title, String msg) {
	    if (mProgressDialog == null) {
	        mProgressDialog = ProgressDialog.show(this, title, msg);
	        mProgressDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    onProgressDialogDismiss();
                }
	        });
	    }
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		
	}

	protected void onProgressDialogDismiss() {
	}
	
	protected void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }
	
	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		super.onError(code, message, manager, e);
		dismissProgressDialog();
	}
	
	protected void startActivity(Class<?> targetClass) {
		Intent intent = new Intent(this, targetClass);
		startActivity(intent);
	}
	
	protected int getResInteger(int integerResId) {
	    return getResources().getInteger(integerResId);
	}

}
