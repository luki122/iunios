/**
 * 
 */
package com.aurora.account.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.account.http.data.HttpRequestGetAccountData;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.datauiapi.data.bean.BaseResponseObject;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * 执行后台任务，如果有的话
 * @author JimXia
 *
 * @date 2014年12月12日 上午10:24:18
 */
public class BackgroundWorkService extends Service {
    private static final String TAG = "BackgroundWorkService";
    
    public static final String EXTRA_COMMAND = "command";
    
    public static final int COMMAND_UNKNOW = -1;
    /**向服务器注销*/
    public static final int COMMAND_PERFORM_LOGOUT= 1;
    
    private AccountPreferencesUtil mPref;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mPref = AccountPreferencesUtil.getInstance(this);
        Log.d(TAG, "Jim, onCreate");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Jim, onDestroy");
        super.onDestroy();
    }
    
    private static final class Command {
        int commandId;

        Command(int command) {
            this.commandId = command;
        }
    }
    
    private void handleCommand(Command command) {
        Log.d(TAG, "Jim, onHandleIntent, command: " + command.commandId);
        switch (command.commandId) {
            case COMMAND_PERFORM_LOGOUT:
                new BackgroundWorkTask().execute(command);
                break;
        }
    }
    
    private class BackgroundWorkTask extends AsyncTask<Command, Integer, Void> {
        @Override
        protected Void doInBackground(Command... params) {
            Command com = params[0];
            switch (com.commandId) {
                case COMMAND_PERFORM_LOGOUT:
                    boolean success = false;
                    try {
                        String result = HttpRequestGetAccountData.logout(mPref.getPendingUserId(),
                                mPref.getPendingUserKey());
                        if (!TextUtils.isEmpty(result)) {
                            BaseResponseObject resultObj = parseResponse(result, BaseResponseObject.class);
                            if (resultObj != null && resultObj.getCode() == BaseResponseObject.CODE_SUCCESS) {
                                success = true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Jim, perform logout failed, exception: ", e);
                    } finally {
                        mPref.setPendingUserId("");
                        mPref.setPendingUserKey("");
                    }
                    if (success) {
                        Log.d(TAG, "Jim, perform logout success");
                    } else {
                        Log.d(TAG, "Jim, perform logout failed");
                    }
                    break;
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            // 执行完任务，结束当前服务
            stopSelf();
        }

        @SuppressWarnings("unchecked")
        private <T> T parseResponse(String result, Class<?> cl) {
            ObjectMapper mapper = new ObjectMapper();
        //  result = result.replaceAll("\t", "");
            try {
                mapper.configure(
                        DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                        true);
                mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                mapper.getDeserializationConfig()
                        .set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                                false);
                return (T) mapper.readValue(result, cl);
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int command = intent.getIntExtra(EXTRA_COMMAND, COMMAND_UNKNOW);
        Command comm = new Command(command);
        handleCommand(comm);
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
