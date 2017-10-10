package com.gokhanmoral.materialstweaks;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

class SyhTab {
    public String name;
    List<SyhPane> panes;
    private Context mContext;
    private Activity mActivity;

    SyhTab(Context context, Activity activity) {
        name = "";
        panes = new ArrayList<>();
        mContext = context;
        mActivity = activity;
    }

    View getCustomView() {
        return null;
    }
}
