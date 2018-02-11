/*
Copyright 2012 Aphid Mobile

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.aurora.calender;

import com.aurora.launcher.R;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;


public class NumberImageView extends ImageView{

  private int number;
  int dayResIdList[] = new int[31];// everyday in month

  public NumberImageView(Context context, int number) {
    super(context);
    setNumber(number);
   
   
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
    setImageResource(dayResIdList[number]);
  
  }

  @Override
  public String toString() {
    return "NumberTextView: " + number;
  }
  
  
  
}
