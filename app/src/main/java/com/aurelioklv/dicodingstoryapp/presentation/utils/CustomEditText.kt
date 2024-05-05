package com.aurelioklv.dicodingstoryapp.presentation.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.aurelioklv.dicodingstoryapp.R

class CustomEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatEditText(context, attrs), View.OnTouchListener {

    private var togglePasswordIcon: Drawable
    private var startIcon: Drawable
    private var isPasswordVisible = false

    init {
        transformationMethod =
            if (!isPasswordVisible) PasswordTransformationMethod.getInstance() else null
        togglePasswordIcon = ContextCompat.getDrawable(
            context,
            if (isPasswordVisible) R.drawable.view else R.drawable.hidden
        ) as Drawable
        startIcon = ContextCompat.getDrawable(context, R.drawable.lock) as Drawable

        setButtonDrawables(startOfTheText = startIcon, endOfTheText = togglePasswordIcon)
        setOnTouchListener(this)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    setError(context.getString(R.string.password_minimum_length), null)
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if ((compoundDrawables[2] != null && layoutDirection == View.LAYOUT_DIRECTION_LTR) ||
            (compoundDrawables[0] != null && layoutDirection == View.LAYOUT_DIRECTION_RTL)
        ) {
            val toggleButtonStart: Float
            val toggleButtonEnd: Float
            var isToggleButtonClicked = false

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                toggleButtonEnd = (togglePasswordIcon.intrinsicWidth + paddingStart).toFloat()
                when {
                    event.x < toggleButtonEnd -> {
                        isToggleButtonClicked = true
                    }
                }
            } else {
                toggleButtonStart =
                    (width - paddingEnd - togglePasswordIcon.intrinsicWidth).toFloat()
                when {
                    event.x > toggleButtonStart -> isToggleButtonClicked = true
                }
            }
            if (isToggleButtonClicked) {
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        isPasswordVisible = !isPasswordVisible
                        togglePasswordIcon = ContextCompat.getDrawable(
                            context,
                            if (isPasswordVisible) R.drawable.view else R.drawable.hidden
                        ) as Drawable
                        transformationMethod =
                            if (!isPasswordVisible) PasswordTransformationMethod.getInstance() else null
                        setButtonDrawables(
                            startOfTheText = startIcon,
                            endOfTheText = togglePasswordIcon
                        )
                    }

                    else -> return false
                }
            } else return false
        }
        return false
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null,
    ) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }
}