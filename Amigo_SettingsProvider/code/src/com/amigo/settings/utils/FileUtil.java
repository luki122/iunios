// Gionee <liuyb> <2013-12-11> add for CR00964937 begin
package com.amigo.settings.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    public static boolean isExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static void copyFile(String sourceFilePath, String targetFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetFilePath);
        copyFile(sourceFile, targetFile);
    }

    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        FileInputStream fin = null;
        FileOutputStream fout = null;
        try {
            fin = new FileInputStream(sourceFile);
            fout = new FileOutputStream(targetFile);
            int bytesRead;
            byte[] buf = new byte[4 * 1024];
            while ((bytesRead = fin.read(buf)) != -1) {
                fout.write(buf, 0, bytesRead);
            }
            fout.flush();
        } finally {
            if (fout != null) {
                fout.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
    }

    public static void del(String filepath) throws IOException {
        File f = new File(filepath);
        if (f.exists()) {
            f.delete();
        }
    }
}
//Gionee <liuyb> <2013-12-11> add for CR00964937 end