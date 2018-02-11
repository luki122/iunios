package com.aurora.email;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import org.apache.james.mime4j.decoder.DecoderUtil;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraMenuBase;

import com.android.mail.ui.WaitFragment;
import com.aurora.email.widget.AuroraComposeAddressView;
import com.aurora.email.widget.AuroraComposeTitle;
import com.aurora.email.widget.AuroraEditTextStateView.AuroraListener;
import com.aurora.email.widget.AuroraEmailScrollView;
import com.aurora.email.widget.AuroraSenderView;
import com.aurora.email.widget.AuroraEmailScrollView.OnInputMethodChangeListener;
import com.aurora.email.widget.AuroraEmailScrollView.onScrollChangedListener;
import com.aurora.email.widget.AuroraFujianView;
import com.aurora.email.widget.FocusControl;
import com.android.mail.MailIntentService;
import com.android.mail.R;
import com.android.mail.browse.MessageHeaderView;
import com.android.mail.compose.ComposeActivity;
import com.android.mail.compose.EmptyService;
import com.android.mail.compose.QuotedTextView.RespondInlineListener;
import com.android.mail.providers.Account;
import com.android.mail.providers.Address;
import com.android.mail.providers.Attachment;
import com.android.mail.providers.Folder;
import com.android.mail.providers.MailAppProvider;
import com.android.mail.providers.MessageModification;
import com.android.mail.providers.ReplyFromAccount;
import com.android.mail.providers.Settings;
import com.android.mail.providers.UIProvider;
import com.android.mail.providers.UIProvider.DraftType;
import com.android.mail.ui.MailActivity;
import com.android.mail.ui.AttachmentTile.AttachmentPreview;
import com.android.mail.utils.AccountUtils;
import com.android.mail.utils.AttachmentUtils;
import com.android.mail.utils.ContentProviderTask;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.MyLog;
import com.android.mail.utils.Utils;
import com.android.mail.providers.Message;
import com.google.common.collect.Sets;
import com.android.mail.compose.QuotedTextView;
import com.android.common.Rfc822Validator;

//lory add 2014.10.30 start
import com.android.mail.analytics.Analytics;
import com.google.common.annotations.VisibleForTesting;
import com.android.common.contacts.DataUsageStatUpdater;
import com.android.email.provider.AuroraAutoCompleteDBHelper;
import com.google.common.collect.Lists;

import android.os.Handler;
import android.os.HandlerThread;

//lory add 2014.10.30 start

