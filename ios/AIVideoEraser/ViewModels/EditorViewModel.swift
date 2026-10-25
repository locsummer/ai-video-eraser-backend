import Foundation
import SwiftUI

@MainActor
class EditorViewModel: ObservableObject {
    @Published var selectedVideoURL: URL?
    @Published var activeTool: DrawingTool = .rectangle
    @Published var brushRadius: CGFloat = 20.0
    @Published var autoTracking: Bool = true
    @Published var eraseMode: EraseMode = .objectRemove
    @Published var modelEngine: ModelEngine = .proPainter
    @Published var qualityMode: QualityMode = .standard

    @Published var trimStart: Double = 0.0
    @Published var trimEnd: Double = 10.0

    @Published var boundingBox: RectDto?
    @Published var trackingPoints: [PointDto]?
    @Published var brushPoints: [PointDto]?

    @Published var isUploading = false
    @Published var errorMessage: String?

    func uploadAndCreateTask() async -> String? {
        guard let fileURL = selectedVideoURL else { return nil }
        isUploading = true
        errorMessage = nil

        do {
            let video = try await NetworkManager.shared.uploadVideo(fileURL: fileURL)

            let shapeType: String
            switch activeTool {
            case .rectangle: shapeType = "RECTANGLE"
            case .ellipse: shapeType = "ELLIPSE"
            case .brush: shapeType = "BRUSH"
            case .track: shapeType = "SAM_POINTS"
            }

            let mask = MaskData(
                shapeType: shapeType,
                boundingBox: boundingBox,
                frameBoxes: nil,
                trackingPoints: trackingPoints,
                brushStroke: brushPoints,
                brushRadius: Double(brushRadius)
            )

            let req = CreateEraseTaskRequest(
                videoId: video.id,
                eraseMode: eraseMode,
                modelEngine: modelEngine,
                qualityMode: qualityMode,
                autoTracking: autoTracking,
                trimConfig: TrimConfig(startTimeSeconds: trimStart, endTimeSeconds: trimEnd),
                maskData: mask
            )

            let body = try JSONEncoder().encode(req)
            let task: EraseTaskResponse = try await NetworkManager.shared.request(
                endpoint: .createEraseTask,
                method: "POST",
                body: body
            )

            isUploading = false
            return task.id
        } catch {
            isUploading = false
            errorMessage = error.localizedDescription
            return nil
        }
    }
}
