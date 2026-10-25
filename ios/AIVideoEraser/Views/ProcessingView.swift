import SwiftUI

struct ProcessingView: View {
    let taskId: String
    @StateObject private var taskViewModel = TaskViewModel()
    @Environment(\.dismiss) private var dismiss

    @State private var navigateToResult = false

    var body: some View {
        ZStack {
            AppTheme.darkBackground.ignoresSafeArea()

            VStack(spacing: 36) {
                Spacer()

                // Circular Progress Ring
                ZStack {
                    Circle()
                        .stroke(AppTheme.surfaceLight, lineWidth: 10)
                        .frame(width: 170, height: 170)

                    Circle()
                        .trim(from: 0, to: CGFloat(taskViewModel.currentTask?.progress ?? 0) / 100.0)
                        .stroke(AppTheme.accentPurple, style: StrokeStyle(lineWidth: 10, lineCap: .round))
                        .frame(width: 170, height: 170)
                        .rotationEffect(.degrees(-90))
                        .animation(.easeInOut, value: taskViewModel.currentTask?.progress)

                    VStack(spacing: 4) {
                        Text("\(taskViewModel.currentTask?.progress ?? 0)%")
                            .font(.system(size: 38, weight: .bold))
                            .foregroundColor(.white)
                        Text("AI Rendering")
                            .font(.system(size: 12))
                            .foregroundColor(AppTheme.textSecondary)
                    }
                }

                // Stage Info Card
                VStack(spacing: 8) {
                    Text(taskViewModel.currentTask?.statusMessage ?? "Connecting to GPU cloud worker...")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)

                    let currFrame = taskViewModel.currentTask?.currentFrame ?? 0
                    let totalFrames = taskViewModel.currentTask?.totalFrames ?? 300
                    if totalFrames > 0 {
                        Text("Frame: \(currFrame) / \(totalFrames)")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(AppTheme.accentCyan)
                    }

                    Text("Temporal consistency inpainting in progress")
                        .font(.system(size: 12))
                        .foregroundColor(AppTheme.textSecondary)
                }
                .padding(20)
                .frame(maxWidth: .infinity)
                .background(AppTheme.surfaceDark)
                .cornerRadius(16)
                .padding(.horizontal, 24)

                Spacer()

                // Cancel Button
                Button {
                    Task {
                        await taskViewModel.cancelTask(taskId: taskId)
                        dismiss()
                    }
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "xmark")
                        Text("Cancel Task (Auto Refund)")
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(AppTheme.surfaceLight, lineWidth: 1)
                    )
                }

                Spacer()
            }
        }
        .navigationBarBackButtonHidden(true)
        .onAppear {
            taskViewModel.startPolling(taskId: taskId)
        }
        .onChange(of: taskViewModel.currentTask?.status) { newStatus in
            if newStatus == .completed {
                navigateToResult = true
            }
        }
        .navigationDestination(isPresented: $navigateToResult) {
            ResultView(taskId: taskId)
        }
    }
}
