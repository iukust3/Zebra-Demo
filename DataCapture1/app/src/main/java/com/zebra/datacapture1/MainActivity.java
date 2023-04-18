// **********************************************************************************************
// *                                                                                            *
// *    This application is intended for demonstration purposes only. It is provided as-is      *
// *    without guarantee or warranty and may be modified to suit individual needs.             *
// *                                                                                            *
// **********************************************************************************************

package com.zebra.datacapture1;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // DataWedge Sample supporting DataWedge APIs up to DW 7.0

    private static final String EXTRA_PROFILENAME = "Scanner Updated";

    // DataWedge Extras
    private static final String EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
    private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    private static final String EXTRA_KEY_APPLICATION_NAME = "com.symbol.datawedge.api.APPLICATION_NAME";
    private static final String EXTRA_KEY_NOTIFICATION_TYPE = "com.symbol.datawedge.api.NOTIFICATION_TYPE";
    private static final String EXTRA_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";
    private static final String EXTRA_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION";
    private static final String EXTRA_REGISTER_NOTIFICATION = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_UNREGISTER_NOTIFICATION = "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION";
    private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";

    private static final String EXTRA_RESULT_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private static final String EXTRA_KEY_VALUE_SCANNER_STATUS = "SCANNER_STATUS";
    private static final String EXTRA_KEY_VALUE_PROFILE_SWITCH = "PROFILE_SWITCH";
    private static final String EXTRA_KEY_VALUE_CONFIGURATION_UPDATE = "CONFIGURATION_UPDATE";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_STATUS = "STATUS";
    private static final String EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME = "PROFILE_NAME";
    private static final String EXTRA_SEND_RESULT = "SEND_RESULT";

    private static final String EXTRA_EMPTY = "";

    private static final String EXTRA_RESULT_GET_VERSION_INFO = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO";
    private static final String EXTRA_RESULT = "RESULT";
    private static final String EXTRA_RESULT_INFO = "RESULT_INFO";
    private static final String EXTRA_COMMAND = "COMMAND";

    // DataWedge Actions
    private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
    private static final String ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
    private static final String ACTION_RESULT = "com.symbol.datawedge.api.RESULT_ACTION";

    // private variables
    private Boolean bRequestSendResult = false;
    final String LOG_TAG = "DataCapture1";
    private FirebaseAnalytics mFirebaseAnalytics;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static final String INTENT_OUTPUT_ACTION = "com.symbol.genericdata.INTENT_OUTPUT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Check selected decoders
        // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        final Button btnSetDecoders = (Button) findViewById(R.id.btnSetDecoders);
        btnSetDecoders.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                final CheckBox checkCode128 = (CheckBox) findViewById(R.id.chkCode128);
                String Code128Value = setDecoder(checkCode128);

                final CheckBox checkCode39 = (CheckBox) findViewById(R.id.chkCode39);
                String Code39Value = setDecoder(checkCode39);

                final CheckBox checkEAN13 = (CheckBox) findViewById(R.id.chkEAN13);
                String EAN13Value = setDecoder(checkEAN13);

                final CheckBox checkUPCA = (CheckBox) findViewById(R.id.chkUPCA);
                String UPCAValue = setDecoder(checkUPCA);

                // Main bundle properties
                Bundle profileConfig = new Bundle();
                profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME);
                profileConfig.putString("PROFILE_ENABLED", "true");
                profileConfig.putString("CONFIG_MODE", "UPDATE");  // Update specified settings in profile

                // PLUGIN_CONFIG bundle properties
                Bundle barcodeConfig = new Bundle();
                barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
                barcodeConfig.putString("RESET_CONFIG", "true");

                // PARAM_LIST bundle properties
                Bundle barcodeProps = new Bundle();
                barcodeProps.putString("scanner_selection", "auto");
                barcodeProps.putString("scanner_input_enabled", "true");
                barcodeProps.putString("decoder_code128", Code128Value);
                barcodeProps.putString("decoder_code39", Code39Value);
                barcodeProps.putString("decoder_ean13", EAN13Value);
                barcodeProps.putString("decoder_upca", UPCAValue);

                // Bundle "barcodeProps" within bundle "barcodeConfig"
                barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
                // Place "barcodeConfig" bundle within main "profileConfig" bundle
                profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

                // Create APP_LIST bundle to associate app with profile
                Bundle appConfig = new Bundle();
                appConfig.putString("PACKAGE_NAME", getPackageName());
                appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
                profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
                sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
                Toast.makeText(getApplicationContext(), "In profile " + EXTRA_PROFILENAME + " the selected decoders are being set: \nCode128=" + Code128Value + "\nCode39="
                        + Code39Value + "\nEAN13=" + EAN13Value + "\nUPCA=" + UPCAValue, Toast.LENGTH_LONG).show();

            }
        });

        // Register for status change notification
        // Use REGISTER_FOR_NOTIFICATION: http://techdocs.zebra.com/datawedge/latest/guide/api/registerfornotification/
        Bundle b = new Bundle();
        b.putString(EXTRA_KEY_APPLICATION_NAME, getPackageName());
        b.putString(EXTRA_KEY_NOTIFICATION_TYPE, "SCANNER_STATUS");     // register for changes in scanner status
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, b);

        registerReceivers();

        // Get DataWedge version
        // Use GET_VERSION_INFO: http://techdocs.zebra.com/datawedge/latest/guide/api/getversioninfo/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_GET_VERSION_INFO, EXTRA_EMPTY);    // must be called after registering BroadcastReceiver
    setConfig();
    }

    // Create profile from UI onClick() event
    public void CreateProfile(View view) {

        if (true)
            return;
        Map<String, Object> user = new HashMap<>();
        user.put("profile", "Create Profile");


        // Add a new document with a generated ID
        db.collection("Create Profile")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                    }
                });
        String profileName = EXTRA_PROFILENAME;

        // Send DataWedge intent with extra to create profile
        // Use CREATE_PROFILE: http://techdocs.zebra.com/datawedge/latest/guide/api/createprofile/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, profileName);

        // Configure created profile to apply to this app
        Bundle profileConfig = new Bundle();
        profileConfig.putString("PROFILE_NAME", EXTRA_PROFILENAME);
        profileConfig.putString("PROFILE_ENABLED", "true");
        profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");  // Create profile if it does not exist

        // Configure barcode input plugin
        Bundle barcodeConfig = new Bundle();
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
        barcodeConfig.putString("RESET_CONFIG", "true"); //  This is the default
        Bundle barcodeProps = new Bundle();
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);

        // Associate profile with this app
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", getPackageName());
        appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
        profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
        profileConfig.remove("PLUGIN_CONFIG");
        Bundle bParamsToken = new Bundle();

        bParamsToken.putString("send_tokens_option", "BARCODES_TOKENS"); // Supported Values: DISABLED, TOKENS, BARCODES_TOKENS
        bParamsToken.putString("token_separator", "LF"); //Supported Values:None, TAB, CR, LF, NONE
        bParamsToken.putString("multibarcode_separator", "LF");
        Bundle bConfigToken = new Bundle();
        bConfigToken.putBundle("PARAM_LIST", bParamsToken);
        // Apply configs
        // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);

        // Configure intent output for captured data to be sent to this app
        Bundle intentConfig = new Bundle();
        intentConfig.putString("PLUGIN_NAME", "INTENT");
        intentConfig.putString("RESET_CONFIG", "true");
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();

        bundlePluginConfig.add(bConfigToken);
        Bundle intentProps = new Bundle();
        intentProps.putString("intent_output_enabled", "true");
        intentProps.putString("intent_action", "com.zebra.datacapture1.ACTION");
        intentProps.putString("intent_delivery", "2");
        intentConfig.putBundle("PARAM_LIST", intentProps);
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);

        Toast.makeText(getApplicationContext(), "Created profile.  Check DataWedge app UI.", Toast.LENGTH_LONG).show();

    }
    public void setConfig() {

        // SetConfig [Start]
        Bundle bMain = new Bundle();

        Bundle bConfigIntent = new Bundle();
        Bundle bParamsIntent = new Bundle();
        bParamsIntent.putString("intent_output_enabled", "true");
        bParamsIntent.putString("intent_action", "com.zebra.datacapture1.ACTION");
        bParamsIntent.putInt("intent_delivery", 2); //Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast, "3" for start foreground service
        bConfigIntent.putString("PLUGIN_NAME", "INTENT");
        bConfigIntent.putString("RESET_CONFIG", "false");
        bConfigIntent.putBundle("PARAM_LIST", bParamsIntent);

        Bundle bConfigSimulScan = new Bundle();
        Bundle bParamsSimulScan = new Bundle();
        bParamsSimulScan.putString("simulscan_input_enabled", "true");
        bParamsSimulScan.putString("simulscan_input_source", "Imager"); //Supported values: Camera, Imager, Default
        bParamsSimulScan.putString("simulscan_region_separator", "TAB"); //Supported Values:None, TAB, CR, LF, NONE
        bParamsSimulScan.putString("simulscan_log_dir", "/storage/zebra/intent/");
        bParamsSimulScan.putString("simulscan_enable_timestamp", "true");

        Bundle templateParamsBundle = new Bundle();
        templateParamsBundle.putString("dynamic_quantity", "99");
        bParamsSimulScan.putString("simulscan_template", "UserDefinedQuantity.xml"); // Ex:  UserDefinedQuantity.xml, Default - BankCheck.xml, Default - Barcode 1.xml, Default - Barcode 10.xml, Default - Barcode 2.xml, Default - Barcode 4.xml, Default - Barcode 5.xml, Default - BookNumber.xml, Default - DocCap + Optional Barcode.xml, Default - DocCap + Required Barcode.xml, Default - TravelDoc.xml, Default - Unstructured Multi-Line.xml, Default - Unstructured Single Line.xml
        bParamsSimulScan.putBundle("simulscan_template_params",templateParamsBundle);

        bConfigSimulScan.putString("PLUGIN_NAME", "SIMULSCAN");
        bConfigSimulScan.putString("RESET_CONFIG", "false");
        bConfigSimulScan.putBundle("PARAM_LIST", bParamsSimulScan);

        Bundle bConfigBarcode = new Bundle();
        Bundle bParamsBarcode = new Bundle();
        bParamsBarcode.putString("scanner_selection","auto");
        bParamsBarcode.putString("scanner_input_enabled","true");
        bConfigBarcode.putString("PLUGIN_NAME", "BARCODE");
        bConfigBarcode.putString("RESET_CONFIG", "false");
        bConfigBarcode.putBundle("PARAM_LIST", bParamsBarcode);

        Bundle bConfigMSR = new Bundle();
        Bundle bParamsMSR = new Bundle();
        bParamsMSR.putString("msr_input_enabled", "true");
        bConfigMSR.putString("PLUGIN_NAME", "MSR");
        bConfigMSR.putString("RESET_CONFIG", "false");
        bConfigMSR.putBundle("PARAM_LIST", bParamsMSR);

        Bundle bConfigIPOutput = new Bundle();
        Bundle bParamsIPOutput = new Bundle();
        bParamsIPOutput.putString("ip_output_enabled", "true");
        bParamsIPOutput.putString("ip_output_ip_wedge_enabled", "false");
        bParamsIPOutput.putString("ip_output_protocol", "UDP"); //Supported Values: TCP: UDP
        bParamsIPOutput.putString("ip_output_address", "192.168.0.1"); //Supported Values : IP Address format
        bParamsIPOutput.putString("ip_output_port", "55555"); //Supported Values : 1 - 65535

        bConfigIPOutput.putString("PLUGIN_NAME", "IP");
        bConfigIPOutput.putString("RESET_CONFIG", "false");
        bConfigIPOutput.putBundle("PARAM_LIST", bParamsIPOutput);

        Bundle bConfigToken = new Bundle();
        Bundle bParamsToken = new Bundle();

        bParamsToken.putString("send_tokens_option", "BARCODES_TOKENS"); // Supported Values: DISABLED, TOKENS, BARCODES_TOKENS
        bParamsToken.putString("token_separator", "LF"); //Supported Values:None, TAB, CR, LF, NONE
        bParamsToken.putString("multibarcode_separator", "LF"); //Supported Values:None, TAB, CR, LF, NONE

        Bundle tokenOrder_manufacturing_date_original = new Bundle();
        tokenOrder_manufacturing_date_original.putString("name","manufacturing_date_original");
        tokenOrder_manufacturing_date_original.putString("enabled","true");

        Bundle tokenOrder_expiration_date_original = new Bundle();
        tokenOrder_expiration_date_original.putString("name","expiration_date_original");
        tokenOrder_expiration_date_original.putString("enabled","true");

        Bundle tokenOrder_di = new Bundle();
        tokenOrder_di.putString("name","di");
        tokenOrder_di.putString("enabled","true");

        Bundle tokenOrder_lot_number = new Bundle();
        tokenOrder_lot_number.putString("name","lot_number");
        tokenOrder_lot_number.putString("enabled","true");

        Bundle tokenOrder_serial_number = new Bundle();
        tokenOrder_serial_number.putString("name","serial_number");
        tokenOrder_serial_number.putString("enabled","true");

        Bundle tokenOrder_mpho_lot_number = new Bundle();
        tokenOrder_mpho_lot_number.putString("name","mpho_lot_number");
        tokenOrder_mpho_lot_number.putString("enabled","true");

        Bundle tokenOrder_donation_id = new Bundle();
        tokenOrder_donation_id.putString("name","donation_id");
        tokenOrder_donation_id.putString("enabled","true");

        Bundle tokenOrder_labeler_identification_code = new Bundle();
        tokenOrder_labeler_identification_code.putString("name","labeler_identification_code");
        tokenOrder_labeler_identification_code.putString("enabled","true");

        Bundle tokenOrder_product_or_catalog_number = new Bundle();
        tokenOrder_product_or_catalog_number.putString("name","product_or_catalog_number");
        tokenOrder_product_or_catalog_number.putString("enabled","true");

        Bundle tokenOrder_unit_of_measure_id = new Bundle();
        tokenOrder_unit_of_measure_id.putString("name","unit_of_measure_id");
        tokenOrder_unit_of_measure_id.putString("enabled","true");

        Bundle tokenOrder_quantity = new Bundle();
        tokenOrder_quantity.putString("name","quantity");
        tokenOrder_quantity.putString("enabled","false");

        ArrayList<Bundle> tokenOrderList = new ArrayList<>();
        tokenOrderList.add(tokenOrder_manufacturing_date_original);
        tokenOrderList.add(tokenOrder_expiration_date_original);
        tokenOrderList.add(tokenOrder_lot_number);
        tokenOrderList.add(tokenOrder_di);
        tokenOrderList.add(tokenOrder_serial_number);
        tokenOrderList.add(tokenOrder_mpho_lot_number);
        tokenOrderList.add(tokenOrder_donation_id);
        tokenOrderList.add(tokenOrder_labeler_identification_code);
        tokenOrderList.add(tokenOrder_product_or_catalog_number);
        tokenOrderList.add(tokenOrder_unit_of_measure_id);
        tokenOrderList.add(tokenOrder_quantity);

        bParamsToken.putParcelableArrayList("token_order", tokenOrderList);

        bConfigToken.putString("PLUGIN_NAME", "TOKEN");
        bConfigToken.putString("OUTPUT_PLUGIN_NAME","IP");
        bConfigToken.putString("RESET_CONFIG", "true");
        bConfigToken.putBundle("PARAM_LIST", bParamsToken);

        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();
        bundlePluginConfig.add(bConfigIntent);
        bundlePluginConfig.add(bConfigBarcode);
        bundlePluginConfig.add(bConfigSimulScan);
        bundlePluginConfig.add(bConfigMSR);
        bundlePluginConfig.add(bConfigIPOutput);
        bundlePluginConfig.add(bConfigToken);

        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);

        //AppList[Start]
        Bundle bundleApp1 = new Bundle();
        bundleApp1.putString("PACKAGE_NAME",getPackageName());
        bundleApp1.putStringArray("ACTIVITY_LIST", new String[]{"*"});

        Bundle bundleApp2 = new Bundle();
        bundleApp2.putString("PACKAGE_NAME", getPackageName());
        bundleApp2.putStringArray("ACTIVITY_LIST", new String[]{"*"});

        Bundle bundleApp3 = new Bundle();
        bundleApp3.putString("PACKAGE_NAME", getPackageName());
        bundleApp3.putStringArray("ACTIVITY_LIST", new String[]{"*"});

        Bundle bundleApp4 = new Bundle();
        bundleApp4.putString("PACKAGE_NAME", getPackageName());
        bundleApp4.putStringArray("ACTIVITY_LIST", new String[]{"*"});

        // ADD APP_LIST BUNDLE(S) INTO THE MAIN BUNDLE
        bMain.putParcelableArray("APP_LIST", new Bundle[]{
                bundleApp1
                , bundleApp2
                , bundleApp3
                , bundleApp4
        });

        //AppList [End]

        Bundle bConfigDCP = new Bundle();
        Bundle bParamsDCP = new Bundle();
        bParamsDCP.putString("dcp_input_enabled", "true");
        bParamsDCP.putString("dcp_dock_button_on", "LEFT"); //Supported values: BOTH - Left or Right, LEFT - Left only, RIGHT - Right only
        bParamsDCP.putString("dcp_start_in", "FULLSCREEN"); //Supported Values: FULLSCREEN, BUTTON, BUTTON_ONLY
        bParamsDCP.putString("dcp_highest_pos", "10"); //Supported Values:  0 - 100
        bParamsDCP.putString("dcp_lowest_pos", "20"); //Supported Values: 0 - 100
        bParamsDCP.putString("dcp_drag_detect_time", "501"); //Supported Values: 0 - 1000
        bConfigDCP.putString("RESET_CONFIG", "true");
        bConfigDCP.putBundle("PARAM_LIST", bParamsDCP);

        bMain.putBundle("DCP", bConfigDCP);

        bMain.putString("PROFILE_NAME", EXTRA_PROFILENAME);
        bMain.putString("PROFILE_ENABLED", "true");
        bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");

        Intent iSetConfig = new Intent();
        iSetConfig.setAction("com.symbol.datawedge.api.ACTION");
        iSetConfig.putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain);
        iSetConfig.putExtra("SEND_RESULT", "COMPLETE_RESULT"); //Supported values: NONE, LAST_RESULT, COMPLETE_RESULT
        iSetConfig.putExtra("COMMAND_IDENTIFIER", "INTENT_API");
        // SetConfig [End]

        this.sendBroadcast(iSetConfig);
    }

    // Toggle soft scan trigger from UI onClick() event
    // Use SOFT_SCAN_TRIGGER: http://techdocs.zebra.com/datawedge/latest/guide/api/softscantrigger/
    public void ToggleSoftScanTrigger(View view) {
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
    }

    // Create filter for the broadcast intent
    private void registerReceivers() {

        Log.d(LOG_TAG, "registerReceivers()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESULT_NOTIFICATION);   // for notification result
        filter.addAction(ACTION_RESULT);                // for error code result
        filter.addAction(INTENT_OUTPUT_ACTION);                // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT);    // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        filter.addAction(getResources().getString(R.string.activity_action_from_service));
        registerReceiver(myBroadcastReceiver, filter);
    }

    // Unregister scanner status notification
    public void unRegisterScannerStatus() {
        Log.d(LOG_TAG, "unRegisterScannerStatus()");
        Bundle b = new Bundle();
        b.putString(EXTRA_KEY_APPLICATION_NAME, getPackageName());
        b.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS);
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(EXTRA_UNREGISTER_NOTIFICATION, b);
        this.sendBroadcast(i);
    }

    public String setDecoder(CheckBox decoder) {
        boolean checkValue = decoder.isChecked();
        String value = "false";
        if (checkValue) {
            value = "true";
            return value;
        } else
            return value;
    }

    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
