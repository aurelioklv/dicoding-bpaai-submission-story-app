package com.aurelioklv.dicodingstoryapp.presentation.utils

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aurelioklv.dicodingstoryapp.R
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem
import com.aurelioklv.dicodingstoryapp.databinding.StoryItemBinding
import com.aurelioklv.dicodingstoryapp.presentation.details.DetailsActivity
import com.bumptech.glide.Glide

class StoryAdapter : PagingDataAdapter<StoryItem, StoryAdapter.ViewHolder>(DIFF_CALLBACK) {
    inner class ViewHolder(private val binding: StoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StoryItem) {
            Glide.with(itemView)
                .load(item.photoUrl)
                .placeholder(R.drawable.message)
                .into(binding.ivItemPhoto)
            binding.tvItemName.text = item.name
            binding.tvItemDescription.text = item.description
            binding.tvTime.text =
                getTimeAgo(itemView.context, getTimeMillisFromString(item.createdAt.toString()))
            val minuteRead = getReadingTimeMinute(item.description.toString())
            binding.tvReadingTime.text = itemView.context.getString(R.string.min_read, minuteRead)

            binding.root.setOnClickListener {
                val intent = Intent(itemView.context, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.EXTRA_ID, item.id)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.ivItemPhoto, "photo"),
                        Pair(binding.tvItemName, "name"),
                        Pair(binding.tvItemDescription, "description"),
                        Pair(binding.tvTime, "time"),
                        Pair(binding.tvReadingTime, "reading_time")
                    )

                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItem>() {
            override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}