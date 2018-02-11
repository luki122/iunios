package com.aurora.puremanager.traffic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraSpinner;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.mConfig;

public class TrafficLimitActivity extends AuroraActivity implements OnClickListener {
	
	private static final String TAG = "TrafficLimitActivity";
	
	private static final int AURORA_CONFIRM = 100;
	
	private EditText et_totalflow;
	private EditText et_usedflow;
	private AuroraSpinner spinner_totalflow;
	private AuroraSpinner spinner_usedflow;
	private TextView tv_percentvalue;
	private TextView tv_warningvalue;
	private TextView tv_warningvalue_unit;
	private RelativeLayout rl_month_end_date;
	private TextView tv_month_end_date;
	private SeekBar seekBar;
	private Button okBtn;
	
	private String monthOfDateformatStr;
	
	private float mActualFlow = 0;
    private float mDefinedFlow = 0;
    private int mSimIndex = 0;
    private int mCycleDay = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (mConfig.isNative) {
			setContentView(R.layout.trafficassistant_limit_activity);
		} else {
			setAuroraContentView(R.layout.trafficassistant_limit_activity,
					AuroraActionBar.Type.Normal);
			getAuroraActionBar().setTitle(R.string.net_flow_set);
			getAuroraActionBar().setBackgroundResource(R.color.main_title_color);
			
			getAuroraActionBar().addItem(AuroraActionBarItem.Type.Done,  AURORA_CONFIRM);
			getAuroraActionBar().setOnAuroraActionBarListener(auroraActionBarItemClickListener);
		}
		
