package com.mediatek.systemui.ext;

import android.content.Context;
// Aurora <zhanggp> <2013-10-17> modified for systemui begin
//import com.mediatek.pluginmanager.PluginManager;
//import com.mediatek.pluginmanager.Plugin;
//import com.mediatek.pluginmanager.Plugin.ObjectCreationException;
// Aurora <zhanggp> <2013-10-17> modified for systemui end
/**
 * M: Plug-in helper class as the facade for accessing related add-ons.
 */
public class PluginFactory {
    private static IStatusBarPlugin mStatusBarPlugin = null;

    public static synchronized IStatusBarPlugin getStatusBarPlugin(Context context) {
		// Aurora <zhanggp> <2013-10-17> modified for systemui begin
		/*
        if (mStatusBarPlugin == null) {
            try {
                mStatusBarPlugin = (IStatusBarPlugin) PluginManager.createPluginObject(
                        context, IStatusBarPlugin.class.getName(), "1.0.0", Plugin.DEFAULT_HANDLER_NAME);
            } catch (ObjectCreationException e) {
                mStatusBarPlugin = new DefaultStatusBarPlugin(context);
            }
        }

        */
        mStatusBarPlugin = new DefaultStatusBarPlugin(context);
		// Aurora <zhanggp> <2013-10-17> modified for systemui end
        return mStatusBarPlugin;
    }
}
