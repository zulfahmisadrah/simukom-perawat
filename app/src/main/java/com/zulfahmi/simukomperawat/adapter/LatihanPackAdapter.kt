package com.zulfahmi.simukomperawat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zulfahmi.simukomperawat.databinding.ItemLatihanPackBinding
import com.zulfahmi.simukomperawat.model.LatihanPack

class LatihanPackAdapter(
    private var packs: List<LatihanPack>,
    private val onSelected: (LatihanPack) -> Unit,
) : RecyclerView.Adapter<LatihanPackAdapter.ViewHolder>() {

    fun submitList(value: List<LatihanPack>) {
        packs = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLatihanPackBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(packs[position])
    }

    override fun getItemCount(): Int = packs.size

    inner class ViewHolder(
        private val binding: ItemLatihanPackBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pack: LatihanPack) {
            binding.tvTitle.text = pack.title
            binding.tvPackNumber.text = "Paket ${pack.displayNumber}"
            binding.root.setOnClickListener { onSelected(pack) }
        }
    }
}
