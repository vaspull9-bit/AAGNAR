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
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

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

        // Pool для повторного использования ViewHolder
        private val viewPool = RecyclerView.RecycledViewPool()
    }

    // DiffUtil для эффективного обновления списка
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
        // Включаем стабильные ID для производительности
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
            message.type == MessageType.SYSTEM -> TYPE_SYSTEM
            message.type == MessageType.SENT -> TYPE_TEXT_SENT
            else -> TYPE_TEXT_RECEIVED
        }
    }

    // ViewHolder для отправленных текстовых сообщений
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val statusIndicator: ImageView = itemView.findViewById(R.id.statusIndicator)

        fun bind(message: Message) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp.time)
            updateStatus(message)

            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }

        fun updateStatus(message: Message) {
            val statusRes = when {
                message.isRead -> R.drawable.ic_read
                message.isDelivered -> R.drawable.ic_delivered
                else -> R.drawable.ic_sent
            }
            statusIndicator.setImageResource(statusRes)
        }
    }

    // ViewHolder для полученных текстовых сообщений
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CircleImageView = itemView.findViewById(R.id.contactAvatar)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: Message) {
            messageText.text = message.content
            timeText.text = formatTime(message.timestamp.time)

            // Цвет аватара на основе имени отправителя
            val avatarColor = getColorForName(message.contactName)
            avatar.setCircleBackgroundColor(avatarColor)

            itemView.setOnClickListener {
                onMessageClick(message)
            }
        }
    }

    // ViewHolder для файлов
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

                // Устанавливаем иконку в зависимости от типа файла
                fileIcon.setImageResource(FileManager.getFileIconResId(fileInfo.type))

                // Настраиваем прогресс передачи
                if (fileInfo.transferProgress in 1..99) {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = fileInfo.transferProgress
                } else {
                    progressBar.visibility = View.GONE
                }

                // Настраиваем кнопку действия
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

    // ViewHolder для голосовых сообщений
    inner class VoiceMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playButton: ImageButton = itemView.findViewById(R.id.playButton)
        private val seekBar: SeekBar = itemView.findViewById(R.id.voiceSeekBar)
        private val durationText: TextView = itemView.findViewById(R.id.durationText)
        private val waveView: VoiceWaveView = itemView.findViewById(R.id.voiceWaveView)

        fun bind(message: Message) {
            message.voiceMessageInfo?.let { voiceInfo ->
                // Устанавливаем длительность
                durationText.text = formatVoiceDuration(voiceInfo.duration)
                seekBar.max = voiceInfo.duration * 1000 // конвертируем в миллисекунды

                // Проверяем воспроизводится ли это сообщение
                val isPlaying = audioViewModel?.isPlaying(message.id) == true
                updatePlayButton(isPlaying)

                // Обработчики кликов
                playButton.setOnClickListener {
                    if (isPlaying) {
                        audioViewModel?.pauseVoiceMessage()
                    } else {
                        // Если есть локальный файл, воспроизводим его
                        voiceInfo.filePath?.let { filePath ->
                            audioViewModel?.playVoiceMessage(message.id, filePath)
                        }
                    }
                }

                // Обработчик SeekBar
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            audioViewModel?.seekVoiceMessage(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                // Подписываемся на обновления прогресса
                audioViewModel?.setAudioProgressCallback { currentPos, duration ->
                    if (audioViewModel.isPlaying(message.id)) {
                        seekBar.progress = currentPos
                        durationText.text = formatVoiceDuration(currentPos / 1000)

                        // Анимируем волну
                        val amplitude = (Math.random() * 0.8 + 0.2).toFloat()
                        waveView.addAmplitude(amplitude)
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

        fun updateProgress(currentPosition: Int, duration: Int) {
            seekBar.progress = currentPosition
            durationText.text = formatVoiceDuration(currentPosition / 1000)
        }

        fun updatePlayingState(isPlaying: Boolean) {
            updatePlayButton(isPlaying)
            if (!isPlaying) {
                waveView.clear()
            }
        }
    }

    // ViewHolder для системных сообщений
    inner class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val systemText: TextView = itemView.findViewById(R.id.systemText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(message: Message) {
            systemText.text = message.content
            timeText.text = formatTime(message.timestamp.time)

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
                FileMessageViewHolder(view).apply {
                    // Используем общий pool для вложенных RecyclerView
                    // binding.nestedRecyclerView.setRecycledViewPool(viewPool)
                }
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

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
            is FileMessageViewHolder -> holder.bind(message)
            is VoiceMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
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
                            holder.updateProgress(voiceInfo.currentPosition, voiceInfo.duration * 1000)
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

    fun getMessageAt(position: Int): Message? {
        return if (position in 0 until itemCount) messages[position] else null
    }

    // Оптимизация: очистка ресурсов
    fun cleanup() {
        // Очищаем MediaPlayer и другие ресурсы
        audioViewModel?.stopVoiceMessage()
    }

    // Вспомогательные методы
    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
    }

    private fun formatVoiceDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
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

    // Методы для обновления конкретных сообщений
    fun updateMessageDeliveryStatus(messageId: String, isDelivered: Boolean, isRead: Boolean) {
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            notifyItemChanged(position, "delivery_status")
        }
    }

    fun updateVoiceMessageProgress(messageId: String, currentPosition: Int) {
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            notifyItemChanged(position, "progress_status")
        }
    }

    fun updateFileTransferProgress(messageId: String, progress: Int) {
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            notifyItemChanged(position, "transfer_progress")
        }
    }

    // View для визуализации звуковой волны (должна быть создана отдельно)
    class VoiceWaveView @JvmOverloads constructor(
        context: android.content.Context,
        attrs: android.util.AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private val wavePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#2196F3")
            style = android.graphics.Paint.Style.FILL
            strokeWidth = 3f
            isAntiAlias = true
        }

        private val amplitudes = mutableListOf<Float>()
        private val random = kotlin.random.Random(System.currentTimeMillis())

        fun addAmplitude(amplitude: Float) {
            amplitudes.add(amplitude)
            // Ограничиваем количество точек для производительности
            if (amplitudes.size > 100) {
                amplitudes.removeAt(0)
            }
            invalidate()
        }

        fun clear() {
            amplitudes.clear()
            invalidate()
        }

        override fun onDraw(canvas: android.graphics.Canvas) {
            super.onDraw(canvas)

            if (amplitudes.isEmpty()) {
                // Рисуем случайную волну в состоянии покоя
                drawRandomWave(canvas)
            } else {
                // Рисуем волну на основе амплитуд
                drawAmplitudeWave(canvas)
            }
        }

        private fun drawRandomWave(canvas: android.graphics.Canvas) {
            val centerY = height / 2f
            val path = android.graphics.Path()
            path.moveTo(0f, centerY)

            for (x in 0 until width step 5) {
                val y = centerY + random.nextFloat() * 10 - 5
                path.lineTo(x.toFloat(), y)
            }

            canvas.drawPath(path, wavePaint)
        }

        private fun drawAmplitudeWave(canvas: android.graphics.Canvas) {
            val centerY = height / 2f
            val path = android.graphics.Path()
            path.moveTo(0f, centerY)

            val step = width.toFloat() / amplitudes.size

            amplitudes.forEachIndexed { index, amplitude ->
                val x = index * step
                val y = centerY + amplitude * height / 2
                path.lineTo(x, y)
            }

            canvas.drawPath(path, wavePaint)
        }
    }
}