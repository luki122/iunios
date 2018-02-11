
package com.aurora.note.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraMultipleChoiceListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraMenuAdapterBase;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;

import com.aurora.utils.DensityUtil;
import com.aurora.note.NoteApp;
import com.aurora.note.R;
import com.aurora.note.activity.picbrowser.PictureViewActivity;
import com.aurora.note.activity.record.PlayActivity2;
import com.aurora.note.activity.record.RecordActivity2;
import com.aurora.note.alarm.NoteAlarmManager;
import com.aurora.note.alarm.NoteAlarmReceiver;
import com.aurora.note.bean.LabelResult;
import com.aurora.note.bean.NoteResult;
import com.aurora.note.crop.Crop;
import com.aurora.note.db.LabelAdapter;
import com.aurora.note.db.NoteAdapter;
import com.aurora.note.report.ReportCommand;
import com.aurora.note.report.ReportUtil;
import com.aurora.note.ui.AlignRightTextView;
import com.aurora.note.ui.ChatEmotion;
import com.aurora.note.ui.CopyNoSpaceEditText;
import com.aurora.note.ui.CopyNoSpaceEditText.OnCursorLineChangedListener;
import com.aurora.note.ui.CopyNoSpaceEditText.OnNoteImageSpanClickListener;
import com.aurora.note.ui.CopyNoSpaceEditText.OnNoteImageSpanLongClickListener;
import com.aurora.note.ui.NoteProgressDialog;
import com.aurora.note.ui.NoteProgressDialog2;
import com.aurora.note.util.BooleanPerfencesUtil;
import com.aurora.note.util.FileLog;
import com.aurora.note.util.Globals;
import com.aurora.note.util.Log;
import com.aurora.note.util.SDcardManager;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.ToastUtil;
import com.aurora.note.widget.NewNoteRelativeLayout;
import com.aurora.note.widget.NewNoteRelativeLayout.OnSizeChangedListener;
import com.aurora.note.widget.NoteImageGroupSpan;
import com.aurora.note.widget.NoteImageSpan;
import com.aurora.note.widget.NoteImageSpan.Type;
import com.aurora.note.widget.NoteImageSpanBitmapCache;
import com.aurora.note.widget.NoteSoundSpan;
import com.aurora.note.wxapi.WXEntryActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class NewNoteActivity extends AuroraActivity implements OnClickListener {
    private static final String TAG = "NewNoteActivity";

//    private static final int AURORA_MORE = 1;
//    private static final int AURORA_GEN_PICTURE = 2;

    public static final String TYPE_GET_DATA = "note_type";
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_OBJ = "note_obj";

    private CopyNoSpaceEditText content;
    private LinearLayout ly_menu;
    private static final int[] BULLET_MENU_SRC_RES = {
            R.drawable.new_note_menu_no_bullet_list_selector,
            R.drawable.new_note_menu_bullet_list_selector,
            R.drawable.new_note_menu_bullet_list_with_indentation_selector
    };

    private static final int SIGN_NO_BULLET = 0;
    private static final int SIGN_NORMAL_BULLET = 1;
    private static final int SIGN_INDENT_BULLET = 2;
    private int sign = SIGN_NO_BULLET;

    private boolean hasMark = false;

    // 0-项目编码 1-图片 2-视频 3-音频
    private int type = 0;

    // 数据库处理
    private NoteAdapter noteDb;
    private NoteResult m_result;
    // 标签处理
    private Context mContext;
    private LabelAdapter mLabelDb;
    private ArrayList<LabelResult> mLabelList;
    private String[] mSelectedLabels = new String[] {
            "", ""
    };
    private AuroraAlertDialog mAlertDialog;
    // 相机拍摄的当前的照片文件
    private File mCurrentPhotoFile;

    private File mCurrentVideoFile;
    private long warningtime = 0;
    private NoteResult m_mainResult = null;
    // 0 : 内部跳转 1：外部跳转
    private int note_tye = 0;
    private View mReminderContainer;
    private TextView mReminderTv;
    private View mNoteLabelContainer;
    private TextView mLabel1Tv;
    private TextView mLabelSepTv;
    private TextView mLabel2Tv;
    private View mRecordMenu;
    private ImageView mBulletMenuIv;
    private ImageView mMarkMenu;

    private TextView mTitleTv;
    private View mGenPic;
    private View mSetPaper;

    private NewNoteRelativeLayout mRootView;

    private String mRecordDuration; // 录音时长

    private int mLastAuroraMenuResId; // 上次使用的菜单资源

    private String mLabel1;
    private String mLabel2;
    private String del_type = "0";

    private String mBackgroudPath;

    private static final int[] PAPER_RESOURCE_IDS = {
        R.drawable.note_paper_01,
        R.drawable.note_paper_02,
        R.drawable.note_paper_03,
        R.drawable.note_paper_04,
        R.drawable.note_paper_05,
        R.drawable.note_paper_06
    };

    private static final int[] PAPER_SIGN_RESOURCE_IDS = {
        R.drawable.note_paper_sign_01,
        R.drawable.note_paper_sign_02,
        R.drawable.note_paper_sign_03,
        R.drawable.note_paper_sign_04,
        R.drawable.note_paper_sign_05,
        R.drawable.note_paper_sign_06
    };

    private static final int[] PAPER_NO_SIGN_RESOURCE_IDS = {
        R.drawable.note_paper_no_sign_01,
        R.drawable.note_paper_02,
        R.drawable.note_paper_03,
        R.drawable.note_paper_04,
        R.drawable.note_paper_05,
        R.drawable.note_paper_06
    };

    private static final String BULLET_TEXT = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_SIGN
            + Globals.SIGN_NOINDENT_ID + Globals.ATTACHMENT_END;
    private static final String BULLET_INDENT_TEXT = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_SIGN
            + Globals.SIGN_INDENT_ID + Globals.ATTACHMENT_END;
    private static final String MARK_UNCHECKED_TEXT = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_SIGN
            + Globals.SIGN_UNCHECKED_ID + Globals.ATTACHMENT_END;
    private static final String MARK_CHECKED_TEXT = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_SIGN
            + Globals.SIGN_CHECKED_ID + Globals.ATTACHMENT_END;

    private OriginalNoteInfo mOriginalNoteInfo;
    private boolean isDelFinish = true;
    // private static Bitmap mBitmap = null;

    private String selectedType;
    private NoteImageSpan selectedSpan;
    private int spanEndIndex;
    private int spanCount;
    private int spanIndex;
    private String spanSource;

    private Runnable mPendingRunnable;
    private InputMethodManager mInputMethodManager;
    // 加载数据等待提示框
    private NoteProgressDialog dialog = null;
    private NewNoteHandler mNewNoteHandler = new NewNoteHandler(this);
    private SpannableString con_text;

    private boolean mIsInputMethodWindowShown = false;
    private static int maxLength = 30000;

    private NoteSoundSpan mLastPlayingSoundSpan = null;
    private int mContentPaddingBottom;

//    private static int mImageRightExtraSpace = -1;

    public static final String EXTRA_KEY_COME_FROM_QUICK_RECORD = "come_from_quick_record";
    private boolean mIsComeFromQuickRecord = false;
    private boolean mIsChanged = false;

    private static HandlerThread sSaveThread = null;
    private static Handler sSaveHandler = null;
    private static final long SAVE_NOTE_DELAY = 10000; // 每隔10s保存一次备忘录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setAuroraContentView(R.layout.new_note, AuroraActionBar.Type.Empty);
        initDB();
        Log.i(TAG, "New oncreate");
        getData();
        initActionBar();
        initViews();
        setAuroraMenuListener();
        initAuroraMenu();
        setListener();

        initTextData();
        Log.i(TAG, "New oncreate the currenttime2 =" + System.currentTimeMillis());
        handleQuickRecord();
        initSaveThread();
    }

    private void initSaveThread() {
        if (sSaveThread == null) {
            sSaveThread = new HandlerThread("save-note");
            sSaveThread.start();
            sSaveHandler = new Handler(sSaveThread.getLooper());
            sSaveHandler.postDelayed(mSaveNoteRunnable, SAVE_NOTE_DELAY);
        }
    }
    
    private void closeSaveThread() {
        if (sSaveHandler != null) {
            sSaveHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (sSaveThread != null) {
                        sSaveThread.quit();
                        sSaveThread = null;
                        sSaveHandler = null;
                    }
                }
            });
        }
    }
    
    private Runnable mSaveNoteRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                saveData();
                sSaveHandler.postDelayed(this, SAVE_NOTE_DELAY);
            } catch (Throwable t) {
                Log.e(TAG, "Save note exception: ", t);
            }
        }
    };
    
    private void handleQuickRecord() {
        if (mIsComeFromQuickRecord) {
            mSoundMenuClickRunnable.run();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);

	    setIntent(intent);

	    getData();
        initViews();
        initTextData();
    }

    private void initTextData() {
        if (null != m_mainResult) {
            mNewNoteHandler.initviewdata();
        } else {
            if (BooleanPerfencesUtil.isAutoIndent()) {
                content.append(Globals.NEW_LINE_PREFIX);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        changeStatusBar(true);

        if (getAuroraMenu().isShowing()) {
            Log.d(TAG, "Jim, will clear content focus.");
            clearContentFocus();
        }
    }

    private static class NewNoteHandler extends Handler {
        private WeakReference<NewNoteActivity> mTarget;
        
        public NewNoteHandler(NewNoteActivity activity) {
            mTarget = new WeakReference<NewNoteActivity>(activity);
        }
        
        private final int NEWNOTEMAIN_INIT = 0;
        private final int NEWNOTEMAIN_DISVIEW = 1;

        @Override
        public void handleMessage(Message msg) {
            final NewNoteActivity activity = mTarget.get();
            if (activity != null) {
                switch (msg.what) {
                    case NEWNOTEMAIN_INIT:
                        Log.i(TAG,
                                "New oncreate the currenttime3 ="
                                        + System.currentTimeMillis());

                        new Thread(new Runnable() {
                            public void run() {
                                activity.con_text = ChatEmotion.string2Symbol(
                                        NoteApp.ysApp,
                                        activity.m_mainResult.getContent(),
                                        activity.getWidth(),
                                        activity.getRightExtraSpace(),
                                        activity.getLeftPadding(),
                                        activity.getTopPadding());
                                showContent();
                            }
                        }).start();

                        break;
                    case NEWNOTEMAIN_DISVIEW:
                        Log.i(TAG,
                                "New oncreate the currenttime4 ="
                                        + System.currentTimeMillis());
                        activity.content.setText(activity.con_text);
                        Log.i(TAG,
                                "New oncreate the currenttime5 ="
                                        + System.currentTimeMillis());
                        activity.dialog.dismiss();
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        }

        public void initviewdata() {
            final NewNoteActivity activity = mTarget.get();
            if (activity != null) {
                activity.dialog.show();
                sendEmptyMessage(NEWNOTEMAIN_INIT);
            }
        }

        public void showContent() {
            sendEmptyMessage(NEWNOTEMAIN_DISVIEW);
        }
    };

    private void getData() {
        Bundle bl = getIntent().getExtras();
        if (null != bl) {
            note_tye = bl.getInt(TYPE_GET_DATA, 0);
            if (note_tye == 0) {
                m_mainResult = bl.getParcelable(NOTE_OBJ);
            } else {
                int note_id = bl.getInt(NOTE_ID);
                m_mainResult = noteDb.queryDataByID(note_id);
            }
        }
        if (null == m_mainResult) {
            Log.i(TAG, "new note");
        } else {
            updateLable();
        }
        mIsComeFromQuickRecord = getIntent().getBooleanExtra(EXTRA_KEY_COME_FROM_QUICK_RECORD, false);
    }

    private void updateLable() {
        String lable1 = m_mainResult.getLabel1();
        String lable2 = m_mainResult.getLabel2();

        if (!TextUtils.isEmpty(lable1)) {
            lable1 = mLabelDb.queryNameById(lable1);
            m_mainResult.setLabel1(lable1);
        }
        if (!TextUtils.isEmpty(lable2)) {
            lable2 = mLabelDb.queryNameById(lable2);
            m_mainResult.setLabel2(lable2);
        }
    }

    private void initDB() {
        noteDb = new NoteAdapter(this);
        noteDb.open();
        mLabelDb = new LabelAdapter(this);
        mLabelDb.open();
    }

    private void addOrModifyLabel() {
        ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_TAG);
        command.updateData();

        if (mLabelDb == null) return;

        if (mLabelList == null) {
            mLabelList = mLabelDb.queryAllData();
        }
        /*if (mLabelList == null || mLabelList.size() == 0) {
            if (mLabelList == null) {
                mLabelList = new ArrayList<LabelResult>();
            }

            showAddLabelDialog();
        } else {
            mSelectedLabels[0] = mLabel1Tv.getText().toString();
            mSelectedLabels[1] = mLabel2Tv.getText().toString();

            showChooseLabelDialog();
        }*/
        if (mLabelList == null) {
            mLabelList = new ArrayList<LabelResult>();
        } else {
            mSelectedLabels[0] = mLabel1Tv.getText().toString();
            mSelectedLabels[1] = mLabel2Tv.getText().toString();
        }
        showModifyLabelDialog();
    }

    @SuppressLint("InflateParams")
    private void showAddLabelDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.edit_label_dialog, null);
        final AuroraEditText editText = (AuroraEditText) view.findViewById(R.id.edit_note_label);
        final AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
                .setTitle(R.string.note_add_label)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String labelString = editText.getText().toString().trim();

                                if (isLabelExist(labelString)) {
                                    ToastUtil.shortToast(R.string.note_add_label_notice);
                                    hideSoftInput(editText);
                                    showChooseLabelDialog();
                                    return;
                                }

                                LabelResult labelResult = new LabelResult();
                                labelResult.setContent(labelString);
                                labelResult.setUpdate_time(System.currentTimeMillis());
                                mLabelDb.insert(labelResult);
                                mLabelList.add(0, labelResult);

                                if (TextUtils.isEmpty(mSelectedLabels[0])) {
                                    mSelectedLabels[0] = labelString;
                                } else if (TextUtils.isEmpty(mSelectedLabels[1])) {
                                    mSelectedLabels[1] = labelString;
                                }

                                hideSoftInput(editText);
                                showChooseLabelDialog();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean haveLabelBefore = mLabelList.size() != 0;
                                if (haveLabelBefore) {
                                    hideSoftInput(editText);
                                    showChooseLabelDialog();
                                }
                            }
                        })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface arg0) {
                showSoftInput(editText);
            }
        });

        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                boolean enabled = !TextUtils.isEmpty(editText.getText().toString().trim());
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(enabled);
            }
        });

        SystemUtils.lengthFilter(editText, 20, getString(R.string.new_note_text_limit));
    }

    private boolean isLabelExist(String labelString) {
        boolean labelExisted = false;
        for (LabelResult label : mLabelList) {
            if (label.getContent().equals(labelString)) {
                labelExisted = true;
                break;
            }
        }
        return labelExisted;
    }

    private void showSoftInput(AuroraEditText editText) {
        InputMethodManager imm = getInputMethodManager();
        imm.showSoftInput(editText, 0);
    }

    private void hideSoftInput(AuroraEditText editText) {
        InputMethodManager imm = getInputMethodManager();
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void showChooseLabelDialog() {
        final String[] lables = new String[mLabelList.size() + 1];
        final boolean[] checkedItems = new boolean[mLabelList.size() + 1];
        int i = 0;
        for (LabelResult label : mLabelList) {
            String labelString = label.getContent();
            lables[i] = labelString;
            if (labelString.equals(mSelectedLabels[0]) || labelString.equals(mSelectedLabels[1])) {
                checkedItems[i] = true;
            } else {
                checkedItems[i] = false;
            }
            i++;
        }
        lables[i] = getString(R.string.note_add_label);
        checkedItems[i] = false;

        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
                .setTitle(R.string.note_label)
                .setTitleDividerVisible(true)
                .setMultiChoiceItems(lables, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (which == mLabelList.size()) {
                                    dialog.dismiss();
                                    showAddLabelDialog();
                                } else {
                                    checkedItems[which] = isChecked;
                                    int checkedCount = 0;
                                    for (int i = 0; i < checkedItems.length; i++) {
                                        if (checkedItems[i]) {
                                            checkedCount++;
                                            if (checkedCount == 1) {
                                                mSelectedLabels[0] = lables[i];
                                                mSelectedLabels[1] = "";
                                            } else if (checkedCount == 2) {
                                                mSelectedLabels[1] = lables[i];
                                            }
                                        }
                                    }
                                    if (checkedCount == 0) {
                                        mSelectedLabels[0] = "";
                                        mSelectedLabels[1] = "";
                                    }
                                    if (checkedCount > 2) {
                                        ToastUtil.shortToast(R.string.note_choose_label_notice);
                                        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                                    } else {
                                        mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                    }
                                }
                            }
                        })
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] labelStrings = new String[] {"", ""};
                                int checkedCount = 0;
                                for (int i = 0; i < checkedItems.length; i++) {
                                    if (checkedItems[i]) {
                                        checkedCount++;
                                        if (checkedCount == 1) {
                                            labelStrings[0] = lables[i];
                                        } else if (checkedCount == 2) {
                                            labelStrings[1] = lables[i];
                                        }
                                    }
                                }
                                updateReminderLabel(labelStrings[0], labelStrings[1]);
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .show();

        mAlertDialog = dialog;
    }

    private void showModifyLabelDialog() {
        final ArrayList<String> labelList = new ArrayList<String>();
        final ArrayList<Boolean> checkedList = new ArrayList<Boolean>();
        boolean[] checkedItems = new boolean[mLabelList.size() + 1];

        int i = 0;
        for (LabelResult label : mLabelList) {
            String labelString = label.getContent();
            labelList.add(labelString);
            if (labelString.equals(mSelectedLabels[0]) || labelString.equals(mSelectedLabels[1])) {
                checkedList.add(true);
                checkedItems[i] = true;
            } else {
                checkedList.add(false);
                checkedItems[i] = false;
            }
            i++;
        }
        labelList.add(getString(R.string.note_add_label));
        checkedList.add(false);
        checkedItems[i] = false;

        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
            .setTitle(R.string.note_label)
            .setTitleDividerVisible(true)
            .setShowAddItemViewInMultiChoiceMode(true)
            .setMultiChoiceItems(labelList.toArray(new String[labelList.size()]), checkedItems,
                new AuroraMultipleChoiceListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedList.set(which, isChecked);

                        int checkedCount = 0;
                        for (boolean checked : checkedList) {
                            if (checked) {
                                checkedCount++;
                            }
                        }

                        if (checkedCount > 2) {
                            ToastUtil.shortToast(R.string.note_choose_label_notice);
                            mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }

                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isEqual,
                            CharSequence newItemText) {
                        if (isEqual) {
                            ToastUtil.shortToast(R.string.note_add_label_notice);
                            return;
                        }

                        String labelString = newItemText.toString().trim();

                        LabelResult labelResult = new LabelResult();
                        labelResult.setContent(labelString);
                        labelResult.setUpdate_time(System.currentTimeMillis());
                        mLabelDb.insert(labelResult);
                        mLabelList.add(labelResult);

                        labelList.add(labelList.size() - 1, labelString);
                        checkedList.add(checkedList.size() - 1, true);

                        int checkedCount = 0;
                        for (boolean checked : checkedList) {
                            if (checked) {
                                checkedCount++;
                            }
                        }

                        if (checkedCount > 2) {
                            ToastUtil.shortToast(R.string.note_choose_label_notice);
                            mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }

                    @Override
                    public void onInput(final EditText editText, final Button button) {
                        button.setEnabled(false);
                        editText.addTextChangedListener(new TextWatcher() {

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                            }

                            @Override
                            public void afterTextChanged(Editable arg0) {
                                String labelName = editText.getText().toString().trim();
                                boolean showToast = false;
                                boolean enabled = !TextUtils.isEmpty(labelName) && !(showToast = isLabelExist(labelName));
                                button.setEnabled(enabled);

                                if (showToast) {
                                    ToastUtil.shortToast(R.string.note_add_label_notice);
                                }
                            }
                        });

                        SystemUtils.lengthFilter(editText, 20, getString(R.string.new_note_text_limit));
                    }
                })
        .setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] labelStrings = new String[] {"", ""};
                        int i = 0;
                        int checkedCount = 0;
                        for (boolean checked : checkedList) {
                            if (checked) {
                                checkedCount++;
                                if (checkedCount == 1) {
                                    labelStrings[0] = labelList.get(i);
                                } else if (checkedCount == 2) {
                                    labelStrings[1] = labelList.get(i);
                                }
                            }
                            i++;
                        }
                        updateReminderLabel(labelStrings[0], labelStrings[1]);
                    }
                })
        .setNegativeButton(R.string.cancel, null)
        .show();

        mAlertDialog = dialog;
    }

    private void goToAddOrEditReminder() {
        ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_REMINDER);
        command.updateData();

        Intent intent = new Intent(this, AddOrEditReminderActivity.class);
        if (hasReminder()) {
            intent.putExtra(AddOrEditReminderActivity.KEY_MODE,
                    AddOrEditReminderActivity.MODE_EDIT_REMINDER);
            intent.putExtra(
                    AddOrEditReminderActivity.KEY_REMINDER_DATE_TIMESTAMP,
                    warningtime);
        } else {
            intent.putExtra(AddOrEditReminderActivity.KEY_MODE,
                    AddOrEditReminderActivity.MODE_ADD_REMINDER);
        }
        startActivityForResult(intent, Globals.REQUEST_CODE_ADD_REMINDER);
    }

    private void initAuroraMenu() {
        int menuResId = R.menu.new_note;
        if (type == 1)
            menuResId = R.menu.pic_note;
        else if (type == 2)
            menuResId = R.menu.video_note;
        else if (type == 4)
            menuResId = R.menu.note_image;

        if (mLastAuroraMenuResId != menuResId) {
            clearOriginalAuroraMenuItems();
            setAuroraMenuItems(menuResId);
            mLastAuroraMenuResId = menuResId;
        }

        if (menuResId == R.menu.note_image) {
            if (spanCount > 1) {
                removeAuroraMenuItemById(R.id.action_crop_image);
            } else {
                addAuroraMenuItemById(R.id.action_crop_image);
            }
        }

        if (menuResId == R.menu.new_note) {
            if (hasReminder()) {
                updateAuroraMenuItemTitle(R.id.action_add_reminder,
                        R.string.menu_mod_reminder);
            } else {
                updateAuroraMenuItemTitle(R.id.action_add_reminder,
                        R.string.menu_add_reminder);
            }

            if (hasLabel()) {
                updateAuroraMenuItemTitle(R.id.action_add_label,
                        R.string.menu_mod_label);
            } else {
                updateAuroraMenuItemTitle(R.id.action_add_label,
                        R.string.menu_add_label);
            }

            if (TextUtils.isEmpty(content.getText().toString().trim())) {
                removeAuroraMenuItemById(R.id.action_gen_picture);
            } else {
                addAuroraMenuItemById(R.id.action_gen_picture);
            }
        }
    }

    private boolean hasReminder() {
        return warningtime > 0;
    }

    private boolean hasLabel() {
        return !TextUtils.isEmpty(mLabel1) || !TextUtils.isEmpty(mLabel2);
    }

    /**
     * 修改指定菜单项的标题
     */
    private void updateAuroraMenuItemTitle(int menuItemId, int newTitleResId) {
        AuroraMenuAdapterBase adapter = getAuroraMenuAdapter();
        if (adapter != null) {
            final ArrayList<AuroraMenuItem> items = adapter.getMenuItems();
            if (items != null && !items.isEmpty()) {
                for (AuroraMenuItem item : items) {
                    if (item.getId() == menuItemId) {
                        item.setTitle(newTitleResId);
                        break;
                    }
                }
            }
        } else {
            Log.e(TAG,
                    "Failed to update aurora menu item title, adapter is null.");
        }
    }

    private void setAuroraMenuListener() {
        setAuroraMenuCallBack(new OnAuroraMenuItemClickListener() {
            @Override
            public void auroraMenuItemClick(int menuItemId) {
                switch (menuItemId) {
                    case R.id.action_add_reminder:
                        goToAddOrEditReminder();
                        break;
                    case R.id.action_add_label:
                        addOrModifyLabel();
                        break;
                    case R.id.action_gen_picture:
                        generatePicture();
                        break;
                    case R.id.action_take_photo:
                        openCamera();
                        break;
                    case R.id.action_select_photo:
                        openGallery(0);
                        break;
                    case R.id.action_take_video:
                        openVideo();
                        break;
                    case R.id.action_select_video:
                        openGallery(1);
                        break;
                    case R.id.action_remove_image:
                        removeImage();
                        break;
                    case R.id.action_view_image:
                        viewImage();
                        break;
                    case R.id.action_crop_image:
                        cropImage();
                        break;
                }
            }
        });
    }
    
    private void generatePicture() {
        ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_SHARE);
        command.updateData();

        if (checkSDCard()) {
            new GeneratePictureTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        }
    }
    
    private static class GeneratePictureTask extends AsyncTask<Void, Void, String> {
        private WeakReference<NewNoteActivity> mTarget;
        private NoteProgressDialog2 mProgressDialog;
        private TextView mTmpTv;
        private DoScreenShotResult mDoScreenShotResult = new DoScreenShotResult();
        
        public GeneratePictureTask(NewNoteActivity activity) {
            mTarget = new WeakReference<NewNoteActivity>(activity);
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            NewNoteActivity activity = mTarget.get();
            if (activity != null) {
                mProgressDialog = NoteProgressDialog2.createDialog(activity, false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(activity.getResources().getString(R.string.new_note_gen_picture));
                mProgressDialog.show();
                
                final EditText view = activity.content;
                AlignRightTextView tv = new AlignRightTextView(activity);
                tv.setWordSpace(DensityUtil.dip2px(activity, 0.5f));
                // tv.setBackgroundResource(activity.getPaperSignResourceId());
                int bgResourceId = SystemUtils.isIndiaVersion() ? activity.getPaperNoSignResourceId() : 
                        activity.getPaperSignResourceId();
                tv.setBackgroundResource(bgResourceId);
                /*tv.setPadding(view.getPaddingLeft(),
                        activity.getResources().getDimensionPixelSize(R.dimen.new_note_gen_picture_padding_top),
                        view.getPaddingRight(), view.getPaddingBottom());*/
                Resources res = activity.getResources();
                tv.setPadding(res.getDimensionPixelSize(R.dimen.new_note_gen_picture_padding_left),
                        res.getDimensionPixelSize(R.dimen.new_note_gen_picture_padding_top),
                        res.getDimensionPixelSize(R.dimen.new_note_gen_picture_padding_left),
                        res.getDimensionPixelSize(R.dimen.new_note_gen_picture_padding_bottom));

                /*tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.getTextSize());
                tv.setTextColor(view.getTextColors());
                tv.setLineSpacing(view.getLineSpacingExtra(), view.getLineSpacingMultiplier());*/
                float lineSpacing = 11.0f * res.getDisplayMetrics().density;
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                tv.setTextColor(res.getColor(R.color.note_generate_picture_text_color));
                tv.setLineSpacing(lineSpacing, 1.0f);

                // tv.setMaxHeight(15000); // 防止内容太多导致生成图片时OOM, 1920 * 10
                // tv.setMaxLines(150); // 防止内容太多导致生成图片时OOM
                Editable contentEditable = view.getText();
                String contentStr = contentEditable.toString();
                while (contentStr.endsWith("\n")) {
                    contentEditable.delete(contentStr.length() - 1, contentStr.length());
                    contentStr = contentStr.substring(0, contentStr.length() - 1);
                }
                tv.setText(contentEditable/*view.getText()*/);
                tv.measure(MeasureSpec.makeMeasureSpec(view.getWidth()
                        - view.getTotalPaddingLeft()
                        - view.getTotalPaddingRight()
                        + tv.getPaddingLeft()
                        + tv.getPaddingRight(),
                        MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
                mTmpTv = tv;
            }
        }

        @Override
        protected String doInBackground(Void... paramArrayOfParams) {
            NewNoteActivity activity = mTarget.get();
            if (activity != null) {
                return activity.doScreenShot(mTmpTv, mDoScreenShotResult);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            NewNoteActivity activity = mTarget.get();
            if (activity != null) {
                if (mProgressDialog != null && mProgressDialog.isShowing() && !activity.isFinishing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                mTmpTv = null;
                
                if (!TextUtils.isEmpty(mDoScreenShotResult.errorInfo)) {
                    ToastUtil.shortToast(mDoScreenShotResult.errorInfo);
                    return;
                }
                
                if (!TextUtils.isEmpty(result)) {
                    Intent intent = new Intent(activity, WXEntryActivity.class);
                    intent.putExtra("url", result);
                    activity.startActivity(intent);
                }
            }
        }
    }
    
    private void selectSubSpan(Editable editable, NoteImageSpan groupSpan, NoteImageSpan subSpan) {
        content.setNoteImageSpanSelected(groupSpan, subSpan);
    }
    
    private void unselectSubSpan() {
        content.clearSelectedNoteImageSpan();
    }
    
    private void emptySelectedSubSpan() {
        content.emptySelectedNoteImagespan();
    }

    private void removeImage() {
    	handleDeleteAttachment(selectedType, spanEndIndex, spanSource, spanIndex);
    }

    private void viewImage() {
        Intent intent = new Intent(mContext, PictureViewActivity.class);
        intent.putExtra("url", spanSource);
        intent.putExtra("content", content.getText().toString());
        startActivity(intent);
    }

    private void cropImage() {
        if (!checkSDCard()) {
            return;
        }
        if (!Globals.CROP_DIR.exists()) {
            Globals.CROP_DIR.mkdirs();
        }

        String fileName = getCropFileName();
        Uri outputUri = Uri.fromFile(new File(Globals.CROP_DIR, fileName));

        new Crop(Uri.parse(spanSource)).output(outputUri).asSquare().withExpectWidth(getWidth()).start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
		if (resultCode == RESULT_OK) {
			Uri uri = Crop.getOutput(result);
			if (uri != null && "file".equalsIgnoreCase(uri.getScheme())) {
				String html = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_IMAGE +
                        Globals.FILE_PROTOCOL + uri.getPath() + Globals.ATTACHMENT_END;

				Editable editable = content.getEditableText();
				int start = editable.getSpanStart(selectedSpan);
				int end = editable.getSpanEnd(selectedSpan);

                editable.delete(start, end);
                editable.insert(start, html);
                SpannableString str = ChatEmotion.string2Symbol(
                        mContext,
                        html,
                        getWidth(),
                        getRightExtraSpace(),
                        getLeftPadding(),
                        getTopPadding());
                editable.replace(start, start + html.length(), str);
			}
		} else if (resultCode == Crop.RESULT_ERROR) {
			Toast.makeText(this, R.string.load_picture_error_io/*Crop.getError(result).getMessage()*/,
					Toast.LENGTH_SHORT).show();
		}
    }

    private void setListener() {
        SystemUtils.lengthFilter(content, maxLength, getString(R.string.new_note_text_limit));

        content.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ly_menu.setVisibility(View.GONE);
                } else {
                    ly_menu.setVisibility(View.VISIBLE);
                }
            }
        });

        content.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (KeyEvent.KEYCODE_ENTER == keyCode
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (sign != 0) {
                        if (sign == SIGN_INDENT_BULLET) {
                            insertPicToEdit(Globals.SIGN_INDENT_ID, Globals.ATTACHMENT_TYPE_SIGN, true);
                        } else {
                            insertPicToEdit(Globals.SIGN_NOINDENT_ID, Globals.ATTACHMENT_TYPE_SIGN, true);
                        }
                        return true;
                    }

                    if (hasMark) {
                        insertPicToEdit(Globals.SIGN_UNCHECKED_ID, Globals.ATTACHMENT_TYPE_SIGN, true);
                        return true;
                    }
                    
                    if (BooleanPerfencesUtil.isAutoIndent()) {
                        content.getText().replace(content.getSelectionStart(),
                                content.getSelectionEnd(), Globals.NEW_LINE_WITH_PREFIX);
                        return true;
                    }
                } else if (KeyEvent.KEYCODE_DEL == keyCode
                        && event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (!isDelFinish) return true;

                    int index = content.getSelectionStart();
                    Editable editable = content.getText();
                    if (TextUtils.isEmpty(editable.toString())) return false;

                    char[] dest = new char[index];
                    editable.getChars(0, index, dest, 0);

                    String front_str = String.valueOf(dest);
                    Log.i(TAG, "zhangwei the dest=" + front_str);
                    // editable.delete(index-1, index);
                    if (front_str.length() < 10) return false;

                    if (front_str/*.trim()*/.endsWith(Globals.ATTACHMENT_END)) {
                        int start = front_str.lastIndexOf(Globals.ATTACHMENT_START);
                        del_type = front_str.substring(start + 10, start + 11);
                        if (String.valueOf(Globals.ATTACHMENT_TYPE_IMAGE_GROUP).equals(del_type)) {
                            // 如果是组合图片的话，需要先选中
                            NoteImageSpan[] spans = editable.getSpans(index, index, NoteImageSpan.class);
                            if (spans != null && spans.length > 0) {
                                NoteImageSpan span = spans[0];
                                if (span instanceof NoteImageGroupSpan) {
                                    NoteImageGroupSpan groupSpan = (NoteImageGroupSpan) span;
                                    if (groupSpan.getSubSpanCount() > 1) {
                                        // 有一张以上的图片
                                        final ArrayList<NoteImageSpan> subSpans = groupSpan.getSubSpans();
                                        NoteImageSpan lastSubSpan = subSpans.get(subSpans.size() - 1);
                                        if (!lastSubSpan.isSelected()) {
                                            selectSubSpan(editable, span, lastSubSpan);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                        isDelFinish = false;
                        return handleDeleteAttachment(del_type, content.getSelectionStart(), null, -1);
                    } else if (front_str/*.trim()*/.endsWith(Globals.ATTACHMENT_END + Globals.NEW_LINE)) {
                        if (index < editable.length() && editable.charAt(index) != Globals.CHAR_NEW_LINE) {
                            content.setSelection(index - 1);
                            return true;
                        }
                        return false;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        });

        content.setOnCursorLineChangedListener(new OnCursorLineChangedListener() {
            @Override
            public void onCursorLineChanged(String currentLineText) {
                if (currentLineText != null) {
                    boolean changed = false;
                    if (currentLineText.startsWith(BULLET_TEXT)) {
                        if (sign != SIGN_NORMAL_BULLET) {
                            sign = SIGN_NORMAL_BULLET;
                            changed = true;
                        }
                    } else if (currentLineText.startsWith(BULLET_INDENT_TEXT)) {
                        if (sign != SIGN_INDENT_BULLET) {
                            sign = SIGN_INDENT_BULLET;
                            changed = true;
                        }
                    } else {
                        if (sign != SIGN_NO_BULLET) {
                            sign = SIGN_NO_BULLET;
                            changed = true;
                        }
                    }

                    if (currentLineText.startsWith(MARK_UNCHECKED_TEXT) ||
                            currentLineText.startsWith(MARK_CHECKED_TEXT)) {
                        if (!hasMark) {
                            hasMark = true;
                            changed = true;
                        }
                    } else {
                        if (hasMark) {
                            hasMark = false;
                            changed = true;
                        }
                    }

                    if (changed) {
                        updateMenuStatus();
                    }
                }
            }
        });

        content.setOnNoteImageSpanLongClickListener(new OnNoteImageSpanLongClickListener() {
            @Override
            public boolean onNoteImageSpanLongClick(Editable editable, NoteImageSpan parentSpan,
                    NoteImageSpan span, int index) {
                int spanStart = editable.getSpanStart(parentSpan);
                int spanEnd = editable.getSpanEnd(parentSpan);

                if (spanStart == -1 || spanEnd == -1) return false;

                String spanText = editable.subSequence(spanStart, spanEnd).toString();
                Log.d(TAG, "Jim, spanText: " + spanText);
                if (!TextUtils.isEmpty(spanText)) {
                    content.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    String type = spanText.substring(Globals.ATTACHMENT_START_LENGTH, Globals.ATTACHMENT_START_LENGTH + 1);
                    Log.d(TAG, "Jim, type: " + type + ", source: " + span.getSource());
                    return handleDeleteAttachment(type, spanEnd, span.getSource(), index);
                }
                return false;
            }
        });

        content.setOnNoteImageSpanClickListener(new OnNoteImageSpanClickListener() {
            @Override
            public boolean onNoteImageSpanClicked(Editable editable, NoteImageSpan span, int count, int index,
            		String source, Type spanType, int selection) {
                if (NoteImageSpan.Type.Type_Picture.equals(spanType)) {
                    /*Intent intent = new Intent(mContext, PictureViewActivity.class);
                    intent.putExtra("url", source);
                    intent.putExtra("content", content.getText().toString());
                    startActivity(intent);*/

                    int spanStart = editable.getSpanStart(span);
                    int spanEnd = editable.getSpanEnd(span);

                    if (spanStart == -1 || spanEnd == -1) return true;

                    String spanText = editable.subSequence(spanStart, spanEnd).toString();
                    if (!TextUtils.isEmpty(spanText)) {
                    	selectedType = spanText.substring(Globals.ATTACHMENT_START_LENGTH,
                    			Globals.ATTACHMENT_START_LENGTH + 1);
                        selectedSpan = span;
                        spanEndIndex = spanEnd;
                        spanCount = count;
                        spanIndex = index;
                        spanSource = source;

                        type = 4;
                        showOrDismissAuroraMenu();
                    }

                    return true;
                } else if (NoteImageSpan.Type.Type_Video.equals(spanType)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(source);
                    intent.setDataAndType(uri, "video/mp4");
                    startActivity(intent);

                    return true;
                } else if (NoteImageSpan.Type.Type_Sound.equals(spanType)) {
                    Intent intent = new Intent(mContext, PlayActivity2.class);
                    String[] tme = source.split("&");
                    intent.putExtra("url", tme[0]);
                    if (tme.length > 2) {
                        intent.putExtra(PlayActivity2.EXTRA_SHORT_FILE_NAME, tme[2]);
                    }
                    startActivityForResult(intent, Globals.REQUEST_CODE_PLAY_RECORD);
                    NoteSoundSpan soundSpan = (NoteSoundSpan) span;
                    soundSpan.setIsPlaying(true);
                    content.refreshNoteImageSpan(span);
                    mLastPlayingSoundSpan = soundSpan;

                    return true;
                } else if (NoteImageSpan.Type.Type_Preset_Image.equals(spanType)) {
                    return true;
                } else if (Globals.SIGN_UNCHECKED_ID.equals(source) || Globals.SIGN_CHECKED_ID.equals(source)) {
                    updateMark(selection);
                    return true;
                }

                return false;
            }
        });

        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Jim, s: " + s + ", start: " + start + ", before: " + before + ", count: " + count);
                if (before == 0 && count > 0) {
                    // 只处理往文本框插入内容的情况
                    String text = s.toString();
                    int rowStartIndex = ChatEmotion.getRowStartIndex(text, start);
                    if (rowStartIndex >= 0 && rowStartIndex < start) {
                        String line = text.substring(rowStartIndex, start);
                        if (!TextUtils.isEmpty(line) && line.endsWith(Globals.ATTACHMENT_END)) {
                            int index = line.lastIndexOf(Globals.ATTACHMENT_START, start);
                            if (index != -1) {
                                index += Globals.ATTACHMENT_START_LENGTH;
                                int type = Integer.parseInt(line.substring(index, index + 1));
                                if (type != Globals.ATTACHMENT_TYPE_SIGN) {
                                    // 在附件后面输入文字
                                    index = start;
                                    if (index < s.length() && s.charAt(index) != Globals.CHAR_NEW_LINE) {
                                        if (s instanceof SpannableStringBuilder) {
                                            SpannableStringBuilder ssb = (SpannableStringBuilder) s;
                                            ssb.replace(index, index, Globals.NEW_LINE);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        int rowEndIndex = ChatEmotion.getRowEndIndex(text, start);
                        if (rowEndIndex > start + count) {
                            String line = text.substring(start + count, rowEndIndex);
                            if (!TextUtils.isEmpty(line) && line.startsWith(Globals.ATTACHMENT_START)) {
                                // 在附件前面输入了内容，强制在内容和附件之间插入换行符
//                                int index = line.indexOf(Globals.ATTACHMENT_START);
//                                if (index != -1) {
//                                    index += Globals.ATTACHMENT_START_LENGTH;
//                                    int type = Integer.parseInt(line.substring(index, index + 1));
//                                    if (type != Globals.ATTACHMENT_TYPE_SIGN) {
//                                        // 在附件前面输入文字
//                                        index = start + count;
//                                        if (index < s.length() && s.charAt(index) != Globals.CHAR_NEW_LINE) {
//                                            if (s instanceof SpannableStringBuilder) {
//                                                SpannableStringBuilder ssb = (SpannableStringBuilder) s;
//                                                ssb.replace(index, index, Globals.NEW_LINE);
//                                            }
//                                        }
//                                    }
//                                }
                                if (s instanceof SpannableStringBuilder) {
                                    // 删除附件前面输入的内容，也就是不让在附件前面输入内容
                                    SpannableStringBuilder ssb = (SpannableStringBuilder) s;
                                    ssb.delete(start, start + count);
                                }
                            }
                        }
                    }
                }

                if (before == 0 && count > 0) {
                    String noteContent = s.toString();
                    int rowStartIndex = ChatEmotion.getRowStartIndex(noteContent, start);
                    int rowEndIndex = ChatEmotion.getRowEndIndex(noteContent, start);
                    String rowContent = null;
                    if (rowStartIndex >= 0 && rowEndIndex >= 0 && rowStartIndex <= rowEndIndex) {
                        rowContent = noteContent.substring(rowStartIndex, rowEndIndex);
                    }
                    Editable noteContentEditable = content.getEditableText();
                    if (noteContentEditable != null && !TextUtils.isEmpty(rowContent)
                            && rowContent.startsWith(MARK_CHECKED_TEXT)) {

                        /*StrikethroughSpan[] spans = noteContentEditable.getSpans(
                                rowStartIndex + MARK_CHECKED_TEXT.length(), rowEndIndex, StrikethroughSpan.class);
                        if (spans != null && spans.length > 0) {
                            noteContentEditable.removeSpan(spans[0]);
                        }*/

                        ForegroundColorSpan[] colorSpans = noteContentEditable.getSpans(
                                rowStartIndex + MARK_CHECKED_TEXT.length(), rowEndIndex, ForegroundColorSpan.class);
                        if (colorSpans != null && colorSpans.length > 0) {
                            noteContentEditable.removeSpan(colorSpans[0]);
                        }

                        SpannableString str = ChatEmotion.string2Symbol(
                                mContext,
                                rowContent,
                                getWidth(),
                                getRightExtraSpace(),
                                getLeftPadding(),
                                getTopPadding());
                        noteContentEditable.replace(rowStartIndex, rowEndIndex, str);
                    }
                }

                if (before > 0 && count == 0) {
                    String noteContent = s.toString();
                    int rowStartIndex = ChatEmotion.getRowStartIndex(noteContent, start);
                    int rowEndIndex = ChatEmotion.getRowEndIndex(noteContent, start);
                    String rowContent = null;
                    if (rowStartIndex >= 0 && rowEndIndex >= 0 && rowStartIndex <= rowEndIndex) {
                        rowContent = noteContent.substring(rowStartIndex, rowEndIndex);
                    }
                    Editable noteContentEditable = content.getEditableText();
                    if (noteContentEditable != null && !TextUtils.isEmpty(rowContent)) {

                        /*StrikethroughSpan[] spans = noteContentEditable.getSpans(
                                rowStartIndex, rowEndIndex, StrikethroughSpan.class);
                        if (spans != null && spans.length > 0) {
                            noteContentEditable.removeSpan(spans[0]);
                        }*/

                        ForegroundColorSpan[] colorSpans = noteContentEditable.getSpans(
                                rowStartIndex, rowEndIndex, ForegroundColorSpan.class);
                        if (colorSpans != null && colorSpans.length > 0) {
                            noteContentEditable.removeSpan(colorSpans[0]);
                        }

                        if (rowContent.startsWith(MARK_CHECKED_TEXT)) {
                            SpannableString str = ChatEmotion.string2Symbol(
                                    mContext,
                                    rowContent,
                                    getWidth(),
                                    getRightExtraSpace(),
                                    getLeftPadding(),
                                    getTopPadding());
                            noteContentEditable.replace(rowStartIndex, rowEndIndex, str);
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "Jim, beforeTextChanged, s: " + s + ", start: " + start + ", count: " + count + ", after: " + after);
                unselectSubSpan();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mReminderContainer.setOnClickListener(this);
        mNoteLabelContainer.setOnClickListener(this);
        mGenPic.setOnClickListener(this);
        mSetPaper.setOnClickListener(this);

        mRootView.setOnSizeChangedListener(new OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                Log.d(TAG, "Jim, w: " + w + ", h: " + h + ", oldw: " + oldw
                        + ", oldh: " + oldh);
                if (h < oldh && h < oldh - 160) {
                    mIsInputMethodWindowShown = true;
                } else if (h > oldh + 160) {
                    mIsInputMethodWindowShown = false;
                    unselectSubSpan();
                }

                adjustEditTextPaddingBottom();
                if (mPendingRunnable != null) {
                    mNewNoteHandler.postDelayed(mPendingRunnable, 200);
                    mPendingRunnable = null;
                }
            }
        });
    }

    private void adjustEditTextPaddingBottom() {
        mNewNoteHandler.post(mAdjustContentPaddingBottom);
    }

    private final Runnable mAdjustContentPaddingBottom = new Runnable() {
        @Override
        public void run() {
            if (mIsInputMethodWindowShown) {
                content.setPadding(content.getPaddingLeft(),
                        content.getPaddingTop(),
                        content.getPaddingRight(),
                        content.getLineHeight());
            } else {
                content.setPadding(content.getPaddingLeft(),
                        content.getPaddingTop(),
                        content.getPaddingRight(),
                        mContentPaddingBottom);
            }
        }
    };

    int getRightExtraSpace() {
        return 6;
//        if (mImageRightExtraSpace == -1) {
//            mImageRightExtraSpace = (int)Math.ceil(content.getPaint().measureText(" "));
//        }
//        
//        Log.d(TAG, "Jim, image right extra space: " + mImageRightExtraSpace);
//        
//        return mImageRightExtraSpace;
    }
    
    int getLeftPadding() {
        return content.getCompoundPaddingLeft();
    }
    
    int getTopPadding() {
        return content.getCompoundPaddingTop();
    }
    
    int getWidth() {
        int measuredWidth = content.getMeasuredWidth();
        if (measuredWidth == 0) {
            preMeasureContent();
            measuredWidth = content.getMeasuredWidth();
        }
        return measuredWidth - content.getPaddingLeft() - content.getPaddingRight() - getRightExtraSpace();
    }
    
    boolean handleDeleteAttachment(final String del_type, final int spanEndIndex, final String imageGroupSource,
            final int spanIndex) {
        if (del_type.equals(String.valueOf(Globals.ATTACHMENT_TYPE_SIGN))) {
            isDelFinish = true;
            return false;
        }

        String message = "", title = "";
        if (del_type.equals(String.valueOf(Globals.ATTACHMENT_TYPE_IMAGE)) ||
                del_type.equals(String.valueOf(Globals.ATTACHMENT_TYPE_IMAGE_GROUP)) ||
                        del_type.equals(String.valueOf(Globals.ATTACHMENT_TYPE_PRESET_IMAGE))) {
            title = getResources().getString(R.string.dialog_delete_image_title);
            message = getResources().getString(R.string.dialog_delete_image_message);
        } else if (del_type.equals(String.valueOf(Globals.ATTACHMENT_TYPE_VIDEO))) {
        	title = getResources().getString(R.string.dialog_delete_video_title);
            message = getResources().getString(R.string.dialog_delete_video_message);
        } else if (del_type.equals(String.valueOf(Globals.ATTACHMENT_TYPE_RECORD))) {
        	title = getResources().getString(R.string.dialog_delete_audio_title);
            message = getResources().getString(R.string.dialog_delete_audio_message);
        }

        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
        builder.setTitle(title).setMessage(message)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int index = spanEndIndex;
                                Editable editable = content.getText();

                                int length = editable.toString().length();
                                if (index > length) {
                                    isDelFinish = true;
                                    return;
                                }

                                char[] dest = new char[index];
                                editable.getChars(0, index, dest, 0);

                                String front_str = String.valueOf(dest);

                                int start = front_str.lastIndexOf(Globals.ATTACHMENT_START);
                                int end = front_str.lastIndexOf(Globals.ATTACHMENT_END);
                                if (start >= 0 && end >= 0) {
                                    if (del_type.equals(String.valueOf(Globals.ATTACHMENT_TYPE_IMAGE_GROUP))) {
                                        String oriImagePath = front_str.substring(start + Globals.ATTACHMENT_START_LENGTH + 1, end);
                                        String[] allPaths = oriImagePath.split(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
                                        if (allPaths.length > 1) {
                                            StringBuilder sb = new StringBuilder();
                                            if (TextUtils.isEmpty(imageGroupSource)) {
                                                // 从缓存里删除被移除图片的缓存
                                                // NoteImageSpanBitmapCache.getInstance().removeBitmap(allPaths[allPaths.length - 1]);
                                                for (int i = 0; i < allPaths.length - 1; i ++) {
                                                    if (i > 0) {
                                                        sb.append(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
                                                    }
                                                    sb.append(allPaths[i]);
                                                }
                                            } else {
                                                // 从缓存里删除被移除图片的缓存
                                                // NoteImageSpanBitmapCache.getInstance().removeBitmap(imageGroupSource);
                                                for (int i = 0; i < allPaths.length; i ++) {
                                                    if (spanIndex >=0 && spanIndex < allPaths.length) {
                                                        if (i != spanIndex) {
                                                            if (sb.length() > 0) {
                                                                sb.append(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
                                                            }
                                                            sb.append(allPaths[i]);
                                                        }
                                                    } else {
                                                        if (!imageGroupSource.equalsIgnoreCase(allPaths[i])) {
                                                            if (sb.length() > 0) {
                                                                sb.append(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
                                                            }
                                                            sb.append(allPaths[i]);
                                                        }
                                                    }
                                                }
                                            }
                                            String html = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_IMAGE_GROUP +
                                                    sb.toString() + Globals.ATTACHMENT_END;
                                            editable.delete(start, end + Globals.ATTACHMENT_END_LENGTH);
                                            editable.insert(start, html);
                                            SpannableString str = ChatEmotion.string2Symbol(
                                                    mContext,
                                                    html,
                                                    getWidth(),
                                                    getRightExtraSpace(),
                                                    getLeftPadding(),
                                                    getTopPadding());
                                            editable.replace(start, start + html.length(), str);
                                        } else {
                                            // 从缓存里删除被移除图片的缓存
                                            // NoteImageSpanBitmapCache.getInstance().removeBitmap(oriImagePath);
                                            editable.delete(start, end + 5);
                                        }
                                    } else {
                                        editable.delete(start, end + 5);
                                    }
                                    isDelFinish = true;
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                unselectSubSpan();
                                isDelFinish = true;
                            }
                        })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface arg0) {
                        emptySelectedSubSpan();
                        isDelFinish = true;
                    }
                }).show();
        return true;
    }

    public void onSign_Click(View view) {
        // closeSoftInputWindow();
        if (sign == 2) {
            sign = SIGN_NO_BULLET;
        } else {
            sign++;
        }
        updateMenuStatus();
        insertOrClearBullet();
    }

    public void onMark_Click(View view) {
        hasMark = !hasMark;
        updateMenuStatus();
        insertOrClearMark();
    }

    private void updateMenuStatus() {
        if (hasMark) {
            mBulletMenuIv.setImageResource(R.drawable.new_note_menu_bullet_list_unable);
            mBulletMenuIv.setClickable(false);
        } else {
            updateBulletMenuStatus();
            mBulletMenuIv.setClickable(true);
        }

        if (sign == SIGN_NO_BULLET) {
            if (hasMark) {
                mMarkMenu.setImageResource(R.drawable.new_note_menu_mark_clear_selector);
            } else {
                mMarkMenu.setImageResource(R.drawable.new_note_menu_mark_selector);
            }
            mMarkMenu.setClickable(true);
        } else {
            mMarkMenu.setImageResource(R.drawable.new_note_menu_mark_unable);
            mMarkMenu.setClickable(false);
        }
    }

    private void insertOrClearMark() {
        if (hasMark) {
            insertPicToEdit(Globals.SIGN_UNCHECKED_ID, Globals.ATTACHMENT_TYPE_SIGN, false);
        } else {
            clearMark();
        }
    }

    private void clearMark() {
        String strNoteContent = content.getText().toString();
        Editable noteContentEditable = content.getEditableText();
        final int selStart = content.getSelectionStart();

        if (!TextUtils.isEmpty(strNoteContent) && noteContentEditable != null) {
            int startIndex = -1;
            int endIndex = -1;

            int index = strNoteContent.indexOf(Globals.NEW_LINE);
            if (index == -1 || selStart <= index) {
                // 只有一行或者光标在第一行
                if (strNoteContent.startsWith(MARK_UNCHECKED_TEXT)) {
                    startIndex = 0;
                    endIndex = MARK_UNCHECKED_TEXT.length();
                } else if (strNoteContent.startsWith(MARK_CHECKED_TEXT)) {
                    startIndex = 0;
                    endIndex = MARK_CHECKED_TEXT.length();
                }
            } else {
                int rowStartIndex = ChatEmotion.getRowStartIndex(strNoteContent, selStart);
                int rowEndIndex = ChatEmotion.getRowEndIndex(strNoteContent, selStart);
                String curLineText = null;
                if (rowStartIndex >= 0 && rowEndIndex >= 0 && rowStartIndex <= rowEndIndex) {
                    curLineText = strNoteContent.substring(rowStartIndex, rowEndIndex);
                }
                if (!TextUtils.isEmpty(curLineText)) {
                    if (curLineText.startsWith(MARK_UNCHECKED_TEXT)) {
                        startIndex = rowStartIndex;
                        endIndex = rowStartIndex + MARK_UNCHECKED_TEXT.length();
                    } else if (curLineText.startsWith(MARK_CHECKED_TEXT)) {
                        startIndex = rowStartIndex;
                        endIndex = rowStartIndex + MARK_CHECKED_TEXT.length();
                    }
                }
            }

            if (startIndex != -1 && endIndex != -1) {
                noteContentEditable.delete(startIndex, endIndex);
            }
        }
    }

    private void updateMark(int selection) {
        String strNoteContent = content.getText().toString();
        Editable noteContentEditable = content.getEditableText();
        final int selStart = selection/*content.getSelectionStart()*/;

        if (!TextUtils.isEmpty(strNoteContent) && noteContentEditable != null) {
            int startIndex = -1;
            int endIndex = -1;
            int lineEndIndex = -1;
            String markText = null;

            int index = strNoteContent.indexOf(Globals.NEW_LINE);
            if (index == -1 || selStart <= index) {
                // 只有一行或者光标在第一行
                if (strNoteContent.startsWith(MARK_UNCHECKED_TEXT)) {
                    startIndex = 0;
                    endIndex = MARK_UNCHECKED_TEXT.length();
                    markText = MARK_CHECKED_TEXT;
                } else if (strNoteContent.startsWith(MARK_CHECKED_TEXT)) {
                    startIndex = 0;
                    endIndex = MARK_CHECKED_TEXT.length();
                    markText = MARK_UNCHECKED_TEXT;
                }
                lineEndIndex = index == -1 ? strNoteContent.length() : index;
            } else {
                int rowStartIndex = ChatEmotion.getRowStartIndex(strNoteContent, selStart);
                int rowEndIndex = ChatEmotion.getRowEndIndex(strNoteContent, selStart);
                String curLineText = null;
                if (rowStartIndex >= 0 && rowEndIndex >= 0 && rowStartIndex <= rowEndIndex) {
                    curLineText = strNoteContent.substring(rowStartIndex, rowEndIndex);
                }
                if (!TextUtils.isEmpty(curLineText)) {
                    if (curLineText.startsWith(MARK_UNCHECKED_TEXT)) {
                        startIndex = rowStartIndex;
                        endIndex = rowStartIndex + MARK_UNCHECKED_TEXT.length();
                        markText = MARK_CHECKED_TEXT;
                    } else if (curLineText.startsWith(MARK_CHECKED_TEXT)) {
                        startIndex = rowStartIndex;
                        endIndex = rowStartIndex + MARK_CHECKED_TEXT.length();
                        markText = MARK_UNCHECKED_TEXT;
                    }
                }
                lineEndIndex = rowEndIndex;
            }

            if (startIndex != -1 && endIndex != -1 && endIndex <= lineEndIndex) {
                /*StrikethroughSpan[] spans = noteContentEditable.getSpans(endIndex,
                        lineEndIndex, StrikethroughSpan.class);
                if (spans != null && spans.length > 0) {
                    noteContentEditable.removeSpan(spans[0]);
                }*/

                ForegroundColorSpan[] colorSpans = noteContentEditable.getSpans(endIndex,
                        lineEndIndex, ForegroundColorSpan.class);
                if (colorSpans != null && colorSpans.length > 0) {
                    noteContentEditable.removeSpan(colorSpans[0]);
                }

                String lineText = markText + strNoteContent.substring(endIndex, lineEndIndex);

                SpannableString str = ChatEmotion.string2Symbol(
                        this,
                        lineText,
                        getWidth(),
                        getRightExtraSpace(),
                        getLeftPadding(),
                        getTopPadding());
                content.setSelection(lineEndIndex);
                noteContentEditable.replace(startIndex, lineEndIndex, str);
            }
        }
    }

    /**
     * 删除之前的项目符号，如果有的话
     * 
     * @param strNoteContent
     * @param noteContentEditable
     */
    private void deleteOriginalBullet(String strNoteContent,
            Editable noteContentEditable, int selStart) {
        if (!TextUtils.isEmpty(strNoteContent) && noteContentEditable != null) {
            int startIndex = -1;
            int endIndex = -1;
            
            int index = strNoteContent.indexOf(Globals.NEW_LINE);
            if (index == -1 || selStart <= index) {
                // 只有一行或者光标在第一行
                if (strNoteContent.startsWith(BULLET_TEXT)) {
                    startIndex = 0;
                    endIndex = BULLET_TEXT.length();
                } else if (strNoteContent.startsWith(BULLET_INDENT_TEXT)) {
                    startIndex = 0;
                    endIndex = BULLET_INDENT_TEXT.length();
                }
            } else {
                int rowStartIndex = ChatEmotion.getRowStartIndex(strNoteContent, selStart);
                int rowEndIndex = ChatEmotion.getRowEndIndex(strNoteContent, selStart);
                String curLineText = null;
                if (rowStartIndex >= 0 && rowEndIndex >= 0 && rowStartIndex <= rowEndIndex) {
                    curLineText = strNoteContent.substring(rowStartIndex, rowEndIndex);
                }
                if (!TextUtils.isEmpty(curLineText)) {
                    if (curLineText.startsWith(BULLET_TEXT)) {
                        startIndex = rowStartIndex;
                        endIndex = rowStartIndex + BULLET_TEXT.length();
                    } else if (curLineText.startsWith(BULLET_INDENT_TEXT)) {
                        startIndex = rowStartIndex;
                        endIndex = rowStartIndex + BULLET_INDENT_TEXT.length();
                    }
                }
            }
            
            if (startIndex != -1 && endIndex != -1) {
                noteContentEditable.delete(startIndex, endIndex);
            }
        }
    }

    private void insertOrClearBullet() {
        final int bulletStatus = sign % 3;
        Editable noteContent = content.getEditableText();
        String text = content.getText().toString();
        final int selStart = content.getSelectionStart();

        // 清除之前的项目符号
        deleteOriginalBullet(text, noteContent, selStart);
        switch (bulletStatus) {
            case SIGN_NORMAL_BULLET:
                // 插入不带缩进的项目符号
                insertPicToEdit(Globals.SIGN_NOINDENT_ID, Globals.ATTACHMENT_TYPE_SIGN, false);
                break;
            case SIGN_INDENT_BULLET:
                // 插入带缩进的项目符号
                insertPicToEdit(Globals.SIGN_INDENT_ID, Globals.ATTACHMENT_TYPE_SIGN, false);
                break;
        }
    }

    private void updateBulletMenuStatus() {
        mBulletMenuIv.setImageResource(BULLET_MENU_SRC_RES[(sign + 1) % 3]); // 显示下一个状态的图片资源
    }

    /**
     * 清空之前的Aurora菜单
     */
    private void clearOriginalAuroraMenuItems() {
        setAuroraMenuAdapter(null);
    }

    public void onPic_Click(View view) {
        String con_str = content.getText().toString();
        if (!SystemUtils.isNull(con_str)) {
            /*int image_count = getImageCount(con_str);
            if (image_count >= 10) {
                ToastUtil.longToast(R.string.new_note_pic_num);
                return;
            }*/
            int attachmentCount = getAttachmentCount(con_str);
            if (attachmentCount >= 30) {
                ToastUtil.longToast(R.string.new_note_attachment_count_limit);
                return;
            }
        }

        type = 1;
        showOrDismissAuroraMenu();
    }

    public void onVideo_Click(View view) {
        String con_str = content.getText().toString();
        if (!SystemUtils.isNull(con_str)) {
            /*int video_count = SystemUtils.getCount(con_str, Globals.ATTACHMENT_VIDEO_PATTERN);
            if (video_count >= 10) {
                ToastUtil.longToast(R.string.new_note_video_num);
                return;
            }*/
            int attachmentCount = getAttachmentCount(con_str);
            if (attachmentCount >= 30) {
                ToastUtil.longToast(R.string.new_note_attachment_count_limit);
                return;
            }
        }

        type = 2;
        showOrDismissAuroraMenu();
    }

    private final Runnable mSoundMenuClickRunnable = new Runnable() {
        @Override
        public void run() {
            mRecordMenu.setSelected(true);
            Intent intent = new Intent(NewNoteActivity.this, RecordActivity2.class);
//            Intent intent = new Intent(NewNoteActivity.this, RecordActivity.class);
            startActivityForResult(intent, Globals.REQUEST_CODE_ADD_RECORD);
            content.setIsHandleEvent(false);
        }
    };

    public void onSound_Click(View view) {
        String con_str = content.getText().toString();
        if (!SystemUtils.isNull(con_str)) {
            /*int sound_count = SystemUtils.getCount(con_str, Globals.ATTACHMENT_SOUND_PATTERN);
            if (sound_count >= 10) {
                ToastUtil.longToast(R.string.new_note_sound_num);
                return;
            }*/
            int attachmentCount = getAttachmentCount(con_str);
            if (attachmentCount >= 30) {
                ToastUtil.longToast(R.string.new_note_attachment_count_limit);
                return;
            }
        }

        type = 3;

        if (isInputMethodShow()) {
            content.setIsHandleEvent(true);
            closeSoftInputWindow();
            mPendingRunnable = mSoundMenuClickRunnable;
        } else {
            mSoundMenuClickRunnable.run();
        }
    }

    private void insertPicToEdit(String imageName, int type, boolean isTriggeredByEnter/* 是否是回车触发的 */) {
        String html = "";
        final String text = content.getText().toString();
        try {
            if ((type == Globals.ATTACHMENT_TYPE_RECORD) || (type == Globals.ATTACHMENT_TYPE_SIGN)) {
                html = Globals.ATTACHMENT_START + type + imageName + Globals.ATTACHMENT_END;
            } else {
                html = Globals.ATTACHMENT_START + type + Globals.FILE_PROTOCOL + imageName +
                        Globals.ATTACHMENT_END;
            }

            int selectedIndex = content.getSelectionStart();
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }
            if (type == Globals.ATTACHMENT_TYPE_SIGN) {
                if (isTriggeredByEnter) {
                    html = Globals.NEW_LINE + html;
                }
            } else {
                if (!TextUtils.isEmpty(text)) {
                    if (selectedIndex == 0) {
                        if (!text.startsWith(Globals.NEW_LINE)) {
                            html = html + Globals.NEW_LINE;
                        }
                    } else {
                        String preText = text.substring(0, selectedIndex);
                        if (!preText.endsWith(Globals.NEW_LINE)) {
                            html = Globals.NEW_LINE + html;
                        }
                        String postText = text.substring(selectedIndex, text.length());
                        if (!TextUtils.isEmpty(postText) && !postText.startsWith(Globals.NEW_LINE)) {
                            html = html + Globals.NEW_LINE;
                        }
                    }
                }
            }
//            if (!TextUtils.isEmpty(text.trim())
//                    && !(type == Globals.ATTACHMENT_TYPE_SIGN && !isTriggeredByEnter)) {
//                if ((type == Globals.ATTACHMENT_TYPE_RECORD) || (type == Globals.ATTACHMENT_TYPE_SIGN)) {
//                    if (type == Globals.ATTACHMENT_TYPE_SIGN) {
//                        html = Globals.NEW_LINE + Globals.ATTACHMENT_START + type + imageName +
//                                Globals.ATTACHMENT_END;
//                    } else {
//                        html = Globals.NEW_LINE + Globals.ATTACHMENT_START + type + imageName +
//                                Globals.ATTACHMENT_END/* + Globals.NEW_LINE*/;
//                    }
//                } else {
//                    html = Globals.NEW_LINE + Globals.ATTACHMENT_START + type + Globals.FILE_PROTOCOL
//                            + imageName + Globals.ATTACHMENT_END/* + Globals.NEW_LINE*/;
//                }
//            } else {
//                if ((type == Globals.ATTACHMENT_TYPE_RECORD) || (type == Globals.ATTACHMENT_TYPE_SIGN)) {
//                    if (type == Globals.ATTACHMENT_TYPE_SIGN) {
//                        html = Globals.ATTACHMENT_START + type + imageName +
//                                Globals.ATTACHMENT_END;
//                    } else {
//                        html = Globals.ATTACHMENT_START + type + imageName +
//                                Globals.ATTACHMENT_END/* + Globals.NEW_LINE*/;
//                    }
//                } else {
//                    html = Globals.ATTACHMENT_START + type + Globals.FILE_PROTOCOL + imageName +
//                            Globals.ATTACHMENT_END/* + Globals.NEW_LINE*/;
//                }
//            }

            Editable editable = content.getEditableText();

            if (!TextUtils.isEmpty(text) && editable != null) {
                int startIndex = ChatEmotion.getRowStartIndex(text, selectedIndex);
                int endIndex = ChatEmotion.getRowEndIndex(text, selectedIndex);
                String rowContent = null;
                if (startIndex >= 0 && endIndex >= 0 && endIndex >= startIndex) {
                    rowContent = text.substring(startIndex, endIndex);
                }
                if (!TextUtils.isEmpty(rowContent) && rowContent.startsWith(MARK_CHECKED_TEXT)) {
                    /*StrikethroughSpan[] spans = editable.getSpans(
                            startIndex + MARK_CHECKED_TEXT.length(), endIndex, StrikethroughSpan.class);
                    if (spans != null && spans.length > 0) {
                        editable.removeSpan(spans[0]);
                    }*/

                    ForegroundColorSpan[] colorSpans = editable.getSpans(
                            startIndex + MARK_CHECKED_TEXT.length(), endIndex, ForegroundColorSpan.class);
                    if (colorSpans != null && colorSpans.length > 0) {
                        editable.removeSpan(colorSpans[0]);
                    }
                }
            }

            boolean changeSelection = false;
            int insertPosIndex = 0;
            if (type == Globals.ATTACHMENT_TYPE_SIGN && !isTriggeredByEnter) {
                // 输入项目符号
                insertPosIndex = ChatEmotion.getRowStartIndex(text, selectedIndex);
                
                int rowEndIndex = ChatEmotion.getRowEndIndex(text, selectedIndex);
                if (rowEndIndex > insertPosIndex) {
                    String lineText = text.substring(insertPosIndex, rowEndIndex);
                    int attachmentStartIndex = lineText.indexOf(Globals.ATTACHMENT_START);
                    if (attachmentStartIndex != -1) {
                        if (rowEndIndex < text.length()) {
                            if (text.charAt(rowEndIndex) == Globals.CHAR_NEW_LINE) {
                                insertPosIndex = rowEndIndex + 1;
                                html = html + Globals.NEW_LINE;
                                changeSelection = true;
                            } else {
                                insertPosIndex = rowEndIndex;
                                html = Globals.NEW_LINE + html + Globals.NEW_LINE;
                            }
                        } else {
                            insertPosIndex = rowEndIndex;
                            html = Globals.NEW_LINE + html;
                        }
                    }
                }
            } else {
                insertPosIndex = selectedIndex;
            }

            // 如果连续插入多张图片，要改成图片分组的形式
            if (Globals.ATTACHMENT_TYPE_IMAGE == type && text.length() > 0) {
                String trimText = null;
                int imageSpanStartIndex = -1;
                int imageSpanEndIndex = -1;
                HandleImageResult result = null;
                if (selectedIndex == 0) {
//                    trimText = text.trim();
//                    if (!TextUtils.isEmpty(trimText) && trimText.startsWith(Globals.ATTACHMENT_START)) {
//                        imageSpanStartIndex = text.indexOf(Globals.ATTACHMENT_START);
//                        imageSpanEndIndex = text.indexOf(Globals.ATTACHMENT_END);
//                    }
                    imageSpanStartIndex = text.indexOf(Globals.ATTACHMENT_START);
                    if (imageSpanStartIndex >= selectedIndex) {
                        trimText = text.substring(selectedIndex, imageSpanStartIndex);
                        if (shouldMergeImage(trimText)) {
                            imageSpanEndIndex = text.indexOf(Globals.ATTACHMENT_END);
                        } else {
                            imageSpanStartIndex = -1;
                        }
                    }
                } else if (selectedIndex == text.length()) {
//                    trimText = text.trim();
//                    if (!TextUtils.isEmpty(trimText) && trimText.endsWith(Globals.ATTACHMENT_END)) {
//                        imageSpanStartIndex = text.lastIndexOf(Globals.ATTACHMENT_START);
//                        imageSpanEndIndex = text.lastIndexOf(Globals.ATTACHMENT_END);
//                    }
                    imageSpanEndIndex = text.lastIndexOf(Globals.ATTACHMENT_END);
                    if (imageSpanEndIndex != -1) {
                        trimText = text.substring(imageSpanEndIndex + Globals.ATTACHMENT_END_LENGTH);
                        if (shouldMergeImage(trimText)) {
                            imageSpanStartIndex = text.lastIndexOf(Globals.ATTACHMENT_START);
                        } else {
                            imageSpanEndIndex = -1;
                        }
                    }
                } else {
                    // 中间开始插入，优先往前找
//                    trimText = text.substring(0, selectedIndex).trim();
//                    if (!TextUtils.isEmpty(trimText) && trimText.endsWith(Globals.ATTACHMENT_END)) {
//                        imageSpanStartIndex = text.lastIndexOf(Globals.ATTACHMENT_START, selectedIndex);
//                        imageSpanEndIndex = text.lastIndexOf(Globals.ATTACHMENT_END, selectedIndex);
//                        result = handleImage(text, imageName, editable, imageSpanStartIndex, imageSpanEndIndex);
//                        if (result == null) {
//                            trimText = text.substring(selectedIndex).trim();
//                            if (!TextUtils.isEmpty(trimText) && trimText.startsWith(Globals.ATTACHMENT_START)) {
//                                imageSpanStartIndex = text.indexOf(Globals.ATTACHMENT_START, selectedIndex);
//                                imageSpanEndIndex = text.indexOf(Globals.ATTACHMENT_END, selectedIndex);
//                            }
//                        }
//                    } else {
//                        trimText = text.substring(selectedIndex).trim();
//                        if (!TextUtils.isEmpty(trimText) && trimText.startsWith(Globals.ATTACHMENT_START)) {
//                            imageSpanStartIndex = text.indexOf(Globals.ATTACHMENT_START, selectedIndex);
//                            imageSpanEndIndex = text.indexOf(Globals.ATTACHMENT_END, selectedIndex);
//                        }
//                    }
                    imageSpanEndIndex = text.lastIndexOf(Globals.ATTACHMENT_END, selectedIndex);
                    if (imageSpanEndIndex != -1) {
                        trimText = text.substring(imageSpanEndIndex + Globals.ATTACHMENT_END_LENGTH, selectedIndex);
                        if (shouldMergeImage(trimText)) {
                            imageSpanStartIndex = text.lastIndexOf(Globals.ATTACHMENT_START, selectedIndex);
                            result = handleImage(text, imageName, editable, imageSpanStartIndex, imageSpanEndIndex);
                            if (result == null) {
                                // 前面分组已经是image group，而且图片的张数已经达到允许的最大张数了
                                imageSpanStartIndex = text.indexOf(Globals.ATTACHMENT_START, selectedIndex);
                                if (imageSpanStartIndex >= selectedIndex) {
                                    trimText = text.substring(selectedIndex, imageSpanStartIndex);
                                    if (shouldMergeImage(trimText)) {
                                        imageSpanEndIndex = text.indexOf(Globals.ATTACHMENT_END, selectedIndex);
                                    } else {
                                        imageSpanStartIndex = -1;
                                    }
                                }
                            }
                        } else {
                            imageSpanEndIndex = -1;
                            imageSpanStartIndex = text.indexOf(Globals.ATTACHMENT_START, selectedIndex);
                            if (imageSpanStartIndex >= selectedIndex) {
                                trimText = text.substring(selectedIndex, imageSpanStartIndex);
                                if (shouldMergeImage(trimText)) {
                                    imageSpanEndIndex = text.indexOf(Globals.ATTACHMENT_END, selectedIndex);
                                } else {
                                    imageSpanStartIndex = -1;
                                }
                            }
                        }
                    } else {
                        imageSpanStartIndex = text.indexOf(Globals.ATTACHMENT_START, selectedIndex);
                        if (imageSpanStartIndex >= selectedIndex) {
                            trimText = text.substring(selectedIndex, imageSpanStartIndex);
                            if (shouldMergeImage(trimText)) {
                                imageSpanEndIndex = text.indexOf(Globals.ATTACHMENT_END, selectedIndex);
                            } else {
                                imageSpanStartIndex = -1;
                            }
                        }
                    }
                }

                if (result == null) {
                    result = handleImage(text, imageName, editable, imageSpanStartIndex, imageSpanEndIndex);
                }
                if (result != null) {
                    html = result.html;
                    insertPosIndex = result.sign_index;
                }
            }

            editable.insert(insertPosIndex, html);

            if (changeSelection) {
                content.setSelection(insertPosIndex + 1);
            }

            if (editable.length() + html.length() >= maxLength)
                return;

//            if (selectedIndex < 0)
//                selectedIndex = 0;

            SpannableString str = ChatEmotion.string2Symbol(
                    this,
                    html,
                    getWidth(),
                    getRightExtraSpace(),
                    getLeftPadding(),
                    getTopPadding());
            editable.replace(insertPosIndex, insertPosIndex + html.length(), str);

            // content.setSelection(selectedIndex + html.length());

//            if (isTriggeredByEnter) {
//                content.setText(ChatEmotion.string2Symbol(this, content
//                        .getText().toString(), content.getMeasuredWidth()
//                        - content.getPaddingLeft() - content.getPaddingRight(),
//                        getSignHeight()));
//                content.setSelection(selectedIndex + html.length());
//            }
        } catch (Exception e) {
            FileLog.e(TAG, e.getMessage());
        }
    }

    /**
     * 是否应该合并图片
     * @param trimText
     * @return
     */
    private static boolean shouldMergeImage(String trimText) {
        if (!TextUtils.isEmpty(trimText)) {
            trimText = trimText.replaceAll(" ", "");
//            if (Globals.NEW_LINE.equals(trimText)) {
//                return true;
//            }

            if (TextUtils.isEmpty(trimText)) {
                return true;
            }

            return false;
        }

        return true;
    }

    private static class HandleImageResult {
        int sign_index;
        String html;
    }

    private HandleImageResult handleImage(String text, String imageName, Editable editable, int imageSpanStartIndex, int imageSpanEndIndex) {
        HandleImageResult result = null;
        if (imageSpanStartIndex != -1 && imageSpanEndIndex != -1) {
            int start = imageSpanStartIndex + Globals.ATTACHMENT_START_LENGTH;
            int originalType = Integer.parseInt(text.substring(start, start + 1));
            if (originalType == Globals.ATTACHMENT_TYPE_IMAGE) {
                // 之前是单张图片，需要替换为图片组
                result = new HandleImageResult();
                String oriImagePath = text.substring(start + 1, imageSpanEndIndex);
                result.html = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_IMAGE_GROUP +
                        oriImagePath + Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP +
                        Globals.FILE_PROTOCOL + imageName + Globals.ATTACHMENT_END;
                editable.delete(imageSpanStartIndex, imageSpanEndIndex + Globals.ATTACHMENT_END_LENGTH);
                result.sign_index = imageSpanStartIndex;
                Log.d(TAG, "Jim, oriImagePath: " + oriImagePath);
            } else if (originalType == Globals.ATTACHMENT_TYPE_IMAGE_GROUP) {
                String oriImagePath = text.substring(start + 1, imageSpanEndIndex);
                String[] allPaths = oriImagePath.split(Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP);
                if (allPaths.length < Globals.ATTACHMENT_IMAGE_GROUP_MAX_IMAGES) {
                    result = new HandleImageResult();
                    oriImagePath = oriImagePath + Globals.ATTACHMENT_IMAGE_GROUP_PATH_SEP +
                            Globals.FILE_PROTOCOL + imageName;
                    result.html = Globals.ATTACHMENT_START + Globals.ATTACHMENT_TYPE_IMAGE_GROUP +
                            oriImagePath + Globals.ATTACHMENT_END;
                    editable.delete(imageSpanStartIndex, imageSpanEndIndex + Globals.ATTACHMENT_END_LENGTH);
                    result.sign_index = imageSpanStartIndex;
                }
                Log.d(TAG, "Jim, oriImagePath: " + oriImagePath);
            }
        }

        return result;
    }

    /**
     * 这个方法只能在有Looper的线程里调用，因为会弹toast，否则会崩溃
     * @return
     */
    private boolean checkSDCard() {
        if (!SDcardManager.checkSDCardMount()) {
            ToastUtil.longToast(R.string.sdcard_not_mounted);
            return false;
        }
        if (!SDcardManager.checkSDCardAvailableSize()) {
            ToastUtil.longToast(R.string.sd_space_not_enough);
            return false;
        }
        return true;
    }

    /**
     * 打开相机
     * 
     * @author jason
     * @since 2014-4-14
     */
    private void openCamera() {
        if (!checkSDCard()) {
            return;
        }
        if (!Globals.PHOTO_DIR.exists()) {
            Globals.PHOTO_DIR.mkdirs();
        }

        String fileName = getPhotoFileName();
        mCurrentPhotoFile = new File(Globals.PHOTO_DIR, fileName);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));
        startActivityForResult(intent, Globals.REQUEST_CODE_GETIMAGE_BYCAMERA);
    }

    /**
     * 打开视频
     * 
     * @author jason
     * @since 2014-4-14
     */
    private void openVideo() {
        if (!checkSDCard()) {
            return;
        }
        if (!Globals.VIDEO_DIR.exists()) {
            Globals.VIDEO_DIR.mkdirs();
        }

        String fileName = getVideoFileName();
        mCurrentVideoFile = new File(Globals.VIDEO_DIR, fileName);
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentVideoFile));
        startActivityForResult(intent, Globals.REQUEST_CODE_GETVIDEO_BYCAMERA);
    }

    @SuppressLint("SimpleDateFormat")
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    @SuppressLint("SimpleDateFormat")
    private String getVideoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'VIDEO'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".mp4";
    }

    @SuppressLint("SimpleDateFormat")
    private String getPictureFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IUNINote'_yyyyMMdd_HHmmss");
        return dateFormat.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    private String getCropFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'CROP'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    private static class DoScreenShotResult {
        String errorInfo = null;
    }

    private String doScreenShot(TextView tv, DoScreenShotResult result) {
        if (!Globals.PIC_DIR.exists()) {
            Globals.PIC_DIR.mkdirs();
        }
        
        Bitmap bitmap = null;
        boolean hasError = false;
        int errorInfoResId = -1;
        try {
            long beginTime = System.currentTimeMillis();
            bitmap = convertViewToBitmap(tv);
            Log.d(TAG, "Jim, generate bitmap use time: " + (System.currentTimeMillis() - beginTime));
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Jim, convertViewToBitmap out of memory", e);
            hasError = true;
            errorInfoResId = R.string.new_note_gen_pic_error_content_too_large;
            // return null;
        } catch (Throwable t) {
            Log.e(TAG, "Jim, convertViewToBitmap error", t);
            hasError = true;
            errorInfoResId = R.string.new_note_gen_pic_error_unknow;
            // return null;
        }
        
        if (hasError) {
            if (result != null) {
                result.errorInfo = getResources().getString(errorInfoResId);
            }
            return null;
        }
        
        final boolean usePngFormat = true;
        String extName = usePngFormat ? ".png": ".jpg";
        String fname = Globals.PIC_DIR.getAbsolutePath() + "/" + getPictureFileName() + extName;
        File bitmapFile = new File(fname);
        if (bitmap != null) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(bitmapFile);
                if (usePngFormat) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                }
//                testImageFile(bitmap, Globals.PIC_DIR.getAbsolutePath() + "/");
                return fname;
            } catch (Exception e) {
                hasError = true;
                FileLog.e(TAG, e.getMessage());
            } catch (Throwable t) {
                hasError = true;
                FileLog.e(TAG, t.getMessage());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        FileLog.e(TAG, e.getMessage());
                    }
                }
                if (hasError && bitmapFile.exists()) {
                    bitmapFile.delete();
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        } else {
            Log.i(TAG, "doScreenShot bitmap is NULL!");
        }
        
        return null;
    }
    
    static void testImageFile(Bitmap bitmap, String path) {
        if (bitmap != null) {
            String baseFileName = System.currentTimeMillis() + "";
            FileOutputStream fos = null;
            try {
                String fileName;
                // Test PNG format
                for (int i = 10; i <= 100; i += 10) {
                    fileName = baseFileName + "_png_" + i + ".png";
                    fos = new FileOutputStream(path + fileName);
                    bitmap.compress(CompressFormat.PNG, i, fos);
                    fos.close();
                    fos = null;
                }
                
                // Test WEBP format
                for (int i = 10; i <= 100; i += 10) {
                    fileName = baseFileName + "_webp_" + i + ".webp";
                    fos = new FileOutputStream(path + fileName);
                    bitmap.compress(CompressFormat.WEBP, i, fos);
                    fos.close();
                    fos = null;
                }
                
                // Test JPEG format
                for (int i = 10; i <= 100; i += 10) {
                    fileName = baseFileName + "_jpg_" + i + ".jpg";
                    fos = new FileOutputStream(path + fileName);
                    bitmap.compress(CompressFormat.JPEG, i, fos);
                    fos.close();
                    fos = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Bitmap convertViewToBitmap(TextView tv) {
        Bitmap bitmap = Bitmap.createBitmap(
                tv.getMeasuredWidth(), tv.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas cas = new Canvas(bitmap);

        cas.save();
        tv.draw(cas);
        cas.restore();

        return bitmap;
    }

    /**
     * 打开图片选择器
     * 
     * @author jason
     * @since 2014-4-14
     */
    private void openGallery(int type) {

        /*Intent intent = new Intent();
        if (type == 0)
            intent.setType("image/*");
        else
            intent.setType("video/*");
        // intent.putExtra("crop", "true");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // 限制为只能用我们系统自带的Gallery来选择
        intent.setPackage("com.android.gallery3d");

        if (type == 0)
            startActivityForResult(intent, Globals.REQUEST_CODE_ALBUM);
        else
            startActivityForResult(intent, Globals.REQUEST_CODE_VIDEO);*/

        Intent intent = new Intent();
        if (type == 0) {
            intent.setType("image/*");
            intent.setAction("com.aurora.filemanager.MORE_GET_CONTENT");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, Globals.REQUEST_CODE_ALBUM);
        } else {
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setPackage("com.android.gallery3d");
            startActivityForResult(intent, Globals.REQUEST_CODE_VIDEO);
        }
    }

    private InputMethodManager getInputMethodManager() {
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        return mInputMethodManager;
    }

    private void closeSoftInputWindow() {
        InputMethodManager imm = getInputMethodManager();
        if (imm != null) {
            View focusView = getCurrentFocus();
            if (focusView != null) {
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Globals.REQUEST_CODE_GETIMAGE_BYCAMERA:
                if (resultCode == Activity.RESULT_OK && mCurrentPhotoFile != null) {
                    preMeasureContent();
                    insertPicToEdit(mCurrentPhotoFile.getAbsolutePath(), Globals.ATTACHMENT_TYPE_IMAGE, false);

                    ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_IMAGE);
                    command.updateData();
                }
                break;
            case Globals.REQUEST_CODE_GETVIDEO_BYCAMERA:
                if (resultCode == Activity.RESULT_OK && mCurrentVideoFile != null) {
                    preMeasureContent();
                    insertPicToEdit(mCurrentVideoFile.getAbsolutePath(), Globals.ATTACHMENT_TYPE_VIDEO, false);

                    ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_VIDEO);
                    command.updateData();
                }
                break;
            case Globals.REQUEST_CODE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    /*Uri uri = data.getData();
                    try {
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        // mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        String imagePath;
                        if (cursor != null) {
                            cursor.moveToFirst();
                            imagePath = cursor.getString(1);
                            insertPicToEdit(imagePath, Globals.ATTACHMENT_TYPE_IMAGE, false);
                        }
                        cursor.close();
                    } catch (Exception e) {
                        FileLog.e(TAG, e.getMessage());
                    }*/

                    Bundle bundle = data.getExtras();
                    ArrayList<String> paths = bundle.getStringArrayList("image");

                    int attCount = 0;
                    String noteContent = content.getText().toString();
                    if (!SystemUtils.isNull(noteContent)) {
                        attCount = getAttachmentCount(noteContent);
                    }
                    if (attCount + paths.size() <= 30) {
                        for (String imagePath : paths) {
                            insertPicToEdit(imagePath, Globals.ATTACHMENT_TYPE_IMAGE, false);
                        }

                        ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_IMAGE, paths.size());
                        command.updateData();
                    } else {
                        int allowCount = 30 - attCount;
                        for (int i = 0; i < allowCount; i++) {
                            insertPicToEdit(paths.get(i), Globals.ATTACHMENT_TYPE_IMAGE, false);
                        }
                        ToastUtil.longToast(R.string.new_note_attachment_count_limit);

                        ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_IMAGE, allowCount);
                        command.updateData();
                    }
                }
                break;
            case Globals.REQUEST_CODE_VIDEO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri1 = data.getData();
                    try {
                        Cursor cursor = getContentResolver().query(uri1, null, null, null, null);
                        // mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        String videoPath;
                        if (cursor != null) {
                            cursor.moveToFirst();
                            videoPath = cursor.getString(1);
                            insertPicToEdit(videoPath, Globals.ATTACHMENT_TYPE_VIDEO, false);
                        }
                        cursor.close();
                    } catch (Exception e) {
                        FileLog.e(TAG, e.getMessage());
                    }

                    ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_VIDEO);
                    command.updateData();
                }
                break;
            case Globals.REQUEST_CODE_ADD_RECORD:
                mRecordMenu.setSelected(false);
                if (resultCode == Activity.RESULT_OK) {
                    String recordFileName = data.getStringExtra("recordFileName");
                    mRecordDuration = data.getStringExtra("recordDuration");
                    String recordTime = data.getStringExtra("recordTime");
                    recordFileName = recordFileName + "&" + mRecordDuration + "&" + recordTime;
                    insertPicToEdit(recordFileName, Globals.ATTACHMENT_TYPE_RECORD, false);
                    if (mIsComeFromQuickRecord && BooleanPerfencesUtil.isFirstTimeUseQuickRecord()) {
                        //第一次使用快速录音功能
                        addTipsForFirstTimeUseQuickRecord();
                        BooleanPerfencesUtil.markFirstTimeUseQuickRecord();
                    }
                    mIsComeFromQuickRecord = false;

                    ReportCommand command = new ReportCommand(mContext, ReportUtil.TAG_RECORD);
                    command.updateData();
                } else {
                    if (mIsComeFromQuickRecord) {
                        finish();
                    }
                }
                break;
            case Globals.REQUEST_CODE_ADD_REMINDER:
                if (resultCode == Activity.RESULT_OK) {
                    warningtime = data.getLongExtra("reminderDateTimestamp", 0);
                    updateReminderTime(warningtime);
                    // Date d = new Date();
                    // d.setTime(warningtime);
                    // Log.d(TAG, "Jim, warn time: " + d.toLocaleString());
                }
                break;
            case Globals.REQUEST_CODE_PLAY_RECORD:
                if (resultCode == Activity.RESULT_OK) {
                    if (mLastPlayingSoundSpan != null) {
                        mLastPlayingSoundSpan.setIsPlaying(false);
                        content.refreshNoteImageSpan(mLastPlayingSoundSpan);
                        mLastPlayingSoundSpan = null;
                    }
                }
                break;
            case Globals.REQUEST_CODE_SELECT_PAPER:
                if (resultCode == RESULT_OK) {
                    mBackgroudPath = data.getStringExtra(NotePaperChangeActivity.PAPER_NAME);
                    updateBackgroud(mBackgroudPath);
                }
                break;
            case Crop.REQUEST_CROP:
                handleCrop(resultCode, data);
                break;
            default:
                break;
        }
    }

    private void addTipsForFirstTimeUseQuickRecord() {
        if (!TextUtils.isEmpty(content.getText().toString().trim())) {
            final Editable editable = content.getEditableText();
            editable.append(Globals.NEW_LINE);            

            final Resources res = getResources();
            editable.append(res.getString(R.string.first_use_quick_record_tip_1));
            insertPicToEdit(Globals.SIGN_NOINDENT_ID, Globals.ATTACHMENT_TYPE_SIGN, false);

            editable.append(Globals.NEW_LINE);
            editable.append(res.getString(R.string.first_use_quick_record_tip_2));
            insertPicToEdit(Globals.SIGN_NOINDENT_ID, Globals.ATTACHMENT_TYPE_SIGN, false);

            editable.append(Globals.NEW_LINE);
            editable.append(res.getString(R.string.first_use_quick_record_tip_3));
            insertPicToEdit(Globals.SIGN_NOINDENT_ID, Globals.ATTACHMENT_TYPE_SIGN, false);

            /*int startIndex = content.getSelectionEnd();
            int endIndex = content.getSelectionEnd();
            editable.setSpan(new BackgroundColorSpan(Color.LTGRAY), startIndex, endIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            new BlinkRunnable(startIndex, endIndex, editable).run();*/
        }
    }

    /*private static class BlinkRunnable implements Runnable {
        // 备忘录文本框文字颜色为#444444
        private final int[] COLORS = new int[] {Color.parseColor("#66444444"), Color.parseColor("#ff444444")};
        private static final int COUNT = 6;
        private int mIndex = 0;
        private final Handler mHandler = new Handler();
        private int mTextStartIndex, mTextEndIndex;
        private Editable mEditable;
        private NoteForegroundColorSpan mLastSpan;

        public BlinkRunnable(int startIndex, int endIndex, Editable editable) {
            mTextStartIndex = startIndex;
            mTextEndIndex = endIndex;
            mEditable = editable;
        }

        @Override
        public void run() {
            if (mIndex < COUNT) {
                NoteForegroundColorSpan colorSpan = mLastSpan;
                if (colorSpan == null) {
                    colorSpan = new NoteForegroundColorSpan(COLORS[mIndex % COLORS.length]);
                    mLastSpan = colorSpan;
                } else {
                    colorSpan.setColor(COLORS[mIndex % COLORS.length]);
                }
                mEditable.setSpan(colorSpan, mTextStartIndex, mTextEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//              content.setTextColor(COLORS[mIndex % COLORS.length]);
                mHandler.postDelayed(this, 500);
                mIndex ++;
            } else {
                if (mLastSpan != null) {
                    mEditable.removeSpan(mLastSpan);
                    mLastSpan = null;
                }
            }
        }

        private static class NoteForegroundColorSpan extends ForegroundColorSpan {

            private int mColor;

            public NoteForegroundColorSpan(int color) {
                super(color);
                mColor = color;
            }

            public void setColor(int color) {
                mColor = color;
            }

            @Override
            public int getForegroundColor() {
                return mColor;
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mColor);
            }
        }
    }*/

    /**
     * 用户设置或修改了备忘提醒时间，刷新界面显示
     * 
     * @param reminderTime
     */
    private void updateReminderTime(long reminderTime) {
        if (reminderTime > 0) {
            // 设置了提醒时间
            mReminderTv.setText(formatReminderTime(reminderTime));
        } else {
            mReminderTv.setText(getResources().getString(R.string.new_note_add_reminder));
        }
    }

    private static String formatReminderTime(long timestamp) {
        return formatTimeForNewNote(timestamp, "yyyy.M.dd  E  HH:mm");
    }
    
    private static String formatTitleTime(long timestamp) {
        return formatTimeForNewNote(timestamp, "yyyy.M.dd  HH:mm");
    }
    
    @SuppressLint("SimpleDateFormat")
    private static String formatTimeForNewNote(long timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return sdf.format(calendar.getTime());
    }

    /**
     * 用户设置或修改了标签，刷新界面显示
     * 
     * @param label1
     * @param label2
     */
    private void updateReminderLabel(String label1, String label2) {
        mLabel1 = label1;
        mLabel2 = label2;

        if (TextUtils.isEmpty(label1) && TextUtils.isEmpty(label2)) {
            // 清除标签
//            mLabel1Tv.setText("");
            mLabel1Tv.setText(getResources().getString(R.string.new_note_add_label));
            mLabel2Tv.setText("");
            if (mLabelSepTv.getVisibility() != View.GONE) {
                mLabelSepTv.setVisibility(View.GONE);
            }
        } else {
            if (!TextUtils.isEmpty(label1)) {
                if (mLabel1Tv.getVisibility() != View.VISIBLE) {
                    mLabel1Tv.setVisibility(View.VISIBLE);
                }
                mLabel1Tv.setText(label1);
            } else {
                mLabel1Tv.setText("");
                mLabel1Tv.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(label2)) {
                if (mLabel2Tv.getVisibility() != View.VISIBLE) {
                    mLabel2Tv.setVisibility(View.VISIBLE);
                }
                mLabel2Tv.setText(label2);
            } else {
                mLabel2Tv.setText("");
                mLabel2Tv.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(label1) && !TextUtils.isEmpty(label2)) {
                mLabelSepTv.setVisibility(View.VISIBLE);
            } else {
                mLabelSepTv.setVisibility(View.GONE);
            }
        }
    }

    private void initViews() {
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mGenPic = findViewById(R.id.gen_pic);
        mSetPaper = findViewById(R.id.set_paper);

        content = (CopyNoSpaceEditText) findViewById(R.id.note_content_et);
        ly_menu = (LinearLayout) findViewById(R.id.menu_ly);
        if (null == m_mainResult) {
            ly_menu.setVisibility(View.VISIBLE);
        } else {
            ly_menu.setVisibility(View.GONE);
        }

        mReminderContainer = findViewById(R.id.note_reminder_ly);
        mReminderTv = (TextView) findViewById(R.id.note_reminder_tv);
        mNoteLabelContainer = findViewById(R.id.note_label_ly);
        mLabel1Tv = (TextView) findViewById(R.id.note_label1_tv);
        mLabelSepTv = (TextView) findViewById(R.id.note_label_sep_tv);
        mLabel2Tv = (TextView) findViewById(R.id.note_label2_tv);
        mRecordMenu = findViewById(R.id.record_menu);
        mBulletMenuIv = (ImageView) findViewById(R.id.bullet_menu_iv);
        mMarkMenu = (ImageView) findViewById(R.id.mark_menu);

        mRootView = (NewNoteRelativeLayout) findViewById(R.id.root);

        long time = System.currentTimeMillis();
        if (null != m_mainResult) {
            preMeasureContent();
            clearContentFocus(); // 默认是浏览模式
            dialog = NoteProgressDialog.createDialog(this);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setMessage(getResources().getString(R.string.new_note_on_loading));
            warningtime = m_mainResult.getWarn_time();
            mLabel1 = m_mainResult.getLabel1();
            mLabel2 = m_mainResult.getLabel2();
            mBackgroudPath = m_mainResult.getBackgroundPath();
            initOriginalNoteInfo(m_mainResult);
            time = m_mainResult.getUpdate_time();
        }

        mTitleTv.setText(formatTitleTime(time));
        updateBackgroud(mBackgroudPath);
        updateReminderTime(warningtime);
        updateReminderLabel(mLabel1, mLabel2);

        mContentPaddingBottom = content.getPaddingBottom();
    }

    private void updateBackgroud(String backgroudPath) {
        int paperResourceId = -1;

        if (TextUtils.isEmpty(backgroudPath)) {
            paperResourceId = R.drawable.note_paper_01;
        } else if (backgroudPath.startsWith(Globals.DRAWABLE_PROTOCOL)) {
            for (int i = 0; i < Globals.NOTE_PAPERS.length; i++) {
                if (Globals.NOTE_PAPERS[i].equals(backgroudPath)) {
                    paperResourceId = PAPER_RESOURCE_IDS[i];
                    break;
                }
            }
        }

        if (paperResourceId != -1) {
            mRootView.setBackgroundResource(paperResourceId);
        }
    }

    private int getPaperSignResourceId() {
        int paperSignResourceId = -1;

        if (TextUtils.isEmpty(mBackgroudPath)) {
            paperSignResourceId = R.drawable.note_paper_sign_01;
        } else if (mBackgroudPath.startsWith(Globals.DRAWABLE_PROTOCOL)) {
            for (int i = 0; i < Globals.NOTE_PAPERS.length; i++) {
                if (Globals.NOTE_PAPERS[i].equals(mBackgroudPath)) {
                    paperSignResourceId = PAPER_SIGN_RESOURCE_IDS[i];
                    break;
                }
            }
        }

        return paperSignResourceId;
    }

    private int getPaperNoSignResourceId() {
        int paperSignResourceId = -1;

        if (TextUtils.isEmpty(mBackgroudPath)) {
            paperSignResourceId = R.drawable.note_paper_no_sign_01;
        } else if (mBackgroudPath.startsWith(Globals.DRAWABLE_PROTOCOL)) {
            for (int i = 0; i < Globals.NOTE_PAPERS.length; i++) {
                if (Globals.NOTE_PAPERS[i].equals(mBackgroudPath)) {
                    paperSignResourceId = PAPER_NO_SIGN_RESOURCE_IDS[i];
                    break;
                }
            }
        }

        return paperSignResourceId;
    }

    private void preMeasureContent() {
//        content.measure(
//                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
//                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        mRootView.measure(MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels,
                MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    private void initOriginalNoteInfo(NoteResult note) {
        mOriginalNoteInfo = new OriginalNoteInfo(note);
    }

    private static class OriginalNoteInfo {
        String content;
        String label1;
        String label2;
        String backgroudPath;
        long notifyTime;

        OriginalNoteInfo(NoteResult note) {
            content = note.getContent();
            label1 = note.getLabel1();
            label2 = note.getLabel2();
            backgroudPath = note.getBackgroundPath();
            notifyTime = note.getWarn_time();
        }
    }

    private void clearContentFocus() {
        View rootView = mRootView;
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
    }

    private void initActionBar() {
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setVisibility(View.GONE);
//        actionBar.setmOnActionBarBackItemListener(auroActionBarItemBackListener);
//        actionBar.setBackgroundResource(R.drawable.common_bg_with_line_repeat);
//        if (null == m_mainResult) {
//            actionBar.setTitle(R.string.new_note);
//        } else {
//            actionBar.setTitle(R.string.note_information);
//        }
//        addAuroraActionBarItem(AuroraActionBarItem.Type.More, AURORA_MORE);
//        actionBar.addItem(R.drawable.new_note_label_icon, AURORA_GEN_PICTURE, "");
//        actionBar.setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener() {
//                    @Override
//                    public void onAuroraActionBarItemClicked(int menuItemId) {
//                        switch (menuItemId) {
//                            case AURORA_MORE:
//                                if (isMenuCanBeDisplayed()) {
//                                    type = 0;
//                                    showOrDismissAuroraMenu();
//                                }
//                                break;
//                            case AURORA_GEN_PICTURE:
//                                generatePicture();
//                                break;
//                        }
//                    }
//                });
    }

    private void showOrDismissAuroraMenu() {
        if (isInputMethodShow()) {
            content.setIsHandleEvent(true);
            closeSoftInputWindow();
            mPendingRunnable = mShowAuroraMenuCommon;
        } else {
            mShowAuroraMenuCommon.run();
        }
    }

    private boolean isInputMethodShow() {
        // InputMethodManager imm = getInputMethodManager();
        // if (imm.isShowing()) {
        // return true;
        // }
        //
        // return false;
        // isShowing()不准

        return mIsInputMethodWindowShown;
    }

    private final Runnable mShowAuroraMenuCommon = new Runnable() {
        @Override
        public void run() {
            if (isFinishing()) return;

            initAuroraMenu();
            showAuroraMenu();
            content.setIsHandleEvent(false);
        }
    };

//    private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
//        public void onAuroraActionBarBackItemClicked(int itemId) {
//            switch (itemId) {
//                case -1:
//                    handleBack();
//                    break;
//                default:
//                    break;
//            }
//        }
//
//    };

    private static boolean hasSignOnly(String content) {
        if (content != null && TextUtils.isEmpty(content.replace(BULLET_TEXT, "")
                .replace(BULLET_INDENT_TEXT, "").replace(MARK_UNCHECKED_TEXT, "")
                        .replace(MARK_CHECKED_TEXT, "").trim())) {
            return true;
        }

        return false;
    }

    private boolean hasUpdateNote() {
        if (mOriginalNoteInfo != null) {
            if (!content.getText().toString().equals(mOriginalNoteInfo.content)) {
                return true;
            }

            if (!isEqual(mLabel1, mOriginalNoteInfo.label1)) {
                return true;
            }

            if (!isEqual(mLabel2, mOriginalNoteInfo.label2)) {
                return true;
            }

            if (!isEqual(mBackgroudPath, mOriginalNoteInfo.backgroudPath)) {
                return true;
            }

            if (mOriginalNoteInfo.notifyTime != warningtime) {
                return true;
            }

            return false;
        }

        return true;
    }

    private static boolean isEqual(String str1, String str2) {
        if (str1 == null && str1 == str2) {
            return true;
        }

        if (str1 != null && str1.length() == 0 && str2 != null
                && str2.length() == 0) {
            return true;
        }

        if (!TextUtils.isEmpty(str1) && str1.equals(str2)) {
            return true;
        }

        return false;
    }

    private void handleBack() {
        finish();
    }

    private synchronized void saveData() {
        Log.d(TAG, "Jim, saveData enter");
        if (noteDb != null) {
            boolean changed = false;
            if (null != m_mainResult) {
                String noteContent = content.getText().toString().trim();
                if (TextUtils.isEmpty(noteContent) || hasSignOnly(noteContent)) {
                    deleteNoteFromDb();
                    changed = true;
                } else {
                    if (hasUpdateNote()) {
                        updateToDb();
                        changed = true;
                    }
                }
            } else if (!hasSignOnly(content.getText().toString().trim())) {
                insertToDb();
                changed = true;
            }

            if (changed && !mIsChanged) {
                mIsChanged = true;
            }
        } else {
            Log.e(TAG, "Jim, saveData, db is closed");
        }
    }
    
    private void closeDb() {
        if (noteDb != null) {
            noteDb.close();
            noteDb = null;
        }
        if (mLabelDb != null) {
            mLabelDb.close();
            mLabelDb = null;
        }
    }

    @Override
    public void finish() {
        if (sSaveHandler != null) {
            sSaveHandler.removeCallbacks(mSaveNoteRunnable);
            closeSaveThread();
        }
        saveData();
        closeDb();
        if (mIsChanged) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        super.finish();
    }

    private void insertToDb() {
        String con_str = content.getText().toString();

        if (!TextUtils.isEmpty(con_str.trim())) {
            int image_count = getImageCount(con_str);
            int video_count = getVideoCount(con_str);
            int sound_count = getRecordCount(con_str);

            m_result = new NoteResult();
            m_result.setIs_preset(0);
            m_result.setUuid(UUID.randomUUID().toString());
            m_result.setBackgroundPath(mBackgroudPath);

            String[][] object = {
                    new String[] {
                            Globals.ATTACHMENT_ALL_PATTERN, ""
                    }
            };
            String character = SystemUtils.replace(con_str, object);

            m_result.setContent(con_str);
            m_result.setCharacter(character);
            m_result.setImage_count(image_count);
            m_result.setVideo_count(video_count);
            m_result.setSound_count(sound_count);
            if (warningtime != 0) {
                m_result.setIs_warn(1);
                m_result.setWarn_time(warningtime);
            } else {
                m_result.setIs_warn(0);
                m_result.setWarn_time(warningtime);
            }
            if (!TextUtils.isEmpty(mLabel1)) {
                String label1 = mLabelDb.queryIDByName(mLabel1);
                m_result.setLabel1(label1);
            }
            if (!TextUtils.isEmpty(mLabel2)) {
                String label2 = mLabelDb.queryIDByName(mLabel2);
                m_result.setLabel2(label2);
            }

            long currentMillis = System.currentTimeMillis();
            m_result.setUpdate_time(currentMillis);
            m_result.setCreate_time(currentMillis);

            long noteId = noteDb.insert(m_result);
            NoteAlarmReceiver.scheduleAlarmById((int) noteId, NoteAlarmManager.ACTION_INSERT);

            // 为了实现自动保存，插入完了之后可能需要做更新操作
            m_result.setId((int) noteId);
            if (!TextUtils.isEmpty(mLabel1)) {
                m_result.setLabel1(mLabel1);
            }
            if (!TextUtils.isEmpty(mLabel2)) {
                m_result.setLabel2(mLabel2);
            }
            m_mainResult = m_result;
            initOriginalNoteInfo(m_result);

            Log.d(TAG, "Jim, Note: " + m_mainResult.getId() + " is inserted.");
        }
    }

    private void updateToDb() {
        String con_str = content.getText().toString();
        if (!SystemUtils.isNull(con_str)) {
            int image_count = getImageCount(con_str);
            int video_count = getVideoCount(con_str);
            int sound_count = getRecordCount(con_str);

            m_result = new NoteResult();

            if (m_mainResult.getIs_preset() == 1) {
                m_result.setIs_preset(0);
            }
            if (m_mainResult.getIs_preset() == 2) {
                m_result.setIs_preset(3);
            }
            m_result.setBackgroundPath(mBackgroudPath);

            String[][] object = {
                    new String[] {
                            Globals.ATTACHMENT_ALL_PATTERN, ""
                    }
            };
            String character = SystemUtils.replace(con_str, object);

            m_result.setContent(con_str);
            m_result.setCharacter(character);
            m_result.setImage_count(image_count);
            m_result.setVideo_count(video_count);
            m_result.setSound_count(sound_count);
            if (warningtime != 0) {
                m_result.setIs_warn(1);
                m_result.setWarn_time(warningtime);
            } else {
                m_result.setIs_warn(0);
                m_result.setWarn_time(warningtime);
            }
            if (!TextUtils.isEmpty(mLabel1)) {
                String label1 = mLabelDb.queryIDByName(mLabel1);
                m_result.setLabel1(label1);
            }
            if (!TextUtils.isEmpty(mLabel2)) {
                String label2 = mLabelDb.queryIDByName(mLabel2);
                m_result.setLabel2(label2);
            }

            m_result.setUpdate_time(System.currentTimeMillis());

            noteDb.updateNoteByID(m_result, String.valueOf(m_mainResult.getId()));
            NoteAlarmReceiver.scheduleAlarmById(m_mainResult.getId(), NoteAlarmManager.ACTION_UPDATE);

            // 为下一次自动更新做准备
            m_result.setId(m_mainResult.getId());
            if (!TextUtils.isEmpty(mLabel1)) {
                m_result.setLabel1(mLabel1);
            }
            if (!TextUtils.isEmpty(mLabel2)) {
                m_result.setLabel2(mLabel2);
            }
            m_mainResult = m_result;
            initOriginalNoteInfo(m_result);

            Log.d(TAG, "Jim, Note: " + m_mainResult.getId() + " is updated.");
        }
    }

    private void deleteNoteFromDb() {
        noteDb.deleteDataById(String.valueOf(m_mainResult.getId()));
        NoteAlarmReceiver.scheduleAlarmById(m_mainResult.getId(), NoteAlarmManager.ACTION_DELETE);

        Log.d(TAG, "Jim, Note: " + m_mainResult.getId() + " is deleted.");
        // 为下一次自动保存做准备
        m_mainResult = null;
        mOriginalNoteInfo = null;
    }

    private int getAttachmentCount(String con_str) {
        return getImageCount(con_str) + getVideoCount(con_str) + getRecordCount(con_str);
    }

    private int getImageCount(String con_str) {
        return SystemUtils.getImageCount(con_str);
    }

    private int getVideoCount(String con_str) {
        return SystemUtils.getCount(con_str, Globals.ATTACHMENT_VIDEO_PATTERN);
    }

    private int getRecordCount(String con_str) {
        return SystemUtils.getCount(con_str, Globals.ATTACHMENT_SOUND_PATTERN);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (isMenuCanBeDisplayed()) {
                type = 0;
                if (isInputMethodShow()) {
                    content.setIsHandleEvent(true);
                    closeSoftInputWindow();
                    mPendingRunnable = mShowAuroraMenuCommon;
                    return true;
                } else {
                    initAuroraMenu();
                    // mShowAuroraMenuCommon.run();
                }
            } else {
                return true; // 吃掉事件，阻止菜单弹出
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
        	
        	Log.i(TAG, "zhangwei dialog");
        	
        	if(null == noteDb) {
        		return true;
        	} else {
                handleBack();
                return true;
        	}
        	
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 是否可以显示菜单
     */
    private boolean isMenuCanBeDisplayed() {
        // return !TextUtils.isEmpty(content.getText().toString().trim());
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.note_reminder_ly:
                goToAddOrEditReminder();
                break;
            case R.id.note_label_ly:
                addOrModifyLabel();
                break;
            case R.id.gen_pic:
                generatePicture();
                break;
            case R.id.set_paper:
                gotoSelectPaper();
        }
    }

    private void gotoSelectPaper() {
        Intent intent = new Intent(this, NotePaperChangeActivity.class);
        intent.putExtra(NotePaperChangeActivity.PAPER_NAME, mBackgroudPath);
        startActivityForResult(intent, Globals.REQUEST_CODE_SELECT_PAPER);
    }

    @Override
    protected void onDestroy() {
        NoteImageSpanBitmapCache.getInstance().clear();
        if (sSaveHandler != null) {
            sSaveHandler.removeCallbacks(mSaveNoteRunnable);
            closeSaveThread();
        }
        saveData();
        closeDb();
        super.onDestroy();
    }
}
