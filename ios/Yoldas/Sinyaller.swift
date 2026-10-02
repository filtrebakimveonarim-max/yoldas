import AVFoundation
import UIKit

/// Ortak ses oturumu: hoparlörden yüksek ses, sessiz modda bile çalsın, aynı anda mikrofon kullanılabilsin.
enum SesOturumu {
    static func hazirla() {
        let oturum = AVAudioSession.sharedInstance()
        try? oturum.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .duckOthers, .allowBluetooth])
        try? oturum.setActive(true)
    }
}

/// Kurtarma ekiplerinin duyabilmesi için yüksek perdeden üçlü düdük. Ses kodla üretilir.
final class Duduk: ObservableObject {
    static let shared = Duduk()

    @Published private(set) var calisiyor = false
    private var oynatici: AVAudioPlayer?
    private var zamanlayici: Timer?
    private lazy var ses: Data = Duduk.sesUret()

    func baslat(aralik: TimeInterval = 15) {
        guard !calisiyor else { return }
        calisiyor = true
        SesOturumu.hazirla()
        cal()
        zamanlayici = Timer.scheduledTimer(withTimeInterval: aralik, repeats: true) { [weak self] _ in
            self?.cal()
        }
    }

    func durdur() {
        zamanlayici?.invalidate()
        zamanlayici = nil
        oynatici?.stop()
        calisiyor = false
    }

    private func cal() {
        oynatici = try? AVAudioPlayer(data: ses)
        oynatici?.volume = 1
        oynatici?.play()
    }

    /// Üç kısa düdük (0,3 sn ses, 0,15 sn sessizlik), 3000 Hz, WAV biçiminde.
    private static func sesUret() -> Data {
        let ornekleme = 44100
        let frekans = 3000.0
        let acik = Int(Double(ornekleme) * 0.30)
        let kapali = Int(Double(ornekleme) * 0.15)
        let yumusatma = Int(Double(ornekleme) * 0.01)
        var ornekler = [Int16](repeating: 0, count: 3 * (acik + kapali))
        var konum = 0
        for _ in 0..<3 {
            for i in 0..<acik {
                let zarf = min(1.0, Double(min(i, acik - i)) / Double(yumusatma))
                let deger = sin(2.0 * Double.pi * frekans * Double(i) / Double(ornekleme)) * zarf * 0.9
                ornekler[konum + i] = Int16(deger * Double(Int16.max))
            }
            konum += acik + kapali
        }

        var veri = Data()
        func ekle32(_ d: UInt32) { var x = d.littleEndian; withUnsafeBytes(of: &x) { veri.append(contentsOf: $0) } }
        func ekle16(_ d: UInt16) { var x = d.littleEndian; withUnsafeBytes(of: &x) { veri.append(contentsOf: $0) } }
        let veriBoyu = UInt32(ornekler.count * 2)
        veri.append(contentsOf: Array("RIFF".utf8)); ekle32(36 + veriBoyu)
        veri.append(contentsOf: Array("WAVE".utf8))
        veri.append(contentsOf: Array("fmt ".utf8)); ekle32(16); ekle16(1); ekle16(1)
        ekle32(UInt32(ornekleme)); ekle32(UInt32(ornekleme * 2)); ekle16(2); ekle16(16)
        veri.append(contentsOf: Array("data".utf8)); ekle32(veriBoyu)
        for o in ornekler { ekle16(UInt16(bitPattern: o)) }
        return veri
    }
}

/// Telefonun fenerini Mors alfabesiyle SOS (··· ––– ···) olarak yakıp söndürür.
final class SosIsik: ObservableObject {
    static let shared = SosIsik()

    @Published private(set) var calisiyor = false
    private var gorev: Task<Void, Never>?

    var destekleniyor: Bool { AVCaptureDevice.default(for: .video)?.hasTorch ?? false }

    func baslat() {
        guard !calisiyor, let cihaz = AVCaptureDevice.default(for: .video), cihaz.hasTorch else { return }
        calisiyor = true
        gorev = Task.detached {
            let birim: UInt64 = 250_000_000
            let desen = [1, 1, 1, 3, 3, 3, 1, 1, 1]
            while !Task.isCancelled {
                for (i, uzunluk) in desen.enumerated() {
                    if Task.isCancelled { break }
                    SosIsik.fener(cihaz, acik: true)
                    try? await Task.sleep(nanoseconds: birim * UInt64(uzunluk))
                    SosIsik.fener(cihaz, acik: false)
                    try? await Task.sleep(nanoseconds: (i == 2 || i == 5) ? birim * 3 : birim)
                }
                try? await Task.sleep(nanoseconds: birim * 7)
            }
            SosIsik.fener(cihaz, acik: false)
        }
    }

    func durdur() {
        gorev?.cancel()
        gorev = nil
        calisiyor = false
        if let cihaz = AVCaptureDevice.default(for: .video), cihaz.hasTorch {
            SosIsik.fener(cihaz, acik: false)
        }
    }

    private static func fener(_ cihaz: AVCaptureDevice, acik: Bool) {
        do {
            try cihaz.lockForConfiguration()
            cihaz.torchMode = acik ? .on : .off
            cihaz.unlockForConfiguration()
        } catch {}
    }
}
