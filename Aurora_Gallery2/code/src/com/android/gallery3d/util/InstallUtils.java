package com.android.gallery3d.util;

import java.io.File;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class InstallUtils {

	public static void installApp(Context context, String packageName, File apkFile, 
			IPackageInstallObserver.Stub observer) {
		PackageManager pm = context.getPackageManager();
//		if (TextUtils.isEmpty(packageName)) {
//			PackageParser.Package parsed = getPackageInfo(apkFile);
//			packageName = parsed.packageName;
//		}
		int installFlags = 0;
//		try {
			/*
			PackageInfo pi = pm.getPackageInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
					*/
			PackageInfo pi = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_UNINSTALLED_PACKAGES);
			
			if (pi != null) {
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
			}
//		} catch (NameNotFoundException e) {
			//e.printStackTrace();
//		}
		
		Uri mPackageURI = Uri.fromFile(apkFile);
		pm.installPackage(mPackageURI, observer, installFlags, null);
	}
	
	private static PackageParser.Package getPackageInfo(File sourceFile) {
        final String archiveFilePath = sourceFile.getAbsolutePath();
/*        PackageParser packageParser = new PackageParser(archiveFilePath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        PackageParser.Package pkg =  packageParser.parsePackage(sourceFile,
                archiveFilePath, metrics, 0);
        // Nuke the parser reference.
        packageParser = null;*/
        
        /*
        PackageParser.Package pkg = null;
        try {
        	PackageParser packageParser = new PackageParser();
        	pkg = packageParser.parsePackage(sourceFile, PackageParser.PARSE_ON_SDCARD | PackageParser.PARSE_IS_OPERATOR | PackageParser.PARSE_IS_SYSTEM);
        	return pkg;
        } catch (Exception e) {
        }
        return null;
        */
        
        
        //PackageInfo pi = pm.getPackageArchiveInfo(APKFilePath, 0);
        return null;
    }

}
