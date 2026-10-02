package com.yoldas.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Android'in kendi metin okuma motoruyla Türkçe sesli okuma.
 * Telefonda Türkçe ses paketi yüklüyse internetsiz çalışır.
 */
object Seslendirici : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var bekleyen: String? = null
    private var bekleyenSonra: (() -> Unit)? = null
    private var bittiginde: (() -> Unit)? = null
    private val anaIs = Handler(Looper.getMainLooper())

    /** Motor hazır mı */
    var hazir by mutableStateOf(false)
        private set

    /** Telefonda Türkçe ses var mı */
    var turkceVar by mutableStateOf(true)
        private set

    /** Kullanıcı sesli okumayı açık mı tutuyor */
    var acik by mutableStateOf(true)

    fun baslat(ctx: Context) {
        if (tts == null) tts = TextToSpeech(ctx.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            turkceVar = false
            return
        }
        val sonuc = tts?.setLanguage(Locale.forLanguageTag("tr-TR"))
        turkceVar = sonuc != TextToSpeech.LANG_MISSING_DATA && sonuc != TextToSpeech.LANG_NOT_SUPPORTED
        tts?.setSpeechRate(0.9f)
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = bitti()

            @Deprecated("Eski Android sürümleri için")
            override fun onError(utteranceId: String?) = bitti()
        })
        hazir = true
        val m = bekleyen
        val s = bekleyenSonra
        bekleyen = null
        bekleyenSonra = null
        if (m != null) oku(m, s)
    }

    private fun bitti() {
        val geri = bittiginde ?: return
        bittiginde = null
        anaIs.post(geri)
    }

    /**
     * Metni sesli okur. [sonra] okuma bitince ana iş parçacığında çağrılır.
     * Ses kapalıysa ya da Türkçe ses yoksa [sonra] hemen çağrılır.
     */
    fun oku(metin: String, sonra: (() -> Unit)? = null) {
        if (!acik || (hazir && !turkceVar)) {
            sonra?.let { anaIs.post(it) }
            return
        }
        if (!hazir) {
            bekleyen = metin
            bekleyenSonra = sonra
            return
        }
        bittiginde = sonra
        tts?.speak(metin, TextToSpeech.QUEUE_FLUSH, null, "yoldas-${System.nanoTime()}")
    }

    fun sus() {
        bekleyen = null
        bekleyenSonra = null
        bittiginde = null
        tts?.stop()
    }

    fun kapat() {
        sus()
        tts?.shutdown()
        tts = null
        hazir = false
    }
}
