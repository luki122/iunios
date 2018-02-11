package com.aurora.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ScrollView;

import com.aurora.launcher.R;
import com.aurora.util.Utils;

public class WidgetsManagerActivity extends Activity {

	private boolean mFromApp = false;
	private TextView actionBar;
	private RelativeLayout switchParent;
	private TextView widgetsTips;
	private TextView widgetsTitle;
	private ListView widgetsList;
	private WidgetsListAdapter widgetsListAdapter;
	private Switch wholeSwitch;
	private SharedPreferences mSharedPrefs;
	private int appWidgetIconSize;
	private int listItemHeight;
	private int listBorderHeight;
	private Handler mHandler;
	public static final String content_uri = "content://"+"com.android.systemui.statusbar.phone"+"/"+"immersion"+"?notify=true";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
		
		/*ContentResolver cr = this.getContentResolver();
    	ContentValues cv=new ContentValues();
    	cv.put("imersion_color", 1);
    	int i = cr.update(Uri.parse(content_uri), cv, null, null);*/
    	
		setContentView(R.layout.manage_widgets);
		mSharedPrefs = getSharedPreferences(
				LauncherApplication.getSharedPreferencesKey(),
				Context.MODE_PRIVATE);
		appWidgetIconSize =(int)this.getResources().getDimension(R.dimen.app_widget_icon_size);
		listItemHeight =(int)this.getResources().getDimension(R.dimen.app_list_item_height);
		listBorderHeight =(int)this.getResources().getDimension(R.dimen.app_list_border_size);
		mHandler = new Handler();
		setupViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getIntent().getExtras() != null) {
			mFromApp = "settings".equals(getIntent().getExtras().getString(
					"fromApp", "launcher"));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	static class IconCompound{
		Drawable icon ;
		String   title;
		String pkgName;
		String simplePinYin;
		boolean isChecked;
		boolean enable;
	}
	
	public static final Comparator<IconCompound> getAppComparator() {
        return new Comparator<IconCompound>() {
            public final int compare(IconCompound a, IconCompound b) {
            	return Utils.compare(a.simplePinYin, b.simplePinYin);
            }
        };
    }
	
	public Drawable resizeImage(Bitmap bitmap, int w, int h)   
    {    
        Bitmap BitmapOrg = bitmap;    
        int width = BitmapOrg.getWidth();    
        int height = BitmapOrg.getHeight();    
        int newWidth = w;    
        int newHeight = h;    
  
        float scaleWidth = ((float) newWidth) / width;    
        float scaleHeight = ((float) newHeight) / height;    
  
        Matrix matrix = new Matrix();    
        matrix.postScale(scaleWidth, scaleHeight);    
        // if you want to rotate the Bitmap     
        // matrix.postRotate(45);     
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,    
                        height, matrix, true);    
        return new BitmapDrawable(this.getResources() , resizedBitmap);    
    }  
	

	private void setupViews() {
		
		actionBar = (TextView) findViewById(R.id.widgets_top_title);
		actionBar.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				if (mFromApp) {
					overridePendingTransition(
							R.anim.activity_close_enter,
							R.anim.activity_close_exit);
				}
			}});
		switchParent = (RelativeLayout) findViewById(R.id.whole_switch_parent);
		widgetsTips = (TextView) findViewById(R.id.widgets_tips);
		widgetsTitle = (TextView) findViewById(R.id.widgets_list_title);
		wholeSwitch = (Switch) findViewById(R.id.whole_switch); 
		widgetsList = (ListView) findViewById(R.id.widgets_list); 
		Launcher launcher =(Launcher) ((LauncherApplication) getApplicationContext()).getModel().getCallback();
		if( launcher != null ){
		final Set<String> pkgSet = launcher.getPkgProviderMap().keySet();
		final ArrayList<IconCompound> iconCompList = new ArrayList<IconCompound>();
		final ArrayList<ShortcutInfo>  itemInfoList = LauncherModel.getAllShortcutsAndApps();
		IconCache mIconCache = ((LauncherApplication) getApplicationContext()).mIconCache;
		boolean isChecked = mSharedPrefs.getBoolean(LauncherSettings.WIDGET_WHOLE_SWITCH, true);
		wholeSwitch.setChecked(isChecked);
		for(String pkgName : pkgSet){
			for(ShortcutInfo mInfo : itemInfoList){
				  if( mInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION 
						&& pkgName.equals(mInfo.getPackageName()) ){
					  IconCompound iconCompItem = new IconCompound();
					  iconCompItem.title = mInfo.title.toString();
					  iconCompItem.pkgName = mInfo.getPackageName();
					  iconCompItem.simplePinYin = mInfo.simplePinyin == null? "#":mInfo.simplePinyin;
					  iconCompItem.enable = isChecked;
					  iconCompItem.icon = resizeImage(mInfo.getIcon(mIconCache), appWidgetIconSize, appWidgetIconSize);
					if( mInfo.enableWidgets == 1 ){
						iconCompItem.isChecked = true;
					}else{
						iconCompItem.isChecked = false;
					}
					iconCompList.add(iconCompItem);
				  }
			}
		}
		final IconCompound multiItems[] = new IconCompound[iconCompList.size()];
	    iconCompList.toArray(multiItems);
		Arrays.sort(multiItems, getAppComparator());
		
		widgetsListAdapter = new WidgetsListAdapter(launcher ,multiItems);
		widgetsList.setAdapter(widgetsListAdapter);
		
		// for listview height wrap_content and getView() in adapter not be invoked duplicate
		int itemCount = widgetsList.getCount();
        widgetsList.getLayoutParams().height = itemCount * listItemHeight + listBorderHeight * (itemCount-1);
        
        Runnable scrollViewRunable = new Runnable() {  
            @Override  
            public void run() {  
             ((ScrollView) WidgetsManagerActivity.this.findViewById(R.id.app_widget_list_scroll)).scrollTo(0, 0);
            }  
          };  

        mHandler.post(scrollViewRunable); 
		
		/*if( isChecked ){
			widgetsTitle.setVisibility(View.VISIBLE);
			widgetsList.setVisibility(View.VISIBLE);
		}else{
			widgetsTitle.setVisibility(View.GONE);
			widgetsList.setVisibility(View.GONE);
			
		}*/
		int resId = isChecked ? R.string.widgets_tips_on : R.string.widgets_tips_off;
		widgetsTips.setText(resId);
		/*wholeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
			    public void onCheckedChanged(CompoundButton mSwitch, boolean isChecked) {
					// TODO Auto-generated method stub
					mSharedPrefs.edit().putBoolean(LauncherSettings.WIDGET_WHOLE_SWITCH, isChecked).commit();
					LauncherSettings.isEnableWidgets = isChecked;
					widgetsTips.setText( (isChecked ? R.string.widgets_tips_on : R.string.widgets_tips_off) );
					if( isChecked ){
						widgetsTitle.setVisibility(View.VISIBLE);
						widgetsList.setVisibility(View.VISIBLE);
					}else{
						widgetsTitle.setVisibility(View.GONE);
						widgetsList.setVisibility(View.GONE);
					}
					for( int m=0; m < multiItems.length; m++){
						multiItems[m].enable = isChecked;
					}
					widgetsListAdapter.notifyDataSetChanged();
				}});*/
		switchParent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				boolean isChecked = !wholeSwitch.isChecked();
				wholeSwitch.setChecked(isChecked);
				mSharedPrefs.edit().putBoolean(LauncherSettings.WIDGET_WHOLE_SWITCH, isChecked).commit();
				LauncherSettings.isEnableWidgets = isChecked;
				widgetsTips.setText( (isChecked ? R.string.widgets_tips_on : R.string.widgets_tips_off) );
				/*if( isChecked ){
					widgetsTitle.setVisibility(View.VISIBLE);
					widgetsList.setVisibility(View.VISIBLE);
				}else{
					widgetsTitle.setVisibility(View.GONE);
					widgetsList.setVisibility(View.GONE);
				}*/
				for( int m=0; m < multiItems.length; m++){
					multiItems[m].enable = isChecked;
				}
				widgetsListAdapter.notifyDataSetChanged();
			}
		});
	 }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			if (mFromApp) {
				overridePendingTransition(R.anim.activity_close_enter,
						R.anim.activity_close_exit);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mFromApp) {
			finish();
			overridePendingTransition(R.anim.activity_close_enter,
					R.anim.activity_close_exit);
		}

		return super.onKeyDown(keyCode, event);
	}

}
