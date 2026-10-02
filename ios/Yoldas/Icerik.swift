import Foundation

/// İnternetsiz çalışan rehber içerikleri (Android sürümüyle aynı).
/// ÖNEMLİ: Yayından önce bu metinler bir sağlık / afet uzmanına kontrol ettirilmeli.
struct Adim {
    let baslik: String
    let aciklama: String
    var soru: String? = nil
    var evetCevap: String? = nil
    var hayirCevap: String? = nil
    var evetteDuduk = false
    var hayirdaDuduk = false
}

struct Rehber {
    let id: String
    let ad: String
    let altNot: String
    let adimlar: [Adim]
}

enum Rehberler {
    static let liste: [Rehber] = [
        Rehber(id: "kanama", ad: "Kanama", altNot: "Mümkünse 112'yi ara", adimlar: [
            Adim(baslik: "Önce etrafı kontrol et.", aciklama: "Yıkılma, gaz kokusu ya da yangın tehlikesi varsa önce güvenli bir yere geçin."),
            Adim(baslik: "Yaraya temiz bir bezle sıkıca bastır.", aciklama: "Bezi kaldırma. Kan bezden sızarsa üstüne bir bez daha koy ve bastırmaya devam et."),
            Adim(baslik: "Bastırmayı bırakma.", aciklama: "Bezi kaldırıp yaraya bakma. Yorulursan yanındaki birinden devralmasını iste."),
            Adim(baslik: "Kişiyi yatır ve sıcak tut.", aciklama: "Üstünü ört. Bilinci açıksa onunla konuşmaya devam et, yalnız bırakma."),
            Adim(baslik: "Kanama durduysa bezi sar.", aciklama: "Bezi oynatmadan üstüne sarıp sabitle.",
                 soru: "Kanama durdu mu?",
                 evetCevap: "Çok iyi. Bezi sar, kişiyi sıcak tut ve yanından ayrılma.",
                 hayirCevap: "Bastırmaya devam et, bırakma. Yardım gelsin diye düdüğü başlatıyorum.",
                 hayirdaDuduk: true),
        ]),
        Rehber(id: "enkaz", ad: "Enkaz altındaysan", altNot: "Düdük ve SOS ışığı afet modu ekranında", adimlar: [
            Adim(baslik: "Sakin kal, az hareket et.", aciklama: "Toz kalkmasın diye ağzını ve burnunu bir bezle kapat."),
            Adim(baslik: "Çakmak ya da kibrit yakma.", aciklama: "Ortamda gaz kaçağı olabilir."),
            Adim(baslik: "Bağırma, enerjini koru.", aciklama: "Bağırmak toz yutmana ve yorulmana neden olur."),
            Adim(baslik: "Boruya ya da duvara vur.", aciklama: "Üç kez vur, sonra dinle.",
                 soru: "Dışarıdan ses duyuyor musun?",
                 evetCevap: "Hemen üç kez vur. Seni duymaları için düdüğü başlatıyorum.",
                 hayirCevap: "Enerjini koru. Düdüğü on beş saniyede bir çalacak şekilde başlatıyorum.",
                 evetteDuduk: true, hayirdaDuduk: true),
            Adim(baslik: "Telefonunu koru.", aciklama: "Ekranı kapalı tut, pilini idareli kullan."),
        ]),
        Rehber(id: "gaz", ad: "Gaz kokusu", altNot: "Doğalgaz acil: 187 · Genel acil: 112", adimlar: [
            Adim(baslik: "Ateş yakma, düğmelere dokunma.", aciklama: "Çakmak, kibrit, elektrik düğmesi ve prizler kıvılcım çıkarabilir."),
            Adim(baslik: "Mümkünse gaz vanasını kapat.", aciklama: "Vanaya güvenle ulaşamıyorsan zorlama."),
            Adim(baslik: "Kapı ve pencereleri aç.", aciklama: "Gazın dağılmasını sağla."),
            Adim(baslik: "Binadan hemen çık.", aciklama: "Yanındakileri de uyar. Asansör kullanma.",
                 soru: "Binadan çıkabildin mi?",
                 evetCevap: "Güzel. Binadan uzak dur ve güvenli bir yerden yardım iste.",
                 hayirCevap: "Merdivenlerle en yakın çıkışa yönel. Asansör kullanma, ateş yakma."),
            Adim(baslik: "Dışarıdan yardım iste.", aciklama: "Güvenli bir yerden 187'yi ya da 112'yi ara."),
        ]),
        Rehber(id: "panik", ad: "Korku ve panik", altNot: "Yalnız değilsin", adimlar: [
            Adim(baslik: "Buradayım, yalnız değilsin.", aciklama: "Korkman çok normal. Birlikte adım adım gideceğiz."),
            Adim(baslik: "Yavaşça nefes al.", aciklama: "Burnundan dört saniye nefes al, ağzından yavaşça ver. Üç kez tekrarla."),
            Adim(baslik: "Etrafına bak.", aciklama: "Gördüğün beş şeyi aklından say. Bu, zihnini şimdiye getirir."),
            Adim(baslik: "Güvende misin?", aciklama: "Etrafında yıkılma, yangın ya da gaz tehlikesi var mı, bir bak.",
                 soru: "Şu an güvende misin?",
                 evetCevap: "Çok iyi. Olduğun yerde dinlen. Ben buradayım.",
                 hayirCevap: "Geri dönüp afet modundaki seçeneklerden sana uyanı seçelim. Yalnız değilsin."),
            Adim(baslik: "Sevdiklerine haber ver.", aciklama: "Bağlantı varsa ailene güvende olduğunu bildir."),
        ]),
    ]

