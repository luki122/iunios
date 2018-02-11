package com.aurora.commemoration.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.calendar.R;
import com.android.calendar.wxapi.WXEntryActivity;
import com.aurora.calendar.util.Globals;
import com.aurora.commemoration.adapter.RememberDayAdapter;
import com.aurora.commemoration.alarm.CalendarAlarmReceiver;
import com.aurora.commemoration.db.RememberDayDao;
import com.aurora.commemoration.model.RememberDayInfo;

import java.util.ArrayList;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraMenuBase;

/**
 * Created by joy on 4/27/15.
 */
public class RememberDayListActivity extends AuroraActivity {

    public AuroraListView mListView;

    private Context mContext;
    private AuroraActionBar mActionBar;
    private TextView leftView;
    private TextView rightView;
    private LinearLayout topLy;

    private RememberDayAdapter mAdapter;
    private RememberDayDao mDayDao;
    public List<RememberDayInfo> rememberDayList;
    private boolean editMode = false;
    private boolean isSelectAll = false;

    private Handler handler = new Handler() {

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAuroraContentView(R.layout.remember_day_list, AuroraActionBar.Type.Normal, false);
        mContext = this;

        initActionBar();
        initViews();
        initdata();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataFromDB();
        mAdapter.notifyForChanged(rememberDayList);
    }

    private void initViews() {
        topLy = (LinearLayout) findViewById(R.id.no_item_ly);
        mListView = (AuroraListView) findViewById(R.id.remember_list);
        mListView.auroraSetNeedSlideDelete(false);
        mListView.setLongClickable(true);
        mListView.auroraEnableSelector(true);
        mListView.setSelector(R.drawable.list_item_selector);
        mListView.setFocusable(true);
    }

    private void initActionBar() {
        mActionBar = getAuroraActionBar();
        mActionBar.setTitle(R.string.remember_title);
        setAuroraSystemMenuCallBack(new AuroraMenuBase.OnAuroraMenuItemClickListener() {
            @Override
            public void auroraMenuItemClick(int paramInt) {
                switch (paramInt) {
                    case R.id.delete:
                        showSelectDeleteDialog();
                        break;
                }
            }
        });
        mActionBar.initActionBottomBarMenu(R.menu.menu_remember_day, 1);
        mActionBar.addItem(aurora.widget.AuroraActionBarItem.Type.Add);
        mActionBar.setOnAuroraActionBarListener(
                new AuroraActionBar.OnAuroraActionBarItemClickListener() {

                    @Override
                    public void onAuroraActionBarItemClicked(int i) {
                        Intent intent = new Intent(RememberDayListActivity.this, WXEntryActivity.class);
                        intent.putExtra("url", Globals.FILE_PROTOCOL + "/storage/emulated/legacy/1.jpg");
                        intent.putExtra("new", true);
                        mContext.startActivity(intent);
                    }
                });
    }

    private void setListener() {
        mListView.auroraSetDeleteItemListener(new AuroraListView.AuroraDeleteItemListener() {
            @Override
            public void auroraDeleteItem(View view, int position) {
                mAdapter.setmDeleteFlag(true);

                rememberDayList.remove(position);
                mAdapter.notifyDataSetChanged();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (editMode) {
                    if (!mAdapter.getSelectSet().contains(
                            rememberDayList.get(position).getId())) {
                        mAdapter.getSelectSet().add(
                                rememberDayList.get(position).getId());
                        AuroraCheckBox cb = (AuroraCheckBox) view
                                .findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                        cb.auroraSetChecked(true, true);
                    } else {
                        mAdapter.getSelectSet().remove(
                                rememberDayList.get(position).getId());

                        AuroraCheckBox cb = (AuroraCheckBox) view
                                .findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                        cb.auroraSetChecked(false, true);
                    }
                    checkSelect();
                } else {
                    Intent intent = new Intent(mContext, WXEntryActivity.class);
                    intent.putExtra("new", false);
                    intent.putExtra("index", position);
                    mContext.startActivity(intent);
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                AuroraCheckBox cb = (AuroraCheckBox) view
                        .findViewById(com.aurora.R.id.aurora_list_left_checkbox);
                cb.setChecked(true);
                cb.postInvalidate();

                mAdapter.getSelectSet()
                        .add(rememberDayList.get(position).getId());
                if (!editMode) {
                    openEditMode();
                }

                return false;
            }
        });
        mListView.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {
            @Override
            public void auroraOnClick(int i) {
                mListView.auroraDeleteSelectedItemAnim();
            }

            @Override
            public void auroraPrepareDraged(int i) {
                mAdapter.setmDeleteFlag(false);
            }

            @Override
            public void auroraDragedSuccess(int i) {

            }

            @Override
            public void auroraDragedUnSuccess(int i) {

            }
        });

    }

