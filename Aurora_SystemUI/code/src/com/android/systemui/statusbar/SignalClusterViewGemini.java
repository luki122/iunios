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

package com.android.systemui.statusbar;

import android.util.Log;
import android.content.Context;
import android.content.res.Resources;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkControllerGemini;
import com.android.systemui.statusbar.policy.TelephonyIconsGemini;
import com.android.systemui.statusbar.util.SIMHelper;

import com.gionee.featureoption.FeatureOption;
import com.mediatek.systemui.ext.IconIdWrapper;
import com.mediatek.systemui.ext.NetworkType;
import com.mediatek.systemui.ext.PluginFactory;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.systemui.Xlog;
import com.gionee.internal.telephony.GnITelephony;

/// M: [SystemUI] Support dual SIM.
public class SignalClusterViewGemini extends LinearLayout implements NetworkControllerGemini.SignalCluster {
    private static final String TAG = "SignalClusterViewGemini";

    static final boolean DEBUG = false;
    
    private NetworkControllerGemini mNC;

    private boolean mRoaming = false;
    private boolean mRoamingGemini = false;
    private int mRoamingId = 0;
    private int mRoamingGeminiId = 0;
    private boolean mShowSimIndicator = false;
    private boolean mShowSimIndicatorGemini = false;
    private int mSimIndicatorResource = 0;
    private int mSimIndicatorResourceGemini = 0;
    
    private boolean mIsAirplaneMode = false;

    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0;
    private int mWifiActivityId = 0;
    private String mWifiDescription;

    private boolean mMobileVisible = false;
    private IconIdWrapper mMobileStrengthId[] = {new IconIdWrapper(), new IconIdWrapper()};
    private IconIdWrapper mMobileActivityId = new IconIdWrapper(0);
    private IconIdWrapper mMobileTypeId = new IconIdWrapper(0);
    private String mMobileDescription;
    private String mMobileTypeDescription;
    private boolean mMobileVisibleGemini = false;
    private IconIdWrapper mMobileStrengthIdGemini[] = {new IconIdWrapper(), new IconIdWrapper()};
    private IconIdWrapper mMobileActivityIdGemini = new IconIdWrapper(0);
    private IconIdWrapper mMobileTypeIdGemini = new IconIdWrapper(0);
    private String mMobileDescriptionGemini;
    private String mMobileTypeDescriptionGemini;

    private ViewGroup mWifiGroup;
    private ImageView mWifi;
    private ImageView mWifiActivity;

    private ViewGroup mSignalClusterCombo;
    private ImageView mSignalNetworkType;
    private ViewGroup mSignalClusterComboGemini;
    private ImageView mSignalNetworkTypeGemini;
    
    private ViewGroup mMobileGroup;
    private ImageView mMobileRoam;
    private ImageView mMobile;
    private ImageView mMobile2;
    private ImageView mMobileActivity;
    private ImageView mMobileType;
    private View mSpacer;
    private View mFlightMode;
    
    private ViewGroup mMobileGroupGemini;
    private ImageView mMobileRoamGemini;
    private ImageView mMobileGemini;
    private ImageView mMobileGemini2;
    private ImageView mMobileActivityGemini;
    private ImageView mMobileTypeGemini;
    private View mSpacerGemini;
    private ImageView mMobileSlotIndicator;
    private ImageView mMobileSlotIndicatorGemini;

    private int mSIMColorId = -1;
    private int mSIMColorIdGemini = -1;

    private boolean mDataConnected = false;
    private boolean mDataConnectedGemini = false;
    private boolean mIsDataGeminiIcon = false;
    private ViewGroup mDataConnectionGroup;

    private NetworkType mDataNetType = null;
    private NetworkType mDataNetTypeGemini = null;
    private ImageView mMobileNetType;
    private ImageView mMobileNetTypeGemini;

    public SignalClusterViewGemini(Context context) {
        this(context, null);
    }

