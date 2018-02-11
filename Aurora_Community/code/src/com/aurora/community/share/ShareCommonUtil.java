package com.aurora.community.share;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.emilsjolander.stickylistheaders.pulltorefresh.PullToRefreshListView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.utils.DensityUtil;

public class ShareCommonUtil {
	
	public static final String TAG = "ShareCommonUtil";
	
	public static final String TEMP_PIC_FILE_PATH = "/sdcard/community/temp_pic";
	
	public static final float SCREEN_WIDTH = 1080f;
	public static final float SCREEN_HEIGHT = 1920f;
	public static final int SCREEN_COUNT = 3;
	
	private static String [] mShareStr = {
		"微信",
		"朋友圈",
//		"微博",
//		"其他",
	};
	
	private static Integer[] mImageIds = {   
		R.drawable.aurora_share_wx, 
		R.drawable.aurora_share_friends, 
//		R.drawable.aurora_share_xlwb, 
//		R.drawable.aurora_share_other,
	};
	
	private AlertDialog mDialog = null;
	private AuroraDilogCallBack mCallback = null;
	
	public interface AuroraDilogCallBack{
		public void onFinishDialog(int ret);
	}
	
	public void setCallback(AuroraDilogCallBack mCallback) {
		this.mCallback = mCallback;
	}

	public void showShareDialog(Activity activity, int messageId) {
		
		int mleftoff = DensityUtil.dip2px(activity, 26f);
		
		View mView = LayoutInflater.from(activity).inflate(R.layout.aurora_dialogfragment, null);
		TextView tv_title = (TextView) mView.findViewById(R.id.aurora_alertTitle);
		final GridView mGridView = (GridView)mView.findViewById(R.id.my_dialog_gridview);
		
		tv_title.setText(activity.getString(R.string.share_title));
		
		List<Map<String, Object>> mList = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < mImageIds.length; i++) {
			Map<String, Object> map =  new HashMap<String, Object>();
			map.put("PIC", mImageIds[i]);
			map.put("TITLE", mShareStr[i]);
			mList.add(map);
		}
		
		final BaseAdapter mAdapter = new SimpleAdapter(activity, (List<Map<String, Object>>)mList, R.layout.aurora_dialog_girditem, 
				new String[] {"PIC", "TITLE"}, new int[] {R.id.girdview_icon, R.id.girdview_text});
		
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if (mCallback != null) {
					mCallback.onFinishDialog(position);
				}
				
				mDialog.dismiss();
			}
		});
		
		mView.findViewById(R.id.btn_sure).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDialog != null) {
					mDialog.dismiss();
				}
			}
		});
		
		mDialog =  new AlertDialog.Builder(activity)
		.setTitle(messageId)
		.setCancelable(true)
		.setCustomTitle(mView).create();
//		.setView(mView, mleftoff, 0, mleftoff, 0)
//		.setTitleDividerVisible(true)
//		.setNegativeButton(R.string.share_cancel, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog,
//					int whichButton) {
//				dialog.dismiss();
//			}
//		}).create();
		
		mDialog.show();
	}

	/**
	 * 保存图片到sdcard中
	 * @param pBitmap
	 */
	public boolean savePic(Bitmap pBitmap, String strName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(strName);
			if (null != fos) {
				pBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
				return true;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 截图listview
	 * **/
	public Bitmap getBitmap(Context context, PullToRefreshListView listView, View headerView, BaseAdapter adapter, int footerOffset) {
		
		Bitmap bitmapScaled = null;
		
		try {
			/*CListView lv = new CListView(context);
			lv.setLayoutParams(new LayoutParams(listView.getWidth(), LayoutParams.WRAP_CONTENT));
			lv.addHeaderView(headerView);
			lv.setAdapter(adapter);
			
			if (lv.getFooterViewsCount() <= 0) {
				footerOffset = 0;
			}
			
			lv.measure(MeasureSpec.makeMeasureSpec(listView.getWidth(),
			        MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			lv.layout(0, 0, lv.getMeasuredWidth(), lv.getMeasuredHeight() + footerOffset);
			
			// 创建对应大小的bitmap
			Bitmap bitmap = Bitmap.createBitmap(listView.getWidth(), lv.getMeasuredHeight() + footerOffset,
					Bitmap.Config.RGB_565);
			final Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(listView.getContext().getResources().getColor(android.R.color.white));
			lv.draw(canvas);
			
			if ((lv.getMeasuredHeight() + footerOffset) / SCREEN_HEIGHT > 10) {
				bitmapScaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
			} else {
				bitmapScaled = bitmap;
			}
			
			lv.setAdapter(null);
			lv = null;*/
			
			Bitmap bitmap = Bitmap.createBitmap(headerView.getWidth(), headerView.getHeight(),
					Bitmap.Config.ARGB_8888);
			final Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(headerView.getContext().getResources().getColor(android.R.color.white));
			headerView.draw(canvas);
			
			float newWidth = 0;
			float newHeight = 0;
			
			float height = headerView.getHeight();
			
//			System.out.println("width: " + headerView.getWidth());
//			System.out.println("height: " + height);
			
			// 最大5屏高度等比缩放
			if (height > SCREEN_COUNT * SCREEN_HEIGHT) {
				newHeight = SCREEN_COUNT * SCREEN_HEIGHT;
				newWidth = headerView.getWidth() * ((SCREEN_COUNT * SCREEN_HEIGHT) / height);
				
				bitmapScaled = Bitmap.createScaledBitmap(bitmap, (int)newWidth, 
						(int)newHeight, true);
			} else {
				bitmapScaled = bitmap;
			}
			
//			System.out.println("newHeight: " + newHeight);
//			System.out.println("newWidth: " + newWidth);
			
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		
		return bitmapScaled;
	}
	
	private static class CListView extends PullToRefreshListView {

		public CListView(Context context) {
			super(context);
		}
		
		@Override  
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
		    int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,  
		            MeasureSpec.AT_MOST);  
		    super.onMeasure(widthMeasureSpec, expandSpec);  
		} 
	}

}
