package cn.com.xy.sms.sdk.ui.menu;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.util.ParseManager;
 


public class PopMenus {
    private JSONArray mJsonArray;
    private Activity mContext;
    private PopupWindow mPopupWindow;
    private LinearLayout mListView;
    private int mWidth, mHeight;
    private View mContainerView;
    private View mParentView = null;
    private boolean mIsShow =false;

    @SuppressLint("ResourceAsColor")
    public PopMenus(Activity context, JSONArray _jsonArray, int _width, int _height) {
        this.mContext = context;
        this.mJsonArray = _jsonArray;
        this.mWidth = _width;
        this.mHeight = _height;
        mContainerView = LayoutInflater.from(context).inflate(R.layout.duoqu_popmenus, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0f);
        mContainerView.setLayoutParams(lp);
    
        mListView = (LinearLayout) mContainerView.findViewById(R.id.layout_subcustommenu);
        try {
            setSubMenu();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mListView.setBackgroundColor(R.color.duoqu_white);
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(false);
        mPopupWindow = new PopupWindow(mContainerView, mWidth == 0 ? LayoutParams.WRAP_CONTENT : mWidth, mHeight == 0 ? LayoutParams.WRAP_CONTENT : mHeight);
        mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
    }

    
    
    public void showAtLocation(View parent) {
        mParentView = parent;
        showPopupAccordingParentView();
    }

    public void showPopupAccordingParentView(){
        if(mParentView == null){
            return;
        }
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mContainerView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
//        int x = (int) ViewUtil.getDimension(R.dimen.duoqu_popu_menu_x);
        int popupWith = mPopupWindow.getContentView().getMeasuredWidth();
        int parentWith = mParentView.getWidth();
        int x = (parentWith - popupWith)/2;
        mPopupWindow.showAsDropDown(mParentView, x, -mPopupWindow.getContentView().getMeasuredHeight()-mParentView.getMeasuredHeight());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(false);
        mPopupWindow.update();
        mPopupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                mIsShow = false;
                if(mParentView != null){
                    mParentView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mParentView.setTag(null);
                        }
                    }, 200);
                }
                destory();
            }
        });
        mIsShow = true;
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    void setSubMenu() throws JSONException {
        mListView.removeAllViews();
        for (int i = 0; i < mJsonArray.length(); i++) {
            final JSONObject ob = mJsonArray.getJSONObject(i);
            LinearLayout layoutItem = (LinearLayout) ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.duoqu_pomenu_menuitem, null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
            mContainerView.setLayoutParams(lp);
            layoutItem.setFocusable(true);
            TextView tv_funbtntitle = (TextView) layoutItem.findViewById(R.id.pop_item_textView);
            View pop_item_line = layoutItem.findViewById(R.id.pop_item_line);
            if ((i + 1) == mJsonArray.length()) {
                pop_item_line.setVisibility(View.GONE);
            }
            tv_funbtntitle.setText(ob.getString("name"));
            layoutItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    try {
                        Map<String, String> extend = new HashMap<String, String>();
                        extend.put("simIndex", "0");
                        ParseManager.doAction(mContext, ob.get("action_data").toString(), extend);
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    

                }
            });
            mListView.addView(layoutItem);
        }
        mListView.setVisibility(View.VISIBLE);
    }
    private void destory(){
       mContext =null;
       mJsonArray =null;
    }
    
    public boolean isShow(){
        return mIsShow;
    }

}
