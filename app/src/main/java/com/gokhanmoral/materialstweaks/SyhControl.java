package com.gokhanmoral.materialstweaks;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

abstract class SyhControl {
    public String name = "";
    public View view;
    String description = "";
    String action = "";
    String valueFromScript = "0";    //loaded from the kernel script (integer, float, "on"/"off"...)
    String valueFromUser = "0";        //user input to be applied to the kernel script (integer, float, "on"/"off"...)
    Boolean canGetValueFromScript = true;
    SyhValueChangedInterface vci; //interface to inform main activity about changed values
    Context context;
    LinearLayout controlLayout;
    private String syh_command = "/res/uci.sh ";

    SyhControl(Activity activityIn) {
        context = activityIn;
        vci = (SyhValueChangedInterface) activityIn;
    }

    boolean isChanged() {
        return (!valueFromUser.equals(valueFromScript));
    }

    // apply user selected value to the kernel script
    String setValueViaScript() {
        String command = syh_command + action + " " + valueFromUser;
        String response = RootUtils.executeRootCommandInThread(command);
        if (response == null) response = "";
        valueFromScript = valueFromUser;
        return response;
    }

    // get the value from kernel script - user interface NOT CHANGED!
    boolean getValueViaScript(boolean optimized) {
        boolean isOk = false;

        if (this.canGetValueFromScript) {
            String command;
            if (optimized) {
                command = "`echo " + action + "|awk '{print \". /res/customconfig/actions/\" $1,$1,$2,$3,$4,$5,$6,$7,$8}'`";
            } else {
                command = syh_command + action;
            }
            String response = RootUtils.executeRootCommandInThread(command);
            if (response != null) {
                if (!response.isEmpty()) {
                    valueFromScript = response.replaceAll("[\n\r]", "");
                    isOk = true;
                }
            }

            if (!isOk) {
                valueFromScript = this.getDefaultValue();
                if (valueFromScript == null) {
                    valueFromScript = "";
                }
            }

            Log.i("getValueViaScript " + this.getClass().getName() + "[" + this.name + "]:", "Value from script:" + valueFromScript);
        }

        return isOk;
    }

    void create() {
        //Assumptions:
        //1. valueFromScript is set correctly before creation.

/*		
 * TODO: Later concern!
		If we use fragments which can be put to stack then we have problems.
		Because of two conditions we are here:
		1.) Control is created for the first time
		2.) Fragment is paused and resuming...
		Question: Which value should be displayed in the user interface:
		          valueFromScript or valueFromUser?
*/

        valueFromUser = valueFromScript; //prevent value changed event!!!

        controlLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.template_controls, controlLayout, false);

        //Control name
        AppCompatTextView nameTextView = (AppCompatTextView) controlLayout.findViewById(R.id.control_name);
        nameTextView.setText(name);

        //Control description
        AppCompatTextView descriptionTextView = (AppCompatTextView) controlLayout.findViewById(R.id.control_desc);
        descriptionTextView.setText(description);

        createInternal();

        AppCompatTextView paneSeparatorBlank = new AppCompatTextView(context);
        paneSeparatorBlank.setHeight(10);
        controlLayout.addView(paneSeparatorBlank);

        //View paneSeparatorLine;
        //paneSeparatorLine = LayoutInflater.from(context).inflate(R.layout.template_control_separater, controlLayout, false);
        //controlLayout.addView(paneSeparatorLine);

        //TextView paneSeparatorBlankAfterLine = new TextView(context);
        //paneSeparatorBlankAfterLine.setHeight(10);
        //controlLayout.addView(paneSeparatorBlankAfterLine);

        view = controlLayout;
    }

    abstract protected void createInternal();    //sets the view

    abstract protected void applyScriptValueToUserInterface();    //clear user input, set it back to the script value

    abstract protected String getDefaultValue();

}
