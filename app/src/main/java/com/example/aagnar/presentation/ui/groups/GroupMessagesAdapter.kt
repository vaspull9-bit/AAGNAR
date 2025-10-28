package com.example.aagnar.presentation.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.example.aagnar.R
import com.example.aagnar.domain.model.GroupMessage
import com.example.aagnar.domain.model.MessageType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class GroupMessagesAdapter(
    private var messages: List<GroupMessage>,
    private val onMessageClick: (GroupMessage) -> Unit
) : RecyclerView.Adapter<GroupMessagesAdapter.GroupMessageViewHolder>() {

    inner class GroupMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CircleImageView = itemView.findViewById(R.id.senderAvatar)
        private val senderName: TextView = itemView.findViewById(R.id.senderName)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: GroupMessage) {
            senderName.text = message.sender
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)

            // Цвет аватара на основе имени отправителя
            val avatarColor = getColorForName(message.sender)
            avatar.setCircleBackgroundColor(avatarColor)

            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }

        private fun formatTime(timestamp: Date): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
        }

        private fun getColorForName(name: String): Int {
            val colors = listOf(
                R.color.avatar_blue,
                R.color.avatar_green,
                R.color.avatar_orange,
                R.color.avatar_purple,
                R.color.avatar_red
            )
            val index = name.hashCode().absoluteValue % colors.size
            return ContextCompat.getColor(itemView.context, colors[index])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_message, parent, false)
        return GroupMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<GroupMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}