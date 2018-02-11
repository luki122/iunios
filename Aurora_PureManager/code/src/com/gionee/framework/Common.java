package com.gionee.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.text.TextUtils;

public class Common {
    public static final String MTK_PLATFORM = "MTK";
    public static final String QCOM_PLATFORM = "QCOM";
    private static String platform = "MTK";
    private static final String STR_PROC_CPUINFO = "/proc/cpuinfo";

    static {
        String cpuInfo = getHardwareFromCpuInfo();
        if (cpuInfo.contains("MT")) {
            platform = MTK_PLATFORM;
        }
        if (cpuInfo.contains("MSM")) {
            platform = QCOM_PLATFORM;
        }
    }

    public static String getPlatform() {
        return platform;
    }

    private static String getHardwareFromCpuInfo() {
        String cpuInfo = null;
        try {
            File file = new File(STR_PROC_CPUINFO);
            if (file.isFile() && file.exists()) {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String tmpLine;
                while ((tmpLine = br.readLine()) != null) {
                    if (!TextUtils.isEmpty(tmpLine) && tmpLine.contains("Hardware")) {
                        cpuInfo = tmpLine;
                        break;
                    }
                }
                br.close();
                isr.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuInfo;
    }
}
