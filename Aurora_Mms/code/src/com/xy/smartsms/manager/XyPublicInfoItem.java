/******************************************************************************* 
 * Copyright (C) 2012-2015 Microfountain Technology, Inc. All Rights Reserved. 
 * 
 * Unauthorized copying of this file, via any medium is strictly prohibited.   
 * Proprietary and confidential
 * 
 * Last Modified: 2015-9-25 19:17:14
 ******************************************************************************/
package com.xy.smartsms.manager;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.ui.publicinfo.PublicInfoManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ConversationListItem;
import com.aurora.mms.ui.AuroraMmsQuickContactBadge;
import com.xy.smartsms.iface.IXYConversationListItemHolder;

public class XyPublicInfoItem {

    IXYConversationListItemHolder mIXYConversationListItemHolder;
    TextView mFromView;
    ImageView mImageView;
    public static Handler mHandler = new Handler() {

    };

    private Context mContext;
    public XyPublicInfoItem (Context context) {
        mContext = context;
    }

    private Spannable resettext(String text) {
        Spannable.Factory sf = Spannable.Factory.getInstance();
        Spannable sp = sf.newSpannable(text);
        int index = text.lastIndexOf(" | ");
        if (sp != null && index > 0) {
            sp.setSpan(new TextAppearanceSpan(mContext, R.style.aurora_conv_list_purpose_text_style), index + 2, text.length(), 0);
        }
        return sp;
    }

