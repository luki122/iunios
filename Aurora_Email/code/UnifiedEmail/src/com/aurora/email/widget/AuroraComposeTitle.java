package com.aurora.email.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mail.R;
import com.aurora.email.AuroraComposeActivity;

import aurora.widget.AuroraEditText;

public class AuroraComposeTitle extends LinearLayout implements OnFocusChangeListener{

	private Context mContext;
	private FocusControl mFocusControl;
	private AuroraEditText mEditText;
	private TextView mTextView;
	private OnFocusChangeListener mFocusChangedListener;
	private boolean mFlag = false;
	public AuroraComposeTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}
	private void init(Context context){
		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.aurora_compose_title_layout, this);
		mEditText = (AuroraEditText)findViewById(R.id.aurora_et_title);
		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence charsequence, int i, int j,
					int k) {
				// TODO Auto-generated method stub
				requestLayout();
			}

			@Override
			public void beforeTextChanged(CharSequence charsequence, int i,
					int j, int k) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable editable) {
				// TODO Auto-generated method stub
				if(!mFlag){
					mTextView.setText(editable.toString());
				}else{
					mFlag = false;
				}
			}
		});
		mEditText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mFocusChangedListener!=null){
					mFocusChangedListener.onFocusChange(AuroraComposeTitle.this, true);
				}
			}
		});
		mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){
					showSingle(false);
				}else{
					if(!TextUtils.isEmpty(mEditText.getText()))
						showSingle(true);
				}
				if(mFocusChangedListener!=null){
					mFocusChangedListener.onFocusChange(AuroraComposeTitle.this, arg1);
				}
			}
		});
		mTextView = (TextView) findViewById(R.id.aurora_tv_title);
		mTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showSingle(false);
				if(mFocusChangedListener!=null){
					mFocusChangedListener.onFocusChange(AuroraComposeTitle.this, true);
				}
			}
		});
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		if(action == KeyEvent.ACTION_DOWN){
			int key = event.getKeyCode();
			switch (key) {
			case KeyEvent.KEYCODE_ENTER:
				showSingle(true);
				if(mFocusControl !=	null){
					mFocusControl.moveFocus(R.id.id_write_email);
				}
				break;

			default:
				return super.dispatchKeyEvent(event);
			}
		}else{
			return super.dispatchKeyEvent(event);
		}
		return true;
	}
	public void  setFocusControl(FocusControl control) {
		mFocusControl = control;
	}
	public void setText(String text){
		if(!TextUtils.isEmpty(text)){
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(text);
		}
	}
	public void setOnFocusChangeListener(OnFocusChangeListener listener){
		mFocusChangedListener = listener;
	}
	public String getText(){
		return mTextView.getText().toString();
	}
	@Override
	public void onFocusChange(View arg0, boolean arg1) {
		// TODO Auto-generated method stub
		mFocusChangedListener.onFocusChange(arg0, arg1);
	}
	private void showSingle(boolean flag){
		if(flag){
			mFlag = true;
			mEditText.getText().clear();
			mTextView.setVisibility(View.VISIBLE);
		}else{
			mTextView.setVisibility(View.GONE);
			mEditText.setText(getText());
			mEditText.setSelection(getText().length());
			mEditText.requestFocus();
			AuroraComposeActivity.showInputMethod(mContext, mEditText);
		}
	}
}
