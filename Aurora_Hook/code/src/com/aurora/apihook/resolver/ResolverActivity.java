package com.aurora.apihook.resolver;

import com.android.internal.R;
import com.android.internal.content.PackageMonitor;
import com.aurora.internal.app.AuroraAlertController;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import aurora.app.AuroraAlertActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ResolverActivity extends AuroraAlertActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "ResolverActivity";
    private static final boolean DEBUG = false;

    private int mLaunchedFromUid;
    private ResolveListAdapter mAdapter;
    private PackageManager mPm;
    private boolean mAlwaysUseOption;
    private boolean mShowExtended;
    private GridView mGrid;
    private Button mAlwaysButton;
    private Button mOnceButton;
    private int mIconDpi;
    private int mIconSize;
    private int mMaxColumns;
    private int mLastSelected = GridView.INVALID_POSITION;


    private boolean mRegistered;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        @Override public void onSomePackagesChanged() {
            mAdapter.handlePackagesChanged();
        }
    };

    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        // The resolver activity is set to be hidden from recent tasks.
        // we don't want this attribute to be propagated to the next activity
        // being launched.  Note that if the original Intent also had this
        // flag set, we are now losing it.  That should be a very rare case
        // and we can live with this.
        intent.setFlags(intent.getFlags()&~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, makeMyIntent(),
                getResources().getText(com.android.internal.R.string.whichApplication),
                null, null, true);
    }

    protected void onCreate(Bundle savedInstanceState, Intent intent,
            CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList,
            boolean alwaysUseOption) {
		auroraOnCreate( savedInstanceState,  intent,
             title, initialIntents,  rList,
             alwaysUseOption);
    }
	
	
	protected void auroraOnCreate(Bundle savedInstanceState, Intent intent,
            CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList,
            boolean alwaysUseOption) {
        //setTheme(R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
                
        super.onCreate(savedInstanceState);
        
        try {
            mLaunchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(
                    getActivityToken());
        } catch (RemoteException e) {
            mLaunchedFromUid = -1;
        }
        mPm = getPackageManager();
        mAlwaysUseOption = alwaysUseOption;
        //mMaxColumns = getResources().getInteger(R.integer.config_maxResolverActivityColumns);
		
		auroraSetAlwaysUseOption();
        auroraSetColumns();
        
        intent.setComponent(null);
		
        AuroraAlertController.AlertParams ap = mAlertParams;
		 
        ap.mTitle = title;
		 
        mPackageMonitor.register(this, getMainLooper(), false);
        mRegistered = true;
		
        final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mIconDpi = am.getLauncherLargeIconDensity();
        mIconSize = am.getLauncherLargeIconSize();
		auroraSetImageSize();
        mAdapter = new ResolveListAdapter(this, intent, initialIntents, rList,
                mLaunchedFromUid);
       
        int count = mAdapter.getCount();
        
        if (mLaunchedFromUid < 0 || UserHandle.isIsolated(mLaunchedFromUid)) {
            // Gulp!
            finish();
            return;
        } else if (count > 1) {
		
         
			auroraInitGridView();
			
            mGrid.setAdapter(mAdapter);
            mGrid.setOnItemClickListener(this);
            mGrid.setOnItemLongClickListener(new ItemLongClickListener());
			
            if (alwaysUseOption) {
                mGrid.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            }
			
            resizeGrid();
        } else if (count == 1) {
            startActivity(mAdapter.intentForPosition(0));
            mPackageMonitor.unregister();
            mRegistered = false;
            finish();
            return;
        } else {
            ap.mMessage = getResources().getText(com.aurora.R.string.aurora_no_application_can_perform_this_operation);
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        setupAlert();
		
		auroraSetBottomBar();
    }
    
    void resizeGrid() {
        final int itemCount = mAdapter.getCount();
        mGrid.setNumColumns(Math.min(itemCount, mMaxColumns));
    }

    Drawable getIcon(Resources res, int resId) {
        Drawable result;
        try {
            result = res.getDrawableForDensity(resId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            result = null;
        }

        return result;
    }

    Drawable loadIconForResolveInfo(ResolveInfo ri) {
        Drawable dr;
        try {
            if (ri.resolvePackageName != null && ri.icon != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return dr;
                }
            }
            final int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Couldn't find resources for package", e);
        }
        return ri.loadIcon(mPm);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!mRegistered) {
            mPackageMonitor.register(this, getMainLooper(), false);
            mRegistered = true;
        }
        mAdapter.handlePackagesChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRegistered) {
            mPackageMonitor.unregister();
            mRegistered = false;
        }
        if ((getIntent().getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            // This resolver is in the unusual situation where it has been
            // launched at the top of a new task.  We don't let it be added
            // to the recent tasks shown to the user, and we need to make sure
            // that each time we are launched we get the correct launching
            // uid (not re-using the same resolver from an old launching uid),
            // so we will now finish ourself since being no longer visible,
            // the user probably can't get back to us.
            if (!isChangingConfigurations()) {
                finish();
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mAlwaysUseOption) {
            final int checkedPos = mGrid.getCheckedItemPosition();
            final boolean enabled = checkedPos != GridView.INVALID_POSITION;
            mLastSelected = checkedPos;
            mAlwaysButton.setEnabled(enabled);
            mOnceButton.setEnabled(enabled);
            if (enabled) {
                mGrid.setSelection(checkedPos);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int checkedPos = mGrid.getCheckedItemPosition();
        final boolean hasValidSelection = checkedPos != GridView.INVALID_POSITION;
      
        if (mAlwaysUseOption && (!hasValidSelection || mLastSelected != checkedPos)) {
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                mGrid.smoothScrollToPosition(checkedPos);
            }
            mLastSelected = checkedPos;
        } else {
            startSelected(position, false);
        }
    }

    public void onButtonClick(View v) {
		/*
        final int id = v.getId();
        startSelected(mGrid.getCheckedItemPosition(), id == R.id.button_always);
        dismiss();
        */
       auroraOnButtonClick(v); 
    }

    void startSelected(int which, boolean always) {
        ResolveInfo ri = mAdapter.resolveInfoForPosition(which);
        Intent intent = mAdapter.intentForPosition(which);
        onIntentSelected(ri, intent, always);
        finish();
    }

    protected void onIntentSelected(ResolveInfo ri, Intent intent, boolean alwaysCheck) {
        if (alwaysCheck) {
            // Build a reasonable intent filter, based on what matched.
            IntentFilter filter = new IntentFilter();

            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            int cat = ri.match&IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = intent.getData();
            if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = intent.resolveType(this);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        Log.w("ResolverActivity", e);
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null) {
                // We need the data specification if there was no type,
                // OR if the scheme is not one of our magical "file:"
                // or "content:" schemes (see IntentFilter for the reason).
                if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme())
                                && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());

                    // Look through the resolved filter to determine which part
                    // of it matched the original Intent.
                    Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                    if (aIt != null) {
                        while (aIt.hasNext()) {
                            IntentFilter.AuthorityEntry a = aIt.next();
                            if (a.match(data) >= 0) {
                                int port = a.getPort();
                                filter.addDataAuthority(a.getHost(),
                                        port >= 0 ? Integer.toString(port) : null);
                                break;
                            }
                        }
                    }
                    Iterator<PatternMatcher> pIt = ri.filter.pathsIterator();
                    if (pIt != null) {
                        String path = data.getPath();
                        while (path != null && pIt.hasNext()) {
                            PatternMatcher p = pIt.next();
                            if (p.match(path)) {
                                filter.addDataPath(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                }
            }

            if (filter != null) {
                final int N = mAdapter.mList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i=0; i<N; i++) {
                    ResolveInfo r = mAdapter.mList.get(i).ri;
                    set[i] = new ComponentName(r.activityInfo.packageName,
                            r.activityInfo.name);
                    if (r.match > bestMatch) bestMatch = r.match;
                }
                getPackageManager().addPreferredActivity(filter, bestMatch, set,
                        intent.getComponent());
            }
        }

        if (intent != null) {
			startActivity(intent);
        }
    }

    void showAppDetails(ResolveInfo ri) {
        Intent in = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
                .setData(Uri.fromParts("package", ri.activityInfo.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(in);
    }

    private final class DisplayResolveInfo {
        ResolveInfo ri;
        CharSequence displayLabel;
        Drawable displayIcon;
        CharSequence extendedInfo;
        Intent origIntent;

        DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel,
                CharSequence pInfo, Intent pOrigIntent) {
            ri = pri;
            displayLabel = pLabel;
            extendedInfo = pInfo;
            origIntent = pOrigIntent;
        }
    }

    private final class ResolveListAdapter extends BaseAdapter {
        private final Intent[] mInitialIntents;
        private final List<ResolveInfo> mBaseResolveList;
        private final Intent mIntent;
        private final int mLaunchedFromUid;
        private final LayoutInflater mInflater;

        private List<DisplayResolveInfo> mList;

        public ResolveListAdapter(Context context, Intent intent,
                Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid) {
            mIntent = new Intent(intent);
            mIntent.setComponent(null);
            mInitialIntents = initialIntents;
            mBaseResolveList = rList;
            mLaunchedFromUid = launchedFromUid;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mList = new ArrayList<DisplayResolveInfo>();
            rebuildList();
            
            auroraSetDrawableCash();
        }

        public void handlePackagesChanged() {
            final int oldItemCount = getCount();
            rebuildList();
            notifyDataSetChanged();
            final int newItemCount = getCount();
            if (newItemCount == 0) {
                // We no longer have any items...  just finish the activity.
                finish();
            } else if (newItemCount != oldItemCount) {
                resizeGrid();
            }
        }

        private void rebuildList() {
            List<ResolveInfo> currentResolveList;

            mList.clear();
            if (mBaseResolveList != null) {
                currentResolveList = mBaseResolveList;
            } else {
                currentResolveList = mPm.queryIntentActivities(
                        mIntent, PackageManager.MATCH_DEFAULT_ONLY
                        | (mAlwaysUseOption ? PackageManager.GET_RESOLVED_FILTER : 0));
                // Filter out any activities that the launched uid does not
                // have permission for.  We don't do this when we have an explicit
                // list of resolved activities, because that only happens when
                // we are being subclassed, so we can safely launch whatever
                // they gave us.
                if (currentResolveList != null) {
                    for (int i=currentResolveList.size()-1; i >= 0; i--) {
                        ActivityInfo ai = currentResolveList.get(i).activityInfo;
                        int granted = ActivityManager.checkComponentPermission(
                                ai.permission, mLaunchedFromUid,
                                ai.applicationInfo.uid, ai.exported);
                        if (granted != PackageManager.PERMISSION_GRANTED) {
                            // Access not allowed!
                            currentResolveList.remove(i);
                        }
                    }
                }
            }
            int N;
            if ((currentResolveList != null) && ((N = currentResolveList.size()) > 0)) {
                // Only display the first matches that are either of equal
                // priority or have asked to be default options.
                ResolveInfo r0 = currentResolveList.get(0);
                for (int i=1; i<N; i++) {
                    ResolveInfo ri = currentResolveList.get(i);
                    if (DEBUG) Log.v(
                        "ResolveListActivity",
                        r0.activityInfo.name + "=" +
                        r0.priority + "/" + r0.isDefault + " vs " +
                        ri.activityInfo.name + "=" +
                        ri.priority + "/" + ri.isDefault);
                    if (r0.priority != ri.priority ||
                        r0.isDefault != ri.isDefault) {
                        while (i < N) {
                            currentResolveList.remove(i);
                            N--;
                        }
                    }
                }
                if (N > 1) {
                    ResolveInfo.DisplayNameComparator rComparator =
                            new ResolveInfo.DisplayNameComparator(mPm);
                    Collections.sort(currentResolveList, rComparator);
                }
                // First put the initial items at the top.
                if (mInitialIntents != null) {
                    for (int i=0; i<mInitialIntents.length; i++) {
                        Intent ii = mInitialIntents[i];
                        if (ii == null) {
                            continue;
                        }
                        ActivityInfo ai = ii.resolveActivityInfo(
                                getPackageManager(), 0);
                        if (ai == null) {
                            Log.w("ResolverActivity", "No activity found for "
                                    + ii);
                            continue;
                        }
                        ResolveInfo ri = new ResolveInfo();
                        ri.activityInfo = ai;
                        if (ii instanceof LabeledIntent) {
                            LabeledIntent li = (LabeledIntent)ii;
                            ri.resolvePackageName = li.getSourcePackage();
                            ri.labelRes = li.getLabelResource();
                            ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                            ri.icon = li.getIconResource();
                        }
                        mList.add(new DisplayResolveInfo(ri,
                                ri.loadLabel(getPackageManager()), null, ii));
                    }
                }

                // Check for applications with same name and use application name or
                // package name if necessary
                r0 = currentResolveList.get(0);
                int start = 0;
                CharSequence r0Label =  r0.loadLabel(mPm);
                mShowExtended = false;
                for (int i = 1; i < N; i++) {
                    if (r0Label == null) {
                        r0Label = r0.activityInfo.packageName;
                    }
                    ResolveInfo ri = currentResolveList.get(i);
                    CharSequence riLabel = ri.loadLabel(mPm);
                    if (riLabel == null) {
                        riLabel = ri.activityInfo.packageName;
                    }
                    if (riLabel.equals(r0Label)) {
                        continue;
                    }
                    processGroup(currentResolveList, start, (i-1), r0, r0Label);
                    r0 = ri;
                    r0Label = riLabel;
                    start = i;
                }
                // Process last group
                processGroup(currentResolveList, start, (N-1), r0, r0Label);
            }
        }

        private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro,
                CharSequence roLabel) {
            // Process labels from start to i
            int num = end - start+1;
            if (num == 1) {
                // No duplicate labels. Use label for entry at start
                mList.add(new DisplayResolveInfo(ro, roLabel, null, null));
            } else {
                mShowExtended = true;
                boolean usePkg = false;
                CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(mPm);
                if (startApp == null) {
                    usePkg = true;
                }
                if (!usePkg) {
                    // Use HashSet to track duplicates
                    HashSet<CharSequence> duplicates =
                        new HashSet<CharSequence>();
                    duplicates.add(startApp);
                    for (int j = start+1; j <= end ; j++) {
                        ResolveInfo jRi = rList.get(j);
                        CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(mPm);
                        if ( (jApp == null) || (duplicates.contains(jApp))) {
                            usePkg = true;
                            break;
                        } else {
                            duplicates.add(jApp);
                        }
                    }
                    // Clear HashSet for later use
                    duplicates.clear();
                }
                for (int k = start; k <= end; k++) {
                    ResolveInfo add = rList.get(k);
                    if (usePkg) {
                        // Use application name for all entries from start to end-1
                        mList.add(new DisplayResolveInfo(add, roLabel,
                                add.activityInfo.packageName, null));
                    } else {
                        // Use package name for all entries from start to end-1
                        mList.add(new DisplayResolveInfo(add, roLabel,
                                add.activityInfo.applicationInfo.loadLabel(mPm), null));
                    }
                }
            }
        }

        public ResolveInfo resolveInfoForPosition(int position) {
            return mList.get(position).ri;
        }

        public Intent intentForPosition(int position) {
            DisplayResolveInfo dri = mList.get(position);
            
            Intent intent = new Intent(dri.origIntent != null
                    ? dri.origIntent : mIntent);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                    |Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            ActivityInfo ai = dri.ri.activityInfo;
            intent.setComponent(new ComponentName(
                    ai.applicationInfo.packageName, ai.name));
            return intent;
        }

        public int getCount() {
            return mList.size();
        }

        public Object getItem(int position) {
            return mList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
           return auroraGetView(position,convertView,parent);
        }
		
		private View auroraGetView(int position, View convertView, ViewGroup parent) {
            View view;
            auroraPosition = position;
          //  Log.e("liuwei","auroraGetView position = "  + position);
            if (convertView == null) {
                view = mInflater.inflate(
                       com.aurora.R.layout.aurora_resolve_list_item, parent, false);
                       
				ImageView icon = (ImageView)view.findViewById(com.aurora.R.id.aurora_icon);
				
                ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) icon.getLayoutParams();
                
                lp.width = lp.height = auroraImageSize;
                
                if(auroraAlwaysUseOption)
				{
					view.setBackgroundResource(com.aurora.R.drawable.aurora_activity_picker_bg);
				}
				
            } else {
                view = convertView;
            }
            
            bindView(view, mList.get(position));
            return view;
        }
        
        private final void bindView(View view, DisplayResolveInfo info) {
			auroraBindView(view, info); 
        }
        
        private final void auroraBindView(View view, DisplayResolveInfo info) {
            TextView text = (TextView)view.findViewById(com.aurora.R.id.aurora_text1);
            
            TextView text2 = (TextView)view.findViewById(com.aurora.R.id.aurora_text2);
            
            ImageView icon = (ImageView)view.findViewById(com.aurora.R.id.aurora_icon);
            
            text.setText(info.displayLabel);
            
            text2.setVisibility(View.GONE);
            if(auroraPosition == 0 && icon.getDrawable() != null)
			{
				return;
			}
			
			if(auroraPosition == 0 && icon.getDrawable() == null)
			{
				info.displayIcon = 	auroraGetImageDrawable(loadIconForResolveInfo(info.ri));
			}
			
            if (info.displayIcon == null) {
                info.displayIcon = loadIconForResolveInfo(info.ri);
            }
          
			icon.setImageDrawable(info.displayIcon);
		
        }
         
	    private void auroraSetDrawableCash()
		{

			if(mList != null)
			{
		
				for(int i = 0 ; i < mList.size(); i++)
				{
					DisplayResolveInfo info = mList.get(i);
					
					if(info.displayIcon == null)
					{
						info.displayIcon = loadIconForResolveInfo(info.ri);
					}
					
					info.displayIcon = auroraGetImageDrawable(info.displayIcon);
					
				}
			}
		}
    }
    
	
    class ItemLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ResolveInfo ri = mAdapter.resolveInfoForPosition(position);
            showAppDetails(ri);
            return true;
        }

    }
    
     /************************************************************
     * 															*
     * 															*				
     * 				IUNI add for shared control					*
     * 															*
     * 															*	
     * ************************************************************/
	public int auroraPosition;
	
	public boolean auroraAlwaysUseOption = false;
	
	public int auroraImageSize;
	
    @Override
    protected void onResume()
    {
		super.onResume();
		
		auroraInitAll();
	}
    
    @Override
    protected void onPause()
    {
		super.onPause();
		
		finish();
	}
	
    public void auroraInitAll()
    {
    	auroraResetWindowAttributes();
    	
    	auroraResetContentPadding();
    } 
    
    public void auroraResetWindowAttributes()
    {
    	android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
    
    
    	int screenWidth  = getWindowManager().getDefaultDisplay().getWidth(); 
    	
    	lp.width = screenWidth - getResources().getInteger(com.aurora.internal.R.integer.auroraSharedWindowPaddings);
		
    	if(mAdapter != null && getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT)
    	{
			final int maxHeightPx = (int)(getResources().getDimension(com.aurora.R.dimen.aurora_resolver_window_max_height));
			
			final int size = mAdapter.getCount();
			
			final int lines = (size-1)/mMaxColumns + 1;
			
			if( lines > 2 )
			{
				lp.height = maxHeightPx;
			}
		}
    	
    	getWindow().setAttributes(lp);
    	
    }
    
    public void auroraResetContentPadding()
    {
    	View parent = getWindow().findViewById(com.aurora.internal.R.id.aurora_custom);
    	
    	parent.setPadding(0, 0, 0, 0);
    	
    }
    
    public void auroraSetColumns()	
    {
		mMaxColumns = getResources().getInteger(com.aurora.internal.R.integer.auroraSharedGridViewMaxColumns);
	}
	
	
	public void auroraSetImageSize()
	{
		auroraImageSize = mIconSize + 12;
	}
	
	public View auroraGetResolverView()
	{
		return getLayoutInflater().inflate(com.aurora.R.layout.aurora_resolver_grid, null);
	}
	
	public GridView auroraSetGridView(View v)
	{
		if(v == null)return null;
		
		return (GridView)v.findViewById(com.aurora.R.id.aurora_resolver_grid); 
	}
	
	
	public void auroraOnButtonClick(View v) {
		
        final int id = v.getId();
        
        startSelected(mGrid.getCheckedItemPosition(), id == com.aurora.R.id.aurora_button_always);
      
        dismiss();
    }
	
	public android.graphics.Bitmap auroraDrawableToBitmap(Drawable drawable, int alpha) {       
		
		if(drawable == null)return null;
		
		int bitmapWidth = 180;
		
		int bitmapHeight = 180;
		
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(

                                        bitmapWidth,

                                        bitmapHeight,

                                        drawable.getOpacity() != android.graphics.PixelFormat.OPAQUE ?
                                        				android.graphics.Bitmap.Config.ARGB_8888

                                                        : android.graphics.Bitmap.Config.RGB_565
                                                        );

        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
		
        drawable.setBounds(0, 0, bitmapWidth, bitmapHeight);
        
        drawable.setAlpha(alpha);
        
        drawable.draw(canvas);

        return bitmap;
	}
	

	public android.graphics.Bitmap auroraGetRoundedCornerBitmap(android.graphics.Bitmap bitmap) 
	{
		if(bitmap == null)return null;
		
		int bitmapWidth = 180;
		
		int bitmapHeight = 180;
		
	    android.graphics.Bitmap output = android.graphics.Bitmap.createBitmap(bitmapWidth,
	        bitmapHeight, android.graphics.Bitmap.Config.ARGB_8888);
	        
	    android.graphics.Canvas canvas = new android.graphics.Canvas(output);
	 
	    final android.graphics.Paint paint = new android.graphics.Paint();
	    
	    final android.graphics.Rect rect = new android.graphics.Rect(0, 0, bitmapWidth, bitmapHeight);
	    
	    final android.graphics.RectF rectF = new android.graphics.RectF(rect);
	    
	    final float roundPx = 10;
	 
	    paint.setAntiAlias(true);
	    
	    canvas.drawARGB(0, 0, 0, 0);
	    
	    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	 
	    paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
	    
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	 
	    return output;
	}
	
	public android.graphics.drawable.StateListDrawable auroraGetImageDrawable(Drawable src)
	{
		android.graphics.drawable.StateListDrawable auroraImageDrawable = new android.graphics.drawable.StateListDrawable();
		
		android.graphics.Bitmap auroraImageBitmapPressed = auroraGetRoundedCornerBitmap(auroraDrawableToBitmap(src,100));
		
		android.graphics.Bitmap auroraImageBitmap = auroraGetRoundedCornerBitmap(auroraDrawableToBitmap(src,255));
		
		auroraImageDrawable.addState(new int[]{android.R.attr.state_pressed}, new android.graphics.drawable.BitmapDrawable(auroraImageBitmapPressed));
		
		auroraImageDrawable.addState(new int[]{0}, new android.graphics.drawable.BitmapDrawable(auroraImageBitmap));
		
		return auroraImageDrawable;
	}
	
	public void auroraSetImageDrawable(ImageView icon,Drawable src)
	{
		if(icon == null) return;
		
		//Log.e("liuwei","auroraPosition = " + auroraPosition);
		
		if(icon.getDrawable() == null || icon.getDrawable() != src)
		{
			//Log.e("liuwei","icon.getDrawable() == null");
			icon.setImageDrawable(auroraGetImageDrawable(src));
		}
	}
	
	
	public ViewGroup auroraGetButtomBarLayout()
	{
		return (ViewGroup) findViewById(com.aurora.R.id.aurora_button_bar);
	}
	
	public void auroraSetButtonIds()
	{
		 ViewGroup buttonLayout = auroraGetButtomBarLayout();
		 
		 mAlwaysButton = (Button) buttonLayout.findViewById(com.aurora.R.id.aurora_button_always);
		 
         mOnceButton = (Button) buttonLayout.findViewById(com.aurora.R.id.aurora_button_once);
	}
	
	public void auroraShowCancelButton()
	{
		AuroraAlertController.AlertParams ap = mAlertParams;
		
		if(ap != null && ap.mView != null)	
		{
			Button auroracancel = (Button)ap.mView.findViewById(com.aurora.R.id.aurora_resolve_cancel_button); 
            
            if(auroracancel != null)
            {
				auroracancel.setOnClickListener(auroraCancelLisener);
			
				auroracancel.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void auroraInitGridView()
	{
		AuroraAlertController.AlertParams ap = mAlertParams;
		
		//ap.mView = getLayoutInflater().inflate(R.layout.resolver_grid, null);
            
        //reset
        ap.mView = auroraGetResolverView();
          
        //mGrid = (GridView) ap.mView.findViewById(R.id.resolver_grid);
            
        //reset
        mGrid = auroraSetGridView(ap.mView);
          
	}
	
	public void auroraSetAlwaysUseOption()
	{
		auroraAlwaysUseOption = mAlwaysUseOption;
	}
	
	public void auroraSetBottomBar()
	{

		if (auroraAlwaysUseOption) 
        {
			
            final ViewGroup buttonLayout = auroraGetButtomBarLayout();
          
            if (buttonLayout != null) {
				
                buttonLayout.setVisibility(View.VISIBLE);
                
                auroraSetButtonIds();
                
               
            } else {
				
                mAlwaysUseOption = false;
            }
        }
        else
        {
			auroraShowCancelButton();
		}
	}
	
	public AuroraClickListener auroraCancelLisener = new AuroraClickListener();
	
	private class AuroraClickListener implements android.widget.Button.OnClickListener
	{
		public AuroraClickListener()
		{
			
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
				
	}
	
// Aurora <Luofu> <2014-5-27> modify for dialog begin
    
    private void auroraSetParams(){
       // p.auroraFromAlertActivity = true;
    }
    
 // Aurora <Luofu> <2014-5-27> modify for dialog end
	
}
