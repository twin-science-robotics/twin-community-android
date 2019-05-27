package com.twinscience.twin.lite.android.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.twinscience.twin.lite.android.R

/**
 * Created by mertselcukdemir on 22.11.2018
 * Copyright (c) 2018 YGA to present
 * All rights reserved.
 */
class SettingsDialogFragment : DialogFragment() {
    private var content: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = arguments?.getString("content")

        val style = DialogFragment.STYLE_NO_FRAME
        val theme = R.style.DialogTheme
        setStyle(style, theme)

    }

    var onResult: ((isSettingsSelected: Boolean) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_settings, container, false)


        val tvTitle = view.findViewById<TextView>(R.id.dialog_settings_tv_title)
        val btnShow = view.findViewById<CardView>(R.id.dialog_settings_card_open)
        val btnDismiss = view.findViewById<View>(R.id.dialog_settings_card_dismiss)


        content.let {
            when (it) {
                "Bluetooth" -> {
                    tvTitle.text = tvTitle.context.getString(R.string.title_bluetooth_settings)
                }
                "Location" -> {
                    tvTitle.text = tvTitle.context.getString(R.string.title_location_settings)
                }
            }
        }

        btnShow.setOnClickListener {
            onResult?.invoke(true)
            dismiss()
        }

        btnDismiss.setOnClickListener {
            onResult?.invoke(false)
            dismiss()
        }

        return view
    }

    companion object {
        val TAG: String = this::class.java.simpleName

        /**
         * Create a new instance of CustomDialogFragment, providing "num" as an
         * argument.
         */
        fun newInstance(content: String): SettingsDialogFragment {
            val f = SettingsDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putString("content", content)
            f.arguments = args

            return f
        }
    }
}
