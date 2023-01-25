package com.mcal.worldcraft.adapters

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.mcal.droid.rugl.util.WorldUtils.WorldInfo
import com.mcal.worldcraft.R
import com.mcal.worldcraft.activity.RemoveMapListener
import com.mikepenz.fastadapter.items.AbstractItem


class WorldListAdapter : AbstractItem<WorldListAdapter.ViewHolder>() {
    @DrawableRes
    var icon: Int? = null
    var worldName: CharSequence? = null
    var gameTime: CharSequence? = null
    var gameMode: CharSequence? = null
    var worldInfo: WorldInfo? = null

    override val type: Int
        get() = R.id.main_menu_container

    override val layoutRes: Int
        get() = R.layout.item_world_list

    fun withId(id: Long): WorldListAdapter {
        this.identifier = id
        return this
    }

    fun withListener(removeMapListener: RemoveMapListener): WorldListAdapter {
        mRemoveMapListener = removeMapListener
        return this
    }

    fun withIcon(icon: Int): WorldListAdapter {
        this.icon = icon
        return this
    }

    fun withWorldName(name: String): WorldListAdapter {
        worldName = name
        return this
    }

    fun withGameTime(time: String): WorldListAdapter {
        gameTime = time
        return this
    }

    fun withGameMode(mode: String): WorldListAdapter {
        gameMode = mode
        return this
    }

    fun withWorldInfo(world: WorldInfo): WorldListAdapter {
        worldInfo = world
        return this
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        icon?.let {
            holder.iconView.setImageResource(it)
        }
        worldName?.let {
            holder.worldNameView.text = it
        }
        gameTime?.let {
            holder.gameTimeView.text = it
        }
        gameMode?.let {
            holder.gameModeView.text = it
        }
        holder.deleteWorldView.setOnClickListener {
            holder.itemView.parent?.takeIf { it is RecyclerView }?.let { view ->
                val position = (view as RecyclerView).getChildAdapterPosition(holder.itemView)
                worldInfo?.let { worldInfo ->
                    mRemoveMapListener?.removeWorld(position, worldInfo)
                }
            }
        }
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.iconView.setImageDrawable(null)
        holder.worldNameView.text = null
        holder.gameTimeView.text = null
        holder.gameModeView.text = null
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    var mRemoveMapListener: RemoveMapListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var iconView: ImageView = view.findViewById(R.id.menu_icon)
        var worldNameView: TextView = view.findViewById(R.id.world_name)
        var gameTimeView: TextView = view.findViewById(R.id.game_time)
        var gameModeView: TextView = view.findViewById(R.id.game_mode)
        var deleteWorldView: Button = view.findViewById(R.id.delete_world)
    }
}