import SwiftUI

// MARK: - Yönlendirme

enum Ekran: Hashable {
    case ana, hazirlik, afet, rehber(String), alan, sakin
}

struct ContentView: View {
    @ObservedObject private var alarm = Alarm.shared
    @State private var ekran: Ekran = .ana

    var body: some View {
        Group {
            switch alarm.durum {
            case .soruluyor:
                IyiMisinEkrani(yardim: {
                    alarm.yardimLazim()
                    ekran = .afet
                })
            case .sinyal:
                YardimSinyaliEkrani(afet: {
                    alarm.yardimLazim()
                    ekran = .afet
                })
            case .yok:
                icerik
            }
        }
    }

    @ViewBuilder private var icerik: some View {
        let git: (Ekran) -> Void = { ekran = $0 }
        switch ekran {
        case .ana: AnaSayfa(git: git)
        case .hazirlik: HazirlikEkrani(git: git)
        case .afet: AfetModu(git: git)
        case .rehber(let id): RehberEkrani(rehber: Rehberler.bul(id), git: git).id(id)
        case .alan: ToplanmaEkrani(git: git)
        case .sakin: SakinlesEkrani(git: git)
        }
    }
}

// MARK: - Ortak parçalar

struct BuyukButon: View {
    let yazi: String
    var zemin: Color
    var yaziRengi: Color
    var cerceve: Color? = nil
    var yukseklik: CGFloat = 72
    var boyut: CGFloat = 20
    let eylem: () -> Void

    var body: some View {
        Button(action: eylem) {
            Text(yazi)
                .font(.system(size: boyut, weight: .bold))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 16)
                .frame(maxWidth: .infinity, minHeight: yukseklik)
                .foregroundColor(yaziRengi)
                .background(zemin)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .overlay(RoundedRectangle(cornerRadius: 20).stroke(cerceve ?? .clear, lineWidth: 2))
        }
        .buttonStyle(.plain)
    }
}

struct Kart<Icerik: View>: View {
    var koyu = false
    let icerik: () -> Icerik

    init(koyu: Bool = false, @ViewBuilder icerik: @escaping () -> Icerik) {
        self.koyu = koyu
        self.icerik = icerik
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10, content: icerik)
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(koyu ? Color.geceKart : Color.gunKart)
            .clipShape(RoundedRectangle(cornerRadius: 18))
    }
}

struct GeriSatiri: View {
    let baslik: String
    var koyu = true
    let geri: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: geri) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 20, weight: .semibold))
                    .frame(width: 48, height: 48)
                    .background(koyu ? Color.geceKart : Color.gunKart)
                    .clipShape(Circle())
            }
            .foregroundColor(koyu ? .geceYazi : .gunYazi)
            .accessibilityLabel("Geri")
            Text(baslik)
                .font(.system(size: 19, weight: .bold))
                .foregroundColor(koyu ? .geceYazi : .gunYazi)
        }
    }
}

struct SesSatiri: View {
    @ObservedObject private var ses = Seslendirici.shared

    var body: some View {
        Toggle(isOn: Binding(get: { ses.acik }, set: { yeni in
            ses.acik = yeni
            if !yeni { ses.sus() }
        })) {
            VStack(alignment: .leading, spacing: 2) {
                Text("Sesli okuma").font(.system(size: 17, weight: .bold)).foregroundColor(.geceYazi)
                if !ses.turkceVar {
                    Text("Telefonunda Türkçe ses yok. Ayarlar → Erişilebilirlik → Sesli İçerik'ten indirebilirsin.")
                        .font(.system(size: 13)).foregroundColor(.geceSoluk)
                }
            }
        }
        .tint(.lamba)
        .padding(.horizontal, 14).padding(.vertical, 8)
        .background(Color.geceKart)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

struct KonusPaneli: View {
    @ObservedObject var dinleyici: Dinleyici
    let duyulan: String?
    let ipucu: String
    let eylem: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            BuyukButon(
                yazi: dinleyici.dinliyor ? "●  Dinliyorum… konuş" : "Konuş",
                zemin: dinleyici.dinliyor ? .kirmiziAcik : .lamba,
                yaziRengi: dinleyici.dinliyor ? .white : .lambaUstuYazi,
                yukseklik: 72,
                boyut: 22
            ) {
                if dinleyici.dinliyor { dinleyici.durdur() } else { eylem() }
            }
            Text(satir)
                .font(.system(size: 15))
                .foregroundColor(.geceSoluk)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
        }
    }

