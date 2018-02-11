/*
 * Copyright (C) 2013 Google Inc.
 * Licensed to The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mail.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.mail.FormattedDateBuilder;
import com.android.mail.R;
import com.android.mail.browse.BorderView;
import com.android.mail.browse.ConversationMessage;
import com.android.mail.browse.ConversationViewAdapter;
import com.android.mail.browse.ConversationViewAdapter.MessageHeaderItem;
import com.android.mail.browse.ConversationViewHeader;
import com.android.mail.browse.MessageFooterView;
import com.android.mail.browse.MessageHeaderView;
import com.android.mail.browse.MessageScrollView;
import com.android.mail.browse.MessageWebView;
import com.android.mail.providers.Message;
import com.android.mail.utils.ConversationViewUtils;

/**
 * Controller to do most of the heavy lifting for {@link SecureConversationViewFragment}
 * and {@link com.android.mail.browse.EmlMessageViewFragment}. Currently that work is
 * pretty much the rendering logic.
 */
public class SecureConversationViewController implements
        MessageHeaderView.MessageHeaderViewCallbacks {
    private static final String BEGIN_HTML =
            "<body style=\"margin: 0 %spx;\"><div style=\"margin: 16px 0; font-size: 100%%\">";//paul modify 80%
    private static final String END_HTML = "</div></body>";

    private final SecureConversationViewControllerCallbacks mCallbacks;

    private MessageWebView mWebView;
    //private ConversationViewHeader mConversationHeaderView; paul del
    private MessageHeaderView mMessageHeaderView;
    private MessageFooterView mMessageFooterView;
	private View mFooterBorderLine; //paul add
    private ConversationMessage mMessage;
    private MessageScrollView mScrollView;

    private ConversationViewProgressController mProgressController;
    private FormattedDateBuilder mDateBuilder;

    private int mSideMarginInWebPx;

    public SecureConversationViewController(SecureConversationViewControllerCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.secure_conversation_view, container, false);
        mScrollView = (MessageScrollView) rootView.findViewById(R.id.scroll_view);
        //mConversationHeaderView = (ConversationViewHeader) rootView.findViewById(R.id.conv_header); paul del
        mMessageHeaderView = (MessageHeaderView) rootView.findViewById(R.id.message_header);
        mMessageFooterView = (MessageFooterView) rootView.findViewById(R.id.message_footer);
		mFooterBorderLine = rootView.findViewById(R.id.footer_border_line);//paul add
        // Add color backgrounds to the header and footer.
        // Otherwise the backgrounds are grey. They can't
        // be set in xml because that would add more overdraw
        // in ConversationViewFragment.
        final int color = rootView.getResources().getColor(
                R.color.message_header_background_color);
        mMessageHeaderView.setBackgroundColor(color);
        mMessageFooterView.setBackgroundColor(color);

		//paul del
        //((BorderView) rootView.findViewById(R.id.top_border)).disableCardBottomBorder();
        //((BorderView) rootView.findViewById(R.id.bottom_border)).disableCardTopBorder();

        mProgressController = new ConversationViewProgressController(
                mCallbacks.getFragment(), mCallbacks.getHandler());
        mProgressController.instantiateProgressIndicators(rootView);
        mWebView = (MessageWebView) rootView.findViewById(R.id.webview);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mWebView.setWebViewClient(mCallbacks.getWebViewClient());
        mWebView.setFocusable(false);
        final WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        ConversationViewUtils.setTextZoom(mCallbacks.getFragment().getResources(), settings);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(90);    //cjs add 
        mScrollView.setInnerScrollableView(mWebView);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        //mCallbacks.setupConversationHeaderView(mConversationHeaderView); paul del
        final Fragment fragment = mCallbacks.getFragment();

        mDateBuilder = new FormattedDateBuilder(fragment.getActivity());
        mMessageHeaderView.initialize(
                mCallbacks.getConversationAccountController(), mCallbacks.getAddressCache());
        mMessageHeaderView.setExpandMode(MessageHeaderView.DEFAULT_MODE);//POPUP_MODE paul modify
        mMessageHeaderView.setContactInfoSource(mCallbacks.getContactInfoSource());
        mMessageHeaderView.setCallbacks(this);
        mMessageHeaderView.setExpandable(false);
        mMessageHeaderView.setViewOnlyMode(mCallbacks.isViewOnlyMode());

        mCallbacks.setupMessageHeaderVeiledMatcher(mMessageHeaderView);

        mMessageFooterView.initialize(fragment.getLoaderManager(), fragment.getFragmentManager());

        mCallbacks.startMessageLoader();

        mProgressController.showLoadingStatus(mCallbacks.isViewVisibleToUser());

        final Resources r = mCallbacks.getFragment().getResources();
        mSideMarginInWebPx = (int) (r.getDimensionPixelOffset(
                R.dimen.conversation_message_content_margin_side) / r.getDisplayMetrics().density);
    }

    /**
     * Populate the adapter with overlay views (message headers, super-collapsed
     * blocks, a conversation header), and return an HTML document with spacer
     * divs inserted for all overlays.
     */
    public void renderMessage(ConversationMessage message) {
        mMessage = message;

        mWebView.getSettings().setBlockNetworkImage(false);//paul modify (!mMessage.alwaysShowImages);

        // Add formatting to message body
        // At this point, only adds margins.
        StringBuilder dataBuilder = new StringBuilder(
                String.format(BEGIN_HTML, mSideMarginInWebPx));
        dataBuilder.append(mMessage.getBodyAsHtml());
        dataBuilder.append(END_HTML);

        mWebView.loadDataWithBaseURL(mCallbacks.getBaseUri(), dataBuilder.toString(),
                "text/html", "utf-8", null);
        final MessageHeaderItem item = ConversationViewAdapter.newMessageHeaderItem(
                null, mDateBuilder, mMessage, true, mMessage.alwaysShowImages);
        // Clear out the old info from the header before (re)binding
        mMessageHeaderView.unbind();
        mMessageHeaderView.bind(item, false);
        if (1 == mMessage.hasAttachments) {//paul modify
            mMessageFooterView.setVisibility(View.VISIBLE);
			mFooterBorderLine.setVisibility(View.VISIBLE);//paul add
            mMessageFooterView.bind(item, mCallbacks.getAccountUri(), false);
        }
    }

    public ConversationMessage getMessage() {
        return mMessage;
    }
	
