package com.yoldas.app

/**
 * İnternetsiz çalışan rehber içerikleri.
 * ÖNEMLİ: Yayından önce bu metinler bir sağlık / afet uzmanına kontrol ettirilmeli.
 */
data class Adim(
    val baslik: String,
    val aciklama: String,
    /** Adım sonunda sorulacak evet/hayır sorusu (isteğe bağlı) */
    val soru: String? = null,
    val evetCevap: String? = null,
    val hayirCevap: String? = null,
    /** Cevaba göre düdük otomatik başlasın mı */
    val evetteDuduk: Boolean = false,
    val hayirdaDuduk: Boolean = false,
)

data class Rehber(
    val id: String,
    val ad: String,
    val altNot: String,
    val adimlar: List<Adim>,
)

object Rehberler {
    val liste: List<Rehber> = listOf(
        Rehber(
            id = "kanama",
            ad = "Kanama",
            altNot = "Mümkünse 112'yi ara",
            adimlar = listOf(
                Adim("Önce etrafı kontrol et.", "Yıkılma, gaz kokusu ya da yangın tehlikesi varsa önce güvenli bir yere geçin."),
                Adim("Yaraya temiz bir bezle sıkıca bastır.", "Bezi kaldırma. Kan bezden sızarsa üstüne bir bez daha koy ve bastırmaya devam et."),
                Adim("Bastırmayı bırakma.", "Bezi kaldırıp yaraya bakma. Yorulursan yanındaki birinden devralmasını iste."),
                Adim("Kişiyi yatır ve sıcak tut.", "Üstünü ört. Bilinci açıksa onunla konuşmaya devam et, yalnız bırakma."),
                Adim(
                    "Kanama durduysa bezi sar.",
                    "Bezi oynatmadan üstüne sarıp sabitle.",
                    soru = "Kanama durdu mu?",
                    evetCevap = "Çok iyi. Bezi sar, kişiyi sıcak tut ve yanından ayrılma.",
                    hayirCevap = "Bastırmaya devam et, bırakma. Yardım gelsin diye düdüğü başlatıyorum.",
                    hayirdaDuduk = true,
                ),
            ),
        ),
        Rehber(
            id = "enkaz",
            ad = "Enkaz altındaysan",
            altNot = "Düdük ve SOS ışığı afet modu ekranında",
            adimlar = listOf(
                Adim("Sakin kal, az hareket et.", "Toz kalkmasın diye ağzını ve burnunu bir bezle kapat."),
                Adim("Çakmak ya da kibrit yakma.", "Ortamda gaz kaçağı olabilir."),
                Adim("Bağırma, enerjini koru.", "Bağırmak toz yutmana ve yorulmana neden olur."),
                Adim(
                    "Boruya ya da duvara vur.",
                    "Üç kez vur, sonra dinle.",
                    soru = "Dışarıdan ses duyuyor musun?",
                    evetCevap = "Hemen üç kez vur. Seni duymaları için düdüğü başlatıyorum.",
                    hayirCevap = "Enerjini koru. Düdüğü on beş saniyede bir çalacak şekilde başlatıyorum.",
                    evetteDuduk = true,
                    hayirdaDuduk = true,
                ),
                Adim("Telefonunu koru.", "Ekranı kapalı tut, pilini idareli kullan."),
            ),
        ),
        Rehber(
            id = "gaz",
            ad = "Gaz kokusu",
            altNot = "Doğalgaz acil: 187 · Genel acil: 112",
            adimlar = listOf(
                Adim("Ateş yakma, düğmelere dokunma.", "Çakmak, kibrit, elektrik düğmesi ve prizler kıvılcım çıkarabilir."),
                Adim("Mümkünse gaz vanasını kapat.", "Vanaya güvenle ulaşamıyorsan zorlama."),
                Adim("Kapı ve pencereleri aç.", "Gazın dağılmasını sağla."),
                Adim(
                    "Binadan hemen çık.",
                    "Yanındakileri de uyar. Asansör kullanma.",
                    soru = "Binadan çıkabildin mi?",
                    evetCevap = "Güzel. Binadan uzak dur ve güvenli bir yerden yardım iste.",
                    hayirCevap = "Merdivenlerle en yakın çıkışa yönel. Asansör kullanma, ateş yakma.",
                ),
                Adim("Dışarıdan yardım iste.", "Güvenli bir yerden 187'yi ya da 112'yi ara."),
            ),
        ),
        Rehber(
            id = "panik",
            ad = "Korku ve panik",
            altNot = "Yalnız değilsin",
            adimlar = listOf(
                Adim("Buradayım, yalnız değilsin.", "Korkman çok normal. Birlikte adım adım gideceğiz."),
                Adim("Yavaşça nefes al.", "Burnundan dört saniye nefes al, ağzından yavaşça ver. Üç kez tekrarla."),
                Adim("Etrafına bak.", "Gördüğün beş şeyi aklından say. Bu, zihnini şimdiye getirir."),
                Adim(
                    "Güvende misin?",
                    "Etrafında yıkılma, yangın ya da gaz tehlikesi var mı, bir bak.",
                    soru = "Şu an güvende misin?",
                    evetCevap = "Çok iyi. Olduğun yerde dinlen. Ben buradayım.",
                    hayirCevap = "Geri dönüp afet modundaki seçeneklerden sana uyanı seçelim. Yalnız değilsin.",
                ),
                Adim("Sevdiklerine haber ver.", "Bağlantı varsa ailene güvende olduğunu bildir."),
            ),
        ),
    )

    fun bul(id: String): Rehber = liste.firstOrNull { it.id == id } ?: liste.first()
}
