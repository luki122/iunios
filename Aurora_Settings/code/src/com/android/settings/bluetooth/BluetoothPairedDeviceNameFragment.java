package com.android.settings.bluetooth;

import com.android.settings.R;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraActionBar;
import aurora.widget.CustomAuroraActionBarItem;
import aurora.app.AuroraActivity;

// Aurora liugj 2013-10-22 created for aurora's new feature
public class BluetoothPairedDeviceNameFragment extends Fragment implements TextWatcher,OnClickListener{
	
	private AuroraEditText mNameText;
	private BluetoothDevice device;
	private CachedBluetoothDevice mCachedDevice;
	// qy
	private Button mSaveBtn;
	private AuroraActionBar mActionBar;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable(DeviceProfilesSettings.EXTRA_DEVICE);
        } else {
            Bundle args = getArguments();
            device = args.getParcelable(DeviceProfilesSettings.EXTRA_DEVICE);
        }
		LocalBluetoothManager localManager = LocalBluetoothManager.getInstance(getActivity());
		CachedBluetoothDeviceManager deviceManager =
				localManager.getCachedDeviceManager();
		mCachedDevice = deviceManager.findDevice(device);
		if (mCachedDevice == null) {
			getActivity().onBackPressed();
		}
		
		//qy
		mActionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
		mActionBar.addItem(R.layout.aurora_actionbar_save, 0);
        CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) mActionBar.getItem(0);
        View view = item.getItemView();
        mSaveBtn = (Button) view.findViewById(R.id.btn_save);
        mSaveBtn.setOnClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.aurora_device_name_layout, container, false);
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
    }
	
	@Override
    public void onPause() {
        super.onPause();
    }
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DeviceProfilesSettings.EXTRA_DEVICE, mCachedDevice.getDevice());
    }
	
	private String getDeviceName() {
        return getArguments().getString("device_name");
    }
	
	@Override
	public void onClick(View v) {
		// modify qy
		/*switch (v.getId()) {
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
            } else if (getDeviceName().equals(name)) {
				getActivity().onBackPressed();
			}else {
	        	mCachedDevice.setName(name);
				getActivity().onBackPressed();
			}
			break;
			
		default:
			break;
		}*/
		
		String name = mNameText.getText().toString();
        String name1 = name;
		if (TextUtils.isEmpty(name)) {
			Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_empty, Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(name1.trim())) {
            Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_space, Toast.LENGTH_SHORT).show();
        } else if (getDeviceName().equals(name)) {
			getActivity().onBackPressed();
		}else {
        	mCachedDevice.setName(name);
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
	
}
