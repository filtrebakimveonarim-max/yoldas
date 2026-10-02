package com.yoldas.app

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Kurtarma ekiplerinin duyabilmesi için yüksek perdeden üçlü düdük sesi çalar.
 * Ses dosyası gerekmez; ses kodla üretilir. Alarm ses kanalını kullanır.
 */
object Duduk {
    private const val ORNEKLEME = 44100
    private const val FREKANS = 3000.0
    private val kapsam = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var gorev: Job? = null

    val calisiyor: Boolean get() = gorev?.isActive == true

    /** Üç kısa düdük: 0,3 sn ses, 0,15 sn sessizlik. */
    private val ses: ShortArray by lazy {
        val acik = (ORNEKLEME * 0.30).toInt()
        val kapali = (ORNEKLEME * 0.15).toInt()
        val yumusatma = (ORNEKLEME * 0.01).toInt()
        val toplam = 3 * (acik + kapali)
        val veri = ShortArray(toplam)
        var konum = 0
        repeat(3) {
            for (i in 0 until acik) {
                val zarf = min(1.0, min(i, acik - i).toDouble() / yumusatma)
                val deger = sin(2.0 * PI * FREKANS * i / ORNEKLEME) * zarf * 0.9
                veri[konum + i] = (deger * Short.MAX_VALUE).toInt().toShort()
            }
            konum += acik + kapali
        }
        veri
    }

    private suspend fun birKezCal() {
        val iz = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(ORNEKLEME)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(ses.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        try {
            iz.write(ses, 0, ses.size)
            iz.play()
            delay(ses.size * 1000L / ORNEKLEME + 100)
        } finally {
            iz.release()
        }
    }

    /** Düdüğü hemen çalar, sonra her [aralikMs] milisaniyede bir tekrarlar. */
    fun baslat(aralikMs: Long = 15_000) {
        if (calisiyor) return
        gorev = kapsam.launch {
            while (isActive) {
                birKezCal()
                delay(aralikMs)
            }
        }
    }

    fun durdur() {
        gorev?.cancel()
        gorev = null
    }
}
