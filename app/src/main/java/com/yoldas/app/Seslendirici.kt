package com.yoldas.app

import android.content.Context
import android.speech.tts.TextToSpeech
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
        hazir = true
        bekleyen?.let { oku(it) }
        bekleyen = null
    }

    fun oku(metin: String) {
        if (!acik) return
        if (!hazir) {
            bekleyen = metin
            return
        }
        if (!turkceVar) return
        tts?.speak(metin, TextToSpeech.QUEUE_FLUSH, null, "yoldas")
    }

    fun sus() {
        bekleyen = null
        tts?.stop()
    }

    fun kapat() {
        tts?.shutdown()
        tts = null
        hazir = false
    }
}
