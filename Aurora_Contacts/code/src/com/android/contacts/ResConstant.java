package com.android.contacts;

import com.android.contacts.R;
import com.android.contacts.preference.DisplayOptionsPreferenceFragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import aurora.preference.AuroraPreferenceManager; // import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.ImageView;

public final class ResConstant {
	private ResConstant() {		
	}
	
    //Gionee:huangzy 20130131 add for CR00770449 start
	public interface ContactPhotoStyle {
		int NAMECOLORFUL = 0;
		int HEADCOLORFUL = 1;
		int HIDE = 2;
		int NAME = 3;
		int HEAD = 4;
	}
    //Gionee:huangzy 20130131 add for CR00770449 end
	
    //Gionee:huangzy 20130314 add for CR00784577 start
	public interface ListItemActionStyle {
		String CALL_LOG_PRIMARY_ACTION_KEY = "calllogPrimaryActionKey";
		
		int CALL_PRIMARY = 0;
		int DETAIL_PRIMARY = 1;
	}
    //Gionee:huangzy 20130314 add for CR00784577 end
	
	private static DisplayMetrics sDisplayMetrics;
	public static int sHeaderTextColor;
	public static int sHeaderTextLeftPadding;
	public static int sListItemTextLeftPaddingNoPhoto;
	private static boolean sFouceHideContactListPhoto = false;
	private static boolean sContactPhotoNameSupport = false;
	private static boolean sContactPhotoColorSupport = true;
	private static Paint sContactPhotoNamePaint;
	
    //Gionee:huangzy 20130314 add for CR00784577 start
	private static int sCallLogListItemPrimaryAction;
    //Gionee:huangzy 20130314 add for CR00784577 end
	
	public static void init(Context context) {
		Resources res = context.getResources();
		
		sDisplayMetrics = res.getDisplayMetrics();
		
    	/*Bitmap cardBitmap = BitmapFactory.decodeResource(res, R.drawable.gn_head_text_color);
    	sHeaderTextColor = cardBitmap.getPixel(0, 0);
    	cardBitmap.recycle();*/
		//aurora <wangth> <2013-9-2> add for auroro ui begin 
		/*
    	Drawable d = res.getDrawable(R.drawable.gn_head_text_color);
    	if (null != d && d instanceof BitmapDrawable) {
    		Bitmap cardBitmap = ((BitmapDrawable)d).getBitmap();
    		sHeaderTextColor = cardBitmap.getPixel(0, 0);
    	}
    	
    	sHeaderTextLeftPadding = res.getDimensionPixelOffset(R.dimen.gn_contact_list_item_headertext_left_margin);
    	*/
		sHeaderTextColor = res.getColor(R.color.aurora_contact_list_header_text_color);
		sHeaderTextLeftPadding = res.getDimensionPixelOffset(R.dimen.aurora_contact_list_item_header_padding_left);
		//aurora <wangth> <2013-9-2> add for auroro ui end
    	sListItemTextLeftPaddingNoPhoto = res.getDimensionPixelOffset(R.dimen.gn_contact_list_item_name_left_margin);
    	
    	{
    	    //aurora <wangth> <2013-9-2> add for auroro ui begin
    	    /*
    	    //Goinee:huangzy 20130320 modify for CR00786812 start
    		int defaultValueId = ContactsApplication.sIsColorfulContactPhotoSupport ?
    				R.string.gn_photo_style_options_default_value_v2 :
    					R.string.gn_photo_style_options_default_value;
	    	int defaultStyle = Integer.valueOf(res.getString(defaultValueId));
	        //Goinee:huangzy 20130320 modify for CR00786812 end
	    	String contactPhotoType = AuroraPreferenceManager.getDefaultSharedPreferences(context).getString(
	    			DisplayOptionsPreferenceFragment.PHOTO_STYLE_KEY, String.valueOf(defaultStyle));
	    	setContactPhotoOptions(Integer.valueOf(contactPhotoType));
	    	*/
    	    setContactPhotoOptions(ContactPhotoStyle.HIDE);
    	    //aurora <wangth> <2013-9-2> add for auroro ui end
    	}
    	
        //Gionee:huangzy 20130314 add for CR00784577 start
    	if (ContactsApplication.sIsGnListItemActionAlterable) {
    		int defaultStyle = Integer.valueOf(res.getString(R.string.gn_listitem_onclick_options_default_value));
    		int calllogActionPrimary = AuroraPreferenceManager.getDefaultSharedPreferences(context).getInt(
        			ListItemActionStyle.CALL_LOG_PRIMARY_ACTION_KEY, defaultStyle);
        	setCallLogListItemPrimaryAction(context, calllogActionPrimary);	
    	} else {
    		setCallLogListItemPrimaryAction(context, ListItemActionStyle.DETAIL_PRIMARY);
    	}
    	
        //Gionee:huangzy 20130314 add for CR00784577 end
    }
	
    //Gionee:huangzy 20130131 add for CR00770449 start
	public static boolean isFouceHideContactListPhoto() {
		return sFouceHideContactListPhoto;
	}
	
	public static boolean isContactPhotoNameSupport() {
		return sContactPhotoNameSupport;
	}
	
	public static boolean isContactPhotoColorSupport() {
		return sContactPhotoColorSupport;
	}
	
	public static void setContactPhotoOptions(int type) {
		setContactPhotoOptions(null, type, false);
	}
	
