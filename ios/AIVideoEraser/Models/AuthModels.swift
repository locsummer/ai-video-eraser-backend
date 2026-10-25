import Foundation

struct RegisterRequest: Codable {
    let email: String
    let password: String
    let fullName: String
}

struct LoginRequest: Codable {
    let email: String
    let password: String
}

struct AuthResponse: Codable {
    let token: String
    let refreshToken: String
    let tokenType: String
    let userId: Int64
    let email: String
    let fullName: String
    let role: String
    let credits: Int
}

struct UserProfileResponse: Codable {
    let id: Int64
    let email: String
    let fullName: String
    let avatarUrl: String?
    let role: String
    let status: String
    let credits: Int
    let activeSubscription: SubscriptionResponse?
    let createdAt: String
}
