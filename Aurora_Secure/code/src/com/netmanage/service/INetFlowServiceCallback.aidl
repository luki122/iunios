package com.netmanage.service;   

import com.netmanage.data.FlowData;

interface INetFlowServiceCallback   
{   
    void valueChanged(in List<com.netmanage.data.FlowData> flowList);
}  