	public static void setContactPhotoOptions(Context context, int type, boolean updatePrefernece) {
		
		switch (type) {
		case ContactPhotoStyle.HIDE:
			sContactPhotoNameSupport = false;
			sFouceHideContactListPhoto = true;
			sContactPhotoColorSupport = false;
			break;
		case ContactPhotoStyle.NAME:
			sContactPhotoNameSupport = true;
			sFouceHideContactListPhoto = false;
			sContactPhotoColorSupport = false;
			break;
		case ContactPhotoStyle.HEAD:
			sContactPhotoNameSupport = false;
			sFouceHideContactListPhoto = false;
			sContactPhotoColorSupport = false;
			break;
		case ContactPhotoStyle.NAMECOLORFUL:
			sContactPhotoNameSupport = true;
			sFouceHideContactListPhoto = false;
			sContactPhotoColorSupport = true;
			break;
		case ContactPhotoStyle.HEADCOLORFUL:
			sContactPhotoNameSupport = false;
			sFouceHideContactListPhoto = false;
			sContactPhotoColorSupport = true;
			break;

		default:
			return;
		}
		
		if (updatePrefernece && null != context) {
			AuroraPreferenceManager.getDefaultSharedPreferences(context).edit().putString(
	    			DisplayOptionsPreferenceFragment.PHOTO_STYLE_KEY,
	    			String.valueOf(type)).commit();
		}
	}
    //Gionee:huangzy 20130131 add for CR00770449 end
	
	public DisplayMetrics getDisplayMetrics() {
		return sDisplayMetrics;
	}
	
	public static Paint getContactPhotoNamePaint() {
		if (null == sContactPhotoNamePaint) {
			Paint paint = new Paint();
	        paint.setTypeface(Typeface.DEFAULT);
	        paint.setAntiAlias(true);
	        paint.setTextAlign(Paint.Align.CENTER);
	        int textSizeInDip = 40;
	        int textSizeInPixel= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
	        		textSizeInDip, sDisplayMetrics);
	        paint.setTextSize(textSizeInPixel);
	        
	        sContactPhotoNamePaint = paint;
		}
		
		int color = 0;
	    //Gionee:huangzy 20130319 add for CR00786443 start
		if (ResConstant.sContactPhotoColorSupport && ResConstant.sContactPhotoNameSupport) {
			color = ContactsApplication.getInstance().getColor(R.color.gn_avatar_text_color_light);
		} else {
			color = ContactsApplication.sIsGnDarkTheme ?
					ContactsApplication.getInstance().getColor(R.color.gn_avatar_text_color_dark) :
						ContactsApplication.getInstance().getColor(R.color.gn_avatar_text_color_light);
		}
	    //Gionee:huangzy 20130319 add for CR00786443 end
		sContactPhotoNamePaint.setColor(color);
		
		return sContactPhotoNamePaint;
	}
	
	public enum IconTpye {
		ADD,
		TRASH,
		SHARE,
		COPY,
		MOVE,
		Setting,
		Account,
		LocalPhone,
		Sim1,
		Sim2,
		Call,
		VedioCall,
		Location
	}
	
	public static int getIconRes(IconTpye it) {
		if (ContactsApplication.sIsGnDarkTheme) {
			switch (it) {
			case ADD:
				return R.drawable.gn_ic_menu_add_dark;
			case TRASH:
				return R.drawable.gn_ic_menu_trash_dark;		
			case SHARE:
				return R.drawable.gn_ic_menu_share_dark;
			case COPY:
				return R.drawable.gn_ic_menu_copy_dark;
			case MOVE:
				return R.drawable.gn_ic_menu_move_dark;
			case Setting:
				return R.drawable.gn_ic_menu_settings_holo_dark;
			case Account:
				return R.drawable.gn_ic_menu_contacts_holo_dark;
			case LocalPhone:
				return R.drawable.ic_contact_account_phone_dark;
			case Sim1:
				return R.drawable.ic_contact_account_sim1_dark;
			case Sim2:
				return R.drawable.ic_contact_account_sim2_dark;
			case Call:
				return R.drawable.gn_phone_num_icon_dark;
			case VedioCall:
				return R.drawable.gn_ic_video_call_dark;
			case Location:
				return R.drawable.ic_location_dark;
			default:
				break;
			}	
		} else {
			switch (it) {
			case ADD:
				return R.drawable.gn_ic_menu_add_light;
			case TRASH:
				return R.drawable.gn_ic_menu_trash_light;		
			case SHARE:
				return R.drawable.gn_ic_menu_share_light;
			case COPY:
				return R.drawable.gn_ic_menu_copy_light;
			case MOVE:
				return R.drawable.gn_ic_menu_move_light;
			case Setting:
				return R.drawable.ic_menu_settings_holo_light;
			case Account:
				return R.drawable.ic_menu_contacts_holo_light;
			case LocalPhone:
				return R.drawable.ic_contact_account_phone;
			case Sim1:
				return R.drawable.ic_contact_account_sim1;
			case Sim2:
				return R.drawable.ic_contact_account_sim2;
			case Call:
				return R.drawable.gn_phone_num_icon;
			case VedioCall:
				return R.drawable.gn_ic_video_call;
			case Location:
				return R.drawable.ic_location_light;
			default:
				break;
			}
		}
		
		return 0;
	}

    //Gionee:huangzy 20130314 add for CR00784577 start
	public static int getCallLogListItemPrimaryAction() {
		return sCallLogListItemPrimaryAction;
	}
	
	public static void setCallLogListItemPrimaryAction(Context context, int primary) {
		sCallLogListItemPrimaryAction = primary;
		
		AuroraPreferenceManager.getDefaultSharedPreferences(context).edit().putInt(
    			ListItemActionStyle.CALL_LOG_PRIMARY_ACTION_KEY,
    			primary).commit();
	}
	
	public static boolean isCallLogListItemCallPrimary() {
		return sCallLogListItemPrimaryAction == ListItemActionStyle.CALL_PRIMARY;
	}
    //Gionee:huangzy 20130314 add for CR00784577 end
}
