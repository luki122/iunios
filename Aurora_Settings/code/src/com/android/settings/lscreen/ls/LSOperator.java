package com.android.settings.lscreen.ls;

import java.io.IOException;
import java.util.List;

import com.android.settings.R;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import aurora.preference.AuroraPreferenceCategory;

import com.android.settings.NotifyPushSettings;
import com.android.settings.lscreen.AppInfo;
import com.android.settings.lscreen.AuroraLSManageModel;
import com.android.settings.lscreen.DataArrayList;
import com.android.settings.lscreen.LSAppGridAdapter;
import com.android.settings.lscreen.LSAppObserver;
import com.android.settings.lscreen.LSAppSubject;
import com.android.settings.lscreen.LSAppActivity;
import com.android.settings.lscreen.Utils;

public class LSOperator implements OnItemClickListener,LSAppObserver{
	
    
	private Context mContext;
	private NotifyPushSettings view;
	private AuroraPreferenceCategory pref_LScreen;
	private LSCustomPreference customPreference;
	private LSAppGridAdapter adapter;
	
	private static final String LSPREFERENCE_KEY="my_preference_key";
	private static final String PREF_LSCREEN="pref_lscreen";
	public static final String TARGETPATH="/data/data/com.android.settings/";
	public static final String TARGETBECOMEPATH="/data/data/com.android.settings/app_icon/";
	
	public static final String TENCENT_WEIXIN="com.tencent.mm";
	public static final String IUNI_EMAIL="com.aurora.email";
	public static final String IUNI_MMS="com.android.mms";
	public static final String IUNI_CONTACTS="com.android.contacts";
	public static final String[] specialApp={LSOperator.IUNI_EMAIL,LSOperator.IUNI_MMS};
	public LSOperator(NotifyPushSettings view)
	{
		this.view=view;
		mContext=view.getActivity();

		//初始化所有APP 信息
		AuroraLSManageModel.getInstance(mContext).queryAppInfo();
		AuroraLSManageModel.getInstance(mContext).attach(this);
		initView();
	}
	
	private void initView()
	{
		pref_LScreen=(AuroraPreferenceCategory) view.findPreference(PREF_LSCREEN);
		customPreference=new LSCustomPreference(mContext);
		customPreference.setmOnItemClickListener(this);
		pref_LScreen.addPreference(customPreference);
	}

	public void onDestory()
	{
		AuroraLSManageModel.getInstance(mContext).detach(this);
		customPreference.onDestory();
	}
	
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		customPreference.dealClickEvent(position);
	}

	@Override
	public void initOrUpdateLSApp(LSAppSubject subject) {
	}
	
    public void onResume()
    {
    	customPreference.initOrUpdate();
    }

    @Override
	public void addOrUpdateLSApp(LSAppSubject subject, List<AppInfo> datas) {
		customPreference.initOrUpdate();
	}

	@Override
	public void delOrUpdateLSApp(LSAppSubject subject, List<AppInfo> datas) {
	}

	@Override
	public void allAppAchieve(LSAppSubject subject, List<AppInfo> datas) {
	}
	
	
}
