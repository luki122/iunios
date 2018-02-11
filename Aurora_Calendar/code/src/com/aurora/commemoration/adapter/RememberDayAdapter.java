package com.aurora.commemoration.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.calendar.R;
import com.aurora.calendar.util.TimeUtils;
import com.aurora.commemoration.activity.RememberDayListActivity;
import com.aurora.commemoration.model.RememberDayInfo;
import com.gionee.calendar.day.DayUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

/**
 * Created by joy on 4/28/15.
 */
public class RememberDayAdapter extends BaseAdapter {

    private static final int TYPE_PAST = 0;
    private static final int TYPE_FUTURE = 1;
    private final int mTotalHeight;

    private RememberDayListActivity mContext;
    private AuroraListView mListView;
    private List<RememberDayInfo> mList;
    private LayoutInflater mInflater;
    private long curTime;
    private boolean deleteFlag = false;
    private Set<Integer> selectSet;
    private boolean editMode = false;
    private boolean isNeedAnim = false;

    public RememberDayAdapter(RememberDayListActivity activity,
                              List<RememberDayInfo> list, AuroraListView mListView) {
        mContext = activity;
        this.mListView = mListView;
        mList = list;
        mInflater = LayoutInflater.from(activity);

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = sDateFormat.format(new Date());
        try {
            curTime = sDateFormat.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mTotalHeight = Math.round(mContext.getResources().getDimension(
                R.dimen.remember_day_item_height));
        selectSet = new HashSet<Integer>();
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RememberDayInfo bean = mList.get(position);
        final int type = bean.getFutureFlag() ? TYPE_FUTURE : TYPE_PAST;
        final RememberDayHolder mHolder;
        int offDay = 0;

        if (convertView == null) {
            convertView = mInflater.inflate(
                    com.aurora.R.layout.aurora_slid_listview, null);
            RelativeLayout front = (RelativeLayout) convertView
                    .findViewById(com.aurora.R.id.aurora_listview_front);
            mInflater.inflate(R.layout.item_remember_day, front);
            mHolder = createHolder(convertView);
            convertView.setTag(mHolder);
        } else {
            mHolder = (RememberDayHolder) convertView.getTag();
        }

        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        mHolder.rightOccur.setVisibility(View.INVISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mListView.auroraIsRubbishOut()) {
                            mHolder.rightOccur.setVisibility(View.INVISIBLE);
                        } else {
                            mHolder.rightOccur.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                return false;
            }
        });

        if (editMode) {
            mHolder.controlPad.setPadding(DayUtils.dip2px(mContext, 17), 0, 0, 0);
        } else {
            mHolder.controlPad.setPadding(0, 0, 0, 0);
        }

        checkCheck(bean, mHolder);
        checkShowCheckBox(bean, mHolder);

        offDay = TimeUtils.getDaysFromNow(bean.getDay());
        mHolder.titleTv.setText(bean.getTitle());

        if (offDay < 0) {
            mHolder.dayLy.setBackgroundResource(R.color.item_blue);
            mHolder.leftColorView.setBackgroundResource(R.color.item_blue);
            mHolder.flagTv.setText(R.string.aurora_remember_past);
            mHolder.dayTv.setText(String.valueOf(-offDay));
        } else {
            mHolder.dayLy.setBackgroundResource(R.color.item_pink);
            mHolder.leftColorView.setBackgroundResource(R.color.item_pink);
            mHolder.flagTv.setText(R.string.aurora_remember_future);
            mHolder.dayTv.setText(String.valueOf(offDay));
        }

        if (deleteFlag) {
            resetListItemHeight(convertView);
        }

        return convertView;
    }

    private void checkCheck(RememberDayInfo bean, RememberDayHolder holder) {
        if (selectSet.contains(bean.getId())) {
            holder.cb.auroraSetChecked(true, isNeedAnim);
        } else {
            holder.cb.auroraSetChecked(false, isNeedAnim);
        }
    }

    private void checkShowCheckBox(RememberDayInfo bean, RememberDayHolder holder) {
        if (editMode) {
            if (isNeedAnim) {
                AuroraListView.auroraStartCheckBoxAppearingAnim(
                        holder.front, holder.cb);
            } else {
                AuroraListView.auroraSetCheckBoxVisible(holder.front,
                        holder.cb, true);
            }
        } else {
            if (isNeedAnim) {
                AuroraListView.auroraStartCheckBoxDisappearingAnim(
                        holder.front, holder.cb);
            } else {
                if (!mContext.mListView.auroraIsRubbishOut()) {
                    AuroraListView.auroraSetCheckBoxVisible(holder.front,
                            holder.cb, false);
                }
            }
        }
    }

    private void resetListItemHeight(View convertView) {
        Object convertTag = convertView.getTag();
        if (null == convertTag || !(convertTag instanceof RememberDayHolder)) {
            return;
        }

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) convertView
                .getLayoutParams();
        ViewGroup.LayoutParams lpFront = (ViewGroup.LayoutParams) convertView
                .findViewById(com.aurora.R.id.aurora_listview_front)
                .getLayoutParams();
        LinearLayout.LayoutParams lpRubbish = (LinearLayout.LayoutParams) convertView
                .findViewById(com.aurora.R.id.aurora_listview_back)
                .getLayoutParams();

        if (null != lp && null != lpFront && null != lpRubbish) {
            lp.height = mTotalHeight + 1;
            lpFront.height = mTotalHeight;
            lpRubbish.height = mTotalHeight;
            convertView.findViewById(com.aurora.R.id.content).setAlpha(255);
        }
    }

    private RememberDayHolder createHolder(View convertView) {
        RememberDayHolder holder = new RememberDayHolder();
        holder.leftColorView = (TextView) convertView.findViewById(R.id.leftColor);
        holder.titleTv = (TextView) convertView.findViewById(R.id.itemTitle);
        holder.flagTv = (TextView) convertView.findViewById(R.id.itemHint);
        holder.dayTv = (TextView) convertView.findViewById(R.id.dayHint);
        holder.dayLy = (LinearLayout) convertView.findViewById(R.id.dayTime);
        holder.rightOccur = convertView.findViewById(R.id.right_occu);
        holder.cb = (AuroraCheckBox) convertView
                .findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        holder.front = (RelativeLayout) convertView
                .findViewById(com.aurora.R.id.aurora_listview_front);
        holder.controlPad = (RelativeLayout) convertView
                .findViewById(com.aurora.R.id.control_padding);

        return holder;
    }

    public void notifyForChanged(List<RememberDayInfo> data) {
        this.mList.clear();
        this.mList.addAll(data);
        notifyDataSetChanged();
    }

    public void setmDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Set<Integer> getSelectSet() {
        return selectSet;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void setNeedAnim(boolean isNeedAnim) {
        this.isNeedAnim = isNeedAnim;
    }

    static class RememberDayHolder {
        TextView leftColorView;
        TextView titleTv;
        TextView flagTv;
        TextView dayTv;
        LinearLayout dayLy;
        View rightOccur;
        AuroraCheckBox cb;
        RelativeLayout front;
        RelativeLayout controlPad;
    }

}
