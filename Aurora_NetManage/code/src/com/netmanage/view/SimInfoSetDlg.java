package com.netmanage.view;

import java.util.ArrayList;

import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.TrafficCorrectionManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;

import com.aurora.netmanage.R;
import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.interfaces.SimInfoSetCallBack;

/**
 * 弹出对话框，让用户选择sim相关的基本属性
 * @author chengrq
 *
 */
public class SimInfoSetDlg implements OnClickListener, OnCancelListener{
	private Activity mContext;
	private TrafficCorrectionManager mTcm;
	private CorrectFlowBySmsData correctFlowBySmsData;
	private SimInfoSetCallBack callBack;
	
	private Dialog mCitysDlg;
	private Dialog mProvinceDlg;
	private Dialog mCarrysDlg;
	private Dialog mBrandsDlg;
	
	private ArrayList<CodeName> mProvinces;
	private ArrayList<CodeName> mCitysList;
	private ArrayList<CodeName> mCarrys;	
	private ArrayList<CodeName> mBrands;
	
	public enum SetSimInfoTypeEnum{
		/**
		 * sim卡所属的省份与城市
		 */
		Location,
		/**
		 * sim卡所属的运营商
		 */
		Carry,
		/**
		 * sim的品牌
		 */
		Brand
	}
	
	public SimInfoSetDlg(Activity context, 
			TrafficCorrectionManager ntcm,
			CorrectFlowBySmsData correctFlowBySmsData,
			SimInfoSetCallBack callBack) {
		mContext = context;
		mTcm = ntcm;
		this.correctFlowBySmsData = correctFlowBySmsData;
		this.callBack = callBack;
	}
	
	/**
	 * 设置sim所属的省份
	 */
	public void startProvinceDlg() {
		mProvinces = mTcm.getAllProvinces();
		String[] nItems = new String[mProvinces.size()];
		for(int i = 0; i < nItems.length; i++) {
			nItems[i] = mProvinces.get(i).mName;
		}		
		mProvinceDlg = showSingleChoiceDialog(mContext,R.string.select_province,nItems,0);
	}
	
	private void startCityDlg(){
		CodeName province = correctFlowBySmsData.getProvince();
		if(province == null){
			return ;
		}
		mCitysList = mTcm.getCities(province.mCode);
		if(mCitysList == null || mCitysList.size() == 0){
			return ;
		}else if(mCitysList.size() == 1){
			//只有一个选项时，直接选择第一个，例如：省为北京，城市也为北京的情况
			correctFlowBySmsData.setCity(mCitysList.get(0));
			if(callBack != null){
				callBack.result(false, SetSimInfoTypeEnum.Location);
			}
			return ;
		}		
		
		String[] nItems = new String[mCitysList.size()];
		for(int i = 0; i < nItems.length; i++) {
			nItems[i] = mCitysList.get(i).mName;
		}
		mCitysDlg = showSingleChoiceDialog(mContext,R.string.select_city,nItems,0);
	}
	
	/**
	 * 设置sim卡的所属的运营商
	 */
	public void startCarryDlg(){
		mCarrys = mTcm.getCarries();
		String[] nItems = new String[mCarrys.size()];
		for(int i = 0; i < nItems.length; i++) {
			nItems[i] = mCarrys.get(i).mName;
		}
		mCarrysDlg = showSingleChoiceDialog(mContext,R.string.select_carry,nItems,0);
	}
	
	public void startBrandDlg(){
		CodeName carry = correctFlowBySmsData.getCarry();
		if(carry == null){
			Toast.makeText(mContext, R.string.select_carry_first, 1000).show();
			return ;
		}
		mBrands = mTcm.getBrands(carry.mCode);
		String[] nItems = new String[mBrands.size()];
		for(int i = 0; i < nItems.length; i++) {
			nItems[i] = mBrands.get(i).mName;
		}
		mBrandsDlg = showSingleChoiceDialog(mContext,R.string.select_brands,nItems,0);
	}
	
	private Dialog showSingleChoiceDialog(Activity activity,
			int title,
			String[] nItems,
			int checkItem){	
		if(activity == null || activity.isFinishing()){
			return null;
		}

		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(activity);
		builder.setTitle(title);
		builder.setNegativeButton(R.string.cancel, this);
		builder.setTitleDividerVisible(true);	
		builder.setSingleChoiceItems(nItems, checkItem, this);
		builder.setOnCancelListener(this);
		Dialog alertDialog = builder.create();			
	    try{	    	
	    	alertDialog.show();
		}catch(Exception e){
			e.printStackTrace();
		} 
	    return alertDialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_NEGATIVE){
			onCancel(dialog);
			return ;
		}
		if(which<0){
			return ;
		}
		if(mProvinceDlg == dialog) {
			correctFlowBySmsData.setProvince(mProvinces.get(which));
			correctFlowBySmsData.setCity(null);			
			startCityDlg();
		} else if(dialog == mCitysDlg) {
			correctFlowBySmsData.setCity(mCitysList.get(which));
			if(callBack != null){
				callBack.result(false, SetSimInfoTypeEnum.Location);
			}
		} else if(dialog == mCarrysDlg) {
			correctFlowBySmsData.setCarry(mCarrys.get(which));
			correctFlowBySmsData.setBrand(null);
			if(callBack != null){
				callBack.result(false, SetSimInfoTypeEnum.Carry);
			}
		} else if(dialog == mBrandsDlg) {
			correctFlowBySmsData.setBrand(mBrands.get(which));
			if(callBack != null){
				callBack.result(false, SetSimInfoTypeEnum.Brand);
			}
		}
		dialog.dismiss();
	}

	@Override
	public void onCancel(DialogInterface dialog) {		
		if(mProvinceDlg == dialog) {
			if(callBack != null){
				callBack.result(true, SetSimInfoTypeEnum.Location);
			}
		} else if(dialog == mCitysDlg) {
			correctFlowBySmsData.setProvince(null);
			correctFlowBySmsData.setCity(null);	
			if(callBack != null){
				callBack.result(true, SetSimInfoTypeEnum.Location);
			}
		} else if(dialog == mCarrysDlg) {
			if(callBack != null){
				callBack.result(true, SetSimInfoTypeEnum.Carry);
			}
		} else if(dialog == mBrandsDlg) {	
			if(callBack != null){
				callBack.result(true, SetSimInfoTypeEnum.Brand);
			}
		}
	}	
}
