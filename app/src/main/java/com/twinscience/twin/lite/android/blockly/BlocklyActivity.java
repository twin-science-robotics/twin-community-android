/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.twinscience.twin.lite.android.blockly;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.material.snackbar.Snackbar;
import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.android.codegen.LoggingCodeGeneratorCallback;
import com.google.blockly.model.DefaultBlocks;
import com.google.blockly.utils.BlockLoadingException;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.squareup.seismic.ShakeDetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.twinscience.twin.lite.android.R;
import com.twinscience.twin.lite.android.TwinLiteApplication;
import com.twinscience.twin.lite.android.TwinSharedPreferences;
import com.twinscience.twin.lite.android.blockly.orientationSensor.sensors.Orientation;
import com.twinscience.twin.lite.android.blockly.orientationSensor.utils.OrientationSensorInterface;
import com.twinscience.twin.lite.android.blockly.presentation.other.AlertState;
import com.twinscience.twin.lite.android.data.ProjectModel;
import com.twinscience.twin.lite.android.project.data.ProjectEntity;
import com.twinscience.twin.lite.android.project.db.ProjectDao;
import com.twinscience.twin.lite.android.project.def.ProjectDef;
import com.twinscience.twin.lite.android.utils.DateUtils;
import com.twinscience.twin.lite.android.utils.GPSHelper;
import com.twinscience.twin.lite.android.utils.LocaleManager;
import com.twinscience.twin.lite.android.utils.MathUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Simplest implementation of AbstractBlocklyActivity.
 */
public class BlocklyActivity extends AbstractBlocklyActivity implements ShakeDetector.Listener, OrientationSensorInterface {
    private static final String TAG = "BlocklyActivity";
    public static final String IS_CONNECTED_KEY = "isConnectedKey";

    private String saveFileName = "simple_workspace.xml";
    private String autoSaveFileName = "simple_workspace_temp.xml";
    private final Handler handler = new Handler();

    // Add custom blocks to this list.
    private List<String> BLOCK_DEFINITIONS = Arrays.asList(
            "default/logic_blocks.json", "default/loop_blocks.json",
            "default/math_blocks.json", "default/procedures.json", "default/variable_blocks.json"
    );

    // Add custom blocks to this list.
    private List<String> BLOCK_DEFINITIONS_TR = Arrays.asList(
            "default/logic_blocks_tr.json", "default/loop_blocks_tr.json",
            "default/math_blocks_tr.json", "default/procedures_tr.json", "default/variable_blocks_tr.json"
    );

    CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new LoggingCodeGeneratorCallback(this, TAG);
    private RxBleClient rxBleClient;
    private Disposable scanSubscription;
    private boolean isConnected;
    private String deviceName = "Twin";
    private RxBleDevice device;
    private boolean isDeviceFound;
    private AlertDialog dialogState;
    private WebView webView;
    private TwinWebInterface twinWebInterface;
    private ProjectModel selectedProject;
    private GPSHelper gpsHelper;
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    public long shakeTime;


    @Inject
    ProjectDao projectDao;
    private Orientation orientationSensor;

