package com.aurora.frameworkdemo;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.aurora.frameworkdemo.dashboard.DashboardCategory;
import com.aurora.frameworkdemo.dashboard.DashboardTile;
import com.aurora.frameworkdemo.dashboard.DashboardTileView;
import static com.aurora.frameworkdemo.dashboard.DashboardTile.TILE_ID_UNDEFINED;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar.Type;
import aurora.widget.AuroraFrameAnimation;
import com.android.internal.util.XmlUtils;
public class MainActivity extends AuroraActivity implements OnItemClickListener{
	
	private ListView mMenuList;
    private LayoutInflater mLayoutInflater;
    private static int[] mPreferenceBackgroundRes;
    // Categories
    private ArrayList<DashboardCategory> mCategories = new ArrayList<DashboardCategory>();
    private final int FRAME_LIST_BACKGROUND_NULL = 0;
    private final int FRAME_LIST_BACKGROUND_FULL = 1;
    private final int FRAME_LIST_BACKGROUND_TOP = 2;
    private final int FRAME_LIST_BACKGROUND_MIDDLE = 3;
    private final int FRAME_LIST_BACKGROUND_BOTTOM = 4;
    private final int FRAME_LIST_BACKGROUND_TOTAL = 5;

    private void getFrameListBackground(Context context) {
        mPreferenceBackgroundRes = new int[FRAME_LIST_BACKGROUND_TOTAL];
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_NULL] = 0;
        TypedValue outValue = new TypedValue();

        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_FULL] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListTopBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_TOP] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListMiddleBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_MIDDLE] = outValue.resourceId;
        context.getTheme().resolveAttribute(com.aurora.R.attr.auroraframeListBottomBackground,
                outValue, true);
        mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_BOTTOM] = outValue.resourceId;

    }
    
    private static final int MSG_REBUILD_UI = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REBUILD_UI: {
                    final Context context = MainActivity.this;
                    rebuildUI(context);
                } break;
            }
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.activity_main, Type.Normal);
		mMenuList = (ListView)findViewById(R.id.menu_page_list);
		mLayoutInflater = LayoutInflater.from(this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 sendRebuildUI();
	}
	
	  private void sendRebuildUI() {
	        if (!mHandler.hasMessages(MSG_REBUILD_UI)) {
	            mHandler.sendEmptyMessage(MSG_REBUILD_UI);
	        }
	    }
	  

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}
	   class DashBoardCatAdapter extends BaseAdapter{
	    	 List<DashboardCategory> categories;
	    	 Context context;
	    	public DashBoardCatAdapter( List<DashboardCategory> cates,Context cxt){
	    		this.categories = cates;
	    		context = cxt;
	    	}
	    	
	    	@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return categories.size();
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return categories.get(position);
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
			            DashboardCategory category = categories.get(position);
			            final Resources res = getResources();
			            View categoryView = mLayoutInflater.inflate(R.layout.aurora_dashboard_category, mMenuList,
			                    false);
			            if(position == 0){
			            	categoryView.setPadding(categoryView.getPaddingLeft(), res.getDimensionPixelSize(R.dimen.dashboard_page_margin_top), 
			            			categoryView.getPaddingRight(), categoryView.getPaddingBottom());
			            }else if(position == categories.size()-1){
			            	categoryView.setPadding(categoryView.getPaddingLeft(), categoryView.getPaddingTop(), 
			            			categoryView.getPaddingRight(), res.getDimensionPixelSize(R.dimen.dashboard_page_margin_bottom));
			            }
			            
			            TextView categoryLabel = (TextView) categoryView.findViewById(R.id.category_title);
			            categoryLabel.setText(category.getTitle(res));

			            ViewGroup categoryContent =
			                    (ViewGroup) categoryView.findViewById(R.id.category_content);

			            final int tilesCount = category.getTilesCount();
			            Log.d("cat", "size 2:"+tilesCount);
			            for (int i = 0; i < tilesCount; i++) {
			                DashboardTile tile = category.getTile(i);
			                DashboardTileView tileView ;
			                 tileView = new DashboardTileView(context);
			                updateTileView(context, res, tile, tileView.getImageView(),
			                        tileView.getTitleTextView(), tileView.getStatusTextView(),tileView);
			                    int mres = 0;
							if (tilesCount == 1) {
								mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_FULL];
							} else {
								if (i == 0) {
									mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_TOP];
								} else if (i == (tilesCount - 1)) {
									mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_BOTTOM];
								} else {
									mres = mPreferenceBackgroundRes[FRAME_LIST_BACKGROUND_MIDDLE];
								}
							}
			                    tileView.setBackgroundResource(mres);

			                tileView.setTile(tile);
			                categoryContent.addView(tileView);
			            }

				return categoryView;
			}
			
	    	
	    	
	    	
	    }
	
	   

	    private void updateTileView(Context context, Resources res, DashboardTile tile,
	            ImageView tileIcon, TextView tileTextView, TextView statusTextView,DashboardTileView titleView) {

	        if (tile.iconRes > 0) {
	            tileIcon.setImageResource(tile.iconRes);
	        } 
	        

	        ///M: feature replace sim to uim
	        tileTextView.setText(tile.getTitle(res).toString());
	        Log.d("cat", ""+tileTextView.getText());
	        CharSequence summary = tile.getSummary(res);
	        if (!TextUtils.isEmpty(summary)) {
	            statusTextView.setVisibility(View.VISIBLE);
	            statusTextView.setText(summary);
	        } else {
	            statusTextView.setVisibility(View.GONE);
	        }
	    }
	   
	    
	    
	    
	    
	    
	    private void rebuildUI(Context context) {

	        getFrameListBackground(context);

	        long start = System.currentTimeMillis();
	        final Resources res = getResources();


	        List<DashboardCategory> categories =getDashboardCategories(true);

	        final int count = categories.size();
	        Log.d("cat", "size-->"+categories.size());
	        DashBoardCatAdapter adapter = new DashBoardCatAdapter(categories, this);
	        Log.d("cat", "test cat-->"+categories.get(0).getTilesCount());
	        mMenuList.setAdapter(adapter);
//	        for (int n =
	        long delta = System.currentTimeMillis() - start;
	    }
	    
	    public List<DashboardCategory> getDashboardCategories(boolean forceRefresh) {
	        if (forceRefresh || mCategories.size() == 0) {
	            buildDashboardCategories(mCategories);
	        }
	      
	        return mCategories;
	    }
	    
	    /**
	     * Called when the activity needs its list of categories/tiles built.
	     *
	     * @param categories The list in which to place the tiles categories.
	     */
	    private void buildDashboardCategories(List<DashboardCategory> categories) {
	        categories.clear();
	        loadCategoriesFromResource(R.xml.dashboard_categories, categories);
	    }
	    
	    
	    /**
	     * Parse the given XML file as a categories description, adding each
	     * parsed categories and tiles into the target list.
	     *
	     * @param resid The XML resource to load and parse.
	     * @param target The list in which the parsed categories and tiles should be placed.
	     */
	    private void loadCategoriesFromResource(int resid, List<DashboardCategory> target) {
	        XmlResourceParser parser = null;
	        try {
	            parser = getResources().getXml(resid);
	            AttributeSet attrs = Xml.asAttributeSet(parser);

	            int type;
	            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
	                    && type != XmlPullParser.START_TAG) {
	                // Parse next until start tag is found
	            }

	            String nodeName = parser.getName();
	            if (!"dashboard-categories".equals(nodeName)) {
	                throw new RuntimeException(
	                        "XML document must start with <preference-categories> tag; found"
	                                + nodeName + " at " + parser.getPositionDescription());
	            }

	            Bundle curBundle = null;

	            final int outerDepth = parser.getDepth();
	            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
	                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
	                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
	                    continue;
	                }

	                nodeName = parser.getName();
	                if ("dashboard-category".equals(nodeName)) {
	                    DashboardCategory category = new DashboardCategory();

	                    TypedArray sa = obtainStyledAttributes(
	                            attrs, com.android.internal.R.styleable.PreferenceHeader);
	                    category.id = sa.getResourceId(
	                            com.android.internal.R.styleable.PreferenceHeader_id,
	                            (int)DashboardCategory.CAT_ID_UNDEFINED);

	                    TypedValue tv = sa.peekValue(
	                            com.android.internal.R.styleable.PreferenceHeader_title);
	                    if (tv != null && tv.type == TypedValue.TYPE_STRING) {
	                        if (tv.resourceId != 0) {
	                            category.titleRes = tv.resourceId;
	                        } else {
	                            category.title = tv.string;
	                        }
	                    }
	                    sa.recycle();

	                    final int innerDepth = parser.getDepth();
	                    while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
	                            && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
	                        if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
	                            continue;
	                        }

	                        String innerNodeName = parser.getName();
	                        if (innerNodeName.equals("dashboard-tile")) {
	                            DashboardTile tile = new DashboardTile();

	                            sa = obtainStyledAttributes(
	                                    attrs, com.android.internal.R.styleable.PreferenceHeader);
	                            tile.id = sa.getResourceId(
	                                    com.android.internal.R.styleable.PreferenceHeader_id,
	                                    (int)TILE_ID_UNDEFINED);
	                            
	                            tv = sa.peekValue(
	                                    com.android.internal.R.styleable.PreferenceHeader_title);
	                            if (tv != null && tv.type == TypedValue.TYPE_STRING) {
	                                if (tv.resourceId != 0) {
	                                    tile.titleRes = tv.resourceId;
	                                } else {
	                                    tile.title = tv.string;
	                                }
	                            }
	                            Log.d("cat", "title-->"+tile.title);
	                            tv = sa.peekValue(
	                                    com.android.internal.R.styleable.PreferenceHeader_summary);
	                            if (tv != null && tv.type == TypedValue.TYPE_STRING) {
	                                if (tv.resourceId != 0) {
	                                    tile.summaryRes = tv.resourceId;
	                                } else {
	                                    tile.summary = tv.string;
	                                }
	                            }
	                            tile.iconRes = sa.getResourceId(
	                                    com.android.internal.R.styleable.PreferenceHeader_icon, 0);
	                            tile.fragment = sa.getString(
	                                    com.android.internal.R.styleable.PreferenceHeader_fragment);
	                            sa.recycle();

	                            if (curBundle == null) {
	                                curBundle = new Bundle();
	                            }

	                            final int innerDepth2 = parser.getDepth();
	                            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
	                                    && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth2)) {
	                                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
	                                    continue;
	                                }

	                                String innerNodeName2 = parser.getName();
	                                if (innerNodeName2.equals("extra")) {
	                                    getResources().parseBundleExtra("extra", attrs, curBundle);
	                                    XmlUtils.skipCurrentTag(parser);

	                                } else if (innerNodeName2.equals("intent")) {
	                                    tile.intent = Intent.parseIntent(getResources(), parser, attrs);

	                                } else {
	                                    XmlUtils.skipCurrentTag(parser);
	                                }
	                            }

	                            if (curBundle.size() > 0) {
	                                tile.fragmentArguments = curBundle;
	                                curBundle = null;
	                            }

	                            category.addTile(tile);

	                        } else {
	                            XmlUtils.skipCurrentTag(parser);
	                        }
	                    }

	                    target.add(category);
	                } else {
	                    XmlUtils.skipCurrentTag(parser);
	                }
	            }

	        } catch (XmlPullParserException e) {
	            throw new RuntimeException("Error parsing categories", e);
	        } catch (IOException e) {
	            throw new RuntimeException("Error parsing categories", e);
	        } finally {
	            if (parser != null) parser.close();
	        }
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    
	    

}
