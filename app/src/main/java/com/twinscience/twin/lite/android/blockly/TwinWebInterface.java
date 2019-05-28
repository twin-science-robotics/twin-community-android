package com.twinscience.twin.lite.android.blockly;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleConnection;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.twinscience.twin.lite.android.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TwinWebInterface {
    private BlocklyActivity activity;
    private static final UUID WRITE_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private RxBleConnection rxBleConnection;
    private boolean isFirstShake = true;
    private byte[] bytes = null;
    private Disposable listenSubscription;
    private Disposable sendSubscription;
    private long updatedTime;
    private Boolean isAnalogFlag = false;

    /**
     * Instantiate the interface and set the context
     */
    public TwinWebInterface(BlocklyActivity c) {
        activity = c;
    }


    @JavascriptInterface
    public void sendMessage(String message) {
        Log.d("TwinWebInterface", "Twin sendMessage: " + message);
        sendCode(message, false);

        // Dirty hack for buzzer bug. We need to wait until playing completion
        if(message.startsWith("AA-44-1C-0F-02")) {
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

        activity.runOnUiThread(() -> activity.evalJSFunction("TwinJSSession.pause();"));
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
            long difference = TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().getTimeInMillis() - activity.shakeTime);
            Log.d("TwinWebInterface", "getDeviceShakeStatus: " +  (difference < buffer) );
            return difference < buffer;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }


    }

    @JavascriptInterface
    public boolean getDeviceOrientation(String orientation) {
        if (!activity.isOrientationSupported) {
            return false;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        switch (orientation.toLowerCase()) {
            case "up":
                return -135 < activity.currentRoll && activity.currentRoll < -45;
            case "right":
                return -90 < activity.currentPitch && activity.currentPitch < -30;
            case "down":
                return 45 < activity.currentRoll && activity.currentRoll < 135;
            case "left":
                return 30 < activity.currentPitch && activity.currentPitch < 90;
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
     * This method will communicate with Twin BLE device. IF device sends any data it will be shown here.
     */
    public void listenBLE() {
        if (rxBleConnection == null) {
            return;
        }

        listenSubscription = rxBleConnection.setupNotification(WRITE_CHARACTERISTIC)
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

                    activity.runOnUiThread(() -> activity.evalJSFunction("TwinJSSession.resume('" + String.valueOf(result) + "');"));


                    /**/
                }, throwable -> {
                    Log.d("TwinWebInterface", throwable.toString());
                });


    }

    public void disposeItems() {
        if (sendSubscription != null && !sendSubscription.isDisposed()) {
            sendSubscription.dispose();
        }
        if (listenSubscription != null && !listenSubscription.isDisposed()) {
            listenSubscription.dispose();
        }
    }
}