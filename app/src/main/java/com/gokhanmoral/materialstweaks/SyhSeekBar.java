package com.gokhanmoral.materialstweaks;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

final class SyhSeekBar extends SyhControl implements OnSeekBarChangeListener {

    private static String LOG_TAG = RootUtils.class.getName();
    String unit = "";
    int min = 0;
    int max = 0;
    int step = 1;
    boolean reversed = false;
    private SeekBar seekbar;
    private TextView seekBarValueText;

    SyhSeekBar(Activity activityIn) {
        super(activityIn);
    }


    //TODO: reverse adjustment needed!
    //TODO: secondary progress needed!
    //TODO: Move to XML

    @Override
    public void createInternal() {

        //Assumption: valueFromScript is set correctly.

        Integer val = 0;
        try {
            val = Integer.parseInt(valueFromScript);
        } catch (Exception e) {
            Log.e(LOG_TAG, "SyhSeekBar createInternal: valueFromScript cannot be converted!");
        }

        if (val < min) {
            val = min;
            valueFromScript = Integer.toString(min);
        } else if (val > max) {
            val = max;
            valueFromScript = Integer.toString(max);
        }
        valueFromUser = valueFromScript;

        int maxInSteps = (max - min) / step;

        //--Log.w(LOG_TAG, " max:" + Integer.toString(max) + " step:" + Integer.toString(step) + " maxInSteps:" + Integer.toString(maxInSteps));

        seekbar = new SeekBar(context);
        seekbar.setMax(maxInSteps);
        seekbar.setProgress((val - min) / step);
        seekbar.setOnSeekBarChangeListener(this); // set listener.

        //--seekbar.setSecondaryProgress(max/2);//TODO: fix it

        applyScriptValueToUserInterface();

        controlLayout.addView(seekbar);

        //TODO: Move this to xml
        seekBarValueText = new TextView(context);
        seekBarValueText.setText(valueFromUser + " " + unit);
        seekBarValueText.setGravity(Gravity.CENTER);
        controlLayout.addView(seekBarValueText);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {

        //-- Log.i(this.getClass().getName(), "min:" + min + " max:" + max + " seekMax:" + seekbar.getMax() + " progress:" + progress);
        int value = min + progress * step;
        valueFromUser = Integer.toString(value);
        seekBarValueText.setText(valueFromUser + " " + unit);
        //--seekBarValueText.setText(progress + " " + unit);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (isChanged()) {
            this.vci.valueChanged();
        }
    }

    @Override
    protected void applyScriptValueToUserInterface() {
        if (seekbar != null) {
            Integer valueHardwareInt = 0;
            try {
                valueHardwareInt = Integer.parseInt(valueFromScript);
            } catch (NumberFormatException ignored) {
            }
            Integer progress = (valueHardwareInt - min) / step;
            seekbar.setProgress(progress);
        }
        valueFromUser = valueFromScript;
    }

    @Override
    protected String getDefaultValue() {
        return Integer.toString(min);
    }
}