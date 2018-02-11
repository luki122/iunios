package com.xy.smartsms.manager;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;

import cn.com.xy.sms.sdk.ui.bubbleview.DuoquBubbleViewManager;
import cn.com.xy.sms.sdk.ui.popu.util.XySdkUtil;
import cn.com.xy.sms.sdk.ui.simplebubbleview.DuoquSimpleBubbleViewManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;

import com.android.mms.R;
import com.xy.smartsms.iface.IXYSmartMessageItem;
import com.xy.smartsms.iface.IXYSmartSmsHolder;
import com.xy.smartsms.iface.IXYSmartSmsListItemHolder;

public class XYBubbleListItem implements SdkCallBack{
private final static String TAG="XIAOYUAN";
    
    public final static int DUOQU_SMARTSMS_SHOW_BUBBLE_RICH=2;
    public final static int DUOQU_SMARTSMS_SHOW_BUBBLE_SIMPLE=1;
    public final static int DUOQU_SMARTSMS_SHOW_DEFAULT_PRIMITIVE =0;
    public final static int DUOQU_SMARTSMS_BIND_BUBBLE_FAILED=-1;
    private final static int DUOQU_CALLBACK_UITHREAD_NEEDPARSE=-2;
    private final static int DUOQU_CALLBACK_UITHREAD_NODATA=-1;
    private final static int DUOQU_CALLBACK_UITHREAD_HASDATA=0;
    private final static int DUOQU_CALLBACK_BACKTHREAD_HASDATA=1;
    private final static int DUOQU_CALLBACK_UITHREAD_SCOLLING=-4;
    private final static String DUOQU_RICH_BUBBLE_DISPLAY="DISPLAY";
    private SdkCallBack mSimpleCallBack =null;
    private IXYSmartSmsHolder mXYsmsHolder=null;
    private ViewGroup mRichItemGroup = null;
    private ViewGroup mSimpleItemGroup=null;
    private View mSmsRootGroup=null;
    private IXYSmartMessageItem mMessageItem=null;
    private IXYSmartSmsListItemHolder mMsgListItem =null;
    private JSONObject mCacheItemData = null;
    private Activity mCtx;
    private int mShowBubbleModel;
    // Aurora xuyong 2016-03-05 modified for bug #20723 start
    public static HashMap<Long, Boolean> sXYBubbleCache = new HashMap<Long, Boolean>();
    // Aurora xuyong 2016-03-05 modified for bug #20723 end

    public static void clearCache() {
        sXYBubbleCache.clear();
    }

    private ImageButton mXySwitchBt;
    
    /**
     *  initialization SmartSmsBubble
     */
    public XYBubbleListItem(IXYSmartSmsHolder xySmsHolder,IXYSmartSmsListItemHolder msgListItem){
        this.mXYsmsHolder=xySmsHolder;
        this.mMsgListItem= msgListItem;
        if(null == mMsgListItem  || null ==xySmsHolder){
            return;
        }
        mCtx= xySmsHolder.getActivityContext();
        initView();
    }

    /**
     *  bind bubble view by showBubbleMode
     *  showBubbleMode 1:simple bubble 2:rich bubble
     *  if simple bubble need load rich bubble data,use control simple/rich change btn visibility
     */
    public void bindBubbleView(final IXYSmartMessageItem mMessageItem,int showBubbleMode){
        if(mMessageItem == null ||  null == mXYsmsHolder){
            return;
        }
        this.mMessageItem=mMessageItem;
        this.mShowBubbleModel = showBubbleMode;
        // Aurora xuyong 2016-02-04 added for xy-smartsms start
        View contentLayout = mMsgListItem.getDefaultContent();
        if (contentLayout != null) {
            contentLayout.setVisibility(View.GONE);
            mXySwitchBt.setVisibility(View.GONE);
        }
        // Aurora xuyong 2016-02-04 added for xy-smartsms end
        bindRichBubbleView(showBubbleMode);
        if(showBubbleMode == DUOQU_SMARTSMS_SHOW_BUBBLE_SIMPLE) {
            bindSimpleBubbleView();
        }
    }
    private void bindRichBubbleView(int showBubbleModel) {
        if(null == mXYsmsHolder || null == mMessageItem){
            android.util.Log.w(TAG, "com.android.mms.ui.SmartSmsBubbleManager.bindRichBubbleView mSmartSmsUiHolder or mMessageItem is null");
            return;
        }
        if(null == mRichItemGroup){
            android.util.Log.w(TAG, "com.android.mms.ui.SmartSmsBubbleManager.bindRichBubbleView mRichItemGroup  is null");
            return;
        }
        mCacheItemData = XySdkUtil.getBubbleDataFromCache(mMessageItem.getMsgId());
        // parse RichBubble-data and displayed SimpleBubble
        if(mCacheItemData == null) {
            getRichBubbleData();
        } else  if(getShowRichViewStatu() == DUOQU_SMARTSMS_SHOW_BUBBLE_RICH ){
            bindRichView(false);
        }else{
            showDefaultListItem(false);
        }
    }
 
