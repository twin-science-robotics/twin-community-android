package com.twinscience.twin.lite.android.blockly;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.google.blockly.android.AbstractBlocklyFragment;
import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.android.codegen.LoggingCodeGeneratorCallback;
import com.google.blockly.model.DefaultBlocks;
import com.google.blockly.utils.BlockLoadingException;
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
import com.twinscience.twin.lite.android.ble.utils.BleUtils;
import com.twinscience.twin.lite.android.blockly.presentation.other.BlocklyAlertState;
import com.twinscience.twin.lite.android.blockly.orientationSensor.sensors.Orientation;
import com.twinscience.twin.lite.android.blockly.orientationSensor.utils.OrientationSensorInterface;
import com.twinscience.twin.lite.android.data.ProjectModel;
import com.twinscience.twin.lite.android.main.MainActivity;
import com.twinscience.twin.lite.android.project.data.ProjectEntity;
import com.twinscience.twin.lite.android.project.db.ProjectDao;
import com.twinscience.twin.lite.android.project.def.ProjectDef;
import com.twinscience.twin.lite.android.utils.GPSHelper;
import com.twinscience.twin.lite.android.viewmodel.ViewModelFactory;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by mertselcukdemir on 16.10.2018
 * Copyright (c) 2018 YGA to present
 * All rights reserved.
 */
public class BlocklyFragment extends AbstractBlocklyFragment implements ShakeDetector.Listener, OrientationSensorInterface {
    public static final String TAG = "BlocklyActivity";

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

    CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback;
    private AlertDialog dialogState;
    private WebView webView;
    private TwinWebInterface twinWebInterface;
    public ProjectModel selectedProject;
    private GPSHelper gpsHelper;
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    private String logProjectType;
    public long shakeTime;

    @Inject
    ViewModelFactory viewModelFactory;
    private BlocklyViewModel viewModel;

    @Inject
    ProjectDao projectDao;
    private Orientation orientationSensor;

    //Orientation
    public Double currentPitch;//rotation around x-axis (-180 to 180), with positive values when the z-axis moves toward the y-axis.
    public Double currentRoll;// Roll, rotation around the y-axis (-90 to 90) increasing as the device moves clockwise.
    public boolean isOrientationSupported;
    private Disposable dbSubscription;
    private TextView tvProjectName;
    private ImageButton btnPlay;
    private ImageButton btnStop;

