package com.mediatek.contacts.dialpad;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.calllog.AuroraCallLogListItemViewV2;
import com.android.contacts.calllog.CallLogResHolder;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.widget.AuroraDialerSearchItemView;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.calllog.CallLogSubInfoHelper;
import com.mediatek.contacts.dialpad.IDialerSearchController.AuroraDialerSearchResultColumns;
import com.android.contacts.GNContactsUtils;

import android.provider.ContactsContract.DialerSearch;
import android.text.TextWatcher;
import android.text.Editable;

public class AuroraDialerSearchAdapter extends CursorAdapter implements OnClickListener {
    private static final String TAG = "AuroraDialerSearchAdapter";
    
     
    private Context mContext;

     
     
    private int mSpanColorFg;
    private int mSpanColorBg;

     
    private ContactPhotoManager mContactPhotoManager;
     
    private CallLogSubInfoHelper mCallLogSubInfoHelper;


     
    protected DisplayMetrics mDisplayMetrics;

     
    private int mOperatorVerticalPadding;
    private int mOperatorHorizontalPadding;

    private HyphonManager mHyphonManager;

    public AuroraDialerSearchAdapter(Context context) {
        super(context, null, false);
        mContext = context;
        
        mSpanColorFg = Color.WHITE;
        mSpanColorBg = context.getResources().getColor(R.color.aurora_search_matched_highlight);


        mContactPhotoManager = ContactPhotoManager.getInstance(context);
        mCallLogSubInfoHelper = new CallLogSubInfoHelper(mContext.getResources());

        final Resources r = mContext.getResources();
        mOperatorVerticalPadding = r.getDimensionPixelSize(R.dimen.dialpad_operator_vertical_padding);
        mOperatorHorizontalPadding = r.getDimensionPixelSize(R.dimen.dialpad_operator_horizontal_padding);

        mHyphonManager = HyphonManager.getInstance();
		mMissedCallColor = context.getResources().getColor(
				R.color.gn_misscall_color);
		mNormalCallColor = context.getResources().getColor(
				R.color.aurora_dialer_search_text_color);
		mCallTypeResHolder = new CallLogResHolder(context);
    }
  

    public void bindView(View view, Context context, Cursor cursor) {
    	final int contact_id = cursor.getInt(AuroraDialerSearchResultColumns.CONTACT_ID_INDEX);
    	
    	if (view instanceof AuroraDialerSearchItemView) {
    		AuroraDialerSearchItemView itemView = (AuroraDialerSearchItemView)view;
    		itemView.setOnClickListener(this);
    		//aurora change liguangyu 201311125 for BUG #1022 start
//    		itemView.mSecondaryButton.setBackgroundResource(R.drawable.aurora_call_log_detail);
    		if(contact_id <= 0) {
    			itemView.mSecondaryButton.setVisibility(View.GONE);
    		} else {
    			itemView.mSecondaryButton.setVisibility(View.VISIBLE);
    		}    		
    		//aurora change liguangyu 201311125 for BUG #1022 end    		
    	}
    	
//    	if (contact_id > 0) {
    		bindViewDialerSearchV2(view, context, cursor);
//    	} else {
//    		bindViewCallLogs(view, context, cursor);
//    	}
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    	View view = AuroraDialerSearchItemView.create(context);
//        view.setId(R.id.dialer_search_item_view);               
        return view;
    }

    protected String formatNumber(String number) {
        if(TextUtils.isEmpty(number))
            return number;

        return PhoneNumberUtils.formatNumber(number);
    }

