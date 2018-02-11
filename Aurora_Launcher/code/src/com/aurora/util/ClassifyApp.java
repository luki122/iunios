package com.aurora.util;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.aurora.launcher.ItemInfo;
import com.aurora.launcher.ShortcutInfo;

import android.content.Context;
import android.util.Log;

public class ClassifyApp {
	
	private static final boolean DEBUG = false;
	
	private List<String> CategoryaxisY;
	
	public ArrayList<ShortcutInfo> ListAppCategoryX; 
	
	public List<String> folderName = new ArrayList<String>();
	
	/**indicated that flag area*/
	private static int appClassifySign[][];
	
	public HashMap<String, List<ShortcutInfo>> folderList = new HashMap<String, List<ShortcutInfo>>();
	
	public ArrayList<clsfolderInfo> mfolderList = new ArrayList<clsfolderInfo>();
	
	/**mHashMapEntryList is after folderList sort*/
	private List<Map.Entry<String, List<ShortcutInfo>>> mHashMapEntryList;
	
	private ArrayList<ShortcutInfo>	gOtherList;
	
	public void setNeedClassifyList(ArrayList<ShortcutInfo> list){
		ListAppCategoryX  = list;
		initialAxisY();
	}
	
	public void setOtherListObject(ArrayList<ShortcutInfo>	OtherList){
		gOtherList = OtherList;
	}
	
	/**初始化Y轴数据*/
	private void initialAxisY(){
		HashSet<String> set = new HashSet<String>();
		for(ShortcutInfo info : ListAppCategoryX){
			if(info.cateName.contains(",")){
				String[] content = info.cateName.split(",");
				for(String str : content){
					set.add(str);
				}
			}else{
				set.add(info.cateName);
			}
		}
		CategoryaxisY  = new ArrayList(set);
		printContentAxisY();
	}
	
	/**输出Y轴列表数据*/
	private void printContentAxisY(){
		if(DEBUG){
			Log.e("linp", "CategoryaxisY.size()="+CategoryaxisY.size());
			for(String str : CategoryaxisY){
				Log.e("linp", "CategoryaxisY content= "+str);
			}
		}
	}
	
	/**输出X轴列表数据*/
	private void printContentAxisX(boolean PrintElements){
		if(DEBUG){
			Log.e("linp", "ListAppCategoryX.size()="+ListAppCategoryX.size());
			for(ShortcutInfo info : ListAppCategoryX){
				if(PrintElements)
					Log.e("linp", info.toString());
			}
		}
	}
	
	/**从X轴移除相关种类为空的APP不进入排序*/
	private void removeEmptyAppCategoryName(){
		for(ShortcutInfo info : ListAppCategoryX){
			String cateName = info.cateName;
			if(null == cateName || cateName.equals("")){
				ListAppCategoryX.remove(info);
			}
		}
	}
	
	/**开始分类数据*/
	public  void classifyAppCategory(){
		List<String> ListCateName = null;
		appClassifySign = new int[ListAppCategoryX.size()+1][CategoryaxisY.size()];
		
		removeEmptyAppCategoryName();
		printContentAxisX(false);
		
		//为中标的应用打上标记
		for(int i=0;i<ListAppCategoryX.size();i++){
			ListCateName = getCateName(ListAppCategoryX.get(i));
			for(int j=0; j<ListCateName.size();j++){
				Object obj = ListCateName.get(j);
				if(CategoryaxisY.contains(obj)){
					int y = CategoryaxisY.indexOf(obj);
					appClassifySign[i][y] = 1;
					appClassifySign[ListAppCategoryX.size()][y]=appClassifySign[ListAppCategoryX.size()][y]+appClassifySign[i][y]; // 累加标1的作为算count 值
				}
			}
		}
		 int maxval = getAppClassifyMaxVal(appClassifySign);
		 
		if (ListAppCategoryX.size() == 1) {
			gOtherList.add((ShortcutInfo)ListAppCategoryX.get(0));
		} else if (ListAppCategoryX.size() > 1) {
            int axisY = getAxisYFromMaxVal(maxval);
            List<ShortcutInfo> ListAppContent =  getFolderContent(axisY);
            getFolderName(axisY);
            getFolderNameAndContent(axisY);
            removeListAppCategoryX(ListAppContent);
            removeListCategoryaxisY(axisY);
            classifyAppCategory();
		}
	}
	
	/**从每个appCategoryInfo中取出CateName并做解析*/
	public List<String> getCateName(ShortcutInfo info){
		List<String> result = new ArrayList<String>();
		String cateName = info.cateName;
		if(cateName!=null){
			if(cateName.contains(",")){ //多个类别
				String[] content = cateName.split(",");
				for(String str : content){
					result.add(str);
				}
			}else{
				result.add(cateName);
			}
		}
		return result;
	}
	
