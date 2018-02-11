package com.aurora.account.activity;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;

import com.aurora.account.R;
import com.aurora.account.common.MessageHandler;
import com.aurora.account.common.WeakHandler;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.BooleanPreferencesUtil;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.ToastUtil;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;

public class BaseActivity extends AuroraActivity implements MessageHandler, INotifiableController {
	protected final String TAG = getClass().getSimpleName();
	
	private static final int ACTION_BAR_RIGHT_ITEM_ID = 1;

	protected WeakHandler mHandler = new WeakHandler(this);
	
	private AuroraProgressDialog mProgressDialog;
	
	private TextView mErrorTipsTv; // 显示错误提示信息的文本控件，可直接<include layout="@layout/error_info_layout" />
	private View mErrorView; // 需要高亮显示的控件的引用
	private EditText mErrorInputEt; // 输入不合法的输入框的引用
	protected TextView mActionBarRightTv;
	protected AccountPreferencesUtil mPref;
	
	private ArrayList<ErrorTextWatcher> mRegisteredWatchers =
	        new ArrayList<BaseActivity.ErrorTextWatcher>(); // 注册过的TextWatcher

	@Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mPref = AccountPreferencesUtil.getInstance(this);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		initActionBar();
		
		if (shouldHandleFocusChanged()) {
		    showInputMethod();
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

	private void initActionBar() {
		AuroraActionBar actionBar = getAuroraActionBar();
		actionBar.setTitle(getActionBarTitle());
		actionBar.setBackground(getActionBarBackground());
		actionBar.getTitleView().setTextColor(getResources().getColor(R.color.action_bar_text_color));
	}
	
	protected void configActionBar(String rightText) {
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.addItem(R.layout.actionbar_right_textview, ACTION_BAR_RIGHT_ITEM_ID);
        mActionBarRightTv = (TextView) actionBar.findViewById(R.id.right_tv);
        mActionBarRightTv.setTextColor(getResources().getColor(R.color.action_bar_text_color));
        mActionBarRightTv.setText(rightText);
    }

	protected String getActionBarTitle() {
		return getResources().getString(R.string.app_name);
	}

	protected Drawable getActionBarBackground() {
		return getResources().getDrawable(
				R.drawable.aurora_action_bar_top_bg_green);
	}

	protected void startActivity(Class<?> targetClass) {
		Intent intent = new Intent(this, targetClass);
		startActivity(intent);
	}
	
	protected int getResInteger(int integerResId) {
	    return getResources().getInteger(integerResId);
	}

//	protected void showErrorTips(EditText et, CharSequence errorMsg) {
//		if (!et.isFocused()) {
//			et.requestFocus();
//		}
//		et.setError(errorMsg);
//	}
	
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
	        mProgressDialog = AuroraProgressDialog.show(this, title, msg);
	        mProgressDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    onProgressDialogDismiss();
                }
	        });
	    }
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
	public void handleMessage(Message msg) {
        switch (msg.what) {
            case Globals.NETWORK_ERROR:
                ToastUtil.longToast(R.string.network_exception);
                break;
            case Globals.NO_NETWORK:
                ToastUtil.longToast(R.string.network_not_available);
                break;
            case Globals.SERVER_ERROR:
                ToastUtil.longToast(R.string.server_error);
                break;
            case Globals.SESSION_EXPIRED_ERROR: // 需要重新登录
                relogin();
                break;
            default:
                break;
        }
	}
	
	private void relogin() {
	    new AuroraAlertDialog.Builder(this)
	    .setTitle(R.string.dialog_prompt)
	    .setMessage(R.string.server_session_is_expired)
	    .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPref.clear();
                BooleanPreferencesUtil.getInstance(getApplicationContext()).setLogin(false);
                
                Intent firstIntent = new Intent(BaseActivity.this, SyncAccountActivity.class);
                firstIntent.putExtra(Globals.EXTRA_COMMAND, Globals.COMMAND_LOGOUT);
                firstIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(firstIntent);
            }
        })
        .create()
        .show();
	}

    @Override
    public void onWrongConnectionState(int state, INotifiableManager manager, Command<?> source) {
        dismissProgressDialog();
    }

    @Override
    public void onError(int code, String message, INotifiableManager manager, Exception e) {
        Log.i(TAG, "the code=" + code + "  msg=" + message);
        dismissProgressDialog();
        
        switch (code) {
        case INotifiableController.CODE_UNKNONW_HOST:
        case INotifiableController.CODE_WRONG_DATA_FORMAT:
        case INotifiableController.CODE_REQUEST_TIME_OUT:
        case INotifiableController.CODE_CONNECT_ERROR:
        case INotifiableController.CODE_GENNERAL_IO_ERROR:
        case INotifiableController.CODE_NOT_FOUND_ERROR:
        case INotifiableController.CODE_JSON_PARSER_ERROR:
        case INotifiableController.CODE_JSON_MAPPING_ERROR:
        case INotifiableController.CODE_UNCAUGHT_ERROR:
            mHandler.sendEmptyMessage(Globals.NETWORK_ERROR);
            break;
        case INotifiableController.CODE_NOT_NETWORK:
            mHandler.sendEmptyMessage(Globals.NO_NETWORK);
            break;
        case INotifiableController.CODE_SERVER_ERROR:
            mHandler.sendEmptyMessage(Globals.SERVER_ERROR);
            break;
        case INotifiableController.CODE_SESSION_EXPIRED_ERROR:
            mHandler.sendEmptyMessage(Globals.SESSION_EXPIRED_ERROR);
            break;
        default:
            break;
        }
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void runOnUI(DataResponse<?> response) {
        mHandler.post(response);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        if (!ret) {
            hideInputKeyboard();
        }
        
        return ret;
    }
    
    protected void hideInputKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            View focusView = getCurrentFocus();
            if (focusView != null) {
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
        }
    }
    
    protected boolean shouldHandleFocusChanged() {
        return true;
    }
    
    private void showInputMethod() {
        final EditText edittext = searchEditText();
        if (edittext == null) {
            Log.e(TAG, "Jim, showInputMethod, edittext is null.");
            return;
        }
        
        edittext.setFocusable(true);
        edittext.requestFocus();
        
        mHandler.postDelayed(new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) edittext
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                if (edittext.isFocused()) {
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
                }
            }
        }, 200);
    }
    
    private EditText searchEditText() {
        FrameLayout fl = getContentView();
        return searchEditText(fl);
    }
    
    private EditText searchEditText(ViewGroup vg) {
        final int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i ++) {
            View v = vg.getChildAt(i);
            if (v instanceof ViewGroup) {
                return searchEditText((ViewGroup)v);
            } else {
                if (v instanceof EditText) {
                    return (EditText) v;
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Jim, onDestroy enter");
        
        if (mRegisteredWatchers != null && !mRegisteredWatchers.isEmpty()) {
            for (int i = 0, size = mRegisteredWatchers.size(); i < size; i ++) {
                ErrorTextWatcher watcher = mRegisteredWatchers.get(i);
                watcher.unregister();
            }
            mRegisteredWatchers.clear();
        }
        mRegisteredWatchers = null;
        
        dismissProgressDialog();
        
        super.onDestroy();
    }    
}