/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philicoe.wavereader.cardreader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Vibrator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.philicoe.wavereader.R;
import com.philicoe.wavereader.common.logger.Log;

import java.lang.ref.WeakReference;

/**
 * Generic UI for sample discovery.
 */
public class CardReaderFragment extends Fragment implements LoyaltyCardReader.AccountCallback {

    public static final String TAG = "CardReaderFragment";
    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    //public static final int mNfcAdapter_FLAG_READER_NFC_A = 0x00000001;
    //public static final int mNfcAdapter_FLAG_READER_SKIP_NDEF_CHECK = 0x00000080;
    public static int READER_FLAGS =  NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    //public static int READER_FLAGS =
           // mNfcAdapter_FLAG_READER_NFC_A | mNfcAdapter_FLAG_READER_SKIP_NDEF_CHECK;
    public LoyaltyCardReader mLoyaltyCardReader;
    private TextView mAccountField,mPanField,mAOSAField,tvMainDescription_banner;

    public static final java.lang.String VIBRATOR_SERVICE = "vibrator";
    public  static Vibrator vib;
    public boolean bDoGetDATA;
    public int ibDoGetDATA;
    public static String strAppversion;
    private TextView tvAppversion;
    public String finalAOSA;
    public String [] strSlits ;

    public String tmpAOSA ;
    public String tag9F68;
    public String tag9F6C ;

    public String tag9F79 ;
    public String tag9F78 ;
    public String tag9F77 ;

    public String tag9F58 ;
    public String tag9F59 ;
    public String tag9F54 ;
    public String tag9F5C ;
    public String tag9F6B ;
    public String strEXPDATE ;


    public CardReaderFragment(  ) {

    }



