package com.android.systemui.recent.utils;

/**
 * [SystemUI] Support "Toolbar".
 */
public interface Configurable {
    void initConfigurationState();
	// Aurora <Steve.Tang> 2014-08-04 update config view. start
	void updateResources();
	// Aurora <Steve.Tang> 2014-08-04 update config view. end
}