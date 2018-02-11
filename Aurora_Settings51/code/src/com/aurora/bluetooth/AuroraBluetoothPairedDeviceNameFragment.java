package com.android.settings.bluetooth;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.settings.R;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraEditText;
import aurora.widget.CustomAuroraActionBarItem;


public class AuroraBluetoothPairedDeviceNameFragment extends Fragment implements TextWatcher,OnClickListener{

    private AuroraEditText mNameText;
    private BluetoothDevice device;
    private CachedBluetoothDevice mCachedDevice;

    private Button mSaveBtn;
    private AuroraActionBar mActionBar;

    private static final int MENU_ID_DONE = Menu.FIRST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable(AuroraDeviceProfilesSettings.EXTRA_DEVICE);
        } else {
            Bundle args = getArguments();
            device = args.getParcelable(AuroraDeviceProfilesSettings.EXTRA_DEVICE);
        }
        if (device == null) {
            Log.w("wolfu", "Device null ,return ");
            return;  // TODO: test this failure path
        }
        LocalBluetoothManager localManager = LocalBluetoothManager.getInstance(getActivity());
        CachedBluetoothDeviceManager deviceManager =
                localManager.getCachedDeviceManager();
        mCachedDevice = deviceManager.findDevice(device);
        if (mCachedDevice == null) {
            getActivity().onBackPressed();
        }

        //Aurora linchunhui modify 20160223 begin
    	mActionBar = ((AuroraActivity)getActivity()).getAuroraActionBar();
    	mActionBar.addItem(AuroraActionBarItem.Type.Done, MENU_ID_DONE);
    	mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        /*
        mActionBar.addItem(R.layout.aurora_actionbar_save, 0);
        CustomAuroraActionBarItem item = (CustomAuroraActionBarItem) mActionBar.getItem(0);
        View view = item.getItemView();
        mSaveBtn = (Button) view.findViewById(R.id.btn_save);
        mSaveBtn.setOnClickListener(this);
        */
        //Aurora linchunhui modify 20160223 end
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
        mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActionBar.setOnAuroraActionBarListener(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(AuroraDeviceProfilesSettings.EXTRA_DEVICE, mCachedDevice.getDevice());
    }

    private String getDeviceName() {
        return getArguments().getString("device_name");
    }

    @Override
    public void onClick(View v) {
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

    /*
     * aurora action bar 点击事件处理方法 linchunhui add 20160223
     */
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int arg0) {
			switch (arg0) {
			case MENU_ID_DONE:
				String name = mNameText.getText().toString();
				String name1 = name;
				if (TextUtils.isEmpty(name)) {
				     Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_empty, Toast.LENGTH_SHORT).show();
				} else if (TextUtils.isEmpty(name1.trim())) {
				     Toast.makeText(getActivity(), R.string.bluetooth_device_name_not_space, Toast.LENGTH_SHORT).show();
				} else if (getDeviceName().equals(name)) {
				     getActivity().onBackPressed();
				} else {
				     mCachedDevice.setName(name);
				     getActivity().onBackPressed();
				} 
				break;
			default:
				break;
			}
		}
    };

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        if(TextUtils.isEmpty(mNameText.getText().toString().trim())){
//    	   mSaveBtn.setImageResource(R.drawable.aurora_wifi_save_unable);
//         mSaveBtn.setOnClickListener(null);
//         mSaveBtn.setEnabled(false);
           mActionBar.getItem(0).getItemView().setEnabled(false);
        }else{
//    	   mSaveBtn.setImageResource(R.drawable.aurora_wifi_save_pressed);
//         mSaveBtn.setOnClickListener(this);
//         mSaveBtn.setEnabled(true);
           mActionBar.getItem(0).getItemView().setEnabled(true);
        }
    }

}
