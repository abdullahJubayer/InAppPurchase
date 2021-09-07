package com.example.inapppurchease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.in_app_design.view.*

class InAppAdapter(private val clickListener: InAppItemClickListner) : RecyclerView.Adapter<InAppAdapter.InAppViewHolder>() {
    val differ = AsyncListDiffer(this, InAppDiffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InAppViewHolder {
        return InAppViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.in_app_design,parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: InAppViewHolder, position: Int) {
        val currentItem=differ.currentList[position]
        if (!currentItem.isAlreadyPurchased){
            holder.itemView.itemName.text= currentItem.skuDetails.title
            holder.itemView.itemPrice.text= currentItem.skuDetails.price
            holder.itemView.disable.visibility= View.GONE
            holder.itemView.setOnClickListener(View.OnClickListener { clickListener.click(position) })
        }else{
            holder.itemView.isEnabled=false
            holder.itemView.itemName.text= currentItem.skuDetails.title
            holder.itemView.itemPrice.text= currentItem.skuDetails.price
            holder.itemView.disable.visibility= View.VISIBLE
        }
    }
    class InAppViewHolder(view: View) :RecyclerView.ViewHolder(view)

    object InAppDiffUtil : DiffUtil.ItemCallback<MySkuDetails>() {
        override fun areItemsTheSame(oldItem: MySkuDetails, newItem: MySkuDetails): Boolean {
            return oldItem.skuDetails.sku == newItem.skuDetails.sku
        }

        override fun areContentsTheSame(oldItem: MySkuDetails, newItem: MySkuDetails): Boolean {
            return oldItem == newItem
        }
    }
}