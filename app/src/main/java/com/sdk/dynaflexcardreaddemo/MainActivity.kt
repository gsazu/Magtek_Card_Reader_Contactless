package com.sdk.dynaflexcardreaddemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.magtek.mobile.android.mtusdk.ConnectionState
import com.magtek.mobile.android.mtusdk.ConnectionStateBuilder
import com.magtek.mobile.android.mtusdk.CoreAPI
import com.magtek.mobile.android.mtusdk.DeviceType
import com.magtek.mobile.android.mtusdk.EventType
import com.magtek.mobile.android.mtusdk.IData
import com.magtek.mobile.android.mtusdk.IDevice
import com.magtek.mobile.android.mtusdk.IDeviceControl
import com.magtek.mobile.android.mtusdk.IDeviceListCallback
import com.magtek.mobile.android.mtusdk.IEventSubscriber
import com.magtek.mobile.android.mtusdk.Transaction
import com.magtek.mobile.android.mtusdk.TransactionBuilder
import com.sdk.dynaflexcardreaddemo.adapter.DevicesAdapter
import com.sdk.dynaflexcardreaddemo.databinding.ActivityMainBinding
import com.sdk.dynaflexcardreaddemo.utils.OnDeviceSelectListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityMainBinding
    private lateinit var devicesAdapter: DevicesAdapter
    private var emptyList: ArrayList<IDevice> = ArrayList()
    private var mDevice:IDevice? = null
    private val TAG = "MainActivity"
    private var device: IDevice? = null

    companion object {
        var textLog = "Welcome"
        var tvLog:TextView? = null

        fun setLogsData(text: String){
            CoroutineScope(Dispatchers.Main.immediate).launch {
                try {
                    textLog += "\n${text}"
                    tvLog!!.text = textLog
                } catch (e: Exception){}
            }
        }
    }

    private val selectDeviceListener = object: OnDeviceSelectListener {
        override fun onDeviceSelect(selectedDevice: IDevice) {
            device = selectedDevice
            setDevice(device!!)
            setLogsData(device!!.Name() +" selected")
            binding.btnConnect.isEnabled = true
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tvLog = binding.tvLogs
        devicesAdapter = DevicesAdapter(this,emptyList,selectDeviceListener)
        binding.rvDevices.adapter = devicesAdapter

        init()
    }

    private fun init(){
        getDevices()

        binding.btnRefresh.setOnClickListener(this)
        binding.btnConnect.setOnClickListener(this)
        binding.btnReadData.setOnClickListener(this)
        binding.btnClearData.setOnClickListener(this)
    }

    fun getDevices(){
        val deviceList: List<IDevice> = CoreAPI.getDeviceList(
            this.applicationContext, DeviceType.MMS
        ) { p0 ->
            Log.d("devices", "getDevices: $p0")
            updateDevices(p0!!)
        }
        Log.d("devices", "getDevices: $deviceList")
        updateDevices(deviceList)
    }

    fun updateDevices(updatedDevices: List<IDevice>) {
        devicesAdapter.data = updatedDevices
        devicesAdapter.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btn_refresh -> {
                getDevices()
            }

            R.id.btn_clearData -> {
                textLog = ""
                binding.tvLogs.text = ""
            }

            R.id.btn_connect -> {
                if(getDevice().connectionState == ConnectionState.Disconnected) {
                    connectDevice()
                } else {
                    disconnectDevice()
                    binding.btnConnect.setText("Connect")
                    setLogsData("Device ${getDevice().connectionState}")
                    binding.btnReadData.isEnabled = false
                }
            }

            R.id.btn_readData -> {
                val timeout: Byte = 45
                val transactionType: Byte = 0
                val paymentMethods =
                    TransactionBuilder.GetPaymentMethods(false, false, true, false)

                val transaction = Transaction(
                    timeout,
                    paymentMethods,
                    "",
                    "",
                    false,
                    false,
                    transactionType
                )

                transaction.setPreventMSRSignatureForCardWithICC(true)

                if (getDevice().startTransaction(transaction)) {
                    setLogsData("[Transaction Started]")
                }
            }
        }
    }

    fun connectDevice() {
        try{
            subscribeAll()
            val deviceControl = getDeviceControl()
            if(deviceControl != null){
                deviceControl.open()
            }
        } catch (e: Exception){

        }
    }

    fun setDevice(device: IDevice){
        this.mDevice = device
    }

    fun getDevice(): IDevice {
        return mDevice!!
    }


    private fun subscribeAll(){
        if(mDevice != null){
            mDevice!!.unsubscribeAll(deviceListener)
            mDevice!!.subscribeAll(deviceListener)
        }
    }
    
    val deviceListener = object : IEventSubscriber{
        override fun OnEvent(p0: EventType?, p1: IData?) {
            Log.d(TAG, "OnEvent: $p0")
            Log.d(TAG, "OnEventData: ${p1!!.StringValue()}")
            setLogsData("[$p0]")
            setLogsData(p1!!.StringValue())

            when(p0!!){
                EventType.ConnectionState -> {
                    setLogsData("Connecting device")
                    Log.d(TAG, "Hello: ${mDevice!!.connectionState}")
                }

                EventType.TransactionStatus -> {
                    if (p1!!.StringValue().contains("card_detected")) {
                        Log.d(TAG, "OnEvent: RokoRoko")
                        getDevice().cancelTransaction()
                    }
                }
                else -> {}
            }

            if(p1 != null) {
                checkDeviceConnectionSate(p1!!)
            }
        }

    }

    fun getDeviceControl(): IDeviceControl {
        return mDevice!!.deviceControl
    }

    fun disconnectDevice() {
        try {
            val deviceControl = getDeviceControl()
            if (deviceControl != null) {
                deviceControl.close()
            }
        } catch (ex: java.lang.Exception) {
        }
    }

    fun checkDeviceConnectionSate(data: IData){
        val value = ConnectionStateBuilder.GetValue(data.StringValue())
        if (value == ConnectionState.Connected) {
            runOnUiThread {
                binding.btnConnect.setText("Disconnect")
                setLogsData("Device ${getDevice().connectionState}")
                binding.btnReadData.isEnabled = true
            }
        } else if (value == ConnectionState.Disconnected) {
            runOnUiThread {
                binding.btnConnect.setText("Connect")
                setLogsData("Device ${getDevice().connectionState}")
                binding.btnReadData.isEnabled = false
            }
        } else if (value == ConnectionState.Disconnecting) {
            setLogsData("Disconnecting Device")
        } else if (value == ConnectionState.Connecting) {
            setLogsData("Connecting Device")
        }
    }
}