package gn.com.android.audioprofile.vibrateprofile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import aurora.app.AuroraListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import gn.com.android.audioprofile.R;

public class VibrateProfileActivity extends AuroraListActivity {

    private static final String TAG = "VibrateProfileActivity";
    private Vibrator mVibrator = null;

    private String[] mVibrateList;
    private static final String VIBRATE_FILE_PATH = "system/etc/haptic/";
    private int mSelectPosition = 0;
    private String mVibrateTitile[];
    public static final int[] VIBRATE_TITLE_RES_ID = new int[] {R.string.vibrate_basecall,
            R.string.vibrate_heartbeat, R.string.vibrate_jinglebell, R.string.vibrate_ticktock};

    public static final String[] VIBRATE_FILE_NAME = new String[] {"01.htxt", "02.htxt", "03.htxt", "04.htxt"};

    public static final String AUTOHORITY = "gn.com.android.audioproflie";
    public static Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY + "/vibrateprofile");
    public static Uri CONTENT_URI_1 = Uri.parse("content://" + AUTOHORITY + "/vibrateprofile" + "/01.htxt");
    public static Uri CONTENT_URI_2 = Uri.parse("content://" + AUTOHORITY + "/vibrateprofile" + "/02.htxt");
    public static Uri CONTENT_URI_3 = Uri.parse("content://" + AUTOHORITY + "/vibrateprofile" + "/03.htxt");
    public static Uri CONTENT_URI_4 = Uri.parse("content://" + AUTOHORITY + "/vibrateprofile" + "/04.htxt");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