try {
    db.collection("IntentActions").add(action).addOnSuccessListener(documentReference -> {});
}catch (Exception e){
}
            Log.d(LOG_TAG, "DataWedge Action:" + action);
            try {
                 action = intent.getAction();
                Bundle extras = intent.getExtras();

                /* ###### Processing scanned data from Intent output [Start] ###### */
                if (action.equals(INTENT_OUTPUT_ACTION)) {



                }
                /* ###### Processing scanned data from Intent output [Finish] ###### */

            } catch (Exception ex) {
                db.collection("Exception").add("Exception 0004 "+ex.getMessage());
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            // Get DataWedge version info
            if (intent.hasExtra(EXTRA_RESULT_GET_VERSION_INFO)) {
                Bundle versionInfo = intent.getBundleExtra(EXTRA_RESULT_GET_VERSION_INFO);
                String DWVersion = versionInfo.getString("DATAWEDGE");

                TextView txtDWVersion = (TextView) findViewById(R.id.txtGetDWVersion);
                txtDWVersion.setText(DWVersion);
                try {
                    Map<String, Object> user = new HashMap<>();
                    user.put("Version", DWVersion);
                    db.collection("Version").add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TAG", "Error adding document", e);
                                }
                            });
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error: " + e);
                }
                Log.i(LOG_TAG, "DataWedge Version: " + DWVersion);
            }

            if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                try {
                    Thread dataProcessingThrad = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle data = intent.getExtras();
                            if (data != null) {

                                String decodedMode = data.getString(DECODED_MODE);

                                /* ###### Processing scanned data when ScanningMode is set as "Single" [Start] ###### */
                                if (decodedMode.equals(SINGLE_DECODE_MODE)) {
                                    processSingleDecode(data);
                                }
                                /* ###### Processing scanned data when ScanningMode is set as "Single" [Finish] ###### */

                                /* ###### Processing scanned data when ScanningMode is set as "SimulScan" [Start] ###### */
                                else if (decodedMode.equals(MULTIPLE_DECODE_MODE)) {
                                   // processMultipleDecode(data, "DataRecive2");
                                    try {
                                        processMultipleDecodeNew(data);
                                    } catch (Exception e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        Map<String,String> map = new HashMap<>();
                                        map.put("Exception00021", e.getMessage());
                                        db.collection("Exception").add(map);
                                        throw  new RuntimeException(e);
                                    }
                                }
                                /* ###### Processing scanned data when ScanningMode is set as "SimulScan" [Finish] ###### */
                            }
                        }
                    });
                    dataProcessingThrad.start();
                } catch (Exception e) {
                    //  Catch error if the UI does not exist when we receive the broadcast...
                }
                //  Received a barcode scan
                try {
                    displayScanResult(intent, "via Broadcast");
                } catch (Exception e) {
                    //  Catch error if the UI does not exist when we receive the broadcast...
                }
            } else if (action.equals(ACTION_RESULT)) {
                // Register to receive the result code
                if ((intent.hasExtra(EXTRA_RESULT)) && (intent.hasExtra(EXTRA_COMMAND))) {
                    String command = intent.getStringExtra(EXTRA_COMMAND);
                    String result = intent.getStringExtra(EXTRA_RESULT);
                    String info = "";

                    if (intent.hasExtra(EXTRA_RESULT_INFO)) {
                        Bundle result_info = intent.getBundleExtra(EXTRA_RESULT_INFO);
                        Set<String> keys = result_info.keySet();
                        for (String key : keys) {
                            Object object = result_info.get(key);
                            if (object instanceof String) {
                                info += key + ": " + object + "\n";
                            } else if (object instanceof String[]) {
                                String[] codes = (String[]) object;
                                for (String code : codes) {
                                    info += key + ": " + code + "\n";
                                }
                            }
                        }
                        Log.d(LOG_TAG, "Command: " + command + "\n" +
                                "Result: " + result + "\n" +
                                "Result Info: " + info + "\n");
                        Toast.makeText(getApplicationContext(), "Error Resulted. Command:" + command + "\nResult: " + result + "\nResult Info: " + info, Toast.LENGTH_LONG).show();
                    }
                }

            }

            // Register for scanner change notification
            else if (action.equals(ACTION_RESULT_NOTIFICATION)) {
                if (intent.hasExtra(EXTRA_RESULT_NOTIFICATION)) {
                    Bundle extras = intent.getBundleExtra(EXTRA_RESULT_NOTIFICATION);
                    String notificationType = extras.getString(EXTRA_RESULT_NOTIFICATION_TYPE);
                    if (notificationType != null) {
                        switch (notificationType) {
                            case EXTRA_KEY_VALUE_SCANNER_STATUS:
                                // Change in scanner status occurred
                                String displayScannerStatusText = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_STATUS) +
                                        ", profile: " + extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME);
                                //Toast.makeText(getApplicationContext(), displayScannerStatusText, Toast.LENGTH_SHORT).show();
                                final TextView lblScannerStatus = (TextView) findViewById(R.id.lblScannerStatus);
                                lblScannerStatus.setText(displayScannerStatusText);
                                Log.i(LOG_TAG, "Scanner status: " + displayScannerStatusText);
                                break;

                            case EXTRA_KEY_VALUE_PROFILE_SWITCH:
                                // Received change in profile
                                // For future enhancement
                                break;

                            case EXTRA_KEY_VALUE_CONFIGURATION_UPDATE:
                                // Configuration change occurred
                                // For future enhancement
                                break;
                        }
                    }
                }
            }
        }
    };

    public static final String SINGLE_DECODE_MODE = "single_decode";
    public static final String MULTIPLE_DECODE_MODE = "multiple_decode";

    public static final String DECODED_MODE = "com.symbol.datawedge.decoded_mode";

    public static final String DATA_TAG = "com.symbol.datawedge.barcodes";

    private void displayScanResult(Intent initiatingIntent, String howDataReceived) {
        // store decoded data
        String decodedData = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_data));
        // store decoder type
        String decodedLabelType = initiatingIntent.getStringExtra(getResources().getString(R.string.datawedge_intent_key_label_type));

        final TextView lblScanData = (TextView) findViewById(R.id.lblScanData);
        final TextView lblScanLabelType = (TextView) findViewById(R.id.lblScanDecoder);

        lblScanData.setText(decodedData);
        lblScanLabelType.setText(decodedLabelType);
        try {
            Map<String, Object> user = new HashMap<>();
            user.put("DecodedData", decodedData);
            user.put("decodedLabelType", decodedLabelType);
            db.collection("FirstProcess").add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error adding document", e);
                        }
                    });

        } catch (Exception e) {

            db.collection("Exception").add("Excpetion 002 " + e.getMessage());
        }
        try {
            Thread dataProcessingThrad = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bundle data = initiatingIntent.getExtras();
                    if (data != null) {

                        String decodedMode = data.getString(DECODED_MODE);

                        /* ###### Processing scanned data when ScanningMode is set as "Single" [Start] ###### */
                        if (decodedMode.equals(SINGLE_DECODE_MODE)) {
                            processSingleDecode(data);
                        }
                        /* ###### Processing scanned data when ScanningMode is set as "Single" [Finish] ###### */

                        /* ###### Processing scanned data when ScanningMode is set as "SimulScan" [Start] ###### */
                        else if (decodedMode.equals(MULTIPLE_DECODE_MODE)) {
                          //  processMultipleDecode(data,"DataRecive1");
                            try {
                                processMultipleDecodeNew(data);
                            } catch (Exception e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                                Map<String,String> map = new HashMap<>();
                                map.put("Exception00021", e.getMessage());
                                db.collection("Exception").add(map);
                            }
                        }
                        /* ###### Processing scanned data when ScanningMode is set as "SimulScan" [Finish] ###### */
                    }
                }
            });
            dataProcessingThrad.start();
        } catch (Exception e) {
            db.collection("Exception").add(new HashMap<String,String>(){{
                put("Exception010022",e.getMessage());
            }});
        }
    }

    public static final String DECODE_DATA_EXTRA = "com.symbol.datawedge.decode_data";

    public static final String LABEL_TYPE = "label_type";
    public static final String FIELD_LABEL_TYPE = "label_type";

    public static final String STRING_DATA_KEY_SINGLE_BARCODE = "data_string";

    public static final String LABEL_TYPE_TAG = "com.symbol.datawedge.label_type";

    public static final String STRING_DATA_KEY = "com.symbol.datawedge.data_string";

    @SuppressLint("SetTextI18n")
    private void processSingleDecode(Bundle data) {
        String decodeDataUri = data.getString(DECODE_DATA_EXTRA);
        String barcodeData = "";
        //Check if the data coming through the content provider.
        if (decodeDataUri != null) {
            //Data is coming through the content provider, using a Cursor object to extract data
            Cursor cursor = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                cursor = getContentResolver()
                        .query(Uri.parse(decodeDataUri), null, null, null);
            }
            if (cursor != null) {
                cursor.moveToFirst();

                @SuppressLint("Range") String labelType = cursor
                        .getString(cursor.getColumnIndex(LABEL_TYPE));
                @SuppressLint("Range") String dataString = cursor
                        .getString(cursor.getColumnIndex(STRING_DATA_KEY_SINGLE_BARCODE));

                barcodeData += "\nLabel type: " + labelType;
                barcodeData += "\nString data: " + dataString;
            }
        } else {
            //Data is coming through the Intent bundle itself
            String labelType = data.getString(LABEL_TYPE_TAG);
            String dataString = data.getString(STRING_DATA_KEY);

            barcodeData += "\nLabel type: " + labelType;
            barcodeData += "\nString data: " + dataString;
        }
        String finalBarcodeData = barcodeData;
        runOnUiThread(() -> {
            final TextView lblScanLabelType = (TextView) findViewById(R.id.lblScanDecoder);
            // TextView txtBarcodeData = new TextView(getApplicationContext());
            lblScanLabelType.setText("Single Barcode Data " + finalBarcodeData);
        });

        try {
            db.collection("ProccessedSingleData").document("BarcodeData").set(barcodeData);
            Map<String, Object> user = new HashMap<>();
            user.put("BarcodeData", barcodeData);
            db.collection("ProccessedSingleData").add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error adding document", e);
                        }
                    });
        } catch (Exception e) {
            db.collection("Exception").add("Excpetion 003 " + e.getMessage());
        }
        //  showInUI(txtBarcodeData, null);
        //updateStatus("Data processing successful");
    }

    public static final String FIELD_DATA_URI = "com.symbol.datawedge.field_data_uri";
    //Columns [id, label_type, data_string, decode_data, next_data_uri, full_data_size, data_buffer_size]
    public static final String DATA_STRING = "data_string";

    public static final String DATA_NEXT_URI = "next_data_uri";

    public static final String DECODE_DATA = "field_raw_data";

    public static final String FULL_DATA_SIZE = "full_data_size";

    public static final String RAW_DATA_SIZE = "data_buffer_size";


    @SuppressLint({"Range", "SetTextI18n"})
    private void processMultipleDecodeNew(Bundle data) throws IOException {
        ArrayList<Bundle> fields = data.getParcelableArrayList(DATA_TAG);
        if(fields == null) //Content provider is not enabled in Intent Output plugin or Scanning mode is not selected as "SimulScan"
        {
            updateStatus("Content provider is not enabled in Intent Output plugin " +
                    "or Scanning mode is not selected as \"SimulScan\".\nPlease check and try again");
            return;
        }
        String strResultStatusData = null;
        //Iterate through each field
        for (Bundle field : fields) {

            String decodeDataUri = field.getString(FIELD_DATA_URI);
            String uri=field.getString("com.symbol.datawedge.decode_data");


            Cursor cursor = null;
            if(uri != null)
                cursor = getContentResolver().query(Uri.parse(uri),
                        null, null, null);
            Cursor finalCursor1 = cursor;

            if (cursor != null) {

                Map<String,String> map = null;

               cursor.moveToFirst();
                    map = new HashMap<>();
                    Cursor finalCursor = cursor;
                 //   db.collection("CursorCount").add(new HashMap<String,String>(){{put("CursorCount", String.valueOf(finalCursor.getColumnCount()));}});
                    /*for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columnName = cursor.getColumnName(i);
                        String columnValue = null;
                        try {
                            columnValue = cursor.get
                        } catch (Exception e) {
                            columnValue=e.getMessage();
                        }
                        map.put(columnName, columnValue);
                        Log.d("DataWedge", "Column " + columnName + " has value " + columnValue);
                    }*/
                    //Columns [id, label_type, data_string, decode_data, next_data_uri, full_data_size, data_buffer_size]
                    map.put("Columns", "Columns " + Arrays.toString( cursor.getColumnNames()));
                    db.collection("CursorData").add(map);


                    String labelType = cursor.
                           getString(cursor.getColumnIndex(FIELD_LABEL_TYPE));

                    strResultStatusData += "\nLabel type: " + labelType;

                    String dataString = cursor
                            .getString(cursor.getColumnIndex("data_string"));
                    strResultStatusData += "\nString data: " + dataString;



                String nextURI = cursor.getString(cursor.getColumnIndex(DATA_NEXT_URI));
                byte[] binaryData = null;
              /*  if (nextURI.isEmpty()) { //No data chunks. All data are available in one chunk
                    binaryData = cursor.getBlob(cursor.getColumnIndex(DECODE_DATA));
                } else {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        final String fullDataSize = cursor
                                .getString(cursor.getColumnIndex(FULL_DATA_SIZE));
                        int bufferSize = cursor.getInt(cursor
                                .getColumnIndex(RAW_DATA_SIZE));
                        baos.write(cursor.getBlob(cursor
                                .getColumnIndex(DECODE_DATA))); //Read the first chunk from initial set
                        while (!nextURI.isEmpty()) {
                            Cursor imageDataCursor = getContentResolver()
                                    .query(Uri.parse(nextURI), null,
                                            null, null);
                            if (imageDataCursor != null) {
                                imageDataCursor.moveToFirst();
                                bufferSize += imageDataCursor
                                        .getInt(imageDataCursor
                                                .getColumnIndex(RAW_DATA_SIZE));
                                byte[] bufferData = imageDataCursor
                                        .getBlob(imageDataCursor
                                                .getColumnIndex(DECODE_DATA));
                                baos.write(bufferData);
                                nextURI = imageDataCursor
                                        .getString(imageDataCursor
                                                .getColumnIndex(DATA_NEXT_URI));
                            }
                            imageDataCursor.close();

                            updateStatus("Data being processed, please wait..\n" +
                                    bufferSize + "/" + fullDataSize + " bytes merged");
                        }
                        binaryData = baos.toByteArray();
                        baos.close();

                }*/
                try {
                    cursor.close();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }


            }
        }
     HashMap   map = new HashMap<>();
        map.put("DataBarcodes", "Barcods : " + strResultStatusData);
        db.collection("MultiBarcodeData1").add(map);
        String finalStrResultStatusData = strResultStatusData;
        MainActivity.this. runOnUiThread(() -> {
            final TextView lblScanLabelType = (TextView) findViewById(R.id.lblScanDecoder);
            // TextView txtBarcodeData = new TextView(getApplicationContext());
            lblScanLabelType.setText("Multi Barcode Data " + finalStrResultStatusData);
        });
        updateStatus("Data processing successful");
    }
    void updateStatus(String status)
    {
        MainActivity.this. runOnUiThread(() -> {
            final TextView lblScanLabelType = (TextView) findViewById(R.id.lblScanData);
            // TextView txtBarcodeData = new TextView(getApplicationContext());
            lblScanLabelType.setText("Multi Barcode Data " + status);
        });
        //Show status in UI
    }
    private void showInUI(final TextView textView, final ImageView imageView)
    {

    }

    private void sendDataWedgeIntentWithExtra(String action, String extraKey, Bundle extras) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extras);
        if (bRequestSendResult)
            dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
        this.sendBroadcast(dwIntent);
    }

    private void sendDataWedgeIntentWithExtra(String action, String extraKey, String extraValue) {
        Intent dwIntent = new Intent();
        dwIntent.setAction(action);
        dwIntent.putExtra(extraKey, extraValue);
        if (bRequestSendResult)
            dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
        this.sendBroadcast(dwIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myBroadcastReceiver);
        unRegisterScannerStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
