/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.quickcontact;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredPostal;
import gionee.provider.GnContactsContract.CommonDataKinds.Email;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.Website;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;

import java.util.List;

import android.content.ActivityNotFoundException;
import com.android.contacts.GNContactsUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import com.android.contacts.ContactsUtils;
import com.android.contacts.util.Constants;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import gionee.provider.GnTelephony.SIMInfo; 
import gionee.provider.GnTelephony;
import android.app.Activity;

/** A fragment that shows the list of resolve items below a tab */
public class QuickContactListFragment extends Fragment {
    private static final String TAG = "QuickContactListFragment";
	
    private AuroraListView mListView;
    private List<Action> mActions;
    private LinearLayout mFragmentContainer;
    private Listener mListener;

    /*
     * New Feature by Mediatek Begin.            
     * save context's object   
     * Original Android’s code:
             public DetailViewCache(View view,
                OnClickListener primaryActionClickListener,
                OnClickListener secondaryActionClickListener) {  
     */
    private final Context mContext;
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * save context's object   
     * public QuickContactListFragment() {             
     */
    public QuickContactListFragment(Context mContext) { 
    	this.mContext = mContext;
    /*
     * New Feature  by Mediatek End.
     */	
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
    	// gionee xuhz 20121114 modify for GIUI2.0 start
		if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
			mFragmentContainer = (LinearLayout) inflater.inflate(R.layout.gn_quickcontact_list_fragment,
					container, false);
		} else {
			mFragmentContainer = (LinearLayout) inflater.inflate(R.layout.quickcontact_list_fragment,
					container, false);
		}
		// gionee xuhz 20121114 modify for GIUI2.0 end

        mListView = (AuroraListView) mFragmentContainer.findViewById(R.id.list);
        mListView.setItemsCanFocus(true);

        mFragmentContainer.setOnClickListener(mOutsideClickListener);
        configureAdapter();
        return mFragmentContainer;
    }

    public void setActions(List<Action> actions) {
        mActions = actions;
        configureAdapter();
    }

    public void setListener(Listener value) {
        mListener = value;
    }

    private void configureAdapter() {
        if (mActions == null || mListView == null) return;

        mListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mActions.size();
            }

            @Override
            public Object getItem(int position) {
                return mActions.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Set action title based on summary value
                /*
                 * New Feature by Mediatek Begin. 
                 * Original Android’s code:
                 * final Action action = mActions.get(position);                           
                 */
                final Action action = mActions.get(position);
                /*
                 * New Feature  by Mediatek End.
                */
                String mimeType = action.getMimeType();

                //Gionee:huangzy 20120621 modify for CR00627478 start
                final View resultView = convertView != null ? convertView
                        : getActivity().getLayoutInflater().inflate(
                                mimeType.equals(StructuredPostal.CONTENT_ITEM_TYPE) ?
                                        R.layout.quickcontact_list_item_address :
                                        (ContactsApplication.sIsGnContactsSupport ? 
                                                R.layout.gn_quickcontact_list_item :
                                                    R.layout.quickcontact_list_item),
                                        parent, false);
                //Gionee:huangzy 20120621 modify for CR00627478 end

                // TODO: Put those findViewByIds in a container
                final TextView text1 = (TextView) resultView.findViewById(
                        android.R.id.text1);
                final TextView text2 = (TextView) resultView.findViewById(
                        android.R.id.text2);
                final View actionsContainer = resultView.findViewById(
                        R.id.actions_view_container);
                final ImageView alternateActionButton = (ImageView) resultView.findViewById(
                        R.id.secondary_action_button);
                final View alternateActionDivider = resultView.findViewById(R.id.vertical_divider);

                actionsContainer.setOnClickListener(mPrimaryActionClickListener);
                actionsContainer.setTag(action);
                alternateActionButton.setOnClickListener(mSecondaryActionClickListener);
                alternateActionButton.setTag(action);

                final boolean hasAlternateAction = action.getAlternateIntent() != null;
                alternateActionDivider.setVisibility(hasAlternateAction ? View.VISIBLE : View.GONE);
                alternateActionButton.setImageDrawable(action.getAlternateIcon());
                alternateActionButton.setContentDescription(action.getAlternateIconDescription());
                alternateActionButton.setVisibility(hasAlternateAction ? View.VISIBLE : View.GONE);

                LinearLayout.LayoutParams pms = (LinearLayout.LayoutParams) text2.getLayoutParams();
                if (showNewAddWidget(action, hasAlternateAction, resultView)) { 
					pms.width = mContext.getResources().getDimensionPixelSize(R.dimen.quickcontact_item_name_width);               	          	
                } else {
                	pms.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                }
                text2.setLayoutParams(pms);
                
                // Special case for phone numbers in accessibility mode
                if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    text1.setContentDescription(getActivity().getString(
                            R.string.description_dial_phone_number, action.getBody()));
                    if (hasAlternateAction) {
                    	if (ContactsApplication.sIsGnGGKJ_V2_0Support && ContactsApplication.sIsGnDarkStyle) {
                            alternateActionButton.setImageResource(R.drawable.ic_text_holo_dark);
                    	}
                        alternateActionButton.setContentDescription(getActivity()
                                .getString(R.string.description_send_message, action.getBody()));
                    }
                }

