package com.yoldas.app

import java.util.Locale

/** Kullanıcının söylediğinden çıkarılan anlam. */
enum class Niyet {
    KANAMA, ENKAZ, GAZ, PANIK, ALAN, DUDUK, ISIK,
    SONRAKI, ONCEKI, TEKRAR, DUR, EVET, HAYIR,
    BILINMIYOR,
}

/**
 * Basit, internetsiz anlama: anahtar kelimelere bakar.
 * İleride telefonda çalışan yapay zeka modeliyle değiştirilecek.
 */
object Anlayici {
    private val tr = Locale.forLanguageTag("tr-TR")

    private fun String.icerir(vararg kelimeler: String) = kelimeler.any { this.contains(it) }

    /** "Ne oldu?" sorusuna verilen cevaptan durumu çıkarır. */
    fun durum(metin: String?): Niyet {
        val t = metin?.lowercase(tr) ?: return Niyet.BILINMIYOR
        return when {
            t.icerir("kanı", "kana", "kan ", "kan.", "yara", "kesik", "kesil") -> Niyet.KANAMA
            t.icerir("enkaz", "sıkış", "altında kal", "göçük", "mahsur", "çıkamıyorum") -> Niyet.ENKAZ
            t.icerir("gaz", "koku") -> Niyet.GAZ
            t.icerir("toplanma", "nereye", "güvenli yer") -> Niyet.ALAN
            t.icerir("düdük") -> Niyet.DUDUK
            t.icerir("ışık", "fener", "sos") -> Niyet.ISIK
            t.icerir("kork", "panik", "ağlı", "titri", "sakinleş") -> Niyet.PANIK
            else -> Niyet.BILINMIYOR
        }
    }

    /** Rehber sırasında söylenen komutu ya da evet/hayır cevabını çıkarır. */
    fun komut(metin: String?): Niyet {
        val t = metin?.lowercase(tr) ?: return Niyet.BILINMIYOR
        return when {
            t.icerir("hayır", "durmadı", "devam ediyor", "olmadı", "çıkamadım", "duymuyorum", "değilim") -> Niyet.HAYIR
            t.icerir("evet", "durdu", "çıktım", "çıkabildim", "duyuyorum", "güvendeyim", "güvende") -> Niyet.EVET
            t.icerir("önceki", "geri") -> Niyet.ONCEKI
            t.icerir("tekrar", "anlamadım", "bir daha") -> Niyet.TEKRAR
            t.icerir("sonraki", "tamam", "yaptım", "devam", "ileri", "geç") -> Niyet.SONRAKI
            t.icerir("dur", "sus", "kapat", "yeter") -> Niyet.DUR
            else -> durum(metin)
        }
    }
}
