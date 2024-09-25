package com.sdk.dynaflexcardreaddemo.utils

import android.content.Context
import android.widget.Toast
import com.magtek.mobile.android.mtusdk.IDevice

interface OnDeviceSelectListener {
    fun onDeviceSelect(selectedDevice: IDevice)
}


fun Context.showToast(msg: String) {
    if(msg != null){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}