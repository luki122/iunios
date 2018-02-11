package com.android.mms.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
// Aurora xuyong 2014-05-05 added for aurora's new feature start
import java.util.Iterator;
// Aurora xuyong 2014-05-05 added for aurora's new feature end
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
// Aurora xuyong 2013-11-15 modified for S4 adapt start
import gionee.provider.GnContactsContract.Contacts;
// Aurora xuyong 2013-11-15 modified for S4 adapt end
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Presence;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Profile;
import android.provider.Telephony.Mms;
import com.android.mms.util.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
// Aurora xuyong 2014-11-14 added for bug #9809 start
import android.database.sqlite.SQLiteException;
// Aurora xuyong 2014-11-14 added for bug #9809 end
import android.database.sqlite.SqliteWrapper;
// Aurora xuyong 2014-05-05 added for aurora's new feature start
import com.android.mms.ui.ConversationListItem;
// Aurora xuyong 2014-05-05 added for aurora's new feature end
import com.android.mms.ui.MessageUtils;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.gionee.featureoption.FeatureOption;
import com.gionee.mms.ui.GnHotLinesUtil;

//Gionee:tianxiaolong 2012.9.3 modify for CR00686770 begin
import android.os.SystemProperties;
//Gionee:tianxiaolong 2012.9.3 modify for CR00686770 end

//gionee gaoj 2012-9-20 added for CR00699291 start
import java.util.Map;
//gionee gaoj 2012-9-20 added for CR00699291 end
import android.content.AsyncQueryHandler;

//gionee gaoj 2013-2-19 adde for CR00771935 start
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetrics;
import aurora.preference.AuroraPreferenceManager;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.ImageView;
import com.android.mms.ui.MessagingPreferenceActivity;
//gionee gaoj 2013-2-19 adde for CR00771935 end

public class Contact {
    public static final int CONTACT_METHOD_TYPE_UNKNOWN = 0;
    public static final int CONTACT_METHOD_TYPE_PHONE = 1;
    public static final int CONTACT_METHOD_TYPE_EMAIL = 2;
    public static final int CONTACT_METHOD_TYPE_SELF = 3;       // the "Me" or profile contact
    public static final String TEL_SCHEME = "tel";
    public static final String CONTENT_SCHEME = "content";
    private static final int CONTACT_METHOD_ID_UNKNOWN = -1;
    private static final String TAG = "Contact";
    private static final String M_TAG = "Mms/Contact";
    private static final boolean V = false;
    private static ContactsCache sContactCache;
    private static final String SELF_ITEM_KEY = "Self_Item_Key";
    private boolean mIsValid = false;

    //Gionee:tianxiaolong 2012.9.3 modify for CR00686770 begin
    private static boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
    //Gionee:tianxiaolong 2012.9.3 modify for CR00686770 end

    //Gionee:lixiaohu 2012.10.25 added for CR00721677 begin
    private static int MATCH_NUM_LEN = SystemProperties.getInt("ro.gn.match.numberlength", 11);
    static {
        if (true == SystemProperties.get("ro.gn.oversea.custom").equals("ITALY_NGM")){
            MATCH_NUM_LEN = 9;
        }
    }
    //Gionee:lixiaohu 2012.10.25 added for CR00721677 end
    
//    private static final ContentObserver sContactsObserver = new ContentObserver(new Handler()) {
//        @Override
//        public void onChange(boolean selfUpdate) {
//            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
//                log("contact changed, invalidate cache");
//            }
//            invalidateCache();
//        }
//    };

