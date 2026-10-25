import Foundation

struct SubscriptionPlanDto: Codable, Identifiable {
    let id: String
    let name: String
    let tier: String
    let price: Double
    let currency: String
    let period: String
    let maxDurationSeconds: Int
    let maxResolution: String
    let watermarkFree: Bool
    let priorityQueue: Bool
    let monthlyCredits: Int
    let features: [String]
}

struct SubscribeRequest: Codable {
    let planId: String
    let paymentToken: String?
}

struct SubscriptionResponse: Codable, Identifiable {
    let id: Int64
    let planName: String
    let tier: String
    let maxDurationSeconds: Int
    let maxResolution: String
    let hasWatermark: Bool
    let priorityQueue: Bool
    let startDate: String
    let endDate: String?
    let status: String
    let amountPaid: Double?
    let createdAt: String
}

struct FeedbackRequest: Codable {
    let rating: Int
    let comment: String?
    let category: String?
}

struct FeedbackResponse: Codable, Identifiable {
    let id: Int64
    let taskId: String
    let userId: Int64
    let rating: Int
    let comment: String?
    let category: String?
    let createdAt: String
}
