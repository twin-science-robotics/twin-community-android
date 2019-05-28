package com.twinscience.twin.lite.android.blockly;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.twinscience.twin.lite.android.R;
import com.twinscience.twin.lite.android.main.MainActivity;
import com.twinscience.twin.lite.android.utils.MathUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TwinWebInterface {
    private static final String TAG = TwinWebInterface.class.getSimpleName();
    private static final UUID WRITE_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private final BlocklyFragment fragment;
    private final MainActivity activity;

    private String deviceName = "Twin";
    private boolean isFirstShake = true;
    private byte[] bytes = null;
    private RxBleClient rxBleClient;
    private RxBleDevice device;
    private RxBleConnection rxBleConnection;
    private Disposable scanSubscription;
    private Disposable listenSubscription;
    private Disposable sendSubscription;
    private Disposable connectionSubscription;
    private long updatedTime;
    private Boolean isAnalogFlag = false;
    public boolean isConnected;
    public boolean isDeviceFound;
    public long scanStartTime;
    private int TIME_OUT_LIMIT_SECONDS = 3;
    private HashMap<RxBleDevice, int[]> scannedDeviceMap;

    /**
     * Instantiate the interface and set the context
     */
    public TwinWebInterface(BlocklyFragment fragment, MainActivity activity) {
        this.fragment = fragment;
        this.activity = activity;
        this.scannedDeviceMap = new HashMap<>();
    }


    @JavascriptInterface
    public void sendMessage(String message) {
        Log.d("TwinWebInterface", "Twin sendMessage: " + message);
        sendCode(message, false);

        // Dirty hack for buzzer bug. We need to wait until playing completion
        if (message.startsWith("AA-44-1C-0F-02")) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @JavascriptInterface
    public String getMessage(String message, String isAnalog) {

        Log.d("TwinWebInterface", "Twin getMessage: " + message);

        this.isAnalogFlag = isAnalog.equals("1");

        Log.d("TwinWebInterface", "Twin isAnalogFlag: " + this.isAnalogFlag);

        activity.runOnUiThread(() -> fragment.evalJSFunction("TwinJSSession.pause();"));
        listenBLE();
        sendCode(message, true);

        return "OK";
    }

    @JavascriptInterface
    public boolean getDeviceShakeStatus() {
        int buffer;
        if (isFirstShake) {
            isFirstShake = false;
            buffer = 10;
        } else {
            buffer = 1;
        }
        try {
            Thread.sleep(100);
            long difference = TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTimeInMillis() - fragment.shakeTime);
            Log.d("TwinWebInterface", "getDeviceShakeStatus: " + (difference < buffer));
            return difference < buffer;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }


    }

    @JavascriptInterface
    public boolean getDeviceOrientation(String orientation) {
        if (!fragment.isOrientationSupported) {
            Toast.makeText(activity, R.string.warn_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switch (orientation.toLowerCase()) {
            case "up":
                return -135 < fragment.currentRoll && fragment.currentRoll < -45;
            case "right":
                return -90 < fragment.currentPitch && fragment.currentPitch < -30;
            case "down":
                return 45 < fragment.currentRoll && fragment.currentRoll < 135;
            case "left":
                return 30 < fragment.currentPitch && fragment.currentPitch < 90;
        }
        return false;
    }

    @JavascriptInterface
    public void sleep(String duration) {
        Log.d("TwinWebInterface", "Twin sleep: " + duration);

        try {
            Thread.sleep(1000 * Integer.parseInt(duration));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*transferData(message);*/
    }


    public void setRxBleConnection(RxBleConnection rxBleConnection) {
        this.rxBleConnection = rxBleConnection;
    }


    public void sendCode(String message, boolean shouldListenBle) {

        if (rxBleConnection == null) {
            return;
        }

        byte[] bytesToWrite = new BigInteger(message.replace("-", ""), 16).toByteArray();

        // a kilobyte array
        sendSubscription = rxBleConnection.writeCharacteristic(WRITE_CHARACTERISTIC, bytesToWrite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    // Log.d("TwinWebInterface", "sendCode: " + Arrays.toString(bytes));
                    if (shouldListenBle) {
                        // listenBLE("FromGet Message");
                    }
                }, Throwable::printStackTrace);
    }


    /**
     * ConnectionMethods
     */


    void initBleClient(MainActivity mainActivity) {
        rxBleClient = RxBleClient.create(mainActivity);
    }

    /**
     * Scanning Methods
     * Scans for 2 seconds and connects to the nearest device containing the name "Twin"
     */
    void startScan(MainActivity activity, BlocklyViewModel viewModel) {
        scanStartTime = Calendar.getInstance().getTimeInMillis() / 1000;
        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder().build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        scanResult -> {
                            long updatedTime = Calendar.getInstance().getTimeInMillis() / 1000;
                            Log.d("RSSI CHECK", "timeDiffFirst: " + (Calendar.getInstance().getTimeInMillis() - scanStartTime));
                            RxBleDevice bleDevice = scanResult.getBleDevice();
                            String name = bleDevice.getName();
                            Log.e(TAG, name == null ? "null" : name);
                            //Return ignored results
                            if (isDeviceFound) {
                                return;
                            }
                            Log.d("RSSI CHECK", "timeDiff: " + (updatedTime - scanStartTime));
                            if ((((updatedTime - scanStartTime)) < TIME_OUT_LIMIT_SECONDS) && name != null && name.contains(deviceName)) {
                                collectScannedDevices(scanResult, bleDevice);
                            } else if (!scannedDeviceMap.isEmpty()) {

                                Log.d("RSSI CHECK", "scannedDeviceMap: " + scannedDeviceMap.toString());
                                device = getNearestDevice();
                                isDeviceFound = true;
                                connectToDevice(viewModel);
                            } else if ((((updatedTime - scanStartTime)) > TIME_OUT_LIMIT_SECONDS)) {
                                disposeScanSubscription(true);
                            }
                        },
                        throwable -> {
                            if (throwable.getMessage().equals("Scan failed because application registration failed (code 6)")) {
                                disposeScanSubscription(true);

                            }
                            throwable.printStackTrace();
                        }
                );


    }

    private void connectToDevice(BlocklyViewModel viewModel) {
        // <-- autoConnect flag
        connectionSubscription = device.establishConnection(false)
                .observeOn(AndroidSchedulers.mainThread())// <-- autoConnect flag
                .subscribe(
                        rxBleConnection -> {
                            fragment.showAlertWithButton(BleState.CONNECTED, activity.getString(R.string.lbl_success_connection), activity.getString(R.string.lbl_ok));
                            fragment.dismissDialogState();
                            setRxBleConnection(rxBleConnection);
                            isConnected = true;
                            fragment.showStopButton();
                            fragment.generateCode();
                            fragment.dismissDialogState();
                            disposeScanSubscription(false);

                        },
                        throwable -> {
                            if (throwable.getMessage().toLowerCase().contains("disconnect")) {
                                fragment.showAlertWithButton(BleState.DISCONNECTED, activity.getString(R.string.lbl_twin_disconnected), getString(R.string.lbl_ok));
                                fragment.dismissDialogState();
                                disposeScanSubscription(true);
                                fragment.showPlayButton();
                            }

                        });
    }

    /**
     * This method will communicate with Twin BLE device. IF device sends any data it will be shown here.
     */
    public void listenBLE() {
        if (rxBleConnection == null) {
            return;
        }

        listenSubscription = rxBleConnection.setupNotification(WRITE_CHARACTERISTIC)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(observable -> {

                })
                .flatMap(observable -> observable)
                .subscribe(bytes -> {

                    long currentTime = Calendar.getInstance().getTimeInMillis();

                    // Log.d("TwinWebInterface", "listenDifference: " + String.valueOf(currentTime - updatedTime));


                    //Filter notification results
                    if (this.bytes == null || (this.bytes != bytes && ((currentTime - updatedTime)) > 50)) {
                        this.bytes = bytes;
                        updatedTime = currentTime;
                    } else {
                        return;
                    }

                    Log.d("TwinWebInterface", "isAnalog: " + this.isAnalogFlag);

                    int msb = (int) bytes[bytes.length - 1] & 0xFF;
                    int lsb = 0;

                    if (this.isAnalogFlag) {
                        lsb = (int) bytes[bytes.length - 2] & 0xFF;
                    }

                    msb = msb << 8;

                    int result = msb + lsb;

                    Log.d("TwinWebInterface ", "listenNotification: " + Arrays.toString(bytes));

                    fragment.evalJSFunction("TwinJSSession.resume('" + result + "');");
                }, throwable -> Log.d("TwinWebInterface", throwable.toString()));


    }

    private void setScanEnable() {
        isConnected = false;
        isDeviceFound = false;
    }

    /**
     * Device Collections
     */
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

    /**
     * Subscription Dispose Methods
     */

    public void disposeScanSubscription(boolean isScanEnabled) {
        if (!scanSubscription.isDisposed()) {
            scanSubscription.dispose();
        }
        scannedDeviceMap.clear();
        if (isScanEnabled) {
            setScanEnable();
        }
    }


    void disposeItems() {
        if (sendSubscription != null && !sendSubscription.isDisposed()) {
            sendSubscription.dispose();
        }
        if (listenSubscription != null && !listenSubscription.isDisposed()) {
            listenSubscription.dispose();
        }
        if (scanSubscription != null && !scanSubscription.isDisposed()) {
            scanSubscription.dispose();
        }
        if (connectionSubscription != null && !connectionSubscription.isDisposed()) {
            connectionSubscription.dispose();
        }
    }
}