//        if (GnSettingsThemeUtils.getThemeType(getApplicationContext()).equals(
//                GnSettingsThemeUtils.TYPE_LIGHT_THEME)) {
//            setTheme(R.style.GnSettingsLightTheme);
//        } else {
//            setTheme(R.style.GnSettingsDarkTheme);
//        }
//        super.onCreate(savedInstanceState);
//        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
//        getAuroraActionBar().setDisplayShowHomeEnabled(false);
        setContentView(R.layout.vibrate_profile_layout);

        if (mVibrator == null) {
            mVibrator = ( Vibrator ) getSystemService(Context.VIBRATOR_SERVICE);
        }

        getFileList();
        if (mVibrateList.length < VIBRATE_FILE_NAME.length) {
            Toast.makeText(this, R.string.vibrate_path_error, Toast.LENGTH_SHORT).show();
            finish();
        }

        mVibrateTitile = new String[VIBRATE_TITLE_RES_ID.length];
        for (int i = 0; i < VIBRATE_TITLE_RES_ID.length; i++) {
            mVibrateTitile[i] = getString(VIBRATE_TITLE_RES_ID[i]);
        }

        setListAdapter(new ArrayAdapter<String>(this, R.layout.vibrate_profile_item, mVibrateTitile));

        final ListView listView = getListView();

        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        String vibrateProfileString = Settings.System.getString(getContentResolver(), "vibrate_uri");
        Uri vibrateUri = CONTENT_URI_1;
        Intent intent = getIntent();
        if (intent.hasExtra("vibrate_uri")) {
            vibrateProfileString = intent.getStringExtra("vibrate_uri");
        }

        if (vibrateProfileString != null) {
            vibrateUri = Uri.parse(vibrateProfileString);
        }

        String menuName = null;
        Cursor cursor = getContentResolver().query(vibrateUri, null, null, null, null);
        if (cursor != null && cursor.getCount() >= 1) {
            cursor.moveToFirst();
            menuName = cursor.getString(cursor.getColumnIndex("menu_name"));
        }

        if (menuName == null) {
            menuName = getString(VIBRATE_TITLE_RES_ID[0]);
        } else {
            for (int i = 0; i < VIBRATE_TITLE_RES_ID.length; i++) {
                if (menuName.equals(getString(VIBRATE_TITLE_RES_ID[i]))) {
                    mSelectPosition = i;
                    break;
                }
            }
        }

        listView.setItemChecked(mSelectPosition, true);

        Button okButton = ( Button ) findViewById(R.id.vibrate_ok);
        if (okButton != null) {
            okButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    String uriString = CONTENT_URI.toString() + "/" + VIBRATE_FILE_NAME[mSelectPosition];
                    Settings.System.putString(getContentResolver(), "vibrate_uri", uriString);
                    Intent intent = new Intent();
                    intent.putExtra("vibrate_uri", uriString);
                    // intent.putExtra("styleName",
                    // getString(VIBRATE_TITLE_RES_ID[mSelectPosition]));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }

        Button cancelButton = ( Button ) findViewById(R.id.vibrate_cancel);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    finish();
                }
            });
        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mVibrator.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    protected void getFileList() {
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (!filename.startsWith(".") && filename.toLowerCase().endsWith(".htxt")) {
                    return true;
                }
                return false;
            }
        };

        AssetManager assetManager = getAssets();
        File dir = new File(VIBRATE_FILE_PATH);
        mVibrateList = new String[20];
        mVibrateList = dir.list(filter);
        // Log.d(TAG, "vibrate file total = " + mVibrateList.length);
    }

    @Override
    protected void onListItemClick(aurora.widget.AuroraListView l, View v, int position, long id) {
        // TODO Auto-generated method stub

        // start vibrate
        mVibrator.cancel();
        mSelectPosition = position;
        playVibrate(position);
        super.onListItemClick(l, v, position, id);
    }

    private void playVibrate(int position) {
        if (position >= mVibrateList.length) {
            Toast.makeText(this, getString(R.string.vibrate_path_error, VIBRATE_FILE_PATH.toString()),
                    Toast.LENGTH_SHORT).show();
        }
        String path = VIBRATE_FILE_PATH + VIBRATE_FILE_NAME[position];
        Log.d(TAG, "vibrate file path = " + path);
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, getString(R.string.vibrate_path_error, VIBRATE_FILE_PATH),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String patternString = getStringFromPatternFile(path);
        byte[] pattern = getByteArrayFromString(patternString);

        try {
            Class<?>[] parameterTypes = new Class[1];
            parameterTypes[0] = byte[].class;
            Method method = Vibrator.class.getMethod("vibrateEx", parameterTypes);
            method.invoke(mVibrator, new Object[] {pattern});
            return;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private byte[] getByteArrayFromString(String pattern) {
        final int MAX_LENGTH = 1000;
        byte[] ret = null;
        byte[] buffer = new byte[MAX_LENGTH];
        int buffer_length = 0;

        if (pattern != null) {
            pattern = pattern.replace(',', ' ');
            String[] numbers = pattern.split(" ");
            for (String number : numbers) {
                try {
                    int code = Integer.parseInt(number);
                    if (code >= -127 && code <= 255 && buffer_length < MAX_LENGTH) {
                        buffer[buffer_length++] = ( byte ) code;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        if (buffer_length > 0) {
            ret = new byte[buffer_length];
            for (int i = 0; i < buffer_length; i++) {
                ret[i] = buffer[i];
            }
        }

        return ret;
    }

    private String getStringFromPatternFile(String patternFile) {
        final int MAX_LENGTH = 1000;
        StringBuilder ret = new StringBuilder();
        byte[] buffer = new byte[MAX_LENGTH];
        int buffer_length = 0;

        if (patternFile != null) {
            File file = new File(patternFile);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    line = line.replace(',', ' ');
                    String[] numbers = line.split(" ");
                    for (String number : numbers) {
                        try {
                            int code = Integer.parseInt(number);
                            if (code >= -127 && code <= 255 && buffer_length < MAX_LENGTH) {
                                buffer[buffer_length++] = ( byte ) code;
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (buffer_length > 0) {
            for (int i = 0; i < buffer_length; i++) {
                ret.append(( int ) buffer[i]);
                if (i != buffer_length - 1) {
                    ret.append(", ");
                }
            }
        }

        return ret.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
