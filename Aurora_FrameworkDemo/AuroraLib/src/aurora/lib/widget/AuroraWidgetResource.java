package aurora.lib.widget;

import android.content.Context;
import android.util.Log;

public class AuroraWidgetResource {

    public static int getIdentifierById(Context context, String idName) {
        if (context == null) {
            Log.e("GnWidget", "context is null");
            return 0;
        }
        return context.getResources().getIdentifier(idName, "id", context.getPackageName());
    }
}
