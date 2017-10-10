package com.gokhanmoral.materialstweaks;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SyhValueChangedInterface, View.OnClickListener {

    public static ArrayList<SyhTab> syhTabList = new ArrayList<>();
    //==================== Syh UI Elements ================================
    private static String LOG_TAG = MainActivity.class.getName();
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Boolean testingWithNoKernelSupport = false;
    private Boolean kernelSupportOk = false;
    private Boolean userInterfaceConfigSuccess = false;
    private Boolean valueChanged = false;
    private ProgressDialog dialog = null;
    private String valuesChanged = "";
    private AlertDialog.Builder builder;
    private AlertDialog alert;

    private boolean getUserInterfaceConfigFromAssets() {
        Log.i(LOG_TAG, "Siyah script NOT found! But testing is enabled, so continue...");
        Log.i(LOG_TAG, "Getting config xml from apk...");
        Boolean isOk = false;
        try {
            InputStream is = getAssets().open("customconfig.xml");
            isOk = parseUIFromXml(is);
        } catch (IOException e) {
            Log.i(LOG_TAG, "No config xml inside apk...");
            //e.printStackTrace();
        }
        return isOk;
    }

    private boolean getUserInterfaceConfigFromScript() {
        Log.i(LOG_TAG, "Siyah script found.");
        Log.i(LOG_TAG, "Getting config xml via script...");
        Boolean isOk = false;
        String response = RootUtils.executeRootCommandInThread("/res/uci.sh config");
        if (response != null && !response.equals("")) {
            Log.i(LOG_TAG, "Config xml extracted from script!");
            ByteArrayInputStream is = new ByteArrayInputStream(response.getBytes());
            isOk = parseUIFromXml(is);
        }
        return isOk;
    }

    private boolean isKernelSupportOk() {
        Boolean isOk = false;
        Log.i(LOG_TAG, "Searching siyah script...");
        File file = new File("/res/uci.sh");
        if (file.exists()) {
            Log.i(LOG_TAG, "Kernel script(s) found.");
            if (file.canExecute()) {
                Log.i(LOG_TAG, "Kernel script(s) OK.");
                isOk = true;
            } else {
                Log.e(LOG_TAG, "Kernel script(s) NOT OK!");
            }
        } else {
            Log.e(LOG_TAG, "Kernel script(s) NOT found!");
        }
        return isOk;
    }

    public boolean parseUIFromXml(InputStream is) {
        Boolean isOk = true;
        syhTabList.clear();
        Log.i(LOG_TAG, "parseUIFromXml !");
        try {
            // get a new XmlPullParser object from Factory
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            // set input source
            parser.setInput(is, null);
            // get event type
            int eventType = parser.getEventType();

            SyhTab tab = null;
            SyhPane pane = null;
            SyhSpinner spinner = null;
            SyhSeekBar seekbar;
            SyhCheckBox checkbox;
            SyhButton button;

            // process tag while not reaching the end of document
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName;
                switch (eventType) {
                    // at start of document: START_DOCUMENT
                    case XmlPullParser.START_DOCUMENT:
                        //study = new Study();
                        break;

                    // at start of a tag: START_TAG
                    case XmlPullParser.START_TAG:
                        // get tag name
                        tagName = parser.getName();
                        // if <settingsTab>, get attribute: 'name'
                        if (tagName.equalsIgnoreCase("settingsTab")) {
                            //Log.w("parseUIFromXml", "settingsTab name = " + parser.getAttributeValue(null, "name"));
                            tab = new SyhTab(this, this);
                            tab.name = parser.getAttributeValue(null, "name");
                        }
                        // if <settingsPane>, get attribute: 'name' and 'description'
                        else if (tagName.equalsIgnoreCase("settingsPane")) {
                            //Log.w("parseUIFromXml", "settingsPane name = " + parser.getAttributeValue(null, "name"));
                            //Log.w("parseUIFromXml", "settingsPane description = " + parser.getAttributeValue(null, "description"));
                            pane = new SyhPane();
                            pane.description = parser.getAttributeValue(null, "description");
                            pane.name = parser.getAttributeValue(null, "name");
                        }
                        // if <checkbox>
                        else if (tagName.equalsIgnoreCase("checkbox")) {
                            //Log.w("parseUIFromXml", "checkbox name = " + parser.getAttributeValue(null, "name"));
                            //Log.w("parseUIFromXml", "checkbox description = " + parser.getAttributeValue(null, "description"));
                            //Log.w("parseUIFromXml", "checkbox action = " + parser.getAttributeValue(null, "action"));
                            //Log.w("parseUIFromXml", "checkbox label = " + parser.getAttributeValue(null, "label"));
                            if (pane != null) {
                                checkbox = new SyhCheckBox(this);
                                checkbox.name = parser.getAttributeValue(null, "name");
                                checkbox.description = parser.getAttributeValue(null, "description");
                                checkbox.action = parser.getAttributeValue(null, "action");
                                checkbox.label = parser.getAttributeValue(null, "label");
                                pane.controls.add(checkbox);
                            }
                        } else if (tagName.equalsIgnoreCase("spinner")) {
                            //Log.w("parseUIFromXml", "spinner name = " + parser.getAttributeValue(null, "name"));
                            //Log.w("parseUIFromXml", "spinner description = " + parser.getAttributeValue(null, "description"));
                            //Log.w("parseUIFromXml", "spinner action = " + parser.getAttributeValue(null, "action"));
                            if (pane != null) {
                                spinner = new SyhSpinner(this);
                                spinner.name = parser.getAttributeValue(null, "name");
                                spinner.description = parser.getAttributeValue(null, "description");
                                spinner.action = parser.getAttributeValue(null, "action");
                                pane.controls.add(spinner);
                            }
                        } else if (tagName.equalsIgnoreCase("spinnerItem")) {
                            //Log.w("parseUIFromXml", "spinnerItem name = " + parser.getAttributeValue(null, "name"));
                            //Log.w("parseUIFromXml", "spinnerItem value = " + parser.getAttributeValue(null, "value"));
                            if (spinner != null) {
                                spinner.addNameAndValue(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "value"));
                            }
                        } else if (tagName.equalsIgnoreCase("seekBar")) {
                            //Log.w("parseUIFromXml", "seekBar name = " + parser.getAttributeValue(null, "name"));
                            //Log.w("parseUIFromXml", "seekBar description = " + parser.getAttributeValue(null, "description"));
                            //Log.w("parseUIFromXml", "seekBar action = " + parser.getAttributeValue(null, "action"));
                            if (pane != null) {
                                seekbar = new SyhSeekBar(this);
                                seekbar.name = parser.getAttributeValue(null, "name");
                                seekbar.description = parser.getAttributeValue(null, "description");
                                seekbar.action = parser.getAttributeValue(null, "action");
                                seekbar.max = Integer.parseInt(parser.getAttributeValue(null, "max"));
                                seekbar.min = Integer.parseInt(parser.getAttributeValue(null, "min"));
                                seekbar.step = Integer.parseInt(parser.getAttributeValue(null, "step"));
                                seekbar.reversed = Boolean.parseBoolean(parser.getAttributeValue(null, "reversed"));
                                seekbar.unit = parser.getAttributeValue(null, "unit");
                                pane.controls.add(seekbar);
                            }
                        } else if (tagName.equalsIgnoreCase("button")) {
                            //Log.w("parseUIFromXml", "button name = " + parser.getAttributeValue(null, "name"));
                            //Log.w("parseUIFromXml", "button description = " + parser.getAttributeValue(null, "description"));
                            //Log.w("parseUIFromXml", "button action = " + parser.getAttributeValue(null, "action"));
                            //Log.w("parseUIFromXml", "button label = " + parser.getAttributeValue(null, "label"));
                            if (pane != null) {
                                button = new SyhButton(this);
                                button.name = parser.getAttributeValue(null, "name");
                                button.description = parser.getAttributeValue(null, "description");
                                button.action = parser.getAttributeValue(null, "action");
                                button.label = parser.getAttributeValue(null, "label");
                                pane.controls.add(button);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        // get tag name
                        tagName = parser.getName();
                        // if <settingsTab>, get attribute: 'name'
                        if (tagName.equalsIgnoreCase("settingsTab")) {
                            //Log.w("parseUIFromXml", "settingsTab name = " + parser.getAttributeValue(null, "name") + " ended!");
                            if (tab != null) {
                                syhTabList.add(tab);
                                tab = null;
                            }
                        }
                        // if <settingsPane>, get attribute: 'name' and 'description'
                        else if (tagName.equalsIgnoreCase("settingsPane")) {
                            //Log.w("parseUIFromXml", "settingsPane name = " + parser.getAttributeValue(null, "name") + " ended!");
                            if ((tab != null) && (pane != null)) {
                                tab.panes.add(pane);
                                pane = null;
                            }
                        }
                        break;
                }
                // jump to next event
                eventType = parser.next();
            }
            // exception stuffs
        } catch (XmlPullParserException | IOException e) {
            isOk = false;
            e.printStackTrace();
        }

        return isOk;
    }

    private void getScriptValuesWithoutUiChange() {
        String exitInActions = RootUtils.executeRootCommandInThread("grep exit /res/customconfig/actions/*");
        boolean optimized = false;
        if (exitInActions == null || exitInActions.length() == 0) {
            RootUtils.executeRootCommandInThread("source /res/customconfig/customconfig-helper");
            RootUtils.executeRootCommandInThread("read_defaults");
            RootUtils.executeRootCommandInThread("read_config");
            optimized = true;
        }
        for (int i = 0; i < syhTabList.size(); i++) {
            SyhTab tab = syhTabList.get(i);
            for (int j = 0; j < tab.panes.size(); j++) {
                SyhPane pane = tab.panes.get(j);
                for (int k = 0; k < pane.controls.size(); k++) {
                    SyhControl control = pane.controls.get(k);
                    control.getValueViaScript(optimized);
                }
            }
        }
    }

    private void clearUserSelections() //UI access
    {
        for (int i = 0; i < syhTabList.size(); i++) {
            SyhTab tab = syhTabList.get(i);
            for (int j = 0; j < tab.panes.size(); j++) {
                SyhPane pane = tab.panes.get(j);
                for (int k = 0; k < pane.controls.size(); k++) {
                    SyhControl control = pane.controls.get(k);
                    control.applyScriptValueToUserInterface();
                }
            }
        }
    }

    private boolean applyUserSelections() //no UI access
    {
        boolean isAllSelectionsOk = true;
        valuesChanged = "";
        for (int i = 0; i < syhTabList.size(); i++) {
            SyhTab tab = syhTabList.get(i);
            for (int j = 0; j < tab.panes.size(); j++) {
                SyhPane pane = tab.panes.get(j);
                for (int k = 0; k < pane.controls.size(); k++) {
                    SyhControl control = pane.controls.get(k);
                    if (control.isChanged() && control.canGetValueFromScript) //TODO: Move these checks into the SyhControl class
                    {
                        Log.i(LOG_TAG, "Changed control:" + control.name);
                        String res = control.setValueViaScript();
                        isAllSelectionsOk = isAllSelectionsOk && (res.length() > 0);
                        valuesChanged += control.name + ": " + res + "\r\n";
                    }
                }
            }
        }
        return isAllSelectionsOk;
    }

    //=====================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout acceptDecline = (LinearLayout) findViewById(R.id.AcceptDeclineLayout);
        acceptDecline.setVisibility(LinearLayout.GONE);
        final AppCompatButton accept = (AppCompatButton) acceptDecline.findViewById(R.id.AcceptButton);
        accept.setOnClickListener(this);
        final AppCompatButton decline = (AppCompatButton) acceptDecline.findViewById(R.id.DeclineButton);
        decline.setOnClickListener(this);

        AppCompatTextView startTextView = (AppCompatTextView) findViewById(R.id.textViewStart);
        kernelSupportOk = isKernelSupportOk();

        if (!kernelSupportOk && !testingWithNoKernelSupport) {
            startTextView.setText(R.string.startmenu_nokernelsupport);
        } else {
            if (RootUtils.canRunRootCommandsInThread()) {
                new LoadDynamicUI().execute();
                dialog = ProgressDialog.show(this, getResources().getText(R.string.app_name), "Loading! Please wait...", true);
            } else {
                startTextView.setText(R.string.startmenu_no_root);
                RootUtils.reset();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.item_reset:
                menuResetChanges();
                break;

            case R.id.item_extras:
                menuExtraDetails();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void valueChanged() {
        if (!valueChanged) {
            LinearLayout acceptDecline = (LinearLayout) findViewById(R.id.AcceptDeclineLayout);
            acceptDecline.setVisibility(LinearLayout.VISIBLE);
        }
        valueChanged = true;
    }

    @Override
    public void onClick(View v) {
        LinearLayout acceptDecline = (LinearLayout) findViewById(R.id.AcceptDeclineLayout);
        switch (v.getId()) {
            case R.id.AcceptButton:
                acceptDecline.setVisibility(LinearLayout.GONE);
                new ApplyChangedValues().execute();
                //TODO: Too fast!!
                //-- dialog = ProgressDialog.show(this, getResources().getText(R.string.app_name), "Applying changed values! Please wait...", true);
                valueChanged = false;
                break;
            case R.id.DeclineButton:
                acceptDecline.setVisibility(LinearLayout.GONE);
                clearUserSelections(); //UI change only, no scripts...
                valueChanged = false;
                break;
        }
    }

    private void menuResetChanges() {
        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Handle Ok
                RootUtils.executeRootCommandInThread("/res/uci.sh delete default");
                finish();
                RootUtils.executeRootCommandInThread("sleep 5");
                RootUtils.executeRootCommandInThread("reboot");
            }
        })
                .setNegativeButton("Cancel", null);

        alert = builder.create();
        alert.setTitle("Warning");
        alert.setMessage("System will reboot automatically in 5 secs after reset");
        alert.show();
        //alert.getWindow().getAttributes();
        //alert.getWindow().setLayout(400,600);
        //AppCompatTextView textView = (AppCompatTextView) alert.findViewById(android.R.id.message);
        //textView.setTextSize(14);
    }

    private void menuExtraDetails() {
        String s = "";
        s += "\nApp Version : " + "\n" + BuildConfig.VERSION_NAME + "\n";
        s += "\nKernel Version : " + "\n" + System.getProperty("os.version") + "\n";
        s += "\nROM API Level : " + "\n" + android.os.Build.VERSION.SDK_INT + "\n";
        s += "\nROM Codename : " + "\n" + android.os.Build.VERSION.CODENAME + "\n";
        s += "\nROM Release Version : " + "\n" + android.os.Build.VERSION.RELEASE + "\n";
        s += "\nHardware Serial : " + "\n" + android.os.Build.SERIAL + "\n";
        s += "\nRadio Version : " + "\n" + android.os.Build.getRadioVersion();

        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", null);
        alert = builder.create();
        alert.setMessage(getResources().getText(R.string.developer_info) + "\n" + s);
        alert.show();
        //alert.getWindow().getAttributes();
        //alert.getWindow().setLayout(400,600);
        AppCompatTextView tv = (AppCompatTextView) alert.findViewById(android.R.id.message);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        //tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(14);
    }

    private void setupTabLayout() {

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setVisibility(ViewPager.GONE);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        CustomTabLayout tabLayout = (CustomTabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabMode(CustomTabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorHeight(5);
        tabLayout.setupWithViewPager(mViewPager);

        //WARNING: This is the trick preventing the Off-screen Fragments from going out of memory!!!
        mViewPager.setOffscreenPageLimit(syhTabList.size());

        mViewPager.addOnPageChangeListener(new CustomTabLayout.TabLayoutOnPageChangeListener(tabLayout));
        onTabSelectedListener(mViewPager);
    }

    private CustomTabLayout.OnTabSelectedListener onTabSelectedListener(final ViewPager viewPager) {
        return new CustomTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(CustomTabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(CustomTabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(CustomTabLayout.Tab tab) {

            }
        };
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class SyhTabFragment extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";
        public Integer mTabIndex = -1;

        public SyhTabFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();
            mTabIndex = args.getInt(ARG_SECTION_NUMBER);
            //--Log.i(LOG_TAG, "onCreate savedInstanceState:" + savedInstanceState + " mTabIndex:" + mTabIndex);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //-- Log.i(LOG_TAG, "onCreateView savedInstanceState:" + savedInstanceState + " mTabIndex:" + mTabIndex);

            return createSyhTab(mTabIndex);
        }

        private ScrollView createSyhTab(Integer tabIndex) {
            ScrollView tabEnclosingLayout = new ScrollView(getActivity());
            tabEnclosingLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            SyhTab tab = syhTabList.get(tabIndex);

            View customView = tab.getCustomView();
            if (customView != null) {
                tabEnclosingLayout.addView(customView);
            } else {
                LinearLayout tabContentLayout = new LinearLayout(getActivity());
                tabContentLayout.setOrientation(LinearLayout.VERTICAL);
                tabContentLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                for (int j = 0; j < tab.panes.size(); j++) {
                    SyhPane pane = tab.panes.get(j);
                    pane.addPaneToUI(getActivity(), tabContentLayout);
                    for (int k = 0; k < pane.controls.size(); k++) {
                        SyhControl control = pane.controls.get(k);
                        control.create();
                        tabContentLayout.addView(control.view);
                    }
                }
                tabEnclosingLayout.addView(tabContentLayout);
            }
            return tabEnclosingLayout;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            //-- Log.i(LOG_TAG, "onDestroyView mTabIndex:" + mTabIndex);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            //-- Log.i(LOG_TAG, "onDetach mTabIndex:" + mTabIndex);
        }

        @Override
        public void onStop() {
            super.onStop();
            //-- Log.i(LOG_TAG, "onStop mTabIndex:" + mTabIndex);
        }

        @Override
        public void onPause() {
            super.onPause();
            //-- Log.i(LOG_TAG, "onPause mTabIndex:" + mTabIndex);
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new SyhTabFragment();
            fragment.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putInt(SyhTabFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
            //-- Log.i(LOG_TAG, "getItem getItem:" + i);
            return fragment;
        }

        @Override
        public int getCount() {
            return syhTabList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < syhTabList.size()) {
                return syhTabList.get(position).name;
            }
            return null;
        }
    }

    private class DialogCancelling extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            return null;
        }

        protected void onPostExecute(Boolean result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.cancel();
                dialog = null;
            }
        }
    }

    //final AsyncTask<Params, Progress, Result>
    private class LoadDynamicUI extends AsyncTask<Void, Void, Boolean> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Boolean doInBackground(Void... params) {
            if (testingWithNoKernelSupport) {
                userInterfaceConfigSuccess = getUserInterfaceConfigFromAssets();
                Log.e(LOG_TAG, "NumOfTabs:" + syhTabList.size());
            } else if (kernelSupportOk) {
                userInterfaceConfigSuccess = getUserInterfaceConfigFromScript();
            }

            if (userInterfaceConfigSuccess) {
                getScriptValuesWithoutUiChange();
            }
            return userInterfaceConfigSuccess;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Boolean result) {
            if (result) {
                AppCompatTextView startTextView = (AppCompatTextView) findViewById(R.id.textViewStart);
                startTextView.setVisibility(AppCompatTextView.GONE);

                setupTabLayout();
                mViewPager.setVisibility(ViewPager.VISIBLE);

                //initUI(mainLayout);
                //clearUserSelections(); //apply script values to UI
            }
            new DialogCancelling().execute();
        }

    }

    //final AsyncTask<Params, Progress, Result>
    private class ApplyChangedValues extends AsyncTask<Void, Void, Boolean> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Boolean doInBackground(Void... params) {
            Boolean isAllOk = false;
            if ((kernelSupportOk || testingWithNoKernelSupport) && userInterfaceConfigSuccess) {
                isAllOk = applyUserSelections(); //apply scripts only, no UI change...
            }
            return isAllOk;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Boolean result) {
            Toast toast;
            if (result) {
                toast = Toast.makeText(getApplicationContext(), valuesChanged, Toast.LENGTH_LONG);
                toast.show();
            } else {
                //TODO: Fix this!
                toast = Toast.makeText(getApplicationContext(), "Some selections failed to apply!", Toast.LENGTH_LONG);
                toast.show();
            }
            new DialogCancelling().execute();
        }
    }
}
