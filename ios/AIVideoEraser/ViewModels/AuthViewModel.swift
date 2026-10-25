import Foundation
import SwiftUI

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: UserProfileResponse?
    @Published var errorMessage: String?
    @Published var isLoading = false

    func login(email: String, pass: String) async {
        isLoading = true
        errorMessage = nil
        do {
            let req = LoginRequest(email: email, password: pass)
            let body = try JSONEncoder().encode(req)
            let authResponse: AuthResponse = try await NetworkManager.shared.request(
                endpoint: .login,
                method: "POST",
                body: body
            )
            NetworkManager.shared.authToken = authResponse.token
            isAuthenticated = true
            await fetchProfile()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func register(email: String, pass: String, name: String) async {
        isLoading = true
        errorMessage = nil
        do {
            let req = RegisterRequest(email: email, password: pass, fullName: name)
            let body = try JSONEncoder().encode(req)
            let authResponse: AuthResponse = try await NetworkManager.shared.request(
                endpoint: .register,
                method: "POST",
                body: body
            )
            NetworkManager.shared.authToken = authResponse.token
            isAuthenticated = true
            await fetchProfile()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func fetchProfile() async {
        do {
            let profile: UserProfileResponse = try await NetworkManager.shared.request(endpoint: .profile)
            self.currentUser = profile
            self.isAuthenticated = true
        } catch {
            print("Failed to fetch profile: \(error)")
        }
    }
}
