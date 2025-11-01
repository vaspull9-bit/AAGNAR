package com.example.aagnar.presentation.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.domain.model.Message
import com.example.aagnar.domain.model.MessageType
import com.example.aagnar.util.FileManager
import com.example.aagnar.util.PerformanceMonitor
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.absoluteValue

class MessagesAdapter(
    private var messages: List<Message>,
    private val onMessageClick: (Message) -> Unit,
    private val onFileClick: (Message) -> Unit,
    private val audioViewModel: AudioViewModel? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT_SENT = 0
        private const val TYPE_TEXT_RECEIVED = 1
        private const val TYPE_FILE = 2
        private const val TYPE_VOICE = 3
        private const val TYPE_SYSTEM = 4
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Message, newItem: Message): Any? {
            return when {
                oldItem.isDelivered != newItem.isDelivered -> "delivery_status"
                oldItem.isRead != newItem.isRead -> "read_status"
                oldItem.voiceMessageInfo?.isPlaying != newItem.voiceMessageInfo?.isPlaying -> "playing_status"
                oldItem.voiceMessageInfo?.currentPosition != newItem.voiceMessageInfo?.currentPosition -> "progress_status"
                oldItem.fileInfo?.transferProgress != newItem.fileInfo?.transferProgress -> "transfer_progress"
                else -> null
            }
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return messages[position].id.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return when {
            message.isVoiceMessage -> TYPE_VOICE
            message.hasAttachment -> TYPE_FILE
            message.type == MessageType.SENT -> TYPE_TEXT_SENT
           else -> TYPE_TEXT_RECEIVED
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)

        fun bind(message: Message) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)
            updateStatus(message) // ← ОБНОВЛЯЕМ СТАТУС

            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }

        fun updateStatus(message: Message) {
            val statusRes = when {
                message.isRead -> R.drawable.ic_read // ✅ Прочитано
                message.isDelivered -> R.drawable.ic_delivered // 🔵 Доставлено
                else -> R.drawable.ic_sent // ⚪ Отправлено
            }
            statusIndicator.setBackgroundResource(statusRes) // ← ИСПРАВИЛ НА setBackgroundResource
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CircleImageView = itemView.findViewById(R.id.contactAvatar)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: Message) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp)

            val avatarColor = getColorForName(message.contactName, itemView.context)
            avatar.setCircleBackgroundColor(avatarColor)

            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }
    }

    inner class FileMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileIcon: ImageView = itemView.findViewById(R.id.fileIcon)
        private val fileName: TextView = itemView.findViewById(R.id.fileName)
        private val fileSize: TextView = itemView.findViewById(R.id.fileSize)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.transferProgress)
        private val fileAction: ImageButton = itemView.findViewById(R.id.fileAction)

        fun bind(message: Message) {
            message.fileInfo?.let { fileInfo ->
                fileName.text = fileInfo.name
                fileSize.text = FileManager.getReadableFileSize(fileInfo.size)
                fileIcon.setImageResource(FileManager.getFileIconResId(fileInfo.type))

                if (fileInfo.transferProgress in 1..99) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = fileInfo.transferProgress
                } else {
                    progressBar.visibility = View.GONE
                }

                fileAction.setImageResource(
                    if (message.type == MessageType.RECEIVED) R.drawable.ic_download
                    else R.drawable.ic_open
                )

                fileAction.setOnClickListener {
                    onFileClick(message)
                }

                itemView.setOnClickListener {
                    onFileClick(message)
                }
            }
        }

        fun updateProgress(progress: Int) {
            progressBar.progress = progress
            if (progress == 100) {
                progressBar.visibility = View.GONE
            }
        }
    }

    inner class VoiceMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playButton: ImageButton = itemView.findViewById(R.id.playButton)
        private val seekBar: SeekBar = itemView.findViewById(R.id.voiceSeekBar)
        private val durationText: TextView = itemView.findViewById(R.id.durationText)
        private val waveView: VoiceWaveView = itemView.findViewById(R.id.voiceWaveView)

        fun bind(message: Message) {
            message.voiceMessageInfo?.let { voiceInfo ->
                durationText.text = formatVoiceDuration(voiceInfo.duration)
                seekBar.max = voiceInfo.duration * 1000

                val isPlaying = audioViewModel?.isPlaying(message.id) == true
                updatePlayButton(isPlaying)

                playButton.setOnClickListener {
                    if (isPlaying) {
                        audioViewModel?.pauseVoiceMessage()
                    } else {
                        voiceInfo.filePath?.let { filePath ->
                            audioViewModel?.playVoiceMessage(message.id, filePath)
                        }
                    }
                }

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            audioViewModel?.seekVoiceMessage(progress)
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                audioViewModel?.setAudioProgressCallback { currentPos, duration ->
                    if (audioViewModel.isPlaying(message.id)) {
                        seekBar.progress = currentPos
                        durationText.text = formatVoiceDuration(currentPos / 1000)
                        val amplitude = (Math.random() * 0.8 + 0.2).toFloat()
                       // waveView.addAmplitude(amplitude)

                    }
                }
            }

            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }

        private fun updatePlayButton(isPlaying: Boolean) {
            playButton.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        fun updatePlayingState(isPlaying: Boolean) {
            updatePlayButton(isPlaying)
            if (!isPlaying) {
             //   waveView.clear()
            }
        }
    }

    inner class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val systemText: TextView = itemView.findViewById(R.id.systemText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: Message) {
            systemText.text = message.content
            timeText.text = formatTime(message.timestamp)

            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            TYPE_TEXT_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            TYPE_FILE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_file, parent, false)
                FileMessageViewHolder(view)
            }
            TYPE_VOICE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_voice, parent, false)
                VoiceMessageViewHolder(view)
            }
            TYPE_SYSTEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_system, parent, false)
                SystemMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        // В MessagesAdapter ДОБАВИТЬ в onBindViewHolder:
        holder.itemView.setOnLongClickListener {
            showMessageOptions(message)
            true
        }

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is FileMessageViewHolder -> holder.bind(message)
            is VoiceMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }
    private fun showMessageOptions(message: Message) {
        val options = arrayOf("Редактировать", "Удалить", "Отмена")
        androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
            .setTitle("Сообщение")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editMessage(message) // Редактировать
                    1 -> deleteMessage(message) // Удалить
                }
            }
            .show()
    }

    private fun editMessage(message: Message) {
        val dialogView = LayoutInflater.from(holder.itemView.context)
            .inflate(R.layout.dialog_edit_message, null)
        val editText = dialogView.findViewById<EditText>(R.id.editMessageInput)
        editText.setText(message.content)

        androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
            .setView(dialogView)
            .setTitle("Редактировать сообщение")
            .setPositiveButton("Сохранить") { _, _ ->
                val newContent = editText.text.toString()
                if (newContent.isNotEmpty()) {
                    onMessageEdit(message.id, newContent)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val message = messages[position]

            when (holder) {
                is SentMessageViewHolder -> {
                    if (payloads.any { it == "delivery_status" || it == "read_status" }) {
                        holder.updateStatus(message)
                    }
                }
                is FileMessageViewHolder -> {
                    if (payloads.any { it == "transfer_progress" }) {
                        message.fileInfo?.transferProgress?.let { progress ->
                            holder.updateProgress(progress)
                        }
                    }
                }
                is VoiceMessageViewHolder -> {
                    if (payloads.any { it == "playing_status" }) {
                        val isPlaying = audioViewModel?.isPlaying(message.id) == true
                        holder.updatePlayingState(isPlaying)
                    }
                    if (payloads.any { it == "progress_status" }) {
                        message.voiceMessageInfo?.let { voiceInfo ->
                            // Обновление прогресса
                        }
                    }
                }
                else -> onBindViewHolder(holder, position)
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<Message>) {
        differ.submitList(newMessages)
        messages = newMessages
    }

    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
    }

    private fun formatVoiceDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    private fun getColorForName(name: String, context: android.content.Context): Int {
        val colors = listOf(
            R.color.avatar_blue,
            R.color.avatar_green,
            R.color.avatar_orange,
            R.color.avatar_purple,
            R.color.avatar_red
        )
        val index = name.hashCode().absoluteValue % colors.size
        return ContextCompat.getColor(context, colors[index])
    }

    class VoiceWaveView @JvmOverloads constructor(
        context: android.content.Context,
        attrs: android.util.AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {
        // Реализация визуализации волны
        // ... (ваша существующая реализация)
    }
}