    static func bul(_ id: String) -> Rehber {
        liste.first { $0.id == id } ?? liste[0]
    }
}

/// Deprem hazırlık görevleri, telefonda saklanır.
enum Hazirlik {
    static let gorevler = [
        "Deprem çantası hazırla",
        "Çantaya su, konserve ve fener ekle",
        "Ağır eşyaları duvara sabitle",
        "Aile buluşma noktası belirle",
        "Kan grubunu ve ilaçlarını not et",
        "Geceleri telefonunu yanında tut",
        "Gaz ve elektrik vanalarının yerini öğren",
        "Deprem algılamayı aç",
    ]

    static func yukle() -> [Bool] {
        gorevler.indices.map { UserDefaults.standard.bool(forKey: "g\($0)") }
    }

    static func kaydet(_ sira: Int, _ tamam: Bool) {
        UserDefaults.standard.set(tamam, forKey: "g\(sira)")
    }
}

/// Kullanıcının söylediğinden çıkarılan anlam.
enum Niyet {
    case kanama, enkaz, gaz, panik, alan, duduk, isik
    case sonraki, onceki, tekrar, dur, evet, hayir
    case bilinmiyor
}

/// Basit, internetsiz anlama: anahtar kelimelere bakar (Android sürümüyle aynı kurallar).
enum Anlayici {
    private static func kucuk(_ metin: String) -> String {
        metin.lowercased(with: Locale(identifier: "tr_TR"))
    }

    private static func icerir(_ t: String, _ kelimeler: [String]) -> Bool {
        kelimeler.contains { t.contains($0) }
    }

    static func durum(_ metin: String?) -> Niyet {
        guard let metin else { return .bilinmiyor }
        let t = kucuk(metin) + " "
        if icerir(t, ["kanı", "kana", "kan ", "kan.", "yara", "kesik", "kesil"]) { return .kanama }
        if icerir(t, ["enkaz", "sıkış", "altında kal", "göçük", "mahsur", "çıkamıyorum"]) { return .enkaz }
        if icerir(t, ["gaz", "koku"]) { return .gaz }
        if icerir(t, ["toplanma", "nereye", "güvenli yer"]) { return .alan }
        if icerir(t, ["düdük"]) { return .duduk }
        if icerir(t, ["ışık", "fener", "sos"]) { return .isik }
        if icerir(t, ["kork", "panik", "ağlı", "titri", "sakinleş"]) { return .panik }
        return .bilinmiyor
    }

    static func komut(_ metin: String?) -> Niyet {
        guard let metin else { return .bilinmiyor }
        let t = kucuk(metin)
        if icerir(t, ["hayır", "durmadı", "devam ediyor", "olmadı", "çıkamadım", "duymuyorum", "değilim"]) { return .hayir }
        if icerir(t, ["evet", "durdu", "çıktım", "çıkabildim", "duyuyorum", "güvendeyim", "güvende"]) { return .evet }
        if icerir(t, ["önceki", "geri"]) { return .onceki }
        if icerir(t, ["tekrar", "anlamadım", "bir daha"]) { return .tekrar }
        if icerir(t, ["sonraki", "tamam", "yaptım", "devam", "ileri", "geç"]) { return .sonraki }
        if icerir(t, ["dur", "sus", "kapat", "yeter"]) { return .dur }
        return durum(metin)
    }
}
