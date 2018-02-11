package com.aurora.frameworkdemo;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraMultipleChoiceListener;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar.Type;

public class AuroraDialogDemo extends AuroraActivity {
	private static final int DIALOG_YES_NO_MESSAGE = 1;
    private static final int DIALOG_YES_NO_LONG_MESSAGE = 2;
    private static final int DIALOG_LIST = 3;
    private static final int DIALOG_PROGRESS = 4;
    private static final int DIALOG_SINGLE_CHOICE = 5;
    private static final int DIALOG_MULTIPLE_CHOICE = 6;
    private static final int DIALOG_TEXT_ENTRY = 7;
    private static final int DIALOG_MULTIPLE_CHOICE_CURSOR = 8;
    private static final int DIALOG_YES_NO_ULTRA_LONG_MESSAGE = 9;
    private static final int DIALOG_YES_NO_OLD_SCHOOL_MESSAGE = 10;
    private static final int DIALOG_WITH_TITLE_DIVIDER = 11;
    private static final int DIALOG_MULTIPLE_CHOICE_WITH_ADD_ITEM = 12;
    private static final int DIALOG_YES_NO_DEFAULT_DARK_MESSAGE = 13;
    private static final int DIALOG_PROGRESS_SPINNER = 14;

    private static final int MAX_PROGRESS = 100;
    private AuroraProgressDialog mProgressSpinnerDialog;
    private AuroraProgressDialog mProgressDialog;
    private int mProgress;
    private Handler mProgressHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.dialog_demo, Type.Normal);
		getAuroraActionBar().setTitle(R.string.dialog_demo_title);
		  /* Display a text message with yes/no buttons and handle each message as well as the cancel action */
        Button twoButtonsTitle = (Button) findViewById(R.id.two_buttons);
        twoButtonsTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_YES_NO_MESSAGE);
            }
        });
        
        /* Display a long text message with yes/no buttons and handle each message as well as the cancel action */
        Button twoButtons2Title = (Button) findViewById(R.id.two_buttons2);
        twoButtons2Title.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_YES_NO_LONG_MESSAGE);
            }
        });
        
        
        /* Display an ultra long text message with yes/no buttons and handle each message as well as the cancel action */
        Button twoButtons2UltraTitle = (Button) findViewById(R.id.two_buttons2ultra);
        twoButtons2UltraTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_YES_NO_ULTRA_LONG_MESSAGE);
            }
        });


        /* Display a list of items */
        Button selectButton = (Button) findViewById(R.id.select_button);
        selectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_LIST);
            }
        });
        
        /* Display a custom progress bar */
        Button progressButton = (Button) findViewById(R.id.progress_button);
        progressButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_PROGRESS);
                mProgress = 0;
                mProgressDialog.setProgress(0);
                mProgressHandler.sendEmptyMessage(0);
            }
        });

        /* Display a custom progress bar */
        Button progressSpinnerButton = (Button) findViewById(R.id.progress_spinner_button);
        progressSpinnerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_PROGRESS_SPINNER);
            }
        });
        
        /* Display a radio button group */
        Button radioButton = (Button) findViewById(R.id.radio_button);
        radioButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_SINGLE_CHOICE);
            }
        });
        
        /* Display a list of checkboxes */
        Button checkBox = (Button) findViewById(R.id.checkbox_button);
        checkBox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_MULTIPLE_CHOICE);
            }
        });
        
        /* Display a list of checkboxes, backed by a cursor */
        Button checkBox2 = (Button) findViewById(R.id.checkbox_button2);
        checkBox2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_MULTIPLE_CHOICE_CURSOR);
            }
        });

        /* Display a text entry dialog */
        Button textEntry = (Button) findViewById(R.id.text_entry_button);
        textEntry.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_TEXT_ENTRY);
            }
        });
        
        /* Two points, in the traditional theme */
        Button twoButtonsOldSchoolTitle = (Button) findViewById(R.id.two_buttons_old_school);
        twoButtonsOldSchoolTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_YES_NO_OLD_SCHOOL_MESSAGE);
            }
        });
        
        /* Two points, in the light holographic theme */
        Button twoButtonsHoloLightTitle = (Button) findViewById(R.id.two_buttons_holo_light);
        twoButtonsHoloLightTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_WITH_TITLE_DIVIDER);
            }
        });

        /* Two points, in the light default theme */
        Button twoButtonsDefaultLightTitle = (Button) findViewById(R.id.two_buttons_default_light);
        twoButtonsDefaultLightTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_MULTIPLE_CHOICE_WITH_ADD_ITEM);
            }
        });

        /* Two points, in the dark default theme */
        Button twoButtonsDefaultDarkTitle = (Button) findViewById(R.id.two_buttons_default_dark);
        twoButtonsDefaultDarkTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_YES_NO_DEFAULT_DARK_MESSAGE);
            }
        });
        
        mProgressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mProgress >= MAX_PROGRESS) {
                    mProgressDialog.dismiss();
                } else {
                    mProgress++;
                    mProgressDialog.incrementProgressBy(1);
                    mProgressHandler.sendEmptyMessageDelayed(0, 100);
                }
            }
        };
    }
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		  switch (id) {
	        case DIALOG_YES_NO_MESSAGE:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_two_buttons_title)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked OK so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Cancel so do some stuff */
	                    }
	                })
	                .create();
	        case DIALOG_YES_NO_OLD_SCHOOL_MESSAGE:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this, AuroraAlertDialog.THEME_TRADITIONAL)
	                .setIconAttribute(android.R.attr.alertDialogIcon)
	                .setTitle(R.string.alert_dialog_two_buttons_title)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    }
	                })
	                .create();
	        case DIALOG_WITH_TITLE_DIVIDER:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this, AuroraAlertDialog.THEME_HOLO_LIGHT)
	                .setTitle("Dialog with title Divider")
	                .setMessage("This is Dialog msg")
	                .setTitleDividerVisible(true)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    }
	                })
	                .create();
	        case DIALOG_MULTIPLE_CHOICE_WITH_ADD_ITEM:
	        	 return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_multi_choice)
	                .setShowAddItemViewInMultiChoiceMode(true)
	                .setMultiChoiceItems(R.array.select_dialog_items4,
	                        new boolean[]{false, true, false, true, false, false, false},
	                        new AuroraMultipleChoiceListener() {//listener must be aurora.app.AuroraMultipleChoiceListener
	                            public void onClick(DialogInterface dialog, int whichButton,
	                                    boolean isChecked) {

	                                /* User clicked on a check box do some stuff */
	                            }

								@Override
								public void onClick(DialogInterface dialog, int which, boolean isEqual,CharSequence itemText) {
									// TODO Auto-generated method stub
									//save the item data here,this method called when click save button
									Toast.makeText(AuroraDialogDemo.this, itemText, Toast.LENGTH_SHORT).show();
								}

								@Override
								public void onInput(EditText edit,Button addBtn) {
									// TODO Auto-generated method stub
									//this method called when input text into edittext
								}
	                        })
	                .setPositiveButton(R.string.alert_dialog_ok,
	                        new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Yes so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel,
	                        new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked No so do some stuff */
	                    }
	                })
	               .create();
	        case DIALOG_YES_NO_DEFAULT_DARK_MESSAGE:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this, AuroraAlertDialog.THEME_DEVICE_DEFAULT_DARK)
	                .setTitle(R.string.alert_dialog_two_buttons_title)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    }
	                })
	                .create();
	        case DIALOG_YES_NO_LONG_MESSAGE:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_two_buttons_msg)
	                .setMessage(R.string.alert_dialog_two_buttons2_msg)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	    
	                        /* User clicked OK so do some stuff */
	                    }
	                })
	                .setNeutralButton(R.string.alert_dialog_something, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Something so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Cancel so do some stuff */
	                    }
	                })
	                .create();
	        case DIALOG_YES_NO_ULTRA_LONG_MESSAGE:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_two_buttons_msg)
	                .setMessage(R.string.alert_dialog_two_buttons2ultra_msg)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked OK so do some stuff */
	                    }
	                })
	                .setNeutralButton(R.string.alert_dialog_something, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Something so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Cancel so do some stuff */
	                    }
	                })
	                .create();
	        case DIALOG_LIST:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.select_dialog)
	                .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {

	                        /* User clicked so do some stuff */
	                        String[] items = getResources().getStringArray(R.array.select_dialog_items);
	                        new AlertDialog.Builder(AuroraDialogDemo.this)
	                                .setMessage("You selected: " + which + " , " + items[which])
	                                .show();
	                    }
	                })
	                .create();
	        case DIALOG_PROGRESS:
	            mProgressDialog = new AuroraProgressDialog(AuroraDialogDemo.this);
	            mProgressDialog.setTitle(R.string.select_dialog);
	            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            mProgressDialog.setMax(MAX_PROGRESS);
	            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
	                    getText(R.string.alert_dialog_hide), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {

	                    /* User clicked Yes so do some stuff */
	                }
	            });
	            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
	                    getText(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {

	                    /* User clicked No so do some stuff */
	                }
	            });
	            return mProgressDialog;
	        case DIALOG_PROGRESS_SPINNER:
	            mProgressSpinnerDialog = new AuroraProgressDialog(AuroraDialogDemo.this);
	            mProgressSpinnerDialog.setTitle(R.string.select_dialog);
	            mProgressSpinnerDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            return mProgressSpinnerDialog;
	        case DIALOG_SINGLE_CHOICE:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_single_choice)
	                .setSingleChoiceItems(R.array.select_dialog_items2, 0, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked on a radio button do some stuff */
	                    }
	                })
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Yes so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked No so do some stuff */
	                    }
	                })
	               .create();
	        case DIALOG_MULTIPLE_CHOICE:
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_multi_choice)
	                .setMultiChoiceItems(R.array.select_dialog_items3,
	                        new boolean[]{false, true, false, true, false, false, false},
	                        new DialogInterface.OnMultiChoiceClickListener() {
	                            public void onClick(DialogInterface dialog, int whichButton,
	                                    boolean isChecked) {

	                                /* User clicked on a check box do some stuff */
	                            }
	                        })
	                .setPositiveButton(R.string.alert_dialog_ok,
	                        new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Yes so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel,
	                        new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked No so do some stuff */
	                    }
	                })
	               .create();
	        case DIALOG_MULTIPLE_CHOICE_CURSOR:
	            String[] projection = new String[] {
	                    ContactsContract.Contacts._ID,
	                    ContactsContract.Contacts.DISPLAY_NAME,
	                    ContactsContract.Contacts.SEND_TO_VOICEMAIL
	            };
	            Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI,
	                    projection, null, null, null);
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_multi_choice_cursor)
	                .setMultiChoiceItems(cursor,
	                        ContactsContract.Contacts.SEND_TO_VOICEMAIL,
	                        ContactsContract.Contacts.DISPLAY_NAME,
	                        new DialogInterface.OnMultiChoiceClickListener() {
	                            public void onClick(DialogInterface dialog, int whichButton,
	                                    boolean isChecked) {
	                                Toast.makeText(AuroraDialogDemo.this,
	                                        "Readonly Demo Only - Data will not be updated",
	                                        Toast.LENGTH_SHORT).show();
	                            }
	                        })
	               .create();
	        case DIALOG_TEXT_ENTRY:
	            // This example shows how to add a custom layout to an AlertDialog
	            LayoutInflater factory = LayoutInflater.from(this);
	            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
	            return new AuroraAlertDialog.Builder(AuroraDialogDemo.this)
	                .setTitle(R.string.alert_dialog_text_entry)
	                .setView(textEntryView)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	    
	                        /* User clicked OK so do some stuff */
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked cancel so do some stuff */
	                    }
	                })
	                .create();
	        }
	        return null;
	}
	
	
	
}
