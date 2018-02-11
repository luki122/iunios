package com.android.settings.lscreen;

import java.util.HashMap;


public class LSSameFirstCharAppData extends BaseData{

	   private String firstChar;
	   private DataArrayList<BaseData> appList;
	   public HashMap<String, DataArrayList> map = new HashMap<String, DataArrayList>();	
	   
	   public LSSameFirstCharAppData(){
		   super(LSSameFirstCharAppData.class.getName());
		   appList = new DataArrayList<BaseData>();
	   }
	   
       
//	   public DataArrayList<BaseData> getCharToList(String chard)
//	   {
//		   if(map.containsKey(chard))
//		   {
//			   return map.get(chard);
//		   }else
//		   {
//			   return null;
//		   }
//	   }
	   
	   /**
	    * 应用首字符
	    * @param categoryName
	    */
	   public void setFirstChar(String firstChar){
		   this.firstChar = firstChar;
		 
	   }
	   
	   /**
	    * 应用首字符
	    * @return
	    */
	   public String getFirstChar(){
		   if(firstChar == null){
			   firstChar = "";
		   }
		   return this.firstChar;
	   }
	   
	   public DataArrayList<BaseData> getAppList(){
		   return this.appList;
	   }
	   
	   public void clear()
	   {
		   firstChar="";
		   appList.clear();
	   }
}
