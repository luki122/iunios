package aurora.lib.app;

import java.util.Calendar;

import aurora.lib.widget.AuroraDatePicker;
import aurora.lib.widget.AuroraTimePicker;
import aurora.lib.widget.AuroraTimePicker.OnTimeChangedListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.aurora.lib.R;
import com.aurora.lib.R;
import com.aurora.lib.utils.DensityUtil;

/**
 * A dialog that prompts the user for the time of day using a {@link TimePicker}.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-timepicker.html">Time Picker
 * tutorial</a>.</p>
 */
public class AuroraTimePickerDialog extends AuroraDialog
        implements OnTimeChangedListener {

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        void onTimeSet(AuroraTimePicker view, int hourOfDay, int minute);
    }

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private  AuroraTimePicker mTimePicker;
    private final OnTimeSetListener mCallback;
    
    private AuroraTimePickerDialog.OnClickListener mListener;

    int mInitialHourOfDay;
    int mInitialMinute;
    boolean mIs24HourView;

    private TextView mTitle;
    private Button mPositiveButton;
    private Button mNegtiveButton;
    
    private boolean mTitleNeedsUpdate = true;
    private Context mContext;
    public interface OnClickListener{
    	void onClick(AuroraTimePicker view, int hour, int minute);
    }
    
    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public AuroraTimePickerDialog(Context context,
            OnTimeSetListener callBack,
            int hourOfDay, int minute, boolean is24HourView) {
        this(context, 0, callBack, hourOfDay, minute, is24HourView);
        mContext = context;
        mIs24HourView = is24HourView;
    }

    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    private AuroraTimePickerDialog(Context context,
            int theme,
            OnTimeSetListener callBack,
            int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme);
        mCallback = callBack;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourView = is24HourView;
        mContext = context;
       // setIcon(0);
        //setTitle(R.string.aurora_time_picker_dialog_title);

//        Context themeContext = getContext();
//        setButton(BUTTON_POSITIVE, themeContext.getText(R.string.aurora_date_time_done), this);
//
//        LayoutInflater inflater =
//                (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.aurora_time_picker_dialog, null);
//        setView(view);
//        mTimePicker = (AuroraTimePicker) view.findViewById(com.aurora.lib.R.id.aurora_timePicker);
//
//        // initialize state
//        mTimePicker.setIs24HourView(mIs24HourView);
//        mTimePicker.setCurrentHour(mInitialHourOfDay);
//        mTimePicker.setCurrentMinute(mInitialMinute);
//        mTimePicker.setOnTimeChangedListener(this);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	super.onCreate(savedInstanceState);
    	setContentView(com.aurora.lib.R.layout.aurora_time_picker_dialog_new);
    	
    	mTimePicker = (AuroraTimePicker)findViewById(com.aurora.lib.R.id.aurora_time_picker);
    	mTitle = (TextView)findViewById(com.aurora.lib.R.id.aurora_date_picker_title);
    	mPositiveButton = (Button)findViewById(com.aurora.lib.R.id.aurora_date_picker_done_button);
    	mNegtiveButton = (Button)findViewById(com.aurora.lib.R.id.aurora_date_picker_cancel_button);
    	
    	View.OnClickListener clickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(v.getId() == com.aurora.lib.R.id.aurora_date_picker_done_button){
					 if(mListener != null)
                     {
                         mListener.onClick(mTimePicker, mTimePicker.getCurrentHour(),mTimePicker.getCurrentMinute());
                     }
                             
                     if(mCallback != null)
                     {
                         mCallback.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
                     }
                     dismiss();
				}else if(v.getId() ==  com.aurora.lib.R.id.aurora_date_picker_cancel_button){
					 dismiss();
				}
			}
		};
		mPositiveButton.setOnClickListener(clickListener);
		mNegtiveButton.setOnClickListener(clickListener);
//    	mTimePicker = (AuroraTimePicker)findViewById(com.aurora.lib.R.id.aurora_timePicker);

        // initialize state
        mTimePicker.setIs24HourView(mIs24HourView);
        mTimePicker.setCurrentHour(mInitialHourOfDay);
        mTimePicker.setCurrentMinute(mInitialMinute);
        mTimePicker.setOnTimeChangedListener(this);
        updateTitle(mInitialHourOfDay,mInitialMinute);
        WindowManager.LayoutParams attr =  getWindow().getAttributes();
//        attr.width = 313;//DensityUtil.dip2px(mContext, 313.0f);
        attr.gravity = Gravity.CENTER;
        
    }
    
    private void updateTitle(int hourOfDay, int minute){
        int limitLine = 10;
        StringBuilder builder = new StringBuilder();
        if (mTimePicker != null) {
            
            builder.append(getHour(mIs24HourView));
            builder.append(":");
            
            int min = mTimePicker.getCurrentMinute();
            if(min < limitLine){
                builder.append("0"+min); 
            }else{
                builder.append(min+""); 
            }
            setTitles(builder.toString());
            mTitleNeedsUpdate = true;
        } else {
            if (mTitleNeedsUpdate) {
                mTitleNeedsUpdate = false;
            }
        }
    }
    
    private String getHour(boolean is24Hour){
        int halfDay = 12;
        if(is24Hour){
            return mTimePicker.getCurrentHour().toString();
        }else{
           return (mTimePicker.getCurrentHour()%halfDay)+"";
        }
    }
    
    @Override
    public void showInternal(boolean hideView) {
    	// TODO Auto-generated method stub
    	super.showInternal(hideView);
    	
    	View content = mWindow.findViewById(com.aurora.lib.R.id.aurora_time_picker_layout);
    	doTouchOutsideEvents(content, mWindow.getDecorView());
    }
    
   private void setTitles(CharSequence title){
      mTitle.setText(title);
      mTitle.invalidate();
   }
            
            
    public void onClick(DialogInterface dialog, int which) {
        tryNotifyTimeSet();
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minutOfHour);
    }

    public void onTimeChanged(AuroraTimePicker view, int hourOfDay, int minute) {
        /* do nothing */
    	updateTitle(hourOfDay,minute);
    }

    private void tryNotifyTimeSet() {
//        if (mCallback != null) {
//            mTimePicker.clearFocus();
//            mCallback.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(),
//                    mTimePicker.getCurrentMinute());
//        }
    }

    public void setOnClickListener(AuroraTimePickerDialog.OnClickListener listener){
    	mListener = listener;
    }
    
    @Override
    protected void onStop() {
        tryNotifyTimeSet();
        super.onStop();
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mTimePicker.getCurrentHour());
        state.putInt(MINUTE, mTimePicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
    }
}
