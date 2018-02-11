/**
 * 
 */
package com.aurora.plugin;

class StatusSet {
	public  boolean mIsHotseatDynamic = false;
	public  boolean mIsDesktopDynamic = false;
	public  boolean mIsScreenOn = true;
	public  boolean mIsLauncherRunning = true;
	
	//At present we do not enable this flag
	//It is for future use.
	public	boolean mIsFolderWorkspaceDynamic = false;

	public	boolean mIsFolderWorkspaceOpen = false;
	
	public StatusSet() {
	}
}

/**
 * Vulcan added these code in 上午10:37:55
 *
 */
public class DynIconDriver {
	
	public static final StatusSet statusSet = new StatusSet();

	/**
	 * 
	 */
	public DynIconDriver() {
		// TODO Auto-generated constructor stub
	}
	
	public static void setFolderWorksapceSwitch(boolean isDynamic) {
		statusSet.mIsFolderWorkspaceDynamic = isDynamic;
		onStatusChange(statusSet);
	}
	
	
	public static void setFolderWorksapceOpenSwitch(boolean isOpen) {
		statusSet.mIsFolderWorkspaceOpen = isOpen;
		onStatusChange(statusSet);
	}

	public static void setHotseatSwitch(boolean isDynamic) {
		statusSet.mIsHotseatDynamic = isDynamic;
		onStatusChange(statusSet);
	}
	
	public static void setDesktopSwitch(boolean isDynamic) {
		statusSet.mIsDesktopDynamic = isDynamic;
		onStatusChange(statusSet);
	}
	
	public static void setScreenSwitch(boolean isOn) {
		statusSet.mIsScreenOn = isOn;
		onStatusChange(statusSet);
	}
	
	public static void setRunningSwitch(boolean isLauncherRunning) {
		statusSet.mIsLauncherRunning = isLauncherRunning;
		onStatusChange(statusSet);
	}

	private static boolean onStatusChange(StatusSet newStatus) {
		if(shouldCloseTimer(newStatus)) {
			DynIconPlg.secheduleTimer(false);
			return true;
		}
		DynIconPlg.secheduleTimer(true);
		return true;
	}
	
	private static boolean shouldCloseTimer(StatusSet newStatus) {
		//if launcher is not running on foreground, 
		//or screen is not on,
		//we should close timer of dynamic 
		/*if(!newStatus.mIsLauncherRunning 
			|| !newStatus.mIsScreenOn) {
			return true;
		}
		
		//Since folder is rarely open, we consider timer could be open in this case.
		if(newStatus.mIsHotseatDynamic
				|| newStatus.mIsDesktopDynamic
				|| newStatus.mIsFolderWorkspaceDynamic
				|| newStatus.mIsFolderWorkspaceOpen) {
			return false;
		}*/
		return true;
	}

}
