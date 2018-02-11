package aurora.lib.app;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.lib.widget.AuroraDatePicker;
import aurora.lib.widget.AuroraDatePicker.OnDateChangedListener;

import com.aurora.lib.R;
import com.aurora.lib.utils.DensityUtil;

public class AuroraDatePickerDialog extends AuroraDialog implements
        OnDateChangedListener {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private  AuroraDatePicker mDatePicker;
    private final OnDateSetListener mCallBack;
    private final Calendar mCalendar;
    private Context mContext;
    
    private AuroraDatePickerDialog.OnClickListener mListener;

    private TextView mTitle;
    private Button mPositiveButton;
    private Button mNegtiveButton;
    
    private ImageView mGreyBackground;
    
    private int year;
    private int monthOfYear;
    private  int dayOfMonth ;
    
    private boolean mTitleNeedsUpdate = true;
    
    private boolean mCancelOnTouch = true;

    
    private int mMaxYear = 2036;
    private int mMaxYearMonth = 12;
    private int mMaxYearDay = 31;
    
    private int mMinYear = 1970;
    private int mMinYearMonth = 1;
    private int mMinYearDay = 1;
    
    private Calendar mMinDate;
    private Calendar mMaxDate;
    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *  with {@link java.util.Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        void onDateSet(AuroraDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    public interface OnClickListener{
    	void onClick(AuroraDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }
    /**
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     */
    public AuroraDatePickerDialog(Context context,
            OnDateSetListener callBack,
            int year,
            int monthOfYear,
            int dayOfMonth) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth);
    }

    /**
     * @param context The context the dialog is to run in.
     * @param theme the theme to apply to this dialog
     * @param callBack How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     */
    private AuroraDatePickerDialog(Context context,
            int theme,
            OnDateSetListener callBack,
            int year,
            int monthOfYear,
            int dayOfMonth) {
        super(context, theme);

        mContext = getContext();
        
        mCallBack = callBack;

        mCalendar = Calendar.getInstance();

        Context themeContext = getContext();
//        setButton(BUTTON_POSITIVE, themeContext.getText(R.string.aurora_date_time_done), this);
//        setIcon(0);
        this.year = year;
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        
//        mMinDate = Calendar.getInstance();
//        mMinDate.set(mMinYear, mMinYearMonth-1, mMinYearDay);
//        mMaxDate = Calendar.getInstance();
//        mMaxDate.set(mMaxYear, mMaxYearMonth-1, mMaxYearDay);
        
//        LayoutInflater inflater =
//                (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.aurora_date_picker_dialog, null);
//        setView(view);
//        mDatePicker = (AuroraDatePicker) view.findViewById(com.aurora.lib.R.id.aurora_datePicker);
//        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
//        updateTitle(year, monthOfYear, dayOfMonth);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	super.onCreate(savedInstanceState);
    	setContentView(com.aurora.lib.R.layout.aurora_date_picker_dialog_new);
    	
    	mDatePicker = (AuroraDatePicker)findViewById(com.aurora.lib.R.id.aurora_date_picker);
    	mTitle = (TextView)findViewById(com.aurora.lib.R.id.aurora_date_picker_title);
    	mPositiveButton = (Button)findViewById(com.aurora.lib.R.id.aurora_date_picker_done_button);
    	mNegtiveButton = (Button)findViewById(com.aurora.lib.R.id.aurora_date_picker_cancel_button);
    	
    	mGreyBackground = (ImageView)findViewById(com.aurora.lib.R.id.aurora_date_picker_dialog_grey_bg);
//    	mGreyBackground.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				if(mCancelOnTouch){
//					dismiss();
//				}
//				return false;
//			}
//		});
    	View.OnClickListener clickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(v.getId() == com.aurora.lib.R.id.aurora_date_picker_done_button){
//					AuroraDatePickerDialog.
					if(mListener!=null){
						mListener.onClick(mDatePicker, mDatePicker.getYear(), mDatePicker.getMonth()+1, mDatePicker.getDayOfMonth());
					}
		                   
					tryNotifyDateSet();
					dismiss();
				}else if(v.getId() == com.aurora.lib.R.id.aurora_date_picker_cancel_button){
					dismiss();
				}
			}
		};
		mPositiveButton.setOnClickListener(clickListener);
		mNegtiveButton.setOnClickListener(clickListener);
		
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        updateTitle(year, monthOfYear, dayOfMonth);
       WindowManager.LayoutParams attr =  getWindow().getAttributes();
//       attr.width = 313;//DensityUtil.dip2px(mContext, 313.0f);
       attr.gravity = Gravity.CENTER;
       //WindowManager.LayoutParams.WRAP_CONTENT;
//       setMinDate(mMinDate.getTimeInMillis());
//       setMaxDate(mMaxDate.getTimeInMillis());
    }
    
    public void setMinDate(long minDate){
        if(mDatePicker != null){
            mDatePicker.setMinDate(minDate);
        }
    }
    
    @Override
    public void showInternal(boolean hideView) {
    	// TODO Auto-generated method stub
    	super.showInternal(hideView);
    	
    	View content = mWindow.findViewById(R.id.aurora_date_picker_layout);
    	doTouchOutsideEvents(content, mWindow.getDecorView());
    }
    
    
    public void setMaxDate(long maxDate){
        if(mDatePicker != null){
            mDatePicker.setMaxDate(maxDate);
        }
    }
