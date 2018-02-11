package com.android.settings.bluetooth;

import com.android.settings.R;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraActionBar;
import aurora.widget.CustomAuroraActionBarItem;
import aurora.app.AuroraActivity;
import android.preference.PreferenceManager;
import android.graphics.Typeface;

// Aurora liugj 2013-10-22 created for aurora's new feature
public class BluetoothDeviceNameFragment extends Fragment implements TextWatcher,OnClickListener{
	
	private AuroraEditText mNameText;
	private final LocalBluetoothAdapter mLocalAdapter;
	private Button mSaveBtn;
	private AuroraActionBar mActionBar;
	private static String ACTION_BAR_TITLE_FONT = "/system/fonts/title.ttf";
	private static Typeface auroraTitleFace;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                updateDeviceName();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) &&
                    (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) ==
                            BluetoothAdapter.STATE_ON)) {
                updateDeviceName();
            }
        }
    };
    
    public BluetoothDeviceNameFragment() {
    	LocalBluetoothManager localManager = LocalBluetoothManager.getInstance(getActivity());
        mLocalAdapter = localManager.getBluetoothAdapter();
    }

private static Typeface auroraCreateTitleFont( ) {
    	try {
			
			auroraTitleFace = Typeface.createFromFile(ACTION_BAR_TITLE_FONT);
		} catch (Exception e) {
			// TODO: handle exception
			e.getCause();
			e.printStackTrace();
			auroraTitleFace = null;
		}
		
		return auroraTitleFace;
    }


	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mActionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
		mActionBar.addItem(R.layout.aurora_actionbar_save, 0);
        CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) mActionBar.getItem(0);
        View view = item.getItemView();
        mSaveBtn = (Button) view.findViewById(R.id.btn_save);
//		mSaveBtn.setTypeface(auroraCreateTitleFont());   
        mSaveBtn.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String name = mNameText.getText().toString();
        String name1 = name;
		if (TextUtils.isEmpty(name)) {
			Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_empty, Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(name1.trim())) {
            Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_space, Toast.LENGTH_SHORT).show();     
        } else if (getDeviceName() != null && getDeviceName().equals(name)) {
			getActivity().onBackPressed();
		}else {
			setDeviceName(name);
			getActivity().onBackPressed();
		}
	}

	
	public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
       if(TextUtils.isEmpty(mNameText.getText().toString().trim())){
//    	   mSaveBtn.setImageResource(R.drawable.aurora_wifi_save_unable);
    	   mSaveBtn.setOnClickListener(null);
    	   mSaveBtn.setEnabled(false);
       }else{
//    	   mSaveBtn.setImageResource(R.drawable.aurora_wifi_save_pressed);
    	   mSaveBtn.setOnClickListener(this);
    	   mSaveBtn.setEnabled(true);
       }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.aurora_my_bluetooth_device_name, container, false);
		mNameText = (AuroraEditText) root.findViewById(R.id.name_edittext);
		mNameText.addTextChangedListener(this);
		
		mNameText.setText(getDeviceName());
		/*Button cancelButton	= (Button) root.findViewById(R.id.name_cancel);
		Button saveButton	= (Button) root.findViewById(R.id.name_save);
		cancelButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);*/
		getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		return root;
	}
	
	@Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);
    }
	
	@Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }
	
	private String getDeviceName() {
		// note 3 qy add 2014 04 03
        SharedPreferences preferences = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);
        
        String dn = preferences.getString("DEVICE_NAME", "");
        if(dn.trim().equals("")){
        	return mLocalAdapter.getName();
        }else{
        	return dn;
        	
        } // note3 end 2014 04 03
		
    }
	
	private void setDeviceName(String deviceName) {
        mLocalAdapter.setName(deviceName);
        // note 3 qy 2014 04 3
        SharedPreferences preferences = getActivity().getSharedPreferences("iuni", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("DEVICE_NAME", deviceName);
        editor.commit();
    }
	
	private void updateDeviceName() {
        if (mLocalAdapter != null && mLocalAdapter.isEnabled()) {
            mNameText.setText(mLocalAdapter.getName());
        }
    }
	
	/*@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.name_cancel:
			getActivity().onBackPressed();
			break;
			
		case R.id.name_save:
			String name = mNameText.getText().toString();
            String name1 = name;
			if (TextUtils.isEmpty(name)) {
				Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_empty, Toast.LENGTH_SHORT).show();
			} else if (TextUtils.isEmpty(name1.trim())) {
                Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_space, Toast.LENGTH_SHORT).show();     
            } else if (getDeviceName() != null && getDeviceName().equals(name)) {
				getActivity().onBackPressed();
			}else {
				setDeviceName(name);
				getActivity().onBackPressed();
			}
			break;
			
		default:
			break;
		}
		
	}*/
	
}
