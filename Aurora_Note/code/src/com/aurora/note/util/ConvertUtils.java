
package com.aurora.note.util;

import android.os.Environment;
import android.os.StatFs;
import java.io.File;

public class ConvertUtils {
    public static String generateTimeString(long elapsedTime) {
        int precision = 0;
        if (elapsedTime < 0x36ee80) {
            precision = 1;
        }
        return generateTimeString(elapsedTime, precision);
    }

    public static String generateTimeString(long elapsedTime, int precision) {
        int[] result = new int[8];
        if (elapsedTime > 0L) {
            int millionSecond = (int) elapsedTime % 1000;
            result[7] = (millionSecond / 10) % 10;
            result[6] = millionSecond / 100;

            int second = (int) ((elapsedTime / 1000L) % 60);
            result[5] = second % 10;
            result[4] = second / 10;

            int minute = (int) ((elapsedTime / 60000) % 60);
            result[3] = minute % 10;
            result[2] = minute / 10;

            int hour = (int) (elapsedTime / 3600000);
            result[1] = hour % 10;
            result[0] = hour / 10;
        }

        StringBuffer sb = new StringBuffer();
        switch (precision) {
            case 0:
                sb.append(result[0]).append(result[1]).append(":");
                sb.append(result[2]).append(result[3]).append(":");
                sb.append(result[4]).append(result[5]);
                return sb.toString();
            case 1:
                sb.append(result[2]).append(result[3]).append(":");
                sb.append(result[4]).append(result[5]).append(".");
                sb.append(result[6]).append(result[7]);
                return sb.toString();
            case 2:
                if (result[2] != 0) {
                    sb.append(result[2]);
                }
                sb.append(result[3]);
                return sb.toString();
            case 3:
                if (result[4] != 0) {
                    sb.append(result[4]);
                }
                sb.append(result[5]);
                return sb.toString();
            default:
                return sb.toString();
        }
    }

    @SuppressWarnings("deprecation")
    public static long getAvailableExternalMemorySize() {
        if (!isSDCardReady()) {
            return -1;
        }

        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

    public static boolean isSDCardReady() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public static byte[] trimByteArray(byte[] baseArray, int startIndex, int endIndex,
            int trimLength) {
        byte[] result = null;
        if (baseArray == null) {
            return result;
        }

        if (startIndex < 0) {
            return result;
        }

        if (startIndex >= endIndex) {
            return result;
        }

        if (endIndex - startIndex < trimLength) {
            trimLength = endIndex - startIndex;
        }

        int baseArrayLength = baseArray.length;
        if (baseArrayLength <= trimLength) {
            return baseArray;
        }

        int gap = endIndex - startIndex;
        float seek = gap * 1f / trimLength;
        result = new byte[trimLength];
        float index = startIndex;
        for (int i = 0; i < trimLength; i++) {
            if (index >= baseArrayLength) {
                return result;
            }
            result[i] = baseArray[(int) index];
            index = index + seek;
        }

        return result;
    }
}
