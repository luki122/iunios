package com.aurora.change.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.change.R;
import com.aurora.change.data.WallpaperValue;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.utils.Consts;

// Aurora liugj 2014-07-30 added for wallpaper pic resources divide
public class WallpaperLocalGridAdapter extends BaseAdapter{
	
	//private Map<String, SoftReference<Bitmap>> mImageCache = new HashMap<String, SoftReference<Bitmap>>();
	/** GridView当前滚动状态  */
    //private int mGridScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
	private Handler mHandler = new Handler();
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<String> mDataList;
	private List<String> mFailedList;
	private int mSelectPos = -1;
	private boolean isEditMode = false;
	private SelectionManager<Integer> mSelectionManager;
	private ImageResizer mImageResizer;
	
	public WallpaperLocalGridAdapter(Context context, List<String> dataList, int selectPos){
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mDataList = dataList;
		mSelectPos = selectPos;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count = Consts.LOCAL_WALLPAPERS.length;
		return mDataList.size() + count;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void setEditMode(boolean mode) {
		isEditMode = mode;
	}
	
	public boolean getEditMod() {
		return isEditMode;
	}
	
	public void setSelectionManager(SelectionManager<Integer> selectionManager) {
        mSelectionManager = selectionManager;
    }
	
	/*public void setGridScrollState(int state) {
		mGridScrollState = state;
	}*/
	
	/*public int getGridScrollState() {
		return mGridScrollState;
	}*/
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HolderView holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.wallpaper_grid_item, null);
			holder = new HolderView();
			holder.mItemBg = (ImageView) convertView.findViewById(R.id.item_bg);
			holder.mItemSelected = (ImageView) convertView.findViewById(R.id.item_selected);
			convertView.setTag(holder);
		}else {
			holder = (HolderView) convertView.getTag();
		}
		int len = Consts.LOCAL_WALLPAPERS.length;
		if (position < len) {
			mImageResizer.loadImage(Consts.DEFAULT_SYSTEM_DESKTOP_WALLPAPER_PATH + Consts.LOCAL_WALLPAPERS[position], holder.mItemBg);
			if (isEditMode) {
				holder.mItemSelected.setVisibility(View.GONE);
			}
		}else {
			loadImage(mDataList.get(position - len), holder.mItemBg);
			if (isEditMode && mSelectionManager != null && position != mSelectPos) {
				holder.mItemSelected.setBackgroundResource(R.drawable.source_select_checkbox);
				holder.mItemSelected.setActivated(mSelectionManager.isSelected(position));
				holder.mItemSelected.setVisibility(View.VISIBLE);
			}else {
				holder.mItemSelected.setVisibility(View.GONE);
			}
		}
		
			if (position == mSelectPos) {
				holder.mItemSelected.setBackgroundResource(R.drawable.grid_item_selected);
				holder.mItemSelected.setVisibility(View.VISIBLE);
			} else {
				if (!isEditMode) {
					holder.mItemSelected.setVisibility(View.GONE);
				}
			}
		
		return convertView;
	}
	
	public void setSelectPos(int position) {
		mSelectPos = position;
	}
	
	public void setDataList(List<String> list, String updatePath) {
		mDataList = list;
		if (updatePath != null) {
			mImageResizer.removeImageCache(mContext, getCacheName(updatePath));
		}
	}
	
	private String getCacheName(String filename) {
    	File file = new File(filename);
    	StringBuffer sb = new StringBuffer();
    	if (file != null && file.exists()) {
			sb.append(file.length()).append("_");
			sb.append(file.hashCode()).append("_");
		}
    	sb.append(filename);
    	return sb.toString();
    }
	
	public void setImageResizer(ImageResizer imageResizer) {
        mImageResizer = imageResizer;
        mImageResizer.setCropNail(true);
    }
	
	private void loadImage(final String path, final ImageView imageView) {
		// Aurora liugj 2014-08-06 added for bug-7181 壁纸设置列表上下滑动不流畅 start
		/*if (mGridScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_default_bg));
			return;
		}*/
		// Aurora liugj 2014-08-06 added for bug-7181 壁纸设置列表上下滑动不流畅 end
		if (!checkFileAvailable(path)) {
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.item_load_failed);
			imageView.setImageDrawable(drawable);
			addFailedImage(path);
		}else {
			mImageResizer.loadImage(Environment.getExternalStorageDirectory().getPath() + path, imageView);
		}
	}
	
	private void addFailedImage(String path) {
		if (mFailedList == null) {
			mFailedList = new ArrayList<String>();
		}
		String savePath = path.replace(WallpaperValue.WALLPAPER_PATH, "");
		if (mFailedList != null && !mFailedList.contains(savePath)) {
			mFailedList.add(savePath);
		}
	}
	
	/**
     * 检查文件是否有效
     * @param path 完整文件路径
     * @return boolean 是否有效
     */
    private boolean checkFileAvailable(String path) {
        boolean exist = false;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), path);
            if (file != null && file.exists()) {
                exist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exist;
    }
    
	class HolderView {
		ImageView mItemBg;
		ImageView mItemSelected;
	}
	
	public void onResume() {
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(false);
            //notifyDataSetChanged();
        }
    }
	
	public void onPause() {
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(true);
            mImageResizer.flushCache();
        }
    }
	
	public void clearCache() {
        if (mImageResizer != null) {
            // mImageResizer.clearCache();
        	mImageResizer.setCropNail(false);
            mImageResizer.clearMemoryCache();
            mImageResizer.closeCache();
        }
        clear();
    }
	
	private void clear() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (mFailedList != null && mFailedList.size() > 0) {
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
					boolean selectDeleted = false;
					int len = mFailedList.size();
					StringBuffer sb = new StringBuffer(WallpaperValue.WALLPAPER_FILENAME);
					sb.append(" IN ('");
					for (int i = 0; i < len; i++) {
						String failPath = mFailedList.get(i);
						if (sp.getString("selectpath", "-1").equals(failPath)) {
							selectDeleted = true;
						}
						sb.append(failPath);
						if (i != len - 1) {
							sb.append("', '");
						}else {
							sb.append("')");
						}
					}
					try {
						mContext.getContentResolver().delete(WallpaperValue.LOCAL_WALLPAPER_URI, sb.toString(), null);					
						if (selectDeleted) {
							SharedPreferences.Editor editor = sp.edit();
							editor.putString("selectpath", "-1");
							editor.putInt("selectpos", -1);
							editor.commit();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					mFailedList.clear();
					mFailedList = null;
				}
			}
		}).start();
	}
}