//    @Override
//    public void setCanceledOnTouchOutside(boolean cancel) {
//    	mCancelOnTouch = cancel;
//    }
    
    public void setOnClickListener(AuroraDatePickerDialog.OnClickListener listener){
    	mListener = listener;
    }
    
    public void onClick(DialogInterface dialog, int which) {
        //tryNotifyDateSet();
    }

    public void onDateChanged(AuroraDatePicker view, int year,
            int month, int day) {
        mDatePicker.init(year, month, day, this);
        updateTitle(year, month, day);
    }

    /**
     * Gets the {@link DatePicker} contained in this dialog.
     *
     * @return The calendar view.
     */
    public AuroraDatePicker getDatePicker() {
        return mDatePicker;
    }

    
    
    
    /**
     * Sets the current date.
     *
     * @param year The date year.
     * @param monthOfYear The date month.
     * @param dayOfMonth The date day of month.
     */
    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    private void tryNotifyDateSet() {
        if (mCallBack != null) {
            mDatePicker.clearFocus();
            mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                    mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        }
    }

    @Override
    protected void onStop() {
        //tryNotifyDateSet();
        super.onStop();
    }

    private String reoderTitle(){
        char[] order = DateFormat.getDateFormatOrder(getContext());
        StringBuilder b = new StringBuilder();
        int dayLenght = 0;
        int monthLenght = 0;
        int yearLenght = 0;
        final int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case DateFormat.DATE:
                   b.append(mDatePicker.getDayOfMonth()+"-");
                    break;
                case DateFormat.MONTH:
                    b.append(mDatePicker.getMonth()+1+"-");
                    break;
                case DateFormat.YEAR:
                    b.append(mDatePicker.getYear()+"-");
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        if(b.charAt(b.length()-1) == '-'){
            b.replace(b.length()-1, b.length(), "");
        }
        
        return b.toString();
    }
    
    private void updateTitle(int year, int month, int day) {
        if (!mDatePicker.getCalendarViewShown()) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, day);
            String title = DateUtils.formatDateTime(mContext,
                    mCalendar.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_SHOW_YEAR
                    | DateUtils.FORMAT_ABBREV_MONTH);
            setTitles(reoderTitle());
            mTitleNeedsUpdate = true;
        } else {
            if (mTitleNeedsUpdate) {
                mTitleNeedsUpdate = false;
                setTitles(R.string.aurora_date_picker_dialog_title);
            }
        }
    }

    private void setTitles(CharSequence title){
    	mTitle.setText(title);
    	mTitle.invalidate();
    }
    private void setTitles(int title){
    	mTitle.setText(title);
    	mTitle.invalidate();
    }
    
    
    
    
    

    
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, this);
    }
}
