/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gn.com.android.emsvr;

/**
 *
 * @author MTK80905
 */
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

public class Client {


	//==============parameter type====================
    public static final int PARAM_TYPE_STRING = 1;
    public static final int PARAM_TYPE_INT = 2;
    
    //private Socket socket;
    LocalSocket socket;
    private DataInputStream in;
    private DataOutputStream out;   
    private int status = 0;

    public void StartClient() {
        try {
            //socket = new Socket("127.0.0.1", 37121);
            //socket.setSoTimeout(10000);
            
            socket = new LocalSocket();
            socket.connect(new LocalSocketAddress("EngineerModeServer"));//, LocalSocketAddress.Namespace.FILESYSTEM

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            Log.e("EMX", "startclient EXP " + e.getMessage());
            if (e.getMessage().equals("Connection refused: connect")) {
                
            } else if (e.getMessage().equals("Read timed out")) {
               
            } else {
                
            }
            status = -1;
        }
        status = 0;
    }

    synchronized String Read() throws IOException {
        if( status == -1 || in == null)
            throw new IOException("INIT ERR OR NOT INIT.");

        int len = in.readInt();
        byte bb[] = new byte[len];
        int x = in.read(bb, 0, len);
        if (-1 == x) {
            return "";
        }
        return new String(bb);
    }

    synchronized void WriteFunctionNo(String s) throws IOException {
        if( status == -1 || out == null)
            throw new IOException("NOT INIT");
        if (s == null || s.length() == 0) {
            return;
        }
        out.writeInt(s.length());
        out.write(s.getBytes(), 0, s.length());
        return;
    }
    
    synchronized void WriteParamNo(int param_num) throws IOException {
        if( status == -1 || out == null)
            throw new IOException("NOT INIT");
        if (param_num < 0) {
            throw new IOException("param < 0");
        }
        out.writeInt(param_num);        
        return;
    }
    synchronized void WriteParamInt(int param) throws IOException {
        if( status == -1 || out == null)
            throw new IOException("NOT INIT");
        
        out.writeInt(PARAM_TYPE_INT);
        out.writeInt(4);
        out.writeInt(param);        
        return;
    }
    synchronized void WriteParamString(String s) throws IOException {
        if( status == -1 || out == null)
            throw new IOException("NOT INIT");
        
        out.writeInt(PARAM_TYPE_STRING);
        out.writeInt(s.length());
        out.write(s.getBytes(), 0, s.length());      
        return;
    }

    void StopClient() {
    	 if(in == null || out == null|| socket == null)
    		 return;
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}


