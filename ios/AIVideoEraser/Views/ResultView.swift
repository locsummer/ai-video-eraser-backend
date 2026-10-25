import SwiftUI

struct ResultView: View {
    let taskId: String
    @StateObject private var taskViewModel = TaskViewModel()
    @Environment(\.dismiss) private var dismiss

    @State private var rating: Int = 5
    @State private var feedbackSubmitted: Bool = false

    var resultURL: URL {
        APIEndpoint.streamResult(taskId).url
    }

    var body: some View {
        ZStack {
            AppTheme.darkBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                // Erased Video Player
                ZStack(alignment: .topTrailing) {
                    VideoPlayerView(videoURL: resultURL)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)

                    // Success Pill
                    Text("Object Erased ✨")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(AppTheme.successGreen)
                        .cornerRadius(8)
                        .padding(16)
                }
                .background(Color.black)

                // Actions & Feedback
                VStack(spacing: 16) {
                    // Save Button
                    Button {
                        // Save video to Photos Library
                    } label: {
                        HStack {
                            Image(systemName: "arrow.down.to.line.circle.fill")
                            Text("Save to Camera Roll")
                                .fontWeight(.bold)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(AppTheme.accentPurple)
                        .foregroundColor(.white)
                        .cornerRadius(14)
                    }

                    // Rating & Feedback Card
                    if !feedbackSubmitted {
                        VStack(spacing: 10) {
                            Text("How was the inpainting quality?")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(.white)

                            HStack(spacing: 12) {
                                ForEach(1...5, id: \.self) { star in
                                    Image(systemName: star <= rating ? "star.fill" : "star")
                                        .foregroundColor(Color.yellow)
                                        .font(.system(size: 24))
                                        .onTapGesture {
                                            self.rating = star
                                        }
                                }
                            }

                            Button {
                                Task {
                                    await taskViewModel.submitFeedback(taskId: taskId, rating: rating, comment: nil)
                                    self.feedbackSubmitted = true
                                }
                            } label: {
                                Text("Submit Rating")
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 6)
                                    .background(AppTheme.accentCyan)
                                    .cornerRadius(8)
                            }
                        }
                        .padding(14)
                        .frame(maxWidth: .infinity)
                        .background(AppTheme.surfaceLight)
                        .cornerRadius(14)
                    } else {
                        Text("Thank you for your rating!")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(AppTheme.successGreen)
                    }
                }
                .padding(20)
                .background(AppTheme.surfaceDark)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text("Result Preview")
                    .foregroundColor(.white)
                    .fontWeight(.bold)
            }
        }
    }
}
