package com.aurora.mms.countmanage;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ThreadPoolExecutor;
public class TotalCount implements Runnable {
    
    public static final String TAG = "TotalCount/Mms";
    
    public static final String MMS_MODULE_kEY = "100";
    public static final String MSG_SEN_RP = "011";
    public static final String MSG_DEL_RP = "012";
    public static final String MSG_REP_RP = "013";
    
    public static final String REJECT_MODULE_kEY = "180";
    // Aurora xuyong 2014-03-05 added for aurora's new feature start
    public static final String ACTION_REJECT = "addbl";
    public static final String ACTION_RESUME = "restoresms";
    // Aurora xuyong 2014-03-05 added for aurora's new feature end
    private Context m_context;
    private String m_module = "";
    private String m_action = "";
    private int value = 0;

    
    /**
     * 构建文件下载器
     * 
     * @param module_id 模块id
     * @param action_id 动作id
     * @param value 值
     */
    public TotalCount(Context context, String module_id, String action_id, int value) {
        this.m_context = context;
        this.m_module = module_id;
        this.m_action = action_id;
        this.value = value;
    }
    
    @Override
    public void run() {
        startCount();
    }

    /**
     * 开始计数
     * 
     */
    private void startCount() {
        int count=DataCount.updataData(m_context, m_module, m_action, value);
        Log.i(TAG, count+"");
    }
    

    
    //============对外控制方法开始=============//

    /**
     * 统计队列
     * 
     */
    public void countData() {
        ThreadPoolExecutor threadPool = CountManage.getThreadPoolExecutor();
        threadPool.execute(this);
    }
    

}
