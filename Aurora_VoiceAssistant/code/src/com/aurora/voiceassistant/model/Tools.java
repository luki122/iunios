package com.aurora.voiceassistant.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Tools 
{
	public boolean saveImage(Bitmap bitmap,String path,String name)
	{
		File destDir = new File(path);
		if (!destDir.exists()) 
		{
			destDir.mkdirs();
		}

		
		File f = new File(path+name); 
		
		if(f.exists())
		{
			f.delete();
		}
		
		try 
		{
			f.createNewFile();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
			return false;
		}  
		
		FileOutputStream fOut = null;  
		try 
		{  
			fOut = new FileOutputStream(f);  
		} 
		catch (FileNotFoundException e) 
		{  
			e.printStackTrace();  
			return false;
		} 
		  
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);  
			     
		try 
		{  
			fOut.flush();  
		} 
		catch (IOException e) 
		{  
			e.printStackTrace();  
			return false;
		}  
		 
		try 
		{  
			fOut.close();  
		} 
		catch (IOException e) 
		{  
			e.printStackTrace();  
			return false;
		}  
		
		return true;
	}
	
	public Bitmap loadImage(String path)
	{
		Bitmap bitmap = null;
		
		FileInputStream fs = null;
		try 
		{
			fs = new FileInputStream(path);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		} 
		
        BufferedInputStream bs = new BufferedInputStream(fs); 
        
        bitmap = BitmapFactory.decodeStream(bs); 
        
        try 
        {
			bs.close();
			fs.close(); 
		} 
        catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		} 
        
		return bitmap;
	}
	
	public void delete(File file) 
	{  
	   if(!file.exists()) return;
	   
       if (file.isFile()) 
       {  
           file.delete();  
           return;  
       }  
 
       if(file.isDirectory())
       {  
           File[] childFiles = file.listFiles();  
           if (childFiles == null || childFiles.length == 0) 
           {  
               file.delete();  
               return;  
           }  
     
           for(int i = 0; i < childFiles.length; i++) 
           {  
               delete(childFiles[i]);  
           }  
           file.delete();  
       }  
   }
}
