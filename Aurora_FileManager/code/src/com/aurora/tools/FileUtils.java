package com.aurora.tools;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.aurora.tools.OperationAction.Operation;

import libcore.io.Libcore;

/**
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>listing files and directories by filter and extension
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 * 
 * @version $Id: FileUtils.java 1349509 2012-06-12 20:39:23Z ggregory $
 */
public class FileUtils {

	private static final String TAG = "FileUtils";
	public static final int maxProgress = 100;

	public static int totalSize = 0;

	public static void initValues(int total) {
		totalSize = total;
		i = 0;
		oldCount = 0;
	}

	private boolean isPri;

	public boolean isPri() {
		return isPri;
	}

	public void setPri(boolean isPri) {
		this.isPri = isPri;
	}

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	public FileUtils() {
		super();
	}

	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final long ONE_KB = 1024;

	/**
	 * The number of bytes in a megabyte.
	 */
	public static final long ONE_MB = ONE_KB * ONE_KB;

	public static FileInfo copyFileToDirectory(File srcFile, File destDir,
			Handler handler) throws IOException {
		return copyFileToDirectory(srcFile, destDir, true, handler);
	}

	public static FileInfo copyFileToDirectory(File srcFile, File destDir,
			boolean preserveFileDate, Handler handler) throws IOException {
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir
					+ "' is not a directory");
		}
		File destFile = new File(destDir, srcFile.getName());
		return copyFile(srcFile, destFile, preserveFileDate, handler);
	}

	public static FileInfo copyFile(File srcFile, File destFile, Handler handler)
			throws IOException {
		return copyFile(srcFile, destFile, true, handler);
	}

	public static FileInfo copyFile(File srcFile, File destFile,
			boolean preserveFileDate, Handler handler) throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcFile.exists() == false) {
			throw new FileNotFoundException("Source '" + srcFile
					+ "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile
					+ "' exists but is a directory");
		}
		File parentFile = destFile.getParentFile();
		if (parentFile != null) {
			if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
				throw new IOException("Destination '" + parentFile
						+ "' directory cannot be created");
			}
		}
		return doCopyFile(srcFile, destFile, preserveFileDate, handler);
	}

	private static int oldCount = 0;
	private static int i = 0;
	// add by JXH 2014-7-9 begin
	private static boolean sigleFile = false;
	private static long sigleFileLength = 0;

	// add by JXH 2014-7-9 end

	private static FileInfo doCopyFile(File srcFile, File destFile,
			boolean preserveFileDate, Handler handler) throws IOException {
		if (destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile
					+ "' exists but is a directory");
		}
		if (destFile.exists()) {
			destFile = new File(Util.autoGenerateName(destFile));
		}
		// add by JXH 2014-7-9 begin
		if (totalSize == 1) {
			sigleFileLength = srcFile.length();
			sigleFile = true;
		} else {
			sigleFile = false;
			sigleFileLength = 0;
		}
		// add by JXH 2014-7-9 end

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			IOUtils.copy(fis, fos, handler, sigleFileLength);
			fos.getFD().sync();
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(fis);
		}
		if (!sigleFile&&handler!=null) {// &&OperationAction.getLastOperation() !=
							// Operation.cut
			i++;
			Message msg = handler.obtainMessage();
			msg.what = maxProgress;
			msg.arg1 = (int) ((Float.valueOf(i + 1) / Float.valueOf(totalSize)) * maxProgress);
			if (msg.arg1 > oldCount) {
				oldCount = msg.arg1;
				handler.sendMessage(msg);
			}
		}

		if (srcFile.length() != destFile.length()) {
			LogUtil.e(TAG, "Failed to copy full contents from '" + srcFile
					+ "' to '" + destFile + "'");
		}
		return Util.getFileInfo(destFile, false);
	}

	public static FileInfo copyDirectoryToDirectory(File srcDir, File destDir,
			Handler handler) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (srcDir.exists() && srcDir.isDirectory() == false) {
			throw new IllegalArgumentException("Source '" + destDir
					+ "' is not a directory");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir
					+ "' is not a directory");
		}
		return copyDirectory(srcDir, new File(destDir, srcDir.getName()), true,
				handler);
	}

	private static FileInfo copyDirectory(File srcDir, File destDir,
			Handler handler) throws IOException {
		return copyDirectory(srcDir, destDir, true, handler);
	}

	public static FileInfo copyDirectory(File srcDir, File destDir,
			boolean preserveFileDate, Handler handler) throws IOException {
		return copyDirectory(srcDir, destDir, null, preserveFileDate, handler);
	}

	public static void copyDirectory(File srcDir, File destDir,
			FileFilter filter, Handler handler) throws IOException {
		copyDirectory(srcDir, destDir, filter, true, handler);
	}

	public static FileInfo copyDirectory(File srcDir, File destDir,
			FileFilter filter, boolean preserveFileDate, Handler handler)
			throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcDir.exists() == false) {
			throw new FileNotFoundException("Source '" + srcDir
					+ "' does not exist");
		}
		if (srcDir.isDirectory() == false) {
			throw new IOException("Source '" + srcDir
					+ "' exists but is not a directory");
		}
		if (destDir.exists()) {
			destDir = new File(Util.autoGenerateName(destDir));
		}
