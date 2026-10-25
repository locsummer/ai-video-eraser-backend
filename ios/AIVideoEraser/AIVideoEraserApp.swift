import SwiftUI

@main
struct AIVideoEraserApp: App {
    @StateObject private var authViewModel = AuthViewModel()

    var body: some Scene {
        WindowGroup {
            HomeView()
                .environmentObject(authViewModel)
                .preferredColorScheme(.dark)
        }
    }
}
