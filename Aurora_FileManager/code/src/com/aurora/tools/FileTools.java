package com.aurora.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.aurora.tools.OperationAction.Operation;

import libcore.io.Libcore;

public class FileTools {

	private static final String TAG = "FileTools";
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final int EOF = -1;
	private static Thread thread;
	public static final int maxProgress = 100;
	private static int i = 0;
	private static char separatorChar;

	static {
		separatorChar = System.getProperty("file.separator", "/").charAt(0);
	}

	/*********************** 复制开始 ************************************/
	public static FileInfo copyAllToDirectory(File srcDir, File destDir,
			Handler handler) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir
					+ "' is not a directory");
		}
		if (srcDir.isDirectory()) {
			return copyDirectoryToDirectory(srcDir, destDir, handler);
		} else {
			return copyFileToDirectory(srcDir, destDir, handler);
		}
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
		return copyDirectory(srcDir, new File(destDir, srcDir.getName()), null,
				handler);
	}

	public static FileInfo copyDirectory(File srcDir, File destDir,
			FileFilter filter, Handler handler) throws IOException {
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
		doCopyDirectory(srcDir, destDir, filter, exclusionList, handler);
		FileInfo fileInfo = Util.getFileInfo(destDir, false);
		return fileInfo;
	}

	private static void doCopyDirectory(File srcDir, File destDir,
			FileFilter filter, List<String> exclusionList, Handler handler)
			throws IOException {
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
			throw new IOException("Destination '" + destDir
					+ "' cannot be written to");
		}
		for (File srcFile : srcFiles) {// 复制 要复制目录下到子文件
			File dstFile = new File(destDir, srcFile.getName());
			if (exclusionList == null
					|| !exclusionList.contains(srcFile.getCanonicalPath())) {
				if (srcFile.isDirectory()) {
					doCopyDirectory(srcFile, dstFile, filter, exclusionList,
							handler);
				} else {
					doCopyFile(srcFile, dstFile, handler);
				}
			}
		}

	}

	public static FileInfo copyFileToDirectory(File srcFile, File destDir,
			Handler handler) throws IOException {
		File destFile = new File(destDir, srcFile.getName());
		return copyFile(srcFile, destFile, handler);

	}

	private static FileInfo copyFile(File srcFile, File destFile,
			Handler handler) throws IOException {
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
		return doCopyFile(srcFile, destFile, handler);
	}

	private static FileInfo doCopyFile(File srcFile, File destFile,
			Handler handler) throws IOException {
		if (destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile
					+ "' exists but is a directory");
		}
		if (destFile.exists()) {
			destFile = new File(Util.autoGenerateName(destFile));
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			copy(fis, fos, handler);
			fos.getFD().sync();
		} finally {
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(fis);
		}

		if (srcFile.length() != destFile.length()) {
			LogUtil.e(TAG, "Failed to copy full contents from '" + srcFile
					+ "' to '" + destFile + "'");
		}
		return Util.getFileInfo(destFile, false);
	}

	public static int copy(InputStream input, OutputStream output,
			Handler handler) throws IOException {
		long count = copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE],
				handler);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	private static int oldCount = 0;
	private static long totalSize = 0;

	public static void initValues(long total, Thread thread) {
		totalSize = total;
		oldCount = 0;
		i = 0;
		count = 0;
		FileTools.thread = thread;
	}

	private static long count = 0;

	public static long copyLarge(InputStream input, OutputStream output,
			byte[] buffer, Handler handler) throws IOException {

		int n = 0;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
			if (FileTools.thread != null && FileTools.thread.isInterrupted()) {
				break;
			}
			if (totalSize != 0 && handler != null) {
				Message msg = handler.obtainMessage();
				msg.what = FileTools.maxProgress;
				msg.arg1 = (int) ((Float.valueOf(count) / Float
						.valueOf(totalSize)) * FileTools.maxProgress);
				if (msg.arg1 > oldCount) {
					oldCount = msg.arg1;
					// LogUtil.elog(TAG, "oldCount=="+oldCount);
					handler.sendMessage(msg);
					if (oldCount == FileTools.maxProgress) {
						oldCount = 0;
					}
				}
			}
		}
		return count;
	}

	/*********************** 复制结束 ************************************/

	/*********************** 删除开始 ************************************/
	/**
	 * 删除全部文件（文件夹和文件）
	 * 
	 * @param file
	 * @param handler
	 * @return
	 */
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
			// del = file.delete();
			del = delete(file.getAbsolutePath(), handler);
		} catch (Exception ignored) {
			ignored.printStackTrace();
			LogUtil.e(TAG, ignored.getLocalizedMessage() == null ? ""
					: ignored.getLocalizedMessage());
			return false;
		}
		return del;
	}

	// Removes duplicate adjacent slashes and any trailing slash.
	private static String fixSlashes(String origPath) {
		// Remove duplicate adjacent slashes.
		boolean lastWasSlash = false;
		char[] newPath = origPath.toCharArray();
		int length = newPath.length;
		int newLength = 0;
		for (int i = 0; i < length; ++i) {
			char ch = newPath[i];
			if (ch == '/') {
				if (!lastWasSlash) {
					newPath[newLength++] = separatorChar;
					lastWasSlash = true;
				}
			} else {
				newPath[newLength++] = ch;
				lastWasSlash = false;
			}
		}
		// Remove any trailing slash (unless this is the root of the file
		// system).
		if (lastWasSlash && newLength > 1) {
			newLength--;
		}
		// Reuse the original string if possible.
		return (newLength != length) ? new String(newPath, 0, newLength)
				: origPath;
	}

	public static boolean delete(String path) {
		try {
			path =fixSlashes(path);
			String temp = path + System.currentTimeMillis();
			Libcore.os.rename(path, temp);
			Libcore.os.remove(temp);
			return true;
		} catch (Exception errnoException) {
			errnoException.printStackTrace();
			LogUtil.e(TAG,
					"errnoException==" + errnoException.getMessage());
			return false;
		}
	}

	public static boolean delete(String path, Handler handler) {
		if ((OperationAction.getLastOperation() == Operation.del)
				&& handler != null) {
			i++;
			// LogUtil.log(TAG,
			// "del i=="+i+" totalSize:"+totalSize+" maxProgress:"+maxProgress);
			Message msg = handler.obtainMessage();
			msg.what = maxProgress;
			msg.arg1 = (int) ((Float.valueOf(i + 1) / Float.valueOf(totalSize)) * maxProgress);
			if (msg.arg1 > oldCount) {
				oldCount = msg.arg1;
				handler.sendMessage(msg);
			}
		}
		return delete(path);
	}

	/**
	 * 删除文件夹
	 * 
	 * @param directory
	 * @param handler
	 * @return
	 */
	public static boolean cleanDirectory(File directory, Handler handler) {// del
		if (directory == null || !directory.exists()) {
			return false;
		}
		if (!directory.isDirectory()) {
			return false;
		}
		File[] files = directory.listFiles();

		for (File file : files) {
			if (thread != null && thread.isInterrupted()) {
				LogUtil.e(TAG, "cleanDirectory thread.isInterrupted()");
				return false;
			}
			try {
				forceDelete(file, handler);
			} catch (IOException ioe) {
				LogUtil.e(TAG, "del error " + ioe.getMessage());
				return false;
			}
		}
		return true;
	}

	public static void forceDelete(File file, Handler handler)// del 3
			throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file, handler);
		} else {
			boolean filePresent = file.exists();
			if (!delete(file.getAbsolutePath(), handler)) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: "
							+ file);
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
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
		if (!delete(directory.getPath(), handler)) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
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

	/*********************** 删除结束 ************************************/

	public static FileInfo moveToDirectory(File src, File destDir,
			Handler handler) throws IOException {
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
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		if (!destDir.exists()) {
			throw new FileNotFoundException("Destination directory '" + destDir
					+ "' does not exist ");
		}
		if (!destDir.isDirectory()) {
			throw new IOException("Destination '" + destDir
					+ "' is not a directory");
		}
		if (src.isDirectory()) {
			return moveDirectory(src, new File(destDir, src.getName()), handler);
		} else {
			return moveFile(src, new File(destDir, src.getName()), handler);
		}
	}

	private static FileInfo moveFile(File srcFile, File destFile,
			Handler handler) throws IOException {

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
				deleteQuietly(destFile, handler);
				throw new IOException("Failed to delete original file '"
						+ srcFile + "' after copy to '" + destFile + "'");
			}
		} else {
			fileInfo = Util.getFileInfo(destFile, false);
		}
		if (handler != null) {
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
			if (destDir.getCanonicalPath()
					.startsWith(srcDir.getCanonicalPath())) {
				throw new IOException("Cannot move directory: " + srcDir
						+ " to a subdirectory of itself: " + destDir);
			}
			copyDirectory(srcDir, destDir, null, handler);
			deleteDirectory(srcDir, handler);
			if (srcDir.exists()) {
				throw new IOException("Failed to delete original directory '"
						+ srcDir + "' after copy to '" + destDir + "'");
			}
		} else {
			if (handler != null) {
				// add by Jxh 2014-8-12 begin
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

	public static boolean changeFile(String file) throws Exception {
		int len = 8;
		// 创建一个随机读写文件对象
		java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "rw");
		long totalLen = raf.length();
		// LogUtil.log(TAG, "文件总长字节是: " + totalLen+" path=="+file);
		// 打开一个文件通道
		java.nio.channels.FileChannel channel = raf.getChannel();
		// 映射文件中的某一部分数据以读写模式到内存中
		java.nio.MappedByteBuffer buffer = channel.map(
				FileChannel.MapMode.READ_WRITE, 0, len);
		// 示例修改字节
		for (int i = 0; i < len; i++) {
			byte src = buffer.get(i);
			buffer.put(i, (byte) (src ^ 2));// 修改Buffer中映射的字节的值
			// LogUtil.log(TAG, "被改为大写的原始字节是:" + src);
		}
		buffer.force();// 强制输出,在buffer中的改动生效到文件
		buffer.clear();
		channel.close();
		raf.close();
		return true;
	}

}
