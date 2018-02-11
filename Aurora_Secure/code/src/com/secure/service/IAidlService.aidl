package com.secure.service;

 interface IAidlService {
   boolean closeApkNetwork(in String packageName);
   boolean openApkNetwork(in String packageName);
   void wantGetDataFromContentProvider();
 }
