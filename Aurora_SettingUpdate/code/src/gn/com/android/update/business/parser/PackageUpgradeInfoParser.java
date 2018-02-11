package gn.com.android.update.business.parser;

import gn.com.android.update.business.PackageUpgradeInfo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

public class PackageUpgradeInfoParser {

    private static final String TAG = "AppUpgradeInfoParser";
    
    public static List<PackageUpgradeInfo> parseOtaUpgradeInfo(String data) throws JSONException {
        List<PackageUpgradeInfo> appsList = new ArrayList<PackageUpgradeInfo>();
        return appsList;
    }
   
}