    /** Called when sample is created. Displays generic UI with welcome text. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.main_fragment, container, false);
        if (v != null) {
            mAccountField = (TextView) v.findViewById(R.id.card_account_field);
            //mPanField = (TextView)v.findViewById(R.id.card_name_field);
            mAOSAField = (TextView)v.findViewById(R.id.card_ballance_field);
            mAccountField.setText("Waiting...");

            mLoyaltyCardReader = new LoyaltyCardReader(this);



            // Disable Android Beam and register our card reader callback
             enableReaderMode();
        }
        try{

            strAppversion = getArguments().getString("APPVER");
            tvAppversion  = (TextView) v.findViewById(R.id.tv_AppVersion);
            tvMainDescription_banner  = (TextView) v.findViewById(R.id.tvMainDescription_banner);
            tvAppversion.setText(strAppversion);
            ((MainActivity) getActivity()).updateBannerString(getString(R.string.intro_message));

        }catch(Exception ie){

            ie.printStackTrace();
        }
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).updateBannerString(getString(R.string.intro_message));
        enableReaderMode();
    }

    private void enableReaderMode() {
        Log.i(TAG, "Enabling reader mode");
        Activity activity = getActivity();
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);

            if (nfc != null) {
                nfc.enableReaderMode(activity, mLoyaltyCardReader, READER_FLAGS, null);

            }
        //nfc.enableForegroundDispatch(this,);

    }

    private void disableReaderMode() {
        Log.i(TAG, "Disabling reader mode");
        Activity activity = getActivity();
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.disableReaderMode(activity);
            nfc.disableReaderMode(activity);
        }
    }

    @Override
    public void onAccountReceived(final String account) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.
        //Context context = getActivity().getApplicationContext();
        //vib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        //long[] duration = { 50, 100, 200, 300 };
        //vib.vibrate(duration, -1);
        ibDoGetDATA = getArguments().getInt("DOGETDATA");
        bDoGetDATA = true;
        // TODO: SPlit PAN,Name and Amoiunt Available here
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String PAN = account.substring(0,4) + " " +
                             account.substring(4,8) + " " +
                             account.substring(8,12) + " " +
                             account.substring(12, 16);

                mAccountField.setText(PAN);
               // mPanField.setText(account.substring(account.indexOf("\n"),account.lastIndexOf("\n")));
               // mPanField.setText("");
                // Example AOSA 000000019999
                strSlits = account.split("\n");

                tmpAOSA = strSlits[2];//account.substring(account.lastIndexOf("\n") - 12, account.length() - 1);
                finalAOSA = ReplaceLeadingZeros(tmpAOSA.substring(1, 11));
                tag9F68 = strSlits[3];
                tag9F6C = strSlits[4];

                tag9F79 = strSlits[5];
                tag9F78 = strSlits[6];
                tag9F77 = strSlits[7];

                tag9F58 = strSlits[8];
                tag9F59 = strSlits[9];
                tag9F54 = strSlits[10];
                tag9F5C = strSlits[11];
                tag9F6B = strSlits[12];
                finalAOSA =  tag9F79;

             //   strEXPDATE = strSlits

                String tmpTagDisplay_list =   "9F68: " + tag9F68 +
                                            "\n9F6C: " + tag9F6C +
                                            "\n9F79: " + tag9F79 +
                                            "\n9F78: " + tag9F78 +
                                            "\n9F77: " + tag9F77 +
                                            "\n9F58: " + tag9F58 +
                                            "\n9F59: " + tag9F59 +
                                            "\n9F54: " + tag9F54 +
                                            "\n9F5C: " + tag9F5C +
                                            "\n9F6B: " + tag9F6B +
                                            "\n9F17: " + strSlits[13] +
                                            "\n9F13: " + strSlits[15]+
                                            "\n9F36: " + strSlits[16];


                mAOSAField.setText(tmpTagDisplay_list);
                //ImageView imgViewCardImage = (ImageView)getView().findViewById(R.id.active_card_image);
                //String Uri = "@drawable/rmb_pvt_bank_cls_mobile_v1";  // where myresource.png is the file
                // extension removed from the String
               // String cardBIN = PAN.replace(" ","").substring(0,6);
               // Uri imgUri=null;
                //TextView textVwTitle = (TextView)getView().findViewById(R.id.card_title);
               // TextView textViewPAN = (TextView)getView().findViewById(R.id.card_account_field);
                //
                // imgViewCardImage.setImageURI(SwitchCardImage(cardBIN));
                Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "Impressed Metal.ttf");
                mAccountField.setTypeface(font);
                mAccountField.setTextColor(getResources().getColor(R.color.plain_white));
                mAccountField.setShadowLayer(1, 1, 1, getResources().getColor(R.color.plain_white));
                strEXPDATE = strSlits[14];
                storeSharedData();



            }


        });
    }

    private void storeSharedData(){
        Context context = getActivity().getBaseContext();
        SharedPreferences sharedData = context.getSharedPreferences(
                "CARD_FRONT_DATA", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedData.edit();
        editor.putString( "CARD_PAN", mAccountField.getText().toString());
        editor.putString( "EXP", strEXPDATE);
        editor.putString( "AOSA", finalAOSA);
        editor.putString( "AOSA", finalAOSA);
        editor.putString( "tag9F68",tag9F68);
        editor.putString( "tag9F6C",tag9F6C);

        editor.putString( "tag9F79",tag9F79);
        editor.putString( "tag9F78",tag9F78);
        editor.putString( "tag9F77",tag9F77);

        editor.putString( "tag9F58",tag9F58);
        editor.putString( "tag9F59",tag9F59);
        editor.putString( "tag9F54",tag9F54);
        editor.putString( "tag9F5C",tag9F5C);
        editor.putString( "tag9F6B",tag9F6B);
        editor.commit();
    }


    public String ReplaceLeadingZeros(String strIn){

        String tmp = "";
        int count=0;
        for(int i=0;i<strIn.length();i++){
            if(strIn.substring(i,i+1).contains("0")){
                    //Do nothing
                count++;
            }else{

                tmp =  strIn.substring(count,strIn.length());
                return tmp;
            }
        }
        return "";
    }
}