    private var satir: String {
        if dinleyici.dinliyor { return "Seni dinliyorum…" }
        if let u = dinleyici.uyari { return u }
        if let d = duyulan { return "Duyduğum: \"\(d)\"" }
        return ipucu
    }
}

struct Kutu: View {
    let yazi: String
    let aciklama: String
    let isaret: Color
    let eylem: () -> Void

    var body: some View {
        Button(action: eylem) {
            VStack(alignment: .leading, spacing: 6) {
                Circle().fill(isaret).frame(width: 14, height: 14)
                Spacer(minLength: 10)
                Text(yazi).font(.system(size: 18, weight: .bold)).foregroundColor(.geceYazi)
                Text(aciklama).font(.system(size: 14)).foregroundColor(.geceSoluk)
            }
            .padding(14)
            .frame(maxWidth: .infinity, minHeight: 104, alignment: .leading)
            .background(Color.geceKart)
            .clipShape(RoundedRectangle(cornerRadius: 18))
        }
        .buttonStyle(.plain)
    }
}

struct IlerlemeCubugu: View {
    let oran: Double

    var body: some View {
        GeometryReader { g in
            ZStack(alignment: .leading) {
                Capsule().fill(Color(hex: 0xEDF0F4))
                Capsule().fill(Color.lamba).frame(width: g.size.width * min(max(oran, 0), 1))
            }
        }
        .frame(height: 10)
    }
}

// MARK: - Ana sayfa

struct AnaSayfa: View {
    let git: (Ekran) -> Void
    @ObservedObject private var izleyici = DepremIzleyici.shared
    @State private var durum = Hazirlik.yukle()

    private var oran: Double { Double(durum.filter { $0 }.count) / Double(durum.count) }
    private var siradaki: String? { Hazirlik.gorevler.indices.first { !durum[$0] }.map { Hazirlik.gorevler[$0] } }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Yoldaş").font(.system(size: 30, weight: .heavy)).foregroundColor(.gunYazi)
                    Text("Afet anında ve her gün yanında.").font(.system(size: 17)).foregroundColor(.gunSoluk)

                    Kart {
                        HStack {
                            Text("Depreme hazırlık").font(.system(size: 18, weight: .bold)).foregroundColor(.gunYazi)
                            Spacer()
                            Text("%\(Int(oran * 100))").font(.system(size: 18, weight: .bold)).foregroundColor(.lambaKoyu)
                        }
                        IlerlemeCubugu(oran: oran)
                        Text(siradaki.map { "Sıradaki görev: \($0)" } ?? "Tüm görevler tamam. Hazırsın.")
                            .font(.system(size: 16)).foregroundColor(.gunYazi)
                        BuyukButon(yazi: "Görevlere git", zemin: .gunYazi, yaziRengi: .white, yukseklik: 52, boyut: 18) {
                            git(.hazirlik)
                        }
                    }

                    Kart {
                        Toggle(isOn: Binding(get: { izleyici.acik }, set: { yeni in
                            izleyici.acik = yeni
                            if yeni {
                                Hazirlik.kaydet(7, true)
                                durum = Hazirlik.yukle()
                            }
                        })) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Deprem algılama").font(.system(size: 18, weight: .bold)).foregroundColor(.gunYazi)
                                Text(izleyici.acik
                                    ? "Açık. Güçlü bir sarsıntıda sana \"İyi misin?\" diye soracağım."
                                    : "Kapalı. Açarsan telefon sarsıntıyı kendisi fark eder.")
                                    .font(.system(size: 15)).foregroundColor(.gunSoluk)
                            }
                        }
                        .tint(.yesil)
                        Text("iPhone'da yalnızca Yoldaş açıkken çalışır; Apple, uygulamaların kapalıyken sensör dinlemesine izin vermiyor.")
                            .font(.system(size: 13)).foregroundColor(.gunSoluk)
                        BuyukButon(yazi: "Algılamayı dene (tatbikat)", zemin: .clear, yaziRengi: .gunYazi, cerceve: .gunCizgi, yukseklik: 52, boyut: 17) {
                            Alarm.shared.baslat(deneme: true)
                        }
                    }

                    Kart {
                        Text("İnternetsiz çalışır").font(.system(size: 18, weight: .bold)).foregroundColor(.gunYazi)
                        Text("İlk yardım rehberleri, düdük ve SOS ışığı telefonunda kayıtlı. Şebeke çökse de çalışır.")
                            .font(.system(size: 16)).foregroundColor(.gunSoluk)
                    }
                }
                .padding(20)
            }

            VStack(spacing: 10) {
                Text("Deprem ya da acil bir durumda bu butona bas.")
                    .font(.system(size: 15)).foregroundColor(.gunSoluk)
                BuyukButon(yazi: "ACİL DURUM", zemin: .kirmizi, yaziRengi: .white, yukseklik: 84, boyut: 24) {
                    git(.afet)
                }
            }
            .padding(.horizontal, 20).padding(.bottom, 16).padding(.top, 4)
        }
        .background(Color.gun.ignoresSafeArea())
        .onAppear { durum = Hazirlik.yukle() }
    }
}

