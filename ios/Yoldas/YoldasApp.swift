import SwiftUI

@main
struct YoldasApp: App {
    @Environment(\.scenePhase) private var asama

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: asama) { yeni in
            switch yeni {
            case .active: DepremIzleyici.shared.baslat()
            case .background: DepremIzleyici.shared.durdur()
            default: break
            }
        }
    }
}
