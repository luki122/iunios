package com.mediatek.contacts.widget;

import java.util.List;

import android.accounts.Account;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import gionee.provider.GnTelephony.SIMInfo;

// Gionee lihuafang 20120422 add for CR00573564 begin
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
// Gionee lihuafang 20120422 add for CR00573564 end
import com.android.contacts.R;
import com.android.contacts.util.Constants;
import com.mediatek.contacts.util.TelephonyUtils;
import com.mediatek.contacts.util.OperatorUtils;
//import com.mediatek.telephony.TelephonyManagerEx;
import com.gionee.internal.telephony.GnTelephonyManagerEx;

public class SimPickerAdapter extends BaseAdapter {

    public static final int ITEM_TYPE_UNKNOWN  = -1;
    public static final int ITEM_TYPE_SIM      =  0;
    public static final int ITEM_TYPE_INTERNET =  1;
    public static final int ITEM_TYPE_TEXT     =  2;
    public static final int ITEM_TYPE_ACCOUNT  =  3;
    // Gionee lihuafang 2012-05-31 add for CR00614104 begin
    private static final int SIM_STATUS_COUNT = 9;
    // Gionee lihuafang 2012-05-31 add for CR00614104 end
    
    Context mContext;
    long mSuggestedSimId;
    List<ItemHolder> mItems;

    boolean mSingleChoice;
    int mSingleChoiceIndex;
    
