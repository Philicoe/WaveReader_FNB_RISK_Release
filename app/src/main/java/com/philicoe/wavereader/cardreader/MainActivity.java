/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.philicoe.wavereader.cardreader;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.philicoe.wavereader.R;
import com.philicoe.wavereader.common.activities.SampleActivityBase;
import com.philicoe.wavereader.common.logger.Log;
import com.philicoe.wavereader.common.logger.LogFragment;
import com.philicoe.wavereader.common.logger.LogWrapper;
import com.philicoe.wavereader.common.logger.MessageOnlyLogFilter;
import android.content.pm.*;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase implements FragmentManager.OnBackStackChangedListener{

    public static final String TAG = "Visa_AOSA";
    public static TextView tvAppversion;
    public TextView  tvMainDescription_banner;
    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    public boolean mDoGetData=true;
    public boolean bToggleView=false;
    public static String strAppversion ;//= pInfo.versionName;
    public static int verCode ;//= pInfo.versionCode;
    public MenuItem logToggle ;//= menu.findItem(R.id.menu_toggle_log);
    public MenuItem menu_togggleView ;//= menu.findItem(R.id.menu_togggleView);
    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            strAppversion = pInfo.versionName;
            strAppversion =  "Current App Version:"+ strAppversion  + " ("+ String.valueOf(pInfo.versionCode) + ")";


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mDoGetData=true;
        bToggleView=false;

        setContentView(R.layout.activity_main);


        if (savedInstanceState == null) {
           // mDoGetData=false;
            RedoFragment();
           //
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.

        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }


        // Monitor back stack changes to ensure the action bar shows the appropriate
        // button (either "photo" or "info").
        getFragmentManager().addOnBackStackChangedListener(this);

        tvAppversion = (TextView) findViewById(R.id.tv_AppVersion);

        tvAppversion.setText(strAppversion);
        tvMainDescription_banner = (TextView) findViewById(R.id.tvMainDescription_banner);
        updateBannerString(getString(R.string.intro_message));


    }


    public void RedoFragment(){

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            CardReaderFragment fragment = new CardReaderFragment();
            Bundle args = new Bundle();
            args.putInt("DOGETDATA", (mDoGetData) ? 1 : 1);// 1 = true and 0=false
            args.putString("APPVER",strAppversion);
            fragment.setArguments(args);
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();

    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);

        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }
    @Override
    public void onPause(){
        super.onPause();
        //tvMainDescription_banner.setText(getString(R.string.intro_message));
    }
    @Override
    public void onResume(){
        super.onResume();
        //tvMainDescription_banner.setText(getString(R.string.intro_message));
    }

    public void updateBannerString(String strtoUpdate){

        tvMainDescription_banner.setText(strtoUpdate);
    }


    private void flipCard() {
       // MenuItem item  = (MenuItem) findViewById(R.id.menu_togggleView);
       // item.setTitle(!mShowingBack ? "Front" : "Back");
     //   MenuItem item  = (MenuItem) findViewById(R.id.menu_togggleView);
        if (mShowingBack) {
           // item.setTitle("Back");
            getFragmentManager().popBackStack();
            updateBannerString(getString(R.string.intro_message));
            return;
        }

        // Flip to the back.

        mShowingBack = true;
        //View v = (View) findViewById(R.id.sample_main_layout);
        Bundle args = new Bundle();
        //args.putInt("", (mDoGetData) ? 1 : 1);// 1 = true and 0=false
        args.putString("CARDDATA","R10.0");
      //  item.setTitle("Front");

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.

        getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing
                // rotations when switching to the back of the card, as well as animator
                // resources representing rotations when flipping back to the front (e.g. when
                // the system Back button is pressed).
                //.addSharedElement(v,"FrontView")
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a fragment
                // representing the next page (indicated by the just-incremented currentPage
                // variable).
                .replace(R.id.sample_content_fragment, new CardBackFragment())

                // Add this transaction to the back stack, allowing users to press Back
                // to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mDoGetData=false;
        bToggleView=false;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //mDoGetData=!mDoGetData;
       // MenuItem DoGetDataToggle = menu.findItem(R.id.menu_toggle_getData);
          logToggle = menu.findItem(R.id.menu_toggle_log);
          menu_togggleView = menu.findItem(R.id.menu_togggleView);
        //DoGetDataToggle.setVisible(findViewById(R.id.menu_toggle_getData) instanceof ViewAnimator);
      //  DoGetDataToggle.setTitle(mDoGetData ?R.string.menu_dont_getDataAPDU: R.string.menu_do_getDataAPDU  );


       //logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
       logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);
        menu_togggleView.setTitle(mShowingBack ? "Front" : "Back");

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                Toast.makeText(this,"Log view disabled",Toast.LENGTH_LONG).show();
                return true;

               /* mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
                */
            case R.id.menu_togggleView:
                bToggleView=!bToggleView;
                menu_togggleView.setTitle(mShowingBack ? "Front" : "Back");// This is to flip card view
               // if(mShowingBack){
               //     NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
               //     return true;
              //  }else{
                    flipCard();
                    return true;
              //  }

            case R.id.menu_refresh:
                RedoFragment();
                return true;
            case R.id.menu_exit:
                System.exit(0);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }
}


