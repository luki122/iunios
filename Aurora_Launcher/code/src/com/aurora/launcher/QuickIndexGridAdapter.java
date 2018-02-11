package com.aurora.launcher;

import com.aurora.view.GridViewForEmbed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class QuickIndexGridAdapter extends ArrayAdapter<ShortcutInfo> implements OnClickListener,OnLongClickListener{      
    private final LayoutInflater mInflater;
    private final IconCache mIconCache;
    private Launcher mLauncher;
    //private float mIconSizeOffset;
    //private Vibrator vibrator=null;
    public GridViewForEmbed mGridEmbed;
    
    public QuickIndexGridAdapter(Activity activity, AppCategoryData curAppCategoryData,GridViewForEmbed gv){
    	super(activity, 0, curAppCategoryData.getCategoryDataList());
    	mLauncher = (Launcher) activity;
    	mInflater = LayoutInflater.from(activity);
		mIconCache = ((LauncherApplication)activity.getApplicationContext()).getIconCache();
		mGridEmbed = gv;
		//mIconSizeOffset = mLauncher.getFloatValueFromResourcesDimens(R.dimen.quick_index_icon_resize_offset);
    }

	@Override 
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.quick_index_application, parent, false);
		} 
				
		if(getCount() <= position){
			return convertView;
		}
		
		final TextView textView = (TextView)convertView;
		ShortcutInfo item = (ShortcutInfo)getItem(position);
		textView.setCompoundDrawablesWithIntrinsicBounds(null,
				new FastBitmapDrawable(item.getIcon(mIconCache)), null, null);
		/*textView.setCompoundDrawablesWithIntrinsicBounds(null,
				new FastBitmapDrawable(item.getIcon(mIconCache)), null, null);*/
		textView.setText(item.title);
		/*textView.setScaleX(0.97f);
		textView.setScaleY(0.97f);*/
		/*textView.setPivotX(textView.getWidth()/2);
		textView.setPivotY(textView.getHeight()/2);*/
		if(textView instanceof ShadowTextView){
			((ShadowTextView)textView).setTag(item,false);
		}
		if(item.newFlag) {
			((BubbleTextView)textView).setNewApp(false);
		}
		
		textView.setOnClickListener(this);
		textView.setOnLongClickListener(this);
	//	textView.setBackgroundColor(Color.CYAN);
		return textView;
	}
	
	private static Bitmap small(Bitmap bitmap, float scale) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale); 
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}
	
	@Override
	public void onClick(View v) {
		Object tag= v.getTag();
		if(tag != null){
			// refactor this code from Folder
			ShortcutInfo item = (ShortcutInfo) tag;
			int[] pos = new int[2];
			v.getLocationOnScreen(pos);
			item.intent.setSourceBounds(new Rect(pos[0], pos[1], pos[0]
					+ v.getWidth(), pos[1] + v.getHeight()));
			LauncherApplication.logVulcan.print("onClick: setSourceBounds intent = " + item.intent.toUri(0));

			// Aurora <haojj> <2013-10-9> add for remove new App标识 begin
			boolean success = mLauncher.startActivitySafely(v, item.intent, item);
			if (success && v instanceof BubbleTextView) {
				((BubbleTextView) v).setNewApp(false);
				item.newFlag = false;
			}
			// Aurora <haojj> <2013-10-9> end
		}
	}


	@Override
	public boolean onLongClick(View arg0) {
		Bitmap m = createBitmapShawdow(arg0);
		int[] mTempXY = new int[2];
		if (null != m) {
			float scale = mLauncher.getDragLayer().getLocationInDragLayer(arg0,
					mTempXY);
			int dragLayerX = Math.round(mTempXY[0]
					- (m.getWidth() - scale * arg0.getWidth()) / 2);
			int dragLayerY = Math.round(mTempXY[1]
					- (m.getHeight() - scale * m.getHeight()) / 2
					- Workspace.DRAG_BITMAP_PADDING / 2);
			mLauncher.getQuickIndexFrameLayout().setQuickIndexGridAdapterContext(this);
			mLauncher.getQuickIndexFrameLayout().startDrag(arg0, m, dragLayerX,
					dragLayerY, arg0.getTag());
			m.recycle();
		}
		
		return false;
	}

	public Bitmap createBitmapShawdow(View v) {
		return mLauncher.getWorkspace().createDragBitmap(v, new Canvas(),
				Workspace.DRAG_BITMAP_PADDING);
	}

	
}
