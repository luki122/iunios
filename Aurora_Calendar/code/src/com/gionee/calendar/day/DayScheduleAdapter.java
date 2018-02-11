package com.gionee.calendar.day;

import java.util.ArrayList;

import com.gionee.calendar.view.Log;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.Vector;
//Gionee <pengwei><2013-04-16> modify for DayView begin
public class DayScheduleAdapter extends BaseAdapter{
        private Vector<DayScheduleInterface> daySchedules;
         

         

        public DayScheduleAdapter(Context context,Vector<DayScheduleInterface> daySchedules){
            this.daySchedules = daySchedules;
        }

        @Override

        public int getCount() {

            // TODO Auto-generated method stub

        	return daySchedules.size();

        }

 

        @Override

        public Object getItem(int position) {

            // TODO Auto-generated method stub

            return daySchedules.get(position);

        }

 

        @Override

        public long getItemId(int position) {

            // TODO Auto-generated method stub

            return position;

        }

 

        @Override

        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = daySchedules.get(position).getView(convertView);
            Log.v("DayScheduleAdapter---position---" + position + "time:" + 
            		System.currentTimeMillis());
            return convertView;
        }
        
    }
//Gionee <pengwei><2013-04-16> modify for DayView begin

     
