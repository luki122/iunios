/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


public class Utils {
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    private static final String TAG = "ListenLog.Utils";
    private static final String MYTAG = "iht";
	@SuppressLint("SimpleDateFormat")
    public static String getCurrentDateAndTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
		return sdf.format(cal.getTime());
	}

	public static final String[] copyAssetsToStorage(Context context, String destAssetDirectory) {
		//create base directory
		File directoryFile = new File(destAssetDirectory);
		if (!directoryFile.isDirectory()) {
			directoryFile.mkdirs();
		}

		AssetManager assetManager = context.getAssets();
		String[] assetFiles = null;

		//get asset files
		try {
			assetFiles = assetManager.list("");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (0 == assetFiles.length) {
			return null;
		}

		int index = 0;
		String[] copiedFilePaths = new String[assetFiles.length];

		//copy asset files
		try {
			for (String filename : assetFiles) {
				InputStream in = assetManager.open(filename);
				String outputFilePath = destAssetDirectory + "/" + filename;
				OutputStream out = new FileOutputStream(outputFilePath);
				copyFile(in, out);

				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;

				copiedFilePaths[index++] = outputFilePath;
			}

			return copiedFilePaths;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	public static void copyExistedFileToNewPath(String oldPath, String newPath) {
		File oldpath = new File(oldPath);
		
		String oldPathString = oldPath;
		String newPathString = newPath;
		
		if (oldpath.isDirectory()) {
			oldPathString = oldPath + File.separator;
			newPathString = newPath + File.separator;
			
			new File(newPathString).mkdirs();
//			copyExistedFileToNewPath(oldPathString, newPathString);
		}
		
		File[] oldFiles = (new File(oldPathString)).listFiles();
		
		try {
			
			for (int i = 0; i < oldFiles.length; i++) {
				if (oldFiles[i].isFile()) {
					//new input stream and buffering this stream					
					FileInputStream input = new FileInputStream(oldFiles[i]);
					BufferedInputStream inBuff = new BufferedInputStream(input);
			  
			        //new output stream and buffering this stream
					FileOutputStream output = new FileOutputStream(new File(newPathString + oldFiles[i].getName()));
					BufferedOutputStream outBuff = new BufferedOutputStream(output);
			        
			        //buffering array
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = inBuff.read(b)) != -1) {
						outBuff.write(b, 0, len);
					}
			        //fresh the output stream
					outBuff.flush();
			        
			        //close input and output stream
					inBuff.close();
					outBuff.close();
					input.close();
					output.close();
					
					//delete this file
					oldFiles[i].delete();
					
				}
			}
			
			oldpath.delete();
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.v(MYTAG, "******************copyExistedFileToNewPath-----Exception e============ = "+e);
			e.printStackTrace();
		}
		
		
	}

	// ===========================
	// CONVERT JAVA TYPES TO BYTES
	// ===========================
	// returns a byte array of length 4
	public static byte[] intToByteArray(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) (i & 0x00FF);
		b[1] = (byte) ((i >> 8) & 0x000000FF);
		b[2] = (byte) ((i >> 16) & 0x000000FF);
		b[3] = (byte) ((i >> 24) & 0x000000FF);
		return b;
	}

	// convert a short to a byte array
	public static byte[] shortToByteArray(short data) {
		return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
	}

	public static ByteBuffer readByteBufferFromFile(String fileStr) throws IOException {
	    File file = new File(fileStr);
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        try {
            long longlength = raf.length();
            int length = (int) longlength;
            if (length != longlength) throw new IOException("File size >= 2 GB");

            byte[] data = new byte[length];
            raf.readFully(data);
            ByteBuffer bb = ByteBuffer.allocateDirect(data.length);
            bb.put(data);
            return bb;
        }
        finally {
            raf.close();
        }
    }

	public static void saveByteBufferToFile(ByteBuffer extendedSoundModel, String filePath) {
	    FileChannel channel;
	    try {
	        channel = new FileOutputStream(filePath, false).getChannel();
	        extendedSoundModel.flip();
	        channel.write(extendedSoundModel);
	        channel.close();
	    } catch (FileNotFoundException e) {
	        Log.e(TAG, "outputExtendedSoundModel: FileNotFound: " + e.getMessage());
	    } catch (IOException e) {
	        Log.e(TAG, "outputExtendedSoundModel: unable to write sound model: " + e.getMessage());
	    }
	}

	@SuppressWarnings("unused")
    public static ShortBuffer readWavFile(String fileStr) throws IOException {
        File file = new File(fileStr);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        int nHdrLen = 44; // RIFF Wav file header length
        if (null == raf) throw new IOException("readWavFile access " + file + " failed");
        try {
            // skip files header
            raf.skipBytes(nHdrLen);
            long longlength = raf.length();
            int length = (int) longlength;
            if (length != longlength) throw new IOException("File size >= 2 GB");
            Log.v(TAG, "readWavFile file length = " + length);
            length = (int) (longlength-nHdrLen);
            int nShorts = (length+1)/2;
            short[] data = new short[nShorts];

            // read bytes as a short taking endianness into account
            short tmpShrt;
            for (int i=0; i<nShorts; i++) {
                if (ByteOrder.LITTLE_ENDIAN == ByteOrder.nativeOrder() ) {
                    tmpShrt = raf.readShort();
                    // flip bytes
                    data[i] = (short)((tmpShrt & 0x00FF) << 8);
                    data[i] += ((tmpShrt & 0xFF00) >> 8);
                } else {
                    data[i] = raf.readShort();
                }
             }

            ShortBuffer sb = ShortBuffer.allocate(data.length);
            sb.put(data);
            return sb;
        }
        finally {
            raf.close();
        }
    }
	
	/**check application is running or not*/
	public static boolean isAppRunning(Context context,String packageName){
	    ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
	    for(RunningAppProcessInfo rapi : infos){
	        if(rapi.processName.equals(packageName))
	            return true;
	        }
	    return false;
	}
	
	
	/**read the file to check enable/disable wakeup Listen*/
	public static boolean readFile(String path, String key){
		boolean bool = false;
		File file = new File(path);
		if(file.exists()){
			String res ="";
			BufferedReader bread = null;
			try {
				bread = new BufferedReader(new FileReader(file));
				while( (res = bread.readLine()) != null){
					if(res.contains(key)){
						bool = Boolean.valueOf(res.split(":")[1].toString());
						break;
					}
				}
				bread.close(); 
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}finally{
				try {
					if(bread != null){
						bread.close();
					}
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
			return bool;
		}else{
			return bool;
		}
	}

	//关键字列表（关键字文件名称列表）
	public static String[] getKeywordList() {
        File dir = new File(Global.APP_PATH);
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(Global.SOUND_MODEL_FILE_EXT);
            }
        });

        List<String> keywordArrayList = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            String soundModelFileName = files[i].getName();
            String keywordName = "";
            if (soundModelFileName.contains("_")) {
                keywordName = soundModelFileName.substring(0, soundModelFileName.lastIndexOf('_'));
            } else {
                keywordName = soundModelFileName.substring(0, soundModelFileName.lastIndexOf('.'));
            }
            if (keywordArrayList.contains(keywordName) == false) {
                keywordArrayList.add(keywordName);
            }
        }

        String[] keywordArray = keywordArrayList.toArray(new String[keywordArrayList.size()]);
        return keywordArray;
    }
	
}