    public SignalClusterViewGemini(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterViewGemini(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNetworkControllerGemini(NetworkControllerGemini nc) {
        if (DEBUG) {
            Xlog.d(TAG, "NetworkControllerGemini=" + nc);
        }
        mNC = nc;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mWifiGroup                    = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi                         = (ImageView) findViewById(R.id.wifi_signal);
        mWifiActivity                 = (ImageView) findViewById(R.id.wifi_inout);
        mMobileGroup                  = (ViewGroup) findViewById(R.id.mobile_combo);
        mMobile                       = (ImageView) findViewById(R.id.mobile_signal);
        mMobileActivity               = (ImageView) findViewById(R.id.mobile_inout);
        mMobileType                   = (ImageView) findViewById(R.id.mobile_type);
        mMobileGroupGemini            = (ViewGroup) findViewById(R.id.mobile_combo_gemini);
        mMobileGemini                 = (ImageView) findViewById(R.id.mobile_signal_gemini);
        
        mMobileRoam                   = (ImageView) findViewById(R.id.mobile_roaming);
        mMobileRoamGemini             = (ImageView) findViewById(R.id.mobile_roaming_gemini);
        
        mMobileActivityGemini         = (ImageView) findViewById(R.id.mobile_inout_gemini);
        mMobileTypeGemini             = (ImageView) findViewById(R.id.mobile_type_gemini);
        mSpacer                       =             findViewById(R.id.spacer);
        mSpacerGemini                 =             findViewById(R.id.spacer_gemini);
        mFlightMode                   = (ImageView) findViewById(R.id.flight_mode);
        mMobileSlotIndicator          = (ImageView) findViewById(R.id.mobile_slot_indicator);
        mMobileSlotIndicatorGemini    = (ImageView) findViewById(R.id.mobile_slot_indicator_gemini);
        mSignalClusterCombo           = (ViewGroup) findViewById(R.id.signal_cluster_combo);
        mSignalNetworkType            = (ImageView) findViewById(R.id.network_type);
        mSignalClusterComboGemini     = (ViewGroup) findViewById(R.id.signal_cluster_combo_gemini);
        mSignalNetworkTypeGemini      = (ImageView) findViewById(R.id.network_type_gemini);
        mMobile2                      = (ImageView) findViewById(R.id.mobile_signal2);
        mMobileGemini2                = (ImageView) findViewById(R.id.mobile_signal_gemini2);
        
        int resId = PluginFactory.getStatusBarPlugin(mContext).getSignalIndicatorIconGemini(FeatureOption.GEMINI_SIM_1);
        if (resId != -1) {
            mMobileSlotIndicator.setImageDrawable(PluginFactory.getStatusBarPlugin(mContext).getPluginResources()
                    .getDrawable(resId));
            mMobileSlotIndicator.setVisibility(View.VISIBLE);
        } else {
            mMobileSlotIndicator.setImageResource(0);
            mMobileSlotIndicator.setVisibility(View.GONE);
        }
        int resIdGemini = PluginFactory.getStatusBarPlugin(mContext).getSignalIndicatorIconGemini(FeatureOption.GEMINI_SIM_2);
        if (resIdGemini != -1) {
            mMobileSlotIndicatorGemini.setImageDrawable(PluginFactory.getStatusBarPlugin(mContext).getPluginResources()
                    .getDrawable(resIdGemini));
            mMobileSlotIndicatorGemini.setVisibility(View.VISIBLE);
        } else {
            mMobileSlotIndicatorGemini.setImageResource(0);
            mMobileSlotIndicatorGemini.setVisibility(View.GONE);
        }
        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mWifiGroup            = null;
        mWifi                 = null;
        mWifiActivity         = null;
        mMobileGroup          = null;
        mMobile               = null;
        mMobileActivity       = null;
        mMobileType           = null;
        mMobileGroupGemini    = null;
        mMobileGemini         = null;
        
        mMobileActivityGemini = null;
        mMobileTypeGemini     = null;
        mSpacer               = null;
        mSpacerGemini         = null;
        
        mMobileRoam           = null;
        mMobileRoamGemini     = null;
        
        mDataConnectionGroup  = null;
        mMobileNetType        = null;
        mMobileNetTypeGemini  = null;
        mMobile2              = null;
        mMobileGemini2        = null;

        super.onDetachedFromWindow();
    }

    public void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon,
            String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiActivityId = activityIcon;
        mWifiDescription = contentDescription;
    }

    public void setMobileDataIndicators(int slotId, boolean visible, IconIdWrapper[] strengthIcon,
            IconIdWrapper activityIcon, IconIdWrapper typeIcon, String contentDescription, String typeContentDescription) {
        Xlog.d(TAG, "setMobileDataIndicators(" + slotId + "), visible=" + visible + ", strengthIcon[0] ~ [1] "
                + strengthIcon[0].getIconId() + " ~ " + strengthIcon[1].getIconId());
        if (slotId == FeatureOption.GEMINI_SIM_1) {
            mMobileVisible = visible;
            mMobileStrengthId[0] = strengthIcon[0].clone();
            mMobileStrengthId[1] = strengthIcon[1].clone();
            mMobileActivityId = activityIcon.clone();
            mMobileTypeId = typeIcon.clone();
            mMobileDescription = contentDescription;
            mMobileTypeDescription = typeContentDescription;
            if (FeatureOption.MTK_DT_SUPPORT) {
                mIsDataGeminiIcon = false;
            }
        } else {
            mMobileVisibleGemini = visible;
            mMobileStrengthIdGemini[0] = strengthIcon[0].clone();
            mMobileStrengthIdGemini[1] = strengthIcon[1].clone();
            mMobileActivityIdGemini = activityIcon.clone();
            mMobileTypeIdGemini = typeIcon.clone();
            mMobileDescriptionGemini = contentDescription;
            mMobileTypeDescriptionGemini = typeContentDescription;
            if (FeatureOption.MTK_DT_SUPPORT) {
                mIsDataGeminiIcon = true;
            }
        }
    }

    public void setIsAirplaneMode(boolean is) {
        mIsAirplaneMode = is;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mWifiVisible && mWifiGroup.getContentDescription() != null) {
            event.getText().add(mWifiGroup.getContentDescription());
        }
        if (mMobileVisible && mMobileGroup.getContentDescription() != null) {
            event.getText().add(mMobileGroup.getContentDescription());
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
    
    public void setRoamingFlagandResource(boolean roaming, boolean roamingGemini, int roamingId, int roamingGeminiId) {
        mRoaming = roaming;
        mRoamingGemini = roamingGemini;
        mRoamingId = roamingId;
        mRoamingGeminiId = roamingGeminiId;
    }

    public void setShowSimIndicator(int slotId, boolean showSimIndicator, int simIndicatorResource) {
        Xlog.d(TAG, "setShowSimIndicator(" + slotId + "), showSimIndicator=" + showSimIndicator 
                + " simIndicatorResource = " + simIndicatorResource);
        if (slotId == FeatureOption.GEMINI_SIM_1) {
            mShowSimIndicator = showSimIndicator;
            mSimIndicatorResource = simIndicatorResource;
        } else {
            mShowSimIndicatorGemini = showSimIndicator;
            mSimIndicatorResourceGemini = simIndicatorResource;
        }
    }
    
    public void setDataConnected(int slotId, boolean dataConnected) {
        Xlog.d(TAG, "setDataConnected(" + slotId + "), dataConnected=" + dataConnected);
        if (slotId == FeatureOption.GEMINI_SIM_1) {
            mDataConnected = dataConnected;
            if (FeatureOption.MTK_DT_SUPPORT) {
                mIsDataGeminiIcon = false;
            } else {
                if (mDataConnected) {
                    mDataConnectedGemini = false;
                }
            }
        } else {
            mDataConnectedGemini = dataConnected;
            if (FeatureOption.MTK_DT_SUPPORT) {
                mIsDataGeminiIcon = true;
            } else {
                if (mDataConnectedGemini) {
                    mDataConnected = false;
                }
            }
        }
    }

    public void setDataNetType3G(int slotId, NetworkType dataNetType) {
        Xlog.d(TAG, "setDataNetType3G(" + slotId + "), dataNetType=" + dataNetType);
        if (slotId == FeatureOption.GEMINI_SIM_1) {
            mDataNetType = dataNetType;
            if (FeatureOption.MTK_DT_SUPPORT) {
                mIsDataGeminiIcon = false;
            }
        } else {
            mDataNetTypeGemini = dataNetType;
            if (FeatureOption.MTK_DT_SUPPORT) {
                mIsDataGeminiIcon = true;
            }
        }
    }

    // Run after each indicator change.
    public void apply() {
        if (mWifiGroup == null) {
            return;
        }

        if (mWifiVisible) {
            mWifiGroup.setVisibility(View.VISIBLE);
            mWifi.setImageResource(mWifiStrengthId);
            mWifiActivity.setImageResource(mWifiActivityId);
            mWifiGroup.setContentDescription(mWifiDescription);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) {
            Xlog.d(TAG, String.format("wifi: %s sig=%d act=%d", (mWifiVisible ? "VISIBLE" : "GONE"), mWifiStrengthId,
                    mWifiActivityId));
        }
        
        Xlog.d(TAG, "apply : mShowSimIndicator = " + mShowSimIndicator
                + " mSimIndicatorResource = " + mSimIndicatorResource
                + " mShowSimIndicatorGemini = " + mShowSimIndicatorGemini
                + " mSimIndicatorResourceGemini = " + mSimIndicatorResourceGemini);
        
        if (mMobileVisible) {
            if (mRoaming) {
                mMobileRoam.setBackgroundResource(mRoamingId);
                mMobileRoam.setVisibility(View.VISIBLE);
            } else {
                mMobileRoam.setVisibility(View.GONE);
            }
            
            if (mMobileStrengthId[0].getIconId() == R.drawable.stat_sys_gemini_signal_null
                    || mMobileStrengthId[0].getIconId() == 0) {
                mMobileRoam.setVisibility(View.GONE);
            }

            mMobileGroup.setVisibility(View.VISIBLE);
            if (mMobileStrengthId[0].getResources() != null) {
                mMobile.setImageDrawable(mMobileStrengthId[0].getDrawable());
            } else {
                if (mMobileStrengthId[0].getIconId() == 0) {
                    mMobile.setImageDrawable(null);
                } else {
                    mMobile.setImageResource(mMobileStrengthId[0].getIconId());
                }
            }
            if (mMobileStrengthId[1].getResources() != null) {
                mMobile2.setImageDrawable(mMobileStrengthId[1].getDrawable());
            } else {
                if (mMobileStrengthId[1].getIconId() == 0) {
                    mMobile2.setImageDrawable(null);
                } else {
                    mMobile2.setImageResource(mMobileStrengthId[1].getIconId());
                }
            }
            if (NetworkType.Type_1X3G != mDataNetType) {
                mMobile2.setVisibility(View.GONE);
            }
            Xlog.d(TAG, "apply, mMobileVisible=" + mMobileVisible
                    + " mMobileActivityId=" + mMobileActivityId.getIconId()
                    + " mMobileTypeId=" + mMobileTypeId.getIconId()
                    + " mMobileStrengthId[0] = " + "" + mMobileStrengthId[0].getIconId()
                    + " mMobileStrengthId[1] = " + mMobileStrengthId[1].getIconId());

            if (mMobileActivityId.getResources() != null) {
                mMobileActivity.setImageDrawable(mMobileActivityId.getDrawable());
            } else {
                if (mMobileActivityId.getIconId() == 0) {
                    mMobileActivity.setImageDrawable(null);
                } else {
                    mMobileActivity.setImageResource(mMobileActivityId.getIconId());
                }
            }
            if (mMobileTypeId.getResources() != null) {
                mMobileType.setImageDrawable(mMobileTypeId.getDrawable());
            } else {
                if (mMobileTypeId.getIconId() == 0) {
                    mMobileType.setImageDrawable(null);
                } else {
                    mMobileType.setImageResource(mMobileTypeId.getIconId());
                }
            }

            int state = SIMHelper.getSimIndicatorStateGemini(FeatureOption.GEMINI_SIM_1);
            if (isSimInserted(FeatureOption.GEMINI_SIM_1)
                    && FeatureOption.SIM_INDICATOR_LOCKED != state
                    && FeatureOption.SIM_INDICATOR_SEARCHING != state
                    && FeatureOption.SIM_INDICATOR_INVALID != state
                    && FeatureOption.SIM_INDICATOR_RADIOOFF != state) {
                int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, FeatureOption.GEMINI_SIM_1);
                if (simColorId > -1 && simColorId < 4 && mDataNetType != null) {
                    IconIdWrapper resId = new IconIdWrapper(0);
                    int id = PluginFactory.getStatusBarPlugin(mContext).getDataNetworkTypeIconGemini(mDataNetType, simColorId);
                    if (id != -1) {
                        resId.setResources(PluginFactory.getStatusBarPlugin(mContext).getPluginResources());
                        resId.setIconId(id);
                    }
                    Xlog.d(TAG, "mDataNetType =" + mDataNetType + " resId= " + resId.getIconId() + " simColorId = "
                            + simColorId);
                    if (resId.getResources() != null) {
                        mSignalNetworkType.setImageDrawable(resId.getDrawable());
                    } else {
                        if (resId.getIconId() == 0) {
                            mSignalNetworkType.setImageDrawable(null);
                        } else {
                            mSignalNetworkType.setImageResource(resId.getIconId());
                        }
                    }
                    mSignalNetworkType.setVisibility(View.VISIBLE);
                    if (mMobileStrengthId[0].getIconId() == R.drawable.stat_sys_gemini_signal_null
                            || mMobileStrengthId[0].getIconId() == 0) {
                        mSignalNetworkType.setVisibility(View.GONE);
                    }
                }
            } else {
                mSignalNetworkType.setImageDrawable(null);
                mSignalNetworkType.setVisibility(View.GONE);
            }
            if (mMobileStrengthId[0].getIconId() == PluginFactory.getStatusBarPlugin(mContext)
                    .getSignalStrengthNullIconGemini(FeatureOption.GEMINI_SIM_1)) {
                mMobileSlotIndicator.setVisibility(View.INVISIBLE);
            }
            mMobileGroup.setContentDescription(mMobileTypeDescription + " " + mMobileDescription);
            if (mShowSimIndicator) {
                mSignalClusterCombo.setBackgroundResource(mSimIndicatorResource);
            } else {
                mSignalClusterCombo.setBackgroundDrawable(null);
            }
            mSignalClusterCombo.setPadding(0, 0, 0, 3);
            
            // For OP01 project data type icon should be always displayed
            if (PluginFactory.getStatusBarPlugin(mContext).supportDataTypeAlwaysDisplayWhileOn()) {
                mMobileType.setVisibility(View.VISIBLE);
            } else {
                mMobileType.setVisibility((!mWifiVisible) ? View.VISIBLE : View.GONE);
            }
            
            /// M: When searching hide the data type icon
            int resId = PluginFactory.getStatusBarPlugin(mContext).getSignalStrengthSearchingIconGemini(FeatureOption.GEMINI_SIM_1);
            if (resId == mMobileStrengthId[0].getIconId()) {
                mMobileType.setVisibility(View.GONE);
            }
            
        } else {
            mMobileGroup.setVisibility(View.GONE);
        }
        
        if (mMobileVisibleGemini) {
            if (mRoamingGemini) {
                mMobileRoamGemini.setBackgroundResource(mRoamingGeminiId);
                mMobileRoamGemini.setVisibility(View.VISIBLE);
            } else {
                mMobileRoamGemini.setVisibility(View.GONE);
            }
            
            if (mMobileStrengthIdGemini[0].getIconId() == R.drawable.stat_sys_gemini_signal_null
                    || mMobileStrengthIdGemini[0].getIconId() == 0) {
                mMobileRoamGemini.setVisibility(View.GONE);
            }
            
            mMobileGroupGemini.setVisibility(View.VISIBLE);
            if (mMobileStrengthIdGemini[0].getResources() != null) {
                mMobileGemini.setImageDrawable(mMobileStrengthIdGemini[0].getDrawable());
            } else {
                if (mMobileStrengthIdGemini[0].getIconId() == 0) {
                    mMobileGemini.setImageDrawable(null);
                } else {
                    mMobileGemini.setImageResource(mMobileStrengthIdGemini[0].getIconId());
                }
            }
            if (mMobileStrengthIdGemini[1].getResources() != null) {
                mMobileGemini2.setImageDrawable(mMobileStrengthIdGemini[1].getDrawable());
            } else {
                if (mMobileStrengthIdGemini[1].getIconId() == 0) {
                    mMobileGemini2.setImageDrawable(null);
                } else {
                    mMobileGemini2.setImageResource(mMobileStrengthIdGemini[1].getIconId());
                }
            }
            if (NetworkType.Type_1X3G != mDataNetTypeGemini) {
                mMobileGemini2.setVisibility(View.GONE);
            }
            Xlog.d(TAG, "apply, mMobileVisibleGemini=" + mMobileVisibleGemini
                    + " mMobileActivityIdGemini=" + mMobileActivityIdGemini.getIconId()
                    + " mMobileTypeIdGemini=" + mMobileTypeIdGemini.getIconId()
                    + " mMobileStrengthIdGemini[0] = " + ""
                    + mMobileStrengthIdGemini[0].getIconId()
                    + " mMobileStrengthIdGemini[1] = "
                    + mMobileStrengthIdGemini[1].getIconId());

            if (mMobileActivityIdGemini.getResources() != null) {
                mMobileActivityGemini.setImageDrawable(mMobileActivityIdGemini.getDrawable());
            } else {
                if (mMobileActivityIdGemini.getIconId() == 0) {
                    mMobileActivityGemini.setImageDrawable(null);
                } else {
                    mMobileActivityGemini.setImageResource(mMobileActivityIdGemini.getIconId());
                }
            }
            if (mMobileTypeIdGemini.getResources() != null) {
                mMobileTypeGemini.setImageDrawable(mMobileTypeIdGemini.getDrawable());
            } else {
                if (mMobileTypeIdGemini.getIconId() == 0) {
                    mMobileTypeGemini.setImageDrawable(null);
                } else {
                    mMobileTypeGemini.setImageResource(mMobileTypeIdGemini.getIconId());
                }
            }
            int state = SIMHelper.getSimIndicatorStateGemini(FeatureOption.GEMINI_SIM_2);
            if (isSimInserted(FeatureOption.GEMINI_SIM_2)
                    && FeatureOption.SIM_INDICATOR_LOCKED != state 
                    && FeatureOption.SIM_INDICATOR_SEARCHING != state 
                    && FeatureOption.SIM_INDICATOR_INVALID != state 
                    && FeatureOption.SIM_INDICATOR_RADIOOFF != state) {
                int simColorId = SIMHelper.getSIMColorIdBySlot(mContext, FeatureOption.GEMINI_SIM_2);
                if (simColorId > -1 && simColorId < 4 && mDataNetTypeGemini != null) {
                    IconIdWrapper resId = new IconIdWrapper(0);
                    int id = PluginFactory.getStatusBarPlugin(mContext).getDataNetworkTypeIconGemini(mDataNetTypeGemini, simColorId);
                    if (id != -1) {
                        resId.setResources(PluginFactory.getStatusBarPlugin(mContext).getPluginResources());
                        resId.setIconId(id);
                    }
                    Xlog.d(TAG, "mDataNetTypeGemini =" + mDataNetTypeGemini
                            + " resId= " + resId.getIconId() + " simColorId = "
                            + simColorId);
                    if (resId.getResources() != null) {
                        mSignalNetworkTypeGemini.setImageDrawable(resId.getDrawable());
                    } else {
                        if (resId.getIconId() == 0) {
                            mSignalNetworkTypeGemini.setImageDrawable(null);
                        } else {
                            mSignalNetworkTypeGemini.setImageResource(resId.getIconId());
                        }
                    }
                    mSignalNetworkTypeGemini.setVisibility(View.VISIBLE);
                    if (mMobileStrengthIdGemini[0].getIconId() == R.drawable.stat_sys_gemini_signal_null
                            || mMobileStrengthIdGemini[0].getIconId() == 0) {
                        mSignalNetworkTypeGemini.setVisibility(View.GONE);
                    }
                }
            } else {
                mSignalNetworkTypeGemini.setImageDrawable(null);
                mSignalNetworkTypeGemini.setVisibility(View.GONE);
            }
            if (mMobileStrengthIdGemini[0].getIconId() == PluginFactory.getStatusBarPlugin(mContext)
                    .getSignalStrengthNullIconGemini(FeatureOption.GEMINI_SIM_2)) {
                mMobileSlotIndicatorGemini.setVisibility(View.INVISIBLE);
            }
            mMobileGroupGemini.setContentDescription(mMobileTypeDescriptionGemini + " " + mMobileDescriptionGemini);
            if (mShowSimIndicatorGemini) {
                mSignalClusterComboGemini.setBackgroundResource(mSimIndicatorResourceGemini);
            } else {
                mSignalClusterComboGemini.setBackgroundDrawable(null);
            }
            mSignalClusterComboGemini.setPadding(0, 0, 0, 3);
            
            // For OP01 project data type icon should be always displayed
            if (PluginFactory.getStatusBarPlugin(mContext).supportDataTypeAlwaysDisplayWhileOn()) {
                mMobileTypeGemini.setVisibility(View.VISIBLE);
            } else {
                mMobileTypeGemini.setVisibility((!mWifiVisible) ? View.VISIBLE : View.GONE);
            }
            
            /// M: When searching hide the data type icon
            int resId = PluginFactory.getStatusBarPlugin(mContext).getSignalStrengthSearchingIconGemini(FeatureOption.GEMINI_SIM_2);
            if (resId == mMobileStrengthId[1].getIconId()) {
                mMobileTypeGemini.setVisibility(View.GONE);
            }
        } else {
            mMobileGroupGemini.setVisibility(View.GONE);
        }

        Xlog.d(TAG, "apply, mMobileVisible=" + mMobileVisible
                + ", mWifiVisible=" + mWifiVisible + ", mIsAirplaneMode="
                + mIsAirplaneMode);
        if (mIsAirplaneMode) {
            mMobileGroup.setVisibility(View.GONE);
            mMobile.setVisibility(View.GONE);
            mMobileActivity.setVisibility(View.GONE);
            mMobileType.setVisibility(View.GONE);
            mMobileGroupGemini.setVisibility(View.GONE);
            mMobileGemini.setVisibility(View.GONE);
            
            if (mRoaming) {
                mMobileRoam.setVisibility(View.GONE);
            }
            if (mRoamingGemini) {
                mMobileRoamGemini.setVisibility(View.GONE);
            }

            mMobileActivityGemini.setVisibility(View.GONE);
            mMobileTypeGemini.setVisibility(View.GONE);
            mSpacer.setVisibility(View.GONE);
            mSpacerGemini.setVisibility(View.GONE);
            mMobileSlotIndicator.setVisibility(View.GONE);
            mMobileSlotIndicatorGemini.setVisibility(View.GONE);
            mSignalClusterCombo.setVisibility(View.GONE);
            mSignalNetworkType.setVisibility(View.GONE);
            mSignalClusterComboGemini.setVisibility(View.GONE);
            mSignalNetworkTypeGemini.setVisibility(View.GONE);
            mMobile2.setVisibility(View.GONE);
            mMobileGemini2.setVisibility(View.GONE);
            mFlightMode.setVisibility(View.VISIBLE);
            return;
        } else {
            mMobile.setVisibility(View.VISIBLE);
            mMobileActivity.setVisibility(View.VISIBLE);
            mMobileGemini.setVisibility(View.VISIBLE);
            //Gionee: <guozj><2013-5-3> add for CR00799985 begin
            mMobileSlotIndicator.setVisibility(View.VISIBLE);
            mMobileSlotIndicatorGemini.setVisibility(View.VISIBLE);
            //Gionee: <guozj><2013-5-3> add for CR00799985 end
            mMobileActivityGemini.setVisibility(View.VISIBLE);
            mSpacer.setVisibility(View.VISIBLE);
            mSpacerGemini.setVisibility(View.VISIBLE);
            mSignalClusterCombo.setVisibility(View.VISIBLE);
            mSignalClusterComboGemini.setVisibility(View.VISIBLE);
            mMobile2.setVisibility(View.VISIBLE);
            mMobileGemini2.setVisibility(View.VISIBLE);
            mFlightMode.setVisibility(View.GONE);
        }

        if (mWifiVisible) {
            mSpacer.setVisibility(View.INVISIBLE);
        } else {
            mSpacer.setVisibility(View.GONE);
        }

        if (mMobileVisibleGemini && mMobileVisible) {
            mSpacerGemini.setVisibility(View.INVISIBLE);
        } else {
            mSpacerGemini.setVisibility(View.GONE);
        }

        if (DEBUG) {
            Xlog.d(TAG, String.format("mobile: %s sig=%d act=%d typ=%d", (mMobileVisible ? "VISIBLE" : "GONE"),
                    mMobileStrengthId[0].getIconId(), mMobileActivityId.getIconId(), mMobileTypeId.getIconId()));
            Xlog.d(TAG, String.format("mobile_gemini: %s sig_gemini=%d act_gemini=%d typ_gemini=%d",
                    (mMobileVisibleGemini ? "VISIBLE" : "GONE"), mMobileStrengthIdGemini[0].getIconId(),
                    mMobileActivityIdGemini.getIconId(), mMobileTypeIdGemini.getIconId()));
        }
    }

    private boolean isSimInserted(int slotId) {
        boolean simInserted = false;
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        if (phone != null) {
            try {
                simInserted = GnITelephony.isSimInsert(phone,slotId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Xlog.d(TAG, "isSimInserted(" + slotId + "), SimInserted=" + simInserted);
        return simInserted;
    }
}

