package com.gokhanmoral.materialstweaks;

import android.app.Activity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

class SyhCheckBox extends SyhControl implements OnClickListener {

    String label;
    private AppCompatCheckBox checkBox;

    SyhCheckBox(Activity activityIn) {
        super(activityIn);
    }

    @Override
    public void createInternal() {

        //Assumption: valueFromScript is set correctly.

        //create CheckBox from xml template
        checkBox = (AppCompatCheckBox) LayoutInflater.from(context).inflate(R.layout.template_checkbox, controlLayout, false);
        checkBox.setText(label);
        checkBox.setOnClickListener(this);

        //--checkBox.setChecked(convertFromScriptFormatToControlFormat(valueFromScript));
        applyScriptValueToUserInterface();

        controlLayout.addView(checkBox);
    }

    @Override
    public void onClick(View v) {
        //-- This not true >>>  this.valueInput = Boolean.toString(checkBox.isChecked());
        this.valueFromUser = convertFromControlFormatToScriptFormat(checkBox.isChecked());
        this.vci.valueChanged();
    }

    @Override
    protected void applyScriptValueToUserInterface() {
        //-- This not true >>> boolean hardware = Boolean.parseBoolean(this.valueHardware);

        if (checkBox != null) {
            boolean hardware = convertFromScriptFormatToControlFormat(valueFromScript);
            checkBox.setChecked(hardware);
        }
        valueFromUser = valueFromScript;
    }

    private Boolean convertFromScriptFormatToControlFormat(String input) {
        return input.equals("on");
    }

    private String convertFromControlFormatToScriptFormat(Boolean input) {
        return (input) ? ("on") : ("off");
    }

    @Override
    protected String getDefaultValue() {
        return "off";
    }

}
