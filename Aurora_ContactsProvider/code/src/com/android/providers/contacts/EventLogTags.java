/* This file is auto-generated.  DO NOT MODIFY.
 * Source file: ../Aurora_ContactsProvider/code/src/com/android/providers/contacts/EventLogTags.logtags
 */

package com.android.providers.contacts;;

/**
 * @hide
 */
public class EventLogTags {
  private EventLogTags() { }  // don't instantiate

  /** 4100 contacts_upgrade_receiver (time|2|3) */
  public static final int CONTACTS_UPGRADE_RECEIVER = 4100;

  public static void writeContactsUpgradeReceiver(long time) {
    android.util.EventLog.writeEvent(CONTACTS_UPGRADE_RECEIVER, time);
  }
}
