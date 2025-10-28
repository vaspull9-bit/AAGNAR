package com.example.aagnar.presentation.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.example.aagnar.R
import com.example.aagnar.domain.model.Chat
import kotlin.math.absoluteValue

class ChatsAdapter(
    private var chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CircleImageView = itemView.findViewById(R.id.chatAvatar)
        private val contactName: TextView = itemView.findViewById(R.id.contactName)
        private val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        private val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadge)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)

        fun bind(chat: Chat) {
            contactName.text = chat.contactName
            lastMessage.text = chat.lastMessage
            timestamp.text = formatTimestamp(chat.timestamp)

            // Статус онлайн
            statusIndicator.visibility = if (chat.isOnline) View.VISIBLE else View.GONE

            // Бейдж непрочитанных сообщений
            if (chat.unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }

            // Цвет аватара
            val avatarColor = getColorForName(chat.contactName)
            avatar.setCircleBackgroundColor(avatarColor)

            itemView.setOnClickListener {
                onChatClick(chat)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60000 -> "только что"
                diff < 3600000 -> "${diff / 60000} мин"
                diff < 86400000 -> "${diff / 3600000} ч"
                else -> "${diff / 86400000} дн"
            }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int = chats.size

    fun updateChats(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }
}