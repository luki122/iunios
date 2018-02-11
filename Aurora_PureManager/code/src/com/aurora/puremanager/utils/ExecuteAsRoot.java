package com.aurora.puremanager.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExecuteAsRoot {

    private static String TAG = "ExecuteAsRoot";

    public static void killBackgroundProcesses(int processId) {
        Log.e(TAG, "killBackgroundProcesses");
        try {
            Log.e(TAG, "killBackgroundProcesses: " + ExecuteAsRoot.execute("kill -9 " + processId));
        } catch (Exception e) {
            Log.e(TAG, "killBackgroundProcesses throw exception");
            return;
        }
    }

    public static final String execute(String currCommand) {
        String result = null;
        List<String> errorMsg = new ArrayList<String>();
        try {
            if (null != currCommand) {
                Process process = Runtime.getRuntime().exec("amigosu");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(currCommand + "\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                        "UTF-8"));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();

                BufferedReader errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream(), "UTF-8"));
                String s;
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.add(s);
                }
                errorResult.close();
                Log.e(TAG, "error info = " + errorMsg);

                try {
                    int suProcessRetval = process.waitFor();
                    if (255 != suProcessRetval) {
                        result = output.toString();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error executing root action", ex);
                }
            }
        } catch (IOException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error executing internal operation", ex);
        }

        return result;
    }

    public static final String execute(ArrayList<String> commands) {
        String result = null;
        try {
            if (null != commands && commands.size() > 0) {
                Process process = Runtime.getRuntime().exec("amigosu");

                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                        "UTF-8"));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();

                try {
                    int suProcessRetval = process.waitFor();
                    if (255 != suProcessRetval) {
                        result = output.toString();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error executing root action", ex);
                }
            }
        } catch (IOException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error executing internal operation", ex);
        }

        return result;
    }

    public static String do_exec(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data, "UTF-8");
        } catch (Exception e) {
            Log.w(TAG, "Can't get root access", e);
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
}
