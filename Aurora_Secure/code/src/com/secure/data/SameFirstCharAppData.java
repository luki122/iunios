package com.secure.data;

/**
 * 首字符相同的应用数据
 */
public class SameFirstCharAppData extends BaseData{

   private String firstChar;
   private MyArrayList<BaseData> appList;
   
   public SameFirstCharAppData(){
	   super(SameFirstCharAppData.class.getName());
	   appList = new MyArrayList<BaseData>();
   }
   
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
   
   public MyArrayList<BaseData> getAppList(){
	   return this.appList;
   }
}
