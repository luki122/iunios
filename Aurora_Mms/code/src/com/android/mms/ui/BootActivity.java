/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2012. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.android.mms.ui;

import aurora.app.AuroraActivity;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.os.SystemProperties; 
import com.android.mms.MmsConfig;
//gionee gaoj 2012-3-22 added for CR00555790 start
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import com.aurora.mms.ui.AuroraConvListActivity;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import com.android.mms.MmsApp;
//gionee gaoj 2012-3-22 added for CR00555790 end

public class BootActivity extends AuroraActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        Intent intent;
          // Aurora liugj 2013-10-31 modified for aurora's new feature start 
        //MTK_OP01_PROTECT_START
        /*boolean dirMode;
        dirMode = MmsConfig.getMmsDirMode();
        if (MmsApp.isTelecomOperator() && dirMode) {
            intent = new Intent(this, FolderViewList.class);
            intent.putExtra("floderview_key", FolderViewList.OPTION_INBOX);// show inbox by default
        } else 
        //MTK_OP01_PROTECT_END
        {*/
            //gionee gaoj 2012-3-22 added for CR00555790 start
           /* if (MmsApp.mGnMessageSupport) {*/
                // Aurora liugj 2013-09-13 modified for aurora's new feature start
                intent = new Intent(this, AuroraConvListActivity.class)
                // Aurora liugj 2013-09-13 modified for aurora's new feature end
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            /*} else {
            intent = new Intent(this, ConversationList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }*/
            //gionee gaoj 2012-3-22 added for CR00555790 end
        //}
          // Aurora liugj 2013-10-31 modified for aurora's new feature end
        startActivity(intent);
        finish();
    }
}

