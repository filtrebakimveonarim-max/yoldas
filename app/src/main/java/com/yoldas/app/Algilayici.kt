package com.yoldas.app

import kotlin.math.sqrt

/**
 * Telefonun sensör verilerinden deprem sarsıntısını ayırt eden mantık.
 *
 * Kural (hepsi birlikte sağlanmalı):
 *  1. ÖNCE SAKİN: Son 5–14 saniyede telefon neredeyse hareketsizdi
 *     (masada, komodinde, yerde). Cepte yürürken ya da elde tutarken tetiklenmez.
 *  2. SONRA GÜÇLÜ SALLANTI: Son 3 saniyenin çoğunda belirgin hareket var.
 *     Telefonun düşmesi tek bir darbedir, 3 saniye sürmez.
 *  3. ELE ALINMADI: Telefonun duruş yönü (yerçekimi yönü) neredeyse aynı kaldı.
 *     Telefonu alıp kaldırınca yön değişir; depremde telefon olduğu yerde sallanır.
 *
 * Basınç sensörü varsa: Kısa sürede hava basıncı yükseldiyse (birkaç metre aşağı
 * inmek gibi) "çökme şüphesi" de işaretlenir.
 *
 * Zamanlar sensör olaylarının nanosaniye cinsinden zaman damgalarıdır.
 */
class Algilayici {
    private class Ornek(val t: Long, val m: Float, val gx: Float, val gy: Float, val gz: Float)

    private val ornekler = ArrayDeque<Ornek>()
    private val basinclar = ArrayDeque<Pair<Long, Float>>()
    private var gx = 0f
    private var gy = 0f
    private var gz = 0f
    private var ilk = true
    private var sonTetik = Long.MIN_VALUE / 2

    /** Son tetiklenmede bina çökmesi şüphesi var mıydı */
    var cokmeSuphesi = false
        private set

    /** İvmeölçer örneği. Deprem algılanırsa true döner. */
    fun ivme(t: Long, x: Float, y: Float, z: Float): Boolean {
        if (ilk) {
            gx = x; gy = y; gz = z
            ilk = false
        }
        // Yavaş değişen kısım yerçekimi; kalanı telefonun hareketi
        gx += YERCEKIMI_KATSAYI * (x - gx)
        gy += YERCEKIMI_KATSAYI * (y - gy)
        gz += YERCEKIMI_KATSAYI * (z - gz)
        val lx = x - gx
        val ly = y - gy
        val lz = z - gz
        val m = sqrt(lx * lx + ly * ly + lz * lz)

        ornekler.addLast(Ornek(t, m, gx, gy, gz))
        while (ornekler.isNotEmpty() && t - ornekler.first().t > PENCERE_NS) ornekler.removeFirst()

        if (t - sonTetik < BEKLEME_NS) return false

        val son = ornekler.filter { t - it.t <= SALLANTI_NS }
        val once = ornekler.filter { t - it.t in SAKIN_BAS_NS..SAKIN_SON_NS }
        if (son.size < EN_AZ_ORNEK || once.size < EN_AZ_ORNEK) return false

        // 1. Önce sakin miydi?
        if (once.maxOf { it.m } > SAKIN_ESIK) return false

        // 2. Son 3 saniye güçlü ve sürekli sallantı mı?
        val gucluOran = son.count { it.m > SALLANTI_ESIK }.toFloat() / son.size
        val ortalama = son.sumOf { it.m.toDouble() } / son.size
        if (gucluOran < GUCLU_ORAN || ortalama < ORTALAMA_ESIK) return false

        // 3. Telefon ele alınıp çevrilmedi mi?
        if (!ayniYonde(once.last(), son.last())) return false

        cokmeSuphesi = basincYukseldi(t)
        sonTetik = t
        return true
    }

    /** Basınç örneği (hPa). */
    fun basinc(t: Long, hpa: Float) {
        basinclar.addLast(t to hpa)
        while (basinclar.isNotEmpty() && t - basinclar.first().first > PENCERE_NS) basinclar.removeFirst()
    }

    private fun basincYukseldi(t: Long): Boolean {
        val once = basinclar.filter { t - it.first in SAKIN_BAS_NS..SAKIN_SON_NS }.map { it.second }
        val simdi = basinclar.filter { t - it.first <= 2_000_000_000L }.map { it.second }
        if (once.isEmpty() || simdi.isEmpty()) return false
        return simdi.average() - once.average() > BASINC_ESIK_HPA
    }

    private fun ayniYonde(a: Ornek, b: Ornek): Boolean {
        val nokta = a.gx * b.gx + a.gy * b.gy + a.gz * b.gz
        val boy = sqrt(a.gx * a.gx + a.gy * a.gy + a.gz * a.gz) * sqrt(b.gx * b.gx + b.gy * b.gy + b.gz * b.gz)
        if (boy < 1f) return false
        return nokta / boy > YON_COS_ESIK
    }

    companion object {
        private const val YERCEKIMI_KATSAYI = 0.03f
        private const val PENCERE_NS = 15_000_000_000L
        private const val SALLANTI_NS = 3_000_000_000L
        private const val SAKIN_BAS_NS = 5_000_000_000L
        private const val SAKIN_SON_NS = 14_000_000_000L
        private const val BEKLEME_NS = 120_000_000_000L
        private const val EN_AZ_ORNEK = 30
        private const val SAKIN_ESIK = 0.8f      // m/s²
        private const val SALLANTI_ESIK = 1.0f   // m/s²
        private const val GUCLU_ORAN = 0.6f
        private const val ORTALAMA_ESIK = 1.2
        private const val YON_COS_ESIK = 0.866f  // yaklaşık 30 derece
        private const val BASINC_ESIK_HPA = 0.3   // yaklaşık 2,5 metre aşağı inme
    }
}
