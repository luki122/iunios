/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.android.browser;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.BrowserContract;
import android.provider.BrowserContract.History;
import android.util.Log;
import android.webkit.WebIconDatabase;

import com.android.browser.provider.BrowserProvider2.Thumbnails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataController {
    private static final String LOGTAG = "DataController";
    // Message IDs
    private static final int HISTORY_UPDATE_VISITED = 100;
    private static final int HISTORY_UPDATE_TITLE = 101;
    private static final int QUERY_URL_IS_BOOKMARK = 200;
    protected static final int TAB_LOAD_THUMBNAIL = 201;
    protected static final int TAB_SAVE_THUMBNAIL_BY_DATABASE = 202;
    protected static final int TAB_DELETE_THUMBNAIL = 203;
    protected static final int TAB_SAVE_THUMBNAIL_BY_FILE = 204;
    
    private static final int MAX_HISTORY_COUNT = 500;
    
    /* truncate this many history items at a time */
    private static final int TRUNCATE_N_OLDEST = 1;
    //private static DataController sInstance;
    private static HashMap<String, DataController> sInstanceMap = new HashMap<String, DataController>();

    private Context mContext;
    protected DataControllerHandler mDataHandler;
    private Handler mCbHandler; // To respond on the UI thread
    private ByteBuffer mBuffer; // to capture thumbnails

    /* package */ static interface OnQueryUrlIsBookmark {
        void onQueryUrlIsBookmark(String url, boolean isBookmark);
    }
    private static class CallbackContainer {
        Object replyTo;
        Object[] args;
    }

    private static class DCMessage {
        int what;
        Object obj;
        Object replyTo;
        DCMessage(int w, Object o) {
            what = w;
            obj = o;
        }
    }

    /* package */ public static DataController getInstance(Context c, String className) {
    	DataController dc = null;
    	if(sInstanceMap.get(className) == null) {
    		try {
				dc  = (DataController)Class.forName(className).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
    		dc.initInstance(c);
    		sInstanceMap.put(className, dc);
    	}
    	return sInstanceMap.get(className);
    }
    
    /**
     * 
     * Vulcan created this method in 2015年4月1日 下午3:24:22 .
     * @param c
     */
    protected void initInstance(Context c) {
        mContext = c.getApplicationContext();
        mDataHandler = getDefaultDataControllerHandler();
        mDataHandler.start();
        mCbHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                CallbackContainer cc = (CallbackContainer) msg.obj;
                switch (msg.what) {
                    case QUERY_URL_IS_BOOKMARK: {
                        OnQueryUrlIsBookmark cb = (OnQueryUrlIsBookmark) cc.replyTo;
                        String url = (String) cc.args[0];
                        boolean isBookmark = (Boolean) cc.args[1];
                        cb.onQueryUrlIsBookmark(url, isBookmark);
                        break;
                    }
                }
            }
        };
        return;
    }
    
    /**
     * 
     * Vulcan created this method in 2015年4月1日 下午3:35:30 .
     * @return
     */
    protected DataControllerHandler getDefaultDataControllerHandler() {
    	return new DataControllerHandler();
    }

    protected DataController() {
    }

    public void updateVisitedHistory(String url) {
        mDataHandler.sendMessage(HISTORY_UPDATE_VISITED, url);
    }

    public void updateHistoryTitle(String url, String title) {
        mDataHandler.sendMessage(HISTORY_UPDATE_TITLE, new String[] { url, title });
    }

    public void queryBookmarkStatus(String url, OnQueryUrlIsBookmark replyTo) {
        if (url == null || url.trim().length() == 0) {
            // null or empty url is never a bookmark
            replyTo.onQueryUrlIsBookmark(url, false);
            return;
        }
        mDataHandler.sendMessage(QUERY_URL_IS_BOOKMARK, url.trim(), replyTo);
    }

    public void loadThumbnail(Tab tab) {
        mDataHandler.sendMessage(TAB_LOAD_THUMBNAIL, tab);
    }

    public void deleteThumbnail(Tab tab) {
    	Log.d(LOGTAG,"deleteThumbnail: sendMessage, id = " + tab.getId());
        mDataHandler.sendMessage(TAB_DELETE_THUMBNAIL, tab.getId());
    }

    public void saveThumbnail(Tab tab) {
        mDataHandler.sendMessage(TAB_SAVE_THUMBNAIL_BY_DATABASE, tab);
    }

    // The standard Handler and Message classes don't allow the queue manipulation
    // we want (such as peeking). So we use our own queue.
    public class DataControllerHandler extends Thread {
    	public static final int MEDIUM_TYPE_DATABASE = 0;
    	public static final int MEDIUM_TYPE_FILE = 1;
    	
    	
        private BlockingQueue<DCMessage> mMessageQueue
                = new LinkedBlockingQueue<DCMessage>();

        public DataControllerHandler() {
            super("DataControllerHandler");
        }

        @Override
        public void run() {
            setPriority(Thread.MIN_PRIORITY);
            while (true) {
                try {
                    handleMessage(mMessageQueue.take());
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }

        public void sendMessage(int what, Object obj) {
            DCMessage m = new DCMessage(what, obj);
            mMessageQueue.add(m);
        }

        void sendMessage(int what, Object obj, Object replyTo) {
            DCMessage m = new DCMessage(what, obj);
            m.replyTo = replyTo;
            mMessageQueue.add(m);
        }

        private void handleMessage(DCMessage msg) {
            switch (msg.what) {
            case HISTORY_UPDATE_VISITED:
                doUpdateVisitedHistory((String) msg.obj);
                break;
            case HISTORY_UPDATE_TITLE:
                String[] args = (String[]) msg.obj;
                doUpdateHistoryTitle(args[0], args[1]);
                break;
            case QUERY_URL_IS_BOOKMARK:
                // TODO: Look for identical messages in the queue and remove them
                // TODO: Also, look for partial matches and merge them (such as
                //       multiple callbacks querying the same URL)
                doQueryBookmarkStatus((String) msg.obj, msg.replyTo);
                break;
            case TAB_LOAD_THUMBNAIL:
                doLoadThumbnail((Tab) msg.obj);
                break;
            case TAB_DELETE_THUMBNAIL:
            	deleteThumbnail((Long)msg.obj);
                break;
            case TAB_SAVE_THUMBNAIL_BY_DATABASE:
                doSaveThumbnail((Tab)msg.obj, MEDIUM_TYPE_DATABASE);
                break;
                
            case TAB_SAVE_THUMBNAIL_BY_FILE:
            	doSaveThumbnail((Tab)msg.obj, MEDIUM_TYPE_FILE);
            	break;
            }
        }

        private byte[] getCaptureBlob(Tab tab) {
            synchronized (tab) {
                Bitmap capture = tab.getScreenshot();
                if (capture == null) {
                    return null;
                }
                if (mBuffer == null || mBuffer.limit() < capture.getByteCount()) {
                    mBuffer = ByteBuffer.allocate(capture.getByteCount());
                }
                capture.copyPixelsToBuffer(mBuffer);
                mBuffer.rewind();
                return mBuffer.array();
            }
        }
        
        /**
         * 
         * Vulcan created this method in 2015年4月1日 下午4:51:33 .
         * @param ctx
         * @return
         */
        private String getThumbnailPathStr(Context ctx, String fileName) {
        	String strAppPath = ctx.getFilesDir().getAbsoluteFile().getPath();
        	String strThumbnailPath = strAppPath + 
        					File.separator + 
        					"thumbnail" + 
        					File.separator +
        					fileName;
        	
        	Log.d(LOGTAG,"getThumbnailPathStr: strThumbnailPath = " + strThumbnailPath);
        	return strThumbnailPath;
        }
        
        /**
         * 
         * Vulcan created this method in 2015年4月1日 下午4:22:13 .
         */
        private boolean writeThumbnailFile(Context ctx, byte[] blob, String fileName) {
        	String strThumbnailPath = getThumbnailPathStr(ctx, fileName);
        	
        	File fileThumbnail = new File(strThumbnailPath);
        	
        	//delete the file with the same name
        	if(fileThumbnail.getParentFile().exists()
        		&& fileThumbnail.getParentFile().isFile()) {
        		Log.d(LOGTAG,"writeThumbnailFile: fileThumbnail.getParentFile().isFile() true");
        		boolean successDelete = fileThumbnail.getParentFile().delete();
        		if(!successDelete) {
        			Log.d(LOGTAG,"writeThumbnailFile: fail: file of thumbnail can't be deleted!!!");
        			return false;
        		}
        	}
        	
        	//mkdirs
        	if(!fileThumbnail.getParentFile().exists()) {
        		Log.d(LOGTAG,"writeThumbnailFile: fileThumbnail.getParentFile().exists() false");
        		boolean successMkdirs = fileThumbnail.getParentFile().mkdirs();
        		if(!successMkdirs) {
        			Log.d(LOGTAG,"writeThumbnailFile: error on mkdirs");
        			return false;
        		}
        	}
        	
        	//write file
    		try {
	        	FileOutputStream out=new FileOutputStream(strThumbnailPath);
	        	out.write(blob);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        	
        	return true;
        }
        
  
        /**
         * 
         * Vulcan created this method in 2015年4月1日 下午3:44:24 .
         * @param tab
         */
        private void doSaveThumbnailByFile(Tab tab) {
        	byte[] blob = getCaptureBlob(tab);
        	
        	if(blob == null) {
        		return;
        	}
        	
        	String fileName = Long.toString(tab.getId());
        	String strThumbnailPath = getThumbnailPathStr(mContext, fileName);
        	writeThumbnailFile(mContext, blob, fileName);

            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Thumbnails._ID, tab.getId());
            values.put(Thumbnails.MEDIUMTYPE, MEDIUM_TYPE_FILE);
            values.put(Thumbnails.THUMBNAIL, strThumbnailPath);
            cr.insert(Thumbnails.CONTENT_URI, values);

        	return;
        }
        
        /**
         * 
         * Vulcan created this method in 2015年4月1日 下午5:34:33 .
         * @param tab
         */
        private void doSaveThumbnailByDatabase(Tab tab) {
            byte[] blob = getCaptureBlob(tab);
            if (blob == null) {
                return;
            }
            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Thumbnails._ID, tab.getId());
            values.put(Thumbnails.MEDIUMTYPE, MEDIUM_TYPE_DATABASE);
            values.put(Thumbnails.THUMBNAIL, blob);
            cr.insert(Thumbnails.CONTENT_URI, values);
            //Log.d(LOGTAG,"doSaveThumbnailByDatabase:cr.insert, id = " + tab.getId());
        }

        /**
         * 
         * Vulcan created this method in 2015年4月1日 下午3:45:15 .
         * @param tab
         */
        private void doSaveThumbnail(Tab tab, int mediumType) {
        	if(MEDIUM_TYPE_DATABASE == mediumType) {
        		doSaveThumbnailByDatabase(tab);
        	}
        	else if(MEDIUM_TYPE_FILE == mediumType){
        		doSaveThumbnailByFile(tab);
        	}
        	return;
        }
        
        /**
         * 
         * Vulcan created this method in 2015年4月2日 下午5:22:00 .
         * @param id
         */
        private void deleteThumbnail(long id) {
            ContentResolver cr = mContext.getContentResolver();

            //try to delete associated file.
            Cursor c = null;
            try {
            	Log.d(LOGTAG,"deleteThumbnail: try to look for record, id = " + id);
                Uri uri = ContentUris.withAppendedId(Thumbnails.CONTENT_URI, id);
                c = cr.query(uri, new String[] {Thumbnails._ID,
                		Thumbnails.MEDIUMTYPE,
                		Thumbnails.THUMBNAIL}, null, null, null);
                if (c.moveToFirst()) {
                	int mediumType = c.getInt(1);
                	if(MEDIUM_TYPE_FILE == mediumType) {
                		String strFile = c.getString(2);
                		File file = new File(strFile);
                		file.delete();
                		Log.d(LOGTAG,"deleteThumbnail: associated file is removed!");
                	}
                }
            } finally {
                if (c != null) {
                    c.close();
                    Log.d(LOGTAG,"deleteThumbnail: cursor is closed");
                }
            }

            //try to delete record in database.
            try {
                int n = cr.delete(ContentUris.withAppendedId(
                        Thumbnails.CONTENT_URI, id),
                        null, null);
                Log.d(LOGTAG,"deleteThumbnail: number of removed record is " + n);
            } catch (Throwable t) {
            	Log.d(LOGTAG,"deleteThumbnail: exception happens when cr.delete(), id = " + id);
            }
        }
        
        /**
         * 
         * Vulcan created this method in 2015年4月2日 下午1:24:45 .
         * @param fileName
         * @return
         */
        private byte[] readThumbnailFile(String fileFullPath) {
        	final String strThumbnailPath = "/data/data/com.android.browser/files/thumbnail/1";
        	FileInputStream inStream = null;
    		try {
	        	inStream=new FileInputStream(strThumbnailPath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
    		
    		byte[] byteBuf = null;
			try {	
				final int bufSize = inStream.available();
				byteBuf = new byte[bufSize];
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
			
			int dataSize = 0;
    		try {
    			dataSize = inStream.read(byteBuf);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
    		
			Log.d(LOGTAG,
    				String.format("readThumbnailFile: dataSize =%d,byteBuf.length=%d",
    						dataSize,byteBuf.length));
    		
        	return byteBuf;
        }

        private void doLoadThumbnail(Tab tab) {
            ContentResolver cr = mContext.getContentResolver();
            Cursor c = null;
            try {
                Uri uri = ContentUris.withAppendedId(Thumbnails.CONTENT_URI, tab.getId());
                c = cr.query(uri, new String[] {Thumbnails._ID,
                		Thumbnails.MEDIUMTYPE,
                        Thumbnails.THUMBNAIL}, null, null, null);
                if (c.moveToFirst()) {
                	int mediumType = c.getInt(1);
                	if(MEDIUM_TYPE_DATABASE == mediumType) {
                        byte[] data = c.getBlob(2);
                        if (data != null && data.length > 0) {
                            tab.updateCaptureFromBlob(data);
                        }
                	}
                	else if(MEDIUM_TYPE_FILE == mediumType) {
                        byte[] data = null;
                        String fileName = c.getString(2);
                        data = readThumbnailFile(fileName);
                        if (data != null && data.length > 0) {
                            tab.updateCaptureFromBlob(data);
                        }
                	}

                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        private void doUpdateVisitedHistory(String url) {
            ContentResolver cr = mContext.getContentResolver();
            Cursor c = null;
            try {
                c = cr.query(History.CONTENT_URI, new String[] { History._ID, History.VISITS },
                        History.URL + "=?", new String[] { url }, null);
                if (c.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(History.VISITS, c.getInt(1) + 1);
                    values.put(History.DATE_LAST_VISITED, System.currentTimeMillis());
                    cr.update(ContentUris.withAppendedId(History.CONTENT_URI, c.getLong(0)),
                            values, null, null);
                } else {
//                    android.provider.Browser.truncateHistory(cr);
                	truncateHistory(cr);
                    ContentValues values = new ContentValues();
                    values.put(History.URL, url);
                    values.put(History.VISITS, 1);
                    values.put(History.DATE_LAST_VISITED, System.currentTimeMillis());
                    values.put(History.TITLE, url);
                    values.put(History.DATE_CREATED, 0);
                    values.put(History.USER_ENTERED, 0);
                    cr.insert(History.CONTENT_URI, values);
                }
            } finally {
                if (c != null) c.close();
            }
        }
        
        /**
         * If there are more than MAX_HISTORY_COUNT non-bookmark history
         * items in the bookmark/history table, delete TRUNCATE_N_OLDEST
         * of them.  This is used to keep our history table to a
         * reasonable size.  Note: it does not prune bookmarks.  If the
         * user wants 1000 bookmarks, the user gets 1000 bookmarks.
         *  Requires {@link android.Manifest.permission#READ_HISTORY_BOOKMARKS}
         *  Requires {@link android.Manifest.permission#WRITE_HISTORY_BOOKMARKS}
         *
         * @param cr The ContentResolver used to access the database.
         */
        private final void truncateHistory(ContentResolver cr) {
            // TODO make a single request to the provider to do this in a single transaction
            Cursor cursor = null;
            try {
                
                // Select non-bookmark history, ordered by date
                cursor = cr.query(History.CONTENT_URI,
                        new String[] { History._ID, History.URL, History.DATE_LAST_VISITED },
                        null, null, History.DATE_LAST_VISITED + " ASC");

                if (cursor.moveToFirst() && cursor.getCount() >= MAX_HISTORY_COUNT) {
                    final WebIconDatabase iconDb = WebIconDatabase.getInstance();
                    /* eliminate oldest history items */
                    for (int i = 0; i < TRUNCATE_N_OLDEST; i++) {
                        cr.delete(ContentUris.withAppendedId(History.CONTENT_URI, cursor.getLong(0)),
                                null, null);
                        iconDb.releaseIconForPageUrl(cursor.getString(1));
                        if (!cursor.moveToNext()) break;
                    }
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "truncateHistory", e);
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        private void doQueryBookmarkStatus(String url, Object replyTo) {
            // Check to see if the site is bookmarked
            Cursor cursor = null;
            boolean isBookmark = false;
            try {
                cursor = mContext.getContentResolver().query(
                        BookmarkUtils.getBookmarksUri(mContext),
                        new String[] { BrowserContract.Bookmarks.URL },
                        BrowserContract.Bookmarks.URL + " == ?",
                        new String[] { url },
                        null);
                isBookmark = cursor.moveToFirst();
            } catch (SQLiteException e) {
                Log.e(LOGTAG, "Error checking for bookmark: " + e);
            } finally {
                if (cursor != null) cursor.close();
            }
            CallbackContainer cc = new CallbackContainer();
            cc.replyTo = replyTo;
            cc.args = new Object[] { url, isBookmark };
            mCbHandler.obtainMessage(QUERY_URL_IS_BOOKMARK, cc).sendToTarget();
        }

        private void doUpdateHistoryTitle(String url, String title) {
            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(History.TITLE, title);
            cr.update(History.CONTENT_URI, values, History.URL + "=?",
                    new String[] { url });
        }
    }
}
