import Foundation

enum EraseMode: String, Codable, CaseIterable {
    case objectRemove = "OBJECT_REMOVE"
    case textWatermarkRemove = "TEXT_WATERMARK_REMOVE"
    case peopleRemove = "PEOPLE_REMOVE"
    case customRectangle = "CUSTOM_RECTANGLE"
    case customEllipse = "CUSTOM_ELLIPSE"
    case freehandBrush = "FREEHAND_BRUSH"
}

enum ModelEngine: String, Codable, CaseIterable {
    case proPainter = "PRO_PAINTER"
    case e2fgvi = "E2FGVI"
    case fastInpaint = "FAST_INPAINT"
}

enum QualityMode: String, Codable, CaseIterable {
    case fast = "FAST"
    case standard = "STANDARD"
    case highRes = "HIGH_RES"
}

enum TaskStatus: String, Codable {
    case pending = "PENDING"
    case queued = "QUEUED"
    case processing = "PROCESSING"
    case completed = "COMPLETED"
    case failed = "FAILED"
    case cancelled = "CANCELLED"
}

struct RectDto: Codable {
    let x: Double
    let y: Double
    let width: Double
    let height: Double
    let startFrame: Int?
    let endFrame: Int?
}

struct PointDto: Codable {
    let x: Double
    let y: Double
    let timeSeconds: Double?
    let frameIndex: Int?
}

struct MaskData: Codable {
    let shapeType: String
    let boundingBox: RectDto?
    let frameBoxes: [RectDto]?
    let trackingPoints: [PointDto]?
    let brushStroke: [PointDto]?
    let brushRadius: Double?
}

struct TrimConfig: Codable {
    let startTimeSeconds: Double
    let endTimeSeconds: Double
}

struct CreateEraseTaskRequest: Codable {
    let videoId: Int64
    let eraseMode: EraseMode
    let modelEngine: ModelEngine
    let qualityMode: QualityMode
    let autoTracking: Bool
    let trimConfig: TrimConfig?
    let maskData: MaskData
}

struct EraseTaskResponse: Codable, Identifiable {
    let id: String
    let videoId: Int64
    let videoFilename: String?
    let eraseMode: EraseMode
    let modelEngine: ModelEngine
    let qualityMode: QualityMode
    let autoTracking: Bool
    let trimStartTime: Double?
    let trimEndTime: Double?
    let status: TaskStatus
    let progress: Int
    let currentFrame: Int?
    let totalFrames: Int?
    let statusMessage: String?
    let resultVideoUrl: String?
    let downloadUrl: String?
    let resultFileSize: Int64?
    let resultDurationSeconds: Double?
    let creditsDeducted: Int
    let creditsRefunded: Bool
    let errorMessage: String?
    let startedAt: String?
    let completedAt: String?
    let createdAt: String
    let updatedAt: String
}
