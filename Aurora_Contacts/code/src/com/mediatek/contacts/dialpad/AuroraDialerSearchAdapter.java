package com.mediatek.contacts.dialpad;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.CallLog.Calls;
import gionee.provider.GnContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
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
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.widget.AuroraDialerSearchItemView;
import com.mediatek.contacts.CallOptionHandler;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.mediatek.contacts.dialpad.IDialerSearchController.GnDialerSearchResultColumns;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.android.contacts.util.GnCallForSelectSim;
import com.android.contacts.GNContactsUtils;

public class AuroraDialerSearchAdapter extends CursorAdapter implements OnClickListener {
    private static final String TAG = "DialerSearchAdapter";
    
    protected Context mContext;

    protected int mSpanColorFg;
    protected int mSpanColorBg;

    protected ContactPhotoManager mContactPhotoManager;
    protected CallLogSimInfoHelper mCallLogSimInfoHelper;

    protected SIMInfoWrapper mSIMInfoWrapper;

    protected DisplayMetrics mDisplayMetrics;

    protected int mOperatorVerticalPadding;
    protected int mOperatorHorizontalPadding;

    protected HyphonManager mHyphonManager;
    protected CallOptionHandler mCallOptionHandler;

    public AuroraDialerSearchAdapter(Context context) {
        super(context, null, false);
        mContext = context;
        
        mSpanColorFg = Color.WHITE;
        mSpanColorBg = context.getResources().getColor(R.color.aurora_search_matched_highlight);

        mSIMInfoWrapper = SIMInfoWrapper.getDefault();

        mContactPhotoManager = ContactPhotoManager.getInstance(context);
        mCallLogSimInfoHelper = new CallLogSimInfoHelper(mContext.getResources());

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

    public AuroraDialerSearchAdapter(Context context, CallOptionHandler callOptionHandler) {
        this(context);
        mCallOptionHandler = callOptionHandler;
    }

    public void bindView(View view, Context context, Cursor cursor) {
    	final int contact_id = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
    	
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
    	
    	if (contact_id > 0) {
    		bindViewDialerSearch(view, context, cursor);
    	} else if (contact_id < 0) {
    		bindViewHotLines(view, context, cursor);
    	} else {
    		bindViewCallLogs(view, context, cursor);
    	}
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    	View view = AuroraDialerSearchItemView.create(context);
        view.setId(R.id.dialer_search_item_view);               
        return view;
    }

    protected String formatNumber(String number) {
        if(TextUtils.isEmpty(number))
            return number;

        return PhoneNumberUtils.formatNumber(number);
    }

    protected Uri getContactUri(Cursor cursor) {
        final String lookup = cursor.getString(GnDialerSearchResultColumns.LOOKUP_KEY_INDEX);
        final int contactId = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
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
				if (ContactsApplication.sIsGnDualSimSelectSupport) {
	            	Intent intent = IntentFactory.newGnDualSimSelectIntent(v, number);
	            	context.startActivity(intent);
	            } else {
	            	Intent intent = IntentFactory.newDialNumberIntent(number);
	            	//aurora add liguangyu 20131206 start
	            	intent.putExtra("contactUri", contactUri);
	            	//aurora add liguangyu 20131206 end
	            	if(simId != -1) {
	            		int slot = SIMInfoWrapper.getDefault().getInsertedSimSlotById(simId);
	            		intent.putExtra(ContactsUtils.getSlotExtraKey(), slot);
	            	}
	            	context.startActivity(intent);
	            }
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
    
    private void bindViewDialerSearch(View view, Context context, Cursor cursor) {
        AuroraDialerSearchItemView dsiv = (AuroraDialerSearchItemView) view;
        dsiv.mMainContainer.setVisibility(View.VISIBLE);
        dsiv.mCallLog.setVisibility(View.GONE);
        
        final String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
        final String formatNumber = mHyphonManager.formatNumber(number);
        int indicate = cursor.getInt(GnDialerSearchResultColumns.INDICATE_PHONE_SIM_INDEX);
        int contactId = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
        Uri contactUri = getContactUri(cursor);
        //aurora add liguangyu 20131224 start
        final String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAora(mContext, number, false);
        dsiv.mArea.setVisibility(!TextUtils.isEmpty(area) ? View.VISIBLE : View.GONE);
        dsiv.mArea.setText(area);
        //aurora add liguangyu 20131224 end
        
        if (!TextUtils.isEmpty(formatNumber)) {
            highlightNumber(dsiv.mNumber, formatNumber, cursor);
        } else {
        	dsiv.mNumber.setText(null);
        }
                
        highlightName(dsiv.mName, cursor);
        
        long is_privacy = cursor.getLong(GnDialerSearchResultColumns.IS_PRIVACY_INDEX);

        dsiv.mSecondaryButton.setTag(new String[]{formatNumber, contactUri.toString(), String.valueOf(is_privacy)});
        dsiv.mSecondaryButton.setOnClickListener(this);

        if (!cursor.isLast()) {
        	cursor.moveToNext();
        	int nextContactId = cursor.getInt(GnDialerSearchResultColumns.CONTACT_ID_INDEX);
        	cursor.moveToPrevious();        	
        }
    }
    
    private void bindViewHotLines(View view, Context context, Cursor cursor) {
    	AuroraDialerSearchItemView dsiv = (AuroraDialerSearchItemView) view;
        dsiv.mMainContainer.setVisibility(View.VISIBLE);
        dsiv.mCallLog.setVisibility(View.GONE);
        
        final String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
        final String formatNumber = mHyphonManager.formatNumber(number);
        final String name = cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);
        final String photoName = cursor.getString(GnDialerSearchResultColumns.PHOTO_ID_INDEX);
        //aurora add liguangyu 20131224 start
        dsiv.mArea.setVisibility(View.GONE);
        //aurora add liguangyu 20131224 end
        
        highlightName(dsiv.mName, cursor);
        highlightNumber(dsiv.mNumber, formatNumber, cursor);
        
        dsiv.mSecondaryButton.setTag(new String[]{formatNumber});
        dsiv.mSecondaryButton.setOnClickListener(this);
    }
    
    private void highlightNumber(TextView tv, String formatNumber, Cursor cursor) {
    	if (!TextUtils.isEmpty(formatNumber)) {
            String numberHighlight = cursor.getString(GnDialerSearchResultColumns.DATA_HIGHLIGHT_INDEX);
            if (null != numberHighlight && numberHighlight.length() == 2) {
                try {
                    SpannableStringBuilder numberStyle = new SpannableStringBuilder(
                            formatNumber);
                    int start = numberHighlight.charAt(0);
                    int end = numberHighlight.charAt(1);

                    char[] numChars = formatNumber.toCharArray();
                    for (int s = 0; s <= start; ++s) {
                        if (' ' == numChars[s] || '-' == numChars[s]) {
                            ++start;
                            ++end;
                        }
                    }

                    for (int e = start + 1; e <= end && e < numChars.length; ++e) {
                        if (' ' == numChars[e] || '-' == numChars[e]) {
                            ++end;
                        }
                    }
                    
                    if (end > formatNumber.length()) {
                    	end = formatNumber.length();
                    }

                    numberStyle.setSpan(new ForegroundColorSpan(mSpanColorBg),
                            start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    tv.setText(numberStyle);
                    return;
                } catch (ArrayIndexOutOfBoundsException ae) {
                    ae.printStackTrace();
                } catch (IndexOutOfBoundsException ie) {
                    ie.printStackTrace();
                }
            }
    		
    		tv.setText(formatNumber);
        }
    }
    
    private void highlightName(TextView tv, Cursor cursor) {      
        String name = cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);
        String pinyin = cursor.getString(GnDialerSearchResultColumns.PINYIN_INDEX);
        
        if (TextUtils.isEmpty(name)) {
            String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
            if (!TextUtils.isEmpty(number)) {
                tv.setText(number);
                setVisibility(tv, View.VISIBLE);
                return;
            }
            
            tv.setText(null);
            return;
        }
        
        if (!TextUtils.isEmpty(pinyin)) {
            String pinyinHighlight = cursor.getString(GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX);
            if (pinyinHighlight != null) {
                SpannableStringBuilder namestyle = new SpannableStringBuilder(name);
                SpannableStringBuilder pinyinstyle = new SpannableStringBuilder(pinyin);
                if (!name.equals(pinyin)) {
                    ArrayList<Integer> nameToken = new ArrayList<Integer>();
                    int nameIndex = 0;
                    int pinyinIndex = 0;
                    int pinyinLength = pinyin.length();
                    for (int i = 0; i < pinyinLength; i++) {
                        char c = pinyin.charAt(i);
                        if (0 == i && c != name.charAt(nameIndex)) {
                            pinyinIndex++;
                            continue;
                        } else if (i > 0 && (nameIndex + 1) < name.length() &&
                                c != name.charAt(nameIndex + 1) && ('a' <= c && c <= 'z')) {
                            pinyinIndex++;
                            continue;
                        } else if (i > 0 && (nameIndex + 1) == name.length()) {
                            if (pinyinIndex < pinyinLength && ('A' <= c && c <= 'Z')) {
                                nameToken.add(pinyinIndex);
                                pinyinIndex++;
                                continue;
                            }
                            
                            pinyinIndex++;
                            if (pinyinIndex == pinyinLength) {
                                nameToken.add(pinyinIndex);
                                break;
                            }
                        } else {
                            if (pinyinIndex > 0) {
                                nameToken.add(pinyinIndex);
                            }
                            nameIndex++;
                            pinyinIndex++;
                        }
                    }
                    
                    
                    if (nameToken.size() <= 0) {
                        tv.setText(name);
                        setVisibility(tv, View.VISIBLE);
                        return;
                    }
                    
                    if (pinyin.charAt(pinyin.length() - 1) == name.charAt(name.length() - 1)) {
                        nameToken.add(nameToken.get(nameToken.size() - 1) + 1);
                    }
                    
//                    for (int i : nameToken) {
//                        Log.d(TAG, " = " + i);
//                    }
                    
                    for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
                        int start = (int) pinyinHighlight.charAt(i);
                        int end = (int) pinyinHighlight.charAt(i + 1);
                        int startIndex = 0;
                        int endIndex = 0;
//                        Log.d(TAG, "start = " + start + "  end = " + end);
                        if (start ==0 && end == 0) {
                            break;
                        }
                        
                        for (int temp = 0; temp < nameToken.size(); temp++) {
                            if (start == 0) {
                                startIndex = 0;
                            } else if (start == nameToken.get(temp)) {
                                startIndex = temp + 1;
                            }
                            
                            if (end > nameToken.get(nameToken.size() - 1)) {
                                endIndex = nameToken.size();
                            } else if (end == nameToken.get(temp)) {
                                endIndex = temp + 1;
                            } else if (end > nameToken.get(temp) && end < nameToken.get(temp + 1)) {
                                endIndex = temp + 2;
                            } else if (end <= nameToken.get(0)) {
                                endIndex = 1;
                            }
//                            Log.d(TAG, "startIndex = " + startIndex + "  endIndex = " + endIndex);
                        }
                        
                        try {
                            namestyle.setSpan(new ForegroundColorSpan(mSpanColorBg), startIndex, endIndex,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    tv.setText(namestyle);
                    setVisibility(tv, View.VISIBLE);
                    return;
                }
                
                for (int i = 0, len = pinyinHighlight.length(); i < len; i = i + 2) {
                    int start = (int) pinyinHighlight.charAt(i);
                    int end = (int) pinyinHighlight.charAt(i + 1);
                    pinyinstyle.setSpan(new ForegroundColorSpan(mSpanColorBg), start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                
                tv.setText(pinyinstyle);
                setVisibility(tv, View.VISIBLE);
                return;
            }
        }
        
        tv.setText(name);
        setVisibility(tv, View.VISIBLE);
    }
    
    void setVisibility(View view, int visibility) {
    	if (view.getVisibility() != visibility) {
    		view.setVisibility(visibility);
    	}
    }
    
    //aurora add liguangyu 20131224 begin
    private void bindViewCallLogs(View view, Context context, Cursor cursor) {
    	AuroraDialerSearchItemView dsivO = (AuroraDialerSearchItemView) view;
    	dsivO.mMainContainer.setVisibility(View.GONE);
    	dsivO.mCallLog.setVisibility(View.VISIBLE);
    	
    	AuroraCallLogListItemViewV2 dsiv = (AuroraCallLogListItemViewV2)view.findViewById(R.id.aurora_primary_action_view);    
        
        final String number = cursor.getString(GnDialerSearchResultColumns.PHONE_NUMBER_INDEX);
        final String formatNumber = mHyphonManager.formatNumber(number);
		final long date = cursor.getLong(GnDialerSearchResultColumns.PINYIN_INDEX);
		final long duration = cursor.getLong(GnDialerSearchResultColumns.PINYIN_HIGHLIGHT_INDEX);
		final int type = cursor.getInt(GnDialerSearchResultColumns.PHOTO_ID_INDEX);
        String area = cursor.getString(GnDialerSearchResultColumns.NAME_INDEX);
		final int simId = cursor.getInt(GnDialerSearchResultColumns.INDEX_IN_SIM_INDEX);

        
		CharSequence dateText = DateUtils.getRelativeTimeSpanString(
				date, System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);		
		dsiv.mDate.setText(dateText.toString().replaceAll(" ", ""));
		if (type == Calls.MISSED_TYPE) {
//			dsiv.mName.setTextColor(mMissedCallColor);
			dsiv.mDuration.setVisibility(View.VISIBLE);	
			if (type == Calls.MISSED_TYPE) {
				if (!TextUtils.isEmpty(area)) {
					dsiv.mDuration.setText(mCallTypeResHolder.getCallDurationText(
							type, duration)+"，");
				} else {
					dsiv.mDuration.setText(mCallTypeResHolder.getCallDurationText(
							type,duration));
				}	
			}
		} else {
//			dsiv.mName.setTextColor(mNormalCallColor);
			dsiv.mDuration.setVisibility(View.GONE);
		}
		int callTypeRes = mCallTypeResHolder.getCallTypeDrawable(
				type,
				0,
				false);
		dsiv.mCallType.setImageResource(callTypeRes);
		
       	if(GNContactsUtils.isMultiSimEnabled()) {
       		dsiv.mSimIcon.setVisibility(View.VISIBLE);
	        int iconRes = ContactsUtils.getSimConvertIcon(context, simId);
	        dsiv.mSimIcon.setImageResource(iconRes);
//	        dsiv.mSimIcon.setAlpha(0.7f);
       	} else {
       		dsiv.mSimIcon.setVisibility(View.GONE);
       	}
        
        dsiv.mArea.setText(area);
        highlightNumber(dsiv.mName, formatNumber, cursor);
        
        dsivO.mSecondaryButton.setTag(new String[]{formatNumber, simId+""});
//        dsiv.mSecondaryButton.setOnClickListener(this);
        
		
    }
    //aurora add liguangyu 20131224 end
    
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

}
