package gn.com.android.update.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import gn.com.android.update.R;
public class AnimUtils {
    private static AnimUtils instance = null;
    private Context mContext;
    
    
    private int CheckedNotifyAnimId = R.anim.aurora_check_result_show;
    private int optionButtonShowAnimID = R.anim.aurora_option_btn_show_anim;
    private int bottomViewAnimID = R.anim.aurora_check_bottom_show;
    private AnimUtils(Context context){
        this.mContext = context;
    }
    
    public synchronized static AnimUtils getInstance(Context context){
        if(instance == null){
            instance = new AnimUtils(context);
        }
        return instance;
    }
    
    public  Animation getAnimation(int resID){
            return AnimationUtils.loadAnimation(mContext, resID);
    }
    
    public Animation getOptionAnimation(){
        return getAnimation(optionButtonShowAnimID);
    }

    public Animation getCheckedNotifyAnim(){
        return getAnimation(CheckedNotifyAnimId);
    }
    
    
    public Animation getBottomViewAnim(){
        return getAnimation(bottomViewAnimID);
    }
    public Animation getMainPagetranslateAnim(){
        return getAnimation(R.anim.aurora_main_page_translate);
    }
}
