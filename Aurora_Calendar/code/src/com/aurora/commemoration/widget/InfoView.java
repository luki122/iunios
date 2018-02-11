package com.aurora.commemoration.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.aurora.calendar.util.TimeUtils;
import com.aurora.commemoration.model.RememberDayInfo;
import com.gionee.calendar.GNCalendarUtils;
import com.gionee.calendar.GNDateTextUtils;
import com.gionee.calendar.view.GNCustomTimeDialog;

import java.util.Date;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraDatePickerDialog;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraEditText;

public class InfoView extends FrameLayout implements OnClickListener {

    private TextView tv_today;
    private TextView tv_title;
    private AuroraEditText et_title;
    private View time_display;
    private View time_picker;
    private TextView time_choose;
    private TextView tv_day;
    private TextView tv_flag;
    private GNDateTextUtils mGNDateTextUtils;
    private int TITIL_MAX_LENGTH = 8;

    public InfoView(Context context) {
        super(context);
    }

    private String chooseDate = "";
    private String tmpChooseDate = "";
    private TitleTW mWatcher = null;
    private CharSequence tmpTitle;
    private CharSequence tmpTimeChoose;
    private int curBytesLength = 0;


    public InfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView();
    }

    public boolean isChanged(RememberDayInfo info) {
        if (chooseDate.length() != info.getDay().length()
                || et_title.getText().length() != info.getTitle().length()) {
            return true;
        }
        int length = chooseDate.length();
        for (int i = 0; i < length; i++ ) {
            if (chooseDate.charAt(i) != info.getDay().charAt(i)) {
                return true;
            }
        }

        length = info.getTitle().length();
        String myTitle = et_title.getText().toString();
        for (int i = 0; i < length; i++ ) {
            if (myTitle.charAt(i) != info.getTitle().charAt(i)) {
                return true;
            }
        }
        return false;
    }

    private void initView() {
        mGNDateTextUtils = new GNDateTextUtils(getContext());
        mGNDateTextUtils.setTime(new Date().getTime());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.remember_day_layout, this);

        tv_today = (TextView) view.findViewById(R.id.today);
        tv_title = (TextView) view.findViewById(R.id.title);
        tv_flag = (TextView) findViewById(R.id.to_from);
        et_title = (AuroraEditText) view.findViewById(R.id.title_edit);
        time_display = view.findViewById(R.id.info);
        time_picker = view.findViewById(R.id.time_picker);
        time_choose = (TextView) view.findViewById(R.id.choose_time);
        setTv_day((TextView) view.findViewById(R.id.day));
        Typeface editDateTypeface = Typeface
                .createFromFile("system/fonts/Roboto-Light.ttf");
        tv_today.setTypeface(editDateTypeface);
        tv_day.setTypeface(editDateTypeface);

        time_picker.setOnClickListener(this);
        et_title.setIsNeedDeleteAll(true);
        et_title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                et_title.setCursorVisible(true);
            }
        });
    }

    public void getEditData(RememberDayInfo info) {
        tmpTitle = et_title.getText();
        tv_title.setText(tmpTitle);
        tv_today.setText(chooseDate);

        info.setTitle(tmpTitle.toString());
        info.setDay(chooseDate);
        tmpTimeChoose = time_choose.getText();
        tmpChooseDate = chooseDate;
        setDays(TimeUtils.getDaysFromNow(tmpTimeChoose.subSequence(0, 10).toString()));
    }

    public boolean needComfirm() {
        if (et_title.getText().length() == 0) {
            return false;
        }
        return true;
    }

    public void initData(boolean newFlag, RememberDayInfo info) {

        if (newFlag) {
            chooseDate = TimeUtils.getStringDateShort();
            tmpChooseDate = chooseDate;
            time_choose.setText(TimeUtils.getStringDateWithWeek(System.currentTimeMillis()));
            tv_today.setText(TimeUtils.getStringDateShort());
            tmpTitle = "";
            setDays(0);
        } else {
            setTitle(info.getTitle());
            chooseDate = info.getDay();
            tmpChooseDate = chooseDate;
            tmpTitle = info.getTitle();
            time_choose.setText(TimeUtils.getWeekFromDay(info.getDay()));
            tv_today.setText(info.getDay());
            setDays(TimeUtils.getDaysFromNow(info.getDay()));
        }

        tmpTimeChoose = time_choose.getText();
    }

    public void setEditMode(boolean isEdit, boolean needAnimation, boolean newFlag) {
        Animation aniDisappear = AnimationUtils.loadAnimation(
                this.getContext(), R.anim.alpha_out);
        Animation aniAppear = AnimationUtils.loadAnimation(this.getContext(),
                R.anim.alpha_in);
        et_title.setCursorVisible(false);
        if (isEdit) {
            if (needAnimation) {
                tv_today.startAnimation(aniDisappear);
                tv_title.startAnimation(aniDisappear);
                time_display.startAnimation(AnimationUtils.loadAnimation(
                        this.getContext(), R.anim.alpha_down));
            }

            tv_today.setVisibility(View.INVISIBLE);
            tv_title.setVisibility(View.GONE);
            time_display.setVisibility(View.GONE);

            et_title.setVisibility(View.VISIBLE);
            if (!newFlag) {
                et_title.setText(tv_title.getText());
                if (Utils.isChinese(et_title.getText().toString())) {
                    TITIL_MAX_LENGTH = 8;
                } else {
                    TITIL_MAX_LENGTH = 16;
                }
                curBytesLength = et_title.getText().toString().length();
            }
            mWatcher = new TitleTW();
            et_title.addTextChangedListener(mWatcher);
            time_picker.setVisibility(View.VISIBLE);
            time_choose.setText(tmpTimeChoose);
            chooseDate = tmpChooseDate;
        } else {
            if (needAnimation) {
                tv_today.startAnimation(aniAppear);
                tv_title.startAnimation(aniAppear);
                time_display.startAnimation(AnimationUtils.loadAnimation(
                        this.getContext(), R.anim.alpha_up));
            }
            tv_today.setVisibility(View.VISIBLE);
            tv_title.setVisibility(View.VISIBLE);
            time_display.setVisibility(View.VISIBLE);
            et_title.setVisibility(View.GONE);
            time_picker.setVisibility(View.GONE);
            et_title.removeTextChangedListener(mWatcher);
        }
    }

    @Override
    public void onClick(View arg0) {
        showDialog();
    }

    public TextView getTv_day() {
        return tv_day;
    }

    public void setTv_day(TextView tv_day) {
        this.tv_day = tv_day;
        this.tv_day.setIncludeFontPadding(false);
    }

    public void setDays(int days) {
        if (days >= 0) {
            tv_flag.setText(R.string.aurora_remember_future);
            tv_day.setText("" + days);
        } else {
            tv_flag.setText(R.string.aurora_remember_past);
            tv_day.setText("" + -days);
        }
    }

    public void setTitle(String title) {
        tv_title.setText(title);
        et_title.setText(title);
    }

    public String[] updateDateYearMonthDay(String dateText) {
        dateText = dateText.substring(0, 10);
        return dateText.split("-");
    }

    private void showDialog() {
        Dialog dialog = null;
        String[] curentShowDate = updateDateYearMonthDay(chooseDate);
        int year = Integer.parseInt(curentShowDate[0]);
        int month = Integer.parseInt(curentShowDate[1]);
        int day = Integer.parseInt(curentShowDate[2]);
        dialog = new GNCustomTimeDialog(getContext(),
                AuroraAlertDialog.THEME_AMIGO_FULLSCREEN, onDateSetListener,
                year, month - 1, day);
        dialog.show();
    }

    AuroraDatePickerDialog.OnDateSetListener onDateSetListener = new AuroraDatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(AuroraDatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            if (year < GNCalendarUtils.MIN_YEAR_NUM
                    || year > GNCalendarUtils.MAX_YEAR_NUM) {
                AllInOneActivity
                        .showOutOfRangeToast(R.string.time_out_of_range);
                return;
            }
            String month, day;
            if (monthOfYear < 9) {
                month = "0" + (monthOfYear + 1);
            } else {
                month = String.valueOf(monthOfYear + 1);
            }

            if (dayOfMonth <= 9) {
                day = "0" + (dayOfMonth);
            } else {
                day = String.valueOf(dayOfMonth);
            }

            chooseDate = year + "-" + month + "-" + day;
            time_choose.setText(TimeUtils.getWeekFromDay(chooseDate));
        }
    };

    public class TitleTW implements TextWatcher {
        private int curIndex = 0;
        private int changeLength = 0;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 0) {
                curBytesLength = s.length();
                changeLength = count;
            } else if (before > 0) {
                curBytesLength = s.length();
                changeLength = before;
            }
            curIndex = start;
            if (Utils.isChinese(s.toString())) {
                TITIL_MAX_LENGTH = 8;
            } else {
                TITIL_MAX_LENGTH = 16;
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (curBytesLength > TITIL_MAX_LENGTH) {
                if (curIndex == 0) {
                    editable.delete(curIndex, curIndex + changeLength);
                } else {
                    int deleteL = curBytesLength - TITIL_MAX_LENGTH;
                    editable.delete(curIndex + changeLength - deleteL, curIndex + changeLength);
                    curBytesLength = editable.toString().length();
                }
            }
            if (Utils.isChinese(editable.toString())) {
                TITIL_MAX_LENGTH = 8;
            } else {
                TITIL_MAX_LENGTH = 16;
            }
        }
    }
}