//		LogUtil.d(
//				TAG,
//				"srcDir.getCanonicalPath()==" + srcDir.getCanonicalPath()
//						+ " destDir.getCanonicalPath()=="
//						+ destDir.getCanonicalPath());

		// Cater for destination being directory within the source directory
		// (see IO-141)
		List<String> exclusionList = null;
		if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
			File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir
					.listFiles(filter);
			if (srcFiles != null && srcFiles.length > 0) {
				exclusionList = new ArrayList<String>(srcFiles.length);
				for (File srcFile : srcFiles) {
					File copiedFile = new File(destDir, srcFile.getName());
					exclusionList.add(copiedFile.getCanonicalPath());
				}
			}
		}
		doCopyDirectory(srcDir, destDir, filter, preserveFileDate,
				exclusionList, handler);
		FileInfo fileInfo = Util.getFileInfo(destDir, false);
		return fileInfo;
	}

	private static void doCopyDirectory(File srcDir, File destDir,
			FileFilter filter, boolean preserveFileDate,
			List<String> exclusionList, Handler handler) throws IOException {
		// recurse
		File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir
				.listFiles(filter);
		if (srcFiles == null) { // null if abstract pathname does not denote a
								// directory, or if an I/O error occurs
			throw new IOException("Failed to list contents of " + srcDir);
		}
		if (destDir.exists()) {
			if (destDir.isDirectory() == false) {
				throw new IOException("Destination '" + destDir
						+ "' exists but is not a directory");
			}
		} else {
			if (!destDir.mkdirs() && !destDir.isDirectory()) {
				throw new IOException("Destination '" + destDir
						+ "' directory cannot be created");
			}
		}
		if (destDir.canWrite() == false) {
			LogUtil.e(TAG, "Destination '" + destDir
					+ "' cannot be written to");
			throw new IOException("Destination '" + destDir
					+ "' cannot be written to");
		}
		for (File srcFile : srcFiles) {// 复制 要复制目录下到子文件
			File dstFile = new File(destDir, srcFile.getName());
			if (exclusionList == null
					|| !exclusionList.contains(srcFile.getCanonicalPath())) {
				if (srcFile.isDirectory()) {
					doCopyDirectory(srcFile, dstFile, filter, preserveFileDate,
							exclusionList, handler);
				} else {
					doCopyFile(srcFile, dstFile, preserveFileDate, handler);
				}
			}
		}

	}

	public static void deleteDirectory(File directory, Handler handler)// del 4
			throws IOException {
		if (!directory.exists()) {
			return;
		}
		if (!isSymlink(directory)) {
			cleanDirectory(directory, handler);
		}
		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	public static void deleteDirectory(File directory)// del 4
			throws IOException {
		if (!directory.exists()) {
			return;
		}

		if (!isSymlink(directory)) {
			cleanDirectory(directory);
		}

		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	public static boolean deleteQuietly(File file, Handler handler) {// del 1
		if (file == null) {
			return false;
		}
		try {
			if (file.isDirectory()) {
				cleanDirectory(file, handler);
			}
		} catch (Exception ignored) {
			ignored.printStackTrace();
			LogUtil.e(TAG, ignored.getLocalizedMessage() == null ? ""
					: ignored.getLocalizedMessage());
		}
		boolean del;
		try {
			del = file.delete();
		} catch (Exception ignored) {
			ignored.printStackTrace();
			LogUtil.e(TAG, ignored.getLocalizedMessage() == null ? ""
					: ignored.getLocalizedMessage());
			return false;
		}
		if (OperationAction.getLastOperation() == Operation.del) {
			i++;
			Message msg = handler.obtainMessage();
			msg.what = maxProgress;
			msg.arg1 = (int) ((Float.valueOf(i + 1) / Float.valueOf(totalSize)) * maxProgress);
			if (msg.arg1 > oldCount) {
				oldCount = msg.arg1;
				handler.sendMessage(msg);
			}
		}
		return del;
	}

	public static boolean deleteQuietly(File file) {// del 1
		if (file == null) {
			return false;
		}
		try {
			if (file.isDirectory()) {
				cleanDirectory(file);
			}
		} catch (Exception ignored) {
			ignored.printStackTrace();
			LogUtil.e(TAG, ignored.getLocalizedMessage() == null ? ""
					: ignored.getLocalizedMessage());
		}
		boolean del;
		try {
			// del =false;
			del = file.delete();
			// del = delete(file.getAbsolutePath());
			// LogUtil.elog(TAG, "delete end del="+del);
		} catch (Exception ignored) {
			ignored.printStackTrace();
			LogUtil.e(TAG, ignored.getLocalizedMessage() == null ? ""
					: ignored.getLocalizedMessage());
			return false;
		}

		return del;
	}

	public static boolean delete(String path) {
		try {
			Libcore.os.remove(path);
			return true;
		} catch (Exception errnoException) {
			LogUtil.e(TAG,
					"errnoException==" + errnoException.getMessage());
			return false;
		}
	}

	public static void forceDelete(File file, Handler handler)// del 3
			throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file, handler);
		} else {
			boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: "
							+ file);
					// return;
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	public static void forceDelete(File file)// del 3
			throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					return;
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	public static void forceMkdir(File directory) throws IOException {
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				String message = "File " + directory + " exists and is "
						+ "not a directory. Unable to create directory.";
				throw new IOException(message);
			}
		} else {
			if (!directory.mkdirs()) {
				// Double-check that some other thread or process hasn't made
				// the directory in the background
				if (!directory.isDirectory()) {
					String message = "Unable to create directory " + directory;
					throw new IOException(message);
				}
			}
		}
	}

	public static long sizeOf(File file) {

		if (!file.exists()) {
			String message = file + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (file.isDirectory()) {
			return sizeOfDirectory(file);
		} else {
			return file.length();
		}

	}

	public static BigInteger sizeOfAsBigInteger(File file) {

		if (!file.exists()) {
			String message = file + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (file.isDirectory()) {
			return sizeOfDirectoryAsBigInteger(file);
		} else {
			return BigInteger.valueOf(file.length());
		}

	}

	public static long sizeOfDirectory(File directory) {
		checkDirectory(directory);

		final File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			return 0L;
		}
		long size = 0;

		for (final File file : files) {
			try {
				if (!isSymlink(file)) {
					size += sizeOf(file);
					if (size < 0) {
						break;
					}
				}
			} catch (IOException ioe) {
				// Ignore exceptions caught when asking if a File is a symlink.
			}
		}

		return size;
	}

	public static BigInteger sizeOfDirectoryAsBigInteger(File directory) {
		checkDirectory(directory);

		final File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			return BigInteger.ZERO;
		}
		BigInteger size = BigInteger.ZERO;

		for (final File file : files) {
			try {
				if (!isSymlink(file)) {
					size = size.add(BigInteger.valueOf(sizeOf(file)));
				}
			} catch (IOException ioe) {
				// Ignore exceptions caught when asking if a File is a symlink.
			}
		}

		return size;
	}

	private static void checkDirectory(File directory) {
		if (!directory.exists()) {
			throw new IllegalArgumentException(directory + " does not exist");
		}
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(directory
					+ " is not a directory");
		}
	}

	public static FileInfo moveDirectory(File srcDir, File destDir,
			Handler handler) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!srcDir.exists()) {
			throw new FileNotFoundException("Source '" + srcDir
					+ "' does not exist");
		}
		if (!srcDir.isDirectory()) {
			throw new IOException("Source '" + srcDir + "' is not a directory");
		}
		if (destDir.exists()) {
			destDir = new File(Util.autoGenerateName(destDir));//
		}
		boolean rename = srcDir.renameTo(destDir);//
		if (!rename) {// 不同存储器false
			LogUtil.e(TAG, "rename==" + rename);
			if (destDir.getCanonicalPath()
					.startsWith(srcDir.getCanonicalPath())) {
				Log.e(TAG, "Cannot move directory: " + srcDir
						+ " to a subdirectory of itself: " + destDir);
				throw new IOException("Cannot move directory: " + srcDir
						+ " to a subdirectory of itself: " + destDir);
			}
			copyDirectory(srcDir, destDir, handler);
			deleteDirectory(srcDir, handler);
			if (srcDir.exists()) {
				Log.e(TAG, "Failed to delete original directory '" + srcDir
						+ "' after copy to '" + destDir + "'");
				throw new IOException("Failed to delete original directory '"
						+ srcDir + "' after copy to '" + destDir + "'");
			}
		} else {
			if (handler!=null) {
				// add by Jxh 2014-8-12 begin
				LogUtil.e(TAG, "moveDirectory i==" + i + " oldCount=="
						+ oldCount + " totalSize==" + totalSize);
				i++;
				Message msg = handler.obtainMessage();
				msg.what = maxProgress;
				msg.arg1 = (int) ((Float.valueOf(i + 1) / Float
						.valueOf(totalSize)) * maxProgress);
				if (msg.arg1 > oldCount) {
					oldCount = msg.arg1;
					handler.sendMessage(msg);
				}
				// add by Jxh 2014-8-12 end
			}
		}
		FileInfo fileInfo = Util.getFileInfo(destDir, false);

		return fileInfo;
	}

	public static FileInfo moveDirectoryToDirectory(File src, File destDir,
			boolean createDestDir, Handler handler) throws IOException {
		if (src == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException(
					"Destination directory must not be null");
		}
		if (!destDir.exists() && createDestDir) {
			destDir.mkdirs();
		}
		if (!destDir.exists()) {
			throw new FileNotFoundException("Destination directory '" + destDir
					+ "' does not exist [createDestDir=" + createDestDir + "]");
		}
		if (!destDir.isDirectory()) {
			throw new IOException("Destination '" + destDir
					+ "' is not a directory");
		}
		return moveDirectory(src, new File(destDir, src.getName()), handler);

	}

	public static FileInfo moveFile(File srcFile, File destFile,
			Handler handler, boolean isPri) throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!srcFile.exists()) {
			throw new FileNotFoundException("Source '" + srcFile
					+ "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile + "' is a directory");
		}
		if (destFile.exists()) {
			destFile = new File(Util.autoGenerateName(destFile));
		}
		if (destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile
					+ "' is a directory");
		}
		FileInfo fileInfo = null;
		boolean rename = srcFile.renameTo(destFile);
		if (!rename) {
			fileInfo = copyFile(srcFile, destFile, handler);
			if (!srcFile.delete()) {
				FileUtils.deleteQuietly(destFile, handler);
				throw new IOException("Failed to delete original file '"
						+ srcFile + "' after copy to '" + destFile + "'");
			}
		} else {
		
			fileInfo = Util.getFileInfo(destFile, false);
		}
		if (handler!=null) {
			// LogUtil.elog(TAG, "moveFile i=="+i+" oldCount=="+oldCount);
			i++;
			Message msg = handler.obtainMessage();
			msg.what = maxProgress;
			msg.arg1 = (int) ((Float.valueOf(i + 1) / Float.valueOf(totalSize)) * maxProgress);
			if (msg.arg1 > oldCount) {
				oldCount = msg.arg1;
				handler.sendMessage(msg);
			}
		}
		return fileInfo;
	}

	public static FileInfo moveFileToDirectory(File srcFile, File destDir,
			boolean createDestDir, Handler handler, boolean isPri)
			throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException(
					"Destination directory must not be null");
		}
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		if (!destDir.isDirectory()) {
			throw new IOException("Destination '" + destDir
					+ "' is not a directory");
		}
		return moveFile(srcFile, new File(destDir, srcFile.getName()), handler,
				isPri);
	}

	public static FileInfo moveToDirectory(File src, File destDir,
			boolean createDestDir, Handler handler, boolean isPri)
			throws IOException {
		if (src == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!src.exists()) {
			throw new FileNotFoundException("Source '" + src
					+ "' does not exist");
		}
		if (handler == null) {
			throw new NullPointerException("handler must not be null");
		}
		if (src.isDirectory()) {
			return moveDirectoryToDirectory(src, destDir, createDestDir,
					handler);
		} else {
			return moveFileToDirectory(src, destDir, createDestDir, handler,
					isPri);
		}
	}

	public static boolean isSymlink(File file) throws IOException {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		} else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}

		if (fileInCanonicalDir.getCanonicalFile().equals(
				fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		} else {
			return true;
		}
	}

	public static Thread thread;

	public static boolean cleanDirectory(File directory, Handler handler) {// del
		if (directory == null || !directory.exists() || handler == null) {
			return false;
		}

		if (!directory.isDirectory()) {
			return false;
		}

		File[] files = directory.listFiles();

		IOException exception = null;
		for (File file : files) {
			if (thread != null && thread.isInterrupted()) {
				LogUtil.e(TAG, "cleanDirectory thread.isInterrupted()");
				return false;
			}
			try {
				forceDelete(file, handler);
			} catch (IOException ioe) {
				exception = ioe;
			}
			if ((OperationAction.getLastOperation() == Operation.del)&&handler!=null) {
				i++;
				Message msg = handler.obtainMessage();
				msg.what = maxProgress;
				msg.arg1 = (int) ((Float.valueOf(i + 1) / Float
						.valueOf(totalSize)) * maxProgress);
				if (msg.arg1 > oldCount) {
					oldCount = msg.arg1;
					handler.sendMessage(msg);
				}
			}
		}

		if (null != exception) {
			LogUtil.e(TAG, "del error "+exception.getMessage());
			return false;
		}
		return true;
	}

	public static boolean cleanDirectory(File directory) {// del
		if (directory == null || !directory.exists()) {
			return false;
		}

		if (!directory.isDirectory()) {
			return false;
		}

		File[] files = directory.listFiles();

		IOException exception = null;
		for (File file : files) {
			if (thread != null && thread.isInterrupted()) {
				LogUtil.e(TAG, "cleanDirectory thread.isInterrupted()");
				return false;
			}
			try {
				forceDelete(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			Log.e(TAG, exception.getMessage());
			return false;
		}
		return true;
	}


	public static boolean changeFile(String file) throws Exception {
		int len = 8;
		// 创建一个随机读写文件对象
		java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw");
		long totalLen = raf.length();
//		LogUtil.log(TAG, "文件总长字节是: " + totalLen+" path=="+file);
		// 打开一个文件通道
		java.nio.channels.FileChannel channel = raf.getChannel();
		// 映射文件中的某一部分数据以读写模式到内存中
		java.nio.MappedByteBuffer buffer = channel.map(
				FileChannel.MapMode.READ_WRITE, 0, len);
		// 示例修改字节
		for (int i = 0; i < len; i++) {
			byte src = buffer.get(i);
			buffer.put(i, (byte) (src ^ 2));// 修改Buffer中映射的字节的值
//			LogUtil.log(TAG, "被改为大写的原始字节是:" + src);
		}
		buffer.force();// 强制输出,在buffer中的改动生效到文件
		buffer.clear();
		channel.close();
		raf.close();
		return true;
	}

}
