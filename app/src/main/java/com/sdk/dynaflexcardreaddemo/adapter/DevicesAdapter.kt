package com.sdk.dynaflexcardreaddemo.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.magtek.mobile.android.mtusdk.IDevice
import com.sdk.dynaflexcardreaddemo.databinding.ItemDeviceBinding
import com.sdk.dynaflexcardreaddemo.utils.OnDeviceSelectListener
import com.sdk.dynaflexcardreaddemo.utils.showToast

class DevicesAdapter(val context: Context, var data: List<IDevice>, val listener: OnDeviceSelectListener) : RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesAdapter.ViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DevicesAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(singleData: IDevice) {
            binding.apply {
                tvTitle.text = singleData.Name()
                tvSubtitle.text = singleData.connectionInfo.address

                root.setOnClickListener {
                    listener.onDeviceSelect(singleData)
                    context.showToast("Device Selected")
                }
            }
        }
    }
}