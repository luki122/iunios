package com.secure.data;

import android.content.Intent;

/**
 * 应用类别 data
 */
public class AppCategoryData extends BaseData{

   private String categoryName;
   private Intent intent;
   private MyArrayList<BaseData> appList;
   private String curDefStartPackageName;//这个类别中，默认启动的软件的包名
   private String curDefStartApkName;//这个类别中，默认启动的软件的名字
   private String curDefStartClass;//这个类别中，默认启动的软件的类名
   
   public AppCategoryData(){
	   super("AppCategoryData");
   }
   
   /**
    * 应用类别名
    * @param categoryName
    */
   public void setCategoryName(String categoryName){
	   this.categoryName = categoryName;
   }
   
   /**
    * 应用类别名
    * @return
    */
   public String getCategoryName(){
	   if(categoryName == null){
		   categoryName = "";
	   }
	   return this.categoryName;
   }
   
   public void setIntent(Intent intent){
	   this.intent = intent;
   }
   
   public Intent getIntent(){
	   return this.intent;
   }
   
   public void setAppList(MyArrayList<BaseData> appList){
	   this.appList = appList;
   }
   
   public MyArrayList<BaseData> getAppList(){
	   return this.appList;
   }
   
   /**
    * 这个类别中，默认启动的软件的包名
    * @return
    */
   public String getCurDefStartPackageName(){
	   return this.curDefStartPackageName;
   }
   
   /**
    * 这个类别中，默认启动的软件的包名
    * @param curDefStartPackageName
    */
   public void setCurDefStartPackageName(String curDefStartPackageName){
	   this.curDefStartPackageName = curDefStartPackageName;
   }
   
   /**
    * 这个类别中，默认启动的软件的名字
    * @return
    */
   public String getCurDefStartApkName(){
	   return this.curDefStartApkName;
   }
   
   /**
    * 这个类别中，默认启动的软件的名字
    * @param curDefStartApkName
    */
   public void setCurDefStartApkName(String curDefStartApkName){
	   this.curDefStartApkName = curDefStartApkName;
   }  
   
   /**
    * 这个类别中，默认启动的软件的类名
    * @return
    */
   public String getCurDefStartClass(){
	   return this.curDefStartClass;
   }
   
   /**
    * 这个类别中，默认启动的软件的类名
    * @param curDefStartApkName
    */
   public void setCurDefStartClass(String curDefStartClass){
	   this.curDefStartClass = curDefStartClass;
   }
}