    public SimPickerAdapter(Context context, List<ItemHolder> items, long suggestedSimId) {
        mContext = context;
        mSuggestedSimId = suggestedSimId;
        mItems = items;
        mSingleChoice = false;
        mSingleChoiceIndex = -1;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return mItems.size();
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }
    
    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        ItemHolder itemHolder = mItems.get(position);
        return itemHolder.type;
    }

    public void setSingleChoice(boolean singleChoice) {
        mSingleChoice = singleChoice;
    }

    public boolean getSingleChoice() {
        return mSingleChoice;
    }

    public void setSingleChoiceIndex(int singleChoiceIndex) {
        mSingleChoiceIndex = singleChoiceIndex;
    }

    public Object getItem(int position) {
        ItemHolder itemHolder = mItems.get(position);
        if(itemHolder.type == ITEM_TYPE_SIM) {
            return Integer.valueOf(((SIMInfo)itemHolder.data).mSlot);
        } else if(itemHolder.type == ITEM_TYPE_INTERNET) {
            return Integer.valueOf((int)ContactsFeatureConstants.VOICE_CALL_SIM_SETTING_INTERNET);
        } else if(itemHolder.type == ITEM_TYPE_TEXT || itemHolder.type == ITEM_TYPE_ACCOUNT) {
            return itemHolder.data;
        } else {
            return null;
        }
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        int viewType = getItemViewType(position);
        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            holder = new ViewHolder();
            
            if(viewType == ITEM_TYPE_SIM) {
                // Gionee lihuafang 20120422 modify for CR00573564 begin
                /*
                view = inflater.inflate(R.layout.sim_picker_item, null);
                */
                if (ContactsUtils.mIsGnContactsSupport && ContactsUtils.mIsGnShowSlotSupport) {
                    view = inflater.inflate(R.layout.gn_sim_picker_item, null);
                } else {
                    view = inflater.inflate(R.layout.sim_picker_item, null);
                }
                // Gionee lihuafang 20120422 modify for CR00573564 end
                holder.mSimSignal = (TextView)view.findViewById(R.id.simSignal);
                holder.mSimStatus = (ImageView)view.findViewById(R.id.simStatus);
                holder.mShortPhoneNumber = (TextView)view.findViewById(R.id.shortPhoneNumber);
                holder.mDisplayName = (TextView)view.findViewById(R.id.displayName);
                holder.mPhoneNumber = (TextView)view.findViewById(R.id.phoneNumber);
                holder.mSimIcon = view.findViewById(R.id.simIcon);
                holder.mSuggested = (TextView)view.findViewById(R.id.suggested);
                holder.mRadioButton = (RadioButton)view.findViewById(R.id.select);
            } else if(viewType == ITEM_TYPE_INTERNET) {
                view = inflater.inflate(R.layout.sim_picker_item_internet, null);
                holder.mInternetIcon = (ImageView)view.findViewById(R.id.internetIcon);
                holder.mRadioButton = (RadioButton)view.findViewById(R.id.select);
            } else if(viewType == ITEM_TYPE_TEXT || viewType == ITEM_TYPE_ACCOUNT) {
                view = inflater.inflate(R.layout.sim_picker_item_text, null);
                holder.mText = (TextView)view.findViewById(R.id.text);
                holder.mRadioButton = (RadioButton)view.findViewById(R.id.select);
            }
            view.setTag(holder);
        }
        
        holder = (ViewHolder)view.getTag();
        // gionee xuhz 20120529 add for gn theme start
        if (ContactsApplication.sIsGnDarkTheme) {
            //Gionee:tianxiaolong 2012.9.8 modify for CR00689188 begin
            if(holder.mDisplayName != null){
                holder.mDisplayName.setTextColor(mContext.getResources().getColor(android.R.color.white));
            }
            //Gionee:tianxiaolong 2012.9.8 modify for CR00689188 end
        }
        // gionee xuhz 20120529 add for gn theme end
        // gionee xuhz 20120710 add for CR00640208 start
        if (ContactsApplication.sIsGnTransparentTheme) {
            //Gionee:tianxiaolong 2012.9.8 modify for CR00689188 begin
            if(holder.mPhoneNumber != null){
                holder.mPhoneNumber.setTextColor(mContext.getResources().getColor(R.color.gn_secondary_text_color_for_phonenumber));
            }
            //Gionee:tianxiaolong 2012.9.8 modify for CR00689188 end
        }
        // gionee xuhz 20120710 add for CR00640208 end

        if(mSingleChoice && holder.mRadioButton != null)
            holder.mRadioButton.setVisibility(View.VISIBLE);
        else
            holder.mRadioButton.setVisibility(View.GONE);

        if(viewType == ITEM_TYPE_SIM) {
            SIMInfo simInfo = (SIMInfo)mItems.get(position).data;
            holder.mDisplayName.setText(simInfo.mDisplayName);
            holder.mSimIcon.setBackgroundResource(gionee.provider.GnTelephony.SIMBackgroundRes[simInfo.mColor]);

            if(simInfo.mSimId == mSuggestedSimId)
                holder.mSuggested.setVisibility(View.VISIBLE);
            else
                holder.mSuggested.setVisibility(View.GONE);

            try {
                String shortNumber = "";
                if(!TextUtils.isEmpty(simInfo.mNumber)) {
                    switch(simInfo.mDispalyNumberFormat) {
                        case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                            if(simInfo.mNumber.length() <= 4)
                                shortNumber = simInfo.mNumber;
                            else
                                shortNumber = simInfo.mNumber.substring(0, 4);
                            break;
                        case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
                            if(simInfo.mNumber.length() <= 4)
                                shortNumber = simInfo.mNumber;
                            else
                                shortNumber = simInfo.mNumber.substring(simInfo.mNumber.length()-4, simInfo.mNumber.length());
                            break;
                        case 0://android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                            shortNumber = "";
                            break;
                    }
                    holder.mPhoneNumber.setText(simInfo.mNumber);
                    holder.mPhoneNumber.setVisibility(View.VISIBLE);
                } else {
                    holder.mPhoneNumber.setVisibility(View.GONE);
                }
                holder.mShortPhoneNumber.setText(shortNumber);
                holder.mSimSignal.setVisibility(View.INVISIBLE);
                // Gionee lihuafang 2012-07-05 remove for CR00637513 begin
                /*
                // MTK_OP02_PROTECT_START
                if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                    if("OP02".equals(OperatorUtils.getOptrProperties()))
                        if(simInfo.mSlot == TelephonyUtils.get3GCapabilitySIM())
                            holder.mSimSignal.setVisibility(View.VISIBLE);
                }
                // MTK_OP02_PROTECT_END
                */
                if (!ContactsUtils.mIsGnContactsSupport) {
                    // MTK_OP02_PROTECT_START
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        if ("OP02".equals(OperatorUtils.getOptrProperties()))
                            if (simInfo.mSlot == TelephonyUtils.get3GCapabilitySIM())
                                holder.mSimSignal.setVisibility(View.VISIBLE);
                    }
                    // MTK_OP02_PROTECT_END
                }
                // Gionee lihuafang 2012-07-05 remove for CR00637513 end

                if(holder.mRadioButton != null) {
                    if(mSingleChoiceIndex == simInfo.mSimId)
                        holder.mRadioButton.setChecked(true);
                    else
                        holder.mRadioButton.setChecked(false);
                }
            } catch(Exception e) {
                holder.mShortPhoneNumber.setText("");
            }
            holder.mSimStatus.setImageResource(getSimStatusIcon(simInfo.mSlot));
            // Gionee lihuafang 20120422 add for CR00573564 begin
            if (ContactsUtils.mIsGnContactsSupport && FeatureOption.MTK_GEMINI_SUPPORT
                    && ContactsUtils.mIsGnShowSlotSupport) {
                holder.mSimSignal.setVisibility(View.VISIBLE);
                if (simInfo.mSlot == 0) {
                    holder.mSimSignal.setText(R.string.slotA_signal);
                } else if (simInfo.mSlot == 1) {
                    holder.mSimSignal.setText(R.string.slotB_signal);
                }
            }
            // Gionee lihuafang 20120422 add for CR00573564 end
        } else if(viewType == ITEM_TYPE_INTERNET) {
            holder.mInternetIcon.setBackgroundResource(R.drawable.sim_background_sip);

            if(holder.mRadioButton != null) {
                if(mSingleChoiceIndex == Constants.FILTER_SIP_CALL)
                    holder.mRadioButton.setChecked(true);
                else
                    holder.mRadioButton.setChecked(false);
            }
        } else if(viewType == ITEM_TYPE_TEXT) {
            String text = (String)mItems.get(position).data;
            holder.mText.setText(text);

            if(holder.mRadioButton != null) {
                if(mSingleChoiceIndex == Constants.FILTER_ALL_RESOURCES)
                    holder.mRadioButton.setChecked(true);
                else
                    holder.mRadioButton.setChecked(false);
            }
        } else if(viewType == ITEM_TYPE_ACCOUNT) {
            Account account = (Account)mItems.get(position).data;
            holder.mText.setText((String)account.name);
        }

        return view;
    }
    
    protected int getSimStatusIcon(int slot) {
        GnTelephonyManagerEx telephonyManager = GnTelephonyManagerEx.getDefault();
        int state = telephonyManager.getSimIndicatorStateGemini(slot);
        int resourceId = 0;
        switch (state) {
            case ContactsFeatureConstants.SIM_INDICATOR_LOCKED:
                resourceId = R.drawable.sim_locked;
                break;
            case ContactsFeatureConstants.SIM_INDICATOR_RADIOOFF:
                resourceId = R.drawable.sim_radio_off;
                break;
            case ContactsFeatureConstants.SIM_INDICATOR_ROAMING:
                resourceId = R.drawable.sim_roaming;
                break;
            case ContactsFeatureConstants.SIM_INDICATOR_SEARCHING:
                resourceId = R.drawable.sim_searching;
                break;
            case ContactsFeatureConstants.SIM_INDICATOR_INVALID:
                resourceId = R.drawable.sim_invalid;
                break;
            case ContactsFeatureConstants.SIM_INDICATOR_CONNECTED:
                resourceId = R.drawable.sim_connected;
                break;
            case ContactsFeatureConstants.SIM_INDICATOR_ROAMINGCONNECTED:
                resourceId = R.drawable.sim_roaming_connected;
                break;
        }
        // Gionee lihuafang 2012-05-31 add for CR00614104 begin
        if (ContactsUtils.mIsGnShowDigitalSlotSupport) {
            if (state <= ContactsFeatureConstants.SIM_INDICATOR_UNKNOWN
                    || state >= SIM_STATUS_COUNT
                    || state == ContactsFeatureConstants.SIM_INDICATOR_NORMAL) {
                if (slot == 0) {
                    resourceId = R.drawable.zzzzz_gn_sim1;
                } else if (slot == 1) {
                    resourceId = R.drawable.zzzzz_gn_sim2;
                }
            }
        }
        // Gionee lihuafang 2012-05-31 add for CR00614104 end

        return resourceId;
    }

    private class ViewHolder {
        View      mSimIcon;
        ImageView mSimStatus;
        TextView mSimSignal;
        TextView  mShortPhoneNumber;
        TextView  mDisplayName;
        TextView  mPhoneNumber;
        TextView  mSuggested;
        TextView  mText;
        ImageView mInternetIcon;
        RadioButton mRadioButton;
    }

    public static class ItemHolder {
        public Object data;
        public int type;
        
        public ItemHolder(Object data, int type) {
            this.data = data;
            this.type = type; 
        }
    }
}
