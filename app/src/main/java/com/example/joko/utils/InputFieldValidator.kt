package com.example.joko.utils

import android.content.res.ColorStateList
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.joko.R

class InputFieldValidator {
    companion object {
        fun validateField(
            isInvalid: Boolean,
            inputView: View,
            errorMessage: String,
            errorView: TextView? = null
        ): Boolean {
            val context = inputView.context

            // 1. Handle Error Message Display
            if (errorView != null) {
                // Scenario A
                errorView.text = if (isInvalid) errorMessage else ""
                errorView.visibility = if (isInvalid) View.VISIBLE else View.GONE
                if (isInvalid) {
                    errorView.setTextColor(ContextCompat.getColor(context, R.color.text_alert))
                }
            } else {
                // Scenario B
                val parentLayout = inputView.parent as? LinearLayout
                if (parentLayout != null) {
                    if (parentLayout.orientation == LinearLayout.HORIZONTAL) {
                        handleHorizontalParent(isInvalid, inputView, parentLayout, errorMessage)
                    } else {
                        handleVerticalParent(isInvalid, inputView, parentLayout, errorMessage)
                    }
                }
            }

            // 2. Handle Input Box Highlighting (Tint)
            val colorStateList = if (isInvalid) {
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_alert))
            } else {
                null
            }

            ViewCompat.setBackgroundTintList(inputView, colorStateList)

            (inputView.parent as? LinearLayout)?.let {
                if (it.orientation == LinearLayout.HORIZONTAL) {
                    ViewCompat.setBackgroundTintList(it, colorStateList)
                }
            }

            return !isInvalid
        }

        fun validateRequiredField(
            value: String,
            inputView: View,
            errorMessage: String = "Field ini wajib diisi",
            isDate: Boolean = false,
            errorView: TextView? = null
        ): Boolean {
            val isInvalid = if (isDate) {
                value.isEmpty() || value == "mm/dd/yyyy"
            } else {
                value.trim().isEmpty()
            }
            return validateField(isInvalid, inputView, errorMessage, errorView)
        }

        private fun handleVerticalParent(isInvalid: Boolean, inputView: View, parent: LinearLayout, msg: String) {
            val errorViewId = inputView.id + 10000
            var errorTv = parent.findViewById<TextView>(errorViewId)
            if (isInvalid) {
                if (errorTv == null) {
                    errorTv = createErrorTextView(inputView, errorViewId, msg, 8)
                    val index = parent.indexOfChild(inputView)
                    parent.addView(errorTv, index + 1)
                } else {
                    errorTv.text = msg
                    errorTv.visibility = View.VISIBLE
                }
            } else if (errorTv != null) {
                parent.removeView(errorTv)
            }
        }

        private fun handleHorizontalParent(isInvalid: Boolean, inputView: View, parent: LinearLayout, msg: String) {
            val grandParent = parent.parent as? LinearLayout ?: return
            val errorViewId = inputView.id + 20000
            var errorTv = grandParent.findViewById<TextView>(errorViewId)
            if (isInvalid) {
                if (errorTv == null) {
                    errorTv = createErrorTextView(inputView, errorViewId, msg, 4)
                    val index = grandParent.indexOfChild(parent)
                    grandParent.addView(errorTv, index + 1)
                } else {
                    errorTv.text = msg
                    errorTv.visibility = View.VISIBLE
                }
            } else if (errorTv != null) {
                grandParent.removeView(errorTv)
            }
        }

        private fun createErrorTextView(anchor: View, viewId: Int, msg: String, topPadding: Int): TextView {
            return TextView(anchor.context).apply {
                id = viewId
                text = msg
                setTextColor(ContextCompat.getColor(context, R.color.text_alert))
                textSize = 12f
                setPadding(4, topPadding, 4, 8)
            }
        }
    }
}