    //Orientation
    public Double currentPitch;//rotation around x-axis (-180 to 180), with positive values when the z-axis moves toward the y-axis.
    public Double currentRoll;// Roll, rotation around the y-axis (-90 to 90) increasing as the device moves clockwise.
    public boolean isOrientationSupported;
    private Disposable connectionSubscription;
    private Disposable dbSubscription;
    private TextView tvProjectName;
    private Disposable connectionStateSubscription;
    private ImageButton btnPlay;
    private ImageButton btnStop;
    private HashMap<RxBleDevice, int[]> scannedDeviceMap;
    private long scanStartTime;
    private int TIME_OUT_LIMIT_SECONDS = 3;

    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        String displayLanguage = LocaleManager.getLocaleLanguage();
        List<String> definitions = new ArrayList<>(displayLanguage.equalsIgnoreCase("tr") ? BLOCK_DEFINITIONS_TR : BLOCK_DEFINITIONS);
        definitions.add(displayLanguage.equalsIgnoreCase("tr") ? "twin_tr.json" : "twin_en.json");
        return definitions;
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        // Replace with a toolbox that includes application specific blocks.
        String displayLanguage = LocaleManager.getLocaleLanguage();
        return displayLanguage.equalsIgnoreCase("tr") ?
                DefaultBlocks.TOOLBOX_TR_PATH : DefaultBlocks.TOOLBOX_EN_PATH;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the device has BLE
        View btnExperiment = this.findViewById(R.id.btn_experiment_menu);
        btnPlay = this.findViewById(R.id.btn_experiment_play);
        btnStop = this.findViewById(R.id.btn_experiment_stop);
        View btnSave = this.findViewById(R.id.btn_experiment_save);
        View btnBack = this.findViewById(R.id.unified_btn_back);
        webView = this.findViewById(R.id.blockly_mock_web_view);
        tvProjectName = this.findViewById(R.id.blockly_tv_project_name);
        scannedDeviceMap = new HashMap<>();

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                switch (consoleMessage.message()) {
                    case "Uncaught Infinite loop.":
                        Log.d(TAG, "JS Exception: " + consoleMessage.message());
                        showAlertWithButton(AlertState.JS_ERROR, getString(R.string.lbl_js_infinite_loop), getString(R.string.lbl_ok));
                        showPlayButton();
                        break;
                    case "Uncaught Code evaluation stopped by client.":
                        Log.d(TAG, "JS Exception: " + consoleMessage.message());
                        showPlayButton();
                        break;
                    case "Uncaught Code evaluation completed.":
                        Log.d(TAG, "JS Exception: " + consoleMessage.message());
                        showPlayButton();
                        break;
                }

