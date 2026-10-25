import SwiftUI

struct EditorView: View {
    let videoURL: URL
    @StateObject private var editorViewModel = EditorViewModel()
    @Environment(\.dismiss) private var dismiss

    @State private var createdTaskId: String?
    @State private var navigateToProcessing = false

    var body: some View {
        ZStack {
            AppTheme.darkBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                // Video Player with Canvas overlay
                ZStack {
                    VideoPlayerView(videoURL: videoURL)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)

                    MaskCanvasView(
                        activeTool: editorViewModel.activeTool,
                        brushRadius: editorViewModel.brushRadius
                    ) { box, trackPts, brushPts in
                        editorViewModel.boundingBox = box
                        editorViewModel.trackingPoints = trackPts
                        editorViewModel.brushPoints = brushPts
                    }
                }
                .background(Color.black)

                // Tool Toolbar & Controls
                VStack(spacing: 14) {
                    // Tool Selection
                    HStack(spacing: 20) {
                        ForEach(DrawingTool.allCases, id: \.self) { tool in
                            Button {
                                editorViewModel.activeTool = tool
                            } label: {
                                VStack(spacing: 4) {
                                    ZStack {
                                        Circle()
                                            .fill(editorViewModel.activeTool == tool ? AppTheme.accentPurple : AppTheme.surfaceLight)
                                            .frame(width: 44, height: 44)
                                        Image(systemName: toolIcon(tool))
                                            .foregroundColor(editorViewModel.activeTool == tool ? .white : AppTheme.textSecondary)
                                    }
                                    Text(tool.rawValue)
                                        .font(.system(size: 11))
                                        .foregroundColor(editorViewModel.activeTool == tool ? .white : AppTheme.textSecondary)
                                }
                            }
                        }
                    }

                    // Brush Radius if Brush tool
                    if editorViewModel.activeTool == .brush {
                        HStack {
                            Text("Size").font(.system(size: 12)).foregroundColor(AppTheme.textSecondary)
                            Slider(value: $editorViewModel.brushRadius, in: 10...50)
                                .accentColor(AppTheme.accentPurple)
                        }
                        .padding(.horizontal)
                    }

                    // Trim Slider
                    VideoTrimSlider(videoDuration: 15.0, maxAllowedSeconds: 15) { start, end in
                        editorViewModel.trimStart = start
                        editorViewModel.trimEnd = end
                    }
                    .padding(.horizontal)

                    // Erase Action Button
                    Button {
                        Task {
                            if let taskId = await editorViewModel.uploadAndCreateTask() {
                                self.createdTaskId = taskId
                                self.navigateToProcessing = true
                            }
                        }
                    } label: {
                        HStack {
                            if editorViewModel.isUploading {
                                ProgressView()
                                    .tint(.white)
                                Text("Uploading & Creating Task...")
                                    .fontWeight(.bold)
                            } else {
                                Image(systemName: "wand.and.stars")
                                Text("Erase Object (10 Credits)")
                                    .fontWeight(.bold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(AppTheme.accentPurple)
                        .foregroundColor(.white)
                        .cornerRadius(14)
                        .padding(.horizontal)
                    }
                    .disabled(editorViewModel.isUploading)
                }
                .padding(.vertical, 14)
                .background(AppTheme.surfaceDark)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("AI Mask Editor")
                    .foregroundColor(.white)
                    .fontWeight(.bold)
            }
        }
        .navigationDestination(isPresented: $navigateToProcessing) {
            if let taskId = createdTaskId {
                ProcessingView(taskId: taskId)
            }
        }
        .onAppear {
            editorViewModel.selectedVideoURL = videoURL
        }
    }

    private func toolIcon(_ tool: DrawingTool) -> String {
        switch tool {
        case .track: return "scope"
        case .rectangle: return "rectangle"
        case .ellipse: return "oval"
        case .brush: return "paintbrush.pointed"
        }
    }
}
