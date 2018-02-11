package com.android.settings.bluetooth;

import aurora.app.AuroraAlertDialog;
//Gionee:zhang_xin 2013-01-16 add for CR00746738 start
import aurora.app.AuroraAlertDialog.Builder;
//Gionee:zhang_xin 2013-01-16 add for CR00746738 end
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.android.settings.R;

public final class ErrorDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    private static final String KEY_ERROR = "errorMessage";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String message = getArguments().getString(KEY_ERROR);

        //Gionee:zhang_xin 2013-01-16 modify for CR00746738 start
        /*
        return new  AuroraAlertDialog.Builder(getActivity())
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.bluetooth_error_title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .show();
        */
		// Aurora liugj 2013-10-22 modified for aurora's new feature start
        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.bluetooth_error_title)
                    .setMessage(message)
                    /*.setPositiveButton(android.R.string.ok, null)*/
                    .create();
        dialog.setCanceledOnTouchOutside(true);
		// Aurora liugj 2013-10-22 modified for aurora's new feature end
        return dialog;
        //Gionee:zhang_xin 2013-01-16 modify for CR00746738 end
    }

    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        
    }
}