                return super.onConsoleMessage(consoleMessage);
            }
        });
        WebView.setWebContentsDebuggingEnabled(true);
        initFields();
        TwinLiteApplication.Companion.getAppComponent(this).inject(this);
        if (savedInstanceState == null) {

            if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("selectedProject") != null) {
                selectedProject = getIntent().getExtras().getParcelable("selectedProject");

                if (selectedProject != null) {
                    switch (selectedProject.getType()) {
                        case PERSONAL:
                            btnSave.setVisibility(View.VISIBLE);
                            tvProjectName.setText(selectedProject.getTitle());
                            mBlocklyActivityHelper.loadWorkspaceFromAppDirSafely(getWorkspaceSavePath());
                            initFileNames(selectedProject.getId());
                            break;
                        case TWIN:
                            btnSave.setVisibility(View.GONE);
                            tvProjectName.setText(selectedProject.getTitle());
                            try {
                                if (selectedProject.getFileName() != null) {
                                    mBlocklyActivityHelper.mController.loadWorkspaceContents(getAssets().open(selectedProject.getFileName()));
                                }
                            } catch (BlockLoadingException | IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case NEW:
                            btnSave.setVisibility(View.VISIBLE);
                            break;

                    }

                    tvProjectName.setText(selectedProject.getTitle());
                    mBlocklyActivityHelper.loadWorkspaceFromAppDirSafely(getWorkspaceSavePath());
                }
            }
            mCodeGeneratorCallback = generatedCode -> handler.post(() -> evalGeneratedCode(generatedCode));
        } else if (savedInstanceState.getBoolean(IS_CONNECTED_KEY)) {
            isConnected = savedInstanceState.getBoolean(IS_CONNECTED_KEY);
        }

        btnExperiment.setOnClickListener(view -> {

        });

        btnBack.setOnClickListener(view -> onBackPressed());

        btnPlay.setOnClickListener(view -> {
            if (isDefaultServicesEnabled()) {
                if (getController().getWorkspace().hasBlocks()) {
                    if (!isConnected) {
                        startScan();
                    } else {
                        generateCode();
                    }
                } else {
                    Snackbar.make(view, getString(R.string.warn_no_blocks), Snackbar.LENGTH_SHORT).show();
                }
            }

        });

        btnStop.setOnClickListener(v -> {
            // Toast.makeText(this, "Stoop", Toast.LENGTH_SHORT).show();
            evalJSFunction("TwinJSSession.setStopStatus(true);");
            showPlayButton();


        });
        btnSave.setOnClickListener(view -> {
            if (selectedProject.getType() == ProjectDef.NEW) {
                showNameProjectDialog(false, false);
            } else {
                saveWorkspaceSafely(false);
            }
        });

        tvProjectName.setOnClickListener(v -> {
            switch (selectedProject.getType()) {
                case NEW:
                    showNameProjectDialog(false, false);
                    break;
                case PERSONAL:
                    showNameProjectDialog(false, true);
                    break;
            }
        });
    }

    private void showNameProjectDialog(boolean shouldEndActivity, boolean isUpdate) {
        View view = getLayoutInflater().inflate(R.layout.dialog_name_project, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.DialogTheme);
        alertDialogBuilder.setView(view);
        AlertDialog dialog = alertDialogBuilder.show();
        CardView cardSave = view.findViewById(R.id.dialog_name_root_save);
        CardView cardCancel = view.findViewById(R.id.dialog_name_root_cancel);
        EditText editName = view.findViewById(R.id.dialog_name_edit);


        if (isUpdate) {
            editName.setText(tvProjectName.getText().toString());
            editName.selectAll(); //user can delete the whole text at one go
            editName.requestFocus();
        }

        cardSave.setOnClickListener(view1 -> {
            String title = editName.getText().toString();
            if (title.isEmpty()) {
                Snackbar.make(cardSave, getString(R.string.warn_name_project), Snackbar.LENGTH_SHORT).show();
            } else {
                if (isUpdate) {
                    updateProjectName(title);
                } else insertEntityWithTitle(title, shouldEndActivity);
                dialog.dismiss();
            }
        });

        cardCancel.setOnClickListener(view1 -> dialog.dismiss());

        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        showKeyboard();
    }

    private void saveWorkspaceSafely(boolean shouldEndActivity) {
        if (mBlocklyActivityHelper.saveWorkspaceToAppDirSafely(saveFileName)) {
            mBlocklyActivityHelper.getController().getDragger().isWorkSpaceChanged = false;
            if (shouldEndActivity) {
                goBack();
            } else {

                showAlertWithButton(AlertState.SAVED, getString(R.string.lbl_saved_success), getString(R.string.lbl_ok));
            }
        }
    }

    private void initFileNames(String fileName) {
        this.saveFileName = fileName + ".xml";
        this.autoSaveFileName = fileName;
    }


    private void updateProjectName(String title) {
        dbSubscription = Observable.just(projectDao)
                .subscribeOn(Schedulers.io())
                .subscribe(dao -> {
                    long id = Calendar.getInstance().getTimeInMillis();
                    initFileNames(String.valueOf(id));
                    ProjectEntity projectentity = new ProjectEntity(Long.valueOf(selectedProject.getId()), title, DateUtils.INSTANCE.convertMSToFormattedString("dd.MM.yyyy", id), saveFileName, "");
                    dao.updateProject(projectentity);
                    runOnUiThread(() -> {
                        selectedProject.setId(String.valueOf(id));
                        selectedProject.setType(ProjectDef.PERSONAL);
                        selectedProject.setTitle(title);
                        tvProjectName.setText(title);
                        saveWorkspaceSafely(false);
                    });
                });
    }

    private void insertEntityWithTitle(String title, boolean shouldEndActivity) {
        dbSubscription = Observable.just(projectDao)
                .subscribeOn(Schedulers.io())
                .subscribe(dao -> {
                    long id = Calendar.getInstance().getTimeInMillis();
                    initFileNames(String.valueOf(id));
                    ProjectEntity projectentity = new ProjectEntity(id, title, DateUtils.INSTANCE.convertMSToFormattedString("dd.MM.yyyy", id), saveFileName, "");
                    dao.insertProject(projectentity);
                    runOnUiThread(() -> {
                        selectedProject.setId(String.valueOf(id));
                        selectedProject.setType(ProjectDef.PERSONAL);
                        selectedProject.setTitle(title);
                        tvProjectName.setText(title);
                        saveWorkspaceSafely(shouldEndActivity);
                    });
                });

    }

    private void initFields() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        orientationSensor = new Orientation(this.getApplicationContext(), this);
        isOrientationSupported = orientationSensor.isSupport();
        if (isOrientationSupported) {
            orientationSensor.init(1.0, 1.0, 1.0);
        } else {
            boolean isWarnedBefore = TwinSharedPreferences.Companion.loadBoolean(this, TwinSharedPreferences.IS_WARNED_ORIENTATION, TwinSharedPreferences.IS_WARNED_ORIENTATION, false);
            if (!isWarnedBefore) {
                TwinSharedPreferences.Companion.saveBoolean(this, TwinSharedPreferences.IS_WARNED_ORIENTATION, TwinSharedPreferences.IS_WARNED_ORIENTATION, true);
                showAlertWithButton(AlertState.ORIENTATION_NOT_SUPPORTED, getString(R.string.warn_orientation_not_supported), getString(R.string.lbl_ok));
            }
        }

        gpsHelper = new GPSHelper(this);
        rxBleClient = RxBleClient.create(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/twin_interpreter.html");
        twinWebInterface = new TwinWebInterface(this);
        webView.addJavascriptInterface(twinWebInterface, "Twin");
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakeDetector.start(sensorManager);
        shakeDetector.setSensitivity(10);
        if (isOrientationSupported) {
            orientationSensor.on(1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        shakeDetector.stop();
        if (isOrientationSupported) {
            orientationSensor.off();
        }
    }

    /**
     * Check bluetooth & gps services
     */
    private boolean isDefaultServicesEnabled() {
        boolean isBluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (!isBluetoothEnabled && !gpsHelper.isGPSenabled()) {
            showAlertWithButton(AlertState.ENABLE_BLUETOOTH, getString(R.string.title_bluetooth_settings), getString(R.string.lbl_ok));
            return false;
        } else if (isBluetoothEnabled && !gpsHelper.isGPSenabled()) {
            showAlertWithButton(AlertState.ENABLE_LOCATION, getString(R.string.title_location_settings), getString(R.string.lbl_ok));
            return false;
        } else if (!isBluetoothEnabled && gpsHelper.isGPSenabled()) {
            showAlertWithButton(AlertState.ENABLE_BLUETOOTH, getString(R.string.title_bluetooth_settings), getString(R.string.lbl_ok));
            return false;
        } else {
            return true;
        }
    }

    /**
     * Communication Methods
     */
    public void evalGeneratedCode(String generatedCode) {

        Log.d(TAG, "evalGeneratedCode - generatedCode: " + generatedCode);

        String javascriptInit = "TwinJS.init(`" + generatedCode + "`);";

        webView.evaluateJavascript(javascriptInit, s -> {

            Log.d(TAG, "evalGeneratedCode: " + s == null ? "null" : s);
        });

    }

    private void showPlayButton() {
        runOnUiThread(() -> {
            btnPlay.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.GONE);
        });
    }


    private void showStopButton() {
        runOnUiThread(() -> {
            btnPlay.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Communication Methods
     */
    public void evalJSFunction(String code) {
        Log.d(TAG, "evalJSFunction - code: " + code);

        webView.evaluateJavascript(code, s -> {
            Log.d(TAG, "evalJSFunction: " + s == null ? "null" : s);
        });

    }

    private void generateCode() {
        showStopButton();

        mBlocklyActivityHelper.requestCodeGeneration(
                getBlockGeneratorLanguage(),
                getBlockDefinitionsJsonPaths(),
                getGeneratorsJsPaths(),
                getCodeGenerationCallback());
    }

    /**
     * Scanning Methods
     * <p>
     * Scans for 2 seconds and connects to the nearest device containing the name "Twin"
     */
    private void startScan() {
        scanStartTime = (long) Calendar.getInstance().getTimeInMillis() / 1000;
        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder().build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(
                        scanResult -> {
                            long updatedTime = Calendar.getInstance().getTimeInMillis() / 1000;
                            Log.d("RSSI CHECK", "timeDiffFirst: " + String.valueOf(Calendar.getInstance().getTimeInMillis() - scanStartTime));
                            RxBleDevice bleDevice = scanResult.getBleDevice();
                            String name = bleDevice.getName();
                            Log.e(TAG, name == null ? "null" : name);
                            //Return ignored results
                            if (isConnected || isDeviceFound) {
                                return;
                            }

                            Log.d("RSSI CHECK", "timeDiff: " + String.valueOf(updatedTime - scanStartTime));


                            if ((((updatedTime - scanStartTime)) < TIME_OUT_LIMIT_SECONDS) && name != null && name.contains(deviceName)) {
                                collectScannedDevices(scanResult, bleDevice);
                            } else if (!scannedDeviceMap.isEmpty()) {

                                Log.d("RSSI CHECK", "scannedDeviceMap: " + scannedDeviceMap.toString());
                                device = getNearestDevice();
                                isDeviceFound = true;
                                runOnUiThread(this::connectToDevice);
                            } else if ((((updatedTime - scanStartTime)) > TIME_OUT_LIMIT_SECONDS)) {
                                disposeScanSubscription(true);
                                showAlertWithButton(AlertState.TIMEOUT, getString(R.string.lbl_timeout), getString(R.string.lbl_ok));
                            }
                        },
                        throwable -> {
                            if (throwable.getMessage().equals("Scan failed because application registration failed (code 6)")) {
                                disposeScanSubscription(true);
                                showAlertWithButton(AlertState.SCAN_ERROR, getString(R.string.warn_scan_registration_fail), getString(R.string.lbl_ok));
                                //Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                            throwable.printStackTrace();
                        }
                );

        showAlertWithButton(AlertState.SCANNING, getString(R.string.lbl_scanning_twin), getString(R.string.lbl_cancel));

    }

    private void disposeScanSubscription(boolean isScanEnabled) {
        dismisDialogState();
        if (!scanSubscription.isDisposed()) {
            scanSubscription.dispose();
        }
        scannedDeviceMap.clear();
        if (isScanEnabled) {
            setScanEnable();
        }
    }

    private RxBleDevice getNearestDevice() {
        RxBleDevice nearestDevice = null;
        double biggestAverage = Double.MAX_VALUE * -1;
        for (RxBleDevice rxBleDevice : scannedDeviceMap.keySet()) {
            int[] rssiLevels = scannedDeviceMap.get(rxBleDevice);
            double average = MathUtils.findAverageWithoutUsingStream(rssiLevels);
            if (average > biggestAverage) {
                nearestDevice = rxBleDevice;
            }
        }
        return nearestDevice;
    }

    private void collectScannedDevices(ScanResult scanResult, RxBleDevice bleDevice) {
        int[] savedRssiList = scannedDeviceMap.get(bleDevice);
        if (savedRssiList == null) {
            int[] rssiLevels = new int[]{scanResult.getRssi()};
            scannedDeviceMap.put(bleDevice, rssiLevels);
        } else {
            savedRssiList[savedRssiList.length - 1] = (scanResult.getRssi());
            scannedDeviceMap.put(bleDevice, savedRssiList);
        }
        Log.d("RSSI CHECK", "collectScannedDevices: " + scannedDeviceMap.toString());

    }

    private void connectToDevice() {
        // <-- autoConnect flag
        connectionSubscription = device.establishConnection(false) // <-- autoConnect flag
                .subscribe(
                        rxBleConnection -> {
                            runOnUiThread(() ->
                                    showAlertWithButton(AlertState.CONNECTED, getString(R.string.lbl_success_connection), getString(R.string.lbl_ok)));
                            dismisDialogState();
                            twinWebInterface.setRxBleConnection(rxBleConnection);
                            isConnected = true;
                            generateCode();
                            checkConnectionState();
                            disposeScanSubscription(false);

                        },
                        throwable -> {
                            if (throwable.getMessage().toLowerCase().contains("disconnect")) {
                            }

                        });
    }

    private void checkConnectionState() {
        connectionStateSubscription = device.observeConnectionStateChanges()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxBleConnectionState -> {
                    String state = rxBleConnectionState.toString();
                    if (state.equalsIgnoreCase("RxBleConnectionState{DISCONNECTED}")) {
                        Log.d("MERTLOSTAR", state);
                        showAlertWithButton(AlertState.DISCONNECTED, getString(R.string.lbl_twin_disconnected), getString(R.string.lbl_ok));
                        disposeScanSubscription(true);
                        showPlayButton();

                    }
                });
    }

    /**
     * Alert Methods
     */

    /**
     * @param title is Dialog Title
     * @param label is Button Label
     */
    private void showAlertWithButton(AlertState state, String title, String label) {
        View view = getLayoutInflater().inflate(R.layout.dialog_with_button, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.DialogTheme);
        alertDialogBuilder.setView(view);
        AlertDialog dialog = null;
        if (state == AlertState.SCANNING || state == AlertState.TIMEOUT) {
            dialogState = alertDialogBuilder.show();
            dialogState.setCancelable(false);
        } else {
            dialog = alertDialogBuilder.show();
        }

        TextView tvTitle = view.findViewById(R.id.dialog_with_button_tv_title);
        TextView tvAction = view.findViewById(R.id.dialog_with_button_tv_action);
        tvTitle.setText(title);
        tvAction.setText(label);
        CardView cardCancel = view.findViewById(R.id.dialog_with_button_root_cancel);
        AlertDialog finalDialog = dialog;
        cardCancel.setOnClickListener(view1 -> {
            switch (state) {
                case SCANNING:
                    dialogState.dismiss();
                    disposeScanSubscription(true);
                case TIMEOUT:
                    dialogState.dismiss();
                    break;
                case CONNECTED:
                case ORIENTATION_NOT_SUPPORTED:
                case DISCONNECTED:
                case SAVED:
                case ENABLE_LOCATION:
                case ENABLE_BLUETOOTH:
                case JS_ERROR:
                case SCAN_ERROR:
                    if (finalDialog != null) {
                        finalDialog.dismiss();
                    }
                    break;

            }

        });
        //We only want to close the dialog when the user presses a button
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (state != AlertState.ORIENTATION_NOT_SUPPORTED && state != AlertState.SCANNING && state != AlertState.SCAN_ERROR && state != AlertState.TIMEOUT && state != AlertState.JS_ERROR && finalDialog != null && finalDialog.isShowing() && !this.isFinishing()) {
                dismisDialogState();
                finalDialog.dismiss();
            }
            handler.removeCallbacksAndMessages(null);
        }, 1000);
    }

    private void showAlertWithTwoButtons(String title, String lblLeft, String lblRight) {
        View view = getLayoutInflater().inflate(R.layout.dialog_with_two_buttons, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.DialogTheme);
        alertDialogBuilder.setView(view);
        TextView tvTitle = view.findViewById(R.id.dialog_with_two_button_tv_title);
        TextView tvActionLeft = view.findViewById(R.id.dialog_with_two_buttons_tv_left);
        TextView tvActionRight = view.findViewById(R.id.dialog_with_two_buttons_tv_right);
        CardView rootActionLeft = view.findViewById(R.id.dialog_with_two_buttons_root_left);
        CardView rootActionRight = view.findViewById(R.id.dialog_with_two_buttons_root_right);

        tvTitle.setText(title);
        tvActionLeft.setText(lblLeft);
        tvActionRight.setText(lblRight);

        AlertDialog alertDialog = alertDialogBuilder.show();

        alertDialog.show();

        rootActionLeft.setOnClickListener(viewLeft -> {
            alertDialog.dismiss();
            goBack();
        });

        rootActionRight.setOnClickListener(viewRight -> {
            alertDialog.dismiss();
            if (selectedProject.getType() == ProjectDef.NEW) {
                showNameProjectDialog(true, false);
            } else saveWorkspaceSafely(true);
        });


    }

    private void dismisDialogState() {
        if (dialogState != null && dialogState.isShowing()) {
            dialogState.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: MERT");
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_CONNECTED_KEY, isConnected);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: MERT");
        super.onRestoreInstanceState(savedInstanceState);
        isConnected = savedInstanceState.getBoolean(IS_CONNECTED_KEY);
    }


    /**
     * Handles the back button.  Default implementation attempts to close the navigation menu, then
     * the toolbox and trash flyouts, before allowing the system to back out of the activity.
     *
     * @see AbstractBlocklyActivity {@link #onBackToCloseNavMenu()}
     */
    @Override
    public void onBackPressed() {
        if (mBlocklyActivityHelper.getController().getDragger().isWorkSpaceChanged && selectedProject.getType() != ProjectDef.TWIN) {
            showAlertWithTwoButtons(getString(R.string.title_dialog_back_blockly), getString(R.string.lbl_dont_save), getString(R.string.lbl_save));
        } else {
            goBack();
        }
    }

    private void goBack() {
        disposeItems();
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void disposeItems() {
        if (scanSubscription != null && !scanSubscription.isDisposed()) {
            scanSubscription.dispose();
        }
        if (connectionSubscription != null && !connectionSubscription.isDisposed()) {
            connectionSubscription.dispose();
        }
        if (dbSubscription != null && !dbSubscription.isDisposed()) {
            dbSubscription.dispose();
        }
        if (connectionStateSubscription != null && !connectionStateSubscription.isDisposed()) {
            connectionStateSubscription.dispose();
        }
        if (twinWebInterface != null) {
            twinWebInterface.disposeItems();
        }


    }

    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        List<String> paths = new ArrayList<String>(1);
        paths.add("twin.js");
        return paths;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        // Uses the same callback for every generation call.
        return mCodeGeneratorCallback;
    }

    /**
     * Optional override of the save path, since this demo Activity has multiple Blockly
     * configurations.
     *
     * @return Workspace save path used by BlocklyActivity and SimpleFragment.
     */
    @Override
    @NonNull
    protected String getWorkspaceSavePath() {
        return saveFileName;
    }

    /**
     * Optional override of the auto-save path, since this demo Activity has multiple Blockly
     * configurations.
     *
     * @return Workspace auto-save path used by BlocklyActivity and SimpleFragment.
     */
    @Override
    @NonNull
    protected String getWorkspaceAutosavePath() {
        return autoSaveFileName;
    }

    private void setScanEnable() {
        isConnected = false;
        isDeviceFound = false;
    }

    @Override
    public void hearShake() {
        shakeTime = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void orientation(Double azimuth, Double pitch, Double roll) {
        currentPitch = pitch;
        currentRoll = roll;
    }

    @Override
    protected void onDestroy() {
        if (dialogState != null) {
            dialogState.cancel();
        }

        super.onDestroy();
    }
}