                text1.setText(action.getBody());
                if (text2 != null) {
                    CharSequence subtitle = action.getSubtitle();
                    text2.setText(subtitle);
                    if (TextUtils.isEmpty(subtitle)) {
                        text2.setVisibility(View.GONE);
                    } else {
                        text2.setVisibility(View.VISIBLE);
                    }
                }
                
                if (ContactsApplication.sIsGnContactsSupport) {
                	final TextView areaView = (TextView) resultView.findViewById(R.id.area);
                	if (null != areaView) {
                		String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAora(mContext, action.getBody().toString(), false);
                		if(area != null){
                			setArea(areaView, text2, area);
                		} else {
                        	final String number = action.getBody().toString();
                        	new Thread(new Runnable() {
        						public void run() {
        			                final String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAoraLock(mContext, number, false);
        			                Activity activity = getActivity();
        			                if(activity != null){
        			                	activity.runOnUiThread(new Runnable() {
        									public void run() {
        			                			setArea(areaView, text2, area);
        									}
        								});
        			                }
        						}
        					}).start();
                		}
                	}
                }
                
                return resultView;
            }
        });
    }
    
    private void setArea(TextView areaView, TextView text2, String area){
		if (TextUtils.isEmpty(area)) {
			areaView.setVisibility(View.GONE);
		} else {
			if (null != text2 && text2.getVisibility() == View.VISIBLE) {
				area = "-" + area;
			}
			areaView.setText(area);
			areaView.setVisibility(View.VISIBLE);
		}
    }

    /** A data item (e.g. phone number) was clicked */
    protected final OnClickListener mPrimaryActionClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Action action = (Action) v.getTag();
        	// gionee xuhz 20121215 modify for Dual Sim Select start
    		if (ContactsApplication.sIsGnDualSimSelectSupport) {
    			if (action.getIntent() != null) {
    				String actionString = action.getIntent().getAction();
    				if ("com.android.contacts.action.GNSELECTSIM".equalsIgnoreCase(actionString)) {
                        int[] location = new int[2] ;
                        v.getLocationOnScreen(location);
                        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
                        int y = location[1] - dm.heightPixels/2;
                    	
                        action.getIntent().putExtra("y", y);
    				}
    			}
    		}
            // gionee xuhz 20121215 modify for Dual Sim Select end
            if (mListener != null) mListener.onItemClicked(action, false);
        }
    };

    /** A secondary action (SMS) was clicked */
    protected final OnClickListener mSecondaryActionClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Action action = (Action) v.getTag();
            if (mListener != null) mListener.onItemClicked(action, true);
        }
    };

    private final OnClickListener mOutsideClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListener != null) mListener.onOutsideClick();
        }
    };

    public interface Listener {
        void onOutsideClick();
        void onItemClicked(Action action, boolean alternate);
    }
    
    
    /*
     * New Feature by Mediatek Begin.              
     * show newly view for association and vtcall                          
     */
    public boolean showNewAddWidget(final Action action, final boolean hasAlternateAction, final View resultView) {
    	if (hasAlternateAction) {               
    		// is association
    	    final ImageView imgAssociationSimFlag = (ImageView) resultView.findViewById(R.id.association_sim_icon);            
            final TextView txtAssociationSimName = (TextView) resultView.findViewById(R.id.association_sim_text);
            
    		int simId = -1;
    		final boolean isQuickDataAction = action instanceof QuickDataAction;
    		if (isQuickDataAction) {
    		    simId = ((QuickDataAction) action).getSimId();
    		}
        	if (simId > -1) {            	
                if (imgAssociationSimFlag != null) {                   	    
                    imgAssociationSimFlag.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_association));
                    imgAssociationSimFlag.setVisibility(View.VISIBLE);
                }                                                                  
                if (txtAssociationSimName != null) { 
                    if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                        txtAssociationSimName.setMaxWidth(150);
                    } else {
                        txtAssociationSimName.setMaxWidth(300); 
                    }                    	
                    
                    SIMInfo simInfo = SIMInfo.getSIMInfoById(mContext, simId);
                	if (simInfo != null) {
                		Log.i(TAG,"simInfo.mDisplayName is " + simInfo.mDisplayName);
                    	Log.i(TAG,"simInfo.mColor is " + simInfo.mColor);
                    	txtAssociationSimName.setText(simInfo.mDisplayName);
                        int slotId = SIMInfo.getSlotById(mContext, simId);
                        Log.d(TAG,"slotId = "+slotId);
                        if(slotId>=0){
                            Log.d(TAG,"slotId >= 0 ");
                            txtAssociationSimName.setBackgroundResource(GnTelephony.SIMBackgroundRes[simInfo.mColor]);
                        } else {
                            Log.d(TAG,"slotId < 0 ");
                            txtAssociationSimName.setBackgroundResource(R.drawable.sim_background_locked);	
                        }
                        int padding = this.getResources().getDimensionPixelOffset(R.dimen.association_sim_leftright_padding);
                        txtAssociationSimName.setPadding(padding, 0, padding, 0);
                	} else {
                		Log.i(TAG,"not find siminfo");
                	}          
                    
                	txtAssociationSimName.setVisibility(View.VISIBLE);                    	 
                }
        	} else {        	    
                if (imgAssociationSimFlag != null) {                                          
                    imgAssociationSimFlag.setVisibility(View.GONE);
                }                 
                if (txtAssociationSimName != null) { 
                    txtAssociationSimName.setVisibility(View.GONE);     
                }
        	}
    		 
        	final View vewVtCallDivider = resultView.findViewById(R.id.vertical_divider_vtcall);
            final ImageView btnVtCallAction = (ImageView) resultView.findViewById(R.id.vtcall_action_button);
        	if (isQuickDataAction && FeatureOption.MTK_VT3G324M_SUPPORT) {
        	    if (vewVtCallDivider != null) {
                    vewVtCallDivider.setVisibility(View.VISIBLE);
                }           
                if (btnVtCallAction != null) {
                    btnVtCallAction.setContentDescription(action.getAlternateIconDescription());
                    if (ContactsApplication.sIsGnGGKJ_V2_0Support && ContactsApplication.sIsGnDarkStyle) {
                    	btnVtCallAction.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_video_call_dark));
                    } else {
                        btnVtCallAction.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_video_call));
                    }
                    btnVtCallAction.setVisibility(View.VISIBLE);
                    btnVtCallAction.setTag(action);
                    btnVtCallAction.setOnClickListener(mVTCallActionClickListener);               
                } 
        	} else {
        	    if (vewVtCallDivider != null) {
                    vewVtCallDivider.setVisibility(View.GONE);
                }           
                if (btnVtCallAction != null) {                   
                    btnVtCallAction.setVisibility(View.GONE);                      
                }
        	}
        	return simId > -1;
        }    
    	return false;
    }    
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.              
     * handle vtcall action           
     */
    protected final OnClickListener mVTCallActionClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final QuickDataAction action = (QuickDataAction) v.getTag();
            String sNumber = action.getBody().toString();
            
            // Gionee:wangth 20130301 modify for CR00771431 begin
            /*
            final Intent intent;
            if (ContactsApplication.sIsGnContactsSupport) {
            	intent = IntentFactory.newDialNumberIntent(sNumber);
            } else {
            	intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel", sNumber, null));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
            intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long) action.getSimId());
            intent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
            startActivity(intent);
            */
            if (GNContactsUtils.isOnlyQcContactsSupport()) {
                Intent intent = GNContactsUtils.startQcVideoCallIntent(sNumber);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException a) {
                    a.printStackTrace();
                }
            } else {
                final Intent intent;
                if (ContactsApplication.sIsGnContactsSupport) {
                    intent = IntentFactory.newDialNumberIntent(sNumber);
                } else {
                    intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel", sNumber, null));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
                intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long) action.getSimId());
                intent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                startActivity(intent);
            }
            // Gionee:wangth 20130301 modify for CR00771431 end
            
            /*
             * Bug Fix by Mediatek Begin.
             *   CR ID: ALPS00119452
             */
            if (mContext instanceof Activity) {
                Activity activity = (Activity) mContext;
                activity.finish();
            } 
            /*
             * Bug Fix by Mediatek End.
             */ 
        }
    };    
    /*
     * New Feature  by Mediatek End.
    */
    
}
