package com.yoldas.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Telefonun ses tanıma özelliğiyle kullanıcıyı dinler.
 * Telefonda Türkçe çevrimdışı konuşma paketi varsa internetsiz çalışır.
 */
class Dinleyici(private val ctx: Context) {
    private var tanima: SpeechRecognizer? = null

    var dinliyor by mutableStateOf(false)
        private set

    /** Kullanıcıya gösterilecek açıklama (ör. paket eksik) */
    var uyari by mutableStateOf<String?>(null)
        private set

    val kullanilabilir: Boolean = SpeechRecognizer.isRecognitionAvailable(ctx)

    /** Bir kez dinler; duyduğu metni (ya da duyamazsa null) [sonuc]'a verir. */
    fun dinle(sonuc: (String?) -> Unit) {
        if (!kullanilabilir) {
            uyari = "Bu telefonda ses tanıma bulunamadı. Butonlarla devam edebilirsin."
            sonuc(null)
            return
        }
        Seslendirici.sus()
        tanima?.destroy()
        var bitti = false
        fun bitir(metin: String?) {
            if (bitti) return
            bitti = true
            dinliyor = false
            sonuc(metin)
        }

        val t = SpeechRecognizer.createSpeechRecognizer(ctx)
        tanima = t
        t.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                dinliyor = true
                uyari = null
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                dinliyor = false
            }

            override fun onError(error: Int) {
                uyari = when (error) {
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                    SpeechRecognizer.ERROR_SERVER ->
                        "İnternetsiz dinleyebilmem için telefona Türkçe çevrimdışı konuşma paketi indirilmeli."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Seni duyabilmem için mikrofon izni gerekli."
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Seni duyamadım."
                    else -> null
                }
                bitir(null)
            }

            override fun onResults(results: Bundle?) {
                val liste = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                bitir(liste?.joinToString(" ")?.takeIf { it.isNotBlank() })
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val istek = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        t.startListening(istek)
    }

    fun durdur() {
        tanima?.cancel()
        dinliyor = false
    }

    fun kapat() {
        tanima?.destroy()
        tanima = null
        dinliyor = false
    }
}
