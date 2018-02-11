package com.mediatek.calendar.selectevent;

import android.view.LayoutInflater;
import android.view.View;

import com.android.calendar.R;
import com.gionee.calendar.agenda.GNAgendaFragment;
import com.gionee.calendar.agenda.GNAgendaListView;

import com.mediatek.calendar.LogUtil;

public class EventSelectionFragment extends GNAgendaFragment {

    private static final String TAG = "EventSelectionFragment";

    public EventSelectionFragment() {
        this(0);
    }

    /**
     * M: constructor
     * @param timeMillis time millis to launch the Fragment
     */
    public EventSelectionFragment(long timeMillis) {
        super(timeMillis, false);
        LogUtil.v(TAG, "EventSelectionFragment created");
    }
    //Gionee <jiating><2013-05-21> modify for CR00000000  new AgendaFragment begin

    @Override
    protected View extInflateFragmentView(LayoutInflater inflater) {
        LogUtil.v(TAG, "mtk_event_selection_fagment inflated");
//        return inflater.inflate(R.layout.mtk_event_selection_fragment, null);
        return inflater.inflate(R.layout.gn_agenda_select_fragment, null);
    }

    @Override
    protected GNAgendaListView extFindListView(View v) {
        LogUtil.v(TAG, "found EventsListView");
//        return (GNAgendaListView)v.findViewById(R.id.mtk_events_list);
        return (GNAgendaListView)v.findViewById(R.id.agenda_select_events_list);
    }
    
    //Gionee <jiating><2013-05-21> modify for CR00000000  new AgendaFragment end

}
