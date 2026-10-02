import AVFoundation
import Speech

/// Telefonun kendi Türkçe sesiyle sesli okuma (internetsiz çalışır).
final class Seslendirici: NSObject, ObservableObject, AVSpeechSynthesizerDelegate {
    static let shared = Seslendirici()

    @Published var acik = true
    private let sentez = AVSpeechSynthesizer()
    private var sonra: (() -> Void)?

    override init() {
        super.init()
        sentez.delegate = self
    }

    var turkceVar: Bool { AVSpeechSynthesisVoice(language: "tr-TR") != nil }

    /// Metni okur; bitince `sonra` çağrılır. Ses kapalıysa `sonra` hemen çağrılır.
    func oku(_ metin: String, sonra: (() -> Void)? = nil) {
        guard acik, turkceVar else {
            if let s = sonra { DispatchQueue.main.async(execute: s) }
            return
        }
        SesOturumu.hazirla()
        self.sonra = nil
        if sentez.isSpeaking { sentez.stopSpeaking(at: .immediate) }
        let soz = AVSpeechUtterance(string: metin)
        soz.voice = AVSpeechSynthesisVoice(language: "tr-TR")
        soz.rate = AVSpeechUtteranceDefaultSpeechRate * 0.95
        self.sonra = sonra
        sentez.speak(soz)
    }

    func sus() {
        sonra = nil
        if sentez.isSpeaking { sentez.stopSpeaking(at: .immediate) }
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        let b = sonra
        sonra = nil
        if let b { DispatchQueue.main.async(execute: b) }
    }
}

/// Kullanıcıyı bir kez dinler ve duyduğunu yazıya çevirir.
/// Telefon destekliyorsa internetsiz (cihaz üzerinde) tanıma kullanılır.
final class Dinleyici: ObservableObject {
    @Published private(set) var dinliyor = false
    @Published var uyari: String?

    private let tanici = SFSpeechRecognizer(locale: Locale(identifier: "tr-TR"))
    private let motor = AVAudioEngine()
    private var istek: SFSpeechAudioBufferRecognitionRequest?
    private var gorev: SFSpeechRecognitionTask?
    private var sessizlik: Timer?
    private var sonMetin = ""
    private var bitti = true
    private var geri: ((String?) -> Void)?

    static var izinliMi: Bool {
        SFSpeechRecognizer.authorizationStatus() == .authorized &&
            AVAudioSession.sharedInstance().recordPermission == .granted
    }

    func dinle(_ sonuc: @escaping (String?) -> Void) {
        izinIste { [weak self] tamam in
            guard let self else { return }
            guard tamam else {
                self.uyari = "Seni duyabilmem için mikrofon ve konuşma tanıma izni gerekli. Ayarlar'dan açabilirsin."
                sonuc(nil)
                return
            }
            self.baslat(sonuc)
        }
    }

    func durdur() {
        bitir(nil)
    }

    private func izinIste(_ tamam: @escaping (Bool) -> Void) {
        SFSpeechRecognizer.requestAuthorization { durum in
            AVAudioSession.sharedInstance().requestRecordPermission { mikrofon in
                DispatchQueue.main.async { tamam(durum == .authorized && mikrofon) }
            }
        }
    }

    private func baslat(_ sonuc: @escaping (String?) -> Void) {
        guard let tanici, tanici.isAvailable else {
            uyari = "Ses tanıma şu an kullanılamıyor. Butonlarla devam edebilirsin."
            sonuc(nil)
            return
        }
        Seslendirici.shared.sus()
        temizle()
        geri = sonuc
        bitti = false
        sonMetin = ""
        SesOturumu.hazirla()

        let yeniIstek = SFSpeechAudioBufferRecognitionRequest()
        yeniIstek.shouldReportPartialResults = true
        if tanici.supportsOnDeviceRecognition {
            yeniIstek.requiresOnDeviceRecognition = true
        }
        istek = yeniIstek

        let giris = motor.inputNode
        let bicim = giris.outputFormat(forBus: 0)
        giris.removeTap(onBus: 0)
        giris.installTap(onBus: 0, bufferSize: 1024, format: bicim) { tampon, _ in
            yeniIstek.append(tampon)
        }
        motor.prepare()
        do {
            try motor.start()
        } catch {
            uyari = "Mikrofon başlatılamadı."
            bitir(nil)
            return
        }
        dinliyor = true
        uyari = nil

        gorev = tanici.recognitionTask(with: yeniIstek) { [weak self] sonuc, hata in
            DispatchQueue.main.async {
                guard let self, !self.bitti else { return }
                if let sonuc {
                    self.sonMetin = sonuc.bestTranscription.formattedString
                    self.sessizlikKur(sure: 1.5)
                    if sonuc.isFinal { self.bitir(self.sonMetin) }
                } else if hata != nil {
                    self.bitir(self.sonMetin.isEmpty ? nil : self.sonMetin)
                }
            }
        }
        // Hiç konuşmazsa 6 saniye sonra bitir
        sessizlikKur(sure: 6)
    }

    private func sessizlikKur(sure: TimeInterval) {
        sessizlik?.invalidate()
        sessizlik = Timer.scheduledTimer(withTimeInterval: sure, repeats: false) { [weak self] _ in
            guard let self else { return }
            self.bitir(self.sonMetin.isEmpty ? nil : self.sonMetin)
        }
    }

    private func bitir(_ metin: String?) {
        guard !bitti else { return }
        bitti = true
        temizle()
        uyari = metin == nil ? (uyari ?? "Seni duyamadım.") : nil
        let g = geri
        geri = nil
        g?(metin)
    }

    private func temizle() {
        sessizlik?.invalidate()
        sessizlik = nil
        if motor.isRunning { motor.stop() }
        motor.inputNode.removeTap(onBus: 0)
        istek?.endAudio()
        gorev?.cancel()
        istek = nil
        gorev = nil
        dinliyor = false
    }
}
