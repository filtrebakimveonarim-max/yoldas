import AudioToolbox
import CoreMotion
import UIKit

/// Sensör verilerinden deprem sarsıntısını ayırt eden mantık (Android sürümüyle aynı kurallar).
/// 1. Önce sakin  2. Sonra en az 4 sn güçlü ve ağırlıklı olarak yatay sallantı  3. Telefon ele alınmadı.
final class Algilayici {
    private struct Ornek {
        let t: Int64; let m: Float; let dikey: Float; let yatay: Float
        let gx: Float; let gy: Float; let gz: Float
    }

    private var ornekler: [Ornek] = []
    private var gx: Float = 0, gy: Float = 0, gz: Float = 0
    private var ilk = true
    private var sonTetik: Int64 = Int64.min / 2

    private let yercekimiKatsayi: Float = 0.03
    private let pencere: Int64 = 15_000_000_000
    private let sallantiSuresi: Int64 = 4_000_000_000
    private let sakinBas: Int64 = 6_000_000_000
    private let sakinSon: Int64 = 14_000_000_000
    private let bekleme: Int64 = 120_000_000_000

    func ivme(t: Int64, x: Float, y: Float, z: Float) -> Bool {
        if ilk { gx = x; gy = y; gz = z; ilk = false }
        gx += yercekimiKatsayi * (x - gx)
        gy += yercekimiKatsayi * (y - gy)
        gz += yercekimiKatsayi * (z - gz)
        let lx = x - gx, ly = y - gy, lz = z - gz
        let m = (lx * lx + ly * ly + lz * lz).squareRoot()
        let gBoy = max((gx * gx + gy * gy + gz * gz).squareRoot(), 0.1)
        let dikey = abs((lx * gx + ly * gy + lz * gz) / gBoy)
        let yatay = max(m * m - dikey * dikey, 0).squareRoot()

        ornekler.append(Ornek(t: t, m: m, dikey: dikey, yatay: yatay, gx: gx, gy: gy, gz: gz))
        ornekler.removeAll { t - $0.t > pencere }

        if t - sonTetik < bekleme { return false }

        let son = ornekler.filter { t - $0.t <= sallantiSuresi }
        let once = ornekler.filter { (sakinBas...sakinSon).contains(t - $0.t) }
        guard son.count >= 40, once.count >= 40 else { return false }

        // 1. Önce sakin miydi?
        if (once.map { $0.m }.max() ?? 0) > 0.8 { return false }

        // 2. Güçlü ve sürekli sallantı mı?
        let gucluOran = Float(son.filter { $0.m > 1.0 }.count) / Float(son.count)
        let ortalama = son.reduce(Float(0)) { $0 + $1.m } / Float(son.count)
        if gucluOran < 0.7 || ortalama < 1.2 { return false }

        // 2b. Ağırlıklı olarak yatay mı?
        let dikeyEtki = (son.reduce(Float(0)) { $0 + $1.dikey * $1.dikey } / Float(son.count)).squareRoot()
        let yatayEtki = (son.reduce(Float(0)) { $0 + $1.yatay * $1.yatay } / Float(son.count)).squareRoot()
        if yatayEtki < dikeyEtki { return false }

        // 3. Telefon ele alınıp çevrilmedi mi?
        guard let a = once.last, let b = son.last else { return false }
        let nokta = a.gx * b.gx + a.gy * b.gy + a.gz * b.gz
        let boy = (a.gx * a.gx + a.gy * a.gy + a.gz * a.gz).squareRoot() * (b.gx * b.gx + b.gy * b.gy + b.gz * b.gz).squareRoot()
        if boy < 1 || nokta / boy <= 0.866 { return false }

        sonTetik = t
        return true
    }
}

/// iPhone'da uygulama açıkken sensörleri izler.
/// (Apple, uygulamaların kapalıyken sürekli sensör dinlemesine izin vermez.)
final class DepremIzleyici: ObservableObject {
    static let shared = DepremIzleyici()

    @Published var acik: Bool = UserDefaults.standard.bool(forKey: "algilama") {
        didSet {
            UserDefaults.standard.set(acik, forKey: "algilama")
            if acik { baslat() } else { durdur() }
        }
    }

    private let hareket = CMMotionManager()
    private var algilayici = Algilayici()

    func baslat() {
        guard acik, hareket.isAccelerometerAvailable, !hareket.isAccelerometerActive else { return }
        algilayici = Algilayici()
        hareket.accelerometerUpdateInterval = 0.04 // saniyede 25 ölçüm
        hareket.startAccelerometerUpdates(to: .main) { [weak self] veri, _ in
            guard let self, let v = veri else { return }
            let g: Float = 9.81
            let t = Int64(v.timestamp * 1_000_000_000)
            let tetik = self.algilayici.ivme(
                t: t,
                x: Float(v.acceleration.x) * g,
                y: Float(v.acceleration.y) * g,
                z: Float(v.acceleration.z) * g
            )
            if tetik { Alarm.shared.baslat(deneme: false) }
        }
    }

    func durdur() {
        hareket.stopAccelerometerUpdates()
    }
}

/// Sarsıntı sonrası akış: SORULUYOR → cevap yoksa SINYAL (düdük + SOS ışığı).
final class Alarm: ObservableObject {
    static let shared = Alarm()

    enum Durum { case yok, soruluyor, sinyal }

    @Published private(set) var durum: Durum = .yok
    @Published private(set) var kalanSaniye = 0
    @Published private(set) var tatbikat = false
    private var sayac: Timer?

    func baslat(deneme: Bool) {
        guard durum == .yok else { return }
        durum = .soruluyor
        tatbikat = deneme
        kalanSaniye = deneme ? 20 : 60
        UIApplication.shared.isIdleTimerDisabled = true
        titret()
        Seslendirici.shared.oku((deneme ? "Bu bir tatbikat. " : "") +
            "Güçlü bir sarsıntı algıladım. İyi misin? Telefonuna dokun ya da bana cevap ver.")
        sayac = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            guard let self, self.durum == .soruluyor else { return }
            self.kalanSaniye -= 1
            if self.kalanSaniye == 30 || self.kalanSaniye == 10 {
                self.titret()
                Seslendirici.shared.oku("İyi misin? Cevap vermezsen yardım sinyalini başlatacağım.")
            }
            if self.kalanSaniye <= 0 { self.sinyalBaslat() }
        }
    }

    func sinyalBaslat() {
        sayac?.invalidate()
        sayac = nil
        durum = .sinyal
        Duduk.shared.baslat()
        SosIsik.shared.baslat()
        Seslendirici.shared.oku("Cevap alamadım. Seni bulmaları için düdük ve ışık sinyalini başlattım.")
    }

    func iyiyim() {
        sayac?.invalidate()
        sayac = nil
        Duduk.shared.durdur()
        SosIsik.shared.durdur()
        durum = .yok
        UIApplication.shared.isIdleTimerDisabled = false
        Seslendirici.shared.oku("Sevindim. Bir şeye ihtiyacın olursa Acil durum butonuna bas.")
    }

    /// Yardım istendi: geri sayım durur, afet moduna geçilir. Açık sinyaller açık kalır.
    func yardimLazim() {
        sayac?.invalidate()
        sayac = nil
        durum = .yok
    }

    private func titret() {
        UINotificationFeedbackGenerator().notificationOccurred(.warning)
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }
}