//paul modify
/*
    public ConversationViewHeader getConversationHeaderView() {
        return mConversationHeaderView;
    }
*/
    public MessageHeaderView getMessageHeaderView() {
        return mMessageHeaderView;
    }

	

    public void dismissLoadingStatus() {
        mProgressController.dismissLoadingStatus();
    }

    public void setSubject(String subject) {
        mMessageHeaderView.setSubject(subject);// mConversationHeaderView.setSubject(subject); paul modify
    }

    // Start MessageHeaderViewCallbacks implementations

    @Override
    public void setMessageSpacerHeight(MessageHeaderItem item, int newSpacerHeight) {
        // Do nothing.
    }

    @Override
    public void setMessageExpanded(MessageHeaderItem item, int newSpacerHeight,
            int topBorderHeight, int bottomBorderHeight) {
        // Do nothing.
    }

    @Override
    public void setMessageDetailsExpanded(MessageHeaderItem i, boolean expanded, int heightBefore) {
        // Do nothing.
    }

    @Override
    public void showExternalResources(final Message msg) {
        mWebView.getSettings().setBlockNetworkImage(false);
    }

    @Override
    public void showExternalResources(final String rawSenderAddress) {
        mWebView.getSettings().setBlockNetworkImage(false);
    }

    @Override
    public boolean supportsMessageTransforms() {
        return false;
    }

    @Override
    public String getMessageTransforms(final Message msg) {
        return null;
    }

    @Override
    public FragmentManager getFragmentManager() {
        return mCallbacks.getFragment().getFragmentManager();
    }

    // End MessageHeaderViewCallbacks implementations
}
