package com.twinscience.twin.lite.android.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.twinscience.twin.lite.android.R
import com.twinscience.twin.lite.android.utils.ScreenUtils


/**
 * Created by mertselcukdemir on 10.10.2018
 * Copyright (c) 2018 YGA to present
 * All rights reserved.
 */
class RemoveDialogFragment : DialogFragment() {
    private var content: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = arguments?.getString("content")

        val style = DialogFragment.STYLE_NO_FRAME
        val theme = R.style.DialogTheme
        setStyle(style, theme)

    }

    var onResult: ((isRemoved: Boolean) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_remove_project, container, false)

        val btnYes = view.findViewById<CardView>(R.id.dialog_remove_tv_yes)
        val btnNo = view.findViewById<CardView>(R.id.dialog_remove_tv_no)

        btnYes.setOnClickListener {
            onResult?.invoke(true)
            ScreenUtils.setFullScreen(activity as Activity)
            dismiss()
        }

        btnNo.setOnClickListener {
            onResult?.invoke(false)
            ScreenUtils.setFullScreen(activity as Activity)
            dismiss()
        }

        return view
    }

    companion object {


        /**
         * Create a new instance of CustomDialogFragment, providing "num" as an
         * argument.
         */
        fun newInstance(content: String): RemoveDialogFragment {
            val f = RemoveDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putString("content", content)
            f.arguments = args

            return f
        }
    }

}