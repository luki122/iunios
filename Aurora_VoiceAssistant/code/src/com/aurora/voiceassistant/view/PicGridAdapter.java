package com.aurora.voiceassistant.view;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.aurora.voiceassistant.model.*;
import com.aurora.voiceassistant.R;

public class PicGridAdapter extends BaseAdapter {
	 private Context context;  
	 private String tempPath;
   
     private ArrayList<RspXmlPicItem> list;  
     private LayoutInflater mInflater;  
     private final String  TAG = "VS-PG";
     
     public PicGridAdapter(Context context,ArrayList<RspXmlPicItem> list,String path) {  
         this.context = context;  
         this.list = list;
         this.tempPath = path;
         mInflater = LayoutInflater.from(context); 
         delTempPath();
     }  
   
     public int getCount() {  
         return list.size();  
     }  
   
     @Override  
     public Object getItem(int index) {
         return list.get(index);  
     }  
   
     @Override  
     public long getItemId(int index) {  
         return index;  
     }  
   
     @Override  
     public View getView(int index, View convertView, ViewGroup parent) {  
    	 RspXmlPicItem item = (RspXmlPicItem)list.get(index);
    	 
    	 String url = (String)item.getImagelink();
         GridHolder holder;  
         if (convertView == null) {     
        	 holder = new GridHolder(); 
             convertView = mInflater.inflate(R.layout.vs_image_layout, null);
             holder.image = (ImageView)convertView.findViewById(R.id.image);
             convertView.setTag(holder);
         } else {
             holder = (GridHolder) convertView.getTag();
         }
         
         Bitmap bitmap = item.getBitmap();
         
         /*String picFileName = null;
         Log.d("DEBUG", "the tempPath = "+tempPath);
         if (tempPath != null) {
        	 picFileName = "vs"+String.valueOf(index)+".png";
         }*/

         if (bitmap == null) {
//        	 Log.d("DEBUG", "getView--------------------------bitmap == null");
        	 /*if (picFileName != null) {
        		 Log.d("DEBUG", "getView--------------------------bitmap == null"+tempPath+picFileName);
        		 Bitmap bm = BitmapFactory.decodeFile(tempPath+picFileName);
            	 holder.image.setImageBitmap(bm);
        	 }*/
         } else {
//        	 Log.d("DEBUG", "getView--------------------------bitmap != null");
        	 holder.image.setImageBitmap(bitmap);
         }
        
         return convertView;  
     }  
     
     class GridHolder {  
         ImageView image;  
     } 
     
     private void delTempPath() {
    	 boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
    	 if(!sdCardExist) {
 			RkLog.e(TAG,"PicGridAdapter.delTempPath: sdcard exist!!");
    	 }
    	 File sdcardDir=Environment.getExternalStorageDirectory();
    	 String path = sdcardDir.toString()+"/"+tempPath+"/";
    	 File temppath = new File(path);
    	 new Tools().delete(temppath);
	 }
}
