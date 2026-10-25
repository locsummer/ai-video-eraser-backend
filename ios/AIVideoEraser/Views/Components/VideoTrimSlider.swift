import SwiftUI

struct VideoTrimSlider: View {
    let videoDuration: Double
    let maxAllowedSeconds: Int
    let onTrimChanged: (Double, Double) -> Void

    @State private var startTime: Double = 0.0
    @State private var endTime: Double = 10.0

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Text("Trim Video")
                    .foregroundColor(.white)
                    .font(.system(size: 13, weight: .semibold))
                Spacer()
                Text(String(format: "%.1fs / Max %ds", endTime - startTime, maxAllowedSeconds))
                    .foregroundColor(AppTheme.accentPurple)
                    .font(.system(size: 12))
            }

            HStack {
                Text(String(format: "%.1fs", startTime))
                    .foregroundColor(AppTheme.textSecondary)
                    .font(.system(size: 11))
                Slider(value: $endTime, in: 1...min(videoDuration > 0 ? videoDuration : 15, Double(maxAllowedSeconds))) { _ in
                    onTrimChanged(startTime, endTime)
                }
                .accentColor(AppTheme.accentPurple)
                Text(String(format: "%.1fs", endTime))
                    .foregroundColor(AppTheme.textSecondary)
                    .font(.system(size: 11))
            }
        }
        .padding(12)
        .background(AppTheme.surfaceDark)
        .cornerRadius(12)
    }
}