public class AuroraComposeActivity extends AuroraActivity implements
		OnClickListener, OnFocusChangeListener, OnInputMethodChangeListener,
		onScrollChangedListener, LoaderManager.LoaderCallbacks<Cursor>,
		FocusControl ,AuroraSenderView.ChangeAccount{

	private static final String TAG = "AuroraComposeActivity";
	private AuroraActionBar mAuroraActionBar;

	private static final String MIME_TYPE_PHOTO = "image/*";
	private static final int RESULT_CAMERA_ADD = 1;
	private static final int RESULT_PICK_ATTACHMENT = 2;
	private static final int RESULT_CREATE_ACCOUNT = 3;
	private static final int RESULT_FILE_ATTACHMENT = 4;
	private static final int RESULT_PICK_CONTACT = 5;
	public static final int SENDVIEW_ENABLE_TRUE = 6;
	public static final int SENDVIEW_ENABLE_FALSE = 3;
	public static final int ATTACHMENT_MORE_SIZE = 10;
	private AuroraEmailScrollView mScrollView;
	private EditText mBodyView;
	private AuroraComposeTitle themeEditText;

	private static final int MSG_SHOW_SMOOTH_VIEW = 100;
	private static final int MSG_SHOW_SMOOTH_CONTENT = 101;

	private static final String SAVE_PATH_DIR = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/DCIM/Camera/IMG_";
	private static final String IMG_SUFFIX = ".jpg";

	private String saveImagePath;
	private AuroraFujianView mAuroraFujianView;
	private AuroraComposeAddressView mAuroraComposeAddressView, mCcAddressView,
			mBccaAddressView;
	private View foucuseView = null;

	public static final String EXTRA_FROM_EMAIL_TASK = "fromemail";
	public static final String EXTRA_ATTACHMENTS = "attachments";
	protected static final String EXTRA_ACTION = "action";

	// Identifiers for which type of composition this is
	public static final int COMPOSE = -1; // 写邮件
	public static final int REPLY = 0; // 回复
	public static final int REPLY_ALL = 1; // 全部回复
	public static final int FORWARD = 2; // 转发
	public static final int EDIT_DRAFT = 3; // 草稿

	private static final String ORIGINAL_DRAFT_MESSAGE = "original-draft-message";
	private static final String EXTRA_IN_REFERENCE_TO_MESSAGE = "in-reference-to-message";
	protected static final String EXTRA_IN_REFERENCE_TO_MESSAGE_URI = "in-reference-to-message-uri";
	private static final String EXTRA_ATTACHMENT_PREVIEWS = "attachmentPreviews";

	public static final String EXTRA_NOTIFICATION_FOLDER = "extra-notification-folder";

	private static final String EXTRA_SELECTED_ACCOUNT = "selectedAccount";

	protected static final String EXTRA_TO = "to";
	private static final String EXTRA_CC = "cc";
	private static final String EXTRA_BCC = "bcc";

	private static final String EXTRA_BODY = "body";
	private static final String EXTRA_QUOTED_TEXT = "quotedText";
	private static final String EXTRA_SUBJECT = "subject";
	public static final String EXTRA_VALUES = "extra-values";

	// List of all the fields
	static final String[] ALL_EXTRAS = { EXTRA_SUBJECT, EXTRA_BODY, EXTRA_TO,
			EXTRA_CC, EXTRA_BCC, EXTRA_QUOTED_TEXT };

	private static final int AUROR_ACTION_BAR_ITEM_SEND_EMAIL = 100;

	private static final int LOADER_ACCOUNT_CURSOR = 1;
	private static final int INIT_DRAFT_USING_REFERENCE_MESSAGE = 2;
	private static final int REFERENCE_MESSAGE_LOADER = 3;

	private Account[] mAccounts;
	protected Account mAccount;
	protected Message mRefMessage;
	private Uri mRefMessageUri;
	private Settings mCachedSettings;
	private boolean mLaunchedFromEmail = false;
	protected int mComposeMode = -1;
	private boolean mForward;
	private boolean mShowQuotedText = false;
	private QuotedTextView mQuotedTextView;
	private boolean mRespondedInline;
	private AuroraSenderView mAuroraSenderView;
	private Rfc822Validator mValidator;
	private String mSignature;
	private Message mDraft;
	private boolean mAddingAttachment;
	private boolean mAttachmentsChanged;
	private boolean mTextChanged;
	private boolean mReplyFromChanged;
	private Object mDraftLock = new Object();
	private ReplyFromAccount mDraftAccount, mReplyFromAccount;
	private ContentValues mExtraValues = null;

	private static final String MAIL_TO = "mailto";
	private static final String UTF8_ENCODING_NAME = "UTF-8";
	protected Bundle mInnerSavedState;
	private static final String KEY_INNER_SAVED_STATE = "compose_state";
	private static final String EXTRA_MESSAGE = "extraMessage";

	private static final String EXTRA_FOCUS_SELECTION_START = "focusSelectionStart";
	private static final String EXTRA_FOCUS_SELECTION_END = "focusSelectionEnd";
	private static final String EXTRA_REQUEST_ID = "requestId";
	private static final String EXTRA_RESPONDED_INLINE = "respondedInline";
	private TextView hinTextView;
	private String mSignatureDefualt;
	private boolean isshowBottomMenu=false;
	
	// -------------------add by tangjie 2014/11/05------------------//
	private TextView mSenderTextView, mSenderNumber;
	private ImageButton mSenderAdd;
	private View sendView;
	
	// -------------------add by tangjie end------------------//
	public static void compose(Context launcher, Account account) {
		launch(launcher, account, null, COMPOSE, null, null, null, null, null /* extraValues */);
	}

	private static void launch(Context launcher, Account account,
			Message message, int action, String toAddress, String body,
			String quotedText, String subject, final ContentValues extraValues) {
		Intent intent = new Intent(launcher, AuroraComposeActivity.class);
		intent.putExtra(EXTRA_FROM_EMAIL_TASK, true);
		intent.putExtra(EXTRA_ACTION, action);
		intent.putExtra(Utils.EXTRA_ACCOUNT, account);
		if (action == EDIT_DRAFT) {
			intent.putExtra(ORIGINAL_DRAFT_MESSAGE, message);
		} else {
			intent.putExtra(EXTRA_IN_REFERENCE_TO_MESSAGE, message);
		}
		if (toAddress != null) {
			intent.putExtra(EXTRA_TO, toAddress);
		}
		if (body != null) {
			intent.putExtra(EXTRA_BODY, body);
		}
		if (quotedText != null) {
			intent.putExtra(EXTRA_QUOTED_TEXT, quotedText);
		}
		if (subject != null) {
			intent.putExtra(EXTRA_SUBJECT, subject);
		}
		if (extraValues != null) {
			intent.putExtra(EXTRA_VALUES, extraValues);
		}
		launcher.startActivity(intent);
	}

	private void saveState(Bundle state) {
		// We have no accounts so there is nothing to compose, and therefore,
		// nothing to save.
		if (mAccounts == null || mAccounts.length == 0) {
			return;
		}
		// The framework is happy to save and restore the selection but only if
		// it also saves and
		// restores the contents of the edit text. That's a lot of text to put
		// in a bundle so we do
		// this manually.
		View focus = getCurrentFocus();
		if (focus != null && focus instanceof EditText) {
			EditText focusEditText = (EditText) focus;
			state.putInt(EXTRA_FOCUS_SELECTION_START,
					focusEditText.getSelectionStart());
			state.putInt(EXTRA_FOCUS_SELECTION_END,
					focusEditText.getSelectionEnd());
		}

		// final List<ReplyFromAccount> replyFromAccounts =
		// mFromSpinner.getReplyFromAccounts();
		// final int selectedPos = mFromSpinner.getSelectedItemPosition();
		/*
		 * final ReplyFromAccount selectedReplyFromAccount = (replyFromAccounts
		 * != null && replyFromAccounts.size() > 0 && replyFromAccounts.size() >
		 * selectedPos) ? replyFromAccounts.get(selectedPos) : null;
		 */
		final ReplyFromAccount selectedReplyFromAccount = mReplyFromAccount;

		if (selectedReplyFromAccount != null) {
			state.putString(EXTRA_SELECTED_REPLY_FROM_ACCOUNT,
					selectedReplyFromAccount.serialize().toString());
			state.putParcelable(Utils.EXTRA_ACCOUNT,
					selectedReplyFromAccount.account);
		} else {
			state.putParcelable(Utils.EXTRA_ACCOUNT, mAccount);
		}

		if (mDraftId == UIProvider.INVALID_MESSAGE_ID && mRequestId != 0) {
			// We don't have a draft id, and we have a request id,
			// save the request id.
			state.putInt(EXTRA_REQUEST_ID, mRequestId);
		}

		// We want to restore the current mode after a pause
		// or rotation.
		int mode = getMode();
		state.putInt(EXTRA_ACTION, mode);

		final Message message = createMessage(selectedReplyFromAccount, mode);
		if (mDraft != null) {
			message.id = mDraft.id;
			message.serverId = mDraft.serverId;
			message.uri = mDraft.uri;
		}
		state.putParcelable(EXTRA_MESSAGE, message);

		if (mRefMessage != null) {
			state.putParcelable(EXTRA_IN_REFERENCE_TO_MESSAGE, mRefMessage);
		} else if (message.appendRefMessageContent) {
			// If we have no ref message but should be appending
			// ref message content, we have orphaned quoted text. Save it.
			state.putCharSequence(EXTRA_QUOTED_TEXT,
					mQuotedTextView.getQuotedTextIfIncluded());
		}
		state.putBoolean(EXTRA_RESPONDED_INLINE, mRespondedInline);
		state.putParcelableArrayList(EXTRA_ATTACHMENT_PREVIEWS,
				mAuroraFujianView.getAttachmentPreviews());

		state.putParcelable(EXTRA_VALUES, mExtraValues);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		final Bundle inner = new Bundle();
		saveState(inner);
		outState.putBundle(KEY_INNER_SAVED_STATE, inner);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {

		super.onRestoreInstanceState(savedInstanceState);
		if (mInnerSavedState != null) {
			if (mInnerSavedState.containsKey(EXTRA_FOCUS_SELECTION_START)) {
				int selectionStart = mInnerSavedState
						.getInt(EXTRA_FOCUS_SELECTION_START);
				int selectionEnd = mInnerSavedState
						.getInt(EXTRA_FOCUS_SELECTION_END);
				// There should be a focus and it should be an EditText since we
				// only save these extras if these conditions are true.

				View focus = getCurrentFocus();
				if (focus != null && focus instanceof EditText) {
					EditText focusEditText = (EditText) focus;
					final int length = focusEditText.getText().length();
					if (selectionStart < length && selectionEnd < length) {
						focusEditText
								.setSelection(selectionStart, selectionEnd);
					}
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.aurora_compose);
		mInnerSavedState = (savedInstanceState != null) ? savedInstanceState
				.getBundle(KEY_INNER_SAVED_STATE) : null;
		initActionbar();
		checkValidAccounts();
	}

	private void checkValidAccounts() {
		final Account[] allAccounts = AccountUtils.getAccounts(this);
		
		if (allAccounts == null || allAccounts.length == 0) {
			final Intent noAccountIntent = MailAppProvider
					.getNoAccountIntent(this);
			if (noAccountIntent != null) {
				mAccounts = null;
				startActivityForResult(noAccountIntent, RESULT_CREATE_ACCOUNT);
			}
		} else {
			// If none of the accounts are syncing, setup a watcher.
			boolean anySyncing = false;
			for (Account a : allAccounts) {
				if (a.isAccountReady()) {
					anySyncing = true;
					break;
				}
			}
			if (!anySyncing) {
				// There are accounts, but none are sync'd, which is just like
				// having no accounts.
				mAccounts = null;
				getLoaderManager()
						.initLoader(LOADER_ACCOUNT_CURSOR, null, this);
				return;
			}
			mAccounts = AccountUtils.getSyncingAccounts(this);
			//MyLog.d(TAG, "allAccounts:" + allAccounts.length+" mAccounts:"+mAccounts);
			finishCreate();
		}
	}

	private void finishCreate() {
		//MyLog.d(TAG, "mAccounts:" + mAccounts.length);
		initView();
		handleIntent();
	}

	@Override
	protected void onDestroy() {
		if (mAuroraFujianView != null)
			mAuroraFujianView.clearCache();
		AuroraAutoCompleteDBHelper.getInstance(this).close(); // ---add by tangjie ---//
		super.onDestroy();
	}

	private static boolean hadSavedInstanceStateMessage(
			final Bundle savedInstanceState) {
		return savedInstanceState != null
				&& savedInstanceState.containsKey(EXTRA_MESSAGE);
	}

	private void handleIntent() {

		final Bundle savedState = mInnerSavedState;
		Intent intent = getIntent();
		Account account = null;
		int action;
		Message message;
		ArrayList<AttachmentPreview> previews;
		mShowQuotedText = false;
		CharSequence quotedText = null;

		if (hadSavedInstanceStateMessage(savedState)) {
			action = savedState.getInt(EXTRA_ACTION, COMPOSE);
			account = savedState.getParcelable(Utils.EXTRA_ACCOUNT);
			message = (Message) savedState.getParcelable(EXTRA_MESSAGE);

			previews = savedState
					.getParcelableArrayList(EXTRA_ATTACHMENT_PREVIEWS);
			mRefMessage = (Message) savedState
					.getParcelable(EXTRA_IN_REFERENCE_TO_MESSAGE);
			quotedText = savedState.getCharSequence(EXTRA_QUOTED_TEXT);

			mExtraValues = savedState.getParcelable(EXTRA_VALUES);
		} else {
			account = obtainAccount(intent);
			action = intent.getIntExtra(EXTRA_ACTION, COMPOSE);
			message = (Message) intent
					.getParcelableExtra(ORIGINAL_DRAFT_MESSAGE);
			previews = intent
					.getParcelableArrayListExtra(EXTRA_ATTACHMENT_PREVIEWS);

			mRefMessage = (Message) intent
					.getParcelableExtra(EXTRA_IN_REFERENCE_TO_MESSAGE);
			mRefMessageUri = (Uri) intent
					.getParcelableExtra(EXTRA_IN_REFERENCE_TO_MESSAGE_URI);
			if (Analytics.isLoggable()) {
				if (intent
						.getBooleanExtra(Utils.EXTRA_FROM_NOTIFICATION, false)) {
					Analytics.getInstance().sendEvent("notification_action",
							"compose", getActionString(action), 0);
				}
			}
		}

		//MyLog.d(TAG, "previews:" + previews);
		mAuroraFujianView.setAttachmentPreviews(previews);
		setAccount(account,true);
		if (mAccount == null) {
			return;
		}

		initRecipients();
		final Folder notificationFolder = intent
				.getParcelableExtra(EXTRA_NOTIFICATION_FOLDER);
		if (notificationFolder != null) {
			final Intent clearNotifIntent = new Intent(
					MailIntentService.ACTION_CLEAR_NEW_MAIL_NOTIFICATIONS);
			clearNotifIntent.setPackage(getPackageName());
			clearNotifIntent.putExtra(Utils.EXTRA_ACCOUNT, account);
			clearNotifIntent.putExtra(Utils.EXTRA_FOLDER, notificationFolder);

			startService(clearNotifIntent);
		}

		if (intent.getBooleanExtra(EXTRA_FROM_EMAIL_TASK, false)) {
			mLaunchedFromEmail = true;
		} else if (Intent.ACTION_SEND.equals(intent.getAction())) {
			final Uri dataUri = intent.getData();
			if (dataUri != null) {
				final String dataScheme = intent.getData().getScheme();
				final String accountScheme = mAccount.composeIntentUri
						.getScheme();
				mLaunchedFromEmail = TextUtils
						.equals(dataScheme, accountScheme);
			}
		}

		if (mRefMessageUri != null) {
			mShowQuotedText = true;
			mComposeMode = action;
			getLoaderManager().initLoader(INIT_DRAFT_USING_REFERENCE_MESSAGE,
					null, this);
			return;
		} else if (message != null && action != EDIT_DRAFT) {
			initFromDraftMessage(message);
			initQuotedTextFromRefMessage(mRefMessage, action);
			// showCcBcc(savedState);
			mShowQuotedText = message.appendRefMessageContent;
			// if we should be showing quoted text but mRefMessage is null
			// and we have some quotedText, display that
			if (mShowQuotedText && mRefMessage == null) {
				if (quotedText != null) {
					initQuotedText(quotedText, false /* shouldQuoteText */);
				} else if (mExtraValues != null) {
					initExtraValues(mExtraValues);
					return;
				}
			}
		} else if (action == EDIT_DRAFT) {
			initFromDraftMessage(message);

			switch (message.draftType) {
			case UIProvider.DraftType.REPLY:
				action = REPLY;
				break;
			case UIProvider.DraftType.REPLY_ALL:
				action = REPLY_ALL;
				break;
			case UIProvider.DraftType.FORWARD:
				action = FORWARD;
				break;
			case UIProvider.DraftType.COMPOSE:
			default:
				action = COMPOSE;
				break;
			}
			mShowQuotedText = message.appendRefMessageContent;
			if (message.refMessageUri != null) {
				// If we're editing an existing draft that was in reference to
				// an existing message,
				// still need to load that original message since we might need
				// to refer to the
				// original sender and recipients if user switches
				// "reply <-> reply-all".
				mRefMessageUri = message.refMessageUri;
				mComposeMode = action;
				getLoaderManager().initLoader(REFERENCE_MESSAGE_LOADER, null,
						this);
				return;
			}
		} else if ((action == REPLY || action == REPLY_ALL || action == FORWARD)) {
			if (mRefMessage != null) {
				initFromRefMessage(action);
				mShowQuotedText = true;
			}
		} else {
			if (initFromExtras(intent)) {
				return;
			}
		}
		mComposeMode = action;
		finishSetup(action, intent, savedState);
		/*MyLog.d(TAG, "account:" + account + " action:" + action + " message:"
				+ message + " mRefMessage:" + mRefMessage + " mRefMessageUri:"
				+ mRefMessageUri + " mCachedSettings:" + mCachedSettings);*/
	}

	private void finishSetup(int action, Intent intent,
			Bundle savedInstanceState) {
		setFocus(action);

		if (!hadSavedInstanceStateMessage(savedInstanceState)) {
			initAttachmentsFromIntent(intent);
		}

		// lory add start for test
		initFromAuroraSpinner(intent.getExtras(), action);
		// lory add end for test

		if (mDraft != null) {
			mDraftAccount = mReplyFromAccount;
		}
	}

	private void initAttachmentsFromIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		//MyLog.d(TAG, "initAttachmentsFromIntent:" + extras);
		if (extras == null) {
			extras = Bundle.EMPTY;
		}
		final String action = intent.getAction();
		if (!mAttachmentsChanged) {
			long totalSize = 0;
			if (extras.containsKey(EXTRA_ATTACHMENTS)) {
				String[] uris = (String[]) extras
						.getSerializable(EXTRA_ATTACHMENTS);
				for (String uriString : uris) {
					final Uri uri = Uri.parse(uriString);
					long size = 0;

					final Attachment a = mAuroraFujianView
							.generateLocalAttachment(uri);
					size = mAuroraFujianView.addAttachment(mAccount, a);

					Analytics.getInstance().sendEvent("send_intent_attachment",
							Utils.normalizeMimeType(a.getContentType()), null,
							size);

					totalSize += size;
				}
			}
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
					ArrayList<Parcelable> uris = extras
							.getParcelableArrayList(Intent.EXTRA_STREAM);
					ArrayList<Attachment> attachments = new ArrayList<Attachment>();
					for (Parcelable uri : uris) {

						final Attachment a = mAuroraFujianView
								.generateLocalAttachment((Uri) uri);
						attachments.add(a);

						Analytics.getInstance().sendEvent(
								"send_intent_attachment",
								Utils.normalizeMimeType(a.getContentType()),
								null, a.size);

					}
					totalSize += addAttachments(attachments);
				} else {
					final Uri uri = (Uri) extras
							.getParcelable(Intent.EXTRA_STREAM);
					long size = 0;
					final Attachment a = mAuroraFujianView
							.generateLocalAttachment(uri);
					if (a != null) {
						size = mAuroraFujianView.addAttachment(mAccount, a);

						Analytics.getInstance().sendEvent(
								"send_intent_attachment",
								Utils.normalizeMimeType(a.getContentType()),
								null, size);
					}
					totalSize += size;
				}
			}

			if (totalSize > 0) {
				mAttachmentsChanged = true;
				mAuroraFujianView.notifyDataSetChanged();
				Analytics.getInstance().sendEvent(
						"send_intent_with_attachments",
						Integer.toString(getAttachments().size()), null,
						totalSize);
			}
		}
	}

	// lory add start for test
	private void initFromAuroraSpinner(Bundle bundle, int action) {
		if (action == EDIT_DRAFT
				&& mDraft.draftType == UIProvider.DraftType.COMPOSE) {
			action = COMPOSE;
		}

		if (bundle != null) {
			if (bundle.containsKey(EXTRA_SELECTED_REPLY_FROM_ACCOUNT)) {
				mReplyFromAccount = ReplyFromAccount.deserialize(mAccount,
						bundle.getString(EXTRA_SELECTED_REPLY_FROM_ACCOUNT));
			} else if (bundle.containsKey(EXTRA_FROM_ACCOUNT_STRING)) {
				final String accountString = bundle
						.getString(EXTRA_FROM_ACCOUNT_STRING);
				// mReplyFromAccount =
				// mFromSpinner.getMatchingReplyFromAccount(accountString);
			}
		}
		if (mReplyFromAccount == null) {
			if (mDraft != null) {
				// mReplyFromAccount = getReplyFromAccountFromDraft(mAccount,
				// mDraft);
			} else if (mRefMessage != null) {
				// mReplyFromAccount = getReplyFromAccountForReply(mAccount,
				// mRefMessage);
			}
		}
		if (mReplyFromAccount == null) {
			mReplyFromAccount = getAuroraDefaultReplyFromAccount(mAccount);
		}
	}

	private static ReplyFromAccount getAuroraDefaultReplyFromAccount(
			final Account account) {
		for (final ReplyFromAccount from : account.getReplyFroms()) {
			if (from.isDefault) {
				return from;
			}
		}
		return new ReplyFromAccount(account, account.uri,
				account.getEmailAddress(), account.name,
				account.getEmailAddress(), true, false);
	}

	// lory add end for test

	private void initView() {
		hinTextView = (TextView) findViewById(R.id.id_hint);
		mAuroraFujianView = (AuroraFujianView) findViewById(R.id.id_fujian);
		mAuroraComposeAddressView = (AuroraComposeAddressView) findViewById(R.id.id_receive_layout);
		mAuroraSenderView = (AuroraSenderView) findViewById(R.id.id_send_layout);
		mAuroraSenderView.setChangAccountListener(this);
		mCcAddressView = (AuroraComposeAddressView) findViewById(R.id.id_cc_layout);
		mCcAddressView.setTitle(getResources().getString(R.string.aurora_cc));
		mBccaAddressView = (AuroraComposeAddressView) findViewById(R.id.id_bcc_layout);
		mQuotedTextView = (QuotedTextView) findViewById(R.id.quoted_text_view);
		mBodyView = (EditText) findViewById(R.id.id_write_email);
		mBccaAddressView
				.setTitle(getResources().getString(R.string.aurora_bcc));

		findViewById(R.id.id_cc_layout_temp).setOnClickListener(this);

		themeEditText = (AuroraComposeTitle) findViewById(R.id.id_theme_info);
		themeEditText.setOnFocusChangeListener(this);
		themeEditText.setFocusControl(this);

		mBodyView.setOnFocusChangeListener(this);
		findViewById(R.id.id_add).setOnClickListener(this);

		findViewById(R.id.id_edite_touch).setOnClickListener(this);

		mScrollView = (AuroraEmailScrollView) findViewById(R.id.id_main);
		mScrollView.setVisibility(View.VISIBLE);
		mScrollView.setOnInputMethodChangeListener(this);
		mScrollView.setonScrollChangedListener(this);
		mAuroraComposeAddressView.setParentScroll(mScrollView);
		mAuroraComposeAddressView.setFocusControl(this);
		mAuroraComposeAddressView.setFocusChangedListener(this);

		mCcAddressView.setParentScroll(mScrollView);
		mCcAddressView.setFocusChangedListener(this);
		mCcAddressView.setFocusControl(this);
		mBccaAddressView.setParentScroll(mScrollView);
		mBccaAddressView.setFocusChangedListener(this);
		mBccaAddressView.setFocusControl(this);
		mQuotedTextView.setRespondInlineListener(mRespondInlineListener);
		// ---------------------------add by tangjie
		// 2014/11/05---------------------------------//
		mSenderTextView = (TextView) findViewById(R.id.aurora_compse_sendertext);
		mSenderNumber = (TextView) findViewById(R.id.aurora_compse_sendernum);
		mAuroraSenderView.setParentScroll(mScrollView);
		// ---------------------------add by tangjie
		// end----------------------------------------//
		initChangeListeners();

		mAuroraFujianView.setFocusControl(this);
	}

	private void initChangeListeners() {
		mBodyView.addTextChangedListener(mBodyTextChangedListener);
	}

	private TextWatcher mBodyTextChangedListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence charsequence, int i, int j, int k) {

		}

		@Override
		public void beforeTextChanged(CharSequence charsequence, int i, int j,
				int k) {

		}

		@Override
		public void afterTextChanged(Editable editable) {
			mTextChanged = true;
			/*MyLog.d(TAG, "mSignatureDefualt:" + mSignatureDefualt
					+ " editable:" + editable.toString());*/
			if (editable.toString().equals(mSignatureDefualt)
					|| editable.length() == 0) {
				hinTextView.setVisibility(View.VISIBLE);
			} else {
				hinTextView.setVisibility(View.GONE);
			}
		}
	};

	private void initActionbar() {

		setAuroraBottomBarMenuCallBack(auroraMenuCallBack);
		mAuroraActionBar = getAuroraActionBar();
		mAuroraActionBar.setTitle(R.string.aurora_compose_title);

		mAuroraActionBar.initActionBottomBarMenu(R.menu.aurora_add_fujian_menu,
				3);
		mAuroraActionBar.setShowBottomBarMenu(false);
		mAuroraActionBar
				.setOnAuroraActionBarListener(mAuroActionBarItemClickListener);
		AuroraActionBarItem item = mAuroraActionBar.addItem(
				R.drawable.aurora_send_icon, AUROR_ACTION_BAR_ITEM_SEND_EMAIL,
				null);
		
		sendView = item.getItemView();
		sendView.setEnabled(false);
		// item.getItemView().setEnabled(false); //置灰
	}

	private OnAuroraActionBarItemClickListener mAuroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case AUROR_ACTION_BAR_ITEM_SEND_EMAIL:
				// 发送
				hideInputMethod(AuroraComposeActivity.this, getCurrentFocus());
				doSend();
				break;

			default:
				break;
			}
		}
	};

	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = new AuroraMenuBase.OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int arg0) {
			mAddingAttachment = true;
			switch (arg0) {
			case R.id.id_camera_add:
				doCameraAttach();
				hideBottomBar(); //add by shihao to hide BottomBar when after onClick add attachment 20150323
				break;
			case R.id.id_image_add:
				doAttach(MIME_TYPE_PHOTO);
				hideBottomBar();//add by shihao to hide BottomBar when after onClick add attachment 20150323
				break;
			case R.id.id_file_add:
				doFileAttach();
				hideBottomBar();//add by shihao to hide BottomBar when after onClick add attachment 20150323
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View arg0) {
		//MyLog.d(TAG, "onClick");
		switch (arg0.getId()) {
		case R.id.id_add:
			View view = getCurrentFocus();
			// if(view!=null&&view.equals(themeEditText)){
			hideInputMethod(this, getCurrentFocus());
			// }
			// themeEditText.requestFocus();
			isshowBottomMenu =true;
			addCoverView();
			//mAuroraActionBar.initActionBottomBarMenu(menuId, 3);
			mAuroraActionBar.setShowBottomBarMenu(mAuroraActionBar
					.isShowBottomBarMenu() ? false : true);
			mAuroraActionBar.showActionBottomeBarMenu();
			break;
		case R.id.id_edite_touch:
			findViewById(R.id.id_write_email).requestFocus();
			showInputMethod(this, getCurrentFocus());
			break;
		case R.id.id_cc_layout_temp:
			findViewById(R.id.id_cc_layout_temp).setVisibility(View.GONE);
			findViewById(R.id.id_cc_layout_parent).setVisibility(View.VISIBLE);
			mCcAddressView.requestFocusFromChild();
			// showInputMethod(this, mCcAddressView.getCurrentFocus());
			// /---modify by tangjie 2014/11/11---/
			break;
		// ----------------add by tangjie 2014/11/11-------------------------//
		/*
		 * case R.id.aurora_btn_add: Intent intent = new
		 * Intent("com.aurora.action.email.select.contact"); ArrayList<String>
		 * mEmailList = new ArrayList<String>(); mEmailList.add("abcd");
		 * //已经填写的邮箱地址 intent.putStringArrayListExtra("emails", mEmailList);
		 * startActivityForResult(intent, 1); break;
		 */
		// ----------------add by tangjie 2014/11/11-------------------------//
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//MyLog.d(TAG, "onActivityResult data:" + data);
		switch (requestCode) {
		case RESULT_FILE_ATTACHMENT:
			if (data == null || resultCode != RESULT_OK
					|| mAuroraFujianView == null) {
				return;
			}
			Uri uri1 = data.getData();
			if (uri1 != null) {
				mAuroraFujianView.addAttachment(mAccount, uri1);
				mAttachmentsChanged = true;
			}
			mAuroraFujianView.notifyDataSetChanged();
			mAddingAttachment = false;
			break;
		case RESULT_PICK_ATTACHMENT:
			if (data == null || resultCode != RESULT_OK
					|| mAuroraFujianView == null) {
				return;
			}
			Bundle bundle = data.getExtras();
			ArrayList<String> arrayList = bundle.getStringArrayList("image");
		//	MyLog.d(TAG, "daxiao:" + arrayList.size());
			for (int i = 0; i < arrayList.size(); i++) {
				String paString = arrayList.get(i);
				
				try {
					if (paString.contains("%") || paString.contains("#")) {
						paString = paString.replaceAll("%", URLEncoder.encode("%", "utf-8")); //注意，一定要先处理完 %，这些是特殊转义符
						paString = paString.replaceAll("#", URLEncoder.encode("#", "utf-8"));
					}
				} catch (UnsupportedEncodingException e) {
			    	
			    }
				
				Uri uri = Uri.parse(FujianInfo.FILE_SCHEMA + "://"
						+ paString);
				
				FujianInfo info = new FujianInfo(uri);
				mAuroraFujianView.addAttachment(mAccount, uri);
			}
			mAttachmentsChanged = true;
			mAuroraFujianView.notifyDataSetChanged();
			// mAuroraFujianView.showFujianLayout();
			mAddingAttachment = false;
			break;
		case RESULT_CAMERA_ADD:
			if (saveImagePath == null || resultCode != RESULT_OK
					|| mAuroraFujianView == null) {
				return;
			}
		//	MyLog.d(TAG, "saveImagePath:" + saveImagePath);
			try {
				if (saveImagePath.contains("%") || saveImagePath.contains("#")) {
					saveImagePath = saveImagePath.replaceAll("%", URLEncoder.encode("%", "utf-8")); //注意，一定要先处理完 %，这些是特殊转义符
					saveImagePath = saveImagePath.replaceAll("#", URLEncoder.encode("#", "utf-8"));
				}
			} catch (UnsupportedEncodingException e) {
		    	
		    }
			Uri uri = Uri.parse(FujianInfo.FILE_SCHEMA + "://" + saveImagePath);
			FujianInfo info = new FujianInfo(uri);
			mAttachmentsChanged = true;
			mAuroraFujianView.addAttachment(mAccount, uri);
			mAuroraFujianView.notifyDataSetChanged();
			// mAuroraFujianView.showFujianLayout();
			mAddingAttachment = false;
			break;
		case RESULT_CREATE_ACCOUNT:
			if (resultCode != RESULT_OK) {
				finish();
			} else {
				// Watch for accounts to show up!
				// restart the loader to get the updated list of accounts
				getLoaderManager()
						.initLoader(LOADER_ACCOUNT_CURSOR, null, this);
				 showWaitFragment(null);
			}
			break;
		case R.id.id_receive_layout:
			if (data == null || resultCode != RESULT_OK
					|| mAuroraComposeAddressView == null) {
				return;
			}
			bundle = data.getExtras();
			arrayList = bundle.getStringArrayList("emails");
			mAuroraComposeAddressView.addContact(arrayList);
			break;
		case R.id.id_cc_layout:
			if (data == null || resultCode != RESULT_OK
					|| mAuroraComposeAddressView == null) {
				return;
			}
			bundle = data.getExtras();
			arrayList = bundle.getStringArrayList("emails");
			mCcAddressView.addContact(arrayList);
			break;
		case R.id.id_bcc_layout:
			if (data == null || resultCode != RESULT_OK
					|| mAuroraComposeAddressView == null) {
				return;
			}
			bundle = data.getExtras();
			arrayList = bundle.getStringArrayList("emails");
			mBccaAddressView.addContact(arrayList);
			break;
		default:
			break;
		}

	}

	private void doAttach(String type) {
		Intent i = new Intent("com.aurora.filemanager.MORE_GET_CONTENT");
		i.addCategory(Intent.CATEGORY_DEFAULT);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		i.setType(type);
		startActivityForResult(i, RESULT_PICK_ATTACHMENT);
	}

	private void doCameraAttach() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		saveImagePath = SAVE_PATH_DIR + getfilenameFromData() + IMG_SUFFIX;

		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(saveImagePath)));
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, RESULT_CAMERA_ADD);
	}

	private void doFileAttach() {
		Intent mIntent = new Intent();
		mIntent.setType("*/*");
		mIntent.setAction("com.aurora.filemanager.FILE_GET_CONTENT");
		startActivityForResult(mIntent, RESULT_FILE_ATTACHMENT);
	}

	private String getfilenameFromData() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.CHINA);
		String data = format.format(new Date());
		return data;
	}

	@Override
	public void onFocusChange(View view, boolean flag) {
		/*MyLog.d(TAG, "onFocusChange---------------------->:" + flag + " input:"
				+ mScrollView.isShowInputMethod());
		MyLog.d(TAG, "view:" + getCurrentFocus() + " view111:" + view);*/
		if (!flag) {
			return;
		}
		foucuseView = view;

		if (mScrollView == null || !mScrollView.isShowInputMethod()) {
			return;
		}
		// smoothView(view);
		if (view.getId() != R.id.id_cc_layout
				&& view.getId() != R.id.id_bcc_layout) {
			setCcViewstate(false);
		}

		mHandler.removeMessages(MSG_SHOW_SMOOTH_VIEW);
		mHandler.sendEmptyMessageDelayed(MSG_SHOW_SMOOTH_VIEW, 100);
	}

	@Override
	public void OnInputMethodChange(int state) {

		/*MyLog.d(TAG, "OnInputMethodChange:" + state + " bottommenu:"
				+ mAuroraActionBar.isShowBottomBarMenu());*/
		if (state == OnInputMethodChangeListener.STATE_PUT_OUT) {
			//-----modify by tangjie 2014/12/16-----start//
//			View foucView = getCurrentFocus();
//			if (foucView.getId() != R.id.id_cc_layout
//					&& foucView.getId() != R.id.id_bcc_layout) {
//				setCcViewstate(false);
//			}
			//-----modify by tangjie 2014/12/16-----end//
			mHandler.removeMessages(MSG_SHOW_SMOOTH_VIEW);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_SMOOTH_VIEW, 100);
		} else {
			View foucView = getCurrentFocus();
			if (foucView.equals(findViewById(R.id.id_write_email))) {
				
				mHandler.removeMessages(MSG_SHOW_SMOOTH_CONTENT);
				mHandler.sendEmptyMessageDelayed(MSG_SHOW_SMOOTH_CONTENT, 100);

			}
		}
	}

	private void smoothView(View view) {
		if (view == null) {
			return;
		}
		switch (view.getId()) {
		case R.id.id_theme_info:
		case R.id.id_write_email:

			mScrollView.smoothScrollTo(0, findViewById(R.id.id_theme_layout)
					.getTop());

			break;
		case R.id.id_receive_layout:
			mScrollView.smoothScrollTo(0, view.getTop());
			break;
		case R.id.id_cc_layout:
			mScrollView.smoothScrollTo(0, findViewById(R.id.id_bcc_cc_parent)
					.getTop());
			break;
		case R.id.id_bcc_layout:
			mScrollView.smoothScrollTo(0, findViewById(R.id.id_bcc_cc_parent)
					.getTop() + view.getTop());
			break;
		default:
			break;
		}
	}

	private void setCcViewstate(boolean flag) {
		findViewById(R.id.id_cc_layout_temp).setVisibility(
				flag ? View.GONE : View.VISIBLE);
		findViewById(R.id.id_cc_layout_parent).setVisibility(
				flag ? View.VISIBLE : View.GONE);
		// -----------------add by tangjie start--------------------//
		if (!flag) {
			String ccAddress = mCcAddressView.getCcText();
			String bccAddress = mBccaAddressView.getCcText();
			int count = 0;
			if (!TextUtils.isEmpty(bccAddress)&&!TextUtils.isEmpty(ccAddress)) {
				ccAddress += ",";
			}
			ccAddress += bccAddress;
			mSenderTextView.setText(ccAddress);
			count += mBccaAddressView.getAdrressCount();
			count += mCcAddressView.getAdrressCount();
			if (mSenderTextView.getPaint().measureText(ccAddress) > getResources()
					.getDimensionPixelSize(R.dimen.aurora_address_maxwidth)) {
				String number = getResources().getString(
						R.string.aurora_num_sender, count);
				mSenderNumber.setText(number);
			} else {
				mSenderNumber.setText("");
			}
		}
		// -----------------add by tangjie end----------------------//

	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case MSG_SHOW_SMOOTH_VIEW:
				//MyLog.d(TAG, "MSG_SHOW_SMOOTH_VIEW");
				View foucView = getCurrentFocus();
				smoothView(foucView);
				//-----add by tangjie 2014/12/16 start------//
				if (foucView.getId() != R.id.id_cc_layout
								&& foucView.getId() != R.id.id_bcc_layout) {
							setCcViewstate(false);
				}
				//-----add by tangjie 2014/12/16 end------//
				break;
			case MSG_SHOW_SMOOTH_CONTENT:
				mScrollView.smoothScrollTo(0,
						findViewById(R.id.id_theme_layout).getTop());
				break;
			default:
				break;
			}
		}
	};

	@Override
	public View getCurrentFocus() {

		if (foucuseView == null) {
			foucuseView = super.getCurrentFocus();
		}
		return foucuseView;
	}

	@Override
	public void onScrollChanged(int scrollY) {
		View focusvView = getCurrentFocus();
		// MyLog.d(TAG, "focusvView:"+focusvView+" scrollY:"+scrollY);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (mAuroraActionBar != null
					&& isShouldHideFujian(mAuroraActionBar
							.getAuroraActionBottomBarMenu().getContentView(),
							ev) && isshowBottomMenu) {

				return hideBottomBar() ? true : super.dispatchTouchEvent(ev);
			} else if (mAuroraFujianView != null
					&& isShouldHideFujian(
							mAuroraFujianView.getFujianListView(), ev)
					&& mAuroraFujianView.getFujianListView().getVisibility() == View.VISIBLE) {

				return mAuroraFujianView.hideFujian() ? true : super
						.dispatchTouchEvent(ev);
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& isshowBottomMenu) {
			return hideBottomBar() ? true : super.dispatchKeyEvent(event);
		}
		return super.dispatchKeyEvent(event);
	}
	
	private boolean hideBottomBar() {
		if (mAuroraActionBar == null) {
			return false;
		}
		if (mAuroraActionBar.isShowBottomBarMenu()) {
			//MyLog.d(TAG, "hideBottomBar()");
			mAuroraActionBar.setShowBottomBarMenu(false);
			mAuroraActionBar.showActionBottomeBarMenu();
			removeCoverView();
			isshowBottomMenu=false;
			return true;
		}
		return false;
	}

	public static boolean isShouldHideFujian(View v, MotionEvent event) {
		if (v != null) {
			int[] leftTop = { 0, 0 };
			v.getLocationInWindow(leftTop);
			int left = leftTop[0], top = leftTop[1], bottom = top
					+ v.getHeight(), right = left + v.getWidth();
			if (event.getX() > left && event.getX() < right
					&& event.getY() > top && event.getY() < bottom) {
				// 保留点击EditText的事件
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * 隐藏键盘
	 * 
	 * @param context
	 * @param v
	 * @return
	 */
	public static Boolean hideInputMethod(Context context, View v) {
		if (v == null) {
			return false;
		}
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		return false;
	}

	/**
	 * 弹出键盘
	 * 
	 * @param context
	 * @param v
	 * @return
	 */
	public static boolean showInputMethod(Context context, View v) {
		if (v == null) {
			return false;
		}
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			return imm.showSoftInput(v, 0);
		}
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		switch (arg0) {
		case INIT_DRAFT_USING_REFERENCE_MESSAGE:
			return new CursorLoader(this, mRefMessageUri,
					UIProvider.MESSAGE_PROJECTION, null, null, null);
		case REFERENCE_MESSAGE_LOADER:
			return new CursorLoader(this, mRefMessageUri,
					UIProvider.MESSAGE_PROJECTION, null, null, null);
		case LOADER_ACCOUNT_CURSOR:
			return new CursorLoader(this, MailAppProvider.getAccountsUri(),
					UIProvider.ACCOUNTS_PROJECTION, null, null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
		case INIT_DRAFT_USING_REFERENCE_MESSAGE:
			if (data != null && data.moveToFirst()) {
				mRefMessage = new Message(data);
				Intent intent = getIntent();
				initFromRefMessage(mComposeMode);
				finishSetup(mComposeMode, intent, null);
				if (mComposeMode != FORWARD) {
					String to = intent.getStringExtra(EXTRA_TO);
					if (!TextUtils.isEmpty(to)) {
						mRefMessage.setTo(null);
						mRefMessage.setFrom(null);
						// clearChangeListeners();
						// mTo.append(to);
						// initChangeListeners();
					}
				}
			} else {
				finish();
			}
			break;
		case REFERENCE_MESSAGE_LOADER:
			// Only populate mRefMessage and leave other fields untouched.
			if (data != null && data.moveToFirst()) {
				mRefMessage = new Message(data);
			}
			finishSetup(mComposeMode, getIntent(), mInnerSavedState);
			break;
		case LOADER_ACCOUNT_CURSOR:
			if (data != null && data.moveToFirst()) {
				// there are accounts now!
				Account account;
				final ArrayList<Account> accounts = new ArrayList<Account>();
				final ArrayList<Account> initializedAccounts = new ArrayList<Account>();
				do {
					account = new Account(data);
					if (account.isAccountReady()) {
						initializedAccounts.add(account);
					}
					accounts.add(account);
				} while (data.moveToNext());
		/*		MyLog.i(TAG,
						"zll ---- LOADER_ACCOUNT_CURSOR 4.1 initializedAccounts.size():"
								+ initializedAccounts.size());*/
				if (initializedAccounts.size() > 0) {
					findViewById(R.id.wait).setVisibility(View.GONE);
					getLoaderManager().destroyLoader(LOADER_ACCOUNT_CURSOR);
					findViewById(R.id.id_main).setVisibility(View.VISIBLE);
					mAccounts = initializedAccounts
							.toArray(new Account[initializedAccounts.size()]);

					finishCreate();
				} else {
					// Show "waiting"
					account = accounts.size() > 0 ? accounts.get(0) : null;
					showWaitFragment(account);
				}
			}
			break;
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}
	
	private void showWaitFragment(Account account) {
		WaitFragment fragment = getWaitFragment();
		if (fragment != null) {
			fragment.updateAccount(account);
		} else {
			findViewById(R.id.wait).setVisibility(View.VISIBLE);
			replaceFragment(WaitFragment.newInstance(account, true),
					FragmentTransaction.TRANSIT_FRAGMENT_OPEN, TAG_WAIT);
		}
	}

	private int replaceFragment(Fragment fragment, int transition, String tag) {
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		fragmentTransaction.setTransition(transition);
		fragmentTransaction.replace(R.id.wait, fragment, tag);
		final int transactionId = fragmentTransaction.commitAllowingStateLoss();
		return transactionId;
	}

	private Account obtainAccount(Intent intent) {
		Account account = null;
		Object accountExtra = null;
		if (intent != null && intent.getExtras() != null) {
			accountExtra = intent.getExtras().get(Utils.EXTRA_ACCOUNT);
			if (accountExtra instanceof Account) {
				return (Account) accountExtra;
			} else if (accountExtra instanceof String) {
				// This is the Account attached to the widget compose intent.
				account = Account.newinstance((String) accountExtra);
				if (account != null) {
					return account;
				}
			}
			accountExtra = intent.hasExtra(Utils.EXTRA_ACCOUNT) ? intent
					.getStringExtra(Utils.EXTRA_ACCOUNT) : intent
					.getStringExtra(EXTRA_SELECTED_ACCOUNT);
		}
		if (account == null) {
			MailAppProvider provider = MailAppProvider.getInstance();
			String lastAccountUri = provider.getLastViewedAccount();
			//MyLog.d(TAG, "lastAccountUri:"+lastAccountUri);
/*			if (TextUtils.isEmpty(lastAccountUri)) {
				lastAccountUri = provider.getLastViewedAccount();
			}*/
			if (!TextUtils.isEmpty(lastAccountUri)) {
				accountExtra = Uri.parse(lastAccountUri);
			}
		}
		
	//	MyLog.d("chenhl", "accountExtra:"+accountExtra);
		if (mAccounts != null && mAccounts.length > 0) {
			if (accountExtra instanceof String
					&& !TextUtils.isEmpty((String) accountExtra)) {
				// For backwards compatibility, we need to check account
				// names.
				for (Account a : mAccounts) {
					if (a.getEmailAddress().equals(accountExtra)) {
						account = a;
					}
				}
			} else if (accountExtra instanceof Uri) {
				// The uri of the last viewed account is what is stored in
				// the current code base.
				for (Account a : mAccounts) {
					if (a.uri.equals(accountExtra)) {
						account = a;
					}
				}
			}
			if (account == null) {
				account = mAccounts[0];
			}
		}
		return account;
	}

	void setAccount(Account account,boolean withSign) {
		if (account == null) {
			return;
		}
		if (!account.equals(mAccount)) {
			mAccount = account;
			mCachedSettings = mAccount.settings;
			if(withSign){
				appendSignature();
			}			
		}
		if (mAccount != null) {
			MailActivity.setNfcMessage(mAccount.getEmailAddress());
		}
	}

	private void initFromRefMessage(int action) {
		setFieldsFromRefMessage(action);

		// Check if To: address and email body needs to be prefilled based on
		// extras.
		// This is used for reporting rendering feedback.
		if (MessageHeaderView.ENABLE_REPORT_RENDERING_PROBLEM) {
			Intent intent = getIntent();
			if (intent.getExtras() != null) {
				String toAddresses = intent.getStringExtra(EXTRA_TO);
				if (toAddresses != null) {
					addToAddresses(Arrays.asList(TextUtils.split(toAddresses,
							",")));
				}
				String body = intent.getStringExtra(EXTRA_BODY);
				if (body != null) {
					setBody(body, false /* withSignature */);
				}
			}
		}
		if (mRefMessage != null) {
			// CC field only gets populated when doing REPLY_ALL.
			// BCC never gets auto-populated, unless the user is editing
			// a draft with one.
		}
	}

	private void setFieldsFromRefMessage(int action) {
		setSubject(mRefMessage, action);
		// Setup recipients
		if (action == FORWARD) {
			mForward = true;
		}
		initRecipientsFromRefMessage(mRefMessage, action);
		initQuotedTextFromRefMessage(mRefMessage, action);
		if (action == ComposeActivity.FORWARD) {
			initAttachments(mRefMessage);
		}
	}

	private void addToAddresses(Collection<String> addresses) {
		addAddressesToList(addresses, mAuroraComposeAddressView);
	}

	void addAddressesToList(Collection<String> addresses,
			AuroraComposeAddressView list) {
		for (String address : addresses) {
			addAddressToList(address, list);
		}
	}

	private static void addAddressToList(final String address,
			AuroraComposeAddressView list) {
		if (address == null || list == null)
			return;
		final Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(address);
		list.setRfc822Tokens(tokens);
	}

	public void setBody(CharSequence text, boolean withSignature) {
		//MyLog.d(TAG, "setBody text:" + text + " withSignature:" + withSignature);
		mBodyView.setText(text);
		if (withSignature) {
			appendSignature();
		}
	}

	private void setSubject(Message refMessage, int action) {
		String subjectString = buildFormattedSubject(getResources(),
				refMessage.subject, action);
		//MyLog.d(TAG, "setSubject :" + subjectString);
		if (themeEditText != null) {
			themeEditText.setText(subjectString);
		}
	}

	public static String buildFormattedSubject(Resources res, String subject,
			int action) {
		String prefix;
		String correctedSubject = null;
		if (action == ComposeActivity.COMPOSE) {
			prefix = "";
		} else if (action == ComposeActivity.FORWARD) {
			prefix = res.getString(R.string.forward_subject_label);
		} else {
			prefix = res.getString(R.string.reply_subject_label);
		}

		// Don't duplicate the prefix
		if (!TextUtils.isEmpty(subject)
				&& subject.toLowerCase().startsWith(prefix.toLowerCase())) {
			correctedSubject = subject;
		} else {
			correctedSubject = String.format(
					res.getString(R.string.formatted_subject), prefix, subject);
		}

		return correctedSubject;
	}

	void initRecipientsFromRefMessage(Message refMessage, int action) {
		// Don't populate the address if this is a forward.
		if (action == ComposeActivity.FORWARD) {
			return;
		}
		initReplyRecipients(refMessage, action);
	}

	void initReplyRecipients(final Message refMessage, final int action) {
		String[] sentToAddresses = refMessage.getToAddressesUnescaped();
		final Collection<String> toAddresses;
		final String[] replyToAddresses = refMessage
				.getReplyToAddressesUnescaped();
		String replyToAddress = replyToAddresses.length > 0 ? replyToAddresses[0]
				: null;
		final String[] fromAddresses = refMessage.getFromAddressesUnescaped();
		final String fromAddress = fromAddresses.length > 0 ? fromAddresses[0]
				: null;

		// If there is no reply to address, the reply to address is the sender.
		if (TextUtils.isEmpty(replyToAddress)) {
			replyToAddress = fromAddress;
		}

		// If this is a reply, the Cc list is empty. If this is a reply-all, the
		// Cc list is the union of the To and Cc recipients of the original
		// message, excluding the current user's email address and any addresses
		// already on the To list.
		if (action == ComposeActivity.REPLY) {
			toAddresses = initToRecipients(fromAddress, replyToAddress,
					sentToAddresses);
			addToAddresses(toAddresses);
		} else if (action == ComposeActivity.REPLY_ALL) {
			final Set<String> ccAddresses = Sets.newHashSet();

			toAddresses = initToRecipients(fromAddress, replyToAddress,
					sentToAddresses);
			addToAddresses(toAddresses);

			/*
			 * addRecipients(ccAddresses, sentToAddresses);
			 * addRecipients(ccAddresses, refMessage.getCcAddressesUnescaped());
			 * addCcAddresses(ccAddresses, toAddresses);
			 */
			addCcAddresses(refMessage);
		}
		setCcViewstate(false);
	}

	private void addCcAddresses(final Message refMessage) {
		String sentToAddresses = refMessage.getTo();
		String ccAddresss = refMessage.getCc();
/*		MyLog.d(TAG, "sentToAddresses1:" + sentToAddresses + " ccAddresss:"
				+ ccAddresss);*/
		if (sentToAddresses != null && sentToAddresses.length() > 0
				&& ccAddresss != null) {
			String last = sentToAddresses
					.substring(sentToAddresses.length() - 1);
			if (!last.equals(",")) {
				sentToAddresses += ",";
			}
			sentToAddresses += ccAddresss;
		}

		//MyLog.d(TAG, "sentToAddresses2:" + sentToAddresses);
		if(sentToAddresses==null||sentToAddresses.length()==0){
			return;
		}
		Rfc822Token[] ccaddressTokens = Rfc822Tokenizer
				.tokenize(sentToAddresses);
		if (ccaddressTokens != null && mCcAddressView != null) {
			mCcAddressView.setRfc822Tokens(ccaddressTokens);
		}
	}

	private void addRecipients(final Set<String> recipients,
			final String[] addresses) {
		for (final String email : addresses) {
			// Do not add this account, or any of its custom from addresses, to
			// the list of recipients.
			final String recipientAddress = Address.getEmailAddress(email)
					.getAddress();
	/*		MyLog.d(TAG, "recipientAddress:" + recipientAddress + " email:"
					+ email);*/
			if (!recipientMatchesThisAccount(recipientAddress)) {
				recipients.add(email.replace("\"\"", ""));
			}
		}
	}

	private void addCcAddresses(Collection<String> addresses,
			Collection<String> toAddresses) {
		addCcAddressesToList(tokenizeAddressList(addresses),
				toAddresses != null ? tokenizeAddressList(toAddresses) : null,
				mCcAddressView);
	}

	private void addBccAddresses(Collection<String> addresses) {
		addAddressesToList(addresses, mBccaAddressView);
	}

	protected List<Rfc822Token[]> tokenizeAddressList(
			Collection<String> addresses) {

		List<Rfc822Token[]> tokenized = new ArrayList<Rfc822Token[]>();

		for (String address : addresses) {
			tokenized.add(Rfc822Tokenizer.tokenize(address));
		}
		return tokenized;
	}

	protected void addCcAddressesToList(List<Rfc822Token[]> addresses,
			List<Rfc822Token[]> compareToList, AuroraComposeAddressView list) {
		String address;

		//MyLog.d(TAG, "compareToList:" + compareToList);
		if (compareToList == null) {
		//	MyLog.d(TAG, "compareToList11:" + compareToList);
			for (Rfc822Token[] tokens : addresses) {
				list.setRfc822Tokens(tokens);
			}
		} else {
			//MyLog.d(TAG, "compareToList22:" + compareToList);
			HashSet<String> compareTo = convertToHashSet(compareToList);
			for (Rfc822Token[] tokens : addresses) {
				list.setRfc822Tokens(tokens);
			}
		}
	}

	private static HashSet<String> convertToHashSet(
			final List<Rfc822Token[]> list) {
		final HashSet<String> hash = new HashSet<String>();
		for (final Rfc822Token[] tokens : list) {
			for (int i = 0; i < tokens.length; i++) {
				hash.add(tokens[i].getAddress());
			}
		}
		return hash;
	}

	protected Collection<String> initToRecipients(
			final String fullSenderAddress, final String replyToAddress,
			final String[] inToAddresses) {
		// The To recipient is the reply-to address specified in the original
		// message, unless it is:
		// the current user OR a custom from of the current user, in which case
		// it's the To recipient list of the original message.
		// OR missing, in which case use the sender of the original message
		/*MyLog.d(TAG, "fullSenderAddress:" + fullSenderAddress + " "
				+ replyToAddress);*/
		Set<String> toAddresses = Sets.newHashSet();
		if (!TextUtils.isEmpty(replyToAddress)
				&& !recipientMatchesThisAccount(replyToAddress)) {
			toAddresses.add(replyToAddress);
		} else {
			// In this case, the user is replying to a message in which their
			// current account or one of their custom from addresses is the only
			// recipient and they sent the original message.
			if (inToAddresses.length == 1
					&& recipientMatchesThisAccount(fullSenderAddress)
					&& recipientMatchesThisAccount(inToAddresses[0])) {
				toAddresses.add(inToAddresses[0]);
				return toAddresses;
			}
			// This happens if the user replies to a message they originally
			// wrote. In this case, "reply" really means "re-send," so we
			// target the original recipients. This works as expected even
			// if the user sent the original message to themselves.
			for (String address : inToAddresses) {
				if (!recipientMatchesThisAccount(address)) {
					toAddresses.add(address);
				}
			}
		}
		return toAddresses;
	}

	protected boolean recipientMatchesThisAccount(String recipientAddress) {
		return ReplyFromAccount.matchesAccountOrCustomFrom(mAccount,
				recipientAddress, mAccount.getReplyFroms());
	}

	private void initQuotedTextFromRefMessage(Message refMessage, int action) {
		if (mRefMessage != null
				&& (action == REPLY || action == REPLY_ALL || action == FORWARD)) {
			// MyLog.d(TAG, "initQuotedTextFromRefMessage:" +
			// refMessage.bodyHtml);
			mQuotedTextView
					.setQuotedText(action, refMessage, action != FORWARD);
			findViewById(R.id.id_edite_touch).setOnClickListener(null);// 隐藏底部touch事件
		}
	}

	protected void initAttachments(Message refMessage) {
		addAttachments(refMessage.getAttachments());
	}

	public long addAttachments(List<Attachment> attachments) {
		if (mAuroraFujianView == null) {
			return 0;
		}
		long size = 0;
		for (Attachment a : attachments) {
			size += mAuroraFujianView.addAttachment(mAccount, a);
		}
		mAuroraFujianView.notifyDataSetChanged();
		return size;
	}

	private boolean initFromExtras(Intent intent) {
		// If we were invoked with a SENDTO intent, the value
		// should take precedence
		final Uri dataUri = intent.getData();
		if (dataUri != null) {
			if (MAIL_TO.equals(dataUri.getScheme())) {
				initFromMailTo(dataUri.toString());
			} else {
				if (!mAccount.composeIntentUri.equals(dataUri)) {
					String toText = dataUri.getSchemeSpecificPart();
					if (toText != null) {
						mAuroraComposeAddressView.setText("");
						addToAddresses(Arrays.asList(TextUtils.split(toText,
								",")));
					}
				}
			}
		}

		String[] extraStrings = intent.getStringArrayExtra(Intent.EXTRA_EMAIL);
		if (extraStrings != null) {
			addToAddresses(Arrays.asList(extraStrings));
		}
		extraStrings = intent.getStringArrayExtra(Intent.EXTRA_CC);
		if (extraStrings != null) {
			addCcAddresses(Arrays.asList(extraStrings), null);
		}
		extraStrings = intent.getStringArrayExtra(Intent.EXTRA_BCC);
		if (extraStrings != null) {
			addBccAddresses(Arrays.asList(extraStrings));
		}

		String extraString = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		if (extraString != null) {
			themeEditText.setText(extraString);
		}

		for (String extra : ALL_EXTRAS) {
			if (intent.hasExtra(extra)) {
				String value = intent.getStringExtra(extra);
				if (EXTRA_TO.equals(extra)) {
					addToAddresses(Arrays.asList(TextUtils.split(value, ",")));
				} else if (EXTRA_CC.equals(extra)) {
					addCcAddresses(Arrays.asList(TextUtils.split(value, ",")),
							null);
				} else if (EXTRA_BCC.equals(extra)) {
					addBccAddresses(Arrays.asList(TextUtils.split(value, ",")));
				} else if (EXTRA_SUBJECT.equals(extra)) {
					themeEditText.setText(value);
				} else if (EXTRA_BODY.equals(extra)) {
					setBody(value, true /* with signature */);
				} else if (EXTRA_QUOTED_TEXT.equals(extra)) {
					initQuotedText(value, true /* shouldQuoteText */);
				}
			}
		}

		Bundle extras = intent.getExtras();
		if (extras != null) {
			CharSequence text = extras.getCharSequence(Intent.EXTRA_TEXT);
			if (text != null) {
				setBody(text, true /* with signature */);
			}

			// TODO - support EXTRA_HTML_TEXT
		}

		mExtraValues = intent.getParcelableExtra(EXTRA_VALUES);
		if (mExtraValues != null) {
			LogUtils.d(TAG, "Launched with extra values: %s",
					mExtraValues.toString());
			initExtraValues(mExtraValues);
			return true;
		}

		return false;
	}

	public void initFromMailTo(String mailToString) {
		// We need to disguise this string as a URI in order to parse it
		// TODO: Remove this hack when http://b/issue?id=1445295 gets fixed
		Uri uri = Uri.parse("foo://" + mailToString);
		int index = mailToString.indexOf("?");
		int length = MAIL_TO.length() + 1;
		String to;
		try {
			// Extract the recipient after mailto:
			if (index == -1) {
				to = decodeEmailInUri(mailToString.substring(length));
			} else {
				to = decodeEmailInUri(mailToString.substring(length, index));
			}
			if (!TextUtils.isEmpty(to)) {
				addToAddresses(Arrays.asList(TextUtils.split(to, ",")));
			}
		} catch (UnsupportedEncodingException e) {
			if (LogUtils.isLoggable(TAG, LogUtils.VERBOSE)) {
				LogUtils.e(TAG, "%s while decoding '%s'", e.getMessage(),
						mailToString);
			} else {
				LogUtils.e(TAG, e, "Exception  while decoding mailto address");
			}
		}

		List<String> cc = uri.getQueryParameters("cc");
		addCcAddresses(Arrays.asList(cc.toArray(new String[cc.size()])), null);

		List<String> otherTo = uri.getQueryParameters("to");
		addToAddresses(Arrays
				.asList(otherTo.toArray(new String[otherTo.size()])));

		List<String> bcc = uri.getQueryParameters("bcc");
		addBccAddresses(Arrays.asList(bcc.toArray(new String[bcc.size()])));

		List<String> subject = uri.getQueryParameters("subject");
		if (subject.size() > 0) {
			try {
				themeEditText.setText(URLDecoder.decode(
						replacePlus(subject.get(0)), UTF8_ENCODING_NAME));
			} catch (UnsupportedEncodingException e) {
				LogUtils.e(TAG, "%s while decoding subject '%s'",
						e.getMessage(), subject);
			}
		}

		List<String> body = uri.getQueryParameters("body");
		if (body.size() > 0) {
			try {
				setBody(URLDecoder.decode(replacePlus(body.get(0)),
						UTF8_ENCODING_NAME), true /* with signature */);
			} catch (UnsupportedEncodingException e) {
				LogUtils.e(TAG, "%s while decoding body '%s'", e.getMessage(),
						body);
			}
		}
	}

	protected String decodeEmailInUri(String s)
			throws UnsupportedEncodingException {
		// TODO: handle the case where there are spaces in the display name as
		// well as the email such as
		// "Guy with spaces <guy+with+spaces@gmail.com>"
		// as they could be encoded ambiguously.
		// Since URLDecode.decode changes + into ' ', and + is a valid
		// email character, we need to find/ replace these ourselves before
		// decoding.
		try {
			return URLDecoder.decode(replacePlus(s), UTF8_ENCODING_NAME);
		} catch (IllegalArgumentException e) {
			if (LogUtils.isLoggable(TAG, LogUtils.VERBOSE)) {
				LogUtils.e(TAG, "%s while decoding '%s'", e.getMessage(), s);
			} else {
				LogUtils.e(TAG, e, "Exception  while decoding mailto address");
			}
			return null;
		}
	}

	/**
	 * Replaces all occurrences of '+' with "%2B", to prevent URLDecode.decode
	 * from changing '+' into ' '
	 * 
	 * @param toReplace
	 *            Input string
	 * @return The string with all "+" characters replaced with "%2B"
	 */
	private static String replacePlus(String toReplace) {
		return toReplace.replace("+", "%2B");
	}

	private void initFromDraftMessage(Message message) {

	//	MyLog.d(TAG, "initFromDraftMessage:" + message);
		mDraft = message;
		mDraftId = message.id;
		themeEditText.setText(message.subject);
		mForward = message.draftType == UIProvider.DraftType.FORWARD;
		final List<String> toAddresses = Arrays.asList(message
				.getToAddressesUnescaped());
		addToAddresses(toAddresses);
		addCcAddresses(Arrays.asList(message.getCcAddressesUnescaped()),
				toAddresses);
		addBccAddresses(Arrays.asList(message.getBccAddressesUnescaped()));

		if (1 == message.hasAttachments) {// paul modify
			List<Attachment> attachments = message.getAttachments();
			addAttachments(attachments);
		}

		int quotedTextIndex = message.appendRefMessageContent ? message.quotedTextOffset
				: -1;
		// Set the body
		CharSequence quotedText = null;
		if (!TextUtils.isEmpty(message.bodyHtml)) {
			CharSequence htmlText = "";
			if (quotedTextIndex > -1) {
				// Find the offset in the htmltext of the actual quoted text and
				// strip it out.
				quotedTextIndex = QuotedTextView
						.findQuotedTextIndex(message.bodyHtml);
				if (quotedTextIndex > -1) {
					htmlText = Utils.convertHtmlToPlainText(message.bodyHtml
							.substring(0, quotedTextIndex));
					quotedText = message.bodyHtml.subSequence(quotedTextIndex,
							message.bodyHtml.length());
				}
			} else {
				htmlText = Utils.convertHtmlToPlainText(message.bodyHtml);
			}
			mBodyView.setText(htmlText);
		} else {
			final String body = message.bodyText;
			final CharSequence bodyText = !TextUtils.isEmpty(body) ? (quotedTextIndex > -1 ? message.bodyText
					.substring(0, quotedTextIndex) : message.bodyText)
					: "";
			if (quotedTextIndex > -1) {
				quotedText = !TextUtils.isEmpty(body) ? message.bodyText
						.substring(quotedTextIndex) : null;
			}
			mBodyView.setText(bodyText);
		}
		if (quotedTextIndex > -1 && quotedText != null) {
			mQuotedTextView.setQuotedTextFromDraft(quotedText, mForward);
		}
	}

	private RespondInlineListener mRespondInlineListener = new RespondInlineListener() {

		@Override
		public void onRespondInline(String text) {
			appendToBody(text, false);
			mQuotedTextView.setUpperDividerVisible(false);
			mRespondedInline = true;
		}
	};

	public void appendToBody(CharSequence text, boolean withSignature) {
		Editable bodyText = mBodyView.getEditableText();
		if (bodyText != null && bodyText.length() > 0) {
			bodyText.append(text);
		} else {
			setBody(text, withSignature);
		}
	}

	private void initRecipients() {
		mAuroraSenderView.setCurrentAccount(mAccount);
		mAuroraSenderView.setAccounts(mAccounts);
		setupRecipients(mAuroraComposeAddressView);
		setupRecipients(mCcAddressView);
		setupRecipients(mBccaAddressView);
	}

	private void setupRecipients(AuroraComposeAddressView view) {
		view.setCurrentAccount(mAccount);// -------------------add by tangjie
											// 2014/11/17------------------//
		if (mValidator == null) {
			final String accountName = mAccount.getEmailAddress();
			int offset = accountName.indexOf("@") + 1;
			String account = accountName;
			if (offset > 0) {
				account = account.substring(offset);
			}
			mValidator = new Rfc822Validator(account);
		}
		view.setValidator(mValidator);
	}

	private void setFocus(int action) {
		if (action == EDIT_DRAFT) {
			int type = mDraft.draftType;
			switch (type) {
			case UIProvider.DraftType.COMPOSE:
			case UIProvider.DraftType.FORWARD:
				action = COMPOSE;
				break;
			case UIProvider.DraftType.REPLY:
			case UIProvider.DraftType.REPLY_ALL:
			default:
				action = REPLY;
				break;
			}
		}
		switch (action) {
		case FORWARD:
		case COMPOSE:
			if (TextUtils.isEmpty(mAuroraComposeAddressView.getText())) {
				mAuroraComposeAddressView.requestFocusFromChild();

				break;
			}
			//$FALL-THROUGH$
		case REPLY:
		case REPLY_ALL:
		default:
			focusBody();
			
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					showInputMethod(AuroraComposeActivity.this, mBodyView);
				}
			},100);
			break;
		}
	}

	/**
	 * Focus the body of the message.
	 */
	public void focusBody() {
		mBodyView.requestFocus();
		setBodySelect();
	}

	private void setBodySelect() {
		int length = mBodyView.getText().length();

		int signatureStartPos = getSignatureStartPosition(mSignature, mBodyView
				.getText().toString());
		if (signatureStartPos > -1) {
			// In case the user deleted the newlines...
			mBodyView.setSelection(signatureStartPos);
		} else if (length >= 0) {
			// Move cursor to the end.
			mBodyView.setSelection(length);
		}
	}

	protected int getSignatureStartPosition(String signature, String bodyText) {
		int startPos = -1;
	//	MyLog.d(TAG, "bodyText:" + bodyText + " signature:" + signature);
		if (TextUtils.isEmpty(signature) || TextUtils.isEmpty(bodyText)) {
			return startPos;
		}

		int bodyLength = bodyText.length();
		int signatureLength = signature.length();
		String printableVersion = convertToPrintableSignature(signature);
		int printableLength = printableVersion.length();
		/*MyLog.d(TAG, "printableLength:" + printableLength + " bodyLength:"
				+ bodyLength);*/
		if (bodyLength >= printableLength
				&& bodyText.substring(bodyLength - printableLength).equals(
						printableVersion)) {
			startPos = bodyLength - printableLength;
		} else if (bodyLength >= signatureLength
				&& bodyText.substring(bodyLength - signatureLength).equals(
						signature)) {
			startPos = bodyLength - signatureLength;
		}
		return startPos;
	}

	private String convertToPrintableSignature(String signature) {
		String signatureResource = getResources().getString(
				R.string.aurora_signature);
		if (signature == null) {
			signature = "";
		}		
	//	MyLog.d(TAG, "signature:" + signature);
		return String.format(signatureResource, signature);
	}

	private void appendSignature() {
		String newSignature = mCachedSettings != null ? mCachedSettings.signature
				: null;
	/*	MyLog.d(TAG, "newSignature:" + newSignature + " mSignature:"
				+ mSignature);*/
		boolean hasFocus = mBodyView.hasFocus();
		int signaturePos = getSignatureStartPosition(mSignature, mBodyView
				.getText().toString());
		if (!TextUtils.equals(newSignature, mSignature) || signaturePos < 0) {
			mSignature = newSignature;
			if (null == mSignature) {//paul modify for BUG #10280  	if (TextUtils.isEmpty(mSignature)) {
				// Appending a signature does not count as changing text.
				mSignature = getString(com.android.email.R.string.aurora_default_signatrue);//paul modify
			}
			mBodyView.removeTextChangedListener(mBodyTextChangedListener);
			mSignatureDefualt = convertToPrintableSignature(mSignature);	
			Spannable WordtoSpan=new SpannableString(mSignatureDefualt);
			if (mSignature.length() > 0) {
				WordtoSpan
						.setSpan(
								new AbsoluteSizeSpan(13, true),
								mSignatureDefualt.length()
										- mSignature.length()-"\n".length(),
										mSignatureDefualt.length()-"\n".length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			mBodyView.append(WordtoSpan);
			mBodyView.addTextChangedListener(mBodyTextChangedListener);
			if (hasFocus) {
				focusBody();
			} else {
				setBodySelect();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();

		//MyLog.i(TAG, "zll ----- onPause 1");
		if (!isChangingConfigurations()) {
		//	MyLog.i(TAG, "zll ----- onPause 2");
			if (isFinishing() && !mPerformedSendOrDiscard && !isBlank()) {
				// log saving upon backing out of activity. (we avoid logging
				// every sendOrSave()
				// because that method can be invoked many times in a single
				// compose session.)

			//	MyLog.i(TAG, "zll ----- onPause 3");
				logSendOrSave(true /* save */);
			}
		}
	}

	private void saveIfNeeded() {
		if (mAccount == null) {
			//MyLog.i(TAG, "zll ----- saveIfNeeded 1 mAccount:");
			// We have not chosen an account yet so there's no way that we can
			// save. This is ok,
			// though, since we are saving our state before AccountsActivity is
			// activated. Thus, the
			// user has not interacted with us yet and there is no real state to
			// save.
			return;
		}

	//	MyLog.i(TAG, "zll ----- saveIfNeeded 2:");
		if (shouldSave()) {
		//	MyLog.i(TAG, "zll ----- saveIfNeeded 3");
			showSendConfirmDialog(R.string.aurora_savedraft, true, false);
		}
	}

	/**
	 * Returns true if we need to save the current draft.
	 */
	private boolean shouldSave() {
		synchronized (mDraftLock) {
			// The message should only be saved if:
			// It hasn't been sent AND
			// Some text has been added to the message OR
			// an attachment has been added or removed
			// AND there is actually something in the draft to save.

/*			MyLog.i(TAG, "zll ----- shouldSave mTextChanged:" + mTextChanged
					+ ",mAttachmentsChanged:" + mAttachmentsChanged
					+ ",mReplyFromChanged:" + mReplyFromChanged);*/
			return (mTextChanged || mAttachmentsChanged || mReplyFromChanged)
					&& !isBlank();
		}
	}

	/**
	 * Check if all fields are blank.
	 * 
	 * @return boolean
	 */
	public boolean isBlank() {
		// Need to check for null since isBlank() can be called from onPause()
		// before findViews() is called
		if (themeEditText == null || mBodyView == null
				|| mAuroraComposeAddressView == null || mCcAddressView == null
				|| mAuroraFujianView == null) {
			LogUtils.w(TAG, "null views in isBlank check");
			return true;
		}
		return themeEditText.getText().length() == 0
				&& (mBodyView.getText().length() == 0 || getSignatureStartPosition(
						mSignature, mBodyView.getText().toString()) == 0)
				&& mAuroraComposeAddressView.length() == 0
				&& mCcAddressView.length() == 0
				&& mBccaAddressView.length() == 0
				&& mAuroraFujianView.getFujianCount() == 0;
	}

	/**
	 * Get the to recipients.
	 */
	public String[] getToAddresses() {
		return getAddressesFromList(mAuroraComposeAddressView);
	}

	/**
	 * Get the cc recipients.
	 */
	public String[] getCcAddresses() {
		return getAddressesFromList(mCcAddressView);
	}

	/**
	 * Get the bcc recipients.
	 */
	public String[] getBccAddresses() {
		return getAddressesFromList(mBccaAddressView);
	}

	public String[] getAddressesFromList(AuroraComposeAddressView list) {
		if (list == null) {
			return new String[0];
		}
		Rfc822Token[] tokens = list.getRfc822Tokens();
		int count = tokens.length;
		String[] result = new String[count];
		for (int i = 0; i < count; i++) {
			result[i] = tokens[i].toString();
/*			MyLog.i(TAG, "zll getAddressesFromList ---- 2 result[i]:"
					+ result[i]);*/
		}
		return result;
	}

	protected void initQuotedText(CharSequence quotedText,
			boolean shouldQuoteText) {
		mQuotedTextView.setQuotedTextFromHtml(quotedText, shouldQuoteText);
		mShowQuotedText = true;
	}

	private void discardChanges() {
		mTextChanged = false;
		mAttachmentsChanged = false;
		mReplyFromChanged = false;
	}

	protected void initExtraValues(ContentValues extraValues) {
		// DO NOTHING - Gmail will override
	}

	// ------------ lory add 2014.10.29 start ----------------//
	private boolean mPerformedSendOrDiscard = false;
	@VisibleForTesting
	public ArrayList<SendOrSaveTask> mActiveTasks = Lists.newArrayList();
	private static AuroraSendOrSaveCallback sTestSendOrSaveCallback = null;
	private Handler mSendSaveTaskHandler = null;
	private int mRequestId;
	private static final String TAG_WAIT = "wait-fragment";
	private long mDraftId = UIProvider.INVALID_MESSAGE_ID;
	private static ConcurrentHashMap<Integer, Long> sRequestMessageIdMap = null;

	private static final String EXTRA_SELECTED_REPLY_FROM_ACCOUNT = "replyFromAccount";
	protected static final String EXTRA_FROM_ACCOUNT_STRING = "fromAccountString";

	private WaitFragment getWaitFragment() {
		return (WaitFragment) getFragmentManager().findFragmentByTag(TAG_WAIT);
	}

	@Override
	public void onBackPressed() {
//		MyLog.i(TAG, "zll ----- onBackPressed 1");
		if (getWaitFragment() != null) {
	///		MyLog.i(TAG, "zll ----- onBackPressed 2");
			finish();
		} else {
			if (!isShoudSave()) {
				super.onBackPressed();
			} else {
				doSaveDraft(R.string.aurora_savedraft, true);
				return;
			}
		}
	}

	private boolean isShoudSave() {
		boolean save = false;

	//	MyLog.i(TAG, "zll ----- isShoudSave 1");
		if (!isChangingConfigurations()) {
			if (mAccount == null) {
	//			MyLog.i(TAG, "zll ----- isShoudSave 2");
				return false;
			}

	//		MyLog.i(TAG, "zll ----- isShoudSave 3");
			if (shouldSave()) {
	//			MyLog.i(TAG, "zll ----- isShoudSave 4");
				return true;
			}
		}

		return false;
	}

	private void doSave(boolean showToast) {
		sendOrSaveWithSanityChecks(true, showToast, false, false);
	}

	private void doSend() {
		sendOrSaveWithSanityChecks(false, true, false, false);
		logSendOrSave(false);
		mPerformedSendOrDiscard = true;
		saveAddress();//---- add by tangjie 2014/11/29 ----//
	}
	//----------------- add by tangjie 2014/11/29 start -------------------//
	private void saveAddress(){
		AuroraAutoCompleteDBHelper dbHelper = AuroraAutoCompleteDBHelper
				.getInstance(this);
		String tableName = mAccount.getEmailAddress().replace("@", "")
				.replace(".", "");
		Rfc822Token[] send_tokens = mAuroraComposeAddressView.getRfc822Tokens();
		dbHelper.saveAddress(send_tokens,tableName);
		Rfc822Token [] bcc_tokens = mBccaAddressView.getRfc822Tokens();
		dbHelper.saveAddress(bcc_tokens,tableName);
		Rfc822Token [] cc_tokens = mCcAddressView.getRfc822Tokens();
		dbHelper.saveAddress(cc_tokens,tableName);
	}
	//----------------- add by tangjie 2014/11/29 end -------------------//
	public static void registerTestSendOrSaveCallback(
			AuroraSendOrSaveCallback testCallback) {
		if (sTestSendOrSaveCallback != null && testCallback != null) {
			throw new IllegalStateException(
					"Attempting to register more than one test callback");
		}
		sTestSendOrSaveCallback = testCallback;
	}

	public void checkInvalidEmails(final String[] to,
			final List<String> wrongEmailsOut) {
		if (mValidator == null) {
			return;
		}
		for (final String email : to) {
			if (!mValidator.isValid(email)) {
				wrongEmailsOut.add(email);
			}
		}
	}

	protected boolean showEmptyTextWarnings() {
		return mAuroraFujianView.getAttachments().size() == 0;
	}

	public boolean isSubjectEmpty() {
		return TextUtils.getTrimmedLength(themeEditText.getText()) == 0;
	}

	public boolean isBodyEmpty() {
		return !mQuotedTextView.isTextIncluded();
	}

	protected boolean sendOrSaveWithSanityChecks(final boolean save,
			final boolean showToast, final boolean orientationChanged,
			final boolean autoSend) {
		if (mAccounts == null || mAccount == null) {
	//		MyLog.i(TAG, "zll ---- sendOrSaveWithSanityChecks 1");
			Toast.makeText(this, R.string.send_failed, Toast.LENGTH_SHORT)
					.show();
			AuroraNotificationUtils.getInstance(getApplicationContext()).showSendFailNotification(getApplicationContext());
			if (autoSend) {
				finish();
			}
			return false;
		}

		final String[] to, cc, bcc;
		if (orientationChanged) {
			to = cc = bcc = new String[0];
		} else {
			to = getToAddresses();
			cc = getCcAddresses();
			bcc = getBccAddresses();
		}

		// Don't let the user send to nobody (but it's okay to save a message
		// with no recipients)
		if (!save && (to.length == 0 && cc.length == 0 && bcc.length == 0)) {
			/*MyLog.i(TAG, "zll ---- sendOrSaveWithSanityChecks 2 to.length:"
					+ to.length + ",cc.length:" + cc.length + ",bcc.length:"
					+ bcc.length);*/
			showRecipientErrorDialog(getString(R.string.recipient_needed));
			return false;
		}

		List<String> wrongEmails = new ArrayList<String>();
		if (!save) {
			checkInvalidEmails(to, wrongEmails);
			checkInvalidEmails(cc, wrongEmails);
			checkInvalidEmails(bcc, wrongEmails);
		}

		// Don't let the user send an email with invalid recipients
		if (wrongEmails.size() > 0) {
		//	MyLog.i(TAG, "zll ---- sendOrSaveWithSanityChecks 3");
			String errorText = String.format(
					getString(R.string.invalid_recipient), wrongEmails.get(0));
			showRecipientErrorDialog(errorText);
			return false;
		}

		// Show a warning before sending only if there are no attachments.
		if (!save) {
			if (mAuroraFujianView.getAttachments().isEmpty()
					&& showEmptyTextWarnings()) {
				boolean warnAboutEmptySubject = isSubjectEmpty();
				boolean emptyBody = TextUtils.getTrimmedLength(mBodyView
						.getEditableText()) == 0;

				// A warning about an empty body may not be warranted when
				// forwarding mails, since a common use case is to forward
				// quoted text and not append any more text.
				boolean warnAboutEmptyBody = emptyBody
						&& (!mForward || isBodyEmpty());

				// When we bring up a dialog warning the user about a send,
				// assume that they accept sending the message. If they do not,
				// the dialog listener is required to enable sending again.
				if (warnAboutEmptySubject) {
				//	MyLog.i(TAG, "zll ---- sendOrSaveWithSanityChecks 4");
					showSendConfirmDialog(
							R.string.confirm_send_message_with_no_subject,
							save, showToast);
					return true;
				}

				if (warnAboutEmptyBody) {
				//	MyLog.i(TAG, "zll ---- sendOrSaveWithSanityChecks 5");
					showSendConfirmDialog(
							R.string.confirm_send_message_with_no_body, save,
							showToast);
					return true;
				}
			}
			// Ask for confirmation to send (if always required)
			if (showSendConfirmation()) {
			//	MyLog.i(TAG, "zll ---- sendOrSaveWithSanityChecks 6");
				showSendConfirmDialog(R.string.confirm_send_message, save,
						showToast);
				return true;
			}
		}

	//	MyLog.i(TAG, "zll ---- sendOrSaveWithSanityChecks 7");
		sendOrSave(save, showToast);
		return true;
	}

	private int getMode() {
		// int mode = ComposeActivity.COMPOSE;

		int mode = mComposeMode;
		return mode;
	}

	private void sendOrSave(final boolean save, final boolean showToast) {
		// Check if user is a monkey. Monkeys can compose and hit send
		// button but are not allowed to send anything off the device.
		if (ActivityManager.isUserAMonkey()) {
			return;
		}

		final Spanned body = mBodyView.getEditableText();

		MyAuroraSendOrSaveCallback callback = new MyAuroraSendOrSaveCallback();
/*		MyLog.i(TAG, "zll ----- sendOrSave 0 mReplyFromAccount.account:"
				+ mReplyFromAccount.account.toString() + ",mAccount:"
				+ mAccount.toString());*/
		// setAccount(mReplyFromAccount.account);
		setAccount(mAccount,false);

		if (mSendSaveTaskHandler == null) {
			HandlerThread handlerThread = new HandlerThread(
					"Send Message Task Thread");
			handlerThread.start();

			mSendSaveTaskHandler = new Handler(handlerThread.getLooper());
		}

		Message msg = createMessage(mReplyFromAccount, getMode());
		mRequestId = sendOrSaveInternal(this, mReplyFromAccount, msg,
				mRefMessage, body, mQuotedTextView.getQuotedTextIfIncluded(),
				callback, mSendSaveTaskHandler, save, mComposeMode,
				mDraftAccount, mExtraValues);

		// Don't display the toast if the user is just changing the orientation,
		// but we still need to save the draft to the cursor because this is how
		// we restore
		// the attachments when the configuration change completes.
		if (showToast
				&& (getChangingConfigurations() & ActivityInfo.CONFIG_ORIENTATION) == 0) {
		//	MyLog.i(TAG, "zll ---- sendOrSave 1");
			/*
			 * Toast.makeText(this, save ? R.string.message_saved :
			 * R.string.sending_message, Toast.LENGTH_SHORT).show();
			 */
		}

		// Need to update variables here because the send or save completes
		// asynchronously even though the toast shows right away.
	//	MyLog.i(TAG, "zll ---- sendOrSave 2 save:" + save);
		discardChanges();
		// updateSaveUi();

		// If we are sending, finish the activity
		if (!save) {
			finish();
		}
	}

	private static SpannableString removeComposingSpans(Spanned body) {
		final SpannableString messageBody = new SpannableString(body);
		BaseInputConnection.removeComposingSpans(messageBody);
		return messageBody;
	}

	private static int getDraftType(int mode) {
		int draftType = -1;
		switch (mode) {
		case ComposeActivity.COMPOSE:
			draftType = DraftType.COMPOSE;
			break;
		case ComposeActivity.REPLY:
			draftType = DraftType.REPLY;
			break;
		case ComposeActivity.REPLY_ALL:
			draftType = DraftType.REPLY_ALL;
			break;
		case ComposeActivity.FORWARD:
			draftType = DraftType.FORWARD;
			break;
		}
		return draftType;
	}

	static int sendOrSaveInternal(Context context,
			ReplyFromAccount replyFromAccount, Message message,
			final Message refMessage, Spanned body,
			final CharSequence quotedText, AuroraSendOrSaveCallback callback,
			Handler handler, boolean save, int composeMode,
			ReplyFromAccount draftAccount, final ContentValues extraValues) {
		final ContentValues values = new ContentValues();

		final String refMessageId = refMessage != null ? refMessage.uri
				.toString() : "";

		MessageModification.putToAddresses(values, message.getToAddresses());
		MessageModification.putCcAddresses(values, message.getCcAddresses());
		MessageModification.putBccAddresses(values, message.getBccAddresses());

		MessageModification.putCustomFromAddress(values, message.getFrom());

		MessageModification.putSubject(values, message.subject);
		// Make sure to remove only the composing spans from the Spannable
		// before saving.
		final String htmlBody = Html.toHtml(removeComposingSpans(body));

		boolean includeQuotedText = !TextUtils.isEmpty(quotedText);
		StringBuilder fullBody = new StringBuilder(htmlBody);
/*		MyLog.i(TAG, "zll ---- sendOrSaveInternal 1 includeQuotedText:"
				+ includeQuotedText);*/
		if (includeQuotedText) {
			// HTML gets converted to text for now
			final String text = quotedText.toString();
			if (QuotedTextView.containsQuotedText(text)) {
				int pos = QuotedTextView.getQuotedTextOffset(text);
				final int quoteStartPos = fullBody.length() + pos;
				fullBody.append(text);
				MessageModification.putQuoteStartPos(values, quoteStartPos);
				MessageModification.putForward(values,
						composeMode == ComposeActivity.FORWARD);
				MessageModification.putAppendRefMessageContent(values,
						includeQuotedText);
			} else {
				LogUtils.w(TAG, "Couldn't find quoted text");
				// This shouldn't happen, but just use what we have,
				// and don't do server-side expansion
				fullBody.append(text);
			}
		}
		int draftType = getDraftType(composeMode);
/*		MyLog.i(TAG, "zll ---- sendOrSaveInternal 2 draftType:" + draftType
				+ ",refMessage:" + refMessage);*/
		MessageModification.putDraftType(values, draftType);
		if (refMessage != null) {
			if (!TextUtils.isEmpty(refMessage.bodyHtml)) {
				MessageModification.putBodyHtml(values, fullBody.toString());
			}
			if (!TextUtils.isEmpty(refMessage.bodyText)) {
				MessageModification
						.putBody(
								values,
								Utils.convertHtmlToPlainText(
										fullBody.toString()).toString());
			}
		} else {
			MessageModification.putBodyHtml(values, fullBody.toString());
			MessageModification.putBody(values,
					Utils.convertHtmlToPlainText(fullBody.toString())
							.toString());
		}
		MessageModification.putAttachments(values, message.getAttachments());
		if (!TextUtils.isEmpty(refMessageId)) {
			MessageModification.putRefMessageId(values, refMessageId);
		}
		if (extraValues != null) {
			values.putAll(extraValues);
		}
		SendOrSaveMessage sendOrSaveMessage = new SendOrSaveMessage(context,
				replyFromAccount, values, refMessageId,
				message.getAttachments(), save);
		SendOrSaveTask sendOrSaveTask = new SendOrSaveTask(context,
				sendOrSaveMessage, callback, draftAccount);

		callback.initializeSendOrSave(sendOrSaveTask);
		// Do the send/save action on the specified handler to avoid possible
		// ANRs
		handler.post(sendOrSaveTask);

/*//		MyLog.i(TAG, "zll ---- sendOrSaveInternal 3 requestId():"
				+ sendOrSaveMessage.requestId());*/
		return sendOrSaveMessage.requestId();
	}

	private static String formatSenders(final String string) {
		if (!TextUtils.isEmpty(string)
				&& string.charAt(string.length() - 1) == ',') {
			return string.substring(0, string.length() - 1);
		}
		return string;
	}

	private Message createMessage(ReplyFromAccount selectedReplyFromAccount,
			int mode) {
		Message message = new Message();
		message.id = UIProvider.INVALID_MESSAGE_ID;
		message.serverId = null;
		message.uri = null;
		message.conversationUri = null;
		message.subject = themeEditText.getText().toString();// mSubject.getText().toString();
		message.snippet = null;
		/*
		 * message.setTo(formatSenders(mTo.getText().toString()));
		 * message.setCc(formatSenders(mCc.getText().toString()));
		 * message.setBcc(formatSenders(mBcc.getText().toString()));
		 */
		message.setTo(formatSenders(mAuroraComposeAddressView.getText()
				.toString()));
		message.setCc(formatSenders(mCcAddressView.getText().toString()));
		message.setBcc(formatSenders(mBccaAddressView.getText().toString()));
		message.setReplyTo(null);
		message.dateReceivedMs = 0;
		final String htmlBody = Html.toHtml(removeComposingSpans(mBodyView
				.getText()));
		final StringBuilder fullBody = new StringBuilder(htmlBody);
		message.bodyHtml = fullBody.toString();
		message.bodyText = mBodyView.getText().toString();
		message.embedsExternalResources = false;
		message.refMessageUri = mRefMessage != null ? mRefMessage.uri : null;
		message.appendRefMessageContent = mQuotedTextView
				.getQuotedTextIfIncluded() != null;
		ArrayList<Attachment> attachments = mAuroraFujianView.getAttachments();// mAttachmentsView.getAttachments();
		// paul modify
		if (attachments != null && attachments.size() > 0) {
			message.hasAttachments = 1;
		} else {
			message.hasAttachments = 0;
		}

		message.attachmentListUri = null;
		message.messageFlags = 0;
		message.alwaysShowImages = false;
		message.attachmentsJson = Attachment.toJSONArray(attachments);
		CharSequence quotedText = mQuotedTextView.getQuotedText();
		message.quotedTextOffset = !TextUtils.isEmpty(quotedText) ? QuotedTextView
				.getQuotedTextOffset(quotedText.toString()) : -1;
		message.accountUri = null;
		final String email = selectedReplyFromAccount != null ? selectedReplyFromAccount.address
				: mAccount != null ? mAccount.getEmailAddress() : null;
		// TODO: this behavior is wrong. Pull the name from
		// selectedReplyFromAccount.name
		final String senderName = mAccount != null ? mAccount.getSenderName()
				: null;
		final Address address = new Address(senderName, email);
		message.setFrom(address.pack());
		message.draftType = getDraftType(mode);

//		MyLog.i(TAG, "zll ---- createMessage message:" + message.toString());
		return message;
	}

	protected boolean showSendConfirmation() {
		return mCachedSettings != null ? mCachedSettings.confirmSend : false;
	}

	@VisibleForTesting
	protected ArrayList<Attachment> getAttachments() {
		return mAuroraFujianView.getAttachments();
	}

	private static String getActionString(int action) {
		final String msgType;
		switch (action) {
		case COMPOSE:
			msgType = "new_message";
			break;
		case REPLY:
			msgType = "reply";
			break;
		case REPLY_ALL:
			msgType = "reply_all";
			break;
		case FORWARD:
			msgType = "forward";
			break;
		default:
			msgType = "unknown";
			break;
		}
		return msgType;
	}

	private void logSendOrSave(boolean save) {
		if (!Analytics.isLoggable() || mAuroraFujianView == null) {
			return;
		}

		final String category = (save) ? "message_save" : "message_send";
		final int attachmentCount = getAttachments().size();
		final String msgType = getActionString(mComposeMode);
		final String label;
		final long value;
		if (mComposeMode == COMPOSE) {
			label = Integer.toString(attachmentCount);
			value = attachmentCount;
		} else {
			label = null;
			value = 0;
		}
		Analytics.getInstance().sendEvent(category, msgType, label, value);
	}

	private void showSendConfirmDialog(final int messageId, final boolean save,
			final boolean showToast) {
		final DialogFragment frag = SendConfirmDialogFragment.newInstance(
				messageId, save, showToast);
		frag.show(getFragmentManager(), "send confirm");
	}

	public static class SendConfirmDialogFragment extends DialogFragment {
		// Public no-args constructor needed for fragment re-instantiation
		public SendConfirmDialogFragment() {
		}

		public static SendConfirmDialogFragment newInstance(
				final int messageId, final boolean save, final boolean showToast) {
			final SendConfirmDialogFragment frag = new SendConfirmDialogFragment();
			final Bundle args = new Bundle(3);
			args.putInt("messageId", messageId);
			args.putBoolean("save", save);
			args.putBoolean("showToast", showToast);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int messageId = getArguments().getInt("messageId");
			final boolean save = getArguments().getBoolean("save");
			final boolean showToast = getArguments().getBoolean("showToast");

			return new AlertDialog.Builder(getActivity())
					.setMessage(messageId)
					.setTitle(R.string.confirm_send_title)
					.setIconAttribute(android.R.attr.alertDialogIcon)
					.setPositiveButton(R.string.send,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((AuroraComposeActivity) getActivity())
											.finishSendConfirmDialog(save,
													showToast);
								}
							}).create();
		}
	}

	private void finishSendConfirmDialog(final boolean save,
			final boolean showToast) {
		sendOrSave(save, showToast);
	}

	public static class SaveDraftConfirmDialogFragment extends DialogFragment {

		public SaveDraftConfirmDialogFragment() {
		}

		public static SaveDraftConfirmDialogFragment newInstance(
				final int messageId, final boolean save) {
			final SaveDraftConfirmDialogFragment frag = new SaveDraftConfirmDialogFragment();
			final Bundle args = new Bundle(2);
			args.putInt("messageId", messageId);
			args.putBoolean("save", save);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int messageId = getArguments().getInt("messageId");
			final boolean save = getArguments().getBoolean("save");

			return new AlertDialog.Builder(getActivity())
					.setMessage(messageId)
					.setCancelable(false)
					.setPositiveButton(R.string.aurora_save,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									((AuroraComposeActivity) getActivity())
											.doSaveDraftWithoutConfirmation(true);
								}
							})
					.setNegativeButton(R.string.aurora_cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									((AuroraComposeActivity) getActivity())
											.doSaveDraftWithoutConfirmation(false);
								}
							}).create();
		}
	}

	private void doSaveDraftWithoutConfirmation(boolean save) {

//		MyLog.i(TAG, "zll ---- doSaveDraftWithoutConfirmation save:" + save);
		if (save) {
			sendOrSave(save, false);
		}

		finish();
	}

	private void doSaveDraft(final int messageId, final boolean save) {
		final DialogFragment frag = SaveDraftConfirmDialogFragment.newInstance(
				messageId, save);
		frag.show(getFragmentManager(), "discard confirm");
	}

	private void showRecipientErrorDialog(final String message) {
		final DialogFragment frag = RecipientErrorDialogFragment
				.newInstance(message);
		frag.show(getFragmentManager(), "recipient error");
	}

	public static class RecipientErrorDialogFragment extends DialogFragment {
		// Public no-args constructor needed for fragment re-instantiation
		public RecipientErrorDialogFragment() {
		}

		public static RecipientErrorDialogFragment newInstance(
				final String message) {
			final RecipientErrorDialogFragment frag = new RecipientErrorDialogFragment();
			final Bundle args = new Bundle(1);
			args.putString("message", message);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final String message = getArguments().getString("message");
			return new AlertDialog.Builder(getActivity())
					.setMessage(message)
					.setTitle(R.string.recipient_error_dialog_title)
					.setIconAttribute(android.R.attr.alertDialogIcon)
					.setPositiveButton(R.string.ok,
							new Dialog.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									((AuroraComposeActivity) getActivity())
											.finishRecipientErrorDialog();
								}
							}).create();
		}
	}

	private void finishRecipientErrorDialog() {
		// after the user dismisses the recipient error dialog we want to make
		// sure to refocus the
		// recipient to field so they can fix the issue easily
		/*
		 * if (mTo != null) { mTo.requestFocus(); }
		 */
	}

	private void saveRequestMap() {
		// TODO: store the request map in user preferences.
	}

	@VisibleForTesting
	public interface AuroraSendOrSaveCallback {
		public void initializeSendOrSave(SendOrSaveTask sendOrSaveTask);

		public void notifyMessageIdAllocated(
				SendOrSaveMessage sendOrSaveMessage, Message message);

		public Message getMessage();

		public void sendOrSaveFinished(SendOrSaveTask sendOrSaveTask,
				boolean success);
	}

	// for sending animation start
	private static AuroraSendStatusCallback mSendStatusCallback = null;
	private int mSendNum = 0;

	public static enum AuroraSendStatus {
		AURORAEMAIL_SEND_START, AURORAEMAIL_SEND_FINISEHED, AURORAEMAIL_SEND_FAIL, AURORAEMAIL_SEND_SUCCES
	}

	public interface AuroraSendStatusCallback {
		public void onSendStatusChanged(AuroraSendStatus status);
	}

	public static void registerSendStatusCallback(
			AuroraSendStatusCallback sendcallback) {
		if (mSendStatusCallback != null && sendcallback != null) {
			// throw new
			// IllegalStateException("zll Attempting to register more than one test callback");
			return;
		}
		mSendStatusCallback = sendcallback;
	}

	// for sending animation end

	@VisibleForTesting
	public static class SendOrSaveTask implements Runnable {
		private final Context mContext;
		@VisibleForTesting
		public final AuroraSendOrSaveCallback mSendOrSaveCallback;
		@VisibleForTesting
		public final SendOrSaveMessage mSendOrSaveMessage;
		private ReplyFromAccount mExistingDraftAccount;

		public SendOrSaveTask(Context context, SendOrSaveMessage message,
				AuroraSendOrSaveCallback callback, ReplyFromAccount draftAccount) {
			mContext = context;
			mSendOrSaveCallback = callback;
			mSendOrSaveMessage = message;
			mExistingDraftAccount = draftAccount;
		}

		@Override
		public void run() {
			final SendOrSaveMessage sendOrSaveMessage = mSendOrSaveMessage;

			final ReplyFromAccount selectedAccount = sendOrSaveMessage.mAccount;
			Message message = mSendOrSaveCallback.getMessage();
			long messageId = message != null ? message.id
					: UIProvider.INVALID_MESSAGE_ID;
			// If a previous draft has been saved, in an account that is
			// diffmAuroraFujianViewerent
			// than what the user wants to send from, remove the old draft, and
			// treat this
			// as a new message

//			MyLog.i(TAG, "zll ---- SendOrSaveTask run 1 messageId:" + messageId);
			if (mExistingDraftAccount != null
					&& !selectedAccount.account.uri
							.equals(mExistingDraftAccount.account.uri)) {
				if (messageId != UIProvider.INVALID_MESSAGE_ID) {
					ContentResolver resolver = mContext.getContentResolver();
					ContentValues values = new ContentValues();
					values.put(BaseColumns._ID, messageId);
					if (mExistingDraftAccount.account.expungeMessageUri != null) {
						new ContentProviderTask.UpdateTask()
								.run(resolver,
										mExistingDraftAccount.account.expungeMessageUri,
										values, null, null);
					} else {
						// TODO(mindyp) delete the conversation.
					}
					// reset messageId to 0, so a new message will be created
					messageId = UIProvider.INVALID_MESSAGE_ID;
				}
			}

			final long messageIdToSave = messageId;
			int err = sendOrSaveMessage(messageIdToSave, sendOrSaveMessage,
					selectedAccount);

			if (!sendOrSaveMessage.mSave) {
				incrementRecipientsTimesContacted(mContext,
						(String) sendOrSaveMessage.mValues
								.get(UIProvider.MessageColumns.TO));
				incrementRecipientsTimesContacted(mContext,
						(String) sendOrSaveMessage.mValues
								.get(UIProvider.MessageColumns.CC));
				incrementRecipientsTimesContacted(mContext,
						(String) sendOrSaveMessage.mValues
								.get(UIProvider.MessageColumns.BCC));
			}

//			MyLog.i(TAG, "zll SendOrSaveTask run 2 xxxx err:" + err);
			if (err != 0) {
				mSendOrSaveCallback.sendOrSaveFinished(SendOrSaveTask.this,
						false);
			} else {
				mSendOrSaveCallback.sendOrSaveFinished(SendOrSaveTask.this,
						true);
			}
		}

		private static void incrementRecipientsTimesContacted(
				final Context context, final String addressString) {
			if (TextUtils.isEmpty(addressString)) {
				return;
			}
			final Rfc822Token[] tokens = Rfc822Tokenizer
					.tokenize(addressString);
			final ArrayList<String> recipients = new ArrayList<String>(
					tokens.length);
			for (int i = 0; i < tokens.length; i++) {
				recipients.add(tokens[i].getAddress());
			}
			final DataUsageStatUpdater statsUpdater = new DataUsageStatUpdater(
					context);
			statsUpdater.updateWithAddress(recipients);
		}

		/**
		 * Send or Save a message.
		 */
		private int sendOrSaveMessage(final long messageIdToSave,
				final SendOrSaveMessage sendOrSaveMessage,
				final ReplyFromAccount selectedAccount) {
			final ContentResolver resolver = mContext.getContentResolver();
			final boolean updateExistingMessage = messageIdToSave != UIProvider.INVALID_MESSAGE_ID;

			final String accountMethod = sendOrSaveMessage.mSave ? UIProvider.AccountCallMethods.SAVE_MESSAGE
					: UIProvider.AccountCallMethods.SEND_MESSAGE;

			try {
/*				MyLog.i(TAG, "zll sendOrSaveMessage 1.1 updateExistingMessage:"
						+ updateExistingMessage + ",accountMethod:"
						+ accountMethod);*/
				if (updateExistingMessage) {
					sendOrSaveMessage.mValues.put(BaseColumns._ID,
							messageIdToSave);
					final Bundle result = callAccountSendSaveMethod(resolver,
							selectedAccount.account, accountMethod,
							sendOrSaveMessage);
	//				MyLog.i(TAG, "zll sendOrSaveMessage 1.2 result:" + result);
					if (result == null) {
						return -1;
					}

					return 0;
				} else {
					Uri messageUri = null;
					final Bundle result = callAccountSendSaveMethod(resolver,
							selectedAccount.account, accountMethod,
							sendOrSaveMessage);
//					MyLog.i(TAG, "zll sendOrSaveMessage 1.3 result:" + result);
					if (result != null) {
						// If a non-null value was returned, then the provider
						// handled the call
						// method
						messageUri = result
								.getParcelable(UIProvider.MessageColumns.URI);
					} else {
						return -1;
					}

					if (sendOrSaveMessage.mSave && messageUri != null) {
						final Cursor messageCursor = resolver
								.query(messageUri,
										UIProvider.MESSAGE_PROJECTION, null,
										null, null);
						if (messageCursor != null) {
							try {
								if (messageCursor.moveToFirst()) {
									// Broadcast notification that a new message
									// has been allocated
/*									MyLog.i(TAG,
											"zll sendOrSaveMessage 1.4 xxxx");*/
									mSendOrSaveCallback
											.notifyMessageIdAllocated(
													sendOrSaveMessage,
													new Message(messageCursor));
/*
									MyLog.i(TAG,
											"zll sendOrSaveMessage 1.5 xxxx");*/
									return 0;
								}
							} finally {
								messageCursor.close();
							}
						}
					}

					return 0;
				}
			} finally {
				// Close any opened file descriptors
				closeOpenedAttachmentFds(sendOrSaveMessage);
			}

			// MyLog.i(TAG, "zll sendOrSaveMessage 1.6 xxxx");
			// return -1;
		}

		private static void closeOpenedAttachmentFds(
				final SendOrSaveMessage sendOrSaveMessage) {
			final Bundle openedFds = sendOrSaveMessage.attachmentFds();
			if (openedFds != null) {
				final Set<String> keys = openedFds.keySet();
				for (final String key : keys) {
					final ParcelFileDescriptor fd = openedFds
							.getParcelable(key);
					if (fd != null) {
						try {
							fd.close();
						} catch (IOException e) {
							// Do nothing
						}
					}
				}
			}
		}

		/**
		 * Use the {@link ContentResolver#call} method to send or save the
		 * message.
		 * 
		 * If this was successful, this method will return an non-null Bundle
		 * instance
		 */
		private static Bundle callAccountSendSaveMethod(
				final ContentResolver resolver, final Account account,
				final String method, final SendOrSaveMessage sendOrSaveMessage) {
			// Copy all of the values from the content values to the bundle
			final Bundle methodExtras = new Bundle(
					sendOrSaveMessage.mValues.size());
			final Set<Entry<String, Object>> valueSet = sendOrSaveMessage.mValues
					.valueSet();

			for (Entry<String, Object> entry : valueSet) {
				final Object entryValue = entry.getValue();
				final String key = entry.getKey();
				if (entryValue instanceof String) {
					methodExtras.putString(key, (String) entryValue);
				} else if (entryValue instanceof Boolean) {
					methodExtras.putBoolean(key, (Boolean) entryValue);
				} else if (entryValue instanceof Integer) {
					methodExtras.putInt(key, (Integer) entryValue);
				} else if (entryValue instanceof Long) {
					methodExtras.putLong(key, (Long) entryValue);
				} else {
					LogUtils.wtf(TAG, "Unexpected object type: %s", entryValue
							.getClass().getName());
				}
			}

			// If the SendOrSaveMessage has some opened fds, add them to the
			// bundle
			final Bundle fdMap = sendOrSaveMessage.attachmentFds();
			if (fdMap != null) {
				methodExtras.putParcelable(
						UIProvider.SendOrSaveMethodParamKeys.OPENED_FD_MAP,
						fdMap);
			}

			return resolver.call(account.uri, method, account.uri.toString(),
					methodExtras);
		}
	}

	@VisibleForTesting
	public static class SendOrSaveMessage {
		final ReplyFromAccount mAccount;
		final ContentValues mValues;
		final String mRefMessageId;
		@VisibleForTesting
		public final boolean mSave;
		final int mRequestId;
		private final Bundle mAttachmentFds;

		public SendOrSaveMessage(Context context, ReplyFromAccount account,
				ContentValues values, String refMessageId,
				List<Attachment> attachments, boolean save) {
			mAccount = account;
			mValues = values;
			mRefMessageId = refMessageId;
			mSave = save;
			mRequestId = mValues.hashCode() ^ hashCode();

			mAttachmentFds = initializeAttachmentFds(context, attachments);
		}

		int requestId() {
			return mRequestId;
		}

		Bundle attachmentFds() {
			return mAttachmentFds;
		}

		/**
		 * Opens {@link ParcelFileDescriptor} for each of the attachments. This
		 * method must be called before the ComposeActivity finishes. Note: The
		 * caller is responsible for closing these file descriptors.
		 */
		private static Bundle initializeAttachmentFds(final Context context,
				final List<Attachment> attachments) {
			if (attachments == null || attachments.size() == 0) {
				return null;
			}

			final Bundle result = new Bundle(attachments.size());
			final ContentResolver resolver = context.getContentResolver();

			for (Attachment attachment : attachments) {
				if (attachment == null || Utils.isEmpty(attachment.contentUri)) {
					continue;
				}

				ParcelFileDescriptor fileDescriptor;
				try {
					fileDescriptor = resolver.openFileDescriptor(
							attachment.contentUri, "r");
				} catch (FileNotFoundException e) {
					LogUtils.e(TAG, e,
							"Exception attempting to open attachment");
					fileDescriptor = null;
				} catch (SecurityException e) {
					// We have encountered a security exception when attempting
					// to open the file
					// specified by the content uri. If the attachment has been
					// cached, this
					// isn't a problem, as even through the original permission
					// may have been
					// revoked, we have cached the file. This will happen when
					// saving/sending
					// a previously saved draft.
					// TODO(markwei): Expose whether the attachment has been
					// cached through the
					// attachment object. This would allow us to limit when the
					// log is made, as
					// if the attachment has been cached, this really isn't an
					// error
					LogUtils.e(TAG, e,
							"Security Exception attempting to open attachment");
					// Just set the file descriptor to null, as the underlying
					// provider needs
					// to handle the file descriptor not being set.
					fileDescriptor = null;
				}

				if (fileDescriptor != null) {
					result.putParcelable(attachment.contentUri.toString(),
							fileDescriptor);
				}
			}

			return result;
		}
	}

	private class MyAuroraSendOrSaveCallback implements
			AuroraSendOrSaveCallback {

		@Override
		public void initializeSendOrSave(SendOrSaveTask sendOrSaveTask) {

			int tnumTask = -1;
			synchronized (mActiveTasks) {
				int numTasks = mActiveTasks.size();
				tnumTask = numTasks;
				if (numTasks == 0) {
					// Start service so we won't be killed if this app is
					// put in the background.
					startService(new Intent(AuroraComposeActivity.this,
							EmptyService.class));
				}

				mActiveTasks.add(sendOrSaveTask);
			}

			if (tnumTask == 0
					&& mSendStatusCallback != null
					&& (sendOrSaveTask != null
							&& sendOrSaveTask.mSendOrSaveMessage != null && !sendOrSaveTask.mSendOrSaveMessage.mSave)) {
//				MyLog.i(TAG, "zll --- initializeSendOrSave 1 -----");
				mSendStatusCallback
						.onSendStatusChanged(AuroraSendStatus.AURORAEMAIL_SEND_START);
				AuroraNotificationUtils.getInstance(AuroraComposeActivity.this)
						.setAuroraSendStatusCallback(mSendStatusCallback);
				AuroraNotificationUtils.getInstance(AuroraComposeActivity.this)
						.showSendingNotification(AuroraComposeActivity.this);
				AuroraNotificationUtils.getInstance(AuroraComposeActivity.this)
						.setSendingState(true);
			}

			if (sTestSendOrSaveCallback != null) {
				sTestSendOrSaveCallback.initializeSendOrSave(sendOrSaveTask);
			}
		}

		@Override
		public void notifyMessageIdAllocated(
				SendOrSaveMessage sendOrSaveMessage, Message message) {
			synchronized (mDraftLock) {
				mDraftAccount = sendOrSaveMessage.mAccount;
				mDraftId = message.id;
				mDraft = message;
				if (sRequestMessageIdMap != null) {
					sRequestMessageIdMap.put(sendOrSaveMessage.requestId(),
							mDraftId);
				}
				// Cache request message map, in case the process is killed
				saveRequestMap();
			}
			if (sTestSendOrSaveCallback != null) {
				sTestSendOrSaveCallback.notifyMessageIdAllocated(
						sendOrSaveMessage, message);
			}
		}

		@Override
		public Message getMessage() {
			synchronized (mDraftLock) {
				return mDraft;
			}
		}

		@Override
		public void sendOrSaveFinished(SendOrSaveTask task, boolean success) {
			if (mAccount != null) {
				MailAppProvider.getInstance().setLastSentFromAccount(
						mAccount.uri.toString());
			}

//			MyLog.i(TAG, "zll --- sendOrSaveFinished 1 success:" + success);
			if (success) {
				// Successfully sent or saved so reset change markers
				discardChanges();
			} else {
				// A failure happened with saving/sending the draft
				// TODO(pwestbro): add a better string that should be used
				// when failing to send or save
				Toast.makeText(AuroraComposeActivity.this,
						R.string.send_failed, Toast.LENGTH_SHORT).show();
			}

			int numTasks;
			synchronized (mActiveTasks) {
				// Remove the task from the list of active tasks
				mActiveTasks.remove(task);
				numTasks = mActiveTasks.size();
			}

			if (numTasks == 0) {
				// Stop service so we can be killed.
				stopService(new Intent(AuroraComposeActivity.this,
						EmptyService.class));

				/*
				 * if (mSendStatusCallback != null) { MyLog.i(TAG,
				 * "zll --- sendOrSaveFinished 2 -----"); mSendStatusCallback
				 * .onSendStatusChanged
				 * (success?AuroraSendStatus.AURORAEMAIL_SEND_SUCCES
				 * :AuroraSendStatus.AURORAEMAIL_SEND_FAIL); } if(success){
				 * AuroraNotificationUtils
				 * .getInstance(AuroraComposeActivity.this
				 * ).showSendSuccessNotification(AuroraComposeActivity.this);
				 * }else{
				 * AuroraNotificationUtils.getInstance(AuroraComposeActivity
				 * .this).showSendFailNotification(AuroraComposeActivity.this);
				 * }
				 */
			}

			if (sTestSendOrSaveCallback != null) {
				sTestSendOrSaveCallback.sendOrSaveFinished(task, success);
			}
		}

	}

	// ------------ lory add 2014.10.29 end ------------------//
	// ------------ lory add 2014.10.04 start ------------------//
	@Override
	public void moveFocus(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case R.id.id_write_email:
			mBodyView.requestFocus();
			break;
		case R.id.aurora_et_title:
			themeEditText.findViewById(id).requestFocus();
			break;
		case SENDVIEW_ENABLE_TRUE:
			if (sendView.isEnabled() == false
					&& !mAuroraFujianView.isMoreSize()) {
				sendView.setEnabled(true);
			}
			break;
		case SENDVIEW_ENABLE_FALSE:
			if (mAuroraComposeAddressView.getAdrressCount() == 0
					&& mBccaAddressView.getAdrressCount() == 0
					&& mCcAddressView.getAdrressCount() == 0
					&& sendView.isEnabled()) {
				sendView.setEnabled(false);
			}
			break;
		case ATTACHMENT_MORE_SIZE:
			if (mAuroraFujianView.isMoreSize()) {
				sendView.setEnabled(false);
			} else if (mAuroraComposeAddressView.getAdrressCount() != 0
					|| mBccaAddressView.getAdrressCount() != 0
					|| mCcAddressView.getAdrressCount() != 0) {
				sendView.setEnabled(true);
			}
			break;
		default:
			break;
		}
	};
	// ------------ lory add 2014.11.04 end ------------------//
	@Override
	public void chang(Account account) {
		// TODO Auto-generated method stub
		setAccount(account,false);
	}
}
