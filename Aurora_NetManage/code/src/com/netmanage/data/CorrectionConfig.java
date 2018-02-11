package com.netmanage.data;

public class CorrectionConfig {

	/**
	 * 在进行流量校正之前，需提供"设置的sim卡信息"
	 * 
	 * @param simIndex
	 *            卡槽1: IDualPhoneInfoFetcher.FIRST_SIM_INDEX 卡槽2:
	 *            IDualPhoneInfoFetcher.SECOND_SIM_INDEX
	 *            如果单卡手机，直接传IDualPhoneInfoFetcher.FIRST_SIM_INDEX
	 * @param provinceId
	 *            省份ID，getAllProvinces()元素中CodeName中mCode的值
	 * @param cityId
	 *            所属城市ID，getCities()元素中CodeName中mCode的值
	 * @param carryId
	 *            运营商ID，getCarries()元素中CodeName中mCode的值
	 * @param brandId
	 *            SIM卡类型ID，getBrands()元素中CodeName中mCode的值
	 * @param closingDay
	 *            月结日
	 * 
	 * @return ErrorCode 错误码
	 */

	public int mSimIndex;
	public String mProvinceId;
	public String mCityId;
	public String mCarryId;
	public String mBrandId;
	public int mClosingDay;

}
