package com.aistra.hail.ui.home

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aistra.hail.R
import com.aistra.hail.app.AppInfo
import com.aistra.hail.app.HailData
import com.aistra.hail.views.*
import com.aistra.hail.utils.AppIconCache
import com.aistra.hail.utils.HPackages.myUserId
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Job

class PagerAdapter(
    private val selectedList: List<AppInfo>,
    private val flags: MutableMap<String, Int> = mutableMapOf()
) : ListAdapter<AppInfo, PagerAdapter.ViewHolder>(HomeDiff(selectedList, flags)) {
    private val boundHolders = mutableSetOf<ViewHolder>()
    lateinit var onItemClickListener: OnItemClickListener
    lateinit var onItemLongClickListener: OnItemLongClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = currentList[position]
        val frozen = info.state == AppInfo.State.FROZEN
        holder.iconJob?.cancel()
        boundHolders.add(holder)
        flags[info.packageName] = info.getFlag(selectedList)
        holder.itemView.run {
            setOnClickListener { onItemClickListener.onItemClick(info) }
            setOnLongClickListener { onItemLongClickListener.onItemLongClick(info) }
            contentDescription = "${info.name}, ${context.getString(when (info.state) {
                AppInfo.State.FROZEN -> R.string.frozen_status
                AppInfo.State.NOT_FOUND -> R.string.app_not_installed
                else -> R.string.unfrozen_status
            })}"
            findViewById<FrozenIconView>(R.id.app_icon).run {
                bindFrozenAppearance(frozen)
                info.applicationInfo?.let {
                    holder.iconJob = AppIconCache.loadIconBitmapAsync(
                        context,
                        it,
                        myUserId,
                        this,
                        HailData.grayscaleIcon && info.state == AppInfo.State.FROZEN
                    )
                } ?: run {
                    setImageDrawable(context.packageManager.defaultActivityIcon)
                    colorFilter = null
                }
            }
            findViewById<TextView>(R.id.app_name).run {
                frozenAppName(info.name, frozen, info.whitelisted)
                isEnabled = true
                when {
                    info in selectedList -> setTextColor(
                        MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary)
                    )

                    info.state == AppInfo.State.NOT_FOUND -> setTextColor(
                        MaterialColors.getColor(this, androidx.appcompat.R.attr.colorError)
                    )

                    else -> setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, HailData.homeFontSize)
            }
            findViewById<TextView>(R.id.app_frozen_state).bindFrozenCaption(frozen)
        }
    }

    fun onDestroy() {
        boundHolders.forEach { it.iconJob?.cancel() }
        boundHolders.clear()
    }

    private class HomeDiff(
        private val selectedList: List<AppInfo>, private val flags: Map<String, Int>
    ) : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            flags[oldItem.packageName] == newItem.getFlag(selectedList)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.iconJob?.cancel()
        boundHolders.remove(holder)
        super.onViewRecycled(holder)
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    fun refreshAppearance() = notifyDataSetChanged()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var iconJob: Job? = null
    }

    interface OnItemClickListener {
        fun onItemClick(info: AppInfo)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(info: AppInfo): Boolean
    }
}

private fun AppInfo.getFlag(selectedList: List<AppInfo>) =
    (1 shl state.ordinal) or (this in selectedList).shl(3) or whitelisted.shl(4)

private fun Boolean.shl(bitCount: Int) = if (this) 1 shl bitCount else 0