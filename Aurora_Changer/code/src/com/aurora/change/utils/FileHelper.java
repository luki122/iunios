package com.aurora.change.utils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.aurora.change.receiver.ChangeReceiver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class FileHelper {
    private static final CommonLog log = LogFactory.createLog();
    private static final int FILE_BUFFER_SIZE = 51200;
    public static final String PATH_DATA_AURORA = "/data/aurora/";
    public static final String PATH_DATA_AURORA_CHANGE = "/data/aurora/change/";
    public static final String PATH_DATA_AURORA_CHANGE_LOCKSCREEN = "/data/aurora/change/lockscreen/";
	

    public static boolean fileIsExist(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            log.e("param invalid, filePath: " + filePath);
            return false;
        }

        File f = new File(filePath);
        if (!f.exists()) {
            return false;
        }
        return true;
    }

    public static InputStream readFile(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return null;
        }

        InputStream is = null;

        try {
            if (fileIsExist(filePath)) {
                File f = new File(filePath);
                is = new FileInputStream(f);
            } else {
                return null;
            }
        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
            return null;
        }
        return is;
    }

    public static boolean createDirectory(String filePath) {
        if (null == filePath) {
            return false;
        }

        File file = new File(filePath);

        if (file.exists()) {
            return true;
        }

        return file.mkdirs();

    }

    public static boolean deleteDirectory(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return false;
        }
		 // Aurora liugj 2014-10-13 modified for bug-8793 start
        File oldFile = new File(filePath);
        File file = new File(oldFile.getAbsolutePath() + System.currentTimeMillis());
        oldFile.renameTo(file);
		 // Aurora liugj 2014-10-13 modified for bug-8793 end
        if (file == null || !file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] list = file.listFiles();

            for (int i = 0; i < list.length; i++) {
                log.d("delete filePath: " + list[i].getAbsolutePath());
                if (list[i].isDirectory()) {
                    deleteDirectory(list[i].getAbsolutePath());
                } else {
                    list[i].delete();
                }
            }
        }

        log.d("delete filePath: " + file.getAbsolutePath());
        file.delete();
        return true;
    }

    public static boolean writeFile(String filePath, InputStream inputStream) {

        if (null == filePath || filePath.length() < 1) {
            log.e("Invalid param. filePath: " + filePath);
            return false;
        }

        try {
            File file = new File(filePath);
            if (file.exists()) {
                deleteDirectory(filePath);
            }

            String pth = filePath.substring(0, filePath.lastIndexOf("/"));
            boolean ret = createDirectory(pth);
            if (!ret) {
                log.e("createDirectory fail path = " + pth);
                return false;
            }

            boolean ret1 = file.createNewFile();
            if (!ret) {
                log.e("createNewFile fail filePath = " + filePath);
                return false;
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int c = inputStream.read(buf);
            while (-1 != c) {
                fileOutputStream.write(buf, 0, c);
                c = inputStream.read(buf);
            }

            fileOutputStream.flush();
            fileOutputStream.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public static boolean writeFile(String filePath, String fileContent) {
        return writeFile(filePath, fileContent, false);
    }

    public static boolean writeFile(String filePath, String fileContent, boolean append) {
        if (null == filePath || fileContent == null || filePath.length() < 1 || fileContent.length() < 1) {
            log.e("Invalid param. filePath: " + filePath + ", fileContent: " + fileContent);
            return false;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }

            BufferedWriter output = new BufferedWriter(new FileWriter(file, append));
            output.write(fileContent);
            output.flush();
            output.close();
        } catch (IOException ioe) {
            log.e("writeFile ioe: " + ioe.toString());
            return false;
        }

        return true;
    }

    public static long getFileSize(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return 0;
        }

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return 0;
        }

        return file.length();
    }

    public static long getFileModifyTime(String filePath) {
        if (null == filePath) {
            log.e("Invalid param. filePath: " + filePath);
            return 0;
        }

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return 0;
        }

        return file.lastModified();
    }

    public static boolean setFileModifyTime(String filePath, long modifyTime) {
        if (null == filePath || modifyTime < 0) {
            log.e("Invalid param. filePath: " + filePath);
            return false;
        }

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return false;
        }

        return file.setLastModified(modifyTime);
    }

    public static boolean copyFile(ContentResolver cr, String fromPath, String destUri) {
        if (null == cr || null == fromPath || fromPath.length() < 1 || null == destUri
                || destUri.length() < 1) {
            log.e("copyFile Invalid param. cr=" + cr + ", fromPath=" + fromPath + ", destUri=" + destUri);
            return false;
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(fromPath);
            if (null == is) {
                log.e("Failed to open inputStream: " + fromPath + "->" + destUri);
                return false;
            }

            // check output uri
            String path = null;
            Uri uri = null;

            String lwUri = destUri.toLowerCase();
            if (lwUri.startsWith("content://")) {
                uri = Uri.parse(destUri);
            } else if (lwUri.startsWith("file://")) {
                uri = Uri.parse(destUri);
                path = uri.getPath();
            } else {
                path = destUri;
            }

            // open output
            if (null != path) {
                File fl = new File(path);
                String pth = path.substring(0, path.lastIndexOf("/"));
                File pf = new File(pth);

                if (pf.exists() && !pf.isDirectory()) {
                    pf.delete();
                }

                pf = new File(pth + File.separator);

                if (!pf.exists()) {
                    if (!pf.mkdirs()) {
                        log.e("Can't make dirs, path=" + pth);
                    }
                }

                pf = new File(path);
                if (pf.exists()) {
                    if (pf.isDirectory())
                        deleteDirectory(path);
                    else
                        pf.delete();
                }

                os = new FileOutputStream(path);
                try {
    				long time = System.currentTimeMillis();
    				if (time > 0) {
    					fl.setLastModified(time);
    				}
    			} catch (Exception e) {
    			}
            } else {
                os = new ParcelFileDescriptor.AutoCloseOutputStream(cr.openFileDescriptor(uri, "w"));
            }

            // copy file
            byte[] dat = new byte[1024];
            int i = is.read(dat);
            while (-1 != i) {
                os.write(dat, 0, i);
                i = is.read(dat);
            }

            is.close();
            is = null;

            os.flush();
            os.close();
            os = null;

            return true;

        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception ex) {
                };
            }
            if (null != os) {
                try {
                    os.close();
                } catch (Exception ex) {
                };
            }
        }
        return false;
    }

	// Aurora liugj 2014-08-13 modified for copy file double protect start
    
    //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//    public static boolean copyFile(String fromPath, String toPath) {
	public static boolean copyFile(String fromPath, String toPath, Context mContext) {
	//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
		
		if (fromPath == null) {
			Log.d(ChangeReceiver.LOCK_TAG, "copyFile: fromPath = " + fromPath);
			return false;
		}
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(fromPath);
		} catch (FileNotFoundException e) {
			Log.d(ChangeReceiver.LOCK_TAG, fromPath + "-->copyFile: FileNotFoundException = " + e.toString());
			try {
				is = new FileInputStream(fromPath);
			} catch (Exception e1) {
				Log.d(ChangeReceiver.LOCK_TAG, fromPath + "--1-->copyFile: Exception = " + e1.toString());
			}
		}
		if (null == is) {
			Log.d(ChangeReceiver.LOCK_TAG, "copyFile: is==null -> " + fromPath);
			//log.e("Failed to open inputStream: " + fromPath + "->" + fromPath);
			return false;
		}
		// open output
		if (null != toPath) {
			File fl = new File(toPath);
			String pth = toPath.substring(0, toPath.lastIndexOf("/"));
			File pf = new File(pth);
			if (pf.exists() && !pf.isDirectory()) {
				pf.delete();
			}

			pf = new File(pth + File.separator);

			if (!pf.exists()) {
				if (!pf.mkdirs()) {
					log.e("Can't make dirs, path=" + pth);
				}
				CommonUtil.chmodFile(PATH_DATA_AURORA);
				CommonUtil.chmodFile(PATH_DATA_AURORA_CHANGE);
				CommonUtil.chmodFile(PATH_DATA_AURORA_CHANGE_LOCKSCREEN);
			}

			pf = new File(toPath);
			if (pf.exists()) {
				if (pf.isDirectory())
					deleteDirectory(toPath);
				else
					pf.delete();
			}

			try {
				os = new FileOutputStream(toPath);
			} catch (FileNotFoundException e) {
				Log.d(ChangeReceiver.LOCK_TAG, toPath + "-->copyFile: FileNotFoundException = " + e.toString());
				try {
					os = new FileOutputStream(toPath);
				} catch (Exception e2) {
					Log.d(ChangeReceiver.LOCK_TAG, toPath + "--2-->copyFile: Exception = " + e2.toString());
				}
			} catch (Exception e) {
				Log.d(ChangeReceiver.LOCK_TAG, "--3-->copyFile: Exception = " + e.toString());
			} 
			if (null == os) {
				if (null != is) {
					try {
						is.close();
						is = null;
					} catch (Exception ex) {
					}
				}
				Log.d(ChangeReceiver.LOCK_TAG, "copyFile: failed ");
				return false;
			}
			try {
				long time = System.currentTimeMillis();
				if (time > 0) {
					Log.d(ChangeReceiver.LOCK_TAG, fromPath+"-->copyFile: setLastModified = " + time);
					fl.setLastModified(time);
				}
			} catch (Exception e) {
			}
		}
		try {
			// copy file
			byte[] dat = new byte[1024];
			int i = is.read(dat);
			while (-1 != i) {
				os.write(dat, 0, i);
				i = is.read(dat);
			}
			
			boolean res = CommonUtil.chmodFile(toPath);
			
			Log.d(ChangeReceiver.LOCK_TAG, "copyFile: success = "+res);
			
			//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
			Intent mIntent = new Intent();
	    	mIntent.setAction("com.aurora.change.alarm.wallpaperupdate");
	    	mContext.sendBroadcast(mIntent);
			//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
			
			return res;

		} catch (Exception ex) {
			log.e("Exception, ex: " + ex.toString());
			try {
				// copy file
				byte[] dat = new byte[1024];
				int i = is.read(dat);
				while (-1 != i) {
					os.write(dat, 0, i);
					i = is.read(dat);
				}

				CommonUtil.chmodFile(toPath);
				Log.d(ChangeReceiver.LOCK_TAG, "2-->copyFile: success ");
				
				//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
				Intent mIntent = new Intent();
		    	mIntent.setAction("com.aurora.change.alarm.wallpaperupdate");
		    	mContext.sendBroadcast(mIntent);
				//M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
				
				return true;
			} catch (Exception e) {
				Log.d(ChangeReceiver.LOCK_TAG, "2-->e:" + e.toString());
			}finally {
				if (null != is) {
					try {
						is.close();
						is = null;
					} catch (Exception e1) {

					}
				}
				if (null != os) {
					try {
						os.flush();
						os.close();
						os = null;
					} catch (Exception e2) {
					}
				}
			}
		} finally {
			if (null != is) {
				try {
					is.close();
					is = null;
				} catch (Exception ex) {

				}
			}
			if (null != os) {
				try {
					os.flush();
					os.close();
					os = null;
				} catch (Exception ex) {
				}
			}
		}
		return false;
	}
	// Aurora liugj 2014-08-13 modified for copy file double protect end

    public static byte[] readAll(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        byte[] buf = new byte[1024];
        int c = is.read(buf);
        while (-1 != c) {
            baos.write(buf, 0, c);
            c = is.read(buf);
        }
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    public static byte[] readFile(Context ctx, Uri uri) {
        if (null == ctx || null == uri) {
            log.e("Invalid param. ctx: " + ctx + ", uri: " + uri);
            return null;
        }

        InputStream is = null;
        String scheme = uri.getScheme().toLowerCase();
        if (scheme.equals("file")) {
            is = readFile(uri.getPath());
        }

        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (null == is) {
                return null;
            }

            byte[] bret = readAll(is);
            is.close();
            is = null;

            return bret;
        } catch (FileNotFoundException fne) {
            log.e("FilNotFoundException, ex: " + fne.toString());
        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception ex) {
                };
            }
        }
        return null;
    }

    public static boolean writeFile(String filePath, byte[] content) {
        if (null == filePath || null == content) {
            log.e("Invalid param. filePath: " + filePath + ", content: " + content);
            return false;
        }

        FileOutputStream fos = null;
        try {
            String pth = filePath.substring(0, filePath.lastIndexOf("/"));
            File pf = null;
            pf = new File(pth);
            if (pf.exists() && !pf.isDirectory()) {
                pf.delete();
            }
            pf = new File(filePath);
            if (pf.exists()) {
                if (pf.isDirectory())
                    FileHelper.deleteDirectory(filePath);
                else
                    pf.delete();
            }

            pf = new File(pth + File.separator);
            if (!pf.exists()) {
                if (!pf.mkdirs()) {
                    log.e("Can't make dirs, path=" + pth);
                }
            }

            fos = new FileOutputStream(filePath);
            fos.write(content);
            fos.flush();
            fos.close();
            fos = null;
            
            try {
				long time = System.currentTimeMillis();
				if (time > 0) {
					pf.setLastModified(time);
				}
			} catch (Exception e) {
			}
            return true;

        } catch (Exception ex) {
            log.e("Exception, ex: " + ex.toString());
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (Exception ex) {
                };
            }
        }
        return false;
    }

    /************* ZIP file operation ***************/
    public static boolean readZipFile(String zipFileName, StringBuffer crc) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                long size = entry.getSize();
                crc.append(entry.getCrc() + ", size: " + size);
            }
            zis.close();
        } catch (Exception ex) {
            log.e("Exception: " + ex.toString());
            return false;
        }
        return true;
    }

    public static byte[] readGZipFile(String zipFileName) {
        if (fileIsExist(zipFileName)) {
            log.i("zipFileName: " + zipFileName);
            try {
                FileInputStream fin = new FileInputStream(zipFileName);
                int size;
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((size = fin.read(buffer, 0, buffer.length)) != -1) {
                    baos.write(buffer, 0, size);
                }
                return baos.toByteArray();
            } catch (Exception ex) {
                log.i("read zipRecorder file error");
            }
        }
        return null;
    }

    public static boolean zipFile(String baseDirName, String fileName, String targerFileName)
            throws IOException {
        if (baseDirName == null || "".equals(baseDirName)) {
            return false;
        }
        File baseDir = new File(baseDirName);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return false;
        }

        String baseDirPath = baseDir.getAbsolutePath();
        File targerFile = new File(targerFileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targerFile));
        File file = new File(baseDir, fileName);

        boolean zipResult = false;
        if (file.isFile()) {
            zipResult = fileToZip(baseDirPath, file, out);
        } else {
            zipResult = dirToZip(baseDirPath, file, out);
        }
        out.close();
        return zipResult;
    }

    public static boolean unZipFile(String fileName, String unZipDir) throws Exception {
        File f = new File(unZipDir);

        if (!f.exists()) {
            f.mkdirs();
        }

        BufferedInputStream is = null;
        ZipEntry entry;
        ZipFile zipfile = new ZipFile(fileName);
        Enumeration<?> enumeration = zipfile.entries();
        byte data[] = new byte[FILE_BUFFER_SIZE];
        log.i("unZipDir: " + unZipDir);

        while (enumeration.hasMoreElements()) {
            entry = ( ZipEntry ) enumeration.nextElement();

            if (entry.isDirectory()) {
                File f1 = new File(unZipDir + "/" + entry.getName());
                log.i("entry.isDirectory XXX " + f1.getPath());
                if (!f1.exists()) {
                    f1.mkdirs();
                }
            } else {
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                String name = unZipDir + "/" + entry.getName();
                RandomAccessFile m_randFile = null;
                File file = new File(name);
                if (file.exists()) {
                    file.delete();
                }

                file.createNewFile();
                m_randFile = new RandomAccessFile(file, "rw");
                int begin = 0;

                while ((count = is.read(data, 0, FILE_BUFFER_SIZE)) != -1) {
                    try {
                        m_randFile.seek(begin);
                    } catch (Exception ex) {
                        log.e("exception, ex: " + ex.toString());
                    }

                    m_randFile.write(data, 0, count);
                    begin = begin + count;
                }

                file.delete();
                m_randFile.close();
                is.close();
            }
        }

        return true;
    }

    private static boolean fileToZip(String baseDirPath, File file, ZipOutputStream out) throws IOException {
        FileInputStream in = null;
        ZipEntry entry = null;

        byte[] buffer = new byte[FILE_BUFFER_SIZE];
        int bytes_read;
        try {
            in = new FileInputStream(file);
            entry = new ZipEntry(getEntryName(baseDirPath, file));
            out.putNextEntry(entry);

            while ((bytes_read = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytes_read);
            }
            out.closeEntry();
            in.close();
        } catch (IOException e) {
            log.e("Exception, ex: " + e.toString());
            return false;
        } finally {
            if (out != null) {
                out.closeEntry();
            }

            if (in != null) {
                in.close();
            }
        }
        return true;
    }

    private static boolean dirToZip(String baseDirPath, File dir, ZipOutputStream out) throws IOException {
        if (!dir.isDirectory()) {
            return false;
        }

        File[] files = dir.listFiles();
        if (files.length == 0) {
            ZipEntry entry = new ZipEntry(getEntryName(baseDirPath, dir));

            try {
                out.putNextEntry(entry);
                out.closeEntry();
            } catch (IOException e) {
                log.e("Exception, ex: " + e.toString());
            }
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileToZip(baseDirPath, files[i], out);
            } else {
                dirToZip(baseDirPath, files[i], out);
            }
        }
        return true;
    }

    private static String getEntryName(String baseDirPath, File file) {
        if (!baseDirPath.endsWith(File.separator)) {
            baseDirPath = baseDirPath + File.separator;
        }

        String filePath = file.getAbsolutePath();
        if (file.isDirectory()) {
            filePath = filePath + "/";
        }

        int index = filePath.indexOf(baseDirPath);
        return filePath.substring(index + baseDirPath.length());
    }

    public static boolean writeImage(Bitmap bitmap, String destPath, int quality) {
        try {
			  // Aurora liugj 2014-10-30 modified for bug-9442 start
            FileHelper.deleteDirectory(destPath);
			  // Aurora liugj 2014-10-30 modified for bug-9442 end
            if (FileHelper.createFile(destPath)) {
                FileOutputStream out = new FileOutputStream(destPath);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                    out.flush();
                    out.close();
                    out = null;
                    Log.d("Wallpaper_DEBUG", "LoadImageTask---------------doInBackground----writeImage----success = ");
                    return true;
                }
            }
        } catch (IOException e) {
        	Log.d(ChangeReceiver.LOCK_TAG, destPath + "-->writeImage: IOException = " + e.toString());
        	Log.d("Wallpaper_DEBUG", "LoadImageTask---------------doInBackground----writeImage----Exception = "+e);
            return false;
        } catch (Exception e) {
        	Log.d(ChangeReceiver.LOCK_TAG, destPath + "-->writeImage: Exception = " + e.toString());
        	Log.d("Wallpaper_DEBUG", "LoadImageTask---------------doInBackground----writeImage----Exception2 = "+e);
        	return false;
		}
        return false;
    }

    /**
     * 删除一个文件
     * 
     * @param filePath
     *            要删除的文件路径名
     * @return true if this file was deleted, false otherwise
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建一个文件，创建成功返回true
     * 
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                return file.createNewFile();
            }
        } catch (IOException e) {
			  Log.d(ChangeReceiver.LOCK_TAG, "createFile-->IOException: "+e.toString());
            e.printStackTrace();
        }
        return false;
    }
}
