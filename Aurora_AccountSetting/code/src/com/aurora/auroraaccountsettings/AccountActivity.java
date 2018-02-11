package com.aurora.auroraaccountsettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.gionee.featureoption.FeatureOption;







//import com.gionee.settings.utils.GnUtils;
import android.os.UserHandle;

import com.android.internal.util.ArrayUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import aurora.preference.*;
import aurora.widget.AuroraSwitch;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

public class AccountActivity extends  AuroraPreferenceActivity 
implements  OnAccountsUpdateListener {

	public static final String ACTION_AURORA_ACCOUNT = "com.android.settings.action.AURORA_ACCOUNT";
	private AuthenticatorHelper mAuthenticatorHelper;
	public static int mListSeparatorHeight;
	private Header mFirstHeader;
	protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap<Integer, Integer>();
	private boolean mListeningToAccountUpdates;
	private AuroraActionBar auroraActionBar;
	private SharedPreferences mDevelopmentPreferences;
	private Header mLastHeader;
    private SharedPreferences.OnSharedPreferenceChangeListener mDevelopmentPreferencesListener;
    private static final String EXTRA_CLEAR_UI_OPTIONS = "settings:remove_ui_options";   
    private Header mCurrentHeader;
    private String mFragmentClass;
    private static final String SAVE_KEY_CURRENT_HEADER = "com.android.settings.CURRENT_HEADER";
    private static final String SAVE_KEY_PARENT_HEADER = "com.android.settings.PARENT_HEADER";
    private Header mParentHeader;
    private boolean mInLocalHeaderSwitch;
    private static final String META_DATA_KEY_FRAGMENT_CLASS =
            "com.android.settings.FRAGMENT_CLASS";
    private static final String META_DATA_KEY_PARENT_TITLE =
            "com.android.settings.PARENT_FRAGMENT_TITLE";
        private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS =
            "com.android.settings.PARENT_FRAGMENT_CLASS";
	
	private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
		public void onAuroraActionBarBackItemClicked(int itemId) {
			switch (itemId) {
			case -1:
				if(ManageAccountsSettings.isManageAccount){
    				Log.i("qy", "KeyEvent.KEYCODE_BACK");
    				Intent intent = new Intent("com.aurora.auroraaccountsettings.action.ACCOUNT_ACTIVITY");
    				
    				startActivity(intent);
    				
    				overridePendingTransition(R.anim.aurora_close_enter,R.anim.aurora_close_exit);
    				finish();
    				
				}else{
					finish();
					overridePendingTransition(R.anim.aurora_close_enter,R.anim.aurora_close_exit);
				}
				
				
				break;
			default:
				break;
			}
		}
	};
	
	// Show only these settings for restricted users
    private int[] SETTINGS_FOR_RESTRICTED = {
            
            R.id.account_settings,
            R.id.account_add
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(com.aurora.R.style.Theme_aurora_Light);
		// TODO Auto-generated method stub
		mListSeparatorHeight = (int) getResources().getDimension(R.dimen.list_separator_height);
		mAuthenticatorHelper = new AuthenticatorHelper();
        mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, null);
        mDevelopmentPreferences = getSharedPreferences(/*DevelopmentSettings.PREF_FILE*/"development",
                Context.MODE_PRIVATE);
        mInLocalHeaderSwitch = true;
		super.onCreate(savedInstanceState);
		Log.i("qy", "*****onCreate(Bundle savedInstanceState)*****");
		
		mInLocalHeaderSwitch = false;
		
		// set title
		auroraActionBar = getAuroraActionBar();
		auroraActionBar.setTitle(R.string.account_settings);  //test title
		auroraActionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
		
		// Retrieve any saved state
        if (savedInstanceState != null) {
            mCurrentHeader = savedInstanceState.getParcelable(SAVE_KEY_CURRENT_HEADER);
            mParentHeader = savedInstanceState.getParcelable(SAVE_KEY_PARENT_HEADER);
        }

        // If the current header was saved, switch to it
        if (savedInstanceState != null && mCurrentHeader != null) {
            //switchToHeaderLocal(mCurrentHeader);
            showBreadCrumbs(mCurrentHeader.title, null);
        }

        if (mParentHeader != null) {
            setParentTitle(mParentHeader.title, null, new OnClickListener() {
                public void onClick(View v) {
                    switchToParent(mParentHeader.fragment);
                }
            });
        }
        
        invalidateHeaders();
		
	}
	
	
	@Override
    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        // This is called from super.onCreate, isMultiPane() is not yet reliable
        // Do not use onIsHidingHeaders either, which relies itself on this method
        if (startingFragment != null && !onIsMultiPane()) {
            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getExtras();
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable("intent", superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, superIntent.getExtras());
            return modIntent;
        }
        return superIntent;
    }
	
	private void switchToHeaderLocal(Header header) {
        mInLocalHeaderSwitch = true;
        switchToHeader(header);
        mInLocalHeaderSwitch = false;
    }

    @Override
    public void switchToHeader(Header header) {
        if (!mInLocalHeaderSwitch) {
            mCurrentHeader = null;
            mParentHeader = null;
        }
        super.switchToHeader(header);
    }

    /**
     * AuroraSwitch to parent fragment and store the grand parent's info
     * @param className name of the activity wrapper for the parent fragment.
     */
    private void switchToParent(String className) {
        final ComponentName cn = new ComponentName(this, className);
        try {
            final PackageManager pm = getPackageManager();
            final ActivityInfo parentInfo = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);

            if (parentInfo != null && parentInfo.metaData != null) {
                String fragmentClass = parentInfo.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                CharSequence fragmentTitle = parentInfo.loadLabel(pm);
                Header parentHeader = new Header();
                parentHeader.fragment = fragmentClass;
                parentHeader.title = fragmentTitle;
                mCurrentHeader = parentHeader;

                switchToHeaderLocal(parentHeader);
//                highlightHeader(mTopLevelHeaderId);

                mParentHeader = new Header();
                mParentHeader.fragment
                        = parentInfo.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
                mParentHeader.title = parentInfo.metaData.getString(META_DATA_KEY_PARENT_TITLE);
            }
        } catch (NameNotFoundException nnfe) {
            Log.w("qy", "Could not find parent activity : " + className);
        }
    }
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current fragment, if it is the same as originally launched
        if (mCurrentHeader != null) {
            outState.putParcelable(SAVE_KEY_CURRENT_HEADER, mCurrentHeader);
        }
        if (mParentHeader != null) {
            outState.putParcelable(SAVE_KEY_PARENT_HEADER, mParentHeader);
        }
    }
	
	
	@Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // If it is not launched from history, then reset to top-level
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (mFirstHeader != null && !onIsHidingHeaders() && onIsMultiPane()) {
                switchToHeaderLocal(mFirstHeader);
            }
            getListView().setSelectionFromTop(0, 0);
        }
    }
	
	protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null) return mFragmentClass;

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) return null;

        /*if ("com.android.settings.ManageApplications".equals(intentClass)
                || "com.android.settings.RunningServices".equals(intentClass)
                || "com.android.settings.applications.StorageUse".equals(intentClass)) {
            // Old names of manage apps.
            intentClass = com.android.settings.applications.ManageApplications.class.getName();
        }*/

        return intentClass;
    }
	
	@Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();
            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }
	
	@Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
            int titleRes, int shortTitleRes) {
        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args,
                titleRes, shortTitleRes);

        // some fragments want to avoid split actionbar
        if (
                AccountSyncSettings.class.getName().equals(fragmentName) ||                
                
                ManageAccountsSettings.class.getName().equals(fragmentName) 
                ) {
            intent.putExtra(EXTRA_CLEAR_UI_OPTIONS, true);
        }

        intent.setClass(this, AuroraSubSettings.class);
        return intent;
    }
	
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		// TODO Auto-generated method stub
		super.onBuildHeaders(target);
		loadHeadersFromResource(R.xml.aurora_account_headers, target);
		updateHeaderList(target);        
        
        getFrameListBackground(AccountActivity.this);
        mPreferenceBackgroundIndexs = getPreferenceBackgroundIndexs(target);
	}

	
	private void updateHeaderList(List<Header> target) {
        
        int i = 0;

        mHeaderIndexMap.clear();
        while (i < target.size()) {
            Header header = target.get(i);
            // Ids are integers, so downcasting
            int id = (int) header.id;
            if (id == R.id.account_settings) {
                int headerIndex = i + 1;
                i = insertAccountsHeaders(target, headerIndex);
            } 

            if (i < target.size() && target.get(i) == header
                    && UserHandle.MU_ENABLED && UserHandle.myUserId() != 0
                    && !ArrayUtils.contains(SETTINGS_FOR_RESTRICTED, id)) {
                target.remove(i);
            }
            
            // Increment if the current one wasn't removed by the Utils code.
            if (i < target.size() && target.get(i) == header) {
                // Hold on to the first header, when we need to reset to the top-level
                if (mFirstHeader == null &&
                        HeaderAdapter.getHeaderType(header) != HeaderAdapter.HEADER_TYPE_CATEGORY) {
                    mFirstHeader = header;
                }
                mHeaderIndexMap.put(id, i);
                i++;
            }
        }
    }
		
	
	private int insertAccountsHeaders(List<Header> target, int headerIndex) {
        String[] accountTypes = mAuthenticatorHelper.getEnabledAccountTypes();
        List<Header> accountHeaders = new ArrayList<Header>(accountTypes.length);
        for (String accountType : accountTypes) {
            CharSequence label = mAuthenticatorHelper.getLabelForType(this, accountType);
            if (label == null) {
                continue;
            }

            Account[] accounts = AccountManager.get(this).getAccountsByType(accountType);
            boolean skipToAccount = accounts.length == 1
                    && !mAuthenticatorHelper.hasAccountPreferences(accountType);
            Header accHeader = new Header();
            accHeader.title = label;
            if (accHeader.extras == null) {
                accHeader.extras = new Bundle();
            }
            if (skipToAccount) {
                accHeader.breadCrumbTitleRes = R.string.account_sync_settings_title;
                accHeader.breadCrumbShortTitleRes = R.string.account_sync_settings_title;
                accHeader.fragment = AccountSyncSettings.class.getName();
                accHeader.fragmentArguments = new Bundle();
                // Need this for the icon
                accHeader.extras.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, accountType);
                accHeader.extras.putParcelable(AccountSyncSettings.ACCOUNT_KEY, accounts[0]);
                accHeader.fragmentArguments.putParcelable(AccountSyncSettings.ACCOUNT_KEY,
                        accounts[0]);
            } else {
                accHeader.breadCrumbTitle = label;
                accHeader.breadCrumbShortTitle = label;
                accHeader.fragment = ManageAccountsSettings.class.getName();
                accHeader.fragmentArguments = new Bundle();
                accHeader.extras.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE, accountType);
                accHeader.fragmentArguments.putString(ManageAccountsSettings.KEY_ACCOUNT_TYPE,
                        accountType);
                if (!isMultiPane()) {
                    accHeader.fragmentArguments.putString(ManageAccountsSettings.KEY_ACCOUNT_LABEL,
                            label.toString());
                }
            }
            accountHeaders.add(accHeader);
        }

        // Sort by label
        Collections.sort(accountHeaders, new Comparator<Header>() {
            @Override
            public int compare(Header h1, Header h2) {
                return h1.title.toString().compareTo(h2.title.toString());
            }
        });

        for (Header header : accountHeaders) {
            target.add(headerIndex++, header);
        }
        if (!mListeningToAccountUpdates) {
            AccountManager.get(this).addOnAccountsUpdatedListener(this, null, true);
            mListeningToAccountUpdates = true;
        }
        return headerIndex;
    }
	
	
	@Override
    public void onHeaderClick(Header header, int position) {
        boolean revert = false;
       
        Log.i("qy", "onHeaderClick");
        if (header.id == R.id.account_add) {
            revert = true;
        }

        super.onHeaderClick(header, position);

        if (revert && mLastHeader != null) {
            highlightHeader((int) mLastHeader.id);
        } else {
            mLastHeader = header;
        }
        
        // qy 
        if(!revert){
        	finish();
        }
       
    }
	
	private void highlightHeader(int id) {
        if (id != 0) {
            Integer index = mHeaderIndexMap.get(id);
            if (index != null) {
                getListView().setItemChecked(index, true);
                if (isMultiPane()) {
                    getListView().smoothScrollToPosition(index);
                }
            }
        }
    }
	
	@Override
    public void onResume() {
        super.onResume();   
        Log.i("qy", "*****onResume()*****");

      /*  mDevelopmentPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                invalidateHeaders();
            }
        };
        mDevelopmentPreferences.registerOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);*/
        
        invalidateHeaders();
    }
	
	@Override
    public void onPause() {
        super.onPause();      
        Log.i("qy", "*****onPause()*****");

        /*mDevelopmentPreferences.unregisterOnSharedPreferenceChangeListener(
                mDevelopmentPreferencesListener);
        mDevelopmentPreferencesListener = null;*/
    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("qy", "*****onDestroy()*****");
        if (mListeningToAccountUpdates) {
            AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        }

    }
	
	public boolean shouldUpRecreateTask(Intent targetIntent) {
        return super.shouldUpRecreateTask(new Intent(this, AccountActivity.class));
    }
	
	@Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(new HeaderAdapter(this, getHeaders(), mAuthenticatorHelper));
        }
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        // TODO: watch for package upgrades to invalidate cache; see 7206643
    	mAuthenticatorHelper.updateAuthDescriptions(this);
        mAuthenticatorHelper.onAccountsUpdated(this, accounts);
        invalidateHeaders();
        
    }
	
	
	private static int[] mPreferenceBackgroundIndexs;
    private static int[] mPreferenceBackgroundRes;
    
    private final int FRAME_LIST_BACKGROUND_NULL = 0;
    private final int FRAME_LIST_BACKGROUND_FULL = 1;
    private final int FRAME_LIST_BACKGROUND_TOP = 2;
    private final int FRAME_LIST_BACKGROUND_MIDDLE = 3;
    private final int FRAME_LIST_BACKGROUND_BOTTOM = 4;
    private final int FRAME_LIST_BACKGROUND_TOTAL = 5;
    
    private int[] getPreferenceBackgroundIndexs(List<Header> header) {
        if (header == null || header.size() <= 0) {
            return null;
        }
        
        
        int[] arrays = new int[header.size()];
        for (int i = 0; i < header.size(); i++) {
            if (HeaderAdapter.getHeaderType(header.get(i)) == HeaderAdapter.HEADER_TYPE_CATEGORY) {
                arrays[i] = FRAME_LIST_BACKGROUND_NULL;
                continue;
            }
            
            if (i > 0) {
                switch (arrays[i - 1]) {
                    case 0:
                        arrays[i] = FRAME_LIST_BACKGROUND_FULL;
                        break;
                    case 1:
                        arrays[i - 1] = FRAME_LIST_BACKGROUND_TOP;
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 2:
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 3:
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    case 4:
                        arrays[i - 1] = FRAME_LIST_BACKGROUND_MIDDLE;
                        arrays[i] = FRAME_LIST_BACKGROUND_BOTTOM;
                        break;
                    default:
                        break;
                }
            } else {
                arrays[i] = FRAME_LIST_BACKGROUND_FULL;
            }
        }
        return arrays;
    }
    
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
    
    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;        
        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_NORMAL + 1;

        private AuthenticatorHelper mAuthHelper;

        private static class HeaderViewHolder {
            ImageView icon;
            TextView title;
            TextView summary;                      
            TextView status;
            ImageView right;
            
        }

        private LayoutInflater mInflater;

        static int getHeaderType(Header header) {
        	if (header.fragment == null && header.intent == null) {
                return HEADER_TYPE_CATEGORY;
            } else {
                return HEADER_TYPE_NORMAL;
            }            
           
        }

        @Override
        public int getItemViewType(int position) {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false; // because of categories
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount() {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public HeaderAdapter(Context context, List<Header> objects,
                AuthenticatorHelper authenticatorHelper) {
            super(context, 0, objects);

            mAuthHelper = authenticatorHelper;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);
            View view = null;

            if (convertView == null) {
                holder = new HeaderViewHolder();
                switch (headerType) {
                    case HEADER_TYPE_CATEGORY:
                        view = new TextView(getContext(), null,
                                android.R.attr.listSeparatorTextViewStyle);                        
                        view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1));                       

                        holder.title = (TextView) view;
                        break;                    

                    case HEADER_TYPE_NORMAL:
                        view = mInflater.inflate(
                                R.layout.preference_header_item, parent,
                                false);
                        holder.icon = (ImageView) view.findViewById(R.id.icon);
                        holder.title = (TextView)
                                view.findViewById(android.R.id.title);
                        holder.summary = (TextView)
                                view.findViewById(android.R.id.summary);

                        
                        holder.status = (TextView) view.findViewById(R.id.status);
                        holder.right = (ImageView) view.findViewById(R.id.right);
                        

                        break;
                }
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            switch (headerType) {
                case HEADER_TYPE_CATEGORY:                	
                    holder.title.setText(null);                    
                    break;
                
                case HEADER_TYPE_NORMAL:
                    if (header.extras != null
                            && header.extras.containsKey(ManageAccountsSettings.KEY_ACCOUNT_TYPE)) {
                        String accType = header.extras.getString(
                                ManageAccountsSettings.KEY_ACCOUNT_TYPE);
                        ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
                        lp.width = getContext().getResources().getDimensionPixelSize(
                                R.dimen.header_icon_width);
                        lp.height = lp.width;
                        holder.icon.setLayoutParams(lp);
                        Drawable icon = mAuthHelper.getDrawableForType(getContext(), accType);
                        holder.icon.setImageDrawable(icon);
                    } else {
                        holder.icon.setImageResource(header.iconRes);
                    }
                    holder.title.setText(header.getTitle(getContext().getResources()));
                    CharSequence summary = header.getSummary(getContext().getResources());
                    if (!TextUtils.isEmpty(summary)) {
                        holder.summary.setVisibility(View.VISIBLE);
                        holder.summary.setText(summary);
                    } else {
                        holder.summary.setVisibility(View.GONE);
                    }
                   
                    if (holder.status != null) {                    	
                        holder.status.setVisibility(View.GONE);                        
                    }                    

                    break;
            }
            
            if (mPreferenceBackgroundIndexs != null && mPreferenceBackgroundIndexs.length > position) {
                if (mPreferenceBackgroundRes != null
                        && mPreferenceBackgroundIndexs[position] < mPreferenceBackgroundRes.length
                        && mPreferenceBackgroundRes[mPreferenceBackgroundIndexs[position]] > 0) {
                    view.setBackgroundResource(mPreferenceBackgroundRes[mPreferenceBackgroundIndexs[position]]);
                }
            }            

            return view;
        }
             

       
    }
    
    
    
    
   

    public static class ManageAccountsSettingsActivity extends AccountActivity { /* empty */ 
    	
    }

    public static class AccountSyncSettingsActivity extends AccountActivity { /* empty */
    	
    	
    }
    public static class AccountSyncSettingsInAddAccountActivity extends AccountActivity { /* empty */ }
    
   
    
    
}