    public synchronized void bindTextImageView(
            final IXYConversationListItemHolder iXYConversationListItemHolder,
            // Aurora xuyong 2016-02-26 modified for aurora 2.0 new feature start
            final TextView fromView, final TextView purposeView, final ImageView imageView, final Contact contact, Conversation conv) {
            // Aurora xuyong 2016-02-26 modified for aurora 2.0 new feature end
        // Aurora xuyong 2016-01-30 modified for xy-smartsmst start
        if (setImage(imageView, contact)) {
            if (iXYConversationListItemHolder instanceof  ConversationListItem) {
                fromView.setText(((ConversationListItem) iXYConversationListItemHolder).formatMessage(conv));
            }
            // Aurora xuyong 2016-03-05 added for xy-smartsms start
            if (purposeView != null) {
                purposeView.setVisibility(View.GONE);
            }
            // Aurora xuyong 2016-03-05 added for xy-smartsms end
            return;
        // Aurora xuyong 2016-01-30 modified for xy-smartsmst end
        }

        this.mIXYConversationListItemHolder = iXYConversationListItemHolder;
        String phoneNumber = mIXYConversationListItemHolder.getPhoneNumber();
        final String phoneNum = phoneNumber;
        if (StringUtils.isPhoneNumber(phoneNumber)) {
            if (iXYConversationListItemHolder instanceof  ConversationListItem) {
                fromView.setText(((ConversationListItem)iXYConversationListItemHolder).formatMessage(conv));
            }
            // Aurora xuyong 2016-03-05 added for xy-smartsms start
            if (purposeView != null) {
                purposeView.setVisibility(View.GONE);
            }
            // Aurora xuyong 2016-03-05 added for xy-smartsms end
            return;
        }
        phoneNumber = StringUtils.getPhoneNumberNo86(phoneNumber);
        this.mFromView = fromView;
        this.mImageView = imageView;
        this.mFromView.setTag(phoneNumber);
        if (mImageView != null) {
            this.mImageView.setTag(phoneNumber);
        }

        final JSONObject json = PublicInfoManager
                .getPublicInfoByPhoneIncache(phoneNumber);
        if (json != null) {
            String name = json.optString("name");
            if (!StringUtils.isNull(name)) {
                fromView.setText(name);
                // Aurora xuyong 2016-02-26 added for aurora 2.0 new feature start
                String purpose = json.optString("purpose");
                if (!StringUtils.isNull(purpose)) {
                    if (purposeView != null) {
                        fromView.setText(name + " | ");
                        purposeView.setText(purpose);
                        purposeView.setVisibility(View.VISIBLE);
                    } else {
                        fromView.setText(resettext(name + " | " + purpose));
                    }
                } else {
                    if (purposeView != null) {
                        purposeView.setVisibility(View.GONE);
                    }
                }
                // Aurora xuyong 2016-02-26 added for aurora 2.0 new feature end
            } else {
                if (purposeView != null) {
                    purposeView.setVisibility(View.GONE);
                }
                if (iXYConversationListItemHolder instanceof  ConversationListItem) {
                    fromView.setText(((ConversationListItem)iXYConversationListItemHolder).formatMessage(conv));
                }
            }
            final String logoName = json.optString("logoc");
            if (TextUtils.isEmpty(logoName)) {
                setImage(imageView, null, contact, mContext);
                return;
            }
            BitmapDrawable bitmap = PublicInfoManager.getLogoDrawable(logoName);
            if (bitmap != null) {
                setImage(imageView, bitmap, contact, mContext);
            } else if (!mIXYConversationListItemHolder.isScrolling()) {
//                // 是否快速滚动
                PublicInfoManager.publicInfoPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final BitmapDrawable bd = PublicInfoManager
                                .findLogoByLogoName(logoName, null);
                        if (bd == null) {
                            setImage(imageView, null, contact, mContext);
                            return;
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!phoneNum
                                        .equals(mIXYConversationListItemHolder
                                                .getPhoneNumber())) {
                                    setImage(imageView, null, contact, mContext);
                                    return;
                                }
                                setImage(imageView, bd, contact, mContext);
                            }
                        });
                    }
                });
                
                
            }
        } else if (!mIXYConversationListItemHolder.isScrolling()) {
            if (purposeView != null) {
                purposeView.setVisibility(View.GONE);
            }
            setImage(imageView, null, contact, mContext);
            if (iXYConversationListItemHolder instanceof  ConversationListItem) {
                fromView.setText(((ConversationListItem)iXYConversationListItemHolder).formatMessage(conv));
            }
            SdkCallBack callBack = new SdkCallBack() {
                @Override
                public void execute(final Object... obj) {
                    try {
                        if (obj != null && obj.length > 3) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!phoneNum
                                            .equals(mIXYConversationListItemHolder
                                                    .getPhoneNumber())) {
                                        setImage(imageView, null, contact, mContext);
                                        return;
                                    }
                                    final String name = (String) obj[1];
                                    final BitmapDrawable bd = (BitmapDrawable) obj[3];
                                    if (!StringUtils.isNull(name)) {
                                        fromView.setText(name);
                                    }
                                    setImage(imageView, bd, contact, mContext);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            PublicInfoManager.loadPublicInfo(Constant.getContext(),
                    phoneNumber, callBack);
        } else {
            if (purposeView != null) {
                purposeView.setVisibility(View.GONE);
            }
            setImage(imageView, null, contact, mContext);
            if (iXYConversationListItemHolder instanceof  ConversationListItem) {
                fromView.setText(((ConversationListItem)iXYConversationListItemHolder).formatMessage(conv));
            }
        }
    }
    // Aurora xuyong 2016-01-30 added for xy-smartsmst start
    private boolean setImage(ImageView imageView, Contact contact) {
        if (contact != null) {
            String contactInfo = contact.getUri() + AuroraMmsQuickContactBadge.CONTACT_INFO_DIVIDER + contact.getNumber()
                    + AuroraMmsQuickContactBadge.CONTACT_INFO_DIVIDER + contact.getPrivacy();
            Drawable existContactDrawable = contact.getAvatar(mContext, null);
            if (imageView instanceof AuroraMmsQuickContactBadge) {
                ((AuroraMmsQuickContactBadge) imageView).addContactDrawable(contactInfo, existContactDrawable);
            }
            if (existContactDrawable != null) {
                return true;
            }
        }
        return false;
    }
    // Aurora xuyong 2016-01-30 added for xy-smartsmst end
    public void setImage(ImageView mImageView, BitmapDrawable bd, Contact contact, Context context) {
        try {
            if (mImageView == null)
                return;
            String contactInfo = null;
            if (contact != null) {
                contactInfo = contact.getUri() + AuroraMmsQuickContactBadge.CONTACT_INFO_DIVIDER + contact.getNumber()
                        + AuroraMmsQuickContactBadge.CONTACT_INFO_DIVIDER + contact.getPrivacy();
            }
            if (mImageView instanceof AuroraMmsQuickContactBadge) {
                ((AuroraMmsQuickContactBadge) mImageView).addContactDrawable(contactInfo, bd);
            } else {
                mImageView.setImageDrawable(bd);
            }/*
            mImageView.requestLayout();

            mImageView.invalidate();*/
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}
