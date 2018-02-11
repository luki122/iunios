package com.aurora.widget;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.dbutil.FileCategoryHelper.CategoryInfo;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileInfo;
import com.aurora.tools.Util;
import com.aurora.tools.Util.SDCardInfo;
import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.R;

/**
 */
public class AuroraFileStorageDetailInfo {

	private String TAG = "AuroraFileStorageDetailInfo";
	private float pic;
	private float video;
	private float music;
	private float document;
	private float apk;
	private float other;
	private float free;
	private long size;
	private long otherSize;
	private long totalSize;
	private Map<FileCategory, Long> chartSizeMap;

	private BigDecimal sumBigDecimal;
	private BigDecimal tempBigDecimal;

	public SDCardInfo storageInfo;

	private View contentView;

	private FileCategoryHelper mFileCategoryHelper;
	private String type;

	private PieChart usbPieChart;

	private PieChart sdCardPieChart;

	private SinglePieChart pie;

	public AuroraFileStorageDetailInfo(View contentView,
			SDCardInfo storageInfo, String infoType) {
		chartSizeMap = new HashMap<FileCategory, Long>();
		this.contentView = contentView;
		this.storageInfo = storageInfo;
		type = infoType;
	}

	public FileCategoryHelper getmFileCategoryHelper() {
		return mFileCategoryHelper;
	}

	public void setmFileCategoryHelper(FileCategoryHelper mFileCategoryHelper) {
		this.mFileCategoryHelper = mFileCategoryHelper;
	}

	public float getPic() {
		return pic;
	}

	public void setPic(float pic) {
		this.pic = pic;
	}

	public float getVideo() {
		return video;
	}

	public void setVideo(float video) {
		this.video = video;
	}

	public float getMusic() {
		return music;
	}

	public void setMusic(float music) {
		this.music = music;
	}

	public float getDocument() {
		return document;
	}

	public void setDocument(float document) {
		this.document = document;
	}

	public float getApk() {
		return apk;
	}

	public void setApk(float apk) {
		this.apk = apk;
	}

	public float getOther() {
		return other;
	}

	public void setOther(float other) {
		this.other = other;
	}

	public float getFree() {
		return free;
	}

