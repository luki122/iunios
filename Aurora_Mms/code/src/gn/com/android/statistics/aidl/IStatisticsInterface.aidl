package gn.com.android.statistics.aidl;
interface IStatisticsInterface {
    boolean getUserImprovementState();
    boolean setUserImprovementState(in boolean state);
    int getDataSize(out int[] number);
    boolean isHasAuthorize(in String packagename);
    void WriteStatistisMessage(String packagename, String versionName, String message);
}