// MARK: - Hazırlık

struct HazirlikEkrani: View {
    let git: (Ekran) -> Void
    @State private var durum = Hazirlik.yukle()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                GeriSatiri(baslik: "Depreme hazırlık", koyu: false) { git(.ana) }
                Kart {
                    Text("%\(Int(Double(durum.filter { $0 }.count) / Double(durum.count) * 100)) hazırsın")
                        .font(.system(size: 18, weight: .bold)).foregroundColor(.gunYazi)
                    IlerlemeCubugu(oran: Double(durum.filter { $0 }.count) / Double(durum.count))
                    Text("Yaptığın görevlere dokunarak işaretle.").font(.system(size: 15)).foregroundColor(.gunSoluk)
                }
                Kart {
                    ForEach(Hazirlik.gorevler.indices, id: \.self) { i in
                        Button {
                            durum[i].toggle()
                            Hazirlik.kaydet(i, durum[i])
                        } label: {
                            HStack(spacing: 12) {
                                Image(systemName: durum[i] ? "checkmark.square.fill" : "square")
                                    .font(.system(size: 24))
                                    .foregroundColor(durum[i] ? .yesil : .gunSoluk)
                                Text(Hazirlik.gorevler[i])
                                    .font(.system(size: 17))
                                    .foregroundColor(durum[i] ? .gunSoluk : .gunYazi)
                                    .multilineTextAlignment(.leading)
                                Spacer()
                            }
                            .frame(minHeight: 48)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(20)
        }
        .background(Color.gun.ignoresSafeArea())
    }
}

// MARK: - Afet modu

struct AfetModu: View {
    let git: (Ekran) -> Void
    @ObservedObject private var duduk = Duduk.shared
    @ObservedObject private var isik = SosIsik.shared
    @StateObject private var dinleyici = Dinleyici()
    @State private var duyulan: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("AFET MODU")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.lambaUstuYazi)
                        .padding(.horizontal, 12).padding(.vertical, 6)
                        .background(Color.lamba)
                        .clipShape(Capsule())
                    Spacer()
                    Text("İnternetsiz çalışıyor").font(.system(size: 15)).foregroundColor(.geceSoluk)
                }
                Text("Ne oldu?").font(.system(size: 34, weight: .heavy)).foregroundColor(.geceYazi)

                KonusPaneli(dinleyici: dinleyici, duyulan: duyulan,
                            ipucu: "Örnek: \"Annemin bacağı kanıyor\", \"Enkaz altındayım\"") { anlat() }

                Text("Ya da durumuna uyan kutuya dokun:").font(.system(size: 16)).foregroundColor(.geceSoluk)
                HStack(spacing: 12) {
                    Kutu(yazi: "Yaralı var", aciklama: "Kanama, yara", isaret: .kirmiziAcik) { git(.rehber("kanama")) }
                    Kutu(yazi: "Enkaz altındayım", aciklama: "Sıkıştım, çıkamıyorum", isaret: .lamba) { git(.rehber("enkaz")) }
                }
                HStack(spacing: 12) {
                    Kutu(yazi: "Gaz kokusu", aciklama: "Kaçak şüphesi", isaret: Color(hex: 0x8FB4E8)) { git(.rehber("gaz")) }
                    Kutu(yazi: "Toplanma alanı", aciklama: "Nereye gideyim?", isaret: .yesilAcik) { git(.alan) }
                }
                Kutu(yazi: "Çok korkuyorum", aciklama: "Sakinleşmeme yardım et", isaret: .geceSoluk) { git(.rehber("panik")) }

                Text("KURTARILMANA YARDIMCI OLUR").font(.system(size: 13, weight: .bold)).foregroundColor(.geceSoluk)
                BuyukButon(
                    yazi: duduk.calisiyor ? "Düdük çalıyor · Durdur" : "Düdük çal (15 sn'de bir)",
                    zemin: duduk.calisiyor ? .lamba : .clear,
                    yaziRengi: duduk.calisiyor ? .lambaUstuYazi : .geceYazi,
                    cerceve: duduk.calisiyor ? nil : .geceCizgi,
                    yukseklik: 60
                ) {
                    if duduk.calisiyor { duduk.durdur() } else { duduk.baslat() }
                }
                if isik.destekleniyor {
                    BuyukButon(
                        yazi: isik.calisiyor ? "SOS ışığı yanıyor · Durdur" : "SOS ışığı (fener)",
                        zemin: isik.calisiyor ? .lamba : .clear,
                        yaziRengi: isik.calisiyor ? .lambaUstuYazi : .geceYazi,
                        cerceve: isik.calisiyor ? nil : .geceCizgi,
                        yukseklik: 60
                    ) {
                        if isik.calisiyor { isik.durdur() } else { isik.baslat() }
                    }
                }
                BuyukButon(yazi: "Sakinleş: nefes egzersizi", zemin: .clear, yaziRengi: .geceYazi, cerceve: .geceCizgi, yukseklik: 60) {
                    git(.sakin)
                }
                SesSatiri()
                Button {
                    duduk.durdur()
                    isik.durdur()
                    Seslendirici.shared.sus()
                    dinleyici.durdur()
                    git(.ana)
                } label: {
                    Text("Afet modundan çık")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.lamba)
                        .frame(maxWidth: .infinity, minHeight: 48)
                }
            }
            .padding(20)
        }
        .background(Color.gece.ignoresSafeArea())
        .onAppear {
            UIApplication.shared.isIdleTimerDisabled = true
            Seslendirici.shared.oku("Afet modu açık. Konuş butonuna basıp ne olduğunu anlatabilir ya da bir kutuya dokunabilirsin.")
        }
        .onDisappear {
            if Alarm.shared.durum == .yok { UIApplication.shared.isIdleTimerDisabled = false }
        }
    }

    private func anlat() {
        dinleyici.dinle { metin in
            duyulan = metin
            guard let metin else {
                if dinleyici.uyari == "Seni duyamadım." {
                    Seslendirici.shared.oku("Seni duyamadım. Tekrar Konuş'a basıp söyle ya da bir kutuya dokun.")
                }
                return
            }
            switch Anlayici.durum(metin) {
            case .kanama: git(.rehber("kanama"))
            case .enkaz: git(.rehber("enkaz"))
            case .gaz: git(.rehber("gaz"))
            case .panik: git(.rehber("panik"))
            case .alan: git(.alan)
            case .duduk:
                Duduk.shared.baslat()
                Seslendirici.shared.oku("Düdüğü başlattım. On beş saniyede bir çalacak.")
            case .isik:
                SosIsik.shared.baslat()
                Seslendirici.shared.oku(SosIsik.shared.destekleniyor ? "SOS ışığını yaktım." : "Bu telefonda fener bulunamadı.")
            default:
                Seslendirici.shared.oku("Seni tam anlayamadım. Kanama, enkaz, gaz kokusu ya da korkuyorum gibi kısa söyle, ya da bir kutuya dokun.")
            }
        }
    }
}

