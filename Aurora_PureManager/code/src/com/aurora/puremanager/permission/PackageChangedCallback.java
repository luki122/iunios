package com.aurora.puremanager.permission;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-1-16 Change List:
 */
public interface PackageChangedCallback {
    /**
     * Receiver new package add, can implement this method handle.
     * 
     * @param pkgName
     *            Package name.
     */
    public void addPackage(String pkgName);

    /**
     * Receiver package remove, can implement this method handle.
     * 
     * @param pkgName
     *            package name
     */
    public void removePackage(String pkgName);

}
