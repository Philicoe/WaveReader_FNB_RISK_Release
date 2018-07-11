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

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.widget.Toast;

import com.philicoe.wavereader.common.logger.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Callback class, invoked when an NFC card is scanned while the device is running in reader mode.
 *
 * Reader mode can be invoked by calling NfcAdapter
 */
public class LoyaltyCardReader implements NfcAdapter.ReaderCallback {
    private static final String TAG = "VISA_CARD";
    private static final String TAG_nfcA = "NFCa";

    // AID for our loyalty card service.
    private static final String SAMPLE_LOYALTY_CARD_AID = "A0000000031010";
    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String SELECT_APDU_HEADER = "00A40400";
    //String Final = "00A40400" + straLen + strAID;
    // "OK" status word sent in response to SELECT AID command (0x9000)
    private static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};
    private static final byte[] SW1_SW2_OK = {(byte) 0x90, (byte) 0x00};
    public static String finalStringReturned = "";
    public byte[] result=null;
    public static boolean lastAPDUIsGood = false;
    // Weak reference to prevent retain loop. mAccountCallback is responsible for exiting
    // foreground mode before it becomes invalid (e.g. during onPause() or onStop()).
    private WeakReference<AccountCallback> mAccountCallback;

    public interface AccountCallback {
        public void onAccountReceived(String account);
    }

    public LoyaltyCardReader(AccountCallback accountCallback) {
        mAccountCallback = new WeakReference<AccountCallback>(accountCallback);
    }

    /**
     * Callback when a new tag is discovered by the system.
     *
     * <p>Communication with the card should take place here.
     *
     * @param tag Discovered tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
       // onTagDiscoverted_ORG(tag);
        ///onNfcATagDiscovered(tag);
       onTagDiscovered_GENERIC(tag);


    }


    public void onTagDiscoverted_ORG(Tag tag){

        Log.i(TAG, "New tag discovered");
        // Android's Host-based Card Emulation (HCE) feature implements the ISO-DEP (ISO 14443-4)
        // protocol.
        //
        // In order to communicate with a device using HCE, the discovered tag should be processed
        // using the IsoDep class.
        IsoDep isoDep = IsoDep.get(tag);

        if (isoDep != null) {
            try {
                // Connect to the remote NFC device
                isoDep.connect();
                // Build SELECT AID command for our loyalty card service.
                // This command tells the remote device which service we wish to communicate with.
                Log.i(TAG, "Requesting remote AID: " + SAMPLE_LOYALTY_CARD_AID);
                byte[] command = BuildSelectApdu(SAMPLE_LOYALTY_CARD_AID);
                // Send command to remote device
                Log.i(TAG, "Sending: " + ByteArrayToHexString(command));

                //TODO: This next command is the command which actaully transmits command out and received response
                result=null;
                result = isoDep.transceive(command);
                // If AID is successfully selected, 0x9000 is returned as the status word (last 2
                // bytes of the result) by convention. Everything before the status word is
                // optional payload, which is used here to hold the account number.
                int resultLength = result.length;
                byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
                byte[] payload = Arrays.copyOf(result, resultLength-2);
                if (Arrays.equals(SELECT_OK_SW, statusWord)) {
                    // The remote NFC device will immediately respond with its stored account number
                    String accountNumber = new String(payload, "UTF-8");
                    Log.i(TAG, "Received: " + accountNumber);
                    // Inform CardReaderFragment of received account number
                    mAccountCallback.get().onAccountReceived(accountNumber);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error communicating with card: " + e.toString());
            }
        }


    }
    public String mHexToAscii(String ThehexSstr) {
        String hex = ThehexSstr.trim();
        String output ="";
        for (int i = 0; i < hex.length()-1; i+=2) {
            String str = hex.substring(i, i+2);
            output = output + ((char)Integer.parseInt(str, 16));
        }
        return output.trim();
    }

    public String MFCAuth(){
        //********************************************************
        //Function Name: authenticateButtonClick
        //Input(Parameter) : sender, e
        //OutPutParameter:-------
        //Description:Authenticate the card using key
        //APDU Description: ClassByte bcla = 0xFF,
        //                  Instruction Byte bins=0x88 / 0x86 ,
        //                  Parameter P1=Address MSB / 0x00 ,
        //                  Parameter P2=Address LSB / 0x00
        //                  P3 = key type / 0x05 and Data Bytes = keynumber /
        //                                (Version,Address MSB,Address LSB,Key Type,Key Number)


        return "FF860000050100006100F4";
    }

    public String MFCLoadKey(){


        return "FF82200006089B0708EC62";
    }
    public void onNfcATagDiscovered(Tag tag){
        Log.i(TAG, "nNFCa tag discovered");
               NfcA mNfca  = NfcA.get(tag);

        try {
            mNfca.setTimeout(5000);
            mNfca.connect();
            if(mNfca.isConnected()){
                Log.e(TAG_nfcA,"NFCa Connected");
                Log.e(TAG_nfcA,"Connection bytes returned: " + ByteArrayToHexString(mNfca.getAtqa()));
                Log.e(TAG_nfcA,"Tag ID:" +ByteArrayToHexString(mNfca.getTag().getId()));

                //if(!mNfca.isConnected()){
                    //mNfca.connect();
                    Log.e(TAG_nfcA,"NFCa load Key request");
                    boolean ia =  NFCaSendApdu(MFCLoadKey(),mNfca);
                //}

               // if(!mNfca.isConnected()) {
                 //   mNfca.connect();
                    Log.e(TAG_nfcA,"NFCa Auth request ");
                    boolean iB = NFCaSendApdu(MFCAuth(), mNfca);
                //}
                Log.e(TAG_nfcA,"NFCa tag Discovred complete");
            }else{

                Log.e(TAG_nfcA,"NFCa Connection Failed");
            }


        }catch (IOException e){

            Log.e(TAG_nfcA,e.getMessage());
        }



        String issDepTechList[] = tag.getTechList();
        for(int i=0;i<issDepTechList.length;i++){

            Log.e(TAG,issDepTechList[i]);

        }



    }
    public void onTagDiscovered_GENERIC(Tag tag){


        Log.i(TAG, "New tag IsoDep discovered");
        // Android's Host-based Card Emulation (HCE) feature implements the ISO-DEP (ISO 14443-4)
        // protocol.
        //
        // In order to communicate with a device using HCE, the discovered tag should be processed
        // using the IsoDep class.
        IsoDep isoDep = IsoDep.get(tag);

        if (isoDep != null) {
            try {
                // Connect to the remote NFC device
                isoDep.connect();
               //TODO: To do the APDU send part and return SW1SW2
               //TODO: First Do the Application Selection

                if(SendApdu(MFCLoadKey(),isoDep)){
                    SendApdu(MFCAuth(),isoDep);
                }
                lastAPDUIsGood=false;
                if(SendApdu("00A4040007A0000000031010",isoDep))
                {
                    //TODO: if application selection is ok then send a Generic GPO command
                    //80 A8 00 00 15 83 13 36000000 000000000001 0710 150302 01020304
                    //80A8000015831336000000000000000000071015040701020304
                    //String Final = "80A80000" + "158313" + strTTQ + "000000000000" + posCountryCode + TransActionDate + "01020304";

                               //﻿80A8000015831336000000000000000000071000000001020304
                               //80A8000015831336000000000000000000071015040701020304

                    if(SendApdu("80A8000015831336000000000000000000071015040701020304",isoDep)){
                        //TODO: if GPO was successfull then Display Card number plus AOSA (Avaialble Offlone Spending Amount)

                        // Inform CardReaderFragment of received account number

                        //TODO: Add, AOSA acc name and pan to values to send back to display
                        //Get TAG 57 from GPO Resp, this contains PAN Number
                        String tag57 = getTagValue("57",finalStringReturned).substring(0, 16);
                        String strEXPDate=finalStringReturned.substring(finalStringReturned.indexOf("D")+1,finalStringReturned.indexOf("D")+5);
                        //Get TAG 5F20 from GPO Resp, this contains Card Name
                        String tag5F20 = mHexToAscii(getTagValue("5F20", finalStringReturned));
                       //Get TAG 9F10 from GPO Resp, this contains AOSA
                        String tag9F10 =  getTagValue("9F10",finalStringReturned).substring(18, 29);
                        String tag9F68="";
                        String tag9F6C= getTagValue("9F6C",finalStringReturned);
                        String tag9F79="";
                        String tag9F78="";
                        String tag9F77="";
                        String tag9F58="";
                        String tag9F59="";
                        String tag9F54="";
                        String tag9F5C="";
                        String tag9F6B="";
                        String tag9F17="";
                        String tag9F13="";
                        String tag9F36= "";// getTagValue("9F36",finalStringReturned);
                        //﻿80CA9F1300 - LOATC
                        tag9F36 = getTagValue("9F36",finalStringReturned);


                        if(SendApdu("80CA9F6807",isoDep)){
                             tag9F68 = getDataTagValue("9F68", finalStringReturned);
                        }

                        if(tag9F6C.length()<1) {
                            if (SendApdu("80CA9F6C05", isoDep)) {
                                tag9F6C = getDataTagValue("9F6C", finalStringReturned);
                            }
                        }

                        if(SendApdu("80CA9F7909",isoDep)){
                            tag9F79 = getDataTagValue("9F79", finalStringReturned);
                        }

                        if(SendApdu("80CA9F7809",isoDep)){
                            tag9F78 = getDataTagValue("9F78", finalStringReturned);
                        }

                        if(SendApdu("80CA9F7709",isoDep)){
                            tag9F77 = getDataTagValue("9F77", finalStringReturned);
                        }

                        if(SendApdu("80CA9F5804",isoDep)){
                            tag9F58 = getDataTagValue("9F58", finalStringReturned);
                        }

                        if(SendApdu("80CA9F5904",isoDep)){
                            tag9F59 = getDataTagValue("9F59", finalStringReturned);
                        }

                        if(SendApdu("80CA9F5409",isoDep)){
                            tag9F54 = getDataTagValue("9F54", finalStringReturned);
                        }

                        if(SendApdu("80CA9F5C09",isoDep)){
                            tag9F5C = getDataTagValue("9F5C", finalStringReturned);
                        }

                        if(SendApdu("80CA9F6B09",isoDep)){
                            tag9F6B = getDataTagValue("9F6B", finalStringReturned);
                        }

                        if(SendApdu("80CA9F6B09",isoDep)){
                            tag9F6B = getDataTagValue("9F6B", finalStringReturned);
                        }

                        if(SendApdu("80CA9F1704",isoDep)){
                            tag9F17 = getDataTagValue("9F17", finalStringReturned);
                        }

                        if(SendApdu("00B2020C4F",isoDep)){
                             tag5F20 = getDataTagValue("5F20", finalStringReturned);
                        }

                        if(SendApdu("80CA9F1300",isoDep)){
                            tag9F13 = getDataTagValue("9F13", finalStringReturned);
                        }




                        String rsp = tag57 + "\n" +
                                tag5F20 + "\n" +
                                tag9F10 + "\n" +
                                tag9F68 +"\n" +
                                tag9F6C + "\n"+
                                tag9F79 + "\n"+
                                tag9F78 + "\n"+
                                tag9F77 + "\n"+
                                tag9F58 + "\n"+
                                tag9F59 + "\n"+
                                tag9F54 + "\n"+
                                tag9F5C + "\n"+
                                tag9F6B + "\n"+
                                tag9F17 + "\n" +
                                strEXPDate + "\n" +
                                tag9F13 + "\n" +
                                tag9F36;


                        //String rsp = getAccBalFromGPO_Resp(finalStringReturned);
                        mAccountCallback.get().onAccountReceived(rsp);


                    }



                }else
                {

                    //Toast.makeText(,"Card Not Active or command failed",Toast.LENGTH_LONG);
                }







            } catch (IOException e) {
                Log.e(TAG, "Error communicating with card: " + e.toString());
            }
        }


    }


    public String getAccBalFromGPO_Resp(String strGPOResp){

            String tmp = strGPOResp;
            int i9F10Pos = strGPOResp.indexOf("9F10")+ 3;

             int iGPOResp_length = tmp.length();
        String str9F10len = "";
        str9F10len = strGPOResp.substring(i9F10Pos+1,i9F10Pos+3);

        int i9F10Len =  Integer.parseInt(str9F10len.trim(), 16);
        String str9F10Content = tmp.substring((i9F10Pos+5),(i9F10Pos+5) + (i9F10Len*2));

            return str9F10Content.substring(16, 26);
        /// This code is from C# Vis_POS project
        /*        istrStart = APDU_Resp.IndexOf("9F10");
                             tmp = APDU_Resp.Substring((istrStart + 4), 2);
                             iLen = int.Parse(tmp, System.Globalization.NumberStyles.HexNumber); ;
                             istrEnd = iLen;  //Convert.ToInt32(tmp);
                             tag_9F10 = APDU_Resp.Substring((istrStart+6),istrEnd*2);
                             tmpLog="";
                             tmpLog+= APDU_Resp.Substring(47, 15) + ",";
                             tmpName = APDU_Resp.Substring(APDU_Resp.IndexOf("5F20") + 6, 52);
                             tmpName = GV.ConvertHex(tmpName);
                             //byte[] cName = null; // GV.str2Bcd(APDUcmd);
                             /*for (int i = 0; i == 52; i++)
                             {


                                 cName[i] = APDU_Resp.Substring(i, 2);
                                MessageBox.Show(tag_9F10.Substring(18,10));
                             }*/


    }

    public String getTagValue(String TagToFind,String dataToSearch){

        String tmp = dataToSearch;
        int iTagPos = dataToSearch.indexOf(TagToFind);
        String strTagContent="";
        int iGPOResp_length = tmp.length();
        String strTagTLVlen = "";
        int iTLVLenDecimal=0;
        int iTLVDataEndPos=0;
        int iTLVDataStartPos=0;
        int iTLVTagNameLength=0;
        String tagname="";
        int iTLVlenBytestartPos ;
        int iTLVlenByteEndPos ;

        tagname="";
        tagname = TagToFind;
        iTLVTagNameLength = tagname.length();

        iTLVlenBytestartPos=0;
         iTLVlenByteEndPos=0;
        iTLVlenBytestartPos = iTagPos + iTLVTagNameLength;
        iTLVlenByteEndPos =iTagPos + iTLVTagNameLength + 2;
        strTagTLVlen = dataToSearch.substring(iTLVlenBytestartPos , iTLVlenByteEndPos);
        iTLVLenDecimal =  Integer.parseInt(strTagTLVlen.trim(), 16 );

        iTLVLenDecimal = iTLVLenDecimal *2;
        iTLVDataStartPos = iTagPos + iTLVTagNameLength + 2 ;
        iTLVDataEndPos =  iTLVDataEndPos + iTLVDataStartPos;
        iTLVDataEndPos =  iTLVDataEndPos + iTLVLenDecimal ;//* 2;


        strTagContent = tmp.substring(iTLVDataStartPos ,iTLVDataEndPos );


        return strTagContent;
    }

    public String getDataTagValue(String TagToFind,String dataToSearch){

        String tmp = dataToSearch;
        int iTagPos = dataToSearch.indexOf(TagToFind)+ 3;
        String strTagContent="";
        int iGPOResp_length = tmp.length();
        String strTaglen = "";
        int iTagLenDecimal=0;
        if (TagToFind.length()==2) {
            strTaglen = dataToSearch.substring(iTagPos - 1, iTagPos+1);
            iTagLenDecimal =  Integer.parseInt(strTaglen.trim(), 16 );
            strTagContent = tmp.substring((iTagPos+1));//, iTagPos+ (iTagLenDecimal*2));
        }else
        {
            strTaglen = dataToSearch.substring(iTagPos + 1, iTagPos + (3));
            iTagLenDecimal =  Integer.parseInt(strTaglen.trim(), 16 );
            strTagContent = tmp.substring((iTagPos+3));//, iTagPos+ (iTagLenDecimal*2));

        }

        return strTagContent;
    }
     public boolean SendApdu(String APDU_to_send,IsoDep isoDep){

         Log.i(TAG, " IsoDep APDU_TO_SEND_START:" + APDU_to_send);

             try {

                 // This command tells the remote device which service we wish to communicate with.
                 Log.i(TAG, "Requesting GENERIC IsoDep APDU: " + APDU_to_send);
                 byte[] command = BuildGenericApdu(APDU_to_send);
                 // Send command to remote device
                 Log.i(TAG, "Sending IsoDep tranceive: " + ByteArrayToHexString(command));

                 //TODO: This next command is the command which actaully transmits command out and received response
                 result=null;
                 result = isoDep.transceive(command);
                 // If APDU successfully sent , and 0x9000 is returned as the status word
                 //Then return back to calling method
                 int resultLength = result.length;
                 byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
                 byte[] payload = Arrays.copyOf(result, resultLength-2);
                 if (Arrays.equals(SW1_SW2_OK, statusWord)) {
                     finalStringReturned = ByteArrayToHexString(payload);
                     Log.i(TAG, "IsoDep_APDU_STR_BACK: " + finalStringReturned);
                     lastAPDUIsGood=true;
                         return true;
                 }
                 else{
                     finalStringReturned = ByteArrayToHexString(payload);
                     Log.i(TAG, "IsoDep_APDU_STR_BACK ERR: " + finalStringReturned);
                     lastAPDUIsGood=false;
                     return false;

                 }
             } catch (IOException e) {
                 Log.e(TAG, "Error communicating with card_IsoDep: " + e.toString());
                 lastAPDUIsGood=false;
             }


        return false;
    }


    public boolean NFCaSendApdu(String APDU_to_send,NfcA mNFCa){

        Log.i(TAG_nfcA, "NFCa_APDU_TO_SEND_START:" + APDU_to_send);

        try {

            // This command tells the remote device which service we wish to communicate with.
            Log.i(TAG_nfcA, "Requesting NFCa APDU: " + APDU_to_send);
            byte[] command = BuildGenericApdu(APDU_to_send);
            // Send command to remote device
            Log.i(TAG_nfcA, "Calling NFCa tranceive: " + ByteArrayToHexString(command));

            //TODO: This next command is the command which actaully transmits command out and received response
            result=null;
            result = mNFCa.transceive(command);
            // If APDU successfully sent , and 0x9000 is returned as the status word
            //Then return back to calling method
            int resultLength = result.length;
            byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
            byte[] payload = Arrays.copyOf(result, resultLength-2);
            if (Arrays.equals(SW1_SW2_OK, statusWord)) {
                finalStringReturned = ByteArrayToHexString(payload);
                Log.i(TAG_nfcA, "NFCa_STR_BACK: " + finalStringReturned);

                return true;
            }
        } catch (IOException e) {
            Log.e(TAG_nfcA, "Error communicating with NFCa card: " + e.toString());
        }


        return false;
    }
    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]

        //String Final = "00A40400" + straLen + strAID;
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
    }

    public static byte[] BuildGenericApdu(String inAPDU) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        //return HexStringToByteArray(String.format("%02X",inAPDU));
        return HexStringToByteArray(inAPDU);
    }

    /**
     * Utility class to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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