// MARK: - Adım adım rehber

struct RehberEkrani: View {
    let rehber: Rehber
    let git: (Ekran) -> Void
    @State private var sira = 0
    @State private var yanit: String?
    @State private var duyulan: String?
    @State private var elleriSerbest = false
    @StateObject private var dinleyici = Dinleyici()

    private var adim: Adim { rehber.adimlar[sira] }
    private var son: Bool { sira == rehber.adimlar.count - 1 }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                GeriSatiri(baslik: rehber.ad) { git(.afet) }
                Text("Adım \(sira + 1) / \(rehber.adimlar.count)").font(.system(size: 16)).foregroundColor(.geceSoluk)
                HStack(spacing: 6) {
                    ForEach(0..<rehber.adimlar.count, id: \.self) { i in
                        Capsule().fill(i <= sira ? Color.lamba : Color.geceCizgi).frame(height: 6)
                    }
                }
                Text(adim.baslik).font(.system(size: 34, weight: .heavy)).foregroundColor(.geceYazi)
                Text(adim.aciklama).font(.system(size: 20)).foregroundColor(Color(hex: 0xC9D3E0))

                if let soru = adim.soru {
                    Kart(koyu: true) {
                        Text(soru).font(.system(size: 22, weight: .bold)).foregroundColor(.lamba)
                        HStack(spacing: 12) {
                            BuyukButon(yazi: "Evet", zemin: .yesil, yaziRengi: .white, yukseklik: 60) { cevapla(true) }
                            BuyukButon(yazi: "Hayır", zemin: .kirmizi, yaziRengi: .white, yukseklik: 60) { cevapla(false) }
                        }
                        if let yanit {
                            Text(yanit).font(.system(size: 18)).foregroundColor(.geceYazi)
                        }
                    }
                }

