package com.aurora.tools;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import android.text.TextUtils;


public class FileSortHelper {

	private static String TAG = "FileSortHelper";

	public enum SortMethod {
		name, size, date, type, pinyin, modifyDate,music
	}

	private SortMethod mSort;

	private boolean mFileFirst;

	private HashMap<SortMethod, Comparator<FileInfo>> mComparatorList = new HashMap<SortMethod, Comparator<FileInfo>>();
	private HashMap<SortMethod, Comparator<File>> fileComparatorList = new HashMap<FileSortHelper.SortMethod, Comparator<File>>();

	public FileSortHelper() {
		mSort = SortMethod.name;
		mComparatorList.put(SortMethod.name, cmpName);
		mComparatorList.put(SortMethod.size, cmpSize);
		mComparatorList.put(SortMethod.date, cmpDate);
		mComparatorList.put(SortMethod.type, cmpType);
		mComparatorList.put(SortMethod.pinyin, cmpPinYin);

		fileComparatorList.put(SortMethod.pinyin, filePinYin);
		fileComparatorList.put(SortMethod.name, fileName);
	}

	public void setSortMethog(SortMethod s) {
		mSort = s;
	}

	public SortMethod getSortMethod() {
		return mSort;
	}

	public void setFileFirst(boolean f) {
		mFileFirst = f;
	}

	public Comparator<FileInfo> getComparator() {
		return mComparatorList.get(mSort);
	}

	public Comparator<File> getFileComparator() {
		return fileComparatorList.get(mSort);
	}

	private abstract class FileComparator implements Comparator<FileInfo> {

		@Override
		public int compare(FileInfo object1, FileInfo object2) {
			if (object1.IsDir == object2.IsDir) {
				return doCompare(object1, object2);
			}
			if (mFileFirst) {
				return (object1.IsDir ? 1 : -1);
			} else {
				return object1.IsDir ? -1 : 1;
			}
		}
		

		protected abstract int doCompare(FileInfo object1, FileInfo object2);
	}

	private abstract class FileComparator2 implements Comparator<File> {

		@Override
		public int compare(File object1, File object2) {
			if (object1.isDirectory() == object2.isDirectory()) {
				return doCompare(object1, object2);
			}
			if (mFileFirst) {
				return (object1.isDirectory() ? 1 : -1);
			} else {
				return object1.isDirectory() ? -1 : 1;
			}
		}

		protected abstract int doCompare(File object1, File object2);
	}

	private Comparator<FileInfo> cmpName = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			Collator comparator = Collator.getInstance(Locale.CHINA);
			if(TextUtils.isEmpty(object1.fileName)||TextUtils.isEmpty(object2.fileName)){
				return -1;
			}
			return comparator.compare(object1.fileName, object2.fileName);
//			return (object1.fileName).compareTo(object2.fileName);
		}
	};

	private Comparator<FileInfo> cmpSize = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object1.fileSize - object2.fileSize);
		}
	};

	private Comparator<FileInfo> cmpDate = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			return longToCompareInt(object2.ModifiedDate - object1.ModifiedDate);
		}
	};

	private int longToCompareInt(long result) {
		return result > 0 ? 1 : (result < 0 ? -1 : 0);
	}

	private Comparator<FileInfo> cmpType = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			int result = Util.getExtFromFilename(object1.fileName)
					.compareToIgnoreCase(
							Util.getExtFromFilename(object2.fileName));
			if (result != 0)
				return result;

			return Util.getNameFromFilename(object1.fileName)
					.compareToIgnoreCase(
							Util.getNameFromFilename(object2.fileName));
		}
	};

	private Comparator<FileInfo> cmpPinYin = new FileComparator() {
		@Override
		public int doCompare(FileInfo object1, FileInfo object2) {
			String name1 = object1.order;
			String name2 = object2.order;
			boolean isLetterOrDigit1 = object1.isLetterOrDigit;
			boolean isLetterOrDigit2 = object2.isLetterOrDigit;
			if (isLetterOrDigit1 && isLetterOrDigit2) {
				return name1.compareToIgnoreCase(name2);
			} else if (isLetterOrDigit1 && !isLetterOrDigit2) {
				return 1;
			} else if (!isLetterOrDigit1 && isLetterOrDigit2) {
				return -1;
			}
			return name1.compareToIgnoreCase(name2);
		}
	};

	private Comparator<File> filePinYin2 = new Comparator<File>() {

		@Override
		public int compare(File lhs, File rhs) {
			String name1 = Util.getSpell(lhs.getName());
			String name2 = Util.getSpell(rhs.getName());
			boolean isLetterOrDigit1 = Character.isLetterOrDigit(name1
					.charAt(0));
			boolean isLetterOrDigit2 = Character.isLetterOrDigit(name2
					.charAt(0));

			if (isLetterOrDigit1 && isLetterOrDigit2) {
				return name1.compareToIgnoreCase(name2);
			} else if (isLetterOrDigit1 && !isLetterOrDigit2) {
				return 1;
			} else if (!isLetterOrDigit1 && isLetterOrDigit2) {
				return -1;
			}
			return name1.compareToIgnoreCase(name2);
		}
	};

	private Comparator<File> filePinYin = new FileComparator2() {

		@Override
		protected int doCompare(File lhs, File rhs) {
			String name1 = Util.getSpell(lhs.getName());//Util.getSpell(
			String name2 = Util.getSpell(rhs.getName());
			boolean isLetterOrDigit1 = Character.isLetterOrDigit(name1
					.charAt(0));
			boolean isLetterOrDigit2 = Character.isLetterOrDigit(name2
					.charAt(0));

			if (isLetterOrDigit1 && isLetterOrDigit2) {
				return name1.compareToIgnoreCase(name2);
			} else if (isLetterOrDigit1 && !isLetterOrDigit2) {
				return 1;
			} else if (!isLetterOrDigit1 && isLetterOrDigit2) {
				return -1;
			}
			return name1.compareToIgnoreCase(name2);
		}
	};
	
	private Comparator<File> fileName = new FileComparator2() {
		@Override
		public int doCompare(File object1, File object2) {
			Collator comparator = Collator.getInstance(java.util.Locale.CHINA);
			return comparator.compare(object1.getName(), object2.getName());
		}
	};
}
