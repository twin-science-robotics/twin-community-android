package com.twinscience.twin.lite.android.dialog.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.twinscience.twin.lite.android.R;
import com.twinscience.twin.lite.android.utils.ScreenUtils;

/**
 * Created by mertselcukdemir on 14.04.2019
 * Copyright (c) 2019 YGA to present
 * All rights reserved.
 */
public class DialogUtils {

    private AlertDialog alertDialog;

    public static void showAlertWithButton(Activity activity, String title, String label, Drawable drawableTop, boolean isAutoDismiss, @Nullable OneButtonCallBack callBack) {

        activity.runOnUiThread(() -> {
            View view = activity.getLayoutInflater().inflate(R.layout.dialog_with_button, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.DialogTheme);
            alertDialogBuilder.setView(view);
            AlertDialog alertDialog = alertDialogBuilder.show();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();

            TextView tvTitle = view.findViewById(R.id.dialog_with_button_tv_title);
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null);
            TextView tvAction = view.findViewById(R.id.dialog_with_button_tv_action);
            tvTitle.setText(title);
            tvAction.setText(label);

            CardView cardOk = view.findViewById(R.id.dialog_with_button_root_cancel);
            cardOk.setOnClickListener(v -> {
                alertDialog.dismiss();
                if (callBack != null) {
                    callBack.onButtonClicked();
                    ScreenUtils.INSTANCE.setFullScreen(activity);
                }
            });

            if (isAutoDismiss) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    handler.removeCallbacksAndMessages(null);
                }, 1000);

            }
            ScreenUtils.INSTANCE.setFullScreen(activity);
        });
    }


    public static void showAlertWithTwoButtons(Activity activity, String title, String lblPositive, String lblNegative, TwoButtonsCallBack callBack) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_with_two_buttons, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.DialogTheme);
        alertDialogBuilder.setView(view);
        TextView tvTitle = view.findViewById(R.id.dialog_with_two_button_tv_title);
        TextView tvActionLeft = view.findViewById(R.id.dialog_with_two_buttons_tv_left);
        TextView tvActionRight = view.findViewById(R.id.dialog_with_two_buttons_tv_right);
        CardView rootActionLeft = view.findViewById(R.id.dialog_with_two_buttons_root_left);
        CardView rootActionRight = view.findViewById(R.id.dialog_with_two_buttons_root_right);

        tvTitle.setText(title);
        tvActionLeft.setText(lblPositive);
        tvActionRight.setText(lblNegative);

        AlertDialog alertDialog = alertDialogBuilder.show();

        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        rootActionLeft.setOnClickListener(viewLeft -> {
            alertDialog.dismiss();
            callBack.onPositiveClicked();
        });

        rootActionRight.setOnClickListener(viewRight -> {
            alertDialog.dismiss();
            callBack.onNegativeClicked();
        });
    }


    /**
     * Specified for BLE
     */
    public interface TwoButtonsCallBack {

        void onPositiveClicked();

        void onNegativeClicked();
    }

    public interface OneButtonCallBack {
        void onButtonClicked();
    }
}
