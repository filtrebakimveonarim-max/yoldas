package com.yoldas.app

import android.content.Context

/** Deprem hazırlık görevleri. Telefonda saklanır, internet gerekmez. */
object Hazirlik {
    val gorevler: List<String> = listOf(
        "Deprem çantası hazırla",
        "Çantaya su, konserve ve fener ekle",
        "Ağır eşyaları duvara sabitle",
        "Aile buluşma noktası belirle",
        "Kan grubunu ve ilaçlarını not et",
        "Geceleri telefonunu yanında tut",
        "Gaz ve elektrik vanalarının yerini öğren",
        "Türkçe çevrimdışı ses paketini indir",
        "Deprem algılamayı aç",
    )

    private fun tercihler(ctx: Context) =
        ctx.getSharedPreferences("hazirlik", Context.MODE_PRIVATE)

    fun yukle(ctx: Context): List<Boolean> =
        gorevler.indices.map { tercihler(ctx).getBoolean("g$it", false) }

    fun kaydet(ctx: Context, sira: Int, tamam: Boolean) {
        tercihler(ctx).edit().putBoolean("g$sira", tamam).apply()
    }
}
