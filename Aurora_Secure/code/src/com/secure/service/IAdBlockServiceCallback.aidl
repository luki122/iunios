package com.secure.service;   


import com.adblock.data.AidlAdData;

interface IAdBlockServiceCallback   
{    	
	void addAidlAdData(in com.adblock.data.AidlAdData aidlAdData);	
	void deleteAidlAdData(in String pkgName);
}  