                KonusPaneli(dinleyici: dinleyici, duyulan: duyulan,
                            ipucu: adim.soru != nil ? "\"Evet\", \"hayır\", \"tekrar\" ya da \"sonraki\" diyebilirsin"
                                : "Ellerin meşgulse \"sonraki\", \"tekrar\" ya da \"dur\" de") { dinle() }

                BuyukButon(yazi: son ? "Tamam, afet moduna dön" : "Yaptım, sonraki adım",
                           zemin: .clear, yaziRengi: .geceYazi, cerceve: .geceCizgi, yukseklik: 64) { ileri() }
                HStack(spacing: 12) {
                    BuyukButon(yazi: "Tekrar oku", zemin: .clear, yaziRengi: .geceYazi, cerceve: .geceCizgi, yukseklik: 52, boyut: 17) {
                        Seslendirici.shared.oku("\(adim.baslik) \(adim.aciklama) \(adim.soru ?? "")")
                    }
                    if sira > 0 {
                        BuyukButon(yazi: "Önceki adım", zemin: .clear, yaziRengi: .geceYazi, cerceve: .geceCizgi, yukseklik: 52, boyut: 17) {
                            sira -= 1
                        }
                    }
                }
                SesSatiri()
                Text(rehber.altNot).font(.system(size: 14)).foregroundColor(.geceSoluk).frame(maxWidth: .infinity)
            }
            .padding(20)
        }
        .background(Color.gece.ignoresSafeArea())
        .onAppear {
            UIApplication.shared.isIdleTimerDisabled = true
            adimiOku()
        }
        .onChange(of: sira) { _ in
            yanit = nil
            duyulan = nil
            adimiOku()
        }
        .onDisappear {
            Seslendirici.shared.sus()
            dinleyici.durdur()
        }
    }

    private func adimiOku() {
        let soruMetni = adim.soru.map { " \($0) Evet ya da hayır de." } ?? ""
        Seslendirici.shared.oku("Adım \(sira + 1). \(adim.baslik) \(adim.aciklama)\(soruMetni)") {
            if elleriSerbest && Dinleyici.izinliMi { dinle() }
        }
    }

    private func dinle() {
        elleriSerbest = true
        dinleyici.dinle { isle($0) }
    }

    private func ileri() {
        if son { git(.afet) } else { sira += 1 }
    }

    private func cevapla(_ evet: Bool) {
        let metin = evet ? adim.evetCevap : adim.hayirCevap
        if (evet && adim.evetteDuduk) || (!evet && adim.hayirdaDuduk) { Duduk.shared.baslat() }
        yanit = metin
        if let metin { Seslendirici.shared.oku(metin) }
    }

    private func isle(_ metin: String?) {
        duyulan = metin
        guard let metin else { return }
        switch Anlayici.komut(metin) {
        case .sonraki: ileri()
        case .onceki: if sira > 0 { sira -= 1 }
        case .tekrar:
            Seslendirici.shared.oku("\(adim.baslik) \(adim.aciklama) \(adim.soru ?? "")") {
                if elleriSerbest { dinle() }
            }
        case .dur:
            Seslendirici.shared.sus()
            elleriSerbest = false
        case .evet:
            if adim.soru != nil {
                if adim.evetteDuduk { Duduk.shared.baslat() }
                yanit = adim.evetCevap
                Seslendirici.shared.oku("\(adim.evetCevap ?? "") Hazırsan sonraki de.") { if elleriSerbest { dinle() } }
            } else {
                ileri()
            }
        case .hayir:
            if adim.soru != nil {
                if adim.hayirdaDuduk { Duduk.shared.baslat() }
                yanit = adim.hayirCevap
                Seslendirici.shared.oku("\(adim.hayirCevap ?? "") Hazırsan sonraki de.") { if elleriSerbest { dinle() } }
            }
        case .duduk:
            Duduk.shared.baslat()
            Seslendirici.shared.oku("Düdüğü başlattım.")
        case .isik:
            SosIsik.shared.baslat()
            Seslendirici.shared.oku("SOS ışığını yaktım.")
        case .kanama: if rehber.id != "kanama" { git(.rehber("kanama")) }
        case .enkaz: if rehber.id != "enkaz" { git(.rehber("enkaz")) }
        case .gaz: if rehber.id != "gaz" { git(.rehber("gaz")) }
        case .panik: if rehber.id != "panik" { git(.rehber("panik")) }
        default:
            Seslendirici.shared.oku("Anlayamadım. Sonraki, tekrar ya da dur diyebilirsin.")
        }
    }
}

