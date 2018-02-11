package com.android.email.preferences;


import android.content.Context;
import android.content.res.TypedArray;
import aurora.preference.AuroraPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import aurora.widget.AuroraEditText;
import com.android.email.R;

public class AuroraManualEditPreferences extends AuroraPreference{
	private String title;
	private String editText;
	private boolean isPassword = false;
	private boolean isNumeric = false;
	private Context mContext;
	private TextView mTitle;
	private AuroraEditText mAuroraEditText;
	private final static float AURORA_EDITTEXT_SIZE = 14 ;
	
	public AuroraManualEditPreferences(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AuroraManualEditPreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		TypedArray type = context.obtainStyledAttributes(attrs, R.styleable.AuroraManualEditPreference);
		title = type.getString(R.styleable.AuroraManualEditPreference_editTitle);
		editText = type.getString(R.styleable.AuroraManualEditPreference_editText);
		isPassword = type.getBoolean(R.styleable.AuroraManualEditPreference_isPassword, false);
		isNumeric = type.getBoolean(R.styleable.AuroraManualEditPreference_isNumeric, false);
	}

	public AuroraManualEditPreferences(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		
		mTitle = (TextView)view.findViewById(R.id.title);
		mAuroraEditText = (AuroraEditText)view.findViewById(R.id.edittext);
		mAuroraEditText.setTextSize(AURORA_EDITTEXT_SIZE);   
		mAuroraEditText.setTextColor(mContext.getResources().getColor(R.color.aurora_manual_login_button_color));
		mAuroraEditText.addTextChangedListener(mTextWatcher);

		mTitle.setText(title);
		mAuroraEditText.setText(editText);
		if(isPassword)
			mAuroraEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		if(isNumeric)
			mAuroraEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		
/*		title = mTitle.getText().toString();
		editText = mAuroraEditText.getText().toString();
		Log.i("haozi","title =="+title + " editText ="+editText);*/
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		// TODO Auto-generated method stub
//		return super.onCreateView(parent);
		return LayoutInflater.from(mContext).inflate(R.layout.aurora_manual_edit_preference_layout
				, parent, false);
	}

	public String getTitle(){
		return title;
	}	
	
	public String getEditText(){
		return editText;
	}
	
	public void setTitle(String title){
		if(!TextUtils.isEmpty(title)){
			this.title = title;
			notifyChanged();
		}
	}
	
	public void setEditText(String input){
		if(!TextUtils.isEmpty(input)){
			this.editText = input;
			notifyChanged();
		}
	}
	
	TextWatcher mTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			editText = mAuroraEditText.getText().toString();
		}
	};
}
