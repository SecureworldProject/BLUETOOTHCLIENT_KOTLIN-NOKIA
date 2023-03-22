package com.example.bluetoothclient

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

// custom View components/ custom view class/ Subclass a View
class RecordButton(
    context:Context,
    attrs: AttributeSet
): AppCompatImageButton(context, attrs) {

    init {
        setBackgroundResource(R.drawable.shape_oval_buttom)
    }

    // Creating interfaces where icons change
    fun updateIconWithState(state: State){
        when (state){
            State.BEFORE_RECORDING-> {
                setImageResource(R.drawable.ic_record)
            }
            State.ON_RECORDING -> { setImageResource(R.drawable.ic_stop)}
            State.AFTER_RECORDING -> {setImageResource(R.drawable.ic_play)}
            State.ON_PLAYING -> {setImageResource(R.drawable.ic_stop)}
        }
    }
}