// MARK: - Toplanma alanları (örnek veri)

struct ToplanmaEkrani: View {
    let git: (Ekran) -> Void
    private let alanlar = [
        ("Sahil Parkı (örnek)", "350 m · ~6 dk yürüme"),
        ("Okul bahçesi (örnek)", "800 m · ~11 dk yürüme"),
        ("Spor sahası (örnek)", "1,2 km · ~17 dk yürüme"),
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                GeriSatiri(baslik: "Toplanma alanları") { git(.afet) }
                Text("Bu bölüm henüz hazırlanıyor. Şimdilik e-Devlet'teki AFAD toplanma alanı sorgulamasından kendi alanını öğrenip not et.")
                    .font(.system(size: 16)).foregroundColor(.geceYazi)
                ForEach(alanlar.indices, id: \.self) { i in
                    Kart(koyu: true) {
                        Text(alanlar[i].0).font(.system(size: 18, weight: .bold)).foregroundColor(.geceYazi)
                        Text(alanlar[i].1).font(.system(size: 16)).foregroundColor(.geceSoluk)
                    }
                }
                Text("Yukarıdakiler örnek veridir; gerçek alanları sonraki sürümlerde ekleyeceğiz.")
                    .font(.system(size: 14)).foregroundColor(.geceSoluk)
            }
            .padding(20)
        }
        .background(Color.gece.ignoresSafeArea())
    }
}

// MARK: - Sakinleş

struct SakinlesEkrani: View {
    let git: (Ekran) -> Void
    @State private var evre = 0
    @State private var sayi = 4
    @State private var basladi = false
    private let evreler: [(String, Int)] = [("Nefes al", 4), ("Tut", 4), ("Yavaşça ver", 6)]
    private let saat = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 16) {
            GeriSatiri(baslik: "Sakinleş") { git(.afet) }
                .frame(maxWidth: .infinity, alignment: .leading)
            Spacer()
            Text(evreler[evre].0).font(.system(size: 40, weight: .heavy)).foregroundColor(.lamba)
            Text("\(sayi)").font(.system(size: 72, weight: .heavy)).foregroundColor(.geceYazi)
            Text("Yalnız değilsin. Burnundan yavaşça nefes al, ağzından ver.")
                .font(.system(size: 18)).foregroundColor(.geceSoluk).multilineTextAlignment(.center)
            Spacer()
            SesSatiri()
        }
        .padding(20)
        .background(Color.gece.ignoresSafeArea())
        .onAppear {
            UIApplication.shared.isIdleTimerDisabled = true
            Seslendirici.shared.oku("Yalnız değilsin. Birlikte nefes alalım. \(evreler[0].0)")
        }
        .onDisappear { Seslendirici.shared.sus() }
        .onReceive(saat) { _ in
            if sayi > 1 {
                sayi -= 1
            } else {
                evre = (evre + 1) % evreler.count
                sayi = evreler[evre].1
                Seslendirici.shared.oku(evreler[evre].0)
            }
        }
    }
}

// MARK: - İyi misin?

