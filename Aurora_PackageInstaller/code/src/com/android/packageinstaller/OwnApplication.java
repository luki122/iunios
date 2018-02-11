package com.android.packageinstaller;

import java.util.HashMap;
import java.util.Map;
import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;
import android.app.Application;

/**
 * 使用tms.jar，必须具备一个Application的子类
 */
public final class OwnApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// TMS总入口
		TMSDKContext.init(this, TmsSecureService.class,
			TMSDKContext.TYPE_OF_NONE, new ITMSApplicaionConfig() {
				@Override
				public HashMap<String, String> config(
						Map<String, String> src) {
					return new HashMap<String, String>(src);
				}
			});
	}
}
