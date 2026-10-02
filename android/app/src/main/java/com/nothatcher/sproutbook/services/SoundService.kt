package com.nothatcher.sproutbook.services

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.nothatcher.sproutbook.MainActivity
import com.nothatcher.sproutbook.R
import com.nothatcher.sproutbook.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SoundState(
    val playing: Boolean = false,
    val kind: String = "Brown",
    val volume: Float = .2f,
    val minutes: Int = 30,
    val error: String? = null,
    val paused: Boolean = false,
    val preparing: Boolean = false,
    val clock: SoundTimer = SoundTimer(),
) {
    val active get() = playing || paused || preparing
}

/** Platform-looped PCM: synthesis runs once per selection, never as an idle redraw loop. */
class SoundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var track: AudioTrack? = null
    private var generation: Job? = null
    private var timer: Job? = null
    private lateinit var manager: AudioManager
    private lateinit var focus: AudioFocusRequest
    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(AudioManager::class.java)
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes())
            .setOnAudioFocusChangeListener { if (it < 0) stopSound() }.build()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("sound", "Sound machine", NotificationManager.IMPORTANCE_LOW))
    }
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "stop" -> stopSound()
            "pause" -> if (mutable.value.playing) {
                timer?.cancel()
                track?.pause()
                mutable.value = mutable.value.copy(playing = false, paused = true,
                    clock = mutable.value.clock.pause(SystemClock.elapsedRealtime()))
                manager.abandonAudioFocusRequest(focus)
                updateNotification()
            }
            "resume" -> if (mutable.value.paused) {
                if (manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    stopSound("Audio is busy. Try again when other playback ends.")
                } else {
                    track?.play()
                    mutable.value = mutable.value.copy(playing = true, paused = false,
                        clock = mutable.value.clock.resume(SystemClock.elapsedRealtime()))
                    scheduleStop()
                    updateNotification()
                }
            }
            "volume" -> {
                val volume = intent.getFloatExtra("volume", .2f).coerceIn(0f, 1f)
                track?.setVolume(volume)
                mutable.value = mutable.value.copy(volume = volume)
            }
            "timer" -> {
                val minutes = intent.getIntExtra("minutes", 30)
                if (minutes in SoundCatalog.timers && mutable.value.active) {
                    val now = SystemClock.elapsedRealtime()
                    val clock = SoundTimer.start(minutes, now)
                    mutable.value = mutable.value.copy(minutes = minutes,
                        clock = if (mutable.value.paused || mutable.value.preparing) clock.pause(now) else clock)
                    scheduleStop()
                    updateNotification()
                }
            }
            "play" -> startSound(intent)
        }
        return START_NOT_STICKY
    }
    private fun startSound(intent: Intent) {
        val kind = intent.getStringExtra("kind") ?: "Brown"
        val minutes = intent.getIntExtra("minutes", 30)
        val volume = intent.getFloatExtra("volume", .2f).coerceIn(0f, 1f)
        if (kind !in SoundCatalog.kinds || minutes !in SoundCatalog.timers) {
            stopSound("Choose an available sound and timer.")
            return
        }
        val old = mutable.value
        val preserve = intent.getBooleanExtra("preserve", false) && old.active
        val keepPaused = preserve && old.paused
        val now = SystemClock.elapsedRealtime()
        val clock = if (preserve) old.clock.pause(now) else SoundTimer.start(minutes, now).pause(now)
        generation?.cancel()
        timer?.cancel()
        releaseTrack()
        mutable.value = SoundState(kind = kind, volume = volume, minutes = minutes,
            preparing = true, clock = clock)
        startForeground(41, notification())
        if (!keepPaused && manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            stopSound("Audio is busy. Try again when other playback ends.")
            return
        }
        generation = scope.launch {
            try {
                val pcm = withContext(Dispatchers.Default) { NoisePcm.generate(kind, 24000 * 8) }
                ensureActive()
                val audio = AudioTrack.Builder().setAudioAttributes(attributes())
                    .setAudioFormat(AudioFormat.Builder().setSampleRate(24000)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                    .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(pcm.size * 2).build()
                track = audio
                check(audio.state != AudioTrack.STATE_UNINITIALIZED)
                check(audio.write(pcm, 0, pcm.size) == pcm.size)
                check(audio.setLoopPoints(0, pcm.size, -1) == AudioTrack.SUCCESS)
                audio.setVolume(mutable.value.volume)
                if (keepPaused) {
                    mutable.value = mutable.value.copy(preparing = false, paused = true)
                    manager.abandonAudioFocusRequest(focus)
                } else {
                    audio.play()
                    mutable.value = mutable.value.copy(preparing = false, playing = true,
                        clock = mutable.value.clock.resume(SystemClock.elapsedRealtime()))
                    scheduleStop()
                }
                updateNotification()
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (com.nothatcher.sproutbook.BuildConfig.DEBUG)
                    android.util.Log.e("SproutAudio", "Playback start failed", e)
                stopSound("Sound could not start. Check your audio output and try again.")
            }
        }
    }
    private fun scheduleStop() {
        timer?.cancel()
        val remaining = mutable.value.clock.remaining(SystemClock.elapsedRealtime())
        if (mutable.value.playing && remaining != null) timer = scope.launch {
            delay(remaining)
            stopSound()
        }
    }
    private fun attributes() = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
    private fun notification(): Notification {
        val current = mutable.value
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        fun command(action: String, request: Int) = PendingIntent.getService(this, request,
            Intent(this, SoundService::class.java).setAction(action), PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, "sound").setSmallIcon(R.drawable.ic_sprout)
            .setContentTitle("${current.kind} · Sound machine")
            .setContentText(when { current.paused -> "Paused · Timer paused too"
                current.preparing -> "Preparing a quiet backdrop"
                current.minutes == 0 -> "Playing · Continuous"
                else -> "Playing · ${current.minutes} minute timer" })
            .setContentIntent(open).setOngoing(true).setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        if (!current.preparing) builder.addAction(0, if (current.paused) "Resume" else "Pause",
            command(if (current.paused) "resume" else "pause", if (current.paused) 3 else 2))
        return builder.addAction(0, "Stop", command("stop", 1)).build()
    }
    private fun updateNotification() {
        getSystemService(NotificationManager::class.java).notify(41, notification())
    }
    private fun releaseTrack() {
        track?.let { runCatching { it.pause(); it.flush() }; it.release() }
        track = null
    }
    private fun stopSound(error: String? = null) {
        generation?.cancel(); timer?.cancel(); releaseTrack()
        manager.abandonAudioFocusRequest(focus)
        mutable.value = mutable.value.copy(playing = false, paused = false, preparing = false,
            clock = SoundTimer(), error = error)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    override fun onDestroy() {
        releaseTrack(); manager.abandonAudioFocusRequest(focus); scope.cancel()
        mutable.value = mutable.value.copy(playing = false, paused = false, preparing = false,
            clock = SoundTimer())
        super.onDestroy()
    }
    companion object {
        private val mutable = MutableStateFlow(SoundState())
        val state = mutable.asStateFlow()
        fun play(context: Context, kind: String, volume: Float, minutes: Int, preserve: Boolean = false) {
            context.startForegroundService(Intent(context, SoundService::class.java).setAction("play")
                .putExtra("kind", kind).putExtra("volume", volume).putExtra("minutes", minutes)
                .putExtra("preserve", preserve))
        }
        private fun command(context: Context, action: String, configure: (Intent) -> Unit = {}) {
            if (state.value.active) context.startService(Intent(context, SoundService::class.java)
                .setAction(action).also(configure))
        }
        fun pause(context: Context) = command(context, "pause")
        fun resume(context: Context) = command(context, "resume")
        fun stop(context: Context) = command(context, "stop")
        fun volume(context: Context, value: Float) = command(context, "volume") { it.putExtra("volume", value) }
        fun timer(context: Context, minutes: Int) = command(context, "timer") { it.putExtra("minutes", minutes) }
    }
}