    private void showSelectDeleteDialog() {
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setTitle(getString(R.string.reminder));
        builder.setMessage(R.string.aurora_remember_delete_message);
        builder.setPositiveButton(getString(R.string.aurora_comfirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        menuDelete();
                    }
                });
        builder.setNegativeButton(getString(R.string.aurora_cancle), null);
        builder.create().show();
    }

    private void openEditMode() {
        editMode = true;

        //mListView.auroraSetNeedSlideDelete(false);
        mListView.setLongClickable(false);
        mListView.auroraEnableSelector(false);

        mActionBar.setShowBottomBarMenu(true);
        mActionBar.showActionBarDashBoard();

        leftView = (TextView) mActionBar.getSelectLeftButton();
        rightView = (TextView) mActionBar.getSelectRightButton();
        leftView.setTextColor(getResources().getColor(R.color.item_word_big));
        rightView.setTextColor(getResources().getColor(R.color.item_word_big));

        rightView.setText(getString(R.string.aurora_remember_choose));
        leftView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (editMode) {
                    closeEditMode();
                }
            }
        });

        rightView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isSelectAll) {
                    mAdapter.getSelectSet().clear();
                    mAdapter.notifyDataSetChanged();
                } else {
                    for (RememberDayInfo bean : rememberDayList) {
                        mAdapter.getSelectSet().add(bean.getId());
                    }
                    mAdapter.notifyDataSetChanged();
                }
                checkSelect();
            }
        });

        mAdapter.setEditMode(true);
        mAdapter.setNeedAnim(true);
        mAdapter.notifyDataSetChanged();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.setNeedAnim(false);
            }
        }, 1);

        checkSelect();
    }

    private void closeEditMode() {
        editMode = false;
        //mListView.auroraSetNeedSlideDelete(true);
        mListView.setLongClickable(true);
        mListView.auroraEnableSelector(true);

        mActionBar.setShowBottomBarMenu(false);
        mActionBar.showActionBarDashBoard();

        for (int i = 0; i < mListView.getChildCount(); i++) {
            AuroraCheckBox cb = (AuroraCheckBox) mListView.getChildAt(i)
                    .findViewById(com.aurora.R.id.aurora_list_left_checkbox);
            cb.setChecked(false);
            cb.postInvalidate();
        }

        mAdapter.getSelectSet().clear();
        mAdapter.setEditMode(false);
        mAdapter.setNeedAnim(true);
        mAdapter.notifyDataSetChanged();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.setNeedAnim(false);
            }
        }, 1);
    }

    private void checkSelect() {
        int allCount = rememberDayList.size();

        if (mAdapter.getSelectSet().size() == allCount) {
            isSelectAll = true;
        } else {
            isSelectAll = false;
        }
        if (isSelectAll) {
            rightView.setText(getString(R.string.aurora_remember_choose_reverse));
        } else {
            rightView.setText(getString(R.string.aurora_remember_choose));
        }
        if (mAdapter.getSelectSet().size() == 0) {
            mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(
                    1, false);
        } else {
            mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(
                    1, true);
        }
    }

    private void initdata() {
        mDayDao = new RememberDayDao(RememberDayListActivity.this);
        rememberDayList = new ArrayList<RememberDayInfo>();
        getDataFromDB();
        //fakeData();

        mAdapter = new RememberDayAdapter(this, rememberDayList, mListView);
        mListView.setAdapter(mAdapter);
    }

    public void menuDelete() {
        RememberDayDao dao = new RememberDayDao(RememberDayListActivity.this);
        for (int i : mAdapter.getSelectSet()) {
            for (RememberDayInfo bean : rememberDayList) {
                if (bean.getId() == i) {
                    rememberDayList.remove(bean);
                    dao.deleteRememberDay(i);
                    CalendarAlarmReceiver.scheduleAlarmById(bean.getId(), AddOrEditReminderActivity.MODE_DEL_REMINDER, bean.getReminderData());
                    break;
                }
            }
            mAdapter.notifyForChanged(rememberDayList);
        }
        dao.closeDatabase();
        closeEditMode();
        isEmpty();
        //mAdapter.notifyForChanged(rememberDayList);
    }

    private void getDataFromDB() {
        RememberDayDao dao = new RememberDayDao(RememberDayListActivity.this);
        rememberDayList = dao.getRememberDayList();
        dao.closeDatabase();
        isEmpty();
    }

    private void isEmpty() {
        if (rememberDayList.size() == 0) {
            topLy.setVisibility(View.VISIBLE);
        } else {
            topLy.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mActionBar.auroraIsExitEditModeAnimRunning()
                    || mActionBar.auroraIsEntryEditModeAnimRunning()) {
                return true;
            }
            if (editMode) {
                closeEditMode();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}