	/** 
	 *获取最后一列的Count值并排序返回最大值。
	 */
	private int getAppClassifyMaxVal(int appClassifySign[][]){
		List<Integer> Count = new ArrayList<Integer>();
		int Max = 0;
		for (int i = 0; i < CategoryaxisY.size(); i++) {
			Count.add(appClassifySign[ListAppCategoryX.size()][i]);
		}
		if (Count.size() > 0) {
			Max = Count.get(0);
			for (int i = 0; i < Count.size(); i++) {
				if (Max < Count.get(i)) {
					Max = Count.get(i);
				}
			}
		}
		return Max;
	}
	
	/**Find which line will catch through max value 
	 * desc : 一个MAX值应只对应一列。后面改用sortMaxList是为防止一个MAX值对应多列
	 * */
	private int getAxisYFromMaxVal(int max) {
		int y = 0;
		List<String> sortMaxList = new ArrayList<String>();
		CollatorComparator comparator = new CollatorComparator();
		
		for (int i = 0; i < CategoryaxisY.size(); i++) {
			if (appClassifySign[ListAppCategoryX.size()][i] == max) {
				sortMaxList.add(CategoryaxisY.get(i));
			}
		}

		if (sortMaxList.size() > 1) {
			Collections.sort(sortMaxList, comparator);
		}
		y = CategoryaxisY.indexOf(sortMaxList.get(0));
		
		return y;
	}
	
	/**获取最大值文件夹的应用列表*/
	public List<ShortcutInfo> getFolderContent(int y){
		List<ShortcutInfo> listFolderContent = new ArrayList<ShortcutInfo>();
		for(int i=0;i<ListAppCategoryX.size();i++){
			if(appClassifySign[i][y] == 1){
				listFolderContent.add(ListAppCategoryX.get(i));
			}
		}
		return listFolderContent;
	}
	
	/**获取最大值文件夹的名字*/
	public List<String> getFolderName(int y){
		//if size will smaller than 1 will not add in folderName
		if(getFolderContent(y).size()>1){
			folderName.add(CategoryaxisY.get(y));
		}
		return folderName;
	}
	
	/**获取文件夹内容以及文件夹名字*/
	
	public  HashMap<String, List<ShortcutInfo>> getFolderNameAndContent(int y) {
		
		List<ShortcutInfo> listFolderContent = new ArrayList<ShortcutInfo>();
		for(int i=0;i<ListAppCategoryX.size();i++){
			if(appClassifySign[i][y] == 1){
				listFolderContent.add(ListAppCategoryX.get(i));
			}
		}
		//if size will smaller than 1 will not add in folderName
		if(listFolderContent.size()>1){
			String folderName = CategoryaxisY.get(y);
//			folderList.put(folderName, listFolderContent);
			clsfolderInfo info = new clsfolderInfo();
			info.folderName = folderName;
			info.folderContent = listFolderContent;
			mfolderList.add(info);
		}else{
			gOtherList.add(listFolderContent.get(0));
		}
		return folderList;
	}
	
	/**移除中标表应用*/
	public void removeListAppCategoryX(List<ShortcutInfo> appInfo){
		for(ShortcutInfo info : appInfo){
			if(ListAppCategoryX.contains(info)){
				ListAppCategoryX.remove(info);
			}
		}
	}
	
	/**移除已中标TAG*/
	public void removeListCategoryaxisY(int y){
		CategoryaxisY.remove(y);
	}
	
	
	/**比较器*/
	class CollatorComparator implements Comparator<String> {
		Collator collator = Collator.getInstance();

		@Override
		public int compare(String arg0, String arg1) {
			// TODO Auto-generated method stub
			CollationKey key1 = collator.getCollationKey(arg0);
			CollationKey key2 = collator.getCollationKey(arg1);
			return key1.compareTo(key2);
		}

	}

	public List<Map.Entry<String, List<ShortcutInfo>>> sortClassifyResultList(HashMap<String, List<ShortcutInfo>> item){
		mHashMapEntryList =  new ArrayList<Map.Entry<String, List<ShortcutInfo>>>(item.entrySet());
		Collections.sort(mHashMapEntryList, new Comparator<Map.Entry<String, List<ShortcutInfo>>>() {   
		    public int compare(Map.Entry<String, List<ShortcutInfo>> o1, Map.Entry<String, List<ShortcutInfo>> o2) {      
		    	Collator collator = Collator.getInstance();
		    	CollationKey key1 = collator.getCollationKey(o1.getKey());
				CollationKey key2 = collator.getCollationKey(o2.getKey());
		    	return  key1.compareTo(key2);
		    }
		}); 
		Collections.sort(folderName, new CollatorComparator());
		return 	mHashMapEntryList;
	}
	
	public void clear(){
		folderList.clear();
		folderName.clear();
		mfolderList.clear();
	}
	
	 public static class clsfolderInfo{
		public String folderName;
		public List<ShortcutInfo> folderContent;
	}
	
}
