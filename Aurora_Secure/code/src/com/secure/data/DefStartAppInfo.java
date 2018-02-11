package com.secure.data;

import android.content.ComponentName;
import android.content.IntentFilter;

/**
 * 默认启动软件数据
 * @author chengrq
 *
 */
public class DefStartAppInfo extends BaseData{

	private ComponentName componentName;
	private IntentFilter prefIntent;

	
	public DefStartAppInfo() {
		super("DefStartAppInfo");
	}
	
	public ComponentName getComponentName() {
		return componentName;
	}

	public void setComponentName(ComponentName componentName) {
		this.componentName = componentName;
	}
	
	/**
	 * 该默认软件的intent属性
	 * @param prefIntent
	 */
	public void setPrefIntent(IntentFilter prefIntent){
		this.prefIntent = prefIntent;
	}
	
	/**
	 * 该默认软件的intent属性
	 * @return
	 */
	public IntentFilter getPrefIntent(){
		return this.prefIntent;
	}
}