    private MainActivity mainActivity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) context);
        mCodeGeneratorCallback = new LoggingCodeGeneratorCallback(mainActivity, TAG);
        TwinLiteApplication.Companion.getAppComponent(mainActivity).inject(this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(mainActivity, viewModelFactory).get(BlocklyViewModel.class);
        btnPlay = view.findViewById(R.id.btn_experiment_play);
        btnStop = view.findViewById(R.id.btn_experiment_stop);
        View btnSave = view.findViewById(R.id.btn_experiment_save);
        View btnBack = view.findViewById(R.id.unified_btn_back);
        webView = view.findViewById(R.id.blockly_mock_web_view);
        tvProjectName = view.findViewById(R.id.blockly_tv_project_name);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                switch (consoleMessage.message()) {
                    case "Uncaught Infinite loop.":
                        Log.d(TAG, "JS Exception: " + consoleMessage.message());
                        showAlertWithButton(BlocklyAlertState.JS_ERROR, getString(R.string.lbl_js_infinite_loop), getString(R.string.lbl_ok));
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

        btnBack.setOnClickListener(viewBack -> onBackPressed());
        if (getArguments() != null && getArguments().getParcelable("selectedProject") != null) {
            selectedProject = getArguments().getParcelable("selectedProject");
            if (selectedProject != null) {

                switch (selectedProject.getType()) {
                    case PERSONAL:
                        logProjectType = "my_projects";
                        btnSave.setVisibility(View.VISIBLE);
                        tvProjectName.setText(selectedProject.getTitle());
                        mBlocklyActivityHelper.loadWorkspaceFromAppDirSafely(getWorkspaceSavePath());
                        initFileNames(selectedProject.getId());
                        break;
                    case TWIN:
                        logProjectType = "examples";
                        btnSave.setVisibility(View.GONE);
                        tvProjectName.setText(selectedProject.getTitle());
                        try {
                            if (selectedProject.getFileName() != null) {
                                mBlocklyActivityHelper.mController.loadWorkspaceContents(mainActivity.getAssets().open(selectedProject.getFileName()));
                            }
                        } catch (BlockLoadingException | IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case NEW:
                        logProjectType = "new_project";
                        btnSave.setVisibility(View.VISIBLE);
                        break;

                }
                tvProjectName.setText(selectedProject.getTitle());
                mBlocklyActivityHelper.loadWorkspaceFromAppDirSafely(getWorkspaceSavePath());
                mCodeGeneratorCallback = generatedCode -> handler.post(() -> evalGeneratedCode(generatedCode));
            }
        }

        btnPlay.setOnClickListener(viewPlay -> {
            if (BleUtils.INSTANCE.isDefaultServicesEnabled(mainActivity)) {
                if (getController().getWorkspace().hasBlocks()) {
                    if (!twinWebInterface.isConnected) {
                        twinWebInterface.startScan(mainActivity, viewModel);
                        showAlertWithButton(BlocklyAlertState.SCANNING, getString(R.string.lbl_scanning_twin), getString(R.string.lbl_cancel));
                    } else {
                        showStopButton();
                        generateCode();
                    }
                } else {
                    Snackbar.make(view, getString(R.string.warn_no_blocks), Snackbar.LENGTH_SHORT).show();
                }
            }

        });

        btnStop.setOnClickListener(v -> {
            evalJSFunction("TwinJSSession.setStopStatus(true);");
            showPlayButton();
        });

        btnSave.setOnClickListener(viewSave -> {
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
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        shakeDetector.start(sensorManager);
        shakeDetector.setSensitivity(10);
        if (isOrientationSupported) {
            orientationSensor.on(1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        shakeDetector.stop();
        if (isOrientationSupported) {
            orientationSensor.off();
        }
    }

    private void showNameProjectDialog(boolean shouldEndActivity, boolean isUpdate) {
        View view = getLayoutInflater().inflate(R.layout.dialog_name_project, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity, R.style.DialogTheme);
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
            HashMap<String, String> saveProjectMap = new HashMap<String, String>() {{
                put(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(selectedProject.getId()));
                put(FirebaseLogUtils.CustomParam.ITEM_TITLE, selectedProject.getTitle());
            }};
            viewModel.logEvent(FirebaseLogUtils.CustomEvent.SAVE_PROJECT, saveProjectMap);
            if (shouldEndActivity) {
                goBack();
            } else {

                showAlertWithButton(BlocklyAlertState.SAVED, getString(R.string.lbl_saved_success), getString(R.string.lbl_ok));
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
                    mainActivity.runOnUiThread(() -> {
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
                    mainActivity.runOnUiThread(() -> {
                        selectedProject.setId(String.valueOf(id));
                        selectedProject.setType(ProjectDef.PERSONAL);
                        selectedProject.setTitle(title);
                        tvProjectName.setText(title);
                        saveWorkspaceSafely(shouldEndActivity);
                    });
                });
    }

    private void initFields() {
        sensorManager = (SensorManager) mainActivity.getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        orientationSensor = new Orientation(mainActivity.getApplicationContext(), this);
        isOrientationSupported = orientationSensor.isSupport();
        if (isOrientationSupported) {
            orientationSensor.init(1.0, 1.0, 1.0);
        }

        gpsHelper = new GPSHelper(mainActivity);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/twin_interpreter.html");
        twinWebInterface = new TwinWebInterface(this, mainActivity);
        twinWebInterface.initBleClient(mainActivity);
        webView.addJavascriptInterface(twinWebInterface, "Twin");
    }

    /**
     * Check bluetooth & gps services
     */
    private boolean isDefaultServicesEnabled() {
        boolean isBluetoothEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (!isBluetoothEnabled && !gpsHelper.isGPSenabled()) {
            showAlertWithButton(BlocklyAlertState.ENABLE_BLUETOOTH, getString(R.string.title_bluetooth_settings), getString(R.string.lbl_ok));
            return false;
        } else if (isBluetoothEnabled && !gpsHelper.isGPSenabled()) {
            showAlertWithButton(BlocklyAlertState.ENABLE_LOCATION, getString(R.string.title_location_settings), getString(R.string.lbl_ok));
            return false;
        } else if (!isBluetoothEnabled) {
            showAlertWithButton(BlocklyAlertState.ENABLE_BLUETOOTH, getString(R.string.title_bluetooth_settings), getString(R.string.lbl_ok));
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

    public void showPlayButton() {
        mainActivity.runOnUiThread(() -> {
            btnPlay.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.GONE);
        });
    }


    public void showStopButton() {
        mainActivity.runOnUiThread(() -> {
            btnPlay.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
        });
    }

    public void evalJSFunction(String code) {
        Log.d(TAG, "evalJSFunction - code: " + code);

        webView.evaluateJavascript(code, s -> {
            Log.d(TAG, "evalJSFunction: " + s == null ? "null" : s);
        });

    }

    /**
     * Code generation
     */
    void generateCode() {
        mBlocklyActivityHelper.requestCodeGeneration(
                getBlockGeneratorLanguage(),
                getBlockDefinitionsJsonPaths(),
                getGeneratorsJsPaths(),
                getCodeGenerationCallback());
    }


    /**
     * @param title is Dialog Title
     * @param label is Button Label
     */
    public void showAlertWithButton(BlocklyAlertState state, String title, String label) {
        View view = getLayoutInflater().inflate(R.layout.dialog_with_button, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity, R.style.DialogTheme);
        alertDialogBuilder.setView(view);
        AlertDialog dialog = null;
        if (state == BlocklyAlertState.SCANNING || state == BlocklyAlertState.TIMEOUT) {
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
                    dismissDialogState();
                    twinWebInterface.disposeScanSubscription(true);
                    viewModel.logEvent(FirebaseLogUtils.CustomEvent.CONNECTION, new HashMap<String, String>() {{
                        put(FirebaseLogUtils.CustomParam.STATUS, FirebaseLogUtils.ConnectionStatus.CANCELED);
                        put(FirebaseLogUtils.CustomParam.SCAN_START_TIME, String.valueOf(twinWebInterface.scanStartTime));
                    }});
                case TIMEOUT:
                    dialogState.dismiss();
                    break;
                case CONNECTED:
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
            if (state != BlocklyAlertState.SCANNING && state != BlocklyAlertState.SCAN_ERROR && state != BlocklyAlertState.TIMEOUT && state != BlocklyAlertState.JS_ERROR && finalDialog != null && finalDialog.isShowing() && !mainActivity.isFinishing()) {
                dismissDialogState();
                finalDialog.dismiss();
            }
            handler.removeCallbacksAndMessages(null);
        }, 1000);
    }

    private void showAlertWithTwoButtons(String title, String lblLeft, String lblRight) {
        View view = getLayoutInflater().inflate(R.layout.dialog_with_two_buttons, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity, R.style.DialogTheme);
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

    public void dismissDialogState() {
        if (dialogState != null && dialogState.isShowing()) {
            dialogState.dismiss();
        }
    }


    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        List<String> definitions = new ArrayList<>(BLOCK_DEFINITIONS_TR);
        definitions.add("twin_tr.json");
        return definitions;
    }

    private void goBack() {
        if (twinWebInterface.isConnected) {
            viewModel.logEvent(FirebaseLogUtils.CustomEvent.CONNECTION, new HashMap<String, String>() {{
                put(FirebaseLogUtils.CustomParam.STATUS, FirebaseLogUtils.ConnectionStatus.DISCONNECTED);
                put(FirebaseLogUtils.CustomParam.SCAN_START_TIME, String.valueOf(twinWebInterface.scanStartTime));
            }});
        }

        viewModel.logEvent(FirebaseLogUtils.CustomEvent.SELECT_CONTENT, new HashMap<String, String>() {
            {
                put(FirebaseLogUtils.CustomParam.CONTENT_CATEGORY, FirebaseLogUtils.ContentCategory.CODING);
                put(FirebaseLogUtils.CustomParam.CONTENT_TYPE, FirebaseLogUtils.ContentType.MAIN);
            }
        });

        if (device != null) {
            BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getMacAddress());
        }

        disposeItems();
        super.onBackPressed();
    }


    private void disposeItems() {
        if (dbSubscription != null && !dbSubscription.isDisposed()) {
            dbSubscription.dispose();
        }
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        return DefaultBlocks.TOOLBOX_TR_PATH;
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
        if (mCodeGeneratorCallback == null) {
            // Late initialization since Context is not available at construction time.
            mCodeGeneratorCallback = new LoggingCodeGeneratorCallback(getContext(), TAG);
        }
        return mCodeGeneratorCallback;
    }

    /**
     * Optional override of the save path, since this demo Activity has multiple Blockly
     * configurations.
     *
     * @return Workspace save path used by SimpleActivity and SimpleFragment.
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
     * @return Workspace auto-save path used by SimpleActivity and SimpleFragment.
     */
    @Override
    @NonNull
    protected String getWorkspaceAutosavePath() {
        return autoSaveFileName;
    }

    public static BlocklyFragment newInstance(ProjectModel projectModel) {

        Bundle args = new Bundle();
        args.putParcelable("selectedProject", projectModel);

        BlocklyFragment fragment = new BlocklyFragment();
        fragment.setArguments(args);
        return fragment;
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
    public void onDestroyView() {
        if (dialogState != null) {
            dialogState.cancel();
        }
        super.onDestroyView();
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

    }
}
