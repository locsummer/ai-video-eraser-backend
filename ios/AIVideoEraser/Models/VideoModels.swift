import Foundation

struct ApiResponse<T: Codable>: Codable {
    let success: Boolean?
    let message: String?
    let data: T?
    let timestamp: String?
}

typealias Boolean = Bool

struct PageResponse<T: Codable>: Codable {
    let content: [T]
    let pageNumber: Int
    let pageSize: Int
    let totalElements: Int64
    let totalPages: Int
    let last: Bool
}

struct VideoResponse: Codable, Identifiable {
    let id: Int64
    let originalFilename: String
    let fileSize: Int64
    let mimeType: String
    let durationSeconds: Double?
    let width: Int?
    let height: Int?
    let fps: Double?
    let streamUrl: String
    let downloadUrl: String
    let thumbnailUrl: String?
    let status: String
    let createdAt: String
}