	public void setFree(float free) {
		this.free = free;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getOtherSize() {
		return otherSize;
	}

	public void setOtherSize(long otherSize) {
		this.otherSize = otherSize;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public Map<FileCategory, Long> getChartSizeMap() {
		return chartSizeMap;
	}

	public void setChartSizeMap(Map<FileCategory, Long> chartSizeMap) {
		this.chartSizeMap = chartSizeMap;
	}

	private int getTextIdAndSetSize(String type, FileCategory fc) {
		int txtId = 0;
		switch (fc) {
		case Music:
			if ("sd".equals(type)) {
				txtId = R.id.category_legend_music_sdCard;
			} else {
				txtId = R.id.category_legend_music;
			}
			chartSizeMap.put(fc, size);
			break;
		case Video:
			if ("sd".equals(type)) {
				txtId = R.id.category_legend_video_sdCard;
			} else {
				txtId = R.id.category_legend_video;
			}
			chartSizeMap.put(fc, size);
			break;
		case Picture:
			if ("sd".equals(type)) {
				txtId = R.id.category_legend_picture_sdCard;
			} else {
				txtId = R.id.category_legend_picture;
			}
			chartSizeMap.put(fc, size);
			break;
		case Doc:
			if ("sd".equals(type)) {
				txtId = R.id.category_legend_document_sdCard;
			} else {
				txtId = R.id.category_legend_document;
			}
			chartSizeMap.put(fc, size);
			break;
		case Apk:
			if ("sd".equals(type)) {
				txtId = R.id.category_legend_apk_sdCard;
			} else {
				txtId = R.id.category_legend_apk;
			}
			chartSizeMap.put(fc, size);
			break;
		case Other:
			if ("sd".equals(type)) {
				txtId = R.id.category_legend_other_sdCard;
			} else {
				txtId = R.id.category_legend_other;
			}
			chartSizeMap.put(fc, size);
			break;

		default:
			break;
		}
		return txtId;
	}

	private void setCategorySize(FileCategory fc, long msize) {
		int txtId = 0;
		size = msize;
		txtId = getTextIdAndSetSize(type, fc);

		tempBigDecimal = new BigDecimal(size);

		sumBigDecimal = sumBigDecimal.add(tempBigDecimal);

		if (size < 0) {
			setTextView(txtId, "0 B");
		} else {
			setTextView(txtId, Util.convertStorage(size));
		}
	}

	private void setTextView(int id, String t) {
		TextView text = (TextView) contentView.findViewById(id);
		text.setText(t);
	}

	private void setTextViewSpan(int id, Spannable t) {
		TextView text = (TextView) contentView.findViewById(id);
		text.append(t);
	}

	/**
	 * @param size
	 * @return 计算百分比
	 */
	private float formatSize(long size) {

		float f1=0;
		try {
			double totalBigDecimal = new BigDecimal(Double.valueOf(totalSize))
					.doubleValue();
			double tempBigDecimal = new BigDecimal(Double.valueOf(size))
					.doubleValue();
			double vv = (totalBigDecimal * 0.0000000001);
			double ss = (tempBigDecimal * 0.0000000001);

			double f = (double) ss / vv;
			BigDecimal b = new BigDecimal(f);
			f1 = b.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return f1;
	}

	private ArrayList<Float> alPercentage = new ArrayList<Float>();

	public void draw(String path,
			ConcurrentHashMap<FileCategory, List<FileInfo>> hashMaps) {
		sumBigDecimal = new BigDecimal(0);
		if(mFileCategoryHelper==null){
			LogUtil.e(TAG, "mFileCategoryHelper is null");
			return;
		}
		ConcurrentHashMap<FileCategory, CategoryInfo> hashMap = mFileCategoryHelper
				.getCategoryInfos(path);
//		LogUtil.elog(TAG, "path==="+path + " hashMap" + hashMap);
		if (hashMap == null) {
			return;
		}
		for (FileCategory fc : FileCategoryHelper.sCategories) {

			CategoryInfo categoryInfo = hashMap.get(fc);
			if (categoryInfo != null) {
//				 LogUtil.elog(TAG, "FileCategory==="+fc + " ==" + categoryInfo.toString());
				if ("usb".equals(type)) {
					setCategorySize(fc, categoryInfo.size);
				} else if ("sd".equals(type)) {
					setCategorySize(fc, categoryInfo.size);
				} else {
					setCategorySize(fc, categoryInfo.size);
				}
			}
		}
		if (storageInfo != null) {

			BigDecimal totalBigDecimal = new BigDecimal(storageInfo.total);

			BigDecimal freeBigDecimal = new BigDecimal(storageInfo.free);

			otherSize = (totalBigDecimal.subtract(freeBigDecimal)
					.subtract(sumBigDecimal)).longValue();

//			Log.i(TAG, storageInfo.total + "---total");
//			Log.i(TAG, storageInfo.free + "---free");
//			Log.i(TAG, otherSize + "---otherSize");

			setCategorySize(FileCategory.Other, otherSize);
			String text = Util.convertStorageG(storageInfo.free);
			Spannable wordtoSpan = new SpannableString(text);

			if ("usb".equals(type)) {
				setTextView(R.id.usb_storage_total,
						Util.convertStorage(storageInfo.total));
				wordtoSpan.setSpan(
						new AbsoluteSizeSpan(SinglePieChart.dp2px(
								contentView.getContext(), 19)), 0,
						text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				wordtoSpan.setSpan(
						new AbsoluteSizeSpan(SinglePieChart.dp2px(
								contentView.getContext(), 9)),
						text.length() - 1, text.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				setTextViewSpan(R.id.usb_storage_free, wordtoSpan);
			} else if ("sd".equals(type)) {
				wordtoSpan.setSpan(
						new AbsoluteSizeSpan(SinglePieChart.dp2px(
								contentView.getContext(), 19)), 0,
						text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				wordtoSpan.setSpan(
						new AbsoluteSizeSpan(SinglePieChart.dp2px(
								contentView.getContext(), 9)),
						text.length() - 1, text.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				setTextView(R.id.sdCard_storage_total,
						Util.convertStorage(storageInfo.total));
				setTextViewSpan(R.id.sdCard_storage_free, wordtoSpan);
			} else {
				wordtoSpan.setSpan(
						new AbsoluteSizeSpan(SinglePieChart.dp2px(
								contentView.getContext(), 30)), 0,
						text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				wordtoSpan.setSpan(
						new AbsoluteSizeSpan(SinglePieChart.dp2px(
								contentView.getContext(), 13)),
						text.length() - 1, text.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				setTextView(R.id.usb_storage_total,
						Util.convertStorage(storageInfo.total));

				setTextViewSpan(R.id.usb_storage_free, wordtoSpan);
			}

			alPercentage.clear();
			totalSize = storageInfo.total;

			long picTemp = chartSizeMap.get(FileCategory.Picture)==null?0:chartSizeMap.get(FileCategory.Picture);
			long videoTemp = chartSizeMap.get(FileCategory.Video)==null?0:chartSizeMap.get(FileCategory.Video);
			long musicTemp = chartSizeMap.get(FileCategory.Music)==null?0:chartSizeMap.get(FileCategory.Music);
			long otherTemp = chartSizeMap.get(FileCategory.Other)==null?0:chartSizeMap.get(FileCategory.Other);

			if (hashMaps != null) {
				List<FileInfo> fileInfos = hashMaps.get(FileCategory.Picture);
				long p = getSizeForFileInfos(fileInfos);
				picTemp += p;
				List<FileInfo> fileInfosv = hashMaps.get(FileCategory.Video);
				long v = getSizeForFileInfos(fileInfosv);
				videoTemp += v;
				List<FileInfo> fileInfosm = hashMaps.get(FileCategory.Music);
				long m = getSizeForFileInfos(fileInfosm);
				musicTemp += m;
//				LogUtil.elog(TAG, " p==" + p + " v==" + v + " m==" + m);
				otherTemp = otherTemp - p - v - m;

				setCategorySize(FileCategory.Picture, picTemp);
				setCategorySize(FileCategory.Video, videoTemp);
				setCategorySize(FileCategory.Music, musicTemp);

			}

			pic = formatSize(picTemp);
			video = formatSize(videoTemp);
			music = formatSize(musicTemp);
			document = formatSize(chartSizeMap.get(FileCategory.Doc) == null ? 0
					: chartSizeMap.get(FileCategory.Doc));
			apk = formatSize((chartSizeMap.get(FileCategory.Apk) == null ? 0
					: chartSizeMap.get(FileCategory.Apk)));
			other = formatSize(otherTemp);
			free = formatSize(storageInfo.free);

			alPercentage.add(pic * 100f);
			alPercentage.add(video * 100f);
			alPercentage.add(music * 100f);
			alPercentage.add(document * 100f);
			alPercentage.add(apk * 100f);
			alPercentage.add(other * 100f);
			alPercentage.add(free * 100f);
			showPieChart(alPercentage);

		}
	}

	public void clear() {
		usbPieChart = null;
		sdCardPieChart = null;
		pie = null;
	}

	private void showPieChart(ArrayList<Float> alPercentage) {
		try {
			// setting data
			if ("usb".equals(type)) {
				usbPieChart = (PieChart) contentView
						.findViewById(R.id.pieChartUsb);
				usbPieChart.setAdapter(alPercentage);
			} else if ("sd".equals(type)) {
				sdCardPieChart = (PieChart) contentView
						.findViewById(R.id.pieChartSdCard);
				sdCardPieChart.setAdapter(alPercentage);
			} else {
				pie = (SinglePieChart) contentView
						.findViewById(R.id.pieChartUsb);
				pie.setAdapter(alPercentage);
			}

		} catch (Exception e) {
			if (e.getMessage().equals(SinglePieChart.ERROR_NOT_EQUAL_TO_100)) {
				Log.e("kenyang", "percentage is not equal to 100");
			}
		}
	}

	private long getSizeForFileInfos(List<FileInfo> fileInfos) {
		if (fileInfos == null || fileInfos.isEmpty()) {
			return 0;
		}
		long size = 0;
		for (FileInfo fileInfo : fileInfos) {
			size += fileInfo.fileSize;
		}
		return size;
	}
}
