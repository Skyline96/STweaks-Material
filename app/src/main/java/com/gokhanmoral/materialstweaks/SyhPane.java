package com.gokhanmoral.materialstweaks;

import android.app.Activity;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

class SyhPane {
    public String name;
    String description;
    List<SyhControl> controls = new ArrayList<>();

    void addPaneToUI(Activity activity, LinearLayout layout) {

        AppCompatTextView paneNameView;
        new AppCompatTextView(activity);
        paneNameView = (AppCompatTextView) LayoutInflater.from(activity).inflate(R.layout.template_panelname, layout, false);
        paneNameView.setText(this.name);
        layout.addView(paneNameView);

        if ((this.description != null) && (!this.description.equals(""))) {
            AppCompatTextView paneDescriptionView;
            new AppCompatTextView(activity);
            paneDescriptionView = (AppCompatTextView) LayoutInflater.from(activity).inflate(R.layout.template_paneldesc, layout, false);
            paneDescriptionView.setText(this.description);
            layout.addView(paneDescriptionView);
        }
    }

}
