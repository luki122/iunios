/* This file is auto-generated.  DO NOT MODIFY.
 * Source file: ../Aurora_Browser/code/src/com/android/browser/EventLogTags.logtags
 */

package com.android.browser;

/**
 * @hide
 */
public class EventLogTags {
  private EventLogTags() { }  // don't instantiate

  /** 70103 browser_bookmark_added (url|3), (where|3) */
  public static final int BROWSER_BOOKMARK_ADDED = 70103;

  /** 70104 browser_page_loaded (url|3), (time|2|3) */
  public static final int BROWSER_PAGE_LOADED = 70104;

  /** 70105 browser_timeonpage (url|3), (time|2|3) */
  public static final int BROWSER_TIMEONPAGE = 70105;

  public static void writeBrowserBookmarkAdded(String url, String where) {
    android.util.EventLog.writeEvent(BROWSER_BOOKMARK_ADDED, url, where);
  }

  public static void writeBrowserPageLoaded(String url, long time) {
    android.util.EventLog.writeEvent(BROWSER_PAGE_LOADED, url, time);
  }

  public static void writeBrowserTimeonpage(String url, long time) {
    android.util.EventLog.writeEvent(BROWSER_TIMEONPAGE, url, time);
  }
}
