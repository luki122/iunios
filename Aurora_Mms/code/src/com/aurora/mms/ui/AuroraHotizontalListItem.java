package com.aurora.mms.ui;
// Aurora xuyong 2015-10-08 created for aurora's new feature
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageListItem;
import com.android.mms.ui.UriImage;
import com.aurora.mms.util.AuroraAttachThumbnailWorker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import aurora.widget.AuroraCheckBox;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;

import aurora.widget.AuroraCheckBox;

public class AuroraHotizontalListItem extends FrameLayout{

	private final static String TAG = "AuroraHotizontalListItem";
	
	public  static final String PATH_SET_SEPERATOR = String.valueOf('\1');
	
	private Context mContext;
	private AuroraRoundImageView mImageContent;
	private AuroraCheckBox mSelectBox;
	private static Map<Integer, Uri> mSelectedMap = new HashMap<Integer, Uri>();
	private static ArrayList<Uri> mSelectedList = new ArrayList<Uri>();
	// MMS SlideViews pages' limit
	private static final int MMS_IMAGES_MAXCOUNT = 5;
	private Handler mHandler;
    // Aurora xuyong 2015-10-15 deleted for aurora's new feature start
	//private static AuroraAttachThumbnailWorker mLoadWorker = null;
	// Aurora xuyong 2015-10-15 deleted for aurora's new feature end
	private String mImagePath;
	
	public AuroraHotizontalListItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
        // Aurora xuyong 2015-10-15 deleted for aurora's new feature start
		//mLoadWorker = new AuroraAttachThumbnailWorker(context);
        // Aurora xuyong 2015-10-15 deleted for aurora's new feature end
	}
	
	public AuroraHotizontalListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
        // Aurora xuyong 2015-10-15 deleted for aurora's new feature start
		//mLoadWorker = new AuroraAttachThumbnailWorker(context);
        // Aurora xuyong 2015-10-15 deleted for aurora's new feature end
	}

	public AuroraHotizontalListItem(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
        // Aurora xuyong 2015-10-15 deleted for aurora's new feature start
		//mLoadWorker = new AuroraAttachThumbnailWorker(context);
        // Aurora xuyong 2015-10-15 deleted for aurora's new feature end
	}
	
	public static StringBuilder getSelectedImagesPath() {
		StringBuilder paths = new StringBuilder();
		/*Iterator iter = mSelectedMap.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, Uri> entry = (Entry<Integer, Uri>) iter.next();
			path.append(entry.getValue().toString() + PATH_SET_SEPERATOR);
		}*/
		for (Uri item : mSelectedList) {
			paths.append(item.toString() + PATH_SET_SEPERATOR);
		}
		return paths;
	}
	
	public static void clearSelectedItems() {
		mSelectedMap.clear();
		mSelectedList.clear();
        // Aurora xuyong 2015-10-15 modified for aurora's new feature start
		if (ComposeMessageActivity.mAuroraAttachThumbnailWorker != null) {
			ComposeMessageActivity.mAuroraAttachThumbnailWorker.clearTaskSet();
        // Aurora xuyong 2015-10-15 modified for aurora's new feature end
		}
	}
	
	public void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	private int getSelectedCount() {
		return mSelectedList.size();
	}
	
	private void handleItemClick(View view, int pos) {
		if (null != view && (mSelectedMap != null && getSelectedCount() < MMS_IMAGES_MAXCOUNT) || ((AuroraCheckBox)view).isChecked()) {
			((AuroraCheckBox) view).setChecked(!((AuroraCheckBox) view).isChecked());
			if (mSelectedMap.containsKey(pos)) {
				Log.e(TAG, "Remove!");
				mSelectedList.remove(mSelectedMap.get(pos));
				mSelectedMap.remove(pos);
			} else {
				Log.e(TAG, "put! path is " + mImagePath);
				mSelectedMap.put(pos, Uri.fromFile(new File(mImagePath)));
				mSelectedList.add(Uri.fromFile(new File(mImagePath)));
			}
			Message msg = mHandler.obtainMessage(ComposeMessageActivity.UPDATE_SELECT_IMAGE_COUNT);
			msg.arg1 = getSelectedCount();
			msg.sendToTarget();
		} else {
			if (!view.isSelected()) {
				Message msg = mHandler.obtainMessage(ComposeMessageActivity.ATTACH_IMAGE_COUNT_OVER);
				msg.sendToTarget();
			}
		}
	}
	
	public void bind(final Cursor cursor) {
		final int pos = cursor.getPosition();
		mImagePath = cursor.getString(1);
		Log.e(TAG, "pos is " + pos);
        // Aurora xuyong 2016-02-22 deleted for bug #19508 start
		/*if (mSelectBox != null) {
			mSelectBox.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					handleItemClick(view, pos);
				}
			});
		}*/
        // Aurora xuyong 2016-02-22 deleted for bug #19508 end
		mImageContent.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				handleItemClick(mSelectBox, pos);
			}
		});
		if (mSelectedMap.containsKey(pos)) {
			Log.e(TAG, "set select " + mSelectedMap.get(pos));
			mSelectBox.setChecked(true);
		} else {
			Log.e(TAG, "set select false");
			mSelectBox.setChecked(false);
		}
		initImageRatio(mImageContent, mImagePath);
        // Aurora xuyong 2015-10-15 modified for aurora's new feature start
		ComposeMessageActivity.mAuroraAttachThumbnailWorker.loadImage(mImagePath, mImageContent);
        // Aurora xuyong 2015-10-15 modified for aurora's new feature end
	}
	
	private void initImageRatio(AuroraRoundImageView imageView, String path) {
		Uri uri = Uri.fromFile(new File(path));
    	UriImage uriImage = new UriImage(mContext, uri);
        int outWidth = uriImage.getWidth();
        int outHeight = uriImage.getHeight();
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
		params.height = mContext.getResources().getDimensionPixelOffset(R.dimen.aurora_attach_item_height);
	    params.width = (int) (((float)outWidth / outHeight) * params.height);
	    imageView.setLayoutParams(params);
	}
	
	@Override
    protected void onFinishInflate() {
		mImageContent = (AuroraRoundImageView)findViewById(R.id.aurora_ai_image);
		mSelectBox = (AuroraCheckBox)this.findViewById(R.id.aurora_ai_cb);
	}

}
