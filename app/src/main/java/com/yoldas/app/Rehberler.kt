package com.yoldas.app

/**
 * İnternetsiz çalışan rehber içerikleri.
 * ÖNEMLİ: Yayından önce bu metinler bir sağlık / afet uzmanına kontrol ettirilmeli.
 */
data class Adim(val baslik: String, val aciklama: String)

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
                Adim("Kanama durduysa bezi sar.", "Bezi oynatmadan üstüne sarıp sabitle. Durmadıysa bastırmaya devam et ve yardım çağır."),
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
                Adim("Boruya ya da duvara vur.", "Üç kez vur, sonra dinle. Düdük butonu da sesini duyurur."),
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
                Adim("Binadan hemen çık.", "Yanındakileri de uyar. Asansör kullanma."),
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
                Adim("Güvende misin?", "Tehlike varsa afet moduna dön; yoksa olduğun yerde dinlen."),
                Adim("Sevdiklerine haber ver.", "Bağlantı varsa ailene güvende olduğunu bildir."),
            ),
        ),
    )

    fun bul(id: String): Rehber = liste.firstOrNull { it.id == id } ?: liste.first()
}
