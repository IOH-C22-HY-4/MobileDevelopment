package com.ioh_c22_h2_4.hy_ponics.ui.iot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.data.sensor.SensorData
import com.ioh_c22_h2_4.hy_ponics.databinding.ItemRowParameterBinding

class ParameterAdapter : ListAdapter<SensorData, ParameterViewHolder>(Comparator) {

    object Comparator : DiffUtil.ItemCallback<SensorData>() {
        override fun areItemsTheSame(oldItem: SensorData, newItem: SensorData): Boolean =
            oldItem == newItem


        override fun areContentsTheSame(oldItem: SensorData, newItem: SensorData): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParameterViewHolder =
        ParameterViewHolder(
            ItemRowParameterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ParameterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ParameterViewHolder(
    private val binding: ItemRowParameterBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(sensorData: SensorData) {
        binding.apply {
            tittleParameter.text = sensorData.name
            detailParameter.text = sensorData.name
            Glide.with(imgParameter)
                .load(binding.imgParameter)
                .into(imgParameter)
        }
    }

}
