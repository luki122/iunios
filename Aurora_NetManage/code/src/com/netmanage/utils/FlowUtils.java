package com.netmanage.utils;

import com.netmanage.activity.FlowSetActivity;
import com.netmanage.data.ConfigData;
import com.netmanage.interfaces.ConfigSubject;
import com.netmanage.model.ConfigModel;
import com.netmanage.model.CorrectFlowModel;
import com.netmanage.model.NetModel;
import com.netmanage.totalCount.CountInfo;
import com.netmanage.totalCount.TotalCount;
import com.netmanage.view.InfoDialog;
import com.netmanage.view.InfoDialog.EditDialogPosiBtnCallBack;
import com.aurora.netmanage.R;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 流量的工具类
 * @author chengrq
 *
 */
public class FlowUtils {
	/**
	 * 校正流量
	 */
	public static synchronized void manualCorrectionFlow(final Activity activity){
		if( !Utils.isActivityAvailable(activity)){
			return ;
		}
		InfoDialog.showCustomEditDialog(
				activity,
				R.string.manual_correction,
				R.string.sure,
				new EditDialogPosiBtnCallBack(){
					@Override
					public void onPositiBtn(String editStr) {
						try{
							long monthlyFlow = Long.parseLong(editStr); 
							correctFlowFunc(activity,monthlyFlow);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				},
				R.string.cancel
		    );
	}
	
	/**
	 * 校正流量
	 * @param correctFlow
	 */
	private static void correctFlowFunc(Context context,long correctFlow){
		if(correctFlow < 0 || context == null){
			return ;
		}
		
		CorrectFlowModel.getInstance(context).
 	       CorrectFlow(TimeUtils.getCurrentTimeMillis(), correctFlow);
		
		// 统计 点击手动校正流量
		new TotalCount(context, CountInfo.MODULE_ID, CountInfo.ACTION_ID_4, 1).CountData();
	}
	
	/**
	 * 改变流量套餐值
	 */
	public static synchronized void changeMonthlyFlow(Context context,long monthlyFlow){
		if(monthlyFlow < 0 || context == null){
			return ;
		}
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		if(configData.getMonthlyFlow() != monthlyFlow){
			configData.setMonthlyFlow(monthlyFlow);
			savePackageSetting(configData,context);
			ConfigSubject.getInstance().notifyObserversOfMonthlyFlowChange();
		}		
	}
	
	/**
	 * 改变套餐月结日
	 */
	public static synchronized void changeMonthEndDate(Context context,int monthEndDate){
		if(monthEndDate < 1 || context == null){
			return ;
		}
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		if(!configData.getIsAlreadyChoiceMonthEndDate() || 
				configData.getMonthEndDate() != monthEndDate){
			CorrectFlowModel.getInstance(context).deleteAllRecord();
			configData.setMonthEndDate(monthEndDate);
			configData.setIsAlreadyChoiceMonthEndDate(true);
			savePackageSetting(configData,context);
			NetModel.getInstance(context).resetNetInfo();
		}		
	}
	
	/**
	 * 保存套餐设置
	 * @param context
	 */
	private static void savePackageSetting(ConfigData configData,Context context){
		if(configData == null || context == null){
			return ;
		}
	    if(configData.isSetedFlowPackage()){
	    	configData.setImsi(Utils.getImsi(context));
	    	MySharedPref.clearSimChangeNotifyData(context);
	    }	
		ConfigModel.getInstance(context).saveConfigData();
	}
	
	
	/**
	 * 当前流量是否已经达到预警值
	 * 1.当流量套餐超过用户设置预警值时，图标变红;
     * 2.未设置套餐不变红;
     * 3.不开启预警值的话我们按照我们默认的85%变红;
	 * @param totalFlow
	 * @param progressFlow
	 * @return
	 */
	public static synchronized boolean isWarningProgress(Context context){
		if(context == null){
			return false;
		}
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		if(!configData.isSetedFlowPackage()){
			return false;
		}
		int earlyWarningPercent;
		if(configData.getExcessEarlyWarningSwitch()){
			earlyWarningPercent = configData.getEarlyWarningPercent();
		}else{
			earlyWarningPercent = ConfigData.DEF_EARLY_WARNIMG_PERCENT;
		}
		long usedFlow = ApkUtils.getUsedFlow(context);		
		long totalFlow = configData.getMonthlyFlow()*1024;
		if(usedFlow > totalFlow*earlyWarningPercent/100){
			return true;
		}else{
			return false;
		}		
	}
}
