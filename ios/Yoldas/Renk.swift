import SwiftUI

/// Uygulamanın renkleri. Günlük ekranlar açık, afet ekranları koyu (Android sürümüyle aynı).
extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255
        )
    }

    // Günlük mod
    static let gun = Color(hex: 0xF2F4F7)
    static let gunKart = Color(hex: 0xFFFFFF)
    static let gunCizgi = Color(hex: 0xDDE3EB)
    static let gunYazi = Color(hex: 0x13203A)
    static let gunSoluk = Color(hex: 0x4A5A72)

    // Afet modu
    static let gece = Color(hex: 0x0D1420)
    static let geceKart = Color(hex: 0x172234)
    static let geceCizgi = Color(hex: 0x3A4A64)
    static let geceYazi = Color(hex: 0xEEF2F7)
    static let geceSoluk = Color(hex: 0xA9B6C8)

    // Vurgu ve anlam renkleri
    static let lamba = Color(hex: 0xF2A541)
    static let lambaKoyu = Color(hex: 0xA85A06)
    static let lambaUstuYazi = Color(hex: 0x1A1206)
    static let kirmizi = Color(hex: 0xB83A26)
    static let kirmiziAcik = Color(hex: 0xE5624F)
    static let yesil = Color(hex: 0x1E6B4E)
    static let yesilAcik = Color(hex: 0x5CC79A)
}