    private static final ContentObserver sPresenceObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfUpdate) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("presence changed, invalidate cache");
            }
            invalidateCache();
        }
    };

    private final static HashSet<UpdateListener> mListeners = new HashSet<UpdateListener>();

    private long mContactMethodId;   // Id in phone or email Uri returned by provider of current
                                     // Contact, -1 is invalid. e.g. contact method id is 20 when
                                     // current contact has phone content://.../phones/20.
    private int mContactMethodType;
    private String mNumber;
    private String mNumberE164;
    private String mName;
    private String mNameAndNumber;   // for display, e.g. Fred Flintstone <670-782-1123>
    private boolean mNumberIsModified; // true if the number is modified

    private long mRecipientId;       // used to find the Recipient cache entry
    private String mLabel;
    private long mPersonId;
    private int mPresenceResId;      // TODO: make this a state instead of a res ID
    private String mPresenceText;
    private BitmapDrawable mAvatar;
    protected byte [] mAvatarData;
    private boolean mIsStale;
    private boolean mQueryPending;
    private boolean mIsMe;          // true if this contact is me!
    private boolean mSendToVoicemail;   // true if this contact should not put up notification
    //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
    private int mIndicatePhoneOrSim;
    //Gionee <guoyx> <2013-05-30> add for CR00820739 end
    public static final int NORMAL_NUMBER_MAX_LENGTH = 15; // Normal number length. For example: +8613012345678

    //gionee gaoj 2012-12-11 added for CR00742048 start
    private Drawable mDrawable;
    private boolean mIsHotline;
    //gionee gaoj 2012-12-11 added for CR00742048 end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private long mPrivacy = 0;
    // Aurora xuyong 2014-10-23 added for privacy feature end
    // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature start
    private int mPhotoId = -1;
    // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature end
    public interface UpdateListener {
        public void onUpdate(Contact updated);
    }

    private Contact(String number, String name) {
        init(number, name);
    }
    /*
     * Make a basic contact object with a phone number.
     */
    private Contact(String number) {
        init(number, "");
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    private Contact(String number, long privacy) {
        init(number, "");
        mPrivacy = privacy;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    private Contact(boolean isMe) {
        init(SELF_ITEM_KEY, "");
        mIsMe = isMe;
    }

    private void init(String number, String name) {
        mContactMethodId = CONTACT_METHOD_ID_UNKNOWN;
        mName = name;
        setNumber(number);
        mNumberIsModified = false;
        mLabel = "";
        mPersonId = 0;
        mPresenceResId = 0;
        mIsStale = true;
        mSendToVoicemail = false;
        //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
        mIndicatePhoneOrSim = -1;
        //Gionee <guoyx> <2013-05-30> add for CR00820739 end
    }

    @Override
    public String toString() {
        return String.format("{ number=%s, name=%s, nameAndNumber=%s, label=%s, person_id=%d, hash=%d method_id=%d }",
                (mNumber != null ? mNumber : "null"),
                (mName != null ? mName : "null"),
                (mNameAndNumber != null ? mNameAndNumber : "null"),
                (mLabel != null ? mLabel : "null"),
                mPersonId, hashCode(),
                mContactMethodId);
    }

    private static void logWithTrace(String msg, Object... format) {
        Thread current = Thread.currentThread();
        StackTraceElement[] stack = current.getStackTrace();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(current.getId());
        sb.append("] ");
        sb.append(String.format(msg, format));

        sb.append(" <- ");
        int stop = stack.length > 7 ? 7 : stack.length;
        for (int i = 3; i < stop; i++) {
            String methodName = stack[i].getMethodName();
            sb.append(methodName);
            if ((i+1) != stop) {
                sb.append(" <- ");
            }
        }

        Log.d(TAG, sb.toString());
    }

    //gionee gaoj 2012-9-20 added for CR00699291 CR00705234 start
    public static void gninit(Context context) {
        sContactCache.gninit(context);
    }
    //gionee gaoj 2012-9-20 added for CR00699291 CR00705234 end

    public static Contact get(String number, boolean canBlock) {
        return sContactCache.get(number, canBlock);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public static Contact get(String number, boolean canBlock, long privacy) {
        return sContactCache.get(number, canBlock, privacy);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    public static Contact getMe(boolean canBlock) {
        return sContactCache.getMe(canBlock);
    }

    public static List<Contact> getByPhoneUris(Parcelable[] uris) {
        return sContactCache.getContactInfoForPhoneUris(uris);
    }
    
    //a0
    public static List<Contact> getByPhoneIds(long[] ids) {
        return sContactCache.getContactInfoForPhoneIds(ids);
    }
    //a1

    public static void invalidateCache() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("invalidateCache");
        }

        // While invalidating our local Cache doesn't remove the contacts, it will mark them
        // stale so the next time we're asked for a particular contact, we'll return that
        // stale contact and at the same time, fire off an asyncUpdateContact to update
        // that contact's info in the background. UI elements using the contact typically
        // call addListener() so they immediately get notified when the contact has been
        // updated with the latest info. They redraw themselves when we call the
        // listener's onUpdate().
        sContactCache.invalidate();
    }

    public boolean isMe() {
        return mIsMe;
    }

    private static String emptyIfNull(String s) {
        return (s != null ? s : "");
    }

    /**
     * Fomat the name and number.
     *
     * @param name
     * @param number
     * @param numberE164 the number's E.164 representation, is used to get the
     *        country the number belongs to.
     * @return the formatted name and number
     */
    public static String formatNameAndNumber(String name, String number, String numberE164) {
        // Format like this: Mike Cleron <(650) 555-1234>
        //                   Erick Tseng <(650) 555-1212>
        //                   Tutankhamun <tutank1341@gmail.com>
        //                   (408) 555-1289
        String formattedNumber = number;
        //m0
//        if (!Mms.isEmailAddress(number)) {
//            formattedNumber = PhoneNumberUtils.formatNumber(number, numberE164,
//                    MmsApp.getApplication().getCurrentCountryIso());
//        }
        //m1

        if (!TextUtils.isEmpty(name) && !name.equals(number)) {
            return name + " <" + formattedNumber + ">";
        } else {
            return formattedNumber;
        }
    }

    public synchronized void reload() {
        mIsStale = true;
        sContactCache.get(mNumber, false);
    }

    public synchronized String getNumber() {
        return mNumber;
    }

    public synchronized void setNumber(String number) {
        //m0
        /*if (!Mms.isEmailAddress(number)) {
            mNumber = PhoneNumberUtils.formatNumber(number, mNumberE164,
                    MmsApp.getApplication().getCurrentCountryIso());
        } else {
            mNumber = number;
        }*/
        mNumber = getValidNumber(number);
        //m1
        notSynchronizedUpdateNameAndNumber();
        mNumberIsModified = true;
    }

    public boolean isNumberModified() {
        return mNumberIsModified;
    }

    public boolean getSendToVoicemail() {
        return mSendToVoicemail;
    }

    public void setIsNumberModified(boolean flag) {
        mNumberIsModified = flag;
    }

    public synchronized String getName() {
        if (TextUtils.isEmpty(mName)) {
            return mNumber;
        } else {
            return mName;
        }
    }
    // Aurora xuyong 2014-07-02 added for reject feature start
    public synchronized String getNameOnly() {
        return mName;
    }
    // Aurora xuyong 2014-07-02 added for reject feature end
    public synchronized String getNameAndNumber() {
        return mNameAndNumber;
    }

    private void notSynchronizedUpdateNameAndNumber() {
        mNameAndNumber = formatNameAndNumber(mName, mNumber, mNumberE164);
    }

    public synchronized long getRecipientId() {
        return mRecipientId;
    }

    public synchronized void setRecipientId(long id) {
        mRecipientId = id;
    }

    public synchronized String getLabel() {
        return mLabel;
    }

    public synchronized Uri getUri() {
        return ContentUris.withAppendedId(Contacts.CONTENT_URI, mPersonId);
    }

    public synchronized int getPresenceResId() {
        return mPresenceResId;
    }

    public synchronized boolean existsInDatabase() {
        return (mPersonId > 0);
    }
    
    //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
    public synchronized int getIndicatePhoneOrSim() {
        return mIndicatePhoneOrSim;
    }
    //Gionee <guoyx> <2013-05-30> add for CR00820739 end

    public static void addListener(UpdateListener l) {
        synchronized (mListeners) {
            mListeners.add(l);
        }
    }

    public static void removeListener(UpdateListener l) {
        synchronized (mListeners) {
            mListeners.remove(l);
        }
    }
    // Aurora xuyong 2014-05-05 added for aurora's new feature start
    public static void removeAllConvListeItemListener() {
        synchronized (mListeners) {
            Iterator iterator;
            iterator = mListeners.iterator();
            while (iterator.hasNext()) {
                UpdateListener listener = (UpdateListener) iterator.next();
                if (listener instanceof ConversationListItem) {
                    iterator.remove();
                }
            }
        }
    }
    // Aurora xuyong 2014-05-05 added for aurora's new feature end
    public static void dumpListeners() {
        synchronized (mListeners) {
            int i = 0;
            Log.i(TAG, "[Contact] dumpListeners; size=" + mListeners.size());
            for (UpdateListener listener : mListeners) {
                Log.i(TAG, "["+ (i++) + "]" + listener);
            }
        }
    }

    public synchronized boolean isEmail() {
        return Mms.isEmailAddress(mNumber);
    }

    public String getPresenceText() {
        return mPresenceText;
    }

    public int getContactMethodType() {
        return mContactMethodType;
    }

    public long getContactMethodId() {
        return mContactMethodId;
    }

    public synchronized Uri getPhoneUri() {
        if (existsInDatabase()) {
            return ContentUris.withAppendedId(Phone.CONTENT_URI, mContactMethodId);
        } else {
            Uri.Builder ub = new Uri.Builder();
            ub.scheme(TEL_SCHEME);
            ub.encodedOpaquePart(mNumber);
            return ub.build();
        }
    }

    public synchronized Drawable getAvatar(Context context, Drawable defaultValue) {
        if (mAvatar == null) {
            if (mAvatarData != null) {
                Bitmap b = BitmapFactory.decodeByteArray(mAvatarData, 0, mAvatarData.length);
                mAvatar = new BitmapDrawable(context.getResources(), b);
            }
        }
        return mAvatar != null ? mAvatar : defaultValue;
    }

    //gionee gaoj 2013-2-19 adde for CR00771935 start
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    public synchronized Drawable getAvatar(Context context, Drawable defaultValue, ImageView view, boolean isSelf) {
        if (mAvatar == null) {
            if (mAvatarData != null) {
                Bitmap b = BitmapFactory.decodeByteArray(mAvatarData, 0, mAvatarData.length);
                mAvatar = new BitmapDrawable(context.getResources(), b);
            }
        }
        if (mAvatar != null) {
            return mAvatar;
        }/* else {
            if (sContactPhotoNameSupport && !isSelf) {
                Drawable nameDrawable = setContactNameAsPhoto(view, false, MmsApp.mDarkTheme, 0);
                if (null != nameDrawable) {
                    return nameDrawable;
                }
            }
            if (sContactPhotoColorSupport) {
                Drawable colorDrawable = setColorfulDefaultContactPhoto(view, false, MmsApp.mDarkTheme, 0);
                if (null != colorDrawable) {
                    return colorDrawable;
                }
            }*/
            return defaultValue;
        //}
    }
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    //gionee gaoj 2013-2-19 adde for CR00771935 end

    //gionee gaoj 2012-12-11 added for CR00742048 start
    public synchronized Drawable getDrawable(Context context, Drawable defaultValue) {
        if (mDrawable == null) {
            return defaultValue;
        } else {
            return mDrawable;
        }
    }

    public synchronized boolean getHotLine() {
        if (mIsHotline) {
            return true;
        } else {
            return false;
        }
    }
    //gionee gaoj 2012-12-11 added for CR00742048 end

    public static void init(final Context context) {
        sContactCache = new ContactsCache(context);

        RecipientIdCache.init(context);

        // it maybe too aggressive to listen for *any* contact changes, and rebuild MMS contact
        // cache each time that occurs. Unless we can get targeted updates for the contacts we
        // care about(which probably won't happen for a long time), we probably should just
        // invalidate cache peoridically, or surgically.
        /*
        context.getContentResolver().registerContentObserver(
                Contacts.CONTENT_URI, true, sContactsObserver);
        */
        
        //gionee gaoj 2013-2-19 adde for CR00771935 start
        mContext = context;
          // Aurora liugj 2013-12-10 modified for app start optimize start
        /*String contactPhotoType = AuroraPreferenceManager.getDefaultSharedPreferences(context).getString(
                MessagingPreferenceActivity.PHOTO_STYLE_KEY, mContext.getResources().getString(R.string.gn_photo_style_options_default_value));*/
        setContactPhotoOptions(/*Integer.valueOf(contactPhotoType)*/0);
         // Aurora liugj 2013-12-10 modified for app start optimize end
        //gionee gaoj 2013-2-19 adde for CR00771935 end
    }

    //gionee gaoj 2013-2-19 adde for CR00771935 start
    public static boolean sFouceHideContactListPhoto = false;
    private static boolean sContactPhotoNameSupport = true;
    private static boolean sContactPhotoColorSupport = true;
    private static Paint sContactPhotoNamePaint;
    private static Context mContext;

    public interface ContactPhotoStyle {
        int NAMECOLORFUL = 0;
        int HEADCOLORFUL = 1;
        int HIDE = 2;
        int NAME = 3;
        int HEAD = 4;
    }

    public static void setContactPhotoOptions(int type) {
        setContactPhotoOptions(null, type, false);
    }

    public static void setContactPhotoOptions(Context context, int type, boolean updatePrefernece) {
        if (!FeatureOption.GN_FEATURE_PHOTO_STYLE) {
            type = 4;
        }
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
                    MessagingPreferenceActivity.PHOTO_STYLE_KEY,
                    String.valueOf(type)).commit();
        }
    }

    public static void setContactPhotoViewTag(ImageView photoView, String name, int position, boolean fouceTagNull) {
        if (null == photoView) {
            return;
        }
        
        if (fouceTagNull) {
            photoView.setTag(null);
            return;
        }
        
        if (sContactPhotoNameSupport) {
            photoView.setTag(new Pair<Integer, String>(position, name));
        } else if (sContactPhotoColorSupport) {
            photoView.setTag(new Pair<Integer, String>(position, null));
        } else {
            photoView.setTag(null);
        }
    }
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    //private static Bitmap[] mContactNameBitmaps = new Bitmap[12];
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end
    private static float[] mContactNameCoordinate;
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature start
    /*public static Drawable setContactNameAsPhoto(ImageView view, boolean hires, boolean darkTheme, int type) {
        Drawable drawable = null;
        if (null == view || hires || type < 0 || type > 2) {
            return null;
        }
        
        if (!(view.getTag() instanceof Pair<?, ?>)) {
            return null;
        }
        
        Pair<Integer, String> pair = (Pair<Integer, String>) view.getTag(); 
        String name = pair.second;
        int position = pair.first;
        int index = type;
        if (!TextUtils.isEmpty(name)) {
            if (name.length() > 1) {
                name = name.substring(name.length() - 1);   
            }
            
            /*if (name.matches("[0-9a-zA-Z]*")) {
                return false;
            }*/
            /*if (!name.matches("[\\u4e00-\\u9fa5]+")) {
                return null;
            }
            
            if (sContactPhotoColorSupport) {
                index += (position%CONTACT_TOTAL_COLOR + 1) * CONTACT_TOTAL_COLOR;
            }
            
            int icRes = 0;
            Bitmap bm = mContactNameBitmaps[index];
            if (null == bm) {
                switch (index) {
                case 0:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_dark :
                        R.drawable.gn_dial_ic_contact_name_picutre_light;   
                    break;
                case 1:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_dark :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim1_light;  
                    break;
                case 2:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_dark :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim2_light;
                    break;
                case 3:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_color1 :
                        R.drawable.gn_dial_ic_contact_name_picutre_color1;
                    break;
                case 4:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_color1 :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim1_color1;
                    break;
                case 5:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_color1 :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim2_color1;
                    break;
                case 6:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_color2 :
                        R.drawable.gn_dial_ic_contact_name_picutre_color2;
                    break;
                case 7:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_color2 :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim1_color2;
                    break;
                case 8:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_color2 :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim2_color2;
                    break;
                case 9:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_color3 :
                        R.drawable.gn_dial_ic_contact_name_picutre_color3;
                    break;
                case 10:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim1_color3 :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim1_color3;
                    break;
                case 11:
                    icRes = darkTheme ? R.drawable.gn_dial_ic_contact_name_picutre_sim2_color3 :
                        R.drawable.gn_dial_ic_contact_name_picutre_sim2_color3;
                    break;
                default:
                    return null;
                }
                
                mContactNameBitmaps[index] = bm = ((BitmapDrawable)view.getContext().getResources().getDrawable(icRes))
                        .getBitmap();
            }
            
            if (null != bm) {
                int width = bm.getWidth();
                int height = bm.getHeight();
                Bitmap newbm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                Canvas canvas = new Canvas(newbm);
                Paint paint = getContactPhotoNamePaint();
                if (null == mContactNameCoordinate) {
                    mContactNameCoordinate = new float[2];
                    FontMetrics fontMetrics = paint.getFontMetrics();
                    float fontHeight = fontMetrics.bottom - fontMetrics.top;
                    mContactNameCoordinate[0] = width>>1;
                    mContactNameCoordinate[1] = height - (height - fontHeight) / 2 - fontMetrics.bottom;                    
                }
                
                canvas.drawBitmap(bm, 0, 0, paint);
                canvas.drawText(name, mContactNameCoordinate[0], mContactNameCoordinate[1], paint);
                canvas.save(Canvas.ALL_SAVE_FLAG);
                //view.setImageBitmap(newbm);
                drawable = new BitmapDrawable(newbm);
                canvas.restore();
            }
        }
        return drawable;
    }*/
    // Aurora xuyong 2014-05-05 deleted for aurora's new feature end

    public static Paint getContactPhotoNamePaint() {
        if (null == sContactPhotoNamePaint) {
            Paint paint = new Paint();
            paint.setTypeface(Typeface.DEFAULT);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            int textSizeInDip = 40;
            int textSizeInPixel= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    textSizeInDip, mContext.getResources().getDisplayMetrics());
            paint.setTextSize(textSizeInPixel);
            
            sContactPhotoNamePaint = paint;
        }
        
        int color = 0;
        if (sContactPhotoColorSupport && sContactPhotoNameSupport) {
            color = 0xffffffff;
        } else {
            color = MmsApp.mDarkTheme ? 0xff999999 : 0xffffffff;
        }
        sContactPhotoNamePaint.setColor(color);
        
        return sContactPhotoNamePaint;
    }
    private static final int CONTACT_TOTAL_COLOR = 3;
    private static Bitmap[] mContactColorfulBitmaps = new Bitmap[9];
    /*public static Drawable setColorfulDefaultContactPhoto(ImageView view, boolean hires, boolean darkTheme, int type) {
        Drawable drawable = null;
        if ((view.getTag() instanceof Pair<?, ?>)) {
            Pair<Integer, String> pair = (Pair<Integer, String>) view.getTag();
            
            int colorIndex = pair.first%CONTACT_TOTAL_COLOR;
            int bmIndex = colorIndex + type*CONTACT_TOTAL_COLOR;
            
            int icRes = 0;
            Bitmap bm = mContactColorfulBitmaps[bmIndex];
            if (null == bm) {
                switch (type) {
                case 0:
                    switch (colorIndex) {
                    case 0:
                        icRes = (R.drawable.ic_contact_picture_holo_color1);
                        break;
                    case 1:
                        icRes = (R.drawable.ic_contact_picture_holo_color2);
                        break;
                    case 2:
                        icRes = (R.drawable.ic_contact_picture_holo_color3);
                        break;
                    }
                    break;
                case 1:
                    switch (colorIndex) {
                    case 0:
                        icRes = (R.drawable.ic_contact_picture_sim1_holo_color1);
                        break;
                    case 1:
                        icRes = (R.drawable.ic_contact_picture_sim1_holo_color2);
                        break;
                    case 2:
                        icRes = (R.drawable.ic_contact_picture_sim1_holo_color3);
                        break;
                    }
                    break;
                case 2:
                    switch (colorIndex) {
                    case 0:
                        icRes = (R.drawable.ic_contact_picture_sim2_holo_color1);
                        break;
                    case 1:
                        icRes = (R.drawable.ic_contact_picture_sim2_holo_color2);
                        break;
                    case 2:
                        icRes = (R.drawable.ic_contact_picture_sim2_holo_color3);
                        break;
                    }
                    break;
                }
                
                if (0 != icRes) {
                    mContactColorfulBitmaps[bmIndex] = bm = BitmapFactory.decodeResource(
                            view.getContext().getResources(), icRes);
                }
            }
            
            if (null != bm) {
                //view.setImageBitmap(bm);
                drawable = new BitmapDrawable(bm);
            }
        }
        return drawable;
    }*/
    //gionee gaoj 2013-2-19 adde for CR00771935 end

    public static void dump() {
        sContactCache.dump();
    }

    private static class ContactsCache {
        private final TaskStack mTaskQueue = new TaskStack();
        private final TaskStack mTaskQueue2 = new TaskStack();
        private boolean selectTask = true;
        private static final String SEPARATOR = ";";

        /**
         * For a specified phone number, 2 rows were inserted into phone_lookup
         * table. One is the phone number's E164 representation, and another is
         * one's normalized format. If the phone number's normalized format in
         * the lookup table is the suffix of the given number's one, it is
         * treated as matched CallerId. E164 format number must fully equal.
         *
         * For example: Both 650-123-4567 and +1 (650) 123-4567 will match the
         * normalized number 6501234567 in the phone lookup.
         *
         *  The min_match is used to narrow down the candidates for the final
         * comparison.
         */
        // query params for caller id lookup
        private static final String CALLER_ID_SELECTION = " Data._ID IN "
                + " (SELECT DISTINCT lookup.data_id "
                + " FROM "
                    + " (SELECT data_id, normalized_number, length(normalized_number) as len "
                    + " FROM phone_lookup "
                    + " WHERE min_match = ?) AS lookup "
                    + ")";

        // query params for caller id lookup without E164 number as param
        private static final String CALLER_ID_SELECTION_WITHOUT_E164 =  " Data._ID IN "
                + " (SELECT DISTINCT lookup.data_id "
                + " FROM "
                    + " (SELECT data_id, normalized_number, length(normalized_number) as len "
                    + " FROM phone_lookup "
                    + " WHERE min_match = ?) AS lookup "
                + " WHERE "
                    + " (lookup.len <= ? AND "
                        + " substr(?, ? - lookup.len + 1) = lookup.normalized_number))";

        private static final String CALLER_ID_SELECTION_EXACT_MATCH =  " Data._ID IN "
                + " (SELECT DISTINCT lookup.data_id "
                + " FROM "
                    + " (SELECT data_id, normalized_number, length(normalized_number) as len "
                    + " FROM phone_lookup "
                    + " WHERE normalized_number = ?) AS lookup "
                + " WHERE "
                    + " (lookup.len <= ? AND "
                        + " substr(?, ? - lookup.len + 1) = lookup.normalized_number))";

        // Utilizing private API
        private static final Uri PHONES_WITH_PRESENCE_URI = Data.CONTENT_URI;

        private static final String[] CALLER_ID_PROJECTION = new String[] {
                Phone._ID,                      // 0
                Phone.NUMBER,                   // 1
                Phone.LABEL,                    // 2
                Phone.DISPLAY_NAME,             // 3
                Phone.CONTACT_ID,               // 4
                Phone.CONTACT_PRESENCE,         // 5
                Phone.CONTACT_STATUS,           // 6
                Phone.NORMALIZED_NUMBER,        // 7
                Contacts.SEND_TO_VOICEMAIL,      // 8
                //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
                // Aurora xuyong 2014-10-23 modified for privacy feature start
                Contacts.INDICATE_PHONE_SIM,     // 9
                // Aurora xuyong 2014-10-23 modified for privacy feature end
                //Gionee <guoyx> <2013-05-30> add for CR00820739 end
                // Aurora xuyong 2014-10-23 added for privacy feature start
                // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature start
                "is_privacy",                     // 10
                // Aurora xuyong 2016-01-23 modfiied for aurora 2.0 new feature end
                // Aurora xuyong 2014-10-23 added for privacy feature end
                // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature start
                Phone.PHOTO_ID
                // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature end
        };

        private static final int PHONE_ID_COLUMN = 0;
        private static final int PHONE_NUMBER_COLUMN = 1;
        private static final int PHONE_LABEL_COLUMN = 2;
        private static final int CONTACT_NAME_COLUMN = 3;
        private static final int CONTACT_ID_COLUMN = 4;
        private static final int CONTACT_PRESENCE_COLUMN = 5;
        private static final int CONTACT_STATUS_COLUMN = 6;
        private static final int PHONE_NORMALIZED_NUMBER = 7;
        private static final int SEND_TO_VOICEMAIL = 8;
        //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
        private static final int INDICATE_PHONE_SIM = 9;
        //Gionee <guoyx> <2013-05-30> add for CR00820739 end
        // Aurora xuyong 2014-10-23 added for privacy feature start
        private static final int IS_PRIVACY = 10;
        // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature start
        private static final int PHOTO_ID = 11;
        // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature end
        // Aurora xuyong 2014-10-23 added for privacy feature end
        private static final String[] SELF_PROJECTION = new String[] {
                Phone._ID,                      // 0
                Phone.DISPLAY_NAME,             // 1
        };

        private static final int SELF_ID_COLUMN = 0;
        private static final int SELF_NAME_COLUMN = 1;

        // query params for contact lookup by email
        private static final Uri EMAIL_WITH_PRESENCE_URI = Data.CONTENT_URI;

        private static final String EMAIL_SELECTION = "UPPER(" + Email.DATA + ")=UPPER(?) AND "
                + Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'";

        private static final String[] EMAIL_PROJECTION = new String[] {
                Email._ID,                    // 0
                Email.DISPLAY_NAME,           // 1
                Email.CONTACT_PRESENCE,       // 2
                Email.CONTACT_ID,             // 3
                Phone.DISPLAY_NAME,           // 4
                Contacts.SEND_TO_VOICEMAIL    // 5
        };
        private static final int EMAIL_ID_COLUMN = 0;
        private static final int EMAIL_NAME_COLUMN = 1;
        private static final int EMAIL_STATUS_COLUMN = 2;
        private static final int EMAIL_CONTACT_ID_COLUMN = 3;
        private static final int EMAIL_CONTACT_NAME_COLUMN = 4;
        private static final int EMAIL_SEND_TO_VOICEMAIL_COLUMN = 5;

        private final Context mContext;

        private final HashMap<String, ArrayList<Contact>> mContactsHash =
            new HashMap<String, ArrayList<Contact>>();

        // gionee lwzh modify for CR00774362 20130227. remove all code about gnContactInfoMap;
        //gionee gaoj 2012-9-20 added for CR00699291 start
        // private static Map<String, Contact> gnContactInfoMap = new HashMap<String, Contact>();

        private static final Uri PICK_PHONE_EMAIL_URI = Uri
                .parse("content://com.android.contacts/data/phone_email");

        private static boolean isContactMapInited = false;

        private int mContactNum = -1;

        private DbChangeResolver mResolver = null;
        //gionee gaoj 2012-9-20 added for CR00699291 end

        //gionee gaoj 2012-9-28 added for CR00705234 start
        private QueryHandler mQueryHandler;
        private static final int CONTACT_QUERY_TOKEN = 10;
        //gionee gaoj 2012-9-28 added for CR00705234 end

        //gionee gaoj 2012-10-15 modified for CR00705539 start
        private int mGetItemNum = 0;
        //gionee gaoj 2012-10-15 modified for CR00705539 end

        private ContactsCache(Context context) {
            mContext = context;
            //gionee gaoj 2012-9-20 added for CR00699291 start
            mResolver = new DbChangeResolver(new Handler());
            context.getContentResolver().registerContentObserver(PICK_PHONE_EMAIL_URI, true, mResolver);
            //gionee gaoj 2012-9-20 added for CR00699291 end
        }

        //gionee gaoj 2012-9-20 added for CR00699291 CR00705234 start
        public void gninit(Context context) {
            // TODO Auto-generated method stub
            if (mQueryHandler == null) {
                mQueryHandler = new QueryHandler(context);
            }
            initContactInfoMap();
        }

        class DbChangeResolver extends ContentObserver {

            public DbChangeResolver(Handler handler) {
                super(handler);
                // TODO Auto-generated constructor stub
            }

            @Override
            public void onChange(boolean selfChange) {
                // TODO Auto-generated method stub
                super.onChange(selfChange);
                if (mQueryHandler != null) {
                    mQueryHandler.removeCallbacks(mQueryRunnable);
                    mQueryHandler.postDelayed(mQueryRunnable, 300);
                }
            }
        }

        private Runnable mQueryRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                isContactMapInited = false;
                initContactInfoMap();
            }
        };

        private void initContactInfoMap() {
            // TODO Auto-generated method stub
            mQueryHandler.cancelOperation(CONTACT_QUERY_TOKEN);
            mQueryHandler.startQuery(CONTACT_QUERY_TOKEN, null,
                    PICK_PHONE_EMAIL_URI, CALLER_ID_PROJECTION, null, null,
                    null);
        }

        private class QueryHandler extends AsyncQueryHandler {

            Context mContext;

            public QueryHandler(Context context) {
                super(context.getContentResolver());
                // TODO Auto-generated constructor stub
                mContext = context;
            }

            protected void onQueryComplete(int token, Object cookie,
                    Cursor cursor) {
                if (token == CONTACT_QUERY_TOKEN) {
                    // gionee zhouyj 2012-11-12 modify for CR00724301 start 
                    if (cursor != null && cursor.getCount() > 0) {
                        //if (mContactNum > cursor.getCount()) {
                        //    gnContactInfoMap.clear();
                        //}
                        mContactNum = cursor.getCount();
                        cursor.moveToPosition(-1);
                        while (cursor.moveToNext()) {
                            String number = cursor.getString(PHONE_NUMBER_COLUMN);
                            // gionee zhouyj 2013-01-25 add for CR00768084 start 
                            if (number == null) number = "";
                            // gionee zhouyj 2013-01-25 add for CR00768084 end 
                            String name = cursor.getString(CONTACT_NAME_COLUMN);
                            Contact entry = new Contact(number, name);
                            fillPhoneTypeContact(entry, cursor);
                            //gionee gaoj 2012-10-19 added for CR00716269 start
                            number = number.replaceAll(" ", "");
                            number = number.replaceAll("-", "");
                            //gionee gaoj 2012-10-19 added for CR00716269 end
                            //gionee gaoj 2012-10-23 modified for CR00715414 start
                            //Gionee:lixiaohu 2012.10.25 added for CR00721677 begin
                            if (number.length() > MATCH_NUM_LEN && !Mms.isEmailAddress(number) && !number.startsWith("12520")) {
                                //gionee gaoj 2012-10-23 modified for CR00715414 end
                                number = number.substring(number.length() - MATCH_NUM_LEN, number.length());
                            }
                            //Gionee:lixiaohu 2012.10.25 added for CR00721677 end
                            //if (gnContactInfoMap.get(number) == null) {
                            //    gnContactInfoMap.put(number, entry);
                            //} else {
                            //    gnupdateContact(number, gnContactInfoMap.get(number), entry);
                            //}
                        }
                    } //else if (cursor != null && cursor.getCount() == 0){ // remove all contacts, need clear the contacts map
                        //gnContactInfoMap.clear();
                    //}
                    // gionee zhouyj 2012-11-12 modify for CR00724301 end 
                    isContactMapInited = true;
                    // TODO: handle exception
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
        }
        private void gnupdateContact(String num, Contact c, Contact entry) {
            if (contactChanged(c, entry)) {
                // gnContactInfoMap.put(num, entry);

                c.mNumber = entry.mNumber;
                c.mLabel = entry.mLabel;
                c.mPersonId = entry.mPersonId;
                //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
                c.mIndicatePhoneOrSim = entry.mIndicatePhoneOrSim;
                //Gionee <guoyx> <2013-05-30> add for CR00820739 end
                c.mPresenceResId = entry.mPresenceResId;
                c.mPresenceText = entry.mPresenceText;
                c.mAvatarData = entry.mAvatarData;
                c.mAvatar = entry.mAvatar;
                c.mContactMethodId = entry.mContactMethodId;
                c.mContactMethodType = entry.mContactMethodType;
                c.mNumberE164 = entry.mNumberE164;
                c.mName = entry.mName;
                c.mSendToVoicemail = entry.mSendToVoicemail;
                c.mIsValid = entry.mIsValid;

                c.notSynchronizedUpdateNameAndNumber();

                //gionee gaoj 2013-2-19 adde for CR00771935 start
                c.mDrawable = entry.mDrawable;
                c.mIsHotline = entry.mIsHotline;
                //gionee gaoj 2013-2-19 adde for CR00771935 end

                // We saw a bug where we were updating an empty contact. That would trigger
                // l.onUpdate() below, which would call ComposeMessageActivity.onUpdate,
                // which would call the adapter's notifyDataSetChanged, which would throw
                // away the message items and rebuild, eventually calling updateContact()
                // again -- all in a vicious and unending loop. Break the cycle and don't
                // notify if the number (the most important piece of information) is empty.
                if (!TextUtils.isEmpty(c.mNumber)) {
                    // clone the list of listeners in case the onUpdate call turns around and
                    // modifies the list of listeners
                    // access to mListeners is synchronized on ContactsCache
                    HashSet<UpdateListener> iterator;
                    synchronized (mListeners) {
                        iterator = (HashSet<UpdateListener>)Contact.mListeners.clone();
                    }
                    for (UpdateListener l : iterator) {
                        if (V) Log.d(TAG, "updating " + l);
                        l.onUpdate(c);
                    }
                }
            }
        }
        //gionee gaoj 2012-9-20 added for CR00699291 CR00705234 end

        void dump() {
            synchronized (ContactsCache.this) {
                Log.d(TAG, "**** Contact cache dump ****");
                for (String key : mContactsHash.keySet()) {
                    ArrayList<Contact> alc = mContactsHash.get(key);
                    for (Contact c : alc) {
                        Log.d(TAG, key + " ==> " + c.toString());
                    }
                }
            }
        }

        private static class TaskStack {
            Thread mWorkerThread;
            private final ArrayList<Runnable> mThingsToLoad;

            public TaskStack() {
                mThingsToLoad = new ArrayList<Runnable>();
                mWorkerThread = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            Runnable r = null;
                            synchronized (mThingsToLoad) {
                                if (mThingsToLoad.size() == 0) {
                                    try {
                                        mThingsToLoad.wait();
                                    } catch (InterruptedException ex) {
                                        // nothing to do
                                    }
                                }
                                if (mThingsToLoad.size() > 0) {
                                    r = mThingsToLoad.remove(0);
                                }
                            }
                            if (r != null) {
                                r.run();
                            }
                        }
                    }
                });
                mWorkerThread.start();
            }

            public void push(Runnable r) {
                synchronized (mThingsToLoad) {
                    mThingsToLoad.add(r);
                    mThingsToLoad.notify();
                }
            }
        }

        public void pushTask(Runnable r) {
            if (selectTask) {
                mTaskQueue.push(r);
            } else {
                mTaskQueue2.push(r);
            }
            selectTask = !selectTask;
        }

        public Contact getMe(boolean canBlock) {
            return get(SELF_ITEM_KEY, true, canBlock);
        }

        public Contact get(String number, boolean canBlock) {
            return get(number, false, canBlock);
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        public Contact get(String number, boolean canBlock, long privacy) {
            return get(number, false, canBlock, privacy);
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end

        private int waitTime = 55;
        private int minWaitTime = 55;
        private int maxWaitTime = 200;
        private Contact get(String number, boolean isMe, boolean canBlock) {

            /// M:
            final Object obj = new Object();

            if (TextUtils.isEmpty(number)) {
                number = "";        // In some places (such as Korea), it's possible to receive
                                    // a message without the sender's address. In this case,
                                    // all such anonymous messages will get added to the same
                                    // thread.
            }

            // Always return a Contact object, if if we don't have an actual contact
            // in the contacts db.
            Contact contact = internalGet(number, isMe);
            Runnable r = null;

            synchronized (contact) {
                // If there's a query pending and we're willing to block then
                // wait here until the query completes.

// a0
                // make sure the block can update contact immediately
                if (canBlock) {
                    contact.mIsStale = true;
                }
// a1

                // If we're stale and we haven't already kicked off a query then kick
                // it off here.
                if (contact.mIsStale) {
                    contact.mIsStale = false;

                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("async update for " + contact.toString() + " canBlock: " + canBlock +
                                " isStale: " + contact.mIsStale);
                    }

                    final Contact c = contact;
                    r = new Runnable() {
                        public void run() {
                            updateContact(c);
                            synchronized (obj) {
                                obj.notifyAll();
                            }
                            c.mQueryPending = false;
                        }
                    };
                }
            }
            // do this outside of the synchronized so we don't hold up any
            // subsequent calls to "get" on other threads
            if (r != null) {
                if (canBlock) {
                    pushTask(r);
                    synchronized (obj) {
                        try {
                            obj.wait(waitTime);
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                    }
                    if (waitTime < maxWaitTime) {
                        waitTime += 5;
                    }
                } else {
                    pushTask(r);
                }
            } else {
                if ((waitTime -= minWaitTime) < minWaitTime) {
                    waitTime = minWaitTime;
                }
            }
            return contact;
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        private Contact get(String number, boolean isMe, boolean canBlock, final long privacy) {

            /// M:
            final Object obj = new Object();

            if (TextUtils.isEmpty(number)) {
                number = "";        // In some places (such as Korea), it's possible to receive
                                    // a message without the sender's address. In this case,
                                    // all such anonymous messages will get added to the same
                                    // thread.
            }

            // Always return a Contact object, if if we don't have an actual contact
            // in the contacts db.
            Contact contact = internalGet(number, isMe, privacy);
            Runnable r = null;

            synchronized (contact) {
                // If there's a query pending and we're willing to block then
                // wait here until the query completes.

// a0
                // make sure the block can update contact immediately
                if (canBlock) {
                    contact.mIsStale = true;
                }
// a1

                // If we're stale and we haven't already kicked off a query then kick
                // it off here.
                if (contact.mIsStale) {
                    contact.mIsStale = false;

                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("async update for " + contact.toString() + " canBlock: " + canBlock +
                                " isStale: " + contact.mIsStale);
                    }

                    final Contact c = contact;
                    r = new Runnable() {
                        public void run() {
                            updateContact(c, privacy);
                            synchronized (obj) {
                                obj.notifyAll();
                            }
                            c.mQueryPending = false;
                        }
                    };
                }
            }
            // do this outside of the synchronized so we don't hold up any
            // subsequent calls to "get" on other threads
            if (r != null) {
                if (canBlock) {
                    pushTask(r);
                    synchronized (obj) {
                        try {
                            obj.wait(waitTime);
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                    }
                    if (waitTime < maxWaitTime) {
                        waitTime += 5;
                    }
                } else {
                    pushTask(r);
                }
            } else {
                if ((waitTime -= minWaitTime) < minWaitTime) {
                    waitTime = minWaitTime;
                }
            }
            return contact;
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end
        /**
         * Get CacheEntry list for given phone URIs. This method will do single one query to
         * get expected contacts from provider. Be sure passed in URIs are not null and contains
         * only valid URIs.
         */
        public List<Contact> getContactInfoForPhoneUris(Parcelable[] uris) {
            if (uris.length == 0) {
                return null;
            }
            StringBuilder idSetBuilder = new StringBuilder();
            boolean first = true;
            for (Parcelable p : uris) {
                Uri uri = (Uri) p;
                if ("content".equals(uri.getScheme())) {
                    if (first) {
                        first = false;
                        idSetBuilder.append(uri.getLastPathSegment());
                    } else {
                        idSetBuilder.append(',').append(uri.getLastPathSegment());
                    }
                }
            }
            // Check whether there is content URI.
            if (first) return null;
            Cursor cursor = null;
            if (idSetBuilder.length() > 0) {
                final String whereClause = Phone._ID + " IN (" + idSetBuilder.toString() + ")";
                cursor = mContext.getContentResolver().query(
                        PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, whereClause, null, null);
            }

            if (cursor == null) {
                return null;
            }

            List<Contact> entries = new ArrayList<Contact>();

            try {
                while (cursor.moveToNext()) {
                    Contact entry = new Contact(cursor.getString(PHONE_NUMBER_COLUMN),
                            cursor.getString(CONTACT_NAME_COLUMN));
                    fillPhoneTypeContact(entry, cursor);
                    ArrayList<Contact> value = new ArrayList<Contact>();
                    value.add(entry);
                    // Put the result in the cache.
                    mContactsHash.put(key(entry.mNumber, sStaticKeyBuffer), value);
                    entries.add(entry);
                }
            } finally {
                cursor.close();
            }
            return entries;
        }
        
        //a0
        public List<Contact> getContactInfoForPhoneIds(long[] ids) {
            if (ids.length == 0) {
                return null;
            }
            StringBuilder idSetBuilder = new StringBuilder();
            boolean first = true;
            for (long id : ids) {
                if (first) {
                    first = false;
                    idSetBuilder.append(id);
                } else {
                    idSetBuilder.append(',').append(id);
                }
            }
            
            Cursor cursor = null;
            if (idSetBuilder.length() > 0) {
                final String whereClause = Phone._ID + " IN (" + idSetBuilder.toString() + ")";
                cursor = mContext.getContentResolver().query(
                        PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, whereClause, null, null);
            }

            if (cursor == null) {
                return null;
            }

            List<Contact> entries = new ArrayList<Contact>();

            try {
                while (cursor.moveToNext()) {
                    Contact entry = new Contact(cursor.getString(PHONE_NUMBER_COLUMN),
                            cursor.getString(CONTACT_NAME_COLUMN));
                    fillPhoneTypeContact(entry, cursor);
                    ArrayList<Contact> value = new ArrayList<Contact>();
                    value.add(entry);
                    // Put the result in the cache.
                    mContactsHash.put(key(entry.mNumber, sStaticKeyBuffer), value);
                    entries.add(entry);
                }
            } finally {
                cursor.close();
            }
            return entries;
        }
        //a1

        private boolean contactChanged(Contact orig, Contact newContactData) {
            // The phone number should never change, so don't bother checking.
            // TODO: Maybe update it if it has gotten longer, i.e. 650-234-5678 -> +16502345678?

            // Do the quick check first.
            if (orig.mContactMethodType != newContactData.mContactMethodType) {
                return true;
            }

            if (orig.mContactMethodId != newContactData.mContactMethodId) {
                return true;
            }

            if (orig.mPersonId != newContactData.mPersonId) {
                if (V) Log.d(TAG, "person id changed");
                return true;
            }

            if (orig.mPresenceResId != newContactData.mPresenceResId) {
                if (V) Log.d(TAG, "presence changed");
                return true;
            }

            if (orig.mSendToVoicemail != newContactData.mSendToVoicemail) {
                return true;
            }

            String oldName = emptyIfNull(orig.mName);
            String newName = emptyIfNull(newContactData.mName);
            if (!oldName.equals(newName)) {
                if (V) Log.d(TAG, String.format("name changed: %s -> %s", oldName, newName));
                return true;
            }

            String oldLabel = emptyIfNull(orig.mLabel);
            String newLabel = emptyIfNull(newContactData.mLabel);
            if (!oldLabel.equals(newLabel)) {
                if (V) Log.d(TAG, String.format("label changed: %s -> %s", oldLabel, newLabel));
                return true;
            }

            if (!Arrays.equals(orig.mAvatarData, newContactData.mAvatarData)) {
                if (V) Log.d(TAG, "avatar changed");
                return true;
            }

            return false;
        }

        private void updateContact(final Contact c) {
            if (c == null) {
                return;
            }

            Contact entry = getContactInfo(c);
            synchronized (c) {
                if (contactChanged(c, entry)) {
                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("updateContact: contact changed for " + entry.mName);
                    }

                    c.mNumber = entry.mNumber;
                    c.mLabel = entry.mLabel;
                    c.mPersonId = entry.mPersonId;
                    c.mPresenceResId = entry.mPresenceResId;
                    c.mPresenceText = entry.mPresenceText;
                    c.mAvatarData = entry.mAvatarData;
                    c.mAvatar = entry.mAvatar;
                    c.mContactMethodId = entry.mContactMethodId;
                    c.mContactMethodType = entry.mContactMethodType;
                    c.mNumberE164 = entry.mNumberE164;
                    c.mName = entry.mName;
                    c.mSendToVoicemail = entry.mSendToVoicemail;
                    c.mIsValid = entry.mIsValid;
                    //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
                    c.mIndicatePhoneOrSim = entry.mIndicatePhoneOrSim;
                    //Gionee <guoyx> <2013-05-30> add for CR00820739 end
                    c.notSynchronizedUpdateNameAndNumber();

                    //gionee gaoj 2013-2-19 adde for CR00771935 start
                    c.mDrawable = entry.mDrawable;
                    c.mIsHotline = entry.mIsHotline;
                    //gionee gaoj 2013-2-19 adde for CR00771935 end
                    // Aurora xuyong 2014-10-23 added for privacy feature start
                    c.mPrivacy = entry.getPrivacy();
                    // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature start
                    c.mPhotoId = entry.mPhotoId;
                    // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature end
                    // Aurora xuyong 2014-10-23 added for privacy feature end
                    // We saw a bug where we were updating an empty contact. That would trigger
                    // l.onUpdate() below, which would call ComposeMessageActivity.onUpdate,
                    // which would call the adapter's notifyDataSetChanged, which would throw
                    // away the message items and rebuild, eventually calling updateContact()
                    // again -- all in a vicious and unending loop. Break the cycle and don't
                    // notify if the number (the most important piece of information) is empty.
                    if (!TextUtils.isEmpty(c.mNumber)) {
                        // clone the list of listeners in case the onUpdate call turns around and
                        // modifies the list of listeners
                        // access to mListeners is synchronized on ContactsCache
                        HashSet<UpdateListener> iterator;
                        synchronized (mListeners) {
                            iterator = (HashSet<UpdateListener>)Contact.mListeners.clone();
                        }
                        for (UpdateListener l : iterator) {
                            if (V) Log.d(TAG, "updating " + l);
                            l.onUpdate(c);
                        }
                    }
                }
            }
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        private void updateContact(final Contact c, long privacy) {
            if (c == null) {
                return;
            }

            Contact entry = getContactInfo(c, privacy);
            synchronized (c) {
                if (contactChanged(c, entry)) {
                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("updateContact: contact changed for " + entry.mName);
                    }

                    c.mNumber = entry.mNumber;
                    c.mLabel = entry.mLabel;
                    c.mPersonId = entry.mPersonId;
                    c.mPresenceResId = entry.mPresenceResId;
                    c.mPresenceText = entry.mPresenceText;
                    c.mAvatarData = entry.mAvatarData;
                    c.mAvatar = entry.mAvatar;
                    c.mContactMethodId = entry.mContactMethodId;
                    c.mContactMethodType = entry.mContactMethodType;
                    c.mNumberE164 = entry.mNumberE164;
                    c.mName = entry.mName;
                    c.mSendToVoicemail = entry.mSendToVoicemail;
                    c.mIsValid = entry.mIsValid;
                    //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
                    c.mIndicatePhoneOrSim = entry.mIndicatePhoneOrSim;
                    //Gionee <guoyx> <2013-05-30> add for CR00820739 end
                    c.notSynchronizedUpdateNameAndNumber();

                    //gionee gaoj 2013-2-19 adde for CR00771935 start
                    c.mDrawable = entry.mDrawable;
                    c.mIsHotline = entry.mIsHotline;
                    //gionee gaoj 2013-2-19 adde for CR00771935 end
                    
                    c.mPrivacy = entry.getPrivacy();
                    // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature start
                    c.mPhotoId = entry.mPhotoId;
                    // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature end
                    // We saw a bug where we were updating an empty contact. That would trigger
                    // l.onUpdate() below, which would call ComposeMessageActivity.onUpdate,
                    // which would call the adapter's notifyDataSetChanged, which would throw
                    // away the message items and rebuild, eventually calling updateContact()
                    // again -- all in a vicious and unending loop. Break the cycle and don't
                    // notify if the number (the most important piece of information) is empty.
                    if (!TextUtils.isEmpty(c.mNumber)) {
                        // clone the list of listeners in case the onUpdate call turns around and
                        // modifies the list of listeners
                        // access to mListeners is synchronized on ContactsCache
                        HashSet<UpdateListener> iterator;
                        synchronized (mListeners) {
                            iterator = (HashSet<UpdateListener>)Contact.mListeners.clone();
                        }
                        for (UpdateListener l : iterator) {
                            if (V) Log.d(TAG, "updating " + l);
                            l.onUpdate(c);
                        }
                    }
                }
            }
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end
        /**
         * Returns the caller info in Contact.
         */
        private Contact getContactInfo(Contact c) {
            Contact contact = null;
            if (c.mIsMe) {
                contact = getContactInfoForSelf();
            } else if (Mms.isEmailAddress(c.mNumber)) {
                contact = getContactInfoForEmailAddress(c.mNumber);
            } else if (isAlphaNumber(c.mNumber)) {
                contact = getContactInfoForEmailAddress(c.mNumber);
                // Some shortcodes are saved as Phone number. So we should find it as phone number,
                // if it can't be found as Email address.
                if (contact.mPersonId < 1) {
                    String number = Contact.getValidNumber(c.mNumber);
                    if (PhoneNumberUtils.isWellFormedSmsAddress(number)) {
                        // make only valid number can query Contact's database.
                        contact = getContactInfoForPhoneNumber(number);
                    } else {
                        contact = c;
                    }
                }
            } else {
                contact = getContactInfoForPhoneNumber(c.mNumber);
            }
            //gionee gaoj 2012-9-20 added for CR00699291 start
            if (MmsApp.mGnMessageSupport) {
                String num = c.mNumber;
                //gionee gaoj 2012-10-19 added for CR00716269 start
                num = num.replaceAll(" ", "");
                num = num.replaceAll("-", "");
                //gionee gaoj 2012-10-19 added for CR00716269 end
                //Gionee:lixiaohu 2012.10.25 added for CR00721677 begin
                // gionee zhouyj 2012-12-25 modify for CR00735650 start 
                if (!Mms.isEmailAddress(num) && num.length() > MATCH_NUM_LEN && !num.startsWith("12520")) {
                    num = num.substring(num.length() - MATCH_NUM_LEN, num.length());
                }
                // gionee zhouyj 2012-12-25 modify for CR00735650 end 
                //Gionee:lixiaohu 2012.10.25 added for CR00721677 begin
                // gnContactInfoMap.put(num, contact);
            }
            //gionee gaoj 2012-9-20 added for CR00699291 end
            return contact;
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        private Contact getContactInfo(Contact c, long privacy) {
            Contact contact = null;
            if (c.mIsMe) {
                contact = getContactInfoForSelf();
            } else if (Mms.isEmailAddress(c.mNumber)) {
                contact = getContactInfoForEmailAddress(c.mNumber);
            } else if (isAlphaNumber(c.mNumber)) {
                contact = getContactInfoForEmailAddress(c.mNumber);
                // Some shortcodes are saved as Phone number. So we should find it as phone number,
                // if it can't be found as Email address.
                if (contact.mPersonId < 1) {
                    String number = Contact.getValidNumber(c.mNumber);
                    if (PhoneNumberUtils.isWellFormedSmsAddress(number)) {
                        // make only valid number can query Contact's database.
                        contact = getContactInfoForPhoneNumber(number);
                    } else {
                        contact = c;
                    }
                }
            } else {
                contact = getContactInfoForPhoneNumber(c.mNumber, privacy);
            }
            //gionee gaoj 2012-9-20 added for CR00699291 start
            if (MmsApp.mGnMessageSupport) {
                String num = c.mNumber;
                //gionee gaoj 2012-10-19 added for CR00716269 start
                num = num.replaceAll(" ", "");
                num = num.replaceAll("-", "");
                //gionee gaoj 2012-10-19 added for CR00716269 end
                //Gionee:lixiaohu 2012.10.25 added for CR00721677 begin
                // gionee zhouyj 2012-12-25 modify for CR00735650 start 
                if (!Mms.isEmailAddress(num) && num.length() > MATCH_NUM_LEN && !num.startsWith("12520")) {
                    num = num.substring(num.length() - MATCH_NUM_LEN, num.length());
                }
                // gionee zhouyj 2012-12-25 modify for CR00735650 end 
                //Gionee:lixiaohu 2012.10.25 added for CR00721677 begin
                // gnContactInfoMap.put(num, contact);
            }
            //gionee gaoj 2012-9-20 added for CR00699291 end
            return contact;
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end
        // Some received sms's have addresses such as "OakfieldCPS" or "T-Mobile". This
        // function will attempt to identify these and return true. If the number contains
        // 3 or more digits, such as "jello123", this function will return false.
        // Some countries have 3 digits shortcodes and we have to identify them as numbers.
        //    http://en.wikipedia.org/wiki/Short_code
        // Examples of input/output for this function:
        //    "Jello123" -> false  [3 digits, it is considered to be the phone number "123"]
        //    "T-Mobile" -> true   [it is considered to be the address "T-Mobile"]
        //    "Mobile1"  -> true   [1 digit, it is considered to be the address "Mobile1"]
        //    "Dogs77"   -> true   [2 digits, it is considered to be the address "Dogs77"]
        //    "****1"    -> true   [1 digits, it is considered to be the address "****1"]
        //    "#4#5#6#"  -> true   [it is considered to be the address "#4#5#6#"]
        //    "AB12"     -> true   [2 digits, it is considered to be the address "AB12"]
        //    "12"       -> true   [2 digits, it is considered to be the address "12"]
        private boolean isAlphaNumber(String number) {
            // TODO: PhoneNumberUtils.isWellFormedSmsAddress() only check if the number is a valid
            // GSM SMS address. If the address contains a dialable char, it considers it a well
            // formed SMS addr. CDMA doesn't work that way and has a different parser for SMS
            // address (see CdmaSmsAddress.parse(String address)). We should definitely fix this!!!
            if (!PhoneNumberUtils.isWellFormedSmsAddress(number)) {
                // The example "T-Mobile" will exit here because there are no numbers.
                return true;        // we're not an sms address, consider it an alpha number
            }
            if (MessageUtils.isAlias(number)) {
                return true;
            }
            number = PhoneNumberUtils.extractNetworkPortion(number);
            if (TextUtils.isEmpty(number)) {
                return true;    // there are no digits whatsoever in the number
            }
            // At this point, anything like "Mobile1" or "Dogs77" will be stripped down to
            // "1" and "77". "#4#5#6#" remains as "#4#5#6#" at this point.
            return number.length() < 3;
        }

        /**
         * Queries the caller id info with the phone number.
         * @return a Contact containing the caller id info corresponding to the number.
         */
        private Contact getContactInfoForPhoneNumber(String number) {
            boolean isValidNumber = PhoneNumberUtils.isWellFormedSmsAddress(number);

            if (isValidNumber) {
                    // Aurora liugj 2013-10-30 modified for bug-188 start 
                //number = PhoneNumberUtils.stripSeparators(number);    //该方法会去除电话号码里的一些字符
                    // Aurora liugj 2013-10-30 modified for bug-188 end
            }
            Contact entry = new Contact(number);
            entry.mContactMethodType = CONTACT_METHOD_TYPE_PHONE;

            //if (LOCAL_DEBUG) log("queryContactInfoByNumber: number=" + number);

            String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
            String matchNumber = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);

            if (!TextUtils.isEmpty(normalizedNumber) && !TextUtils.isEmpty(matchNumber)) {
                String numberLen = String.valueOf(normalizedNumber.length());
                String[] args = new String[] {normalizedNumber, numberLen, normalizedNumber, numberLen};

                // gionee lwzh add for CR00774362 20130227 begin
                Cursor cursor = null;
                // Aurora xuyong 2014-11-14 modified for bug #9809 start
                try {
                // Aurora xuyong 2014-11-14 modified for bug #9809 end
                    cursor = mContext.getContentResolver().query(PHONES_WITH_PRESENCE_URI,
                            CALLER_ID_PROJECTION, CALLER_ID_SELECTION, new String[] {
                                matchNumber
                            }, null);

                    if (cursor == null) {
                        return entry;
                    }

                    if (cursor != null && cursor.getCount() > 1) {
                        try {
                            cursor.moveToPosition(-1);
                            while (cursor.moveToNext()) {
                                String numberE164 = cursor.getString(PHONE_NORMALIZED_NUMBER);

                                if (normalizedNumber.equals(numberE164)) {
                                    fillPhoneTypeContact(entry, cursor);
                                    // Aurora xuyong 2016-01-09 added for bug #18295 start
                                    if (cursor != null && !cursor.isClosed()) {
                                        cursor.close();
                                    }
                                    // Aurora xuyong 2016-01-09 added for bug #18295 end
                                    return entry;
                                }
                            }
                        } catch (Exception ex) {
                            // nothing to do
                        }
                    }
                // Aurora xuyong 2014-11-14 deleted for bug #9809 start
                //} else {
                //    cursor = mContext.getContentResolver().query(
                //            PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, CALLER_ID_SELECTION_EXACT_MATCH, args, null);
                //    if (cursor == null || (cursor != null && cursor.getCount() == 0)) {
                //        if (cursor != null) {
                //            cursor.close();
                //        }
                //        cursor = mContext.getContentResolver().query(PHONES_WITH_PRESENCE_URI,
                //                CALLER_ID_PROJECTION, CALLER_ID_SELECTION, new String[] {matchNumber}, null);
                //    }
                //}
                //// gionee lwzh add for CR00774362 20130227 end
                //
                //try {
                // Aurora xuyong 2014-11-14 deleted for bug #9809 end
                    if (cursor.moveToFirst()) {
                        fillPhoneTypeContact(entry, cursor);
                        //gionee gaoj 2012-12-11 added for CR00742048 start
                    } else {
                        if (MmsApp.sIsHotLinesSupport) {
                            addHotLine(entry);
                        }
                    }
                    //gionee gaoj 2012-12-11 added for CR00742048 end
                // Aurora xuyong 2014-11-14 modified for bug #9809 start
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
                // Aurora xuyong 2014-11-14 modified for bug #9809 end
            }
            return entry;
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        private Contact getContactInfoForPhoneNumber(String number, long privacy) {
            boolean isValidNumber = PhoneNumberUtils.isWellFormedSmsAddress(number);
            if (isValidNumber) {
                    // Aurora liugj 2013-10-30 modified for bug-188 start 
                //number = PhoneNumberUtils.stripSeparators(number);    //该方法会去除电话号码里的一些字符
                    // Aurora liugj 2013-10-30 modified for bug-188 end
            }
            Contact entry = new Contact(number, privacy);
            entry.mContactMethodType = CONTACT_METHOD_TYPE_PHONE;

            //if (LOCAL_DEBUG) log("queryContactInfoByNumber: number=" + number);

            String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
            String matchNumber = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);

            if (!TextUtils.isEmpty(normalizedNumber) && !TextUtils.isEmpty(matchNumber)) {
                String numberLen = String.valueOf(normalizedNumber.length());
                String[] args = new String[] {normalizedNumber, numberLen, normalizedNumber, numberLen};

                // gionee lwzh add for CR00774362 20130227 begin
                Cursor cursor = null;
                // Aurora xuyong 2014-11-14 modified for bug #9809 start
                try {
                // Aurora xuyong 2014-11-14 modified for bug #9809 end
                    cursor = mContext.getContentResolver().query(PHONES_WITH_PRESENCE_URI,
                            CALLER_ID_PROJECTION, CALLER_ID_SELECTION + "AND is_privacy = " + privacy, new String[] {
                                matchNumber
                            }, null);

                    if (cursor == null) {
                        return entry;
                    }

                    if (cursor != null && cursor.getCount() > 1) {
                        try {
                            cursor.moveToPosition(-1);
                            while (cursor.moveToNext()) {
                                String numberE164 = cursor.getString(PHONE_NORMALIZED_NUMBER);

                                if (normalizedNumber.equals(numberE164)) {
                                    fillPhoneTypeContact(entry, cursor);
                                    // Aurora xuyong 2016-01-09 added for bug #18295 start
                                    if (cursor != null && !cursor.isClosed()) {
                                        cursor.close();
                                    }
                                    // Aurora xuyong 2016-01-09 added for bug #18295 end
                                    return entry;
                                }
                            }
                        } catch (Exception ex) {
                            // nothing to do
                        }
                    }
                // Aurora xuyong 2014-11-14 deleted for bug #9809 start
                //} else {
                //    cursor = mContext.getContentResolver().query(
                //            PHONES_WITH_PRESENCE_URI, CALLER_ID_PROJECTION, CALLER_ID_SELECTION_EXACT_MATCH, args, null);
                //    if (cursor == null || (cursor != null && cursor.getCount() == 0)) {
                //        if (cursor != null) {
                //            cursor.close();
                //        }
                //        cursor = mContext.getContentResolver().query(PHONES_WITH_PRESENCE_URI,
                //                CALLER_ID_PROJECTION, CALLER_ID_SELECTION, new String[] {matchNumber}, null);
                //    }
                //}
                // gionee lwzh add for CR00774362 20130227 end
                //
                //try {
                // Aurora xuyong 2014-11-14 deleted for bug #9809 end
                    if (cursor.moveToFirst()) {
                        fillPhoneTypeContact(entry, cursor);
                        //gionee gaoj 2012-12-11 added for CR00742048 start
                    } else {
                        if (MmsApp.sIsHotLinesSupport) {
                            addHotLine(entry);
                        }
                    }
                    //gionee gaoj 2012-12-11 added for CR00742048 end
                // Aurora xuyong 2014-11-14 modified for bug #9809 start
                } catch (SQLiteException e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
                // Aurora xuyong 2014-11-14 modified for bug #9809 end
            }
            return entry;
        }
        // Aurora xuyong 2014-10-23 added for privacy feature en
        //gionee gaoj 2012-12-11 added for CR00742048 start
        private void addHotLine(Contact c) {
            if (null != c && null != c.mNumber) {
                    // Aurora liugj 2013-11-07 modified for bug-456 start
                String [] info = GnHotLinesUtil.getInfo(mContext, c.mNumber.replaceAll("'", "").replaceAll(" ", ""));
                    // Aurora liugj 2013-11-07 modified for bug-456 end
                if (null != info) {
                    c.mIsHotline = true;
                    c.mName = info[0];
                    String imageStr = info[1];
                    Uri imageUri = Uri.withAppendedPath(GnHotLinesUtil.HOT_LINES_DISPLAY_PHOTO, imageStr);
                    Drawable d = null;
                    InputStream ips = null;
                    try {
                        ips = mContext.getContentResolver().openInputStream(imageUri);
                        d = Drawable.createFromStream(ips, "src");
                        ips.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    c.mDrawable = d;
                } else {
                    c.mIsHotline = true;
                }
            }
        }
        //gionee gaoj 2012-12-11 added for CR00742048 end

        /**
         * @return a Contact containing the info for the profile.
         */
        private Contact getContactInfoForSelf() {
            Contact entry = new Contact(true);
            entry.mContactMethodType = CONTACT_METHOD_TYPE_SELF;

            //if (LOCAL_DEBUG) log("getContactInfoForSelf: number=" + number);
            Cursor cursor = mContext.getContentResolver().query(
                    Profile.CONTENT_URI, SELF_PROJECTION, null, null, null);
            if (cursor == null) {
                return entry;
            }

            try {
                if (cursor.moveToFirst()) {
                    fillSelfContact(entry, cursor);
                }
            } finally {
                cursor.close();
            }
            return entry;
        }
        // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature start
        // Aurora xuyong 2016-01-25 modified for aurora 2.0 new feature start
        private BitmapDrawable getBdByIndex(long contactId) {
            final int index = (int)contactId;
            int[] randomPhotoIds = new int[]{
        // Aurora xuyong 2016-01-25 modified for aurora 2.0 new feature end
                    R.drawable.small_contact_photo_dog,
                    R.drawable.small_contact_photo_bear,
                    R.drawable.small_contact_photo_bird,
                    R.drawable.small_contact_photo_cat,
                    R.drawable.small_contact_photo_cattle,//4
                    R.drawable.small_contact_photo_crocodile,
                    R.drawable.small_contact_photo_elephant,
                    R.drawable.small_contact_photo_fox,
                    R.drawable.small_contact_photo_jellyfish,
                    R.drawable.small_contact_photo_parrot,
                    R.drawable.small_contact_photo_rhinoceros,
                    R.drawable.small_contact_photo_sheep,//11
                    R.drawable.small_contact_photo_swan,
            };
            // Aurora xuyong 2016-01-25 modified for aurora 2.0 new feature start
            return (BitmapDrawable)mContext.getResources().getDrawable(randomPhotoIds[index % randomPhotoIds.length]);
            // Aurora xuyong 2016-01-25 modified for aurora 2.0 new feature end
        }
        // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature end
        private void fillPhoneTypeContact(final Contact contact, final Cursor cursor) {
            synchronized (contact) {
                contact.mContactMethodType = CONTACT_METHOD_TYPE_PHONE;
                contact.mContactMethodId = cursor.getLong(PHONE_ID_COLUMN);
                contact.mLabel = cursor.getString(PHONE_LABEL_COLUMN);
                contact.mName = cursor.getString(CONTACT_NAME_COLUMN);
                contact.mPersonId = cursor.getLong(CONTACT_ID_COLUMN);
                contact.mPresenceResId = getPresenceIconResourceId(
                        cursor.getInt(CONTACT_PRESENCE_COLUMN));
                contact.mPresenceText = cursor.getString(CONTACT_STATUS_COLUMN);
                contact.mNumberE164 = cursor.getString(PHONE_NORMALIZED_NUMBER);
                //Gionee <guoyx> <2013-05-30> add for CR00820739 begin
                contact.mIndicatePhoneOrSim = cursor.getInt(INDICATE_PHONE_SIM);
                //Gionee <guoyx> <2013-05-30> add for CR00820739 end
                contact.mSendToVoicemail = cursor.getInt(SEND_TO_VOICEMAIL) == 1;
                contact.mIsValid = true;
                // Aurora xuyong 2014-10-23 added for privacy feature start
                contact.mPrivacy = cursor.getLong(IS_PRIVACY);
                // Aurora xuyong 2014-10-23 added for privacy feature end
                // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature start
                contact.mPhotoId = cursor.getInt(PHOTO_ID);
                // Aurora xuyong 2016-01-23 added for aurora 2.0 new feature end
                if (V) {
                    log("fillPhoneTypeContact: name=" + contact.mName + ", number="
                            + contact.mNumber + ", presence=" + contact.mPresenceResId
                            + " SendToVoicemail: " + contact.mSendToVoicemail);
                }
            }
            // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature start
            byte[] data = null;
            // Aurora xuyong 2016-01-25 modified for aurora 2.0 new feature start
            data = loadAvatarData(contact);
            if (data == null) {
                contact.mAvatar = getBdByIndex(contact.mPersonId);
            // Aurora xuyong 2016-01-25 modified for aurora 2.0 new feature end
            }
            // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature end
            synchronized (contact) {
                contact.mAvatarData = data;
            }
        }

        private void fillSelfContact(final Contact contact, final Cursor cursor) {
            synchronized (contact) {
                contact.mName = cursor.getString(SELF_NAME_COLUMN);
                if (TextUtils.isEmpty(contact.mName)) {
                    contact.mName = mContext.getString(R.string.messagelist_sender_self);
                }
                contact.mIsValid = true;
                if (V) {
                    log("fillSelfContact: name=" + contact.mName + ", number="
                            + contact.mNumber);
                }
            }
            byte[] data = loadAvatarData(contact);

            synchronized (contact) {
                contact.mAvatarData = data;
            }
        }
        /*
         * Load the avatar data from the cursor into memory.  Don't decode the data
         * until someone calls for it (see getAvatar).  Hang onto the raw data so that
         * we can compare it when the data is reloaded.
         * TODO: consider comparing a checksum so that we don't have to hang onto
         * the raw bytes after the image is decoded.
         */
        private byte[] loadAvatarData(Contact entry) {
            byte [] data = null;

            if ((!entry.mIsMe && entry.mPersonId == 0) || entry.mAvatar != null) {
                Log.d(M_TAG, "loadAvatarData(): return null");
                return null;
            }

            if (V) {
                log("loadAvatarData: name=" + entry.mName + ", number=" + entry.mNumber);
            }

            // If the contact is "me", then use my local profile photo. Otherwise, build a
            // uri to get the avatar of the contact.
            Uri contactUri = entry.mIsMe ?
                    Profile.CONTENT_URI :
                    ContentUris.withAppendedId(Contacts.CONTENT_URI, entry.mPersonId);
              // Aurora liugj 2013-11-27 modified for bug-1001 start
            InputStream avatarDataStream = null;
            try {
                avatarDataStream = Contacts.openContactPhotoInputStream(
                        mContext.getContentResolver(),
                        contactUri);
                if (avatarDataStream != null) {
                    data = new byte[avatarDataStream.available()];
                    avatarDataStream.read(data, 0, data.length);
                }
            } catch (IOException ex) {
                Log.w(M_TAG, "loadAvatarData(): IOException!");
            }catch (Exception e) {
                e.printStackTrace();
              // Aurora liugj 2013-11-27 modified for bug-1001 end
            } finally {
                try {
                    if (avatarDataStream != null) {
                        avatarDataStream.close();
                    }
                } catch (IOException e) {
                }
            }

            return data;
        }

        private int getPresenceIconResourceId(int presence) {
            // TODO: must fix for SDK
            if (presence != Presence.OFFLINE) {
                return Presence.getPresenceIconResourceId(presence);
            }

            return 0;
        }

        /**
         * Query the contact email table to get the name of an email address.
         */
        private Contact getContactInfoForEmailAddress(String email) {
            Contact entry = new Contact(email);
            entry.mContactMethodType = CONTACT_METHOD_TYPE_EMAIL;

            Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                    EMAIL_WITH_PRESENCE_URI,
                    EMAIL_PROJECTION,
                    EMAIL_SELECTION,
                    new String[] { email },
                    null);

            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        boolean found = false;
                        entry.mContactMethodId = cursor.getLong(EMAIL_ID_COLUMN);
                        entry.mPresenceResId = getPresenceIconResourceId(
                                cursor.getInt(EMAIL_STATUS_COLUMN));
                        entry.mPersonId = cursor.getLong(EMAIL_CONTACT_ID_COLUMN);
                        entry.mSendToVoicemail = cursor.getInt(EMAIL_SEND_TO_VOICEMAIL_COLUMN) == 1;

                        synchronized (entry) {
                            entry.mPresenceResId = getPresenceIconResourceId(
                                    cursor.getInt(EMAIL_STATUS_COLUMN));

                            String name = cursor.getString(EMAIL_NAME_COLUMN);
                            if (TextUtils.isEmpty(name)) {
                                name = cursor.getString(EMAIL_CONTACT_NAME_COLUMN);
                            }
                            if (!TextUtils.isEmpty(name)) {
                                entry.mName = name;
                                if (V) {
                                    log("getContactInfoForEmailAddress: name=" + entry.mName +
                                            ", email=" + email + ", presence=" +
                                            entry.mPresenceResId);
                                }
                                found = true;
                                entry.mIsValid = true;
                            }
                        }
                        if (found) {
                            byte[] data = loadAvatarData(entry);
                            synchronized (entry) {
                                entry.mAvatarData = data;
                            }

                            break;
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
            return entry;
        }

        // Invert and truncate to five characters the phoneNumber so that we
        // can use it as the key in a hashtable.  We keep a mapping of this
        // key to a list of all contacts which have the same key.
        private String key(String phoneNumber, CharBuffer keyBuffer) {
            keyBuffer.clear();
            keyBuffer.mark();
            int position = phoneNumber.length();
            int resultCount = 0;
            while (--position >= 0) {
                keyBuffer.put(phoneNumber.charAt(position));
                if (++resultCount == STATIC_KEY_BUFFER_MAXIMUM_LENGTH) {
                    break;
                }
            }
            keyBuffer.reset();
            if (resultCount > 0) {
                return keyBuffer.toString();
            } else {
                // there were no usable digits in the input phoneNumber
                return phoneNumber;
            }
        }

        // Reuse this so we don't have to allocate each time we go through this
        // "get" function.
// m0
//        static final int STATIC_KEY_BUFFER_MAXIMUM_LENGTH = 5;
        static final int STATIC_KEY_BUFFER_MAXIMUM_LENGTH = 10;
// m1
        static CharBuffer sStaticKeyBuffer = CharBuffer.allocate(STATIC_KEY_BUFFER_MAXIMUM_LENGTH);

        private Contact internalGet(String numberOrEmail, boolean isMe) {
            synchronized (ContactsCache.this) {
                // See if we can find "number" in the hashtable.
                // If so, just return the result.
                String workingNumberOrEmail = numberOrEmail;
                workingNumberOrEmail = workingNumberOrEmail.replaceAll(" ", "").replaceAll("-", "");
                String key = "";

                final boolean isNotRegularPhoneNumber = isMe || Mms.isEmailAddress(numberOrEmail) ||
                        MessageUtils.isAlias(numberOrEmail);
                if (isNotRegularPhoneNumber) {
                    key = numberOrEmail;
                } else if (PhoneNumberUtils.isWellFormedSmsAddress(workingNumberOrEmail)) {
                    if (workingNumberOrEmail.length() > NORMAL_NUMBER_MAX_LENGTH) {
                        // handle number like 1252002613111111111
                        key = workingNumberOrEmail;
                    } else {
                        numberOrEmail = workingNumberOrEmail;
                        key = key(numberOrEmail, sStaticKeyBuffer);
                    }
                } else {
                    workingNumberOrEmail = PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
                    workingNumberOrEmail = PhoneNumberUtils.formatNumber(workingNumberOrEmail);
                    if (numberOrEmail.equals(workingNumberOrEmail)) {
                        numberOrEmail = PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
                        key = key(numberOrEmail, sStaticKeyBuffer);
                    } else {
                        key = numberOrEmail;
                    }
                }

                ArrayList<Contact> candidates = mContactsHash.get(key);
                if (candidates != null) {
                    int length = candidates.size();
                    for (int i = 0; i < length; i++) {
                        Contact c= candidates.get(i);
                        if (isNotRegularPhoneNumber) {
                            if (numberOrEmail.equals(c.mNumber)) {
                                return c;
                            }
                        } else {
                            if (PhoneNumberUtils.compare(numberOrEmail, c.mNumber)) {
                                return c;
                            }
                        }
                    }
                } else {
                    candidates = new ArrayList<Contact>();
                    // call toString() since it may be the static CharBuffer
                    mContactsHash.put(key, candidates);
                }
                Contact c = isMe ?
                        new Contact(true) :
                        new Contact(numberOrEmail);
                candidates.add(c);
                return c;
            }
        }
        // Aurora xuyong 2014-10-23 added for privacy feature start
        private Contact internalGet(String numberOrEmail, boolean isMe, long privacy) {
            synchronized (ContactsCache.this) {
                // See if we can find "number" in the hashtable.
                // If so, just return the result.
                String workingNumberOrEmail = numberOrEmail;
                workingNumberOrEmail = workingNumberOrEmail.replaceAll(" ", "").replaceAll("-", "");
                String key = "";
                final boolean isNotRegularPhoneNumber = isMe || Mms.isEmailAddress(numberOrEmail) ||
                        MessageUtils.isAlias(numberOrEmail);
                if (isNotRegularPhoneNumber) {
                    key = numberOrEmail;
                } else if (PhoneNumberUtils.isWellFormedSmsAddress(workingNumberOrEmail)) {
                    if (workingNumberOrEmail.length() > NORMAL_NUMBER_MAX_LENGTH) {
                        // handle number like 1252002613111111111
                        key = workingNumberOrEmail;
                    } else {
                        numberOrEmail = workingNumberOrEmail;
                        key = key(numberOrEmail, sStaticKeyBuffer);
                    }
                } else {
                    workingNumberOrEmail = PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
                    workingNumberOrEmail = PhoneNumberUtils.formatNumber(workingNumberOrEmail);
                    if (numberOrEmail.equals(workingNumberOrEmail)) {
                        numberOrEmail = PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
                        key = key(numberOrEmail, sStaticKeyBuffer);
                    } else {
                        key = numberOrEmail;
                    }
                }

                ArrayList<Contact> candidates = mContactsHash.get(key + privacy);
                if (candidates != null) {
                    int length = candidates.size();
                    for (int i = 0; i < length; i++) {
                        Contact c= candidates.get(i);
                        if (isNotRegularPhoneNumber) {
                            if (numberOrEmail.equals(c.mNumber) && c.getPrivacy() == privacy) {
                                return c;
                            }
                        } else {
                            if (PhoneNumberUtils.compare(numberOrEmail, c.mNumber) && c.getPrivacy() == privacy) {
                                
                                return c;
                            }
                        }
                    }
                } else {
                    candidates = new ArrayList<Contact>();
                    // call toString() since it may be the static CharBuffer
                    mContactsHash.put(key + privacy, candidates);
                }
                Contact c = isMe ?
                        new Contact(true) :
                        new Contact(numberOrEmail, privacy);
                candidates.add(c);
                return c;
            }
        }
        // Aurora xuyong 2014-10-23 added for privacy feature end
        void invalidate() {
            // Don't remove the contacts. Just mark them stale so we'll update their
            // info, particularly their presence.
            synchronized (ContactsCache.this) {
                for (ArrayList<Contact> alc : mContactsHash.values()) {
                    for (Contact c : alc) {
                        synchronized (c) {
                            c.mIsStale = true;
                            c.mIsValid = false;
                        }
                    }
                }
            }
        }

        //gionee gaoj 2012-3-22 added for CR00555790 start
        public Contact get(String number, boolean canBlock, int flag) {
            if (V) logWithTrace("get(%s, %s)", number, canBlock);

            if (TextUtils.isEmpty(number)) {
                number = "";        // In some places (such as Korea), it's possible to receive
                                    // a message without the sender's address. In this case,
                                    // all such anonymous messages will get added to the same
                                    // thread.
            }

            // Always return a Contact object, if if we don't have an actual contact
            // in the contacts db.
            Contact contact = getContact(number, flag);
            Runnable r = null;

            synchronized (contact) {
                // If there's a query pending and we're willing to block then
                // wait here until the query completes.
                while (canBlock && contact.mQueryPending) {
                    try {
                        contact.wait();
                    } catch (InterruptedException ex) {
                        // try again by virtue of the loop unless mQueryPending is false
                    }
                }

                // If we're stale and we haven't already kicked off a query then kick
                // it off here.
                if (contact.mIsStale && !contact.mQueryPending) {
                    contact.mIsStale = false;

                    if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                        log("async update for " + contact.toString() + " canBlock: " + canBlock +
                                " isStale: " + contact.mIsStale);
                    }

                    final Contact c = contact;
                    r = new Runnable() {
                        public void run() {
                            updateContact(c);
                        }
                    };

                    // set this to true while we have the lock on contact since we will
                    // either run the query directly (canBlock case) or push the query
                    // onto the queue.  In either case the mQueryPending will get set
                    // to false via updateContact.
                    contact.mQueryPending = true;
                }
            }
            // do this outside of the synchronized so we don't hold up any
            // subsequent calls to "get" on other threads
            if (r != null) {
                if (canBlock) {
                    r.run();
                } else {
                    pushTask(r);
                }
            }
            return contact;
        }

        public Contact getContact(String numberOrEmail, int flag) {
            synchronized (ContactsCache.this) {
                // See if we can find "number" in the hashtable.
                // If so, just return the result.
                final boolean isNotRegularPhoneNumber = Mms.isEmailAddress(numberOrEmail) ||
                        MessageUtils.isAlias(numberOrEmail);
                final String key = isNotRegularPhoneNumber ?
                        numberOrEmail : key(numberOrEmail, sStaticKeyBuffer);

                ArrayList<Contact> candidates = mContactsHash.get(key);
                if (candidates != null) {
                    int length = candidates.size();
                    Contact c = null;
                    for (int i = 0; i < length; i++) {
                        c = candidates.get(i);
                        if (isNotRegularPhoneNumber) {
                            if (numberOrEmail.equals(c.mNumber)) {
                                return c;
                            }
                        } else {
                            if (PhoneNumberUtils.compare(numberOrEmail, c.mNumber)) {
                                c = new Contact(numberOrEmail);
                                candidates.set(i, c);
                                return c;
                            }
                        }
                    }
                } else {
                    candidates = new ArrayList<Contact>();
                    // call toString() since it may be the static CharBuffer
                    mContactsHash.put(key, candidates);
                }
                Contact c = new Contact(numberOrEmail);
                candidates.add(c);
                return c;
            }
        }
        //gionee gaoj 2012-3-22 added for CR00555790 end
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

// a0
    protected Contact(String number, String label, String name, String nameAndNumber, long personId, int presence, String presenceText) {
        setNumber(number);
        mLabel = label;
        mName = name;
        mNameAndNumber = nameAndNumber;
        mPersonId = personId;
        mPresenceResId = sContactCache.getPresenceIconResourceId(presence);
        mPresenceText = presenceText;
        mNumberIsModified = false;
        mIsStale = true;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature start
    protected Contact(String number, String label, String name, String nameAndNumber, long personId, int presence, String presenceText, long privacy) {
        setNumber(number);
        mLabel = label;
        mName = name;
        mNameAndNumber = nameAndNumber;
        mPersonId = personId;
        mPresenceResId = sContactCache.getPresenceIconResourceId(presence);
        mPresenceText = presenceText;
        mNumberIsModified = false;
        mIsStale = true;
        setPrivacy(privacy);
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
    public synchronized void reload(boolean isBlock) {
        mIsStale = true;
        sContactCache.get(mNumber, isBlock);
    }

    public static String getValidNumber(String numberOrEmail) {
        if (numberOrEmail == null) {
            return null;
        }
        String workingNumberOrEmail = new String(numberOrEmail);
        
        //Gionee:tianxiaolong 2012.9.3 modify for CR00686770 begin
        // Gionee: 20121023 chenrui modify for CR00717533 begin
        /*
        if(gnFlyFlag){
            workingNumberOrEmail = workingNumberOrEmail.replaceAll("\\(", " ").replaceAll("\\)", " ");
        }
        */
        // Aurora xuyong 2014-11-26 deleted for bug #9503 start
        //workingNumberOrEmail = workingNumberOrEmail.replaceAll("\\(", " ").replaceAll("\\)", " ");
        // Aurora xuyong 2014-11-26 deleted for bug #9503 end
        // Gionee: 20121023 chenrui modify for CR00717533 end
        //Gionee:tianxiaolong 2012.9.3 modify for CR00686770 end
        
        workingNumberOrEmail = workingNumberOrEmail.replaceAll(" ", "").replaceAll("-", "");
        if (numberOrEmail.equals(SELF_ITEM_KEY) || Mms.isEmailAddress(numberOrEmail)) {
            return numberOrEmail;
        } else if (PhoneNumberUtils.isWellFormedSmsAddress(workingNumberOrEmail)) {
            return workingNumberOrEmail;
        } else {
            workingNumberOrEmail = PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
            workingNumberOrEmail = PhoneNumberUtils.formatNumber(workingNumberOrEmail);
            if (numberOrEmail.equals(workingNumberOrEmail)) {
                return PhoneNumberUtils.stripSeparators(workingNumberOrEmail);
            } else {
                return numberOrEmail;
            }
        }
    }

    protected static byte[] loadAvatarData(Contact entry, Context mContext) {
        byte [] data = null;

        if ((!entry.mIsMe && entry.mPersonId == 0) || entry.mAvatar != null) {
            return null;
        }

        if (V) {
            log("loadAvatarData: name=" + entry.mName + ", number=" + entry.mNumber);
        }

        // If the contact is "me", then use my local profile photo. Otherwise, build a
        // uri to get the avatar of the contact.
        Uri contactUri = entry.mIsMe ?
                Profile.CONTENT_URI :
                ContentUris.withAppendedId(Contacts.CONTENT_URI, entry.mPersonId);

        InputStream avatarDataStream = Contacts.openContactPhotoInputStream(
                    mContext.getContentResolver(),
                    contactUri);
        try {
            if (avatarDataStream != null) {
                data = new byte[avatarDataStream.available()];
                avatarDataStream.read(data, 0, data.length);
            }
        } catch (IOException ex) {
            //
        } finally {
            try {
                if (avatarDataStream != null) {
                    avatarDataStream.close();
                }
            } catch (IOException e) {
            }
        }

        return data;
    }
// a1
    //gionee gaoj 2012-3-22 added for CR00555790 start
    public static synchronized Contact get(String number,  boolean canBlock, int flag) {
        return sContactCache.get(number, canBlock, flag);
    }
    
    public static synchronized long getIdInDatabase(String number, boolean canBlock) {
        return get(number, canBlock).mPersonId;
    }

    public static void clear() {
        mListeners.clear();
    }
    //gionee gaoj 2012-3-22 added for CR00555790 end
    // Aurora xuyong 2014-10-23 added for privacy feature start
    public long getPrivacy() {
        return mPrivacy;
    }
    
    public void setPrivacy(long num) {
        mPrivacy = num;
    }
    // Aurora xuyong 2014-10-23 added for privacy feature end
}