		initView();
        initData(); 
	}
	
	private void initView() {
		et_totalflow = (EditText) findViewById(R.id.et_totalflow);
		et_usedflow = (EditText) findViewById(R.id.et_usedflow);
		spinner_totalflow = (AuroraSpinner) findViewById(R.id.spinner_totalflow);
		spinner_usedflow = (AuroraSpinner) findViewById(R.id.spinner_usedflow);
		tv_percentvalue = (TextView) findViewById(R.id.tv_percentvalue);
		tv_warningvalue = (TextView) findViewById(R.id.tv_warningvalue);
		tv_warningvalue_unit = (TextView) findViewById(R.id.tv_warningvalue_unit);
		rl_month_end_date = (RelativeLayout) findViewById(R.id.rl_month_end_date);
		tv_month_end_date = (TextView) findViewById(R.id.tv_month_end_date);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		okBtn = (Button) findViewById(R.id.okBtn);
		
		rl_month_end_date.setOnClickListener(this);
		okBtn.setOnClickListener(this);
		
		et_totalflow.addTextChangedListener(mTextWatcher);
		et_usedflow.addTextChangedListener(mTextWatcher);
		seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		
		spinner_totalflow.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				updateWarnningView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	private void initData() {
		monthOfDateformatStr = getString(R.string.net_flow_month_of_date);
		
		// Gionee: mengdw <2015-08-25> modify for CR01543192 begin
		Bundle bundle =  getIntent().getExtras();
        if (bundle != null) {
            // Gionee: mengdw <2015-10-15> modify for CR01568633 begin
            if (TrafficPreference.getSimValue() != null) {
                mSimIndex = bundle.getInt(TrafficPreference.getSimValue(), 0);
            } else {
                Log.d(TAG,"initData TrafficPreference.getSimValue() is null");
                mSimIndex = 0;
            }
            // Gionee: mengdw <2015-10-15> modify for CR01568633 end
            String[] value = bundle.getString(TrafficPreference.getUserDefined(), "0.0:0.0").split(":");
            mActualFlow = Float.valueOf(value[1]);
            mDefinedFlow = Float.valueOf(value[0]);
        } else {
            mActualFlow = 0;
            mDefinedFlow = 0;
        }
        // Gionee: mengdw <2015-08-25> modify for CR01543192 end

        String[] data = TrafficPreference.getPreference(this, mSimIndex);

        if (data[0].toString().isEmpty()) {
            data[0] = "";
            data[1] = "60";
            data[2] = "1";
            data[3] = "0";
        }
        
        if (!TextUtils.isEmpty(data[0])) {
        	// 初始化套餐流量
        	double totalValue = Double.valueOf(data[0]);
        	
        	if (totalValue / Constant.UNIT >= 1) {
        		totalValue = totalValue / Constant.UNIT;
        		
        		String totalValueStr = "";
        		if (totalValue >= 100) {
        			totalValueStr = String.format("%.0f", totalValue);
        		} else if (totalValue >= 10) {
        			totalValueStr = String.format("%.1f", totalValue);
        		} else {
        			totalValueStr = String.format("%.2f", totalValue);
        		}
        		
        		et_totalflow.setText(totalValueStr);
        		et_totalflow.setSelection(totalValueStr.length());
        		spinner_totalflow.setSelection(1);
        	} else {
        		et_totalflow.setText(data[0]);
        		et_totalflow.setSelection(data[0].length());
        	}
        }
        
        // 初始化已用流量
        double usedValue = mActualFlow + mDefinedFlow;
        if (usedValue / Constant.UNIT >= 1) {
        	usedValue = usedValue / Constant.UNIT;
        	String usedValueStr = StringFormat.getStringFormat(usedValue);
        	et_usedflow.setHint(usedValueStr);
        	spinner_usedflow.setSelection(1);
        } else {
        	et_usedflow.setHint(StringFormat.getStringFormat(usedValue));
        }
        
        tv_percentvalue.setText(data[1]);

        mCycleDay = Integer.parseInt(data[2]);
        updateDateText();

        if (!et_totalflow.getText().toString().isEmpty()) {
        	tv_warningvalue
                    .setText(String.valueOf(((1.0 * Double.parseDouble(et_totalflow.getText().toString()) * Double
                    		.parseDouble(tv_percentvalue.getText().toString())) / 100)));
        } else {
        	tv_warningvalue.setText("0");
        }
        
        seekBar.setProgress(Integer.parseInt(tv_percentvalue.getText().toString()));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_month_end_date:
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(TrafficLimitActivity.this);
			String[] items = new String[31];
			for (int i = 0; i < 31; i ++) {
				items[i] = String.format(monthOfDateformatStr, String.valueOf(i + 1));
			}
			builder.setSingleChoiceItems(items, mCycleDay - 1, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int witch) {
					mCycleDay = witch + 1;
					updateDateText();
					dialog.dismiss();
				}
			});
			AuroraAlertDialog dialog = builder.create();
			dialog.setCancelable(false);
			dialog.show();
			break;
		case R.id.okBtn:
			saveTrafficSettings(TrafficLimitActivity.this);
			break;
		}
	}
	
	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case AURORA_CONFIRM:
				saveTrafficSettings(TrafficLimitActivity.this);
			}
		}
	};
	
	private void updateDateText() {
		if (tv_month_end_date != null) {
			tv_month_end_date.setText(String.format(monthOfDateformatStr, mCycleDay));
		}
	}
	
	private void saveTrafficSettings(Context context) {

        if (et_totalflow.getText().toString().isEmpty()) {

            Toast.makeText(context, getString(R.string.info_save_error_no_flow_input), Toast.LENGTH_SHORT)
                    .show();

        } else {

            String[] data = setTrafficSettingsNewData();
            savePreference(context, data);
            onStartFlowMonitor(context);
            updateNotificationBar(context);
            finish();
        }
    }
	
	private void updateNotificationBar(Context context) {
//        TrafficSettingsPrefsFragment.commitTrafficNotiAction(mContext);
    }
	
	private String[] setTrafficSettingsNewData() {
        String[] data = new String[5];

        int[] times = TimeFormat.getNowTimeArray();
        if (spinner_totalflow.getSelectedItemPosition() == 1) {
        	data[0] = String.valueOf((int)(Double.parseDouble(et_totalflow.getText().toString()) * Constant.UNIT));
        } else {
//        	data[0] = et_totalflow.getText().toString();
        	data[0] = String.valueOf((int)Double.parseDouble(et_totalflow.getText().toString()));
        }
        data[1] = tv_percentvalue.getText().toString();
        data[2] = String.valueOf(mCycleDay);
        
        String mUsedFlow = et_usedflow.getText().toString().isEmpty() ? String.valueOf(Float.valueOf(et_usedflow.getHint()
                .toString())) : String.valueOf(Float.valueOf(et_usedflow.getText().toString()));
        double mUsedFlowDouble = Double.valueOf(mUsedFlow);
        if (spinner_usedflow.getSelectedItemPosition() == 1) {
        	mUsedFlowDouble = mUsedFlowDouble * Constant.UNIT;
        }
        mUsedFlowDouble = mUsedFlowDouble - mActualFlow;
        data[3] = StringFormat.getStringFormat(mUsedFlowDouble);
        data[4] = String.valueOf(times[0]) + "-" + String.valueOf(times[1]) + "-" + String.valueOf(times[2])
                + "-" + String.valueOf(times[3]) + "-" + String.valueOf(times[4]) + "-"
                + String.valueOf(times[5]);
        return data;
    }
	
	private void savePreference(Context context, String[] data) {
        TrafficPreference.setPreference(context, mSimIndex, data);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(TrafficPreference.getSimSetting(mSimIndex), true);
        TrafficAssistantMainActivity.setTrafficSettings(mSimIndex, true);// 2;
        editor.putInt(TrafficPreference.getSimFlowlinkFlag(mSimIndex), 0);
        editor.putBoolean(TrafficPreference.getSimReset(mSimIndex), true);
        editor.putBoolean(TrafficPreference.getSimStopWarning(mSimIndex), false);
        editor.commit();
    }
	
	private void onStartFlowMonitor(final Context context) {
        if (isActivated(context, mSimIndex)) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    TrafficProcessorService.processIntent(context, false);
                    TrafficProcessorService.processIntent(context, true);
                }
            }).start();
        }
    }
	
	private boolean isActivated(Context context, int simIndex) {
        SIMInfoWrapper simInfo = SIMInfoWrapper.getDefault(context);
        int activatedSimIndex = simInfo.getSimIndex_CurrentNetworkActivated();

        return simInfo.gprsIsOpenMethod("getMobileDataEnabled") && !simInfo.isWiFiActived()
                && (activatedSimIndex == simIndex);
    }
	
	private void updateWarnningView() {
		double value = 0;
        tv_percentvalue.setText(String.valueOf(seekBar.getProgress()));
        if (!et_totalflow.getText().toString().isEmpty()) {
            value = (Double.parseDouble(tv_percentvalue.getText().toString()) * 1.0f / 100)
                    * Double.parseDouble(et_totalflow.getText().toString());
            if (spinner_totalflow.getSelectedItemPosition() == 1) {
            	value = value * Constant.UNIT;
            }
        }
        if (value / Constant.UNIT >= 1) {
        	value = value / Constant.UNIT;
        	tv_warningvalue_unit.setText(getString(R.string.net_flow_gb));
        } else {
        	tv_warningvalue_unit.setText(getString(R.string.net_flow_mb));
        }
        
        tv_warningvalue.setText(String.format("%.2f", value));
	}
	
	TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (et_totalflow.isFocused() && !s.toString().isEmpty()) {
                if (Float.valueOf(s.toString()) == 0) {
                    s.replace(0, s.length(), "");
                } else if (s.toString().startsWith("0")) {
                    s.replace(0, s.length(), String.valueOf(Double.valueOf(s.toString())));
                }

                updateWarnningView();
            //Gionee: mengdw <2015-09-17> modify for CR01555074  begin
            } else if(s.toString().isEmpty()) {
            	tv_warningvalue.setText("0");
            }
            //Gionee: mengdw <2015-09-17> add log for CR01555074  end

//            if (et_usedflow.isFocused() && !s.toString().isEmpty()) {
//                if (s.toString().startsWith(".")) {
//                    s.replace(0, 1, "0.");
//                } else if (s.toString().length() > 1 && !s.toString().startsWith("0.")) {
//                    if (Float.valueOf(s.toString()) == 0) {
//                        s.replace(0, s.length(), "0");
//                    } else if (s.toString().startsWith("0")) {
//                        if (s.toString().contains(".")) {
//                            s.replace(0, s.length(), String.valueOf(Float.valueOf(s.toString())));
//                        } else {
//                            s.replace(0, s.length(), String.valueOf(Double.valueOf(s.toString())));
//                        }
//                        // s.replace(0, s.length(),
//                        // String.valueOf(Float.valueOf(s.toString())));
//                    }
//                }
//            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

        }

    };
    
    OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // TODO Auto-generated method stub
        	updateWarnningView();
        }
        
    };

}