struct IyiMisinEkrani: View {
    let yardim: () -> Void
    @ObservedObject private var alarm = Alarm.shared
    @StateObject private var dinleyici = Dinleyici()
    @State private var duyulan: String?

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text((alarm.tatbikat ? "TATBİKAT · " : "") + "GÜÇLÜ SARSINTI ALGILANDI")
                    .font(.system(size: 14, weight: .bold)).foregroundColor(.geceSoluk)
                ZStack {
                    Circle().fill(Color(hex: 0x2A2418)).frame(width: 150, height: 150)
                    Text("\(alarm.kalanSaniye)").font(.system(size: 56, weight: .heavy)).foregroundColor(.lamba)
                }
                .padding(.top, 12)
                Text("İyi misin?").font(.system(size: 48, weight: .heavy)).foregroundColor(.geceYazi)
                Text("Cevap vermezsen \(alarm.kalanSaniye) saniye sonra seni bulmaları için düdük ve ışık sinyalini başlatacağım.")
                    .font(.system(size: 17)).foregroundColor(.geceSoluk).multilineTextAlignment(.center)
                KonusPaneli(dinleyici: dinleyici, duyulan: duyulan, ipucu: "\"İyiyim\" ya da \"Yardım lazım\" de") { dinle() }
                BuyukButon(yazi: "Yardım lazım", zemin: .kirmiziAcik, yaziRengi: .white, yukseklik: 76, boyut: 22, eylem: yardim)
                BuyukButon(yazi: "İyiyim", zemin: .clear, yaziRengi: .geceYazi, cerceve: .yesilAcik, yukseklik: 76, boyut: 22) {
                    alarm.iyiyim()
                }
                Button { alarm.iyiyim() } label: {
                    Text("Yanlış alarm, kapat").font(.system(size: 17, weight: .bold)).foregroundColor(.lamba).padding(14)
                }
            }
            .padding(20)
        }
        .background(Color.gece.ignoresSafeArea())
        .onAppear {
            // Soru okunduktan sonra, izin varsa kendiliğinden dinle
            DispatchQueue.main.asyncAfter(deadline: .now() + 6) {
                if alarm.durum == .soruluyor && Dinleyici.izinliMi { dinle() }
            }
        }
        .onDisappear { dinleyici.durdur() }
    }

    private func dinle() {
        dinleyici.dinle { isle($0) }
    }

    private func isle(_ metin: String?) {
        duyulan = metin
        guard let metin else { return }
        let t = metin.lowercased(with: Locale(identifier: "tr_TR"))
        let yardimKelimeleri = ["yardım", "yaralı", "kan", "sıkış", "enkaz", "hayır", "değil", "kötü"]
        if yardimKelimeleri.contains(where: { t.contains($0) }) {
            yardim()
        } else if t.contains("iyiyim") || t.contains("evet") || t.contains("iyi") {
            alarm.iyiyim()
        } else {
            Seslendirici.shared.oku("Anlayamadım. İyiysen iyiyim, değilsen yardım lazım de.")
        }
    }
}

// MARK: - Yardım sinyali

struct YardimSinyaliEkrani: View {
    let afet: () -> Void
    @ObservedObject private var alarm = Alarm.shared

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text((alarm.tatbikat ? "TATBİKAT · " : "") + "CEVAP ALINAMADI")
                    .font(.system(size: 14, weight: .bold)).foregroundColor(.kirmiziAcik)
                Text("Seni bulmalarına yardım ediyorum").font(.system(size: 32, weight: .heavy)).foregroundColor(.geceYazi)
                Kart(koyu: true) {
                    satir("Düdük 15 saniyede bir çalıyor")
                    if SosIsik.shared.destekleniyor { satir("SOS ışığı yanıp sönüyor") }
                    satir("Ekran açık tutuluyor")
                }
                Text("Yakında aile mesajı ve Bluetooth \"buradayım\" sinyali de eklenecek.")
                    .font(.system(size: 14)).foregroundColor(.geceSoluk)
                BuyukButon(yazi: "Ben iyiyim, durdur", zemin: .clear, yaziRengi: .geceYazi, cerceve: .yesilAcik, yukseklik: 76, boyut: 22) {
                    alarm.iyiyim()
                }
                BuyukButon(yazi: "Yardım lazım: afet moduna geç", zemin: .lamba, yaziRengi: .lambaUstuYazi, yukseklik: 72, eylem: afet)
            }
            .padding(20)
        }
        .background(Color.gece.ignoresSafeArea())
    }

    private func satir(_ yazi: String) -> some View {
        HStack(spacing: 12) {
            Circle().fill(Color.lamba).frame(width: 12, height: 12)
            Text(yazi).font(.system(size: 17)).foregroundColor(.geceYazi)
        }
    }
}