    protected Uri getContactUri(Cursor cursor) {
        final String lookup = cursor.getString(AuroraDialerSearchResultColumns.CONTACT_NAME_LOOKUP_INDEX);
        final int contactId = cursor.getInt(AuroraDialerSearchResultColumns.CONTACT_ID_INDEX);
        return Contacts.getLookupUri(contactId, lookup);
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            mContactPhotoManager.pause();
        } else {
            mContactPhotoManager.resume();
            notifyDataSetChanged();
        }
    }
   
    public class ViewHolder {
        public QuickContactBadge photo;
        public ImageButton call;
        public TextView name;
        public TextView labelAndNumber;
        public ImageView type;
        public TextView operator;
        public TextView date;

        public int callId;
        public int viewType;
        public Uri contactUri;
        public View divider;
    }

    public void onClick(View v) {
        //Gionee:huangzy 20130314 modify for CR00784577 start
    	Context context = mContext;
    	boolean isCallPrimary = ResConstant.isCallLogListItemCallPrimary();
		boolean isPrimaryClick = false;
		Uri contactUri = null;
		String number = null;
		int simId = -1;
		//aurora add liguangyu 20140829 for start
		boolean isHotLine = false;
		//aurora add liguangyu 20140829 for end
		
		boolean isPrivacyContact = false;
		
		{
			Object obj = null;
			View secondActonView = null;
			
			if (v instanceof AuroraDialerSearchItemView) {
				secondActonView = ((AuroraDialerSearchItemView)v).mSecondaryButton;
				isPrimaryClick = true;
			} else {
				secondActonView = v;
				isPrimaryClick = false;
			}
			
			if (null != secondActonView) {
				obj = secondActonView.getTag();
				if (null != obj && obj instanceof String[]) {
					String[] strs = (String[])obj;
					//aurora modify liguangyu 20140829 for start
					if (3 == strs.length) {
						number = strs[0];
						mNumber = number;
						if (null != strs[1]) {
							contactUri = Uri.parse(strs[1]);
							mContactUri = contactUri;
						}
						
						if (null != strs[2]) {
							isPrivacyContact = Long.valueOf(strs[2]) > 0 ? true : false;
						}
					} else if (2 == strs.length) {
						number = strs[0];
						if(null != strs[1]) {
							simId = Integer.valueOf(strs[1]);
						}
						mNumber = null;
						mContactUri = null;
					} else {
						number = strs[0];
						isHotLine = true;
						mNumber = null;
						mContactUri = null;
					}
					//aurora modify liguangyu 20140829 for end
				}
			}
		}
		
		
		
//		if ((isPrimaryClick && isCallPrimary) || ((!isPrimaryClick && !isCallPrimary))) {
		if (!((isPrimaryClick && isCallPrimary) || ((!isPrimaryClick && !isCallPrimary)))) {//aurora change zhouxiaobing 20130918
			//call
			
			//aurora add liguangyu 20140829 for start
//			if(isHotLine && GNContactsUtils.isMultiSimEnabled()) {
			if(GNContactsUtils.isMultiSimEnabled() && ContactsUtils.isShowDoubleButton()) {
				if(mOnDialCompleteListener != null) {
					mOnDialCompleteListener.onHotLineFill(number);
				}
				return;
			}
			//aurora add liguangyu 20140829 for end
		     long now = SystemClock.uptimeMillis();
	         if (now < mLastCallActionTime + 2000) {
	        	 return;
	         }
			 mLastCallActionTime = now;
			if (!TextUtils.isEmpty(number)) {
				
            	Intent intent = IntentFactory.newDialNumberIntent(number);
            	//aurora add liguangyu 20131206 start
            	intent.putExtra("contactUri", contactUri);
            	//aurora add liguangyu 20131206 end
            	if(simId != -1) {    
            		int slot = ContactsUtils.getSlotBySubId(simId);
            		intent.putExtra(ContactsUtils.getSlotExtraKey(), slot);
            	}
            	context.startActivity(intent);
	            
				if(mOnDialCompleteListener != null) {
					mOnDialCompleteListener.onDialComplete();
				}
			}
		} else {
			//detail
			if (null != contactUri) {
				Intent intent = IntentFactory.newViewContactIntent(contactUri);
				intent.putExtra("is_privacy_contact", isPrivacyContact);
				context.startActivity(intent);
				if(mOnDialCompleteListener != null) {
					mOnDialCompleteListener.onDialComplete();
				}
			}
		}
	}
    

    
   
    
    void setVisibility(View view, int visibility) {
    	if (view.getVisibility() != visibility) {
    		view.setVisibility(visibility);
    	}
    }
    
   
    
	private int mNormalCallColor;
	private final int mMissedCallColor;
	private CallLogResHolder mCallTypeResHolder;
	private long mLastCallActionTime;
	
	public interface OnDialCompleteListener {
		void onDialComplete();
		//aurora add liguangyu 20140829 for start
		void onHotLineFill(String number);
		//aurora add liguangyu 20140829 for end
	}
	
	private OnDialCompleteListener mOnDialCompleteListener = null;
	
	public void setDialCompleteListener(OnDialCompleteListener l) {
		mOnDialCompleteListener = l;
	} 
	
	private Uri mContactUri;
	public Uri getContactUri() {
		return mContactUri;
	}
	
	private String mNumber;
	public String getDialNumber() {
		return mNumber;
	}
	
	public void resetLastClickResult(){
		mNumber = null;
		mContactUri = null;
	}

    public void onResume() {
    }
    
    public void setResultCursor(Cursor cursor) {
    	changeCursor(cursor);
    }
    
    
    private void bindViewDialerSearchV2(View view, Context context, Cursor cursor) {
        AuroraDialerSearchItemView dsiv = (AuroraDialerSearchItemView) view;
        dsiv.mMainContainer.setVisibility(View.VISIBLE);
        dsiv.mCallLog.setVisibility(View.GONE);
        
        final String number = cursor.getString(AuroraDialerSearchResultColumns.SEARCH_PHONE_NUMBER_INDEX);
        final String formatNumber = mHyphonManager.formatNumber(number);
        int indicate = cursor.getInt(AuroraDialerSearchResultColumns.INDICATE_PHONE_SIM_INDEX);
        int contactId = cursor.getInt(AuroraDialerSearchResultColumns.CONTACT_ID_INDEX);
        Uri contactUri = getContactUri(cursor);
        //aurora add liguangyu 20131224 start
        final String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAora(mContext, number, false);
        dsiv.mArea.setVisibility(!TextUtils.isEmpty(area) ? View.VISIBLE : View.GONE);
        dsiv.mArea.setText(area);
        //aurora add liguangyu 20131224 end
        
    	String name = cursor.getString(AuroraDialerSearchResultColumns.NAME_INDEX);
        
        if(TextUtils.isEmpty(name)){
        	 if (!TextUtils.isEmpty(formatNumber)) {
        		 ContactsUtils.highlightNumber(dsiv.mName, formatNumber, cursor, mSpanColorBg);
             } else {
             	dsiv.mName.setText(null);
             }
        	dsiv.mNumber.setText(null);
        } else {
            if (!TextUtils.isEmpty(formatNumber)) {
            	ContactsUtils.highlightNumber(dsiv.mNumber, formatNumber, cursor, mSpanColorBg);
            } else {
            	dsiv.mNumber.setText(null);
            }
            ContactsUtils.highlightName(dsiv.mName, cursor, mSpanColorBg);
        } 

        
        long is_privacy = cursor.getLong(AuroraDialerSearchResultColumns.IS_PRIVACY_INDEX);
//
        dsiv.mSecondaryButton.setTag(new String[]{formatNumber, contactUri.toString(), String.valueOf(is_privacy)});
//        dsiv.mSecondaryButton.setTag(new String[]{formatNumber, contactUri.toString(), String.valueOf(0l)});
        dsiv.mSecondaryButton.setOnClickListener(this);

        if (!cursor.isLast()) {
        	cursor.moveToNext();
        	int nextContactId = cursor.getInt(AuroraDialerSearchResultColumns.CONTACT_ID_INDEX);
        	cursor.moveToPrevious();        	
        }
    } 
	
    
}
