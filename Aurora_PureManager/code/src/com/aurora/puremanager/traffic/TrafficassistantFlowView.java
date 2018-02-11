package com.aurora.puremanager.traffic;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aurora.puremanager.R;

public class TrafficassistantFlowView extends LinearLayout {
	
	private TextView tv_month_date;
	private TextView tv_num;
	private TextView tv1;
	private TextView tv2;
	private RelativeLayout rl_progress;
	private SeekBar seekBar;
	private TextView tv_min_flow;
	private TextView tv_max_flow;
	private Button btn_set;
	
	private String setCard;
	private String setCardOk;
	
	public TrafficassistantFlowView(Context context) {
		super(context);
		initView();
	}
	
	public TrafficassistantFlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public TrafficassistantFlowView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	public TrafficassistantFlowView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView();
	}
	
	private void initView() {
		setCard = getContext().getResources().getString(R.string.traffic_set_card);
		setCardOk = getContext().getResources().getString(R.string.traffic_set_card_ok);
		
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.trafficassistant_flow_view, this, true);
		
		tv_month_date = (TextView) view.findViewById(R.id.tv_month_date);
		tv_num = (TextView) view.findViewById(R.id.tv_num);
		tv1 = (TextView) view.findViewById(R.id.tv_1);
		tv2 = (TextView) view.findViewById(R.id.tv_2);
		rl_progress = (RelativeLayout) view.findViewById(R.id.rl_progress);
		seekBar = (SeekBar) view.findViewById(R.id.seekBar);
		tv_min_flow = (TextView) view.findViewById(R.id.tv_min_flow);
		tv_max_flow = (TextView) view.findViewById(R.id.tv_max_flow);
		btn_set = (Button) view.findViewById(R.id.btn_set);
		
	}
	
	public void setSetButtonVisable(boolean visable) {
		if (btn_set != null) {
			if (visable) {
				btn_set.setVisibility(View.VISIBLE);
			} else {
				btn_set.setVisibility(View.GONE);
			}
		}
	}
	
	public void setSetButtonEnable(boolean enable) {
		if (btn_set != null) {
			if (enable) {
				btn_set.setEnabled(true);
				btn_set.setAlpha(1);
				btn_set.setText(setCard);
			} else {
				btn_set.setEnabled(false);
				btn_set.setAlpha(0.4f);
				btn_set.setText(setCardOk);
			}
		}
	}
	
	public void setHasSetPackage(boolean hasSet) {
		if (hasSet) {
			tv_month_date.setVisibility(View.VISIBLE);
			rl_progress.setVisibility(View.VISIBLE);
		} else {
			tv_month_date.setVisibility(View.GONE);
			rl_progress.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setSetButtonListener(OnClickListener onClickListener) {
		if (btn_set != null) {
			btn_set.setOnClickListener(onClickListener);
		}
	}
	
	/*********************begin**********************/
	private AtomicBoolean isDuringSetProgress= new AtomicBoolean(false);
	private int curProgress = 0;
	private boolean isWarningProgress;
	
	/**
	 * 设置进度,不并伴随动画
	 * @param totalFlow (单位KB)
	 * @param progressFlow (单位KB)
	 */
	public void setProgressNoAnim(final long totalFlow, final long progressFlow) {
		calculateCurProgress(totalFlow, progressFlow);
		resetViewOfNum();
		changeProgress();
		updateViewOfNum(totalFlow, progressFlow, 1);
		isDuringSetProgress.set(false);
	}
	
	/**
	 * 计算curProgress的值
	 * @param totalFlow
	 * @param progressFlow
	 */
	private void calculateCurProgress(long totalFlow,long progressFlow){
		curProgress = getProgressValue(totalFlow,progressFlow);
		System.out.println("curProgress: " + curProgress);
//		isWarningProgress = FlowUtils.isWarningProgress(getContext());
	}
	
	/**
	 * 根据总流量和当前流量值计算进度条的比例
	 * @param totalFlow
	 * @param progressFlow
	 * @return
	 */
	public static synchronized int getProgressValue(long totalFlow,long progressFlow){
		int progressValue = 0;
		if(totalFlow == 0){
			progressValue = 50;
		}else{
			progressValue =(int)Math.round(100.0*progressFlow/totalFlow);
		}
		if(progressValue <0){
			progressValue = 0;
		}else if(progressValue >100){
			progressValue = 100;
		}
		return progressValue;
	}
	
	private void changeProgress() {
		if (curProgress < 0) {
			curProgress = 0;
		} else if (curProgress > 100) {
			curProgress = 100;
		}
		if (seekBar != null) {
			seekBar.setProgress(curProgress);
		}
	}
	
	private long curNum = -1;
	
	private void resetViewOfNum(){
		curNum = -1;	
	}
	
	/**
	 * @param totalFlow 单位为KB
	 * @param progressFlow 单位为KB
	 * @param interpolatedTime
	 */
	private void updateViewOfNum(long totalFlow,long progressFlow,float interpolatedTime){
        double scale = interpolatedTime;//必须要这样做，如果progressFlow=9999GB的时候，就会出现数据溢出	
        long tmpNum = Math.round(progressFlow*scale);	//起始值为0	
		if(tmpNum == curNum){
			return ;
		}
		curNum = tmpNum;
		boolean isExceedMonthlyFlow = false;//是否超出月结流量
		if(tmpNum < 0){
			isExceedMonthlyFlow = true;
			tmpNum = Math.abs(tmpNum);
		}
		double showNum;
        if(progressFlow >= 1024 || totalFlow >= 1024){//使用流量已经操过1M
        	if(tmpNum>=1024*1024){
    			if(totalFlow == 0){
    				setFlowTextSrc(R.string.net_flow_used);
    				setFlowUnitSrc(R.string.net_flow_gb);
    			}else{
    				if(isExceedMonthlyFlow){
    					setFlowTextSrc(R.string.net_flow_exceed);
    					setFlowUnitSrc(R.string.net_flow_gb);   					
    				}else{
    					setFlowTextSrc(R.string.net_flow_remainder);
    					setFlowUnitSrc(R.string.net_flow_gb);   					
    				} 				
    			}	
    			if(interpolatedTime < 1 ){
    				showNum = (int)Math.round(1.0*tmpNum/(1024*1024));
    			}else{
    				showNum = 1.0*tmpNum/(1024*1024);
        			if(showNum < 10 ){
        				showNum = Math.round(showNum*100)/100.0;
        			}else if(showNum < 100){
        				showNum = Math.round(showNum*10)/10.0;
        			}else{
        				showNum = (int)Math.round(showNum);
        			}
    			}  			
    		}else{
    			if(totalFlow == 0){ 				
    				setFlowTextSrc(R.string.net_flow_used);
    				setFlowUnitSrc(R.string.net_flow_mb);
    			}else{
    				if(isExceedMonthlyFlow){
    					setFlowTextSrc(R.string.net_flow_exceed);
    					setFlowUnitSrc(R.string.net_flow_mb);				
    				}else{
    					setFlowTextSrc(R.string.net_flow_remainder);
    					setFlowUnitSrc(R.string.net_flow_mb);    					
    				} 	  					
    			}		
    			showNum = (int)Math.round(1.0*tmpNum/(1024));
    		}
		}else{
			if(totalFlow == 0){
				setFlowTextSrc(R.string.net_flow_used);
				setFlowUnitSrc(R.string.net_flow_kb);    						
			}else{
				if(isExceedMonthlyFlow){
					setFlowTextSrc(R.string.net_flow_exceed);
					setFlowUnitSrc(R.string.net_flow_kb);			
				}else{
					setFlowTextSrc(R.string.net_flow_remainder);
					setFlowUnitSrc(R.string.net_flow_kb);					
				} 				
			}		
			showNum = (int)tmpNum;
		}
        Log.i("ProgressScrollView", "showNum="+showNum);
        
		int tmpNumOfDecimal1 = (int) (showNum * 10 % 10);
		int tmpNumOfDecimal2 = (int) (showNum * 100 % 10);
		boolean decimal1Show = false, decimal2Show = false;

		if (tmpNumOfDecimal2 > 0) {
			decimal1Show = decimal2Show = true;
		} else if (tmpNumOfDecimal1 > 0) {
			decimal1Show = true;
		}
		
		if (decimal2Show) {
			tv_num.setText(String.valueOf(showNum));
		} else if (decimal1Show && !decimal2Show) {
			tv_num.setText(String.format("%.2f", showNum));
		} else {
			tv_num.setText(String.format("%.0f", showNum));
		}
	}
	
	private int lastResIdOfFlowUnit = 0;
	private int lastResIdOfFlowText = 0;
	
	private void setFlowUnitSrc(int resId){
		if(resId == lastResIdOfFlowUnit){
			return ;
		}
		getFlowUnitView().setText(resId);
		lastResIdOfFlowUnit = resId;
	}
	
	private void setFlowTextSrc(int resId) {
		if(resId == lastResIdOfFlowText){
			return ;
		}
		getFlowTextView().setText(resId);
		lastResIdOfFlowText = resId;
	}
	
	private TextView getFlowUnitView(){
		return tv2;
	}
	
	private TextView getFlowTextView() {
		return tv1;
	}
	
	public void updatePro(float useFlow, float totalFlow, boolean setTrafficPackages, String timeZoneStr) {
		setHasSetPackage(setTrafficPackages);
		
		tv_min_flow.setText("0" + Constant.STRING_UNIT_MB);
		if (Math.abs(totalFlow) >= Constant.UNIT) {
			String str = StringFormat.getStringFormat(Math.abs(totalFlow) / Constant.UNIT, 1);
            if (Integer.parseInt(str.substring(str.length() - 1, str.length())) == 0) {
                str = StringFormat.getStringFormat(Math.abs(totalFlow) / Constant.UNIT, 0);
            }
            tv_max_flow.setText(str + Constant.STRING_UNIT_GB);
		} else {
			String str = StringFormat.getStringFormat(Math.abs(totalFlow), 0);
			tv_max_flow.setText(str +Constant.STRING_UNIT_MB);
		}
		
		boolean isFloatUp = true;	// 是否四舍五入
		
		float difference;
        if (!setTrafficPackages) {
            difference = useFlow;
            setFlowTextSrc(R.string.net_flow_used);
        } else {
            difference = totalFlow - useFlow;
            if (difference > 0) {
            	setFlowTextSrc(R.string.net_flow_remainder);
            	int progress = (int) ((useFlow * 1.0 / totalFlow) * 100);
            	seekBar.setProgress(progress);
            	
            	isFloatUp = false;
            } else {
            	setFlowTextSrc(R.string.net_flow_exceed);
            	seekBar.setProgress(100);
            }
        }
        
        String str;
        if (Math.abs(difference) >= Constant.UNIT) {
        	if (isFloatUp) {	// 四舍五入
        		str = StringFormat.getStringFormat(Math.abs(difference) / Constant.UNIT, 1);
        		if (Integer.parseInt(str.substring(str.length() - 1, str.length())) == 0) {
        			str = StringFormat.getStringFormat(Math.abs(difference) / Constant.UNIT, 0);
        		}
        	} else {  // 不四舍五入（剩余流量的情况）
                DecimalFormat formater = new DecimalFormat();
                formater.setMaximumFractionDigits(2);
                formater.setGroupingSize(0);
                formater.setRoundingMode(RoundingMode.FLOOR);
                str = formater.format(Math.abs(difference) / Constant.UNIT);
        		
        		if (Integer.parseInt(str.substring(str.length() - 1, str.length())) == 0) {
        			str = StringFormat.getStringFormat(Math.abs(difference) / Constant.UNIT, 0);
        		}
        	}
            tv_num.setText(str);
            setFlowUnitSrc(R.string.net_flow_gb);
        } else {
        	str = StringFormat.getStringFormat(Math.abs(difference), 1);
            if (Integer.parseInt(str.substring(str.length() - 1, str.length())) == 0) {
                str = StringFormat.getStringFormat(Math.abs(difference), 0);
            }
            tv_num.setText(str);
            setFlowUnitSrc(R.string.net_flow_mb);
        }

        tv_month_date.setText(timeZoneStr);
//        getRemainderDaysToMonthEndDate();
	}

}
