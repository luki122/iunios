package com.secure.service;

import  com.secure.service.IAdBlockServiceCallback;

interface IAdBlockService {
   	void unregisterCallback(String clientPkgName);
    void registerCallback(IAdBlockServiceCallback mCallback,String clientPkgName);
    void getDataFromSecure(String clientPkgName);
 }
