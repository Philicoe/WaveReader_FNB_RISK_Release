package com.philicoe.wavereader.cardreader;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philicoe.wavereader.R;

/**
 * A fragment representing the back of the card.
 */
public class CardBackFragment extends Fragment   {

    public TextView mAccountField_back;
    public TextView mExpDateField_back;
    //public  TextView mAOSAField_back;
    public TextView mTapEvent_back;
    public  TextView mcardAccountName_back,tvMainDescription_banner;
    public CardReaderFragment mCardReaderFragment;
    public String [] strSlits_back ;
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
    public String businessCVMLimit,businessSingleTrnsAmt,businessThresholdAMT;
    public String finalAOSA;
    public CardBackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.main_fragment_flip, container, false);

        if (v != null) {


            mAccountField_back = (TextView) v.findViewById(R.id.card_account_field_back);
            mExpDateField_back = (TextView) v.findViewById(R.id.card_exp_field_back);
            mcardAccountName_back = (TextView)v.findViewById(R.id.card_account_name_back);
            //mAOSAField_back = (TextView) v.findViewById(R.id.card_ballance_field);
            mTapEvent_back = (TextView) v.findViewById(R.id.card_tap_event_back);
            tvMainDescription_banner  = (TextView) v.findViewById(R.id.tvMainDescription_banner);

            //mAccountField_back.setText("0000 0000 0000 0000");
            mExpDateField_back.setText("00/00");
            mcardAccountName_back.setText("Card name not provided by card..");
            //mTapEvent_back.setText("Event Description goes here");
            mTapEvent_back.setText("");

            ReadSharedData();

            //((MainActivity) getActivity()).updateBannerString(returnTapEvenRules());


            //mLoyaltyCardReader = new LoyaltyCardReader(this);

        }



        return v;// inflater.inflate(R.layout.main_fragment_flip, container, false);
    }

    private void ReadSharedData(){
        Context context = getActivity().getBaseContext();
        SharedPreferences sharedData = context.getSharedPreferences(
                "CARD_FRONT_DATA", Context.MODE_PRIVATE);
        finalAOSA = ReplaceLeadingZeros(sharedData.getString( "AOSA", "R0.00"));
        mAccountField_back.setText(sharedData.getString("CARD_PAN","4901 1500 1234 5678"));

       if(finalAOSA.length()!=0)
        mTapEvent_back.setText("R" + finalAOSA.substring(0,finalAOSA.length()-2) + "." + finalAOSA.substring(finalAOSA.length()-2,finalAOSA.length()) + " available to tap & go");

        tag9F68 = sharedData.getString( "tag9F68",tag9F68);//CAP
        tag9F6C=sharedData.getString( "tag9F6C",tag9F6C);//CTQ

        tag9F79=ReplaceLeadingZeros(sharedData.getString( "tag9F79",tag9F79));//VLP available funds
        tag9F78=ReplaceLeadingZeros(sharedData.getString( "tag9F78",tag9F78));// single transaction amount
        tag9F77=sharedData.getString( "tag9F77",tag9F77);// vlp funs limit
        businessThresholdAMT = ReplaceLeadingZeros(tag9F77.substring(0,tag9F77.length()-2) + "." + tag9F77.substring(tag9F77.length()-2,tag9F77.length()));

        tag9F58=sharedData.getString( "tag9F58",tag9F58);//LCOL
        tag9F59=sharedData.getString( "tag9F59",tag9F59);//UCOL
        tag9F54=ReplaceLeadingZeros(sharedData.getString( "tag9F54",tag9F54));///CTTA
        tag9F5C=ReplaceLeadingZeros(sharedData.getString( "tag9F5C",tag9F5C));//CTTAUL
        tag9F6B=ReplaceLeadingZeros(sharedData.getString( "tag9F6B",tag9F6B));

        if(tag9F6B.length()!=0)
            businessCVMLimit =  tag9F6B.substring(0,tag9F6B.length()-2)+ "." + tag9F6B.substring(tag9F6B.length()-2,tag9F6B.length());
        if(tag9F78.length()!=0)
            businessSingleTrnsAmt =  tag9F78.substring(0,tag9F78.length()-2)+ "." + tag9F78.substring(tag9F78.length()-2,tag9F78.length());
        mExpDateField_back.setText(sharedData.getString("EXP", "99/99").substring(2,4) + "/" + sharedData.getString("EXP", "99/99").substring(0,2));

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
    private String returnTapEvenRules(){
        //TODO: Build a description string on tap event outcome posibilities based on card values

        String strReturnString="";
        String curAOSA = "R" + finalAOSA.substring(0,finalAOSA.length()-2) + "." + finalAOSA.substring(finalAOSA.length()-2,finalAOSA.length());
        if(tag9F78.contentEquals(tag9F6B) || Integer.parseInt(tag9F78)>Integer.parseInt(tag9F6B)){

            if(Integer.parseInt(finalAOSA)<Integer.parseInt(tag9F6B)) {
                strReturnString = "* Domestic Tap above R" + businessCVMLimit + " will result in TAP + PIN\n" +
                        "* Domestic Tap below " + curAOSA + " will result in TAP + GO\n" +
                        "* Domestic Tap below R" + businessCVMLimit + " & above " + curAOSA  +
                        " You wil be asked to DIP card \nto reset the " + curAOSA + " to R" + businessThresholdAMT;
            }else{

                strReturnString = "* Domestic Tap above R" + businessCVMLimit + " will result in TAP + PIN\n" +
                        "* Domestic Tap below R" + businessCVMLimit + " will result in TAP + GO\n" +
                        "* Domestic Tap below R" + businessCVMLimit + " & above " + curAOSA + " will result\n" +
                        "You will be asked to DIP card to reset the " + curAOSA + " to R" + businessThresholdAMT;

            }

           // if(Integer.parseInt(finalAOSA)>Integer.parseInt(tag9F6B)) {

           // }





        }

        if (Integer.parseInt(tag9F78) < Integer.parseInt(tag9F6B)) {
                strReturnString = "* Domestic Tap below R" + businessSingleTrnsAmt + " & " + " will result in TAP + GO\n" +
                                  "* Domestic Tap above R" + businessSingleTrnsAmt + " will result in you being asked to DIP card" +
                                   "\nto reset the TAP & go AMT to R" + businessThresholdAMT +
                                   "* Domestic Tap above R" + businessCVMLimit + " will result in TAP + PIN\n";//TODO: Test & Confim if this is true


        }


        processCAP();
        //Append international check string
        //
        strReturnString = strReturnString + "\n* For International Tap for any amount will result in TAP + PIN if POS asks for customer validation" ;
        if(tag9F68.contentEquals("9AC0F000")){

        }

        return strReturnString;
    }

    public void processCAP(){
        //TAG 9F68
        /*  Byte 1
            1 = Low Value Check supported
            1 = Low value and CTTA check supported q VSDC
            1 = Count qVSDC online transactions
            1 = STREAMLINED qVSDC are Supported
            1 = Pin tries Exceeded check supported
            1 = Offline international transactions are allowed
            1 = Card Prefers Contact Chip - MUST READ COMMENTS
            1 = Return Available Offline Spending Amount (AOSA)
        */

        /* BYTE 2
            1 = Include country code in determining international transactions
            1 = International transactions are not allowed
            1 = Disable Offline Data Authentication (ODA) for Online Authorizations
            1 = Issuer Update Processing supported

            ** BITS 1 to 4 = RFU
         */

        /* BYTE 3
            1 = Online PIN supported for domestic transactions
            1 = Online PIN supported for international transactions
            1 = (Contact Chip) Offline PIN supported
            1 = Signature supported
         */

        //BYTE 4 = RFU

        //byte [] t9F68_1 = HexStringToByteArray(tag9F68.substring(0,2));

        /**********
          BYTE 1
         **********/
 //       int iB1 = Integer.parseInt(tag9F68.substring(0,2),16);

 //       if(iB1>>> 7==1){ //CAP byte bit 8

 //       }

       // if(tag9F68.substring(0,1).contentEquals(""))






    }


    /**
     * Utility class to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     */
    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}