    /**
     *  hide bubble view 
     */
    public void hideBubbleView() {
        showSimpleBubbleView();
    }
    private void showSimpleBubbleView(){
        if (mSmsRootGroup != null) {
            mSmsRootGroup.setVisibility(View.VISIBLE);
        }
        if (mRichItemGroup != null) {
            mRichItemGroup.setVisibility(View.GONE);
        }
    }
    private void showRichBubbleView(){
        if (mSmsRootGroup != null) {
           mSmsRootGroup.setVisibility(View.GONE);
        }
        if (mRichItemGroup != null) {
           mRichItemGroup.setVisibility(View.VISIBLE);
           mXySwitchBt.setVisibility(View.VISIBLE);
        }
    }
 

    /**
     *  initialization SmartSmsBubble all views
     */
    private void initView() {
        //if need simple button
        mSimpleItemGroup = (ViewGroup)mMsgListItem.findViewById(R.id.duoqu_simple_bubble_action_group);
        mRichItemGroup = (ViewGroup)mMsgListItem.findViewById(R.id.duoqu_rich_item_group);
        mSmsRootGroup = mMsgListItem.findViewById(R.id.message_block);
        mXySwitchBt = (ImageButton)mMsgListItem.findViewById(R.id.aurora_xy_switch_bt);
        mXySwitchBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mMsgListItem != null && mMsgListItem.getDefaultContent() != null && mMsgListItem.getDefaultContent().getVisibility() != View.VISIBLE) {
                    // Aurora xuyong 2016-03-05 modified for bug #20723 start
                    sXYBubbleCache.put(XYBubbleListItem.this.mMessageItem.getMsgId(), false);
                    // Aurora xuyong 2016-03-05 modified for bug #20723 end
                    mMsgListItem.showDefaultListItem();
                    mMsgListItem.getDefaultContent().setVisibility(View.VISIBLE);
                    mRichItemGroup.setVisibility(View.GONE);
                } else {
                    // Aurora xuyong 2016-03-05 modified for bug #20723 start
                    sXYBubbleCache.put(XYBubbleListItem.this.mMessageItem.getMsgId(), true);
                    // Aurora xuyong 2016-03-05 modified for bug #20723 end
                    bindRichView(false);
                    mMsgListItem.getDefaultContent().setVisibility(View.GONE);
                    mRichItemGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private int getShowRichViewStatu(){
        try {
            if(mCacheItemData == null){
                return DUOQU_SMARTSMS_BIND_BUBBLE_FAILED;
            }
            else if(!mCacheItemData.has(DUOQU_RICH_BUBBLE_DISPLAY)){
                return DUOQU_SMARTSMS_SHOW_BUBBLE_RICH;
            }else {
                return mCacheItemData.getInt(DUOQU_RICH_BUBBLE_DISPLAY);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return DUOQU_SMARTSMS_SHOW_BUBBLE_SIMPLE;
    }
    private void setShowRichViewStatu(int statu){
        try {
            if(mCacheItemData == null){
                return;
            }
            mCacheItemData.put(DUOQU_RICH_BUBBLE_DISPLAY, statu);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * bind and display RichBubbleView
     */
    private boolean bindRichView(boolean anim){
        
        final String msgIds = String.valueOf(mMessageItem.getMsgId());
        final HashMap extendMap = mMessageItem.getSmartSmsExtendMap();
        extendMap.put("isClickAble", !mXYsmsHolder.isEditAble());
        View richBubbleView = DuoquBubbleViewManager.getRichBubbleView(mCtx, mCacheItemData,msgIds,mMessageItem.getSmsBody(),
                mMessageItem.getPhoneNum(), 
                mMessageItem.getSmsReceiveTime(),
                mMsgListItem.getListItemView(), 
                mXYsmsHolder.getListView(), 
                extendMap);
        // Aurora xuyong 2016-03-05 modified for bug #20723 start
        Boolean result = sXYBubbleCache.get(this.mMessageItem.getMsgId());
        // Aurora xuyong 2016-03-05 modified for bug #20723 end
        if(richBubbleView != null && mRichItemGroup!=null && (result == null || result.booleanValue())){
            mRichItemGroup.removeAllViews();
            ViewParent parent= richBubbleView.getParent();
            if(parent instanceof ViewGroup){
                ViewGroup p= (ViewGroup)parent;
                p.removeView(richBubbleView);
            }
            mRichItemGroup.addView(richBubbleView);
            showRichBubbleView();
            setShowRichViewStatu(DUOQU_SMARTSMS_SHOW_BUBBLE_RICH);
        }else{
            if (result == null) {
                setShowRichViewStatu(DUOQU_SMARTSMS_BIND_BUBBLE_FAILED);
                noRichDataShowDefaultListItem();
            } else if (!result.booleanValue()) {
                if(mMsgListItem != null){
                    mMsgListItem.showDefaultListItem();
                    mXySwitchBt.setVisibility(View.VISIBLE);
                }
            }
        }
        return true;
    }

    /**
     *  displayed views according to callback status
     */
    private void getRichBubbleData(){
        final String msgId = String.valueOf(mMessageItem.getMsgId());
        mRichItemGroup.setTag(msgId);
        DuoquBubbleViewManager.getRichBubbleData(
                mCtx,
                msgId,
                mMessageItem.getPhoneNum(),
                mMessageItem.getServiceCenterNum(),
                mMessageItem.getSmsBody(),
                mMessageItem.getSmsReceiveTime(),
                DuoquBubbleViewManager.DUOQU_RETURN_CACHE_SDK_MSG_ID,
                mMsgListItem.getListItemView(),
                null, 
                mRichItemGroup,
                mXYsmsHolder.getListView(),
                mMessageItem.getSmartSmsExtendMap(),this,isScrollFing());
    }

    /**
     * getRichBubbleData callback
     */
    public void execute(Object... obj) {
       
        if(obj==null || obj.length==0 || mXYsmsHolder ==null){
            noRichDataShowDefaultListItem();
            return;
        }
        if(obj.length > 2){
            String oldmsgid = (String)obj[2];
            String orgMsgId = (String)mRichItemGroup.getTag();
            if(StringUtils.isNull(orgMsgId)||StringUtils.isNull(oldmsgid)||!orgMsgId.equals(oldmsgid)){
                return;
            }
        }
        final int status = (Integer)obj[0]; 
        android.util.Log.i("duoqu_test", "status: "+status+" msgid: "+obj[2]+" obj[1]: "+obj[1]);
        switch(status){
        case DUOQU_CALLBACK_UITHREAD_NEEDPARSE:
            //cureent msg  need parse,show default  
            noRichDataShowDefaultListItem();
            break;
        case DUOQU_CALLBACK_UITHREAD_NODATA: 
           //current msg  has not rich data,show default
            noRichDataShowDefaultListItem();
            break;
        case DUOQU_CALLBACK_UITHREAD_HASDATA:
            //UI THREAD CALLBACK HAS DATA
            mCacheItemData=(JSONObject)obj[1];
            if(mShowBubbleModel == DUOQU_SMARTSMS_SHOW_BUBBLE_RICH && getShowRichViewStatu() == DUOQU_SMARTSMS_SHOW_BUBBLE_RICH){
                bindRichView(false);
            }else{
                showDefaultListItem(false);
            }
            addRichItemDataToCache(mMessageItem.getMsgId(), mCacheItemData);
            break;
        case DUOQU_CALLBACK_BACKTHREAD_HASDATA:
            //BACKGROUD THREAD CALLBACK HASDATA
            if(null == mCtx){
                return;
            }
            mCacheItemData = (JSONObject)obj[1];
            addRichItemDataToCache(mMessageItem.getMsgId(), mCacheItemData);
            break;
		case DUOQU_CALLBACK_UITHREAD_SCOLLING:
        	  noRichDataShowDefaultListItem();
        default:
            
            break;
        }
    }
    private boolean isScrollFing(){
        if(null == mXYsmsHolder || null ==mXYsmsHolder.getListView()){
            return false;
        }
        boolean scroll_state=mXYsmsHolder.getListView().getFirstVisiblePosition()==0?true:false;
        return scroll_state?false:mXYsmsHolder.isScrolling();
    }
    /**
     * show default msg body ListItem
     */
    private void showDefaultListItem(boolean needAnim){
        if(mMsgListItem != null){
            mMsgListItem.showDefaultListItem();
        }
        bindSimpleBubbleView();
        hideBubbleView();
  
    }
    /**
     * has not richview only show  default msg body ListItem
     */
    private void noRichDataShowDefaultListItem(){
        mCacheItemData=null;
        showDefaultListItem(false);
    }
    /**
     * bind and display SimpleBubbleView
     */
    private void bindSimpleBubbleView(){
        if(mSimpleItemGroup ==null){
            return;
        }
        final String msgIds = String.valueOf(mMessageItem.getMsgId());
        mSimpleItemGroup.setTag(msgIds);
        try{
            //query simple bubble data 
            DuoquSimpleBubbleViewManager.getSimpleBubbleData(
                msgIds, mMessageItem.getPhoneNum(),
                mMessageItem.getServiceCenterNum(), mMessageItem.getSmsBody(),
                mMessageItem.getSmsReceiveTime(),
                DuoquSimpleBubbleViewManager.DUOQU_RETURN_CACHE_SDK_MSG_ID, mMessageItem.getSmartSmsExtendMap(),
                getSimpleBubbleDataCallBack(),isScrollFing());
        }catch(Exception e){
            mSimpleItemGroup.setVisibility(View.GONE);
            Log.e(TAG, "com.android.mms.ui.SmartSmsBubbleManager.getSimpleBubbleData error", e);
        }
        
    }
    private SdkCallBack  getSimpleBubbleDataCallBack(){
         if(mSimpleCallBack ==null){
             mSimpleCallBack = new SdkCallBack(){
                public void execute(final Object... obj) {
                    if(obj==null || obj.length==0 || null == mXYsmsHolder){
                        return;
                    }
                    final int statu = (Integer)obj[0];
                    if(obj.length > 2){
                        String oldmsgid = (String)obj[2];
                        String orgMsgId = (String)mSimpleItemGroup.getTag();
                        if(StringUtils.isNull(orgMsgId)||StringUtils.isNull(oldmsgid)||!orgMsgId.equals(oldmsgid)){
                            return;
                        }
                    }
                    switch(statu){
                    case DUOQU_CALLBACK_UITHREAD_NEEDPARSE:
                        bindSimpleView(null,mMessageItem, mMsgListItem.getListItemView());
                        break;
                    case DUOQU_CALLBACK_UITHREAD_NODATA: 
                        bindSimpleView(null,mMessageItem, mMsgListItem.getListItemView());
                        break;
                    case DUOQU_CALLBACK_UITHREAD_HASDATA:
                        bindSimpleView((JSONArray)obj[1], mMessageItem, mMsgListItem.getListItemView());
                        break;
                    default:
                        break;  
                }
                }
            };
        }
        return mSimpleCallBack;
    }
    /**
     * set views when displayed SimpleBubble
     */
    private void bindSimpleView(JSONArray btnData,final IXYSmartMessageItem mMessageItem,final View msgListItem){
        View buttonView= null;
        try{
            if(btnData != null){
                HashMap<String,Object> extend= mMessageItem.getSmartSmsExtendMap();
                extend.put("isClickAble", !mXYsmsHolder.isEditAble());
                buttonView=DuoquSimpleBubbleViewManager.getSimpleBubbleView(mCtx,btnData, mSimpleItemGroup,extend); 
            }
            if(buttonView!=null && mSimpleItemGroup != null){
                mSimpleItemGroup.setVisibility(View.VISIBLE);
                mXySwitchBt.setVisibility(View.VISIBLE);
            } else if(mSimpleItemGroup!=null){
                mSimpleItemGroup.setVisibility(View.GONE);
            }
        }catch(Exception e){
            e.printStackTrace();
            if(mSimpleItemGroup!=null){
                mSimpleItemGroup.setVisibility(View.GONE);
            }
        }
    }
    private void addRichItemDataToCache(long msgId,JSONObject itemData){
        if(itemData == null){
            return;
        }
        if(!itemData.has(DUOQU_RICH_BUBBLE_DISPLAY)){
            setShowRichViewStatu(mShowBubbleModel);
        }
        XySdkUtil.putBubbleDataToCache(msgId, itemData);
    }
    
//    public static void loadBubbleData(Context ctx,final String recipientNumber){
//    if(ctx == null){
//        return;
//    }
//    Thread thread =  new Thread(){
//        public void run() {
//            ParseBubbleManager.loadBubbleDataByPhoneNum(recipientNumber,true);
////            mIsNotifyComposeMessage=ParseManager.isEnterpriseSms(ctx, recipientNumber, null, null);
//        } 
//    };
//    thread.start();
//}
}
