package com.android.email.preferences;

import android.content.Context;
import aurora.preference.AuroraPreference;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.email.R;

public class AuroraTypePreferences extends AuroraPreference{
	private String serverType;
	private Context mContext;
	private TextView mType;
	
	public AuroraTypePreferences(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AuroraTypePreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		TypedArray type = context.obtainStyledAttributes(attrs,R.styleable.AuroraTypePreference);
		serverType = type.getString(R.styleable.AuroraTypePreference_type);
	}
	
	public AuroraTypePreferences(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		mType = (TextView)view.findViewById(R.id.server_type);
		if(!TextUtils.isEmpty(serverType))
			mType.setText(serverType);
	}
	
	private View view;

	@Override
	protected View onCreateView(ViewGroup parent) {
		// TODO Auto-generated method stub
//		return super.onCreateView(parent);
		view = LayoutInflater.from(mContext).inflate(R.layout.aurora_manual_login_servertype
				, parent, false);
		return view;
	}

	public String getServerType(){
		return serverType;
	}

	public void setServerType(String type){
		if(!TextUtils.isEmpty(type)){
			this.serverType = type;
			notifyChanged();
		}
	}
	
	
	public View getCurentView(){
		return view;
	}

}
